package squidpony.performance.alternate.other;

import squidpony.ArrayTools;
import squidpony.squidai.Technique;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.LOS;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.*;

import java.io.Serializable;
import java.util.*;

/**
 * An alternative to AStarSearch when you want to fully explore a search space, or when you want a gradient floodfill.
 * It's currently significantly faster that AStarSearch, and also supports pathfinding to the nearest of multiple
 * goals, which is not possible with AStarSearch. This last feature enables a whole host of others, like pathfinding
 * for creatures that can attack targets between a specified minimum and maximum distance, and there's also the
 * standard uses of Dijkstra Maps such as finding ideal paths to run away. One unique optimization made possible by
 * Dijkstra Maps is for when only one endpoint of a path can change in some section of a game, such as when you want to
 * draw a path from the player's current cell to the cell the mouse is over, and while the mouse can move quickly, the
 * player doesn't move until instructed to. This can be done very efficiently by setting the player as a goal with
 * {@link #setGoal(Coord)}, scanning the map to find distances with {@link #scan(Collection)}, and then as long as the
 * player's position is unchanged (and no obstacles are added/moved), you can get the path by calling
 * {@link #findPathPreScanned(Coord)} and giving it the mouse position as a Coord. If various parts of the path can
 * change instead of just one (such as other NPCs moving around), then you should set a goal or goals and call
 * {@link #findPath(int, Collection, Collection, Coord, Coord...)}. The parameters for this are used in various methods
 * in this class with only slight differences: length is the length of path that can be moved "in one go," so 1 for most
 * roguelikes and more for most strategy games, impassable used for enemies and solid moving obstacles, onlyPassable can
 * be null in most roguelikes but in strategy games should contain ally positions that can be moved through as long as
 * no one stops in them, start is the NPC's starting position, and targets is an array or vararg of Coord that the NPC
 * should pathfind toward (it could be just one Coord, with or without explicitly putting it in an array, or it could be
 * more and the NPC will pick the closest).
 * <br>
 * As a bit of introduction, the article http://www.roguebasin.com/index.php?title=Dijkstra_Maps_Visualized can
 * provide some useful information on how these work and how to visualize the information they can produce, while
 * http://www.roguebasin.com/index.php?title=The_Incredible_Power_of_Dijkstra_Maps is an inspiring list of the
 * various features Dijkstra Maps can enable.
 * <br>
 * If you can't remember how to spell this, just remember: Does It Just Know Stuff? That's Really Awesome!
 * Created by Tommy Ettinger on 4/4/2015.
 */
public class DijkstraMap implements Serializable {
    private static final long serialVersionUID = -2456306898212944441L;

    private static final double root2 = Math.sqrt(2.0);

    /**
     * The type of heuristic to use.
     */
    public static enum Measurement {

        /**
         * The distance it takes when only the four primary directions can be
         * moved in. The default.
         */
        MANHATTAN,
        /**
         * The distance it takes when diagonal movement costs the same as
         * cardinal movement.
         */
        CHEBYSHEV,
        /**
         * The distance it takes as the crow flies. This will NOT affect movement cost when calculating a path,
         * only the preferred squares to travel to (resulting in drastically more reasonable-looking paths).
         */
        EUCLIDEAN;

        public double heuristic(Direction target) {
            switch (this) {
                case EUCLIDEAN:
                    switch (target) {
                        case DOWN_LEFT:
                        case DOWN_RIGHT:
                        case UP_LEFT:
                        case UP_RIGHT:
                            return root2;
                    }
                default: 
                    return 1.0;
            }
        }

        public int directionCount() {
            switch (this) {
                case MANHATTAN: return 4;
                default: return 8;
            }
        }
        /**
         * Gets the appropriate DijkstraMap.Measurement that matches a Radius enum.
         * Matches SQUARE or CUBE to CHEBYSHEV, DIAMOND or OCTAHEDRON to MANHATTAN, and CIRCLE or SPHERE to EUCLIDEAN.
         * <br>
         * Equivalent to {@link DijkstraMap#findMeasurement(Radius)}.
         *
         * @param radius the Radius to find the corresponding Measurement for
         * @return a DijkstraMap.Measurement that matches radius; SQUARE to CHEBYSHEV, DIAMOND to MANHATTAN, etc.
         */
        public static Measurement matchingMeasurement(Radius radius) {
            switch (radius)
            {
                case CUBE:
                case SQUARE:
                    return DijkstraMap.Measurement.CHEBYSHEV;
                case DIAMOND:
                case OCTAHEDRON:
                    return DijkstraMap.Measurement.MANHATTAN;
                default:
                    return DijkstraMap.Measurement.EUCLIDEAN;
            }
        }

        /**
         * Gets the appropriate Radius corresponding to a DijkstraMap.Measurement.
         * Matches CHEBYSHEV to SQUARE, MANHATTAN to DIAMOND, and EUCLIDEAN to CIRCLE.
         * <br>
         * You may also consider {@link DijkstraMap#findRadius(Measurement)}, which is a static method with the same
         * general capability.
         * @return a Radius enum that matches this Measurement; CHEBYSHEV to SQUARE, MANHATTAN to DIAMOND, etc.
         */
        public Radius matchingRadius() {
            switch (this) {
                case CHEBYSHEV:
                    return Radius.SQUARE;
                case EUCLIDEAN:
                    return Radius.CIRCLE;
                default:
                    return Radius.DIAMOND;
            }
        }

    }

    /**
     * This affects how distance is measured on diagonal directions vs. orthogonal directions. MANHATTAN should form a
     * diamond shape on a featureless map, while CHEBYSHEV and EUCLIDEAN will form a square. EUCLIDEAN does not affect
     * the length of paths, though it will change the DijkstraMap's gradientMap to have many non-integer values, and
     * that in turn will make paths this finds much more realistic and smooth (favoring orthogonal directions unless a
     * diagonal one is a better option).
     */
    public Measurement measurement = Measurement.MANHATTAN;


    /**
     * Stores which parts of the map are accessible and which are not. Should not be changed unless the actual physical
     * terrain has changed. You should call initialize() with a new map instead of changing this directly.
     */
    public double[][] physicalMap;
    /**
     * The frequently-changing values that are often the point of using this class; goals will have a value of 0, and
     * any cells that can have a character reach a goal in n steps will have a value of n. Cells that cannot be
     * entered because they are solid will have a very high value equal to the WALL constant in this class, and cells
     * that cannot be entered because they cannot reach a goal will have a different very high value equal to the
     * DARK constant in this class.
     */
    public double[][] gradientMap;
    /**
     * This stores the entry cost multipliers for each cell; that is, a value of 1.0 is a normal, unmodified cell, but
     * a value of 0.5 can be entered easily (two cells of its cost can be entered for the cost of one 1.0 cell), and a
     * value of 2.0 can only be entered with difficulty (one cell of its cost can be entered for the cost of two 1.0
     * cells). Unlike the measurement field, this does affect the length of paths, as well as the numbers assigned
     * to gradientMap during a scan. The values for walls are identical to the value used by gradientMap, that is, this
     * class' WALL static final field. Floors, however, are never given FLOOR as a value, and default to 1.0 .
     */
    public double[][] costMap = null;

    public boolean standardCosts = true;
    /**
     * Height of the map. Exciting stuff. Don't change this, instead call initialize().
     */
    public int height;
    /**
     * Width of the map. Exciting stuff. Don't change this, instead call initialize().
     */
    public int width;
    /**
     * The latest path that was obtained by calling findPath(). It will not contain the value passed as a starting
     * cell; only steps that require movement will be included, and so if the path has not been found or a valid
     * path toward a goal is impossible, this ArrayList will be empty.
     */
    public ArrayList<Coord> path = new ArrayList<>();

    private GreasedRegion impassable2, friends;
    
    public boolean cutShort = false;

    /**
     * Goals are always marked with 0.
     */
    public static final double GOAL = 0.0;
    /**
     * Floor cells, which include any walkable cell, are marked with a high number equal to 999200.0 .
     */
    public static final double FLOOR = 999200.0;
    /**
     * Walls, which are solid no-entry cells, are marked with a high number equal to 999500.0 .
     */
    public static final double WALL = 999500.0;
    /**
     * This is used to mark cells that the scan couldn't reach, and these dark cells are marked with a high number
     * equal to 999800.0 .
     */
    public static final double DARK = 999800.0;
    /**
     * Goals that pathfinding will seek out. The Double value should almost always be 0.0 , the same as the static GOAL
     * constant in this class.
     */
    protected IntVLA goals = new IntVLA(256), fresh = new IntVLA(256);

    /**
     * The IRNG used to decide which one of multiple equally-short paths to take. You may want to give this a
     * {@link GWTRNG} if you may target web browsers with GWT, or a {@link RNG} or {@link StatefulRNG} otherwise.
     */
    public IRNG rng;
    private int frustration = 0;
    public Coord[][] targetMap;

    private Direction[] dirs = new Direction[9];

    private boolean initialized = false;

    private int mappedCount = 0;

    private int blockingRequirement = 2;

    /**
     * Construct a DijkstraMap without a level to actually scan. If you use this constructor, you must call an
     * initialize() method before using this class.
     */
    public DijkstraMap() {
        rng = new RNG();
        path = new ArrayList<>();
    }

    /**
     * Construct a DijkstraMap without a level to actually scan. This constructor allows you to specify an IRNG, such as
     * an {@link RNG}, before it is ever used in this class. If you use this constructor, you must call an initialize()
     * method before using any other methods in the class.
     */
    public DijkstraMap(IRNG random) {
        rng = random;
        path = new ArrayList<>();
    }

    /**
     * Used to construct a DijkstraMap from the output of another.
     *
     * @param level
     */
    public DijkstraMap(final double[][] level) {
        this(level, Measurement.MANHATTAN);
    }

