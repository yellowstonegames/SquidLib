/*
Written in 2015 by Sebastiano Vigna (vigna@acm.org)

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package squidpony.squidmath;

/**
 * This is a SplittableRandom-style generator, meant to have a tiny state
 * that permits storing many different generators with low overhead.
 * It should be rather fast, though no guarantees can be made.
 * Written in 2015 by Sebastiano Vigna (vigna@acm.org)
 * @author Sebastiano Vigna
 * @author Tommy Ettinger
 */
public class LightRNG implements RandomnessSource, StatefulRandomness
{
	/** 2 raised to the 53, - 1. */
    private static final long DOUBLE_MASK = ( 1L << 53 ) - 1;
    /** 2 raised to the -53. */
    private static final double NORM_53 = 1. / ( 1L << 53 );
    /** 2 raised to the 24, -1. */
    private static final long FLOAT_MASK = ( 1L << 24 ) - 1;
    /** 2 raised to the -24. */
    private static final double NORM_24 = 1. / ( 1L << 24 );

	private static final long serialVersionUID = -1656615589113474497L;

    public long state; /* The state can be seeded with any value. */

    /** Creates a new generator seeded using Math.random. */
    public LightRNG() {
        this((long) Math.floor(Math.random() * Long.MAX_VALUE));
    }

    public LightRNG( final long seed ) {
        setSeed(seed);
    }

    @Override
    public int next( int bits ) {
        return (int)( nextLong() & ( 1L << bits ) - 1 );
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     * @return any long, all 64 bits are random
     */
    public long nextLong() {
        long z = ( state += 0x9E3779B97F4A7C15l );
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9l;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBl;
        return z ^ (z >>> 31);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public int nextInt() {
        return (int)nextLong();
    }

    /**
     * Exclusive on the upper bound n.  The lower bound is 0.
     * @param n the upper bound; should be positive
     * @return a random int less than n and at least equal to 0
     */
    public int nextInt( final int n ) {
        if ( n <= 0 ) throw new IllegalArgumentException();
        //for(;;) {
            final int bits = nextInt() >>> 1;
        return bits % n;
            //int value = bits % n;
            //value = (value < 0) ? -value : value;
            //if ( bits - value + ( n - 1 ) >= 0 ) return value;
        //}
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
     * @param n the upper bound; should be positive
     * @return a random long less than n
     */
    public long nextLong( final long n ) {
        if ( n <= 0 ) throw new IllegalArgumentException();
        //for(;;) {
            final long bits = nextLong() >>> 1;
        return bits % n;
            //long value = bits % n;
            //value = (value < 0) ? -value : value;
            //if ( bits - value + ( n - 1 ) >= 0 ) return value;
        //}
    }

    /**
     * Inclusive lower, exclusive upper.
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, should be positive, must be greater than lower
     * @return a random long at least equal to lower and less than upper
     */
    public long nextLong( final long lower, final long upper ) {
        if ( upper - lower <= 0 ) throw new IllegalArgumentException();
        return lower + nextLong(upper - lower);
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
        return (float)( ( nextLong() & FLOAT_MASK ) * NORM_24 );
    }

    /**
     * Gets a random value, true or false.
     * Calls nextLong() once.
     * @return a random true or false value.
     */
    public boolean nextBoolean() {
        return ( nextLong() & 1 ) != 0L;
    }

    /**
     * Given a byte array as a parameter, this will fill the array with random bytes (modifying it
     * in-place). Calls nextLong() {@code Math.ceil(bytes.length / 8.0)} times.
     * @param bytes a byte array that will have its contents overwritten with random bytes.
     */
    public void nextBytes( final byte[] bytes ) {
        int i = bytes.length, n = 0;
        while( i != 0 ) {
            n = Math.min( i, 8 );
            for ( long bits = nextLong(); n-- != 0; bits >>= 8 ) bytes[ --i ] = (byte)bits;
        }
    }



    /**
     * Sets the seed of this generator (which is also the current state).
     * @param seed the seed to use for this LightRNG, as if it was constructed with this seed.
     */
    public void setSeed( final long seed ) {
        state = seed;
    }
    /**
     * Sets the seed (also the current state) of this generator.
     * @param seed the seed to use for this LightRNG, as if it was constructed with this seed.
     */
    @Override
    public void setState( final long seed ) {
        state = seed;
    }
    /**
     * Gets the current state of this generator.
     * @return the current seed of this LightRNG, changed once per call to nextLong()
     */
    @Override
    public long getState( ) {
        return state;
    }

    /**
     * Advances or rolls back the LightRNG's state without actually generating numbers. Skip forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextInt().
     * @param advance Number of future generations to skip past. Can be negative to backtrack.
     * @return the state after skipping.
     */
    public long skip(long advance)
    {
        return state += 0x9E3779B97F4A7C15l * advance;
    }

}
