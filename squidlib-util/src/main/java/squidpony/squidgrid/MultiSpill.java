package squidpony.squidgrid;

import squidpony.squidgrid.Spill.Measurement;
import squidpony.squidmath.Coord;
import squidpony.squidmath.RNG;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A randomized flood-fill implementation that can be used for level generation (e.g. filling ponds and lakes), for
 * gas propagation, or for all sorts of fluid-dynamics-on-the-cheap.
 * Created by Tommy Ettinger on 4/7/2015.
 */
public class MultiSpill {

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
     * The cells that are filled by the a spiller with index n when it reaches its volume or limits will be equal to n;
     * others will be -1.
     */
    public short[][] spillMap;

    /**
     * The cells that are filled by the any spiller will be true, others will be false.
     */
    public boolean[][] anySpillMap;

    /**
     * Each spiller in the MultiSpill corresponds to a list of points that it will randomly fill, starting with the
     * initial point for each spiller passed to start(), in order of when they are reached.
     */
    public ArrayList<ArrayList<Coord>> spreadPattern;
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
    private ArrayList<LinkedHashSet<Coord>> fresh;
    /**
     * The StatefulRNG used to decide how to randomly fill a space; can have its state set and read.
     */
    public StatefulRNG rng;

    private boolean initialized = false;
    /**
     * Construct a Spill without a level to actually scan. If you use this constructor, you must call an
     * initialize() method before using this class.
     */
    public MultiSpill() {
        rng = new StatefulRNG();

        fresh = new ArrayList<LinkedHashSet<Coord>>();
    }
    /**
     * Construct a Spill without a level to actually scan. This constructor allows you to specify an RNG, but the actual
     * RandomnessSource the RNG that this object uses will not be identical to the one passed as random (64 bits will
     * be requested from the passed RNG, and that will be used to seed this class' RNG).
     *
     * If you use this constructor, you must call an  initialize() method before using this class.
     * @param random an RNG that will be converted to a StatefulRNG if it is not one already
     */
    public MultiSpill(RNG random) {
        rng = new StatefulRNG(random.getRandomness());

        fresh = new ArrayList<LinkedHashSet<Coord>>();
    }

