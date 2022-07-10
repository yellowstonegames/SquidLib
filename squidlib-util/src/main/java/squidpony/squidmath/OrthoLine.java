/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package squidpony.squidmath;

import squidpony.squidgrid.mapping.DungeonUtility;

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
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link DungeonUtility#generateResistances(char[][])} or {@link DungeonUtility#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case a temporary ArrayList is allocated, which can be wasteful), or may be
     * an existing ArrayList of Coord (which will be cleared if it has any contents). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param start the starting point
     * @param target the target point
     * @param resistanceMap a resistance map as produced by {@link DungeonUtility#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ArrayList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(Coord start, Coord target, double[][] resistanceMap,
                                    ArrayList<Coord> buffer){
        return reachable(start.x, start.y, target.x, target.y, 0x7FFFFFFF, resistanceMap, buffer);
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link DungeonUtility#generateResistances(char[][])} or {@link DungeonUtility#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case a temporary ArrayList is allocated, which can be wasteful), or may be
     * an existing ArrayList of Coord (which will be cleared if it has any contents). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param resistanceMap a resistance map as produced by {@link DungeonUtility#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ArrayList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(int startX, int startY, int targetX, int targetY,
                                    double[][] resistanceMap, ArrayList<Coord> buffer){
        return reachable(startX, startY, targetX, targetY, 0x7FFFFFFF, resistanceMap, buffer);
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code maxLength} and {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link DungeonUtility#generateResistances(char[][])} or {@link DungeonUtility#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case a temporary ArrayList is allocated, which can be wasteful), or may be
     * an existing ArrayList of Coord (which will be cleared if it has any contents). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param maxLength the maximum permitted length of a line of sight
     * @param resistanceMap a resistance map as produced by {@link DungeonUtility#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ArrayList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(int startX, int startY, int targetX, int targetY, int maxLength,
                                    double[][] resistanceMap, ArrayList<Coord> buffer) {
        int dx = targetX - startX, dy = targetY - startY, nx = Math.abs(dx), ny = Math.abs(dy);
        int signX = dx >> 31 | 1, signY = dy >> 31 | 1, x = startX, y = startY;

        int dist = nx + ny;
        if(buffer == null) {
            buffer = new ArrayList<>(dist + 1);
        }
        else {
            buffer.clear();
        }
        if(maxLength <= 0) return false;

        if(startX == targetX && startY == targetY) {
            buffer.add(Coord.get(startX, startY));
            return true; // already at the point; we can see our own feet just fine!
        }
        double decay = 1.0 / dist;
        double currentForce = 1.0;

        for (int ix = 0, iy = 0; (ix <= nx || iy <= ny) && buffer.size() < maxLength; ) {
            buffer.add(Coord.get(x, y));
            if (x == targetX && y == targetY) {
                return true;
            }

            if (x != startX || y != startY) { //don't discount the start location even if on resistant cell
                currentForce -= resistanceMap[x][y];
            }
            currentForce -= decay;
            if (currentForce <= -0.001) {
                return false; //too much resistance
            }

            if ((0.5 + ix) / nx < (0.5 + iy) / ny) {
                x += signX;
                ix++;
            } else {
                y += signY;
                iy++;
            }
        }
        return false;//never got to the target point
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap} to determine whether
     * the line of sight is obstructed, without storing the line of points along the way. {@code resistanceMap} must not
     * be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link DungeonUtility#generateResistances(char[][])} or {@link DungeonUtility#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param start the starting point
     * @param target the target point
     * @param resistanceMap a resistance map as produced by {@link DungeonUtility#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(Coord start, Coord target, double[][] resistanceMap){
        return reachable(start.x, start.y, target.x, target.y, 0x7FFFFFFF, resistanceMap);
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap} to determine whether
     * the line of sight is obstructed, without storing the line of points along the way. {@code resistanceMap} must not
     * be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link DungeonUtility#generateResistances(char[][])} or {@link DungeonUtility#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param resistanceMap a resistance map as produced by {@link DungeonUtility#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(int startX, int startY, int targetX, int targetY,
                                    double[][] resistanceMap){
        return reachable(startX, startY, targetX, targetY, 0x7FFFFFFF, resistanceMap);
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code maxLength} and {@code resistanceMap}
     * to determine whether the line of sight is obstructed, without storing the line of points along the way.
     * {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link DungeonUtility#generateResistances(char[][])} or {@link DungeonUtility#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param maxLength the maximum permitted length of a line of sight
     * @param resistanceMap a resistance map as produced by {@link DungeonUtility#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(int startX, int startY, int targetX, int targetY, int maxLength,
                                    double[][] resistanceMap) {
        if(maxLength <= 0) return false;

        int dx = targetX - startX, dy = targetY - startY, nx = Math.abs(dx), ny = Math.abs(dy);
        int signX = dx >> 31 | 1, signY = dy >> 31 | 1, x = startX, y = startY;

        int dist = nx + ny, traveled = 0;
        if(startX == targetX && startY == targetY) {
            return true; // already at the point; we can see our own feet just fine!
        }
        double decay = 1.0 / dist;
        double currentForce = 1.0;

        for (int ix = 0, iy = 0; (ix <= nx || iy <= ny) && traveled < maxLength; ) {
            ++traveled;
            if (x == targetX && y == targetY) {
                return true;
            }

            if (x != startX || y != startY) { //don't discount the start location even if on resistant cell
                currentForce -= resistanceMap[x][y];
            }
            currentForce -= decay;
            if (currentForce <= -0.001) {
                return false; //too much resistance
            }

            if ((0.5 + ix) / nx < (0.5 + iy) / ny) {
                x += signX;
                ix++;
            } else {
                y += signY;
                iy++;
            }
        }
        return false;//never got to the target point
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
