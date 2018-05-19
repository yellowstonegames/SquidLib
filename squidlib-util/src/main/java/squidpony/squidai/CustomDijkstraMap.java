package squidpony.squidai;

import squidpony.annotation.Beta;
import squidpony.squidai.DijkstraMap.Measurement;
import squidpony.squidgrid.Adjacency;
import squidpony.squidgrid.Adjacency.BasicAdjacency;
import squidpony.squidgrid.Adjacency.RotationAdjacency;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.*;

import java.io.Serializable;

/**
 * An alternative to AStarSearch when you want to fully explore a search space, or when you want a gradient
 * floodfill, with customizable rules for what is considered adjacent. This can be used for games where
 * rotation matters (and possibly costs movement), for games with thin walls (where a wall between cells
 * prevents travel between those two cells even if the wall doesn't occupy a walkable cell), for games where
 * the edges between cells may have some requisite to travel across, like a vertical amount that must be
 * hopped up or down between cells, and for games that have portals between distant cells on the same map.
 * <br>
 * As a bit of introduction, the article http://www.roguebasin.com/index.php?title=Dijkstra_Maps_Visualized can
 * provide some useful information on how these work and how to visualize the information they can produce, while
 * http://www.roguebasin.com/index.php?title=The_Incredible_Power_of_Dijkstra_Maps is an inspiring list of the
 * various features Dijkstra Maps can enable.
 * <br>
 * If you can't remember how to spell this, just remember: Does It Just Know Stuff? That's Really Awesome!
 * Created by Tommy Ettinger on 4/4/2015.
 */
@Beta
public class CustomDijkstraMap implements Serializable {
    private static final long serialVersionUID = -2456306898212944440L;

    /**
     * The main factor in determining the "Custom" behavior of CustomDijkstraMap; using an Adjacency implementation like
     * {@link BasicAdjacency} should cause this class to mimic {@link DijkstraMap}, but using
     * {@link RotationAdjacency} will be very different.
     */
    public Adjacency adjacency;

    /**
     * Stores which parts of the map are accessible and which are not. Should not be changed unless the actual physical
     * terrain has changed. You should call initialize() with a new map instead of changing this directly.
     */
    public double[] physicalMap;
    /**
     * The frequently-changing values that are often the point of using this class; goals will have a value of 0, and
     * any cells that can have a character reach a goal in n steps will have a value of n. Cells that cannot be
     * entered because they are solid will have a very high value equal to the WALL constant in this class, and cells
     * that cannot be entered because they cannot reach a goal will have a different very high value equal to the
     * DARK constant in this class.
     */
    public double[] gradientMap;
    /**
     * This stores the type of each cell for the purposes of determining its cost to enter; in most cases this type is
     * the char used to represent the cell, but any int can be used if you need more information. An int from costMap
     * is looked up in {@link Adjacency#costRules} to get the actual cost as a double; this collection should almost
     * always start with a reasonable default value for when the int key is not present. It's common to simply assign
     * a char like '#' or '.' to an element in costMap.
     */
    public int[] costMap = null;

    /**
     * The neighbors map, as produced by adjacency; can be modified by passing neighbors as the first argument to
     * {@link Adjacency#portal(int[][][], int, int, boolean)} if you want to create portals between non-adjacent cells.
     */
    public int[][][] neighbors;
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
    public IntVLA path;
    /**
     * Goals are always marked with 0.
     */
    public static final double GOAL = 0;
    /**
     * Floor cells, which include any walkable cell, are marked with a high number equal to 999200 .
     */
    public static final double FLOOR = 999200;
    /**
     * Walls, which are solid no-entry cells, are marked with a high number equal to 999500 .
     */
    public static final double WALL = 999500;
    /**
     * This is used to mark cells that the scan couldn't reach, and these dark cells are marked with a high number
     * equal to 999800 .
     */
    public static final double DARK = 999800;

    protected IntVLA goals = new IntVLA(256), fresh = new IntVLA(256);

    /**
     * The RNG used to decide which one of multiple equally-short paths to take.
     */
    public IRNG rng;
    private int frustration = 0;

    private int[] reuse = new int[9];

    private boolean initialized = false;

    private int mappedCount = 0;
    private double[] heuristics;

    /**
     * Construct a CustomDijkstraMap without a level to actually scan. If you use this constructor, you must call an
     * initialize() method before using this class.
     */
    public CustomDijkstraMap() {
        rng = new RNG();
        path = new IntVLA();
    }

    /**
     * Construct a CustomDijkstraMap without a level to actually scan. This constructor allows you to specify an RNG before
     * it is ever used in this class. If you use this constructor, you must call an initialize() method before using
     * any other methods in the class.
     */
    public CustomDijkstraMap(IRNG random) {
        rng = random;
        path = new IntVLA();
    }

    /**
     * Used to construct a CustomDijkstraMap from the output of another.
     *
     * @param level
     */
    public CustomDijkstraMap(final double[] level, int width, int height) {
        this(level, new BasicAdjacency(width, height, Measurement.MANHATTAN));
    }

