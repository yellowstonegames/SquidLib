package squidpony.squidmath;

import squidpony.StringKit;
import squidpony.annotation.Beta;

import java.io.Serializable;

/**
 * A 64-bit generator based on {@link ThrustAltRNG} but with 2 to the 64 possible streams instead of 1 stream.
 * Its period is 2 to the 64, but you can change the stream after some large amount of generated numbers if you want to
 * effectively extend the period. It is currently slightly slower than LightRNG, a generator that at least in theory
 * supports 2 to the 63 switchable streams, but its SplitMix64 algorithm in practice requires disallowing many of
 * those streams. It is unclear how many streams of Vortex may be unsuitable, though because the stream variable changes
 * in-step with the state variable, it seems less likely that a single stream would be problematic for long.
 * <br>
 * This implements SkippingRandomness but not StatefulRandomness, because while you can skip forwards or backwards from
 * any given state in constant time, you would need to set two variables (state and stream) to accurately change the
 * state, while StatefulRandomness only permits returning one 64-bit long for state or setting the state with one long.
 * <br>
 * Created by Tommy Ettinger on 11/9/2017.
 */
@Beta
public final class VortexRNG implements RandomnessSource, SkippingRandomness, Serializable {
    private static final long serialVersionUID = 3L;
    /**
     * Can be any long value.
     */
    public long state;

    /**
     * A long that decides which stream this VortexRNG will generate numbers with; the stream changes in a Weyl
     * sequence (adding a large odd number), and the relationship between the Weyl sequence and the state determines how
     * numbers will be generated differently when stream or state changes. It's perfectly fine to supply a value of 0
     * for stream, since it won't be used verbatim and will also change during the first number generation.
     * <br>
     * This can be changed after construction but not with any guarantees of quality staying the same
     * relative to previously-generated numbers on a different stream.
     */
    public long stream;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public VortexRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }
    public VortexRNG(long seed)
    {
        state = seed;
        stream = -1L;
    }
    public VortexRNG(final long seed, final long stream) {
        state = seed;
        this.stream = stream;
    }

    /**
     * Get the current internal state of this VortexRNG as a long.
     * This is not the full state; you also need {@link #getStream()}.
     *
     * @return the current internal state of this object.
     */
    public long getState() {
        return state;
    }
    /**
     * Set the current internal state of this VortexRNG with a long.
     * @param state any 64-bit long
     */
    public void setState(long state) {
        this.state = state;
    }
    /**
     * Get the current internal stream of this VortexRNG as a long.
     * This is not the full state; you also need {@link #getState()}.
     *
     * @return the current internal stream of this object.
     */
    public long getStream() {
        return stream;
    }
    /**
     * Set the current internal stream of this VortexRNG with a long.
     * @param stream any 64-bit long
     */
    public void setStream(long stream) {
        this.stream = stream;
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
        long z = (state += 0x6C8E9CF570932BD5L);
        z = (z ^ (z >>> 25)) * ((stream += 0x9E3779B97F4A7BB5L) | 1L);
        return (int)(
                (z ^ (z >>> 28))
                >>> (64 - bits));
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
        long z = (state += 0x6C8E9CF570932BD5L);
        z = (z ^ (z >>> 25)) * ((stream += 0x9E3779B97F4A7BB5L) | 1L);
        return z ^ (z >>> 28);
    }

    /**
     * Call with {@code VortexRNG.determine(++state, ++stream)}, where state and stream can each be any long; if the
     * assignments to state and stream have stayed intact on the next time this is called in the same way, it will have
     * the same qualities as VortexRNG normally does. You can use {@code VortexRNG.determine(--state, --stream)} to go
     * backwards. If you have control over state and stream, you may prefer {@link #determineBare(long, long)}, which
     * requires adding a specific large number to each parameter but may be slightly faster.
     * @param state any long; increment while calling with {@code ++state}
     * @param stream any long; increment while calling with {@code ++stream}
     * @return a pseudo-random long obtained from the given state and stream deterministically
     */
    public static long determine(long state, long stream)
    {
        state = ((state *= 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (stream * 0x9E3779B97F4A7BB5L | 1L);
        return state ^ (state >>> 28);
    }

    /**
     * Call with {@code VortexRNG.determineBare(state += 0x6C8E9CF570932BD5L, stream += 0x9E3779B97F4A7BB5L)}, where
     * state and stream can each be any long; if the assignments to state and stream have stayed intact on
     * the next time this is called in the same way, it will have the same qualities as VortexRNG normally does.
     * You can probably experiment with different increments for stream. 0x9E3779B97F4A7BB5L is a fixed-point version of
     * the golden ratio that takes up 64 bits, and using an irrational number like the golden ratio is key to how a Weyl
     * sequence like this should be made, but any large-enough odd number should work as an increment. You probably
     * shouldn't change the increment for state, since other parts of the generator depend on its qualities (to be
     * precise, bitwise shifts are aligned to where clusters of bits are the same in the increment 0x6C8E9CF570932BD5L).
     * @param state any long; increment while calling with {@code state += 0x6C8E9CF570932BD5L} (this number should not be changed)
     * @param stream any long; increment while calling with {@code stream += 0x9E3779B97F4A7BB5L} (other large odd numbers may work just as well)
     * @return a pseudo-random long obtained from the given state and stream deterministically
     */
    public static long determineBare(long state, long stream)
    {
        state = (state ^ (state >>> 25)) * (stream | 1L);
        return state ^ (state >>> 28);
    }
//public static long vortex(long state, long stream) { state = ((state *= 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (stream * 0x9E3779B97F4A7BB5L | 1L); return state ^ (state >>> 28); } //vortex(++state, ++stream)
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
        long z = (state += 0x6C8E9CF570932BD5L * advance);
        z = (z ^ z >>> 25) * ((stream += 0x9E3779B97F4A7BB5L * advance) | 1L);
        return z ^ (z >>> 28);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public VortexRNG copy() {
        return new VortexRNG(state, stream);
    }
    @Override
    public String toString() {
        return "VortexRNG on stream 0x" + StringKit.hex(stream) + "L with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VortexRNG vortexRNG = (VortexRNG) o;

        return state == vortexRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) ((state ^ state >>> 32) + 31 * (stream >>> 1 ^ stream >>> 33));
    }
}
