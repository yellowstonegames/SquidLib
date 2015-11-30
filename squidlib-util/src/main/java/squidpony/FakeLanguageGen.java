package squidpony;

import squidpony.squidmath.RNG;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A text generator for producing sentences and/or words in nonsense languages that fit a theme.
 * Created by Tommy Ettinger on 11/29/2015.
 */
public class FakeLanguageGen {
    protected final String[] vowels, openingConsonants, midConsonants, closingConsonants, vowelSplitters,
            closingSyllables;
    protected final LinkedHashMap<Integer, Double> syllableFrequencies;
    protected double totalSyllableFrequency = 0.0;
    protected final double vowelStartFrequency, vowelEndFrequency, vowelSplitFrequency, syllableEndFrequency;

    /**
     * Ia! Ia! Cthulhu Rl'yeh ftaghn! Useful for generating cultist ramblings or unreadable occult texts.
     */
    public static final FakeLanguageGen LOVECRAFT = new FakeLanguageGen(
            new String[]{"a", "i", "o", "e", "u", "a", "i", "o", "e", "u", "ia", "ai", "ei"},
            new String[]{"s", "t", "k", "n", "y", "p", "k", "l", "g", "gl", "th", "sh", "ny", "ft", "hm", "zvr", "cth"},
            new String[]{"h", "gl", "gr", "nd", "mr", "vr", "kr"},
            new String[]{"l", "p", "s", "t", "n", "k", "g", "x", "rl", "th", "gg", "gh", "ts", "lt", "rk", "kh", "sh", "ng", "shk"},
            new String[]{"aghn", "ulhu", "urath", "oigor", "alos", "'yeh", "achtal"},
            new String[]{"'", "-"}, new int[]{1, 2, 3}, new double[]{6, 7, 2}, 0.4, 0.31, 0.07, 0.04);
    /**
     * Imitation ancient Greek, romanized to use the Latin alphabet. Likely to seem pretty fake to many readers.
     */
    public static final FakeLanguageGen GREEK_ROMANIZED = new FakeLanguageGen(
            new String[]{"a", "a", "a", "o", "o", "o", "e", "e", "i", "i", "i", "au", "ai", "ai", "oi", "oi", "ia", "ou", "ou", "eo", "ei", "ei", "ui"},
            new String[]{"rh", "s", "z", "t", "t", "k", "ch", "n", "th", "kth", "m", "p", "ps", "b", "l", "kr", "g", "phth"},
            new String[]{"lph", "pl", "l", "l", "kr", "nch", "nx", "ps"},
            new String[]{"s", "p", "t", "ch", "n", "m", "s", "p", "t", "ch", "n", "m", "b", "g", "st", "rst", "rt", "sp", "rk", "ph", "x", "z", "nk", "ng", "th"},
            new String[]{"os", "os", "is", "us", "um", "eum", "ium", "iam", "us", "um", "es", "anes", "eros", "or", "ophon", "on", "otron"},
            new String[]{}, new int[]{1, 2, 3}, new double[]{5, 7, 4}, 0.45, 0.45, 0.0, 0.3);
    /**
     * Imitation ancient Greek, using the original Greek alphabet. People may try to translate it and get gibberish.
     * Make sure the font you use to render this supports the Greek alphabet! In the GDX display module, the "smooth"
     * fonts support all the Greek you need for this.
     */
    public static final FakeLanguageGen GREEK_AUTHENTIC = new FakeLanguageGen(
            new String[]{"α", "α", "α", "ο", "ο", "ο", "ε", "ε", "ι", "ι", "ι", "αυ", "αι", "αι", "οι", "οι", "ια", "ου", "ου", "εο", "ει", "ει", "υι"},
            new String[]{"ρ", "σ", "ζ", "τ", "τ", "κ", "χ", "ν", "θ", "κθ", "μ", "π", "ψ", "β", "λ", "κρ", "γ", "ϕθ"},
            new String[]{"λϕ", "πλ", "λ", "λ", "κρ", "γχ", "γξ", "ψ"},
            new String[]{"σ", "π", "τ", "χ", "ν", "μ", "σ", "π", "τ", "χ", "ν", "μ", "β", "γ", "στ", "ρστ", "ρτ", "σπ", "ρκ", "ϕ", "ξ", "ζ", "γκ", "γγ", "θ"},
            new String[]{"ος", "ος", "ις", "υς", "υμ", "ευμ", "ιυμ", "ιαμ", "υς", "υμ", "ες", "ανες", "ερος", "ορ", "οϕον", "ον", "οτρον"},
            new String[]{}, new int[]{1, 2, 3}, new double[]{5, 7, 4}, 0.45, 0.45, 0.0, 0.3);

