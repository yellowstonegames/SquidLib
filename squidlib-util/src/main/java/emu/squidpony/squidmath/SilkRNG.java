package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

public final class SilkRNG extends AbstractRNG implements IStatefulRNG, Serializable {
    private static final long serialVersionUID = 5L;

    public int stateA, stateB;
    
    public SilkRNG()
    {
        setState((int)((Math.random() - 0.5) * 0x1p32), (int)((Math.random() - 0.5) * 0x1p32));
    }
    
    public SilkRNG(final int seed) {
        setSeed(seed);
    }
    
    public SilkRNG(final long seed) {
        setState(seed);
    }
    
    public SilkRNG(final int stateA, final int stateB) {
        setState(stateA, stateB);
    }

    public SilkRNG(final String seed) {
        setState(CrossHash.hash(seed), seed == null ? 0 : seed.hashCode());
    }

    @Override
    public final int next(int bits) {
        final int s = (stateA = stateA + 0xC1C64E6D | 0);
        int x = (s ^ s >>> 17) * ((stateB = stateB + ((s | -s) >> 31 & 0x9E3779BB) | 0) >>> 12 | 1);
        x = (x ^ x >>> 16) * 0xAC451;
        return (x ^ x >>> 15) >>> (32 - bits);
    }

    @Override
    public final int nextInt() {
        final int s = (stateA = stateA + 0xC1C64E6D | 0);
        int x = (s ^ s >>> 17) * ((stateB = stateB + ((s | -s) >> 31 & 0x9E3779BB) | 0) >>> 12 | 1);
        x = (x ^ x >>> 16) * 0xAC451;
        return (x ^ x >>> 15);
    }
    
    @Override
    public final int nextInt(final int bound) {
        final int s = (stateA = stateA + 0xC1C64E6D | 0);
        int x = (s ^ s >>> 17) * ((stateB = stateB + ((s | -s) >> 31 & 0x9E3779BB) | 0) >>> 12 | 1);
        x = (x ^ x >>> 16) * 0xAC451;
        return (int) ((bound * ((x ^ x >>> 15) & 0xFFFFFFFFL)) >>> 32) & ~(bound >> 31);
    }

    @Override
    public final long nextLong() {
        int s = (stateA + 0xC1C64E6D | 0);
        int x = (s ^ s >>> 17) * ((stateB = stateB + ((s | -s) >> 31 & 0x9E3779BB) | 0) >>> 12 | 1);
        x = (x ^ x >>> 16) * 0xAC451;
        final long high = (x ^ x >>> 15);
        s = (stateA = stateA + 0x838C9CDA | 0);
        x = (s ^ s >>> 17) * ((stateB = stateB + ((s | -s) >> 31 & 0x9E3779BB) | 0) >>> 12 | 1);
        x = (x ^ x >>> 16) * 0xAC451;
        return (high << 32) | ((x ^ x >>> 15) & 0xFFFFFFFFL);
    }

    @Override
    public final boolean nextBoolean() {
        final int s = (stateA = stateA + 0xC1C64E6D | 0);
        final int x = (s ^ s >>> 17) * ((stateB = stateB + ((s | -s) >> 31 & 0x9E3779BB) | 0) >>> 12 | 1);
        return ((x ^ x >>> 16) & 1) == 0;
    }

    @Override
    public final double nextDouble() {
        int s = (stateA + 0xC1C64E6D | 0);
        int x = (s ^ s >>> 17) * ((stateB = stateB + ((s | -s) >> 31 & 0x9E3779BB) | 0) >>> 12 | 1);
        x = (x ^ x >>> 16) * 0xAC451;
        final long high = (x ^ x >>> 15);
        s = (stateA = stateA + 0x838C9CDA | 0);
        x = (s ^ s >>> 17) * ((stateB = stateB + ((s | -s) >> 31 & 0x9E3779BB) | 0) >>> 12 | 1);
        x = (x ^ x >>> 16) * 0xAC451;
        return  (((high << 32) | ((x ^ x >>> 15) & 0xFFFFFFFFL))
                & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    @Override
    public final float nextFloat() {
        final int s = (stateA = stateA + 0xC1C64E6D | 0);
        int x = (s ^ s >>> 17) * ((stateB = stateB + ((s | -s) >> 31 & 0x9E3779BB) | 0) >>> 12 | 1);
        x = (x ^ x >>> 16) * 0xAC451;
        return ((x ^ x >>> 15) & 0xffffff) * 0x1p-24f;
    }
    
    @Override
    public SilkRNG copy() {
        return new SilkRNG(stateA, stateB);
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
    }

    public int getStateA()
    {
        return stateA;
    }
    
    public void setStateA(int stateA)
    {
        this.stateA = stateA;
    }
    
    public int getStateB()
    {
        return stateB;
    }
    
    public void setStateB(int stateB)
    {
        this.stateB = stateB;
    }

    public void setState(int stateA, int stateB)
    {
        this.stateA = stateA;
        this.stateB = stateB;
    }

    @Override
    public long getState() {
        return stateA & 0xFFFFFFFFL | ((long)stateB) << 32;
    }

    @Override
    public void setState(final long state) {
        stateA = (int)(state & 0xFFFFFFFFL);
        stateB = (int)(state >>> 32);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SilkRNG silkRNG = (SilkRNG) o;

        if (stateA != silkRNG.stateA) return false;
        return stateB == silkRNG.stateB;
    }

    @Override
    public int hashCode() {
        return 31 * stateA + stateB;
    }
    
    @Override
    public String toString() {
        return "SilkRNG with stateA 0x" + StringKit.hex(stateA) + " and stateB 0x" + StringKit.hex(stateB);
    }
    public static int determineInt(int state) {
        return (state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19;
    }
    public static int determineBounded(int state, final int bound)
    {
        return (int) ((((state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19) & 0xFFFFFFFFL) * bound >> 32);
    }
    public static long determine(int state)
    {
        int r = (state ^ 0xD1B54A35) * 0x102473;
        r = (r = (r ^ r >>> 11 ^ r >>> 21) * (r | 0xFFE00001)) ^ r >>> 13 ^ r >>> 19;
        return ((long) r << 32) | (((state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19) & 0xFFFFFFFFL);
    }
    public static float determineFloat(int state) {
        return (((state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19) & 0xFFFFFF) * 0x1p-24f;
    }
    public static double determineDouble(int state)
    {
        return ((state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19) * 0x1p-32 + 0.5;
    }
}
