package squidpony.squidmath;

import squidpony.squidgrid.Direction;

import java.io.Serializable;

/**
 * A 2D coordinate with (constant) x and y fields. Coord objects are immutable; a single pool of Coord values, with
 * x and y each ranging from -3 to 255, is shared by all users of Coord. This pool helps reduce pressure on the
 * garbage collector when many Coord values would have been created for some purpose and quickly discarded; instead
 * of creating a new Coord with a constructor, you use the static method {@link #get(int, int)}, which retrieves an
 * already-existing Coord from the pool if possible, and always returns a usable Coord.
 * <br>
 * The Coord class is a fundamental part of SquidLib; any class that uses positions on a grid makes use of it here.
 * It finds usage naturally in classes throughout {@link squidpony.squidgrid}, with {@link squidpony.squidgrid.zone}
 * providing an abstraction around groups of Coord and {@link squidpony.squidgrid.iterator} providing various ways to
 * iterate through the Coords that make up a larger shape. In this package, {@link squidpony.squidmath}, a few classes
 * should be pointed out. {@link CoordPacker} is a class with all static methods that provides various ways to compress
 * the memory usage of regions made of many Coord values (and can be constructed in other ways but still provide Coords
 * later), but since Coords don't use much memory anyway, the real use of the class is for manipulating the shapes and
 * sizes of the regions those Coords are part of. {@link GreasedRegion} has similar functionality to CoordPacker, but
 * where CoordPacker is purely static, taking and returning regions as encoded, usually-low-memory-cost arrays of
 * {@code short} that it considers immutable, a GreasedRegion is a mutable object that allows the same region-altering
 * techniques to be applied in-place in a way that is relatively (very) low-time-cost. If deciding between the two,
 * GreasedRegion should usually be preferred, and CoordPacker cannot actually be used when storing regions in larger
 * than a 256x256 space (usually when the Coord pool has been expanded; see below); GreasedRegion can store potentially
 * large positions.
 * <br>
 * More on the Coord pool used by this class:  Coords can't always be retrieved from the pool; Coord.get constructs a
 * new Coord if one of x or y is unusually large (greater than 255) or too negative (below -3). The upper limit of 255
 * is not a hard rule; you can increase the limit on the pool by calling {@link #expandPoolTo(int, int)} or
 * {@link #expandPool(int, int)}, which cause more memory to be spent initially on storing Coords but can save memory
 * or ease GC pressure over the long term by preventing duplicate Coords from being created many times. The pool can
 * never shrink because allowing that would cause completely unpredictable results if existing Coords were in use, or
 * could easily cause crashes on Android after resuming an application that had previously shrunken the pool due to
 * platform quirks. Long story short, you should only expand the pool size when your game needs a larger set of 2D
 * points it will commonly use, and in most cases you shouldn't need to change it at all.
 * 
 * Created by Tommy Ettinger on 8/12/2015.
 */
public class Coord implements Serializable {
    private static final long serialVersionUID = 300L;

	/** The x-coordinate. */
	public final int x;

	/** The y-coordinate (the ordinate) */
	public final int y;

    protected Coord()
    {
        this(0, 0);
    }
    protected Coord(final int x, final int y)
    {
        this.x = x;
        this.y = y;
    }
    public static Coord get(final int x, final int y)
    {
        if(x >= -3 && y >= -3 && x < POOL.length - 3 && y < POOL[x + 3].length - 3)
            return POOL[x + 3][y + 3];
        else return new Coord(x, y);
    }

