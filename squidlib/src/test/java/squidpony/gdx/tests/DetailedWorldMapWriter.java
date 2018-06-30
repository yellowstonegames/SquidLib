package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.FakeLanguageGen;
import squidpony.squidgrid.gui.gdx.PNG8;
import squidpony.squidgrid.gui.gdx.PaletteReducer;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.*;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Port of Zachary Carter's world generation technique, https://github.com/zacharycarter/mapgen
 * It seems to mostly work now, though it only generates one view of the map that it renders (but biome, moisture, heat,
 * and height maps can all be requested from it).
 */
public class DetailedWorldMapWriter extends ApplicationAdapter {
    public static final int
        Desert                 = 0 ,
        Savanna                = 1 ,
        TropicalRainforest     = 2 ,
        Grassland              = 3 ,
        Woodland               = 4 ,
        SeasonalForest         = 5 ,
        TemperateRainforest    = 6 ,
        BorealForest           = 7 ,
        Tundra                 = 8 ,
        Ice                    = 9 ,
        Beach                  = 10,
        Rocky                  = 11,
        River                  = 12,
        Ocean                  = 13,
        Empty                  = 14;

    //private static final int width = 1920, height = 1080;
    private static final int width = 1024, height = 512; // elliptical, roundSide
    //private static final int width = 512, height = 256; // mimic, elliptical
    //private static final int width = 1000, height = 1000; // space view
    //private static final int width = 256, height = 128;
    //private static final int width = 314 * 4, height = 400;
    //private static final int width = 512, height = 512;

