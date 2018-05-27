package squidpony.squidmath;

import squidpony.ArrayTools;
import squidpony.StringKit;
import squidpony.annotation.Beta;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.zone.MutableZone;
import squidpony.squidgrid.zone.Zone;

import java.io.Serializable;
import java.util.*;

/**
 * Region encoding of on/off information about areas using bitsets; uncompressed (fatty), but fast (greased lightning).
 * This can handle any size of 2D data, and is not strictly limited to 256x256 as CoordPacker is. It stores several long
 * arrays and uses each bit in one of those numbers to represent a single point, though sometimes this does waste bits
 * if the height of the area this encodes is not a multiple of 64 (if you store a 80x64 map, this uses 80 longs; if you
 * store an 80x65 map, this uses 160 longs, 80 for the first 64 rows and 80 more to store the next row). It's much
 * faster than CoordPacker at certain operations (anything that expands or retracts an area, including
 * {@link #expand()}), {@link #retract()}), {@link #fringe()}), {@link #surface()}, and {@link #flood(GreasedRegion)},
 * and slightly faster on others, like {@link #and(GreasedRegion)} (called intersectPacked() in CoordPacker) and
 * {@link #or(GreasedRegion)} (called unionPacked() in CoordPacker).
 * <br>
 * Each GreasedRegion is mutable, and instance methods typically modify that instance and return it for chaining. There
 * are exceptions, usually where multiple GreasedRegion values are returned and the instance is not modified.
 * <br>
 * Typical usage involves constructing a GreasedRegion from some input data, like a char[][] for a map or a double[][]
 * from DijkstraMap, and modifying it spatially with expand(), retract(), flood(), etc. It's common to mix in data from
 * other GreasedRegions with and() (which gets the intersection of two GreasedRegions and stores it in one), or() (which
 * is like and() but for the union), xor() (like and() but for exclusive or, finding only cells that are on in exactly
 * one of the two GreasedRegions), and andNot() (which can be considered the "subtract another region from me" method).
 * There are 8-way (Chebyshev distance) variants on all of the spatial methods, and methods without "8way" in the name
 * are either 4-way (Manhattan distance) or not affected by distance measurement. Once you have a GreasedRegion, you may
 * want to:
 * <ul>
 *     <li>get a single random point from it (use {@link #singleRandom(IRNG)}),</li>
 *     <li>get several random points from it with random sampling (use {@link #randomPortion(IRNG, int)}),</li>
 *     <li>mutate the current GreasedRegion to keep random points from it with random sampling (use {@link #randomRegion(IRNG, int)}),</li>
 *     <li>get random points that are likely to be separated (use {@link #mixedRandomSeparated(double, int, long)} with
 *     a random long for the last parameter, or either {@link #quasiRandomSeparated(double)} or
 *     {@link #separatedZCurve(double)} if you don't want a random seed),</li>
 *     <li>do what any of the above "separated" methods can do, but mutate the current GreasedRegion (use
 *     {@link #mixedRandomRegion(double, int, long)}, {@link #separatedRegionZCurve(double, int)}, or 
 *     {@link #quasiRandomRegion(double, int)}),</li>
 *     <li>get all points from it (use {@link #asCoords()}).</li>
 * </ul>
 * <br>
 * You may also want to produce some 2D data from one or more GreasedRegions, as with {@link #sum(GreasedRegion...)} or
 * {@link #toChars()}. The most effective techniques regarding GreasedRegion involve multiple methods, like getting a
 * few random points from an existing GreasedRegion representing floor tiles in a dungeon with
 * {@link #randomRegion(IRNG, int)}, then finding a random expansion of those initial points with
 * {@link #spill(GreasedRegion, int, IRNG)}, giving the original GreasedRegion of floor tiles as the first argument.
 * This could be used to position puddles of water or toxic waste in a dungeon level, while still keeping the starting
 * points and finished points within the boundaries of valid (floor) cells. If you wanted to place something like mold
 * that can be on floors or on cells immediately adjacent to floors (like walls), you could call {@link #expand()} on
 * the floor tiles before calling spill, allowing the spill to spread onto non-floor cells that are next to floors.
 * <br>
 * For efficiency, you can place one GreasedRegion into another (typically a temporary value that is no longer needed
 * and can be recycled) using {@link #remake(GreasedRegion)}, or give the information that would normally be used to
 * construct a fresh GreasedRegion to an existing one of the same dimensions with {@link #refill(boolean[][])} or any
 * of the overloads of refill(). These re-methods don't do as much work as a constructor does if the width and height
 * of their argument are identical to their current width and height, and don't create more garbage for the GC.
 * <br>
 * Created by Tommy Ettinger on 6/24/2016.
 */
@Beta
public class GreasedRegion extends Zone.Skeleton implements Collection<Coord>, Serializable, MutableZone {
    private static final long serialVersionUID = 0;
    private static final SobolQRNG sobol = new SobolQRNG(2);

    public long[] data;
    public int height;
    public int width;
    protected int ySections;
    protected long yEndMask;

    /**
     * Constructs an empty 64x64 GreasedRegion.
     * GreasedRegions are mutable, so you can add to this with insert() or insertSeveral(), among others.
     */
    public GreasedRegion()
    {
        width = 64;
        height = 64;
        ySections = 1;
        yEndMask = -1L;
        data = new long[64];
    }