    /**
     * Used to construct a CustomDijkstraMap from the output of another, specifying a distance calculation.
     *
     * @param level
     * @param adjacency
     */
    public CustomDijkstraMap(final double[] level, Adjacency adjacency) {
        rng = new RNG();
        this.adjacency = adjacency;
        path = new IntVLA();

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
    public CustomDijkstraMap(final char[][] level) {
        this(level, new BasicAdjacency(level.length, level[0].length, Measurement.MANHATTAN), new RNG());
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
    public CustomDijkstraMap(final char[][] level, IRNG rng) {
        this(level, new BasicAdjacency(level.length, level[0].length, Measurement.MANHATTAN), rng);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(), or any other
     * char[][] where one char means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. You can specify the character used for walls.
     *
     * @param level
     */
    public CustomDijkstraMap(final char[][] level, char alternateWall) {
        rng = new RNG();
        path = new IntVLA();
        adjacency = new BasicAdjacency(level.length, level[0].length, Measurement.MANHATTAN);

        initialize(level, alternateWall);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. This constructor specifies a distance measurement.
     *
     * @param level
     * @param adjacency
     */
    public CustomDijkstraMap(final char[][] level, Adjacency adjacency) {
        this(level, adjacency, new RNG());
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. Also takes a distance measurement and an IRNG that can be seeded
     * to ensure predictable path choices given otherwise identical inputs and circumstances.
     *
     * @param level
     * @param rng   The IRNG, such as an RNG, to use for certain decisions; only affects find* methods like findPath, not scan.
     */
    public CustomDijkstraMap(final char[][] level, Adjacency adjacency, IRNG rng) {
        this.rng = rng;
        path = new IntVLA();
        this.adjacency = adjacency;

        initialize(level);
    }

    /**
     * Used to initialize or re-initialize a CustomDijkstraMap that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ).
     *
     * @param level
     * @return
     */
    public CustomDijkstraMap initialize(final double[] level) {
        width = adjacency.width;
        height = adjacency.height;
        int len = level.length;
        gradientMap = new double[len];
        physicalMap = new double[len];
        costMap = new int[len];
        System.arraycopy(level, 0, gradientMap, 0, len);
        System.arraycopy(level, 0, physicalMap, 0, len);
        for (int i = 0; i < len; i++) {
            costMap[i] = (gradientMap[i] > FLOOR) ? '#' : '.';
        }
        adjacency.costRules.putAndMoveToFirst('#', WALL);

        neighbors = adjacency.neighborMaps();
        heuristics = new double[adjacency.directions.length];
        for (int i = 0; i < heuristics.length; i++) {
            heuristics[i] = adjacency.measurement.heuristic(adjacency.directions[i]);
        }
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a CustomDijkstraMap that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ).
     *
     * @param level
     * @return
     */
    public CustomDijkstraMap initialize(final char[][] level) {
        return initialize(level, '#');
    }

    /**
     * Used to initialize or re-initialize a CustomDijkstraMap that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ). This
     * initialize() method allows you to specify an alternate wall char other than the default character, '#' .
     *
     * @param level
     * @param alternateWall
     * @return
     */
    public CustomDijkstraMap initialize(final char[][] level, char alternateWall) {
        width = level.length;
        height = level[0].length;
        int rot = adjacency.rotations, len = width*height*rot, dex;
        gradientMap = new double[len];
        physicalMap = new double[len];
        costMap = new int[len];
        IntDoubleOrderedMap cst = adjacency.costRules;
        cst.putAndMoveToFirst(alternateWall, WALL);
        int c;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                c = level[x][y];
                double t = (c == alternateWall) ? WALL : FLOOR;
                for (int r = 0; r < rot; r++) {
                    dex = adjacency.composite(x, y, r, 0);
                    gradientMap[dex] = t;
                    physicalMap[dex] = t;
                    costMap[dex] = c;
                }
            }
        }
        neighbors = adjacency.neighborMaps();
        heuristics = new double[adjacency.directions.length];
        for (int i = 0; i < heuristics.length; i++) {
            heuristics[i] = adjacency.measurement.heuristic(adjacency.directions[i]);
        }
        initialized = true;
        return this;
    }

    /**
     * Used to initialize the entry cost modifiers for games that require variable costs to enter squares. This expects
     * a char[][] of the same exact dimensions as the 2D array that was used to previously initialize() this
     * CustomDijkstraMap, treating the '#' char as a wall (impassable) and anything else as having a normal cost to enter.
     * The costs can be accessed later by using costMap directly (which will have a valid value when this does not
     * throw an exception), or by calling setCost().
     *
     * @param level a 2D char array that uses '#' for walls
     * @return this CustomDijkstraMap for chaining.
     */
    public CustomDijkstraMap initializeCost(final char[][] level) {
        if (!initialized) throw new IllegalStateException("CustomDijkstraMap must be initialized first!");
        int rot = adjacency.rotations;
        int c;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                c = level[x][y];
                for (int r = 0; r < rot; r++) {
                    costMap[adjacency.composite(x, y, r, 0)] = c;
                }
            }
        }
        return this;
    }

    /**
     * Used to initialize the entry cost modifiers for games that require variable costs to enter squares. This expects
     * an int[] with length equal to the length of any inner array of neighbors (a field that is given a value during
     * initialize() by this object's Adjacency value), using the int corresponding to a location as the tile type to
     * look up for that location, as a key in {@link Adjacency#costRules}, even if an int isn't what this class would
     * assign normally -- although, walls and other impassable values should be given '#' (which can be put in an int
     * array) or the value of alternateWall, if this was given one, as a value. The tiles can be accessed later by using
     * costMap directly (which will have a valid value when this does not throw an exception), or by calling setCost().
     * <p/>
     * This method should be slightly more efficient than the other initializeCost methods.
     *
     * @param tiles an int array that already has tile types that {@link #adjacency} can find values for
     * @return this CustomDijkstraMap for chaining.
     */
    public CustomDijkstraMap initializeCost(final int[] tiles) {
        if (!initialized)
            throw new IllegalStateException("CustomDijkstraMap must be initialized first!");
        if(tiles.length != gradientMap.length)
            throw new IllegalArgumentException("costs.length must equal gradientMap.length");
        costMap = new int[tiles.length];
        System.arraycopy(tiles, 0, costMap, 0, tiles.length);
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
        System.arraycopy(physicalMap, 0, gradientMap, 0, physicalMap.length);
    }

    /**
     * Resets this CustomDijkstraMap to a state with no goals, no discovered path, and no changes made to gradientMap
     * relative to physicalMap.
     */
    public void reset() {
        resetMap();
        goals.clear();
        fresh.clear();
        path.clear();
        frustration = 0;
    }

    /**
     * Marks a cell as a goal for pathfinding, unless the cell is a wall or unreachable area (then it does nothing).
     *
     * @param pt
     */
    public void setGoal(int pt) {
        if (!initialized || !adjacency.validate(pt)) return;
        if (physicalMap[pt] > FLOOR) {
            return;
        }
        adjacency.putAllVariants(goals, gradientMap, pt, 0.0);
    }

    /**
     * Marks a cell's type for pathfinding cost as tile (it still will look up the tile in the
     * {@link Adjacency#costRules} field of {@link #adjacency} when it tries to move through one), unless the cell is a
     * wall or unreachable area (then it always sets the cost to a value that should have the same cost as a wall).
     * The normal usage of this is something like {@code setCost(position, '.')} for maps without rotation (this sets
     * the cost of moving into the cell position to the cost of entering a floor marked with '.'; thos is looked up in
     * the Adjacency's cost rules and those can be set with {@link Adjacency#addCostRule(char, double)}). If the map has
     * rotation, {@code setCost(position, '.' | 0x10000)} will change the cost to turn while standing on a tile to the
     * cost of turning on a '.' floor, though this is just one way Adjacency can be implemented (it's how
     * RotationAdjacency works).
     *
     * @param pt the encoded position/rotation/height to set the cost for
     * @param tile typically a char such as '.' for floors, but if this uses rotation, turns on that tile are different
     */
    public void setCost(int pt, int tile) {
        if (!initialized || !adjacency.validate(pt)) return;
        if (physicalMap[pt] > FLOOR) {
            costMap[pt] = adjacency.costRules.firstIntKey();
            return;
        }
        costMap[pt] = tile;
    }

    /**
     * Marks a specific cell in gradientMap as completely impossible to enter.
     *
     * @param pt
     */
    public void setOccupied(int pt) {
        if (!initialized || !adjacency.validate(pt)) return;
        gradientMap[pt] = WALL;
    }

    /**
     * Reverts a cell to the value stored in the original state of the level as known by physicalMap.
     *
     * @param pt
     */
    public void resetCell(int pt) {
        if (!initialized || !adjacency.validate(pt)) return;
        gradientMap[pt] = physicalMap[pt];
    }

    /**
     * Used to remove all goals and undo any changes to gradientMap made by having a goal present.
     */
    public void clearGoals() {
        if (!initialized)
            return;
        int sz = goals.size;
        for (int i = 0; i < sz; i++) {
            resetCell(goals.pop());
        }
    }

    protected void setFresh(final int pt, double counter) {
        if (!initialized || !adjacency.validate(pt)) return;
        if(gradientMap[pt] < counter && gradientMap[pt] < FLOOR)
            return;
        gradientMap[pt] = counter;
        fresh.add(pt);
    }

    public boolean isBlocked(int start, int direction) {
        return adjacency.isBlocked(start, direction, neighbors, gradientMap, WALL);
        /*
        if (rotations != 1) {
            if (rotations <= 4 || (direction & 1) == 0)
                return !adjacency.validate(start);
            return neighbors[1][0][start] < 0 || gradientMap[neighbors[1][0][start]] >= WALL
                    || neighbors[1][2][start] < 0 || gradientMap[neighbors[1][2][start]] >= WALL;
        } else {
            if (direction < 4)
                return !adjacency.validate(start);
            int[][] near = neighbors[0];
            switch (direction) {
                case 4: //UP_LEFT
                    return (near[0][start] < 0 || gradientMap[near[0][start]] >= WALL)
                            && (near[2][start] < 0 || gradientMap[near[2][start]] >= WALL);
                case 5: //UP_RIGHT
                    return (near[0][start] < 0 || gradientMap[near[0][start]] >= WALL)
                            && (near[3][start] < 0 || gradientMap[near[3][start]] >= WALL);
                case 6: //DOWN_LEFT
                    return (near[1][start] < 0 || gradientMap[near[1][start]] >= WALL)
                            && (near[2][start] < 0 || gradientMap[near[2][start]] >= WALL);
                default: //DOWN_RIGHT
                    return (near[1][start] < 0 || gradientMap[near[1][start]] >= WALL)
                            && (near[3][start] < 0 || gradientMap[near[3][start]] >= WALL);
            }
        }
        */
    }

    /**
     * Recalculate the CustomDijkstra map and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.
     *
     * @param usable how much of impassable to actually use; should usually be equal to impassable.length, but can be
     *               anything if impassable is null (then, it is ignored). This exists to differentiate this method from
     *               the overload that takes an IntVLA when that argument is null, but also to give some flexibility.
     * @param impassable An array of int keys (encoded by an Adjacency, usually) representing the locations of enemies
     * or other moving obstacles to a path that cannot be moved through; this can be null (meaning no obstacles).
     * @return An int array using the dimensions of what this knows about the physical map.
     */
    public double[] scan(int usable, int[] impassable) {
        if(impassable == null)
            scanInternal(-1, null, -1);
        else
            scanInternal(-1, impassable, Math.min(usable, impassable.length));
        int maxLength = gradientMap.length;
        double[] gradientClone = new double[maxLength];
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] == FLOOR) {
                gradientMap[l] = DARK;
            }
        }
        System.arraycopy(gradientMap, 0, gradientClone, 0, maxLength);
        return gradientClone;
    }
    /**
     * Recalculate the CustomDijkstra map and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.
     *
     * @param impassable An array of int keys (encoded by an Adjacency, usually) representing the locations of enemies
     * or other moving obstacles to a path that cannot be moved through; this can be null (meaning no obstacles).
     * @return An int array using the dimensions of what this knows about the physical map.
     */
    public double[] scan(IntVLA impassable) {
        if(impassable == null)
            scanInternal(-1, null, -1);
        else
            scanInternal(-1, impassable.items, impassable.size);
        int maxLength = gradientMap.length;
        double[] gradientClone = new double[maxLength];
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] == FLOOR) {
                gradientMap[l] = DARK;
            }
        }
        System.arraycopy(gradientMap, 0, gradientClone, 0, maxLength);
        return gradientClone;

    }

    /**
     * Recalculate the CustomDijkstra map, stopping early if it has a path from a goal to start, and return that map.
     * Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.
     *
     * @param start the encoded index of the start of the pathfinder; when this has a path from goal to start, it ends
     * @param usable how much of impassable to actually use; should usually be equal to impassable.length, but can be
     *               anything if impassable is null (then, it is ignored). This exists to differentiate this method from
     *               the overload that takes an IntVLA when that argument is null, but also to give some flexibility.
     * @param impassable An array of int keys (encoded by an Adjacency, usually) representing the locations of enemies
     * or other moving obstacles to a path that cannot be moved through; this can be null (meaning no obstacles).
     * @return An int array using the dimensions of what this knows about the physical map.
     */
    public double[] scanToStart(int start, int usable, int[] impassable) {
        if(impassable == null)
            scanInternal(start, null, -1);
        else
            scanInternal(start, impassable, Math.min(usable, impassable.length));
        return gradientMap;
    }
    /**
     * Recalculate the CustomDijkstra map, stopping early if it has a path from a goal to start, and return that map.
     * Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.
     *
     * @param start the encoded index of the start of the pathfinder; when this has a path from goal to start, it ends
     * @param impassable An array of int keys (encoded by an Adjacency, usually) representing the locations of enemies
     * or other moving obstacles to a path that cannot be moved through; this can be null (meaning no obstacles).
     * @return An int array using the dimensions of what this knows about the physical map.
     */
    public double[] scanToStart(int start, IntVLA impassable) {
        if(impassable == null)
            scanInternal(start, null, -1);
        else
            scanInternal(start, impassable.items, impassable.size);
        return gradientMap;
    }

    protected void scanInternal(int start, int[] impassable, int usable)
    {
        if (!initialized) return;
        Adjacency adjacency = this.adjacency;
        int[][] fromNeighbors = neighbors[0];
        int near, cen, mid, neighborCount = fromNeighbors.length;

        if (impassable != null) {
            if(usable > impassable.length)
                usable = impassable.length;
            for (int i = 0; i < usable; i++) {
                adjacency.putAllVariants(null, gradientMap, impassable[i], WALL);
            }
        }
        boolean standardCosts = adjacency.hasStandardCost();
        mappedCount = goals.size;
        for (int i = 0; i < mappedCount; i++) {
            gradientMap[goals.get(i)] = 0;
        }
        double currentLowest = 999000, cs, csm, dist;
        fresh.clear();
        int maxLength = gradientMap.length;
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] <= FLOOR) {
                if (gradientMap[l] < currentLowest) {
                    currentLowest = gradientMap[l];
                    fresh.clear();
                    fresh.add(l);
                } else if (gradientMap[l] == currentLowest) {
                    fresh.add(l);
                }
            }
        }
        int fsz, numAssigned = fresh.size;
        IntDoubleOrderedMap costs = adjacency.costRules;
        while (numAssigned > 0) {
            numAssigned = 0;
            fsz = fresh.size;
            for (int ci = fsz-1; ci >= 0; ci--) {
                cen = fresh.removeIndex(ci);
                dist = gradientMap[cen];
                for (int d = 0; d < neighborCount; d++) {
                    near = fromNeighbors[d][cen];
                    if (!adjacency.validate(near))
                    	// Outside the map
                    	continue;
                    if(isBlocked(cen, d))
                        continue;
                    if(adjacency.twoStepRule) {
                        near = fromNeighbors[d][mid = near];
                        // Outside the map
                        if (!adjacency.validate(near))
                            continue;
                        if(isBlocked(mid, d))
                            continue;
                        csm = (costs.get(costMap[mid]) * heuristics[d]);
                        cs = (costs.get(costMap[near]) * heuristics[d]);
                        if ((gradientMap[mid] = dist + csm) + cs < gradientMap[near]) {
                            setFresh(near, dist + cs + csm);
                            ++numAssigned;
                            ++mappedCount;
                            if(start == near && standardCosts)
                            {
                                if (impassable != null)
                                    adjacency.resetAllVariants(gradientMap, impassable, usable, physicalMap, 1);
                                return;
                            }
                        }
                    }
                    else
                    {
                        cs = (costs.get(costMap[near] | (adjacency.extractR(cen) == adjacency.extractR(near) ? 0 : 0x10000))
                                        * heuristics[d]);
                        //int h = adjacency.measurement.heuristic(adjacency.directions[d]);
                        if (gradientMap[cen] + cs < gradientMap[near]) {
                            setFresh(near, dist + cs);
                            ++numAssigned;
                            ++mappedCount;
                            if(start == near && standardCosts)
                            {
                                if (impassable != null)
                                    adjacency.resetAllVariants(gradientMap, impassable, usable, physicalMap, 1);
                                return;
                            }
                        }
                    }
                }
            }
        }
        if (impassable != null)
            adjacency.resetAllVariants(gradientMap, impassable, usable, physicalMap, 1);
    }

    /**
     * Recalculate the CustomDijkstra map up to a limit and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. If a cell would take more steps to
     * reach than the given limit, it will have a value of DARK if it was passable instead of the distance. The
     * exceptions are walls, which will have a value defined by the WALL constant in this class, and areas that the scan
     * was unable to reach, which will have a value defined by the DARK constant in this class. This uses the
     * current measurement.
     *
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param usable how much of impassable to actually use; should usually be equal to impassable.length, but can be
     *               anything if impassable is null (then, it is ignored). This exists to differentiate this method from
     *               the overload that takes an IntVLA when that argument is null, but also to give some flexibility.
     * @param impassable An array or vararg of int keys representing the locations of enemies or other moving obstacles
     *                   to a path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A int array using the dimensions of what this knows about the physical map.
     */
    public double[] partialScan(int limit, int usable, int[] impassable) {
        if(impassable == null)
            partialScanInternal(-1, limit, null, -1);
        else
            partialScanInternal(-1, -1, impassable, Math.min(usable, impassable.length));
        int maxLength = gradientMap.length;
        double[] gradientClone = new double[maxLength];
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] == FLOOR) {
                gradientMap[l] = DARK;
            }
        }
        System.arraycopy(gradientMap, 0, gradientClone, 0, maxLength);
        return gradientClone;
    }

    /**
     * Recalculate the CustomDijkstra map up to a limit and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. If a cell would take more steps to
     * reach than the given limit, it will have a value of DARK if it was passable instead of the distance. The
     * exceptions are walls, which will have a value defined by the WALL constant in this class, and areas that the scan
     * was unable to reach, which will have a value defined by the DARK constant in this class. This uses the
     * current measurement.
     *
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param impassable An IntVLA of int keys representing the locations of enemies or other moving obstacles
     *                   to a path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A int array using the dimensions of what this knows about the physical map.
     */
    public double[] partialScan(int limit, IntVLA impassable) {
        if(impassable == null)
            partialScanInternal(-1, limit, null, -1);
        else
            partialScanInternal(-1, limit, impassable.items, impassable.size);
        int maxLength = gradientMap.length;
        double[] gradientClone = new double[maxLength];
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] == FLOOR) {
                gradientMap[l] = DARK;
            }
        }
        System.arraycopy(gradientMap, 0, gradientClone, 0, maxLength);
        return gradientClone;
    }


    /**
     * Recalculate the CustomDijkstra map up to a limit, stopping early if it has a path from a goal to start, and
     * return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.
     *
     * @param start the encoded index of the start of the pathfinder; when this has a path from goal to start, it ends
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param usable how much of impassable to actually use; should usually be equal to impassable.length, but can be
     *               anything if impassable is null (then, it is ignored). This exists to differentiate this method from
     *               the overload that takes an IntVLA when that argument is null, but also to give some flexibility.
     * @param impassable An array of int keys (encoded by an Adjacency, usually) representing the locations of enemies
     * or other moving obstacles to a path that cannot be moved through; this can be null (meaning no obstacles).
     * @return An int array using the dimensions of what this knows about the physical map.
     */
    public double[] partialScanToStart(int limit, int start, int usable, int[] impassable) {
        if(impassable == null)
            partialScanInternal(start, limit, null, -1);
        else
            partialScanInternal(start, limit, impassable, Math.min(usable, impassable.length));
        return gradientMap;
    }
    /**
     * Recalculate the CustomDijkstra map up to a limit, stopping early if it has a path from a goal to start, and
     * return that map. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.
     *
     * @param start the encoded index of the start of the pathfinder; when this has a path from goal to start, it ends
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param impassable An array of int keys (encoded by an Adjacency, usually) representing the locations of enemies
     * or other moving obstacles to a path that cannot be moved through; this can be null (meaning no obstacles).
     * @return An int array using the dimensions of what this knows about the physical map.
     */
    public double[] partialScanToStart(int limit, int start, IntVLA impassable) {
        if(impassable == null)
            partialScanInternal(start, limit, null, -1);
        else
            partialScanInternal(start, limit, impassable.items, impassable.size);
        return gradientMap;
    }

    protected void partialScanInternal(int start, int limit, int[] impassable, int usable)
    {
        if (!initialized) return;
        Adjacency adjacency = this.adjacency;
        int[][] fromNeighbors = neighbors[0];
        int near, cen, mid, neighborCount = fromNeighbors.length;

        if (impassable != null) {
            if(usable > impassable.length)
                usable = impassable.length;
            for (int i = 0; i < usable; i++) {
                adjacency.putAllVariants(null, gradientMap, impassable[i], WALL);
            }
        }
        boolean standardCosts = adjacency.hasStandardCost();
        mappedCount = goals.size;
        for (int i = 0; i < mappedCount; i++) {
            gradientMap[goals.get(i)] = 0;
        }
        double currentLowest = 999000, cs, csm, dist;
        fresh.clear();
        int maxLength = gradientMap.length;
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] <= FLOOR) {
                if (gradientMap[l] < currentLowest) {
                    currentLowest = gradientMap[l];
                    fresh.clear();
                    fresh.add(l);
                } else if (gradientMap[l] == currentLowest) {
                    fresh.add(l);
                }
            }
        }
        int fsz, numAssigned = fresh.size;
        IntDoubleOrderedMap costs = adjacency.costRules;
        int iter = 0;
        while (numAssigned > 0 && iter++ < limit) {
            numAssigned = 0;
            fsz = fresh.size;
            for (int ci = fsz-1; ci >= 0; ci--) {
                cen = fresh.removeIndex(ci);
                dist = gradientMap[cen];
                for (int d = 0; d < neighborCount; d++) {
                    near = fromNeighbors[d][cen];
                    if (!adjacency.validate(near))
                        // Outside the map
                        continue;
                    if(isBlocked(cen, d))
                        continue;
                    if(adjacency.twoStepRule) {
                        near = fromNeighbors[d][mid = near];
                        // Outside the map
                        if (!adjacency.validate(near))
                            continue;
                        if(isBlocked(mid, d))
                            continue;
                        csm = (costs.get(costMap[mid]) * heuristics[d]);
                        cs = (costs.get(costMap[near]) * heuristics[d]);
                        if ((gradientMap[mid] = dist + csm) + cs < gradientMap[near]) {
                            setFresh(near, dist + cs + csm);
                            ++numAssigned;
                            ++mappedCount;
                            if(start == near && standardCosts)
                            {
                                if (impassable != null)
                                    adjacency.resetAllVariants(gradientMap, impassable, usable, physicalMap, 1);
                                return;
                            }
                        }
                    }
                    else
                    {
                        cs = (costs.get(costMap[near] | (adjacency.extractR(cen) == adjacency.extractR(near) ? 0 : 0x10000))
                                * heuristics[d]);
                        //int h = adjacency.measurement.heuristic(adjacency.directions[d]);
                        if (gradientMap[cen] + cs < gradientMap[near]) {
                            setFresh(near, dist + cs);
                            ++numAssigned;
                            ++mappedCount;
                            if(start == near && standardCosts)
                            {
                                if (impassable != null)
                                    adjacency.resetAllVariants(gradientMap, impassable, usable, physicalMap, 1);
                                return;
                            }
                        }
                    }
                }
            }
        }
        if (impassable != null)
            adjacency.resetAllVariants(gradientMap, impassable, usable, physicalMap, 1);
    }

    /*
     * Recalculate the CustomDijkstra map until it reaches a cell index in targets, then returns the first target found.
     * This uses the current measurement.
     *
     * @param start   the cell to use as the origin for finding the nearest target
     * @param targets the cell indices that this is trying to find; it will stop once it finds one
     * @return the cell index that it found first.
     * /
    public int findNearest(int start, int... targets) {
        if (!initialized) return -1;
        if (targets == null)
            return -1;
        for (int i = 0; i < targets.length; i++) {
            if(targets[i] == start)
                return start;
        }
        resetMap();
        int xShift = width / 8, yShift = height / 8;
        while (physicalMap[start.x][start.y] >= WALL && frustration < 50) {
            start2 = Coord.get(Math.min(Math.max(1, start.x + rng.nextInt(1 + xShift * 2) - xShift), width - 2),
                    Math.min(Math.max(1, start.y + rng.nextInt(1 + yShift * 2) - yShift), height - 2));
        }
        if (closed.containsKey(start2.encode()))
            closed.remove(start2.encode());
        gradientMap[start2.x][start2.y] = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (gradientMap[x][y] > FLOOR && !goals.containsKey(Coord.pureEncode(x, y)))
                    closed.put(Coord.pureEncode(x, y), physicalMap[x][y]);
            }
        }
        int numAssigned = 1;
        mappedCount = 1;
        open.put(start2.encode(), 0);
        int near, cen;
        int enc;

        while (numAssigned > 0) {

            numAssigned = 0;

            for (IntDoubleOrderedMap.MapEntry cell : open.mapEntrySet()) {
                cen = cell.getIntKey();
                for (int d = 0; d < neighbors.length; d++) {
                    near = neighbors[d][cen];
                    if (!adjacency.validate(near))
                        // Outside the map
                        continue;
                    dir = adjacency.directions[d];
                    if(adjacency.isBlocked(cen, d, neighbors, gradientMap, WALL))
                        continue;
                    int h = adjacency.measurement.heuristic(dir);
                    if (!closed.containsKey(near) && !open.containsKey(near) && gradientMap[cen] + h * costMap[near] < gradientMap[near]) {
                        setFresh(near, cell.getDoubleValue() + h * costMap[near]);
                        ++numAssigned;
                        ++mappedCount;
                    }
                }
            }
            open.clear();
            open.putAll(fresh);
            fresh.clear();


            numAssigned = 0;

            for (IntDoubleOrderedMap.MapEntry cell : open.mapEntrySet()) {
                cen = Coord.decode(cell.getIntKey());
                for (int d = 0; d < dirs.length; d++) {
                    adj = cen.translate(dirs[d].deltaX, dirs[d].deltaY);
                    if (adj.x < 0 || adj.y < 0 || width <= adj.x || height <= adj.y)
                    	// Outside the map
                        continue;
                    enc = adj.encode();
                    int h = heuristic(dirs[d]);
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

            open.clear();
            open.putAll(fresh);
            fresh.clear();
        }
        closed.clear();
        open.clear();
        return null;
    }
    */


    /*
     * Recalculate the CustomDijkstra map until it reaches a Coord in targets, then returns the first target found.
     * This uses the current measurement.
     *
     * @param start   the cell to use as the origin for finding the nearest target
     * @param targets the Coords that this is trying to find; it will stop once it finds one
     * @return the Coord that it found first.
     *
    public Coord findNearest(Coord start, Coord... targets) {
        OrderedSet<Coord> tgts = new OrderedSet<>(targets.length);
        Collections.addAll(tgts, targets);
        return findNearest(start, tgts);
    }
    */

    /*
     * If you have a target or group of targets you want to pathfind to without scanning the full map, this can be good.
     * It may find sub-optimal paths in the presence of costs to move into cells. It is useful when you want to move in
     * a straight line to a known nearby goal.
     *
     * @param start   your starting location
     * @param targets an array or vararg of Coords to pathfind to the nearest of
     * @return an ArrayList of Coord that goes from a cell adjacent to start and goes to one of the targets. Copy of path.
     *
    public ArrayList<Coord> findShortcutPath(Coord start, Coord... targets) {
        if (targets.length == 0) {
            path.clear();
            return new IntVLA(path);
        }
        Coord currentPos = findNearest(start, targets);
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            int best = gradientMap[currentPos.x][currentPos.y];
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(dirs.length);

            for (int d = 0; d < dirs.length; d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (gradientMap[pt.x][pt.y] < best) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                path.clear();
                break;
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
            path.add(currentPos);
            frustration++;
        }
        frustration = 0;
        Collections.reverse(path);
        return new ArrayList<>(path);

    }
    */


    /*
     * Recalculate the CustomDijkstra map until it reaches a Coord in targets, then returns the first several targets found,
     * up to limit or less if the map is fully searched without finding enough.
     * This uses the current measurement.
     *
     * @param start   the cell to use as the origin for finding the nearest targets
     * @param limit   the maximum number of targets to find before returning
     * @param targets the Coords that this is trying to find; it will stop once it finds enough (based on limit)
     * @return the Coords that it found first.
     *
    public ArrayList<Coord> findNearestMultiple(Coord start, int limit, Set<Coord> targets) {
        if (!initialized) return null;
        if (targets == null)
            return null;
        ArrayList<Coord> found = new ArrayList<>(limit);
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
        gradientMap[start2.x][start2.y] = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (gradientMap[x][y] > FLOOR && !goals.containsKey(Coord.pureEncode(x, y)))
                    closed.put(Coord.pureEncode(x, y), physicalMap[x][y]);
            }
        }
        int numAssigned = 1;
        mappedCount = 1;
        open.put(start2.encode(), 0);

        Direction[] dirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;
        while (numAssigned > 0) {
//            ++iter;
            numAssigned = 0;
            for (IntDoubleOrderedMap.MapEntry cell : open.mapEntrySet()) {
                cen = Coord.decode(cell.getIntKey());
                for (int d = 0; d < dirs.length; d++) {
                    adj = cen.translate(dirs[d].deltaX, dirs[d].deltaY);
                    if (adj.x < 0 || adj.y < 0 || width <= adj.x || height <= adj.y)
                    	// Outside the map
                        continue;
                    enc = adj.encode();

                    int h = heuristic(dirs[d]);
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
    */

    /**
     * Recalculate the CustomDijkstra map for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned CustomDijkstra map assumes that a creature is square, with a side length equal to the passed
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
     * <br>
     * Portals and wrapping are not currently recommended in conjunction with multi-square creatures, since a
     * 2x2 creature could easily occupy two cells on the east edge and two cells on the west edge of the map,
     * and that poses all sorts of issues for creatures trying to pathfind to it, not to mention the more
     * general issues of how to display a bisected, but mobile, creature.
     *
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @param usable how much of impassable to actually use; should usually be equal to impassable.length, but can be
     *               anything if impassable is null (then, it is ignored). This exists to differentiate this method from
     *               the overload that takes an IntVLA when that argument is null, but also to give some flexibility.
     * @param impassable An array of encoded int keys representing the locations of enemies or other moving obstacles to
     *                   a path that cannot be moved through; this can be null if there are no such obstacles.
     * @return An int array using the dimensions/rotations of what this knows about the physical map.
     */
    public double[] scanLarge(int size, int usable, int[] impassable) {
        if(impassable == null)
            scanLargeInternal(-1, size, null, -1);
        else
            scanLargeInternal(-1, size, impassable, Math.min(usable, impassable.length));
        int maxLength = gradientMap.length;
        double[] gradientClone = new double[maxLength];
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] == FLOOR) {
                gradientMap[l] = DARK;
            }
        }
        System.arraycopy(gradientMap, 0, gradientClone, 0, maxLength);
        return gradientClone;
    }
    /**
     * Recalculate the CustomDijkstra map for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned CustomDijkstra map assumes that a creature is square, with a side length equal to the passed
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
     * <br>
     * Portals and wrapping are not currently recommended in conjunction with multi-square creatures, since a
     * 2x2 creature could easily occupy two cells on the east edge and two cells on the west edge of the map,
     * and that poses all sorts of issues for creatures trying to pathfind to it, not to mention the more
     * general issues of how to display a bisected, but mobile, creature.
     *
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @param impassable An IntVLA where items are ints representing the locations of enemies or other moving obstacles
     *                   to a path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D int[width][height] using the width and height of what this knows about the physical map.
     */
    public double[] scanLarge(int size, IntVLA impassable) {
        if(impassable == null)
            scanLargeInternal(-1, size, null, -1);
        else
            scanLargeInternal(-1, size, impassable.items, impassable.size);
        int maxLength = gradientMap.length;
        double[] gradientClone = new double[maxLength];
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] == FLOOR) {
                gradientMap[l] = DARK;
            }
        }
        System.arraycopy(gradientMap, 0, gradientClone, 0, maxLength);
        return gradientClone;
    }
    /**
     * Recalculate the CustomDijkstra map for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned CustomDijkstra map assumes that a creature is square, with a side length equal to the passed
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
     * <br>
     * Portals and wrapping are not currently recommended in conjunction with multi-square creatures, since a
     * 2x2 creature could easily occupy two cells on the east edge and two cells on the west edge of the map,
     * and that poses all sorts of issues for creatures trying to pathfind to it, not to mention the more
     * general issues of how to display a bisected, but mobile, creature.
     *
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @param start the encoded index of the start of the pathfinder; when this has a path from goal to start, it ends
     * @param usable how much of impassable to actually use; should usually be equal to impassable.length, but can be
     *               anything if impassable is null (then, it is ignored). This exists to differentiate this method from
     *               the overload that takes an IntVLA when that argument is null, but also to give some flexibility.
     * @param impassable An array of encoded int keys representing the locations of enemies or other moving obstacles to
     *                   a path that cannot be moved through; this can be null if there are no such obstacles.
     * @return An int array using the dimensions/rotations of what this knows about the physical map.
     */
    public double[] scanToStartLarge(int size, int start, int usable, int[] impassable) {
        if(impassable == null)
            scanLargeInternal(start, size, null, -1);
        else
            scanLargeInternal(start, size, impassable, Math.min(usable, impassable.length));
        int maxLength = gradientMap.length;
        double[] gradientClone = new double[maxLength];
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] == FLOOR) {
                gradientMap[l] = DARK;
            }
        }
        System.arraycopy(gradientMap, 0, gradientClone, 0, maxLength);
        return gradientClone;
    }
    /**
     * Recalculate the CustomDijkstra map for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned CustomDijkstra map assumes that a creature is square, with a side length equal to the passed
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
     * <br>
     * Portals and wrapping are not currently recommended in conjunction with multi-square creatures, since a
     * 2x2 creature could easily occupy two cells on the east edge and two cells on the west edge of the map,
     * and that poses all sorts of issues for creatures trying to pathfind to it, not to mention the more
     * general issues of how to display a bisected, but mobile, creature.
     *
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @param start the encoded index of the start of the pathfinder; when this has a path from goal to start, it ends
     * @param impassable An IntVLA where items are ints representing the locations of enemies or other moving obstacles
     *                   to a path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D int[width][height] using the width and height of what this knows about the physical map.
     */
    public double[] scanToStartLarge(int size, int start, IntVLA impassable) {
        if(impassable == null)
            scanLargeInternal(start, size, null, -1);
        else
            scanLargeInternal(start, size, impassable.items, impassable.size);
        int maxLength = gradientMap.length;
        double[] gradientClone = new double[maxLength];
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] == FLOOR) {
                gradientMap[l] = DARK;
            }
        }
        System.arraycopy(gradientMap, 0, gradientClone, 0, maxLength);
        return gradientClone;
    }

    protected void scanLargeInternal(int start, int size, int[] impassable, int usable)
    {
        if (!initialized) return;
        Adjacency adjacency = this.adjacency;
        int[][] fromNeighbors = neighbors[0];
        int near, cen, mid, neighborCount = fromNeighbors.length, sx, sy, sr, sn;

        if (impassable != null) {
            if(usable > impassable.length)
                usable = impassable.length;
            for (int i = 0; i < usable; i++) {
                adjacency.putAllVariants(null, gradientMap, impassable[i], WALL, -size);
            }
        }
        mappedCount = goals.size;
        for (int i = 0; i < mappedCount; i++) {
            cen = goals.get(i);
            sx = adjacency.extractX(cen);
            sy = adjacency.extractY(cen);
            sr = adjacency.extractR(cen);
            sn = adjacency.extractN(cen);
            for (int xx = 0; xx < size; xx++) {
                for (int yy = 0; yy < size; yy++) {
                    cen = adjacency.composite(sx - xx, sy - yy, sr, sn);
                    if(cen >= 0)
                        gradientMap[cen] = 0;
                }
            }
        }
        double currentLowest = 999000, cs, csm, dist;
        fresh.clear();
        int maxLength = gradientMap.length;
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] <= FLOOR) {
                if (gradientMap[l] < currentLowest) {
                    currentLowest = gradientMap[l];
                    fresh.clear();
                    fresh.add(l);
                } else if (gradientMap[l] == currentLowest) {
                    fresh.add(l);
                }
            }
        }
        int fsz, numAssigned = fresh.size;
        IntDoubleOrderedMap costs = adjacency.costRules;
        boolean standardCosts = adjacency.hasStandardCost();
        while (numAssigned > 0) {
            numAssigned = 0;
            fsz = fresh.size;
            for (int ci = fsz-1; ci >= 0; ci--) {
                cen = fresh.removeIndex(ci);
                dist = gradientMap[cen];
                for (int d = 0; d < neighborCount; d++) {
                    near = fromNeighbors[d][cen];
                    if (!adjacency.validate(near))
                        // Outside the map
                        continue;
                    if(isBlocked(cen, d))
                        continue;
                    if(adjacency.twoStepRule) {
                        near = fromNeighbors[d][mid = near];
                        // Outside the map
                        if (!adjacency.validate(near))
                            continue;
                        if(isBlocked(mid, d))
                            continue;
                        csm = (costs.get(costMap[mid]) * heuristics[d]);
                        cs = (costs.get(costMap[near]) * heuristics[d]);
                        if ((gradientMap[mid] = dist + csm) + cs < gradientMap[near]) {
                            setFresh(near, dist + cs + csm);
                            ++numAssigned;
                            ++mappedCount;
                            if(start == near && standardCosts)
                            {
                                if (impassable != null)
                                    adjacency.resetAllVariants(gradientMap, impassable, usable, physicalMap, -size);
                                return;
                            }
                        }
                    }
                    else
                    {
                        cs = (costs.get(costMap[near] | (adjacency.extractR(cen) == adjacency.extractR(near) ? 0 : 0x10000))
                                * heuristics[d]);
                        //int h = adjacency.measurement.heuristic(adjacency.directions[d]);
                        if (gradientMap[cen] + cs < gradientMap[near]) {
                            setFresh(near, dist + cs);
                            ++numAssigned;
                            ++mappedCount;
                            if(start == near && standardCosts)
                            {
                                if (impassable != null)
                                    adjacency.resetAllVariants(gradientMap, impassable, usable, physicalMap, -size);
                                return;
                            }
                        }
                    }
                }
            }
        }

        if (impassable != null)
            adjacency.resetAllVariants(gradientMap, impassable, usable, physicalMap, -size);
    }


    /**
     * Recalculate the CustomDijkstra map for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned CustomDijkstra map assumes that a creature is square, with a side length equal to the passed
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
     * <br>
     * Portals and wrapping are not currently recommended in conjunction with multi-square creatures, since a
     * 2x2 creature could easily occupy two cells on the east edge and two cells on the west edge of the map,
     * and that poses all sorts of issues for creatures trying to pathfind to it, not to mention the more
     * general issues of how to display a bisected, but mobile, creature.
     *
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param usable how much of impassable to actually use; should usually be equal to impassable.length, but can be
     *               anything if impassable is null (then, it is ignored). This exists to differentiate this method from
     *               the overload that takes an IntVLA when that argument is null, but also to give some flexibility.
     * @param impassable An array of encoded int keys representing the locations of enemies or other moving obstacles to
     *                   a path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D int[width][height] using the width and height of what this knows about the physical map.
     */
    public double[] partialScanLarge(int size, int limit, int usable, int[] impassable) {
        if(impassable == null)
            partialScanLargeInternal(-1, size, limit, null, -1);
        else
            partialScanLargeInternal(-1, size, limit, impassable, Math.min(usable, impassable.length));
        int maxLength = gradientMap.length;
        double[] gradientClone = new double[maxLength];
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] == FLOOR) {
                gradientMap[l] = DARK;
            }
        }
        System.arraycopy(gradientMap, 0, gradientClone, 0, maxLength);
        return gradientClone;
    }
    /**
     * Recalculate the CustomDijkstra map, up to a limit, for a creature that is potentially larger than 1x1 cell and
     * return it. The value of a cell in the returned CustomDijkstra map assumes that a creature is square, with a side
     * length equal to the passed size, that its minimum-x, minimum-y cell is the starting cell, and that any cell with
     * a distance number represents the distance for the creature's minimum-x, minimum-y cell to reach it. Cells that
     * cannot be entered by the minimum-x, minimum-y cell because of sizing (such as a floor cell next to a maximum-x
     * and/or maximum-y wall if size is &gt; 1) will be marked as DARK. Cells that were marked as goals with setGoal
     * will have a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class. (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.
     * <br>
     * Portals and wrapping are not currently recommended in conjunction with multi-square creatures, since a
     * 2x2 creature could easily occupy two cells on the east edge and two cells on the west edge of the map,
     * and that poses all sorts of issues for creatures trying to pathfind to it, not to mention the more
     * general issues of how to display a bisected, but mobile, creature.
     *
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param impassable An IntVLA where items are ints representing the locations of enemies or other moving obstacles
     *                   to a path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D int[width][height] using the width and height of what this knows about the physical map.
     */
    public double[] partialScanLarge(int size, int limit, IntVLA impassable) {
        if(impassable == null)
            partialScanLargeInternal(-1, size, limit, null, -1);
        else
            partialScanLargeInternal(-1, size, limit, impassable.items, impassable.size);
        int maxLength = gradientMap.length;
        double[] gradientClone = new double[maxLength];
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] == FLOOR) {
                gradientMap[l] = DARK;
            }
        }
        System.arraycopy(gradientMap, 0, gradientClone, 0, maxLength);
        return gradientClone;
    }
    /**
     * Recalculate the CustomDijkstra map, up to a limit, for a creature that is potentially larger than 1x1 cell,
     * stopping early if a path is found between a goal and start, and return that map. The value of
     * a cell in the returned CustomDijkstra map assumes that a creature is square, with a side length equal to the passed
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
     * <br>
     * Portals and wrapping are not currently recommended in conjunction with multi-square creatures, since a
     * 2x2 creature could easily occupy two cells on the east edge and two cells on the west edge of the map,
     * and that poses all sorts of issues for creatures trying to pathfind to it, not to mention the more
     * general issues of how to display a bisected, but mobile, creature.
     *
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param start the encoded index of the start of the pathfinder; when this has a path from goal to start, it ends
     * @param usable how much of impassable to actually use; should usually be equal to impassable.length, but can be
     *               anything if impassable is null (then, it is ignored). This exists to differentiate this method from
     *               the overload that takes an IntVLA when that argument is null, but also to give some flexibility.
     * @param impassable An array of encoded int keys representing the locations of enemies or other moving obstacles to
     *                   a path that cannot be moved through; this can be null if there are no such obstacles.
     * @return An int array using the dimensions/rotations of what this knows about the physical map.
     */
    public double[] partialScanToStartLarge(int size, int limit, int start, int usable, int[] impassable) {
        if(impassable == null)
            partialScanLargeInternal(start, size, limit, null, -1);
        else
            partialScanLargeInternal(start, size, limit, impassable, Math.min(usable, impassable.length));
        int maxLength = gradientMap.length;
        double[] gradientClone = new double[maxLength];
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] == FLOOR) {
                gradientMap[l] = DARK;
            }
        }
        System.arraycopy(gradientMap, 0, gradientClone, 0, maxLength);
        return gradientClone;
    }
    /**
     * Recalculate the CustomDijkstra map, up to a limit, for a creature that is potentially larger than 1x1 cell,
     * stopping early if a path is found between a goal and start, and return that map. The value of
     * a cell in the returned CustomDijkstra map assumes that a creature is square, with a side length equal to the passed
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
     * <br>
     * Portals and wrapping are not currently recommended in conjunction with multi-square creatures, since a
     * 2x2 creature could easily occupy two cells on the east edge and two cells on the west edge of the map,
     * and that poses all sorts of issues for creatures trying to pathfind to it, not to mention the more
     * general issues of how to display a bisected, but mobile, creature.
     *
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param start the encoded index of the start of the pathfinder; when this has a path from goal to start, it ends
     * @param impassable An IntVLA where items are ints representing the locations of enemies or other moving obstacles
     *                   to a path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D int[width][height] using the width and height of what this knows about the physical map.
     */
    public double[] partialScanToStartLarge(int size, int limit, int start, IntVLA impassable) {
        if(impassable == null)
            partialScanLargeInternal(start, size, limit, null, -1);
        else
            partialScanLargeInternal(start, size, limit, impassable.items, impassable.size);
        int maxLength = gradientMap.length;
        double[] gradientClone = new double[maxLength];
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] == FLOOR) {
                gradientMap[l] = DARK;
            }
        }
        System.arraycopy(gradientMap, 0, gradientClone, 0, maxLength);
        return gradientClone;
    }

    protected void partialScanLargeInternal(int start, int size, int limit, int[] impassable, int usable)
    {
        if (!initialized) return;
        Adjacency adjacency = this.adjacency;
        int[][] fromNeighbors = neighbors[0];
        int near, cen, mid, neighborCount = fromNeighbors.length, sx, sy, sr, sn;

        if (impassable != null) {
            if(usable > impassable.length)
                usable = impassable.length;
            for (int i = 0; i < usable; i++) {
                adjacency.putAllVariants(null, gradientMap, impassable[i], WALL, -size);
            }
        }
        mappedCount = goals.size;
        for (int i = 0; i < mappedCount; i++) {
            cen = goals.get(i);
            sx = adjacency.extractX(cen);
            sy = adjacency.extractY(cen);
            sr = adjacency.extractR(cen);
            sn = adjacency.extractN(cen);
            for (int xx = 0; xx < size; xx++) {
                for (int yy = 0; yy < size; yy++) {
                    cen = adjacency.composite(sx - xx, sy - yy, sr, sn);
                    if(cen >= 0)
                        gradientMap[cen] = 0;
                }
            }
        }
        double currentLowest = 999000, cs, csm, dist;
        fresh.clear();
        int maxLength = gradientMap.length;
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] <= FLOOR) {
                if (gradientMap[l] < currentLowest) {
                    currentLowest = gradientMap[l];
                    fresh.clear();
                    fresh.add(l);
                } else if (gradientMap[l] == currentLowest) {
                    fresh.add(l);
                }
            }
        }
        int fsz, numAssigned = fresh.size;
        IntDoubleOrderedMap costs = adjacency.costRules;
        boolean standardCosts = adjacency.hasStandardCost();
        int iter = 0;
        while (numAssigned > 0 && iter++ < limit) {
            numAssigned = 0;
            fsz = fresh.size;
            for (int ci = fsz-1; ci >= 0; ci--) {
                cen = fresh.removeIndex(ci);
                dist = gradientMap[cen];
                for (int d = 0; d < neighborCount; d++) {
                    near = fromNeighbors[d][cen];
                    if (!adjacency.validate(near))
                        // Outside the map
                        continue;
                    if(isBlocked(cen, d))
                        continue;
                    if(adjacency.twoStepRule) {
                        near = fromNeighbors[d][mid = near];
                        // Outside the map
                        if (!adjacency.validate(near))
                            continue;
                        if(isBlocked(mid, d))
                            continue;
                        csm = (costs.get(costMap[mid]) * heuristics[d]);
                        cs = (costs.get(costMap[near]) * heuristics[d]);
                        if ((gradientMap[mid] = dist + csm) + cs < gradientMap[near]) {
                            setFresh(near, dist + cs + csm);
                            ++numAssigned;
                            ++mappedCount;
                            if(start == near && standardCosts)
                            {
                                if (impassable != null)
                                    adjacency.resetAllVariants(gradientMap, impassable, usable, physicalMap, -size);
                                return;
                            }
                        }
                    }
                    else
                    {
                        cs = (costs.get(costMap[near] | (adjacency.extractR(cen) == adjacency.extractR(near) ? 0 : 0x10000))
                                * heuristics[d]);
                        //int h = adjacency.measurement.heuristic(adjacency.directions[d]);
                        if (gradientMap[cen] + cs < gradientMap[near]) {
                            setFresh(near, dist + cs);
                            ++numAssigned;
                            ++mappedCount;
                            if(start == near && standardCosts)
                            {
                                if (impassable != null)
                                    adjacency.resetAllVariants(gradientMap, impassable, usable, physicalMap, -size);
                                return;
                            }
                        }
                    }
                }
            }
        }

        if (impassable != null)
            adjacency.resetAllVariants(gradientMap, impassable, usable, physicalMap, -size);
    }

    /**
     * Scans the dungeon using CustomDijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to the closest reachable
     * goal. The maximum length of the returned list is given by length; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
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
    public IntVLA findPath(int length, IntVLA impassable,
                           IntVLA onlyPassable, int start, int... targets)
    {
        return findPath(length, -1, impassable, onlyPassable, start, targets);
    }


    /**
     * Scans the dungeon using CustomDijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to the closest reachable
     * goal. The maximum length of the returned list is given by length; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
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
    public IntVLA findPath(int length, int scanLimit, IntVLA impassable,
                           IntVLA onlyPassable, int start, int... targets)
    {
        if (!initialized) return null;
        path.clear();
        IntVLA impassable2;
        if (impassable == null)
            impassable2 = new IntVLA();
        else {
            impassable2 = new IntVLA(impassable);
            impassable2.removeValue(start);
        }
        if (onlyPassable == null)
            onlyPassable = new IntVLA();

        resetMap();
        for (int g = 0; g < targets.length; g++) {
            setGoal(targets[g]);
        }
        if (goals.size == 0)
            return new IntVLA(path);
        Adjacency adjacency = this.adjacency;
        int[][] toNeighbors = neighbors[1];
        if(length < 0)
            length = 0;
        if(scanLimit <= 0 || scanLimit < length)
            scanInternal(start, impassable2.items, impassable2.size);
        else
            partialScanInternal(start, scanLimit, impassable2.items, impassable2.size);

        int currentPos = start, pt;
        int paidLength = 0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos];
            rng.randomOrdering(adjacency.maxAdjacent, reuse);
            int choice = rng.nextInt(adjacency.maxAdjacent);

            for (int d = 0; d < adjacency.maxAdjacent; d++) {
                pt = toNeighbors[reuse[d]][currentPos];
                if(adjacency.twoStepRule)
                    pt = toNeighbors[reuse[d]][pt];
                if (gradientMap[pt] < best && !path.contains(pt)) {
                    best = gradientMap[pt];
                    choice = reuse[d];// adjacency.invertAdjacent[reuse[d]];
                }
            }


            if (best >= gradientMap[currentPos] || physicalMap[toNeighbors[choice][currentPos]] > FLOOR) {
                break;
            }
            currentPos = toNeighbors[choice][pt = currentPos];
            if(adjacency.twoStepRule)
                currentPos = toNeighbors[choice][currentPos];
            path.add(currentPos);
            paidLength += adjacency.costRules.get(costMap[currentPos] | (adjacency.extractR(pt) == adjacency.extractR(currentPos) ? 0 : 0x10000));
            frustration++;
            if (paidLength > length - 1.0) {
                if (onlyPassable.contains(currentPos)) {
                    setOccupied(currentPos);
                    impassable2.add(currentPos);
                    return findPath(length, scanLimit, impassable2, onlyPassable, start, targets);
                }
                break;
            }
            if (gradientMap[currentPos] == 0)
                break;
        }
        frustration = 0;
        goals.clear();
        return new IntVLA(path);
    }


    // TODO: Tackle these next two once there's a CustomLOS class
    /*
     * Scans the dungeon using CustomDijkstraMap.scan with the listed goals and start point, and returns a list
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
     * /
    public ArrayList<Coord> findAttackPath(int moveLength, int preferredRange, LOS los, Set<Coord> impassable,
                                           Set<Coord> onlyPassable, Coord start, Coord... targets) {
        return findAttackPath(moveLength, preferredRange, preferredRange, los, impassable, onlyPassable, start, targets);
    }
    */
    /*
     * Scans the dungeon using CustomDijkstraMap.scan with the listed goals and start point, and returns a list
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
     * /
    public ArrayList<Coord> findAttackPath(int moveLength, int minPreferredRange, int maxPreferredRange, LOS los,
                                           Set<Coord> impassable, Set<Coord> onlyPassable, Coord start, Coord... targets) {
        if (!initialized) return null;
        if (minPreferredRange < 0) minPreferredRange = 0;
        if (maxPreferredRange < minPreferredRange) maxPreferredRange = minPreferredRange;
        int[][] resMap = new int[width][height];
        if (los != null) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    resMap[x][y] = (physicalMap[x][y] == WALL) ? 1.0 : 0;
                }
            }
        }
        path.clear();
        OrderedSet<Coord> impassable2;
        if (impassable == null)
            impassable2 = new OrderedSet<>();
        else
            impassable2 = new OrderedSet<>(impassable);
        if (onlyPassable == null)
            onlyPassable = new OrderedSet<>();

        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty())
            return new ArrayList<>(path);

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
        int paidLength = 0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            int best = gradientMap[currentPos.x][currentPos.y];
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(dirs.length);

            for (int d = 0; d < dirs.length; d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (gradientMap[pt.x][pt.y] < best) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                path.clear();
                break;
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.add(Coord.get(currentPos.x, currentPos.y));
            paidLength += costMap[currentPos.x][currentPos.y];
            frustration++;
            if (paidLength > moveLength - 1) {

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
        frustration = 0;
        goals.clear();
        return new ArrayList<>(path);
    }
    */


    private double cachedLongerPaths = 1.2;
    private long cachedImpassable = 0L, cachedFearSources = 0L;
    private double[] cachedFleeMap;
    private int cachedSize = 1;

    /**
     * Scans the dungeon using CustomDijkstraMap.scan with the listed fearSources and start point, and returns a list
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
    public IntVLA findFleePath(int length, double preferLongerPaths, IntVLA impassable,
                                         IntVLA onlyPassable, int start, int... fearSources) {
        return findFleePath(length, -1, preferLongerPaths, impassable, onlyPassable, start, fearSources);
    }
    /**
     * Scans the dungeon using CustomDijkstraMap.scan with the listed fearSources and start point, and returns a list
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
     * @param scanLimit         how many steps away from a fear source to calculate; negative scans the whole map
     * @param preferLongerPaths Set this to 1.2 if you aren't sure; it will probably need tweaking for different maps.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param fearSources       a vararg or array of Coord positions to run away from
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes away from fear sources. Copy of path.
     */
    public IntVLA findFleePath(int length, int scanLimit, double preferLongerPaths, IntVLA impassable,
                               IntVLA onlyPassable, int start, int... fearSources) {

        if (!initialized) return null;
        path.clear();
        IntVLA impassable2;
        if (impassable == null)
            impassable2 = new IntVLA();
        else
            impassable2 = new IntVLA(impassable);

        if (onlyPassable == null)
            onlyPassable = new IntVLA();
        if (fearSources == null || fearSources.length < 1) {
            path.clear();
            return new IntVLA(1);
        }
        if (cachedSize == 1 && preferLongerPaths == cachedLongerPaths && impassable2.hash64() == cachedImpassable &&
                CrossHash.hash64(fearSources) == cachedFearSources) {
            gradientMap = cachedFleeMap;
        } else {
            cachedLongerPaths = preferLongerPaths;
            cachedImpassable = impassable2.hash64();
            cachedFearSources = CrossHash.hash64(fearSources);
            cachedSize = 1;
            resetMap();
            for (int g = 0; g < fearSources.length; g++) {
                setGoal(fearSources[g]);
            }
            if (goals.size == 0)
                return new IntVLA(path);
            if(length < 0) length = 0;
            if(scanLimit <= 0 || scanLimit < length)
                cachedFleeMap = scan(impassable2);
            else
                cachedFleeMap = partialScan(scanLimit, impassable2);

            for (int l = 0; l < gradientMap.length; l++) {
                gradientMap[l] *= (gradientMap[l] >= FLOOR) ? 1 : - preferLongerPaths;
            }
            if(scanLimit <= 0 || scanLimit < length)
                cachedFleeMap = scan(impassable2);
            else
                cachedFleeMap = partialScan(scanLimit, impassable2);

        }
        Adjacency adjacency = this.adjacency;
        int[][] toNeighbors = neighbors[1];
        int currentPos = start, pt;
        int paidLength = 0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos];
            rng.randomOrdering(adjacency.maxAdjacent, reuse);
            int choice = rng.nextInt(adjacency.maxAdjacent);

            for (int d = 0; d < adjacency.maxAdjacent; d++) {
                pt = toNeighbors[reuse[d]][currentPos];
                if (gradientMap[pt] < best && !path.contains(pt)) {
                    best = gradientMap[pt];
                    choice = reuse[d];
                }
            }


            if (best >= gradientMap[currentPos] || physicalMap[toNeighbors[choice][currentPos]] > FLOOR) {
                path.clear();
                break;
            }
            currentPos = toNeighbors[choice][pt = currentPos];
            path.add(currentPos);
            paidLength += adjacency.costRules.get(costMap[currentPos] | (adjacency.extractR(pt) == adjacency.extractR(currentPos) ? 0 : 0x10000));
            frustration++;
            if (paidLength > length - 1) {
                if (onlyPassable.contains(currentPos)) {
                    setOccupied(currentPos);
                    impassable2.add(currentPos);
                    return findFleePath(length, scanLimit, preferLongerPaths, impassable2, onlyPassable, start, fearSources);
                }
                break;
            }
        }
        frustration = 0;
        goals.clear();
        return new IntVLA(path);
    }

    /**
     * Scans the dungeon using CustomDijkstraMap.scan with the listed goals and start point, and returns a list
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
    public IntVLA findPathLarge (int size, int length, IntVLA impassable,
                                 IntVLA onlyPassable, int start, int... targets) {
        return findPathLarge(size, length, -1, impassable, onlyPassable, start, targets);
    }
    /**
     * Scans the dungeon using CustomDijkstraMap.scanLarge with the listed goals and start point, and returns a list
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
     * @param scanLimit    how many steps away from a goal to calculate; negative scans the whole map
     * @param impassable   a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start        the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets      a vararg or array of Coord that this will try to pathfind toward
     * @return an ArrayList of Coord that will contain the min-x, min-y locations of this creature as it goes toward a target. Copy of path.
     */
    public IntVLA findPathLarge (int size, int length, int scanLimit, IntVLA impassable,
                                 IntVLA onlyPassable, int start, int... targets) {
        if (!initialized) return null;
        path.clear();
        IntVLA impassable2;
        if (impassable == null)
            impassable2 = new IntVLA();
        else
            impassable2 = new IntVLA(impassable);
        if (onlyPassable == null)
            onlyPassable = new IntVLA();

        resetMap();
        for (int g = 0; g < targets.length; g++) {
            setGoal(targets[g]);
        }
        if (goals.size == 0)
            return new IntVLA(path);
        Adjacency adjacency = this.adjacency;
        int[][] toNeighbors = neighbors[1];
        if(length < 0)
            length = 0;
        if(scanLimit <= 0 || scanLimit < length)
            scanLargeInternal(start, size, impassable2.items, impassable2.size);
        else
            partialScanLargeInternal(start, size, scanLimit, impassable2.items, impassable2.size);

        int currentPos = start, pt;
        int paidLength = 0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos];
            rng.randomOrdering(adjacency.maxAdjacent, reuse);
            int choice = rng.nextInt(adjacency.maxAdjacent);

            for (int d = 0; d < adjacency.maxAdjacent; d++) {
                pt = toNeighbors[reuse[d]][currentPos];
                if (gradientMap[pt] < best && !path.contains(pt)) {
                    best = gradientMap[pt];
                    choice = reuse[d];
                }
            }


            if (best >= gradientMap[currentPos] || physicalMap[toNeighbors[choice][currentPos]] > FLOOR) {
                path.clear();
                break;
            }
            currentPos = toNeighbors[choice][pt = currentPos];
            path.add(currentPos);
            paidLength += adjacency.costRules.get(costMap[currentPos] | (adjacency.extractR(pt) == adjacency.extractR(currentPos) ? 0 : 0x10000));
            frustration++;
            if (paidLength > length - 1) {
                if (onlyPassable.contains(currentPos)) {
                    setOccupied(currentPos);
                    impassable2.add(currentPos);
                    return findPathLarge(size, scanLimit, length, impassable2, onlyPassable, start, targets);
                }
                break;
            }
            if (gradientMap[currentPos] == 0)
                break;
        }
        frustration = 0;
        goals.clear();
        return new IntVLA(path);
    }

    /*
    public ArrayList<Coord> findPathLarge(int size, int length, Set<Coord> impassable,
                                          Set<Coord> onlyPassable, Coord start, Coord... targets) {
        if (!initialized) return null;
        path.clear();
        OrderedSet<Coord> impassable2;
        if (impassable == null)
            impassable2 = new OrderedSet<>();
        else
            impassable2 = new OrderedSet<>(impassable);

        if (onlyPassable == null)
            onlyPassable = new OrderedSet<>();

        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty())
            return new ArrayList<>(path);

        scan(impassable2, size);
        Coord currentPos = start;
        int paidLength = 0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            int best = gradientMap[currentPos.x][currentPos.y];
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(dirs.length);

            for (int d = 0; d < dirs.length; d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (gradientMap[pt.x][pt.y] < best) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                path.clear();
                break;
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);

            path.add(currentPos);
            paidLength += costMap[currentPos.x][currentPos.y];
            frustration++;
            if (paidLength > length - 1) {
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
        frustration = 0;
        goals.clear();
        return new ArrayList<>(path);
    }
    */
    // TODO: Again, this needs CustomLOS
    /*
     * Scans the dungeon using CustomDijkstraMap.scan with the listed goals and start point, and returns a list
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
     * /
    public ArrayList<Coord> findAttackPathLarge(int size, int moveLength, int preferredRange, LOS los, Set<Coord> impassable,
                                                Set<Coord> onlyPassable, Coord start, Coord... targets) {
        if (!initialized) return null;
        if (preferredRange < 0) preferredRange = 0;
        int[][] resMap = new int[width][height];
        if (los != null) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    resMap[x][y] = (physicalMap[x][y] == WALL) ? 1 : 0;
                }
            }
        }
        path.clear();
        OrderedSet<Coord> impassable2;
        if (impassable == null)
            impassable2 = new OrderedSet<>();
        else
            impassable2 = new OrderedSet<>(impassable);

        if (onlyPassable == null)
            onlyPassable = new OrderedSet<>();

        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty())
            return new ArrayList<>(path);

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
        int paidLength = 0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            int best = gradientMap[currentPos.x][currentPos.y];
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(dirs.length);

            for (int d = 0; d < dirs.length; d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (gradientMap[pt.x][pt.y] < best) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                path.clear();
                break;
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.add(currentPos);
            frustration++;
            paidLength += costMap[currentPos.x][currentPos.y];
            if (paidLength > moveLength - 1) {
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
        frustration = 0;
        goals.clear();
        return new ArrayList<>(path);
    }

    /*
     * Scans the dungeon using CustomDijkstraMap.scan with the listed goals and start point, and returns a list
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
     * /
    public ArrayList<Coord> findAttackPathLarge(int size, int moveLength, int minPreferredRange, int maxPreferredRange, LOS los,
                                                Set<Coord> impassable, Set<Coord> onlyPassable, Coord start, Coord... targets) {
        if (!initialized) return null;
        if (minPreferredRange < 0) minPreferredRange = 0;
        if (maxPreferredRange < minPreferredRange) maxPreferredRange = minPreferredRange;
        int[][] resMap = new int[width][height];
        if (los != null) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    resMap[x][y] = (physicalMap[x][y] == WALL) ? 1 : 0;
                }
            }
        }
        path.clear();
        OrderedSet<Coord> impassable2;
        if (impassable == null)
            impassable2 = new OrderedSet<>();
        else
            impassable2 = new OrderedSet<>(impassable);

        if (onlyPassable == null)
            onlyPassable = new OrderedSet<>();

        resetMap();
        for (Coord goal : targets) {
            setGoal(goal);
        }
        if (goals.isEmpty())
            return new ArrayList<>(path);

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
        int paidLength = 0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }

            int best = gradientMap[currentPos.x][currentPos.y];
            final Direction[] dirs = appendDirToShuffle(rng);
            int choice = rng.nextInt(dirs.length);

            for (int d = 0; d < dirs.length; d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (gradientMap[pt.x][pt.y] < best) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }
            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                path.clear();
                break;
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);

            path.add(currentPos);
            frustration++;
            paidLength += costMap[currentPos.x][currentPos.y];
            if (paidLength > moveLength - 1) {
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
        frustration = 0;
        goals.clear();
        return new ArrayList<>(path);
    }
    */
    /**
     * Scans the dungeon using CustomDijkstraMap.scanLarge with the listed fearSources and start point, and returns a list
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
    public IntVLA findFleePathLarge(int size, int length, int preferLongerPaths, IntVLA impassable,
                                    IntVLA onlyPassable, int start, int... fearSources) {
        return findFleePathLarge(size, length, -1, preferLongerPaths, impassable, onlyPassable, start, fearSources);
    }


    /**
     * Scans the dungeon using CustomDijkstraMap.scanLarge with the listed fearSources and start point, and returns a list
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
     * @param scanLimit         how many steps away from a goal to calculate; negative scans the whole map
     * @param preferLongerPaths Set this to 1.2 if you aren't sure; it will probably need tweaking for different maps.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param fearSources       a vararg or array of Coord positions to run away from
     * @return an ArrayList of Coord that will contain the locations of this creature as it goes away from fear sources. Copy of path.
     */
    public IntVLA findFleePathLarge(int size, int length, int scanLimit, int preferLongerPaths, IntVLA impassable,
                                    IntVLA onlyPassable, int start, int... fearSources) {
        if (!initialized) return null;
        path.clear();
        IntVLA impassable2;
        if (impassable == null)
            impassable2 = new IntVLA();
        else
            impassable2 = new IntVLA(impassable);

        if (onlyPassable == null)
            onlyPassable = new IntVLA();
        if (fearSources == null || fearSources.length < 1) {
            path.clear();
            return new IntVLA(1);
        }
        if (cachedSize == size && preferLongerPaths == cachedLongerPaths && impassable2.hash64() == cachedImpassable &&
                CrossHash.hash64(fearSources) == cachedFearSources) {
            gradientMap = cachedFleeMap;
        } else {
            cachedLongerPaths = preferLongerPaths;
            cachedImpassable = impassable2.hash64();
            cachedFearSources = CrossHash.hash64(fearSources);
            cachedSize = size;
            resetMap();
            for (int g = 0; g < fearSources.length; g++) {
                setGoal(fearSources[g]);
            }
            if (goals.size == 0)
                return new IntVLA(path);

            if(scanLimit <= 0 || scanLimit < length)
                cachedFleeMap = scanLarge(size, impassable2);
            else
                cachedFleeMap = partialScanLarge(size, scanLimit, impassable2);

            for (int l = 0; l < gradientMap.length; l++) {
                gradientMap[l] *= (gradientMap[l] >= FLOOR) ? 1 : -preferLongerPaths;
            }
            if(scanLimit <= 0 || scanLimit < length)
                cachedFleeMap = scanLarge(size, impassable2);
            else
                cachedFleeMap = partialScanLarge(size, scanLimit, impassable2);
        }
        Adjacency adjacency = this.adjacency;
        int[][] toNeighbors = neighbors[1];
        int currentPos = start, pt;
        int paidLength = 0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos];
            rng.randomOrdering(adjacency.maxAdjacent, reuse);
            int choice = rng.nextInt(adjacency.maxAdjacent);

            for (int d = 0; d < adjacency.maxAdjacent; d++) {
                pt = toNeighbors[reuse[d]][currentPos];
                if (gradientMap[pt] < best && !path.contains(pt)) {
                    best = gradientMap[pt];
                    choice = reuse[d];
                }
            }


            if (best >= gradientMap[currentPos] || physicalMap[toNeighbors[choice][currentPos]] > FLOOR) {
                path.clear();
                break;
            }
            currentPos = toNeighbors[choice][pt = currentPos];
            path.add(currentPos);
            paidLength += adjacency.costRules.get(costMap[currentPos] | (adjacency.extractR(pt) == adjacency.extractR(currentPos) ? 0 : 0x10000));
            frustration++;
            if (paidLength > length - 1) {
                if (onlyPassable.contains(currentPos)) {
                    setOccupied(currentPos);
                    impassable2.add(currentPos);
                    return findFleePathLarge(size, scanLimit, length, preferLongerPaths, impassable2, onlyPassable, start, fearSources);
                }
                break;
            }
        }
        frustration = 0;
        goals.clear();
        return new IntVLA(path);
    }

    /*
     * Intended primarily for internal use. Needs scan() to already be called and at least one goal to already be set,
     * and does not restrict the length of the path or behave as if the pathfinder has allies or enemies.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param target the target cell
     * @return an ArrayList of Coord that make up the best path. Copy of path.
     * /
    public ArrayList<Coord> findPathPreScanned(Coord target) {
        if (!initialized || goals == null || goals.size == 0) return null;
        RNG rng2 = new StatefulRNG(new LightRNG(0xf00d));
        path.clear();
        Coord currentPos = target;
        while (true) {
            if (frustration > 2000) {
                path.clear();
                break;
            }
            int best = gradientMap[currentPos.x][currentPos.y];
            final Direction[] dirs = appendDirToShuffle(rng2);
            int choice = rng2.nextInt(dirs.length);

            for (int d = 0; d < dirs.length; d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (gradientMap[pt.x][pt.y] < best) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                path.clear();
                break;
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.add(0, currentPos);
            frustration++;

            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        frustration = 0;
        return new ArrayList<>(path);
    }
     */
    /**
     * A simple limited flood-fill that returns a OrderedMap of Coord keys to the Double values in the CustomDijkstraMap, only
     * calculating out to a number of steps determined by limit. This can be useful if you need many flood-fills and
     * don't need a large area for each, or if you want to have an effect spread to a certain number of cells away.
     *
     * @param radius the number of steps to take outward from each starting position.
     * @param starts a vararg group of Points to step outward from; this often will only need to be one Coord.
     * @return A OrderedMap of Coord keys to Double values; the starts are included in this with the value 0.
     */
    public IntDoubleOrderedMap floodFill(int radius, int... starts) {
        if (!initialized || starts == null) return null;
        IntDoubleOrderedMap fill = new IntDoubleOrderedMap();

        resetMap();
        for (int g = 0; g < starts.length; g++) {
            setGoal(starts[g]);
        }

        if (goals.size == 0)
            return fill;

        partialScan(radius, -1, null);
        double temp;
        for (int l = 0; l < gradientMap.length; l++) {
            temp = gradientMap[l];
            if (temp < FLOOR) {
                fill.put(l, temp);
            }
        }
        goals.clear();
        return fill;
    }

    public int getMappedCount() {
        return mappedCount;
    }


    /*
    public static void main(String[] args) {
        squidpony.squidgrid.mapping.DungeonGenerator dungeonGen =
                new squidpony.squidgrid.mapping.DungeonGenerator(40, 40, new RNG(0x1337BEEFDEAL));
        char[][] map = dungeonGen.generate();
        squidpony.squidgrid.mapping.DungeonUtility.debugPrint(map);
        squidpony.squidmath.GreasedRegion floors = new squidpony.squidmath.GreasedRegion(map, '.');
        System.out.println("Floors: " + floors.size());
        System.out.println("Percentage walkable: " + floors.size() / 16.0 + "%");
        Adjacency adj = new BasicAdjacency(40, 40, Measurement.EUCLIDEAN);
        adj.blockingRule = 2;
        RNG rng = new RNG(0x1337BEEF);
        CustomDijkstraMap dijkstra = new CustomDijkstraMap(
                map, adj, rng);
        double[] scanned;
        short[][] sMap = new short[40][40];
        //for (int x = 1; x < 39; x++) {
        //    for (int y = 1; y < 39; y++) {
        squidpony.squidmath.Coord c = floors.singleRandom(rng);
        dijkstra.setGoal(adj.composite(c.x, c.y, 0, 0));
        scanned = dijkstra.scan(null);
        dijkstra.clearGoals();
        dijkstra.resetMap();
        System.out.println("MAPPED: " + dijkstra.getMappedCount());
        for (int i = 0; i < 1600; i++) {
            sMap[adj.extractX(i)][adj.extractY(i)] = (scanned[i] >= FLOOR ? -999 : (short) (scanned[i] * 100));
        }
        for (int yy = 0; yy < 40; yy++) {
            for (int xx = 0; xx < 40; xx++) {
                System.out.print((sMap[xx][yy] == -999) ? "#### " : squidpony.StringKit.hex(sMap[xx][yy]) + ' ');
            }
            System.out.println();
        }
        //    }
        //}
    }
    */
}
