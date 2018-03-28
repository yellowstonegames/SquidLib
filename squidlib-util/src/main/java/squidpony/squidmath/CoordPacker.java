package squidpony.squidmath;

import squidpony.GwtCompatibility;
import squidpony.squidai.AimLimit;
import squidpony.squidai.Reach;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.Radius;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


/**
 * Provides static methods to encode Coords as single primitive ints in various ways, hence the namesake, but also
 * provides advanced methods to encode 2D arrays of various sorts produced by SquidLib in extremely memory-efficient
 * representations, and decode those representations to various types of 2D array on-demand. IMPORTANT: you must call
 * {@link #init()} before using this class if you do not already use a SquidLib class that does so. It is a good habit
 * to call init() in your main() method or primary game entry point if you use CoordPacker. Failure to call init() will
 * not result in exceptions, but will make results inaccurate.
 *
 * There's a detailed introduction
 * <a href="https://github.com/SquidPony/SquidLib/wiki/Handling-Map-Regions-with-CoordPacker">on the SquidLib wiki</a>,
 * which is probably the best way to learn the techniques possible with this class. Most methods in this aren't useful
 * on their own, but can be mixed and matched to get specific regions from a map, such as all floors not adjacent to a
 * wall, or all grass within 3 squares of deep or shallow water, with walls blocking the distance measurement. You can
 * also use packed data that this class produces as keys for {@link RegionMap} to associate values with regions.
 * <br>
 * NOTE: Internally, this class is atypically complex and low-level for SquidLib because it is attempting to attain some
 * very challenging performance gains. You should not consider it idiomatic SquidLib code or start modifying it unless
 * you have a good grasp of bitwise operations and the performance implications, particularly in regard to memory
 * consumption, that higher-level and more convenient Java programming techniques have.
 * <br>
 * NOTE 2: This class fills a role that is very similar to {@link GreasedRegion}, and there are times when code will do
 * better using CoordPacker than GreasedRegion and vice versa. GreasedRegion uses objects and tries to change them
 * in-place whenever possible instead of allocating new data; CoordPacker uses short[] values that it never changes
 * in-place and frequently allocates new short[] values to represent data. GreasedRegion stores the full map without any
 * compression beyond "one bit per cell" and always needs to store all cells on the map (sometimes slightly more);
 * CoordPacker can compress some data to significantly less than one bit per cell, and doesn't care about the maximum
 * bounds of the map as long as it fits inside a 256x256 square. GreasedRegion is significantly faster than CoordPacker
 * when performing any operations that move, shrink, or expand an area, such as {@link #expand(short[], int, int, int)},
 * {@link #retract(short[], int, int, int)}, and {@link #translate(short[], int, int, int, int)}. CoordPacker usually
 * (not always) uses somewhat less memory than GreasedRegion, though both use very little unless the maps are very
 * large or very numerous. Both can do essentially the same things, except that GreasedRegion has no equivalent to the
 * {@link #packMulti(byte[][], int)} and other Multi kinds of method, no {@link #radiate(short[], short[], int)}, and no
 * {@link #reachable(short[], short[], Reach)}, while CoordPacker has no equivalent to
 * {@link GreasedRegion#expandSeriesToLimit()} and other ToLimit kinds of method, no
 * {@link GreasedRegion#flip(int, int)}, no {@link GreasedRegion#deteriorate(RNG, int)}, no
 * {@link GreasedRegion#insert(int, int, GreasedRegion)} (though CoordPacker can generally use
 * {@link #unionPacked(short[], short[])} for that), and several other methods don't have equivalents. In general, you
 * will probably find GreasedRegion more intuitive because it involves working with objects instead of a short[] that is
 * treated like a particular kind of data, and the methods are also somewhat more clearly-named. GreasedRegion also
 * implements Collection of Coord, while the short[] data can't implement anything. A way around this for CoordPacker is
 * the {@link squidpony.squidgrid.zone.CoordPackerZone} class; both that and GreasedRegion implement the
 * {@link squidpony.squidgrid.zone.Zone} interface, which ensures a way to get a List of Coord from the Zone.
 * <br>
 * The pack() methods in this class take a 2D array with a clear division between cells in an "on" state and cells in an
 * "off" state, and they produce a very tightly compressed short array that can be losslessly decompressed with the
 * unpack() methods to a boolean 2D array that stores equivalent on/off data to the input. The packMulti() method in
 * this class takes a double 2D array that has more than two states that may need to be encoded, such as an FOV map that
 * stores light level as a value between 0.0 and 1.0 instead of just on or off, and an additional double array that
 * defines what states should be distinguished in the result (for example, if the FOV can store values that differ by
 * 0.1 for a FOV radius of 10, you could pass the array of 10 levels: 0.1, 0.2, 0.3, ... 0.9, 1.0). The value returned
 * by packMulti() is a short[][], but with different array lengths for each sub-array (a jagged array); the length of
 * the short[][] is the same as the length of the levels array, and each sub-array corresponds to a different level of
 * FOV lighting or other gradation as defined in levels. This short[][] can be passed to the unpackMultiByte() method in
 * this class to produce a byte 2D array where the original levels correspond to progressively greater bytes, with 0
 * used for cells that were less than the smallest value in levels, 1 for values that were only greater than the
 * smallest value, and no others, in levels, then 2 for larger values, etc. until it places a byte with a value equal to
 * the length of levels in the cells that are the highest. There is also the unpackMultiDouble() method in this class
 * that takes the same short[][] unpackMultiByte() can take, but also takes a levels double array that should be the
 * same as the one used to compress short[][]. It will return a double 2D array with any cells that were smaller than
 * the smallest value in levels assigned 0.0, and any other cells will be assigned a double that corresponds to the
 * highest value in levels that does not exceed the original double at that location in the unpacked data. To make this
 * more clear, if you have 4 levels: [0.25, 0.5, 0.75, 1.0] and you packMulti() on an FOV with a large radius and
 * sample values 0.1, 0.45, 0.8, 1.0, you will get a packed short[][] with 4 sub-arrays to match the 4 levels. If you
 * then pass the short[][] and levels to unpackMultiDouble later, much of the same radius will be filled, but because
 * the sample value 0.1 was less than the smallest value in levels, its cell will be given 0.0. What was originally 0.45
 * will be given the next-lower levels value, 0.25; 0.8 will be given 0.75, and 1.0 will remain 1.0.
 * <br>
 * This compression is meant to produce a short[] or short[][] that uses as little memory as possible for the specific
 * case of compressing maps with these qualities:
 * <ul>
 *     <li>Maps are not especially large for a grid-based game; the maximum size is 256x256 cells.</li>
 *     <li>The vast majority of that 256x256 space is either unused or filled with cells no greater than 0.</li>
 *     <li>The cells that are greater than 0 are mostly near each other, though separate areas are possible.</li>
 * </ul>
 * These properties are all shared by typical roguelike FOV maps, and the specificity of these circumstances mean
 * extraordinarily dense compression can be achieved using the right combination of algorithms. In early testing,
 * using dungeon maps generated by {@link squidpony.squidgrid.mapping.DungeonGenerator} that should be typical of
 * roguelike maps and a diamond-shaped FOV with radius 8, compression of the short[] returned by pack() vs.
 * the original double[][] (which wastefully represents 2 states with 8 bytes) yields average memory usage ratios
 * between (with relatively optimal parameters) 0.0001237905030818498 in one of the best cases, and (with some very
 * poor parameters for the dungeon, but still using a realistic FOV map) 0.003135985198889917 in one of the worst.
 * <br>
 * This table shows the results for the average of 100 runs of pack() in a map with a "good size" and 100 runs in a map
 * with a "bad size." Both the compression ratio vs. a double[][] that stores only whether a cell is on or off and a
 * boolean[][] that stores the same information are provided.
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 *     <caption>Memory Performance of CoordPacker</caption>
 *     <tr>
 *         <th></th>
 *         <th>Bytes of RAM used, double 2D array</th>
 *         <th>Bytes of RAM used, boolean 2D array</th>
 *         <th>Average Bytes of RAM used, short 1D array (packed)</th>
 *         <th>Compression ratio, packed vs. doubles</th>
 *         <th>Compression ratio, packed vs. booleans</th>
 *     </tr>
 *     <tr>
 *         <td>240x240 dungeon map (good size)</td>
 *         <td>464656</td>
 *         <td>61456</td>
 *         <td>57.52</td>
 *         <td>0.0001237905030818498</td>
 *         <td>0.000935954178599323</td>
 *     </tr>
 *     <tr>
 *         <td>30x70 dungeon map (bad size)</td>
 *         <td>17296</td>
 *         <td>2656</td>
 *         <td>54.24</td>
 *         <td>0.003135985198889917</td>
 *         <td>0.020421686746987953</td>
 *     </tr>
 * </table>
 * In the best-case scenario of packing a 240x240 double array to a short array encoding two states, the result
 * uses less than 1/8000 the memory that the input uses. Writing to disk can store both input and output more
 * efficiently, but the method used here should ensure that even encoding the input FOV map as a flat sequence of
 * single bits and compressing the file should still be on par with the output of pack() due to optimization to
 * ensure nearby cells on a map are compressed together.
 * <br>
 * The technique used by this class is to walk along a Hilbert Curve, storing whether the walk is traveling through
 * "on" or "off" cells, which can be determined by a comparison to a number or a boolean, then encoding alternate shorts
 * into the short[] to be returned, with even-number indices (starting at 0) in the array corresponding to the number of
 * contiguous cells walked through in the "off" state, and odd-number indices corresponding to the number of
 * contiguous cells walked through in the "on" state. A user of this library does not need to understand the details
 * and properties of this algorithm unless they want to generate maps that will compress more optimally. In short:
 * <ul>
 * <li>Smaller maps tend to be processed faster by pack(), since the nature of a Hilbert Curve means a map that
 * fits in one half the width and one half the height of the curve only needs to walk one quarter of the Curve to
 * get all the needed information.</li>
 * <li>Smaller maps also compress less optimally ratio-wise than larger maps with the same area of "on" cells. The
 * compression ratio approaches its best when using very large maps, such as 240x240, and encoding just a few
 * cells on that map (such as for a small FOV radius or a cramped room). A map that is entirely "off" uses only 16
 * bytes of RAM (the minimum for any array on the JVM).</li>
 * <li>Unusually shaped maps can cause compression problems by forcing adjacent cells to sometimes require walking
 * more cells than needed to get to an adjacent cell. For example, a map greater than 64 cells tall, but less than
 * 33 cells wide, has properties that require walking through a large empty area to get to sometimes only a few
 * cells that are "on" before it walks back through empty space. Similarly, a map that is greater than 128 cells
 * tall but is otherwise narrow has the same property of requiring walking through empty space, but also requires
 * the entire Curve to be walked even if the map's width is only a tiny fraction of the Curve's 256 cells.</li>
 * </ul>
 * <b>In shorter-than-short</b>, you'll get particularly good results for compression speed and compressed size with
 * maps approximately these sizes: 240x240, 240x120, 120x120, 60x120, 60x60, 60x30, 30x30. The biggest maps have the
 * best relative gain on compressed memory usage, and the smallest maps have the best compression speed.
 *<br>
 * The details of the algorithm are not terribly complex once you understand the Hilbert Curve. The simplified
 * version of the Hilbert Curve that SquidLib employs is essentially a path through a square grid (it must have side
 * lengths that are powers of 2, and SquidLib always uses 256), starting in the corner cell (x=0,y=0), ending in the
 * corner cell (x=0,y=255), and traversing every other cell on the grid along its path without ever traveling in a
 * loop, crossing the path it walked, or moving in any direction but one cell up, down, left, or right. The shape
 * of the path this takes has the useful property of keeping most groups of cells walked through with similar x and
 * y at similar distances traveled from the start of the curve, and most groups of cells with very dissimilar x and
 * y at very different distances traveled. Since FOV and several other things you might want to encode with CoordPacker
 * tends to be clustered in small areas and occupy more complicated shapes than straight lines due to dungeon layout
 * blocking sections of FOV, the simplest paths of a wide zigzag from side-to-side, or an outward-going-in spiral, have
 * rather poor behavior when determining how much of an area they pass through contiguously. The contiguous area trait
 * is important because of the next step: Run-Length Encoding.
 *<br>
 * Run-Length Encoding is much simpler to explain than the Hilbert Curve, especially without visual aids. In the version
 * SquidLib uses, only on or off states need to be recorded, so the method used here is smaller and more efficient than
 * most methods that need to store repeated characters in strings (and letters, numbers, and punctuation clearly have
 * more than 2 states). The technique works like this:
 *<br>
 * Start in the "off" state, walk down the Hilbert Curve counting how many cells you walk through that are still "off,"
 * and when you encounter a cell that is "on," you write down how many cells were off, transition to the "on" state. Now
 * keep walking the Hilbert Curve, but counting how many cells you walk through that are still "on." When you reach
 * an "off" cell, write down how many were "on," then start walking and counting again, with your count starting at 0.
 * Repeat until you reach the end of the Hilbert Curve, but if you reach the end while counting "off" cells, you don't
 * need to write down that number (a shortcut allows many maps to stop sooner than the 65,536th element of the Curve).
 *<br>
 * There are some additional traits that relate to the edge of the map being treated as "off" even though no
 * calculations are done for cells out of map bounds, and some optimizations that ensure that maps that are smaller than
 * a half, a quarter, or an eighth of the 256x256 curve in both dimensions (and sometimes just one) only need to walk a
 * portion of the Hilbert Curve and simply skip the rest without walking it.
 *<br>
 * The Hilbert Curve has not been definitively proven to be the best possible path to ensure 1D distance and 2D location
 * are similar, but it has been extensively used for tasks that require similar locations for similar distances (in
 * particular, it has become useful in supercomputing clusters for allocating related work to physically nearby
 * machines), and since there hasn't been anything with better spatial properties discovered yet, this technique should
 * remain useful for some time.
 * <br>
 * Created by Tommy Ettinger on 10/1/2015.
 * @author Tommy Ettinger
 */
public class CoordPacker {
    public static final int DEPTH = 8;
    private static final int BITS = DEPTH << 1;