    private SpriteBatch batch;
    //private SquidPanel display;//, overlay;
//    private FakeLanguageGen lang = FakeLanguageGen.randomLanguage(-1234567890L).removeAccents()
//            .mix(FakeLanguageGen.SIMPLISH, 0.6);
    private FakeLanguageGen lang = FakeLanguageGen.mixAll(FakeLanguageGen.SIMPLISH, 6.0, FakeLanguageGen.FANTASY_NAME, 5.0, FakeLanguageGen.JAPANESE_ROMANIZED, 2.0);
    //private GreasedRegion earth = GreasedRegion.deserializeFromString("256,128,-1,-2,72057594037927935,-256,9007199254740991,-4096,2251799813685247,-16384,562949953421311,-65536,140737488355327,-262144,35184372088831,-1048576,8796093022207,-2097152,4398046511103,-8388608,2199023255551,-16777216,1099511627775,-33554432,274877906943,-67108864,137438953471,-134217728,68719476735,-536870912,34359738367,-1073741824,17179869183,-2147483648,8589934591,-2147483648,4294967295,-4294967296,4294967295,-8589934592,2147483647,-17179869184,1073741823,-34359738368,536870911,-68719476736,268435455,-68719476736,268435455,-137438953472,134217727,-274877906944,67108863,-274877906944,67108863,-549755813888,33554431,-1099511627776,16777215,-1099511627776,16777215,-2199023255552,8388607,-4398046511104,8388607,-4398046511104,4194303,-8796093022208,2097151,-8796093022208,2097151,-17592186044416,1048575,-17592186044416,1048575,-35184372088832,524287,-35184372088832,524287,-70368744177664,262143,-70368744177664,262143,-140737488355328,131071,-140737488355328,131071,-281474976710656,65535,-281474976710656,65535,-562949953421312,65535,-562949953421312,32767,-1125899906842624,32767,-1125899906842624,16383,-1125899906842624,16383,-2251799813685248,16383,-2251799813685248,8191,-4503599627370496,8191,-4503599627370496,1404454309887,-4503599627370496,136365314047,-9007199254740992,961535905791,-9007199254740992,8795824646143,-9007199254740992,281474842544127,-18014398509481984,562949886337023,-18014398509481984,1125899873305599,-18014398509481984,1125899898483711,-36028797018963968,2251799801132031,-36028797018963968,862017115156991,-36028797018963968,567347999470079,-72057594037927936,4223224162286079,-72057594037927936,4223224162286079,-72057594037927936,8867011522198783,-72057594037927936,7248530406112511,-144115188075855872,69876712724233471,-144115188075855872,51791945470573823,-144115188075855872,274877906559,-144115188075855872,144115462953762431,-288230376151711694,-9223371761976869249,-288230376151711617,-4467552963287581057,-288230376151711233,-144111339785159105,-288230376151709697,-72057319160021441,-576460752303415297,-35958290835833281,-576460752303407105,-18014329790006209,-576460752303407105,-54043161168708577,-576460752303390721,-17873626661389281,-1152921504606781441,-36028779839095777,-1152921504606715905,-36028792723997665,-1152921504602652673,-72057591890445281,-1152921504472629249,-36028794871481329,-1152921500311879681,-72057592964383729,-2305842974853955585,-144115187539477489,-2305842974853955585,-144115187542623729,-2305842734335787009,-576460752037070321,-2017611533550354433,-576460752170238201,-2017608235015471105,-576460752186499193,-1441143084665536513,-1152921504540255481,-3746978156779667457,-1152921504548381433,-3746984985777668099,-9223372036779341817,-3746994882456059908,4169479,-1441151879684816898,4178503,-288230375614840834,4178435,-2594073385298296834,4064771,-2594073385361211396,29229059,-7061644215714840584,10039555,-4755801206502195208,4355,-4755801206502195208,4483,-4755801206502981640,1987,-4899916394579097616,3907,-4890909195324357664,835,-4899916394579099456,1,-4863887597560135680,33,-828662331436171264,33,8286623314361712640,30721,8574853690513424384,30737,8502796096475496448,15361,6917529027641081856,7721,6917529027641081856,4073,6917529027641081856,4073,6917529027641081856,4081,-2305843009213693952,2033,-4611686018427387904,2033,-4611686018427387904,1017,-3458764513820540928,17697739160683513,-3458764513820540928,36020000925942777,-4611686018427387904,144112989052604664,-4611686018427387904,288229276640088440,-1729382256910270464,576459652791799928,-576460752303423488,1152921367167893528,-576460752303423488,1152921444208869384,-144115188075855872,1152921478570180608,-144115188075855872,1152921495748476928,-144115188075855872,1152921478571098112,-72057594037927936,1152921491473956864,-72057594037927936,576460736191004672,-72057594037927936,576460744246165504,-72057594037927936,576460743977730048,-72057594037927936,576460743979941904,-72057594037927936,1152921496150196240,-72057594037927936,1152921496150142976,-72057594037927936,1152921496150341632,-72057594037927936,-6917529095891584000,-72057594037927929,-68653408768,-72057594037927921,-136768909824,-72057594037669825,-274609967615,-72057594029539329,-274341266431,-36028796985409537,-273804198911,-36028795945222145,-133144022015,-36028795945222145,-67645742079,-36028796482093057,-136365217791,-36028796482093057,-135559913471,9205357638882164735,-267328162815,9205357638882164735,-267328161791,9205357638613729279,-266757736447,9205357638479511551,-266791289855,9214364837667143679,-266791289855,9214364837633589247,-1366739125247,-9007199250546689,-7697386701823,-18014398505287681,-32986154140415,-18014398509350913,-61572651155965,-54043195528380417,-545357767377917,-72057594037895169,-2181431136616509,-36028797018931201,-4362862642335037,-4647714815446348281,-3940650714137661,-4647714815446351869,-13510802908643389,-4629700416932937728,9196350431037489091,-4629700416932708352,4584662488464752579,-18014398508449792,2274313959236435907,-2323857407722921984,1121393008680369927,-2323857407723147264,128337196217270151,-2323857407723175936,1110506744053511,-2323857407723175936,560750930165639,-3476778912330022912,549755813887751,-3467771713075281920,268280837177095,-1161928703861587968,39582418599695,-1161928703861587968,4398046510863,-1161928703861587968,4398046510863,-1161928703861587968,4398046510863,-1747396655419752448,4398046510607,-1738389456165011456,8796093021711,-585467951558164480,17592186043935,-594475150812905472,17592186043935,-882705526964617216,70368744177439,-882705526964617216,70368744177439,-882705526964617216,281474976710431,-297237575406452736,1125899906842175,-306244774661193728,4503599627370047,-441352763482308608,18014398509481535,-441352763482308608,144115188075855423,-153122387330596864,72057594037927551,-153122387330596864,1125899906842239,-162129586585337856,432908514180988543,-252201579132747776,140737488354431,-144115188075855872,35184372088063,-144115188075855872,35184372088063,-144115188075855872,17592186043647,-144115188075855872,35184372088063,-72057594037927936,70368744177151,-72057594037927936,140737488354815,-72057594037927936,562949953420799,-72057594037927936,1125899906842623,-36028797018963968,562949953224703,-36028797018963968,1155173304419548159,-36028797018963968,3467771713074297855,-18014398509481984,7066147815343308799,-18014398509481984,-8926134461449357313,-18014398509481983,-5170132375443589121,-9007199254740985,4647222226718851071,-9007198717870065,71002060723646463,-9007198449434596,33988070143557631,-4503598587183104,9042317046603775,-4503599092596672,35049315901439,-2251799277862912,-9223354715117043713,-2251799277338623,-4611677763499442177,-2251799545773945,-4611686018426847233,-1125899638669177,-1152912708512743425,-1125899638669305,-576460752169172993,-562949685247993,-2882022269226254337,-562949819334655,2814766947041279,-562949819269120,27917418495,-281474708307962,30064902143,-281440080134130,131071,-140695075569664,432345564227829759,-140728898428928,262143,-70360154251264,524287,-70360154251264,524287,-35180077125632,1048575,-35180077123584,1048575,-17590038562816,2097151,-17591112312830,4194303,-8795556182008,4194303,-8520678309880,8388607,-4397778141156,8388607,-2061450100676,16777215,-2198956150536,33554431,-1099478081288,33554431,-1099504287624,67108863,-549755813776,67108863,-274877906848,134217727,-274877906688,268435455,-137438953472,536870911,-60129542112,536870911,-34359738368,1073741823,-32212254720,2147483647,-17179869184,4294967295,-8589934592,8589934591,-4294967296,17179869183,-2147221504,17179869183,-1073741824,34359738367,-536870912,68719476735,-268435456,274877906943,-134217728,549755813887,-67108864,1099511627775,-33554432,2199023255551,-16777216,4398046511103,-4194304,17592186044415,-2097152,35184372088831,-524288,140737488355327,-262144,562949953421311,-65536,2251799813685247,-16384,18014398509481983,-2048,288230376151711743,-128"),
    //greasedWorld = new GreasedRegion(width, height);
    //private final int voidCount = 7036;
    //private double earthCount, worldCount, intersectionCount;
    //private long[] earthHash = new long[4], worldHash = new long[4];
    //private int[] workingHash = new int[256];
    private Pixmap pm;
    private Texture pt;
    private int counter = 0;
//    private Color tempColor = Color.WHITE.cpy();
    private static final int cellWidth = 1, cellHeight = 1;
    private SquidInput input;
    //private Stage stage;
    private Viewport view;
    private StatefulRNG rng;
    private long seed;
    private long ttg = 0; // time to generate
    private WorldMapGenerator world;
    //private WorldMapGenerator.DetailedBiomeMapper dbm;

