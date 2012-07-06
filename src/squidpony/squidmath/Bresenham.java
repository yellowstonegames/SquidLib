package squidpony.squidmath;

import java.util.LinkedList;

/**
 * @author Lewis Potter Derived from Bob Pendletons C algorithm, which is in
 *         turn derived from Paul Heckberts algorithm ("Digital Line Drawing"
 *         from "Graphics Gems", Academic Press, 1990
 */
public class Bresenham {
    /** Returns 0 for 0, -1 for negative parameters and 1 for positive
     * parameters.
     */
    private static int zsgn(int a) {
        return ((a < 0) ? -1 : (a > 0) ? 1 : 0);
    }

    /**
     * Generates a 3D Bresenham line between two points.
     *
     * @param a Point to start from. This will be the first element of the list
     * @param b Point to end at. This will be the last element of the list.
     * @return A list of points between a and b.
     */
    public static LinkedList<Coord3D> line3d(Coord3D a, Coord3D b) {
        return line3d(a.x, a.y, a.z, b.x, b.y, b.z);
    }

    /**
     * Generates a 3D Bresenham line between the given coordinates.
     * 
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return 
     */
    public static LinkedList<Coord3D> line3d(int x1, int y1, int z1, int x2, int y2, int z2) {
        LinkedList<Coord3D> result = new LinkedList<Coord3D>();

        int xd, yd, zd;
        int x, y, z;
        int ax, ay, az;
        int sx, sy, sz;
        int dx, dy, dz;

        dx = x2 - x1;
        dy = y2 - y1;
        dz = z2 - z1;

        ax = Math.abs(dx) << 1;
        ay = Math.abs(dy) << 1;
        az = Math.abs(dz) << 1;

        sx = zsgn(dx);
        sy = zsgn(dy);
        sz = zsgn(dz);

        x = x1;
        y = y1;
        z = z1;

        if (ax >= Math.max(ay, az)) /* x dominant */ {
            yd = ay - (ax >> 1);
            zd = az - (ax >> 1);
            for (;;) {
                result.add(new Coord3D(x, y, z));
                if (x == x2) {
                    return result;
                }

                if (yd >= 0) {
                    y += sy;
                    yd -= ax;
                }

                if (zd >= 0) {
                    z += sz;
                    zd -= ax;
                }

                x += sx;
                yd += ay;
                zd += az;
            }
        } else if (ay >= Math.max(ax, az)) /* y dominant */ {
            xd = ax - (ay >> 1);
            zd = az - (ay >> 1);
            for (;;) {
                result.add(new Coord3D(x, y, z));
                if (y == y2) {
                    return result;
                }

                if (xd >= 0) {
                    x += sx;
                    xd -= ay;
                }

                if (zd >= 0) {
                    z += sz;
                    zd -= ay;
                }

                y += sy;
                xd += ax;
                zd += az;
            }
        } else if (az >= Math.max(ax, ay)) /* z dominant */ {
            xd = ax - (az >> 1);
            yd = ay - (az >> 1);
            for (;;) {
                result.add(new Coord3D(x, y, z));
                if (z == z2) {
                    return result;
                }

                if (xd >= 0) {
                    x += sx;
                    xd -= az;
                }

                if (yd >= 0) {
                    y += sy;
                    yd -= az;
                }

                z += sz;
                xd += ax;
                yd += ay;
            }
        }
        return result;
    }
}
