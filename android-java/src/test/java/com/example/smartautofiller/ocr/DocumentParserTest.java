package com.example.smartautofiller.ocr;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DocumentParserTest {

    @Test
    void testPanCardParsing() {
        String ocrText = "INCOME TAX DEPARTMENT\nGOVT. OF INDIA\nPermanent Account Number\nABCDE1234F\nName: RAHUL SHARMA\nFather's Name: SURESH SHARMA\nDOB: 15/08/1998";
        ScannedData data = DocumentParser.parse(ocrText);

        assertThat(data.getDocType()).isEqualTo("PAN Card");
        assertThat(data.getPanNumber()).isEqualTo("ABCDE1234F");
        assertThat(data.getName()).contains("RAHUL SHARMA");
        assertThat(data.getFatherName()).contains("SURESH SHARMA");
        assertThat(data.getDob()).isEqualTo("15/08/1998");
    }

    @Test
    void testAadhaarCardParsing() {
        String ocrText = "Government of India\nUnique Identification Authority of India\nTo\nJane Doe\nDOB: 01/01/1995\nGender: Female\n1234 5678 9012\nमेरा आधार, मेरी पहचान";
        ScannedData data = DocumentParser.parse(ocrText);

        assertThat(data.getDocType()).isEqualTo("Aadhaar Card");
        assertThat(data.getAadhaarNumber()).isEqualTo("123456789012");
        assertThat(data.getDob()).isEqualTo("01/01/1995");
    }

    @Test
    void testMarksheetParsing() {
        String ocrText = "CENTRAL BOARD OF SECONDARY EDUCATION\nHIGHER SECONDARY EXAMINATION 2024\nROLL NO: 1234567\nName: ARUN KUMAR\nGRAND TOTAL: 480\nRESULT: PASS";
        ScannedData data = DocumentParser.parse(ocrText);

        assertThat(data.getDocType()).contains("Marksheet");
        assertThat(data.getSectionFields()).anyMatch(f -> f.getLabel().equals("Roll Number") && f.getValue().equals("1234567"));
        assertThat(data.getSectionFields()).anyMatch(f -> f.getLabel().equals("Year of Passing") && f.getValue().equals("2024"));
        assertThat(data.getSectionFields()).anyMatch(f -> f.getLabel().equals("Grand Total") && f.getValue().equals("480"));
    }
}
