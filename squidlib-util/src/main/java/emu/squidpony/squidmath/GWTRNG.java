package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

public final class GWTRNG extends AbstractRNG implements IStatefulRNG, Serializable {
    private static final long serialVersionUID = 2L;

    public int stateA, stateB;

    public GWTRNG()
    {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    public GWTRNG(final int seed) {
        setSeed(seed);
    }
    public GWTRNG(final long seed) {
        setState(seed);
    }
    public GWTRNG(final int stateA, final int stateB) {
        setState(stateA, stateB);
    }
    public GWTRNG(final String seed) {
        setState(CrossHash.hash(seed), seed == null ? 1 : seed.hashCode());
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

    @Override
    public final int nextInt() {
        final int s0 = stateA;
        final int s1 = stateB ^ s0;
        final int result = s0 * 31;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return (result << 28 | result >>> 4) + 0x9E3779BD | 0;
    }

    @Override
    public int nextInt(final int bound) {
        final int s0 = stateA;
        final int s1 = stateB ^ s0;
        final int result = s0 * 31;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return (int) ((bound * ((result << 28 | result >>> 4) + 0x9E3779BD & 0xFFFFFFFFL)) >>> 32) & ~(bound >> 31);
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
        return ((high << 28 | high >>> 4) + 0x9E3779BDL) << 32
                | ((low << 28 | low >>> 4) + 0x9E3779BD & 0xFFFFFFFFL);
    }

    @Override
    public final boolean nextBoolean() {
        final int s0 = stateA;
        final int s1 = stateB ^ s0;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return (s0 * 31 & 8) == 8; // same effect as a sign check if this was rotated as normal
    }

    @Override
    public final double nextDouble() {
        int s0 = stateA;
        int s1 = stateB ^ s0;
        final int high = s0 * 31;
        s0 = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        s1 = (s1 << 13 | s1 >>> 19) ^ s0;
        final int low = s0 * 31;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return  ((((high << 28 | high >>> 4) + 0x9E3779BDL) << 32
                | ((low << 28 | low >>> 4) + 0x9E3779BD & 0xFFFFFFFFL))
                & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    @Override
    public final float nextFloat() {
        final int s0 = stateA;
        final int s1 = stateB ^ s0;
        final int result = s0 * 31;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return ((result << 28 | result >>> 4) + 0x9E3779BD & 0xffffff) * 0x1p-24f;
    }

    @Override
    public GWTRNG copy() {
        return new GWTRNG(stateA, stateB);
    }

    @Override
    public Serializable toSerializable() {
        return this;
    }
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
    public void setState(int stateA, int stateB)
    {
        this.stateA = stateA == 0 && stateB == 0 ? 1 : stateA;
        this.stateB = stateB;
    }

    @Override
    public long getState() {
        return stateA & 0xFFFFFFFFL | ((long)stateB) << 32;
    }

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
        return 31 * stateA + stateB | 0;
    }
    
    @Override
    public String toString() {
        return "GWTRNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }
    public static int determineInt(int state)
    {
        return (state = ((state = (state ^ 0xD1B54A35) * 0x102473 | 0) ^ (state << 11 | state >>> 21) ^ (state << 21 | state >>> 11)) * ((state ^ state >>> 15) | 0xFFE00001) + state) ^ state >>> 14;
    }
    public static int determineBounded(int state, final int bound)
    {
        return (int)(bound * (((state = ((state = (state ^ 0xD1B54A35) * 0x102473 | 0) ^ (state << 11 | state >>> 21) ^ (state << 21 | state >>> 11)) * ((state ^ state >>> 15) | 0xFFE00001) + state) ^ state >>> 14) & 0xFFFFFFFFL) >> 32);
    }
    public static long determine(int state)
    {
        final long r = (state = ((state = (state ^ 0xD1B54A35) * 0x102473 | 0) ^ (state << 11 | state >>> 21) ^ (state << 21 | state >>> 11)) * ((state ^ state >>> 15) | 0xFFE00001) + state) ^ state >>> 14;
        return (r << 32) | (((state = ((state = (state ^ 0xD1B54A35) * 0x102473 | 0) ^ (state << 11 | state >>> 21) ^ (state << 21 | state >>> 11)) * ((state ^ state >>> 15) | 0xFFE00001) + state) ^ state >>> 14) & 0xFFFFFFFFL);
    }
    public static float determineFloat(int state)
    {
        return (((state = ((state = (state ^ 0xD1B54A35) * 0x102473 | 0) ^ (state << 11 | state >>> 21) ^ (state << 21 | state >>> 11)) * ((state ^ state >>> 15) | 0xFFE00001) + state) ^ state >>> 14) & 0xFFFFFF) * 0x1p-24f;
    }
    public static double determineDouble(int state)
    {
        return (((state = ((state = (state ^ 0xD1B54A35) * 0x102473 | 0) ^ (state << 11 | state >>> 21) ^ (state << 21 | state >>> 11)) * ((state ^ state >>> 15) | 0xFFE00001) + state) ^ state >>> 14) & 0x7FFFFFFF) * 0x1p-31;
    }
}
