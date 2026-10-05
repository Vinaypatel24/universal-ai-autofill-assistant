package com.example.smartautofiller.matcher;

import com.example.smartautofiller.model.ProfileSection;
import com.example.smartautofiller.model.SectionField;
import com.example.smartautofiller.model.UserProfile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles instant text expansion for typed shortcuts (e.g. "name-", "email-", "mob-").
 */
public final class ShortcutExpander {

    public static final Map<String, String> STANDARD_SHORTCUTS;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("name-", "fullName");
        map.put("naam-", "fullName");
        map.put("fullname-", "fullName");
        map.put("email-", "email");
        map.put("mail-", "email");
        map.put("gmail-", "email");
        map.put("mob-", "phoneNumber");
        map.put("phone-", "phoneNumber");
        map.put("mobile-", "phoneNumber");
        map.put("mobileno-", "phoneNumber");
        map.put("addr-", "address");
        map.put("address-", "address");
        map.put("pata-", "address");
        STANDARD_SHORTCUTS = Collections.unmodifiableMap(map);
    }

    private ShortcutExpander() {}

    /**
     * Scans multi-line or single-line text and replaces any matching shortcuts with profile values.
     *
     * @param currentText text currently inside the focused input field
     * @param profile active user profile
     * @return Result containing whether any shortcut was filled, and the modified text
     */
    public static ExpandResult expandShortcuts(String currentText, UserProfile profile) {
        if (currentText == null || currentText.isEmpty() || profile == null) {
            return new ExpandResult(false, currentText);
        }

        String[] lines = currentText.split("\n", -1);
        boolean anyFilled = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String lineLower = line.trim().toLowerCase();

            // 1. Check standard shortcuts
            String standardKey = STANDARD_SHORTCUTS.get(lineLower);
            if (standardKey != null) {
                String val = resolveStandardValue(standardKey, profile);
                if (val != null && !val.trim().isEmpty()) {
                    lines[i] = line + " " + val.trim();
                    anyFilled = true;
                    continue;
                }
            }

            // 2. Check custom fields & sections if ending with "-"
            if (lineLower.endsWith("-")) {
                String prefix = lineLower.substring(0, lineLower.length() - 1).trim();

                // Custom fields check
                if (profile.getCustomFields() != null) {
                    for (Map.Entry<String, String> entry : profile.getCustomFields().entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(prefix) || 
                            entry.getKey().toLowerCase().replace(" ", "").equals(prefix)) {
                            lines[i] = line + " " + entry.getValue().trim();
                            anyFilled = true;
                            break;
                        }
                    }
                }

                // Custom sections check
                if (!anyFilled && profile.getSections() != null) {
                    for (ProfileSection section : profile.getSections()) {
                        for (SectionField field : section.getFields()) {
                            if (field.getLabel().equalsIgnoreCase(prefix) || 
                                field.getLabel().toLowerCase().replace(" ", "").equals(prefix)) {
                                lines[i] = line + " " + field.getValue().trim();
                                anyFilled = true;
                                break;
                            }
                        }
                        if (anyFilled) break;
                    }
                }
            }
        }

        if (anyFilled) {
            return new ExpandResult(true, String.join("\n", lines));
        }
        return new ExpandResult(false, currentText);
    }

    private static String resolveStandardValue(String fieldKey, UserProfile profile) {
        switch (fieldKey) {
            case "fullName": return profile.getFullName();
            case "email": return profile.getEmail();
            case "phoneNumber": return profile.getPhoneNumber();
            case "address": return profile.getAddress();
            default: return null;
        }
    }

    public static class ExpandResult {
        private final boolean filled;
        private final String updatedText;

        public ExpandResult(boolean filled, String updatedText) {
            this.filled = filled;
            this.updatedText = updatedText;
        }

        public boolean isFilled() {
            return filled;
        }

        public String getUpdatedText() {
            return updatedText;
        }
    }
}
