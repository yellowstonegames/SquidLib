package squidpony.squidgrid;

import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.ShortVLA;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static squidpony.squidmath.CoordPacker.*;

/**
 * Created by Tommy Ettinger on 10/7/2015.
 */
public class FOVCache {
    protected int maxRadius;
    protected int width;
    protected int height;
    protected  double[][] resMap;
    protected Radius radiusKind;

    protected short[][][] cache;
    protected boolean complete;
    protected FOV fov;
    protected short[][] ALL_WALLS;
    protected final int NUM_THREADS;
    public FOVCache(FOV fov, char[][] map, int maxRadius)
    {
        if(map == null || map.length == 0)
            throw new UnsupportedOperationException("The map used by FOVCache must not be null or empty");
        NUM_THREADS = 8;
        width = map.length;
        height = map[0].length;
        if(width > 256 || height > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently cache, aborting");
        if(maxRadius <= 0 || maxRadius >= 64)
            throw new UnsupportedOperationException("FOV radius is incorrect. Must be 0 < maxRadius < 64");
        this.fov = fov;
        resMap = DungeonUtility.generateResistances(map);
        this.maxRadius = maxRadius;
        radiusKind = Radius.SQUARE;
        cache = new short[width * height][][];
        ALL_WALLS = new short[maxRadius][];
        for (int i = 0; i < maxRadius; i++) {
            ALL_WALLS[i] = ALL_WALL;
        }
        complete = false;
    }
    public FOVCache(FOV fov, char[][] map, int maxRadius, int threadCount)
    {
        if(map == null || map.length == 0)
            throw new UnsupportedOperationException("The map used by FOVCache must not be null or empty");
        NUM_THREADS = threadCount;
        width = map.length;
        height = map[0].length;
        if(width > 256 || height > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently cache, aborting");
        if(maxRadius <= 0 || maxRadius >= 64)
            throw new UnsupportedOperationException("FOV radius is incorrect. Must be 0 < maxRadius < 64");
        this.fov = fov;
        resMap = DungeonUtility.generateResistances(map);
        this.maxRadius = maxRadius;
        radiusKind = Radius.SQUARE;
        cache = new short[width * height][][];
        ALL_WALLS = new short[maxRadius][];
        for (int i = 0; i < maxRadius; i++) {
            ALL_WALLS[i] = ALL_WALL;
        }
        complete = false;
    }

    /**
     * Packs FOV for a point as a center, and returns it to be stored.
     * @param index an int that stores the x,y center of FOV as calculated by: x + y * width
     * @return a multi-packed series of progressively wider FOV radii
     */
    protected long storeCellFOV(int index) {
        long startTime = System.currentTimeMillis();
        cache[index] = calculateCellFOV(index % width, index / width);
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Packs FOV for the given viewer's X and Y as a center, and returns the packed data to be stored.
     * @param viewerX an int less than 256 and less than width
     * @param viewerY an int less than 256 and less than height
     * @return a multi-packed series of progressively wider FOV radii
     */
    public short[][] calculateCellFOV(int viewerX, int viewerY)
    {
        if(viewerX < 0 || viewerY < 0 || viewerX >= width || viewerY >= height)
            return ALL_WALLS;
        if(resMap[viewerX][viewerY] >= 1.0)
        {
            return ALL_WALLS;
        }
        int limit = 0x10000, mapLimit = width * height, llen = maxRadius;
        long on = 0, current = 0;
        ShortVLA[] packing = new ShortVLA[llen];
        int[] skip = new int[llen];
        if(height <= 128) {
            limit >>= 1;
            if (width <= 128) {
                limit >>= 1;
                if (width <= 64) {
                    limit >>= 1;
                    if (height <= 64) {
                        limit >>= 1;
                        if (height <= 32) {
                            limit >>= 1;
                            if (width <= 32) {
                                limit >>= 1;
                            }
                        }
                    }
                }
            }
        }
        short x, y;
        short[][] packed = new short[llen][];
        double[][] fovMap;
        for(int l = 0; l < llen; l++) {

            fovMap = fov.calculateFOV(resMap, viewerX, viewerY, l + 1, radiusKind);
            packing[l] = new ShortVLA(64);
            for (int i = 0, ml = 0; i < limit && ml < mapLimit; i++, skip[l]++) {

                x = hilbertX[i];
                y = hilbertY[i];
                if (x >= width || y >= height) {
                    if ((on & (1L << l)) != 0L) {
                        on ^= (1L << l);
                        packing[l].add((short) skip[l]);
                        skip[l] = 0;
                    }
                    continue;
                }
                ml++;
                // sets the bit at position l in current to 1 if the following is true, 0 if it is false:
                //     fovMap[x][y] > levels[l]
                // looks more complicated than it is.
                current ^= ((fovMap[x][y] > 0.0 ? -1 : 0) ^ current) & (1 << l);
                if (((current >> l) & 1L) != ((on >> l) & 1L)) {
                    packing[l].add((short) skip[l]);
                    skip[l] = 0;
                    on = current;

                    // sets the bit at position l in on to the same as the bit at position l in current.
                    on ^= (-((current >> l) & 1L) ^ on) & (1L << l);

                }
            }

            if (((on >> l) & 1L) == 1L)
                packing[l].add((short) skip[l]);
            if(packing[l].size == 0)
                packed[l] = ALL_WALL;
            else
                packed[l] = packing[l].shrink();
        }
        return packed;
    }

    public short[][] getCacheEntry(int x, int y)
    {
        return cache[x + y * width];
    }

    public void cacheAll() {
        List<FOVUnit> units = new ArrayList<FOVUnit>(width * height);
        for (int i = 0; i < width * height; i++) {
            units.add(new FOVUnit(i));
        }
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        long totalTime = System.currentTimeMillis(), threadTime = 0L;

        try {
            final List<Future<Long>> invoke = executor.invokeAll(units);
            for (Future<Long> future : invoke) {
                long t = future.get();
                threadTime += t;
                //System.out.println(t);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        totalTime = System.currentTimeMillis() - totalTime;
        System.out.println("Total real time elapsed: " + totalTime);
        System.out.println("Total CPU time elapsed, on " + NUM_THREADS + " threads: " + threadTime);

        long totalRAM = 0;
        for (int c = 0; c < width * height; c++) {
            int ctr = 0;
            for (int i = 0; i < cache[c].length; i++) {
                ctr += arrayMemoryUsage(cache[c][i].length, 2);
            }
            totalRAM += (((ctr + 12 - 1) / 8) + 1) * 8;
        }
        System.out.println("Total memory used by cache: " + totalRAM);
    }

    public long arrayMemoryUsage(int length, long bytesPerItem)
    {
        return (((bytesPerItem * length + 12 - 1) / 8) + 1) * 8L;
    }
    public class FOVUnit implements Callable<Long>
    {
        protected int index;
        public FOVUnit(int index)
        {
            this.index = index;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public Long call() throws Exception {
            return storeCellFOV(index);
        }
    }
}
