package com.example.smartautofiller.matcher;

import com.example.smartautofiller.model.ProfileSection;
import com.example.smartautofiller.model.SectionField;
import com.example.smartautofiller.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FieldMatcherTest {

    private UserProfile profile;

    @BeforeEach
    void setUp() {
        profile = new UserProfile("Personal", "Pushpraj Singhal", "pushpraj@example.com", "9876543210", "123 MG Road, Bengaluru");
        profile.getCustomFields().put("fatherName", "Rajesh Singhal");

        ProfileSection idSection = new ProfileSection("Identity Cards", "🆔");
        idSection.addField(new SectionField("Aadhaar Number", "1234 5678 9012"));
        idSection.addField(new SectionField("PAN Number", "ABCDE1234F"));
        profile.getSections().add(idSection);
    }

    @Test
    void testStandardFieldMatching() {
        assertThat(FieldMatcher.matchFieldValue("Enter Candidate Name", profile)).isEqualTo("Pushpraj Singhal");
        assertThat(FieldMatcher.matchFieldValue("email_address_input", profile)).isEqualTo("pushpraj@example.com");
        assertThat(FieldMatcher.matchFieldValue("Mobile Number", profile)).isEqualTo("9876543210");
        assertThat(FieldMatcher.matchFieldValue("Permanent Residential Address", profile)).isEqualTo("123 MG Road, Bengaluru");
    }

    @Test
    void testRegionalLanguageDictionaryMatching() {
        // Hindi labels matching
        assertThat(FieldMatcher.matchFieldValue("नाम", profile)).isEqualTo("Pushpraj Singhal");
        assertThat(FieldMatcher.matchFieldValue("पिता का नाम", profile)).isEqualTo("Rajesh Singhal");
        assertThat(FieldMatcher.matchFieldValue("मोबाइल नंबर", profile)).isEqualTo("9876543210");
        assertThat(FieldMatcher.matchFieldValue("पता", profile)).isEqualTo("123 MG Road, Bengaluru");

        // Tamil labels matching
        assertThat(FieldMatcher.matchFieldValue("பெயர்", profile)).isEqualTo("Pushpraj Singhal");
        assertThat(FieldMatcher.matchFieldValue("கைபேசி எண்", profile)).isEqualTo("9876543210");
    }

    @Test
    void testStructuredSectionMatching() {
        assertThat(FieldMatcher.matchFieldValue("Aadhaar Number", profile)).isEqualTo("1234 5678 9012");
        assertThat(FieldMatcher.matchFieldValue("pan_number", profile)).isEqualTo("ABCDE1234F");
    }
}
