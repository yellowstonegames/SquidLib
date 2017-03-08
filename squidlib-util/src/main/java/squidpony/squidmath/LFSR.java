/*  Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package squidpony.squidmath;

import squidpony.StringKit;
import squidpony.annotation.Beta;

import java.io.Serializable;

/**
 * A Linear Feedback Shift Register that may be used like a StatefulRandomness but is not truly random. This has a
 * period of (2 to the 64) minus 1, and is based on Wikipedia's code for a Galois LFSR but uses data from
 * http://web.archive.org/web/20161007061934/http://courses.cse.tamu.edu/csce680/walker/lfsr_table.pdf .
 * It is important to note that an LFSR will produce each number from 1 until its maximum exactly once before repeating,
 * so this may be useful as a way of generating test data in an unpredictable order.
 * @author Tommy Ettinger
 */
@Beta
public class LFSR implements StatefulRandomness, Serializable {

	private static final long DOUBLE_MASK = (1L << 53) - 1;
    private static final double NORM_53 = 1. / (1L << 53);
    private static final long FLOAT_MASK = (1L << 24) - 1;
    private static final double NORM_24 = 1. / (1L << 24);

	private static final long serialVersionUID = -2373549048478690398L;

    public long state;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public LFSR() {
        this((long) (Math.random() * Long.MAX_VALUE));
    }

    public LFSR(final long seed) {
        setState(seed);
    }

    public LFSR(final CharSequence seed)
    {
        this(CrossHash.Wisp.hash64(seed));
    }


    @Override
    public int next(int bits) {
        return (int) (nextLong() & (1L << bits) - 1);
    }

    @Override
    public long nextLong() {
        long lsb = state & 1L;
        state >>>= 1;
        return (state ^= -lsb & 0xD800000000000000L);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public RandomnessSource copy() {
        return new LFSR(state);
    }


    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public int nextInt() {
        return (int)nextLong();
    }

    /**
     * Exclusive on the upper bound.  The lower bound is 0.
     * @param bound the upper bound; should be positive
     * @return a random int less than n and at least equal to 0
     */
    public int nextInt( final int bound ) {
        return (int)((bound * (nextLong() & 0x7FFFFFFFL)) >> 31);
    }
    /**
     * Inclusive lower, exclusive upper.
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, should be positive, must be greater than lower
     * @return a random int at least equal to lower and less than upper
     */
    public int nextInt( final int lower, final int upper ) {
        if ( upper - lower <= 0 ) throw new IllegalArgumentException("Upper bound must be greater than lower bound");
        return lower + nextInt(upper - lower);
    }

    /**
     * Exclusive on the upper bound. The lower bound is 0.
     * @param bound the upper bound; should be positive
     * @return a random long less than n
     */
    public long nextLong( final long bound ) {
        if ( bound <= 0 ) return 0;
        long threshold = (0x7fffffffffffffffL - bound + 1) % bound;
        for (;;) {
            long bits = nextLong() & 0x7fffffffffffffffL;
            if (bits >= threshold)
                return bits % bound;
        }
    }

    public double nextDouble() {
        return (nextLong() & DOUBLE_MASK) * NORM_53;
    }

    public float nextFloat() {
        return (float) ((nextLong() & FLOAT_MASK) * NORM_24);
    }

    public boolean nextBoolean() {
        return nextLong() < 0L;
    }

    public void nextBytes(final byte[] bytes) {
        int i = bytes.length, n = 0;
        while (i != 0) {
            n = Math.min(i, 8);
            for (long bits = nextLong(); n-- != 0; bits >>>= 8) {
                bytes[--i] = (byte) bits;
            }
        }
    }

    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    @Override
    public long getState() {
        return state;
    }

    /**
     * Sets the seed of this generator using one long, running that through LightRNG's algorithm twice to get the state.
     * @param seed the number to use as the seed
     */
    public void setState(final long seed) {
        if(seed == 0)
            state = -1L;
        else
            state = seed;
    }

    @Override
    public String toString() {
        return "LFSR with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LFSR lfsr = (LFSR) o;

        return (state == lfsr.state);
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }


    /**
     * Gets the next number that an LFSR would produce using {@link #nextLong()} if its state was {@code state}.
     * Does not allow state to be 0.
     * @param state any long other than 0
     * @return the next long that an LFSR would produce with the given state
     */
    public static long determine(long state)
    {

        long lsb = state & 1L;
        state >>>= 1;
        return state ^ (-lsb & 0xD800000000000000L);
    }

    /**
     * Gets the next number that an LFSR would produce using {@link #nextInt(int)} if its state was {@code state} and
     * {@code bound} was passed to nextInt(). Does not allow state to be 0.
     * @param state any long other than 0
     * @param bound the exclusive bound on the result as an int; does better if the bound is not too high (below 10000?)
     * @return the next int that an LFSR would produce with the given state and bound
     */
    public static int determineBounded(final long state, final int bound)
    {
        return (int)((bound * ((state >>> 1) & 0x7FFFFFFFL)) >>> 31);
    }
}
