package squidpony.squidmath;


import squidpony.StringKit;
import squidpony.annotation.Beta;

import java.io.Serializable;

/**
 * A very fast and high-quality generator, but one that is not equidistributed because it produces 64-bit longs from 63
 * bits of state (meaning at least half of all possible long values cannot be returned by this generator). This still
 * passes gjrand with no failures on 100 GB of test data, and PractRand with 1TB (possibly more), while sometimes being
 * significantly faster than {@link ThrustRNG} and {@link ThrustAltRNG}. ThrustRNG fails PractRand at 32GB, so this
 * seems to be a substantial improvement in some ways, but ThrustAltRNG passes PractRand at the normal limit of 32TB,
 * which this may as well despite having a limited distribution. This does not pass all of PractRand in the current
 * version, failing at 2TB, so this class is expected to change if an improvement is found. The inlined version of
 * {@link #nextLong()} is the fastest generator in all of SquidLib, even counting deeply-flawed generators like
 * {@link LapRNG}. The name comes from its origin with Thrust, but as opposed to a full-body movement for a thrust in
 * fencing, this only "moves" some of its bits, like a jab in boxing.
 * <br>
 * The state is always odd here, and {@link #setState(long)} will always ensure that is maintained.
 * <br>
 * Created by Tommy Ettinger on 11/1/2017.
 */
@Beta
public final class JabRNG implements StatefulRandomness, Serializable {
    private static final long serialVersionUID = 2L;
    /**
     * Can be any odd-number long value.
     */
    private long state;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public JabRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public JabRNG(final long seed) {
        state = seed | 1L;
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
     * @param state a 64-bit long. You may want to avoid passing 0 for compatibility, though this implementation can handle that.
     */
    @Override
    public void setState(long state) {
        this.state = state | 1L;
    }

    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public final int next(int bits) {
        long z = (state += 0x3C6EF372FE94F82AL);
        z *= (z ^ (z >>> 21));
        return (int)(z ^ z >>> 28) >>> (32 - bits);
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
        long z = (state += 0x3C6EF372FE94F82AL);
        z *= (z ^ (z >>> 21));
        return z ^ z >>> 28;
    }

    /**
     * Call with {@code nextLong(z += 0x3C6EF372FE94F82AL)}. Using -= is also OK. The value for z must be odd; you
     * can ensure this initially with {@code z |= 1} if it is set from a random source or user input. Using the given
     * increment (or decrement) value for z will keep an odd-number value for z as an odd number; this is important to
     * the behavior of this algorithm.
     * @param z an odd-number long that should change with {@code z += 0x3C6EF372FE94F82AL} each time this is called
     * @return a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive)
     */
    public static long randomize(long z) {
        return (z *= (z ^ (z >>> 21))) ^ (z >>> 28);
    }

    /**
     * Advances or rolls back the Jab63RNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextLong(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    public final long skip(long advance) {
        long z = (state += 0x3C6EF372FE94F82AL * advance);
        z *= (z ^ (z >>> 21));
        return z ^ z >>> 28;
    }


    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public JabRNG copy() {
        return new JabRNG(state);
    }
    @Override
    public String toString() {
        return "JabRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JabRNG jabRNG = (JabRNG) o;

        return state == jabRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state >>> 1 ^ (state >>> 32));
    }

    /**
     * Returns a random permutation of state; if state is the same on two calls to this, this will return the same
     * number. This is expected to be called with some changing variable, e.g. {@code determine(++state)}, where
     * the increment for state should be odd but otherwise doesn't really matter. This uses state multiplied by
     * {@code 0x6A5D39EAE12657BAL} within this method, so using a small increment won't be much different from using a
     * very large one, as long as it is odd. You want to use {@link #randomize(long)} instead if you can control how
     * the state is changed and can ensure it is an odd number; randomize() uses the exact behavior of the
     * RandomnessSource methods in class and this method doesn't, necessarily.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random permutation of state
     * @see #randomize(long) randomize is meant for when you can ensure the state is odd and will be changed exactly
     */
    public static long determine(long state)
    {
        state = state * 0x3C6EF372FE94F82AL | 1L;
        state *= (state ^ (state >>> 21));
        return state ^ state >>> 28;
    }

    /**
     * Given a state that should usually change each time this is called, and a bound that limits the result to some
     * (typically fairly small) int, produces a pseudo-random int between 0 and bound (exclusive). The bound can be
     * negative, which will cause this to produce 0 or a negative int; otherwise this produces 0 or a positive int.
     * The state should change each time this is called, generally by incrementing by an odd number (not an even number,
     * especially not 0). It's fine to use {@code determineBounded(++state, bound)} to get a different int each time.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determineBounded(++state, bound)} is recommended to go forwards or
     *              {@code determineBounded(--state, bound)} to generate numbers in reverse order
     * @param bound the outer exclusive bound for the int this produces; can be negative or positive
     * @return a pseudo-random int between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(long state, final int bound)
    {
        state = state * 0x3C6EF372FE94F82AL | 1L;
        state *= (state ^ (state >>> 21));
        return (int)((bound * ((state ^ state >>> 28) & 0x7FFFFFFFL)) >> 31);
    }
}
