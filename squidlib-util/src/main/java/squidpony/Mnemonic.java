package squidpony;

import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A utility class to print (typically very large) numbers in a way that players can more-meaningfully tell them apart.
 * It isn't that great for this task currently, but it can bi-directionally turn {@code long} values like
 * -8798641734435409502 into {@code String}s like nwihyayeetyoruehyazuetro. The advantage here is that
 * nwihyayeetyoruehyazuetro is very different from protoezlebauyauzlutoatra, even though the numbers they are made from
 * are harder to distinguish (-8798641734435409502 vs. -8032477240987739423, when using the default seed).
 * <br>
 * The constructor optionally takes a seed that can greatly change the generated mnemonics, which may be useful if
 * mnemonic strings produced for some purpose should only be decipherable by that program or that play of the game. If
 * no seed is given, this acts as if the seed is 1. Only 256 possible 3-letter sections are used with any given seed,
 * but 431 sections are possible (hand-selected to avoid the likelihood of producing possibly-vulgar words). Two
 * different seeds may use mostly-different selections of "syllable" sections, though a not-very-small amount of overlap
 * in potential generated mnemonic strings must occur between any two seeds.
 * <br>
 * Created by Tommy Ettinger on 1/24/2018.
 */
public class Mnemonic {
    private static final String baseTriplets =
            "baibaublabyabeabeebeibeoblebrebwebyebiabiebioblibribwibyiboaboeboiboubrobuobyobuabuebuibrubwubyu" +
            "daudradyadeadeedeodredwediodridwidyidoadoedoidoudroduodyoduadueduidrudwudyu" +
            "haihauhmahrahyahwaheaheeheiheohmehrehwehyehiahiehiohmihrihwihyihmohrohuohyohuahuehuihmuhruhwuhyu" +
            "jaijaujyajwajeajeejeijeojwejyejiajiejiojwijyijoajoejoijoujyo" +
            "kaikaukrakyakeakeekeoklekrekyekiakiokrikwikyikoakoekoikouklokrokyokuokuakuekuikrukyu" +
            "lailaulyalwalealeeleileolwelyelialieliolwilyiloaloeloiluolyolualuilwulyu" +
            "maimaumlamramwamyameameemeimeomlemremwemyemiamiemiomlimrimwimyimoamoemoimoumlomromuomyomuamuemuimlumrumwumyu" +
            "nainaunranwanyaneaneeneonrenwenyenianienionrinwinyinoanoenoinounronuonyonuanuenuinrunwunyu" +
            "paipauplaprapwapyapleprepiapiepioplipripwipyipoapoepoiplopropuopyopluprupyu" +
            "quaquequiquo" +
            "rairauryareareereireoryeriarierioryiroaroeroirouryoruarueruiryu" +
            "saisauskaslasmasnaswasyaseaseeseiseoskeslesmesneswesyesiasiesioskislismisniswisyisoasoesoisouskoslosmosnosuosyosuasuesuiskuslusmusnuswusyu" +
            "taitautratsatwatyateateeteiteotretsetwetyetiatiotritwityitoatoetoitoutrotsotuotyotuatuetuitrutsutwutyu" +
            "veeveiveovrevwevyevieviovrivwivyivoevoivrovuovyovuevuivruvwuvyu" +
            "yaiyauyeayeeyeiyeoyiayieyioyoayoeyoiyouyuayueyuiyuo" +
            "zaizauzvazlazwazyazeazeezeizeozvezlezwezyeziazieziozvizlizwizyizoazoezoizouzvozlozuozyozuazuezuizvuzluzwuzyu";
    public final Arrangement<String> items = new Arrangement<>(256, 0.5f, Hashers.caseInsensitiveStringHasher);
    public final OrderedMap<String, ArrayList<String>> adjective = new OrderedMap<>(Thesaurus.adjective), 
            noun = new OrderedMap<>(Thesaurus.noun);
    public final Arrangement<String> allAdjectives = new Arrangement<>(155, 0.5f, Hashers.caseInsensitiveStringHasher),
            allNouns = new Arrangement<>(327, 0.5f, Hashers.caseInsensitiveStringHasher);

    /**
     * Default constructor for a Mnemonic generator; equivalent to {@code new Mnemonic(1L)}, and probably a good choice
     * unless you know you need different seeds.
     */
    public Mnemonic()
    {
        this(1L);
    }

    /**
     * Constructor for a Mnemonic generator that allows a different seed to be chosen, which will alter the syllables
     * produced by {@link #toMnemonic(long)} and the words produced by {@link #toWordMnemonic(int, boolean)} if you give
     * the same numeric argument to differently-seeded Mnemonic generators. Unless you know you need this, you should
     * probably use {@link #Mnemonic()} to ensure that your text can be decoded.
     * @param seed a long seed that will be used to randomize the syllables and words used.
     */
    public Mnemonic(long seed)
    {
        RNG rng = new RNG(new LightRNG(seed));
        int[] order = rng.randomOrdering(431);
        int o;
        for (int i = 0; i < 256; i++) {
            o = order[i];
            items.add(baseTriplets.substring(o * 3, o * 3 + 3));
        }
        for (int i = 0; i < adjective.size(); i++) {
            allAdjectives.putAll(adjective.getAt(i));
        }
        allAdjectives.shuffle(rng);
        for (int i = 0; i < noun.size(); i++) {
            allNouns.putAll(noun.getAt(i));
        }
        allNouns.shuffle(rng);
    }

