package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;


/**
 * A random number generator that is extremely fast but can't return all possible results. ThrustAltRNG passes TestU01's
 * BigCrush, which is a difficult statistical quality test. On <a href="http://gjrand.sourceforge.net/">gjrand</a>'s
 * "testunif" checks, this does very well on 100GB of tested data, with the "Overall summary one sided P-value P =
 * 0.981", where 1 is perfect and 0.1 or less is a failure. On <a href="http://pracrand.sourceforge.net/">PractRand</a>,
 * this passes all 32TB of generated numbers without finding any failures (and very rarely finding anomalies). Like
 * {@link LightRNG}, this changes its state with a steady fixed increment, and does hash-like adjustments to the
 * current state to randomize it (the change is not a cipher because it is not reversible; this may be an advantage
 * for some usage). The period on ThrustAltRNG is 2 to the 64. ThrustAltRNG is very strong on speed, outpacing the
 * default generator for {@link RNG}, {@link DiverRNG}, by a small margin, and most other RandomnessSources in
 * SquidLib by a larger margin (it is slower than {@link MiniMover64RNG}, but this has a better period). Similarly to
 * other hash-like PRNGs, ThrustAltRNG has a {@link #determine(long)} method that takes a state as a long and returns
 * a deterministic random number (each input has one output, though in this case the reverse isn't true and some outputs
 * will be returned by multiple inputs). Like LightRNG, but unlike an LCG such as {@link java.util.Random}, changing the
 * seed even slightly generally produces completely different results, which applies primarily to determine() but also
 * the first number generated in a series of nextLong() calls. This generator is GWT-safe but will be much slower on GWT
 * than generators designed for usage there, such as {@link GWTRNG} or {@link Lathe32RNG}.
 * <br>
 * Because this generator can't produce all longs (it is not equidistributed), that alone is enough to discount its use
 * in some (mainly scientific) scenarios, although it passes all major testing suites (TestU01's BigCrush, PractRand
 * over the full 32TB of tests, and gjrand to some degree, at least better than most). DiverRNG is the default
 * generator after ThrustAltRNG was used extensively for some time, since DiverRNG passes the same tests, is almost as
 * fast, and is known to produce all longs over the course of its period.
 * <br>
 * There was a ThrustRNG in SquidLib, but it failed statistical tests badly in roughly a minute of testing, so even
 * though it was faster it probably wasn't a good idea to use it. ThrustAltRNG modifies ThrustRNG's algorithm very
 * heavily, and isn't especially similar, but the name stuck, I guess. The idea behind the name is that the generator is
 * acting like a thrust in fencing, pushing quickly but leaving a hole (not in the quality, but in the distribution).
 * <br>
 * Created by Tommy Ettinger on 10/18/2017.
 */
public final class ThrustAltRNG implements StatefulRandomness, SkippingRandomness, Serializable {
    private static final long serialVersionUID = 3L;
    /**
     * Can be any long value.
     */
    public long state;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public ThrustAltRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public ThrustAltRNG(final long seed) {
        state = seed;
    }

    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    @Override
    public long getState() {
        return state;
    }

    /**
     * Set the current internal state of this StatefulRandomness with a long.
     *
     * @param state a 64-bit long
     */
    @Override
    public void setState(long state) {
        this.state = state;
    }

    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public final int next(final int bits) {
        final long s = (state += 0x6C8E9CF570932BD5L);
        final long z = (s ^ (s >>> 25)) * (s | 0xA529L);
        return (int)(z ^ (z >>> 22)) >>> (32 - bits);
    }
    /**
     * Using this method, any algorithm that needs to efficiently generate more
     * than 32 bits of random data can interface with this randomness source.
     * <p>
     * Get a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive).
     *
     * @return a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive)
     */
    @Override
    public final long nextLong() {
        final long s = (state += 0x6C8E9CF570932BD5L);
        final long z = (s ^ (s >>> 25)) * (s | 0xA529L);
        return z ^ (z >>> 22);
    }

    /**
     * Advances or rolls back the ThrustAltRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextLong(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    @Override
    public final long skip(long advance) {
        final long s = (state += 0x6C8E9CF570932BD5L * advance);
        final long z = (s ^ (s >>> 25)) * (s | 0xA529L);
        return z ^ (z >>> 22);
    }


    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public ThrustAltRNG copy() {
        return new ThrustAltRNG(state);
    }
    @Override
    public String toString() {
        return "ThrustAltRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThrustAltRNG thrustAltRNG = (ThrustAltRNG) o;

        return state == thrustAltRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }

    /**
     * Returns a random permutation of state; if state is the same on two calls to this, this will return the same
     * number. This is expected to be called with some changing variable, e.g. {@code determine(++state)}, where
     * the increment for state should be odd but otherwise doesn't really matter. This multiplies state by
     * {@code 0x6C8E9CF570932BD5L} within this method, so using a small increment won't be much different from using a
     * very large one, as long as it is odd. The period is 2 to the 64 if you increment or decrement by 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random permutation of state
     */
    public static long determine(long state) {
        return (state = ((state *= 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22);
    }
    //for quick one-line pastes of how the algo can be used with "randomize(++state)"
    //public static long randomize(long state) { return (state = ((state *= 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22); }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g. {@code determine(++state)},
     * where the increment for state should be odd but otherwise doesn't really matter. This multiplies state by
     * {@code 0x6C8E9CF570932BD5L} within this method, so using a small increment won't be much different from using a
     * very large one, as long as it is odd. The period is 2 to the 64 if you increment or decrement by 1, but there are
     * only 2 to the 30 possible floats between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float determineFloat(long state) { return (((state = ((state *= 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22)) & 0xFFFFFF) * 0x1p-24f; }


    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determine(++state)}, where the increment for state should be odd but otherwise doesn't really matter. This
     * multiplies state by {@code 0x6C8E9CF570932BD5L} within this method, so using a small increment won't be much
     * different from using a very large one, as long as it is odd. The period is 2 to the 64 if you increment or
     * decrement by 1, but there are only 2 to the 62 possible doubles between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double determineDouble(long state) { return (((state = ((state *= 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22)) & 0x1FFFFFFFFFFFFFL) * 0x1p-53; }

    /**
     * Given a state that should usually change each time this is called, and a bound that limits the result to some
     * (typically fairly small) int, produces a pseudo-random int between 0 and bound (exclusive). The bound can be
     * negative, which will cause this to produce 0 or a negative int; otherwise this produces 0 or a positive int.
     * The state should change each time this is called, generally by incrementing by an odd number (not an even number,
     * especially not 0). It's fine to use {@code determineBounded(++state, bound)} to get a different int each time.
     * The period is usually 2 to the 64 when you increment or decrement by 1, but some bounds may reduce the period (in
     * the extreme case, a bound of 1 would force only 0 to be generated, so that would make the period 1).
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determineBounded(++state, bound)} is recommended to go forwards or
     *              {@code determineBounded(--state, bound)} to generate numbers in reverse order
     * @param bound the outer exclusive bound for the int this produces; can be negative or positive
     * @return a pseudo-random int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(long state, final int bound)
    {
        return (int)((bound * (
                ((state = ((state *= 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22))
                        & 0xFFFFFFFFL)) >> 32);
    }
}

