package squidpony.squidgrid.mapping;

import squidpony.GwtCompatibility;
import squidpony.annotation.Beta;
import squidpony.squidmath.*;

import java.util.Arrays;

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
    public StatefulRNG rng;
    protected long rebuildSeed;
    protected boolean seedFixed = false;

    protected char[][] map = null;
    protected int[][] environment = null;
    private PacMazeGenerator mazeGenerator;
    public RegionMap<MapModule> layout, modules, inverseModules;
    private void putModule(short[] module)
    {
        MapModule mm = new MapModule(CoordPacker.unpackChar(module, '.', '#'));
        short[] b = CoordPacker.rectangle(1 + mm.max.x, 1 + mm.max.y);
        modules.put(b, mm);
        inverseModules.put(CoordPacker.negatePacked(b), mm);
    }
    private void putRectangle(int width, int height, float multiplier)
    {
        putModule(CoordPacker.rectangle(Math.round(width * multiplier), Math.round(height * multiplier)));
    }
    private void putCircle(int radius, float multiplier)
    {
        putModule(CoordPacker.circle(Coord.get(Math.round(radius * multiplier), Math.round(radius * multiplier)),
                Math.round(radius * multiplier),
                Math.round((radius+1)*2 * multiplier), Math.round((radius+1)*2 * multiplier)));
    }

    private void initModules()
    {
        layout = new RegionMap<>(64);
        modules = new RegionMap<>(64);
        inverseModules = new RegionMap<>(64);
        float multiplier = (float) Math.sqrt(Math.max(1f, Math.min(width, height) / 24f));
        putRectangle(2, 2, multiplier);
        putRectangle(3, 3, multiplier);
        putRectangle(4, 4, multiplier);
        putRectangle(4, 2, multiplier);
        putRectangle(2, 4, multiplier);
        putRectangle(6, 6, multiplier);
        putRectangle(6, 3, multiplier);
        putRectangle(3, 6, multiplier);
        putCircle(2, multiplier);

        putRectangle(8, 8, multiplier);
        putRectangle(6, 12, multiplier);
        putRectangle(12, 6, multiplier);
        putCircle(4, multiplier);

        putRectangle(14, 14, multiplier);
        putRectangle(9, 18, multiplier);
        putRectangle(18, 9, multiplier);
        putRectangle(14, 18, multiplier);
        putRectangle(18, 14, multiplier);
        putCircle(6, multiplier);
    }

    /**
     * Make a ModularMapGenerator with a StatefulRNG (backed by LightRNG) using a random seed, height 30, and width 60.
     */
    public ModularMapGenerator()
    {
        this(60, 30);
    }

    /**
     * Make a ModularMapGenerator with the given height and width; the RNG used for generating a dungeon and
     * adding features will be a StatefulRNG (backed by LightRNG) using a random seed.
     * @param width The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     */
    public ModularMapGenerator(int width, int height)
    {
        this(width, height, new StatefulRNG());
    }

    /**
     * Make a ModularMapGenerator with the given height, width, and RNG. Use this if you want to seed the RNG.
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
        for (int x = 0; x < this.width; x++) {
            Arrays.fill(map[x], '#');
        }
        mazeGenerator = new PacMazeGenerator(width, height, this.rng);
        initModules();
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
        initModules();
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
        MapModule mm;

        // you gave it a tiny map, what can it do?
        if(minDim < 16) {

            mm = rng.getRandomElement(modules.values().toList());
            map = GwtCompatibility.first(modules.allAt(rng.between(3, minDim), rng.between(3, minDim))).map;
            return DungeonUtility.wallWrap(map);
        }

        int frustration = 0;
        while ((mm = rng.getRandomElement(modules.allAt(rng.between(4, minDim), rng.between(4, minDim)))) == null
                        && frustration++ < 50)
        {}
        if(frustration >= 50 || mm == null)
        {
            mm = rng.getRandomElement(modules.values().toList());
            map = GwtCompatibility.first(modules.allAt(rng.between(3, minDim), rng.between(3, minDim))).map;
            return DungeonUtility.wallWrap(map);
        }
        // ok, mm is valid.

        int placeX = rng.nextInt(minDim - mm.max.x), placeY = rng.nextInt(minDim - mm.max.y);
        for (int x = 0; x < mm.max.x; x++) {
            System.arraycopy(mm.map[x], 0, map[x + placeX], placeY, mm.max.y);
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
