package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * Similar to DashRNG (emphasizes speed over quality), but like PintRNG this only uses 32-bit int math (good for GWT).
 * It's likely that the period of FlapRNG is a full 2 to the 32 (0 seed is allowed), but the distribution for 32-bit
 * values is also likely to cover much less than the full range of all ints. The quality of the random numbers this
 * produces is probably rather bad, but the speed is on par with ThunderRNG (our current best generator for longs) when
 * this generates ints and ThunderRNG generates longs. FlapRNG generates ints at a higher speed than ThunderRNG
 * generates ints, but because FlapRNG needs to generate and mix two ints to make a long, it takes over twice the time
 * as ThunderRNG to generate longs. Long story short, FlapRNG is a good choice if you need a StatefulRandomness that
 * uses int math, you don't need great quality, and you do want great speed. If you want a much-higher-quality int-math
 * StatefulRandomness generator, use PintRNG.
 * <br>
 * Created by Tommy Ettinger on 5/1/2017.
 */
public class FlapRNG implements StatefulRandomness, Serializable {
    private static final long serialVersionUID = 1L;
    public FlapRNG(){
        this((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    public FlapRNG(final int seed) {
        state = seed;
    }
    public FlapRNG(final long seed) {
        state = (int)((seed ^ seed >>> 32) & 0xFFFFFFFFL);
    }
    public FlapRNG(final CharSequence seed)
    {
        state = CrossHash.Wisp.hash(seed);
    }

    public int state;
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
     * @param state a 64-bit long, but this is always truncated when used; the upper 32 bits are discarded
     */
    @Override
    public void setState(final long state) {
        this.state = (int)(state & 0xFFFFFFFFL);
    }

    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public final int next( final int bits ) {
        return nextInt() >>> (32 - bits);
    }

    public final int nextInt()
    {
        final int z = (state += 0x9E3779B9);
        return (z >> (z >>> 28)) * 0xC6BC278D;
    }
    /**
     * Using this method, any algorithm that needs to efficiently generate more
     * than 32 bits of random data can interface with this randomness source.
     * This particular implementation is not much more efficient than calling
     * {@link #nextInt()} twice.
     * <p>
     * Get a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive).
     *
     * @return a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive)
     */
    @Override
    public final long nextLong() {
        int z = state + 0x9E3779B9;
        final long j = (z >> (z >>> 28)) * 0xC6BC279692B5CC83L;
        z = (state += 0x3C6EF372);
        return j ^ ((z >> (z >>> 28)) * 0xC6BC279692B5CC83L) << 32;
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
        return new FlapRNG(state);
    }

    /**
     * A simple "output stage" applied to state, this method does not update state on its own. If you expect to call
     * this method more than once, you should add a large int that is an odd number to state as part of the call, such
     * as with {@code FlapRNG.determine(state += 0x9E3779B9)}. Here, "large long" means at least two of the
     * upper 5 bits should be set to be safe. The golden-ratio-derived constant 0x9E3779B9 should be fine.
     * This method doesn't offer particularly good quality assurances, but should be very fast.
     * @param state should be changed when you call this by adding some large odd number, e.g. 0x9E3779B9
     * @return an altered version of state that should be very fast to compute but doesn't promise great quality
     */
    public static int determine(final int state)
    {
        return (state >> (state >>> 28)) * 0xC6BC278D;
    }
    /**
     * Gets a pseudo-random float between 0f (inclusive) and 1f (exclusive) using the given state. Calling this
     * repeatedly won't have correlation problems if you give it sequential ints.
     * @param state any int
     * @return a pseudo-random float from -0f (inclusive) to 1f (exclusive)
     */
    public static float randomFloat(int state)
    {
        return NumberTools.intBitsToFloat(((((state *= 0x9E3779B9) >> (state >>> 28)) * 0xC6BC278D) >>> 9) | 0x3f800000) - 1f;
    }

    /**
     * Gets a pseudo-random float between -1f (inclusive) and 1f (exclusive) using the given state. Calling this
     * repeatedly won't have correlation problems if you give it sequential ints.
     * @param state any int
     * @return a pseudo-random float from -1f (inclusive) to 1f (exclusive)
     */
    public static float randomSignedFloat(int state)
    {
        return NumberTools.intBitsToFloat(((((state *= 0x9E3779B9) >> (state >>> 28)) * 0xC6BC278D) >>> 9) | 0x40000000) - 3f;
    }

    @Override
    public String toString() {
        return "FlapRNG with state 0x" + StringKit.hex(state);
    }

    @Override
    public int hashCode() {
        return 0x632BE5AB * state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlapRNG flapRNG = (FlapRNG) o;

        return state == flapRNG.state;
    }

}
