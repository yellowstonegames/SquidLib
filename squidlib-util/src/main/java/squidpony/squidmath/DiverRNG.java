package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A very-high-quality StatefulRandomness that is the fastest 64-bit generator in this library that passes statistical
 * tests and is one-dimensionally equidistributed across all 64-bit outputs. Has 64 bits of state and natively outputs
 * 64 bits at a time, changing the state with an "XLCG" or xor linear congruential generator (XLCGs are very similar to
 * normal LCGs but have slightly better random qualities on the high bits; the code for this XLCG is
 * {@code state = (state ^ 7822362180758744021) * -4126379630918251389}, and the only requirements for an XLCG are that
 * the constant used with XOR, when treated as unsigned and modulo 8, equals 5, while the multiplier, again treated as
 * unsigned and modulo 8, equals 3). Starting with that XLCG's output, it bitwise-left-rotates by 27, multiplies by a
 * very large negative long (see next), then returns a right-xorshift by 25. The large negative long is
 * -2643881736870682267, which when treated as unsigned is 2 to the 64 divided by an irrational number that generalizes
 * the golden ratio. This specific irrational number is the solution to {@code x}<sup>{@code 5}</sup>{@code = x + 1}.
 * Other multipliers also seem to work well as long as they have enough set bits (fairly-small multipliers fail tests).
 * For whatever reason, the output of this simple function passes all 32TB of PractRand with one anomaly ("unusual"
 * at 256GB), meaning its statistical quality is excellent. {@link ThrustAltRNG} is slightly faster, but isn't
 * equidistributed; unlike ThrustAltRNG, this can produce all long values as output. ThrustAltRNG bunches some outputs
 * and makes producing them more likely, while others can't be produced at all. Notably, this generator is faster than
 * {@link LinnormRNG}, which it is based on, while improving its quality, is faster than {@link LightRNG} while keeping
 * the same or higher quality, and is also faster than {@link XoRoRNG} while passing tests that XoRoRNG always or
 * frequently fails, such as binary matrix rank tests.
 * <br>
 * This generator is a StatefulRandomness but not a SkippingRandomness, so it can't (efficiently) have the skip() method
 * that LightRNG has. A method could be written to run the generator's state backwards, though, as well as to get the
 * state from an output of {@link #nextLong()}.
 * <br>
 * The static determine() methods in this class are a completely different algorithm from the {@link #nextLong()} and
 * similar instance methods here; they're a little faster than {@link LinnormRNG#determine(long)} and its family while
 * actually having much better stability in case an increment is a poor fit for the internals of the generator. Like
 * {@link #nextLong()}, {@link #determine(long)} can produce all possible long outputs and can take any long input; 
 * among determine() methods in this library that satisfy that constraint on input and output, this class' appears to be
 * the fastest.
 * <br>
 * The name comes in a roundabout way from Xmulzencab, Maya mythology's bee god who is also called the Diving God,
 * because the state transition is built around Xor and MUL. I was also listening to a Dio song, Holy Diver, at the
 * time, and Diver is much more reasonable to pronounce than Xmulzencab.
 * <br>
 * Written December 14, 2018 by Tommy Ettinger. Thanks to M.E. O'Neill for her insights into the family of generators
 * both this and her PCG-Random fall into, and to the team that worked on SplitMix64 for SplittableRandom in JDK 8.
 * Chris Doty-Humphrey's work on PractRand has been invaluable, and I wouldn't know about XLCGs without his findings.
 * Martin Roberts showed the technique for generalizing the golden ratio that produced the high-quality multiplier this
 * uses in one place. Other constants were found empirically or via searching for probable primes with desirable values
 * for use in an XLCG.
 * @author Tommy Ettinger
 */
public final class DiverRNG implements StatefulRandomness, Serializable {

    private static final long serialVersionUID = 153186732328748834L;

    private long state; /* The state can be seeded with any value. */

    /**
     * Creates a new generator seeded using Math.random.
     */
    public DiverRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public DiverRNG(final long seed) {
        state = seed;
    }

    public DiverRNG(final String seed) {
        state = CrossHash.hash64(seed);
    }

    @Override
    public final int next(int bits)
    {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        z = (z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L;
        return (int)(z ^ z >>> 25) >>> (32 - bits);
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public final long nextLong() {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        z = (z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L;
        return (z ^ z >>> 25);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public DiverRNG copy() {
        return new DiverRNG(state);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     *
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        z = (z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L;
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
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        z = (z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L;
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
     * can even be 0, which will cause this to return 0L every time.
     * <br>
     * Credit for this method goes to <a href="https://oroboro.com/large-random-in-range/">Rafael Baptista's blog</a>,
     * with some adaptation for signed long values and a 64-bit generator. This method is drastically faster than the
     * previous implementation when the bound varies often (roughly 4x faster, possibly more). It also always gets at
     * most one random number, so it advances the state as much as {@link #nextInt(int)}.
     * @param bound the outer exclusive bound; can be positive or negative
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    public long nextLong(long bound) {
        long rand = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        rand = (rand << 27 | rand >>> 37) * 0xDB4F0B9175AE2165L;
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
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        z = (z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L;
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
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        z = (z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L;
        return ((z ^ z >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53 * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     *
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public final float nextFloat() {
        final long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        return ((z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L >>> 40) * 0x1p-24f;
    }

    /**
     * Gets a random value, true or false.
     * Calls nextLong() once.
     *
     * @return a random true or false value.
     */
    public final boolean nextBoolean() {
        final long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        return ((z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L) < 0;
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
     * @param seed the seed to use for this LightRNG, as if it was constructed with this seed.
     */
    @Override
    public final void setState(final long seed) {
        state = seed;
    }

    /**
     * Gets the current state of this generator.
     *
     * @return the current seed of this LightRNG, changed once per call to nextLong()
     */
    @Override
    public final long getState() {
        return state;
    }

    @Override
    public String toString() {
        return "DiverRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return state == ((DiverRNG) o).state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }

    /**
     * Fast static randomizing method that takes its state as a parameter; state is expected to change between calls to
     * this. It is recommended that you use {@code DiverRNG.determine(++state)} or {@code DiverRNG.determine(--state)}
     * to produce a sequence of different numbers, and you may have slightly worse quality with increments or decrements
     * other than 1. All longs are accepted by this method, and all longs can be produced; unlike several other classes'
     * determine() methods, passing 0 here does not return 0.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is the same as
     * {@link LinnormRNG#determine(long)} and will behave well when the inputs are sequential, while {@code randomize()}
     * is a completely different algorithm based on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Each method will produce all long outputs if given
     * all possible longs as input.
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @return any long
     */
    public static long determine(long state)
    {
        return (state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25;
    }

    /**
     * High-quality static randomizing method that takes its state as a parameter; state is expected to change between
     * calls to this. It is suggested that you use {@code DiverRNG.randomize(++state)} or
     * {@code DiverRNG.randomize(--state)} to produce a sequence of different numbers, but any increments are allowed
     * (even-number increments won't be able to produce all outputs, but their quality will be fine for the numbers they
     * can produce). All longs are accepted by this method, and all longs can be produced; unlike several other classes'
     * determine() methods, passing 0 here does not return 0.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is the same as
     * {@link LinnormRNG#determine(long)} and will behave well when the inputs are sequential, while {@code randomize()}
     * is a completely different algorithm based on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Each method will produce all long outputs if given
     * all possible longs as input.
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @return any long
     */
    public static long randomize(long state)
    {
        return (state = ((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ state >>> 43 ^ state >>> 31 ^ state >>> 23) * 0xDB4F0B9175AE2165L) ^ state >>> 28;
    }

    /**
     * Fast static randomizing method that takes its state as a parameter and limits output to an int between 0
     * (inclusive) and bound (exclusive); state is expected to change between calls to this. It is recommended that you
     * use {@code DiverRNG.determineBounded(++state, bound)} or {@code DiverRNG.determineBounded(--state, bound)} to
     * produce a sequence of different numbers. All longs are accepted
     * by this method, but not all ints between 0 and bound are guaranteed to be produced with equal likelihood (for any
     * odd-number values for bound, this isn't possible for most generators). The bound can be negative.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is the same as
     * {@link LinnormRNG#determine(long)} and will behave well when the inputs are sequential, while {@code randomize()}
     * is a completely different algorithm based on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Each method will produce all long outputs if given
     * all possible longs as input.
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @param bound the outer exclusive bound, as an int
     * @return an int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(long state, final int bound)
    {
        return (int)((bound * (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0xFFFFFFFFL)) >> 32);
    }
    /**
     * High-quality static randomizing method that takes its state as a parameter and limits output to an int between 0
     * (inclusive) and bound (exclusive); state is expected to change between calls to this. It is suggested that you
     * use {@code DiverRNG.randomizeBounded(++state)} or {@code DiverRNG.randomize(--state)} to produce a sequence of
     * different numbers, but any increments are allowed (even-number increments won't be able to produce all outputs,
     * but their quality will be fine for the numbers they can produce). All longs are accepted by this method, but not
     * all ints between 0 and bound are guaranteed to be produced with equal likelihood (for any odd-number values for
     * bound, this isn't possible for most generators). The bound can be negative.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is the same as
     * {@link LinnormRNG#determine(long)} and will behave well when the inputs are sequential, while {@code randomize()}
     * is a completely different algorithm based on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Each method will produce all long outputs if given
     * all possible longs as input.
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @param bound the outer exclusive bound, as an int
     * @return an int between 0 (inclusive) and bound (exclusive)
     */

    public static int randomizeBounded(long state, final int bound)
    {
        return (int)((bound * (((state = ((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ state >>> 43 ^ state >>> 31 ^ state >>> 23) * 0xDB4F0B9175AE2165L) ^ state >>> 28) & 0xFFFFFFFFL)) >> 32);
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determineFloat(++state)}, where the increment for state should generally be 1. The period is 2 to the 64
     * if you increment or decrement by 1, but there are only 2 to the 30 possible floats between 0 and 1.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is the same as
     * {@link LinnormRNG#determine(long)} and will behave well when the inputs are sequential, while {@code randomize()}
     * is a completely different algorithm based on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Each method will produce all long outputs if given
     * all possible longs as input.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determineFloat(++state)} is recommended to go forwards or
     *              {@code determineFloat(--state)} to generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float determineFloat(long state) {
        return ((((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) >>> 40) * 0x1p-24f;
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomizeFloat(++state)}, where the increment for state can be any value and should usually be odd
     * (even-number increments reduce the period). The period is 2 to the 64 if you increment or decrement by any odd
     * number, but there are only 2 to the 30 possible floats between 0 and 1.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is the same as
     * {@link LinnormRNG#determine(long)} and will behave well when the inputs are sequential, while {@code randomize()}
     * is a completely different algorithm based on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Each method will produce all long outputs if given
     * all possible longs as input.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomizeFloat(++state)} is recommended to go forwards or
     *              {@code randomizeFloat(--state)} to generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float randomizeFloat(long state) {
        return (((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ state >>> 43 ^ state >>> 31 ^ state >>> 23) * 0xDB4F0B9175AE2165L >>> 40) * 0x1p-24f;

    }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determineDouble(++state)}, where the increment for state should generally be 1. The period is 2 to the 64
     * if you increment or decrement by 1, but there are only 2 to the 62 possible doubles between 0 and 1.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is the same as
     * {@link LinnormRNG#determine(long)} and will behave well when the inputs are sequential, while {@code randomize()}
     * is a completely different algorithm based on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Each method will produce all long outputs if given
     * all possible longs as input.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determineDouble(++state)} is recommended to go forwards or
     *              {@code determineDouble(--state)} to generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double determineDouble(long state) {
        return (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomizeDouble(++state)}, where the increment for state can be any number but should usually be odd
     * (even-number increments reduce the period). The period is 2 to the 64 if you increment or decrement by 1, but 
     * there are only 2 to the 62 possible doubles between 0 and 1.
     * <br>
     * You have a choice between determine() and randomize() in this class. {@code determine()} is the same as
     * {@link LinnormRNG#determine(long)} and will behave well when the inputs are sequential, while {@code randomize()}
     * is a completely different algorithm based on Pelle Evensen's rrxmrrxmsx_0 and evaluated with
     * <a href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">the same
     * testing requirements Evensen used for rrxmrrxmsx_0</a>; it will have excellent quality regardless of patterns in
     * input but will be about 30% slower than {@code determine()}. Each method will produce all long outputs if given
     * all possible longs as input.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomizeDouble(++state)} is recommended to go forwards or
     *              {@code randomizeDouble(--state)} to generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double randomizeDouble(long state) {
        return (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

}
