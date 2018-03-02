package squidpony.squidmath;

// This differs from the non-super-sourced version only by including " | 0" after any potentially-overflowing math.
// That's actually relevant on GWT, due to ints simply being JS doubles on that platform, and those don't overflow.

import squidpony.StringKit;
import java.io.Serializable;

public final class ThrustAlt32RNG implements StatefulRandomness, Serializable {
    private static final long serialVersionUID = 3L;
    public int state;

    public ThrustAlt32RNG() {
        this((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    public ThrustAlt32RNG(final int seed) {
        state = seed | 0;
    }

    public ThrustAlt32RNG(final long seed) {
        state = (int)(seed ^ seed >>> 32);
    }
    @Override
    public long getState() {
        return state;
    }

    @Override
    public void setState(long state) {
        this.state = (int)state | 0;
    }

    @Override
    public final int next(final int bits) {
        final int a = (state = state + 0x62BD5 | 0);
        final int z = (a ^ a >>> 13) * ((a & 0xFFFF8) ^ 0xCD7B5) | 0;
        return (((z << 21) | (z >>> 11)) ^ (((z << 7) | (z >>> 25)) * 0x62BD5)) >>> (32 - bits);

    }
    public final int nextInt()
    {
        final int a = (state = state + 0x62BD5 | 0);
        final int z = (a ^ a >>> 13) * ((a & 0xFFFF8) ^ 0xCD7B5) | 0;
        return (((z << 21) | (z >>> 11)) ^ (((z << 7) | (z >>> 25)) * 0x62BD5));
    }

    @Override
    public final long nextLong() {
        final int b = (state = state + 0xC57AA | 0);
        final int a = (b - 0x62BD5 | 0);
        final int y = (a ^ a >>> 13) * ((a & 0xFFFF8) ^ 0xCD7B5) | 0;
        final int z = (b ^ b >>> 13) * ((b & 0xFFFF8) ^ 0xCD7B5) | 0;
        return (long) (((y << 21) | (y >>> 11)) ^ (((y << 7) | (y >>> 25)) * 0x62BD5)) << 32
                | ((((z << 21) | (z >>> 11)) ^ (((z << 7) | (z >>> 25)) * 0x62BD5)) & 0xFFFFFFFFL);
    }

    public final int skip(final int advance) {
        final int a = (state = state + advance * 0x62BD5 | 0);
        final int z = (a ^ a >>> 13) * ((a & 0xFFFF8) ^ 0xCD7B5) | 0;
        return (((z << 21) | (z >>> 11)) ^ (((z << 7) | (z >>> 25)) * 0x62BD5));
    }


    @Override
    public ThrustAlt32RNG copy() {
        return new ThrustAlt32RNG(state);
    }
    @Override
    public String toString() {
        return "ThrustAlt32RNG with state 0x" + StringKit.hex(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThrustAlt32RNG thrustAlt32RNG = (ThrustAlt32RNG) o;

        return state == thrustAlt32RNG.state;
    }

    @Override
    public int hashCode() {
        return state;
    }

    public static int determine(int state) {
        state = ((state *= 0x62BD5) ^ state >>> 13) * ((state & 0xFFFF8) ^ 0xCD7B5);
        return ((state << 21) | (state >>> 11)) ^ (((state << 7) | (state >>> 25)) * 0x62BD5);
    }
    
    /**
     * Be careful calling this with {@code randomize(state += 0x62BD5)} as you would normally on desktop, since state
     * won't overflow (it needs to). You need to use {@code randomize(state = state + 0x62BD5 | 0)} instead.
     * @param state call with {@code randomize(state += 0x62BD5)}
     * @return a pseudo-random long determined by state
     */
    public static int randomize(int state) {
        state = ((state *= 0x62BD5) ^ state >>> 13) * ((state & 0xFFFF8) ^ 0xCD7B5);
        return ((state << 21) | (state >>> 11)) ^ (((state << 7) | (state >>> 25)) * 0x62BD5);

    }

    public static float determineFloat(int state) {
        state = ((state *= 0x62BD5) ^ state >>> 13) * ((state & 0xFFFF8) ^ 0xCD7B5);
        return ((((state << 21) | (state >>> 11)) ^ (((state << 7) | (state >>> 25)) * 0x62BD5)) & 0xFFFFFF) * 0x1p-24f;
    }

    public static int determineBounded(int state, final int bound)
    {
        state = ((state *= 0x62BD5) ^ state >>> 13) * ((state & 0xFFFF8) ^ 0xCD7B5);
        return (int) ((((((state << 21) | (state >>> 11)) ^ (((state << 7) | (state >>> 25)) * 0x62BD5)) & 0xFFFFFFFFL) * bound) >> 32);
    }
}
