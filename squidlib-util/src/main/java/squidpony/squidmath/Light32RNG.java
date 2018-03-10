package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * Based on the same SplitMix algorithm {@link LightRNG} uses, but a 32-bit variant, this RandomnessSource has 63 bits
 * of state and should have a period of 2 to the 63. It has 2 ints for state, one of which changes with every number
 * generated (state A), and the other of which changes rarely and is always odd (state B). State A goes through every
 * number in a simple sequence that would normally repeat after 2 to the 32 generations, and always will be 0 once every
 * 2 to the 32 generations; the sequence is formed by just adding state B to state A. When state A changes to 0, state B
 * changes by adding a large even constant and that is used for the rest of the next 2 to the 32 generations. Although
 * this uses a ternary conditional to determine when to change state B, the branch on which state B changes happens so
 * rarely that processor branch prediction can almost optimize it out, and this is just slightly slower than LightRNG on
 * 32-bit int generation. Quality is good here, and this passes PractRand without failures on 64 MB of random values.
 * Unlike LightRNG, it does not provide a skip() method.
 * Created by Tommy Ettinger on 7/15/2017.
 */
public class Light32RNG implements StatefulRandomness, RandomnessSource, Serializable {
    private int state, inc;
    private static final long serialVersionUID = -374415589203474497L;