    /**
     * This is a very complicated constructor! Maybe look at the calls to this to initialize static members of this
     * class, LOVECRAFT and GREEK_ROMANIZED.
     * @param vowels String array where each element is a vowel or group of vowels; elements may be repeated to make
     *               them more common
     * @param openingConsonants String array where each element is a consonant or consonant cluster that can appear
     *                          at the start of a word; elements may be repeated to make them more common
     * @param midConsonants String array where each element is a consonant or consonant cluster than can appear
     *                      between vowels, and all closingConsonants are automatically copied into this internally;
     *                      elements may be repeated to make them more common
     * @param closingConsonants String array where each element is a consonant or consonant cluster than can appear
     *                          at the end of a word; elements may be repeated to make them more common
     * @param closingSyllables String array where each element is a syllable starting with a vowel and ending in
     *                         whatever the word should end in; elements may be repeated to make them more common
     * @param vowelSplitters String array where each element is a mark that goes between vowels, so if "-" is in this,
     *                       then "a-a" may be possible; elements may be repeated to make them more common
     * @param syllableLengths int array where each element is a possible number of syllables a word can use; closely
     *                        tied to syllableFrequencies
     * @param syllableFrequencies double array where each element corresponds to an element in syllableLengths and
     *                            represents how often each syllable count should appear relative to other counts; there
     *                            is no need to restrict the numbers to add up to any other number
     * @param vowelStartFrequency a double between 0.0 and 1.0 that determines how often words start with vowels;
     *                            higher numbers yield more words starting with vowels
     * @param vowelEndFrequency a double between 0.0 and 1.0 that determines how often words end with vowels; higher
     *                          numbers yield more words ending in vowels
     * @param vowelSplitFrequency a double between 0.0 and 1.0 that, if vowelSplitters is not empty, determines how
     *                            often a vowel will be split into two vowels separated by one of those splitters
     * @param syllableEndFrequency a double between 0.0 and 1.0 that determines how often an element of
     *                             closingSyllables is used instead of ending normally
     */
    public FakeLanguageGen(String[] vowels, String[] openingConsonants, String[] midConsonants, String[] closingConsonants,
                           String[] closingSyllables, String[] vowelSplitters,
                           int[] syllableLengths, double[] syllableFrequencies, double vowelStartFrequency,
                           double vowelEndFrequency, double vowelSplitFrequency, double syllableEndFrequency) {
        this.vowels = vowels;
        this.openingConsonants = openingConsonants;
        this.midConsonants = new String[midConsonants.length + closingConsonants.length];
        System.arraycopy(midConsonants, 0, this.midConsonants, 0, midConsonants.length);
        System.arraycopy(closingConsonants, 0, this.midConsonants, midConsonants.length, closingConsonants.length);
        this.closingConsonants = closingConsonants;
        this.vowelSplitters = vowelSplitters;
        this.closingSyllables = closingSyllables;

        this.syllableFrequencies = new LinkedHashMap<Integer, Double>(syllableLengths.length);
        for (int i = 0; i < syllableLengths.length && i < syllableFrequencies.length; i++) {
            this.syllableFrequencies.put(syllableLengths[i], syllableFrequencies[i]);
        }
        for (Double freq : this.syllableFrequencies.values()) {
            totalSyllableFrequency += freq;
        }
        if (vowelStartFrequency > 1.0)
            this.vowelStartFrequency = 1.0 / vowelStartFrequency;
        else
            this.vowelStartFrequency = vowelStartFrequency;
        if (vowelEndFrequency > 1.0)
            this.vowelEndFrequency = 1.0 / vowelEndFrequency;
        else
            this.vowelEndFrequency = vowelEndFrequency;
        if (vowelSplitters.length == 0)
            this.vowelSplitFrequency = 0.0;
        else if (vowelSplitFrequency > 1.0)
            this.vowelSplitFrequency = 1.0 / vowelSplitFrequency;
        else
            this.vowelSplitFrequency = vowelSplitFrequency;
        if (closingSyllables.length == 0)
            this.syllableEndFrequency = 0.0;
        else if (syllableEndFrequency > 1.0)
            this.syllableEndFrequency = 1.0 / syllableEndFrequency;
        else
            this.syllableEndFrequency = syllableEndFrequency;
    }

