/*  Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A port of Blackman and Vigna's xoroshiro128+ generator; should be very fast and produce medium-quality output.
 * Testing shows it is within 5% the speed of LightRNG, sometimes faster and sometimes slower, and has a larger period.
 * It's called XoRo because it involves Xor as well as Rotate operations on the 128-bit pseudo-random state. Note that
 * xoroshiro128+ fails some statistical quality tests systematically, and fails others often; if this could be a concern
 * for you, {@link LinnormRNG}, which is the default for {@link RNG}, will be faster and won't fail (those) tests, and
 * though its period is shorter, it would still take years to exhaust on one core generating only random numbers.
 * <br>
 * {@link LightRNG} is also very fast, but relative to XoRoRNG it has a significantly shorter period (the amount of
 * random numbers it will go through before repeating), at {@code pow(2, 64)} as opposed to XorRNG and XoRoRNG's
 * {@code pow(2, 128) - 1}, but LightRNG also allows the current RNG state to be retrieved and altered with
 * {@code getState()} and {@code setState()}. For most cases, you should decide between LinnormRNG, LightRNG, XoRoRNG,
 * and other RandomnessSource implementations based on your needs for period length and state manipulation (LinnormRNG
 * is also used internally by almost all StatefulRNG objects). You might want significantly less predictable random
 * results, which {@link IsaacRNG} can provide, along with a large period. You may want a very long period of random
 * numbers, which  would suggest {@link LongPeriodRNG} as a good choice or {@link MersenneTwister} as a potential
 * alternative. You may want better performance on 32-bit machines or on GWT, where {@link Starfish32RNG} is currently
 * the best choice most of the time, and {@link Lathe32RNG} can be faster but has slightly worse quality (both of these
 * generators use a 32-bit variant on the xoroshiro algorithm but change the output scrambler). These all can generate
 * pseudo-random numbers in a handful of nanoseconds (with the key exception of 64-bit generators being used on GWT,
 * where they may take more than 100 nanoseconds per number), so unless you need a LOT of random numbers in a hurry,
 * they'll probably all be fine on performance. You may want to decide on the special features of a generator, indicated
 * by implementing {@link StatefulRandomness} if their state can be read and written to, and/or
 * {@link SkippingRandomness} if sections in the generator's sequence can be skipped in long forward or backward leaps.
 * <br>
 * <a href="http://xoroshiro.di.unimi.it/xoroshiro128plus.c">Original version here.</a>
 * <br>
 * Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)
 *
 * @author Sebastiano Vigna
 * @author David Blackman
 * @author Tommy Ettinger (if there's a flaw, use SquidLib's issues and don't bother Vigna or Blackman, it's probably a mistake in SquidLib's implementation)
 */
public final class XoRoRNG implements RandomnessSource, Serializable {

	private static final long DOUBLE_MASK = (1L << 53) - 1;
    private static final double NORM_53 = 1. / (1L << 53);
    private static final long FLOAT_MASK = (1L << 24) - 1;
    private static final double NORM_24 = 1. / (1L << 24);

	private static final long serialVersionUID = 1018744536171610262L;

    private long state0, state1;

