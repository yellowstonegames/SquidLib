/*
 * Ported to Java from the PCG library. Its copyright header follows:
 *
 * PCG Random Number Generation for C++
 *
 * Copyright 2014 Melissa O'Neill <oneill@pcg-random.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For additional information about the PCG random number generation scheme,
 * including its license and other licensing options, visit
 *
 *     http://www.pcg-random.org
 */
package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * This is a RandomnessSource in the PCG-Random family. It performs pseudo-
 * random modifications to the output based on the techniques from the
 * Permuted Congruential Generators created by M.E. O'Neill.
 * Specifically, this variant is:
 * RXS M XS -- random xorshift, mcg multiply, fixed xorshift
 *
 * The most statistically powerful generator, but all those steps
 * make it slower than some of the others.
 * <br>
 * Even though benchmarks on similar programs in C would lead you to
 * believe this should be somewhat faster than LightRNG, benchmarking
 * with JMH seems to show LightRNG being roughly 16% faster than
 * PermutedRNG, and both drastically faster than java.util.Random .
 * @author Melissa E. O'Neill (Go HMC!)
 * @author Tommy Ettinger
 * @see PintRNG PintRNG is similar to this algorithm but uses only 32-bit math, where possible.
 */
public class PermutedRNG implements RandomnessSource, StatefulRandomness, SkippingRandomness, Serializable
{
	/** 2 raised to the 53, - 1. */
    private static final long DOUBLE_MASK = ( 1L << 53 ) - 1;
    /** 2 raised to the -53. */
    private static final double NORM_53 = 1. / ( 1L << 53 );
    /** 2 raised to the 24, -1. */
    private static final long FLOAT_MASK = ( 1L << 24 ) - 1;
    /** 2 raised to the -24. */
    private static final double NORM_24 = 1. / ( 1L << 24 );
    /**
     * The state can be seeded with any value.
     */
    public long state;

	private static final long serialVersionUID = 3637443966125527620L;

    /** Creates a new generator seeded using Math.random. */
    public PermutedRNG() {
        this((long)Math.floor(Math.random() * Long.MAX_VALUE));
    }

    public PermutedRNG(final long seed) {
        state = (seed + 1442695040888963407L) * 6364136223846793005L + 1442695040888963407L;
    }

