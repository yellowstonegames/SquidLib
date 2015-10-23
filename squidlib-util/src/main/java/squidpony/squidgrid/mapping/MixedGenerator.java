package squidpony.squidgrid.mapping;

import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;
import squidpony.squidmath.PoissonDisk;
import squidpony.squidmath.StatefulRNG;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

/**
 * A dungeon generator that can use a mix of techniques to have part-cave, part-room dungeons.
 * <br>
 * Based on Michael Patraw's excellent Drunkard's Walk dungeon generator.
 * http://mpatraw.github.io/libdrunkard/
 * Created by Tommy Ettinger on 10/22/2015.
 */
public class MixedGenerator {
    public enum CarverType
    {
        CAVE,
        BOX,
        ROUND
    }
    private EnumMap<CarverType, Integer> carvers;
    private int height, width;
    public StatefulRNG rng;
    private char[][] dungeon;
    private boolean[][] marked;
    private List<Coord> starts, ends;
    private int totalPoints;

    public MixedGenerator(int width, int height, StatefulRNG rng) {
        this.height = height;
        this.width = width;
        if(width <= 2 || height <= 2)
            throw new ExceptionInInitializerError("width and height must be greater than 2");
        this.rng = rng;
        dungeon = new char[width][height];
        marked = new boolean[width][height];
        Arrays.fill(dungeon[0], '#');
        for (int i = 1; i < width; i++) {
            System.arraycopy(dungeon[0], 0, dungeon[i], 0, height);
        }
        starts = PoissonDisk.sampleRectangle(Coord.get(1, 1), Coord.get(width - 1, height - 1),
                Math.min(width, height) / 7f, width, height, 25, rng);
        ends = PoissonDisk.sampleRectangle(Coord.get(1, 1), Coord.get(width - 1, height - 1),
                Math.min(width, height) / 7f, width, height, 25, rng);
        totalPoints = Math.min(starts.size(), ends.size());
        starts = rng.shuffle(starts);
        ends = rng.shuffle(ends);
        starts = starts.subList(0, totalPoints);
        ends = ends.subList(0, totalPoints);
        carvers = new EnumMap<CarverType, Integer>(CarverType.class);
    }

    public void putCaveCarvers(int count)
    {
        carvers.put(CarverType.CAVE, count);
    }
    public void putBoxRoomCarvers(int count)
    {
        carvers.put(CarverType.BOX, count);
    }
    public void putRoundRoomCarvers(int count)
    {
        carvers.put(CarverType.ROUND, count);
    }
    public char[][] generate()
    {
        CarverType[] carvings = carvers.keySet().toArray(new CarverType[carvers.size()]);
        int[] carvingsCounters = new int[carvings.length];
        int totalLength = 0;
        for (int i = 0; i < carvings.length; i++) {
            carvingsCounters[i] = carvers.get(carvings[i]);
            totalLength += carvingsCounters[i];
        }
        CarverType[] allCarvings = new CarverType[totalLength];

        for (int i = 0, c = 0; i < carvings.length; i++) {
            for (int j = 0; j < carvingsCounters[i]; j++) {
                allCarvings[c++] = carvings[i];
            }
        }
        if(allCarvings.length == 0)
        {
            DungeonGenerator gen = new DungeonGenerator(width, height, rng);
            return gen.generate();
        }
        allCarvings = rng.shuffle(allCarvings);

        for (int p = 0, c = 0; p < totalPoints; p++, c = (++c) % totalLength) {
            Coord start = starts.get(p), end = ends.get(p);
            CarverType ct = allCarvings[c];
            switch (ct)
            {
                default:
                    mark(end);
                    store();
                    Direction dir;
                    do {
                        markPlus(start);
                        dir = stepWobbly(start, end, 0.75);
                        start = start.translate(dir);
                        if(dungeon[start.x][start.y] == '.') {
                            markPlus(start);
                            break;
                        }
                    }while (dir != Direction.NONE);
            }
            store();
        }


        return dungeon;
    }
    private void store()
    {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(marked[i][j])
                {
                    dungeon[i][j] = '.';
                    marked[i][j] = false;
                }
            }
        }
    }
    private void mark(int x, int y)
    {
        if(x > 0 && x < width - 1 && y > 0 && y < height - 1)
            marked[x][y] = true;
    }
    private void mark(Coord pos)
    {
        mark(pos.x, pos.y);
    }
    private void markPlus(Coord pos)
    {
        mark(pos.x, pos.y);
        mark(pos.x+1, pos.y);
        mark(pos.x-1, pos.y);
        mark(pos.x, pos.y+1);
        mark(pos.x, pos.y-1);
    }

    private void markRectangle(Coord pos, int halfWidth, int halfHeight)
    {
        for (int i = pos.x - halfWidth; i <= pos.x + halfWidth; i++) {
            for (int j = pos.y - halfHeight; j < pos.y + halfHeight; j++) {
                mark(i, j);
            }
        }
    }
    private void markCircle(Coord pos, int radius)
    {
        int high;
        for (int dx = -radius; dx <= radius; ++dx)
        {
            high = (int)Math.round(Math.sqrt(radius * radius - dx * dx));
            for (int dy = -high; dy <= high; ++dy)
            {
                mark(pos.x + dx, pos.y + dy);
            }
        }
    }

    private Direction stepWobbly(Coord current, Coord target, double weight)
    {
        int dx = target.x - current.x;
        int dy = target.y - current.y;

        if (dx >  1) dx = 1;
        if (dx < -1) dx = -1;
        if (dy >  1) dy = 1;
        if (dy < -1) dy = -1;

        double r = rng.nextDouble();
        Direction dir;
        if (dx == 0 && dy == 0)
        {
            return Direction.NONE;
        }
        else if (dx == 0 || dy == 0)
        {
            int dx2 = dx, dy2 = dy;
            if (r >= (weight * 0.5))
            {
                r -= weight * 0.5;
                if (r < weight * (1.0 / 6) + (1 - weight) * (1.0 / 3))
                {
                    dx2 = -1;
                    dy2 = 0;
                }
                else if (r < weight * (2.0 / 6) + (1 - weight) * (2.0 / 3))
                {
                    dx2 = 1;
                    dy2 = 0;
                }
                else
                {
                    dx2 = 0;
                    dy2 *= -1;
                }
            }
            if (dx == 0)
            {
                dir = Direction.getCardinalDirection(dx2, -dy2);
                dx = dx2;
                dy = dy2;
            }
            else
            {
                dir = Direction.getCardinalDirection(dy2, -dx2);
                dx = dy2;
                dy = dx2;
            }
        }
        else
        {
            if (r < weight * 0.5)
            {
                dy = 0;
            }
            else if (r < weight)
            {
                dx = 0;
            }
            else if (r < weight + (1 - weight) * 0.5)
            {
                dx *= -1;
                dy = 0;
            }
            else
            {
                dx = 0;
                dy *= -1;
            }
            dir = Direction.getCardinalDirection(dx, -dy);
        }
        if(current.x + dir.deltaX <= 0 || current.x + dir.deltaX >= width - 1) {
            if (current.y < target.y) dir = Direction.DOWN;
            else if (current.y > target.y) dir = Direction.UP;
        }
        else if(current.y + dir.deltaY <= 0 || current.y + dir.deltaY >= height - 1) {
            if (current.x < target.x) dir = Direction.RIGHT;
            else if (current.x > target.x) dir = Direction.LEFT;
        }
        return dir;
    }

}