    private PNG8 writer;
    
    // Biome map colors
    private static float baseIce = SColor.ALICE_BLUE.toFloatBits();
    private static float ice = baseIce;
    private static float lightIce = SColor.FLOAT_WHITE;
    private static float desert = SColor.floatGetI(248, 229, 180);
    private static float savanna = SColor.floatGetI(181, 200, 100);
    private static float tropicalRainforest = SColor.floatGetI(66, 123, 25);
    private static float tundra = SColor.floatGetI(151, 175, 159);
    private static float temperateRainforest = SColor.floatGetI(54, 113, 60);
    private static float grassland = SColor.floatGetI(169, 185, 105);
    private static float seasonalForest = SColor.floatGetI(100, 158, 75);
    private static float borealForest = SColor.floatGetI(75, 105, 45);
    private static float woodland = SColor.floatGetI(122, 170, 90);
    private static float rocky = SColor.floatGetI(171, 175, 145);
    private static float beach = SColor.floatGetI(255, 235, 180);
    private static float emptyColor = SColor.DB_INK.toFloatBits();

    // water colors
    private static float baseDeepColor = SColor.floatGetI(0, 42, 88);
    private static float baseShallowColor = SColor.floatGetI(0, 73, 137);
    private static float baseCoastalColor = SColor.lerpFloatColors(baseShallowColor, SColor.FLOAT_WHITE, 0.3f);
    private static float baseFoamColor = SColor.floatGetI(61,  162, 215);

    private static float deepColor = baseDeepColor;
    private static float shallowColor = baseShallowColor;
    private static float coastalColor = baseCoastalColor;
    private static float foamColor = baseFoamColor;
    
    private static float desertAlt = SColor.floatGetI(253, 226, 160);
    
    private static float[] biomeColors = {
            desert,
            savanna,
            tropicalRainforest,
            grassland,
            woodland,
            seasonalForest,
            temperateRainforest,
            borealForest,
            tundra,
            ice,
            beach,
            rocky,
            foamColor,
            deepColor,
            emptyColor
    };

