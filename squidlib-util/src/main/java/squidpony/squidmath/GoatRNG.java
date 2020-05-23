package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A larger-period generator with 128 bits of state, good speed, and high quality in PractRand testing; it is at least
 * 1-dimensionally equidistributed. It appears to be slightly faster than a Xorshift128+ generator, like RandomXS128 in
 * libGDX, with practically the same period (this has a period of 2 to the 128), but it isn't as fast as as
 * {@link OrbitRNG} or {@link GearRNG} (Goat seems to have higher statistical quality than either of those late in
 * testing, though). It passes 64TB of PractRand testing with no anomalies, and might be able to endure tests past even
 * that (the default setting stops at 32TB); I had set up PractRand to test up to 128TB but it ran out of memory after
 * 5 days of nonstop testing on all 6 cores. If 16GB of RAM isn't enough to find even an anomaly in GoatRNG, then it is
 * probably quite good.
 * <br>
 * Unlike most RNGs from outside Goat, Gear, and Orbit's family, this relies on a conditional and comparison to achieve
 * both its period and randomness goals. All of these generators use two additive sequences for their states, and stall
 * one of the state updates when the other state matches a criterion. For Orbit, it stalls only when stateA is 0
 * (roughly 1 in 18 quintillion generated numbers); for Gear, stateA must be less than 1863783533 away from the minimum
 * long value (roughly 1 in 10 billion generated numbers), and here, stateA must be less than 5096992405936522019L to
 * stall (roughly 3 in 4 generated numbers).
 * <br>
 * Unlike GearRNG, this allows all long values for both states; unlike OrbitRNG, there shouldn't be significant
 * correlation between an even value for stateB and the value 1 greater than that (such as 4 and 5, or 100 and 101). 
 * <br>
 * The name comes from how goats are reliably hard to predict, like an RNG should be.
 * It is called GhoulRNG in some tests run on it; an earlier version of GoatRNG also exists
 * that does well in testing for the first 32TB and then suddenly has serious issues.
 * <br>
 * Created by Tommy Ettinger on 5/8/2020.
 */
public final class GoatRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * Can be any long value.
     */
    public long stateA;
    /**
     * Can be any long value.
     */
    public long stateB;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public GoatRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public GoatRNG(long seed) {
        stateA = (seed = (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25);
        stateB =         (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25;
    }

    public GoatRNG(final long seedA, final long seedB) {
        stateA = seedA;
        stateB = seedB;
    }

    /**
     * Get the "A" part of the internal state as a long.
     *
     * @return the current internal state of this object.
     */
    public long getStateA() {
        return stateA;
    }

    /**
     * Set the "A" part of the internal state with a long.
     *
     * @param stateA any 64-bit long
     */
    public void setStateA(long stateA) {
        this.stateA = stateA;
    }

    /**
     * Get the "B" part of the internal state as a long.
     *
     * @return the current internal "B" state of this object.
     */
    public long getStateB() {
        return stateB;
    }

    /**
     * Set the "B" part of the internal state with a long.
     *
     * @param stateB any 64-bit long
     */
    public void setStateB(long stateB) {
        this.stateB = stateB;
    }


    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public final int next(final int bits) {
//        long s = (stateA += 0xD1342543DE82EF95L);
//        s ^= s >>> 31;
//        if(s < 0x31E131D6149D9795L) {
//            final long t = (stateB += 0xC6BC279692B5C323L);
//            s *= ((t ^ t >>> 29 ^ t << 11) | 1L);
//            return (int)(s ^ s >>> 25) >>> (32 - bits);
//        }
//        else {
//            final long t = stateB;
//            s *= ((t ^ t >>> 29 ^ t << 11) | 1L);
//            return (int)(s ^ s >>> 25) >>> (32 - bits);
//        }
        long s = (stateA += 0xD1342543DE82EF95L);
        s ^= s >>> 31 ^ s >>> 23;
        if(s < 0x46BC279692B5C323L) {
            final long t = stateB;
            s *= ((t ^ t << 9) | 1L);
            return (int)(s ^ s >>> 25) >>> (32 - bits);
        }
        else {
            final long t = (stateB += 0xB1E131D6149D9795L);
            s *= ((t ^ t << 9) | 1L);
            return (int)(s ^ s >>> 25) >>> (32 - bits);
        }
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
        long s = (stateA += 0xD1342543DE82EF95L);
        s ^= s >>> 31 ^ s >>> 23;
        if(s < 0x46BC279692B5C323L) {
            final long t = stateB;
            s *= ((t ^ t << 9) | 1L);
            return s ^ s >>> 25;
        }
        else {
            final long t = (stateB += 0xB1E131D6149D9795L);
            s *= ((t ^ t << 9) | 1L);
            return s ^ s >>> 25;
        }
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public GoatRNG copy() {
        return new GoatRNG(stateA, stateB);
    }
    @Override
    public String toString() {
        return "GoatRNG with stateA 0x" + StringKit.hex(stateA) + "L and stateB 0x" + StringKit.hex(stateB) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GoatRNG goatRNG = (GoatRNG) o;

        return stateA == goatRNG.stateA && stateB == goatRNG.stateB;
    }

    @Override
    public int hashCode() {
        return (int) (31L * (stateA ^ (stateA >>> 32)) + (stateB ^ stateB >>> 32));
    }
    
}
