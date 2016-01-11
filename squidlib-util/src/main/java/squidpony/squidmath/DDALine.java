package squidpony.squidmath;

import java.util.LinkedList;
import java.util.List;

/**
 * A fixed-point line-drawing algorithm that should have good performance; may be useful for LOS.
 * Algorithm is from https://hbfs.wordpress.com/2009/07/28/faster-than-bresenhams-algorithm/
 * Created by Tommy Ettinger on 1/10/2016.
 */
public class DDALine {
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
        int dx = endX - startX, dy = endY - startY, nx = Math.abs(dx), ny = Math.abs(dy),
                octant = ((dy < 0) ? 4 : 0) | ((dx < 0) ? 2 : 0) | ((ny > nx) ? 1 : 0), move, frac = 0;

        LinkedList<Coord> drawn = new LinkedList<Coord>();
        switch (octant)
        {
            // x positive, y positive
            case 0:
                move = (ny << 16)/nx;
                for (int primary = startX; primary <= endX; primary++, frac+=move) {
                    drawn.add(Coord.get(primary, startY + ((frac+0x7FFF)>>16)));
                }
                break;
            case 1:
                move = (nx << 16)/ny;
                for (int primary = startY; primary <= endY; primary++, frac+=move) {
                    drawn.add(Coord.get(startX + ((frac+0x7FFF)>>16), primary));
                }
                break;
            // x negative, y positive
            case 2:
                move = (ny << 16)/nx;
                for (int primary = startX; primary >= endX; primary--, frac+=move) {
                    drawn.add(Coord.get(primary, startY + ((frac+0x7FFF)>>16)));
                }
                break;
            case 3:
                move = (nx << 16)/ny;
                for (int primary = startY; primary <= endY; primary++, frac+=move) {
                    drawn.add(Coord.get(startX - ((frac+0x7FFF)>>16), primary));
                }
                break;
            // x negative, y negative
            case 6:
                move = (ny << 16)/nx;
                for (int primary = startX; primary >= endX; primary--, frac+=move) {
                    drawn.add(Coord.get(primary, startY - ((frac+0x7FFF)>>16)));
                }
                break;
            case 7:
                move = (nx << 16)/ny;
                for (int primary = startY; primary >= endY; primary--, frac+=move) {
                    drawn.add(Coord.get(startX - ((frac+0x7FFF)>>16), primary));
                }
                break;
            // x positive, y negative
            case 4:
                move = (ny << 16)/nx;
                for (int primary = startX; primary <= endX; primary++, frac+=move) {
                    drawn.add(Coord.get(primary, startY - ((frac+0x7FFF)>>16)));
                }
                break;
            case 5:
                move = (nx << 16)/ny;
                for (int primary = startY; primary >= endY; primary--, frac+=move) {
                    drawn.add(Coord.get(startX + ((frac+0x7FFF)>>16), primary));
                }
                break;
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