    @Override
    public int next( final int bits ) {
        return (int)( nextLong() & ( 1L << bits ) - 1 );
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * Calls nextLong() exactly one time.
     * @return any int, all 32 bits are random
     */
    public int nextInt() {
        return (int)nextLong();
    }
    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public long nextLong()
    {
        // increment  = 1442695040888963407L;
        // multiplier = 6364136223846793005L;

        long p = (state += 0x9E3779B97F4A7C15L);
        p ^= p >>> (5 + (p >>> 59));
        //state = state * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL;
        return ((p *= 0xAEF17502108EF2D9L) >>> 43) ^ p;
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
        PermutedRNG next = new PermutedRNG(state);
        next.setState(state);
        return next;
    }

    /**
     * Exclusive on the upper bound n.  The lower bound is 0.
     * Will call nextLong() with no arguments at least 1 time, possibly more.
     * @param bound the upper bound; should be positive
     * @return a random int less than n and at least equal to 0
     */
    public int nextInt( final int bound ) {
        if (bound <= 0) return 0;
        return (int)((bound * (nextLong() & 0x7FFFFFFFL)) >> 31);
    }

    /**
     * Inclusive lower, exclusive upper.
     * Will call nextLong() with no arguments at least 1 time, possibly more.
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, should be positive, must be greater than lower
     * @return a random int at least equal to lower and less than upper
     */
    public int nextInt( final int lower, final int upper ) {
        if ( upper - lower <= 0 ) throw new IllegalArgumentException();
        return lower + nextInt(upper - lower);
    }

    /**
     * Exclusive on the upper bound n. The lower bound is 0.
     *
     * Will call nextLong() with no arguments at least 1 time, possibly more.
     * @param n the upper bound; should be positive
     * @return a random long less than n
     */
    public long nextLong( final long n ) {
        if ( n <= 0 ) return 0;
        long threshold = (0x7fffffffffffffffL - n + 1) % n;
        for (;;) {
            long bits = nextLong() & 0x7fffffffffffffffL;
            if (bits >= threshold)
                return bits % n;
        }
    }

    /**
     * Exclusive on the upper bound n. The lower bound is 0.
     *
     * Will call nextLong() at least 1 time, possibly more.
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, should be positive, must be greater than lower
     * @return a random long at least equal to lower and less than upper
     */
    public long nextLong( final long lower, final long upper ) {
        if ( upper - lower <= 0 ) return 0;
        return lower + nextLong(upper - lower);
    }

    /**
     * Gets a uniform random double in the range [0.0,1.0)
     *
     * Calls nextLong() exactly one time.
     *
     * @return a random double at least equal to 0.0 and less than 1.0
     */
    public double nextDouble() {
        return ( nextLong() & DOUBLE_MASK ) * NORM_53;
    }

    /**
     * Gets a uniform random double in the range [0.0,outer) given a positive parameter outer. If outer
     * is negative, it will be the (exclusive) lower bound and 0.0 will be the (inclusive) upper bound.
     *
     * Calls nextLong() exactly one time.
     *
     *  @param outer the exclusive outer bound, can be negative
     * @return a random double between 0.0 (inclusive) and outer (exclusive)
     */
    public double nextDouble(final double outer) {
        return nextDouble() * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     *
     * Calls nextLong() exactly one time.
     *
     * @return a random float at least equal to 0.0f and less than 1.0f
     */
    public float nextFloat() {
        return (float)( ( nextLong() & FLOAT_MASK ) * NORM_24 );
    }

    /**
     * Gets a random value, true or false.
     * Calls nextLong() once.
     * @return a random true or false value.
     */
    public boolean nextBoolean() {
        return nextLong() < 0L;
    }

    /**
     * Given a byte array as a parameter, this will fill the array with random bytes (modifying it
     * in-place). Calls nextLong() {@code Math.ceil(bytes.length / 8.0)} times.
     * @param bytes a byte array that will have its contents overwritten with random bytes.
     */
    public void nextBytes( final byte[] bytes ) {
        int i = bytes.length, n = 0;
        while( i != 0 ) {
            n = Math.min(i, 8 );
            for ( long bits = nextLong(); n-- != 0; bits >>>= 8 ) bytes[ --i ] = (byte)bits;
        }
    }


    /**
     * Sets the seed of this generator (which is also the current state).
     * @param seed the seed to use for this PermutedRNG, as if it was constructed with this seed.
     */
    public void setSeed( final long seed ) {
        state = seed;
    }
    /**
     * Sets the seed (also the current state) of this generator.
     * @param seed the seed to use for this PermutedRNG, as if it was constructed with this seed.
     */
    @Override
	public void setState( final long seed ) {
        state = seed;
    }
    /**
     * Gets the current state of this generator.
     * @return the current seed of this PermutedRNG, changed once per call to nextLong()
     */
    @Override
	public long getState( ) {
        return state;
    }

    /**
     * Advances or rolls back the PermutedRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextLong(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     * @param advance Number of future generations to skip past. Can be negative to backtrack.
     * @return the number that would be generated after generating advance random numbers.
     */
    @Override
    public long skip(final long advance)
    {
        // The method used here is based on Brown, "Random Number Generation
        // with Arbitrary Stride,", Transactions of the American Nuclear
        // Society (Nov. 1994).  The algorithm is very similar to fast
        // exponentiation.
        //
        // Even though advance is a signed long, it is treated as unsigned, effectively, for the purposes
        // of how many iterations it goes through (at most 63 for forwards, 64 for "backwards").
//        if(advance == 0)
//            return state;
//        long acc_mult = 1, acc_plus = 0, cur_mult = 6364136223846793005L, cur_plus = 1442695040888963407L;
//
//        do {
//            if ((advance & 1L) != 0L) {
//                acc_mult *= cur_mult;
//                acc_plus = acc_plus*cur_mult + cur_plus;
//            }
//            cur_plus *= (cur_mult+1L);
//            cur_mult *= cur_mult;
//            advance >>>= 1;
//        }while (advance > 0L);
//        return acc_mult * state + acc_plus;
        long p = (state += 0x9E3779B97F4A7C15L * advance);
        p ^= p >>> (5 + (p >>> 59));
        //state = state * 0x5851F42D4C957F2DL + 0x14057B7EF767814FL;
        return ((p *= 0xAEF17502108EF2D9L) >>> 43) ^ p;
    }

    @Override
    public String toString() {
        return "PermutedRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    public static long determine(long state)
    {
        state ^= state >>> (5 + (state >>> 59));
        return ((state *= 0xAEF17502108EF2D9L) >>> 43) ^ state;
    }

    public static int determineBounded(long state, final int bound)
    {
        state ^= state >>> (5 + (state >>> 59));
        return (int)((bound * ((((state *= 0xAEF17502108EF2D9L) >>> 43) ^ state) & 0x7FFFFFFFL)) >>> 31);
    }
}
