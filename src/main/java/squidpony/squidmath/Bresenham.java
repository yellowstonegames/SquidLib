package squidpony.squidmath;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Provides a means to generate Bresenham lines in 2D and 3D.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Lewis Potter
 */
public class Bresenham {

    /**
     * Prevents any instances from being created
     */
    private Bresenham() {
    }

    /**
     * Generates a 2D Bresenham line between two points.
     *
     * @param a the starting point
     * @param b the ending point
     * @return
     */
    public static Queue<Point> line2D(Point a, Point b) {
        return line2D(a.x, a.y, b.x, b.y);
    }

    /**
     * Generates a 2D Bresenham line between two points.
     *
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param endX the x coordinate of the ending point
     * @param endY the y coordinate of the ending point
     * @return
     */
    public static Queue<Point> line2D(int startX, int startY, int endX, int endY) {
        Queue<Point> line = new LinkedList<>();
        Queue<Point3D> found = line3D(startX, startY, 0, endX, endY, 0);
        while (!found.isEmpty()) {
            line.offer(found.poll());
        }
        return line;
    }

    /**
     * Generates a 3D Bresenham line between two points.
     *
     * @param a Point to start from. This will be the first element of the list
     * @param b Point to end at. This will be the last element of the list.
     * @return A list of points between a and b.
     */
    public static Queue<Point3D> line3D(Point3D a, Point3D b) {
        return line3D(a.x, a.y, a.z, b.x, b.y, b.z);
    }

    /**
     * Generates a 3D Bresenham line between the given coordinates.
     *
     * @param startx the x coordinate of the starting point
     * @param starty the y coordinate of the starting point
     * @param startz the z coordinate of the starting point
     * @param endx the x coordinate of the starting point
     * @param endy the y coordinate of the starting point
     * @param endz the z coordinate of the starting point
     * @return
     */
    public static Queue<Point3D> line3D(int startx, int starty, int startz, int endx, int endy, int endz) {
        Queue<Point3D> result = new LinkedList<>();

        int dx = endx - startx;
        int dy = endy - starty;
        int dz = endz - startz;

        int ax = Math.abs(dx) << 1;
        int ay = Math.abs(dy) << 1;
        int az = Math.abs(dz) << 1;

        int signx = (int) Math.signum(dx);
        int signy = (int) Math.signum(dy);
        int signz = (int) Math.signum(dz);

        int x = startx;
        int y = starty;
        int z = startz;

        int deltax, deltay, deltaz;
        if (ax >= Math.max(ay, az)) /* x dominant */ {
            deltay = ay - (ax >> 1);
            deltaz = az - (ax >> 1);
            while (true) {
                result.offer(new Point3D(x, y, z));
                if (x == endx) {
                    return result;
                }

                if (deltay >= 0) {
                    y += signy;
                    deltay -= ax;
                }

                if (deltaz >= 0) {
                    z += signz;
                    deltaz -= ax;
                }

                x += signx;
                deltay += ay;
                deltaz += az;
            }
        } else if (ay >= Math.max(ax, az)) /* y dominant */ {
            deltax = ax - (ay >> 1);
            deltaz = az - (ay >> 1);
            while (true) {
                result.offer(new Point3D(x, y, z));
                if (y == endy) {
                    return result;
                }

                if (deltax >= 0) {
                    x += signx;
                    deltax -= ay;
                }

                if (deltaz >= 0) {
                    z += signz;
                    deltaz -= ay;
                }

                y += signy;
                deltax += ax;
                deltaz += az;
            }
        } else if (az >= Math.max(ax, ay)) /* z dominant */ {
            deltax = ax - (az >> 1);
            deltay = ay - (az >> 1);
            while (true) {
                result.offer(new Point3D(x, y, z));
                if (z == endz) {
                    return result;
                }

                if (deltax >= 0) {
                    x += signx;
                    deltax -= az;
                }

                if (deltay >= 0) {
                    y += signy;
                    deltay -= az;
                }

                z += signz;
                deltax += ax;
                deltay += ay;
            }
        }
        return result;
    }
}
