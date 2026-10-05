package com.example.smartautofiller.matcher;

import com.example.smartautofiller.model.ProfileSection;
import com.example.smartautofiller.model.SectionField;
import com.example.smartautofiller.model.UserProfile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Intelligent multi-tiered field matcher that maps detected UI labels, hints, and IDs
 * to corresponding profile values using canonical normalization, keywords, and Levenshtein fuzzy metrics.
 */
public final class FieldMatcher {

    private static final double FUZZY_THRESHOLD = 0.80;

    private static final List<String> NAME_KEYWORDS = Arrays.asList(
            "name", "fullname", "full name", "first name", "firstname", "candidate name",
            "applicant name", "student name", "your name", "naam", "candidate_name", "user_name"
    );

    private static final List<String> EMAIL_KEYWORDS = Arrays.asList(
            "email", "e-mail", "mail", "gmail", "email address", "email_id", "emailid"
    );

    private static final List<String> PHONE_KEYWORDS = Arrays.asList(
            "phone", "mobile", "mobile no", "mobile number", "contact", "contact no",
            "phone number", "cell", "cell phone", "telephone", "phone_number", "mobilenumber"
    );

    private static final List<String> ADDRESS_KEYWORDS = Arrays.asList(
            "address", "residential address", "permanent address", "current address",
            "street", "house no", "flat no", "residence", "pata", "full_address"
    );

    private FieldMatcher() {}

    /**
     * Resolves the best matching text value from the profile for a given UI label/hint.
     *
     * @param detectedLabel extracted UI label, hint, content description, or view ID
     * @param profile active user profile
     * @return matching string value or null if no confident match
     */
    public static String matchFieldValue(String detectedLabel, UserProfile profile) {
        if (detectedLabel == null || detectedLabel.trim().isEmpty() || profile == null) {
            return null;
        }

        String raw = detectedLabel.trim();
        String normalized = raw.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", " ").replaceAll("\\s+", " ").trim();

        // 1. Try Instant Regional Translation Dictionary
        String canonicalKey = TranslationDictionary.translateToCanonical(raw);
        if (canonicalKey != null) {
            String val = getByCanonicalKey(canonicalKey, profile);
            if (val != null && !val.trim().isEmpty()) return val;
        }

        // 2. Keyword & Fuzzy Match for Core Standard Fields
        if (matchesAny(normalized, NAME_KEYWORDS)) {
            if (isValid(profile.getFullName())) return profile.getFullName();
        }

        if (matchesAny(normalized, EMAIL_KEYWORDS)) {
            if (isValid(profile.getEmail())) return profile.getEmail();
        }

        if (matchesAny(normalized, PHONE_KEYWORDS)) {
            if (isValid(profile.getPhoneNumber())) return profile.getPhoneNumber();
        }

        if (matchesAny(normalized, ADDRESS_KEYWORDS)) {
            if (isValid(profile.getAddress())) return profile.getAddress();
        }

        // 3. Match against Profile Custom Fields (Key-Value map)
        if (profile.getCustomFields() != null) {
            for (Map.Entry<String, String> entry : profile.getCustomFields().entrySet()) {
                String keyNorm = entry.getKey().toLowerCase().replaceAll("[^a-zA-Z0-9 ]", " ").trim();
                if (normalized.equals(keyNorm) || 
                    normalized.contains(keyNorm) || 
                    LevenshteinDistance.computeSimilarity(normalized, keyNorm) >= FUZZY_THRESHOLD) {
                    if (isValid(entry.getValue())) return entry.getValue();
                }
            }
        }

        // 4. Match against Structured Profile Sections (e.g. Marksheet, Identity Card)
        if (profile.getSections() != null) {
            for (ProfileSection section : profile.getSections()) {
                for (SectionField field : section.getFields()) {
                    String fieldNorm = field.getLabel().toLowerCase().replaceAll("[^a-zA-Z0-9 ]", " ").trim();
                    if (normalized.equals(fieldNorm) || 
                        normalized.contains(fieldNorm) || 
                        LevenshteinDistance.computeSimilarity(normalized, fieldNorm) >= FUZZY_THRESHOLD) {
                        if (isValid(field.getValue())) return field.getValue();
                    }
                }
            }
        }

        return null;
    }

    private static boolean matchesAny(String input, List<String> keywords) {
        for (String kw : keywords) {
            if (input.equals(kw) || input.contains(kw)) {
                return true;
            }
            if (LevenshteinDistance.computeSimilarity(input, kw) >= FUZZY_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    private static String getByCanonicalKey(String key, UserProfile profile) {
        switch (key) {
            case "fullName": return profile.getFullName();
            case "email": return profile.getEmail();
            case "phoneNumber": return profile.getPhoneNumber();
            case "address": return profile.getAddress();
            default:
                // Check in custom fields
                if (profile.getCustomFields() != null && profile.getCustomFields().containsKey(key)) {
                    return profile.getCustomFields().get(key);
                }
                return null;
        }
    }

    private static boolean isValid(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