    protected final static float[] BIOME_TABLE = {
            //COLDEST   //COLDER      //COLD               //HOT                     //HOTTER                 //HOTTEST
            Ice+0.7f,   Ice+0.65f,    Grassland+0.9f,      Desert+0.75f,             Desert+0.8f,             Desert+0.85f,            //DRYEST
            Ice+0.6f,   Tundra+0.9f,  Grassland+0.6f,      Grassland+0.3f,           Desert+0.65f,            Desert+0.7f,             //DRYER
            Ice+0.5f,   Tundra+0.7f,  Woodland+0.4f,       Woodland+0.6f,            Savanna+0.8f,           Desert+0.6f,              //DRY
            Ice+0.4f,   Tundra+0.5f,  SeasonalForest+0.3f, SeasonalForest+0.5f,      Savanna+0.6f,            Savanna+0.4f,            //WET
            Ice+0.2f,   Tundra+0.3f,  BorealForest+0.35f,  TemperateRainforest+0.4f, TropicalRainforest+0.6f, Savanna+0.2f,            //WETTER
            Ice+0.0f,   BorealForest, BorealForest+0.15f,  TemperateRainforest+0.2f, TropicalRainforest+0.4f, TropicalRainforest+0.2f, //WETTEST
            Rocky+0.9f, Rocky+0.6f,   Beach+0.4f,          Beach+0.55f,              Beach+0.75f,             Beach+0.9f,              //COASTS
            Ice+0.3f,   River+0.8f,   River+0.7f,          River+0.6f,               River+0.5f,              River+0.4f,              //RIVERS
            Ice+0.2f,   River+0.7f,   River+0.6f,          River+0.5f,               River+0.4f,              River+0.3f,              //LAKES
            Ocean+0.9f, Ocean+0.75f,  Ocean+0.6f,          Ocean+0.45f,              Ocean+0.3f,              Ocean+0.15f,             //OCEANS
            Empty                                                                                                                      //SPACE
    }, BIOME_COLOR_TABLE = new float[61], BIOME_DARK_COLOR_TABLE = new float[61];
    private static final float[] NATION_COLORS = new float[144];
    private static void randomizeColors(long seed)
    {
        float b, diff, alt, hue = NumberTools.randomSignedFloat(seed);
        int bCode;
        for (int i = 0; i < 60; i++) {
            b = BIOME_TABLE[i];
            bCode = (int)b;
            alt = SColor.toEditedFloat(biomeColors[bCode],
                    hue,
                    NumberTools.randomSignedFloat(seed * 3L + bCode) * 0.45f - 0.1f,
                    NumberTools.randomSignedFloat(seed * 5L + bCode) * 0.5f,
                    0f);
            diff = ((b % 1.0f) - 0.48f) * 0.27f;
            BIOME_COLOR_TABLE[i] = (b = (diff >= 0)
                    ? SColor.lerpFloatColors(alt, SColor.FLOAT_WHITE, diff)
                    : SColor.lerpFloatColors(alt, SColor.FLOAT_BLACK, -diff));
            BIOME_DARK_COLOR_TABLE[i] = SColor.lerpFloatColors(b, SColor.FLOAT_BLACK, 0.08f);
        }
        float sat = NumberTools.randomSignedFloat(seed * 3L - 1L) * 0.4f,
                value = NumberTools.randomSignedFloat(seed * 5L - 1L) * 0.3f;

        deepColor = SColor.toEditedFloat(baseDeepColor, hue, sat, value, 0f);
        shallowColor = SColor.toEditedFloat(baseShallowColor, hue, sat, value, 0f);
        coastalColor = SColor.toEditedFloat(baseCoastalColor, hue, sat, value, 0f);
        foamColor = SColor.toEditedFloat(baseFoamColor, hue, sat, value, 0f);
        ice = SColor.toEditedFloat(baseIce, hue, sat * 0.3f, value * 0.2f, 0f);
    }

    static {
        float b, diff;
        for (int i = 0; i < 60; i++) {
            b = BIOME_TABLE[i];
            diff = ((b % 1.0f) - 0.48f) * 0.27f;
            BIOME_COLOR_TABLE[i] = (b = (diff >= 0)
                    ? SColor.lerpFloatColors(biomeColors[(int)b], SColor.FLOAT_WHITE, diff)
                    : SColor.lerpFloatColors(biomeColors[(int)b], SColor.FLOAT_BLACK, -diff));
            BIOME_DARK_COLOR_TABLE[i] = SColor.lerpFloatColors(b, SColor.FLOAT_BLACK, 0.08f);
        }
        BIOME_COLOR_TABLE[60] = BIOME_DARK_COLOR_TABLE[60] = emptyColor;
    }

    private String date, path;
    private final float emphasize(final float a)
    {
        return a * a * (3f - 2f * a);
    }
    private final float extreme(final float a)
    {
        return a * a * a * (a * (a * 6f - 15f) + 10f);
    }

