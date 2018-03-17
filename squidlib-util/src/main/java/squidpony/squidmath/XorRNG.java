/*
Written in 2015 by Sebastiano Vigna (vigna@acm.org)

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package squidpony.squidmath;

import squidpony.StringKit;

/**
 * A port of Sebastiano Vigna's XorShift 128+ generator. Should be very fast and produce high-quality output.
 * Original version at http://xorshift.di.unimi.it/xorshift128plus.c
 * Written in 2015 by Sebastiano Vigna (vigna@acm.org)
 * @author Sebastiano Vigna
 * @author Tommy Ettinger
 */
public class XorRNG implements RandomnessSource {

	private static final long DOUBLE_MASK = (1L << 53) - 1;
    private static final double NORM_53 = 1. / (1L << 53);
    private static final long FLOAT_MASK = (1L << 24) - 1;
    private static final double NORM_24 = 1. / (1L << 24);

	private static final long serialVersionUID = 1263134736171610359L;

    private long state0, state1;

    /**
     * Creates a new generator seeded using four calls to Math.random().
     */
    public XorRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    /**
     * Constructs this XorRNG by dispersing the bits of seed using a unary hash across the two parts of state this has.
     * @param seed a long that won't be used exactly, but will affect both components of state
     */
    public XorRNG(final long seed) {
        setSeed(seed);
    }

    /**
     * Constructs this XorRNG by calling {@link #setSeed(long, long)} on the arguments as given; see that method for the
     * specific details (stateA and stateB are kept as-is unless they are both 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     */
    public XorRNG(final long stateA, final long stateB) {
        setSeed(stateA, stateB);
    }

    @Override
    public final int next(int bits) {
        return (int)nextLong() >>> (32 - bits);
    }

    @Override
    public long nextLong() {
        long s1 = state0;
        final long s0 = state1;
        state0 = s0;
        s1 ^= s1 << 23; // a
        return ( state1 = s1 ^ s0 ^ ( s1 >>> 17 ) ^ ( s0 >>> 26 )) + s0; // b, c
    }

    public int nextInt() {
        return (int) nextLong();
    }

    public int nextInt(final int n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        return (int) ((nextLong() >>> 1) % n);
    }

    public long nextLong(final long n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        for (;;) {
            final long bits = nextLong() >>> 1;
            final long value = bits % n;
            if (bits - value + (n - 1) >= 0) {
                return value;
            }
        }
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

    private long avalanche ( long k )
    {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;

        return k;
    }

    /**
     * Sets the seed of this generator. Passing this 0 will just set it to -1
     * instead.
     *
     * @param seed the number to use as the seed
     */
    public void setSeed(final long seed) {
        state0 = avalanche(seed == 0 ? -1 : seed);
        state1 = avalanche(state0);
        state0 = avalanche(state1);
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
        return "XorRNG with stateA 0x" + StringKit.hex(state0) + "L and stateB 0x" + StringKit.hex(state1) + 'L';
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public XorRNG copy() {
        XorRNG next = new XorRNG(state0);
        next.state0 = state0;
        next.state1 = state1;
        return next;
    }
}
