package squidpony.squidmath;

import squidpony.StringKit;
import squidpony.annotation.Beta;

import java.io.Serializable;

/**
 * A different kind of RandomnessSource that operates internally on ints, which may have significant advantages on GWT
 * since that toolchain has to emulate longs (slowly) for almost any math with them. It is, however, expectd to be
 * slower on other platforms, at least platforms with 64-bit JVMs. Running on 32-bit desktop JVMs, 32-bit Dalvik on
 * Android, or other increasingly-rare environments may benefit from this class, though.
 * <br>
 * Quality is uncertain at this point, but visual tests at least appear indistinguishable from other PRNGs. Not all
 * seeds or combinations of seeds may be ideal, and the period of certain bits could easily be lower than the optimal
 * period of 2^64 32-bit numbers that this might have.
 * Created by Tommy Ettinger on 11/15/2016.
 */
@Beta
public class PintRNG implements RandomnessSource, StatefulRandomness, Serializable {

    /** 2 raised to the 53, - 1. */
    private static final long DOUBLE_MASK = ( 1L << 53 ) - 1;
    /** 2 raised to the -53. */
    private static final double NORM_53 = 1. / ( 1L << 53 );
    /** 2 raised to the 24, -1. */
    private static final long FLOAT_MASK = ( 1L << 24 ) - 1;
    /** 2 raised to the -24. */
    private static final double NORM_24 = 1. / ( 1L << 24 );

    private static final long serialVersionUID = -374415589203474497L;

    public int stateA, stateB; /* The state can be seeded with any value. */

    /** Creates a new generator seeded using Math.random. */
    public PintRNG() {
        this((int)((Math.random() - 0.5) * 4.294967296E9));
    }

    public PintRNG( final long seed ) {
        setState(seed);
    }

    public PintRNG(final int a, final int b)
    {
        stateA = a;
        stateB = b;
    }

    @Override
    public int next( int bits ) {
        return nextInt() >>> (32 - bits);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public int nextInt() {
        //return stateA += (stateB ^= (stateB + 0x62E2AC0D + 0x85157AF5 * stateA));
        return stateB += ((stateB ^ (stateA += 0x62E2AC0D)) >>> 8) * 0x9E3779B9;
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     * Internally, generates two random 32-bit values and combines them into one random long.
     * @return any long, all 64 bits are random
     */
    @Override
    public long nextLong() {
        return (0x100000000L * nextInt()) | nextInt();
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public RandomnessSource copy() {
        return new PintRNG((0x100000000L * stateA) | stateB);
    }

    /**
     * Exclusive on the upper bound.  The lower bound is 0. Uses a technique that RNG calls
     * {@link squidpony.squidmath.RNG#nextIntHasty(int)}, made known to me by Daniel Lemire.
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
            n = Math.min( i, 4 );
            for ( int bits = nextInt(); n-- != 0; bits >>>= 8 ) bytes[ --i ] = (byte)bits;
        }
    }



    /**
     * Sets the current state of this generator (two ints) by splitting the given long into upper and lower values.
     * @param seed the seed to use for this PinRNG, as if it was constructed with this seed.
     */
    @Override
    public void setState( final long seed ) {
        stateA = (int)(seed>>>32);
        stateB = (int)seed;
    }
    /**
     * Gets the current state of this generator.
     * @return the current seed of this PintRNG, changed once per call to nextInt()
     */
    @Override
    public long getState() {
        return (0x100000000L * stateA) | stateB;
    }

    @Override
    public String toString() {
        return "PintRNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PintRNG pintRNG = (PintRNG) o;

        return stateA == pintRNG.stateA && stateB == pintRNG.stateB;

    }

    @Override
    public int hashCode() {
        return 0x632BE5AB * stateA ^ stateB;
    }
}