    public static short[] hilbertX = new short[0x10000], hilbertY = new short[0x10000],
            hilbertDistances = new short[0x10000], mooreX = new short[0x100], mooreY = new short[0x100],
            mooreDistances = new short[0x100], hilbert3X = new short[0x200], hilbert3Y = new short[0x200],
            hilbert3Z = new short[0x200], hilbert3Distances = new short[0x200],
            ALL_WALL = new short[0], ALL_ON = new short[]{0, -1};
    private static boolean initialized = false;
    public static void init() {
        if(initialized) return;
        /*
        Coord c;
        for (int i = 0; i < 0x10000; i++) {
            c = hilbertToCoordNoLUT(i);
            hilbertX[i] = (short) c.x;
            hilbertY[i] = (short) c.y;
            hilbertDistances[c.x + (c.y << 8)] = (short) i;
        }*/
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                computeHilbert2D(x, y);
            }
        }

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 8; z++) {
                    computeHilbert3D(x, y, z);
                }
            }
        }

        for (int i = 64; i < 128; i++) {
            mooreX[i - 64] = hilbertX[i];
            mooreY[i - 64] = hilbertY[i];
            mooreDistances[mooreX[i - 64] + (mooreY[i - 64] << 4)] = (short)(i - 64);

            mooreX[i] = hilbertX[i];
            mooreY[i] = (short)(hilbertY[i] + 8);
            mooreDistances[mooreX[i] + (mooreY[i] << 4)] = (short)(i);

            mooreX[i + 64] = (short)(15 - hilbertX[i]);
            mooreY[i + 64] = (short)(15 - hilbertY[i]);
            mooreDistances[mooreX[i + 64] + (mooreY[i + 64] << 4)] = (short)(i + 64);

            mooreX[i + 128] = (short)(15 - hilbertX[i]);
            mooreY[i + 128] = (short)(7 - hilbertY[i]);
            mooreDistances[mooreX[i + 128] + (mooreY[i + 128] << 4)] = (short)(i + 128);
        }
        initialized = true;
    }

    private CoordPacker()
    {
    }
    /**
     * Compresses a double[][] (typically one generated by {@link squidpony.squidgrid.FOV}) that only stores two
     * relevant states (one of which should be 0 or less, the other greater than 0), returning a short[] as described in
     * the {@link CoordPacker} class documentation. This short[] can be passed to CoordPacker.unpack() to restore the
     * relevant states and their positions as a boolean[][] (with false meaning 0 or less and true being any double
     * greater than 0). As stated in the class documentation, the compressed result is intended to use as little memory
     * as possible for most roguelike FOV maps. To avoid floating-point number comparison issues, this actually needs
     * doubles to be greater than 0.0001, which should never cause incorrect behavior with FOV's double[][] maps.
     * <br>
     * <b>To store more than two states</b>, you should use packMulti().
     *
     * @param map a double[][] that probably was returned by FOV. If you obtained a double[][] from DijkstraMap, it
     *            will not meaningfully compress with this method.
     * @return a packed short[] that should, in most circumstances, be passed to unpack() when it needs to be used.
     */
    public static short[] pack(double[][] map)
    {
        if(map == null || map.length == 0)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.pack() must be given a non-empty array");
        int xSize = map.length, ySize = map[0].length;
        if(xSize > 256 || ySize > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently pack, aborting");
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, anyAdded = false, current;
        int skip = 0, limit = 0x10000, mapLimit = xSize * ySize;
        if(ySize <= 128) {
            limit >>= 1;
            if (xSize <= 128) {
                limit >>= 1;
                if (xSize <= 64) {
                    limit >>= 1;
                    if (ySize <= 64) {
                        limit >>= 1;
                        if (ySize <= 32) {
                            limit >>= 1;
                            if (xSize <= 32) {
                                limit >>= 1;
                            }
                        }
                    }
                }
            }
        }

        for(int i = 0, ml = 0; i < limit && ml < mapLimit; i++, skip++)
        {
            if(hilbertX[i] >= xSize || hilbertY[i] >= ySize) {
                if(on) {
                    on = false;
                    packing.add((short) skip);
                    skip = 0;
                    anyAdded = true;
                }
                continue;
            }
            ml++;
            current = map[hilbertX[i]][hilbertY[i]] > 0.0001;
            if(current != on)
            {
                packing.add((short) skip);
                skip = 0;
                on = current;
                anyAdded = true;
            }
        }
        if(on)
            packing.add((short)skip);
        else if(!anyAdded)
            return ALL_WALL;
        return packing.toArray();
    }

    /**
     * Compresses a double[][] (typically one generated by {@link squidpony.squidai.DijkstraMap}) that only stores two
     * relevant states (one of which should be equal to or less than threshold, the other greater than threshold),
     * returning a short[] as described in the {@link CoordPacker} class documentation. This short[] can be passed to
     * CoordPacker.unpack() to restore the relevant states and their positions as a boolean[][] (with true meaning
     * threshold or less and false being any double greater than threshold). As stated in the class documentation, the
     * compressed result is intended to use as little memory as possible for most roguelike FOV maps, but here is also
     * useful for compressing physical maps and gradient maps from DijkstraMap.
     * <br>
     * <b>To store more than two states</b>, you should use packMulti().
     *
     * @param map a double[][] that probably relates in some way to DijkstraMap.
     * @param threshold upper inclusive; any double greater than this will be off, any equal or less will be on
     * @return a packed short[] that should, in most circumstances, be passed to unpack() when it needs to be used.
     */
    public static short[] pack(double[][] map, double threshold)
    {
        if(map == null || map.length == 0)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.pack() must be given a non-empty array");
        int xSize = map.length, ySize = map[0].length;
        if(xSize > 256 || ySize > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently pack, aborting");
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, anyAdded = false, current;
        int skip = 0, limit = 0x10000, mapLimit = xSize * ySize;
        if(ySize <= 128) {
            limit >>= 1;
            if (xSize <= 128) {
                limit >>= 1;
                if (xSize <= 64) {
                    limit >>= 1;
                    if (ySize <= 64) {
                        limit >>= 1;
                        if (ySize <= 32) {
                            limit >>= 1;
                            if (xSize <= 32) {
                                limit >>= 1;
                            }
                        }
                    }
                }
            }
        }

        for(int i = 0, ml = 0; i < limit && ml < mapLimit; i++, skip++)
        {
            if(hilbertX[i] >= xSize || hilbertY[i] >= ySize) {
                if(on) {
                    on = false;
                    packing.add((short) skip);
                    skip = 0;
                    anyAdded = true;
                }
                continue;
            }
            ml++;
            current = map[hilbertX[i]][hilbertY[i]] <= threshold;
            if(current != on)
            {
                packing.add((short) skip);
                skip = 0;
                on = current;
                anyAdded = true;
            }
        }
        if(on)
            packing.add((short)skip);
        else if(!anyAdded)
            return ALL_WALL;
        return packing.toArray();
    }


    /**
     * Compresses a double[][] (typically one generated by {@link squidpony.squidai.DijkstraMap}) that only stores two
     * relevant states (a state for values between lowerBound (inclusive) and upperBound (exclusive), and another state
     * for anything else), returning a short[] as described in the {@link CoordPacker} class documentation. This short[]
     * can be passed to CoordPacker.unpack() to restore the relevant states and their positions as a boolean[][] (with
     * true meaning between the bounds and false being anything outside them). As stated in the class documentation, the
     * compressed result is intended to use as little memory as possible for most roguelike FOV maps, but here is also
     * useful for compressing physical maps and gradient maps from DijkstraMap.
     * <br>
     * <b>To store more than two states</b>, you should use packMulti().
     *
     * @param map a double[][] that probably relates in some way to DijkstraMap.
     * @param lowerBound lower inclusive; any double lower than this will be off, any equal to or greater than this,
     *                   but less than upper, will be on
     * @param upperBound upper exclusive; any double greater than this will be off, any doubles both less than this
     *                   and equal to or greater than lower will be on
     * @return a packed short[] that should, in most circumstances, be passed to unpack() when it needs to be used.
     */
    public static short[] pack(double[][] map, double lowerBound, double upperBound)
    {
        if(map == null || map.length == 0)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.pack() must be given a non-empty array");
        int xSize = map.length, ySize = map[0].length;
        if(xSize > 256 || ySize > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently pack, aborting");
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, anyAdded = false, current;
        int skip = 0, limit = 0x10000, mapLimit = xSize * ySize;
        if(ySize <= 128) {
            limit >>= 1;
            if (xSize <= 128) {
                limit >>= 1;
                if (xSize <= 64) {
                    limit >>= 1;
                    if (ySize <= 64) {
                        limit >>= 1;
                        if (ySize <= 32) {
                            limit >>= 1;
                            if (xSize <= 32) {
                                limit >>= 1;
                            }
                        }
                    }
                }
            }
        }

        for(int i = 0, ml = 0; i < limit && ml < mapLimit; i++, skip++)
        {
            if(hilbertX[i] >= xSize || hilbertY[i] >= ySize) {
                if(on) {
                    on = false;
                    packing.add((short) skip);
                    skip = 0;
                    anyAdded = true;
                }
                continue;
            }
            ml++;
            current = map[hilbertX[i]][hilbertY[i]] >= lowerBound && map[hilbertX[i]][hilbertY[i]] < upperBound;
            if(current != on)
            {
                packing.add((short) skip);
                skip = 0;
                on = current;
                anyAdded = true;
            }
        }
        if(on)
            packing.add((short)skip);
        else if(!anyAdded)
            return ALL_WALL;
        return packing.toArray();
    }

    /**
     * Compresses a byte[][] (typically one generated by an FOV-like method) that only stores two
     * relevant states (one of which should be 0 or less, the other greater than 0), returning a short[] as described in
     * the {@link CoordPacker} class documentation. This short[] can be passed to CoordPacker.unpack() to restore the
     * relevant states and their positions as a boolean[][] (with false meaning 0 or less and true being any byte
     * greater than 0). As stated in the class documentation, the compressed result is intended to use as little memory
     * as possible for most roguelike FOV maps.
     *<br>
     * <b>To store more than two states</b>, you should use packMulti().
     *
     * @param map a byte[][] that probably was returned by an FOV-like method.
     * @return a packed short[] that should, in most circumstances, be passed to unpack() when it needs to be used.
     */
    public static short[] pack(byte[][] map)
    {
        if(map == null || map.length == 0)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.pack() must be given a non-empty array");
        int xSize = map.length, ySize = map[0].length;
        if(xSize > 256 || ySize > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently pack, aborting");
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, anyAdded = false, current;
        int skip = 0, limit = 0x10000, mapLimit = xSize * ySize;
        if(ySize <= 128) {
            limit >>= 1;
            if (xSize <= 128) {
                limit >>= 1;
                if (xSize <= 64) {
                    limit >>= 1;
                    if (ySize <= 64) {
                        limit >>= 1;
                        if (ySize <= 32) {
                            limit >>= 1;
                            if (xSize <= 32) {
                                limit >>= 1;
                            }
                        }
                    }
                }
            }
        }

        for(int i = 0, ml = 0; i < limit && ml < mapLimit; i++, skip++)
        {
            if(hilbertX[i] >= xSize || hilbertY[i] >= ySize) {
                if(on) {
                    on = false;
                    packing.add((short) skip);
                    skip = 0;
                    anyAdded = true;
                }
                continue;
            }
            ml++;
            current = map[hilbertX[i]][hilbertY[i]] > 0;
            if(current != on)
            {
                packing.add((short) skip);
                skip = 0;
                on = current;
                anyAdded = true;
            }
        }
        if(on)
            packing.add((short)skip);
        else if(!anyAdded)
            return ALL_WALL;
        return packing.toArray();
    }

    /**
     * Compresses a boolean[][], returning a short[] as described in the {@link CoordPacker} class documentation. This
     * short[] can be passed to CoordPacker.unpack() to restore the relevant states and their positions as a boolean[][]
     * As stated in the class documentation, the compressed result is intended to use as little memory as possible for
     * most roguelike FOV maps.
     *
     * @param map a boolean[][] that should ideally be mostly false.
     * @return a packed short[] that should, in most circumstances, be passed to unpack() when it needs to be used.
     */
    public static short[] pack(boolean[][] map)
    {
        if(map == null || map.length == 0)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.pack() must be given a non-empty array");
        int xSize = map.length, ySize = map[0].length;
        if(xSize > 256 || ySize > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently pack, aborting");
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, anyAdded = false, current;
        int skip = 0, limit = 0x10000, mapLimit = xSize * ySize;
        if(ySize <= 128) {
            limit >>= 1;
            if (xSize <= 128) {
                limit >>= 1;
                if (xSize <= 64) {
                    limit >>= 1;
                    if (ySize <= 64) {
                        limit >>= 1;
                        if (ySize <= 32) {
                            limit >>= 1;
                            if (xSize <= 32) {
                                limit >>= 1;
                            }
                        }
                    }
                }
            }
        }
        for(int i = 0, ml = 0; i < limit && ml < mapLimit; i++, skip++)
        {
            if(hilbertX[i] >= xSize || hilbertY[i] >= ySize) {
                if(on) {
                    on = false;
                    packing.add((short) skip);
                    skip = 0;
                    anyAdded = true;
                }
                continue;
            }
            ml++;
            current = map[hilbertX[i]][hilbertY[i]];
            if(current != on)
            {
                packing.add((short) skip);
                skip = 0;
                on = current;
                anyAdded = true;
            }
        }
        if(on)
            packing.add((short)skip);
        else if(!anyAdded)
            return ALL_WALL;
        return packing.toArray();
    }

    /**
     * Compresses a char[][] (typically one generated by a map generating method) so only the cells that equal the yes
     * parameter will be encoded as "on", returning a short[] as described in
     * the {@link CoordPacker} class documentation. This short[] can be passed to CoordPacker.unpack() to restore the
     * positions of chars that equal the parameter yes as a boolean[][] (with false meaning not equal and true equal to
     * yes). As stated in the class documentation, the compressed result is intended to use as little memory
     * as possible for most roguelike FOV maps, but this will typically not be used for FOV (more typical uses are for
     * walls, floors, and so on). This can still be useful for certain kinds of processing that can be done more
     * efficiently on packed data than on 2D arrays, like unions, intersections, and random elements or subsets.
     *
     * @param map a char[][] that may contain some area of cells that you want stored as packed data
     * @param yes the char to encode as "on" in the result; all others are encoded as "off"
     * @return a packed short[] that should, in most circumstances, be passed to unpack() when it needs to be used.
     */
    public static short[] pack(char[][] map, char yes)
    {
        if(map == null || map.length == 0)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.pack() must be given a non-empty array");
        int xSize = map.length, ySize = map[0].length;
        if(xSize > 256 || ySize > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently pack, aborting");
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, anyAdded = false, current;
        int skip = 0, limit = 0x10000, mapLimit = xSize * ySize;
        if(ySize <= 128) {
            limit >>= 1;
            if (xSize <= 128) {
                limit >>= 1;
                if (xSize <= 64) {
                    limit >>= 1;
                    if (ySize <= 64) {
                        limit >>= 1;
                        if (ySize <= 32) {
                            limit >>= 1;
                            if (xSize <= 32) {
                                limit >>= 1;
                            }
                        }
                    }
                }
            }
        }

        for(int i = 0, ml = 0; i < limit && ml < mapLimit; i++, skip++)
        {
            if(hilbertX[i] >= xSize || hilbertY[i] >= ySize) {
                if(on) {
                    on = false;
                    packing.add((short) skip);
                    skip = 0;
                    anyAdded = true;
                }
                continue;
            }
            ml++;
            current = map[hilbertX[i]][hilbertY[i]] == yes;
            if(current != on)
            {
                packing.add((short) skip);
                skip = 0;
                on = current;
                anyAdded = true;
            }
        }
        if(on)
            packing.add((short)skip);
        else if(!anyAdded)
            return ALL_WALL;
        return packing.toArray();
    }

    /**
     * Compresses a char[][] (typically one generated by a map generating method) so only the cells that are contained
     * in the yes parameter will be encoded as "on", returning a short[] as described in
     * the {@link CoordPacker} class documentation. This short[] can be passed to CoordPacker.unpack() to restore the
     * positions of chars that equal the parameter yes as a boolean[][] (with false meaning not equal and true equal to
     * yes). As stated in the class documentation, the compressed result is intended to use as little memory
     * as possible for most roguelike FOV maps, but this will typically not be used for FOV (more typical uses are for
     * walls, floors, and so on). This can still be useful for certain kinds of processing that can be done more
     * efficiently on packed data than on 2D arrays, like unions, intersections, and random elements or subsets.
     *
     * @param map a char[][] that may contain some area of cells that you want stored as packed data
     * @param yes the vararg or array of chars to encode as "on" in the result; all others are encoded as "off"
     * @return a packed short[] that should, in most circumstances, be passed to unpack() when it needs to be used.
     */
    public static short[] pack(char[][] map, char... yes)
    {
        if(yes == null || yes.length == 0)
            return ALL_WALL;
        if(yes.length == 1)
            return pack(map, yes[0]);
        if(map == null || map.length == 0)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.pack() must be given a non-empty array");
        int xSize = map.length, ySize = map[0].length;
        if(xSize > 256 || ySize > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently pack, aborting");
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, anyAdded = false, current;
        int skip = 0, limit = 0x10000, mapLimit = xSize * ySize;
        if(ySize <= 128) {
            limit >>= 1;
            if (xSize <= 128) {
                limit >>= 1;
                if (xSize <= 64) {
                    limit >>= 1;
                    if (ySize <= 64) {
                        limit >>= 1;
                        if (ySize <= 32) {
                            limit >>= 1;
                            if (xSize <= 32) {
                                limit >>= 1;
                            }
                        }
                    }
                }
            }
        }
        char c;
        for(int i = 0, ml = 0; i < limit && ml < mapLimit; i++, skip++)
        {
            if(hilbertX[i] >= xSize || hilbertY[i] >= ySize) {
                if(on) {
                    on = false;
                    packing.add((short) skip);
                    skip = 0;
                    anyAdded = true;
                }
                continue;
            }
            ml++;
            c = map[hilbertX[i]][hilbertY[i]];
            current = false;
            for (int j = 0; j < yes.length; j++) {
                if(yes[j] == c)
                {
                    current = true;
                    break;
                }
            }
            if(current != on)
            {
                packing.add((short) skip);
                skip = 0;
                on = current;
                anyAdded = true;
            }
        }
        if(on)
            packing.add((short)skip);
        else if(!anyAdded)
            return ALL_WALL;
        return packing.toArray();
    }
    /**
     * Compresses a int[][] (typically one generated by MixedGenerator.getEnvironment()) so only the cells that equal
     * the yes parameter will be encoded as "on", returning a short[] as described in
     * the {@link CoordPacker} class documentation. This short[] can be passed to CoordPacker.unpack() to restore the
     * positions of ints that equal the parameter yes as a boolean[][] (with false meaning not equal and true equal to
     * yes). As stated in the class documentation, the compressed result is intended to use as little memory
     * as possible for most roguelike FOV maps, but this will typically not be used for FOV (more typical uses are for
     * walls, floors, and so on). This can still be useful for certain kinds of processing that can be done more
     * efficiently on packed data than on 2D arrays, like unions, intersections, and random elements or subsets.
     *
     * @param map a int[][] that may contain some area of cells that you want stored as packed data
     * @param yes the int to encode as "on" in the result; all others are encoded as "off"
     * @return a packed short[] that should, in most circumstances, be passed to unpack() when it needs to be used.
     */
    public static short[] pack(int[][] map, int yes)
    {
        if(map == null || map.length == 0)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.pack() must be given a non-empty array");
        int xSize = map.length, ySize = map[0].length;
        if(xSize > 256 || ySize > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently pack, aborting");
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, anyAdded = false, current;
        int skip = 0, limit = 0x10000, mapLimit = xSize * ySize;
        if(ySize <= 128) {
            limit >>= 1;
            if (xSize <= 128) {
                limit >>= 1;
                if (xSize <= 64) {
                    limit >>= 1;
                    if (ySize <= 64) {
                        limit >>= 1;
                        if (ySize <= 32) {
                            limit >>= 1;
                            if (xSize <= 32) {
                                limit >>= 1;
                            }
                        }
                    }
                }
            }
        }

        for(int i = 0, ml = 0; i < limit && ml < mapLimit; i++, skip++)
        {
            if(hilbertX[i] >= xSize || hilbertY[i] >= ySize) {
                if(on) {
                    on = false;
                    packing.add((short) skip);
                    skip = 0;
                    anyAdded = true;
                }
                continue;
            }
            ml++;
            current = map[hilbertX[i]][hilbertY[i]] == yes;
            if(current != on)
            {
                packing.add((short) skip);
                skip = 0;
                on = current;
                anyAdded = true;
            }
        }
        if(on)
            packing.add((short)skip);
        else if(!anyAdded)
            return ALL_WALL;
        return packing.toArray();
    }

    /**
     * Compresses a int[][] (typically one generated by MixedGenerator.getEnvironment()) so only the cells that are
     * contained in the yes parameter will be encoded as "on", returning a short[] as described in
     * the {@link CoordPacker} class documentation. This short[] can be passed to CoordPacker.unpack() to restore the
     * positions of ints that equal the parameter yes as a boolean[][] (with false meaning not equal and true equal to
     * yes). As stated in the class documentation, the compressed result is intended to use as little memory
     * as possible for most roguelike FOV maps, but this will typically not be used for FOV (more typical uses are for
     * walls, floors, and so on). This can still be useful for certain kinds of processing that can be done more
     * efficiently on packed data than on 2D arrays, like unions, intersections, and random elements or subsets.
     *
     * @param map a int[][] that may contain some area of cells that you want stored as packed data
     * @param yes the vararg or array of ints to encode as "on" in the result; all others are encoded as "off"
     * @return a packed short[] that should, in most circumstances, be passed to unpack() when it needs to be used.
     */
    public static short[] pack(int[][] map, int... yes)
    {
        if(map == null || map.length == 0)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.pack() must be given a non-empty array");
        int xSize = map.length, ySize = map[0].length;
        if(xSize > 256 || ySize > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently pack, aborting");
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, anyAdded = false, current;
        int skip = 0, limit = 0x10000, mapLimit = xSize * ySize;
        if(ySize <= 128) {
            limit >>= 1;
            if (xSize <= 128) {
                limit >>= 1;
                if (xSize <= 64) {
                    limit >>= 1;
                    if (ySize <= 64) {
                        limit >>= 1;
                        if (ySize <= 32) {
                            limit >>= 1;
                            if (xSize <= 32) {
                                limit >>= 1;
                            }
                        }
                    }
                }
            }
        }
        int c;
        for(int i = 0, ml = 0; i < limit && ml < mapLimit; i++, skip++)
        {
            if(hilbertX[i] >= xSize || hilbertY[i] >= ySize) {
                if(on) {
                    on = false;
                    packing.add((short) skip);
                    skip = 0;
                    anyAdded = true;
                }
                continue;
            }
            ml++;
            c = map[hilbertX[i]][hilbertY[i]];
            current = false;
            for (int j = 0; j < yes.length; j++) {
                if(yes[j] == c)
                {
                    current = true;
                    break;
                }
            }
            if(current != on)
            {
                packing.add((short) skip);
                skip = 0;
                on = current;
                anyAdded = true;
            }
        }
        if(on)
            packing.add((short)skip);
        else if(!anyAdded)
            return ALL_WALL;
        return packing.toArray();
    }

    /**
     * Given a number of total levels to consider separate in a double[][] such as an FOV result, this produces a levels
     * array that can be passed to packMulti() to ensure that you have the requested number of separate levels in the
     * multi-packed result. For example, if you pass 6 to this method, it will return a length-6 double array, and if
     * you pass that as the levels parameter to packMulti(), then that method will return a length-6 array of short
     * arrays that each encode a region that met a different minimum value in the originally packed double[][].
     * The behavior of this method causes any doubles that are closer to 1.0 / totalLevels than they are to 0.0 to be
     * packed as "on" in at least one of packMulti()'s resultant sub-arrays. This allows Radius.CIRCLE or similar FOV
     * that produces cells with values that aren't evenly distributed between 0.0 and 1.0 to be used without causing an
     * explosion in the number of required levels.
     * <br>
     * <b>This method should not be used to generate levels for unpacking; it is only intended for packing.</b> Use the
     * similar method generateLightLevels() to generate a levels array that is suitable for unpacking FOV.
     * @param totalLevels the number of separate levels to group doubles into
     * @return a double[] suitable as a levels parameter for packMulti()
     */
    public static double[] generatePackingLevels(int totalLevels)
    {
        if (totalLevels > 63 || totalLevels <= 0)
            throw new UnsupportedOperationException(
                    "Bad totalLevels; should be 0 < totalLevels < 64 but was given " +
                            totalLevels);

        double[] levels = new double[totalLevels];
        for (int i = 0; i < totalLevels; i++) {
            levels[i] = 1.0 * i / totalLevels + 0.5 / totalLevels;
        }
        return levels;
    }

    /**
     * Given a number of total levels to consider separate in a double[][] such as an FOV result, this produces a levels
     * array that can be passed to unpackMultiDouble() to ensure that the minimum double returned for an "on" cell is
     * 1.0 / totalLevels, and every progressively tighter level in the short[][] being unpacked will be close to a
     * multiple of that minimum double value. This only applies to "on" cells; any cells that did not meet a minimum
     * value when packed will still be 0.0. For example, if you pass 6 to this method, it will return a length-6 double
     * array, and if you pass that as the levels parameter to unpackMultiDouble(), then that method will return a
     * double[][] with no more than totalLevels + 1 used values, ranging from 0.0 to 1.0 with evenly spaced values, all
     * multiples of 1.0 / totalLevels, in between.
     * <br>
     * <b>This method should not be used to generate levels for packing; it is only intended for unpacking.</b> Use the
     * similar method generatePackingLevels() to generate a levels array that is suitable for packing double[][] values.
     * @param totalLevels the number of separate levels to assign doubles; this MUST match the size of the levels
     *                    parameter used to pack a double[][] with packMulti() if this is used to unpack that data
     * @return a double[] suitable as a levels parameter for unpackMultiDouble()
     */
    public static double[] generateLightLevels(int totalLevels)
    {
        if (totalLevels > 63 || totalLevels <= 0)
            throw new UnsupportedOperationException(
                    "Bad totalLevels; should be 0 < totalLevels < 64 but was given " +
                            totalLevels);

        double[] levels = new double[totalLevels];
        for (int i = 0; i < totalLevels; i++) {
            levels[i] = (i + 1.0) / totalLevels;
        }
        return levels;
    }

    /**
     * Compresses a double[][] (typically one generated by {@link squidpony.squidgrid.FOV}) that stores any number of
     * states and a double[] storing up to 63 states, ordered from lowest to highest, returning a short[][] as described
     * in the {@link CoordPacker} class documentation. This short[][] can be passed to CoordPacker.unpackMultiDouble()
     * to restore the state at a position to the nearest state in levels, rounded down, and return a double[][] that
     * should preserve the states as closely as intended for most purposes. <b>For compressing FOV, you should generate
     * levels with CoordPacker.generatePackingLevels()</b> instead of manually creating the array, because some
     * imprecision is inherent in floating point math and comparisons are often incorrect between FOV with multiple
     * levels and exact levels generated as simply as possible. generatePackingLevels() adds a small correction to the
     * levels to compensate for floating-point math issues, which shouldn't affect the correctness of the results for
     * FOV radii under 100.
     *<br>
     * As stated in the class documentation, the compressed result is intended to use as little memory as possible for
     * most roguelike FOV maps.
     *<br>
     * <b>To store only two states</b>, you should use pack(), unless the double[][] divides data into on and off based
     * on a relationship to some number other than 0.0. To (probably poorly) pack all the walls (and any cells with
     * values higher than DijkstraMap.WALL) in a DijkstraMap's 2D array of doubles called dijkstraArray, you could call
     * <code>packMulti(dijkstraArray, new double[]{DijkstraMap.WALL});</code>
     * Then, you would use only the one sub-element of the returned short[][].
     *
     * @param map a double[][] that probably was returned by FOV. If you obtained a double[][] from DijkstraMap, it
     *            will not meaningfully compress with this method unless you have very specific needs.
     * @param levels a double[] starting with the lowest value that should be counted as "on" (the outermost cells of
     *               an FOV map that has multiple grades of brightness would be counted by this) and ascending until the
     *               last value; the last value should be highest (commonly 1.0 for FOV), and will be used for any cells
     *               higher than all the other levels values. An example is an array of: 0.25, 0.5, 0.75, 1.0
     * @return a packed short[][] that should, in most circumstances, be passed to unpackMultiDouble() or
     *               unpackMultiByte() when it needs to be used. The 2D array will be jagged with an outer length equal
     *               to the length of levels and sub-arrays that go from having longer lengths early on to very compact
     *               lengths by the end of the short[][].
     */
    public static short[][] packMulti(double[][] map, double[] levels) {
        if (levels == null || levels.length == 0)
            throw new UnsupportedOperationException("Must be given at least one level");
        if (levels.length > 63)
            throw new UnsupportedOperationException(
                    "Too many levels to efficiently pack; should be less than 64 but was given " +
                            levels.length);
        if (map == null || map.length == 0)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.packMulti() must be given a non-empty array");
        int xSize = map.length, ySize = map[0].length;
        if (xSize > 256 || ySize > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently pack, aborting");
        int limit = 0x10000, llen = levels.length, mapLimit = xSize * ySize;
        long on = 0, current = 0;
        ShortVLA[] packing = new ShortVLA[llen];
        int[] skip = new int[llen];

        if(ySize <= 128) {
            limit >>= 1;
            if (xSize <= 128) {
                limit >>= 1;
                if (xSize <= 64) {
                    limit >>= 1;
                    if (ySize <= 64) {
                        limit >>= 1;
                        if (ySize <= 32) {
                            limit >>= 1;
                            if (xSize <= 32) {
                                limit >>= 1;
                            }
                        }
                    }
                }
            }
        }
        short[][] packed = new short[llen][];
        for(int l = 0; l < llen; l++) {
            packing[l] = new ShortVLA(64);
            boolean anyAdded = false;
            for (int i = 0, ml = 0; i < limit && ml < mapLimit; i++, skip[l]++) {
                if (hilbertX[i] >= xSize || hilbertY[i] >= ySize) {
                    if ((on & (1L << l)) != 0L) {
                        on ^= (1L << l);
                        packing[l].add((short) skip[l]);
                        skip[l] = 0;
                        anyAdded = true;
                    }
                    continue;
                }
                ml++;
                // sets the bit at position l in current to 1 if the following is true, 0 if it is false:
                //     map[hilbertX[i]][hilbertY[i]] > levels[l]
                // looks more complicated than it is.
                current ^= ((map[hilbertX[i]][hilbertY[i]] > levels[l] ? -1 : 0) ^ current) & (1 << l);
                if (((current >> l) & 1L) != ((on >> l) & 1L)) {
                    packing[l].add((short) skip[l]);
                    skip[l] = 0;
                    on = current;

                    // sets the bit at position l in on to the same as the bit at position l in current.
                    on ^= (-((current >> l) & 1L) ^ on) & (1L << l);

                    anyAdded = true;
                }
            }

            if (((on >> l) & 1L) == 1L) {
                packing[l].add((short) skip[l]);
                anyAdded = true;
            }
            if(!anyAdded)
                packed[l] = ALL_WALL;
            else
                packed[l] = packing[l].toArray();
        }
        return packed;
    }

    /**
     * Compresses a byte[][] (typically one generated by {@link squidpony.squidgrid.FOVCache}) that stores any number
     * of states and an int no more than 63, returning a short[][] as described in the {@link CoordPacker} class
     * documentation. This short[][] can be passed to CoordPacker.unpackMultiByte() to restore the state at a position
     * to the nearest state possible, capped at levelCount, and return a byte[][] that should preserve the states as
     * closely as intended for most purposes.
     *<br>
     * As stated in the class documentation, the compressed result is intended to use as little memory as possible for
     * most roguelike FOV maps.
     *<br>
     * <b>To store only two states</b>, you should use pack().
     *
     * @param map a byte[][] that probably was returned by a specialized FOV.
     * @param levelCount an int expressing how many levels should be present in the output; values greater than
     *                   levelCount in map will be treated as the highest level.
     * @return a packed short[][] that should, in most circumstances, be passed to unpackMultiDouble() or
     *               unpackMultiByte() when it needs to be used. The 2D array will be jagged with an outer length equal
     *               to the length of levels and sub-arrays that go from having longer lengths early on to very compact
     *               lengths by the end of the short[][].
     */
    public static short[][] packMulti(byte[][] map, int levelCount) {
        if (map == null || map.length == 0)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.packMulti() must be given a non-empty array");
        if (levelCount > 63)
            throw new UnsupportedOperationException(
                    "Too many levels to efficiently pack; should be less than 64 but was given " +
                            levelCount);
        int xSize = map.length, ySize = map[0].length;
        if (xSize > 256 || ySize > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently pack, aborting");
        int limit = 0x10000, mapLimit = xSize * ySize;
        long on = 0, current = 0;
        ShortVLA[] packing = new ShortVLA[levelCount];
        int[] skip = new int[levelCount];

        if(ySize <= 128) {
            limit >>= 1;
            if (xSize <= 128) {
                limit >>= 1;
                if (xSize <= 64) {
                    limit >>= 1;
                    if (ySize <= 64) {
                        limit >>= 1;
                        if (ySize <= 32) {
                            limit >>= 1;
                            if (xSize <= 32) {
                                limit >>= 1;
                            }
                        }
                    }
                }
            }
        }
        short[][] packed = new short[levelCount][];
        short x, y;
        for(int l = 0; l < levelCount; l++) {
            packing[l] = new ShortVLA(64);
            boolean anyAdded = false;
            for (int i = 0, ml = 0; i < limit && ml < mapLimit; i++, skip[l]++) {
                x = hilbertX[i];
                y = hilbertY[i];
                if (x >= xSize || y >= ySize) {
                    if ((on & (1L << l)) != 0L) {
                        on ^= (1L << l);
                        packing[l].add((short) skip[l]);
                        skip[l] = 0;
                        anyAdded = true;
                    }
                    continue;
                }
                ml++;
                // sets the bit at position l in current to 1 if the following is true, 0 if it is false:
                //     map[x][y] > l
                // looks more complicated than it is.
                current ^= ((map[x][y] > l ? -1L : 0L) ^ current) & (1L << l);
                if (((current >> l) & 1L) != ((on >> l) & 1L)) {
                    packing[l].add((short) skip[l]);
                    skip[l] = 0;
                    on = current;

                    // sets the bit at position l in on to the same as the bit at position l in current.
                    on ^= (-((current >> l) & 1L) ^ on) & (1L << l);

                    anyAdded = true;
                }
            }

            if (((on >> l) & 1L) == 1L) {
                packing[l].add((short) skip[l]);
                anyAdded = true;
            }
            if(!anyAdded)
                packed[l] = ALL_WALL;
            else
                packed[l] = packing[l].toArray();
        }
        return packed;
    }

    /**
     * Decompresses a short[] returned by pack() or a sub-array of a short[][] returned by packMulti(), as described in
     * the {@link CoordPacker} class documentation. This returns a boolean[][] that stores the same values that were
     * packed if the overload of pack() taking a boolean[][] was used. If a double[][] was compressed with pack(), the
     * boolean[][] this returns will have true for all values greater than 0 and false for all others. If this is one
     * of the sub-arrays compressed by packMulti(), the index of the sub-array will correspond to an index in the levels
     * array passed to packMulti(), and any cells that were at least equal to the corresponding value in levels will be
     * true, while all others will be false. Width and height do not technically need to match the dimensions of the
     * original 2D array, but under most circumstances where they don't match, the data produced will be junk.
     * @param packed a short[] encoded by calling one of this class' packing methods on a 2D array.
     * @param width the width of the 2D array that will be returned; should match the unpacked array's width.
     * @param height the height of the 2D array that will be returned; should match the unpacked array's height.
     * @return a boolean[][] storing which cells encoded by packed are on (true) or off (false).
     */
    public static boolean[][] unpack(short[] packed, int width, int height)
    {
        if(packed == null)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.unpack() must be given a non-null array");
        boolean[][] unpacked = new boolean[width][height];
        if(packed.length == 0)
            return unpacked;
        boolean on = false;
        int idx = 0;
        short x =0, y = 0;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int toSkip = idx +(packed[p] & 0xffff); idx < toSkip && idx < 0x10000; idx++) {
                    x = hilbertX[idx];
                    y = hilbertY[idx];
                    if(x >= width || y >= height)
                        continue;
                    unpacked[x][y] = true;
                }
            } else {
                idx += packed[p] & 0xffff;
            }
        }
        return unpacked;
    }

    /**
     * Decompresses a short[] returned by pack() or a sub-array of a short[][] returned by packMulti(), as described in
     * the {@link CoordPacker} class documentation. This returns a double[][] that stores 1.0 for true and 0.0 for
     * false if the overload of pack() taking a boolean[][] was used. If a double[][] was compressed with pack(), the
     * double[][] this returns will have 1.0 for all values greater than 0 and 0.0 for all others. If this is one
     * of the sub-arrays compressed by packMulti(), the index of the sub-array will correspond to an index in the levels
     * array passed to packMulti(), and any cells that were at least equal to the corresponding value in levels will be
     * 1.0, while all others will be 0.0. Width and height do not technically need to match the dimensions of the
     * original 2D array, but under most circumstances where they don't match, the data produced will be junk.
     * @param packed a short[] encoded by calling one of this class' packing methods on a 2D array.
     * @param width the width of the 2D array that will be returned; should match the unpacked array's width.
     * @param height the height of the 2D array that will be returned; should match the unpacked array's height.
     * @return a double[][] storing which cells encoded by packed are on (1.0) or off (0.0).
     */
    public static double[][] unpackDouble(short[] packed, int width, int height)
    {
        if(packed == null)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.unpack() must be given a non-null array");
        double[][] unpacked = new double[width][height];
        if(packed.length == 0)
            return unpacked;
        boolean on = false;
        int idx = 0;
        short x =0, y = 0;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int toSkip = idx +(packed[p] & 0xffff); idx < toSkip && idx < 0x10000; idx++) {
                    x = hilbertX[idx];
                    y = hilbertY[idx];
                    if(x >= width || y >= height)
                        continue;
                    unpacked[x][y] = 1.0;
                }
            } else {
                idx += packed[p] & 0xffff;
            }
        }
        return unpacked;
    }

    /**
     * Decompresses a short[] returned by pack() or a sub-array of a short[][] returned by packMulti(), as described in
     * the {@link CoordPacker} class documentation. This returns a double[][] that stores 1.0 for true and 0.0 for
     * false if the overload of pack() taking a boolean[][] was used. If a double[][] was compressed with pack(), the
     * double[][] this returns will have 1.0 for all values greater than 0 and 0.0 for all others. If this is one
     * of the sub-arrays compressed by packMulti(), the index of the sub-array will correspond to an index in the levels
     * array passed to packMulti(), and any cells that were at least equal to the corresponding value in levels will be
     * 1.0, while all others will be 0.0. Width and height do not technically need to match the dimensions of the
     * original 2D array, but under most circumstances where they don't match, the data produced will be junk.
     * @param packed a short[] encoded by calling one of this class' packing methods on a 2D array.
     * @param width the width of the 2D array that will be returned; should match the unpacked array's width.
     * @param height the height of the 2D array that will be returned; should match the unpacked array's height.
     * @return a double[][] storing which cells encoded by packed are on (1.0) or off (0.0).
     */
    public static double[][] unpackDoubleConical(short[] packed, int width, int height,  int centerX, int centerY,
                                                 double angle, double span)
    {
        if(packed == null)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.unpack() must be given a non-null array");
        double[][] unpacked = new double[width][height];
        if(packed.length == 0)
            return unpacked;
        boolean on = false;
        int idx = 0;
        short x =0, y = 0;
        double angle2 = Math.toRadians((angle > 360.0 || angle < 0.0) ? GwtCompatibility.IEEEremainder(angle + 720.0, 360.0) : angle);
        double span2 = Math.toRadians(span);

        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int toSkip = idx +(packed[p] & 0xffff); idx < toSkip && idx < 0x10000; idx++) {
                    x = hilbertX[idx];
                    y = hilbertY[idx];
                    if(x >= width || y >= height)
                        continue;
                    double newAngle = NumberTools.atan2(y - centerY, x - centerX) + Math.PI * 2;
                    if(Math.abs(GwtCompatibility.IEEEremainder(angle2 - newAngle + Math.PI * 8, Math.PI * 2)) > span2 / 2.0)
                        unpacked[x][y] = 0.0;
                    else
                        unpacked[x][y] = 1.0;
                }
            } else {
                idx += packed[p] & 0xffff;
            }
        }
        return unpacked;
    }

    /**
     * Decompresses a short[][] returned by packMulti() and produces an approximation of the double[][] it compressed
     * using the given levels double[] as the values to assign, as described in the {@link CoordPacker} class
     * documentation. The length of levels and the length of the outer array of packed must be equal. However, the
     * levels array passed to this method should not be identical to the levels array passed to packMulti(); for FOV
     * compression, you should get an array for levels using generatePackingLevels(), but for decompression, you should
     * create levels using generateLightLevels(), which should more appropriately fit the desired output. Reusing the
     * levels array used to pack the FOV will usually produce values at the edge of FOV that are less than 0.01 but
     * greater than 0, and will have a maximum value somewhat less than 1.0; neither are usually desirable, but using a
     * different array made with generateLightLevels() will produce doubles ranging from 1.0 / levels.length to 1.0 at
     * the highest. Width and height do not technically need to match the dimensions of the original 2D array, but under
     * most circumstances where they don't match, the data produced will be junk.
     * @param packed a short[][] encoded by calling this class' packMulti() method on a 2D array.
     * @param width the width of the 2D array that will be returned; should match the unpacked array's width.
     * @param height the height of the 2D array that will be returned; should match the unpacked array's height.
     * @param levels a double[] that must have the same length as packed, and will be used to assign cells in the
     *               returned double[][] based on what levels parameter was used to compress packed
     * @return a double[][] where the values that corresponded to the nth value in the levels parameter used to
     * compress packed will now correspond to the nth value in the levels parameter passed to this method.
     */
    public static double[][] unpackMultiDouble(short[][] packed, int width, int height, double[] levels)
    {
        if(packed == null || packed.length == 0)
            throw new ArrayIndexOutOfBoundsException(
                    "CoordPacker.unpackMultiDouble() must be given a non-empty array");
        if (levels == null || levels.length != packed.length)
            throw new UnsupportedOperationException("The lengths of packed and levels must be equal");
        if (levels.length > 63)
            throw new UnsupportedOperationException(
                    "Too many levels to be packed by CoordPacker; should be less than 64 but was given " +
                            levels.length);
        double[][] unpacked = new double[width][height];
        short x= 0, y = 0;
        for(int l = 0; l < packed.length; l++) {
            boolean on = false;
            int idx = 0;
            for (int p = 0; p < packed[l].length; p++, on = !on) {
                if (on) {
                    for (int toSkip = idx + (packed[l][p] & 0xffff); idx < toSkip && idx < 0x10000; idx++) {
                        x = hilbertX[idx];
                        y = hilbertY[idx];
                        if(x >= width || y >= height)
                            continue;
                        unpacked[x][y] = levels[l];
                    }
                } else {
                    idx += packed[l][p] & 0xffff;
                }
            }
        }
        return unpacked;
    }

    /**
     * Decompresses a short[][] returned by packMulti() and produces an approximation of the double[][] it compressed
     * using the given levels double[] as the values to assign, but only using the innermost indices up to limit, as
     * described in the {@link CoordPacker} class documentation. The length of levels and the length of the outer array
     * of packed do not have to be equal. However, the levels array passed to this method should not be identical to the
     * levels array passed to packMulti(); for FOV compression, you should get an array for levels using
     * generatePackingLevels(), but for decompression, you should create levels using generateLightLevels(), which
     * should more appropriately fit the desired output. Reusing the levels array used to pack the FOV will usually
     * produce values at the edge of FOV that are less than 0.01 but greater than 0, and will have a maximum value
     * somewhat less than 1.0; neither are usually desirable, but using a different array made with
     * generateLightLevels() will produce doubles ranging from 1.0 / levels.length to 1.0 at the highest. Width and
     * height do not technically need to match the dimensions of the original 2D array, but under most circumstances
     * where they don't match, the data produced will be junk.
     * @param packed a short[][] encoded by calling this class' packMulti() method on a 2D array.
     * @param width the width of the 2D array that will be returned; should match the unpacked array's width.
     * @param height the height of the 2D array that will be returned; should match the unpacked array's height.
     * @param levels a double[] that must have the same length as packed, and will be used to assign cells in the
     *               returned double[][] based on what levels parameter was used to compress packed
     * @param limit the number of elements to consider from levels and packed, starting from the innermost.
     * @return a double[][] where the values that corresponded to the nth value in the levels parameter used to
     * compress packed will now correspond to the nth value in the levels parameter passed to this method.
     */
    public static double[][] unpackMultiDoublePartial(short[][] packed, int width, int height, double[] levels,
                                                      int limit)
    {
        if(packed == null || packed.length == 0)
            throw new ArrayIndexOutOfBoundsException(
                    "CoordPacker.unpackMultiDouble() must be given a non-empty array");
        if (levels == null || levels.length != packed.length)
            throw new UnsupportedOperationException("The lengths of packed and levels must be equal");
        if (levels.length > 63)
            throw new UnsupportedOperationException(
                    "Too many levels to be packed by CoordPacker; should be less than 64 but was given " +
                            levels.length);
        if(limit > levels.length)
            limit = levels.length;
        double[][] unpacked = new double[width][height];
        short x= 0, y = 0;
        for(int l = packed.length - limit; l < packed.length; l++) {
            boolean on = false;
            int idx = 0;
            for (int p = 0; p < packed[l].length; p++, on = !on) {
                if (on) {
                    for (int toSkip = idx + (packed[l][p] & 0xffff); idx < toSkip && idx < 0x10000; idx++) {
                        x = hilbertX[idx];
                        y = hilbertY[idx];
                        if(x >= width || y >= height)
                            continue;
                        unpacked[x][y] = levels[l];
                    }
                } else {
                    idx += packed[l][p] & 0xffff;
                }
            }
        }
        return unpacked;
    }

    /**
     * Decompresses a short[][] returned by packMulti() and produces an approximation of the double[][] it compressed
     * using the given levels double[] as the values to assign, but only using the innermost indices up to limit, as
     * described in the {@link CoordPacker} class documentation. The length of levels and the length of the outer array
     * of packed do not have to be equal. However, the levels array passed to this method should not be identical to the
     * levels array passed to packMulti(); for FOV compression, you should get an array for levels using
     * generatePackingLevels(), but for decompression, you should create levels using generateLightLevels(), which
     * should more appropriately fit the desired output. Reusing the levels array used to pack the FOV will usually
     * produce values at the edge of FOV that are less than 0.01 but greater than 0, and will have a maximum value
     * somewhat less than 1.0; neither are usually desirable, but using a different array made with
     * generateLightLevels() will produce doubles ranging from 1.0 / levels.length to 1.0 at the highest. This method
     * takes an angle and span as well as a centerX and centerY; the only values that will be greater than 0.0 in the
     * result will be within the round-based conical section that could be produced by traveling from (centerX,centerY)
     * along angle in a limitless line and expanding the cone to be span degrees broad (circularly), centered on angle.
     * Width and height do not technically need to match the dimensions of the original 2D array, but under most
     * circumstances where they don't match, the data produced will be junk.
     * @param packed a short[][] encoded by calling this class' packMulti() method on a 2D array.
     * @param width the width of the 2D array that will be returned; should match the unpacked array's width.
     * @param height the height of the 2D array that will be returned; should match the unpacked array's height.
     * @param levels a double[] that must have the same length as packed, and will be used to assign cells in the
     *               returned double[][] based on what levels parameter was used to compress packed
     * @param limit the number of elements to consider from levels and packed, starting from the innermost.
     * @param centerX the x position of the corner or origin of the conical FOV
     * @param centerY the y position of the corner or origin of the conical FOV
     * @param angle the center of the conical area to limit this to, in degrees
     * @param span the total span of the conical area to limit this to, in degrees
     * @return a double[][] where the values that corresponded to the nth value in the levels parameter used to
     * compress packed will now correspond to the nth value in the levels parameter passed to this method.
     */
    public static double[][] unpackMultiDoublePartialConical(short[][] packed, int width, int height, double[] levels,
                                                      int limit, int centerX, int centerY, double angle, double span)
    {
        if(packed == null || packed.length == 0)
            throw new ArrayIndexOutOfBoundsException(
                    "CoordPacker.unpackMultiDouble() must be given a non-empty array");
        if (levels == null || levels.length != packed.length)
            throw new UnsupportedOperationException("The lengths of packed and levels must be equal");
        if (levels.length > 63)
            throw new UnsupportedOperationException(
                    "Too many levels to be packed by CoordPacker; should be less than 64 but was given " +
                            levels.length);
        if(limit > levels.length)
            limit = levels.length;

        double angle2 = Math.toRadians((angle > 360.0 || angle < 0.0) ? GwtCompatibility.IEEEremainder(angle + 720.0, 360.0) : angle);
        double span2 = Math.toRadians(span);
        double[][] unpacked = new double[width][height];
        short x= 0, y = 0;
        for(int l = packed.length - limit; l < packed.length; l++) {
            boolean on = false;
            int idx = 0;
            for (int p = 0; p < packed[l].length; p++, on = !on) {
                if (on) {
                    for (int toSkip = idx + (packed[l][p] & 0xffff); idx < toSkip && idx < 0x10000; idx++) {
                        x = hilbertX[idx];
                        y = hilbertY[idx];
                        if(x >= width || y >= height)
                            continue;
                        double newAngle = NumberTools.atan2(y - centerY, x - centerX) + Math.PI * 2;
                        if(Math.abs(GwtCompatibility.IEEEremainder(angle2 - newAngle + Math.PI * 8, Math.PI * 2)) > span2 / 2.0)
                            unpacked[x][y] = 0.0;
                        else
                            unpacked[x][y] = levels[l];
                    }
                } else {
                    idx += packed[l][p] & 0xffff;
                }
            }
        }
        return unpacked;
    }

    /**
     * Decompresses a short[][] returned by packMulti() and produces a simple 2D array where the values are bytes
     * corresponding to 1 + the highest index into levels (that is, the original levels parameter passed to packMulti)
     * matched by a cell, or 0 if the cell didn't match any levels during compression, as described in the
     * {@link CoordPacker} class documentation. Width and height do not technically need to match the dimensions of
     * the original 2D array, but under most circumstances where they don't match, the data produced will be junk.
     * @param packed a short[][] encoded by calling this class' packMulti() method on a 2D array.
     * @param width the width of the 2D array that will be returned; should match the unpacked array's width.
     * @param height the height of the 2D array that will be returned; should match the unpacked array's height.
     * @return a byte[][] where the values that corresponded to the nth value in the levels parameter used to
     * compress packed will now correspond to bytes with the value n+1, or 0 if they were "off" in the original array.
     */
    public static byte[][] unpackMultiByte(short[][] packed, int width, int height)
    {
        if(packed == null || packed.length == 0)
            throw new ArrayIndexOutOfBoundsException(
                    "CoordPacker.unpackMultiByte() must be given a non-empty array");
        byte[][] unpacked = new byte[width][height];
        byte lPlus = 1;
        short x=0, y=0;
        for(int l = 0; l < packed.length; l++, lPlus++) {
            boolean on = false;
            int idx = 0;
            for (int p = 0; p < packed[l].length; p++, on = !on) {
                if (on) {
                    for (int toSkip = idx + (packed[l][p] & 0xffff); idx < toSkip && idx <= 0xffff; idx++) {
                        x = hilbertX[idx];
                        y = hilbertY[idx];
                        if(x >= width || y >= height)
                            continue;
                        unpacked[x][y] = lPlus;
                    }
                } else {
                    idx += packed[l][p] & 0xffff;
                }
            }
        }
        return unpacked;
    }

    /**
     * Given a piece of packed data defining a region to use from that map, a char to use for "on" cells and a char to use
     * for "off" cells, produces a 2D char array where all positions that are "off" in packed are filled with the char
     * passed as f, and the cells that are "on" are filled with the char passed as t. Finds the bounding rectangle starting
     * at the origin and extending to the highest x and highest y values encoded in packed, and uses that to determine the
     * width and height of the returned 2D array.
     * @param packed a packed short array, as produced by pack()
     * @param t the char to use for "on" positions in packed
     * @param f the char to use for "off" positions in packed
     * @return a 2D char array, with dimensions determined by the bounds of packed, where any "on" cells equal t and anything else equals f.
     */
    public static char[][] unpackChar(short[] packed, char t, char f)
    {
        if(packed == null || packed.length <= 1)
            throw new UnsupportedOperationException("packed has no contents in unpackChar");

        Coord max = bounds(packed)[1];
        int width = max.x+1, height = max.y+1;
        if(width <= 0 || height <= 0)
            throw new UnsupportedOperationException("Height and width must both be positive in unpackChar");

        char[][] c = new char[width][height];

        for (int i = 0; i < width; i++) {
            Arrays.fill(c[i], f);
        }
        boolean on = false;
        int idx = 0, x, y;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    x = hilbertX[i];
                    y = hilbertY[i];
                    if(x >= width || y >= height)
                        continue;
                    c[x][y] = t;
                }
            }
            idx += packed[p] & 0xffff;
        }
        return c;
    }
    /**
     * Given a piece of packed data defining a region to use from that map, a desired width and height, a char to use for
     * "on" cells and a char to use for "off" cells, produces a 2D char array where all positions that are "off" in packed
     * are filled with the char passed as f, and the cells that are "on" are filled with the char passed as t.
     * @param packed a packed short array, as produced by pack()
     * @param width the desired 2D array width
     * @param height the desired 2D array height
     * @param t the char to use for "on" positions in packed
     * @param f the char to use for "off" positions in packed
     * @return a 2D char array, width by height in dimensions, where any "on" cells equal t and anything else equals f.
     */
    public static char[][] unpackChar(short[] packed, int width, int height, char t, char f)
    {
        if(width <= 0 || height <= 0)
            throw new UnsupportedOperationException("Height and width must both be positive in unpackChar");

        char[][] c = new char[width][height];

        if(packed.length <= 1)
            return c;
        for (int i = 0; i < width; i++) {
            Arrays.fill(c[i], f);
        }
        boolean on = false;
        int idx = 0, x, y;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    x = hilbertX[i];
                    y = hilbertY[i];
                    if(x >= width || y >= height)
                        continue;
                    c[x][y] = t;
                }
            }
            idx += packed[p] & 0xffff;
        }
        return c;
    }

    /**
     * Simple utility method that constructs a GreasedRegion (a faster but more-memory-hungry way to encode regions)
     * from a short array of packed data.
     * @param packed a packed short array, as produced by pack()
     * @param width the desired GreasedRegion's width
     * @param height the desired GreasedRegion's height
     * @return a GreasedRegion that contains the same data as packed, with the specified width and height
     */
    public static GreasedRegion unpackGreasedRegion(short[] packed, int width, int height)
    {
        return new GreasedRegion(unpack(packed, width, height));
    }

    /**
     * Quickly determines if an x,y position is true or false in the given packed array, without unpacking it.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check due to very tight performance constraints).
     * @param x between 0 and 255, inclusive
     * @param y between 0 and 255, inclusive
     * @return true if the packed data stores true at the given x,y location, or false in any other case.
     */
    public static boolean queryPacked(short[] packed, int x, int y)
    {
        int hilbertDistance = posToHilbert(x, y), total = 0;
        boolean on = false;
        for(int p = 0; p < packed.length; p++, on = !on)
        {
            total += packed[p] & 0xffff;
            if(hilbertDistance < total)
                return on;
        }
        return false;
    }
    /**
     * Quickly determines if a Hilbert Curve index corresponds to true or false in the given packed array, without
     * unpacking it.
     * <br>
     * Typically this method will not be needed by library-consuming code unless that code deals with Hilbert Curves in
     * a frequent and deeply involved manner. It does have the potential to avoid converting to and from x,y coordinates
     * and Hilbert Curve indices unnecessarily, which could matter for high-performance code.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check due to very tight performance constraints).
     * @param hilbert a Hilbert Curve index, such as one taken directly from a packed short[] without extra processing
     * @return true if the packed data stores true at the given Hilbert Curve index, or false in any other case.
     */
    public static boolean queryPackedHilbert(short[] packed, short hilbert)
    {
        int hilbertDistance = hilbert & 0xffff, total = 0;
        boolean on = false;
        for(int p = 0; p < packed.length; p++, on = !on)
        {
            total += packed[p] & 0xffff;
            if(hilbertDistance < total)
                return on;
        }
        return false;
    }

    /**
     * Quickly determines if an x,y position is true or false in one of the given packed arrays, without unpacking them,
     * and returns a List of all packed arrays that contain the position.
     * @param x between 0 and 255, inclusive
     * @param y between 0 and 255, inclusive
     * @param packed an array or vararg of short[], such as those returned by pack() or one of the sub-arrays in what is
     *               returned by packMulti(); null elements in packed will be skipped.
     * @return an OrderedSet of all packed arrays that store true at the given x,y location.
     */
    public static OrderedSet<short[]> findManyPacked(int x, int y, short[] ... packed)
    {
        OrderedSet<short[]> packs = new OrderedSet<>(packed.length, CrossHash.shortHasher);
        int hilbertDistance = posToHilbert(x, y);
        for (int a = 0; a < packed.length; a++) {
            if(packed[a] == null) continue;
            int total = 0;
            boolean on = false;
            for (int p = 0; p < packed[a].length; p++, on = !on) {
                total += packed[a][p] & 0xffff;
                if (hilbertDistance < total)
                {
                    if(on)
                        packs.add(packed[a]);
                    break;
                }
            }
        }
        return packs;
    }
    /**
     * Quickly determines if an x,y position is true or false in one of the given packed arrays, without unpacking them,
     * and returns a List of all packed arrays that contain the position.
     * @param x between 0 and 255, inclusive
     * @param y between 0 and 255, inclusive
     * @param packed a Collection of short[] (as encoded by this class); null elements in packed will be skipped.
     * @return an OrderedSet of all packed arrays that store true at the given x,y location.
     */
    public static OrderedSet<short[]> findManyPacked(int x, int y, Collection<short[]> packed)
    {
        OrderedSet<short[]> packs = new OrderedSet<>(packed.size(), CrossHash.shortHasher);
        int hilbertDistance = posToHilbert(x, y);
        for (short[] current : packed) {
            if(current == null) continue;
            int total = 0;
            boolean on = false;
            for (int p = 0; p < current.length; p++, on = !on) {
                total += current[p] & 0xffff;
                if (hilbertDistance < total)
                {
                    if(on)
                        packs.add(current);
                    break;
                }
            }
        }
        return packs;
    }

    /**
     * Quickly determines if a region is contained in one of the given packed arrays, without unpacking them, and
     * returns true if the region checking has some overlap with any of the packed arrays, or false otherwise.
     * @param checking the packed data to check for overlap with the other regions
     * @param packed an array or vararg of short[], such as those returned by pack() or one of the sub-arrays in what is
     *               returned by packMulti(); null elements in packed will be skipped
     * @return true if checking overlaps with any of the packed arrays, or false otherwise
     */
    public static boolean regionsContain(short[] checking, short[] ... packed)
    {
        OrderedSet<short[]> packs = new OrderedSet<>(packed.length, CrossHash.shortHasher);
        for (int a = 0; a < packed.length; a++) {
            if(packed[a] == null) continue;
            if(intersects(checking, packed[a]))
                return true;
        }
        return false;
    }
    /**
     * Quickly determines if a region is contained in one of the given packed arrays, without unpacking them, and
     * returns true if the region checking has some overlap with any of the packed arrays, or false otherwise.
     * @param checking the packed data to check for overlap with the other regions
     * @param packed a Collection of short[], as encoded by this class; null elements in packed will be skipped
     * @return true if checking overlaps with any of the packed arrays, or false otherwise
     */
    public static boolean regionsContain(short[] checking, Collection<short[]> packed)
    {
        OrderedSet<short[]> packs = new OrderedSet<>(packed.size(), CrossHash.shortHasher);
        for (short[] current : packed) {
            if(current == null) continue;
            if(intersects(checking, current))
                return true;
        }
        return false;
    }
    /**
     * Quickly determines if a Hilbert Curve index corresponds to true or false in one of the given packed arrays,
     * without unpacking them, and returns a List of all packed arrays that contain the position.
     * <br>
     * Typically this method will not be needed by library-consuming code unless that code deals with Hilbert Curves in
     * a frequent and deeply involved manner. It does have the potential to avoid converting to and from x,y coordinates
     * and Hilbert Curve indices unnecessarily, which could matter for high-performance code.
     * @param hilbert a Hilbert Curve index, such as one taken directly from a packed short[] without extra processing
     * @param packed an array or vararg of short[], such as those returned by pack() or one of the sub-arrays in what is
     *               returned by packMulti(); null elements in packed will be skipped.
     * @return an ArrayList of all packed arrays that store true at the given x,y location.
     */
    public static ArrayList<short[]> findManyPackedHilbert(short hilbert, short[] ... packed)
    {
        ArrayList<short[]> packs = new ArrayList<>(packed.length);
        int hilbertDistance = hilbert & 0xffff;
        for (int a = 0; a < packed.length; a++) {
            int total = 0;
            boolean on = false;
            for (int p = 0; p < packed[a].length; p++, on = !on) {
                total += packed[a][p] & 0xffff;
                if (hilbertDistance < total)
                {
                    if(on)
                        packs.add(packed[a]);
                    break;
                }
            }
        }
        return packs;
    }

    /**
     * Gets all positions that are "on" in the given packed array, without unpacking it, and returns them as a Coord[].
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check due to very tight performance constraints).
     * @return a Coord[], ordered by distance along the Hilbert Curve, corresponding to all "on" cells in packed.
     */
    public static Coord[] allPacked(short[] packed)
    {
        ShortVLA vla = new ShortVLA(64);
        boolean on = false;
        int idx = 0;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                vla.addRange(idx, idx + (packed[p] & 0xffff));
            }
            idx += packed[p] & 0xffff;
        }
        int[] distances = vla.asInts();
        Coord[] cs = new Coord[distances.length];
        for (int i = 0; i < distances.length; i++) {
            cs[i] = Coord.get(hilbertX[distances[i]], hilbertY[distances[i]]);
        }
        return cs;
    }

    /**
     * Gets all positions that are "on" in the given packed array, without unpacking it, and returns them as an array of
     * Hilbert Curve indices.
     * <br>
     * Typically this method will not be needed by library-consuming code unless that code deals with Hilbert Curves in
     * a frequent and deeply involved manner. It does have the potential to avoid converting to and from x,y coordinates
     * and Hilbert Curve indices unnecessarily, which could matter for high-performance code.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check due to very tight performance constraints).
     * @return a Hilbert Curve index array, in ascending distance order, corresponding to all "on" cells in packed.
     */
    public static short[] allPackedHilbert(short[] packed)
    {
        ShortVLA vla = new ShortVLA(64);
        boolean on = false;
        int idx = 0;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                vla.addRange(idx, idx + (packed[p] & 0xffff));
            }
            idx += packed[p] & 0xffff;
        }
        return vla.toArray();
    }

    /**
     * Gets the nth position that is "on" in the given packed array, without unpacking it, and returns it as a Coord.
     * Uses Hilbert Curve ordering for the exact Hilbert Curve CoordPacker uses, so any two given Coords will always
     * have the same before-after relationship. Returns null if n is not between 0 (inclusive) and the count of packed
     * (exclusive), as by {@code CoordPacker.count()}.
     *
     * You can technically use nth to iterate over only the Coords that are defined in some packed data, but it's
     * drastically more efficient to store a Coord array once with allPacked(). Using nth() as an iterator is
     * essentially running a growing portion of what allPacked() does, over and over again, until the last Coord encoded
     * in packed takes almost as long to process as one call to allPacked(). That said, for a single Coord this can be
     * significantly faster than getting an array with allPacked() and fetching only one item from it.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check due to very tight performance constraints).
     * @param n the index to get in packed
     * @return the nth Coord encoded as "on" by packed, ordered by distance along the Hilbert Curve, or null if n is out of bounds
     */
    public static Coord nth(final short[] packed, final int n)
    {
        if(n < 0)
            return null;
        boolean on = false;
        int idx = 0, ct = n, tmp;
        for(int p = 0; p < packed.length; p++, on = !on) {
            tmp = (packed[p] & 0xffff);
            if (on) {
                if(ct - tmp < 0)
                {
                    idx += ct;
                    ct -= tmp;
                    break;
                }
                ct -= tmp;
            }
            idx += tmp;
        }
        if(ct >= 0)
            return null;
        return Coord.get(hilbertX[idx], hilbertY[idx]);
    }
    /**
     * Gets the positions that are "on" in the given packed array, without unpacking it, repeatedly goes through a
     * number of "on" cells equal to fraction and stores one of those cells as a Coord, and returns the accumulated
     * portion of positions as a Coord[].
     * <br>
     * For purposes of finding mostly cells with a similar distance to each other but without obvious patterns, a value
     * of 5, 6, or 7 for fraction works well for relatively-close Coords, but larger values are better for packed data
     * with wide, expansive areas. If you want to make the regular pattern this uses impossible to discern, you can use
     * {@code randomSeparated()} to keep distance between Coords and sample most areas of some packed data. Values for
     * fraction that are multiples of 4 are likely to show a pattern in large open spaces more easily.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check due to very tight performance constraints).
     * @param fraction the denominator of the approximate fraction of "on" cells to use
     * @return a Coord[] corresponding to a fraction of the "on" cells in packed.
     */
    public static Coord[] fractionPacked(short[] packed, int fraction)
    {
        if(fraction <= 1)
            return allPacked(packed);
        ShortVLA vla = new ShortVLA(64);
        boolean on = false;
        int idx = 0, ctr = 0;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++, ctr = (ctr + 1) % fraction) {
                    if(ctr == 0)
                        vla.add((short)i);
                }
            }
            idx += packed[p] & 0xffff;
        }
        int[] distances = vla.asInts();
        Coord[] cs = new Coord[distances.length];
        for (int i = 0; i < distances.length; i++) {
            cs[i] = Coord.get(hilbertX[distances[i]], hilbertY[distances[i]]);
        }
        return cs;
    }

    /**
     * Gets the positions that are "on" in the given packed array, without unpacking it, repeatedly goes through a
     * number of "on" cells equal to fraction and stores a random one of those cells as a Coord, and returns the
     * accumulated random portion of positions as a Coord[]. Because of how this works, it is much more likely that the
     * Coords will be dispersed so that there's a good amount of minimum distance between most Coords, while methods
     * like randomPortion() do not make such dispersal a priority and may return tight clusters of Coords.
     * <br>
     * For purposes of finding mostly cells with a similar distance to each other but without obvious patterns, a value
     * of at least 7 for fraction works well.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check due to very tight performance constraints).
     * @param separation the denominator of the approximate fraction of "on" cells to use
     * @param rng the RNG to use to incorporate a random factor to the generation
     * @return a Coord[] corresponding to a fraction of the "on" cells in packed.
     */
    public static Coord[] randomSeparated(short[] packed, int separation, RNG rng)
    {
        if(separation <= 1)
            return allPacked(packed);
        ShortVLA vla = new ShortVLA(64);
        boolean on = false;
        int idx = 0, ctr = 0, tgt = rng.nextInt(separation);
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++, ctr++) {
                    if(ctr >= separation)
                    {
                        ctr %= separation;
                        tgt = rng.nextInt(separation);
                    }
                    if(ctr == tgt)
                        vla.add((short)i);
                }
            }
            idx += packed[p] & 0xffff;
        }
        int[] distances = vla.asInts();
        Coord[] cs = new Coord[distances.length];
        for (int i = 0; i < distances.length; i++) {
            cs[i] = Coord.get(hilbertX[distances[i]], hilbertY[distances[i]]);
        }
        return cs;
    }

    /**
     * Gets the positions that are "on" in the given packed array, without unpacking it, repeatedly goes through a
     * number of "on" cells equal to fraction and stores one of those cells as a Coord, and returns the accumulated
     * portion of positions as an array of Hilbert Curve indices.
     * <br>
     * For purposes of finding mostly cells with a similar distance to each other but without obvious patterns, a value
     * of 5, 6, or 7 for fraction works well.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check due to very tight performance constraints).
     * @param fraction the approximate fraction of "on" cells to use
     * @return a Hilbert Curve index array corresponding to a fraction of the "on" cells in packed.
     */
    public static short[] fractionPackedHilbert(short[] packed, int fraction)
    {
        if(fraction <= 1)
            return allPackedHilbert(packed);
        ShortVLA vla = new ShortVLA(64);
        boolean on = false;
        int idx = 0, ctr = 0;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++, ctr = (ctr + 1) % fraction) {
                    if(ctr == 0)
                        vla.add((short)i);
                }
            }
            idx += packed[p] & 0xffff;
        }
        return vla.toArray();
    }

    /**
     * Gets the positions that are "on" in the given packed array, without unpacking it, keeps only positions that are
     * at least minDistance apart from other positions this will return, and returns the positions as a Coord[].
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check due to very tight performance constraints).
     * @param minDistance the minimum distance (measured 8-way, Chebyshev) between any positions this returns
     * @return a Coord[] corresponding to a portion of the "on" cells in packed.
     */
    public static Coord[] apartPacked(short[] packed, int minDistance)
    {
        if(minDistance < 1)
            return allPacked(packed);
        ShortVLA vla = new ShortVLA(64);
        boolean on = false;
        int idx = 0;
        ShortSet ss = new ShortSet(256);
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    int x = hilbertX[i], y = hilbertY[i], dist = hilbertDistances[x + (y << 8)];
                    if (ss.add((short) dist)) {
                        for (int xx = Math.max(0, x - minDistance); xx <= Math.min(255, x + minDistance); xx++) {
                            for (int yy = Math.max(0, y - minDistance); yy <= Math.min(255, y + minDistance); yy++) {
                                dist = hilbertDistances[xx + (yy << 8)];
                                if(dist >= i)
                                    ss.add((short) dist);
                            }
                        }
                        vla.add((short) i);
                    }
                }
            }
            idx += packed[p] & 0xffff;
        }
        int[] distances = vla.asInts();
        Coord[] cs = new Coord[distances.length];
        for (int i = 0; i < distances.length; i++) {
            cs[i] = Coord.get(hilbertX[distances[i]], hilbertY[distances[i]]);
        }
        return cs;
    }
    /**
     * Gets the positions that are "on" in the given packed array, without unpacking it, keeps only positions that are
     * at least minDistance apart from other positions this will return, and returns the positions as a Coord[].
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check due to very tight performance constraints).
     * @param minDistance the minimum distance (measurement depends on eightWay) between any positions this returns
     * @param eightWay true if distance should be measured equally in 8 directions, false to use 4 directions
     * @return a Coord[] corresponding to a portion of the "on" cells in packed.
     */
    public static Coord[] apartPacked(short[] packed, int minDistance, boolean eightWay)
    {
        if(minDistance < 1)
            return allPacked(packed);
        if(eightWay)
            return apartPacked(packed, minDistance);

        ShortVLA vla = new ShortVLA(64);
        boolean on = false;
        int idx = 0;
        ShortSet ss = new ShortSet(256);
        int xx, yy;
        int[] xOffsets = new int[]{0, 1, 0, -1, 0}, yOffsets = new int[]{1, 0, -1, 0, 1};
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    int x = hilbertX[i], y = hilbertY[i], dist = hilbertDistances[x + (y << 8)];
                    if (ss.add((short) dist)) {
                        for (int d = 0; d < 4; d++) {
                            for (int e = 1; e <= minDistance; e++) {
                                for (int e2 = 0; e2 < minDistance; e2++) {
                                    xx = Math.min(255, Math.max(0, x + xOffsets[d] * e + yOffsets[d + 1] * e2));
                                    yy = Math.min(255, Math.max(0, y + yOffsets[d] * e + xOffsets[d + 1] * e2));
                                    dist = hilbertDistances[xx + (yy << 8)];
                                    if (dist >= i)
                                        ss.add((short) dist);
                                }
                            }
                        }
                        vla.add((short) i);
                    }
                }
            }
            idx += packed[p] & 0xffff;
        }
        int[] distances = vla.asInts();
        Coord[] cs = new Coord[distances.length];
        for (int i = 0; i < distances.length; i++) {
            cs[i] = Coord.get(hilbertX[distances[i]], hilbertY[distances[i]]);
        }
        return cs;
    }

    /**
     * Gets the positions that are "on" in the given packed array, without unpacking it, keeps only positions that are
     * at least minDistance apart from other positions this will return, and returns the positions as an array of
     * Hilbert Curve indices.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check due to very tight performance constraints).
     * @param minDistance the minimum distance (measured 8-way, Chebyshev) between any positions this returns
     * @return a Hilbert Curve index array corresponding to a portion of the "on" cells in packed.
     */
    public static short[] apartPackedHilbert(short[] packed, int minDistance)
    {
        if(minDistance < 1)
            return allPackedHilbert(packed);
        ShortVLA vla = new ShortVLA(64);
        boolean on = false;
        int idx = 0;
        ShortSet ss = new ShortSet(256);
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    int x = hilbertX[i], y = hilbertY[i], dist = hilbertDistances[x + (y << 8)];
                    if (ss.add((short) dist)) {
                        for (int xx = Math.max(0, x - minDistance); xx <= Math.min(255, x + minDistance); xx++) {
                            for (int yy = Math.max(0, y - minDistance); yy <= Math.min(255, y + minDistance); yy++) {
                                dist = hilbertDistances[xx + (yy << 8)];
                                if(dist >= i)
                                    ss.add((short) dist);
                            }
                        }
                        vla.add((short) i);
                    }
                }
            }
            idx += packed[p] & 0xffff;
        }
        return vla.toArray();
    }

    /**
     * Gets the positions that are "on" in the given packed array, without unpacking it, keeps only positions that are
     * at least minDistance apart from other positions this will return, and returns the positions as an array of
     * Hilbert Curve indices.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check due to very tight performance constraints).
     * @param minDistance the minimum distance (measurement depends on eightWay) between any positions this returns
     * @param eightWay true if distance should be measured equally in 8 directions, false to use 4 directions
     * @return a Hilbert Curve index array corresponding to a portion of the "on" cells in packed.
     */
    public static short[] apartPackedHilbert(short[] packed, int minDistance, boolean eightWay)
    {
        if(minDistance < 1)
            return allPackedHilbert(packed);
        if(eightWay)
            return apartPackedHilbert(packed, minDistance);

        ShortVLA vla = new ShortVLA(64);
        boolean on = false;
        int idx = 0;
        ShortSet ss = new ShortSet(256);
        int xx, yy;
        int[] xOffsets = new int[]{0, 1, 0, -1, 0}, yOffsets = new int[]{1, 0, -1, 0, 1};
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    int x = hilbertX[i], y = hilbertY[i], dist = hilbertDistances[x + (y << 8)];
                    if (ss.add((short) dist)) {
                        for (int d = 0; d < 4; d++) {
                            for (int e = 1; e <= minDistance; e++) {
                                for (int e2 = 0; e2 < minDistance; e2++) {
                                    xx = Math.min(255, Math.max(0, x + xOffsets[d] * e + yOffsets[d + 1] * e2));
                                    yy = Math.min(255, Math.max(0, y + yOffsets[d] * e + xOffsets[d + 1] * e2));
                                    dist = hilbertDistances[xx + (yy << 8)];
                                    if (dist >= i)
                                        ss.add((short) dist);
                                }
                            }
                        }
                        vla.add((short) i);
                    }
                }
            }
            idx += packed[p] & 0xffff;
        }
        return vla.toArray();
    }
    private static int clamp(int n, int min, int max)
    {
        return Math.min(Math.max(min, n), max - 1);
    }

    /**
     * Move all "on" positions in packed by the number of cells given in xMove and yMove, unless the move
     * would take them further than 0, width - 1 (for xMove) or height - 1 (for yMove), in which case that
     * cell is stopped at the edge (moving any shape by an xMove greater than width or yMove greater than
     * height will move all "on" cells to that edge, in a 1-cell thick line). Returns a new packed short[]
     * and does not modify packed.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti()
     * @param xMove distance to move the x-coordinate; can be positive or negative
     * @param yMove distance to move the y-coordinate; can be positive or negative
     * @param width the maximum width; if a cell would move to x at least equal to width, it stops at width - 1
     * @param height the maximum height; if a cell would move to y at least equal to height, it stops at height - 1
     * @return a packed array that encodes "on" for cells that were moved from cells that were "on" in packed
     */
    public static short[] translate(short[] packed, int xMove, int yMove, int width, int height)
    {
        if(packed == null || packed.length <= 1)
        {
            return ALL_WALL;
        }
        ShortVLA vla = new ShortVLA(256);
        boolean on = false;
        int idx = 0, x, y;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    x = clamp(hilbertX[i] + xMove, 0, width);
                    y = clamp(hilbertY[i] + yMove, 0, height);
                    vla.add(hilbertDistances[x + (y << 8)]);
                }
            }
            idx += packed[p] & 0xffff;
        }
        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);
        vla = new ShortVLA(128);
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (int i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));

        return vla.toArray();
    }

    /**
     * Expand each "on" position in packed to cover a a square with side length equal to 1 + expansion * 2,
     * centered on the original "on" position, unless the expansion would take a cell further than 0,
     * width - 1 (for xMove) or height - 1 (for yMove), in which case that cell is stopped at the edge.
     * Uses 8-way movement (Chebyshev distance) unless the overload of this function that takes a boolean argument
     * eightWay is used and that argument is false.
     * Returns a new packed short[] and does not modify packed.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti()
     * @param expansion the positive (square) radius, in cells, to expand each cell out by
     * @param width the maximum width; if a cell would move to x at least equal to width, it stops at width - 1
     * @param height the maximum height; if a cell would move to y at least equal to height, it stops at height - 1
     * @return a packed array that encodes "on" for packed and cells that expanded from cells that were "on" in packed
     */
    public static short[] expand(short[] packed, int expansion, int width, int height)
    {
        if(packed == null || packed.length <= 1)
        {
            return ALL_WALL;
        }
        ShortVLA vla = new ShortVLA(256);
        ShortSet ss = new ShortSet(256);
        boolean on = false;
        int idx = 0, x, y;
        short dist;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    x = hilbertX[i];
                    y = hilbertY[i];
                    for (int j = Math.max(0, x - expansion); j <= Math.min(width - 1, x + expansion); j++) {
                        for (int k = Math.max(0, y - expansion); k <= Math.min(height - 1, y + expansion); k++) {
                            dist = hilbertDistances[j + (k << 8)];
                            if (ss.add(dist))
                                vla.add(dist);
                        }
                    }
                }
            }
            idx += packed[p] & 0xffff;
        }

        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);

        vla = new ShortVLA(128);
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (int i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));
        return vla.toArray();
    }


    /**
     * Expand each "on" position in packed to cover a a square with side length equal to 1 + expansion * 2,
     * centered on the original "on" position, unless the expansion would take a cell further than 0,
     * width - 1 (for xMove) or height - 1 (for yMove), in which case that cell is stopped at the edge.
     * Returns a new packed short[] and does not modify packed.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti()
     * @param expansion the positive (square) radius, in cells, to expand each cell out by
     * @param width the maximum width; if a cell would move to x at least equal to width, it stops at width - 1
     * @param height the maximum height; if a cell would move to y at least equal to height, it stops at height - 1
     * @param eightWay true if the expansion should be both diagonal and orthogonal; false for just orthogonal
     * @return a packed array that encodes "on" for packed and cells that expanded from cells that were "on" in packed
     */
    public static short[] expand(short[] packed, int expansion, int width, int height, boolean eightWay)
    {
        if(eightWay)
            return expand(packed, expansion, width, height);
        if(packed == null || packed.length <= 1)
        {
            return ALL_WALL;
        }
        ShortVLA vla = new ShortVLA(256);
        ShortSet ss = new ShortSet(256);
        boolean on = false;
        int idx = 0, x, y, j, k;
        short dist;
        int[] xOffsets = new int[]{0, 1, 0, -1, 0}, yOffsets = new int[]{1, 0, -1, 0, 1};
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    x = hilbertX[i];
                    y = hilbertY[i];
                    dist = hilbertDistances[x + (y << 8)];
                    if (ss.add(dist))
                        vla.add(dist);
                    for (int d = 0; d < 4; d++) {
                        for (int e = 1; e <= expansion; e++) {
                            for (int e2 = 0; e2 <= expansion - e; e2++) {
                                j = Math.min(width - 1, Math.max(0, x + xOffsets[d] * e + yOffsets[d + 1] * e2));
                                k = Math.min(height - 1, Math.max(0, y + yOffsets[d] * e + xOffsets[d + 1] * e2));
                                dist = hilbertDistances[j + (k << 8)];
                                if (ss.add(dist))
                                    vla.add(dist);
                            }
                        }
                    }
                }
            }
            idx += packed[p] & 0xffff;
        }

        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);

        vla = new ShortVLA(128);
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (int i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));

        return vla.toArray();
    }

    /**
     * Finds the area made by removing the "on" positions in packed that are within the specified retraction distance of
     * an "off" position or the edge of the map. This essentially finds a shrunken version of packed.
     * Uses 8-way movement (Chebyshev distance) unless the overload of this function that takes a boolean argument
     * eightWay is used and that argument is false.
     * Returns a new packed short[] and does not modify packed.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti()
     * @param retraction the positive (square) radius, in cells, to pull each cell in by
     * @param width the maximum width; if a cell would move to x at least equal to width, it stops at width - 1
     * @param height the maximum height; if a cell would move to y at least equal to height, it stops at height - 1
     * @return a short[] that encodes "on" for cells that were "on" in packed and were far enough from an "off" cell
     */
    public static short[] retract(short[] packed, int retraction, int width, int height)
    {
        return differencePacked(packed, expand(negatePacked(packed), retraction, width, height, true));
    }