    /**
     * Constructs a GreasedRegion with the given rectangular boolean array, with width of bits.length and height of
     * bits[0].length, any value of true considered "on", and any value of false considered "off."
     * @param bits a rectangular 2D boolean array where true is on and false is off
     */
    public GreasedRegion(final boolean[][] bits)
    {
        width = bits.length;
        height = bits[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(bits[x][y]) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }
    /**
     * Reassigns this GreasedRegion with the given rectangular boolean array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then any value of true in map is considered "on", and any value of false in map is
     * considered "off."
     * @param map a rectangular 2D boolean array where true is on and false is off
     * @return this for chaining
     */
    public GreasedRegion refill(final boolean[][] map) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= (map[x][y] ? 1L : 0L) << (y & 63);
                }
            }
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(map[x][y]) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            return this;
        }
    }

    /**
     * Constructs a GreasedRegion with the given rectangular char array, with width of map.length and height of
     * map[0].length, any value that equals yes is considered "on", and any other value considered "off."
     * @param map a rectangular 2D char array where yes is on and everything else is off
     * @param yes which char to encode as "on"
     */
    public GreasedRegion(final char[][] map, final char yes)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] == yes) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }
    /**
     * Reassigns this GreasedRegion with the given rectangular char array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then any value that equals yes is considered "on", and any other value considered "off."
     * @param map a rectangular 2D char array where yes is on and everything else is off
     * @param yes which char to encode as "on"
     * @return this for chaining
     */
    public GreasedRegion refill(final char[][] map, final char yes) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= ((map[x][y] == yes) ? 1L : 0L) << (y & 63);
                }
            }
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(map[x][y] == yes) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            return this;
        }
    }

    /**
     * Weird constructor that takes a String array, _as it would be printed_, so each String is a row and indexing would
     * be done with y, x instead of the normal x, y.
     * @param map String array (as printed, not the normal storage) where each String is a row
     * @param yes the char to consider "on" in the GreasedRegion
     */
    public GreasedRegion(final String[] map, final char yes)
    {
        height = map.length;
        width = map[0].length();
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[y].charAt(x) == yes) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }

    /**
     * Weird refill method that takes a String array, _as it would be printed_, so each String is a row and indexing
     * would be done with y, x instead of the normal x, y.
     * @param map String array (as printed, not the normal storage) where each String is a row
     * @param yes the char to consider "on" in the GreasedRegion
     * @return this for chaining
     */
    public GreasedRegion refill(final String[] map, final char yes) {
        if (map != null && map.length > 0 && height == map.length && width == map[0].length()) {
            Arrays.fill(data, 0L);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= ((map[y].charAt(x) == yes) ? 1L : 0L) << (y & 63);
                }
            }
            return this;
        } else {
            height = (map == null) ? 0 : map.length;
            width = (map == null || map.length <= 0) ? 0 : map[0].length();
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(map[y].charAt(y) == yes) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            return this;
        }
    }
    /**
     * Constructs a GreasedRegion with the given rectangular int array, with width of map.length and height of
     * map[0].length, any value that equals yes is considered "on", and any other value considered "off."
     * @param map a rectangular 2D int array where an int == yes is on and everything else is off
     * @param yes which int to encode as "on"
     */
    public GreasedRegion(final int[][] map, final int yes)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] == yes) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }
    /**
     * Reassigns this GreasedRegion with the given rectangular int array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then any value that equals yes is considered "on", and any other value considered "off."
     * @param map a rectangular 2D int array where an int == yes is on and everything else is off
     * @param yes which int to encode as "on"
     * @return this for chaining
     */
    public GreasedRegion refill(final int[][] map, final int yes) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= ((map[x][y] == yes) ? 1L : 0L) << (y & 63);
                }
            }
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(map[x][y] == yes) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            return this;
        }
    }

    /**
     * Constructs this GreasedRegion using an int[][], treating cells as on if they are greater than or equal to lower
     * and less than upper, or off otherwise.
     * @param map an int[][] that should have some ints between lower and upper
     * @param lower lower bound, inclusive; all on cells will have values in map that are at least equal to lower
     * @param upper upper bound, exclusive; all on cells will have values in map that are less than upper
     */
    public GreasedRegion(final int[][] map, final int lower, final int upper)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        int[] column;
        for (int x = 0; x < width; x++) {
            column = map[x];
            for (int y = 0; y < height; y++) {
                if(column[y] >= lower && column[y] < upper) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }

    /**
     * Reassigns this GreasedRegion with the given rectangular int array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then cells are treated as on if they are greater than or equal to lower and less than
     * upper, or off otherwise.
     * @param map a rectangular 2D int array that should have some values between lower and upper
     * @param lower lower bound, inclusive; all on cells will have values in map that are at least equal to lower
     * @param upper upper bound, exclusive; all on cells will have values in map that are less than upper
     * @return this for chaining
     */
    public GreasedRegion refill(final int[][] map, final int lower, final int upper) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            int[] column;
            for (int x = 0; x < width; x++) {
                column = map[x];
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= ((column[y] >= lower && column[y] < upper) ? 1L : 0L) << (y & 63);
                }
            }
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            int[] column;
            for (int x = 0; x < width; x++) {
                column = map[x];
                for (int y = 0; y < height; y++) {
                    if(column[y] >= lower && column[y] < upper) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            return this;
        }
    }

    /**
     * Constructs this GreasedRegion using a short[][], treating cells as on if they are greater than or equal to lower
     * and less than upper, or off otherwise.
     * @param map a short[][] that should have some shorts between lower and upper
     * @param lower lower bound, inclusive; all on cells will have values in map that are at least equal to lower
     * @param upper upper bound, exclusive; all on cells will have values in map that are less than upper
     */
    public GreasedRegion(final short[][] map, final int lower, final int upper)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        short[] column;
        for (int x = 0; x < width; x++) {
            column = map[x];
            for (int y = 0; y < height; y++) {
                if(column[y] >= lower && column[y] < upper) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }

    /**
     * Reassigns this GreasedRegion with the given rectangular short array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then cells are treated as on if they are greater than or equal to lower and less than
     * upper, or off otherwise.
     * @param map a rectangular 2D short array that should have some values between lower and upper
     * @param lower lower bound, inclusive; all on cells will have values in map that are at least equal to lower
     * @param upper upper bound, exclusive; all on cells will have values in map that are less than upper
     * @return this for chaining
     */
    public GreasedRegion refill(final short[][] map, final int lower, final int upper) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            short[] column;
            for (int x = 0; x < width; x++) {
                column = map[x];
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= ((column[y] >= lower && column[y] < upper) ? 1L : 0L) << (y & 63);
                }
            }
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            short[] column;
            for (int x = 0; x < width; x++) {
                column = map[x];
                for (int y = 0; y < height; y++) {
                    if(column[y] >= lower && column[y] < upper) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            return this;
        }
    }
    /**
     * Constructs this GreasedRegion using a double[][] (typically one generated by
     * {@link squidpony.squidai.DijkstraMap}) that only stores two relevant states:  an "on" state for values less than
     * or equal to upperBound (inclusive), and an "off" state for anything else.
     * @param map a double[][] that probably relates in some way to DijkstraMap.
     * @param upperBound upper inclusive; any double greater than this will be off, any others will be on
     */
    public GreasedRegion(final double[][] map, final double upperBound)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] <= upperBound)
                    data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }
    /**
     * Reassigns this GreasedRegion with the given rectangular double array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then cells are treated as on if they are less than or equal to upperBound, or off
     * otherwise.
     * @param map a rectangular 2D double array that should usually have some values less than or equal to upperBound
     * @param upperBound upper bound, inclusive; all on cells will have values in map that are less than or equal to this
     * @return this for chaining
     */
    public GreasedRegion refill(final double[][] map, final double upperBound) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(map[x][y] <= upperBound) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(map[x][y] <= upperBound) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            return this;
        }
    }

    /**
     * Constructs this GreasedRegion using a double[][] (typically one generated by
     * {@link squidpony.squidai.DijkstraMap}) that only stores two relevant states:  an "on" state for values between
     * lowerBound (inclusive) and upperBound (exclusive), and an "off" state for anything else.
     * @param map a double[][] that probably relates in some way to DijkstraMap.
     * @param lowerBound lower inclusive; any double lower than this will be off, any equal to or greater than this,
     *                   but less than upper, will be on
     * @param upperBound upper exclusive; any double greater than or equal to this this will be off, any doubles both
     *                   less than this and equal to or greater than lower will be on
     */
    public GreasedRegion(final double[][] map, final double lowerBound, final double upperBound)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] >= lowerBound && map[x][y] < upperBound)
                    data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }
    /**
     * Reassigns this GreasedRegion with the given rectangular double array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then cells are treated as on if they are greater than or equal to lower and less than
     * upper, or off otherwise.
     * @param map a rectangular 2D double array that should have some values between lower and upper
     * @param lower lower bound, inclusive; all on cells will have values in map that are at least equal to lower
     * @param upper upper bound, exclusive; all on cells will have values in map that are less than upper
     * @return this for chaining
     */
    public GreasedRegion refill(final double[][] map, final double lower, final double upper) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            double[] column;
            for (int x = 0; x < width; x++) {
                column = map[x];
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= ((column[y] >= lower && column[y] < upper) ? 1L : 0L) << (y & 63);
                }
            }
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            double[] column;
            for (int x = 0; x < width; x++) {
                column = map[x];
                for (int y = 0; y < height; y++) {
                    if(column[y] >= lower && column[y] < upper) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            return this;
        }
    }

    /**
     * Constructs this GreasedRegion using a double[][] (this was made for use inside
     * {@link squidpony.squidgrid.BevelFOV}) that only stores two relevant states:  an "on" state for values between
     * lowerBound (inclusive) and upperBound (exclusive), and an "off" state for anything else. This variant scales the
     * input so each "on" position in map produces a 2x2 on area if scale is 2, a 3x3 area if scale is 3, and so on.
     * @param map a double[][] that may relate in some way to BevelFOV
     * @param lowerBound lower inclusive; any double lower than this will be off, any equal to or greater than this,
     *                   but less than upper, will be on
     * @param upperBound upper exclusive; any double greater than or equal to this this will be off, any doubles both
     *                   less than this and equal to or greater than lower will be on
     * @param scale      the size of the square of cells in this that each "on" value in map will correspond to
     */
    public GreasedRegion(final double[][] map, final double lowerBound, final double upperBound, int scale)
    {
        scale = Math.min(63, Math.max(1, scale));
        int baseWidth = map.length, baseHeight = map[0].length;
        width =  baseWidth * scale;
        height = baseHeight * scale;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        long shape = (1L << scale) - 1L, leftover;
        for (int bx = 0, x = 0; bx < baseWidth; bx++, x += scale) {
            for (int by = 0, y = 0; by < baseHeight; by++, y += scale) {
                if(map[bx][by] >= lowerBound && map[bx][by] < upperBound) {
                    for (int i = 0; i < scale; i++) {
                        data[(x + i) * ySections + (y >> 6)] |= shape << (y & 63);
                        if((leftover = (y + scale - 1 & 63) + 1) < (y & 63) + 1 && (y + leftover >> 6) < ySections)
                        {
                            data[(x + i) * ySections + (y >> 6) + 1] |= (1L << leftover) - 1L;
                        }
                    }
                }
            }
        }
    }
    /**
     * Reassigns this GreasedRegion with the given rectangular double array, reusing the current data storage (without
     * extra allocations) if {@code this.width == map.length * scale && this.height == map[0].length * scale}. The
     * current values stored in this are always cleared, then cells are treated as on if they are greater than or equal
     * to lower and less than upper, or off otherwise, before considering scaling. This variant scales the input so each
     * "on" position in map produces a 2x2 on area if scale is 2, a 3x3 area if scale is 3, and so on.
     * @param map a double[][] that may relate in some way to BevelFOV
     * @param lowerBound lower inclusive; any double lower than this will be off, any equal to or greater than this,
     *                   but less than upper, will be on
     * @param upperBound upper exclusive; any double greater than or equal to this this will be off, any doubles both
     *                   less than this and equal to or greater than lower will be on
     * @param scale      the size of the square of cells in this that each "on" value in map will correspond to
     * @return this for chaining
     */
    public GreasedRegion refill(final double[][] map, final double lowerBound, final double upperBound, int scale) {
        scale = Math.min(63, Math.max(1, scale));
        if (map != null && map.length > 0 && width == map.length * scale && height == map[0].length * scale) {
            Arrays.fill(data, 0L);
            double[] column;
            int baseWidth = map.length, baseHeight = map[0].length;
            long shape = (1L << scale) - 1L, leftover;
            for (int bx = 0, x = 0; bx < baseWidth; bx++, x += scale) {
                column = map[bx];
                for (int by = 0, y = 0; by < baseHeight; by++, y += scale) {
                    if(column[by] >= lowerBound && column[by] < upperBound) {
                        for (int i = 0; i < scale; i++) {
                            data[(x + i) * ySections + (y >> 6)] |= shape << (y & 63);
                            if((leftover = (y + scale - 1 & 63) + 1) < (y & 63) + 1 && (y + leftover >> 6) < ySections)
                            {
                                data[(x + i) * ySections + (y >> 6) + 1] |= (1L << leftover) - 1L;
                            }
                        }
                    }
                }
            }
            return this;
        } else {
            int baseWidth = (map == null) ? 0 : map.length,
                    baseHeight = (map == null || map.length <= 0) ? 0 : map[0].length;
            width =  baseWidth * scale;
            height = baseHeight * scale;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            long shape = (1L << scale) - 1L, leftover;
            for (int bx = 0, x = 0; bx < baseWidth; bx++, x += scale) {
                for (int by = 0, y = 0; by < baseHeight; by++, y += scale) {
                    if(map[bx][by] >= lowerBound && map[bx][by] < upperBound) {
                        for (int i = 0; i < scale; i++) {
                            data[(x + i) * ySections + (y >> 6)] |= shape << (y & 63);
                            if((leftover = (y + scale - 1 & 63)) < y && y < height - leftover)
                            {
                                data[(x + i) * ySections + (y >> 6) + 1] |= (1L << leftover) - 1L;
                            }
                        }
                    }
                }
            }
            return this;
        }
    }

    /**
     * Constructs a GreasedRegion with the given 1D boolean array, with the given width and height, where an [x][y]
     * position is obtained from bits given an index n with x = n / height, y = n % height, any value of true
     * considered "on", and any value of false considered "off."
     * @param bits a 1D boolean array where true is on and false is off
     * @param width the width of the desired GreasedRegion; width * height should equal bits.length
     * @param height the height of the desired GreasedRegion; width * height should equal bits.length
     */
    public GreasedRegion(final boolean[] bits, final int width, final int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int a = 0, x = 0, y = 0; a < bits.length; a++, x = a / height, y = a % height) {
            if(bits[a]) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
        }
    }
    /**
     * Reassigns this GreasedRegion with the given 1D boolean array, reusing the current data storage (without
     * extra allocations) if this.width == width and this.height == height, where an [x][y]
     * position is obtained from bits given an index n with x = n / height, y = n % height, any value of true
     * considered "on", and any value of false considered "off."
     * @param bits a 1D boolean array where true is on and false is off
     * @param width the width of the desired GreasedRegion; width * height should equal bits.length
     * @param height the height of the desired GreasedRegion; width * height should equal bits.length
     * @return this for chaining
     */
    public GreasedRegion refill(final boolean[] bits, final int width, final int height) {
        if (bits != null && this.width == width && this.height == height) {
            Arrays.fill(data, 0L);
            for (int a = 0, x = 0, y = 0; a < bits.length; a++, x = a / height, y = a % height) {
                data[x * ySections + (y >> 6)] |= (bits[a] ? 1L : 0L) << (y & 63);
            }
            return this;
        } else {
            this.width = (bits == null || width < 0) ? 0 : width;
            this.height = (bits == null || bits.length <= 0 || height < 0) ? 0 : height;
            ySections = (this.height + 63) >> 6;
            yEndMask = -1L >>> (64 - (this.height & 63));
            data = new long[this.width * ySections];
            if(bits != null) {
                for (int a = 0, x = 0, y = 0; a < bits.length; a++, x = a / this.height, y = a % this.height) {
                    if (bits[a]) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            return this;
        }
    }

    /**
     * Constructor for an empty GreasedRegion of the given width and height.
     * GreasedRegions are mutable, so you can add to this with insert() or insertSeveral(), among others.
     * @param width the maximum width for the GreasedRegion
     * @param height the maximum height for the GreasedRegion
     */
    public GreasedRegion(final int width, final int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
    }

    /**
     * If this GreasedRegion has the same width and height passed as parameters, this acts the same as {@link #empty()},
     * makes no allocations, and returns this GreasedRegion with its contents all "off"; otherwise, this does allocate
     * a differently-sized amount of internal data to match the new width and height, sets the fields to all match the
     * new width and height, and returns this GreasedRegion with its new width and height, with all contents "off". This
     * is meant for cases where a GreasedRegion may be reused effectively, but its size may not always be the same.
     * @param width the width to potentially resize this GreasedRegion to
     * @param height the height to potentially resize this GreasedRegion to
     * @return this GreasedRegion, always with all contents "off", and with the height and width set.
     */
    public GreasedRegion resizeAndEmpty(final int width, final int height) {
        if (width == this.width && height == this.height) {
            Arrays.fill(data, 0L);
        } else {
            this.width = (width <= 0) ? 0 : width;
            this.height = (height <= 0) ? 0 : height;
            ySections = (this.height + 63) >> 6;
            yEndMask = -1L >>> (64 - (this.height & 63));
            data = new long[this.width * ySections];
        }
        return this;

    }

    /**
     * Constructor for a GreasedRegion that contains a single "on" cell, and has the given width and height.
     * Note that to avoid confusion with the constructor that takes multiple Coord values, this takes the single "on"
     * Coord first, while the multiple-Coord constructor takes its vararg or array of Coords last.
     * @param single the one (x,y) point to store as "on" in this GreasedRegion
     * @param width the maximum width for the GreasedRegion
     * @param height the maximum height for the GreasedRegion
     */
    public GreasedRegion(final Coord single, final int width, final int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];

        if(single.x < width && single.y < height && single.x >= 0 && single.y >= 0)
            data[single.x * ySections + (single.y >> 6)] |= 1L << (single.y & 63);
    }

    /**
     * Constructor for a GreasedRegion that can have several "on" cells specified, and has the given width and height.
     * Note that to avoid confusion with the constructor that takes one Coord value, this takes the vararg or array of
     * Coords last, while the single-Coord constructor takes its one Coord first.
     * @param width the maximum width for the GreasedRegion
     * @param height the maximum height for the GreasedRegion
     * @param points an array or vararg of Coord to store as "on" in this GreasedRegion
     */
    public GreasedRegion(final int width, final  int height, final Coord... points)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        if(points != null)
        {
            for (int i = 0, x, y; i < points.length; i++) {
                x = points[i].x;
                y = points[i].y;
                if(x < width && y < height && x >= 0 && y >= 0)
                    data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }

    /**
     * Constructor for a GreasedRegion that can have several "on" cells specified, and has the given width and height.
     * Note that to avoid confusion with the constructor that takes one Coord value, this takes the Iterable of
     * Coords last, while the single-Coord constructor takes its one Coord first.
     * @param width the maximum width for the GreasedRegion
     * @param height the maximum height for the GreasedRegion
     * @param points an array or vararg of Coord to store as "on" in this GreasedRegion
     */
    public GreasedRegion(final int width, final int height, final Iterable<Coord> points)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        if(points != null) {
            int x, y;
            for (Coord c : points) {
                x = c.x;
                y = c.y;
                if (x < width && y < height && x >= 0 && y >= 0)
                    data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
    }

    /**
     * Constructor for a random GreasedRegion of the given width and height, typically assigning approximately half of
     * the cells in this to "on" and the rest to off. A RandomnessSource can be slightly more efficient than an RNG when
     * you're making a lot of calls on it.
     * @param random a RandomnessSource that should have a good nextLong() method; LightRNG is excellent but Lathe32RNG is faster on GWT (only there) 
     * @param width the maximum width for the GreasedRegion
     * @param height the maximum height for the GreasedRegion
     */
    public GreasedRegion(final RandomnessSource random, final int width, final int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int i = 0; i < width * ySections; i++) {
            data[i] = random.nextLong();
        }
        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
    }
    /**
     * Reassigns this GreasedRegion by filling it with random values from random, reusing the current data storage
     * (without extra allocations) if this.width == width and this.height == height, and typically assigning
     * approximately half of the cells in this to "on" and the rest to off. A RandomnessSource can be slightly more
     * efficient than an RNG when you're making a lot of calls on it.
     * @param random a RandomnessSource that should have a good nextLong() method; LightRNG is excellent but Lathe32RNG is faster on GWT (only there) 
     * @param width the width of the desired GreasedRegion
     * @param height the height of the desired GreasedRegion
     * @return this for chaining
     */
    public GreasedRegion refill(final RandomnessSource random, final int width, final int height) {
        if (random != null){
            if(this.width == width && this.height == height) {
                for (int i = 0; i < width * ySections; i++) {
                    data[i] = random.nextLong();
                }
            } else {
                this.width = (width <= 0) ? 0 : width;
                this.height = (height <= 0) ? 0 : height;
                ySections = (this.height + 63) >> 6;
                yEndMask = -1L >>> (64 - (this.height & 63));
                data = new long[this.width * ySections];
                for (int i = 0; i < this.width * ySections; i++) {
                    data[i] = random.nextLong();
                }
            }
            if(ySections > 0 && yEndMask != -1) {
                for (int a = ySections - 1; a < data.length; a += ySections) {
                    data[a] &= yEndMask;
                }
            }
        }
        return this;
    }

    /**
     * Constructor for a random GreasedRegion of the given width and height, typically assigning approximately half of
     * the cells in this to "on" and the rest to off. A RandomnessSource can be slightly more efficient than an RNG when
     * you're making a lot of calls on it, so you may prefer {@link #GreasedRegion(RandomnessSource, int, int)}.
     * @param random an IRNG, such as an RNG, that this will use to generate its contents
     * @param width the maximum width for the GreasedRegion
     * @param height the maximum height for the GreasedRegion
     */
    public GreasedRegion(final IRNG random, final int width, final int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int i = 0; i < width * ySections; i++) {
            data[i] = random.nextLong();
        }
        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
    }
    /**
     * Reassigns this GreasedRegion by filling it with random values from random, reusing the current data storage
     * (without extra allocations) if this.width == width and this.height == height, and typically assigning
     * approximately half of the cells in this to "on" and the rest to off.
     * @param random an IRNG that should have a good nextLong() method; an RNG constructed with the default RandomnessSource will be fine
     * @param width the width of the desired GreasedRegion
     * @param height the height of the desired GreasedRegion
     * @return this for chaining
     */
    public GreasedRegion refill(final IRNG random, final int width, final int height) {
        if (random != null){
            if(this.width == width && this.height == height) {
                for (int i = 0; i < width * ySections; i++) {
                    data[i] = random.nextLong();
                }
            } else {
                this.width = (width <= 0) ? 0 : width;
                this.height = (height <= 0) ? 0 : height;
                ySections = (this.height + 63) >> 6;
                yEndMask = -1L >>> (64 - (this.height & 63));
                data = new long[this.width * ySections];
                for (int i = 0; i < this.width * ySections; i++) {
                    data[i] = random.nextLong();
                }
            }
            if(ySections > 0 && yEndMask != -1) {
                for (int a = ySections - 1; a < data.length; a += ySections) {
                    data[a] &= yEndMask;
                }
            }
        }
        return this;
    }

    /**
     * Constructor for a random GreasedRegion of the given width and height, trying to set the given fraction of cells
     * to on. Depending on the value of fraction, this makes between 0 and 6 calls to the nextLong() method of random's
     * internal RandomnessSource, per 64 cells of this GreasedRegion (if height is not a multiple of 64, round up to get
     * the number of calls this makes). As such, this sacrifices the precision of the fraction to obtain significantly
     * better speed than generating one random number per cell, although the precision is probably good enough (fraction
     * is effectively rounded down to the nearest multiple of 0.015625, and clamped between 0.0 and 1.0). The parameter
     * {@code random} can be an object like a {@link LightRNG}, an {@link RNG} backed by a well-distributed
     * RandomnessSource like its default, LightRNG, a {@link GWTRNG} (especially if you target GWT, where it will
     * perform much better than most alternatives), or any of various other RandomnessSource implementations that
     * distribute bits well for {@link RandomnessSource#nextLong()}, but should not be intentionally-biased RNGs like
     * {@link DharmaRNG} or {@link EditRNG}, nor double-based QRNGs like {@link VanDerCorputQRNG} or {@link SobolQRNG}.
     * @param random a RandomnessSource that should produce high-quality long values, like the defaults for {@link RNG}
     * @param fraction between 0.0 and 1.0 (clamped), only considering a precision of 1/64.0 (0.015625) between steps
     * @param width the maximum width for the GreasedRegion
     * @param height the maximum height for the GreasedRegion
     */
    public GreasedRegion(final RandomnessSource random, final double fraction, final int width, final int height)
    {
        this.width = width;
        this.height = height;
        int bitCount = (int) (fraction * 64);
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int i = 0; i < width * ySections; i++) {
            data[i] = approximateBits(random, bitCount);
        }
        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
    }
    /**
     * Reassigns this GreasedRegion randomly, reusing the current data storage (without extra allocations) if this.width
     * == width and this.height == height, while trying to set the given fraction of cells to on. Depending on the value
     * of fraction, this makes between 0 and 6 calls to the nextLong() method of random's internal RandomnessSource, per
     * 64 cells of this GreasedRegion (if height is not a multiple of 64, round up to get the number of calls this
     * makes). As such, this sacrifices the precision of the fraction to obtain significantly better speed than
     * generating one random number per cell, although the precision is probably good enough (fraction is effectively
     * rounded down to the nearest multiple of 0.015625, and clamped between 0.0 and 1.0). The parameter {@code random}
     * can be an object like a {@link LightRNG}, an {@link RNG} backed by a well-distributed RandomnessSource like its
     * default, LightRNG, a {@link GWTRNG} (especially if you target GWT, where it will perform much better than most
     * alternatives), or any of various other RandomnessSource implementations that distribute bits well for
     * {@link RandomnessSource#nextLong()}, but should not be intentionally-biased RNGs like {@link DharmaRNG} or
     * {@link EditRNG}, nor double-based QRNGs like {@link VanDerCorputQRNG} or {@link SobolQRNG}.
     * @param random a RandomnessSource that should produce high-quality long values, like the defaults for {@link RNG}
     * @param fraction between 0.0 and 1.0 (clamped), only considering a precision of 1/64.0 (0.015625) between steps
     * @param width the maximum width for the GreasedRegion
     * @param height the maximum height for the GreasedRegion
     * @return this for chaining
     */
    public GreasedRegion refill(final RandomnessSource random, final double fraction, final int width, final int height) {
        if (random != null){
            int bitCount = (int) (fraction * 64);
            if(this.width == width && this.height == height) {
                for (int i = 0; i < width * ySections; i++) {
                    data[i] = approximateBits(random, bitCount);
                }
            } else {
                this.width = (width <= 0) ? 0 : width;
                this.height = (height <= 0) ? 0 : height;
                ySections = (this.height + 63) >> 6;
                yEndMask = -1L >>> (64 - (this.height & 63));
                data = new long[this.width * ySections];
                for (int i = 0; i < this.width * ySections; i++) {
                    data[i] = approximateBits(random, bitCount);
                }
            }
            if(ySections > 0 && yEndMask != -1) {
                for (int a = ySections - 1; a < data.length; a += ySections) {
                    data[a] &= yEndMask;
                }
            }

        }
        return this;
    }

    /**
     * Copy constructor that takes another GreasedRegion and copies all of its data into this new one.
     * If you find yourself frequently using this constructor and assigning it to the same variable, consider using the
     * {@link #remake(GreasedRegion)} method on the variable instead, which will, if it has the same width and height
     * as the other GreasedRegion, avoid creating garbage and quickly fill the variable with the other's contents.
     * @see #copy() for a convenience method that just uses this constructor
     * @param other another GreasedRegion that will be copied into this new GreasedRegion
     */
    public GreasedRegion(final GreasedRegion other)
    {
        width = other.width;
        height = other.height;
        ySections = other.ySections;
        yEndMask = other.yEndMask;
        data = new long[width * ySections];
        System.arraycopy(other.data, 0, data, 0, width * ySections);
    }

    /**
     * Primarily for internal use, this constructor copies data2 exactly into the internal long array the new
     * GreasedRegion will use, and does not perform any validation steps to ensure that cells that would be "on" but are
     * outside the actual height of the GreasedRegion are actually removed (this only matters if height is not a
     * multiple of 64).
     * @param data2 a long array that is typically from another GreasedRegion, and would be hard to make otherwise
     * @param width the width of the GreasedRegion to construct
     * @param height the height of the GreasedRegion to construct
     */
    public GreasedRegion(final long[] data2, final int width, final int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        System.arraycopy(data2, 0, data, 0, width * ySections);
        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
    }

    /**
     * Primarily for internal use, this constructor copies data2 into the internal long array the new GreasedRegion will
     * use, but treats data2 as having the dimensions [dataWidth][dataHeight], and uses the potentially-different
     * dimensions [width][height] for the constructed GreasedRegion. This will truncate data2 on width, height, or both
     * if width or height is smaller than dataWidth or dataHeight. It will fill extra space with all "off" if width or
     * height is larger than dataWidth or dataHeight. It will interpret data2 as the same 2D shape regardless of the
     * width or height it is being assigned to, and data2 will not be reshaped by truncation.
     * @param data2 a long array that is typically from another GreasedRegion, and would be hard to make otherwise
     * @param dataWidth the width to interpret data2 as having
     * @param dataHeight the height to interpret data2 as having
     * @param width the width of the GreasedRegion to construct
     * @param height the height of the GreasedRegion to construct
     */
    public GreasedRegion(final long[] data2, final int dataWidth, final int dataHeight, final int width, final int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];

        final int ySections2 = (dataHeight + 63) >> 6;
        if(ySections2 == 0)
            return;
        if(ySections == 1) {
            System.arraycopy(data2, 0, data, 0, dataWidth);
        }
        else
        {
            if(dataHeight >= height) {
                for (int i = 0, j = 0; i < width && i < dataWidth; i += ySections2, j += ySections) {
                    System.arraycopy(data2, i, data, j, ySections);
                }
            }
            else
            {
                for (int i = 0, j = 0; i < width && i < dataWidth; i += ySections2, j += ySections) {
                    System.arraycopy(data2, i, data, j, ySections2);
                }
            }
        }
        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
    }
    /**
     * Primarily for internal use, this method copies data2 into the internal long array the new GreasedRegion will
     * use, but treats data2 as having the dimensions [dataWidth][dataHeight], and uses the potentially-different
     * dimensions [width][height] for this GreasedRegion, potentially re-allocating the internal data this uses if width
     * and/or height are different from what they were. This will truncate data2 on width, height, or both if width or
     * height is smaller than dataWidth or dataHeight. It will fill extra space with all "off" if width or height is
     * larger than dataWidth or dataHeight. It will interpret data2 as the same 2D shape regardless of the width or
     * height it is being assigned to, and data2 will not be reshaped by truncation.
     * @param data2 a long array that is typically from another GreasedRegion, and would be hard to make otherwise
     * @param dataWidth the width to interpret data2 as having
     * @param dataHeight the height to interpret data2 as having
     * @param width the width to set this GreasedRegion to have
     * @param height the height to set this GreasedRegion to have
     */
    public GreasedRegion refill(final long[] data2, final int dataWidth, final int dataHeight, final int width, final int height)
    {
        if(width != this.width || height != this.height) {
            this.width = width;
            this.height = height;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
        }
        else {
            Arrays.fill(data, 0L);
        }
        final int ySections2 = (dataHeight + 63) >> 6;
        if(ySections2 == 0)
            return this;
        if(ySections == 1) {
            System.arraycopy(data2, 0, data, 0, dataWidth);
        }
        else
        {
            if(dataHeight >= height) {
                for (int i = 0, j = 0; i < width && i < dataWidth; i += ySections2, j += ySections) {
                    System.arraycopy(data2, i, data, j, ySections);
                }
            }
            else
            {
                for (int i = 0, j = 0; i < width && i < dataWidth; i += ySections2, j += ySections) {
                    System.arraycopy(data2, i, data, j, ySections2);
                }
            }
        }
        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        return this;
    }

    /**
     * A useful method for efficiency, remake() reassigns this GreasedRegion to have its contents replaced by other. If
     * other and this GreasedRegion have identical width and height, this is very efficient and performs no additional
     * allocations, simply replacing the cell data in this with the cell data from other. If width and height are not
     * both equal between this and other, this does allocate a new data array, but still reassigns this GreasedRegion
     * in-place and acts similarly to when width and height are both equal (it just uses some more memory).
     * <br>
     * Using remake() or the similar refill() methods in chains of operations on multiple GreasedRegions can be key to
     * maintaining good performance and memory usage. You often can recycle a no-longer-used GreasedRegion by assigning
     * a GreasedRegion you want to keep to it with remake(), then mutating either the remade value or the one that was
     * just filled into this but keeping one version around for later usage.
     * @param other another GreasedRegion to replace the data in this GreasedRegion with
     * @return this for chaining
     */
    public GreasedRegion remake(GreasedRegion other) {
        if (width == other.width && height == other.height) {
            System.arraycopy(other.data, 0, data, 0, width * ySections);
            return this;
        } else {
            width = other.width;
            height = other.height;
            ySections = other.ySections;
            yEndMask = other.yEndMask;
            data = new long[width * ySections];
            System.arraycopy(other.data, 0, data, 0, width * ySections);
            return this;
        }
    }

    /**
     * Changes the width and/or height of this GreasedRegion, enlarging or shrinking starting at the edges where
     * {@code x == width - 1} and {@code y == height - 1}. There isn't an especially efficient way to expand from the
     * other edges, but this method is able to copy data in bulk, so at least this method should be very fast. You can
     * use {@code insert(int, int, GreasedRegion)} if you want to place one GreasedRegion inside another one,
     * potentially with a different size. The space created by any enlargement starts all off; shrinking doesn't change
     * the existing data where it isn't removed by the shrink.
     * @param widthChange the amount to change width by; can be positive, negative, or zero
     * @param heightChange the amount to change height by; can be positive, negative, or zero
     * @return this for chaining
     */
    public GreasedRegion alterBounds(int widthChange, int heightChange)
    {
        int newWidth = width + widthChange;
        int newHeight = height + heightChange;
        if(newWidth <= 0 || newHeight <= 0)
        {
            width = 0;
            height = 0;
            ySections= 0;
            yEndMask = -1;
            data = new long[0];
            return this;
        }
        int newYSections = (newHeight + 63) >> 6;
        yEndMask = -1L >>> (64 - (newHeight & 63));
        long[] newData = new long[newWidth * newYSections];
        for (int x = 0; x < width && x < newWidth; x++) {
            for (int ys = 0; ys < ySections && ys < newYSections; ys++) {
                newData[x * newYSections + ys] = data[x * ySections + ys];
            }
        }
        ySections = newYSections;
        width = newWidth;
        height = newHeight;
        data = newData;
        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        return this;
    }

    /**
     * Sets the cell at x,y to on if value is true or off if value is false. Does nothing if x,y is out of bounds.
     * @param value the value to set in the cell
     * @param x the x-position of the cell
     * @param y the y-position of the cell
     * @return this for chaining
     */
    public GreasedRegion set(boolean value, int x, int y)
    {
        if(x < width && y < height && x >= 0 && y >= 0) {
            if(value)
                data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            else
                data[x * ySections + (y >> 6)] &= ~(1L << (y & 63));
        }
        return this;
    }

    /**
     * Sets the cell at point to on if value is true or off if value is false. Does nothing if point is out of bounds,
     * or if point is null.
     * @param value the value to set in the cell
     * @param point the x,y Coord of the cell to set
     * @return this for chaining
     */
    public GreasedRegion set(boolean value, Coord point)
    {
        if(point == null) return this;
        return set(value, point.x, point.y);
    }

    /**
     * Sets the cell at x,y to "on". Does nothing if x,y is out of bounds.
     * More efficient, slightly, than {@link #set(boolean, int, int)} if you just need to set a cell to "on".
     * @param x the x-position of the cell
     * @param y the y-position of the cell
     * @return this for chaining
     */
    public GreasedRegion insert(int x, int y)
    {
        if(x < width && y < height && x >= 0 && y >= 0)
            data[x * ySections + (y >> 6)] |= 1L << (y & 63);
        return this;
    }

    /**
     * Sets the given cell, "tightly" encoded for a specific width/height as by {@link #asTightEncoded()}, to "on".
     * Does nothing if the cell is out of bounds.
     * @param tight a cell tightly encoded for this GreasedRegion's width and height
     * @return this for chaining
     */
    public GreasedRegion insert(int tight)
    {
        if(tight < width * height && tight >= 0)
            data[(tight % width) * ySections + ((tight / width) >>> 6)] |= 1L << ((tight / width) & 63);
        return this;
    }
    /**
     * Sets the cell at point to "on". Does nothing if point is out of bounds, or if point is null.
     * More efficient, slightly, than {@link #set(boolean, Coord)} if you just need to set a cell to "on".
     * @param point the x,y Coord of the cell
     * @return this for chaining
     */
    public GreasedRegion insert(Coord point)
    {

        if(point == null) return this;
        return insert(point.x, point.y);
    }

    /**
     * Takes another GreasedRegion, called other, with potentially different size and inserts its "on" cells into thi
     * GreasedRegion at the given x,y offset, allowing negative x and/or y to put only part of other in this.
     * <br>
     * This is a rather complex method internally, but should be about as efficient as a general insert-region method
     * can be.
     * @param x the x offset to start inserting other at; may be negative
     * @param y the y offset to start inserting other at; may be negative
     * @param other the other GreasedRegion to insert
     * @return this for chaining
     */
    public GreasedRegion insert(int x, int y, GreasedRegion other)
    {
        if(other == null || other.ySections <= 0 || other.width <= 0)
            return this;

        int start = Math.max(0, x), len = Math.min(width, Math.min(other.width, other.width + x) - start),
        oys = other.ySections, jump = (y == 0) ? 0 : (y < 0) ? -(1-y >>> 6) : (y-1 >>> 6), lily = (y < 0) ? -(-y & 63) : (y & 63),
        originalJump = Math.max(0, -jump), alterJump = Math.max(0, jump);
        long[] data2 = new long[other.width * ySections];

        long prev, tmp;
        if(oys == ySections) {
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * ySections + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = other.data[j * ySections + oi];
                    }
                }
            }
        }
        else if(oys < ySections)
        {
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {// oi < oys - Math.max(0, jump)
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = other.data[j * oys + oi];
                    }
                }
            }
        }
        else
        {
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = other.data[j * oys + oi];
                    }
                }
            }
        }

        if(lily < 0) {
            for (int i = start; i < len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L << -lily)) << (64 + lily);
                    data2[i * ySections + j] >>>= -lily;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }
        else if(lily > 0) {
            for (int i = start; i < start + len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L >>> lily)) >>> (64 - lily);
                    data2[i * ySections + j] <<= lily;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }
        len = Math.min(width, start + len);
        for (int i = start; i < len; i++) {
            for (int j = 0; j < ySections; j++) {
                data[i * ySections + j] |= data2[i * ySections + j];
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }

        return this;
    }

    public GreasedRegion insertSeveral(Coord... points)
    {
        for (int i = 0, x, y; i < points.length; i++) {
            x = points[i].x;
            y = points[i].y;
            if(x < width && y < height && x >= 0 && y >= 0)
                data[x * ySections + (y >> 6)] |= 1L << (y & 63);
        }
        return this;
    }

    public GreasedRegion insertSeveral(final int[] points)
    {
        for (int i = 0, tight; i < points.length; i++) {
            tight = points[i];
            if(tight < width * height && tight >= 0)
                data[(tight % width) * ySections + ((tight / width) >>> 6)] |= 1L << ((tight / width) & 63);
        }
        return this;
    }

    public GreasedRegion insertSeveral(Iterable<Coord> points)
    {
        int x, y;
        for (Coord pt : points) {
            x = pt.x;
            y = pt.y;
            if(x < width && y < height && x >= 0 && y >= 0)
                data[x * ySections + (y >> 6)] |= 1L << (y & 63);
        }
        return this;
    }

    public GreasedRegion insertRectangle(int startX, int startY, int rectangleWidth, int rectangleHeight)
    {
        if(rectangleWidth < 1 || rectangleHeight < 1 || ySections <= 0)
            return this;
        if(startX < 0)
            startX = 0;
        else if(startX >= width)
            startX = width - 1;
        if(startY < 0)
            startY = 0;
        else if(startY >= height)
            startY = height - 1;
        int endX = Math.min(width, startX + rectangleWidth) - 1,
                endY = Math.min(height, startY + rectangleHeight) - 1,
                startSection = startY >> 6, endSection = endY >> 6;
        if(startSection < endSection)
        {
            long startMask = -1L << (startY & 63),
                    endMask = -1L >>> (~endY & 63);
            for (int a = startX * ySections + startSection; a <= endX * ySections + startSection; a += ySections) {
                data[a] |= startMask;
            }
            if(endSection - startSection > 1)
            {
                for (int b = 1; b < endSection - startSection; b++) {
                    for (int a = startX * ySections + startSection + b; a < endX * ySections + ySections; a += ySections) {
                        data[a] = -1;
                    }
                }
            }
            for (int a = startX * ySections + endSection; a <= endX * ySections + endSection; a += ySections) {
                data[a] |= endMask;
            }
        }
        else
        {
            long mask = (-1L << (startY & 63)) & (-1L >>> (~endY & 63));
            for (int a = startX * ySections + startSection; a <= endX * ySections + startSection; a += ySections) {
                data[a] |= mask;
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        return this;
    }

    public GreasedRegion insertCircle(Coord center, int radius)
    {
        return insertSeveral(Radius.CIRCLE.pointsInside(center, radius, false, width, height));
    }

    public GreasedRegion remove(int x, int y)
    {
        if(x < width && y < height && x >= 0 && y >= 0)
            data[x * ySections + (y >> 6)] &= ~(1L << (y & 63));
        return this;
    }
    public GreasedRegion remove(Coord point)
    {
        return remove(point.x, point.y);
    }
    /**
     * Takes another GreasedRegion, called other, with potentially different size and removes its "on" cells from this
     * GreasedRegion at the given x,y offset, allowing negative x and/or y to remove only part of other in this.
     * <br>
     * This is a rather complex method internally, but should be about as efficient as a general remove-region method
     * can be. The code is identical to {@link #insert(int, int, GreasedRegion)} except that where insert only adds
     * cells, this only removes cells. Essentially, insert() is to {@link #or(GreasedRegion)} as remove() is to
     * {@link #andNot(GreasedRegion)}.
     * @param x the x offset to start removing other from; may be negative
     * @param y the y offset to start removing other from; may be negative
     * @param other the other GreasedRegion to remove
     * @return this for chaining
     */
    public GreasedRegion remove(int x, int y, GreasedRegion other)
    {
        if(other == null || other.ySections <= 0 || other.width <= 0)
            return this;

        int start = Math.max(0, x), len = Math.min(width, Math.min(other.width, other.width + x) - start),
                oys = other.ySections, jump = (y == 0) ? 0 : (y < 0) ? -(1-y >>> 6) : (y-1 >>> 6), lily = (y < 0) ? -(-y & 63) : (y & 63),
                originalJump = Math.max(0, -jump), alterJump = Math.max(0, jump);
        long[] data2 = new long[other.width * ySections];

        long prev, tmp;
        if(oys == ySections) {
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * ySections + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = other.data[j * ySections + oi];
                    }
                }
            }
        }
        else if(oys < ySections)
        {
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {// oi < oys - Math.max(0, jump)
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = other.data[j * oys + oi];
                    }
                }
            }
        }
        else
        {
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = other.data[j * oys + oi];
                    }
                }
            }
        }

        if(lily < 0) {
            for (int i = start; i < len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L << -lily)) << (64 + lily);
                    data2[i * ySections + j] >>>= -lily;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }
        else if(lily > 0) {
            for (int i = start; i < start + len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L >>> lily)) >>> (64 - lily);
                    data2[i * ySections + j] <<= lily;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }
        len = Math.min(width, start + len);
        for (int i = start; i < len; i++) {
            for (int j = 0; j < ySections; j++) {
                data[i * ySections + j] &= ~data2[i * ySections + j];
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }

        return this;
    }
    public GreasedRegion removeSeveral(Coord... points)
    {
        for (int i = 0, x, y; i < points.length; i++) {
            x = points[i].x;
            y = points[i].y;
            if(x < width && y < height && x >= 0 && y >= 0)
                data[x * ySections + (y >> 6)] &= ~(1L << (y & 63));
        }
        return this;
    }

    public GreasedRegion removeSeveral(Iterable<Coord> points)
    {
        int x, y;
        for (Coord pt : points) {
            x = pt.x;
            y = pt.y;
            if(x < width && y < height && x >= 0 && y >= 0)
                data[x * ySections + (y >> 6)] &= ~(1L << (y & 63));
        }
        return this;
    }

    public GreasedRegion removeRectangle(int startX, int startY, int rectangleWidth, int rectangleHeight)
    {
        if(startX < 0)
        {
            rectangleWidth += startX;
            startX = 0;
        }
        else if(startX >= width)
        {
            rectangleWidth = 1;
            startX = width - 1;
        }
        if(startY < 0)
        {
            rectangleHeight += startY;
            startY = 0;
        }
        else if(startY >= height)
        {
            rectangleHeight = 1;
            startY = height - 1;
        }
        if(rectangleWidth < 1 || rectangleHeight < 1 || ySections <= 0)
            return this;
        int endX = Math.min(width, startX + rectangleWidth) - 1,
                endY = Math.min(height, startY + rectangleHeight) - 1,
                startSection = startY >> 6, endSection = endY >> 6;
        if(startSection < endSection)
        {
            long startMask = ~(-1L << (startY & 63)),
                    endMask = ~(-1L >>> (~endY & 63));
            for (int a = startX * ySections + startSection; a <= endX * ySections; a += ySections) {
                data[a] &= startMask;
            }
            if(endSection - startSection > 1)
            {
                for (int b = 1; b < endSection - startSection; b++) {
                    for (int a = startX * ySections + startSection + b; a < endX * ySections + ySections; a += ySections) {
                        data[a] = 0;
                    }
                }
            }
            for (int a = startX * ySections + endSection; a <= endX * ySections + ySections; a += ySections) {
                data[a] &= endMask;
            }
        }
        else
        {
            long mask = ~((-1L << (startY & 63)) & (-1L >>> (~endY & 63)));
            for (int a = startX * ySections + startSection; a <= endX * ySections + startSection; a += ySections) {
                data[a] &= mask;
            }
        }
        return this;
    }

    public GreasedRegion removeCircle(Coord center, int radius)
    {
        return removeSeveral(Radius.CIRCLE.pointsInside(center, radius, false, width, height));
    }

    /**
     * Equivalent to {@link #clear()}, setting all cells to "off," but also returns this for chaining.
     * @return this for chaining
     */
    public GreasedRegion empty()
    {
        Arrays.fill(data, 0L);
        return this;
    }

    /**
     * Sets all cells in this to "on."
     * @return this for chaining
     */
    public GreasedRegion allOn()
    {
        if(ySections > 0)
        {
            if(yEndMask == -1) {
                Arrays.fill(data, -1);
            }
            else
            {
                for (int a = ySections - 1; a < data.length; a += ySections) {
                    data[a] = yEndMask;
                    for (int i = 0; i < ySections - 1; i++) {
                        data[a-i-1] = -1;
                    }
                }
            }
        }
        return this;
    }

    /**
     * Sets all cells in this to "on" if contents is true, or "off" if contents is false.
     * @param contents true to set all cells to on, false to set all cells to off
     * @return this for chaining
     */
    public GreasedRegion fill(boolean contents)
    {
        if(contents)
        {
            if(ySections > 0)
            {
                if(yEndMask == -1) {
                    Arrays.fill(data, -1);
                }
                else
                {
                    for (int a = ySections - 1; a < data.length; a += ySections) {
                        data[a] = yEndMask;
                        for (int i = 0; i < ySections - 1; i++) {
                            data[a-i-1] = -1;
                        }
                    }
                }
            }
            //else... what, if ySections is 0 there's nothing to do
        }
        else
        {
            Arrays.fill(data, 0L);
        }
        return this;
    }
    /**
     * Turns all cells that are adjacent to the boundaries of the GreasedRegion to "off".
     * @return this for chaining
     */
    public GreasedRegion removeEdges()
    {
        if(ySections > 0) {
            for (int i = 0; i < ySections; i++) {
                data[i] = 0L;
                data[width * ySections - 1 - i] = 0L;
            }
            if (ySections == 1) {
                for (int i = 0; i < width; i++) {
                    data[i] &= yEndMask >>> 1 & -2L;
                }
            } else {
                for (int i = ySections; i < data.length - ySections; i += ySections) {
                    data[i] &= -2L;
                }
                for (int a = ySections * 2 - 1; a < data.length - ySections; a += ySections) {
                    data[a] &= yEndMask >>> 1;
                }

            }
        }
        return this;
    }

    /**
     * Simple method that returns a newly-allocated copy of this GreasedRegion; modifications to one won't change the
     * other, and this method returns the copy while leaving the original unchanged.
     * @return a copy of this GreasedRegion; the copy can be changed without altering the original
     */
    public GreasedRegion copy()
    {
        return new GreasedRegion(this);
    }

    /**
     * Returns this GreasedRegion's data as a 2D boolean array, [width][height] in size, with on treated as true and off
     * treated as false.
     * @return a 2D boolean array that represents this GreasedRegion's data
     */
    public boolean[][] decode()
    {
        boolean[][] bools = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bools[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0;
            }
        }
        return bools;
    }

    /**
     * Fills this GreasedRegion's data into the given 2D char array, modifying it and returning it, with "on" cells
     * filled with the char parameter {@code on} and "off" cells with the parameter {@code off}.
     * @param chars a 2D char array that will be modified; must not be null, nor can it contain null elements
     * @param on the char to use for "on" cells
     * @param off the char to use for "off" cells
     * @return a 2D char array that represents this GreasedRegion's data
     */
    public char[][] intoChars(char[][] chars, char on, char off)
    {
        for (int x = 0; x < width && x < chars.length; x++) {
            for (int y = 0; y < height && y < chars[x].length; y++) {
                chars[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? on : off;
            }
        }
        return chars;
    }

    /**
     * Fills this GreasedRegion's data into the given 2D char array, modifying it and returning it, with "on" cells
     * filled with the char parameter {@code on} and "off" cells left as-is.
     * @param chars a 2D char array that will be modified; must not be null, nor can it contain null elements
     * @param on the char to use for "on" cells
     * @return a 2D char array that represents the "on" cells in this GreasedRegion's data written over chars
     */
    public char[][] intoChars(char[][] chars, char on)
    {
        for (int x = 0; x < width && x < chars.length; x++) {
            for (int y = 0; y < height && y < chars[x].length; y++) {
                if((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0)
                    chars[x][y] = on;
            }
        }
        return chars;
    }

    /**
     * Returns this GreasedRegion's data as a 2D char array,  [width][height] in size, with "on" cells filled with the
     * char parameter on and "off" cells with the parameter off.
     * @param on the char to use for "on" cells
     * @param off the char to use for "off" cells
     * @return a 2D char array that represents this GreasedRegion's data
     */
    public char[][] toChars(char on, char off)
    {
        char[][] chars = new char[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                chars[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? on : off;
            }
        }
        return chars;
    }
    /**
     * Returns this GreasedRegion's data as a 2D char array,  [width][height] in size, with "on" cells filled with '.'
     * and "off" cells with '#'.
     * @return a 2D char array that represents this GreasedRegion's data
     */

    public char[][] toChars()
    {
        return toChars('.', '#');
    }

    /**
     * Returns this GreasedRegion's data as a StringBuilder, with each row made of the parameter on for "on" cells and
     * the parameter off for "off" cells, separated by newlines, with no trailing newline at the end.
     * @param on the char to use for "on" cells
     * @param off the char to use for "off" cells
     * @return a StringBuilder that stores each row of this GreasedRegion as chars, with rows separated by newlines.
     */
    public StringBuilder show(char on, char off)
    {
        StringBuilder sb = new StringBuilder((width+1) * height);
        for (int y = 0; y < height;) {
            for (int x = 0; x < width; x++) {
                sb.append((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? on : off);
            }
            if(++y < height)
                sb.append('\n');
        }
        return sb;
    }

    /**
     * Returns a legible String representation of this that can be printed over multiple lines, with all "on" cells
     * represented by '.' and all "off" cells by '#', in roguelike floors-on walls-off convention, separating each row
     * by newlines (without a final trailing newline, so you could append text right after this).
     * @return a String representation of this GreasedRegion using '.' for on, '#' for off, and newlines between rows
     */
    @Override
    public String toString()
    {
        return show('.', '#').toString();
    }

    /**
     * Returns a copy of map where if a cell is "on" in this GreasedRegion, this keeps the value in map intact,
     * and where a cell is "off", it instead writes the char filler.
     * @param map a 2D char array that will not be modified
     * @param filler the char to use where this GreasedRegion stores an "off" cell
     * @return a masked copy of map
     */
    public char[][] mask(char[][] map, char filler)
    {
        if(map == null || map.length == 0)
            return new char[0][0];
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        char[][] chars = new char[width2][height2];
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                chars[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? map[x][y] : filler;
            }
        }
        return chars;
    }

    /**
     * Returns a copy of map where if a cell is "on" in this GreasedRegion, this keeps the value in map intact,
     * and where a cell is "off", it instead writes the short filler. Meant for use with MultiSpill, but may be
     * used anywhere you have a 2D short array. {@link #mask(char[][], char)} is more likely to be useful.
     * @param map a 2D short array that will not be modified
     * @param filler the short to use where this GreasedRegion stores an "off" cell
     * @return a masked copy of map
     */
    public short[][] mask(short[][] map, short filler)
    {
        if(map == null || map.length == 0)
            return new short[0][0];
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        short[][] shorts = new short[width2][height2];
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                shorts[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? map[x][y] : filler;
            }
        }
        return shorts;
    }

    /**
     * Returns a copy of map where if a cell is "off" in this GreasedRegion, this keeps the value in map intact,
     * and where a cell is "on", it instead writes the char toWrite.
     * @param map a 2D char array that will not be modified
     * @param toWrite the char to use where this GreasedRegion stores an "on" cell
     * @return a masked copy of map
     */
    public char[][] inverseMask(char[][] map, char toWrite)
    {
        if(map == null || map.length == 0)
            return new char[0][0];
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        char[][] chars = new char[width2][height2];
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                chars[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? toWrite : map[x][y];
            }
        }
        return chars;
    }

    /**
     * "Inverse mask for ints;" returns a copy of map where if a cell is "off" in this GreasedRegion, this keeps
     * the value in map intact, and where a cell is "on", it instead writes the int toWrite.
     * @param map a 2D int array that will not be modified
     * @param toWrite the int to use where this GreasedRegion stores an "on" cell
     * @return an altered copy of map
     */
    public int[][] writeInts(int[][] map, int toWrite)
    {
        if(map == null || map.length == 0)
            return new int[0][0];
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        int[][] ints = new int[width2][height2];
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                ints[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? toWrite : map[x][y];
            }
        }
        return ints;
    }

    /**
     * "Inverse mask for ints;" returns a copy of map where if a cell is "off" in this GreasedRegion, this keeps
     * the value in map intact, and where a cell is "on", it instead writes the int toWrite. Modifies map in-place,
     * unlike {@link #writeInts(int[][], int)}.
     * @param map a 2D int array that <b>will</b> be modified
     * @param toWrite the int to use where this GreasedRegion stores an "on" cell
     * @return map, with the changes applied; not a copy
     */
    public int[][] writeIntsInto(int[][] map, int toWrite)
    {
        if(map == null || map.length == 0)
            return map;
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                if((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0)
                    map[x][y] = toWrite;
            }
        }
        return map;
    }
    /**
     * "Inverse mask for doubles;" returns a copy of map where if a cell is "off" in this GreasedRegion, this keeps
     * the value in map intact, and where a cell is "on", it instead writes the double toWrite.
     * @param map a 2D double array that will not be modified
     * @param toWrite the double to use where this GreasedRegion stores an "on" cell
     * @return an altered copy of map
     */
    public double[][] writeDoubles(double[][] map, double toWrite)
    {
        if(map == null || map.length == 0)
            return new double[0][0];
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        double[][] doubles = new double[width2][height2];
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                doubles[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? toWrite : map[x][y];
            }
        }
        return doubles;
    }

    /**
     * "Inverse mask for doubles;" returns a copy of map where if a cell is "off" in this GreasedRegion, this keeps
     * the value in map intact, and where a cell is "on", it instead writes the double toWrite. Modifies map in-place,
     * unlike {@link #writeDoubles(double[][], double)}.
     * @param map a 2D double array that <b>will</b> be modified
     * @param toWrite the double to use where this GreasedRegion stores an "on" cell
     * @return map, with the changes applied; not a copy
     */
    public double[][] writeDoublesInto(double[][] map, double toWrite)
    {
        if(map == null || map.length == 0)
            return map;
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                if((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0)
                    map[x][y] = toWrite;
            }
        }
        return map;
    }

    /**
     * Union of two GreasedRegions, assigning the result into this GreasedRegion. Any cell that is "on" in either
     * GreasedRegion will be made "on" in this GreasedRegion.
     * @param other another GreasedRegion that will not be modified
     * @return this, after modification, for chaining
     */
    public GreasedRegion or(GreasedRegion other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] |= other.data[x * ySections + y];
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }

        return this;
    }

    /**
     * Intersection of two GreasedRegions, assigning the result into this GreasedRegion. Any cell that is "on" in both
     * GreasedRegions will be kept "on" in this GreasedRegion, but all other cells will be made "off."
     * @param other another GreasedRegion that will not be modified
     * @return this, after modification, for chaining
     */
    public GreasedRegion and(GreasedRegion other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] &= other.data[x * ySections + y];
            }
        }
        return this;
    }
    /**
     * Difference of two GreasedRegions, assigning the result into this GreasedRegion. Any cell that is "on" in this
     * GreasedRegion and "off" in other will be kept "on" in this GreasedRegion, but all other cells will be made "off."
     * @param other another GreasedRegion that will not be modified
     * @return this, after modification, for chaining
     * @see #notAnd(GreasedRegion) notAnd is a very similar method that acts sort-of in reverse of this method
     */
    public GreasedRegion andNot(GreasedRegion other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] &= ~other.data[x * ySections + y];
            }
        }
        return this;
    }

    /**
     * Like andNot, but subtracts this GreasedRegion from other and stores the result in this GreasedRegion, without
     * mutating other.
     * @param other another GreasedRegion that will not be modified
     * @return this, after modification, for chaining
     * @see #andNot(GreasedRegion) andNot is a very similar method that acts sort-of in reverse of this method
     */
    public GreasedRegion notAnd(GreasedRegion other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] = other.data[x * ySections + y] & ~data[x * ySections + y];
            }
        }
        return this;
    }

    /**
     * Symmetric difference (more commonly known as exclusive or, hence the name) of two GreasedRegions, assigning the
     * result into this GreasedRegion. Any cell that is "on" in this and "off" in other, or "off" in this and "on" in
     * other, will be made "on" in this; all other cells will be made "off." Useful to find cells that are "on" in
     * exactly one of two GreasedRegions (not "on" in both, or "off" in both).
     * @param other another GreasedRegion that will not be modified
     * @return this, after modification, for chaining
     */
    public GreasedRegion xor(GreasedRegion other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] ^= other.data[x * ySections + y];
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        return this;
    }

    /**
     * Negates this GreasedRegion, turning "on" to "off" and "off" to "on."
     * @return this, after modification, for chaining
     */
    public GreasedRegion not()
    {
        for (int a = 0; a < data.length; a++)
        {
            data[a] = ~data[a];
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        return this;
    }

    /**
     * Moves the "on" cells in this GreasedRegion to the given x and y offset, removing cells that move out of bounds.
     * @param x the x offset to translate by; can be negative
     * @param y the y offset to translate by; can be negative
     * @return this for chaining
     */
    public GreasedRegion translate(int x, int y)
    {
        if(width < 1 || ySections <= 0 || (x == 0 && y == 0))
            return this;
        int start = Math.max(0, x), len = Math.min(width, width + x) - start,
                jump = (y == 0) ? 0 : (y < 0) ? -(1-y >>> 6) : (y-1 >>> 6), lily = (y < 0) ? -(-y & 63) : (y & 63),
                originalJump = Math.max(0, -jump), alterJump = Math.max(0, jump);
        long[] data2 = new long[width * ySections];

        long prev, tmp;
        if (x < 0) {
            for (int i = alterJump, oi = originalJump; i < ySections && oi < ySections; i++, oi++) {
                for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                    data2[jj * ySections + i] = data[j * ySections + oi];
                }
            }
        } else if (x > 0) {
            for (int i = alterJump, oi = originalJump; i < ySections && oi < ySections; i++, oi++) {
                for (int j = 0, jj = start; j < len; j++, jj++) {
                    data2[jj * ySections + i] = data[j * ySections + oi];
                }
            }
        } else {
            for (int i = alterJump, oi = originalJump; i < ySections && oi < ySections; i++, oi++) {
                for (int j = 0; j < len; j++) {
                    data2[j * ySections + i] = data[j * ySections + oi];
                }
            }
        }

        if(lily < 0) {
            for (int i = start; i < len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L << -lily)) << (64 + lily);
                    data2[i * ySections + j] >>>= -lily;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }
        else if(lily > 0) {
            for (int i = start; i < start + len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L >>> lily)) >>> (64 - lily);
                    data2[i * ySections + j] <<= lily;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }


        if(yEndMask != -1) {
            for (int a = ySections - 1; a < data2.length; a += ySections) {
                data2[a] &= yEndMask;
            }
        }

        data = data2;
        return this;
    }

    /**
     * Adds to this GreasedRegion with a moved set of its own "on" cells, moved to the given x and y offset.
     * Ignores cells that would be added out of bounds. Keeps all cells that are currently "on" unchanged.
     * @param x the x offset to translate by; can be negative
     * @param y the y offset to translate by; can be negative
     * @return this for chaining
     */
    public GreasedRegion insertTranslation(int x, int y)
    {
        if(width < 1 || ySections <= 0 || (x == 0 && y == 0))
            return this;
        int start = Math.max(0, x), len = Math.min(width, width + x) - start,
                jump = (y == 0) ? 0 : (y < 0) ? -(1-y >>> 6) : (y-1 >>> 6), lily = (y < 0) ? -(-y & 63) : (y & 63),
                originalJump = Math.max(0, -jump), alterJump = Math.max(0, jump);
        long[] data2 = new long[width * ySections];
        long prev, tmp;
        if (x < 0) {
            for (int i = alterJump, oi = originalJump; i < ySections && oi < ySections; i++, oi++) {
                for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                    data2[jj * ySections + i] = data[j * ySections + oi];
                }
            }
        } else if (x > 0) {
            for (int i = alterJump, oi = originalJump; i < ySections && oi < ySections; i++, oi++) {
                for (int j = 0, jj = start; j < len; j++, jj++) {
                    data2[jj * ySections + i] = data[j * ySections + oi];
                }
            }
        } else {
            for (int i = alterJump, oi = originalJump; i < ySections && oi < ySections; i++, oi++) {
                for (int j = 0; j < len; j++) {
                    data2[j * ySections + i] = data[j * ySections + oi];
                }
            }
        }

        if(lily < 0) {
            for (int i = start; i < len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L << -lily)) << (64 + lily);
                    data2[i * ySections + j] >>>= -lily;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }
        else if(lily > 0) {
            for (int i = start; i < start + len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L >>> lily)) >>> (64 - lily);
                    data2[i * ySections + j] <<= lily;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }

        for (int i = 0; i < width * ySections; i++) {
            data2[i] |= data[i];
        }


        if(yEndMask != -1) {
            for (int a = ySections - 1; a < data2.length; a += ySections) {
                data2[a] &= yEndMask;
            }
        }

        data = data2;
        return this;
    }

    /**
     * Effectively doubles the x and y values of each cell this contains (not scaling each cell to be larger, so each
     * "on" cell will be surrounded by "off" cells), and re-maps the positions so the given x and y in the doubled space
     * become 0,0 in the resulting GreasedRegion (which is this, assigning to itself).
     * @param x in the doubled coordinate space, the x position that should become 0 x in the result; can be negative
     * @param y in the doubled coordinate space, the y position that should become 0 y in the result; can be negative
     * @return this for chaining
     */
    public GreasedRegion zoom(int x, int y)
    {
        if(width < 1 || ySections <= 0)
            return this;
        x = -x;
        y = -y;
        int
                width2 = width + 1 >>> 1, ySections2 = ySections + 1 >>> 1,
                start = Math.max(0, x), len = Math.min(width, width + x) - start,
                //tall = (Math.min(height, height + y) - Math.max(0, y)) + 63 >> 6,
                jump = (y == 0) ? 0 : (y < 0) ? -(1-y >>> 6) : (y-1 >>> 6), lily = (y < 0) ? -(-y & 63) : (y & 63),
                originalJump = Math.max(0, -jump), alterJump = Math.max(0, jump),
                oddX = (x & 1), oddY = (y & 1);
        long[] data2 = new long[width * ySections];

        long prev, tmp, yEndMask2 = -1L >>> (64 - ((height + 1 >>> 1) & 63));
        if (x < 0) {
            for (int i = alterJump, oi = originalJump; i <= ySections2 && oi < ySections; i++, oi++) {
                for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                    data2[jj * ySections + i] = data[j * ySections + oi];
                }
            }
        } else if (x > 0) {
            for (int i = alterJump, oi = originalJump; i <= ySections2 && oi < ySections; i++, oi++) {
                for (int j = 0, jj = start; j < len; j++, jj++) {
                    data2[jj * ySections + i] = data[j * ySections + oi];
                }
            }
        } else {
            for (int i = alterJump, oi = originalJump; i <= ySections2 && oi < ySections; i++, oi++) {
                for (int j = 0; j < len; j++) {
                    data2[j * ySections + i] = data[j * ySections + oi];
                }
            }
        }

        if(lily < 0) {
            for (int i = start; i < len; i++) {
                prev = 0L;
                for (int j = ySections2; j >= 0; j--) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L << -lily)) << (64 + lily);
                    data2[i * ySections + j] >>>= -lily;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }
        else if(lily > 0) {
            for (int i = start; i < start + len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections2; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L >>> lily)) >>> (64 - lily);
                    data2[i * ySections + j] <<= lily;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }


        if(ySections2 > 0 && yEndMask2 != -1) {
            for (int a = ySections2 - 1; a < data2.length; a += ySections) {
                data2[a] &= yEndMask2;
                if(ySections2 < ySections)
                    data2[a+1] = 0L;
            }
        }
        for (int i = 0; i < width2; i++) {
            for (int j = 0; j < ySections2; j++) {
                prev = data2[i * ySections + j];
                tmp = prev >>> 32;
                prev &= 0xFFFFFFFFL;
                prev = (prev | (prev << 16)) & 0x0000FFFF0000FFFFL;
                prev = (prev | (prev << 8)) & 0x00FF00FF00FF00FFL;
                prev = (prev | (prev << 4)) & 0x0F0F0F0F0F0F0F0FL;
                prev = (prev | (prev << 2)) & 0x3333333333333333L;
                prev = (prev | (prev << 1)) & 0x5555555555555555L;
                prev <<= oddY;
                if(oddX == 1) {
                    if (i * 2 + 1 < width)
                        data[(i * ySections + j) * 2 + ySections] = prev;
                    if (i * 2 < width)
                        data[(i * ySections + j) * 2] = 0L;
                }
                else
                {
                    if (i * 2 < width)
                        data[(i * ySections + j) * 2] = prev;
                    if (i * 2 + 1 < width)
                        data[(i * ySections + j) * 2 + ySections] = 0L;
                }
                if(j * 2 + 1 < ySections) {
                    tmp = (tmp | (tmp << 16)) & 0x0000FFFF0000FFFFL;
                    tmp = (tmp | (tmp << 8)) & 0x00FF00FF00FF00FFL;
                    tmp = (tmp | (tmp << 4)) & 0x0F0F0F0F0F0F0F0FL;
                    tmp = (tmp | (tmp << 2)) & 0x3333333333333333L;
                    tmp = (tmp | (tmp << 1)) & 0x5555555555555555L;
                    tmp <<= oddY;
                    if(oddX == 1) {
                        if (i * 2 + 1 < width)
                            data[(i * ySections + j) * 2 + ySections + 1] = tmp;
                        if (i * 2 < width)
                            data[(i * ySections + j) * 2 + 1] = 0L;
                    }
                    else
                    {
                        if (i * 2 < width)
                            data[(i * ySections + j) * 2 + 1] = tmp;
                        if (i * 2 + 1 < width)
                            data[(i * ySections + j) * 2 + ySections + 1] = 0L;
                    }
                }
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }

        return this;
    }

    /**
     * Takes the pairs of "on" cells in this GreasedRegion that are separated by exactly one cell in an orthogonal line,
     * and changes the gap cells to "on" as well.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return this for chaining
     */
    public GreasedRegion connect()
    {
        if(width < 2 || ySections == 0)
            return this;

        final long[] next = new long[width * ySections];
        System.arraycopy(data, 0, next, 0, width * ySections);
        for (int a = 0; a < ySections; a++) {
            next[a] |= ((data[a] << 1) & (data[a] >>> 1)) | data[a+ySections];
            next[(width-1)*ySections+a] |= ((data[(width-1)*ySections+a] << 1) & (data[(width-1)*ySections+a] >>> 1)) | data[(width-2) *ySections+a];

            for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                next[i] |= ((data[i] << 1) & (data[i] >>> 1)) | (data[i - ySections] & data[i + ySections]);
            }

            if(a > 0) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= (data[i - 1] & 0x8000000000000000L) >>> 63 & (data[i] >>> 1);
                }
            }
            else
            {
                for (int i = ySections; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= (data[i] >>> 1 & 1L);
                }
            }

            if(a < ySections - 1) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= (data[i + 1] & 1L) << 63 & (data[i] << 1);
                }
            }
            else
            {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= (data[i] << 1 & 0x8000000000000000L);
                }

            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }

    /**
     * Takes the pairs of "on" cells in this GreasedRegion that are separated by exactly one cell in an orthogonal or
     * diagonal line, and changes the gap cells to "on" as well.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return this for chaining
     */
    public GreasedRegion connect8way()
    {
        if(width < 2 || ySections == 0)
            return this;

        final long[] next = new long[width * ySections];
        System.arraycopy(data, 0, next, 0, width * ySections);
        for (int a = 0; a < ySections; a++) {
            next[a] |= ((data[a] << 1) & (data[a] >>> 1)) | data[a+ySections] | (data[a+ySections] << 1) | (data[a+ySections] >>> 1);
            next[(width-1)*ySections+a] |= ((data[(width-1)*ySections+a] << 1) & (data[(width-1)*ySections+a] >>> 1))
                    | data[(width-2) *ySections+a] | (data[(width-2)*ySections+a] << 1) | (data[(width-2)*ySections+a] >>> 1);

            for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                next[i] |= ((data[i] << 1) & (data[i] >>> 1)) | (data[i - ySections] & data[i + ySections])
                        | ((data[i - ySections] << 1) & (data[i + ySections] >>> 1))
                        | ((data[i + ySections] << 1) & (data[i - ySections] >>> 1));
            }

            if(a > 0) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= ((data[i - 1] & 0x8000000000000000L) >>> 63 & (data[i] >>> 1)) |
                            ((data[i - ySections - 1] & 0x8000000000000000L) >>> 63 & (data[i + ySections] >>> 1)) |
                            ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63 & (data[i - ySections] >>> 1));
                }
            }
            else
            {
                for (int i = ySections; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= (data[i] >>> 1 & 1L) | (data[i - ySections] >>> 1 & 1L) | (data[i + ySections] >>> 1 & 1L);
                }
            }

            if(a < ySections - 1) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= ((data[i + 1] & 1L) << 63 & (data[i] << 1)) |
                            ((data[i - ySections + 1] & 1L) << 63 & (data[i + ySections] << 1)) |
                            ((data[i + ySections + 1] & 1L) << 63 & (data[i - ySections] << 1)) ;
                }
            }
            else
            {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= (data[i] << 1 & 0x8000000000000000L)
                            | (data[i - ySections] << 1 & 0x8000000000000000L) | (data[i + ySections] << 1 & 0x8000000000000000L);
                }

            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }
    /**
     * Takes the pairs of "on" cells in this GreasedRegion that are separated by exactly one cell in an orthogonal or
     * diagonal line, and changes the gap cells to "on" as well. As a special case, this requires diagonals to either
     * have no "on" cells adjacent along the perpendicular diagonal, or both cells on that perpendicular diagonal need
     * to be "on." This is useful to counteract some less-desirable behavior of {@link #connect8way()}, where a right
     * angle would always get the inner corners filled because it was considered a diagonal.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return this for chaining
     */
    public GreasedRegion connectLines()
    {
        if(width < 2 || ySections == 0)
            return this;

        final long[] next = new long[width * ySections];
        System.arraycopy(data, 0, next, 0, width * ySections);
        for (int a = 0; a < ySections; a++) {
            next[a] |= ((data[a] << 1) & (data[a] >>> 1)) | data[a+ySections] | (data[a+ySections] << 1) | (data[a+ySections] >>> 1);
            next[(width-1)*ySections+a] |= ((data[(width-1)*ySections+a] << 1) & (data[(width-1)*ySections+a] >>> 1))
                    | data[(width-2) *ySections+a] | (data[(width-2)*ySections+a] << 1) | (data[(width-2)*ySections+a] >>> 1);

            for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                next[i] |= ((data[i] << 1) & (data[i] >>> 1)) | (data[i - ySections] & data[i + ySections])
                        | (((data[i - ySections] << 1) & (data[i + ySections] >>> 1))
                           ^ ((data[i + ySections] << 1) & (data[i - ySections] >>> 1)));
            }

            if(a > 0) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= ((data[i - 1] & 0x8000000000000000L) >>> 63 & (data[i] >>> 1))
                            | (((data[i - ySections - 1] & 0x8000000000000000L) >>> 63 & (data[i + ySections] >>> 1))
                               ^ ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63 & (data[i - ySections] >>> 1)));
                }
            }
            else
            {
                for (int i = ySections; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= (data[i] >>> 1 & 1L) | (data[i - ySections] >>> 1 & 1L) | (data[i + ySections] >>> 1 & 1L);
                }
            }

            if(a < ySections - 1) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= ((data[i + 1] & 1L) << 63 & (data[i] << 1))
                            | (((data[i - ySections + 1] & 1L) << 63 & (data[i + ySections] << 1))
                               ^ ((data[i + ySections + 1] & 1L) << 63 & (data[i - ySections] << 1)));
                }
            }
            else
            {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= (data[i] << 1 & 0x8000000000000000L)
                            | (data[i - ySections] << 1 & 0x8000000000000000L) | (data[i + ySections] << 1 & 0x8000000000000000L);
                }

            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }

    /**
     * Like {@link #retract()}, this reduces the width of thick areas of this GreasedRegion, but thin() will not remove
     * areas that would be identical in a subsequent call to retract(), such as if the area would be eliminated. This
     * is useful primarily for adjusting areas so they do not exceed a width of 2 cells, though their length (the longer
     * of the two dimensions) will be unaffected by this. Especially wide, irregularly-shaped areas may have unintended
     * appearances if you call this repeatedly or use {@link #thinFully()}; consider using this sparingly, or primarily
     * when an area has just gotten thicker than desired.
     * <br>
     * This currently uses 4-way adjacency, but had previously used 8-way; if you want the behavior this previously had,
     * you can use {@link #thin8way()}, but it may be a good idea to try this method as well (some of the old behavior
     * had problems where it yielded significantly larger minimum widths in some areas).
     * @return this for chaining
     */
    public GreasedRegion thin()
    {
        if(width <= 2 || ySections <= 0)
            return this;
        GreasedRegion c1 = new GreasedRegion(this).retract(),
                c2 = new GreasedRegion(c1).expand().xor(this).expand().and(this);
        remake(c1).or(c2);
        /*
        System.out.println("\n\nc1:\n" + c1.toString() + "\n");
        System.out.println("\n\nc2:\n" + c2.toString() + "\n");
        System.out.println("\n\nthis:\n" + toString() + "\n");
        */
        return this;
    }

    /**
     * Calls {@link #thin()} repeatedly, until the result is unchanged from the last call. Consider using the idiom
     * {@code expand8way().retract().thinFully()} to help change a possibly-strange appearance when the GreasedRegion
     * this is called on touches the edges of the grid. In general, this method is likely to go too far when it tries to
     * thin a round or irregular area, and this often results in many diagonal lines spanning the formerly-thick area.
     * <br>
     * This currently uses 4-way adjacency, but had previously used 8-way; if you want the behavior this previously had,
     * you can use {@link #thinFully8way()}, but it may be a good idea to try this method as well (some of the old
     * behavior had problems where it yielded significantly larger minimum widths in some areas).
     * @return this for chaining
     */
    public GreasedRegion thinFully()
    {
        while (size() != thin().size());
        return this;
    }


    /**
     * Like {@link #retract8way()}, this reduces the width of thick areas of this GreasedRegion, but thin8way() will not
     * remove areas that would be identical in a subsequent call to retract8way(), such as if the area would be
     * eliminated. This is useful primarily for adjusting areas so they do not exceed a width of 2 cells, though their
     * length (the longer of the two dimensions) will be unaffected by this. Especially wide, irregularly-shaped areas
     * may have unintended appearances if you call this repeatedly or use {@link #thinFully8way()}; consider using this
     * sparingly, or primarily when an area has just gotten thicker than desired.
     * <br>
     * This method was called {@link #thin()}, but now that name refers to a variant that uses 4-way adjacency.
     * @return this for chaining
     */
    public GreasedRegion thin8way()
    {
        if(width <= 2 || ySections <= 0)
            return this;
        GreasedRegion c1 = new GreasedRegion(this).retract8way(),
                c2 = new GreasedRegion(c1).expand8way().xor(this).expand8way().and(this);
        remake(c1).or(c2);
        return this;
    }

    /**
     * Calls {@link #thin8way()} repeatedly, until the result is unchanged from the last call. Consider using the idiom
     * {@code expand8way().retract().thinFully8way()} to help change a strange appearance when the GreasedRegion this is
     * called on touches the edges of the grid. In general, this method is likely to go too far when it tries to thin a
     * round or irregular area, and this often results in many diagonal lines spanning the formerly-thick area.
     * <br>
     * This method was called {@link #thinFully()}, but now that name refers to a variant that uses 4-way adjacency.
     * @return this for chaining
     */
    public GreasedRegion thinFully8way()
    {
        while (size() != thin8way().size());
        return this;
    }


    /**
     * Removes "on" cells that are orthogonally adjacent to other "on" cells, keeping at least one cell in a group "on."
     * Uses a "checkerboard" pattern to determine which cells to turn  off, with all cells that would be black on a
     * checkerboard turned off and all others kept as-is.
     * @return this for chaining
     */
    public GreasedRegion disperse()
    {
        if(width < 1 || ySections <= 0)
            return this;
        long mask = 0x5555555555555555L;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < ySections; j++) {
                data[j] &= mask;
            }
            mask = ~mask;
        }
        return this;
    }
    /**
     * Removes "on" cells that are 8-way adjacent to other "on" cells, keeping at least one cell in a group "on."
     * Uses a "grid-like" pattern to determine which cells to turn off, with all cells with even x and even y kept as-is
     * but all other cells (with either or both odd x or odd y) turned off.
     * @return this for chaining
     */
    public GreasedRegion disperse8way()
    {
        if(width < 1 || ySections <= 0)
            return this;
        int len = data.length;
        long mask = 0x5555555555555555L;
        for (int j = 0; j < len - 1; j += 2) {
            data[j] &= mask;
            data[j+1] = 0;
        }
        return this;
    }
    /**
     * Removes "on" cells that are nearby other "on" cells, with a random factor to which bits are actually turned off
     * that still ensures exactly half of the bits are kept as-is (the one exception is when height is an odd number,
     * which makes the bottom row slightly random).
     * @param random the RNG used for a random factor
     * @return this for chaining
     */
    public GreasedRegion disperseRandom(RandomnessSource random)
    {
        if(width < 1 || ySections <= 0)
            return this;
        int len = data.length;
        for (int j = 0; j < len; j++) {
            data[j] &= randomInterleave(random);
        }
        return this;
    }
    /**
     * Generates a random 64-bit long with a number of '1' bits (Hamming weight) equal on average to bitCount.
     * For example, calling this with a parameter of 32 will be equivalent to calling nextLong() on the given
     * RandomnessSource, which could be an {@link RNG} or any RandomnessSource with good 64-bit generation quality.
     * Calling this with a parameter of 16 will have on average 16 of the 64 bits in the returned long set to '1',
     * distributed pseudo-randomly, while a parameter of 47 will have on average 47 bits set. This can be useful for
     * certain code that uses bits to represent data but needs a different ratio of set bits to unset bits than 1:1.
     * <br>
     * The parameter {@code random} can be an object like a {@link LightRNG}, an {@link RNG} backed by a
     * well-distributed RandomnessSource like its default, LightRNG, a {@link GWTRNG} (especially if you target GWT,
     * where it will perform much better than most alternatives), or any of various other RandomnessSource
     * implementations that distribute bits well for {@link RandomnessSource#nextLong()}, but should not be
     * intentionally-biased RNGs like {@link DharmaRNG} or {@link EditRNG}, nor double-based QRNGs like
     * {@link VanDerCorputQRNG} or {@link SobolQRNG}.
     * @param random used to determine random factors; likely to be an {@link RNG}, {@link LightRNG}, or {@link GWTRNG}
     * @param bitCount an int, only considered if between 0 and 64, that is the average number of bits to set
     * @return a 64-bit long that, on average, should have bitCount bits set to 1, potentially anywhere in the long
     */
    public static long approximateBits(RandomnessSource random, int bitCount) {
        if (bitCount <= 0)
            return 0L;
        if (bitCount >= 64)
            return -1L;
        if (bitCount == 32)
            return random.nextLong();
        boolean high = bitCount > 32;
        int altered = (high ? 64 - bitCount : bitCount), lsb = NumberTools.lowestOneBit(altered);
        long data = random.nextLong();
        for (int i = lsb << 1; i <= 16; i <<= 1) {
            if ((altered & i) == 0)
                data &= random.nextLong();
            else
                data |= random.nextLong();
        }
        return high ? ~(random.nextLong() & data) : (random.nextLong() & data);
    }

    /**
     * Gets a somewhat-random long with exactly 32 bits set; in each pair of bits starting at bit 0 and bit 1, then bit
     * 2 and bit 3, up to bit 62 and bit 3, one bit will be 1 and one bit will be 0 in each pair.
     * <br>
     * Not exactly general-use; meant for generating data for GreasedRegion.
     * @return a random long with 32 "1" bits, distributed so exactly one bit is "1" for each pair of bits
     */
    public static long randomInterleave(RandomnessSource random) {
        long bits = random.nextLong() & 0xFFFFFFFFL, ib = ~bits & 0xFFFFFFFFL;
        bits |= (bits << 16);
        ib |= (ib << 16);
        bits &= 0x0000FFFF0000FFFFL;
        ib &= 0x0000FFFF0000FFFFL;
        bits |= (bits << 8);
        ib |= (ib << 8);
        bits &= 0x00FF00FF00FF00FFL;
        ib &= 0x00FF00FF00FF00FFL;
        bits |= (bits << 4);
        ib |= (ib << 4);
        bits &= 0x0F0F0F0F0F0F0F0FL;
        ib &= 0x0F0F0F0F0F0F0F0FL;
        bits |= (bits << 2);
        ib |= (ib << 2);
        bits &= 0x3333333333333333L;
        ib &= 0x3333333333333333L;
        bits |= (bits << 1);
        ib |= (ib << 1);
        bits &= 0x5555555555555555L;
        ib &= 0x5555555555555555L;
        return (bits | (ib << 1));
    }

    /**
     * Takes the "on" cells in this GreasedRegion and expands them by one cell in the 4 orthogonal directions, making
     * each "on" cell take up a plus-shaped area that may overlap with other "on" cells (which is just a normal "on"
     * cell then).
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return this for chaining
     */
    public GreasedRegion expand()
    {
        if(width < 2 || ySections == 0)
            return this;

        final long[] next = new long[width * ySections];
        System.arraycopy(data, 0, next, 0, width * ySections);
        for (int a = 0; a < ySections; a++) {
            next[a] |= (data[a] << 1) | (data[a] >>> 1) | data[a+ySections];
            next[(width-1)*ySections+a] |= (data[(width-1)*ySections+a] << 1) | (data[(width-1)*ySections+a] >>> 1) | data[(width-2) *ySections+a];

            for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                next[i] |= (data[i] << 1) | (data[i] >>> 1) | data[i - ySections] | data[i + ySections];
            }

            if(a > 0) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= (data[i - 1] & 0x8000000000000000L) >>> 63;
                }
            }

            if(a < ySections - 1) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= (data[i + 1] & 1L) << 63;
                }
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }
    /**
     * Takes the "on" cells in this GreasedRegion and expands them by amount cells in the 4 orthogonal directions,
     * making each "on" cell take up a plus-shaped area that may overlap with other "on" cells (which is just a normal
     * "on" cell then).
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return this for chaining
     */
    @Override
    public GreasedRegion expand(int amount)
    {
        for (int i = 0; i < amount; i++) {
            expand();
        }
        return this;
    }
    /**
     * Takes the "on" cells in this GreasedRegion and produces amount GreasedRegions, each one expanded by 1 cell in
     * the 4 orthogonal directions relative to the previous GreasedRegion, making each "on" cell take up a plus-shaped
     * area that may overlap with other "on" cells (which is just a normal "on" cell then). This returns an array of
     * GreasedRegions with progressively greater expansions, and does not modify this GreasedRegion.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return an array of new GreasedRegions, length == amount, where each one is expanded by 1 relative to the last
     */
    public GreasedRegion[] expandSeries(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        GreasedRegion[] regions = new GreasedRegion[amount];
        GreasedRegion temp = new GreasedRegion(this);
        for (int i = 0; i < amount; i++) {
            regions[i] = new GreasedRegion(temp.expand());
        }
        return regions;
    }

    public ArrayList<GreasedRegion> expandSeriesToLimit()
    {
        ArrayList<GreasedRegion> regions = new ArrayList<>();
        GreasedRegion temp = new GreasedRegion(this);
        while (temp.size() != temp.expand().size()) {
            regions.add(new GreasedRegion(temp));
        }
        return regions;
    }
    /**
     * Takes the "on" cells in this GreasedRegion and expands them by one cell in the 4 orthogonal directions, producing
     * a diamoond shape, then removes the original area before expansion, producing only the cells that were "off" in
     * this and within 1 cell (orthogonal-only) of an "on" cell. This method is similar to {@link #surface()}, but
     * surface finds cells inside the current GreasedRegion, while fringe finds cells outside it.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time. The surface and fringe methods do allocate one
     * temporary GreasedRegion to store the original before modification, but the others generally don't.
     * @return this for chaining
     */
    public GreasedRegion fringe()
    {
        GreasedRegion cpy = new GreasedRegion(this);
        expand();
        return andNot(cpy);
    }
    /**
     * Takes the "on" cells in this GreasedRegion and expands them by amount cells in the 4 orthogonal directions
     * (iteratively, producing a diamond shape), then removes the original area before expansion, producing only the
     * cells that were "off" in this and within amount cells (orthogonal-only) of an "on" cell. This method is similar
     * to {@link #surface()}, but surface finds cells inside the current GreasedRegion, while fringe finds cells outside
     * it.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time. The surface and fringe methods do allocate one
     * temporary GreasedRegion to store the original before modification, but the others generally don't.
     * @return this for chaining
     */

    public GreasedRegion fringe(int amount)
    {
        GreasedRegion cpy = new GreasedRegion(this);
        expand(amount);
        return andNot(cpy);
    }

    /**
     * Takes the "on" cells in this GreasedRegion and produces amount GreasedRegions, each one expanded by 1 cell in
     * the 4 orthogonal directions relative to the previous GreasedRegion, making each "on" cell take up a diamond-
     * shaped area. After producing the expansions, this removes the previous GreasedRegion from the next GreasedRegion
     * in the array, making each "fringe" in the series have 1 "thickness," which can be useful for finding which layer
     * of expansion a cell lies in. This returns an array of GreasedRegions with progressively greater expansions
     * without the cells of this GreasedRegion, and does not modify this GreasedRegion.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return an array of new GreasedRegions, length == amount, where each one is a 1-depth fringe pushed further out from this
     */
    public GreasedRegion[] fringeSeries(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        GreasedRegion[] regions = new GreasedRegion[amount];
        GreasedRegion temp = new GreasedRegion(this);
        regions[0] = new GreasedRegion(temp);
        for (int i = 1; i < amount; i++) {
            regions[i] = new GreasedRegion(temp.expand());
        }
        for (int i = 0; i < amount - 1; i++) {
            regions[i].xor(regions[i + 1]);
        }
        regions[amount - 1].fringe();
        return regions;
    }
    public ArrayList<GreasedRegion> fringeSeriesToLimit()
    {
        ArrayList<GreasedRegion> regions = expandSeriesToLimit();
        for (int i = regions.size() - 1; i > 0; i--) {
            regions.get(i).xor(regions.get(i-1));
        }
        regions.get(0).xor(this);
        return regions;
    }

    /**
     * Takes the "on" cells in this GreasedRegion and retracts them by one cell in the 4 orthogonal directions,
     * making each "on" cell that was orthogonally adjacent to an "off" cell into an "off" cell.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return this for chaining
     */
    public GreasedRegion retract()
    {
        if(width <= 2 || ySections <= 0)
            return this;

        final long[] next = new long[width * ySections];
        System.arraycopy(data, ySections, next, ySections, (width - 2) * ySections);
        for (int a = 0; a < ySections; a++) {
            if(a > 0 && a < ySections - 1) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                            & data[i - ySections]
                            & data[i + ySections];
                }
            }
            else if(a > 0) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & (data[i] >>> 1)
                            & data[i - ySections]
                            & data[i + ySections];
                }
            }
            else if(a < ySections - 1) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (data[i] << 1)
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                            & data[i - ySections]
                            & data[i + ySections];
                }
            }
            else // only the case when ySections == 1
            {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (data[i] << 1) & (data[i] >>> 1) & data[i - ySections] & data[i + ySections];
                }
            }
        }

        if(yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }

    /**
     * Takes the "on" cells in this GreasedRegion and retracts them by one cell in the 4 orthogonal directions, doing
     * this iteeratively amount times, making each "on" cell that was within amount orthogonal distance to an "off" cell
     * into an "off" cell.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return this for chaining
     */
    public GreasedRegion retract(int amount)
    {
        for (int i = 0; i < amount; i++) {
            retract();
        }
        return this;
    }

    public GreasedRegion[] retractSeries(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        GreasedRegion[] regions = new GreasedRegion[amount];
        GreasedRegion temp = new GreasedRegion(this);
        for (int i = 0; i < amount; i++) {
            regions[i] = new GreasedRegion(temp.retract());
        }
        return regions;
    }

    public ArrayList<GreasedRegion> retractSeriesToLimit()
    {
        ArrayList<GreasedRegion> regions = new ArrayList<>();
        GreasedRegion temp = new GreasedRegion(this);
        while (!temp.retract().isEmpty()) {
            regions.add(new GreasedRegion(temp));
        }
        return regions;
    }

    public GreasedRegion surface()
    {
        GreasedRegion cpy = new GreasedRegion(this).retract();
        return xor(cpy);
    }
    public GreasedRegion surface(int amount)
    {
        GreasedRegion cpy = new GreasedRegion(this).retract(amount);
        return xor(cpy);
    }

    public GreasedRegion[] surfaceSeries(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        GreasedRegion[] regions = new GreasedRegion[amount];
        GreasedRegion temp = new GreasedRegion(this);
        regions[0] = new GreasedRegion(temp);
        for (int i = 1; i < amount; i++) {
            regions[i] = new GreasedRegion(temp.retract());
        }
        for (int i = 0; i < amount - 1; i++) {
            regions[i].xor(regions[i + 1]);
        }
        regions[amount - 1].surface();
        return regions;
    }

    public ArrayList<GreasedRegion> surfaceSeriesToLimit()
    {
        ArrayList<GreasedRegion> regions = retractSeriesToLimit();
        if(regions.isEmpty())
            return regions;
        regions.add(0, regions.get(0).copy().xor(this));
        for (int i = 1; i < regions.size() - 1; i++) {
            regions.get(i).xor(regions.get(i+1));
        }
        return regions;
    }
    public GreasedRegion expand8way()
    {
        if(width < 2 || ySections <= 0)
            return this;

        final long[] next = new long[width * ySections];
        System.arraycopy(data, 0, next, 0, width * ySections);
        for (int a = 0; a < ySections; a++) {
            next[a] |= (data[a] << 1) | (data[a] >>> 1)
                    | data[a+ySections] | (data[a+ySections] << 1) | (data[a+ySections] >>> 1);
            next[(width-1)*ySections+a] |= (data[(width-1)*ySections+a] << 1) | (data[(width-1)*ySections+a] >>> 1)
                    | data[(width-2) *ySections+a] | (data[(width-2)*ySections+a] << 1) | (data[(width-2)*ySections+a] >>> 1);

            for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                next[i] |= (data[i] << 1) | (data[i] >>> 1)
                        | data[i - ySections] | (data[i - ySections] << 1) | (data[i - ySections] >>> 1)
                        | data[i + ySections] | (data[i + ySections] << 1) | (data[i + ySections] >>> 1);
            }

            if(a > 0) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= ((data[i - 1] & 0x8000000000000000L) >>> 63) |
                            ((data[i - ySections - 1] & 0x8000000000000000L) >>> 63) |
                            ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63);
                }
            }

            if(a < ySections - 1) {
                for (int i = ySections+a; i < (width-1) * ySections; i+= ySections) {
                    next[i] |= ((data[i + 1] & 1L) << 63) |
                            ((data[i - ySections + 1] & 1L) << 63) |
                            ((data[i + ySections+ 1] & 1L) << 63);
                }
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }

    @Override
    public GreasedRegion expand8way(int amount)
    {
        for (int i = 0; i < amount; i++) {
            expand8way();
        }
        return this;
    }

    public GreasedRegion[] expandSeries8way(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        GreasedRegion[] regions = new GreasedRegion[amount];
        GreasedRegion temp = new GreasedRegion(this);
        for (int i = 0; i < amount; i++) {
            regions[i] = new GreasedRegion(temp.expand8way());
        }
        return regions;
    }
    public ArrayList<GreasedRegion> expandSeriesToLimit8way()
    {
        ArrayList<GreasedRegion> regions = new ArrayList<>();
        GreasedRegion temp = new GreasedRegion(this);
        while (temp.size() != temp.expand8way().size()) {
            regions.add(new GreasedRegion(temp));
        }
        return regions;
    }

    public GreasedRegion fringe8way()
    {
        GreasedRegion cpy = new GreasedRegion(this);
        expand8way();
        return andNot(cpy);
    }
    public GreasedRegion fringe8way(int amount)
    {
        GreasedRegion cpy = new GreasedRegion(this);
        expand8way(amount);
        return andNot(cpy);
    }

    public GreasedRegion[] fringeSeries8way(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        GreasedRegion[] regions = new GreasedRegion[amount];
        GreasedRegion temp = new GreasedRegion(this);
        regions[0] = new GreasedRegion(temp);
        for (int i = 1; i < amount; i++) {
            regions[i] = new GreasedRegion(temp.expand8way());
        }
        for (int i = 0; i < amount - 1; i++) {
            regions[i].xor(regions[i + 1]);
        }
        regions[amount - 1].fringe8way();
        return regions;
    }
    public ArrayList<GreasedRegion> fringeSeriesToLimit8way()
    {
        ArrayList<GreasedRegion> regions = expandSeriesToLimit8way();
        for (int i = regions.size() - 1; i > 0; i--) {
            regions.get(i).xor(regions.get(i-1));
        }
        regions.get(0).xor(this);
        return regions;
    }

    public GreasedRegion retract8way()
    {
        if(width <= 2 || ySections <= 0)
            return this;

        final long[] next = new long[width * ySections];
        System.arraycopy(data, ySections, next, ySections, (width - 2) * ySections);
        for (int a = 0; a < ySections; a++) {
            if(a > 0 && a < ySections - 1) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                            & data[i - ySections]
                            & data[i + ySections]
                            & ((data[i - ySections] << 1) | ((data[i - 1 - ySections] & 0x8000000000000000L) >>> 63))
                            & ((data[i + ySections] << 1) | ((data[i - 1 + ySections] & 0x8000000000000000L) >>> 63))
                            & ((data[i - ySections] >>> 1) | ((data[i + 1 - ySections] & 1L) << 63))
                            & ((data[i + ySections] >>> 1) | ((data[i + 1 + ySections] & 1L) << 63));
                }
            }
            else if(a > 0) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & (data[i] >>> 1)
                            & data[i - ySections]
                            & data[i + ySections]
                            & ((data[i - ySections] << 1) | ((data[i - 1 - ySections] & 0x8000000000000000L) >>> 63))
                            & ((data[i + ySections] << 1) | ((data[i - 1 + ySections] & 0x8000000000000000L) >>> 63))
                            & (data[i - ySections] >>> 1)
                            & (data[i + ySections] >>> 1);
                }
            }
            else if(a < ySections - 1) {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (data[i] << 1)
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                            & data[i - ySections]
                            & data[i + ySections]
                            & (data[i - ySections] << 1)
                            & (data[i + ySections] << 1)
                            & ((data[i - ySections] >>> 1) | ((data[i + 1 - ySections] & 1L) << 63))
                            & ((data[i + ySections] >>> 1) | ((data[i + 1 + ySections] & 1L) << 63));
                }
            }
            else // only the case when ySections == 1
            {
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (data[i] << 1)
                            & (data[i] >>> 1)
                            & data[i - ySections]
                            & data[i + ySections]
                            & (data[i - ySections] << 1)
                            & (data[i + ySections] << 1)
                            & (data[i - ySections] >>> 1)
                            & (data[i + ySections] >>> 1);
                }
            }
        }

        if(yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }

    public GreasedRegion retract8way(int amount)
    {
        for (int i = 0; i < amount; i++) {
            retract8way();
        }
        return this;
    }

    public GreasedRegion[] retractSeries8way(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        GreasedRegion[] regions = new GreasedRegion[amount];
        GreasedRegion temp = new GreasedRegion(this);
        for (int i = 0; i < amount; i++) {
            regions[i] = new GreasedRegion(temp.retract8way());
        }
        return regions;
    }

    public ArrayList<GreasedRegion> retractSeriesToLimit8way()
    {
        ArrayList<GreasedRegion> regions = new ArrayList<>();
        GreasedRegion temp = new GreasedRegion(this);
        while (!temp.retract8way().isEmpty()) {
            regions.add(new GreasedRegion(temp));
        }
        return regions;
    }

    public GreasedRegion surface8way()
    {
        GreasedRegion cpy = new GreasedRegion(this).retract8way();
        return xor(cpy);
    }

    public GreasedRegion surface8way(int amount)
    {
        GreasedRegion cpy = new GreasedRegion(this).retract8way(amount);
        return xor(cpy);
    }

    public GreasedRegion[] surfaceSeries8way(int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        GreasedRegion[] regions = new GreasedRegion[amount];
        GreasedRegion temp = new GreasedRegion(this);
        regions[0] = new GreasedRegion(temp);
        for (int i = 1; i < amount; i++) {
            regions[i] = new GreasedRegion(temp.retract8way());
        }
        for (int i = 0; i < amount - 1; i++) {
            regions[i].xor(regions[i + 1]);
        }
        regions[amount - 1].surface8way();
        return regions;
    }
    public ArrayList<GreasedRegion> surfaceSeriesToLimit8way()
    {
        ArrayList<GreasedRegion> regions = retractSeriesToLimit8way();
        if(regions.isEmpty())
            return regions;
        regions.add(0, regions.get(0).copy().xor(this));
        for (int i = 1; i < regions.size() - 1; i++) {
            regions.get(i).xor(regions.get(i+1));
        }
        return regions;
    }
    public GreasedRegion flood(GreasedRegion bounds)
    {
        if(width < 2 || ySections <= 0 || bounds == null || bounds.width < 2 || bounds.ySections <= 0)
            return this;

        final long[] next = new long[width * ySections];
        for (int a = 0; a < ySections && a < bounds.ySections; a++) {
            next[a] |= (data[a] |(data[a] << 1) | (data[a] >>> 1) | data[a+ySections]) & bounds.data[a];
            next[(width-1)*ySections+a] |= (data[(width-1)*ySections+a] | (data[(width-1)*ySections+a] << 1)
                    | (data[(width-1)*ySections+a] >>> 1) | data[(width-2) *ySections+a]) & bounds.data[(width-1)*bounds.ySections+a];

            for (int i = ySections+a, j = bounds.ySections+a; i < (width - 1) * ySections &&
                    j < (bounds.width - 1) * bounds.ySections; i+= ySections, j+= bounds.ySections) {
                next[i] |= (data[i] | (data[i] << 1) | (data[i] >>> 1) | data[i - ySections] | data[i + ySections]) & bounds.data[j];
            }

            if(a > 0) {
                for (int i = ySections+a, j = bounds.ySections+a; i < (width-1) * ySections && j < (bounds.width-1) * bounds.ySections;
                     i+= ySections, j += bounds.ySections) {
                    next[i] |= (data[i] | ((data[i - 1] & 0x8000000000000000L) >>> 63)) & bounds.data[j];
                }
            }

            if(a < ySections - 1 && a < bounds.ySections - 1) {
                for (int i = ySections+a, j = bounds.ySections+a;
                     i < (width-1) * ySections && j < (bounds.width-1) * bounds.ySections; i+= ySections, j += bounds.ySections) {
                    next[i] |= (data[i] | ((data[i + 1] & 1L) << 63)) & bounds.data[j];
                }
            }
        }

        if(yEndMask != -1 && bounds.yEndMask != -1) {
            if(ySections == bounds.ySections) {
                long mask = ((yEndMask >>> 1) <= (bounds.yEndMask >>> 1))
                        ? yEndMask : bounds.yEndMask;
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= mask;
                }
            }
            else if(ySections < bounds.ySections) {
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= yEndMask;
                }
            }
            else {
                for (int a = bounds.ySections - 1; a < next.length; a += ySections) {
                    next[a] &= bounds.yEndMask;
                }
            }
        }
        data = next;
        return this;
    }

    public GreasedRegion flood(GreasedRegion bounds, int amount)
    {
        int ct = size(), ct2;
        for (int i = 0; i < amount; i++) {
            flood(bounds);
            if(ct == (ct2 = size()))
                break;
            else
                ct = ct2;

        }
        return this;
    }


    public GreasedRegion[] floodSeries(GreasedRegion bounds, int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        int ct = size(), ct2;
        GreasedRegion[] regions = new GreasedRegion[amount];
        boolean done = false;
        GreasedRegion temp = new GreasedRegion(this);
        for (int i = 0; i < amount; i++) {
            if(done) {
                regions[i] = new GreasedRegion(temp);
            }
            else {
                regions[i] = new GreasedRegion(temp.flood(bounds));
                if (ct == (ct2 = temp.size()))
                    done = true;
                else
                    ct = ct2;
            }
        }
        return regions;
    }

    public ArrayList<GreasedRegion> floodSeriesToLimit(GreasedRegion bounds) {
        int ct = size(), ct2;
        ArrayList<GreasedRegion> regions = new ArrayList<>();
        GreasedRegion temp = new GreasedRegion(this);
        while (true) {
            temp.flood(bounds);
            if (ct == (ct2 = temp.size()))
                return regions;
            else {
                ct = ct2;
                regions.add(new GreasedRegion(temp));
            }
        }
    }

    public GreasedRegion flood8way(GreasedRegion bounds)
    {
        if(width < 2 || ySections <= 0 || bounds == null || bounds.width < 2 || bounds.ySections <= 0)
            return this;

        final long[] next = new long[width * ySections];
        for (int a = 0; a < ySections && a < bounds.ySections; a++) {
            next[a] |= (data[a] | (data[a] << 1) | (data[a] >>> 1)
                    | data[a+ySections] | (data[a+ySections] << 1) | (data[a+ySections] >>> 1)) & bounds.data[a];
            next[(width-1)*ySections+a] |= (data[(width-1)*ySections+a]
                    | (data[(width-1)*ySections+a] << 1) | (data[(width-1)*ySections+a] >>> 1)
                    | data[(width-2) *ySections+a] | (data[(width-2)*ySections+a] << 1) | (data[(width-2)*ySections+a] >>> 1))
                    & bounds.data[(width-1)*bounds.ySections+a];

            for (int i = ySections+a, j = bounds.ySections+a; i < (width - 1) * ySections &&
                    j < (bounds.width - 1) * bounds.ySections; i+= ySections, j+= bounds.ySections) {
                next[i] |= (data[i] | (data[i] << 1) | (data[i] >>> 1)
                        | data[i - ySections] | (data[i - ySections] << 1) | (data[i - ySections] >>> 1)
                        | data[i + ySections] | (data[i + ySections] << 1) | (data[i + ySections] >>> 1))
                        & bounds.data[j];
            }

            if(a > 0) {
                for (int i = ySections+a, j = bounds.ySections+a; i < (width-1) * ySections && j < (bounds.width-1) * bounds.ySections;
                     i+= ySections, j += bounds.ySections) {
                    next[i] |= (data[i] | ((data[i - 1] & 0x8000000000000000L) >>> 63) |
                            ((data[i - ySections - 1] & 0x8000000000000000L) >>> 63) |
                            ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63)) & bounds.data[j];
                }
            }

            if(a < ySections - 1 && a < bounds.ySections - 1) {
                for (int i = ySections+a, j = bounds.ySections+a;
                     i < (width-1) * ySections && j < (bounds.width-1) * bounds.ySections; i+= ySections, j += bounds.ySections) {
                    next[i] |= (data[i] | ((data[i + 1] & 1L) << 63) |
                            ((data[i - ySections + 1] & 1L) << 63) |
                            ((data[i + ySections+ 1] & 1L) << 63)) & bounds.data[j];
                }
            }
        }

        if(yEndMask != -1 && bounds.yEndMask != -1) {
            if(ySections == bounds.ySections) {
                long mask = ((yEndMask >>> 1) <= (bounds.yEndMask >>> 1))
                        ? yEndMask : bounds.yEndMask;
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= mask;
                }
            }
            else if(ySections < bounds.ySections) {
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= yEndMask;
                }
            }
            else {
                for (int a = bounds.ySections - 1; a < next.length; a += ySections) {
                    next[a] &= bounds.yEndMask;
                }
            }
        }
        data = next;
        return this;
    }

    public GreasedRegion flood8way(GreasedRegion bounds, int amount)
    {
        int ct = size(), ct2;
        for (int i = 0; i < amount; i++) {
            flood8way(bounds);
            if(ct == (ct2 = size()))
                break;
            else
                ct = ct2;
        }
        return this;
    }

    public GreasedRegion[] floodSeries8way(GreasedRegion bounds, int amount)
    {
        if(amount <= 0) return new GreasedRegion[0];
        int ct = size(), ct2;
        GreasedRegion[] regions = new GreasedRegion[amount];
        boolean done = false;
        GreasedRegion temp = new GreasedRegion(this);
        for (int i = 0; i < amount; i++) {
            if(done) {
                regions[i] = new GreasedRegion(temp);
            }
            else {
                regions[i] = new GreasedRegion(temp.flood8way(bounds));
                if (ct == (ct2 = temp.size()))
                    done = true;
                else
                    ct = ct2;
            }
        }
        return regions;
    }
    public ArrayList<GreasedRegion> floodSeriesToLimit8way(GreasedRegion bounds) {
        int ct = size(), ct2;
        ArrayList<GreasedRegion> regions = new ArrayList<>();
        GreasedRegion temp = new GreasedRegion(this);
        while (true) {
            temp.flood8way(bounds);
            if (ct == (ct2 = temp.size()))
                return regions;
            else {
                ct = ct2;
                regions.add(new GreasedRegion(temp));
            }
        }
    }
    public GreasedRegion spill(GreasedRegion bounds, int volume, IRNG rng)
    {
        if(width < 2 || ySections <= 0 || bounds == null || bounds.width < 2 || bounds.ySections <= 0)
            return this;
        int current = size();
        if(current >= volume)
            return this;
        GreasedRegion t = new GreasedRegion(this);
        Coord c = Coord.get(-1, -1);
        for (int i = current; i < volume; i++) {
            insert(t.remake(this).fringe().and(bounds).singleRandom(rng));
        }
        return this;
    }

    public GreasedRegion removeCorners()
    {
        if(width <= 2 || ySections <= 0)
            return this;

        final long[] next = new long[width * ySections];
        System.arraycopy(data, 0, next, 0, width * ySections);
        for (int a = 0; a < ySections; a++) {
            if(a > 0 && a < ySections - 1) {
                next[a] &= (((data[a] << 1) | ((data[a - 1] & 0x8000000000000000L) >>> 63))
                        & ((data[a] >>> 1) | ((data[a + 1] & 1L) << 63)));
                next[(width - 1) * ySections + a] &= (((data[(width - 1) * ySections + a] << 1)
                        | ((data[(width - 1) * ySections + a - 1] & 0x8000000000000000L) >>> 63))
                        & ((data[(width - 1) * ySections + a] >>> 1)
                        | ((data[(width - 1) * ySections + a + 1] & 1L) << 63)));
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63)))
                            | (data[i - ySections]
                            & data[i + ySections]);
                }
            }
            else if(a > 0) {
                next[a] &= (((data[a] << 1) | ((data[a - 1] & 0x8000000000000000L) >>> 63))
                        & (data[a] >>> 1));
                next[(width - 1) * ySections + a] &= (((data[(width - 1) * ySections + a] << 1)
                        | ((data[(width - 1) * ySections + a - 1] & 0x8000000000000000L) >>> 63))
                        & (data[(width - 1) * ySections + a] >>> 1));
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & (data[i] >>> 1))
                            | (data[i - ySections]
                            & data[i + ySections]);
                }
            }
            else if(a < ySections - 1) {
                next[a] &= ((data[a] << 1)
                        & ((data[a] >>> 1)
                        | ((data[a + 1] & 1L) << 63)));
                next[(width - 1) * ySections + a] &= ((data[(width - 1) * ySections + a] << 1)
                        & ((data[(width - 1) * ySections + a] >>> 1)
                        | ((data[(width - 1) * ySections + a + 1] & 1L) << 63)));
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= ((data[i] << 1)
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63)))
                            | (data[i - ySections]
                            & data[i + ySections]);
                }
            }
            else // only the case when ySections == 1
            {
                next[0] &= (data[0] << 1) & (data[0] >>> 1);
                next[width-1] &= (data[width-1] << 1) & (data[width-1] >>> 1);
                for (int i = 1+a; i < (width - 1); i++) {
                    next[i] &= ((data[i] << 1) & (data[i] >>> 1)) | (data[i - ySections] & data[i + ySections]);
                }
            }
        }

        if(yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        return this;
    }

    /**
     * If this GreasedRegion stores multiple unconnected "on" areas, this finds each isolated area (areas that
     * are only adjacent diagonally are considered separate from each other) and returns it as an element in an
     * ArrayList of GreasedRegion, with one GreasedRegion per isolated area. Not to be confused with
     * {@link #split8way()}, which considers diagonally-adjacent cells as part of one region, while this method requires
     * cells to be orthogonally adjacent.
     * <br>
     * Useful when you have, for example, all the rooms in a dungeon with their connecting corridors removed, but want
     * to separate the rooms. You can get the aforementioned data assuming a bare dungeon called map using:
     * <br>
     * {@code GreasedRegion floors = new GreasedRegion(map, '.'),
     * rooms = floors.copy().retract8way().flood(floors, 2),
     * corridors = floors.copy().andNot(rooms),
     * doors = rooms.copy().and(corridors.copy().fringe());}
     * <br>
     * You can then get all rooms as separate regions with {@code List<GreasedRegion> apart = split(rooms);}, or
     * substitute {@code split(corridors)} to get the corridors. The room-finding technique works by shrinking floors
     * by a radius of 1 (8-way), which causes thin areas like corridors of 2 or less width to be removed, then
     * flood-filling the floors out from the area that produces by 2 cells (4-way this time) to restore the original
     * size of non-corridor areas (plus some extra to ensure odd shapes are kept). Corridors are obtained by removing
     * the rooms from floors. The example code also gets the doors (which overlap with rooms, not corridors) by finding
     * where the a room and a corridor are adjacent. This technique is used with some enhancements in the RoomFinder
     * class.
     * @see squidpony.squidgrid.mapping.RoomFinder for a class that uses this technique without exposing GreasedRegion
     * @return an ArrayList containing each unconnected area from packed as a GreasedRegion element
     */
    public ArrayList<GreasedRegion> split()
    {
        ArrayList<GreasedRegion> scattered = new ArrayList<>(32);
        int fst = firstTight();
        GreasedRegion remaining = new GreasedRegion(this);
        while (fst >= 0) {
            GreasedRegion filled = new GreasedRegion(width, height).insert(fst).flood(remaining, width * height);
            scattered.add(filled);
            remaining.andNot(filled);
            fst = remaining.firstTight();
        }
        return scattered;
    }
    /**
     * If this GreasedRegion stores multiple unconnected "on" areas, this finds each isolated area (areas that
     * are only adjacent diagonally are considered <b>one area</b> with this) and returns it as an element in an
     * ArrayList of GreasedRegion, with one GreasedRegion per isolated area. This should not be confused with
     * {@link #split()}, which is almost identical except that split() considers only orthogonal connections, while this
     * method considers both orthogonal and diagonal connections between cells as joining an area.
     * <br>
     * Useful when you have, for example, all the rooms in a dungeon with their connecting corridors removed, but want
     * to separate the rooms. You can get the aforementioned data assuming a bare dungeon called map using:
     * <br>
     * {@code GreasedRegion floors = new GreasedRegion(map, '.'),
     * rooms = floors.copy().retract8way().flood(floors, 2),
     * corridors = floors.copy().andNot(rooms),
     * doors = rooms.copy().and(corridors.copy().fringe());}
     * <br>
     * You can then get all rooms as separate regions with {@code List<GreasedRegion> apart = split(rooms);}, or
     * substitute {@code split(corridors)} to get the corridors. The room-finding technique works by shrinking floors
     * by a radius of 1 (8-way), which causes thin areas like corridors of 2 or less width to be removed, then
     * flood-filling the floors out from the area that produces by 2 cells (4-way this time) to restore the original
     * size of non-corridor areas (plus some extra to ensure odd shapes are kept). Corridors are obtained by removing
     * the rooms from floors. The example code also gets the doors (which overlap with rooms, not corridors) by finding
     * where the a room and a corridor are adjacent. This technique is used with some enhancements in the RoomFinder
     * class.
     * @see squidpony.squidgrid.mapping.RoomFinder for a class that uses this technique without exposing GreasedRegion
     * @return an ArrayList containing each unconnected area from packed as a GreasedRegion element
     */
    public ArrayList<GreasedRegion> split8way()
    {
        ArrayList<GreasedRegion> scattered = new ArrayList<>(32);
        int fst = firstTight();
        GreasedRegion remaining = new GreasedRegion(this);
        while (fst >= 0) {
            GreasedRegion filled = new GreasedRegion(width, height).insert(fst).flood8way(remaining, width * height);
            scattered.add(filled);
            remaining.andNot(filled);
            fst = remaining.firstTight();
        }
        return scattered;
    }

    /**
     * Finds the largest contiguous area of "on" cells in this GreasedRegion and returns it; does not modify this
     * GreasedRegion. If there are multiple areas that are all equally large with no larger area, this returns the
     * region it checks first and still is largest (first determined by the same ordering {@link #nth(int)} takes).
     * This may return an empty GreasedRegion if there are no "on" cells, but it will never return null.
     * Here, contiguous means adjacent on an orthogonal direction, and this doesn't consider diagonally-connected cells
     * as contiguous unless they also have an orthogonal connection.
     * @return a new GreasedRegion that corresponds to the largest contiguous sub-region of "on" cells in this.
     */
    public GreasedRegion largestPart()
    {
        int fst = firstTight(), bestSize = 0, currentSize;
        GreasedRegion remaining = new GreasedRegion(this), filled = new GreasedRegion(width, height),
                choice = new GreasedRegion(width, height);
        while (fst >= 0) {
            filled.empty().insert(fst).flood(remaining, width * height);
            if((currentSize = filled.size()) > bestSize)
            {
                bestSize = currentSize;
                choice.remake(filled);
            }
            remaining.andNot(filled);
            fst = remaining.firstTight();
        }
        return choice;
    }

    /**
     * Finds the largest contiguous area of "on" cells in this GreasedRegion and returns it; does not modify this
     * GreasedRegion. If there are multiple areas that are all equally large with no larger area, this returns the
     * region it checks first and still is largest (first determined by the same ordering {@link #nth(int)} takes).
     * This may return an empty GreasedRegion if there are no "on" cells, but it will never return null.
     * Here, contiguous means adjacent on any 8-way direction, and considers cells as part of a contiguous area even if
     * all connections but one, which can be orthogonal or diagonal, are blocked by "off" cells.
     * @return a new GreasedRegion that corresponds to the largest contiguous sub-region of "on" cells in this.
     */
    public GreasedRegion largestPart8way()
    {
        int fst = firstTight(), bestSize = 0, currentSize;
        GreasedRegion remaining = new GreasedRegion(this), filled = new GreasedRegion(width, height),
                choice = new GreasedRegion(width, height);
        while (fst >= 0) {
            filled.empty().insert(fst).flood8way(remaining, width * height);
            if((currentSize = filled.size()) > bestSize)
            {
                bestSize = currentSize;
                choice.remake(filled);
            }
            remaining.andNot(filled);
            fst = remaining.firstTight();
        }
        return choice;
    }

    /**
     * Modifies this GreasedRegion so the only cells that will be "on" have a neighbor upwards when this is called.
     * Up is defined as negative y. Neighbors are "on" cells exactly one cell away. A cell can have a neighbor
     * without itself being on; this is useful when finding the "shadow" cast away from "on" cells in one direction.
     * @return this, after modifications, for chaining
     */
    public GreasedRegion neighborUp()
    {
        if(width < 2 || ySections <= 0)
            return this;
        for (int a = ySections - 1; a >= 0; a--) {
            if(a > 0) {
                for (int i = a; i < width * ySections; i+= ySections) {
                    data[i] = (data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63);
                }
            }
            else {
                for (int i = a; i < width * ySections; i+= ySections) {
                    data[i] = (data[i] << 1);
                }
            }

        }
        return this;
    }

    /**
     * Modifies this GreasedRegion so the only cells that will be "on" have a neighbor downwards when this is called.
     * Down is defined as positive y. Neighbors are "on" cells exactly one cell away. A cell can have a neighbor
     * without itself being on; this is useful when finding the "shadow" cast away from "on" cells in one direction.
     * @return this, after modifications, for chaining
     */
    public GreasedRegion neighborDown()
    {
        if(width < 2 || ySections <= 0)
            return this;
        for (int a = 0; a < ySections; a++) {
            if(a < ySections - 1) {
                for (int i = a; i < width * ySections; i+= ySections) {
                    data[i] = (data[i] >>> 1) | ((data[i + 1] & 1L) << 63);
                }
            }
            else {
                for (int i = a; i < width * ySections; i+= ySections) {
                    data[i] = (data[i] >>> 1);
                }
            }

        }
        return this;
    }

    /**
     * Modifies this GreasedRegion so the only cells that will be "on" have a neighbor to the left when this is called.
     * Left is defined as negative x. Neighbors are "on" cells exactly one cell away. A cell can have a neighbor
     * without itself being on; this is useful when finding the "shadow" cast away from "on" cells in one direction.
     * @return this, after modifications, for chaining
     */
    public GreasedRegion neighborLeft()
    {
        if(width < 2 || ySections <= 0)
            return this;
        for (int a = 0; a < ySections; a++) {
            for (int i = ySections * (width - 1) + a; i >= ySections; i-= ySections) {
                data[i] = data[i - ySections];
            }
            data[a] = 0L;
        }
        return this;
    }

    /**
     * Modifies this GreasedRegion so the only cells that will be "on" have a neighbor to the right when this is called.
     * Right is defined as positive x. Neighbors are "on" cells exactly one cell away. A cell can have a neighbor
     * without itself being on; this is useful when finding the "shadow" cast away from "on" cells in one direction.
     * @return this, after modifications, for chaining
     */
    public GreasedRegion neighborRight()
    {
        if(width < 2 || ySections <= 0)
            return this;
        for (int a = 0; a < ySections; a++) {
            for (int i = a; i < (width - 1) * ySections; i+= ySections) {
                data[i] = data[i + ySections];
            }
            data[(width-1)*ySections+a] = 0L;
        }
        return this;
    }

    /**
     * Modifies this GreasedRegion so the only cells that will be "on" have a neighbor upwards and to the left when this
     * is called. Up is defined as negative y, left as negative x. Neighbors are "on" cells exactly one cell away. A
     * cell can have a neighbor without itself being on; this is useful when finding the "shadow" cast away from "on"
     * cells in one direction.
     * @return this, after modifications, for chaining
     */
    public GreasedRegion neighborUpLeft()
    {
        if(width < 2 || ySections <= 0)
            return this;
        for (int a = ySections - 1; a >= 0; a--) {
            if(a > 0) {
                for (int i = ySections * (width - 1) + a; i >= ySections; i-= ySections) {
                    data[i] = (data[i - ySections] << 1) | ((data[i - ySections - 1] & 0x8000000000000000L) >>> 63);
                }
                data[a] = 0L;
            }
            else {
                for (int i = ySections * (width - 1) + a; i >= ySections; i-= ySections) {
                    data[i] = (data[i - ySections] << 1);
                }
                data[a] = 0L;
            }
        }
        return this;
    }

    /**
     * Modifies this GreasedRegion so the only cells that will be "on" have a neighbor upwards and to the right when
     * this is called. Up is defined as negative y, right as positive x. Neighbors are "on" cells exactly one cell away.
     * A cell can have a neighbor without itself being on; this is useful when finding the "shadow" cast away from
     * "on" cells in one direction.
     * @return this, after modifications, for chaining
     */
    public GreasedRegion neighborUpRight()
    {
        if(width < 2 || ySections <= 0)
            return this;
        for (int a = ySections - 1; a >= 0; a--) {
            if(a > 0) {
                for (int i = a; i < (width - 1) * ySections; i+= ySections) {
                    data[i] = (data[i + ySections] << 1) | ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63);
                }
                data[(width-1)*ySections+a] = 0L;
            }
            else {
                for (int i = a; i < (width - 1) * ySections; i+= ySections) {
                    data[i] = (data[i + ySections] << 1);
                }
                data[(width-1)*ySections+a] = 0L;
            }
        }
        return this;
    }

    /**
     * Modifies this GreasedRegion so the only cells that will be "on" have a neighbor downwards and to the left when
     * this is called. Down is defined as positive y, left as negative x. Neighbors are "on" cells exactly one cell
     * away. A cell can have a neighbor without itself being on; this is useful when finding the "shadow" cast away from
     * "on" cells in one direction.
     * @return this, after modifications, for chaining
     */
    public GreasedRegion neighborDownLeft()
    {
        if(width < 2 || ySections <= 0)
            return this;
        for (int a = 0; a < ySections; a++) {
            if(a < ySections - 1) {
                for (int i = ySections * (width - 1) + a; i >= ySections; i-= ySections) {
                    data[i] = (data[i - ySections] >>> 1) | ((data[i - ySections + 1] & 1L) << 63);
                }
                data[a] = 0L;
            }
            else {
                for (int i = ySections * (width - 1) + a; i >= ySections; i-= ySections) {
                    data[i] = (data[i - ySections] >>> 1);
                }
                data[a] = 0L;
            }
        }
        return this;
    }

    /**
     * Modifies this GreasedRegion so the only cells that will be "on" have a neighbor downwards and to the right when
     * this is called. Down is defined as positive y, right as positive x. Neighbors are "on" cells exactly one cell
     * away. A cell can have a neighbor without itself being on; this is useful when finding the "shadow" cast away from
     * "on" cells in one direction.
     * @return this, after modifications, for chaining
     */
    public GreasedRegion neighborDownRight()
    {
        if(width < 2 || ySections <= 0)
            return this;
        for (int a = 0; a < ySections; a++) {
            if(a < ySections - 1) {
                for (int i = a; i < (width - 1) * ySections; i+= ySections) {
                    data[i] = (data[i + ySections] >>> 1) | ((data[i + ySections + 1] & 1L) << 63);
                }
                data[(width-1)*ySections+a] = 0L;
            }
            else {
                for (int i = a; i < (width - 1) * ySections; i+= ySections) {
                    data[i] = (data[i + ySections] >>> 1);
                }
                data[(width-1)*ySections+a] = 0L;
            }
        }
        return this;
    }

    public GreasedRegion removeIsolated()
    {
        int fst = firstTight();
        GreasedRegion remaining = new GreasedRegion(this), filled = new GreasedRegion(this);
        while (fst >= 0) {
            filled.empty().insert(fst).flood(remaining, 8);
            if(filled.size() <= 4)
                andNot(filled);
            remaining.andNot(filled);
            fst = remaining.firstTight();
        }
        return this;
    }

    /**
     * Returns true if any cell is "on" in both this GreasedRegion and in other; returns false otherwise. For example,
     * if (1,1) is "on" in this and (1,1) is "on" in other, this would return true, regardless of other cells.
     * @param other another GreasedRegion; its size does not have to match this GreasedRegion's size
     * @return true if this shares any "on" cells with other
     */
    public boolean intersects(GreasedRegion other)
    {
        if(other == null)
            return false;
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                if((data[x * ySections + y] & other.data[x * ySections + y]) != 0)
                    return true;
            }
        }
        return false;
    }

    public static OrderedSet<GreasedRegion> whichContain(int x, int y, GreasedRegion ... packed)
    {
        OrderedSet<GreasedRegion> found = new OrderedSet<>(packed.length);
        GreasedRegion tmp;
        for (int i = 0; i < packed.length; i++) {
            if((tmp = packed[i]) != null && tmp.contains(x, y))
                found.add(tmp);
        }
        return found;
    }

    public static OrderedSet<GreasedRegion> whichContain(int x, int y, Collection<GreasedRegion> packed)
    {
        OrderedSet<GreasedRegion> found = new OrderedSet<>(packed.size());
        for (GreasedRegion tmp : packed) {
            if(tmp != null && tmp.contains(x, y))
                found.add(tmp);
        }
        return found;
    }


    public int size()
    {
        int c = 0;
        for (int i = 0; i < width * ySections; i++) {
            c += Long.bitCount(data[i]);
        }
        return c;
    }

    public Coord fit(double xFraction, double yFraction)
    {
        int tmp, xTotal = 0, yTotal = 0, xTarget, yTarget, bestX = -1;
        long t;
        int[] xCounts = new int[width];
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                t = data[x * ySections + s];
                if (t != 0) {
                    tmp = Long.bitCount(t);
                    xCounts[x] += tmp;
                    xTotal += tmp;
                }
            }
        }
        xTarget = (int)(xTotal * xFraction);
        for (int x = 0; x < width; x++) {
            if((xTarget -= xCounts[x]) < 0)
            {
                bestX = x;
                yTotal = xCounts[x];
                break;
            }
        }
        if(bestX < 0)
        {
            return Coord.get(-1, -1);
        }
        yTarget = (int)(yTotal * yFraction);

        for (int s = 0, y = 0; s < ySections; s++) {
            t = data[bestX * ySections + s];
            for (long cy = 1; cy != 0 && y < height; y++, cy <<= 1) {
                if((t & cy) != 0 && --yTarget < 0)
                {
                    return Coord.get(bestX, y);
                }
            }
        }

        return Coord.get(-1, -1);
    }

    public int[][] fit(int[][] basis, int defaultValue)
    {
        int[][] next = ArrayTools.fill(defaultValue, width, height);
        if(basis == null || basis.length <= 0 || basis[0] == null || basis[0].length <= 0)
            return next;
        int tmp, xTotal = 0, yTotal = 0, xTarget, yTarget, bestX = -1, oX = basis.length, oY = basis[0].length, ao;
        long t;
        int[] xCounts = new int[width];
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                t = data[x * ySections + s];
                if (t != 0) {
                    tmp = Long.bitCount(t);
                    xCounts[x] += tmp;
                    xTotal += tmp;
                }
            }
        }
        if(xTotal <= 0)
            return next;
        for (int aX = 0; aX < oX; aX++) {
            CELL_WISE:
            for (int aY = 0; aY < oY; aY++) {
                if((ao = basis[aX][aY]) == defaultValue)
                    continue;
                xTarget = xTotal * aX / oX;
                for (int x = 0; x < width; x++) {
                    if((xTarget -= xCounts[x]) < 0)
                    {
                        bestX = x;
                        yTotal = xCounts[x];
                        yTarget = yTotal * aY / oY;
                        for (int s = 0, y = 0; s < ySections; s++) {
                            t = data[bestX * ySections + s];
                            for (long cy = 1; cy != 0 && y < height; y++, cy <<= 1) {
                                if((t & cy) != 0 && --yTarget < 0)
                                {
                                    next[bestX][y] = ao;
                                    continue CELL_WISE;
                                }
                            }
                        }
                        continue CELL_WISE;
                    }
                }

            }
        }

        return next;
    }
    
    /**
     * Don't use this in new code; prefer {@link #mixedRandomSeparated(double)}, {@link #quasiRandomSeparated(double)},
     * or {@link #separatedZCurve(double)}. This method has issues with being unable to fill the requested fraction, but
     * the others mentioned don't. See their documentation for what all these group of methods do.
     * @param fraction between 0.0 and 1.0
     * @return a Coord array that may not have the full fraction used; you have been advised
     */
    public Coord[] separatedPortion(double fraction)
    {
        if(fraction < 0)
            return new Coord[0];
        if(fraction > 1)
            fraction = 1;
        int ct, tmp, xTotal = 0, yTotal = 0, xTarget, yTarget, bestX = -1;
        long t;
        int[] xCounts = new int[width];
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                t = data[x * ySections + s];
                if (t != 0) {
                    tmp = Long.bitCount(t);
                    xCounts[x] += tmp;
                    xTotal += tmp;
                }
            }
        }
        Coord[] vl = new Coord[ct = (int)(fraction * xTotal)];
        double[] vec = new double[2];
        sobol.skipTo(1337);
        EACH_SOBOL:
        for (int i = 0; i < ct; i++)
        {
            sobol.fillVector(vec);
            xTarget = (int) (xTotal * vec[0]);
            for (int x = 0; x < width; x++) {
                if ((xTarget -= xCounts[x]) < 0) {
                    bestX = x;
                    yTotal = xCounts[x];
                    break;
                }
            }
            yTarget = (int) (yTotal * vec[1]);

            for (int s = 0, y = 0; s < ySections; s++) {
                t = data[bestX * ySections + s];
                for (long cy = 1; cy != 0 && y < height; y++, cy <<= 1) {
                    if ((t & cy) != 0 && --yTarget < 0) {
                        vl[i] = Coord.get(bestX, y);
                        continue EACH_SOBOL;
                    }
                }
            }
        }
        return vl;

    }
    /**
     * Don't use this in new code; prefer {@link #mixedRandomSeparated(double, int, long)} with a random long as the
     * last parameter. This method has issues with being unable to fill the requested fraction, but mixedRandomSeparated
     * does not. See its documentation for what this method is supposed to do.
     * @param fraction between 0.0 and 1.0
     * @param rng an IRNG that will be used to get a random starting point in the Sobol sequence this uses internally
     * @return a Coord array that may not have the full fraction used; you have been advised
     */
    public Coord[] randomSeparated(double fraction, IRNG rng)
    {
        return randomSeparated(fraction, rng, -1);
    }
    /**
     * Don't use this in new code; prefer {@link #mixedRandomSeparated(double, int, long)} with a random long as the
     * last parameter. This method has issues with being unable to fill the requested fraction, but mixedRandomSeparated
     * does not. See its documentation for what this method is supposed to do.
     * @param fraction between 0.0 and 1.0
     * @param rng an IRNG that will be used to get a random starting point in the Sobol sequence this uses internally
     * @param limit how many Coord values this should return, at most; typically this will return less
     * @return a Coord array that may not have the full fraction used; you have been advised
     */
    public Coord[] randomSeparated(double fraction, IRNG rng, int limit)
    {
        if(fraction < 0)
            return new Coord[0];
        if(fraction > 1)
            fraction = 1;
        int ct, tmp, xTotal = 0, yTotal = 0, xTarget, yTarget, bestX = -1;
        long t;
        int[] xCounts = new int[width];
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                t = data[x * ySections + s];
                if (t != 0) {
                    tmp = Long.bitCount(t);
                    xCounts[x] += tmp;
                    xTotal += tmp;
                }
            }
        }
        ct = (int)(fraction * xTotal);
        if(limit >= 0 && limit < ct)
            ct = limit;
        Coord[] vl = new Coord[ct];
        double[] vec = new double[2];
        sobol.skipTo(rng.between(1000, 65000));
        EACH_SOBOL:
        for (int i = 0; i < ct; i++)
        {
            sobol.fillVector(vec);
            xTarget = (int) (xTotal * vec[0]);
            for (int x = 0; x < width; x++) {
                if ((xTarget -= xCounts[x]) < 0) {
                    bestX = x;
                    yTotal = xCounts[x];
                    break;
                }
            }
            yTarget = (int) (yTotal * vec[1]);

            for (int s = 0, y = 0; s < ySections; s++) {
                t = data[bestX * ySections + s];
                for (long cy = 1; cy != 0 && y < height; y++, cy <<= 1) {
                    if ((t & cy) != 0 && --yTarget < 0) {
                        vl[i] = Coord.get(bestX, y);
                        continue EACH_SOBOL;
                    }
                }
            }
        }
        return vl;

    }

    /**
     * Gets a Coord array from the "on" contents of this GreasedRegion, using a deterministic but random-seeming
     * scattering of chosen cells with a count that matches the given {@code fraction} of the total amount of "on" cells
     * in this. This is pseudo-random with a fixed seed, but is very good at avoiding overlap (just as good as
     * {@link #separatedRegionZCurve(double, int)}, and probably faster). If you request too many cells (too high of a
     * value for fraction), it will start to overlap, but a fraction value of 0.4 reliably has had no overlap in
     * testing. Does not restrict the size of the returned array other than only using up to
     * {@code fraction * size()} cells.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @return a freshly-allocated Coord array containing the quasi-random cells
     */
    public Coord[] mixedRandomSeparated(double fraction)
    {
        return mixedRandomSeparated(fraction, -1, 1L);
    }

    /**
     * Gets a Coord array from the "on" contents of this GreasedRegion, using a deterministic but random-seeming
     * scattering of chosen cells with a count that matches the given {@code fraction} of the total amount of "on" cells
     * in this. This is pseudo-random with a fixed seed, but is very good at avoiding overlap (just as good as
     * {@link #separatedRegionZCurve(double, int)}, and probably faster). If you request too many cells (too high of a
     * value for fraction), it will start to overlap, but a fraction value of 0.4 reliably has had no overlap in
     * testing. Restricts the total size of the returned array to a maximum of {@code limit} (minimum is 0 if no cells
     * are "on"). If limit is negative, this will not restrict the size.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @param limit the maximum size of the array to return
     * @return a freshly-allocated Coord array containing the pseudo-random cells
     */
    public Coord[] mixedRandomSeparated(double fraction, int limit)
    {
        return mixedRandomSeparated(fraction, limit, 1L);
    }

    /**
     * Gets a Coord array from the "on" contents of this GreasedRegion, using a deterministic but random-seeming
     * scattering of chosen cells with a count that matches the given {@code fraction} of the total amount of "on" cells
     * in this. This is pseudo-random with the given seed (which will be made into an odd number if it is not one
     * already), and is very good at avoiding overlap (just as good as {@link #separatedZCurve(double, int)}, and
     * probably faster). If you request too many cells (too high of a value for fraction), it will start to overlap, but
     * a fraction value of 0.4 reliably has had no overlap in testing. Restricts the total size of the returned array to
     * a maximum of {@code limit} (minimum is 0 if no cells are "on"). If limit is negative, this will not restrict the
     * size.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @param limit the maximum size of the array to return
     * @param seed a long seed to change the points; the most significant 21 bits (except the sign bit) and least significant bit are ignored
     * @return a freshly-allocated Coord array containing the pseudo-random cells
     */
    public Coord[] mixedRandomSeparated(double fraction, int limit, long seed)
    {
        if(fraction < 0)
            return new Coord[0];
        if(fraction > 1)
            fraction = 1;
        int ct = 0, tmp, ic;
        long t, w;
        int[] counts = new int[width * ySections];
        for (int i = 0; i < width * ySections; i++) {
            tmp = Long.bitCount(data[i]);
            counts[i] = tmp == 0 ? -1 : (ct += tmp);
        }
        seed |= 1L;
        final int total = ct;
        ct *= fraction;// (int)(fraction * ct);
        if(limit >= 0 && limit < ct)
            ct = limit;
        Coord[] vl = new Coord[ct];
        EACH_QUASI:
        for (int i = 0; i < ct; i++)
        {
            tmp = (int)(VanDerCorputQRNG.altDetermine(seed, i+1) * total);
            
            for (int s = 0; s < ySections; s++) {
                for (int x = 0; x < width; x++) {
                    if ((ic = counts[x * ySections + s]) > tmp) {
                        t = data[x * ySections + s];
                        w = NumberTools.lowestOneBit(t);
                        for (--ic; w != 0; ic--) {
                            if (ic == tmp) {
                                vl[i] = Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                                continue EACH_QUASI;
                            }
                            t ^= w;
                            w = NumberTools.lowestOneBit(t);
                        }
                    }
                }
            }
        }
        return vl;
    }



    /**
     * Modifies this GreasedRegion so it contains a deterministic but random-seeming subset of its previous contents,
     * choosing cells so that the {@link #size()} matches the given {@code fraction} of the total amount of "on" cells
     * in this. This is pseudo-random with a fixed seed, and is very good at avoiding overlap (just as good as
     * {@link #separatedRegionZCurve(double, int)}, and probably faster). If you request too many cells (too high of a
     * value for fraction), it will start to overlap, but a fraction value of 0.4 reliably has had no overlap in
     * testing. Does not restrict the count of "on" cells after this returns other than by only using up to
     * {@code fraction * size()} cells.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @return this for chaining
     */
    public GreasedRegion mixedRandomRegion(double fraction) {
        return mixedRandomRegion(fraction, -1, 1L);
    }
    /**
     * Modifies this GreasedRegion so it contains a deterministic but random-seeming subset of its previous contents,
     * choosing cells so that the {@link #size()} matches the given {@code fraction} of the total amount of "on" cells
     * in this. This is pseudo-random with a fixed seed, and is very good at avoiding overlap (just as good as
     * {@link #separatedRegionZCurve(double, int)}, and probably faster). If you request too many cells (too high of a
     * value for fraction), it will start to overlap, but a fraction value of 0.4 reliably has had no overlap in
     * testing. Restricts the total count of "on" cells after this returns to a maximum of {@code limit} (minimum is 0
     * if no cells are "on"). If limit is negative, this will not restrict the count.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @param limit the maximum count of "on" cells to keep
     * @return this for chaining
     */
    public GreasedRegion mixedRandomRegion(double fraction, int limit) {
        return mixedRandomRegion(fraction, limit, 1L);
    }
    /**
     * Modifies this GreasedRegion so it contains a deterministic but random-seeming subset of its previous contents,
     * choosing cells so that the {@link #size()} matches the given {@code fraction} of the total amount of "on" cells
     * in this. This is pseudo-random with the given seed (which will be made into an odd number if it is not one
     * already), and is very good at avoiding overlap (just as good as {@link #separatedZCurve(double, int)}, and
     * probably faster). If you request too many cells (too high of a value for fraction), it will start to overlap, but
     * a fraction value of 0.4 reliably has had no overlap in testing. Restricts the total count of "on" cells after
     * this returns to a maximum of {@code limit} (minimum is 0 if no cells are "on"). If limit is negative, this will
     * not restrict the count.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @param limit the maximum count of "on" cells to keep
     * @return this for chaining
     */
    public GreasedRegion mixedRandomRegion(double fraction, int limit, long seed) {
        int ct = 0, idx, run = 0;
        for (int i = 0; i < width * ySections; i++) {
            ct += Long.bitCount(data[i]);
        }
        if (ct <= limit)
            return this;
        if (ct <= 0)
            return empty();
        if (limit < 0)
            limit = (int) (fraction * ct);
        if(limit <= 0)
            return empty();
        seed |= 1L;
        final int total = ct;
        int[] order = new int[limit];
        for (int i = 0, m = 1; i < limit; i++, m++) {
            idx = (int)(VanDerCorputQRNG.altDetermine(seed, m) * total);
            BIG:
            while (true) {
                for (int j = 0; j < i; j++) {
                    if (order[j] == idx) {
                        idx = (int)(VanDerCorputQRNG.altDetermine(seed, ++m) * total);
                        continue BIG;
                    }
                }
                break;
            }
            order[i] = idx;
        }
        idx = 0;
        Arrays.sort(order);
        long t, w;
        ALL:
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                if ((t = data[x * ySections + s]) != 0) {
                    w = NumberTools.lowestOneBit(t);
                    while (w != 0) {
                        if (run++ == order[idx]) {
                            if (++idx >= limit) {
                                data[x * ySections + s] &= (w<<1)-1;
                                for (int rx = x+1; rx < width; rx++) {
                                    data[rx * ySections + s] = 0;
                                }
                                for (int rs = s+1; rs < ySections; rs++) {
                                    for (int rx = 0; rx < width; rx++) {
                                        data[rx * ySections + rs] = 0;
                                    }
                                }
                                break ALL;
                            }
                        } else {
                            data[x * ySections + s] ^= w;
                        }
                        t ^= w;
                        w = NumberTools.lowestOneBit(t);
                    }
                }
            }
        }
        return this;
    }

    /**
     * Gets a Coord array from the "on" contents of this GreasedRegion, using a quasi-random scattering of chosen cells
     * with a count that matches the given {@code fraction} of the total amount of "on" cells in this. This is quasi-
     * random instead of pseudo-random because it is somewhat less likely to produce nearby cells in the result. If you
     * request too many cells (too high of a value for fraction), it will start to overlap, however.
     * Does not restrict the size of the returned array other than only using up to {@code fraction * size()} cells.
     * <br>
     * You can choose between {@link #mixedRandomSeparated(double)}, {@link #separatedZCurve(double)}, and this method,
     * where all are quasi-random, mixedRandom is probably fastest, ZCurve may have better 2-dimensional gaps between
     * cells, and this method is somewhere in the middle.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @return a freshly-allocated Coord array containing the quasi-random cells
     */
    public Coord[] quasiRandomSeparated(double fraction)
    {
        return quasiRandomSeparated(fraction, -1);
    }

    /**
     * Gets a Coord array from the "on" contents of this GreasedRegion, using a quasi-random scattering of chosen cells
     * with a count that matches the given {@code fraction} of the total amount of "on" cells in this. This is quasi-
     * random instead of pseudo-random because it is somewhat less likely to produce nearby cells in the result. If you
     * request too many cells (too high of a value for fraction), it will start to overlap, however.
     * Restricts the total size of the returned array to a maximum of {@code limit} (minimum is 0 if no cells are "on").
     * If limit is negative, this will not restrict the size.
     * <br>
     * You can choose between {@link #mixedRandomSeparated(double, int)}, {@link #separatedZCurve(double, int)}, and
     * this method, where all are quasi-random, mixedRandom is probably fastest, ZCurve may have better 2-dimensional
     * gaps between cells, and this method is somewhere in the middle.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @param limit the maximum size of the array to return
     * @return a freshly-allocated Coord array containing the quasi-random cells
     */
    public Coord[] quasiRandomSeparated(double fraction, int limit)
    {
        if(fraction < 0)
            return new Coord[0];
        if(fraction > 1)
            fraction = 1;
        int ct = 0, tmp, total, ic;
        long t, w;
        int[] counts = new int[width * ySections];
        for (int i = 0; i < width * ySections; i++) {
            tmp = Long.bitCount(data[i]);
            counts[i] = tmp == 0 ? -1 : (ct += tmp);
        }
        total = ct;
        ct *= fraction;// (int)(fraction * ct);
        if(limit >= 0 && limit < ct)
            ct = limit;
        Coord[] vl = new Coord[ct];
        EACH_QUASI:
        for (int i = 0; i < ct; i++)
        {
            tmp = (int)(VanDerCorputQRNG.determine2(i ^ i >>> 1) * total);
            for (int s = 0; s < ySections; s++) {
                for (int x = 0; x < width; x++) {
                    if ((ic = counts[x * ySections + s]) > tmp) {
                        t = data[x * ySections + s];
                        w = NumberTools.lowestOneBit(t);
                        for (--ic; w != 0; ic--) {
                            if (ic == tmp) {
                                vl[i] = Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                                continue EACH_QUASI;
                            }
                            t ^= w;
                            w = NumberTools.lowestOneBit(t);
                        }
                    }
                }
            }
            vl[i] = atFraction(tmp);
        }
        return vl;
    }



    /**
     * Modifies this GreasedRegion so it contains a quasi-random subset of its previous contents, choosing cells so that
     * the {@link #size()} matches the given {@code fraction} of the total amount of "on" cells in this. This is quasi-
     * random instead of pseudo-random because it is somewhat less likely to produce nearby cells in the result. If you
     * request too many cells (too high of a value for fraction), it will start to overlap, however.
     * Does not restrict the count of "on" cells after this returns other than by only using up to
     * {@code fraction * size()} cells.
     * <br>
     * You can choose between {@link #mixedRandomRegion(double)}, {@link #separatedRegionZCurve(double)}, and this
     * method, where all are quasi-random, mixedRandom is probably fastest, ZCurve may have better 2-dimensional gaps
     * between cells, and this method is somewhere in the middle.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @return this for chaining
     */
    public GreasedRegion quasiRandomRegion(double fraction) {
        return quasiRandomRegion(fraction, -1);
    }
    /**
     * Modifies this GreasedRegion so it contains a quasi-random subset of its previous contents, choosing cells so that
     * the {@link #size()} matches the given {@code fraction} of the total amount of "on" cells in this. This is quasi-
     * random instead of pseudo-random because it is somewhat less likely to produce nearby cells in the result. If you
     * request too many cells (too high of a value for fraction), it will start to overlap, however.
     * Restricts the total count of "on" cells after this returns to a maximum of {@code limit} (minimum is 0 if no
     * cells are "on"). If limit is negative, this will not restrict the count.
     * <br>
     * You can choose between {@link #mixedRandomRegion(double, int)}, {@link #separatedRegionZCurve(double, int)}, and
     * this method, where all are quasi-random, mixedRandom is probably fastest, ZCurve may have better 2-dimensional
     * gaps between cells, and this method is somewhere in the middle.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @param limit the maximum count of "on" cells to keep
     * @return this for chaining
     */
    public GreasedRegion quasiRandomRegion(double fraction, int limit) {
        int ct = 0, idx, run = 0;
        for (int i = 0; i < width * ySections; i++) {
            ct += Long.bitCount(data[i]);
        }
        if (ct <= limit)
            return this;
        if (ct <= 0)
            return empty();
        if (limit < 0)
            limit = (int) (fraction * ct);
        if(limit <= 0)
            return empty();
        int[] order = new int[limit];
        for (int i = 0, m = 0; i < limit; i++, m++) {
            idx = (int) (VanDerCorputQRNG.determine2(m ^ m >>> 1) * ct);
            BIG:
            while (true) {
                for (int j = 0; j < i; j++) {
                    if (order[j] == idx) {
                        idx = (int) (VanDerCorputQRNG.determine2(++m ^ m >>> 1) * ct);
                        continue BIG;
                    }
                }
                break;
            }
            order[i] = idx;
        }
        idx = 0;
        Arrays.sort(order);
        long t, w;
        ALL:
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                if ((t = data[x * ySections + s]) != 0) {
                    w = NumberTools.lowestOneBit(t);
                    while (w != 0) {
                        if (run++ == order[idx]) {
                            if (++idx >= limit) {
                                data[x * ySections + s] &= (w<<1)-1;
                                for (int rx = x+1; rx < width; rx++) {
                                    data[rx * ySections + s] = 0;
                                }
                                for (int rs = s+1; rs < ySections; rs++) {
                                    for (int rx = 0; rx < width; rx++) {
                                        data[rx * ySections + rs] = 0;
                                    }
                                }
                                break ALL;
                            }
                        } else {
                            data[x * ySections + s] ^= w;
                        }
                        t ^= w;
                        w = NumberTools.lowestOneBit(t);
                    }
                }
            }
        }
        return this;
    }

    /**
     * Like {@link #retract()}, this removes the "on" cells that are 4-way-adjacent to any "off" cell, but unlike that
     * method it keeps a fraction of those surface cells, quasi-randomly selecting them. This can be thought of as
     * running {@link #surface()} on a copy of this GreasedRegion, running {@link #quasiRandomRegion(double)} on that
     * surface with the given fractionKept, taking the original GreasedRegion and removing its whole surface with
     * {@link #retract()}, then inserting the quasi-randomly-removed surface into this GreasedRegion to replace its
     * surface with a randomly "damaged" one.
     * @param fractionKept the fraction between 0.0 and 1.0 of how many cells on the outer surface of this to keep "on"
     * @return this for chaining
     */
    public GreasedRegion fray(double fractionKept)
    {
        GreasedRegion cpy = new GreasedRegion(this).retract();
        return xor(cpy).quasiRandomRegion(1.0 - fractionKept).or(cpy);
    }

    /**
     * Modifies this GreasedRegion so it contains a random subset of its previous contents, choosing cells so that the
     * distance between any two "on" cells is at least {@code minimumDistance}, with at least one cell as "on" if any
     * were "on" in this originally. Does not limit the count of "on" cells in the result.
     * @param rng used to generate random positions
     * @param minimumDistance the minimum distance between "on" cells in the result
     * @return this for chaining
     */
    public GreasedRegion randomScatter(IRNG rng, int minimumDistance) {
        return randomScatter(rng, minimumDistance, -1);
    }
    /**
     * Modifies this GreasedRegion so it contains a random subset of its previous contents, choosing cells so that the
     * distance between any two "on" cells is at least {@code minimumDistance}, with at least one cell as "on" if any
     * were "on" in this originally.
     * Restricts the total count of "on" cells after this returns to a maximum of {@code limit} (minimum is 0 if no
     * cells are "on"). If limit is negative, this will not restrict the count.
     * @param rng used to generate random positions
     * @param minimumDistance the minimum distance between "on" cells in the result
     * @param limit the maximum count of "on" cells to keep
     * @return this for chaining
     */
    public GreasedRegion randomScatter(IRNG rng, int minimumDistance, int limit) {
        int ic = 0;
        for (; ic < width * ySections; ic++) {
            if(Long.bitCount(data[ic]) > 0)
                break;
        }
        if(ic == width * ySections)
            return this;
        if(limit == 0)
            return empty();
        else if(limit < 0)
            limit = 0x7fffffff;
        long[] data2 = new long[data.length];
        long t, w;
        int tmp, total = 0;
        MAIN_LOOP:
        while (total < limit) {
            int ct = 0;
            int[] counts = new int[width * ySections];
            for (int i = 0; i < width * ySections; i++) {
                tmp = Long.bitCount(data[i]);
                counts[i] = tmp == 0 ? -1 : (ct += tmp);
            }
            tmp = rng.nextInt(ct);

            for (int s = 0; s < ySections; s++) {
                for (int x = 0; x < width; x++) {
                    if ((ct = counts[x * ySections + s]) > tmp) {
                        t = data[x * ySections + s];
                        w = NumberTools.lowestOneBit(t);
                        for (--ct; w != 0; ct--) {
                            if (ct == tmp) {
                                data2[x * ySections + s] |= w;
                                ++total;
                                removeRectangle(x - minimumDistance,
                                        ((s << 6) | Long.numberOfTrailingZeros(w)) - minimumDistance,
                                        minimumDistance << 1 | 1, minimumDistance << 1 | 1);
                                continue MAIN_LOOP;
                            }
                            t ^= w;
                            w = NumberTools.lowestOneBit(t);
                        }
                    }
                }
            }
            break;
        }
        data = data2;
        return this;
    }

    public double rateDensity()
    {
        double sz = height * width;
        if(sz == 0)
            return 0;
        double onAmount = sz - size(), retractedOn = sz - copy().retract().size();
        return (onAmount + retractedOn) / (sz * 2.0);
    }
    public double rateRegularity()
    {
        GreasedRegion me2 = copy().surface8way();
        double irregularCount = me2.size();
        if(irregularCount == 0)
            return 0;
        return me2.remake(this).surface().size() / irregularCount;
    }

    private static int median(int[] working, int start, int amount)
    {
        Arrays.sort(working, start, start + amount);
        if ((amount & 1) == 0) {
            return working[start + (amount >> 1) - 1] + working[start + (amount >> 1)] >>> 1;
        } else {
            return working[start + (amount >> 1)];
        }
    }

    /**
     * Calculates a perceptual hash for this GreasedRegion using a method that is only precise for some sizes of
     * GreasedRegion; it writes a result to into, and uses working as a temporary buffer. The lengths of into and
     * working should be related; if into is length 1, then working should be length 64, and though the hash won't be
     * very detailed, it will work well for images with width and height that are multiples of 8; if into is length 4,
     * then working should be length 256, and this will work with more detail on images that have width and height that
     * are multiples of 16. If working is null or is too small, then this won't reuse it and will allocate an
     * appropriately-sized array for internal use.
     * <br>
     * Ported from https://github.com/commonsmachinery/blockhash/blob/master/blockhash.c , which is MIT-licensed.
     * @param into should be a long array of length 1 or 4; the contents don't matter and this will be where output is written to
     * @param working should be an int array of length 64 (if into has length 1) or 256 (if into has length 4); may be null if you like garbage collection
     */
    public void perceptualHashQuick(long[] into, int[] working)
    {
        final int bits = 8 << (Integer.numberOfTrailingZeros(Integer.highestOneBit(into.length)) >> 1);
        if(working == null || working.length < bits * bits)
            working = new int[bits * bits];
        final int blockWidth = width / bits, blockHeight = height / bits, blockWidthSections = blockWidth * ySections;
        if(blockHeight == 1)
        {
            for (int y = 0; y < bits; y++) {
                for (int x = 0; x < bits; x++) {
                    int value = 0;
                    for (int ix = 0; ix < blockWidthSections; ix += ySections) {
                        value += (data[x * blockWidthSections + ix + (y >> 6)] >>> (y & 63) & 1L);
                    }
                    working[x * bits + y] = value;
                }
            }
        }
        else if(blockHeight < 64 && Integer.bitCount(blockHeight) == 1) {
            final long yBlockMask = ~(-1L << blockHeight);
            final int divisorMask = (64 / blockHeight) - 1;
            long currentMask;
            int blockY = 0;
            for (int y = 0; y < bits; y++, blockY += blockHeight) {
                currentMask = yBlockMask << ((y & divisorMask) << blockHeight);
                for (int x = 0; x < bits; x++) {
                    int value = 0;
                    for (int ix = 0; ix < blockWidthSections; ix += ySections) {
                        value += Long.bitCount(data[x * blockWidthSections + ix + (blockY >> 6)] & currentMask);
                    }
                    working[x * bits + y] = value;
                }
            }
        }
        final int cellsPerBlock = blockWidth * blockHeight, numBlocks = bits * bits,
                halfCellCount = cellsPerBlock >>> 1;
        int bandSize = numBlocks >>> 2;
        int m, v;
        int currentInto = 0;
        long currentIntoPos = 1L;
        for (int i = 0; i < 4; i++) {
            m = median(working, i * bandSize, bandSize);
            for (int j = i * bandSize; j < (i + 1) * bandSize; j++) {
                v = working[j];
                if(v > m || (v - m == 0 && m > halfCellCount)) into[currentInto] |= currentIntoPos;
                if((currentIntoPos <<= 1) == 0)
                {
                    ++currentInto;
                    currentIntoPos = 1L;
                }
            }
        }
    }

    /*
    // This showed a strong x-y correlation because it didn't have a way to use a non-base-2 van der Corput sequence.
    // It also produced very close-together points, unfortunately.
    public static double quasiRandomX(int idx)
    {
        return atVDCSequence(26 + idx * 5);
    }
    public static double quasiRandomY(int idx)
    {
        return atVDCSequence(19 + idx * 3);
    }

    private static double atVDCSequence(int idx)
    {
        int leading = Integer.numberOfLeadingZeros(idx);
        return (Integer.reverse(idx) >>> leading) / (1.0 * (1 << (32 - leading)));
    }
    */
    public Coord[] asCoords()
    {
        return asCoords(new Coord[size()]);

    }
    public Coord[] asCoords(Coord[] points)
    {
        if(points == null)
            points = new Coord[size()];
        int idx = 0, len = points.length;
        long t, w;
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if((t = data[x * ySections + s]) != 0)
                {
                    w = NumberTools.lowestOneBit(t);
                    while (w != 0) {
                        if(idx >= len) return points;
                        points[idx++] = Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                        t ^= w;
                        w = NumberTools.lowestOneBit(t);
                    }
                }
            }
        }
        return points;
    }
    public int[] asEncoded()
    {
        int ct = 0, idx = 0;
        for (int i = 0; i < width * ySections; i++) {
            ct += Long.bitCount(data[i]);
        }
        int[] points = new int[ct];
        long t, w;
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if((t = data[x * ySections + s]) != 0)
                {
                    w = NumberTools.lowestOneBit(t);
                    while (w != 0) {
                        points[idx++] = Coord.pureEncode(x, (s << 6) | Long.numberOfTrailingZeros(w));
                        t ^= w;
                        w = NumberTools.lowestOneBit(t);
                    }
                }
            }
        }
        return points;
    }
    public int[] asTightEncoded()
    {
        int ct = 0, idx = 0;
        for (int i = 0; i < width * ySections; i++) {
            ct += Long.bitCount(data[i]);
        }
        int[] points = new int[ct];
        long t, w;
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if((t = data[x * ySections + s]) != 0)
                {
                    w = NumberTools.lowestOneBit(t);
                    while (w != 0) {
                        points[idx++] =  ((s << 6) | Long.numberOfTrailingZeros(w)) * width + x;
                        t ^= w;
                        w = NumberTools.lowestOneBit(t);
                    }
                }
            }
        }
        return points;
    }

    /**
     * @return All cells in this zone.
     */
    @Override
    public List<Coord> getAll() {
        ArrayList<Coord> points = new ArrayList<>();
        long t, w;
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if((t = data[x * ySections + s]) != 0)
                {
                    w = NumberTools.lowestOneBit(t);
                    while (w != 0) {
                        points.add(Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w)));
                        t ^= w;
                        w = NumberTools.lowestOneBit(t);
                    }
                }
            }
        }
        return points;

    }

    public Coord first()
    {
        long w;
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if ((w = NumberTools.lowestOneBit(data[x * ySections + s])) != 0) {
                    return Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                }
            }
        }
        return Coord.get(-1, -1);
    }

    public int firstTight()
    {
        long w;
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if ((w = NumberTools.lowestOneBit(data[x * ySections + s])) != 0) {
                    return ((s << 6) | Long.numberOfTrailingZeros(w)) * width + x;
                }
            }
        }
        return -1;
    }
    public Coord nth(final int index)
    {
        if(index < 0)
            return Coord.get(-1, -1);
        int ct = 0, tmp;
        int[] counts = new int[width * ySections];
        for (int i = 0; i < width * ySections; i++) {
            tmp = Long.bitCount(data[i]);
            counts[i] = tmp == 0 ? -1 : (ct += tmp);
        }
        if(index >= ct)
            return Coord.get(-1, -1);
        long t, w;
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                if ((ct = counts[x * ySections + s]) > index) {
                    t = data[x * ySections + s];
                    w = NumberTools.lowestOneBit(t);
                    for (--ct; w != 0; ct--) {
                        if (ct == index)
                            return Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                        t ^= w;
                        w = NumberTools.lowestOneBit(t);
                    }
                }
            }
        }
        return Coord.get(-1, -1);
    }

    public Coord atFraction(final double fraction)
    {
        int ct = 0, tmp;
        int[] counts = new int[width * ySections];
        for (int i = 0; i < width * ySections; i++) {
            tmp = Long.bitCount(data[i]);
            counts[i] = tmp == 0 ? -1 : (ct += tmp);
        }
        tmp = Math.abs((int)(fraction * ct) % ct);
        long t, w;
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                if ((ct = counts[x * ySections + s]) > tmp) {
                    t = data[x * ySections + s];
                    w = NumberTools.lowestOneBit(t);
                    for (--ct; w != 0; ct--) {
                        if (ct == tmp)
                            return Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                        t ^= w;
                        w = NumberTools.lowestOneBit(t);
                    }
                }
            }
        }
        return Coord.get(-1, -1);
    }

    public int atFractionTight(final double fraction)
    {
        int ct = 0, tmp;
        int[] counts = new int[width * ySections];
        for (int i = 0; i < width * ySections; i++) {
            tmp = Long.bitCount(data[i]);
            counts[i] = tmp == 0 ? -1 : (ct += tmp);
        }
        if(ct <= 0) return -1;
        tmp = Math.abs((int)(fraction * ct) % ct);
        long t, w;

        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if ((ct = counts[x * ySections + s]) > tmp) {
                    t = data[x * ySections + s];
                    w = NumberTools.lowestOneBit(t);
                    for (--ct; w != 0; ct--) {
                        if (ct == tmp)
                            return ((s << 6) | Long.numberOfTrailingZeros(w)) * width + x;
                        t ^= w;
                        w = NumberTools.lowestOneBit(t);
                    }
                }
            }
        }
        return -1;
    }

    public Coord singleRandom(IRNG rng)
    {
        int ct = 0, tmp;
        int[] counts = new int[width * ySections];
        for (int i = 0; i < width * ySections; i++) {
            tmp = Long.bitCount(data[i]);
            counts[i] = tmp == 0 ? -1 : (ct += tmp);
        }
        tmp = rng.nextInt(ct);
        long t, w;
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                if ((ct = counts[x * ySections + s]) > tmp) {
                    t = data[x * ySections + s];
                    w = NumberTools.lowestOneBit(t);
                    for (--ct; w != 0; ct--) {
                        if (ct == tmp)
                            return Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                        t ^= w;
                        w = NumberTools.lowestOneBit(t);
                    }
                }
            }
        }

        return Coord.get(-1, -1);
    }

    public int singleRandomTight(IRNG rng)
    {
        int ct = 0, tmp;
        int[] counts = new int[width * ySections];
        for (int i = 0; i < width * ySections; i++) {
            tmp = Long.bitCount(data[i]);
            counts[i] = tmp == 0 ? -1 : (ct += tmp);
        }
        tmp = rng.nextInt(ct);
        long t, w;
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                if ((ct = counts[x * ySections + s]) > tmp) {
                    t = data[x * ySections + s];
                    w = NumberTools.lowestOneBit(t);
                    for (--ct; w != 0; ct--) {
                        if (ct == tmp)
                            return ((s << 6) | Long.numberOfTrailingZeros(w)) * width + x;
                        t ^= w;
                        w = NumberTools.lowestOneBit(t);
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Narrow-purpose; takes an x and a y value, each between 0 and 65535 inclusive, and interleaves their bits so the
     * least significant bit and every other bit after it are filled with the bits of x, while the
     * second-least-significant bit and every other bit after that are filled with the bits of y. Essentially, this
     * takes two numbers with bits labeled like {@code a b c} for x and {@code R S T} for y and makes a number with
     * those bits arranged like {@code R a S b T c}.
     * @param x an int between 0 and 65535, inclusive
     * @param y an int between 0 and 65535, inclusive
     * @return an int that interleaves x and y, with x in the least significant bit position
     */
    public static int interleaveBits(int x, int y)
    {
        x |= y << 16;
        x =    ((x & 0x0000ff00) << 8) | ((x >>> 8) & 0x0000ff00) | (x & 0xff0000ff);
        x =    ((x & 0x00f000f0) << 4) | ((x >>> 4) & 0x00f000f0) | (x & 0xf00ff00f);
        x =    ((x & 0x0c0c0c0c) << 2) | ((x >>> 2) & 0x0c0c0c0c) | (x & 0xc3c3c3c3);
        return ((x & 0x22222222) << 1) | ((x >>> 1) & 0x22222222) | (x & 0x99999999);
    }

    /**
     * Narrow-purpose; takes an int that represents a distance down the Z-order curve and moves its bits around so that
     * its x component is stored in the bottom 16 bits (use {@code (n & 0xffff)} to obtain) and its y component is
     * stored in the upper 16 bits (use {@code (n >>> 16)} to obtain). This may be useful for ordering traversals of all
     * points in a GreasedRegion less predictably.
     * @param n an int that has already been interleaved, though this can really be any int
     * @return an int with x in its lower bits ({@code x = n & 0xffff;}) and y in its upper bits ({@code y = n >>> 16;})
     */
    public static int disperseBits(int n)
    {
        n =    ((n & 0x22222222) << 1) | ((n >>> 1) & 0x22222222) | (n & 0x99999999);
        n =    ((n & 0x0c0c0c0c) << 2) | ((n >>> 2) & 0x0c0c0c0c) | (n & 0xc3c3c3c3);
        n =    ((n & 0x00f000f0) << 4) | ((n >>> 4) & 0x00f000f0) | (n & 0xf00ff00f);
        return ((n & 0x0000ff00) << 8) | ((n >>> 8) & 0x0000ff00) | (n & 0xff0000ff);
    }
    private static int nextPowerOfTwo(int n)
    {
        final int highest = Integer.highestOneBit(n);
        return  (highest == NumberTools.lowestOneBit(n)) ? highest : highest << 1;
    }

    /**
     * Like {@link #nth(int)}, this gets the Coord at a given index along a path through the GreasedRegion, but unlike
     * nth(), this traverses the path in a zig-zag pattern called the Z-Order Curve. This method is often not very fast
     * compared to nth(), but this different path can help if iteration needs to seem less regular while still covering
     * all "on" cells in the GresedRegion eventually.
     * @param index the distance along the Z-order curve to travel, only counting "on" cells in this GreasedRegion.
     * @return the Coord at the given distance, or the Coord with x and y both -1 if index is too high or low
     */
    public Coord nthZCurve(final int index)
    {
        int ct = -1, x, y, s, d, max = nextPowerOfTwo(width) * nextPowerOfTwo(height);
        long t, w;
        for (int o = 0; o < max; o++) {
            d = disperseBits(o);
            x = d & 0xffff;
            y = d >>> 16;
            if(x >= width && y >= height)
                break;
            if(x >= width)
                continue;
            if(y >= height)
                continue;
            s = d >>> 22;
            t = data[x * ySections + s];
            if(t == 0)
                continue;
            if((ct += (t & (1L << (y & 63))) == 0 ? 0 : 1) == index)
                return Coord.get(x, y);
        }
        return Coord.get(-1, -1);
    }
    /**
     * Like {@link #nth(int)}, this finds a given index along a path through the GreasedRegion, but unlike nth(), this
     * traverses the path in a zig-zag pattern called the Z-Order Curve, and unlike {@link #nthZCurve(int)}, this does
     * not return a Coord and instead produces a "tight"-encoded int. This method is often not very fast compared to
     * nth(), but this different path can help if iteration needs to seem less regular while still covering all "on"
     * cells in the GreasedRegion eventually, and the tight encoding may be handy if you need to use ints.
     * @param index the distance along the Z-order curve to travel, only counting "on" cells in this GreasedRegion.
     * @return the "tight" encoded point at the given distance, or -1 if index is too high or low
     */

    public int nthZCurveTight(final int index)
    {
        int ct = -1, x, y, s, d, max = nextPowerOfTwo(width) * nextPowerOfTwo(height);
        long t, w;
        for (int o = 0; o < max; o++) {
            d = disperseBits(o);
            x = d & 0xffff;
            y = d >>> 16;
            if(x >= width && y >= height)
                break;
            if(x >= width)
                continue;
            if(y >= height)
                continue;
            s = d >>> 22;
            t = data[x * ySections + s];
            if(t == 0)
                continue;
            if((ct += (t & (1L << (y & 63))) == 0 ? 0 : 1) == index)
                return y * width + x;
        }
        return -1;
    }

    /**
     * Gets a Coord array from the "on" contents of this GreasedRegion, using a quasi-random scattering of chosen cells
     * with a count that matches the given {@code fraction} of the total amount of "on" cells in this. This is quasi-
     * random instead of pseudo-random because it is somewhat less likely to produce nearby cells in the result. If you
     * request too many cells (too high of a value for fraction), it will start to overlap, however. Contrast with
     * {@link #mixedRandomSeparated(double, int)}, which tends to overlap more frequently. This method seems to work
     * well because it chooses quasi-random points by their index in the Z-Order Curve as opposed to the simpler
     * approach mixedRandomSeparated uses to traverse points (which just runs through the "on" cells a column at a time,
     * not caring if two points are in adjacent cells as long as they are in different columns). Testing with a dungeon
     * layout of mostly-on cells, this method has no overlap with a fraction of 0.4, while mixedRandomSeparated has
     * overlap as early as 0.15 for fraction, and it only gets worse at higher values. A change to the algorithm used by
     * {@link #quasiRandomSeparated(double)} has it overlapping at the same rate as this method, though it should be
     * much faster. This method can be especially slow, since each Z-Order traversal may need to try many cells that are
     * outside the GreasedRegion but are on the Z-Order Curve.
     * Does not restrict the size of the returned array other than only using up to {@code fraction * size()} cells.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @return a freshly-allocated Coord array containing the quasi-random cells
     */
    public Coord[] separatedZCurve(double fraction)
    {
        return separatedZCurve(fraction, -1);
    }

    /**
     * Gets a Coord array from the "on" contents of this GreasedRegion, using a quasi-random scattering of chosen cells
     * with a count that matches the given {@code fraction} of the total amount of "on" cells in this. This is quasi-
     * random instead of pseudo-random because it is somewhat less likely to produce nearby cells in the result. If you
     * request too many cells (too high of a value for fraction), it will start to overlap, however. Contrast with
     * {@link #mixedRandomSeparated(double, int)}, which tends to overlap more frequently. This method seems to work
     * well because it chooses quasi-random points by their index in the Z-Order Curve as opposed to the simpler
     * approach mixedRandomSeparated uses to traverse points (which just runs through the "on" cells a column at a time,
     * not caring if two points are in adjacent cells as long as they are in different columns). Testing with a dungeon
     * layout of mostly-on cells, this method has no overlap with a fraction of 0.4, while mixedRandomSeparated has
     * overlap as early as 0.15 for fraction, and it only gets worse at higher values. A change to the algorithm used by
     * {@link #quasiRandomSeparated(double, int)} has it overlapping at the same rate as this method, though it should
     * be much faster. This method can be especially slow, since each Z-Order traversal may need to try many cells that
     * are outside the GreasedRegion but are on the Z-Order Curve.
     * Restricts the total size of the returned array to a maximum of {@code limit} (minimum is 0 if no cells are "on").
     * If limit is negative, this will not restrict the size.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @param limit the maximum size of the array to return
     * @return a freshly-allocated Coord array containing the quasi-random cells
     */
    public Coord[] separatedZCurve(double fraction, int limit)
    {
        if(fraction < 0)
            return new Coord[0];
        if(fraction > 1)
            fraction = 1;
        int ct = size(), total;
        total = ct;
        ct *= fraction;
        if(limit >= 0 && limit < ct)
            ct = limit;
        Coord[] vl = new Coord[ct];
        for (int i = 0; i < ct; i++)
        {
            vl[i] = nthZCurve((int) (VanDerCorputQRNG.determine2(i ^ i >>> 1) * total));
        }
        return vl;
    }
    /**
     * Modifies this GreasedRegion so it contains a quasi-random subset of its previous contents, choosing cells so that
     * the {@link #size()} matches the given {@code fraction} of the total amount of "on" cells in this. This is quasi-
     * random instead of pseudo-random because it is somewhat less likely to produce nearby cells in the result. If you
     * request too many cells (too high of a value for fraction), it will start to overlap, however. Contrast with
     * {@link #mixedRandomRegion(double)}, which tends to overlap more frequently. This method seems to work
     * well because it chooses quasi-random points by their index in the Z-Order Curve as opposed to the simpler
     * approach mixedRandomRegion uses to traverse points (which just runs through the "on" cells a column at a time,
     * not caring if two points are in adjacent cells as long as they are in different columns). Testing with a dungeon
     * layout of mostly-on cells, this method has no overlap with a fraction of 0.4, while mixedRandomRegion has
     * overlap as early as 0.15 for fraction, and it only gets worse at higher values. A change to the algorithm used by
     * {@link #quasiRandomRegion(double)} has it overlapping at the same rate as this method, though it should be much
     * faster. This method can be especially slow, since each Z-Order traversal may need to try many cells that are
     * outside the GreasedRegion but are on the Z-Order Curve.
     * Does not restrict the size of the returned array other than only using up to {@code fraction * size()} cells.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @return this, after modifications, for chaining
     */
    public GreasedRegion separatedRegionZCurve(double fraction)
    {
        return separatedRegionZCurve(fraction, -1);
    }

    /**
     * Modifies this GreasedRegion so it contains a quasi-random subset of its previous contents, choosing cells so that
     * the {@link #size()} matches the given {@code fraction} of the total amount of "on" cells in this. This is quasi-
     * random instead of pseudo-random because it is somewhat less likely to produce nearby cells in the result. If you
     * request too many cells (too high of a value for fraction), it will start to overlap, however. Contrast with
     * {@link #mixedRandomRegion(double, int)}, which tends to overlap more frequently. This method seems to work
     * well because it chooses quasi-random points by their index in the Z-Order Curve as opposed to the simpler
     * approach mixedRandomRegion uses to traverse points (which just runs through the "on" cells a column at a time,
     * not caring if two points are in adjacent cells as long as they are in different columns). Testing with a dungeon
     * layout of mostly-on cells, this method has no overlap with a fraction of 0.4, while mixedRandomRegion has
     * overlap as early as 0.15 for fraction, and it only gets worse at higher values. A change to the algorithm used by
     * {@link #quasiRandomRegion(double, int)} has it overlapping at the same rate as this method, though it should be
     * much faster. This method can be especially slow, since each Z-Order traversal may need to try many cells that
     * are outside the GreasedRegion but are on the Z-Order Curve.
     * Restricts the total size of the returned array to a maximum of {@code limit} (minimum is 0 if no cells are "on").
     * If limit is negative, this will not restrict the size.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @param limit the maximum size of the array to return
     * @return this, after modifications, for chaining
     */
    public GreasedRegion separatedRegionZCurve(double fraction, int limit)
    {
        if(fraction <= 0)
            return empty();
        if(fraction >= 1)
            return this;
        int ct = size(), total;
        if(limit >= ct)
            return this;
        total = ct;
        ct *= fraction;
        if(limit >= 0 && limit < ct)
            ct = limit;
        int[] vl = new int[ct];
        for (int i = 0; i < ct; i++)
        {
            vl[i] = nthZCurveTight((int) (VanDerCorputQRNG.determine2(i ^ i >>> 1) * total));
        }
        return empty().insertSeveral(vl);
    }


    public Coord[] randomPortion(IRNG rng, int size)
    {
        int ct = 0, idx = 0, run = 0;
        for (int i = 0; i < width * ySections; i++) {
            ct += Long.bitCount(data[i]);
        }
        if(ct <= 0 || size <= 0)
            return new Coord[0];
        if(ct <= size)
            return asCoords();
        Coord[] points = new Coord[size];
        int[] order = rng.randomOrdering(ct);
        Arrays.sort(order, 0, size);
        long t, w;
        ALL:
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                if((t = data[x * ySections + s]) != 0)
                {
                    w = NumberTools.lowestOneBit(t);
                    while (w != 0) {
                        if (run++ == order[idx]) {
                            points[idx++] = Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                            if (idx >= size) break ALL;
                        }
                        t ^= w;
                        w = NumberTools.lowestOneBit(t);
                    }
                }
            }
        }
        return points;
    }

    public GreasedRegion randomRegion(IRNG rng, int size)
    {
        int ct = 0, idx = 0, run = 0;
        for (int i = 0; i < width * ySections; i++) {
            ct += Long.bitCount(data[i]);
        }
        if(ct <= 0 || size <= 0)
            return empty();
        if(ct <= size)
            return this;
        int[] order = rng.randomOrdering(ct);
        Arrays.sort(order, 0, size);
        long t, w;
        ALL:
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                if((t = data[x * ySections + s]) != 0)
                {
                    w = NumberTools.lowestOneBit(t);
                    while (w != 0) {
                        if (run++ == order[idx]) {
                            if(++idx >= size) break ALL;
                        }
                        else
                        {
                            data[x * ySections + s] &= ~(1L << Long.numberOfTrailingZeros(w));
                        }
                        t ^= w;
                        w = NumberTools.lowestOneBit(t);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public boolean contains(int x, int y)
    {
        return x >= 0 && y >= 0 && x < width && y < height && ySections > 0 &&
                ((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0);
    }


    /**
     * @return Whether this zone is empty.
     */
    @Override
    public boolean isEmpty() {
        for (int i = 0; i < data.length; i++) {
            if(data[i] != 0L) return false;
        }
        return true;
    }

    /**
     * Generates a 2D int array from an array or vararg of GreasedRegions, starting at all 0 and adding 1 to the int at
     * a position once for every GreasedRegion that has that cell as "on." This means if you give 8 GreasedRegions to
     * this method, it can produce any number between 0 and 8 in a cell; if you give 16 GreasedRegions, then it can
     * produce any number between 0 and 16 in a cell.
     * @param regions an array or vararg of GreasedRegions; must all have the same width and height
     * @return a 2D int array with the same width and height as the regions, where an int cell equals the number of given GreasedRegions that had an "on" cell at that position
     */
    public static int[][] sum(GreasedRegion... regions)
    {
        if(regions == null || regions.length <= 0)
            return new int[0][0];
        int w = regions[0].width, h = regions[0].height, l = regions.length, ys = regions[0].ySections;
        int[][] numbers = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] += (regions[i].data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 : 0;
                }
            }
        }
        return numbers;
    }

    /**
     * Generates a 2D int array from a List of GreasedRegions, starting at all 0 and adding 1 to the int at
     * a position once for every GreasedRegion that has that cell as "on." This means if you give 8 GreasedRegions to
     * this method, it can produce any number between 0 and 8 in a cell; if you give 16 GreasedRegions, then it can
     * produce any number between 0 and 16 in a cell.
     * @param regions a List of GreasedRegions; must all have the same width and height
     * @return a 2D int array with the same width and height as the regions, where an int cell equals the number of given GreasedRegions that had an "on" cell at that position
     */
    public static int[][] sum(List<GreasedRegion> regions)
    {
        if(regions == null || regions.isEmpty())
            return new int[0][0];
        GreasedRegion t = regions.get(0);
        int w = t.width, h = t.height, l = regions.size(), ys = t.ySections;
        int[][] numbers = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] += (regions.get(i).data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 : 0;
                }
            }
        }
        return numbers;
    }

    /**
     * Generates a 2D double array from an array or vararg of GreasedRegions, starting at all 0 and adding 1 to the double at
     * a position once for every GreasedRegion that has that cell as "on." This means if you give 8 GreasedRegions to
     * this method, it can produce any number between 0 and 8 in a cell; if you give 16 GreasedRegions, then it can
     * produce any number between 0 and 16 in a cell.
     * @param regions an array or vararg of GreasedRegions; must all have the same width and height
     * @return a 2D double array with the same width and height as the regions, where an double cell equals the number of given GreasedRegions that had an "on" cell at that position
     */
    public static double[][] sumDouble(GreasedRegion... regions)
    {
        if(regions == null || regions.length <= 0)
            return new double[0][0];
        int w = regions[0].width, h = regions[0].height, l = regions.length, ys = regions[0].ySections;
        double[][] numbers = new double[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] += (regions[i].data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1.0 : 0.0;
                }
            }
        }
        return numbers;
    }

    /**
     * Generates a 2D double array from a List of GreasedRegions, starting at all 0 and adding 1 to the double at
     * a position once for every GreasedRegion that has that cell as "on." This means if you give 8 GreasedRegions to
     * this method, it can produce any number between 0 and 8 in a cell; if you give 16 GreasedRegions, then it can
     * produce any number between 0 and 16 in a cell.
     * @param regions a List of GreasedRegions; must all have the same width and height
     * @return a 2D double array with the same width and height as the regions, where an double cell equals the number of given GreasedRegions that had an "on" cell at that position
     */
    public static double[][] sumDouble(List<GreasedRegion> regions)
    {
        if(regions == null || regions.isEmpty())
            return new double[0][0];
        GreasedRegion t = regions.get(0);
        int w = t.width, h = t.height, l = regions.size(), ys = t.ySections;
        double[][] numbers = new double[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] += (regions.get(i).data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1.0 : 0.0;
                }
            }
        }
        return numbers;
    }

    /**
     * Generates a 2D int array from an array of GreasedRegions and an array of weights, starting the 2D result at all 0
     * and, for every GreasedRegion that has that cell as "on," adding the int in the corresponding weights array at
     * the position of that cell. This means if you give an array of 4 GreasedRegions to this method along with the
     * weights {@code 1, 2, 3, 4}, it can produce a number between 0 and 10 in a cell (where 10 is used when all 4
     * GreasedRegions have a cell "on," since {@code 1 + 2 + 3 + 4 == 10}); if the weights are instead
     * {@code 1, 10, 100, 1000}, then the results can vary between 0 and 1111, where 1111 is only if all GreasedRegions
     * have a cell as "on." The weights array must have a length at least equal to the length of the regions array.
     * @param regions an array of GreasedRegions; must all have the same width and height
     * @param weights an array of ints; must have length at least equal to regions' length
     * @return a 2D int array with the same width and height as the regions, where an int cell equals the sum of the weights corresponding to GreasedRegions that had an "on" cell at that position
     */
    public static int[][] sumWeighted(GreasedRegion[] regions, int[] weights)
    {
        if(regions == null || regions.length <= 0 || weights == null || weights.length < regions.length)
            return new int[0][0];
        int w = regions[0].width, h = regions[0].height, l = regions.length, ys = regions[0].ySections;
        int[][] numbers = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] += (regions[i].data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? weights[i] : 0;
                }
            }
        }
        return numbers;
    }

    /**
     * Generates a 2D double array from an array of GreasedRegions and an array of weights, starting the 2D result at
     * all 0 and, for every GreasedRegion that has that cell as "on," adding the double in the corresponding weights
     * array at the position of that cell. This means if you give an array of 4 GreasedRegions to this method along with
     * the weights {@code 1, 2, 3, 4}, it can produce a number between 0 and 10 in a cell (where 10 is used when all 4
     * GreasedRegions have a cell "on," since {@code 1 + 2 + 3 + 4 == 10}); if the weights are instead
     * {@code 1, 10, 100, 1000}, then the results can vary between 0 and 1111, where 1111 is only if all GreasedRegions
     * have a cell as "on." The weights array must have a length at least equal to the length of the regions array.
     * @param regions an array of GreasedRegions; must all have the same width and height
     * @param weights an array of doubles; must have length at least equal to regions' length
     * @return a 2D double array with the same width and height as the regions, where an double cell equals the sum of the weights corresponding to GreasedRegions that had an "on" cell at that position
     */
    public static double[][] sumWeightedDouble(GreasedRegion[] regions, double[] weights)
    {
        if(regions == null || regions.length <= 0 || weights == null || weights.length < regions.length)
            return new double[0][0];
        int w = regions[0].width, h = regions[0].height, l = regions.length, ys = regions[0].ySections;
        double[][] numbers = new double[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] += (regions[i].data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? weights[i] : 0.0;
                }
            }
        }
        return numbers;
    }

    /**
     * Adds to an existing 2D int array with an array or vararg of GreasedRegions, adding 1 to the int in existing at
     * a position once for every GreasedRegion that has that cell as "on." This means if you give 8 GreasedRegions to
     * this method, it can increment by any number between 0 and 8 in a cell; if you give 16 GreasedRegions, then it can
     * increase the value in existing by any number between 0 and 16 in a cell.
     * @param existing a non-null 2D int array that will have each cell incremented by the sum of the GreasedRegions
     * @param regions an array or vararg of GreasedRegions; must all have the same width and height
     * @return existing, after modification, where an int cell will be changed by the number of given GreasedRegions that had an "on" cell at that position
     */
    public static int[][] sumInto(int[][] existing, GreasedRegion... regions)
    {
        if(regions == null || regions.length <= 0 || existing == null || existing.length == 0 || existing[0].length == 0)
            return existing;
        int w = existing.length, h = existing[0].length, l = regions.length, ys;
        for (int i = 0; i < l; i++) {
            GreasedRegion region = regions[i];
            ys = region.ySections;
            for (int x = 0; x < w && x < region.width; x++) {
                for (int y = 0; y < h && y < region.height; y++) {
                    existing[x][y] += (region.data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 : 0;
                }
            }
        }
        return existing;
    }


    /**
     * Adds to an existing 2D double array with an array or vararg of GreasedRegions, adding 1 to the double in existing
     * at a position once for every GreasedRegion that has that cell as "on." This means if you give 8 GreasedRegions to
     * this method, it can increment by any number between 0 and 8 in a cell; if you give 16 GreasedRegions, then it can
     * increase the value in existing by any number between 0 and 16 in a cell.
     * @param existing a non-null 2D double array that will have each cell incremented by the sum of the GreasedRegions
     * @param regions an array or vararg of GreasedRegions; must all have the same width and height
     * @return existing, after modification, where a double cell will be changed by the number of given GreasedRegions that had an "on" cell at that position
     */
    public static double[][] sumIntoDouble(double[][] existing, GreasedRegion... regions)
    {
        if(regions == null || regions.length <= 0 || existing == null || existing.length == 0 || existing[0].length == 0)
            return existing;
        int w = existing.length, h = existing[0].length, l = regions.length, ys = regions[0].ySections;
        for (int i = 0; i < l; i++) {
            for (int x = 0; x < w && x < regions[i].width; x++) {
                for (int y = 0; y < h && y < regions[i].height; y++) {
                    existing[x][y] += (regions[i].data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1.0 : 0.0;
                }
            }
        }
        return existing;
    }

    /**
     * Discouraged from active use; slower than {@link squidpony.squidai.DijkstraMap} and has less features.
     * @param map a 2D char array where '#' is a wall
     * @param goals an array or vararg of Coord to get the distances toward
     * @return a 2D double array of distances from a cell to the nearest goal
     */
    public static double[][] dijkstraScan(char[][] map, Coord... goals)
    {
        if(map == null || map.length <= 0 || map[0].length <= 0 || goals == null || goals.length <= 0)
            return new double[0][0];
        int w = map.length, h = map[0].length, ys = (h + 63) >>> 6;
        double[][] numbers = new double[w][h];
        GreasedRegion walls = new GreasedRegion(map, '#'), floors = new GreasedRegion(walls).not(),
                middle = new GreasedRegion(w, h, goals).and(floors);
        ArrayList<GreasedRegion> regions = middle.floodSeriesToLimit(floors);
        int l = regions.size();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] += (regions.get(i).data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 : 0;
                }
            }
        }
        return numbers;
    }
    /**
     * Discouraged from active use; slower than {@link squidpony.squidai.DijkstraMap} and has less features.
     * @param map a 2D char array where '#' is a wall
     * @param goals an array or vararg of Coord to get the distances toward
     * @return a 2D double array of distances from a cell to the nearest goal
     */
    public static double[][] dijkstraScan8way(char[][] map, Coord... goals)
    {
        if(map == null || map.length <= 0 || map[0].length <= 0 || goals == null || goals.length <= 0)
            return new double[0][0];
        int w = map.length, h = map[0].length, ys = (h + 63) >>> 6;
        double[][] numbers = new double[w][h];
        GreasedRegion walls = new GreasedRegion(map, '#'), floors = new GreasedRegion(walls).not(),
                middle = new GreasedRegion(w, h, goals).and(floors);
        ArrayList<GreasedRegion> regions = middle.floodSeriesToLimit8way(floors);
        int l = regions.size();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] += (regions.get(i).data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 : 0;
                }
            }
        }
        return numbers;
    }

    /**
     * Generates a 2D int array from an array or vararg of GreasedRegions, treating each cell in the nth region as the
     * nth bit of the int at the corresponding x,y cell in the int array. This means if you give 8 GreasedRegions to
     * this method, it can produce any 8-bit number in a cell (0-255); if you give 16 GreasedRegions, then it can
     * produce any 16-bit number (0-65535).
     * @param regions an array or vararg of GreasedRegions; must all have the same width and height
     * @return a 2D int array with the same width and height as the regions, with bits per int taken from the regions
     */
    public static int[][] bitSum(GreasedRegion... regions)
    {
        if(regions == null || regions.length <= 0)
            return new int[0][0];
        int w = regions[0].width, h = regions[0].height, l = Math.min(32, regions.length), ys = regions[0].ySections;
        int[][] numbers = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] |= (regions[i].data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 << i : 0;
                }
            }
        }
        return numbers;
    }

    /*
    public static int[][] selectiveNegate(int[][] numbers, GreasedRegion region, int mask)
    {
        if(region == null)
            return numbers;
        int w = region.width, h = region.height, ys = region.ySections;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if((region.data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0) numbers[x][y] = (~numbers[x][y] & mask);
            }
        }
        return numbers;
    }
    */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GreasedRegion that = (GreasedRegion) o;

        if (height != that.height) return false;
        if (width != that.width) return false;
        if (ySections != that.ySections) return false;
        if (yEndMask != that.yEndMask) return false;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        /*
        int result = CrossHash.Lightning.hash(data);
        result = 31 * result + height;
        result = 31 * result + width;
        result = 31 * result + ySections; //not needed; purely dependent on height
        result = 31 * result + (int) (yEndMask ^ (yEndMask >>> 32)); //not needed; purely dependent on height
        return result;
        */
        /*
        long z = 0x632BE59BD9B4E019L, result = 1L;
        for (int i = 0; i < data.length; i++) {
            result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        }
        result ^= (z += (height + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        result ^= (z += (width + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
        return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
         */
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        result += (a ^= 0x8329C6EB9E6AD3E3L * height);
        result += (a ^= 0x8329C6EB9E6AD3E3L * width);
        return (int)((result = (result * (a | 1L) ^ (result >>> 27 | result << 37))) ^ (result >>> 32));
    }

    public String serializeToString()
    {
        return width +
                "," + height +
                "," + StringKit.joinAlt(",",data);
    }
    public static GreasedRegion deserializeFromString(String s)
    {
        if(s == null || s.isEmpty())
            return null;
        int gap = s.indexOf(','), w = Integer.parseInt(s.substring(0, gap)),
                gap2 = s.indexOf(',', gap+1), h = Integer.parseInt(s.substring(gap+1, gap2));
        String[] splits = StringKit.split(s.substring(gap2+1), ",");
        long[] data = new long[splits.length];
        for (int i = 0; i < splits.length; i++) {
            data[i] = StringKit.longFromDec(splits[i]);
        }
        return new GreasedRegion(data, w, h);
    }

    /**
     * Constructs a GreasedRegion using a vararg for data. Primarily meant for generated code, since
     * {@link #serializeToString()} produces a String that happens to be a valid parameter list for this method.
     * @param width width of the GreasedRegion to produce
     * @param height height of the GreasedRegion to produce
     * @param data array or vararg of long containing the exact data, probably from an existing GreasedRegion
     * @return a new GreasedRegion with the given width, height, and data
     */
    public static GreasedRegion of(final int width, final int height, final long... data)
    {
        return new GreasedRegion(data, width, height);
    }

    public String toCompressedString()
    {
        if(height > 0x4000)
            throw new UnsupportedOperationException("Height is too large to compress, aborting.");
        StringBuilder sb = new StringBuilder(width * height >> 2);
        boolean on = false;
        int span = 0;
        char adjust = (char) nextPowerOfTwo(Math.max(height, 32));
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(((data[x * ySections + (y >> 6)] & (1L << (y & 63))) == 0) == on)
                    span++;
                else
                {
                    on = !on;
                    sb.append((char)(adjust | span));
                    span = 1;
                }
            }
            if(on)
            {
                sb.append((char)(adjust | span));
            }
            sb.append('\n');
            on = false;
            span = 0;
        }
        return sb.toString();
    }

    @Override
    public boolean contains(Object o) {
        if(o instanceof Coord)
            return contains((Coord)o);
        return false;
    }

    @Override
    public Iterator<Coord> iterator() {
        return new GRIterator();
    }

    @Override
    public Object[] toArray() {
        return asCoords();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        if(a instanceof Coord[])
            return (T[])asCoords((Coord[])a);
        return a;
    }

    @Override
    public boolean add(Coord coord) {
        if(contains(coord))
            return false;
        insert(coord);
        return true;
    }
    @Override
    public void clear()
    {
        Arrays.fill(data, 0L);
    }

    @Override
    public boolean remove(Object o) {
        if(o instanceof Coord)
        {
            if(contains((Coord)o))
            {
                remove((Coord)o);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object o : c)
        {
            if(!contains(o))
                return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Coord> c) {
        boolean changed = false;
        for(Coord co : c)
        {
            changed |= add(co);
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for(Object o : c)
        {
            changed |= remove(o);
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        GreasedRegion g2 = new GreasedRegion(width, height);
        for(Object o : c)
        {
            if(contains(o) && o instanceof Coord)
            {
                g2.add((Coord)o);
            }
        }
        boolean changed = equals(g2);
        remake(g2);
        return changed;
    }

    /**
     * Randomly removes points from a GreasedRegion, with larger values for preservation keeping more of the existing
     * shape intact. If preservation is 1, roughly 1/2 of all points will be removed; if 2, roughly 1/4, if 3, roughly
     * 1/8, and so on, so that preservation can be thought of as a negative exponent of 2.
     * @param rng used to determine random factors
     * @param preservation roughly what degree of points to remove (higher keeps more); removes about {@code 1/(2^preservation)} points
     * @return a randomly modified change to this GreasedRegion
     */
    public GreasedRegion deteriorate(RandomnessSource rng, int preservation) {
        if(rng == null || width <= 2 || ySections <= 0 || preservation <= 0)
            return this;
        long mash;
        for (int i = 0; i < width * ySections; i++) {
            mash = rng.nextLong();
            for (int j = i; j < preservation; j++) {
                mash |= rng.nextLong();
            }
            data[i] &= mash;
        }
        return this;
    }

    /**
     * Randomly removes points from a GreasedRegion, with preservation as a fraction between 1.0 (keep all) and 0.0
     * (remove all). If preservation is 0.5, roughly 1/2 of all points will be removed; if 0.25, roughly 3/4 will be
     * removed (roughly 0.25 will be _kept_), if 0.8, roughly 1/5 will be removed (and about 0.8 will be kept), and so
     * on. Preservation must be between 0.0 and 1.0 for this to have the intended behavior; 1.0 or higher will keep all
     * points without change (returning this GreasedRegion), while anything less than 0.015625 (1.0/64) will empty this
     * GreasedRegion (using {@link #empty()}) and then return it. The parameter {@code random} can be an object like a
     * {@link LightRNG}, an {@link RNG} backed by a well-distributed RandomnessSource like its default, LightRNG, a
     * {@link GWTRNG} (especially if you target GWT, where it will perform much better than most alternatives), or any
     * of various other RandomnessSource implementations that distribute bits well for
     * {@link RandomnessSource#nextLong()}, but should not be intentionally-biased RNGs like {@link DharmaRNG} or 
     * {@link EditRNG}, nor double-based QRNGs like {@link VanDerCorputQRNG} or {@link SobolQRNG}.
     * @param random used to determine random factors; likely to be an {@link RNG}, {@link LightRNG}, or {@link GWTRNG}
     * @param preservation the rough fraction of points to keep, between 0.0 and 1.0
     * @return a randomly modified change to this GreasedRegion
     */
    public GreasedRegion deteriorate(final RandomnessSource random, final double preservation) {
        if(random == null || width <= 2 || ySections <= 0 || preservation >= 1)
            return this;
        if(preservation <= 0)
            return empty();
        int bitCount = (int) (preservation * 64);
        for (int i = 0; i < width * ySections; i++) {
            data[i] &= approximateBits(random, bitCount);
        }
        return this;
    }

    /**
     * Inverts the on/off state of the cell with the given x and y.
     * @param x the x position of the cell to flip
     * @param y the y position of the cell to flip
     * @return this for chaining, modified
     */
    public GreasedRegion flip(int x, int y) {
        if(x >= 0 && y >= 0 && x < width && y < height && ySections > 0)
            data[x * ySections + (y >> 6)] ^= (1L << (y & 63));
        return this;

    }

    /**
     * Returns a new GreasedRegion that has been mirrored along the rightmost edge, parallel to the y-axis. The new
     * GreasedRegion will have exactly twice the width, the additional width will have the contents of the original
     * GreasesRegion in reversed order. The positions shared by both GreasedRegions will be the same, that is, any area
     * not added to the original will be equal to the original.
     * @return a new GreasedRegion with twice the width of {@code this}, that is mirrored along the rightmost edge
     */
    public GreasedRegion mirrorY()
    {
        GreasedRegion next = new GreasedRegion(data, width, height, width * 2, height);
        for (int i = 0, o = width * 2 - 1; i < width; i++, o--) {
            System.arraycopy(data, ySections * i, next.data, ySections * o, ySections);
        }
        return next;
    }

    @Override
    public boolean intersectsWith(Zone other) {
        if (other instanceof GreasedRegion)
            return intersects((GreasedRegion) other);
        long t, w;
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if ((t = data[x * ySections + s]) != 0) {
                    w = NumberTools.lowestOneBit(t);
                    while (w != 0) {
                        if (other.contains(x, (s << 6) | Long.numberOfTrailingZeros(w)))
                            return true;
                        t ^= w;
                        w = NumberTools.lowestOneBit(t);
                    }
                }
            }
        }
        return false;
    }
    /**
     * Translates a copy of {@code this} by the x,y values in {@code c}.
     * Implemented with {@code return copy().translate(c.x, c.y);}
     * @return {@code this} copied and shifted by {@code (c.x,c.y)}
     */
    @Override
    public Zone translate(Coord c) {
        return copy().translate(c.x, c.y);
    }
    /**
     * Gets a Collection of Coord values that are not in this GreasedRegion, but are
     * adjacent to it, either orthogonally or diagonally. Related to the fringe()
     * methods in CoordPacker and GreasedRegion, but guaranteed to use 8-way
     * adjacency and to return a new Collection of Coord. This implementation returns
     * a GreasedRegion produced simply by {@code return copy().fringe8way();} .
     * @return Cells adjacent to {@code this} (orthogonally or diagonally) that
     * aren't in {@code this}
     */
    @Override
    public GreasedRegion getExternalBorder() {
        return copy().fringe8way();
    }

    /**
     * Gets a new Zone that contains all the Coords in {@code this} plus all
     * neighboring Coords, which can be orthogonally or diagonally adjacent
     * to any Coord this has in it. Related to the expand() methods in
     * CoordPacker and GreasedRegion, but guaranteed to use 8-way adjacency
     * and to return a new Zone. This implementation returns a GreasedRegion
     * produced simply by {@code return copy().expand8way();} .
     * @return A new GreasedRegion where "off" cells adjacent to {@code this}
     *         (orthogonally or diagonally) have been added to the "on" cells
     *         in {@code this}
     */
    @Override
    public GreasedRegion extend() {
        return copy().expand8way();
    }

    /**
     * Checks if {@code c} is present in this GreasedRegion. Returns true if and only if c is present in this
     * GreasedRegion as an "on" cell. This will never be true if c is null, has negative x or y, has a value for x that
     * is equal to or greater than {@link #width}, or has a value for y that is equal to or greater than
     * {@link #height}, but none of those conditions will cause Exceptions to be thrown.
     * @param c a Coord to try to find in this GreasedRegion; if null this will always return false
     * @return true if {@code c} is an "on" cell in this GreasedRegion, or false otherwise, including if c is null
     */
    @Override
    public boolean contains(Coord c) {
        return c != null && contains(c.x, c.y);
    }

    /**
     * Checks whether all Coords in {@code other} are also present in {@code this}.
     * Requires that {@code other} won't give a null Coord while this method iterates over it.
     * @param other another Zone, such as a GreasedRegion or a {@link squidpony.squidgrid.zone.CoordPackerZone}
     * @return true if all Coords in other are "on" in this GreasedRegion, or false otherwise
     */
    @Override
    public boolean contains(Zone other) {
        if(other instanceof Collection)
            return containsAll((Collection) other);
        for(Coord c : other)
        {
            if(!contains(c.x, c.y))
                return false;
        }
        return true;
    }

    /**
     * @param smallestOrBiggest if true, finds the smallest x-coordinate value;
     *                          if false, finds the biggest.
     * @return The x-coordinate of the Coord within {@code this} that has the
     * smallest (or biggest) x-coordinate. Or -1 if the zone is empty.
     */
    @Override
    public int x(boolean smallestOrBiggest) {
        if(smallestOrBiggest)
            return first().x;
        else
            return nth(size()-1).x;
    }

    /**
     * @param smallestOrBiggest if true, finds the smallest y-coordinate value;
     *                          if false, finds the biggest.
     * @return The y-coordinate of the Coord within {@code this} that has the
     * smallest (or biggest) y-coordinate. Or -1 if the zone is empty.
     */
    @Override
    public int y(boolean smallestOrBiggest) {
        long t, w;
        if(smallestOrBiggest) {
            int best = Integer.MAX_VALUE;
            for (int x = 0; x < width; x++) {
                for (int s = 0; s < ySections; s++) {
                    if ((t = data[x * ySections + s]) != 0) {
                        w = NumberTools.lowestOneBit(t);
                        while (w != 0) {
                            best = Math.min(s << 6 | Long.numberOfTrailingZeros(w), best);
                            if(best == 0)
                                return 0;
                            t ^= w;
                            w = NumberTools.lowestOneBit(t);
                        }
                    }
                }
            }
            return best == Integer.MAX_VALUE ? -1 : best;
        }
        else
        {
            int best = -1;
            for (int x = 0; x < width; x++) {
                for (int s = 0; s < ySections; s++) {
                    if ((t = data[x * ySections + s]) != 0) {
                        w = NumberTools.lowestOneBit(t);
                        while (w != 0) {
                            best = Math.max(s << 6 | Long.numberOfTrailingZeros(w), best);
                            if(best == height - 1)
                                return best;
                            t ^= w;
                            w = NumberTools.lowestOneBit(t);
                        }
                    }
                }
            }
            return best;
        }
    }

    /**
     * Gets the distance between the minimum x-value contained in this GreasedRegion and the maximum x-value in it.
     * Not the same as accessing the field {@link #width} on a GreasedRegion! The field will get the span of the space
     * that the GreasedRegion can use, including "on" and "off" cells. This method will only get the distance between
     * the furthest-separated "on" cells on the x-axis, and won't consider "off" cells. This method can return -1 if the
     * GreasedRegion is empty, 0 if the "on" cells are all in a vertical line (that is, when the minimum x is equal to
     * the maximum x), or a positive int in other cases with multiple x-values.
     * @return the distance on the x-axis between the "on" cell with the lowest x-value and the one with the highest
     */
    @Override
    public int getWidth() {
        if (super.width == -2)
            super.width = isEmpty() ? -1 : x(false) - x(true);
        return super.width;
    }
    /**
     * Gets the distance between the minimum y-value contained in this GreasedRegion and the maximum y-value in it.
     * Not the same as accessing the field {@link #height} on a GreasedRegion! The field will get the span of the space
     * that the GreasedRegion can use, including "on" and "off" cells. This method will only get the distance between
     * the furthest-separated "on" cells on the y-axis, and won't consider "off" cells. This method can return -1 if the
     * GreasedRegion is empty, 0 if the "on" cells are all in a horizontal line (that is, when the minimum y is equal to
     * the maximum y), or a positive int in other cases with multiple y-values.
     * @return the distance on the y-axis between the "on" cell with the lowest y-value and the one with the highest
     */
    @Override
    public int getHeight() {
        if (super.height == -2)
            super.height = isEmpty() ? -1 : y(false) - y(true);
        return super.height;
    }

    /**
     * Gets the diagonal distance from the point combining the lowest x-value present in this GreasedRegion with the
     * lowest y-value in this, to the point combining the highest x-value and the highest y-value. These minimum and
     * maximum values don't necessarily match a single "on" cell for each min and max corner, and can take their x and y
     * values from two different points. The diagonal distance uses Euclidean measurement (basic Pythagorean Theorem
     * math here), and will be a double.
     * @return the diagonal distance from (min x, min y) to (max x, max y), as a double
     */
    @Override
    public double getDiagonal() {
        final int w = getWidth();
        final int h = getHeight();
        return Math.sqrt((w * w) + (h * h));
    }
//////Not duplicated because the superclass does this just fine.
//    @Override
//    public Coord getCenter() {
//        return super.getCenter();
//    }

    @Override
    public GreasedRegion getInternalBorder() {
        return copy().surface8way();
    }

    public class GRIterator implements Iterator<Coord>
    {
        public int index = 0;
        private int[] counts;
        private int limit;
        private long t, w;
        public GRIterator()
        {
            limit = 0;
            counts = new int[width * ySections];
            int tmp;
            for (int i = 0; i < width * ySections; i++) {
                tmp = Long.bitCount(data[i]);
                counts[i] = tmp == 0 ? -1 : (limit += tmp);
            }
        }
        @Override
        public boolean hasNext() {
            return index < limit;
        }

        @Override
        public Coord next() {
            int ct;
            if(index >= limit)
                return null;
            for (int s = 0; s < ySections; s++) {
                for (int x = 0; x < width; x++) {
                    if ((ct = counts[x * ySections + s]) > index) {
                        t = data[x * ySections + s];
                        w = NumberTools.lowestOneBit(t);
                        for (--ct; w != 0; ct--) {
                            if (ct == index)
                            {
                                if(index++ < limit)
                                    return Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                                else
                                    return null;
                            }
                            t ^= w;
                            w = NumberTools.lowestOneBit(t);
                        }
                    }
                }
            }
            return null;

            /*
            for (int x = 0; x < width; x++) {
                for (int s = 0; s < ySections; s++) {
                    if ((w = NumberTools.lowestOneBit(data[x * ySections + s])) != 0 && i++ >= index) {
                        if(index++ < limit)
                            return Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                        else
                            return null;
                    }
                }
            }
            */
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() is not supported on this Iterator.");
        }
    }
}
