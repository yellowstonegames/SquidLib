package squidpony.squidgrid.mapping;

import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Generate dungeons with between 1 and 3 primary "lanes" going from the upper left "base" to the bottom right "base"
 * (and vice versa, since this is symmetrical). Also fills the area not covered by lanes with "jungle" (random, but
 * symmetrical, room or cave connections). Dungeons are produced by MixedGenerator, like those SerpentMapGenerator
 * makes, but include the wide lanes going from corner to corner. You can call different methods like putCaveCarvers(),
 * putBoxRoomCarvers(), putWalledRoundRoomCarvers(), etc. to affect the "jungle", which defaults to caves unless one or
 * more of the putXXXCarvers methods was called. The lanes are always 5 floor cells wide, measured 8-way. This supports
 * the getEnvironment() method, which can be used in conjunction with RoomFinder to find where separate room, corridor,
 * and cave areas have been placed.
 * <br>
 * A preview can be seen here https://gist.github.com/tommyettinger/4f57cff23eead11b17bf , with dungeons created with
 * one, two, and three lanes, and only using box-shaped rooms for "jungle." Currently, the two-lane dungeon seems to be
 * ideal for maps that aren't incredibly large; the samples are 80x80, but larger maps may have better jungle layout
 * with three lanes than those three-lane maps can manage on smaller sizes. Another potential advantage of the two-lane
 * approach is that it can be used to generate a "ring" of wide paths around a central "core" of jungle, which wasn't
 * originally intended as a use of this generator but could be very useful for games that, for instance, want guards
 * patrolling an obvious ring, while the player, monsters, and/or other prisoners start in the jungle.
 * Created by Tommy Ettinger on 10/24/2015.
 */
public class LanesMapGenerator implements IDungeonGenerator {
    protected SymmetryDungeonGenerator mix;
    protected int[] columns, rows;
    protected IRNG random;
    protected int lanes;
    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * The dungeon will have the specified number of wide lanes going from upper left to lower right, possibly taking a
     * longer path to approach the other corners.  You call the different carver-adding methods to affect what the
     * non-lane portion of the dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(),
     * defaulting to only caves if none are called. You call generate() after adding carvers, which returns a char[][]
     * for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng an RNG object to use for random choices; this make a lot of random choices.
     * @param lanes between 1 and 3; the number of wide paths to generate going from upper left to lower right.
     * @see MixedGenerator
     */
    public LanesMapGenerator(int width, int height, IRNG rng, int lanes)
    {
        if(width <= 8 || height <= 8)
            throw new IllegalArgumentException("width and height must be greater than 8");
        CoordPacker.init();
        this.lanes = (lanes < 1 || lanes > 3) ? 1 : lanes;
        random = rng;
        /*
        long columnAlterations = random.nextLong();
        float columnBase = width / (Long.bitCount(columnAlterations) + 16.0f);
        long rowAlterations = random.nextLong();
        float rowBase = height / (Long.bitCount(rowAlterations) + 16.0f);

        columns = new int[32];
        rows = new int[32];
        int csum = 0, rsum = 0;
        long b = 3;
        for (int i = 0; i < 32; i++, b <<= 2) {
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
        for (int i = 15; i >= 16; i--) {
            cs3 = cs3 * (i - 16) / 16;
            rs3 = rs3 * (i - 16) / 16;
            columns[i] += cs3;
            rows[i] += rs3;
        }
        */
        long columnAlterations = random.nextLong(0x1000000000000L);
        float columnBase = width / (Long.bitCount(columnAlterations) + 48.0f);
        long rowAlterations = random.nextLong(0x1000000000000L);
        float rowBase = height / (Long.bitCount(rowAlterations) + 48.0f);

        columns = new int[16];
        rows = new int[16];
        int csum = 0, rsum = 0;
        long b = 7;
        for (int i = 0; i < 16; i++, b <<= 3) {
            columns[i] = csum + (int)(columnBase * 0.5f * (3 + Long.bitCount(columnAlterations & b)));
            csum += (int)(columnBase * (3 + Long.bitCount(columnAlterations & b)));
            rows[i] = rsum + (int)(rowBase * 0.5f * (3 + Long.bitCount(rowAlterations & b)));
            rsum += (int)(rowBase * (3 + Long.bitCount(rowAlterations & b)));
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


        OrderedMap<Coord, List<Coord>> connections = new OrderedMap<>(80);
        Coord temp, t;
        int m = random.nextInt(32), r = random.between(8, 24);
        temp = CoordPacker.hilbertToCoord(m);
        Coord starter = CoordPacker.hilbertToCoord(m);
        m += r;
        for (int i = r; i < 256 && m < 256 - 9; i += r, m += r) {
            List<Coord> cl = new ArrayList<>(4);
            cl.add(Coord.get(columns[temp.x], rows[temp.y]));
            temp = CoordPacker.hilbertToCoord(m);
            r = random.between(8, 24);
            for (int j = 0, p = r - 1;
                 j < 3 && p > 2 && Math.min(random.nextDouble(), random.nextDouble()) < 0.2;
                 j++, p -= random.between(1, p)) {
                t = CoordPacker.hilbertToCoord(m + p);
                cl.add(Coord.get(columns[t.x], rows[t.y]));
            }
            connections.put(Coord.get(columns[temp.x], rows[temp.y]), cl);
        }
        connections.get(Coord.get(columns[temp.x], rows[temp.y])).add(
                Coord.get(columns[starter.x], rows[starter.y]));
        mix = new SymmetryDungeonGenerator(width, height, random, connections, 0.6f);
        boolean[][] fixed = new boolean[width][height];

        if(lanes != 2)
        {
            List<Coord> path = DDALine.line(3, 3, width - 4, height - 4);
            for(Coord c : path)
            {
                for (int x = c.x - 2; x <= c.x + 2; x++) {
                    for (int y = c.y - 2; y <= c.y + 2; y++) {
                        fixed[x][y] = true;
                    }
                }
            }
        }
        if(lanes > 1)
        {
            List<Coord> path = DDALine.line(3, 3, 3, height - 4);
            path.addAll(DDALine.line(3, 3, width - 4, 3));
            for(Coord c : path)
            {
                for (int x = c.x - 2; x <= c.x + 2; x++) {
                    for (int y = c.y - 2; y <= c.y + 2; y++) {
                        fixed[x][y] = true;
                    }
                }
            }
        }
        mix.setFixedRooms(fixed);
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
        mix.putCaveCarvers(count);
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
        mix.putBoxRoomCarvers(count);
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
        mix.putWalledBoxRoomCarvers(count);
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
        mix.putRoundRoomCarvers(count);
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
        mix.putWalledRoundRoomCarvers(count);
    }

    /**
     * This generates a new map by stretching a 16x16 grid of potential rooms to fit the width and height passed to the
     * constructor, randomly expanding columns and rows before contracting the whole to fit perfectly. This uses the
     * Moore Curve, a space-filling curve that loops around on itself, to guarantee that the rooms will always have a
     * long path through the dungeon that, if followed completely, will take you back to your starting room. Some small
     * branches are possible, and large rooms may merge with other rooms nearby. This uses MixedGenerator.
     * @see MixedGenerator
     * @return a char[][] where '#' is a wall and '.' is a floor or corridor; x first y second
     */
    public char[][] generate()
    {
        return mix.generate();
    }

    /**
     * Gets a 2D array of int constants, each representing a type of environment corresponding to a static field of
     * MixedGenerator. This array will have the same size as the last char 2D array prduced by generate(), and the value
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
        return mix.getEnvironment();
    }

    public char[][] getDungeon() {
        return mix.getDungeon();
    }
}
