package com.example.smartautofiller.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartautofiller.R;
import com.example.smartautofiller.database.AppDatabase;
import com.example.smartautofiller.model.ProfileSection;
import com.example.smartautofiller.model.SectionField;
import com.example.smartautofiller.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-screen profile creation and editing activity with comprehensive sections:
 * Personal, ID Numbers, Contact, Address, Education, Professional.
 */
public class ProfileCreateActivity extends AppCompatActivity {

    public static final String EXTRA_EDIT_ID = "edit_profile_id";

    // Personal
    private EditText etProfileName, etFullName, etFatherName, etMotherName, etDob, etGender;
    // ID
    private EditText etAadhaar, etPan, etPassport;
    // Contact
    private EditText etEmail, etPhone, etWhatsapp, etAltPhone;
    // Address
    private EditText etHouseNo, etStreet, etCity, etPincode, etState, etCountry;
    // Education
    private EditText etSchool, etClass10, etClass12, etCollege, etDegree, etBranch, etCgpa, etPassYear;
    // Professional
    private EditText etCompany, etDesignation, etLinkedin, etGithub;

    private int editingProfileId = -1;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_create);

        bindViews();
        setupCameraLauncher();
        setupButtons();

        editingProfileId = getIntent().getIntExtra(EXTRA_EDIT_ID, -1);
        if (editingProfileId != -1) {
            loadProfileForEditing(editingProfileId);
        } else {
            handleScanPrefill(getIntent());
        }
    }

    private void bindViews() {
        etProfileName   = findViewById(R.id.et_profile_name);
        etFullName      = findViewById(R.id.et_full_name);
        etFatherName    = findViewById(R.id.et_father_name);
        etMotherName    = findViewById(R.id.et_mother_name);
        etDob           = findViewById(R.id.et_dob);
        etGender        = findViewById(R.id.et_gender);

        etAadhaar       = findViewById(R.id.et_aadhaar);
        etPan           = findViewById(R.id.et_pan);
        etPassport      = findViewById(R.id.et_passport);

        etEmail         = findViewById(R.id.et_email);
        etPhone         = findViewById(R.id.et_phone);
        etWhatsapp      = findViewById(R.id.et_whatsapp);
        etAltPhone      = findViewById(R.id.et_alt_phone);

        etHouseNo       = findViewById(R.id.et_house_no);
        etStreet        = findViewById(R.id.et_street);
        etCity          = findViewById(R.id.et_city);
        etPincode       = findViewById(R.id.et_pincode);
        etState         = findViewById(R.id.et_state);
        etCountry       = findViewById(R.id.et_country);

        etSchool        = findViewById(R.id.et_school);
        etClass10       = findViewById(R.id.et_class10_percent);
        etClass12       = findViewById(R.id.et_class12_percent);
        etCollege       = findViewById(R.id.et_college);
        etDegree        = findViewById(R.id.et_degree);
        etBranch        = findViewById(R.id.et_branch);
        etCgpa          = findViewById(R.id.et_cgpa);
        etPassYear      = findViewById(R.id.et_pass_year);

        etCompany       = findViewById(R.id.et_company);
        etDesignation   = findViewById(R.id.et_designation);
        etLinkedin      = findViewById(R.id.et_linkedin);
        etGithub        = findViewById(R.id.et_github);
    }

    private void setupButtons() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        Button btnScan = findViewById(R.id.btn_scan_top);
        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(this, CameraActivity.class);
            cameraLauncher.launch(intent);
        });

        Button btnSave = findViewById(R.id.btn_save_profile);
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleScanPrefill(result.getData());
                        Toast.makeText(this, "Fields auto-filled from scan!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void handleScanPrefill(Intent data) {
        if (data == null) return;
        setIfNotEmpty(etFullName,   data.getStringExtra("detected_name"));
        setIfNotEmpty(etEmail,      data.getStringExtra("detected_email"));
        setIfNotEmpty(etPhone,      data.getStringExtra("detected_phone"));
        setIfNotEmpty(etAadhaar,    data.getStringExtra("detected_aadhaar"));
        setIfNotEmpty(etPan,        data.getStringExtra("detected_pan"));
        setIfNotEmpty(etDob,        data.getStringExtra("detected_dob"));
        setIfNotEmpty(etCity,       data.getStringExtra("detected_city"));
        setIfNotEmpty(etState,      data.getStringExtra("detected_state"));
        setIfNotEmpty(etPincode,    data.getStringExtra("detected_pincode"));

        String docType = data.getStringExtra("doc_type");
        if (docType != null && !docType.isEmpty() && etProfileName.getText().toString().isEmpty()) {
            etProfileName.setText(docType);
        }
    }

    private void loadProfileForEditing(int profileId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserProfile p = AppDatabase.getDatabase(this).userProfileDao().getProfileById(profileId);
            if (p == null) return;
            new Handler(Looper.getMainLooper()).post(() -> {
                TextView tvTitle = findViewById(R.id.tv_form_title);
                if (tvTitle != null) tvTitle.setText("Edit Profile");
                Button btnSave = findViewById(R.id.btn_save_profile);
                if (btnSave != null) btnSave.setText("Update Profile");

                etProfileName.setText(p.getProfileName());
                etFullName.setText(p.getFullName());
                etEmail.setText(p.getEmail());
                etPhone.setText(p.getPhoneNumber());

                if (p.getSections() != null) {
                    for (ProfileSection sec : p.getSections()) {
                        if (sec.getFields() == null) continue;
                        for (SectionField f : sec.getFields()) {
                            setFieldValue(f.getLabel(), f.getValue());
                        }
                    }
                }
            });
        });
    }

    private void setFieldValue(String label, String value) {
        if (label == null || value == null || value.isEmpty()) return;
        switch (label) {
            case "Full Name":        setIfNotEmpty(etFullName, value); break;
            case "Father's Name":    setIfNotEmpty(etFatherName, value); break;
            case "Mother's Name":    setIfNotEmpty(etMotherName, value); break;
            case "Date of Birth":    setIfNotEmpty(etDob, value); break;
            case "Gender":           setIfNotEmpty(etGender, value); break;
            case "Aadhaar Number":   setIfNotEmpty(etAadhaar, value); break;
            case "PAN Number":       setIfNotEmpty(etPan, value); break;
            case "Passport No.":     setIfNotEmpty(etPassport, value); break;
            case "Email":            setIfNotEmpty(etEmail, value); break;
            case "Mobile":           setIfNotEmpty(etPhone, value); break;
            case "WhatsApp":         setIfNotEmpty(etWhatsapp, value); break;
            case "Alternate Phone":  setIfNotEmpty(etAltPhone, value); break;
            case "House No.":        setIfNotEmpty(etHouseNo, value); break;
            case "Street":           setIfNotEmpty(etStreet, value); break;
            case "City":             setIfNotEmpty(etCity, value); break;
            case "Pincode":          setIfNotEmpty(etPincode, value); break;
            case "State":            setIfNotEmpty(etState, value); break;
            case "Country":          setIfNotEmpty(etCountry, value); break;
            case "School":           setIfNotEmpty(etSchool, value); break;
            case "Class 10 %":       setIfNotEmpty(etClass10, value); break;
            case "Class 12 %":       setIfNotEmpty(etClass12, value); break;
            case "College":          setIfNotEmpty(etCollege, value); break;
            case "Degree":           setIfNotEmpty(etDegree, value); break;
            case "Branch":           setIfNotEmpty(etBranch, value); break;
            case "CGPA":             setIfNotEmpty(etCgpa, value); break;
            case "Passing Year":     setIfNotEmpty(etPassYear, value); break;
            case "Company":          setIfNotEmpty(etCompany, value); break;
            case "Designation":      setIfNotEmpty(etDesignation, value); break;
            case "LinkedIn":         setIfNotEmpty(etLinkedin, value); break;
            case "GitHub":           setIfNotEmpty(etGithub, value); break;
        }
    }

    private void setIfNotEmpty(EditText field, String value) {
        if (value != null && !value.isEmpty()) {
            field.setText(value);
        }
    }

    private void saveProfile() {
        String profileName = etProfileName.getText().toString().trim();
        String fullName    = etFullName.getText().toString().trim();

        if (profileName.isEmpty()) {
            etProfileName.setError("Profile tag is required");
            etProfileName.requestFocus();
            return;
        }
        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        // Build base profile
        UserProfile profile = new UserProfile(
                profileName,
                fullName,
                etEmail.getText().toString().trim(),
                etPhone.getText().toString().trim(),
                buildFullAddress()
        );

        if (editingProfileId != -1) {
            profile.setId(editingProfileId);
        }

        // Personal section
        List<SectionField> personalFields = new ArrayList<>();
        addField(personalFields, "Full Name",     etFullName);
        addField(personalFields, "Father's Name", etFatherName);
        addField(personalFields, "Mother's Name", etMotherName);
        addField(personalFields, "Date of Birth", etDob);
        addField(personalFields, "Gender",        etGender);
        if (!personalFields.isEmpty()) {
            ProfileSection ps = new ProfileSection("Personal Info", "👤");
            ps.setFields(personalFields);
            profile.getSections().add(ps);
        }

        // ID section
        List<SectionField> idFields = new ArrayList<>();
        addField(idFields, "Aadhaar Number", etAadhaar);
        addField(idFields, "PAN Number",     etPan);
        addField(idFields, "Passport No.",   etPassport);
        if (!idFields.isEmpty()) {
            ProfileSection ids = new ProfileSection("ID Numbers", "🪪");
            ids.setFields(idFields);
            profile.getSections().add(ids);
        }

        // Contact section
        List<SectionField> contactFields = new ArrayList<>();
        addField(contactFields, "Email",           etEmail);
        addField(contactFields, "Mobile",          etPhone);
        addField(contactFields, "WhatsApp",        etWhatsapp);
        addField(contactFields, "Alternate Phone", etAltPhone);
        if (!contactFields.isEmpty()) {
            ProfileSection cs = new ProfileSection("Contact", "📞");
            cs.setFields(contactFields);
            profile.getSections().add(cs);
        }

        // Address section
        List<SectionField> addressFields = new ArrayList<>();
        addField(addressFields, "House No.",   etHouseNo);
        addField(addressFields, "Street",      etStreet);
        addField(addressFields, "City",        etCity);
        addField(addressFields, "Pincode",     etPincode);
        addField(addressFields, "State",       etState);
        addField(addressFields, "Country",     etCountry);
        if (!addressFields.isEmpty()) {
            ProfileSection as = new ProfileSection("Address", "🏠");
            as.setFields(addressFields);
            profile.getSections().add(as);
        }

        // Education section
        List<SectionField> eduFields = new ArrayList<>();
        addField(eduFields, "School",        etSchool);
        addField(eduFields, "Class 10 %",    etClass10);
        addField(eduFields, "Class 12 %",    etClass12);
        addField(eduFields, "College",       etCollege);
        addField(eduFields, "Degree",        etDegree);
        addField(eduFields, "Branch",        etBranch);
        addField(eduFields, "CGPA",          etCgpa);
        addField(eduFields, "Passing Year",  etPassYear);
        if (!eduFields.isEmpty()) {
            ProfileSection es = new ProfileSection("Education", "🎓");
            es.setFields(eduFields);
            profile.getSections().add(es);
        }

        // Professional section
        List<SectionField> proFields = new ArrayList<>();
        addField(proFields, "Company",     etCompany);
        addField(proFields, "Designation", etDesignation);
        addField(proFields, "LinkedIn",    etLinkedin);
        addField(proFields, "GitHub",      etGithub);
        if (!proFields.isEmpty()) {
            ProfileSection prs = new ProfileSection("Professional", "💼");
            prs.setFields(proFields);
            profile.getSections().add(prs);
        }

        // Save to Room DB
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (editingProfileId != -1) {
                AppDatabase.getDatabase(this).userProfileDao().updateProfile(profile);
            } else {
                AppDatabase.getDatabase(this).userProfileDao().insertProfile(profile);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                String msg = editingProfileId != -1 ? "✅ Profile updated!" : "✅ Profile '" + profileName + "' saved!";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }

    private String buildFullAddress() {
        StringBuilder sb = new StringBuilder();
        append(sb, etHouseNo.getText().toString().trim());
        append(sb, etStreet.getText().toString().trim());
        append(sb, etCity.getText().toString().trim());
        append(sb, etState.getText().toString().trim());
        append(sb, etPincode.getText().toString().trim());
        append(sb, etCountry.getText().toString().trim());
        return sb.toString();
    }

    private void append(StringBuilder sb, String val) {
        if (!val.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(val);
        }
    }

    private void addField(List<SectionField> list, String label, EditText field) {
        String val = field.getText().toString().trim();
        if (!val.isEmpty()) {
            list.add(new SectionField(label, val));
        }
    }
}
