package squidpony;

import squidpony.squidmath.RNG;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A text generator for producing sentences and/or words in nonsense languages that fit a theme. This does not use an
 * existing word list as a basis for its output, so it may or may not produce existing words occasionally, but you can
 * safely assume it won't generate a meaningful sentence except in the absolute unlikeliest of cases.
 * Created by Tommy Ettinger on 11/29/2015.
 */
public class FakeLanguageGen implements Serializable {
    private static final long serialVersionUID = -3578460257281186351L;
    public final String[] openingVowels, midVowels, openingConsonants, midConsonants, closingConsonants,
            vowelSplitters, closingSyllables;
    public final LinkedHashMap<Integer, Double> syllableFrequencies;
    protected double totalSyllableFrequency = 0.0;
    public final double vowelStartFrequency, vowelEndFrequency, vowelSplitFrequency, syllableEndFrequency;
    private static final Pattern removeRepeats = Pattern.compile("(.)\\1+(.)\\2+");

    /**
     * Ia! Ia! Cthulhu Rl'yeh ftaghn! Useful for generating cultist ramblings or unreadable occult texts.
     * <br>
     * Zvrugg pialuk, ya'as irlemrugle'eith iposh hmo-es nyeighi, glikreirk shaivro'ei!
     */
    public static final FakeLanguageGen LOVECRAFT = new FakeLanguageGen(
            new String[]{"a", "i", "o", "e", "u", "a", "i", "o", "e", "u", "ia", "ai", "aa", "ei"},
            new String[]{},
            new String[]{"s", "t", "k", "n", "y", "p", "k", "l", "g", "gl", "th", "sh", "ny", "ft", "hm", "zvr", "cth"},
            new String[]{"h", "gl", "gr", "nd", "mr", "vr", "kr"},
            new String[]{"l", "p", "s", "t", "n", "k", "g", "x", "rl", "th", "gg", "gh", "ts", "lt", "rk", "kh", "sh", "ng", "shk"},
            new String[]{"aghn", "ulhu", "urath", "oigor", "alos", "'yeh", "achtal", "urath", "ikhet", "adzek"},
            new String[]{"'", "-"}, new int[]{1, 2, 3}, new double[]{6, 7, 2}, 0.4, 0.31, 0.07, 0.04);
    /**
     * Imitation English; may seem closer to Dutch in some generated text, and is not exactly the best imitation.
     * Should seem pretty fake to many readers; does not filter out dictionary words. If you want to
     * avoid generating certain words, you can subclass FakeLanguageGen and modify the word() method.
     * <br>
     * Mont tiste frot; mousation hauddes?
     * Lily wrely stiebes; flarrousseal gapestist.
     */
    public static final FakeLanguageGen ENGLISH = new FakeLanguageGen(
            new String[]{
                    "a", "a", "a", "a", "o", "o", "o", "e", "e", "e", "e", "e", "i", "i", "i", "i", "u",
                    "a", "a", "a", "a", "o", "o", "o", "e", "e", "e", "e", "e", "i", "i", "i", "i", "u",
                    "a", "a", "a", "o", "o", "e", "e", "e", "i", "i", "i", "u",
                    "a", "a", "a", "o", "o", "e", "e", "e", "i", "i", "i", "u",
                    "au", "ai", "ai", "ou", "ea", "ie", "io", "ei",
            },
            new String[]{"u", "u", "oa", "oo", "oo", "oo", "ee", "ee", "ee", "ee",},
            new String[]{
                    "b", "bl", "br", "c", "cl", "cr", "ch", "d", "dr", "f", "fl", "fr", "g", "gl", "gr", "h", "j", "k", "l", "m", "n",
                    "p", "pl", "pr", "qu", "r", "s", "sh", "sk", "st", "sp", "sl", "sm", "sn", "t", "tr", "th", "thr", "v", "w", "y", "z",
                    "b", "bl", "br", "c", "cl", "cr", "ch", "d", "dr", "f", "fl", "fr", "g", "gr", "h", "j", "k", "l", "m", "n",
                    "p", "pl", "pr", "r", "s", "sh", "st", "sp", "sl", "t", "tr", "th", "w", "y",
                    "b", "br", "c", "ch", "d", "dr", "f", "g", "h", "j", "l", "m", "n",
                    "p", "r", "s", "sh", "st", "sl", "t", "tr", "th",
                    "b", "d", "f", "g", "h", "l", "m", "n",
                    "p", "r", "s", "sh", "t", "th",
                    "b", "d", "f", "g", "h", "l", "m", "n",
                    "p", "r", "s", "sh", "t", "th",
                    "r", "s", "t", "l", "n",
                    "str", "spr", "spl", "wr", "kn", "kn", "gn",
            },
            new String[]{"x", "cst", "bs", "ff", "lg", "g", "gs",
                    "ll", "ltr", "mb", "mn", "mm", "ng", "ng", "ngl", "nt", "ns", "nn", "ps", "mbl", "mpr",
                    "pp", "ppl", "ppr", "rr", "rr", "rr", "rl", "rtn", "ngr", "ss", "sc", "rst", "tt", "tt", "ts", "ltr", "zz"
            },
            new String[]{"b", "rb", "bb", "c", "rc", "ld", "d", "ds", "dd", "f", "ff", "lf", "rf", "rg", "gs", "ch", "lch", "rch", "tch",
                    "ck", "ck", "lk", "rk", "l", "ll", "lm", "m", "rm", "mp", "n", "nk", "nch", "nd", "ng", "ng", "nt", "ns", "lp", "rp",
                    "p", "r", "rn", "rts", "s", "s", "s", "s", "ss", "ss", "st", "ls", "t", "t", "ts", "w", "wn", "x", "ly", "lly", "z",
                    "b","c","d","f","g","k","l","m","n","p","r","s","t","w",
            },
            new String[]{"ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                    "ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                    "ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                    "ay", "ay", "ey", "oy", "ay", "ay", "ey", "oy",
                    "ough", "aught", "ant", "ont", "oe", "ance", "ell", "eal", "oa", "urt", "ut", "iom", "ion", "ion", "ision", "ation", "ation", "ition",
                    "ough", "aught", "ant", "ont", "oe", "ance", "ell", "eal", "oa", "urt", "ut", "iom", "ion", "ion", "ision", "ation", "ation", "ition",
                    "ily", "ily", "ily", "adly", "owly", "oorly", "ardly", "iedly",
            },
            new String[]{}, new int[]{1, 2, 3, 4}, new double[]{7, 8, 4, 1}, 0.22, 0.1, 0.0, 0.25);
    /**
     * Imitation ancient Greek, romanized to use the Latin alphabet. Likely to seem pretty fake to many readers.
     * <br>
     * Psuilas alor; aipeomarta le liaspa...
     */
    public static final FakeLanguageGen GREEK_ROMANIZED = new FakeLanguageGen(
            new String[]{"a", "a", "a", "o", "o", "o", "e", "e", "i", "i", "i", "au", "ai", "ai", "oi", "oi", "ia", "io", "ou", "ou", "eo", "ei"},
            new String[]{"ui", "ei"},
            new String[]{"rh", "s", "z", "t", "t", "k", "ch", "n", "th", "kth", "m", "p", "ps", "b", "l", "kr", "g", "phth"},
            new String[]{"lph", "pl", "l", "l", "kr", "nch", "nx", "ps"},
            new String[]{"s", "p", "t", "ch", "n", "m", "s", "p", "t", "ch", "n", "m", "b", "g", "st", "rst", "rt", "sp", "rk", "ph", "x", "z", "nk", "ng", "th"},
            new String[]{"os", "os", "is", "us", "um", "eum", "ium", "iam", "us", "um", "es", "anes", "eros", "or", "ophon", "on", "otron"},
            new String[]{}, new int[]{1, 2, 3}, new double[]{5, 7, 4}, 0.45, 0.45, 0.0, 0.3);
    /**
     * Imitation ancient Greek, using the original Greek alphabet. People may try to translate it and get gibberish.
     * Make sure the font you use to render this supports the Greek alphabet! In the GDX display module, the "smooth"
     * fonts support all the Greek you need for this.
     * <br>
     * Ψυιλασ αλορ; αιπεομαρτα λε λιασπα...
     */
    public static final FakeLanguageGen GREEK_AUTHENTIC = new FakeLanguageGen(
            new String[]{"α", "α", "α", "ο", "ο", "ο", "ε", "ε", "ι", "ι", "ι", "αυ", "αι", "αι", "οι", "οι", "ια", "ιο", "ου", "ου", "εο", "ει"},
            new String[]{"υι", "ει"},
            new String[]{"ρ", "σ", "ζ", "τ", "τ", "κ", "χ", "ν", "θ", "κθ", "μ", "π", "ψ", "β", "λ", "κρ", "γ", "ϕθ"},
            new String[]{"λϕ", "πλ", "λ", "λ", "κρ", "γχ", "γξ", "ψ"},
            new String[]{"σ", "π", "τ", "χ", "ν", "μ", "σ", "π", "τ", "χ", "ν", "μ", "β", "γ", "στ", "ρστ", "ρτ", "σπ", "ρκ", "ϕ", "ξ", "ζ", "γκ", "γγ", "θ"},
            new String[]{"ος", "ος", "ις", "υς", "υμ", "ευμ", "ιυμ", "ιαμ", "υς", "υμ", "ες", "ανες", "ερος", "ορ", "οϕον", "ον", "οτρον"},
            new String[]{}, new int[]{1, 2, 3}, new double[]{5, 7, 4}, 0.45, 0.45, 0.0, 0.3);

