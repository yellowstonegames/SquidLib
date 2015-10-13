package squidpony.squidgrid;

import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.ShortVLA;

import java.util.ArrayList;
import java.util.Arrays;
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
    protected int mapLimit;
    protected int limit;
    protected double[][] resMap;
    protected Radius radiusKind;

    protected short[][][] cache;
    protected short[][][] tmpCache;
    protected short[][] losCache;
    protected boolean complete;
    protected FOV fov;
    protected short[][] ALL_WALLS;
    private double[][] atan2Cache, directionAngles;
    private short[][] distanceCache;
    private Coord[][] waves;
    protected final int NUM_THREADS;
    protected ExecutorService executor;
    protected double fovPermissiveness;
    private static final double HALF_PI = Math.PI * 0.5, QUARTER_PI = Math.PI * 0.25125,
            SLIVER_PI = Math.PI * 0.05, PI2 = Math.PI * 2;
    public FOVCache(char[][] map, int maxRadius, Radius radiusKind)
    {
        if(map == null || map.length == 0)
            throw new UnsupportedOperationException("The map used by FOVCache must not be null or empty");
        NUM_THREADS = 8;
        executor = Executors.newFixedThreadPool(NUM_THREADS);
        width = map.length;
        height = map[0].length;
        if(width > 256 || height > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently cache, aborting");
        mapLimit = width * height;
        if(maxRadius <= 0 || maxRadius >= 63)
            throw new UnsupportedOperationException("FOV radius is incorrect. Must be 0 < maxRadius < 63");
        this.fov = new FOV(FOV.SHADOW);
        resMap = DungeonUtility.generateResistances(map);
        this.maxRadius = maxRadius;
        this.radiusKind = radiusKind;
        fovPermissiveness = 0.9;
        cache = new short[mapLimit][][];
        tmpCache = new short[mapLimit][][];
        losCache = new short[mapLimit][];
        ALL_WALLS = new short[maxRadius + 1][];
        for (int i = 0; i < maxRadius + 1; i++) {
            ALL_WALLS[i] = ALL_WALL;
        }
        limit = 0x10000;
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
        preloadMeasurements();
        complete = false;
    }
    public FOVCache(char[][] map, int maxRadius, Radius radiusKind, int threadCount)
    {
        if(map == null || map.length == 0)
            throw new UnsupportedOperationException("The map used by FOVCache must not be null or empty");
        NUM_THREADS = threadCount;
        executor = Executors.newFixedThreadPool(NUM_THREADS);
        width = map.length;
        height = map[0].length;
        if(width > 256 || height > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently cache, aborting");
        mapLimit = width * height;
        if(maxRadius <= 0 || maxRadius >= 63)
            throw new UnsupportedOperationException("FOV radius is incorrect. Must be 0 < maxRadius < 63");
        this.fov = new FOV(FOV.SHADOW);
        resMap = DungeonUtility.generateResistances(map);
        this.maxRadius = maxRadius;
        this.radiusKind = radiusKind;
        fovPermissiveness = 0.9;
        cache = new short[mapLimit][][];
        tmpCache = new short[mapLimit][][];
        losCache = new short[mapLimit][];
        ALL_WALLS = new short[maxRadius][];
        for (int i = 0; i < maxRadius; i++) {
            ALL_WALLS[i] = ALL_WALL;
        }
        limit = 0x10000;
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
        preloadMeasurements();
        complete = false;
    }

    public FOVCache(char[][] map, int maxRadius, Radius radiusKind, int threadCount, double permissiveness)
    {
        if(map == null || map.length == 0)
            throw new UnsupportedOperationException("The map used by FOVCache must not be null or empty");
        NUM_THREADS = threadCount;
        executor = Executors.newFixedThreadPool(NUM_THREADS);
        width = map.length;
        height = map[0].length;
        if(width > 256 || height > 256)
            throw new UnsupportedOperationException("Map size is too large to efficiently cache, aborting");
        mapLimit = width * height;
        if(maxRadius <= 0 || maxRadius >= 63)
            throw new UnsupportedOperationException("FOV radius is incorrect. Must be 0 < maxRadius < 63");
        this.fov = new FOV(FOV.SHADOW);
        resMap = DungeonUtility.generateResistances(map);
        this.maxRadius = maxRadius;
        this.radiusKind = radiusKind;
        fovPermissiveness = (9 + permissiveness) * 0.1;
        cache = new short[mapLimit][][];
        tmpCache = new short[mapLimit][][];
        losCache = new short[mapLimit][];
        ALL_WALLS = new short[maxRadius][];
        for (int i = 0; i < maxRadius; i++) {
            ALL_WALLS[i] = ALL_WALL;
        }
        limit = 0x10000;
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
                if(atan2Cache[maxRadius + i][maxRadius + j] < 0)
                    atan2Cache[maxRadius + i][maxRadius + j] += PI2;
                if(tmp > 0) {
                    atan2Cache[maxRadius - i][maxRadius + j] = Math.atan2(j, -i);
                    if(atan2Cache[maxRadius - i][maxRadius + j] < 0)
                        atan2Cache[maxRadius - i][maxRadius + j] += PI2;

                    atan2Cache[maxRadius + i][maxRadius - j] = Math.atan2(-j, i);
                    if(atan2Cache[maxRadius + i][maxRadius - j] < 0)
                        atan2Cache[maxRadius + i][maxRadius - j] += PI2;

                    atan2Cache[maxRadius - i][maxRadius - j] = Math.atan2(-j, -i);
                    if(atan2Cache[maxRadius - i][maxRadius - j] < 0)
                        atan2Cache[maxRadius - i][maxRadius - j] += PI2;

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
        directionAngles[0][2] = Math.atan2(-1,1) + PI2;
        directionAngles[1][0] = Math.atan2(1,0);
        directionAngles[1][1] = 0;
        directionAngles[1][2] = Math.atan2(-1,0) + PI2;
        directionAngles[2][0] = Math.atan2(1,-1);
        directionAngles[2][1] = Math.atan2(0,-1);
        directionAngles[2][2] = Math.atan2(-1,-1) + PI2;
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
     * Uses previously cached FOV and .
     * @param index an int that stores the x,y center of FOV as calculated by: x + y * width
     * @return a multi-packed series of progressively wider FOV radii
     */
    protected long storeCellSymmetry(int index) {
        long startTime = System.currentTimeMillis();
        tmpCache[index] = permissiveSymmetry(index % width, index / width);
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
        long on = 0, current = 0;
        ShortVLA[] packing = new ShortVLA[maxRadius];
        int[] skip = new int[maxRadius];
        short x, y;
        short[][] packed = new short[maxRadius][];
        double[][] fovMap;
        for(int l = 0; l < maxRadius; l++) {

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

    public boolean queryCache(int visionRange, int viewerX, int viewerY, int targetX, int targetY)
    {
        return queryPacked(cache[viewerX + viewerY  * width][maxRadius - visionRange], targetX, targetY);
    }
    public boolean isCellVisible(int visionRange, int viewerX, int viewerY, int targetX, int targetY)
    {
        return queryPacked(cache[viewerX + viewerY  * width][maxRadius - visionRange], targetX, targetY) ||
                queryPacked(cache[targetX + targetY  * width][maxRadius - visionRange], viewerX, viewerY);
    }

    public void cacheAllPerformance() {
        List<LOSUnit> losUnits = new ArrayList<LOSUnit>(mapLimit);
        List<FOVUnit> fovUnits = new ArrayList<FOVUnit>(mapLimit);
        for (int i = 0; i < mapLimit; i++) {
            losUnits.add(new LOSUnit(i));
            fovUnits.add(new FOVUnit(i));
        }
        //long totalTime = System.currentTimeMillis(), threadTime = 0L;

        try {
            final List<Future<Long>> invoke = executor.invokeAll(losUnits);
            for (Future<Long> future : invoke) {
                long t = future.get();
                //threadTime += t;
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
                //threadTime += t;
                //System.out.println(t);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //totalTime = System.currentTimeMillis() - totalTime;
        //System.out.println("Total real time elapsed: " + totalTime);
        //System.out.println("Total CPU time elapsed, on " + NUM_THREADS + " threads: " + threadTime);
        /*
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
        */
        complete = true;
    }

    //needs rewrite, must store the angle a ray traveled at to get around an obstacle, and propagate it to the end of
    //the ray. It should check if the angle theta for a given point is too different from the angle in angleMap.
    public byte[][] waveFOVWIP(int viewerX, int viewerY) {
        byte[][] gradientMap = new byte[width][height];
        double[][] angleMap = new double[2 * maxRadius + 1][2 * maxRadius + 1];
        for (int i = 0; i < angleMap.length; i++) {
            Arrays.fill(angleMap[i], -20);
        }
        gradientMap[viewerX][viewerY] = (byte)(2 * maxRadius);
        Direction[] dirs = (radiusKind == Radius.DIAMOND || radiusKind == Radius.OCTAHEDRON)
                ? Direction.CARDINALS : Direction.OUTWARDS;
        int cx, cy, ccwAdjX, ccwAdjY, cwAdjX, cwAdjY, ccwGridX, ccwGridY, cwGridX, cwGridY;
        Coord pt;
        double theta, angleCW, angleCCW;
        byte dist;
        boolean blockedCCW, blockedCW, isStraightCCW;
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

                            //positive x, postive y (lower on screen), closer to x-axis
                            case 0:
                                cwAdjX = pt.x - 1;
                                cwAdjY = pt.y;
                                angleCW = directionAngles[0][1];
                                isStraightCCW = false;
                                ccwAdjX = pt.x - 1;
                                ccwAdjY = pt.y - 1;
                                angleCCW = directionAngles[0][0];
                                break;
                            //positive x, postive y (lower on screen), closer to y-axis
                            case 1:
                                cwAdjX = pt.x - 1;
                                cwAdjY = pt.y - 1;
                                angleCW = directionAngles[0][0];
                                ccwAdjX = pt.x;
                                ccwAdjY = pt.y - 1;
                                angleCCW = directionAngles[1][0];
                                isStraightCCW = true;
                                break;
                            //negative x, postive y (lower on screen), closer to y-axis
                            case 2:
                                cwAdjX = pt.x;
                                cwAdjY = pt.y - 1;
                                angleCW = directionAngles[1][0];
                                isStraightCCW = false;
                                ccwAdjX = pt.x + 1;
                                ccwAdjY = pt.y - 1;
                                angleCCW = directionAngles[2][0];
                                break;
                            //negative x, postive y (lower on screen), closer to x-axis
                            case 3:
                                cwAdjX = pt.x + 1;
                                cwAdjY = pt.y - 1;
                                angleCW = directionAngles[2][0];
                                ccwAdjX = pt.x + 1;
                                ccwAdjY = pt.y;
                                angleCCW = directionAngles[2][1];
                                isStraightCCW = true;
                                break;

                            //negative x, negative y (higher on screen), closer to x-axis
                            case 4:
                                cwAdjX = pt.x + 1;
                                cwAdjY = pt.y + 1;
                                angleCW = directionAngles[2][2];
                                ccwAdjX = pt.x + 1;
                                ccwAdjY = pt.y;
                                angleCCW = directionAngles[2][1];
                                isStraightCCW = false;
                                break;
                            //negative x, negative y (higher on screen), closer to y-axis
                            case 5:
                                cwAdjX = pt.x + 1;
                                cwAdjY = pt.y + 1;
                                angleCW = directionAngles[2][2];
                                ccwAdjX = pt.x;
                                ccwAdjY = pt.y + 1;
                                angleCCW = directionAngles[1][2];
                                isStraightCCW = true;
                                break;
                            //positive x, negative y (higher on screen), closer to y-axis
                            case 6:
                                cwAdjX = pt.x;
                                cwAdjY = pt.y + 1;
                                angleCW = directionAngles[1][2];
                                isStraightCCW = false;
                                ccwAdjX = pt.x - 1;
                                ccwAdjY = pt.y + 1;
                                angleCCW = directionAngles[0][2];
                                break;
                            //positive x, negative y (higher on screen), closer to x-axis
                            default:
                                cwAdjX = pt.x - 1;
                                cwAdjY = pt.y + 1;
                                angleCW = directionAngles[0][2];
                                ccwAdjX = pt.x - 1;
                                ccwAdjY = pt.y;
                                angleCCW = directionAngles[0][1];
                                isStraightCCW = true;
                                break;
                        }
                        /*
                        angleCCW = (((Math.abs(atan2Cache[ccwAdjX][ccwAdjY] - angleCCW) > Math.PI)
                                ? atan2Cache[ccwAdjX][ccwAdjY] + angleCCW + PI2
                                : atan2Cache[ccwAdjX][ccwAdjY] + angleCCW)
                                * 0.5) % PI2;
                                //(angleCCW + atan2Cache[ccwAdjX][ccwAdjY]) * 0.5;
                        angleCW = (((Math.abs(atan2Cache[cwAdjX][cwAdjY] - angleCW) > Math.PI)
                                ? atan2Cache[cwAdjX][cwAdjY] + angleCW + PI2
                                : atan2Cache[cwAdjX][cwAdjY] + angleCW)
                                * 0.5) % PI2;
                                //(angleCW + atan2Cache[cwAdjX][cwAdjY]) * 0.5;

                         */
                        angleCCW = atan2Cache[ccwAdjX][ccwAdjY];
                        //(angleCCW + atan2Cache[ccwAdjX][ccwAdjY]) * 0.5;
                        angleCW = atan2Cache[cwAdjX][cwAdjY];
                        //(angleCW + atan2Cache[cwAdjX][cwAdjY]) * 0.5;


                        cwGridX = cwAdjX + viewerX - maxRadius;
                        ccwGridX = ccwAdjX + viewerX - maxRadius;
                        cwGridY = cwAdjY + viewerY - maxRadius;
                        ccwGridY = ccwAdjY + viewerY - maxRadius;

                        blockedCW = cwGridX >= width || cwGridY >= height || cwGridX < 0 || cwGridY < 0 ||
                                resMap[cwGridX][cwGridY] > 0.5 ||
                                angleMap[cwAdjX][cwAdjY] >= PI2;
                        blockedCCW = ccwGridX >= width || ccwGridY >= height || ccwGridX < 0 || ccwGridY < 0 ||
                                resMap[ccwGridX][ccwGridY] > 0.5 ||
                                angleMap[ccwAdjX][ccwAdjY] >= PI2;

                        if (blockedCW && blockedCCW) {
                            angleMap[pt.x][pt.y] = PI2;
                            continue;
                        }
                        if (theta % (HALF_PI - 0.00125) < 0.005)
                            if (isStraightCCW) {
                                if (blockedCCW) {
                                    angleMap[pt.x][pt.y] = PI2;
                                    gradientMap[cx][cy] = dist;
                                    continue;
                                }
                                else
                                    angleMap[pt.x][pt.y] = theta;
                            } else {
                                if (blockedCW) {
                                    angleMap[pt.x][pt.y] = PI2;
                                    gradientMap[cx][cy] = dist;
                                    continue;
                                }
                                else
                                    angleMap[pt.x][pt.y] = theta;
                            }
                        else if(theta % (QUARTER_PI  - 0.0025) < 0.005)
                            if (isStraightCCW) {
                                if (blockedCW) {
                                    angleMap[pt.x][pt.y] = PI2;
                                    gradientMap[cx][cy] = dist;
                                    continue;
                                }
                                else
                                    angleMap[pt.x][pt.y] = theta;
                            } else {
                                if (blockedCCW) {
                                    angleMap[pt.x][pt.y] = PI2;
                                    gradientMap[cx][cy] = dist;
                                    continue;
                                }
                                else
                                    angleMap[pt.x][pt.y] = theta;
                            }
                        else {
                            if (blockedCW) {
                                angleMap[pt.x][pt.y] = angleMap[ccwAdjX][ccwAdjY];
//                                angleMap[pt.x][pt.y] = Math.max(angleMap[ccwAdjX][ccwAdjY],
//                                (theta - (angleCCW - theta + PI2) % PI2 * 0.5 + PI2) % PI2);
//                                        (theta - angleCCW > Math.PI)
//                                                ? (theta - (angleCCW - theta + PI2) * 0.5) % PI2
//                                                : theta - (angleCCW - theta + PI2) % PI2 * 0.5;
                                //angleMap[pt.x][pt.y] = angleCCW;

                                // (((Math.abs(theta - angleCCW) > Math.PI)
                                //        ? theta + angleCCW + PI2
                                //        : theta + angleCCW)
                                //        * 0.5) % PI2;
                                //angleMap[cwAdjX - viewerX + maxRadius][cwAdjY - viewerY + maxRadius];
                                //Math.abs(angleMap[cwAdjX - viewerX + maxRadius][cwAdjY - viewerY + maxRadius] -

                                //angleMap[pt.x][pt.y] = Math.abs(theta - angleCCW) +
                                //        angleMap[cwAdjX - viewerX + maxRadius][cwAdjY - viewerY + maxRadius];

                            } else if (blockedCCW) {
                                angleMap[pt.x][pt.y] = angleMap[cwAdjX][cwAdjY];
//                                        angleMap[pt.x][pt.y] = Math.max(angleMap[cwAdjX][cwAdjY],
//                                        (theta + (theta - angleCW + PI2) % PI2 * 0.5 + PI2) % PI2);
//                                        (angleCW - theta > Math.PI)
//                                                ? theta + (theta - angleCW + PI2) % PI2 * 0.5
//                                                : (theta + (theta - angleCW + PI2) * 0.5) % PI2;
                                        //angleCW;

                                //angleMap[ccwAdjX - viewerX + maxRadius][ccwAdjY - viewerY + maxRadius];
                                //angleMap[ccwAdjX - viewerX + maxRadius][ccwAdjY - viewerY + maxRadius]
                            }
                            else
                            {
                                double cwTemp = angleMap[cwAdjX][cwAdjY], ccwTemp = angleMap[ccwAdjX][ccwAdjY];
                                if(cwTemp < 0)
                                    cwTemp = (atan2Cache[cwAdjX][cwAdjY]);
                                if(ccwTemp < 0)
                                    ccwTemp = (atan2Cache[ccwAdjX][ccwAdjY]);
                                if(cwTemp != atan2Cache[cwAdjX][cwAdjY] &&
                                        ccwTemp != atan2Cache[ccwAdjX][ccwAdjY])
                                    angleMap[pt.x][pt.y] = 0.5 * (cwTemp + ccwTemp);
                                else if(ccwTemp != atan2Cache[ccwAdjX][ccwAdjY])
                                    angleMap[pt.x][pt.y] = ccwTemp;
                                else if(cwTemp != atan2Cache[cwAdjX][cwAdjY])
                                    angleMap[pt.x][pt.y] = cwTemp;
                                else
                                    angleMap[pt.x][pt.y] = theta;
                            }
                            /*

                            else if (!blockedCW)
                                angleMap[pt.x][pt.y] = (angleMap[cwAdjX][cwAdjY] != atan2Cache[cwAdjX][cwAdjY])
                                        ? angleMap[cwAdjX][cwAdjY]
                                        : theta;
                            else
                                angleMap[pt.x][pt.y] = (angleMap[ccwAdjX][ccwAdjY] != atan2Cache[ccwAdjX][ccwAdjY])
                                        ? angleMap[ccwAdjX][ccwAdjY]
                                        : theta;
                             */
                        }
                        if(Math.abs(angleMap[pt.x][pt.y] - theta) <= 0.001 || resMap[pt.x][pt.y] > 0.5)
                            gradientMap[cx][cy] = dist;
                        else
                            angleMap[pt.x][pt.y] = PI2 * 2;
                    }
                }
            }
        }

        return gradientMap;
    }

    public byte[][] waveFOV(int viewerX, int viewerY) {
        byte[][] gradientMap = new byte[width][height];
        double[][] angleMap = new double[2 * maxRadius + 1][2 * maxRadius + 1];
        gradientMap[viewerX][viewerY] = (byte)(2 * maxRadius);
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
                            case 4:
                                nearCWx = pt.x + 1;
                                nearCWy = pt.y;
                                angleCW = -directionAngles[2][1];
                                straight = angleCW;
                                nearCCWx = pt.x + 1;
                                nearCCWy = pt.y + 1;
                                angleCCW = directionAngles[2][2];

                                break;
                            //negative x, negative y, closer to y-axis
                            case 5:
                                nearCWx = pt.x;
                                nearCWy = pt.y + 1;
                                angleCW = directionAngles[1][2];
                                straight = angleCW;
                                nearCCWx = pt.x + 1;
                                nearCCWy = pt.y + 1;
                                angleCCW = directionAngles[2][2];
                                break;
                            //positive x, negative y, closer to y-axis
                            case 6:
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
                                angleMap[pt.x][pt.y] += 0.5 *
                                        angleMap[nearCWx - viewerX + maxRadius][nearCWy - viewerY + maxRadius];
                            if (!blockedCCW)
                                angleMap[pt.x][pt.y] += 0.5 *
                                        angleMap[nearCCWx - viewerX + maxRadius][nearCCWy - viewerY + maxRadius];
                        }
                        if(angleMap[pt.x][pt.y] <= fovPermissiveness)
                            gradientMap[cx][cy] = dist;
                        else
                            angleMap[pt.x][pt.y] = PI2;
                    }
                }
            }
        }

        return gradientMap;
    }

    public short[][] permissiveSymmetry(int viewerX, int viewerY) {
        if(!complete) throw new IllegalStateException(
                "cacheAllPerformance() must be called before permissiveSymmetry() to fill the cache.");
        if (viewerX < 0 || viewerY < 0 || viewerX >= width || viewerY >= height)
            return ALL_WALLS;
        if (resMap[viewerX][viewerY] >= 1.0) {
            return ALL_WALLS;
        }
        short myHilbert = (short)posToHilbert(viewerX, viewerY);
        ShortVLA packing = new ShortVLA(128);
        short[][] packed = new short[maxRadius + 1][], cached = cache[viewerX + viewerY * width];
        short[] knownSeen;
        for (int l = 0; l < maxRadius + 1; l++) {
            packing.clear();
            knownSeen = allPackedHilbert(cached[maxRadius - l]);
            Arrays.sort(knownSeen);
            for (int x = Math.max(0, viewerX - l); x <= Math.min(viewerX + l, width - 1); x++) {
                for (int y = Math.max(0, viewerY - l); y <= Math.min(viewerY + l, height - 1); y++) {
                    if(cache[x + y * width] == ALL_WALLS)
                        continue;
                    if (radiusKind.radius(viewerX, viewerY, x, y) > l)
                        continue;
                    short i = (short) posToHilbert(x, y);
                    if (Arrays.binarySearch(knownSeen, i) >= 0)
                        continue;
                    if (queryPackedHilbert(cache[x + y * width][maxRadius - l], myHilbert))
                        packing.add(i);
                }
            }
            packed[maxRadius - l] = insertSeveralPacked(cached[maxRadius - l], packing.shrink());
        }
        return packed;
    }

    public void cacheAllQuality()
    {
        long totalTime = System.currentTimeMillis(), threadTime = 0L;
        if(!complete)
            cacheAllPerformance();
        List<SymmetryUnit> symUnits = new ArrayList<SymmetryUnit>(mapLimit);
        for (int i = 0; i < mapLimit; i++) {
            symUnits.add(new SymmetryUnit(i));
        }

        try {
            final List<Future<Long>> invoke = executor.invokeAll(symUnits);
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
        cache = tmpCache;
        totalTime = System.currentTimeMillis() - totalTime;
        System.out.println("Total real time elapsed : " + totalTime);
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
    public class SymmetryUnit implements Callable<Long>
    {
        protected int index;
        public SymmetryUnit(int index)
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
            return storeCellSymmetry(index);
        }
    }
}
