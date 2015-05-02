package squidpony.squidgrid;

import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.*;

/**
 * This class is used to determine when a sound is audible on a map and at what positions.
 * Created by Tommy Ettinger on 4/4/2015.
 */
public class SoundMap
{
    /**
     * The type of heuristic to use. Note that EUCLIDEAN is not an option here because it would only affect paths, and
     * there is no path-finding functionality in this class.
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
        CHEBYSHEV
    }

    /**
     * This affects how sound travels on diagonal directions vs. orthogonal directions. MANHATTAN should form a diamond
     * shape on a featureless map, while CHEBYSHEV will form a square.
     */
    public Measurement measurement = Measurement.MANHATTAN;


    /**
     * Stores which parts of the map are accessible and which are not. Should not be changed unless the actual physical
     * terrain has changed. You should call initialize() with a new map instead of changing this directly.
     */
    public double[][] physicalMap;
    /**
     * The frequently-changing values that are often the point of using this class; cells producing sound will have a
     * value greater than 0, cells that cannot possibly be reached by a sound will have a value of exactly 0, and walls
     * will have a value equal to the WALL constant (a negative number).
     */
    public double[][] gradientMap;
    /**
     * Height of the map. Exciting stuff. Don't change this, instead call initialize().
     */
    public int height;
    /**
     * Width of the map. Exciting stuff. Don't change this, instead call initialize().
     */
    public int width;
    /**
     * The latest results of findAlerted(), with Point keys representing the positions of creatures that were alerted
     * and Double values representing how loud the sound was when it reached them.
     */
    public HashMap<Point, Double> alerted = new HashMap<Point, Double>();
    /**
     * Cells with no sound are always marked with 0.
     */
    public static final double SILENT = 0.0;
    /**
     * Walls, which are solid no-entry cells, are marked with a significant negative number equal to -999500.0 .
     */
    public static final double WALL = -999500.0;
    /**
     * Sources of sound on the map; keys are positions, values are how loud the noise is (10.0 should spread 10 cells
     * away, with diminishing values assigned to further positions).
     */
    public HashMap<Point, Double> sounds;
    private HashMap<Point, Double> fresh;
    /**
     * The RNG used to decide which one of multiple equally-short paths to take.
     */
    public RNG rng;
    private static int frustration = 0;

    private boolean initialized = false;
    /**
     * Construct a SoundMap without a level to actually scan. If you use this constructor, you must call an
     * initialize() method before using this class.
     */
    public SoundMap() {
        rng = new RNG(new LightRNG());
        alerted = new HashMap<Point, Double>();
        fresh = new HashMap<Point, Double>();
        sounds = new HashMap<Point, Double>();
    }

    /**
     * Construct a SoundMap without a level to actually scan. This constructor allows you to specify an RNG before it is
     * used. If you use this constructor, you must call an initialize() method before using this class.
     */
    public SoundMap(RNG random) {
        rng = random;
        alerted = new HashMap<Point, Double>();
        fresh = new HashMap<Point, Double>();
        sounds = new HashMap<Point, Double>();
    }

