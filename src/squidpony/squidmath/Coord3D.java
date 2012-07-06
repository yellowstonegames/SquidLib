package squidpony.squidmath;

/**
 * Generic three dimensional coordinate class.
 *
 * @author Lewis Potter
 * @author Eben Howard - http://squidpony.com
 */
public class Coord3D {
    public int x, y, z;

    /**
     * Creates a three dimensional coordinate with the given location.
     * 
     * @param x
     * @param y
     * @param z 
     */
    public Coord3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns the linear distance between this coordinate point and the provided one.
     * 
     * @param other
     * @return 
     */
    public double distance(Coord3D other) {
        return Math.sqrt(squareDistance(other));
    }

    /**
     * Returns the square of the linear distance between this coordinate
     * point and the provided one.
     * 
     * @param other
     * @return 
     */
    public double squareDistance(Coord3D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Returns the Manhattan distance between this point and the provided one.
     * The Manhattan distance is the distance between each point on each separate
     * axis all added together.
     * 
     * @param other
     * @return 
     */
    public int manhattanDistance(Coord3D other) {
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
    public int maxAxisDistance(Coord3D other) {
        return Math.max(Math.max(Math.abs(x - other.x), Math.abs(y - other.y)), Math.abs(z - other.z));
    }
    

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + this.x;
        hash = 73 * hash + this.y;
        hash = 73 * hash + this.z;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Coord3D) {
            Coord3D other = (Coord3D) o;
            return x == other.x && y == other.y && z == other.z;
        } else {
            return false;
        }
    }
}
