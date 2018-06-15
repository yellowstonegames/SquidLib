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
 * longs, as well as modifying the output with two additional operations on the existing state; this is both the fastest
 * generator on GWT I have found without statistical failures, and a StatefulRandomness. This algorithm is sometimes
 * called xoroshiro64++ and is mentioned in <a href="http://vigna.di.unimi.it/ftp/papers/ScrambledLinear.pdf">this paper
 * by Blackman and Vigna</a> (in section 10.7; the 'r' constant is 10, which seems to usually do well on those same
 * authors' HWD test). Lathe32RNG passes the full 32TB battery of PractRand's statistical tests, and does so with 3
 * "unusual" anomalies, no more-serious anomalies, and no failures. It isn't especially likely that this can pass much
 * more than 32TB of testing (judging by related attempts, 128TB would be a likely failure point), but because
 * multi-threaded code is either impossible or impractical on GWT, actually using that many numbers would take a very
 * long time (generating them would take about 3 nanoseconds per int, but it would take more than 2 to the 43 ints to
 * start to approach detectable failures, and detecting the failures in anything but the worst case would take more than
 * a day). In statistical testing, xoroshiro with the '+' scrambler always fails some binary matrix rank tests, but
 * smaller-state versions fail other tests as well. The changes Lathe makes apply only to the output of xoroshiro64+ (in
 * Vigna's and Blackman's terms, they are a scrambler), not its well-tested state transition, and these changes
 * eliminate all statistical failures on 32TB of tested data, avoiding the failures the small-state variant of xoroshiro
 * suffers on BinaryRank, BCFN, DC6, and FPF. It avoids multiplication (except in {@link #setSeed(int)}, which needs to
 * use a different algorithm to spread a seed out across twice as much state), like xoroshiro and much of the xorshift
 * family of generators, and any arithmetic it performs is safe for GWT. Lathe makes an extremely small set of changes
 * to xoroshiro64+, running xoroshiro64+ as normal (holding on to the result as well as the initial stateA, called s[0]
 * in the original xoroshiro code) and then bitwise-rotating the result and adding the (now previous) stateA. Although
 * no bits of xoroshiro are truly free of artifacts, some are harder to find issues with
 * (see <a href="http://www.pcg-random.org/posts/xoroshiro-fails-truncated.html">this article by PCG-Random's author</a>
 * for more detail). It is unclear if the changes made here would improve the larger-state version, but they probably
 * would help to some extent with at least the binary rank failures. The period is identical to xoroshiro with two
 * 32-bit states, at 0xFFFFFFFFFFFFFFFF or 2 to the 64 minus 1. This generator is slightly slower than xoroshiro without
 * the small extra steps applied to the output, but about as fast as {@link Oriole32RNG} (this has a smaller period and
 * smaller state but implements StatefulRandomness). Some simple tests on bytes instead of ints showed that the
 * technique used here produces all possible bytes with equal frequency when run on bytes as state, with the exception
 * of producing 0 one less time (because both states cannot be 0 at the same time). This gives some confidence for the
 * algorithm used here, but doesn't say anything about how equidistributed this is across more than one dimension (it
 * could be better or worse than xoroshiro128+).
 * <br>
 * The name comes from a tool that rotates very quickly to remove undesirable parts of an object, akin to how this
 * generator adds an extra bitwise rotation to xoroshiro64+ to remove several types of
 * undesirable statistical failures from its test results.
 * <br>
 * <a href="http://xoroshiro.di.unimi.it/xoroshiro128plus.c">Original version here for xorshiro128+</a>; this version
 * uses <a href="https://groups.google.com/d/msg/prng/Ll-KDIbpO8k/bfHK4FlUCwAJ">different constants</a> by the same
 * author, Sebastiano Vigna. It does not use <a href="http://xoshiro.di.unimi.it/xoroshiro64star.c">the constants used
 * in other xoroshiro64 scrambled generators</a>, instead using similar-quality ones from the earlier constants link.
 * <br>
 * Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)
 * Ported and modified in 2018 by Tommy Ettinger
 * @author Sebastiano Vigna
 * @author David Blackman
 * @author Tommy Ettinger (if there's a flaw, use SquidLib's or Sarong's issues and don't bother Vigna or Blackman, it's probably a mistake in SquidLib's implementation)
 */
public final class Lathe32RNG implements StatefulRandomness, Serializable {

    private static final long serialVersionUID = 1L;

    private int stateA, stateB;