    /**
     * Imitation modern French, using (too many of) the accented vowels that are present in the language. Translating it
     * will produce gibberish if it produces anything at all. In the GDX display module, the "smooth" and "unicode"
     * fonts support all the accented characters you need for this.
     * <br>
     * Fa veau, ja ri avé re orçe jai braï aisté.
     */
    public static final FakeLanguageGen FRENCH = new FakeLanguageGen(
            new String[]{"a", "a", "a", "e", "e", "e", "i", "i", "o", "u", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                    "a", "a", "a", "e", "e", "e", "i", "i", "o", "u", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                    "a", "a", "e", "e", "i", "o", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                    "ai", "oi", "oui", "au", "œu", "ou"
            },
            new String[]{
                    "ai", "aie", "aou", "eau", "oi", "oui", "oie", "eu", "eu",
                    "à", "â", "ai", "aî", "aï", "aie", "aou", "aoû", "au", "ay", "e", "é", "ée", "è",
                    "ê", "eau", "ei", "eî", "eu", "eû", "i", "î", "ï", "o", "ô", "oe", "oê", "oë", "œu",
                    "oi", "oie", "oï", "ou", "oû", "oy", "u", "û", "ue"
            },
            new String[]{"tr", "ch", "m", "b", "b", "br", "j", "j", "j", "j", "g", "t", "t", "t", "c", "d", "f", "f", "h", "n", "l", "l",
                     "s", "s", "s", "r", "r", "r", "v", "v", "p", "pl", "pr", "bl", "br", "dr", "gl", "gr"},
            new String[]{"cqu", "gu", "qu", "rqu", "nt", "ng", "ngu", "mb", "ll", "nd", "ndr", "nct", "st",
                         "xt", "mbr", "pl", "g", "gg", "ggr", "gl",
                         "m", "m", "mm", "v", "v", "f", "f", "f", "ff", "b", "b", "bb", "d", "d", "dd", "s", "s", "s", "ss",  "ss", "ss",
                         "cl", "cr", "ng", "ç", "ç", "rç"},
            new String[]{},
            new String[]{"e", "e", "e", "e", "e", "é", "é", "er", "er", "er", "er", "er", "es", "es", "es", "es", "es", "es",
                    "e", "e", "e", "e", "e", "é", "é", "er", "er", "er", "er", "er", "er", "es", "es", "es", "es", "es",
                    "e", "e", "e", "e", "e", "é", "é", "é", "er", "er", "er", "er", "er", "es", "es", "es", "es", "es",
                    "ent", "em", "en", "en", "aim", "ain", "an", "oin", "ien", "iere", "ors", "anse",
                    "ombs", "ommes", "ancs", "ends", "œufs", "erfs", "ongs", "aps", "ats", "ives", "ui", "illes",
                    "aen", "aon", "am", "an", "eun", "ein", "age", "age", "uile", "uin", "um", "un", "un", "un",
                    "aille", "ouille", "eille", "ille", "eur", "it", "ot", "oi", "oi", "oi", "aire", "om", "on", "on",
                    "im", "in", "in", "ien", "ien", "ion", "il", "eil", "oin", "oint", "iguïté", "ience", "incte",
                    "ang", "ong", "acré", "eau", "ouche", "oux", "oux", "ect", "ecri", "agne", "uer", "aix", "eth", "ut", "ant",
                    "anc", "anc", "anche", "ioche", "eaux", "ive", "eur", "ancois", "ecois"},
            new String[]{}, new int[]{1, 2, 3}, new double[]{18, 7, 2}, 0.35, 1.0, 0.0, 0.55);

