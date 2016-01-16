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

/**
 * This is a RandomnessSource in the PCG-Random family. It performs pseudo-
 * random modifications to the output based on the techniques from the
 * Permuted Congruential Generators created by M.E. O'Neill.
 * Specifically, this variant is:
 * RXS M XS -- random xorshift, mcg multiply, fixed xorshift
 *
 * The most statistically powerful generator, but all those steps
 * make it slower than some of the others.
 *
 * Because it's usually used in contexts where the state type and the
 * result type are the same, it is a permutation and is thus invert-able.
 * We thus provide a (protected) function to invert it.
 * <br>
 * It should actually be somewhat faster than LightRNG, though this needs
 * to be tested.
 * @author Melissa E. O'Neill (Go HMC!)
 * @author Tommy Ettinger
 */
public class PermutedRNG implements RandomnessSource, StatefulRandomness
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

	private static final long serialVersionUID = 3291443966125527620L;

    /** Creates a new generator seeded using Math.random. */
    public PermutedRNG() {
        this((long)Math.floor(Math.random() * Long.MAX_VALUE));
    }

    public PermutedRNG(final long seed) {
        state = (seed + 1442695040888963407L) * 6364136223846793005L + 1442695040888963407L;
    }

    @Override
    public int next( int bits ) {
        return (int)( nextInt() & ( 1L << bits ) - 1 );
    }


    /**
     * From the PCG-Random source:
     * XorShifts are invert-able, but they are something of a pain to invert.
     * This function backs them out.
     * @param n a XorShift-ed value
     * @param bits the number of bits we still need to invert, not constant
     * @param shift the crazy one; the wild-card; it's some weird value every time it's used
     * @return a long that inverts the shift done to n
     */
    private static long unxorshift(long n, int bits, int shift)
    {
        if (2*shift >= bits) {
            return n ^ (n >>> shift);
        }
        long lowmask1 = (1L << (bits - shift*2)) - 1;
        long highmask1 = ~lowmask1;
        long top1 = n;
        long bottom1 = n & lowmask1;
        top1 ^= top1 >>> shift;
        top1 &= highmask1;
        n = top1 | bottom1;
        long lowmask2 = (1L << (bits - shift)) - 1;
        long bottom2 = n & lowmask2;
        bottom2 = unxorshift(bottom2, bits - shift, shift);
        bottom2 &= lowmask1;
        return top1 | bottom2;
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public int nextInt() {
        return (int)(nextLong() >> 32);
    }
    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * Generates two 32-bit integers using two calls to nextInt() and combines them into one long.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public long nextLong()
    {
        // increment  = 1442695040888963407L;
        // multiplier = 6364136223846793005L;

        final long old = permute(state);
        state = state * 6364136223846793005L + 1442695040888963407L;
        return old;
    }
    private static long permute(long p)
    {
        p ^= p >>> (5 + ((p >>> 59) & 31));
        p *= -5840758589994634535L;
        return p ^ (p >>> 43);

    }

    protected static long invert(long internal)
    {
        internal = unxorshift(internal, 64, 43);
        internal *= -3437190434928431767L;
        return unxorshift(internal, 64, 5 + ((int)(internal >>> 59) & 31));
    }

    /**
     * Exclusive on the upper bound n.  The lower bound is 0.
     * @param n the upper bound; should be positive
     * @return a random int less than n and at least equal to 0
     */
    public int nextInt( final int n ) {
        if ( n <= 0 ) return 0;
        int threshold = (0x7fffffff - n + 1) % n;
        for (;;) {
            int bits = (int)(nextLong() & 0x7fffffff);
            if (bits >= threshold)
                return bits % n;
        }
    }

    /**
     * Inclusive lower, exclusive upper.
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
     * Will call nextInt() 2 times.
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
     * Will call nextInt() at least 2 times, possibly more.
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
     * Calls nextInt() exactly two times.
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
     * Calls nextInt() exactly two times.
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
     * Calls nextInt() exactly one time.
     *
     * @return a random float at least equal to 0.0f and less than 1.0f
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
        return ( nextLong() & 1L ) != 0L;
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
     * @return the current seed of this PermutedRNG, changed once per call to nextInt()
     */
    @Override
	public long getState( ) {
        return state;
    }

    /**
     * Advances or rolls back the PermutedRNG's state without actually generating numbers. Skip forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextInt().
     * @param advance Number of future generations to skip past. Can be negative to backtrack.
     * @return the state after skipping.
     */
    public long skip(long advance)
    {
        // The method used here is based on Brown, "Random Number Generation
        // with Arbitrary Stride,", Transactions of the American Nuclear
        // Society (Nov. 1994).  The algorithm is very similar to fast
        // exponentiation.
        //
        // Even though advance is a signed long, it is treated as unsigned, effectively, for the purposes
        // of how many iterations it goes through (at most 63 for forwards, 64 for "backwards").
        if(advance == 0)
            return state;
        long acc_mult = 1, acc_plus = 0, cur_mult = 6364136223846793005L, cur_plus = 1442695040888963407L;

        do {
            if ((advance & 1L) != 0L) {
                acc_mult *= cur_mult;
                acc_plus = acc_plus*cur_mult + cur_plus;
            }
            cur_plus = (cur_mult+1L)*cur_plus;
            cur_mult *= cur_mult;
            advance >>>= 1;
        }while (advance > 0L);
        return acc_mult * state + acc_plus;
    }
}
