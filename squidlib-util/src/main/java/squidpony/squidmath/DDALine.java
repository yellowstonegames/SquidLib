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
     * Draws a line from (startX, startY) to (endX, endY) using the DDA algorithm. Returns a List of Coord in order.
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @return List of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static List<Coord> line(int startX, int startY, int endX, int endY) {
        return line(startX, startY, endX, endY, 0x7fff, 0x7fff);
    }

    /**
     * Not intended for external use; prefer the overloads without a modifier argument.
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @param modifierX an integer that should typically be one of 0x3fff, 0x7fff, or 0xbfff
     * @param modifierY an integer that should typically be one of 0x3fff, 0x7fff, or 0xbfff
     * @return List of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static List<Coord> line(int startX, int startY, int endX, int endY, int modifierX, int modifierY) {
        int dx = endX - startX, dy = endY - startY, nx = Math.abs(dx), ny = Math.abs(dy),
                octant = ((dy < 0) ? 4 : 0) | ((dx < 0) ? 2 : 0) | ((ny > nx) ? 1 : 0), move, frac = 0,
                mn = Math.max(nx, ny);
        LinkedList<Coord> drawn = new LinkedList<>();
        if(mn == 0)
        {
            drawn.add(Coord.get(startX, startY));
            return drawn;
        }
        if(mn == nx) //ny is 0
        {
            for (int x = startX; x <= endX; x++) {
                drawn.add(Coord.get(x, startY));
            }
            return drawn;
        }
        if(mn == ny) //nx is 0
        {
            for (int y = startY; y <= endY; y++) {
                drawn.add(Coord.get(startX, y));
            }
            return drawn;
        }

        switch (octant)
        {
            // x positive, y positive
            case 0:
                move = (ny << 16)/nx;
                for (int primary = startX; primary <= endX; primary++, frac+=move) {
                    drawn.add(Coord.get(primary, startY + ((frac+modifierY)>>16)));
                }
                break;
            case 1:
                move = (nx << 16)/ny;
                for (int primary = startY; primary <= endY; primary++, frac+=move) {
                    drawn.add(Coord.get(startX + ((frac+modifierX)>>16), primary));
                }
                break;
            // x negative, y positive
            case 2:
                move = (ny << 16)/nx;
                for (int primary = startX; primary >= endX; primary--, frac+=move) {
                    drawn.add(Coord.get(primary, startY + ((frac+modifierY)>>16)));
                }
                break;
            case 3:
                move = (nx << 16)/ny;
                for (int primary = startY; primary <= endY; primary++, frac+=move) {
                    drawn.add(Coord.get(startX - ((frac+modifierX)>>16), primary));
                }
                break;
            // x negative, y negative
            case 6:
                move = (ny << 16)/nx;
                for (int primary = startX; primary >= endX; primary--, frac+=move) {
                    drawn.add(Coord.get(primary, startY - ((frac+modifierY)>>16)));
                }
                break;
            case 7:
                move = (nx << 16)/ny;
                for (int primary = startY; primary >= endY; primary--, frac+=move) {
                    drawn.add(Coord.get(startX - ((frac+modifierX)>>16), primary));
                }
                break;
            // x positive, y negative
            case 4:
                move = (ny << 16)/nx;
                for (int primary = startX; primary <= endX; primary++, frac+=move) {
                    drawn.add(Coord.get(primary, startY - ((frac+modifierY)>>16)));
                }
                break;
            case 5:
                move = (nx << 16)/ny;
                for (int primary = startY; primary >= endY; primary--, frac+=move) {
                    drawn.add(Coord.get(startX + ((frac+modifierX)>>16), primary));
                }
                break;
        }
        return drawn;
    }

    /**
     * Draws a line from start to end using the DDA algorithm. Returns a List of Coord in order.
     * @param start starting point
     * @param end ending point
     * @return List of Coord, including start and end and all points walked between
     */
    public static List<Coord> line(Coord start, Coord end)
    {
        return line(start.x, start.y, end.x, end.y);
    }
    /**
     * Draws a line from (startX, startY) to (endX, endY) using the DDA algorithm. Returns an array of Coord in order.
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @return array of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static Coord[] line_(int startX, int startY, int endX, int endY) {
        return line_(startX, startY, endX, endY, 0x7fff, 0x7fff);
    }

    /**
     * Not intended for external use; prefer the overloads without a modifier argument.
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @param modifierX an integer that should typically be one of 0x3fff, 0x7fff, or 0xbfff
     * @param modifierY an integer that should typically be one of 0x3fff, 0x7fff, or 0xbfff
     * @return array of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static Coord[] line_(int startX, int startY, int endX, int endY, int modifierX, int modifierY) {
        int dx = endX - startX, dy = endY - startY, nx = Math.abs(dx), ny = Math.abs(dy),
                octant = ((dy < 0) ? 4 : 0) | ((dx < 0) ? 2 : 0) | ((ny > nx) ? 1 : 0), move, frac = 0,
                mn = Math.max(nx, ny);
        if(mn == 0)
        {
            return new Coord[]{Coord.get(startX, startY)};
        }
        Coord[] drawn = new Coord[mn + 1];
        if(mn == nx) //ny is 0
        {
            if(dx > 0) {
                for (int x = startX, i = 0; x <= endX; x++, i++) {
                    drawn[i] = Coord.get(x, startY);
                }
            }
            else {
                for (int x = startX, i = 0; x >= endX; x--, i++) {
                    drawn[i] = Coord.get(x, startY);
                }
            }

            return drawn;
        }
        if(mn == ny) //nx is 0
        {
            if(dy > 0) {
                for (int y = startY, i = 0; y <= endY; y++, i++) {
                    drawn[i] = Coord.get(startX, y);
                }
            }
            else {
                for (int y = startY, i = 0; y >= endY; y--, i++) {
                    drawn[i] = Coord.get(startX, y);
                }
            }
            return drawn;
        }
        switch (octant)
        {
            // x positive, y positive
            case 0:
                move = (ny << 16)/nx;
                for (int i = 0, primary = startX; primary <= endX; primary++, frac+=move, i++) {
                    drawn[i] = Coord.get(primary, startY + ((frac+modifierY)>>16));
                }
                break;
            case 1:
                move = (nx << 16)/ny;
                for (int i = 0, primary = startY; primary <= endY; primary++, frac+=move, i++) {
                    drawn[i] = Coord.get(startX + ((frac+modifierX)>>16), primary);
                }
                break;
            // x negative, y positive
            case 2:
                move = (ny << 16)/nx;
                for (int i = 0, primary = startX; primary >= endX; primary--, frac+=move, i++) {
                    drawn[i] = Coord.get(primary, startY + ((frac+modifierY)>>16));
                }
                break;
            case 3:
                move = (nx << 16)/ny;
                for (int i = 0, primary = startY; primary <= endY; primary++, frac+=move, i++) {
                    drawn[i] = Coord.get(startX - ((frac+modifierX)>>16), primary);
                }
                break;
            // x negative, y negative
            case 6:
                move = (ny << 16)/nx;
                for (int i = 0, primary = startX; primary >= endX; primary--, frac+=move, i++) {
                    drawn[i] = Coord.get(primary, startY - ((frac+modifierY)>>16));
                }
                break;
            case 7:
                move = (nx << 16)/ny;
                for (int i = 0, primary = startY; primary >= endY; primary--, frac+=move, i++) {
                    drawn[i] = Coord.get(startX - ((frac+modifierX)>>16), primary);
                }
                break;
            // x positive, y negative
            case 4:
                move = (ny << 16)/nx;
                for (int i = 0, primary = startX; primary <= endX; primary++, frac+=move, i++) {
                    drawn[i] = Coord.get(primary, startY - ((frac+modifierY)>>16));
                }
                break;
            case 5:
                move = (nx << 16)/ny;
                for (int i = 0, primary = startY; primary >= endY; primary--, frac+=move, i++) {
                    drawn[i] = Coord.get(startX + ((frac+modifierX)>>16), primary);
                }
                break;
        }
        return drawn;
    }

    /**
     * Draws a line from start to end using the DDA algorithm. Returns an array of Coord in order.
     * @param start starting point
     * @param end ending point
     * @return array of Coord, including start and end and all points walked between
     */
    public static Coord[] line_(Coord start, Coord end)
    {
        return line_(start.x, start.y, end.x, end.y);
    }

}
