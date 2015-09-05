package squidpony.squidgrid.mapping;

import squidpony.annotation.Beta;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.mapping.styled.DungeonBoneGen;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;
import squidpony.squidgrid.Spill;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * The primary way to create a more-complete dungeon, layering different effects and modifications on top of
 * a DungeonBoneGen's dungeon.
 *
 * @see squidpony.squidgrid.mapping.DungeonUtility
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
@Beta
public class DungeonGenerator {

    /**
     * The effects that can be applied to this dungeon. More may be added in future releases.
     */
    public enum FillEffect
    {
        /**
         * Water, represented by '~'
         */
        WATER,
        /**
         * Doors, represented by '+' for east-to-west connections or '/' for north-to-south ones.
         */
        DOORS,
        /**
         * Traps, represented by '^'
         */
        TRAPS
    }

    /**
     * The effects that will be applied when generate is called. Strongly prefer using addWater, addDoors, and addTraps.
     */
    public HashMap<FillEffect, Integer> fx;
    private DungeonBoneGen gen;
    private int height, width;
    public RNG rng;

    private char[][] dungeon = null;

    /**
     * Get the most recently generated char[][] dungeon out of this class, which may be null if none have been
     * generated with the method generate and setDungeon has not been called.
     * @return a char[][] dungeon, or null.
     */
    public char[][] getDungeon() {
        return dungeon;
    }

    /**
     * Change the underlying char[][]; only affects the toString method, and of course getDungeon.
     * @param dungeon a char[][], probably produced by an earlier call to this class and then modified.
     */
    public void setDungeon(char[][] dungeon) {
        this.dungeon = dungeon;
        if(dungeon == null)
        {
            width = 0;
            height = 0;
            return;
        }
        width = dungeon.length;
        if(width > 0)
            height = dungeon[0].length;
    }

    /**
     * Height of the dungeon in cells.
     * @return Height of the dungeon in cells.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Width of the dungeon in cells.
     * @return Width of the dungeon in cells.
     */
    public int getWidth() {
        return width;
    }


    /**
     * Make a DungeonGenerator with a LightRNG using a random seed, height 40, and width 40.
     */
    public DungeonGenerator()
    {
        rng = new RNG(new LightRNG());
        gen = new DungeonBoneGen(rng);
        height = 40;
        width = 40;
        fx = new HashMap<FillEffect, Integer>();
    }

    /**
     * Make a DungeonGenerator with the given height and width, and the RNG used for generating a dungeon and
     * adding features will be a LightRNG using a random seed.
     * @param width The width of the dungeon in cells.
     * @param height The height of the dungeon in cells.
     */
    public DungeonGenerator(int width, int height)
    {
        rng = new RNG(new LightRNG());
        gen = new DungeonBoneGen(rng);
        this.height = height;
        this.width = width;
        fx = new HashMap<FillEffect, Integer>();
    }

    /**
     * Make a DungeonGenerator with the given height, width, and RNG. Use this if you want to seed the RNG.
     * @param width The width of the dungeon in cells.
     * @param height The height of the dungeon in cells.
     * @param rng The RNG to use for all purposes in this class; if this has been seeded and you want the same
     *            results from map generation every time, don't use the same RNG object in other places.
     */
    public DungeonGenerator(int width, int height, RNG rng)
    {
        this.rng = rng;
        gen = new DungeonBoneGen(rng);
        this.height = height;
        this.width = width;
        fx = new HashMap<FillEffect, Integer>();
    }

    /**
     * Copies all fields from copying and makes a new DungeonGenerator.
     * @param copying
     */
    public DungeonGenerator(DungeonGenerator copying)
    {
        rng = new RNG(copying.rng.getRandomness());
        gen = new DungeonBoneGen(rng);
        height = copying.height;
        width = copying.width;
        fx = new HashMap<FillEffect, Integer>(copying.fx);
        dungeon = copying.dungeon;
    }

