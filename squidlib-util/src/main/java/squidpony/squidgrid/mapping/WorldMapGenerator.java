package squidpony.squidgrid.mapping;

import squidpony.LZSPlus;
import squidpony.annotation.Beta;
import squidpony.squidmath.*;
import squidpony.squidmath.Noise.Noise2D;
import squidpony.squidmath.Noise.Noise3D;
import squidpony.squidmath.Noise.Noise4D;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Can be used to generate world maps with a wide variety of data, starting with height, temperature and moisture.
 * From there, you can determine biome information in as much detail as your game needs, with default implementations
 * available; one assigns a single biome to each cell based on heat/moisture, and the other gives a gradient between two
 * biome types for every cell. The maps this produces with {@link SphereMap} are valid for spherical world projections,
 * while the maps from {@link TilingMap} are for toroidal world projections and will wrap from edge to opposite edge
 * seamlessly thanks to <a href="https://www.gamedev.net/blog/33/entry-2138456-seamless-noise/">a technique from the
 * Accidental Noise Library</a> that involves getting a 2D slice of 4D Simplex noise. Because of how Simplex noise
 * works, this also allows extremely high zoom levels for all types of map as long as certain parameters are within
 * reason. Other world maps produce more conventional shapes, like {@link SpaceViewMap} and {@link RotatingSpaceMap}
 * make a view of a marble-like world from space, and others make more unconventional shapes, like {@link EllipticalMap}
 * or {@link EllipticalHammerMap}, which form a 2:1 ellipse shape that accurately keeps sizes but not relative shapes,
 * {@link RoundSideMap}, which forms a pill-shape, and {@link HyperellipticalMap}, which takes parameters so it can fit
 * any shape between a circle or ellipse and a rectangle (the default is a slightly squared-off ellipse). You can access
 * the height map with the {@link #heightData} field, the heat map with the {@link #heatData} field, the moisture map
 * with the {@link #moistureData} field, and a special map that stores ints representing the codes for various ranges of
 * elevation (0 to 8 inclusive, with 0 the deepest ocean and 8 the highest mountains) with {@link #heightCodeData}. The
 * last map should be noted as being the simplest way to find what is land and what is water; any height code 4 or
 * greater is land, and any height code 3 or less is water.
 * <br>
 * Biome mapping is likely to need customization per-game, but some good starting points are {@link SimpleBiomeMapper},
 * which stores one biome per cell, and {@link DetailedBiomeMapper}, which gives each cell a midway value between two
 * biomes.
 */
@Beta
public abstract class WorldMapGenerator implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int width, height;
    public int seedA, seedB, cacheA, cacheB;
    public GWTRNG rng;
    public final double[][] heightData, heatData, moistureData;
    public final GreasedRegion landData;
    public final int[][] heightCodeData;
    public double landModifier = -1.0, coolingModifier = 1.0,
            minHeight = Double.POSITIVE_INFINITY, maxHeight = Double.NEGATIVE_INFINITY,
            minHeightActual = Double.POSITIVE_INFINITY, maxHeightActual = Double.NEGATIVE_INFINITY,
            minHeat = Double.POSITIVE_INFINITY, maxHeat = Double.NEGATIVE_INFINITY,
            minWet = Double.POSITIVE_INFINITY, maxWet = Double.NEGATIVE_INFINITY;
    protected double centerLongitude = 0.0;

    /**
     * Gets the longitude line the map is centered on, which should usually be between 0 and 2 * PI.
     * @return the longitude line the map is centered on, in radians from 0 to 2 * PI
     */
    public double getCenterLongitude() {
        return centerLongitude;
    }

    /**
     * Sets the center longitude line to a longitude measured in radians, from 0 to 2 * PI. Positive arguments will be
     * corrected with modulo, but negative ones may not always act as expected, and are strongly discouraged.
     * @param centerLongitude the longitude to center the map projection on, from 0 to 2 * PI (can be any non-negative double).
     */
    public void setCenterLongitude(double centerLongitude) {
        this.centerLongitude = centerLongitude % 6.283185307179586;
    }

    public int zoom = 0, startX = 0, startY = 0, usedWidth, usedHeight;
    protected IntVLA startCacheX = new IntVLA(8), startCacheY = new IntVLA(8);
    protected int zoomStartX = 0, zoomStartY = 0;
    public static final double
            deepWaterLower = -1.0, deepWaterUpper = -0.7,        // 0
            mediumWaterLower = -0.7, mediumWaterUpper = -0.3,    // 1
            shallowWaterLower = -0.3, shallowWaterUpper = -0.1,  // 2
            coastalWaterLower = -0.1, coastalWaterUpper = 0.02,   // 3
            sandLower = 0.02, sandUpper = 0.12,                   // 4
            grassLower = 0.14, grassUpper = 0.35,                // 5
            forestLower = 0.35, forestUpper = 0.6,               // 6
            rockLower = 0.6, rockUpper = 0.8,                    // 7
            snowLower = 0.8, snowUpper = 1.0;                    // 8

    /**
     * Arc sine approximation with fairly low error while still being faster than {@link NumberTools#sin(double)}.
     * This formula is number 201 in <a href=">http://www.fastcode.dk/fastcodeproject/articles/index.htm">Dennis
     * Kjaer Christensen's unfinished math work on arc sine approximation</a>. This method is about 40 times faster
     * than {@link Math#asin(double)}.
     * @param a an input to the inverse sine function, from -1 to 1 inclusive (error is higher approaching -1 or 1)
     * @return an output from the inverse sine function, from -PI/2 to PI/2 inclusive.
     */
    protected static double asin(double a) {
        return (a * (1.0 + (a *= a) * (-0.141514171442891431 + a * -0.719110791477959357))) /
                (1.0 + a * (-0.439110389941411144 + a * -0.471306172023844527));
    }

    protected static double removeExcess(double radians)
    {
        radians *= 0.6366197723675814;
        final int floor = (radians >= 0.0 ? (int) radians : (int) radians - 1);
        return (radians - (floor & -2) - ((floor & 1) << 1)) * (Math.PI);
//        if(radians < -Math.PI || radians > Math.PI)
//            System.out.println("UH OH, radians produced: " + radians);
//        if(Math.random() < 0.00001)
//            System.out.println(radians);
//        return radians;

    }
    /**
     * Constructs a WorldMapGenerator (this class is abstract, so you should typically call this from a subclass or as
     * part of an anonymous class that implements {@link #regenerate(int, int, int, int, double, double, int, int)}).
     * Always makes a 256x256 map. If you were using {@link WorldMapGenerator#WorldMapGenerator(long, int, int)}, then
     * this would be the same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 256}.
     */
    protected WorldMapGenerator()
    {
        this(0x1337BABE1337D00DL, 256, 256);
    }
    /**
     * Constructs a WorldMapGenerator (this class is abstract, so you should typically call this from a subclass or as
     * part of an anonymous class that implements {@link #regenerate(int, int, int, int, double, double, int, int)}).
     * Takes only the width/height of the map. The initial seed is set to the same large long
     * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
     * height of the map cannot be changed after the fact, but you can zoom in.
     *
     * @param mapWidth the width of the map(s) to generate; cannot be changed later
     * @param mapHeight the height of the map(s) to generate; cannot be changed later
     */
    protected WorldMapGenerator(int mapWidth, int mapHeight)
    {
        this(0x1337BABE1337D00DL, mapWidth, mapHeight);
    }
    /**
     * Constructs a WorldMapGenerator (this class is abstract, so you should typically call this from a subclass or as
     * part of an anonymous class that implements {@link #regenerate(int, int, int, int, double, double, int, int)}).
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
     * The width and height of the map cannot be changed after the fact, but you can zoom in.
     *
     * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
     * @param mapWidth the width of the map(s) to generate; cannot be changed later
     * @param mapHeight the height of the map(s) to generate; cannot be changed later
     */
    protected WorldMapGenerator(long initialSeed, int mapWidth, int mapHeight)
    {
        width = mapWidth;
        height = mapHeight;
        usedWidth = width;
        usedHeight = height;
        seedA = (int) (initialSeed & 0xFFFFFFFFL);
        seedB = (int) (initialSeed >>> 32);
        cacheA = ~seedA;
        cacheB = ~seedB;
        rng = new GWTRNG(seedA, seedB);
        heightData = new double[width][height];
        heatData = new double[width][height];
        moistureData = new double[width][height];
        landData = new GreasedRegion(width, height);
//        riverData = new GreasedRegion(width, height);
//        lakeData = new GreasedRegion(width, height);
//        partialRiverData = new GreasedRegion(width, height);
//        partialLakeData = new GreasedRegion(width, height);
//        workingData = new GreasedRegion(width, height);
        heightCodeData = new int[width][height];
    }

    /**
     * Generates a world using a random RNG state and all parameters randomized.
     * The worlds this produces will always have width and height as specified in the constructor (default 256x256).
     * You can call {@link #zoomIn(int, int, int)} to double the resolution and center on the specified area, but the width
     * and height of the 2D arrays this changed, such as {@link #heightData} and {@link #moistureData} will be the same.
     */
    public void generate()
    {
        generate(rng.nextLong());
    }

    /**
     * Generates a world using the specified RNG state as a long. Other parameters will be randomized, using the same
     * RNG state to start with.
     * The worlds this produces will always have width and height as specified in the constructor (default 256x256).
     * You can call {@link #zoomIn(int, int, int)} to double the resolution and center on the specified area, but the width
     * and height of the 2D arrays this changed, such as {@link #heightData} and {@link #moistureData} will be the same.
     * @param state the state to give this generator's RNG; if the same as the last call, this will reuse data
     */
    public void generate(long state) {
        generate(-1.0, -1.0, state);
    }

    /**
     * Generates a world using the specified RNG state as a long, with specific water and cooling modifiers that affect
     * the land-water ratio and the average temperature, respectively.
     * The worlds this produces will always have width and height as specified in the constructor (default 256x256).
     * You can call {@link #zoomIn(int, int, int)} to double the resolution and center on the specified area, but the width
     * and height of the 2D arrays this changed, such as {@link #heightData} and {@link #moistureData} will be the same.
     * @param landMod 1.0 is Earth-like, less than 1 is more-water, more than 1 is more-land; a random value will be used if this is negative
     * @param coolMod 1.125 is Earth-like, less than 1 is cooler, more than 1 is hotter; a random value will be used if this is negative
     * @param state the state to give this generator's RNG; if the same as the last call, this will reuse data
     */
    public void generate(double landMod, double coolMod, long state)
    {
        if(cacheA != (int) (state & 0xFFFFFFFFL) || cacheB != (int) (state >>> 32) ||
                landMod != landModifier || coolMod != coolingModifier)
        {
            seedA = (int) (state & 0xFFFFFFFFL);
            seedB = (int) (state >>> 32);
            zoom = 0;
            startCacheX.clear();
            startCacheY.clear();
            startCacheX.add(0);
            startCacheY.add(0);
            zoomStartX = width >> 1;
            zoomStartY = height >> 1;

        }
        //System.out.printf("generate, zoomStartX: %d, zoomStartY: %d\n", zoomStartX, zoomStartY);

        regenerate(startX = (zoomStartX >> zoom) - (width >> 1 + zoom), startY = (zoomStartY >> zoom) - (height >> 1 + zoom),
                //startCacheX.peek(), startCacheY.peek(),
                usedWidth = (width >> zoom), usedHeight = (height >> zoom), landMod, coolMod, seedA, seedB);
    }

    /**
     * Halves the resolution of the map and doubles the area it covers; the 2D arrays this uses keep their sizes. This
     * version of zoomOut always zooms out from the center of the currently used area.
     * <br>
     * Only has an effect if you have previously zoomed in using {@link #zoomIn(int, int, int)} or its overload.
     */
    public void zoomOut()
    {
        zoomOut(1, width >> 1, height >> 1);
    }
    /**
     * Halves the resolution of the map and doubles the area it covers repeatedly, halving {@code zoomAmount} times; the
     * 2D arrays this uses keep their sizes. This version of zoomOut allows you to specify where the zoom should be
     * centered, using the current coordinates (if the map size is 256x256, then coordinates should be between 0 and
     * 255, and will refer to the currently used area and not necessarily the full world size).
     * <br>
     * Only has an effect if you have previously zoomed in using {@link #zoomIn(int, int, int)} or its overload.
     * @param zoomCenterX the center X position to zoom out from; if too close to an edge, this will stop moving before it would extend past an edge
     * @param zoomCenterY the center Y position to zoom out from; if too close to an edge, this will stop moving before it would extend past an edge
     */
    public void zoomOut(int zoomAmount, int zoomCenterX, int zoomCenterY)
    {
        zoomAmount = Math.min(zoom, zoomAmount);
        if(zoomAmount == 0) return;
        if(zoomAmount < 0) {
            zoomIn(-zoomAmount, zoomCenterX, zoomCenterY);
            return;
        }
        if(zoom > 0)
        {
            if(cacheA != seedA || cacheB != seedB)
            {
                generate(rng.nextLong());
            }
            zoomStartX = Math.min(Math.max(
                    (zoomStartX + (zoomCenterX - (width >> 1))) >> zoomAmount,
                    width >> 1), (width << zoom - zoomAmount) - (width >> 1));
            zoomStartY = Math.min(Math.max(
                    (zoomStartY + (zoomCenterY - (height >> 1))) >> zoomAmount,
                    height >> 1), (height << zoom - zoomAmount) - (height >> 1));
//            System.out.printf("zoomOut, zoomStartX: %d, zoomStartY: %d\n", zoomStartX, zoomStartY);
            zoom -= zoomAmount;
            startCacheX.pop();
            startCacheY.pop();
            startCacheX.add(Math.min(Math.max(startCacheX.pop() + (zoomCenterX >> zoom + 1) - (width >> zoom + 2),
                    0), width - (width >> zoom)));
            startCacheY.add(Math.min(Math.max(startCacheY.pop() + (zoomCenterY >> zoom + 1) - (height >> zoom + 2),
                    0), height - (height >> zoom)));
//            zoomStartX = Math.min(Math.max((zoomStartX >> 1) + (zoomCenterX >> zoom + 1) - (width >> zoom + 2),
//                    0), width - (width >> zoom));
//            zoomStartY = Math.min(Math.max((zoomStartY >> 1) + (zoomCenterY >> zoom + 1) - (height >> zoom + 2),
//                    0), height - (height >> zoom));
            regenerate(startX = (zoomStartX >> zoom) - (width >> zoom + 1), startY = (zoomStartY >> zoom) - (height >> zoom + 1),
                    //startCacheX.peek(), startCacheY.peek(),
                    usedWidth = width >> zoom,  usedHeight = height >> zoom,
                    landModifier, coolingModifier, cacheA, cacheB);
            rng.setState(cacheA, cacheB);
        }

    }
    /**
     * Doubles the resolution of the map and halves the area it covers; the 2D arrays this uses keep their sizes. This
     * version of zoomIn always zooms in to the center of the currently used area.
     * <br>
     * Although there is no technical restriction on maximum zoom, zooming in more than 5 times (64x scale or greater)
     * will make the map appear somewhat less realistic due to rounded shapes appearing more bubble-like and less like a
     * normal landscape.
     */
    public void zoomIn()
    {
        zoomIn(1, width >> 1, height >> 1);
    }
    /**
     * Doubles the resolution of the map and halves the area it covers repeatedly, doubling {@code zoomAmount} times;
     * the 2D arrays this uses keep their sizes. This version of zoomIn allows you to specify where the zoom should be
     * centered, using the current coordinates (if the map size is 256x256, then coordinates should be between 0 and
     * 255, and will refer to the currently used area and not necessarily the full world size).
     * <br>
     * Although there is no technical restriction on maximum zoom, zooming in more than 5 times (64x scale or greater)
     * will make the map appear somewhat less realistic due to rounded shapes appearing more bubble-like and less like a
     * normal landscape.
     * @param zoomCenterX the center X position to zoom in to; if too close to an edge, this will stop moving before it would extend past an edge
     * @param zoomCenterY the center Y position to zoom in to; if too close to an edge, this will stop moving before it would extend past an edge
     */
    public void zoomIn(int zoomAmount, int zoomCenterX, int zoomCenterY)
    {
        if(zoomAmount == 0) return;
        if(zoomAmount < 0)
        {
            zoomOut(-zoomAmount, zoomCenterX, zoomCenterY);
            return;
        }
        if(seedA != cacheA || seedB != cacheB)
        {
            generate(rng.nextLong());
        }
        zoomStartX = Math.min(Math.max(
                (zoomStartX + zoomCenterX - (width >> 1) << zoomAmount),
                width >> 1), (width << zoom + zoomAmount) - (width >> 1));
//        int oldZoomY = zoomStartY;
        zoomStartY = Math.min(Math.max(
                (zoomStartY + zoomCenterY - (height >> 1) << zoomAmount),
                height >> 1), (height << zoom + zoomAmount) - (height >> 1));
//        System.out.printf("zoomIn, zoomStartX: %d, zoomStartY: %d, oldZoomY: %d, unedited: %d, upperCap: %d\n", zoomStartX, zoomStartY,
//                oldZoomY, (oldZoomY + zoomCenterY - (height >> 1) << zoomAmount), (height << zoom + zoomAmount) - (height >> 1));
        zoom += zoomAmount;
        if(startCacheX.isEmpty())
        {
            startCacheX.add(0);
            startCacheY.add(0);
        }
        else {
            startCacheX.add(Math.min(Math.max(startCacheX.peek() + (zoomCenterX >> zoom - 1) - (width >> zoom + 1),
                    0), width - (width >> zoom)));
            startCacheY.add(Math.min(Math.max(startCacheY.peek() + (zoomCenterY >> zoom - 1) - (height >> zoom + 1),
                    0), height - (height >> zoom)));
        }
        regenerate(startX = (zoomStartX >> zoom) - (width >> 1 + zoom), startY = (zoomStartY >> zoom) - (height >> 1 + zoom),
                //startCacheX.peek(), startCacheY.peek(),
                usedWidth = width >> zoom, usedHeight = height >> zoom,
                landModifier, coolingModifier, cacheA, cacheB);
        rng.setState(cacheA, cacheB);
    }

    protected abstract void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                       double landMod, double coolMod, int stateA, int stateB);
    /**
     * Given a latitude and longitude in radians (the conventional way of describing points on a globe), this gets the
     * (x,y) Coord on the map projection this generator uses that corresponds to the given lat-lon coordinates. If this
     * generator does not represent a globe (if it is toroidal, for instance) or if there is no "good way" to calculate
     * the projection for a given lat-lon coordinate, this returns null. The default implementation always returns null.
     * If this is a supported operation and the parameters are valid, this returns a Coord with x between 0 and
     * {@link #width}, and y between 0 and {@link #height}, both exclusive. Automatically wraps the Coord's values using
     * {@link #wrapX(int, int)} and {@link #wrapY(int, int)}.
     * @param latitude the latitude, from {@code Math.PI * -0.5} to {@code Math.PI * 0.5}
     * @param longitude the longitude, from {@code 0.0} to {@code Math.PI * 2.0}
     * @return the point at the given latitude and longitude, as a Coord with x between 0 and {@link #width} and y between 0 and {@link #height}, or null if unsupported
     */
    public Coord project(double latitude, double longitude)
    {
        return null;
    }

    public int codeHeight(final double high)
    {
        if(high < deepWaterUpper)
            return 0;
        if(high < mediumWaterUpper)
            return 1;
        if(high < shallowWaterUpper)
            return 2;
        if(high < coastalWaterUpper)
            return 3;
        if(high < sandUpper)
            return 4;
        if(high < grassUpper)
            return 5;
        if(high < forestUpper)
            return 6;
        if(high < rockUpper)
            return 7;
        return 8;
    }
    protected final int decodeX(final int coded)
    {
        return coded % width;
    }
    protected final int decodeY(final int coded)
    {
        return coded / width;
    }
    public int wrapX(final int x, final int y)  {
        return (x + width) % width;
    }
    public int wrapY(final int x, final int y)  {
        return (y + height) % height;
    }
//    private static final Direction[] reuse = new Direction[6];
//    private void appendDirToShuffle(RNG rng) {
//        rng.randomPortion(Direction.CARDINALS, reuse);
//        reuse[rng.next(2)] = Direction.DIAGONALS[rng.next(2)];
//        reuse[4] = Direction.DIAGONALS[rng.next(2)];
//        reuse[5] = Direction.OUTWARDS[rng.next(3)];
//    }

//    protected void addRivers()
//    {
//        landData.refill(heightCodeData, 4, 999);
//        long rebuildState = rng.nextLong();
//        //workingData.allOn();
//                //.empty().insertRectangle(8, 8, width - 16, height - 16);
//        riverData.empty().refill(heightCodeData, 6, 100);
//        riverData.quasiRandomRegion(0.0036);
//        int[] starts = riverData.asTightEncoded();
//        int len = starts.length, currentPos, choice, adjX, adjY, currX, currY, tcx, tcy, stx, sty, sbx, sby;
//        riverData.clear();
//        lakeData.clear();
//        PER_RIVER:
//        for (int i = 0; i < len; i++) {
//            workingData.clear();
//            currentPos = starts[i];
//            stx = tcx = currX = decodeX(currentPos);
//            sty = tcy = currY = decodeY(currentPos);
//            while (true) {
//
//                double best = 999999;
//                choice = -1;
//                appendDirToShuffle(rng);
//
//                for (int d = 0; d < 5; d++) {
//                    adjX = wrapX(currX + reuse[d].deltaX);
//                    /*
//                    if (adjX < 0 || adjX >= width)
//                    {
//                        if(rng.next(4) == 0)
//                            riverData.or(workingData);
//                        continue PER_RIVER;
//                    }*/
//                    adjY = wrapY(currY + reuse[d].deltaY);
//                    if (heightData[adjX][adjY] < best && !workingData.contains(adjX, adjY)) {
//                        best = heightData[adjX][adjY];
//                        choice = d;
//                        tcx = adjX;
//                        tcy = adjY;
//                    }
//                }
//                currX = tcx;
//                currY = tcy;
//                if (best >= heightData[stx][sty]) {
//                    tcx = rng.next(2);
//                    adjX = wrapX(currX + ((tcx & 1) << 1) - 1);
//                    adjY = wrapY(currY + (tcx & 2) - 1);
//                    lakeData.insert(currX, currY);
//                    lakeData.insert(wrapX(currX+1), currY);
//                    lakeData.insert(wrapX(currX-1), currY);
//                    lakeData.insert(currX, wrapY(currY+1));
//                    lakeData.insert(currX, wrapY(currY-1));
//
//                    if(heightCodeData[adjX][adjY] <= 3) {
//                        riverData.or(workingData);
//                        continue PER_RIVER;
//                    }
//                    else if((heightData[adjX][adjY] -= 0.0002) < 0.0) {
//                        if (rng.next(3) == 0)
//                            riverData.or(workingData);
//                        continue PER_RIVER;
//                    }
//                    tcx = rng.next(2);
//                    adjX = wrapX(currX + ((tcx & 1) << 1) - 1);
//                    adjY = wrapY(currY + (tcx & 2) - 1);
//                    if(heightCodeData[adjX][adjY] <= 3) {
//                        riverData.or(workingData);
//                        continue PER_RIVER;
//                    }
//                    else if((heightData[adjX][adjY] -= 0.0002) < 0.0) {
//                        if (rng.next(3) == 0)
//                            riverData.or(workingData);
//                        continue PER_RIVER;
//                    }
//                }
//                if(choice != -1 && reuse[choice].isDiagonal())
//                {
//                    tcx = wrapX(currX - reuse[choice].deltaX);
//                    tcy = wrapY(currY - reuse[choice].deltaY);
//                    if(heightData[tcx][currY] <= heightData[currX][tcy] && !workingData.contains(tcx, currY))
//                    {
//                        if(heightCodeData[tcx][currY] < 3 || riverData.contains(tcx, currY))
//                        {
//                            riverData.or(workingData);
//                            continue PER_RIVER;
//                        }
//                        workingData.insert(tcx, currY);
//                    }
//                    else if(!workingData.contains(currX, tcy))
//                    {
//                        if(heightCodeData[currX][tcy] < 3 || riverData.contains(currX, tcy))
//                        {
//                            riverData.or(workingData);
//                            continue PER_RIVER;
//                        }
//                        workingData.insert(currX, tcy);
//
//                    }
//                }
//                if(heightCodeData[currX][currY] < 3 || riverData.contains(currX, currY))
//                {
//                    riverData.or(workingData);
//                    continue PER_RIVER;
//                }
//                workingData.insert(currX, currY);
//            }
//        }
//
//        GreasedRegion tempData = new GreasedRegion(width, height);
//        int riverCount = riverData.size() >> 4, currentMax = riverCount >> 3, idx = 0, prevChoice;
//        for (int h = 5; h < 9; h++) { //, currentMax += riverCount / 18
//            workingData.empty().refill(heightCodeData, h).and(riverData);
//            RIVER:
//            for (int j = 0; j < currentMax && idx < riverCount; j++) {
//                double vdc = VanDerCorputQRNG.weakDetermine(idx++), best = -999999;
//                currentPos = workingData.atFractionTight(vdc);
//                if(currentPos < 0)
//                    break;
//                stx = sbx = tcx = currX = decodeX(currentPos);
//                sty = sby = tcy = currY = decodeY(currentPos);
//                appendDirToShuffle(rng);
//                choice = -1;
//                prevChoice = -1;
//                for (int d = 0; d < 5; d++) {
//                    adjX = wrapX(currX + reuse[d].deltaX);
//                    adjY = wrapY(currY + reuse[d].deltaY);
//                    if (heightData[adjX][adjY] > best) {
//                        best = heightData[adjX][adjY];
//                        prevChoice = choice;
//                        choice = d;
//                        sbx = tcx;
//                        sby = tcy;
//                        tcx = adjX;
//                        tcy = adjY;
//                    }
//                }
//                currX = sbx;
//                currY = sby;
//                if (prevChoice != -1 && heightCodeData[currX][currY] >= 4) {
//                    if (reuse[prevChoice].isDiagonal()) {
//                        tcx = wrapX(currX - reuse[prevChoice].deltaX);
//                        tcy = wrapY(currY - reuse[prevChoice].deltaY);
//                        if (heightData[tcx][currY] <= heightData[currX][tcy]) {
//                            if(heightCodeData[tcx][currY] < 3)
//                            {
//                                riverData.or(tempData);
//                                continue;
//                            }
//                            tempData.insert(tcx, currY);
//                        }
//                        else
//                        {
//                            if(heightCodeData[currX][tcy] < 3)
//                            {
//                                riverData.or(tempData);
//                                continue;
//                            }
//                            tempData.insert(currX, tcy);
//                        }
//                    }
//                    if(heightCodeData[currX][currY] < 3)
//                    {
//                        riverData.or(tempData);
//                        continue;
//                    }
//                    tempData.insert(currX, currY);
//                }
//
//                while (true) {
//                    best = -999999;
//                    appendDirToShuffle(rng);
//                    choice = -1;
//                    for (int d = 0; d < 6; d++) {
//                        adjX = wrapX(currX + reuse[d].deltaX);
//                        adjY = wrapY(currY + reuse[d].deltaY);
//                        if (heightData[adjX][adjY] > best && !riverData.contains(adjX, adjY)) {
//                            best = heightData[adjX][adjY];
//                            choice = d;
//                            sbx = adjX;
//                            sby = adjY;
//                        }
//                    }
//                    currX = sbx;
//                    currY = sby;
//                    if (choice != -1) {
//                        if (reuse[choice].isDiagonal()) {
//                            tcx = wrapX(currX - reuse[choice].deltaX);
//                            tcy = wrapY(currY - reuse[choice].deltaY);
//                            if (heightData[tcx][currY] <= heightData[currX][tcy]) {
//                                if(heightCodeData[tcx][currY] < 3)
//                                {
//                                    riverData.or(tempData);
//                                    continue RIVER;
//                                }
//                                tempData.insert(tcx, currY);
//                            }
//                            else
//                            {
//                                if(heightCodeData[currX][tcy] < 3)
//                                {
//                                    riverData.or(tempData);
//                                    continue RIVER;
//                                }
//                                tempData.insert(currX, tcy);
//                            }
//                        }
//                        if(heightCodeData[currX][currY] < 3)
//                        {
//                            riverData.or(tempData);
//                            continue RIVER;
//                        }
//                        tempData.insert(currX, currY);
//                    }
//                    else
//                    {
//                        riverData.or(tempData);
//                        tempData.clear();
//                        continue RIVER;
//                    }
//                    if (best <= heightData[stx][sty] || heightData[currX][currY] > rng.nextDouble(280.0)) {
//                        riverData.or(tempData);
//                        tempData.clear();
//                        if(heightCodeData[currX][currY] < 3)
//                            continue RIVER;
//                        lakeData.insert(currX, currY);
//                        sbx = rng.next(8);
//                        sbx &= sbx >>> 4;
//                        if ((sbx & 1) == 0)
//                            lakeData.insert(wrapX(currX + 1), currY);
//                        if ((sbx & 2) == 0)
//                            lakeData.insert(wrapX(currX - 1), currY);
//                        if ((sbx & 4) == 0)
//                            lakeData.insert(currX, wrapY(currY + 1));
//                        if ((sbx & 8) == 0)
//                            lakeData.insert(currX, wrapY(currY - 1));
//                        sbx = rng.next(2);
//                        lakeData.insert(wrapX(currX + (-(sbx & 1) | 1)), wrapY(currY + ((sbx & 2) - 1))); // random diagonal
//                        lakeData.insert(currX, wrapY(currY + ((sbx & 2) - 1))); // ortho next to random diagonal
//                        lakeData.insert(wrapX(currX + (-(sbx & 1) | 1)), currY); // ortho next to random diagonal
//
//                        continue RIVER;
//                    }
//                }
//            }
//
//        }
//
//        rng.setState(rebuildState);
//    }

    public interface BiomeMapper
    {
        /**
         * Gets the most relevant biome code for a given x,y point on the map. Some mappers can store more than one
         * biome at a location, but only the one with the highest influence will be returned by this method. Biome codes
         * are always ints, and are typically between 0 and 60, both inclusive; they are meant to be used as indices
         * into a table of names or other objects that identify a biome, accessible via {@link #getBiomeNameTable()}.
         * Although different classes may define biome codes differently, they should all be able to be used as indices
         * into the String array returned by getBiomeNameTable().
         * @param x the x-coordinate on the map
         * @param y the y-coordinate on the map
         * @return an int that can be used as an index into the array returned by {@link #getBiomeNameTable()}
         */
        int getBiomeCode(int x, int y);

        /**
         * Gets a heat code for a given x,y point on a map, usually as an int between 0 and 5 inclusive. Some
         * implementations may use more or less detail for heat codes, but 0 is always the coldest code used, and the
         * highest value this can return for a given implementation refers to the hottest code used.
         * @param x the x-coordinate on the map
         * @param y the y-coordinate on the map
         * @return an int that can be used to categorize how hot an area is, with 0 as coldest
         */
        int getHeatCode(int x, int y);
        /**
         * Gets a moisture code for a given x,y point on a map, usually as an int between 0 and 5 inclusive. Some
         * implementations may use more or less detail for moisture codes, but 0 is always the driest code used, and the
         * highest value this can return for a given implementation refers to the wettest code used. Some
         * implementations may allow seasonal change in moisture, e.g. monsoon seasons, to be modeled differently from
         * average precipitation in an area, but the default assumption is that this describes the average amount of
         * moisture (rain, humidity, and possibly snow/hail or other precipitation) that an area receives annually.
         * @param x the x-coordinate on the map
         * @param y the y-coordinate on the map
         * @return an int that can be used to categorize how much moisture an area tends to receive, with 0 as driest
         */
        int getMoistureCode(int x, int y);

        /**
         * Gets a String array where biome codes can be used as indices to look up a name for the biome they refer to. A
         * sample table is in {@link SimpleBiomeMapper#biomeTable}; the 61-element array format documented for that
         * field is encouraged for implementing classes if they use 6 levels of heat and 6 levels of moisture, and track
         * rivers, coastlines, lakes, and oceans as potentially different types of terrain. Biome codes can be obtained
         * with {@link #getBiomeCode(int, int)}, or for some implementing classes other methods may provide more
         * detailed information.
         * @return a String array that often contains 61 elements, to be used with biome codes as indices.
         */
        String[] getBiomeNameTable();
        /**
         * Analyzes the last world produced by the given WorldMapGenerator and uses all of its generated information to
         * assign biome codes for each cell (along with heat and moisture codes). After calling this, biome codes can be
         * retrieved with {@link #getBiomeCode(int, int)} and used as indices into {@link #getBiomeNameTable()} or a
         * custom biome table.
         * @param world a WorldMapGenerator that should have generated at least one map; it may be at any zoom
         */
        void makeBiomes(WorldMapGenerator world);
    }
    /**
     * A way to get biome information for the cells on a map when you only need a single value to describe a biome, such
     * as "Grassland" or "TropicalRainforest".
     * <br>
     * To use: 1, Construct a SimpleBiomeMapper (constructor takes no arguments). 2, call
     * {@link #makeBiomes(WorldMapGenerator)} with a WorldMapGenerator that has already produced at least one world map.
     * 3, get biome codes from the {@link #biomeCodeData} field, where a code is an int that can be used as an index
     * into the {@link #biomeTable} static field to get a String name for a biome type, or used with an alternate biome
     * table of your design. Biome tables in this case are 61-element arrays organized into groups of 6 elements, with
     * the last element reserved for empty space where the map doesn't cover (as with some map projections). Each
     * group goes from the coldest temperature first to the warmest temperature last in the group. The first group of 6
     * contains the dryest biomes, the next 6 are medium-dry, the next are slightly-dry, the next slightly-wet, then
     * medium-wet, then wettest. After this first block of dry-to-wet groups, there is a group of 6 for coastlines, a
     * group of 6 for rivers, a group of 6 for lakes, a group of 6 for oceans, and then one element for space outside
     * the map. The last element, with code 60, is by convention the String "Empty", but normally the code should be
     * enough to tell that a space is off-map. This also assigns moisture codes and heat codes from 0 to 5 for each
     * cell, which may be useful to simplify logic that deals with those factors.
     */
    public static class SimpleBiomeMapper implements BiomeMapper
    {
        /**
         * The heat codes for the analyzed map, from 0 to 5 inclusive, with 0 coldest and 5 hottest.
         */
        public int[][] heatCodeData,
        /**
         * The moisture codes for the analyzed map, from 0 to 5 inclusive, with 0 driest and 5 wettest.
         */
        moistureCodeData,
        /**
         * The biome codes for the analyzed map, from 0 to 60 inclusive. You can use {@link #biomeTable} to look up
         * String names for biomes, or construct your own table as you see fit (see docs in {@link SimpleBiomeMapper}).
         */
        biomeCodeData;

        @Override
        public int getBiomeCode(int x, int y) {
            return biomeCodeData[x][y];
        }

        @Override
        public int getHeatCode(int x, int y) {
            return heatCodeData[x][y];
        }

        @Override
        public int getMoistureCode(int x, int y) {
            return moistureCodeData[x][y];
        }

        /**
         * Gets a String array where biome codes can be used as indices to look up a name for the biome they refer to.
         * This table uses 6 levels of heat and 6 levels of moisture, and tracks rivers, coastlines, lakes, and oceans
         * as potentially different types of terrain. Biome codes can be obtained with {@link #getBiomeCode(int, int)}.
         * This method returns a direct reference to {@link #biomeTable}, so modifying the returned array is discouraged
         * (you should implement {@link BiomeMapper} using this class as a basis if you want to change its size).
         * @return a direct reference to {@link #biomeTable}, a String array containing names of biomes
         */
        @Override
        public String[] getBiomeNameTable() {
            return biomeTable;
        }

        public static final double
                coldestValueLower = 0.0,   coldestValueUpper = 0.15, // 0
                colderValueLower = 0.15,   colderValueUpper = 0.31,  // 1
                coldValueLower = 0.31,     coldValueUpper = 0.5,     // 2
                warmValueLower = 0.5,      warmValueUpper = 0.69,    // 3
                warmerValueLower = 0.69,   warmerValueUpper = 0.85,  // 4
                warmestValueLower = 0.85,  warmestValueUpper = 1.0,  // 5

        driestValueLower = 0.0,    driestValueUpper  = 0.27, // 0
                drierValueLower = 0.27,    drierValueUpper   = 0.4,  // 1
                dryValueLower = 0.4,       dryValueUpper     = 0.6,  // 2
                wetValueLower = 0.6,       wetValueUpper     = 0.8,  // 3
                wetterValueLower = 0.8,    wetterValueUpper  = 0.9,  // 4
                wettestValueLower = 0.9,   wettestValueUpper = 1.0;  // 5

        /**
         * The default biome table to use with biome codes from {@link #biomeCodeData}. Biomes are assigned based on
         * heat and moisture for the first 36 of 61 elements (coldest to warmest for each group of 6, with the first
         * group as the dryest and the last group the wettest), then the next 6 are for coastlines (coldest to warmest),
         * then rivers (coldest to warmest), then lakes (coldest to warmest), then oceans (coldest to warmest), and
         * lastly a single "biome" for empty space outside the map (meant for projections that don't fill a rectangle).
         */
        public static final String[] biomeTable = {
                //COLDEST //COLDER        //COLD            //HOT                  //HOTTER              //HOTTEST
                "Ice",    "Ice",          "Grassland",      "Desert",              "Desert",             "Desert",             //DRYEST
                "Ice",    "Tundra",       "Grassland",      "Grassland",           "Desert",             "Desert",             //DRYER
                "Ice",    "Tundra",       "Woodland",       "Woodland",            "Savanna",            "Desert",             //DRY
                "Ice",    "Tundra",       "SeasonalForest", "SeasonalForest",      "Savanna",            "Savanna",            //WET
                "Ice",    "Tundra",       "BorealForest",   "TemperateRainforest", "TropicalRainforest", "Savanna",            //WETTER
                "Ice",    "BorealForest", "BorealForest",   "TemperateRainforest", "TropicalRainforest", "TropicalRainforest", //WETTEST
                "Rocky",  "Rocky",        "Beach",          "Beach",               "Beach",              "Beach",              //COASTS
                "Ice",    "River",        "River",          "River",               "River",              "River",              //RIVERS
                "Ice",    "River",        "River",          "River",               "River",              "River",              //LAKES
                "Ocean",  "Ocean",        "Ocean",          "Ocean",               "Ocean",              "Ocean",              //OCEAN
                "Empty",                                                                                                       //SPACE
        };

        /**
         * Simple constructor; pretty much does nothing. Make sure to call {@link #makeBiomes(WorldMapGenerator)} before
         * using fields like {@link #biomeCodeData}.
         */
        public SimpleBiomeMapper()
        {
            heatCodeData = null;
            moistureCodeData = null;
            biomeCodeData = null;
        }

        /**
         * Analyzes the last world produced by the given WorldMapGenerator and uses all of its generated information to
         * assign biome codes for each cell (along with heat and moisture codes). After calling this, biome codes can be
         * taken from {@link #biomeCodeData} and used as indices into {@link #biomeTable} or a custom biome table.
         * @param world a WorldMapGenerator that should have generated at least one map; it may be at any zoom
         */
        @Override
        public void makeBiomes(WorldMapGenerator world) {
            if(world == null || world.width <= 0 || world.height <= 0)
                return;
            if(heatCodeData == null || (heatCodeData.length != world.width || heatCodeData[0].length != world.height))
                heatCodeData = new int[world.width][world.height];
            if(moistureCodeData == null || (moistureCodeData.length != world.width || moistureCodeData[0].length != world.height))
                moistureCodeData = new int[world.width][world.height];
            if(biomeCodeData == null || (biomeCodeData.length != world.width || biomeCodeData[0].length != world.height))
                biomeCodeData = new int[world.width][world.height];
            final double i_hot = (world.maxHeat == world.minHeat) ? 1.0 : 1.0 / (world.maxHeat - world.minHeat);
            for (int x = 0; x < world.width; x++) {
                for (int y = 0; y < world.height; y++) {
                    final double hot = (world.heatData[x][y] - world.minHeat) * i_hot, moist = world.moistureData[x][y];
                    final int heightCode = world.heightCodeData[x][y];
                    if(heightCode == 1000) {
                        biomeCodeData[x][y] = 60;
                        continue;
                    }
                    int hc, mc;
                    boolean isLake = false,// world.generateRivers && heightCode >= 4 && fresh > 0.65 && fresh + moist * 2.35 > 2.75,//world.partialLakeData.contains(x, y) && heightCode >= 4,
                            isRiver = false;// world.generateRivers && !isLake && heightCode >= 4 && fresh > 0.55 && fresh + moist * 2.2 > 2.15;//world.partialRiverData.contains(x, y) && heightCode >= 4;
                    if(heightCode < 4) {
                        mc = 9;
                    }
                    else if (moist > wetterValueUpper) {
                        mc = 5;
                    } else if (moist > wetValueUpper) {
                        mc = 4;
                    } else if (moist > dryValueUpper) {
                        mc = 3;
                    } else if (moist > drierValueUpper) {
                        mc = 2;
                    } else if (moist > driestValueUpper) {
                        mc = 1;
                    } else {
                        mc = 0;
                    }

                    if (hot > warmerValueUpper) {
                        hc = 5;
                    } else if (hot > warmValueUpper) {
                        hc = 4;
                    } else if (hot > coldValueUpper) {
                        hc = 3;
                    } else if (hot > colderValueUpper) {
                        hc = 2;
                    } else if (hot > coldestValueUpper) {
                        hc = 1;
                    } else {
                        hc = 0;
                    }

                    heatCodeData[x][y] = hc;
                    moistureCodeData[x][y] = mc;
                    biomeCodeData[x][y] = heightCode < 4 ? hc + 54 // 54 == 9 * 6, 9 is used for Ocean groups
                            : isLake ? hc + 48 : (isRiver ? hc + 42 : ((heightCode == 4) ? hc + 36 : hc + mc * 6));
                }
            }
        }
    }
    /**
     * A way to get biome information for the cells on a map when you want an area's biome to be a combination of two
     * main biome types, such as "Grassland" or "TropicalRainforest", with the biomes varying in weight between areas.
     * <br>
     * To use: 1, Construct a DetailedBiomeMapper (constructor takes no arguments). 2, call
     * {@link #makeBiomes(WorldMapGenerator)} with a WorldMapGenerator that has already produced at least one world map.
     * 3, get biome codes from the {@link #biomeCodeData} field, where a code is an int that can be used with the
     * extract methods in this class to get various information from it (these are {@link #extractBiomeA(int)},
     * {@link #extractBiomeB(int)}, {@link #extractPartA(int)}, {@link #extractPartB(int)}, and
     * {@link #extractMixAmount(int)}). You can get predefined names for biomes using the extractBiome methods (these
     * names can be changed in {@link #biomeTable}), or raw indices into some (usually 61-element) collection or array
     * with the extractPart methods. The extractMixAmount() method gets a float that is the amount by which biome B
     * affects biome A; if this is higher than 0.5, then biome B is the "dominant" biome in the area.
     */
    public static class DetailedBiomeMapper implements BiomeMapper
    {
        /**
         * The heat codes for the analyzed map, from 0 to 5 inclusive, with 0 coldest and 5 hottest.
         */
        public int[][] heatCodeData,
        /**
         * The moisture codes for the analyzed map, from 0 to 5 inclusive, with 0 driest and 5 wettest.
         */
        moistureCodeData,
        /**
         * The biome codes for the analyzed map, using one int to store the codes for two biomes and the degree by which
         * the second biome affects the first. These codes can be used with methods in this class like
         * {@link #extractBiomeA(int)}, {@link #extractBiomeB(int)}, and {@link #extractMixAmount(int)} to find the two
         * dominant biomes in an area, called biome A and biome B, and the mix amount, for finding how much biome B
         * affects biome A.
         */
        biomeCodeData;

        @Override
        public int getBiomeCode(int x, int y) {
            int code = biomeCodeData[x][y];
            if(code < 0x2000000) return code & 1023;
            return (code >>> 10) & 1023;
        }

        @Override
        public int getHeatCode(int x, int y) {
            return heatCodeData[x][y];
        }

        @Override
        public int getMoistureCode(int x, int y) {
            return moistureCodeData[x][y];
        }

        /**
         * Gets a String array where biome codes can be used as indices to look up a name for the biome they refer to.
         * This table uses 6 levels of heat and 6 levels of moisture, and tracks rivers, coastlines, lakes, and oceans
         * as potentially different types of terrain. Biome codes can be obtained with {@link #getBiomeCode(int, int)}.
         * This method returns a direct reference to {@link #biomeTable}, so modifying the returned array is discouraged
         * (you should implement {@link BiomeMapper} using this class as a basis if you want to change its size).
         * @return a direct reference to {@link #biomeTable}, a String array containing names of biomes
         */
        @Override
        public String[] getBiomeNameTable() {
            return biomeTable;
        }

        public static final double
                coldestValueLower = 0.0,   coldestValueUpper = 0.15, // 0
                colderValueLower = 0.15,   colderValueUpper = 0.31,  // 1
                coldValueLower = 0.31,     coldValueUpper = 0.5,     // 2
                warmValueLower = 0.5,      warmValueUpper = 0.69,     // 3
                warmerValueLower = 0.69,    warmerValueUpper = 0.85,   // 4
                warmestValueLower = 0.85,   warmestValueUpper = 1.0,  // 5

        driestValueLower = 0.0,    driestValueUpper  = 0.27, // 0
                drierValueLower = 0.27,    drierValueUpper   = 0.4,  // 1
                dryValueLower = 0.4,       dryValueUpper     = 0.6,  // 2
                wetValueLower = 0.6,       wetValueUpper     = 0.8,  // 3
                wetterValueLower = 0.8,    wetterValueUpper  = 0.9,  // 4
                wettestValueLower = 0.9,   wettestValueUpper = 1.0;  // 5

        /**
         * The default biome table to use with parts of biome codes from {@link #biomeCodeData}. Biomes are assigned by
         * heat and moisture for the first 36 of 61 elements (coldest to warmest for each group of 6, with the first
         * group as the dryest and the last group the wettest), then the next 6 are for coastlines (coldest to warmest),
         * then rivers (coldest to warmest), then lakes (coldest to warmest). The last is reserved for empty space.
         * <br>
         * Unlike with {@link SimpleBiomeMapper}, you cannot use a biome code directly from biomeCodeData as an index
         * into this in almost any case; you should pass the biome code to one of the extract methods.
         * {@link #extractBiomeA(int)} or {@link #extractBiomeB(int)} will work if you want a biome name, or
         * {@link #extractPartA(int)} or {@link #extractPartB(int)} should be used if you want a non-coded int that
         * represents one of the biomes' indices into something like this. You can also get the amount by which biome B
         * is affecting biome A with {@link #extractMixAmount(int)}.
         */
        public static final String[] biomeTable = {
                //COLDEST //COLDER        //COLD            //HOT                  //HOTTER              //HOTTEST
                "Ice",    "Ice",          "Grassland",      "Desert",              "Desert",             "Desert",             //DRYEST
                "Ice",    "Tundra",       "Grassland",      "Grassland",           "Desert",             "Desert",             //DRYER
                "Ice",    "Tundra",       "Woodland",       "Woodland",            "Savanna",            "Desert",             //DRY
                "Ice",    "Tundra",       "SeasonalForest", "SeasonalForest",      "Savanna",            "Savanna",            //WET
                "Ice",    "Tundra",       "BorealForest",   "TemperateRainforest", "TropicalRainforest", "Savanna",            //WETTER
                "Ice",    "BorealForest", "BorealForest",   "TemperateRainforest", "TropicalRainforest", "TropicalRainforest", //WETTEST
                "Rocky",  "Rocky",        "Beach",          "Beach",               "Beach",              "Beach",              //COASTS
                "Ice",    "River",        "River",          "River",               "River",              "River",              //RIVERS
                "Ice",    "River",        "River",          "River",               "River",              "River",              //LAKES
                "Ocean",  "Ocean",        "Ocean",          "Ocean",               "Ocean",              "Ocean",              //OCEAN
                "Empty",                                                                                                       //SPACE
        };

        /**
         * Gets the int stored in part A of the given biome code, which can be used as an index into other collections.
         * This int should almost always range from 0 to 60 (both inclusive), so collections this is used as an index
         * for should have a length of at least 61.
         * @param biomeCode a biome code that was probably received from {@link #biomeCodeData}
         * @return an int stored in the biome code's part A; almost always between 0 and 60, inclusive.
         */
        public int extractPartA(int biomeCode)
        {
            return biomeCode & 1023;
        }
        /**
         * Gets a String from {@link #biomeTable} that names the appropriate biome in part A of the given biome code.
         * @param biomeCode a biome code that was probably received from {@link #biomeCodeData}
         * @return a String that names the biome in part A of biomeCode, or "Empty" if none can be found
         */
        public String extractBiomeA(int biomeCode)
        {
            biomeCode &= 1023;
            if(biomeCode < 60)
                return biomeTable[biomeCode];
            return "Empty";
        }
        /**
         * Gets the int stored in part B of the given biome code, which can be used as an index into other collections.
         * This int should almost always range from 0 to 60 (both inclusive), so collections this is used as an index
         * for should have a length of at least 61.
         * @param biomeCode a biome code that was probably received from {@link #biomeCodeData}
         * @return an int stored in the biome code's part B; almost always between 0 and 60, inclusive.
         */
        public int extractPartB(int biomeCode)
        {
            return (biomeCode >>> 10) & 1023;
        }

        /**
         * Gets a String from {@link #biomeTable} that names the appropriate biome in part B of the given biome code.
         * @param biomeCode a biome code that was probably received from {@link #biomeCodeData}
         * @return a String that names the biome in part B of biomeCode, or "Ocean" if none can be found
         */
        public String extractBiomeB(int biomeCode)
        {
            biomeCode = (biomeCode >>> 10) & 1023;
            if(biomeCode < 60)
                return biomeTable[biomeCode];
            return "Empty";
        }

        /**
         * This gets the portion of a biome code that represents the amount of mixing between two biomes.
         * Biome codes are normally obtained from the {@link #biomeCodeData} field, and aren't very usable on their own
         * without calling methods like this, {@link #extractBiomeA(int)}, and {@link #extractBiomeB(int)}. This returns
         * a float between 0.0f (inclusive) and 1.0f (exclusive), with 0.0f meaning biome B has no effect on an area and
         * biome A is the only one used, 0.5f meaning biome A and biome B have equal effect, and 0.75f meaning biome B
         * has most of the effect, three-fourths of the area, and biome A has less, one-fourth of the area.
         * @param biomeCode a biome code that was probably received from {@link #biomeCodeData}
         * @return a float between 0.0f (inclusive) and 1.0f (exclusive) representing mixing of biome B into biome A
         */
        public float extractMixAmount(int biomeCode)
        {
            return (biomeCode >>> 20) * 0x1p-10f;
        }

        /**
         * Simple constructor; pretty much does nothing. Make sure to call {@link #makeBiomes(WorldMapGenerator)} before
         * using fields like {@link #biomeCodeData}.
         */
        public DetailedBiomeMapper()
        {
            heatCodeData = null;
            moistureCodeData = null;
            biomeCodeData = null;
        }

        /**
         * Analyzes the last world produced by the given WorldMapGenerator and uses all of its generated information to
         * assign biome codes for each cell (along with heat and moisture codes). After calling this, biome codes can be
         * taken from {@link #biomeCodeData} and used with methods in this class like {@link #extractBiomeA(int)},
         * {@link #extractBiomeB(int)}, and {@link #extractMixAmount(int)} to find the two dominant biomes in an area,
         * called biome A and biome B, and the mix amount, for finding how much biome B affects biome A.
         * @param world a WorldMapGenerator that should have generated at least one map; it may be at any zoom
         */
        @Override
        public void makeBiomes(WorldMapGenerator world) {
            if(world == null || world.width <= 0 || world.height <= 0)
                return;
            if(heatCodeData == null || (heatCodeData.length != world.width || heatCodeData[0].length != world.height))
                heatCodeData = new int[world.width][world.height];
            if(moistureCodeData == null || (moistureCodeData.length != world.width || moistureCodeData[0].length != world.height))
                moistureCodeData = new int[world.width][world.height];
            if(biomeCodeData == null || (biomeCodeData.length != world.width || biomeCodeData[0].length != world.height))
                biomeCodeData = new int[world.width][world.height];
            final int[][] heightCodeData = world.heightCodeData;
            final double[][] heatData = world.heatData, moistureData = world.moistureData, heightData = world.heightData;
            int hc, mc, heightCode, bc;
            double hot, moist, high, i_hot = 1.0 / world.maxHeat, fresh;
            for (int x = 0; x < world.width; x++) {
                for (int y = 0; y < world.height; y++) {

                    heightCode = heightCodeData[x][y];
                    if(heightCode == 1000) {
                        biomeCodeData[x][y] = 61;
                        continue;
                    }
                    hot = heatData[x][y];
                    moist = moistureData[x][y];
                    high = heightData[x][y];
//                    fresh = world.freshwaterData[x][y];
                    boolean isLake = false,//world.generateRivers && heightCode >= 4 && fresh > 0.65 && fresh + moist * 2.35 > 2.75,//world.partialLakeData.contains(x, y) && heightCode >= 4,
                            isRiver = false;//world.generateRivers && !isLake && heightCode >= 4 && fresh > 0.55 && fresh + moist * 2.2 > 2.15;//world.partialRiverData.contains(x, y) && heightCode >= 4;
                    if (moist >= (wettestValueUpper - (wetterValueUpper - wetterValueLower) * 0.2)) {
                        mc = 5;
                    } else if (moist >= (wetterValueUpper - (wetValueUpper - wetValueLower) * 0.2)) {
                        mc = 4;
                    } else if (moist >= (wetValueUpper - (dryValueUpper - dryValueLower) * 0.2)) {
                        mc = 3;
                    } else if (moist >= (dryValueUpper - (drierValueUpper - drierValueLower) * 0.2)) {
                        mc = 2;
                    } else if (moist >= (drierValueUpper - (driestValueUpper) * 0.2)) {
                        mc = 1;
                    } else {
                        mc = 0;
                    }

                    if (hot >= (warmestValueUpper - (warmerValueUpper - warmerValueLower) * 0.2) * i_hot) {
                        hc = 5;
                    } else if (hot >= (warmerValueUpper - (warmValueUpper - warmValueLower) * 0.2) * i_hot) {
                        hc = 4;
                    } else if (hot >= (warmValueUpper - (coldValueUpper - coldValueLower) * 0.2) * i_hot) {
                        hc = 3;
                    } else if (hot >= (coldValueUpper - (colderValueUpper - colderValueLower) * 0.2) * i_hot) {
                        hc = 2;
                    } else if (hot >= (colderValueUpper - (coldestValueUpper) * 0.2) * i_hot) {
                        hc = 1;
                    } else {
                        hc = 0;
                    }

                    heatCodeData[x][y] = hc;
                    moistureCodeData[x][y] = mc;
                    bc = heightCode < 4 ? hc + 54 // 54 == 9 * 6, 9 is used for Ocean groups
                            : isLake ? hc + 48 : (isRiver ? hc + 42 : ((heightCode == 4) ? hc + 36 : hc + mc * 6));

                    if(heightCode < 4) {
                        mc = 9;
                    }
                    else if (moist >= (wetterValueUpper + (wettestValueUpper - wettestValueLower) * 0.2)) {
                        mc = 5;
                    } else if (moist >= (wetValueUpper + (wetterValueUpper - wetterValueLower) * 0.2)) {
                        mc = 4;
                    } else if (moist >= (dryValueUpper + (wetValueUpper - wetValueLower) * 0.2)) {
                        mc = 3;
                    } else if (moist >= (drierValueUpper + (dryValueUpper - dryValueLower) * 0.2)) {
                        mc = 2;
                    } else if (moist >= (driestValueUpper + (drierValueUpper - drierValueLower) * 0.2)) {
                        mc = 1;
                    } else {
                        mc = 0;
                    }

                    if (hot >= (warmerValueUpper + (warmestValueUpper - warmestValueLower) * 0.2) * i_hot) {
                        hc = 5;
                    } else if (hot >= (warmValueUpper + (warmerValueUpper - warmerValueLower) * 0.2) * i_hot) {
                        hc = 4;
                    } else if (hot >= (coldValueUpper + (warmValueUpper - warmValueLower) * 0.2) * i_hot) {
                        hc = 3;
                    } else if (hot >= (colderValueUpper + (coldValueUpper - coldValueLower) * 0.2) * i_hot) {
                        hc = 2;
                    } else if (hot >= (coldestValueUpper + (colderValueUpper - colderValueLower) * 0.2) * i_hot) {
                        hc = 1;
                    } else {
                        hc = 0;
                    }

                    bc |= (hc + mc * 6) << 10;
                    if(heightCode < 4)
                        biomeCodeData[x][y] = bc | (int)((heightData[x][y] + 1.0) * 1000.0) << 20;
                    else if (isRiver || isLake)
                        biomeCodeData[x][y] = bc | (int)(moist * 358.4 + 665.0) << 20;
                    else
                        biomeCodeData[x][y] = bc | (int) ((heightCode == 4)
                                ? (sandUpper - high) * 10240.0 // multiplier affected by changes to sandLower
                                : NumberTools.sway((high + moist) * (4.1 + high - hot)) * 512 + 512) << 20;
                }
            }
        }
    }

    /**
     * A concrete implementation of {@link WorldMapGenerator} that tiles both east-to-west and north-to-south. It tends
     * to not appear distorted like {@link WorldMapGenerator.SphereMap} does in some areas, even though this is inaccurate for a
     * rectangular projection of a spherical world (that inaccuracy is likely what players expect in a map, though).
     * <a href="http://squidpony.github.io/SquidLib/DetailedWorldMapRiverDemo.png" >Example map</a>.
     */
    public static class TilingMap extends WorldMapGenerator {
        //protected static final double terrainFreq = 1.5, terrainRidgedFreq = 1.3, heatFreq = 2.8, moistureFreq = 2.9, otherFreq = 4.5;
//        protected static final double terrainFreq = 1.175, terrainRidgedFreq = 1.3, heatFreq = 2.3, moistureFreq = 2.4, otherFreq = 3.5;
        protected static final double terrainFreq = 0.95, terrainRidgedFreq = 3.1, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375;
        private double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final Noise4D terrain, terrainRidged, heat, moisture, otherRidged;

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
         * as north-to-south. Always makes a 256x256 map.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link TilingMap#TilingMap(long, int, int, Noise4D, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 256, FastNoise.instance, 1.0}.
         */
        public TilingMap() {
            this(0x1337BABE1337D00DL, 256, 256, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
         * as north-to-south.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public TilingMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
         * as north-to-south.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public TilingMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
         * as north-to-south. Takes an initial seed, the width/height of the map, and a noise generator (a
         * {@link Noise4D} implementation, which is usually {@link FastNoise#instance}. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call
         * {@link #generate(long)}. The width and height of the map cannot be changed after the fact, but you can zoom
         * in. Any seed supplied to the Noise4D given to this (if it takes one) will be ignored, and
         * {@link Noise4D#getNoiseWithSeed(double, double, double, double, long)} will be used to specify the seed many
         * times. The detail level, which is the {@code octaveMultiplier} parameter that can be passed to another
         * constructor, is always 1.0 with this constructor.
         *
         * @param initialSeed      the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth         the width of the map(s) to generate; cannot be changed later
         * @param mapHeight        the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator   an instance of a noise generator capable of 4D noise, recommended to be {@link FastNoise#instance}
         */
        public TilingMap(long initialSeed, int mapWidth, int mapHeight, final Noise4D noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
         * as north-to-south. Takes an initial seed, the width/height of the map, and parameters for noise
         * generation (a {@link Noise4D} implementation, which is usually {@link FastNoise#instance}, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
         * cannot be changed after the fact, but you can zoom in. Any seed supplied to the Noise4D given to this (if it takes one) will be ignored, and
         * {@link Noise4D#getNoiseWithSeed(double, double, double, double, long)} will be used to specify the seed many
         * times. The {@code octaveMultiplier} parameter should probably be no lower than 0.5, but can be arbitrarily
         * high if you're willing to spend much more time on generating detail only noticeable at very high zoom;
         * normally 1.0 is fine and may even be too high for maps that don't require zooming.
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 4D noise, almost always {@link FastNoise}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public TilingMap(long initialSeed, int mapWidth, int mapHeight, final Noise4D noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            terrain = new Noise.InverseLayered4D(noiseGenerator, (int) (0.5 + octaveMultiplier * 8), terrainFreq);
            terrainRidged = new Noise.Ridged4D(noiseGenerator, (int) (0.5 + octaveMultiplier * 10), terrainRidgedFreq);
            heat = new Noise.InverseLayered4D(noiseGenerator, (int) (0.5 + octaveMultiplier * 3), heatFreq);
            moisture = new Noise.InverseLayered4D(noiseGenerator, (int) (0.5 + octaveMultiplier * 4), moistureFreq);
            otherRidged = new Noise.Ridged4D(noiseGenerator, (int) (0.5 + octaveMultiplier * 6), otherFreq);
        }

        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double coolMod, int stateA, int stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || coolMod != coolingModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
                minHeat0 = Double.POSITIVE_INFINITY;
                maxHeat0 = Double.NEGATIVE_INFINITY;
                minHeat1 = Double.POSITIVE_INFINITY;
                maxHeat1 = Double.NEGATIVE_INFINITY;
                minHeat = Double.POSITIVE_INFINITY;
                maxHeat = Double.NEGATIVE_INFINITY;
                minWet0 = Double.POSITIVE_INFINITY;
                maxWet0 = Double.NEGATIVE_INFINITY;
                minWet = Double.POSITIVE_INFINITY;
                maxWet = Double.NEGATIVE_INFINITY;
                cacheA = stateA;
                cacheB = stateB;
                fresh = true;
            }
            rng.setState(stateA, stateB);
            long seedA = rng.nextLong(), seedB = rng.nextLong(), seedC = rng.nextLong();
            int t;

            landModifier = (landMod <= 0) ? rng.nextDouble(0.1875) + 0.99 : landMod;
            coolingModifier = (coolMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : coolMod;

            double p, q,
                    ps, pc,
                    qs, qc,
                    h, temp,
                    i_w = 6.283185307179586 / width, i_h = 6.283185307179586 / height,
                    xPos = startX, yPos = startY, i_uw = usedWidth / (double)width, i_uh = usedHeight / (double)height;
            double[] trigTable = new double[width << 1];
            for (int x = 0; x < width; x++, xPos += i_uw) {
                p = xPos * i_w;
                trigTable[x<<1]   = NumberTools.sin(p);
                trigTable[x<<1|1] = NumberTools.cos(p);
            }
            for (int y = 0; y < height; y++, yPos += i_uh) {
                q = yPos * i_h;
                qs = NumberTools.sin(q);
                qc = NumberTools.cos(q);
                for (int x = 0, xt = 0; x < width; x++) {
                    ps = trigTable[xt++];//NumberTools.sin(p);
                    pc = trigTable[xt++];//NumberTools.cos(p);
                    heightData[x][y] = (h = terrain.getNoiseWithSeed(pc +
                                    terrainRidged.getNoiseWithSeed(pc, ps, qc, qs,seedB - seedA) * 0.25,
                            ps, qc, qs, seedA) + landModifier - 1.0);
                    heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps, qc
                                    + otherRidged.getNoiseWithSeed(pc, ps, qc, qs, seedB + seedC)
                            , qs, seedB));
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qc, qs
                                    + otherRidged.getNoiseWithSeed(pc, ps, qc, qs, seedC + seedA)
                            , seedC));
                    minHeightActual = Math.min(minHeightActual, h);
                    maxHeightActual = Math.max(maxHeightActual, h);
                    if(fresh) {
                        minHeight = Math.min(minHeight, h);
                        maxHeight = Math.max(maxHeight, h);

                        minHeat0 = Math.min(minHeat0, p);
                        maxHeat0 = Math.max(maxHeat0, p);

                        minWet0 = Math.min(minWet0, temp);
                        maxWet0 = Math.max(maxWet0, temp);

                    }
                }
                minHeightActual = Math.min(minHeightActual, minHeight);
                maxHeightActual = Math.max(maxHeightActual, maxHeight);

            }
            double heightDiff = 2.0 / (maxHeightActual - minHeightActual),
                    heatDiff = 0.8 / (maxHeat0 - minHeat0),
                    wetDiff = 1.0 / (maxWet0 - minWet0),
                    hMod,
                    halfHeight = (height - 1) * 0.5, i_half = 1.0 / halfHeight;
            double minHeightActual0 = minHeightActual;
            double maxHeightActual0 = maxHeightActual;
            yPos = startY;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                temp = Math.abs(yPos - halfHeight) * i_half;
                temp *= (2.4 - temp);
                temp = 2.2 - temp;
                for (int x = 0; x < width; x++) {
//                    heightData[x][y] = (h = (heightData[x][y] - minHeightActual) * heightDiff - 1.0);
//                    minHeightActual0 = Math.min(minHeightActual0, h);
//                    maxHeightActual0 = Math.max(maxHeightActual0, h);
                    h = heightData[x][y];
                    heightCodeData[x][y] = (t = codeHeight(h));
                    hMod = 1.0;
                    switch (t) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            h = 0.4;
                            hMod = 0.2;
                            break;
                        case 6:
                            h = -0.1 * (h - forestLower - 0.08);
                            break;
                        case 7:
                            h *= -0.25;
                            break;
                        case 8:
                            h *= -0.4;
                            break;
                        default:
                            h *= 0.05;
                    }
                    heatData[x][y] = (h = (((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6) * temp);
                    if (fresh) {
                        ps = Math.min(ps, h); //minHeat0
                        pc = Math.max(pc, h); //maxHeat0
                    }
                }
            }
            if(fresh)
            {
                minHeat1 = ps;
                maxHeat1 = pc;
            }
            heatDiff = coolingModifier / (maxHeat1 - minHeat1);
            qs = Double.POSITIVE_INFINITY;
            qc = Double.NEGATIVE_INFINITY;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;


            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    heatData[x][y] = (h = ((heatData[x][y] - minHeat1) * heatDiff));
                    moistureData[x][y] = (temp = (moistureData[x][y] - minWet0) * wetDiff);
                    if (fresh) {
                        qs = Math.min(qs, h);
                        qc = Math.max(qc, h);
                        ps = Math.min(ps, temp);
                        pc = Math.max(pc, temp);
                    }
                }
            }
            if(fresh)
            {
                minHeat = qs;
                maxHeat = qc;
                minWet = ps;
                maxWet = pc;
            }
            landData.refill(heightCodeData, 4, 999);
            /*
            if(generateRivers) {
                if (fresh) {
                    addRivers();
                    riverData.connect8way().thin().thin();
                    lakeData.connect8way().thin();
                    partialRiverData.remake(riverData);
                    partialLakeData.remake(lakeData);
                } else {
                    partialRiverData.remake(riverData);
                    partialLakeData.remake(lakeData);
                    int stx = (zoomStartX >> (zoom)) - (width >> 1),
                            sty = (zoomStartY >> (zoom)) - (height >> 1);
                    for (int i = 1; i <= zoom; i++) {
//                        int stx = (startCacheX.get(i) - startCacheX.get(i - 1)) << (i - 1),
//                                sty = (startCacheY.get(i) - startCacheY.get(i - 1)) << (i - 1);
                        if ((i & 3) == 3) {
                            partialRiverData.zoom(stx, sty).connect8way();
                            partialRiverData.or(workingData.remake(partialRiverData).fringe().quasiRandomRegion(0.4));
                            partialLakeData.zoom(stx, sty).connect8way();
                            partialLakeData.or(workingData.remake(partialLakeData).fringe().quasiRandomRegion(0.55));
                        } else {
                            partialRiverData.zoom(stx, sty).connect8way().thin();
                            partialRiverData.or(workingData.remake(partialRiverData).fringe().quasiRandomRegion(0.5));
                            partialLakeData.zoom(stx, sty).connect8way().thin();
                            partialLakeData.or(workingData.remake(partialLakeData).fringe().quasiRandomRegion(0.7));
                        }
                    }
                }
            }
            */
        }
    }

    /**
     * A concrete implementation of {@link WorldMapGenerator} that distorts the map as it nears the poles, expanding the
     * smaller-diameter latitude lines in extreme north and south regions so they take up the same space as the equator;
     * this counteracts certain artifacts that are common in Simplex noise world maps by using a 4D noise call to
     * generate terrain, using a normal 3D noise call's result as the extra 4th dimension. This generator allows
     * choosing a {@link Noise3D}, which is used for most of the generation. This is ideal for projecting onto a 3D
     * sphere, which could squash the poles to counteract the stretch this does. You might also want to produce an oval
     * map that more-accurately represents the changes in the diameter of a latitude line on a spherical world; you
     * should use {@link WorldMapGenerator.EllipticalMap} or {@link WorldMapGenerator.EllipticalHammerMap} for this.
     * {@link WorldMapGenerator.HyperellipticalMap} is also a nice option because it can project onto a shape between a
     * rectangle (like this class) and an ellipse (like EllipticalMap), with all-round sides.
     * <a href="http://i.imgur.com/wth01QD.png">Example map, showing distortion</a>
     */
    @Beta
    public static class SphereMap extends WorldMapGenerator {
        protected static final double terrainFreq = 1.45, terrainRidgedFreq = 3.1, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375;
        //protected static final double terrainFreq = 1.65, terrainRidgedFreq = 1.8, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375, riverRidgedFreq = 21.7;
        private double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final Noise3D terrain, heat, moisture, otherRidged, terrainLayered;
        public final double[][] xPositions,
                yPositions,
                zPositions;


        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Always makes a 256x128 map.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link SphereMap#SphereMap(long, int, int, Noise3D, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 128, FastNoise.instance, 1.0}.
         */
        public SphereMap() {
            this(0x1337BABE1337D00DL, 256, 128, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public SphereMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight,  FastNoise.instance,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public SphereMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public SphereMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         */
        public SphereMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed, the width/height of the map, and parameters for noise
         * generation (a {@link Noise3D} implementation, which is usually {@link FastNoise#instance}, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
         * cannot be changed after the fact, but you can zoom in. FastNoise will be the fastest 3D generator to use for
         * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
         * seed several times at different scales of noise (it's fine to use the static {@link FastNoise#instance}
         * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
         * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
         * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
         * that don't require zooming.
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise#instance}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public SphereMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[width][height];
            yPositions = new double[width][height];
            zPositions = new double[width][height];

            terrain = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 10), terrainFreq);
            terrainLayered = new Noise.InverseLayered3D(noiseGenerator, (int) (1 + octaveMultiplier * 6), terrainRidgedFreq * 0.325);
            heat = new Noise.InverseLayered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 3), heatFreq, 0.75);
            moisture = new Noise.InverseLayered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 4), moistureFreq, 0.55);
            otherRidged = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 6), otherFreq);
        }
        @Override
        public int wrapY(final int x, final int y)  {
            return Math.max(0, Math.min(y, height - 1));
        }

        /**
         * Given a latitude and longitude in radians (the conventional way of describing points on a globe), this gets the
         * (x,y) Coord on the map projection this generator uses that corresponds to the given lat-lon coordinates. If this
         * generator does not represent a globe (if it is toroidal, for instance) or if there is no "good way" to calculate
         * the projection for a given lat-lon coordinate, this returns null. This implementation never returns null.
         * If this is a supported operation and the parameters are valid, this returns a Coord with x between 0 and
         * {@link #width}, and y between 0 and {@link #height}, both exclusive. Automatically wraps the Coord's values using
         * {@link #wrapX(int, int)} and {@link #wrapY(int, int)}.
         * @param latitude the latitude, from {@code Math.PI * -0.5} to {@code Math.PI * 0.5}
         * @param longitude the longitude, from {@code 0.0} to {@code Math.PI * 2.0}
         * @return the point at the given latitude and longitude, as a Coord with x between 0 and {@link #width} and y between 0 and {@link #height}, or null if unsupported
         */
        @Override
        public Coord project(double latitude, double longitude) {
            int x = (int)(((longitude - getCenterLongitude() + 12.566370614359172) % 6.283185307179586) * 0.15915494309189535 * width + 0.5),
                    y = (int)((NumberTools.sin(latitude) * 0.5 + 0.5) * height + 0.5);
            return Coord.get(
                    wrapX(x, y),
                    wrapY(x, y));
        }

        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double coolMod, int stateA, int stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || coolMod != coolingModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
                minHeat0 = Double.POSITIVE_INFINITY;
                maxHeat0 = Double.NEGATIVE_INFINITY;
                minHeat1 = Double.POSITIVE_INFINITY;
                maxHeat1 = Double.NEGATIVE_INFINITY;
                minHeat = Double.POSITIVE_INFINITY;
                maxHeat = Double.NEGATIVE_INFINITY;
                minWet0 = Double.POSITIVE_INFINITY;
                maxWet0 = Double.NEGATIVE_INFINITY;
                minWet = Double.POSITIVE_INFINITY;
                maxWet = Double.NEGATIVE_INFINITY;
                cacheA = stateA;
                cacheB = stateB;
                fresh = true;
            }
            rng.setState(stateA, stateB);
            long seedA = rng.nextLong(), seedB = rng.nextLong(), seedC = rng.nextLong();
            int t;

            landModifier = (landMod <= 0) ? rng.nextDouble(0.29) + 0.91 : landMod;
            coolingModifier = (coolMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : coolMod;

            double p,
                    ps, pc,
                    qs, qc,
                    h, temp,
                    i_w = 6.283185307179586 / width, i_h = 2.0 / (height+2.0),//(3.141592653589793) / (height+2.0),
                    xPos = startX, yPos, i_uw = usedWidth / (double)width, i_uh = usedHeight * i_h / (height+2.0);
            final double[] trigTable = new double[width << 1];
            for (int x = 0; x < width; x++, xPos += i_uw) {
                p = xPos * i_w + centerLongitude;
                trigTable[x<<1]   = NumberTools.sin(p);
                trigTable[x<<1|1] = NumberTools.cos(p);
            }
            yPos = startY * i_h + i_uh;
            for (int y = 0; y < height; y++, yPos += i_uh) {
                qs = -1 + yPos;//-1.5707963267948966 + yPos;
                qc = NumberTools.cos(asin(qs));
                //qs = qs;
                //qs = NumberTools.sin(qs);
                for (int x = 0, xt = 0; x < width; x++) {
                    ps = trigTable[xt++] * qc;//NumberTools.sin(p);
                    pc = trigTable[xt++] * qc;//NumberTools.cos(p);
                    xPositions[x][y] = pc;
                    yPositions[x][y] = ps;
                    zPositions[x][y] = qs;
                    heightData[x][y] = (h = terrainLayered.getNoiseWithSeed(pc +
                                    terrain.getNoiseWithSeed(pc, ps, qs,seedB - seedA) * 0.5,
                            ps, qs, seedA) + landModifier - 1.0);
                    heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps
                                    + otherRidged.getNoiseWithSeed(pc, ps, qs,seedB + seedC)
                            , qs, seedB));
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qs
                                    + otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA)
                            , seedC));

                    minHeightActual = Math.min(minHeightActual, h);
                    maxHeightActual = Math.max(maxHeightActual, h);
                    if(fresh) {
                        minHeight = Math.min(minHeight, h);
                        maxHeight = Math.max(maxHeight, h);

                        minHeat0 = Math.min(minHeat0, p);
                        maxHeat0 = Math.max(maxHeat0, p);

                        minWet0 = Math.min(minWet0, temp);
                        maxWet0 = Math.max(maxWet0, temp);
                    }
                }
                minHeightActual = Math.min(minHeightActual, minHeight);
                maxHeightActual = Math.max(maxHeightActual, maxHeight);

            }
            double  heatDiff = 0.8 / (maxHeat0 - minHeat0),
                    wetDiff = 1.0 / (maxWet0 - minWet0),
                    hMod;
            yPos = startY * i_h + i_uh;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                temp = Math.abs(yPos - 1.0);
                temp *= (2.4 - temp);
                temp = 2.2 - temp;
                for (int x = 0; x < width; x++) {
//                    heightData[x][y] = (h = (heightData[x][y] - minHeightActual) * heightDiff - 1.0);
//                    minHeightActual0 = Math.min(minHeightActual0, h);
//                    maxHeightActual0 = Math.max(maxHeightActual0, h);
                    h = heightData[x][y];
                    heightCodeData[x][y] = (t = codeHeight(h));
                    hMod = 1.0;
                    switch (t) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            h = 0.4;
                            hMod = 0.2;
                            break;
                        case 6:
                            h = -0.1 * (h - forestLower - 0.08);
                            break;
                        case 7:
                            h *= -0.25;
                            break;
                        case 8:
                            h *= -0.4;
                            break;
                        default:
                            h *= 0.05;
                    }
                    heatData[x][y] = (h = (((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6) * temp);
                    if (fresh) {
                        ps = Math.min(ps, h); //minHeat0
                        pc = Math.max(pc, h); //maxHeat0
                    }
                }
            }
            if(fresh)
            {
                minHeat1 = ps;
                maxHeat1 = pc;
            }
            heatDiff = coolingModifier / (maxHeat1 - minHeat1);
            qs = Double.POSITIVE_INFINITY;
            qc = Double.NEGATIVE_INFINITY;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;


            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    heatData[x][y] = (h = ((heatData[x][y] - minHeat1) * heatDiff));
                    moistureData[x][y] = (temp = (moistureData[x][y] - minWet0) * wetDiff);
                    if (fresh) {
                        qs = Math.min(qs, h);
                        qc = Math.max(qc, h);
                        ps = Math.min(ps, temp);
                        pc = Math.max(pc, temp);
                    }
                }
            }
            if(fresh)
            {
                minHeat = qs;
                maxHeat = qc;
                minWet = ps;
                maxWet = pc;
            }
            landData.refill(heightCodeData, 4, 999);
            /*
            if(generateRivers) {
                if (fresh) {
                    addRivers();
                    riverData.connect8way().thin().thin();
                    lakeData.connect8way().thin();
                    partialRiverData.remake(riverData);
                    partialLakeData.remake(lakeData);
                } else {
                    partialRiverData.remake(riverData);
                    partialLakeData.remake(lakeData);
                    int stx = Math.min(Math.max((zoomStartX >> zoom) - (width >> 2), 0), width),
                            sty = Math.min(Math.max((zoomStartY >> zoom) - (height >> 2), 0), height);
                    for (int i = 1; i <= zoom; i++) {
                        int stx2 = (startCacheX.get(i) - startCacheX.get(i - 1)) << (i - 1),
                                sty2 = (startCacheY.get(i) - startCacheY.get(i - 1)) << (i - 1);
                        //(zoomStartX >> zoom) - (width >> 1 + zoom), (zoomStartY >> zoom) - (height >> 1 + zoom)

//                        Map is 200x100, GreasedRegions have that size too.
//                        Zoom 0 only allows 100,50 as the center, 0,0 as the corner
//                        Zoom 1 allows 100,50 to 300,150 as the center (x2 coordinates), 0,0 to 200,100 (refers to 200,100) as the corner
//                        Zoom 2 allows 100,50 to 700,350 as the center (x4 coordinates), 0,0 to 200,100 (refers to 600,300) as the corner


                        System.out.printf("zoomStartX: %d zoomStartY: %d, stx: %d sty: %d, stx2: %d, sty2: %d\n", zoomStartX, zoomStartY, stx, sty, stx2, sty2);
                        if ((i & 3) == 3) {
                            partialRiverData.zoom(stx, sty).connect8way();
                            partialRiverData.or(workingData.remake(partialRiverData).fringe().quasiRandomRegion(0.4));
                            partialLakeData.zoom(stx, sty).connect8way();
                            partialLakeData.or(workingData.remake(partialLakeData).fringe().quasiRandomRegion(0.55));
                        } else {
                            partialRiverData.zoom(stx, sty).connect8way().thin();
                            partialRiverData.or(workingData.remake(partialRiverData).fringe().quasiRandomRegion(0.5));
                            partialLakeData.zoom(stx, sty).connect8way().thin();
                            partialLakeData.or(workingData.remake(partialLakeData).fringe().quasiRandomRegion(0.7));
                        }
                        //stx = (width >> 1) ;//Math.min(Math.max(, 0), width);
                        //sty = (height >> 1);//Math.min(Math.max(, 0), height);
                    }
                    System.out.println();
                }
            }
            */
        }
    }
    /**
     * A concrete implementation of {@link WorldMapGenerator} that projects the world map onto an ellipse that should be
     * twice as wide as it is tall (although you can stretch it by width and height that don't have that ratio).
     * This uses the <a href="https://en.wikipedia.org/wiki/Mollweide_projection">Mollweide projection</a>.
     * <a href="https://i.imgur.com/BBKrKjI.png" >Example map, showing ellipse shape</a>
     */
    @Beta
    public static class EllipticalMap extends WorldMapGenerator {
        //        protected static final double terrainFreq = 1.35, terrainRidgedFreq = 1.8, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375, riverRidgedFreq = 21.7;
        protected static final double terrainFreq = 1.45, terrainRidgedFreq = 3.1, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375;
        protected double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final Noise3D terrain, heat, moisture, otherRidged, terrainLayered;
        public final double[][] xPositions,
                yPositions,
                zPositions;
        protected final int[] edges;


        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Always makes a 200x100 map.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link EllipticalMap#EllipticalMap(long, int, int, Noise3D, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 200, 100, FastNoise.instance, 1.0}.
         */
        public EllipticalMap() {
            this(0x1337BABE1337D00DL, 200, 100, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public EllipticalMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight,  FastNoise.instance,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public EllipticalMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public EllipticalMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail. The suggested Noise3D
         * implementation to use is {@link FastNoise#instance}.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         */
        public EllipticalMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed, the width/height of the map, and parameters for noise generation (a
         * {@link Noise3D} implementation, where {@link FastNoise#instance} is suggested, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
         * cannot be changed after the fact, but you can zoom in.  FastNoise will be the fastest 3D generator to use for
         * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
         * seed several times at different scales of noise (it's fine to use the static {@link FastNoise#instance}
         * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
         * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
         * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
         * that don't require zooming.
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public EllipticalMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[width][height];
            yPositions = new double[width][height];
            zPositions = new double[width][height];
            edges = new int[height << 1];
            terrain = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 10), terrainFreq);
            terrainLayered = new Noise.InverseLayered3D(noiseGenerator, (int) (1 + octaveMultiplier * 6), terrainRidgedFreq * 0.325);
            heat = new Noise.InverseLayered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 3), heatFreq, 0.75);
            moisture = new Noise.InverseLayered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 4), moistureFreq, 0.55);
            otherRidged = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 6), otherFreq);
        }

        @Override
        public int wrapX(final int x, int y) {
            y = Math.max(0, Math.min(y, height - 1));
            if(x < edges[y << 1])
                return edges[y << 1 | 1];
            else if(x > edges[y << 1 | 1])
                return edges[y << 1];
            else return x;
        }

        @Override
        public int wrapY(final int x, final int y)  {
            return Math.max(0, Math.min(y, height - 1));
        }

        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double coolMod, int stateA, int stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || coolMod != coolingModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
                minHeat0 = Double.POSITIVE_INFINITY;
                maxHeat0 = Double.NEGATIVE_INFINITY;
                minHeat1 = Double.POSITIVE_INFINITY;
                maxHeat1 = Double.NEGATIVE_INFINITY;
                minHeat = Double.POSITIVE_INFINITY;
                maxHeat = Double.NEGATIVE_INFINITY;
                minWet0 = Double.POSITIVE_INFINITY;
                maxWet0 = Double.NEGATIVE_INFINITY;
                minWet = Double.POSITIVE_INFINITY;
                maxWet = Double.NEGATIVE_INFINITY;
                cacheA = stateA;
                cacheB = stateB;
                fresh = true;
            }
            rng.setState(stateA, stateB);
            long seedA = rng.nextLong(), seedB = rng.nextLong(), seedC = rng.nextLong();
            int t;

            landModifier = (landMod <= 0) ? rng.nextDouble(0.2) + 0.91 : landMod;
            coolingModifier = (coolMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : coolMod;

            double p,
                    ps, pc,
                    qs, qc,
                    h, temp, yPos, xPos,
                    i_uw = usedWidth / (double)width,
                    i_uh = usedHeight / (double)height,
                    th, thx, thy, lon, lat, ipi = 1.0 / Math.PI,
                    rx = width * 0.25, irx = 1.0 / rx, hw = width * 0.5,
                    ry = height * 0.5, iry = 1.0 / ry;

            yPos = startY - ry;
            for (int y = 0; y < height; y++, yPos += i_uh) {
                thx = asin((yPos) * iry);
                lon = (thx == Math.PI * 0.5 || thx == Math.PI * -0.5) ? thx : Math.PI * irx * 0.5 / NumberTools.cos(thx);
                thy = thx * 2.0;
                lat = asin((thy + NumberTools.sin(thy)) * ipi);

                qc = NumberTools.cos(lat);
                qs = NumberTools.sin(lat);

                boolean inSpace = true;
                xPos = startX;
                for (int x = 0; x < width; x++, xPos += i_uw) {
                    th = lon * (xPos - hw);
                    if(th < -3.141592653589793 || th > 3.141592653589793) {
                        heightCodeData[x][y] = 10000;
                        inSpace = true;
                        continue;
                    }
                    if(inSpace)
                    {
                        inSpace = false;
                        edges[y << 1] = x;
                    }
                    edges[y << 1 | 1] = x;
                    th += centerLongitude;
                    ps = NumberTools.sin(th) * qc;
                    pc = NumberTools.cos(th) * qc;
                    xPositions[x][y] = pc;
                    yPositions[x][y] = ps;
                    zPositions[x][y] = qs;
                    heightData[x][y] = (h = terrainLayered.getNoiseWithSeed(pc +
                                    terrain.getNoiseWithSeed(pc, ps, qs,seedB - seedA) * 0.5,
                            ps, qs, seedA) + landModifier - 1.0);
                    heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps
                                    + otherRidged.getNoiseWithSeed(pc, ps, qs,seedB + seedC)
                            , qs, seedB));
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qs
                                    + otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA)
                            , seedC));
                    minHeightActual = Math.min(minHeightActual, h);
                    maxHeightActual = Math.max(maxHeightActual, h);
                    if(fresh) {
                        minHeight = Math.min(minHeight, h);
                        maxHeight = Math.max(maxHeight, h);

                        minHeat0 = Math.min(minHeat0, p);
                        maxHeat0 = Math.max(maxHeat0, p);

                        minWet0 = Math.min(minWet0, temp);
                        maxWet0 = Math.max(maxWet0, temp);
                    }
                }
                minHeightActual = Math.min(minHeightActual, minHeight);
                maxHeightActual = Math.max(maxHeightActual, maxHeight);

            }
            double  heatDiff = 0.8 / (maxHeat0 - minHeat0),
                    wetDiff = 1.0 / (maxWet0 - minWet0),
                    hMod,
                    halfHeight = (height - 1) * 0.5, i_half = 1.0 / halfHeight;
            yPos = startY + i_uh;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                temp = Math.abs(yPos - halfHeight) * i_half;
                temp *= (2.4 - temp);
                temp = 2.2 - temp;
                for (int x = 0; x < width; x++) {
                    h = heightData[x][y];
                    if(heightCodeData[x][y] == 10000) {
                        heightCodeData[x][y] = 1000;
                        continue;
                    }
                    else
                        heightCodeData[x][y] = (t = codeHeight(h));
                    hMod = 1.0;
                    switch (t) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            h = 0.4;
                            hMod = 0.2;
                            break;
                        case 6:
                            h = -0.1 * (h - forestLower - 0.08);
                            break;
                        case 7:
                            h *= -0.25;
                            break;
                        case 8:
                            h *= -0.4;
                            break;
                        default:
                            h *= 0.05;
                    }
                    heatData[x][y] = (h = (((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6) * temp);
                    if (fresh) {
                        ps = Math.min(ps, h); //minHeat0
                        pc = Math.max(pc, h); //maxHeat0
                    }
                }
            }
            if(fresh)
            {
                minHeat1 = ps;
                maxHeat1 = pc;
            }
            heatDiff = coolingModifier / (maxHeat1 - minHeat1);
            qs = Double.POSITIVE_INFINITY;
            qc = Double.NEGATIVE_INFINITY;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;


            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    heatData[x][y] = (h = ((heatData[x][y] - minHeat1) * heatDiff));
                    moistureData[x][y] = (temp = (moistureData[x][y] - minWet0) * wetDiff);
                    if (fresh) {
                        qs = Math.min(qs, h);
                        qc = Math.max(qc, h);
                        ps = Math.min(ps, temp);
                        pc = Math.max(pc, temp);
                    }
                }
            }
            if(fresh)
            {
                minHeat = qs;
                maxHeat = qc;
                minWet = ps;
                maxWet = pc;
            }
            landData.refill(heightCodeData, 4, 999);
        }
    }

    /**
     * An unusual map generator that imitates an existing map (such as a map of Earth, which it can do by default). It
     * uses the Mollweide projection (an elliptical map projection, the same as what EllipticalMap uses) for both its
     * input and output; <a href="https://squidpony.github.io/SquidLib/MimicWorld.png">an example can be seen here</a>,
     * imitating Earth using a 512x256 world map as a GreasedRegion for input.
     */
    public static class MimicMap extends EllipticalMap
    {
        public GreasedRegion earth;
        public GreasedRegion shallow;
        public GreasedRegion coast;
        public GreasedRegion earthOriginal;
        /**
         * Constructs a concrete WorldMapGenerator for a map that should look like Earth using an elliptical projection
         * (specifically, a Mollweide projection).
         * Always makes a 512x256 map.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link MimicMap#MimicMap(long, Noise3D, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, FastNoise.instance, 1.0}.
         */
        public MimicMap() {
            this(0x1337BABE1337D00DL
                    , FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given GreasedRegion's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
         * The initial seed is set to the same large long every time, and it's likely that you would set the seed when
         * you call {@link #generate(long)}. The width and height of the map cannot be changed after the fact.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param toMimic the world map to imitate, as a GreasedRegion with land as "on"; the height and width will be copied
         */
        public MimicMap(GreasedRegion toMimic) {
            this(0x1337BABE1337D00DL, toMimic,  FastNoise.instance,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given GreasedRegion's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
         * Takes an initial seed and the GreasedRegion containing land positions. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a GreasedRegion with land as "on"; the height and width will be copied
         */
        public MimicMap(long initialSeed, GreasedRegion toMimic) {
            this(initialSeed, toMimic, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given GreasedRegion's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
         * Takes an initial seed, the GreasedRegion containing land positions, and a multiplier that affects the level
         * of detail by increasing or decreasing the number of octaves of noise used. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact.
         * Uses FastNoise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a GreasedRegion with land as "on"; the height and width will be copied
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public MimicMap(long initialSeed, GreasedRegion toMimic, double octaveMultiplier) {
            this(initialSeed, toMimic, FastNoise.instance, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given GreasedRegion's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
         * Takes an initial seed, the GreasedRegion containing land positions, and parameters for noise generation (a
         * {@link Noise3D} implementation, which is usually {@link FastNoise#instance}. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call
         * {@link #generate(long)}. The width and height of the map cannot be changed after the fact. Both FastNoise
         * and FastNoise make sense to use for {@code noiseGenerator}, and the seed it's constructed with doesn't matter
         * because this will change the seed several times at different scales of noise (it's fine to use the static
         * {@link FastNoise#instance} or {@link FastNoise#instance} because they have no changing state between runs
         * of the program). Uses the given noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a GreasedRegion with land as "on"; the height and width will be copied
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise} or {@link FastNoise}
         */
        public MimicMap(long initialSeed, GreasedRegion toMimic, Noise3D noiseGenerator) {
            this(initialSeed, toMimic, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given GreasedRegion's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
         * Takes an initial seed, the GreasedRegion containing land positions, parameters for noise generation (a
         * {@link Noise3D} implementation, which is usually {@link FastNoise#instance}, and a multiplier on how many
         * octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers producing even more
         * detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
         * cannot be changed after the fact.  FastNoise will be the fastest 3D generator to use for
         * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
         * seed several times at different scales of noise (it's fine to use the static {@link FastNoise#instance}
         * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
         * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
         * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
         * that don't require zooming.
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a GreasedRegion with land as "on"; the height and width will be copied
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise} or {@link FastNoise}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public MimicMap(long initialSeed, GreasedRegion toMimic, Noise3D noiseGenerator, double octaveMultiplier) {
            super(initialSeed, toMimic.width, toMimic.height, noiseGenerator, octaveMultiplier);
            earth = toMimic;
            earthOriginal = earth.copy();
            coast   = earth.copy().not().fringe(2);
            shallow = earth.copy().fringe(2);
        }

        /**
         * Constructs a 512x256 elliptical world map that will use land forms with a similar shape to Earth.
         * @param initialSeed
         * @param noiseGenerator
         * @param octaveMultiplier
         */
        public MimicMap(long initialSeed, Noise3D noiseGenerator, double octaveMultiplier)
        {
            this(initialSeed,
                    GreasedRegion.deserializeFromString(LZSPlus.decompress(
                            "ᖢᠳ\u0088氦悠ぉ䓙㛃疓ᾏ尿䣢✶㒫敹✊盰织\u0CDC淶\u1FD5玿帛悟სᇢ捉䀡㇀׀ԃ̠\u0CE4倽䘾६.ど嗋䇗掊䞯\u0AC6ᖌ猘⎲䯬䃣䱟Ȥᮔ囵⟡㎡ᚲᠫዌђ\u2D26ڐ䍘෴℞吹ᡖ孶擉ྠ䒻㒵၊恝Ψᙤॉᄕ̡ᖕ᪁敨ᖨᝆӋᝳ娻Žዠす\u17DE∰屴啎▋慳 ᝐ⫑卲ฤ〸禷\u0DEB㕀ᕅ祩噛❆寰嫩旭⪾൵◬أ椩ழ䧩Ⱖ䥨ᥞ暯◉⅘咖᰷♙撹㿦暠䂗㠺̒ᚧⱖస岾㭹ኙಒ\u2EF5༪ູᡵ旈簡ఠ㐈恝〺㈈ਮཱུ╅ⱖ㚼嘐琫\u2E42\u0BA0瓷\u2BF2暅㺨ಬᔑ\u2E61\u1AFA◺吮\u31E6埰⛿ⱇ坒Łт喩唒偉ⶳ㒗 ⎊̀ᐮ⩕滹\u2BF3༘ᰰ睹侄\u2D2BⒺ濙洡⧅咫ᤪЯ░ଌ浨槓哬呾Ê䴎柖梑䩊㥤Ꮻ猑嗀挗Ꮹ㐠愢⼐ʫᤔ獩緔ⷰᒺⱛ\u2BF2ⲑ➱\u09D0䯷㽶ឍ㐴敻䭊揚▪ɍ滔氣ြ㶡ᰱ榓哥磀ኲ勆ᴕ总嗀ජ佋⒮宵挈ᙛ瑻⩔伉と崡擇异Ü榉᪪⛓珛盀皅旱㝙♵⊼捝削℄漩⍨牎ᅬⵚ䈬ᒢ䓉㒩㉑坌\u08D1ဤ爅㩈թĨ傠㈾ᓣㅊ曰༱◉十䐢䑩ྣੈӑ⇛ᅅٌ䛄㔼ѱ᥎晘☤埰瞱㓩強૰䑢䉌圧䜹挥⚨ؤұ⢘䫂瀯愨恩朩\u1AB8ᐩྱ㙴㈤䳲ḧ冐仔ඡパ搈⌢斅⊹䩃ģ䎘㩔\u0878犵໐౩䶒㉏䋰ᑦ៩义⠉ᄤ㔵祅䕑䒊䨝㲀䰹縦䖑ऩ侪㨱ଣ稲勅ᒕ䓈盆䜙㦂⼝⾴䃓フ䔸ᕘ杳山༴礷䐹\u2E5A⢡䵋⟕♦促َ\u13FD䘷䎘㫄₊➫为㈥琥ୌɔ瘴Ǳಿ⌘䍻ⱺ睊传〣䟨矗\u0B7A捪圹⒞楙ॆ≝䜷㈝ᕚ\u243B䁣爙䭡䙐犥᠐炎凙澇皱㝬敤ᓔĽվ♛濞\u2D26䑾嚣ᤲ冐ヵ勱穗愛嫞殥沱䝩僫攒ம梃့涔愥䝰卪ᏘᒎᏨ坼\u0EC7ệ䑨⾭ⓕ断冐秝\u0EDB炑\u0C51崣䖭槉⃝\u1AC3䑱㝛\u19DB⪷䂙榄〓撔࿘幐\u0C3A哨歵ᩎ䄩ٛ₵孚⥌ằ㎇⋁強瓇䂨ᕎ斨漖\u2B68ᘱ䔭档崷哧⢑ᴺ⏩唒偱䩋瓰㨾䙛穄䬵‰攮⾤㤸ဴ㥱ᵈ⾮ᕓᝓ⎔\u20C3\u31BB归⍉⛝义㴑绔Ǳ䌳庳◥Ⓢᗻ⊡\u0E5D䒐◔≄ዂ⸮ㇸ㪏╘˷絔䮙ス烰䬱फ粻塐༸ᵉ倠ᱨ̹瘓⾌儩璒ⷵ㳭╩瑷㙰⊖线咆❰悆䡊狋䠋咥㷫Ꮾ绹䙆❨估\u0BD2ⅵ㾋䛨堡㻟౩漽棆\u05C8材⎄≠䉖Ᏸ瀳ჸ✑窿屍ᢪ戬獠ᩇ㱛ͨ㮥\u0089\u0B80ⴹ∔ہؤ㮋㐶摝㘌\u0C00竳\u0C91在瓉\u05CA㩀⎬㯧㱃≈ബ䛔䔢ᒧ禃Ḁ塻懐噳手ቌࠣ懞ⶐ愯ᇎ\u0BE0䑿捡㢫⡩\u08CE弡Ӯ\u0B11㬼ハᏙ☬拎ᚣѵᄔⴶᄫ߃Ṣ紬㲁\u05CE搔ᷤ㲟ӑ⍯䉶૭璡抚ᨤ⑫䜀䑓\u1779䍞䂥౧ༀมᷙ埣㶨┈厭砨䪪率绱⪂䘢意\u0FE6晑剚⍢䥹ᜒ䛪勾↜唿ࡂᦱ䈺\u09D3㊩抋▤㚬ሒղਦ剫ၒ灣▮፮㑜⛼ኲ㤆ଠႤ灃䮂ᴽ⋜᬴椁仪冻೭ៀ樣テ⳹洭㧥㚰广Ԑ瞡哵♥䱤為チ昽ガ৮礞瘌焈帺e⼳ℎ᮱Ꮨ碌淪洊\u2432㎋ࣽᑰẪ⃘䃑Ῑ٘᪡碴⟌瘍㧪\u0C0Dද၊ጀ个䭼㺦ṵ嗹檋そ溁剫℆㏒⊃䅊⠭峏ង刴幦䋜毹ᇱტ㜳䩴₲๎狹ᑪ䈯ಐ䳚恹㡲ᙏ穠∢咏痄й嬓䆵㓚爉㉀帽㫪爓䨮擐沭Ⴈ༢替倱㉁翕䋸ᵉ㑍▬⺨㻱ȁ\u085F籗Ủ၊炄ᎆ\u312Fδஈאד昼愹ପ\u10C6\u05FBˊ䣤ٌů榥⡒䛗Ꮗଛ⤤欽ᤰ⊈Ꮸ⏨ᥒ勅≔᎔⭆帺ᗈ摎剬䂎瓽䔀䂀བྷ⁍ᙴ䬥䉄⋇ᭉ⠵⊣熵奫懪⊸唳ൌ㉢∔⁙ㅌ\u0588儆˅༠焚Ϫ椵䁓㲁䉛䁰嵲ゕᢠ䭚ⵠ۶杌痸仭籅檰㗠ⴹᖱᵰ橴倳\u0C50∎䦀ẫᖬ揸嶕徃㱲僴͑䉓ቖ渌౽檕㒣ᵏ彛Ⴛၟ㓢ᘡҸኵᮉ滽㯼͍ਧⓖ攵ᥠ尿㗠⧴展⡡㝲ᰬᣰ䔭呐篗䵮ຽ༴涙ᔽ⻣㈾ใ竓子柔棠懠⻡䵵啠惭梁䪍ᷕႆⅣ㓚䌽∧略䕲\u242F摥䪈Ἤھḁ㢨㣈ᨉశ䰤愻ᖊ塢₵➖ᜄⳇⰄュ౬䑘淴卽\u1AC6̲㓚ې瑚㚬㊤棆椔粭巓儑冣烒峰嘲癜₂ᬅվ啕ピ䴼ḩ樿ᩓ\u00AD県る⺟ᯍ幄┏敃䄸疦\u1DF4⣜୷⨎旄\u4DB9㠬㒐ડ▋ៃ䋌屈်娦杻எᅿ続㐹町\u0B8C椡嫾㦾峡㎁憈ᜮ␖\u0AF7妈㛁ⷮ徬ᣴ◧瑳\u0560㛞灂唊㴨͠䵒ᵎ惵䃾断姰ⷘᠯ䙬淫㩵ݴ㴾ⅱ㨔希⢫岄ŏ摉掜Η垫穆π墉奪毤ᖪऐ敖墷ॴ䧯寞⁀榧ᶑඬᬮ嵊ȸᖤ䵠庯ਿ浬姮᱖䀱හ\u18F8燱ὔ㕴䵣垏፭\u12B6屜洴皽禴砒粴˲\u18AC᧹ᴔ՝紊姳䡾滄痱崇狉物᪦唫埩珑㯣ᚸ剗姯䌉侙窼◴䆴⟑❿僥缾ؑཝᬒ僝䝾炾巤ᶏ瘃䮁㮧䜷ᡯᵴ崞狹ᚍ\u10CC巗湅ᵼ䟪䷴䫅粲╥଼ٕ㗪⺉⥏㴣喂婼剗\u2B92剻㫇捽▵\u0C75ᢁ玈弭ᠺ焍\u0B4E竽盺ه廜㟘䫠\u2438ᔨ揤➿国溫扜䯴㻽凃摟Ⓒ枴㴇ၮ祡ㆠ䢤㍮ၐ檌ຄ䒇ᦒ枝\u2B68ⲟ孷⭖㱾䁙傛甈ٯ嫣唎ᮩቲ̫⧧璒⌧力ߚ䮸卣妰\u009F∞ړ絅䊓䠖籓簁䆗紓㜏撦㠈尔⎈Ṑ㟩湟㢛ሸ௭祫ᾓ嫭員㬠ᶉ᥈䮼⬁፼∆䕁‶ౘ玉厴口∣ɹ圐⬥圶愛ᜩ༨㭄Ӽ䰣⠿ㅑ⎉ɀ䳄殂≠ϴ\u08C5疄⩜Ĺ䑯ⅽ㉤ᦥ嘃ᦋ⇬ް㨨㠦Ԝ䤴᱈溏䂀值㍮ᡮ㎕\u0E71㼤\u086B䌬\u0863\u08C1夿⩈倥恢ů媉ᑵ㴯ᄃ匉ᜣ䜍圢ڦᏘᨼį妠䝣嵫\u0B49⫢ᇠ丌ㄜԠ⿸♣⩋\u23FD䴙८ѝ㊈ᤨ狡Üޛᗢ桢㈩⣏ዡ槈婁慸Ꭳ❯㵀䝘⑃㬏癃犤Ꮳ㪎㹥凜щ測⠭Ɉٱ䄺㝗ޜɃᔪ庈䟸ℵ䝪䢗獶䓃☶ऒ䛧ᖵᔵ\u08D0ㆳḀண捆䚤⣱愶棙ѕ\u0D98䑦Ѵ㈔ၐ箠椴䖻䵃䅬ᣨ䖬㢩Զ呝㊡䲝⌇祡側㒃儰䰪愫᩠嬼杒䫔㑈⌵⤉Ǖ弴ᝯ䝂挨唃⬆ሦ㥌⏂挷ᕺ拉\u0DFD紨礉氟䯜᪪惝䝼⡐劫䉵ገ\u10C9㓄礻㪗弽䂹⨴炧ẙ槦Ⅎ㊶᠂嬫䡘⓬ĳ県犖僂⒙梳ᣔ扢卑䬠Ү移寝ⰼṔ珜岳䄥ፃ卒ὡာ磑㤍ᆲ梼操㧸ᭁ甩䅮掲ⓢ̰壞✄Ḅ嬸畦抖ᥢ䰱慿⭂⻙Ⓖ擂䔕Ӑᑣ磏î̳ỂႿ⬫ᥙ檴攁䍅ᖁ缀刳̢ͼ䪼㔚戭ᩳ⏢╭㏌ᝨ㿈呰Ꮲ塣⨼瓈̲㳳粴渥⡳䞸猩ֆ㈼Ձ渤汙䌢\u1C4Cⓢⱪ猦㕙↴ヹ䚻宬ڻ䅮熺⍒㊼ᢃ⤛̀㯨᜴䋤ԑֺ䔂擌ܙ儠唰䍔൰崽ぽɈᵀᒨҔ时ⷳ挱ぷ槸ὀ澭Ʉ䩅ሀ⌨摯\u0AF4\u08C0綱哆犲䢀ᘤ㑙晤ᡅ桉㖌捍ὠ泫⳥杳ᓌ慯梃僇ྐ\u0EEDٷⓞᓀޱ㔓箅叁勡喔牾㫠ስ䅜⋾㬓潁⡛ةဉ濧ޜ厠䛔㫨慮݈Ɠ瞶ぎ䤽॓ḧ嵄爁ń囬奢簎⠒倈泤䍫ਬ䡄䞔\u087Aᓒ峆㕊\u0866᧘◪癡㬾⑀痫潂⡧᪀㊣䌆摡㥴䛢溊ᓤᜂᨧ亀䀷ᤪ溭渵昗Č哋㳵↑ⳙᖸ癭犖呪ᓂ灠⎎ᜉ狭佣㣄ह⬲喂५౫ᓂ勩\u1737\u05F8㉳ࡍᒡ\u0DE9ᛂጝᕉ≫ᱵ⋓ᕟ᭪厵磆昁⥴↠狳ᗗᳫ六睵䖟ᩨ汹犮ቱ⟫无勻ក䧌ܺ˶ᒙ\u2D78并助梉\u3101㱶棝â᠌\u2073挤噊䥊慾ᴨ⇅㫉Χ䴙ᗙ㜪ռ\u0884唎䈔ঈ⌁ၴ䕀糥⪰嘵㿠彤̦啚䰹✼で䌹崥止⋳Ⱈ㲫嘭欛歲ᇋ毮ᑂ䦧Ῐ䢡ℑ戺ㆢ人唉囁俰⋮硊䏓丁㸎礿䋘ᶋ纽畠枍㟋㭼∥坓\u1CA5丩㥱⌄\u23F9䤺⢶䝽㚊絶牛ਐ䡤殹╭ᇍ㮲罅欈䊱彁ᎄ\u0E7A⟘ᜱ㻭䣕嘢ㄲ㴿䨰筬\u0A3B撍♓䂴ტ歬ぉઽ㿨㘎䋵䯔͇ᢩ嵶ᬮᓩ巩偡⏈䨀䷪窫磏䤜ち✆᭘ߨ㓳䜟响啄俌䙓吮䶔⠹沼㊓⼉峿\u0893ႸӠԂ梓℃㫉滦ˁ祐䨪勵⚾㗫ⵕ¤嚮懸䬄\u1FB5䍏柱ኺⲋ嚯䭋䊼Ӻᢶ\u08B2⋳┦欺ᩌ\u1C81㾀溚ᇔඅ┥䊷㞅䷻⸉盔\u09D1͘剸狿᠘ᗻᘮಘᯣႪ佻盧掘啋૮㠸甛⊈⠅䦖兙ᅉ⨍湓¼ᘀ癍ौ爧㙛廭沢⦣ૈ⁼灳䋤წⳡ䐪睧ૠ姿\u0EFA疈䴥㋨Ὠ⃡ፚ燳\u2EFC痦⊢嗩\u243E䚜ᷛᘋܫÐ㮩叁⚭㦪ࠢ俇\u1CB5ᅯ⡍函䮂䈞⁝⚄拵盱\u1C4C乧㱽焙帹㏾欣痎⤚\u16FA弐ϓ匀ⶢሬ䛝ਫ▉娴嬑候啯㫡䉝㖀㡌绎倠ⵀ兩㢲悌ᰛ\u1AFD累ኡ怌叹紑䇳䋻ห禌反Ꮣ攥䖈✖⸧ၰ䈟瑻㽻㢢҃䇵˙⪮䖘᩹Ⴇᦀ縦妈ዌ棲悥䐟ᙱ㑀瘱䃇Ń矡䚸寵䕡ඈ⚧Ű䯕ᄠ塵卟㗤活㡧˥䌚≻⡹歖ᵀ帣炨⊠ᷘ曩倣⨆㓀ⳍⅿ扚䅪橐䮆䊈抣㪾䒇瞍Ꮵ⤦Ṟ\u0FF1⠔᱄䞅晸斬ᩊ䡯棹㟘癶䚈樧㺚䶮㙰䭗㧹杂楥⛈汈㹓㡍\u0E5F㾐䱘噱໐抌⧴™સ孱䮵Ҷ䃩ṱ⩠皆ᑉ⧭㡷ᒇ΄Ⴆ⢎&\u2BE1ః≧ぃ̲᳀俾⨆亠东ଯ⠠㈬䈨甠\u0D54⤒ᯘ浟硩㮯㸼劬䉳⡔\u0C04㪫田≹֪叢槰揔檇㕃抒॥䛰汫砨愲\u1CACᔢ殆䲵㎇ፚ廐痝⸨㏡ʋ扚ሑⵋ咙ᯠ䌴咷\u088A㤜\u087Bᙌ్\u0CD4䳰偗ᡈ䘼Ї㡥秠䥇ކ⩦砺䎘Ⲉ偁媀檍چ枹בᮜ楙㝒ͳ䈬១唬吨ⱝ䮁僙剸ⶤɉ⺫౩扆䴓漥煼↮ᴧ㽕ᅥᨪૠ愤䀯⦡ა繛㉁䫼掷⚬ᐾ⸌綕泇㓢㩍ሃ厼\u2437↲\u08CDᢴ犉᭪潉㋝ᢶ矂ỹڳ幮䞸᭔\u1ADB㦩䖊\u0A31䫘偩ⷊ棶㸷ᘎ䔏㱶⫐㘕\u2EFA灐枩䱍\u23FAា囚⦻⾛㛁\u206Aಞ䄌䤠ⲻ䡸ͨ悻晣皹⎵㒈瞭喃㥢㓹ᇔᜂღ懜勯㑐淄ɖ嶶䷄眷䭗⇙的渆汭Ӹⷬ求䓗䜄玅塜媳给兢瑵ᄠ㱄孉竒㏭毡䷶濺ޤط桤敘ᄘ抯伊淖昢\u2B5A硳朗ۆ称䩧掦వ淙祁淮璖煡ᶫ㡄ఁᙉ〰护ୠ狞᷅汫䗉ࠠ㸅⬾洛ௗ䨫潊ዥ咣樳䋄勓痻捺㧭༲\u2FD6嵠䂸ЖᏒ\u1B4D叮㙴嬡ڵ䥣Ì䇡ḓ券䎁悞灨ṹᅠ゜淸泝ᠹ⇑ΰ竁攜握䷠㍴済㢜䏜ᱱമቑ䏬ㅿ✹凃᥀校᪐杓⏆㈰䷚ಔᔓ͑稳犎䶠渕ႃ᳖ᗎẊេ挩ē៷\u3103˭瑎᳁̺硒❓清۵勽⛏䌶ᅯ㲒吚矵㰊¤䁿䍠ৡफ籮䏾戢♺湗㆜Ӯ⡥⠫ᴔ廛₵छ⁙泯湫澍济瓺䄉ᐃᷥ凯暟⺙凕ᵺ᰿噀已௯些玾筀ℌደ䙯Ῐᑎ㺕㏈Ņ皍⦜\u0BD8嶥歏ᆑ̈忚哮娀噎䊒⢩\u2E6A暡䭡ᥰ媲ࡂ䫲羥ᩪ犇竷䌪廖籣傝嫇磪爣涂嘀ᒯ䞓ޅ罪㇠硺\u2BA5籽䓮摴䊹擠㔡批⧈⓹籡偹ᘧ\u19DB冦緊ෛ䭇殔ᔽ䋴ᮏ晸㘺䔴䠕❣桄ˀ倔䊨䖜㢫θ寺碉眂䃭仫㐖瀧ḏ៷ښὋ\u09C6´䏌⌍搯㙆ϰ㿰⬽孡䉆䴣仔\u08E0߲ᆞ皈椘䙫ᝣ甥ѡ㊧握昦瑨⟏掣璯ᄘࠐጣ笾睾篳ⸯ㒿ۢᘇ尳烹㊧呖歓樿妧ᐁㄡह㊛䱊愃我ᴛ⢝⇄みᙶ䌥㙱୰ིࠊ懊\u08D0峓承ŉẹ睤ᰤⶴͥ瓿㹰棈伉⡪ቨ䶂ᖑ烒⡕Ḏᖙ⎼\u086Dཱྀ亾恝㠗\u08CE囕厦ཇ噇䞐掙㡬厄\u23FAア⒘㙋瑾婿砉䈗ᅈఎⱵ䵊\u0A61೫䘹恐䊩ᘈ䚐排ᭀ༧⩅ଃ\u175F䖡᭢ᙝ湄峐䩓呭敁ӗ⡰协ㅡ丣煛\u1FDCዃ喺惇ਭ㠩㸌䘳勸ܓ▢倿㫵⨁窺{\u0AE5ὈŔ⡥䄋с㺎ᢪጽ֢㎚㚊₮Χょኗ㓶ө㑰⒒箠๐⍯尔\u2BE5ᕤ̯䴭∘橡ጐ䄦峊ᔨ反әᭃḸ廪㭚桖婀㏂测搀⧡㏑洡㺚慡㓘ٰ晡Մ☴₇䈤̤㖑怢⣱\u0885儸▀夒倳榯\u2451ᐨ≲\u0096‑䴡⣫偉ᤌ\u20BBफ़懡糣燀Ź獪\u0D5AŰ煀\u08B2倣⃜ど姐潀戢ধ㨦ě࿚㒭䔲䆰房䤫 䜘ඹ媋氢盵䌛䒈྄㗢⢣\u0D81ᄎ旄ੜ䛪傭᱘汞㉀℁Υ淡爰䖅攍Ꮀ寄眡पᦿ瀣ψ\u0EE2ͱⰷ\u18AF甴↝凞\u0EF2cĤⅇ㡵⤘ณ\u0B00㿗\u0B3Aሯ¬⌠⫪☙䭍䌢䣶䂸႖㗡ŀǪՀဠᕓŸ⡃巍捝ङ⥍䌈ଥℬ㈤䈳儁\u0094ᛊ老ᛣ㿔⢉妭⚧ō䇃ᴨၓㅱĮᝂ篙Ϭㅞ\u202A祢䞈\u1DF9祣穥℧典㞖ᥢ䪫⒧⑇䇎毅՞㼱◰㬩兘䔉搿奢ᱠ\u052B◌ṩእચ←㛶䮺〣哚䝚䵤ⳁ嬁ᮤ䘰䈔掶ᄐḁ憣\u0B98ᤎヹ᎔垀昦⏉ᔁӔ䦤⺙ृ庱⒖४㍘㦨\u0BA7唯ਢ䱂⦧慾㚙ㆵ㍽ጴႲᄹք枃㈻ᔵ缉碸\u08D0ހ縩簠媂墜䜖Ụ煩⨳ⰳ㋼夵䢪˕籠稣ൽϒ吉屘禁⫥朿\u0E8B叿ೈ⦑Զ䤡䈭ᆷ唓䏠筃满坛\u244Fޑ䑙ཽ琮䟩゜灺昉ᮮ⣓燥䤩拐\u0B91䬔⏠ℕ弸璕\u09BA砐嶰\u0A78\u0B65浈呥ƌ伤⍘瓤᭼値淝吜ጐ哰樭璾ಓ礊Ⰴង㭆ᒨ烶ϖ珼䒀㖱秃㰪氵òూᒢิ映买䈵\u23F8\u18FC氋篦悴礎⣵⽘ṵᧀ武ᑲ६䚅䌢ẉ➡у䃐⥭\u2073\u206D⽂加傏䠬ጅ后䃙滅Ҵ儣ᢌׄହ㗣欥栻⇄ͼ墖ᾱᗊ\u05CD爯8⳺ខढ‱ࠢ桉̒\u1774濫‣堦岛劦撨\u0DC8僴ᥠ䪟ⷢ䈂\u1316㸺妯哉塎扂乑ƀ浣⑮摋懭݈༦嵫䙂巁怨搘䠰\u0B00䢠⊿牅栲ȃỮἰ㬹嬱䆚ᅀ毛㑠喅挠᎒⣳ॲ厸淤惢呞⩍䨵Ⅻኸ㗗㴩碣ᖴ垹Ὀ監㞩乙ŕ懱♸浠ᖨ孫潜ᣇ֨噀⑇眵䁚Ч䙋䒖㒑㧣䜣㤾ぃ烪循̢\u0A61䬩ㇰ⟤⊈㴅㝕‥ᤇ捘■\u05C8窫悪䁛⫣䦯殄㒠洡㑕䔜哉㐎࿘㻁稠͝ᘨዱৠ滈懤圮⅔µᔂ\u2BE0㬃古扁氶婐ⱬᕅ呣悯\u2E76ኺᆤⷀ䁢敨硙磈Ǫ✛ₜ䩆厵`⒃ቢự祒墲⚻峙Ǒ䦒\u2B79䰇嗳≷ᎊ惝ౄ㑑冢ᡁ᳔╅檰ိ叀箭\u0095㗓◴共ⴀ廤⭙ₔӘஈ\u3101垁䦽ᑱೣϛᮔ挡倧眩ⱱ儒慨۹᎓‰儁䇳▂⒜ૄ⊵夰䒯䃵ᩄᯍ爃禤⩳Iª⃩䎠✅㶷䁷媳步%纥ሺ丶搓Т⻪篦敮枷爌⠩➢ඡ捩㹄Ӛ㰦係峑㼦䚶䁖Ἆ冰૯㫜堢䠮悎\u3103䍀㓉⢋䧣滆§➾⊔䪲આ৴\u2B60秤ښ㑠埄庾穻楌Ġ "
                    )),
                    noiseGenerator, octaveMultiplier);
        }

        /**
         * Meant for making maps conform to the Mollweide (elliptical) projection that MimicMap uses.
         * @param rectangular A GreasedRegion where "on" represents land and "off" water, using any rectangular projection
         * @return a reprojected version of {@code rectangular} that uses an elliptical projection
         */
        public static GreasedRegion reprojectToElliptical(GreasedRegion rectangular) {
            int width = rectangular.width, height = rectangular.height;
            GreasedRegion t = new GreasedRegion(width, height);
            double yPos, xPos,
                    th, thx, thy, lon, lat, ipi = 1.0 / Math.PI,
                    rx = width * 0.25, irx = 1.0 / rx, hw = width * 0.5,
                    ry = height * 0.5, iry = 1.0 / ry;
    
            yPos = -ry;
            for (int y = 0; y < height; y++, yPos++) {
                thx = asin((yPos) * iry);
                lon = (thx == Math.PI * 0.5 || thx == Math.PI * -0.5) ? thx : Math.PI * irx * 0.5 / NumberTools.cos(thx);
                thy = thx * 2.0;
                lat = asin((thy + NumberTools.sin(thy)) * ipi);
                xPos = 0;
                for (int x = 0; x < width; x++, xPos++) {
                    th = lon * (xPos - hw);
                    if (th >= -3.141592653589793 && th <= 3.141592653589793
                            && rectangular.contains((int) ((th + 1) * hw), (int) ((lat + 1) * ry))) {
                        t.insert(x, y);
                    }
                }
            }
            return t;
        }

        @Override
        public int wrapX(final int x, int y) {
            y = Math.max(0, Math.min(y, height - 1));
            if(x < edges[y << 1])
                return edges[y << 1 | 1];
            else if(x > edges[y << 1 | 1])
                return edges[y << 1];
            else return x;
        }

        @Override
        public int wrapY(final int x, final int y)  {
            return Math.max(0, Math.min(y, height - 1));
        }

        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double coolMod, int stateA, int stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || coolMod != coolingModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
                minHeat0 = Double.POSITIVE_INFINITY;
                maxHeat0 = Double.NEGATIVE_INFINITY;
                minHeat1 = Double.POSITIVE_INFINITY;
                maxHeat1 = Double.NEGATIVE_INFINITY;
                minHeat = Double.POSITIVE_INFINITY;
                maxHeat = Double.NEGATIVE_INFINITY;
                minWet0 = Double.POSITIVE_INFINITY;
                maxWet0 = Double.NEGATIVE_INFINITY;
                minWet = Double.POSITIVE_INFINITY;
                maxWet = Double.NEGATIVE_INFINITY;
                cacheA = stateA;
                cacheB = stateB;
                fresh = true;
            }
            rng.setState(stateA, stateB);
            long seedA = rng.nextLong(), seedB = rng.nextLong(), seedC = rng.nextLong();
            int t;

            landModifier = (landMod <= 0) ? rng.nextDouble(0.29) + 0.91 : landMod;
            coolingModifier = (coolMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : coolMod;

            earth.remake(earthOriginal);

            if(zoom > 0)
            {
                int stx = Math.min(Math.max((zoomStartX - (width  >> 1)) / ((2 << zoom) - 2), 0), width ),
                        sty = Math.min(Math.max((zoomStartY - (height >> 1)) / ((2 << zoom) - 2), 0), height);
                for (int z = 0; z < zoom; z++) {
                    earth.zoom(stx, sty).expand8way().fray(0.5).expand();
                }
                coast.remake(earth).not().fringe(2 << zoom).expand().fray(0.5);
                shallow.remake(earth).fringe(2 << zoom).expand().fray(0.5);
            }
            else
            {
                coast.remake(earth).not().fringe(2);
                shallow.remake(earth).fringe(2);
            }
            double p,
                    ps, pc,
                    qs, qc,
                    h, temp, yPos, xPos,
                    i_uw = usedWidth / (double)width,
                    i_uh = usedHeight / (double)height,
                    th, thx, thy, lon, lat, ipi = 1.0 / Math.PI,
                    rx = width * 0.25, irx = 1.0 / rx, hw = width * 0.5,
                    ry = height * 0.5, iry = 1.0 / ry;
            yPos = startY - ry;
            for (int y = 0; y < height; y++, yPos += i_uh) {

                thx = asin((yPos) * iry);
                lon = (thx == Math.PI * 0.5 || thx == Math.PI * -0.5) ? thx : Math.PI * irx * 0.5 / NumberTools.cos(thx);
                thy = thx * 2.0;
                lat = asin((thy + NumberTools.sin(thy)) * ipi);

                qc = NumberTools.cos(lat);
                qs = NumberTools.sin(lat);

                boolean inSpace = true;
                xPos = startX;
                for (int x = 0/*, xt = 0*/; x < width; x++, xPos += i_uw) {
                    th = lon * (xPos - hw);
                    if(th < -3.141592653589793 || th > 3.141592653589793) {
                        heightCodeData[x][y] = 10000;
                        inSpace = true;
                        continue;
                    }
                    if(inSpace)
                    {
                        inSpace = false;
                        edges[y << 1] = x;
                    }
                    edges[y << 1 | 1] = x;
//                    th += centerLongitude;
                    ps = NumberTools.sin(th) * qc;
                    pc = NumberTools.cos(th) * qc;
                    xPositions[x][y] = pc;
                    yPositions[x][y] = ps;
                    zPositions[x][y] = qs;
                    if(earth.contains(x, y))
                    {
                        h = NumberTools.swayTight(terrainLayered.getNoiseWithSeed(pc + terrain.getNoiseWithSeed(pc, ps, qs,seedB - seedA) * 0.5,
                                ps, qs, seedA)) * 0.85;
                        if(coast.contains(x, y))
                            h += 0.05;
                        else
                            h += 0.15;
                    }
                    else
                    {
                        h = NumberTools.swayTight(terrainLayered.getNoiseWithSeed(pc + terrain.getNoiseWithSeed(pc, ps, qs,seedB - seedA) * 0.5,
                                ps, qs, seedA)) * -0.9;
                        if(shallow.contains(x, y))
                            h = (h - 0.08) * 0.375;
                        else
                            h = (h - 0.125) * 0.75;
                    }
                    //h += landModifier - 1.0;
                    heightData[x][y] = h;
                    heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps
                                    + otherRidged.getNoiseWithSeed(pc, ps, qs,seedB + seedC)
                            , qs, seedB));
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qs
                                    + otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA)
                            , seedC));
                    minHeightActual = Math.min(minHeightActual, h);
                    maxHeightActual = Math.max(maxHeightActual, h);
                    if(fresh) {
                        minHeight = Math.min(minHeight, h);
                        maxHeight = Math.max(maxHeight, h);

                        minHeat0 = Math.min(minHeat0, p);
                        maxHeat0 = Math.max(maxHeat0, p);

                        minWet0 = Math.min(minWet0, temp);
                        maxWet0 = Math.max(maxWet0, temp);
                    }
                }
                minHeightActual = Math.min(minHeightActual, minHeight);
                maxHeightActual = Math.max(maxHeightActual, maxHeight);

            }
            double heightDiff = 2.0 / (maxHeightActual - minHeightActual),
                    heatDiff = 0.8 / (maxHeat0 - minHeat0),
                    wetDiff = 1.0 / (maxWet0 - minWet0),
                    hMod,
                    halfHeight = (height - 1) * 0.5, i_half = 1.0 / (halfHeight);
            double minHeightActual0 = minHeightActual;
            double maxHeightActual0 = maxHeightActual;
            yPos = startY + i_uh;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                temp = Math.pow(Math.abs(yPos - halfHeight) * i_half, 1.5);
                temp *= (2.4 - temp);
                temp = 2.2 - temp;
                for (int x = 0; x < width; x++) {
//                    heightData[x][y] = (h = (heightData[x][y] - minHeightActual) * heightDiff - 1.0);
//                    minHeightActual0 = Math.min(minHeightActual0, h);
//                    maxHeightActual0 = Math.max(maxHeightActual0, h);
                    h = heightData[x][y];
                    if(heightCodeData[x][y] == 10000) {
                        heightCodeData[x][y] = 1000;
                        continue;
                    }
                    else
                        heightCodeData[x][y] = (t = codeHeight(h));
                    hMod = 1.0;
                    switch (t) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            h = 0.4;
                            hMod = 0.2;
                            break;
                        case 6:
                            h = -0.1 * (h - forestLower - 0.08);
                            break;
                        case 7:
                            h *= -0.25;
                            break;
                        case 8:
                            h *= -0.4;
                            break;
                        default:
                            h *= 0.05;
                    }
                    heatData[x][y] = (h = (((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6) * temp);
                    if (fresh) {
                        ps = Math.min(ps, h); //minHeat0
                        pc = Math.max(pc, h); //maxHeat0
                    }
                }
            }
            if(fresh)
            {
                minHeat1 = ps;
                maxHeat1 = pc;
            }
            heatDiff = coolingModifier / (maxHeat1 - minHeat1);
            qs = Double.POSITIVE_INFINITY;
            qc = Double.NEGATIVE_INFINITY;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;


            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    heatData[x][y] = (h = ((heatData[x][y] - minHeat1) * heatDiff));
                    moistureData[x][y] = (temp = (moistureData[x][y] - minWet0) * wetDiff);
                    if (fresh) {
                        qs = Math.min(qs, h);
                        qc = Math.max(qc, h);
                        ps = Math.min(ps, temp);
                        pc = Math.max(pc, temp);
                    }
                }
            }
            if(fresh)
            {
                minHeat = qs;
                maxHeat = qc;
                minWet = ps;
                maxWet = pc;
            }
            landData.refill(heightCodeData, 4, 999);
        }

    }
    /**
     * A concrete implementation of {@link WorldMapGenerator} that imitates an infinite-distance perspective view of a
     * world, showing only one hemisphere, that should be as wide as it is tall (its outline is a circle). This uses an
     * <a href="https://en.wikipedia.org/wiki/Orthographic_projection_in_cartography">Orthographic projection</a> with
     * the latitude always at the equator.
     * <a href="https://tommyettinger.github.io/DorpBorx/worlds7/index.html">Example views of 50 planets</a>.
     */
    @Beta
    public static class SpaceViewMap extends WorldMapGenerator {
        //        protected static final double terrainFreq = 1.65, terrainRidgedFreq = 1.8, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375, riverRidgedFreq = 21.7;
        protected static final double terrainFreq = 1.45, terrainRidgedFreq = 3.1, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375;
        protected double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final Noise3D terrain, heat, moisture, otherRidged, terrainLayered;
        public final double[][] xPositions,
                yPositions,
                zPositions;
        protected final int[] edges;

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Always makes a 100x100 map.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link SpaceViewMap#SpaceViewMap(long, int, int, Noise3D, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 100, 100, FastNoise.instance, 1.0}.
         */
        public SpaceViewMap() {
            this(0x1337BABE1337D00DL, 100, 100, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public SpaceViewMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight,  FastNoise.instance,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public SpaceViewMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public SpaceViewMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         */
        public SpaceViewMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed, the width/height of the map, and parameters for noise
         * generation (a {@link Noise3D} implementation, which is usually {@link FastNoise#instance}, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
         * cannot be changed after the fact, but you can zoom in. FastNoise will be the fastest 3D generator to use for
         * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
         * seed several times at different scales of noise (it's fine to use the static {@link FastNoise#instance}
         * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
         * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
         * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
         * that don't require zooming.
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public SpaceViewMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[width][height];
            yPositions = new double[width][height];
            zPositions = new double[width][height];
            edges = new int[height << 1];
            terrain = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 10), terrainFreq);
            terrainLayered = new Noise.InverseLayered3D(noiseGenerator, (int) (1 + octaveMultiplier * 6), terrainRidgedFreq * 0.325, 0.475);
//            terrain = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 8), terrainFreq);
//            terrainLayered = new Noise.Layered3D(noiseGenerator, (int) (1 + octaveMultiplier * 6), terrainRidgedFreq * 5.25, 0.475);
            heat = new Noise.InverseLayered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 3), heatFreq, 0.75);
            moisture = new Noise.InverseLayered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 4), moistureFreq, 0.55);
            otherRidged = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 6), otherFreq);
        }

        @Override
        public int wrapX(int x, int y) {
            y = Math.max(0, Math.min(y, height - 1));
            return Math.max(edges[y << 1], Math.min(x, edges[y << 1 | 1]));
        }

        @Override
        public int wrapY(final int x, final int y)  {
            return Math.max(0, Math.min(y, height - 1));
        }

        //private static final double root2 = Math.sqrt(2.0), inverseRoot2 = 1.0 / root2, halfInverseRoot2 = 0.5 / root2;

        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double coolMod, int stateA, int stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || coolMod != coolingModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
                minHeightActual = Double.POSITIVE_INFINITY;
                maxHeightActual = Double.NEGATIVE_INFINITY;
                minHeat0 = Double.POSITIVE_INFINITY;
                maxHeat0 = Double.NEGATIVE_INFINITY;
                minHeat1 = Double.POSITIVE_INFINITY;
                maxHeat1 = Double.NEGATIVE_INFINITY;
                minHeat = Double.POSITIVE_INFINITY;
                maxHeat = Double.NEGATIVE_INFINITY;
                minWet0 = Double.POSITIVE_INFINITY;
                maxWet0 = Double.NEGATIVE_INFINITY;
                minWet = Double.POSITIVE_INFINITY;
                maxWet = Double.NEGATIVE_INFINITY;
                cacheA = stateA;
                cacheB = stateB;
                fresh = true;
            }
            rng.setState(stateA, stateB);
            long seedA = rng.nextLong(), seedB = rng.nextLong(), seedC = rng.nextLong();
            int t;

            landModifier = (landMod <= 0) ? rng.nextDouble(0.2) + 0.91 : landMod;
            coolingModifier = (coolMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : coolMod;

            double p,
                    ps, pc,
                    qs, qc,
                    h, temp, yPos, xPos, iyPos, ixPos,
                    i_uw = usedWidth / (double)width,
                    i_uh = usedHeight / (double)height,
                    th, lon, lat, rho,
                    rx = width * 0.5, irx = i_uw / rx,
                    ry = height * 0.5, iry = i_uh / ry;

            yPos = startY - ry;
            iyPos = yPos / ry;
            for (int y = 0; y < height; y++, yPos += i_uh, iyPos += iry) {

                boolean inSpace = true;
                xPos = startX - rx;
                ixPos = xPos / rx;
                for (int x = 0; x < width; x++, xPos += i_uw, ixPos += irx) {
                    rho = Math.sqrt(ixPos * ixPos + iyPos * iyPos);
                    if(rho > 1.0) {
                        heightCodeData[x][y] = 10000;
                        inSpace = true;
                        continue;
                    }
                    if(inSpace)
                    {
                        inSpace = false;
                        edges[y << 1] = x;
                    }
                    edges[y << 1 | 1] = x;
                    th = asin(rho); // c
                    lat = asin(iyPos);
                    lon = centerLongitude + NumberTools.atan2(ixPos * rho, rho * NumberTools.cos(th));

                    qc = NumberTools.cos(lat);
                    qs = NumberTools.sin(lat);

                    pc = NumberTools.cos(lon) * qc;
                    ps = NumberTools.sin(lon) * qc;

                    xPositions[x][y] = pc;
                    yPositions[x][y] = ps;
                    zPositions[x][y] = qs;
                    heightData[x][y] = (h = terrainLayered.getNoiseWithSeed(pc +
                                    terrain.getNoiseWithSeed(pc, ps, qs,seedB - seedA) * 0.5,
                            ps, qs, seedA) + landModifier - 1.0);
//                    heightData[x][y] = (h = terrain4D.getNoiseWithSeed(pc, ps, qs,
//                            (terrainLayered.getNoiseWithSeed(pc, ps, qs, seedB - seedA)
//                                    + terrain.getNoiseWithSeed(pc, ps, qs, seedC - seedB)) * 0.5,
//                            seedA) * landModifier);
                    heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps
                                    + otherRidged.getNoiseWithSeed(pc, ps, qs,seedB + seedC)
                            , qs, seedB));
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qs
                                    + otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA)
                            , seedC));
                    minHeightActual = Math.min(minHeightActual, h);
                    maxHeightActual = Math.max(maxHeightActual, h);
                    if(fresh) {
                        minHeight = Math.min(minHeight, h);
                        maxHeight = Math.max(maxHeight, h);

                        minHeat0 = Math.min(minHeat0, p);
                        maxHeat0 = Math.max(maxHeat0, p);

                        minWet0 = Math.min(minWet0, temp);
                        maxWet0 = Math.max(maxWet0, temp);
                    }
                }
                minHeightActual = Math.min(minHeightActual, minHeight);
                maxHeightActual = Math.max(maxHeightActual, maxHeight);

            }
            double  heatDiff = 0.8 / (maxHeat0 - minHeat0),
                    wetDiff = 1.0 / (maxWet0 - minWet0),
                    hMod,
                    halfHeight = (height - 1) * 0.5, i_half = 1.0 / halfHeight;
            yPos = startY + i_uh;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                temp = Math.abs(yPos - halfHeight) * i_half;
                temp *= (2.4 - temp);
                temp = 2.2 - temp;
                for (int x = 0; x < width; x++) {
                    h = heightData[x][y];
                    if(heightCodeData[x][y] == 10000) {
                        heightCodeData[x][y] = 1000;
                        continue;
                    }
                    else
                        heightCodeData[x][y] = (t = codeHeight(h));
                    hMod = 1.0;
                    switch (t) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            h = 0.4;
                            hMod = 0.2;
                            break;
                        case 6:
                            h = -0.1 * (h - forestLower - 0.08);
                            break;
                        case 7:
                            h *= -0.25;
                            break;
                        case 8:
                            h *= -0.4;
                            break;
                        default:
                            h *= 0.05;
                    }
                    heatData[x][y] = (h = (((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6) * temp);
                    if (fresh) {
                        ps = Math.min(ps, h); //minHeat0
                        pc = Math.max(pc, h); //maxHeat0
                    }
                }
            }
            if(fresh)
            {
                minHeat1 = ps;
                maxHeat1 = pc;
            }
            heatDiff = coolingModifier / (maxHeat1 - minHeat1);
            qs = Double.POSITIVE_INFINITY;
            qc = Double.NEGATIVE_INFINITY;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;


            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    heatData[x][y] = (h = ((heatData[x][y] - minHeat1) * heatDiff));
                    moistureData[x][y] = (temp = (moistureData[x][y] - minWet0) * wetDiff);
                    if (fresh) {
                        qs = Math.min(qs, h);
                        qc = Math.max(qc, h);
                        ps = Math.min(ps, temp);
                        pc = Math.max(pc, temp);
                    }
                }
            }
            if(fresh)
            {
                minHeat = qs;
                maxHeat = qc;
                minWet = ps;
                maxWet = pc;
            }
            landData.refill(heightCodeData, 4, 999);
        }
    }
    /**
     * A concrete implementation of {@link WorldMapGenerator} that projects the world map onto a shape with a flat top
     * and bottom but near-circular sides. This is an equal-area projection, like EllipticalMap, so effects that fill
     * areas on a map like {@link PoliticalMapper} will fill (almost) equally on any part of the map. This has less
     * distortion on the far left and far right edges of the map than EllipticalMap, but the flat top and bottom are
     * probably very distorted in a small area near the poles.
     * This uses the <a href="https://en.wikipedia.org/wiki/Eckert_IV_projection">Eckert IV projection</a>.
     * <a href="https://squidpony.github.io/SquidLib/RoundSideWorldMap.png">Example map</a>
     */
    @Beta
    public static class RoundSideMap extends WorldMapGenerator {
        //        protected static final double terrainFreq = 1.35, terrainRidgedFreq = 1.8, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375, riverRidgedFreq = 21.7;
        protected static final double terrainFreq = 1.45, terrainRidgedFreq = 3.1, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375;
        protected double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final Noise3D terrain, heat, moisture, otherRidged, terrainLayered;
        public final double[][] xPositions,
                yPositions,
                zPositions;
        protected final int[] edges;


        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Always makes a 200x100 map.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link RoundSideMap#RoundSideMap(long, int, int, Noise3D, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 200, 100, FastNoise.instance, 1.0}.
         */
        public RoundSideMap() {
            this(0x1337BABE1337D00DL, 200, 100, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public RoundSideMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight,  FastNoise.instance,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public RoundSideMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public RoundSideMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail. The suggested Noise3D
         * implementation to use is {@link FastNoise#instance}
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         */
        public RoundSideMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed, the width/height of the map, and parameters for noise generation (a
         * {@link Noise3D} implementation, where {@link FastNoise#instance} is suggested, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
         * cannot be changed after the fact, but you can zoom in. FastNoise will be the fastest 3D generator to use for
         * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
         * seed several times at different scales of noise (it's fine to use the static {@link FastNoise#instance}
         * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
         * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
         * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
         * that don't require zooming.
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public RoundSideMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[width][height];
            yPositions = new double[width][height];
            zPositions = new double[width][height];
            edges = new int[height << 1];
            terrain = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 10), terrainFreq);
            terrainLayered = new Noise.InverseLayered3D(noiseGenerator, (int) (1 + octaveMultiplier * 6), terrainRidgedFreq * 0.325);
//            terrain = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 8), terrainFreq);
//            terrainLayered = new Noise.Layered3D(noiseGenerator, (int) (1 + octaveMultiplier * 6), terrainRidgedFreq * 5.25);
            heat = new Noise.InverseLayered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 3), heatFreq, 0.75);
            moisture = new Noise.InverseLayered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 4), moistureFreq, 0.55);
            otherRidged = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 6), otherFreq);
        }

        @Override
        public int wrapX(final int x, int y) {
            y = Math.max(0, Math.min(y, height - 1));
            if(x < edges[y << 1])
                return edges[y << 1 | 1];
            else if(x > edges[y << 1 | 1])
                return edges[y << 1];
            else return x;
        }

        @Override
        public int wrapY(final int x, final int y)  {
            return Math.max(0, Math.min(y, height - 1));
        }

        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double coolMod, int stateA, int stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || coolMod != coolingModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
                minHeightActual = Double.POSITIVE_INFINITY;
                maxHeightActual = Double.NEGATIVE_INFINITY;
                minHeat0 = Double.POSITIVE_INFINITY;
                maxHeat0 = Double.NEGATIVE_INFINITY;
                minHeat1 = Double.POSITIVE_INFINITY;
                maxHeat1 = Double.NEGATIVE_INFINITY;
                minHeat = Double.POSITIVE_INFINITY;
                maxHeat = Double.NEGATIVE_INFINITY;
                minWet0 = Double.POSITIVE_INFINITY;
                maxWet0 = Double.NEGATIVE_INFINITY;
                minWet = Double.POSITIVE_INFINITY;
                maxWet = Double.NEGATIVE_INFINITY;
                cacheA = stateA;
                cacheB = stateB;
                fresh = true;
            }
            rng.setState(stateA, stateB);
            long seedA = rng.nextLong(), seedB = rng.nextLong(), seedC = rng.nextLong();
            int t;

            landModifier = (landMod <= 0) ? rng.nextDouble(0.2) + 0.91 : landMod;
            coolingModifier = (coolMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : coolMod;

            double p,
                    ps, pc,
                    qs, qc,
                    h, temp, yPos, xPos,
                    i_uw = usedWidth / (double)width,
                    i_uh = usedHeight / (double)height,
                    th, thb, thx, thy, lon, lat,
                    rx = width * 0.25, irx = 1.326500428177002 / rx, hw = width * 0.5,
                    ry = height * 0.5, iry = 1.0 / ry;

            yPos = startY - ry;
            for (int y = 0; y < height; y++, yPos += i_uh) {
                thy = yPos * iry;//NumberTools.sin(thb);
                thb = asin(thy);
                thx = NumberTools.cos(thb);
                //1.3265004 0.7538633073600218  1.326500428177002
                lon = (thx == Math.PI * 0.5 || thx == Math.PI * -0.5) ? 0x1.0p100 : irx / (0.42223820031577125 * (1.0 + thx));
                qs = (thb + (thx + 2.0) * thy) * 0.2800495767557787;
                lat = asin(qs);

                qc = NumberTools.cos(lat);

                boolean inSpace = true;
                xPos = startX - hw;
                for (int x = 0/*, xt = 0*/; x < width; x++, xPos += i_uw) {
                    th = lon * xPos;
                    if(th < -3.141592653589793 || th > 3.141592653589793) {
                        heightCodeData[x][y] = 10000;
                        inSpace = true;
                        continue;
                    }
                    if(inSpace)
                    {
                        inSpace = false;
                        edges[y << 1] = x;
                    }
                    edges[y << 1 | 1] = x;
                    th += centerLongitude;
                    ps = NumberTools.sin(th) * qc;
                    pc = NumberTools.cos(th) * qc;
                    xPositions[x][y] = pc;
                    yPositions[x][y] = ps;
                    zPositions[x][y] = qs;
                    heightData[x][y] = (h = terrainLayered.getNoiseWithSeed(pc +
                                    terrain.getNoiseWithSeed(pc, ps, qs,seedB - seedA) * 0.5,
                            ps, qs, seedA) + landModifier - 1.0);
                    heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps
                                    + otherRidged.getNoiseWithSeed(pc, ps, qs,seedB + seedC)
                            , qs, seedB));
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qs
                                    + otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA)
                            , seedC));
                    minHeightActual = Math.min(minHeightActual, h);
                    maxHeightActual = Math.max(maxHeightActual, h);
                    if(fresh) {
                        minHeight = Math.min(minHeight, h);
                        maxHeight = Math.max(maxHeight, h);

                        minHeat0 = Math.min(minHeat0, p);
                        maxHeat0 = Math.max(maxHeat0, p);

                        minWet0 = Math.min(minWet0, temp);
                        maxWet0 = Math.max(maxWet0, temp);
                    }
                }
                minHeightActual = Math.min(minHeightActual, minHeight);
                maxHeightActual = Math.max(maxHeightActual, maxHeight);

            }
            double  heatDiff = 0.8 / (maxHeat0 - minHeat0),
                    wetDiff = 1.0 / (maxWet0 - minWet0),
                    hMod,
                    halfHeight = (height - 1) * 0.5, i_half = 1.0 / halfHeight;
            yPos = startY + i_uh;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                temp = Math.abs(yPos - halfHeight) * i_half;
                temp *= (2.4 - temp);
                temp = 2.2 - temp;
                for (int x = 0; x < width; x++) {
                    h = heightData[x][y];
                    if(heightCodeData[x][y] == 10000) {
                        heightCodeData[x][y] = 1000;
                        continue;
                    }
                    else
                        heightCodeData[x][y] = (t = codeHeight(h));
                    hMod = 1.0;
                    switch (t) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            h = 0.4;
                            hMod = 0.2;
                            break;
                        case 6:
                            h = -0.1 * (h - forestLower - 0.08);
                            break;
                        case 7:
                            h *= -0.25;
                            break;
                        case 8:
                            h *= -0.4;
                            break;
                        default:
                            h *= 0.05;
                    }
                    heatData[x][y] = (h = (((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6) * temp);
                    if (fresh) {
                        ps = Math.min(ps, h); //minHeat0
                        pc = Math.max(pc, h); //maxHeat0
                    }
                }
            }
            if(fresh)
            {
                minHeat1 = ps;
                maxHeat1 = pc;
            }
            heatDiff = coolingModifier / (maxHeat1 - minHeat1);
            qs = Double.POSITIVE_INFINITY;
            qc = Double.NEGATIVE_INFINITY;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;


            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    heatData[x][y] = (h = ((heatData[x][y] - minHeat1) * heatDiff));
                    moistureData[x][y] = (temp = (moistureData[x][y] - minWet0) * wetDiff);
                    if (fresh) {
                        qs = Math.min(qs, h);
                        qc = Math.max(qc, h);
                        ps = Math.min(ps, temp);
                        pc = Math.max(pc, temp);
                    }
                }
            }
            if(fresh)
            {
                minHeat = qs;
                maxHeat = qc;
                minWet = ps;
                maxWet = pc;
            }
            landData.refill(heightCodeData, 4, 999);
        }
    }
    /**
     * A concrete implementation of {@link WorldMapGenerator} that projects the world map onto a shape that resembles a
     * mix part-way between an ellipse and a rectangle. This is an equal-area projection, like EllipticalMap, so effects that fill
     * areas on a map like {@link PoliticalMapper} will fill (almost) equally on any part of the map. This has less
     * distortion around all the edges than the other maps here, especially when comparing the North and South poles
     * with RoundSideMap.
     * This uses the <a href="https://en.wikipedia.org/wiki/Tobler_hyperelliptical_projection">Tobler hyperelliptical projection</a>.
     * <a href="">Example map</a>
     */
    @Beta
    public static class HyperellipticalMap extends WorldMapGenerator {
        protected static final double terrainFreq = 1.45, terrainRidgedFreq = 3.1, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375;
        protected double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final Noise3D terrain, heat, moisture, otherRidged, terrainLayered;
        public final double[][] xPositions,
                yPositions,
                zPositions;
        protected final int[] edges;
        private final double alpha, kappa, epsilon;
        private final double[] Z;


        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Always makes a 200x100 map.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link HyperellipticalMap#HyperellipticalMap(long, int, int, Noise3D, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 200, 100, FastNoise.instance, 1.0}.
         */
        public HyperellipticalMap() {
            this(0x1337BABE1337D00DL, 200, 100, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public HyperellipticalMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight,  FastNoise.instance,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public HyperellipticalMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public HyperellipticalMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail. The suggested Noise3D
         * implementation to use is {@link FastNoise#instance}.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         */
        public HyperellipticalMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed, the width/height of the map, and parameters for noise generation (a
         * {@link Noise3D} implementation, where {@link FastNoise#instance} is suggested, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
         * cannot be changed after the fact, but you can zoom in. FastNoise will be the fastest 3D generator to use for
         * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
         * seed several times at different scales of noise (it's fine to use the static {@link FastNoise#instance}
         * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
         * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
         * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
         * that don't require zooming.
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public HyperellipticalMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator, double octaveMultiplier){
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, octaveMultiplier, 0.0625, 2.5);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed, the width/height of the map, and parameters for noise generation (a
         * {@link Noise3D} implementation, where {@link FastNoise#instance} is suggested, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
         * cannot be changed after the fact, but you can zoom in. FastNoise will be the fastest 3D generator to use for
         * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
         * seed several times at different scales of noise (it's fine to use the static {@link FastNoise#instance}
         * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
         * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
         * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
         * that don't require zooming.
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         * @param alpha one of the Tobler parameters;  0.0625 is the default and this can range from 0.0 to 1.0 at least
         * @param kappa one of the Tobler parameters; 2.5 is the default but 2.0-5.0 range values are also often used
         */
        public HyperellipticalMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator,
                                  double octaveMultiplier, double alpha, double kappa){
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[width][height];
            yPositions = new double[width][height];
            zPositions = new double[width][height];
            edges = new int[height << 1];
            terrain = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 10), terrainFreq);
            terrainLayered = new Noise.InverseLayered3D(noiseGenerator, (int) (1 + octaveMultiplier * 6), terrainRidgedFreq * 0.325);
            heat = new Noise.InverseLayered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 3), heatFreq, 0.75);
            moisture = new Noise.InverseLayered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 4), moistureFreq, 0.55);
            otherRidged = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 6), otherFreq);
            this.alpha = alpha;
            this.kappa = kappa;
            this.Z = new double[height << 2];
            this.epsilon = ProjectionTools.simpsonIntegrateHyperellipse(0.0, 1.0, 0.25 / height, kappa);
            ProjectionTools.simpsonODESolveHyperellipse(1, this.Z, 0.25 / height, alpha, kappa, epsilon);
        }

        @Override
        public int wrapX(final int x, int y) {
            y = Math.max(0, Math.min(y, height - 1));
            if(x < edges[y << 1])
                return edges[y << 1 | 1];
            else if(x > edges[y << 1 | 1])
                return edges[y << 1];
            else return x;
        }

        @Override
        public int wrapY(final int x, final int y)  {
            return Math.max(0, Math.min(y, height - 1));
        }

        /**
         * Given a latitude and longitude in radians (the conventional way of describing points on a globe), this gets the
         * (x,y) Coord on the map projection this generator uses that corresponds to the given lat-lon coordinates. If this
         * generator does not represent a globe (if it is toroidal, for instance) or if there is no "good way" to calculate
         * the projection for a given lat-lon coordinate, this returns null. This implementation never returns null.
         * If this is a supported operation and the parameters are valid, this returns a Coord with x between 0 and
         * {@link #width}, and y between 0 and {@link #height}, both exclusive. Automatically wraps the Coord's values using
         * {@link #wrapX(int, int)} and {@link #wrapY(int, int)}.
         *
         * @param latitude  the latitude, from {@code Math.PI * -0.5} to {@code Math.PI * 0.5}
         * @param longitude the longitude, from {@code 0.0} to {@code Math.PI * 2.0}
         * @return the point at the given latitude and longitude, as a Coord with x between 0 and {@link #width} and y between 0 and {@link #height}, or null if unsupported
         */
        @Override
        public Coord project(double latitude, double longitude) {
            final double z0 = Math.abs(NumberTools.sin(latitude));
            final int i = Arrays.binarySearch(Z, z0);
            final double y;
            if (i >= 0)
                y = i/(Z.length-1.);
            else if (-i-1 >= Z.length)
                y = Z[Z.length-1];
            else
                y = ((z0-Z[-i-2])/(Z[-i-1]-Z[-i-2]) + (-i-2))/(Z.length-1.);
            final int xx = (int)(((longitude - getCenterLongitude() + 12.566370614359172) % 6.283185307179586) * Math.abs(alpha + (1-alpha)*Math.pow(1 - Math.pow(Math.abs(y),kappa), 1/kappa)) + 0.5);
            final int yy = (int)(y * Math.signum(latitude) * height * 0.5 + 0.5);
            return Coord.get(wrapX(xx, yy), wrapY(xx, yy));
        }

        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double coolMod, int stateA, int stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || coolMod != coolingModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
                minHeightActual = Double.POSITIVE_INFINITY;
                maxHeightActual = Double.NEGATIVE_INFINITY;
                minHeat0 = Double.POSITIVE_INFINITY;
                maxHeat0 = Double.NEGATIVE_INFINITY;
                minHeat1 = Double.POSITIVE_INFINITY;
                maxHeat1 = Double.NEGATIVE_INFINITY;
                minHeat = Double.POSITIVE_INFINITY;
                maxHeat = Double.NEGATIVE_INFINITY;
                minWet0 = Double.POSITIVE_INFINITY;
                maxWet0 = Double.NEGATIVE_INFINITY;
                minWet = Double.POSITIVE_INFINITY;
                maxWet = Double.NEGATIVE_INFINITY;
                cacheA = stateA;
                cacheB = stateB;
                fresh = true;
            }
            rng.setState(stateA, stateB);
            long seedA = rng.nextLong(), seedB = rng.nextLong(), seedC = rng.nextLong();
            int t;

            landModifier = (landMod <= 0) ? rng.nextDouble(0.2) + 0.91 : landMod;
            coolingModifier = (coolMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : coolMod;

            double p,
                    ps, pc,
                    qs, qc,
                    h, temp, yPos, xPos,
                    i_uw = usedWidth / (double)width,
                    i_uh = usedHeight / (double)height,
                    th, lon,
                    rx = width * 0.5, irx = Math.PI / rx, hw = width * 0.5,
                    ry = height * 0.5, iry = 1.0 / ry;

            yPos = startY - ry;
            for (int y = 0; y < height; y++, yPos += i_uh) {
//                thy = yPos * iry;//NumberTools.sin(thb);
//                thb = asin(thy);
//                thx = NumberTools.cos(thb);
//                //1.3265004 0.7538633073600218  1.326500428177002
//                lon = (thx == Math.PI * 0.5 || thx == Math.PI * -0.5) ? 0x1.0p100 : irx / (0.42223820031577125 * (1.0 + thx));
//                qs = (thb + (thx + 2.0) * thy) * 0.2800495767557787;
//                lat = asin(qs);
//
//                qc = NumberTools.cos(lat);

                lon = asin(Z[(int)(0.5 + Math.abs(yPos*iry)*(Z.length-1))])*Math.signum(yPos);
                qs = NumberTools.sin(lon);
                qc = NumberTools.cos(lon);

                boolean inSpace = true;
                xPos = startX - hw;
                for (int x = 0/*, xt = 0*/; x < width; x++, xPos += i_uw) {
                    //th = lon * xPos;
                    th = xPos * irx / Math.abs(alpha + (1-alpha)*ProjectionTools.hyperellipse(yPos * iry, kappa));
                    if(th < -3.141592653589793 || th > 3.141592653589793) {
                        //if(th < -2.0 || th > 2.0) {
                        heightCodeData[x][y] = 10000;
                        inSpace = true;
                        continue;
                    }
                    if(inSpace)
                    {
                        inSpace = false;
                        edges[y << 1] = x;
                    }
                    edges[y << 1 | 1] = x;
                    th += centerLongitude;
                    ps = NumberTools.sin(th) * qc;
                    pc = NumberTools.cos(th) * qc;
                    xPositions[x][y] = pc;
                    yPositions[x][y] = ps;
                    zPositions[x][y] = qs;
                    heightData[x][y] = (h = terrainLayered.getNoiseWithSeed(pc +
                                    terrain.getNoiseWithSeed(pc, ps, qs, seedB - seedA) * 0.5,
                            ps, qs, seedA) + landModifier - 1.0);
                    heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps
                                    + otherRidged.getNoiseWithSeed(pc, ps, qs, seedB + seedC)
                            , qs, seedB));
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qs
                                    + otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA)
                            , seedC));
                    minHeightActual = Math.min(minHeightActual, h);
                    maxHeightActual = Math.max(maxHeightActual, h);
                    if(fresh) {
                        minHeight = Math.min(minHeight, h);
                        maxHeight = Math.max(maxHeight, h);

                        minHeat0 = Math.min(minHeat0, p);
                        maxHeat0 = Math.max(maxHeat0, p);

                        minWet0 = Math.min(minWet0, temp);
                        maxWet0 = Math.max(maxWet0, temp);
                    }
                }
                minHeightActual = Math.min(minHeightActual, minHeight);
                maxHeightActual = Math.max(maxHeightActual, maxHeight);

            }
            double  heatDiff = 0.8 / (maxHeat0 - minHeat0),
                    wetDiff = 1.0 / (maxWet0 - minWet0),
                    hMod,
                    halfHeight = (height - 1) * 0.5, i_half = 1.0 / halfHeight;
            yPos = startY + i_uh;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                temp = Math.abs(yPos - halfHeight) * i_half;
                temp *= (2.4 - temp);
                temp = 2.2 - temp;
                for (int x = 0; x < width; x++) {
                    h = heightData[x][y];
                    if(heightCodeData[x][y] == 10000) {
                        heightCodeData[x][y] = 1000;
                        continue;
                    }
                    else
                        heightCodeData[x][y] = (t = codeHeight(h));
                    hMod = 1.0;
                    switch (t) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            h = 0.4;
                            hMod = 0.2;
                            break;
                        case 6:
                            h = -0.1 * (h - forestLower - 0.08);
                            break;
                        case 7:
                            h *= -0.25;
                            break;
                        case 8:
                            h *= -0.4;
                            break;
                        default:
                            h *= 0.05;
                    }
                    heatData[x][y] = (h = (((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6) * temp);
                    if (fresh) {
                        ps = Math.min(ps, h); //minHeat0
                        pc = Math.max(pc, h); //maxHeat0
                    }
                }
            }
            if(fresh)
            {
                minHeat1 = ps;
                maxHeat1 = pc;
            }
            heatDiff = coolingModifier / (maxHeat1 - minHeat1);
            qs = Double.POSITIVE_INFINITY;
            qc = Double.NEGATIVE_INFINITY;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;


            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    heatData[x][y] = (h = ((heatData[x][y] - minHeat1) * heatDiff));
                    moistureData[x][y] = (temp = (moistureData[x][y] - minWet0) * wetDiff);
                    if (fresh) {
                        qs = Math.min(qs, h);
                        qc = Math.max(qc, h);
                        ps = Math.min(ps, temp);
                        pc = Math.max(pc, temp);
                    }
                }
            }
            if(fresh)
            {
                minHeat = qs;
                maxHeat = qc;
                minWet = ps;
                maxWet = pc;
            }
            landData.refill(heightCodeData, 4, 999);
        }
    }

    /**
     * A concrete implementation of {@link WorldMapGenerator} that projects the world map onto an ellipse that should be
     * twice as wide as it is tall (although you can stretch it by width and height that don't have that ratio).
     * This uses the <a href="https://en.wikipedia.org/wiki/Hammer_projection">Hammer projection</a>, so the latitude
     * lines are curved instead of flat. The Mollweide projection that {@link WorldMapGenerator.EllipticalMap} uses has flat lines, but
     * the two projection are otherwise very similar, and are both equal-area (Hammer tends to have less significant
     * distortion around the edges, but the curvature of the latitude lines can be hard to visualize).
     * <a href="https://i.imgur.com/nmN6lMK.gifv">Preview image link of a world rotating</a>.
     */
    @Beta
    public static class EllipticalHammerMap extends WorldMapGenerator {
        //        protected static final double terrainFreq = 1.35, terrainRidgedFreq = 1.8, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375, riverRidgedFreq = 21.7;
        protected static final double terrainFreq = 1.45, terrainRidgedFreq = 3.1, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375;
        protected double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final Noise3D terrain, heat, moisture, otherRidged, terrainLayered;
        public final double[][] xPositions,
                yPositions,
                zPositions;
        protected final int[] edges;


        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape. This is very
         * similar to {@link EllipticalMap}, but has curved latitude lines instead of flat ones (it also may see more
         * internal usage because some operations on this projection are much faster and simpler).
         * Always makes a 200x100 map.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link EllipticalHammerMap#EllipticalHammerMap(long, int, int, Noise3D, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 200, 100, FastNoise.instance, 1.0}.
         */
        public EllipticalHammerMap() {
            this(0x1337BABE1337D00DL, 200, 100, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape. This is very
         * similar to {@link EllipticalMap}, but has curved latitude lines instead of flat ones (it also may see more
         * internal usage because some operations on this projection are much faster and simpler).
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public EllipticalHammerMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight,  FastNoise.instance,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape. This is very
         * similar to {@link EllipticalMap}, but has curved latitude lines instead of flat ones (it also may see more
         * internal usage because some operations on this projection are much faster and simpler).
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public EllipticalHammerMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape. This is very
         * similar to {@link EllipticalMap}, but has curved latitude lines instead of flat ones (it also may see more
         * internal usage because some operations on this projection are much faster and simpler).
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public EllipticalHammerMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape. This is very
         * similar to {@link EllipticalMap}, but has curved latitude lines instead of flat ones (it also may see more
         * internal usage because some operations on this projection are much faster and simpler).
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail. The suggested Noise3D
         * implementation to use is {@link FastNoise#instance}.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         */
        public EllipticalHammerMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape. This is very
         * similar to {@link EllipticalMap}, but has curved latitude lines instead of flat ones (it also may see more
         * internal usage because some operations on this projection are much faster and simpler).
         * Takes an initial seed, the width/height of the map, and parameters for noise generation (a
         * {@link Noise3D} implementation, where {@link FastNoise#instance} is suggested, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
         * cannot be changed after the fact, but you can zoom in. FastNoise will be the fastest 3D generator to use for
         * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
         * seed several times at different scales of noise (it's fine to use the static {@link FastNoise#instance}
         * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
         * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
         * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
         * that don't require zooming.
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public EllipticalHammerMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[width][height];
            yPositions = new double[width][height];
            zPositions = new double[width][height];
            edges = new int[height << 1];
            terrain = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 10), terrainFreq);
            terrainLayered = new Noise.InverseLayered3D(noiseGenerator, (int) (1 + octaveMultiplier * 6), terrainRidgedFreq * 0.325);
//            terrain = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 8), terrainFreq);
//            terrainLayered = new Noise.Layered3D(noiseGenerator, (int) (1 + octaveMultiplier * 6), terrainRidgedFreq * 5.25);
            heat = new Noise.InverseLayered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 3), heatFreq, 0.75);
            moisture = new Noise.InverseLayered3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 4), moistureFreq, 0.55);
            otherRidged = new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 6), otherFreq);
        }

        @Override
        public int wrapX(final int x, int y) {
            y = Math.max(0, Math.min(y, height - 1));
            if(x < edges[y << 1])
                return edges[y << 1 | 1];
            else if(x > edges[y << 1 | 1])
                return edges[y << 1];
            else return x;
        }

        @Override
        public int wrapY(final int x, final int y)  {
            return Math.max(0, Math.min(y, height - 1));
        }

        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double coolMod, int stateA, int stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || coolMod != coolingModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
                minHeightActual = Double.POSITIVE_INFINITY;
                maxHeightActual = Double.NEGATIVE_INFINITY;
                minHeat0 = Double.POSITIVE_INFINITY;
                maxHeat0 = Double.NEGATIVE_INFINITY;
                minHeat1 = Double.POSITIVE_INFINITY;
                maxHeat1 = Double.NEGATIVE_INFINITY;
                minHeat = Double.POSITIVE_INFINITY;
                maxHeat = Double.NEGATIVE_INFINITY;
                minWet0 = Double.POSITIVE_INFINITY;
                maxWet0 = Double.NEGATIVE_INFINITY;
                minWet = Double.POSITIVE_INFINITY;
                maxWet = Double.NEGATIVE_INFINITY;
                cacheA = stateA;
                cacheB = stateB;
                fresh = true;
            }
            rng.setState(stateA, stateB);
            long seedA = rng.nextLong(), seedB = rng.nextLong(), seedC = rng.nextLong();
            int t;

            landModifier = (landMod <= 0) ? rng.nextDouble(0.2) + 0.91 : landMod;
            coolingModifier = (coolMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : coolMod;

            double p,
                    ps, pc,
                    qs, qc,
                    h, temp, yPos, xPos,
                    z, th, lon, lat,
                    rx = width * 0.5, hw = width * 0.5, root2 = Math.sqrt(2.0),
                    irx = 1.0 / rx, iry = 2.0 / (double) height,
                    xAdj, yAdj,
                    i_uw = usedWidth / (double)(width),
                    i_uh = usedHeight / (double)(height);

            yPos = (startY - height * 0.5);
            for (int y = 0; y < height; y++, yPos += i_uh) {
                boolean inSpace = true;
                yAdj = yPos * iry;
                xPos = (startX - hw);
                for (int x = 0; x < width; x++, xPos += i_uw) {
                    xAdj = xPos * irx;
                    z = Math.sqrt(1.0 - 0.5 * xAdj * xAdj - 0.5 * yAdj * yAdj);
                    th = z * yAdj * root2;
                    lon = 2.0 * NumberTools.atan2((2.0 * z * z - 1.0), (z * xAdj * root2));
                    if(th != th || lon < 0.0) {
                        heightCodeData[x][y] = 10000;
                        inSpace = true;
                        continue;
                    }
                    lat = asin(th);
                    qc = NumberTools.cos(lat);
                    qs = th;
                    th = Math.PI - lon + centerLongitude;
                    if(inSpace)
                    {
                        inSpace = false;
                        edges[y << 1] = x;
                    }
                    edges[y << 1 | 1] = x;
                    ps = NumberTools.sin(th) * qc;
                    pc = NumberTools.cos(th) * qc;
                    xPositions[x][y] = pc;
                    yPositions[x][y] = ps;
                    zPositions[x][y] = qs;
                    heightData[x][y] = (h = terrainLayered.getNoiseWithSeed(pc +
                                    terrain.getNoiseWithSeed(pc, ps, qs,seedB - seedA) * 0.5,
                            ps, qs, seedA) + landModifier - 1.0);
                    heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps
                                    + otherRidged.getNoiseWithSeed(pc, ps, qs,seedB + seedC)
                            , qs, seedB));
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qs
                                    + otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA)
                            , seedC));
                    minHeightActual = Math.min(minHeightActual, h);
                    maxHeightActual = Math.max(maxHeightActual, h);
                    if(fresh) {
                        minHeight = Math.min(minHeight, h);
                        maxHeight = Math.max(maxHeight, h);

                        minHeat0 = Math.min(minHeat0, p);
                        maxHeat0 = Math.max(maxHeat0, p);

                        minWet0 = Math.min(minWet0, temp);
                        maxWet0 = Math.max(maxWet0, temp);
                    }
                }
                minHeightActual = Math.min(minHeightActual, minHeight);
                maxHeightActual = Math.max(maxHeightActual, maxHeight);

            }
            double  heatDiff = 0.8 / (maxHeat0 - minHeat0),
                    wetDiff = 1.0 / (maxWet0 - minWet0),
                    hMod,
                    halfHeight = (height - 1) * 0.5, i_half = 1.0 / halfHeight;
            yPos = startY + i_uh;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                temp = Math.abs(yPos - halfHeight) * i_half;
                temp *= (2.4 - temp);
                temp = 2.2 - temp;
                for (int x = 0; x < width; x++) {
                    h = heightData[x][y];
                    if(heightCodeData[x][y] == 10000) {
                        heightCodeData[x][y] = 1000;
                        continue;
                    }
                    else
                        heightCodeData[x][y] = (t = codeHeight(h));
                    hMod = 1.0;
                    switch (t) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            h = 0.4;
                            hMod = 0.2;
                            break;
                        case 6:
                            h = -0.1 * (h - forestLower - 0.08);
                            break;
                        case 7:
                            h *= -0.25;
                            break;
                        case 8:
                            h *= -0.4;
                            break;
                        default:
                            h *= 0.05;
                    }
                    heatData[x][y] = (h = (((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6) * temp);
                    if (fresh) {
                        ps = Math.min(ps, h); //minHeat0
                        pc = Math.max(pc, h); //maxHeat0
                    }
                }
            }
            if(fresh)
            {
                minHeat1 = ps;
                maxHeat1 = pc;
            }
            heatDiff = coolingModifier / (maxHeat1 - minHeat1);
            qs = Double.POSITIVE_INFINITY;
            qc = Double.NEGATIVE_INFINITY;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;


            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    heatData[x][y] = (h = ((heatData[x][y] - minHeat1) * heatDiff));
                    moistureData[x][y] = (temp = (moistureData[x][y] - minWet0) * wetDiff);
                    if (fresh) {
                        qs = Math.min(qs, h);
                        qc = Math.max(qc, h);
                        ps = Math.min(ps, temp);
                        pc = Math.max(pc, temp);
                    }
                }
            }
            if(fresh)
            {
                minHeat = qs;
                maxHeat = qc;
                minWet = ps;
                maxWet = pc;
            }
            landData.refill(heightCodeData, 4, 999);
        }
    }



    /**
     * A concrete implementation of {@link WorldMapGenerator} that imitates an infinite-distance perspective view of a
     * world, showing only one hemisphere, that should be as wide as it is tall (its outline is a circle). It should
     * look as a world would when viewed from space, and implements rotation differently to allow the planet to be
     * rotated without recalculating all the data, though it cannot zoom. Note that calling
     * {@link #setCenterLongitude(double)} does a lot more work than in other classes, but less than fully calling
     * {@link #generate()} in those classes, since it doesn't remake the map data at a slightly different rotation and
     * instead keeps a single map in use the whole time, using sections of it. This uses an
     * <a href="https://en.wikipedia.org/wiki/Orthographic_projection_in_cartography">Orthographic projection</a> with
     * the latitude always at the equator; the internal map is stored as a {@link WorldMapGenerator.SphereMap}, which uses a
     * <a href="https://en.wikipedia.org/wiki/Cylindrical_equal-area_projection#Discussion">cylindrical equal-area
     * projection</a>, specifically the Smyth equal-surface projection.
     * <br>
     * <a href="https://i.imgur.com/WNa5nQ1.gifv">Example view of a planet rotating</a>.
     * <a href="https://i.imgur.com/NV5IMd6.gifv">Another example</a>.
     */
    @Beta
    public static class RotatingSpaceMap extends WorldMapGenerator {
        protected double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final double[][] xPositions,
                yPositions,
                zPositions;
        protected final int[] edges;
        public final SphereMap storedMap;
        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Always makes a 100x100 map.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link RotatingSpaceMap#RotatingSpaceMap(long, int, int, Noise3D, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 100, 100, FastNoise.instance, 1.0}.
         */
        public RotatingSpaceMap() {
            this(0x1337BABE1337D00DL, 100, 100, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public RotatingSpaceMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight,  FastNoise.instance,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public RotatingSpaceMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public RotatingSpaceMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         */
        public RotatingSpaceMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed, the width/height of the map, and parameters for noise
         * generation (a {@link Noise3D} implementation, which is usually {@link FastNoise#instance}, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
         * cannot be changed after the fact, but you can zoom in. FastNoise will be the fastest 3D generator to use for
         * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
         * seed several times at different scales of noise (it's fine to use the static {@link FastNoise#instance}
         * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
         * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
         * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
         * that don't require zooming.
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public RotatingSpaceMap(long initialSeed, int mapWidth, int mapHeight, Noise3D noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[mapWidth][mapHeight];
            yPositions = new double[mapWidth][mapHeight];
            zPositions = new double[mapWidth][mapHeight];
            edges = new int[height << 1];
            storedMap = new SphereMap(initialSeed, mapWidth << 1, mapHeight, noiseGenerator, octaveMultiplier);
        }

        @Override
        public int wrapX(int x, int y) {
            y = Math.max(0, Math.min(y, height - 1));
            return Math.max(edges[y << 1], Math.min(x, edges[y << 1 | 1]));
        }

        @Override
        public int wrapY(final int x, final int y)  {
            return Math.max(0, Math.min(y, height - 1));
        }

        @Override
        public void setCenterLongitude(double centerLongitude) {
            super.setCenterLongitude(centerLongitude);
            int ax, ay;
            double
                    ps, pc,
                    qs, qc,
                    h, yPos, xPos, iyPos, ixPos,
                    i_uw = usedWidth / (double)width,
                    i_uh = usedHeight / (double)height,
                    th, lon, lat, rho,
                    i_pi = 1.0 / Math.PI,
                    rx = width * 0.5, irx = i_uw / rx,
                    ry = height * 0.5, iry = i_uh / ry;

            yPos = startY - ry;
            iyPos = yPos / ry;
            for (int y = 0; y < height; y++, yPos += i_uh, iyPos += iry) {
                boolean inSpace = true;
                xPos = startX - rx;
                ixPos = xPos / rx;
                lat = asin(iyPos);
                for (int x = 0; x < width; x++, xPos += i_uw, ixPos += irx) {
                    rho = (ixPos * ixPos + iyPos * iyPos);
                    if(rho > 1.0) {
                        heightCodeData[x][y] = 1000;
                        inSpace = true;
                        continue;
                    }
                    rho = Math.sqrt(rho);
                    if(inSpace)
                    {
                        inSpace = false;
                        edges[y << 1] = x;
                    }
                    edges[y << 1 | 1] = x;
                    th = asin(rho); // c
                    lon = removeExcess((centerLongitude + (NumberTools.atan2(ixPos * rho, rho * NumberTools.cos(th)))) * 0.5);

                    qs = lat * 0.6366197723675814;
                    qc = qs + 1.0;
                    int sf = (qs >= 0.0 ? (int) qs : (int) qs - 1) & -2;
                    int cf = (qc >= 0.0 ? (int) qc : (int) qc - 1) & -2;
                    qs -= sf;
                    qc -= cf;
                    qs *= 2.0 - qs;
                    qc *= 2.0 - qc;
                    qs = qs * (-0.775 - 0.225 * qs) * ((sf & 2) - 1);
                    qc = qc * (-0.775 - 0.225 * qc) * ((cf & 2) - 1);


                    ps = lon * 0.6366197723675814;
                    pc = ps + 1.0;
                    sf = (ps >= 0.0 ? (int) ps : (int) ps - 1) & -2;
                    cf = (pc >= 0.0 ? (int) pc : (int) pc - 1) & -2;
                    ps -= sf;
                    pc -= cf;
                    ps *= 2.0 - ps;
                    pc *= 2.0 - pc;
                    ps = ps * (-0.775 - 0.225 * ps) * ((sf & 2) - 1);
                    pc = pc * (-0.775 - 0.225 * pc) * ((cf & 2) - 1);

                    ax = (int)((lon * i_pi + 1.0) * width);
                    ay = (int)((qs + 1.0) * ry);
                    
//                    // Hammer projection, not an inverse projection like we usually use
//                    z = 1.0 / Math.sqrt(1 + qc * NumberTools.cos(lon * 0.5));
//                    ax = (int)((qc * NumberTools.sin(lon * 0.5) * z + 1.0) * width);
//                    ay = (int)((qs * z + 1.0) * height * 0.5);

                    if(ax >= storedMap.width || ax < 0 || ay >= storedMap.height || ay < 0)
                    {
                        heightCodeData[x][y] = 1000;
                        continue;
                    }
                    if(storedMap.heightCodeData[ax][ay] >= 1000) // for the seam we get when looping around
                    {
                        ay = storedMap.wrapY(ax, ay);
                        ax = storedMap.wrapX(ax, ay);
                    }

                    xPositions[x][y] = pc * qc;
                    yPositions[x][y] = ps * qc;
                    zPositions[x][y] = qs;

                    heightData[x][y] = h = storedMap.heightData[ax][ay];
                    heightCodeData[x][y] = codeHeight(h);
                    heatData[x][y] = storedMap.heatData[ax][ay];
                    moistureData[x][y] = storedMap.moistureData[ax][ay];

                    minHeightActual = Math.min(minHeightActual, h);
                    maxHeightActual = Math.max(maxHeightActual, h);
                }
                minHeightActual = Math.min(minHeightActual, minHeight);
                maxHeightActual = Math.max(maxHeightActual, maxHeight);
            }

        }

        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double coolMod, int stateA, int stateB)
        {
            if(cacheA != stateA || cacheB != stateB)// || landMod != storedMap.landModifier || coolMod != storedMap.coolingModifier)
            {
                storedMap.regenerate(0, 0, width << 1, height, landMod, coolMod, stateA, stateB);
                minHeightActual = Double.POSITIVE_INFINITY;
                maxHeightActual = Double.NEGATIVE_INFINITY;

                minHeight = storedMap.minHeight;
                maxHeight = storedMap.maxHeight;

                minHeat0 = storedMap.minHeat0;
                maxHeat0 = storedMap.maxHeat0;

                minHeat1 = storedMap.minHeat1;
                maxHeat1 = storedMap.maxHeat1;

                minWet0 = storedMap.minWet0;
                maxWet0 = storedMap.maxWet0;

                minHeat = storedMap.minHeat;
                maxHeat = storedMap.maxHeat;

                minWet = storedMap.minWet;
                maxWet = storedMap.maxWet;

                cacheA = stateA;
                cacheB = stateB;
            }
            setCenterLongitude(centerLongitude);
            landData.refill(heightCodeData, 4, 999);
        }
    }
    /**
     * A concrete implementation of {@link WorldMapGenerator} that does no projection of the map, as if the area were
     * completely flat or small enough that curvature is impossible to see. This also does not change heat levels at the
     * far north and south regions of the map, since it is meant for areas that are all about the same heat level.
     */
    public static class LocalMap extends WorldMapGenerator {
        protected static final double terrainFreq = 1.45, terrainRidgedFreq = 3.1, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375;
        //protected static final double terrainFreq = 1.65, terrainRidgedFreq = 1.8, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375, riverRidgedFreq = 21.7;
        protected double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final Noise.Ridged2D terrain, otherRidged;
        public final Noise.InverseLayered2D heat, moisture, terrainLayered;
        public final double[][] xPositions,
                yPositions,
                zPositions;


        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Always makes a 256x128 map.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link LocalMap#LocalMap(long, int, int, Noise2D, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 128, FastNoise.instance, 1.0}.
         */
        public LocalMap() {
            this(0x1337BABE1337D00DL, 256, 128, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public LocalMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight,  FastNoise.instance,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public LocalMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses FastNoise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public LocalMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, FastNoise.instance, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise}
         */
        public LocalMap(long initialSeed, int mapWidth, int mapHeight, Noise2D noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed, the width/height of the map, and parameters for noise
         * generation (a {@link Noise3D} implementation, which is usually {@link FastNoise#instance}, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
         * cannot be changed after the fact, but you can zoom in. FastNoise will be the fastest 3D generator to use for
         * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
         * seed several times at different scales of noise (it's fine to use the static {@link FastNoise#instance}
         * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
         * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
         * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
         * that don't require zooming.
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param mapWidth the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise#instance}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public LocalMap(long initialSeed, int mapWidth, int mapHeight, Noise2D noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[width][height];
            yPositions = new double[width][height];
            zPositions = new double[width][height];

            terrain = new Noise.Ridged2D(noiseGenerator, (int) (0.5 + octaveMultiplier * 10), terrainFreq);
            terrainLayered = new Noise.InverseLayered2D(noiseGenerator, (int) (1 + octaveMultiplier * 6), terrainRidgedFreq * 0.325);
            heat = new Noise.InverseLayered2D(noiseGenerator, (int) (0.5 + octaveMultiplier * 3), heatFreq, 0.75);
            moisture = new Noise.InverseLayered2D(noiseGenerator, (int) (0.5 + octaveMultiplier * 4), moistureFreq, 0.55);
            otherRidged = new Noise.Ridged2D(noiseGenerator, (int) (0.5 + octaveMultiplier * 6), otherFreq);
        }
        @Override
        public int wrapY(final int x, final int y)  {
            return Math.max(0, Math.min(y, height - 1));
        }
        
        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double coolMod, int stateA, int stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || coolMod != coolingModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
                minHeat0 = Double.POSITIVE_INFINITY;
                maxHeat0 = Double.NEGATIVE_INFINITY;
                minHeat1 = Double.POSITIVE_INFINITY;
                maxHeat1 = Double.NEGATIVE_INFINITY;
                minHeat = Double.POSITIVE_INFINITY;
                maxHeat = Double.NEGATIVE_INFINITY;
                minWet0 = Double.POSITIVE_INFINITY;
                maxWet0 = Double.NEGATIVE_INFINITY;
                minWet = Double.POSITIVE_INFINITY;
                maxWet = Double.NEGATIVE_INFINITY;
                cacheA = stateA;
                cacheB = stateB;
                fresh = true;
            }
            rng.setState(stateA, stateB);
            long seedA = rng.nextLong(), seedB = rng.nextLong(), seedC = rng.nextLong();
            int t;

            landModifier = (landMod <= 0) ? rng.nextDouble(0.29) + 0.91 : landMod;
            coolingModifier = (coolMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : coolMod;

            double p,
                    ps, pc,
                    qs, qc,
                    h, temp,
                    i_w = 1.0 / width, i_h = 1.0 / (height),
                    i_uw = usedWidth * i_w * i_w, i_uh = usedHeight * i_h * i_h, xPos, yPos = startY * i_h;
            for (int y = 0; y < height; y++, yPos += i_uh) { 
                xPos = startX * i_w;
                for (int x = 0, xt = 0; x < width; x++, xPos += i_uw) {
                    xPositions[x][y] = (xPos - .5) * 2.0;
                    yPositions[x][y] = (yPos - .5) * 2.0;
                    zPositions[x][y] = 0.0;
                    heightData[x][y] = (h = terrainLayered.getNoiseWithSeed(xPos +
                                    terrain.getNoiseWithSeed(xPos, yPos, seedB - seedA) * 0.5,
                            yPos, seedA) + landModifier - 1.0);
                    heatData[x][y] = (p = heat.getNoiseWithSeed(xPos, yPos
                                    + otherRidged.getNoiseWithSeed(xPos, yPos, seedB + seedC),
                            seedB));
                    temp = otherRidged.getNoiseWithSeed(xPos, yPos, seedC + seedA);
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(xPos - temp, yPos + temp, seedC));

                    minHeightActual = Math.min(minHeightActual, h);
                    maxHeightActual = Math.max(maxHeightActual, h);
                    if(fresh) {
                        minHeight = Math.min(minHeight, h);
                        maxHeight = Math.max(maxHeight, h);

                        minHeat0 = Math.min(minHeat0, p);
                        maxHeat0 = Math.max(maxHeat0, p);

                        minWet0 = Math.min(minWet0, temp);
                        maxWet0 = Math.max(maxWet0, temp);
                    }
                }
                minHeightActual = Math.min(minHeightActual, minHeight);
                maxHeightActual = Math.max(maxHeightActual, maxHeight);

            }
            double  heatDiff = 0.8 / (maxHeat0 - minHeat0),
                    wetDiff = 1.0 / (maxWet0 - minWet0),
                    hMod;
            yPos = startY * i_h + i_uh;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                for (int x = 0; x < width; x++) {
                    h = heightData[x][y];
                    heightCodeData[x][y] = (t = codeHeight(h));
                    hMod = 1.0;
                    switch (t) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            h = 0.4;
                            hMod = 0.2;
                            break;
                        case 6:
                            h = -0.1 * (h - forestLower - 0.08);
                            break;
                        case 7:
                            h *= -0.25;
                            break;
                        case 8:
                            h *= -0.4;
                            break;
                        default:
                            h *= 0.05;
                    }
                    heatData[x][y] = (h = ((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6);
                    if (fresh) {
                        ps = Math.min(ps, h); //minHeat0
                        pc = Math.max(pc, h); //maxHeat0
                    }
                }
            }
            if(fresh)
            {
                minHeat1 = ps;
                maxHeat1 = pc;
            }
            heatDiff = coolingModifier / (maxHeat1 - minHeat1);
            qs = Double.POSITIVE_INFINITY;
            qc = Double.NEGATIVE_INFINITY;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;


            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    heatData[x][y] = (h = ((heatData[x][y] - minHeat1) * heatDiff));
                    moistureData[x][y] = (temp = (moistureData[x][y] - minWet0) * wetDiff);
                    if (fresh) {
                        qs = Math.min(qs, h);
                        qc = Math.max(qc, h);
                        ps = Math.min(ps, temp);
                        pc = Math.max(pc, temp);
                    }
                }
            }
            if(fresh)
            {
                minHeat = qs;
                maxHeat = qc;
                minWet = ps;
                maxWet = pc;
            }
            landData.refill(heightCodeData, 4, 999);
        }
    }

    /**
     * An unusual map generator that imitates an existing local map (such as a map of Australia, which it can do by
     * default), without applying any projection or changing heat levels in the polar regions or equator.
     */
    public static class LocalMimicMap extends LocalMap
    {
        public GreasedRegion earth;
        public GreasedRegion shallow;
        public GreasedRegion coast;
        public GreasedRegion earthOriginal;
        /**
         * Constructs a concrete WorldMapGenerator for a map that should look like Australia, without projecting the
         * land positions or changing heat by latitude. Always makes a 256x256 map.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link LocalMimicMap#LocalMimicMap(long, Noise2D, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, FastNoise.instance, 1.0}.
         */
        public LocalMimicMap() {
            this(0x1337BABE1337D00DL
                    , FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given GreasedRegion's "on" cells, without projecting the land positions or changing heat by latitude.
         * The initial seed is set to the same large long every time, and it's likely that you would set the seed when
         * you call {@link #generate(long)}. The width and height of the map cannot be changed after the fact.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param toMimic the world map to imitate, as a GreasedRegion with land as "on"; the height and width will be copied
         */
        public LocalMimicMap(GreasedRegion toMimic) {
            this(0x1337BABE1337D00DL, toMimic,  FastNoise.instance,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given GreasedRegion's "on" cells, without projecting the land positions or changing heat by latitude.
         * Takes an initial seed and the GreasedRegion containing land positions. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact.
         * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a GreasedRegion with land as "on"; the height and width will be copied
         */
        public LocalMimicMap(long initialSeed, GreasedRegion toMimic) {
            this(initialSeed, toMimic, FastNoise.instance, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given GreasedRegion's "on" cells, without projecting the land positions or changing heat by latitude.
         * Takes an initial seed, the GreasedRegion containing land positions, and a multiplier that affects the level
         * of detail by increasing or decreasing the number of octaves of noise used. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact.
         * Uses FastNoise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a GreasedRegion with land as "on"; the height and width will be copied
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public LocalMimicMap(long initialSeed, GreasedRegion toMimic, double octaveMultiplier) {
            this(initialSeed, toMimic, FastNoise.instance, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given GreasedRegion's "on" cells, without projecting the land positions or changing heat by latitude.
         * Takes an initial seed, the GreasedRegion containing land positions, and parameters for noise generation (a
         * {@link Noise3D} implementation, which is usually {@link FastNoise#instance}. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call
         * {@link #generate(long)}. The width and height of the map cannot be changed after the fact. Both FastNoise
         * and FastNoise make sense to use for {@code noiseGenerator}, and the seed it's constructed with doesn't matter
         * because this will change the seed several times at different scales of noise (it's fine to use the static
         * {@link FastNoise#instance} or {@link FastNoise#instance} because they have no changing state between runs
         * of the program). Uses the given noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a GreasedRegion with land as "on"; the height and width will be copied
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise} or {@link FastNoise}
         */
        public LocalMimicMap(long initialSeed, GreasedRegion toMimic, Noise2D noiseGenerator) {
            this(initialSeed, toMimic, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given GreasedRegion's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
         * Takes an initial seed, the GreasedRegion containing land positions, parameters for noise generation (a
         * {@link Noise3D} implementation, which is usually {@link FastNoise#instance}, and a multiplier on how many
         * octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers producing even more
         * detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
         * cannot be changed after the fact.  FastNoise will be the fastest 3D generator to use for
         * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
         * seed several times at different scales of noise (it's fine to use the static {@link FastNoise#instance}
         * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
         * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
         * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
         * that don't require zooming.
         * @param initialSeed the seed for the GWTRNG this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a GreasedRegion with land as "on"; the height and width will be copied
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link FastNoise} or {@link FastNoise}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public LocalMimicMap(long initialSeed, GreasedRegion toMimic, Noise2D noiseGenerator, double octaveMultiplier) {
            super(initialSeed, toMimic.width, toMimic.height, noiseGenerator, octaveMultiplier);
            earth = toMimic;
            earthOriginal = earth.copy();
            coast   = earth.copy().not().fringe(2);
            shallow = earth.copy().fringe(2);
        }

        /**
         * Constructs a 256x256 unprojected local map that will use land forms with a similar shape to Australia.
         * @param initialSeed
         * @param noiseGenerator
         * @param octaveMultiplier
         */
        public LocalMimicMap(long initialSeed, Noise2D noiseGenerator, double octaveMultiplier)
        {
            this(initialSeed,
                    GreasedRegion.deserializeFromString(LZSPlus.decompress(
                            "ঢ堻\u0089⤠怹؈䊺䯼曵䟗温撞⎠ࣦಡ泡⠣乹ƹ畗♛Ղມ᳝⦱Ħ♮塳6Ð⸨劙⦤䤶氦ф䱾悽䐱⥑۴挬塉历㊳ኻǨ䀫⇆㭩䧏ᡃᮀ呣吰撤✽ڱ䪪䌥\u0E74䒉䂰䱁牪\u0D5Dᠠ\u2B69ร硘₤↖ଵ࠵ቬ䇏惒稊泈⇬㋖⼨੦䐳Ñթత汬ぺ皨ќ⟫恱椧㴈䥤❩ڔ坋悦±\u0FEEយ䭔嗈Ⓒ͆४ニ嬧柩ኅ娅ấ㞺ᑆиऩ䦙䥖审撣筬礐かྫྷüඈ䖮⡑ቀ䋋ᢐ吽卣£ឌ৪樬兾᳠䌡惇䉡䤐嘰㥈᧹᪐Ģ傢\u209Eईㅥ剭匼才吺ཤ筜啭್刂\u0D49\u1CAD䬒棡s㦉簬⌤爘\u08DAᥤௐ㙯ᱭ吝ό䑌\u3098ᒟ嵶ㇹ㲍➇∊獬⏬ዹ癳㍱㜸嚻㥂Ꮝㄇ㳹㜊\u16FBㅿ㚈䕿\u2B76琧笙䑘瘄打≓坻Ȏ䣪\u0D5B礭⿹ೇ凛㵗\u0FE8ॽᤵন幯\u0FE6䱚懌煚睆Ἑ䊩ࣰᒐ燞盇獹亪咑ḟ⽔\u0E80乞悐箆䨎罘呮ॉ懜ὗᢥ炑ϳ㵏\u1DEA擕⍖ኗ☻ಽ斳Ǔկ仡㿣ₑ䨅┄嵋孨ဤӰ\u1AAF捺Α᰿⡻؎Ⴔ⠂ᆱ\u05CC愦aҠ翪⦄ᐥ籀瀍Ƞ牦粤Ṯ¶㼪棙ᖠ哧㧢̰溣秄ከၮォ\u0600栿䣔\u0B4F禆ैə〰䊢ጽ؛㪏ㇸ拂⋘挵\u058E⬺榈ᴺ⏘墲搘咿ᢀ溠碘碰匤夌君Ⲽ挘嬹◎\u1C4Aጔ䒰ጬ䶬㊌\u1CBF卄暴䂼㖭ਃ\u1ABFሬ䲋ΩƢґⶭ敾⤀版ᬥ㍼涾㍿ূ㎘妸卑㪥ௌ榱刭㹍௴箷㒔究猌牫劂䩲氪䙷䫧⇪ᄦɦᄶ↧↱⸌煎ቸ⭒䈤灌ڧ\u208F㚮…Ӣ椒⎲\u2B92䑸䡫㵰წἭ炊㭼櫢汿㏺䮳㊶棢ᯢ屰䇍ৣ㡆٣䣾⺦䗺戲歪売孬༣檔ᐰ\u1A5F㎬㋂ ⧪①ત嘿㮬⇢㧚堣ሆ粢ᇚ䬮创ⳤ⌼俾℞䉥⒡䌼᭗ᛁ㷶⢡⥧ݳ㯡婏㰚枤筱礯慚㶬ᆤ\u0C5Aギᵦ⥾⢡ᢙࡕŞᏡㅠᕓسூ屍ᚪ曩䝙⟙傽᳛ᒌ㍱揢楊噯熣Ӷⷆ文\u1ABA䝧䐅宎摶痋ᒄ㔾堲⭙値カ縛⚵䑮㫦旸㉀䴔⥛㥂␃墑丳夐壍ウ旫∞ᯒ㡝傸娴珐ཽ䍴܌⋂წ↬♴建机ᶧ成您唶ⅎ\u312Eㆮ堥歓䗏ᔴㆣᡗㅼᑙ䵱榓姫滜煾澱窴\u1AC1噀Γ㤜֖\u1C9C㡓⼘翔ু㍥亁殟⬜柹溅礙仺ཟ爎䄋\u31EA∰庺ጓ㡷捎⇷徵徺挒榷斓⩁䜢ỳ娙\u1F4F儙ޏ垓溻拍῾羿\u1F4Eᰗ澻掷ܲ⒮㖯⪅瀝઼䐭䪨⩑嫓ཤ㉚倸殅盲ຜѭᕱ䄥稽捻㦡՞Ѐ缣䤝䐝爺䢐㤦ₒञᐷ繈冶䄬䓊䞨玩嗬ޣ硃玕㐆䮆劁\u0B64ڂέҼ\u0588【您ࠄ©\u3101ແㄹȲḨ剌㈘ኀ懈䞞⊫Ω\u0CD8唱ᡊɈ掯䧅ݢԼ公ព汽䕄礵凈⽢剚拜ྀv䊨㓱䊓˄✻䉋#ῶ㱛侥椿䛘亗⌢狦䛊䛮 ᅅ処Ꮑ䩍ᅸ塭壍ᗴᔮ僂ɭᜮᏢ㢩ూ掠㵶\u0871瓼璿履ᕁ秥ᄋ彅⩠Щ㒠焏Éʱ䤮\u2E6Cİ㈌ದ⪗寤兪\u082Fƀ⤢ঌ歈眶Ң䯲帨ᄄ⓮屿䖨⅍灉ᒑ嚤絢⢆\u0A63䮀刪祔䯹⺿ᩒᑃ嵵且㵥ӋᎠ\u08A1⠽䢠᮸ㅜ杬\u20BE䧒冧㽌䗐掳碌䃀㑤\u0B04ᯰ䝚淍ỉ\u2066熀䔨䋥␥ࡋ榳ᵡ㽒獝下㑆ࠐ積ケૄ抺䦐師紺妨乩䑘Ⴗᠬ㜨଼ᖿ碟Ղ翰怠⊢悙䈸㏭㌞炒䙳ᥲᶭ儷痑判⣀哲瓅睖⧮⪼禡潴ㇴ\u1DEA幵厮䎴ઢ䠡曖亿壑ᤳᣣ抑嘄࿋ᨚّ\u0883⾼丰ㆆ嗽፡၆Ꮒ॔\u20FA磤䰪䲕ጢ瘊ޢ\u1755䍆ぇ宷欱\u0089啔爡㋄ଁ喒巪˪ჺ悐䆜đじ䚦ಎУኃ泴ƅᡰ懎㮰⺡⊆⊢㔁\u171C瀓\u2456ᄘᄃ玗⥨溉䩁➴㮿怵\u2BC8ᭆ焃♸\u10C8Ȩ䭦⊚⦭仼倴ઙ樤ᓳ䎤ἠᩌᇡ捷\u0099甡䢴Ȩጹ獰⟸はᮺᏤ㞁и滏磀唡竱᪀⚨ʗ猔ܥ㐶䇈珰䋵【ȯूῐਣᒒ畐撆ഘᨫ兡ߑ奈瀾⎛ⵖ歘੦偺\u0080儢悝䬪ǹ炈ኡ稴䁄༡㺞「仂悴ล∽璥崬爰ಘ㿵懯㉰翌↶\u08D1嫊⏇㰢氲砘㾠瀤䏡㢄း惛䣡\u2BE2ခవ䌸͒⼏倨忈፨氭\u2FEE区\u0BE1䵚愔\u242B捛吊₌仆湁嗰⇄ῑ㐩折ດ倪<\u1ACE炳⌳᜴刜ᔖ娸䎶ڤ㉥⇰ଭ㸲᎐井ߘ儃桓ϐ⇴䑙๊䙍烏Ɖ琳\u1DEAসᨡေ斢⠯䐰樧ັ㋅ҍᙨ獎ʰ壅䡃旔⡋糈戩㘬\u0EE7䁍孚〥⥹畱烚傊榇傟◀ኁ䄍䗃⦦ɕ啴磻ᮋ秈䈨⃦ĩ䞪Ỉぇ⫽⦥甧右⛅㌜ガ䊜淚ཁ㳧䋘⾼䒀♺朠樒̬͛愱㔴想䊽䐢嬀惉ધ䎃㚼ᓾ㣾昍㮬寝玥涿Ả㽜Ѳᩬឰњ壍च⺱涥㍸丸秆⓼姁⪾Ԟ丿牢ỡ瞻⡕\u0CD1咊ߐ⠣吣惢ॠ䇺ള癇喌⣉碝⥪䵥\u2452㐉玀Żツٛ剌㾇⁼秌皔䴕໙昆㹅恌\u1ACE縆冸㦡樲峫柆ⲅ緊\u0E79ᑳ粤禐㱵愋์禐ɼ䮎穂啈墓竘䀣㫷䯹⋪䍥級〚᷈瞚ؕํ㍖⢽㚐䵖璍䓜\u2E3C氓Ḿ塱ኋ宱㮴㚑㫃\u2BC0⌙瞅秿烋巳敟Τ縖⬬侔䶙☥Ⅴẉ杷ю漭䠛乖㍒婔᷃ไ⻄ᬶ䰴⇚玥∫䐋崫ガ䞲澍⍂᎙䘛⨭疹㗖Ի⦐⒚倁缹秬瀎ा潮恜Ḗݗ獭捾ēૡ䫳喛ോѺ⳿ᖘଦ纠\u08D3ɱ燭濄ㆱ㇗仔\u07B9咫簾笨౾с⧀獜䩦宵த⛉挣㴟\u1F46翖▇梪द㳔礝⾀⬣犹棠ᢞ愵ㅁᅌ揬牧㐷㧈֞攤绊磿潔吨咹ᢆ䋰⫇ⱛᘐẊ伥⿒䆇殡✗Ȫ䡫䣨䆣ᠧ䄩ؖⱧ傗攔Ⲩ展㱉璟\u2434乁⌣\u4DB6¦濱巐䦤ᨢ䎠ఠࡉê䱄矝污ր➡\u08C2䈨᭤狆柝䰩ׁ݄⠺甝柁㈫≑⇉⼺紫恷၅ь⬪㕅ᒝঁ癵䛒昌ᓈ婱䪠想䎖并㲷ᔎ➪ࣷ₂ᕜṼ珇ⷜ䴡䗖ᛧ弻ℒζ哖ᆹⲺन笕ૃ⡷⫝̸⻖≍F\u0CFC݃硰熓\u0AD8㮰Ἰ䀲ǜ\u0EE4温点\u0BD8௦咺灧␊㘨渺㴗⩐暮㫋⌔ฎ儕ၨ↣⸜捄ཕ㦿䴬ڢ䛝䄔ช溭㫑洠⛔ः࿙ᥙ䕬ᄦ\u2455帚\u1AB2如嫓ᄮ惰̗ጳ焉䧌㦀倢堪ᅾ\u0530㣄䐰ڌ缡瀨⸗榨ⷥṓၸñ㉆䝛壒懨⫱ᒑ㡍漪㉰Ⱡ慚॔狡䴵〾\u0B3A䋑᷒壆䅸姆\u1CA0唞Ñ䩔䌷疧拤犒†䵛⋊氣液䈘˚ѕ㠡ᅐ䠰紦睐䁗ɠ猠ろቨ૩䜣㉫\u13F9ᗩ†"
                    )),
                    noiseGenerator, octaveMultiplier);
        }
        
        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double coolMod, int stateA, int stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || coolMod != coolingModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
                minHeat0 = Double.POSITIVE_INFINITY;
                maxHeat0 = Double.NEGATIVE_INFINITY;
                minHeat1 = Double.POSITIVE_INFINITY;
                maxHeat1 = Double.NEGATIVE_INFINITY;
                minHeat = Double.POSITIVE_INFINITY;
                maxHeat = Double.NEGATIVE_INFINITY;
                minWet0 = Double.POSITIVE_INFINITY;
                maxWet0 = Double.NEGATIVE_INFINITY;
                minWet = Double.POSITIVE_INFINITY;
                maxWet = Double.NEGATIVE_INFINITY;
                cacheA = stateA;
                cacheB = stateB;
                fresh = true;
            }
            rng.setState(stateA, stateB);
            long seedA = rng.nextLong(), seedB = rng.nextLong(), seedC = rng.nextLong();
            int t;

            landModifier = (landMod <= 0) ? rng.nextDouble(0.29) + 0.91 : landMod;
            coolingModifier = (coolMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : coolMod;

            earth.remake(earthOriginal);

            if(zoom > 0)
            {
                int stx = Math.min(Math.max((zoomStartX - (width  >> 1)) / ((2 << zoom) - 2), 0), width ),
                        sty = Math.min(Math.max((zoomStartY - (height >> 1)) / ((2 << zoom) - 2), 0), height);
                for (int z = 0; z < zoom; z++) {
                    earth.zoom(stx, sty).expand8way().fray(0.5).expand();
                }
                coast.remake(earth).not().fringe(2 << zoom).expand().fray(0.5);
                shallow.remake(earth).fringe(2 << zoom).expand().fray(0.5);
            }
            else
            {
                coast.remake(earth).not().fringe(2);
                shallow.remake(earth).fringe(2);
            }
            double p,
                    ps, pc,
                    qs, qc,
                    h, temp,
                    i_w = 1.0 / width, i_h = 1.0 / (height),
                    i_uw = usedWidth * i_w * i_w, i_uh = usedHeight * i_h * i_h, xPos, yPos = startY * i_h;
            for (int y = 0; y < height; y++, yPos += i_uh) {
                xPos = startX * i_w;
                for (int x = 0, xt = 0; x < width; x++, xPos += i_uw) {
                    xPositions[x][y] = (xPos - .5) * 2.0;
                    yPositions[x][y] = (yPos - .5) * 2.0;
                    zPositions[x][y] = 0.0;

                    if(earth.contains(x, y))
                    {
                        h = NumberTools.swayTight(terrainLayered.getNoiseWithSeed(xPos +
                                        terrain.getNoiseWithSeed(xPos, yPos, seedB - seedA) * 0.5,
                                yPos, seedA)) * 0.85;
                        if(coast.contains(x, y))
                            h += 0.05;
                        else
                            h += 0.15;
                    }
                    else
                    {
                        h = NumberTools.swayTight(terrainLayered.getNoiseWithSeed(xPos +
                                        terrain.getNoiseWithSeed(xPos, yPos, seedB - seedA) * 0.5,
                                yPos, seedA)) * -0.9;
                        if(shallow.contains(x, y))
                            h = (h - 0.08) * 0.375;
                        else
                            h = (h - 0.125) * 0.75;
                    }
                    //h += landModifier - 1.0;
                    heightData[x][y] = h;
                    heatData[x][y] = (p = heat.getNoiseWithSeed(xPos, yPos
                                    + otherRidged.getNoiseWithSeed(xPos, yPos, seedB + seedC),
                            seedB));
                    temp = otherRidged.getNoiseWithSeed(xPos, yPos, seedC + seedA);
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(xPos - temp, yPos + temp, seedC));

                    minHeightActual = Math.min(minHeightActual, h);
                    maxHeightActual = Math.max(maxHeightActual, h);
                    if(fresh) {
                        minHeight = Math.min(minHeight, h);
                        maxHeight = Math.max(maxHeight, h);

                        minHeat0 = Math.min(minHeat0, p);
                        maxHeat0 = Math.max(maxHeat0, p);

                        minWet0 = Math.min(minWet0, temp);
                        maxWet0 = Math.max(maxWet0, temp);
                    }
                }
                minHeightActual = Math.min(minHeightActual, minHeight);
                maxHeightActual = Math.max(maxHeightActual, maxHeight);

            }
            double  heatDiff = 0.8 / (maxHeat0 - minHeat0),
                    wetDiff = 1.0 / (maxWet0 - minWet0),
                    hMod;
            yPos = startY * i_h + i_uh;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                for (int x = 0; x < width; x++) {
                    h = heightData[x][y];
                    heightCodeData[x][y] = (t = codeHeight(h));
                    hMod = 1.0;
                    switch (t) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                            h = 0.4;
                            hMod = 0.2;
                            break;
                        case 6:
                            h = -0.1 * (h - forestLower - 0.08);
                            break;
                        case 7:
                            h *= -0.25;
                            break;
                        case 8:
                            h *= -0.4;
                            break;
                        default:
                            h *= 0.05;
                    }
                    heatData[x][y] = (h = ((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6);
                    if (fresh) {
                        ps = Math.min(ps, h); //minHeat0
                        pc = Math.max(pc, h); //maxHeat0
                    }
                }
            }
            if(fresh)
            {
                minHeat1 = ps;
                maxHeat1 = pc;
            }
            heatDiff = coolingModifier / (maxHeat1 - minHeat1);
            qs = Double.POSITIVE_INFINITY;
            qc = Double.NEGATIVE_INFINITY;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;


            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    heatData[x][y] = (h = ((heatData[x][y] - minHeat1) * heatDiff));
                    moistureData[x][y] = (temp = (moistureData[x][y] - minWet0) * wetDiff);
                    if (fresh) {
                        qs = Math.min(qs, h);
                        qc = Math.max(qc, h);
                        ps = Math.min(ps, temp);
                        pc = Math.max(pc, temp);
                    }
                }
            }
            if(fresh)
            {
                minHeat = qs;
                maxHeat = qc;
                minWet = ps;
                maxWet = pc;
            }
            landData.refill(heightCodeData, 4, 999);
        }
    }
}
