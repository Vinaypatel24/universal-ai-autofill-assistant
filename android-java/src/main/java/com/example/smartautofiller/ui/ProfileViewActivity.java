package com.example.smartautofiller.ui;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartautofiller.R;
import com.example.smartautofiller.database.AppDatabase;
import com.example.smartautofiller.model.ProfileSection;
import com.example.smartautofiller.model.SectionField;
import com.example.smartautofiller.model.UserProfile;

import java.util.List;

/**
 * Full-screen view of a saved profile with all sections and fields.
 * Long-press any field to copy it. Tap Edit to modify, Delete to remove.
 */
public class ProfileViewActivity extends AppCompatActivity {

    public static final String EXTRA_PROFILE_ID = "profile_id";
    private UserProfile profile;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        db = AppDatabase.getDatabase(this);
        int profileId = getIntent().getIntExtra(EXTRA_PROFILE_ID, -1);
        if (profileId == -1) { finish(); return; }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            profile = db.userProfileDao().getProfileById(profileId);
            new Handler(Looper.getMainLooper()).post(this::populateUI);
        });
    }

    private void populateUI() {
        if (profile == null) { finish(); return; }

        // Header
        TextView tvTag = findViewById(R.id.tv_profile_tag);
        tvTag.setText(profile.getProfileName());

        // Avatar
        TextView tvAvatar = findViewById(R.id.tv_avatar);
        String name = profile.getProfileName();
        tvAvatar.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());

        // Name + contact
        TextView tvFullName = findViewById(R.id.tv_full_name);
        tvFullName.setText(profile.getFullName().isEmpty() ? "No Name" : profile.getFullName());

        TextView tvEmailPhone = findViewById(R.id.tv_email_phone);
        String contact = "";
        if (!profile.getEmail().isEmpty()) contact += profile.getEmail();
        if (!profile.getPhoneNumber().isEmpty()) {
            if (!contact.isEmpty()) contact += " · ";
            contact += profile.getPhoneNumber();
        }
        tvEmailPhone.setText(contact.isEmpty() ? "No contact info" : contact);

        // Sections
        LinearLayout container = findViewById(R.id.container_sections);
        container.removeAllViews();

        List<ProfileSection> sections = profile.getSections();
        if (sections != null && !sections.isEmpty()) {
            for (ProfileSection section : sections) {
                addSectionView(container, section);
            }
        } else {
            // Fallback: show basic fields
            addBasicFields(container);
        }

        // Buttons
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btn_edit).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileCreateActivity.class);
            intent.putExtra(ProfileCreateActivity.EXTRA_EDIT_ID, profile.getId());
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btn_delete).setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Delete '" + profile.getProfileName() + "'? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        db.userProfileDao().deleteProfile(profile);
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show()
        );
    }

    private void addSectionView(LinearLayout container, ProfileSection section) {
        // Section header
        TextView header = new TextView(this);
        String icon = (section.getIcon() != null && !section.getIcon().isEmpty()) ? section.getIcon() : "📋";
        String name = (section.getName() != null && !section.getName().isEmpty()) ? section.getName() : "Section";
        header.setText(icon + "  " + name.toUpperCase());
        header.setTextSize(11f);
        header.setTextColor(0xFF6200EE);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setPadding(dp(20), dp(16), dp(20), dp(6));
        container.addView(header);

        // Card with fields
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFFFFFFFF);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(dp(12), 0, dp(12), 0);
        card.setLayoutParams(cardParams);
        card.setElevation(dp(2));

        List<SectionField> fields = section.getFields();
        if (fields != null) {
            for (int i = 0; i < fields.size(); i++) {
                SectionField field = fields.get(i);
                if (field.getValue() == null || field.getValue().isEmpty()) continue;
                addFieldRow(card, field.getLabel(), field.getValue());
                if (i < fields.size() - 1) addDivider(card);
            }
        }
        container.addView(card);
    }

    private void addBasicFields(LinearLayout container) {
        TextView header = new TextView(this);
        header.setText("👤  BASIC INFORMATION");
        header.setTextSize(11f);
        header.setTextColor(0xFF6200EE);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setPadding(dp(20), dp(16), dp(20), dp(6));
        container.addView(header);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFFFFFFFF);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(dp(12), 0, dp(12), 0);
        card.setLayoutParams(p);
        card.setElevation(dp(2));

        if (!profile.getFullName().isEmpty())    addFieldRow(card, "Full Name", profile.getFullName());
        if (!profile.getEmail().isEmpty())        { addDivider(card); addFieldRow(card, "Email", profile.getEmail()); }
        if (!profile.getPhoneNumber().isEmpty())  { addDivider(card); addFieldRow(card, "Phone", profile.getPhoneNumber()); }
        if (!profile.getAddress().isEmpty())      { addDivider(card); addFieldRow(card, "Address", profile.getAddress()); }
        container.addView(card);
    }

    private void addFieldRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        row.setClickable(true);
        row.setFocusable(true);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextSize(11f);
        tvLabel.setTextColor(0xFF9E9E9E);
        row.addView(tvLabel);

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setTextSize(15f);
        tvValue.setTextColor(0xFF1A1A2E);
        tvValue.setPadding(0, dp(2), 0, 0);
        row.addView(tvValue);

        // Long press → copy to clipboard
        row.setOnLongClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText(label, value));
            Toast.makeText(this, "'" + label + "' copied!", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Short tap → also copy
        row.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText(label, value));
            Toast.makeText(this, "Copied: " + value, Toast.LENGTH_SHORT).show();
        });

        parent.addView(row);
    }

    private void addDivider(LinearLayout parent) {
        View divider = new View(this);
        divider.setBackgroundColor(0xFFEEEEEE);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        divider.setLayoutParams(p);
        parent.addView(divider);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