    /**
     * Constructs a Light32RNG with a random state, using two calls to Math.random().
     */
    public Light32RNG()
    {
        this((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    /**
     * Constructs a Light32RNG with the exact state A given and a similar state B (the least significant bit of state B
     * will always be 1 internally, so even values for state B will be incremented and odd values will be kept as-is).
     * @param stateA any int
     * @param stateB any int, but the last bit will not be used (e.g. 20 and 21 will be treated the same)
     */
    public Light32RNG(int stateA, int stateB)
    {
        state = stateA;
        inc = stateB | 1;
    }

    /**
     * Takes 32 bits of state and uses it to randomly fill the 63 bits of state this uses.
     * @param statePart any int
     */
    public Light32RNG(int statePart)
    {
        state = determine(statePart + 19) + statePart;
        inc = determine(state + statePart) | 1;
    }

    /**
     * Constructs a Light32RNG using a long that combines the two parts of state, as from {@link #getState()}.
     * @param stateCombined a long that combines state A and state B, with state A in the less significant 32 bits
     */
    public Light32RNG(long stateCombined)
    {
        state = (int)stateCombined;
        inc = (int)(stateCombined >>> 32 | 1);
    }

    public final int nextInt() {
        int z = (state += (state == 0) ? (inc += 0x632BE5A6) : inc);
        z = (z ^ (z >>> 16)) * 0x85EBCA6B;
        z = (z ^ (z >>> 13)) * 0xC2B2AE35;
        return z ^ (z >>> 16);
    }

    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public final int next(int bits) {
        int z = (state += (state == 0) ? (inc += 0x632BE5A6) : inc);
        z = (z ^ (z >>> 16)) * 0x85EBCA6B;
        z = (z ^ (z >>> 13)) * 0xC2B2AE35;
        return (z ^ (z >>> 16)) >>> (32 - bits);
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
        int y = (state += (state == 0) ? (inc += 0x632BE5A6) : inc),
                z = (state += (state == 0) ? (inc += 0x632BE5A6) : inc);
        y = (y ^ (y >>> 16)) * 0x85EBCA6B;
        z = (z ^ (z >>> 16)) * 0x85EBCA6B;
        y = (y ^ (y >>> 13)) * 0xC2B2AE35;
        z = (z ^ (z >>> 13)) * 0xC2B2AE35;
        return (long)(y ^ (y >>> 16)) << 32 ^ (z ^ (z >>> 16));
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
        return new Light32RNG(state, inc);
    }

    public int getStateA()
    {
        return state;
    }
    public void setStateA(int stateA)
    {
        state = stateA;
    }
    public int getStateB()
    {
        return inc;
    }
    public void setStateB(int stateB)
    {
        inc = stateB | 1;
    }

    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    @Override
    public long getState() {
        return (long)inc << 32 | (state & 0xFFFFFFFFL);
    }

    /**
     * Set the current internal state of this StatefulRandomness with a long.
     * This implementation may not use the state verbatim, since only 63 bits are actually used as state here.
     * Specifically, the bit in state that would be masked by 0x0000000100000000L is unused; it is always 1 internally.
     * @param state a 64-bit long. You should avoid passing 0, even though some implementations can handle that.
     */
    @Override
    public void setState(long state) {
        this.state = (int)(state & 0xFFFFFFFFL);
        inc = (int) (state >>> 32) | 1;
    }

    /**
     * Sets the current internal state of this Light32RNG with two ints.
     * The least significant bit of stateB is unused; it is always 1 internally.
     * @param stateA any int
     * @param stateB any int, but the last bit will not be used (e.g. 20 and 21 will be treated the same)
     */
    public void setState(int stateA, int stateB)
    {
        state = stateA;
        inc = stateB | 1;
    }

    @Override
    public String toString() {
        return "Light32RNG with stateA 0x" + StringKit.hex(state) + " and stateB 0x" + StringKit.hex(inc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Light32RNG that = (Light32RNG) o;

        return state == that.state && inc == that.inc;
    }

    @Override
    public int hashCode() {
        int result = state;
        result = 31 * result + inc;
        return result;
    }

    /**
     * Gets a pseudo-random int from the given state as an int; the state should change with each call.
     * This can be done easily with {@code determine(++state)} or {@code determine(state += 12345)}, where 12345
     * can be any odd number (it should stay the same across calls that should have random-seeming results). Uses the
     * same algorithm as Light32RNG, but does not change the increment on its own (it leaves that to the user).
     * @param state should be changed with each call; {@code determine(++state)} will work fine
     * @return a pseudo-random int
     */
    public static int determine(int state)
    {
        return (state = ((state = ((state *= 0x9E3779B9) ^ (state >>> 16)) * 0x85EBCA6B) ^ (state >>> 13)) * 0xC2B2AE35) ^ (state >>> 16);
    }
    /**
     * Gets a pseudo-random int between 0 and the given bound, using the given state as a basis, as an int; the state
     * should change with each call. This can be done easily with {@code determineBounded(++state, bound)} or
     * {@code determineBounded(state += 12345, bound)}, where 12345 can be any odd number (it should stay the same
     * across calls that should have random-seeming results). The bound can be any int, but more significant bounds
     * won't usually work very well. Uses the same algorithm as Light32RNG, but does not change the increment on its own
     * (it leaves that to the user). This does use math on long values, but not very much; if you know that bound is
     * fairly small (small enough to fit in a short) you can use {@link #determineSmallBounded(int, int)} to avoid
     * operations on longs entirely.
     * @param state should be changed with each call; {@code determineBounded(++state, bound)} will work fine
     * @param bound the outer exclusive limit on the random number; can be any int
     * @return a pseudo-random int, between 0 (inclusive) and bound (exclusive)
     */
    public static int determineBounded(int state, int bound)
    {
        return (int)((bound * (((state = ((state = ((state *= 0x9E3779B9) ^ (state >>> 16)) * 0x85EBCA6B) ^ (state >>> 13)) * 0xC2B2AE35) ^ (state >>> 16)) & 0x7FFFFFFFL)) >> 31);
    }
    /**
     * Gets a pseudo-random int between 0 and the given bound, using the given state as a basis, as an int; the state
     * should change with each call. This can be done easily with {@code determineSmallBounded(++state, bound)} or
     * {@code determineSmallBounded(state += 12345, bound)}, where 12345 can be any odd number (it should stay the same
     * across calls that should have random-seeming results). The bound should be between -32768 and 32767 (both
     * inclusive); more significant bounds won't usually work well. Uses the same algorithm as Light32RNG, but does not
     * change the increment on its own (it leaves that to the user). This does not use any math on long values, which
     * means this generator should be faster on 32-bit platforms but may behave incorrectly on GWT.
     * @param state should be changed with each call; {@code determineSmallBounded(++state, bound)} will work fine
     * @param bound the outer exclusive limit on the random number; should be between -32768 and 32767 (both inclusive)
     * @return a pseudo-random int, between 0 (inclusive) and bound (exclusive)
     */
    public static int determineSmallBounded(int state, int bound)
    {
        return ((bound * (((((state = ((state *= 0x9E3779B9) ^ (state >>> 16)) * 0x85EBCA6B) ^ (state >>> 13)) * 0xC2B2AE35) >>> 17) & 0x7FFF)) >> 15);
    }

}
