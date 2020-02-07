package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.Thesaurus;
import squidpony.annotation.Beta;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.BlueNoise;
import squidpony.squidmath.IRNG;
import squidpony.squidmath.IStatefulRNG;
import squidpony.squidmath.SilkRNG;

import java.io.Serializable;
import java.util.ArrayList;

import static squidpony.Maker.makeList;

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
    public final int width, height;
    public int biome;
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

    public static ArrayList<String> floorsByBiome(int biome, IRNG rng) {
        biome &= 1023;
        switch (biome) {
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
                return makeList("fresh water");
            case 54: //Ocean
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
                return makeList("salt water");
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
                return makeList("sand");
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
                return makeList("empty space");
        }
    }

    public static ArrayList<String> pathsByBiome(int biome) {
        biome &= 1023;
        switch (biome) {
            case 0: //Ice
            case 1:
            case 6:
            case 12:
            case 18:
            case 24:
            case 30:
            case 42:
            case 48:
                return makeList("snow path");
            case 7: //Tundra
            case 13:
            case 19:
            case 25: 
            case 26: //BorealForest
            case 31:
            case 32:
                return makeList("snow path", "dirt path");
//            case 43: //River
//            case 44:
//            case 45:
//            case 46:
//            case 47:
//            case 49:
//            case 50:
//            case 51:
//            case 52:
//            case 53:
//            case 54: //Ocean
//            case 55:
//            case 56:
//            case 57:
//            case 58:
//            case 59:
//                return makeList("wooden bridge");
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
                return makeList("sand path");
            case 2: //Grassland
            case 8:
            case 9:
            case 14: //Woodland
            case 15:
            case 16: //Savanna
            case 22:
            case 23:
            case 29:
                return makeList("dirt path");
            case 20: //SeasonalForest
            case 21:
                return makeList("dirt path", "grass path");
            case 27: //TemperateRainforest
            case 33:
            case 28: //TropicalRainforest
            case 34:
            case 35:
                return makeList("grass path");
            case 36: // Rocky
            case 37:
                return makeList("stone path");
            default:
                return makeList("wooden bridge");

        }
    }
    
    public static ArrayList<String> contentByBiome(int biome, IRNG rng) {
        biome &= 1023;
        switch (biome) {
            case 0: //Ice
            case 1:
            case 6:
            case 12:
            case 18:
            case 24:
            case 30:
            case 42:
            case 48:
                return makeShuffledRepeats(rng, "snow mound", 5, "icy divot", 2, "powder snowdrift", 5);
            case 7: //Tundra
            case 13:
            case 19:
            case 25:
                return makeShuffledRepeats(rng, "snow mound", 4, "hillock", 6, "animal burrow", 5, "small bush 1", 2);
            case 26: //BorealForest
            case 31:
            case 32:
                return makeShuffledRepeats(rng, "snow mound", 3, "small bush 1", 5, "large bush 1", 3, "evergreen tree 1", 17, "evergreen tree 2", 12);
//                case 43: //River
//                case 44:
//                case 45:
//                case 46:
//                case 47:
//                case 49:
//                case 50:
//                case 51:
//                case 52:
//                case 53:
//                case 54: //Ocean
//                case 55:
//                case 56:
//                case 57:
//                case 58:
//                case 59:
//                    return new ArrayList<>(0);
            case 3: //Desert
            case 4:
            case 5:
            case 10:
            case 11:
            case 17:
                return makeShuffledRepeats(rng, "small cactus 1", 2, "large cactus 1", 2, "succulent 1", 1, "animal burrow", 2);
            case 38: //Beach
            case 39:
            case 40:
            case 41:
                return makeShuffledRepeats(rng, "seashell 1", 3, "seashell 2", 3, "seashell 3", 3, "seashell 4", 3, "driftwood", 5, "boulder", 3);
            case 2: //Grassland
            case 8:
            case 9:
                return makeShuffledRepeats(rng, "deciduous tree 1", 3, "small bush 1", 5, "small bush 2", 4, "large bush 1", 5, "animal burrow", 8, "hillock", 4);
            case 14: //Woodland
            case 15:
                return makeShuffledRepeats(rng, "deciduous tree 1", 12, "deciduous tree 2", 9, "deciduous tree 3", 6, "small bush 1", 4, "small bush 2", 3, "animal burrow", 3);
            case 16: //Savanna
            case 22:
            case 23:
            case 29:
                return makeShuffledRepeats(rng, "small bush 1", 8, "small bush 2", 5, "large bush 1", 2, "animal burrow", 3, "hillock", 6);
            case 20: //SeasonalForest
            case 21:
                return makeShuffledRepeats(rng, "deciduous tree 1", 15, "deciduous tree 2", 13, "deciduous tree 3", 12, "small bush 1", 3, "large bush 1", 5, "large bush 2", 4, "animal burrow", 3);
            case 27: //TemperateRainforest
            case 33:
                return makeShuffledRepeats(rng, "tropical tree 1", 6, "tropical tree 2", 5, "deciduous tree 1", 13, "deciduous tree 2", 12, "small bush 1", 8, "large bush 1", 7, "large bush 2", 7, "large bush 3", 3, "animal burrow", 3);
            case 28: //TropicalRainforest
            case 34:
            case 35:
                return makeShuffledRepeats(rng, "tropical tree 1", 12, "tropical tree 2", 11, "tropical tree 3", 10, "tropical tree 4", 9, "small bush 1", 6, "small bush 2", 5, "large bush 1", 6, "large bush 2", 5, "large bush 3", 3, "animal burrow", 9, "boulder", 1);
            case 36: // Rocky
            case 37:
                return makeShuffledRepeats(rng, "seashell 1", 3, "seashell 2", 2, "seashell 3", 2, "driftwood", 6, "boulder", 9);
            default:
                return new ArrayList<>(0);
        }
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
        this(width, height, biome, rng, floorsByBiome(biome, rng), contentByBiome(biome, rng));
    }
    public WildMap(int width, int height, int biome, IStatefulRNG rng, ArrayList<String> contentTypes)
    {
        this(width, height, biome, rng, floorsByBiome(biome, rng), contentTypes);
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

    /**
     * Produces a map by filling the {@link #floors} 2D array with indices into {@link #floorTypes}, and similarly
     * filling the {@link #content} 2D array with indices into {@link #contentTypes}. You only need to call this method
     * when you first generate a map with the specific parameters you want, such as biome, and later if you want another
     * map with the same parameters.
     * <br>
     * Virtually all of this method is a wrapper around functionality provided by {@link BlueNoise}, adjusted to fit
     * wilderness maps slightly.
     */
    public void generate() {
        ArrayTools.fill(content, -1);
        final int seed = rng.nextInt();//, otherSeed = rng.nextInt(), choice = seed + otherSeed & 15;
        final int limit = contentTypes.size(), floorLimit = floorTypes.size();
        int b;
        BlueNoise.blueSpill(floors, floorLimit, rng);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if((b = BlueNoise.getSeededScratchy(x, y, seed) + 128) < limit)
                    content[x][y] = b;
                //floors[x][y] = (int)((FastNoise.instance.layered2D(x,  y, otherSeed, 2, 0x1p-5f) * 0.4999f + 0.5f) * (floorLimit - 1) + 0.25f + rng.nextFloat(0.5f));
            }
        }
    }
    
    public static class MixedWildMap extends WildMap implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public final int[][] pieceMap;
        public final WildMap[] pieces;
        protected final int[] minFloors, maxFloors, minContents, maxContents;
        
        public MixedWildMap()
        {
            this(new WildMap(), new WildMap(), new WildMap(), new WildMap(), new SilkRNG());
        }
        
        public MixedWildMap(WildMap northeast, WildMap southeast, WildMap southwest, WildMap northwest, IStatefulRNG rng)
        {
            super(northeast.width, northeast.height, northeast.biome, rng, new ArrayList<>(northeast.floorTypes), new ArrayList<>(northeast.contentTypes));
            minFloors = new int[4];
            maxFloors = new int[4];
            minContents = new int[4];
            maxContents = new int[4];
            floorTypes.addAll(southeast.floorTypes);
            floorTypes.addAll(southwest.floorTypes);
            floorTypes.addAll(northwest.floorTypes);
            contentTypes.addAll(southeast.contentTypes);
            contentTypes.addAll(southwest.contentTypes);
            contentTypes.addAll(northwest.contentTypes);
            minFloors[1]   = maxFloors[0]   = northeast.floorTypes.size();
            minContents[1] = maxContents[0] = northeast.contentTypes.size();
            minFloors[2]   = maxFloors[1]   = maxFloors[0]    + southeast.floorTypes.size();
            minContents[2] = maxContents[1] = maxContents[0]  + southeast.contentTypes.size();
            minFloors[3]   = maxFloors[2]   = maxFloors[1]    + southwest.floorTypes.size();
            minContents[3] = maxContents[2] = maxContents[1]  + southwest.contentTypes.size();
            maxFloors[3]   = maxFloors[2]    + northwest.floorTypes.size();
            maxContents[3] = maxContents[2]  + northwest.contentTypes.size();
            pieces = new WildMap[]{northeast, southeast, southwest, northwest};
            pieceMap = new int[width][height];
        }
        
        protected void preparePieceMap()
        {
            ArrayTools.fill(pieceMap, 255);
            pieceMap[width - 1][0] = 0; // northeast
            pieceMap[width - 1][height - 1] = 1; // southeast
            pieceMap[0][height - 1] = 2; // southwest
            pieceMap[0][0] = 3; //northwest
            final int spillerLimit = 4;
            final Direction[] dirs = Direction.CARDINALS;
            Direction d;
            int t, rx, ry, ctr;
            int[] ox = new int[width], oy = new int[height];
            boolean anySuccesses = false;
            do {
                ctr = 0;
                rng.randomOrdering(width, ox);
                rng.randomOrdering(height, oy);
                for (int x = 0; x < width; x++) {
                    rx = ox[x];
                    for (int y = 0; y < height; y++) {
                        ry = oy[y];
                        if ((t = pieceMap[rx][ry]) < spillerLimit) {
                            d = dirs[rng.next(2)];
                            if (rx + d.deltaX >= 0 && rx + d.deltaX < width && ry + d.deltaY >= 0 && ry + d.deltaY < height &&
                                    pieceMap[rx + d.deltaX][ry + d.deltaY] >= spillerLimit) {
                                pieceMap[rx + d.deltaX][ry + d.deltaY] = t;
                                ctr++;
                            }
                            d = dirs[rng.next(2)];
                            if (rx + d.deltaX >= 0 && rx + d.deltaX < width && ry + d.deltaY >= 0 && ry + d.deltaY < height &&
                                    pieceMap[rx + d.deltaX][ry + d.deltaY] >= spillerLimit) {
                                pieceMap[rx + d.deltaX][ry + d.deltaY] = t;
                                ctr++;
                            }
                        }

                    }
                }
                if(!anySuccesses && ctr == 0)
                {
                    ArrayTools.fill(pieceMap, 0);
                    return;
                }
                else
                    anySuccesses = true;
            } while (ctr > 0);
            do {
                ctr = 0;
                rng.randomOrdering(width, ox);
                rng.randomOrdering(height, oy);
                for (int x = 0; x < width; x++) {
                    rx = ox[x];
                    for (int y = 0; y < height; y++) {
                        ry = oy[y];
                        if ((t = pieceMap[rx][ry]) < spillerLimit) {
                            for (int i = 0; i < 4; i++) {
                                d = dirs[i];
                                if (rx + d.deltaX >= 0 && rx + d.deltaX < width && ry + d.deltaY >= 0 && ry + d.deltaY < height &&
                                        pieceMap[rx + d.deltaX][ry + d.deltaY] >= spillerLimit) {
                                    pieceMap[rx + d.deltaX][ry + d.deltaY] = t;
                                    ctr++;
                                }
                            }
                        }

                    }
                }
            } while (ctr > 0);

        }

        @Override
        public void generate() {
            ArrayTools.fill(content, -1);
            for (int i = 0; i < pieces.length; i++) {
                pieces[i].generate();
            }
            preparePieceMap();
            int p, c;
            WildMap piece;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    p = pieceMap[x][y];
                    piece = pieces[p];
                    floors[x][y] = piece.floors[x][y] + minFloors[p];
                    if((c = piece.content[x][y]) >= 0)
                    {
                        content[x][y] = c + minContents[p];
                    }
                }
            }
        }
    }
}
