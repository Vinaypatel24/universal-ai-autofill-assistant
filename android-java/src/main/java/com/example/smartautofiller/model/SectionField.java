package com.example.smartautofiller.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single key-value field inside a structured section
 * (e.g., "Roll Number" -> "CS2026", "Marks" -> "95").
 */
public class SectionField implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String label;
    private String value;

    public SectionField() {
        this.id = UUID.randomUUID().toString();
        this.label = "";
        this.value = "";
    }

    public SectionField(String label, String value) {
        this.id = UUID.randomUUID().toString();
        this.label = label != null ? label : "";
        this.value = value != null ? value : "";
    }

    public SectionField(String id, String label, String value) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.label = label != null ? label : "";
        this.value = value != null ? value : "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SectionField that = (SectionField) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(label, that.label) &&
               Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, value);
    }

    @Override
    public String toString() {
        return "SectionField{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
