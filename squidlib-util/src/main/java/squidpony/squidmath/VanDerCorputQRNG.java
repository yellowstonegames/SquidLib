package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A quasi-random number generator that goes through one of many sub-random sequences found by J.G. van der Corput.
 * More specifically, this is a kind of scrambled van der Corput sequence, where the state is internally stored in a
 * simple 64-bit long that is incremented once per generated number, but the state is altered with a Gray code before
 * being used, more on this later if you want to read about it. The important things to know about this class are:
 * size of state affects speed (prefer smaller seeds, but quality is sometimes a bit poor at first if you start at 0);
 * the base (when given) should be prime and moderately small; this doesn't generate very random numbers (which can be
 * good for making points that should not overlap); and this is a StatefulRandomness with an additional method for
 * generating quasi-random doubles, {@link #nextDouble()}.
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
 * 2D, 3D, or even higher dimensions. Using a VanDerCorputQRNG with base 11 for the x-axis and another VanDerCorputQRNG
 * with base 19 for the y-axis, requesting a double from each to make points between (0.0, 0.0) and (1.0, 1.0), has an
 * interesting trait that can be desirable for many kinds of positioning in 2D: once a point has been generated, an
 * identical point will never be generated until floating-point precision runs out, but more than that, nearby points
 * will almost never be generated for many generations, and most points will stay at a comfortable distance from each
 * other if the bases are different and both prime (as well as, more technically, if they share no common denominators).
 * This is also known as a Halton sequence, which is a group of sequences of points instead of simply numbers. The
 * choices of 11 and 19 are just examples; any two different primes will work well for 2D, especially prime numbers
 * between 10 and 60 or so. Three VanDerCorputQRNG sequences could be used for a Halton sequence of 3D points, using
 * three different prime bases, four for 4D, etc. SobolQRNG can be used for the same purpose, but the points it
 * generates are typically more closely-aligned to a specific pattern, the pattern is symmetrical between all four
 * quadrants of the square between (0.0, 0.0) and (1.0, 1.0) in 2D, and it probably extends into higher dimensions.
 * Using one of the many possible Halton sequences gives some more flexibility in the kinds of procedural random-like
 * points produced.
 * <br>
 * Gray codes sound much more complex than they actually are; the sequence of Gray codes for the integers from 0 to 16
 * is {@code 0, 1, 3, 2, 6, 7, 5, 4, 12, 13, 15, 14, 10, 11, 9, 8, 24}, and this can be generated efficiently with
 * {@code i ^ (i >> 1)} *see note. Gray codes, as we use them here, simply are sequences of numbers where exactly one
 * bit changes between the binary representations of successive Gray codes. There is always a unique Gray code for any
 * non-negative integer, and all non-negative integers are possible as Gray codes. Because of this, changing the state
 * via Gray code gives a moderately-good scramble without significantly reducing the total amount of numbers a
 * VanDerCorputQRNG can generate before repeating.
 * <br>
 * Expected output for {@link #nextDouble()} called 33 times on an instance made with {@code new VanDerCorputQRNG(11, 83L)}:
 * 0.4552967693463561, 0.546205860255447, 0.3643876784372652, 0.2734785875281743, 0.628099173553719,
 * 0.71900826446281, 0.9008264462809917, 0.8099173553719008, 0.4462809917355372, 0.5371900826446281,
 * 0.35537190082644626, 0.2644628099173554, 0.3305785123966942, 0.4214876033057851, 0.6033057851239669,
 * 0.512396694214876, 0.8760330578512396, 0.9669421487603306, 0.7851239669421488, 0.6942148760330579,
 * 0.4297520661157025, 0.5206611570247934, 0.7024793388429752, 0.6115702479338843, 0.24793388429752067,
 * 0.33884297520661155, 0.15702479338842976, 0.06611570247933884, 0.5950413223140496, 0.6859504132231405,
 * 0.8677685950413223, 0.7768595041322314, 0.1487603305785124
 * <br>
 * Expected output for {@link #nextDouble()} called 33 times on an instance made with {@code new VanDerCorputQRNG(19, 83L)}:
 * 0.6481994459833795, 0.7008310249307479, 0.5955678670360111, 0.5429362880886427, 0.12188365650969529,
 * 0.1745152354570637, 0.27977839335180055, 0.22714681440443213, 0.01662049861495845, 0.06925207756232687,
 * 0.961218836565097, 0.9085872576177285, 0.22160664819944598, 0.2742382271468144, 0.37950138504155123,
 * 0.3268698060941828, 0.5373961218836565, 0.590027700831025, 0.48476454293628807, 0.43213296398891965,
 * 0.853185595567867, 0.9058171745152355, 0.013850415512465374, 0.9584487534626038, 0.7479224376731302,
 * 0.8005540166204986, 0.6952908587257618, 0.6426592797783933, 0.7977839335180056, 0.850415512465374,
 * 0.9556786703601108, 0.9030470914127424, 0.11634349030470914
 * <br>
 * Note on Gray code implementation: typically the unsigned right shift would be used instead of the example code, which
 * could look like {@code i ^ (i >>> 1)}, but using the sign-extending right shift here has a useful property of making
 * this only output positive Gray codes.
 * Created by Tommy Ettinger on 11/18/2016.
 */
public class VanDerCorputQRNG implements StatefulRandomness, RandomnessSource, Serializable {
    private static final long serialVersionUID = 1;
    public long state;
    public final int base;

    /**
     * Constructs a new van der Corput sequence generator with base 13 and starting point 83.
     */
    public VanDerCorputQRNG()
    {
        base = 13;
        state = 83;
    }

    /**
     * Constructs a new van der Corput sequence generator with the given starting point in the sequence as a seed.
     * Usually seed should be at least 20 with this constructor, but not drastically larger; 2000 is probably too much.
     * This will use a base 13 van der Corput sequence.
     * @param seed the seed as a long that will be used as the starting point in the sequence; ideally positive but low
     */
    public VanDerCorputQRNG(long seed)
    {
        base = 13;
        state = seed;
    }

    /**
     * Constructs a new van der Corput sequence generator with the given base (a.k.a. radix; if given a base less than
     * 2, this will use base 2 instead) and starting point in the sequence as a seed. Good choices for base are between
     * 10 and 60 or so, and should usually be prime. Good choices for seed are larger than base but not by very much,
     * and should generally be positive at construction time.
     * @param base the base or radix used for this VanDerCorputQRNG; for most uses this should be prime but small-ish
     * @param seed the seed as a long that will be used as the starting point in the sequence; ideally positive but low
     */
    public VanDerCorputQRNG(int base, long seed)
    {
        this.base = base < 2 ? 2 : base;
        state = seed;
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
        long s = ++state ^ (state >> 1), // intentionally non-standard Gray code; ensures s is non-negative
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
     * have already been generated. Certain unusual bases may make this more likely.
     * @return a quasi-random double that will always be less than 1.0 and will be no lower than 0.0
     */
    public double nextDouble() {
        long s = ++state ^ (state >> 1), // intentionally non-standard Gray code; ensures s is non-negative
                num = s % base, den = base;
        while (den <= s) {
            num *= base;
            num += (s % (den * base)) / den;
            den *= base;
        }
        return num / ((double)den);
    }

    @Override
    public VanDerCorputQRNG copy() {
        return new VanDerCorputQRNG(base, state);
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
                " and state 0x" + StringKit.hex(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VanDerCorputQRNG that = (VanDerCorputQRNG) o;
        return state == that.state && base == that.base;
    }

    @Override
    public int hashCode() {
        int result = (int) (state ^ (state >>> 32));
        result = 31 * result + base;
        return result;
    }
}
