package squidpony;

import regexodus.*;
import squidpony.squidmath.*;

import java.io.Serializable;
import java.util.*;

/**
 * A text generator for producing sentences and/or words in nonsense languages that fit a theme. This does not use an
 * existing word list as a basis for its output, so it may or may not produce existing words occasionally, but you can
 * safely assume it won't generate a meaningful sentence except in the absolute unlikeliest of cases.
 * Created by Tommy Ettinger on 11/29/2015.
 *
 * @author Tommy Ettinger
 */

public class FakeLanguageGen implements Serializable {
    private static final long serialVersionUID = -2396642435461186352L;
    public final String[] openingVowels, midVowels, openingConsonants, midConsonants, closingConsonants,
            vowelSplitters, closingSyllables;
    public boolean clean;
    public final IntDoubleOrderedMap syllableFrequencies;
    protected double totalSyllableFrequency = 0.0;
    public final double vowelStartFrequency, vowelEndFrequency, vowelSplitFrequency, syllableEndFrequency;
    public final Pattern[] sanityChecks;
    public ArrayList<Modifier> modifiers;
    public static final StatefulRNG srng = new StatefulRNG();
    static final Pattern repeats = Pattern.compile("(.)\\1+"),
            vowelClusters = Pattern.compile(
                    "[àáâãäåæāăąǻǽaèéêëēĕėęěeìíîïĩīĭįıiòóôõöøōŏőœǿoùúûüũūŭůűųuýÿŷỳyαοειυаеёийоуъыэюя]+",
                    REFlags.IGNORE_CASE | REFlags.UNICODE),
            consonantClusters = Pattern.compile(
                    "[bcçćĉċčdþðďđfgĝğġģhĥħjĵȷkķlĺļľŀłmnñńņňŋpqrŕŗřsśŝşšștţťțvwŵẁẃẅxyýÿŷỳzźżžṛṝḷḹḍṭṅṇṣṃḥρσζτκχνθμπψβλγφξςбвгдклпрстфхцжмнзчшщ]+",
                    REFlags.IGNORE_CASE | REFlags.UNICODE);
    //latin
    //àáâãäåæāăąǻǽaèéêëēĕėęěeìíîïĩīĭįıiòóôõöøōŏőœǿoùúûüũūŭůűųuýÿŷỳybcçćĉċčdþðďđfgĝğġģhĥħjĵȷkķlĺļľŀłmnñńņňŋpqrŕŗřsśŝşšștţťțvwŵẁẃẅxyýÿŷỳzźżžṛṝḷḹḍṭṅṇṣṃḥ
    //ÀÁÂÃÄÅÆĀĂĄǺǼAÈÉÊËĒĔĖĘĚEÌÍÎÏĨĪĬĮIIÒÓÔÕÖØŌŎŐŒǾOÙÚÛÜŨŪŬŮŰŲUÝŸŶỲYBCÇĆĈĊČDÞÐĎĐFGĜĞĠĢHĤĦJĴȷKĶLĹĻĽĿŁMNÑŃŅŇŊPQRŔŖŘSŚŜŞŠȘTŢŤȚVWŴẀẂẄXYÝŸŶỲZŹŻŽṚṜḶḸḌṬṄṆṢṂḤ
    //greek
    //αοειυρσζτκχνθμπψβλγφξς
    //ΑΟΕΙΥΡΣΖΤΚΧΝΘΜΠΨΒΛΓΦΞΣ
    //cyrillic
    //аеёийоуъыэюябвгдклпрстфхцжмнзчшщ
    //АЕЁИЙОУЪЫЭЮЯБВГДКЛПРСТФХЦЖМНЗЧШЩ

    static final Pattern[]
            vulgarChecks = new Pattern[]
            {
                    //17 is REFlags.UNICODE | REFlags.IGNORE_CASE
                    Pattern.compile("[sξζzkкκcсς][hнlι].{1,3}[dtтτΓг]", 17),
                    Pattern.compile("(?:(?:[pрρ][hн])|[fd]).{1,3}[kкκcсςxхжχq]", 17), // lots of these end in a 'k' sound, huh
                    Pattern.compile("[kкκcсςСQq].{1,3}[kкκcсςxхжχqmм]", 17),
                    Pattern.compile("[bъыбвβЪЫБ].{1,3}[cсς]", 17),
                    Pattern.compile("[hн][^aаαΛeезξεЗΣiτιyуλγУ][^aаαΛeезξεЗΣiτιyуλγУ]?[rяΓ]", 17),
                    Pattern.compile("[tтτΓгcсς]..?[tтτΓг]", 17),
                    Pattern.compile("(?:(?:[pрρ][hн])|[f])..?[rяΓ][tтτΓг]", 17),
                    Pattern.compile("[Ssξζzcсς][hн][iτιyуλγУ].?[sξζzcсς]", 17),
                    Pattern.compile("[aаαΛ][nи]..?[Ssξlιζz]", 17),
                    Pattern.compile("[aаαΛ]([sξζz]{2})", 17),
                    Pattern.compile("[uμυνv]([hн]?)[nи]+[tтτΓг]", 17),
                    Pattern.compile("[nиfvν]..?[jg]", 17), // might as well remove two possible slurs and a body part with one check
                    Pattern.compile("[pрρ](?:(?:([eезξεЗΣoоюσοuμυνv])\\1)|(?:[eезξεЗΣiτιyуλγУuμυνv]+[sξζzcсς]))", 17), // the grab bag of juvenile words
                    Pattern.compile("[mм][hнwψшщ]?..?[rяΓ].?d", 17), // should pick up the #1 obscenity from Spanish and French
                    Pattern.compile("[g][hн]?[aаαАΑΛeеёзξεЕЁЗΕΣ][yуλγУeеёзξεЕЁЗΕΣ]", 17), // could be inappropriate for random text
                    Pattern.compile("[wψшщuμυνv](?:[hн]?)[aаαΛeеёзξεЗΕΣoоюσοuμυνv](?:[nи]+)[gkкκcсςxхжχq]", 17)
            },
            genericSanityChecks = new Pattern[]
                    {
                            Pattern.compile("[AEIOUaeiou]{3}"),
                            Pattern.compile("(\\p{L})\\1\\1"),
                            Pattern.compile("[Uu][uoj]"),
                            Pattern.compile("[Ii][iyqhl]"),
                            Pattern.compile("[Yy]([aiu])\\1"),
                            Pattern.compile("[Rr][aeiouy]+[rh]"),
                            Pattern.compile("[Qq]u[yu]"),
                            Pattern.compile("[^oaei]uch"),
                            Pattern.compile("[Hh][tcszi]?h"),
                            Pattern.compile("[Tt]t[^aeiouy]{2}"),
                            Pattern.compile("[Yy]h([^aeiouy]|$)"),
                            Pattern.compile("([dbvcxqjky])\\1$"),
                            Pattern.compile("[szSZrlRL][^aeiou][rlsz]"),
                            Pattern.compile("[UIuiYy][wy]"),
                            Pattern.compile("^[UIui][ae]")
                    },
            englishSanityChecks = new Pattern[]
                    {
                            Pattern.compile("[AEIOUaeiou]{3}"),
                            Pattern.compile("(\\w)\\1\\1"),
                            Pattern.compile("(.)\\1(.)\\2"),
                            Pattern.compile("[Aa][ae]"),
                            Pattern.compile("[Uu][umlkj]"),
                            Pattern.compile("[Ii][iyqkhrl]"),
                            Pattern.compile("[Oo][c]"),
                            Pattern.compile("[Yy]([aiu])\\1"),
                            Pattern.compile("[Rr][aeiouy]+[rh]"),
                            Pattern.compile("[Qq]u[yu]"),
                            Pattern.compile("[^oaei]uch"),
                            Pattern.compile("[Hh][tcszi]?h"),
                            Pattern.compile("[Tt]t[^aeiouy]{2}"),
                            Pattern.compile("[Yy]h([^aeiouy]|$)"),
                            Pattern.compile("[szSZrlRL][^aeiou][rlsz]"),
                            Pattern.compile("[UIuiYy][wy]"),
                            Pattern.compile("^[UIui][ae]"),
                            Pattern.compile("q$")
                    },
            japaneseSanityChecks = new Pattern[]
                    {
                            Pattern.compile("[AEIOUaeiou]{3}"),
                            Pattern.compile("(\\w)\\1\\1"),
                            Pattern.compile("[Tt]s[^u]"),
                            Pattern.compile("[Ff][^u]"),
                            Pattern.compile("[Yy][^auo]"),
                            Pattern.compile("[Tt][ui]"),
                            Pattern.compile("[SsZzDd]i"),
                            Pattern.compile("[Hh]u"),
                    },
            arabicSanityChecks = new Pattern[]
                    {
                            Pattern.compile("(\\w)\\1\\1"),
                            Pattern.compile("-[^aeiou]{2}"),
                    };
    static final Replacer[]
            accentFinders = new Replacer[]
            {
                    Pattern.compile("[àáâãäåæāăąǻǽ]").replacer("a"),
                    Pattern.compile("[èéêëēĕėęě]").replacer("e"),
                    Pattern.compile("[ìíîïĩīĭįı]").replacer("i"),
                    Pattern.compile("[òóôõöøōŏőœǿ]").replacer("o"),
                    Pattern.compile("[ùúûüũūŭůűų]").replacer("u"),
                    Pattern.compile("[ÀÁÂÃÄÅÆĀĂĄǺǼ]").replacer("A"),
                    Pattern.compile("[ÈÉÊËĒĔĖĘĚ]").replacer("E"),
                    Pattern.compile("[ÌÍÎÏĨĪĬĮI]").replacer("I"),
                    Pattern.compile("[ÒÓÔÕÖØŌŎŐŒǾ]").replacer("O"),
                    Pattern.compile("[ÙÚÛÜŨŪŬŮŰŲ]").replacer("U"),
                    Pattern.compile("Ё").replacer("Е"),
                    Pattern.compile("Й").replacer("И"),
                    Pattern.compile("[çćĉċč]").replacer("c"),
                    Pattern.compile("[þðďđḍ]").replacer("d"),
                    Pattern.compile("[ĝğġģ]").replacer("g"),
                    Pattern.compile("[ĥħḥ]").replacer("h"),
                    Pattern.compile("[ĵȷ]").replacer("j"),
                    Pattern.compile("ķ").replacer("k"),
                    Pattern.compile("[ĺļľŀłḷḹļ]").replacer("l"),
                    Pattern.compile("ṃ").replacer("m"),
                    Pattern.compile("[ñńņňŋṅṇ]").replacer("n"),
                    Pattern.compile("[ŕŗřṛṝŗŕ]").replacer("r"),
                    Pattern.compile("[śŝşšșṣ]").replacer("s"),
                    Pattern.compile("[ţťŧțṭ]").replacer("t"),
                    Pattern.compile("[ŵẁẃẅ]").replacer("w"),
                    Pattern.compile("[ýÿŷỳ]").replacer("y"),
                    Pattern.compile("[źżž]").replacer("z"),
                    Pattern.compile("[ÇĆĈĊČ]").replacer("C"),
                    Pattern.compile("[ÞÐĎĐḌ]").replacer("D"),
                    Pattern.compile("[ĜĞĠĢ]").replacer("G"),
                    Pattern.compile("[ĤĦḤ]").replacer("H"),
                    Pattern.compile("Ĵ").replacer("J"),
                    Pattern.compile("Ķ").replacer("K"),
                    Pattern.compile("[ĹĻĽĿŁḶḸĻ]").replacer("L"),
                    Pattern.compile("Ṃ").replacer("M"),
                    Pattern.compile("[ÑŃŅŇŊṄṆ]").replacer("N"),
                    Pattern.compile("[ŔŖŘṚṜŖŔ]").replacer("R"),
                    Pattern.compile("[ŚŜŞŠȘṢ]").replacer("S"),
                    Pattern.compile("[ŢŤŦȚṬ]").replacer("T"),
                    Pattern.compile("[ŴẀẂẄ]").replacer("W"),
                    Pattern.compile("[ÝŸŶỲ]").replacer("Y"),
                    Pattern.compile("[ŹŻŽ]").replacer("Z"),
                    Pattern.compile("ё").replacer("е"),
                    Pattern.compile("й").replacer("и"),
            };

    static final char[][] accentedVowels = new char[][]{
            new char[]{
                    'a', 'à', 'á', 'â', 'ä', 'ā', 'ă', 'ã', 'å', 'æ', 'ą', 'ǻ', 'ǽ'
            },
            new char[]{
                    'e', 'è', 'é', 'ê', 'ë', 'ē', 'ĕ', 'ė', 'ę', 'ě'
            },
            new char[]{
                    'i', 'ì', 'í', 'î', 'ï', 'ī', 'ĭ', 'ĩ', 'į', 'ı',
            },
            new char[]{
                    'o', 'ò', 'ó', 'ô', 'ö', 'ō', 'ŏ', 'õ', 'ø', 'ő', 'œ', 'ǿ'
            },
            new char[]{
                    'u', 'ù', 'ú', 'û', 'ü', 'ū', 'ŭ', 'ũ', 'ů', 'ű', 'ų'
            }
    },
            accentedConsonants = new char[][]
                    {
                            new char[]{
                                    'b'
                            },
                            new char[]{
                                    'c', 'ç', 'ć', 'ĉ', 'ċ', 'č',
                            },
                            new char[]{
                                    'd', 'þ', 'ð', 'ď', 'đ',
                            },
                            new char[]{
                                    'f'
                            },
                            new char[]{
                                    'g', 'ĝ', 'ğ', 'ġ', 'ģ',
                            },
                            new char[]{
                                    'h', 'ĥ', 'ħ',
                            },
                            new char[]{
                                    'j', 'ĵ', 'ȷ',
                            },
                            new char[]{
                                    'k', 'ķ',
                            },
                            new char[]{
                                    'l', 'ĺ', 'ļ', 'ľ', 'ŀ', 'ł',
                            },
                            new char[]{
                                    'm',
                            },
                            new char[]{
                                    'n', 'ñ', 'ń', 'ņ', 'ň', 'ŋ',
                            },
                            new char[]{
                                    'p',
                            },
                            new char[]{
                                    'q',
                            },
                            new char[]{
                                    'r', 'ŕ', 'ŗ', 'ř',
                            },
                            new char[]{
                                    's', 'ś', 'ŝ', 'ş', 'š', 'ș',
                            },
                            new char[]{
                                    't', 'ţ', 'ť', 'ț',
                            },
                            new char[]{
                                    'v',
                            },
                            new char[]{
                                    'w', 'ŵ', 'ẁ', 'ẃ', 'ẅ',
                            },
                            new char[]{
                                    'x',
                            },
                            new char[]{
                                    'y', 'ý', 'ÿ', 'ŷ', 'ỳ',
                            },
                            new char[]{
                                    'z', 'ź', 'ż', 'ž',
                            },
                    };

    /*
     * Removes accented characters from a string; if the "base" characters are non-English anyway then the result won't
     * be an ASCII string, but otherwise it probably will be.
     * <br>
     * Credit to user hashable from http://stackoverflow.com/a/1215117
     *
     * @param str a string that may contain accented characters
     * @return a string with all accented characters replaced with their (possibly ASCII) counterparts
     *
    public String removeAccents(String str) {
        String alteredString = Normalizer.normalize(str, Normalizer.Form.NFD);
        alteredString = diacritics.matcher(alteredString).replaceAll("");
        alteredString = alteredString.replace('æ', 'a');
        alteredString = alteredString.replace('œ', 'o');
        alteredString = alteredString.replace('Æ', 'A');
        alteredString = alteredString.replace('Œ', 'O');
        return alteredString;
    }*/

