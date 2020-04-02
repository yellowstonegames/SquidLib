package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A larger-period generator with 127 bits of state (two longs, one is always odd), a period of 2 to the 127, and what
 * should be slightly better speed than the related OrbitRNG. It passes 32TB of PractRand testing with no anomalies or
 * failures. The idea for this generator is to have a simple Weyl sequence (a counter with a large increment) for
 * stateA, and at one point to multiply a value based on stateA by stateB, which is always odd. The trick is that stateB
 * usually updates by adding a large even number, but about once every 10 billion generated numbers, decided by a
 * comparison between a constant and stateA's value, it "shifts gears" and stays on the same value while stateA updates.
 * <br>
 * For some purposes you may want to instead consider {@link TangleRNG}, which also has two states (one odd) and uses a
 * very similar algorithm, but it never "shifts gears," which drops its period down to 2 to the 64, makes it a
 * {@link SkippingRandomness}, and should in theory speed it up (in practice, TangleRNG needs an extra step that actually
 * makes it slower than GearRNG). An individual TangleRNG can't produce all possible long outputs and can produce some
 * duplicates, but each pair of states for a TangleRNG has a different set of which outputs will be skipped and which
 * will be duplicated. Since it would require months of solid number generation to exhaust the period of a TangleRNG, and
 * that's the only time an output can be confirmed as skipped, it's probably fine for most usage to use many different
 * TangleRNGs, all seeded differently. Other choices: you could use one {@link OrbitRNG} (which technically has a longer
 * period, but some states produce very similar output), {@link DiverRNG} (if you don't mind that it never produces a
 * duplicate output), {@link IsaacRNG} (if speed is less important but more secure output is), or Lathe64RNG, though all
 * of those are probably slower than using one GearRNG object or even many TangleRNG objects.
 * <br>
 * The name comes from shifting gears; pretty straightforward here.
 * <br>
 * Created by Tommy Ettinger on 3/29/2020.
 */
public final class GearRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 5L;
    /**
     * Can be any long value.
     */
    private long stateA;
    /**
     * Can be any odd long value.
     */
    private long stateB;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public GearRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public GearRNG(long seed) {
        stateA = (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25;
        stateB = ((seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25) | 1L;
    }

    public GearRNG(final long seedA, final long seedB) {
        stateA = seedA;
        stateB = seedB | 1L;
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
     * Set the "B" part of the internal state with a long; the lowest bit is always discarded and replaced with 1.
     * That is, stateB is always an odd number, and if an even number is given it will be incremented.
     *
     * @param stateB any 64-bit long
     */
    public void setStateB(long stateB) {
        this.stateB = stateB | 1L;
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
        final long s = (stateA += 0xC6BC279692B5C323L);
        final long z = ((s < 0x800000006F17146DL) ? stateB : (stateB += 0x9479D2858AF899E6L)) * (s ^ s >>> 31);
        return (int)(z ^ z >>> 25) >>> (32 - bits);
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
        final long s = (stateA += 0xC6BC279692B5C323L);
        final long z = ((s < 0x800000006F17146DL) ? stateB : (stateB += 0x9479D2858AF899E6L)) * (s ^ s >>> 31);
        return z ^ z >>> 25;
    }
    
    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public GearRNG copy() {
        return new GearRNG(stateA, stateB);
    }
    @Override
    public String toString() {
        return "GearRNG with stateA 0x" + StringKit.hex(stateA) + "L and stateB 0x" + StringKit.hex(stateB) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GearRNG gearRNG = (GearRNG) o;

        return stateA == gearRNG.stateA && stateB == gearRNG.stateB;
    }

    @Override
    public int hashCode() {
        return (int) (31L * (stateA ^ (stateA >>> 32)) + (stateB ^ stateB >>> 32));
    }
    
}