    /**
     * Turns the given percentage of floor cells into water cells, represented by '~'. Water will be clustered into
     * a random number of pools, with more appearing if needed to fill the percentage. Each pool will have randomized
     * volume that should fill or get very close to filling the requested percentage, unless the pools encounter too
     * much tight space. If this DungeonGenerator previously had addWater called, the latest call will take precedence.
     * @param percentage the percentage of floor cells to fill with water; this can vary quite a lot. It may be
     *                   difficult to fill very high (approaching 100) percentages with water, though it will succeed.
     * @return this DungeonGenerator; can be chained
     */
    public DungeonGenerator addWater(int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        if(fx.containsKey(FillEffect.WATER)) fx.remove(FillEffect.WATER);
        fx.put(FillEffect.WATER, percentage);
        return this;
    }
    /**
     * Turns the given percentage of viable doorways into doors, represented by '+' for doors that allow travel along
     * the x-axis and '/' for doors that allow travel along the y-axis. If doubleDoors is true,
     * 2-cell-wide openings will be considered viable doorways and will fill one cell with a wall, the other a door.
     * If this DungeonGenerator previously had addDoors called, the latest call will take precedence.
     * @param percentage the percentage of valid openings to corridors to fill with doors; should be between 10 and
     *                   20 if you want doors to appear more than a few times, but not fill every possible opening.
     * @param doubleDoors true if you want two-cell-wide openings to receive a door and a wall; false if only
     *                    one-cell-wide openings should receive doors. Usually, this should be true.
     * @return this DungeonGenerator; can be chained
     */
    public DungeonGenerator addDoors(int percentage, boolean doubleDoors)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        if(doubleDoors) percentage *= -1;
        if(fx.containsKey(FillEffect.DOORS)) fx.remove(FillEffect.DOORS);
        fx.put(FillEffect.DOORS, percentage);
        return this;
    }

    /**
     * Turns the given percentage of open area floor cells into trap cells, represented by '^'. Corridors that have no
     * possible way to move around a trap will not receive traps, ever. If this DungeonGenerator previously had
     * addTraps called, the latest call will take precedence.
     * @param percentage the percentage of valid cells to fill with traps; should be no higher than 5 unless
     *                   the dungeon floor is meant to be a kill screen or minefield.
     * @return this DungeonGenerator; can be chained
     */
    public DungeonGenerator addTraps(int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        if(fx.containsKey(FillEffect.TRAPS)) fx.remove(FillEffect.TRAPS);
        fx.put(FillEffect.TRAPS, percentage);
        return this;
    }

    /**
     * Removes any door, water, or trap insertion effects that this DungeonGenerator would put in future dungeons.
     * @return this DungeonGenerator, with all effects removed. Can be chained.
     */
    public DungeonGenerator clearEffects()
    {
        fx.clear();
        return this;
    }

    private LinkedHashSet<Point> removeAdjacent(LinkedHashSet<Point> coll, Point pt)
    {
        for(Point temp : new Point[]{new Point(pt.x+1, pt.y), new Point(pt.x-1, pt.y),
                new Point(pt.x, pt.y+1), new Point(pt.x, pt.y-1)})
        {
            if(coll.contains(temp) && !(temp.x == pt.x && temp.y == pt.y))
                coll.remove(temp);
        }

        return coll;
    }
    private LinkedHashSet<Point> removeAdjacent(LinkedHashSet<Point> coll, Point pt1, Point pt2)
    {

        for(Point temp : new Point[]{new Point(pt1.x+1, pt1.y), new Point(pt1.x-1, pt1.y),
                new Point(pt1.x, pt1.y+1), new Point(pt1.x, pt1.y-1),
                new Point(pt2.x+1, pt2.y), new Point(pt2.x-1, pt2.y),
                new Point(pt2.x, pt2.y+1), new Point(pt2.x, pt2.y-1),})
        {
            if(coll.contains(temp) && !(temp.x == pt1.x && temp.y == pt1.y) && !(temp.x == pt2.x && temp.y == pt2.y))
                coll.remove(temp);
        }

        return coll;
    }

    private LinkedHashSet<Point> viableDoorways(boolean doubleDoors, char[][] map)
    {
        LinkedHashSet<Point> doors = new LinkedHashSet<Point>();
        Point temp = new Point(0, 0);

        for(int x = 1; x < map.length - 1; x++, temp.x = x) {
            for (int y = 1; y < map[x].length - 1; y++, temp.y = y) {
                if(map[x][y] == '#')
                    continue;
                if (doubleDoors) {
                    if (x >= map.length - 2 || y >= map[x].length - 2)
                        continue;
                    else {
                        if (map[x+1][y] != '#' &&
                                map[x + 2][y] == '#' && map[x - 1][y] == '#'
                                && map[x][y + 1] != '#' && map[x][y - 1] != '#'
                                && map[x+1][y + 1] != '#' && map[x+1][y - 1] != '#') {
                            if (map[x + 2][y + 1] != '#' || map[x - 1][y + 1] != '#' || map[x + 2][y - 1] != '#' || map[x - 1][y - 1] != '#') {
                                doors.add(new Point(x, y));
                                doors.add(new Point(x + 1, y));
                                doors = removeAdjacent(doors, new Point(x, y), new Point(x + 1, y));
                                continue;
                            }
                        } else if (map[x][y+1] != '#' &&
                                map[x][y + 2] == '#' && map[x][y - 1] == '#'
                                && map[x + 1][y] != '#' && map[x - 1][y] != '#'
                                && map[x + 1][y+1] != '#' && map[x - 1][y+1] != '#') {
                            if (map[x + 1][y + 2] != '#' || map[x + 1][y - 1] != '#' || map[x - 1][y + 2] != '#' || map[x - 1][y - 1] != '#') {
                                doors.add(new Point(x, y));
                                doors.add(new Point(x, y+1));
                                doors = removeAdjacent(doors, new Point(x, y), new Point(x, y+1));
                                continue;
                            }
                        }
                    }
                }
                if (map[x + 1][y] == '#' && map[x - 1][y] == '#' && map[x][y + 1] != '#' && map[x][y - 1] != '#') {
                    if (map[x + 1][y + 1] != '#' || map[x - 1][y + 1] != '#' || map[x + 1][y - 1] != '#' || map[x - 1][y - 1] != '#') {
                        doors.add(new Point(x, y));
                        doors = removeAdjacent(doors, new Point(x, y));
                    }
                } else if (map[x][y + 1] == '#' && map[x][y - 1] == '#' && map[x + 1][y] != '#' && map[x - 1][y] != '#') {
                    if (map[x + 1][y + 1] != '#' || map[x + 1][y - 1] != '#' || map[x - 1][y + 1] != '#' || map[x - 1][y - 1] != '#') {
                        doors.add(new Point(x, y));
                        doors = removeAdjacent(doors, new Point(x, y));
                    }
                }

            }
        }


        return doors;
    }

    /**
     * Generate a char[][] dungeon using TilesetType.DEFAULT_DUNGEON; this produces a dungeon appropriate for a level
     * of ruins or a partially constructed dungeon. This uses '#' for walls, '.' for floors, '~' for water,
     * '^' for traps, '+' for doors that provide horizontal passage, and '/' for doors that provide vertical passage.
     * Use the addDoors, addWater, and addTraps methods of this class to request these in the next generated map.
     * @return a char[][] dungeon
     */
    public char[][] generate() {
        return generate(TilesetType.DEFAULT_DUNGEON);
    }

    /**
     * Generate a char[][] dungeon given a TilesetType; the comments in that class provide some opinions on what
     * each TilesetType value could be used for in a game. This uses '#' for walls, '.' for floors, '~' for water,
     * '^' for traps, '+' for doors that provide horizontal passage, and '/' for doors that provide vertical passage.
     * Use the addDoors, addWater, and addTraps methods of this class to request these in the next generated map.
     * @see squidpony.squidgrid.mapping.styled.TilesetType
     * @param kind a TilesetType enum value, such as TilesetType.DEFAULT_DUNGEON
     * @return a char[][] dungeon
     */
    public char[][] generate(TilesetType kind)
    {
        DungeonUtility.rng = rng;
        char[][] map = DungeonBoneGen.wallWrap(gen.generate(kind, width, height));
        DijkstraMap dijkstra = new DijkstraMap(map);
        int frustrated = 0;
        do {
            dijkstra.setGoal(DungeonUtility.randomFloor(map));
            dijkstra.scan(null);
            frustrated++;
        }while (dijkstra.getMappedCount() < width + height && frustrated < 10);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(dijkstra.gradientMap[i][j] == DijkstraMap.DARK)
                    map[i][j] = '#';
            }
        }

        LinkedHashSet<Point> floors = new LinkedHashSet<Point>();
        LinkedHashSet<Point> doorways = new LinkedHashSet<Point>();
        LinkedHashSet<Point> hazards = new LinkedHashSet<Point>();
        Point temp = new Point(0, 0);
        boolean doubleDoors = false;
        int doorFill = 0;
        int waterFill = 0;
        int trapFill = 0;
        if(fx.containsKey(FillEffect.DOORS))
        {
            doorFill = fx.get(FillEffect.DOORS);
            if(doorFill < 0)
            {
                doubleDoors = true;
                doorFill *= -1;
            }
        }
        if(fx.containsKey(FillEffect.WATER)) {
            waterFill = fx.get(FillEffect.WATER);
        }

        if(fx.containsKey(FillEffect.TRAPS)) {
            trapFill = fx.get(FillEffect.TRAPS);
        }

        doorways = viableDoorways(doubleDoors, map);

        LinkedHashSet<Point> obstacles = new LinkedHashSet<Point>(doorways.size() * doorFill / 100);
        if(doorFill > 0)
        {
            int total = doorways.size() * doorFill / 100;

            BigLoop:
            for(int i = 0; i < total; i++)
            {
                Point entry = (Point) doorways.toArray()[rng.nextInt(doorways.size())];
                if(map[entry.x - 1][entry.y] != '#' && map[entry.x + 1][entry.y] != '#')
                {
                    map[entry.x][entry.y] = '+';
                }
                else {
                    map[entry.x][entry.y] = '/';
                }
                obstacles.add(new Point(entry));
                Point[] adj = new Point[]{new Point(entry.x + 1, entry.y), new Point(entry.x - 1, entry.y),
                        new Point(entry.x, entry.y + 1), new Point(entry.x, entry.y - 1)};
                for(Point near : adj) {
                    if (doorways.contains(near)) {
                        map[near.x][near.y] = '#';
                        obstacles.add(new Point(near.x, near.y));
                        doorways.remove(near);
                        i++;
                        doorways.remove(entry);
                        continue BigLoop;
                    }
                }
                doorways.remove(entry);
            }
        }

        for(int x = 1; x < map.length - 1; x++, temp.x = x)
        {
            for(int y = 1; y < map[x].length - 1; y++, temp.y = y)
            {
                if(map[x][y] == '.' && !obstacles.contains(temp))
                {
                    floors.add(new Point(x, y));
                    int ctr = 0;
                    if(map[x+1][y] != '#') ++ctr;
                    if(map[x-1][y] != '#') ++ctr;
                    if(map[x][y+1] != '#') ++ctr;
                    if(map[x][y-1] != '#') ++ctr;
                    if(map[x+1][y+1] != '#') ++ctr;
                    if(map[x-1][y+1] != '#') ++ctr;
                    if(map[x+1][y-1] != '#') ++ctr;
                    if(map[x-1][y-1] != '#') ++ctr;
                    if(ctr >= 5) hazards.add(new Point(x, y));
                }
            }
        }
        if(waterFill > 0)
        {
            int numPools = rng.nextInt(4) + 2 + waterFill / 20;
            int[] volumes = new int[numPools];
            int total = floors.size() * waterFill / 100;
            int error = 0;
            for(int i = 0; i < numPools; i++) {
                volumes[i] = total / numPools;
                error += volumes[i];
            }
            volumes[0] += total - error;

            for(int i = 0; i < numPools; i++) {
                int r = rng.nextInt(volumes[i] / 2) - volumes[i] / 4;
                volumes[i] += r;
                volumes[(i + 1) % numPools] -= r;
            }
            Spill spill = new Spill(map, Spill.Measurement.MANHATTAN, rng);
            int bonusVolume = 0;
            for(int i = 0; i < numPools; i++)
            {
                floors.removeAll(obstacles);
                Point entry = (Point) floors.toArray()[rng.nextInt(floors.size())];
//                spill.start(entry, volumes[i] / 3, obstacles);
//                spill.start(entry, 2 * volumes[i] / 3, obstacles);
                ArrayList<Point> ordered = new ArrayList<Point>(spill.start(entry, volumes[i], obstacles));
                floors.removeAll(ordered);
                hazards.removeAll(ordered);
                obstacles.addAll(ordered);

                if(spill.filled <= volumes[i])
                {
                    bonusVolume += volumes[i] - spill.filled;
                }

            }
            for(int x = 1; x < map.length - 1; x++) {
                for (int y = 1; y < map[x].length - 1; y++) {
                    if(spill.spillMap[x][y])
                        map[x][y] = '~';
                }
            }
            int frustration = 0;
            while (bonusVolume > 0 && frustration < 50)
            {
                Point entry = DungeonUtility.randomFloor(map);
                ArrayList<Point> finisher = spill.start(entry, bonusVolume, obstacles);
                for(Point p : finisher)
                {
                    map[p.x][p.y] = '~';
                }
                bonusVolume -= spill.filled;
                hazards.removeAll(finisher);
                frustration++;
            }
        }



        if(trapFill > 0)
        {
            int total = hazards.size() * trapFill / 100;

            for(int i = 0; i < total; i++)
            {
                Point entry = (Point) hazards.toArray()[rng.nextInt(hazards.size())];
                map[entry.x][entry.y] = '^';
                hazards.remove(entry);
            }
        }

        dungeon = map;
        return map;

    }

    /**
     * Provides a string representation of the latest generated dungeon.
     *
     * @return
     */
    public String toString() {
        char[][] trans = new char[height][width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                trans[y][x] = dungeon[x][y];
            }
        }
        StringBuffer sb = new StringBuffer();
        for (int row = 0; row < height; row++) {
            sb.append(trans[row]);
            sb.append('\n');
        }
        return sb.toString();
    }

}
