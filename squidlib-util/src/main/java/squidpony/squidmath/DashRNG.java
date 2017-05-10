package squidpony.squidmath;

import squidpony.StringKit;
import squidpony.annotation.Beta;

import java.io.Serializable;

/**
 * A work in progress as I try to generate numbers as quickly as possible without completely giving up decent quality.
 * So far, not much luck.
 * <br>
 * Created by Tommy Ettinger on 4/30/2017.
 */
@Beta
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

        // current best quality
        final long z = state;
        return state += ~(z << 1) * 0x9E3779B97F4A7C15L;//0xC6BC279692B5CC83L;

        //trying to enhance optimize-ability
        //return state += 0x9E3779B97F4A7C15L + (state >> (state & 31L)) * 0xC6BC279692B5CC83L;
        //return state += (state >> 5) * 0xC6BC279692B5CC83L + 0x9E3779B97F4A7C15L;

        // good quality, pretty bad speed (2000 ms per billion)
        //return state += ((state + 0x9E3779B97F4A7C15L) >> 5) * 0xC6BC279692B5CC83L;

        //final long z = (state + 0x9E3779B97F4A7C15L);
        //final int r = (int) ((state & 15L) + (z >>> 60));
        //return (state += (state * 0x9E3779B97F4A7C15L) >> (state >>> 59)) * 0xC6BC279692B5CC83L;
        //return state += ((++state) >> (state >>> 59)) * 0xC6BC279692B5CC83L;
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