    /**
     * Used to construct a SoundMap from the output of another. Any sounds will need to be assigned again.
     * @param level
     */
    public SoundMap(final double[][] level) {
        rng = new RNG(new LightRNG());
        alerted = new HashMap<Point, Double>();
        fresh = new HashMap<Point, Double>();
        sounds = new HashMap<Point, Double>();
        initialize(level);
    }
    /**
     * Used to construct a DijkstraMap from the output of another, specifying a distance calculation.
     * @param level
     * @param measurement
     */
    public SoundMap(final double[][] level, Measurement measurement) {
        rng = new RNG(new LightRNG());
        this.measurement = measurement;
        alerted = new HashMap<Point, Double>();
        fresh = new HashMap<Point, Double>();
        sounds = new HashMap<Point, Double>();
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
    public SoundMap(final char[][] level) {
        rng = new RNG(new LightRNG());
        alerted = new HashMap<Point, Double>();
        fresh = new HashMap<Point, Double>();
        sounds = new HashMap<Point, Double>();
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
    public SoundMap(final char[][] level, char alternateWall) {
        rng = new RNG(new LightRNG());
        alerted = new HashMap<Point, Double>();
        fresh = new HashMap<Point, Double>();
        sounds = new HashMap<Point, Double>();
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
    public SoundMap(final char[][] level, Measurement measurement) {
        rng = new RNG(new LightRNG());
        this.measurement = measurement;
        alerted = new HashMap<Point, Double>();
        fresh = new HashMap<Point, Double>();
        sounds = new HashMap<Point, Double>();
        initialize(level);
    }

    /**
     * Used to initialize or re-initialize a SoundMap that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently.
     * @param level
     * @return
     */
    public SoundMap initialize(final double[][] level) {
        width = level.length;
        height = level[0].length;
        gradientMap = new double[width][height];
        physicalMap = new double[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                gradientMap[x][y] = level[x][y];
                physicalMap[x][y] = level[x][y];
            }
        }
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a SoundMap that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently.
     * @param level
     * @return
     */
    public SoundMap initialize(final char[][] level) {
        width = level.length;
        height = level[0].length;
        gradientMap = new double[width][height];
        physicalMap = new double[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double t = (level[x][y] == '#') ? WALL : SILENT;
                gradientMap[x][y] = t;
                physicalMap[x][y] = t;
            }
        }
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a SoundMap that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently. This
     * initialize() method allows you to specify an alternate wall char other than the default character, '#' .
     * @param level
     * @param alternateWall
     * @return
     */
    public SoundMap initialize(final char[][] level, char alternateWall) {
        width = level.length;
        height = level[0].length;
        gradientMap = new double[width][height];
        physicalMap = new double[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double t = (level[x][y] == alternateWall) ? WALL : SILENT;
                gradientMap[x][y] = t;
                physicalMap[x][y] = t;
            }
        }
        initialized = true;
        return this;
    }

    /**
     * Resets the gradientMap to its original value from physicalMap. Does not remove sounds (they will still affect
     * scan() normally).
     */
    public void resetMap() {
            if(!initialized) return;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                gradientMap[x][y] = physicalMap[x][y];
            }
        }
    }

    /**
     * Resets this SoundMap to a state with no sounds, no alerted creatures, and no changes made to gradientMap
     * relative to physicalMap.
     */
    public void reset() {
        resetMap();
        alerted.clear();
        fresh.clear();
        sounds.clear();
        frustration = 0;
    }

    /**
     * Marks a cell as producing a sound with the given loudness; this can be placed on a wall or unreachable area,
     * but that may cause the sound to be un-hear-able. A sound emanating from a cell on one side of a 2-cell-thick
     * wall will only radiate sound on one side, which can be used for certain effects. A sound emanating from a cell
     * in a 1-cell-thick wall will radiate on both sides.
     * @param x
     * @param y
     * @param loudness The number of cells the sound should spread away using the current measurement.
     */
    public void setSound(int x, int y, double loudness) {
        if(!initialized) return;
        Point pt = new Point(x, y);
        if(sounds.containsKey(pt) && sounds.get(pt) >= loudness)
            return;
        sounds.put(pt, loudness);
    }

    /**
     * Marks a cell as producing a sound with the given loudness; this can be placed on a wall or unreachable area,
     * but that may cause the sound to be un-hear-able. A sound emanating from a cell on one side of a 2-cell-thick
     * wall will only radiate sound on one side, which can be used for certain effects. A sound emanating from a cell
     * in a 1-cell-thick wall will radiate on both sides.
     * @param pt
     * @param loudness The number of cells the sound should spread away using the current measurement.
     */
    public void setSound(Point pt, double loudness) {
        if(!initialized) return;
        if(sounds.containsKey(pt) && sounds.get(pt) >= loudness)
            return;
        sounds.put(pt, loudness);
    }

    /**
     * If a sound is being produced at a given (x, y) location, this removes it.
     * @param x
     * @param y
     */
    public void removeSound(int x, int y) {
        if(!initialized) return;
        Point pt = new Point(x, y);
        if(sounds.containsKey(pt))
            sounds.remove(pt);
    }

    /**
     * If a sound is being produced at a given location (a Point), this removes it.
     * @param pt
     */
    public void removeSound(Point pt) {
        if(!initialized) return;
        if(sounds.containsKey(pt))
            sounds.remove(pt);
    }

    /**
     * Marks a specific cell in gradientMap as a wall, which makes sounds potentially unable to pass through it.
     * @param x
     * @param y
     */
    public void setOccupied(int x, int y) {
        if(!initialized) return;
        gradientMap[x][y] = WALL;
    }

    /**
     * Reverts a cell to the value stored in the original state of the level as known by physicalMap.
     * @param x
     * @param y
     */
    public void resetCell(int x, int y) {
        if(!initialized) return;
        gradientMap[x][y] = physicalMap[x][y];
    }

