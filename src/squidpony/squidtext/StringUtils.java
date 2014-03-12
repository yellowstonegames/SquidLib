package squidpony.squidtext;
 
import squidpony.annotation.Beta;

/**
 * Based on work by Nolithius available at the following two sites
 * https://github.com/Nolithius/weighted-letter-namegen
 * http://code.google.com/p/weighted-letter-namegen/
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class StringUtils {

    public static String ucfirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    /**
     * Computes the Levenshtein distance between two words.
     *
     * Cost of operations is assumed to be 1. Implemented from algorithm found in
     * http://en.wikipedia.org/wiki/Levenshtein_distance
     *
     * @param	word1	The first word to compare.
     * @param	word2	The second word to compare.
     * @return	The Levenshtein distance between the two words.
     */
    public static int levenshtein(String word1, String word2) {
        return levenshtein(word1, word2, 1, 1, 1);
    }

    public static int levenshtein(String word1, String word2, int deleteCost, int insertCost, int removeCost) {
        // Initialize sizes
        int word1Length = word1.length();
        int word2Length = word2.length();
        int[][] levenshteinMatrix = new int[word1Length + 1][word2Length + 1];

        // Initial values for word1
        for (int i_m = 0; i_m <= word1Length; i_m++) {
            levenshteinMatrix[i_m][0] = i_m;
        }

        // Initial values for word2
        for (int i_n = 0; i_n <= word2Length; i_n++) {
            levenshteinMatrix[0][i_n] = i_n;
        }

        for (int i_m = 1; i_m <= word1Length; i_m++) {
            for (int i_n = 1; i_n <= word2Length; i_n++) {
                // The same
                if (word1.charAt(i_m - 1) == word2.charAt(i_n - 1)) {
                    levenshteinMatrix[i_m][i_n] = levenshteinMatrix[i_m - 1][i_n - 1];
                } // Different values
                else {
                    levenshteinMatrix[i_m][i_n] = Math.min(
                            Math.min(levenshteinMatrix[i_m - 1][i_n] + deleteCost,
                                    levenshteinMatrix[i_m][i_n - 1] + insertCost),
                            levenshteinMatrix[i_m - 1][i_n - 1] + removeCost
                    );
                }
            }
        }

        // Return the shortest path, defined by levenshteinMatrix[word1Length][word2Length];
        return levenshteinMatrix[word1Length][word2Length];
    }

    /**
     * Computes the Damerau-Levenshtein distance between two words. The Damerau-Levenshtein distance
     * is the same as the Levenshtein distance, except it counts transposition of adjacent letters,
     * as 1. For example, the Levenshtein distance of "time" vs. "tiem" is 2, but the
     * Damerau-Levenshtein distance is 1, since only one operation occurred: transposition.
     * Implemented from algorithm found in http://en.wikipedia.org/wiki/Damerau-Levenshtein_distance
     *
     * @param	word1	The first word to compare.
     * @param	word2	The second word to compare.
     * @return	The Damerau-Levenshtein distance between the two words.
     */
    public static int damerau(String word1, String word2) {
        // Initialize sizes
        int word1Length = word1.length();
        int word2Length = word2.length();
        int[][] levenshteinMatrix = new int[word1Length + 1][word2Length + 1];

        // Initial values for word1
        for (int i_m = 0; i_m <= word1Length; i_m++) {
            levenshteinMatrix[i_m][0] = i_m;
        }

        // Initial values for word2
        for (int i_n = 0; i_n <= word2Length; i_n++) {
            levenshteinMatrix[0][i_n] = i_n;
        }

        for (int i_m = 1; i_m <= word1Length; i_m++) {
            for (int i_n = 1; i_n <= word2Length; i_n++) {
                int cost = 1;

                // The same, zero cost
                if (word1.charAt(i_m - 1) == word2.charAt(i_n - 1)) {
                    cost = 0;
                }

                levenshteinMatrix[i_m][i_n] = Math.min(
                        Math.min(levenshteinMatrix[i_m - 1][i_n] + 1, // Deletion
                                levenshteinMatrix[i_m][i_n - 1] + 1), // Insertion
                        levenshteinMatrix[i_m - 1][i_n - 1] + cost // Removal
                );

                // Test for transposition
                if (i_m > 1 && i_n > 1 && word1.charAt(i_m - 1) == word2.charAt(i_n - 2) && word1.charAt(i_m - 2) == word2.charAt(i_n - 1)) {
                    levenshteinMatrix[i_m][i_n] = Math.min(
                            levenshteinMatrix[i_m][i_n],
                            levenshteinMatrix[i_m - 2][i_n - 2] + cost // Transposition
                    );
                }
            }
        }

        // Return the shortest path, defined by levenshteinMatrix[word1Length][word2Length];
        return levenshteinMatrix[word1Length][word2Length];
    }

    private StringUtils() {
    }
}
