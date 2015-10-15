package squidpony.squidmath;

import java.util.Arrays;

import squidpony.squidgrid.Direction;

/**
 * A 2D coordinate.
 * 
 * Created by Tommy Ettinger on 8/12/2015.
 */
public class Coord implements java.io.Serializable {
    private static final long serialVersionUID = 300L;

	/** The x-coordinate. */
	public final int x;

	/** The y-coordinate (the ordinate) */
	public final int y;

    protected Coord()
    {
        this(0, 0);
    }
    protected Coord(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    public static Coord get(int x, int y)
    {
        if(x >= -3 && y >= -3 && x < POOL.length - 3 && y < POOL[x + 3].length - 3)
            return POOL[x + 3][y + 3];
        else return new Coord(x, y);
    }

    public Coord getLocation()
    {
        return this;
    }
    public Coord translate(int x, int y)
    {
        return get(this.x + x, this.y + y);
    }

	/**
	 * @param d
	 *            A non-{@code null} direction.
	 * @return The coordinate obtained by applying {@code d} on {@code this}.
	 */
	public Coord translate(Direction d) {
		return Coord.get(x + d.deltaX, y + d.deltaY);
	}

	/**
	 * @param i
	 * @return {@code (x*i,y*i)}.
	 */
	public Coord scale(int i) {
		return Coord.get(x * i, y * i);
	}

	/**
	 * @param i
	 * @return {@code (x*i,y*j)}.
	 */
	public Coord scale(int i, int j) {
		return Coord.get(x * i, y * j);
	}

    public double distance(double x2, double y2)
    {
        return Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
    }
    public double distance(Coord co)
    {
        return Math.sqrt((co.x - x) * (co.x - x) + (co.y - y) * (co.y - y));
    }
    public double distanceSq(double x2, double y2)
    {
        return (x2 - x) * (x2 - x) + (y2 - y) * (y2 - y);
    }
    public double distanceSq(Coord co)
    {
        return (co.x - x) * (co.x - x) + (co.y - y) * (co.y - y);
    }

    public int getX() {
        return x;
    }

    public Coord setX(int x) {
        return get(x, this.y);
    }

    public int getY() {
        return y;
    }

    public Coord setY(int y) {
        return get(this.x, y);
    }

    @Override
    public String toString()
    {
        return "Coord (x " + x + ", y " + y + ")";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 113 * hash + x;
        hash = 113 * hash + y;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Coord) {
            Coord other = (Coord) o;
            return x == other.x && y == other.y;
        } else {
            return false;
        }
    }
    private static Coord[][] POOL = new Coord[259][259];
    static {
        int width = POOL.length, height = POOL[0].length;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                POOL[i][j] = new Coord(i - 3, j - 3);
            }
        }
    }
    public static void expandPool(int xIncrease, int yIncrease)
    {
        if(xIncrease < 0 || yIncrease < 0)
            return;
        int width = POOL.length, height = POOL[0].length;
        POOL = Arrays.copyOf(POOL, width + xIncrease);
        for (int i = 0; i < width + xIncrease; i++) {
            POOL[i] = Arrays.copyOf(POOL[i], height + yIncrease);
            for (int j = 0; j < height + yIncrease; j++) {
                if(POOL[i][j] == null) POOL[i][j] = new Coord(i - 3, j - 3);
            }
        }
    }
}
