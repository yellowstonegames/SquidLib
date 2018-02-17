package squidpony.examples;

import squidpony.squidmath.*;

/**
 * This class is a scratchpad area to test things out.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Playground {

    public static void main(String... args) {
        new Playground().go();
    }

    private static float carp(final float x)
    {
        return x * (x * (x - 1) + (1 - x) * (1 - x));
    }
    private static float carp2(final float x) { return x - x * (x * (x - 1) + (1 - x) * (1 - x)); }
    private static float carpMid(final float x) { return carp2(x * 0.5f + 0.5f) * 2f - 1f; }
    private static float cerp(final float x) { return x * x * (3f - 2f * x); }

    public static double determine2(final int index)
    {
        int s = (index+1 & 0x7fffffff), leading = Integer.numberOfLeadingZeros(s);
        return (Integer.reverse(s) >>> leading) / (double)(1 << (32 - leading));
    }

    public static double determine3(final double base, final int index)
    {
        //int s = ;//, highest = Integer.highestOneBit(s);
        //return Double.longBitsToDouble((Double.doubleToLongBits(Math.pow(1.6180339887498948482, base + (index+1 & 0x7fffffff))) & 0xfffffffffffffL) | 0x3FFFFFFFFFFFFFFFL);
        return NumberTools.setExponent(Math.pow(1.6180339887498948482, base + (index+1 & 0x7fffffff)), 0x3ff) - 1.0;
    }
    /*
     * Quintic-interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
     * Will smoothly transition toward start or end as a approaches 0 or 1, respectively.
     * @param start a valid float
     * @param end a valid float
     * @param a a float between 0 and 1 inclusive
     * @return a float between x and y inclusive
     */
    private static float querp(final float start, final float end, float a){
        return (1f - (a *= a * a * (a * (a * 6f - 15f) + 10f))) * start + a * end;
    }
    public static double querp(final double start, final double end, double a) {
        return (1.0 - (a *= a * a * (a * (a * 6.0 - 15.0) + 10.0))) * start + a * end;
    }