    @Override
    public void create() {
//        earth.perceptualHashQuick(earthHash, workingHash);
//        earthCount = earth.size() - voidCount;
        batch = new SpriteBatch();
        //display = new SquidPanel(width, height, cellWidth, cellHeight);
        view = new StretchViewport(width * cellWidth, height * cellHeight);
        //stage = new Stage(view, batch);
        date = DateFormat.getDateInstance().format(new Date());
        //path = "out/worlds/Sphere " + date + "/";
        //path = "out/worlds/Tiling " + date + "/";
        //path = "out/worlds/AltSphere " + date + "/";
        //path = "out/worlds/Ellipse " + date + "/";
        //path = "out/worlds/Mimic " + date + "/";
        //path = "out/worlds/Dump " + date + "/";
        //path = "out/worlds/SpaceView " + date + "/";
        path = "out/worlds/RoundSide " + date + "/";

        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();
        //Gdx.files.local(path + "Earth.txt").writeString(StringKit.hex(earthHash), false);

        pm = new Pixmap(width * cellWidth, height * cellHeight, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pt = new Texture(pm);

        writer = new PNG8((int)(pm.getWidth() * pm.getHeight() * 1.5f)); // Guess at deflated size.
        writer.setFlipY(false);
        writer.setCompression(6);
        writer.palette = new PaletteReducer();

        rng = new StatefulRNG(CrossHash.hash64(date));
        //rng = new StatefulRNG(0L);
        seed = rng.getState();
        ///world = new WorldMapGenerator.SphereMap(seed, width, height, WhirlingNoise.instance, 0.9);
        //world = new WorldMapGenerator.TilingMap(seed, width, height, WhirlingNoise.instance, 1.625);
        //world = new WorldMapGenerator.SphereMapAlt(seed, width, height, WhirlingNoise.instance, 1.625);
        //world = new WorldMapGenerator.EllipticalMap(seed, width, height, ClassicNoise.instance, 1.5);
        //world = new WorldMapGenerator.MimicMap(seed, ClassicNoise.instance, 1.5);
        //world = new WorldMapGenerator.SpaceViewMap(seed, width, height, ClassicNoise.instance, 0.7);
        world = new WorldMapGenerator.RoundSideMap(seed, width, height, ClassicNoise.instance, 0.7);
        //dbm = new WorldMapGenerator.DetailedBiomeMapper();
        world.generateRivers = false;
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key) {
                    case SquidInput.ENTER:
                        seed = rng.nextLong();
                        generate(seed);
                        rng.setState(seed);
                        break;
                    /*
                    case '=':
                    case '+':
                        zoomIn();
                        break;
                    case '-':
                    case '_':
                        zoomOut();
                        break;
                    */
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE: {
                        Gdx.app.exit();
                    }
                }
                Gdx.graphics.requestRendering();
            }
        }/*, new SquidMouse(1, 1, width, height, 0, 0, new InputAdapter()
        {
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(button == Input.Buttons.RIGHT)
                {
                    zoomOut(screenX, screenY);
                    Gdx.graphics.requestRendering();
                }
                else
                {
                    zoomIn(screenX, screenY);
                    Gdx.graphics.requestRendering();
                }
                return true;
            }
        })*/);
        generate(seed);
        rng.setState(seed);
        //Gdx.input.setInputProcessor(input);
        //display.setPosition(0, 0);
        //stage.addActor(display);
        //Gdx.graphics.setContinuousRendering(false);
        //Gdx.graphics.requestRendering();
    }

    public void zoomIn() {
        zoomIn(width >> 1, height >> 1);
    }
    public void zoomIn(int zoomX, int zoomY)
    {
        long startTime = System.currentTimeMillis();
        world.zoomIn(1, zoomX, zoomY);
//        dbm.makeBiomes(world);
        ttg = System.currentTimeMillis() - startTime;
    }
    public void zoomOut()
    {
        zoomOut(width>>1, height>>1);
    }
    public void zoomOut(int zoomX, int zoomY)
    {
        long startTime = System.currentTimeMillis();
        world.zoomOut(1, zoomX, zoomY);
//        dbm.makeBiomes(world);
        ttg = System.currentTimeMillis() - startTime;
    }
    public void generate(final long seed)
    {
        long startTime = System.currentTimeMillis();
        //randomizeColors(seed);
//        world.generate(1, 1.125, seed); // mimic of Earth favors too-cold planets
//        dbm.makeBiomes(world);
        world.generate(1.0 + NumberTools.formCurvedDouble((seed ^ 0x123456789ABCDL) * 0x12345689ABL) * 0.3,
                LinnormRNG.determineDouble(seed * 0x12345L + 0x54321L) * 0.2 + 0.9, seed);
//        dbm.makeBiomes(world);
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        ++counter;
        String name = lang.word(rng, true); //, Math.min(3 - rng.next(1), rng.betweenWeighted(1, 5, 4))
        while (Gdx.files.local(path + name + ".png").exists())
            name = lang.word(rng, true);

        generate(CrossHash.hash64(name));
        //display.erase();
//        int hc, tc, bc;
//        int[][] heightCodeData = world.heightCodeData;
//        greasedWorld.refill(heightCodeData, 4, 10001).perceptualHashQuick(worldHash, workingHash);
//        worldCount = greasedWorld.size() - voidCount;
//        intersectionCount = greasedWorld.and(earth).size() - voidCount;
//        double jaccard = intersectionCount / (earthCount + worldCount - intersectionCount);
//        if(jaccard < 0.3)
//            return;
        int hc;
        final int[][] heightCodeData = world.heightCodeData;
        final double[][] moistureData = world.moistureData, heatData = world.heatData, heightData = world.heightData;
        double elevation, heat, moisture;
        boolean icy;

//        double[][] heightData = world.heightData;
//        int[][] heatCodeData = dbm.heatCodeData;
//        int[][] biomeCodeData = dbm.biomeCodeData;
        //pm.setColor(SColor.quantize253I(SColor.DB_INK));
        pm.setColor(Color.rgba8888(SColor.DB_INK));
        pm.fill();
//        for (int y = 0; y < height; y++) {
//            PER_CELL:
//            for (int x = 0; x < width; x++) {
//                hc = heightCodeData[x][y];
//                if(hc == 1000)
//                    continue;
//                tc = heatCodeData[x][y];
//                bc = biomeCodeData[x][y];
//                if(tc == 0)
//                {
//                    switch (hc)
//                    {
//                        case 0:
//                        case 1:
//                        case 2:
//                        case 3:
////                            Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(shallowColor, ice,
////                                    (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0))));
////                            pm.setColor(tempColor);
//                            pm.setColor(SColor.floatToInt(SColor.lerpFloatColors(shallowColor, ice,
//                                    (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0)))));
//                            pm.drawRectangle(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
////                            pm.drawPixel(x, y, Color.rgba8888(tempColor));
//                            //display.put(x, y, SColor.lerpFloatColors(shallowColor, ice,
//                            //        (float) ((heightData[x][y] - -1.0) / (0.1 - -1.0))));
//                            continue PER_CELL;
//                        case 4:
////                            Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(lightIce, ice,
////                                    (float) ((heightData[x][y] - WorldMapGenerator.sandLower) / (WorldMapGenerator.sandUpper - WorldMapGenerator.sandLower))));
////                            pm.setColor(tempColor);
//                            pm.setColor(SColor.floatToInt(SColor.lerpFloatColors(lightIce, ice,
//                                    (float) ((heightData[x][y] - WorldMapGenerator.sandLower) / (WorldMapGenerator.sandUpper - WorldMapGenerator.sandLower)))));
//                            pm.drawRectangle(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
////                            pm.drawPixel(x, y, Color.rgba8888(tempColor));
//                            //display.put(x, y, SColor.lerpFloatColors(lightIce, ice,
//                            //        (float) ((heightData[x][y] - 0.1) / (0.18 - 0.1))));
//                            continue PER_CELL;
//                    }
//                }
//                switch (hc) {
//                    case 0:
//                    case 1:
//                    case 2:
//                    case 3:
////                        Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(deepColor, coastalColor,
////                                (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0))));
////                        pm.setColor(tempColor);
//                        pm.setColor(SColor.floatToInt(SColor.lerpFloatColors(deepColor, coastalColor,
//                                (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0)))));
//                        pm.drawRectangle(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
////                            pm.drawPixel(x, y, Color.rgba8888(tempColor));
//                        //display.put(x, y, SColor.lerpFloatColors(deepColor, coastalColor,
//                        //        (float) ((heightData[x][y] - -1.0) / (0.1 - -1.0))));
//                        break;
//                    default:
//                        /*
//                        if(partialLakeData.contains(x, y))
//                            System.out.println("LAKE  x=" + x + ",y=" + y + ':' + (((heightData[x][y] - lowers[hc]) / (differences[hc])) * 19
//                                    + shadingData[x][y] * 13) * 0.03125f);
//                        else if(partialRiverData.contains(x, y))
//                            System.out.println("RIVER x=" + x + ",y=" + y + ':' + (((heightData[x][y] - lowers[hc]) / (differences[hc])) * 19
//                                    + shadingData[x][y] * 13) * 0.03125f);
//                        */
//
////                        Color.abgr8888ToColor(tempColor, SColor.lerpFloatColors(BIOME_COLOR_TABLE[dbm.extractPartB(bc)],
////                                BIOME_DARK_COLOR_TABLE[dbm.extractPartA(bc)], dbm.extractMixAmount(bc)));
////                        pm.setColor(tempColor);
//                        pm.setColor(SColor.floatToInt(SColor.lerpFloatColors(BIOME_COLOR_TABLE[dbm.extractPartB(bc)],
//                                BIOME_DARK_COLOR_TABLE[dbm.extractPartA(bc)], dbm.extractMixAmount(bc))));
//                        pm.drawRectangle(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
////                            pm.drawPixel(x, y, Color.rgba8888(tempColor));
//                        //display.put(x, y, SColor.lerpFloatColors(BIOME_COLOR_TABLE[biomeLowerCodeData[x][y]],
//                        //        BIOME_DARK_COLOR_TABLE[biomeUpperCodeData[x][y]],
//                        //        (float) //(((heightData[x][y] - lowers[hc]) / (differences[hc])) * 11 +
//                        //                shadingData[x][y]// * 21) * 0.03125f
//                        //        ));
//
//                        //display.put(x, y, SColor.lerpFloatColors(darkTropicalRainforest, desert, (float) (heightData[x][y])));
//                }
//            }
//        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                hc = heightCodeData[x][y];
                if (hc == 1000)
                    continue;
                moisture = moistureData[x][y];
                heat = heatData[x][y];
                elevation = heightData[x][y];
                icy = heat - elevation * 0.25 < 0.16;
                if(hc < 4) {
                    pm.drawPixel(x, y, SColor.floatToInt(
                            heat < 0.26 ? SColor.lerpFloatColors(shallowColor, ice,
                                    (float)((elevation + 1.0) / (WorldMapGenerator.sandLower+1.0)))
                                    : SColor.lerpFloatColors(
                                    BIOME_COLOR_TABLE[56], coastalColor,
                                    (MathUtils.clamp((float) (((elevation + 0.06) * 16.0) / (WorldMapGenerator.sandLower + 1.0)), 0f, 1f)))));
                }
                else if(hc == 4)
                    pm.drawPixel(x, y, SColor.floatToInt(SColor.lerpFloatColors(icy ? BIOME_COLOR_TABLE[0] : SColor.lerpFloatColors(BIOME_DARK_COLOR_TABLE[36], BIOME_COLOR_TABLE[41],
                            (float) ((heat - world.minHeat) / (world.maxHeat - world.minHeat + 0.001))),
                            SColor.lerpFloatColors(icy ? ice : SColor.lerpFloatColors(rocky, desertAlt,
                                    (float) ((heat - world.minHeat) / (world.maxHeat - world.minHeat + 0.001))),
                                    icy ? lightIce : SColor.lerpFloatColors(woodland, BIOME_COLOR_TABLE[35],
                                            ((float)heat)),
                                    (extreme((float) (moisture)))),
                            (float) ((elevation - WorldMapGenerator.sandLower) / (WorldMapGenerator.sandUpper - WorldMapGenerator.sandLower)))));
                else
                    pm.drawPixel(x, y, SColor.floatToInt(SColor.lerpFloatColors(icy ? ice : SColor.lerpFloatColors(rocky, desertAlt,
                            (float) ((heat - world.minHeat) / (world.maxHeat - world.minHeat + 0.001))),
                            icy ? lightIce : SColor.lerpFloatColors(woodland, BIOME_COLOR_TABLE[35],
                                    ((float)heat)),
                            (extreme((float) (moisture))))));
            }
        }
        writer.palette.analyze(pm);
        writer.palette.reduce(pm);
        batch.begin();
        pt.draw(pm, 0, 0);
        batch.draw(pt, 0, 0);
        batch.end();
