package squidpony.squidgrid.mapping;

import squidpony.annotation.Beta;
import squidpony.squidmath.Coord;
import squidpony.squidmath.PerlinNoise;
import squidpony.squidmath.StatefulRNG;

import java.util.LinkedList;
import java.util.List;

/**
 * A map generation factory using Perlin noise to make island chain style maps.
 *
 * Based largely on work done by Metsa from #rgrd
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class MetsaMapFactory {
    //HEIGHT LIMITS

    public static final double SEA_LEVEL = 0,
            BEACH_LEVEL = 0.15,
            PLAINS_LEVEL = 0.5,
            MOUNTAIN_LEVEL = 0.73,
            SNOW_LEVEL = 0.95,
            DEEP_SEA_LEVEL = -0.1;

    //BIOMESTUFF
    private final double POLAR_LIMIT = 0.65, DESERT_LIMIT = 0.15;

    //SHADOW
    private final double SHADOW_LIMIT = 0.01;
//COLORORDER
/*
     0 = deepsea
     1 = beach
     2 = low
     3 = high
     4 = mountain
     5 = snowcap
     6 = lowsea
     */
//            new SColor[]{SColor.DARK_SLATE_GRAY, SColor.SCHOOL_BUS_YELLOW, SColor.YELLOW_GREEN,
//        SColor.GREEN_BAMBOO, SColorFactory.lighter(SColor.LIGHT_BLUE_SILK), SColor.ALICE_BLUE, SColor.AZUL};
//            new SColor[]{SColor.DARK_SLATE_GRAY, SColor.SCHOOL_BUS_YELLOW, SColor.YELLOW_GREEN,
//        SColor.GREEN_BAMBOO, SColorFactory.lighter(SColor.LIGHT_BLUE_SILK), SColor.ALICE_BLUE, SColor.AZUL};

    private int width;
    private int height;
    private int CITYAMOUNT = 14;

    private List<Coord> cities = new LinkedList<>();
    private StatefulRNG rng;
    private double maxPeak = 0;
    private double[][] map;
    public MetsaMapFactory()
    {
        this(240, 120, new StatefulRNG());
    }
    public MetsaMapFactory(int width, int height)
    {
        this(width, height, new StatefulRNG());
    }
    public MetsaMapFactory(int width, int height, long rngSeed)
    {
        this(width, height, new StatefulRNG(rngSeed));
    }

    public MetsaMapFactory(int width, int height, StatefulRNG rng)
    {
        this.rng = rng;
        this.width = width;
        this.height = height;
        map = makeHeightMap();
    }

	public int getShadow(int x, int y, double[][] map) {
        if (x >= width - 1 || y <= 0) {
            return 0;
        }
        double upRight = map[x + 1][y - 1];
        double right = map[x + 1][y];
        double up = map[x][y - 1];
        double cur = map[x][y];
        if (cur <= 0) {
            return 0;
        }
        double slope = cur - (upRight + up + right) / 3;
        if (slope < SHADOW_LIMIT && slope > -SHADOW_LIMIT) {
            return 0;
        }
        if (slope >= SHADOW_LIMIT) {
            return -1; //"alpha"
        }
        if (slope <= -SHADOW_LIMIT) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Finds and returns the closest point containing a city to the given point.
     * Does not include provided point as a possible city location.
     *
     * If there are no cities, null is returned.
     *
     * @param point
     * @return
     */
	public Coord closestCity(Coord point) {
        double dist = 999999999, newdist;
        Coord closest = null;
        for (Coord c : cities) {
            if (c.equals(point)) {
                continue;//skip the one being tested for
            }
            newdist = Math.pow(point.x - c.x, 2) + Math.pow(point.y - c.y, 2);
            if (newdist < dist) {
                dist = newdist;
                closest = c;
            }
        }
        return closest;
    }

	public double[][] makeHeightMap() {
        double[][] map = HeightMapFactory.heightMap(width, height, rng.nextInt() * 0x1p-16f);

        for (int x = 0; x < width / 8; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = map[x][y] - 1.0 + x / ((width - 1) * 0.125);
                if (map[x][y] > maxPeak) {
                    maxPeak = map[x][y];
                }
            }
        }

        for (int x = width / 8; x < 7 * width / 8; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = map[x][y];
                if (map[x][y] > maxPeak) {
                    maxPeak = map[x][y];
                }
            }
        }

        for (int x = 7 * width / 8; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = map[x][y] - 1.0 + (width - 1 - x) / ((width - 1) * 0.125);
                if (map[x][y] > maxPeak) {
                    maxPeak = map[x][y];
                }
            }
        }

        return map;
    }

    public void regenerateHeightMap()
    {
        map = makeHeightMap();
    }
    public void regenerateHeightMap(int width, int height)
    {
        this.width = width;
        this.height = height;
        map = makeHeightMap();
        cities.clear();
    }

	public int[][] makeBiomeMap() {
        //biomes 0 normal 1 snow
        int biomeMap[][] = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                biomeMap[x][y] = 0;
                double distanceFromEquator = Math.abs(y - height * 0.5) / (height * 0.5);
                distanceFromEquator += PerlinNoise.noise(x * 0.0625, y * 0.0625) / 8 + map[x][y] / 32;
                if (distanceFromEquator > POLAR_LIMIT) {
                    biomeMap[x][y] = 1;
                }
                if (distanceFromEquator < DESERT_LIMIT) {
                    biomeMap[x][y] = 2;
                }
                if (distanceFromEquator > POLAR_LIMIT + 0.25) {
                    biomeMap[x][y] = 3;
                }
            }
        }
        return biomeMap;
    }

	public int[][] makeNationMap() {
        // nationmap, 4 times less accurate map used for nations -1 no nation
        int nationMap[][] = new int[width][height];
        for (int i = 0; i < width / 4; i++) {
            for (int j = 0; j < height / 4; j++) {
                if (map[i * 4][j * 4] < 0) {
                    nationMap[i][j] = -1;
                } else {
                    nationMap[i][j] = 0;
                }
            }
        }
        return nationMap;
    }

	public double[][] makeWeightedMap() {
        //Weighted map for road
        double weightedMap[][] = new double[width][height];
        double SEALEVEL = 0;
        double BEACHLEVEL = 0.05;
        double PLAINSLEVEL = 0.3;
        for (int i = 0; i < width / 4; i++) {
            for (int j = 0; j < height / 4; j++) {
                weightedMap[i][j] = 0;
                if (map[i * 4][j * 4] > BEACHLEVEL) {
                    weightedMap[i][j] = 2 + (map[i * 4][j * 4] - PLAINSLEVEL) * 8;
                }
                if (map[i][j] <= BEACHLEVEL && map[i * 4][j * 4] >= SEALEVEL) {
                    weightedMap[i][j] = 2 - map[i * 4][j * 4] * 2;
                }
            }
        }

        CITIES:
        for (int i = 0; i < CITYAMOUNT; i++) {
            int px = rng.between(0, width), py = rng.between(0, height), frustration = 0;
            while (map[px][py] < SEALEVEL || map[px][py] > BEACHLEVEL) {
                px = rng.between(0, width);
                py = rng.between(0, height);
                if(frustration++ > 20)
                    continue CITIES;
            }
            cities.add(Coord.get(4 * (px >> 2), 4 * (py >> 2)));
        }
        return weightedMap;
    }

    public List<Coord> getCities() {
        return cities;
    }

    public double getMaxPeak() {
        return maxPeak;
    }

    public double[][] getHeightMap() {
        return map;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
