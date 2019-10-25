package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.Maker;
import squidpony.Thesaurus;
import squidpony.annotation.Beta;
import squidpony.squidmath.*;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A finite 2D area map for some kind of wilderness, with specifics handled by subclasses.
 * Regional maps for wilderness areas have very different requirements from mostly-artificial dungeons. This is intended
 * to work alongside {@link WorldMapGenerator} and {@link WorldMapGenerator.DetailedBiomeMapper} to produce blends
 * between biomes where a strict line can't be drawn between, say, woodland and grassland.
 * <br>
 * Using this code mostly involves constructing a subclass, adding kinds of game objects that you want to appear
 * in this type of map to {@link #contentTypes}, and only then calling {@link #generate()}, which is implemented by
 * subclasses only. You can use just about any type for {@code T}, including {@link Object}; all this code uses is the
 * number of items in {@link #contentTypes} to put correct indices into {@link #content}. Because contentTypes is an
 * ArrayList, you can have and are encouraged to have duplicates when an object should appear more often. An index of -1
 * in content indicates nothing of note is present there. There is also a String array of {@link #floorTypes} that is
 * not typically user-set unless you subclass WildMap yourself; it is used to look up the indices in {@link #floors}.
 * The floors are set to reasonable values for the particular subclass, so a forest might have "dirt" and "leaves" among
 * others, while a desert might only have "sand". Again, only the indices matter, so you could change the values in
 * {@link #floorTypes} to match names of textures in a graphical game and make lookup easier, or to a char followed by
 * the name of a color (as in SColor in the display module) for a text-based game.
 * <br>
 * Created by Tommy Ettinger on 10/16/2019.
 */
@Beta
public class WildMap implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int width, height, biome;
    public IStatefulRNG rng;
    public ArrayList<String> contentTypes;
    public ArrayList<String> floorTypes;
    public final int[][] content, floors;

    /**
     * Meant for generating large ArrayLists of Strings where an individual String may occur quite a few times.
     * The rest parameter is a vararg (it may also be an Object array) of alternating String and Integer values, where
     * an Integer is how many times to repeat the preceding String in the returned ArrayList.
     * @param rest a vararg (or Object array) of alternating String and Integer values
     * @return an ArrayList of Strings, probably with some or most of them repeated; you may want to shuffle this result
     */
    public static ArrayList<String> makeRepeats(Object... rest)
    {
        if(rest == null || rest.length < 2)
        {
            return new ArrayList<>(0);
        }
        ArrayList<String> al = new ArrayList<>(rest.length);

        for (int i = 0; i < rest.length - 1; i+=2) {
            try {
                int count = (int)rest[i+1];
                String v = (String) rest[i];
                for (int j = 0; j < count; j++) {
                    al.add(v);
                }
            }catch (ClassCastException ignored) {
            }
        }
        return al;
    }
    public static ArrayList<String> makeShuffledRepeats(IRNG rng, Object... rest) 
    {
        ArrayList<String> al = makeRepeats(rest);
        rng.shuffleInPlace(al);
        return al;
    }
    public static ArrayList<String> makeVegetation(IRNG rng, int size, double monoculture, FakeLanguageGen naming)
    {
        Thesaurus t = new Thesaurus(rng);
        ArrayList<String> al = new ArrayList<>(size);
        String latest;
        for (int i = size; i > 0; i--) {
            al.add(latest = t.makePlantName(naming));
            for (double j = rng.nextDouble(monoculture * 2.0 * size); j >= 1 && i > 0; j--, i--) {
                al.add(latest);
            }
        }
        rng.shuffleInPlace(al);
        return al;
    }
    
    public static ArrayList<String> floorsByBiome(int biome, IRNG rng)
    {
        biome = (biome & 1023) % 61;
        switch (biome)
        {
            case 0: //Ice
            case 1:
            case 6:
            case 12:
            case 18:
            case 24: 
            case 30:
            case 42:
            case 48:
                return makeShuffledRepeats(rng, "snow", 3, "ice", 1);
            case 7: //Tundra
            case 13:
            case 19:
            case 25:
                return makeShuffledRepeats(rng, "dirt", 6, "pebbles", 1, "snow", 9, "dry grass", 4);
            case 26: //BorealForest
            case 31:
            case 32:
                return makeShuffledRepeats(rng, "dirt", 3, "pebbles", 1, "snow", 11);
            case 43: //River
            case 44:
            case 45:
            case 46:
            case 47:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
                return Maker.makeList("fresh water");
            case 54: //Ocean
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
                return Maker.makeList("salt water");
            case 3: //Desert
            case 4:
            case 5:
            case 10:
            case 11:
            case 17:
            case 38: //Beach
            case 39:
            case 40:
            case 41: 
                return Maker.makeList("sand");
            case 2: //Grassland
            case 8:
            case 9:
                return makeShuffledRepeats(rng, "dirt", 8, "dry grass", 13, "grass", 2);
            case 14: //Woodland
            case 15:
                return makeShuffledRepeats(rng, "dirt", 11, "leaves", 3, "dry grass", 8);
            case 16: //Savanna
            case 22:
            case 23:
            case 29:
                return makeShuffledRepeats(rng, "dirt", 4, "dry grass", 17);
            case 20: //SeasonalForest
            case 21:
                return makeShuffledRepeats(rng, "dirt", 9, "leaves", 6, "grass", 14);
            case 27: //TemperateRainforest
            case 33:
                return makeShuffledRepeats(rng, "mud", 3, "leaves", 8, "grass", 10, "moss", 5);
            case 28: //TropicalRainforest
            case 34:
            case 35:
                return makeShuffledRepeats(rng, "mud", 7, "leaves", 6, "grass", 4, "moss", 11);
            case 36: // Rocky
            case 37:
                return makeShuffledRepeats(rng, "pebbles", 5, "rubble", 1);
            default:
                return Maker.makeList("empty space");

        }
//                //COLDEST //COLDER        //COLD            //HOT                  //HOTTER              //HOTTEST
//                "Ice",    "Ice",          "Grassland",      "Desert",              "Desert",             "Desert",             //DRYEST
//                "Ice",    "Tundra",       "Grassland",      "Grassland",           "Desert",             "Desert",             //DRYER
//                "Ice",    "Tundra",       "Woodland",       "Woodland",            "Savanna",            "Desert",             //DRY
//                "Ice",    "Tundra",       "SeasonalForest", "SeasonalForest",      "Savanna",            "Savanna",            //WET
//                "Ice",    "Tundra",       "BorealForest",   "TemperateRainforest", "TropicalRainforest", "Savanna",            //WETTER
//                "Ice",    "BorealForest", "BorealForest",   "TemperateRainforest", "TropicalRainforest", "TropicalRainforest", //WETTEST
//                "Rocky",  "Rocky",        "Beach",          "Beach",               "Beach",              "Beach",              //COASTS
//                "Ice",    "River",        "River",          "River",               "River",              "River",              //RIVERS
//                "Ice",    "River",        "River",          "River",               "River",              "River",              //LAKES
//                "Ocean",  "Ocean",        "Ocean",          "Ocean",               "Ocean",              "Ocean",              //OCEAN
//                "Empty",                                                                                                       //SPACE

    }
    
    public WildMap()
    {
        this(128, 128, 21);
    }
    public WildMap(int width, int height, int biome)
    {
        this(width, height, biome, new SilkRNG());
    }
    public WildMap(int width, int height, int biome, int seedA, int seedB)
    {
        this(width, height, biome, new SilkRNG(seedA, seedB));
    }
    public WildMap(int width, int height, int biome, IStatefulRNG rng)
    {
        this(width, height, biome, rng, floorsByBiome(biome, rng), makeVegetation(rng, 70, 0.3, rng.getRandomElement(FakeLanguageGen.romanizedHumanLanguages)));
    }
    public WildMap(int width, int height, int biome, IStatefulRNG rng, ArrayList<String> floorTypes, ArrayList<String> contentTypes)
    {
        this.width = width;
        this.height = height;
        this.biome = biome;
        this.rng = rng;
        content = ArrayTools.fill(-1, width, height);
        floors = new int[width][height];
        this.floorTypes = floorTypes;
        this.contentTypes = contentTypes;
    }

    public void generate() {
        ArrayTools.fill(content, -1);
        final int seed = rng.nextInt(), otherSeed = rng.nextInt(), choice = seed + otherSeed & 15;
        final int limit = contentTypes.size(), floorLimit = floorTypes.size();
        int b;
        BlueNoise.blueSpill(floors, floorLimit, rng);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if((b = BlueNoise.getSeeded(x, y, seed, BlueNoise.ALT_NOISE[choice]) + 128) < limit)
                    content[x][y] = b;
                //floors[x][y] = (int)((FastNoise.instance.layered2D(x,  y, otherSeed, 2, 0x1p-5f) * 0.4999f + 0.5f) * (floorLimit - 1) + 0.25f + rng.nextFloat(0.5f));
            }
        }
    }
}