    /**
     * Used to construct a DijkstraMap from the output of another, specifying a distance calculation.
     *
     * @param level
     * @param measurement
     */
    public DijkstraMap(final double[][] level, Measurement measurement) {
        rng = new RNG();
        this.measurement = measurement;
        path = new ArrayList<>();
        initialize(level);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here.
     *
     * @param level
     */
    public DijkstraMap(final char[][] level) {
        this(level, Measurement.MANHATTAN, new RNG());
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. Also takes an IRNG, such as an {@link RNG}, that ensures
     * predictable path choices given otherwise identical inputs and circumstances.
     *
     * @param level
     * @param rng   The RNG to use for certain decisions; only affects find* methods like findPath, not scan.
     */
    public DijkstraMap(final char[][] level, IRNG rng) {
        this(level, Measurement.MANHATTAN, rng);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(), or any other
     * char[][] where one char means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. You can specify the character used for walls.
     *
     * @param level
     */
    public DijkstraMap(final char[][] level, char alternateWall) {
        rng = new RNG();
        path = new ArrayList<>();

        initialize(level, alternateWall);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. This constructor specifies a distance measurement.
     *
     * @param level
     * @param measurement
     */
    public DijkstraMap(final char[][] level, Measurement measurement) {
        this(level, measurement, new RNG());
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. Also takes a distance measurement and an RNG that ensures
     * predictable path choices given otherwise identical inputs and circumstances.
     *
     * @param level
     * @param rng   The RNG to use for certain decisions; only affects find* methods like findPath, not scan.
     */
    public DijkstraMap(final char[][] level, Measurement measurement, IRNG rng) {
        this.rng = rng;
        path = new ArrayList<>();
        this.measurement = measurement;

        initialize(level);
    }

    /**
     * Used to initialize or re-initialize a DijkstraMap that needs a new physicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ).
     *
     * @param level a 2D double array that should be used as the physicalMap for this DijkstraMap
     * @return this for chaining
     */
    public DijkstraMap initialize(final double[][] level) {
        width = level.length;
        height = level[0].length;
        gradientMap = new double[width][height];
        physicalMap = new double[width][height];
        costMap = new double[width][height];
        targetMap = new Coord[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(level[x], 0, gradientMap[x], 0, height);
            System.arraycopy(level[x], 0, physicalMap[x], 0, height);
            Arrays.fill(costMap[x], 1.0);
        }
        standardCosts = true;
        impassable2 = new GreasedRegion(width, height);
        friends = new GreasedRegion(width, height);
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a DijkstraMap that needs a new physicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ).
     *
     * @param level a 2D char array that this will use to establish which cells are walls ('#' as wall, others as floor)
     * @return this for chaining
     */
    public DijkstraMap initialize(final char[][] level) {
        width = level.length;
        height = level[0].length;
        gradientMap = new double[width][height];
        physicalMap = new double[width][height];
        costMap = new double[width][height];
        targetMap = new Coord[width][height];
        for (int x = 0; x < width; x++) {
            Arrays.fill(costMap[x], 1.0);
            for (int y = 0; y < height; y++) {
                double t = (level[x][y] == '#') ? WALL : FLOOR;
                gradientMap[x][y] = t;
                physicalMap[x][y] = t;
            }
        }
        standardCosts = true;
        impassable2 = new GreasedRegion(width, height);
        friends = new GreasedRegion(width, height);
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a DijkstraMap that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ). This
     * initialize() method allows you to specify an alternate wall char other than the default character, '#' .
     *
     * @param level a 2D char array that this will use to establish which cells are walls (alternateWall defines the wall char, everything else is floor)
     * @param alternateWall the char to consider a wall when it appears in level
     * @return this for chaining
     */
    public DijkstraMap initialize(final char[][] level, char alternateWall) {
        width = level.length;
        height = level[0].length;
        gradientMap = new double[width][height];
        physicalMap = new double[width][height];
        costMap = new double[width][height];
        targetMap = new Coord[width][height];
        for (int x = 0; x < width; x++) {
            Arrays.fill(costMap[x], 1.0);
            for (int y = 0; y < height; y++) {
                double t = (level[x][y] == alternateWall) ? WALL : FLOOR;
                gradientMap[x][y] = t;
                physicalMap[x][y] = t;
            }
        }
        standardCosts = true;
        impassable2 = new GreasedRegion(width, height);
        friends = new GreasedRegion(width, height);
        initialized = true;
        return this;
    }

    /**
     * Used to initialize the entry cost modifiers for games that require variable costs to enter squares. This expects
     * a char[][] of the same exact dimensions as the 2D array that was used to previously initialize() this
     * DijkstraMap, treating the '#' char as a wall (impassable) and anything else as having a normal cost to enter.
     * The costs can be accessed later by using costMap directly (which will have a valid value when this does not
     * throw an exception), or by calling setCost().
     *
     * @param level a 2D char array that uses '#' for walls
     * @return this DijkstraMap for chaining.
     */
    public DijkstraMap initializeCost(final char[][] level) {
        if (!initialized) throw new IllegalStateException("DijkstraMap must be initialized first!");
        ArrayTools.fill(costMap, 1.0);
        standardCosts = true;
        return this;
    }

    /**
     * Used to initialize the entry cost modifiers for games that require variable costs to enter squares. This expects
     * a char[][] of the same exact dimensions as the 2D array that was used to previously initialize() this
     * DijkstraMap, treating the '#' char as a wall (impassable) and anything else as having a normal cost to enter.
     * The costs can be accessed later by using costMap directly (which will have a valid value when this does not
     * throw an exception), or by calling setCost().
     * <p/>
     * This method allows you to specify an alternate wall char other than the default character, '#' .
     *
     * @param level         a 2D char array that uses alternateChar for walls.
     * @param alternateWall a char to use to represent walls.
     * @return this DijkstraMap for chaining.
     */
    public DijkstraMap initializeCost(final char[][] level, char alternateWall) {
        if (!initialized) throw new IllegalStateException("DijkstraMap must be initialized first!");
        ArrayTools.fill(costMap, 1.0);
        standardCosts = true;
        return this;
    }

    /**
     * Used to initialize the entry cost modifiers for games that require variable costs to enter squares. This expects
     * a double[][] of the same exact dimensions as the 2D array that was used to previously initialize() this
     * DijkstraMap, using the exact values given in costs as the values to enter cells, even if they aren't what this
     * class would assign normally -- walls and other impassable values should be given WALL as a value, however.
     * The costs can be accessed later by using costMap directly (which will have a valid value when this does not
     * throw an exception), or by calling setCost(). Causes findPath() to always explore the full map instead of
     * stopping as soon as it finds any path, since unequal costs could make some paths cost less but be discovered
     * later in the pathfinding process.
     * <p/>
     * This method should be slightly more efficient than the other initializeCost methods.
     *
     * @param costs a 2D double array that already has the desired cost values
     * @return this DijkstraMap for chaining.
     */
    public DijkstraMap initializeCost(final double[][] costs) {
        if (!initialized) throw new IllegalStateException("DijkstraMap must be initialized first!");
        costMap = new double[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(costs[x], 0, costMap[x], 0, height);
        }
        standardCosts = false;
        return this;
    }

    /**
     * Internally, DijkstraMap uses int primitives instead of Coord objects, but the specific encoding depends on
     * this DijkstraMap's width and height. This method converts from a Coord to an encoded int that stores the same
     * information, but is specific to this width and height and is somewhat more efficient to work with.
     * @param point a Coord to find an encoded int for
     * @return an int that encodes the given Coord for this DijkstraMap's width and height
     */
    public int encode(final Coord point)
    {
        return width * point.y + point.x;
    }
    /**
     * Internally, DijkstraMap uses int primitives instead of Coord objects, but the specific encoding depends on
     * this DijkstraMap's width and height. This method converts from an x,y point to an encoded int that stores the
     * same information, but is specific to this width and height and is somewhat more efficient to work with.
     * @param x the x component of the point to find an encoded int for
     * @param y the y component of the point to find an encoded int for
     * @return an int that encodes the given x,y point for this DijkstraMap's width and height
     */
    public int encode(final int x, final int y)
    {
        return width * y + x;
    }

    /**
     * If you for some reason have one of the internally-used ints produced by {@link #encode(Coord)}, this will convert
     * it back to a Coord if you need it as such. You may prefer using {@link #decodeX(int)} and  {@link #decodeY(int)}
     * to get the x and y components independently and without involving objects.
     * @param encoded an encoded int specific to this DijkstraMap's height and width; see {@link #encode(Coord)}
     * @return the Coord that represents the same x,y position that the given encoded int stores
     */
    public Coord decode(final int encoded)
    {
        return Coord.get(encoded % width, encoded / width);
    }

    /**
     * If you for some reason have one of the internally-used ints produced by {@link #encode(Coord)}, this will decode
     * the x component of the point encoded in that int. This is an extremely simple method that is equivalent to the
     * code {@code encoded % width}, where width is a public field in this class. You probably would use this method in
     * conjunction with {@link #decodeY(int)}, or would instead use {@link #decode(int)} to get a Coord.
     * @param encoded an encoded int specific to this DijkstraMap's height and width; see {@link #encode(Coord)}
     * @return the x component of the position that the given encoded int stores
     */
    public int decodeX(final int encoded)
    {
        return encoded % width;
    }

    /**
     * If you for some reason have one of the internally-used ints produced by {@link #encode(Coord)}, this will decode
     * the y component of the point encoded in that int. This is an extremely simple method that is equivalent to the
     * code {@code encoded / width}, where width is a public field in this class. You probably would use this method in
     * conjunction with {@link #decodeX(int)}, or would instead use {@link #decode(int)} to get a Coord.
     * @param encoded an encoded int specific to this DijkstraMap's height and width; see {@link #encode(Coord)}
     * @return the y component of the position that the given encoded int stores
     */
    public int decodeY(final int encoded)
    {
        return encoded / width;
    }

    /**
     * Gets the appropriate DijkstraMap.Measurement to pass to a constructor if you already have a Radius.
     * Matches SQUARE or CUBE to CHEBYSHEV, DIAMOND or OCTAHEDRON to MANHATTAN, and CIRCLE or SPHERE to EUCLIDEAN.
     *
     * @param radius the Radius to find the corresponding Measurement for
     * @return a DijkstraMap.Measurement that matches radius; SQUARE to CHEBYSHEV, DIAMOND to MANHATTAN, etc.
     */
    public static Measurement findMeasurement(Radius radius) {
        switch (radius)
        {
            case CUBE:
            case SQUARE:
                return Measurement.CHEBYSHEV;
            case DIAMOND:
            case OCTAHEDRON:
                return Measurement.MANHATTAN;
            default:
                return Measurement.EUCLIDEAN;
        }
    }

    /**
     * Gets the appropriate Radius corresponding to a DijkstraMap.Measurement.
     * Matches CHEBYSHEV to SQUARE, MANHATTAN to DIAMOND, and EUCLIDEAN to CIRCLE.
     * <br>
     * See also {@link Measurement#matchingRadius()} as a method on Measurement.
     * @param measurement the Measurement to find the corresponding Radius for
     * @return a Radius that matches measurement; CHEBYSHEV to SQUARE, MANHATTAN to DIAMOND, etc.
     */
    public static Radius findRadius(Measurement measurement) {
        switch (measurement) {
            case CHEBYSHEV:
                return Radius.SQUARE;
            case EUCLIDEAN:
                return Radius.CIRCLE;
            default:
                return Radius.DIAMOND;
        }
    }

    /**
     * Resets the gradientMap to its original value from physicalMap.
     */
    public void resetMap() {
        if (!initialized) return;
        for (int x = 0; x < width; x++) {
            System.arraycopy(physicalMap[x], 0, gradientMap[x], 0, height);
        }
    }

    /**
     * Resets the targetMap (which is only assigned in the first place if you use findTechniquePath() ).
     */
    public void resetTargetMap() {
        if (!initialized) return;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                targetMap[x][y] = null;
            }
        }
    }


    /**
     * Resets this DijkstraMap to a state with no goals, no discovered path, and no changes made to gradientMap
     * relative to physicalMap.
     */
    public void reset() {
        resetMap();
        resetTargetMap();
        goals.clear();
        path.clear();
        fresh.clear();
        frustration = 0;
    }

    /**
     * Marks a cell as a goal for pathfinding, unless the cell is a wall or unreachable area (then it does nothing).
     *
     * @param x
     * @param y
     */
    public void setGoal(int x, int y) {
        if (!initialized || x < 0 || x >= width || y < 0 || y >= height) return;
        if (physicalMap[x][y] > FLOOR) {
            return;
        }

        goals.add(encode(x, y));
        gradientMap[x][y] = 0.0;
    }

    /**
     * Marks a cell as a goal for pathfinding, unless the cell is a wall or unreachable area (then it does nothing).
     *
     * @param pt
     */
    public void setGoal(Coord pt) {
        if (!initialized || !pt.isWithin(width, height)) return;
        if (physicalMap[pt.x][pt.y] > FLOOR) {
            return;
        }

        goals.add(encode(pt));
        gradientMap[pt.x][pt.y] = 0.0;

    }
    /**
     * Marks many cells as goals for pathfinding, ignoring cells in walls or unreachable areas. Possibly more efficient
     * than a loop that calls {@link #setGoal(Coord)} over and over, since this doesn't need to do a bounds check. The
     * GreasedRegion passed to this should have the same (or smaller) width and height as this DijkstraMap.
     *
     * @param pts a GreasedRegion containing "on" cells to treat as goals; should have the same width and height as this
     */
    public void setGoals(GreasedRegion pts) {
        if (!initialized || pts.width > width || pts.height > height) return;
        int[] enc = new GreasedRegion(physicalMap, FLOOR).and(pts).asTightEncoded();
        for(Coord c : pts)
        {
            if(physicalMap[c.x][c.y] <= FLOOR) {
                goals.add(encode(c));
                gradientMap[c.x][c.y] = 0.0;
            }
        }
    }

    /**
     * Marks many cells as goals for pathfinding, ignoring cells in walls or unreachable areas. Simply loops through
     * pts and calls {@link #setGoal(Coord)} on each Coord in pts.
     * If you have a GreasedRegion, you should use it with {@link #setGoals(GreasedRegion)}, which is faster.
     * @param pts any Iterable of Coord, which can be a List, Set, Queue, etc. of Coords to mark as goals
     */
    public void setGoals(Iterable<Coord> pts) {
        if (!initialized) return;
        for(Coord c : pts)
        {
            setGoal(c);
        }
    }
    /**
     * Marks many cells as goals for pathfinding, ignoring cells in walls or unreachable areas. Simply loops through
     * pts and calls {@link #setGoal(Coord)} on each Coord in pts.
     * If you have a GreasedRegion, you should use it with {@link #setGoals(GreasedRegion)}, which is faster.
     * @param pts an array of Coord to mark as goals
     */
    public void setGoals(Coord[] pts) {
        if (!initialized) return;
        for(int i = 0; i < pts.length; i++)
        {
            setGoal(pts[i]);
        }
    }

    /**
     * Marks a cell's cost for pathfinding as cost, unless the cell is a wall or unreachable area (then it always sets
     * the cost to the value of the WALL field).
     *
     * @param pt
     * @param cost
     */
    public void setCost(Coord pt, double cost) {
        if (!initialized || !pt.isWithin(width, height)) return;
        if (physicalMap[pt.x][pt.y] > FLOOR) {
            costMap[pt.x][pt.y] = 1.0;
            return;
        }
        if(cost != 1.0)
            standardCosts = false;
        costMap[pt.x][pt.y] = cost;
    }

    /**
     * Marks a cell's cost for pathfinding as cost, unless the cell is a wall or unreachable area (then it always sets
     * the cost to the value of the WALL field).
     *
     * @param x
     * @param y
     * @param cost
     */
    public void setCost(int x, int y, double cost) {
        if (!initialized || x < 0 || x >= width || y < 0 || y >= height) return;
        if (physicalMap[x][y] > FLOOR) {
            costMap[x][y] = 1.0;
            return;
        }
        if(cost != 1.0)
            standardCosts = false;
        costMap[x][y] = cost;
    }

    /**
     * Marks a specific cell in gradientMap as completely impossible to enter.
     *
     * @param x
     * @param y
     */
    public void setOccupied(int x, int y) {
        if (!initialized || x < 0 || x >= width || y < 0 || y >= height) return;
        gradientMap[x][y] = WALL;
    }

    /**
     * Reverts a cell to the value stored in the original state of the level as known by physicalMap.
     *
     * @param x
     * @param y
     */
    public void resetCell(int x, int y) {
        if (!initialized || x < 0 || x >= width || y < 0 || y >= height) return;
        gradientMap[x][y] = physicalMap[x][y];
    }

    /**
     * Reverts a cell to the value stored in the original state of the level as known by physicalMap.
     *
     * @param pt
     */
    public void resetCell(Coord pt) {
        if (!initialized || !pt.isWithin(width, height)) return;
        gradientMap[pt.x][pt.y] = physicalMap[pt.x][pt.y];
    }

    /**
     * Used to remove all goals and undo any changes to gradientMap made by having a goal present.
     */
    public void clearGoals() {
        if (!initialized)
            return;
        int sz = goals.size, t;
        for (int i = 0; i < sz; i++) {
            resetCell(decodeX(t = goals.pop()), decodeY(t));
        }
    }

    private void setFresh(final int x, final int y, double counter) {
        if (x < 0 || x >= width || y < 0 || y >= height || gradientMap[x][y] < counter)
            return;
        gradientMap[x][y] = counter;
        fresh.add(encode(x, y));
    }

    private void setFresh(final Coord pt, double counter) {
        if (!pt.isWithin(width, height) || gradientMap[pt.x][pt.y] < counter)
            return;
        gradientMap[pt.x][pt.y] = counter;
        fresh.add(encode(pt));
    }

    /**
     * Recalculate the Dijkstra map and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @return A 2D double[width][height] using the width and height of what this knows about the physical map.
     */
    public double[][] scan() {
        return scan(null);
    }

    /**
     * Recalculate the Dijkstra map and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param impassable A Collection of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D double[width][height] using the width and height of what this knows about the physical map.
     */
    public double[][] scan(final Collection<Coord> impassable) {
        scan(null, impassable);
        double[][] gradientClone = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
            System.arraycopy(gradientMap[x], 0, gradientClone[x], 0, height);
        }

        return gradientClone;
    }

    /**
     * Recalculate the Dijkstra map and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field, and nothing is returned.
     * If you want the data returned, you can use {@link #scan(Collection)} (which calls this method with
     * null for the start parameter, then modifies the gradientMap field and returns a copy), or you can
     * just retrieve the gradientMap (maybe copying it; {@link squidpony.ArrayTools#copy(double[][])} is a
     * convenient option for copying a 2D double array). If start is non-null, which is usually used when
     * finding a single path, then cells that didn't need to be explored (because they were further than the
     * path needed to go from start to goal) will have the value {@link #FLOOR}. You may wish to assign a
     * different value to these cells in some cases (especially if start is null, which means any cells that
     * are still FLOOR could not be reached from any goal), and the overloads of scan that return 2D double
     * arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to {@link #WALL}.
     *
     * @param start a Coord representing the location of the pathfinder; may be null, which has this scan the whole map
     * @param impassable A Collection of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     */
    public void scan(final Coord start, final Collection<Coord> impassable) {

        if (!initialized) return;
        if (impassable != null && !impassable.isEmpty()) {
            for (Coord pt : impassable) {
                if(pt != null && pt.isWithin(width, height))
                    gradientMap[pt.x][pt.y] = WALL;
            }
        }
        int dec, adjX, adjY, cen, cenX, cenY;

        for (int i = 0; i < goals.size; i++) {
            dec = goals.get(i);
            gradientMap[decodeX(dec)][decodeY(dec)] = GOAL;
        }
        double currentLowest = 999000, cs, dist;
        fresh.clear();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] <= FLOOR) {
                    if (gradientMap[x][y] < currentLowest) {
                        currentLowest = gradientMap[x][y];
                        fresh.clear();
                        fresh.add(encode(x, y));
                    } else if (gradientMap[x][y] == currentLowest) {
                        fresh.add(encode(x, y));
                    }
                }
            }
        }
        int fsz, numAssigned = fresh.size;
        mappedCount = goals.size;
        Direction[] moveDirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        while (numAssigned > 0) {
            numAssigned = 0;
            fsz = fresh.size;
            for (int ci = fsz-1; ci >= 0; ci--) {
                cen = fresh.removeIndex(ci);
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientMap[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if(d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if((gradientMap[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientMap[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement)
                        {
                            continue;
                        }
                    }
                    double h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h * costMap[adjX][adjY];
                    if (gradientMap[adjX][adjY] <= FLOOR && cs < gradientMap[adjX][adjY]) {
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                        ++mappedCount;
                        if(start != null && start.x == adjX && start.y == adjY && standardCosts)
                        {
                            if (impassable != null && !impassable.isEmpty()) {
                                for (Coord pt : impassable) {
                                    if(pt != null && pt.isWithin(width, height))
                                        gradientMap[pt.x][pt.y] = physicalMap[pt.x][pt.y];
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
        if (impassable != null && !impassable.isEmpty()) {
            for (Coord pt : impassable) {
                if(pt != null && pt.isWithin(width, height))
                    gradientMap[pt.x][pt.y] = physicalMap[pt.x][pt.y];
            }
        }
    }

    /**
     * Recalculate the Dijkstra map up to a limit and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. If a cell would take more steps to
     * reach than the given limit, it will have a value of DARK if it was passable instead of the distance. The
     * exceptions are walls, which will have a value defined by the WALL constant in this class, and areas that the scan
     * was unable to reach, which will have a value defined by the DARK constant in this class. This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @return A 2D double[width][height] using the width and height of what this knows about the physical map.
     */
    public double[][] partialScan(final int limit) {
        return partialScan(limit, null);
    }

    /**
     * Recalculate the Dijkstra map up to a limit and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. If a cell would take more steps to
     * reach than the given limit, it will have a value of DARK if it was passable instead of the distance. The
     * exceptions are walls, which will have a value defined by the WALL constant in this class, and areas that the scan
     * was unable to reach, which will have a value defined by the DARK constant in this class. This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param impassable A Collection of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D double[width][height] using the width and height of what this knows about the physical map.
     */
    public double[][] partialScan(final int limit, final Collection<Coord> impassable) {
        partialScan(null, limit, impassable);
        double[][] gradientClone = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
            System.arraycopy(gradientMap[x], 0, gradientClone[x], 0, height);
        }

        return gradientClone;
    }

    /**
     * Recalculate the Dijkstra map up to a limit and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. If a cell would take more steps to
     * reach than the given limit, it will have a value of DARK if it was passable instead of the distance. The
     * exceptions are walls, which will have a value defined by the WALL constant in this class, and areas that the scan
     * was unable to reach, which will have a value defined by the DARK constant in this class. This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field, and nothing is returned.
     * If you want the data returned, you can use {@link #partialScan(int, Collection)} (which calls this method with
     * null for the start parameter, then modifies the gradientMap field and returns a copy), or you can
     * just retrieve the gradientMap (maybe copying it; {@link squidpony.ArrayTools#copy(double[][])} is a
     * convenient option for copying a 2D double array). If start is non-null, which is usually used when
     * finding a single path, then cells that didn't need to be explored (because they were further than the
     * path needed to go from start to goal) will have the value {@link #FLOOR}. You may wish to assign a
     * different value to these cells in some cases (especially if start is null, which means any cells that
     * are still FLOOR could not be reached from any goal), and the overloads of partialScan that return 2D double
     * arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to {@link #WALL}.
     *
     * @param start a Coord representing the location of the pathfinder; may be null to have this scan more of the map
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param impassable A Collection of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     */
    public void partialScan(final Coord start, final int limit, final Collection<Coord> impassable) {

        if (!initialized || limit <= 0) return;
        if (impassable != null && !impassable.isEmpty()) {
            for (Coord pt : impassable) {
                if(pt != null && pt.isWithin(width, height))
                    gradientMap[pt.x][pt.y] = WALL;
            }
        }
        int dec, adjX, adjY, cen, cenX, cenY;

        for (int i = 0; i < goals.size; i++) {
            //if (closed.containsKey(entry.getIntKey()))
            //    continue;
            //    closed.remove(entry.getIntKey());
            dec = goals.get(i);
            gradientMap[decodeX(dec)][decodeY(dec)] = GOAL;
        }
        double currentLowest = 999000, cs, dist;
        fresh.clear();
        if(start == null) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (gradientMap[x][y] <= FLOOR) {
                        if (gradientMap[x][y] < currentLowest) {
                            currentLowest = gradientMap[x][y];
                            fresh.clear();
                            fresh.add(encode(x, y));
                        } else if (gradientMap[x][y] == currentLowest) {
                            fresh.add(encode(x, y));
                        }
                    }
                }
            }
        } else{
            final int x0 = Math.max(0, start.x - limit), x1 = Math.min(start.x + limit + 1, width),
                    y0 = Math.max(0, start.y - limit), y1 = Math.min(start.y + limit + 1, height);
            for (int x = x0; x < x1; x++) {
                for (int y = y0; y < y1; y++) {
                    if (gradientMap[x][y] <= FLOOR) {
                        if (gradientMap[x][y] < currentLowest) {
                            currentLowest = gradientMap[x][y];
                            fresh.clear();
                            fresh.add(encode(x, y));
                        } else if (gradientMap[x][y] == currentLowest) {
                            fresh.add(encode(x, y));
                        }
                    }
                }
            }
        }
        int fsz, numAssigned = fresh.size;
        mappedCount = goals.size;
        Direction[] moveDirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        int iter = 0;
        while (numAssigned > 0 && iter++ < limit) {
            numAssigned = 0;
            fsz = fresh.size;
            for (int ci = fsz-1; ci >= 0; ci--) {
                cen = fresh.removeIndex(ci);
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientMap[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if(d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if((gradientMap[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientMap[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement)
                        {
                            continue;
                        }
                    }
                    double h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h * costMap[adjX][adjY];
                    if (gradientMap[adjX][adjY] <= FLOOR && cs < gradientMap[adjX][adjY]) {
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                        ++mappedCount;
                        if(start != null && start.x == adjX && start.y == adjY && standardCosts)
                        {
                            if (impassable != null && !impassable.isEmpty()) {
                                for (Coord pt : impassable) {
                                    if(pt != null && pt.isWithin(width, height))
                                        gradientMap[pt.x][pt.y] = physicalMap[pt.x][pt.y];
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
        if (impassable != null && !impassable.isEmpty()) {
            for (Coord pt : impassable) {
                if(pt != null && pt.isWithin(width, height))
                    gradientMap[pt.x][pt.y] = physicalMap[pt.x][pt.y];
            }
        }
    }

    /**
     * Recalculate the Dijkstra map until it reaches a Coord in targets, then returns the first target found.
     * This uses the current measurement.
     *
     * @param start   the cell to use as the origin for finding the nearest target
     * @param targets the Coords that this is trying to find; it will stop once it finds one
     * @return the Coord that it found first.
     */
    public Coord findNearest(Coord start, Collection<Coord> targets) {
        if (!initialized) return null;
        if (targets == null)
            return null;
        if (targets.contains(start))
            return start;
        resetMap();
        Coord start2 = start;
        int xShift = width / 6, yShift = height / 6;
        while (physicalMap[start.x][start.y] >= WALL && frustration < 50) {
            start2 = Coord.get(Math.min(Math.max(1, start.x + rng.nextInt(1 + xShift * 2) - xShift), width - 2),
                    Math.min(Math.max(1, start.y + rng.nextInt(1 + yShift * 2) - yShift), height - 2));
        }
        gradientMap[start2.x][start2.y] = 0.0;
        int adjX, adjY, cen, cenX, cenY;
        double cs, dist;
        Coord adj;
        fresh.clear();
        fresh.add(encode(start2));
        int fsz, numAssigned = 1;
        mappedCount = 1;
        Direction[] moveDirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        while (numAssigned > 0) {
            numAssigned = 0;
            fsz = fresh.size;
            for (int ci = fsz-1; ci >= 0; ci--) {
                cen = fresh.removeIndex(ci);
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientMap[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if(d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if((gradientMap[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientMap[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement)
                        {
                            continue;
                        }
                    }
                    double h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h * costMap[adjX][adjY];
                    if (gradientMap[adjX][adjY] <= FLOOR && cs < gradientMap[adjX][adjY]) {
                        ++mappedCount;
                        if (targets.contains(adj = Coord.get(adjX, adjY))) {
                            fresh.clear();
                            return adj;
                        }
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Recalculate the Dijkstra map until it reaches a Coord in targets, then returns the first target found.
     * This uses the current measurement.
     *
     * @param start   the cell to use as the origin for finding the nearest target
     * @param targets the Coords that this is trying to find; it will stop once it finds one
     * @return the Coord that it found first.
     */
    public Coord findNearest(Coord start, Coord... targets) {
        return findNearest(start, new OrderedSet<>(targets));
    }

    /**
     * If you have a target or group of targets you want to pathfind to without scanning the full map, this can be good.
     * It may find sub-optimal paths in the presence of costs to move into cells. It is useful when you want to move in
     * a straight line to a known nearby goal.
     *
     * @param start   your starting location
     * @param targets an array or vararg of Coords to pathfind to the nearest of
     * @return an ArrayList of Coord that goes from a cell adjacent to start and goes to one of the targets. Copy of path.
     */
    public ArrayList<Coord> findShortcutPath(Coord start, Coord... targets) {
        if (targets.length == 0) {
            cutShort = true;
            path.clear();
            return new ArrayList<>(path);
        }
        Coord currentPos = findNearest(start, targets);
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d < measurement.directionCount() + 1; d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if(!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                return new ArrayList<>(path);
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
            path.add(currentPos);
            frustration++;
        }
        frustration = 0;
        cutShort = false;
        Collections.reverse(path);
        return new ArrayList<>(path);

    }

    /**
     * Recalculate the Dijkstra map until it reaches a Coord in targets, then returns the first several targets found,
     * up to limit or less if the map is fully searched without finding enough.
     * This uses the current measurement.
     *
     * @param start   the cell to use as the origin for finding the nearest targets
     * @param limit   the maximum number of targets to find before returning
     * @param targets the Coords that this is trying to find; it will stop once it finds enough (based on limit)
     * @return the Coords that it found first.
     */
    public ArrayList<Coord> findNearestMultiple(Coord start, int limit, Collection<Coord> targets) {
        if (!initialized) return null;
        ArrayList<Coord> found = new ArrayList<>(limit);
        if (targets == null)
            return found;
        if (targets.contains(start))
            return found;
        resetMap();
        Coord start2 = start;
        int xShift = width / 6, yShift = height / 6;
        while (physicalMap[start.x][start.y] >= WALL && frustration < 50) {
            start2 = Coord.get(Math.min(Math.max(1, start.x + rng.nextInt(1 + xShift * 2) - xShift), width - 2),
                    Math.min(Math.max(1, start.y + rng.nextInt(1 + yShift * 2) - yShift), height - 2));
        }
        gradientMap[start2.x][start2.y] = 0.0;
        int adjX, adjY, cen, cenX, cenY;
        double cs, dist;
        Coord adj;
        fresh.clear();
        fresh.add(encode(start2));
        int fsz, numAssigned = 1;
        mappedCount = 1;
        Direction[] moveDirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        while (numAssigned > 0) {
            numAssigned = 0;
            fsz = fresh.size;
            for (int ci = fsz-1; ci >= 0; ci--) {
                cen = fresh.removeIndex(ci);
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientMap[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if(d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if((gradientMap[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientMap[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement)
                        {
                            continue;
                        }
                    }
                    double h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h * costMap[adjX][adjY];
                    if (gradientMap[adjX][adjY] <= FLOOR && cs < gradientMap[adjX][adjY]) {
                        ++mappedCount;
                        if (targets.contains(adj = Coord.get(adjX, adjY))) {
                            found.add(adj);
                            if (found.size() >= limit) {
                                fresh.clear();
                                return found;
                            }
                        }
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                    }
                }
            }
        }
        return found;
    }

    /**
     * Recalculate the Dijkstra map for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned Dijkstra map assumes that a creature is square, with a side length equal to the passed
     * size, that its minimum-x, minimum-y cell is the starting cell, and that any cell with a distance number
     * represents the distance for the creature's minimum-x, minimum-y cell to reach it. Cells that cannot be entered
     * by the minimum-x, minimum-y cell because of sizing (such as a floor cell next to a maximum-x and/or maximum-y
     * wall if size is &gt; 1) will be marked as DARK. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class. (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.  The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param impassable A Collection of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @return A 2D double[width][height] using the width and height of what this knows about the physical map.
     */
    public double[][] scan(final Collection<Coord> impassable, final int size) {
        scan(null, impassable, size);
        double[][] gradientClone = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
            System.arraycopy(gradientMap[x], 0, gradientClone[x], 0, height);
        }
        return gradientClone;

    }

    /**
     * Recalculate the Dijkstra map for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned Dijkstra map assumes that a creature is square, with a side length equal to the passed
     * size, that its minimum-x, minimum-y cell is the starting cell, and that any cell with a distance number
     * represents the distance for the creature's minimum-x, minimum-y cell to reach it. Cells that cannot be entered
     * by the minimum-x, minimum-y cell because of sizing (such as a floor cell next to a maximum-x and/or maximum-y
     * wall if size is &gt; 1) will be marked as DARK. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class. (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.  The result is stored in the {@link #gradientMap} field, and nothing is returned.
     * If you want the data returned, you can use {@link #scan(Collection, int)} (which calls this method with
     * null for the start parameter, then modifies the gradientMap field and returns a copy), or you can
     * just retrieve the gradientMap (maybe copying it; {@link squidpony.ArrayTools#copy(double[][])} is a
     * convenient option for copying a 2D double array). If start is non-null, which is usually used when
     * finding a single path, then cells that didn't need to be explored (because they were further than the
     * path needed to go from start to goal) will have the value {@link #FLOOR}. You may wish to assign a
     * different value to these cells in some cases (especially if start is null, which means any cells that
     * are still FLOOR could not be reached from any goal), and the overloads of scan that return 2D double
     * arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to {@link #WALL}.
     *
     * @param impassable A Collection of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     */
    public void scan(final Coord start, final Collection<Coord> impassable, final int size) {

        if (!initialized) return;
        double[][] gradientClone = ArrayTools.copy(gradientMap);
        if (impassable != null && !impassable.isEmpty()) {
            for (Coord pt : impassable) {
                if(pt != null && pt.isWithin(width, height))
                    gradientMap[pt.x][pt.y] = WALL;
            }
        }
        for (int xx = size; xx < width; xx++) {
            for (int yy = size; yy < height; yy++) {
                if(gradientMap[xx][yy] > FLOOR) {
                    for (int xs = xx, xi = 0; xi < size && xs >= 0; xs--, xi++) {
                        for (int ys = yy, yi = 0; yi < size && ys >= 0; ys--, yi++) {
                            gradientClone[xs][ys] = WALL;
                        }
                    }
                }
            }
        }
        int dec, adjX, adjY, cen, cenX, cenY;

        PER_GOAL:
        for (int i = 0; i < goals.size; i++) {
            dec = goals.get(i);
            for (int xs = decodeX(dec), xi = 0; xi < size && xs >= 0; xs--, xi++) {
                for (int ys = decodeY(dec), yi = 0; yi < size && ys >= 0; ys--, yi++) {
                    if(physicalMap[xs][ys] > FLOOR)
                        continue PER_GOAL;
                    gradientClone[xs][ys] = GOAL;
                }
            }
        }
        double currentLowest = 999000, cs, dist;
        fresh.clear();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientClone[x][y] <= FLOOR) {
                    if (gradientClone[x][y] < currentLowest) {
                        currentLowest = gradientClone[x][y];
                        fresh.clear();
                        fresh.add(encode(x, y));
                    } else if (gradientClone[x][y] == currentLowest) {
                        fresh.add(encode(x, y));
                    }
                }
            }
        }
        int fsz, numAssigned = fresh.size;
        mappedCount = goals.size;
        Direction[] moveDirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        while (numAssigned > 0) {
            numAssigned = 0;
            fsz = fresh.size;
            for (int ci = fsz-1; ci >= 0; ci--) {
                cen = fresh.removeIndex(ci);
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientClone[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if(d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if((gradientClone[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientClone[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement)
                        {
                            continue;
                        }
                    }
                    double h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h * costMap[adjX][adjY];
                    if (gradientClone[adjX][adjY] <= FLOOR && cs < gradientClone[adjX][adjY]) {
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                        ++mappedCount;
                        if(start != null && start.x == adjX && start.y == adjY && standardCosts)
                        {
                            if (impassable != null && !impassable.isEmpty()) {
                                for (Coord pt : impassable) {
                                    if(pt != null && pt.isWithin(width, height)) {
                                        for (int xs = pt.x, xi = 0; xi < size && xs >= 0; xs--, xi++) {
                                            for (int ys = pt.y, yi = 0; yi < size && ys >= 0; ys--, yi++) {
                                                gradientClone[xs][ys] = physicalMap[xs][ys];
                                            }
                                        }
                                    }
                                }
                            }
                            gradientMap = gradientClone;
                            return;
                        }
                    }
                }
            }
        }
        if (impassable != null && !impassable.isEmpty()) {
            for (Coord pt : impassable) {
                if(pt != null && pt.isWithin(width, height)) {
                    for (int xs = pt.x, xi = 0; xi < size && xs >= 0; xs--, xi++) {
                        for (int ys = pt.y, yi = 0; yi < size && ys >= 0; ys--, yi++) {
                            gradientClone[xs][ys] = physicalMap[xs][ys];
                        }
                    }
                }
            }
        }
        gradientMap = gradientClone;
    }


    /**
     * Recalculate the Dijkstra map for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned Dijkstra map assumes that a creature is square, with a side length equal to the passed
     * size, that its minimum-x, minimum-y cell is the starting cell, and that any cell with a distance number
     * represents the distance for the creature's minimum-x, minimum-y cell to reach it. Cells that cannot be entered
     * by the minimum-x, minimum-y cell because of sizing (such as a floor cell next to a maximum-x and/or maximum-y
     * wall if size is &gt; 1) will be marked as DARK. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class. (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.  The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param impassable A Collection of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @return A 2D double[width][height] using the width and height of what this knows about the physical map.
     */
    public double[][] partialScan(final int limit, final Collection<Coord> impassable, final int size) {
        partialScan(limit,null, impassable, size);
        double[][] gradientClone = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
            System.arraycopy(gradientMap[x], 0, gradientClone[x], 0, height);
        }
        return gradientClone;

    }

    /**
     * Recalculate the Dijkstra map for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned Dijkstra map assumes that a creature is square, with a side length equal to the passed
     * size, that its minimum-x, minimum-y cell is the starting cell, and that any cell with a distance number
     * represents the distance for the creature's minimum-x, minimum-y cell to reach it. Cells that cannot be entered
     * by the minimum-x, minimum-y cell because of sizing (such as a floor cell next to a maximum-x and/or maximum-y
     * wall if size is &gt; 1) will be marked as DARK. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class. (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.  The result is stored in the {@link #gradientMap} field, and nothing is returned.
     * If you want the data returned, you can use {@link #partialScan(int, Collection, int)} (which calls this method
     * with null for the start parameter, then modifies the gradientMap field and returns a copy), or you can
     * just retrieve the gradientMap (maybe copying it; {@link squidpony.ArrayTools#copy(double[][])} is a
     * convenient option for copying a 2D double array). If start is non-null, which is usually used when
     * finding a single path, then cells that didn't need to be explored (because they were further than the
     * path needed to go from start to goal) will have the value {@link #FLOOR}. You may wish to assign a
     * different value to these cells in some cases (especially if start is null, which means any cells that
     * are still FLOOR could not be reached from any goal), and the overloads of partialScan that return 2D double
     * arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to {@link #WALL}.
     *
     * @param impassable A Collection of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     */
    public void partialScan(final int limit, final Coord start, final Collection<Coord> impassable, final int size) {

        if (!initialized || limit <= 0) return;
        double[][] gradientClone = ArrayTools.copy(gradientMap);
        if (impassable != null && !impassable.isEmpty()) {
            for (Coord pt : impassable) {
                if(pt != null && pt.isWithin(width, height))
                    gradientMap[pt.x][pt.y] = WALL;
            }
        }
        for (int xx = size; xx < width; xx++) {
            for (int yy = size; yy < height; yy++) {
                if(gradientMap[xx][yy] > FLOOR) {
                    for (int xs = xx, xi = 0; xi < size && xs >= 0; xs--, xi++) {
                        for (int ys = yy, yi = 0; yi < size && ys >= 0; ys--, yi++) {
                            gradientClone[xs][ys] = WALL;
                        }
                    }
                }
            }
        }
        int dec, adjX, adjY, cen, cenX, cenY;

        PER_GOAL:
        for (int i = 0; i < goals.size; i++) {
            dec = goals.get(i);
            for (int xs = decodeX(dec), xi = 0; xi < size && xs >= 0; xs--, xi++) {
                for (int ys = decodeY(dec), yi = 0; yi < size && ys >= 0; ys--, yi++) {
                    if(physicalMap[xs][ys] > FLOOR)
                        continue PER_GOAL;
                    gradientClone[xs][ys] = GOAL;
                }
            }
        }
        double currentLowest = 999000, cs, dist;
        fresh.clear();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientClone[x][y] <= FLOOR) {
                    if (gradientClone[x][y] < currentLowest) {
                        currentLowest = gradientClone[x][y];
                        fresh.clear();
                        fresh.add(encode(x, y));
                    } else if (gradientClone[x][y] == currentLowest) {
                        fresh.add(encode(x, y));
                    }
                }
            }
        }
        int fsz, numAssigned = fresh.size;
        mappedCount = goals.size;
        Direction[] moveDirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        int iter = 0;
        while (numAssigned > 0 && iter++ < limit) {
            numAssigned = 0;
            fsz = fresh.size;
            for (int ci = fsz-1; ci >= 0; ci--) {
                cen = fresh.removeIndex(ci);
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientClone[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if(d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if((gradientClone[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientClone[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement)
                        {
                            continue;
                        }
                    }
                    double h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h * costMap[adjX][adjY];
                    if (gradientClone[adjX][adjY] <= FLOOR && cs < gradientClone[adjX][adjY]) {
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                        ++mappedCount;
                        if(start != null && start.x == adjX && start.y == adjY && standardCosts)
                        {
                            if (impassable != null && !impassable.isEmpty()) {
                                for (Coord pt : impassable) {
                                    if(pt != null && pt.isWithin(width, height)) {
                                        for (int xs = pt.x, xi = 0; xi < size && xs >= 0; xs--, xi++) {
                                            for (int ys = pt.y, yi = 0; yi < size && ys >= 0; ys--, yi++) {
                                                gradientClone[xs][ys] = physicalMap[xs][ys];
                                            }
                                        }
                                    }
                                }
                            }
                            gradientMap = gradientClone;
                            return;
                        }
                    }
                }
            }
        }
        if (impassable != null && !impassable.isEmpty()) {
            for (Coord pt : impassable) {
                if(pt != null && pt.isWithin(width, height)) {
                    for (int xs = pt.x, xi = 0; xi < size && xs >= 0; xs--, xi++) {
                        for (int ys = pt.y, yi = 0; yi < size && ys >= 0; ys--, yi++) {
                            gradientClone[xs][ys] = physicalMap[xs][ys];
                        }
                    }
                }
            }
        }
        gradientMap = gradientClone;
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to the closest reachable
     * goal. The maximum length of the returned list is given by length, which represents movement in a system where
     * a single move can be multiple cells if length is greater than 1 and should usually be 1 in standard roguelikes;
     * if moving the full length of the list would place the mover in a position shared  by one of the positions in
     * onlyPassable (which is typically filled with friendly units that can be passed through in multi-cell-movement
     * scenarios), it will recalculate a move so that it does not pass into that cell. The keys in impassable should
     * be the positions of enemies and obstacles that cannot be moved  through, and will be ignored if there is a goal
     * overlapping one. This overload always scans the whole map; use
     * {@link #findPath(int, int, Collection, Collection, Coord, Coord...)} to scan a smaller area for performance reasons.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     * @param length       the length of the path to calculate
     * @param impassable   a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start        the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets      a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ArrayList<Coord> findPath(int length, Collection<Coord> impassable,
                                     Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        return findPath(length, -1, impassable, onlyPassable, start, targets);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan or DijkstraMap.partialScan with the listed goals and start
     * point, and returns a list of Coord positions (using the current measurement) needed to get closer
     * to the closest reachable goal. The maximum length of the returned list is given by length, which represents
     * movement in a system where a single move can be multiple cells if length is greater than 1 and should usually
     * be 1 in standard roguelikes; if moving the full length of the list would place the mover in a position shared
     * by one of the positions in onlyPassable (which is typically filled with friendly units that can be passed
     * through in multi-cell-movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * The full map will only be scanned if scanLimit is 0 or less; for positive scanLimit values this will scan only
     * that distance out from each goal, which can save processing time on maps where only a small part matters.
     * Generally, scanLimit should be significantly greater than length.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param length       the length of the path to calculate
     * @param scanLimit    how many cells away from a goal to actually process; negative to process whole map
     * @param impassable   a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start        the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets      a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ArrayList<Coord> findPath(int length, int scanLimit, Collection<Coord> impassable,
                                     Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        return findPath(null, length, scanLimit, impassable, onlyPassable, start, targets);
    }
    /**
     * Scans the dungeon using DijkstraMap.scan or DijkstraMap.partialScan with the listed goals and start
     * point, and returns a list of Coord positions (using the current measurement) needed to get closer
     * to the closest reachable goal. The maximum length of the returned list is given by length, which represents
     * movement in a system where a single move can be multiple cells if length is greater than 1 and should usually
     * be 1 in standard roguelikes; if moving the full length of the list would place the mover in a position shared
     * by one of the positions in onlyPassable (which is typically filled with friendly units that can be passed
     * through in multi-cell-movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * The full map will only be scanned if scanLimit is 0 or less; for positive scanLimit values this will scan only
     * that distance out from each goal, which can save processing time on maps where only a small part matters.
     * Generally, scanLimit should be significantly greater than length.
     * <br>
     * This overload takes a buffer parameter, an ArrayList of Coord, that the results will be appended to. If the
     * buffer is null, a new ArrayList will be made and appended to. This caches its result in a member field, path,
     * which can be fetched after finding a path and will change with each call to a pathfinding method. Any existing
     * contents of buffer will not affect the path field of this DijkstraMap.
     *
     * @param buffer       an existing ArrayList of Coord that will have the result appended to it (in-place); if null, this will make a new ArrayList
     * @param length       the length of the path to calculate
     * @param scanLimit    how many cells away from a goal to actually process; negative to process whole map
     * @param impassable   a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start        the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets      a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ArrayList<Coord> findPath(ArrayList<Coord> buffer, int length, int scanLimit, Collection<Coord> impassable,
                                     Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        path.clear();
        if (!initialized || length <= 0)
        {
            cutShort = true;
            if(buffer == null)
                return new ArrayList<>();
            else
            {
                return buffer;
            }
        }
        if (impassable == null)
            impassable2.clear();
        else
            impassable2.empty().addAll(impassable);
        if (onlyPassable != null && length == 1) 
            impassable2.addAll(onlyPassable);
        
        resetMap();
        setGoals(targets);
        if (goals.isEmpty())
        {
            cutShort = true;
            if(buffer == null)
                return new ArrayList<>();
            else
            {
                return buffer;
            }
        }
        if(scanLimit <= 0 || scanLimit < length)
            scan(start, impassable2);
        else
            partialScan(start, scanLimit, impassable2);
        Coord currentPos = start;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if(!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                if(buffer == null)
                    return new ArrayList<>(path);
                else
                {
                    buffer.addAll(path);
                    return buffer;
                }
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.add(currentPos);
            paidLength += costMap[currentPos.x][currentPos.y];
            frustration++;
            if (paidLength > length - 1.0) {
                if (onlyPassable != null && onlyPassable.contains(currentPos)) {
                    impassable2.add(currentPos);
                    return findPath(buffer, length, scanLimit, impassable2, onlyPassable, start, targets);
                }
                break;
            }
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        if(buffer == null)
            return new ArrayList<>(path);
        else
        {
            buffer.addAll(path);
            return buffer;
        }
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, until preferredRange is
     * reached, or further from a goal if the preferredRange has not been met at the current distance.
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param moveLength     the length of the path to calculate
     * @param preferredRange the distance this unit will try to keep from a target
     * @param los            a squidgrid.LOS object if the preferredRange should try to stay in line of sight, or null if LoS
     *                       should be disregarded.
     * @param impassable     a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable   a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start          the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets        a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ArrayList<Coord> findAttackPath(int moveLength, int preferredRange, LOS los, Collection<Coord> impassable,
                                           Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        return findAttackPath(moveLength, preferredRange, preferredRange, los, impassable, onlyPassable, start, targets);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, until a cell is reached with
     * a distance from a goal that is at least equal to minPreferredRange and no more than maxPreferredRange,
     * which may go further from a goal if the minPreferredRange has not been met at the current distance.
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param moveLength        the length of the path to calculate
     * @param minPreferredRange the (inclusive) lower bound of the distance this unit will try to keep from a target
     * @param maxPreferredRange the (inclusive) upper bound of the distance this unit will try to keep from a target
     * @param los               a squidgrid.LOS object if the preferredRange should try to stay in line of sight, or null if LoS
     *                          should be disregarded.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets           a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ArrayList<Coord> findAttackPath(int moveLength, int minPreferredRange, int maxPreferredRange, LOS los,
                                           Collection<Coord> impassable, Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        return findAttackPath(null, moveLength, minPreferredRange, maxPreferredRange, los, impassable, onlyPassable, start, targets);
    }
    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, until a cell is reached with
     * a distance from a goal that is at least equal to minPreferredRange and no more than maxPreferredRange,
     * which may go further from a goal if the minPreferredRange has not been met at the current distance.
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * <br>
     * This overload takes a buffer parameter, an ArrayList of Coord, that the results will be appended to. If the
     * buffer is null, a new ArrayList will be made and appended to. This caches its result in a member field, path,
     * which can be fetched after finding a path and will change with each call to a pathfinding method. Any existing
     * contents of buffer will not affect the path field of this DijkstraMap.
     *
     * @param buffer            an existing ArrayList of Coord that will have the result appended to it (in-place); if null, this will make a new ArrayList
     * @param moveLength        the length of the path to calculate
     * @param minPreferredRange the (inclusive) lower bound of the distance this unit will try to keep from a target
     * @param maxPreferredRange the (inclusive) upper bound of the distance this unit will try to keep from a target
     * @param los               a squidgrid.LOS object if the preferredRange should try to stay in line of sight, or null if LoS
     *                          should be disregarded.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets           a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ArrayList<Coord> findAttackPath(ArrayList<Coord> buffer, int moveLength, int minPreferredRange, int maxPreferredRange, LOS los,
                                           Collection<Coord> impassable, Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        if (!initialized || moveLength <= 0)
        {
            cutShort = true;
            if(buffer == null)
                return new ArrayList<>();
            else
            {
                return buffer;
            }
        }
        if (minPreferredRange < 0) minPreferredRange = 0;
        if (maxPreferredRange < minPreferredRange) maxPreferredRange = minPreferredRange;
        double[][] resMap = new double[width][height];
        if (los != null) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    resMap[x][y] = (physicalMap[x][y] == WALL) ? 1.0 : 0.0;
                }
            }
        }
        path.clear();
        if (impassable == null)
            impassable2.clear();
        else
            impassable2.empty().addAll(impassable);
        if (onlyPassable != null && moveLength == 1)
            impassable2.addAll(onlyPassable);

        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty())
        {
            cutShort = true;
            if(buffer == null)
                return new ArrayList<>();
            else
            {
                return buffer;
            }
        }

        Measurement mess = measurement;
        if (measurement == Measurement.EUCLIDEAN) {
            measurement = Measurement.CHEBYSHEV;
        }
        scan(impassable2);
        goals.clear();

        for (int x = 0; x < width; x++) {
            CELL:
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == WALL || gradientMap[x][y] == DARK)
                    continue;
                if (gradientMap[x][y] >= minPreferredRange && gradientMap[x][y] <= maxPreferredRange) {

                    for (Coord goal : targets) {
                        if (los == null || los.isReachable(resMap, x, y, goal.x, goal.y)) {
                            setGoal(x, y);
                            gradientMap[x][y] = 0;
                            continue CELL;
                        }
                    }
                    gradientMap[x][y] = FLOOR;
                } else
                    gradientMap[x][y] = FLOOR;
            }
        }
        measurement = mess;
        scan(impassable2);

        Coord currentPos = start;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if(!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best  && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                if(buffer == null)
                    return new ArrayList<>(path);
                else
                {
                    buffer.addAll(path);
                    return buffer;
                }
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.add(Coord.get(currentPos.x, currentPos.y));
            paidLength += costMap[currentPos.x][currentPos.y];
            frustration++;
            if (paidLength > moveLength - 1.0) {

                if (onlyPassable != null && onlyPassable.contains(currentPos)) {
                    impassable2.add(currentPos);
                    return findAttackPath(buffer, moveLength, minPreferredRange, maxPreferredRange, los, impassable2,
                            onlyPassable, start, targets);
                }
                break;
            }
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        if(buffer == null)
            return new ArrayList<>(path);
        else
        {
            buffer.addAll(path);
            return buffer;
        }
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, where goals are
     * considered valid if they are at a valid range for the given Technique to hit at least one target
     * and ideal if that Technique can affect as many targets as possible from a cell that can be moved
     * to with at most movelength steps.
     * <br>
     * The return value of this method is the path to get to a location to attack, but on its own it
     * does not tell the user how to perform the attack.  It does set the targetMap 2D Coord array field
     * so that if your position at the end of the returned path is non-null in targetMap, it will be
     * a Coord that can be used as a target position for Technique.apply() . If your position at the end
     * of the returned path is null, then an ideal attack position was not reachable by the path.
     * <br>
     * This needs a char[][] dungeon as an argument because DijkstraMap does not always have a char[][]
     * version of the map available to it, and certain AOE implementations that a Technique uses may
     * need a char[][] specifically to determine what they affect.
     * <br>
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in allies
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios, and is also used considered an undesirable thing to affect for the Technique),
     * it will recalculate a move so that it does not pass into that cell.
     * <br>
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a target overlapping one.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param moveLength the maximum distance to try to pathfind out to; if a spot to use a Technique can be found
     *                   while moving no more than this distance, then the targetMap field in this object will have a
     *                   target Coord that is ideal for the given Technique at the x, y indices corresponding to the
     *                   last Coord in the returned path.
     * @param tech       a Technique that we will try to find an ideal place to use, and/or a path toward that place.
     * @param dungeon    a char 2D array with '#' for walls.
     * @param los        a squidgrid.LOS object if the preferred range should try to stay in line of sight, or null if LoS
     *                   should be disregarded.
     * @param impassable locations of enemies or mobile hazards/obstacles that aren't in the map as walls
     * @param allies     called onlyPassable in other methods, here it also represents allies for Technique things
     * @param start      the Coord the pathfinder starts at.
     * @param targets    a Set of Coord, not an array of Coord or variable argument list as in other methods.
     * @return an ArrayList of Coord that represents a path to travel to get to an ideal place to use tech. Copy of path.
     */
    public ArrayList<Coord> findTechniquePath(int moveLength, Technique tech, char[][] dungeon, LOS los,
                                              Collection<Coord> impassable, Collection<Coord> allies, Coord start, Collection<Coord> targets) {
        return findTechniquePath(null, moveLength, tech, dungeon, los, impassable, allies, start, targets);
    }
    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, where goals are
     * considered valid if they are at a valid range for the given Technique to hit at least one target
     * and ideal if that Technique can affect as many targets as possible from a cell that can be moved
     * to with at most movelength steps.
     * <br>
     * The return value of this method is the path to get to a location to attack, but on its own it
     * does not tell the user how to perform the attack.  It does set the targetMap 2D Coord array field
     * so that if your position at the end of the returned path is non-null in targetMap, it will be
     * a Coord that can be used as a target position for Technique.apply() . If your position at the end
     * of the returned path is null, then an ideal attack position was not reachable by the path.
     * <br>
     * This needs a char[][] dungeon as an argument because DijkstraMap does not always have a char[][]
     * version of the map available to it, and certain AOE implementations that a Technique uses may
     * need a char[][] specifically to determine what they affect.
     * <br>
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in allies
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios, and is also used considered an undesirable thing to affect for the Technique),
     * it will recalculate a move so that it does not pass into that cell.
     * <br>
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a target overlapping one.
     * <br>
     * This overload takes a buffer parameter, an ArrayList of Coord, that the results will be appended to. If the
     * buffer is null, a new ArrayList will be made and appended to. This caches its result in a member field, path,
     * which can be fetched after finding a path and will change with each call to a pathfinding method. Any existing
     * contents of buffer will not affect the path field of this DijkstraMap.
     *
     * @param buffer     an existing ArrayList of Coord that will have the result appended to it (in-place); if null, this will make a new ArrayList
     * @param moveLength the maximum distance to try to pathfind out to; if a spot to use a Technique can be found
     *                   while moving no more than this distance, then the targetMap field in this object will have a
     *                   target Coord that is ideal for the given Technique at the x, y indices corresponding to the
     *                   last Coord in the returned path.
     * @param tech       a Technique that we will try to find an ideal place to use, and/or a path toward that place.
     * @param dungeon    a char 2D array with '#' for walls.
     * @param los        a squidgrid.LOS object if the preferred range should try to stay in line of sight, or null if LoS
     *                   should be disregarded.
     * @param impassable locations of enemies or mobile hazards/obstacles that aren't in the map as walls
     * @param allies     called onlyPassable in other methods, here it also represents allies for Technique things
     * @param start      the Coord the pathfinder starts at.
     * @param targets    a Set of Coord, not an array of Coord or variable argument list as in other methods.
     * @return an ArrayList of Coord that represents a path to travel to get to an ideal place to use tech. Copy of path.
     */
    public ArrayList<Coord> findTechniquePath(ArrayList<Coord> buffer, int moveLength, Technique tech, char[][] dungeon, LOS los,
                                              Collection<Coord> impassable, Collection<Coord> allies, Coord start, Collection<Coord> targets) {
        if (!initialized || moveLength <= 0)
        {
            cutShort = true;
            if(buffer == null)
                return new ArrayList<>();
            else
            {
                return buffer;
            }
        }
        tech.setMap(dungeon);
        double[][] resMap = new double[width][height];
        double[][] worthMap = new double[width][height];
        double[][] userDistanceMap;
        double paidLength = 0.0;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                resMap[x][y] = (physicalMap[x][y] == WALL) ? 1.0 : 0.0;
                targetMap[x][y] = null;
            }
        }

        path.clear();
        if (targets == null || targets.size() == 0)
        {
            cutShort = true;
            if(buffer == null)
                return new ArrayList<>();
            else
            {
                return buffer;
            }
        }
        if (impassable == null)
            impassable2.clear();
        else
            impassable2.empty().addAll(impassable);

        if (allies == null)
            friends.clear();
        else {
            friends.empty().insertSeveral(allies).remove(start);
        }

        resetMap();
        setGoal(start);
        userDistanceMap = scan(impassable2);
        clearGoals();
        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty())
        {
            cutShort = true;
            if(buffer == null)
                return new ArrayList<>();
            else
            {
                return buffer;
            }
        }

        Measurement mess = measurement;
        /*
        if(measurement == Measurement.EUCLIDEAN)
        {
            measurement = Measurement.CHEBYSHEV;
        }
        */
        scan(impassable2);
        clearGoals();

        Coord tempPt = Coord.get(0, 0);
        OrderedMap<Coord, ArrayList<Coord>> ideal;
        // generate an array of the single best location to attack when you are in a given cell.
        for (int x = 0; x < width; x++) {
            CELL:
            for (int y = 0; y < height; y++) {
                tempPt = Coord.get(x, y);
                if (gradientMap[x][y] == WALL || gradientMap[x][y] == DARK || userDistanceMap[x][y] > moveLength * 2.0)
                    continue;
                if (gradientMap[x][y] >= tech.aoe.getMinRange() && gradientMap[x][y] <= tech.aoe.getMaxRange()) {
                    for (Coord tgt : targets) {
                        if (los == null || los.isReachable(resMap, x, y, tgt.x, tgt.y)) {
                            ideal = tech.idealLocations(tempPt, targets, friends);
                            if (!ideal.isEmpty()) {
                                targetMap[x][y] = ideal.keyAt(0);
                                worthMap[x][y] = ideal.getAt(0).size();
                                setGoal(x, y);
                                gradientMap[x][y] = 0;
                            }
                            continue CELL;
                        }
                    }
                    gradientMap[x][y] = FLOOR;
                } else
                    gradientMap[x][y] = FLOOR;
            }
        }
        scan(impassable2);

        double currentDistance = gradientMap[start.x][start.y];
        if (currentDistance <= moveLength) {
            int[] g_arr = goals.toArray();

            goals.clear();
            setGoal(start);
            scan(impassable2);
            gradientMap[start.x][start.y] = moveLength;
            int decX, decY;
            double bestWorth = 0.0;
            for (int g, ig = 0; ig < g_arr.length; ig++) {
                g = g_arr[ig];
                decX = decodeX(g);
                decY = decodeY(g);
                if (gradientMap[decX][decY] <= moveLength && worthMap[decX][decY] > bestWorth) {
                    goals.clear();
                    goals.add(g);
                    bestWorth = worthMap[decX][decY];
                }
                else if (gradientMap[decX][decY] <= moveLength && bestWorth > 0 && worthMap[decX][decY] == bestWorth)
                {
                    goals.add(g);
                }
            }
            resetMap();
           /* for(Coord g : goals.keySet())
            {
                gradientMap[g.x][g.y] = 0.0 - worthMap[g.x][g.y];
            }*/
            scan(impassable2);

        }

        measurement = mess;

        Coord currentPos = Coord.get(start.x, start.y);
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if(!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }
            if (best >= gradientMap[currentPos.x][currentPos.y]) {
                if (friends.contains(currentPos)) {
                    impassable2.add(currentPos);
                    return findTechniquePath(buffer, moveLength, tech, dungeon, los, impassable2,
                            friends, start, targets);
                }
                break;
            }
            if (best > gradientMap[start.x][start.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                if(buffer == null)
                    return new ArrayList<>(path);
                else
                {
                    buffer.addAll(path);
                    return buffer;
                }
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.add(currentPos);
            paidLength += costMap[currentPos.x][currentPos.y];
            frustration++;
            if (paidLength > moveLength - 1.0) {
                if (friends.contains(currentPos)) {
                    impassable2.add(currentPos);
                    return findTechniquePath(buffer, moveLength, tech, dungeon, los, impassable2,
                            friends, start, targets);
                }
                break;
            }
//            if(gradientMap[currentPos.x][currentPos.y] == 0)
//                break;
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        if(buffer == null)
            return new ArrayList<>(path);
        else
        {
            buffer.addAll(path);
            return buffer;
        }
    }


    private double cachedLongerPaths = 1.2;
    private Collection<Coord> cachedImpassable = new OrderedSet<>();
    private Coord[] cachedFearSources;
    private double[][] cachedFleeMap;
    private int cachedSize = 1;

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed fearSources and start point, and returns a list
     * of Coord positions (using Manhattan distance) needed to get further from the closest fearSources, meant
     * for running away. The maximum length of the returned list is given by length; if moving the full
     * length of the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a fearSource overlapping one. The preferLongerPaths parameter
     * is meant to be tweaked and adjusted; higher values should make creatures prefer to escape out of
     * doorways instead of hiding in the closest corner, and a value of 1.2 should be typical for many maps.
     * The parameters preferLongerPaths, impassable, and the varargs used for fearSources will be cached, and
     * any subsequent calls that use the same values as the last values passed will avoid recalculating
     * unnecessary scans.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param length            the length of the path to calculate
     * @param preferLongerPaths Set this to 1.2 if you aren't sure; it will probably need tweaking for different maps.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param fearSources       a vararg or array of Coord positions to run away from
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes away from fear sources. Copy of path.
     */
    public ArrayList<Coord> findFleePath(int length, double preferLongerPaths, Collection<Coord> impassable,
                                         Collection<Coord> onlyPassable, Coord start, Coord... fearSources) {
        return findFleePath(null, length, -1, preferLongerPaths, impassable, onlyPassable, start, fearSources);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan or DijkstraMap.partialScan with the listed fearSources and start
     * point, and returns a list of Coord positions (using this DijkstraMap's metric) needed to get further from
     * the closest fearSources, meant for running away. The maximum length of the returned list is given by length,
     * which represents movement in a system where a single move can be multiple cells if length is greater than 1 and
     * should usually be 1 in standard roguelikes; if moving the full length of the list would place the mover in a
     * position shared by one of the positions in onlyPassable (which is typically filled with friendly units that can
     * be passed through in multi-cell-movement scenarios), it will recalculate a move so that it does not pass into
     * that cell. The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a fearSource overlapping one. The preferLongerPaths parameter
     * is meant to be tweaked and adjusted; higher values should make creatures prefer to escape out of
     * doorways instead of hiding in the closest corner, and a value of 1.2 should be typical for many maps.
     * The parameters preferLongerPaths, impassable, and the varargs used for fearSources will be cached, and
     * any subsequent calls that use the same values as the last values passed will avoid recalculating
     * unnecessary scans. However, scanLimit is not cached; if you use scanLimit then it is assumed you are using some
     * value for it that shouldn't change relative to the other parameters (like twice the length).
     * The full map will only be scanned if scanLimit is 0 or less; for positive scanLimit values this will scan only
     * that distance out from each goal, which can save processing time on maps where only a small part matters.
     * Generally, scanLimit should be significantly greater than length.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param length            the length of the path to calculate
     * @param scanLimit         how many steps away from a fear source to calculate; negative scans the whole map
     * @param preferLongerPaths Set this to 1.2 if you aren't sure; it will probably need tweaking for different maps.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param fearSources       a vararg or array of Coord positions to run away from
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes away from fear sources. Copy of path.
     */
    public ArrayList<Coord> findFleePath(int length, int scanLimit, double preferLongerPaths, Collection<Coord> impassable,
                                         Collection<Coord> onlyPassable, Coord start, Coord... fearSources) {
        return findFleePath(null, length, scanLimit, preferLongerPaths, impassable, onlyPassable, start, fearSources);
    }
    /**
     * Scans the dungeon using DijkstraMap.scan or DijkstraMap.partialScan with the listed fearSources and start
     * point, and returns a list of Coord positions (using this DijkstraMap's metric) needed to get further from
     * the closest fearSources, meant for running away. The maximum length of the returned list is given by length,
     * which represents movement in a system where a single move can be multiple cells if length is greater than 1 and
     * should usually be 1 in standard roguelikes; if moving the full length of the list would place the mover in a
     * position shared by one of the positions in onlyPassable (which is typically filled with friendly units that can
     * be passed through in multi-cell-movement scenarios), it will recalculate a move so that it does not pass into
     * that cell. The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a fearSource overlapping one. The preferLongerPaths parameter
     * is meant to be tweaked and adjusted; higher values should make creatures prefer to escape out of
     * doorways instead of hiding in the closest corner, and a value of 1.2 should be typical for many maps.
     * The parameters preferLongerPaths, impassable, and the varargs used for fearSources will be cached, and
     * any subsequent calls that use the same values as the last values passed will avoid recalculating
     * unnecessary scans. However, scanLimit is not cached; if you use scanLimit then it is assumed you are using some
     * value for it that shouldn't change relative to the other parameters (like twice the length).
     * The full map will only be scanned if scanLimit is 0 or less; for positive scanLimit values this will scan only
     * that distance out from each goal, which can save processing time on maps where only a small part matters.
     * Generally, scanLimit should be significantly greater than length.
     * <br>
     * This overload takes a buffer parameter, an ArrayList of Coord, that the results will be appended to. If the
     * buffer is null, a new ArrayList will be made and appended to. This caches its result in a member field, path,
     * which can be fetched after finding a path and will change with each call to a pathfinding method. Any existing
     * contents of buffer will not affect the path field of this DijkstraMap.
     *
     * @param buffer            an existing ArrayList of Coord that will have the result appended to it (in-place); if null, this will make a new ArrayList
     * @param length            the length of the path to calculate
     * @param scanLimit         how many steps away from a fear source to calculate; negative scans the whole map
     * @param preferLongerPaths Set this to 1.2 if you aren't sure; it will probably need tweaking for different maps.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param fearSources       a vararg or array of Coord positions to run away from
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes away from fear sources. Copy of path.
     */
    public ArrayList<Coord> findFleePath(ArrayList<Coord> buffer, int length, int scanLimit, double preferLongerPaths, Collection<Coord> impassable,
                                         Collection<Coord> onlyPassable, Coord start, Coord... fearSources) {
        if (!initialized || length <= 0)
        {
            cutShort = true;
            if(buffer == null)
                return new ArrayList<>();
            else
            {
                return buffer;
            }
        }
        path.clear();
        if (impassable == null)
            impassable2.clear();
        else
            impassable2.empty().addAll(impassable);

        if (fearSources == null || fearSources.length < 1) {
            cutShort = true;
            if(buffer == null)
                return new ArrayList<>();
            else
            {
                return buffer;
            }
        }
        if (cachedSize == 1 && preferLongerPaths == cachedLongerPaths && impassable2.equals(cachedImpassable) &&
                Arrays.equals(fearSources, cachedFearSources)) {
            gradientMap = cachedFleeMap;
        } else {
            cachedLongerPaths = preferLongerPaths;
            cachedImpassable = new OrderedSet<>(impassable2);
            cachedFearSources = new Coord[fearSources.length];
            System.arraycopy(fearSources, 0, cachedFearSources, 0, fearSources.length);
            cachedSize = 1;
            resetMap();
            setGoals(fearSources);
            if (goals.isEmpty())
            {
                cutShort = true;
                if(buffer == null)
                    return new ArrayList<>();
                else
                {
                    return buffer;
                }
            }

            if(length < 0) length = 0;
            if(scanLimit <= 0 || scanLimit < length)
                cachedFleeMap = scan(impassable2);
            else
                cachedFleeMap = partialScan(scanLimit, impassable2);


            for (int x = 0; x < gradientMap.length; x++) {
                for (int y = 0; y < gradientMap[x].length; y++) {
                    gradientMap[x][y] *= (gradientMap[x][y] >= FLOOR) ? 1.0 : -preferLongerPaths;
                }
            }

            if(scanLimit <= 0 || scanLimit < length)
                cachedFleeMap = scan(impassable2);
            else
                cachedFleeMap = partialScan(scanLimit, impassable2);
        }
        Coord currentPos = start;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if(!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }
            if (best >= gradientMap[start.x][start.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                if(buffer == null)
                    return new ArrayList<>(path);
                else
                {
                    buffer.addAll(path);
                    return buffer;
                }
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            if (path.size() > 0) {
                Coord last = path.get(path.size() - 1);
                if (gradientMap[last.x][last.y] <= gradientMap[currentPos.x][currentPos.y])
                    break;
            }
            path.add(currentPos);
            frustration++;
            paidLength += costMap[currentPos.x][currentPos.y];
            if (paidLength > length - 1.0) {
                if (onlyPassable != null && onlyPassable.contains(currentPos)) {
                    impassable2.add(currentPos);
                    return findFleePath(buffer, length, scanLimit, preferLongerPaths, impassable2, onlyPassable, start, fearSources);
                }
                break;
            }
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        if(buffer == null) 
            return new ArrayList<>(path);
        else
        {
            buffer.addAll(path);
            return buffer;
        }
    }

    /**
     * For pathfinding creatures larger than 1x1 cell; scans the dungeon using DijkstraMap.scan with the listed goals
     * and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to the closest reachable
     * goal. The maximum length of the returned list is given by length; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * The parameter size refers to the side length of a square unit, such as 2 for a 2x2 unit. The
     * parameter start must refer to the minimum-x, minimum-y cell of that unit if size is &gt; 1, and
     * all positions in the returned path will refer to movement of the minimum-x, minimum-y cell.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param size         the side length of the creature trying to find a path
     * @param length       the length of the path to calculate
     * @param impassable   a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start        the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets      a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the min-x, min-y locations of this creature as it goes toward a target. Copy of path.
     */

    public ArrayList<Coord> findPathLarge(final int size, int length, Collection<Coord> impassable,
                                          Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        return findPathLarge(size, length, -1, impassable, onlyPassable, start, targets);
    }
    public ArrayList<Coord> findPathLarge(final int size, int length, final int scanLimit, Collection<Coord> impassable,
                                          Collection<Coord> onlyPassable, Coord start, Coord... targets) {

        if (!initialized) return null;
        path.clear();
        Collection<Coord> impassable2;
        if (impassable == null)
            impassable2 = Collections.emptySet();
        else
            impassable2 = new GreasedRegion(width, height, impassable);

        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty())
        {
            cutShort = true;
            return new ArrayList<>(path);
        }

        if(length < 0)
            length = 0;
        if(scanLimit <= 0 || scanLimit < length)
            scan(start, impassable2, size);
        else
            partialScan(scanLimit, start, impassable2, size);

        Coord currentPos = start;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if(!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                return new ArrayList<>(path);
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);

            path.add(currentPos);
            paidLength += costMap[currentPos.x][currentPos.y];
            frustration++;
            if (paidLength > length - 1.0) {
                if (onlyPassable != null && onlyPassable.contains(currentPos)) {
                    impassable2.add(currentPos);
                    return findPathLarge(size, length, impassable2, onlyPassable, start, targets);
                }
                break;
            }
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        return new ArrayList<>(path);
    }

    /**
     * For pathfinding creatures larger than 1x1 cell; scans the dungeon using DijkstraMap.scan with the listed goals
     * and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, until preferredRange is
     * reached, or further from a goal if the preferredRange has not been met at the current distance.
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * The parameter size refers to the side length of a square unit, such as 2 for a 2x2 unit. The
     * parameter start must refer to the minimum-x, minimum-y cell of that unit if size is &gt; 1, and
     * all positions in the returned path will refer to movement of the minimum-x, minimum-y cell.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param size           the side length of the creature trying to find a path
     * @param moveLength     the length of the path to calculate
     * @param preferredRange the distance this unit will try to keep from a target
     * @param los            a squidgrid.LOS object if the preferredRange should try to stay in line of sight, or null if LoS
     *                       should be disregarded.
     * @param impassable     a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable   a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start          the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets        a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the min-x, min-y locations of this creature as it goes toward a target. Copy of path.
     */
    public ArrayList<Coord> findAttackPathLarge(int size, int moveLength, int preferredRange, LOS los, Collection<Coord> impassable,
                                                Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        if (!initialized) return null;
        if (preferredRange < 0) preferredRange = 0;
        double[][] resMap = new double[width][height];
        if (los != null) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    resMap[x][y] = (physicalMap[x][y] == WALL) ? 1.0 : 0.0;
                }
            }
        }
        path.clear();
        Collection<Coord> impassable2;
        if (impassable == null)
            impassable2 = Collections.emptySet();
        else
            impassable2 = new GreasedRegion(width, height, impassable);

        if (onlyPassable == null)
            onlyPassable = Collections.emptySet();

        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty())
        {
            cutShort = true;
            return new ArrayList<>(path);
        }

        Measurement mess = measurement;
        if (measurement == Measurement.EUCLIDEAN) {
            measurement = Measurement.CHEBYSHEV;
        }
        scan(impassable2, size);
        goals.clear();

        for (int x = 0; x < width; x++) {
            CELL:
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == WALL || gradientMap[x][y] == DARK)
                    continue;
                if (x + 2 < width && y + 2 < height && gradientMap[x][y] == preferredRange) {
                    for (Coord goal : targets) {
                        if (los == null
                                || los.isReachable(resMap, x, y, goal.x, goal.y)
                                || los.isReachable(resMap, x + 1, y, goal.x, goal.y)
                                || los.isReachable(resMap, x, y + 1, goal.x, goal.y)
                                || los.isReachable(resMap, x + 1, y + 1, goal.x, goal.y)) {
                            setGoal(x, y);
                            gradientMap[x][y] = 0;
                            continue CELL;
                        }
                    }
                    gradientMap[x][y] = FLOOR;
                } else
                    gradientMap[x][y] = FLOOR;
            }
        }
        measurement = mess;
        scan(impassable2, size);

        Coord currentPos = start;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if(!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                return new ArrayList<>(path);
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.add(currentPos);
            frustration++;
            paidLength += costMap[currentPos.x][currentPos.y];
            if (paidLength > moveLength - 1.0) {
                if (onlyPassable.contains(currentPos)) {
                    impassable2.add(currentPos);
                    return findAttackPathLarge(size, moveLength, preferredRange, los, impassable2, onlyPassable, start, targets);
                }
                break;
            }
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        return new ArrayList<>(path);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, until a cell is reached with
     * a distance from a goal that is at least equal to minPreferredRange and no more than maxPreferredRange,
     * which may go further from a goal if the minPreferredRange has not been met at the current distance.
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * The parameter size refers to the side length of a square unit, such as 2 for a 2x2 unit. The
     * parameter start must refer to the minimum-x, minimum-y cell of that unit if size is &gt; 1, and
     * all positions in the returned path will refer to movement of the minimum-x, minimum-y cell.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param size              the side length of the creature trying to find a path
     * @param moveLength        the length of the path to calculate
     * @param minPreferredRange the (inclusive) lower bound of the distance this unit will try to keep from a target
     * @param maxPreferredRange the (inclusive) upper bound of the distance this unit will try to keep from a target
     * @param los               a squidgrid.LOS object if the preferredRange should try to stay in line of sight, or null if LoS
     *                          should be disregarded.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets           a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the min-x, min-y locations of this creature as it goes toward a target. Copy of path.
     */
    public ArrayList<Coord> findAttackPathLarge(int size, int moveLength, int minPreferredRange, int maxPreferredRange, LOS los,
                                                Collection<Coord> impassable, Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        if (!initialized) return null;
        if (minPreferredRange < 0) minPreferredRange = 0;
        if (maxPreferredRange < minPreferredRange) maxPreferredRange = minPreferredRange;
        double[][] resMap = new double[width][height];
        if (los != null) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    resMap[x][y] = (physicalMap[x][y] == WALL) ? 1.0 : 0.0;
                }
            }
        }
        path.clear();
        Collection<Coord> impassable2;
        if (impassable == null)
            impassable2 = Collections.emptySet();
        else
            impassable2 = new GreasedRegion(width, height, impassable);

        if (onlyPassable == null)
            onlyPassable = Collections.emptySet();

        resetMap();
        for (Coord goal : targets) {
            setGoal(goal);
        }
        if (goals.isEmpty())
        {
            cutShort = true;
            return new ArrayList<>(path);
        }

        Measurement mess = measurement;
        if (measurement == Measurement.EUCLIDEAN) {
            measurement = Measurement.CHEBYSHEV;
        }
        scan(impassable2, size);
        goals.clear();

        for (int x = 0; x < width; x++) {
            CELL:
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == WALL || gradientMap[x][y] == DARK)
                    continue;
                if (x + 2 < width && y + 2 < height && gradientMap[x][y] >= minPreferredRange && gradientMap[x][y] <= maxPreferredRange) {
                    for (Coord goal : targets) {
                        if (los == null
                                || los.isReachable(resMap, x, y, goal.x, goal.y)
                                || los.isReachable(resMap, x + 1, y, goal.x, goal.y)
                                || los.isReachable(resMap, x, y + 1, goal.x, goal.y)
                                || los.isReachable(resMap, x + 1, y + 1, goal.x, goal.y)) {
                            setGoal(x, y);
                            gradientMap[x][y] = 0;
                            continue CELL;
                        }
                    }
                    gradientMap[x][y] = FLOOR;
                } else
                    gradientMap[x][y] = FLOOR;
            }
        }
        measurement = mess;
        scan(impassable2, size);

        Coord currentPos = start;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }

            double best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if(!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }
            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                return new ArrayList<>(path);
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);

            path.add(currentPos);
            frustration++;
            paidLength += costMap[currentPos.x][currentPos.y];
            if (paidLength > moveLength - 1.0) {
                if (onlyPassable.contains(currentPos)) {
                    impassable2.add(currentPos);
                    return findAttackPathLarge(size, moveLength, minPreferredRange, maxPreferredRange, los, impassable2,
                            onlyPassable, start, targets);
                }
                break;
            }
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        return new ArrayList<>(path);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed fearSources and start point, and returns a list
     * of Coord positions (using Manhattan distance) needed to get further from the closest fearSources, meant
     * for running away. The maximum length of the returned list is given by length; if moving the full
     * length of the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a fearSource overlapping one. The preferLongerPaths parameter
     * is meant to be tweaked and adjusted; higher values should make creatures prefer to escape out of
     * doorways instead of hiding in the closest corner, and a value of 1.2 should be typical for many maps.
     * The parameters size, preferLongerPaths, impassable, and the varargs used for fearSources will be cached, and
     * any subsequent calls that use the same values as the last values passed will avoid recalculating
     * unnecessary scans. Calls to findFleePath will cache as if size is 1, and may share a cache with this function.
     * The parameter size refers to the side length of a square unit, such as 2 for a 2x2 unit. The
     * parameter start must refer to the minimum-x, minimum-y cell of that unit if size is &gt; 1, and
     * all positions in the returned path will refer to movement of the minimum-x, minimum-y cell.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param size              the side length of the creature trying the find a path
     * @param length            the length of the path to calculate
     * @param preferLongerPaths Set this to 1.2 if you aren't sure; it will probably need tweaking for different maps.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param fearSources       a vararg or array of Coord positions to run away from
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes away from fear sources. Copy of path.
     */
    public ArrayList<Coord> findFleePathLarge(int size, int length, double preferLongerPaths, Collection<Coord> impassable,
                                              Collection<Coord> onlyPassable, Coord start, Coord... fearSources) {
        if (!initialized) return null;
        path.clear();
        Collection<Coord> impassable2;
        if (impassable == null)
            impassable2 = Collections.emptySet();
        else
            impassable2 = new GreasedRegion(width, height, impassable);

        if (onlyPassable == null)
            onlyPassable = Collections.emptySet();
        if (fearSources == null || fearSources.length < 1) {
            cutShort = true;
            path.clear();
            return new ArrayList<>(path);
        }
        if (size == cachedSize && preferLongerPaths == cachedLongerPaths && impassable2.equals(cachedImpassable)
                && Arrays.equals(fearSources, cachedFearSources)) {
            gradientMap = cachedFleeMap;
        } else {
            cachedLongerPaths = preferLongerPaths;
            cachedImpassable = new OrderedSet<>(impassable2);
            cachedFearSources = new Coord[fearSources.length];
            System.arraycopy(fearSources, 0, cachedFearSources, 0, fearSources.length);
            cachedSize = size;
            resetMap();
            setGoals(fearSources);
            if (goals.isEmpty())
            {
                cutShort = true;
                return new ArrayList<>(path);
            }

            scan(impassable2, size);

            for (int x = 0; x < gradientMap.length; x++) {
                for (int y = 0; y < gradientMap[x].length; y++) {
                    gradientMap[x][y] *= (gradientMap[x][y] >= FLOOR) ? 1.0 : (0.0 - preferLongerPaths);
                }
            }
            cachedFleeMap = scan(impassable2, size);
        }
        Coord currentPos = start;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }

            double best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if(!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }
            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                return new ArrayList<>(path);
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);

            if (path.size() > 0) {
                Coord last = path.get(path.size() - 1);
                if (gradientMap[last.x][last.y] <= gradientMap[currentPos.x][currentPos.y])
                    break;
            }
            path.add(currentPos);
            frustration++;
            paidLength += costMap[currentPos.x][currentPos.y];
            if (paidLength > length - 1.0) {
                if (onlyPassable.contains(currentPos)) {
                    impassable2.add(currentPos);
                    return findFleePathLarge(size, length, preferLongerPaths, impassable2, onlyPassable, start, fearSources);
                }
                break;
            }
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        return new ArrayList<>(path);
    }


    /**
     * When you can control how often the (relatively time-intensive) scan() method is called, but may need simple paths
     * very frequently (such as for a path that follows the mouse), you can use this method to reduce the amount of work
     * needed to find paths. Needs scan() or partialScan() to already be called and at least one goal to already be set,
     * and does not restrict the length of the path or behave as if the pathfinder has allies or enemies.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param target the target cell
     * @return an ArrayList of Coord that make up the best path. Copy of path.
     */
    public ArrayList<Coord> findPathPreScanned(Coord target) {
        return findPathPreScanned(null, target);
    }
    /**
     * When you can control how often the (relatively time-intensive) scan() method is called, but may need simple paths
     * very frequently (such as for a path that follows the mouse), you can use this method to reduce the amount of work
     * needed to find paths. Needs scan() or partialScan() to already be called and at least one goal to already be set,
     * and does not restrict the length of the path or behave as if the pathfinder has allies or enemies.
     * <br>
     * This overload takes a buffer parameter, an ArrayList of Coord, that the results will be appended to. If the
     * buffer is null, a new ArrayList will be made and appended to. This caches its result in a member field, path,
     * which can be fetched after finding a path and will change with each call to a pathfinding method. Any existing
     * contents of buffer will not affect the path field of this DijkstraMap.
     *
     * @param buffer an existing ArrayList of Coord that will have the result appended to it (in-place); if null, this will make a new ArrayList
     * @param target the target cell
     * @return an ArrayList of Coord that make up the best path, appended to buffer (if non-null)
     */
    public ArrayList<Coord> findPathPreScanned(ArrayList<Coord> buffer, Coord target) {
        path.clear();
        if (!initialized || goals == null || goals.isEmpty())
        {
            if(buffer == null)
                return new ArrayList<>();
            else
            {
                return buffer;
            }
        }
        Coord currentPos = target;
        if(gradientMap[currentPos.x][currentPos.y] <= FLOOR)
            path.add(currentPos);
        else
        {
            if(buffer == null)
                return new ArrayList<>();
            else
            {
                return buffer;
            }

        }
        while (true) {
            if (frustration > 2000) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if(!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                if(buffer == null)
                    return new ArrayList<>(path);
                else
                {
                    buffer.addAll(path);
                    return buffer;
                }
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.add(0, currentPos);
            frustration++;

            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        cutShort = false;
        frustration = 0;
        if(buffer == null)
            return new ArrayList<>(path);
        else
        {
            buffer.addAll(path);
            return buffer;
        }

    }

    /**
     * A simple limited flood-fill that returns a OrderedMap of Coord keys to the Double values in the DijkstraMap, only
     * calculating out to a number of steps determined by limit. This can be useful if you need many flood-fills and
     * don't need a large area for each, or if you want to have an effect spread to a certain number of cells away.
     *
     * @param radius the number of steps to take outward from each starting position.
     * @param starts a vararg group of Points to step outward from; this often will only need to be one Coord.
     * @return A OrderedMap of Coord keys to Double values; the starts are included in this with the value 0.0.
     */
    public Map<Coord, Double> floodFill(int radius, Coord... starts) {
        if (!initialized) return null;
        OrderedMap<Coord, Double> fill = new OrderedMap<>();

        resetMap();
        for (Coord goal : starts) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty())
            return fill;

        partialScan(radius, null);
        double temp;
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                temp = gradientMap[x][y];
                if (temp < FLOOR) {
                    fill.put(Coord.get(x, y), temp);
                }
            }
        }
        goals.clear();
        return fill;
    }

    public int getMappedCount() {
        return mappedCount;
    }

    /**
     * If you want obstacles present in orthogonal cells to prevent pathfinding along the diagonal between them, this
     * can be used to make thin diagonal walls non-viable to move through, or even to prevent diagonal movement if any
     * one obstacle is orthogonally adjacent to both the start and target cell of a diagonal move. If you haven't set
     * this yet, then the default is 2.
     * <br>
     * If this is 0, as a special case no orthogonal obstacles will block diagonal moves.
     * <br>
     * If this is 1, having one orthogonal obstacle adjacent to both the current cell and the cell the pathfinder is
     * trying to diagonally enter will block diagonal moves. This generally blocks movement around corners, the "hard
     * corner" rule used in some games.
     * <br>
     * If this is 2 (the default), having two orthogonal obstacles adjacent to both the current cell and the cell the
     * pathfinder is trying to diagonally enter will block diagonal moves. As an example, if there is a wall to the
     * north and a wall to the east, then the pathfinder won't be able to move northeast even if there is a floor there.
     * @return the current level of blocking required to stop a diagonal move
     */
    public int getBlockingRequirement() {
        return blockingRequirement;
    }

    /**
     * If you want obstacles present in orthogonal cells to prevent pathfinding along the diagonal between them, this
     * can be used to make thin diagonal walls non-viable to move through, or even to prevent diagonal movement if any
     * one obstacle is orthogonally adjacent to both the start and target cell of a diagonal move. If you haven't set
     * this yet, then the default is 2.
     * <br>
     * If this is 0, as a special case no orthogonal obstacles will block diagonal moves.
     * <br>
     * If this is 1, having one orthogonal obstacle adjacent to both the current cell and the cell the pathfinder is
     * trying to diagonally enter will block diagonal moves. This generally blocks movement around corners, the "hard
     * corner" rule used in some games.
     * <br>
     * If this is 2 (the default), having two orthogonal obstacles adjacent to both the current cell and the cell the
     * pathfinder is trying to diagonally enter will block diagonal moves. As an example, if there is a wall to the
     * north and a wall to the east, then the pathfinder won't be able to move northeast even if there is a floor there.
     * @param blockingRequirement the desired level of blocking required to stop a diagonal move
     */
    public void setBlockingRequirement(int blockingRequirement) {
        this.blockingRequirement = blockingRequirement > 2 ? 2 : blockingRequirement < 0 ? 0 : blockingRequirement;
    }

    private void appendDirToShuffle(IRNG rng) {
        final Direction[] src = measurement == Measurement.MANHATTAN
                ? Direction.CARDINALS : Direction.OUTWARDS;
        final int n = measurement.directionCount();
        System.arraycopy(src, 0, dirs, 0, n);
        for (int i = n - 1; i > 0; i--) {
            final int r = rng.nextInt(i+1);
            Direction t = dirs[r];
            dirs[r] = dirs[i];
            dirs[i] = t;
        }
        dirs[n] = Direction.NONE;
    }
}