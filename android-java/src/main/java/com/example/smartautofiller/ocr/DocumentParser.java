package com.example.smartautofiller.ocr;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Intelligent Document & OCR Parser in pure Java.
 * Extracts document types (PAN, Aadhaar, Driving License, Passport, Marksheets)
 * and structured key-value fields from raw OCR text streams.
 */
public final class DocumentParser {

    private static final Pattern PAN_REGEX = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]");
    private static final Pattern AADHAAR_REGEX = Pattern.compile("\\d{4}[\\s]?\\d{4}[\\s]?\\d{4}");
    private static final Pattern DOB_REGEX = Pattern.compile("[0-3]\\d[/\\-.][0-1]\\d[/\\-.][12]\\d{3}");
    private static final Pattern EMAIL_REGEX = Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_REGEX = Pattern.compile("(?:\\+91[\\s]?)?[6-9]\\d{9}");
    private static final Pattern YEAR_REGEX = Pattern.compile("20[12][0-9]");
    private static final Pattern ROLL_REGEX = Pattern.compile("(?:ROLL[\\s\\-]?(?:NO|NUMBER|NUMB)[.:\\s]*|ROLL[:\\s]+)([A-Z0-9]{6,12})", Pattern.CASE_INSENSITIVE);
    private static final Pattern ENROL_REGEX = Pattern.compile("(?:ENROL[A-Z]*[\\s\\-]?(?:NO|NUMBER)[.:\\s]*|ENROL[:\\s]+)([A-Z0-9/]{6,15})", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOTAL_REGEX = Pattern.compile("(?:GRAND[\\s]?TOTAL|महायोग)[\\s:]*([0-9]{2,4})", Pattern.CASE_INSENSITIVE);

    private static final Set<String> SKIP_WORDS = new HashSet<>(Arrays.asList(
            "income tax", "govt", "government", "permanent account", "uidai",
            "driving", "motor vehicle", "passport", "election", "india", "भारत", "board",
            "secondary", "education", "marksheet", "certificate", "माध्यमिक", "शिक्षा", "मण्डल",
            "cbse", "icse", "mpbse", "high school", "higher secondary", "bhopal", "subject",
            "theory", "practical", "maximum", "minimum", "obtained", "remarks", "date", "roll",
            "enrol", "centre", "school", "regular", "private", "grand total", "result", "pass",
            "division", "shri", "sushri", "certified", "appeared", "examination", "year"
    ));

    private DocumentParser() {}

    /**
     * Parses raw OCR string and extracts structured data.
     *
     * @param rawText complete text string obtained from ML Kit TextRecognizer
     * @return ScannedData object with detected fields
     */
    public static ScannedData parse(String rawText) {
        ScannedData data = new ScannedData();
        if (rawText == null || rawText.trim().isEmpty()) {
            return data;
        }

        data.setRawText(rawText);
        String upperText = rawText.toUpperCase();
        String[] lines = rawText.split("\n");

        // 1. Detect Document Type
        String docType = detectDocumentType(upperText);
        data.setDocType(docType);

        // 2. Regex extractions
        Matcher panMatcher = PAN_REGEX.matcher(upperText.replaceAll("\\s+", ""));
        if (panMatcher.find()) {
            data.setPanNumber(panMatcher.group());
            data.addField("PAN Number", data.getPanNumber());
        }

        Matcher aadhaarMatcher = AADHAAR_REGEX.matcher(rawText);
        if (aadhaarMatcher.find()) {
            String cleanAadhaar = aadhaarMatcher.group().replaceAll("\\s+", "");
            if (cleanAadhaar.length() == 12) {
                data.setAadhaarNumber(cleanAadhaar);
                data.addField("Aadhaar Number", cleanAadhaar);
            }
        }

        Matcher dobMatcher = DOB_REGEX.matcher(rawText);
        if (dobMatcher.find()) {
            data.setDob(dobMatcher.group());
            data.addField("Date of Birth", data.getDob());
        }

        Matcher emailMatcher = EMAIL_REGEX.matcher(rawText);
        if (emailMatcher.find()) {
            data.setEmail(emailMatcher.group());
        }

        Matcher phoneMatcher = PHONE_REGEX.matcher(rawText);
        if (phoneMatcher.find()) {
            data.setPhone(phoneMatcher.group());
        }

        // 3. Marksheet specific fields
        if (docType.contains("Marksheet")) {
            Matcher yearMatcher = YEAR_REGEX.matcher(rawText);
            if (yearMatcher.find()) {
                data.addField("Year of Passing", yearMatcher.group());
            }

            Matcher rollMatcher = ROLL_REGEX.matcher(rawText);
            if (rollMatcher.find()) {
                data.addField("Roll Number", rollMatcher.group(1));
            }

            Matcher enrolMatcher = ENROL_REGEX.matcher(rawText);
            if (enrolMatcher.find()) {
                data.addField("Enrollment Number", enrolMatcher.group(1));
            }

            Matcher totalMatcher = TOTAL_REGEX.matcher(rawText);
            if (totalMatcher.find()) {
                data.addField("Grand Total", totalMatcher.group(1));
            }
        }

        // 4. Candidate & Parents Name Detection
        detectNames(lines, data);

        return data;
    }

    private static String detectDocumentType(String upper) {
        if (upper.contains("INCOME TAX") || upper.contains("PERMANENT ACCOUNT")) {
            return "PAN Card";
        } else if (upper.contains("AADHAAR") || upper.contains("AADHAR") || upper.contains("UIDAI") || upper.contains("आधार")) {
            return "Aadhaar Card";
        } else if (upper.contains("DRIVING LICENCE") || upper.contains("DRIVING LICENSE")) {
            return "Driving Licence";
        } else if (upper.contains("PASSPORT")) {
            return "Passport";
        } else if (upper.contains("VOTER") || upper.contains("ELECTION COMMISSION")) {
            return "Voter ID";
        } else if (upper.contains("MARKSHEET") || upper.contains("BOARD OF SECONDARY") ||
                   upper.contains("SECONDARY EDUCATION") || upper.contains("माध्यमिक") ||
                   upper.contains("CBSE") || upper.contains("ICSE") || upper.contains("MPBSE")) {
            if (upper.contains("10+2") || upper.contains("HIGHER SECONDARY") || upper.contains("12TH")) {
                return "12th Marksheet";
            }
            return "10th Marksheet";
        }
        return "Document";
    }

    private static void detectNames(String[] lines, ScannedData data) {
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            String lo = line.toLowerCase();
            String next = (i + 1 < lines.length) ? lines[i + 1].trim() : "";

            if (data.getName().isEmpty() && (lo.startsWith("name") || lo.contains("name:"))) {
                String candidate = line.contains(":") ? line.substring(line.indexOf(":") + 1).trim() : next;
                if (looksLikeName(candidate)) {
                    data.setName(cleanName(candidate));
                }
            }

            if (data.getFatherName().isEmpty() && (lo.contains("father") || lo.contains("s/o"))) {
                String candidate = line.contains(":") ? line.substring(line.indexOf(":") + 1).trim() : next;
                if (looksLikeName(candidate)) {
                    data.setFatherName(cleanName(candidate));
                    data.addField("Father's Name", data.getFatherName());
                }
            }
        }
    }

    private static boolean looksLikeName(String t) {
        if (t == null) return false;
        String lo = t.toLowerCase().trim();
        if (lo.length() < 3 || lo.length() > 50) return false;
        for (String sw : SKIP_WORDS) {
            if (lo.contains(sw)) return false;
        }
        return t.matches("^[a-zA-Z\\s\\u0900-\\u097F]+$");
    }

    private static String cleanName(String s) {
        return s.replaceAll("[^a-zA-Z\\s\\u0900-\\u097F]", "").trim();
    }
}
