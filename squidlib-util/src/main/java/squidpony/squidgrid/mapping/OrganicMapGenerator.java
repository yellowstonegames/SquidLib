package squidpony.squidgrid.mapping;

import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Map generator using Perlin/Simplex noise for the formation of "rooms" and then WobblyLine to connect with corridors.
 * Created by Tommy Ettinger on 4/18/2016.
 */
public class OrganicMapGenerator implements IDungeonGenerator {
    public char[][] map;
    public int[][] environment;
    public IRNG rng;
    protected int width, height;
    public double noiseMin, noiseMax;
    public OrganicMapGenerator()
    {
        this(0.55, 0.65, 80, 30, new RNG());
    }
    public OrganicMapGenerator(int width, int height)
    {
        this(0.55, 0.65, width, height, new RNG());
    }
    public OrganicMapGenerator(int width, int height, IRNG rng)
    {
        this(0.55, 0.65, width, height, rng);
    }
    public OrganicMapGenerator(double noiseMin, double noiseMax, int width, int height, IRNG rng)
    {
        CoordPacker.init();
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
        double shift, shift2, temp;
        boolean[][] working = new boolean[width][height], blocks = new boolean[8][8];
        int ctr = 0, frustration = 0;
        REDO:
        while (frustration < 10) {
            shift = rng.nextDouble(2048);
            shift2 = rng.between(4096, 8192);
            ctr = 0;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    map[x][y] = '#';
                    temp = (PerlinNoise.noise(x * 4.7 + shift, y * 4.7 + shift) * 5
                            + PerlinNoise.noise(x * 11.4 + shift2, y * 11.4 + shift2) * 3) / 8.0;
                    if (temp >= noiseMin && temp <= noiseMax) {
                        working[x][y] = true;
                        ctr++;
                        blocks[x * 8 / width][y * 8 / height] = true;
                    }
                    else
                    {
                        working[x][y] = false;
                    }
                }
            }
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    if (!blocks[x][y]) {
                        frustration++;
                        ctr = 0;
                        continue REDO;
                    }
                }
            }
            break;
        }
        if(ctr < (width + height) * 3 || frustration >= 10) {
            noiseMin = Math.min(0.9, Math.max(-1.0, noiseMin - 0.05));
            noiseMax = Math.min(1.0, Math.max(noiseMin + 0.05, noiseMax + 0.05));
            return generate();
        }
        ctr = 0;
        ArrayList<short[]> allRegions = CoordPacker.split(CoordPacker.pack(working)),
                regions = new ArrayList<>(allRegions.size());
        short[] region, linking, tempPacked;
        List<Coord> path, path2;
        Coord start, end;
        char[][] t;
        for (short[] r : allRegions) {
            if (CoordPacker.count(r) > 5) {
                region = CoordPacker.expand(r, 1, width, height, false);
                if(CoordPacker.isEmpty(region))
                    continue;
                regions.add(region);
                ctr += CoordPacker.count(region);
                tempPacked = CoordPacker.negatePacked(region);
                map = CoordPacker.mask(map, tempPacked, '.');
            }
        }
        int oldSize = regions.size();
        if(oldSize < 4 || ctr < (width + height) * 3) {
            noiseMin = Math.min(0.9, Math.max(-1.0, noiseMin - 0.05));
            noiseMax = Math.min(1.0, Math.max(noiseMin + 0.05, noiseMax + 0.05));
            return generate();
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                environment[x][y] = (map[x][y] == '.') ? DungeonUtility.CAVE_FLOOR : DungeonUtility.CAVE_WALL;
            }
        }
        //tempPacked = CoordPacker.pack(map, '.');
        //int tick = 1;
        regions = rng.shuffle(regions);
        while (regions.size() > 1)
        {

            region = regions.remove(0);
            /*
            tick = (tick + 1) % 5;
            if(tick == 0) {
                ctr -= CoordPacker.count(region);
                continue;
            }*/
            linking = regions.get(0);
            start = CoordPacker.singleRandom(region, rng);
            end = CoordPacker.singleRandom(linking, rng);
            path = WobblyLine.line(start.x, start.y, end.x, end.y, width, height, 0.75, rng);
            for(Coord elem : path)
            {
                if(elem.x < width && elem.y < height) {
                    if (map[elem.x][elem.y] == '#') {
                        map[elem.x][elem.y] = '.';
                        environment[elem.x][elem.y] = DungeonUtility.CORRIDOR_FLOOR;
                        ctr++;
                    } /*else if (rng.nextBoolean() &&
                            CoordPacker.queryPacked(CoordPacker.differencePacked(tempPacked, region), elem.x, elem.y)) {
                        linking = regions.get(rng.nextInt(regions.size()));
                        start = elem;
                        end = CoordPacker.singleRandom(linking, rng);
                        path2 = WobblyLine.line(start.x, start.y, end.x, end.y, width, height, 0.75, rng);
                        for(Coord elem2 : path2)
                        {
                            if(elem2.x < width && elem2.y < height) {
                                if (map[elem2.x][elem2.y] == '#') {
                                    map[elem2.x][elem2.y] = '.';
                                    environment[elem2.x][elem2.y] = MixedGenerator.CORRIDOR_FLOOR;
                                    ctr++;
                                }
                            }
                        }
                        break;
                    }*/
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

    public char[][] getDungeon() {
        return map;
    }

}
