/*
Written in 2015 by Sebastiano Vigna (vigna@acm.org)

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * This is a SplittableRandom-style generator, meant to have a tiny state
 * that permits storing many different generators with low overhead.
 * It should be rather fast, though no guarantees can be made on all hardware.
 * <br>
 * Benchmarking on a Windows laptop with an i7-4700MQ processor running OpenJDK 8
 * reports generation of 64-bit random long output as 17.8x faster than generating
 * an equivalent number of random longs with java.util.Random, and generation of
 * 32-bit random int output as 9.8x faster. Specifically, generating 1 billion longs
 * took about 1.28 nanoseconds per long (1.277 seconds for the whole group) with
 * LightRNG, while java.util.Random (which is meant to produce int, to be fair) took
 * about 22.8 nanoseconds per long (22.797 seconds for the whole group). XorRNG
 * appears to be occasionally faster on int output than LightRNG, but it isn't clear
 * why or what causes that (JIT or GC internals, possibly). XorRNG is slightly
 * slower at generating 64-bit random data, including long and double, but not by
 * a significant degree (a multiplier between 0.9 and 1.2 times). The only deciding
 * factor then is state size, where LightRNG is as small as possible for any JVM
 * object with even a single field: 16 bytes (on a 64-bit JVM; 8-byte objects with
 * 4 bytes or less of non-static members may be possible on 32-bit JVMs but I can't
 * find anything confirming that guess).
 * <br>
 * So yes, this should be very fast, and with only a single long used per LightRNG,
 * it is about as memory-efficient as these generators get.
 * <br>
 * Written in 2015 by Sebastiano Vigna (vigna@acm.org)
 * @author Sebastiano Vigna
 * @author Tommy Ettinger
 */
public final class LightRNG implements RandomnessSource, StatefulRandomness, SkippingRandomness, Serializable
{
	/** 2 raised to the 53, - 1. */
    private static final long DOUBLE_MASK = ( 1L << 53 ) - 1;
    /** 2 raised to the -53. */
    private static final double NORM_53 = 1. / ( 1L << 53 );
    /** 2 raised to the 24, -1. */
    private static final long FLOAT_MASK = ( 1L << 24 ) - 1;
    /** 2 raised to the -24. */
    private static final float NORM_24 = 1f / (1 << 24);

	private static final long serialVersionUID = -374415589203474497L;

    public long state; /* The state can be seeded with any value. */

    /** Creates a new generator seeded using Math.random. */
    public LightRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public LightRNG( final long seed ) {
        setSeed(seed);
    }