    /**
     * Imitation modern Russian, romanized to use the Latin alphabet. Likely to seem pretty fake to many readers.
     * <br>
     * Zhydotuf ruts pitsas, gogutiar shyskuchebab - gichapofeglor giunuz ieskaziuzhin.
     */
    public static final FakeLanguageGen RUSSIAN_ROMANIZED = new FakeLanguageGen(
            new String[]{"a", "e", "e", "i", "i", "o", "u", "ie", "y", "e", "iu", "ia", "y", "a", "a", "o", "u"},
            new String[]{},
            new String[]{"b", "v", "g", "d", "k", "l", "p", "r", "s", "t", "f", "kh", "ts",
                    "b", "v", "g", "d", "k", "l", "p", "r", "s", "t", "f", "kh", "ts",
                    "b", "v", "g", "d", "k", "l", "p", "r", "s", "t", "f",
                    "zh", "m", "n", "z", "ch", "sh", "shch",
                    "br", "sk", "tr", "bl", "gl", "kr", "gr"},
            new String[]{"bl", "br", "pl", "dzh", "tr", "gl", "gr", "kr"},
            new String[]{"b", "v", "g", "d", "zh", "z", "k", "l", "m", "n", "p", "r", "s", "t", "f", "kh", "ts", "ch", "sh",
                    "v", "f", "sk", "sk", "sk", "s", "b", "d", "d", "n", "r", "r"},
            new String[]{"odka", "odna", "usk", "ask", "usky", "ad", "ar", "ovich", "ev", "ov", "of", "agda", "etsky", "ich", "on", "akh", "iev", "ian"},
            new String[]{}, new int[]{1, 2, 3, 4, 5, 6}, new double[]{4, 5, 6, 5, 3, 1}, 0.1, 0.2, 0.0, 0.12);


