package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.mapping.styled.DungeonBoneGen;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.*;

import java.util.*;

/**
 * A good way to create a more-complete dungeon, layering different effects and modifications on top of a dungeon
 * produced by DungeonBoneGen or another dungeon without such effects. Unlike DungeonGenerator, this class uses
 * environment information for the dungeons it is given (or quickly generates such information if using DungeonBoneGen),
 * and uses that information to only place effects like grass or water where you specify, like "only in caves", or
 * "doors should never be in caves". Ensures only connected regions of the map are used by filling unreachable areas
 * with walls, and can find far-apart staircase positions if generate() is used or can keep existing staircases in a map
 * if generateRespectingStairs() is used.
 * <br>
 * The main technique for using this is simple: Construct a DungeonGenerator, usually with the desired width and height,
 * then call any feature adding methods that you want in the dungeon, like addWater(), addTraps, addGrass(), or
 * addDoors(). All of these methods except addDoors() take an int argument that corresponds to a constant in this class,
 * CAVE, CORRIDOR, or ROOM, or ALL, and they will only cause the requested feature to show up in that environment. Some
 * of these take different parameters, like addDoors() which needs to know if it should check openings that are two
 * cells wide to add a door and a wall to, or whether it should only add doors to single-cell openings. In the case of
 * addDoors(), it doesn't take an environment argument since doors almost always are between environments (rooms and
 * corridors), so placing them only within one or the other doesn't make sense. This class, unlike the normal
 * DungeonGenerator, also has an addLake() method, which, like addDoors(), doesn't take an environment parameter. It can
 * be used to turn a large section of what would otherwise be walls into a lake (of some character for deep lake cells
 * and some character for shallow lake cells), and corridors that cross the lake become bridges, shown as ':'. It should
 * be noted that because the lake fills walls, it doesn't change the connectivity of the map unless you can cross the
 * lake. There's also addMaze(), which does change the connectivity by replacing sections of impassable walls with
 * twisty, maze-like passages.
 * <br>
 * Once you've added any features to the generator's effects list, call generate() to get a char[][] with the
 * desired dungeon map, using a fixed repertoire of chars to represent the different features, with the exception of the
 * customization that can be requested from addLake(). If you use the libGDX text-based display module, you can change
 * what chars are shown by using addSwap() in TextCellFactory. After calling generate(), you can safely get the values
 * from the stairsUp and stairsDown fields, which are Coords that should be a long distance from each other but
 * connected in the dungeon. You may want to change those to staircase characters, but there's no requirement to do
 * anything with them. It's recommended that you keep the resulting char[][] maps in some collection that can be saved,
 * since SectionDungeonGenerator only stores a temporary copy of the most recently-generated map. The DungeonUtility
 * field of this class, utility, is a convenient way of accessing the non-static methods in that class, such as
 * randomFloor(), without needing to create another DungeonUtility (this class creates one, so you don't have to).
 * Similarly, the Placement field of this class, placement, can be used to find parts of a dungeon that fit certain
 * qualities for the placement of items, terrain features, or NPCs.
 * <br>
 * Example map with a custom-representation lake: https://gist.github.com/tommyettinger/0055075f9de59c452d25
 * @see DungeonUtility this class exposes a DungeonUtility member; DungeonUtility also has many useful static methods
 * @see DungeonGenerator for a slightly simpler alternative that does not recognize different sections of dungeon
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
public class SectionDungeonGenerator implements IDungeonGenerator{

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
     * Constant for features being added to all environment types.
     */
    public static final int ALL = 0,
    /**
     * Constant for features being added only to rooms.
     */
    ROOM = 1,
    /**
     * Constant for features being added only to corridors.
     */
    CORRIDOR = 2,
    /**
     * Constant for features being added only to caves.
     */
    CAVE = 3;

    /**
     * The effects that will be applied when generate is called. Strongly prefer using addWater, addDoors, addTraps,
     * and addGrass.
     */
    public EnumMap<FillEffect, Integer> roomFX, corridorFX, caveFX;

    /**
     * Percentage of viable positions to fill with doors, represented by '+' for east-to-west connections or '/' for
     * north-to-south ones; this number will be negative if filling two-cell wide positions but will be made positive
     * when needed.
     */
    public int doorFX = 0;
    /**
     * The char to use for deep lake cells.
     */
    public char deepLakeGlyph = '~';
    /**
     * The char to use for shallow lake cells.
     */
    public char shallowLakeGlyph = ',';
    /**
     * The approximate percentage of non-room, non-cave, non-edge-of-map wall cells to try to fill with lake. Corridors
     * that are covered by a lake will become bridges, the glyph ':'.
     */
    public int lakeFX = 0;
    /**
     * The approximate percentage of non-room, non-cave, non-edge-of-map wall cells to try to fill with maze. Corridors
     * that are covered by a maze will become part of its layout.
     */
    public int mazeFX = 0;
    public DungeonUtility utility;
    protected int height, width;
    public Coord stairsUp = null, stairsDown = null;
    public StatefulRNG rng;
    protected long rebuildSeed;
    protected boolean seedFixed = false;
    protected int environmentType = 1;

    protected char[][] dungeon = null;
    /**
     * Potentially important if you need to identify specific rooms, corridors, or cave areas in a map.
     */
    public RoomFinder finder;
    /**
     * Configured by this class after you call generate(), this Placement can be used to locate areas of the dungeon
     * that fit certain properties, like "out of sight from a door" or "a large flat section of wall that could be used
     * to place a straight-line object." You can use this as-needed; it does only a small amount of work at the start,
     * and does the calculations for what areas have certain properties on request.
     */
    public Placement placement;

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
     * Make a SectionDungeonGenerator with a LightRNG using a random seed, height 40, and width 40.
     */
    public SectionDungeonGenerator()
    {
        rng = new StatefulRNG();
        utility = new DungeonUtility(rng);
        rebuildSeed = rng.getState();
        height = 40;
        width = 40;
        roomFX = new EnumMap<>(FillEffect.class);
        corridorFX = new EnumMap<>(FillEffect.class);
        caveFX = new EnumMap<>(FillEffect.class);
    }

    /**
     * Make a SectionDungeonGenerator with the given height and width; the RNG used for generating a dungeon and
     * adding features will be a LightRNG using a random seed. If width or height is greater than 256, then this will
     * expand the Coord pool from its 256x256 default so it stores a reference to each Coord that might be used in the
     * creation of the dungeon (if width and height are 300 and 300, the Coord pool will be 300x300; if width and height
     * are 500 and 100, the Coord pool will be 500x256 because it won't shrink below the default size of 256x256).
     * @param width The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     */
    public SectionDungeonGenerator(int width, int height)
    {
    	this(width, height, new StatefulRNG());
    }

    /**
     * Make a SectionDungeonGenerator with the given height, width, and RNG. Use this if you want to seed the RNG. If
     * width or height is greater than 256, then this will expand the Coord pool from its 256x256 default so it stores a
     * reference to each Coord that might be used in the creation of the dungeon (if width and height are 300 and 300,
     * the Coord pool will be 300x300; if width and height are 500 and 100, the Coord pool will be 500x256 because it
     * won't shrink below the default size of 256x256).
     * @param width The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     * @param rng The RNG to use for all purposes in this class; if it is a StatefulRNG, then it will be used as-is,
     *            but if it is not a StatefulRNG, a new StatefulRNG will be used, randomly seeded by this parameter
     */
    public SectionDungeonGenerator(int width, int height, RNG rng)
    {
        Coord.expandPoolTo(width, height);
        this.rng = (rng instanceof StatefulRNG) ? (StatefulRNG) rng : new StatefulRNG(rng.nextLong());
        utility = new DungeonUtility(this.rng);
        rebuildSeed = this.rng.getState();
        this.height = height;
        this.width = width;
        roomFX = new EnumMap<>(FillEffect.class);
        corridorFX = new EnumMap<>(FillEffect.class);
        caveFX = new EnumMap<>(FillEffect.class);
    }

    /**
     * Copies all fields from copying and makes a new DungeonGenerator.
     * @param copying the DungeonGenerator to copy
     */
    public SectionDungeonGenerator(SectionDungeonGenerator copying)
    {
        rng = new StatefulRNG(copying.rng.getState());
        utility = new DungeonUtility(rng);
        rebuildSeed = rng.getState();
        height = copying.height;
        width = copying.width;
        Coord.expandPoolTo(width, height);
        roomFX = new EnumMap<>(copying.roomFX);
        corridorFX = new EnumMap<>(copying.corridorFX);
        caveFX = new EnumMap<>(copying.caveFX);
        doorFX = copying.doorFX;
        lakeFX = copying.lakeFX;
        deepLakeGlyph = copying.deepLakeGlyph;
        shallowLakeGlyph = copying.shallowLakeGlyph;
        dungeon = copying.dungeon;
    }

    /**
     * Turns the majority of the given percentage of floor cells into water cells, represented by '~'. Water will be
     * clustered into a random number of pools, with more appearing if needed to fill the percentage.
     * Each pool will have randomized volume that should fill or get very close to filling the requested
     * percentage, unless the pools encounter too much tight space. If this DungeonGenerator previously had addWater
     * called, the latest call will take precedence. No islands will be placed with this variant, but the edge of the
     * water will be shallow, represented by ','.
     * @param env the environment to apply this to; uses MixedGenerator's constants, or 0 for "all environments"
     * @param percentage the percentage of floor cells to fill with water
     * @return this DungeonGenerator; can be chained
     */
    public SectionDungeonGenerator addWater(int env, int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        switch (env)
        {
            case ROOM:
                if(roomFX.containsKey(FillEffect.WATER)) roomFX.remove(FillEffect.WATER);
                roomFX.put(FillEffect.WATER, percentage);
                break;
            case CORRIDOR:
                if(corridorFX.containsKey(FillEffect.WATER)) corridorFX.remove(FillEffect.WATER);
                corridorFX.put(FillEffect.WATER, percentage);
                break;
            case CAVE:
                if(caveFX.containsKey(FillEffect.WATER)) caveFX.remove(FillEffect.WATER);
                caveFX.put(FillEffect.WATER, percentage);
                break;
            default:
                if(roomFX.containsKey(FillEffect.WATER))
                    roomFX.put(FillEffect.WATER, Math.min(100, roomFX.get(FillEffect.WATER) + percentage));
                else
                    roomFX.put(FillEffect.WATER, percentage);
                if(corridorFX.containsKey(FillEffect.WATER))
                    corridorFX.put(FillEffect.WATER, Math.min(100, corridorFX.get(FillEffect.WATER) + percentage));
                else
                    corridorFX.put(FillEffect.WATER, percentage);
                if(caveFX.containsKey(FillEffect.WATER))
                    caveFX.put(FillEffect.WATER, Math.min(100, caveFX.get(FillEffect.WATER) + percentage));
                else
                    caveFX.put(FillEffect.WATER, percentage);
        }
        return this;
    }
    /**
     * Turns the majority of the given percentage of floor cells into water cells, represented by '~'. Water will be
     * clustered into a random number of pools, with more appearing if needed to fill the percentage. Each pool will
     * have randomized volume that should fill or get very close to filling the requested percentage,
     * unless the pools encounter too much tight space. If this DungeonGenerator previously had addWater called, the
     * latest call will take precedence. If islandSpacing is greater than 1, then this will place islands of floor, '.',
     * surrounded by shallow water, ',', at about the specified distance with Euclidean measurement.
     * @param env the environment to apply this to; uses MixedGenerator's constants, or 0 for "all environments"
     * @param percentage the percentage of floor cells to fill with water
     * @param islandSpacing if greater than 1, islands will be placed randomly this many cells apart.
     * @return this DungeonGenerator; can be chained
     */
    public SectionDungeonGenerator addWater(int env, int percentage, int islandSpacing)
    {
        addWater(env, percentage);

        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        switch (env) {
            case ROOM:
                if (roomFX.containsKey(FillEffect.ISLANDS)) roomFX.remove(FillEffect.ISLANDS);
                if(islandSpacing > 1)
                    roomFX.put(FillEffect.ISLANDS, percentage);
                break;
            case CORRIDOR:
                if (corridorFX.containsKey(FillEffect.ISLANDS)) corridorFX.remove(FillEffect.ISLANDS);
                if(islandSpacing > 1)
                    corridorFX.put(FillEffect.ISLANDS, percentage);
                break;
            case CAVE:
                if (caveFX.containsKey(FillEffect.ISLANDS)) caveFX.remove(FillEffect.ISLANDS);
                if(islandSpacing > 1)
                    caveFX.put(FillEffect.ISLANDS, percentage);
                break;
            default:
                if (roomFX.containsKey(FillEffect.ISLANDS)) roomFX.remove(FillEffect.ISLANDS);
                if(islandSpacing > 1)
                    roomFX.put(FillEffect.ISLANDS, percentage);
                if (corridorFX.containsKey(FillEffect.ISLANDS)) corridorFX.remove(FillEffect.ISLANDS);
                if(islandSpacing > 1)
                    corridorFX.put(FillEffect.ISLANDS, percentage);
                if (caveFX.containsKey(FillEffect.ISLANDS)) caveFX.remove(FillEffect.ISLANDS);
                if(islandSpacing > 1)
                    caveFX.put(FillEffect.ISLANDS, percentage);
        }

        return this;
    }

    /**
     * Turns the majority of the given percentage of floor cells into grass cells, represented by '"'. Grass will be
     * clustered into a random number of patches, with more appearing if needed to fill the percentage. Each area will
     * have randomized volume that should fill or get very close to filling (two thirds of) the requested percentage,
     * unless the patches encounter too much tight space. If this DungeonGenerator previously had addGrass called, the
     * latest call will take precedence.
     * @param env the environment to apply this to; uses MixedGenerator's constants, or 0 for "all environments"
     * @param percentage the percentage of floor cells to fill with grass; this can vary quite a lot. It may be
     *                   difficult to fill very high (over 66%) percentages of map with grass, though you can do this by
     *                   giving a percentage of between 100 and 150.
     * @return this DungeonGenerator; can be chained
     */
    public SectionDungeonGenerator addGrass(int env, int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        switch (env)
        {
            case ROOM:
                if(roomFX.containsKey(FillEffect.GRASS)) roomFX.remove(FillEffect.GRASS);
                roomFX.put(FillEffect.GRASS, percentage);
                break;
            case CORRIDOR:
                if(corridorFX.containsKey(FillEffect.GRASS)) corridorFX.remove(FillEffect.GRASS);
                corridorFX.put(FillEffect.GRASS, percentage);
                break;
            case CAVE:
                if(caveFX.containsKey(FillEffect.GRASS)) caveFX.remove(FillEffect.GRASS);
                caveFX.put(FillEffect.GRASS, percentage);
                break;
            default:
                if(roomFX.containsKey(FillEffect.GRASS))
                    roomFX.put(FillEffect.GRASS, Math.min(100, roomFX.get(FillEffect.GRASS) + percentage));
                else
                    roomFX.put(FillEffect.GRASS, percentage);
                if(corridorFX.containsKey(FillEffect.GRASS))
                    corridorFX.put(FillEffect.GRASS, Math.min(100, corridorFX.get(FillEffect.GRASS) + percentage));
                else
                    corridorFX.put(FillEffect.GRASS, percentage);
                if(caveFX.containsKey(FillEffect.GRASS))
                    caveFX.put(FillEffect.GRASS, Math.min(100, caveFX.get(FillEffect.GRASS) + percentage));
                else
                    caveFX.put(FillEffect.GRASS, percentage);
        }
        return this;
    }
    /**
     * Turns the given percentage of floor cells not already adjacent to walls into wall cells, represented by '#'.
     * If this DungeonGenerator previously had addBoulders called, the latest call will take precedence.
     * @param env the environment to apply this to; uses MixedGenerator's constants, or 0 for "all environments"
     * @param percentage the percentage of floor cells not adjacent to walls to fill with boulders.
     * @return this DungeonGenerator; can be chained
     */
    public SectionDungeonGenerator addBoulders(int env, int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        switch (env)
        {
            case ROOM:
                if(roomFX.containsKey(FillEffect.BOULDERS)) roomFX.remove(FillEffect.BOULDERS);
                roomFX.put(FillEffect.BOULDERS, percentage);
                break;
            case CORRIDOR:
                if(corridorFX.containsKey(FillEffect.BOULDERS)) corridorFX.remove(FillEffect.BOULDERS);
                corridorFX.put(FillEffect.BOULDERS, percentage);
                break;
            case CAVE:
                if(caveFX.containsKey(FillEffect.BOULDERS)) caveFX.remove(FillEffect.BOULDERS);
                caveFX.put(FillEffect.BOULDERS, percentage);
                break;
            default:
                if(roomFX.containsKey(FillEffect.BOULDERS))
                    roomFX.put(FillEffect.BOULDERS, Math.min(100, roomFX.get(FillEffect.BOULDERS) + percentage));
                else
                    roomFX.put(FillEffect.BOULDERS, percentage);
                if(corridorFX.containsKey(FillEffect.BOULDERS))
                    corridorFX.put(FillEffect.BOULDERS, Math.min(100, corridorFX.get(FillEffect.BOULDERS) + percentage));
                else
                    corridorFX.put(FillEffect.BOULDERS, percentage);
                if(caveFX.containsKey(FillEffect.BOULDERS))
                    caveFX.put(FillEffect.BOULDERS, Math.min(100, caveFX.get(FillEffect.BOULDERS) + percentage));
                else
                    caveFX.put(FillEffect.BOULDERS, percentage);
        }
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
    public SectionDungeonGenerator addDoors(int percentage, boolean doubleDoors)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        if(doubleDoors) percentage *= -1;
        doorFX = percentage;
        return this;
    }

    /**
     * Instructs the generator to add a winding section of corridors into a large area that can be filled without
     * overwriting rooms, caves, or the edge of the map; wall cells will become either '#' or '.' and corridors will be
     * overwritten. If the percentage is too high (40% is probably too high to adequately fill), this will fill less than
     * the requested percentage rather than fill multiple mazes.
     * @param percentage The percentage of non-room, non-cave, non-edge-of-map wall cells to try to fill with maze.
     * @return this for chaining
     */
    public SectionDungeonGenerator addMaze(int percentage)
    {

        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        mazeFX = percentage;
        return this;
    }

    /**
     * Instructs the generator to add a lake (here, of water) into a large area that can be filled without overwriting
     * rooms, caves, or the edge of the map; wall cells will become the deep lake glyph (here, '~'), unless they are
     * close to an existing room or cave, in which case they become the shallow lake glyph (here, ','), and corridors
     * that are "covered" by a lake will become bridges, the glyph ':'. If the percentage is too high (40% is probably
     * too high to adequately fill), this will fill less than the requested percentage rather than fill multiple lakes.
     * @param percentage The percentage of non-room, non-cave, non-edge-of-map wall cells to try to fill with lake.
     * @return this for chaining
     */
    public SectionDungeonGenerator addLake(int percentage)
    {
        return addLake(percentage, '~', ',');
    }

    /**
     * Instructs the generator to add a lake into a large area that can be filled without overwriting rooms, caves, or
     * the edge of the map; wall cells will become the char deepLake, unless they are close to an existing room or cave,
     * in which case they become the char shallowLake, and corridors that are "covered" by a lake will become bridges,
     * the glyph ':'. If the percentage is too high (40% is probably too high to adequately fill), this will fill less
     * than the requested percentage rather than fill multiple lakes.
     * @param percentage The percentage of non-room, non-cave, non-edge-of-map wall cells to try to fill with lake.
     * @param deepLake the char to use for deep lake cells, such as '~'
     * @param shallowLake the char to use for shallow lake cells, such as ','
     * @return this for chaining
     */
    public SectionDungeonGenerator addLake(int percentage, char deepLake, char shallowLake)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        lakeFX = percentage;
        deepLakeGlyph = deepLake;
        shallowLakeGlyph = shallowLake;
        return this;
    }

    /**
     * Turns the given percentage of open area floor cells into trap cells, represented by '^'. Corridors that have no
     * possible way to move around a trap will not receive traps, ever. If this DungeonGenerator previously had
     * addTraps called, the latest call will take precedence.
     * @param env the environment to apply this to; uses MixedGenerator's constants, or 0 for "all environments"
     * @param percentage the percentage of valid cells to fill with traps; should be no higher than 5 unless
     *                   the dungeon floor is meant to be a kill screen or minefield.
     * @return this DungeonGenerator; can be chained
     */
    public SectionDungeonGenerator addTraps(int env, int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        switch (env)
        {
            case ROOM:
                if(roomFX.containsKey(FillEffect.TRAPS)) roomFX.remove(FillEffect.TRAPS);
                roomFX.put(FillEffect.TRAPS, percentage);
                break;
            case CORRIDOR:
                if(corridorFX.containsKey(FillEffect.TRAPS)) corridorFX.remove(FillEffect.TRAPS);
                corridorFX.put(FillEffect.TRAPS, percentage);
                break;
            case CAVE:
                if(caveFX.containsKey(FillEffect.TRAPS)) caveFX.remove(FillEffect.TRAPS);
                caveFX.put(FillEffect.TRAPS, percentage);
                break;
            default:
                if(roomFX.containsKey(FillEffect.TRAPS))
                    roomFX.put(FillEffect.TRAPS, Math.min(100, roomFX.get(FillEffect.TRAPS) + percentage));
                else
                    roomFX.put(FillEffect.TRAPS, percentage);
                if(corridorFX.containsKey(FillEffect.TRAPS))
                    corridorFX.put(FillEffect.TRAPS, Math.min(100, corridorFX.get(FillEffect.TRAPS) + percentage));
                else
                    corridorFX.put(FillEffect.TRAPS, percentage);
                if(caveFX.containsKey(FillEffect.TRAPS))
                    caveFX.put(FillEffect.TRAPS, Math.min(100, caveFX.get(FillEffect.TRAPS) + percentage));
                else
                    caveFX.put(FillEffect.TRAPS, percentage);
        }
        return this;
    }

    /**
     * Removes any door, water, or trap insertion effects that this DungeonGenerator would put in future dungeons.
     * @return this DungeonGenerator, with all effects removed. Can be chained.
     */
    public SectionDungeonGenerator clearEffects()
    {
        roomFX.clear();
        corridorFX.clear();
        caveFX.clear();
        lakeFX = 0;
        mazeFX = 0;
        doorFX = 0;
        return this;
    }

    protected OrderedSet<Coord> removeAdjacent(OrderedSet<Coord> coll, Coord pt)
    {
        for(Coord temp : new Coord[]{Coord.get(pt.x + 1, pt.y), Coord.get(pt.x - 1, pt.y),
                Coord.get(pt.x, pt.y + 1), Coord.get(pt.x, pt.y - 1)})
        {
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
            if(!(temp.x == pt1.x && temp.y == pt1.y) && !(temp.x == pt2.x && temp.y == pt2.y))
                coll.remove(temp);
        }

        return coll;
    }
    protected OrderedSet<Coord> removeNearby(OrderedSet<Coord> coll, char[][] disallowed)
    {
        if(coll == null || disallowed == null || disallowed.length == 0 || disallowed[0].length == 0)
            return new OrderedSet<>();
        OrderedSet<Coord> next = new OrderedSet<>(coll.size());
        int width = disallowed.length, height = disallowed[0].length;
        COORD_WISE:
        for(Coord c : coll)
        {
            for (int x = Math.max(0, c.x - 1); x <= Math.min(width - 1, c.x + 1); x++) {

                for (int y = Math.max(0, c.y - 1); y <= Math.min(height - 1, c.y + 1); y++) {
                    if(disallowed[x][y] != '#')
                        continue COORD_WISE;
                }
            }
            next.add(c);
        }

        return next;
    }


    protected OrderedSet<Coord> viableDoorways(boolean doubleDoors, char[][] map, char[][] allCaves,
                                                  char[][] allCorridors)
    {
        OrderedSet<Coord> doors = new OrderedSet<>();
        OrderedSet<Coord> blocked = new OrderedSet<>(4);
        DijkstraMap dm = new DijkstraMap(map, DijkstraMap.Measurement.EUCLIDEAN);
        for(int x = 1; x < map.length - 1; x++) {
            for (int y = 1; y < map[x].length - 1; y++) {
                if(map[x][y] == '#' || allCorridors[x][y] != '#')
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
                                dm.partialScan(null, 16, blocked);
                                if(dm.gradientMap[x][y-1] < DijkstraMap.FLOOR)
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
                                dm.partialScan(null, 16, blocked);
                                if(dm.gradientMap[x-1][y] < DijkstraMap.FLOOR)
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
                        dm.partialScan(null, 16, blocked);
                        if(dm.gradientMap[x][y-1] < DijkstraMap.FLOOR)
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
                        dm.partialScan(null, 16, blocked);
                        if(dm.gradientMap[x-1][y] < DijkstraMap.FLOOR)
                            continue;
                        doors.add(Coord.get(x, y));
                        doors = removeAdjacent(doors, Coord.get(x, y));
                    }
                }

            }
        }

        return removeNearby(doors, allCaves);
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
     * @see TilesetType
     * @param kind a TilesetType enum value, such as TilesetType.DEFAULT_DUNGEON
     * @return a char[][] dungeon
     */
    public char[][] generate(TilesetType kind)
    {
        rebuildSeed = rng.getState();
        environmentType = kind.environment();
        DungeonBoneGen gen = new DungeonBoneGen(rng);
        char[][] map = DungeonUtility.wallWrap(gen.generate(kind, width, height));

        seedFixed = false;
        DijkstraMap dijkstra = new DijkstraMap(map);
        int frustrated = 0;
        do {
            dijkstra.clearGoals();
            stairsUp = utility.randomFloor(map);
            dijkstra.setGoal(stairsUp);
            dijkstra.scan(null, null);
            frustrated++;
        }while (dijkstra.getMappedCount() < width + height && frustrated < 15);
        double maxDijkstra = 0.0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(dijkstra.gradientMap[i][j] >= DijkstraMap.FLOOR) {
                    map[i][j] = '#';
                }
                else if(dijkstra.gradientMap[i][j] > maxDijkstra) {
                    maxDijkstra = dijkstra.gradientMap[i][j];
                }
            }
        }
        stairsDown = new GreasedRegion(dijkstra.gradientMap, maxDijkstra * 0.7,
                DijkstraMap.FLOOR).singleRandom(rng);
        finder = new RoomFinder(map, environmentType);
        return innerGenerate();
    }

    /**
     * Generate a char[][] dungeon with extra features given a baseDungeon that has already been generated and an
     * environment as an int[][], which can often be obtained from MixedGenerator or classes that use it, like
     * SerpentMapGenerator, with their getEnvironment method.
     * Typically, you want to call generate with a TilesetType or no argument for the easiest generation; this method
     * is meant for adding features like water and doors to existing maps while avoiding placing incongruous features in
     * areas where they don't fit, like a door in a cave or moss in a room.
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
     * passing dungeon (which that code modifies) in as baseDungeon to this method. Because tabs will always be replaced
     * by floors ('.'), this considers any tabs that overlap with what the environment considers a wall (cave wall, room
     * wall, corridor wall, or untouched) to really refer to a corridor floor, but doesn't reconsider tabs that overlap
     * with floors already (it keeps the state of actual room, cave, and corridor floors). This is useful so you only
     * have to call ensurePath or a similar method on the 2D char array and can leave the 2D int array alone.
     * @param baseDungeon a pre-made dungeon consisting of '#' for walls and '.' for floors; may be modified in-place
     * @param environment stores whether a cell is room, corridor, or cave; getEnvironment() typically gives this
     * @return a char[][] dungeon
     */
    public char[][] generate(char[][] baseDungeon, int[][] environment)
    {
        if(!seedFixed)
        {
            rebuildSeed = rng.getState();
        }
        seedFixed = false;
        char[][] map = DungeonUtility.wallWrap(baseDungeon);
        width = map.length;
        height = map[0].length;
        int[][] env2 = new int[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(environment[x], 0, env2[x], 0, height);
        }

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
            dijkstra.scan(null, null);
            frustrated++;
        }while (dijkstra.getMappedCount() < width + height && frustrated < 8);
        if(frustrated >= 8)
        {
            return generate();
        }
        double maxDijkstra = 0.0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(dijkstra.gradientMap[i][j] >= DijkstraMap.FLOOR) {
                    map[i][j] = '#';
                    env2[i][j] = DungeonUtility.UNTOUCHED;
                }
                else if(dijkstra.gradientMap[i][j] > maxDijkstra) {
                    maxDijkstra = dijkstra.gradientMap[i][j];
                }
                if (map[i][j] == '\t') {
                    map[i][j] = '.';
                    if((env2[i][j] & 1) == 0) // environment is a wall here
                        env2[i][j] = DungeonUtility.CORRIDOR_FLOOR;
                }
            }
        }
        if(maxDijkstra < 16)
            return generate(baseDungeon, environment);
        stairsDown = new GreasedRegion(dijkstra.gradientMap, maxDijkstra * 0.7,
                DijkstraMap.FLOOR).singleRandom(rng);
        finder = new RoomFinder(map, env2);
        return innerGenerate();
    }

    /**
     * Generate a char[][] dungeon with extra features given a baseDungeon that has already been generated, with
     * staircases represented by greater than and less than signs, and an environment as an int[][], which can often be
     * obtained from MixedGenerator or classes that use it, like SerpentMapGenerator, with their getEnvironment method.
     * Typically, you want to call generate with a TilesetType or no argument for the easiest generation; this method
     * is meant for adding features like water and doors to existing maps while avoiding placing incongruous features in
     * areas where they don't fit, like a door in a cave or moss in a room.
     * This uses '#' for walls, '.' for floors, '~' for deep water, ',' for shallow water, '^' for traps, '+' for doors
     * that provide horizontal passage, and '/' for doors that provide vertical passage.
     * Use the addDoors, addWater, addGrass, and addTraps methods of this class to request these in the generated map.
     * Also sets the fields stairsUp and stairsDown to null, and expects stairs to be already handled.
     * @param baseDungeon a pre-made dungeon consisting of '#' for walls and '.' for floors, with stairs already in;
     *                    may be modified in-place
     * @param environment stores whether a cell is room, corridor, or cave; getEnvironment() typically gives this
     * @return a char[][] dungeon
     */
    public char[][] generateRespectingStairs(char[][] baseDungeon, int[][] environment) {
        if(!seedFixed)
        {
            rebuildSeed = rng.getState();
        }
        seedFixed = false;
        char[][] map = DungeonUtility.wallWrap(baseDungeon);
        int[][] env2 = new int[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(environment[x], 0, env2[x], 0, height);
        }
        DijkstraMap dijkstra = new DijkstraMap(map);
        stairsUp = null;
        stairsDown = null;

        dijkstra.clearGoals();
        ArrayList<Coord> stairs = DungeonUtility.allMatching(map, '<', '>');
        for (int j = 0; j < stairs.size(); j++) {
            dijkstra.setGoal(stairs.get(j));
        }
        dijkstra.scan(null, null);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (dijkstra.gradientMap[i][j] >= DijkstraMap.FLOOR) {
                    map[i][j] = '#';
                    env2[i][j] = DungeonUtility.UNTOUCHED;
                }
            }
        }
        finder = new RoomFinder(map, env2);
        return innerGenerate();
    }



    protected char[][] innerGenerate() {
        dungeon = ArrayTools.fill('#', width, height);
        ArrayList<char[][]> rm = finder.findRooms(),
                cr = finder.findCorridors(),
                cv = finder.findCaves();
        char[][] roomMap = innerGenerate(RoomFinder.merge(rm, width, height), roomFX),
                allCorridors = RoomFinder.merge(cr, width, height),
                corridorMap = innerGenerate(allCorridors, corridorFX),
                allCaves = RoomFinder.merge(cv, width, height),
                caveMap = innerGenerate(allCaves, caveFX),
                doorMap;
        char[][][] lakesAndMazes = makeLake(rm, cv);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (corridorMap[x][y] != '#' && lakesAndMazes[0][x][y] != '#')
                    dungeon[x][y] = ':';
                else if (roomMap[x][y] != '#')
                    dungeon[x][y] = roomMap[x][y];
                else if (lakesAndMazes[1][x][y] != '#') {
                    dungeon[x][y] = lakesAndMazes[1][x][y];
                    finder.environment[x][y] = DungeonUtility.CORRIDOR_FLOOR;
                } else if (corridorMap[x][y] != '#')
                    dungeon[x][y] = corridorMap[x][y];
                else if (caveMap[x][y] != '#')
                    dungeon[x][y] = caveMap[x][y];
                else if (lakesAndMazes[0][x][y] != '#') {
                    dungeon[x][y] = lakesAndMazes[0][x][y];
                    finder.environment[x][y] = DungeonUtility.CAVE_FLOOR;
                }
            }
        }
        finder = new RoomFinder(dungeon, finder.environment);
        rm = finder.findRooms();
        cr = finder.findCorridors();
        cv = finder.findCaves();
        cv.add(lakesAndMazes[0]);
        allCaves = RoomFinder.merge(cv, width, height);
        doorMap = makeDoors(rm, cr, allCaves, allCorridors);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (doorMap[x][y] == '+' || doorMap[x][y] == '/')
                    dungeon[x][y] = doorMap[x][y];
                else if (doorMap[x][y] == '*')
                    dungeon[x][y] = '#';

            }
        }
        placement = new Placement(finder);
        return dungeon;

    }
    protected char[][] makeDoors(ArrayList<char[][]> rooms, ArrayList<char[][]> corridors, char[][] allCaves,
                               char[][] allCorridors)
    {
        char[][] map = new char[width][height];
        for (int x = 0; x < width; x++) {
            Arrays.fill(map[x], '#');
        }
        if(doorFX == 0 || (rooms.isEmpty() && corridors.isEmpty()))
            return map;
        boolean doubleDoors = false;
        int doorFill = doorFX;
        if(doorFill < 0)
        {
            doubleDoors = true;
            doorFill *= -1;
        }
        ArrayList<char[][]> fused = new ArrayList<>(rooms.size() + corridors.size());
        fused.addAll(rooms);
        fused.addAll(corridors);

        map = RoomFinder.merge(fused, width, height);

        OrderedSet<Coord> doorways = viableDoorways(doubleDoors, map, allCaves, allCorridors);


        int total = doorways.size() * doorFill / 100;

        BigLoop:
        for(int i = 0; i < total; i++) {
            Coord entry = rng.getRandomElement(doorways);
            if (map[entry.x][entry.y] == '<' || map[entry.x][entry.y] == '>')
                continue;
            if (map[entry.x - 1][entry.y] != '#' && map[entry.x + 1][entry.y] != '#' &&
                    map[entry.x - 1][entry.y] != '*' && map[entry.x + 1][entry.y] != '*') {
                map[entry.x][entry.y] = '+';
            } else {
                map[entry.x][entry.y] = '/';
            }
            Coord[] adj = new Coord[]{Coord.get(entry.x + 1, entry.y), Coord.get(entry.x - 1, entry.y),
                    Coord.get(entry.x, entry.y + 1), Coord.get(entry.x, entry.y - 1)};
            for (Coord near : adj) {
                if (doorways.contains(near)) {
                    map[near.x][near.y] = '*';
                    doorways.remove(near);
                    doorways.remove(entry);
                    i++;
                    continue BigLoop;
                }
            }
            doorways.remove(entry);
        }
        return map;

    }
    protected char[][][] makeLake(ArrayList<char[][]> rooms, ArrayList<char[][]> caves)
    {
        char[][][] maps = new char[2][width][height];
        char[][] fusedMap;
        for (int x = 0; x < width; x++) {
            Arrays.fill(maps[0][x], '#');
            Arrays.fill(maps[1][x], '#');
        }
        if((lakeFX == 0 && mazeFX == 0) || (rooms.isEmpty() && caves.isEmpty()))
            return maps;
        int lakeFill = lakeFX, mazeFill = mazeFX;
        if(lakeFX + mazeFX > 100)
        {
            lakeFill -= (lakeFX + mazeFX - 100) / 2;
            mazeFill -= (lakeFX + mazeFX - 99) / 2;
        }

        ArrayList<char[][]> fused = new ArrayList<>(rooms.size() + caves.size());
        fused.addAll(rooms);
        fused.addAll(caves);

        fusedMap = RoomFinder.merge(fused, width, height);
        GreasedRegion limit = new GreasedRegion(width, height).insertRectangle(1, 1, width - 2, height - 2),
                potential = new GreasedRegion(fusedMap, '#').and(limit),
                flooded, chosen, tmp = new GreasedRegion(width, height);
        int ctr = potential.size(), potentialMazeSize = ctr * mazeFill / 100, potentialLakeSize = ctr * lakeFill / 100;
        ArrayList<GreasedRegion> viable;
        int minSize;
        Coord center;
        boolean[][] deep;
        if(potentialMazeSize > 0) {
            viable = potential.split();
            if (viable.isEmpty())
                return maps;

            chosen = viable.get(0);
            minSize = chosen.size();
            for (GreasedRegion sa : viable) {
                int sz = sa.size();
                if (sz > minSize) {
                    chosen = sa;
                    minSize = sz;
                }
            }
            PacMazeGenerator pac = new PacMazeGenerator(width - width % 3, height - height % 3, rng);
            char[][] pacMap = ArrayTools.insert(pac.generate(), ArrayTools.fill('#', width, height), 1, 1);
            center = chosen.singleRandom(rng);
            flooded = new GreasedRegion(center, width, height).spill(chosen, potentialMazeSize, rng).and(limit);
            GreasedRegion pacEnv = new GreasedRegion(pacMap, '.').and(flooded).removeIsolated();
            deep = pacEnv.decode();

            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    if (deep[x][y])
                        maps[1][x][y] = pacMap[x][y];
                }
            }
            finder.corridors.put(pacEnv, new ArrayList<GreasedRegion>());
            finder.allCorridors.or(pacEnv);
            finder.allFloors.or(pacEnv);
            potential.andNot(flooded);
        }
        if(potentialLakeSize > 0) {
            viable = potential.split();
            if (viable.isEmpty())
                return maps;
            chosen = viable.get(0);
            minSize = chosen.size();
            for (GreasedRegion sa : viable) {
                int sz = sa.size();
                if (sz > minSize) {
                    chosen = sa;
                    minSize = sz;
                }
            }
            center = chosen.singleRandom(rng);
            flooded = new GreasedRegion(center, width, height).spill(chosen, potentialLakeSize, rng).and(limit);

            deep = flooded.decode();
            flooded.flood(new GreasedRegion(fusedMap, '.').fringe8way(3), 3).and(limit);

            boolean[][] shallow = flooded.decode();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (deep[x][y])
                        maps[0][x][y] = deepLakeGlyph;
                    else if (shallow[x][y])
                        maps[0][x][y] = shallowLakeGlyph;
                }
            }
            ArrayList<GreasedRegion> change = new ArrayList<>();
            for (GreasedRegion room : finder.rooms.keySet()) {
                if(flooded.intersects(tmp.remake(room).expand8way()))
                    change.add(room);
            }
            for (GreasedRegion region : change) {
                finder.caves.put(region, finder.rooms.remove(region));
                finder.allRooms.andNot(region);
                finder.allCaves.or(region);
            }
        }
        return maps;
    }

    protected char[][] innerGenerate(char[][] map, EnumMap<FillEffect, Integer> fx)
    {
        OrderedSet<Coord> hazards = new OrderedSet<>();
        int floorCount = DungeonUtility.countCells(map, '.'),
                doorFill = 0,
                waterFill = 0,
                grassFill = 0,
                trapFill = 0,
                boulderFill = 0,
                islandSpacing = 0;

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
                    if (map[x][y] == '.') {
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
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < height; row++) {
            sb.append(trans[row]);
            sb.append('\n');
        }
        return sb.toString();
    }

}