    /**
     * Removes accented Latin-script characters from a string; if the "base" characters are non-English anyway then the
     * result won't be an ASCII string, but otherwise it probably will be.
     *
     * @param str a string that may contain accented Latin-script characters
     * @return a string with all accented characters replaced with their (possibly ASCII) counterparts
     */
    public static CharSequence removeAccents(CharSequence str) {
        CharSequence alteredString = str;
        for (int i = 0; i < accentFinders.length; i++) {
            alteredString = accentFinders[i].replace(alteredString);
        }
        return alteredString;
    }


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
            new String[]{"'", "-"}, new int[]{1, 2, 3}, new double[]{6, 7, 2}, 0.4, 0.31, 0.07, 0.04, null, true);
    /**
     * Imitation English; may seem closer to Dutch in some generated text, and is not exactly the best imitation.
     * Should seem pretty fake to many readers; does not filter out dictionary words but does perform basic vulgarity
     * filtering. If you want to avoid generating other words, you can subclass FakeLanguageGen and modify word() .
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
                    "b", "c", "d", "f", "g", "k", "l", "m", "n", "p", "r", "s", "t", "w",
            },
            new String[]{"ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                    "ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                    "ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                    "ay", "ay", "ey", "oy", "ay", "ay", "ey", "oy",
                    "ough", "aught", "ant", "ont", "oe", "ance", "ell", "eal", "oa", "urt", "ut", "iom", "ion", "ion", "ision", "ation", "ation", "ition",
                    "ough", "aught", "ant", "ont", "oe", "ance", "ell", "eal", "oa", "urt", "ut", "iom", "ion", "ion", "ision", "ation", "ation", "ition",
                    "ily", "ily", "ily", "adly", "owly", "oorly", "ardly", "iedly",
            },
            new String[]{}, new int[]{1, 2, 3, 4}, new double[]{10, 11, 4, 1}, 0.22, 0.1, 0.0, 0.22, englishSanityChecks, true);
    /**
     * Imitation ancient Greek, romanized to use the Latin alphabet. Likely to seem pretty fake to many readers.
     * <br>
     * Psuilas alor; aipeomarta le liaspa...
     */
    public static final FakeLanguageGen GREEK_ROMANIZED = new FakeLanguageGen(
            new String[]{"a", "a", "a", "a", "a", "o", "o", "e", "e", "e", "i", "i", "i", "i", "i", "au", "ai", "ai", "oi", "oi",
                    "ia", "io", "u", "u", "eo", "ei", "o", "o", "ou", "oi", "y", "y", "y", "y"},
            new String[]{"ui", "ui", "ei", "ei"},
            new String[]{"rh", "s", "z", "t", "t", "k", "ch", "n", "th", "kth", "m", "p", "ps", "b", "l", "kr",
                    "g", "phth", "d", "t", "k", "ch", "n", "ph", "ph", "k",},
            new String[]{"lph", "pl", "l", "l", "kr", "nch", "nx", "ps"},
            new String[]{"s", "p", "t", "ch", "n", "m", "s", "p", "t", "ch", "n", "m", "b", "g", "st", "rst",
                    "rt", "sp", "rk", "ph", "x", "z", "nk", "ng", "th", "d", "k", "n", "n",},
            new String[]{"os", "os", "os", "is", "is", "us", "um", "eum", "ium", "iam", "us", "um", "es",
                    "anes", "eros", "or", "or", "ophon", "on", "on", "ikon", "otron", "ik",},
            new String[]{}, new int[]{1, 2, 3, 4}, new double[]{5, 7, 4, 1}, 0.45, 0.45, 0.0, 0.3, null, true);
    /**
     * Imitation ancient Greek, using the original Greek alphabet. People may try to translate it and get gibberish.
     * Make sure the font you use to render this supports the Greek alphabet! In the GDX display module, the "smooth"
     * fonts support all the Greek you need for this.
     * <br>
     * Ψυιλασ αλορ; αιπεομαρτα λε λιασπα...
     */
    public static final FakeLanguageGen GREEK_AUTHENTIC = new FakeLanguageGen(
            new String[]{"α", "α", "α", "α", "α", "ο", "ο", "ε", "ε", "ε", "ι", "ι", "ι", "ι", "ι", "αυ", "αι", "αι", "οι", "οι",
                    "ια", "ιο", "ου", "ου", "εο", "ει", "ω", "ω", "ωυ", "ωι", "υ", "υ", "υ", "υ"},
            new String[]{"υι", "υι", "ει", "ει"},
            new String[]{"ρ", "σ", "ζ", "τ", "τ", "κ", "χ", "ν", "θ", "κθ", "μ", "π", "ψ", "β", "λ", "κρ",
                    "γ", "φθ", "δ", "τ", "κ", "χ", "ν", "φ", "φ", "κ",},
            new String[]{"λφ", "πλ", "λ", "λ", "κρ", "γχ", "γξ", "ψ"},
            new String[]{"σ", "π", "τ", "χ", "ν", "μ", "σ", "π", "τ", "χ", "ν", "μ", "β", "γ", "στ", "ρστ",
                    "ρτ", "σπ", "ρκ", "φ", "ξ", "ζ", "γκ", "γγ", "θ", "δ", "κ", "ν", "ν",},
            new String[]{"ος", "ος", "ος", "ις", "ις", "υς", "υμ", "ευμ", "ιυμ", "ιαμ", "υς", "υμ", "ες",
                    "ανες", "ερος", "ορ", "ορ", "οφον", "ον", "ον", "ικον", "οτρον", "ικ",},
            new String[]{}, new int[]{1, 2, 3, 4}, new double[]{5, 7, 4, 1}, 0.45, 0.45, 0.0, 0.3, null, true);

    /**
     * Imitation modern French, using (too many of) the accented vowels that are present in the language. Translating it
     * will produce gibberish if it produces anything at all. In the GDX display module, the "smooth" and "unicode"
     * fonts support all the accented characters you need for this.
     * <br><br>
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
                    "oi", "oie", "oï", "ou", "oû", "oy", "u", "û", "ue",
                    "a", "a", "a", "e", "e", "e", "i", "i", "o", "u", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                    "a", "a", "e", "e", "i", "o", "a", "a", "a", "e", "e", "e", "i", "i", "o",
            },
            new String[]{"tr", "ch", "m", "b", "b", "br", "j", "j", "j", "j", "g", "t", "t", "t", "c", "d", "f", "f", "h", "n", "l", "l",
                    "s", "s", "s", "r", "r", "r", "v", "v", "p", "pl", "pr", "bl", "br", "dr", "gl", "gr"},
            new String[]{"cqu", "gu", "qu", "rqu", "nt", "ng", "ngu", "mb", "ll", "nd", "ndr", "nct", "st",
                    "xt", "mbr", "pl", "g", "gg", "ggr", "gl",
                    "m", "m", "mm", "v", "v", "f", "f", "f", "ff", "b", "b", "bb", "d", "d", "dd", "s", "s", "s", "ss", "ss", "ss",
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
            new String[]{}, new int[]{1, 2, 3}, new double[]{18, 7, 2}, 0.35, 1.0, 0.0, 0.55, null, true);

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
            new String[]{}, new int[]{1, 2, 3, 4, 5, 6}, new double[]{4, 5, 6, 5, 3, 1}, 0.1, 0.2, 0.0, 0.12, englishSanityChecks, true);


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
            new String[]{}, new int[]{1, 2, 3, 4, 5, 6}, new double[]{4, 5, 6, 5, 3, 1}, 0.1, 0.2, 0.0, 0.12, null, true);

    /**
     * Imitation Japanese, romanized to use the Latin alphabet. Likely to seem pretty fake to many readers.
     * <br>
     * Narurehyounan nikase keho...
     */
    public static final FakeLanguageGen JAPANESE_ROMANIZED = new FakeLanguageGen(
            new String[]{"a", "a", "a", "a", "e", "e", "i", "i", "i", "i", "o", "o", "o", "u", "ou", "u", "ai", "ai"},
            new String[]{},
            new String[]{"k", "ky", "s", "sh", "t", "ts", "ch", "n", "ny", "h", "f", "hy", "m", "my", "y", "r", "ry", "g",
                    "gy", "z", "j", "d", "b", "by", "p", "py",
                    "k", "t", "n", "s", "k", "t", "d", "s", "sh", "sh", "g", "r", "b",
                    "k", "t", "n", "s", "k", "t", "b", "s", "sh", "sh", "g", "r", "b",
                    "k", "t", "n", "s", "k", "t", "z", "s", "sh", "sh", "ch", "ry", "ts"
            },
            new String[]{"k", "ky", "s", "sh", "t", "ts", "ch", "n", "ny", "h", "f", "hy", "m", "my", "y", "r", "ry", "g",
                    "gy", "z", "j", "d", "b", "by", "p", "py",
                    "k", "t", "d", "s", "k", "t", "d", "s", "sh", "sh", "y", "j", "p", "r", "d",
                    "k", "t", "b", "s", "k", "t", "b", "s", "sh", "sh", "y", "j", "p", "r", "d",
                    "k", "t", "z", "s", "f", "g", "z", "b", "d", "ts",
                    "nn", "nn", "nn", "nd", "nz", "mm", "kk", "kk", "tt", "ss", "ssh", "tch"},
            new String[]{"n"},
            new String[]{},
            new String[]{}, new int[]{1, 2, 3, 4, 5}, new double[]{5, 4, 5, 4, 3}, 0.3, 0.9, 0.0, 0.0, japaneseSanityChecks, true);

    /**
     * Swahili is one of the more commonly-spoken languages in sub-Saharan Africa, and serves mainly as a shared language
     * that is often learned after becoming fluent in one of many other (vaguely-similar) languages of the area. An
     * example sentence in Swahili, that this might try to imitate aesthetically, is "Mtoto mdogo amekisoma," meaning
     * "The small child reads it" (where it is a book). A notable language feature used here is the redoubling of words,
     * which is used in Swahili to emphasize or alter the meaning of the doubled word; here, it always repeats exactly
     * and can't make minor changes like a real language might. This generates things like "gata-gata", "hapi-hapi", and
     * "mimamzu-mimamzu", always separating with a hyphen here.
     * <br>
     * As an aside, please try to avoid the ugly stereotypes that fantasy media often assigns to speakers of African-like
     * languages when using this or any of the generators. Many fantasy tropes come from older literature written with
     * major cultural biases, and real-world cultural elements can be much more interesting to players than yet another
     * depiction of a "jungle savage" with stereotypical traits. Consider drawing from existing lists of real-world
     * technological discoveries, like https://en.wikipedia.org/wiki/History_of_science_and_technology_in_Africa , for
     * inspiration when world-building; though some groups may not have developed agriculture by early medieval times,
     * their neighbors may be working iron and studying astronomy just a short distance away.
     * <br>
     * Kondueyu; ma mpiyamdabota mise-mise nizakwaja alamsa amja, homa nkajupomba.
     */
    public static final FakeLanguageGen SWAHILI = new FakeLanguageGen(
            new String[]{"a", "i", "o", "e", "u",
                    "a", "a", "i", "o", "o", "e", "u",
                    "a", "a", "i", "o", "o", "u",
                    "a", "a", "i", "i", "o",
                    "a", "a", "a", "a", "a",
                    "a", "i", "o", "e", "u",
                    "a", "a", "i", "o", "o", "e", "u",
                    "a", "a", "i", "o", "o", "u",
                    "a", "a", "i", "i", "o",
                    "a", "a", "a", "a", "a",
                    "aa", "aa", "ue", "uo", "ii", "ea"},
            new String[]{},
            new String[]{
                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "nb", "nj", "ns", "nz",
                    "nb", "nch", "nj", "ns", "ny", "nz",
                    "nb", "nch", "nf", "ng", "nj", "nk", "np", "ns", "nz",
                    "nb", "nch", "nd", "nf", "ng", "nj", "nk", "np", "ns", "nt", "nz",
                    "nb", "nch", "nd", "nf", "ng", "nj", "nk", "np", "ns", "nt", "nv", "nw", "nz",

                    "mb", "ms", "my", "mz",
                    "mb", "mch", "ms", "my", "mz",
                    "mb", "mch", "mk", "mp", "ms", "my", "mz",
                    "mb", "mch", "md", "mk", "mp", "ms", "mt", "my", "mz",
                    "mb", "mch", "md", "mf", "mg", "mj", "mk", "mp", "ms", "mt", "mv", "mw", "my", "mz",
                    "sh", "sh", "sh", "ny", "kw",
                    "dh", "th", "sh", "ny",
                    "dh", "th", "sh", "gh", "r", "ny",
                    "dh", "th", "sh", "gh", "r", "ny",
            },
            new String[]{
                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "b", "h", "j", "l", "s", "y", "m", "n",
                    "b", "ch", "h", "j", "l", "s", "y", "z", "m", "n",
                    "b", "ch", "f", "g", "h", "j", "k", "l", "p", "s", "y", "z", "m", "n",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "y", "z", "m", "n", "kw",
                    "b", "ch", "d", "f", "g", "h", "j", "k", "l", "p", "s", "t", "v", "w", "y", "z", "m", "n", "kw",

                    "nb", "nj", "ns", "nz",
                    "nb", "nch", "nj", "ns", "ny", "nz",
                    "nb", "nch", "nf", "ng", "nj", "nk", "np", "ns", "nz",
                    "nb", "nch", "nd", "nf", "ng", "nj", "nk", "np", "ns", "nt", "nz",
                    "nb", "nch", "nd", "nf", "ng", "nj", "nk", "np", "ns", "nt", "nw", "nz",

                    "mb", "ms", "my", "mz",
                    "mb", "mch", "ms", "my", "mz",
                    "mb", "mch", "mk", "mp", "ms", "my", "mz",
                    "mb", "mch", "md", "mk", "mp", "ms", "mt", "my", "mz",
                    "mb", "mch", "md", "mf", "mg", "mj", "mk", "mp", "ms", "mt", "mw", "my", "mz",
                    "sh", "sh", "sh", "ny", "kw",
                    "dh", "th", "sh", "ny",
                    "dh", "th", "sh", "gh", "r", "ny",
                    "dh", "th", "sh", "gh", "r", "ny",
                    "ng", "ng", "ng", "ng", "ng"
            },
            new String[]{""},
            new String[]{"-@"},
            new String[]{}, new int[]{1, 2, 3, 4, 5, 6}, new double[]{3, 8, 6, 9, 2, 2}, 0.2, 1.0, 0.0, 0.12, null, true);

    /**
     * Imitation Somali, using the Latin alphabet. Due to uncommon word structure, unusual allowed combinations of
     * letters, and no common word roots with most familiar languages, this may seem like an unidentifiable or "alien"
     * language to most readers. However, it's based on the Latin writing system for the Somali language (probably
     * closest to the northern dialect), which due to the previously mentioned properties, makes it especially good for
     * mixing with other languages to make letter combinations that seem strange to appear. It is unlikely that this
     * particular generated language style will be familiar to readers, so it probably won't have existing stereotypes
     * associated with the text. One early comment this received was, "it looks like a bunch of letters semi-randomly
     * thrown together", which is probably a typical response (the comment was made by someone fluent in German and
     * English, and most Western European languages are about as far as you can get from Somali).
     * <br>
     * Libor cat naqoxekh dhuugad gisiqir?
     */
    public static final FakeLanguageGen SOMALI = new FakeLanguageGen(
            new String[]{"a", "a", "a", "a", "a", "a", "a", "aa", "aa", "aa",
                    "e", "e", "ee",
                    "i", "i", "i", "i", "ii",
                    "o", "o", "o", "oo",
                    "u", "u", "u", "uu", "uu",
            },
            new String[]{},
            new String[]{"b", "t", "j", "x", "kh", "d", "r", "s", "sh", "dh", "c", "g", "f", "q", "k", "l", "m",
                    "n", "w", "h", "y",
                    "x", "g", "b", "d", "s", "m", "dh", "n", "r",
                    "g", "b", "s", "dh",
            },
            new String[]{
                    "bb", "gg", "dd", "bb", "dd", "rr", "ddh", "cc", "gg", "ff", "ll", "mm", "nn",
                    "bb", "gg", "dd", "bb", "dd", "gg",
                    "bb", "gg", "dd", "bb", "dd", "gg",
                    "cy", "fk", "ft", "nt", "rt", "lt", "qm", "rdh", "rsh", "lq",
                    "my", "gy", "by", "lkh", "rx", "md", "bd", "dg", "fd", "mf",
                    "dh", "dh", "dh", "dh",
            },
            new String[]{
                    "b", "t", "j", "x", "kh", "d", "r", "s", "sh", "c", "g", "f", "q", "k", "l", "m", "n", "h",
                    "x", "g", "b", "d", "s", "m", "q", "n", "r",
                    "b", "t", "j", "x", "kh", "d", "r", "s", "sh", "c", "g", "f", "q", "k", "l", "m", "n", "h",
                    "x", "g", "b", "d", "s", "m", "q", "n", "r",
                    "b", "t", "j", "x", "kh", "d", "r", "s", "sh", "c", "g", "f", "q", "k", "l", "m", "n",
                    "g", "b", "d", "s", "q", "n", "r",
                    "b", "t", "x", "kh", "d", "r", "s", "sh", "g", "f", "q", "k", "l", "m", "n",
                    "g", "b", "d", "s", "r", "n",
                    "b", "t", "kh", "d", "r", "s", "sh", "g", "f", "q", "k", "l", "m", "n",
                    "g", "b", "d", "s", "r", "n",
                    "b", "t", "d", "r", "s", "sh", "g", "f", "q", "k", "l", "m", "n",
                    "g", "b", "d", "s", "r", "n",
            },
            new String[]{"aw", "ow", "ay", "ey", "oy", "ay", "ay"},
            new String[]{}, new int[]{1, 2, 3, 4, 5}, new double[]{5, 4, 5, 4, 1}, 0.25, 0.3, 0.0, 0.08, null, true);
    /**
     * Imitation Hindi, romanized to use the Latin alphabet using accented glyphs similar to the IAST standard.
     * Most fonts do not support the glyphs that IAST-standard romanization of Hindi needs, so this uses alternate
     * glyphs from at most Latin Extended-A. Relative to HINDI_IAST, also defined here, the IAST standard glyphs
     * {@code "ṛṝḷḹḍṭṅṇṣṃḥ"} become {@code "ŗŕļĺđţńņşĕĭ"}, with the nth glyph in the first string being substituted
     * with the nth glyph in the second string. This version of imitation Hindi is preferred over the IAST kind because
     * font support is much better for the glyphs this version uses.
     * <br>
     * Darvāga yar; ghađhinopŕauka āĕrdur, conśaigaijo śabhodhaĕđū jiviđaudu.
     */
    public static final FakeLanguageGen HINDI_ROMANIZED = new FakeLanguageGen(
            new String[]{
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "u", "ū", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "u", "ū", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "u", "ū", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "u", "ū", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "aĕ", "aĕ", "aĕ", "aĕ", "aĕ", "āĕ", "āĕ", "iĕ", "iĕ", "iĕ", "īĕ", "īĕ",
                    "uĕ", "uĕ", "ūĕ", "aiĕ", "aiĕ", "oĕ", "oĕ", "oĕ", "auĕ",
                    //"aĭ", "aĭ", "aĭ", "aĭ", "aĭ", "āĭ", "āĭ", "iĭ", "iĭ", "iĭ", "īĭ", "īĭ",
                    //"uĭ", "uĭ", "ūĭ", "aiĭ", "aiĭ", "oĭ", "oĭ", "oĭ", "auĭ",
            },
            new String[]{"á", "í", "ú", "ó", "á", "í", "ú", "ó",
            },
            new String[]{
                    "k", "k", "k", "k", "k", "k", "k", "k", "kŗ", "kŕ", "kļ",
                    "c", "c", "c", "c", "c", "c", "cŗ", "cŕ", "cļ",
                    "ţ", "t", "t", "t", "t", "t", "t", "t", "t", "t", "tŗ", "tŕ", "tŗ", "tŕ",
                    "p", "p", "p", "p", "p", "p", "p", "p", "p", "p", "pŗ", "pŕ", "pļ", "pĺ", "pŗ", "pŕ", "p", "p",
                    "kh", "kh", "kh", "kh", "kh", "kh", "kh", "kh", "kh", "kh", "khŗ", "khŕ", "khļ", "khĺ",
                    "ch", "ch", "ch", "ch", "ch", "ch", "ch", "ch", "ch", "chŗ", "chŕ", "chļ", "chĺ",
                    "ţh", "th", "th", "th", "th", "th", "th", "th", "th", "th", "thŗ", "thŕ", "thļ", "thĺ",
                    "ph", "ph", "ph", "ph", "ph", "ph", "ph", "phŗ", "phŕ", "phļ", "phĺ",
                    "g", "j", "đ", "d", "b", "gh", "jh", "đh", "dh", "bh",
                    "ń", "ñ", "ņ", "n", "m", "h", "y", "r", "l", "v", "ś", "ş", "s",
                    "g", "j", "đ", "d", "b", "gh", "jh", "đh", "dh", "bh",
                    "ń", "ñ", "ņ", "n", "m", "h", "y", "r", "l", "v", "ś", "ş", "s",
                    "g", "j", "đ", "d", "b", "gh", "jh", "đh", "dh", "bh",
                    "ń", "ñ", "ņ", "n", "m", "h", "y", "r", "l", "v", "ś", "ş", "s",
                    "g", "j", "đ", "d", "b", "gh", "jh", "đh", "dh", "bh",
                    "ń", "ñ", "ņ", "n", "m", "h", "y", "r", "l", "v", "ś", "ş", "s",
                    "g", "j", "đ", "d", "b", "gh", "jh", "đh", "dh", "bh",
                    "ń", "ñ", "ņ", "n", "m", "h", "y", "r", "l", "v", "ś", "ş", "s",
                    "g", "j", "đ", "d", "b", "gh", "jh", "đh", "dh", "bh",
                    "ń", "ñ", "ņ", "n", "m", "h", "y", "r", "l", "v", "ś", "ş", "s",
                    "g", "j", "đ", "d", "b", "gh", "jh", "đh", "dh", "bh",
                    "ń", "ñ", "ņ", "n", "m", "h", "y", "r", "l", "v", "ś", "ş", "s",
                    "g", "j", "đ", "d", "b", "gh", "đh", "dh", "bh",
                    "ń", "ñ", "ņ", "n", "m", "h", "y", "r", "l", "v", "ś", "ş", "s",
                    "g", "j", "đ", "d", "b", "gh", "đh", "dh", "bh",
                    "ń", "ņ", "n", "m", "h", "y", "r", "l", "v", "ş", "s",
                    "g", "j", "đ", "d", "b", "gh", "đh", "dh", "bh",
                    "ń", "ņ", "n", "m", "h", "y", "r", "l", "v", "ş", "s",
                    "g", "đ", "d", "b", "gh", "đh", "dh", "bh", "n", "m", "v", "s",
                    "g", "đ", "d", "b", "g", "d", "b", "dh", "bh", "n", "m", "v",
                    "g", "đ", "d", "b", "g", "d", "b", "dh", "bh", "n", "m", "v",
            },
            new String[]{
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "kŗ", "kŗ", "kŗ", "kŗ", "kŗ", "nkŗ", "rkŗ",
                    "kŕ", "kŕ", "kŕ", "kŕ", "kŕ", "nkŕ", "rkŕ",
                    "kļ", "kļ", "kļ", "kļ", "kļ", "nkļ", "rkļ",

                    "c", "c", "c", "c", "c", "c", "cŗ", "cŕ", "cļ",
                    "ţ", "t", "t", "t", "t", "t", "nt", "rt",
                    "ţ", "t", "t", "t", "t", "nt", "rt",
                    "ţ", "t", "t", "t", "t", "nt", "rt",
                    "ţ", "t", "t", "t", "t", "nt", "rt",
                    "ţ", "t", "t", "t", "t", "nt", "rt",
                    "ţ", "t", "t", "t", "t", "nt", "rt",
                    "ţ", "t", "t", "t", "t", "nt", "rt",
                    "ţ", "t", "t", "t", "t", "nt", "rt",
                    "ţ", "t", "t", "t", "t", "nt", "rt",
                    "tŗ", "tŗ", "tŗ", "tŗ", "tŗ", "ntŗ", "rtŗ",
                    "tŕ", "tŕ", "tŕ", "tŕ", "tŕ", "ntŕ", "rtŕ",
                    "tŗ", "tŗ", "tŗ", "tŗ", "tŗ", "ntŗ", "rtŗ",
                    "tŕ", "tŕ", "tŕ", "tŕ", "tŕ", "ntŕ", "rtŕ",

                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "pŗ", "pŗ", "pŗ", "pŗ", "pŗ", "npŗ", "rpŗ",
                    "pŕ", "pŕ", "pŕ", "pŕ", "pŕ", "npŕ", "rpŕ",
                    "pļ", "pļ", "pļ", "pļ", "pļ", "npļ", "rpļ",
                    "pĺ", "pĺ", "pĺ", "pĺ", "pĺ", "npĺ", "rpĺ",
                    "pŗ", "pŗ", "pŗ", "pŗ", "pŗ", "npŗ", "rpŗ",
                    "pŕ", "pŕ", "pŕ", "pŕ", "pŕ", "npŕ", "rpŕ",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",

                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "khŗ", "khŗ", "khŗ", "khŗ", "khŗ", "nkhŗ", "rkhŗ",
                    "khŕ", "khŕ", "khŕ", "khŕ", "khŕ", "nkhŕ", "rkhŕ",
                    "khļ", "khļ", "khļ", "khļ", "khļ", "nkhļ", "rkhļ",
                    "khĺ", "khĺ", "khĺ", "khĺ", "khĺ", "nkhĺ", "rkhĺ",

                    "ch", "ch", "ch", "ch", "ch", "ch", "ch", "ch", "ch", "chŗ", "chŕ", "chļ", "chĺ",
                    "ţh", "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "thŗ", "thŗ", "thŗ", "thŗ", "thŗ", "nthŗ", "rthŗ",
                    "thŕ", "thŕ", "thŕ", "thŕ", "thŕ", "nthŕ", "rthŕ",
                    "thļ", "thļ", "thļ", "thļ", "thļ", "nthļ", "rthļ",
                    "thĺ", "thĺ", "thĺ", "thĺ", "thĺ", "nthĺ", "rthĺ",

                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "phŗ", "phŗ", "phŗ", "phŗ", "phŗ", "nphŗ", "rphŗ",
                    "phŕ", "phŕ", "phŕ", "phŕ", "phŕ", "nphŕ", "rphŕ",
                    "phļ", "phļ", "phļ", "phļ", "phļ", "nphļ", "rphļ",
                    "phĺ", "phĺ", "phĺ", "phĺ", "phĺ", "nphĺ", "rphĺ",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "đ", "đ", "đ", "đ", "đ", "nđ", "rđ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "đh", "đh", "đh", "đh", "đh", "nđh", "rđh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ń", "ñ", "ņ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ş", "ş", "ş", "ş", "ş", "nş", "rş",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "đ", "đ", "đ", "đ", "đ", "nđ", "rđ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "đh", "đh", "đh", "đh", "đh", "nđh", "rđh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ń", "ñ", "ņ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ş", "ş", "ş", "ş", "ş", "nş", "rş",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "đ", "đ", "đ", "đ", "đ", "nđ", "rđ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "đh", "đh", "đh", "đh", "đh", "nđh", "rđh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ń", "ñ", "ņ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ş", "ş", "ş", "ş", "ş", "nş", "rş",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "đ", "đ", "đ", "đ", "đ", "nđ", "rđ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "đh", "đh", "đh", "đh", "đh", "nđh", "rđh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ń", "ñ", "ņ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ş", "ş", "ş", "ş", "ş", "nş", "rş",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "đ", "đ", "đ", "đ", "đ", "nđ", "rđ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "đh", "đh", "đh", "đh", "đh", "nđh", "rđh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ń", "ñ", "ņ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ş", "ş", "ş", "ş", "ş", "nş", "rş",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "đ", "đ", "đ", "đ", "đ", "nđ", "rđ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "đh", "đh", "đh", "đh", "đh", "nđh", "rđh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ń", "ñ", "ņ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ş", "ş", "ş", "ş", "ş", "nş", "rş",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "đ", "đ", "đ", "đ", "đ", "nđ", "rđ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "đh", "đh", "đh", "đh", "đh", "nđh", "rđh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ń", "ñ", "ņ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ş", "ş", "ş", "ş", "ş", "nş", "rş",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "đ", "đ", "đ", "đ", "đ", "nđ", "rđ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "đh", "đh", "đh", "đh", "đh", "nđh", "rđh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ń", "ñ", "ņ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ş", "ş", "ş", "ş", "ş", "nş", "rş",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "đ", "đ", "đ", "đ", "đ", "nđ", "rđ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "đh", "đh", "đh", "đh", "đh", "nđh", "rđh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ń", "ņ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ş", "ş", "ş", "ş", "ş", "nş", "rş",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "đ", "đ", "đ", "đ", "đ", "nđ", "rđ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "đh", "đh", "đh", "đh", "đh", "nđh", "rđh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ń", "ņ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ş", "ş", "ş", "ş", "ş", "nş", "rş",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "đ", "đ", "đ", "đ", "đ", "nđ", "rđ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "đh", "đh", "đh", "đh", "đh", "nđh", "rđh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",
                    "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "v", "v", "v", "v", "v", "nv", "rv",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "đ", "đ", "đ", "đ", "đ", "nđ", "rđ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "g", "g", "g", "g", "g", "ng", "rg",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",
                    "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "v", "v", "v", "v", "v", "nv", "rv",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "đ", "đ", "đ", "đ", "đ", "nđ", "rđ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "g", "g", "g", "g", "g", "ng", "rg",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",
                    "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "v", "v", "v", "v", "v", "nv", "rv",
            },
            new String[]{"t", "d", "m", "r", "dh", "b", "t", "d", "m", "r", "dh", "bh", "nt", "nt", "nk", "ş"},
            new String[]{"it", "it", "ati", "adva", "aş", "arma", "ardha", "abi", "ab", "aya"},
            new String[]{}, new int[]{1, 2, 3, 4, 5}, new double[]{1, 2, 3, 3, 1}, 0.15, 0.75, 0.0, 0.12, null, true);


    /**
     * Imitation Hindi, romanized to use the Latin alphabet using accented glyphs from the IAST standard, which are not
     * typically printable with many fonts but are more likely to seem like samples of Hindi from, say, Wikipedia.
     * There is also HINDI_ROMANIZED, which changes the IAST standard glyphs {@code "ṛṝḷḹḍṭṅṇṣṃḥ"} to
     * {@code "ŗŕļĺđţńņşĕĭ"}, with the nth glyph in the first string being substituted with the nth glyph in the second
     * string. Using HINDI_ROMANIZED is recommended if you use the fonts known by SquidLib's display module.
     * <br>
     * Datṝo thīndoṇa, oḍītad; ḍhivīvidh beśībo ru'markiḍaibhit.
     */
    public static final FakeLanguageGen HINDI_IAST = new FakeLanguageGen(
            new String[]{
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "u", "ū", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "u", "ū", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "u", "ū", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "a", "a", "a", "a", "a", "a", "ā", "ā", "i", "i", "i", "i", "ī", "i", "i", "ī", "ī",
                    "u", "u", "u", "ū", "u", "ū", "u", "ū", "e", "ai", "ai", "o", "o", "o", "au",
                    "aṃ", "aṃ", "aṃ", "aṃ", "aṃ", "āṃ", "āṃ", "iṃ", "iṃ", "iṃ", "īṃ", "īṃ",
                    "uṃ", "uṃ", "ūṃ", "aiṃ", "aiṃ", "oṃ", "oṃ", "oṃ", "auṃ",
                    //"aḥ", "aḥ", "aḥ", "aḥ", "aḥ", "āḥ", "āḥ", "iḥ", "iḥ", "iḥ", "īḥ", "īḥ",
                    //"uḥ", "uḥ", "ūḥ", "aiḥ", "aiḥ", "oḥ", "oḥ", "oḥ", "auḥ",
            },
            new String[]{"a'", "i'", "u'", "o'", "a'", "i'", "u'", "o'",
            },
            new String[]{
                    "k", "k", "k", "k", "k", "k", "k", "k", "kṛ", "kṝ", "kḷ",
                    "c", "c", "c", "c", "c", "c", "cṛ", "cṝ", "cḷ",
                    "ṭ", "t", "t", "t", "t", "t", "t", "t", "t", "t", "tṛ", "tṝ", "tṛ", "tṝ",
                    "p", "p", "p", "p", "p", "p", "p", "p", "p", "p", "pṛ", "pṝ", "pḷ", "pḹ", "pṛ", "pṝ", "p", "p",
                    "kh", "kh", "kh", "kh", "kh", "kh", "kh", "kh", "kh", "kh", "khṛ", "khṝ", "khḷ", "khḹ",
                    "ch", "ch", "ch", "ch", "ch", "ch", "ch", "ch", "ch", "chṛ", "chṝ", "chḷ", "chḹ",
                    "ṭh", "th", "th", "th", "th", "th", "th", "th", "th", "th", "thṛ", "thṝ", "thḷ", "thḹ",
                    "ph", "ph", "ph", "ph", "ph", "ph", "ph", "phṛ", "phṝ", "phḷ", "phḹ",
                    "g", "j", "ḍ", "d", "b", "gh", "jh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "jh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "jh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "jh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "jh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "jh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "jh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "ḍh", "dh", "bh",
                    "ṅ", "ñ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ś", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "ḍh", "dh", "bh",
                    "ṅ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ṣ", "s",
                    "g", "j", "ḍ", "d", "b", "gh", "ḍh", "dh", "bh",
                    "ṅ", "ṇ", "n", "m", "h", "y", "r", "l", "v", "ṣ", "s",
                    "g", "ḍ", "d", "b", "gh", "ḍh", "dh", "bh", "n", "m", "v", "s",
                    "g", "ḍ", "d", "b", "g", "d", "b", "dh", "bh", "n", "m", "v",
                    "g", "ḍ", "d", "b", "g", "d", "b", "dh", "bh", "n", "m", "v",
            },
            new String[]{
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "k", "k", "k", "k", "k", "nk", "rk",
                    "kṛ", "kṛ", "kṛ", "kṛ", "kṛ", "nkṛ", "rkṛ",
                    "kṝ", "kṝ", "kṝ", "kṝ", "kṝ", "nkṝ", "rkṝ",
                    "kḷ", "kḷ", "kḷ", "kḷ", "kḷ", "nkḷ", "rkḷ",

                    "c", "c", "c", "c", "c", "c", "cṛ", "cṝ", "cḷ",
                    "ṭ", "t", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "ṭ", "t", "t", "t", "t", "nt", "rt",
                    "tṛ", "tṛ", "tṛ", "tṛ", "tṛ", "ntṛ", "rtṛ",
                    "tṝ", "tṝ", "tṝ", "tṝ", "tṝ", "ntṝ", "rtṝ",
                    "tṛ", "tṛ", "tṛ", "tṛ", "tṛ", "ntṛ", "rtṛ",
                    "tṝ", "tṝ", "tṝ", "tṝ", "tṝ", "ntṝ", "rtṝ",

                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "pṛ", "pṛ", "pṛ", "pṛ", "pṛ", "npṛ", "rpṛ",
                    "pṝ", "pṝ", "pṝ", "pṝ", "pṝ", "npṝ", "rpṝ",
                    "pḷ", "pḷ", "pḷ", "pḷ", "pḷ", "npḷ", "rpḷ",
                    "pḹ", "pḹ", "pḹ", "pḹ", "pḹ", "npḹ", "rpḹ",
                    "pṛ", "pṛ", "pṛ", "pṛ", "pṛ", "npṛ", "rpṛ",
                    "pṝ", "pṝ", "pṝ", "pṝ", "pṝ", "npṝ", "rpṝ",
                    "p", "p", "p", "p", "p", "np", "rp",
                    "p", "p", "p", "p", "p", "np", "rp",

                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "kh", "kh", "kh", "kh", "kh", "nkh", "rkh",
                    "khṛ", "khṛ", "khṛ", "khṛ", "khṛ", "nkhṛ", "rkhṛ",
                    "khṝ", "khṝ", "khṝ", "khṝ", "khṝ", "nkhṝ", "rkhṝ",
                    "khḷ", "khḷ", "khḷ", "khḷ", "khḷ", "nkhḷ", "rkhḷ",
                    "khḹ", "khḹ", "khḹ", "khḹ", "khḹ", "nkhḹ", "rkhḹ",

                    "ch", "ch", "ch", "ch", "ch", "ch", "ch", "ch", "ch", "chṛ", "chṝ", "chḷ", "chḹ",
                    "ṭh", "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "th", "th", "th", "th", "th", "nth", "rth",
                    "thṛ", "thṛ", "thṛ", "thṛ", "thṛ", "nthṛ", "rthṛ",
                    "thṝ", "thṝ", "thṝ", "thṝ", "thṝ", "nthṝ", "rthṝ",
                    "thḷ", "thḷ", "thḷ", "thḷ", "thḷ", "nthḷ", "rthḷ",
                    "thḹ", "thḹ", "thḹ", "thḹ", "thḹ", "nthḹ", "rthḹ",

                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "ph", "ph", "ph", "ph", "ph", "nph", "rph",
                    "phṛ", "phṛ", "phṛ", "phṛ", "phṛ", "nphṛ", "rphṛ",
                    "phṝ", "phṝ", "phṝ", "phṝ", "phṝ", "nphṝ", "rphṝ",
                    "phḷ", "phḷ", "phḷ", "phḷ", "phḷ", "nphḷ", "rphḷ",
                    "phḹ", "phḹ", "phḹ", "phḹ", "phḹ", "nphḹ", "rphḹ",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "jh", "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ñ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ś", "ś", "ś", "ś", "ś", "nś", "rś",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "j", "j", "j", "j", "j", "nj", "rj",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",

                    "ṅ", "ṇ", "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "h", "y", "y", "y", "y", "y", "ny", "ry",
                    "r", "l", "v", "v", "v", "v", "v", "nv", "rv",
                    "ṣ", "ṣ", "ṣ", "ṣ", "ṣ", "nṣ", "rṣ",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "gh", "gh", "gh", "gh", "gh", "ngh", "rgh",
                    "ḍh", "ḍh", "ḍh", "ḍh", "ḍh", "nḍh", "rḍh",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",
                    "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "v", "v", "v", "v", "v", "nv", "rv",
                    "s", "s", "s", "s", "s", "ns", "rs",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "g", "g", "g", "g", "g", "ng", "rg",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",
                    "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "v", "v", "v", "v", "v", "nv", "rv",

                    "g", "g", "g", "g", "g", "ng", "rg",
                    "ḍ", "ḍ", "ḍ", "ḍ", "ḍ", "nḍ", "rḍ",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "g", "g", "g", "g", "g", "ng", "rg",
                    "d", "d", "d", "d", "d", "nd", "rd",
                    "b", "b", "b", "b", "b", "nb", "rb",
                    "dh", "dh", "dh", "dh", "dh", "ndh", "rdh",
                    "bh", "bh", "bh", "bh", "bh", "nbh", "rbh",
                    "n", "m", "m", "m", "m", "m", "nm", "rm",
                    "v", "v", "v", "v", "v", "nv", "rv",
            },
            new String[]{"t", "d", "m", "r", "dh", "b", "t", "d", "m", "r", "dh", "bh", "nt", "nt", "nk", "ṣ"},
            new String[]{"it", "it", "ati", "adva", "aṣ", "arma", "ardha", "abi", "ab", "aya"},
            new String[]{}, new int[]{1, 2, 3, 4, 5}, new double[]{1, 2, 3, 3, 1}, 0.15, 0.75, 0.0, 0.12, null, true);

    /**
     * Imitation Arabic, using mostly the Latin alphabet but with some Greek letters for tough transliteration topics.
     * It's hard to think of a more different (widely-spoken) language to romanize than Arabic. Written Arabic does not
     * ordinarily use vowels (the writing system is called an abjad, in contrast to an alphabet), and it has more than a
     * few sounds that are very different from those in English. This version, because of limited support in fonts and
     * the need for separate words to be distinguishable with regular expressions, uses Greek letters in place of hamzah
     * and 'ayin (the second of the two isn't entered correctly here since it wouldn't be printed with most fonts; you
     * can see https://en.wikipedia.org/wiki/Ayin for more details). Hamzah is represented with Greek delta, 'δ', and
     * 'ayin is represented with Greek xi, 'ξ', both picked because of similarity to some forms of the glyphs in the
     * Arabic script. Many other letters are mapped to alternate representations because the common romanizations use
     * rare glyphs that SquidLib's fonts in the display module can't support. Using the conventions this FakeLanguageGen
     * does for writing the Arabic glyph names, these are: ţāδ becomes ţ, ĥāδ becomes ĥ, ħāδ becomes ħ, đāl becomes
     * đ, šīn becomes š, şād becomes ş, ďād becomes ď, ťāδ becomes ť, żāδ becomes ż, gain becomes g, wāw becomes ū, and
     * yāδ becomes ī. You can add the modifier {@link FakeLanguageGen.Modifier#SIMPLIFY_ARABIC} to this FakeLanguageGen
     * to remove the hard-or-impossible-to-pronounce greek letters and most accents from the generated text and use
     * English-like blends such as "sh" in place of accented letters.
     * <br>
     * Please try to be culturally-sensitive about how you use this generator. Classical Arabic (the variant that
     * normally marks vowels explicitly and is used to write the Qur'an) has deep religious significance in Islam, and
     * if you machine-generate text that (probably) isn't valid Arabic, but claim that it is real, or that it has
     * meaning when it actually doesn't, that would be an improper usage of what this generator is meant to do. In a
     * fantasy setting, you can easily confirm that the language is fictional and any overlap is coincidental; an
     * example of imitation Arabic in use is the Dungeons and Dragons setting, Al-Qadim, which according to one account
     * sounds similar to a word in real Arabic (that does not mean anything like what the designer was aiming for). In a
     * historical setting, FakeLanguageGen is probably "too fake" to make a viable imitation for any language, and may
     * just sound insulting if portrayed as realistic. You may want to mix ARABIC_ROMANIZED with a very different kind
     * of language, like GREEK_ROMANIZED or RUSSIAN_AUTHENTIC, to emphasize that this is not a real-world language.
     * <br>
     * Iramzā qāşi, qīqa banji, rūşiďīq ifateh!
     *
     * @see FakeLanguageGen.Modifier#SIMPLIFY_ARABIC to make this more romanized and probaby more legible.
     */
    public static final FakeLanguageGen ARABIC_ROMANIZED = new FakeLanguageGen(
            new String[]{"a", "a", "a", "a", "a", "a", "ā", "ā", "ā", "ai", "au",
                    "a", "i", "u", "a", "i", "u",
                    "i", "i", "i", "i", "i", "ī", "ī", "ī",
                    "u", "u", "u", "ū", "ū",
            },
            new String[]{},
            new String[]{"δ", "b", "t", "ţ", "j", "ĥ", "ħ", "d", "đ", "r", "z", "s", "š", "ş", "ď", "ť",
                    "ż", "ξ", "g", "f", "q", "k", "l", "m", "n", "h", "w",
                    "q", "k", "q", "k", "b", "d", "f", "l", "z", "ż", "h", "h", "ĥ", "j", "s", "š", "ş", "r",
                    "q", "k", "q", "k", "f", "l", "z", "h", "h", "j", "s", "r",
                    "q", "k", "f", "l", "z", "h", "h", "j", "s", "r",
                    "al-", "al-", "ibn-",
            },
            new String[]{
                    "kk", "kk", "kk", "kk", "kk", "dd", "dd", "dd", "dd",
                    "nj", "mj", "bj", "mj", "bj", "mj", "bj", "dj", "ďj", "đj",
                    "nz", "nż", "mz", "mż", "rz", "rż", "bz", "dz", "tz",
                    "s-h", "š-h", "ş-h", "tw", "bn", "fq", "hz", "hl", "ĥm",
                    "lb", "lz", "lj", "lf", "ll", "lk", "lq", "lg", "ln"
            },
            new String[]{
                    "δ", "b", "t", "ţ", "j", "ĥ", "ħ", "d", "đ", "r", "z", "s", "š", "ş", "ď", "ť",
                    "ż", "ξ", "g", "f", "q", "k", "l", "m", "n", "h", "w",
                    "k", "q", "k", "b", "d", "f", "l", "z", "ż", "h", "h", "ĥ", "j", "s", "š", "ş", "r",
                    "k", "q", "k", "f", "l", "z", "h", "h", "j", "s", "r",
                    "k", "f", "l", "z", "h", "h", "j", "s", "r",
                    "b", "t", "ţ", "j", "ĥ", "ħ", "d", "đ", "r", "z", "s", "š", "ş", "ď", "ť",
                    "ż", "g", "f", "q", "k", "l", "m", "n", "h", "w",
                    "k", "q", "k", "b", "d", "f", "l", "z", "ż", "h", "h", "ĥ", "j", "s", "š", "ş", "r",
                    "k", "q", "k", "f", "l", "z", "h", "h", "j", "s", "r",
                    "k", "f", "l", "z", "h", "h", "j", "s", "r",
            },
            new String[]{"āδ", "āδ", "ari", "ari", "aīd", "ūq", "arīd", "adih", "ateh", "adeš", "amīt", "it",
                    "īt", "aĥmen", "aĥmed", "ani", "abīb", "īb", "ūni", "īz", "aqarī", "adīq",
            },
            new String[]{}, new int[]{1, 2, 3, 4}, new double[]{6, 5, 5, 1}, 0.55, 0.65, 0.0, 0.15, arabicSanityChecks, true);

    /**
     * A mix of four different languages, using only ASCII characters, that is meant for generating single words for
     * creature or place names in fantasy settings.
     * <br>
     * Adeni, Sainane, Caneros, Sune, Alade, Tidifi, Muni, Gito, Lixoi, Bovi...
     */
    public static final FakeLanguageGen FANTASY_NAME = GREEK_ROMANIZED.mix(
            RUSSIAN_ROMANIZED.mix(
                    FRENCH.removeAccents().mix(
                            JAPANESE_ROMANIZED, 0.5), 0.85), 0.925);
    /**
     * A mix of four different languages with some accented characters added onto an ASCII base, that can be good for
     * generating single words for creature or place names in fantasy settings that should have a "fancy" feeling from
     * having unnecessary accents added primarily for visual reasons.
     * <br>
     * Askieno, Blarcīnũn, Mēmida, Zizhounkô, Blęrinaf, Zemĭ, Mónazôr, Renerstă, Uskus, Toufounôr...
     */
    public static final FakeLanguageGen FANCY_FANTASY_NAME = FANTASY_NAME.addAccents(0.47, 0.07);

    /**
     * Zero-arg constructor for a FakeLanguageGen; produces a FakeLanguageGen equivalent to FakeLanguageGen.ENGLISH .
     */
    public FakeLanguageGen() {
        this(
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
                        "b", "c", "d", "f", "g", "k", "l", "m", "n", "p", "r", "s", "t", "w",
                },
                new String[]{"ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                        "ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                        "ate", "ite", "ism", "ist", "er", "er", "er", "ed", "ed", "ed", "es", "es", "ied", "y", "y", "y", "y",
                        "ay", "ay", "ey", "oy", "ay", "ay", "ey", "oy",
                        "ough", "aught", "ant", "ont", "oe", "ance", "ell", "eal", "oa", "urt", "ut", "iom", "ion", "ion", "ision", "ation", "ation", "ition",
                        "ough", "aught", "ant", "ont", "oe", "ance", "ell", "eal", "oa", "urt", "ut", "iom", "ion", "ion", "ision", "ation", "ation", "ition",
                        "ily", "ily", "ily", "adly", "owly", "oorly", "ardly", "iedly",
                },
                new String[]{}, new int[]{1, 2, 3, 4}, new double[]{7, 8, 4, 1}, 0.22, 0.1, 0.0, 0.25, englishSanityChecks, true);
    }

    /**
     * This is a very complicated constructor! Maybe look at the calls to this to initialize static members of this
     * class, LOVECRAFT and GREEK_ROMANIZED.
     *
     * @param openingVowels        String array where each element is a vowel or group of vowels that may appear at the start
     *                             of a word or in the middle; elements may be repeated to make them more common
     * @param midVowels            String array where each element is a vowel or group of vowels that may appear in the
     *                             middle of the word; all openingVowels are automatically copied into this internally.
     *                             Elements may be repeated to make them more common
     * @param openingConsonants    String array where each element is a consonant or consonant cluster that can appear
     *                             at the start of a word; elements may be repeated to make them more common
     * @param midConsonants        String array where each element is a consonant or consonant cluster than can appear
     *                             between vowels; all closingConsonants are automatically copied into this internally.
     *                             Elements may be repeated to make them more common
     * @param closingConsonants    String array where each element is a consonant or consonant cluster than can appear
     *                             at the end of a word; elements may be repeated to make them more common
     * @param closingSyllables     String array where each element is a syllable starting with a vowel and ending in
     *                             whatever the word should end in; elements may be repeated to make them more common
     * @param vowelSplitters       String array where each element is a mark that goes between vowels, so if "-" is in this,
     *                             then "a-a" may be possible; elements may be repeated to make them more common
     * @param syllableLengths      int array where each element is a possible number of syllables a word can use; closely
     *                             tied to syllableFrequencies
     * @param syllableFrequencies  double array where each element corresponds to an element in syllableLengths and
     *                             represents how often each syllable count should appear relative to other counts; there
     *                             is no need to restrict the numbers to add up to any other number
     * @param vowelStartFrequency  a double between 0.0 and 1.0 that determines how often words start with vowels;
     *                             higher numbers yield more words starting with vowels
     * @param vowelEndFrequency    a double between 0.0 and 1.0 that determines how often words end with vowels; higher
     *                             numbers yield more words ending in vowels
     * @param vowelSplitFrequency  a double between 0.0 and 1.0 that, if vowelSplitters is not empty, determines how
     *                             often a vowel will be split into two vowels separated by one of those splitters
     * @param syllableEndFrequency a double between 0.0 and 1.0 that determines how often an element of
     *                             closingSyllables is used instead of ending normally
     */
    public FakeLanguageGen(String[] openingVowels, String[] midVowels, String[] openingConsonants,
                           String[] midConsonants, String[] closingConsonants, String[] closingSyllables, String[] vowelSplitters,
                           int[] syllableLengths, double[] syllableFrequencies, double vowelStartFrequency,
                           double vowelEndFrequency, double vowelSplitFrequency, double syllableEndFrequency) {
        this(openingVowels, midVowels, openingConsonants, midConsonants, closingConsonants, closingSyllables,
                vowelSplitters, syllableLengths, syllableFrequencies, vowelStartFrequency, vowelEndFrequency,
                vowelSplitFrequency, syllableEndFrequency, englishSanityChecks, true);
    }

    /**
     * This is a very complicated constructor! Maybe look at the calls to this to initialize static members of this
     * class, LOVECRAFT and GREEK_ROMANIZED.
     *
     * @param openingVowels        String array where each element is a vowel or group of vowels that may appear at the start
     *                             of a word or in the middle; elements may be repeated to make them more common
     * @param midVowels            String array where each element is a vowel or group of vowels that may appear in the
     *                             middle of the word; all openingVowels are automatically copied into this internally.
     *                             Elements may be repeated to make them more common
     * @param openingConsonants    String array where each element is a consonant or consonant cluster that can appear
     *                             at the start of a word; elements may be repeated to make them more common
     * @param midConsonants        String array where each element is a consonant or consonant cluster than can appear
     *                             between vowels; all closingConsonants are automatically copied into this internally.
     *                             Elements may be repeated to make them more common
     * @param closingConsonants    String array where each element is a consonant or consonant cluster than can appear
     *                             at the end of a word; elements may be repeated to make them more common
     * @param closingSyllables     String array where each element is a syllable starting with a vowel and ending in
     *                             whatever the word should end in; elements may be repeated to make them more common
     * @param vowelSplitters       String array where each element is a mark that goes between vowels, so if "-" is in this,
     *                             then "a-a" may be possible; elements may be repeated to make them more common
     * @param syllableLengths      int array where each element is a possible number of syllables a word can use; closely
     *                             tied to syllableFrequencies
     * @param syllableFrequencies  double array where each element corresponds to an element in syllableLengths and
     *                             represents how often each syllable count should appear relative to other counts; there
     *                             is no need to restrict the numbers to add up to any other number
     * @param vowelStartFrequency  a double between 0.0 and 1.0 that determines how often words start with vowels;
     *                             higher numbers yield more words starting with vowels
     * @param vowelEndFrequency    a double between 0.0 and 1.0 that determines how often words end with vowels; higher
     *                             numbers yield more words ending in vowels
     * @param vowelSplitFrequency  a double between 0.0 and 1.0 that, if vowelSplitters is not empty, determines how
     *                             often a vowel will be split into two vowels separated by one of those splitters
     * @param syllableEndFrequency a double between 0.0 and 1.0 that determines how often an element of
     *                             closingSyllables is used instead of ending normally
     * @param sane                 true to perform sanity checks for pronounce-able sounds to most English speakers, replacing many
     *                             words that are impossible to say; slows down generation slightly, irrelevant for non-Latin alphabets
     * @param clean                true to perform vulgarity/obscenity checks on the word, replacing it if it is too close to a
     *                             common English vulgarity, obscenity, or slur/epithet; slows down generation slightly
     */
    public FakeLanguageGen(String[] openingVowels, String[] midVowels, String[] openingConsonants,
                           String[] midConsonants, String[] closingConsonants, String[] closingSyllables, String[] vowelSplitters,
                           int[] syllableLengths, double[] syllableFrequencies, double vowelStartFrequency,
                           double vowelEndFrequency, double vowelSplitFrequency, double syllableEndFrequency,
                           Pattern[] sane, boolean clean) {
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

        this.syllableFrequencies = new IntDoubleOrderedMap(syllableLengths, syllableFrequencies, 0.75f);

        totalSyllableFrequency = this.syllableFrequencies.values().sum();
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
        this.clean = clean;
        sanityChecks = sane;
        modifiers = new ArrayList<>(16);
    }

    private FakeLanguageGen(String[] openingVowels, String[] midVowels, String[] openingConsonants,
                            String[] midConsonants, String[] closingConsonants, String[] closingSyllables,
                            String[] vowelSplitters, IntDoubleOrderedMap syllableFrequencies,
                            double vowelStartFrequency, double vowelEndFrequency, double vowelSplitFrequency,
                            double syllableEndFrequency, Pattern[] sanityChecks, boolean clean,
                            List<Modifier> modifiers) {
        this.openingVowels = copyStrings(openingVowels);
        this.midVowels = copyStrings(midVowels);
        this.openingConsonants = copyStrings(openingConsonants);
        this.midConsonants = copyStrings(midConsonants);
        this.closingConsonants = copyStrings(closingConsonants);
        this.closingSyllables = copyStrings(closingSyllables);
        this.vowelSplitters = copyStrings(vowelSplitters);
        this.syllableFrequencies = new IntDoubleOrderedMap(syllableFrequencies);
        this.vowelStartFrequency = vowelStartFrequency;
        this.vowelEndFrequency = vowelEndFrequency;
        this.vowelSplitFrequency = vowelSplitFrequency;
        this.syllableEndFrequency = syllableEndFrequency;
        for (Double freq : this.syllableFrequencies.values()) {
            totalSyllableFrequency += freq;
        }
        if (sanityChecks == null)
            this.sanityChecks = null;
        else {
            this.sanityChecks = new Pattern[sanityChecks.length];
            System.arraycopy(sanityChecks, 0, this.sanityChecks, 0, sanityChecks.length);
        }
        this.clean = clean;
        this.modifiers = new ArrayList<>(modifiers);
    }

    private static String[] processParts(OrderedMap<String, String> parts, OrderedSet<String> missingSounds,
                                         OrderedSet<String> forbidden, RNG rng, double repeatSingleChance,
                                         int preferredLimit) {
        int l, sz = parts.size();
        List<String> working = new ArrayList<>(sz * 24);
        for (int e = 0; e < parts.size(); e++) {
            Map.Entry<String, String> sn = parts.entryAt(e);
            if(missingSounds.contains(sn.getKey()))
                continue;
            for (String t : sn.getValue().split(" ")) {
                if(forbidden.contains(t))
                    continue;
                l = t.length();
                int num;
                char c;
                switch (l) {
                    case 0:
                        break;
                    case 1:
                        working.add(t);
                        working.add(t);
                        c = t.charAt(0);
                        num = 0;
                        boolean repeat = true;
                        switch (c) {
                            case 'w':
                                num++;
                            case 'y':
                            case 'h':
                                num++;
                            case 'j':
                                num++;
                            case 'q':
                            case 'x':
                                num+=2;
                                repeat = false;
                                break;
                            case 'i':
                            case 'u':
                                repeat = false;
                                num = 5;
                                break;
                            case 'z':
                            case 'v':
                                num = 2;
                                break;
                            default:
                                if(e >= preferredLimit)
                                    num = 2;
                                else
                                    num = 5;
                        }
                        for (int i = 0; i < num * 3; i++) {
                            if (rng.nextDouble() < 0.7) {
                                working.add(t);
                            }
                        }

                        if (repeat && rng.nextDouble() < repeatSingleChance) {
                            working.add(t + t);
                            working.add(t + t);
                            working.add(t + t);
                            if (rng.nextDouble() < 0.8) {
                                working.add(t + t);
                                working.add(t + t);
                            }
                            if (rng.nextDouble() < 0.8) {
                                working.add(t + t);
                            }
                        }

                        break;
                    case 2:
                        if (rng.nextDouble() < 0.75) {
                            c = t.charAt(1);
                            num = 0;
                            switch (c) {
                                case 'z':
                                    num = 1;
                                    break;
                                case 'w':
                                    num = 3;
                                    break;
                                case 'n':
                                    num = 4;
                                    break;
                                default:

                                    if(e >= preferredLimit)
                                        num = 2;
                                    else
                                        num = 6;
                            }
                            working.add(t);
                            for (int i = 0; i < num; i++) {
                                    if (rng.nextDouble() < 0.3) {
                                        working.add(t);
                                    }
                                }
                            }
                        break;
                    case 3:
                        c = t.charAt(0);
                        num = 0;
                        switch (c) {
                            case 'z':
                                num = 1;
                                break;
                            case 'w':
                                num = 3;
                                break;
                            case 'n':
                                num = 4;
                                break;
                            default:
                                if(e >= preferredLimit)
                                    num = 2;
                                else
                                    num = 6;
                        }
                        working.add(t);
                        for (int i = 0; i < num; i++) {
                            if (rng.nextDouble() < 0.2) {
                                working.add(t);
                            }
                        }
                        break;
                    default:
                        if (rng.nextDouble() < 0.3 && (t.charAt(l - 1) != 'z' || rng.nextDouble() < 0.1)) {
                            working.add(t);
                        }
                        break;
                }
            }
        }
        return working.toArray(new String[working.size()]);
    }

    /*private static final String[][] openVowels = new String[][]{
            new String[]{"a", "a", "a", "a", "aa", "ae", "ai", "au", "ea", "ia", "oa", "ua",},
            new String[]{"e", "e", "e", "e", "ae", "ea", "ee", "ei", "eo", "eu", "ie", "ue",},
            new String[]{"i", "i", "i", "i", "ai", "ei", "ia", "ie", "io", "iu", "oi", "ui",},
            new String[]{"o", "o", "o", "o", "eo", "io", "oa", "oi", "oo", "ou",},
            new String[]{"u", "u", "u", "u", "au", "eu", "iu", "ou", "ua", "ue", "ui",},
    };
*/
    private static final OrderedMap<String, String> openVowels = new OrderedMap<>(16, 0.875f),
            openCons = new OrderedMap<>(64, 0.875f), midCons = new OrderedMap<>(64, 0.875f), closeCons = new OrderedMap<>(64, 0.875f);

    static {
        openVowels.put("a", "a aa ae ai au ea ia oa ua");
        openVowels.put("e", "e ae ea ee ei eo eu ie ue");
        openVowels.put("i", "i ai ei ia ie io iu oi ui");
        openVowels.put("o", "o eo io oa oi oo ou");
        openVowels.put("u", "u au eu iu ou ua ue ui");

        openCons.put("b", "b bl br by bw bh");
        openCons.put("bh", "bh");
        openCons.put("c", "c cl cr cz cth sc scl");
        openCons.put("ch", "ch ch chw");
        openCons.put("d", "d dr dz dy dw dh");
        openCons.put("dh", "dh");
        openCons.put("f", "f fl fr fy fw sf");
        openCons.put("g", "g gl gr gw gy gn");
        openCons.put("h", "bh cth ch ch chw dh h hm hy hw kh khl khw ph phl phr sh shl shqu shk shp shm shn shr shw shpl th th thr thl thw");
        openCons.put("j", "j j");
        openCons.put("k", "k kr kl ky kn sk skl shk");
        openCons.put("kh", "kh khl khw");
        openCons.put("l", "bl cl fl gl kl khl l pl phl scl skl spl sl shl shpl tl thl zl");
        openCons.put("m", "hm m mr mw my sm smr shm");
        openCons.put("n", "gn kn n nw ny pn sn shn");
        openCons.put("p", "p pl pr py pw pn sp spr spl shp shpl ph phl phr");
        openCons.put("ph", "ph phl phr");
        openCons.put("q", "q");
        openCons.put("qu", "qu squ shqu");
        openCons.put("r", "br cr dr fr gr kr mr pr phr r str spr smr shr tr thr vr wr zvr");
        openCons.put("s", "s sc scl sf sk skl st str sp spr spl sl sm smr sn sw sy squ ts sh shl shqu shk shp shm shn shr shw shpl");
        openCons.put("sh", "sh shl shqu shk shp shm shn shr shw shpl");
        openCons.put("t", "st str t ts tr tl ty tw tl");
        openCons.put("th", "cth th thr thl thw");
        openCons.put("tl", "tl");
        openCons.put("v", "v vr vy zv zvr");
        openCons.put("w", "bw chw dw fw gw hw khw mw nw pw sw shw tw thw w wr zw");
        openCons.put("x", "x");
        openCons.put("y", "by dy fy gy hy ky my ny py sy ty vy y zy");
        openCons.put("z", "cz dz z zv zvr zl zy zw");

        midCons.put("b", "lb lb rb rb bl br bl br bl br lbr rbl skbr scbr zb lbh rbh bb");
        midCons.put("bh", "lbh rbh");
        midCons.put("c", "lc lc lsc rc rc rsc cl cr cl cr cl cr lcr rcl sctr scdr scbr scpr msc mscr nsc nscr ngscr ndscr cc");
        midCons.put("ch", "lch lch rch rch");
        midCons.put("d", "ld ld rd rd skdr scdr dr dr dr rdr ldr zd zdr ndr ndscr ndskr ndst ldh rdh dd");
        midCons.put("dh", "ldh rdh");
        midCons.put("f", "lf lf rf rf fl fr fl fr fl fr lfr rfl ff");
        midCons.put("g", "lg lg rg rg gl gr gl gr gl gr lgr rgl zg zgr ngr ngl ngscr ngskr gg");
        midCons.put("h", "lch lph lth lsh rch rph rsh rth phl phr lphr rphl shl shr lshr rshl msh mshr zth ");
        midCons.put("j", "lj lj rj rj");
        midCons.put("k", "lk lsk rk rsk kl kr lkr rkl sktr skdr skbr skpr zk zkr msk mskr nsk nskr ngskr ndskr kk");
        midCons.put("kh", "lkh rkh");
        midCons.put("l", "lb lc lch ld lf lg lj lk lm ln lp lph ls lst lt lth lsc lsk lsp lv lz lsh bl lbr rbl cl lcr rcl fl lfr rfl gl lgr rgl kl lkr rkl pl lpr rpl phl lphr rphl shl lshr rshl sl rsl lsl ldr ltr lx ngl nsl msl nsl ll");
        midCons.put("m", "lm lm rm rm zm msl msc mscr msh mshr mst msp msk mskr mm");
        midCons.put("n", "ln ln rn rn nx zn zn ntr ntr ndr ngr ngl nsl nsl nsc nscr ngscr ndscr nsk nskr ngskr ndskr nst ndst nsp nn");
        midCons.put("p", "lp lsp rp rsp pl pr lpr rpl skpr scpr zp msp nsp lph rph phl phr lphr rphl pp");
        midCons.put("ph", "lph lph rph rph phl phr lphr rphl");
        midCons.put("q", "");
        midCons.put("qu", "lqu rqu");
        midCons.put("r", "rb rc rch rd rf rg rj rk rm rn rp rph rs rsh rst rt rth rsc rsk rsp rv rz br br br lbr rbl cr cr cr lcr rcl fr fr fr lfr rfl gr gr gr lgr rgl kr kr kr lkr rkl pr pr pr lpr rpl phr phr phr lphr rphl shr shr shr lshr rshl rsl sktr sctr skdr scdr skbr scbr skpr scpr dr dr dr rdr ldr tr tr tr rtr ltr rx zr zdr ztr zgr zkr ntr ntr ndr ngr mscr mshr mskr nscr ngscr ndscr nskr ngskr ndskr rr");
        midCons.put("s", "ls lst lsc lsk lsp rs rst rsc rsk rsp sl rsl lsl sktr sctr skdr scdr skbr scbr skpr scpr nsl msl msc mscr mst msp msk mskr nsl nsc nscr ngscr ndscr nsk nskr ngskr ndskr nst ndst nsp lsh rsh sh shl shqu shk shp shm shn shr shw shpl lshr rshl msh mshr ss");
        midCons.put("sh", "lsh rsh sh shl shqu shk shp shm shn shr shw shpl lshr rshl msh mshr");
        midCons.put("t", "lst lt rst rt sktr sctr tr rtr ltr zt ztr ntr ntr mst nst ndst ltl rtl tt");
        midCons.put("th", "lth rth zth cth");
        midCons.put("tl", "ltl rtl");
        midCons.put("v", "lv rv vv");
        midCons.put("w", "bw chw dw fw gw hw khw mw nw pw sw shw tw thw w wr zw");
        midCons.put("x", "nx rx lx");
        midCons.put("y", "by dy fy gy hy ky my ny py sy ty vy y zy");
        midCons.put("z", "lz rz zn zd zt zg zk zm zn zp zb zr zdr ztr zgr zkr zth zz");

        closeCons.put("b", "b lb rb bs bz mb mbs bh bh lbh rbh mbh bb");
        closeCons.put("bh", "bh lbh rbh mbh");
        closeCons.put("c", "c ck cks lc rc cs cz ct cz cth sc");
        closeCons.put("ch", "ch lch rch tch pch kch mch nch");
        closeCons.put("d", "d ld rd ds dz dt dsh dth gd nd nds dh dh ldh rdh ndh dd");
        closeCons.put("dh", "dh ldh rdh ndh");
        closeCons.put("f", "f lf rf fs fz ft fsh fth ff");
        closeCons.put("g", "g lg rg gs gz gd gsh gth ng ngs gg");
        closeCons.put("h", "cth ch lch rch tch pch kch mch nch dsh dth fsh fth gsh gth h hs ksh kth psh pth ph ph ph ph ph ph lph rph phs pht phth");
        closeCons.put("j", "j");
        closeCons.put("k", "ck cks kch k lk rk ks kz kt ksh kth nk nks sk");
        closeCons.put("kh", "kh");
        closeCons.put("l", "lb lc lch ld lf lg lk l ls lz lp lph ll");
        closeCons.put("m", "mch m ms mb mt mp mbs mps mz sm mm");
        closeCons.put("n", "nch n ns nd nt nk nds nks nz ng ngs nn");
        closeCons.put("p", "pch mp mps p lp rp ps pz pt psh pth sp sp ph lph rph phs pht phth");
        closeCons.put("ph", "ph lph rph phs pht phth");
        closeCons.put("q", "q");
        closeCons.put("qu", "");
        closeCons.put("r", "rb rc rch rd rf rg rk rp rph r rs rz");
        closeCons.put("s", "bs cks cs ds fs gs hs ks ls ms mbs mps ns nds nks ngs ps phs rs s st sp st sp sc sk sm ts lsh rsh sh shk shp msh ss");
        closeCons.put("sh", "lsh rsh sh shk shp msh");
        closeCons.put("t", "ct tch dt ft kt mt nt pt pht st st t ts tz tt");
        closeCons.put("th", "cth dth fth gth kth pth phth th ths");
        closeCons.put("tl", "");
        closeCons.put("v", "v");
        closeCons.put("w", "");
        closeCons.put("x", "x");
        closeCons.put("y", "");
        closeCons.put("z", "bz cz dz fz gz kz lz mz nz pz rz tz z zz");

    }

    public static FakeLanguageGen randomLanguage(RNG rng) {
        int[] lengths = new int[rng.between(3, 5)];
        System.arraycopy(new int[]{1, 2, 3, 4}, 0, lengths, 0, lengths.length);
        double[] chances = new double[lengths.length];
        System.arraycopy(new double[]{
                5 + rng.nextDouble(4), 13 + rng.nextDouble(9), 3 + rng.nextDouble(3), 1 + rng.nextDouble(2)
        }, 0, chances, 0, chances.length);
        double vowelHeavy = rng.between(0.2, 0.5), diversity = rng.between(0.4, 0.8);
        int sz = openCons.size();
        int[] reordering = rng.randomOrdering(sz), vOrd = rng.randomOrdering(5);
            OrderedMap<String, String>
                    parts0 = new OrderedMap<>(openVowels),
                    parts1 = new OrderedMap<>(openCons),
                    parts2 = new OrderedMap<>(midCons),
                    parts3 = new OrderedMap<>(closeCons);
            OrderedSet<String> forbidden = new OrderedSet<>(1024, 0.25f), missingSounds = new OrderedSet<>(64, 0.875f);
        parts1.reorder(reordering);
        parts2.reorder(reordering);
        parts3.reorder(reordering);
        parts0.reorder(vOrd);
        int n = 0;

        int mn = Math.min(rng.nextInt(3), rng.nextInt(3)), sz0, p0s;

        for (n = 0; n < mn; n++) {
            missingSounds.add(parts0.keyAt(0));
            Collections.addAll(forbidden, parts0.getAt(0).split(" "));
            parts0.removeFirst();
        }
        sz0 = Math.max(rng.between(1, parts0.size()+1), rng.between(1, parts0.size()+1));
        char[] nextAccents = new char[sz0], unaccented = new char[sz0];
        int vowelAccent = rng.between(1, 7);
        for (int i = 0; i < sz0; i++) {
            nextAccents[i] = accentedVowels[vOrd[i + mn]][vowelAccent];
            unaccented[i] = accentedVowels[vOrd[i + mn]][0];
        }
        p0s = parts0.size();
        if(rng.nextDouble() < 0.8)
        {
            for (int i = 0; i < sz0; i++) {
                char ac = nextAccents[i], ua = unaccented[i];
                String v = "", uas = String.valueOf(ua);
                Pattern pat = Pattern.compile("\\b([a-z]*)(" + ua + ")([a-z]*)\\b");
                Replacer rep = pat.replacer("$1$2$3 $1" + ac + "$3"), repLess = pat.replacer("$1" + ac + "$3");
                for (int j = 0; j < p0s; j++) {
                    String k = parts0.keyAt(j);
                    if(uas.equals(k))
                        v = parts0.getAt(j);
                    else
                    {
                        String current = parts0.getAt(j);
                        String[] splits = current.split(" ");
                        for (int s = 0; s < splits.length; s++) {
                            if(forbidden.contains(uas))
                                forbidden.add(splits[s].replace(ua, ac));
                        }
                        parts0.put(k, rep.replace(current));
                    }
                }
                parts0.put(String.valueOf(ac), repLess.replace(v));
            }
        }

        n = 0;
        if (rng.nextDouble() < 0.75) {
            missingSounds.add("z");
            Collections.addAll(forbidden, parts1.get("z").split(" "));
            Collections.addAll(forbidden, parts2.get("z").split(" "));
            Collections.addAll(forbidden, parts3.get("z").split(" "));
            n++;
        }
        if (rng.nextDouble() < 0.82) {
            missingSounds.add("x");
            Collections.addAll(forbidden, parts1.get("x").split(" "));
            Collections.addAll(forbidden, parts2.get("x").split(" "));
            Collections.addAll(forbidden, parts3.get("x").split(" "));
            n++;
        }
        if (rng.nextDouble() < 0.92) {
            missingSounds.add("qu");
            Collections.addAll(forbidden, parts1.get("qu").split(" "));
            Collections.addAll(forbidden, parts2.get("qu").split(" "));
            Collections.addAll(forbidden, parts3.get("qu").split(" "));
            n++;
        }
        if (rng.nextDouble() < 0.96) {
            missingSounds.add("q");
            Collections.addAll(forbidden, parts1.get("q").split(" "));
            Collections.addAll(forbidden, parts2.get("q").split(" "));
            Collections.addAll(forbidden, parts3.get("q").split(" "));
            n++;
        }
        if (rng.nextDouble() < 0.97) {
            missingSounds.add("tl");
            Collections.addAll(forbidden, parts1.get("tl").split(" "));
            Collections.addAll(forbidden, parts2.get("tl").split(" "));
            Collections.addAll(forbidden, parts3.get("tl").split(" "));
            n++;
        }
        if (rng.nextDouble() < 0.86) {
            missingSounds.add("ph");
            Collections.addAll(forbidden, parts1.get("ph").split(" "));
            Collections.addAll(forbidden, parts2.get("ph").split(" "));
            Collections.addAll(forbidden, parts3.get("ph").split(" "));
            n++;
        }
        if (rng.nextDouble() < 0.94) {
            missingSounds.add("kh");
            Collections.addAll(forbidden, parts1.get("kh").split(" "));
            Collections.addAll(forbidden, parts2.get("kh").split(" "));
            Collections.addAll(forbidden, parts3.get("kh").split(" "));
            n++;
        }
        if (rng.nextDouble() < 0.96) {
            missingSounds.add("bh");
            missingSounds.add("dh");
            Collections.addAll(forbidden, parts1.get("bh").split(" "));
            Collections.addAll(forbidden, parts2.get("bh").split(" "));
            Collections.addAll(forbidden, parts3.get("bh").split(" "));
            Collections.addAll(forbidden, parts1.get("dh").split(" "));
            Collections.addAll(forbidden, parts2.get("dh").split(" "));
            Collections.addAll(forbidden, parts3.get("dh").split(" "));
            n++;
            n++;
        }

        for (; n < sz * diversity; n++) {
            missingSounds.add(parts1.keyAt(n));
            missingSounds.add(parts2.keyAt(n));
            missingSounds.add(parts3.keyAt(n));
            Collections.addAll(forbidden, parts1.getAt(n).split(" "));
            Collections.addAll(forbidden, parts2.getAt(n).split(" "));
            Collections.addAll(forbidden, parts3.getAt(n).split(" "));
        }

        return new FakeLanguageGen(
                processParts(parts0, missingSounds, forbidden, rng, 0.0, p0s),
                new String[]{"y", "y"},
                processParts(openCons, missingSounds, forbidden, rng, 0.0, 9999),
                processParts(midCons, missingSounds, forbidden, rng, (rng.nextDouble() * 3 - 0.75) / 2.25, 9999),
                processParts(closeCons, missingSounds, forbidden, rng, (rng.nextDouble() * 3 - 0.75) / 3.5, 9999),
                new String[]{},
                new String[]{}, lengths, chances, vowelHeavy, vowelHeavy * 1.8, 0.0, 0.0, genericSanityChecks, true);
    }

    protected boolean checkAll(CharSequence testing, Pattern[] checks) {
        CharSequence fixed = removeAccents(testing);
        for (int i = 0; i < checks.length; i++) {
            if (checks[i].matcher(fixed).find())
                return false;
        }
        return true;
    }

    /**
     * Generate a word from this FakeLanguageGen, using and changing the current seed.
     *
     * @param capitalize true if the word should start with a capital letter, false otherwise
     * @return a word in the fake language as a String
     */
    public String word(boolean capitalize) {
        return word(srng, capitalize);
    }

    /**
     * Generate a word from this FakeLanguageGen using the specified RNG.
     *
     * @param rng        the RNG to use for the randomized string building
     * @param capitalize true if the word should start with a capital letter, false otherwise
     * @return a word in the fake language as a String
     */
    public String word(RNG rng, boolean capitalize) {
        while (true) {
            StringBuilder sb = new StringBuilder(20), ender = new StringBuilder(12);
            double syllableChance = rng.nextDouble(totalSyllableFrequency);
            int syllables = 1, i = 0;
            for (IntDoubleOrderedMap.MapEntry kv : syllableFrequencies.mapEntrySet()) {
                if (syllableChance < kv.getDoubleValue()) {
                    syllables = kv.getIntKey();
                    break;
                } else
                    syllableChance -= kv.getDoubleValue();
            }
            if (rng.nextDouble() < vowelStartFrequency) {
                sb.append(rng.getRandomElement(openingVowels));
                if (syllables == 1)
                    sb.append(rng.getRandomElement(closingConsonants));
                else
                    sb.append(rng.getRandomElement(midConsonants));
                i++;
            } else {
                sb.append(rng.getRandomElement(openingConsonants));
            }
            String close = "";
            boolean redouble = false;
            if (i < syllables) {
                if (rng.nextDouble() < syllableEndFrequency) {
                    close = rng.getRandomElement(closingSyllables);
                    if (close.contains("@") && (syllables & 1) == 0) {
                        redouble = true;
                        syllables = (syllables | 1) >> 1;

                        //sb.append(close.replaceAll("@\\d", sb.toString()));
                    }
                    if (!close.contains("@"))
                        ender.append(close);
                    else if (rng.nextDouble() < vowelEndFrequency) {
                        ender.append(rng.getRandomElement(midVowels));
                        if (rng.nextDouble() < vowelSplitFrequency) {
                            ender.append(rng.getRandomElement(vowelSplitters));
                            ender.append(rng.getRandomElement(midVowels));
                        }
                    }
                } else {
                    ender.append(rng.getRandomElement(midVowels));
                    if (rng.nextDouble() < vowelSplitFrequency) {
                        ender.append(rng.getRandomElement(vowelSplitters));
                        ender.append(rng.getRandomElement(midVowels));
                    }
                    if (rng.nextDouble() >= vowelEndFrequency) {
                        ender.append(rng.getRandomElement(closingConsonants));
                        if (rng.nextDouble() < syllableEndFrequency) {
                            close = rng.getRandomElement(closingSyllables);
                            if (close.contains("@") && (syllables & 1) == 0) {
                                redouble = true;
                                syllables = (syllables | 1) >> 1;

                                //sb.append(close.replaceAll("@\\d", sb.toString()));
                            }
                            if (!close.contains("@"))
                                ender.append(close);
                        }
                    }
                }
                i += vowelClusters.matcher(ender).findAll().count();

            }

            for (; i < syllables; i++) {
                sb.append(rng.getRandomElement(midVowels));
                if (rng.nextDouble() < vowelSplitFrequency) {
                    sb.append(rng.getRandomElement(vowelSplitters));
                    sb.append(rng.getRandomElement(midVowels));
                }
                sb.append(rng.getRandomElement(midConsonants));
            }

            sb.append(ender);
            if (redouble && i <= syllables + 1) {
                sb.append(close.replaceAll("@", sb.toString()));
            }

            if (sanityChecks != null && !checkAll(sb, sanityChecks))
                continue;

            for (Modifier mod : modifiers) {
                sb = mod.modify(rng, sb);
            }

            if (capitalize)
                sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));

            if (clean && !checkAll(sb, vulgarChecks))
                continue;
            return sb.toString();
        }
    }

    /**
     * Generate a word from this FakeLanguageGen using the specified RNG.
     *
     * @param rng        the RNG to use for the randomized string building
     * @param capitalize true if the word should start with a capital letter, false otherwise
     * @return a word in the fake language as a String
     */
    public String word(RNG rng, boolean capitalize, int approxSyllables) {
        return word(rng, capitalize, approxSyllables, null);
    }
    /**
     * Generate a word from this FakeLanguageGen using the specified RNG.
     *
     * @param rng        the RNG to use for the randomized string building
     * @param capitalize true if the word should start with a capital letter, false otherwise
     * @return a word in the fake language as a String
     */
    public String word(RNG rng, boolean capitalize, int approxSyllables, Pattern[] additionalChecks) {
        if (approxSyllables <= 0) {
            String finished = rng.getRandomElement(openingVowels);
            if (capitalize) return finished.substring(0, 1).toUpperCase();
            else return finished.substring(0, 1);
        }
        while (true) {
            StringBuilder sb = new StringBuilder(20), ender = new StringBuilder(12);
            int i = 0;
            if (rng.nextDouble() < vowelStartFrequency) {
                sb.append(rng.getRandomElement(openingVowels));
                if (approxSyllables == 1 && closingConsonants.length > 0)
                    sb.append(rng.getRandomElement(closingConsonants));
                else if(midConsonants.length > 0)
                    sb.append(rng.getRandomElement(midConsonants));
                i++;
            } else if(openingConsonants.length > 0){
                sb.append(rng.getRandomElement(openingConsonants));
            }
            String close = "";
            boolean redouble = false;
            if (i < approxSyllables) {
                if (closingSyllables.length > 0 && rng.nextDouble() < syllableEndFrequency) {
                    close = rng.getRandomElement(closingSyllables);
                    if (close.contains("@") && (approxSyllables & 1) == 0) {
                        redouble = true;
                        approxSyllables = approxSyllables >> 1;

                        //sb.append(close.replaceAll("@\\d", sb.toString()));
                    }
                    if (!close.contains("@"))
                        ender.append(close);
                    else if (redouble && rng.nextDouble() < vowelEndFrequency) {
                        ender.append(rng.getRandomElement(midVowels));
                        if (vowelSplitters.length > 0 && rng.nextDouble() < vowelSplitFrequency) {
                            ender.append(rng.getRandomElement(vowelSplitters));
                            ender.append(rng.getRandomElement(midVowels));
                        }
                    }
                } else {
                    ender.append(rng.getRandomElement(midVowels));
                    if (rng.nextDouble() < vowelSplitFrequency) {
                        ender.append(rng.getRandomElement(vowelSplitters));
                        ender.append(rng.getRandomElement(midVowels));
                    }
                    if (rng.nextDouble() >= vowelEndFrequency) {
                        ender.append(rng.getRandomElement(closingConsonants));
                        if (rng.nextDouble() < syllableEndFrequency) {
                            close = rng.getRandomElement(closingSyllables);
                            if (close.contains("@") && (approxSyllables & 1) == 0) {
                                redouble = true;
                                approxSyllables = approxSyllables >> 1;

                                //sb.append(close.replaceAll("@\\d", sb.toString()));
                            }
                            if (!close.contains("@"))
                                ender.append(close);
                        }
                    }
                }
                i += vowelClusters.matcher(ender).findAll().count();
            }

            for (; i < approxSyllables; i++) {
                sb.append(rng.getRandomElement(midVowels));
                if (rng.nextDouble() < vowelSplitFrequency) {
                    sb.append(rng.getRandomElement(vowelSplitters));
                    sb.append(rng.getRandomElement(midVowels));
                }
                sb.append(rng.getRandomElement(midConsonants));
            }

            sb.append(ender);
            if (redouble && i <= approxSyllables + 1) {
                sb.append(close.replaceAll("@", sb.toString()));
            }

            if (sanityChecks != null && !checkAll(sb, sanityChecks))
                continue;

            for (Modifier mod : modifiers) {
                sb = mod.modify(rng, sb);
            }

            if (clean && !checkAll(sb, vulgarChecks))
                continue;

            if(additionalChecks != null && !checkAll(sb, additionalChecks))
                continue;

            if (capitalize)
                sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));

            return sb.toString();
        }
    }

    /**
     * Generate a word from this FakeLanguageGen using the specified RNG.
     *
     * @param rng        the RNG to use for the randomized string building
     * @param capitalize true if the word should start with a capital letter, false otherwise
     * @return a word in the fake language as a String
     */
    public String word(StatefulRNG rng, boolean capitalize, int approxSyllables, long... reseeds) {
        if (approxSyllables <= 0) {
            String finished = rng.getRandomElement(openingVowels);
            if (capitalize) return finished.substring(0, 1).toUpperCase();
            else return finished.substring(0, 1);
        }
        int numSeeds, fraction = 1;
        if(reseeds != null)
            numSeeds = Math.min(reseeds.length, approxSyllables-1);
        else numSeeds = 0;
        while (true) {
            StringBuilder sb = new StringBuilder(20), ender = new StringBuilder(12);
            int i = 0;
            if (rng.nextDouble() < vowelStartFrequency) {
                sb.append(rng.getRandomElement(openingVowels));
                if (approxSyllables == 1)
                    sb.append(rng.getRandomElement(closingConsonants));
                else
                    sb.append(rng.getRandomElement(midConsonants));
                i++;
            } else {
                sb.append(rng.getRandomElement(openingConsonants));
            }
            String close = "";
            boolean redouble = false;
            if (i < approxSyllables) {
                if(numSeeds > 0 && i > 0 && i == approxSyllables * fraction / (1+numSeeds))
                    rng.setState(reseeds[fraction++ - 1]);
                if (rng.nextDouble() < syllableEndFrequency) {
                    close = rng.getRandomElement(closingSyllables);
                    if (close.contains("@") && (approxSyllables & 1) == 0) {
                        redouble = true;
                        approxSyllables = approxSyllables >> 1;
                    }
                    if (!close.contains("@"))
                        ender.append(close);
                    else if (rng.nextDouble() < vowelEndFrequency) {
                        ender.append(rng.getRandomElement(midVowels));
                        if (rng.nextDouble() < vowelSplitFrequency) {
                            ender.append(rng.getRandomElement(vowelSplitters));
                            ender.append(rng.getRandomElement(midVowels));
                        }
                    }
                } else {
                    ender.append(rng.getRandomElement(midVowels));
                    if (rng.nextDouble() < vowelSplitFrequency) {
                        ender.append(rng.getRandomElement(vowelSplitters));
                        ender.append(rng.getRandomElement(midVowels));
                    }
                    if (rng.nextDouble() >= vowelEndFrequency) {
                        ender.append(rng.getRandomElement(closingConsonants));
                        if (rng.nextDouble() < syllableEndFrequency) {
                            close = rng.getRandomElement(closingSyllables);
                            if (close.contains("@") && (approxSyllables & 1) == 0) {
                                redouble = true;
                                approxSyllables = approxSyllables >> 1;

                                //sb.append(close.replaceAll("@\\d", sb.toString()));
                            }
                            if (!close.contains("@"))
                                ender.append(close);
                        }
                    }
                }
                i += vowelClusters.matcher(ender).findAll().count();
            }

            for (; i < approxSyllables; i++) {
                if(numSeeds > 0 && i > 0 && i == approxSyllables * fraction / (1+numSeeds))
                    rng.setState(reseeds[fraction++ - 1]);
                sb.append(rng.getRandomElement(midVowels));
                if (rng.nextDouble() < vowelSplitFrequency) {
                    sb.append(rng.getRandomElement(vowelSplitters));
                    sb.append(rng.getRandomElement(midVowels));
                }
                sb.append(rng.getRandomElement(midConsonants));
            }

            sb.append(ender);
            if (redouble && i <= approxSyllables + 1) {
                sb.append(close.replaceAll("@", sb.toString()));
            }

            if (sanityChecks != null && !checkAll(sb, sanityChecks))
                continue;

            for (Modifier mod : modifiers) {
                sb = mod.modify(rng, sb);
            }

            if (capitalize)
                sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));

            if (clean && !checkAll(sb, vulgarChecks))
                continue;
            return sb.toString();
        }
    }

    /**
     * Generate a sentence from this FakeLanguageGen, using and changing the current seed.
     *
     * @param minWords an int for the minimum number of words in a sentence; should be at least 1
     * @param maxWords an int for the maximum number of words in a sentence; should be at least equal to minWords
     * @return a sentence in the gibberish language as a String
     */
    public String sentence(int minWords, int maxWords) {
        return sentence(srng, minWords, maxWords, new String[]{",", ",", ",", ";"},
                new String[]{".", ".", ".", "!", "?", "..."}, 0.2);
    }

    /**
     * Generate a sentence from this FakeLanguageGen, using and changing the current seed.
     *
     * @param minWords                an int for the minimum number of words in a sentence; should be at least 1
     * @param maxWords                an int for the maximum number of words in a sentence; should be at least equal to minWords
     * @param midPunctuation          a String array where each element is a comma, semicolon, or the like that goes before a
     *                                space in the middle of a sentence
     * @param endPunctuation          a String array where each element is a period, question mark, or the like that goes at
     *                                the very end of a sentence
     * @param midPunctuationFrequency a double between 0.0 and 1.0 that determines how often Strings from
     *                                midPunctuation should be inserted before spaces
     * @return a sentence in the gibberish language as a String
     */
    public String sentence(int minWords, int maxWords, String[] midPunctuation, String[] endPunctuation,
                           double midPunctuationFrequency) {
        return sentence(srng, minWords, maxWords, midPunctuation, endPunctuation, midPunctuationFrequency);
    }

    /**
     * Generate a sentence from this FakeLanguageGen using the specific RNG.
     *
     * @param rng                     the RNG to use for the randomized string building
     * @param minWords                an int for the minimum number of words in a sentence; should be at least 1
     * @param maxWords                an int for the maximum number of words in a sentence; should be at least equal to minWords
     * @param midPunctuation          a String array where each element is a comma, semicolon, or the like that goes before a
     *                                space in the middle of a sentence
     * @param endPunctuation          a String array where each element is a period, question mark, or the like that goes at
     *                                the very end of a sentence
     * @param midPunctuationFrequency a double between 0.0 and 1.0 that determines how often Strings from
     *                                midPunctuation should be inserted before spaces
     * @return a sentence in the gibberish language as a String
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
        if(endPunctuation != null && endPunctuation.length > 0)
            sb.append(rng.getRandomElement(endPunctuation));
        return sb.toString();
    }

    /**
     * Generate a sentence from this FakeLanguageGen that fits in the given length limit..
     *
     * @param minWords                an int for the minimum number of words in a sentence; should be at least 1
     * @param maxWords                an int for the maximum number of words in a sentence; should be at least equal to minWords
     * @param midPunctuation          a String array where each element is a comma, semicolon, or the like that goes before a
     *                                space in the middle of a sentence
     * @param endPunctuation          a String array where each element is a period, question mark, or the like that goes at
     *                                the very end of a sentence
     * @param midPunctuationFrequency a double between 0.0 and 1.0 that determines how often Strings from
     *                                midPunctuation should be inserted before spaces
     * @param maxChars                the longest string length this can produce; should be at least {@code 6 * minWords}
     * @return a sentence in the gibberish language as a String
     */
    public String sentence(int minWords, int maxWords, String[] midPunctuation, String[] endPunctuation,
                           double midPunctuationFrequency, int maxChars) {
        return sentence(srng, minWords, maxWords, midPunctuation, endPunctuation, midPunctuationFrequency, maxChars);
    }

    /**
     * Generate a sentence from this FakeLanguageGen using the specific RNG that fits in the given length limit.
     *
     * @param rng                     the RNG to use for the randomized string building
     * @param minWords                an int for the minimum number of words in a sentence; should be at least 1
     * @param maxWords                an int for the maximum number of words in a sentence; should be at least equal to minWords
     * @param midPunctuation          a String array where each element is a comma, semicolon, or the like that goes before a
     *                                space in the middle of a sentence
     * @param endPunctuation          a String array where each element is a period, question mark, or the like that goes at
     *                                the very end of a sentence
     * @param midPunctuationFrequency a double between 0.0 and 1.0 that determines how often Strings from
     *                                midPunctuation should be inserted before spaces
     * @param maxChars                the longest string length this can produce; should be at least {@code 6 * minWords}
     * @return a sentence in the gibberish language as a String
     */
    public String sentence(RNG rng, int minWords, int maxWords, String[] midPunctuation, String[] endPunctuation,
                           double midPunctuationFrequency, int maxChars) {
        if (minWords < 1)
            minWords = 1;
        if (minWords > maxWords)
            maxWords = minWords;
        if (midPunctuationFrequency > 1.0) {
            midPunctuationFrequency = 1.0 / midPunctuationFrequency;
        }
        if (maxChars < 4)
            return "!";
        if (maxChars <= 5 * minWords) {
            minWords = 1;
            maxWords = 1;
        }
        int frustration = 0;
        StringBuilder sb = new StringBuilder(maxChars);
        String next = word(rng, true);
        while (next.length() >= maxChars - 1 && frustration < 50) {
            next = word(rng, true);
            frustration++;
        }
        if (frustration >= 50) return "!";
        sb.append(next);
        for (int i = 1; i < minWords && frustration < 50 && sb.length() < maxChars - 7; i++) {
            if (rng.nextDouble() < midPunctuationFrequency && sb.length() < maxChars - 3) {
                sb.append(rng.getRandomElement(midPunctuation));
            }
            next = word(rng, false);
            while (sb.length() + next.length() >= maxChars - 2 && frustration < 50) {
                next = word(rng, false);
                frustration++;
            }
            if (frustration >= 50) break;
            sb.append(' ');
            sb.append(next);
        }
        for (int i = minWords; i < maxWords && sb.length() < maxChars - 7 && rng.nextInt(2 * maxWords) > i && frustration < 50; i++) {
            if (rng.nextDouble() < midPunctuationFrequency && sb.length() < maxChars - 3) {
                sb.append(rng.getRandomElement(midPunctuation));
            }
            next = word(rng, false);
            while (sb.length() + next.length() >= maxChars - 2 && frustration < 50) {
                next = word(rng, false);
                frustration++;
            }
            if (frustration >= 50) break;
            sb.append(' ');
            sb.append(next);
        }

        if(endPunctuation != null && endPunctuation.length > 0)
            sb.append(rng.getRandomElement(endPunctuation));

        if (sb.length() + next.length() >= maxChars)
            next = ".";
        sb.append(next);
        if (sb.length() > maxChars)
            return "!";
        return sb.toString();
    }

    protected String[] merge1000(RNG rng, String[] me, String[] other, double otherInfluence) {
        if (other.length <= 0 && me.length <= 0)
            return new String[]{};
        String[] ret = new String[1000];
        int otherCount = (int) (1000 * otherInfluence);
        int idx = 0;
        if (other.length > 0) {
            String[] tmp = new String[other.length];
            rng.shuffle(other, tmp);
            for (idx = 0; idx < otherCount; idx++) {
                ret[idx] = tmp[idx % tmp.length];
            }
        }
        if (me.length > 0) {
            String[] tmp = new String[me.length];
            rng.shuffle(me, tmp);
            for (; idx < 1000; idx++) {
                ret[idx] = tmp[idx % tmp.length];
            }
        } else {
            for (; idx < 1000; idx++) {
                ret[idx] = other[idx % other.length];
            }
        }
        return ret;
    }


    protected String[] accentVowels(RNG rng, String[] me, double influence) {
        String[] ret = new String[1000];
        int otherCount = (int) (1000 * influence);
        int idx = 0;
        Matcher matcher;
        if (me.length > 0) {
            String[] tmp = new String[me.length];
            rng.shuffle(me, tmp);
            for (idx = 0; idx < otherCount; idx++) {
                ret[idx] = tmp[idx % tmp.length]
                        .replace('a', accentedVowels[0][rng.nextInt(accentedVowels[0].length)])
                        .replace('e', accentedVowels[1][rng.nextInt(accentedVowels[1].length)])
                        .replace('i', accentedVowels[2][rng.nextInt(accentedVowels[2].length)])
                        .replace('o', accentedVowels[3][rng.nextInt(accentedVowels[3].length)])
                        .replace('u', accentedVowels[4][rng.nextInt(accentedVowels[4].length)]);
                matcher = repeats.matcher(ret[idx]);
                if (matcher.find()) {
                    ret[idx] = matcher.replaceAll(rng.getRandomElement(me));
                }
            }
            for (; idx < 1000; idx++) {
                ret[idx] = tmp[idx % tmp.length];
            }
        } else
            return new String[]{};
        return ret;
    }

    protected String[] accentConsonants(RNG rng, String[] me, double influence) {
        String[] ret = new String[1000];
        int otherCount = (int) (1000 * influence);
        int idx = 0;
        Matcher matcher;
        if (me.length > 0) {
            String[] tmp = new String[me.length];
            rng.shuffle(me, tmp);
            for (idx = 0; idx < otherCount; idx++) {
                ret[idx] = tmp[idx % tmp.length]
                        //0
                        .replace('c', accentedConsonants[1][rng.nextInt(accentedConsonants[1].length)])
                        .replace('d', accentedConsonants[2][rng.nextInt(accentedConsonants[2].length)])
                        .replace('f', accentedConsonants[3][rng.nextInt(accentedConsonants[3].length)])
                        .replace('g', accentedConsonants[4][rng.nextInt(accentedConsonants[4].length)])
                        .replace('h', accentedConsonants[5][rng.nextInt(accentedConsonants[5].length)])
                        .replace('j', accentedConsonants[6][rng.nextInt(accentedConsonants[6].length)])
                        .replace('k', accentedConsonants[7][rng.nextInt(accentedConsonants[7].length)])
                        .replace('l', accentedConsonants[8][rng.nextInt(accentedConsonants[8].length)])
                        //9
                        .replace('n', accentedConsonants[10][rng.nextInt(accentedConsonants[10].length)])
                        //11
                        //12
                        .replace('r', accentedConsonants[13][rng.nextInt(accentedConsonants[13].length)])
                        .replace('s', accentedConsonants[14][rng.nextInt(accentedConsonants[14].length)])
                        .replace('t', accentedConsonants[15][rng.nextInt(accentedConsonants[15].length)])
                        //16
                        .replace('w', accentedConsonants[17][rng.nextInt(accentedConsonants[17].length)])
                        //18
                        .replace('y', accentedConsonants[19][rng.nextInt(accentedConsonants[19].length)])
                        .replace('z', accentedConsonants[20][rng.nextInt(accentedConsonants[20].length)]);

                matcher = repeats.matcher(ret[idx]);
                if (matcher.find()) {
                    ret[idx] = matcher.replaceAll(rng.getRandomElement(me));
                }
            }
            for (; idx < 1000; idx++) {
                ret[idx] = tmp[idx % tmp.length];
            }
        } else
            return new String[]{};
        return ret;
    }

    protected String[] accentBoth(RNG rng, String[] me, double vowelInfluence, double consonantInfluence) {
        String[] ret = new String[1000];
        int idx = 0;
        Matcher matcher;
        if (me.length > 0) {
            String[] tmp = new String[me.length];
            rng.shuffle(me, tmp);
            for (idx = 0; idx < 1000; idx++) {
                boolean subVowel = rng.nextDouble() < vowelInfluence, subCon = rng.nextDouble() < consonantInfluence;
                if (subVowel && subCon) {
                    ret[idx] = tmp[idx % tmp.length]
                            .replace('a', accentedVowels[0][rng.nextInt(accentedVowels[0].length)])
                            .replace('e', accentedVowels[1][rng.nextInt(accentedVowels[1].length)])
                            .replace('i', accentedVowels[2][rng.nextInt(accentedVowels[2].length)])
                            .replace('o', accentedVowels[3][rng.nextInt(accentedVowels[3].length)])
                            .replace('u', accentedVowels[4][rng.nextInt(accentedVowels[4].length)])

                            //0
                            .replace('c', accentedConsonants[1][rng.nextInt(accentedConsonants[1].length)])
                            .replace('d', accentedConsonants[2][rng.nextInt(accentedConsonants[2].length)])
                            .replace('f', accentedConsonants[3][rng.nextInt(accentedConsonants[3].length)])
                            .replace('g', accentedConsonants[4][rng.nextInt(accentedConsonants[4].length)])
                            .replace('h', accentedConsonants[5][rng.nextInt(accentedConsonants[5].length)])
                            .replace('j', accentedConsonants[6][rng.nextInt(accentedConsonants[6].length)])
                            .replace('k', accentedConsonants[7][rng.nextInt(accentedConsonants[7].length)])
                            .replace('l', accentedConsonants[8][rng.nextInt(accentedConsonants[8].length)])
                            //9
                            .replace('n', accentedConsonants[10][rng.nextInt(accentedConsonants[10].length)])
                            //11
                            //12
                            .replace('r', accentedConsonants[13][rng.nextInt(accentedConsonants[13].length)])
                            .replace('s', accentedConsonants[14][rng.nextInt(accentedConsonants[14].length)])
                            .replace('t', accentedConsonants[15][rng.nextInt(accentedConsonants[15].length)])
                            //16
                            .replace('w', accentedConsonants[17][rng.nextInt(accentedConsonants[17].length)])
                            //18
                            .replace('y', accentedConsonants[19][rng.nextInt(accentedConsonants[19].length)])
                            .replace('z', accentedConsonants[20][rng.nextInt(accentedConsonants[20].length)]);

                    matcher = repeats.matcher(ret[idx]);
                    if (matcher.find()) {
                        ret[idx] = matcher.replaceAll(rng.getRandomElement(me));
                    }
                } else if (subVowel) {
                    ret[idx] = tmp[idx % tmp.length]
                            .replace('a', accentedVowels[0][rng.nextInt(accentedVowels[0].length)])
                            .replace('e', accentedVowels[1][rng.nextInt(accentedVowels[1].length)])
                            .replace('i', accentedVowels[2][rng.nextInt(accentedVowels[2].length)])
                            .replace('o', accentedVowels[3][rng.nextInt(accentedVowels[3].length)])
                            .replace('u', accentedVowels[4][rng.nextInt(accentedVowels[4].length)]);

                    matcher = repeats.matcher(ret[idx]);
                    if (matcher.find()) {
                        ret[idx] = matcher.replaceAll(rng.getRandomElement(me));
                    }
                } else if (subCon) {
                    ret[idx] = tmp[idx % tmp.length]
                            //0
                            .replace('c', accentedConsonants[1][rng.nextInt(accentedConsonants[1].length)])
                            .replace('d', accentedConsonants[2][rng.nextInt(accentedConsonants[2].length)])
                            .replace('f', accentedConsonants[3][rng.nextInt(accentedConsonants[3].length)])
                            .replace('g', accentedConsonants[4][rng.nextInt(accentedConsonants[4].length)])
                            .replace('h', accentedConsonants[5][rng.nextInt(accentedConsonants[5].length)])
                            .replace('j', accentedConsonants[6][rng.nextInt(accentedConsonants[6].length)])
                            .replace('k', accentedConsonants[7][rng.nextInt(accentedConsonants[7].length)])
                            .replace('l', accentedConsonants[8][rng.nextInt(accentedConsonants[8].length)])
                            //9
                            .replace('n', accentedConsonants[10][rng.nextInt(accentedConsonants[10].length)])
                            //11
                            //12
                            .replace('r', accentedConsonants[13][rng.nextInt(accentedConsonants[13].length)])
                            .replace('s', accentedConsonants[14][rng.nextInt(accentedConsonants[14].length)])
                            .replace('t', accentedConsonants[15][rng.nextInt(accentedConsonants[15].length)])
                            //16
                            .replace('w', accentedConsonants[17][rng.nextInt(accentedConsonants[17].length)])
                            //18
                            .replace('y', accentedConsonants[19][rng.nextInt(accentedConsonants[19].length)])
                            .replace('z', accentedConsonants[20][rng.nextInt(accentedConsonants[20].length)]);

                    matcher = repeats.matcher(ret[idx]);
                    if (matcher.find()) {
                        ret[idx] = matcher.replaceAll(rng.getRandomElement(me));
                    }
                } else ret[idx] = tmp[idx % tmp.length];

            }
        } else
            return new String[]{};
        return ret;
    }

    public FakeLanguageGen mix(FakeLanguageGen other, double otherInfluence) {
        otherInfluence = Math.max(0.0, Math.min(otherInfluence, 1.0));
        double myInfluence = 1.0 - otherInfluence;

        RNG rng = new RNG((hashCode() & 0xffffffffL) | ((other.hashCode() & 0xffffffffL) << 32)
                ^ Double.doubleToLongBits(otherInfluence));

        String[] ov = merge1000(rng, openingVowels, other.openingVowels, otherInfluence),
                mv = merge1000(rng, midVowels, other.midVowels, otherInfluence),
                oc = merge1000(rng, openingConsonants, other.openingConsonants, otherInfluence *
                        Math.max(0.0, Math.min(1.0, (1.0 - other.vowelStartFrequency + vowelStartFrequency)))),
                mc = merge1000(rng, midConsonants, other.midConsonants, otherInfluence),
                cc = merge1000(rng, closingConsonants, other.closingConsonants, otherInfluence *
                        Math.max(0.0, Math.min(1.0, (1.0 - other.vowelEndFrequency + vowelEndFrequency)))),
                cs = merge1000(rng, closingSyllables, other.closingSyllables, otherInfluence *
                        Math.max(0.0, Math.min(1.0, (other.syllableEndFrequency - syllableEndFrequency)))),
                splitters = merge1000(rng, vowelSplitters, other.vowelSplitters, otherInfluence);

        IntDoubleOrderedMap freqs = new IntDoubleOrderedMap(syllableFrequencies);
        for (IntDoubleOrderedMap.MapEntry kv : other.syllableFrequencies.mapEntrySet()) {
            if (freqs.containsKey(kv.getIntKey()))
                freqs.put(kv.getIntKey(), kv.getDoubleValue() + freqs.get(kv.getIntKey()));
            else
                freqs.put(kv.getIntKey(), kv.getDoubleValue());
        }
        List<Modifier> mods = new ArrayList<>((int) (Math.ceil(modifiers.size() * myInfluence) +
                Math.ceil(other.modifiers.size() * otherInfluence)));
        mods.addAll(rng.randomPortion(modifiers, (int) Math.ceil(modifiers.size() * myInfluence)));
        mods.addAll(rng.randomPortion(other.modifiers, (int) Math.ceil(other.modifiers.size() * otherInfluence)));
        FakeLanguageGen finished = new FakeLanguageGen(ov, mv, oc, mc, cc, cs, splitters, freqs,
                vowelStartFrequency * myInfluence + other.vowelStartFrequency * otherInfluence,
                vowelEndFrequency * myInfluence + other.vowelEndFrequency * otherInfluence,
                vowelSplitFrequency * myInfluence + other.vowelSplitFrequency * otherInfluence,
                syllableEndFrequency * myInfluence + other.syllableEndFrequency * otherInfluence,
                (sanityChecks == null) ? other.sanityChecks : sanityChecks, true, mods);
        return finished;
    }

    public FakeLanguageGen addAccents(double vowelInfluence, double consonantInfluence) {
        vowelInfluence = Math.max(0.0, Math.min(vowelInfluence, 1.0));
        consonantInfluence = Math.max(0.0, Math.min(consonantInfluence, 1.0));

        RNG rng = new RNG((hashCode() & 0xffffffffL) ^
                ((Double.doubleToLongBits(vowelInfluence) & 0xffffffffL) | (Double.doubleToLongBits(consonantInfluence) << 32)));
        String[] ov = accentVowels(rng, openingVowels, vowelInfluence),
                mv = accentVowels(rng, midVowels, vowelInfluence),
                oc = accentConsonants(rng, openingConsonants, consonantInfluence),
                mc = accentConsonants(rng, midConsonants, consonantInfluence),
                cc = accentConsonants(rng, closingConsonants, consonantInfluence),
                cs = accentBoth(rng, closingSyllables, vowelInfluence, consonantInfluence);


        FakeLanguageGen finished = new FakeLanguageGen(ov, mv, oc, mc, cc, cs, vowelSplitters, syllableFrequencies,
                vowelStartFrequency,
                vowelEndFrequency,
                vowelSplitFrequency,
                syllableEndFrequency, sanityChecks, clean, modifiers);
        return finished;
    }

    static String[] copyStrings(String[] start) {
        String[] next = new String[start.length];
        System.arraycopy(start, 0, next, 0, start.length);
        return next;
    }

    public FakeLanguageGen removeAccents() {

        String[] ov = copyStrings(openingVowels),
                mv = copyStrings(midVowels),
                oc = copyStrings(openingConsonants),
                mc = copyStrings(midConsonants),
                cc = copyStrings(closingConsonants),
                cs = copyStrings(closingSyllables);
        for (int i = 0; i < ov.length; i++) {
            ov[i] = removeAccents(openingVowels[i]).toString();
        }
        for (int i = 0; i < mv.length; i++) {
            mv[i] = removeAccents(midVowels[i]).toString();
        }
        for (int i = 0; i < oc.length; i++) {
            oc[i] = removeAccents(openingConsonants[i]).toString();
        }
        for (int i = 0; i < mc.length; i++) {
            mc[i] = removeAccents(midConsonants[i]).toString();
        }
        for (int i = 0; i < cc.length; i++) {
            cc[i] = removeAccents(closingConsonants[i]).toString();
        }
        for (int i = 0; i < cs.length; i++) {
            cs[i] = removeAccents(closingSyllables[i]).toString();
        }

        return new FakeLanguageGen(ov, mv, oc, mc, cc, cs, vowelSplitters, syllableFrequencies,
                vowelStartFrequency,
                vowelEndFrequency,
                vowelSplitFrequency,
                syllableEndFrequency, sanityChecks, clean, modifiers);
    }

    /**
     * Adds the specified Modifier objects from a Collection to a copy of this FakeLanguageGen and returns it.
     * You can obtain a Modifier with the static constants in the FakeLanguageGen.Modifier nested class, the
     * FakeLanguageGen.modifier() method, or Modifier's constructor.
     *
     * @param mods an array or vararg of Modifier objects
     * @return a copy of this with the Modifiers added
     */
    public FakeLanguageGen addModifiers(Collection<Modifier> mods) {
        FakeLanguageGen next = copy();
        next.modifiers.addAll(mods);
        return next;
    }

    /**
     * Adds the specified Modifier objects to a copy of this FakeLanguageGen and returns it.
     * You can obtain a Modifier with the static constants in the FakeLanguageGen.Modifier nested class, the
     * FakeLanguageGen.modifier() method, or Modifier's constructor.
     *
     * @param mods an array or vararg of Modifier objects
     * @return a copy of this with the Modifiers added
     */
    public FakeLanguageGen addModifiers(Modifier... mods) {
        FakeLanguageGen next = copy();
        Collections.addAll(next.modifiers, mods);
        return next;
    }

    /**
     * Creates a copy of this FakeLanguageGen with no modifiers.
     *
     * @return a copy of this FakeLanguageGen with modifiers removed.
     */
    public FakeLanguageGen removeModifiers() {
        FakeLanguageGen next = copy();
        next.modifiers.clear();
        return next;
    }

    public static Modifier modifier(String pattern, String replacement) {
        return new Modifier(pattern, replacement);
    }

    public static Modifier modifier(String pattern, String replacement, double chance) {
        return new Modifier(pattern, replacement, chance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FakeLanguageGen that = (FakeLanguageGen) o;

        if (clean != that.clean) return false;
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
        if (!syllableFrequencies.equals(that.syllableFrequencies)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(sanityChecks, that.sanityChecks)) return false;
        return modifiers != null ? modifiers.equals(that.modifiers) : that.modifiers == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = CrossHash.hash(openingVowels);
        result = 31 * result + CrossHash.hash(midVowels);
        result = 31 * result + CrossHash.hash(openingConsonants);
        result = 31 * result + CrossHash.hash(midConsonants);
        result = 31 * result + CrossHash.hash(closingConsonants);
        result = 31 * result + CrossHash.hash(vowelSplitters);
        result = 31 * result + CrossHash.hash(closingSyllables);
        result = 31 * result + (clean ? 1 : 0);
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
        result = 31 * result + (sanityChecks != null ? sanityChecks.length + 1 : 0);
        result = 31 * result + (modifiers != null ? modifiers.hashCode() : 0);
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
                ", clean=" + clean +
                ", syllableFrequencies=" + syllableFrequencies +
                ", totalSyllableFrequency=" + totalSyllableFrequency +
                ", vowelStartFrequency=" + vowelStartFrequency +
                ", vowelEndFrequency=" + vowelEndFrequency +
                ", vowelSplitFrequency=" + vowelSplitFrequency +
                ", syllableEndFrequency=" + syllableEndFrequency +
                ", sanityChecks=" + Arrays.toString(sanityChecks) +
                ", modifiers=" + modifiers +
                '}';
    }

    public FakeLanguageGen copy() {
        return new FakeLanguageGen(openingVowels, midVowels, openingConsonants, midConsonants,
                closingConsonants, closingSyllables, vowelSplitters, syllableFrequencies, vowelStartFrequency,
                vowelEndFrequency, vowelSplitFrequency, syllableEndFrequency, sanityChecks, clean, modifiers);
    }

    public static class Modifier implements Serializable {
        private static final long serialVersionUID = 1734863678490422371L;
        public final Alteration[] alterations;

        public Modifier() {
            this("[tţťțṭ]?[sśŝşšș]+h?", "th");
        }

        public Modifier(String pattern, String replacement) {
            alterations = new Alteration[]{new Alteration(pattern, replacement)};
        }

        public Modifier(String pattern, String replacement, double chance) {
            alterations = new Alteration[]{new Alteration(pattern, replacement, chance)};
        }

        public Modifier(Alteration... alts) {
            alterations = (alts == null) ? new Alteration[0] : alts;
        }

        public StringBuilder modify(RNG rng, StringBuilder sb) {
            Matcher m;
            Replacer.StringBuilderBuffer tb, working = Replacer.wrap(sb);
            String tmp;
            boolean found;
            for (Alteration alt : alterations) {
                tmp = working.toString();
                tb = Replacer.wrap(new StringBuilder(tmp.length()));
                m = alt.replacer.getPattern().matcher(tmp);

                found = false;
                while (true) {
                    if (rng.nextDouble() < alt.chance) {
                        if (!Replacer.replaceStep(m, alt.replacer.getSubstitution(), tb))
                            break;
                        found = true;
                    } else {
                        if (!m.find())
                            break;
                        found = true;
                        m.getGroup(MatchResult.PREFIX, tb);
                        m.getGroup(MatchResult.MATCH, tb);
                        m.setTarget(m, MatchResult.SUFFIX);
                    }
                }
                if (found) {
                    m.getGroup(MatchResult.TARGET, tb);
                    working = tb;
                }
            }
            return working.toStringBuilder();
        }

        /**
         * For a character who always pronounces 's', 'ss', and 'sh' as 'th'.
         */
        public static final Modifier LISP = new Modifier("[tţťțṭ]?[sśŝşšș]+h?", "th");

        /**
         * For a character who always lengthens 's' and 'z' sounds not starting a word.
         */
        public static final Modifier HISS = new Modifier("(.)([sśŝşšșzźżž])+", "$1$2$2$2");

        /**
         * For a character who has a 20% chance to repeat a starting consonant or vowel.
         */
        public static final Modifier STUTTER = new Modifier(
                new Alteration("^([^aàáâãäåæāăąǻǽeèéêëēĕėęěiìíîïĩīĭįıoòóôõöøōŏőœǿuùúûüũūŭůűųyýÿŷỳαοειυаеёийъыэюяоу]+)", "$1-$1", 0.2),
                new Alteration("^([aàáâãäåæāăąǻǽeèéêëēĕėęěiìíîïĩīĭįıoòóôõöøōŏőœǿuùúûüũūŭůűųαοειυаеёийъыэюяоу]+)", "$1-$1", 0.2));

        /**
         * For a language that has a 40% chance to repeat a single Latin vowel (a, e, o, or a variant on one of them
         * like å or ö, but not merged letters like æ and œ).
         */
        public static final Modifier DOUBLE_VOWELS = new Modifier(
                "([^aàáâãäåæāăąǻǽeèéêëēĕėęěiìíîïĩīĭįıoòóôõöøōŏőœǿuùúûüũūŭůűųyýÿŷỳ]|^)"
                        + "([aàáâãäåāăąǻeèéêëēĕėęěòóôõöøōŏőǿ])"
                        + "([^aàáâãäåæāăąǻǽeèéêëēĕėęěiìíîïĩīĭįıoòóôõöøōŏőœǿuùúûüũūŭůűųyýÿŷỳ]|$)", "$1$2$2$3", 0.4);


        /**
         * For a language that has a 50% chance to repeat a single consonant.
         */
        public static final Modifier DOUBLE_CONSONANTS = new Modifier("([aàáâãäåæāăąǻǽeèéêëēĕėęěiìíîïĩīĭįıoòóôõöøōŏőœǿuùúûüũūŭůűųyýÿŷỳαοειυаеёийъыэюяоу])" +
                "([^aàáâãäåæāăąǻǽeèéêëēĕėęěiìíîïĩīĭįıoòóôõöøōŏőœǿuùúûüũūŭůűųyýÿŷỳαοειυаеёийъыэюяоуqwhjx])" +
                "([aàáâãäåæāăąǻǽeèéêëēĕėęěiìíîïĩīĭįıoòóôõöøōŏőœǿuùúûüũūŭůűųyýÿŷỳαοειυаеёийъыэюяоу]|$)", "$1$2$2$3", 0.5);

        /**
         * For a language that never repeats the same letter twice in a row.
         */
        public static final Modifier NO_DOUBLES = new Modifier("(.)\\1", "$1");

        /**
         * Creates a Modifier that will replace the nth char in initial with the nth char in change. Expects initial and
         * change to be the same length, but will use the lesser length if they are not equal-length. Because of the
         * state of the text at the time modifiers are run, only lower-case letters need to be searched for.
         *
         * @param initial a String containing lower-case letters or other symbols to be swapped out of a text
         * @param change  a String containing characters that will replace occurrences of characters in initial
         * @return a Modifier that can be added to a FakeLanguageGen with its addModifiers() method
         */
        public static Modifier replacementTable(String initial, String change) {
            Alteration[] alts = new Alteration[Math.min(initial.length(), change.length())];
            for (int i = 0; i < alts.length; i++) {
                //literal string syntax; avoids sensitive escaping issues and also doesn't need a character class,
                // which is slightly slower and has some odd escaping cases.
                alts[i] = new Alteration("\\Q" + initial.charAt(i), change.substring(i, i + 1));
            }
            return new Modifier(alts);
        }

        public static final Modifier SIMPLIFY_ARABIC = new Modifier(
                new Alteration("ţ", "th"),
                new Alteration("ĥ", "kh"),
                new Alteration("ħ", "khr"),
                new Alteration("đ", "dh"),
                new Alteration("ď", "dt"),
                new Alteration("š", "sh"),
                new Alteration("ş", "shw"),
                new Alteration("ť", "ch"),
                new Alteration("ż", "zh"),
                new Alteration("ξ", "ql"),
                new Alteration("δ", "qh"),
                new Alteration("ġ", "gh"),
                new Alteration("ā", "aa"),
                new Alteration("ū", "uu"),
                new Alteration("ī", "ii"));

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Modifier modifier = (Modifier) o;

            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(alterations, modifier.alterations);
        }

        @Override
        public int hashCode() {
            return CrossHash.Lightning.hash(alterations);
        }

        @Override
        public String toString() {
            return "Modifier{" +
                    "alterations=" + Arrays.toString(alterations) +
                    '}';
        }
    }

    public static class Alteration implements Serializable {
        private static final long serialVersionUID = -2138854697837563188L;
        public Replacer replacer;
        public double chance;

        public Alteration() {
            this("[tţťțṭ]?[sśŝşšș]+h?", "th");
        }

        public Alteration(String pattern, String replacement) {
            replacer = Pattern.compile(pattern).replacer(replacement);
            chance = 1.0;
        }

        public Alteration(String pattern, String replacement, double chance) {
            replacer = Pattern.compile(pattern).replacer(replacement);
            this.chance = chance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Alteration that = (Alteration) o;

            if (Double.compare(that.chance, chance) != 0) return false;
            return replacer.equals(that.replacer);

        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = replacer.hashCode();
            temp = Double.doubleToLongBits(chance);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "Alteration{" +
                    "replacer=" + replacer +
                    ", chance=" + chance +
                    '}';
        }
    }

}