    /**
     * Reverts a cell to the value stored in the original state of the level as known by physicalMap.
     * @param pt
     */
    public void resetCell(Point pt) {
        if(!initialized) return;
        gradientMap[pt.x][pt.y] = physicalMap[pt.x][pt.y];
    }

    /**
     * Used to remove all sounds.
     */
    public void clearSounds() {
        if(!initialized) return;
        sounds.clear();
    }

    protected void setFresh(int x, int y, double counter) {
        if(!initialized) return;
        gradientMap[x][y] = counter;
        fresh.put(new Point(x, y), counter);
    }

    protected void setFresh(final Point pt, double counter) {
        if(!initialized) return;
        gradientMap[pt.x][pt.y] = counter;
        fresh.put(new Point(pt.x, pt.y), counter);
    }

    /**
     * Recalculate the sound map and return it. Cells that were marked as goals with setSound will have
     * a value greater than 0 (higher numbers are louder sounds), the cells adjacent to sounds will have a value 1 less
     * than the loudest adjacent cell, and cells progressively further from sounds will have a value equal to the
     * loudness of the nearest sound minus the distance from it, to a minimum of 0. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class. Like sound itself, the sound map
     * allows some passage through walls; specifically, 1 cell thick of wall can be passed through, with reduced
     * loudness, before the fill cannot go further. This uses the current measurement.
     *
     * @return A 2D double[width][height] using the width and height of what this knows about the physical map.
     */
    public double[][] scan() {
        if(!initialized) return null;

        for (Map.Entry<Point, Double> entry : sounds.entrySet()) {
            gradientMap[entry.getKey().x][entry.getKey().y] = entry.getValue();
            if(fresh.containsKey(entry.getKey()) && fresh.get(entry.getKey()) > entry.getValue())
            {
            }
            else
            {
                fresh.put(entry.getKey(), entry.getValue());
            }

        }
        int numAssigned = fresh.size();

        Direction[] dirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        while (numAssigned > 0) {
            numAssigned = 0;
            HashMap<Point, Double> fresh2 = new HashMap<Point, Double>(fresh.size());
            fresh2.putAll(fresh);
            fresh.clear();

            for (Map.Entry<Point, Double> cell : fresh2.entrySet()) {
                if(cell.getValue() <= 1) //We shouldn't assign values lower than 1.
                    continue;
                for (int d = 0; d < dirs.length; d++) {
                    Point adj = new Point(cell.getKey());
                    adj.translate(dirs[d].deltaX, dirs[d].deltaY);
                    if(adj.x < 0 || adj.x >= width || adj.y < 0 || adj.y >= height)
                        continue;
                    if(physicalMap[cell.getKey().x][cell.getKey().y] == WALL && physicalMap[adj.x][adj.y] == WALL)
                        continue;
                    if (gradientMap[cell.getKey().x][cell.getKey().y] > gradientMap[adj.x][adj.y] + 1) {
                        double v = cell.getValue() - 1 - ((physicalMap[adj.x][adj.y] == WALL) ? 1 : 0);
                        if (v > 0) {
                            gradientMap[adj.x][adj.y] = v;
                            fresh.put(new Point(adj.x, adj.y), v);
                            ++numAssigned;
                        }
                    }
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (physicalMap[x][y] == WALL) {
                    gradientMap[x][y] = WALL;
                }
            }
        }

        return gradientMap;
    }

    /**
     * Scans the dungeon using SoundMap.scan, adding any positions in extraSounds to the group of known sounds before
     * scanning.  The creatures passed to this function as a Set of Points will have the loudness of all sounds at
     * their position put as the value in alerted corresponding to their Point position.
     *
     * @param creatures
     * @param extraSounds
     * @return
     */
    public HashMap<Point, Double> findAlerted(Set<Point> creatures, Map<Point, Double> extraSounds) {
        if(!initialized) return null;
        alerted = new HashMap<Point, Double>(creatures.size());

        resetMap();
        for (Map.Entry<Point, Double> sound : extraSounds.entrySet()) {
            setSound(sound.getKey(), sound.getValue());
        }
        scan();
        for(Point critter : creatures)
        {
            if(critter.x < 0 || critter.x >= width || critter.y < 0 || critter.y >= height)
                continue;
            alerted.put(new Point(critter.x, critter.y), gradientMap[critter.x][critter.y]);
        }
        return alerted;
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
}
