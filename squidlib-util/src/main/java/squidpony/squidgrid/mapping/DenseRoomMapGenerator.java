package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.IRNG;
import squidpony.squidmath.RNG;

/**
 * Map generator that constructs a large number of overlapping rectangular rooms.
 * Meant for the kind of crowded architecture that might fit the dungeon equivalent of urban areas.
 * Likely to have many dead-end sections with only one door-like area, similar to hotel rooms or closets depending on
 * size, and the distance required to reach a particular room may be... cruel and/or unusual. Whole winding areas of a
 * large map may only be accessible from outside by one door, for instance.
 * <br>
 * An example of what this outputs:
 * https://gist.github.com/tommyettinger/3144b56a3a8e5bbe5ee401c1a93989f4
 * Created by Tommy Ettinger on 5/4/2016.
 */
public class DenseRoomMapGenerator implements IDungeonGenerator {
    public char[][] map;
    public int[][] environment;
    public IRNG rng;
    protected int width, height;
    public DenseRoomMapGenerator()
    {
        this(80, 30, new RNG());
    }
    public DenseRoomMapGenerator(int width, int height)
    {
        this(width, height, new RNG());
    }
    public DenseRoomMapGenerator(int width, int height, IRNG rng)
    {
        this.rng = rng;
        this.width = Math.max(3, width);
        this.height = Math.max(3, height);
        map = ArrayTools.fill('#', this.width, this.height);
        environment = new int[this.width][this.height];
    }

    public char[][] getDungeon() {
        return map;
    }
    /**
     * Generate a map as a 2D char array using the width and height specified in the constructor.
     * Should produce a crowded arrangement of rectangular rooms that overlap with each other.
     * @return a 2D char array for the map of densely-packed rectangular rooms.
     */
    public char[][] generate()
    {
        //ArrayList<short[]> regions = new ArrayList<>();
        short[] tempPacked;
        int nh, nw, nx, ny, hnw, hnh;
//        Collection<Coord> sampled = PoissonDisk.sampleRectangle(Coord.get(1, 1), Coord.get(width - 2, height - 2),
//                6f, width, height, 35, rng);
//        sampled.addAll(PoissonDisk.sampleRectangle(Coord.get(1, 1), Coord.get(width - 2, height - 2),
//                9, width, height, 40, rng));
        GreasedRegion sampled = new GreasedRegion(width, height).allOn().removeEdges();
        sampled.remake(sampled.copy().randomScatter(rng, 6).or(sampled.randomScatter(rng,8)));

        for(Coord center : sampled) {
            nw = rng.between(4, 16);
            nh = rng.between(4, 16);
            hnw = (nw + 1) / 2;
            hnh = (nh + 1) / 2;
            nx = Math.max(0, Math.min(width - 2 - hnw, center.x - hnw));
            ny = Math.max(0, Math.min(height - 2 - hnh, center.y - hnh));
            if (center.x - hnw != nx)
                nw -= Math.abs(center.x - hnw - nx);
            if (center.y - hnh != ny)
                nh -= Math.abs(center.y - hnh - ny);
            if (nw >= 0 && nh >= 0) {
                ArrayTools.insert(DungeonUtility.wallWrap(ArrayTools.fill('.', nw, nh)),
                        map, nx, ny);
                //regions.add(CoordPacker.rectangle(nx, ny, nw, nh));
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                environment[x][y] = (map[x][y] == '.') ? DungeonUtility.ROOM_FLOOR : DungeonUtility.ROOM_WALL;
            }
        }
//        tempPacked = CoordPacker.intersectPacked(
//                CoordPacker.rectangle(1, 1, width - 2, height - 2),
//                CoordPacker.pack(map, '#'));
//        Coord[] holes = CoordPacker.randomSeparated(tempPacked, 3, rng);
        for(Coord hole : new GreasedRegion(map, '.').fringe().removeEdges().mixedRandomSeparated(0.25, -1, rng.nextLong())) {
            if (((map[hole.x - 1][hole.y] == '.' && map[hole.x + 1][hole.y] == '.') ||
                            (map[hole.x][hole.y - 1] == '.' && map[hole.x][hole.y + 1] == '.'))) {
                map[hole.x][hole.y] = '.';
                environment[hole.x][hole.y] = DungeonUtility.CORRIDOR_FLOOR;
            }
        }

        /*
        regions = rng.shuffle(regions);
        while (regions.size() > 1)
        {

            region = regions.remove(0);
            linking = regions.get(0);
            start = CoordPacker.singleRandom(region, rng);
            end = CoordPacker.singleRandom(linking, rng);
            path = OrthoLine.line(start, end);
            for(Coord elem : path)
            {
                if(elem.x < width && elem.y < height) {
                    if (map[elem.x][elem.y] == '#') {
                        map[elem.x][elem.y] = '.';
                        environment[elem.x][elem.y] = MixedGenerator.CORRIDOR_FLOOR;
                        ctr++;
                    }
                }
            }
        }
        */

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
}
