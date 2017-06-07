package squidpony.squidmath;

import squidpony.StringKit;
import squidpony.annotation.Beta;

import java.io.Serializable;

/**
 * A variant on LapRNG that improves the quality at the expense of some speed. Running ZapRNG through PractRand, a tool
 * for evaluating the statistical quality of PRNGs, found far, far fewer statistical failures than LapRNG, and also had
 * them generally be less severe. The period is still relatively low, at (with a high degree of certainty) 2 to the 65,
 * but the speed is still fairly good; approximately the same as or slightly faster than ThunderRNG (generally within
 * 1% difference, and definitely within the margin of error), faster than LightRNG all around, slower than LapRNG for
 * all methods, and slower than FlapRNG on next() and nextInt() but not on nextLong(). Like LapRNG, there should be many
 * possible sequences of length (2 to the 65) this can produce, depending on the relationship between the two longs used
 * for state, determined at construction or when the seed is set.
 * <br>
 * Created by Tommy Ettinger on 5/25/2017.
 */
@Beta
public class ZapRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * Constructs a ZapRNG with a random internal state, using {@link Math#random()} four times to get enough bits.
     */
    public ZapRNG(){
        this((long)((Math.random() * 2.0 - 1.0) * 0x8000000000000L)
                        ^ (long)((Math.random() * 2.0 - 1.0) * 0x8000000000000000L),
                (long)((Math.random() * 2.0 - 1.0) * 0x8000000000000L)
                        ^ (long)((Math.random() * 2.0 - 1.0) * 0x8000000000000000L));
    }

    /**
     * Given one long for a seed, this fills the two internal longs of state with different values, each produced by
     * multiplying seed by a large constant and adding a different large constant. You can obtain the state that this
     * ends up using with {@link #getState0()} and {@link #getState1()}.
     * @param seed any long; will not be used verbatim for either internal state
     */
    public ZapRNG(final long seed) {
        state0 = seed * 0xC6BC279692B5C483L + 0x8329C6EB9E6AD3E3L;
        state1 = seed * 0x8329C6EB9E6AD3E3L - 0xC6BC279692B5C483L;
    }
    /**
     * Given one int for a seed, this fills the two internal longs of state with different values, each produced by
     * multiplying seed by a large constant and adding a different large constant. You can obtain the state that this
     * ends up using with {@link #getState0()} and {@link #getState1()}.
     * @param seed any int; will not be used verbatim for either internal state
     */
    public ZapRNG(final int seed) {
        state0 = seed * 0xC6BC279692B5C483L + 0x8329C6EB9E6AD3E3L;
        state1 = seed * 0x8329C6EB9E6AD3E3L - 0xC6BC279692B5C483L;
    }

    /**
     * The only constructor that does not modify its seeds in some way.
     * @param seed0 any long
     * @param seed1 any long
     */
    public ZapRNG(final long seed0, final long seed1) {
        state0 = seed0;
        state1 = seed1;
    }

    /**
     * This constructor takes ints for seeds, but internally ZapRNG uses longs, so it multiplies each input by a large
     * constant and adds another to get the state it actually will start with. You can obtain the state that this ends
     * up using with {@link #getState0()} and {@link #getState1()}.
     * @param seed0 any int, will not be used verbatim
     * @param seed1 any int, will not be used verbatim
     */
    public ZapRNG(final int seed0, final int seed1) {
        state0 = seed0 * 0xC6BC279692B5C483L + 0x8329C6EB9E6AD3E3L;
        state1 = seed1 * 0x8329C6EB9E6AD3E3L - 0xC6BC279692B5C483L;
    }

