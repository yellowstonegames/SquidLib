package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.BathtubDistribution;
import squidpony.squidmath.FastNoise;
import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.IRNG;

/**
 * Map generator that produces erratic, non-artificial-looking areas that could be cave complexes. This works by
 * generating a region of continuous noise and two distant points in it, progressively using larger
 * portions of the noise until it connects the points. The algorithm was discovered by tann, a libGDX user and noise
 * aficionado, and it works a little more cleanly than the old approach this class used.
 * <br>
 * Initially created by Tommy Ettinger on 4/18/2016, reworked a few times, now mostly tann's work.
 */
public class OrganicMapGenerator implements IDungeonGenerator {
    public char[][] map;
    public int[][] environment;
    public GreasedRegion floors;
    public IRNG rng;
    protected int width, height;
    public double noiseMin, noiseMax;
    protected FastNoise noise;

    public OrganicMapGenerator()
    {
        this(0.55, 0.65, 80, 30, new GWTRNG());
    }
    public OrganicMapGenerator(int width, int height)
    {
        this(0.55, 0.65, width, height, new GWTRNG());
    }
    public OrganicMapGenerator(int width, int height, IRNG rng)
    {
        this(0.55, 0.65, width, height, rng);
    }
    public OrganicMapGenerator(double noiseMin, double noiseMax, int width, int height, IRNG rng)
    {
        this.rng = rng;
        this.width = Math.max(3, width);
        this.height = Math.max(3, height);
        this.noiseMin = Math.min(0.9, Math.max(-1.0, noiseMin - 0.1));
        this.noiseMax = Math.min(1.0, Math.max(noiseMin + 0.05, noiseMax + 0.1));
        map = new char[this.width][this.height];
        environment = new int[this.width][this.height];
        floors = new GreasedRegion(width, height);
        noise = new FastNoise(1, 0.375f, FastNoise.HONEY_FRACTAL, 2);
    }

    /**
     * Generate a map as a 2D char array using the width and height specified in the constructor.
     * Should produce an organic, cave-like map.
     * @return a 2D char array for the map that should be organic-looking.
     */
    public char[][] generate() {
        noise.setSeed(rng.nextInt());
        double[][] noiseMap = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                noiseMap[x][y] = noise.getConfiguredNoise(x, y);
            }
        }
        int w2 = width - 2, h2 = height - 2;
        int startX = (int) (BathtubDistribution.instance.nextDouble(rng) * w2) + 1;
        int endX = (int) (BathtubDistribution.instance.nextDouble(rng) * w2) + 1;
        int startY = (int) (BathtubDistribution.instance.nextDouble(rng) * h2) + 1;
        int endY = (int) (BathtubDistribution.instance.nextDouble(rng) * h2) + 1;
        while (Radius.CIRCLE.radius(startX, startY, endX, endY) * 3 < width + height){
            startX = (int) (BathtubDistribution.instance.nextDouble(rng) * w2) + 1;
            endX = (int) (BathtubDistribution.instance.nextDouble(rng) * w2) + 1;
            startY = (int) (BathtubDistribution.instance.nextDouble(rng) * h2) + 1;
            endY = (int) (BathtubDistribution.instance.nextDouble(rng) * h2) + 1;
        }
        GreasedRegion region = new GreasedRegion(width, height), linking;
        for (int i = 1; i <= 25; i++) {
            noiseMax = 0.04 * i;
            floors.refill(noiseMap, -noiseMax, noiseMax);
            if(floors.contains(startX, startY) && floors.contains(endX, endY) && 
                    region.empty().insert(startX, startY).flood(floors, width * height).contains(endX, endY))
                break;
        }
        floors.intoChars(map, '.', '#');
        ArrayTools.fill(environment, DungeonUtility.UNTOUCHED);
        floors.writeIntsInto(environment, DungeonUtility.CAVE_FLOOR);
        region.remake(floors).fringe8way().writeIntsInto(environment, DungeonUtility.CAVE_WALL);
        int upperY = height - 1;
        int upperX = width - 1;
        for (int i = 0; i < width; i++) {
            map[i][0] = '#';
            map[i][upperY] = '#';
            environment[i][0] = DungeonUtility.UNTOUCHED;
            environment[i][upperY] = DungeonUtility.UNTOUCHED;
        }
        for (int i = 0; i < height; i++) {
            map[0][i] = '#';
            map[upperX][i] = '#';
            environment[0][i] = DungeonUtility.UNTOUCHED;
            environment[upperX][i] = DungeonUtility.UNTOUCHED;
        }
        
        return map;
    }


    /**
     * Gets a 2D array of int constants, each representing a type of environment corresponding to a static field of
     * MixedGenerator. This array will have the same size as the last char 2D array produced by generate(); the value
     * of this method if called before generate() is undefined, but probably will be a 2D array of all 0 (UNTOUCHED).
     * <ul>
     *     <li>MixedGenerator.UNTOUCHED, equal to 0, is used for any cells that aren't near a floor.</li>
     *     <li>MixedGenerator.ROOM_FLOOR, equal to 1, is used for floor cells inside wide room areas.</li>
     *     <li>MixedGenerator.ROOM_WALL, equal to 2, is used for wall cells around wide room areas.</li>
     *     <li>MixedGenerator.CAVE_FLOOR, equal to 3, is used for floor cells inside rough cave areas.</li>
     *     <li>MixedGenerator.CAVE_WALL, equal to 4, is used for wall cells around rough cave areas.</li>
     *     <li>MixedGenerator.CORRIDOR_FLOOR, equal to 5, is used for floor cells inside narrow corridor areas.</li>
     *     <li>MixedGenerator.CORRIDOR_WALL, equal to 6, is used for wall cells around narrow corridor areas.</li>
     * </ul>
     * @return a 2D int array where each element is an environment type constant in MixedGenerator
     */
    public int[][] getEnvironment()
    {
        return environment;
    }

    public char[][] getDungeon() {
        return map;
    }
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getNoiseMin() {
        return noiseMin;
    }

    public void setNoiseMin(double noiseMin) {
        this.noiseMin = noiseMin;
    }

    public double getNoiseMax() {
        return noiseMax;
    }

    public void setNoiseMax(double noiseMax) {
        this.noiseMax = noiseMax;
    }

}
