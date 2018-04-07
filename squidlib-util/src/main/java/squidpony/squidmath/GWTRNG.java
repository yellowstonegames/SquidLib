package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * An IRNG implementation that is meant to provide random numbers very quickly when targeting GWT but also to produce
 * the same numbers when used on desktop, Android, or other platforms, and that can have its state read as a
 * StatefulRandomness. This uses the same algorithm as {@link Lathe32RNG}, which means it has two 32-bit ints for state
 * and a period of 0xFFFFFFFFFFFFFFFF (2 to the 64 minus 1), while passing 32TB of PractRand tests (so its quality is
 * very good). Unlike {@link RNG}, there is no RandomnessSource that can be swapped out, but also somewhat less
 * indirection on common calls like {@link #nextInt()} and {@link #nextFloat()}. Although this implements
 * {@link StatefulRandomness}, it is not recommended to use this as the RandomnessSource for a StatefulRNG; you should
 * use {@link Lathe32RNG} if you want the larger API provided by StatefulRNG and/or RNG while keeping similar, though
 * probably slightly weaker, GWT performance relative to this class.
 * <br>
 * <a href="http://xoroshiro.di.unimi.it/xoroshiro128plus.c">Original version here for xorshiro128+</a>; this version
 * uses <a href="https://groups.google.com/d/msg/prng/Ll-KDIbpO8k/bfHK4FlUCwAJ">different constants</a> by the same
 * author, Sebastiano Vigna.
 * <br>
 * Written in 2016 by David Blackman and Sebastiano Vigna (vigna@acm.org)
 * Ported and modified in 2018 by Tommy Ettinger
 * @author Sebastiano Vigna
 * @author David Blackman
 * @author Tommy Ettinger (if there's a flaw, use SquidLib's issues and don't bother Vigna or Blackman, the algorithm here has been adjusted from their work)
 */
public final class GWTRNG extends AbstractRNG implements StatefulRandomness, Serializable {
    private static final long serialVersionUID = 1L;

    private int stateA, stateB;

    /**
     * Creates a new generator seeded using two calls to Math.random().
     */
    public GWTRNG()
    {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    /**
     * Constructs this GWTRNG by dispersing the bits of seed using {@link #setSeed(int)} across the two parts of state
     * this has.
     * @param seed an int that won't be used exactly, but will affect both components of state
     */
    public GWTRNG(final int seed) {
        setSeed(seed);
    }
    /**
     * Constructs this GWTRNG by splitting the given seed across the two parts of state this has with
     * {@link #setState(long)}.
     * @param seed a long that will be split across both components of state
     */
    public GWTRNG(final long seed) {
        setState(seed);
    }
    /**
     * Constructs this GWTRNG by calling {@link #setState(int, int)} on stateA and stateB as given; see that method
     * for the specific details (stateA and stateB are kept as-is unless they are both 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     */
    public GWTRNG(final int stateA, final int stateB) {
        setState(stateA, stateB);
    }

    /**
     * Get up to 32 bits (inclusive) of random output; the int this produces
     * will not require more than {@code bits} bits to represent.
     *
     * @param bits an int between 1 and 32, both inclusive
     * @return a random number that fits in the specified number of bits
     */
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
     * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both inclusive).
     *
     * @return a 32-bit random int.
     */
    @Override
    public final int nextInt() {
        final int s0 = stateA;
        int s1 = stateB;
        final int result = s0 + s1;
        s1 ^= s0;
        stateA = (s0 << 13 | s0 >>> 19) ^ s1 ^ (s1 << 5); // a, b
        stateB = (s1 << 28 | s1 >>> 4); // c
        return (result << 10 | result >>> 22) + s0;
    }

    /**
     * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive).
     *
     * @return a 64-bit random long.
     */
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
     * Get a random bit of state, interpreted as true or false with approximately equal likelihood.
     * This implementation uses a sign check as a safeguard, since its algorithm is based on (but is not equivalent to)
     * xoroshiro, which recommends a sign check instead of using the least significant bit.
     *
     * @return a random boolean.
     */
    @Override
    public final boolean nextBoolean() {
        final int s0 = stateA;
        int s1 = stateB;
        final int result = s0 + s1;
        s1 ^= s0;
        stateA = (s0 << 13 | s0 >>> 19) ^ s1 ^ (s1 << 5); // a, b
        stateB = (s1 << 28 | s1 >>> 4); // c
        return (result << 10 | result >>> 22) + s0 < 0;
    }

    /**
     * Gets a random double between 0.0 inclusive and 1.0 exclusive.
     * This returns a maximum of 0.9999999999999999 because that is the largest double value that is less than 1.0 .
     * <br>
     * This is abstract because some generators may natively work with double or float values, but others may need to
     * convert a long to a double as with {@code (nextLong() & 0x1fffffffffffffL) * 0x1p-53}, which is recommended if
     * longs are fast to produce.
     *
     * @return a double between 0.0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    @Override
    public final double nextDouble() {
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
        return (((long)((high << 10 | high >>> 22) + s0) << 32 ^ ((low << 10 | low >>> 22) + s00))
                & 0x1fffffffffffffL) * 0x1p-53;
    }

    /**
     * Gets a random float between 0.0f inclusive and 1.0f exclusive.
     * This returns a maximum of 0.99999994 because that is the largest float value that is less than 1.0f .
     * <br>
     * This is abstract because some generators may natively work with double or float values, but others may need to
     * convert an int or long to a float as with {@code (nextInt() & 0xffffff) * 0x1p-24f},
     * {@code (nextLong() & 0xffffffL) * 0x1p-24f}, or {@code next(24) * 0x1p-24f}, any of which can work when the
     * method they call is high-quality and fast. You probably would want to use nextInt() or next() if your
     * implementation is natively 32-bit and is slower at producing longs, for example.
     *
     * @return a float between 0f (inclusive) and 0.99999994f (inclusive)
     */
    @Override
    public final float nextFloat() {
        final int s0 = stateA;
        int s1 = stateB;
        final int result = s0 + s1;
        s1 ^= s0;
        stateA = (s0 << 13 | s0 >>> 19) ^ s1 ^ (s1 << 5);
        stateB = (s1 << 28 | s1 >>> 4);
        return ((result << 10 | result >>> 22) + s0 & 0xffffff) * 0x1p-24f;
    }

    /**
     * Creates a copy of this IRNG; it will generate the same random numbers, given the same calls in order, as this
     * IRNG at the point copy() is called. The copy will not share references with this IRNG. This implementation will
     * faithfully reproduce this generator as a copied GWTRNG.
     * 
     * @return a copy of this IRNG
     */
    @Override
    public GWTRNG copy() {
        return new GWTRNG(stateA, stateB);
    }

    /**
     * Gets a view of this IRNG in a way that implements {@link Serializable}, which is simply this IRNG.
     * @return a {@link Serializable} view of this IRNG or a similar one; always {@code this}
     */
    @Override
    public Serializable toSerializable() {
        return this;
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
     * Sets the current internal state of this GWTRNG with three ints, where stateA and stateB can each be any int
     * unless they are both 0 (which will be treated as if stateA is 1 and stateB is 0).
     * @param stateA any int (if stateA and stateB are both 0, this will be treated as 1)
     * @param stateB any int
     */
    public void setState(int stateA, int stateB)
    {
        this.stateA = stateA == 0 && stateB == 0 ? 1 : stateA;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GWTRNG gwtrng = (GWTRNG) o;

        if (stateA != gwtrng.stateA) return false;
        return stateB == gwtrng.stateB;
    }

    @Override
    public int hashCode() {
        return 31 * stateA + stateB;
    }
    
    @Override
    public String toString() {
        return "GWTRNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }

}
