package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * Like LightRNG, but shares a lot in common with CrossHash's Lightning hash. The name comes from its
 * similarity to Lightning, but also to how it acts like LightRNG except with a thunder-like "echo" where the
 * state affects the result in two places, in different ways. It appears to be the fastest RandomnessSource
 * we have available, but there's no way to be certain of its exact period or potential flaws until a more
 * thorough test, like TestU01, can be run on it.
 * Created by Tommy Ettinger on 8/23/2016.
 */
@Beta
public class ThunderRNG implements StatefulRandomness, RandomnessSource {

    public long state; /* The state can be seeded with any value. */

    /** Creates a new generator seeded using Math.random. */
    public ThunderRNG() {
        this((long) Math.floor(Math.random() * Long.MAX_VALUE));
    }

    public ThunderRNG( final long seed ) {
        setState(seed);
    }

    @Override
    public int next( int bits ) {
        return (int)( nextLong() & ( 1L << bits ) - 1 );
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     * @return any long, all 64 bits are random
     */
    @Override
    public long nextLong() {
        return (state >>> 7) * (((state += 0x9E3779B97F4A7C15L) & -28L) + 0x632BE59BD9B4E019L) + 0xD0E89D2D311E289FL;
    }

    public int nextInt()
    {
        return (int)(nextLong());
    }
    /**
     * This returns a maximum of 0.9999999999999999 because that is the largest
     * Double value that is less than 1.0
     *
     * @return a value between 0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    public double nextDouble() {
        return Double.longBitsToDouble(0x3FFL << 52 | nextLong() >>> 12) - 1.0;
    }

    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    @Override
    public long getState() {
        return state;
    }

    /**
     * Set the current internal state of this StatefulRandomness with a long.
     *
     * @param state a 64-bit long. You should avoid passing 0, even though some implementations can handle that.
     */
    @Override
    public void setState(long state) {
        this.state = state;
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
        return new ThunderRNG(state);
    }
}
