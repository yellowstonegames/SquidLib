package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A very-high-quality StatefulRandomness that is the fastest 64-bit generator in this library that passes statistical
 * tests and is equidistributed. Has 64 bits of state and natively outputs 64 bits at a time, changing the state with a
 * basic linear congruential generator (it is simply {@code state = state * 1103515245 + 1}). Starting with that LCG's
 * output, it xorshifts that long, multiplies by a very large negative long, then returns another xorshift. For
 * whatever reason, the output of this simple function passes all 32TB of PractRand with no anomalies, meaning its
 * statistical quality is excellent. As mentioned earlier, this is the fastest high-quality generator here other than
 * {@link ThrustAltRNG}. Unlike ThrustAltRNG, this can produce all long values as output; ThrustAltRNG bunches some
 * outputs and makes producing them more likely while others can't be produced at all. Notably, this generator is faster
 * than {@link LightRNG} while keeping the same or higher quality, and also faster than {@link XoRoRNG} while passing
 * tests that XoRoRNG always or frequently fails, such as binary matrix rank tests. It is not the fastest option on GWT;
 * if you intend to target GWT then {@link Lathe32RNG} or {@link GWTRNG} is recommended (they use the same algorithm,
 * xoroshiro64++ as it has recently been termed, and it has almost identical period to LinnormRNG but avoids math on
 * longs to increase performance on GWT).
 * <br>
 * This generator is a StatefulRandomness but not a SkippingRandomness, so it can't (efficiently) have the skip() method
 * that LightRNG has. A method could be written to run the generator's state backwards, though, as well as to get the
 * state from an output of {@link #nextLong()}. At least in theory, and supported in practice by how PCG-Random works,
 * this type of generator could be used to make a generator with multiple non-overlapping streams by changing the linear
 * congruential generator to add a different odd number instead of 1; any odd number should work, but optimizations can
 * apply to adding 1 that aren't possible for other numbers, so that's a good default to use here. LinnormRNG has a
 * static determine() method that uses a slightly different algorithm; it performs similar operations to
 * {@link LightRNG#determine(long)}. That involves 3 multiplications by constants, 3 "xorshifts" that each use a XOR and
 * a bitwise right shift on a local variable, and 3 temporary assignments; this determine() uses 3 multiplications by
 * constants, only 2 xorshifts, only 2 temporary assignments, and one XOR by a constant. Like LightRNG's determine(),
 * LinnormRNG.determine() can take any long as input and has all longs as possible outputs. Unlike LightRNG's
 * determine(), an input of 0 does not produce a result of 0.
 * <br>
 * The name comes from LINear congruential generator this uses to change it state, while the rest is a NORMal
 * SplitMix64-like generator. "Linnorm" is a Norwegian name for a kind of dragon, as well. 
 * <br>
 * Written May 9, 2018 by Tommy Ettinger.
 * @author Tommy Ettinger
 */
public final class LinnormRNG implements RandomnessSource, StatefulRandomness, Serializable {

    private static final long serialVersionUID = 153186732328748834L;

    private long state; /* The state can be seeded with any value. */

    /**
     * Constructor that seeds the generator with two calls to Math.random.
     */
    public LinnormRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    /**
     * Constructor that uses the given seed as the state without changes; all long seeds are acceptable.
     * @param seed any long, will be used exactly
     */
    public LinnormRNG(final long seed) {
        state = seed;
    }

    /**
     * Constructor that hashes seed with {@link CrossHash#hash64(CharSequence)} and uses the result as the state.
     * @param seed any CharSequence, such as a String or StringBuilder; should probably not be null (it might work?)
     */
    public LinnormRNG(final CharSequence seed) {
        state = CrossHash.hash64(seed);
    }

    @Override
    public final int next(int bits)
    {
        long z = (state = state * 0x41C64E6DL + 1L);
        z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
        return (int)(z ^ z >>> 25) >>> (32 - bits);
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public final long nextLong() {
        long z = (state = state * 0x41C64E6DL + 1L);
        z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
        return (z ^ z >>> 25);
    }

    /**
     * Produces a copy of this LinnormRNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this LinnormRNG
     */
    @Override
    public LinnormRNG copy() {
        return new LinnormRNG(state);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     *
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        long z = (state = state * 0x41C64E6DL + 1L);
        z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
        return (int)(z ^ z >>> 25);
    }

    /**
     * Exclusive on the outer bound.  The inner bound is 0.
     * The bound can be negative, which makes this produce either a negative int or 0.
     *
     * @param bound the upper bound; should be positive
     * @return a random int between 0 (inclusive) and bound (exclusive)
     */
    public final int nextInt(final int bound) {
        long z = (state = state * 0x41C64E6DL + 1L);
        z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
        return (int)((bound * ((z ^ z >>> 25) & 0xFFFFFFFFL)) >> 32);
    }

    /**
     * Inclusive inner, exclusive outer.
     *
     * @param inner the inner bound, inclusive, can be positive or negative
     * @param outer the outer bound, exclusive, can be positive or negative, usually greater than inner
     * @return a random int between inner (inclusive) and outer (exclusive)
     */
    public final int nextInt(final int inner, final int outer) {
        return inner + nextInt(outer - inner);
    }

    /**
     * Exclusive on bound (which may be positive or negative), with an inner bound of 0.
     * If bound is negative this returns a negative long; if bound is positive this returns a positive long. The bound
     * can even be 0, which will cause this to return 0L every time. This uses a biased technique to get numbers from
     * large ranges, but the amount of bias is incredibly small (expected to be under 1/1000 if enough random ranged
     * numbers are requested, which is about the same as an unbiased method that was also considered). It may have
     * noticeable bias if the LinnormRNG's period is exhausted by only calls to this method, which would take months on
     * 2018-era consumer hardware. Unlike all unbiased methods, this advances the state by an equivalent to exactly one
     * call to {@link #nextLong()}, where rejection sampling would sometimes advance by one call, but other times by
     * arbitrarily many more.
     * <br>
     * Credit for this method goes to <a href="https://oroboro.com/large-random-in-range/">Rafael Baptista's blog</a>,
     * with some adaptation for signed long values and a 64-bit generator. This method is drastically faster than the
     * previous implementation when the bound varies often (roughly 4x faster, possibly more). It also always gets at
     * most one random number, so it advances the state as much as {@link #nextInt(int)} or {@link #nextLong()}.
     * @param bound the outer exclusive bound; can be positive or negative
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    public long nextLong(long bound) {
        long rand = (state = state * 0x41C64E6DL + 1L);
        rand = (rand ^ rand >>> 27) * 0xAEF17502108EF2D9L;
        rand ^= rand >>> 25;
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>= 32;
        final long z = (randLow * boundLow >> 32);
        final long t = rand * boundLow + z;
        return rand * bound + (t >> 32) + ((t & 0xFFFFFFFFL) + randLow * bound >> 32) - (z >> 63);
    }
    /**
     * Inclusive inner, exclusive outer; lower and upper can be positive or negative and there's no requirement for one
     * to be greater than or less than the other.
     *
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, can be positive or negative
     * @return a random long that may be equal to lower and will otherwise be between lower and upper
     */
    public final long nextLong(final long lower, final long upper) {
        return lower + nextLong(upper - lower);
    }

    /**
     * Gets a uniform random double in the range [0.0,1.0)
     *
     * @return a random double at least equal to 0.0 and less than 1.0
     */
    public final double nextDouble() {
        long z = (state = state * 0x41C64E6DL + 1L);
        z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
        return ((z ^ z >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;

    }

    /**
     * Gets a uniform random double in the range [0.0,outer) given a positive parameter outer. If outer
     * is negative, it will be the (exclusive) lower bound and 0.0 will be the (inclusive) upper bound.
     *
     * @param outer the exclusive outer bound, can be negative
     * @return a random double between 0.0 (inclusive) and outer (exclusive)
     */
    public final double nextDouble(final double outer) {
        long z = (state = state * 0x41C64E6DL + 1L);
        z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
        return ((z ^ z >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53 * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     *
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public final float nextFloat() {
        long z = (state = state * 0x41C64E6DL + 1L);
        return ((z ^ z >>> 27) * 0xAEF17502108EF2D9L >>> 40) * 0x1p-24f;
    }

    /**
     * Gets a random value, true or false.
     * Calls nextLong() once.
     *
     * @return a random true or false value.
     */
    public final boolean nextBoolean() {
        long z = (state = state * 0x41C64E6DL + 1L);
        return ((z ^ z >>> 27) * 0xAEF17502108EF2D9L) < 0;
    }

    /**
     * Given a byte array as a parameter, this will fill the array with random bytes (modifying it
     * in-place). Calls nextLong() {@code Math.ceil(bytes.length / 8.0)} times.
     *
     * @param bytes a byte array that will have its contents overwritten with random bytes.
     */
    public final void nextBytes(final byte[] bytes) {
        int i = bytes.length, n;
        while (i != 0) {
            n = Math.min(i, 8);
            for (long bits = nextLong(); n-- != 0; bits >>>= 8) bytes[--i] = (byte) bits;
        }
    }

    /**
     * Sets the seed (also the current state) of this generator.
     *
     * @param seed the seed to use for this LinnormRNG, as if it was constructed with this seed.
     */
    @Override
    public final void setState(final long seed) {
        state = seed;
    }

    /**
     * Gets the current state of this generator.
     *
     * @return the current seed of this LinnormRNG, changed once per call to nextLong()
     */
    @Override
    public final long getState() {
        return state;
    }

    @Override
    public String toString() {
        return "LinnormRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return state == ((LinnormRNG) o).state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }

    /**
     * Static randomizing method that takes its state as a parameter; state is expected to change between calls to this.
     * It is recommended that you use {@code LinnormRNG.determine(++state)} or {@code LinnormRNG.determine(--state)} to
     * produce a sequence of different numbers, but you can also use {@code LinnormRNG.determine(state += 12345L)} or
     * any odd-number increment. All longs are accepted by this method, and all longs can be produced; unlike several
     * other classes' determine() methods, passing 0 here does not return 0.
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @return any long
     */
    public static long determine(long state)
    {
        return (state = ((state = ((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25;
    }

    /**
     * Static randomizing method that takes its state as a parameter and limits output to an int between 0 (inclusive)
     * and bound (exclusive); state is expected to change between calls to this. It is recommended that you use
     * {@code LinnormRNG.determineBounded(++state, bound)} or {@code LinnormRNG.determineBounded(--state, bound)} to
     * produce a sequence of different numbers, but you can also use
     * {@code LinnormRNG.determineBounded(state += 12345L, bound)} or any odd-number increment. All longs are accepted
     * by this method, but not all ints between 0 and bound are guaranteed to be produced with equal likelihood (for any
     * odd-number values for bound, this isn't possible for most generators). The bound can be negative.
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @param bound the outer exclusive bound, as an int
     * @return an int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(long state, final int bound)
    {
        return (int)((bound * (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0x7FFFFFFFL)) >> 31);
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g. {@code determine(++state)},
     * where the increment for state should be odd but otherwise doesn't really matter. This multiplies state by
     * {@code 0x632BE59BD9B4E019L} within this method, so using a small increment won't be much different from using a
     * very large one, as long as it is odd. The period is 2 to the 64 if you increment or decrement by 1, but there are
     * less than 2 to the 30 possible floats between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float determineFloat(long state) { return ((((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) >>> 40) * 0x1p-24f; }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determine(++state)}, where the increment for state should be odd but otherwise doesn't really matter. This
     * multiplies state by {@code 0x632BE59BD9B4E019L} within this method, so using a small increment won't be much
     * different from using a very large one, as long as it is odd. The period is 2 to the 64 if you increment or
     * decrement by 1, but there are less than 2 to the 62 possible doubles between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double determineDouble(long state) { return (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53; }

}
