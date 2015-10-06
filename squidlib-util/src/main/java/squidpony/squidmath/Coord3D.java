package squidpony.squidmath;

/**
 * Generic three dimensional coordinate class.
 * Not cached in a pool because it is rarely used internally.
 * @author Lewis Potter
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Coord3D extends Coord {

	public int z;

	private static final long serialVersionUID = 1835370798982845336L;

    /**
     * Creates a three dimensional coordinate with the given location.
     *
     * @param x
     * @param y
     * @param z
     */
    public Coord3D(int x, int y, int z) {
        super(x, y);
        this.z = z;
    }

    public static Coord3D get(int x, int y, int z)
    {
        return new Coord3D(x, y, z);
    }
    /**
     * Returns the linear distance between this coordinate point and the
     * provided one.
     *
     * @param other
     * @return
     */
    public double distance(Coord3D other) {
        return Math.sqrt(squareDistance(other));
    }

    /**
     * Returns the square of the linear distance between this coordinate point
     * and the provided one.
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
     * The Manhattan distance is the distance between each point on each
     * separate axis all added together.
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
        hash = 73 * hash + x;
        hash = 73 * hash + y;
        hash = 73 * hash + z;
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
