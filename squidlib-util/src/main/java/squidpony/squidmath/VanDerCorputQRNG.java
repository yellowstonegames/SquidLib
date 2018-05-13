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
 * you start at 0); the base (when given) should be prime and moderately small (or very small if scramble is false, any
 * of 2, 3, 5, or 7 should be safe); this doesn't generate very random numbers when scramble is false (which can be good
 * for making points that should not overlap), but it will seem much more random when scramble is true; this is a
 * StatefulRandomness with an additional method for generating quasi-random doubles, {@link #nextDouble()}; and there
 * are several static methods offered for convenient generation of points on the related Halton sequence (as well as
 * faster generation of doubles in the base-2 van der Corput sequence, and a special method that switches which base
 * it uses depending on the index to seem even less clearly-patterned).
 * <br>
 * This generator allows a base (also called a radix) that changes the sequence significantly; a base should be prime,
 * and this performs a little better in terms of time used with larger primes, though quality is also improved by
 * preferring primes that aren't very large relative to the quantity of numbers expected to be generated. Unfortunately,
 * performance is not especially similar to conventional PRNGs; smaller (positive) state values are processed more
 * quickly than larger ones, or most negative states. At least one 64-bit integer modulus and one 64-bit integer
 * division are required for any number to be produced, and the amount of both of these relatively-non-cheap operations
 * increases linearly with the number of digits (in the specified base, which is usually not 10) needed to represent
 * the current state. Since performance is not optimal, and the results are rather strange anyway relative to PRNGs,
 * this should not be used as a direct substitute for a typical RandomnessSource (however, it is similar to, but simpler
 * than, {@link squidpony.squidmath.SobolQRNG}). So what's it good for?
 * <br>
 * A VanDerCorputSequence can be a nice building block for more complicated quasi-randomness, especially for points in
 * 2D or 3D, when scramble is false. There's a simple way that should almost always "just work" as the static method
 * {@link #halton(int, int, int)} here. If it doesn't meet your needs, there's a little more complexity involved. Using
 * a VanDerCorputQRNG with base 3 for the x-axis and another VanDerCorputQRNG with base 5 for the y-axis, requesting a
 * double from each to make points between (0.0, 0.0) and (1.0, 1.0), has an interesting trait that can be desirable for
 * many kinds of positioning in 2D: once a point has been generated, an identical point will never be generated until
 * floating-point precision runs out, but more than that, nearby points will almost never be generated for many
 * generations, and most points will stay at a comfortable distance from each other if the bases are different and both
 * prime (as well as, more technically, if they share no common denominators). This is also known as a Halton sequence,
 * which is a group of sequences of points instead of simply numbers. The choices of 3 and 5 are just examples; any two
 * different primes will technically work for 2D, but patterns can become noticeable with primes larger than about 7,
 * with 11 and 13 sometimes acceptable. Three VanDerCorputQRNG sequences could be used for a Halton sequence of 3D
 * points, using three different prime bases, four for 4D, etc. SobolQRNG can be used for the same purpose, but the
 * points it generates are typically more closely-aligned to a specific pattern, the pattern is symmetrical between all
 * four quadrants of the square between (0.0, 0.0) and (1.0, 1.0) in 2D, and it probably extends into higher dimensions.
 * Using one of the possible Halton sequences gives some more flexibility in the kinds of random-like points produced.
 * <br>
 * Because just using the state in a simple incremental fashion puts some difficult requirements on the choice of base
 * and seed, we can use a technique like Gray codes to scramble the state. The sequence of these Gray-like codes for the
 * integers from 0 to 16 is {@code 0, 9, 21, 16, 50, 51, 23, 10, 4, 28, 49, 39, 246, 198, 179, 97, 393}, and this can be
 * generated efficiently with {@code (i * i) ^ ((i * 137) >>> 4)}. No duplicate results were found in any numbers of 20
 * bits or less, and trying to find duplicates in 24 bits exhausted the testing computer's memory, without finding a
 * duplicate. You should be fine.
 * <br>
 * Expected output for {@link #nextDouble()} called 33 times on an instance made with {@code new VanDerCorputQRNG(11, 83L, true)}:
 * 0.5194317328051362, 0.8590943241581859, 0.5931288846390274, 0.5045420394781778, 0.7892220476743392,
 * 0.6645037907246772, 0.7809575848644218, 0.0725360289597705, 0.6322655556314459, 0.6998838877125879,
 * 0.5578853903421898, 0.5969537599890717, 0.6323338569769824, 0.873505908066389, 0.09514377433235435,
 * 0.6623864490130456, 0.4376750221979373, 0.6946929854518133, 0.4250392732736835, 0.5294720305990028,
 * 0.4790656375930606, 0.2626869749334062, 0.056075404685472306, 0.9758213236800765, 0.676729731575712,
 * 0.899118912642579, 0.1876237961887849, 0.652755959292398, 0.039683081756710606, 0.10504746943514787,
 * 0.2598183184208729, 0.4078273341984837, 0.3034628782187009
 * <br>
 * Expected output for {@link #nextDouble()} called 33 times on an instance made with {@code new VanDerCorputQRNG(19, 83L, true)}:
 * 0.8944452544102639, 0.7842327790609341, 0.4352023081468067, 0.0696971324652205, 0.5957213342439054,
 * 0.8864342661581787, 0.0167739658228528, 0.8956192785506557, 0.26943470353972115, 0.3525371966145134,
 * 0.09754375733765087, 0.8924118139056637, 0.3585147443619984, 0.9126771587081131, 0.28926266679967155,
 * 0.8542138258607592, 0.8789987799356972, 0.7905019145034184, 0.1624220194749887, 0.9349836173755572,
 * 0.48623015477167914, 0.6716799287912155, 0.8240344994283346, 0.6557883994137552, 0.4561966221867542,
 * 0.07946532024769608, 0.15134168706501638, 0.1382202407900492, 0.7890439760284221, 0.7142517322611092,
 * 0.3069037223471275, 0.7030256060036372, 0.01678163918324752
 * <br>
 * Note on Gray-like code implementation: This is not a typical Gray code. Normally, the operation looks like
 * {@code i ^ (i >>> 1)}, which gives negative results for negative values of i (not wanted here), and also clusters
 * two very similar numbers together for every pair of sequential numbers. An earlier version scrambled with a basic
 * Gray code, but the current style, {@code (i * i) ^ ((i * 137) >>> 4)}, produces much more "wild and crazy" results,
 * but never negative ones. The period of this, if it is seen as a RandomnessSource, is probably about 2^56, at least
 * for reasonably small starting states (under 1000 or so?). This is ideal for a scramble.
 * <br>
 * Created by Tommy Ettinger on 11/18/2016.
 */
public class VanDerCorputQRNG implements StatefulRandomness, RandomnessSource, Serializable {
    private static final long serialVersionUID = 5;
    public long state;
    public final int base;
    public final boolean scramble;
    /**
     * Constructs a new van der Corput sequence generator with base 13, starting point 83, and scrambling enabled.
     */
    public VanDerCorputQRNG()
    {
        base = 7;
        state = 37;
        scramble = true;
    }

    /**
     * Constructs a new van der Corput sequence generator with the given starting point in the sequence as a seed.
     * Usually seed should be at least 20 with this constructor, but not drastically larger; 2000 is probably too much.
     * This will use a base 13 van der Corput sequence and have scrambling enabled.
     * @param seed the seed as a long that will be used as the starting point in the sequence; ideally positive but low
     */
    public VanDerCorputQRNG(long seed)
    {
        base = 7;
        state = seed;
        scramble = true;
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
        // when scrambling the sequence, intentionally uses a non-standard Gray-like code
        long s = (scramble) ? (++state & 0x7fffffffffffffffL) : (++state * state) ^ (state * 137 >>> 4),
                num = s % base, den = base;
        while (den <= s) {
            num *= base;
            num += (s % (den * base)) / den;
            den *= base;
        }
        return (Long.MAX_VALUE / den) * num;
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
        // when scrambling the sequence, intentionally uses a non-standard Gray-like code
        long s = (scramble) ? (++state & 0x7fffffffffffffffL) : (++state * state) ^ (state * 137 >>> 4),
                num = s % base, den = base;
        while (den <= s) {
            num *= base;
            num += (s % (den * base)) / den;
            den *= base;
        }
        return num / (double)den;
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
                ", and state 0x" + StringKit.hex(state);
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
        result = 31 * result + base;
        result = 31 * result + (scramble ? 1 : 0);
        return result;
    }

    /**
     * Convenience method that gets a quasi-random Coord between integer (0,0) inclusive and (width,height) exclusive.
     * This is roughly equivalent to creating two VanDerCorputQRNG generators, one with
     * {@code new VanDerCorputQRNG(2, index, false)} and the other with {@code new VanDerCorputQRNG(3, index, false)},
     * then getting an x-coordinate from the first with {@code (int)(nextDouble() * width)} and similarly for y with the
     * other generator. The advantage here is you don't actually create any objects using this static method, other than
     * a (almost always shared) reference to the returned Coord. You might find an advantage in using values for index
     * that start higher than 20 or so, but you can pass sequential values for index and generally get Coords that won't
     * be near each other; this is not true for all parameters to Halton sequences, but it is true for this one.
     * @param width the maximum exclusive bound for the x-positions of Coord values this can return
     * @param height the maximum exclusive bound for the y-positions of Coord values this can return
     * @param index an int that, if unique, positive, and not too large, will usually result in unique Coord values
     * @return a Coord that usually will have a comfortable distance from Coords produced with close index values
     */
    public static Coord halton(int width, int height, int index)
    {
        int s = (index+1 & 0x7fffffff),
                numY = s % 3, denY = 3;
        while (denY <= s) {
            numY *= 3;
            numY += (s % (denY * 3)) / denY;
            denY *= 3;
        }
        return Coord.get((int)(width * determine2(s)), numY * height / denY);
    }
    /**
     * Convenience method that gets a quasi-random Coord3D between integer (0,0,0) inclusive and (width,height,depth)
     * exclusive. This is roughly equivalent to creating three VanDerCorputQRNG generators, one with
     * {@code new VanDerCorputQRNG(2, index, false)} another with {@code new VanDerCorputQRNG(3, index, false)},
     * and another with {@code new VanDerCorputQRNG(5, index, false)}, then getting an x-coordinate from the first with
     * {@code (int)(nextDouble() * width)} and similarly for y and z with the other generators. The advantage here is
     * you don't actually create any objects using this static method, other than a returned Coord3D. You might find an
     * advantage in using values for index that start higher than 20 or so, but you can pass sequential values for index
     * and generally get Coord3Ds that won't be near each other; this is not true for all parameters to Halton
     * sequences, but it is true for this one.
     * @param width the maximum exclusive bound for the x-positions of Coord3D values this can return
     * @param height the maximum exclusive bound for the y-positions of Coord3D values this can return
     * @param depth the maximum exclusive bound for the z-positions of Coord3D values this can return
     * @param index an int that, if unique, positive, and not too large, will usually result in unique Coord3D values
     * @return a Coord3D that usually will have a comfortable distance from Coord3Ds produced with close index values
     */
    public static Coord3D halton(int width, int height, int depth, int index)
    {
        int s = (index+1 & 0x7fffffff),
                numY = s % 3, denY = 3, numZ = s % 5, denZ = 5;
        while (denY <= s) {
            numY *= 3;
            numY += (s % (denY * 3)) / denY;
            denY *= 3;
        }
        while (denZ <= s) {
            numZ *= 5;
            numZ += (s % (denZ * 5)) / denZ;
            denZ *= 5;
        }
        return Coord3D.get((int)(width * determine2(s)), numY * height / denY, numZ * depth / denZ);
    }

    /**
     * Convenience method to get a double from the van der Corput sequence with the given {@code base} at the requested
     * {@code index} without needing to construct a VanDerCorputQRNG. You should use a prime number for base; 2, 3, 5,
     * and 7 should be among the first choices. This does not perform any scrambling on index other than incrementing it
     * and ensuring it is positive (by discarding the sign bit; for all positive index values other than 0x7FFFFFFF,
     * this has no effect).
     * <br>
     * Delegates to {@link #determine2(int)} when base is 2, which should offer some speed improvement.
     * @param base a (typically very small) prime number to use as the base/radix of the van der Corput sequence
     * @param index the position in the sequence of the requested base
     * @return a quasi-random double between 0.0 (inclusive) and 1.0 (exclusive).
     */
    public static double determine(int base, int index)
    {
        final int s = (index+1 & 0x7fffffff);
        if(base <= 2) {
            final int leading = Integer.numberOfLeadingZeros(s);
            return (Integer.reverse(s) >>> leading) / (double) (1 << (32 - leading));
        }
        int num = s % base, den = base;
        while (den <= s) {
            num *= base;
            num += (s % (den * base)) / den;
            den *= base;
        }
        return num / (double)den;
    }
    /**
     * Convenience method to get a double from the van der Corput sequence with the base 2 at the requested
     * {@code index} without needing to construct a VanDerCorputQRNG. This does not perform any scrambling on index
     * other than incrementing it and ensuring it is positive (by discarding the sign bit; for all positive index values
     * other than 0x7FFFFFFF, this has no effect).
     * <br>
     * Because binary manipulation of numbers is easier and more efficient, this method should be somewhat faster than
     * the alternatives, like {@link #determine(int, int)} with base 2.
     * @param index the position in the sequence of the requested base
     * @return a quasi-random double between 0.0 (inclusive) and 1.0 (exclusive).
     */
    public static double determine2(int index)
    {
        int s = (index+1 & 0x7fffffff), leading = Integer.numberOfLeadingZeros(s);
        return (Integer.reverse(s) >>> leading) / (double)(1 << (32 - leading));
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
        int s = ((++index ^ index << 1 ^ index >> 1) & 0x7fffffff), leading = Integer.numberOfLeadingZeros(s);
        return (Integer.reverse(s) >>> leading) / (double)(1 << (32 - leading));
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
    public static double altDetermine(long base, final int index) { return (((base *= (Long.reverse(base ^ index) ^ 0x5851F42D4C957F2DL) * 0x14057B7EF767814BL) >>> 11) ^ (base >>> 13) ^ (base >>> 16)) * 0x1p-53; }

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
}
