package squidpony.squidtext.nolithiusgen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import squidpony.annotation.Beta;
import squidpony.squidmath.RNG;
import squidpony.squidtext.StringUtils;
import squidpony.squidutility.SCollections;

/**
 * Based on work by Nolithius available at the following two sites
 * https://github.com/Nolithius/weighted-letter-namegen
 * http://code.google.com/p/weighted-letter-namegen/
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class WeightedLetterNamegen {

    public static int LAST_LETTER_CANDIDATES_MAX = 5;
    private static final RNG rng = new RNG();

    private boolean initialized = false;

    private String[] names;
    private ArrayList<Integer> sizes;

    private TreeMap<Character, WeightedLetter> letters;
    private ArrayList<Character> firstLetterSamples;
    private ArrayList<Character> lastLetterSamples;

    public WeightedLetterNamegen(String[] names) {
        this.names = names;
    }

    /**
     * Initialization, statistically measures letter likelyhood. Called by generate() the first
     * time.
     */
    private void init() {
        sizes = new ArrayList<>();
        letters = new TreeMap<>();
        firstLetterSamples = new ArrayList<>();
        lastLetterSamples = new ArrayList<>();

        for (int i = 0; i < names.length - 1; i++) {
            String name = names[i];
            if (name == null || name.length() < 1) {
                continue;
            }

            // (1) Insert size
            sizes.add(name.length());

            // (2) Grab first letter
            firstLetterSamples.add(name.charAt(0));

            // (3) Grab last letter
            lastLetterSamples.add(name.charAt(name.length() - 1));

            // (4) Process all letters
            for (int n = 0; n < name.length() - 1; n++) {
                char letter = name.charAt(n);
                char nextLetter = name.charAt(n + 1);

                // Create letter if it doesn't exist
                WeightedLetter wl = letters.get(letter);
                if (wl == null) {
                    wl = new WeightedLetter(letter);
                    letters.put(letter, wl);
                }
                wl.addNextLetter(nextLetter);

                // If letter was uppercase (beginning of name), also add a lowercase entry
                if (Character.isUpperCase(letter)) {
                    letter = Character.toLowerCase(letter);

                    wl = letters.get(letter);
                    if (wl == null) {
                        wl = new WeightedLetter(letter);
                        letters.put(letter, wl);
                    }
                    wl.addNextLetter(nextLetter);
                }
            }
        }

        for (WeightedLetter weightedLetter : letters.values()) {
            // Expand letters into samples
            weightedLetter.nextLetters.expandSamples();
        }

        initialized = true;
    }

    public String[] generate() {
        return generate(1);
    }

    public String[] generate(int amountToGenerate) {
        // Initialize if called for the first time
        if (!initialized) {
            init();
        }

        ArrayList<String> result = new ArrayList<>();

        int nameCount = 0;
        while (nameCount < amountToGenerate) {
            String name = "";

            // Pick size
            int size = SCollections.getRandomElement(sizes);

            // Pick first letter
            char firstLetter = SCollections.getRandomElement(firstLetterSamples);

            name += firstLetter;

            for (int i = 1; i < size - 2; i++) {
                name += getRandomNextLetter(name.charAt(name.length() - 1));
            }

            // Attempt to find a last letter
            for (int lastLetterFits = 0; lastLetterFits < LAST_LETTER_CANDIDATES_MAX; lastLetterFits++) {
                char lastLetter = SCollections.getRandomElement(lastLetterSamples);
                char intermediateLetterCandidate = getIntermediateLetter(name.charAt(name.length() - 1), lastLetter);

                // Only attach last letter if the candidate is valid (if no candidate, the antepenultimate letter always occurs at the end)
                if (Character.isLetter(intermediateLetterCandidate)) {
                    name += intermediateLetterCandidate;
                    name += lastLetter;
                    break;
                }
            }

            String nameString = name;

            // Check that the word has no triple letter sequences, and that the Levenshtein distance is kosher
            if (tripleLetterCheck(name) && checkLevenshtein(nameString)) {
                result.add(nameString);

                // Only increase the counter if we've successfully added a name
                nameCount++;
            }
        }

        return result.toArray(new String[]{});
    }

    /**
     * Searches for the best fit letter between the letter before and the letter after (non-random).
     * Used to determine penultimate letters in names.
     *
     * @param	letterBefore	The letter before the desired letter.
     * @param	letterAfter	The letter after the desired letter.
     * @return	The best fit letter between the provided letters.
     */
    private char getIntermediateLetter(char letterBefore, char letterAfter) {
        if (Character.isLetter(letterBefore) && Character.isLetter(letterAfter)) {
            // First grab all letters that come after the 'letterBefore'
            LinkedHashMap<Character, Integer> letterCandidates = letters.get(letterBefore).nextLetters.sequences;

            char bestFitLetter = '\'';
            int bestFitScore = 0;

            // Step through candidates, and return best scoring letter
            for (char letter : letterCandidates.keySet()) {
                WeightedLetterGroup weightedLetterGroup = letters.get(letter).nextLetters;
                Integer letterCounter = weightedLetterGroup.sequences.get(letterAfter);

                if (letterCounter != null && letterCounter > bestFitScore) {
                    bestFitLetter = letter;
                    bestFitScore = letterCounter;
                }
            }

            return bestFitLetter;
        } else {
            return '-';
        }
    }

    /**
     * Checks that no three letters happen in succession.
     *
     * @param	name	The name array (easier to iterate)
     * @return	True if no triple letter sequence is found.
     */
    private boolean tripleLetterCheck(String name) {
        for (int i = 2; i < name.length(); i++) {
            if (name.charAt(i) == name.charAt(i - 1) && name.charAt(i) == name.charAt(i - 2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks that the Damerau-Levenshtein distance of this name is within a given bias from a name
     * on the master list.
     *
     * @param	name	The name string.
     * @return	True if a name is found that is within the bias.
     */
    private boolean checkLevenshtein(String name) {
        int levenshteinBias = name.length() / 2;

        // Grab the closest matches, just for fun
        String closestName = "";
        int closestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < names.length; i++) {
            int levenshteinDistance = StringUtils.damerau(name, names[i]);

            // This is just to get an idea of what is failing
            if (levenshteinDistance < closestDistance) {
                closestDistance = levenshteinDistance;
                closestName = names[i];
            }

            if (levenshteinDistance <= levenshteinBias) {
                return true;
            }
        }

        return false;
    }

    private char getRandomNextLetter(char letter) {
        WeightedLetter weightedLetter = letters.get(letter);
        char[] samples = weightedLetter.nextLetters.expandSamples();
        return samples[rng.nextInt(samples.length)];// pickRandomElementFromArray(weightedLetter.nextLetters.letterSamples);
    }
}
