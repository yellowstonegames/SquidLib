package squidpony.squidmath;

import squidpony.StringKit;
import squidpony.annotation.Beta;

import java.io.Serializable;

/**
 * Like FlapRNG, this sacrifices quality for speed, but it uses 64-bit math and as such has a larger period (FlapRNG
 * will repeat the cycle of random numbers it produces after about 8 billion numbers, while this will only repeat after
 * many more, at least 2 to the 64). As such, LapRNG is like FlapRNG but can generate for a longer period of time (more
 * laps) without repeating, and in some cases (mainly generating 64-bit values) at even higher speed.
 * <br>
 * Created by Tommy Ettinger on 5/25/2017.
 */
@Beta
public class LapRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 1L;
    public LapRNG(){
        this((long)((Math.random() * 2.0 - 1.0) * 0x8000000000000L)
                ^ (long)((Math.random() * 2.0 - 1.0) * 0x8000000000000000L),
                (long)((Math.random() * 2.0 - 1.0) * 0x8000000000000L)
                        ^ (long)((Math.random() * 2.0 - 1.0) * 0x8000000000000000L));
    }
    public LapRNG(final long seed) {
        state0 = seed;
        state1 = LightRNG.determine(seed);
    }
    public LapRNG(final long seed0, final long seed1) {
        state0 = seed0;
        state1 = seed1;
    }
    public LapRNG(final CharSequence seed)
    {
        this(CrossHash.hash64(seed));
    }

    public long state0, state1;

    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public final int next( final int bits ) {
        return (int) ((state1 += ((state0 += 0x9E3779B97F4A7C15L) >> 24) * 0x632AE59B69B3C209L) >>> (32 - bits));
    }

    /**
     * Gets a pseudo-random int, which can be positive or negative but is likely to be drawn from less possible options
     * than the full range of {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}. Very fast, though.
     * @return a pseudo-random 32-bit int
     */
    public final int nextInt()
    {
        return (int) ((state1 += ((state0 += 0x9E3779B97F4A7C15L) >> 24) * 0x632AE59B69B3C209L) >>> 32);
    }
    /**
     * Using this method, any algorithm that needs to efficiently generate more
     * than 32 bits of random data can interface with this randomness source.
     * This implementation is the primary one, and {@link #nextInt()} and {@link #next(int)} just use some portion of
     * the bits this produces. It should be very, very fast on most desktops, with lower speed relative to its closest
     * competitor ({@link FlapRNG}) on 32-bit operating systems or 32-bit JREs or JDKs, and possibly lower speed on
     * mobile devices as well, but at the time of writing LapRNG is the fastest source SquidLib has of pseudo-random
     * long values.
     * <p>
     * Pseudo-random results may be between between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive).
     *
     * @return a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive)
     */
    @Override
    public final long nextLong() {
        return (state1 += ((state0 += 0x9E3779B97F4A7C15L) >> 24) * 0x632AE59B69B3C209L);
        //return (state0 += (((state1 += 0xC6BC279692B5C483L) >>> 59) + 124) * 0x632AE59B79B4E319L);
        //0x9E3779B97F4A7C15L
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
        return new LapRNG(state0, state1);
    }

    /**
     * @param state0 any long
     * @param state1 any long
     * @return any long, from the full range
     */
    public static long determine(final long state0, final long state1)
    {
        return (state1 + ((state0 * 0x9E3779B97F4A7C15L) >> 24) * 0x632AE59B69B3C209L);
    }
    /**
     * @param state0 any long
     * @param state1 any long
     * @return a pseudo-random double from 0.0 (inclusive) to 1.0 (exclusive)
     */
    public static double randomDouble(final long state0, final long state1)
    {
        return NumberTools.longBitsToDouble(((state1 + ((state0 * 0x9E3779B97F4A7C15L) >> 24) * 0x632AE59B69B3C209L)
                >>> 12) | 0x3ff00000) - 1f;
    }

    /**
     * @param state0 any long
     * @param state1 any long
     * @return a pseudo-random double from -1.0 (inclusive) to 1.0 (exclusive)
     */
    public static double randomSignedDouble(final long state0, final long state1) {
        return NumberTools.longBitsToDouble(((state1 + ((state0 * 0x9E3779B97F4A7C15L) >> 24) * 0x632AE59B69B3C209L)
                >>> 12) | 0x40000000) - 3.0;
    }

    @Override
    public String toString() {
        return "FlapRNG with state0 0x" + StringKit.hex(state0) + ", state1 0x" + StringKit.hex(state1);
    }

    @Override
    public int hashCode() {
        long h = (0x632BE5ABL * state0 ^ state1);
        return (int) (h ^ h >>> 32);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LapRNG lapRNG = (LapRNG) o;

        if (state0 != lapRNG.state0) return false;
        return state1 == lapRNG.state1;
    }
}
