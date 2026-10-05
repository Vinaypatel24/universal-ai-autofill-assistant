package com.example.smartautofiller.matcher;

/**
 * Calculates string similarity using the Levenshtein Distance algorithm.
 * Used for fuzzy matching between form labels and user profile fields.
 */
public final class LevenshteinDistance {

    private LevenshteinDistance() {
        // Utility class
    }

    /**
     * Computes the Levenshtein edit distance between two strings.
     *
     * @param s1 first string
     * @param s2 second string
     * @return minimum number of single-character edits required
     */
    public static int computeDistance(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return s2.length();
        if (s2 == null) return s1.length();

        int len1 = s1.length();
        int len2 = s2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            char c1 = s1.charAt(i - 1);
            for (int j = 1; j <= len2; j++) {
                char c2 = s2.charAt(j - 1);
                int cost = (c1 == c2) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[len1][len2];
    }

    /**
     * Computes normalized similarity between 0.0 (completely different) and 1.0 (exact match).
     *
     * @param s1 first string
     * @param s2 second string
     * @return similarity score between 0.0 and 1.0
     */
    public static double computeSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        String str1 = s1.trim().toLowerCase();
        String str2 = s2.trim().toLowerCase();

        if (str1.equals(str2)) return 1.0;
        int maxLen = Math.max(str1.length(), str2.length());
        if (maxLen == 0) return 1.0;

        int distance = computeDistance(str1, str2);
        return 1.0 - ((double) distance / maxLen);
    }
}
