package squidpony.squidmath;

import squidpony.squidgrid.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * A drunkard's-walk-like algorithm for line drawing "wobbly" paths.
 * The line() methods here use an IRNG (and will make their own if they don't take one as a parameter) to make a choice
 * between orthogonal directions to travel in. Because they can go around the target instead of straight to it, they
 * also need a width and height for the map so they don't wander over the edge. You can also pass a weight to one of the
 * line() methods, which affects how straight the wobbly path will be (1.0 being just about perfectly straight, 0.5
 * being very chaotic, and less than 0.5 being almost unrecognizable as a path).
 * <br>
 * Based on Michael Patraw's C code, used for cave carving in his map generator. http://mpatraw.github.io/libdrunkard/
 * Created by Tommy Ettinger on 1/10/2016.
 */
public class WobblyLine {
    private WobblyLine(){}
    /**
     * Draws a line from (startX, startY) to (endX, endY) using the Drunkard's Walk algorithm. Returns a List of Coord
     * in order.
     * <br>
     * Equivalent to calling {@code line(startX, startY, endX, endY, width, height, 0.75, new RNG())} .
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @param width maximum map width
     * @param height maximum map height
     * @return List of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static List<Coord> line(int startX, int startY, int endX, int endY, int width, int height) {
        return line(startX, startY, endX, endY, width, height, 0.75, new RNG());
    }
    /**
     * Draws a line from (startX, startY) to (endX, endY) using the Drunkard's Walk algorithm. Returns a List of Coord
     * in order.
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @param width maximum map width
     * @param height maximum map height
     * @param weight between 0.5 and 1.0, usually. 0.6 makes very random walks, 0.9 is almost a straight line.
     * @param rng the random number generator to use
     * @return List of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static List<Coord> line(int startX, int startY, int endX, int endY,
                                   int width, int height, double weight, IRNG rng) {
        List<Coord> pts = new ArrayList<>();
        Coord start = Coord.get(startX, startY);
        Direction dir;
        do {
            pts.add(start);
            dir = stepWobbly(start.x, start.y, endX, endY, weight, width, height, rng);
            start = start.translate(dir);
            if(start.x < 1 || start.y < 1 || start.x >= width - 1 || start.y >= height - 1)
                break;
        }while (dir != Direction.NONE);
        return pts;
    }

    /**
     * Internal use. Drunkard's walk algorithm, single step. Based on Michael Patraw's C code, used for cave carving.
     * http://mpatraw.github.io/libdrunkard/
     * @param currentX the x coordinate of the current point
     * @param currentY the y coordinate of the current point
     * @param targetX the x coordinate of the point to wobble towards
     * @param targetY the y coordinate of the point to wobble towards
     * @param weight between 0.5 and 1.0, usually. 0.6 makes very random walks, 0.9 is almost a straight line.
     * @param width maximum map width
     * @param height maximum map height
     * @param rng the random number generator to use
     * @return a Direction, either UP, DOWN, LEFT, or RIGHT if we should move, or NONE if we have reached our target
     */
    private static Direction stepWobbly(int currentX, int currentY, int targetX, int targetY, double weight,
                                        int width, int height, IRNG rng)
    {
        int dx = targetX - currentX;
        int dy = targetY - currentY;

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
        if(currentX + dir.deltaX <= 0 || currentX + dir.deltaX >= width - 1) {
            if (currentY < targetY) dir = Direction.DOWN;
            else if (currentY > targetY) dir = Direction.UP;
        }
        else if(currentY + dir.deltaY <= 0 || currentY + dir.deltaY >= height - 1) {
            if (currentX < targetX) dir = Direction.RIGHT;
            else if (currentX > targetX) dir = Direction.LEFT;
        }
        return dir;
    }

    /**
     * Draws a line from start to end using the Drunkard's Walk algorithm. Returns a List of Coord in order.
     * @param start starting point
     * @param end ending point
     * @param width maximum map width
     * @param height maximum map height
     * @return List of Coord, including start and end and all points walked between
     */
    public static List<Coord> line(Coord start, Coord end, int width, int height)
    {
        return line(start.x, start.y, end.x, end.y, width, height);
    }
}
