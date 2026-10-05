package com.example.smartautofiller.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a structured section inside a user profile
 * (e.g., "Identity Card", "Academic Marksheet", "Employment Details").
 */
public class ProfileSection implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String icon;
    private List<SectionField> fields;

    public ProfileSection() {
        this.id = UUID.randomUUID().toString();
        this.name = "";
        this.icon = "📋";
        this.fields = new ArrayList<>();
    }

    public ProfileSection(String name, String icon) {
        this.id = UUID.randomUUID().toString();
        this.name = name != null ? name : "";
        this.icon = icon != null ? icon : "📋";
        this.fields = new ArrayList<>();
    }

    public ProfileSection(String id, String name, String icon, List<SectionField> fields) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name != null ? name : "";
        this.icon = icon != null ? icon : "📋";
        this.fields = fields != null ? fields : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<SectionField> getFields() {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        return fields;
    }

    public void setFields(List<SectionField> fields) {
        this.fields = fields != null ? fields : new ArrayList<>();
    }

    public void addField(SectionField field) {
        if (field != null) {
            getFields().add(field);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileSection that = (ProfileSection) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(icon, that.icon) &&
               Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, icon, fields);
    }

    @Override
    public String toString() {
        return "ProfileSection{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", fieldCount=" + (fields != null ? fields.size() : 0) +
                '}';
    }
}