    /**
     * Gets a pseudo-random int with at most the specified count of bits; for example, if bits is 3 this can return any
     * int between 0 and 2 to the 3 (that is, 8), exclusive on the upper end. That would mean 7 could be returned, but
     * no higher ints, and 0 could be returned, but no lower ints.
     * 
     * The algorithm used here changed on March 8, 2018 when LightRNG was remade the default generator in SquidLib.
     * The older method is available as {@link #compatibleNext(int)}, but its use is discouraged; it's slightly slower 
     * for no good reason.
     * @param bits the number of bits to be returned; if 0 or less, or if 32 or greater, can return any 32-bit int
     * @return a pseudo-random int that uses at most the specified amount of bits
     */
    @Override
    public final int next( int bits ) {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (int)(z ^ (z >>> 31)) >>> (32 - bits);
    }
    /**
     * Gets a pseudo-random int with at most the specified count of bits; for example, if bits is 3 this can return any
     * int between 0 and 2 to the 3 (that is, 8), exclusive on the upper end. That would mean 7 could be returned, but
     * no higher ints, and 0 could be returned, but no lower ints.
     *
     * The algorithm used here is the older version of {@link #next(int)} before some things changed on March 8 2018.
     * Using this method is discouraged unless you need to reproduce values exactly; it's slightly slower for no good
     * reason. Calling {@code next(32)} and {@code compatibleNext(32)} should have identical results, but other values
     * for bits will probably be different.
     * @param bits the number of bits to be returned; if 0 or less, or if 32 or greater, can return any 32-bit int
     * @return a pseudo-random int that uses at most the specified amount of bits
     */
    public final int compatibleNext( int bits ) {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (int)( (z ^ (z >>> 31)) & ( 1L << bits ) - 1 );
    }
    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     * @return any long, all 64 bits are random
     */
    @Override
    public final long nextLong() {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public LightRNG copy() {
        return new LightRNG(state);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public int nextInt() {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (int) (z ^ (z >>> 31)); 
    }

    /**
     * Returns a random non-negative integer between 0 (inclusive) and the given bound (exclusive),
     * or 0 if the bound is 0. The bound can be negative, which will produce 0 or a negative result.
     * Uses an aggressively optimized technique that has some bias, but mostly for values of
     * bound over 1 billion. This method uses the same technique as {@link RNG#nextIntHasty(int)},
     * and like that method will always advance state exactly once (equivalent to one call to
     * {@link #nextLong()}).
     * <br>
     * Credit goes to Daniel Lemire, http://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/
     *
     * @param bound the outer bound (exclusive), can be negative or positive
     * @return the found number
     */
    public int nextInt( final int bound ) {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (int) ((bound * ((z ^ (z >>> 31)) & 0x7FFFFFFFL)) >> 31);
    }

    /**
     * Like {@link #compatibleNext(int)}, but for compatibility with {@link #nextInt(int)}.
     * Exclusive on the upper bound.  The lower bound is 0.
     * @param bound the upper bound; should be positive
     * @return a random int less than n and at least equal to 0
     */
    public int compatibleNextInt( final int bound ) {
        if (bound <= 0) return 0;
        int threshold = (0x7fffffff - bound + 1) % bound;
        for (; ; ) {
            int bits = (int) (nextLong() & 0x7fffffff);
            if (bits >= threshold)
                return bits % bound;
        }
    }
    /**
     * Inclusive lower, exclusive upper. Although you should usually use a higher value for upper than for lower, you
     * can use a lower "upper" than "lower" and this will still work, producing an int between the two bounds.
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, can be positive or negative, usually should be greater than lower
     * @return a random int between lower (inclusive) and upper (exclusive)
     */
    public int nextInt( final int lower, final int upper ) {
        return lower + nextInt(upper - lower);
    }

    /**
     * Inclusive lower, exclusive upper.
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, should be positive, must be greater than lower
     * @return a random int between lower (inclusive) and upper (exclusive)
     */
    public int compatibleNextInt( final int lower, final int upper ) {
        if ( upper - lower <= 0 ) throw new IllegalArgumentException("Upper bound must be greater than lower bound");
        return lower + compatibleNextInt(upper - lower);
    }

    /**
     * Exclusive on the outer bound; the inner bound is 0. The bound may be negative, which will produce a non-positive
     * result.
     * @param bound the outer exclusive bound; may be positive or negative
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    public long nextLong(long bound) {
        long rand = nextLong();
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>= 32;
        final long z = (randLow * boundLow >> 32);
        long t = rand * boundLow + z;
        final long tLow = t & 0xFFFFFFFFL;
        t >>>= 32;
        return rand * bound + t + (tLow + randLow * bound >> 32) - (z >> 63) - (bound >> 63);
    }
    
    /**
     * Inclusive inner, exclusive outer; both inner and outer can be positive or negative.
     * @param inner the inner bound, inclusive, can be positive or negative
     * @param outer the outer bound, exclusive, can be positive or negative and may be greater than or less than inner
     * @return a random long that may be equal to inner and will otherwise be between inner and outer
     */
    public long nextLong(final long inner, final long outer) {
        return inner + nextLong(outer - inner);
    }


    /**
     * Exclusive on the upper bound. The lower bound is 0. Unlike {@link #nextInt(int)} or {@link #nextLong(long)}, this
     * may sometimes advance the state more than once, depending on what numbers are produced internally and the bound.
     * {@link #nextLong(long)} is preferred because it is much faster and reliably advances the state only once. Because
     * this method uses rejection sampling, getting multiple random longs to "smooth the odds" when the bound is such
     * that it can't fairly distribute one random long across all possible outcomes, it may be more "fair" than
     * {@link #nextLong(long)}, though it could potentially consume more of the period faster if pathologically bad
     * bounds were used very often, and if enough of the period is gone then statistical flaws may become detectable.
     * @param bound the upper bound; if this isn't positive, this method always returns 0
     * @return a random long less than n and at least equal to 0
     */
    public long compatibleNextLong(final long bound) {
        if ( bound <= 0 ) return 0;
        long threshold = (0x7fffffffffffffffL - bound + 1) % bound;
        for (;;) {
            long bits = nextLong() & 0x7fffffffffffffffL;
            if (bits >= threshold)
                return bits % bound;
        }
    }

    /**
     * Inclusive lower, exclusive upper.
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, should be positive, must be greater than lower
     * @return a random long at least equal to lower and less than upper
     */
    public long compatibleNextLong( final long lower, final long upper ) {
        if ( upper - lower <= 0 )  throw new IllegalArgumentException("Upper bound must be greater than lower bound");
        return lower + compatibleNextLong(upper - lower);
    }
    /**
     * Gets a uniform random double in the range [0.0,1.0)
     * @return a random double at least equal to 0.0 and less than 1.0
     */
    public double nextDouble() {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return ((z ^ (z >>> 31)) & 0x1fffffffffffffL) * 0x1p-53;
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
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return ((z ^ (z >>> 31)) & 0xffffffL) * 0x1p-24f;

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
        int i = bytes.length, n;
        while( i != 0 ) {
            n = Math.min( i, 8 );
            for ( long bits = nextLong(); n-- != 0; bits >>>= 8 ) bytes[ --i ] = (byte)bits;
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
    public long getState() {
        return state;
    }

    /**
     * Advances or rolls back the LightRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextLong(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most recent generated number
     * @return the random long generated after skipping advance numbers
     */
    @Override
    public long skip(long advance) {
        long z = (state += 0x9E3779B97F4A7C15L * advance);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }


    @Override
    public String toString() {
        return "LightRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LightRNG lightRNG = (LightRNG) o;

        return state == lightRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }

    public static long determine(long state)
    {
        return ((state = ((state = ((state *= 0x9E3779B97F4A7C15L) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31);
    }
    public static long determine(final int a, final int b)
    {
        long state = 0x9E3779B97F4A7C15L + (a & 0xFFFFFFFFL) + ((long)b << 32);
        state = ((state >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        return state ^ (state >>> 31);
    }

    public static int determineBounded(long state, final int bound)
    {
        return (int)((bound * (((state = ((state = ((state *= 0x9E3779B97F4A7C15L) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31) & 0x7FFFFFFFL)) >> 31);
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g. {@code determine(++state)},
     * where the increment for state should be odd but otherwise doesn't really matter. This multiplies state by
     * {@code 0x9E3779B97F4A7C15L} within this method, so using a small increment won't be much different from using a
     * very large one, as long as it is odd. The period is 2 to the 64 if you increment or decrement by 1, but there are
     * only 2 to the 30 possible floats between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float determineFloat(long state) { return (((state = ((state = ((state *= 0x9E3779B97F4A7C15L) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31) & 0xFFFFFF) * 0x1p-24f; }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determine(++state)}, where the increment for state should be odd but otherwise doesn't really matter. This
     * multiplies state by {@code 0x9E3779B97F4A7C15L} within this method, so using a small increment won't be much
     * different from using a very large one, as long as it is odd. The period is 2 to the 64 if you increment or
     * decrement by 1, but there are only 2 to the 62 possible doubles between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double determineDouble(long state) { return (((state = ((state = ((state *= 0x9E3779B97F4A7C15L) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31) & 0x1FFFFFFFFFFFFFL) * 0x1p-53; }
}
