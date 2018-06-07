package squidpony.squidmath;

/**
 * This is Ken Perlin's third revision of his noise function. It is sometimes
 * referred to as "Simplex Noise". Results are bound by (-1, 1) inclusive.
 *
 *
 * It is significantly faster than his earlier versions. This particular version
 * was originally from Stefan Gustavson. This is much preferred to the earlier
 * versions of Perlin Noise due to the reasons noted in the articles:
 * <ul>
 * <li>http://www.itn.liu.se/~stegu/simplexnoise/simplexnoise.pdf</li>
 * <li>http://webstaff.itn.liu.se/~stegu/TNM022-2005/perlinnoiselinks/ch02.pdf</li>
 * </ul>
 * But, Gustavson's paper is not without its own issues, particularly for 2D noise.
 * More detail is noted here,
 * http://stackoverflow.com/questions/18885440/why-does-simplex-noise-seem-to-have-more-artifacts-than-classic-perlin-noise#21568753
 * and some changes have been made to 2D noise generation to reduce angular artifacts.
 * Specifically for the 2D gradient table, code based on Gustavson's paper used 12
 * points, with some duplicates, and not all on the unit circle. In this version,
 * points are used on the unit circle starting at (1,0) and moving along the circle
 * in increments of 1.61803398875 radians, that is, the golden ratio phi, getting the
 * sin and cosine of 15 points after the starting point and storing them as constants.
 * This definitely doesn't have a noticeable 45 degree angle artifact, though it does
 * have, to a lesser extent, some other minor artifacts.
 * <br>
 * You can also consider {@link WhirlingNoise} as an alternative, which can be faster
 * and also reduces the likelihood of angular artifacts. WhirlingNoise does not scale its
 * input (it doesn't need to), so it won't produce the same results as PerlinNoise for the
 * same inputs, but it will produce similar shape, density, and aesthetic quality of noise.
 * @see WhirlingNoise A subclass that has a faster implementation and some different qualities.
 */
public class PerlinNoise {

    protected static final double phi = 1.61803398875,
    epi = 1.0 / Math.E / Math.PI, unit1_4 =  0.70710678118, unit1_8 = 0.38268343236, unit3_8 = 0.92387953251;
    
