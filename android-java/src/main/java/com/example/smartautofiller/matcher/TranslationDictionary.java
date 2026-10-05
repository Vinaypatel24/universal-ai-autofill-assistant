package com.example.smartautofiller.matcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * High-speed offline static dictionary mapping regional language form labels
 * (Hindi, Tamil, Telugu, Marathi, Bengali, Gujarati, Kannada) to standard English field keys.
 * Operates in < 1ms with 0 network latency.
 */
public final class TranslationDictionary {

    private static final Map<String, String> DICTIONARY;

    static {
        Map<String, String> map = new HashMap<>();

        // ── Full Name / Candidate Name ─────────────────────────
        map.put("नाम", "fullName");
        map.put("पूरा नाम", "fullName");
        map.put("उम्मीदवार का नाम", "fullName");
        map.put("विद्यार्थी का नाम", "fullName");
        map.put("आवेदक का नाम", "fullName");
        map.put("নাম", "fullName");
        map.put("பெயர்", "fullName");
        map.put("முழு பெயர்", "fullName");
        map.put("పేరు", "fullName");
        map.put("పూర్తి పేరు", "fullName");
        map.put("नाव", "fullName");
        map.put("संपूर्ण नाव", "fullName");
        map.put("નામ", "fullName");
        map.put("ಹೆಸರು", "fullName");

        // ── Father's Name ──────────────────────────────────────
        map.put("पिता का नाम", "fatherName");
        map.put("पिताजी का नाम", "fatherName");
        map.put("বাবার নাম", "fatherName");
        map.put("தந்தை பெயர்", "fatherName");
        map.put("తండ్రి పేరు", "fatherName");
        map.put("वडिलांचे नाव", "fatherName");

        // ── Mother's Name ──────────────────────────────────────
        map.put("माता का नाम", "motherName");
        map.put("माँ का नाम", "motherName");
        map.put("মায়ের নাম", "motherName");
        map.put("தாய் பெயர்", "motherName");
        map.put("తల్లి పేరు", "motherName");
        map.put("आईचे नाव", "motherName");

        // ── Email Address ──────────────────────────────────────
        map.put("ईमेल", "email");
        map.put("ई-मेल", "email");
        map.put("इमेल", "email");
        map.put("মেইল", "email");
        map.put("மின்னஞ்சல்", "email");
        map.put("ఈమెయిల్", "email");

        // ── Mobile / Phone Number ──────────────────────────────
        map.put("मोबाइल", "phoneNumber");
        map.put("मोबाइल नंबर", "phoneNumber");
        map.put("फोन नंबर", "phoneNumber");
        map.put("दूरभाष", "phoneNumber");
        map.put("মোবাইল", "phoneNumber");
        map.put("கைபேசி எண்", "phoneNumber");
        map.put("மொபைல் எண்", "phoneNumber");
        map.put("ఫోన్ నంబర్", "phoneNumber");
        map.put("मोबाईल क्रमांक", "phoneNumber");
        map.put("મોબાઇલ નંબર", "phoneNumber");
        map.put("ಮೊಬೈಲ್ ಸಂಖ್ಯೆ", "phoneNumber");

        // ── Address / State / Country ──────────────────────────
        map.put("पता", "address");
        map.put("स्थायी पता", "address");
        map.put("वर्तमान पता", "address");
        map.put("ঠিকানা", "address");
        map.put("முகவரி", "address");
        map.put("చిరునామా", "address");
        map.put("पत्ता", "address");
        map.put("સરનામું", "address");
        map.put("ವಿಳಾಸ", "address");

        map.put("राज्य", "state");
        map.put("राज्य / प्रांत", "state");
        map.put("மாநிலம்", "state");
        map.put("రాష్ట్రం", "state");

        map.put("देश", "country");
        map.put("நாடு", "country");
        map.put("దేశం", "country");

        map.put("पिन कोड", "pinCode");
        map.put("पिनकोड", "pinCode");
        map.put("அஞ்சல் குறியீடு", "pinCode");

        // ── Date of Birth / Gender ─────────────────────────────
        map.put("जन्म तिथि", "dob");
        map.put("जन्म तारीख", "dob");
        map.put("জন্ম তারিখ", "dob");
        map.put("பிறந்த தேதி", "dob");
        map.put("పుట్టిన తేదీ", "dob");
        map.put("जन्मतारीख", "dob");

        map.put("लिंग", "gender");
        map.put("பாலினம்", "gender");
        map.put("లింగం", "gender");

        // ── Identity / Documents ───────────────────────────────
        map.put("आधार नंबर", "aadhaarNumber");
        map.put("आधार कार्ड", "aadhaarNumber");
        map.put("पैन कार्ड", "panNumber");
        map.put("पैन नंबर", "panNumber");
        map.put("रोल नंबर", "rollNumber");
        map.put("अनुक्रमांक", "rollNumber");

        DICTIONARY = Collections.unmodifiableMap(map);
    }

    private TranslationDictionary() {}

    /**
     * Resolves a regional or English label to a standardized canonical key.
     *
     * @param rawText raw label detected from UI
     * @return canonical key (e.g. "fullName", "email", "phoneNumber") or null if not found
     */
    public static String translateToCanonical(String rawText) {
        if (rawText == null) return null;
        String clean = rawText.trim().toLowerCase();
        return DICTIONARY.get(clean);
    }
}