	/**
     * Gets the angle in degrees to go between two Coords; 0 is up.
	 * @param from the starting Coord to measure from
	 * @param to the ending Coord to measure to
	 * @return The degree from {@code from} to {@code to}; 0 is up
	 */
	public static double degrees(final Coord from, final Coord to) {
		final int x = to.x - from.x;
		final int y = to.y - from.y;
		double degree = Math.toDegrees(NumberTools.atan2(y, x));
		degree += 450;// rotate to all positive and 0 is up
		degree %= 360;// normalize
		return degree;
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
    public Coord translate(final int x, final int y)
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
    public Coord translateCapped(final int x, final int y, final int width, final int height)
    {
        return get(Math.min(Math.max(0, this.x + x), width - 1), Math.min(Math.max(0, this.y + y), height - 1));
    }

    /**
     * Separately combines the x and y positions of this Coord and other, producing a different Coord as their "sum."
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x + other.x; y = this.y + other.y}
     */
    public Coord add(final Coord other)
    {
        return get(x + other.x, y + other.y);
    }

    /**
     * Separately adds the x and y positions of this Coord to operand, producing a different Coord as their
     * "sum."
     * @param operand a value to add each of x and y to
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x + operand; y = this.y + operand}
     */
    public Coord add(final int operand)
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
    public Coord add(final double operand)
    {
        return get((int)Math.round(x + operand), (int)Math.round(y + operand));
    }

    /**
     * Separately subtracts the x and y positions of other from this Coord, producing a different Coord as their
     * "difference."
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x - other.x; y = this.y - other.y}
     */
    public Coord subtract(final Coord other)
    {
        return get(x - other.x, y - other.y);
    }

    /**
     * Separately subtracts operand from the x and y positions of this Coord, producing a different Coord as their
     * "difference."
     * @param operand a value to subtract from each of x and y
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x - operand; y = this.y - operand}
     */
    public Coord subtract(final int operand)
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
    public Coord subtract(final double operand)
    {
        return get((int)Math.round(x - operand), (int)Math.round(y - operand));
    }
    /**
     * Separately multiplies the x and y positions of other from this Coord, producing a different Coord as their
     * "product."
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x * other.x; y = this.y * other.y}
     */
    public Coord multiply(final Coord other)
    {
        return get(x * other.x, y * other.y);
    }
    /**
     * Separately multiplies the x and y positions of this Coord by operand, producing a different Coord as their
     * "product."
     * @param operand a value to multiply each of x and y by
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x * operand; y = this.y * operand}
     */
    public Coord multiply(final int operand)
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
    public Coord multiply(final double operand)
    {
        return get((int)Math.round(x * operand), (int)Math.round(y * operand));
    }

    /**
     * Separately divides the x and y positions of this Coord by other, producing a different Coord as their
     * "quotient." If other has 0 for x or y, this will throw an exception, as dividing by 0 is expected to do.
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x / other.x; y = this.y / other.y}
     */
    public Coord divide(final Coord other)
    {
        return get(x / other.x, y / other.y);
    }
    /**
     * Separately divides the x and y positions of this Coord by operand, producing a different Coord as their
     * "quotient." If operand is 0, this will throw an exception, as dividing by 0 is expected to do.
     * @param operand a value to divide each of x and y by
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x / operand; y = this.y / operand}
     */
    public Coord divide(final int operand)
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
    public Coord divide(final double operand)
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
    public Coord divideRounding(final double operand)
    {
        return get((int)Math.round(x / operand), (int)Math.round(y / operand));
    }

    /**
     * Separately averages the x and y positions of this Coord with other, producing a different Coord as their
     * "midpoint."
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) halfway between this and other, rounded nearest.
     */
    public Coord average(final Coord other)
    {
        return get(Math.round((x + other.x) / 2.0f), Math.round((y + other.y) / 2.0f));
    }
	/**
	 * @param d
	 *            A non-{@code null} direction.
	 * @return The coordinate obtained by applying {@code d} on {@code this}.
	 */
	public Coord translate(final Direction d) {
		return Coord.get(x + d.deltaX, y + d.deltaY);
	}

	/**
	 * @param i
	 * @return {@code (x*i,y*i)}.
	 */
	public Coord scale(final int i) {
		return Coord.get(x * i, y * i);
	}

	/**
	 * @param i
	 * @return {@code (x*i,y*j)}.
	 */
	public Coord scale(final int i, final int j) {
		return Coord.get(x * i, y * j);
	}

    public double distance(final double x2, final double y2)
    {
        return Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
    }
    public double distance(final Coord co)
    {
        return Math.sqrt((co.x - x) * (co.x - x) + (co.y - y) * (co.y - y));
    }
    public double distanceSq(final double x2, final double y2)
    {
        return (x2 - x) * (x2 - x) + (y2 - y) * (y2 - y);
    }
    public double distanceSq(final Coord co) { return (co.x - x) * (co.x - x) + (co.y - y) * (co.y - y); }

    /**
     * Gets a Coord based off this instance but with odd values for x and/or y decreased to the nearest even number.
     * May be useful for thin-wall maps as produced by {@link squidpony.squidgrid.mapping.ThinDungeonGenerator} and used
     * with {@link squidpony.squidgrid.Adjacency.ThinWallAdjacency}.
     * @return a Coord (probably from the pool) with even x and even y, changing (decrementing) only if they are odd
     */
    public Coord makeEven()
    {
        return get(x & -2, y & -2);
    }

    /**
     * Gets a Coord based off this instance but with even values for x and/or y increased to the nearest odd number.
     * May be useful for thin-wall maps as produced by {@link squidpony.squidgrid.mapping.ThinDungeonGenerator} and used
     * with {@link squidpony.squidgrid.Adjacency.ThinWallAdjacency}.
     * @return a Coord (probably from the pool) with odd x and odd y, changing (incrementing) only if they are even
     */
    public Coord makeOdd() {
        return get(x | 1, y | 1);
    }

	/**
	 * @param c
	 * @return Whether {@code this} is adjacent to {@code c}. Not that a cell is
	 *         not adjacent to itself with this method.
	 */
	public boolean isAdjacent(final Coord c) {
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
	/* different implementation than before, closer to isAdjacent() */
	public Direction toGoTo(final Coord adjacent) {
        switch (adjacent.x - x) {
            case 0:
                switch (adjacent.y - y)
                {
                    case -1: return Direction.UP;
                    case 1: return Direction.DOWN;
                    default:
                        throw new IllegalStateException(this + " is not adjacent to " + adjacent);
                }
            case 1:
                switch (adjacent.y - y)
                {
                    case -1: return Direction.UP_RIGHT;
                    case 0: return Direction.RIGHT;
                    case 1: return Direction.DOWN_RIGHT;
                    default:
                        throw new IllegalStateException(this + " is not adjacent to " + adjacent);
                }
            case -1:
                switch (adjacent.y - y)
                {
                    case -1: return Direction.UP_LEFT;
                    case 0: return Direction.LEFT;
                    case 1: return Direction.DOWN_LEFT;
                    default:
                        throw new IllegalStateException(this + " is not adjacent to " + adjacent);
                }
            default:
                throw new IllegalStateException(this + " is not adjacent to " + adjacent);
        }
	}

    /**
     * Returns true if x is between 0 (inclusive) and width (exclusive) and y is between 0 (inclusive) and height
     * (exclusive), false otherwise.
     * @param width the upper limit on x to check, exclusive
     * @param height the upper limit on y to check, exclusive
     * @return true if this Coord is within the limits of width and height and has non-negative x and y
     */
    public boolean isWithin(final int width, final int height)
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

    public Coord setX(final int x) {
        return get(x, y);
    }

    public int getY() {
        return y;
    }

    public Coord setY(final int y) {
        return get(x, y);
    }

    @Override
    public String toString()
    {
        return "(" + x + "," + y + ")";
    }

    /**
     * Gets the hash code for this Coord; does not use the standard "auto-complete" style of hash that most IDEs will
     * generate, but instead uses a highly-specific technique based on {@link Lathe32RNG}'s random int generation, which
     * also has two independent ints it uses like a Coord's x and y. Unlike Lathe32RNG, this uses only bitwise
     * operations (not even addition). This guarantees it will behave correctly on GWT (Lathe32RNG uses an alternate
     * source file on GWT).  It also manages to get extremely low collision rates under many circumstances, and very
     * frequently manages to avoid colliding on more than 25% of Coords (making the load factor of most hash-based
     * collections fine at a default of 0.75) while often having 0 collisions with some data sets.
     * <br>
     * For reference, this was tested on several rectangular sections of Coords of varying density, with sizes from 64
     * to 512 for x and y separately. On a total of 7,830,980 Coords, an older hashCode() this used got 2,159,553
     * collisions (27.6% rate), the previous similar hashCode() this used got 1,514,191 collisions (19.3% rate), a naive
     * {@link java.util.Objects#hash(Object...)} approach on x and y got 6,163,604 collisions (78.7% rate), and this
     * method gets 179,922 collisions (2.3% rate). A perfect general hashCode isn't possible, but for the common usage
     * of Coords (usually x and y are no greater than 511 and are rarely negative, etc.) we can do better than
     * {@code return x * 31 + y;} (which is close to the high-collision Objects.hash way).
     * <br>
     * This changed at least five times in SquidLib's history. In general, you shouldn't rely on hashCodes to stay the
     * same across platforms and versions, whether for the JDK or this library. SquidLib (tries to) never depend on the
     * unpredictable ordering of some hash-based collections like HashSet and HashMap, instead using its own
     * {@link OrderedSet} and {@link OrderedMap}; if you use the ordered kinds, then the only things that matter about
     * this hash code are that it's fast (it's fast enough), it's cross-platform compatible (this version avoids using
     * long values, which are slow on GWT, and is carefully written to behave the same on GWT as desktop) and that it
     * doesn't collide often (which is now much more accurate than in earlier versions of this method).
     * @return an int that should, for most different Coord values, be significantly different from the other hash codes
     */
    @Override
    public int hashCode() {
        int r = x ^ y;
        r ^= (x << 13 | x >>> 19) ^ (r << 5) ^ (r << 28 | r >>> 4);
        r = x ^ (r << 11 | r >>> 21);
        return r ^ (r << 25 | r >>> 7);
    }

    /**
     * Something like hashCode(), but reversible with {@code Coord.decode()}. Works for Coords between roughly -256 and
     * 32000 in each of x and y, but will probably only decode to pooled Coords if x and y are both between -3 and 255
     * (inclusive for both).
     * @return an int as a unique code for this Coord
     */
    public int encode()
    {
        return ((x + 256) << 16) ^ (y + 256);
    }

    /**
     * An alternative to getting a Coord with Coord.get() only to encode() it as the next step. This doesn't create a
     * Coord in the middle step. Can be decoded with Coord.decode() to get the (x,y) Coord.
     * @param x the x position to encode
     * @param y the y position to encode
     * @return the coded int that a Coord at (x,y) would produce with encode()
     */
    public static int pureEncode(final int x, final int y)
    {
        return ((x + 256) << 16) ^ (y + 256);
    }
    /**
     * This can take an int produced by {@code someCoord.encode()} and get the original Coord back out of it. It
     * works for all pooled Coords where the pool hasn't been expanded past about 32,000 in either dimension. It even
     * works for Coords with negative x or y as well, if they are no lower than -256 in either dimension. This will
     * almost certainly fail (producing a gibberish Coord that probably won't be pooled) on hashes produced by any other
     * class, including subclasses of Coord.
     * @param code an encoded int from a Coord, but not a subclass of Coord
     * @return the Coord that gave hash as its hashCode()
     */
    public static Coord decode(final int code)
    {
        return get((code >>> 16) - 256, (code & 0xFFFF) - 256);
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

    /**
     * Gets the width of the pool used as a cache for Coords, not including negative Coords.
     * Unless expandPool() has been called, this should be 256.
     * Useful for finding the upper (exclusive) bound for x values that can be used efficiently in Coords.
     * Requesting a Coord with a x greater than or equal to this value will result in a new Coord being allocated and
     * not cached, which may cause problems with code that expects the normal reference equality of Coords to be upheld
     * and in extreme cases may require more time garbage collecting than is normally necessary.
     * @return the width of the Coord cache, disregarding negative Coords
     */
    public static int getCacheWidth()
    {
        return POOL.length - 3;
    }

    /**
     * Gets the height of the pool used as a cache for Coords, not including negative Coords.
     * Unless expandPool() has been called, this should be 256.
     * Useful for finding the upper (exclusive) bound for y values that can be used efficiently in Coords.
     * Requesting a Coord with a y greater than or equal to this value will result in a new Coord being allocated and
     * not cached, which may cause problems with code that expects the normal reference equality of Coords to be upheld
     * and in extreme cases may require more time garbage collecting than is normally necessary.
     * @return the height of the Coord cache, disregarding negative Coords
     */
    public static int getCacheHeight()
    {
        return POOL[0].length - 3;
    }

    /**
     * Enlarges the pool of cached Coords to the given width and height, and doesn't change
     * a dimension if it would be reduced in size.
     * Cached Coord values will be reused by Coord.get instead of re-allocated each time.
     * The default pool allows Coords with x and y each between -3 and 255, inclusive, to
     * be cached, and is considered to have width and height of 256 to begin with. Giving a
     * width greater than 256 will allow Coords with x greater than 255 to be cached;
     * likewise for height. If width or height is smaller than the current cache width or
     * height, that dimension will not change, but the other still may if it is valid. You
     * cannot shrink the pool size.
     * @param width the new width for the pool of cached Coords; will be ignored if smaller than the current width
     * @param height the new height for the pool of cached Coords; will be ignored if smaller than the current height
     */
    public static void expandPoolTo(final int width, final int height)
    {
        expandPool(Math.max(0, width + 3 - POOL.length), Math.max(0, height + 3 - POOL[0].length));
    }

    /**
     * Enlarges the pool of cached Coords by the given amount of expansion for x and y.
     * Cached Coord values will be reused by Coord.get instead of re-allocated each time.
     * The default pool allows Coords with x and y each between -3 and 255, inclusive, to
     * be cached, and this can increase the size in the positive direction. If either
     * xIncrease or yIncrease is negative, this method returns immediately and does nothing
     * else; the same is true of both arguments are zero. You cannot shrink the pool size.
     * @param xIncrease the amount to increase cache's width by
     * @param yIncrease the amount to increase cache's height by
     */
    public static void expandPool(final int xIncrease, final int yIncrease)
    {
        if(xIncrease < 0 || yIncrease < 0 || (xIncrease | yIncrease) == 0 )
            return;
        int width = POOL.length, height = POOL[0].length;
        Coord[][] POOL2 = new Coord[width + xIncrease][height + yIncrease];
        for (int i = 0; i < width; i++) {
            POOL2[i] = new Coord[height + yIncrease];
            System.arraycopy(POOL[i], 0, POOL2[i], 0, height);
            for (int j = 0; j < height + yIncrease; j++) {
                if(POOL2[i][j] == null) POOL2[i][j] = new Coord(i - 3, j - 3);
            }
        }
        for (int i = width; i < width + xIncrease; i++) {
            POOL2[i] = new Coord[height + yIncrease];
            for (int j = 0; j < height + yIncrease; j++) {
                POOL2[i][j] = new Coord(i - 3, j - 3);
            }
        }
        POOL = POOL2;
    }

    public Coord interpolate(Coord end, float amountTraveled) {
        return Coord.get(x + Math.round((end.x - x) * amountTraveled),
                y + Math.round((end.y - y) * amountTraveled));
    }
}