    /**
     * Used to construct a Spill from the output of another.
     * @param level a short[][] that should have been the spillMap of another MultiSpill
     */
    public MultiSpill(final short[][] level) {
        rng = new StatefulRNG();

        initialize(level);
    }
    /**
     * Used to construct a Spill from the output of another, specifying a distance calculation.
     * @param level a short[][] that should have been the spillMap of another MultiSpill
     * @param measurement a Spill.Measurement that should usually be MANHATTAN
     */
    public MultiSpill(final short[][] level, Measurement measurement) {
        rng = new StatefulRNG();

        this.measurement = measurement;

        initialize(level);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here.
     *
     * @param level a char[][] that should use '#' for walls and '.' for floors
     */
    public MultiSpill(final char[][] level) {
        rng = new StatefulRNG();

        initialize(level);
    }
    /**
     * Constructor meant to take a char[][] returned by DungeonGen.generate(), or any other
     * char[][] where one char means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. You can specify the character used for walls.
     *
     * @param level a char[][] that should use alternateWall for walls and '.' for floors
     * @param alternateWall the char to use for walls
     */
    public MultiSpill(final char[][] level, char alternateWall) {
        rng = new StatefulRNG();

        initialize(level, alternateWall);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. This constructor specifies a distance measurement.
     *
     * @param level a char[][] that should use '#' for walls and '.' for floors
     * @param measurement a Spill.Measurement that should usually be MANHATTAN
     */
    public MultiSpill(final char[][] level, Measurement measurement) {
        rng = new StatefulRNG();
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
     * @param level a char[][] that should use '#' for walls and '.' for floors
     * @param measurement a Spill.Measurement that should usually be MANHATTAN
     * @param random an RNG that will be converted to a StatefulRNG if it is not one already
     */
    public MultiSpill(final char[][] level, Measurement measurement, RNG random) {
        rng = new StatefulRNG(random.getRandomness());
        this.measurement = measurement;

        initialize(level);
    }

    /**
     * Used to initialize or re-initialize a Spill that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently.
     * @param level a short[][] that should have been the spillMap of another MultiSpill
     * @return this for chaining
     */
    public MultiSpill initialize(final short[][] level) {
        fresh = new ArrayList<LinkedHashSet<Coord>>();
        width = level.length;
        height = level[0].length;
        spillMap = new short[width][height];
        anySpillMap = new boolean[width][height];
        physicalMap = new boolean[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                spillMap[x][y] = level[x][y];
                anySpillMap[x][y] = level[x][y] > 0;
                physicalMap[x][y] = level[x][y] >= 0;
            }
        }
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a Spill that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ).
     * @param level a char[][] that should use '#' for walls and '.' for floors
     * @return this for chaining
     */
    public MultiSpill initialize(final char[][] level) {
        fresh = new ArrayList<LinkedHashSet<Coord>>();
        width = level.length;
        height = level[0].length;
        spillMap = new short[width][height];
        anySpillMap = new boolean[width][height];
        physicalMap = new boolean[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                spillMap[x][y] = -1;
                anySpillMap[x][y] = false;
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
     * @param level a char[][] that should use alternateWall for walls and '.' for floors
     * @param alternateWall the char to use for walls
     * @return this for chaining
     */
    public MultiSpill initialize(final char[][] level, char alternateWall) {
        fresh = new ArrayList<LinkedHashSet<Coord>>();
        width = level.length;
        height = level[0].length;
        spillMap = new short[width][height];
        anySpillMap = new boolean[width][height];
        physicalMap = new boolean[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                spillMap[x][y] = -1;
                anySpillMap[x][y] = false;
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
                spillMap[x][y] = -1;
                anySpillMap[x][y] = false;
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
    }

    /**
     * Reverts a cell to an unfilled state (false in spillMap).
     * @param x
     * @param y
     */
    public void resetCell(int x, int y) {
        if(!initialized) return;
        spillMap[x][y] = -1;
        anySpillMap[x][y] = false;
    }

    /**
     * Reverts a cell to an unfilled state (false in spillMap).
     * @param pt
     */
    public void resetCell(Coord pt) {
        if(!initialized) return;
        spillMap[pt.x][pt.y] = -1;
        anySpillMap[pt.x][pt.y] = false;
    }

    protected void setFresh(int idx, int x, int y) {
        if(!initialized) return;
        fresh.get(idx).add(Coord.get(x, y));
    }

    protected void setFresh(int idx, final Coord pt) {
        if(!initialized) return;
        for(LinkedHashSet<Coord> f : fresh)
        {
            if(f.contains(pt))
                return;
        }
        fresh.get(idx).add(pt);
    }

    /**
     * Recalculate the spillMap and return the spreadPattern. The cell corresponding to a Coord in entries will be true,
     * the cells near each of those will be true if chosen at random from all passable cells adjacent to a
     * filled (true) cell, and all other cells will be false. This takes a total number of cells to attempt
     * to fill (the volume parameter), which can be negative to simply fill the whole map, and will fill less if it has
     * completely exhausted all passable cells from all sources in entries.
     * If the measurement this Spill uses is anything other than MANHATTAN, you can expect many gaps in the first
     * filled area.  Subsequent calls to start() with the same entry and a higher volume will expand the area
     * of the Spill, and are likely to fill any gaps after a few subsequent calls. Increasing the volume slowly
     * is the best way to ensure that gaps only exist on the very edge if you use a non-MANHATTAN measurement.
     *
     * @param entries the first cell for each spiller to spread from, which should really be passable.
     * @param volume the total number of cells to attempt to fill; if negative will fill the whole map.
     * @param impassable a Set of Position keys representing the locations of moving obstacles to a
     *                   fill that cannot be moved through; this can be null if there are no such obstacles.
     * @return an ArrayList of Points that this will enter, in order starting with entry at index 0, until it
     * reaches its volume or fills its boundaries completely.
     */
    public ArrayList<ArrayList<Coord>> start(List<Coord> entries, int volume, Set<Coord> impassable) {
        if(!initialized) return null;
        if(impassable == null)
            impassable = new LinkedHashSet<>();
        if(volume < 0)
            volume = Integer.MAX_VALUE;
        ArrayList<Coord> spillers = new ArrayList<Coord>(entries);
        spreadPattern = new ArrayList<ArrayList<Coord>>(spillers.size());
        fresh.clear();
        for (short i = 0; i < spillers.size(); i++) {
            spreadPattern.add(new ArrayList<Coord>(128));
            fresh.add(new LinkedHashSet<Coord>(128));
            Coord c = spillers.get(i);
            spillMap[c.x][c.y] = i;

        }
        boolean hasFresh = false;
        Coord temp;
        for (int x = 0; x < spillMap.length; x++) {
            for (int y = 0; y < spillMap[x].length; y++) {
                temp = Coord.get(x, y);
                if (spillMap[x][y] >= 0 && !impassable.contains(temp)) {
                    fresh.get(spillMap[x][y]).add(temp);
                    hasFresh = true;
                }
            }
        }
        Direction[] dirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        while (hasFresh && filled < volume) {
            hasFresh = false;
            for (short i = 0; i < spillers.size() && filled < volume; i++) {
                LinkedHashSet<Coord> currentFresh = fresh.get(i);
                if(currentFresh.isEmpty())
                    continue;
                else
                    hasFresh = true;
                Coord cell = currentFresh.toArray(new Coord[currentFresh.size()])[rng.nextInt(currentFresh.size())];

                spreadPattern.get(i).add(cell);
                spillMap[cell.x][cell.y] = i;
                filled++;
                anySpillMap[cell.x][cell.y] = true;

                for (int d = 0; d < dirs.length; d++) {
                    Coord adj = cell.translate(dirs[d].deltaX, dirs[d].deltaY);
                    double h = heuristic(dirs[d]);
                    if (physicalMap[adj.x][adj.y] && !anySpillMap[adj.x][adj.y] && !impassable.contains(adj) && rng.nextDouble() <= 1.0 / h) {
                        setFresh(i, adj);
                    }
                }
                currentFresh.remove(cell);
            }
        }
        return spreadPattern;
    }

    /**
     * Recalculate the spillMap and return the spreadPattern. The cell corresponding to a key in entries will be true,
     * the cells near each of those will be true if chosen at random from all passable cells adjacent to a
     * filled (true) cell, and all other cells will be false. This takes a total number of cells to attempt
     * to fill (the volume parameter), which can be negative to simply fill the whole map, and will fill less if it has
     * completely exhausted all passable cells from all sources in entries. It uses the values in entries to determine
     * whether it should advance from a particular key in that step or not; this choice is pseudo-random. If you have
     * some values that are at or near 1.0 and some values that are closer to 0.0, you should expect the keys for the
     * higher values to spread further out than the keys associated with lower values.
     * <br>
     * If the measurement this Spill uses is anything other than MANHATTAN, you can expect many gaps in the first
     * filled area.  Subsequent calls to start() with the same entry and a higher volume will expand the area
     * of the Spill, and are likely to fill any gaps after a few subsequent calls. Increasing the volume slowly
     * is the best way to ensure that gaps only exist on the very edge if you use a non-MANHATTAN measurement.
     * <br>
     * The intended purpose for this method is filling contiguous areas of dungeon with certain terrain features, but it
     * has plenty of other uses as well.
     * @param entries key: the first cell for each spiller to spread from. value: the bias toward advancing this key;
     *                1.0 will always advance, 0.0 will never advance beyond the key, in between will randomly choose
     * @param volume the total number of cells to attempt to fill; if negative will fill the whole map.
     * @param impassable a Set of Position keys representing the locations of moving obstacles to a
     *                   fill that cannot be moved through; this can be null if there are no such obstacles.
     * @return an ArrayList of Points that this will enter, in order starting with entry at index 0, until it
     * reaches its volume or fills its boundaries completely.
     */
    public ArrayList<ArrayList<Coord>> start(LinkedHashMap<Coord, Double> entries, int volume, Set<Coord> impassable) {
        if(!initialized) return null;
        if(impassable == null)
            impassable = new LinkedHashSet<>();
        if(volume < 0)
            volume = Integer.MAX_VALUE;
        ArrayList<Coord> spillers = new ArrayList<Coord>(entries.keySet());
        ArrayList<Double> biases = new ArrayList<Double>(entries.values());
        spreadPattern = new ArrayList<ArrayList<Coord>>(spillers.size());
        fresh.clear();
        for (short i = 0; i < spillers.size(); i++) {
            spreadPattern.add(new ArrayList<Coord>(128));
            fresh.add(new LinkedHashSet<Coord>(128));
            Coord c = spillers.get(i);
            spillMap[c.x][c.y] = i;

        }
        boolean hasFresh = false;
        Coord temp;
        for (int x = 0; x < spillMap.length; x++) {
            for (int y = 0; y < spillMap[x].length; y++) {
                temp = Coord.get(x, y);
                if (spillMap[x][y] >= 0 && !impassable.contains(temp)) {
                    fresh.get(spillMap[x][y]).add(temp);
                    hasFresh = true;
                }
            }
        }

        Direction[] dirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        while (hasFresh && filled < volume) {
            hasFresh = false;
            for (short i = 0; i < spillers.size() && filled < volume; i++) {
                LinkedHashSet<Coord> currentFresh = fresh.get(i);
                if(currentFresh.isEmpty())
                    continue;
                else
                    hasFresh = true;
                Coord cell = currentFresh.toArray(new Coord[currentFresh.size()])[rng.nextInt(currentFresh.size())];
                if(rng.nextDouble() < biases.get(i)) {

                    spreadPattern.get(i).add(cell);
                    spillMap[cell.x][cell.y] = i;
                    filled++;
                    anySpillMap[cell.x][cell.y] = true;


                    for (int d = 0; d < dirs.length; d++) {
                        Coord adj = cell.translate(dirs[d].deltaX, dirs[d].deltaY);
                        double h = heuristic(dirs[d]);
                        if (physicalMap[adj.x][adj.y] && !anySpillMap[adj.x][adj.y] && !impassable.contains(adj)
                                && rng.nextDouble() <= 1.0 / h) {
                            setFresh(i, adj);
                        }
                    }
                    currentFresh.remove(cell);
                }
            }
        }
        return spreadPattern;
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