    /**
     * Generate a word from this FakeLanguageGen.
     * @param rng the RNG to use for the randomized string building
     * @param capitalize true if the word should start with a capital letter, false otherwise
     * @return a word in the fake language as a String
     */
    public String word(RNG rng, boolean capitalize) {
        StringBuilder sb = new StringBuilder(20);
        if (rng.nextDouble() < vowelStartFrequency) {
            sb.append(rng.getRandomElement(vowels));
        }
        sb.append(rng.getRandomElement(openingConsonants));

        double syllableChance = rng.nextDouble(totalSyllableFrequency);
        int syllables = 1;
        for (Map.Entry<Integer, Double> kv : syllableFrequencies.entrySet()) {
            if (syllableChance < kv.getValue()) {
                syllables = kv.getKey();
                break;
            } else
                syllableChance -= kv.getValue();
        }
        for (int i = 0; i < syllables - 1; i++) {
            sb.append(rng.getRandomElement(vowels));
            if (rng.nextDouble() < vowelSplitFrequency) {
                sb.append(rng.getRandomElement(vowelSplitters));
                sb.append(rng.getRandomElement(vowels));
            }
            sb.append(rng.getRandomElement(midConsonants));
        }
        if (rng.nextDouble() < syllableEndFrequency) {
            sb.append(rng.getRandomElement(closingSyllables));
        } else {
            sb.append(rng.getRandomElement(vowels));
            if (rng.nextDouble() < vowelSplitFrequency) {
                sb.append(rng.getRandomElement(vowelSplitters));
                sb.append(rng.getRandomElement(vowels));
            }
            if (rng.nextDouble() >= vowelEndFrequency) {
                sb.append(rng.getRandomElement(closingConsonants));
                if (rng.nextDouble() < syllableEndFrequency) {
                    sb.append(rng.getRandomElement(closingSyllables));
                }
            }
        }
        if (capitalize)
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     * Generate a sentence from this FakeLanguageGen
     * @param rng the RNG to use for the randomized string building
     * @param minWords an int for the minimum number of words in a sentence; should be at least 1
     * @param maxWords an int for the maximum number of words in a sentence; should be at least equal to minWords
     * @param midPunctuation a String array where each element is a comma, semicolon, or the like that goes before a
     *                       space in the middle of a sentence
     * @param endPunctuation a String array where each element is a period, question mark, or the like that goes at
     *                       the very end of a sentence
     * @param midPunctuationFrequency a double between 0.0 and 1.0 that determines how often Strings from
     *                                midPunctuation should be inserted before spaces
     * @return a sentence in the fake language as a String
     */
    public String sentence(RNG rng, int minWords, int maxWords, String[] midPunctuation, String[] endPunctuation,
                           double midPunctuationFrequency) {
        if (minWords < 1)
            minWords = 1;
        if (minWords > maxWords)
            maxWords = minWords;
        if (midPunctuationFrequency > 1.0) {
            midPunctuationFrequency = 1.0 / midPunctuationFrequency;
        }
        StringBuilder sb = new StringBuilder(12 * maxWords);
        sb.append(word(rng, true));
        for (int i = 1; i < minWords; i++) {
            if (rng.nextDouble() < midPunctuationFrequency) {
                sb.append(rng.getRandomElement(midPunctuation));
            }
            sb.append(' ');
            sb.append(word(rng, false));
        }
        for (int i = minWords; i < maxWords && rng.nextInt(2 * maxWords) > i; i++) {
            if (rng.nextDouble() < midPunctuationFrequency) {
                sb.append(rng.getRandomElement(midPunctuation));
            }
            sb.append(' ');
            sb.append(word(rng, false));
        }
        sb.append(rng.getRandomElement(endPunctuation));
        return sb.toString();
    }

}
