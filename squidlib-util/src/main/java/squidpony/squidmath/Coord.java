package squidpony.squidmath;

import squidpony.squidgrid.Direction;

import java.util.Arrays;

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

    /**
     * Provided for compatibility with earlier code that used the AWT Point API.
     * @return this Coord, without changes
     */
    public Coord getLocation()
    {
        return this;
    }

    /**
     * Takes this Coord, adds x to its x and y to its y, and returns the Coord at that position.
     * @param x the amount of x distance to move
     * @param y the amount of y distance to move
     * @return a Coord (usually cached and not a new instance) that has been moved the specified distance
     */
    public Coord translate(int x, int y)
    {
        return get(this.x + x, this.y + y);
    }
    /**
     * Takes this Coord, adds x to its x and y to its y, limiting x from 0 to width and limiting y from 0 to height,
     * and returns the Coord at that position.
     * @param x the amount of x distance to move
     * @param y the amount of y distance to move
     * @param width one higher than the maximum x value this can use; typically the length of an array
     * @param height one higher than the maximum y value this can use; typically the length of an array
     * @return a Coord (usually cached and not a new instance) that has been moved the specified distance
     */
    public Coord translateCapped(int x, int y, int width, int height)
    {
        return get(Math.min(Math.max(0, this.x + x), width - 1), Math.min(Math.max(0, this.y + y), height - 1));
    }

    /**
     * Separately combines the x and y positions of this Coord and other, producing a different Coord as their "sum."
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x + other.x; y = this.y + other.y}
     */
    public Coord add(Coord other)
    {
        return get(x + other.x, y + other.y);
    }

    /**
     * Separately adds the x and y positions of this Coord to operand, producing a different Coord as their
     * "sum."
     * @param operand a value to add each of x and y to
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x + operand; y = this.y + operand}
     */
    public Coord add(int operand)
    {
        return get(x + operand, y + operand);
    }

    /**
     * Separately adds the x and y positions of this Coord to operand, rounding to the nearest int for each of x
     * and y and producing a different Coord as their "sum."
     * @param operand a value to add each of x and y to
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x + operand; y = this.y +
     *          operand}, with both x and y rounded accordingly
     */
    public Coord add(double operand)
    {
        return get((int)Math.round(x + operand), (int)Math.round(y + operand));
    }

    /**
     * Separately subtracts the x and y positions of other from this Coord, producing a different Coord as their
     * "difference."
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x - other.x; y = this.y - other.y}
     */
    public Coord subtract(Coord other)
    {
        return get(x - other.x, y - other.y);
    }

    /**
     * Separately subtracts operand from the x and y positions of this Coord, producing a different Coord as their
     * "difference."
     * @param operand a value to subtract from each of x and y
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x - operand; y = this.y - operand}
     */
    public Coord subtract(int operand)
    {
        return get(x - operand, y - operand);
    }

    /**
     * Separately subtracts operand from the x and y positions of this Coord, rounding to the nearest int for each of x
     * and y and producing a different Coord as their "difference."
     * @param operand a value to subtract from each of x and y
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x - operand; y = this.y -
     *          operand}, with both x and y rounded accordingly
     */
    public Coord subtract(double operand)
    {
        return get((int)Math.round(x - operand), (int)Math.round(y - operand));
    }
    /**
     * Separately multiplies the x and y positions of other from this Coord, producing a different Coord as their
     * "product."
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x * other.x; y = this.y * other.y}
     */
    public Coord multiply(Coord other)
    {
        return get(x * other.x, y * other.y);
    }
    /**
     * Separately multiplies the x and y positions of this Coord by operand, producing a different Coord as their
     * "product."
     * @param operand a value to multiply each of x and y by
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x * operand; y = this.y * operand}
     */
    public Coord multiply(int operand)
    {
        return get(x * operand, y * operand);
    }

    /**
     * Separately multiplies the x and y positions of this Coord by operand, rounding to the nearest int for each of x
     * and y and producing a different Coord as their "product."
     * @param operand a value to multiply each of x and y by
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x * operand; y = this.y *
     *          operand}, with both x and y rounded accordingly
     */
    public Coord multiply(double operand)
    {
        return get((int)Math.round(x * operand), (int)Math.round(y * operand));
    }

    /**
     * Separately divides the x and y positions of this Coord by other, producing a different Coord as their
     * "quotient." If other has 0 for x or y, this will throw an exception, as dividing by 0 is expected to do.
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x / other.x; y = this.y / other.y}
     */
    public Coord divide(Coord other)
    {
        return get(x / other.x, y / other.y);
    }
    /**
     * Separately divides the x and y positions of this Coord by operand, producing a different Coord as their
     * "quotient." If operand is 0, this will throw an exception, as dividing by 0 is expected to do.
     * @param operand a value to divide each of x and y by
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x / operand; y = this.y / operand}
     */
    public Coord divide(int operand)
    {
        return get(x / operand, y / operand);
    }

    /**
     * Separately divides the x and y positions of this Coord by operand, flooring to a lower int for each of x and
     * y and producing a different Coord as their "quotient." If operand is 0.0, expect strange results (infinity and
     * NaN are both possibilities).
     * @param operand a value to divide each of x and y by
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x / operand; y = this.y /
     *          operand}, with both x and y rounded accordingly
     */
    public Coord divide(double operand)
    {
        return get((int)(x / operand), (int)(y / operand));
    }

    /**
     * Separately divides the x and y positions of this Coord by operand, rounding to the nearest int for each of x and
     * y and producing a different Coord as their "quotient." If operand is 0.0, expect strange results (infinity and
     * NaN are both possibilities).
     * @param operand a value to divide each of x and y by
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x / operand; y = this.y /
     *          operand}, with both x and y rounded accordingly
     */
    public Coord divideRounding(double operand)
    {
        return get((int)Math.round(x / operand), (int)Math.round(y / operand));
    }

    /**
     * Separately averages the x and y positions of this Coord with other, producing a different Coord as their
     * "midpoint."
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) halfway between this and other, rounded nearest.
     */
    public Coord average(Coord other)
    {
        return get(Math.round((x + other.x) / 2.0f), Math.round((y + other.y) / 2.0f));
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

	/**
	 * @param c
	 * @return Whether {@code this} is adjacent to {@code c}. Not that a cell is
	 *         not adjacent to itself with this method.
	 */
	public boolean isAdjacent(Coord c) {
		switch (Math.abs(x - c.x)) {
		case 0:
			return Math.abs(y - c.y) == 1;
		case 1:
			return y == c.y || Math.abs(y - c.y) == 1;
		default:
			return false;
		}
	}

	/**
	 * Precondition: {@code this} is {@link #isAdjacent(Coord) adjacent} to
	 * {@code adjacent}.
	 * 
	 * @param adjacent
	 *            A {@link Coord} that is {@link #isAdjacent(Coord) adjacent} to
	 *            {@code this}.
	 * @return The direction to go from {@code this} to {@code adjacent} i.e.
	 *         the direction {@code d} such that {@code translate(this, d)}
	 *         yields {@code adjacent}.
	 * @throws IllegalStateException
	 *             If {@code this} isn't adjacent to {@code adjacent}.
	 */
	/* KISS implementation */
	public Direction toGoTo(Coord adjacent) {
		assert isAdjacent(adjacent);
		for (Direction d : Direction.values()) {
			/* Not calling #translate, to avoid calling the cache */
			if (x + d.deltaX == adjacent.x && y + d.deltaY == adjacent.y)
				return d;
		}
		throw new IllegalStateException(String.format("%s is not adjacent to %s", this, adjacent));
	}

    /**
     * Returns true if x is between 0 (inclusive) and width (exclusive) and y is between 0 (inclusive) and height
     * (exclusive), false otherwise.
     * @param width the upper limit on x to check, exclusive
     * @param height the upper limit on y to check, exclusive
     * @return true if this Coord is within the limits of width and height and has non-negative x and y
     */
    public boolean isWithin(int width, int height)
    {
        return x >= 0 && y >= 0 && x < width && y < height;
    }
    /**
     * Returns true if x is between minX (inclusive) and maxX (exclusive) and y is between minY (inclusive) and maxY
     * (exclusive), false otherwise.
     * @param minX the lower limit on x to check, inclusive
     * @param minY the lower limit on y to check, inclusive
     * @param maxX the upper limit on x to check, exclusive
     * @param maxY the upper limit on y to check, exclusive
     * @return true if this Coord is within the limits of the given parameters
     */
    public boolean isWithinRectangle(int minX, int minY, int maxX, int maxY)
    {
        return x >= minX && y >= minY && x < maxX && y < maxY;
    }
    public int getX() {
        return x;
    }

    public Coord setX(int x) {
        return get(x, y);
    }

    public int getY() {
        return y;
    }

    public Coord setY(int y) {
        return get(x, y);
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
