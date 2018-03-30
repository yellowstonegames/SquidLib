/*  Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */

package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;


/**
 * A modification of Blackman and Vigna's xoroshiro128+ generator using two 32-bit ints of state instead of two 64-bit
 * longs and also incorporating a large-increment counter (Weyl sequence) that is added to the rotated xoroshiro output;
 * this is the fastest generator on GWT I have found that also passes the full 32TB battery of PractRand's
 * statistical tests while still producing all ints over its period. ThrustAlt32RNG is faster and also passes tests, but
 * cannot produce all outputs, and its period is too small for heavy usage. In statistical testing, xoroshiro always
 * fails some binary matrix rank tests, but smaller-state versions fail other tests as well. The changes Oriole makes
 * apply only to the output of xoroshiro, not its well-tested state transition for the "xoroshiro state" part of this
 * generator, and these changes eliminate all statistical failures on 32TB of tested data, avoiding the failures the
 * small-state variant of xoroshiro suffers on BCFN, DC6, and FPF. It avoids multiplication, like xoroshiro and much of
 * the xorshift family of generators, and any arithmetic it performs is safe for GWT. Oriole takes advantage of
 * xoroshiro's issues being mostly confined to its  output's lower bits by rotating the output of xoroshiro (the weaker
 * 32-bit-state version) and adding a "Weyl sequence," essentially a large-increment counter that overflows and wraps
 * frequently, to the output. Although the upper bits of xoroshiro are not free of artifacts either, they are harder to
 * find issues with (see
 * <a href="http://www.pcg-random.org/posts/xoroshiro-fails-truncated.html">this article by PCG-Random's author</a> for
 * more detail). It is unclear if the changes made here would improve the larger-state version, but they probably would
 * help to some extent with at least the binary rank failures. The period is also improved by incorporating the Weyl
 * sequence, up to 0xFFFFFFFFFFFFFFFF00000000 .
 * <br>
 * The name comes from the insectivorous orange songbird called an oriole, which as I have found from the one that
 * visits my backyard, reacts quickly and is always looking for bugs to remove. It also sounds like "xoro."
 * <br>
 * <a href="http://xoroshiro.di.unimi.it/xoroshiro128plus.c">Original version here for xorshiro128+</a>; this version
 * uses <a href="https://groups.google.com/d/msg/prng/Ll-KDIbpO8k/bfHK4FlUCwAJ">different constants</a> by the same
 * author, Sebastiano Vigna.
 * <br>
 * Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)
 * Ported and modified in 2018 by Tommy Ettinger
 * @author Sebastiano Vigna
 * @author David Blackman
 * @author Tommy Ettinger (if there's a flaw, use SquidLib's or Sarong's issues and don't bother Vigna or Blackman, it's probably a mistake in SquidLib's implementation)
 */
public final class Oriole32RNG implements RandomnessSource, Serializable {

    private static final long serialVersionUID = 1L;

    private int stateA, stateB, stateC;

