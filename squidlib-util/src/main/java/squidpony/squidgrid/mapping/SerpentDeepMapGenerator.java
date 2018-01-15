package squidpony.squidgrid.mapping;

import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Generate dungeons based on a random, winding, looping path through 3D space, requiring a character to move up and
 * down as well as north/south/east/west to get through the dungeon. Uses techniques from MixedGenerator.
 * Uses a Moore Curve, which is related to Hilbert Curves but loops back to its starting point, and stretches and
 * distorts the grid to make sure a visual correlation isn't obvious.
 * <br>
 * The name comes from a vivid dream I had about gigantic, multi-colored snakes that completely occupied a roguelike
 * dungeon. Shortly after, I made the connection to the Australian mythology I'd heard about the Rainbow Serpent, which
 * in some stories dug water-holes and was similarly gigantic.
 * Created by Tommy Ettinger on 10/24/2015.
 */
public class SerpentDeepMapGenerator {
    private MixedGenerator[] mix;
    private int[] columns, rows;
    private int width, height, depth;
    private ArrayList<OrderedSet<Coord>> linksUp,linksDown;
    private RNG random;

    /**
     * This prepares a map generator that will generate a map with the given width, height and depth, using the given
     * RNG. The intended purpose is to carve a long path that loops through the whole dungeon's 3D space, while
     * hopefully maximizing the amount of rooms the player encounters. You call the different carver-adding methods to
     * affect what the dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(),
     * defaulting to only caves if none are called. You call generate() after adding carvers, which returns a char[][][]
     * for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param depth the number of levels deep to create
     * @param rng an RNG object to use for random choices; this make a lot of random choices.
     * @see MixedGenerator
     */
    public SerpentDeepMapGenerator(int width, int height, int depth, RNG rng) {
        this(width, height, depth, rng, 0.3);
    }
    /**
     * This prepares a map generator that will generate a map with the given width, height and depth, using the given
     * RNG, and will branch out to other nearby rooms that (probably) do not have staircases between layers.
     * The intended purpose is to carve a long path that loops through the whole dungeon's 3D space, while
     * hopefully maximizing the amount of rooms the player encounters. You call the different carver-adding methods to
     * affect what the dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(),
     * defaulting to only caves if none are called. You call generate() after adding carvers, which returns a char[][][]
     * for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param depth the number of levels deep to create
     * @param rng an RNG object to use for random choices; this make a lot of random choices.
     * @param branchingChance the odds from 0.0 to 1.0 that a branch will be created near each necessary room.
     * @see MixedGenerator
     */
    public SerpentDeepMapGenerator(int width, int height, int depth, RNG rng, double branchingChance)
    {
        if(width <= 2 || height <= 2)
            throw new IllegalArgumentException("width and height must be greater than 2");
        if(depth < 1)
            throw new IllegalArgumentException("depth must be at least 1");
        CoordPacker.init();
        random = rng;
        this.width = width;
        this.height = height;
        this.depth = depth;
        int numLayers = (int)Math.ceil(depth / 4.0f);
        long columnAlterations = random.nextLong(0x100000000L);
        float columnBase = width / (Long.bitCount(columnAlterations) + 16.0f);
        long rowAlterations = random.nextLong(0x100000000L);
        float rowBase = height / (Long.bitCount(rowAlterations) + 16.0f);

        columns = new int[16];
        rows = new int[16];
        linksUp = new ArrayList<>(depth);
        linksDown = new ArrayList<>(depth);
        for (int i = 0; i < depth; i++) {
            linksUp.add(new OrderedSet<Coord>(80));
            linksDown.add(new OrderedSet<Coord>(80));
        }
        int csum = 0, rsum = 0;
        long b = 3;
        for (int i = 0; i < 16; i++, b <<= 2) {
            columns[i] = csum + (int)(columnBase * 0.5f * (1 + Long.bitCount(columnAlterations & b)));
            csum += (int)(columnBase * (1 + Long.bitCount(columnAlterations & b)));
            rows[i] = rsum + (int)(rowBase * 0.5f * (1 + Long.bitCount(rowAlterations & b)));
            rsum += (int)(rowBase * (1 + Long.bitCount(rowAlterations & b)));
        }
        int cs = width - csum;
        int rs = height - rsum;
        int cs2 = cs, rs2 = rs, cs3 = cs, rs3 = rs;
        for (int i = 0; i <= 7; i++) {
            cs2= cs2 * i / 7;
            rs2 = rs2 * i / 7;
            columns[i] -= cs2;
            rows[i] -= rs2;
        }
        for (int i = 15; i >= 8; i--) {
            cs3 = cs3 * (i - 8) / 8;
            rs3 = rs3 * (i - 8) / 8;
            columns[i] += cs3;
            rows[i] += rs3;
        }

        List<OrderedMap<Coord, List<Coord>>> connections = new ArrayList<>(depth);
        for (int i = 0; i < depth; i++) {
            connections.add(new OrderedMap<Coord, List<Coord>>(80));
        }
        int m = random.nextInt(0x800 * numLayers);
        int x = CoordPacker.getXMoore3D(m, numLayers), y = CoordPacker.getYMoore3D(m, numLayers),
                z = (int)Math.floor(CoordPacker.getZMoore3D(m, numLayers) * depth / (8f * numLayers)),
                sx = x, sy = y, sz = z, tz = z;
        int r = random.between(12, 33);
        m += r;
        for (int i = 0; i < 0x800 * numLayers; r = random.between(12, 33), i += r, m = (m + r) % (0x800 * numLayers)) {
            tz = z;
            int tx = x, ty = y;
            do {
                List<Coord> cl = new ArrayList<>(4);

                for (int j = 0;
                     j < 2;
                     j++) {
                    int x2 = random.between(Math.max(0, tx - 2), tx);
                    int x3 = random.between(tx + 1, Math.min(tx + 3, 15));
                    int y2 = random.between(Math.max(0, ty - 2), ty);
                    int y3 = random.between(ty + 1, Math.min(ty + 3, 15));
                    if (x3 < 16 && random.nextBoolean())
                        x2 = x3;
                    if (y3 < 16 && random.nextBoolean())
                        y2 = y3;
                    cl.add(Coord.get(columns[x2], rows[y2]));
                    if (random.nextDouble() >= branchingChance)
                        break;
                }

                List<Coord> connect = connections.get(tz).get(Coord.get(columns[tx], rows[ty]));
                if(connect != null)
                    connections.get(tz).get(Coord.get(columns[tx], rows[ty])).addAll(cl);
                else
                    connections.get(tz).put(Coord.get(columns[tx], rows[ty]), new ArrayList<>(cl));

                x = CoordPacker.getXMoore3D(m, numLayers);
                y = CoordPacker.getYMoore3D(m, numLayers);
                z = (int)Math.floor(CoordPacker.getZMoore3D(m, numLayers) * depth / (8f * numLayers));
                if(z != tz)
                    cl.clear();
                cl.add(Coord.get(columns[x], rows[y]));

                if (tz == z) {
                    List<Coord> conn = connections.get(z).get(Coord.get(columns[tx], rows[ty]));
                    if(conn != null)
                        connections.get(z).get(Coord.get(columns[tx], rows[ty])).addAll(cl);
                    else
                        connections.get(z).put(Coord.get(columns[tx], rows[ty]), new ArrayList<>(cl));
                    break;
                }
                else {
                    if (z > tz) {
                        linksDown.get(tz).add(Coord.get(tx, ty));
                        tz++;
                        linksUp.get(tz).add(Coord.get(tx, ty));
                    }
                    else
                    {
                        linksUp.get(tz).add(Coord.get(tx, ty));
                        tz--;
                        linksDown.get(tz).add(Coord.get(tx, ty));
                    }
                }
            }while (true);
        }

        do {
            List<Coord> cl = new ArrayList<>(4);

            for (int j = 0;
                 j < 2;
                 j++) {
                int x2 = random.between(Math.max(0, x - 2), x);
                int x3 = random.between(x + 1, Math.min(x + 3, 15));
                int y2 = random.between(Math.max(0, y - 2), y);
                int y3 = random.between(y + 1, Math.min(y + 3, 15));
                if (x3 < 16 && random.nextBoolean())
                    x2 = x3;
                if (y3 < 16 && random.nextBoolean())
                    y2 = y3;
                cl.add(Coord.get(columns[x2], rows[y2]));
                if (Math.min(random.nextDouble(), random.nextDouble()) >= branchingChance)
                    break;
            }

            List<Coord> connect = connections.get(tz).get(Coord.get(columns[x], rows[y]));
            if(connect != null)
                connections.get(tz).get(Coord.get(columns[x], rows[y])).addAll(cl);
            else
                connections.get(tz).put(Coord.get(columns[x], rows[y]), new ArrayList<>(cl));

            if(sz != tz)
                cl.clear();
            cl.add(Coord.get(columns[x], rows[y]));

            if (tz == sz) {
                connections.get(sz).get(Coord.get(columns[x], rows[y])).add(
                        Coord.get(columns[sx], rows[sy]));
                break;
            }
            else {
                if (sz > tz) {
                    linksDown.get(tz).add(Coord.get(x, y));
                    tz++;
                    linksUp.get(tz).add(Coord.get(x, y));
                }
                else
                {
                    linksUp.get(tz).add(Coord.get(x, y));
                    tz--;
                    linksDown.get(tz).add(Coord.get(x, y));
                }
            }
        }while (true);

        mix = new MixedGenerator[depth];
        for (int i = 0; i < depth; i++) {
            mix[i] = new MixedGenerator(width, height, random, connections.get(i), 0.35f);
        }
    }
    /**
     * Changes the number of "carvers" that will create caves from one room to the next. If count is 0 or less, no caves
     * will be made. If count is at least 1, caves are possible, and higher numbers relative to the other carvers make
     * caves more likely. Carvers are shuffled when used, then repeat if exhausted during generation. Since typically
     * about 30-40 rooms are carved, large totals for carver count aren't really needed; aiming for a total of 10
     * between the count of putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers() is reasonable.
     * @see MixedGenerator
     * @param count the number of carvers making caves between rooms; only matters in relation to other carvers
     */
    public void putCaveCarvers(int count)
    {
        for (int i = 0; i < depth; i++) {
            mix[i].putCaveCarvers(count);
        }
    }
    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a box shape at the start and end, and a small room at the corner if there is one. If count
     * is 0 or less, no box-shaped rooms will be made. If count is at least 1, box-shaped rooms are possible, and higher
     * numbers relative to the other carvers make box-shaped rooms more likely. Carvers are shuffled when used, then
     * repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * and putRoundRoomCarvers() is reasonable.
     * @see MixedGenerator
     * @param count the number of carvers making box-shaped rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putBoxRoomCarvers(int count)
    {
        for (int i = 0; i < depth; i++) {
            mix[i].putBoxRoomCarvers(count);
        }
    }
    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a box shape at the start and end, and a small room at the corner if there is one. This also
     * ensures walls will be placed around the room, only allowing corridors and small cave openings to pass. If count
     * is 0 or less, no box-shaped rooms will be made. If count is at least 1, box-shaped rooms are possible, and higher
     * numbers relative to the other carvers make box-shaped rooms more likely. Carvers are shuffled when used, then
     * repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * and putRoundRoomCarvers() is reasonable.
     * @see MixedGenerator
     * @param count the number of carvers making box-shaped rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putWalledBoxRoomCarvers(int count)
    {
        for (int i = 0; i < depth; i++) {
            mix[i].putWalledBoxRoomCarvers(count);
        }
    }
    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a circle shape at the start and end, and a small circular room at the corner if there is
     * one. If count is 0 or less, no circular rooms will be made. If count is at least 1, circular rooms are possible,
     * and higher numbers relative to the other carvers make circular rooms more likely. Carvers are shuffled when used,
     * then repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * and putRoundRoomCarvers() is reasonable.
     * @see MixedGenerator
     * @param count the number of carvers making circular rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putRoundRoomCarvers(int count)
    {
        for (int i = 0; i < depth; i++) {
            mix[i].putRoundRoomCarvers(count);
        }
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a circle shape at the start and end, and a small circular room at the corner if there is
     * one. This also ensures walls will be placed around the room, only allowing corridors and small cave openings to
     * pass. If count is 0 or less, no circular rooms will be made. If count is at least 1, circular rooms are possible,
     * and higher numbers relative to the other carvers make circular rooms more likely. Carvers are shuffled when used,
     * then repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * and putRoundRoomCarvers() is reasonable.
     * @see MixedGenerator
     * @param count the number of carvers making circular rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putWalledRoundRoomCarvers(int count)
    {
        for (int i = 0; i < depth; i++) {
            mix[i].putWalledRoundRoomCarvers(count);
        }
    }

    /**
     * This generates a new map by stretching a 32x32x(multiple of 8) grid of potential rooms to fit the width, height,
     * and depth passed to the constructor, randomly expanding columns and rows before contracting the whole to fit
     * perfectly. This uses the Moore Curve, a space-filling curve that loops around on itself, to guarantee that the
     * rooms will always have a long path through the dungeon, going up and down as well as north/south/east/west, that,
     * if followed completely, will take you back to your starting room. Some small branches are possible, and large
     * rooms may merge with other rooms nearby. This uses MixedGenerator.
     * @see MixedGenerator
     * @return a char[][][] where the outermost array is layers, then inside that are x and y in order (z x y)
     */
    public char[][][] generate()
    {
        char[][][] dungeon = new char[depth][][];
        short[][] floors = new short[depth][];
        int dlimit = (height + width) / 3;
        for (int i = 0; i < depth; i++) {
            dungeon[i] = mix[i].generate();
            floors[i] = CoordPacker.pack(dungeon[i], '.');
        }
        //using actual dungeon space per layer, not row/column 3D grid space
        ArrayList<OrderedSet<Coord>> ups = new ArrayList<>(depth),
                downs = new ArrayList<>(depth);
        for (int i = 0; i < depth; i++) {
            ups.add(new OrderedSet<Coord>(40));
            downs.add(new OrderedSet<Coord>(40));
            OrderedSet<Coord> above = null;
            if (i > 0) {
                above = new OrderedSet<>(linksDown.get(i - 1));
                if(above.size() == 0)
                    continue;
                Coord higher = above.randomItem(random);//random.getRandomElement(above.toArray(new Coord[above.size()]));
                while(above.size() > 0)
                {
                    short[] nearAbove = CoordPacker.flood(floors[i - 1],
                            CoordPacker.packOne(columns[higher.x], rows[higher.y]),
                            dlimit);
                    short[] near = CoordPacker.intersectPacked(nearAbove, CoordPacker.flood(floors[i],
                            CoordPacker.packOne(columns[higher.x], rows[higher.y]),
                            dlimit));
                    ArrayList<Coord> subLinks = CoordPacker.randomPortion(near, 1, random);
                    ups.get(i).addAll(subLinks);
                    downs.get(i-1).addAll(subLinks);
                    for(Coord abv : linksDown.get(i-1))
                    {
                        if(CoordPacker.queryPacked(nearAbove, columns[abv.x], rows[abv.y])) //scannedAbove[columns[abv.x]][rows[abv.y]] <= dlimit
                            above.remove(abv);
                    }
                    if(above.isEmpty())
                        break;
                    higher = above.randomItem(random);//random.getRandomElement(above.toArray(new Coord[above.size()]));
                }
            }
        }

        for (int i = 0; i < depth; i++) {
            OrderedMap<Coord, Integer> used = new OrderedMap<>(128);
            for(Coord up : ups.get(i))
            {
                Integer count = used.get(up);
                if(count != null && count > 1)
                    continue;
                dungeon[i][up.x][up.y] = '<';

                used.put(up, (count == null) ? 1 : count + 1);
            }
            used.clear();
            for(Coord down : downs.get(i))
            {
                Integer count = used.get(down);
                if(count != null && count > 1)
                    continue;
                dungeon[i][down.x][down.y] = '>';

                used.put(down, (count == null) ? 1 : count + 1);
            }
        }
        return dungeon;
    }

    /**
     * Gets an array (length equals depth) of 2D int arrays representing the environments for levels.
     * @return an array of 2D int arrays, where each 2D array is a level's environment
     */
    public int[][][] getEnvironments()
    {
        int[][][] env = new int[depth][][];
        for (int i = 0; i < depth; i++) {
            env[i] = mix[i].getEnvironment();
        }
        return env;
    }

    /**
     * Gets a 2D int array representing the environment for the requested level.
     * @param level the level to get from the generated dungeon; will be clamped between 0 and depth - 1
     * @return a 2D int array representing the requested level's environment
     */
    public int[][] getEnvironment(int level)
    {
        return mix[Math.max(0, Math.min(depth - 1, level))].getEnvironment();
    }
}
