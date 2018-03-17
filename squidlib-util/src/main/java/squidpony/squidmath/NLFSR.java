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
 * A Non-Linear Feedback Shift Register that may be used like a StatefulRandomness but is not truly random. This is
 * based on the {@link LFSR} class, and is less predictable but is otherwise less than optimal in some ways. It has a
 * period of (2 to the 27) minus 1, and uses data from
 * http://people.kth.se/~dubrova/nlfsr.html and https://eprint.iacr.org/2012/314.pdf . You would normally only prefer
 * NLFSR over LFSR if you expect players to scrutinize your randomly-generated data, or if you want to use it as part of
 * a more complex process such as encoding a saved file in a more robust way.
 * It is important to note that an NLFSR or LFSR will produce each number from 1 until its maximum exactly once before
 * repeating, so this may be useful as a way of generating test data in an unpredictable order.
 * @author Tommy Ettinger
 */
@Beta
public class NLFSR implements StatefulRandomness, Serializable {

	private static final long DOUBLE_MASK = (1L << 53) - 1;
    private static final double NORM_53 = 1. / (1L << 53);
    private static final long FLOAT_MASK = (1L << 24) - 1;
    private static final double NORM_24 = 1. / (1L << 24);

	private static final long serialVersionUID = -2373549048478690398L;

    public int state;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public NLFSR() {
        this((int) (Math.random() * Long.MAX_VALUE));
    }

    public NLFSR(final int seed) {
        if(seed <= 0 || seed > 134217727)
            state = 134217727;
        else
            state = seed;
    }

    public NLFSR(final CharSequence seed)
    {
        this(CrossHash.hash(seed));
    }


    @Override
    public int next(int bits) {
        return (int) (nextLong() >>> (64 - bits));
    }

    @Override
    public long nextLong() {
        return nextInt() * 0x2000000000L ^ nextInt() * 0x40000L ^ nextInt();
    }

    /**
     * Produces up to 27 bits of random int, with a minimum result of 1 and a max of 134217727 (both inclusive).
     * @return a random int between 1 and 134217727, both inclusive
     */
    public int nextInt() {
        return state = (state >>> 1 | (0x4000000 & (
                (state << 26) //0
                        ^ (state << 22) //4
                        ^ (state << 18) //8
                        ^ (state << 17) //9
                        ^ (state << 15) //11
                        ^ (state << 14) //12
                        ^ (state << 11) //15
                        ^ (state << 10) //16
                        ^ (state << 3)  //23
                        ^ ((state << 14) & (state << 4)) //12 22
                        ^ ((state << 13) & (state << 3)) //13 23
                        ^ ((state << 13) & (state << 1)) //13 25
                        ^ ((state << 4) & (state << 3))  //22 23
                        ^ ((state << 19) & (state << 18) & (state << 2))  //7 8 24
                        ^ ((state << 14) & (state << 12) & (state))       //12 14 26
                        ^ ((state << 20) & (state << 15) & (state << 7) & (state << 4))       //6 11 19 22

        )));
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public NLFSR copy() {
        return new NLFSR(state);
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
        return (nextInt() & 1) == 0;
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
    @Override
    public void setState(final long seed) {
         state = (int) ((seed & 0x7FFFFFFFFFFFFFFFL) % 134217727) + 1;
    }

    @Override
    public String toString() {
        return "NLFSR with state 0x" + StringKit.hex(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NLFSR nlfsr = (NLFSR) o;

        return (state == nlfsr.state);
    }

    @Override
    public int hashCode() {
        return state;
    }
}
