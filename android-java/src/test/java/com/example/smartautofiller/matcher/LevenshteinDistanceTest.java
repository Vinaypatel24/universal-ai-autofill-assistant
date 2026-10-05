package com.example.smartautofiller.matcher;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class LevenshteinDistanceTest {

    @Test
    void testExactMatch() {
        assertThat(LevenshteinDistance.computeDistance("email", "email")).isEqualTo(0);
        assertThat(LevenshteinDistance.computeSimilarity("email", "email")).isEqualTo(1.0);
    }

    @Test
    void testSingleTypo() {
        int distance = LevenshteinDistance.computeDistance("fullname", "fullnam");
        assertThat(distance).isEqualTo(1);

        double similarity = LevenshteinDistance.computeSimilarity("fullname", "fullnam");
        assertThat(similarity).isGreaterThan(0.85);
    }

    @Test
    void testCompletelyDifferent() {
        double similarity = LevenshteinDistance.computeSimilarity("address", "phone");
        assertThat(similarity).isLessThan(0.4);
    }
}
