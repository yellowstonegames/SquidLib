package squidpony.squidgrid.mapping;

import squidpony.squidmath.Coord;
import squidpony.squidmath.FastNoise;
import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.IRNG;
import squidpony.squidmath.WobblyLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

/**
 * Map generator using Simplex noise for the formation of "rooms" and then WobblyLine to connect with corridors.
 * Created by Tommy Ettinger on 4/18/2016.
 */
public class OrganicMapGenerator implements IDungeonGenerator {
    public char[][] map;
    public int[][] environment;
    public GreasedRegion floors;
    public IRNG rng;
    protected int width, height;
    public double noiseMin, noiseMax;
    protected FastNoise noise;
    private static final Comparator<GreasedRegion> sizeComparator = new Comparator<GreasedRegion>() {
        @Override
        public int compare(GreasedRegion o1, GreasedRegion o2) {
            return o2.size() - o1.size();
        }
    };

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
        noise = new FastNoise(1, 0.333f, FastNoise.SIMPLEX_FRACTAL, 2);
    }

    /**
     * Generate a map as a 2D char array using the width and height specified in the constructor.
     * Should produce an organic, cave-like map.
     * @return a 2D char array for the map that should be organic-looking.
     */
    public char[][] generate()
    {
        double temp;
        int frustration = 0;
        while (frustration < 10) {
            noise.setSeed(rng.nextInt());
            floors.clear();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    map[x][y] = '#';
                    temp = noise.getConfiguredNoise(x, y);
                    if (temp >= noiseMin && temp <= noiseMax) {
                        floors.insert(x, y);
                    }
                }
            }
            if (floors.size() < width * height * 0.1f) {
                frustration++;
                continue;
            }
            break;
        }
        if(frustration >= 10) {
            noiseMin = Math.min(0.9, Math.max(-1.0, noiseMin - 0.05));
            noiseMax = Math.min(1.0, Math.max(noiseMin + 0.05, noiseMax + 0.05));
            return generate();
        }
        ArrayList<GreasedRegion> regions = floors.split();
        GreasedRegion region, linking;
        ArrayList<Coord> path;
        Coord start, end;
        Collections.sort(regions, sizeComparator);
        ListIterator<GreasedRegion> ri = regions.listIterator();
        int ctr = 0, rs = regions.size() >> 1, pos = 0;
        while (ri.hasNext())
        {
            region = ri.next();
            if(pos++ > rs || region.size() <= 5)
                ri.remove();
            else {
                region.expand().inverseMask(map, '.');
                ctr += region.size();
            }
        }
        int oldSize = regions.size();
        if(oldSize < 4 || ctr < width * height * 0.1f) {
            noiseMin = Math.min(0.9, Math.max(-1.0, noiseMin - 0.05));
            noiseMax = Math.min(1.0, Math.max(noiseMin + 0.05, noiseMax + 0.05));
            return generate();
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                environment[x][y] = (map[x][y] == '.') ? DungeonUtility.CAVE_FLOOR : DungeonUtility.CAVE_WALL;
            }
        }
        rng.shuffleInPlace(regions);
        while (regions.size() > 1)
        {

            region = regions.remove(regions.size() - 1);
            linking = regions.get(regions.size() - 1);
            start = region.singleRandom(rng);
            end = linking.singleRandom(rng);
            path = WobblyLine.line(start.x, start.y, end.x, end.y, width, height, 0.75, rng);
            Coord elem;
            for (int i = 0; i < path.size(); i++) {
                elem = path.get(i);
                if(elem.x < width && elem.y < height) {
                    if (map[elem.x][elem.y] == '#') {
                        map[elem.x][elem.y] = '.';
                        environment[elem.x][elem.y] = DungeonUtility.CORRIDOR_FLOOR;
                        ctr++;
                    }
                }
            }
        }
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

        if(ctr < width * height * 0.1f) {
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
