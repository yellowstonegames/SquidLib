package squidpony.squidgrid;

import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
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
    protected double[][] resMap;
    protected Radius radiusKind;

    protected short[][][] cache;
    protected short[][] losCache;
    protected boolean complete;
    protected FOV fov;
    protected short[][] ALL_WALLS;
    private double[][] atan2Cache, directionAngles;
    private short[][] distanceCache;
    private Coord[][] waves;
    protected final int NUM_THREADS;
    private static final double HALF_PI = Math.PI * 0.5, QUARTER_PI = Math.PI * 0.25125,
            SLIVER_PI = Math.PI * 0.05, PI2 = Math.PI * 2;
    public FOVCache(char[][] map, int maxRadius, Radius radiusKind)
    {
        if(map == null || map.length == 0)
            throw new UnsupportedOperationException("The map used by FOVCache must not be null or empty");
        NUM_THREADS = 8;
        width = map.length;
        height = map[0].length;
        if(width > 256 || height > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently cache, aborting");
        if(maxRadius <= 0 || maxRadius >= 63)
            throw new UnsupportedOperationException("FOV radius is incorrect. Must be 0 < maxRadius < 63");
        this.fov = new FOV(FOV.SHADOW);
        resMap = DungeonUtility.generateResistances(map);
        this.maxRadius = maxRadius;
        this.radiusKind = radiusKind;
        cache = new short[width * height][][];
        losCache = new short[width * height][];
        ALL_WALLS = new short[maxRadius][];
        for (int i = 0; i < maxRadius; i++) {
            ALL_WALLS[i] = ALL_WALL;
        }
        preloadMeasurements();
        complete = false;
    }
    public FOVCache(char[][] map, int maxRadius, Radius radiusKind, int threadCount)
    {
        if(map == null || map.length == 0)
            throw new UnsupportedOperationException("The map used by FOVCache must not be null or empty");
        NUM_THREADS = threadCount;
        width = map.length;
        height = map[0].length;
        if(width > 256 || height > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently cache, aborting");
        if(maxRadius <= 0 || maxRadius >= 63)
            throw new UnsupportedOperationException("FOV radius is incorrect. Must be 0 < maxRadius < 63");
        this.fov = new FOV(FOV.SHADOW);
        resMap = DungeonUtility.generateResistances(map);
        this.maxRadius = maxRadius;
        this.radiusKind = radiusKind;
        cache = new short[width * height][][];
        losCache = new short[width * height][];
        ALL_WALLS = new short[maxRadius][];
        for (int i = 0; i < maxRadius; i++) {
            ALL_WALLS[i] = ALL_WALL;
        }
        preloadMeasurements();
        complete = false;
    }

    private void preloadMeasurements()
    {
        atan2Cache = new double[maxRadius * 2 + 1][maxRadius * 2 + 1];
        distanceCache = new short[maxRadius * 2 + 1][maxRadius * 2 + 1];
        waves = new Coord[maxRadius + 1][];
        waves[0] = new Coord[]{Coord.get(maxRadius, maxRadius)};

        ShortVLA[] positionsAtDistance = new ShortVLA[maxRadius + 1];
        for (int i = 0; i < maxRadius + 1; i++) {
            positionsAtDistance[i] = new ShortVLA(i * 8 + 1);
        }
        short tmp, inverse_tmp;
        for (int i = 0; i <= maxRadius; i++) {
            for (int j = 0; j <= maxRadius; j++) {
                tmp = distance(i, j);
                inverse_tmp = (short)(maxRadius - tmp / 2);

                atan2Cache[maxRadius + i][maxRadius + j] = Math.atan2(j, i);
                if(tmp > 0) {
                    atan2Cache[maxRadius - i][maxRadius + j] = Math.atan2(j, -i);
                    atan2Cache[maxRadius + i][maxRadius - j] = Math.atan2(-j, i);
                    atan2Cache[maxRadius - i][maxRadius - j] = Math.atan2(-j, -i);
                }
                if(tmp / 2 <= maxRadius) {
                    distanceCache[maxRadius + i][maxRadius + j] = inverse_tmp;
                    if (tmp > 0) {
                        distanceCache[maxRadius - i][maxRadius + j] = inverse_tmp;
                        distanceCache[maxRadius + i][maxRadius - j] = inverse_tmp;
                        distanceCache[maxRadius - i][maxRadius - j] = inverse_tmp;
                        positionsAtDistance[tmp / 2].add(zEncode((short) (maxRadius + i), (short) (maxRadius + j)));
                        if (i > 0)
                            positionsAtDistance[tmp / 2].add(zEncode((short) (maxRadius - i), (short) (maxRadius + j)));
                        if (j > 0)
                            positionsAtDistance[tmp / 2].add(zEncode((short) (maxRadius + i), (short) (maxRadius - j)));
                        if(i > 0 && j > 0)
                            positionsAtDistance[tmp / 2].add(zEncode((short) (maxRadius - i), (short) (maxRadius - j)));

                    }else {
                        positionsAtDistance[0].add(zEncode((short) maxRadius, (short) maxRadius));
                    }
                }
            }
        }
        short[][] positionsZ = new short[maxRadius + 1][];
        for (int i = 0; i <= maxRadius; i++) {
            positionsZ[i] = positionsAtDistance[i].shrink();
            waves[i] = new Coord[positionsZ[i].length];
            for (int j = 0; j < waves[i].length; j++) {
                waves[i][j] = zDecode(positionsZ[i][j]);
            }
        }
        directionAngles = new double[3][3];
        directionAngles[0][0] = Math.atan2(1,1);
        directionAngles[0][1] = Math.atan2(0,1);
        directionAngles[0][2] = Math.atan2(-1,1);
        directionAngles[1][0] = Math.atan2(1,0);
        directionAngles[1][1] = 0;
        directionAngles[1][2] = Math.atan2(-1,0);
        directionAngles[2][0] = Math.atan2(1,-1);
        directionAngles[2][1] = Math.atan2(0,-1);
        directionAngles[2][2] = Math.atan2(-1,-1);
    }

    /**
     * Packs FOV for a point as a center, and returns it to be stored.
     * @param index an int that stores the x,y center of FOV as calculated by: x + y * width
     * @return a multi-packed series of progressively wider FOV radii
     */
    protected long storeCellFOV(int index) {
        long startTime = System.currentTimeMillis();
        cache[index] = calculateWaveFOV(index % width, index / width);
        //cache[index] = calculateCellFOV(index % width, index / width);
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Packs FOV for a point as a center, and returns it to be stored.
     * @param index an int that stores the x,y center of FOV as calculated by: x + y * width
     * @return a multi-packed series of progressively wider FOV radii
     */
    protected long storeCellLOS(int index) {
        long startTime = System.currentTimeMillis();
        losCache[index] = calculateCellLOS(index % width, index / width);
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

    /**
     * Packs FOV for the given viewer's X and Y as a center, and returns the packed data to be stored.
     * @param viewerX an int less than 256 and less than width
     * @param viewerY an int less than 256 and less than height
     * @return a packed FOV map for radius 62
     */
    public short[] calculateCellLOS(int viewerX, int viewerY)
    {
        if(viewerX < 0 || viewerY < 0 || viewerX >= width || viewerY >= height)
            return ALL_WALL;
        if(resMap[viewerX][viewerY] >= 1.0)
        {
            return ALL_WALL;
        }
        return pack(fov.calculateFOV(resMap, viewerX, viewerY, 62, radiusKind));
    }

    /**
     * Packs FOV for the given viewer's X and Y as a center, and returns the packed data to be stored.
     * @param viewerX an int less than 256 and less than width
     * @param viewerY an int less than 256 and less than height
     * @return a multi-packed series of progressively wider FOV radii
     */
    public short[][] calculateWaveFOV(int viewerX, int viewerY) {
        if (viewerX < 0 || viewerY < 0 || viewerX >= width || viewerY >= height)
            return ALL_WALLS;
        if (resMap[viewerX][viewerY] >= 1.0) {
            return ALL_WALLS;
        }
        return packMulti(waveFOV(viewerX, viewerY), maxRadius + 1);
    }
    public short[][] getCacheEntry(int x, int y)
    {
        return cache[x + y * width];
    }

    public boolean isCellVisible(int visionRange, int viewerX, int viewerY, int targetX, int targetY)
    {
        return queryPacked(cache[viewerX + viewerY  * width][maxRadius - visionRange], targetX, targetY) ||
                queryPacked(cache[targetX + targetY  * width][maxRadius - visionRange], viewerX, viewerY);
    }

    public void cacheAll() {
        List<LOSUnit> losUnits = new ArrayList<LOSUnit>(width * height);
        List<FOVUnit> fovUnits = new ArrayList<FOVUnit>(width * height);
        for (int i = 0; i < width * height; i++) {
            losUnits.add(new LOSUnit(i));
            fovUnits.add(new FOVUnit(i));
        }
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        long totalTime = System.currentTimeMillis(), threadTime = 0L;

        try {
            final List<Future<Long>> invoke = executor.invokeAll(losUnits);
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

        try {
            final List<Future<Long>> invoke = executor.invokeAll(fovUnits);
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
            long ctr = 0, losCtr = 0;
            for (int i = 0; i < cache[c].length; i++) {
                ctr += (((2 * cache[c][i].length + 12 - 1) / 8) + 1) * 8L;
            }
            totalRAM += (((ctr + 12 - 1) / 8) + 1) * 8;

            losCtr = (((2 * losCache[c].length + 12 - 1) / 8) + 1) * 8L;
            totalRAM += (((losCtr + 12 - 1) / 8) + 1) * 8;
        }
        System.out.println("Total memory used by cache: " + totalRAM);
    }


    public byte[][] waveFOV(int viewerX, int viewerY) {
        byte[][] gradientMap = new byte[width][height];
        double[][] angleMap = new double[2 * maxRadius + 1][2 * maxRadius + 1];
        gradientMap[viewerX][viewerY] = (byte)(2 * maxRadius);
        Direction[] dirs = (radiusKind == Radius.DIAMOND || radiusKind == Radius.OCTAHEDRON)
                ? Direction.CARDINALS : Direction.OUTWARDS;
        int cx, cy, nearCWx, nearCWy, nearCCWx, nearCCWy;
        Coord pt;
        double theta, angleCW, angleCCW, straight;
        byte dist;
        boolean blockedCCW, blockedCW;
        for(int w = 0; w < waves.length; w++)
        {
            for(int c = 0; c < waves[w].length; c++)
            {
                pt = waves[w][c];
                cx = viewerX - maxRadius + pt.x;
                cy = viewerY - maxRadius + pt.y;
                if(cx < width && cx >= 0 && cy < height && cy >= 0)
                {
                    theta = atan2Cache[pt.x][pt.y];
                    dist = (byte)(distanceCache[pt.x][pt.y ] + 1);

                    if(w <= 0)
                    {
                        gradientMap[cx][cy] = dist;
                    }
                    else {
                        switch ((int) Math.floor(theta / QUARTER_PI)) {

                            //positive x, postive y, closer to x-axis
                            case 0:
                                nearCCWx = pt.x - 1;
                                nearCCWy = pt.y;
                                angleCCW = directionAngles[0][1]; //atan2Cache[nearCCWx][nearCCWy];
                                straight = angleCCW;
                                nearCWx = pt.x - 1;
                                nearCWy = pt.y - 1;
                                angleCW = directionAngles[0][0]; //atan2Cache[nearCWx][nearCWy];
                                break;
                            //positive x, postive y, closer to y-axis
                            case 1:
                                nearCWx = pt.x;
                                nearCWy = pt.y - 1;
                                angleCW = directionAngles[1][0];
                                straight = angleCW;
                                nearCCWx = pt.x - 1;
                                nearCCWy = pt.y - 1;
                                angleCCW = directionAngles[0][0];
                                break;
                            //negative x, postive y, closer to y-axis
                            case 2:
                                nearCCWx = pt.x;
                                nearCCWy = pt.y - 1;
                                angleCCW = directionAngles[1][0];
                                straight = angleCCW;
                                nearCWx = pt.x + 1;
                                nearCWy = pt.y - 1;
                                angleCW = directionAngles[2][0];
                                break;
                            //negative x, postive y, closer to x-axis
                            case 3:
                                nearCCWx = pt.x + 1;
                                nearCCWy = pt.y;
                                angleCCW = directionAngles[2][1];
                                straight = angleCCW;
                                nearCWx = pt.x + 1;
                                nearCWy = pt.y - 1;
                                angleCW = directionAngles[2][0];
                                break;

                            //negative x, negative y, closer to x-axis
                            case -4:
                                nearCWx = pt.x + 1;
                                nearCWy = pt.y;
                                angleCW = -directionAngles[2][1];
                                straight = angleCW;
                                nearCCWx = pt.x + 1;
                                nearCCWy = pt.y + 1;
                                angleCCW = directionAngles[2][2];

                                break;
                            //negative x, negative y, closer to y-axis
                            case -3:
                                nearCWx = pt.x;
                                nearCWy = pt.y + 1;
                                angleCW = directionAngles[1][2];
                                straight = angleCW;
                                nearCCWx = pt.x + 1;
                                nearCCWy = pt.y + 1;
                                angleCCW = directionAngles[2][2];
                                break;
                            //positive x, negative y, closer to y-axis
                            case -2:
                                nearCCWx = pt.x;
                                nearCCWy = pt.y + 1;
                                angleCCW = directionAngles[1][2];
                                straight = angleCCW;
                                nearCWx = pt.x - 1;
                                nearCWy = pt.y + 1;
                                angleCW = directionAngles[0][2];
                                break;
                            //positive x, negative y, closer to x-axis
                            default:
                                nearCWx = pt.x - 1;
                                nearCWy = pt.y;
                                angleCW = directionAngles[0][1];
                                straight = angleCW;
                                nearCCWx = pt.x - 1;
                                nearCCWy = pt.y + 1;
                                angleCCW = directionAngles[0][2];
                                break;
                        }
                        nearCCWx += viewerX - maxRadius;
                        nearCWx += viewerX - maxRadius;
                        nearCCWy += viewerY - maxRadius;
                        nearCWy += viewerY - maxRadius;

                        blockedCCW = resMap[nearCCWx][nearCCWy] > 0.5 ||
                                angleMap[nearCCWx - viewerX + maxRadius][nearCCWy - viewerY + maxRadius] >= PI2;
                        blockedCW = resMap[nearCWx][nearCWy] > 0.5 ||
                                angleMap[nearCWx - viewerX + maxRadius][nearCWy - viewerY + maxRadius] >= PI2;

                        if( theta == 0 || theta == Math.PI || (Math.abs(theta) - HALF_PI < 0.005 && Math.abs(theta) - HALF_PI > -0.005))
                            angleMap[pt.x][pt.y] = (straight == angleCCW)
                                    ?  (blockedCCW)
                                      ? PI2
                                      : angleMap[nearCCWx - viewerX + maxRadius][nearCCWy - viewerY + maxRadius]
                                    : (blockedCW)
                                      ? PI2
                                      : angleMap[nearCWx - viewerX + maxRadius][nearCWy - viewerY + maxRadius];
                        else {
                            if (blockedCW && blockedCCW) {
                                angleMap[pt.x][pt.y] = PI2;
                                continue;
                            }
                            if (blockedCW) {
                                angleMap[pt.x][pt.y] = Math.abs(theta - angleCCW) + SLIVER_PI;
                                        //angleMap[nearCCWx - viewerX + maxRadius][nearCCWy - viewerY + maxRadius];
                                        //Math.abs(angleMap[nearCCWx - viewerX + maxRadius][nearCCWy - viewerY + maxRadius] -


                                //angleMap[pt.x][pt.y] = Math.abs(theta - angleCCW) +
                                //        angleMap[nearCCWx - viewerX + maxRadius][nearCCWy - viewerY + maxRadius];

                            } else if (blockedCCW) {
                                angleMap[pt.x][pt.y] = Math.abs(angleCW - theta) + SLIVER_PI;
                                //angleMap[nearCWx - viewerX + maxRadius][nearCWy - viewerY + maxRadius];
                                //angleMap[nearCWx - viewerX + maxRadius][nearCWy - viewerY + maxRadius]
                            }
                            if (!blockedCW)
                                angleMap[pt.x][pt.y] += 0.5 * angleMap[nearCWx - viewerX + maxRadius][nearCWy - viewerY + maxRadius];
                            if (!blockedCCW)
                                angleMap[pt.x][pt.y] += 0.5 * angleMap[nearCCWx - viewerX + maxRadius][nearCCWy - viewerY + maxRadius];
                        }
                        if(angleMap[pt.x][pt.y] <= QUARTER_PI)
                            gradientMap[cx][cy] = dist;
                        else
                            angleMap[pt.x][pt.y] = PI2;
                    }
                }
            }
        }

        return gradientMap;
    }
    private byte heuristic(Direction target) {
        switch (radiusKind) {
            case CIRCLE:
            case SPHERE:
                switch (target) {
                    case DOWN_LEFT:
                    case DOWN_RIGHT:
                    case UP_LEFT:
                    case UP_RIGHT:
                        return 3;
                    default:
                        return 2;
                }
            default:
                return 2;
        }
    }
    private short distance(int x, int y) {
        switch (radiusKind) {
            case CIRCLE:
            case SPHERE:
            {
                if(x == y)
                    return (short)(3 * x);
                else if(x < y)
                    return (short)(3 * x + 2 * (y - x));
                else
                    return (short)(3 * y + 2 * (x - y));
            }
            case DIAMOND:
            case OCTAHEDRON:
                return (short)(2 * (x + y));
            default:
                return (short)(2 * Math.max(x, y));
        }
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

    public class LOSUnit implements Callable<Long>
    {
        protected int index;
        public LOSUnit(int index)
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
            return storeCellLOS(index);
        }
    }
}