    /**
     * 256 2-element gradient vectors formed from the cos and sin of increasing multiples of the inverse of phi, the
     * golden ratio, while also adding increasing multiples of 2/3 of the reciprocal of {@link Math#E}. This produces
     * a sequence with remarkably low overlap possible from nearby angles, distributed nicely around the unit circle.
     * For i from 1 to 256 inclusive, this gets the cosine and sine of an angle in radians of
     * {@code 0.61803398874989484820458683436563811772 * i + (i / (1.5 * 2.7182818284590452354))}. This is expected to
     * be accessed using an 8-bit int (depending on how you got your int, the normal way to get 8 bits would be
     * {@code index & 255}), but smaller numbers should also work down to about 4 bits (typically using
     * {@code index & 15}).
     */
    public static final double[][] phiGrad2 = {
            {0.6499429579167653, 0.759982994187637},
            {-0.1551483029088119, 0.9878911904175052},
            {-0.8516180517334043, 0.5241628506120981},
            {-0.9518580082090311, -0.30653928330368374},
            {-0.38568876701087174, -0.9226289476282616},
            {0.4505066120763985, -0.8927730912586049},
            {0.9712959670388622, -0.23787421973396244},
            {0.8120673355833279, 0.5835637432865366},
            {0.08429892519436613, 0.9964405106232257},
            {-0.702488350003267, 0.7116952424385647},
            {-0.9974536374007479, -0.07131788861160528},
            {-0.5940875849508908, -0.804400361391775},
            {0.2252075529515288, -0.9743108118529653},
            {0.8868317111719171, -0.4620925405802277},
            {0.9275724981153959, 0.373643226540993},
            {0.3189067150428103, 0.9477861083074618},
            {-0.5130301507665112, 0.8583705868705491},
            {-0.9857873824221494, 0.1679977281313266},
            {-0.7683809836504446, -0.6399927061806058},
            {-0.013020236219374872, -0.9999152331316848},
            {0.7514561619680513, -0.6597830223946701},
            {0.9898275175279653, 0.14227257481477412},
            {0.5352066871710182, 0.8447211386057674},
            {-0.29411988281443646, 0.9557685360657266},
            {-0.9175289804081126, 0.39766892022290273},
            {-0.8985631161871687, -0.43884430750324743},
            {-0.2505005588110731, -0.968116454790094},
            {0.5729409678802212, -0.8195966369650838},
            {0.9952584535626074, -0.09726567026534665},
            {0.7207814785200723, 0.6931623620930514},
            {-0.05832476124070039, 0.998297662136006},
            {-0.7965970142012075, 0.6045107087270838},
            {-0.977160478114496, -0.21250270589112422},
            {-0.4736001288089817, -0.8807399831914728},
            {0.36153434093875386, -0.9323587937709286},
            {0.9435535266854258, -0.3312200813348966},
            {0.8649775992346886, 0.5018104750024599},
            {0.1808186720712497, 0.9835164502083277},
            {-0.6299339540895539, 0.7766487066139361},
            {-0.9996609468975833, 0.02603826506945166},
            {-0.6695112313914258, -0.7428019325774111},
            {0.12937272671950842, -0.9915960354807594},
            {0.8376810167470904, -0.5461597881403947},
            {0.959517028911149, 0.28165061908243916},
            {0.4095816551369482, 0.9122734610714476},
            {-0.42710760401484793, 0.9042008043530463},
            {-0.9647728141412515, 0.2630844295924223},
            {-0.8269869890664444, -0.562221059650754},
            {-0.11021592552380209, -0.9939076666174438},
            {0.6837188597775012, -0.72974551782423},
            {0.998972441738333, 0.04532174585508431},
            {0.6148313475439905, 0.7886586169422362},
            {-0.1997618324529528, 0.9798444827088829},
            {-0.8744989400706802, 0.48502742583822706},
            {-0.9369870231562731, -0.3493641630687752},
            {-0.3434772946489506, -0.9391609809082988},
            {0.4905057254335028, -0.8714379687143274},
            {0.9810787787756657, -0.1936089611460388},
            {0.7847847614201463, 0.6197684069414349},
            {0.03905187955516296, 0.9992371844077906},
            {-0.7340217731995672, 0.6791259356474049},
            {-0.9931964444524306, -0.1164509455824639},
            {-0.5570202966000876, -0.830498879695542},
            {0.2691336060685578, -0.9631028512493016},
            {0.9068632806061, -0.4214249521425399},
            {0.9096851999779008, 0.4152984913783901},
            {0.27562369868737335, 0.9612656119522284},
            {-0.5514058359842319, 0.8342371389734039},
            {-0.9923883787916933, 0.12314749546456379},
            {-0.7385858406439617, -0.6741594440488484},
            {0.032311046904542805, -0.9994778618098213},
            {0.7805865154410089, -0.6250477517051506},
            {0.9823623706068018, 0.18698709264487903},
            {0.49637249435561115, 0.8681096398768929},
            {-0.3371347561867868, 0.9414564016304079},
            {-0.9346092156607797, 0.35567627697379833},
            {-0.877750600058892, -0.47911781859606817},
            {-0.20636642697019966, -0.9784747813917093},
            {0.6094977881394418, -0.7927877687333024},
            {0.998644017504346, -0.052058873429796634},
            {0.6886255051458764, 0.7251171723677399},
            {-0.10350942208147358, 0.9946284731196666},
            {-0.8231759450656516, 0.567786371327519},
            {-0.9665253951623188, -0.2565709658288005},
            {-0.43319680340129196, -0.9012993562201753},
            {0.4034189716368784, -0.9150153732716426},
            {0.9575954428121146, -0.28811624026678895},
            {0.8413458575409575, 0.5404971304259356},
            {0.13605818775026976, 0.9907008476558967},
            {-0.664485735550556, 0.7473009482463117},
            {-0.999813836664718, -0.01929487014147803},
            {-0.6351581891853917, -0.7723820781910558},
            {0.17418065221630152, -0.984713714941304},
            {0.8615731658120597, -0.5076334109892543},
            {0.945766171482902, 0.32484819358982736},
            {0.3678149601703667, 0.9298990026206456},
            {-0.4676486851245607, 0.883914423064399},
            {-0.9757048995218635, 0.2190889067228882},
            {-0.8006563717736747, -0.5991238388999518},
            {-0.06505704156910719, -0.9978815467490495},
            {0.716089639712196, -0.6980083293893113},
            {0.9958918787052943, 0.09055035024139549},
            {0.5784561871098056, 0.8157134543418942},
            {-0.24396482815448167, 0.9697840804135497},
            {-0.8955826311865743, 0.4448952131872543},
            {-0.9201904205900768, -0.39147105876968413},
            {-0.3005599364234082, -0.9537629289384008},
            {0.5294967923694863, -0.84831193960148},
            {0.9888453593035162, -0.1489458135829932},
            {0.7558893631265085, 0.6546993743025888},
            {-0.006275422246980369, 0.9999803093439501},
            {-0.764046696121276, 0.6451609459244744},
            {-0.9868981170802014, -0.16134468229090512},
            {-0.5188082666339063, -0.8548906260290385},
            {0.31250655826478446, -0.9499156020623616},
            {0.9250311403279032, -0.3798912863223621},
            {0.889928392754896, 0.45610026942404636},
            {0.2317742435145519, 0.9727696027545563},
            {-0.5886483179573486, 0.8083892365475831},
            {-0.996949901406418, 0.0780441803450664},
            {-0.707272817672466, -0.7069407057042696},
            {0.07757592706207364, -0.9969864470194466},
            {0.8081126726681943, -0.5890279350532263},
            {0.9728783545459001, 0.23131733021125322},
            {0.4565181982253288, 0.8897140746830408},
            {-0.3794567783511009, 0.9252094645881026},
            {-0.9497687200714887, 0.31295267753091066},
            {-0.8551342041690687, -0.5184066867432686},
            {-0.16180818807538452, -0.9868222283024238},
            {0.6448020194233159, -0.7643496292585048},
            {0.9999772516247822, -0.006745089543285545},
            {0.6550543261176665, 0.7555817823601425},
            {-0.14848135899860646, 0.9889152066936411},
            {-0.848063153443784, 0.5298951667745091},
            {-0.9539039899003245, -0.300111942535184},
            {-0.3919032080850608, -0.9200064540494471},
            {0.44447452934057863, -0.8957914895596358},
            {0.9696693887216105, -0.24442028675267172},
            {0.8159850520735595, 0.5780730012658526},
            {0.0910180879994953, 0.9958492394217692},
            {-0.6976719213969089, 0.7164173993520435},
            {-0.9979119924958648, -0.06458835214597858},
            {-0.5994998228898376, -0.8003748886334786},
            {0.2186306161766729, -0.9758076929755208},
            {0.8836946816279001, -0.46806378802740584},
            {0.9300716543684309, 0.36737816720699407},
            {0.32529236260160294, 0.9456134933645286},
            {-0.5072286936943775, 0.8618114946396893},
            {-0.9846317976415725, 0.17464313062106204},
            {-0.7726803123417516, -0.6347953488483143},
            {-0.019764457813331488, -0.9998046640256011},
            {0.7469887719961158, -0.6648366525032559},
            {0.9907646418168752, 0.13559286310672486},
            {0.5408922318074902, 0.8410919055432124},
            {-0.2876664477065717, 0.9577306588304888},
            {-0.9148257956391065, 0.40384868903250853},
            {-0.9015027194859215, -0.4327734358292892},
            {-0.2570248925062563, -0.9664047830139022},
            {0.5673996816983953, -0.8234425306046317},
            {0.9945797473944409, -0.10397656501736473},
            {0.7254405241129018, 0.6882848581617921},
            {-0.05158982732517303, 0.9986683582233687},
            {-0.7925014140531963, 0.609870075281354},
            {-0.9785715990807187, -0.20590683687679034},
            {-0.47953002522651733, -0.8775254725113429},
            {0.35523727306945746, -0.9347761656258549},
            {0.9412979532686209, -0.33757689964259285},
            {0.868342678987353, 0.4959647082697184},
            {0.18744846526420056, 0.9822744386728669},
            {-0.6246810590458048, 0.7808800000444446},
            {-0.9994625758058275, 0.03278047534097766},
            {-0.674506266646887, -0.738269121834361},
            {0.12268137965007223, -0.9924461089082646},
            {0.8339780641890598, -0.5517975973592748},
            {0.9613949601033843, 0.2751721837101493},
            {0.41572570400265835, 0.9094900433932711},
            {-0.42099897262033487, 0.907061114287578},
            {-0.9629763390922247, 0.2695859238694348},
            {-0.8307604078465821, -0.5566301687427484},
            {-0.11691741449967302, -0.9931416405461567},
            {0.6787811074228051, -0.7343406622310046},
            {0.999255415972447, 0.03858255628819732},
            {0.6201369341201711, 0.7844935837468874},
            {-0.19314814942146824, 0.9811696042861612},
            {-0.8712074932224428, 0.4909149659086258},
            {-0.9393222007870077, -0.34303615422962713},
            {-0.3498042060103595, -0.9368228314134226},
            {0.4846166400948296, -0.8747266499559725},
            {0.9797505510481769, -0.20022202106859724},
            {0.7889473022428521, 0.6144608647291752},
            {0.045790935472179155, 0.9989510449609544},
            {-0.7294243101497431, 0.684061529222753},
            {-0.9939593229024027, -0.10974909756074072},
            {-0.562609414602539, -0.8267228354174018},
            {0.26263126874523307, -0.9648962724963078},
            {0.9040001019019392, -0.4275322394408211},
            {0.9124657316291773, 0.4091531358824348},
            {0.28210125132356934, 0.9593846381935018},
            {-0.5457662881946498, 0.8379374431723614},
            {-0.9915351626845509, 0.12983844253579577},
            {-0.7431163048326799, -0.6691622803863227},
            {0.02556874420628532, -0.9996730662170076},
            {0.7763527553119807, -0.6302986588273021},
            {0.9836012681423212, 0.1803567168386515},
            {0.5022166799422209, 0.8647418148718223},
            {-0.330776879188771, 0.9437089891455613},
            {-0.9321888864830543, 0.3619722087639923},
            {-0.8809623252471085, -0.47318641305008735},
            {-0.21296163248563432, -0.9770605626515961},
            {0.604136498566135, -0.7968808512571063},
            {0.9982701582127194, -0.05879363249495786},
            {0.6935008202914851, 0.7204558364362367},
            {-0.09679820929680796, 0.9953040272584711},
            {-0.8193274492343137, 0.5733258505694586},
            {-0.9682340024187017, -0.25004582891994304},
            {-0.4392662937408502, -0.8983569018954422},
            {0.39723793388455464, -0.9177156552457467},
            {0.9556302892322005, -0.2945687530984589},
            {0.8449724198323217, 0.5348098818484104},
            {0.14273745857559722, 0.9897605861618151},
            {-0.6594300077680133, 0.7517659641504648},
            {-0.9999212381512442, -0.01255059735959867},
            {-0.6403535266476091, -0.768080308893523},
            {0.16753470770767478, -0.9858661784001437},
            {0.8581295336101056, -0.5134332513054668},
            {0.9479357869928937, 0.31846152630759517},
            {0.37407884501651706, 0.9273969040875156},
            {-0.461675964944643, 0.8870486477034012},
            {-0.9742049295269273, 0.22566513972130173},
            {-0.8046793020829978, -0.5937097108850584},
            {-0.07178636201352963, -0.9974200309943962},
            {0.7113652211526822, -0.7028225395748172},
            {0.9964799940037152, 0.08383091047075403},
            {0.5839450884626246, 0.8117931594072332},
            {-0.23741799789097484, 0.9714075840127259},
            {-0.8925614000865144, 0.45092587758477687},
            {-0.9228099950981292, -0.38525538665538556},
            {-0.30698631553196837, -0.95171392869712},
            {0.5237628071845146, -0.8518641451605984},
            {0.9878182118285335, -0.15561227580071732},
            {0.7602881737752754, 0.6495859395164404},
            {4.6967723669845613E-4, 0.9999998897016406},
            {-0.7596776469502666, 0.6502998329417794},
            {-0.9879639510809196, -0.15468429579171308},
            {-0.5245627784110601, -0.8513717704420726},
            {0.3060921834538644, -0.9520018777441807},
            {0.9224476966294768, -0.3861220622846781},
            {0.8929845854878761, 0.45008724718774934},
            {0.23833038910266038, 0.9711841358002995},
            {-0.5831822693781987, 0.8123413326200348},
            {-0.9964008074312266, 0.0847669213219385},
            {-0.712025106726807, -0.7021540054650968},
            {0.07084939947717452, -0.9974870237721009},
            {0.8041212432524677, -0.5944653279629567},
            {0.9744164792492415, 0.22474991650168097},
            {0.462509014279733, 0.8866145790082576},
    };
    protected static final int[][] grad3 = {{1, 1, 0}, {-1, 1, 0}, {1, -1, 0},
            {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1},
            {1, 0, -1}, {-1, 0, -1}, {0, 1, 1},
            {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
    protected static final int[][] grad4 = {{0, 1, 1, 1}, {0, 1, 1, -1},
    {0, 1, -1, 1}, {0, 1, -1, -1},
    {0, -1, 1, 1}, {0, -1, 1, -1},
    {0, -1, -1, 1}, {0, -1, -1, -1},
    {1, 0, 1, 1}, {1, 0, 1, -1},
    {1, 0, -1, 1}, {1, 0, -1, -1},
    {-1, 0, 1, 1}, {-1, 0, 1, -1},
    {-1, 0, -1, 1}, {-1, 0, -1, -1},
    {1, 1, 0, 1}, {1, 1, 0, -1},
    {1, -1, 0, 1}, {1, -1, 0, -1},
    {-1, 1, 0, 1}, {-1, 1, 0, -1},
    {-1, -1, 0, 1}, {-1, -1, 0, -1},
    {1, 1, 1, 0}, {1, 1, -1, 0},
    {1, -1, 1, 0}, {1, -1, -1, 0},
    {-1, 1, 1, 0}, {-1, 1, -1, 0},
    {-1, -1, 1, 0}, {-1, -1, -1, 0}};
    private static final int p[] = {151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96,
        53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142,
        8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247,
        120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203,
        117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56,
        87, 174, 20, 125, 136, 171, 168, 68, 175, 74,
        165, 71, 134, 139, 48, 27, 166, 77, 146, 158,
        231, 83, 111, 229, 122, 60, 211, 133, 230, 220,
        105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54,
        65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132,
        187, 208, 89, 18, 169, 200, 196, 135, 130, 116,
        188, 159, 86, 164, 100, 109, 198, 173, 186, 3,
        64, 52, 217, 226, 250, 124, 123, 5, 202, 38, 147,
        118, 126, 255, 82, 85, 212, 207, 206, 59, 227,
        47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170,
        213, 119, 248, 152, 2, 44, 154, 163, 70, 221,
        153, 101, 155, 167, 43, 172, 9, 129, 22, 39, 253,
        19, 98, 108, 110, 79, 113, 224, 232, 178, 185,
        112, 104, 218, 246, 97, 228, 251, 34, 242, 193,
        238, 210, 144, 12, 191, 179, 162, 241, 81, 51,
        145, 235, 249, 14, 239, 107, 49, 192, 214, 31,
        181, 199, 106, 157, 184, 84, 204, 176, 115, 121,
        50, 45, 127, 4, 150, 254, 138, 236, 205, 93, 222,
        114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66,
        215, 61, 156, 180};
    protected static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
    protected static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;
    protected static final double F3 = 1.0 / 3.0;
    protected static final double G3 = 1.0 / 6.0;
    protected static final double F4 = (Math.sqrt(5.0) - 1.0) / 4.0;
    protected static final double G4 = (5.0 - Math.sqrt(5.0)) / 20.0;
    // To remove the need for index wrapping, double the permutation table
    // length
    protected static final int perm[] = new int[512];

    static {
        for (int i = 0; i < 512; i++) {
            perm[i] = p[i & 255];
        }
    }
    protected PerlinNoise()
    {

    }
    // A lookup table to traverse the simplex around a given point in 4D.
    // Details can be found where this table is used, in the 4D noise method.
    protected static final int simplex[][]
            = {{0, 1, 2, 3}, {0, 1, 3, 2}, {0, 0, 0, 0}, {0, 2, 3, 1},
            {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {1, 2, 3, 0},
            {0, 2, 1, 3}, {0, 0, 0, 0}, {0, 3, 1, 2}, {0, 3, 2, 1},
            {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {1, 3, 2, 0},
            {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0},
            {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0},
            {1, 2, 0, 3}, {0, 0, 0, 0}, {1, 3, 0, 2}, {0, 0, 0, 0},
            {0, 0, 0, 0}, {0, 0, 0, 0}, {2, 3, 0, 1}, {2, 3, 1, 0},
            {1, 0, 2, 3}, {1, 0, 3, 2}, {0, 0, 0, 0}, {0, 0, 0, 0},
            {0, 0, 0, 0}, {2, 0, 3, 1}, {0, 0, 0, 0}, {2, 1, 3, 0},
            {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0},
            {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0},
            {2, 0, 1, 3}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0},
            {3, 0, 1, 2}, {3, 0, 2, 1}, {0, 0, 0, 0}, {3, 1, 2, 0},
            {2, 1, 0, 3}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0},
            {3, 1, 0, 2}, {0, 0, 0, 0}, {3, 2, 0, 1}, {3, 2, 1, 0}};

    protected static double dot(double g[], double x, double y) {
        return g[0] * x + g[1] * y;
    }

    protected static double dot(int g[], double x, double y, double z) {
        return g[0] * x + g[1] * y + g[2] * z;
    }

    protected static double dot(int g[], double x, double y, double z, double w) {
        return g[0] * x + g[1] * y + g[2] * z + g[3] * w;
    }

    /**
     * 2D simplex noise.
     * This doesn't use its parameters verbatim; xin and yin are both effectively divided by
     * ({@link Math#E} * {@link Math#PI}), because without a step like that, any integer parameters would return 0 and
     * only doubles with a decimal component would produce actual noise. This step allows integers to be passed in a
     * arguments, and changes the cycle at which 0 is repeated to multiples of (E*PI).
     *
     * @param xin x input; works well if between 0.0 and 1.0, but anything is accepted
     * @param yin y input; works well if between 0.0 and 1.0, but anything is accepted
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(double xin, double yin) {
        xin *= epi;
        yin *= epi;
        double noise0, noise1, noise2; // from the three corners
        // Skew the input space to determine which simplex cell we're in
        double skew = (xin + yin) * F2; // Hairy factor for 2D
        int i = (int) Math.floor(xin + skew);
        int j = (int) Math.floor(yin + skew);
        double t = (i + j) * G2;
        double X0 = i - t; // Unskew the cell origin back to (x,y) space
        double Y0 = j - t;
        double x0 = xin - X0; // The x,y distances from the cell origin
        double y0 = yin - Y0;
        // For the 2D case, the simplex shape is an equilateral triangle.
        // Determine which simplex we are in.
        int i1, j1; // Offsets for second (middle) corner of simplex in (i,j)
        // coords
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } // lower triangle, XY order: (0,0)->(1,0)->(1,1)
        else {
            i1 = 0;
            j1 = 1;
        } // upper triangle, YX order: (0,0)->(0,1)->(1,1)
        // A step of (1,0) in (i,j) means a step of (1-c,-c) in (x,y), and
        // a step of (0,1) in (i,j) means a step of (-c,1-c) in (x,y),
        // where
        // c = (3-sqrt(3))/6
        double x1 = x0 - i1 + G2; // Offsets for middle corner in (x,y)
        // unskewed coords
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2; // Offsets for last corner in (x,y)
        // unskewed coords
        double y2 = y0 - 1.0 + 2.0 * G2;
        // Work out the hashed gradient indices of the three simplex corners
        int ii = i & 255;
        int jj = j & 255;
        int gi0 = perm[ii + perm[jj]];
        int gi1 = perm[ii + i1 + perm[jj + j1]];
        int gi2 = perm[ii + 1 + perm[jj + 1]];
        // Calculate the contribution from the three corners
        double t0 = 0.5 - x0 * x0 - y0 * y0;
        if (t0 < 0) {
            noise0 = 0.0;
        } else {
            t0 *= t0;
            noise0 = t0 * t0 * dot(phiGrad2[gi0], x0, y0); // (x,y) of grad3 used
            // for 2D gradient
        }
        double t1 = 0.5 - x1 * x1 - y1 * y1;
        if (t1 < 0) {
            noise1 = 0.0;
        } else {
            t1 *= t1;
            noise1 = t1 * t1 * dot(phiGrad2[gi1], x1, y1);
        }
        double t2 = 0.5 - x2 * x2 - y2 * y2;
        if (t2 < 0) {
            noise2 = 0.0;
        } else {
            t2 *= t2;
            noise2 = t2 * t2 * dot(phiGrad2[gi2], x2, y2);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to return values in the interval [-1,1].
        return 70.0 * (noise0 + noise1 + noise2);
    }

    /**
     * 3D simplex noise.
     *
     * @param xin X input
     * @param yin Y input
     * @param zin Z input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(double xin, double yin, double zin) {
        xin *= epi;
        yin *= epi;
        zin *= epi;
        double n0, n1, n2, n3; // Noise contributions from the four corners
        // Skew the input space to determine which simplex cell we're in
        double s = (xin + yin + zin) * F3; // Very nice and simple skew
        // factor for 3D
        int i = (int) Math.floor(xin + s);
        int j = (int) Math.floor(yin + s);
        int k = (int) Math.floor(zin + s);
        double t = (i + j + k) * G3;
        double X0 = i - t; // Unskew the cell origin back to (x,y,z) space
        double Y0 = j - t;
        double Z0 = k - t;
        double x0 = xin - X0; // The x,y,z distances from the cell origin
        double y0 = yin - Y0;
        double z0 = zin - Z0;
        // For the 3D case, the simplex shape is a slightly irregular
        // tetrahedron.
        // Determine which simplex we are in.
        int i1, j1, k1; // Offsets for second corner of simplex in (i,j,k)
        // coords
        int i2, j2, k2; // Offsets for third corner of simplex in (i,j,k)
        // coords
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } // X Y Z order
            else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } // X Z Y order
            else {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } // Z X Y order
        } else { // x0<y0
            if (y0 < z0) {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } // Z Y X order
            else if (x0 < z0) {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } // Y Z X order
            else {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } // Y X Z order
        }
        // A step of (1,0,0) in (i,j,k) means a step of (1-c,-c,-c) in
        // (x,y,z),
        // a step of (0,1,0) in (i,j,k) means a step of (-c,1-c,-c) in
        // (x,y,z), and
        // a step of (0,0,1) in (i,j,k) means a step of (-c,-c,1-c) in
        // (x,y,z), where
        // c = 1/6.
        double x1 = x0 - i1 + G3; // Offsets for second corner in (x,y,z)
        // coords
        double y1 = y0 - j1 + G3;
        double z1 = z0 - k1 + G3;
        double x2 = x0 - i2 + 2.0 * G3; // Offsets for third corner in
        // (x,y,z) coords
        double y2 = y0 - j2 + 2.0 * G3;
        double z2 = z0 - k2 + 2.0 * G3;
        double x3 = x0 - 1.0 + 3.0 * G3; // Offsets for last corner in
        // (x,y,z) coords
        double y3 = y0 - 1.0 + 3.0 * G3;
        double z3 = z0 - 1.0 + 3.0 * G3;
        // Work out the hashed gradient indices of the four simplex corners
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;
        int gi0 = perm[ii + perm[jj + perm[kk]]] % 12;
        int gi1 = perm[ii + i1 + perm[jj + j1 + perm[kk + k1]]] % 12;
        int gi2 = perm[ii + i2 + perm[jj + j2 + perm[kk + k2]]] % 12;
        int gi3 = perm[ii + 1 + perm[jj + 1 + perm[kk + 1]]] % 12;
        // Calculate the contribution from the four corners
        double t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad3[gi0], x0, y0, z0);
        }
        double t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad3[gi1], x1, y1, z1);
        }
        double t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad3[gi2], x2, y2, z2);
        }
        double t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 < 0) {
            n3 = 0.0;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dot(grad3[gi3], x3, y3, z3);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to stay just inside [-1,1]
        return 32.0 * (n0 + n1 + n2 + n3);
    }

    /**
     * 4D simplex noise.
     *
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @param w Fourth-dimensional position. It is I, the Fourth-Dimensional Ziltoid the Omniscient!
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(double x, double y, double z, double w) {
        x *= epi;
        y *= epi;
        z *= epi;
        w *= epi;
        // The skewing and unskewing factors are hairy again for the 4D case
        double n0, n1, n2, n3, n4; // Noise contributions from the five
        // corners
        // Skew the (x,y,z,w) space to determine which cell of 24 simplices
        // we're in
        double s = (x + y + z + w) * F4; // Factor for 4D skewing
        int i = (int) Math.floor(x + s);
        int j = (int) Math.floor(y + s);
        int k = (int) Math.floor(z + s);
        int l = (int) Math.floor(w + s);
        double t = (i + j + k + l) * G4; // Factor for 4D unskewing
        double X0 = i - t; // Unskew the cell origin back to (x,y,z,w) space
        double Y0 = j - t;
        double Z0 = k - t;
        double W0 = l - t;
        double x0 = x - X0; // The x,y,z,w distances from the cell origin
        double y0 = y - Y0;
        double z0 = z - Z0;
        double w0 = w - W0;
        // For the 4D case, the simplex is a 4D shape I won't even try to
        // describe.
        // To find out which of the 24 possible simplices we're in, we need
        // to
        // determine the magnitude ordering of x0, y0, z0 and w0.
        // The method below is a good way of finding the ordering of x,y,z,w
        // and
        // then find the correct traversal order for the simplex we’re in.
        // First, six pair-wise comparisons are performed between each
        // possible pair
        // of the four coordinates, and the results are used to add up binary
        // bits
        // for an integer index.
        int c =
                (x0 > y0 ? 32 : 0)
              + (x0 > z0 ? 16 : 0)
              + (y0 > z0 ? 8 : 0 )
              + (x0 > w0 ? 4 : 0 )
              + (y0 > w0 ? 2 : 0 )
              + (z0 > w0 ? 1 : 0 );
        int i1, j1, k1, l1; // The integer offsets for the second simplex
        // corner
        int i2, j2, k2, l2; // The integer offsets for the third simplex
        // corner
        int i3, j3, k3, l3; // The integer offsets for the fourth simplex
        // corner
        // simplex[c] is a 4-vector with the numbers 0, 1, 2 and 3 in some
        // order.
        // Many values of c will never occur, since e.g. x>y>z>w makes x<z,
        // y<w and x<w
        // impossible. Only the 24 indices which have non-zero entries make
        // any sense.
        // We use a thresholding to set the coordinates in turn from the
        // largest magnitude.
        // The number 3 in the "simplex" array is at the position of the
        // largest coordinate.
        i1 = simplex[c][0] >= 3 ? 1 : 0;
        j1 = simplex[c][1] >= 3 ? 1 : 0;
        k1 = simplex[c][2] >= 3 ? 1 : 0;
        l1 = simplex[c][3] >= 3 ? 1 : 0;
        // The number 2 in the "simplex" array is at the second largest
        // coordinate.
        i2 = simplex[c][0] >= 2 ? 1 : 0;
        j2 = simplex[c][1] >= 2 ? 1 : 0;
        k2 = simplex[c][2] >= 2 ? 1 : 0;
        l2 = simplex[c][3] >= 2 ? 1 : 0;
        // The number 1 in the "simplex" array is at the second smallest
        // coordinate.
        i3 = simplex[c][0] >= 1 ? 1 : 0;
        j3 = simplex[c][1] >= 1 ? 1 : 0;
        k3 = simplex[c][2] >= 1 ? 1 : 0;
        l3 = simplex[c][3] >= 1 ? 1 : 0;
        // The fifth corner has all coordinate offsets = 1, so no need to
        // look that up.
        double x1 = x0 - i1 + G4; // Offsets for second corner in (x,y,z,w)
        // coords
        double y1 = y0 - j1 + G4;
        double z1 = z0 - k1 + G4;
        double w1 = w0 - l1 + G4;
        double x2 = x0 - i2 + 2.0 * G4; // Offsets for third corner in
        // (x,y,z,w) coords
        double y2 = y0 - j2 + 2.0 * G4;
        double z2 = z0 - k2 + 2.0 * G4;
        double w2 = w0 - l2 + 2.0 * G4;
        double x3 = x0 - i3 + 3.0 * G4; // Offsets for fourth corner in
        // (x,y,z,w) coords
        double y3 = y0 - j3 + 3.0 * G4;
        double z3 = z0 - k3 + 3.0 * G4;
        double w3 = w0 - l3 + 3.0 * G4;
        double x4 = x0 - 1.0 + 4.0 * G4; // Offsets for last corner in
        // (x,y,z,w) coords
        double y4 = y0 - 1.0 + 4.0 * G4;
        double z4 = z0 - 1.0 + 4.0 * G4;
        double w4 = w0 - 1.0 + 4.0 * G4;
        // Work out the hashed gradient indices of the five simplex corners
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;
        int ll = l & 255;
        int gi0 = perm[ii + perm[jj + perm[kk + perm[ll]]]] & 31;
        int gi1
                = perm[ii + i1 + perm[jj + j1 + perm[kk + k1 + perm[ll + l1]]]] & 31;
        int gi2
                = perm[ii + i2 + perm[jj + j2 + perm[kk + k2 + perm[ll + l2]]]] & 31;
        int gi3
                = perm[ii + i3 + perm[jj + j3 + perm[kk + k3 + perm[ll + l3]]]] & 31;
        int gi4
                = perm[ii + 1 + perm[jj + 1 + perm[kk + 1 + perm[ll + 1]]]] & 31;
        // Calculate the contribution from the five corners
        double t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad4[gi0], x0, y0, z0, w0);
        }
        double t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad4[gi1], x1, y1, z1, w1);
        }
        double t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad4[gi2], x2, y2, z2, w2);
        }
        double t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t3 < 0) {
            n3 = 0.0;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dot(grad4[gi3], x3, y3, z3, w3);
        }
        double t4 = 0.6 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t4 < 0) {
            n4 = 0.0;
        } else {
            t4 *= t4;
            n4 = t4 * t4 * dot(grad4[gi4], x4, y4, z4, w4);
        }
        // Sum up and scale the result to cover the range [-1,1]
        return 27.0 * (n0 + n1 + n2 + n3 + n4);
    }
}
