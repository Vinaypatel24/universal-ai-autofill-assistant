package com.example.smartautofiller.ocr;

import com.example.smartautofiller.model.SectionField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates structured data extracted from camera OCR on an ID card or marksheet.
 */
public class ScannedData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String docType = "Document";
    private String name = "";
    private String fatherName = "";
    private String motherName = "";
    private String dob = "";
    private String panNumber = "";
    private String aadhaarNumber = "";
    private String dlNumber = "";
    private String address = "";
    private String phone = "";
    private String email = "";
    private String rawText = "";
    private List<SectionField> sectionFields = new ArrayList<>();

    public ScannedData() {}

    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }

    public String getDlNumber() { return dlNumber; }
    public void setDlNumber(String dlNumber) { this.dlNumber = dlNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public List<SectionField> getSectionFields() { return sectionFields; }
    public void setSectionFields(List<SectionField> sectionFields) { this.sectionFields = sectionFields; }

    public void addField(String label, String value) {
        if (value != null && !value.trim().isEmpty()) {
            this.sectionFields.add(new SectionField(label, value));
        }
    }
}
