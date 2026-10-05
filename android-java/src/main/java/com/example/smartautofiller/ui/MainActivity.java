package com.example.smartautofiller.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartautofiller.R;
import com.example.smartautofiller.database.AppDatabase;
import com.example.smartautofiller.database.ProfileJsonSerializer;
import com.example.smartautofiller.model.ProfileSection;
import com.example.smartautofiller.model.SectionField;
import com.example.smartautofiller.model.UserProfile;
import com.example.smartautofiller.security.PinManager;
import com.example.smartautofiller.service.SmartAccessibilityService;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Dashboard Activity for profile management, accessibility service status,
 * floating bubble controls, and document scanning integration.
 */
public class MainActivity extends AppCompatActivity implements ProfileAdapter.OnProfileClickListener {

    private AppDatabase db;
    private ProfileAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private TextView tvServiceStatus;
    private Switch switchBubble;

    private SharedPreferences prefs;

    // Temporary references for dialog autofill from camera scan
    private ActivityResultLauncher<Intent> cameraLauncher;
    private EditText currentEtFullName;
    private EditText currentEtEmail;
    private EditText currentEtPhone;
    private EditText currentEtAddress;
    private EditText currentEtProfileName;
    private final List<SectionField> currentScannedFields = new ArrayList<>();
    private String currentDocType = "Scanned Section";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDatabase(this);
        prefs = getSharedPreferences("autofill_prefs", MODE_PRIVATE);