    /**
     * Constructor that allows you to specify the adjective and noun collections used by
     * {@link #toWordMnemonic(int, boolean)} as well as a seed. This should be useful when you want to enforce a stable
     * relationship between word mnemonics produced by {@link #toWordMnemonic(int, boolean)} and the int values they
     * decode to with {@link #fromWordMnemonic(String)}, because the default can change if the adjective and noun
     * collections in {@link Thesaurus} change. There should be a fairly large amount of unique adjectives and nouns;
     * {@code (long)adjectives.size() * nouns.size() * adjectives.size() * nouns.size()} should be at least 0x80000000L
     * (2147483648L), with case disregarded. If the total is less than that, not all possible ints can be encoded with
     * {@link #toWordMnemonic(int, boolean)}. Having 216 adjectives and 216 nouns is enough for a rough target. Each
     * word (adjectives and nouns alike) can have any characters in it except for space, since space is used during
     * decoding to separate words.
     * @param seed a long seed that will be used to randomize the syllables and words used.
     * @param adjectives a Collection of unique Strings (case-insensitive) that will be used as adjectives
     * @param nouns a Collection of unique Strings (case-insensitive) that will be used as nouns
     */
    public Mnemonic(long seed, Collection<String> adjectives, Collection<String> nouns)
    {
        RNG rng = new RNG(new LightRNG(seed));
        int[] order = rng.randomOrdering(431);
        int o;
        for (int i = 0; i < 256; i++) {
            o = order[i];
            items.add(baseTriplets.substring(o * 3, o * 3 + 3));
        }
        allAdjectives.putAll(adjectives);
        allAdjectives.shuffle(rng);
        allNouns.putAll(nouns);
        allNouns.shuffle(rng);
    }
    /**
     * Constructor that allows you to specify the adjective and noun collections (given as arrays) used by
     * {@link #toWordMnemonic(int, boolean)} as well as a seed. This should be useful when you want to enforce a stable
     * relationship between word mnemonics produced by {@link #toWordMnemonic(int, boolean)} and the int values they
     * decode to with {@link #fromWordMnemonic(String)}, because the default can change if the adjective and noun
     * collections in {@link Thesaurus} change. There should be a fairly large amount of unique adjectives and nouns;
     * {@code (long)adjectives.length * nouns.length * adjectives.length * nouns.length} should be at least 0x80000000L
     * (2147483648L), with case disregarded. If the total is less than that, not all possible ints can be encoded with
     * {@link #toWordMnemonic(int, boolean)}. Having 216 adjectives and 216 nouns is enough for a rough target. Each
     * word (adjectives and nouns alike) can have any characters in it except for space, since space is used during
     * decoding to separate words. You may want to use {@link StringKit#split(String, String)} with space or newline as
     * the delimiter to get a String array from data containing space-separated words or data with one word per line.
     * It's also possible to use {@link String#split(String)}, which can use {@code "\\s"} to split on any whitespace.
     * @param seed a long seed that will be used to randomize the syllables and words used.
     * @param adjectives an array of unique Strings (case-insensitive) that will be used as adjectives
     * @param nouns an array of unique Strings (case-insensitive) that will be used as nouns
     */
    public Mnemonic(long seed, String[] adjectives, String[] nouns)
    {
        RNG rng = new RNG(new LightRNG(seed));
        int[] order = rng.randomOrdering(431);
        int o;
        for (int i = 0; i < 256; i++) {
            o = order[i];
            items.add(baseTriplets.substring(o * 3, o * 3 + 3));
        }
        allAdjectives.putAll(adjectives);
        allAdjectives.shuffle(rng);
        allNouns.putAll(nouns);
        allNouns.shuffle(rng);
    }

    /**
     * Given any long, generates a slightly-more-memorable gibberish phrase that can be decoded back to the original
     * long with {@link #fromMnemonic(String)}. Examples of what this can produce are "noahritwimoesaidrubiotso" and
     * "loanuiskohaimrunoizlupwi", generated by a Mnemonic with a seed of 1 from -3743983437744699304L and
     * -8967299915041170097L, respectively. The Strings this returns are always 24 chars long, and contain only the
     * letters a-z.
     * @param number any long
     * @return a 24-character String made of gibberish syllables
     */
    public String toMnemonic(long number)
    {
        return toMnemonic(number, false);
    }

