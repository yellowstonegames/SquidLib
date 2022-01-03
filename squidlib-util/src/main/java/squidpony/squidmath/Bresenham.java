package squidpony.squidmath;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Provides a means to generate Bresenham lines in 2D and 3D.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Lewis Potter
 * @author Tommy Ettinger
 * @author smelC
 */
public class Bresenham {

    /**
     * Prevents any instances from being created
     */
    private Bresenham() {
    }

    /**
     * Generates a 2D Bresenham line between two points. If you don't need
     * the {@link Queue} interface for the returned reference, consider
     * using {@link #line2D_(Coord, Coord)} to save some memory.
     *
     * @param a the starting point
     * @param b the ending point
     * @return The path between {@code a} and {@code b}.
     */
    public static Queue<Coord> line2D(Coord a, Coord b) {
        return line2D(a.x, a.y, b.x, b.y);
    }

    /**
     * Generates a 2D Bresenham line between two points.
     *
     * @param a the starting point
     * @param b the ending point
     * @return The path between {@code a} and {@code b}.
     */
    public static Coord[] line2D_(Coord a, Coord b) {
        return line2D_(a.x, a.y, b.x, b.y);
    }

    /**
     * Generates a 3D Bresenham line between two points.
     *
     * @param a Coord to start from. This will be the first element of the list
     * @param b Coord to end at. This will be the last element of the list.
     * @return A list of points between a and b.
     */
    public static Queue<Coord3D> line3D(Coord3D a, Coord3D b) {
        return line3D(a.x, a.y, a.z, b.x, b.y, b.z);
    }

    /**
     * Generates a 3D Bresenham line between the given coordinates.
     * Uses a Coord3D for each point; keep in mind Coord3D values are not
     * pooled like ordinary 2D Coord values, and this may cause more work
     * for the garbage collector, especially on Android, if many Coord3D
     * values are produced.
     *
     * @param startx the x coordinate of the starting point
     * @param starty the y coordinate of the starting point
     * @param startz the z coordinate of the starting point
     * @param endx the x coordinate of the starting point
     * @param endy the y coordinate of the starting point
     * @param endz the z coordinate of the starting point
     * @return a Queue (internally, an ArrayDeque) of Coord3D points along the line
     */
    public static Queue<Coord3D> line3D(int startx, int starty, int startz, int endx, int endy, int endz) {
        int dx = endx - startx;
        int dy = endy - starty;
        int dz = endz - startz;

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);
        int az = Math.abs(dz);
        Queue<Coord3D> result = new ArrayDeque<>(Math.max(Math.max(ax, ay), az));

        ax <<= 1;
        ay <<= 1;
        az <<= 1;
        
        int signx = (dx >> 31 | -dx >>> 31); // project nayuki signum
        int signy = (dy >> 31 | -dy >>> 31); // project nayuki signum
        int signz = (dz >> 31 | -dz >>> 31); // project nayuki signum

        int x = startx;
        int y = starty;
        int z = startz;

