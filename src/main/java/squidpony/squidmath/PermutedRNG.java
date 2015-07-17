/*
Written in 2015 by Sebastiano Vigna (vigna@acm.org)

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package squidpony.squidmath;

/**
 * This is a SplittableRandom-style generator, meant to have a tiny state
 * that can be efficiently stored and passed, that also performs pseudo-
 * random modifications to the output based on the techniques from the
 * Permuted Congruential Generators created by M.E. O'Neill.
 * It should be rather fast, though LightRNG is probably slightly faster,
 * but the quality of this generator should be better.
 * Written in 2015 by Sebastiano Vigna (vigna@acm.org)
 * @author Sebastiano Vigna
 * @author Tommy Ettinger
 */
public class PermutedRNG implements RandomnessSource
{
    private static final long serialVersionUID = 3L;
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

    /** Creates a new generator seeded using Math.random. */
    public PermutedRNG() {
        this((long)Math.floor(Math.random() * Long.MAX_VALUE));
    }

    public PermutedRNG(final long seed) {
        this.state = seed;
    }

    @Override
    public int next( int bits ) {
        return (int)( nextInt() & ( 1L << bits ) - 1 );
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return
     */
    public int nextInt() {
        long z = ( state += 0x9E3779B97F4A7C15l );
        z = (z ^ (z >> 30)) * 0xBF58476D1CE4E5B9l;
        z = (z ^ (z >> 27)) * 0x94D049BB133111EBl;
        z = z ^ (z >> 31);
        return Integer.rotateRight((int)((z ^ (z >> 18)) >> 27), (int)(z >> 59));
    }
    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * Generates two 32-bit integers using two calls to nextInt() and combines them into one long.
     *
     * @return
     */
    public long nextLong() {
        return ((long)(nextInt()) << 32) | nextInt();
    }

    /**
     * Exclusive on the upper bound n.  The lower bound is 0.
     *
     * May call nextInt() multiple times, but usually will not.
     * @param n
     * @return
     */
    public int nextInt( final int n ) {
        if ( n <= 0 ) throw new IllegalArgumentException();
        for(;;) {
            final int bits = nextInt();
            int value = bits % n;
            value = (value < 0) ? -value : value;
            if ( bits - value + ( n - 1 ) >= 0 ) return value;
        }
    }

    /**
     * Inclusive lower, exclusive upper.
     *
     * May call nextInt() multiple times, but usually will not.
     * @param lower
     * @param upper
     * @return
     */
    public int nextInt( final int lower, final int upper ) {
        if ( upper - lower <= 0 ) throw new IllegalArgumentException();
        return lower + nextInt(upper - lower);
    }

    /**
     * Exclusive on the upper bound n. The lower bound is 0.
     *
     * Will call nextInt() at least 2 times, possibly more.
     * @param n
     * @return
     */
    public long nextLong( final long n ) {
        if ( n <= 0 ) throw new IllegalArgumentException();
        for(;;) {
            final long bits = nextLong() >>> 1;
            long value = bits % n;
            value = (value < 0) ? -value : value;
            if ( bits - value + ( n - 1 ) >= 0 ) return value;
        }
    }

    /**
     * Exclusive on the upper bound n. The lower bound is 0.
     *
     * Will call nextInt() at least 2 times, possibly more.
     * @param lower
     * @param upper
     * @return
     */
    public long nextLong( final long lower, final long upper ) {
        if ( upper - lower <= 0 ) throw new IllegalArgumentException();
        return lower + nextLong(upper - lower);
    }

    /**
     * Gets a uniform random double in the range [0.0,1.0)
     *
     * Calls nextInt() exactly two times.
     *
     * @return
     */
    public double nextDouble() {
        return ( nextLong() & DOUBLE_MASK ) * NORM_53;
    }

    /**
     * Gets a uniform random double in the range [0.0,upper) given the parameter upper.
     *
     * Calls nextInt() exactly two times.
     *
     * @param upper
     * @return
     */
    public double nextDouble(final double upper) {
        return nextDouble() * upper;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     *
     * Calls nextInt() exactly one time.
     *
     * @return
     */
    public float nextFloat() {
        return (float)( ( nextInt() & FLOAT_MASK ) * NORM_24 );
    }

    public boolean nextBoolean() {
        return ( nextInt() & 1 ) != 0;
    }

    public void nextBytes( final byte[] bytes ) {
        int i = bytes.length, n = 0;
        while( i != 0 ) {
            n = Math.min(i, 4 );
            for ( int bits = nextInt(); n-- != 0; bits >>= 8 ) bytes[ --i ] = (byte)bits;
        }
    }


    /**
     * Sets the seed of this generator (which is also the current state).
     */
    public void setSeed( final long seed ) {
        state = seed;
    }
    /**
     * Sets the seed (also the current state) of this generator.
     */
    public void setState( final long seed ) {
        state = seed;
    }
    /**
     * Gets the current state of this generator.
     */
    public long getState( ) {
        return state;
    }

    /**
     * Skip forward or backward a number of steps specified by advance, without generating a number at each step.
     * @param advance Number of future generations to skip past. Can be negative to backtrack.
     */
    public void skip(long advance)
    {
        state += 0x9E3779B97F4A7C15l * advance;
    }
}
