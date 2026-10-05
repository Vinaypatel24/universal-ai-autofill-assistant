package com.example.smartautofiller.database;

import com.example.smartautofiller.model.ProfileSection;
import com.example.smartautofiller.model.SectionField;
import com.example.smartautofiller.model.UserProfile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileJsonSerializerTest {

    @Test
    void testExportAndImportCycle() {
        UserProfile profile = new UserProfile("College", "Jane Doe", "jane@university.edu", "555-0199", "Tech Street");
        profile.getCustomFields().put("rollNumber", "CS101");

        ProfileSection marksheet = new ProfileSection("Marksheet", "🎓");
        marksheet.addField(new SectionField("Maths", "98"));
        marksheet.addField(new SectionField("Physics", "95"));
        profile.getSections().add(marksheet);

        List<UserProfile> originalList = new ArrayList<>();
        originalList.add(profile);

        String json = ProfileJsonSerializer.exportProfilesToJson(originalList);
        assertThat(json).contains("Jane Doe");
        assertThat(json).contains("Maths");
        assertThat(json).contains("CS101");

        List<UserProfile> imported = ProfileJsonSerializer.importProfilesFromJson(json);
        assertThat(imported).hasSize(1);
        UserProfile p = imported.get(0);
        assertThat(p.getFullName()).isEqualTo("Jane Doe");
        assertThat(p.getCustomFields().get("rollNumber")).isEqualTo("CS101");
        assertThat(p.getSections().get(0).getFields()).hasSize(2);
        assertThat(p.getSections().get(0).getFields().get(0).getLabel()).isEqualTo("Maths");
    }
}