    /**
     * Creates a new generator seeded using three calls to Math.random().
     */
    public Oriole32RNG() {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000),
                (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    /**
     * Constructs this Oriole32RNG by dispersing the bits of seed using {@link #setSeed(int)} across the two parts of state
     * this has.
     * @param seed a long that won't be used exactly, but will affect both components of state
     */
    public Oriole32RNG(final int seed) {
        setSeed(seed);
    }
    /**
     * Constructs this Oriole32RNG by calling {@link #setState(int, int, int)} on stateA and stateB as given but
     * producing stateC via {@code stateA ^ stateB}; see that method for the specific details (stateA and stateB are
     * kept as-is unless they are both 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     */
    public Oriole32RNG(final int stateA, final int stateB) {
        setState(stateA, stateB, stateA ^ stateB);
    }

    /**
     * Constructs this Oriole32RNG by calling {@link #setState(int, int, int)} on the arguments as given; see that
     * method for the specific details (stateA and stateB are kept as-is unless they are both 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     * @param stateC the number to use as the counter part of the state (third part)
     */
    public Oriole32RNG(final int stateA, final int stateB, final int stateC) {
        setState(stateA, stateB, stateC);
    }

    @Override
    public final int next(int bits) {
        final int s0 = stateA;
        int s1 = stateB;
        final int result = s0 + s1 | 0;
        s1 ^= s0;
        stateA = (s0 << 13 | s0 >>> 19) ^ s1 ^ (s1 << 5); // a, b
        stateB = (s1 << 28 | s1 >>> 4); // c
        return (result << 29 | result >>> 3) + (stateC = stateC + 0x632BE5AB | 0) >>> (32 - bits);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        final int s0 = stateA;
        int s1 = stateB;
        final int result = s0 + s1 | 0;
        s1 ^= s0;
        stateA = (s0 << 13 | s0 >>> 19) ^ s1 ^ (s1 << 5); // a, b
        stateB = (s1 << 28 | s1 >>> 4); // c
        return (result << 29 | result >>> 3) + (stateC = stateC + 0x632BE5AB | 0) | 0;
    }

    @Override
    public final long nextLong() {
        final int s0 = stateA;
        int s1 = stateB;
        final int high = s0 + s1 | 0;
        s1 ^= s0;
        final int s00 = (s0 << 13 | s0 >>> 19) ^ s1 ^ (s1 << 5); // a, b
        s1 = (s1 << 28 | s1 >>> 4); // c
        final int low = s00 + s1 | 0;
        s1 ^= s00;
        stateA = (s00 << 13 | s00 >>> 19) ^ s1 ^ (s1 << 5); // a, b
        stateB = (s1 << 28 | s1 >>> 4); // c
        return (long)((high << 29 | high >>> 3) + (stateC + 0x632BE5AB | 0) | 0) << 32 ^ ((low << 29 | low >>> 3) + (stateC = stateC + 0xC657CB56 | 0) | 0);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public Oriole32RNG copy() {
        return new Oriole32RNG(stateA, stateB, stateC);
    }

    /**
     * Sets the state of this generator using one int, running it through Zog32RNG's algorithm three times to get 
     * three ints.
     * @param seed the int to use to assign this generator's state
     */
    public void setSeed(final int seed) {
        int z = seed + 0xC74EAD55 | 0, a = seed ^ z;
        a ^= a >>> 14;
        z = (z ^ z >>> 10) * 0xA5CB3 | 0;
        a ^= a >>> 15;
        stateA = (z ^ z >>> 20) + (a ^= a << 13) | 0;
        z = seed + 0x8E9D5AAA | 0;
        a ^= a >>> 14;
        z = (z ^ z >>> 10) * 0xA5CB3 | 0;
        a ^= a >>> 15;
        stateB = (z ^ z >>> 20) + (a ^ a << 13) | 0;
        if((stateA | stateB) == 0)
            stateA = 1;
        z = seed - 0xC74EAD55 | 0;
        a ^= a >>> 14;
        z = (z ^ z >>> 10) * 0xA5CB3 | 0;
        a ^= a >>> 15;
        stateC = (z ^ z >>> 20) + (a ^ a << 13) | 0;
    }

    public int getStateA()
    {
        return stateA;
    }
    public void setStateA(int stateA)
    {
        this.stateA = (stateA | stateB) == 0 ? 1 : stateA;
    }
    public int getStateB()
    {
        return stateB;
    }
    public void setStateB(int stateB)
    {
        this.stateB = stateB;
        if((stateB | stateA) == 0) stateA = 1;
    }
    public int getStateC()
    {
        return stateC;
    }
    public void setStateC(int stateC)
    {
        this.stateC = stateC;
    }

    /**
     * Sets the current internal state of this Oriole32RNG with three ints, where stateA and stateB can each be any int
     * unless they are both 0, and stateC can be any int without restrictions.
     * @param stateA any int except 0 (0 will be treated as 1 instead)
     * @param stateB any int
     */
    public void setState(int stateA, int stateB, int stateC)
    {
        this.stateA = (stateA | stateB) == 0 ? 1 : stateA;
        this.stateB = stateB;
        this.stateC = stateC;
    }
    @Override
    public String toString() {
        return "Oriole32RNG with stateA 0x" + StringKit.hex(stateA) + ", stateB 0x" + StringKit.hex(stateB) + ", and stateC 0x" + StringKit.hex(stateC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Oriole32RNG oriole32RNG = (Oriole32RNG) o;

        if (stateA != oriole32RNG.stateA) return false;
        return stateB == oriole32RNG.stateB;
    }

    @Override
    public int hashCode() {
        return 31 * stateA + stateB | 0;
    }
}