//    public static float sway(final float value)
//    {
//        final int s = Float.floatToIntBits(value + (value < 0f ? -2f : 2f)), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
//        final float a = (Float.intBitsToFloat(((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40000000) - 2f);
//        return a * a * a * (a * (a * 6f - 15f) + 10f) * 2f - 1f;
//    }

    public static float swayOld(float a) { a = Math.abs(Math.abs(a - 1f) % 2f - 1f); return a * a * a * (a * (a * 6f - 15f) + 10f); }

    private int state = 0;
    private int mul = 0xF7910000;
    private float nextFloat(final int salt)
    {
        return (((state >>> 1) * mul ^ ((state += salt) * mul)) & 0xFFFF) * 0x1p-16f;
        /*
        return NumberTools.intBitsToFloat((state = state >>> 1 | (0x400000 & (
                (state << 22) //0
                        ^ (state << 19) //3
                        ^ ((state << 9) & (state << 3)) //13 19
                        ^ ((state << 4) & (state << 3)) //18 19
        ))) | 0x3f800000) - 1f;
        */
    }
    private int nextInt(final int salt)
    {
        final int t = (state += salt) * 0xF7910000;
        return (t >>> 26 | t >>> 10) & 0xFFFF;
        /*
        return NumberTools.intBitsToFloat((state = state >>> 1 | (0x400000 & (
                (state << 22) //0
                        ^ (state << 19) //3
                        ^ ((state << 9) & (state << 3)) //13 19
                        ^ ((state << 4) & (state << 3)) //18 19
        ))) | 0x3f800000) - 1f;
        */
    }
    public static double swayRandomized(final long seed, final double value)
    {
        final long s = Double.doubleToLongBits(value + (value < 0.0 ? -2.0 : 2.0)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m,
                flip = -((sm & 0x8000000000000L)>>51), floor = Noise.longFloor(value) + seed, sb = (s >> 63) ^ flip;
        double a = (Double.longBitsToDouble(((sm ^ flip) & 0xfffffffffffffL)
                | 0x4000000000000000L) - 2.0);
        final double start = NumberTools.randomSignedDouble(floor), end = NumberTools.randomSignedDouble(floor + 1L);
        a = a * a * a * (a * (a * 6.0 - 15.0) + 10.0) * (sb | 1L) - sb;
        return (1.0 - a) * start + a * end;
    }

    public static double asin(double a) { return (a * (1.0 + (a *= a) * (-0.141514171442891431 + a * -0.719110791477959357))) / (1.0 + a * (-0.439110389941411144 + a * -0.471306172023844527)); }

    private void go() {
//        for (double i = Math.PI / 9.0; i <= 0x1p30; i *= Math.E) {
//            System.out.printf("% 21.10f : % 3.10f  % 3.10f  % 3.10f\n", i, NumberTools.sway((float)i), NumberTools.sway(i), swayRandomized(seed, i));
//            System.out.printf("% 21.10f : % 3.10f  % 3.10f  % 3.10f\n", -i, NumberTools.sway((float)-i), NumberTools.sway(-i), swayRandomized(seed,-i));
//        }

//        for (double i = 0.0; i <= 1.0; i += 0x1p-8) {
//            System.out.printf("% 3.10f : % 3.10f  % 3.10f\n", i,  Math.asin(i),  asin(i));
//            System.out.printf("% 3.10f : % 3.10f  % 3.10f\n", -i, Math.asin(-i), asin(-i));
//        }
//        GreasedRegion earth = new GreasedRegion(INSERT_MAP_HERE, '.').expand(2), work = earth.copy();
//        String ser = earth.serializeToString(), comp = LZSPlus.compress(ser), serSurface, serFringe;
//        //System.out.println(ser);
//        System.out.println(ser.length());
//        System.out.println(comp);
//        System.out.println(comp.length());
//        System.out.println(ser.equals(LZSPlus.decompress(comp)));
//        GreasedRegion remade = GreasedRegion.deserializeFromString(LZSPlus.decompress("ᖢᠳ\u0088氦悠ぉ䓙㛃疓ᾏ尿䣢✶㒫敹✊盰织\u0CDC淶\u1FD5玿帛悟სᇢ捉䀡㇀׀ԃ̠\u0CE4倽䘾६.ど嗋䇗掊䞯\u0AC6ᖌ猘⎲䯬䃣䱟Ȥᮔ囵⟡㎡ᚲᠫዌђ\u2D26ڐ䍘෴℞吹ᡖ孶擉ྠ䒻㒵၊恝Ψᙤॉᄕ̡ᖕ᪁敨ᖨᝆӋᝳ娻Žዠす\u17DE∰屴啎▋慳 ᝐ⫑卲ฤ〸禷\u0DEB㕀ᕅ祩噛❆寰嫩旭⪾൵◬أ椩ழ䧩Ⱖ䥨ᥞ暯◉⅘咖᰷♙撹㿦暠䂗㠺̒ᚧⱖస岾㭹ኙಒ\u2EF5༪ູᡵ旈簡ఠ㐈恝〺㈈ਮཱུ╅ⱖ㚼嘐琫\u2E42\u0BA0瓷\u2BF2暅㺨ಬᔑ\u2E61\u1AFA◺吮\u31E6埰⛿ⱇ坒Łт喩唒偉ⶳ㒗 ⎊̀ᐮ⩕滹\u2BF3༘ᰰ睹侄\u2D2BⒺ濙洡⧅咫ᤪЯ░ଌ浨槓哬呾Ê䴎柖梑䩊㥤Ꮻ猑嗀挗Ꮹ㐠愢⼐ʫᤔ獩緔ⷰᒺⱛ\u2BF2ⲑ➱\u09D0䯷㽶ឍ㐴敻䭊揚▪ɍ滔氣ြ㶡ᰱ榓哥磀ኲ勆ᴕ总嗀ජ佋⒮宵挈ᙛ瑻⩔伉と崡擇异Ü榉᪪⛓珛盀皅旱㝙♵⊼捝削℄漩⍨牎ᅬⵚ䈬ᒢ䓉㒩㉑坌\u08D1ဤ爅㩈թĨ傠㈾ᓣㅊ曰༱◉十䐢䑩ྣੈӑ⇛ᅅٌ䛄㔼ѱ᥎晘☤埰瞱㓩強૰䑢䉌圧䜹挥⚨ؤұ⢘䫂瀯愨恩朩\u1AB8ᐩྱ㙴㈤䳲ḧ冐仔ඡパ搈⌢斅⊹䩃ģ䎘㩔\u0878犵໐౩䶒㉏䋰ᑦ៩义⠉ᄤ㔵祅䕑䒊䨝㲀䰹縦䖑ऩ侪㨱ଣ稲勅ᒕ䓈盆䜙㦂⼝⾴䃓フ䔸ᕘ杳山༴礷䐹\u2E5A⢡䵋⟕♦促َ\u13FD䘷䎘㫄₊➫为㈥琥ୌɔ瘴Ǳಿ⌘䍻ⱺ睊传〣䟨矗\u0B7A捪圹⒞楙ॆ≝䜷㈝ᕚ\u243B䁣爙䭡䙐犥᠐炎凙澇皱㝬敤ᓔĽվ♛濞\u2D26䑾嚣ᤲ冐ヵ勱穗愛嫞殥沱䝩僫攒ம梃့涔愥䝰卪ᏘᒎᏨ坼\u0EC7ệ䑨⾭ⓕ断冐秝\u0EDB炑\u0C51崣䖭槉⃝\u1AC3䑱㝛\u19DB⪷䂙榄〓撔࿘幐\u0C3A哨歵ᩎ䄩ٛ₵孚⥌ằ㎇⋁強瓇䂨ᕎ斨漖\u2B68ᘱ䔭档崷哧⢑ᴺ⏩唒偱䩋瓰㨾䙛穄䬵‰攮⾤㤸ဴ㥱ᵈ⾮ᕓᝓ⎔\u20C3\u31BB归⍉⛝义㴑绔Ǳ䌳庳◥Ⓢᗻ⊡\u0E5D䒐◔≄ዂ⸮ㇸ㪏╘˷絔䮙ス烰䬱फ粻塐༸ᵉ倠ᱨ̹瘓⾌儩璒ⷵ㳭╩瑷㙰⊖线咆❰悆䡊狋䠋咥㷫Ꮾ绹䙆❨估\u0BD2ⅵ㾋䛨堡㻟౩漽棆\u05C8材⎄≠䉖Ᏸ瀳ჸ✑窿屍ᢪ戬獠ᩇ㱛ͨ㮥\u0089\u0B80ⴹ∔ہؤ㮋㐶摝㘌\u0C00竳\u0C91在瓉\u05CA㩀⎬㯧㱃≈ബ䛔䔢ᒧ禃Ḁ塻懐噳手ቌࠣ懞ⶐ愯ᇎ\u0BE0䑿捡㢫⡩\u08CE弡Ӯ\u0B11㬼ハᏙ☬拎ᚣѵᄔⴶᄫ߃Ṣ紬㲁\u05CE搔ᷤ㲟ӑ⍯䉶૭璡抚ᨤ⑫䜀䑓\u1779䍞䂥౧ༀมᷙ埣㶨┈厭砨䪪率绱⪂䘢意\u0FE6晑剚⍢䥹ᜒ䛪勾↜唿ࡂᦱ䈺\u09D3㊩抋▤㚬ሒղਦ剫ၒ灣▮፮㑜⛼ኲ㤆ଠႤ灃䮂ᴽ⋜᬴椁仪冻೭ៀ樣テ⳹洭㧥㚰广Ԑ瞡哵♥䱤為チ昽ガ৮礞瘌焈帺e⼳ℎ᮱Ꮨ碌淪洊\u2432㎋ࣽᑰẪ⃘䃑Ῑ٘᪡碴⟌瘍㧪\u0C0Dද၊ጀ个䭼㺦ṵ嗹檋そ溁剫℆㏒⊃䅊⠭峏ង刴幦䋜毹ᇱტ㜳䩴₲๎狹ᑪ䈯ಐ䳚恹㡲ᙏ穠∢咏痄й嬓䆵㓚爉㉀帽㫪爓䨮擐沭Ⴈ༢替倱㉁翕䋸ᵉ㑍▬⺨㻱ȁ\u085F籗Ủ၊炄ᎆ\u312Fδஈאד昼愹ପ\u10C6\u05FBˊ䣤ٌů榥⡒䛗Ꮗଛ⤤欽ᤰ⊈Ꮸ⏨ᥒ勅≔᎔⭆帺ᗈ摎剬䂎瓽䔀䂀བྷ⁍ᙴ䬥䉄⋇ᭉ⠵⊣熵奫懪⊸唳ൌ㉢∔⁙ㅌ\u0588儆˅༠焚Ϫ椵䁓㲁䉛䁰嵲ゕᢠ䭚ⵠ۶杌痸仭籅檰㗠ⴹᖱᵰ橴倳\u0C50∎䦀ẫᖬ揸嶕徃㱲僴͑䉓ቖ渌౽檕㒣ᵏ彛Ⴛၟ㓢ᘡҸኵᮉ滽㯼͍ਧⓖ攵ᥠ尿㗠⧴展⡡㝲ᰬᣰ䔭呐篗䵮ຽ༴涙ᔽ⻣㈾ใ竓子柔棠懠⻡䵵啠惭梁䪍ᷕႆⅣ㓚䌽∧略䕲\u242F摥䪈Ἤھḁ㢨㣈ᨉశ䰤愻ᖊ塢₵➖ᜄⳇⰄュ౬䑘淴卽\u1AC6̲㓚ې瑚㚬㊤棆椔粭巓儑冣烒峰嘲癜₂ᬅվ啕ピ䴼ḩ樿ᩓ\u00AD県る⺟ᯍ幄┏敃䄸疦\u1DF4⣜୷⨎旄\u4DB9㠬㒐ડ▋ៃ䋌屈်娦杻எᅿ続㐹町\u0B8C椡嫾㦾峡㎁憈ᜮ␖\u0AF7妈㛁ⷮ徬ᣴ◧瑳\u0560㛞灂唊㴨͠䵒ᵎ惵䃾断姰ⷘᠯ䙬淫㩵ݴ㴾ⅱ㨔希⢫岄ŏ摉掜Η垫穆π墉奪毤ᖪऐ敖墷ॴ䧯寞⁀榧ᶑඬᬮ嵊ȸᖤ䵠庯ਿ浬姮᱖䀱හ\u18F8燱ὔ㕴䵣垏፭\u12B6屜洴皽禴砒粴˲\u18AC᧹ᴔ՝紊姳䡾滄痱崇狉物᪦唫埩珑㯣ᚸ剗姯䌉侙窼◴䆴⟑❿僥缾ؑཝᬒ僝䝾炾巤ᶏ瘃䮁㮧䜷ᡯᵴ崞狹ᚍ\u10CC巗湅ᵼ䟪䷴䫅粲╥଼ٕ㗪⺉⥏㴣喂婼剗\u2B92剻㫇捽▵\u0C75ᢁ玈弭ᠺ焍\u0B4E竽盺ه廜㟘䫠\u2438ᔨ揤➿国溫扜䯴㻽凃摟Ⓒ枴㴇ၮ祡ㆠ䢤㍮ၐ檌ຄ䒇ᦒ枝\u2B68ⲟ孷⭖㱾䁙傛甈ٯ嫣唎ᮩቲ̫⧧璒⌧力ߚ䮸卣妰\u009F∞ړ絅䊓䠖籓簁䆗紓㜏撦㠈尔⎈Ṑ㟩湟㢛ሸ௭祫ᾓ嫭員㬠ᶉ᥈䮼⬁፼∆䕁‶ౘ玉厴口∣ɹ圐⬥圶愛ᜩ༨㭄Ӽ䰣⠿ㅑ⎉ɀ䳄殂≠ϴ\u08C5疄⩜Ĺ䑯ⅽ㉤ᦥ嘃ᦋ⇬ް㨨㠦Ԝ䤴᱈溏䂀值㍮ᡮ㎕\u0E71㼤\u086B䌬\u0863\u08C1夿⩈倥恢ů媉ᑵ㴯ᄃ匉ᜣ䜍圢ڦᏘᨼį妠䝣嵫\u0B49⫢ᇠ丌ㄜԠ⿸♣⩋\u23FD䴙८ѝ㊈ᤨ狡Üޛᗢ桢㈩⣏ዡ槈婁慸Ꭳ❯㵀䝘⑃㬏癃犤Ꮳ㪎㹥凜щ測⠭Ɉٱ䄺㝗ޜɃᔪ庈䟸ℵ䝪䢗獶䓃☶ऒ䛧ᖵᔵ\u08D0ㆳḀண捆䚤⣱愶棙ѕ\u0D98䑦Ѵ㈔ၐ箠椴䖻䵃䅬ᣨ䖬㢩Զ呝㊡䲝⌇祡側㒃儰䰪愫᩠嬼杒䫔㑈⌵⤉Ǖ弴ᝯ䝂挨唃⬆ሦ㥌⏂挷ᕺ拉\u0DFD紨礉氟䯜᪪惝䝼⡐劫䉵ገ\u10C9㓄礻㪗弽䂹⨴炧ẙ槦Ⅎ㊶᠂嬫䡘⓬ĳ県犖僂⒙梳ᣔ扢卑䬠Ү移寝ⰼṔ珜岳䄥ፃ卒ὡာ磑㤍ᆲ梼操㧸ᭁ甩䅮掲ⓢ̰壞✄Ḅ嬸畦抖ᥢ䰱慿⭂⻙Ⓖ擂䔕Ӑᑣ磏î̳ỂႿ⬫ᥙ檴攁䍅ᖁ缀刳̢ͼ䪼㔚戭ᩳ⏢╭㏌ᝨ㿈呰Ꮲ塣⨼瓈̲㳳粴渥⡳䞸猩ֆ㈼Ձ渤汙䌢\u1C4Cⓢⱪ猦㕙↴ヹ䚻宬ڻ䅮熺⍒㊼ᢃ⤛̀㯨᜴䋤ԑֺ䔂擌ܙ儠唰䍔൰崽ぽɈᵀᒨҔ时ⷳ挱ぷ槸ὀ澭Ʉ䩅ሀ⌨摯\u0AF4\u08C0綱哆犲䢀ᘤ㑙晤ᡅ桉㖌捍ὠ泫⳥杳ᓌ慯梃僇ྐ\u0EEDٷⓞᓀޱ㔓箅叁勡喔牾㫠ስ䅜⋾㬓潁⡛ةဉ濧ޜ厠䛔㫨慮݈Ɠ瞶ぎ䤽॓ḧ嵄爁ń囬奢簎⠒倈泤䍫ਬ䡄䞔\u087Aᓒ峆㕊\u0866᧘◪癡㬾⑀痫潂⡧᪀㊣䌆摡㥴䛢溊ᓤᜂᨧ亀䀷ᤪ溭渵昗Č哋㳵↑ⳙᖸ癭犖呪ᓂ灠⎎ᜉ狭佣㣄ह⬲喂५౫ᓂ勩\u1737\u05F8㉳ࡍᒡ\u0DE9ᛂጝᕉ≫ᱵ⋓ᕟ᭪厵磆昁⥴↠狳ᗗᳫ六睵䖟ᩨ汹犮ቱ⟫无勻ក䧌ܺ˶ᒙ\u2D78并助梉\u3101㱶棝â᠌\u2073挤噊䥊慾ᴨ⇅㫉Χ䴙ᗙ㜪ռ\u0884唎䈔ঈ⌁ၴ䕀糥⪰嘵㿠彤̦啚䰹✼で䌹崥止⋳Ⱈ㲫嘭欛歲ᇋ毮ᑂ䦧Ῐ䢡ℑ戺ㆢ人唉囁俰⋮硊䏓丁㸎礿䋘ᶋ纽畠枍㟋㭼∥坓\u1CA5丩㥱⌄\u23F9䤺⢶䝽㚊絶牛ਐ䡤殹╭ᇍ㮲罅欈䊱彁ᎄ\u0E7A⟘ᜱ㻭䣕嘢ㄲ㴿䨰筬\u0A3B撍♓䂴ტ歬ぉઽ㿨㘎䋵䯔͇ᢩ嵶ᬮᓩ巩偡⏈䨀䷪窫磏䤜ち✆᭘ߨ㓳䜟响啄俌䙓吮䶔⠹沼㊓⼉峿\u0893ႸӠԂ梓℃㫉滦ˁ祐䨪勵⚾㗫ⵕ¤嚮懸䬄\u1FB5䍏柱ኺⲋ嚯䭋䊼Ӻᢶ\u08B2⋳┦欺ᩌ\u1C81㾀溚ᇔඅ┥䊷㞅䷻⸉盔\u09D1͘剸狿᠘ᗻᘮಘᯣႪ佻盧掘啋૮㠸甛⊈⠅䦖兙ᅉ⨍湓¼ᘀ癍ौ爧㙛廭沢⦣ૈ⁼灳䋤წⳡ䐪睧ૠ姿\u0EFA疈䴥㋨Ὠ⃡ፚ燳\u2EFC痦⊢嗩\u243E䚜ᷛᘋܫÐ㮩叁⚭㦪ࠢ俇\u1CB5ᅯ⡍函䮂䈞⁝⚄拵盱\u1C4C乧㱽焙帹㏾欣痎⤚\u16FA弐ϓ匀ⶢሬ䛝ਫ▉娴嬑候啯㫡䉝㖀㡌绎倠ⵀ兩㢲悌ᰛ\u1AFD累ኡ怌叹紑䇳䋻ห禌反Ꮣ攥䖈✖⸧ၰ䈟瑻㽻㢢҃䇵˙⪮䖘᩹Ⴇᦀ縦妈ዌ棲悥䐟ᙱ㑀瘱䃇Ń矡䚸寵䕡ඈ⚧Ű䯕ᄠ塵卟㗤活㡧˥䌚≻⡹歖ᵀ帣炨⊠ᷘ曩倣⨆㓀ⳍⅿ扚䅪橐䮆䊈抣㪾䒇瞍Ꮵ⤦Ṟ\u0FF1⠔᱄䞅晸斬ᩊ䡯棹㟘癶䚈樧㺚䶮㙰䭗㧹杂楥⛈汈㹓㡍\u0E5F㾐䱘噱໐抌⧴™સ孱䮵Ҷ䃩ṱ⩠皆ᑉ⧭㡷ᒇ΄Ⴆ⢎&\u2BE1ః≧ぃ̲᳀俾⨆亠东ଯ⠠㈬䈨甠\u0D54⤒ᯘ浟硩㮯㸼劬䉳⡔\u0C04㪫田≹֪叢槰揔檇㕃抒॥䛰汫砨愲\u1CACᔢ殆䲵㎇ፚ廐痝⸨㏡ʋ扚ሑⵋ咙ᯠ䌴咷\u088A㤜\u087Bᙌ్\u0CD4䳰偗ᡈ䘼Ї㡥秠䥇ކ⩦砺䎘Ⲉ偁媀檍چ枹בᮜ楙㝒ͳ䈬១唬吨ⱝ䮁僙剸ⶤɉ⺫౩扆䴓漥煼↮ᴧ㽕ᅥᨪૠ愤䀯⦡ა繛㉁䫼掷⚬ᐾ⸌綕泇㓢㩍ሃ厼\u2437↲\u08CDᢴ犉᭪潉㋝ᢶ矂ỹڳ幮䞸᭔\u1ADB㦩䖊\u0A31䫘偩ⷊ棶㸷ᘎ䔏㱶⫐㘕\u2EFA灐枩䱍\u23FAា囚⦻⾛㛁\u206Aಞ䄌䤠ⲻ䡸ͨ悻晣皹⎵㒈瞭喃㥢㓹ᇔᜂღ懜勯㑐淄ɖ嶶䷄眷䭗⇙的渆汭Ӹⷬ求䓗䜄玅塜媳给兢瑵ᄠ㱄孉竒㏭毡䷶濺ޤط桤敘ᄘ抯伊淖昢\u2B5A硳朗ۆ称䩧掦వ淙祁淮璖煡ᶫ㡄ఁᙉ〰护ୠ狞᷅汫䗉ࠠ㸅⬾洛ௗ䨫潊ዥ咣樳䋄勓痻捺㧭༲\u2FD6嵠䂸ЖᏒ\u1B4D叮㙴嬡ڵ䥣Ì䇡ḓ券䎁悞灨ṹᅠ゜淸泝ᠹ⇑ΰ竁攜握䷠㍴済㢜䏜ᱱമቑ䏬ㅿ✹凃᥀校᪐杓⏆㈰䷚ಔᔓ͑稳犎䶠渕ႃ᳖ᗎẊេ挩ē៷\u3103˭瑎᳁̺硒❓清۵勽⛏䌶ᅯ㲒吚矵㰊¤䁿䍠ৡफ籮䏾戢♺湗㆜Ӯ⡥⠫ᴔ廛₵छ⁙泯湫澍济瓺䄉ᐃᷥ凯暟⺙凕ᵺ᰿噀已௯些玾筀ℌደ䙯Ῐᑎ㺕㏈Ņ皍⦜\u0BD8嶥歏ᆑ̈忚哮娀噎䊒⢩\u2E6A暡䭡ᥰ媲ࡂ䫲羥ᩪ犇竷䌪廖籣傝嫇磪爣涂嘀ᒯ䞓ޅ罪㇠硺\u2BA5籽䓮摴䊹擠㔡批⧈⓹籡偹ᘧ\u19DB冦緊ෛ䭇殔ᔽ䋴ᮏ晸㘺䔴䠕❣桄ˀ倔䊨䖜㢫θ寺碉眂䃭仫㐖瀧ḏ៷ښὋ\u09C6´䏌⌍搯㙆ϰ㿰⬽孡䉆䴣仔\u08E0߲ᆞ皈椘䙫ᝣ甥ѡ㊧握昦瑨⟏掣璯ᄘࠐጣ笾睾篳ⸯ㒿ۢᘇ尳烹㊧呖歓樿妧ᐁㄡह㊛䱊愃我ᴛ⢝⇄みᙶ䌥㙱୰ིࠊ懊\u08D0峓承ŉẹ睤ᰤⶴͥ瓿㹰棈伉⡪ቨ䶂ᖑ烒⡕Ḏᖙ⎼\u086Dཱྀ亾恝㠗\u08CE囕厦ཇ噇䞐掙㡬厄\u23FAア⒘㙋瑾婿砉䈗ᅈఎⱵ䵊\u0A61೫䘹恐䊩ᘈ䚐排ᭀ༧⩅ଃ\u175F䖡᭢ᙝ湄峐䩓呭敁ӗ⡰协ㅡ丣煛\u1FDCዃ喺惇ਭ㠩㸌䘳勸ܓ▢倿㫵⨁窺{\u0AE5ὈŔ⡥䄋с㺎ᢪጽ֢㎚㚊₮Χょኗ㓶ө㑰⒒箠๐⍯尔\u2BE5ᕤ̯䴭∘橡ጐ䄦峊ᔨ反әᭃḸ廪㭚桖婀㏂测搀⧡㏑洡㺚慡㓘ٰ晡Մ☴₇䈤̤㖑怢⣱\u0885儸▀夒倳榯\u2451ᐨ≲\u0096‑䴡⣫偉ᤌ\u20BBफ़懡糣燀Ź獪\u0D5AŰ煀\u08B2倣⃜ど姐潀戢ধ㨦ě࿚㒭䔲䆰房䤫 䜘ඹ媋氢盵䌛䒈྄㗢⢣\u0D81ᄎ旄ੜ䛪傭᱘汞㉀℁Υ淡爰䖅攍Ꮀ寄眡पᦿ瀣ψ\u0EE2ͱⰷ\u18AF甴↝凞\u0EF2cĤⅇ㡵⤘ณ\u0B00㿗\u0B3Aሯ¬⌠⫪☙䭍䌢䣶䂸႖㗡ŀǪՀဠᕓŸ⡃巍捝ङ⥍䌈ଥℬ㈤䈳儁\u0094ᛊ老ᛣ㿔⢉妭⚧ō䇃ᴨၓㅱĮᝂ篙Ϭㅞ\u202A祢䞈\u1DF9祣穥℧典㞖ᥢ䪫⒧⑇䇎毅՞㼱◰㬩兘䔉搿奢ᱠ\u052B◌ṩእચ←㛶䮺〣哚䝚䵤ⳁ嬁ᮤ䘰䈔掶ᄐḁ憣\u0B98ᤎヹ᎔垀昦⏉ᔁӔ䦤⺙ृ庱⒖४㍘㦨\u0BA7唯ਢ䱂⦧慾㚙ㆵ㍽ጴႲᄹք枃㈻ᔵ缉碸\u08D0ހ縩簠媂墜䜖Ụ煩⨳ⰳ㋼夵䢪˕籠稣ൽϒ吉屘禁⫥朿\u0E8B叿ೈ⦑Զ䤡䈭ᆷ唓䏠筃满坛\u244Fޑ䑙ཽ琮䟩゜灺昉ᮮ⣓燥䤩拐\u0B91䬔⏠ℕ弸璕\u09BA砐嶰\u0A78\u0B65浈呥ƌ伤⍘瓤᭼値淝吜ጐ哰樭璾ಓ礊Ⰴង㭆ᒨ烶ϖ珼䒀㖱秃㰪氵òూᒢิ映买䈵\u23F8\u18FC氋篦悴礎⣵⽘ṵᧀ武ᑲ६䚅䌢ẉ➡у䃐⥭\u2073\u206D⽂加傏䠬ጅ后䃙滅Ҵ儣ᢌׄହ㗣欥栻⇄ͼ墖ᾱᗊ\u05CD爯8⳺ខढ‱ࠢ桉̒\u1774濫‣堦岛劦撨\u0DC8僴ᥠ䪟ⷢ䈂\u1316㸺妯哉塎扂乑ƀ浣⑮摋懭݈༦嵫䙂巁怨搘䠰\u0B00䢠⊿牅栲ȃỮἰ㬹嬱䆚ᅀ毛㑠喅挠᎒⣳ॲ厸淤惢呞⩍䨵Ⅻኸ㗗㴩碣ᖴ垹Ὀ監㞩乙ŕ懱♸浠ᖨ孫潜ᣇ֨噀⑇眵䁚Ч䙋䒖㒑㧣䜣㤾ぃ烪循̢\u0A61䬩ㇰ⟤⊈㴅㝕‥ᤇ捘■\u05C8窫悪䁛⫣䦯殄㒠洡㑕䔜哉㐎࿘㻁稠͝ᘨዱৠ滈懤圮⅔µᔂ\u2BE0㬃古扁氶婐ⱬᕅ呣悯\u2E76ኺᆤⷀ䁢敨硙磈Ǫ✛ₜ䩆厵`⒃ቢự祒墲⚻峙Ǒ䦒\u2B79䰇嗳≷ᎊ惝ౄ㑑冢ᡁ᳔╅檰ိ叀箭\u0095㗓◴共ⴀ廤⭙ₔӘஈ\u3101垁䦽ᑱೣϛᮔ挡倧眩ⱱ儒慨۹᎓‰儁䇳▂⒜ૄ⊵夰䒯䃵ᩄᯍ爃禤⩳Iª⃩䎠✅㶷䁷媳步%纥ሺ丶搓Т⻪篦敮枷爌⠩➢ඡ捩㹄Ӛ㰦係峑㼦䚶䁖Ἆ冰૯㫜堢䠮悎\u3103䍀㓉⢋䧣滆§➾⊔䪲આ৴\u2B60秤ښ㑠埄庾穻楌Ġ "));
//        System.out.println(remade.equals(earth));

//        long seed = 0x1337DEADBEEFCAFEL;
//        for (double i = 0.0; i <= 17.0; i += 0x1p-4) {
//            System.out.printf("% 21.10f : % 3.10f  % 3.10f  % 3.10f  % 3.10f\n", i, NumberTools.sway((float)i), NumberTools.sway(i), NumberTools.swayRandomized(seed, i), NumberTools.swayRandomized(seed+1L, i));
//            System.out.printf("% 21.10f : % 3.10f  % 3.10f  % 3.10f  % 3.10f\n", -i, NumberTools.sway((float)-i), NumberTools.sway(-i), NumberTools.swayRandomized(seed,-i), NumberTools.swayRandomized(seed+1L,-i));
//        }
//        System.out.println("NumberTools.sway(Float.POSITIVE_INFINITY)  :  " + NumberTools.sway(Float.POSITIVE_INFINITY));
//        System.out.println("NumberTools.sway(Float.NEGATIVE_INFINITY)  :  " + NumberTools.sway(Float.NEGATIVE_INFINITY));
//        System.out.println("NumberTools.sway(Float.MIN_VALUE)          :  " + NumberTools.sway(Float.MIN_VALUE));
//        System.out.println("NumberTools.sway(Float.MAX_VALUE)          :  " + NumberTools.sway(Float.MAX_VALUE));
//        System.out.println("NumberTools.sway(Float.MIN_NORMAL)         :  " + NumberTools.sway(Float.MIN_NORMAL));
//        System.out.println("NumberTools.sway(Float.NaN)                :  " + NumberTools.sway(Float.NaN));
//        System.out.println();
//        System.out.println("NumberTools.sway(Double.POSITIVE_INFINITY) :  " + NumberTools.sway(Double.POSITIVE_INFINITY));
//        System.out.println("NumberTools.sway(Double.NEGATIVE_INFINITY) :  " + NumberTools.sway(Double.NEGATIVE_INFINITY));
//        System.out.println("NumberTools.sway(Double.MIN_VALUE)         :  " + NumberTools.sway(Double.MIN_VALUE));
//        System.out.println("NumberTools.sway(Double.MAX_VALUE)         :  " + NumberTools.sway(Double.MAX_VALUE));
//        System.out.println("NumberTools.sway(Double.MIN_NORMAL)        :  " + NumberTools.sway(Double.MIN_NORMAL));
//        System.out.println("NumberTools.sway(Double.NaN)               :  " + NumberTools.sway(Double.NaN));
//        System.out.println();
//        System.out.println("swayRandomized(Double.POSITIVE_INFINITY)   :  " + NumberTools.swayRandomized(seed, Double.POSITIVE_INFINITY));
//        System.out.println("swayRandomized(Double.NEGATIVE_INFINITY)   :  " + NumberTools.swayRandomized(seed, Double.NEGATIVE_INFINITY));
//        System.out.println("swayRandomized(Double.MIN_VALUE)           :  " + NumberTools.swayRandomized(seed, Double.MIN_VALUE));
//        System.out.println("swayRandomized(Double.MAX_VALUE)           :  " + NumberTools.swayRandomized(seed, Double.MAX_VALUE));
//        System.out.println("swayRandomized(Double.MIN_NORMAL)          :  " + NumberTools.swayRandomized(seed, Double.MIN_NORMAL));
//        System.out.println("swayRandomized(Double.NaN)                 :  " + NumberTools.swayRandomized(seed, Double.NaN));




//        for (int n = 100; n < 120; n++) {
//            long i = ThrustAltRNG.determine(n);
//            System.out.printf("%016X : % 3.10f  % 3.10f\n", i, NumberTools.formFloat((int) (i >>> 32)), NumberTools.formDouble(i));
//            System.out.printf("%016X : % 3.10f  % 3.10f\n", ~i, NumberTools.formFloat((int)(~i >>> 32)), NumberTools.formDouble(~i));
//        }

//        TabbyNoise tabby = TabbyNoise.instance;
//        double v;
//        for(double x : new double[]{0.0, -1.0, 1.0, -10.0, 10.0, -100.0, 100.0, -1000.0, 1000.0, -10000.0, 10000.0}){
//            for(double y : new double[]{0.0, -1.0, 1.0, -10.0, 10.0, -100.0, 100.0, -1000.0, 1000.0, -10000.0, 10000.0}){
//                for(double z : new double[]{0.0, -1.0, 1.0, -10.0, 10.0, -100.0, 100.0, -1000.0, 1000.0, -10000.0, 10000.0}) {
//                    for (double a = -0.75; a <= 0.75; a += 0.375) {
//                        System.out.printf("x=%f,y=%f,z=%f,value=%f\n", x+a, y+a, z+a, tabby.getNoiseWithSeed(x+a, y+a, z+a, 1234567));
//                    }
//                }
//            }
//        }

//        for (float f = 0f; f <= 1f; f += 0.0625f) {
//            System.out.printf("%f: querp: %f, carp2: %f, cerp: %f\n", f, querp(-100, 100, f), carp2(f), cerp(f));
//        }

//        Mnemonic mn = new Mnemonic(1L);
//        String text;
//        long r;
//        for (long i = 1L; i <= 50; i++) {
//            r = ThrustAltRNG.determine(i);
//            System.out.println(r + ": " + (text = mn.toMnemonic(r, true)) + " decodes to " + mn.fromMnemonic(text));
//        }
    }

    private static long rand(final long z, final long mod, final long n2)
    {
        return (z * mod >> 8) + ((z + mod) * n2);
    }

    private void attempt(int n1, long n2)
    {
        ShortSet sset = new ShortSet(65536);
        short s;
        long mod = ThrustRNG.determine(n2 + n1) | 1L, state;
        for (int i = 0; i < 65536; i++) {
            //s = (short)(i * 0x9E37 + 0xDE4D);
            //s = (short) ((state = i * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL) + (state >> n2) >>> n1);
            s = (short) (rand(i, mod, n2) >>> n1);

            System.out.print((s & 0xFFFF) + " ");
            if((i & 31) == 31)
                System.out.println();
            if(!sset.add(s))
            {
                //System.out.println("already contains " + s + " at index " + i);
                return;
            }
        }
        System.out.printf("success! for n1 = %d, n2 = %016X, mod = %016X\n", n1, n2, mod);
    }

}
