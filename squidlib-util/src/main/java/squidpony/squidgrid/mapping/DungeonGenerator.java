package squidpony.squidgrid.mapping;

import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.mapping.styled.DungeonBoneGen;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.EnumMap;

/**
 * The primary way to create a more-complete dungeon, layering different effects and modifications on top of
 * a DungeonBoneGen's dungeon or another dungeon without such effects. Also ensures only connected regions of the map
 * are used by filling unreachable areas with walls, and can find far-apart staircase positions if generate() is used or
 * can keep existing staircases in a map if generateRespectingStairs() is used.
 * <br>
 * The main technique for using this is simple: Construct a DungeonGenerator, usually with the desired width and height,
 * then call any feature adding methods that you want in the dungeon, like addWater(), addTraps, addGrass(), or
 * addDoors(). Some of these take different parameters, like addDoors() which need to know if it should check openings
 * that are two cells wide to add a door and a wall to, or whether it should only add doors to single-cell openings.
 * Then call generate() to get a char[][] with the desired dungeon map, using a fixed repertoire of chars to represent
 * the different features. After calling generate(), you can safely get the values from the stairsUp and stairsDown
 * fields, which are Coords that should be a long distance from each other but connected in the dungeon. You may want
 * to change those to staircase characters, but there's no requirement to do anything with them. It's recommended that
 * you keep the resulting char[][] maps in some collection that can be saved, since DungeonGenerator only stores a
 * temporary copy of the most recently-generated map. The DungeonUtility field of this class, utility, is a convenient
 * way of accessing the non-static methods in that class, such as randomFloor(), without needing to create another
 * DungeonUtility (this class creates one, so you don't have to).
 * <br>
 * Previews for the kinds of dungeon this generates, given a certain argument to generate():
 * <ul>
 *     <li>Using TilesetType.DEFAULT_DUNGEON (text, click "Raw", may need to zoom out): https://gist.github.com/tommyettinger/a3bd413b903f2e103541</li>
 *     <li>Using TilesetType.DEFAULT_DUNGEON (graphical, scroll down and to the right): http://tommyettinger.github.io/home/PixVoxel/dungeon/dungeon.html</li>
 *     <li>Using SerpentMapGenerator.generate()  (text, click "Raw", may need to zoom out): https://gist.github.com/tommyettinger/93b47048fc8a209a9712</li>
 * </ul>
 * <br>
 * As of March 6, 2016, the algorithm this uses to place water and grass was swapped for a more precise version. You no
 * longer need to give this 150% in addWater or addGrass to effectively produce 100% water or grass, and this should be
 * no more than 1.1% different from the percentage you request for any effects. If you need to reproduce dungeons with
 * the same seed and get the same (imprecise) results as before this change, it's probably not possible unless you save
 * the previously-generated char[][] dungeons, since several other things may have changed as well.
 * @see squidpony.squidgrid.mapping.DungeonUtility this class exposes a DungeonUtility member; DungeonUtility also has many useful static methods
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
public class DungeonGenerator implements IDungeonGenerator {
    /**
     * The effects that can be applied to this dungeon. More may be added in future releases.
     */
    public enum FillEffect
    {
        /**
         * Water, represented by '~'
         */
        WATER,
        /**
         * Doors, represented by '+' for east-to-west connections or '/' for north-to-south ones.
         */
        DOORS,
        /**
         * Traps, represented by '^'
         */
        TRAPS,
        /**
         * Grass, represented by '"'
         */
        GRASS,
        /**
         * Boulders strewn about open areas, represented by '#' and treated as walls
         */
        BOULDERS,
        /**
         * Islands of ground, '.', surrounded by shallow water, ',', to place in water at evenly spaced points
         */
        ISLANDS
    }

    /**
     * The effects that will be applied when generate is called. Strongly prefer using addWater, addDoors, addTraps,
     * and addGrass.
     */
    public EnumMap<FillEffect, Integer> fx;
    protected DungeonBoneGen gen;
    public DungeonUtility utility;
    protected int height, width;
    public Coord stairsUp = null, stairsDown = null;
    public StatefulRNG rng;
    protected long rebuildSeed;
    protected boolean seedFixed = false;

    protected char[][] dungeon = null;

    /**
     * Get the most recently generated char[][] dungeon out of this class. The
     * dungeon may be null if generate() or setDungeon() have not been called.
     * @return a char[][] dungeon, or null.
     */
    public char[][] getDungeon() {
        return dungeon;
    }
    /**
     * Get the most recently generated char[][] dungeon out of this class without any chars other than '#' or '.', for
     * walls and floors respectively. The dungeon may be null if generate() or setDungeon() have not been called.
     * @return a char[][] dungeon with only '#' for walls and '.' for floors, or null.
     */
    public char[][] getBareDungeon() {
        return DungeonUtility.simplifyDungeon(dungeon);
    }

    /**
     * Change the underlying char[][]; only affects the toString method, and of course getDungeon.
     * @param dungeon a char[][], probably produced by an earlier call to this class and then modified.
     */
    public void setDungeon(char[][] dungeon) {
        this.dungeon = dungeon;
        if(dungeon == null)
        {
            width = 0;
            height = 0;
            return;
        }
        width = dungeon.length;
        if(width > 0)
            height = dungeon[0].length;
    }

    /**
     * Height of the dungeon in cells.
     * @return Height of the dungeon in cells.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Width of the dungeon in cells.
     * @return Width of the dungeon in cells.
     */
    public int getWidth() {
        return width;
    }


    /**
     * Make a DungeonGenerator with a LightRNG using a random seed, height 40, and width 40.
     */
    public DungeonGenerator()
    {
        rng = new StatefulRNG();
        gen = new DungeonBoneGen(rng);
        utility = new DungeonUtility(rng);
        rebuildSeed = rng.getState();
        height = 40;
        width = 40;
        fx = new EnumMap<>(FillEffect.class);
    }

    /**
     * Make a DungeonGenerator with the given height and width; the RNG used for generating a dungeon and
     * adding features will be a LightRNG using a random seed. If width or height is greater than 256, then this will
     * expand the Coord pool from its 256x256 default so it stores a reference to each Coord that might be used in the
     * creation of the dungeon (if width and height are 300 and 300, the Coord pool will be 300x300; if width and height
     * are 500 and 100, the Coord pool will be 500x256 because it won't shrink below the default size of 256x256).
     * @param width The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     */
    public DungeonGenerator(int width, int height)
    {
    	this(width, height, new RNG());
    }

    /**
     * Make a DungeonGenerator with the given height, width, and RNG. Use this if you want to seed the RNG. If width or
     * height is greater than 256, then this will expand the Coord pool from its 256x256 default so it stores a
     * reference to each Coord that might be used in the creation of the dungeon (if width and height are 300 and 300,
     * the Coord pool will be 300x300; if width and height are 500 and 100, the Coord pool will be 500x256 because it
     * won't shrink below the default size of 256x256).
     * @param width The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     * @param rng The RNG to use for all purposes in this class; if it is a StatefulRNG, then it will be used as-is,
     *            but if it is not a StatefulRNG, a new StatefulRNG will be used, randomly seeded by this parameter
     */
    public DungeonGenerator(int width, int height, RNG rng)
    {
        Coord.expandPoolTo(width, height);
        this.rng = (rng instanceof StatefulRNG) ? (StatefulRNG) rng : new StatefulRNG(rng.nextLong());
        gen = new DungeonBoneGen(this.rng);
        utility = new DungeonUtility(this.rng);
        rebuildSeed = this.rng.getState();
        this.height = height;
        this.width = width;
        fx = new EnumMap<>(FillEffect.class);
    }

    /**
     * Copies all fields from copying and makes a new DungeonGenerator.
     * @param copying the DungeonGenerator to copy
     */
    public DungeonGenerator(DungeonGenerator copying)
    {
        rng = new StatefulRNG(copying.rng.getState());
        gen = new DungeonBoneGen(rng);
        utility = new DungeonUtility(rng);
        rebuildSeed = rng.getState();
        height = copying.height;
        width = copying.width;
        Coord.expandPoolTo(width, height);
        fx = new EnumMap<>(copying.fx);
        dungeon = copying.dungeon;
    }

    /**
     * Turns the majority of the given percentage of floor cells into water cells, represented by '~'. Water will be
     * clustered into a random number of pools, with more appearing if needed to fill the percentage.
     * Each pool will have randomized volume that should fill or get very close to filling the requested
     * percentage, unless the pools encounter too much tight space. If this DungeonGenerator previously had addWater
     * called, the latest call will take precedence. No islands will be placed with this variant, but the edge of the
     * water will be shallow, represented by ','.
     * @param percentage the percentage of floor cells to fill with water
     * @return this DungeonGenerator; can be chained
     */
    public DungeonGenerator addWater(int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        if(fx.containsKey(FillEffect.WATER)) fx.remove(FillEffect.WATER);
        fx.put(FillEffect.WATER, percentage);
        return this;
    }
    /**
     * Turns the majority of the given percentage of floor cells into water cells, represented by '~'. Water will be
     * clustered into a random number of pools, with more appearing if needed to fill the percentage. Each pool will
     * have randomized volume that should fill or get very close to filling the requested percentage,
     * unless the pools encounter too much tight space. If this DungeonGenerator previously had addWater called, the
     * latest call will take precedence. If islandSpacing is greater than 1, then this will place islands of floor, '.',
     * surrounded by shallow water, ',', at about the specified distance with Euclidean measurement.
     * @param percentage the percentage of floor cells to fill with water
     * @param islandSpacing if greater than 1, islands will be placed randomly this many cells apart.
     * @return this DungeonGenerator; can be chained
     */
    public DungeonGenerator addWater(int percentage, int islandSpacing)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        if(fx.containsKey(FillEffect.WATER)) fx.remove(FillEffect.WATER);
        fx.put(FillEffect.WATER, percentage);
        if(fx.containsKey(FillEffect.ISLANDS)) fx.remove(FillEffect.ISLANDS);
        if(islandSpacing > 1)
            fx.put(FillEffect.ISLANDS, islandSpacing);
        return this;
    }

    /**
     * Turns the majority of the given percentage of floor cells into grass cells, represented by '"'. Grass will be
     * clustered into a random number of patches, with more appearing if needed to fill the percentage. Each area will
     * have randomized volume that should fill or get very close to filling the requested percentage,
     * unless the patches encounter too much tight space. If this DungeonGenerator previously had addGrass called, the
     * latest call will take precedence.
     * @param percentage the percentage of floor cells to fill with grass
     * @return this DungeonGenerator; can be chained
     */
    public DungeonGenerator addGrass(int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        if(fx.containsKey(FillEffect.GRASS)) fx.remove(FillEffect.GRASS);
        fx.put(FillEffect.GRASS, percentage);
        return this;
    }
    /**
     * Turns the given percentage of floor cells not already adjacent to walls into wall cells, represented by '#'.
     * If this DungeonGenerator previously had addBoulders called, the latest call will take precedence.
     * @param percentage the percentage of floor cells not adjacent to walls to fill with boulders.
     * @return this DungeonGenerator; can be chained
     */
    public DungeonGenerator addBoulders(int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        if(fx.containsKey(FillEffect.BOULDERS)) fx.remove(FillEffect.BOULDERS);
        fx.put(FillEffect.BOULDERS, percentage);
        return this;
    }
    /**
     * Turns the given percentage of viable doorways into doors, represented by '+' for doors that allow travel along
     * the x-axis and '/' for doors that allow travel along the y-axis. If doubleDoors is true,
     * 2-cell-wide openings will be considered viable doorways and will fill one cell with a wall, the other a door.
     * If this DungeonGenerator previously had addDoors called, the latest call will take precedence.
     * @param percentage the percentage of valid openings to corridors to fill with doors; should be between 10 and
     *                   20 if you want doors to appear more than a few times, but not fill every possible opening.
     * @param doubleDoors true if you want two-cell-wide openings to receive a door and a wall; false if only
     *                    one-cell-wide openings should receive doors. Usually, this should be true.
     * @return this DungeonGenerator; can be chained
     */
    public DungeonGenerator addDoors(int percentage, boolean doubleDoors)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        if(doubleDoors) percentage *= -1;
        if(fx.containsKey(FillEffect.DOORS)) fx.remove(FillEffect.DOORS);
        fx.put(FillEffect.DOORS, percentage);
        return this;
    }

    /**
     * Turns the given percentage of open area floor cells into trap cells, represented by '^'. Corridors that have no
     * possible way to move around a trap will not receive traps, ever. If this DungeonGenerator previously had
     * addTraps called, the latest call will take precedence.
     * @param percentage the percentage of valid cells to fill with traps; should be no higher than 5 unless
     *                   the dungeon floor is meant to be a kill screen or minefield.
     * @return this DungeonGenerator; can be chained
     */
    public DungeonGenerator addTraps(int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        if(fx.containsKey(FillEffect.TRAPS)) fx.remove(FillEffect.TRAPS);
        fx.put(FillEffect.TRAPS, percentage);
        return this;
    }

    /**
     * Removes any door, water, or trap insertion effects that this DungeonGenerator would put in future dungeons.
     * @return this DungeonGenerator, with all effects removed. Can be chained.
     */
    public DungeonGenerator clearEffects()
    {
        fx.clear();
        return this;
    }

    protected OrderedSet<Coord> removeAdjacent(OrderedSet<Coord> coll, Coord pt)
    {
        for(Coord temp : new Coord[]{Coord.get(pt.x + 1, pt.y), Coord.get(pt.x - 1, pt.y),
                Coord.get(pt.x, pt.y + 1), Coord.get(pt.x, pt.y - 1)})
        {
            if(coll.contains(temp) && !(temp.x == pt.x && temp.y == pt.y))
                coll.remove(temp);
        }

        return coll;
    }
    protected OrderedSet<Coord> removeAdjacent(OrderedSet<Coord> coll, Coord pt1, Coord pt2)
    {

        for(Coord temp : new Coord[]{Coord.get(pt1.x + 1, pt1.y), Coord.get(pt1.x - 1, pt1.y),
                Coord.get(pt1.x, pt1.y + 1), Coord.get(pt1.x, pt1.y - 1),
                Coord.get(pt2.x + 1, pt2.y), Coord.get(pt2.x - 1, pt2.y),
                Coord.get(pt2.x, pt2.y + 1), Coord.get(pt2.x, pt2.y - 1),})
        {
            if(coll.contains(temp) && !(temp.x == pt1.x && temp.y == pt1.y) && !(temp.x == pt2.x && temp.y == pt2.y))
                coll.remove(temp);
        }

        return coll;
    }
    protected OrderedSet<Coord> viableDoorways(boolean doubleDoors, char[][] map)
    {
        OrderedSet<Coord> doors = new OrderedSet<>();
        OrderedSet<Coord> blocked = new OrderedSet<>(4);
        DijkstraMap dm = new DijkstraMap(map, DijkstraMap.Measurement.EUCLIDEAN);
        for(int x = 1; x < map.length - 1; x++) {
            for (int y = 1; y < map[x].length - 1; y++) {
                if(map[x][y] == '#')
                    continue;
                if (doubleDoors) {
                    if (x >= map.length - 2 || y >= map[x].length - 2)
                        continue;
                    else {
                        if (map[x+1][y] != '#' &&
                                map[x + 2][y] == '#' && map[x - 1][y] == '#'
                                && map[x][y + 1] != '#' && map[x][y - 1] != '#'
                                && map[x+1][y + 1] != '#' && map[x+1][y - 1] != '#') {
                            if (map[x + 2][y + 1] != '#' || map[x - 1][y + 1] != '#' || map[x + 2][y - 1] != '#' || map[x - 1][y - 1] != '#') {
                                dm.resetMap();
                                dm.clearGoals();
                                dm.setGoal(x, y+1);
                                blocked.clear();
                                blocked.add(Coord.get(x, y));
                                blocked.add(Coord.get(x + 1, y));
                                if(dm.partialScan(16, blocked)[x][y-1] < DijkstraMap.FLOOR)
                                    continue;
                                doors.add(Coord.get(x, y));
                                doors.add(Coord.get(x + 1, y));
                                doors = removeAdjacent(doors, Coord.get(x, y), Coord.get(x + 1, y));
                                continue;
                            }
                        } else if (map[x][y+1] != '#' &&
                                map[x][y + 2] == '#' && map[x][y - 1] == '#'
                                && map[x + 1][y] != '#' && map[x - 1][y] != '#'
                                && map[x + 1][y+1] != '#' && map[x - 1][y+1] != '#') {
                            if (map[x + 1][y + 2] != '#' || map[x + 1][y - 1] != '#' || map[x - 1][y + 2] != '#' || map[x - 1][y - 1] != '#') {
                                dm.resetMap();
                                dm.clearGoals();
                                dm.setGoal(x+1, y);
                                blocked.clear();
                                blocked.add(Coord.get(x, y));
                                blocked.add(Coord.get(x, y+1));
                                if(dm.partialScan(16, blocked)[x-1][y] < DijkstraMap.FLOOR)
                                    continue;
                                doors.add(Coord.get(x, y));
                                doors.add(Coord.get(x, y+1));
                                doors = removeAdjacent(doors, Coord.get(x, y), Coord.get(x, y + 1));
                                continue;
                            }
                        }
                    }
                }
                if (map[x + 1][y] == '#' && map[x - 1][y] == '#' && map[x][y + 1] != '#' && map[x][y - 1] != '#') {
                    if (map[x + 1][y + 1] != '#' || map[x - 1][y + 1] != '#' || map[x + 1][y - 1] != '#' || map[x - 1][y - 1] != '#') {
                        dm.resetMap();
                        dm.clearGoals();
                        dm.setGoal(x, y+1);
                        blocked.clear();
                        blocked.add(Coord.get(x, y));
                        if(dm.partialScan(16, blocked)[x][y-1] < DijkstraMap.FLOOR)
                            continue;
                        doors.add(Coord.get(x, y));
                        doors = removeAdjacent(doors, Coord.get(x, y));
                    }
                } else if (map[x][y + 1] == '#' && map[x][y - 1] == '#' && map[x + 1][y] != '#' && map[x - 1][y] != '#') {
                    if (map[x + 1][y + 1] != '#' || map[x + 1][y - 1] != '#' || map[x - 1][y + 1] != '#' || map[x - 1][y - 1] != '#') {
                        dm.resetMap();
                        dm.clearGoals();
                        dm.setGoal(x+1, y);
                        blocked.clear();
                        blocked.add(Coord.get(x, y));
                        if(dm.partialScan(16, blocked)[x-1][y] < DijkstraMap.FLOOR)
                            continue;
                        doors.add(Coord.get(x, y));
                        doors = removeAdjacent(doors, Coord.get(x, y));
                    }
                }

            }
        }

        return doors;
    }

    /**
     * Generate a char[][] dungeon using TilesetType.DEFAULT_DUNGEON; this produces a dungeon appropriate for a level
     * of ruins or a partially constructed dungeon. This uses '#' for walls, '.' for floors, '~' for deep water, ',' for
     * shallow water, '^' for traps, '+' for doors that provide horizontal passage, and '/' for doors that provide
     * vertical passage. Use the addDoors, addWater, addGrass, and addTraps methods of this class to request these in
     * the generated map.
     * Also sets the fields stairsUp and stairsDown to two randomly chosen, distant, connected, walkable cells.
     * @return a char[][] dungeon
     */
    public char[][] generate() {
        return generate(TilesetType.DEFAULT_DUNGEON);
    }

    /**
     * Generate a char[][] dungeon given a TilesetType; the comments in that class provide some opinions on what
     * each TilesetType value could be used for in a game. This uses '#' for walls, '.' for floors, '~' for deep water,
     * ',' for shallow water, '^' for traps, '+' for doors that provide horizontal passage, and '/' for doors that
     * provide vertical passage. Use the addDoors, addWater, addGrass, and addTraps methods of this class to request
     * these in the generated map.
     * Also sets the fields stairsUp and stairsDown to two randomly chosen, distant, connected, walkable cells.
     * @see squidpony.squidgrid.mapping.styled.TilesetType
     * @param kind a TilesetType enum value, such as TilesetType.DEFAULT_DUNGEON
     * @return a char[][] dungeon
     */
    public char[][] generate(TilesetType kind)
    {
        seedFixed = true;
        rebuildSeed = rng.getState();
        return generate(gen.generate(kind, width, height));
    }

    /**
     * Generate a char[][] dungeon with extra features given a baseDungeon that has already been generated.
     * Typically, you want to call generate with a TilesetType or no argument for the easiest generation; this method
     * is meant for adding features like water and doors to existing simple maps.
     * This uses '#' for walls, '.' for floors, '~' for deep water, ',' for shallow water, '^' for traps, '+' for doors
     * that provide horizontal passage, and '/' for doors that provide vertical passage.
     * Use the addDoors, addWater, addGrass, and addTraps methods of this class to request these in the generated map.
     * Also sets the fields stairsUp and stairsDown to two randomly chosen, distant, connected, walkable cells.
     * <br>
     * Special behavior here: If tab characters are present in the 2D char array, they will be replaced with '.' in the
     * final dungeon, but will also be tried first as valid staircase locations (with a high distance possible to travel
     * away from the starting staircase). If no tab characters are present this will search for '.' floors to place
     * stairs on, as normal. This tab-first behavior is useful in conjunction with some methods that establish a good
     * path in an existing dungeon; an example is {@code DungeonUtility.ensurePath(dungeon, rng, '\t', '#');} then
     * passing dungeon (which that code modifies) in as baseDungeon to this method.
     * @param baseDungeon a pre-made dungeon consisting of '#' for walls and '.' for floors; may be modified in-place
     * @return a char[][] dungeon
     */
    public char[][] generate(char[][] baseDungeon)
    {
        if(!seedFixed)
        {
            rebuildSeed = rng.getState();
        }
        seedFixed = false;
        char[][] map = DungeonUtility.wallWrap(baseDungeon);
        width = map.length;
        height = map[0].length;
        DijkstraMap dijkstra = new DijkstraMap(map);
        int frustrated = 0;
        do {
            dijkstra.clearGoals();
            stairsUp = utility.randomMatchingTile(map, '\t');
            if(stairsUp == null) {
                stairsUp = utility.randomFloor(map);
                if (stairsUp == null) {
                    frustrated++;
                    continue;
                }
            }
            dijkstra.setGoal(stairsUp);
            dijkstra.scan(null);
            frustrated++;
        }while (dijkstra.getMappedCount() < width + height && frustrated < 8);
        if(frustrated >= 8)
        {
            return generate();
        }
        double maxDijkstra = 0.0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (dijkstra.gradientMap[i][j] >= DijkstraMap.FLOOR) {
                    map[i][j] = '#';
                } else if (dijkstra.gradientMap[i][j] > maxDijkstra) {
                    maxDijkstra = dijkstra.gradientMap[i][j];
                }
                if (map[i][j] == '\t') {
                    map[i][j] = '.';
                }
            }
        }
        stairsDown = new GreasedRegion(dijkstra.gradientMap, maxDijkstra * 0.7,
                DijkstraMap.FLOOR).singleRandom(rng);

        return innerGenerate(map);
    }

    /**
     * Generate a char[][] dungeon with extra features given a baseDungeon that has already been generated, and that
     * already has staircases represented by greater than and less than signs.
     * Typically, you want to call generate with a TilesetType or no argument for the easiest generation; this method
     * is meant for adding features like water and doors to existing simple maps.
     * This uses '#' for walls, '.' for floors, '~' for deep water, ',' for shallow water, '^' for traps, '+' for doors
     * that provide horizontal passage, and '/' for doors that provide vertical passage.
     * Use the addDoors, addWater, addGrass, and addTraps methods of this class to request these in the generated map.
     * Also sets the fields stairsUp and stairsDown to null, and expects stairs to be already handled.
     * @param baseDungeon a pre-made dungeon consisting of '#' for walls and '.' for floors, with stairs already in;
     *                    may be modified in-place
     * @return a char[][] dungeon
     */
    public char[][] generateRespectingStairs(char[][] baseDungeon) {
        if(!seedFixed)
        {
            rebuildSeed = rng.getState();
        }
        seedFixed = false;
        char[][] map = DungeonUtility.wallWrap(baseDungeon);
        DijkstraMap dijkstra = new DijkstraMap(map);
        stairsUp = null;
        stairsDown = null;

        dijkstra.clearGoals();
        ArrayList<Coord> stairs = DungeonUtility.allMatching(map, '<', '>');
        for (int j = 0; j < stairs.size(); j++) {
            dijkstra.setGoal(stairs.get(j));
        }
        dijkstra.scan(null);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (dijkstra.gradientMap[i][j] >= DijkstraMap.FLOOR) {
                    map[i][j] = '#';
                }
            }
        }
        return innerGenerate(map);
    }




    private char[][] innerGenerate(char[][] map)
    {
        OrderedSet<Coord> doorways;
        OrderedSet<Coord> hazards = new OrderedSet<>();
        Coord temp;
        boolean doubleDoors = false;
        int floorCount = DungeonUtility.countCells(map, '.'),
                doorFill = 0,
                waterFill = 0,
                grassFill = 0,
                trapFill = 0,
                boulderFill = 0,
                islandSpacing = 0;
        if(fx.containsKey(FillEffect.DOORS))
        {
            doorFill = fx.get(FillEffect.DOORS);
            if(doorFill < 0)
            {
                doubleDoors = true;
                doorFill *= -1;
            }
        }
        if(fx.containsKey(FillEffect.GRASS)) {
            grassFill = fx.get(FillEffect.GRASS);
        }
        if(fx.containsKey(FillEffect.WATER)) {
            waterFill = fx.get(FillEffect.WATER);
        }
        if(fx.containsKey(FillEffect.BOULDERS)) {
            boulderFill = fx.get(FillEffect.BOULDERS) * floorCount / 100;
        }
        if(fx.containsKey(FillEffect.ISLANDS)) {
            islandSpacing = fx.get(FillEffect.ISLANDS);
        }
        if(fx.containsKey(FillEffect.TRAPS)) {
            trapFill = fx.get(FillEffect.TRAPS);
        }

        doorways = viableDoorways(doubleDoors, map);

        OrderedSet<Coord> obstacles = new OrderedSet<>(doorways.size() * doorFill / 100);
        if(doorFill > 0)
        {
            int total = doorways.size() * doorFill / 100;

            BigLoop:
            for(int i = 0; i < total; i++)
            {
                Coord entry = rng.getRandomElement(doorways);
                if(map[entry.x][entry.y] == '<' || map[entry.x][entry.y] == '>')
                    continue;
                if(map[entry.x - 1][entry.y] != '#' && map[entry.x + 1][entry.y] != '#')
                {
                    map[entry.x][entry.y] = '+';
                }
                else {
                    map[entry.x][entry.y] = '/';
                }
                obstacles.add(entry);
                Coord[] adj = new Coord[]{Coord.get(entry.x + 1, entry.y), Coord.get(entry.x - 1, entry.y),
                        Coord.get(entry.x, entry.y + 1), Coord.get(entry.x, entry.y - 1)};
                for(Coord near : adj) {
                    if (doorways.contains(near)) {
                        map[near.x][near.y] = '#';
                        obstacles.add(near);
                        doorways.remove(near);
                        i++;
                        doorways.remove(entry);
                        continue BigLoop;
                    }
                }
                doorways.remove(entry);
            }
        }
        if (boulderFill > 0.0) {
            /*
            short[] floor = pack(map, '.');
            short[] viable = retract(floor, 1, width, height, true);
            ArrayList<Coord> boulders = randomPortion(viable, boulderFill, rng);
            for (Coord boulder : boulders) {
                map[boulder.x][boulder.y] = '#';
            }
            */
            Coord[] boulders = new GreasedRegion(map, '.').retract8way(1).randomPortion(rng, boulderFill);
            Coord t;
            for (int i = 0; i < boulders.length; i++) {
                t = boulders[i];
                map[t.x][t.y] = '#';
            }
        }


        if(trapFill > 0) {
            for (int x = 1; x < map.length - 1; x++) {
                for (int y = 1; y < map[x].length - 1; y++) {
                    temp = Coord.get(x, y);
                    if (map[x][y] == '.' && !obstacles.contains(temp)) {
                        int ctr = 0;
                        if (map[x + 1][y] != '#') ++ctr;
                        if (map[x - 1][y] != '#') ++ctr;
                        if (map[x][y + 1] != '#') ++ctr;
                        if (map[x][y - 1] != '#') ++ctr;
                        if (map[x + 1][y + 1] != '#') ++ctr;
                        if (map[x - 1][y + 1] != '#') ++ctr;
                        if (map[x + 1][y - 1] != '#') ++ctr;
                        if (map[x - 1][y - 1] != '#') ++ctr;
                        if (ctr >= 5) hazards.add(Coord.get(x, y));
                    }
                }
            }
        }
        GreasedRegion floors = new GreasedRegion(map, '.'), working = new GreasedRegion(width, height);
        floorCount = floors.size();
        float waterRate = waterFill / 100.0f, grassRate = grassFill / 100.0f;
        if(waterRate + grassRate > 1.0f)
        {
            waterRate /= (waterFill + grassFill) / 100.0f;
            grassRate /= (waterFill + grassFill) / 100.0f;
        }
        int targetWater = Math.round(floorCount * waterRate),
                targetGrass = Math.round(floorCount * grassRate),
                sizeWaterPools = targetWater / rng.between(3, 6),
                sizeGrassPools = targetGrass / rng.between(2, 5);

        Coord[] scatter;
        int remainingWater = targetWater, remainingGrass = targetGrass;
        if(targetWater > 0) {
            scatter = floors.quasiRandomSeparated(1.0 / 7.0);
            rng.shuffleInPlace(scatter);
            GreasedRegion allWater = new GreasedRegion(width, height);
            for (int i = 0; i < scatter.length; i++) {
                if (remainingWater > 5)
                {
                    if(!floors.contains(scatter[i]))
                        continue;
                    working.empty().insert(scatter[i]).spill(floors, rng.between(4, Math.min(remainingWater, sizeWaterPools)), rng);

                    floors.andNot(working);
                    remainingWater -= working.size();
                    allWater.addAll(working);
                } else
                    break;
            }

            for (Coord pt : allWater) {
                hazards.remove(pt);
                //obstacles.add(pt);
                if (map[pt.x][pt.y] != '<' && map[pt.x][pt.y] != '>')
                    map[pt.x][pt.y] = '~';
            }
            for (Coord pt : allWater) {
                if (map[pt.x][pt.y] != '<' && map[pt.x][pt.y] != '>' &&
                        (map[pt.x - 1][pt.y] == '.' || map[pt.x + 1][pt.y] == '.' ||
                                map[pt.x][pt.y - 1] == '.' || map[pt.x][pt.y + 1] == '.'))
                    map[pt.x][pt.y] = ',';
            }
        }
        if(targetGrass > 0) {
            scatter = floors.quasiRandomSeparated(1.03/6.7);
            rng.shuffleInPlace(scatter);
            for (int i = 0; i < scatter.length; i++) {
                if (remainingGrass > 5) //remainingGrass >= targetGrass * 0.02 &&
                {
                    working.empty().insert(scatter[i]).spill(floors, rng.between(4, Math.min(remainingGrass, sizeGrassPools)), rng);
                    if (working.isEmpty())
                        continue;
                    floors.andNot(working);
                    remainingGrass -= working.size();
                    map = working.inverseMask(map, '"');
                } else
                    break;
            }
        }

        if(islandSpacing > 1 && targetWater > 0) {
            OrderedSet<Coord> islands = PoissonDisk.sampleMap(map, 1f * islandSpacing, rng, '#', '.', '"', '+', '/', '^', '<', '>');
            for (Coord c : islands) {
                map[c.x][c.y] = '.';
                if (map[c.x - 1][c.y] != '#' && map[c.x - 1][c.y] != '<' && map[c.x - 1][c.y] != '>')
                    map[c.x - 1][c.y] = ',';
                if (map[c.x + 1][c.y] != '#' && map[c.x + 1][c.y] != '<' && map[c.x + 1][c.y] != '>')
                    map[c.x + 1][c.y] = ',';
                if (map[c.x][c.y - 1] != '#' && map[c.x][c.y - 1] != '<' && map[c.x][c.y - 1] != '>')
                    map[c.x][c.y - 1] = ',';
                if (map[c.x][c.y + 1] != '#' && map[c.x][c.y + 1] != '<' && map[c.x][c.y + 1] != '>')
                    map[c.x][c.y + 1] = ',';
            }
        }

        if(trapFill > 0)
        {
            int total = hazards.size() * trapFill / 100;

            for(int i = 0; i < total; i++)
            {
                Coord entry = rng.getRandomElement(hazards);
                if(map[entry.x][entry.y] == '<' || map[entry.x][entry.y] == '<')
                    continue;
                map[entry.x][entry.y] = '^';
                hazards.remove(entry);
            }
        }

        dungeon = map;
        return map;

    }


    /**
     * Gets the seed that can be used to rebuild an identical dungeon to the latest one generated (or the seed that
     * will be used to generate the first dungeon if none has been made yet). You can pass the long this returns to
     * the setState() method on this class' rng field, which assuming all other calls to generate a dungeon are
     * identical, will ensure generate() or generateRespectingStairs() will produce the same dungeon output as the
     * dungeon originally generated with the seed this returned.
     * <br>
     * You can also call getState() on the rng field yourself immediately before generating a dungeon, but this method
     * handles some complexities of when the state is actually used to generate a dungeon; since StatefulRNG objects can
     * be shared between different classes that use random numbers, the state could change between when you call
     * getState() and when this class generates a dungeon. Using getRebuildSeed() eliminates that confusion.
     * @return a seed as a long that can be passed to setState() on this class' rng field to recreate a dungeon
     */
    public long getRebuildSeed() {
        return rebuildSeed;
    }

    /**
     * Provides a string representation of the latest generated dungeon.
     *
     * @return a printable string version of the latest generated dungeon.
     */
    @Override
	public String toString() {
        char[][] trans = new char[height][width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                trans[y][x] = dungeon[x][y];
            }
        }
        StringBuffer sb = new StringBuffer();
        for (int row = 0; row < height; row++) {
            sb.append(trans[row]);
            sb.append('\n');
        }
        return sb.toString();
    }

}