    /**
     * Imitation modern Russian, using the authentic Cyrillic alphabet used in Russia and other countries.
     * Make sure the font you use to render this supports the Cyrillic alphabet!
     * In the GDX display module, the "smooth" fonts support all the Cyrillic alphabet you need for this.
     * <br>
     * Жыдотуф руц пйцас, гогутяр шыскучэбаб - гйчапофёглор гюнуз ъсказюжин.
     */
    public static final FakeLanguageGen RUSSIAN_AUTHENTIC = new FakeLanguageGen(
            new String[]{"а", "е", "ё", "и", "й", "о", "у", "ъ", "ы", "э", "ю", "я", "ы", "а", "а", "о", "у"},
            new String[]{},
            new String[]{"б", "в", "г", "д", "к", "л", "п", "р", "с", "т", "ф", "х", "ц",
                    "б", "в", "г", "д", "к", "л", "п", "р", "с", "т", "ф", "х", "ц",
                    "б", "в", "г", "д", "к", "л", "п", "р", "с", "т", "ф",
                    "ж", "м", "н", "з", "ч", "ш", "щ",
                    "бр", "ск", "тр", "бл", "гл", "кр", "гр"},
            new String[]{"бл", "бр", "пл", "дж", "тр", "гл", "гр", "кр"},
            new String[]{"б", "в", "г", "д", "ж", "з", "к", "л", "м", "н", "п", "р", "с", "т", "ф", "х", "ц", "ч", "ш",
                    "в", "ф", "ск", "ск", "ск", "с", "б", "д", "д", "н", "р", "р"},
            new String[]{"одка", "одна", "уск", "аск", "ускы", "ад", "ар", "овйч", "ев", "ов", "оф", "агда", "ёцкы", "йч", "он", "ах", "ъв", "ян"},
            new String[]{}, new int[]{1, 2, 3, 4, 5, 6}, new double[]{4, 5, 6, 5, 3, 1}, 0.1, 0.2, 0.0, 0.12);



