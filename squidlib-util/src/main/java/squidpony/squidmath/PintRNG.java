package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A RandomnessSource based on PCG-Random that has a single int of state. Its period is extremely short at 2 to the 32,
 * but its quality over that period is high even though its speed is not especially noteworthy. This generator is not
 * suitable for GWT; for that you should use {@link Zag32RNG} if you need a StatefulRandomness or {@link Oriole32RNG} if
 * you need higher period and speed but don't need StatefulRandomness' state adjustment methods.
 * <br>
 * Quality should be excellent in this version (at least for a generator with so little state) since it's based directly
 * on PCG-Random's choices of numerical constants. Visual tests, at least, appear indistinguishable from other PRNGs.
 * Period is very low, at 2 to the 32, but all seeds should be valid, including 0. Generating 64 bits of random data
 * takes a little less than twice as much time as generating 32 bits, since this can avoid some overhead via inlining.
 * <br>
 * The name can be construed as Pint-Size, since this has a small period and uses a smaller amount of space, or as
 * Permuted Int, since this is based on PermutedRNG, changed to use 32-bit operations on ints.
 * <br>
 * Based on work by Melissa E. O'Neill for PCG-Random; some code has been ported more-directly than other sections, but
 * the foundation for this class would not be possible without O'Neill's work.
 * <br>
 * Created by Tommy Ettinger on 11/15/2016.
 */
public final class PintRNG implements RandomnessSource, StatefulRandomness, Serializable {

    /** 2 raised to the 53, - 1. */
    private static final long DOUBLE_MASK = ( 1L << 53 ) - 1;
    /** 2 raised to the -53. */
    private static final double NORM_53 = 1. / ( 1L << 53 );
    /** 2 raised to the 24, -1. */
    private static final long FLOAT_MASK = ( 1L << 24 ) - 1;
    /** 2 raised to the -24. */
    private static final double NORM_24 = 1. / ( 1L << 24 );

    private static final long serialVersionUID = -374415589203474497L;

    public int state; /* The state can be seeded with any value. */

    /** Creates a new generator seeded using Math.random. */
    public PintRNG() {
        this((int)((Math.random() - 0.5) * 4.294967296E9));
    }

    public PintRNG( final long seed ) {
        setState(seed);
    }

    public PintRNG(final int a)
    {
        state = a;
    }