    /**
     * This constructor takes 3 ints for seeds, but internally ZapRNG uses 2 longs, so it mixes the parameters around
     * to fill out 128 bits worth of state. This is mainly provided for compatibility with LapRNG, which works better
     * with a constructor that is guaranteed to have many of the bits set in places that it treats as important, but
     * this behavior isn't needed here, so this acts much like the other constructors and just uses seed2 as an extra
     * modifying factor when setting the state.
     * You can obtain the state that this ends up using with {@link #getState0()} and {@link #getState1()}.
     * @param seed0 any int, will not be used verbatim
     * @param seed1 any int, will not be used verbatim
     * @param seed2 any int, will not be used verbatim
     */
    public ZapRNG(final int seed0, final int seed1, final int seed2) {
        state0 = (seed0 * 0xC6BC279692B5C483L ^ seed1 + 0x9E3779B97F4A7C15L) - seed2 * 0x8329C6EB9E6AD3E3L;
        state1 = (seed1 * 0x8329C6EB9E6AD3E3L ^ seed2 - 0xC6BC279692B5C483L) + seed0 * 0x9E3779B97F4A7C15L;
    }

    /**
     * This constructor gets a pair of differently-calculated 64-bit hash codes from the given String or other
     * CharSequence and gives them to the constructor that takes two longs, {@link #ZapRNG(long, long)}. You can pass a
     * null seed and this will still work.
     * @param seed any CharSequence, such as a String
     */
    public ZapRNG(final CharSequence seed)
    {
        this(CrossHash.Mist.alpha.hash64(seed) ^ 0xC6BC279692B5C483L, CrossHash.Mist.beta.hash64(seed));
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
        return (int) (((state0 | 0xC6BC279692B5CC83L) * (state1 += (state0 += 0x9E3779B97F4A7C15L) >>> 24)) >>> (64 - bits));
    }

    /**
     * Gets a pseudo-random int, which can be positive or negative but is likely to be drawn from less possible options
     * than the full range of {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}. Very fast, though.
     * @return a pseudo-random 32-bit int
     */
    public final int nextInt()
    {
        return (int) ((state0 | 0xC6BC279692B5CC83L) * (state1 += (state0 += 0x9E3779B97F4A7C15L) >>> 24));
    }
    /**
     * Using this method, any algorithm that needs to efficiently generate more
     * than 32 bits of random data can interface with this randomness source.
     * This implementation is the primary one, and {@link #nextInt()} and {@link #next(int)} just use some portion of
     * the bits this produces. It should be fairly fast on desktops, with good-not-great quality, unlike alternatives
     * such as LapRNG, which is extremely fast but has low quality when inspected enough, and LightRNG, which the
     * fastest of the "Made by an Expert" RandomnessSources and has superior quality but is somewhat slower.
     * <p>
     * Pseudo-random results may be between between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive).
     *
     * @return a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive)
     */
    @Override
    public final long nextLong() {
        //return (state1 += state0 ^ (state0 += 0xD43779B97F4A7C13L) >> 24);
        return ((state0 | 0xC6BC279692B5CC83L) * (state1 += (state0 += 0x9E3779B97F4A7C15L) >>> 24));

        //return (state0 += (((state1 += 0xC6BC279692B5C483L) >>> 59) + 124) * 0x632AE59B79B4E319L);
        //0x9E3779B97F4A7C15L
        //0xBF58476D1CE4E5B9L
        //0x632AE59B69B3C209L
        //0xE32AF556CBA5E739L
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
        return new ZapRNG(state0, state1);
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

    public void setState(final long seed)
    {
        state0 = seed * 0xC6BC279692B5C483L + 0x8329C6EB9E6AD3E3L;
        state1 = seed * 0x8329C6EB9E6AD3E3L - 0xC6BC279692B5C483L;
    }
    public void setState(final long seed0, final long seed1) {
        state0 = seed0 * 0xC6BC279692B5C483L + 0x8329C6EB9E6AD3E3L;
        state1 = seed1 * 0x8329C6EB9E6AD3E3L - 0xC6BC279692B5C483L;
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
        return "ZapRNG with state0 0x" + StringKit.hex(state0) + ", state1 0x" + StringKit.hex(state1);
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

        ZapRNG zapRNG = (ZapRNG) o;

        if (state0 != zapRNG.state0) return false;
        return state1 == zapRNG.state1;
    }
}
