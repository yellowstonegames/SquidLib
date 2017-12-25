package squidpony.squidmath;

import squidpony.StringKit;
import squidpony.annotation.Beta;

import java.io.Serializable;

/**
 * Like FlapRNG, this sacrifices quality for speed, but it uses 64-bit math and as such has a larger period (FlapRNG
 * will repeat the cycle of random numbers it produces after about 8 billion numbers, while this will only repeat after
 * many more, probably 2 to the 65). As such, LapRNG is like FlapRNG but can generate for a longer period of time (more
 * laps) without repeating, and in some cases (mainly generating 64-bit values) at even higher speed. It is currently
 * the fastest RandomnessSource in this library at generating 64-bit longs, which is most of what {@link RNG} uses, and
 * is second-fastest at generating 32-bit ints, with FlapRNG just slightly faster at that. While the period must be at
 * least 2 to the 64, it is likely that it is actually 2 to the 65. This is one of three RandomnessSources recommended
 * for frequent use, but only if speed is the only consideration; the quality of the numbers this produces is very bad
 * (though it appears random to even a trained human eye, a computer could detect patterns).
 * <br>
 * Created by Tommy Ettinger on 5/25/2017.
 * @see FlapRNG FlapRNG is similar to LapRNG but is implemented using int math; LapRNG should usually be preferred
 */