    /**
     * Creates a new generator seeded using four calls to Math.random().
     */
    public XoRoRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }
    /**
     * Constructs this XoRoRNG by dispersing the bits of seed using {@link #setSeed(long)} across the two parts of state
     * this has.
     * @param seed a long that won't be used exactly, but will affect both components of state
     */
    public XoRoRNG(final long seed) {
        setSeed(seed);
    }
    /**
     * Constructs this XoRoRNG by calling {@link #setSeed(long, long)} on the arguments as given; see that method for 
     * the specific details (stateA and stateB are kept as-is unless they are both 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     */
    public XoRoRNG(final long stateA, final long stateB) {
        setSeed(stateA, stateB);
    }

    @Override
    public final int next(int bits) {
        final long s0 = state0;
        long s1 = state1;
        final int result = (int)(s0 + s1) >>> (32 - bits);
        s1 ^= s0;
        state0 = (s0 << 55 | s0 >>> 9) ^ s1 ^ (s1 << 14); // a, b
        state1 = (s1 << 36 | s1 >>> 28); // c
        return result;
    }

    @Override
    public final long nextLong() {
        final long s0 = state0;
        long s1 = state1;
        final long result = s0 + s1;

        s1 ^= s0;
        state0 = (s0 << 55 | s0 >>> 9) ^ s1 ^ (s1 << 14); // a, b
        state1 = (s1 << 36 | s1 >>> 28); // c
        /*
        state0 = Long.rotateLeft(s0, 55) ^ s1 ^ (s1 << 14); // a, b
        state1 = Long.rotateLeft(s1, 36); // c
        */
        return result;
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public XoRoRNG copy() {
        XoRoRNG next = new XoRoRNG(state0);
        next.state0 = state0;
        next.state1 = state1;
        return next;
    }


    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public int nextInt() {
        return (int)nextLong();
    }

    /**
     * Exclusive on the outer bound; the inner bound is 0. The bound may be negative, which will produce a non-positive
     * result.
     * @param bound the outer exclusive bound; may be positive or negative
     * @return a random int between 0 (inclusive) and bound (exclusive)
     */
    public int nextInt(final int bound) {
        return (int) ((bound * (nextLong() >>> 33)) >> 31);
    }
    /**
     * Inclusive lower, exclusive upper.
     * @param inner the inner bound, inclusive, can be positive or negative
     * @param outer the outer bound, exclusive, should be positive, should usually be greater than inner
     * @return a random int that may be equal to inner and will otherwise be between inner and outer
     */
    public int nextInt(final int inner, final int outer) {
        return inner + nextInt(outer - inner);
    }

    /**
     * Exclusive on the outer bound; the inner bound is 0. The bound may be negative, which will produce a non-positive
     * result.
     * @param bound the outer exclusive bound; may be positive or negative
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    public long nextLong(long bound) {
        long rand = nextLong();
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>= 32;
        final long z = (randLow * boundLow >> 32);
        long t = rand * boundLow + z;
        final long tLow = t & 0xFFFFFFFFL;
        t >>>= 32;
        return rand * bound + t + (tLow + randLow * bound >> 32) - (z >> 63) - (bound >> 63);
    }
    /**
     * Inclusive inner, exclusive outer; both inner and outer can be positive or negative.
     * @param inner the inner bound, inclusive, can be positive or negative
     * @param outer the outer bound, exclusive, can be positive or negative and may be greater than or less than inner
     * @return a random long that may be equal to inner and will otherwise be between inner and outer
     */
    public long nextLong(final long inner, final long outer) {
        return inner + nextLong(outer - inner);
    }

    public double nextDouble() {
        return (nextLong() & DOUBLE_MASK) * NORM_53;
    }

    public float nextFloat() {
        return (float) ((nextLong() & FLOAT_MASK) * NORM_24);
    }

    public boolean nextBoolean() {
        return nextLong() < 0L;
    }

    public void nextBytes(final byte[] bytes) {
        int i = bytes.length, n = 0;
        while (i != 0) {
            n = Math.min(i, 8);
            for (long bits = nextLong(); n-- != 0; bits >>>= 8) {
                bytes[--i] = (byte) bits;
            }
        }
    }

    /**
     * Sets the seed of this generator using one long, running that through LightRNG's algorithm twice to get the state.
     * @param seed the number to use as the seed
     */
    public void setSeed(final long seed) {

        long state = seed + 0x9E3779B97F4A7C15L,
                z = state;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        state0 = z ^ (z >>> 31);
        state += 0x9E3779B97F4A7C15L;
        z = state;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        state1 = z ^ (z >>> 31);
    }

    /**
     * Sets the seed of this generator using two longs, using them without changes unless both are 0 (then it makes the
     * state variable corresponding to stateA 1 instead).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     */
    public void setSeed(final long stateA, final long stateB) {

        state0 = stateA;
        state1 = stateB;
        if((stateA | stateB) == 0L)
            state0 = 1L;
    }

    /**
     * Gets the first component of this generator's two-part state, as a long. This can be 0 on its own, but will never
     * be 0 at the same time as the other component of state, {@link #getStateB()}. You can set the state with two exact
     * values using {@link #setSeed(long, long)}, but the alternative overload {@link #setSeed(long)} won't use the
     * state without changing it (it needs to cover 128 bits with a 64-bit value).
     * @return the first component of this generator's state
     */
    public long getStateA()
    {
        return state0;
    }
    /**
     * Gets the second component of this generator's two-part state, as a long. This can be 0 on its own, but will never
     * be 0 at the same time as the other component of state, {@link #getStateA()}. You can set the state with two exact
     * values using {@link #setSeed(long, long)}, but the alternative overload {@link #setSeed(long)} won't use the
     * state without changing it (it needs to cover 128 bits with a 64-bit value).
     * @return the second component of this generator's state
     */
    public long getStateB()
    {
        return state1;
    }

    @Override
    public String toString() {
        return "XoRoRNG with stateA 0x" + StringKit.hex(state0) + "L and stateB 0x" + StringKit.hex(state1) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XoRoRNG xoRoRNG = (XoRoRNG) o;

        if (state0 != xoRoRNG.state0) return false;
        return state1 == xoRoRNG.state1;
    }

    @Override
    public int hashCode() {
        return (int) (31L * (state0 ^ (state0 >>> 32)) + (state1 ^ (state1 >>> 32)));
    }
}
