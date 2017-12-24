package squidpony.performance.alternate;

import squidpony.GwtCompatibility;
import squidpony.annotation.GwtIncompatible;
import squidpony.squidai.Technique;
import squidpony.squidai.Threat;
import squidpony.squidgrid.*;
import squidpony.squidmath.*;

import java.io.Serializable;
import java.util.*;

/**
 * An alternative to AStarSearch when you want to fully explore a search space, or when you want a gradient floodfill.
 * It's currently significantly faster that AStarSearch, and also supports pathfinding to the nearest of multiple
 * goals, which is not possible with AStarSearch. This last feature enables a whole host of others, like pathfinding
 * for creatures that can attack targets between a specified minimum and maximum distance, and there's also the
 * standard uses of Dijkstra Maps such as finding ideal paths to run away.
 * As a bit of introduction, the article http://www.roguebasin.com/index.php?title=Dijkstra_Maps_Visualized can
 * provide some useful information on how these work and how to visualize the information they can produce, while
 * http://www.roguebasin.com/index.php?title=The_Incredible_Power_of_Dijkstra_Maps is an inspiring list of the
 * various features Dijkstra Maps can enable.
 * <br>
 * If you can't remember how to spell this, just remember: Does It Just Know Stuff? That's Really Awesome!
 * Created by Tommy Ettinger on 4/4/2015.
 */
public class OldDijkstraMap implements Serializable {
    private static final long serialVersionUID = -2456306898212944440L;

    private static final double root2 = Math.sqrt(2.0);