/*    public static short[] retract(short[] packed, int retraction, int width, int height)
    {
        if(packed == null || packed.length <= 1)
        {
            return ALL_WALL;
        }
        ShortVLA vla = new ShortVLA(256);
        boolean on = false;
        int idx = 0, x, y;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                INDICES:
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++)
                {
                    x = hilbertX[i];
                    y = hilbertY[i];
                    for (int j = x - retraction; j <= x + retraction; j++) {
                        for (int k = y - retraction; k <= y + retraction; k++) {
                            if(j < 0 || k < 0 || j >= width || k >= height ||
                                    !queryPackedHilbert(packed, hilbertDistances[j + (k << 8)]))
                                continue INDICES;
                        }
                    }

                    vla.add((short)i);
                }
            }
            idx += packed[p] & 0xffff;
        }

        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);

        vla = new ShortVLA(128);
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (int i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));
        return vla.toArray();
    }
    */
    /**
     * Finds the area made by removing the "on" positions in packed that are within the specified retraction distance of
     * an "off" position or the edge of the map. This essentially finds a shrunken version of packed.
     * Returns a new packed short[] and does not modify packed.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti()
     * @param retraction the positive (square) radius, in cells, to pull each cell in by
     * @param width the maximum width; cells outside this are considered "off" for this method's purposes
     * @param height the maximum height; cells outside this are considered "off" for this method's purposes
     * @param eightWay true if the retraction should be both diagonal and orthogonal; false for just orthogonal
     * @return a packed array that encodes "on" for packed and cells that expanded from cells that were "on" in packed
     */
    public static short[] retract(short[] packed, int retraction, int width, int height, boolean eightWay)
    {
        return differencePacked(packed, expand(negatePacked(packed), retraction, width, height, eightWay));
    }
    /*
    {
        if(eightWay)
            return retract(packed, retraction, width, height);
        if(packed == null || packed.length <= 1)
        {
            return ALL_WALL;
        }
        ShortVLA vla = new ShortVLA(256);
        boolean on = false;
        int idx = 0, x, y, j, k;
        int[] xOffsets = new int[]{0, 1, 0, -1, 0}, yOffsets = new int[]{1, 0, -1, 0, 1};
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                INDICES:
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++)
                {
                    x = hilbertX[i];
                    y = hilbertY[i];
                    for (int d = 0; d < 4; d++) {
                        for (int e = 1; e <= retraction; e++) {
                            for (int e2 = 0; e2 < retraction; e2++) {
                                j = x + xOffsets[d] * e + yOffsets[d + 1] * e2;
                                k = y + yOffsets[d] * e + xOffsets[d + 1] * e2;
                                if (j < 0 || k < 0 || j >= width || k >= height ||
                                        !queryPackedHilbert(packed, hilbertDistances[j + (k << 8)]))
                                    continue INDICES;
                            }
                        }
                    }
                    vla.add((short)i);
                }
            }
            idx += packed[p] & 0xffff;
        }

        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);

        vla = new ShortVLA(128);
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (int i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));
        return vla.toArray();
    }
    */



    /**
     * Finds the area around the cells encoded in packed, without including those cells. For each "on"
     * position in packed, expand it to cover a a square with side length equal to 1 + expansion * 2,
     * centered on the original "on" position, unless the expansion would take a cell further than 0,
     * width - 1 (for xMove) or height - 1 (for yMove), in which case that cell is stopped at the edge.
     * If a cell is "on" in packed, it will always be "off" in the result.
     * Uses 8-way movement (Chebyshev distance) unless the overload of this function that takes a boolean argument
     * eightWay is used and that argument is false.
     * Returns a new packed short[] and does not modify packed.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti()
     * @param expansion the positive (square-shaped) radius, in cells, to expand each cell out by
     * @param width the maximum width; if a cell would move to x at least equal to width, it stops at width - 1
     * @param height the maximum height; if a cell would move to y at least equal to height, it stops at height - 1
     * @return a packed array that encodes "on" for cells that were pushed from the edge of packed's "on" cells
     */
    public static short[] fringe(short[] packed, int expansion, int width, int height)
    {
        if(packed == null || packed.length <= 1)
        {
            return ALL_WALL;
        }
        ShortVLA vla = new ShortVLA(256);
        ShortSet ss = new ShortSet(256);
        boolean on = false;
        int idx = 0;
        short x, y, dist;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    ss.add((short) i);
                }
            }
            idx += packed[p] & 0xffff;
        }
        on = false;
        idx = 0;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    x = hilbertX[i];
                    y = hilbertY[i];
                    for (int j = Math.max(0, x - expansion); j <= Math.min(width - 1, x + expansion); j++) {
                        for (int k = Math.max(0, y - expansion); k <= Math.min(height - 1, y + expansion); k++) {
                            dist = hilbertDistances[j + (k << 8)];
                            if (ss.add(dist))
                                vla.add(dist);
                        }
                    }
                }
            }
            idx += packed[p] & 0xffff;
        }
        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);

        vla = new ShortVLA(128);
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (int i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));

        return vla.toArray();
    }

    /**
     * Finds the area around the cells encoded in packed, without including those cells. For each "on"
     * position in packed, expand it to cover a a square with side length equal to 1 + expansion * 2,
     * centered on the original "on" position, unless the expansion would take a cell further than 0,
     * width - 1 (for xMove) or height - 1 (for yMove), in which case that cell is stopped at the edge.
     * If a cell is "on" in packed, it will always be "off" in the result.
     * Returns a new packed short[] and does not modify packed.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti()
     * @param expansion the positive (square-shaped) radius, in cells, to expand each cell out by
     * @param width the maximum width; if a cell would move to x at least equal to width, it stops at width - 1
     * @param height the maximum height; if a cell would move to y at least equal to height, it stops at height - 1
     * @param eightWay true if the expansion should be both diagonal and orthogonal; false for just orthogonal
     * @return a packed array that encodes "on" for cells that were pushed from the edge of packed's "on" cells
     */
    public static short[] fringe(short[] packed, int expansion, int width, int height, boolean eightWay)
    {
        if(eightWay)
            return fringe(packed, expansion, width, height);
        if(packed == null || packed.length <= 1)
        {
            return ALL_WALL;
        }
        ShortVLA vla = new ShortVLA(256);
        ShortSet ss = new ShortSet(256);
        boolean on = false;
        int idx = 0;
        short x, y, dist;
        int[] xOffsets = new int[]{0, 1, 0, -1, 0}, yOffsets = new int[]{1, 0, -1, 0, 1};
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    ss.add((short) i);
                }
            }
            idx += packed[p] & 0xffff;
        }
        on = false;
        idx = 0;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    x = hilbertX[i];
                    y = hilbertY[i];
                    for (int d = 0; d < 4; d++) {
                        for (int e = 1; e <= expansion; e++) {
                            for (int e2 = 0; e2 <= expansion - e; e2++) {
                                int j = Math.min(width - 1, Math.max(0, x + xOffsets[d] * e + yOffsets[d + 1] * e2));
                                int k = Math.min(height - 1, Math.max(0, y + yOffsets[d] * e + xOffsets[d + 1] * e2));
                                dist = hilbertDistances[j + (k << 8)];
                                if (ss.add(dist))
                                    vla.add(dist);
                            }
                        }
                    }

                }
            }
            idx += packed[p] & 0xffff;
        }
        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);

        vla = new ShortVLA(128);
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (int i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));

        return vla.toArray();
    }
    /**
     * Finds the area around the cells encoded in packed, without including those cells. For each "on"
     * position in packed, expand it to cover a a square with side length equal to 1 + expansion * 2,
     * centered on the original "on" position, unless the expansion would take a cell further than 0,
     * width - 1 (for xMove) or height - 1 (for yMove), in which case that cell is removed if drop is
     * true, or stopped at the edge if drop is false.
     * If a cell is "on" in packed, it will always be "off" in the result.
     * Returns a new packed short[] and does not modify packed.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti()
     * @param expansion the positive (square-shaped) radius, in cells, to expand each cell out by
     * @param width the maximum width; if a cell would move to x at least equal to width, it stops at width - 1
     * @param height the maximum height; if a cell would move to y at least equal to height, it stops at height - 1
     * @param eightWay true if the expansion should be both diagonal and orthogonal; false for just orthogonal
     * @param drop true to drop cells that would expand into negative coordinates or past width/height, false to stop
     *             their expansion early
     * @return a packed array that encodes "on" for cells that were pushed from the edge of packed's "on" cells
     */
    public static short[] fringe(short[] packed, int expansion, int width, int height, boolean eightWay, boolean drop)
    {
        if(!drop)
            return fringe(packed, expansion, width, height, eightWay);
        if(packed == null || packed.length <= 1)
        {
            return ALL_WALL;
        }
        ShortVLA vla = new ShortVLA(256);
        ShortSet ss = new ShortSet(256);
        boolean on = false;
        int idx = 0;
        short x, y, dist;
        int[] xOffsets = new int[]{0, 1, 0, -1, 0}, yOffsets = new int[]{1, 0, -1, 0, 1};
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    ss.add((short) i);
                }
            }
            idx += packed[p] & 0xffff;
        }
        on = false;
        idx = 0;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    x = hilbertX[i];
                    y = hilbertY[i];
                    if (eightWay) {
                        for (int j = x - expansion; j <= x + expansion; j++) {
                            for (int k = y - expansion; k <= y + expansion; k++) {
                                if (j >= 0 && k >= 0 && j < width && k < height) {
                                    dist = hilbertDistances[j + (k << 8)];
                                    if (ss.add(dist))
                                        vla.add(dist);
                                }
                            }
                        }
                    } else {
                        for (int d = 0; d < 4; d++) {
                            for (int e = 1; e <= expansion; e++) {
                                for (int e2 = 0; e2 <= expansion - e; e2++) {
                                    int j = x + xOffsets[d] * e + yOffsets[d + 1] * e2;
                                    int k = y + yOffsets[d] * e + xOffsets[d + 1] * e2;

                                    if (j >= 0 && k >= 0 && j < width && k < height) {
                                        dist = hilbertDistances[j + (k << 8)];
                                        if (ss.add(dist))
                                            vla.add(dist);
                                    }
                                }
                            }
                        }
                    }

                }
            }
            idx += packed[p] & 0xffff;
        }
        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);

        vla = new ShortVLA(128);
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (int i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));

        return vla.toArray();
    }

    /**
     * Finds the concentric areas around the cells encoded in packed, without including those cells. For each "on"
     * position in packed, expand it to cover a a square with side length equal to 1 + n * 2, where n starts at 1 and
     * goes up to include the expansions parameter, with each expansion centered on the original "on" position, unless
     * the expansion would take a cell further than 0, width - 1 (for xMove) or height - 1 (for yMove), in which case
     * that cell is stopped at the edge. If a cell is "on" in packed, it will always be "off" in the results.
     * Returns a new packed short[][] where the outer array has length equal to expansions and the inner arrays are
     * packed data encoding a one-cell-wide concentric fringe region. Uses 8-way measurement. Does not modify packed.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti()
     * @param expansions the positive (square-shaped) radius, in cells, to expand each cell out by, also the length
     *                   of the outer array returned by this method
     * @param width the maximum width; if a cell would move to x at least equal to width, it stops at width - 1
     * @param height the maximum height; if a cell would move to y at least equal to height, it stops at height - 1
     * @return an array of packed arrays that encode "on" for cells that were pushed from the edge of packed's "on"
     *          cells; the outer array will have length equal to expansions, and inner arrays will normal packed data
     */
    public static short[][] fringes(short[] packed, int expansions, int width, int height) {
        short[][] finished = new short[expansions][];
        if (packed == null || packed.length <= 1) {
            Arrays.fill(finished, ALL_WALL);
            return finished;
        }
        ShortSet ss = new ShortSet(256);
        boolean on = false;
        int idx = 0;
        short x, y, dist;
        for (int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    ss.add((short) i);
                }
            }
            idx += packed[p] & 0xffff;
        }
        for (int expansion = 1; expansion <= expansions; expansion++) {
            ShortVLA vla = new ShortVLA(256);
            on = false;
            idx = 0;
            for (int p = 0; p < packed.length; p++, on = !on) {
                if (on) {
                    for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                        x = hilbertX[i];
                        y = hilbertY[i];
                        for (int j = Math.max(0, x - expansion); j <= Math.min(width - 1, x + expansion); j++) {
                            for (int k = Math.max(0, y - expansion); k <= Math.min(height - 1, y + expansion); k++) {
                                dist = hilbertDistances[j + (k << 8)];
                                if (ss.add(dist))
                                    vla.add(dist);
                            }
                        }
                    }
                }
                idx += packed[p] & 0xffff;
            }
            int[] indices = vla.asInts();
            if(indices.length < 1)
            {
                finished[expansion - 1] = ALL_WALL;
                continue;
            }
            Arrays.sort(indices);

            vla = new ShortVLA(128);
            int current, past = indices[0], skip = 0;

            vla.add((short) indices[0]);
            for (int i = 1; i < indices.length; i++) {
                current = indices[i];
                if (current - past > 1)
                {
                    vla.add((short) (skip+1));
                    skip = 0;
                    vla.add((short)(current - past - 1));
                }
                else if(current != past)
                    skip++;
                past = current;
            }
            vla.add((short) (skip + 1));

            finished[expansion-1] = vla.toArray();
        }
        return finished;
    }


    /**
     * Finds the concentric areas around the cells encoded in packed, without including those cells. For each "on"
     * position in packed, expand it to cover a a square or diamond with radius equal to n, where n starts at 1 and
     * goes up to include the expansions parameter, with each expansion centered on the original "on" position, unless
     * the expansion would take a cell further than 0, width - 1 (for xMove) or height - 1 (for yMove), in which case
     * that cell is stopped at the edge. If a cell is "on" in packed, it will always be "off" in the results.
     * Returns a new packed short[][] where the outer array has length equal to expansions and the inner arrays are
     * packed data encoding a one-cell-wide concentric fringe region. Does not modify packed.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti()
     * @param expansions the positive (square-shaped) radius, in cells, to expand each cell out by, also the length
     *                   of the outer array returned by this method
     * @param width the maximum width; if a cell would move to x at least equal to width, it stops at width - 1
     * @param height the maximum height; if a cell would move to y at least equal to height, it stops at height - 1
     * @param eightWay true if the expansion should be both diagonal and orthogonal; false for just orthogonal
     * @return an array of packed arrays that encode "on" for cells that were pushed from the edge of packed's "on"
     *          cells; the outer array will have length equal to expansions, and inner arrays will normal packed data
     */
    public static short[][] fringes(short[] packed, int expansions, int width, int height, boolean eightWay) {
        if(eightWay)
            return fringes(packed, expansions, width, height);
        short[][] finished = new short[expansions][];
        if (packed == null || packed.length <= 1) {
            Arrays.fill(finished, ALL_WALL);
            return finished;
        }
        ShortSet ss = new ShortSet(256);
        boolean on = false;
        int idx = 0;
        short x, y, dist;
        int[] xOffsets = new int[]{0, 1, 0, -1, 0}, yOffsets = new int[]{1, 0, -1, 0, 1};
        for (int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    ss.add((short) i);
                }
            }
            idx += packed[p] & 0xffff;
        }
        for (int expansion = 1; expansion <= expansions; expansion++) {
            ShortVLA vla = new ShortVLA(256);
            on = false;
            idx = 0;
            for (int p = 0; p < packed.length; p++, on = !on) {
                if (on) {
                    for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                        x = hilbertX[i];
                        y = hilbertY[i];
                        for (int d = 0; d < 4; d++) {
                            for (int e = 1; e <= expansion; e++) {
                                for (int e2 = 0; e2 <= expansion - e; e2++) {
                                    int j = Math.min(width - 1, Math.max(0, x + xOffsets[d] * e + yOffsets[d + 1] * e2));
                                    int k = Math.min(height - 1, Math.max(0, y + yOffsets[d] * e + xOffsets[d + 1] * e2));
                                    dist = hilbertDistances[j + (k << 8)];
                                    if (ss.add(dist))
                                        vla.add(dist);
                                }
                            }
                        }
                    }
                }
                idx += packed[p] & 0xffff;
            }
            int[] indices = vla.asInts();
            if(indices.length < 1)
            {
                finished[expansion - 1] = ALL_WALL;
                continue;
            }
            Arrays.sort(indices);

            vla = new ShortVLA(128);
            int current, past = indices[0], skip = 0;

            vla.add((short) indices[0]);
            for (int i = 1; i < indices.length; i++) {
                current = indices[i];
                if (current - past > 1)
                {
                    vla.add((short) (skip+1));
                    skip = 0;
                    vla.add((short)(current - past - 1));
                }
                else if(current != past)
                    skip++;
                past = current;
            }
            vla.add((short) (skip + 1));

            finished[expansion-1] = vla.toArray();
        }
        return finished;
    }

    /**
     * Finds the area consisting of the "on" positions in packed that are within the specified depth distance of an
     * "off" position or the edge of the map. This essentially finds the part of packed that is close to its edge.
     * Uses 8-way movement (Chebyshev distance) unless the overload of this function that takes a boolean argument
     * eightWay is used and that argument is false.
     * Returns a new packed short[] and does not modify packed.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti()
     * @param depth the positive (square) radius, in cells, to go inward from an "off" cell into the "in" cells
     * @param width the maximum width; if a cell would move to x at least equal to width, it stops at width - 1
     * @param height the maximum height; if a cell would move to y at least equal to height, it stops at height - 1
     * @return a short[] that encodes "on" for cells that were "on" in packed and were close enough to an "off" cell
     */
    public static short[] surface(short[] packed, int depth, int width, int height)
    {
        return intersectPacked(packed, expand(negatePacked(packed), depth, width, height, true));
    }
    /*{
        if(packed == null || packed.length <= 1)
        {
            return ALL_WALL;
        }
        ShortVLA vla = new ShortVLA(256);
        boolean on = false;
        int idx = 0, x, y;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                INDICES:
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++)
                {
                    x = hilbertX[i];
                    y = hilbertY[i];
                    for (int j = Math.max(0, x - depth); j <= Math.min(width - 1, x + depth); j++) {
                        for (int k = Math.max(0, y - depth); k <= Math.min(height - 1, y + depth); k++) {
                            if(!queryPackedHilbert(packed, hilbertDistances[j + (k << 8)]))
                            {
                                vla.add((short)i);
                                continue INDICES;
                            }
                        }
                    }
                }
            }
            idx += packed[p] & 0xffff;
        }

        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);

        vla = new ShortVLA(128);
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (int i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));
        return vla.toArray();
    }
    */
    /**
     * Finds the area consisting of the "on" positions in packed that are within the specified depth distance of an
     * "off" position or the edge of the map. This essentially finds the part of packed that is close to its edge.
     * Returns a new packed short[] and does not modify packed.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti()
     * @param depth the positive (square) radius, in cells, to go inward from an "off" cell into the "in" cells
     * @param width the maximum width; if a cell would move to x at least equal to width, it stops at width - 1
     * @param height the maximum height; if a cell would move to y at least equal to height, it stops at height - 1
     * @param eightWay true if the retraction should be both diagonal and orthogonal; false for just orthogonal
     * @return a short[] that encodes "on" for cells that were "on" in packed and were close enough to an "off" cell
     */
    public static short[] surface(short[] packed, int depth, int width, int height, boolean eightWay)
    {
        return intersectPacked(packed, expand(negatePacked(packed), depth, width, height, eightWay));
    }
    /**
     * Finds the concentric, progressively-smaller surfaces of packed as if packed was shrinking with each iteration.
     * Essentially, this is the inverse of fringes, where fringe finds a ring around packed and fringes finds concentric
     * rings around growing versions of packed, while surface finds a ring at the edge and surfaces finds rings at the
     * edge of shrinking versions of packed.
     * Returns a new packed short[] and does not modify packed.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti()
     * @param depth the positive (square) radius, in cells, to go inward from an "off" cell into the "in" cells
     * @param width the maximum width; if a cell would move to x at least equal to width, it stops at width - 1
     * @param height the maximum height; if a cell would move to y at least equal to height, it stops at height - 1
     * @return an array of packed short[] that each encodes "on" for cells that were "on" in packed and were at a
     * distance between 1 and depth to an "off" cell
     */
    public static short[][] surfaces(short[] packed, int depth, int width, int height)
    {
        short[][] sfs = new short[depth][], frs = fringes(negatePacked(packed), depth, width, height);
        for (int i = 0; i < depth; i++) {
            sfs[i] = intersectPacked(packed, frs[i]);
        }
        return sfs;
    }
    /**
     * Finds the concentric, progressively-smaller surfaces of packed as if packed was shrinking with each iteration.
     * Essentially, this is the inverse of fringes, where fringe finds a ring around packed and fringes finds concentric
     * rings around growing versions of packed, while surface finds a ring at the edge and surfaces finds rings at the
     * edge of shrinking versions of packed.
     * Returns a new packed short[] and does not modify packed.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti()
     * @param depth the positive (square) radius, in cells, to go inward from an "off" cell into the "in" cells
     * @param width the maximum width; if a cell would move to x at least equal to width, it stops at width - 1
     * @param height the maximum height; if a cell would move to y at least equal to height, it stops at height - 1
     * @param eightWay true if the retraction should be both diagonal and orthogonal; false for just orthogonal
     * @return an array of packed short[] that each encodes "on" for cells that were "on" in packed and were at a
     * distance between 1 and depth to an "off" cell
     */
    public static short[][] surfaces(short[] packed, int depth, int width, int height, boolean eightWay)
    {
        short[][] sfs = new short[depth][], frs = fringes(negatePacked(packed), depth, width, height, eightWay);
        for (int i = 0; i < depth; i++) {
            sfs[i] = intersectPacked(packed, frs[i]);
        }
        return sfs;
    }

    /*{
        if(eightWay)
            return surface(packed, depth, width, height);
        if(packed == null || packed.length <= 1)
        {
            return ALL_WALL;
        }
        ShortVLA vla = new ShortVLA(256);
        boolean on = false;
        int idx = 0, x, y, j, k;
        int[] xOffsets = new int[]{0, 1, 0, -1, 0}, yOffsets = new int[]{1, 0, -1, 0, 1};
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                INDICES:
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++)
                {
                    x = hilbertX[i];
                    y = hilbertY[i];
                    for (int d = 0; d < 4; d++) {
                        for (int e = 1; e <= depth; e++) {
                            for (int e2 = 0; e2 < depth; e2++) {
                                j = x + xOffsets[d] * e + yOffsets[d + 1] * e2;
                                k = y + yOffsets[d] * e + xOffsets[d + 1] * e2;
                                if (j < 0 || k < 0 || j >= width || k >= height ||
                                        !queryPackedHilbert(packed, hilbertDistances[j + (k << 8)])) {
                                    vla.add((short)i);
                                    continue INDICES;
                                }
                            }
                        }
                    }
                }
            }
            idx += packed[p] & 0xffff;
        }

        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);

        vla = new ShortVLA(128);
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (int i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));
        return vla.toArray();
    }*/


    /**
     * Given a packed array encoding a larger area, a packed array encoding one or more points inside bounds, and an
     * amount of expansion, expands each cell in start by a Manhattan (diamond) radius equal to expansion, limiting any
     * expansion to within bounds and returning the final expanded (limited) packed data.  Notably, if a small area is
     * not present within bounds, then the flood will move around the "hole" similarly to DijkstraMap's behavior;
     * essentially, it needs to expand around the hole to get to the other side, and this takes more steps of expansion
     * than crossing straight over.
     * Returns a new packed short[] and does not modify bounds or start.
     * @param bounds packed data representing the maximum extent of the region to flood-fill; often floors
     * @param start a packed array that encodes position(s) that the flood will spread outward from
     * @param expansion the positive (square) radius, in cells, to expand each cell out by
     * @return a packed array that encodes "on" for cells that are "on" in bounds and are within expansion Manhattan
     * distance from a Coord in start
     */
    public static short[] flood(short[] bounds, short[] start, int expansion)
    {
        if(bounds == null || bounds.length <= 1)
        {
            return ALL_WALL;
        }
        int boundSize = count(bounds);
        ShortVLA vla = new ShortVLA(256);
        ShortSet ss = new ShortSet(boundSize), quickBounds = new ShortSet(boundSize);
        boolean on = false, justAdded;
        int idx = 0;
        short x, y, dist;
        for(int p = 0; p < bounds.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (bounds[p] & 0xffff); i++) {
                    quickBounds.add((short) i);
                }
            }
            idx += bounds[p] & 0xffff;
        }
        short[] s2 = allPackedHilbert(start);
        ss.addAll(s2);
        vla.addAll(s2);
        int[] xOffsets = new int[]{0, 1, 0, -1}, yOffsets = new int[]{1, 0, -1, 0};
        for (int e = 0; e < expansion; e++) {
            justAdded = false;
            ShortVLA edge = new ShortVLA(128);
            for (int s = 0; s < s2.length; s++) {
                int i = s2[s] & 0xffff;
                x = hilbertX[i];
                y = hilbertY[i];

                for (int d = 0; d < 4; d++) {
                    int j = Math.min(255, Math.max(0, x + xOffsets[d]));
                    int k = Math.min(255, Math.max(0, y + yOffsets[d]));
                    dist = hilbertDistances[j + (k << 8)];
                    if (quickBounds.contains(dist)) {
                        if (ss.add(dist)) {
                            vla.add(dist);
                            edge.add(dist);
                            justAdded = true;
                        }
                    }
                }
            }
            if(!justAdded)
                break;
            s2 = edge.toArray();
        }

        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);

        vla = new ShortVLA(128);
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (int i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));

        return vla.toArray();
    }


    /**
     * Given a packed array encoding a larger area, a packed array encoding one or more points inside bounds, and an
     * amount of expansion, expands each cell in start by a radius (if eightWay is true, it uses Chebyshev distance; if
     * it is false, it uses Manhattan distance) equal to expansion, limiting any expansion to within bounds and
     * returning the final expanded (limited) packed data. Notably, if a small area is not present within bounds, then
     * the flood will move around the "hole" similarly to DijkstraMap's behavior; essentially, it needs to expand around
     * the hole to get to the other side, and this takes more steps of expansion than crossing straight over.
     * Returns a new packed short[] and does not modify bounds or start.
     * @param bounds packed data representing the maximum extent of the region to flood-fill; often floors
     * @param start a packed array that encodes position(s) that the flood will spread outward from
     * @param expansion the positive (square) radius, in cells, to expand each cell out by
     * @param eightWay true to flood-fill out in all eight directions at each step, false for just orthogonal
     * @return a packed array that encodes "on" for cells that are "on" in bounds and are within expansion either
     * Chebyshev (if eightWay is true) or Manhattan (otherwise) distance from a Coord in start
     */
    public static short[] flood(short[] bounds, short[] start, int expansion, boolean eightWay)
    {
        if(!eightWay)
            return flood(bounds, start, expansion);
        if(bounds == null || bounds.length <= 1)
        {
            return ALL_WALL;
        }
        int boundSize = count(bounds);
        ShortVLA vla = new ShortVLA(256);
        ShortSet ss = new ShortSet(boundSize), quickBounds = new ShortSet(boundSize);
        boolean on = false, justAdded;
        int idx = 0;
        short x, y, dist;
        for(int p = 0; p < bounds.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (bounds[p] & 0xffff); i++) {
                    quickBounds.add((short) i);
                }
            }
            idx += bounds[p] & 0xffff;
        }
        short[] s2 = allPackedHilbert(start);
        ss.addAll(s2);
        vla.addAll(s2);
        int[] xOffsets = new int[]{-1, 0, 1, -1,    1, -1, 0, 1}, yOffsets = new int[]{-1, -1, -1, 0,    0, 1, 1, 1};
        for (int e = 0; e < expansion; e++) {
            justAdded = false;
            ShortVLA edge = new ShortVLA(128);
            for (int s = 0; s < s2.length; s++) {
                int i = s2[s] & 0xffff;
                x = hilbertX[i];
                y = hilbertY[i];
                for (int d = 0; d < 8; d++) {
                    int j = Math.min(255, Math.max(0, x + xOffsets[d]));
                    int k = Math.min(255, Math.max(0, y + yOffsets[d]));
                    dist = hilbertDistances[j + (k << 8)];
                    if (quickBounds.contains(dist)) {
                        if (ss.add(dist)) {
                            vla.add(dist);
                            edge.add(dist);
                            justAdded = true;
                        }
                    }
                }
            }
            if(!justAdded)
                break;
            s2 = edge.toArray();
        }

        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);

        vla = new ShortVLA(128);
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (int i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));

        return vla.toArray();
    }


    /**
     * Given a packed array encoding a larger area, a packed array encoding one or more points inside bounds, an RNG,
     * and a volume in cells, expands a random cell in start in a random Manhattan (diamond) direction equal, then
     * continues to expand from random cells in start or the expanded area until it has filled volume cells, limiting
     * any expansion to within bounds and returning the final expanded (limited) packed data.  Notably, if a small area
     * is not present within bounds, then the spill will move around the "hole" similarly to DijkstraMap's behavior;
     * essentially, it needs to expand around the hole to get to the other side, and this takes more steps of expansion
     * than crossing straight over.
     * <br>
     * Could also be given a name like randomizedFlood(), but spill() is used by the Spill class that does this too.
     * <br>
     * Returns a new packed short[] and does not modify bounds or start.
     * @param bounds packed data representing the maximum extent of the region to random-flood-fill; often floors
     * @param start a packed array that encodes position(s) that the random-flood will spread outward from
     * @param volume the total number of cells to try to fill
     * @param rng used to generate random numbers for the flooding
     * @return a packed array that encodes "on" for cells that are "on" in bounds and are within expansion Manhattan
     * distance from a Coord in start
     */
    public static short[] spill(short[] bounds, short[] start, int volume, RNG rng)
    {
        if(bounds == null || bounds.length <= 1)
        {
            return ALL_WALL;
        }
        int boundSize = count(bounds);
        ShortVLA vla = new ShortVLA(256);
        ShortSet ss = new ShortSet(boundSize), edge = new ShortSet(boundSize), quickBounds = new ShortSet(boundSize);
        boolean on = false, justAdded;
        int idx = 0;
        short x, y, dist;
        for(int p = 0; p < bounds.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (bounds[p] & 0xffff); i++) {
                    quickBounds.add((short) i);
                }
            }
            idx += bounds[p] & 0xffff;
        }
        short[] s2 = allPackedHilbert(start);
        int ct = s2.length;
        ss.addAll(s2);
        vla.addAll(s2);
        edge.addAll(allPackedHilbert(intersectPacked(bounds, fringe(start, 1, 256, 256, false))));
        ss.addAll(edge);
        if(edge.size <= 0)
        {
            if(!intersects(bounds, start))
                return ALL_WALL;

            short[] cpy = new short[start.length];
            System.arraycopy(start, 0, cpy, 0, start.length);
            return cpy;
        }
        int[] xOffsets = new int[]{0, 1, 0, -1}, yOffsets = new int[]{1, 0, -1, 0};
        for (int v = ct; v < volume; v++) {
            short s = rng.getRandomElement(edge);

            edge.remove(s);
            vla.add(s);
            int i = s & 0xffff;
            x = hilbertX[i];
            y = hilbertY[i];

            for (int d = 0; d < 4; d++) {
                int j = Math.min(255, Math.max(0, x + xOffsets[d]));
                int k = Math.min(255, Math.max(0, y + yOffsets[d]));
                dist = hilbertDistances[j + (k << 8)];
                if (quickBounds.contains(dist)) {
                    if (ss.add(dist)) {
                        edge.add(dist);
                    }
                }
            }

            if(edge.size <= 0)
                break;
        }

        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);

        vla.clear();
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (int i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));

        return vla.toArray();
    }

    private static void modifiedShadowFOV(int expansion, int viewerX, int viewerY, Radius metric, ShortSet bounds, ShortSet storedSet, ShortVLA vla)
    {
        if(expansion < 1)
            return;
        short start = hilbertDistances[viewerX + (viewerY << 8)];
        if(storedSet.add(start))
            vla.add(start);

        for (Direction d : Direction.DIAGONALS) {
            modifiedShadowCast(expansion, 1, 1.0, 0.0, 0, d.deltaX, d.deltaY, 0, viewerX, viewerY, metric, bounds, storedSet, vla);
            modifiedShadowCast(expansion, 1, 1.0, 0.0, d.deltaX, 0, 0, d.deltaY, viewerX, viewerY, metric, bounds, storedSet, vla);
        }
    }

    private static void modifiedShadowCast(int expansion, int row, double start, double end, int xx, int xy, int yx, int yy,
                                     int viewerX, int viewerY, Radius metric, ShortSet bounds, ShortSet storedSet, ShortVLA vla) {
        double newStart = 0;
        if (start < end) {
            return;
        }

        boolean blocked = false;
        int dist;
        short currentPos;
        for (int distance = row; distance <= expansion && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = viewerX + deltaX * xx + deltaY * xy;
                int currentY = viewerY + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                double rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);
                currentPos = hilbertDistances[currentX + (currentY << 8)];

                /*
                if (!bounds.contains(currentPos)) {
                    newStart = rightSlope;
                    continue;
                }
                else
                 */
                if(!(currentX - viewerX + expansion >= 0 && currentX - viewerX <= expansion
                        && currentY - viewerY + expansion >= 0 && currentY - viewerY <= expansion)
                        || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }

                if (blocked) { //previous cell was a blocking one
                    if (!bounds.contains(currentPos)) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                        dist = metric.roughDistance(currentX - viewerX, currentY - viewerY);
                        //check if it's within the lightable area and light if needed
                        if (dist <= expansion * 2) {
                            if(storedSet.add(currentPos))
                                vla.add(currentPos);
                        }
                    }
                } else {
                    if (!bounds.contains(currentPos) && distance < expansion) {//hit a wall within sight line
                        blocked = true;
                        modifiedShadowCast(expansion, distance + 1, start, leftSlope, xx, xy, yx, yy, viewerX, viewerY, metric, bounds, storedSet, vla);
                        newStart = rightSlope;
                    }
                    else
                    {
                        if(bounds.contains(currentPos)) {
                            dist = metric.roughDistance(currentX - viewerX, currentY - viewerY);
                            //check if it's within the lightable area and light if needed
                            if (dist <= expansion * 2) {
                                if (storedSet.add(currentPos))
                                    vla.add(currentPos);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Given a packed array encoding a larger area, a packed array encoding one or more points inside bounds, and an
     * amount of expansion, expands each cell in start by a Manhattan (diamond) radius equal to expansion, limiting any
     * expansion to within bounds and returning the final expanded (limited) packed data.
     * Though this is otherwise similar to flood(), radiate() behaves like FOV and will not move around obstacles and
     * will instead avoid expanding if it would go into any cell that cannot be reached by a straight line (drawn
     * directly, not in grid steps) that is mostly unobstructed.
     * Returns a new packed short[] and does not modify bounds or start.
     * @param bounds packed data representing the maximum extent of the region to flood-fill; often floors
     * @param start a packed array that encodes position(s) that the flood will spread outward from
     * @param expansion the positive (square) radius, in cells, to expand each cell out by
     * @return a packed array that encodes "on" for cells that are "on" in bounds and are within expansion Manhattan
     * distance from a Coord in start
     */
    public static short[] radiate(short[] bounds, short[] start, int expansion)
    {
        return radiate(bounds, start, expansion, Radius.DIAMOND);
    }
    /**
     * Given a packed array encoding a larger area, a packed array encoding one or more points inside bounds, and an
     * amount of expansion, expands each cell in start by a radius, with a shape determined by metric, equal to
     * expansion, limiting any expansion to within bounds and returning the final expanded (limited) packed data.
     * Though this is otherwise similar to flood(), radiate() behaves like FOV and will not move around obstacles and
     * will instead avoid expanding if it would go into any cell that cannot be reached by a straight line (drawn
     * directly, not in grid steps) that is mostly unobstructed.
     * Returns a new packed short[] and does not modify bounds or start.
     * @param bounds packed data representing the maximum extent of the region to flood-fill; often floors
     * @param start a packed array that encodes position(s) that the flood will spread outward from
     * @param expansion the positive (square) radius, in cells, to expand each cell out by
     * @param metric a Radius that defines how this should expand, SQUARE for 8-way, DIAMOND for 4-way, CIRCLE for
     *               Euclidean expansion (not guaranteed to be perfectly circular)
     * @return a packed array that encodes "on" for cells that are "on" in bounds and are within expansion Manhattan
     * distance from a Coord in start
     */
    public static short[] radiate(short[] bounds, short[] start, int expansion, Radius metric)
    {
        if(bounds == null || bounds.length <= 1)
        {
            return ALL_WALL;
        }
        int boundSize = count(bounds);
        ShortVLA vla = new ShortVLA(256);
        ShortSet storedSet = new ShortSet(boundSize), quickBounds = new ShortSet(boundSize);
        boolean on = false;
        int idx = 0, i;
        short x, y;
        for(int p = 0; p < bounds.length; p++, on = !on) {
            if (on) {
                for (i = idx; i < idx + (bounds[p] & 0xffff); i++) {
                    quickBounds.add((short) i);
                }
            }
            idx += bounds[p] & 0xffff;
        }
        short[] s2 = allPackedHilbert(start);
        for (int s = 0; s < s2.length; s++) {
            i = s2[s] & 0xffff;
            x = hilbertX[i];
            y = hilbertY[i];

            modifiedShadowFOV(expansion, x, y, metric, quickBounds, storedSet, vla);
        }

        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);

        vla = new ShortVLA(128);
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));

        return vla.toArray();
    }


    /**
     * Given a packed array encoding a larger area, a packed array encoding one or more points inside bounds, and an
     * amount of expansion, expands each cell in start by a radius, with a square shape if eightWay is true or a diamond
     * otherwise, equal to expansion, limiting any expansion to within bounds and returning the final expanded (limited)
     * packed data. Though this is otherwise similar to flood(), radiate() behaves like FOV and will not move around
     * obstacles and will instead avoid expanding if it would go into any cell that cannot be reached by a straight line
     * (drawn directly, not in grid steps) that is mostly unobstructed.
     * Returns a new packed short[] and does not modify bounds or start.
     * @param bounds packed data representing the maximum extent of the region to flood-fill; often floors
     * @param start a packed array that encodes position(s) that the flood will spread outward from
     * @param expansion the positive (square) radius, in cells, to expand each cell out by
     * @param eightWay true to flood-fill out in all eight directions at each step, false for just orthogonal
     * @return a packed array that encodes "on" for cells that are "on" in bounds and are within expansion either
     * Chebyshev (if eightWay is true) or Manhattan (otherwise) distance from a Coord in start
     */
    public static short[] radiate(short[] bounds, short[] start, int expansion, boolean eightWay)
    {
        if(eightWay)
            return radiate(bounds, start, expansion, Radius.SQUARE);
        return radiate(bounds, start, expansion, Radius.DIAMOND);
    }

    /**
     * Given a packed array encoding a larger area, a packed array encoding one or more points inside bounds, and a
     * Reach object that determines targeting constraints, gets all cells contained within bounds that can be targeted
     * from a cell in start using the rules defined by reach.
     * Though this is otherwise similar to flood(), reachable() behaves like FOV and will not move around obstacles and
     * will instead avoid expanding if it would go into any cell that cannot be reached by a straight line (drawn
     * directly, not in grid steps) that is mostly unobstructed. This does not behave quite like FOV if an AimLimit has
     * been set in reach to any value other than null or AimLimit.FREE; in these cases it requires an exactly straight
     * orthogonal or diagonal line without obstructions, checking only cells along the precise path. For diagonals and
     * eight-way targeting, this means it can target through walls that only meet at a perpendicular diagonal, such as
     * an X shape where one line is a one-cell-thick diagonal wall and the other is the targeting line. This is normally
     * only allowed in some games and only if they use Chebyshev (Radius.SQUARE) distance, so be advised that it may not
     * be desirable behavior.
     * Returns a new packed short[] and does not modify bounds or start.
     * @param bounds packed data representing the max extent of the region to check for reach-ability; often floors
     * @param start a packed array that encodes position(s) that the flood will spread outward from
     * @param reach a {@link Reach} object that determines minimum and maximum range, distance metric, and AimLimit
     * @return a packed array that encodes "on" for cells that are "on" in bounds and can be targeted from a cell in
     * start using the given Reach
     */
    public static short[] reachable(short[] bounds, short[] start, Reach reach)
    {
        if(bounds == null || bounds.length <= 1)
        {
            return ALL_WALL;
        }
        int boundSize = count(bounds);
        ShortVLA vla = new ShortVLA(256), discard = new ShortVLA(128);
        ShortSet storedSet = new ShortSet(boundSize), quickBounds = new ShortSet(boundSize);
        boolean on = false;
        int idx = 0, i;
        short x, y;
        for(int p = 0; p < bounds.length; p++, on = !on) {
            if (on) {
                for (i = idx; i < idx + (bounds[p] & 0xffff); i++) {
                    quickBounds.add((short) i);
                }
            }
            idx += bounds[p] & 0xffff;
        }
        short[] s2 = allPackedHilbert(start);
        if(reach.limit == null || reach.limit == AimLimit.FREE) {
            for (int s = 0; s < s2.length; s++) {
                i = s2[s] & 0xffff;
                x = hilbertX[i];
                y = hilbertY[i];
                //add all cells at less than minimum distance to storedSet.
                modifiedShadowFOV(reach.minDistance - 1, x, y, reach.metric, quickBounds, storedSet, discard);
                discard.clear();
                modifiedShadowFOV(reach.maxDistance, x, y, reach.metric, quickBounds, storedSet, vla);
            }
        }
        else
        {
            for (int s = 0; s < s2.length; s++) {
                i = s2[s] & 0xffff;
                x = hilbertX[i];
                y = hilbertY[i];
                Direction[] dirs;
                switch (reach.limit)
                {
                    case ORTHOGONAL: dirs = Direction.CARDINALS;
                        break;
                    case DIAGONAL: dirs = Direction.DIAGONALS;
                        break;
                    default: dirs = Direction.OUTWARDS;
                }
                Direction dir;
                DIRECTIONAL:
                for (int which = 0; which < dirs.length; which++) {
                    dir = dirs[which];
                    int d;
                    //add all cells at less than minimum distance to storedSet.
                    for (d = 1; d < reach.minDistance; d++) {
                        int extended = (x + dir.deltaX * d) + ((y + dir.deltaY * d) << 8);
                        if (extended < 0 || extended > 0xffff)
                            continue DIRECTIONAL;
                        short next = hilbertDistances[extended];
                        if (quickBounds.contains(next))
                            storedSet.add(next);
                        else
                            continue DIRECTIONAL;
                    }
                    for (; d <= reach.maxDistance; d++) {
                        int extended = (x + dir.deltaX * d) + ((y + dir.deltaY * d) << 8);
                        if (extended < 0 || extended > 0xffff)
                            continue DIRECTIONAL;
                        short next = hilbertDistances[extended];
                        if (quickBounds.contains(next)) {
                            if (storedSet.add(next))
                                vla.add(next);
                        }
                        else
                            continue DIRECTIONAL;
                    }
                }
            }
        }
        int[] indices = vla.asInts();
        if(indices.length < 1)
            return ALL_WALL;
        Arrays.sort(indices);

        vla = new ShortVLA(128);
        int current, past = indices[0], skip = 0;

        vla.add((short)indices[0]);
        for (i = 1; i < indices.length; i++) {
            current = indices[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));

        return vla.toArray();
    }
    /**
     * Given a width and height, returns a packed array that encodes "on" for the rectangle from (0,0) to
     * (width - 1, height - 1). Primarily useful with intersectPacked() to ensure things like negatePacked() that can
     * encode "on" cells in any position are instead limited to the bounds of the map.
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @return a packed short[] encoding "on" for all cells with x less than width and y less than height.
     */
    public static short[] rectangle(int width, int height)
    {
        if(width > 256 || height > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently pack, aborting");
        boolean[][] rect = new boolean[width][height];
        for (int i = 0; i < width; i++) {
            Arrays.fill(rect[i], true);
        }
        return pack(rect);
    }
    /**
     * Given x, y, width and height, returns a packed array that encodes "on" for the rectangle from (x,y) to
     * (width + x - 1, height + y - 1). Primarily useful with intersectPacked() to ensure things like negatePacked() that
     * can encode "on" cells in any position are instead limited to the bounds of the map, but also handy for basic "box
     * drawing" for other uses.
     * @param x the minimum x coordinate
     * @param y the minimum y coordinate
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @return a packed short[] encoding "on" for all cells between (x,y) and (x+width-1,y+height-1).
     */
    public static short[] rectangle(int x, int y, int width, int height)
    {
        int width2 = width, height2 = height;
        if(x + width >= 256)
            width2 = 255 - x;
        if(y + height >= 256)
            height2 = 255 - y;
        if(width2 < 0 || height2 < 0 || x < 0 || y < 0)
            return ALL_WALL;
        boolean[][] rect = new boolean[x + width2][y + height2];
        for (int i = x; i < x + width2; i++) {
            Arrays.fill(rect[i], y, y + height2, true);
        }
        return pack(rect);
    }
    /**
     * Given x, y, width and height, returns an array of all Hilbert distance within the rectangle from (x,y) to
     * (width + x - 1, height + y - 1).
     * @param x the minimum x coordinate
     * @param y the minimum y coordinate
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @return a short[] that is not packed, and instead stores individual Hilbert distances in the rectangle
     */
    public static short[] rectangleHilbert(int x, int y, int width, int height)
    {
        int width2 = width, height2 = height;
        if(x + width >= 256)
            width2 = 256 - x;
        if(y + height >= 256)
            height2 = 256 - y;
        if(width2 <= 0 || height2 <= 0 || x < 0 || y < 0)
            return new short[0];
        short[] hilberts = new short[width2 * height2];
        int idx = 0;
        for (int i = x; i < x + width2; i++) {
            for (int j = y; j < y + height2; j++) {
                hilberts[idx++] = hilbertDistances[i + (j << 8)];
            }
        }
        return hilberts;
    }

    /**
     * Given a center and radius for a circle, plus the width and height for the map boundaries, returns the packed data
     * that encodes the circle.
     * @param center center position of the circle
     * @param radius radius to extend in all directions from center
     * @param width the maximum width of the map (exclusive); the circle will not extend past this or below 0
     * @param height the maximum height of the map (exclusive); the circle will not extend past this or below 0
     * @return a packed short[] encoding "on" for all elements inside the circle
     */
    public static short[] circle(Coord center, int radius, int width, int height)
    {
        return packSeveral(Radius.CIRCLE.pointsInside(center, radius, false, width, height));
    }
    /**
     * Counts the number of "on" cells encoded in a packed array without unpacking it.
     * @param packed a packed short array, as produced by pack()
     * @return the number of "on" cells.
     */
    public static int count(short[] packed)
    {
        return count(packed, true);
    }

    /**
     * Counts the number of cells encoding a boolean equal to wanted in a packed array without unpacking it.
     * @param packed a packed short array, as produced by pack()
     * @param wanted the boolean you want to count, true for "on" and false for "off"
     * @return the number of cells that encode a value equal to wanted.
     */
    public static int count(short[] packed, boolean wanted)
    {
        int c = 0;
        boolean on = false;
        for (int i = 0; i < packed.length; i++, on = !on) {
            if(on == wanted)
                c += packed[i] & 0xffff;
        }
        return c;
    }
    /**
     * Finds how many cells are encoded in a packed array (both on and off) without unpacking it.
     * @param packed a packed short array, as produced by pack()
     * @return the number of cells that are encoded explicitly in the packed data as either on or off.
     */
    public static int covered(short[] packed)
    {
        int c = 0;
        for (int i = 0; i < packed.length; i++) {
            c += packed[i] & 0xffff;
        }
        return c;
    }

    /**
     * Finds the minimum bounding rectangle for a packed array without unpacking it. Returns a Coord array with 2
     * elements: the minimum-x/minimum-y Coord corner of the bounds, then the maximum-x/maximum-y Coord corner. Both
     * array elements will be the Coord (-1,-1) if the packed array does not encode any "on" values (all empty).
     * @param packed a packed short array, as produced by pack()
     * @return a 2-element Coord array starting with the bounds' minimum corner and followed by the maximum corner
     */
    public static Coord[] bounds(short[] packed)
    {
        Coord[] c = new Coord[]{Coord.get(-1,-1), Coord.get(-1,-1)};
        boolean on = false;
        int idx = 0, min_x = 256, min_y = 256, max_x = -1, max_y = -1, x, y;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    x = hilbertX[i];
                    y = hilbertY[i];
                    if(min_x > x)
                        min_x = x;
                    if(min_y > y)
                        min_y = y;
                    if(max_x < x)
                        max_x = x;
                    if(max_y < y)
                        max_y = y;
                }
            }
            idx += packed[p] & 0xffff;
        }
        if(min_x < 256) {
            c[0] = Coord.get(min_x, min_y);
            c[1] = Coord.get(max_x, max_y);
        }
        return c;
    }

    /**
     * Given a 2D char array for a map, a piece of packed data defining a region to use from that map, and a filler
     * char, produces a 2D char array where all positions that are "off" in packed are filled with filler, and the rest
     * are the same as in map.
     * @param map a 2D char array that will not be modified
     * @param packed a packed short array, as produced by pack()
     * @param filler the char to use for "off" positions in packed
     * @return a 2D char array similar to map but with any "off" positions in packed replaced with filler
     */
    public static char[][] mask(char[][] map, short[] packed, char filler)
    {
        if(map.length <= 0)
            return map;
        char[][] c = new char[map.length][map[0].length];
        for (int i = 0; i < map.length; i++) {
            Arrays.fill(c[i], filler);
        }
        boolean on = false;
        int idx = 0, x, y;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    x = hilbertX[i];
                    y = hilbertY[i];
                    if(x >= map.length || y >= map[x].length)
                        continue;
                    c[x][y] = map[x][y];
                }
            }
            idx += packed[p] & 0xffff;
        }
        return c;
    }

    /**
     * Given two packed short arrays, left and right, this produces a packed short array that encodes "on" for any cell
     * that was "on" in either left or in right, and only encodes "off" for cells that were off in both. This method
     * does not do any unpacking (which can be somewhat computationally expensive) and so should be strongly preferred
     * when merging two pieces of packed data.
     * @param left A packed array such as one produced by pack()
     * @param right A packed array such as one produced by pack()
     * @return A packed array that encodes "on" for all cells that were "on" in either left or right
     */
    public static short[] unionPacked(short[] left, short[] right)
    {
        if(left.length == 0)
            return right;
        if(right.length == 0)
            return left;
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, onLeft = false, onRight = false;
        int idx = 0, skip = 0, elemLeft = 0, elemRight = 0, totalLeft = 0, totalRight = 0;
        while ((elemLeft < left.length || elemRight < right.length) && idx <= 0xffff) {
            if (elemLeft >= left.length) {
                totalLeft = 0x20000;
                onLeft = false;
            }
            else if(totalLeft <= idx) {
                totalLeft += left[elemLeft] & 0xffff;
            }
            if(elemRight >= right.length) {
                totalRight = 0x20000;
                onRight = false;
            }
            else if(totalRight <= idx) {
                totalRight += right[elemRight] & 0xffff;
            }
            // 300, 5, 6, 8, 2, 4
            // 290, 12, 9, 1
            // =
            // 290, 15, 6, 8, 2, 4
            // 290 off in both, 10 in right, 2 in both, 3 in left, 6 off in both, 1 on in both, 7 on in left, 2 off in
            //     both, 4 on in left
            if(totalLeft < totalRight)
            {
                onLeft = !onLeft;
                skip += totalLeft - idx;
                idx = totalLeft;
                if(on != (onLeft || onRight)) {
                    packing.add((short) skip);
                    skip = 0;
                    on = !on;
                }
                elemLeft++;
            }
            else if(totalLeft == totalRight)
            {
                onLeft = !onLeft;
                onRight = !onRight;
                skip += totalLeft - idx;
                idx = totalLeft;
                if(on != (onLeft || onRight)) {
                    packing.add((short) skip);
                    skip = 0;
                    on = !on;
                }
                elemLeft++;
                elemRight++;

            }
            else
            {
                onRight = !onRight;
                skip += totalRight - idx;
                idx = totalRight;
                if(on != (onLeft || onRight)) {
                    packing.add((short) skip);
                    skip = 0;
                    on = !on;
                }
                elemRight++;
            }
        }
        return packing.toArray();
    }

    /**
     * Given two packed short arrays, left and right, this produces a packed short array that encodes "on" for any cell
     * that was "on" in both left and in right, and encodes "off" for cells that were off in either array. This method
     * does not do any unpacking (which can be somewhat computationally expensive) and so should be strongly preferred
     * when finding the intersection of two pieces of packed data.
     * @param left A packed array such as one produced by pack()
     * @param right A packed array such as one produced by pack()
     * @return A packed array that encodes "on" for all cells that were "on" in both left and right
     */
    public static short[] intersectPacked(short[] left, short[] right)
    {
        if(left.length == 0 || right.length == 0)
            return ALL_WALL;
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, onLeft = false, onRight = false;
        int idx = 0, skip = 0, elemLeft = 0, elemRight = 0, totalLeft = 0, totalRight = 0;
        while ((elemLeft < left.length && elemRight < right.length) && idx <= 0xffff) {
            if (elemLeft >= left.length) {
                totalLeft = 0x20000;
                onLeft = false;
            }
            else if(totalLeft <= idx) {
                totalLeft += left[elemLeft] & 0xffff;
            }
            if(elemRight >= right.length) {
                totalRight = 0x20000;
                onRight = false;
            }
            else if(totalRight <= idx) {
                totalRight += right[elemRight] & 0xffff;
            }
            // 300, 5, 6, 8, 2, 4
            // 290, 12, 9, 1
            // =
            // 300, 2, 9, 1
            // 300 off, 2 on, 9 off, 1 on
            if(totalLeft < totalRight)
            {
                onLeft = !onLeft;
                skip += totalLeft - idx;
                idx = totalLeft;
                if(on != (onLeft && onRight)) {
                    packing.add((short) skip);
                    skip = 0;
                    on = !on;
                }
                elemLeft++;
            }
            else if(totalLeft == totalRight)
            {
                onLeft = !onLeft;
                onRight = !onRight;
                skip += totalLeft - idx;
                idx = totalLeft;
                if(on != (onLeft && onRight)) {
                    packing.add((short) skip);
                    skip = 0;
                    on = !on;
                }
                elemLeft++;
                elemRight++;

            }
            else
            {
                onRight = !onRight;
                skip += totalRight - idx;
                idx = totalRight;
                if(on != (onLeft && onRight)) {
                    packing.add((short) skip);
                    skip = 0;
                    on = !on;
                }
                elemRight++;
            }
        }
        return packing.toArray();
    }

    /**
     * Checks if no cells are encoded as "on" in packed.
     * @param packed a packed array such as one produced by pack()
     * @return false if there is at least one "on" cell in packed; true if there are no "on" cells
     */
    public static boolean isEmpty(short[] packed)
    {
        return packed == null || packed.length <= 1;
    }
    /**
     * Given two packed short arrays, left and right, this returns true if they encode any overlapping area (their areas
     * intersect), or false if they do not overlap at all (they don't intersect). This is more efficient than calculating
     * the intersection with intersectPacked() just to check if it is non-empty, since this method can short-circuit and
     * return true the moment it finds an intersection, plus it needs less overhead (slightly) to do so.
     * @param left A packed array such as one produced by pack()
     * @param right A packed array such as one produced by pack()
     * @return The boolean true if left and right overlap at all, or false if they lack any intersecting area
     */
    public static boolean intersects(short[] left, short[] right)
    {
        if(left.length == 0 || right.length == 0)
            return false;
        boolean onLeft = false, onRight = false;
        int idx = 0, elemLeft = 0, elemRight = 0, totalLeft = 0, totalRight = 0;
        while ((elemLeft < left.length && elemRight < right.length) && idx <= 0xffff) {
            if (elemLeft >= left.length) {
                totalLeft = 0x20000;
                onLeft = false;
            }
            else if(totalLeft <= idx) {
                totalLeft += left[elemLeft] & 0xffff;
            }
            if(elemRight >= right.length) {
                totalRight = 0x20000;
                onRight = false;
            }
            else if(totalRight <= idx) {
                totalRight += right[elemRight] & 0xffff;
            }
            // 300, 5, 6, 8, 2, 4
            // 290, 12, 9, 1
            // =
            // 300, 2, 9, 1
            // 300 off, 2 on, 9 off, 1 on
            if(totalLeft < totalRight)
            {
                onLeft = !onLeft;
                idx = totalLeft;
                if(onLeft && onRight) return true;
                elemLeft++;
            }
            else if(totalLeft == totalRight)
            {
                onLeft = !onLeft;
                onRight = !onRight;
                idx = totalLeft;
                if(onLeft && onRight) return true;
                elemLeft++;
                elemRight++;

            }
            else
            {
                onRight = !onRight;
                idx = totalRight;
                if(onLeft && onRight) return true;
                elemRight++;
            }
        }
        return false;
    }

    /**
     * Given one packed short array, this produces a packed short array that is the exact opposite of the one passed in,
     * that is, every "on" cell becomes "off" and every "off" cell becomes "on", including cells that were "off" because
     * they were beyond the boundaries of the original 2D array passed to pack() or a similar method. This method does
     * not do any unpacking (which can be somewhat computationally expensive), and actually requires among the lowest
     * amounts of computation to get a result of any methods in CoordPacker. However, because it will cause cells to be
     * considered "on" that would cause an exception if directly converted to x,y positions and accessed in the source
     * 2D array, this method should primarily be used in conjunction with operations such as intersectPacked(), or have
     * the checking for boundaries handled internally by unpack() or related methods such as unpackMultiDouble().
     * @param original A packed array such as one produced by pack()
     * @return A packed array that encodes "on" all cells that were "off" in original
     */
    public static short[] negatePacked(short[] original) {
        if (original.length <= 1) {
            return ALL_ON;
        }
        if (original[0] == 0) {
            short[] copy = new short[original.length - 2];
            System.arraycopy(original, 1, copy, 0, original.length - 2);
            //copy[original.length - 3] = (short) (0xFFFF - covered(copy));
            return copy;
        }
        short[] copy = new short[original.length + 2];
        copy[0] = 0;
        System.arraycopy(original, 0, copy, 1, original.length);
        copy[copy.length - 1] = (short) (0xFFFF - covered(copy));
        return copy;
    }

    /**
     * Given two packed short arrays, left and right, this produces a packed short array that encodes "on" for any cell
     * that was "on" in left but "off" in right, and encodes "off" for cells that were "on" in right or "off" in left.
     * This method does not do any unpacking (which can be somewhat computationally expensive) and so should be strongly
     * preferred when finding a region of one packed array that is not contained in another packed array.
     * @param left A packed array such as one produced by pack()
     * @param right A packed array such as one produced by pack()
     * @return A packed array that encodes "on" for all cells that were "on" in left and "off" in right
     */
    public static short[] differencePacked(short[] left, short[] right)
    {
        if(left.length <= 1)
            return ALL_WALL;
        if(right.length <= 1)
            return left;
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, onLeft = false, onRight = false;
        int idx = 0, skip = 0, elemLeft = 0, elemRight = 0, totalLeft = 0, totalRight = 0;
        while ((elemLeft < left.length || elemRight < right.length) && idx <= 0xffff) {
            if (elemLeft >= left.length) {
                totalLeft = 0x20000;
                onLeft = false;
            }
            else if(totalLeft <= idx) {
                totalLeft += left[elemLeft] & 0xffff;
            }
            if(elemRight >= right.length) {
                totalRight = 0x20000;
                onRight = false;
            }
            else if(totalRight <= idx) {
                totalRight += right[elemRight] & 0xffff;
            }
            if(totalLeft < totalRight)
            {
                onLeft = !onLeft;
                skip += totalLeft - idx;
                idx = totalLeft;
                if(on != (onLeft && !onRight)) {
                    packing.add((short) skip);
                    skip = 0;
                    on = !on;
                }
                elemLeft++;
            }
            else if(totalLeft == totalRight)
            {
                onLeft = !onLeft;
                onRight = !onRight;
                skip += totalLeft - idx;
                idx = totalLeft;
                if(on != (onLeft && !onRight)) {
                    packing.add((short) skip);
                    skip = 0;
                    on = !on;
                }
                elemLeft++;
                elemRight++;

            }
            else
            {
                onRight = !onRight;
                skip += totalRight - idx;
                idx = totalRight;
                if(on != (onLeft && !onRight)) {
                    packing.add((short) skip);
                    skip = 0;
                    on = !on;
                }
                elemRight++;
            }
        }
        return packing.toArray();
    }

    /**
     * Given two packed short arrays, left and right, this produces a packed short array that encodes "on" for any cell
     * that was "on" only in left or only in right, but not a cell that was "off" in both or "on" in both. This method
     * does not do any unpacking (which can be somewhat computationally expensive) and so should be strongly preferred
     * when performing an exclusive-or operation on two pieces of packed data.
     * <br>
     * Could more-correctly be called exclusiveDisjunctionPacked to match the other terms, but... seriously?
     * @param left A packed array such as one produced by pack()
     * @param right A packed array such as one produced by pack()
     * @return A packed array that encodes "on" for all cells such that left's cell ^ right's cell returns true
     */
    public static short[] xorPacked(short[] left, short[] right)
    {
        if(left.length == 0)
            return right;
        if(right.length == 0)
            return left;
        ShortVLA packing = new ShortVLA(64);
        boolean on = false, onLeft = false, onRight = false;
        int idx = 0, skip = 0, elemLeft = 0, elemRight = 0, totalLeft = 0, totalRight = 0;
        while ((elemLeft < left.length || elemRight < right.length) && idx <= 0xffff) {
            if (elemLeft >= left.length) {
                totalLeft = 0x20000;
                onLeft = false;
            }
            else if(totalLeft <= idx) {
                totalLeft += left[elemLeft] & 0xffff;
            }
            if(elemRight >= right.length) {
                totalRight = 0x20000;
                onRight = false;
            }
            else if(totalRight <= idx) {
                totalRight += right[elemRight] & 0xffff;
            }
            // 300, 5, 6, 8, 2, 4
            // 290, 12, 9, 1
            // =
            // 290, 15, 6, 8, 2, 4
            // 290 off in both, 10 in right, 2 in both, 3 in left, 6 off in both, 1 on in both, 7 on in left, 2 off in
            //     both, 4 on in left
            if(totalLeft < totalRight)
            {
                onLeft = !onLeft;
                skip += totalLeft - idx;
                idx = totalLeft;
                if(on != (onLeft ^ onRight)) {
                    packing.add((short) skip);
                    skip = 0;
                    on = !on;
                }
                elemLeft++;
            }
            else if(totalLeft == totalRight)
            {
                onLeft = !onLeft;
                onRight = !onRight;
                skip += totalLeft - idx;
                idx = totalLeft;
                if(on != (onLeft ^ onRight)) {
                    packing.add((short) skip);
                    skip = 0;
                    on = !on;
                }
                elemLeft++;
                elemRight++;

            }
            else
            {
                onRight = !onRight;
                skip += totalRight - idx;
                idx = totalRight;
                if(on != (onLeft ^ onRight)) {
                    packing.add((short) skip);
                    skip = 0;
                    on = !on;
                }
                elemRight++;
            }
        }
        return packing.toArray();
    }

    /**
     * Returns a new packed short[] containing the Hilbert distance hilbert as "on", and all other cells "off".
     * Much more efficient than packSeveral called with only one argument.
     * @param hilbert a Hilbert distance that will be encoded as "on"
     * @return the point given to this encoded as "on" in a packed short array
     */
    public static short[] packOne(int hilbert)
    {
        return new short[]{(short) hilbert, 1};
    }
    /**
     * Returns a new packed short[] containing the Coord point as "on", and all other cells "off".
     * Much more efficient than packSeveral called with only one argument.
     * @param point a Coord that will be encoded as "on"
     * @return the point given to this encoded as "on" in a packed short array
     */
    public static short[] packOne(Coord point)
    {
        return new short[]{(short) coordToHilbert(point), 1};
    }
    /**
     * Returns a new packed short[] containing the given x,y cell as "on", and all other cells "off".
     * Much more efficient than packSeveral called with only one argument.
     * @param x the x component of the point that will be encoded as "on"
     * @param y the y component of the point that will be encoded as "on"
     * @return the point given to this encoded as "on" in a packed short array
     */
    public static short[] packOne(int x, int y)
    {
        return new short[]{(short) posToHilbert(x, y), 1};
    }
    /**
     * Returns a new packed short[] containing the Hilbert distances in hilbert as "on" cells, and all other cells "off"
     * @param hilbert a vararg or array of Hilbert distances that will be encoded as "on"
     * @return the points given to this encoded as "on" in a packed short array
     */
    public static short[] packSeveral(int... hilbert)
    {
        if(hilbert == null || hilbert.length == 0)
            return ALL_WALL;
        Arrays.sort(hilbert);
        ShortVLA vla = new ShortVLA(128);
        int current, past = hilbert[0], skip = 0;

        vla.add((short)hilbert[0]);
        for (int i = 1; i < hilbert.length; i++) {
            current = hilbert[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));
        return vla.toArray();
    }

    /**
     * Returns a new packed short[] containing the Coords in points as "on" cells, and all other cells "off"
     * @param points a vararg or array of Coords that will be encoded as "on"
     * @return the points given to this encoded as "on" in a packed short array
     */
    public static short[] packSeveral(Coord... points)
    {
        if(points == null || points.length == 0)
            return ALL_WALL;
        int[] hilbert = new int[points.length];
        for (int i = 0; i < points.length; i++) {
            if(points[i] == null) return ALL_WALL;
            hilbert[i] = coordToHilbert(points[i]);
        }

        Arrays.sort(hilbert);
        ShortVLA vla = new ShortVLA(128);
        int current, past = hilbert[0], skip = 0;

        vla.add((short)hilbert[0]);
        for (int i = 1; i < hilbert.length; i++) {
            current = hilbert[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));
        return vla.toArray();
    }
    /**
     * Returns a new packed short[] containing the Coords in points as "on" cells, and all other cells "off"
     * @param points a Collection of Coords that will be encoded as "on"
     * @return the points given to this encoded as "on" in a packed short array
     */
    public static short[] packSeveral(Collection<Coord> points)
    {
        if(points == null || points.isEmpty())
            return ALL_WALL;
        int sz = points.size();
        int[] hilbert = new int[sz];
        int idx = 0;
        for(Coord c : points)
        {
            if(c == null) return ALL_WALL;
            hilbert[idx++] = coordToHilbert(c);
        }
        Arrays.sort(hilbert);
        ShortVLA vla = new ShortVLA(128);
        int current, past = hilbert[0], skip = 0;

        vla.add((short)hilbert[0]);
        for (int i = 1; i < hilbert.length; i++) {
            current = hilbert[i];
            if (current - past > 1)
            {
                vla.add((short) (skip+1));
                skip = 0;
                vla.add((short)(current - past - 1));
            }
            else if(current != past)
                skip++;
            past = current;
        }
        vla.add((short)(skip+1));
        return vla.toArray();
    }
    /**
     * Given one packed short array, original, and a Hilbert Curve index, hilbert, this produces a packed short array
     * that encodes "on" for any cell that was "on" in original, always encodes "on" for the position referred
     * to by hilbert, and encodes "off" for cells that were "off" in original and are not the cell hilbert refers to.
     * This method does not do any unpacking (which can be somewhat computationally expensive) and so should be strongly
     * preferred when finding a region of one packed array that is not contained in another packed array.
     * @param original A packed array such as one produced by pack()
     * @param hilbert A Hilbert Curve index that should be inserted into the result
     * @return A packed array that encodes "on" for all cells that are "on" in original or correspond to hilbert
     */
    public static short[] insertPacked(short[] original, short hilbert)
    {
        return unionPacked(original, new short[]{hilbert, 1});
    }
    /**
     * Given one packed short array, original, and a position as x,y numbers, this produces a packed short array
     * that encodes "on" for any cell that was "on" in original, always encodes "on" for the position referred
     * to by x and y, and encodes "off" for cells that were "off" in original and are not the cell x and y refer to.
     * This method does not do any unpacking (which can be somewhat computationally expensive) and so should be strongly
     * preferred when finding a region of one packed array that is not contained in another packed array.
     * @param original A packed array such as one produced by pack()
     * @param x The x position at which to insert the "on" cell
     * @param y The y position at which to insert the "on" cell
     * @return A packed array that encodes "on" for all cells that are "on" in original or correspond to x,y
     */
    public static short[] insertPacked(short[] original, int x, int y)
    {
        return unionPacked(original, new short[]{(short)posToHilbert(x, y), 1});
    }

    /**
     * Given one packed short array, original, and a number of Hilbert Curve indices, hilbert, this produces a packed
     * short array that encodes "on" for any cell that was "on" in original, always encodes "on" for the position
     * referred to by any element of hilbert, and encodes "off" for cells that were "off" in original and are not in any
     * cell hilbert refers to. This method does not do any unpacking (which can be somewhat computationally expensive)
     * and so should be strongly preferred when you have several Hilbert Curve indices, possibly nearby each other but
     * just as possibly not, that you need inserted into a packed array.
     * @param original A packed array such as one produced by pack()
     * @param hilbert an array or vararg of Hilbert Curve indices that should be inserted into the result
     * @return A packed array that encodes "on" for all cells that are "on" in original or are contained in hilbert
     */
    public static short[] insertSeveralPacked(short[] original, int... hilbert)
    {
        return unionPacked(original, packSeveral(hilbert));
    }
    /**
     * Given one packed short array, original, and a number of Coords, points, this produces a packed
     * short array that encodes "on" for any cell that was "on" in original, always encodes "on" for the position
     * referred to by any element of points, and encodes "off" for cells that were "off" in original and are not in any
     * cell points refers to. This method does not do any unpacking (which can be somewhat computationally expensive)
     * and so should be strongly preferred when you have several Coords, possibly nearby each other but
     * just as possibly not, that you need inserted into a packed array.
     * @param original A packed array such as one produced by pack()
     * @param points an array or vararg of Coords that should be inserted into the result
     * @return A packed array that encodes "on" for all cells that are "on" in original or are contained in hilbert
     */
    public static short[] insertSeveralPacked(short[] original, Coord... points)
    {
        return unionPacked(original, packSeveral(points));
    }
    /**
     * Given one packed short array, original, and a Collection of Coords, points, this produces a packed
     * short array that encodes "on" for any cell that was "on" in original, always encodes "on" for the position
     * referred to by any element of points, and encodes "off" for cells that were "off" in original and are not in any
     * cell points refers to. This method does not do any unpacking (which can be somewhat computationally expensive)
     * and so should be strongly preferred when you have several Coords, possibly nearby each other but
     * just as possibly not, that you need inserted into a packed array.
     * @param original A packed array such as one produced by pack()
     * @param points an array or vararg of Coords that should be inserted into the result
     * @return A packed array that encodes "on" for all cells that are "on" in original or are contained in hilbert
     */
    public static short[] insertSeveralPacked(short[] original, Collection<Coord> points)
    {
        return unionPacked(original, packSeveral(points));
    }
    /**
     * Given one packed short array, original, and a Hilbert Curve index, hilbert, this produces a packed short array
     * that encodes "on" for any cell that was "on" in original, unless it was the position referred to by hilbert, and
     * encodes "off" for cells that were "off" in original or are the cell hilbert refers to.
     * This method does not do any unpacking (which can be somewhat computationally expensive) and so should be strongly
     * preferred when finding a region of one packed array that is not contained in another packed array.
     * @param original A packed array such as one produced by pack()
     * @param hilbert A Hilbert Curve index that should be removed from the result
     * @return A packed array that encodes "on" for all cells that are "on" in original and don't correspond to hilbert
     */
    public static short[] removePacked(short[] original, short hilbert)
    {
        return differencePacked(original, new short[]{hilbert, 1});
    }
    /**
     * Given one packed short array, original, and a position as x,y numbers, this produces a packed short array that
     * encodes "on" for any cell that was "on" in original, unless it was the position referred to by x and y, and
     * encodes "off" for cells that were "off" in original or are the cell x and y refer to.
     * This method does not do any unpacking (which can be somewhat computationally expensive) and so should be strongly
     * preferred when finding a region of one packed array that is not contained in another packed array.
     * @param original A packed array such as one produced by pack()
     * @param x The x position at which to remove any "on" cell
     * @param y The y position at which to remove any "on" cell
     * @return A packed array that encodes "on" for all cells that are "on" in original and don't correspond to x,y
     */
    public static short[] removePacked(short[] original, int x, int y)
    {
        int dist = posToHilbert(x, y);
        return differencePacked(original, new short[]{(short)dist, 1});
    }

    /**
     * Given one packed short array, original, and a number of Hilbert Curve indices, hilbert, this produces a packed
     * short array that encodes "on" for any cell that was "on" in original, unless it was a position referred to by
     * hilbert, and encodes "off" for cells that were "off" in original and are a cell hilbert refers to. This method
     * does not do any unpacking (which can be somewhat computationally expensive) and so should be strongly preferred
     * when you have several Hilbert Curve indices, possibly nearby each other but just as possibly not, that you need
     * removed from a packed array.
     * @param original A packed array such as one produced by pack()
     * @param hilbert an array or vararg of Hilbert Curve indices that should be inserted into the result
     * @return A packed array that encodes "on" for all cells that are "on" in original and aren't contained in hilbert
     */
    public static short[] removeSeveralPacked(short[] original, int... hilbert)
    {
        return differencePacked(original, packSeveral(hilbert));
    }

    /**
     * Given one packed short array, original, and a number of Coords, points, this produces a packed short
     * array that encodes "on" for any cell that was "on" in original, unless it was a position referred to by an element
     * in points, and encodes "off" for cells that were "off" in original and are a cell points refers to. This method
     * does not do any unpacking (which can be somewhat computationally expensive) and so should be strongly preferred
     * when you have several Hilbert Curve indices, possibly nearby each other but just as possibly not, that you need
     * removed from a packed array.
     * @param original A packed array such as one produced by pack()
     * @param points an array or vararg of Coords that should be removed from the result
     * @return A packed array that encodes "on" for all cells that are "on" in original and aren't contained in points
     */
    public static short[] removeSeveralPacked(short[] original, Coord... points)
    {
        return differencePacked(original, packSeveral(points));
    }

    /**
     * Given one packed short array, original, and a number of Coords, points, this produces a packed short
     * array that encodes "on" for any cell that was "on" in original, unless it was a position referred to by an element
     * in points, and encodes "off" for cells that were "off" in original and are a cell points refers to. This method
     * does not do any unpacking (which can be somewhat computationally expensive) and so should be strongly preferred
     * when you have several Hilbert Curve indices, possibly nearby each other but just as possibly not, that you need
     * removed from a packed array.
     * @param original A packed array such as one produced by pack()
     * @param points a Collection of Coords that should be removed from the result
     * @return A packed array that encodes "on" for all cells that are "on" in original and aren't contained in points
     */
    public static short[] removeSeveralPacked(short[] original, Collection<Coord> points)
    {
        return differencePacked(original, packSeveral(points));
    }

    /**
     * Given a packed data array that encodes multiple unconnected "on" areas, this finds each isolated area (areas that
     * are only adjacent diagonally are considered separate from each other) and returns it as an element in an
     * ArrayList of short[], with one short[] array per isolated area. Useful when you have, for example, all the rooms
     * in a dungeon with their connecting corridors removed, but want to separate the rooms. You can get the
     * aforementioned data assuming a bare dungeon called map with WIDTH and HEIGHT constants using:
     * <br>
     * {@code short[] floors = pack(map, '.'),
     * rooms = flood(floors, retract(floors, 1, WIDTH, HEIGHT, true), 2, false),
     * corridors = differencePacked(floors, rooms),
     * doors = intersectPacked(rooms, fringe(corridors, 1, WIDTH, HEIGHT, false));}
     * <br>
     * You can then get all rooms as separate regions with {@code List<short[]> apart = split(rooms);}, or substitute
     * {@code split(corridors)} to get the corridors. The room-finding technique works by shrinking floors by a radius
     * of 1 (8-way), which causes thin areas like corridors of 2 or less width to be removed, then flood-filling the
     * floors out from the area that produces by 2 cells (4-way this time) to restore the original size of non-corridor
     * areas (plus some extra to ensure odd shapes are kept). Corridors are obtained by removing the rooms from floors.
     * The example code also gets the doors (which overlap with rooms, not corridors) by finding where the a room and a
     * corridor are adjacent. This technique is used with some enhancements in the RoomFinder class.
     * @see squidpony.squidgrid.mapping.RoomFinder for a class that uses this technique without exposing CoordPacker
     * @param packed a packed data array that probably encodes multiple unconnected "on" areas
     * @return an ArrayList of short[] containing each unconnected area from packed as a short[] element
     */
    public static ArrayList<short[]> split(short[] packed)
    {
        ArrayList<short[]> arrays = new ArrayList<>(32);
        short[] remaining = new short[packed.length];
        System.arraycopy(packed, 0, remaining, 0, packed.length);
        while (remaining.length > 1) {
            boolean on = false;
            int idx = 0;
            for (int p = 0; p < remaining.length; p++, on = !on) {
                if (on) {
                    short[] area = flood(packed, packOne((short) idx), 512, false);
                    arrays.add(area);
                    remaining = differencePacked(remaining, area);
                    break;
                }
                idx += remaining[p] & 0xffff;
            }
        }
        return arrays;
    }

    public static short[] removeIsolated(short[] packed)
    {
        short[] remaining = new short[packed.length], viable = new short[packed.length];
        System.arraycopy(packed, 0, remaining, 0, packed.length);
        System.arraycopy(packed, 0, viable, 0, packed.length);
        while (remaining.length > 1) {
            boolean on = false;
            int idx = 0;
            for (int p = 0; p < remaining.length; p++, on = !on) {
                if (on) {
                    short[] area = flood(packed, packOne((short) idx), 512, false);
                    if(count(area) <= 4)
                        viable = differencePacked(viable, area);
                    remaining = differencePacked(remaining, area);
                    break;
                }
                idx += remaining[p] & 0xffff;
            }
        }
        return viable;
    }
    /**
     * Gets a random subset of positions that are "on" in the given packed array, without unpacking it, and returns
     * them as a Coord[]. Random numbers are generated by the rng parameter.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check).
     * @param fraction the likelihood to return one of the "on" cells, from 0.0 to 1.0
     * @param rng the random number generator used to decide random factors.
     * @return a Coord[], ordered by distance along the Hilbert Curve, corresponding to a random section of "on" cells
     * in packed that has a random length approximately equal to the count of all "on" cells in packed times fraction.
     */
    public static Coord[] randomSample(short[] packed, double fraction, RNG rng)
    {
        int counted = count(packed);
        ShortVLA vla = new ShortVLA((int)(counted * fraction) + 1);
        boolean on = false;
        int idx = 0;
        for(int p = 0; p < packed.length; p++, on = !on) {
            if (on) {
                for (int i = idx; i < idx + (packed[p] & 0xffff); i++) {
                    if(rng.nextDouble() < fraction)
                        vla.add((short)i);
                }
            }
            idx += packed[p] & 0xffff;
        }
        int[] distances = vla.asInts();
        Coord[] cs = new Coord[distances.length];
        for (int i = 0; i < distances.length; i++) {
            cs[i] = Coord.get(hilbertX[distances[i]], hilbertY[distances[i]]);
        }
        return cs;
    }
    /**
     * Gets a single randomly chosen position that is "on" in the given packed array, without unpacking it, and returns
     * it as a Coord or returns null of the array is empty. Random numbers are generated by the rng parameter.
     * More efficient in most cases than randomSample(), and will always return at least one Coord for non-empty arrays.
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check).
     * @param rng the random number generator used to decide random factors
     * @return a Coord corresponding to a random "on" cell in packed, or the Coord (-1, -1) if packed is empty
     */
    public static Coord singleRandom(short[] packed, RNG rng)
    {
        int counted = count(packed);
        if(counted == 0)
            return Coord.get(-1,-1);
        int r = rng.nextInt(counted);
        int c = 0, idx = 0;
        boolean on = false;
        for (int i = 0; i < packed.length; on = !on, idx += packed[i] & 0xFFFF, i++) {
            if (on) {
                if(c + (packed[i] & 0xFFFF) > r)
                {
                    idx += r - c;
                    return Coord.get(hilbertX[idx], hilbertY[idx]);
                }
                c += packed[i] & 0xFFFF;
            }
        }
        return Coord.get(-1,-1);

    }

    /**
     * Gets a fixed number of randomly chosen positions that are "on" in the given packed array, without unpacking it,
     * and returns a List of Coord with a count equal to size (or less if there aren't enough "on" cells). Random
     * numbers are generated by the rng parameter. This orders the returned array in the order the Hilbert Curve takes,
     * and you may want to call RNG.shuffle() with it as a parameter to randomize the order.
     *
     * @param packed a short[] returned by pack() or one of the sub-arrays in what is returned by packMulti(); must
     *               not be null (this method does not check).
     * @param size the desired size of the List to return; may be smaller if there aren't enough elements
     * @param rng the random number generator used to decide random factors.
     * @return a List of Coords, ordered by distance along the Hilbert Curve, corresponding to randomly "on" cells in
     * packed, with a length equal to the smaller of size and the count of all "on" cells in packed
     */
    public static ArrayList<Coord> randomPortion(short[] packed, int size, RNG rng)
    {
        int counted = count(packed);
        ArrayList<Coord> coords = new ArrayList<>(Math.min(counted, size));
        if(counted == 0 || size == 0)
            return coords;
        int[] data = rng.randomRange(0, counted, Math.min(counted, size));
        Arrays.sort(data);
        int r = data[0];
        int c = 0, idx = 0;
        boolean on = false;
        for (int i = 0, ri = 0; i < packed.length; on = !on, idx += packed[i] & 0xffff, i++) {
            if (on) {
                while (c + (packed[i] & 0xffff) > r)
                {
                    int n = idx + r - c;
                    coords.add(Coord.get(hilbertX[n], hilbertY[n]));
                    if(++ri < data.length)
                        r = data[ri];
                    else
                        return coords;
                }
                c += packed[i] & 0xffff;
            }
        }
        return coords;
    }


    /**
     * Takes multiple pieces of packed data as short[], encoded by pack() or another similar method of this class, and
     * generates a 2D int array with the specified width and height and a starting value of 0 for all elements, then
     * where every occurrence of a cell as "on" in a piece of packed data increments the cell's value in the returned
     * array. Width and height do not technically need to match the dimensions of the original 2D array, but under most
     * circumstances where they don't match, the data produced will be junk.
     * @param width the width of the 2D array that will be returned; should match the unpacked array's width
     * @param height the height of the 2D array that will be returned; should match the unpacked array's height
     * @param many a vararg or array of short[] encoded by calling one of this class' packing methods on a 2D array
     * @return an int[][] storing at least 0 for all cells, plus 1 for every element of packed that has that cell "on"
     */
    public static int[][] sumMany(int width, int height, short[] ... many)
    {
        if(many == null)
            throw new ArrayIndexOutOfBoundsException("CoordPacker.sumMany() must be given a non-null many arg");
        int[][] unpacked = new int[width][height];
        for (int e = 0; e < many.length; e++) {
            boolean on = false;
            int idx = 0;
            short x = 0, y = 0;
            for(int p = 0; p < many[e].length; p++, on = !on) {
                if (on) {
                    for (int toSkip = idx + (many[e][p] & 0xffff); idx < toSkip && idx < 0x10000; idx++) {
                        x = hilbertX[idx];
                        y = hilbertY[idx];
                        if(x >= width || y >= height)
                            continue;
                        unpacked[x][y]++;
                    }
                } else {
                    idx += many[e][p] & 0xffff;
                }
            }
        }
        return unpacked;
    }

    /**
     * Quick utility method for printing packed data as a grid of 1 (on) and/or 0 (off). Useful primarily for debugging.
     * @param packed a packed short[] such as one produced by pack()
     * @param width the width of the packed 2D array
     * @param height the height of the packed 2D array
     */
    public static void printPacked(short[] packed, int width, int height)
    {
        boolean[][] unpacked = unpack(packed, width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(unpacked[x][y] ? '1' : '0');
            }
            System.out.println();
        }
    }

    public static void printCompressedData(short[] packed)
    {
        if(packed == null || packed.length == 0)
        {
            System.out.println("[]");
            return;
        }
        System.out.print("[" + packed[0]);
        for (int i = 1; i < packed.length; i++) {
            System.out.print(", " + packed[i]);
        }
        System.out.println("]");
    }

    /**
     * Encodes a short array of packed data as a (larger, more memory-hungry) ASCII string, which can be decoded using
     * CoordPacker.decodeASCII() . Uses 64 printable chars, from '?' (ASCII 63) to '~' (ASCII 126).
     * @param packed a packed data item produced by pack() or some other method from this class.
     * @return a printable String, which can be decoded with CoordPacker.decodeASCII()
     */
    public static String encodeASCII(short[] packed)
    {
        int len = packed.length * 3;
        char[] chars = new char[len];
        for (int i = 0, c = 0; c < len; i++, c += 3) {
            chars[c] = (char)((packed[i] & 31) + 63);
            chars[c+1] = (char)(((packed[i] >> 5) & 31) + 63);
            chars[c+2] = (char)(((packed[i] >>> 10) & 63) + 63);
        }
        return new String(chars);
    }
    /**
     * Given a String specifically produced by CoordPacker.encodeASCII(), this will produce a packed data array.
     * @param text a String produced by CoordPacker.encodeASCII(); this will almost certainly fail on other strings.
     * @return the packed data as a short array that was originally used to encode text
     */
    public static short[] decodeASCII(String text)
    {
        int len = text.length();
        if(len % 3 != 0)
            return ALL_WALL;
        char[] chars = text.toCharArray();
        short[] packed = new short[len / 3];
        for (int c = 0, i = 0; c < len; i++, c += 3) {
            packed[i] = (short)(((chars[c] - 63) & 31) | (((chars[c+1] - 63) & 31) << 5) | (((chars[c+2] - 63) & 63) << 10));
        }
        return packed;
    }

    /**
     * Encodes a short array of packed data as a (larger, slightly more memory-hungry) Unicode string using only Braille
     * characters, which can be decoded using CoordPacker.decodeBraille(). Uses 256 semi-printable chars, from 0x2800
     * to 0x28FF, which allows UTF-8 encoding (and some other encodings) to more efficiently store the data. These are
     * only semi-printable because many fonts do not support Braille, and 0x2800 is sometimes printed equivalently to a
     * space char (or sometimes as 8 small dots or open circles, which is preferable). Braille was chosen because it's
     * available as a full Unicode block of 256 characters with no gaps or chars that require special treatment, like
     * newlines and carriage returns.
     * @param packed a packed data item produced by pack() or some other method from this class.
     * @return a semi-printable String, which can be decoded with CoordPacker.decodeBraille()
     */
    public static String encodeBraille(short[] packed)
    {
        int len = packed.length * 2;
        char[] chars = new char[len];
        for (int i = 0, c = 0; c < len; i++, c += 2) {
            chars[c] = (char) ((packed[i] & 0xff) | 0x2800);
            chars[c+1] = (char)((packed[i] >>> 8) | 0x2800);
        }
        return new String(chars);
    }
    /**
     * Given a String specifically produced by CoordPacker.encodeBraille(), this will produce a packed data array.
     * Uses 256 semi-printable chars, from 0x2800 to 0x28FF, which allows UTF-8 encoding (and some other encodings) to
     * more efficiently store the data. These are only semi-printable because many fonts do not support Braille, and
     * 0x2800 is sometimes printed equivalently to a space char (or sometimes as 8 small dots or open circles, which is
     * preferable). Braille was chosen because it's available as a full Unicode block of 256 characters with no gaps or
     * chars that require special treatment, like newlines and carriage returns.
     * @param text a String produced by CoordPacker.encodeBraille(); this will almost certainly fail on other strings.
     * @return the packed data as a short array that was originally used to encode text
     */
    public static short[] decodeBraille(String text)
    {
        int len = text.length();
        if(len % 2 != 0 || len == 0)
            return ALL_WALL;
        char[] chars = text.toCharArray();
        short[] packed = new short[len / 2];
        for (int c = 0, i = 0; c < len; i++, c += 2) {
            packed[i] = (short)((chars[c] ^ 0x2800) | ((chars[c+1] ^ 0x2800) << 8));
        }
        return packed;
    }



    /**
     * Encode a number n as a Gray code; Gray codes have a relation to the Hilbert curve and may be useful.
     * Source: http://xn--2-umb.com/15/hilbert , http://aggregate.org/MAGIC/#Gray%20Code%20Conversion
     * @param n any int
     * @return the gray code for n
     */
    public static int grayEncode(int n){
        return n ^ (n >> 1);
    }

    /**
     * Decode a number from a Gray code n; Gray codes have a relation to the Hilbert curve and may be useful.
     * Source: http://xn--2-umb.com/15/hilbert , http://aggregate.org/MAGIC/#Gray%20Code%20Conversion
     * @param n a gray code, as produced by grayEncode
     * @return the decoded int
     */
    public static int grayDecode(int n) {
        int p = n;
        while ((n >>= 1) != 0)
            p ^= n;
        return p;
    }

    /**
     * Takes an x, y position and returns the length to travel along the 256x256 Hilbert curve to reach that position.
     * This assumes x and y are between 0 and 255, inclusive.
     * This uses a lookup table for the 256x256 Hilbert Curve, which should make it faster than calculating the
     * distance along the Hilbert Curve repeatedly.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param x between 0 and 255 inclusive
     * @param y between 0 and 255 inclusive
     * @return the distance to travel along the 256x256 Hilbert Curve to get to the given x, y point.
     */
    public static int posToHilbert( final int x, final int y ) {
        //int dist = posToHilbertNoLUT(x, y);
        //return dist;
        return hilbertDistances[x + (y << 8)] & 0xffff;
    }
    /**
     * Takes an x, y, z position and returns the length to travel along the 32x32x32 Hilbert curve to reach that
     * position. This assumes x, y, and z are between 0 and 31, inclusive.
     * This uses a lookup table for the 32x32x32 Hilbert Curve, which should make it faster than calculating the
     * distance along the Hilbert Curve repeatedly.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param x between 0 and 31 inclusive
     * @param y between 0 and 31 inclusive
     * @param z between 0 and 31 inclusive
     * @return the distance to travel along the 32x32x32 Hilbert Curve to get to the given x, y, z point.
     */
    public static int posToHilbert3D( final int x, final int y, final int z ) {
        return hilbert3Distances[x + (y << 5) + (z << 10)];
    }
    /**
     * Takes an x, y position and returns the length to travel along the 16x16 Moore curve to reach that position.
     * This assumes x and y are between 0 and 15, inclusive.
     * This uses a lookup table for the 16x16 Moore Curve, which should make it faster than calculating the
     * distance along the Moore Curve repeatedly.
     * @param x between 0 and 15 inclusive
     * @param y between 0 and 15 inclusive
     * @return the distance to travel along the 16x16 Moore Curve to get to the given x, y point.
     */
    public static int posToMoore( final int x, final int y ) {
        return mooreDistances[x + (y << 4)] & 0xff;
    }
    /*
     * Takes an x, y position and returns the length to travel along the 256x256 Hilbert curve to reach that position.
     * This assumes x and y are between 0 and 255, inclusive.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param x between 0 and 255 inclusive
     * @param y between 0 and 255 inclusive
     * @return the distance to travel along the 256x256 Hilbert Curve to get to the given x, y point.
     */

    private static int posToHilbertNoLUT( final int x, final int y )
    {
        int hilbert = 0, remap = 0xb4, mcode, hcode;
        /*
        while( block > 0 )
        {
            --block;
            mcode = ( ( x >> block ) & 1 ) | ( ( ( y >> ( block ) ) & 1 ) << 1);
            hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
            remap ^= ( 0x82000028 >> ( hcode << 3 ) );
            hilbert = ( ( hilbert << 2 ) + hcode );
        }
         */

        mcode = ( ( x >> 7 ) & 1 ) | ( ( ( y >> ( 7 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 6 ) & 1 ) | ( ( ( y >> ( 6 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 5 ) & 1 ) | ( ( ( y >> ( 5 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 4 ) & 1 ) | ( ( ( y >> ( 4 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 3 ) & 1 ) | ( ( ( y >> ( 3 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 2 ) & 1 ) | ( ( ( y >> ( 2 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 1 ) & 1 ) | ( ( ( y >> ( 1 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( x & 1 ) | ( ( y & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );

        hilbert = ( ( hilbert << 2 ) + hcode );

        return hilbert;
    }

    /**
     * Takes a position as a Morton code, with interleaved x and y bits and x in the least significant bit, and returns
     * the length to travel along the 256x256 Hilbert Curve to reach that position.
     * This uses 16 bits of the Morton code and requires that the code is non-negative.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param morton a Morton code that interleaves two 8-bit unsigned numbers, with x as index1 and y as index2.
     * @return a distance to travel down the Hilbert Curve to reach the location that can be decoded from morton.
     */
    public static int mortonToHilbert( final int morton )
    {
        int hilbert = 0;
        int remap = 0xb4;
        int block = BITS;
        while( block > 0 )
        {
            block -= 2;
            int mcode = ( ( morton >> block ) & 3 );
            int hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
            remap ^= ( 0x82000028 >> ( hcode << 3 ) );
            hilbert = ( ( hilbert << 2 ) + hcode );
        }
        return hilbert;
    }

    /**
     * Takes a distance to travel along the 256x256 Hilbert curve and returns a Morton code representing the position
     * in 2D space that corresponds to that point on the Hilbert Curve; the Morton code will have interleaved x and y
     * bits and x in the least significant bit. This uses a lookup table for the 256x256 Hilbert curve, which should
     * make it faster than calculating the position repeatedly.
     * The parameter hilbert is an int but only 16 unsigned bits are used.
     * @param hilbert a distance to travel down the Hilbert Curve
     * @return a Morton code that stores x and y interleaved; can be converted to a Coord with other methods.
     */

    public static int hilbertToMorton( final int hilbert )
    {
        return mortonEncode(hilbertX[hilbert], hilbertY[hilbert]);
    }

    /**
     * Takes a distance to travel along the 256x256 Hilbert curve and returns a Coord representing the position
     * in 2D space that corresponds to that point on the Hilbert curve. This uses a lookup table for the
     * 256x256 Hilbert curve, which should make it faster than calculating the position repeatedly.
     * The parameter hilbert is an int but only 16 unsigned bits are used.
     * @param hilbert a distance to travel down the Hilbert Curve
     * @return a Coord corresponding to the position in 2D space at the given distance down the Hilbert Curve
     */
    public static Coord hilbertToCoord( final int hilbert )
    {
        return Coord.get(hilbertX[hilbert], hilbertY[hilbert]);
    }

    /**
     * Takes a distance to travel along the 16x16 Hilbert curve and returns a Coord representing the position
     * in 2D space that corresponds to that point on the Hilbert curve. This uses a lookup table for the
     * 16x16 Hilbert curve, which should make it faster than calculating the position repeatedly.
     * The parameter moore is an int but only 8 unsigned bits are used, and since the Moore Curve loops, it is
     * calculated as {@code moore % 256}.
     * @param moore a distance to travel down the Moore Curve
     * @return a Coord corresponding to the position in 2D space at the given distance down the Hilbert Curve
     */
    public static Coord mooreToCoord( final int moore )
    {
        return Coord.get(mooreX[moore % 256], mooreY[moore % 256]);
    }


    /*
     * Takes a distance to travel along the 256x256 Hilbert curve and returns a Morton code representing the position
     * in 2D space that corresponds to that point on the Hilbert curve; the Morton code will have interleaved x and y
     * bits and x in the least significant bit. This variant does not use a lookup table, and is likely slower.
     * The parameter hilbert is an int but only 16 unsigned bits are used.
     * @param hilbert
     * @return
     */
    /*
    public static int hilbertToMortonNoLUT( final int hilbert )
    {
        int morton = 0;
        int remap = 0xb4;
        int block = BITS;
        while( block > 0 )
        {
            block -= 2;
            int hcode = ( ( hilbert >> block ) & 3 );
            int mcode = ( ( remap >> ( hcode << 1 ) ) & 3 );
            remap ^= ( 0x330000cc >> ( hcode << 3 ) );
            morton = ( ( morton << 2 ) + mcode );
        }
        return morton;
    }
    */
    /*
     * Takes a distance to travel along the 256x256 Hilbert curve and returns a Coord representing the position
     * in 2D space that corresponds to that point on the Hilbert curve. This variant does not use a lookup table,
     * and is likely slower.
     * The parameter hilbert is an int but only 16 unsigned bits are used.
     * @param hilbert
     * @return
     */
    /*
    private static Coord hilbertToCoordNoLUT( final int hilbert )
    {
        int x = 0, y = 0;
        int remap = 0xb4;
        int block = BITS;
        while( block > 0 )
        {
            block -= 2;
            int hcode = ( ( hilbert >> block ) & 3 );
            int mcode = ( ( remap >> ( hcode << 1 ) ) & 3 );
            remap ^= ( 0x330000cc >> ( hcode << 3 ) );
            x = (x << 1) + (mcode & 1);
            y = (y << 1) + ((mcode & 2) >> 1);
        }
        return Coord.get(x, y);
    }
    */
    /**
     * Takes a position as a Coord called pt and returns the length to travel along the 256x256 Hilbert curve to reach
     * that position.
     * This assumes pt.x and pt.y are between 0 and 255, inclusive.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param pt a Coord with values between 0 and 255, inclusive
     * @return a distance from the start of the 256x256 Hilbert curve to get to the position of pt
     */
    public static int coordToHilbert(final Coord pt)
    {
        return posToHilbert(pt.x, pt.y);
    }

    /**
     * Takes a position as a Coord called pt and returns the length to travel along the 16x16 Moore curve to reach
     * that position.
     * This assumes pt.x and pt.y are between 0 and 15, inclusive.
     * @param pt a Coord with values between 0 and 15, inclusive
     * @return a distance from the "start" of the 16x16 Moore curve to get to the position of pt
     */
    public static int coordToMoore(final Coord pt)
    {
        return posToMoore(pt.x, pt.y);
    }

    public static int mortonEncode3D( int index1, int index2, int index3 )
    { // pack 3 5-bit indices into a 15-bit Morton code
        index1 &= 0x0000001f;
        index2 &= 0x0000001f;
        index3 &= 0x0000001f;
        index1 *= 0x01041041;
        index2 *= 0x01041041;
        index3 *= 0x01041041;
        index1 &= 0x10204081;
        index2 &= 0x10204081;
        index3 &= 0x10204081;
        index1 *= 0x00011111;
        index2 *= 0x00011111;
        index3 *= 0x00011111;
        index1 &= 0x12490000;
        index2 &= 0x12490000;
        index3 &= 0x12490000;
        return( ( index1 >> 16 ) | ( index2 >> 15 ) | ( index3 >> 14 ) );
    }
    public static Coord3D mortonDecode3D( int morton )
    { // unpack 3 5-bit indices from a 15-bit Morton code
        int value1 = morton;
        int value2 = ( value1 >>> 1 );
        int value3 = ( value1 >>> 2 );
        value1 &= 0x00001249;
        value2 &= 0x00001249;
        value3 &= 0x00001249;
        value1 |= ( value1 >>> 2 );
        value2 |= ( value2 >>> 2 );
        value3 |= ( value3 >>> 2 );
        value1 &= 0x000010c3;
        value2 &= 0x000010c3;
        value3 &= 0x000010c3;
        value1 |= ( value1 >>> 4 );
        value2 |= ( value2 >>> 4 );
        value3 |= ( value3 >>> 4 );
        value1 &= 0x0000100f;
        value2 &= 0x0000100f;
        value3 &= 0x0000100f;
        value1 |= ( value1 >>> 8 );
        value2 |= ( value2 >>> 8 );
        value3 |= ( value3 >>> 8 );
        value1 &= 0x0000001f;
        value2 &= 0x0000001f;
        value3 &= 0x0000001f;
        return new Coord3D(value1, value2, value3);
    }
    public static int mortonBitDecode3D( int morton )
    { // unpack 3 5-bit indices from a 15-bit Morton code
        int value1 = morton;
        int value2 = ( value1 >>> 1 );
        int value3 = ( value1 >>> 2 );
        value1 &= 0x00001249;
        value2 &= 0x00001249;
        value3 &= 0x00001249;
        value1 |= ( value1 >>> 2 );
        value2 |= ( value2 >>> 2 );
        value3 |= ( value3 >>> 2 );
        value1 &= 0x000010c3;
        value2 &= 0x000010c3;
        value3 &= 0x000010c3;
        value1 |= ( value1 >>> 4 );
        value2 |= ( value2 >>> 4 );
        value3 |= ( value3 >>> 4 );
        value1 &= 0x0000100f;
        value2 &= 0x0000100f;
        value3 &= 0x0000100f;
        value1 |= ( value1 >>> 8 );
        value2 |= ( value2 >>> 8 );
        value3 |= ( value3 >>> 8 );
        value1 &= 0x0000001f;
        value2 &= 0x0000001f;
        value3 &= 0x0000001f;
        return value1 | (value2 << 5) | (value3 << 10);
    }

    private static void computeHilbert2D(int x, int y)
    {
        int hilbert = 0, remap = 0xb4, mcode, hcode;

        mcode = ( ( x >> 7 ) & 1 ) | ( ( ( y >> ( 7 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 6 ) & 1 ) | ( ( ( y >> ( 6 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 5 ) & 1 ) | ( ( ( y >> ( 5 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 4 ) & 1 ) | ( ( ( y >> ( 4 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 3 ) & 1 ) | ( ( ( y >> ( 3 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 2 ) & 1 ) | ( ( ( y >> ( 2 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 1 ) & 1 ) | ( ( ( y >> ( 1 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( x & 1 ) | ( ( y & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );

        hilbertDistances[x | (y << 8)] = (short) ( hilbert = ( hilbert << 2 ) + hcode );
        hilbertX[hilbert] = (short)x;
        hilbertY[hilbert] = (short)y;
    }

    private static void computeHilbert3D(int x, int y, int z)
    {
        int hilbert = mortonEncode3D(x, y, z);
            int block = 6;
            int hcode = ( ( hilbert >> block ) & 7 );
            int mcode, shift, signs;
            shift = signs = 0;
            while( block > 0 )
            {
                block -= 3;
                hcode <<= 2;
                mcode = ( ( 0x20212021 >> hcode ) & 3 );
                shift = ( ( 0x48 >> ( 7 - shift - mcode ) ) & 3 );
                signs = ( ( signs | ( signs << 3 ) ) >> mcode );
                signs = ( ( signs ^ ( 0x53560300 >> hcode ) ) & 7 );
                mcode = ( ( hilbert >> block ) & 7 );
                hcode = mcode;
                hcode = ( ( ( hcode | ( hcode << 3 ) ) >> shift ) & 7 );
                hcode ^= signs;
                hilbert ^= ( ( mcode ^ hcode ) << block );
            }

        hilbert ^= ( ( hilbert >> 1 ) & 0x92492492 );
        hilbert ^= ( ( hilbert & 0x92492492 ) >> 1 );

        hilbert3X[hilbert] = (short)x;
        hilbert3Y[hilbert] = (short)y;
        hilbert3Z[hilbert] = (short)z;
        hilbert3Distances[x | (y << 3) | (z << 6)] = (short)hilbert;
    }

    /**
     * Gets the x coordinate for a given index into the 16x16x(8*n) Moore curve. Expects indices to touch the following
     * corners of the 16x16x(8*n) cube in this order, using x,y,z syntax:
     * (0,0,0) (0,0,(8*n)) (0,16,(8*n)) (0,16,0) (16,16,0) (16,16,(8*n)) (16,0,(8*n)) (16,0,0)
     * @param index the index into the 3D 16x16x(8*n) Moore Curve, must be less than 0x1000
     * @param n the number of 8-deep layers to use as part of the box shape this travels through
     * @return the x coordinate of the given distance traveled through the 3D 16x16x(8*n) Moore Curve
     */
    public static int getXMoore3D(final int index, final int n) {
        int hilbert = index & 0x1ff;
        int sector = index >> 9;
        if (sector < 2 * n)
            return 7 - hilbert3X[hilbert];
        else
            return 8 + hilbert3X[hilbert];
    }

    /**
     * Gets the y coordinate for a given index into the 16x16x(8*n) Moore curve. Expects indices to touch the following
     * corners of the 16x16x(8*n) cube in this order, using x,y,z syntax:
     * (0,0,0) (0,0,(8*n)) (0,16,(8*n)) (0,16,0) (16,16,0) (16,16,(8*n)) (16,0,(8*n)) (16,0,0)
     * @param index the index into the 3D 16x16x(8*n) Moore Curve, must be less than 0x1000
     * @param n the number of 8-deep layers to use as part of the box shape this travels through
     * @return the y coordinate of the given distance traveled through the 3D 16x16x(8*n) Moore Curve
     */
    public static int getYMoore3D(final int index, final int n)
    {
        int hilbert = index & 0x1ff;
        int sector = index >> 9;
        if (sector < n || sector >= 3 * n)
            return 7 - hilbert3Y[hilbert];
        else
            return 8 + hilbert3Y[hilbert];

    }
    /**
     * Gets the z coordinate for a given index into the 16x16x(8*n) Moore curve. Expects indices to touch the following
     * corners of the 16x16x(8*n) cube in this order, using x,y,z syntax:
     * (0,0,0) (0,0,(8*n)) (0,16,(8*n)) (0,16,0) (16,16,0) (16,16,(8*n)) (16,0,(8*n)) (16,0,0)
     * @param index the index into the 3D 16x16x(8*n) Moore Curve, must be less than 0x1000
     * @param n the number of 8-deep layers to use as part of the box shape this travels through
     * @return the z coordinate of the given distance traveled through the 3D 16x16x(8*n) Moore Curve
     */
    public static int getZMoore3D(final int index, final int n) {
        int hilbert = index & 0x1ff;
        int sector = index >> 9;
        if ((sector / n) % 2 == 0)
            return hilbert3Z[hilbert] + 8 * (sector % n);
        else
            return (8 * n - 1) - hilbert3Z[hilbert] - 8 * (sector % n);
    }



    /**
     * Takes two 8-bit unsigned integers index1 and index2, and returns a Morton code, with interleaved index1 and
     * index2 bits and index1 in the least significant bit. With this method, index1 and index2 can have up to 8 bits.
     * This returns a 16-bit Morton code and WILL encode information in the sign bit if the inputs are large enough.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param index1 a non-negative integer using at most 8 bits, to be placed in the "x" slots
     * @param index2 a non-negative integer using at most 8 bits, to be placed in the "y" slots
     * @return a Morton code/Z-Code that interleaves the two numbers into one 16-bit short
     */
    public static short zEncode(short index1, short index2)
    { // pack 2 8-bit (unsigned) indices into a 16-bit (signed...) Morton code/Z-Code
        index1 &= 0x000000ff;
        index2 &= 0x000000ff;
        index1 |= ( index1 << 4 );
        index2 |= ( index2 << 4 );
        index1 &= 0x00000f0f;
        index2 &= 0x00000f0f;
        index1 |= ( index1 << 2 );
        index2 |= ( index2 << 2 );
        index1 &= 0x00003333;
        index2 &= 0x00003333;
        index1 |= ( index1 << 1 );
        index2 |= ( index2 << 1 );
        index1 &= 0x00005555;
        index2 &= 0x00005555;
        return (short)(index1 | ( index2 << 1 ));
    }
    /**
     * Takes two 8-bit unsigned integers index1 and index2, and returns a Morton code, with interleaved index1 and
     * index2 bits and index1 in the least significant bit. With this method, index1 and index2 can have up to 8 bits.
     * This returns a 32-bit Morton code but only uses 16 bits, and will not encode information in the sign bit.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param index1 a non-negative integer using at most 8 bits, to be placed in the "x" slots
     * @param index2 a non-negative integer using at most 8 bits, to be placed in the "y" slots
     * @return a Morton code that interleaves the two numbers as one 32-bit int, but only in 16 bits of it
     */
    public static int mortonEncode(int index1, int index2)
    { // pack 2 8-bit (unsigned) indices into a 32-bit (signed...) Morton code
        index1 &= 0x000000ff;
        index2 &= 0x000000ff;
        index1 |= ( index1 << 4 );
        index2 |= ( index2 << 4 );
        index1 &= 0x00000f0f;
        index2 &= 0x00000f0f;
        index1 |= ( index1 << 2 );
        index2 |= ( index2 << 2 );
        index1 &= 0x00003333;
        index2 &= 0x00003333;
        index1 |= ( index1 << 1 );
        index2 |= ( index2 << 1 );
        index1 &= 0x00005555;
        index2 &= 0x00005555;
        return index1 | ( index2 << 1 );
    }

    /**
     * Takes a Morton code, with interleaved x and y bits and x in the least significant bit, and returns the Coord
     * representing the same x, y position.
     * This uses 16 bits of the Morton code and requires that the code is non-negative.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param morton an int containing two interleaved numbers, from 0 to 255 each
     * @return a Coord matching the x and y extracted from the Morton code
     */
    public static Coord mortonDecode( final int morton )
    { // unpack 2 8-bit (unsigned) indices from a 32-bit (signed...) Morton code
        int value1 = morton;
        int value2 = ( value1 >> 1 );
        value1 &= 0x5555;
        value2 &= 0x5555;
        value1 |= ( value1 >> 1 );
        value2 |= ( value2 >> 1 );
        value1 &= 0x3333;
        value2 &= 0x3333;
        value1 |= ( value1 >> 2 );
        value2 |= ( value2 >> 2 );
        value1 &= 0x0f0f;
        value2 &= 0x0f0f;
        value1 |= ( value1 >> 4 );
        value2 |= ( value2 >> 4 );
        value1 &= 0x00ff;
        value2 &= 0x00ff;
        return Coord.get(value1, value2);
    }

    /**
     * Takes a Morton code, with interleaved x and y bits and x in the least significant bit, and returns the Coord
     * representing the same x, y position.
     * This takes a a 16-bit Z-Code with data in the sign bit, as returned by zEncode().
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param morton a short containing two interleaved numbers, from 0 to 255 each
     * @return a Coord matching the x and y extracted from the Morton code
     */
    public static Coord zDecode( final short morton )
    { // unpack 2 8-bit (unsigned) indices from a 32-bit (signed...) Morton code
        int value1 = morton & 0xffff;
        int value2 = ( value1 >> 1 );
        value1 &= 0x5555;
        value2 &= 0x5555;
        value1 |= ( value1 >> 1 );
        value2 |= ( value2 >> 1 );
        value1 &= 0x3333;
        value2 &= 0x3333;
        value1 |= ( value1 >> 2 );
        value2 |= ( value2 >> 2 );
        value1 &= 0x0f0f;
        value2 &= 0x0f0f;
        value1 |= ( value1 >> 4 );
        value2 |= ( value2 >> 4 );
        value1 &= 0x00ff;
        value2 &= 0x00ff;
        return Coord.get(value1, value2);
    }
}