    @Override
    public int next( int bits )
    {
        int p = (state = state * 0x2C9277B5 + 0xAC564B05);
        p ^= p >>> (4 + (p >>> 28));
        return (((p *= 0x108EF2D9) >>> 22) ^ p) >>> (32 - bits);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public int nextInt() {
        // increment  = 2891336453;
        // multiplier = 747796405;

//        int p = state;
//        p ^= p >>> (4 + (p >>> 28));
//        state = state * 0x2C9277B5 + 0xAC564B05;
        int p = (state = state * 0x2C9277B5 + 0xAC564B05);
        p ^= p >>> (4 + (p >>> 28));
        return ((p *= 0x108EF2D9) >>> 22) ^ p;
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     * Internally, generates two random 32-bit values and combines them into one random long.
     * @return any long, all 64 bits are random
     */
    @Override
    public long nextLong() {
        int p = (state = state * 0x2C9277B5 + 0xAC564B05);
        p ^= p >>> (4 + (p >>> 28));
        int q = (state = state * 0x2C9277B5 + 0xAC564B05);
        q ^= q >>> (4 + (q >>> 28));
        return (((p *= 0x108EF2D9) >>> 22) ^ p) | ((((q *= 0x108EF2D9) >>> 22) ^ q) & 0xffffffffL) << 32;
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public PintRNG copy() {
        return new PintRNG(state);
    }

    /**
     * Exclusive on the upper bound.  The lower bound is 0.
     * <br>
     * Credit goes to Daniel Lemire, http://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/
     * @param bound the upper bound; should be positive
     * @return a random int less than n and at least equal to 0
     */
    public int nextInt( final int bound ) {
        return (bound <= 0) ? 0 : (int)((bound * (nextInt() & 0x7FFFFFFFL)) >> 31);
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
     * Gets a uniform random double in the range [0.0,1.0)
     * @return a random double at least equal to 0.0 and less than 1.0
     */
    public double nextDouble() {
        return ( nextLong() & DOUBLE_MASK ) * NORM_53;
    }

    /**
     * Gets a uniform random double in the range [0.0,outer) given a positive parameter outer. If outer
     * is negative, it will be the (exclusive) lower bound and 0.0 will be the (inclusive) upper bound.
     * @param outer the exclusive outer bound, can be negative
     * @return a random double between 0.0 (inclusive) and outer (exclusive)
     */
    public double nextDouble(final double outer) {
        return nextDouble() * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public float nextFloat() {
        return (float)( ( nextInt() & FLOAT_MASK ) * NORM_24 );
    }

    /**
     * Gets a random value, true or false.
     * Calls nextInt() once.
     * @return a random true or false value.
     */
    public boolean nextBoolean() {
        return nextInt() > 0;
    }

    /**
     * Given a byte array as a parameter, this will fill the array with random bytes (modifying it
     * in-place). Calls nextInt() {@code Math.ceil(bytes.length / 4.0)} times.
     * @param bytes a byte array that will have its contents overwritten with random bytes.
     */
    public void nextBytes( final byte[] bytes ) {
        int i = bytes.length, n = 0;
        while( i != 0 ) {
            n = Math.min( i, 4 );
            for ( int bits = nextInt(); n-- != 0; bits >>>= 8 ) bytes[ --i ] = (byte)bits;
        }
    }



    /**
     * Sets the current state of this generator (an int) using only the least-significant 32 bits of seed (by casting
     * a mask of those bits in seed to int, which helps ensure that a full 32 bits of state are possible). Giving
     * int seeds should set the seed to an identical int; long seeds will lose any information in higher bits (including
     * the sign, so 0xFFFFFFFF00000000L, which is a negative long, would be treated as 0 since only the 0x00000000 part
     * at the end is actually used).
     * @param seed the seed to use for this PintRNG, as if it was constructed with this seed.
     */
    @Override
    public void setState( final long seed ) {
        state = (int)(seed & 0xFFFFFFFFL);
    }
    /**
     * Gets the current state of this generator.
     * @return the current seed of this PintRNG, changed once per call to nextInt()
     */
    @Override
    public long getState() {
        return state;
    }

    @Override
    public String toString() {
        return "PintRNG with state 0x" + StringKit.hex(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PintRNG pintRNG = (PintRNG) o;

        return state == pintRNG.state;

    }

    @Override
    public int hashCode() {
        return state;
    }
    /**
     * Advances or rolls back the PintRNG's state without actually generating each number. Skip forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextInt(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     * <br>
     * The method used here is based on Brown, "Random Number Generation with Arbitrary Stride,", Transactions of the
     * American Nuclear Society (Nov. 1994). The code is mostly the same as in PCG-Random's C port by M.E. O'Neill,
     * specifically <a href="https://github.com/imneme/pcg-c/blob/master/src/pcg-advance-32.c">this file</a>. Skipping
     * ahead or behind takes more than constant time, unlike with {@link LightRNG}, but less time than calling nextInt()
     * {@code advance} times. Skipping backwards by one step is the worst case for this.
     * @param advance Number of future generations to skip past. Can be negative to backtrack.
     * @return the int that would be generated after generating advance random numbers.
     */
    public int skip(int advance)
    {
        int acc_mult = 1;
        int acc_plus = 0;
        int cur_mult = 0x2C9277B5;
        int cur_plus = 0xAC564B05;
        while (advance != 0) {
            if ((advance & 1) == 1) {
                acc_mult *= cur_mult;
                acc_plus = acc_plus * cur_mult + cur_plus;
            }
            cur_plus *= (cur_mult + 1);
            cur_mult *= cur_mult;
            advance >>>= 1;
        }
        int p = (state = acc_mult * state + acc_plus);
        p ^= p >>> (4 + (p >>> 28));
        return ((p *= 0x108EF2D9) >>> 22) ^ p;
    }

    /**
     * Gets a pseudo-random int that is a permutation of {@code state}, which is an int.
     * This should normally be called with a technique like
     * {@code PintRNG.determine(state = state * 0x2C9277B5 + 0xAC564B05)}, where 0xAC564B05 can be changed to any odd
     * constant as long as it is the same across calls to this. You can effectively produce multiple uncorrelated
     * streams by adding different constants in place of 0xAC564B05, applied to different states.
     * @param state any int, but should be updated like {@code state = state * 0x2C9277B5 + 0xAC564B05}, where 0xAC564B05 can be any odd-number constant
     * @return any int, pseudo-randomly obtained from state
     */
    public static int determine(int state)
    {
        state ^= state >>> (4 + (state >>> 28));
        return ((state *= 0x108EF2D9) >>> 22) ^ state;
    }
    /**
     * Like {@link #determine(int)}, gets a pseudo-random int that is a permutation of {@code state}, which is an int.
     * Unlike determine(), this static method performs an extra step to avoid correlation between similar inputs, such
     * as 4, 5, and 6, and their outputs. If you already give very distant numbers as subsequent inputs to determine(),
     * then you should continue to use that method unless you discover issues with correlation; otherwise it's not a bad
     * idea to default to this method, though it is somewhat slower than determine(). This method is safe to use with
     * sequential ints, so you can call it with the technique {@code PintRNG.disperse(++state)}, or just use it on int
     * data as you obtain it to randomize its values.
     * @param state any int
     * @return any int, pseudo-randomly obtained from state
     */
    public static int disperse(int state)
    {
        state = ((state >>> 19 | state << 13) ^ 0x13A5BA1D);
        state ^= state >>> (4 + (state >>> 28));
        return ((state *= 0x108EF2D9) >>> 22) ^ state;
    }
    public static int determine(final int a, final int b)
    {
        int state = a * 0x9E3779B9 + b * 0x85157AF5;
        state ^= state >>> (4 + (state >>> 28));
        return ((state *= 0x108EF2D9) >>> 22) ^ state;
    }

    public static int determineBounded(int state, final int bound)
    {
        state ^= state >>> (4 + (state >>> 28));
        return (int)((bound * ((((state *= 0x108EF2D9) >>> 22) ^ state) & 0x7FFFFFFFL)) >>> 31);
    }
    public static int disperseBounded(int state, final int bound)
    {
        state = ((state >>> 19 | state << 13) ^ 0x13A5BA1D);
        state ^= state >>> (4 + (state >>> 28));
        return (int)((bound * ((((state *= 0x108EF2D9) >>> 22) ^ state) & 0x7FFFFFFFL)) >>> 31);
    }

}
