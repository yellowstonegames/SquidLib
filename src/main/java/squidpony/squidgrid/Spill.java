package squidpony.squidgrid;

import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import java.awt.*;
import java.util.*;
/**
 * A randomized flood-fill implementation that can be used for level generation (e.g. filling ponds and lakes), for
 * gas propagation, or for all sorts of fluid-dynamics-on-the-cheap.
 * Created by Tommy Ettinger on 4/7/2015.
 */
public class Spill {
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
         * The distance it takes as the crow flies.
         */
        EUCLIDEAN
    }

    /**
     * This affects how distance is measured on diagonal directions vs. orthogonal directions. MANHATTAN should form a
     * diamond shape on a featureless map, while CHEBYSHEV and EUCLIDEAN will form a square. If you only call
     * Spill.start() once, you should strongly prefer MANHATTAN, even if the rest of the game uses another
     * measurement, because CHEBYSHEV and EUCLIDEAN can produce odd, gap-filled flood-fills.  Any case where you have
     * too many gaps can be corrected to varying extent by calling start() more than once with slowly increasing values.
     * Because start() will extend from the existing area of the Spill, holes are likely to be filled after a few calls,
     * but if the last call to start() tries to fill too many more cells than the previous one, it can cause holes on
     * the periphery of the Spill area.
     */
    public Measurement measurement = Measurement.MANHATTAN;


    /**
     * Stores which parts of the map are accessible (with a value of true) and which are not (with a value of false,
     * including both walls and unreachable sections of the map). Should not be changed unless the actual physical
     * terrain has changed. You should call initialize() with a new map instead of changing this directly.
     */
    public boolean[][] physicalMap;
    /**
     * The cells that are filled by the Spill when it reaches its volume or limits will be true; others will be false.
     */
    public boolean[][] spillMap;

    /**
     * The list of points that the Spill will randomly fill, starting with what is passed to start(), in order of when
     * they are reached.
     */
    public ArrayList<Point> spreadPattern;
    /**
     * Height of the map. Exciting stuff. Don't change this, instead call initialize().
     */
    public int height;
    /**
     * Width of the map. Exciting stuff. Don't change this, instead call initialize().
     */
    public int width;
    /**
     * The amount of cells filled by this Spill, which may be less than the volume passed to start() if the boundaries
     * are reached on all sides and the Spill has no more room to fill.
     */
    public int filled = 0;
    private LinkedHashSet<Point> fresh;
    /**
     * The RNG used to decide which one of multiple equally-short paths to take.
     */
    public RNG rng;
    /**
     * The LightRNG used as a RandomnessSource for the RNG this object uses. Can have its state read and set.
     */
    public LightRNG lrng;
    private static int frustration = 0;

    private boolean initialized = false;
    /**
     * Construct a Spill without a level to actually scan. If you use this constructor, you must call an
     * initialize() method before using this class.
     */
    public Spill() {
        lrng = new LightRNG();
        rng = new RNG(lrng);

        fresh = new LinkedHashSet<>();
    }
    /**
     * Construct a Spill without a level to actually scan. This constructor allows you to specify an RNG, but the actual
     * RandomnessSource the RNG that this object uses will not be identical to the one passed as random (64 bits will
     * be requested from the passed RNG, and that will be used to seed this class' RNG).
     *
     * If you use this constructor, you must call an  initialize() method before using this class.
     */
    public Spill(RNG random) {
        lrng = new LightRNG(random.nextLong());
        rng = new RNG(lrng);

        fresh = new LinkedHashSet<>();
    }

    /**
     * Construct a Spill without a level to actually scan. This constructor allows you to specify a RandomnessSource
     * in the form of a LightRNG, which will be referenced in this class (if the state of random changes because this
     * object needed a random number, the state change will be reflected in the code that passed random to here).
     *
     * If you use this constructor, you must call an  initialize() method before using this class.
     */
    public Spill(LightRNG random) {
        lrng = random;
        rng = new RNG(lrng);

        fresh = new LinkedHashSet<>();
    }

    /**
     * Used to construct a Spill from the output of another.
     * @param level
     */
    public Spill(final boolean[][] level) {
        lrng = new LightRNG();
        rng = new RNG(lrng);

        initialize(level);
    }
    /**
     * Used to construct a Spill from the output of another, specifying a distance calculation.
     * @param level
     * @param measurement
     */
    public Spill(final boolean[][] level, Measurement measurement) {
        lrng = new LightRNG();
        rng = new RNG(lrng);

        this.measurement = measurement;

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
    public Spill(final char[][] level) {
        lrng = new LightRNG();
        rng = new RNG(lrng);

        initialize(level);
    }
    /**
     * Constructor meant to take a char[][] returned by DungeonGen.generate(), or any other
     * char[][] where one char means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. You can specify the character used for walls.
     *
     * @param level
     */
    public Spill(final char[][] level, char alternateWall) {
        lrng = new LightRNG();
        rng = new RNG(lrng);

        initialize(level, alternateWall);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. This constructor specifies a distance measurement.
     *
     * @param level
     * @param measurement
     */
    public Spill(final char[][] level, Measurement measurement) {
        lrng = new LightRNG();
        rng = new RNG(lrng);
        this.measurement = measurement;

        initialize(level);
    }
    /**
     * Constructor meant to take a char[][] returned by DungeonGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. This constructor specifies a distance measurement.
     *
     * This constructor allows you to specify an RNG, but the actual RandomnessSource the RNG that this object uses
     * will not be identical to the one passed as random (64 bits will be requested from the passed RNG, and that will
     * be used to seed this class' RNG).
     *
     * @param level
     * @param measurement
     */
    public Spill(final char[][] level, Measurement measurement, RNG random) {
        lrng = new LightRNG(random.nextLong());
        rng = new RNG(lrng);
        this.measurement = measurement;

        initialize(level);
    }
    /**
     * Constructor meant to take a char[][] returned by DungeonGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. This constructor specifies a distance measurement.
     *
     * This constructor allows you to specify a RandomnessSource in the form of a LightRNG, which will be referenced in
     * this class (if the state of random changes because this object needed a random number, the state change will be
     * reflected in the code that passed random to here).
     * @param level
     * @param measurement
     */
    public Spill(final char[][] level, Measurement measurement, LightRNG random) {
        lrng = random;
        rng = new RNG(lrng);
        this.measurement = measurement;

        initialize(level);
    }

    /**
     * Used to initialize or re-initialize a Spill that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ).
     * @param level
     * @return
     */
    public Spill initialize(final boolean[][] level) {
        fresh = new LinkedHashSet<>();
        width = level.length;
        height = level[0].length;
        spillMap = new boolean[width][height];
        physicalMap = new boolean[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                spillMap[x][y] = level[x][y];
                physicalMap[x][y] = level[x][y];
            }
        }
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a Spill that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ).
     * @param level
     * @return
     */
    public Spill initialize(final char[][] level) {
        fresh = new LinkedHashSet<>();
        width = level.length;
        height = level[0].length;
        spillMap = new boolean[width][height];
        physicalMap = new boolean[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                spillMap[x][y] = false;
                physicalMap[x][y] = (level[x][y] != '#');
            }
        }
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a Spill that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ). This
     * initialize() method allows you to specify an alternate wall char other than the default character, '#' .
     * @param level
     * @param alternateWall
     * @return
     */
    public Spill initialize(final char[][] level, char alternateWall) {
        fresh = new LinkedHashSet<>();
        width = level.length;
        height = level[0].length;
        spillMap = new boolean[width][height];
        physicalMap = new boolean[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                spillMap[x][y] = false;
                physicalMap[x][y] = (level[x][y] != alternateWall);
            }
        }
        initialized = true;
        return this;
    }

    /**
     * Resets the spillMap to being empty.
     */
    public void resetMap() {
        if(!initialized) return;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                spillMap[x][y] = false;
            }
        }
    }

    /**
     * Resets this Spill to a state with an empty spillMap and an empty spreadPattern.
     */
    public void reset() {
        resetMap();
        spreadPattern.clear();
        fresh.clear();
        frustration = 0;
    }

    /**
     * Reverts a cell to an unfilled state (false in spillMap).
     * @param x
     * @param y
     */
    public void resetCell(int x, int y) {
        if(!initialized) return;
        spillMap[x][y] = false;
    }

    /**
     * Reverts a cell to an unfilled state (false in spillMap).
     * @param pt
     */
    public void resetCell(Point pt) {
        if(!initialized) return;
        spillMap[pt.x][pt.y] = false;
    }

    protected void setFresh(int x, int y) {
        if(!initialized) return;
        fresh.add(new Point(x, y));
    }

    protected void setFresh(final Point pt) {
        if(!initialized) return;
        fresh.add(pt);
    }

    /**
     * Recalculate the spillMap and return the spreadPattern. The cell corresponding to entry will be true,
     * the cells near that will be true if chosen at random from all passable cells adjacent to a
     * filled (true) cell, and all other cells will be false. This takes a total number of cells to attempt
     * to fill (the volume parameter), and will fill less if it has completely exhausted all passable cells.
     * If the measurement this Spill uses is anything other than MANHATTAN, you can expect many gaps in the first
     * filled area.  Subsequent calls to start() with the same entry and a higher volume will expand the area
     * of the Spill, and are likely to fill any gaps after a few subsequent calls. Increasing the volume slowly
     * is the best way to ensure that gaps only exist on the very edge if you use a non-MANHATTAN measurement.
     *
     * @param entry The first cell to spread from, which should really be passable.
     * @param volume The total number of cells to attempt to fill, which must be non-negative.
     * @param impassable A Set of Position keys representing the locations of moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @return An ArrayList of Points that this will enter, in order starting with entry at index 0, until it
     * reaches its volume or fills its boundaries completely.
     */
    public ArrayList<Point> start(Point entry, int volume, Set<Point> impassable) {
        if(!initialized) return null;
        if(impassable == null)
            impassable = new LinkedHashSet<>();
        if(!physicalMap[entry.x][entry.y] || impassable.contains(entry))
            return null;
        spreadPattern = new ArrayList<Point>(volume);
        spillMap[entry.x][entry.y] = true;
        Point temp = new Point(0,0);
        for(int x = 0; x < spillMap.length; x++, temp.x = x)
        {
            for(int y = 0; y < spillMap[x].length; y++, temp.y = y)
            {
                if(spillMap[x][y] && !impassable.contains(temp))
                    fresh.add(new Point(temp));
            }
        }

        Direction[] dirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;
        while (!fresh.isEmpty() && spreadPattern.size() < volume) {
            Point cell = (Point) fresh.toArray()[rng.nextInt(fresh.size())];
            spreadPattern.add(cell);
            spillMap[cell.x][cell.y] = true;
            for (int d = 0; d < dirs.length; d++) {
                Point adj = new Point(cell);
                adj.translate(dirs[d].deltaX, dirs[d].deltaY);
                double h = heuristic(dirs[d]);
                if (physicalMap[adj.x][adj.y] && !spillMap[adj.x][adj.y] && !impassable.contains(adj) && rng.nextDouble() <= 1.0 / h) {
                    setFresh(adj);
                }
            }
            fresh.remove(cell);
        }
        filled = spreadPattern.size();
        return spreadPattern;
    }

    /**
     * Everybody do the Fisher-Yates Shuffle, come on.
     * @param dirs
     * @return
     */
    private Direction[] shuffle(Direction[] dirs)
    {
        Direction[] array = dirs.clone();
        int n = array.length;
        for (int i = 0; i < n; i++)
        {
            int r = i + (int)(rng.nextDouble() * (n - i));
            Direction d = array[r];
            array[r] = array[i];
            array[i] = d;
        }
        return array;
    }
    private static final double root2 = Math.sqrt(2.0);
    private double heuristic(Direction target) {
        switch (measurement) {
            case MANHATTAN:
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
                        return  1.0;
                }
        }
        return 1.0;
    }
}
