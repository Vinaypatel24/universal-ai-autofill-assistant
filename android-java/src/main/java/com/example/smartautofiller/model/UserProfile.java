package com.example.smartautofiller.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Main Room Entity representing a stored user profile.
 * Table name: "user_profiles" (compatible with Room database version 5).
 */
@Entity(tableName = "user_profiles")
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "profile_name")
    private String profileName;

    @ColumnInfo(name = "full_name")
    private String fullName;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "phone_number")
    private String phoneNumber;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "custom_fields")
    private Map<String, String> customFields;

    @ColumnInfo(name = "sections")
    private List<ProfileSection> sections;

    public UserProfile() {
        this.id = 0;
        this.profileName = "";
        this.fullName = "";
        this.email = "";
        this.phoneNumber = "";
        this.address = "";
        this.customFields = new HashMap<>();
        this.sections = new ArrayList<>();
    }

    public UserProfile(String profileName, String fullName, String email, String phoneNumber, String address) {
        this.id = 0;
        this.profileName = profileName != null ? profileName : "";
        this.fullName = fullName != null ? fullName : "";
        this.email = email != null ? email : "";
        this.phoneNumber = phoneNumber != null ? phoneNumber : "";
        this.address = address != null ? address : "";
        this.customFields = new HashMap<>();
        this.sections = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName != null ? profileName : "";
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName != null ? fullName : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email : "";
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber != null ? phoneNumber : "";
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address != null ? address : "";
    }

    public Map<String, String> getCustomFields() {
        if (customFields == null) {
            customFields = new HashMap<>();
        }
        return customFields;
    }

    public void setCustomFields(Map<String, String> customFields) {
        this.customFields = customFields != null ? customFields : new HashMap<>();
    }

    public List<ProfileSection> getSections() {
        if (sections == null) {
            sections = new ArrayList<>();
        }
        return sections;
    }

    public void setSections(List<ProfileSection> sections) {
        this.sections = sections != null ? sections : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return id == that.id &&
               Objects.equals(profileName, that.profileName) &&
               Objects.equals(fullName, that.fullName) &&
               Objects.equals(email, that.email) &&
               Objects.equals(phoneNumber, that.phoneNumber) &&
               Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, profileName, fullName, email, phoneNumber, address);
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "id=" + id +
                ", profileName='" + profileName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", sections=" + (sections != null ? sections.size() : 0) +
                '}';
    }
}
