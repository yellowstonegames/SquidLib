package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.DiverRNG;
import squidpony.squidmath.NumberTools;

/**
 * Created by Tommy Ettinger on 9/6/2019.
 */
@SuppressWarnings("NumericOverflow")
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

    public void setBiomeMapper(WorldMapGenerator.DetailedBiomeMapper biomeMapper) {
        this.biomeMapper = biomeMapper;
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
            Shallow                = 12,
            Ocean                  = 13,
            Empty                  = 14;

    public static float iceColor = SColor.floatGet(240 << 24 | 248 << 16 | 255 << 8 | 255);
    public static float desertColor = SColor.floatGet(248 << 24 | 229 << 16 | 180 << 8 | 255);
    public static float savannaColor = SColor.floatGet(190 << 24 | 195 << 16 | 105 << 8 | 255);
    public static float tropicalRainforestColor = SColor.floatGet(40 << 24 | 100 << 16 | 15 << 8 | 255);
    public static float tundraColor = SColor.floatGet(151 << 24 | 175 << 16 | 159 << 8 | 255);
    public static float temperateRainforestColor = SColor.floatGet(54 << 24 | 113 << 16 | 60 << 8 | 255);
    public static float grasslandColor = SColor.floatGet(145 << 24 | 165 << 16 | 100 << 8 | 255);
    public static float seasonalForestColor = SColor.floatGet(70 << 24 | 120 << 16 | 35 << 8 | 255);
    public static float borealForestColor = SColor.floatGet(75 << 24 | 105 << 16 | 45 << 8 | 255);
    public static float woodlandColor = SColor.floatGet(92 << 24 | 160 << 16 | 70 << 8 | 255);
    public static float rockyColor = SColor.floatGet(171 << 24 | 175 << 16 | 145 << 8 | 255);
    public static float beachColor = SColor.floatGet(255 << 24 | 235 << 16 | 180 << 8 | 255);
    public static float emptyColor = SColor.floatGet(34 << 24 | 32 << 16 | 52 << 8 | 255);

    // water colors
    public static float deepColor = SColor.floatGet(0 << 24 | 42 << 16 | 88 << 8 | 255);
    public static float shallowColor = SColor.floatGet(20 << 24 | 145 << 16 | 197 << 8 | 255);
    public static float foamColor = SColor.floatGet(61 << 24 | 162 << 16 | 215 << 8 | 255);

    protected float[] biomeColors = {
            desertColor,
            savannaColor,
            tropicalRainforestColor,
            grasslandColor,
            woodlandColor,
            seasonalForestColor,
            temperateRainforestColor,
            borealForestColor,
            tundraColor,
            iceColor,
            beachColor,
            rockyColor,
            shallowColor,
            deepColor,
            emptyColor
    };

    public final static float[] BIOME_TABLE = {
            //COLDEST   //COLDER      //COLD               //HOT                     //HOTTER                 //HOTTEST
            Ice+0.85f,  Ice+0.65f,    Grassland+0.9f,      Desert+0.75f,             Desert+0.8f,             Desert+0.85f,            //DRYEST
            Ice+0.7f,   Tundra+0.9f,  Grassland+0.6f,      Grassland+0.3f,           Desert+0.65f,            Desert+0.7f,             //DRYER
            Ice+0.55f,  Tundra+0.7f,  Woodland+0.4f,       Woodland+0.6f,            Savanna+0.8f,            Desert+0.6f,             //DRY
            Ice+0.4f,   Tundra+0.5f,  SeasonalForest+0.3f, SeasonalForest+0.5f,      Savanna+0.6f,            Savanna+0.4f,            //WET
            Ice+0.2f,   Tundra+0.3f,  BorealForest+0.35f,  TemperateRainforest+0.4f, TropicalRainforest+0.6f, Savanna+0.2f,            //WETTER
            Ice+0.0f,   BorealForest, BorealForest+0.15f,  TemperateRainforest+0.2f, TropicalRainforest+0.4f, TropicalRainforest+0.2f, //WETTEST
            Rocky+0.9f, Rocky+0.6f,   Beach+0.4f,          Beach+0.55f,              Beach+0.75f,             Beach+0.9f,              //COASTS
            Ice+0.3f,   Shallow+0.9f, Shallow+0.75f,       Shallow+0.6f,             Shallow+0.5f,            Shallow+0.4f,            //RIVERS
            Ice+0.2f,   Shallow+0.9f, Shallow+0.65f,       Shallow+0.5f,             Shallow+0.4f,            Shallow+0.3f,            //LAKES
            Ocean+0.9f, Ocean+0.75f,  Ocean+0.6f,          Ocean+0.45f,              Ocean+0.3f,              Ocean+0.15f,             //OCEANS
            Empty                                                                                                                      //SPACE
    };
    public final float[] BIOME_COLOR_TABLE = new float[61], BIOME_DARK_COLOR_TABLE = new float[61];
    
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

    /**
     * Initializes the colors to use for each biome (these are almost always mixed with other biome colors in practice).
     * Each parameter may be null to use the default for an Earth-like world; otherwise it should be a libGDX
     * {@link Color} or some subclass, like {@link SColor}. All non-null parameters should probably be fully opaque,
     * except {@code emptyColor}, which is only used for world maps that show empty space (like a globe, as produced by
     * {@link WorldMapGenerator.RotatingSpaceMap}).
     * @param desertColor hot, dry, barren land; may be sandy, but many real-world deserts don't have much sand
     * @param savannaColor hot, mostly-dry land with some parched vegetation; also called scrub or chaparral
     * @param tropicalRainforestColor hot, extremely wet forests with dense rich vegetation
     * @param grasslandColor prairies that are dry and usually wind-swept, but not especially hot or cold
     * @param woodlandColor part-way between a prairie and a forest; not especially hot or cold
     * @param seasonalForestColor forest that becomes barren in winter (deciduous trees); not especially hot or cold
     * @param temperateRainforestColor forest that tends to be slightly warm but very wet
     * @param borealForestColor forest that tends to be cold and very wet
     * @param tundraColor very cold plains that still have some low-lying vegetation; also called taiga 
     * @param iceColor cold barren land covered in permafrost; also used for rivers and lakes that are frozen
     * @param beachColor sandy or otherwise light-colored shorelines; here, these are more common in warmer places
     * @param rockyColor rocky or otherwise rugged shorelines; here, these are more common in colder places
     * @param shallowColor the color of very shallow water; will be mixed with {@code deepColor} to get most ocean colors
     * @param deepColor the color of very deep water; will be mixed with {@code shallowColor} to get most ocean colors
     * @param emptyColor the color used for empty space off the edge of the world map; may be transparent
     */
    public void initialize(
            Color desertColor,
            Color savannaColor,
            Color tropicalRainforestColor,
            Color grasslandColor,
            Color woodlandColor,
            Color seasonalForestColor,
            Color temperateRainforestColor,
            Color borealForestColor,
            Color tundraColor,
            Color iceColor,
            Color beachColor,
            Color rockyColor,
            Color shallowColor,
            Color deepColor,
            Color emptyColor
    )
    {
        biomeColors[ 0] = desertColor == null ? WorldMapView.desertColor : desertColor.toFloatBits();
        biomeColors[ 1] = savannaColor == null ? WorldMapView.savannaColor : savannaColor.toFloatBits();
        biomeColors[ 2] = tropicalRainforestColor == null ? WorldMapView.tropicalRainforestColor : tropicalRainforestColor.toFloatBits();
        biomeColors[ 3] = grasslandColor == null ? WorldMapView.grasslandColor : grasslandColor.toFloatBits();
        biomeColors[ 4] = woodlandColor == null ? WorldMapView.woodlandColor : woodlandColor.toFloatBits();
        biomeColors[ 5] = seasonalForestColor == null ? WorldMapView.seasonalForestColor : seasonalForestColor.toFloatBits();
        biomeColors[ 6] = temperateRainforestColor == null ? WorldMapView.temperateRainforestColor : temperateRainforestColor.toFloatBits();
        biomeColors[ 7] = borealForestColor == null ? WorldMapView.borealForestColor : borealForestColor.toFloatBits();
        biomeColors[ 8] = tundraColor == null ? WorldMapView.tundraColor : tundraColor.toFloatBits();
        biomeColors[ 9] = iceColor == null ? WorldMapView.iceColor : iceColor.toFloatBits();
        biomeColors[10] = beachColor == null ? WorldMapView.beachColor : beachColor.toFloatBits();
        biomeColors[11] = rockyColor == null ? WorldMapView.rockyColor : rockyColor.toFloatBits();
        biomeColors[12] = shallowColor == null ? WorldMapView.shallowColor : shallowColor.toFloatBits();
        biomeColors[13] = deepColor == null ? WorldMapView.deepColor : deepColor.toFloatBits();
        biomeColors[14] = emptyColor == null ? WorldMapView.emptyColor : emptyColor.toFloatBits();
        float b, diff;
        for (int i = 0; i < 60; i++) {
            b = BIOME_TABLE[i];
            diff = ((b % 1.0f) - 0.48f) * 0.27f;
            BIOME_COLOR_TABLE[i] = b = (diff >= 0)
                    ? SColor.lightenFloat(biomeColors[(int)b], diff)
                    : SColor.darkenFloat(biomeColors[(int)b], -diff);
            BIOME_DARK_COLOR_TABLE[i] = SColor.darkenFloat(b, 0.08f);
        }
        BIOME_COLOR_TABLE[60] = BIOME_DARK_COLOR_TABLE[60] = biomeColors[14];
        biomeColors[ 0] = WorldMapView.desertColor;
        biomeColors[ 1] = WorldMapView.savannaColor;
        biomeColors[ 2] = WorldMapView.tropicalRainforestColor;
        biomeColors[ 3] = WorldMapView.grasslandColor;
        biomeColors[ 4] = WorldMapView.woodlandColor;
        biomeColors[ 5] = WorldMapView.seasonalForestColor;
        biomeColors[ 6] = WorldMapView.temperateRainforestColor;
        biomeColors[ 7] = WorldMapView.borealForestColor;
        biomeColors[ 8] = WorldMapView.tundraColor;
        biomeColors[ 9] = WorldMapView.iceColor;
        biomeColors[10] = WorldMapView.beachColor;
        biomeColors[11] = WorldMapView.rockyColor;
        biomeColors[12] = WorldMapView.shallowColor;
        biomeColors[13] = WorldMapView.deepColor;
        biomeColors[14] = WorldMapView.emptyColor;
    }

    /**
     * Initializes the colors to use in some combination for all biomes, without regard for what the biome really is.
     * There should be at least one packed float color given in similarColors, but there can be many of them.
     * @param similarColors an array or vararg of packed float colors with at least one element
     */
    public void match(
            float... similarColors
    )
    {
        for (int i = 0; i < 14; i++) {
            biomeColors[i] = SColor.lerpFloatColors(similarColors[i % similarColors.length], similarColors[(i * 5 + 3) % similarColors.length], 0.5f);
        }
        biomeColors[14] = WorldMapView.emptyColor;
        float b, diff;
        for (int i = 0; i < 60; i++) {
            b = BIOME_TABLE[i];
            diff = ((b % 1.0f) - 0.48f) * 0.27f;
            BIOME_COLOR_TABLE[i] = b = (diff >= 0)
                    ? SColor.lightenFloat(biomeColors[(int)b], diff)
                    : SColor.darkenFloat(biomeColors[(int)b], -diff);
            BIOME_DARK_COLOR_TABLE[i] = SColor.darkenFloat(b, 0.08f);
        }
        BIOME_COLOR_TABLE[60] = BIOME_DARK_COLOR_TABLE[60] = biomeColors[14];
        biomeColors[ 0] = WorldMapView.desertColor;
        biomeColors[ 1] = WorldMapView.savannaColor;
        biomeColors[ 2] = WorldMapView.tropicalRainforestColor;
        biomeColors[ 3] = WorldMapView.grasslandColor;
        biomeColors[ 4] = WorldMapView.woodlandColor;
        biomeColors[ 5] = WorldMapView.seasonalForestColor;
        biomeColors[ 6] = WorldMapView.temperateRainforestColor;
        biomeColors[ 7] = WorldMapView.borealForestColor;
        biomeColors[ 8] = WorldMapView.tundraColor;
        biomeColors[ 9] = WorldMapView.iceColor;
        biomeColors[10] = WorldMapView.beachColor;
        biomeColors[11] = WorldMapView.rockyColor;
        biomeColors[12] = WorldMapView.shallowColor;
        biomeColors[13] = WorldMapView.deepColor;
        biomeColors[14] = WorldMapView.emptyColor;
    }

    public void generate()
    {
//        generate(world.seedA, world.seedB, 0.9 + NumberTools.formCurvedDouble((world.seedA ^ 0x123456789ABCDL) * 0x12345689ABL ^ world.seedB) * 0.15,
//                DiverRNG.determineDouble(world.seedB * 0x12345L + 0x54321L ^ world.seedA) * 0.2 + 1.0);
        generate(world.seedA, world.seedB, 1.0 + NumberTools.formCurvedDouble((world.seedA ^ 0x123456789ABCDL) * 0x12345689ABL ^ world.seedB) * 0.25,
                DiverRNG.determineDouble(world.seedB * 0x12345L + 0x54321L ^ world.seedA) * 0.375 + 1.0625);
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
                            colorMap[x][y] = SColor.lerpFloatColors(BIOME_COLOR_TABLE[50], BIOME_COLOR_TABLE[12],
                                    (float) ((heightData[x][y] - -1.0) / (WorldMapGenerator.sandLower - -1.0)));
                            continue PER_CELL;
                        case 4:
                            colorMap[x][y] = SColor.lerpFloatColors(BIOME_COLOR_TABLE[0], BIOME_COLOR_TABLE[12],
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
                                BIOME_COLOR_TABLE[56], BIOME_COLOR_TABLE[43],
                                (MathUtils.clamp((float) (((heightData[x][y] + 0.06) * 8.0) / (WorldMapGenerator.sandLower + 1.0)), 0f, 1f)));
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
