package squidpony.squidmath;

import java.awt.Point;

/**
 * Generic three dimensional coordinate class.
 *
 * @author Lewis Potter
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Point3D extends Point {

    public int z;

    /**
     * Creates a three dimensional coordinate with the given location.
     *
     * @param x
     * @param y
     * @param z
     */
    public Point3D(int x, int y, int z) {
        super(x, y);
        this.z = z;
    }

    /**
     * Returns the linear distance between this coordinate point and the
     * provided one.
     *
     * @param other
     * @return
     */
    public double distance(Point3D other) {
        return Math.sqrt(squareDistance(other));
    }

    /**
     * Returns the square of the linear distance between this coordinate point
     * and the provided one.
     *
     * @param other
     * @return
     */
    public double squareDistance(Point3D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Returns the Manhattan distance between this point and the provided one.
     * The Manhattan distance is the distance between each point on each
     * separate axis all added together.
     *
     * @param other
     * @return
     */
    public int manhattanDistance(Point3D other) {
        int distance = Math.abs(x - other.x);
        distance += Math.abs(y - other.y);
        distance += Math.abs(z - other.z);
        return distance;
    }

    /**
     * Returns the largest difference between the two points along any one axis.
     *
     * @param other
     * @return
     */
    public int maxAxisDistance(Point3D other) {
        return Math.max(Math.max(Math.abs(x - other.x), Math.abs(y - other.y)), Math.abs(z - other.z));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + x;
        hash = 73 * hash + y;
        hash = 73 * hash + z;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Point3D) {
            Point3D other = (Point3D) o;
            return x == other.x && y == other.y && z == other.z;
        } else {
            return false;
        }
    }
}
