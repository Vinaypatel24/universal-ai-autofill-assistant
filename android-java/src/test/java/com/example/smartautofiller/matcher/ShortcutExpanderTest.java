package com.example.smartautofiller.matcher;

import com.example.smartautofiller.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShortcutExpanderTest {

    private UserProfile profile;

    @BeforeEach
    void setUp() {
        profile = new UserProfile("Personal", "Pushpraj Singhal", "pushpraj@example.com", "9876543210", "123 Tech Street");
    }

    @Test
    void testSingleLineShortcut() {
        ShortcutExpander.ExpandResult res = ShortcutExpander.expandShortcuts("name-", profile);
        assertThat(res.isFilled()).isTrue();
        assertThat(res.getUpdatedText()).isEqualTo("name- Pushpraj Singhal");
    }

    @Test
    void testMultiLineShortcuts() {
        String input = "name-\nemail-\nmob-";
        ShortcutExpander.ExpandResult res = ShortcutExpander.expandShortcuts(input, profile);
        assertThat(res.isFilled()).isTrue();
        assertThat(res.getUpdatedText()).isEqualTo(
                "name- Pushpraj Singhal\nemail- pushpraj@example.com\nmob- 9876543210"
        );
    }

    @Test
    void testNoMatchingShortcut() {
        String input = "random-text-without-shortcut";
        ShortcutExpander.ExpandResult res = ShortcutExpander.expandShortcuts(input, profile);
        assertThat(res.isFilled()).isFalse();
        assertThat(res.getUpdatedText()).isEqualTo(input);
    }
}
