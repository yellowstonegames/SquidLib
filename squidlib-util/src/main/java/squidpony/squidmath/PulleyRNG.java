package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A very-high-quality StatefulRandomness that is meant to be reasonably fast, but also to be robust against frequent
 * state changes, and is built around a strong determine() method. It passes 32TB of PractRand with no anomalies, and
 * calling {@link #determine(long)} on two different sequences of inputs (one that added 3 each time, and one that added
 * 5 each time) showed no failures on 32TB of data produced by XORing calls to determine() on both sequences. PulleyRNG
 * is one-dimensionally equidistributed across all 64-bit outputs, has 64 bits of state, natively outputs 64 bits at a
 * time, can have its state set and skipped over as a {@link StatefulRandomness} and {@link SkippingRandomness}. It has
 * a 1-to-1 correspondence between inputs and outputs for {@link #determine(long)}, and you can get the input to
 * determine() that produced a particular long output by passing that output to {@link #inverseNextLong(long)}. It is
 * largely the work of Pelle Evensen, who discovered that where a unary hash (called a determine() method here) can
 * start with the XOR of the input and two rotations of that input, and that sometimes acts as a better randomization
 * procedure than multiplying by a large constant (which is what {@link LightRNG#determine(long)},
 * {@link LinnormRNG#determine(long)}, and even {@link ThrustAltRNG#determine(long)} do). Evensen also crunched the
 * numbers to figure out that {@code n ^ n >>> A ^ n >>> B} is a bijection for all distinct non-zero values for A and B,
 * though this wasn't used in his unary hash rrxmrrxmsx_0.
 * <br>
 * The algorithm for {@link #determine(long)} looks like this ({@link #nextLong()} just calls determine() on a counter):
 * <ol>
 *     <li>XOR the input with two different bitwise rotations: {@code n ^ (n << 41 | n >>> 23) ^ (n << 17 | n >>> 47)}</li>
 *     <li>Multiply by a large constant, {@code 0x369DEA0F31A53F85L}, and store it in n</li>
 *     <li>XOR n with two different unsigned bitwise right shifts: {@code n ^ n >>> 25 ^ n >>> 37}</li>
 *     <li>Multiply by a large constant, {@code 0xDB4F0B9175AE2165L}, and store it in n</li>
 *     <li>XOR n with n right-shifted by 28, and return</li>
 * </ol>
 * This is the result of some simplifications on PelicanRNG (present here as {@link DiverRNG#randomize(long)}), which is
 * ridiculously strong (passing the full battery of tests that rrxmrrxmsx_0 only narrowly failed) but not especially
 * fast. PulleyRNG is an effort to speed up PelicanRNG just a little, but without doing the extensive testing that
 * ensure virtually any bit pattern given to PelicanRNG will produce pseudo-random outputs. PulleyRNG does well in tough
 * tests. Other than the input stream correlation test mentioned earlier, this also passes tests if the inputs are
 * incremented by what is normally one of the worst-case scenarios for other generators -- using an increment that is
 * the multiplicative inverse (mod 2 to the 64 in this case) of one of the fixed constants in the generator. The first
 * multiplication performed here is by {@code 0x369DEA0F31A53F85L}, and
 * {@code 0xBE21F44C6018E14DL * 0x369DEA0F31A53F85L == 1L}, so testing determine() with inputs that change by
 * 0xBE21F44C6018E14DL should stress the generator, but instead it does fine through 32TB, with only one "unusual"
 * anomaly rather early on.
 * <br>
 * @author Pelle Evensen
 * @author Tommy Ettinger
 */
public final class PulleyRNG implements StatefulRandomness, SkippingRandomness, Serializable {

    private static final long serialVersionUID = 1L;

    private long state; /* The state can be seeded with any value. */

    /**
     * Creates a new generator seeded using Math.random.
     */
    public PulleyRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public PulleyRNG(final long seed) {
        state = seed;
    }

    public PulleyRNG(final String seed) {
        state = CrossHash.hash64(seed);
    }

    @Override
    public final int next(int bits)
    {
        long z = state++;
        z = (z ^ (z << 41 | z >>> 23) ^ (z << 17 | z >>> 47)) * 0x369DEA0F31A53F85L;
        z = (z ^ z >>> 25 ^ z >>> 37) * 0xDB4F0B9175AE2165L;
        return (int)((z ^ z >>> 28) >>> (64 - bits));
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public final long nextLong() {
        long z = state++;
        z = (z ^ (z << 41 | z >>> 23) ^ (z << 17 | z >>> 47)) * 0x369DEA0F31A53F85L;
        z = (z ^ z >>> 25 ^ z >>> 37) * 0xDB4F0B9175AE2165L;
        return (z ^ z >>> 28);
    }


    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public PulleyRNG copy() {
        return new PulleyRNG(state);
    }

    @Override
    public final long skip(final long advance) {
        long z = state += advance;
        z = (z ^ (z << 41 | z >>> 23) ^ (z << 17 | z >>> 47)) * 0x369DEA0F31A53F85L;
        z = (z ^ z >>> 25 ^ z >>> 37) * 0xDB4F0B9175AE2165L;
        return (z ^ z >>> 28);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     *
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        long z = state++;
        z = (z ^ (z << 41 | z >>> 23) ^ (z << 17 | z >>> 47)) * 0x369DEA0F31A53F85L;
        z = (z ^ z >>> 25 ^ z >>> 37) * 0xDB4F0B9175AE2165L;
        return (int)(z ^ z >>> 28);
    }

    /**
     * Exclusive on the outer bound.  The inner bound is 0.
     * The bound can be negative, which makes this produce either a negative int or 0.
     *
     * @param bound the upper bound; should be positive
     * @return a random int between 0 (inclusive) and bound (exclusive)
     */
    public final int nextInt(final int bound) {
        long z = state++;
        z = (z ^ (z << 41 | z >>> 23) ^ (z << 17 | z >>> 47)) * 0x369DEA0F31A53F85L;
        z = (z ^ z >>> 25 ^ z >>> 37) * 0xDB4F0B9175AE2165L;
        return (int)((bound * ((z ^ z >>> 28) & 0xFFFFFFFFL)) >> 32);
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
     * can even be 0, which will cause this to return 0L every time.
     * <br>
     * Credit for this method goes to <a href="https://oroboro.com/large-random-in-range/">Rafael Baptista's blog</a>
     * for the original idea, and the JDK10 Math class' usage of Hacker's Delight code for the current algorithm. 
     * This method is drastically faster than the previous implementation when the bound varies often (roughly 4x
     * faster, possibly more). It also always gets exactly one random long, so by default it advances the state as much
     * as {@link #nextLong()}.
     *
     * @param bound the outer exclusive bound; can be positive or negative
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    public long nextLong(long bound) {
        long rand = state++;
        rand = (rand ^ (rand << 41 | rand >>> 23) ^ (rand << 17 | rand >>> 47)) * 0x369DEA0F31A53F85L;
        rand = (rand ^ rand >>> 25 ^ rand >>> 37) * 0xDB4F0B9175AE2165L;
        rand ^= rand >>> 28;
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>= 32;
        final long t = rand * boundLow + (randLow * boundLow >>> 32);
        return rand * bound + (t >> 32) + (randLow * bound + (t & 0xFFFFFFFFL) >> 32);
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
        long z = state++;
        z = (z ^ (z << 41 | z >>> 23) ^ (z << 17 | z >>> 47)) * 0x369DEA0F31A53F85L;
        z = (z ^ z >>> 25 ^ z >>> 37) * 0xDB4F0B9175AE2165L;
        return ((z ^ z >>> 28) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;

    }

    /**
     * Gets a uniform random double in the range [0.0,outer) given a positive parameter outer. If outer
     * is negative, it will be the (exclusive) lower bound and 0.0 will be the (inclusive) upper bound.
     *
     * @param outer the exclusive outer bound, can be negative
     * @return a random double between 0.0 (inclusive) and outer (exclusive)
     */
    public final double nextDouble(final double outer) {
        long z = state++;
        z = (z ^ (z << 41 | z >>> 23) ^ (z << 17 | z >>> 47)) * 0x369DEA0F31A53F85L;
        z = (z ^ z >>> 25 ^ z >>> 37) * 0xDB4F0B9175AE2165L;
        return ((z ^ z >>> 28) & 0x1FFFFFFFFFFFFFL) * 0x1p-53 * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     *
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public final float nextFloat() {
        long z = state++;
        z = (z ^ (z << 41 | z >>> 23) ^ (z << 17 | z >>> 47)) * 0x369DEA0F31A53F85L;
        return ((z ^ z >>> 25 ^ z >>> 37) * 0xDB4F0B9175AE2165L >>> 40) * 0x1p-24f;
    }

    /**
     * Gets a random value, true or false.
     * Calls nextLong() once.
     *
     * @return a random true or false value.
     */
    public final boolean nextBoolean() {
        long z = state++;
        z = (z ^ (z << 41 | z >>> 23) ^ (z << 17 | z >>> 47)) * 0x369DEA0F31A53F85L;
        return ((z ^ z >>> 25 ^ z >>> 37) * 0xDB4F0B9175AE2165L) < 0;
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
     * @param seed the seed to use for this PulleyRNG, as if it was constructed with this seed.
     */
    @Override
    public final void setState(final long seed) {
        state = seed;
    }

    /**
     * Gets the current state of this generator.
     *
     * @return the current seed of this PulleyRNG, changed once per call to nextLong()
     */
    @Override
    public final long getState() {
        return state;
    }

    /**
     * Given the output of a call to {@link #nextLong()} as {@code out}, this finds the state of the PulleyRNG that
     * produce that output. If you set the state of a PulleyRNG with {@link #setState(long)} to the result of this
     * method and then call {@link #nextLong()} on it, you should get back {@code out}. This can also reverse
     * {@link #determine(long)}; it uses the same algorithm as nextLong().
     * <br>
     * This isn't as fast as {@link #nextLong()}, but both run in constant time.
     * <br>
     * This will not necessarily work if out was produced by a generator other than a PulleyRNG, or if it was produced
     * with the bounded {@link #nextLong(long)} method by any generator.
     * @param out a long as produced by {@link #nextLong()}, without changes
     * @return the state of the RNG that will produce the given long
     */
    public static long inverseNextLong(long out)
    {
        out ^= out >>> 28 ^ out >>> 56; // inverts xorshift by 28
        out *= 0xF179F93568D4286DL; // inverse of 0xDB4F0B9175AE2165L
        out ^= out >>> 25 ^ out >>> 50 ^ out >>> 37; // inverts paired xorshift by 25, 37
        out *= 0xBE21F44C6018E14DL; // inverse of 0x369DEA0F31A53F85L
        // follow the steps from http://marc-b-reynolds.github.io/math/2017/10/13/XorRotate.html
        // this is the inverse of (0, 17, 41), working on 64-bit numbers.
        out ^= (out << 17 | out >>> 47) ^ (out << 41 | out >>> 23);
        out ^= (out << 34 | out >>> 30) ^ (out << 18 | out >>> 46);
        out ^= (out << 4  | out >>> 60) ^ (out << 36 | out >>> 28);
        return out;
    }


    @Override
    public String toString() {
        return "PulleyRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return state == ((PulleyRNG) o).state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ state >>> 32);
    }

    /**
     * Static randomizing method that takes its state as a parameter; state is expected to change between calls to this.
     * It is recommended that you use {@code PulleyRNG.determine(++state)} or {@code PulleyRNG.determine(--state)} to
     * produce a sequence of different numbers, but you can also use {@code PulleyRNG.determine(state += 12345L)} or
     * any odd-number increment. All longs are accepted by this method, and all longs can be produced; passing 0 here
     * will return 0.
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @return any long
     */
    public static long determine(long state)
    {
        return (state = ((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47)) * 0x369DEA0F31A53F85L) ^ state >>> 37 ^ state >>> 25) * 0xDB4F0B9175AE2165L) ^ state >>> 28;
    }
    
    /**
     * Static randomizing method that takes its state as a parameter and limits output to an int between 0 (inclusive)
     * and bound (exclusive); state is expected to change between calls to this. It is recommended that you use
     * {@code PulleyRNG.determineBounded(++state, bound)} or {@code PulleyRNG.determineBounded(--state, bound)} to
     * produce a sequence of different numbers, but you can also use
     * {@code PulleyRNG.determineBounded(state += 12345L, bound)} or any odd-number increment. All longs are accepted
     * by this method, but not all ints between 0 and bound are guaranteed to be produced with equal likelihood (for any
     * odd-number values for bound, this isn't possible for most generators). The bound can be negative.
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @param bound the outer exclusive bound, as an int
     * @return an int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(long state, final int bound)
    {
        return (int)((bound * (((state = ((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47)) * 0x369DEA0F31A53F85L) ^ state >>> 37 ^ state >>> 25) * 0xDB4F0B9175AE2165L) ^ state >>> 28) & 0xFFFFFFFFL)) >> 32);
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g. {@code determine(++state)},
     * where the increment for state should be odd but otherwise doesn't really matter. This should tolerate just about
     * any increment as long as it is odd. The period is 2 to the 64 if you increment or decrement by 1, but there are
     * only 2 to the 30 possible floats between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float determineFloat(long state) {
        return ((state = ((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47)) * 0x369DEA0F31A53F85L) ^ state >>> 37 ^ state >>> 25) * 0xDB4F0B9175AE2165L) >>> 40) * 0x1p-24f;
    }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determine(++state)}, where the increment for state should be odd but otherwise doesn't really matter. This
     * should tolerate just about any increment, as long as it is odd. The period is 2 to the 64 if you increment or
     * decrement by 1, but there are only 2 to the 62 possible doubles between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double determineDouble(long state) {
        return (((state = ((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47)) * 0x369DEA0F31A53F85L) ^ state >>> 37 ^ state >>> 25) * 0xDB4F0B9175AE2165L) ^ state >>> 28) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }
}
