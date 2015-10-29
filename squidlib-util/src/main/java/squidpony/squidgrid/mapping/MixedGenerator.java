package squidpony.squidgrid.mapping;

import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;
import squidpony.squidmath.PoissonDisk;
import squidpony.squidmath.RNG;

import java.util.ArrayList;
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
        ROUND,
        BOX_WALLED,
        ROUND_WALLED
    }
    private EnumMap<CarverType, Integer> carvers;
    private int height, width;
    public RNG rng;
    private char[][] dungeon;
    private boolean[][] marked, walled;
    private List<Coord> points;
    private int totalPoints;

    /**
     * Internal use.
     * @param width dungeon width in cells
     * @param height dungeon height in cells
     * @param rng rng to use
     * @return evenly spaced Coord points in a list made by PoissonDisk, trimmed down so they aren't all used
     * @see PoissonDisk used to make the list
     */
    private static List<Coord> basicPoints(int width, int height, RNG rng)
    {
        return PoissonDisk.sampleRectangle(Coord.get(2, 2), Coord.get(width - 3, height - 3),
                8.5f * (width + height) / 120f, width, height, 35, rng);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses Poisson Disk sampling to generate the points it will draw caves and
     * corridors between, ensuring a minimum distance between points, but it does not ensure that paths between points
     * will avoid overlapping with rooms or other paths. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng an RNG object to use for random choices; this make a lot of random choices.
     * @see PoissonDisk used to ensure spacing for the points.
     */
    public MixedGenerator(int width, int height, RNG rng) {
        this(width, height, rng, basicPoints(width, height, rng));
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a List of Coord points from some other source to determine the path to add
     * rooms or caves to and then connect. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng an RNG object to use for random choices; this make a lot of random choices.
     * @param sequence a List of Coord to connect in order; index 0 is the start, index size() - 1 is the end.
     * @see SerpentMapGenerator a class that uses this technique
     */
    public MixedGenerator(int width, int height, RNG rng, List<Coord> sequence) {
        this.height = height;
        this.width = width;
        if(width <= 2 || height <= 2)
            throw new ExceptionInInitializerError("width and height must be greater than 2");
        this.rng = rng;
        dungeon = new char[width][height];
        marked = new boolean[width][height];
        walled = new boolean[width][height];
        Arrays.fill(dungeon[0], '#');
        for (int i = 1; i < width; i++) {
            System.arraycopy(dungeon[0], 0, dungeon[i], 0, height);
        }
        points = new ArrayList<Coord>(sequence);
        totalPoints = sequence.size();
        carvers = new EnumMap<CarverType, Integer>(CarverType.class);
    }

    /**
     * Changes the number of "carvers" that will create caves from one room to the next. If count is 0 or less, no caves
     * will be made. If count is at least 1, caves are possible, and higher numbers relative to the other carvers make
     * caves more likely. Carvers are shuffled when used, then repeat if exhausted during generation. Since typically
     * about 30-40 rooms are carved, large totals for carver count aren't really needed; aiming for a total of 10
     * between the count of putCaveCarvers(), putBoxRoomCarvers(), putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and
     * putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making caves between rooms; only matters in relation to other carvers
     */
    public void putCaveCarvers(int count)
    {
        carvers.put(CarverType.CAVE, count);
    }
    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a box shape at the start and end, and a small room at the corner if there is one. If count
     * is 0 or less, no box-shaped rooms will be made. If count is at least 1, box-shaped rooms are possible, and higher
     * numbers relative to the other carvers make box-shaped rooms more likely. Carvers are shuffled when used, then
     * repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making box-shaped rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putBoxRoomCarvers(int count)
    {
        carvers.put(CarverType.BOX, count);
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a circle shape at the start and end, and a small circular room at the corner if there is
     * one. If count is 0 or less, no circular rooms will be made. If count is at least 1, circular rooms are possible,
     * and higher numbers relative to the other carvers make circular rooms more likely. Carvers are shuffled when used,
     * then repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making circular rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putRoundRoomCarvers(int count)
    {
        carvers.put(CarverType.ROUND, count);
    }
    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a box shape at the start and end, and a small room at the corner if there is one, enforcing
     * the presence of walls around the rooms even if another room is already there or would be placed there. Corridors
     * can always pass through enforced walls, but caves will open at most one cell in the wall. If count
     * is 0 or less, no box-shaped rooms will be made. If count is at least 1, box-shaped rooms are possible, and higher
     * numbers relative to the other carvers make box-shaped rooms more likely. Carvers are shuffled when used, then
     * repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making box-shaped rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putWalledBoxRoomCarvers(int count)
    {
        carvers.put(CarverType.BOX_WALLED, count);
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a circle shape at the start and end, and a small circular room at the corner if there is
     * one, enforcing the presence of walls around the rooms even if another room is already there or would be placed
     * there. Corridors can always pass through enforced walls, but caves will open at most one cell in the wall. If
     * count is 0 or less, no circular rooms will be made. If count is at least 1, circular rooms are possible,
     * and higher numbers relative to the other carvers make circular rooms more likely. Carvers are shuffled when used,
     * then repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making circular rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putWalledRoundRoomCarvers(int count)
    {
        carvers.put(CarverType.ROUND_WALLED, count);
    }

    /**
     * Uses the added carvers (or just makes caves if none were added) to carve from point to point in sequence, if it
     * was provided by the constructor, or evenly-spaced randomized points if it was not. This will never carve out
     * cells on the very edge of the map. Uses the numbers of the various kinds of carver that were added relative to
     * each other to determine how frequently to use a given carver type.
     * @return a char[][] where '#' is a wall and '.' is a floor or corridor; x first y second
     */
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
            allCarvings = new CarverType[]{CarverType.CAVE};
            totalLength = 1;
        }
        else
            allCarvings = rng.shuffle(allCarvings);

        for (int p = 0, c = 0; p < totalPoints - 1; p++, c = (++c) % totalLength) {
            Coord start = points.get(p), end = points.get(p + 1);
            CarverType ct = allCarvings[c];
            Direction dir;
            switch (ct)
            {
                case CAVE:
                    markPiercing(end);
                    store();
                    double weight = 0.75;
                    do {
                        Coord cent = markPlus(start);
                        if(cent != null)
                        {
                            markPiercing(cent);
                            markPiercing(cent.translate(1, 0));
                            markPiercing(cent.translate(-1, 0));
                            markPiercing(cent.translate(0, 1));
                            markPiercing(cent.translate(0, -1));
                            weight = 0.95;
                        }
                        dir = stepWobbly(start, end, weight);
                        start = start.translate(dir);
                    }while (dir != Direction.NONE);
                    break;
                case BOX:
                    markRectangle(end, rng.between(1, 5), rng.between(1, 5));
                    markRectangle(start, rng.between(1, 4), rng.between(1, 4));
                    store();
                    dir = Direction.getDirection(end.x - start.x, (end.y - start.y));
                    if(dir.isDiagonal())
                        dir = rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
                                : Direction.getCardinalDirection(0, -dir.deltaY);
                    while (start.x != end.x && start.y != end.y)
                    {
                        markPiercing(start);
                        start = start.translate(dir);
                    }
                    markRectangle(start, 1, 1);
                    dir = Direction.getCardinalDirection(end.x - start.x, -(end.y - start.y));
                    while (!(start.x == end.x && start.y == end.y))
                    {
                        markPiercing(start);
                        start = start.translate(dir);
                    }
                    break;
                case BOX_WALLED:
                    markRectangleWalled(end, rng.between(1, 5), rng.between(1, 5));
                    markRectangleWalled(start, rng.between(1, 4), rng.between(1, 4));
                    store();
                    dir = Direction.getDirection(end.x - start.x, (end.y - start.y));
                    if(dir.isDiagonal())
                        dir = rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
                                : Direction.getCardinalDirection(0, -dir.deltaY);
                    while (start.x != end.x && start.y != end.y)
                    {
                        markPiercing(start);
                        start = start.translate(dir);
                    }
                    markRectangleWalled(start, 1, 1);
                    dir = Direction.getCardinalDirection(end.x - start.x, -(end.y - start.y));
                    while (!(start.x == end.x && start.y == end.y))
                    {
                        markPiercing(start);
                        start = start.translate(dir);
                    }
                    break;
                case ROUND:
                    markCircle(end, rng.between(2, 6));
                    markCircle(start, rng.between(2, 6));
                    store();
                    dir = Direction.getDirection(end.x - start.x, (end.y - start.y));
                    if(dir.isDiagonal())
                        dir = rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
                                : Direction.getCardinalDirection(0, -dir.deltaY);
                    while (start.x != end.x && start.y != end.y)
                    {
                        markPiercing(start);
                        start = start.translate(dir);
                    }
                    markCircle(start, 2);
                    dir = Direction.getCardinalDirection(end.x - start.x, -(end.y - start.y));
                    while (!(start.x == end.x && start.y == end.y))
                    {
                        markPiercing(start);
                        start = start.translate(dir);
                    }
                    break;
                case ROUND_WALLED:
                    markCircleWalled(end, rng.between(2, 6));
                    markCircleWalled(start, rng.between(2, 6));
                    store();
                    dir = Direction.getDirection(end.x - start.x, (end.y - start.y));
                    if(dir.isDiagonal())
                        dir = rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
                                : Direction.getCardinalDirection(0, -dir.deltaY);
                    while (start.x != end.x && start.y != end.y)
                    {
                        markPiercing(start);
                        start = start.translate(dir);
                    }
                    markCircleWalled(start, 2);
                    dir = Direction.getCardinalDirection(end.x - start.x, -(end.y - start.y));
                    while (!(start.x == end.x && start.y == end.y))
                    {
                        markPiercing(start);
                        start = start.translate(dir);
                    }
                    break;
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

    /**
     * Internal use. Marks a point to be made into floor.
     * @param x x position to mark
     * @param y y position to mark
     * @return false if everything is normal, true if and only if this failed to mark because the position is walled
     */
    private boolean mark(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1 && !walled[x][y]) {
            marked[x][y] = true;
            return false;
        }
        else return x > 0 && x < width - 1 && y > 0 && y < height - 1 && walled[x][y];
    }

    /**
     * Internal use. Marks a point to be made into floor.
     * @param x x position to mark
     * @param y y position to mark
     */
    private void markPiercing(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1) {
            marked[x][y] = true;
        }
    }

    /**
     * Internal use. Marks a point to be made into floor.
     * @param pos position to mark
     */
    private boolean mark(Coord pos)
    {
        return mark(pos.x, pos.y);
    }

    /**
     * Internal use. Marks a point to be made into floor.
     * @param pos position to mark
     */
    private void markPiercing(Coord pos)
    {
        markPiercing(pos.x, pos.y);
    }

    /**
     * Internal use. Marks a point and the four cells orthogonally adjacent to it.
     * @param pos center position to mark
     * @return null if the center of the plus shape wasn't blocked by wall, otherwise the Coord of the center
     */
    private Coord markPlus(Coord pos) {
        Coord block = null;
        if (mark(pos.x, pos.y))
            block = pos;
        mark(pos.x + 1, pos.y);
        mark(pos.x - 1, pos.y);
        mark(pos.x, pos.y + 1);
        mark(pos.x, pos.y - 1);
        return block;
    }
    /**
     * Internal use. Marks a rectangle of points centered on pos, extending halfWidth in both x directions and
     * halfHeight in both vertical directions.
     * @param pos center position to mark
     * @param halfWidth the distance from the center to extend horizontally
     * @param halfHeight the distance from the center to extend vertically
     * @return null if no points in the rectangle were blocked by walls, otherwise a Coord blocked by a wall
     */
    private Coord markRectangle(Coord pos, int halfWidth, int halfHeight)
    {
        halfWidth = halfWidth * width / 64;
        halfHeight = halfHeight * height / 64;
        Coord block = null;
        for (int i = pos.x - halfWidth; i <= pos.x + halfWidth; i++) {
            for (int j = pos.y - halfHeight; j <= pos.y + halfHeight; j++) {
                if(mark(i, j))
                    block = Coord.get(i, j);
            }
        }
        return block;
    }
    /**
     * Internal use. Marks a rectangle of points centered on pos, extending halfWidth in both x directions and
     * halfHeight in both vertical directions. Also considers the area just beyond each wall, but not corners, to be
     * a blocking wall that can only be passed by corridors and small cave openings.
     * @param pos center position to mark
     * @param halfWidth the distance from the center to extend horizontally
     * @param halfHeight the distance from the center to extend vertically
     * @return null if no points in the rectangle were blocked by walls, otherwise a Coord blocked by a wall
     */
    private Coord markRectangleWalled(Coord pos, int halfWidth, int halfHeight)
    {
        halfWidth = halfWidth * width / 64;
        halfHeight = halfHeight * height / 64;
        Coord block = null;
        for (int i = pos.x - halfWidth; i <= pos.x + halfWidth; i++) {
            for (int j = pos.y - halfHeight; j <= pos.y + halfHeight; j++) {
                if(mark(i, j))
                    block = Coord.get(i, j);
            }
        }
        for (int i = Math.max(0, pos.x - halfWidth - 1); i <= Math.min(width - 1, pos.x + halfWidth + 1); i++) {
            for (int j = Math.max(0, pos.y - halfHeight - 1); j <= Math.min(height - 1, pos.y + halfHeight + 1); j++)
            {
                walled[i][j] = true;
            }
        }
        return block;
    }

    /**
     * Internal use. Marks a circle of points centered on pos, extending out to radius in Euclidean measurement.
     * @param pos center position to mark
     * @param radius radius to extend in all directions from center
     * @return null if no points in the circle were blocked by walls, otherwise a Coord blocked by a wall
     */
    private Coord markCircle(Coord pos, int radius)
    {
        Coord block = null;
        int high;
        radius = radius * Math.min(width, height) / 64;
        for (int dx = -radius; dx <= radius; ++dx)
        {
            high = (int)Math.floor(Math.sqrt(radius * radius - dx * dx));
            for (int dy = -high; dy <= high; ++dy)
            {
                if(mark(pos.x + dx, pos.y + dy))
                    block = pos.translate(dx, dy);
            }
        }
        return block;
    }
    /**
     * Internal use. Marks a circle of points centered on pos, extending out to radius in Euclidean measurement.
     * Also considers the area just beyond each wall, but not corners, to be
     * a blocking wall that can only be passed by corridors and small cave openings.
     * @param pos center position to mark
     * @param radius radius to extend in all directions from center
     * @return null if no points in the circle were blocked by walls, otherwise a Coord blocked by a wall
     */
    private Coord markCircleWalled(Coord pos, int radius)
    {
        Coord block = null;
        int high;
        radius = radius * Math.min(width, height) / 64;
        for (int dx = -radius; dx <= radius; ++dx)
        {
            high = (int)Math.floor(Math.sqrt(radius * radius - dx * dx));
            for (int dy = -high; dy <= high; ++dy)
            {
                if(mark(pos.x + dx, pos.y + dy))
                    block = pos.translate(dx, dy);
            }
        }
        for (int dx = -radius; dx <= radius; ++dx)
        {
            high = (int)Math.floor(Math.sqrt(radius * radius - dx * dx));
            int dx2 = Math.max(1, Math.min(pos.x + dx, width - 2));
            for (int dy = -high; dy <= high; ++dy)
            {
                int dy2 = Math.max(1, Math.min(pos.y + dy, height - 2));

                walled[dx2][dy2] = true;
                walled[dx2+1][dy2] = true;
                walled[dx2-1][dy2] = true;
                walled[dx2][dy2+1] = true;
                walled[dx2+1][dy2+1] = true;
                walled[dx2-1][dy2+1] = true;
                walled[dx2][dy2-1] = true;
                walled[dx2+1][dy2-1] = true;
                walled[dx2-1][dy2-1] = true;

            }
        }
        return block;
    }

    /**
     * Internal use. Drunkard's walk algorithm, single step. Based on Michael Patraw's C code, used for cave carving.
     * http://mpatraw.github.io/libdrunkard/
     * @param current the current point
     * @param target the point to wobble towards
     * @param weight between 0.5 and 1.0, usually. 0.6 makes very random caves, 0.9 is almost a straight line.
     * @return a Direction, either UP, DOWN, LEFT, or RIGHT if we should move, or NONE if we have reached our target
     */
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
            int dx2 = (dx == 0) ? dx : dy, dy2 = (dx == 0) ? dy : dx;
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
            dir = Direction.getCardinalDirection(dx2, -dy2);

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
