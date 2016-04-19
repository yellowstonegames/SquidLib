package squidpony.squidgrid.mapping;

import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Map generator using Perlin/Simplex noise for the formation of "rooms" and then WobblyLine to connect with corridors.
 * Created by Tommy Ettinger on 4/18/2016.
 */
public class OrganicMapGenerator {
    public char[][] map;
    public int[][] environment;
    public RNG rng;
    protected int width, height;
    protected double noiseMin, noiseMax;
    public OrganicMapGenerator()
    {
        this(0.3, 0.6, 80, 30, new RNG());
    }
    public OrganicMapGenerator(int width, int height)
    {
        this(0.3, 0.6, width, height, new RNG());
    }
    public OrganicMapGenerator(int width, int height, RNG rng)
    {
        this(0.3, 0.6, width, height, rng);
    }
    public OrganicMapGenerator(double noiseMin, double noiseMax, int width, int height, RNG rng)
    {
        this.rng = rng;
        this.width = Math.max(3, width);
        this.height = Math.max(3, height);
        this.noiseMin = Math.min(0.9, Math.max(-1.0, noiseMin));
        this.noiseMax = Math.min(1.0, Math.max(noiseMin + 0.05, noiseMax));
        map = new char[this.width][this.height];
        environment = new int[this.width][this.height];
    }

    /**
     * Generate a map as a 2D char array using the width and height specified in the constructor.
     * Should produce an organic, cave-like map.
     * @return a 2D char array for the map that should be organic-looking.
     */
    public char[][] generate()
    {
        double layer = rng.nextDouble(1024), temp;
        boolean[][] working = new boolean[width][height];
        int ctr = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = '#';
                temp = PerlinNoise.noise(x / 2.5, y / 2.5, layer);
                if (temp >= noiseMin && temp <= noiseMax) {
                    working[x][y] = true;
                    ctr++;
                }
            }
        }
        if(ctr < (width + height) * 3) {
            noiseMin = Math.min(0.9, Math.max(-1.0, noiseMin - 0.05));
            noiseMax = Math.min(1.0, Math.max(noiseMin + 0.05, noiseMax + 0.05));
            return generate();
        }
        ctr = 0;
        ArrayList<short[]> allRegions = CoordPacker.split(CoordPacker.pack(working)),
                regions = new ArrayList<>(allRegions.size());
        short[] region, linking, r2;
        List<Coord> path;
        Coord start, end;
        char[][] t;
        for (short[] r : allRegions) {
            if (CoordPacker.count(r) > 5) {
                region = CoordPacker.expand(r, 1, width, height, false);
                if(CoordPacker.isEmpty(region))
                    continue;
                regions.add(region);
                ctr += CoordPacker.count(region);
                r2 = CoordPacker.negatePacked(region);
                map = CoordPacker.mask(map, r2, '.');
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                environment[x][y] = (map[x][y] == '.') ? MixedGenerator.CAVE_FLOOR : MixedGenerator.CAVE_WALL;
            }
        }
        double oldSize = regions.size();
        if(oldSize < 4 || ctr < (width + height) * 3) {
            noiseMin = Math.min(0.9, Math.max(-1.0, noiseMin - 0.05));
            noiseMax = Math.min(1.0, Math.max(noiseMin + 0.05, noiseMax + 0.05));
            return generate();
        }
        while (regions.size() > 1)
        {
            region = regions.remove(rng.nextInt(regions.size()));
            linking = regions.get(rng.nextInt(regions.size()));
            if(rng.nextDouble(0.7) > regions.size() / oldSize) {
                ctr -= CoordPacker.count(region);
                continue;
            }
            start = CoordPacker.singleRandom(region, rng);
            end = CoordPacker.singleRandom(linking, rng);
            path = WobblyLine.line(start.x, start.y, end.x, end.y, width, height, 0.7, rng);
            for(Coord elem : path)
            {
                if(elem.x < width && elem.y < height && map[elem.x][elem.y] == '#')
                {
                    map[elem.x][elem.y] = '.';
                    environment[elem.x][elem.y] = MixedGenerator.CORRIDOR_FLOOR;
                    ctr++;
                }
            }
        }
        int upperY = height - 1;
        int upperX = width - 1;
        for (int i = 0; i < width; i++) {
            map[i][0] = '#';
            map[i][upperY] = '#';
            environment[i][0] = MixedGenerator.UNTOUCHED;
            environment[i][upperY] = MixedGenerator.UNTOUCHED;
        }
        for (int i = 0; i < height; i++) {
            map[0][i] = '#';
            map[upperX][i] = '#';
            environment[0][i] = MixedGenerator.UNTOUCHED;
            environment[upperX][i] = MixedGenerator.UNTOUCHED;
        }
        if(ctr < (width + height) * 3) {
            noiseMin = Math.min(0.9, Math.max(-1.0, noiseMin - 0.05));
            noiseMax = Math.min(1.0, Math.max(noiseMin + 0.05, noiseMax + 0.05));
            return generate();
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
}