        setupCameraLauncher();
        initViews();
        setupRecyclerView();
        setupServiceToggle();
        loadProfiles();
    }

    private void setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String name = data.getStringExtra("detected_name");
                        String email = data.getStringExtra("detected_email");
                        String phone = data.getStringExtra("detected_phone");
                        String address = data.getStringExtra("detected_address");
                        String docType = data.getStringExtra("doc_type");

                        String[] labels = data.getStringArrayExtra("section_labels");
                        String[] values = data.getStringArrayExtra("section_values");

                        currentScannedFields.clear();
                        if (labels != null && values != null && labels.length == values.length) {
                            for (int i = 0; i < labels.length; i++) {
                                currentScannedFields.add(new SectionField(labels[i], values[i]));
                            }
                        }

                        if (docType != null && !docType.isEmpty()) {
                            currentDocType = docType;
                        }

                        if (currentEtFullName != null && name != null && !name.isEmpty()) {
                            currentEtFullName.setText(name);
                        }
                        if (currentEtEmail != null && email != null && !email.isEmpty()) {
                            currentEtEmail.setText(email);
                        }
                        if (currentEtPhone != null && phone != null && !phone.isEmpty()) {
                            currentEtPhone.setText(phone);
                        }
                        if (currentEtAddress != null && address != null && !address.isEmpty()) {
                            currentEtAddress.setText(address);
                        }
                        if (currentEtProfileName != null && currentEtProfileName.getText().toString().isEmpty()) {
                            currentEtProfileName.setText(currentDocType);
                        }

                        Toast.makeText(this, "Autofilled from " + currentDocType + " (" + currentScannedFields.size() + " fields)", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStateUI();
        loadProfiles(); // Refresh list when returning from ProfileCreateActivity
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_profiles);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        tvServiceStatus = findViewById(R.id.tv_service_status);
        switchBubble = findViewById(R.id.switch_bubble);

        Button btnAddProfile = findViewById(R.id.btn_add_profile);
        btnAddProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileCreateActivity.class);
            startActivity(intent);
        });

        Button btnBackup = findViewById(R.id.btn_export_import);
        btnBackup.setOnClickListener(v -> showBackupDialog());

        Button btnSecurity = findViewById(R.id.btn_security_pin);
        if (btnSecurity != null) {
            btnSecurity.setOnClickListener(v -> showSecurityDialog());
        }
    }

    private void setupRecyclerView() {
        adapter = new ProfileAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupServiceToggle() {
        switchBubble.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return; // ignore programmatic changes

            if (isChecked) {
                // Check Overlay Permission first
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Please grant Overlay Permission", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    switchBubble.setChecked(false);
                    return;
                }

                // Check Accessibility Service
                if (SmartAccessibilityService.getInstance() == null) {
                    Toast.makeText(this, "Enable 'Universal AI Autofill' in Accessibility Settings", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    switchBubble.setChecked(false);
                    return;
                }

                prefs.edit().putBoolean("bubble_enabled", true).apply();
                SmartAccessibilityService.getInstance().setBubbleVisible(true);
            } else {
                prefs.edit().putBoolean("bubble_enabled", false).apply();
                if (SmartAccessibilityService.getInstance() != null) {
                    SmartAccessibilityService.getInstance().setBubbleVisible(false);
                }
            }
            updateServiceStateUI();
        });
    }

    private void updateServiceStateUI() {
        boolean accessibilityOn = SmartAccessibilityService.getInstance() != null;
        boolean bubbleOn = prefs.getBoolean("bubble_enabled", false);

        if (!accessibilityOn) {
            tvServiceStatus.setText("Accessibility Service is OFF. Tap to enable.");
            switchBubble.setChecked(false);
        } else if (bubbleOn) {
            tvServiceStatus.setText("Assistant Active — Floating bubble visible on screen.");
            switchBubble.setChecked(true);
            SmartAccessibilityService.getInstance().setBubbleVisible(true);
        } else {
            tvServiceStatus.setText("Assistant Ready — Toggle on to show bubble.");
            switchBubble.setChecked(false);
        }
    }

    private void loadProfiles() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<UserProfile> profiles = db.userProfileDao().getAllProfilesList();
            new Handler(Looper.getMainLooper()).post(() -> {
                adapter.setProfiles(profiles);
                tvEmptyState.setVisibility(profiles.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void showAddProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_profile, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        currentEtProfileName = dialogView.findViewById(R.id.et_profile_name);
        currentEtFullName = dialogView.findViewById(R.id.et_full_name);
        currentEtEmail = dialogView.findViewById(R.id.et_email);
        currentEtPhone = dialogView.findViewById(R.id.et_phone);
        currentEtAddress = dialogView.findViewById(R.id.et_address);
        Button btnSave = dialogView.findViewById(R.id.btn_save_profile);
        Button btnScan = dialogView.findViewById(R.id.btn_scan_document);

        btnScan.setOnClickListener(v -> {
            Intent scanIntent = new Intent(this, CameraActivity.class);
            cameraLauncher.launch(scanIntent);
        });

        btnSave.setOnClickListener(v -> {
            String pName = currentEtProfileName.getText().toString().trim();
            String fName = currentEtFullName.getText().toString().trim();
            String email = currentEtEmail.getText().toString().trim();
            String phone = currentEtPhone.getText().toString().trim();
            String addr = currentEtAddress.getText().toString().trim();

            if (pName.isEmpty() || fName.isEmpty()) {
                Toast.makeText(this, "Please enter Profile Name and Full Name", Toast.LENGTH_SHORT).show();
                return;
            }

            UserProfile newProfile = new UserProfile(pName, fName, email, phone, addr);

            // Attach scanned section if available
            if (!currentScannedFields.isEmpty()) {
                ProfileSection section = new ProfileSection(currentDocType, "🆔");
                section.setFields(new ArrayList<>(currentScannedFields));
                newProfile.getSections().add(section);
                currentScannedFields.clear();
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.userProfileDao().insertProfile(newProfile);
                loadProfiles();
            });

            dialog.dismiss();
            Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void showBackupDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Backup & Export")
                .setMessage("Export profiles to JSON or view options.")
                .setPositiveButton("Export JSON", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        List<UserProfile> list = db.userProfileDao().getAllProfilesList();
                        String json = ProfileJsonSerializer.exportProfilesToJson(list);
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("application/json");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, json);
                            startActivity(Intent.createChooser(shareIntent, "Export Profiles JSON"));
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSecurityDialog() {
        PinManager pinManager = new PinManager(this);
        if (pinManager.isPinSet()) {
            new AlertDialog.Builder(this)
                    .setTitle("🔐 PIN Lock Security")
                    .setMessage("App is protected by a 4-digit PIN.")
                    .setPositiveButton("Change PIN", (dialog, which) -> {
                        Intent intent = new Intent(this, PinLockActivity.class);
                        intent.putExtra(PinLockActivity.EXTRA_MODE, PinLockActivity.MODE_SETUP);
                        startActivity(intent);
                    })
                    .setNeutralButton("Remove PIN", (dialog, which) -> {
                        pinManager.clearPin();
                        Toast.makeText(this, "PIN protection removed", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("🔐 Setup PIN Lock")
                    .setMessage("Protect your sensitive identity numbers (Aadhaar, PAN, Passport) with a 4-digit PIN.")
                    .setPositiveButton("Set PIN Now", (dialog, which) -> {
                        Intent intent = new Intent(this, PinLockActivity.class);
                        intent.putExtra(PinLockActivity.EXTRA_MODE, PinLockActivity.MODE_SETUP);
                        startActivity(intent);
                    })
                    .setNegativeButton("Later", null)
                    .show();
        }
    }

    @Override
    public void onProfileClick(UserProfile profile) {
        if (profile == null) return;
        Intent intent = new Intent(this, ProfileViewActivity.class);
        intent.putExtra(ProfileViewActivity.EXTRA_PROFILE_ID, profile.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(UserProfile profile) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete '" + profile.getProfileName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        db.userProfileDao().deleteProfile(profile);
                        loadProfiles();
                    });
                    Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
