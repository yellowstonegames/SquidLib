package squidpony;

import squidpony.squidmath.*;

import java.util.ArrayList;

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
    public final int adjectiveCount, nounCount;
    public Mnemonic()
    {
        this(1L);
    }
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
        adjectiveCount = allAdjectives.size();
        for (int i = 0; i < noun.size(); i++) {
            allNouns.putAll(noun.getAt(i));
        }
        allNouns.shuffle(rng);
        nounCount = allNouns.size();
    }
    public String toMnemonic(long number)
    {
        return toMnemonic(number, false);
    }
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

    public long fromMnemonic(String mnemonic)
    {
        long result = 0L;
        for (int i = 0; i < 8; i++) {
            result |= (items.getInt(mnemonic.substring(i * 3, i * 3 + 3)) & 0xFFL) << (i << 3);
        }
        return result;
    }
    
    public String toWordMnemonic(int number, boolean capitalize)
    {
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
    
    public int fromWordMnemonic(String mnemonic)
    {
        int idx = mnemonic.indexOf(' ', 0), factor = adjectiveCount;
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