    /**
     * Creates a new generator seeded using two calls to Math.random().
     */
    public Lathe32RNG() {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    /**
     * Constructs this Lathe32RNG by dispersing the bits of seed using {@link #setSeed(int)} across the two parts of state
     * this has.
     * @param seed an int that won't be used exactly, but will affect both components of state
     */
    public Lathe32RNG(final int seed) {
        setSeed(seed);
    }
    /**
     * Constructs this Lathe32RNG by splitting the given seed across the two parts of state this has with
     * {@link #setState(long)}.
     * @param seed a long that will be split across both components of state
     */
    public Lathe32RNG(final long seed) {
        setState(seed);
    }
    /**
     * Constructs this Lathe32RNG by calling {@link #setState(int, int)} on stateA and stateB as given; see that method
     * for the specific details (stateA and stateB are kept as-is unless they are both 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     */
    public Lathe32RNG(final int stateA, final int stateB) {
        setState(stateA, stateB);
    }
    
    @Override
    public final int next(int bits) {
        final int s0 = stateA;
        int s1 = stateB;
        final int result = s0 + s1;
        s1 ^= s0;
        stateA = (s0 << 13 | s0 >>> 19) ^ s1 ^ (s1 << 5); // a, b
        stateB = (s1 << 28 | s1 >>> 4); // c
        return (result << 10 | result >>> 22) + s0 >>> (32 - bits);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        final int s0 = stateA;
        int s1 = stateB;
        final int result = s0 + s1;
        s1 ^= s0;
        stateA = (s0 << 13 | s0 >>> 19) ^ s1 ^ (s1 << 5); // a, b
        stateB = (s1 << 28 | s1 >>> 4); // c
        return (result << 10 | result >>> 22) + s0;
    }

    @Override
    public final long nextLong() {
        final int s0 = stateA;
        int s1 = stateB;
        final int high = s0 + s1;
        s1 ^= s0;
        final int s00 = (s0 << 13 | s0 >>> 19) ^ s1 ^ (s1 << 5); // a, b
        s1 = (s1 << 28 | s1 >>> 4); // c
        final int low = s00 + s1;
        s1 ^= s00;
        stateA = (s00 << 13 | s00 >>> 19) ^ s1 ^ (s1 << 5); // a, b
        stateB = (s1 << 28 | s1 >>> 4); // c
        return (long)((high << 10 | high >>> 22) + s0) << 32 ^ ((low << 10 | low >>> 22) + s00);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public Lathe32RNG copy() {
        return new Lathe32RNG(stateA, stateB);
    }

    /**
     * Sets the state of this generator using one int, running it through Zog32RNG's algorithm two times to get 
     * two ints. If the states would both be 0, state A is assigned 1 instead.
     * @param seed the int to use to produce this generator's state
     */
    public void setSeed(final int seed) {
        int z = seed + 0xC74EAD55, a = seed ^ z;
        a ^= a >>> 14;
        z = (z ^ z >>> 10) * 0xA5CB3;
        a ^= a >>> 15;
        stateA = (z ^ z >>> 20) + (a ^= a << 13);
        z = seed + 0x8E9D5AAA;
        a ^= a >>> 14;
        z = (z ^ z >>> 10) * 0xA5CB3;
        a ^= a >>> 15;
        stateB = (z ^ z >>> 20) + (a ^ a << 13);
        if((stateA | stateB) == 0)
            stateA = 1;
    }

    public int getStateA()
    {
        return stateA;
    }
    /**
     * Sets the first part of the state to the given int. As a special case, if the parameter is 0 and stateB is
     * already 0, this will set stateA to 1 instead, since both states cannot be 0 at the same time. Usually, you
     * should use {@link #setState(int, int)} to set both states at once, but the result will be the same if you call
     * setStateA() and then setStateB() or if you call setStateB() and then setStateA().
     * @param stateA any int
     */

    public void setStateA(int stateA)
    {
        this.stateA = (stateA | stateB) == 0 ? 1 : stateA;
    }
    public int getStateB()
    {
        return stateB;
    }

    /**
     * Sets the second part of the state to the given int. As a special case, if the parameter is 0 and stateA is
     * already 0, this will set stateA to 1 and stateB to 0, since both cannot be 0 at the same time. Usually, you
     * should use {@link #setState(int, int)} to set both states at once, but the result will be the same if you call
     * setStateA() and then setStateB() or if you call setStateB() and then setStateA().
     * @param stateB any int
     */
    public void setStateB(int stateB)
    {
        this.stateB = stateB;
        if((stateB | stateA) == 0) stateA = 1;
    }

    /**
     * Sets the current internal state of this Lathe32RNG with three ints, where stateA and stateB can each be any int
     * unless they are both 0 (which will be treated as if stateA is 1 and stateB is 0).
     * @param stateA any int (if stateA and stateB are both 0, this will be treated as 1)
     * @param stateB any int
     */
    public void setState(int stateA, int stateB)
    {
        this.stateA = (stateA | stateB) == 0 ? 1 : stateA;
        this.stateB = stateB;
    }

    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    @Override
    public long getState() {
        return stateA & 0xFFFFFFFFL | ((long)stateB) << 32;
    }

    /**
     * Set the current internal state of this StatefulRandomness with a long.
     *
     * @param state a 64-bit long. You should avoid passing 0; this implementation will treat it as 1.
     */
    @Override
    public void setState(long state) {
        stateA = state == 0 ? 1 : (int)(state & 0xFFFFFFFFL);
        stateB = (int)(state >>> 32);
    }

    @Override
    public String toString() {
        return "Lathe32RNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lathe32RNG lathe32RNG = (Lathe32RNG) o;

        return stateA == lathe32RNG.stateA && stateB == lathe32RNG.stateB;
    }

    @Override
    public int hashCode() {
        return 31 * stateA + stateB;
    }
}