@Beta
public class LapRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * Constructs a LapRNG with a random internal state, using {@link Math#random()} three times to get enough bits and
     * passing responsibility off to {@link #LapRNG(int, int, int)}.
     */
    public LapRNG(){
        this((int) ((Math.random() * 2.0 - 1.0) * 0x80000000),
                (int) ((Math.random() * 2.0 - 1.0) * 0x80000000),
                (int) ((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    /**
     * Given one long for a seed, this fills the two internal longs of state with different values, each produced by
     * multiplying seed by a large constant and adding a different large constant. You can obtain the state that this
     * ends up using with {@link #getState0()} and {@link #getState1()}.
     * @param seed any long; will not be used verbatim for either internal state
     */
    public LapRNG(final long seed) {
        state0 = seed * 0xC6BC279692B5C483L + 0x8329C6EB9E6AD3E3L;
        state1 = seed * 0x8329C6EB9E6AD3E3L - 0xC6BC279692B5C483L;
    }
    /**
     * Given one int for a seed, this fills the two internal longs of state with different values, each produced by
     * multiplying seed by a large constant and adding a different large constant. You can obtain the state that this
     * ends up using with {@link #getState0()} and {@link #getState1()}.
     * @param seed any int; will not be used verbatim for either internal state
     */
    public LapRNG(final int seed) {
        state0 = seed * 0xC6BC279692B5C483L + 0x8329C6EB9E6AD3E3L;
        state1 = seed * 0x8329C6EB9E6AD3E3L - 0xC6BC279692B5C483L;
    }

    /**
     * The only constructor that does not modify its seeds in some way; you should ensure seed0 is large enough (at
     * least 25 bits should be needed to represent it, so greater than 33554432 or less than -33554433). If seed0 is too
     * small, then this will appear highly repetitive for many cycles before changing its output pattern.
     * @param seed0 a long that should be greater than 33554432 or less than -33554433, preferably by a large amount
     * @param seed1 any long
     */
    public LapRNG(final long seed0, final long seed1) {
        state0 = seed0;
        state1 = seed1;
    }

    /**
     * This constructor takes ints for seeds, but internally LapRNG uses longs, so it multiplies each input by a large
     * constant and adds another to get the state it actually will start with. You can obtain the state that this ends
     * up using with {@link #getState0()} and {@link #getState1()}.
     * @param seed0 any int, will not be used verbatim
     * @param seed1 any int, will not be used verbatim
     */
    public LapRNG(final int seed0, final int seed1) {
        state0 = seed0 * 0xC6BC279692B5C483L + 0x8329C6EB9E6AD3E3L;
        state1 = seed1 * 0x8329C6EB9E6AD3E3L - 0xC6BC279692B5C483L;
    }
    /**
     * This constructor takes 3 ints for seeds, but internally LapRNG uses 2 longs (one, state0, only considers its
     * upper 48 bits significant and has closely correlated results if inputs don't differ on significant bits). To
     * ensure the state works fairly well, this uses seed0 to affect the upper 48 bits of state0 (this also uses the
     * product of seed1 and seed2), while employing all 3 seed parameters again to set state1 (but these can affect all
     * 64 bits of state1). Note that only 96 bits of parameter are supplied here to set 128 bits of state, which is all
     * right in this case because LapRNG only has a period of 2 to the 65. Anything more than 65 bits of state isn't
     * strictly necessary, though giving more should allow different, non-overlapping sequences to be produced.
     * You can obtain the state that this ends up using with {@link #getState0()} and {@link #getState1()}.
     * @param seed0 any int, will not be used verbatim; should probably not be 0
     * @param seed1 any int, will not be used verbatim; should probably not be 0
     * @param seed2 any int, will not be used verbatim; should probably not be 0
     */
    public LapRNG(final int seed0, final int seed1, final int seed2) {
        state0 = (seed0 * 8219L + seed1 * 4999L * seed2 << 20) ^ 0x8329C6EB9E6AD3E3L;
        state1 = (seed1 * 0x8329C6EB9E6AD3E3L ^ seed2 - 0xC6BC279692B5C483L) + seed0 * 0x9E3779B97F4A7C15L;
    }

    /**
     * This constructor gets three differently-calculated 32-bit hash codes from the given String or other
     * CharSequence and gives them to the constructor that takes three ints, {@link #LapRNG(int, int, int)}. You can
     * pass a null seed and this will still work.
     * @param seed any CharSequence, such as a String
     */
    public LapRNG(final CharSequence seed)
    {
        this(CrossHash.Mist.alpha.hash(seed), CrossHash.Mist.beta.hash(seed), CrossHash.Mist.gamma.hash(seed));
    }

    private long state0, state1;

    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public final int next( final int bits ) {
        return (int) ((state1 += ((state0 += 0x9E3779B97F4A7C15L) >> 24) * 0x632AE59B69B3C209L) >>> (64 - bits));
    }

    /**
     * Gets a pseudo-random int, which can be positive or negative but is likely to be drawn from less possible options
     * than the full range of {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}. Very fast, though.
     * @return a pseudo-random 32-bit int
     */
    public final int nextInt()
    {
        return (int) (state1 += ((state0 += 0x9E3779B97F4A7C15L) >> 24) * 0x632AE59B69B3C209L);
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
     * Gets the linearly-changing part of the state.
     * @return one of the two parts of the state as a long
     */
    public long getState0() {
        return state0;
    }

    /**
     * Sets the linearly-changing part of the state; if the parameter is too small, the generator will behave poorly.
     * At minimum, the parameter should have significant bits beyond the 24th bit (greater than 33554432 for positive
     * longs), and should ideally be larger than can be represented in a 32-bit int.
     * @param state0 a large-enough long, either greater than 33554432 or less than -33554433, preferably greatly so
     */
    public void setState0(long state0) {
        this.state0 = state0;
    }

    /**
     * Gets the irregularly-changing part of the state.
     * @return one of the two parts of the state as a long
     */
    public long getState1() {
        return state1;
    }

    /**
     * Sets the irregularly-changing part of the state; just about any long can be passed as a reasonable value here.
     * @param state1 any long
     */
    public void setState1(long state1) {
        this.state1 = state1;
    }

    /**
     * @param state any long
     * @return any long, from the full range
     */
    public static long determine(final long state)
    {
        return (state + ((state * 0x9E3779B97F4A7C15L) >> 24) * 0x632AE59B69B3C209L);
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
     * @param state any long
     * @return any long, from the full range
     */
    public static int determineBounded(final long state, final int bound)
    {
        return (int)((bound * ((state + ((state * 0x9E3779B97F4A7C15L) >> 24) * 0x632AE59B69B3C209L) & 0x7FFFFFFFL)) >> 31);
    }

    /**
     * @param state0 any long
     * @param state1 any long
     * @return any long, from the full range
     */
    public static int determineBounded(final long state0, final long state1, final int bound)
    {
        return (int)((bound * ((state1 + ((state0 * 0x9E3779B97F4A7C15L) >> 24) * 0x632AE59B69B3C209L) & 0x7FFFFFFFL)) >> 31);

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
        return "LapRNG with state0 0x" + StringKit.hex(state0) + ", state1 0x" + StringKit.hex(state1);
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
