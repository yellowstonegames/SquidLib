package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A quasi-random number generator that goes through one of many sub-random sequences found by J.G. van der Corput.
 * More specifically, this offers both the normal van der Corput sequence, which only changes the state by incrementing
 * it (this works better in the normal usage as part of a 2D or 3D point generator) and a kind of scrambled van der
 * Corput sequence, where the state changes unpredictably (this works better when using this to generate 1D sequences,
 * or when the base may be larger than 7 or non-prime). In both cases, the state is internally stored in a 64-bit long
 * that is incremented once per generated number, but when scramble is true, the state is altered with something similar
 * to a Gray code before being used; more on this later if you want to read about it. The important things to know about
 * this class are: size of state affects speed (prefer smaller seeds, but quality is sometimes a bit poor at first if
 * you start at 0); the base (when given) should be prime (smaller bases tend to yield better quality, but oddly, larger
 * bases perform better); this doesn't generate very random numbers when scramble is false (which can be good
 * for making points that should not overlap), but it will seem much more random when scramble is true; this is a
 * StatefulRandomness with an additional method for generating quasi-random doubles, {@link #nextDouble()}; and there
 * are several static methods offered for convenient generation of points on the related Halton sequence (as well as
 * faster generation of doubles in the base-2 van der Corput sequence, and a special method that switches which base
 * it uses depending on the index to seem even less clearly-patterned).
 * <br>
 * This generator allows a base (also called a radix) that changes the sequence significantly; a base should be prime,
 * and this performs better in terms of time used with larger primes, though quality is also improved by preferring
 * primes that aren't very large relative to the quantity of numbers expected to be generated. Unfortunately,
 * performance is not especially similar to conventional PRNGs; smaller (positive) state values are processed more
 * quickly than larger ones, or most negative states. At least one 64-bit integer modulus and one 64-bit integer
 * division are required for any number to be produced, and the amount of both of these relatively-non-cheap operations
 * increases linearly with the number of digits (in the specified base, which is usually not 10) needed to represent
 * the current state. Since performance is not optimal, and the results are rather strange anyway relative to PRNGs,
 * this should not be used as a direct substitute for a typical RandomnessSource (however, it is similar to, but simpler
 * and faster than, {@link SobolQRNG}). So what's it good for?
 * <br>
 * A VanDerCorputSequence can be a nice building block for more complicated quasi-randomness, especially for points in
 * 2D or 3D, when scramble is false. There's a simple way that should almost always "just work" as the static method
 * {@link #halton(int, int, int, int[])} here. If it doesn't meet your needs, there's a little more complexity involved.
 * Using a VanDerCorputQRNG with base 3 for the x-axis and another VanDerCorputQRNG with base 5 for the y-axis,
 * requesting a double from each to make points between (0.0, 0.0) and (1.0, 1.0), has an interesting trait that can be
 * desirable for many kinds of positioning in 2D: once a point has been generated, an identical point will never be
 * generated until floating-point precision runs out, but more than that, nearby points will almost never be generated
 * for many generations, and most points will stay at a comfortable distance from each other if the bases are different
 * and both prime (or, more technically, if they share no common denominators). This is also known as a Halton sequence,
 * which is a group of sequences of points instead of simply numbers. The choices of 3 and 5 are examples; any two
 * different primes will technically work for 2D (as well as any two numbers that share no common factor other than 1,
 * that is, they are relatively coprime), but patterns can be noticeable with primes larger than about 7, with 11 and 13
 * sometimes acceptable. Three VanDerCorputQRNG sequences could be used for a Halton sequence of 3D
 * points, using three different prime bases, four for 4D, etc. SobolQRNG can be used for the same purpose, but the
 * points it generates are typically more closely-aligned to a specific pattern, the pattern is symmetrical between all
 * four quadrants of the square between (0.0, 0.0) and (1.0, 1.0) in 2D, and it probably extends into higher dimensions.
 * Using one of the possible Halton sequences gives some more flexibility in the kinds of random-like points produced.
 * Oddly, using a base-2 van der Corput sequence as the x axis in a Halton sequence and a base-39 van der Corput
 * sequence as the y axis results in the greatest minimum distance between points found so far while still involving a
 * base-2 sequence (which is preferential for performance). There, the minimum distance between the first 65536 points
 * in the [0,1) range is 0.001147395; a runner-up is a y base of 13, which has a minimum distance of 0.000871727 . The
 * (2,39) Halton sequence is used by {@link #halton(int, int, int)} and {@link #halton(int, int, int, int[])}.
 * <br>
 * Because just using the state in a simple incremental fashion puts some difficult requirements on the choice of base
 * and seed, we can scramble the long state {@code i} with code equivalent to
 * {@code (i ^ Long.rotateLeft(i, 7) ^ Long.rotateLeft(i, 17))}. This scramble is different from earlier versions and
 * unlike the earlier ones has a one-to-one mapping from input states to scrambled states.
 * <br>
 * Created by Tommy Ettinger on 11/18/2016. Uses code adapted from
 * <a href="https://blog.demofox.org/2017/05/29/when-random-numbers-are-too-random-low-discrepancy-sequences/">Alan Wolfe's blog</a>,
 * which turned out to be a lot faster than the previous way I had it implemented.
 */
public class VanDerCorputQRNG implements StatefulRandomness, RandomnessSource, Serializable {
    private static final long serialVersionUID = 6;
    public long state;
    public final int base;
    public final boolean scramble;
    /**
     * Constructs a new van der Corput sequence generator with base 7, starting point 37, and scrambling disabled.
     */
    public VanDerCorputQRNG()
    {
        base = 7;
        state = 37;
        scramble = false;
    }

    /**
     * Constructs a new van der Corput sequence generator with the given starting point in the sequence as a seed.
     * Usually seed should be at least 20 with this constructor, but not drastically larger; 2000 is probably too much.
     * This will use a base 7 van der Corput sequence and have scrambling disabled.
     * @param seed the seed as a long that will be used as the starting point in the sequence; ideally positive but low
     */
    public VanDerCorputQRNG(long seed)
    {
        base = 7;
        state = seed;
        scramble = false;
    }

    /**
     * Constructs a new van der Corput sequence generator with the given base (a.k.a. radix; if given a base less than
     * 2, this will use base 2 instead) and starting point in the sequence as a seed. Good choices for base are between
     * 10 and 60 or so, and should usually be prime. Good choices for seed are larger than base but not by very much,
     * and should generally be positive at construction time.
     * @param base the base or radix used for this VanDerCorputQRNG; for most uses this should be prime but small-ish
     * @param seed the seed as a long that will be used as the starting point in the sequence; ideally positive but low
     * @param scramble if true, will produce more-random values that are better for 1D; if false, better for 2D or 3D
     */
    public VanDerCorputQRNG(int base, long seed, boolean scramble)
    {
        this.base = base < 2 ? 2 : base;
        state = seed;
        this.scramble = scramble;
    }

    /**
     * Gets the next quasi-random long as a fraction of {@link Long#MAX_VALUE}; this can never produce a negative value.
     * It is extremely unlikely to produce two identical values unless the state is very high or is negative; state
     * increases by exactly 1 each time this, {@link #next(int)}, or {@link #nextDouble()} is called and can potentially
     * wrap around to negative values after many generations.
     * @return a quasi-random non-negative long; may return 0 rarely, probably can't return {@link Long#MAX_VALUE}
     */
    @Override
    public long nextLong() {
        long n = (((scramble) ? (++state ^ (state << 7 | state >>> 57) ^ (state << 17 | state >>> 47)) : ++state)
                & 0x7fffffffffffffffL);
        if(base <= 2) {
            return Long.reverse(n) >>> 1;
        }
        double denominator = base, res = 0.0;
        while (n > 0)
        {
            res += (n % base) / denominator;
            n /= base;
            denominator *= base;
        }
        return (long) (res * Long.MAX_VALUE);
    }

    @Override
    public int next(int bits) {
        return (int)(nextLong()) >>> (32 - bits);
    }

    /**
     * Gets the next quasi-random double from between 0.0 and 1.0 (normally both exclusive; only if state is negative or
     * has wrapped around to a negative value can 0.0 ever be produced). It should be nearly impossible for this to
     * return the same number twice unless floating-point precision has been exhausted or a very large amount of numbers
     * have already been generated. Certain unusual bases may make this more likely, and similar numbers may be returned
     * more frequently if scramble is true.
     * @return a quasi-random double that will always be less than 1.0 and will be no lower than 0.0
     */
    public double nextDouble() {
        long n = (((scramble) ? (++state ^ (state << 7 | state >>> 57) ^ (state << 17 | state >>> 47)) : ++state)
                & 0x7fffffffffffffffL);
        if(base <= 2) {
            return (Long.reverse(n) >>> 11) * 0x1p-53;
        }
        double denominator = base, res = 0.0;
        while (n > 0)
        {
            res += (n % base) / denominator;
            n /= base;
            denominator *= base;
        }
        return res;
    }

    @Override
    public VanDerCorputQRNG copy() {
        return new VanDerCorputQRNG(base, state, scramble);
    }

    @Override
    public long getState() {
        return state;
    }

    @Override
    public void setState(long state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "VanDerCorputQRNG with base " + base +
                ", scrambling " + (scramble ? "on" : "off") +
                ", and state 0x" + StringKit.hex(state) + "L";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VanDerCorputQRNG that = (VanDerCorputQRNG) o;

        return state == that.state && base == that.base && scramble == that.scramble;

    }

    @Override
    public int hashCode() {
        int result = (int) (state ^ (state >>> 32));
        result = 31 * result + base | 0;
        return 31 * result + (scramble ? 1 : 0) | 0;
    }

    /**
     * Convenience method that gets a quasi-random 2D point between integer (0,0) inclusive and (width,height)
     * exclusive and fills it into point. This is roughly equivalent to creating two VanDerCorputQRNG generators, one
     * with {@code new VanDerCorputQRNG(2, index, false)} and the other with
     * {@code new VanDerCorputQRNG(39, index, false)}, then getting an x-coordinate from the first with
     * {@code (int)(nextDouble() * width)} and similarly for y with the other generator. The advantage here is you don't
     * actually create any objects using this static method, only assigning to point, if valid. You might find an
     * advantage in using values for index that start higher than 20 or so, but you can pass sequential values for index
     * and generally get points that won't be near each other; this is not true for all parameters to Halton sequences,
     * but it is true for this one.
     * @param width the maximum exclusive bound for the x-positions (index 0) of points this can return
     * @param height the maximum exclusive bound for the y-positions (index 1) of points this can return
     * @param index an int that, if unique, positive, and not too large, will usually result in unique points
     * @param point an int array that will be modified; should have length 2; if null or too small, a new array will be created
     * @return point after modifications to the first two items, or a new array if point is null or too small
     */
    public static int[] halton(int width, int height, int index, int[] point)
    {
        if(point == null || point.length < 2)
            point = new int[2];

        double denominator = 39.0, resY = 0.0;
        int n = (index+1 & 0x7fffffff);
        while (n > 0)
        {
            resY += (n % 39) / denominator;
            n /= 39;
            denominator *= 39.0;
        }
        point[0] = (int)(width * (Integer.reverse(index + 1) >>> 1) * 0x1p-31);
        point[1] = (int)(resY * height);
        return point;
    }
    /**
     * Convenience method that gets a quasi-random Coord between integer (0,0) inclusive and (width,height) exclusive
     * and gets the corresponding Coord from the Coord pool. This is roughly equivalent to creating two VanDerCorputQRNG
     * generators, one with {@code new VanDerCorputQRNG(2, index, false)} and the other with
     * {@code new VanDerCorputQRNG(39, index, false)}, then getting an x-coordinate from the first with
     * {@code (int)(nextDouble() * width)} and similarly for y with the other generator. You might find an advantage in
     * using values for index that start higher than 20 or so, but you can pass sequential values for index and
     * generally get points that won't be near each other; this is not true for all parameters to Halton sequences, but
     * it is true for this one.
     * @param width the maximum exclusive bound for the x-positions (index 0) of points this can return
     * @param height the maximum exclusive bound for the y-positions (index 1) of points this can return
     * @param index an int that, if unique, positive, and not too large, will usually result in unique points
     * @return point after modifications to the first two items, or a new array if point is null or too small
     */
    public static Coord halton(int width, int height, int index)
    {
        return halton(width, height, 0, 0, index);
    }
    /**
     * Convenience method that gets a quasi-random Coord between integer (0,0) inclusive and (width,height) exclusive
     * and gets the corresponding Coord from the Coord pool. This is roughly equivalent to creating two VanDerCorputQRNG
     * generators, one with {@code new VanDerCorputQRNG(2, index, false)} and the other with
     * {@code new VanDerCorputQRNG(39, index, false)}, then getting an x-coordinate from the first with
     * {@code (int)(nextDouble() * width)} and similarly for y with the other generator. You might find an advantage in
     * using values for index that start higher than 20 or so, but you can pass sequential values for index and
     * generally get points that won't be near each other; this is not true for all parameters to Halton sequences, but
     * it is true for this one.
     * @param width the width of the area this can cover
     * @param height the height of the area this can cover
     * @param xOffset the lowest x-coordinate this can produce, and also added to width to get the upper bound on x
     * @param yOffset the lowest y-coordinate this can produce, and also added to height to get the upper bound on y
     * @param index an int that, if unique, positive, and not too large, will usually result in unique points
     * @return point after modifications to the first two items, or a new array if point is null or too small
     */
    public static Coord halton(int width, int height, int xOffset, int yOffset, int index)
    {
        double denominator = 39.0, resY = 0.0;
        int n = (index+1 & 0x7fffffff);
        while (n > 0)
        {
            resY += (n % 39) / denominator;
            n /= 39;
            denominator *= 39.0;
        }
        return Coord.get((int)(width * (Integer.reverse(index + 1) >>> 1) * 0x1p-31) + xOffset, (int)(resY * height) + yOffset);
    }

    /**
     * Convenience method that gets a quasi-random 3D point between integer (0,0,0) inclusive and (width,height,depth)
     * exclusive. This is roughly equivalent to creating three VanDerCorputQRNG generators, one with
     * {@code new VanDerCorputQRNG(2, index, false)} another with {@code new VanDerCorputQRNG(3, index, false)},
     * and another with {@code new VanDerCorputQRNG(5, index, false)}, then getting an x-coordinate from the first with
     * {@code (int)(nextDouble() * width)} and similarly for y and z with the other generators. The advantage here is
     * you don't actually create any objects using this static method, only assigning to point, if valid. You might find
     * an advantage in using values for index that start higher than 20 or so, but you can pass sequential values for
     * index and generally get points that won't be near each other; this is not true for all parameters to Halton
     * sequences, but it is true for this one.
     * @param width the maximum exclusive bound for the x-positions (index 0) of points this can return
     * @param height the maximum exclusive bound for the y-positions (index 1) of points this can return
     * @param depth the maximum exclusive bound for the z-positions (index 2) of points this can return
     * @param index an int that, if unique, positive, and not too large, will usually result in unique points
     * @param point an int array that will be modified; should have length 3; if null or too small, a new array will be created
     * @return point after modifications to the first two items, or a new array if point is null or too small
     */
    public static int[] halton(int width, int height, int depth, int index, int[] point)
    {
        if(point == null || point.length < 3)
            point = new int[3];

        double denominator = 3.0, resY = 0.0, resZ = 0.0;
        int n = (index+1 & 0x7fffffff);
        while (n > 0)
        {
            resY += (n % 3) / denominator;
            n /= 3;
            denominator *= 3.0;
        }

        denominator = 5;
        n = (index+1 & 0x7fffffff);
        while (n > 0)
        {
            resZ += (n % 5) / denominator;
            n /= 5;
            denominator *= 5.0;
        }
        point[0] = (int)(width * (Integer.reverse(index + 1) >>> 1) * 0x1p-31);
        point[1] = (int)(resY * height);
        point[2] = (int)(resZ * depth);
        return point;
    }

    /**
     * Convenience method that gets a quasi-random Coord3D between integer (0,0,0) inclusive and (width,height,depth)
     * exclusive. This is roughly equivalent to creating three VanDerCorputQRNG generators, one with
     * {@code new VanDerCorputQRNG(2, index, false)} another with {@code new VanDerCorputQRNG(3, index, false)},
     * and another with {@code new VanDerCorputQRNG(5, index, false)}, then getting an x-coordinate from the first with
     * {@code (int)(nextDouble() * width)} and similarly for y and z with the other generators. This overload always
     * creates a new Coord3D object, so you might prefer {@link #halton(int, int, int, int, int[])}, which can reuse an
     * int array. You might find an advantage in using values for index that start higher than 20 or so, but you can
     * pass sequential values for index and generally get points that won't be near each other; this is not true for all
     * parameters to Halton sequences, but it is true for this one.
     * @param width the maximum exclusive bound for the x-positions (index 0) of points this can return
     * @param height the maximum exclusive bound for the y-positions (index 1) of points this can return
     * @param depth the maximum exclusive bound for the z-positions (index 2) of points this can return
     * @param index an int that, if unique, positive, and not too large, will usually result in unique points
     * @return a new Coord3D with x,y,z between 0,0,0 (inclusive) and width,height,depth (exclusive)
     */
    public static Coord3D halton(int width, int height, int depth, int index)
    {
        double denominator = 3.0, resY = 0.0, resZ = 0.0;
        int n = (index+1 & 0x7fffffff);
        while (n > 0)
        {
            resY += (n % 3) / denominator;
            n /= 3;
            denominator *= 3.0;
        }

        denominator = 5;
        n = (index+1 & 0x7fffffff);
        while (n > 0)
        {
            resZ += (n % 5) / denominator;
            n /= 5;
            denominator *= 5.0;
        }
        return new Coord3D((int)(width * (Integer.reverse(index + 1) >>> 1) * 0x1p-31),
                (int)(resY * height),
                (int)(resZ * depth));
    }

    /**
     * Convenience method to get a double from the van der Corput sequence with the given {@code base} at the requested
     * {@code index} without needing to construct a VanDerCorputQRNG. You should use a prime number for base; 2, 3, 5,
     * and 7 should be among the first choices to ensure optimal quality unless you are scrambling the index yourself.
     * If speed is the priority, then larger prime bases counter-intuitively perform better than small ones; 0x1337,
     * 0xDE4D, 0x510B and 0xACED are all probable primes (using {@link java.math.BigInteger#isProbablePrime(int)}) that
     * may do well here for speed but will likely require some basic scrambling of the index order. This method on its
     * own does not perform any scrambling on index other than incrementing it and ensuring it is positive (by
     * discarding the sign bit; for all positive index values other than 0x7FFFFFFF, this has no effect). If you want
     * to quickly scramble an int index {@code i} for this purpose, try
     * {@code (i ^ (i << 7 | i >>> 25) ^ (i << 19 | i >>> 13))}, which may compile to SSE instructions on recent 
     * desktop processors and won't risk losing precision on GWT.
     * <br>
     * Uses the same algorithm as {@link #determine2(int)} when base is 2, which should offer some speed improvement.
     * The other bases use code adapted from
     * <a href="https://blog.demofox.org/2017/05/29/when-random-numbers-are-too-random-low-discrepancy-sequences/">Alan Wolfe's blog</a>,
     * which turned out to be a lot faster than the previous way I had it implemented.
     * @param base a prime number to use as the base/radix of the van der Corput sequence
     * @param index the position in the sequence of the requested base, as a non-negative int
     * @return a quasi-random double between 0.0 (inclusive) and 1.0 (exclusive).
     */
    public static double determine(final int base, final int index)
    {
        if(base <= 2) {
            return (Integer.reverse(index + 1) >>> 1) * 0x1p-31;
        }
        double denominator = base, res = 0.0;
        int n = (index+1 & 0x7fffffff);
        while (n > 0)
        {
            res += (n % base) / denominator;
            n /= base;
            denominator *= base;
        }
        return res;
    }

    /**
     * Convenience method to get a double from the van der Corput sequence with the base 2 at the requested
     * {@code index} without needing to construct a VanDerCorputQRNG. This does not perform any scrambling on index
     * other than incrementing it and ensuring it is positive (by discarding the sign bit; for all positive index values
     * other than 0x7FFFFFFF ({@link Integer#MAX_VALUE}), this has no effect).
     * <br>
     * Because binary manipulation of numbers is easier and more efficient, the technique used by this method is also
     * used by {@link #determine(int, int)} when base is 2, and should be faster than other bases.
     * @param index the position in the base-2 van der Corput sequence, as a non-negative int
     * @return a quasi-random double between 0.0 (inclusive) and 1.0 (exclusive).
     */
    public static double determine2(int index)
    {
        return (Integer.reverse(index + 1) >>> 1) * 0x1p-31;
    }
    /**
     * Method to get a double from the van der Corput sequence with the base 2 at a scrambling of the requested
     * {@code index} without needing to construct a VanDerCorputQRNG. This performs different scrambling on index
     * than the instance methods on this class perform, and it seems to do well enough while being a little simpler.
     * This is meant to be usable as an alternative to a different base for a van der Corput sequence when you need two
     * different sequences, and are already using base 2 via {@link #determine2(int)}.
     * <br>
     * Because binary manipulation of numbers is easier and more efficient, this method should be somewhat faster than
     * the alternatives, like {@link #determine(int, int)} with base 2. It should take only slightly longer to run than
     * {@link #determine2(int)}, due to the brief amount of time needed to scramble the index.
     * @param index the position in the sequence of the requested base
     * @return a quasi-random double between 0.0 (inclusive) and 1.0 (exclusive).
     */
    public static double determine2_scrambled(int index)
    {
        return (Integer.reverse(++index ^ (index << 7 | index >>> 25) ^ (index << 19 | index >>> 13)) >>> 1) * 0x1p-31;
    }

    private static final int[] lowPrimes = {2, 3, 2, 3, 5, 2, 3, 2};

    /**
     * Chooses one sequence from the van der Corput sequences with bases 2, 3, and 5, where 5 is used 1/8 of the time,
     * 3 is used 3/8 of the time, and 2 is used 1/2 of the time, and returns a double from the chosen sequence at the
     * specified {@code index}. The exact setup used for the choice this makes is potentially fragile, but in certain
     * circumstances this does better than {@link SobolQRNG} at avoiding extremely close values (the kind that overlap
     * on actual maps). Speed is not a concern here; this should be very much fast enough for the expected usage in
     * map generation (it's used in {@link GreasedRegion#quasiRandomSeparated(double)}.
     * @param index the index to use from one of the sequences; will also be used to select sequence
     * @return a double from 0.0 (inclusive, but extremely rare) to 1.0 (exclusive); values will tend to spread apart
     */
    public static double determineMixed(int index)
    {
        return determine(lowPrimes[index & 7], index);
    }

    /**
     * Given any int (0 is allowed), this gets a somewhat-sub-random float from 0.0 (inclusive) to 1.0 (exclusive)
     * using the same implementation as {@link NumberTools#randomFloat(long)} but with index alterations. Only "weak"
     * because it lacks the stronger certainty of subsequent numbers being separated that the Van der Corput sequence
     * has. Not actually sub-random, but should be distributed fairly well (internally uses {@link ThrustAltRNG}'s
     * algorithm, which does not guarantee that its outputs are unique).
     * <br>
     * Not all int values for index will produce unique results, since this produces a float and there are less distinct
     * floats between 0.0 and 1.0 than there are all ints (1/512 as many floats in that range as ints, specifically).
     * It should take a while calling this method before you hit an actual collision.
     * @param index any int
     * @return a float from 0.0 (inclusive) to 1.0 (exclusive) that should not be closely correlated to index
     */
    public static float weakDetermine(final int index)
    {
        return NumberTools.randomFloat((index >>> 19 | index << 13) ^ 0x13A5BA1D);
        //return NumberTools.longBitsToDouble(0x3ff0000000000000L | ((index<<1|1) * 0x9E3779B97F4A7C15L * ~index
        //        - ((index ^ ~(index * 11L)) * 0x632BE59BD9B4E019L)) >>> 12) - 1.0;
        //return NumberTools.setExponent(
        //        (NumberTools.setExponent((index<<1|1) * 0.618033988749895, 0x3ff))
        //                * (0x232BE5 * (~index)), 0x3ff) - 1.0;
    }

    /**
     * Like {@link #weakDetermine(int)}, but returns a float between -1.0f and 1.0f, exclusive on both. Uses
     * {@link NumberTools#randomSignedFloat(long)} internally but alters the index parameter so calls with nearby values
     * for index are less likely to have nearby results.
     * @param index any int
     * @return a sub-random float between -1.0f and 1.0f (both exclusive, unlike some other methods)
     */
    public static float weakSignedDetermine(final int index) {
        return NumberTools.randomSignedFloat((index >>> 19 | index << 13) ^ 0x13A5BA1D);
    }

    /**
     * Similar to {@link #determine(int, int)}, but can take bases that aren't prime and can sometimes produce a
     * Halton-like sequence with almost-as-good distance between points. The base is allowed to be any odd long, 
     * (negative bases are allowed). The index can technically also be negative, and if this is given 0 it will not
     * return any specific number (it will vary with the base). This returns a double between 0.0 inclusive and 1.0
     * exclusive. Better results have been found with larger bases (points tend to be more spread out). It is never as
     * good at spreading out 2D points as a 2,3 Halton sequence, at least for any bases tried so far.
     * <br>
     * Earlier versions of this method wound up only producing points on parallel lines in 2D, never placing points in
     * between those lines. This sometimes formed a hex-like grid that, as hexagons do, has optimal packing properties,
     * which made the optimal distance seem very good despite the points having a clear pattern. This can still
     * sometimes be useful; when you want optimal distance and don't have a case where a clear pattern on a grid is an
     * issue, it can have high performance. The code for the old way is small, though not simple:
     * {@code ((base * Integer.reverse(index) << 21) & 0x1fffffffffffffL) * 0x1p-53}, where base is an odd long and
     * index is any int. It works best in one dimension.
     * @param base any odd long
     * @param index any int
     * @return a double between 0.0 inclusive and 1.0 exclusive
     */
    public static double altDetermine(long base, final int index) { return (((base = (Long.reverse(base + index) ^ 0x5851F42D4C957F2DL) * 0x14057B7EF767814BL) >>> 11) ^ (base >>> 13) ^ (base >>> 14)) * 0x1p-53; }
//    public static double altDetermine(long base, final int index) { return ((((index * 0x5851F42D4C957F2DL + base) * 0x5851F42D4C957F2DL + base) * 0x5851F42D4C957F2DL + base) >>> 11) * 0x1p-53; }

    /**
     * A quasi-random number generator of doubles between 0.0 inclusive and 1.0 exclusive, but that has issues when it
     * would be used like a Halton sequence. Only ideal in 1D, this produces well-separated points that are aligned to
     * parallel hyperplanes when called with different bases and each base used as an axis for more than 1 dimension.
     * This can produce points with more separation in 2D than a Halton sequence can, but not often. Two bases that do
     * this when used together are the ints 0xDE4DBEEF and 0x1337D00D (or in decimal, -565330193 and 322424845; note
     * that 0xDE4DBEEF is a negative integer); they were tried as a gimmick but nothing else turned out better. They do
     * still produce points on parallel lines, and like all bases, never points between those lines.
     * <br>
     * Note, the source of this method is one line, and you may see benefits from copying that code into the call-site
     * with minor modifications. This returns
     * {@code (((base * Integer.reverse(index)) << 21) & 0x1fffffffffffffL) * 0x1p-53;}, where base is a long and index
     * is an int (as in the method signature). The multiplier 0x1p-53 is a very small hexadecimal double literal, using
     * the same syntax as other parts of SquidLib use for packed floats; using this helps avoid precision loss. If you
     * want a range larger than 0.0 to 1.0, you can change the multiplier {@code 0x1p-53} to some other constant, like
     * declaring {@code final double upTo100 = 0x1p-53 * 100.0} before some code that wants quasi-random numbers between
     * 0.0 inclusive and 100.0 exclusive, then using
     * {@code (((base * Integer.reverse(index)) << 21) & 0x1fffffffffffffL) * upTo100;} to get those numbers. It isn't
     * precise to use this technique to get numbers with an upper bound less than 1.0, because 0x1p-53 is about as small
     * as a double can get with precision intact. In that case, you can use
     * {@code (((base * Integer.reverse(index)) << 8) & 0xffffffffffL) * smallBound;} where smallBound has been declared
     * as {@code final double smallBound = 0x1p-40 * 0.05;} (where 0.05 can be switched out for any double between
     * 1.0/8192.0 and 1.0).
     * @param base any odd long; the most significant 21 bits (except the sign bit) are effectively ignored
     * @param index any int; if 0 this will return 0
     * @return a double between 0.0 inclusive and 1.0 exclusive
     */
    public static double planarDetermine(long base, final int index) { return ((base * Integer.reverse(index) << 21) & 0x1fffffffffffffL) * 0x1p-53; }

    /**
     * Samples a quasi-random Coord sequence that is almost unique to the given seed, getting a Coord with x between
     * xOffset (inclusive) and width + xOffset (exclusive), y between yOffset (inclusive) and height + yOffset
     * (exclusive). The seed is "almost" unique because even seeds are discouraged; there is an identical sequence for
     * every even seed produced by some odd seed. This generates a not-very random number, reverses its bits as other
     * methods in this class do, then treats that single 32-bit int as two coordinates on a Z-order curve to get
     * separate x and y from them. In practice these Coords are very well-dispersed if only a small amount are sampled
     * and all the index values are close-by, and get closer together as more are sampled. Unlike the Halton sequence,
     * this has very different results with different seeds (Halton only allows bases to be changed), and doesn't
     * involve any division, modulus, conditionals, or loops.
     * @param seed an int seed that should be an odd number
     * @param width the width of the area this can cover
     * @param height the height of the area this can cover
     * @param xOffset the lowest x-coordinate this can produce, and also added to width to get the upper bound on x
     * @param yOffset the lowest y-coordinate this can produce, and also added to height to get the upper bound on y
     * @param index the index in the sequence, almost always a positive int that increases by 1 with each call
     * @return a Coord between (xOffset, yOffset) inclusive and (width+xOffset, height+yOffset) exclusive
     */
    public static Coord haltoid(int seed, int width, int height, int xOffset, int yOffset, int index)
    {
        int morton = GreasedRegion.disperseBits(Integer.reverse((seed * 0x2C9277B5 | 1) * (index + 1)));
        return Coord.get((int)(width * ((morton & 0xffff) * 0x1p-16)) + xOffset, (int)(((morton >>> 16) * 0x1p-16) * height) + yOffset);
    }

}