    /**
     * Given any long, generates a slightly-more-memorable gibberish phrase that can be decoded back to the original
     * long with {@link #fromMnemonic(String)}. Examples of what this can produce are "noahritwimoesaidrubiotso" and
     * "loanuiskohaimrunoizlupwi", generated by a Mnemonic with a seed of 1 from -3743983437744699304L and
     * -8967299915041170097L, respectively. The Strings this returns are always 24 chars long. If capitalize is true,
     * then the first letter will be a capital letter from A-Z, all other letters will be a-z (including the first if
     * capitalize is false).
     * @param number any long
     * @param capitalize if true, the initial letter of the returned mnemonic String will be capitalized
     * @return a 24-character String made of gibberish syllables
     */
    public String toMnemonic(long number, boolean capitalize)
    {
        char[] c = new char[24];
        String item;
        int idx = 0;
        item = items.keyAt((int)(number & 0xFF));
        c[idx++] = capitalize ? Character.toUpperCase(item.charAt(0)) : item.charAt(0);
        c[idx++] = item.charAt(1);
        c[idx++] = item.charAt(2);

        for (int i = 8; i < 64; i+=8) {
            item = items.keyAt((int)(number >>> i & 0xFF));
            c[idx++] = item.charAt(0);
            c[idx++] = item.charAt(1);
            c[idx++] = item.charAt(2);
        }
        return String.valueOf(c);
    }

    /**
     * Takes a String produced by {@link #toMnemonic(long)} or {@link #toMnemonic(long, boolean)} and returns the long
     * used to encode that gibberish String. This can't take just any String; if the given parameter isn't at least 24
     * characters long, this can throw an {@link IndexOutOfBoundsException}, and if it isn't made purely from the 3-char
     * syllables toMnemonic() produces, it won't produce a meaningful result.
     * @param mnemonic a gibberish String produced by {@link #toMnemonic(long)} or {@link #toMnemonic(long, boolean)}
     * @return the long used to generate {@code mnemonic} originally
     */
    public long fromMnemonic(String mnemonic)
    {
        long result = 0L;
        for (int i = 0; i < 8; i++) {
            result |= (items.getInt(mnemonic.substring(i * 3, i * 3 + 3)) & 0xFFL) << (i << 3);
        }
        return result;
    }

    /**
     * Given any int, generates a short phrase that can be decoded back to the original int with
     * {@link #fromWordMnemonic(String)}. Examples of what this can produce are "Mindful warriors and the pure torch"
     * and "Dynastic earldom and the thousandfold bandit", generated by a Mnemonic with a seed of 1 from -587415991 and
     * -1105099633, respectively. Those Strings were generated using the current state of {@link Thesaurus} and the
     * adjectives and nouns it stores now, and if Thesaurus is added to over time, those Strings won't correspond to
     * those ints any more. The Strings this returns vary in length. The words this uses by default use only the letters
     * a-z and the single quote (with A-Z for the first character if capitalize is true), with space separating words.
     * If you constructed this Mnemonic with adjective and noun collections or arrays, then this will use only those
     * words and will still separate words with space (and it will capitalize the first char if capitalize is true).
     * @param number any int
     * @param capitalize if true, the initial letter of the returned mnemonic String will be capitalized
     * @return a short phrase that will be uniquely related to number
     */
    public String toWordMnemonic(int number, boolean capitalize)
    {
        final int adjectiveCount = allAdjectives.size(), nounCount = allNouns.size();
        StringBuilder sb = new StringBuilder(80);
        boolean negative = (number < 0);
        if(negative) number = ~number;
        sb.append(allAdjectives.keyAt(number % adjectiveCount)).append(' ')
                .append(allNouns.keyAt((number /= adjectiveCount) % nounCount))
                .append(negative ? " and the " : " of the ")
                .append(allAdjectives.keyAt((number /= nounCount) % adjectiveCount)).append(' ')
                .append(allNouns.keyAt((number / adjectiveCount) % nounCount));
        if(capitalize)
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     * Takes a String phrase produced by {@link #toWordMnemonic(int, boolean)} and returns the int used to encode that
     * String. This can't take just any String; it must be produced by {@link #toWordMnemonic(int, boolean)} to give a
     * meaningful result.
     * @param mnemonic a String phrase produced by {@link #toWordMnemonic(int, boolean)}
     * @return the int used to generate {@code mnemonic} originally
     */
    public int fromWordMnemonic(String mnemonic)
    {
        final int adjectiveCount = allAdjectives.size(), nounCount = allNouns.size();
        int idx = mnemonic.indexOf(' '), factor = adjectiveCount;
        boolean negative;
        int result = allAdjectives.getInt(StringKit.safeSubstring(mnemonic, 0, idx));
        result += factor * allNouns.getInt(StringKit.safeSubstring(mnemonic, idx + 1, idx = mnemonic.indexOf(' ', idx + 1)));
        negative = (mnemonic.charAt(idx + 1) == 'a');
        if(negative) idx += 8;
        else idx += 7;
        result += (factor *= nounCount) * allAdjectives.getInt(StringKit.safeSubstring(mnemonic, idx + 1, idx = mnemonic.indexOf(' ', idx + 1)));
        result += factor * adjectiveCount * allNouns.getInt(StringKit.safeSubstring(mnemonic, idx + 1, -1));
        if(negative) return ~result;
        else return result;
    }
}
