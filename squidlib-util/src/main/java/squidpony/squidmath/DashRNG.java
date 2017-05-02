package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * Similar to ThunderRNG (emphasizes speed over quality), but unlike ThunderRNG this is a StatefulRandomness.
 * Being a StatefulRandomness means you can pass a DashRNG to the {@link StatefulRNG#StatefulRNG(RandomnessSource)}
 * constructor and have expected results, and also that you can get and set the state on a DashRNG directly. If a
 * DashRNG is stored in some variable that is then passed to {@link RNG#RNG(RandomnessSource)}, you can call
 * {@link #setState(long)} on the DashRNG variable to set the state used by the RNG, which can be very useful.
 * <br>
 * The {@link #nextLong()} method on this class can produce 64-bit data in somewhere between 3/4 and 2/3 the time needed
 * by LightRNG, and only slightly more than ThunderRNG. This class fails many more statistical tests than ThunderRNG or
 * especially LightRNG; this probably doesn't matter for games. If you need something closer to a cryptographic RNG, you
 * can use IsaacRNG (though it won't be as fast). The speed of DashRNG probably makes breaking it easier, especially in
 * conjunction with its poor statistical quality. It's likely that the period of DashRNG is a full 2 to the 64 (0 seed
 * is allowed), but the distribution is also likely to cover much less than the full range of all longs.
 * <br>
 * Created by Tommy Ettinger on 4/30/2017.
 * @see FlapRNG FlapRNG is a variant on this class that uses primarily 32-bit math (good for use on GWT)
 */
public class DashRNG implements StatefulRandomness, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a DashRNG using two calls to {@link Math#random()} to make a 64-bit mostly-random seed.
     */
    public DashRNG(){
        this((long)(Math.random() * 0x100000000L) << 32 | (long)(Math.random() * 0x100000000L));
    }

    /**
     * Constructs a DashRNG using the given seed without changes.
     * @param seed any long
     */
    public DashRNG(long seed) {
        state = seed;
    }

    /**
     * Constructs a DashRNG using the 64-bit hash of the given CharSequence (such as a String) as its seed.
     * Uses {@link CrossHash.Wisp#hash64(CharSequence)} to get a 64-bit long seed from the CharSequence given.
     * @param seed
     */
    public DashRNG(CharSequence seed)
    {
        state = CrossHash.Wisp.hash64(seed);
    }

    public long state;
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
     * @param state a 64-bit long. DashRNG can handle any long parameter.
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
    public final int next( final int bits ) {
        return (int)( nextLong() >>> (64 - bits) );
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
        //return ((state += 0x9E3779B97F4A7C15L) >>> 8) * 0x8329C6EB9E6AD3E3L ^ (state >>> 9) * 0xC6BC279692B5CC83L;
//        final long z = (state += 0x9E3779B97F4A7C15L);
//        final int r = (int)(z >>> 58);
//        return (z >>> r) | (z << (64 - r));
        final long z = (state += 0x9E3779B97F4A7C15L);
        return (z >> (z >>> 59)) * 0xC6BC279692B5CC83L;
//        return (z >>> (z >>> 58)) | (z << (64L - (z >>> 58)));

//        final long z = state + 0x9E3779B97F4A7C15L;
//        return state = (z ^ (z >> 8)) * 0x8329C6EB9E6AD3E3L;

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
        return new DashRNG(state);
    }

    /**
     * A simple "output stage" applied to state, this method does not update state on its own. If you expect to call
     * this method more than once, you should add a large long that is an odd number to state as part of the call, such
     * as with {@code DashRNG.determine(state += 0x9E3779B97F4A7C15L)}. Here, "large long" means at least two of the
     * upper 5 bits should be set to be safe. The golden-ratio-derived constant 0x9E3779B97F4A7C15L should be fine.
     * This method doesn't offer particularly good quality assurances, but should be very fast.
     * @param state should be changed when you call this by adding some large odd number, e.g. 0x9E3779B97F4A7C15L
     * @return an altered version of state that should be very fast to compute but doesn't promise great quality
     */
    public static long determine(final long state)
    {
        return (state >> (state >>> 59)) * 0xC6BC279692B5CC83L;

    }
    @Override
    public String toString() {
        return "DashRNG with state 0x" + StringKit.hex(state);
    }

    @Override
    public int hashCode() {
        return 0x632BE5AB * (int)(state ^ state >>> 32);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DashRNG dashRNG = (DashRNG) o;

        return state == dashRNG.state;
    }
}