//        PixmapIO.writePNG(Gdx.files.local(path + name + ".png"), pm);
        try {
            writer.write(Gdx.files.local(path + name + ".png"), pm, false);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: " + path + name + ".png", ex);
        }

        //PixmapIO.writePNG(Gdx.files.local(path + name + ".png"), pm);
//        int dist = Long.bitCount(earthHash[0] ^ worldHash[0]) + Long.bitCount(earthHash[1] ^ worldHash[1])
//                + Long.bitCount(earthHash[2] ^ worldHash[2]) + Long.bitCount(earthHash[3] ^ worldHash[3]);
//        Gdx.files.local(path + StringKit.bin((long)(jaccard * 0x100000000000L)) + " " + name +".txt").writeString(
//                "Perceptual Hash: " + StringKit.hex(worldHash) +
//                        "\nJaccard similarity as double: " + jaccard +
//                        "\nHamming distance between hashes of Earth and " + name + ": " + dist +
//                        "\nNon-water cells: " + greasedWorld.size() +
//                        "\nGuessed empty cells: " + voidCount +
//                        "\nActual empty cells: " + greasedWorld.refill(world.heightCodeData, 1000).size() +
//                        "\nJaccard similarity as ratio: " + (int)intersectionCount + "/" + (int)(earthCount + worldCount - intersectionCount) +
//                        "\nSeed: 0x" + StringKit.hex(CrossHash.hash64(name)) + "L", false);

