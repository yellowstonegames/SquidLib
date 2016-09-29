package squidpony.squidai;

import squidpony.annotation.Beta;
import squidpony.squidai.DijkstraMap.Measurement;
import squidpony.squidgrid.Adjacency;
import squidpony.squidgrid.Adjacency.BasicAdjacency;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.IntDoubleOrderedMap;
import squidpony.squidmath.IntVLA;
import squidpony.squidmath.RNG;

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
     * This stores the entry cost multipliers for each cell; that is, a value of 1.0 is a normal, unmodified cell, but
     * a value of 0.5 can be entered easily (two cells of its cost can be entered for the cost of one 1.0 cell), and a
     * value of 2.0 can only be entered with difficulty (one cell of its cost can be entered for the cost of two 1.0
     * cells). Unlike the measurement field, this does affect the length of paths, as well as the numbers assigned
     * to gradientMap during a scan. The values for walls are identical to the value used by gradientMap, that is, this
     * class' WALL static final field. Floors, however, are never given FLOOR as a value, and default to 1.0 .
     */
    public int[] costMap = null;

    /**
     * The neighbors map, as produced by adjacency; can be modified by passing neighbors as the first argument to
     * {@link Adjacency#portal(int[][], int, int, boolean)} if you want to create portals between non-adjacent cells.
     */
    public int[][] neighbors;
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
    public IntDoubleOrderedMap goals;
    private IntDoubleOrderedMap fresh, closed, open;
    /**
     * The RNG used to decide which one of multiple equally-short paths to take.
     */
    public RNG rng;
    private int frustration = 0;

    private int[] reuse = new int[9];

    private boolean initialized = false;

    private int mappedCount = 0;

    /**
     * Construct a CustomDijkstraMap without a level to actually scan. If you use this constructor, you must call an
     * initialize() method before using this class.
     */
    public CustomDijkstraMap() {
        rng = new RNG();
        path = new IntVLA();

        goals = new IntDoubleOrderedMap();
        fresh = new IntDoubleOrderedMap();
        closed = new IntDoubleOrderedMap();
        open = new IntDoubleOrderedMap();
    }

    /**
     * Construct a CustomDijkstraMap without a level to actually scan. This constructor allows you to specify an RNG before
     * it is ever used in this class. If you use this constructor, you must call an initialize() method before using
     * any other methods in the class.
     */
    public CustomDijkstraMap(RNG random) {
        rng = random;
        path = new IntVLA();

        goals = new IntDoubleOrderedMap();
        fresh = new IntDoubleOrderedMap();
        closed = new IntDoubleOrderedMap();
        open = new IntDoubleOrderedMap();
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

        goals = new IntDoubleOrderedMap();
        fresh = new IntDoubleOrderedMap();
        closed = new IntDoubleOrderedMap();
        open = new IntDoubleOrderedMap();
        initialize(level);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonGen.generate(), or any other
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
     * Constructor meant to take a char[][] returned by DungeonGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. Also takes an RNG that ensures predictable path choices given
     * otherwise identical inputs and circumstances.
     *
     * @param level
     * @param rng   The RNG to use for certain decisions; only affects find* methods like findPath, not scan.
     */
    public CustomDijkstraMap(final char[][] level, RNG rng) {
        this(level, new BasicAdjacency(level.length, level[0].length, Measurement.MANHATTAN), rng);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonGen.generate(), or any other
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

        goals = new IntDoubleOrderedMap();
        fresh = new IntDoubleOrderedMap();
        closed = new IntDoubleOrderedMap();
        open = new IntDoubleOrderedMap();
        initialize(level, alternateWall);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonGen.generate(), or any other
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
     * Constructor meant to take a char[][] returned by DungeonGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. Also takes a distance measurement and an RNG that ensures
     * predictable path choices given otherwise identical inputs and circumstances.
     *
     * @param level
     * @param rng   The RNG to use for certain decisions; only affects find* methods like findPath, not scan.
     */
    public CustomDijkstraMap(final char[][] level, Adjacency adjacency, RNG rng) {
        this.rng = rng;
        path = new IntVLA();
        this.adjacency = adjacency;

        goals = new IntDoubleOrderedMap();
        fresh = new IntDoubleOrderedMap();
        closed = new IntDoubleOrderedMap();
        open = new IntDoubleOrderedMap();
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
        int rot = adjacency.rotations, dex;
        gradientMap = new double[width*height*rot];
        physicalMap = new double[width*height*rot];
        costMap = new int[width*height*rot];
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
        path.clear();
        closed.clear();
        fresh.clear();
        open.clear();
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
        adjacency.putAllVariants(goals, pt, GOAL);
    }

    /**
     * Marks a cell's type for pathfinding cost as tile (it still will look up the tile in the
     * {@link Adjacency#costRules} field of {@link #adjacency} when it tries to move through one), unless the cell is a
     * wall or unreachable area (then it always sets the cost to a value that should have the same cost as a wall).
     * @param pt
     * @param tile
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
        IntDoubleOrderedMap.KeyIterator ki = goals.keySet().iterator();
        while(ki.hasNext())
            resetCell(ki.nextInt());
        goals.clear();
    }

    protected void setFresh(final int pt, double counter) {
        if (!initialized || !adjacency.validate(pt)) return;
        gradientMap[pt] = counter;
        fresh.put(pt, counter);
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
     * @param impassable A Set of Position keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D double[width][height] using the width and height of what this knows about the physical map.
     */
    public double[] scan(int[] impassable) {
        if (!initialized) return null;
        Adjacency adjacency = this.adjacency;

        if (impassable != null) {
            for (int i = 0; i <impassable.length; i++) {
                adjacency.putAllVariants(closed, impassable[i], WALL);
            }
        }
        int[][] neighbors = this.neighbors;
        int near, cen, neighborCount = neighbors.length, mid;

        for (IntDoubleOrderedMap.MapEntry entry : goals.mapEntrySet()) {
            closed.remove(entry.getIntKey());
            gradientMap[entry.getIntKey()] = entry.getDoubleValue();
        }
        double currentLowest = 999000, cs;
        IntDoubleOrderedMap lowest = new IntDoubleOrderedMap();
        int maxLength = gradientMap.length;
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] > FLOOR && !goals.containsKey(l))
                closed.put(l, physicalMap[l]);
            else if (gradientMap[l] < currentLowest) {
                currentLowest = gradientMap[l];
                lowest.clear();
                lowest.put(l, currentLowest);
            } else if (gradientMap[l] == currentLowest) {
                lowest.put(l, currentLowest);
            }
        }
        int numAssigned = lowest.size();
        mappedCount = goals.size();
        open.putAll(lowest);
        lowest = adjacency.costRules;
        while (numAssigned > 0) {
            numAssigned = 0;

            for (IntDoubleOrderedMap.MapEntry cell : open.mapEntrySet()) {
                cen = cell.getIntKey();
                for (int d = 0; d < neighborCount; d++) {
                    near = neighbors[d][cen];
                    if (!adjacency.validate(near))
                    	// Outside the map
                    	continue;
                    if(adjacency.isBlocked(cen, d, neighbors, gradientMap, WALL))
                        continue;
                    if(adjacency.twoStepRule) {
                        near = neighbors[d][mid = near];
                        // Outside the map
                        if (!adjacency.validate(near))
                            continue;
                        if(adjacency.isBlocked(mid, d, neighbors, gradientMap, WALL))
                            continue;
                        cs = lowest.get(costMap[near]);
                        if (!closed.containsKey(near) && !open.containsKey(near) && gradientMap[mid] + cs < gradientMap[near]) {
                            setFresh(near, cell.getDoubleValue() + cs);
                            ++numAssigned;
                            ++mappedCount;
                        }
                    }
                    else
                    {
                        cs = lowest.get(costMap[near] | ((adjacency.extractR(cen) == adjacency.extractR(near) ? 0 : 0x10000)));
                        //double h = adjacency.measurement.heuristic(adjacency.directions[d]);
                        if (!closed.containsKey(near) && !open.containsKey(near) && gradientMap[cen] + cs < gradientMap[near]) {
                            setFresh(near, cell.getDoubleValue() + cs);
                            ++numAssigned;
                            ++mappedCount;
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
     * @param impassable A Set of Position keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D double[width][height] using the width and height of what this knows about the physical map.
     */
    public double[] partialScan(int limit, int... impassable) {
        if (!initialized) return null;
        Adjacency adjacency = this.adjacency;

        if (impassable != null) {
            for (int i = 0; i <impassable.length; i++) {
                adjacency.putAllVariants(closed, impassable[i], WALL);
            }
        }
        int near, cen, neighborCount = neighbors.length, mid;

        for (IntDoubleOrderedMap.MapEntry entry : goals.mapEntrySet()) {
            closed.remove(entry.getIntKey());
            gradientMap[entry.getIntKey()] = entry.getDoubleValue();
        }
        double currentLowest = 999000, cs;
        IntDoubleOrderedMap lowest = new IntDoubleOrderedMap();
        int maxLength = gradientMap.length;
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] > FLOOR && !goals.containsKey(l))
                closed.put(l, physicalMap[l]);
            else if (gradientMap[l] < currentLowest) {
                currentLowest = gradientMap[l];
                lowest.clear();
                lowest.put(l, currentLowest);
            } else if (gradientMap[l] == currentLowest) {
                lowest.put(l, currentLowest);
            }
        }
        int numAssigned = lowest.size();
        mappedCount = goals.size();
        open.putAll(lowest);
        lowest = adjacency.costRules;
        int iter = 0;
        while (numAssigned > 0 && iter < limit) {
            numAssigned = 0;

            for (IntDoubleOrderedMap.MapEntry cell : open.mapEntrySet()) {
                cen = cell.getIntKey();
                for (int d = 0; d < neighborCount; d++) {
                    near = neighbors[d][cen];
                    if (!adjacency.validate(near))
                        // Outside the map
                        continue;
                    if(adjacency.isBlocked(cen, d, neighbors, gradientMap, WALL))
                        continue;
                    if(adjacency.twoStepRule) {
                        near = neighbors[d][mid = near];
                        if (!adjacency.validate(near))
                            // Outside the map
                            continue;
                        if(adjacency.isBlocked(mid, d, neighbors, gradientMap, WALL))
                            continue;
                        cs = lowest.get(costMap[near]);
                        if (!closed.containsKey(near) && !open.containsKey(near) && gradientMap[mid] + cs < gradientMap[near]) {
                            setFresh(near, cell.getDoubleValue() + cs);
                            ++numAssigned;
                            ++mappedCount;
                        }
                    }
                    else
                    {
                        cs = lowest.get(costMap[near] | ((adjacency.extractR(cen) == adjacency.extractR(near) ? 0 : 0x10000)));
                        //double h = adjacency.measurement.heuristic(adjacency.directions[d]);
                        if (!closed.containsKey(near) && !open.containsKey(near) && gradientMap[cen] + cs < gradientMap[near]) {
                            setFresh(near, cell.getDoubleValue() + cs);
                            ++numAssigned;
                            ++mappedCount;
                        }
                    }
                }
            }
            open.clear();
            open.putAll(fresh);
            fresh.clear();
            iter++;
        }
        closed.clear();
        open.clear();

        double[] gradientClone = new double[maxLength];
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] == FLOOR) {
                gradientMap[l] = DARK;
            }
        }
        System.arraycopy(gradientMap, 0, gradientClone, 0, maxLength);
        return gradientClone;
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
                    double h = adjacency.measurement.heuristic(dir);
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
                    double h = heuristic(dirs[d]);
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
            double best = gradientMap[currentPos.x][currentPos.y];
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
                    	// Outside the map
                        continue;
                    enc = adj.encode();

                    double h = heuristic(dirs[d]);
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
     * @param impassable A Set of Position keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @return A 2D double[width][height] using the width and height of what this knows about the physical map.
     */
    public double[] scan(int size, int[] impassable) {
        if (!initialized) return null;

        int near, cen, neighborCount = neighbors.length, mid, tmp, tmp2, xStore, yStore, rStore, nStore;
        double valStore;
        Adjacency adjacency = this.adjacency;

        if (impassable != null) {
            for (int i = 0; i <impassable.length; i++) {
                adjacency.putAllVariants(closed, impassable[i], WALL, -size);
            }
        }
        mappedCount = 0;

        for (IntDoubleOrderedMap.MapEntry entry : goals.mapEntrySet()) {
            tmp = entry.getIntKey();
            xStore = adjacency.extractX(tmp);
            yStore = adjacency.extractY(tmp);
            rStore = adjacency.extractR(tmp);
            nStore = adjacency.extractN(tmp);
            valStore = entry.getDoubleValue();
            for (int a = 0; a < size; a++) {
                for (int b = 0; b < size; b++) {
                    tmp2 = adjacency.composite(xStore - a, yStore - b, rStore, nStore);
                    if (tmp2 >= 0) {
                        closed.remove(tmp2);
                        gradientMap[tmp2] = valStore;
                        mappedCount++;
                    }
                }
            }
        }
        double currentLowest = 999000, cs;
        IntDoubleOrderedMap lowest = new IntDoubleOrderedMap();
        int maxLength = gradientMap.length;
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] > FLOOR && !goals.containsKey(l)) {
                xStore = adjacency.extractX(l);
                yStore = adjacency.extractY(l);
                rStore = adjacency.extractR(l);
                nStore = adjacency.extractN(l);
                for (int a = 0; a < size; a++) {
                    for (int b = 0; b < size; b++) {
                        tmp2 = adjacency.composite(xStore - a, yStore - b, rStore, nStore);
                        if (tmp2 >= 0)
                            closed.put(tmp2, physicalMap[l]);
                    }
                }
            }
            else if (gradientMap[l] < currentLowest) {
                currentLowest = gradientMap[l];
                lowest.clear();
                lowest.put(l, currentLowest);
            } else if (gradientMap[l] == currentLowest) {
                lowest.put(l, currentLowest);
            }
        }
        int numAssigned = lowest.size();
        open.putAll(lowest);
        lowest = adjacency.costRules;
        while (numAssigned > 0) {
            numAssigned = 0;

            for (IntDoubleOrderedMap.MapEntry cell : open.mapEntrySet()) {
                cen = cell.getIntKey();
                for (int d = 0; d < neighborCount; d++) {
                    near = neighbors[d][cen];
                    if (!adjacency.validate(near))
                        // Outside the map
                        continue;
                    //if(adjacency.isBlocked(cen, d, neighbors, gradientMap, WALL))
                    //    continue;
                    if(adjacency.twoStepRule) {
                        near = neighbors[d][mid = near];
                        if (!adjacency.validate(near))
                            // Outside the map
                            continue;
                        //if(adjacency.isBlocked(mid, d, neighbors, gradientMap, WALL))
                        //    continue;
                        cs = lowest.get(costMap[near]);
                        if (!closed.containsKey(near) && !open.containsKey(near) && gradientMap[mid] + cs < gradientMap[near]) {
                            setFresh(near, cell.getDoubleValue() + cs);
                            ++numAssigned;
                            ++mappedCount;
                        }
                    }
                    else
                    {
                        cs = lowest.get(costMap[near] | ((adjacency.extractR(cen) == adjacency.extractR(near) ? 0 : 0x10000)));
                        //double h = adjacency.measurement.heuristic(adjacency.directions[d]);
                        if (!closed.containsKey(near) && !open.containsKey(near) && gradientMap[cen] + cs < gradientMap[near]) {
                            setFresh(near, cell.getDoubleValue() + cs);
                            ++numAssigned;
                            ++mappedCount;
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

        double[] gradientClone = new double[maxLength];
        for (int l = 0; l < maxLength; l++) {
            if (gradientMap[l] == FLOOR) {
                gradientMap[l] = DARK;
            }
        }
        System.arraycopy(gradientMap, 0, gradientClone, 0, maxLength);
        return gradientClone;
    }



    /*
    public double[][] scan(Set<Coord> impassable, int size) {
        if (!initialized) return null;
        if (impassable == null)
            impassable = new OrderedSet<>();
        IntDoubleOrderedMap blocking = new IntDoubleOrderedMap(impassable.size());
        for (Coord pt : impassable) {
            blocking.put(pt.encode(), WALL);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if (x + y == 0)
                        continue;
                    if (gradientMap[pt.x - x][pt.y - y] <= FLOOR)
                        blocking.put(Coord.pureEncode(pt.x - x, pt.y - y), DARK);
                }
            }
        }
        closed.putAll(blocking);
        Coord dec, cen, adj;
        int enc;
        for (IntDoubleOrderedMap.MapEntry entry : goals.mapEntrySet()) {
            if (closed.containsKey(entry.getIntKey()))
                closed.remove(entry.getIntKey());
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
                    	// Outside the map
                        continue;
                    enc = adj.encode();
                    double h = heuristic(dirs[d]);
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
    */

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
                                     IntVLA onlyPassable, int start, int... targets) {
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
        if (goals.isEmpty())
            return new IntVLA(path);
        Adjacency adjacency = this.adjacency;
        scan(impassable2.toArray());
        int currentPos = start, pt;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos];
            rng.randomOrdering(adjacency.maxAdjacent, reuse);
            int choice = rng.nextIntHasty(adjacency.maxAdjacent);

            for (int d = 0; d < adjacency.maxAdjacent; d++) {
                pt = neighbors[reuse[d]][currentPos];
                if (gradientMap[pt] < best && !path.contains(pt)) {
                    best = gradientMap[pt];
                    choice = adjacency.invertAdjacent[reuse[d]];
                }
            }


            if (best >= gradientMap[currentPos] || physicalMap[neighbors[choice][currentPos]] > FLOOR) {
                break;
            }
            currentPos = neighbors[choice][pt = currentPos];
            path.add(currentPos);
            paidLength += adjacency.costRules.get(costMap[currentPos] | ((adjacency.extractR(pt) == adjacency.extractR(currentPos) ? 0 : 0x10000)));
            frustration++;
            if (paidLength > length - 1.0) {
                if (onlyPassable.contains(currentPos)) {
                    closed.put(currentPos, WALL);
                    impassable2.add(currentPos);
                    return findPath(length, impassable2, onlyPassable, start, targets);
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
        double[][] resMap = new double[width][height];
        if (los != null) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    resMap[x][y] = (physicalMap[x][y] == WALL) ? 1.0 : 0.0;
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
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
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
                CrossHash.Lightning.hash64(fearSources) == cachedFearSources) {
            gradientMap = cachedFleeMap;
        } else {
            cachedLongerPaths = preferLongerPaths;
            cachedImpassable = impassable2.hash64();
            cachedFearSources = CrossHash.Lightning.hash64(fearSources);
            cachedSize = 1;
            resetMap();
            for (int g = 0; g < fearSources.length; g++) {
                setGoal(fearSources[g]);
            }
            if (goals.isEmpty())
                return new IntVLA(path);
            int[] scanArray = impassable2.toArray();
            scan(scanArray);

            for (int l = 0; l < gradientMap.length; l++) {
                gradientMap[l] *= (gradientMap[l] >= FLOOR) ? 1.0 : (- preferLongerPaths);
            }
            cachedFleeMap = scan(scanArray);
        }
        Adjacency adjacency = this.adjacency;
        int currentPos = start, pt;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos];
            rng.randomOrdering(adjacency.maxAdjacent, reuse);
            int choice = rng.nextIntHasty(adjacency.maxAdjacent);

            for (int d = 0; d < adjacency.maxAdjacent; d++) {
                pt = neighbors[reuse[d]][currentPos];
                if (gradientMap[pt] < best && !path.contains(pt)) {
                    best = gradientMap[pt];
                    choice = reuse[d];
                }
            }


            if (best >= gradientMap[currentPos] || physicalMap[neighbors[choice][currentPos]] > FLOOR) {
                path.clear();
                break;
            }
            currentPos = neighbors[choice][pt = currentPos];
            path.add(currentPos);
            paidLength += adjacency.costRules.get(costMap[currentPos] | ((adjacency.extractR(pt) == adjacency.extractR(currentPos) ? 0 : 0x10000)));
            frustration++;
            if (paidLength > length - 1.0) {
                if (onlyPassable.contains(currentPos)) {
                    closed.put(currentPos, WALL);
                    impassable2.add(currentPos);
                    return findFleePath(length, preferLongerPaths, impassable2, onlyPassable, start, fearSources);
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
        if (goals.isEmpty())
            return new IntVLA(path);
        Adjacency adjacency = this.adjacency;
        scan(size, impassable2.toArray());
        int currentPos = start, pt;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos];
            rng.randomOrdering(adjacency.maxAdjacent, reuse);
            int choice = rng.nextIntHasty(adjacency.maxAdjacent);

            for (int d = 0; d < adjacency.maxAdjacent; d++) {
                pt = neighbors[reuse[d]][currentPos];
                if (gradientMap[pt] < best && !path.contains(pt)) {
                    best = gradientMap[pt];
                    choice = reuse[d];
                }
            }


            if (best >= gradientMap[currentPos] || physicalMap[neighbors[choice][currentPos]] > FLOOR) {
                path.clear();
                break;
            }
            currentPos = neighbors[choice][pt = currentPos];
            path.add(currentPos);
            paidLength += adjacency.costRules.get(costMap[currentPos] | ((adjacency.extractR(pt) == adjacency.extractR(currentPos) ? 0 : 0x10000)));
            frustration++;
            if (paidLength > length - 1.0) {
                if (onlyPassable.contains(currentPos)) {
                    closed.put(currentPos, WALL);
                    impassable2.add(currentPos);
                    return findPathLarge(size, length, impassable2, onlyPassable, start, targets);
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
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
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
        double[][] resMap = new double[width][height];
        if (los != null) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    resMap[x][y] = (physicalMap[x][y] == WALL) ? 1.0 : 0.0;
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
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
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
        double[][] resMap = new double[width][height];
        if (los != null) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    resMap[x][y] = (physicalMap[x][y] == WALL) ? 1.0 : 0.0;
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
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }

            double best = gradientMap[currentPos.x][currentPos.y];
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
        frustration = 0;
        goals.clear();
        return new ArrayList<>(path);
    }
    */
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
    public IntVLA findFleePathLarge(int size, int length, double preferLongerPaths, IntVLA impassable,
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
                CrossHash.Lightning.hash64(fearSources) == cachedFearSources) {
            gradientMap = cachedFleeMap;
        } else {
            cachedLongerPaths = preferLongerPaths;
            cachedImpassable = impassable2.hash64();
            cachedFearSources = CrossHash.Lightning.hash64(fearSources);
            cachedSize = size;
            resetMap();
            for (int g = 0; g < fearSources.length; g++) {
                setGoal(fearSources[g]);
            }
            if (goals.isEmpty())
                return new IntVLA(path);
            int[] scanArray = impassable2.toArray();
            scan(size, scanArray);

            for (int l = 0; l < gradientMap.length; l++) {
                gradientMap[l] *= (gradientMap[l] >= FLOOR) ? 1.0 : (-preferLongerPaths);
            }
            cachedFleeMap = scan(size, scanArray);
        }
        Adjacency adjacency = this.adjacency;
        int currentPos = start, pt;
        double paidLength = 0.0;
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos];
            rng.randomOrdering(adjacency.maxAdjacent, reuse);
            int choice = rng.nextIntHasty(adjacency.maxAdjacent);

            for (int d = 0; d < adjacency.maxAdjacent; d++) {
                pt = neighbors[reuse[d]][currentPos];
                if (gradientMap[pt] < best && !path.contains(pt)) {
                    best = gradientMap[pt];
                    choice = reuse[d];
                }
            }


            if (best >= gradientMap[currentPos] || physicalMap[neighbors[choice][currentPos]] > FLOOR) {
                path.clear();
                break;
            }
            currentPos = neighbors[choice][pt = currentPos];
            path.add(currentPos);
            paidLength += adjacency.costRules.get(costMap[currentPos] | ((adjacency.extractR(pt) == adjacency.extractR(currentPos) ? 0 : 0x10000)));
            frustration++;
            if (paidLength > length - 1.0) {
                if (onlyPassable.contains(currentPos)) {
                    closed.put(currentPos, WALL);
                    impassable2.add(currentPos);
                    return findFleePathLarge(size, length, preferLongerPaths, impassable2, onlyPassable, start, fearSources);
                }
                break;
            }
        }
        frustration = 0;
        goals.clear();
        return new IntVLA(path);
    }
/*
        if (!initialized) return null;
        path.clear();
        OrderedSet<Coord> impassable2;
        if (impassable == null)
            impassable2 = new OrderedSet<>();
        else
            impassable2 = new OrderedSet<>(impassable);

        if (onlyPassable == null)
            onlyPassable = new OrderedSet<>();
        if (fearSources == null || fearSources.length < 1) {
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
            if (goals.isEmpty())
                return new ArrayList<>(path);

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
        frustration = 0;
        goals.clear();
        return new ArrayList<>(path);
    }
    */


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
        if (!initialized || goals == null || goals.isEmpty()) return null;
        RNG rng2 = new StatefulRNG(new LightRNG(0xf00d));
        path.clear();
        Coord currentPos = target;
        while (true) {
            if (frustration > 2000) {
                path.clear();
                break;
            }
            double best = gradientMap[currentPos.x][currentPos.y];
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
     * @return A OrderedMap of Coord keys to Double values; the starts are included in this with the value 0.0.
     */
    public IntDoubleOrderedMap floodFill(int radius, int... starts) {
        if (!initialized || starts == null) return null;
        IntDoubleOrderedMap fill = new IntDoubleOrderedMap();

        resetMap();
        for (int g = 0; g < starts.length; g++) {
            setGoal(starts[g]);
        }

        if (goals.isEmpty())
            return fill;

        partialScan(radius);
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
}
