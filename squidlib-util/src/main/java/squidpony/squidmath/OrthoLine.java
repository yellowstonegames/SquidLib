package squidpony.squidmath;

import java.util.ArrayList;
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
    public static ArrayList<Coord> line(int startX, int startY, int endX, int endY) {
        int dx = endX - startX, dy = endY - startY, nx = Math.abs(dx), ny = Math.abs(dy);
        int signX = (dx > 0) ? 1 : -1, signY = (dy > 0) ? 1 : -1, workX = startX, workY = startY;
        ArrayList<Coord> drawn = new ArrayList<>(1 + nx + ny);
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
    public static ArrayList<Coord> line(Coord start, Coord end)
    {
        return line(start.x, start.y, end.x, end.y);
    }
    /**
     * Draws a line from (startX, startY) to (endX, endY) using only N/S/E/W movement. Returns an array of Coord in order.
     *
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @return array of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static Coord[] line_(int startX, int startY, int endX, int endY) {
        int dx = endX - startX, dy = endY - startY, nx = Math.abs(dx), ny = Math.abs(dy);
        int signX = (dx > 0) ? 1 : -1, signY = (dy > 0) ? 1 : -1, workX = startX, workY = startY;
        Coord[] drawn = new Coord[nx + ny + 1];
        drawn[0] = Coord.get(startX, startY);
        for (int i = 1, ix = 0, iy = 0; ix < nx || iy < ny; i++) {
            if ((0.5f + ix) / nx < (0.5 + iy) / ny) {
                workX += signX;
                ix++;
            } else {
                workY += signY;
                iy++;
            }
            drawn[i] = Coord.get(workX, workY);
        }
        return drawn;
    }

    /**
     * Draws a line from start to end using only N/S/E/W movement. Returns a List of Coord in order.
     * @param start starting point
     * @param end ending point
     * @return List of Coord, including start and end and all points walked between
     */
    public static Coord[] line_(Coord start, Coord end)
    {
        return line_(start.x, start.y, end.x, end.y);
    }

    /**
     * Given an array of Coord as produced by {@link #line_(Coord, Coord)} or {@link #line_(int, int, int, int)}, this
     * gets a char array of box-drawing characters that connect when drawn at the corresponding Coord positions in the
     * given line. This can be useful for drawing highlight lines or showing what path something will take, as long as
     * it only uses 4-way orthogonal connections between Coords. Any connections that require a diagonal will not be
     * handled by this method (returning a straight line without much accuracy), and any Coords that aren't adjacent
     * will cause an {@link IllegalStateException} if this has to draw a line between them. If this method is called on
     * the result of this class' line_() method, then it should always return a valid result; if it is called on a path
     * made with some other method, such as from {@link Bresenham#line2D_(Coord, Coord)}, then it shouldn't throw an
     * exception but may produce a low-quality (disconnected visually) line.
     * @param line a Coord array where each Coord is orthogonally adjacent to its neighbor(s) in the array; usually
     *             produced via {@link #line_(Coord, Coord)} or {@link #line_(int, int, int, int)}
     * @return a char array of box-drawing chars that will connect when drawn at the same points as in line
     */
    public static char[] lineChars(Coord[] line)
    {
        // ─│┌┐└┘├┤┬┴┼
        if(line == null) return null;
        int len = line.length;
        if(len == 0) return new char[0];
        if(len == 1) return new char[]{'─'};
        char[] cs = new char[len];
        cs[0] = line[0].x == line[1].x ? '│' : '─';
        cs[len - 1] = line[len - 1].x == line[len - 2].x ? '│' : '─';
        Coord before, current, next;
        for (int i = 1; i < len - 1; i++) {
            before = line[i-1];
            current = line[i];
            next = line[i+1];
            switch (before.toGoTo(current))
            {
                case RIGHT:
                    switch (current.toGoTo(next))
                    {
                        case UP: cs[i] = '┘';
                            break;
                        case DOWN: cs[i] = '┐';
                            break;
                        default: cs[i] = '─';
                            break;
                    }
                    break;
                case LEFT:
                    switch (current.toGoTo(next))
                    {
                        case UP: cs[i] = '└';
                            break;
                        case DOWN: cs[i] = '┌';
                            break;
                        default: cs[i] = '─';
                            break;
                    }
                    break;
                case UP:
                    switch (current.toGoTo(next))
                    {
                        case LEFT: cs[i] = '┐';
                            break;
                        case RIGHT: cs[i] = '┌';
                            break;
                        default: cs[i] = '│';
                            break;
                    }
                    break;
                default:
                    switch (current.toGoTo(next))
                    {
                        case LEFT: cs[i] = '┘';
                            break;
                        case RIGHT: cs[i] = '└';
                            break;
                        default: cs[i] = '│';
                            break;
                    }
            }
        }
        return cs;
    }
    /**
     * Given a List of Coord as produced by {@link #line(Coord, Coord)} or {@link #line(int, int, int, int)}, this
     * gets a char array of box-drawing characters that connect when drawn at the corresponding Coord positions in the
     * given line. This can be useful for drawing highlight lines or showing what path something will take, as long as
     * it only uses 4-way orthogonal connections between Coords. Any connections that require a diagonal will not be
     * handled by this method (returning a straight line without much accuracy), and any Coords that aren't adjacent
     * will cause an {@link IllegalStateException} if this has to draw a line between them. If this method is called on
     * the result of this class' line() method, then it should always return a valid result; if it is called on a path
     * made with some other method, such as from {@link Bresenham#line2D(Coord, Coord)}, then it shouldn't throw an
     * exception but may produce a low-quality (disconnected visually) line.
     * @param line a List of Coord where each Coord is orthogonally adjacent to its neighbor(s) in the List; usually
     *             produced via {@link #line(Coord, Coord)} or {@link #line(int, int, int, int)}
     * @return a char array of box-drawing chars that will connect when drawn at the same points as in line
     */
    public static char[] lineChars(List<Coord> line)
    {
        // ─│┌┐└┘├┤┬┴┼
        if(line == null) return null;
        int len = line.size();
        if(len == 0) return new char[0];
        if(len == 1) return new char[]{'─'};
        char[] cs = new char[len];
        cs[0] = line.get(0).x == line.get(1).x ? '│' : '─';
        cs[len - 1] = line.get(len - 1).x == line.get(len - 2).x ? '│' : '─';
        Coord before, current, next;
        for (int i = 1; i < len - 1; i++) {
            before = line.get(i-1);
            current = line.get(i);
            next = line.get(i+1);
            switch (before.toGoTo(current))
            {
                case RIGHT:
                    switch (current.toGoTo(next))
                    {
                        case UP: cs[i] = '┘';
                            break;
                        case DOWN: cs[i] = '┐';
                            break;
                        default: cs[i] = '─';
                            break;
                    }
                    break;
                case LEFT:
                    switch (current.toGoTo(next))
                    {
                        case UP: cs[i] = '└';
                            break;
                        case DOWN: cs[i] = '┌';
                            break;
                        default: cs[i] = '─';
                            break;
                    }
                    break;
                case UP:
                    switch (current.toGoTo(next))
                    {
                        case LEFT: cs[i] = '┐';
                            break;
                        case RIGHT: cs[i] = '┌';
                            break;
                        default: cs[i] = '│';
                            break;
                    }
                    break;
                default:
                    switch (current.toGoTo(next))
                    {
                        case LEFT: cs[i] = '┘';
                            break;
                        case RIGHT: cs[i] = '└';
                            break;
                        default: cs[i] = '│';
                            break;
                    }
            }
        }
        return cs;
    }
}