        int deltax, deltay, deltaz;
        if (ax >= Math.max(ay, az)) /* x dominant */ {
            deltay = ay - (ax >> 1);
            deltaz = az - (ax >> 1);
            while (true) {
                result.offer(new Coord3D(x, y, z));
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
                result.offer(new Coord3D(x, y, z));
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
        } else {
            deltax = ax - (az >> 1);
            deltay = ay - (az >> 1);
            while (true) {
                result.offer(new Coord3D(x, y, z));
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
    }

    /**
     * Generates a 2D Bresenham line between two points. If you don't need
     * the {@link Queue} interface for the returned reference, consider
     * using {@link #line2D_(int, int, int, int)} to save some memory.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they aren't beyond what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startx the x coordinate of the starting point
     * @param starty the y coordinate of the starting point
     * @param endx the x coordinate of the starting point
     * @param endy the y coordinate of the starting point
     * @return a Queue (internally, an ArrayDeque) of Coord points along the line
     */
    public static Queue<Coord> line2D(int startx, int starty, int endx, int endy) {
        // largest positive int for maxLength; a Queue cannot actually be given that many elements on the JVM
        return line2D(startx, starty, endx, endy, 0x7fffffff);
    }

    /**
     * Generates a 2D Bresenham line between two points, stopping early if
     * the number of Coords returned reaches maxLength. If you don't need
     * the {@link Queue} interface for the returned reference, consider
     * using {@link #line2D_(int, int, int, int, int)} to save some memory.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they aren't beyond what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startx the x coordinate of the starting point
     * @param starty the y coordinate of the starting point
     * @param endx the x coordinate of the starting point
     * @param endy the y coordinate of the starting point
     * @param maxLength the largest count of Coord points this can return; will stop early if reached
     * @return a Queue (internally, a ArrayDeque) of Coord points along the line
     */
    public static Queue<Coord> line2D(int startx, int starty, int endx, int endy, int maxLength) {
        int dx = endx - startx;
        int dy = endy - starty;

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);

        Queue<Coord> result = new ArrayDeque<>(Math.max(ax, ay));

        ax <<= 1;
        ay <<= 1;
        
        int signx = (dx >> 31 | -dx >>> 31); // project nayuki signum
        int signy = (dy >> 31 | -dy >>> 31); // project nayuki signum

        int x = startx;
        int y = starty;

        int deltax, deltay;
        if (ax >= ay) /* x dominant */ {
            deltay = ay - (ax >> 1);
            while (result.size() < maxLength) {
                result.offer(Coord.get(x, y));
                if (x == endx) {
                    return result;
                }

                if (deltay >= 0) {
                    y += signy;
                    deltay -= ax;
                }

                x += signx;
                deltay += ay;
            }
        } else /* y dominant */ {
            deltax = ax - (ay >> 1);
            while (result.size() < maxLength) {
                result.offer(Coord.get(x, y));
                if (y == endy) {
                    return result;
                }

                if (deltax >= 0) {
                    x += signx;
                    deltax -= ay;
                }


                y += signy;
                deltax += ax;
            }
        }
        return result;
    }


    /**
     * Generates a 2D Bresenham line between two points. Returns an array
     * of Coord instead of a Queue.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they aren't beyond what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startx the x coordinate of the starting point
     * @param starty the y coordinate of the starting point
     * @param endx the x coordinate of the starting point
     * @param endy the y coordinate of the starting point
     * @return an array of Coord points along the line
     */
    public static Coord[] line2D_(int startx, int starty, int endx, int endy) {
        // largest positive int for maxLength; it is extremely unlikely that this could be reached
        return line2D_(startx, starty, endx, endy, 0x7fffffff);
    }


    /**
     * Generates a 2D Bresenham line between two points, stopping early if
     * the number of Coords returned reaches maxLength.. Returns an array
     * of Coord instead of a Queue.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they aren't beyond what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startx the x coordinate of the starting point
     * @param starty the y coordinate of the starting point
     * @param endx the x coordinate of the starting point
     * @param endy the y coordinate of the starting point
     * @param maxLength the largest count of Coord points this can return; will stop early if reached
     * @return an array of Coord points along the line
     */
    public static Coord[] line2D_(int startx, int starty, int endx, int endy, int maxLength) {
        int dx = endx - startx;
        int dy = endy - starty;

        int signx = (dx >> 31 | -dx >>> 31); // project nayuki signum
        int signy = (dy >> 31 | -dy >>> 31); // project nayuki signum

        int ax = (dx = Math.abs(dx)) << 1;
        int ay = (dy = Math.abs(dy)) << 1;

        int x = startx;
        int y = starty;

        int deltax, deltay;
        if (ax >= ay) /* x dominant */ {
            deltay = ay - (ax >> 1);
            Coord[] result = new Coord[Math.min(maxLength, dx+1)];
            for (int i = 0; i <= dx && i < maxLength; i++) {
                result[i] = Coord.get(x, y);

                if (deltay >= 0) {
                    y += signy;
                    deltay -= ax;
                }

                x += signx;
                deltay += ay;
            }
            return result;
        } else /* y dominant */ {
            deltax = ax - (ay >> 1);
            Coord[] result = new Coord[Math.min(maxLength, dy+1)];
            for (int i = 0; i <= dy && i < maxLength; i++) {
                result[i] = Coord.get(x, y);

                if (deltax >= 0) {
                    x += signx;
                    deltax -= ay;
                }


                y += signy;
                deltax += ax;
            }
            return result;
        }
    }

}
