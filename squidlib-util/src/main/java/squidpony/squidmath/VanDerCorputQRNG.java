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
 * for making points that should not overlap), but it will seem much more random when scramble is true; and this is a
 * StatefulRandomness with an additional method for generating quasi-random doubles, {@link #nextDouble()}.
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
 * 2D or 3D, when scramble is false. Using a VanDerCorputQRNG with base 3 for the x-axis and another VanDerCorputQRNG
 * with base 5 for the y-axis, requesting a double from each to make points between (0.0, 0.0) and (1.0, 1.0), has an
 * interesting trait that can be desirable for many kinds of positioning in 2D: once a point has been generated, an
 * identical point will never be generated until floating-point precision runs out, but more than that, nearby points
 * will almost never be generated for many generations, and most points will stay at a comfortable distance from each
 * other if the bases are different and both prime (as well as, more technically, if they share no common denominators).
 * This is also known as a Halton sequence, which is a group of sequences of points instead of simply numbers. The
 * choices of 3 and 5 are just examples; any two different primes will technically work for 2D, but patterns can become
 * noticeable with primes larger than about 7, with 11 and 13 sometimes acceptable. Three VanDerCorputQRNG sequences
 * could be used for a Halton sequence of 3D points, using three different prime bases, four for 4D, etc. SobolQRNG can
 * be used for the same purpose, but the points it generates are typically more closely-aligned to a specific pattern,
 * the pattern is symmetrical between all four quadrants of the square between (0.0, 0.0) and (1.0, 1.0) in 2D, and it
 * probably extends into higher dimensions. Using one of the possible Halton sequences gives some more flexibility in
 * the kinds of procedural random-like points produced.
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
        base = 13;
        state = 83;
        scramble = true;
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
        scramble = true;
    }

    /**
     * Constructs a new van der Corput sequence generator with the given base (a.k.a. radix; if given a base less than
     * 2, this will use base 2 instead) and starting point in the sequence as a seed. Good choices for base are between
     * 10 and 60 or so, and should usually be prime. Good choices for seed are larger than base but not by very much,
     * and should generally be positive at construction time.
     * @param base the base or radix used for this VanDerCorputQRNG; for most uses this should be prime but small-ish
     * @param seed the seed as a long that will be used as the starting point in the sequence; ideally positive but low
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
        long s = (scramble) ? ++state : (++state * state) ^ (state * 137 >> 4),
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
        long s = (scramble) ? ++state : (++state * state) ^ (state * 137 >> 4),
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
}
