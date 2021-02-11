package squidpony.squidmath;

import java.io.Serializable;

/**
 * A specialized StatefulRandomness that produces a pre-determined sequence of results as {@link #nextLong()}
 * and/or {@link #next(int)} are called. This is meant for testing corner-case bugs in any kind of procedural
 * generation, where certain, possibly very rare, number sequences at points in a program cause issues.
 * Rather than rely on finding a seed that, at just the right point in its sequence, causes such a problem,
 * you can use a SpecifiedRandomness that could cause the problem right away so it can be debugged.
 * <br>
 * The idea for this came from GoRogue and its
 * <a href="https://github.com/Chris3606/GoRogue/blob/master/GoRogue/Random/KnownSeriesRandom.cs">KnownSeriesRandom</a>
 * class. Thanks Chris3606!
 */
public class SpecifiedRandomness implements StatefulRandomness, Serializable {
    private static final long serialVersionUID = 0L;

    public long state;
    public long[] results;

    /**
     * Constructs a SpecifiedRandomness that will produce the sequence:
     * {@code -1, 0, 9223372036854775807, -9223372036854775808}
     * in a cycle.
     */
    public SpecifiedRandomness() {
        state = 0L;
        results = new long[]{-1L, 0L, Long.MAX_VALUE, Long.MIN_VALUE};
    }

    /**
     * Constructs a SpecifiedRandomness given a non-null, non-empty array or vararg of long results.
     * This array or vararg will be the sequence this produces.
     * @param results a non-null, non-empty array or vararg of long to use as the sequence this produces
     */
    public SpecifiedRandomness(long... results) {
        state = 0L;
        this.results = results == null || results.length == 0
                ? new long[]{-1L, 0L, Long.MAX_VALUE, Long.MIN_VALUE} : results;
    }

    /**
     * Copies the given SpecifiedRandomness into a new one, using the same state and results sequence.
     * @param other another, non-null, SpecifiedRandomness to deep-copy into this
     */
    public SpecifiedRandomness(SpecifiedRandomness other) {
        state = other.state;
        results = new long[other.results.length];
        System.arraycopy(other.results, 0, results, 0, results.length);
    }

    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public int next(int bits) {
        return (int) results[(int) (state++ & 0x7FFFFFFFL) % results.length] & (-1 >>> 32 - bits);
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
    public long nextLong() {
        return results[(int) (state++ & 0x7FFFFFFFL) % results.length];
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
     * @param state any 64-bit long.
     */
    @Override
    public void setState(long state) {
        this.state = state;
    }

    /**
     * Produces a copy of this SpecifiedRandomness that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this SpecifiedRandomness
     */
    @Override
    public SpecifiedRandomness copy() {
        return new SpecifiedRandomness(this);
    }
}
