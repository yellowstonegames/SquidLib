package squidpony.squidgrid.mapping;

import squidpony.annotation.Beta;
import squidpony.squidmath.Coord;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.PerlinNoise;
import squidpony.squidmath.StatefulRNG;

import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.round;

/**
 * A map generation factory using perlin noise to make island chain style maps.
 *
 * Based largely on work done by Metsa from #rgrd
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class MetsaMapFactory {
    //HEIGHT LIMITS

    //BIOMESTUFF
    private final double POLARLIMIT = 0.5, DESERTLIMIT = 0.1;

    //SHADOW
    private final double SHADOWLIMIT = 0.01;
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

    public MetsaMapFactory()
    {
        rng = new StatefulRNG();
        width = 1000;
        height = 600;
    }
    public MetsaMapFactory(int width, int height)
    {
        rng = new StatefulRNG();
        this.width = width;
        this.height = height;
    }
    public MetsaMapFactory(int width, int height, long rngSeed)
    {
        rng = new StatefulRNG(new LightRNG(rngSeed));
        this.width = width;
        this.height = height;
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
        if (slope < SHADOWLIMIT && slope > -SHADOWLIMIT) {
            return 0;
        }
        if (slope >= SHADOWLIMIT) {
            return -1; //"alpha"
        }
        if (slope <= -SHADOWLIMIT) {
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
        double[][] map = MapFactory.heightMap(width, height, rng);

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

	public int[][] makeBiomeMap(double[][] map) {
        //biomes 0 normal 1 snow
        int biomeMap[][] = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                biomeMap[x][y] = 0;
                double distanceFromEquator = Math.abs(y - height / 2.0) / (height / 2.0);
                distanceFromEquator += PerlinNoise.noise(x / 32, y / 32) / 8 + map[x][y] / 32;
                if (distanceFromEquator > POLARLIMIT) {
                    biomeMap[x][y] = 1;
                }
                if (distanceFromEquator < DESERTLIMIT) {
                    biomeMap[x][y] = 2;
                }
                if (distanceFromEquator > POLARLIMIT + 0.25) {
                    biomeMap[x][y] = 3;
                }
            }
        }
        return biomeMap;
    }

	public int[][] makeNationMap(double[][] map) {
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

	public double[][] makeWeightedMap(double[][] map) {
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
                    weightedMap[i][j] = 2 - (map[i * 4][j * 4]) * 2;
                }
            }
        }

        for (int i = 0; i < CITYAMOUNT; i++) {
            int px = rng.between(0, width);
            int py = rng.between(0, height);
            while (map[px][py] < SEALEVEL || map[px][py] > BEACHLEVEL) {
                px = rng.between(0, width);
                py = rng.between(0, height);
            }
            cities.add(Coord.get(4 * round(px / 4F), 4 * round(py / 4F)));
        }
        return weightedMap;
    }

    public List<Coord> getCities() {
        return cities;
    }

    public double getMaxPeak() {
        return maxPeak;
    }
}
