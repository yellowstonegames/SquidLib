package squidpony.squidgrid.mapping;

import squidpony.annotation.Beta;
import squidpony.squidmath.*;

/**
 * Generator for maps of high-tech areas like space stations or starships, with repeated modules laid out in random ways.
 * Different from traditional fantasy dungeon generation in that it should seem generally less chaotic in how it's laid
 * out, and repeated elements with minor tweaks should be especially common.
 * Created by Tommy Ettinger on 4/2/2016.
 */
@Beta
public class ModularMapGenerator {
    public DungeonUtility utility;
    protected int height, width;
    public StatefulRNG rng = new StatefulRNG(0x1337D1CE);
    protected long rebuildSeed;
    protected boolean seedFixed = false;

    protected char[][] map = null;
    protected int[][] environment = null;
    private PacMazeGenerator mazeGenerator;
    public static RegionMap<short[]> modules0 = new RegionMap<>(16),
            modules1 = new RegionMap<>(16), modules2 = new RegionMap<>(16);

    private static void putModule(RegionMap<short[]> modules, short[] module)
    {
        modules.put(CoordPacker.fringe(module, 1, 128, 128, false), module);
    }

    static {

        putModule(modules0, CoordPacker.rectangle(85 - 1, 85 - 1, 2, 2));
        putModule(modules0, CoordPacker.rectangle(85 - 1, 85 - 1, 3, 3));
        putModule(modules0, CoordPacker.rectangle(85 - 2, 85 - 2, 4, 4));
        putModule(modules0, CoordPacker.rectangle(85 - 1, 85 - 2, 2, 4));
        putModule(modules0, CoordPacker.rectangle(85 - 2, 85 - 1, 4, 2));
        putModule(modules0, CoordPacker.circle(Coord.get(85, 85), 2, 128, 128));

        putModule(modules1, CoordPacker.rectangle(85 - 4, 85 - 4, 8, 8));
        putModule(modules1, CoordPacker.rectangle(85 - 3, 85 - 6, 6, 12));
        putModule(modules1, CoordPacker.rectangle(85 - 6, 85 - 3, 12, 6));
        putModule(modules1, CoordPacker.circle(Coord.get(85, 85), 4, 128, 128));

        putModule(modules2, CoordPacker.rectangle(85 - 6, 85 - 6, 12, 12));
        putModule(modules2, CoordPacker.rectangle(85 - 4, 85 - 8, 8, 16));
        putModule(modules2, CoordPacker.rectangle(85 - 8, 85 - 4, 16, 8));
        putModule(modules2, CoordPacker.circle(Coord.get(85, 85), 6, 128, 128));
    }

    /**
     * Make a Mod with a LightRNG using a random seed, height 40, and width 40.
     */
    public ModularMapGenerator()
    {
        this(60, 30);
    }

    /**
     * Make a DungeonGenerator with the given height and width; the RNG used for generating a dungeon and
     * adding features will be a LightRNG using a random seed.
     * @param width The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     */
    public ModularMapGenerator(int width, int height)
    {
        this(width, height, new StatefulRNG());
    }

    /**
     * Make a DungeonGenerator with the given height, width, and RNG. Use this if you want to seed the RNG.
     * @param width The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     * @param rng The RNG to use for all purposes in this class; if it is a StatefulRNG, then it will be used as-is,
     *            but if it is not a StatefulRNG, a new StatefulRNG will be used, randomly seeded by this parameter
     */
    public ModularMapGenerator(int width, int height, RNG rng)
    {
        this.rng = (rng instanceof StatefulRNG) ? (StatefulRNG) rng : new StatefulRNG(rng.nextLong());
        utility = new DungeonUtility(this.rng);
        rebuildSeed = this.rng.getState();
        this.height = height;
        this.width = width;
        map = new char[width][height];
        environment = new int[width][height];
        mazeGenerator = new PacMazeGenerator(width, height, this.rng);
    }

    /**
     * Copies all fields from copying and makes a new DungeonGenerator.
     * @param copying the DungeonGenerator to copy
     */
    public ModularMapGenerator(ModularMapGenerator copying)
    {
        rng = new StatefulRNG(copying.rng.getState());
        utility = new DungeonUtility(rng);
        rebuildSeed = rng.getState();
        height = copying.height;
        width = copying.width;
        map = copying.map;
        environment = copying.environment;
        mazeGenerator = new PacMazeGenerator(width, height, rng);
    }
    /**
     * Get the most recently generated char[][] map out of this class. The
     * map may be null if generate() or setMap() have not been called.
     * @return a char[][] map, or null.
     */
    public char[][] getMap() {
        return map;
    }
    /**
     * Get the most recently generated char[][] map out of this class without any chars other than '#' or '.', for
     * walls and floors respectively. The map may be null if generate() or setMap() have not been called.
     * @return a char[][] map with only '#' for walls and '.' for floors, or null.
     */
    public char[][] getBareMap() {
        return DungeonUtility.simplifyDungeon(map);
    }

    public char[][] generate()
    {
        int minDim = Math.min(height, width), maxDim = Math.max(height, width), numCores, numOuter;
        RegionMap<short[]> core, outer;
        if(minDim >= 32) {
            core = modules2;
            if(rng.nextBoolean())
            {
                outer = modules1;
                numCores = (maxDim / 32) * (minDim / 32);
                numOuter = 2 * numCores;
            }
            else
            {
                outer = modules0;
                numCores = (maxDim / 32) * (minDim / 32);
                numOuter = 3 * numCores;
            }

        }
        else if(minDim >= 20)
        {
            core = modules1;
            outer = modules0;
            numCores = (maxDim / 20) * (minDim / 20);
            numOuter = 2 * numCores;
        }
        else // you gave it a tiny map, what can it do?
        {
            short[] tmp = rng.getRandomElement(modules0.keys().toList()), inner = modules0.get(tmp);
            Coord[] bnds = CoordPacker.bounds(tmp);
            map = CoordPacker.unpackChar(CoordPacker.translate(inner, -bnds[0].x, -bnds[0].y, width, height),
                    width, height, '.', '#');
            return DungeonUtility.wallWrap(map);
        }
        return map;
    }

    /**
     * Change the underlying char[][]; only affects the toString method, and of course getMap
     * @param map a char[][], probably produced by an earlier call to this class and then modified.
     */
    public void setMap(char[][] map) {
        this.map = map;
        if(map == null)
        {
            width = 0;
            height = 0;
            return;
        }
        width = map.length;
        if(width > 0)
            height = map[0].length;
    }

    /**
     * Height of the map in cells.
     * @return Height of the map in cells.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Width of the map in cells.
     * @return Width of the map in cells.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the environment int 2D array for use with classes like RoomFinder.
     * @return the environment int 2D array
     */
    public int[][] getEnvironment() {
        return environment;
    }

    /**
     * Sets the environment int 2D array.
     * @param environment a 2D array of int, where each int corresponds to a constant in MixedGenerator.
     */
    public void setEnvironment(int[][] environment) {
        this.environment = environment;
    }
}