    /**
     * This is a very complicated constructor! Maybe look at the calls to this to initialize static members of this
     * class, LOVECRAFT and GREEK_ROMANIZED.
     * @param openingVowels String array where each element is a vowel or group of vowels that may appear at the start
     *                      of a word or in the middle; elements may be repeated to make them more common
     * @param midVowels String array where each element is a vowel or group of vowels that may appear in the
     *                      middle of the word; all openingVowels are automatically copied into this internally.
     *                      Elements may be repeated to make them more common
     * @param openingConsonants String array where each element is a consonant or consonant cluster that can appear
     *                          at the start of a word; elements may be repeated to make them more common
     * @param midConsonants String array where each element is a consonant or consonant cluster than can appear
     *                      between vowels; all closingConsonants are automatically copied into this internally.
     *                      Elements may be repeated to make them more common
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
    public FakeLanguageGen(String[] openingVowels, String[] midVowels, String[] openingConsonants,
                           String[] midConsonants, String[] closingConsonants, String[] closingSyllables, String[] vowelSplitters,
                           int[] syllableLengths, double[] syllableFrequencies, double vowelStartFrequency,
                           double vowelEndFrequency, double vowelSplitFrequency, double syllableEndFrequency) {
        this.openingVowels = openingVowels;
        this.midVowels = new String[openingVowels.length + midVowels.length];
        System.arraycopy(midVowels, 0, this.midVowels, 0, midVowels.length);
        System.arraycopy(openingVowels, 0, this.midVowels, midVowels.length, openingVowels.length);
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
            sb.append(rng.getRandomElement(openingVowels));
            sb.append(rng.getRandomElement(midConsonants));
        }
        else
        {
            sb.append(rng.getRandomElement(openingConsonants));
        }

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
            sb.append(rng.getRandomElement(midVowels));
            if (rng.nextDouble() < vowelSplitFrequency) {
                sb.append(rng.getRandomElement(vowelSplitters));
                sb.append(rng.getRandomElement(midVowels));
            }
            sb.append(rng.getRandomElement(midConsonants));
        }
        if (rng.nextDouble() < syllableEndFrequency) {
            sb.append(rng.getRandomElement(closingSyllables));
        } else {
            sb.append(rng.getRandomElement(midVowels));
            if (rng.nextDouble() < vowelSplitFrequency) {
                sb.append(rng.getRandomElement(vowelSplitters));
                sb.append(rng.getRandomElement(midVowels));
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
        return removeRepeats.matcher(sb.toString()).replaceAll("$1$2");
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

    private String[] merge1000(RNG r, String[] me, String[] other, double otherInfluence)
    {
        String[] ret = new String[1000];
        int otherCount = (int)(1000 * otherInfluence);
        int idx = 0;
        if(other.length > 0) {
            String[] tmp = r.shuffle(other);
            for (idx = 0; idx < otherCount; idx++) {
                ret[idx] = tmp[idx % tmp.length];
            }
        }
        if(me.length > 0) {
            String[] tmp = r.shuffle(me);
            for (; idx < 1000; idx++) {
                ret[idx] = tmp[idx % tmp.length];
            }
        }
        return ret;
    }

    public FakeLanguageGen mix(FakeLanguageGen other, double otherInfluence)
    {
        otherInfluence = Math.max(0.0, Math.min(otherInfluence, 1.0));
        double myInfluence = 1.0 - otherInfluence;
        RNG r = new RNG((hashCode() & 0xffffffffL) | ((other.hashCode() & 0xffffffffL) << 32)
                ^ Double.doubleToLongBits(otherInfluence));
        String[] ov = merge1000(r, openingVowels, other.openingVowels, otherInfluence),
                mv = merge1000(r, midVowels, other.midVowels, otherInfluence),
                oc = merge1000(r, openingConsonants, other.openingConsonants, otherInfluence),
                mc = merge1000(r, midConsonants, other.midConsonants, otherInfluence),
                cc = merge1000(r, closingConsonants, other.closingConsonants, otherInfluence),
                cs = merge1000(r, closingSyllables, other.closingSyllables, otherInfluence),
                splitters = merge1000(r, vowelSplitters, other.vowelSplitters, otherInfluence);

        LinkedHashMap<Integer, Double> freqs = new LinkedHashMap<Integer, Double>(syllableFrequencies);
        for(Map.Entry<Integer, Double> kv : other.syllableFrequencies.entrySet())
        {
            if(freqs.containsKey(kv.getKey()))
                freqs.put(kv.getKey(), kv.getValue() + freqs.get(kv.getKey()));
            else
                freqs.put(kv.getKey(), kv.getValue());
        }
        int[] lens = new int[freqs.size()];
        double[] odds = new double[freqs.size()];
        int i = 0;
        for(Map.Entry<Integer, Double> kv : freqs.entrySet())
        {
            lens[i] = kv.getKey();
            odds[i++] = kv.getValue();
        }
        return new FakeLanguageGen(ov, mv, oc, mc, cc, cs, splitters, lens, odds,
                vowelStartFrequency * myInfluence + other.vowelStartFrequency * otherInfluence,
                vowelEndFrequency * myInfluence + other.vowelEndFrequency * otherInfluence,
                vowelSplitFrequency * myInfluence + other.vowelSplitFrequency * otherInfluence,
                syllableEndFrequency * myInfluence + other.syllableEndFrequency * otherInfluence);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FakeLanguageGen that = (FakeLanguageGen) o;

        if (Double.compare(that.totalSyllableFrequency, totalSyllableFrequency) != 0) return false;
        if (Double.compare(that.vowelStartFrequency, vowelStartFrequency) != 0) return false;
        if (Double.compare(that.vowelEndFrequency, vowelEndFrequency) != 0) return false;
        if (Double.compare(that.vowelSplitFrequency, vowelSplitFrequency) != 0) return false;
        if (Double.compare(that.syllableEndFrequency, syllableEndFrequency) != 0) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(openingVowels, that.openingVowels)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(midVowels, that.midVowels)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(openingConsonants, that.openingConsonants)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(midConsonants, that.midConsonants)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(closingConsonants, that.closingConsonants)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(vowelSplitters, that.vowelSplitters)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(closingSyllables, that.closingSyllables)) return false;
        return syllableFrequencies.equals(that.syllableFrequencies);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = Arrays.hashCode(openingVowels);
        result = 31 * result + Arrays.hashCode(midVowels);
        result = 31 * result + Arrays.hashCode(openingConsonants);
        result = 31 * result + Arrays.hashCode(midConsonants);
        result = 31 * result + Arrays.hashCode(closingConsonants);
        result = 31 * result + Arrays.hashCode(vowelSplitters);
        result = 31 * result + Arrays.hashCode(closingSyllables);
        result = 31 * result + syllableFrequencies.hashCode();
        temp = Double.doubleToLongBits(totalSyllableFrequency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(vowelStartFrequency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(vowelEndFrequency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(vowelSplitFrequency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(syllableEndFrequency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "FakeLanguageGen{" +
                "openingVowels=" + Arrays.toString(openingVowels) +
                ", midVowels=" + Arrays.toString(midVowels) +
                ", openingConsonants=" + Arrays.toString(openingConsonants) +
                ", midConsonants=" + Arrays.toString(midConsonants) +
                ", closingConsonants=" + Arrays.toString(closingConsonants) +
                ", vowelSplitters=" + Arrays.toString(vowelSplitters) +
                ", closingSyllables=" + Arrays.toString(closingSyllables) +
                ", syllableFrequencies=" + syllableFrequencies +
                ", totalSyllableFrequency=" + totalSyllableFrequency +
                ", vowelStartFrequency=" + vowelStartFrequency +
                ", vowelEndFrequency=" + vowelEndFrequency +
                ", vowelSplitFrequency=" + vowelSplitFrequency +
                ", syllableEndFrequency=" + syllableEndFrequency +
                '}';
    }
}
