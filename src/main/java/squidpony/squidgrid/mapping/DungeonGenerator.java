package squidpony.squidgrid.mapping;

import squidpony.annotation.Beta;
import squidpony.squidgrid.mapping.styled.DungeonGen;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.Spill;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Creates a map for use in creating adventure areas.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class DungeonGenerator {

    public enum FillEffect
    {
        WATER, DOORS, TRAPS
    }
    public HashMap<FillEffect, Integer> fx;
    private DungeonGen gen;
    private int height, width;
    public LightRNG rng;
    public char[][] dungeon = null;
    public DungeonGenerator()
    {
        rng = new LightRNG();
        gen = new DungeonGen(rng);
        height = 40;
        width = 40;
        fx = new HashMap<FillEffect, Integer>();
    }
    public DungeonGenerator(int height, int width)
    {
        rng = new LightRNG();
        gen = new DungeonGen(rng);
        this.height = height;
        this.width = width;
        fx = new HashMap<FillEffect, Integer>();
    }

    /**
     * Turns the given percentage of floor cells into water cells, represented by '~'.
     * @param percentage
     * @return
     */
    public DungeonGenerator AddWater(int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        if(fx.containsKey(FillEffect.WATER)) fx.remove(FillEffect.WATER);
        fx.put(FillEffect.WATER, percentage);
        return this;
    }
    /**
     * Turns the given percentage of viable doorways into doors, represented by '+'. If doubleDoors is true,
     * 2-cell-wide openings will be considered viable doorways and may receive a door in each cell.
     * @param percentage
     * @param doubleDoors
     * @return
     */
    public DungeonGenerator AddDoors(int percentage, boolean doubleDoors)
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
     * possible way to move around a trap will not receive traps, ever.
     * @param percentage
     * @return
     */
    public DungeonGenerator AddTraps(int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        if(fx.containsKey(FillEffect.TRAPS)) fx.remove(FillEffect.TRAPS);
        fx.put(FillEffect.TRAPS, percentage);
        return this;
    }

    private HashSet<Point> removeAdjacent(HashSet<Point> coll, Point pt)
    {
        for(Point temp : new Point[]{new Point(pt.x+1, pt.y), new Point(pt.x-1, pt.y),
                new Point(pt.x, pt.y+1), new Point(pt.x, pt.y-1)})
        {
            if(coll.contains(temp) && !(temp.x == pt.x && temp.y == pt.y))
                coll.remove(temp);
        }

        return coll;
    }
    private HashSet<Point> removeAdjacent(HashSet<Point> coll, Point pt1, Point pt2)
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

    private HashSet<Point> viableDoorways(boolean doubleDoors, char[][] map)
    {
        HashSet<Point> doors = new HashSet<Point>();
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

    public char[][] generate(TilesetType kind)
    {
        char[][] map = DungeonGen.wallWrap(gen.generate(kind, width, height));

        HashSet<Point> floors = new HashSet<Point>();
        HashSet<Point> doorways = new HashSet<Point>();
        HashSet<Point> hazards = new HashSet<Point>();
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
        for(int x = 1; x < map.length - 1; x++, temp.x = x)
        {
            for(int y = 1; y < map[x].length - 1; y++, temp.y = y)
            {
                if(map[x][y] == '.')
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


        if(doorFill > 0)
        {
            int total = doorways.size() * doorFill / 100;

            BigLoop:
            for(int i = 0; i < total; i++)
            {
                Point entry = (Point) doorways.toArray()[rng.nextInt(doorways.size())];
                map[entry.x][entry.y] = '+';
                Point[] adj = new Point[]{new Point(entry.x + 1, entry.y), new Point(entry.x - 1, entry.y),
                        new Point(entry.x, entry.y + 1), new Point(entry.x, entry.y - 1)};
                for(Point near : adj) {
                    if (doorways.contains(near)) {
                        map[near.x][near.y] = '+';
                        doorways.remove(near);
                        i++;
                        doorways.remove(entry);
                        continue BigLoop;
                    }
                }
                doorways.remove(entry);
            }
        }

        if(waterFill > 0)
        {
            int numPools = rng.nextInt(2, 6);
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
            Spill spill = new Spill(map, Spill.Measurement.MANHATTAN);

            for(int i = 0; i < numPools; i++)
            {
                Point entry = (Point) floors.toArray()[rng.nextInt(floors.size())];
                spill.start(entry, volumes[i] / 3, null);
                spill.start(entry, 2 * volumes[i] / 3, null);
                spill.start(entry, volumes[i], null);
            }
            for(int x = 1; x < map.length - 1; x++) {
                for (int y = 1; y < map[x].length - 1; y++) {
                    if(spill.spillMap[x][y])
                        map[x][y] = '~';
                }
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
