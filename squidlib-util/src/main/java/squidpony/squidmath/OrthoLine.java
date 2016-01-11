package squidpony.squidmath;

import java.util.LinkedList;
import java.util.List;

/**
 * A simple line-drawing algorithm that only takes orthogonal steps; may be useful for LOS in games that use Manhattan
 * distances for measurements.
 * Algorithm is from http://www.redblobgames.com/grids/line-drawing.html#stepping , thanks Amit!
 * Created by Tommy Ettinger on 1/10/2016.
 */
public class OrthoLine {
    /**
     * Draws a line from (startX, startY) to (endX, endY) using only N/S/E/W movement. Returns a List of Coord in order.
     *
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @return List of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static List<Coord> line(int startX, int startY, int endX, int endY) {
        int dx = endX - startX, dy = endY - startY, nx = Math.abs(dx), ny = Math.abs(dy);
        int signX = (dx > 0) ? 1 : -1, signY = (dy > 0) ? 1 : -1, workX = startX, workY = startY;
        LinkedList<Coord> drawn = new LinkedList<Coord>();
        drawn.add(Coord.get(startX, startY));
        for (int ix = 0, iy = 0; ix < nx || iy < ny; ) {
            if ((0.5f + ix) / nx < (0.5 + iy) / ny) {
                workX += signX;
                ix++;
            } else {
                workY += signY;
                iy++;
            }
            drawn.add(Coord.get(workX, workY));
        }
        return drawn;
    }

    /**
     * Draws a line from start to end using only N/S/E/W movement. Returns a List of Coord in order.
     * @param start starting point
     * @param end ending point
     * @return List of Coord, including start and end and all points walked between
     */
    public static List<Coord> line(Coord start, Coord end)
    {
        return line(start.x, start.y, end.x, end.y);
    }
}
