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
    public final boolean clean;
    public final IntDoubleOrderedMap syllableFrequencies;
    protected double totalSyllableFrequency = 0.0;
    public final double vowelStartFrequency, vowelEndFrequency, vowelSplitFrequency, syllableEndFrequency;
    public final Pattern[] sanityChecks;
    public ArrayList<Modifier> modifiers;
    public static final StatefulRNG srng = new StatefulRNG();
    private static final OrderedSet<FakeLanguageGen> registry = new OrderedSet<>(32);
    protected String summary = null;
    /**
     * A pattern String that will match any vowel FakeLanguageGen can produce out-of-the-box, including Latin, Greek,
     * and Cyrillic; for use when a String will be interpreted as a regex ({@link FakeLanguageGen.Alteration}).
     */
    public static final String anyVowel = "[àáâãäåæāăąǻǽaèéêëēĕėęěeìíîïĩīĭįıiòóôõöøōŏőœǿoùúûüũūŭůűųuýÿŷỳyαοειυаеёийоуъыэюя]",
    /**
     * A pattern String that will match one or more of any vowels FakeLanguageGen can produce out-of-the-box, including
     * Latin, Greek, and Cyrillic; for use when a String will be interpreted as a regex
     * ({@link FakeLanguageGen.Alteration}).
     */
    anyVowelCluster = anyVowel + '+',
    /**
     * A pattern String that will match any consonant FakeLanguageGen can produce out-of-the-box, including Latin,
     * Greek, and Cyrillic; for use when a String will be interpreted as a regex ({@link FakeLanguageGen.Alteration}).
     */
    anyConsonant = "[bcçćĉċčdþðďđfgĝğġģhĥħjĵȷkķlĺļľŀłmnñńņňŋpqrŕŗřsśŝşšștţťțvwŵẁẃẅxyýÿŷỳzźżžṛṝḷḹḍṭṅṇṣṃḥρσζτκχνθμπψβλγφξςбвгдклпрстфхцжмнзчшщ]",
    /**
     * A pattern String that will match one or more of any consonants FakeLanguageGen can produce out-of-the-box,
     * including Latin, Greek, and Cyrillic; for use when a String will be interpreted as a regex
     * ({@link FakeLanguageGen.Alteration}).
     */
    anyConsonantCluster = anyConsonant + '+';
    protected static final Pattern repeats = Pattern.compile("(.)\\1+"),
            vowelClusters = Pattern.compile(anyVowelCluster, REFlags.IGNORE_CASE | REFlags.UNICODE),
            consonantClusters = Pattern.compile(anyConsonantCluster, REFlags.IGNORE_CASE | REFlags.UNICODE);
    //latin
    //àáâãäåæāăąǻǽaèéêëēĕėęěeìíîïĩīĭįıiòóôõöøōŏőœǿoùúûüũūŭůűųuýÿŷỳybcçćĉċčdþðďđfgĝğġģhĥħjĵȷkķlĺļľŀłmnñńņňŋpqrŕŗřsśŝşšștţťțvwŵẁẃẅxyýÿŷỳzźżžṛṝḷḹḍṭṅṇṣṃḥ
    //ÀÁÂÃÄÅÆĀĂĄǺǼAÈÉÊËĒĔĖĘĚEÌÍÎÏĨĪĬĮIIÒÓÔÕÖØŌŎŐŒǾOÙÚÛÜŨŪŬŮŰŲUÝŸŶỲYBCÇĆĈĊČDÞÐĎĐFGĜĞĠĢHĤĦJĴȷKĶLĹĻĽĿŁMNÑŃŅŇŊPQRŔŖŘSŚŜŞŠȘTŢŤȚVWŴẀẂẄXYÝŸŶỲZŹŻŽṚṜḶḸḌṬṄṆṢṂḤ
    //greek
    //αοειυρσζτκχνθμπψβλγφξς
    //ΑΟΕΙΥΡΣΖΤΚΧΝΘΜΠΨΒΛΓΦΞ
    //cyrillic
    //аеёийоуъыэюябвгдклпрстфхцжмнзчшщ
    //АЕЁИЙОУЪЫЭЮЯБВГДКЛПРСТФХЦЖМНЗЧШЩ

    static final Pattern[]
            vulgarChecks = new Pattern[]
            {
                    //17 is REFlags.UNICODE | REFlags.IGNORE_CASE
                    Pattern.compile("[sξζzkкκcсς][hнlι].{1,3}[dtтτΓг]", 17),
                    Pattern.compile("(?:(?:[pрρ][hн])|[fd]).{1,3}[kкκcсςxхжχq]", 17), // lots of these end in a 'k' sound, huh
                    Pattern.compile("[kкκcсςСQq][uμυνvhн]{1,3}[kкκcсςxхжχqmм]", 17),
                    Pattern.compile("[bъыбвβЪЫБ].?[iτιyуλγУ].?[cсς]", 17),
                    Pattern.compile("[hн][^aаαΛeезξεЗΣiτιyуλγУ][^aаαΛeезξεЗΣiτιyуλγУ]?[rяΓ]", 17),
                    Pattern.compile("[tтτΓгcсς][iτιyуλγУ][tтτΓг]+$", 17),
                    Pattern.compile("(?:(?:[pрρ][hн])|f)[aаαΛhн]{1,}[rяΓ][tтτΓг]", 17),
                    Pattern.compile("[Ssξζzcсς][hн][iτιyуλγУ].?[sξζzcсς]", 17),
                    Pattern.compile("[aаαΛ][nи][aаαΛeезξεЗΣiτιyуλγУoоюσοuμυνv]{1,2}[Ssξlιζz]", 17),
                    Pattern.compile("[aаαΛ]([sξζz]{2})", 17),
                    Pattern.compile("[kкκcсςСQq][hн]?[uμυνv]([hн]?)[nи]+[tтτΓг]", 17),
                    Pattern.compile("[nиfvν]..?[jg]", 17), // might as well remove two possible slurs and a body part with one check
                    Pattern.compile("[pрρ](?:(?:([eезξεЗΣoоюσοuμυνv])\\1)|(?:[eезξεЗΣiτιyуλγУuμυνv]+[sξζz]))", 17), // the grab bag of juvenile words
                    Pattern.compile("[mм][hнwψшщ]?..?[rяΓ].?d", 17), // should pick up the #1 obscenity from Spanish and French
                    Pattern.compile("[g][hн]?[aаαАΑΛeеёзξεЕЁЗΕΣ][yуλγУeеёзξεЕЁЗΕΣ]", 17), // could be inappropriate for random text
                    Pattern.compile("[wψшщuμυνv](?:[hн]?)[aаαΛeеёзξεЗΕΣoоюσοuμυνv](?:[nи]+)[gkкκcсςxхжχq]", 17)
            },
            genericSanityChecks = new Pattern[]
                    {
                            Pattern.compile("[AEIOUaeiou]{3}"),
                            Pattern.compile("(\\p{L})\\1\\1"),
                            Pattern.compile("[Ii][iyqhl]"),
                            Pattern.compile("[Yy]([aiu])\\1"),
                            Pattern.compile("[Rr][aeiouy]+[rh]"),
                            Pattern.compile("[Qq]u[yu]"),
                            Pattern.compile("[^oaei]uch"),
                            Pattern.compile("[Hh][tcszi]?h"),
                            Pattern.compile("[Tt]t[^aeiouy]{2}"),
                            Pattern.compile("[Yy]h([^aeiouy]|$)"),
                            Pattern.compile("([xqy])\\1$"),
                            Pattern.compile("[qi]y$"),
                            Pattern.compile("[szSZrlRL]+?[^aeiouytdfgkcpbmnslrv][rlsz]"),
                            Pattern.compile("[UIuiYy][wy]"),
                            Pattern.compile("^[UIui][ae]"),
                            Pattern.compile("^([^aeiouyl])\\1", 17)
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
                            Pattern.compile("[szSZrlRL]+?[^aeiouytdfgkcpbmnslr][rlsz]"),
                            Pattern.compile("[UIuiYy][wy]"),
                            Pattern.compile("^[UIui][ae]"),
                            Pattern.compile("q(?:u?)$")
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
                            Pattern.compile("(.)\\1\\1"),
                            Pattern.compile("-[^aeiou](?:[^aeiou]|$)"),
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
    private static final OrderedMap<String, String> openVowels,
            openCons, midCons, closeCons;

    static {

        registry.add(null);
        
        openVowels = Maker.makeOM(
        "a", "a aa ae ai au ea ia oa ua",
        "e", "e ae ea ee ei eo eu ie ue",
        "i", "i ai ei ia ie io iu oi ui",
        "o", "o eo io oa oi oo ou",
        "u", "u au eu iu ou ua ue ui");

        openCons = Maker.makeOM(
                "b", "b bl br by bw bh",
                "bh", "bh",
        "c", "c cl cr cz cth sc scl",
        "ch", "ch ch chw",
        "d", "d dr dz dy dw dh",
        "dh", "dh",
        "f", "f fl fr fy fw sf",
        "g", "g gl gr gw gy gn",
        "h", "bh cth ch ch chw dh h hm hy hw kh khl khw ph phl phr sh shl shqu shk shp shm shn shr shw shpl th th thr thl thw",
        "j", "j j",
        "k", "k kr kl ky kn sk skl shk",
        "kh", "kh khl khw",
        "l", "bl cl fl gl kl khl l pl phl scl skl spl sl shl shpl tl thl vl zl",
        "m", "hm m mr mw my sm smr shm",
        "n", "gn kn n nw ny pn sn shn",
        "p", "p pl pr py pw pn sp spr spl shp shpl ph phl phr",
        "ph", "ph phl phr",
        "q", "q",
        "qu", "qu squ shqu",
        "r", "br cr dr fr gr kr mr pr phr r str spr smr shr tr thr vr wr zvr",
        "s", "s sc scl sf sk skl st str sp spr spl sl sm smr sn sw sy squ ts sh shl shqu shk shp shm shn shr shw shpl",
        "sh", "sh shl shqu shk shp shm shn shr shw shpl",
        "t", "st str t ts tr tl ty tw tl",
        "th", "cth th thr thl thw",
        "tl", "tl",
        "v", "v vr vy zv zvr vl",
        "w", "bw chw dw fw gw hw khw mw nw pw sw shw tw thw w wr zw",
        "x", "x",
        "y", "by dy fy gy hy ky my ny py sy ty vy y zy",
        "z", "cz dz z zv zvr zl zy zw");

        midCons = Maker.makeOM(
                "b", "lb rb bj bl br lbr rbl skbr scbr zb bq bdh dbh bbh lbh rbh bb",
        "bh", "bbh dbh lbh rbh",
        "c", "lc lsc rc rsc cl cqu cr ct lcr rcl sctr scdr scbr scpr msc mscr nsc nscr ngscr ndscr cc",
        "ch", "lch rch rch",
        "d", "ld ld rd rd skdr scdr dr dr dr rdr ldr zd zdr ndr ndscr ndskr ndst dq ldh rdh dbh bdh ddh dd",
        "dh", "bdh ddh ldh rdh",
        "f", "lf rf fl fr fl fr fl fr lfr rfl ft ff",
        "g", "lg lg rg rg gl gr gl gr gl gr lgr rgl zg zgr ngr ngl ngscr ngskr gq gg",
        "h", "lch lph lth lsh rch rph rsh rth phl phr lphr rphl shl shr lshr rshl msh mshr zth bbh dbh lbh rbh bdh ddh ldh rdh",
        "j", "bj lj rj",
        "k", "lk lsk rk rsk kl kr lkr rkl sktr skdr skbr skpr tk zk zkr msk mskr nsk nskr ngskr ndskr kq kk",
        "kh", "lkh rkh",
        "l", "lb lc lch ld lf lg lj lk lm ln lp lph ls lst lt lth lsc lsk lsp lv lz lsh bl lbr rbl cl lcr rcl fl lfr rfl gl lgr rgl kl lkr rkl pl lpr rpl phl lphr rphl shl lshr rshl sl rsl lsl ldr ltr lx ngl nsl msl nsl ll lth tl ltl rtl vl",
        "m", "lm rm zm msl msc mscr msh mshr mst msp msk mskr mm",
        "n", "ln rn nx zn zn ndr nj ntr ntr ngr ngl nsl nsl nsc nscr ngscr ndscr nsk nskr ngskr ndskr nst ndst nsp nn",
        "p", "lp lsp rp rsp pl pr lpr rpl skpr scpr zp msp nsp lph rph phl phr lphr rphl pq pp",
        "ph", "lph lph rph rph phl phr lphr rphl",
        "q", "bq dq gq kq pq tq",
        "qu", "cqu lqu rqu",
        "r", "rb rc rch rd rf rg rj rk rm rn rp rph rs rsh rst rt rth rsc rsk rsp rv rz br br br lbr rbl cr cr cr lcr rcl fr fr fr lfr rfl gr gr gr lgr rgl kr kr kr lkr rkl pr pr pr lpr rpl phr phr phr lphr rphl shr shr shr lshr rshl rsl sktr sctr skdr scdr skbr scbr skpr scpr dr dr dr rdr ldr tr tr tr rtr ltr vr rx zr zdr ztr zgr zkr ntr ntr ndr ngr mscr mshr mskr nscr ngscr ndscr nskr ngskr ndskr rr",
        "s", "ls lst lsc lsk lsp rs rst rsc rsk rsp sl rsl lsl sktr sctr skdr scdr skbr scbr skpr scpr nsl msl msc mscr mst msp msk mskr nsl nsc nscr ngscr ndscr nsk nskr ngskr ndskr nst ndst nsp lsh rsh sh shl shqu shk shp shm shn shr shw shpl lshr rshl msh mshr ss",
        "sh", "lsh rsh sh shl shqu shk shp shm shn shr shw shpl lshr rshl msh mshr",
        "t", "ct ft lst lt rst rt sktr sctr tk tr rtr ltr zt ztr ntr ntr mst nst ndst tq ltl rtl tt",
        "th", "lth rth zth cth",
        "tl", "ltl rtl",
        "v", "lv rv vv vl vr",
        "w", "bw chw dw fw gw hw khw mw nw pw sw shw tw thw w wr wy zw",
        "x", "nx rx lx",
        "y", "by dy fy gy hy ky my ny py sy ty vy wy zy",
        "z", "lz rz zn zd zt zg zk zm zn zp zb zr zdr ztr zgr zkr zth zz");

        closeCons = Maker.makeOM("b", "b lb rb bs bz mb mbs bh bh lbh rbh mbh bb",
        "bh", "bh lbh rbh mbh",
        "c", "c ck cks lc rc cs cz ct cz cth sc",
        "ch", "ch lch rch tch pch kch mch nch",
        "d", "d ld rd ds dz dt dsh dth gd nd nds dh dh ldh rdh ndh dd",
        "dh", "dh ldh rdh ndh",
        "f", "f lf rf fs fz ft fsh ft fth ff",
        "g", "g lg rg gs gz gd gsh gth ng ngs gg",
        "h", "cth ch lch rch tch pch kch mch nch dsh dth fsh fth gsh gth h hs ksh kth psh pth ph ph ph ph ph ph lph rph phs pht phth",
        "j", "j",
        "k", "ck cks kch k lk rk ks kz kt ksh kth nk nks sk",
        "kh", "kh",
        "l", "lb lc lch ld lf lg lk l ls lz lp lph ll",
        "m", "mch m ms mb mt mp mbs mps mz sm mm",
        "n", "nch n ns nd nt nk nds nks nz ng ngs nn",
        "p", "pch mp mps p lp rp ps pz pt psh pth sp sp ph lph rph phs pht phth",
        "ph", "ph lph rph phs pht phth",
        "q", "q",
        "qu", "",
        "r", "rb rc rch rd rf rg rk rp rph r rs rz",
        "s", "bs cks cs ds fs gs hs ks ls ms mbs mps ns nds nks ngs ps phs rs s st sp st sp sc sk sm ts lsh rsh sh shk shp msh ss",
        "sh", "lsh rsh sh shk shp msh",
        "t", "ct ft tch dt ft kt mt nt pt pht st st t ts tz tt",
        "th", "cth dth fth gth kth pth phth th ths",
        "tl", "tl",
        "v", "v",
        "w", "",
        "x", "x",
        "y", "",
        "z", "bz cz dz fz gz kz lz mz nz pz rz tz z zz");
    }

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

    private FakeLanguageGen register() {
        summary = registry.size() + "@1";
        registry.add(this);
        return copy();
    }

    private FakeLanguageGen summarize(String brief) {
        summary = brief;
        return this;
    }

    private static FakeLanguageGen lovecraft() {
        return new FakeLanguageGen(
                new String[]{"a", "i", "o", "e", "u", "a", "i", "o", "e", "u", "ia", "ai", "aa", "ei"},
                new String[]{},
                new String[]{"s", "t", "k", "n", "y", "p", "k", "l", "g", "gl", "th", "sh", "ny", "ft", "hm", "zvr", "cth"},
                new String[]{"h", "gl", "gr", "nd", "mr", "vr", "kr"},
                new String[]{"l", "p", "s", "t", "n", "k", "g", "x", "rl", "th", "gg", "gh", "ts", "lt", "rk", "kh", "sh", "ng", "shk"},
                new String[]{"aghn", "ulhu", "urath", "oigor", "alos", "'yeh", "achtal", "elt", "ikhet", "adzek", "agd"},
                new String[]{"'", "-"}, new int[]{1, 2, 3}, new double[]{6, 7, 2},
                0.4, 0.31, 0.07, 0.04, null, true);
    }
    /**
     * Ia! Ia! Cthulhu Rl'yeh ftaghn! Useful for generating cultist ramblings or unreadable occult texts. You may want
     * to consider mixing this with multiple other languages using {@link #mixAll(Object...)}; using some very different
     * languages in low amounts relative to the amount used for this, like {@link #NAHUATL}, {@link #INUKTITUT}, and
     * {@link #SOMALI}, can alter the aesthetic of the generated text in ways that may help distinguish magic styles.
     * <br>
     * Zvrugg pialuk, ya'as irlemrugle'eith iposh hmo-es nyeighi, glikreirk shaivro'ei!
     */
    public static final FakeLanguageGen LOVECRAFT = lovecraft().register();
    private static FakeLanguageGen english() {
        return new FakeLanguageGen(
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
                new String[]{}, new int[]{1, 2, 3, 4}, new double[]{10, 11, 4, 1},
                0.22, 0.1, 0.0, 0.22, englishSanityChecks, true);
    }
    /**
     * Imitation English; may seem closer to Dutch in some generated text, and is not exactly the best imitation.
     * Should seem pretty fake to many readers; does not filter out dictionary words but does perform basic vulgarity
     * filtering. If you want to avoid generating other words, you can subclass FakeLanguageGen and modify word() .
     * <br>
     * Mont tiste frot; mousation hauddes?
     * Lily wrely stiebes; flarrousseal gapestist.
     */
    public static final FakeLanguageGen ENGLISH = english().register();

    private static FakeLanguageGen greekRomanized(){
        return new FakeLanguageGen(
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
                new String[]{}, new int[]{1, 2, 3, 4}, new double[]{5, 7, 4, 1}, 0.45, 0.45, 0.0, 0.2, null, true);
    }

    /**
     * Imitation ancient Greek, romanized to use the Latin alphabet. Likely to seem pretty fake to many readers.
     * <br>
     * Psuilas alor; aipeomarta le liaspa...
     */
    public static final FakeLanguageGen GREEK_ROMANIZED = greekRomanized().register();
    private static FakeLanguageGen greekAuthentic(){
        return new FakeLanguageGen(
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
            new String[]{}, new int[]{1, 2, 3, 4}, new double[]{5, 7, 4, 1}, 0.45, 0.45, 0.0, 0.2, null, true);
    }
    /**
     * Imitation ancient Greek, using the original Greek alphabet. People may try to translate it and get gibberish.
     * Make sure the font you use to render this supports the Greek alphabet! In the GDX display module, most
     * fonts support all the Greek you need for this.
     * <br>
     * Ψυιλασ αλορ; αιπεομαρτα λε λιασπα...
     */
    public static final FakeLanguageGen GREEK_AUTHENTIC = greekAuthentic().register();

    private static FakeLanguageGen french(){
        return new FakeLanguageGen(
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
                        "a", "a", "a", "e", "e", "e", "i", "i", "o", "u", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                        "a", "a", "e", "e", "i", "o", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                        "ai", "ai", "eau", "oi", "oi", "oui", "eu", "au", "au", "ei", "ei", "oe", "oe", "ou", "ou", "ue"
                },
                new String[]{"tr", "ch", "m", "b", "b", "br", "j", "j", "j", "j", "g", "t", "t", "t", "c", "d", "f", "f", "h", "n", "l", "l",
                        "s", "s", "s", "r", "r", "r", "v", "v", "p", "pl", "pr", "bl", "br", "dr", "gl", "gr"},
                new String[]{"cqu", "gu", "qu", "rqu", "nt", "ng", "ngu", "mb", "ll", "nd", "ndr", "nct", "st",
                        "xt", "mbr", "pl", "g", "gg", "ggr", "gl", "bl", "j", "gn",
                        "m", "m", "mm", "v", "v", "f", "f", "f", "ff", "b", "b", "bb", "d", "d", "dd", "s", "s", "s", "ss", "ss", "ss",
                        "cl", "cr", "ng", "ç", "ç", "rç", "rd", "lg", "rg"},
                new String[]{"rt", "ch", "m", "b", "b", "lb", "t", "t", "t", "t", "c", "d", "f", "f", "n", "n", "l", "l",
                        "s", "s", "s", "r", "r", "p", "rd", "ff", "ss", "ll"
                },
                new String[]{"e", "e", "e", "e", "e", "é", "é", "er", "er", "er", "er", "er", "es", "es", "es", "es", "es", "es",
                        "e", "e", "e", "e", "e", "é", "é", "er", "er", "er", "er", "er", "er", "es", "es", "es", "es", "es",
                        "e", "e", "e", "e", "e", "é", "é", "é", "er", "er", "er", "er", "er", "es", "es", "es", "es", "es",
                        "ent", "em", "en", "en", "aim", "ain", "an", "oin", "ien", "iere", "ors", "anse",
                        "ombs", "ommes", "ancs", "ends", "œufs", "erfs", "ongs", "aps", "ats", "ives", "ui", "illes",
                        "aen", "aon", "am", "an", "eun", "ein", "age", "age", "uile", "uin", "um", "un", "un", "un",
                        "aille", "ouille", "eille", "ille", "eur", "it", "ot", "oi", "oi", "oi", "aire", "om", "on", "on",
                        "im", "in", "in", "ien", "ien", "ine", "ion", "il", "eil", "oin", "oint", "iguïté", "ience", "incte",
                        "ang", "ong", "acré", "eau", "ouche", "oux", "oux", "ect", "ecri", "agne", "uer", "aix", "eth", "ut", "ant",
                        "anc", "anc", "anche", "ioche", "eaux", "ive", "eur", "ancois", "ecois", "ente", "enri",
                        "arc", "oc", "ouis", "arche", "ique", "ique", "ique", "oque", "arque", "uis", "este", "oir", "oir"
                },
                new String[]{}, new int[]{1, 2, 3}, new double[]{15, 7, 2}, 0.35, 1.0, 0.0, 0.4, null, true);
    }
    /**
     * Imitation modern French, using the (many) accented vowels that are present in the language. Translating it
     * will produce gibberish if it produces anything at all. In the GDX display module, most
     * fonts support all the accented characters you need for this.
     * <br>
     * Bœurter; ubi plaqua se saigui ef brafeur?
     */
    public static final FakeLanguageGen FRENCH = french().register();

    private static FakeLanguageGen russianRomanized(){
        return new FakeLanguageGen(
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
    }
    /**
     * Imitation modern Russian, romanized to use the Latin alphabet. Likely to seem pretty fake to many readers.
     * <br>
     * Zhydotuf ruts pitsas, gogutiar shyskuchebab - gichapofeglor giunuz ieskaziuzhin.
     */
    public static final FakeLanguageGen RUSSIAN_ROMANIZED = russianRomanized().register();

    private static FakeLanguageGen russianAuthentic(){
        return new FakeLanguageGen(
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
    }
    /**
     * Imitation modern Russian, using the authentic Cyrillic alphabet used in Russia and other countries.
     * Make sure the font you use to render this supports the Cyrillic alphabet!
     * In the GDX display module, the "smooth" fonts support all the Cyrillic alphabet you need for this.
     * <br>
     * Жыдотуф руц пйцас, гогутяр шыскучэбаб - гйчапофёглор гюнуз ъсказюжин.
     */
    public static final FakeLanguageGen RUSSIAN_AUTHENTIC = russianAuthentic().register();

    private static FakeLanguageGen japaneseRomanized(){
        return new FakeLanguageGen(
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
                        "k", "t", "z", "s", "f", "g", "z", "b", "d", "ts", "sh", "m",
                        "k", "t", "z", "s", "f", "g", "z", "b", "d", "ts", "sh", "m",
                        "nn", "nn", "nd", "nz", "mm", "kk", "tt", "ss", "ssh", "tch"},
                new String[]{"n"},
                new String[]{"ima", "aki", "aka", "ita", "en", "izen", "achi", "uke", "aido", "outsu", "uki", "oku", "aku", "oto", "okyo"},
                new String[]{}, new int[]{1, 2, 3, 4, 5}, new double[]{5, 4, 5, 4, 3}, 0.3, 0.9, 0.0, 0.07, japaneseSanityChecks, true);
    }
    /**
     * Imitation Japanese, romanized to use the Latin alphabet. Likely to seem pretty fake to many readers.
     * <br>
     * Narurehyounan nikase keho...
     */
    public static final FakeLanguageGen JAPANESE_ROMANIZED = japaneseRomanized().register();

    private static FakeLanguageGen swahili(){
        return new FakeLanguageGen(
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
    }
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
    public static final FakeLanguageGen SWAHILI = swahili().register();

    private static FakeLanguageGen somali(){
        return new FakeLanguageGen(
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
    }
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
    public static final FakeLanguageGen SOMALI = somali().register();
    private static FakeLanguageGen hindi(){
        return new FakeLanguageGen(
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
    }
    /**
     * Imitation Hindi, romanized to use the Latin alphabet using accented glyphs similar to the IAST standard.
     * Most fonts do not support the glyphs that IAST-standard romanization of Hindi needs, so this uses alternate
     * glyphs from at most Latin Extended-A. Relative to the IAST standard, the glyphs {@code "ṛṝḷḹḍṭṅṇṣṃḥ"} become
     * {@code "ŗŕļĺđţńņşĕĭ"}, with the nth glyph in the first string being substituted with the nth glyph in the second
     * string. You may want to get a variant on this language with {@link #removeAccents()} if you can't display the
     * less-commonly-supported glyphs {@code āīūĕĭáíúóŗŕļţĺđńñņśş}. For some time SquidLib had a separate version of
     * imitation Hindi that was accurate to the IAST standard, but this version is more usable because font support is
     * much better for the glyphs it uses, so the IAST kind was removed (it added quite a bit of code for something that
     * was mostly unusable).
     * <br>
     * Darvāga yar; ghađhinopŕauka āĕrdur, conśaigaijo śabhodhaĕđū jiviđaudu.
     */
    public static final FakeLanguageGen HINDI_ROMANIZED = hindi().register();

    private static FakeLanguageGen arabic(){
        return new FakeLanguageGen(
                new String[]{"a", "a", "a", "a", "a", "a", "aa", "aa", "aa", "ai", "au",
                        "a", "i", "u", "a", "i", "u",
                        "i", "i", "i", "i", "i", "ii", "ii", "ii",
                        "u", "u", "u", "uu", "uu",
                },
                new String[]{},
                new String[]{"gh", "b", "t", "th", "j", "kh", "khr", "d", "dh", "r", "z", "s", "sh", "shw",
                        "zh", "khm", "g", "f", "q", "k", "l", "m", "n", "h", "w",
                        "q", "k", "q", "k", "b", "d", "f", "l", "z", "zh", "h", "h", "kh", "j", "s", "sh", "shw", "r",
                        "q", "k", "q", "k", "f", "l", "z", "h", "h", "j", "s", "r",
                        "q", "k", "f", "l", "z", "h", "h", "j", "s", "r",
                        "al-", "al-", "ibn-",
                },
                new String[]{
                        "kk", "kk", "kk", "kk", "kk", "dd", "dd", "dd", "dd",
                        "nj", "mj", "bj", "mj", "bj", "mj", "bj", "dj", "dtj", "dhj",
                        "nz", "nzh", "mz", "mzh", "rz", "rzh", "bz", "dz", "tz",
                        "s-h", "sh-h", "shw-h", "tw", "bn", "fq", "hz", "hl", "khm",
                        "lb", "lz", "lj", "lf", "ll", "lk", "lq", "lg", "ln"
                },
                new String[]{
                        "gh", "b", "t", "th", "j", "kh", "khr", "d", "dh", "r", "z", "s", "sh", "shw", "dt", "jj",
                        "zh", "khm", "g", "f", "q", "k", "l", "m", "n", "h", "w",
                        "k", "q", "k", "b", "d", "f", "l", "z", "zh", "h", "h", "kh", "j", "s", "sh", "shw", "r",
                        "k", "q", "k", "f", "l", "z", "h", "h", "j", "s", "r",
                        "k", "f", "l", "z", "h", "h", "j", "s", "r",
                        "b", "t", "th", "j", "kh", "khr", "d", "dh", "r", "z", "s", "sh", "shw", "dt", "jj",
                        "zh", "g", "f", "q", "k", "l", "m", "n", "h", "w",
                        "k", "q", "k", "b", "d", "f", "l", "z", "zh", "h", "h", "kh", "j", "s", "sh", "shw", "r",
                        "k", "q", "k", "f", "l", "z", "h", "h", "j", "s", "r",
                        "k", "f", "l", "z", "h", "h", "j", "s", "r",
                },
                new String[]{"aagh", "aagh", "ari", "ari", "aiid", "uuq", "ariid", "adih", "ateh", "adesh", "amiit", "it",
                        "iit", "akhmen", "akhmed", "ani", "abiib", "iib", "uuni", "iiz", "aqarii", "adiiq",
                },
                new String[]{}, new int[]{1, 2, 3, 4}, new double[]{6, 5, 5, 1}, 0.55, 0.65, 0.0, 0.15, arabicSanityChecks, true);
    }
    /**
     * Imitation Arabic, using mostly the Latin alphabet but with some Greek letters for tough transliteration topics.
     * It's hard to think of a more different (widely-spoken) language to romanize than Arabic. Written Arabic does not
     * ordinarily use vowels (the writing system is called an abjad, in contrast to an alphabet), and it has more than a
     * few sounds that are very different from those in English. This version, because of limited support in fonts and
     * the need for separate words to be distinguishable with regular expressions, uses somewhat-accurate digraphs or
     * trigraphs instead of the many accented glyphs (not necessarily supported by most fonts) in most romanizations of
     * Arabic, and this scheme uses no characters from outside ASCII.
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
     * Hiijakki al-aafusiib rihit, ibn-ullukh aj shwisari!
     */
    public static final FakeLanguageGen ARABIC_ROMANIZED = arabic().register();
    /*
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
            */

    private static FakeLanguageGen inuktitut(){
        return new FakeLanguageGen(
                new String[]{"a", "a", "a", "a", "a", "aa", "aa", "aa", "aa", "i", "i", "i", "ii", "ii", "u", "u", "u", "uu", "uu", "ai", "ia", "iu", "ua", "ui"},
                new String[]{},
                new String[]{"p", "t", "k", "q", "s", "l", "h", "v", "j", "g", "r", "m", "n",
                        "t", "t", "t", "t", "k", "k", "q", "q", "n", "n", "n", "n", "g", "l"},
                new String[]{"pp", "tt", "kk", "pk", "tk", "gk", "kp", "kt", "kg", "pq", "tq", "gq", "ss", "ll", "rr", "mm",
                        "nn", "nng", "ng", "ng",
                        "ll", "nn", "nn", "nn",},
                new String[]{"n", "t", "q", "k", "n", "t", "q", "k", "n", "t", "q", "k", "n", "t", "q", "k", "p", "s", "m", "g", "g", "ng", "ng", "ng"},
                new String[]{"itut", "uit", "uq", "iuq", "iaq", "aq", "it", "aat", "aak", "aan", "ait", "ik", "uut", "un", "unnun",
                        "ung", "ang", "ing", "iin", "iit", "iik", "in",
                        "uq", "iaq", "aq", "ik", "it", "uit", "ut", "ut", "at", "un", "in"
                },
                new String[]{}, new int[]{1, 2, 3, 4, 5}, new double[]{3, 4, 6, 5, 4}, 0.45, 0.0, 0.0, 0.25, null, true);
    }
    /**
     * Imitation text from an approximation of one of the Inuktitut languages spoken by various people of the Arctic and
     * nearby areas. This is likely to be hard to pronounce. Inuktitut is the name accepted in Canada for one language
     * family of that area, but other parts of the Arctic circle speak languages with varying levels of difference from
     * this style of generated text. The term "Inuit language" may be acceptable, but "Eskimo language" is probably not,
     * and when that term is not considered outright offensive it refers to a different language group anyway (more
     * properly called Yupik or Yup'ik, and primarily spoken in Siberia instead of Canada and Alaska).
     * <br>
     * Ugkangungait ninaaq ipkutuilluuq um aitqiinnaitunniak tillingaat.
     */
    public static final FakeLanguageGen INUKTITUT = inuktitut().register();

    private static FakeLanguageGen norse(){
        return new FakeLanguageGen(
                new String[]{"a","a","a","á","á","au","e","e","e","é","é","ei","ey","i","i","í","í","y","y","ý","ý",
                        "o","o","o","ó","ó","u","u","u","ú","ú","æ","æ","æ","ö","ö",},
                new String[]{},
                new String[]{"b","bl","br","bj","d","dr","dj","ð","ðl","ðr","f","fl","flj","fr","fn","fj","g","gn","gj","h",
                        "hj","hl","hr","hv","j","k","kl","kr","kn","kj","l","lj","m","mj","n","nj","p","pl","pr","pj","r",
                        "rj","s","sj","sl","sn","sp","st","str","skr","skj","sþ","sð","t","tj","v","vl","vr","vj","þ","þl","þr",

                        "d","f","fl","g","gl","gr","k","h","hr","n","k","l","m","mj","n","r","s","st","t","þ","ð",
                        "d","f","fl","g","gl","gr","k","h","hr","n","k","l","m","mj","n","r","s","st","t","þ","ð",
                        "d","f","fl","g","gl","gr","k","h","hr","n","k","l","m","mj","n","r","s","st","t","þ","ð",
                        "d","f","fl","g","gl","gr","k","h","hr","n","k","l","m","mj","n","r","s","st","t","þ","ð",
                        "d","f","fl","g","gl","gr","k","h","hr","n","k","l","m","mj","n","r","s","st","t","þ","ð",

                        "d","d","f","f","fl","g","g","g","gl","gr","k","h","hr","n","k","kl","l","n","r","r","s","st","t","t",
                        "d","d","f","f","fl","g","g","g","gl","gr","k","h","hr","n","k","kl","l","n","r","r","s","st","t","t",
                        "d","d","f","f","fl","g","g","g","gl","gr","k","h","hr","n","k","kl","l","n","r","r","s","st","t","t",
                        "d","d","f","f","fl","g","g","g","gl","gr","k","h","hr","n","k","kl","l","n","r","r","s","st","t","t",
                },
                new String[]{"bd","bf","bg","bk","bl","bp","br","bt","bv","bm","bn","bð","bj",
                        "db","df","dg","dk","dl","dp","dr","dt","dv","dm","dn","dð","dþ","dj","ndk","ndb","ndg","ndl","nds","nds",
                        "ðl","ðr","ðk","ðj","ðg","ðd","ðb","ðp","ðs",
                        "fb","fd","fg","fk","fl","fp","fr","fs","ft","fv","fm","fn","fð","fj",
                        "gb","gd","gf","gk","gl","gp","gr","gt","gv","gm","gn","gð","gj",
                        "h","hj","hl","hr","hv",
                        "kb","kd","kf","kp","kv","km","kn","kð","kl","kr","nkj","nkr","nkl",
                        "lbr","ldr","lfr","lg","lgr","lj","lkr","ln","ls","ltr","lv","lð","lðr","lþ",
                        "mb","md","mk","mg","ml","mp","mr","ms","mt","mv","mð","mþ","mj",
                        "nb","nl","np","nr","nv","nð","nþ","nj",
                        "ngl","ngb","ngd","ngk","ngp","ngt","ngv","ngm","ngð","ngþ","ngr",
                        "mbd","mbg","mbs","mbt","ldg","ldn","ldk","lds","rðn","rðl","gðs","gðr",
                        "pb","pd","pg","pk","pl","pr","ps","psj","pð","pj",
                        "rl","rbr","rdr","rg","rgr","rkr","rpr","rs","rts","rtr","rv","rj",
                        "sb","sbr","sd","sdr","sf","sfj","sg","skr","skl","sm","sn","str","sv","sð","sþ","sj",
                        "tr","tn","tb","td","tg","tv","tf","tj","tk","tm","tp",},
                new String[]{"kk","ll","nn","pp","tt","kk","ll","nn","pp","tt",
                        "bs","ds","gs","x","rn","gn","gt","gs","ks","kt","nt","nd","nk","nt","ng","ngs","ns",
                        "ps","pk","pt","pts","lb","ld","lf","lk","lm","lp","lps","lt",
                        "rn","rb","rd","rk","rp","rt","rm","rð","rþ","sk","sp","st","ts",
                        "b","d","ð","f","g","gn","h","k","nk","l","m","n","ng","p","r","s","sp","st","sþ","sð","t","v","þ",
                        "b","d","ð","f","g","gn","h","k","nk","l","m","n","ng","p","r","s","sp","st","sþ","sð","t","v","þ",
                        "b","d","ð","f","g","gn","h","k","nk","l","m","n","ng","p","r","s","sp","st","sþ","sð","t","v","þ",

                        "b","b","b","d","d","d","f","f","f","g","g","k","k","nk","l","n","ng","p","p","r","r","r","s","s","st","t","t",
                        "b","b","b","d","d","d","f","f","f","g","g","k","k","nk","l","n","ng","p","p","r","r","r","s","s","st","t","t",
                        "b","b","b","d","d","d","f","f","f","g","g","k","k","nk","l","n","ng","p","p","r","r","r","s","s","st","t","t",
                },
                new String[]{"etta","eþa","uinn","ing","ard","eign","ef","efs","eg","ir","ir","ir","ir","ír","ír","ar","ar",
                        "ar","ár","or","or","ór","ör","on","on","ón","onn","unn","ung","ut","ett","att","ot"},
                new String[]{}, new int[]{1, 2, 3, 4, 5}, new double[]{5, 5, 4, 3, 1}, 0.25, 0.5, 0.0, 0.08, genericSanityChecks, true);
    }
    /**
     * Somewhat close to Old Norse, which is itself very close to Icelandic, so this uses Icelandic spelling rules. Not
     * to be confused with the language(s) of Norway, where the Norwegian languages are called norsk, and are further
     * distinguished into Bokmål and Nynorsk. This should not be likely to seem like any form of Norwegian, since it
     * doesn't have the a-with-ring letter 'å' and has the letters eth ('Ðð') and thorn (Þþ). If you want to remove any
     * letters not present on a US-ASCII keyboard, you can use {@link Modifier#SIMPLIFY_NORSE} on this language or some
     * mix of this with other languages; it also changes some of the usage of "j" where it means the English "y" sound,
     * making "fjord" into "fyord", which is closer to familiar uses from East Asia like "Tokyo" and "Pyongyang".
     * <br>
     * Leyrk tjör stomri kna snó æd ðrépdápá, prygso?
     */
    public static final FakeLanguageGen NORSE = norse().register();

    private static FakeLanguageGen nahuatl(){
        return new FakeLanguageGen(
                new String[]{"a", "a", "a", "a", "a", "a", "a", "i", "i", "i", "i", "i", "o", "o", "o", "e", "e", "eo", "oa", "ea"},
                new String[]{},
                new String[]{"ch", "c", "h", "m", "l", "n", "p", "t", "tl", "tz", "x", "y", "z", "hu", "cu",
                        "l", "l", "l", "p", "p", "t", "t", "t", "t", "t", "tl", "tl", "tz", "z", "x", "hu"},
                new String[]{"zp", "ztl", "zc", "zt", "zl", "ct", "cl", "pl", "mt", "mc", "mch", "cz", "tc", "lc",
                        "hu", "hu", "hu", "cu"},
                new String[]{
                        "ch", "c", "h", "m", "l", "n", "p", "t", "tl", "tz", "x", "y", "z",
                        "l", "l", "l", "l", "p", "t", "t", "t", "tl", "tl", "tz", "tz", "z", "x"
                },
                new String[]{"otl", "eotl", "ili", "itl", "atl", "atli", "oca", "itli", "oatl", "al", "ico", "acual",
                        "ote", "ope", "oli", "ili", "acan", "ato", "atotl", "ache", "oc", "aloc", "ax", "itziz", "iz"
                },
                new String[]{}, new int[]{1, 2, 3, 4, 5, 6}, new double[]{3, 4, 5, 4, 3, 1}, 0.3, 0.2, 0.0, 0.3, genericSanityChecks, true)
                .addModifiers(new Modifier("c([ie])", "qu$1"),
                        new Modifier("z([ie])", "c$1"));
    }

    /**
     * Imitation text from an approximation of the language spoken by the Aztec people and also over a million
     * contemporary people in parts of Mexico. This is may be hard to pronounce, since it uses "tl" as a normal
     * consonant (it can start or end words), but is mostly a fairly recognizable style of language.
     * <br>
     * Olcoletl latl palitz ach; xatatli tzotloca amtitl, xatloatzoatl tealitozaztitli otamtax?
     */
    public static final FakeLanguageGen NAHUATL = nahuatl().register();

    private static FakeLanguageGen mongolian(){
        return new FakeLanguageGen(
                new String[]{"a", "a", "a", "a", "a", "a", "a", "aa", "aa", "e", "i", "i", "i", "i", "i", "i", "i", "i", "ii",
                        "o", "o", "o", "o", "oo", "u", "u", "u", "u", "u", "u", "u", "u", "uu", "uu", "ai", "ai"},
                new String[]{},
                new String[]{"g", "m", "n", "g", "m", "n", "g", "m", "n", "n", "n", "ch", "gh", "ch", "gh", "gh", "j", "j", "j", "j",
                        "s", "s", "s", "t", "ts", "kh", "r", "r", "l", "h", "h", "h", "h", "h", "b", "b", "b", "b", "z", "z", "y", "y"},
                new String[]{},
                new String[]{"g", "m", "n", "g", "m", "n", "g", "m", "n", "n", "n", "ch", "gh", "ch", "gh", "gh", "gh", "j", "j", "j",
                        "s", "s", "s", "t", "ts", "kh", "r", "r", "l", "h", "h", "h", "h", "h", "b", "b", "b", "b", "z", "z", "g", "n",
                        "g", "m", "n", "g", "m", "n", "g", "m", "n", "n", "n", "ch", "gh", "ch", "gh", "gh", "gh", "j", "j", "j", "n",
                        "s", "s", "s", "t", "ts", "kh", "r", "r", "l", "h", "h", "h", "h", "h", "b", "b", "b", "b", "z", "z", "y", "y",
                        "ng", "ng", "ng", "ngh", "ngh", "lj", "gch", "sd", "rl", "bl", "sd", "st", "md", "mg", "gd", "gd",
                        "sv", "rg", "rg", "mr", "tn", "tg", "ds", "dh", "dm", "gts", "rh", "lb", "gr", "gy", "rgh"},
                new String[]{"ei", "ei", "ei", "uulj", "iig", "is", "is", "an", "aan", "iis", "alai", "ai", "aj", "ali"
                },
                new String[]{}, new int[]{1, 2, 3, 4}, new double[]{5, 9, 3, 1}, 0.3, 0.2, 0.0, 0.07, null, true);
    }

    /**
     * Imitation text from an approximation of one of the languages spoken in the 13th-century Mongol Empire. Can be
     * hard to pronounce. This is closest to Middle Mongolian, and is probably not the best way to approximate modern
     * Mongolian, which was written for many years in the Cyrillic alphabet (same alphabet as Russian) and has changed a
     * lot in other ways.
     * <br>
     * Ghamgarg zilijuub lirgh arghar zunghichuh naboogh.
     */
    public static final FakeLanguageGen MONGOLIAN = mongolian().register();

    /**
     * A mix of four different languages, using only ASCII characters, that is meant for generating single words for
     * creature or place names in fantasy settings.
     * <br>
     * Adeni, Sainane, Caneros, Sune, Alade, Tidifi, Muni, Gito, Lixoi, Bovi...
     */
    public static final FakeLanguageGen FANTASY_NAME = GREEK_ROMANIZED.mix(
            RUSSIAN_ROMANIZED.mix(
                    FRENCH.removeAccents().mix(
                            JAPANESE_ROMANIZED, 0.5), 0.85), 0.925).register();
    /**
     * A mix of four different languages with some accented characters added onto an ASCII base, that can be good for
     * generating single words for creature or place names in fantasy settings that should have a "fancy" feeling from
     * having unnecessary accents added primarily for visual reasons.
     * <br>
     * Askieno, Blarcīnũn, Mēmida, Zizhounkô, Blęrinaf, Zemĭ, Mónazôr, Renerstă, Uskus, Toufounôr...
     */
    public static final FakeLanguageGen FANCY_FANTASY_NAME = FANTASY_NAME.addAccents(0.47, 0.07).register();

    private static FakeLanguageGen goblin(){
        return new FakeLanguageGen(
                new String[]{"a", "a", "a", "a",
                        "e", "e",
                        "i", "i", "i",
                        "o", "o", "o", "o",
                        "u", "u", "u", "u", "u", "u", "u",
                },
                new String[]{},
                new String[]{"b", "g", "d", "m", "h", "n", "r", "v", "sh", "p", "w", "y", "f", "br", "dr", "gr", "pr", "fr",
                        "br", "dr", "gr", "pr", "fr", "bl", "dw", "gl", "gw", "pl", "fl", "hr",
                        "b", "g", "d", "m", "h", "n", "r", "b", "g", "d", "m", "h", "n", "r",
                        "b", "g", "d", "m", "r", "b", "g", "d", "r",
                },
                new String[]{
                        "br", "gr", "dr", "pr", "fr", "rb", "rd", "rg", "rp", "rf",
                        "br", "gr", "dr", "rb", "rd", "rg",
                        "mb", "mg", "md", "mp", "mf", "bm", "gm", "dm", "pm", "fm",
                        "mb", "mg", "md", "bm", "gm", "dm",
                        "bl", "gl", "dw", "pl", "fl", "lb", "ld", "lg", "lp", "lf",
                        "bl", "gl", "dw", "lb", "ld", "lg",
                        "nb", "ng", "nd", "np", "nf", "bn", "gn", "dn", "pn", "fn",
                        "nb", "ng", "nd", "bn", "gn", "dn",
                        "my", "gy", "by", "py", "mw", "gw", "bw", "pw",
                        "bg", "gb", "bd", "db", "bf", "fb",
                        "gd", "dg", "gp", "pg", "gf", "fg",
                        "dp", "pd", "df", "fd",
                        "pf", "fp",
                        "bg", "gb", "bd", "db", "gd", "dg",
                        "bg", "gb", "bd", "db", "gd", "dg",
                        "bg", "gb", "bd", "db", "gd", "dg",
                        "bg", "gb", "bd", "db", "gd", "dg",
                        "bg", "gb", "bd", "db", "gd", "dg",
                },
                new String[]{
                        "b", "g", "d", "m", "n", "r", "sh", "p", "f",
                        "b", "g", "d", "m", "n", "r", "b", "g", "d", "m", "n", "r", "sh",
                        "b", "g", "d", "m", "r", "b", "g", "d", "r",
                        "rb", "rd", "rg", "rp", "rf", "lb", "ld", "lg", "lp", "lf",
                },
                new String[]{},
                new String[]{}, new int[]{1, 2, 3, 4}, new double[]{3, 7, 5, 1}, 0.1, 0.15, 0.0, 0.0, genericSanityChecks, true);
    }
    /**
     * Fantasy language that might be suitable for stealthy humanoids, such as goblins, or as a secret language used
     * by humans who want to avoid notice. Uses no "hard" sounds like "t" and "k", but also tries to avoid the flowing
     * aesthetic of fantasy languages associated with elves. Tends toward clusters of consonants like "bl", "gm", "dg",
     * and "rd".
     * <br>
     * Gwabdip dwupdagorg moglab yurufrub.
     */
    public static final FakeLanguageGen GOBLIN = goblin().register();

    private static FakeLanguageGen elf(){
        return new FakeLanguageGen(
                new String[]{"a", "a", "a", "e", "e", "e", "i", "i", "o", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                        "a", "a", "a", "e", "e", "e", "i", "i", "o", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                        "a", "a", "e", "e", "i", "o", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                        "ai", "ai", "ai", "ea", "ea", "ea", "ia", "ae"
                },
                new String[]{
                        "ai", "ai", "ae", "ea", "ia", "ie",
                        "â", "â", "ai", "âi", "aî", "aï", "î", "î", "ï", "ï", "îe", "iê", "ïe", "iê",
                        "e", "ë", "ë", "ëa", "ê", "êa", "eâ", "ei", "eî", "o", "ô",
                        "a", "a", "a", "e", "e", "e", "i", "i", "o", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                        "a", "a", "e", "e", "i", "o", "a", "a", "a", "e", "e", "e", "i", "i", "o",
                        "ai", "ai", "ai", "ai", "ai", "ei", "ei", "ei", "ea", "ea", "ea", "ea",
                        "ie", "ie", "ie", "ie", "ie", "ia", "ia", "ia", "ia"
                },
                new String[]{"l", "r", "n", "m", "th", "v", "s", "sh", "z", "f", "p", "h", "y", "c",
                        "l", "r", "n", "m", "th", "v", "f", "y",
                        "l", "r", "n", "m", "th", "v", "f",
                        "l", "r", "n", "th", "l", "r", "n", "th",
                        "l", "r", "n", "l", "r", "n", "l", "r", "n",
                        "pl", "fy", "ly", "cl", "fr", "pr", "qu",
                },
                new String[]{"rm", "ln", "lv", "lth", "ml", "mv", "nv", "vr", "rv", "ny", "mn", "nm", "ns", "nth"},
                new String[]{
                        "l", "r", "n", "m", "th", "s",
                        "l", "r", "n", "th", "l", "r", "n", "th",
                        "l", "r", "n", "l", "r", "n", "l", "r", "n",
                        "r", "n", "r", "n", "r", "n", "n", "n", "n", "n"
                },
                new String[]{},
                new String[]{}, new int[]{1, 2, 3, 4, 5}, new double[]{3, 6, 6, 3, 1}, 0.4, 0.3, 0.0, 0.0, genericSanityChecks, true);
    }

    /**
     * Fantasy language that tries to imitate the various languages spoken by elves in J.R.R. Tolkien's works, using
     * accented vowels occasionally and aiming for long, flowing, vowel-heavy words. It's called ELF because there isn't
     * a consistent usage across fantasy and mythological sources of either "elvish", "elfish", "elven", "elfin", or any
     * one adjective for "relating to an elf." In the GDX display module, the "smooth" and "unicode" fonts, among
     * others, support all the accented characters you need for this.
     * <br>
     * Il ilthiê arel enya; meâlelail theasor arôreisa.
     */
    public static final FakeLanguageGen ELF = elf().register();

    private static FakeLanguageGen demonic(){
        return new FakeLanguageGen(
                new String[]{"a", "a", "a", "a",
                        "e",
                        "i", "i",
                        "o", "o", "o", "o", "o",
                        "u", "u", "u", "u", "u",
                },
                new String[]{},
                new String[]{
                        "b", "bh", "d", "dh", "t", "tl", "ts", "k", "ch", "kh", "g", "gh", "f", "x", "s", "sh", "z", "r", "v", "y",
                        "br", "bhr", "dr", "dhr", "tr", "tsr", "kr", "khr", "gr", "ghr", "fr", "shr", "vr",
                        "bl", "bhl", "tsl", "kl", "chl", "khl", "gl", "ghl", "fl", "sl", "zl", "vl",
                        "dz", "chf", "sf", "shf", "zv", "st", "sk",
                        "t", "t", "t", "ts", "ts", "k", "k", "k", "kh", "kh", "kh", "kh", "khr", "kl", "kl", "kr", "kr",
                        "z", "z", "z", "v", "v", "v", "zv", "zv", "vr", "vr", "vl", "vl", "dz", "sk", "sk", "sh", "shr",
                        "x", "x", "x", "gh", "gh", "ghr",
                        "t", "t", "t", "ts", "ts", "k", "k", "k", "kh", "kh", "kh", "kh", "khr", "kl", "kl", "kr", "kr",
                        "z", "z", "z", "v", "v", "v", "zv", "zv", "vr", "vr", "vl", "vl", "dz", "sk", "sk", "sh", "shr",
                        "x", "x", "x", "gh", "gh", "ghr",
                        "t", "t", "t", "ts", "ts", "k", "k", "k", "kh", "kh", "kh", "kh", "khr", "kl", "kl", "kr", "kr",
                        "z", "z", "z", "v", "v", "v", "zv", "zv", "vr", "vr", "vl", "vl", "dz", "sk", "sk", "sh", "shr",
                        "x", "x", "x", "gh", "gh", "ghr",
                },
                new String[]{},
                new String[]{
                        "b", "bh", "d", "dh", "t", "lt", "k", "ch", "kh", "g", "gh", "f", "x", "s", "sh", "z", "r",
                        "b", "bh", "d", "dh", "t", "lt", "k", "ch", "kh", "g", "gh", "f", "x", "s", "sh", "z", "r",
                        "b", "bh", "d", "dh", "t", "lt", "k", "ch", "kh", "g", "gh", "f", "x", "s", "sh", "z", "r",
                        "b", "bh", "d", "dh", "t", "lt", "k", "ch", "kh", "g", "gh", "f", "x", "s", "sh", "z", "r",
                        "rb", "rbs", "rbh", "rd", "rds", "rdh", "rt", "rts", "rk", "rks", "rch", "rkh", "rg", "rsh", "rv", "rz",
                        "lt", "lts", "lk", "lch", "lkh", "lg", "ls", "lz", "lx",
                        "bs", "ds", "ts", "lts", "ks", "khs", "gs", "fs", "rs", "rx",
                        "bs", "ds", "ts", "lts", "ks", "khs", "gs", "fs", "rs", "rx",
                        "rbs", "rds", "rts", "rks", "rkhs", "rgs", "rfs", "rs", "rx",
                        "lbs", "lds", "lts", "lks", "lkhs", "lgs", "lfs",
                        "rdz", "rvz", "gz", "rgz", "vd", "kt",
                        "t", "t", "t", "rt", "lt", "k", "k", "k", "k", "k", "kh", "kh", "kh", "kh", "kh", "rkh", "lk", "rk", "rk",
                        "z", "z", "z", "z", "v", "rv", "rv", "dz", "ks", "sk", "sh",
                        "x", "x", "x", "gh", "gh", "gh", "rgh",
                        "ts", "ts", "ks", "ks", "khs",
                        "t", "t", "t", "rt", "lt", "k", "k", "k", "k", "k", "kh", "kh", "kh", "kh", "kh", "rkh", "lk", "rk", "rk",
                        "z", "z", "z", "z", "v", "rv", "rv", "dz", "ks", "sk", "sh",
                        "x", "x", "x", "gh", "gh", "gh", "rgh",
                        "ts", "ts", "ks", "ks", "khs",
                        "t", "t", "t", "rt", "lt", "k", "k", "k", "k", "k", "kh", "kh", "kh", "kh", "kh", "rkh", "lk", "rk", "rk",
                        "z", "z", "z", "z", "v", "rv", "rv", "dz", "ks", "sk", "sh",
                        "x", "x", "x", "gh", "gh", "gh", "rgh",
                        "ts", "ts", "ks", "ks", "khs",
                },
                new String[]{},
                new String[]{"'"}, new int[]{1, 2, 3}, new double[]{6, 7, 3}, 0.05, 0.08, 0.11, 0.0, null, true);
    }
    /**
     * Fantasy language that might be suitable for a language spoken by demons, aggressive warriors, or people who seek
     * to emulate or worship similar groups. The tendency here is for DEMONIC to be the language used by creatures that
     * are considered evil because of their violence, while INFERNAL would be the language used by creatures that are
     * considered evil because of their manipulation and deceit (DEMONIC being "chaotic evil" and INFERNAL being "lawful
     * evil"). This uses lots of sounds that don't show up in natural languages very often, mixing harsh or guttural
     * sounds like "kh" and "ghr" with rare sounds like "vr", "zv", and "tl". It uses vowel-splitting in a way that is
     * similar to LOVECRAFT, sometimes producing sounds like "tsa'urz" or "khu'olk".
     * <br>
     * Vrirvoks xatughat ogz; olds xu'oz xorgogh!
     */
    public static final FakeLanguageGen DEMONIC = demonic().register();

    private static FakeLanguageGen infernal(){
        return new FakeLanguageGen(
                new String[]{
                        "a", "a", "a", "à", "á", "â", "ä",
                        "e", "e", "e", "e", "e", "e", "e", "e", "è", "é", "ê", "ë",
                        "i", "i", "i", "i", "ì", "í", "î", "ï",
                        "o", "o", "ò", "ó", "ô", "ö",
                        "u", "u", "ù", "ú", "û", "ü",
                },
                new String[]{"æ", "ai", "aî", "i", "i", "î", "ï", "ia", "iâ", "ie", "iê", "eu", "eû", "u", "u", "û", "ü"},
                new String[]{"b", "br", "d", "dr", "h", "m", "z", "k", "l", "ph", "t", "n", "y", "th", "s", "sh",
                        "m", "m", "m", "z", "z", "l", "l", "l", "k", "k", "b", "d", "h", "h", "y", "th", "th", "s", "sh",
                },
                new String[]{
                        "mm", "mm", "mm", "lb", "dd", "dd", "dd", "ddr", "bb", "bb", "bb", "bbr", "lz", "sm", "zr",
                        "thsh", "lkh", "shm", "mh", "mh",
                },
                new String[]{
                        "b", "d", "h", "m", "n", "z", "k", "l", "ph", "t", "th", "s", "sh", "kh",
                        "h", "m", "n", "z", "l", "ph", "t", "th", "s",
                        "h", "h", "h", "m", "m", "n", "n", "n", "n", "n", "l", "l", "l", "l", "l", "t", "t", "t",
                        "th", "th", "s", "s", "z", "z", "z", "z",
                },
                new String[]{"ael", "im", "on", "oth", "et", "eus", "iel", "an", "is", "ub", "ez", "ath", "esh", "ekh", "uth", "ut"},
                new String[]{"'"}, new int[]{1, 2, 3, 4}, new double[]{3, 5, 9, 4}, 0.75, 0.35, 0.17, 0.07, genericSanityChecks, true);
    }
    /**
     * Fantasy language that might be suitable for a language spoken by fiends, users of witchcraft, or people who seek
     * to emulate or worship similar groups. The tendency here is for DEMONIC to be the language used by creatures that
     * are considered evil because of their violence, while INFERNAL is the language used by creatures that are
     * considered evil because of their manipulation and deceit (DEMONIC being "chaotic evil" and INFERNAL being "lawful
     * evil"). The name INFERNAL refers to Dante's Inferno and the various naming conventions used for residents of Hell
     * in the more-modern Christian traditions (as well as some of the stylistic conventions of Old Testament figures
     * described as false idols, such as Moloch and Mammon). In an effort to make this distinct from the general style
     * of names used in ancient Hebrew (since this is specifically meant for the names of villains as opposed to normal
     * humans), we add in vowel splits as used in LOVECRAFT and DEMONIC, then add quite a few accented vowels. These
     * traits make the language especially well-suited for "deal with the Devil" written bargains, where a single accent
     * placed incorrectly could change the meaning of a contract and provide a way for a fiend to gain leverage.
     * <br>
     * Zézîzûth eke'iez áhìphon; úhiah îbbëphéh haîtemheû esmez...
     */
    public static final FakeLanguageGen INFERNAL = infernal().register();

    private static FakeLanguageGen simplish(){
        return new FakeLanguageGen(
                new String[]{
                        "a", "a", "a", "a", "o", "o", "o", "e", "e", "e", "e", "e", "i", "i", "i", "i", "u",
                        "a", "a", "a", "a", "o", "o", "o", "e", "e", "e", "e", "e", "i", "i", "i", "i", "u",
                        "a", "a", "a", "a", "o", "o", "o", "e", "e", "e", "e", "e", "i", "i", "i", "i", "u",
                        "a", "a", "a", "o", "o", "e", "e", "e", "i", "i", "i", "u",
                        "a", "a", "a", "o", "o", "e", "e", "e", "i", "i", "i", "u",
                        "ai", "ai", "ea", "io", "oi", "ia", "io", "eo"
                },
                new String[]{"u", "u", "oa"},
                new String[]{
                        "b", "bl", "br", "c", "cl", "cr", "ch", "d", "dr", "f", "fl", "fr", "g", "gl", "gr", "h", "j", "k", "l", "m", "n",
                        "p", "pl", "pr", "r", "s", "sh", "sk", "st", "sp", "sl", "sm", "sn", "t", "tr", "th", "v", "w", "y", "z",
                        "b", "bl", "br", "c", "cl", "cr", "ch", "d", "dr", "f", "fl", "fr", "g", "gr", "h", "j", "k", "l", "m", "n",
                        "p", "pl", "pr", "r", "s", "sh", "st", "sp", "sl", "t", "tr", "th", "w", "y",
                        "b", "c", "ch", "d", "f", "g", "h", "j", "k", "l", "m", "n",
                        "p", "r", "s", "sh", "t", "th",
                        "b", "c", "ch", "d", "f", "g", "h", "j", "k", "l", "m", "n",
                        "p", "r", "s", "sh", "t", "th",
                        "b", "c", "ch", "d", "f", "g", "h", "j", "k", "l", "m", "n",
                        "p", "r", "s", "sh", "t", "th",
                        "b", "c", "ch", "d", "f", "g", "h", "j", "k", "l", "m", "n",
                        "p", "r", "s", "sh", "t", "th",
                        "b", "d", "f", "g", "h", "l", "m", "n",
                        "p", "r", "s", "sh", "t", "th",
                        "b", "d", "f", "g", "h", "l", "m", "n",
                        "p", "r", "s", "sh", "t", "th",
                        "r", "s", "t", "l", "n",
                },
                new String[]{"ch", "j", "w", "y", "v", "w", "y", "w", "y", "ch",
                        "b", "c", "d", "f", "g", "k", "l", "m", "n", "p", "r", "s", "sh", "t",
                },
                new String[]{"bs", "lt", "mb", "ng", "ng", "nt", "ns", "ps", "mp", "rt", "rg", "sk", "rs", "ts", "lk", "ct",
                        "b", "c", "d", "f", "g", "k", "l", "m", "n", "p", "r", "s", "sh", "t", "th", "z",
                        "b", "c", "d", "f", "g", "k", "l", "m", "n", "p", "r", "s", "sh", "t",
                        "b", "c", "d", "f", "g", "k", "l", "m", "n", "p", "r", "s", "sh", "t",
                        "d", "f", "g", "k", "l", "m", "n", "p", "r", "s", "sh", "t",
                        "d", "f", "g", "k", "l", "m", "n", "p", "r", "s", "sh", "t",
                        "d", "f", "g", "k", "l", "m", "n", "p", "r", "s", "sh", "t",
                        "d", "f", "g", "k", "l", "m", "n", "p", "r", "s", "sh", "t",
                },
                new String[]{},
                new String[]{}, new int[]{1, 2, 3, 4}, new double[]{7, 18, 6, 1}, 0.26, 0.12, 0.0, 0.0, genericSanityChecks, true);
    }
    /**
     * English-like language that omits complex spelling and doesn't include any of the uncommon word endings of English
     * like "ought" or "ation." A good choice when you want something that doesn't use any non-US-keyboard letters,
     * looks somewhat similar to English, and tries to be pronounceable without too much effort. This doesn't have any
     * doubled or silent letters, nor does it require special rules for pronouncing vowels like "road" vs. "rod", though
     * someone could make up any rules they want.
     * <br>
     * Fledan pranam, simig bag chaimer, drefar, woshash is sasik.
     */
    public static final FakeLanguageGen SIMPLISH = simplish().register();


    private static FakeLanguageGen alien_a(){
        return new FakeLanguageGen(
                new String[]{"a", "a", "a", "a", "a", "a", "a", "ai", "ai", "ao", "ao", "ae", "ae", "e", "e", "e", "e",
                        "ea", "eo", "i", "i", "i", "i", "i", "i", "ia", "ie", "io", "o", "o", "o", "oa"},
                new String[]{},
                new String[]{"c", "f", "h", "j", "l", "m", "n", "p", "q", "r", "s", "v", "w", "x", "y", "z",
                        "c", "h", "j", "l", "m", "n", "q", "r", "s", "v", "w", "x", "y", "z",
                        "h", "j", "l", "m", "n", "q", "r", "s", "v", "w", "x", "y", "z",
                        "hc", "hf", "hj", "hl", "hm", "hn", "hq", "hr", "hv", "hw", "hy", "hz",
                        "cr", "fr", "jr", "mr", "nr", "pr", "qr", "sr", "vr", "xr", "yr", "zr",
                        "cy", "fy", "jy", "my", "ny", "py", "qy", "ry", "sy", "vy", "xy", "zy",
                        "cl", "fl", "jl", "ml", "nl", "pl", "ql", "sl", "vl", "xl", "yl", "zl",
                },
                new String[]{
                        "cr", "fr", "jr", "mr", "nr", "pr", "qr", "sr", "vr", "xr", "yr", "zr",
                        "cy", "fy", "jy", "my", "ny", "py", "qy", "ry", "sy", "vy", "xy", "zy",
                        "cl", "fl", "jl", "ml", "nl", "pl", "ql", "sl", "vl", "xl", "yl", "zl",
                                    "jc", "lc", "mc", "nc", "qc", "rc", "sc",       "wc", "yc", "zc",
                        "cf",       "jf", "lf",       "nf", "qf", "rf", "sf", "vf", "wf", "yf", "zf",
                        "cj", "fj",       "lj", "mj", "nj", "qj", "rj", "sj",       "wj", "yj", "zj",
                        "cm", "fm", "jm", "lm",       "nm", "qm", "rm", "sm", "vm", "wm", "ym", "zm",
                        "cn", "fn", "jn", "ln", "mn",       "qn", "rn", "sn", "vn", "wn", "yn", "zn",
                        "cp", "fp", "jp", "lp", "mp", "np", "qp", "rp", "sp", "vp", "wp", "yp", "zp",
                        "cq",       "jq", "lq", "mq", "nq",       "rq", "sq",       "wq", "yq", "zq",
                        "cs", "fs", "js", "ls", "ms", "ns", "qs",             "vs", "ws", "ys", "zs",
                        "cv", "fv", "jv", "lv", "mv", "nv", "qv", "rv", "sv",       "wv", "yv", "zv",
                        "cw", "fw", "jw", "lw", "mw", "nw", "qw", "rw", "sw", "vw",       "yw", "zw",
                        "cx",       "jx", "lx", "mx", "nx", "qx", "rx",       "vx", "wx", "yx", "zx",
                        "cz", "fz",       "lz", "mz", "nz", "qz", "rz", "sz", "vz", "wz", "yz",
                },
                new String[]{
                        "c", "f", "h", "j", "l", "m", "n", "p", "q", "r", "s", "v", "w", "x", "y", "z",
                        "c", "h", "j", "l", "m", "n", "q", "r", "s", "v", "w", "x", "y", "z",
                        "h", "j", "l", "m", "n", "q", "r", "s", "v", "w", "x", "y", "z",
                        "hc", "hf", "hj", "hl", "hm", "hn", "hq", "hr", "hv", "hw", "hy", "hz",
                },
                new String[]{},
                new String[]{}, new int[]{1, 2, 3}, new double[]{1, 1, 1}, 0.65, 0.6, 0.0, 0.0, null, true);
    }

    /**
     * Fantasy/sci-fi language that could be spoken by some very-non-human culture that would typically be fitting for
     * an alien species. This alien language emphasizes unusual consonant groups and prefers the vowels 'a' and 'i',
     * sometimes with two different vowels in one syllable, like with 'ea', but never two of the same vowel, like 'ee'.
     * Many consonant groups may border on unpronounceable unless a different sound is meant by some letters, such as
     * 'c', 'h', 'q', 'x', 'w', and 'y'. In particular, 'x' and 'q' may need to sound like different breathy, guttural,
     * or click noises for this to be pronounced by humans effectively.
     * <br>
     * Jlerno iypeyae; miojqaexli qraisojlea epefsaihj xlae...
     */
    public static final FakeLanguageGen ALIEN_A = alien_a().register();

    private static FakeLanguageGen korean()
    {
        return new FakeLanguageGen(
                new String[]{
                        "a", "ae", "ya", "yae", "eo", "e", "yeo", "ye", "o", "wa", "wae",
                        "oe", "yo", "u", "wo", "we", "wi", "yu", "eu", "i",  "ui",
                        "a", "a", "a", "i", "i", "i", "i", "o", "o", "o", "o", "u", "u", "u", "u",
                        "ae", "ya", "eo", "eo", "eu", "eu", "wa", "wae", "wo", "oe", "oe",
                        "yo", "yo", "yu", "yu", "eu",
                },
                new String[]{},
                new String[]{
                        "g", "n", "d", "r", "m", "b", "s", "j", "ch", "k", "t", "p", "h",
                        "g", "n", "d", "b", "p", "k", "j", "ch", "h",
                        "g", "n", "d", "b", "p", "k", "j", "h",
                        "g", "n", "p", "k", "j",
                        "g", "p", "k",
                        "g", "p", "k",
                },
                new String[]{
                        "g", "kg", "ngn", "kd", "ngn", "ngm", "kb", "ks", "kj", "kch", "k-k", "kt", "kp", "k",
                        "n", "n-g", "nn", "nd", "nn", "nm", "nb", "ns", "nj", "nch", "nk", "nt", "np", "nh",
                        "d", "tg", "nn", "td", "nn", "nm", "tb", "ts", "tj", "tch", "tk", "t-t", "tp", "t",
                        "r", "lg", "nn", "ld", "ll", "lm", "lb", "ls", "lj", "lch", "lk", "lt", "lp", "lh",
                        "m", "mg", "mn", "md", "mn", "mm", "mb", "ms", "mj", "mch", "mk", "mt", "mp", "mh",
                        "b", "pg", "mn", "pd", "mn", "mm", "pb", "ps", "pj", "pch", "pk", "pt", "p-p", "p",
                        "s", "tg", "nn", "td", "nn", "nm", "tb", "ts", "tj", "tch", "tk", "t-t", "tp", "t",
                        "ng-", "ngg", "ngn", "ngd", "ngn", "ngm", "ngb", "ngs", "ngj", "ngch", "ngk", "ngt", "ngp", "ngh",
                        "j", "tg", "nn", "td", "nn", "nm", "tb", "ts", "tj", "tch", "tk", "t-t", "tp", "ch",
                        "t", "t", "t", "j", "j", "j", "g", "g", "g", "g", "n", "n", "n", "n", "n", "ng", "ng", "ng",
                        "d", "d", "d", "b", "b",
                        "tt", "nn", "kk", "kk", "ks",
                        "h", "k", "nn", "t", "nn", "nm", "p", "hs", "ch", "tch", "tk", "tt", "tp", "t",
                        "kk", "pp", "ss", "tt", "jj", "ks", "nch", "nh", "r",
                        "r", "r", "r", "r", "r", "r", "r", "r", "r", "r", "r", "r",
                        "ngg", "ngn", "ngm", "ngj", "ngch", "ngk", "ngp",
                        "mg", "mch", "mk", "md", "mb", "mp",
                        "nj", "nch", "nd", "nk", "nb", "nj", "nch", "nd", "nk",
                        "kg", "kj", "kch"
                },
                new String[]{
                        "k", "n", "t", "l", "m", "p", "k", "ng", "h", "n", "n",
                        "k", "n", "t", "l", "m", "p", "k", "ng", "h", "t",
                },
                new String[]{"ul", "eul", "eol", "ol",  "il", "yeol", "yol", "uk", "euk", "eok", "aek", "ok", "ak",
                        "on", "ong", "eong", "yang", "yong", "yeong", "ung", "wong", "om", "am", "im", "yuh", "uh", "euh",
                        "ap", "yaep", "eop", "wep", "yeop"
                },
                new String[]{"-"},
                new int[]{1, 2, 3, 4}, new double[]{14, 9, 3, 1}, 0.14, 0.24, 0.02, 0.09,
                null, true);
    }
    /**
     * Imitation text from an approximation of Korean, using the Revised Romanization method that is official in South
     * Korea today and is easier to type. The text this makes may be hard to pronounce. Korean is interesting as a
     * language to imitate for a number of reasons; many of the sounds in it are rarely found elsewhere, it can cluster
     * consonants rather tightly (most languages don't; English does to a similar degree but Japanese hardly has any
     * groups of consonants), and there are many more vowel sounds without using tones (here, two or three letters are
     * used for a vowel, where the first can be y or w and the rest can be a, e, i, o, or u in some combination). Some
     * letter combinations possible here are impossible or very rare in correctly-Romanized actual Korean, such as the
     * rare occurrence of a single 'l' before a vowel (it normally only appears in Romanized text before a consonant or
     * at the end of a word).
     * <br>
     * Hyeop euryam, sonyon muk tyeok aengyankeon, koelgwaelmwak.
     */
    public static final FakeLanguageGen KOREAN_ROMANIZED = korean().register();

    private static FakeLanguageGen alien_e(){
        return new FakeLanguageGen(
                new String[]{"a", "a", "a", "a", "a", "a", "aa", "aa",
                        "e", "e", "e", "e", "e", "e", "e", "e", "e", "e", "ee", "ee", "ee", "ee",
                        "i", "i", "i", "i", "i", "ii",
                        "o", "o", "o", "o",
                        "u", "u", "u"
                },
                new String[]{},
                new String[]{"t", "k", "c", "g", "z", "s", "d", "r", "ts",
                        "tr", "kr", "cr", "gr", "zr", "st", "sk", "dr",
                        "tq", "kq", "cq", "gq", "zq", "sq", "dq",
                        "tq", "kq", "cq", "gq", "zq", "sq", "dq",
                        "tq", "kq", "cq", "gq", "zq", "sq", "dq",
                        "t", "k", "c", "g", "r", "ts", "t", "k", "c", "g", "r", "ts",
                        "t", "k", "c", "g", "r", "ts", "t", "k", "c", "g", "r", "ts",
                        "t", "k", "c", "g", "r", "ts", "t", "k", "c", "g", "r", "ts",
                        "t", "k", "ts", "t", "k", "ts", "t", "k", "ts", "t", "k", "ts",
                        "t", "k", "ts", "t", "k", "ts", "t", "k", "ts", "t", "k", "ts",
                        "t", "k", "t", "k", "t", "k", "t", "k", "t", "k", "t", "k",
                        "tr", "kr", "st", "sk", "tq", "kq", "sq"
                },
                new String[]{
                        "tt", "kk", "cc", "gg", "zz", "dd", "s", "r", "ts",
                        "tr", "kr", "cr", "gr", "zr", "st", "sk", "dr",
                        "tq", "kq", "cq", "gq", "zq", "sq", "dq",
                        "tq", "kq", "cq", "gq", "zq", "sq", "dq",
                        "tq", "kq", "cq", "gq", "zq", "sq", "dq",
                        "tk", "kt", "tc", "ct", "gt", "tg", "zt", "tz", "td", "dt", "rt", "rtr", "tst",
                        "kc", "ck", "gk", "kg", "zk", "kz", "kd", "dk", "rk", "rkr", "tsk", "kts",
                        "gc", "cg", "zc", "cz", "cd", "dc", "rc", "rcr", "tsc", "cts",
                        "zg", "gz", "gd", "dg", "rg", "rgr", "tsg", "gts",
                        "zd", "dz", "rz", "rzr", "tsz", "zts",
                        "rd", "rdr", "tsd", "dts",
                        "tt", "tt", "tt", "tt", "tt", "tt",
                        "tt", "tt", "tt", "tt", "tt", "tt",
                        "kk", "kk", "kk", "kk", "kk", "kk",
                        "kk", "kk", "kk", "kk", "kk", "kk",
                        "kt", "tk", "kt", "tk", "kt", "tk", "kt", "tk",
                },
                new String[]{
                        "t", "k", "c", "g", "z", "s", "d", "r", "ts",
                        "t", "k", "t", "k", "t", "k", "ts",
                        "t", "k", "c", "g", "z", "s", "d", "r", "ts",
                        "t", "k", "t", "k", "t", "k", "ts",
                        "st", "sk", "sc", "sg", "sz", "ds",
                        "rt", "rk", "rc", "rg", "rz", "rd", "rts"
                },
                new String[]{},
                new String[]{}, new int[]{1, 2, 3}, new double[]{5, 4, 2}, 0.45, 0.0, 0.0, 0.0, null, true);
    }

    /**
     * Fantasy/sci-fi language that could be spoken by some very-non-human culture that would typically be fitting for
     * an alien species. This alien language emphasizes hard sounds and prefers the vowels 'e' and 'a', sometimes with
     * two of the same vowel, like 'ee', but never with two different vowels in one syllable, like with 'ea'.
     * This language is meant to use click sounds, if pronunciation is given, where 'q' modifies a consonant to form a
     * click, such as 'tq'. This is like how 'h' modifies letters in English to make 'th' different from 't' or 'h'.
     * This may be ideal for a species with a beak (or one that lacks lips for some other reason), since it avoids using
     * sounds that require lips (some clicks might be approximated by other species using their lips if this uses some
     * alien-specific clicking organ).
     * <br>
     * Reds zasg izqekkek zagtsarg ukaard ac ots as!
     */
    public static final FakeLanguageGen ALIEN_E = alien_e().register();

    private static FakeLanguageGen alien_i(){
        return new FakeLanguageGen(
                new String[]{
                        "a", "a", "a", "a", "a", "a", "à", "á", "â", "ā", "ä",
                        "e", "e", "e", "e", "e", "e", "è", "é", "ê", "ē", "ë",
                        "i", "i", "i", "i", "i", "i", "i", "i", "ì", "í", "î", "ï", "ī",
                        "i", "i", "i", "i", "i", "i", "i", "i", "ì", "í", "î", "ï", "ī",
                        "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "o", "ò", "ó", "ô", "ō", "ö",
                        "u", "u", "u", "u", "u", "u", "ù", "ú", "û", "ū", "ü",
                },
                new String[]{},
                new String[]{
                        "r", "l", "ch", "g", "z", "zh", "s", "sh", "th", "m", "n", "p", "b", "j", "v", "h", "r", "l",
                        "r", "l", "ch", "g", "z", "zh", "s", "sh", "th", "m", "n", "p", "b", "j", "v", "h", "r", "l",
                        "r", "l", "ch", "g", "z", "zh", "s", "sh", "th", "m", "n", "p", "b", "j", "v", "h", "r", "l",
                        "r", "r", "r", "r", "r", "l", "l", "l", "l", "l",
                        "gr", "gl", "zr", "zl", "sl", "shr", "thr", "mr", "nr", "pr", "pl", "br", "bl", "vr", "vl", "hr",
                        "zv", "sp", "zg"
                },
                new String[]{
                        "j", "h",
                },
                new String[]{
                        "r", "l", "ch", "g", "z", "zh", "s", "sh", "th", "m", "n", "p", "b", "v", "r", "l",
                        "th", "zh", "sh", "th", "zh", "sh", "lth", "lzh", "lsh", "rth", "rzh", "rsh",
                },
                new String[]{},
                new String[]{"'"}, new int[]{1, 2, 3, 4}, new double[]{6, 9, 5, 1}, 0.6, 0.4, 0.075, 0.0, null, true);
    }

    /**
     * Fantasy/sci-fi language that could be spoken by some very-non-human culture that would typically be fitting for
     * an alien species. This alien language emphasizes "liquid" sounds such as 'l', 'r', and mixes with those and other
     * consonants, and prefers the vowels 'i' and 'o', never with two of the same vowel, like 'ee', nor with two
     * different vowels in one syllable, like with 'ea'; it uses accent marks heavily and could be a tonal language.
     * It sometimes splits vowels with a single apostrophe, and rarely has large consonant clusters.
     * <br>
     * Asherzhäl zlómór ìsiv ázá nralthóshos, zlôbùsh.
     */
    public static final FakeLanguageGen ALIEN_I = alien_i().register();

    private static FakeLanguageGen alien_o(){
        return new FakeLanguageGen(
                new String[]{
                        "a", "e", "i", "o", "o", "o", "o", "u",
                        "aa", "ea", "ia", "oa", "oa", "oa", "ua", "ae", "ai", "ao", "ao", "ao", "au",
                        "ee", "ie", "oe", "oe", "oe", "ue", "ei", "eo", "eo", "eo", "eu",
                        "ii", "oi", "oi", "oi", "ui", "io", "io", "io", "iu",
                        "oo", "ou", "uo", "oo", "ou", "uo", "oo", "ou", "uo", "uu",
                        "aa", "ea", "ia", "oa", "oa", "oa", "ua", "ae", "ai", "ao", "ao", "ao", "au",
                        "ee", "ie", "oe", "oe", "oe", "ue", "ei", "eo", "eo", "eo", "eu",
                        "ii", "oi", "ui", "io", "io", "io", "iu",
                        "oo", "ou", "uo", "oo", "ou", "uo", "oo", "ou", "uo", "uu",
                        "aea", "aia", "aoa", "aoa", "aoa", "aua", "eae", "eie", "eoe", "eoe", "eoe", "eue",
                        "iai", "iei", "ioi", "ioi", "ioi", "iui", "uau", "ueu", "uiu", "uou",
                        "oao", "oeo", "oio", "ouo", "oao", "oeo", "oio", "ouo", "oao", "oeo", "oio", "ouo",
                        "aei", "aeo", "aeo", "aeo", "aeu", "aie", "aio", "aio", "aio", "aiu",
                        "aoe", "aoi", "aou", "aoe", "aoi", "aou", "aoe", "aoi", "aou", "aue", "aui", "auo", "auo", "auo",
                        "eai", "eao", "eao", "eao", "eau", "eia", "eio", "eio", "eio", "eiu",
                        "eoa", "eoi", "eou", "eoa", "eoi", "eou", "eoa", "eoi", "eou", "eua", "eui", "euo", "euo", "euo",
                        "iae", "iao", "iao", "iao", "iau", "iea", "ieo", "ieo", "ieo", "ieu",
                        "ioa", "ioe", "iou", "ioa", "ioe", "iou", "ioa", "ioe", "iou", "iua", "iue", "iuo", "iuo", "iuo",
                        "oae", "oai", "oau", "oea", "oei", "oeu", "oia", "oie", "oiu", "oua", "oue", "oui",
                        "oae", "oai", "oau", "oea", "oei", "oeu", "oia", "oie", "oiu", "oua", "oue", "oui",
                        "oae", "oai", "oau", "oea", "oei", "oeu", "oia", "oie", "oiu", "oua", "oue", "oui",
                        "uae", "uai", "uao", "uao", "uao", "uea", "uei", "ueo", "ueo", "ueo", "uia", "uie",
                        "uio", "uoa", "uoe", "uoi", "uio", "uoa", "uoe", "uoi", "uio", "uoa", "uoe", "uoi",
                },
                new String[]{},
                new String[]{
                        "m", "n", "r", "w", "h", "v", "f", "l", "y",
                        "m", "n", "r", "w", "h", "v", "f", "l", "y",
                        "m", "n", "r", "w", "h", "v", "f", "l", "y",
                        "m", "n", "r", "w", "h", "v", "f", "l", "y",
                        "m", "n", "r", "w", "h", "v", "f", "l", "y",
                        "hm", "hn", "hr", "hw", "hv", "hl", "hy",
                        "fm", "fn", "fr", "fw", "fv", "fl", "fy",
                        "mr", "vr", "ry"
                },
                new String[]{
                        "m", "n", "r", "w", "h", "v", "f", "l", "y",
                        "m", "n", "r", "w", "h", "v", "f", "l", "y",
                        "m", "n", "r", "w", "h", "v", "f", "l", "y",
                        "m", "n", "r", "w", "h", "v", "f", "l", "y",
                        "mm", "nn", "rr", "ww", "hh", "vv", "ff", "ll", "yy",
                        "mm", "nn", "rr", "ww", "hh", "vv", "ff", "ll", "yy",
                        "hm", "hn", "hr", "hw", "hv", "hl", "hy",
                        "fm", "fn", "fr", "fw", "fv", "fl", "fy",
                        "mr", "vr", "ry"
                },
                new String[]{
                        "m", "n", "r", "h", "v", "f", "l",
                        "m", "n", "r", "h", "v", "f", "l",
                        "m", "n", "r", "h", "v", "f", "l",
                        "rm", "rn", "rv", "rf", "rl",
                        "lm", "ln", "lv", "lf"
                },
                new String[]{},
                new String[]{}, new int[]{1, 2, 3}, new double[]{3, 6, 4}, 0.0, 0.55, 0.0, 0.0, null, true);
    }

    /**
     * Fantasy/sci-fi language that could be spoken by some very-non-human culture that would typically be fitting for
     * an alien species. This alien language emphasizes large clusters of vowels, typically with 2 or 3 vowel sounds
     * between consonants, though some vowel groups could be interpreted in multiple ways (such as English "maim" and
     * "bail", which also have regional differences in pronunciation). As the name would suggest, it strongly prefers
     * using the vowel "o", with it present in about half the groups, but doesn't have any preference toward or against
     * the other vowels it uses, "a", "e", "i", and "u". The consonants completely avoid hard sounds like "t" and "k",
     * medium-hard sounds like "g" and "b", and also sibilants like "s" and "z".  This should be fairly hard to
     * pronounce, but possible.
     * <br>
     * Foiuhoeorfeaorm novruol naionouffeu meuif; hmoieloreo naemriou.
     */
    public static final FakeLanguageGen ALIEN_O = alien_o().register();

    // àáâãäåæāăąǻǽaèéêëēĕėęěeìíîïĩīĭįıiòóôõöøōŏőœǿoùúûüũūŭůűųuýÿŷỳ
    // çðþñýćĉċčďđĝğġģĥħĵķĺļľŀłńņňŋŕŗřśŝşšţťŵŷÿźżžșțẁẃẅ
    private static FakeLanguageGen alien_u(){
        return new FakeLanguageGen(
                new String[]{
                        "a", "a", "a", "a", "ä", "i", "o", "o", "o", "ö", "u", "u", "u", "u", "u", "u", "ü", "ü"
                },
                new String[]{},
                new String[]{
                        "b", "b", "b", "b", "d", "d", "g", "g", "ġ", "h", "h", "h", "h", "ħ",
                        "l", "l", "l", "l", "ł", "m", "m", "m", "m", "m", "n", "n", "n", "n", "ñ", "ŋ", "p", "p", "p",
                        "q", "q", "r", "r", "r", "ŕ", "s", "s", "s", "s", "ś", "v", "v", "v", "v",
                        "w", "w", "w", "w", "ẃ", "y", "y", "y", "y", "ý"
                },
                new String[]{
                        "b", "b", "b", "b", "d", "d", "g", "g", "ġ", "h", "h", "h", "h", "ħ",
                        "l", "l", "l", "l", "ł", "m", "m", "m", "m", "m", "n", "n", "n", "n", "ñ", "ŋ", "p", "p", "p",
                        "q", "q", "r", "r", "r", "ŕ", "s", "s", "s", "s", "ś", "v", "v", "v", "v",
                        "w", "w", "w", "w", "ẃ", "y", "y", "y", "y", "ý"
                },
                new String[]{
                        "b", "b", "b", "b", "d", "d", "g", "g", "ġ",
                        "l", "l", "l", "l", "ł", "m", "m", "m", "m", "m", "n", "n", "n", "n", "ñ", "ŋ", "p", "p", "p",
                        "r", "r", "r", "ŕ", "s", "s", "s", "s", "ś", "v", "v", "v", "v",
                },
                new String[]{"emb", "embrid", "embraŋ", "eŋ", "eŋul", "eŋov", "eẃul", "eẃuld", "eẃulb",
                        "eviś", "evim", "ełurn", "ełav", "egiġ", "ergiġ", "elgiġ", "eŕu", "eŕup", "eŕulm", "eŕuv",
                        "eħul", "eħid", "eħiŋ", "eyü", "eyür", "eyürl", "eyüld", "eyüns", "eqä", "eqäp", "eqäġ",
                        "esu", "esumb", "esulg", "esurl", "eśo", "eśold", "eśolg", "eśu", "eśur", "eśuŋ",
                        "eñu", "eñuns", "eñurn", "eño", "eñolb", "eñols"
                },
                new String[]{"'"}, new int[]{1, 2, 3, 4, 5}, new double[]{3, 4, 7, 5, 2}, 0.4, 0.15, 0.06, 0.5, null, true);
    }

    /**
     * Fantasy/sci-fi language that could be spoken by some very-non-human culture that would typically be fitting for
     * an alien species. This alien language is meant to have an abrupt change mid-word for many words, with the suffix
     * of roughly half of words using the letter "e", which is absent from the rest of the language; these suffixes can
     * also use consonant clusters, which are similarly absent elsewhere. The suffixes would make sense as a historical
     * relic or as a linguistic holdout from a historical merger. As the name would suggest, it strongly prefers
     * using the vowel "u", with it present in about half the groups, and can use the umlaut accent "ü" on some vowels.
     * The consonants completely avoid hard sounds like "t" and "k", and don't cluster; they often have special marks.
     * This should be relatively easy to pronounce for an alien language, though the words are rather long.
     * <br>
     * Üweħid vuŕeħid deẃul leŋul waloyeyür; äyovavü...
     */
    public static final FakeLanguageGen ALIEN_U = alien_u().register();

    private static FakeLanguageGen dragon(){
        return new FakeLanguageGen(
                new String[]{
                        "a", "a", "a", "e", "e", "i", "i", "o", "o", "u",
                        "a", "a", "a", "e", "e", "i", "i", "o", "o", "u",
                        "a", "a", "a", "e", "e", "i", "i", "o", "o", "u",
                        "a", "a", "a", "e", "e", "i", "i", "o", "o", "u",
                        "a", "a", "a", "a", "a", "a", "e", "i", "o",
                        "ai", "ai", "aa", "ae", "au", "ea", "ea", "ea",
                        "ia", "ia", "ie", "io", "io", "oa", "ou"
                },
                new String[]{
                        "aa", "aa", "aa", "ai", "ae", "ae", "ae", "au", "au",
                        "ea", "ea", "eo", "eo",
                        "ii", "ii", "ia", "ia", "ia", "ia", "ie", "ie", "ie", "io", "io", "io",
                        "oa", "ou", "ou", "ou", "ou"
                },
                new String[]{
                        "ch", "d", "f", "g", "h", "k", "l", "m", "n", "p", "r", "t", "th", "v", "w", "y", "z",
                        "ch", "d", "f", "g", "h", "k", "l", "m", "n", "p", "r", "t", "th", "v", "w", "y", "z",
                        "d", "f", "g", "h", "k", "l", "m", "n", "r", "t", "th", "v", "z",
                        "d", "f", "g", "h", "k", "l", "n", "r", "t", "th", "v", "z",
                        "d", "f", "g", "h", "l", "k", "l", "n", "r", "t", "th", "v", "z",
                        "d", "g", "h", "k", "l", "n", "r", "t", "th", "v", "z",
                        "d", "g", "h", "k", "l", "n", "r", "t", "th", "v", "z",
                        "d", "g", "k", "l", "r", "t",
                        "d", "g", "k", "l", "r", "t",
                        "d", "g", "k", "l", "r", "t",
                        "k", "k", "t", "t", "v",
                        "k", "k", "t", "t", "th",
                        "k", "k", "t", "t", "ch",
                        "dr", "fr", "gr", "hr", "kr", "tr", "thr",
                        "dr", "fr", "gr", "hr", "kr", "tr", "thr",
                        "dr", "fr", "gr", "hr", "kr", "tr", "thr",
                        "dr", "gr", "hr", "kr", "tr", "thr", "dr", "gr", "kr", "tr",
                        "dr", "gr", "hr", "kr", "tr", "thr", "dr", "gr", "kr", "tr",
                },
                new String[]{
                        "rch", "rd", "rg", "rk", "rm", "rn", "rp", "rt", "rth", "rv", "rw", "rz",
                        "rch", "rd", "rg", "rk", "rm", "rn", "rp", "rt", "rth", "rv", "rw", "rz",
                        "rdr", "rgr", "rkr", "rtr", "rthr",
                        "lk", "lt", "lv", "lz",
                        "ng", "nk", "ng", "nk", "ng", "nk", "ng", "nk", "nt", "nth", "nt", "nth", "nt", "nth", "nd",
                        "ngr", "nkr", "ntr", "nthr",
                        "dh", "gh", "lh", "mh", "nh", "rh",
                        "dch", "dg", "dk", "dth", "dv", "dz",
                        "kch", "kg", "kd", "kth", "kv", "kz",
                        "gch", "gd", "gk", "gth", "gv", "gz",
                        "tch", "tg", "tk", "ty", "tv", "tz",
                        "zm", "zn", "zk", "zv", "zt", "zg", "zd",

                        "ch", "d", "f", "g", "h", "k", "l", "m", "n", "p", "r", "t", "th", "v", "w", "y", "z",
                        "ch", "d", "f", "g", "h", "k", "l", "m", "n", "p", "r", "t", "th", "v", "w", "y", "z",
                        "d", "f", "g", "h", "k", "l", "m", "n", "r", "t", "th", "v", "z",
                        "d", "f", "g", "h", "k", "l", "n", "r", "t", "th", "v", "z",
                        "d", "f", "g", "h", "k", "l", "n", "r", "t", "th", "v",
                        "d", "g", "k", "l", "n", "r", "t", "th", "v",
                        "d", "g", "k", "l", "n", "r", "t", "th", "v",
                        "d", "g", "k", "l", "r", "t",
                        "d", "g", "k", "l", "r", "t",
                        "d", "g", "k", "l", "r", "t",
                        "k", "k", "t", "t", "r",
                        "k", "k", "t", "t", "r",
                        "k", "k", "t", "t", "r",
                        "dr", "fr", "gr", "hr", "kr", "tr", "thr",
                        "dr", "fr", "gr", "hr", "kr", "tr", "thr",
                        "dr", "fr", "gr", "hr", "kr", "tr", "thr",
                        "dr", "gr", "hr", "kr", "tr", "thr", "dr", "gr", "kr", "tr",
                        "dr", "gr", "hr", "kr", "tr", "thr", "dr", "gr", "kr", "tr",

                },
                new String[]{
                        "z", "z", "z", "t", "t", "t", "n", "r", "k", "th"
                },
                new String[]{"iamat", "at", "ut", "ok", "iok", "ioz", "ez", "ion", "ioth", "aaz", "iel"},
                new String[]{}, new int[]{2, 3, 4, 5}, new double[]{2, 7, 10, 3}, 0.14, 0.04, 0.0, 0.11, genericSanityChecks, true);
    }

    /**
     * Fantasy language that tries to sound like the speech of a powerful and pompous dragon, using long, complex words
     * and a mix of hard consonants like "t" and "k", "liquid" consonants like "l" and "r", and sometimes vowel groups
     * like "ie" and "aa". It frequently uses consonant clusters involving "r". It uses no accented characters.
     * <br>
     * Vokegodzaaz kigrofreth ariatarkioth etrokagik deantoznik hragriemitaaz gianehaadaz...
     */
    public static final FakeLanguageGen DRAGON = dragon().register();

    /**
     * Fantasy language based closely on {@link #DRAGON}, but with much shorter words normally and closing syllables
     * that may sound "rushed" or "crude", though it has the same general frequency of most consonants and vowels.
     * This means it still uses lots of "t", "k", and "r", can group two vowels sometimes, and when there's a consonant
     * in the middle of a word, it is often accompanied by an "r" on one or both sides. If used with
     * {@link NaturalLanguageCipher}, this will look very similar to DRAGON, because the syllable lengths aren't
     * determined by this object but by the text being ciphered. Still, the ends of words are often different. It is
     * called KOBOLD because, even though the original kobold myth was that of a goblin-like spirit that haunted cobalt
     * mines, the modern RPG treatment of kobolds frequently describes them as worshippers of dragons or in some way
     * created by dragons, but generally they're a sort of failure to live up to a dragon's high expectations. The feel
     * of this language is meant to be something like a dragon's speech, but much less "fancy" and rather curt.
     * <br>
     * Thritriz, laazak gruz kokak thon lut...
     */
    public static final FakeLanguageGen KOBOLD = new FakeLanguageGen(
            DRAGON.openingVowels, DRAGON.midVowels, DRAGON.openingConsonants, DRAGON.midConsonants, DRAGON.closingConsonants,
            new String[]{"ik", "ak", "ek", "at", "it", "ik", "ak", "ek", "at", "it", "ik", "ak", "ek", "at", "it", "et", "ut", "ark", "irk", "erk"},
            DRAGON.vowelSplitters, new int[]{1, 2, 3}, new double[]{5, 11, 1},
            0.1, 0.0, 0.0, 0.22, genericSanityChecks, true);

    /**
     * An array that stores all the hand-made FakeLanguageGen constants; it does not store randomly-generated languages
     * nor does it store modifications or mixes of languages. The order these are stored in is related to the numeric
     * codes for languages in the {@link #serializeToString()} output, but neither is dependent on the other if this
     * array is changed for some reason (which is not recommended, but not out of the question). If this is modified,
     * then it is probably a bad idea to assign null to any elements in registered; special care is taken to avoid null
     * elements in its original state, so some code may rely on the items being usable and non-null.
     */
    public static final FakeLanguageGen[] registered;

    static {
        // the first item in registry is null so it can be a placeholder for random languages; we want to skip it.
        registered = new FakeLanguageGen[registry.size()-1];
        for (int i = 0; i < registered.length; i++) {
            registered[i] = registry.getAt(i+1);
        }
    }
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
                new String[]{}, new int[]{1, 2, 3, 4}, new double[]{10, 11, 4, 1}, 0.22, 0.1, 0.0, 0.22, englishSanityChecks, true);
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

    private static String[] processParts(OrderedMap<String, String> parts, Set<String> missingSounds,
                                         Set<String> forbidden, RNG rng, double repeatSingleChance,
                                         int preferredLimit) {
        int l, sz = parts.size();
        List<String> working = new ArrayList<>(sz * 24);
        String pair;
        for (int e = 0; e < parts.size(); e++) {
            Map.Entry<String, String> sn = parts.entryAt(e);
            if (missingSounds.contains(sn.getKey()))
                continue;
            for (String t : sn.getValue().split(" ")) {
                if (forbidden.contains(t))
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
                        working.add(t);
                        c = t.charAt(0);
                        num = 0;
                        boolean repeat = true;
                        switch (c) {
                            case 'w':
                                num += 2;
                            case 'y':
                            case 'h':
                                num += 4;
                            case 'q':
                            case 'x':
                                num += 4;
                                repeat = false;
                                break;
                            case 'i':
                            case 'u':
                                repeat = false;
                                num = 13;
                                break;
                            case 'z':
                            case 'v':
                                num = 4;
                                break;
                            case 'j':
                                num = 7;
                                break;
                            default:
                                if (e >= preferredLimit)
                                    num = 6;
                                else
                                    num = 13;
                        }
                        for (int i = 0; i < num * 3; i++) {
                            if (rng.nextDouble() < 0.75) {
                                working.add(t);
                            }
                        }

                        if (repeat && rng.nextDouble() < repeatSingleChance) {
                            pair = t + t;
                            if (missingSounds.contains(pair))
                                continue;
                            working.add(pair);
                            working.add(pair);
                            working.add(pair);
                            if (rng.nextDouble() < 0.7) {
                                working.add(pair);
                                working.add(pair);
                            }
                            if (rng.nextDouble() < 0.7) {
                                working.add(pair);
                            }
                        }

                        break;
                    case 2:
                        if (rng.nextDouble() < 0.65) {
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

                                    if (e >= preferredLimit)
                                        num = 2;
                                    else
                                        num = 7;
                            }
                            working.add(t);
                            for (int i = 0; i < num; i++) {
                                if (rng.nextDouble() < 0.25) {
                                    working.add(t);
                                }
                            }
                        }
                        break;
                    case 3:
                        if (rng.nextDouble() < 0.5) {
                            c = t.charAt(0);
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
                                    if (e >= preferredLimit)
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
        return working.toArray(new String[0]);
    }

    /*private static final String[][] openVowels = new String[][]{
            new String[]{"a", "a", "a", "a", "aa", "ae", "ai", "au", "ea", "ia", "oa", "ua",},
            new String[]{"e", "e", "e", "e", "ae", "ea", "ee", "ei", "eo", "eu", "ie", "ue",},
            new String[]{"i", "i", "i", "i", "ai", "ei", "ia", "ie", "io", "iu", "oi", "ui",},
            new String[]{"o", "o", "o", "o", "eo", "io", "oa", "oi", "oo", "ou",},
            new String[]{"u", "u", "u", "u", "au", "eu", "iu", "ou", "ua", "ue", "ui",},
    };
*/

    public static FakeLanguageGen randomLanguage(RNG rng) {
        return randomLanguage(rng.nextLong());
    }

    public static FakeLanguageGen randomLanguage(long seed) {
        StatefulRNG rng = new StatefulRNG(seed);
        int[] lengths = new int[rng.between(3, 5)];
        System.arraycopy(new int[]{1, 2, 3, 4}, 0, lengths, 0, lengths.length);
        double[] chances = new double[lengths.length];
        System.arraycopy(new double[]{
                5 + rng.nextDouble(4), 13 + rng.nextDouble(9), 3 + rng.nextDouble(3), 1 + rng.nextDouble(2)
        }, 0, chances, 0, chances.length);
        double vowelHeavy = rng.between(0.2, 0.5), removalRate = rng.between(0.15, 0.65);
        int sz = openCons.size();
        int[] reordering = rng.randomOrdering(sz), vOrd = rng.randomOrdering(openVowels.size());
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
        p0s = parts0.size();
        sz0 = Math.max(rng.between(1, p0s + 1), rng.between(1, p0s + 1));
        char[] nextAccents = new char[sz0], unaccented = new char[sz0];
        int vowelAccent = rng.between(1, 7);
        for (int i = 0; i < sz0; i++) {
            nextAccents[i] = accentedVowels[vOrd[i + mn]][vowelAccent];
            unaccented[i] = accentedVowels[vOrd[i + mn]][0];
        }
        if (rng.nextDouble() < 0.8) {
            for (int i = 0; i < sz0; i++) {
                char ac = nextAccents[i], ua = unaccented[i];
                String v = "", uas = String.valueOf(ua);
                Pattern pat = Pattern.compile("\\b([aeiou]*)(" + ua + ")([aeiou]*)\\b");
                Replacer rep = pat.replacer("$1$2$3 $1" + ac + "$3"), repLess = pat.replacer("$1" + ac + "$3");
                for (int j = 0; j < p0s; j++) {
                    String k = parts0.keyAt(j);
                    if (uas.equals(k))
                        v = parts0.getAt(j);
                    else {
                        String current = parts0.getAt(j);
                        String[] splits = current.split(" ");
                        for (int s = 0; s < splits.length; s++) {
                            if (forbidden.contains(uas) && splits[s].contains(uas))
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

        for (; n < sz * removalRate; n++) {
            missingSounds.add(parts1.keyAt(n));
            missingSounds.add(parts2.keyAt(n));
            missingSounds.add(parts3.keyAt(n));
            Collections.addAll(forbidden, parts1.getAt(n).split(" "));
            Collections.addAll(forbidden, parts2.getAt(n).split(" "));
            Collections.addAll(forbidden, parts3.getAt(n).split(" "));
        }

        return new FakeLanguageGen(
                processParts(parts0, missingSounds, forbidden, rng, 0.0, p0s),
                new String[]{},
                processParts(openCons, missingSounds, forbidden, rng, 0.0, 4096),
                processParts(midCons, missingSounds, forbidden, rng, (rng.nextDouble() * 3 - 0.75) * 0.4444, 4096),
                processParts(closeCons, missingSounds, forbidden, rng, (rng.nextDouble() * 3 - 0.75) * 0.2857, 4096),
                new String[]{},
                new String[]{}, lengths, chances, vowelHeavy, vowelHeavy * 1.8, 0.0, 0.0, genericSanityChecks, true).summarize("0#" + seed + "@1");
    }

    protected static boolean checkAll(CharSequence testing, Pattern[] checks) {
        CharSequence fixed = removeAccents(testing);
        for (int i = 0; i < checks.length; i++) {
            if (checks[i].matcher(fixed).find())
                return false;
        }
        return true;
    }

    /**
     * Checks a CharSequence, such as a String, against an overzealous vulgarity filter, returning true if the text
     * could contain vulgar elements or words that could seem vulgar or juvenile. The idea here is that false positives
     * are OK as long as there are very few false negatives (missed vulgar words). Does not check punctuation or numbers
     * that could look like letters.
     * @param testing the text, as a CharSequence such as a String, to check
     * @return true if the text could contain a vulgar or juvenile element; false if it probably doesn't
     */
    public static boolean checkVulgarity(CharSequence testing)
    {
        CharSequence fixed = removeAccents(testing);
        for (int i = 0; i < vulgarChecks.length; i++) {
            if (vulgarChecks[i].matcher(fixed).find())
            {
                System.out.println(vulgarChecks[i]);
                return true;
            }
        }
        return false;
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
            StringBuilder sb = new StringBuilder(rng.getRandomElement(openingVowels));
            for (Modifier mod : modifiers) {
                sb = mod.modify(rng, sb);
            }
            if (capitalize) sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            return sb.toString();
        }
        while (true) {
            StringBuilder sb = new StringBuilder(20), ender = new StringBuilder(12);
            int i = 0;
            if (rng.nextDouble() < vowelStartFrequency) {
                sb.append(rng.getRandomElement(openingVowels));
                if (approxSyllables == 1 && closingConsonants.length > 0)
                    sb.append(rng.getRandomElement(closingConsonants));
                else if (midConsonants.length > 0)
                    sb.append(rng.getRandomElement(midConsonants));
                i++;
            } else if (openingConsonants.length > 0) {
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

            if (additionalChecks != null && !checkAll(sb, additionalChecks))
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
            StringBuilder sb = new StringBuilder(rng.getRandomElement(openingVowels));
            for (Modifier mod : modifiers) {
                sb = mod.modify(rng, sb);
            }
            if (capitalize) sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            return sb.toString();
        }
        int numSeeds, fraction = 1;
        if (reseeds != null)
            numSeeds = Math.min(reseeds.length, approxSyllables - 1);
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
                if (numSeeds > 0 && i > 0 && i == approxSyllables * fraction / (1 + numSeeds))
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
                if (numSeeds > 0 && i > 0 && i == approxSyllables * fraction / (1 + numSeeds))
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
     * @param minWords an int for the minimum number of words in a sentence; should be at least 1
     * @param maxWords an int for the maximum number of words in a sentence; should be at least equal to minWords
     * @return a sentence in the gibberish language as a String
     */
    public String sentence(RNG rng, int minWords, int maxWords) {
        return sentence(rng, minWords, maxWords, new String[]{",", ",", ",", ";"},
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
        if (endPunctuation != null && endPunctuation.length > 0)
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
        if(maxChars < 0)
            return sentence(rng, minWords, maxWords, midPunctuation, endPunctuation, midPunctuationFrequency);
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

        if (endPunctuation != null && endPunctuation.length > 0) {

            next = rng.getRandomElement(endPunctuation);
            if (sb.length() + next.length() >= maxChars)
                sb.append('.');
            else
                sb.append(next);
        }

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

        RNG rng = new RNG((hash64() & 0xffffffffL) | ((other.hash64() & 0xffffffffL) << 32)
                ^ NumberTools.doubleToLongBits(otherInfluence));

        String[] ov = merge1000(rng, openingVowels, other.openingVowels, otherInfluence),
                mv = merge1000(rng, midVowels, other.midVowels, otherInfluence),
                oc = merge1000(rng, openingConsonants, other.openingConsonants, otherInfluence *
                        Math.max(0.0, Math.min(1.0, 1.0 - other.vowelStartFrequency + vowelStartFrequency))),
                mc = merge1000(rng, midConsonants, other.midConsonants, otherInfluence),
                cc = merge1000(rng, closingConsonants, other.closingConsonants, otherInfluence *
                        Math.max(0.0, Math.min(1.0, 1.0 - other.vowelEndFrequency + vowelEndFrequency))),
                cs = merge1000(rng, closingSyllables, other.closingSyllables, otherInfluence *
                        Math.max(0.0, Math.min(1.0, other.syllableEndFrequency - syllableEndFrequency))),
                splitters = merge1000(rng, vowelSplitters, other.vowelSplitters, otherInfluence);

        IntDoubleOrderedMap freqs = new IntDoubleOrderedMap(syllableFrequencies);
        for (IntDoubleOrderedMap.MapEntry kv : other.syllableFrequencies.mapEntrySet()) {
            if (freqs.containsKey(kv.getIntKey()))
                freqs.put(kv.getIntKey(), kv.getDoubleValue() + freqs.get(kv.getIntKey()));
            else
                freqs.put(kv.getIntKey(), kv.getDoubleValue());
        }
        List<Modifier> mods = new ArrayList<>(modifiers.size() + other.modifiers.size());
        mods.addAll(modifiers);
        mods.addAll(other.modifiers);
        return new FakeLanguageGen(ov, mv, oc, mc, cc, cs, splitters, freqs,
                vowelStartFrequency * myInfluence + other.vowelStartFrequency * otherInfluence,
                vowelEndFrequency * myInfluence + other.vowelEndFrequency * otherInfluence,
                vowelSplitFrequency * myInfluence + other.vowelSplitFrequency * otherInfluence,
                syllableEndFrequency * myInfluence + other.syllableEndFrequency * otherInfluence,
                (sanityChecks == null) ? other.sanityChecks : sanityChecks, true, mods);
    }

    private static double readDouble(Object o) {
        if (o instanceof Double) return (Double) o;
        else if (o instanceof Float) return (Float) o;
        else if (o instanceof Long) return ((Long) o).doubleValue();
        else if (o instanceof Integer) return (Integer) o;
        else if (o instanceof Short) return (Short) o;
        else if (o instanceof Byte) return (Byte) o;
        else if (o instanceof Character) return (Character) o;
        return 0.0;
    }

    /**
     * Produces a FakeLanguageGen by mixing this FakeLanguageGen with one or more other FakeLanguageGen objects. Takes
     * a weight for this, another FakeLanguageGen, a weight for that FakeLanguageGen, then a possibly-empty group of
     * FakeLanguageGen parameters and the weights for those parameters. If other1 is null or if pairs has been given a
     * value of null instead of the normal (possibly empty) array of Objects, then this simply returns a copy of this
     * FakeLanguageGen. Otherwise, it will at least mix this language with other1 using the given weights for each.
     * If pairs is not empty, it has special requirements for what types it allows and in what order, but does no type
     * checking. Specifically, pairs requires the first Object to be a FakeLanguageGen, the next to be a number of some
     * kind that will be the weight for the previous FakeLanguageGen(this method can handle non-Double weights, and
     * converts them to Double if needed), and every two parameters after that to follow the same order and pattern
     * (FakeLanguageGen, then number, then FakeLanguageGen, then number...). Weights are absolute, and don't depend on
     * earlier weights, which is the case when chaining the {@link #mix(FakeLanguageGen, double)} method. This makes
     * reasoning about the ideal weights for multiple mixed languages easier; to mix 3 languages equally you can use
     * 3 equal weights with this, whereas with mix chaining you would need to mix the first two with 0.5 and the third
     * with 0.33 .
     * <br>
     * Unlike the static method {@link #mixAll(Object...)}, this _is_ intended for external use, in part because the
     * technique for mixing languages by weight is so much more intuitive, but also because this assigns valid data for
     * serializing and deserializing this FakeLanguageGen that allows it to use significantly less space (less than 1/72
     * the bytes used in one not-quite-simple test).
     *
     * @param myWeight the weight to assign this FakeLanguageGen in the mix
     * @param other1   another FakeLanguageGen to mix in; if null, this method will abort and return {@link #copy()}
     * @param weight1  the weight to assign other1 in the mix
     * @param pairs    may be empty, not null; otherwise must alternate between FakeLanguageGen and number (weight) elements
     * @return a FakeLanguageGen produced by mixing this with any FakeLanguageGen arguments by the given weights
     */
    public FakeLanguageGen mix(double myWeight, FakeLanguageGen other1, double weight1, Object... pairs) {
        if (other1 == null || pairs == null)
            return copy();
        OrderedSet<Modifier> mods = new OrderedSet<>(modifiers);
        FakeLanguageGen mixer = removeModifiers();
        FakeLanguageGen[] languages = new FakeLanguageGen[2 + (pairs.length >>> 1)];
        double[] weights = new double[languages.length];
        String[] summaries = new String[languages.length];
        boolean summarize = true;
        double total = 0.0, current, weight;
        languages[0] = mixer;
        total += weights[0] = myWeight;
        if ((summaries[0] = mixer.summary) == null) summarize = false;
        mods.addAll(other1.modifiers);
        languages[1] = other1.removeModifiers();
        total += weights[1] = weight1;
        if (summarize && (summaries[1] = languages[1].summary) == null) summarize = false;
        for (int i = 1, p = 2; i < pairs.length; i += 2, p++) {
            if (pairs[i] == null || pairs[i - 1] == null)
                continue;
            languages[p] = ((FakeLanguageGen) pairs[i - 1]).removeModifiers();
            total += weights[p] = readDouble(pairs[i]);
            if (summarize && (summaries[p] = languages[p].summary) == null) summarize = false;
        }
        if (total == 0)
            return copy();
        current = myWeight / total;
        for (int i = 1; i < languages.length; i++) {
            if ((weight = weights[i]) > 0)
                mixer = mixer.mix(languages[i], weight / total / (current += weight / total));
        }
        if (summarize) {
            StringBuilder brief = new StringBuilder(64);
            String c;
            int idx;
            for (int i = 0; i < summaries.length; i++) {
                c = summaries[i];
                idx = c.indexOf('@');
                if (idx >= 0) {
                    brief.append(c.substring(0, idx + 1)).append(weights[i]);
                    if (i < summaries.length - 1)
                        brief.append('~');
                }
            }
            for (int i = 0; i < mods.size(); i++) {
                brief.append('℗').append(mods.getAt(i).serializeToString());
            }
            return mixer.addModifiers(mods).summarize(brief.toString());
        } else
            return mixer.addModifiers(mods);
    }

    /**
     * Produces a FakeLanguageGen from a group of FakeLanguageGen parameters and the weights for those parameters.
     * Requires the first Object in pairs to be a FakeLanguageGen, the next to be a number of some kind that will be the
     * weight for the previous FakeLanguageGen(this method can handle non-Double weights, and converts them to Double
     * if needed), and every two parameters after that to follow the same order and pattern (FakeLanguageGen, then
     * number, then FakeLanguageGen, then number...). There should be at least 4 elements in pairs, half of them
     * languages and half of them weights, for this to do any mixing, but it can produce a result with as little as one
     * FakeLanguageGen (returning a copy of the first FakeLanguageGen). Weights are absolute, and don't depend on
     * earlier weights, which is the case when chaining the {@link #mix(FakeLanguageGen, double)} method. This makes
     * reasoning about the ideal weights for multiple mixed languages easier; to mix 3 languages equally you can use
     * 3 equal weights with this, whereas with mix chaining you would need to mix the first two with 0.5 and the third
     * with 0.33 .
     * <br>
     * Not intended for external use, but it could be useful. Used internally in the deserialization code.
     *
     * @param pairs should have at least one item, and must alternate between FakeLanguageGen and number (weight) elements
     * @return a FakeLanguageGen produced by mixing any FakeLanguageGen arguments by the given weights
     */
    public static FakeLanguageGen mixAll(Object... pairs) {
        int len;
        if (pairs == null || (len = pairs.length) <= 0)
            return ENGLISH.copy();
        if (len < 4)
            return ((FakeLanguageGen) pairs[0]).copy();
        Object[] pairs2 = new Object[len - 4];
        if (len > 4)
            System.arraycopy(pairs, 4, pairs2, 0, len - 4);
        return ((FakeLanguageGen) pairs[0]).mix(readDouble(pairs[1]), (FakeLanguageGen) pairs[2], readDouble(pairs[3]), pairs2);
    }

    public FakeLanguageGen addAccents(double vowelInfluence, double consonantInfluence) {
        vowelInfluence = Math.max(0.0, Math.min(vowelInfluence, 1.0));
        consonantInfluence = Math.max(0.0, Math.min(consonantInfluence, 1.0));

        RNG rng = new RNG((hash64() & 0xffffffffL) ^
                ((NumberTools.doubleToLongBits(vowelInfluence) & 0xffffffffL) | (NumberTools.doubleToLongBits(consonantInfluence) << 32)));
        String[] ov = accentVowels(rng, openingVowels, vowelInfluence),
                mv = accentVowels(rng, midVowels, vowelInfluence),
                oc = accentConsonants(rng, openingConsonants, consonantInfluence),
                mc = accentConsonants(rng, midConsonants, consonantInfluence),
                cc = accentConsonants(rng, closingConsonants, consonantInfluence),
                cs = accentBoth(rng, closingSyllables, vowelInfluence, consonantInfluence);


        return new FakeLanguageGen(ov, mv, oc, mc, cc, cs, vowelSplitters, syllableFrequencies,
                vowelStartFrequency,
                vowelEndFrequency,
                vowelSplitFrequency,
                syllableEndFrequency, sanityChecks, clean, modifiers);
    }

    private static String[] copyStrings(String[] start) {
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
        result = CrossHash.hash(openingVowels);
        result = 31 * result + CrossHash.hash(midVowels);
        result = 31 * result + CrossHash.hash(openingConsonants);
        result = 31 * result + CrossHash.hash(midConsonants);
        result = 31 * result + CrossHash.hash(closingConsonants);
        result = 31 * result + CrossHash.hash(vowelSplitters);
        result = 31 * result + CrossHash.hash(closingSyllables);
        result = 31 * result + (clean ? 1 : 0);
        result = 31 * result + syllableFrequencies.hashCode();
        result = 31 * result + NumberTools.doubleToMixedIntBits(totalSyllableFrequency);
        result = 31 * result + NumberTools.doubleToMixedIntBits(vowelStartFrequency);
        result = 31 * result + NumberTools.doubleToMixedIntBits(vowelEndFrequency);
        result = 31 * result + NumberTools.doubleToMixedIntBits(vowelSplitFrequency);
        result = 31 * result + NumberTools.doubleToMixedIntBits(syllableEndFrequency);
        result = 31 * result + (sanityChecks != null ? sanityChecks.length + 1 : 0);
        result *= 31;
        if(modifiers != null) {
            for (int i = 0; i < modifiers.size(); i++) {
                result += 7 * (i + 1) * modifiers.get(i).hashCode();
            }
        }
        return result;
    }
    public long hash64() {
        long result           = CrossHash.hash64(openingVowels);
        result = 31L * result + CrossHash.hash64(midVowels);
        result = 31L * result + CrossHash.hash64(openingConsonants);
        result = 31L * result + CrossHash.hash64(midConsonants);
        result = 31L * result + CrossHash.hash64(closingConsonants);
        result = 31L * result + CrossHash.hash64(vowelSplitters);
        result = 31L * result + CrossHash.hash64(closingSyllables);
        result = 31L * result + (clean ? 1L : 0L);
        result = 31L * result + syllableFrequencies.hash64();
        result = 31L * result + NumberTools.doubleToLongBits(totalSyllableFrequency);
        result = 31L * result + NumberTools.doubleToLongBits(vowelStartFrequency);
        result = 31L * result + NumberTools.doubleToLongBits(vowelEndFrequency);
        result = 31L * result + NumberTools.doubleToLongBits(vowelSplitFrequency);
        result = 31L * result + NumberTools.doubleToLongBits(syllableEndFrequency);
        result = 31L * result + (sanityChecks != null ? sanityChecks.length + 1L : 0L);
        result *= 31L;
        if(modifiers != null) {
            for (int i = 0; i < modifiers.size(); i++) {
                result += 7L * (i + 1L) * CrossHash.hash64(modifiers.get(i).alterations);
            }
        }
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
                vowelEndFrequency, vowelSplitFrequency, syllableEndFrequency, sanityChecks, clean, modifiers)
                .summarize(summary);
    }


    public String serializeToString() {
        return (summary == null) ? "" : summary;
    }

    public static FakeLanguageGen deserializeFromString(String data) {
        if (data == null || data.equals(""))
            return ENGLISH.copy();
        int poundIndex = data.indexOf('#'), snailIndex = data.indexOf('@'), tempBreak = data.indexOf('℗'),
                breakIndex = (tempBreak < 0) ? data.length() : tempBreak,
                tildeIndex = Math.min(data.indexOf('~'), breakIndex), prevTildeIndex = -1;
        if (tildeIndex < 0)
            tildeIndex = data.length();

        if (snailIndex < 0)
            return ENGLISH.copy();
        ArrayList<Object> pairs = new ArrayList<>(4);
        while (snailIndex >= 0) {
            if (poundIndex >= 0 && poundIndex < snailIndex) // random case
            {
                pairs.add(randomLanguage(Long.parseLong(data.substring(poundIndex + 1, snailIndex))));
                pairs.add(Double.valueOf(data.substring(snailIndex + 1, tildeIndex)));
                poundIndex = -1;
            } else {
                pairs.add(registry.getAt(Integer.parseInt(data.substring(prevTildeIndex + 1, snailIndex))));
                pairs.add(Double.valueOf(data.substring(snailIndex + 1, tildeIndex)));
            }
            snailIndex = data.indexOf('@', snailIndex + 1);
            if (snailIndex > breakIndex)
                break;
            prevTildeIndex = tildeIndex;
            tildeIndex = Math.min(data.indexOf('~', tildeIndex + 1), breakIndex);
            if (tildeIndex < 0)
                tildeIndex = data.length();
        }
        ArrayList<Modifier> mods = new ArrayList<>(8);
        if (breakIndex == tempBreak) {
            tildeIndex = breakIndex - 1;
            while ((prevTildeIndex = data.indexOf('℗', tildeIndex + 1)) >= 0) {
                tildeIndex = data.indexOf('℗', prevTildeIndex + 1);
                if (tildeIndex < 0) tildeIndex = data.length();
                mods.add(Modifier.deserializeFromString(data.substring(prevTildeIndex, tildeIndex)));
            }
        }
        FakeLanguageGen flg = mixAll(pairs.toArray());
        flg.modifiers.addAll(mods);
        return flg;
    }

    public static class Modifier implements Serializable {
        private static final long serialVersionUID = 1734863678490422371L;
        public final Alteration[] alterations;

        public Modifier() {
            alterations = new Alteration[0];
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
            StringBuilder tmp;
            boolean found;
            Alteration alt;
            for (int a = 0; a < alterations.length; a++) {
                alt = alterations[a];
                tmp = working.sb;
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
            return working.sb;
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
         * Removes accented letters and the two non-English consonants from text generated with {@link #NORSE}.
         * Replaces á, é, í, ý, ó, æ, ú, and ö with a, e, i, y, o, ae, and ou. In some instances, replaces j
         * with y. Replaces ð and þ with th and th, except for when preceded by s (then it replaces sð or sþ
         * with st or st) or when the start of a word is fð or fþ, where it replaces with fr or fr.
         */
        public static final Modifier SIMPLIFY_NORSE = replacementTable(
                "á", "a",
                "é", "e",
                "í", "i",
                "ý", "y",
                "ó", "o",
                "ú", "u",
                "æ", "ae",
                "ö", "ou",
                "([^aeiou])jy", "$1yai",
                "([^aeiou])j(?:[aeiouy]+)", "$1yo",
                "s([ðþ])", "st",
                "\\bf[ðþ]", "fr",
                "[ðþ]", "th");

        /**
         * Simple changes to merge "ae" into "æ", "oe" into "œ", and any of "aé", "áe", or "áé" into "ǽ".
         */
        public static final Modifier LIGATURES = replacementTable("ae", "æ", "oe", "œ", "[aá][eé]", "ǽ");
        /**
         * Some changes that can be applied when sanity checks (which force re-generating a new word) aren't appropriate
         * for fixing a word that isn't pronounceable.
         */
        public static final Modifier GENERAL_CLEANUP = replacementTable(
                "[æǽœìíîïĩīĭįıiùúûüũūŭůűųuýÿŷỳy]([æǽœýÿŷỳy])", "$1",
                "q([ùúûüũūŭůűųu])$", "q$1e",
                "([ìíîïĩīĭįıi])[ìíîïĩīĭįıi]", "$1",
                "([æǽœìíîïĩīĭįıiùúûüũūŭůűųuýÿŷỳy])[wŵẁẃẅ]$", "$1",
                "([ùúûüũūŭůűųu])([òóôõöøōŏőǿo])", "$2$1",
                "[àáâãäåāăąǻaèéêëēĕėęěeìíîïĩīĭįıiòóôõöøōŏőǿoùúûüũūŭůűųuýÿŷỳy]([æǽœ])", "$1",
                "([æǽœ])[àáâãäåāăąǻaèéêëēĕėęěeìíîïĩīĭįıiòóôõöøōŏőǿoùúûüũūŭůűųuýÿŷỳy]", "$1",
                "([wŵẁẃẅ])[wŵẁẃẅ]", "$1",
                "qq", "q");

        //àáâãäåāăąǻæǽaèéêëēĕėęěeìíîïĩīĭįıiòóôõöøōŏőœǿoùúûüũūŭůűųuýÿŷỳy
        //bcçćĉċčdþðďđḍfgĝğġģhĥħḥjĵȷkķlĺļľŀłḷḹmṃnñńņňŋṅṇpqrŕŗřṛṝsśŝşšșṣtţťțṭvwŵẁẃẅxyýÿŷỳzźżž

        /**
         * Creates a Modifier that will replace the nth char in initial with the nth char in change. Expects initial and
         * change to be the same length, but will use the lesser length if they are not equal-length. Because of the
         * state of the text at the time modifiers are run, only lower-case letters need to be searched for.
         *
         * @param initial a String containing lower-case letters or other symbols to be swapped out of a text
         * @param change  a String containing characters that will replace occurrences of characters in initial
         * @return a Modifier that can be added to a FakeLanguageGen with its addModifiers() method
         */
        public static Modifier charReplacementTable(String initial, String change) {
            Alteration[] alts = new Alteration[Math.min(initial.length(), change.length())];
            for (int i = 0; i < alts.length; i++) {
                //literal string syntax; avoids sensitive escaping issues and also doesn't need a character class,
                // which is slightly slower and has some odd escaping cases.
                alts[i] = new Alteration("\\Q" + initial.charAt(i), change.substring(i, i + 1));
            }
            return new Modifier(alts);
        }

        /**
         * Creates a Modifier that will replace the nth String key in map with the nth value. Because of the
         * state of the text at the time modifiers are run, only lower-case letters need to be searched for.
         * This overload of replacementTable allows full regex pattern strings as keys and replacement syntax,
         * such as searching for "([aeiou])\\1+" to find repeated occurrences of the same vowel, and "$1" in
         * this example to replace the repeated section with only the first vowel.
         * The ordering of map matters if a later key contains an earlier key (the earlier one will be replaced
         * first, possibly making the later key not match), or if an earlier replacement causes a later one to
         * become valid.
         *
         * @param map containing String keys to replace and String values to use instead; replacements happen in order
         * @return a Modifier that can be added to a FakeLanguageGen with its addModifiers() method
         */
        public static Modifier replacementTable(OrderedMap<String, String> map) {
            if (map == null)
                return new Modifier();
            Alteration[] alts = new Alteration[map.size()];
            for (int i = 0; i < alts.length; i++) {
                alts[i] = new Alteration(map.keyAt(i), map.getAt(i));
            }
            return new Modifier(alts);
        }

        /**
         * Creates a Modifier that will replace the (n*2)th String in pairs with the (n*2+1)th value in pairs. Because
         * of the state of the text at the time modifiers are run, only lower-case letters need to be searched for.
         * This overload of replacementTable allows full regex syntax for search and replacement Strings,
         * such as searching for "([aeiou])\\1+" to find repeated occurrences of the same vowel, and "$1" in
         * this example to replace the repeated section with only the first vowel.
         * The ordering of pairs matters if a later search contains an earlier search (the earlier one will be replaced
         * first, possibly making the later search not match), or if an earlier replacement causes a later one to
         * become valid.
         *
         * @param pairs array or vararg of alternating Strings to search for and Strings to replace with; replacements happen in order
         * @return a Modifier that can be added to a FakeLanguageGen with its addModifiers() method
         */
        public static Modifier replacementTable(String... pairs) {
            int len;
            if (pairs == null || (len = pairs.length) <= 1)
                return new Modifier();
            Alteration[] alts = new Alteration[len >> 1];
            for (int i = 0; i < alts.length; i++) {
                alts[i] = new Alteration(pairs[i<< 1], pairs[i<<1|1]);
            }
            return new Modifier(alts);
        }

        /**
         * Adds the potential for the String {@code insertion} to be used as a vowel in addition to the vowels that the
         * language already uses; insertion will replace an existing vowel (at any point in a word that had a vowel
         * generated) with a probability of {@code chance}, so chance should be low (0.1 at most) unless you want the
         * newly-inserted vowel to be likely to be present in every word of some sentences.
         * @param insertion the String to use as an additional vowel
         * @param chance the chance for a vowel cluster to be replaced with insertion; normally 0.1 or less
         * @return a Modifier that can be added to a FakeLanguageGen with its addModifiers() method
         */
        public static Modifier insertVowel(String insertion, double chance)
        {
            return new Modifier(anyVowelCluster, insertion, chance);
        }

        /**
         * Adds the potential for the String {@code insertion} to be used as a consonant in addition to the consonants
         * that the language already uses; insertion will replace an existing consonant (at any point in a word that had
         * a consonant generated) with a probability of {@code chance}, so chance should be low (0.1 at most) unless you
         * want the newly-inserted consonant to be likely to be present in every word of some sentences.
         * @param insertion the String to use as an additional consonant
         * @param chance the chance for a consonant cluster to be replaced with insertion; normally 0.1 or less
         * @return a Modifier that can be added to a FakeLanguageGen with its addModifiers() method
         */
        public static Modifier insertConsonant(String insertion, double chance)
        {
            return new Modifier(anyConsonantCluster, insertion, chance);
        }

        /**
         * Adds the potential for the String {@code insertion} to be used as a vowel in addition to the vowels that the
         * language already uses; insertion will replace an existing vowel at the start of a word with a probability of
         * {@code chance}, so chance should be low (0.2 at most) unless you want the newly-inserted vowel to be likely
         * to start every word of some sentences. Not all languages can start words with vowels, or do that very rarely,
         * so this might not do anything.
         * @param insertion the String to use as an additional opening vowel
         * @param chance the chance for a vowel cluster at the start of a word to be replaced with insertion; normally 0.2 or less
         * @return a Modifier that can be added to a FakeLanguageGen with its addModifiers() method
         */
        public static Modifier insertOpeningVowel(String insertion, double chance)
        {
            return new Modifier("\\b[àáâãäåæāăąǻǽaèéêëēĕėęěeìíîïĩīĭįıiòóôõöøōŏőœǿoùúûüũūŭůűųuýÿŷỳyαοειυаеёийоуъыэюя]+", insertion, chance);
        }

        /**
         * Adds the potential for the String {@code insertion} to be used as a consonant in addition to the consonants
         * that the language already uses; insertion will replace an existing consonant at the start of a word with a
         * probability of {@code chance}, so chance should be low (0.2 at most) unless you want the newly-inserted
         * consonant to be likely to start every word of some sentences. Not all languages can start words with
         * consonants, or do that very rarely, so this might not do anything.
         * @param insertion the String to use as an additional opening consonant
         * @param chance the chance for a consonant cluster at the start of a word to be replaced with insertion; normally 0.2 or less
         * @return a Modifier that can be added to a FakeLanguageGen with its addModifiers() method
         */
        public static Modifier insertOpeningConsonant(String insertion, double chance)
        {
            return new Modifier("\\b[bcçćĉċčdþðďđfgĝğġģhĥħjĵȷkķlĺļľŀłmnñńņňŋpqrŕŗřsśŝşšștţťțvwŵẁẃẅxyýÿŷỳzźżžṛṝḷḹḍṭṅṇṣṃḥρσζτκχνθμπψβλγφξςбвгдклпрстфхцжмнзчшщ]+", insertion, chance);
        }

        /**
         * Adds the potential for the String {@code insertion} to be used as a vowel in addition to the vowels that the
         * language already uses; insertion will replace an existing vowel at the end of a word with a probability of
         * {@code chance}, so chance should be low (0.2 at most) unless you want the newly-inserted vowel to be likely
         * to end every word of some sentences. Not all languages can end words with vowels, or do that very
         * rarely, so this might not do anything.
         * @param insertion the String to use as an additional closing vowel
         * @param chance the chance for a vowel cluster at the end of a word to be replaced with insertion; normally 0.2 or less
         * @return a Modifier that can be added to a FakeLanguageGen with its addModifiers() method
         */
        public static Modifier insertClosingVowel(String insertion, double chance)
        {
            return new Modifier("[àáâãäåæāăąǻǽaèéêëēĕėęěeìíîïĩīĭįıiòóôõöøōŏőœǿoùúûüũūŭůűųuýÿŷỳyαοειυаеёийоуъыэюя]+\\b", insertion, chance);
        }

        /**
         * Adds the potential for the String {@code insertion} to be used as a consonant in addition to the consonants
         * that the language already uses; insertion will replace an existing consonant at the end of a word with a
         * probability of {@code chance}, so chance should be low (0.2 at most) unless you want the newly-inserted
         * consonant to be likely to end every word of some sentences. Not all languages can end words with consonants,
         * or do that very rarely, so this might not do anything.
         * @param insertion the String to use as an additional closing consonant
         * @param chance the chance for a consonant cluster at the end of a word to be replaced with insertion; normally 0.2 or less
         * @return a Modifier that can be added to a FakeLanguageGen with its addModifiers() method
         */
        public static Modifier insertClosingConsonant(String insertion, double chance)
        {
            return new Modifier("[bcçćĉċčdþðďđfgĝğġģhĥħjĵȷkķlĺļľŀłmnñńņňŋpqrŕŗřsśŝşšștţťțvwŵẁẃẅxyýÿŷỳzźżžṛṝḷḹḍṭṅṇṣṃḥρσζτκχνθμπψβλγφξςбвгдклпрстфхцжмнзчшщ]+\\b", insertion, chance);
        }

        /**
         * This was used in an earlier version, which attempted to use a Romanization technique that would use Greek
         * letters and several accented Latin letters to try to accurately represent some of the complex orthography of
         * Arabic, but because FakeLanguageGen doesn't use that technique any more (it was effectively unreadable), this
         * Modifier is a no-op. Its use is never needed any more.
         * @deprecated This Modifier doesn't do anything, and isn't needed.
         */
        @Deprecated
        public static final Modifier SIMPLIFY_ARABIC = new Modifier(
                /*
                new Alteration("ţ", "th"),
                new Alteration("ĥ", "kh"),
                new Alteration("ħ", "khr"),
                new Alteration("đ", "dh"),
                new Alteration("ď", "dt"),
                new Alteration("š", "sh"),
                new Alteration("ş", "shw"),
                new Alteration("ť", "ch"),
                new Alteration("ż", "zh"),
                new Alteration("ξ", "khm"),
                new Alteration("δ", "gh"),
                new Alteration("ā", "aa"),
                new Alteration("ū", "uu"),
                new Alteration("ī", "ii")*/);

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
            return CrossHash.hash(alterations);
        }

        @Override
        public String toString() {
            return "Modifier{" +
                    "alterations=" + Arrays.toString(alterations) +
                    '}';
        }

        public String serializeToString() {
            if (alterations == null || alterations.length == 0) return "\6";
            StringBuilder sb = new StringBuilder(32).append('\6');
            for (int i = 0; i < alterations.length; i++)
                sb.append(alterations[i].serializeToString()).append('\6');
            return sb.toString();
        }

        public static Modifier deserializeFromString(String data) {
            int currIdx = data.indexOf(6), altIdx = currIdx, matches = 0;
            while (currIdx >= 0) {
                if ((currIdx = data.indexOf(6, currIdx + 1)) < 0)
                    break;
                matches++;
            }
            Alteration[] alts = new Alteration[matches];
            for (int i = 0; i < matches; i++) {
                alts[i] = Alteration.deserializeFromString(data.substring(altIdx + 1, altIdx = data.indexOf(6, altIdx + 1)));
            }
            return new Modifier(alts);
        }
    }

    public static class Alteration implements Serializable {
        private static final long serialVersionUID = -2138854697837563188L;
        public Replacer replacer;
        public String replacement;
        public double chance;

        public Alteration() {
            this("[tţťțṭ]?[sśŝşšș]+h?", "th");
        }

        public Alteration(String pattern, String replacement) {
            this.replacement = replacement;
            replacer = Pattern.compile(pattern).replacer(replacement);
            chance = 1.0;
        }

        public Alteration(String pattern, String replacement, double chance) {
            this.replacement = replacement;
            replacer = Pattern.compile(pattern).replacer(replacement);
            this.chance = chance;
        }

        public Alteration(Pattern pattern, String replacement, double chance) {
            this.replacement = replacement;
            replacer = pattern.replacer(replacement);
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
            long result;
            result = CrossHash.hash64(replacer.getPattern().serializeToString());
            result = 31L * result + NumberTools.doubleToLongBits(chance);
            result ^= result >>> 32;
            return (int) (0xFFFFFFFFL & result);
        }

        @Override
        public String toString() {
            return "Alteration{" +
                    "replacer=" + replacer +
                    ", chance=" + chance +
                    '}';
        }

        public String serializeToString() {
            return replacer.getPattern().serializeToString() + '\2' + replacement + '\4' + chance;
        }

        public static Alteration deserializeFromString(String data) {
            int split2 = data.indexOf('\2'), split4 = data.indexOf('\4');
            return new Alteration(Pattern.deserializeFromString(data.substring(0, split2)),
                    data.substring(split2 + 1, split4),
                    Double.parseDouble(data.substring(split4 + 1)));
        }
    }

    /**
     * A simple way to bundle a FakeLanguageGen with the arguments that would be passed to it when calling
     * {@link FakeLanguageGen#sentence(RNG, int, int, String[], String[], double, int)} or one of its overloads.
     * You can call {@link #sentence()} on this to produce another String sentence with the parameters it was given
     * at construction. The parameters to
     * {@link #SentenceForm(FakeLanguageGen, StatefulRNG, int, int, String[], String[], double, int)} are stored in fields of
     * the same name, and all fields in this class are public and modifiable.
     */
    public static class SentenceForm implements Serializable
    {
        private static final long serialVersionUID = 1246527948419533147L;
        public StatefulRNG rng;
        public int minWords, maxWords, maxChars;
        public String[] midPunctuation, endPunctuation;
        public double midPunctuationFrequency;
        public FakeLanguageGen language;

        /**
         * Builds a SentenceForm with all default fields, using {@link FakeLanguageGen#FANTASY_NAME} for a language,
         * using between 1 and 9 words in a sentence, and otherwise defaulting to how
         * {@link #SentenceForm(FakeLanguageGen, int, int)} behaves.
         */
        public SentenceForm()
        {
            this(FakeLanguageGen.FANTASY_NAME, FakeLanguageGen.srng, 1, 9,
                    new String[]{",", ",", ",", ";", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.18, -1);
        }
        /**
         * Builds a SentenceForm with only a few fields specified. The {@link #rng} will be made based on
         * FakeLanguageGen's static {@link FakeLanguageGen#srng} field, maxChars will be -1 so the sentence length
         * will be limited only by maxWords and the length of words produced, and the between-word and end-of-sentence
         * punctuation will be set to reasonable defaults. This places either a comma or a semicolon after a word in the
         * middle of a sentence about 18% of the time ({@code midPunctuationFrequency} is 0.18), and can end a sentence
         * in a period, exclamation mark, question mark, or ellipsis (the "..." punctuation).
         * @param language A FakeLanguageGen to use to generate words
         * @param minWords minimum words per sentence
         * @param maxWords maximum words per sentence
         */
        public SentenceForm(FakeLanguageGen language, int minWords, int maxWords)
        {
            this(language, FakeLanguageGen.srng, minWords, maxWords, new String[]{",", ",", ",", ";", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.18, -1);
        }
        /**
         * Builds a SentenceForm with all fields specified except for {@link #rng}, which will be made based on
         * FakeLanguageGen's static {@link FakeLanguageGen#srng} field, and maxChars, which means the sentence length
         * will be limited only by maxWords and the length of words produced.
         * @param language A FakeLanguageGen to use to generate words
         * @param minWords minimum words per sentence
         * @param maxWords maximum words per sentence
         * @param midPunctuation an array of Strings that can be used immediately after words in the middle of sentences, like "," or ";"
         * @param endPunctuation an array of Strings that can end a sentence, like ".", "?", or "..."
         * @param midPunctuationFrequency the probability that two words will be separated by a String from midPunctuation, between 0.0 and 1.0
         */
        public SentenceForm(FakeLanguageGen language, int minWords, int maxWords, String[] midPunctuation,
                            String[] endPunctuation, double midPunctuationFrequency)
        {
            this(language, FakeLanguageGen.srng, minWords, maxWords, midPunctuation, endPunctuation,
                    midPunctuationFrequency, -1);
        }
        /**
         * Builds a SentenceForm with all fields specified except for {@link #rng}, which will be made based on
         * FakeLanguageGen's static {@link FakeLanguageGen#srng} field.
         * @param language A FakeLanguageGen to use to generate words
         * @param minWords minimum words per sentence
         * @param maxWords maximum words per sentence
         * @param midPunctuation an array of Strings that can be used immediately after words in the middle of sentences, like "," or ";"
         * @param endPunctuation an array of Strings that can end a sentence, like ".", "?", or "..."
         * @param midPunctuationFrequency the probability that two words will be separated by a String from midPunctuation, between 0.0 and 1.0
         * @param maxChars the maximum number of chars to use in a sentence, or -1 for no hard limit
         */
        public SentenceForm(FakeLanguageGen language, int minWords, int maxWords, String[] midPunctuation,
                            String[] endPunctuation, double midPunctuationFrequency, int maxChars)
        {
            this(language, FakeLanguageGen.srng, minWords, maxWords, midPunctuation, endPunctuation,
                    midPunctuationFrequency, maxChars);
        }

        /**
         * Builds a SentenceForm with all fields specified; each value is referenced directly except for {@code rng},
         * which will not change or be directly referenced (a new StatefulRNG will be used with the same state value).
         * Note that the StatefulRandomness used by this class' rng field will always be ThrustAltRNG, even if the given
         * rng parameter used a different StatefulRandomness implementation.
         * @param language A FakeLanguageGen to use to generate words
         * @param rng a StatefulRNG that will not be directly referenced; the state will be copied into a new StatefulRNG
         * @param minWords minimum words per sentence
         * @param maxWords maximum words per sentence
         * @param midPunctuation an array of Strings that can be used immediately after words in the middle of sentences, like "," or ";"
         * @param endPunctuation an array of Strings that can end a sentence, like ".", "?", or "..."
         * @param midPunctuationFrequency the probability that two words will be separated by a String from midPunctuation, between 0.0 and 1.0
         * @param maxChars the maximum number of chars to use in a sentence, or -1 for no hard limit
         */
        public SentenceForm(FakeLanguageGen language, StatefulRNG rng, int minWords, int maxWords,
                            String[] midPunctuation, String[] endPunctuation,
                            double midPunctuationFrequency, int maxChars)
        {
            this.language = language;
            this.rng = new StatefulRNG(rng.getState());
            this.minWords = minWords;
            this.maxWords = maxWords;
            this.midPunctuation = midPunctuation;
            this.endPunctuation = endPunctuation;
            this.midPunctuationFrequency = midPunctuationFrequency;
            this.maxChars = maxChars;
        }
        public String sentence()
        {
            return language.sentence(rng, minWords, maxWords, midPunctuation, endPunctuation,
                    midPunctuationFrequency, maxChars);
        }

        public String serializeToString() {
            return language.serializeToString() + '℘' +
                    rng.getState() + '℘' +
                    minWords + '℘' +
                    maxWords + '℘' +
                    StringKit.join("ℙ", midPunctuation) + '℘' +
                    StringKit.join("ℙ", endPunctuation) + '℘' +
                    NumberTools.doubleToLongBits(midPunctuationFrequency) + '℘' +
                    maxChars;
        }
        public static SentenceForm deserializeFromString(String ser)
        {
            int gap = ser.indexOf('℘');
            FakeLanguageGen lang = FakeLanguageGen.deserializeFromString(ser.substring(0, gap));
            StatefulRNG rng = new StatefulRNG(
                    StringKit.longFromDec(ser,gap + 1, gap = ser.indexOf('℘', gap + 1)));
            int minWords = StringKit.intFromDec(ser,gap + 1, gap = ser.indexOf('℘', gap + 1));
            int maxWords = StringKit.intFromDec(ser,gap + 1, gap = ser.indexOf('℘', gap + 1));
            String[] midPunctuation =
                    StringKit.split(ser.substring(gap + 1, gap = ser.indexOf('℘', gap + 1)), "ℙ");
            String[] endPunctuation =
                    StringKit.split(ser.substring(gap + 1, gap = ser.indexOf('℘', gap + 1)), "ℙ");
            double midFreq = NumberTools.longBitsToDouble(StringKit.longFromDec(ser,gap + 1, gap = ser.indexOf('℘', gap + 1)));
            int maxChars = StringKit.intFromDec(ser,gap + 1, ser.length());
            return new SentenceForm(lang, rng, minWords, maxWords, midPunctuation, endPunctuation, midFreq, maxChars);
        }
    }
}
