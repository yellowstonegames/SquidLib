package squidpony.squidgrid.mapping;

import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.RNG;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private ArrayList<ArrayList<Coord>> linksUp,linksDown;
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
            throw new ExceptionInInitializerError("width and height must be greater than 2");
        if(depth < 1 || depth > 32)
            throw new ExceptionInInitializerError("depth must be between 1 and 32 inclusive");
        random = rng;
        this.width = width;
        this.height = height;
        this.depth = depth;
        long columnAlterations = random.nextLong(0x100000000L);
        float columnBase = width / (Long.bitCount(columnAlterations) + 32.0f);
        long rowAlterations = random.nextLong(0x100000000L);
        float rowBase = height / (Long.bitCount(rowAlterations) + 32.0f);

        columns = new int[32];
        rows = new int[32];
        linksUp = new ArrayList<ArrayList<Coord>>(depth);
        linksDown = new ArrayList<ArrayList<Coord>>(depth);
        for (int i = 0; i < depth; i++) {
            linksUp.add(new ArrayList<Coord>(80));
            linksDown.add(new ArrayList<Coord>(80));
        }
        int csum = 0, rsum = 0;
        long b = 1;
        for (int i = 0; i < 32; i++, b <<= 1) {
            columns[i] = csum + (int)(columnBase * 0.5f * (1 + Long.bitCount(columnAlterations & b)));
            csum += (int)(columnBase * (1 + Long.bitCount(columnAlterations & b)));
            rows[i] = rsum + (int)(rowBase * 0.5f * (1 + Long.bitCount(rowAlterations & b)));
            rsum += (int)(rowBase * (1 + Long.bitCount(rowAlterations & b)));
        }
        int cs = (width - csum);
        int rs = (height - rsum);
        int cs2 = cs, rs2 = rs, cs3 = cs, rs3 = rs;
        for (int i = 15; i >= 0; i--) {
            cs2= cs2 * i / 15;
            rs2 = rs2 * i / 15;
            columns[i] -= cs2;
            rows[i] -= rs2;
        }
        for (int i = 31; i >= 16; i--) {
            cs3 = cs3 * (i - 16) / 16;
            rs3 = rs3 * (i - 16) / 16;
            columns[i] += cs3;
            rows[i] += rs3;
        }

        List<LinkedHashMap<Coord, List<Coord>>> connections = new ArrayList<LinkedHashMap<Coord, List<Coord>>>(depth);
        for (int i = 0; i < depth; i++) {
            connections.add(new LinkedHashMap<Coord, List<Coord>>(80));
        }
        int m = random.nextInt(0x8000);
        int x = CoordPacker.getXMoore3D(m), y = CoordPacker.getYMoore3D(m), z = CoordPacker.getZMoore3D(m) * depth / 32, tz;
        int r = random.between(48, 96);
        m += r;
        for (int i = 0; i < 0x8000; r = random.between(48, 96), i += r, m = (m + r) % 0x8000) {
            tz = z;
            do {
                int tx = x, ty = y;
                List<Coord> cl = new ArrayList<Coord>(4);

                for (int j = 0;
                     j < 2;
                     j++) {
                    int x2 = random.between(Math.max(0, x - 3), x);
                    int x3 = random.between(x + 1, Math.min(x + 3, 31));
                    int y2 = random.between(Math.max(0, y - 3), y);
                    int y3 = random.between(y + 1, Math.min(y + 3, 31));
                    if (x3 < 32 && random.nextBoolean())
                        x2 = x3;
                    if (y3 < 32 && random.nextBoolean())
                        y2 = y3;
                    cl.add(Coord.get(columns[x2], rows[y2]));
                    if (Math.min(random.nextDouble(), random.nextDouble()) >= branchingChance)
                        break;
                }
                connections.get(tz).put(Coord.get(columns[tx], rows[ty]), cl);

                x = CoordPacker.getXMoore3D(m);
                y = CoordPacker.getYMoore3D(m);
                z = CoordPacker.getZMoore3D(m) * depth / 32;
                if(z != tz)
                    cl.clear();
                cl.add(Coord.get(columns[x], rows[y]));

                if (tz == z) {
                    connections.get(z).put(Coord.get(columns[tx], rows[ty]), cl);
                    break;
                }
                else {
                    if (z > tz) {
                        linksDown.get(tz).add(Coord.get(columns[x], rows[y]));
                        tz++;
                        linksUp.get(tz).add(Coord.get(columns[x], rows[y]));
                    }
                    else
                    {
                        linksUp.get(tz).add(Coord.get(columns[x], rows[y]));
                        tz--;
                        linksDown.get(tz).add(Coord.get(columns[x], rows[y]));
                    }
                }
            }while (true);
        }
        mix = new MixedGenerator[depth];
        for (int i = 0; i < depth; i++) {
            mix[i] = new MixedGenerator(width, height, random, connections.get(i), 0.6f);
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
     * This generates a new map by stretching a 32x32x32 grid of potential rooms to fit the width, height, and depth
     * passed to the constructor, randomly expanding columns and rows before contracting the whole to fit perfectly.
     * This uses the Moore Curve, a space-filling curve that loops around on itself, to guarantee that the rooms will
     * always have a long path through the dungeon, going up and down as well as north/south/east/west, that, if
     * followed completely, will take you back to your starting room. Some small branches are possible, and large rooms
     * may merge with other rooms nearby. This uses MixedGenerator.
     * @see MixedGenerator
     * @return a char[][][] where the outermost array is layers, then inside that are x and y in order (z x y)
     */
    public char[][][] generate()
    {
        char[][][] dungeon = new char[depth][][];
        for (int i = 0; i < depth; i++) {
            dungeon[i] = mix[i].generate();
        }
        for (int i = 0; i < depth; i++) {
            ArrayList<Coord> ups = linksUp.get(i), downs = linksDown.get(i);
            for(Coord up : ups)
            {
                int x = up.x + random.between(-2,2), y = up.y + random.between(-2, 2), frustration = 0;
                while ((x < 0 || y < 0 || x >= width || y >= height || dungeon[i][x][y] != '.')
                        && frustration < 40)
                {
                    x = up.x + random.between(-2, 2);
                    y = up.y + random.between(-2, 2);
                    frustration++;
                }
                if(frustration >= 40) {
                    dungeon[i][up.x][up.y] = '<';
                    if(up.x > 1)
                        dungeon[i][up.x-1][up.y] = '.';
                    if(up.y > 1)
                        dungeon[i][up.x][up.y-1] = '.';
                    if(up.x < width - 2)
                        dungeon[i][up.x+1][up.y] = '.';
                    if(up.x < height - 2)
                        dungeon[i][up.x][up.y+1] = '.';
                }else
                    dungeon[i][x][y] = '<';
            }
            for(Coord down : downs)
            {
                int x = down.x + random.between(-2,2), y = down.y + random.between(-2, 2), frustration = 0;
                while ((x < 0 || y < 0 || x >= width || y >= height || dungeon[i][x][y] != '.')
                        && frustration < 40)
                {
                    x = down.x + random.between(-2, 2);
                    y = down.y + random.between(-2, 2);
                    frustration++;
                }
                if(frustration >= 40) {
                    dungeon[i][down.x][down.y] = '>';

                    if(down.x > 1)
                        dungeon[i][down.x-1][down.y] = '.';
                    if(down.y > 1)
                        dungeon[i][down.x][down.y-1] = '.';
                    if(down.x < width - 2)
                        dungeon[i][down.x+1][down.y] = '.';
                    if(down.x < height - 2)
                        dungeon[i][down.x][down.y+1] = '.';
                }
                else
                    dungeon[i][x][y] = '>';
            }
        }
        return dungeon;
    }
}