//        StringBuilder csv = new StringBuilder((width + 50) * height * 5);
//        csv.append("public static final String[] BIOME_TABLE = {\n" +
//                "    //COLDEST //COLDER        //COLD            //HOT                  //HOTTER              //HOTTEST\n" +
//                "    \"Ice\",    \"Ice\",          \"Grassland\",      \"Desert\",              \"Desert\",             \"Desert\",             //DRYEST\n" +
//                "    \"Ice\",    \"Tundra\",       \"Grassland\",      \"Grassland\",           \"Desert\",             \"Desert\",             //DRYER\n" +
//                "    \"Ice\",    \"Tundra\",       \"Woodland\",       \"Woodland\",            \"Savanna\",            \"Desert\",             //DRY\n" +
//                "    \"Ice\",    \"Tundra\",       \"SeasonalForest\", \"SeasonalForest\",      \"Savanna\",            \"Savanna\",            //WET\n" +
//                "    \"Ice\",    \"Tundra\",       \"BorealForest\",   \"TemperateRainforest\", \"TropicalRainforest\", \"Savanna\",            //WETTER\n" +
//                "    \"Ice\",    \"BorealForest\", \"BorealForest\",   \"TemperateRainforest\", \"TropicalRainforest\", \"TropicalRainforest\", //WETTEST\n" +
//                "    \"Rocky\",  \"Rocky\",        \"Beach\",          \"Beach\",               \"Beach\",              \"Beach\",              //COASTS\n" +
//                "    \"Ice\",    \"River\",        \"River\",          \"River\",               \"River\",              \"River\",              //RIVERS\n" +
//                "    \"Ice\",    \"River\",        \"River\",          \"River\",               \"River\",              \"River\",              //LAKES\n" +
//                "    \"Ocean\",  \"Ocean\",        \"Ocean\",          \"Ocean\",               \"Ocean\",              \"Ocean\",              //OCEAN\n" +
//                "};\n\n" +
//                "public static float extractFloat(String[] mapData, int x, int y) {\n" +
//                "    return (mapData[y].codePointAt(x) - 93) * 0x1p-10f;\n" +
//                "}\n\n" +
//                "public static int extractInt(String[] mapData, int x, int y) {\n" +
//                "    return mapData[y].codePointAt(x) - 93;\n" +
//                "}\n\n" +
//                "public static String extractBiome(String[] mapData, int x, int y) {\n" +
//                "    return BIOME_TABLE[mapData[y].codePointAt(x) - 93];\n" +
//                "}\n\n");
//        csv.append("/** Use with extractFloat() or extractInt() to get the height of this area, either between 0.0 and 1.0 or between 0 and 1023 */\npublic String[] heightMap = new String[] {\n");
//        for (int y = 0; y < height; y++) {
//            csv.append('"');
//            for (int x = 0; x < width; x++) {
//                csv.append((char)((1.0 + world.heightData[x][y]) * 512 + 93));
//            }
//            csv.append("\",\n");
//        }
//        csv.append("};\n\n");
//        csv.append("/** Use with extractFloat() to get a heat level between 0.0 and 1.0, as some kind of yearly average*/\n" +
//                "public String[] heatMap = new String[] {\n");
//        for (int y = 0; y < height; y++) {
//            csv.append('"');
//            for (int x = 0; x < width; x++) {
//                csv.append((char)(world.heatData[x][y] * 1024 + 93));
//            }
//            csv.append("\",\n");
//        }
//        csv.append("};\n\n");
//        csv.append("/** Use with extractFloat() to get moisture level between 0.0 and 1.0, as some kind of yearly average */\n" +
//                "public String[] moistureMap = new String[] {\n");
//        for (int y = 0; y < height; y++) {
//            csv.append('"');
//            for (int x = 0; x < width; x++) {
//                csv.append((char)(world.moistureData[x][y] * 1024 + 93));
//            }
//            csv.append("\",\n");
//        }
//        csv.append("};\n\n");
//        StringBuilder csv2 = new StringBuilder(width * height + 50);
//        StringBuilder csv3 = new StringBuilder(width * height + 50);
//        StringBuilder csv4 = new StringBuilder(width * height + 50);
//        csv.append("/** Use with extractBiome() to get a biome name or extractInt() to get an index (0-59, inclusive); this is biome A */\n" +
//                "public String[] biomeMapA = new String[] {\n");
//        csv2.append("/** Use with extractBiome() to get a biome name or extractInt() to get an index (0-59, inclusive); this is biome B */\n" +
//                "public String[] biomeMapB = new String[] {\n");
//        csv3.append("/** Use with extractFloat(); how much biome A affects this area. */\npublic String[] biomePortionMapA = new String[] {\n");
//        csv4.append("/** Use with extractFloat(); how much biome B affects this area. */\npublic String[] biomePortionMapB = new String[] {\n");
//        for (int y = 0; y < height; y++) {
//            csv.append('"');
//            csv2.append('"');
//            csv3.append('"');
//            csv4.append('"');
//            for (int x = 0; x < width; x++) {
//                int biome = dbm.biomeCodeData[x][y];
//                float mix = dbm.extractMixAmount(biome);
//                csv.append((char)(dbm.extractPartA(biome) + 93));
//                csv2.append((char)(dbm.extractPartB(biome) + 93));
//                csv3.append((char)(93 + 1024.0 * mix));
//                csv4.append((char)(1024 + 93 - 1024.0 * mix));
//            }
//            csv.append("\",\n");
//            csv2.append("\",\n");
//            csv3.append("\",\n");
//            csv4.append("\",\n");
//        }
//        csv.append("};\n\n");
//        csv2.append("};\n\n");
//        csv3.append("};\n\n");
//        csv4.append("};\n\n");
//        csv.append(csv2).append(csv3).append(csv4);
//        Gdx.files.local(path + name + ".java").writeString(csv.toString(), false);
        //if(counter >= 1000000 || jaccard >= 0.4)
        if(counter >= 20)
                Gdx.app.exit();
    }
    @Override
    public void render() {
        // standard clear the background routine for libGDX
        //Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        Gdx.graphics.setTitle("Map! Took " + ttg + " ms to generate");

        // if we are waiting for the player's input and get input, process it.
        if (input.hasNext()) {
            input.next();
        }
        // stage has its own batch and must be explicitly told to draw().
        //stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        writer.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Detailed World Map";
        config.width = width * cellWidth;
        config.height = height * cellHeight;
        //config.fullscreen = true;
        config.foregroundFPS = 0;
        //config.fullscreen = true;
        config.backgroundFPS = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new DetailedWorldMapWriter(), config);
    }
}