    /**
     * The type of heuristic to use.
     */
    public enum Measurement {

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
                case CHEBYSHEV:
                    return 1.0;
                case EUCLIDEAN:
                    switch (target) {
                        case DOWN_LEFT:
                        case DOWN_RIGHT:
                        case UP_LEFT:
                        case UP_RIGHT:
                            return root2;
                        default:
                            return 1.0;
                    }
            }
            return 1.0;
        }

        public int directionCount() {
            switch (this) {
                case MANHATTAN:
                    return 4;
                default:
                    return 8;
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
     * A 2D array of modifiers to apply to the perceived safety of an area; modifiers go up when deteriorate() is
     * called, which makes the cells specified in that method call more dangerous (usually because staying in one place
     * is perceived as risky).
     */
    public double[][] safetyMap;
    /**
     * This stores the entry cost multipliers for each cell; that is, a value of 1.0 is a normal, unmodified cell, but
     * a value of 0.5 can be entered easily (two cells of its cost can be entered for the cost of one 1.0 cell), and a
     * value of 2.0 can only be entered with difficulty (one cell of its cost can be entered for the cost of two 1.0
     * cells). Unlike the measurement field, this does affect the length of paths, as well as the numbers assigned
     * to gradientMap during a scan. The values for walls are identical to the value used by gradientMap, that is, this
     * class' WALL static final field. Floors, however, are never given FLOOR as a value, and default to 1.0 .
     */
    public double[][] costMap = null;
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
    protected IntDoubleOrderedMap goals;
    private IntDoubleOrderedMap fresh, closed, open;
    /**
     * The RNG used to decide which one of multiple equally-short paths to take.
     */
    public RNG rng;
    private int frustration = 0;
    public Coord[][] targetMap;

    private Direction[] reuse = new Direction[9];

    private boolean initialized = false;

    private int mappedCount = 0;

    private int blockingRequirement = 2;

    /**
     * Construct a DijkstraMap without a level to actually scan. If you use this constructor, you must call an
     * initialize() method before using this class.
     */
    public OldDijkstraMap() {
        rng = new RNG();
        path = new ArrayList<>();

        goals = new IntDoubleOrderedMap();
        fresh = new IntDoubleOrderedMap();
        closed = new IntDoubleOrderedMap();
        open = new IntDoubleOrderedMap();
    }

    /**
     * Construct a DijkstraMap without a level to actually scan. This constructor allows you to specify an RNG before
     * it is ever used in this class. If you use this constructor, you must call an initialize() method before using
     * any other methods in the class.
     */
    public OldDijkstraMap(RNG random) {
        rng = random;
        path = new ArrayList<>();

        goals = new IntDoubleOrderedMap();
        fresh = new IntDoubleOrderedMap();
        closed = new IntDoubleOrderedMap();
        open = new IntDoubleOrderedMap();
    }

    /**
     * Used to construct a DijkstraMap from the output of another.
     *
     * @param level
     */
    public OldDijkstraMap(final double[][] level) {
        this(level, Measurement.MANHATTAN);
    }

    /**
     * Used to construct a DijkstraMap from the output of another, specifying a distance calculation.
     *
     * @param level
     * @param measurement
     */
    public OldDijkstraMap(final double[][] level, Measurement measurement) {
        rng = new RNG();
        this.measurement = measurement;
        path = new ArrayList<>();

        goals = new IntDoubleOrderedMap();
        fresh = new IntDoubleOrderedMap();
        closed = new IntDoubleOrderedMap();
        open = new IntDoubleOrderedMap();
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
    public OldDijkstraMap(final char[][] level) {
        this(level, Measurement.MANHATTAN, new RNG());
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. Also takes an RNG that ensures predictable path choices given
     * otherwise identical inputs and circumstances.
     *
     * @param level
     * @param rng   The RNG to use for certain decisions; only affects find* methods like findPath, not scan.
     */
    public OldDijkstraMap(final char[][] level, RNG rng) {
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
    public OldDijkstraMap(final char[][] level, char alternateWall) {
        rng = new RNG();
        path = new ArrayList<>();

        goals = new IntDoubleOrderedMap();
        fresh = new IntDoubleOrderedMap();
        closed = new IntDoubleOrderedMap();
        open = new IntDoubleOrderedMap();
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
    public OldDijkstraMap(final char[][] level, Measurement measurement) {
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
    public OldDijkstraMap(final char[][] level, Measurement measurement, RNG rng) {
        this.rng = rng;
        path = new ArrayList<>();
        this.measurement = measurement;

        goals = new IntDoubleOrderedMap();
        fresh = new IntDoubleOrderedMap();
        closed = new IntDoubleOrderedMap();
        open = new IntDoubleOrderedMap();
        initialize(level);
    }

    /**
     * Used to initialize or re-initialize a DijkstraMap that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ).
     *
     * @param level
     * @return
     */
    public OldDijkstraMap initialize(final double[][] level) {
        width = level.length;
        height = level[0].length;
        gradientMap = new double[width][height];
        safetyMap = new double[width][height];
        physicalMap = new double[width][height];
        costMap = new double[width][height];
        targetMap = new Coord[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(level[x], 0, gradientMap[x], 0, height);
            System.arraycopy(level[x], 0, physicalMap[x], 0, height);
            Arrays.fill(costMap[x], 1.0);
        }
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a DijkstraMap that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ).
     *
     * @param level
     * @return
     */
    public OldDijkstraMap initialize(final char[][] level) {
        width = level.length;
        height = level[0].length;
        gradientMap = new double[width][height];
        safetyMap = new double[width][height];
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
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a DijkstraMap that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ). This
     * initialize() method allows you to specify an alternate wall char other than the default character, '#' .
     *
     * @param level
     * @param alternateWall
     * @return
     */
    public OldDijkstraMap initialize(final char[][] level, char alternateWall) {
        width = level.length;
        height = level[0].length;
        gradientMap = new double[width][height];
        safetyMap = new double[width][height];
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
    public OldDijkstraMap initializeCost(final char[][] level) {
        if (!initialized) throw new IllegalStateException("DijkstraMap must be initialized first!");
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                costMap[x][y] = (level[x][y] == '#') ? WALL : 1.0;
            }
        }
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
    public OldDijkstraMap initializeCost(final char[][] level, char alternateWall) {
        if (!initialized) throw new IllegalStateException("DijkstraMap must be initialized first!");
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                costMap[x][y] = (level[x][y] == alternateWall) ? WALL : 1.0;
            }
        }
        return this;
    }

    /**
     * Used to initialize the entry cost modifiers for games that require variable costs to enter squares. This expects
     * a double[][] of the same exact dimensions as the 2D array that was used to previously initialize() this
     * DijkstraMap, using the exact values given in costs as the values to enter cells, even if they aren't what this
     * class would assign normally -- walls and other impassable values should be given WALL as a value, however.
     * The costs can be accessed later by using costMap directly (which will have a valid value when this does not
     * throw an exception), or by calling setCost().
     * <p/>
     * This method should be slightly more efficient than the other initializeCost methods.
     *
     * @param costs a 2D double array that already has the desired cost values
     * @return this DijkstraMap for chaining.
     */
    public OldDijkstraMap initializeCost(final double[][] costs) {
        if (!initialized) throw new IllegalStateException("DijkstraMap must be initialized first!");
        costMap = new double[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(costs[x], 0, costMap[x], 0, height);
        }
        return this;
    }

    /**
     * Gets the appropriate DijkstraMap.Measurement to pass to a constructor if you already have a Radius.
     * Matches SQUARE or CUBE to CHEBYSHEV, DIAMOND or OCTAHEDRON to MANHATTAN, and CIRCLE or SPHERE to EUCLIDEAN.
     *
     * @param radius the Radius to find the corresponding Measurement for
     * @return a DijkstraMap.Measurement that matches radius; SQUARE to CHEBYSHEV, DIAMOND to MANHATTAN, etc.
     */
    public static Measurement findMeasurement(Radius radius) {
        if (radius.equals2D(Radius.SQUARE))
            return Measurement.CHEBYSHEV;
        else if (radius.equals2D(Radius.DIAMOND))
            return Measurement.MANHATTAN;
        else
            return Measurement.EUCLIDEAN;
    }

    /**
     * Gets the appropriate Radius corresponding to a DijkstraMap.Measurement.
     * Matches CHEBYSHEV to SQUARE, MANHATTAN to DIAMOND, and EUCLIDEAN to CIRCLE.
     *
     * @param measurement the Measurement to find the corresponding Radius for
     * @return a DijkstraMap.Measurement that matches radius; CHEBYSHEV to SQUARE, MANHATTAN to DIAMOND, etc.
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
            for (int y = 0; y < height; y++) {
                gradientMap[x][y] = physicalMap[x][y];
            }
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
     * Resets the targetMap (which is only assigned in the first place if you use findTechniquePath() ).
     */
    public void resetSafetyMap() {
        if (!initialized) return;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                safetyMap[x][y] = 0.0;
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
        closed.clear();
        fresh.clear();
        open.clear();
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

        goals.put(Coord.pureEncode(x, y), GOAL);
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

        goals.put(pt.encode(), GOAL);
    }

    /**
     * Marks many cells as goals for pathfinding, ignoring cells in walls or unreachable areas. More efficient than
     * a loop that calls {@link #setGoal(Coord)} over and over, since this doesn't need to do a bounds check. The
     * GreasedRegion passed to this should have the same width and height as this DijkstraMap.
     *
     * @param pts a GreasedRegion containing "on" cells to treat as goals; should have the same width and height as this
     */
    public void setGoals(GreasedRegion pts) {
        if (!initialized || pts.width > width || pts.height > height) return;
        int[] enc = new GreasedRegion(physicalMap, FLOOR).and(pts).asEncoded();
        double[] gls = new double[enc.length];
        Arrays.fill(gls, GOAL);
        goals.putAll(enc, gls);
    }

    /**
     * Marks many cells as goals for pathfinding, ignoring cells in walls or unreachable areas. Simply loops through
     * pts and calls {@link #setGoal(Coord)} on each Coord in pts.
     * If you have a GreasedRegion, you should use it with {@link #setGoals(GreasedRegion)}, which is faster.
     *
     * @param pts any Iterable of Coord, which can be a List, Set, Queue, etc. of Coords to mark as goals
     */
    public void setGoals(Iterable<Coord> pts) {
        if (!initialized) return;
        for (Coord c : pts) {
            setGoal(c);
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
            costMap[pt.x][pt.y] = WALL;
            return;
        }
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
            costMap[x][y] = WALL;
            return;
        }
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
        IntDoubleOrderedMap.KeyIterator ki = goals.keySet().iterator();
        while (ki.hasNext())
            resetCell(Coord.decode(ki.nextInt()));
        goals.clear();
    }

    protected void setFresh(int x, int y, double counter) {
        if (!initialized || x < 0 || x >= width || y < 0 || y >= height) return;
        gradientMap[x][y] = counter;
        fresh.put(Coord.pureEncode(x, y), counter);
    }

    protected void setFresh(final Coord pt, double counter) {
        if (!initialized || !pt.isWithin(width, height)) return;
        gradientMap[pt.x][pt.y] = counter;
        fresh.put(pt.encode(), counter);
    }

    /**
     * Used in conjunction with methods that depend on finding cover, like findCoveredAttackPath(), this method causes
     * specified risky points to be considered less safe, and will encourage a pathfinder to keep moving toward a goal
     * instead of just staying in cover forever (or until an enemy moves around the cover and ambushes the pathfinder).
     * Typically, you call deteriorate() with the current Coord position of the pathfinder and any Coords they stayed at
     * earlier along a path, and you do this once every turn or once every few turns, depending on how aggressively the
     * pathfinder should seek a goal.
     *
     * @param riskyPoints a List of Coord that should be considered more risky to stay at with each call.
     * @return the current safetyMap.
     */
    public double[][] deteriorate(List<Coord> riskyPoints) {
        return deteriorate(riskyPoints.toArray(new Coord[riskyPoints.size()]));
    }

    /**
     * Used in conjunction with methods that depend on finding cover, like findCoveredAttackPath(), this method causes
     * specified risky points to be considered less safe, and will encourage a pathfinder to keep moving toward a goal
     * instead of just staying in cover forever (or until an enemy moves around the cover and ambushes the pathfinder).
     * Typically, you call deteriorate() with the current Coord position of the pathfinder and any Coords they stayed at
     * earlier along a path, and you do this once every turn or once every few turns, depending on how aggressively the
     * pathfinder should seek a goal.
     *
     * @param riskyPoints a vararg or array of Coord that should be considered more risky to stay at with each call.
     * @return the current safetyMap.
     */
    public double[][] deteriorate(Coord... riskyPoints) {
        if (!initialized)
            return null;
        Coord c;
        for (int i = 0; i < riskyPoints.length; i++) {
            c = riskyPoints[i];
            if (c.isWithin(width, height))
                safetyMap[c.x][c.y] += 1.0;
        }
        return safetyMap;
    }

    /**
     * Used in conjunction with methods that depend on finding cover, like findCoveredAttackPath(), this method causes
     * specified safer points to be considered more safe, and will make a pathfinder more likely to enter those places
     * if they were considered dangerous earlier (due to calling deteriorate()).
     * <p/>
     * Typically, you call relax() with previous Coords a pathfinder stayed at that should be safer now than they were
     * at some previous point in time, and you might do this when no one has been attacked in a while or when the AI is
     * sure that a threat has been neutralized or no longer threatens a safer point.
     *
     * @param saferPoints a List of Coord that should be considered less risky to stay at with each call.
     * @return the current safetyMap.
     */
    public double[][] relax(List<Coord> saferPoints) {
        return relax(saferPoints.toArray(new Coord[saferPoints.size()]));
    }

    /**
     * Used in conjunction with methods that depend on finding cover, like findCoveredAttackPath(), this method causes
     * specified safer points to be considered more safe, and will make a pathfinder more likely to enter those places
     * if they were considered dangerous earlier (due to calling deteriorate()).
     * <p/>
     * Typically, you call relax() with previous Coords a pathfinder stayed at that should be safer now than they were
     * at some previous point in time, and you might do this when no one has been attacked in a while or when the AI is
     * sure that a threat has been neutralized or no longer threatens a safer point.
     *
     * @param saferPoints a vararg or array of Coord that should be considered less risky to stay at with each call.
     * @return the current safetyMap.
     */
    public double[][] relax(Coord... saferPoints) {
        if (!initialized)
            return null;
        Coord c;
        for (int i = 0; i < saferPoints.length; i++) {
            c = saferPoints[i];
            if (c.isWithin(width, height)) {
                safetyMap[c.x][c.y] -= 1.0;
                if (safetyMap[c.x][c.y] < 0.0)
                    safetyMap[c.x][c.y] = 0.0;
            }
        }
        return safetyMap;
    }

    /**
     * Recalculate the Dijkstra map and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.
     *
     * @param impassable A Set of Position keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D double[width][height] using the width and height of what this knows about the physical map.
     */
    public double[][] scan(Collection<Coord> impassable) {
        if (!initialized) return null;
        if (impassable != null && !impassable.isEmpty()) {
            for (Coord pt : impassable) {
                closed.put(pt.encode(), WALL);
            }
        }
        Coord dec, adj, cen;
        int enc;

        for (IntDoubleOrderedMap.MapEntry entry : goals.mapEntrySet()) {
            //if (closed.containsKey(entry.getIntKey()))
            //    continue;
            //    closed.remove(entry.getIntKey());
            dec = Coord.decode(entry.getIntKey());
            gradientMap[dec.x][dec.y] = entry.getDoubleValue();
        }
        double currentLowest = 999000;
        IntDoubleOrderedMap lowest = new IntDoubleOrderedMap();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (gradientMap[x][y] > FLOOR && !goals.containsKey(Coord.pureEncode(x, y)))
                    closed.put(Coord.pureEncode(x, y), physicalMap[x][y]);
                else if (gradientMap[x][y] < currentLowest) {
                    currentLowest = gradientMap[x][y];
                    lowest.clear();
                    lowest.put(Coord.pureEncode(x, y), currentLowest);
                } else if (gradientMap[x][y] == currentLowest) {
                    lowest.put(Coord.pureEncode(x, y), currentLowest);
                }
            }
        }
        int numAssigned = lowest.size();
        mappedCount = goals.size();
        open.putAll(lowest);
        Direction[] dirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;
        while (numAssigned > 0) {
//            ++iter;
            numAssigned = 0;

            for (IntDoubleOrderedMap.MapEntry cell : open.mapEntrySet()) {
                cen = Coord.decode(cell.getIntKey());
                for (int d = 0; d < dirs.length; d++) {
                    adj = cen.translate(dirs[d].deltaX, dirs[d].deltaY);
                    if (adj.x < 0 || adj.y < 0 || width <= adj.x || height <= adj.y)
                        /* Outside the map */
                        continue;
                    if (d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if ((gradientMap[adj.x][cen.y] > FLOOR ? 1 : 0)
                                + (gradientMap[cen.x][adj.y] > FLOOR ? 1 : 0)
                                >= blockingRequirement) {
                            continue;
                        }
                    }
                    enc = adj.encode();
                    double h = measurement.heuristic(dirs[d]);
                    if (!closed.containsKey(enc) && !open.containsKey(enc) && gradientMap[cen.x][cen.y] + h * costMap[adj.x][adj.y] < gradientMap[adj.x][adj.y]) {
                        setFresh(adj, cell.getDoubleValue() + h * costMap[adj.x][adj.y]);
                        ++numAssigned;
                        ++mappedCount;
                    }
                }
            }
//            closed.putAll(open);
            open.clear();
            open.putAll(fresh);
            fresh.clear();
        }
        closed.clear();
        open.clear();

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
     * current measurement.
     *
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param impassable A Set of Position keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D double[width][height] using the width and height of what this knows about the physical map.
     */
    public double[][] partialScan(int limit, Collection<Coord> impassable) {
        if (!initialized) return null;
        if (impassable != null && !impassable.isEmpty()) {
            for (Coord pt : impassable) {
                closed.put(pt.encode(), WALL);
            }
        }
        Coord dec, adj, cen;
        int enc;

        for (IntDoubleOrderedMap.MapEntry entry : goals.mapEntrySet()) {
            //if (closed.containsKey(entry.getIntKey()))
            //    closed.remove(entry.getIntKey());
            dec = Coord.decode(entry.getIntKey());
            gradientMap[dec.x][dec.y] = entry.getDoubleValue();
        }
        double currentLowest = 999000;
        IntDoubleOrderedMap lowest = new IntDoubleOrderedMap();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (gradientMap[x][y] > FLOOR && !goals.containsKey(Coord.pureEncode(x, y)))
                    closed.put(Coord.pureEncode(x, y), physicalMap[x][y]);
                else if (gradientMap[x][y] < currentLowest) {
                    currentLowest = gradientMap[x][y];
                    lowest.clear();
                    lowest.put(Coord.pureEncode(x, y), currentLowest);
                } else if (gradientMap[x][y] == currentLowest) {
                    lowest.put(Coord.pureEncode(x, y), currentLowest);
                }
            }
        }
        int numAssigned = lowest.size();
        mappedCount = goals.size();
        open.putAll(lowest);

        Direction[] dirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;
        int iter = 0;
        while (numAssigned > 0 && iter++ < limit) {
            numAssigned = 0;

            for (IntDoubleOrderedMap.MapEntry cell : open.mapEntrySet()) {
                cen = Coord.decode(cell.getIntKey());
                for (int d = 0; d < dirs.length; d++) {
                    adj = cen.translate(dirs[d].deltaX, dirs[d].deltaY);
                    if (adj.x < 0 || adj.y < 0 || width <= adj.x || height <= adj.y)
                    	/* Outside the map */
                        continue;
                    if (d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if ((gradientMap[cen.x + dirs[d].deltaX][cen.y] > FLOOR ? 1 : 0)
                                + (gradientMap[cen.x][cen.y + dirs[d].deltaY] > FLOOR ? 1 : 0)
                                >= blockingRequirement) {
                            continue;
                        }
                    }
                    enc = adj.encode();
                    double h = measurement.heuristic(dirs[d]);
                    if (!closed.containsKey(enc) && !open.containsKey(enc) &&
                            gradientMap[cen.x][cen.y] + h * costMap[adj.x][adj.y] < gradientMap[adj.x][adj.y]) {
                        setFresh(adj, cell.getDoubleValue() + h * costMap[adj.x][adj.y]);
                        ++numAssigned;
                        ++mappedCount;
                    }
                }
            }
//            closed.putAll(open);
            open = new IntDoubleOrderedMap(fresh);
            fresh.clear();
        }
        closed.clear();
        open.clear();


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
        int xShift = width / 8, yShift = height / 8;
        while (physicalMap[start.x][start.y] >= WALL && frustration < 50) {
            start2 = Coord.get(Math.min(Math.max(1, start.x + rng.nextInt(1 + xShift * 2) - xShift), width - 2),
                    Math.min(Math.max(1, start.y + rng.nextInt(1 + yShift * 2) - yShift), height - 2));
        }
        if (closed.containsKey(start2.encode()))
            closed.remove(start2.encode());
        gradientMap[start2.x][start2.y] = 0.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (gradientMap[x][y] > FLOOR && !goals.containsKey(Coord.pureEncode(x, y)))
                    closed.put(Coord.pureEncode(x, y), physicalMap[x][y]);
            }
        }
        int numAssigned = 1;
        mappedCount = 1;
        open.put(start2.encode(), 0.0);
        Coord dec, adj, cen;
        int enc;


        Direction[] dirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;
        while (numAssigned > 0) {
//            ++iter;
            numAssigned = 0;

            for (IntDoubleOrderedMap.MapEntry cell : open.mapEntrySet()) {
                cen = Coord.decode(cell.getIntKey());
                for (int d = 0; d < dirs.length; d++) {
                    adj = cen.translate(dirs[d].deltaX, dirs[d].deltaY);
                    if (adj.x < 0 || adj.y < 0 || width <= adj.x || height <= adj.y)
                    	/* Outside the map */
                        continue;
                    enc = adj.encode();
                    double h = measurement.heuristic(dirs[d]);
                    if (!closed.containsKey(enc) && !open.containsKey(enc) &&
                            gradientMap[cen.x][cen.y] + h * costMap[adj.x][adj.y] < gradientMap[adj.x][adj.y]) {
                        setFresh(adj, cell.getDoubleValue() + h * costMap[adj.x][adj.y]);
                        ++numAssigned;
                        ++mappedCount;
                        if (targets.contains(adj)) {
                            fresh.clear();
                            closed.clear();
                            open.clear();
                            return adj;
                        }
                    }
                }
            }
//            closed.putAll(open);
            open = new IntDoubleOrderedMap(fresh);
            fresh.clear();
        }
        closed.clear();
        open.clear();
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
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d < measurement.directionCount() + 1; d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
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
        Coord start2 = start, adj, cen;
        int enc;
        while (physicalMap[start.x][start.y] >= WALL && frustration < 50) {
            start2 = Coord.get(Math.min(Math.max(1, start.x + rng.nextInt(15) - 7), width - 2),
                    Math.min(Math.max(1, start.y + rng.nextInt(15) - 7), height - 2));
            frustration++;
        }
        if (closed.containsKey(start2.encode()))
            closed.remove(start2.encode());
        gradientMap[start2.x][start2.y] = 0.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (gradientMap[x][y] > FLOOR && !goals.containsKey(Coord.pureEncode(x, y)))
                    closed.put(Coord.pureEncode(x, y), physicalMap[x][y]);
            }
        }
        int numAssigned = 1;
        mappedCount = 1;
        open.put(start2.encode(), 0.0);

        Direction[] dirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;
        while (numAssigned > 0) {
//            ++iter;
            numAssigned = 0;
            for (IntDoubleOrderedMap.MapEntry cell : open.mapEntrySet()) {
                cen = Coord.decode(cell.getIntKey());
                for (int d = 0; d < dirs.length; d++) {
                    adj = cen.translate(dirs[d].deltaX, dirs[d].deltaY);
                    if (adj.x < 0 || adj.y < 0 || width <= adj.x || height <= adj.y)
                    	/* Outside the map */
                        continue;
                    enc = adj.encode();

                    double h = measurement.heuristic(dirs[d]);
                    if (!closed.containsKey(enc) && !open.containsKey(enc) &&
                            gradientMap[cen.x][cen.y] + h * costMap[adj.x][adj.y] < gradientMap[adj.x][adj.y]) {
                        setFresh(adj, cell.getDoubleValue() + h * costMap[adj.x][adj.y]);
                        ++numAssigned;
                        ++mappedCount;
                        if (targets.contains(adj)) {
                            found.add(adj);
                            if (found.size() >= limit) {
                                fresh.clear();
                                open.clear();
                                closed.clear();
                                return found;
                            }
                        }
                    }
                }
            }
//            closed.putAll(open);
            open = new IntDoubleOrderedMap(fresh);
            fresh.clear();
        }
        closed.clear();
        open.clear();
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
     * current measurement.
     *
     * @param impassable A Set of Position keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @return A 2D double[width][height] using the width and height of what this knows about the physical map.
     */
    public double[][] scan(Collection<Coord> impassable, int size) {
        if (!initialized) return null;
        if (impassable == null)
            impassable = new OrderedSet<>();
        IntDoubleOrderedMap blocking = new IntDoubleOrderedMap(impassable.size());
        for (Coord pt : impassable) {
            blocking.put(pt.encode(), WALL);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if (x + y != 0 && gradientMap[pt.x - x][pt.y - y] <= FLOOR)
                        blocking.put(Coord.pureEncode(pt.x - x, pt.y - y), DARK);
                }
            }
        }
        closed.putAll(blocking);
        Coord dec, cen, adj;
        int enc;
        for (IntDoubleOrderedMap.MapEntry entry : goals.mapEntrySet()) {
            //if (closed.containsKey(entry.getIntKey()))
            //    closed.remove(entry.getIntKey());
            dec = Coord.decode(entry.getIntKey());
            gradientMap[dec.x][dec.y] = entry.getDoubleValue();
        }
        mappedCount = goals.size();
        double currentLowest = 999000;
        IntDoubleOrderedMap lowest = new IntDoubleOrderedMap();
        int temp, p;
        for (int y = 0; y < height; y++) {
            I_AM_BECOME_DEATH_DESTROYER_OF_WORLDS:
            for (int x = 0; x < width; x++) {
                p = Coord.pureEncode(x, y);
                if (gradientMap[x][y] > FLOOR && !goals.containsKey(p)) {
                    closed.put(p, physicalMap[x][y]);
                    if (gradientMap[x][y] == WALL) {
                        for (int i = 0; i < size; i++) {
                            if (x - i < 0)
                                continue;
                            for (int j = 0; j < size; j++) {
                                temp = Coord.pureEncode(x - i, y - j);
                                if (y - j < 0 || closed.containsKey(temp))
                                    continue;
                                if (gradientMap[x - i][y - j] <= FLOOR && !goals.containsKey(temp))
                                    closed.put(temp, DARK);
                            }
                        }
                    }
                } else if (gradientMap[x][y] < currentLowest && !closed.containsKey(p)) {
                    for (int i = 0; i < size; i++) {
                        if (x + i >= width)
                            continue I_AM_BECOME_DEATH_DESTROYER_OF_WORLDS;
                        for (int j = 0; j < size; j++) {
                            temp = Coord.pureEncode(x + i, y + j);
                            if (y + j >= height || closed.containsKey(temp))
                                continue I_AM_BECOME_DEATH_DESTROYER_OF_WORLDS;
                        }
                    }

                    currentLowest = gradientMap[x][y];
                    lowest.clear();
                    lowest.put(Coord.pureEncode(x, y), currentLowest);

                } else if (gradientMap[x][y] == currentLowest && !closed.containsKey(p)) {
                    if (!closed.containsKey(p)) {
                        for (int i = 0; i < size; i++) {
                            if (x + i >= width)
                                continue I_AM_BECOME_DEATH_DESTROYER_OF_WORLDS;
                            for (int j = 0; j < size; j++) {
                                temp = Coord.pureEncode(x + i, y + j);
                                if (y + j >= height || closed.containsKey(temp))
                                    continue I_AM_BECOME_DEATH_DESTROYER_OF_WORLDS;
                            }
                        }
                        lowest.put(p, currentLowest);
                    }
                }
            }
        }
        int numAssigned = lowest.size();
        open.putAll(lowest);
        Direction[] dirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;
        while (numAssigned > 0) {
            numAssigned = 0;
            for (IntDoubleOrderedMap.MapEntry cell : open.mapEntrySet()) {
                cen = Coord.decode(cell.getIntKey());
                for (int d = 0; d < dirs.length; d++) {
                    adj = cen.translate(dirs[d].deltaX, dirs[d].deltaY);
                    if (adj.x < 0 || adj.y < 0 || width <= adj.x || height <= adj.y)
                    	/* Outside the map */
                        continue;
                    enc = adj.encode();
                    double h = measurement.heuristic(dirs[d]);
                    if (!closed.containsKey(enc) && !open.containsKey(enc) && gradientMap[cen.x][cen.y] + h * costMap[adj.x][adj.y] < gradientMap[adj.x][adj.y]) {
                        setFresh(adj, cell.getDoubleValue() + h * costMap[adj.x][adj.y]);
                        ++numAssigned;
                        ++mappedCount;
                    }
                }
            }
//            closed.putAll(open);
            open = new IntDoubleOrderedMap(fresh);
            fresh.clear();
        }
        closed.clear();
        open.clear();


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
     *
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
        if (!initialized) return null;
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
        if (goals.isEmpty()) {
            cutShort = true;
            return new ArrayList<>(path);
        }
        if (length < 0)
            length = 0;
        if (scanLimit <= 0 || scanLimit < length)
            scan(impassable2);
        else
            partialScan(scanLimit, impassable2);
        Coord currentPos = start;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
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
                if (onlyPassable.contains(currentPos)) {
                    closed.put(currentPos.encode(), WALL);
                    impassable2.add(currentPos);
                    return findPath(length, scanLimit, impassable2, onlyPassable, start, targets);
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
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty()) {
            cutShort = true;
            return new ArrayList<>(path);
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
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
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
            path.add(Coord.get(currentPos.x, currentPos.y));
            paidLength += costMap[currentPos.x][currentPos.y];
            frustration++;
            if (paidLength > moveLength - 1.0) {

                if (onlyPassable.contains(currentPos)) {

                    closed.put(currentPos.encode(), WALL);
                    impassable2.add(currentPos);
                    return findAttackPath(moveLength, minPreferredRange, maxPreferredRange, los, impassable2,
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
        if (!initialized) return null;
        tech.setMap(dungeon);
        double[][] resMap = new double[width][height];
        double[][] worthMap = new double[width][height];
        double[][] userDistanceMap;
        double paidLength = 0.0;

        OrderedSet<Coord> friends;


        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                resMap[x][y] = (physicalMap[x][y] == WALL) ? 1.0 : 0.0;
                targetMap[x][y] = null;
            }
        }

        path.clear();
        if (targets == null || targets.size() == 0) {
            cutShort = true;
            return new ArrayList<>(path);
        }
        Collection<Coord> impassable2;
        if (impassable == null)
            impassable2 = Collections.emptySet();
        else
            impassable2 = new GreasedRegion(width, height, impassable);

        if (allies == null)
            friends = new OrderedSet<>();
        else {
            friends = new OrderedSet<>(allies);
            friends.remove(start);
        }

        resetMap();
        setGoal(start);
        userDistanceMap = scan(impassable2);
        clearGoals();
        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty()) {
            cutShort = true;
            return new ArrayList<>(path);
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
            int[] g_arr = goals.keySet().toIntArray();

            goals.clear();
            setGoal(start);
            scan(impassable2);
            goals.clear();
            gradientMap[start.x][start.y] = moveLength;
            Coord dec;
            for (int g, ig = 0; ig < g_arr.length; ig++) {
                g = g_arr[ig];
                dec = Coord.decode(g);
                if (gradientMap[dec.x][dec.y] <= moveLength && worthMap[dec.x][dec.y] > 0) {
                    goals.put(g, 0.0 - worthMap[dec.x][dec.y]);
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
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
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
                    closed.put(currentPos.encode(), WALL);
                    impassable2.add(currentPos);
                    return findTechniquePath(moveLength, tech, dungeon, los, impassable2,
                            friends, start, targets);
                }
                break;
            }
            if (best > gradientMap[start.x][start.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                return new ArrayList<>(path);
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.add(currentPos);
            paidLength += costMap[currentPos.x][currentPos.y];
            frustration++;
            if (paidLength > moveLength - 1.0) {
                if (friends.contains(currentPos)) {
                    closed.put(currentPos.encode(), WALL);
                    impassable2.add(currentPos);
                    return findTechniquePath(moveLength, tech, dungeon, los, impassable2,
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
        return new ArrayList<>(path);
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
     * @param cache          a FOVCache that has completed its calculations, and will be used for LOS work, may be null
     * @param impassable     a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable   a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start          the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets        a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    @GwtIncompatible
    public ArrayList<Coord> findAttackPath(int moveLength, int preferredRange, FOVCache cache, Collection<Coord> impassable,
                                           Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        return findAttackPath(moveLength, preferredRange, preferredRange, cache, impassable, onlyPassable, start, targets);
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
     * @param cache             a FOVCache that has completed its calculations, and will be used for LOS work, may be null
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets           a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    @GwtIncompatible
    public ArrayList<Coord> findAttackPath(int moveLength, int minPreferredRange, int maxPreferredRange, FOVCache cache,
                                           Collection<Coord> impassable, Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        if (!initialized) return null;
        if (minPreferredRange < 0) minPreferredRange = 0;
        if (maxPreferredRange < minPreferredRange) maxPreferredRange = minPreferredRange;

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
        if (goals.isEmpty()) {
            cutShort = true;
            return new ArrayList<>(path);
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
                        if (cache == null || cache.queryLOS(x, y, goal.x, goal.y)) {
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
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
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
            path.add(Coord.get(currentPos.x, currentPos.y));
            paidLength += costMap[currentPos.x][currentPos.y];
            frustration++;
            if (paidLength > moveLength - 1.0) {

                if (onlyPassable.contains(currentPos)) {

                    closed.put(currentPos.encode(), WALL);
                    impassable2.add(currentPos);
                    return findAttackPath(moveLength, minPreferredRange, maxPreferredRange, cache, impassable2,
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
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list of Coord
     * positions (using the current measurement) needed to get closer to a goal while staying in areas that none of the
     * given threats are able to see (which should prevent them from attacking), until a cell is reached with
     * a distance from a goal that is at least equal to minPreferredRange and no more than maxPreferredRange,
     * which may go further from a goal if the minPreferredRange has not been met at the current distance.
     * <p/>
     * Essentially, this method is for finding ways to approach enemies who can attack at range without constantly being
     * attacked by them. You are expected to call deteriorate() and possible relax() at points when a position becomes
     * riskier to stay at (then you call deteriorate()) or a position starts to seem like a safer place (then, relax()).
     * <p/>
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
     * @param coverPreference   positive, typically around 1.0, higher numbers make the pathfinder stay behind cover
     *                          more, lower numbers make the pathfinder move more aggressively toward targets
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param threats           a List of Threat objects that store a position, min and max threatening distance
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets           a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ArrayList<Coord> findCoveredAttackPath(int moveLength, int minPreferredRange, int maxPreferredRange,
                                                  double coverPreference, Collection<Coord> impassable,
                                                  Collection<Coord> onlyPassable, Iterable<Threat> threats, Coord start,
                                                  Coord... targets) {
        if (!initialized) return null;

        if (minPreferredRange < 0) minPreferredRange = 0;
        if (maxPreferredRange < minPreferredRange) maxPreferredRange = minPreferredRange;
        double[][] resMap = new double[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                resMap[x][y] = (physicalMap[x][y] == WALL) ? 1.0 : 0.0;
            }
        }


        path = new ArrayList<Coord>();
        Collection<Coord> impassable2;
        if (impassable == null)
            impassable2 = new OrderedSet<Coord>();
        else
            impassable2 = new OrderedSet<Coord>(impassable);
        if (onlyPassable == null)
            onlyPassable = new OrderedSet<Coord>();

        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty()) {
            cutShort = true;
            return new ArrayList<>(path);
        }

        Measurement mess = measurement;
        if (measurement == Measurement.EUCLIDEAN) {
            measurement = Measurement.CHEBYSHEV;
        }
        scan(impassable2);
        goals.clear();
        IntDoubleOrderedMap cachedGoals = new IntDoubleOrderedMap();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == WALL || gradientMap[x][y] == DARK)
                    continue;
                if (gradientMap[x][y] >= minPreferredRange && gradientMap[x][y] <= maxPreferredRange) {
                    gradientMap[x][y] = 0.001 * (maxPreferredRange - gradientMap[x][y]);
                    cachedGoals.put(Coord.pureEncode(x, y), gradientMap[x][y]);
                } else
                    gradientMap[x][y] = FLOOR;
            }
        }
        measurement = mess;
        double[][] storedScan = scan(impassable2);
        if (storedScan[start.x][start.y] > moveLength) {
            clearGoals();
            resetMap();
            double[][] seen;
            short[] packed = CoordPacker.ALL_WALL, floors = CoordPacker.pack(physicalMap, FLOOR), tempPacked;
            for (Threat t : threats) {
                packed = CoordPacker.unionPacked(
                        packed, CoordPacker.reachable(floors, CoordPacker.packOne(t.position), t.reach));

            }
            short[] unseen = CoordPacker.differencePacked(CoordPacker.rectangle(width, height),
                    CoordPacker.expand(packed, 1, width, height));
            Coord[] safe = CoordPacker.allPacked(unseen);
            for (int i = 0; i < safe.length; i++) {
                setGoal(safe[i]);
            }
            safetyMap = scan(impassable2);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (storedScan[x][y] < FLOOR) {
                        gradientMap[x][y] = storedScan[x][y] * 2.0 * (moveLength + 1) + safetyMap[x][y] * coverPreference;
                    }
                    //safeMap[x][y] = Math.pow(safeMap[x][y] + safetyMap[x][y], 1.5);
                }
            }
            goals = cachedGoals;
            scan(impassable2);

            //gradientMap = storedScan;
        }
        Coord currentPos = start;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path = new ArrayList<Coord>();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] ||
                    physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                break;
            }
            currentPos = currentPos.translate(dirs[choice]);
            path.add(Coord.get(currentPos.x, currentPos.y));
            paidLength += costMap[currentPos.x][currentPos.y];
            frustration++;
            if (paidLength > moveLength - 1.0)
                break;
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        goals.clear();
        if (onlyPassable.contains(currentPos) || impassable2.contains(currentPos)) {
            closed.put(currentPos.encode(), WALL);
            impassable2.add(currentPos);
            return findCoveredAttackPath(moveLength, minPreferredRange, maxPreferredRange, coverPreference,
                    impassable2, onlyPassable, threats, start, targets);
        }
        cutShort = false;
        frustration = 0;
        return new ArrayList<>(path);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list of Coord
     * positions (using the current measurement) needed to get closer to a goal while staying in areas that none of the
     * given threats are able to see (which should prevent them from attacking), until a cell is reached with
     * a distance from a goal that is at equal to preferredRange,
     * which may go further from a goal if the preferredRange has not been met at the current distance.
     * <p/>
     * Essentially, this method is for finding ways to approach enemies who can attack at range without constantly being
     * attacked by them. You are expected to call deteriorate() and possible relax() at points when a position becomes
     * riskier to stay at (then you call deteriorate()) or a position starts to seem like a safer place (then, relax()).
     * <p/>
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
     * @param moveLength       the length of the path to calculate
     * @param preferredRange   the distance this unit will try to keep from a target
     * @param fov              a FOV that will be used for LOS work, must not be null
     * @param seekDistantGoals true if this should pathfind to goals that it cannot see, false if FOV restricts pathfinding
     * @param impassable       a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable     a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param threats          a List of Threat objects that store a position, min and max threatening distance
     * @param start            the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets          a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ArrayList<Coord> findCoveredAttackPath(int moveLength, int preferredRange, double coverPreference,
                                                  FOV fov, boolean seekDistantGoals, Collection<Coord> impassable,
                                                  Collection<Coord> onlyPassable, List<Threat> threats, Coord start,
                                                  Coord... targets) {
        return findCoveredAttackPath(moveLength, preferredRange, preferredRange, coverPreference, fov,
                seekDistantGoals, impassable, onlyPassable, threats, start, targets);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list of Coord
     * positions (using the current measurement) needed to get closer to a goal while staying in areas that none of the
     * given threats are able to see (which should prevent them from attacking), until a cell is reached with
     * a distance from a goal that is at least equal to minPreferredRange and no more than maxPreferredRange,
     * which may go further from a goal if the minPreferredRange has not been met at the current distance.
     * <p/>
     * Essentially, this method is for finding ways to approach enemies who can attack at range without constantly being
     * attacked by them. You are expected to call deteriorate() and possible relax() at points when a position becomes
     * riskier to stay at (then you call deteriorate()) or a position starts to seem like a safer place (then, relax()).
     * <p/>
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
     * @param coverPreference   positive, typically around 1.0, higher numbers make the pathfinder stay behind cover
     *                          more, lower numbers make the pathfinder move more aggressively toward targets
     * @param fov               a FOV that will be used for LOS work, MUST NOT be null
     * @param seekDistantGoals  true if this should pathfind to goals that it cannot see, false if FOV restricts pathfinding
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param threats           a List of Threat objects that store a position, min and max threatening distance
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets           a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ArrayList<Coord> findCoveredAttackPath(int moveLength, int minPreferredRange, int maxPreferredRange,
                                                  double coverPreference, FOV fov, boolean seekDistantGoals, Collection<Coord> impassable,
                                                  Collection<Coord> onlyPassable, Iterable<Threat> threats, Coord start,
                                                  Coord... targets) {
        if (!initialized) return null;
        if (fov == null) {
            return findCoveredAttackPath(moveLength, minPreferredRange, maxPreferredRange, coverPreference,
                    impassable, onlyPassable, threats, start, targets);
        }
        if (minPreferredRange < 0) minPreferredRange = 0;
        if (maxPreferredRange < minPreferredRange) maxPreferredRange = minPreferredRange;
        double[][] resMap = new double[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                resMap[x][y] = (physicalMap[x][y] == WALL) ? 1.0 : 0.0;
            }
        }


        path = new ArrayList<Coord>();
        Collection<Coord> impassable2;
        if (impassable == null)
            impassable2 = new OrderedSet<Coord>();
        else
            impassable2 = new OrderedSet<Coord>(impassable);
        if (onlyPassable == null)
            onlyPassable = new OrderedSet<Coord>();

        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty()) {
            cutShort = true;
            return new ArrayList<>(path);
        }

        Measurement mess = measurement;
        if (measurement == Measurement.EUCLIDEAN) {
            measurement = Measurement.CHEBYSHEV;
        }
        scan(impassable2);
        goals.clear();
        IntDoubleOrderedMap cachedGoals = new IntDoubleOrderedMap();

        for (int x = 0; x < width; x++) {
            CELL:
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == WALL || gradientMap[x][y] == DARK)
                    continue;
                if (gradientMap[x][y] >= minPreferredRange && gradientMap[x][y] <= maxPreferredRange) {

                    double[][] results = new double[width][height];
                    if (!seekDistantGoals)
                        results = fov.calculateFOV(resMap, x, y, maxPreferredRange, findRadius(mess));
                    for (Coord goal : targets) {
                        if (seekDistantGoals || results[goal.x][goal.y] > 0.0) {
                            gradientMap[x][y] = 0.001 * (maxPreferredRange - gradientMap[x][y]);
                            cachedGoals.put(Coord.pureEncode(x, y), gradientMap[x][y]);
                            continue CELL;
                        }
                    }
                    gradientMap[x][y] = FLOOR;
                } else
                    gradientMap[x][y] = FLOOR;
            }
        }
        measurement = mess;
        double[][] storedScan = scan(impassable2);
        if (storedScan[start.x][start.y] > moveLength) {
            clearGoals();
            resetMap();
            double[][] seen;
            short[] packed = CoordPacker.ALL_WALL, tempPacked;
            for (Threat t : threats) {
                seen = fov.calculateFOV(resMap, t.position.x, t.position.y, t.reach.maxDistance, findRadius(measurement));
                tempPacked = CoordPacker.pack(seen);

                if (t.reach.minDistance > 0) {

                    seen = fov.calculateFOV(resMap, t.position.x, t.position.y, t.reach.minDistance, findRadius(measurement));
                    tempPacked = CoordPacker.differencePacked(tempPacked, CoordPacker.pack(seen));

                }
                packed = CoordPacker.unionPacked(packed, tempPacked);
            }
            short[] unseen = CoordPacker.differencePacked(CoordPacker.rectangle(width, height),
                    CoordPacker.expand(packed, 1, width, height));
            Coord[] safe = CoordPacker.allPacked(unseen);
            for (int i = 0; i < safe.length; i++) {
                setGoal(safe[i]);
            }
            safetyMap = scan(impassable2);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (storedScan[x][y] < FLOOR) {
                        gradientMap[x][y] = storedScan[x][y] * 2.0 * (moveLength + 1) + safetyMap[x][y] * coverPreference;
                    }
                    //safeMap[x][y] = Math.pow(safeMap[x][y] + safetyMap[x][y], 1.5);
                }
            }
            goals = cachedGoals;
            scan(impassable2);

            //gradientMap = storedScan;
        }
        Coord currentPos = start;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path = new ArrayList<Coord>();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] ||
                    physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                break;
            }
            currentPos = currentPos.translate(dirs[choice]);
            path.add(Coord.get(currentPos.x, currentPos.y));
            paidLength += costMap[currentPos.x][currentPos.y];
            frustration++;
            if (paidLength > moveLength - 1.0)
                break;
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        goals.clear();
        if (onlyPassable.contains(currentPos) || impassable2.contains(currentPos)) {
            closed.put(currentPos.encode(), WALL);
            impassable2.add(currentPos);
            return findCoveredAttackPath(moveLength, minPreferredRange, maxPreferredRange, coverPreference,
                    fov, seekDistantGoals, impassable2, onlyPassable, threats, start, targets);
        }
        cutShort = false;
        frustration = 0;
        return new ArrayList<>(path);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list of Coord
     * positions (using the current measurement) needed to get closer to a goal while staying in areas that none of the
     * given threats are able to see (which should prevent them from attacking), until a cell is reached with
     * a distance from a goal that is at equal to preferredRange,
     * which may go further from a goal if the preferredRange has not been met at the current distance.
     * <p/>
     * Essentially, this method is for finding ways to approach enemies who can attack at range without constantly being
     * attacked by them. You are expected to call deteriorate() and possible relax() at points when a position becomes
     * riskier to stay at (then you call deteriorate()) or a position starts to seem like a safer place (then, relax()).
     * <p/>
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
     * @param moveLength       the length of the path to calculate
     * @param preferredRange   the distance this unit will try to keep from a target
     * @param fov              a FOVCache that has completed its calculations, and will be used for LOS work, may be null
     * @param seekDistantGoals true if this should pathfind to goals that it cannot see, false if FOV restricts pathfinding
     * @param impassable       a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable     a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param threats          a List of Threat objects that store a position, min and max threatening distance
     * @param start            the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets          a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    @GwtIncompatible
    public ArrayList<Coord> findCoveredAttackPath(int moveLength, int preferredRange, double coverPreference,
                                                  FOVCache fov, boolean seekDistantGoals, Collection<Coord> impassable,
                                                  Collection<Coord> onlyPassable, List<Threat> threats, Coord start,
                                                  Coord... targets) {
        return findCoveredAttackPath(moveLength, preferredRange, preferredRange, coverPreference, fov,
                seekDistantGoals, impassable, onlyPassable, threats, start, targets);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list of Coord
     * positions (using the current measurement) needed to get closer to a goal while staying in areas that none of the
     * given threats are able to see (which should prevent them from attacking), until a cell is reached with
     * a distance from a goal that is at least equal to minPreferredRange and no more than maxPreferredRange,
     * which may go further from a goal if the minPreferredRange has not been met at the current distance.
     * <p/>
     * Essentially, this method is for finding ways to approach enemies who can attack at range without constantly being
     * attacked by them. You are expected to call deteriorate() and possible relax() at points when a position becomes
     * riskier to stay at (then you call deteriorate()) or a position starts to seem like a safer place (then, relax()).
     * <p/>
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
     * @param coverPreference   positive, typically around 1.0, higher numbers make the pathfinder stay behind cover
     *                          more, lower numbers make the pathfinder move more aggressively toward targets
     * @param fov               a FOVCache that has completed its calculations, and will be used for LOS work
     * @param seekDistantGoals  true if this should pathfind to goals that it cannot see, false if FOV restricts pathfinding
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param threats           a List of Threat objects that store a position, min and max threatening distance
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets           a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    @GwtIncompatible
    public ArrayList<Coord> findCoveredAttackPath(int moveLength, int minPreferredRange, int maxPreferredRange,
                                                  double coverPreference, FOVCache fov, boolean seekDistantGoals, Collection<Coord> impassable,
                                                  Collection<Coord> onlyPassable, List<Threat> threats, Coord start,
                                                  Coord... targets) {
        if (!initialized) return null;
        if (fov == null) {
            return findCoveredAttackPath(moveLength, minPreferredRange, maxPreferredRange, coverPreference,
                    impassable, onlyPassable, threats, start, targets);
        }

        if (minPreferredRange < 0) minPreferredRange = 0;
        if (maxPreferredRange < minPreferredRange) maxPreferredRange = minPreferredRange;
        double[][] resMap = new double[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                resMap[x][y] = (physicalMap[x][y] == WALL) ? 1.0 : 0.0;
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
        if (goals.isEmpty()) {
            cutShort = true;
            return new ArrayList<>(path);
        }

        Measurement mess = measurement;
        if (measurement == Measurement.EUCLIDEAN) {
            measurement = Measurement.CHEBYSHEV;
        }
        scan(impassable2);
        goals.clear();
        IntDoubleOrderedMap cachedGoals = new IntDoubleOrderedMap();

        for (int x = 0; x < width; x++) {
            CELL:
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == WALL || gradientMap[x][y] == DARK)
                    continue;
                if (gradientMap[x][y] >= minPreferredRange && gradientMap[x][y] <= maxPreferredRange) {

                    double[][] results = new double[width][height];
                    if (!seekDistantGoals)
                        results = fov.calculateFOV(resMap, x, y, maxPreferredRange, findRadius(mess));
                    for (Coord goal : targets) {
                        if (seekDistantGoals || results[goal.x][goal.y] > 0.0) {
                            gradientMap[x][y] = 0.001 * (maxPreferredRange - gradientMap[x][y]);
                            cachedGoals.put(Coord.pureEncode(x, y), gradientMap[x][y]);
                            continue CELL;
                        }
                    }
                    gradientMap[x][y] = FLOOR;
                } else
                    gradientMap[x][y] = FLOOR;
            }
        }
        measurement = mess;
        double[][] storedScan = scan(impassable2);
        if (storedScan[start.x][start.y] > moveLength) {
            clearGoals();
            resetMap();
            double[][] seen;
            short[] packed = CoordPacker.ALL_WALL, tempPacked;
            for (Threat t : threats) {

                tempPacked = fov.getCacheEntry(t.position.x, t.position.y, t.reach.maxDistance);


                if (t.reach.minDistance > 0) {
                    tempPacked = CoordPacker.differencePacked(tempPacked,
                            fov.getCacheEntry(t.position.x, t.position.y, t.reach.minDistance));
                }
                packed = CoordPacker.unionPacked(packed, tempPacked);
            }
            short[] unseen = CoordPacker.differencePacked(CoordPacker.rectangle(width, height),
                    CoordPacker.expand(packed, 1, width, height));
            Coord[] safe = CoordPacker.allPacked(unseen);
            for (int i = 0; i < safe.length; i++) {
                setGoal(safe[i]);
            }
            safetyMap = scan(impassable2);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (storedScan[x][y] < FLOOR) {
                        gradientMap[x][y] = storedScan[x][y] * 2.0 * (moveLength + 1) + safetyMap[x][y] * coverPreference;
                    }
                    //safeMap[x][y] = Math.pow(safeMap[x][y] + safetyMap[x][y], 1.5);
                }
            }
            goals = cachedGoals;
            scan(impassable2);

            //gradientMap = storedScan;
        }
        Coord currentPos = start;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] ||
                    physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                break;
            }
            currentPos = currentPos.translate(dirs[choice]);
            path.add(Coord.get(currentPos.x, currentPos.y));
            paidLength += costMap[currentPos.x][currentPos.y];
            frustration++;
            if (paidLength > moveLength - 1.0)
                break;
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        goals.clear();
        if (onlyPassable.contains(currentPos) || impassable2.contains(currentPos)) {
            closed.put(currentPos.encode(), WALL);
            impassable2.add(currentPos);
            return findCoveredAttackPath(moveLength, minPreferredRange, maxPreferredRange, coverPreference,
                    fov, seekDistantGoals, impassable2, onlyPassable, threats, start, targets);
        }
        cutShort = false;
        frustration = 0;
        return new ArrayList<>(path);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, where goals are
     * considered valid if they are at a valid range for the given Technique to hit at least one target
     * and ideal if that Technique can affect as many targets as possible from a cell that can be moved
     * to with at most movelength steps.
     * <p/>
     * The return value of this method is the path to get to a location to attack, but on its own it
     * does not tell the user how to perform the attack.  It does set the targetMap 2D Coord array field
     * so that if your position at the end of the returned path is non-null in targetMap, it will be
     * a Coord that can be used as a target position for Technique.apply() . If your position at the end
     * of the returned path is null, then an ideal attack position was not reachable by the path.
     * <p/>
     * This needs a char[][] dungeon as an argument because DijkstraMap does not always have a char[][]
     * version of the map available to it, and certain AOE implementations that a Technique uses may
     * need a char[][] specifically to determine what they affect.
     * <p/>
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in allies
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios, and is also used considered an undesirable thing to affect for the Technique),
     * it will recalculate a move so that it does not pass into that cell.
     * <p/>
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
     * @param cache      a FOVCache that has completed its calculations, and will be used for LOS and Technique work, may be null
     * @param impassable locations of enemies or mobile hazards/obstacles that aren't in the map as walls
     * @param allies     called onlyPassable in other methods, here it also represents allies for Technique things
     * @param start      the Coord the pathfinder starts at.
     * @param targets    a Set (or any Collection) of Coord, not an array of Coord or variable argument list as in other methods.
     * @return an ArrayList of Coord that represents a path to travel to get to an ideal place to use tech. Copy of path.
     */
    @GwtIncompatible
    public ArrayList<Coord> findTechniquePath(int moveLength, Technique tech, char[][] dungeon, FOVCache cache,
                                              Collection<Coord> impassable, Collection<Coord> allies, Coord start, Collection<Coord> targets) {
        if (!initialized) return null;
        tech.setMap(dungeon);
        if (cache != null)
            tech.aoe.setCache(cache);
        double[][] resMap = new double[width][height];
        double[][] worthMap = new double[width][height];
        double[][] userDistanceMap;
        double paidLength = 0.0;

        Collection<Coord> friends;


        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                resMap[x][y] = (physicalMap[x][y] == WALL) ? 1.0 : 0.0;
                targetMap[x][y] = null;
            }
        }

        path.clear();
        if (targets == null || targets.size() == 0) {
            cutShort = true;
            return new ArrayList<>(path);
        }
        Collection<Coord> impassable2;
        if (impassable == null)
            impassable2 = Collections.emptySet();
        else
            impassable2 = new GreasedRegion(width, height, impassable);

        if (allies == null)
            friends = Collections.emptySet();
        else {
            friends = new GreasedRegion(width, height, allies);
            friends.remove(start);
        }

        resetMap();
        setGoal(start);
        userDistanceMap = scan(impassable2);
        clearGoals();
        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty()) {
            cutShort = true;
            return new ArrayList<>(path);
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
                        if (cache == null || cache.queryLOS(x, y, tgt.x, tgt.y)) {
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
            int[] g_arr = goals.keySet().toIntArray();
            Coord dec;

            goals.clear();
            setGoal(start);
            scan(impassable2);
            goals.clear();
            gradientMap[start.x][start.y] = moveLength;

            for (int g : g_arr) {
                dec = Coord.decode(g);
                if (gradientMap[dec.x][dec.y] <= moveLength && worthMap[dec.x][dec.y] > 0) {
                    goals.put(g, 0.0 - worthMap[dec.x][dec.y]);
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
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
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
                    closed.put(currentPos.encode(), WALL);
                    impassable2.add(currentPos);
                    return findTechniquePath(moveLength, tech, dungeon, cache, impassable2,
                            friends, start, targets);
                }
                break;
            }
            if (best > gradientMap[start.x][start.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                return new ArrayList<>(path);
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.add(currentPos);
            paidLength += costMap[currentPos.x][currentPos.y];
            frustration++;
            if (paidLength > moveLength - 1.0) {
                if (friends.contains(currentPos)) {
                    closed.put(currentPos.encode(), WALL);
                    impassable2.add(currentPos);
                    return findTechniquePath(moveLength, tech, dungeon, cache, impassable2,
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
        return new ArrayList<>(path);
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
        return findFleePath(length, -1, preferLongerPaths, impassable, onlyPassable, start, fearSources);
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
        if (cachedSize == 1 && preferLongerPaths == cachedLongerPaths && impassable2.equals(cachedImpassable) &&
                Arrays.equals(fearSources, cachedFearSources)) {
            gradientMap = cachedFleeMap;
        } else {
            cachedLongerPaths = preferLongerPaths;
            cachedImpassable = new OrderedSet<>(impassable2);
            cachedFearSources = GwtCompatibility.cloneCoords(fearSources);
            cachedSize = 1;
            resetMap();
            for (Coord goal : fearSources) {
                setGoal(goal.x, goal.y);
            }
            if (goals.isEmpty()) {
                cutShort = true;
                return new ArrayList<>(path);
            }

            if (length < 0) length = 0;
            if (scanLimit <= 0 || scanLimit < length)
                cachedFleeMap = scan(impassable2);
            else
                cachedFleeMap = partialScan(scanLimit, impassable2);


            for (int x = 0; x < gradientMap.length; x++) {
                for (int y = 0; y < gradientMap[x].length; y++) {
                    gradientMap[x][y] *= (gradientMap[x][y] >= FLOOR) ? 1.0 : -preferLongerPaths;
                }
            }

            if (scanLimit <= 0 || scanLimit < length)
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
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
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

                    closed.put(currentPos.encode(), WALL);
                    impassable2.add(currentPos);
                    return findFleePath(length, scanLimit, preferLongerPaths, impassable2, onlyPassable, start, fearSources);
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
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
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

    public ArrayList<Coord> findPathLarge(int size, int length, Collection<Coord> impassable,
                                          Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        if (!initialized) return null;
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
        if (goals.isEmpty()) {
            cutShort = true;
            return new ArrayList<>(path);
        }

        scan(impassable2, size);
        Coord currentPos = start;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
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
                if (onlyPassable.contains(currentPos)) {

                    closed.put(currentPos.encode(), WALL);
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
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
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
        if (goals.isEmpty()) {
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
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
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

                    closed.put(currentPos.encode(), WALL);
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
        if (goals.isEmpty()) {
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
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
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

                    closed.put(currentPos.encode(), WALL);
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
            cachedFearSources = GwtCompatibility.cloneCoords(fearSources);
            cachedSize = size;
            resetMap();
            for (Coord goal : fearSources) {
                setGoal(goal.x, goal.y);
            }
            if (goals.isEmpty()) {
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
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
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

                    closed.put(currentPos.encode(), WALL);
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
     * Intended primarily for internal use. Needs scan() to already be called and at least one goal to already be set,
     * and does not restrict the length of the path or behave as if the pathfinder has allies or enemies.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param target the target cell
     * @return an ArrayList of Coord that make up the best path. Copy of path.
     */
    public ArrayList<Coord> findPathPreScanned(Coord target) {
        path.clear();
        if (!initialized || goals == null || goals.isEmpty()) return path;
        Coord currentPos = target;
        if (gradientMap[currentPos.x][currentPos.y] <= FLOOR)
            path.add(currentPos);
        else
            return path;
        RNG rng2 = new StatefulRNG(0xf00d);
        while (true) {
            if (frustration > 2000) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
            final Direction[] dirs = appendDirToShuffle(rng2);
            int choice = rng2.nextInt(measurement.directionCount() + 1);

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
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
            path.add(0, currentPos);
            frustration++;

            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        cutShort = false;
        frustration = 0;
        return new ArrayList<>(path);
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
     * one obstacle is orthogonally adjacent to both the start and target cell of a diagonal move.
     * <br>
     * If this is 0, as a special case no orthogonal obstacles will block diagonal moves.
     * <br>
     * If this is 1, having one orthogonal obstacle adjacent to both the current cell and the cell the pathfinder is
     * trying to diagonally enter will block diagonal moves. This generally blocks movement around corners, the "hard
     * corner" rule used in some games.
     * <br>
     * If this is 2, having two orthogonal obstacles adjacent to both the current cell and the cell the pathfinder is
     * trying to diagonally enter will block diagonal moves. As an example, if there is a wall to the north and a wall
     * to the east, then the pathfinder won't be able to move northeast even if there is a floor there.
     *
     * @return the current level of blocking required to stop a diagonal move
     */
    public int getBlockingRequirement() {
        return blockingRequirement;
    }

    /**
     * If you want obstacles present in orthogonal cells to prevent pathfinding along the diagonal between them, this
     * can be used to make thin diagonal walls non-viable to move through, or even to prevent diagonal movement if any
     * one obstacle is orthogonally adjacent to both the start and target cell of a diagonal move.
     * <br>
     * If this is 0, as a special case no orthogonal obstacles will block diagonal moves.
     * <br>
     * If this is 1, having one orthogonal obstacle adjacent to both the current cell and the cell the pathfinder is
     * trying to diagonally enter will block diagonal moves. This generally blocks movement around corners, the "hard
     * corner" rule used in some games.
     * <br>
     * If this is 2, having two orthogonal obstacles adjacent to both the current cell and the cell the pathfinder is
     * trying to diagonally enter will block diagonal moves. As an example, if there is a wall to the north and a wall
     * to the east, then the pathfinder won't be able to move northeast even if there is a floor there.
     *
     * @param blockingRequirement the desired level of blocking required to stop a diagonal move
     */
    public void setBlockingRequirement(int blockingRequirement) {
        this.blockingRequirement = blockingRequirement > 2 ? 2 : blockingRequirement < 0 ? 0 : blockingRequirement;
    }

    /* For Gwt compatibility */
    private Direction[] shuffleDirs(RNG rng) {
        final Direction[] src = measurement == Measurement.MANHATTAN
                ? Direction.CARDINALS : Direction.OUTWARDS;
        return rng.randomPortion(src, reuse);
    }

    /* For Gwt compatibility */
    private Direction[] appendDirToShuffle(RNG rng) {
        //appendDir(shuffleDirs(rng),  Direction.NONE)
        shuffleDirs(rng);
        reuse[measurement.directionCount()] = Direction.NONE;
        return reuse;
    }
}