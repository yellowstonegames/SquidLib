/*  Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)

To the extent possible under law, the author has dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

See <http://creativecommons.org/publicdomain/zero/1.0/>. */
package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A modification of Blackman and Vigna's xoroshiro64** generator; uses two 32-bit ints of state like {@link Lathe32RNG}
 * but has better equidistribution. Starfish is 2-dimensionally equidistributed, so it can return all long values except
 * for one, while Lathe is 1-dimensionally equidistributed so it can return all int values but not all longs. Starfish
 * passes all 32TB of PractRand's statistical tests, and does so with no anomalies and no failures (the best possible
 * outcome). It also passes at least one seed of TestU01's BigCrush in both forward and reverse with no failures. In
 * statistical testing, xoroshiro128+ always fails some binary matrix rank tests, but that uses a pair of 64-bit states,
 * and when the states are reduced to 32-bits, these small-word versions fail other tests as well. Starfish uses a
 * simpler variant on xoroshiro64** that reduces some issues with that generator and trades them for better-understood
 * issues. Starfish does not change xoroshiro's well-tested state transition, but it doesn't base the output on the sum
 * of the two states (like xoroshiro128+), instead using the first state only for output (exactly like xoroshiro64** and
 * similar to xoshiro256**). Any arithmetic it performs is safe for GWT. Starfish adds an extremely small amount of
 * extra code to xoroshiro, running xoroshiro's state transition as normal, using stateA (or s[0] in the original
 * xoroshiro code) multiplied by 31 as the initial result, then bitwise-rotating that initial result by 28 and adding a
 * constant that is close to 2 to the 32 times the golden ratio, specifically {@code 0x9E3779BD}. Although no bits of
 * xoroshiro are truly free of artifacts, some are harder to find issues with
 * (see <a href="http://www.pcg-random.org/posts/xoroshiro-fails-truncated.html">this article by PCG-Random's author</a>
 * for more detail). It is unclear if the changes made here would improve the larger-state version, but they probably
 * would help to some extent with at least the binary rank failures. The period is identical to xoroshiro with two
 * 32-bit states, at 0xFFFFFFFFFFFFFFFF or 2 to the 64 minus 1. This generator is a little slower than xoroshiro64+ or
 * Lathe, but has better distribution than either. It is equivalent to the algorithm used in {@link GWTRNG}.
 * <br>
 * This avoids an issue in xoroshiro** generators where many multipliers, when applied to the output of a xoroshiro**
 * generator, will cause the modified output to rapidly fail binary matrix rank tests. It has its own issue where
 * subtracting {@code 0x9E3779BD} or a number with a low Hamming distance from {@code 0x9E3779BD} from every output will
 * cause similar binary matrix rank failures. It should be clear that this is not a cryptographic generator, but I am
 * not claiming this is a rock-solid or all-purpose generator either; if a hostile user is trying to subvert a Starfish
 * generator and can access full outputs, it is a cakewalk to find or create issues.
 * <br>
 * The name comes from the single Star operation used (relative to the StarStar scrambler) and the addition of the
 * golden ratio, or phi, which sounds close to fish.
 * <br>
 * <a href="http://xoshiro.di.unimi.it/xoroshiro64starstar.c">Original version here for xoroshiro64**</a>.
 * <br>
 * Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)
 * Ported and modified in 2018 by Tommy Ettinger
 * @author Sebastiano Vigna
 * @author David Blackman
 * @author Tommy Ettinger (if there's a flaw, use SquidLib's or Sarong's issues and don't bother Vigna or Blackman, it's probably a mistake in SquidLib's implementation)
 */
public final class Starfish32RNG implements StatefulRandomness, Serializable {

    private static final long serialVersionUID = 1L;

    private int stateA, stateB;

    /**
     * Creates a new generator seeded using two calls to Math.random().
     */
    public Starfish32RNG() {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    /**
     * Constructs this Lathe32RNG by dispersing the bits of seed using {@link #setSeed(int)} across the two parts of state
     * this has.
     * @param seed an int that won't be used exactly, but will affect both components of state
     */
    public Starfish32RNG(final int seed) {
        setSeed(seed);
    }
    /**
     * Constructs this Lathe32RNG by splitting the given seed across the two parts of state this has with
     * {@link #setState(long)}.
     * @param seed a long that will be split across both components of state
     */
    public Starfish32RNG(final long seed) {
        setState(seed);
    }
    /**
     * Constructs this Lathe32RNG by calling {@link #setState(int, int)} on stateA and stateB as given; see that method
     * for the specific details (stateA and stateB are kept as-is unless they are both 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     */
    public Starfish32RNG(final int stateA, final int stateB) {
        setState(stateA, stateB);
    }
    
    @Override
    public final int next(int bits) {
        final int s0 = stateA;
        final int s1 = stateB ^ s0;
        final int result = s0 * 31;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return (result << 28 | result >>> 4) + 0x9E3779BD >>> (32 - bits);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        final int s0 = stateA;
        final int s1 = stateB ^ s0;
        final int result = s0 * 31;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return (result << 28 | result >>> 4) + 0x9E3779BD;
    }

    @Override
    public final long nextLong() {
        int s0 = stateA;
        int s1 = stateB ^ s0;
        final int high = s0 * 31;
        s0 = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        s1 = (s1 << 13 | s1 >>> 19) ^ s0;
        final int low = s0 * 31;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        final long result = ((high << 28 | high >>> 4) + 0x9E3779BD);
        return result << 32 ^ ((low << 28 | low >>> 4) + 0x9E3779BD);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public Starfish32RNG copy() {
        return new Starfish32RNG(stateA, stateB);
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
        return (stateA & 0xFFFFFFFFL) | ((long)stateB) << 32;
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
        return "Starfish32RNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Starfish32RNG starfish32RNG = (Starfish32RNG) o;

        if (stateA != starfish32RNG.stateA) return false;
        return stateB == starfish32RNG.stateB;
    }

    @Override
    public int hashCode() {
        return 31 * stateA + stateB;
    }
}
