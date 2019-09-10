package squidpony.squidgrid.gui.gdx;

import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.DiverRNG;
import squidpony.squidmath.MathExtras;
import squidpony.squidmath.NumberTools;

/**
 * Created by Tommy Ettinger on 9/6/2019.
 */
public class WorldMapView {
    protected int width, height;
    protected float[][] colorMap;
    protected WorldMapGenerator world;
    protected WorldMapGenerator.DetailedBiomeMapper biomeMapper;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float[][] getColorMap() {
        return colorMap;
    }

    public WorldMapGenerator.DetailedBiomeMapper getBiomeMapper() {
        return biomeMapper;
    }

    public WorldMapGenerator getWorld() {
        return world;
    }

    public void setWorld(WorldMapGenerator world) {
        this.world = world;
        if(this.width != world.width || this.height != world.height)
        {
            width = world.width;
            height = world.height;
            colorMap = new float[width][height];
        }
    }

    public WorldMapView(WorldMapGenerator worldMapGenerator)
    {
        world = worldMapGenerator == null ? new WorldMapGenerator.LocalMap() : worldMapGenerator;
        width = world.width;
        height = world.height;
        colorMap = new float[width][height];
        this.biomeMapper = new WorldMapGenerator.DetailedBiomeMapper();
        initialize();
    }
    
    public WorldMapView(long seed, int width, int height)
    {
        this(new WorldMapGenerator.LocalMap(seed, width, height));
    }
    
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
    private static float baseCoastalColor = SColor.lightenFloat(baseShallowColor, 0.3f);
    private static float baseFoamColor = SColor.floatGetI(61,  162, 215);

    private static float deepColor = baseDeepColor;
    private static float shallowColor = baseShallowColor;
    private static float coastalColor = baseCoastalColor;
    private static float foamColor = baseFoamColor;

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
    
    public void initialize()
    {
        initialize(0f, 0f, 0f, 1f);
    }
    
    public void initialize(float hue, float saturation, float brightness, float contrast)
    {
        float b, diff;
        for (int i = 0; i < 60; i++) {
            b = BIOME_TABLE[i];
            diff = ((b % 1.0f) - 0.48f) * 0.27f * contrast;
            BIOME_COLOR_TABLE[i] = b = SColor.toEditedFloat((diff >= 0)
                    ? SColor.lightenFloat(biomeColors[(int)b], diff)
                    : SColor.darkenFloat(biomeColors[(int)b], -diff), hue, saturation, brightness, 0f);
            BIOME_DARK_COLOR_TABLE[i] = SColor.darkenFloat(b, 0.08f);
        }
        BIOME_COLOR_TABLE[60] = BIOME_DARK_COLOR_TABLE[60] = emptyColor;
    }

    public void generate()
    {
        generate(world.seedA, world.seedB, 0.9 + NumberTools.formCurvedDouble((world.seedA ^ 0x123456789ABCDL) * 0x12345689ABL ^ world.seedB) * 0.15,
                DiverRNG.determineDouble(world.seedB * 0x12345L + 0x54321L ^ world.seedA) * 0.2 + 1.0);
    }
    public void generate(double landMod, double heatMod)
    {
        generate(world.seedA, world.seedB, landMod, heatMod);
    }
    
    public void generate(int seedA, int seedB, double landMod, double heatMod) {
        long seed = (long) seedB << 32 | (seedA & 0xFFFFFFFFL);
        world.generate(landMod, heatMod, seed);
        biomeMapper.makeBiomes(world);
    }
    public float[][] show()
    {
        int hc, tc, bc;
        final int[][] heightCodeData = world.heightCodeData;
        double[][] heightData = world.heightData;
        int[][] heatCodeData = biomeMapper.heatCodeData;
        int[][] biomeCodeData = biomeMapper.biomeCodeData;

        for (int y = 0; y < height; y++) {
            PER_CELL:
            for (int x = 0; x < width; x++) {
                hc = heightCodeData[x][y];
                if(hc == 1000)
                {
                    colorMap[x][y] = emptyColor;
                    continue;
                }
                tc = heatCodeData[x][y];
                bc = biomeCodeData[x][y];
                if(tc == 0)
                {
                    switch (hc)
                    {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            colorMap[x][y] = SColor.lerpFloatColors(shallowColor, ice,
                                    (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0)));
                            continue PER_CELL;
                        case 4:
                            colorMap[x][y] = SColor.lerpFloatColors(lightIce, ice,
                                    (float) ((heightData[x][y] - WorldMapGenerator.sandLower) / (WorldMapGenerator.sandUpper - WorldMapGenerator.sandLower)));
                            continue PER_CELL;
                    }
                }
                switch (hc) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        colorMap[x][y] = SColor.lerpFloatColors(
                                BIOME_COLOR_TABLE[56], coastalColor,
                                (MathExtras.clamp((float) (((heightData[x][y] + 0.06) * 8.0) / (WorldMapGenerator.sandLower + 1.0)), 0f, 1f)));
                        break;
                    default:
                        colorMap[x][y] = SColor.lerpFloatColors(BIOME_COLOR_TABLE[biomeMapper.extractPartB(bc)],
                                BIOME_DARK_COLOR_TABLE[biomeMapper.extractPartA(bc)], biomeMapper.extractMixAmount(bc));
                }
            }
        }
        
        return colorMap;
    }
}
