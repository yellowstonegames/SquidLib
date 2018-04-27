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
     * 256 2-element gradient vectors formed from the cos and sin of increasing multiples of phi, the golden ratio.
     * This starts with {@code Math.cos(1.61803398875 * 1), Math.sin(1.61803398875 * 1)} and increases the multiple up
     * to 256 at the end. This is expected to be accessed using an 8-bit int (depending on how you got your int, the
     * normal way to get 8 bits would be {@code index & 255}), but smaller numbers should also work down to about 4 bits
     * (typically using {@code index & 15}).
     */
    protected static final double[][] phiGrad2 = {
            {0.8150192046878179, 0.5794339444578966},
            {0.3285126080199264, 0.9444995851624003},
            {-0.27953103565117526, 0.9601366569961702},
            {-0.7841589327438924, 0.6205600439908774},
            {-0.9986781437763752, 0.05140005003279401},
            {-0.8437248002155627, -0.5367759881935933},
            {-0.37662568751777636, -0.9263655280189145},
            {0.22981046360408072, -0.9732354035987786},
            {0.7512255700688496, -0.6600455612112939},
            {0.9947160697132525, -0.10266421311352707},
            {0.8701998299869244, 0.49269895056791824},
            {0.4237430769975827, 0.9057824267983017},
            {-0.17948233877384698, 0.983761195650789},
            {-0.7163061830035246, 0.6977861077657832},
            {-0.9881242523951498, 0.15365696153616482},
            {-0.894374301636155, -0.44731935859387983},
            {-0.4697402116302938, -0.8828046973014625},
            {0.12867971425053623, -0.991686205984736},
            {0.6794930883661505, -0.7336819085016549},
            {0.9789201186915606, -0.20424348513698282},
            {0.9161843048116499, 0.4007571828636311},
            {0.5144954882185442, 0.857493086037877},
            {-0.07753689756495279, 0.9969894831521554},
            {-0.6408836093932414, 0.7676380652437},
            {-0.9671280016853234, 0.25429004769387453},
            {-0.9355721801365401, -0.3531355203807194},
            {-0.5578905866805359, -0.8299145096293036},
            {0.026189095618157535, -0.9996570068131884},
            {0.6005798184449439, -0.7995648076776756},
            {0.9527790763429471, -0.30366434048646623},
            {0.9524866716435015, 0.30458026912701447},
            {0.5998107828543199, 0.8001418779014681},
            {0.025227942766709196, 0.9996817248023291},
            {-0.5586882671550536, 0.8293777306772129},
            {-0.9359112770969633, 0.35223583208232967},
            {-0.9668830622808007, -0.25521979522462845},
            {-0.6401452517954772, -0.7682539011314586},
            {-0.07657828572526153, -0.997063571772322},
            {0.5153197047391562, -0.8569980174466851},
            {0.9165691975581982, -0.39987611342454643},
            {0.9787232921313124, 0.20518459357282604},
            {0.6787873607664123, 0.7343348819603821},
            {0.12772617771665648, 0.9918094693669208},
            {-0.4705887852055238, 0.8823526478901672},
            {-0.8948039726230813, 0.44645923730834813},
            {-0.987976059032004, -0.1546069428570078},
            {-0.7156349511426562, -0.6984744925214149},
            {-0.17853639842218227, -0.9839333079220541},
            {0.42461376424291053, -0.9053745916555567},
            {0.8706731432876941, -0.49186205134928496},
            {0.9946169013278476, 0.10362055584193075},
            {0.7505906084908771, 0.6607675373724824},
            {0.22887462022891217, 0.9734559097437702},
            {-0.37751618658649483, 0.926002985343563},
            {-0.8442405045259113, 0.53596452356274},
            {-0.998628262541406, -0.052360225873583494},
            {-0.7835619201046374, -0.6213137028602651},
            {-0.27860776335327647, -0.9604049740600497},
            {0.32942056468855896, -0.9441832934130274},
            {0.815575936633839, -0.5786500596939711},
            {0.9999995377871086, 9.614705243498993E-4},
            {0.8144617193170308, 0.5802172935781439},
            {0.32760434766577023, 0.9448150037920043},
            {-0.2804540495433758, 0.9598674523572105},
            {-0.7847552204864104, 0.6198058114597872},
            {-0.9987271018075193, 0.05043982667647658},
            {-0.8432083159442574, -0.5375869566148805},
            {-0.37573484028656684, -0.9267272143380857},
            {0.23074609453653327, -0.9730139977698868},
            {0.7518598371945376, -0.6593229748869694},
            {0.9948143185574757, -0.10170777547968135},
            {0.8697257122509974, 0.4935353943229353},
            {0.4228719980332324, 0.9061894246124161},
            {-0.18042811320741017, 0.9835881739653105},
            {-0.7169767526924914, 0.69709707795868},
            {-0.9882715323107604, 0.15270683817086136},
            {-0.8939438038665631, -0.44817906636586646},
            {-0.4688912038150978, -0.883255930625336},
            {0.1296331318295739, -0.9915620258622535},
            {0.6801981878249586, -0.7330282568084551},
            {0.9791160403128117, -0.20330218789319596},
            {0.9157985651207083, 0.40163788183244326},
            {0.5136707960850377, 0.8579873619403514},
            {-0.07849543772753681, 0.9969144728892054},
            {-0.6416213745416761, 0.7670215197315198},
            {-0.9673720470517861, 0.2533600650908442},
            {-0.9352322183090706, -0.3540348822315325},
            {-0.5570923904775789, -0.8304505213870221},
            {0.0271502242597271, -0.9996313647153368},
            {0.6013482988440962, -0.7989869983155617},
            {0.9530706002688482, -0.30274813113077575},
            {0.9521933864408199, 0.3054959162057056},
            {0.5990411927831463, 0.8007182084534767},
            {0.02426676659390367, 0.9997055186599086},
            {-0.5594854311637297, 0.8288401850269662},
            {-0.9362495088768715, 0.3513358181680552},
            {-0.9666372290646448, -0.2561493068236286},
            {-0.639406302430932, -0.7688690268255078},
            {-0.07561960309462855, -0.9971367386812156},
            {0.5161434448849468, -0.8565021566244302},
            {0.9169532430045485, -0.39899467432967256},
            {0.9785255608140186, 0.20612551233074072},
            {0.6780810056781368, 0.7349871765810113},
            {0.1267725231094102, 0.9919318158948598},
            {-0.4714369237563429, 0.8818997828093372},
            {-0.8952328164301424, 0.4455987033043912},
            {-0.9878269523583152, -0.15555678125521535},
            {-0.7149630577303816, -0.6991622315892235},
            {-0.1775902930268572, -0.9841045106200027},
            {0.42548405896433017, -0.9049659195611945},
            {0.8711456517157633, -0.4910246974406867},
            {0.9945168134929346, 0.10457680278082451},
            {0.7499549530475861, 0.6614889027031315},
            {0.2279385652761324, 0.973675516001023},
            {-0.37840633666951456, 0.9256395866471777},
            {-0.8447554283985693, 0.535152563472464},
            {-0.9985774581487236, -0.05332035331122841},
            {-0.782964183120542, -0.6220667873712457},
            {-0.27768423350317883, -0.9606724033008089},
            {0.3303282168323275, -0.9438661288362745},
            {0.8161319146404362, -0.5778656400110114},
            {0.9999981511488616, 0.0019229401598916565},
            {0.8139034810368312, 0.5810001063305656},
            {0.3266957844657079, 0.945129549010259},
            {-0.281376804176623, 0.9595973603920294},
            {-0.7853507827809703, 0.6190510059642236},
            {-0.9987751365895806, 0.04947955669228112},
            {-0.8426910521894434, -0.538397428076926},
            {-0.3748436457163809, -0.9270880439667272},
            {0.23168151216134933, -0.9727916924617678},
            {0.7524934092816089, -0.6585997790674858},
            {0.994911647769694, -0.10075124382454212},
            {0.8692507905181963, 0.49437138184111223},
            {0.42200052815510103, 0.906595584721664},
            {-0.18137372084857706, 0.9834142430255637},
            {-0.7176466595896562, 0.6964074037370712},
            {-0.9884178986426848, 0.15175657363942827},
            {-0.8935124797122745, -0.4490383598295609},
            {-0.46804176254479146, -0.8837063474446503},
            {0.1305864295723934, -0.9914369291142702},
            {0.6809026584910192, -0.7323739274850399},
            {0.9793110568139495, -0.20236070271163187},
            {0.9154119788419642, 0.40251820951683553},
            {0.5128456291010095, 0.8584808446971836},
            {-0.07945390532690727, 0.9968385410528141},
            {-0.6423585465587638, 0.7664042651648753},
            {-0.9676151981545856, 0.2524298482752425},
            {-0.934891391928828, -0.35493391680336583},
            {-0.5562936792840555, -0.8309857654548652},
            {0.028111327802940005, -0.9996047985324779},
            {0.6021162233413851, -0.7984084503492602},
            {0.9533612431517168, -0.30183164190689693},
            {0.9518992210060183, 0.30641128087610536},
            {0.5982710489428404, 0.8012937988009351},
            {0.023305567988262783, 0.9997283883639319},
            {-0.5602820779696569, 0.8283018731754765},
            {-0.9365868751635984, 0.350435479469882},
            {-0.9663905022641126, -0.25707858163160174},
            {-0.6386667619827179, -0.7694834417572027},
            {-0.07466085055928477, -0.997208983811199},
            {0.5169667078944292, -0.8560055040294986},
            {0.9173364407956793, -0.3981128663938346},
            {0.9783269249224673, 0.2070662405409173},
            {0.6773740237542967, 0.7356387917605443},
            {0.12581875131036094, 0.9920532454554551},
            {-0.47228462649872727, 0.8814461024776034},
            {-0.8956608326609128, 0.44473775737749144},
            {-0.9876769325119248, -0.1565064758527096},
            {-0.7142905033878327, -0.6998493243334286},
            {-0.17664402346250044, -0.9842748035863661},
            {0.4263539603573059, -0.9045564108930083},
            {0.8716173548343263, -0.4901868896162075},
            {0.994415806301039, 0.1055329530462149},
            {0.7493186043266098, 0.6622096565363733},
            {0.22700229961107984, 0.9738942221675214},
            {-0.37929613694395364, 0.9252753322656957},
            {-0.8452695713575256, 0.5343401086733665},
            {-0.998525730645293, -0.054280431458156884},
            {-0.7823657223441729, -0.6228192968276437},
            {-0.2767604469546231, -0.9609389444712277},
            {0.3312355636121689, -0.943548091725339},
            {0.8166871381936452, -0.5770806861341607},
            {0.999995840086541, 0.0028844080178108454},
            {0.8133444903632729, 0.5817823819915036},
            {0.325786919259646, 0.9454432205263883},
            {-0.28229929869789083, 0.9593263813503092},
            {-0.7859456190770135, 0.6182956282019548},
            {-0.998822248078154, 0.04851924096791483},
            {-0.8421730094292976, -0.5392074018305016},
            {-0.373952104631071, -0.9274480165712753},
            {0.23261671561379524, -0.9725684878799286},
            {0.7531262857443665, -0.6578759744213911},
            {0.9950080572599341, -0.09979461903234838},
            {0.8687750652275492, 0.49520691234964365},
            {0.42112866816879463, 0.9070009067505816},
            {-0.18231916082320707, 0.9832394029923341},
            {-0.7183159030757559, 0.6957170857384928},
            {-0.9885633512556217, 0.15080616882029174},
            {-0.893080329572006, -0.44989723819063077},
            {-0.46719188860459987, -0.8841559473430393},
            {0.1315396065977646, -0.9913109158564256},
            {0.681606499713117, -0.7317189211362739},
            {0.9795051680146997, -0.20141903046260284},
            {0.9150245463327793, 0.40339816510302995},
            {0.512019988029247, 0.858973533852196},
            {-0.08041229947705328, 0.9967616877131729},
            {-0.6430951247630602, 0.7657863021143588},
            {-0.9678574547689481, 0.2514993981069807},
            {-0.9345497013108786, -0.35583262326513526},
            {-0.5554944538383123, -0.8315202413380417},
            {0.02907240535928829, -0.9995773082891715},
            {0.6028835912268903, -0.7978291643136192},
            {0.9536510047228631, -0.3009148736620934},
            {0.9516041756110342, 0.307326362292017},
            {0.5975003520453519, 0.8018686484117461},
            {0.022344347838353967, 0.9997503338932568},
            {-0.5610782068363855, 0.8277627956203795},
            {-0.93692337564527, 0.34953481682011633},
            {-0.966142882107282, -0.2580076187895127},
            {-0.6379266311344768, -0.7700971453585691},
            {-0.0737020290055187, -0.9972803070954874},
            {0.5177894930065641, -0.8555080601210051},
            {0.9177187905773555, -0.3972306904321919},
            {0.978127384640281, 0.20800677733372933},
            {0.6766664156484392, 0.7362897268966162},
            {0.12486486320121397, 0.9921737579364525},
            {-0.47313189264902605, 0.8809916073143662},
            {-0.8960880209197166, 0.4438764003235422},
            {-0.9875259996315107, -0.15745602577159645},
            {-0.713617288736716, -0.7005357701188843},
            {-0.17569759060384005, -0.9844441866637262},
            {0.4272234676177009, -0.9041460660295477},
            {0.8720882522073408, -0.48934862865031664},
            {0.9943138798455315, 0.1064890057542367},
            {0.7486815629161868, 0.6629297982059457},
            {0.22606582409923492, 0.9741120280410943},
            {-0.3801855865872776, 0.9249102225358349},
            {-0.8457829329275041, 0.5335271599164837},
            {-0.9984730800789315, -0.055240459426869205},
            {-0.7817665383287496, -0.6235712305338369},
            {-0.2758364045615609, -0.9612045973249145},
            {0.3321426041893285, -0.9432291823742155},
            {0.8172416067802154, -0.5762951987890336},
            {0.9999926046022829, 0.003845873209323106},
            {0.8127847478130893, 0.582564119837819},
            {0.3248777528877428, 0.9457560180504329},
            {-0.2832215322544221, 0.9590545154825441},
            {-0.7865397288246715, 0.6175396788712546},
            {-0.9988684362296896, 0.04755888039109703},
            {-0.8416541881427008, -0.5400168771268647},
            {-0.37306021785478083, -0.92780713181897},
            {0.23355170402936554, -0.9723443842307002},
            {0.7537584659977772, -0.6571515616177731},
            {0.9951035469390707, -0.09883790198744995},
            {0.8682985368188393, 0.4960419850761251},
            {0.4202564188803024, 0.90740539032447}
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
