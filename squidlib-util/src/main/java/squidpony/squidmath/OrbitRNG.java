package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A variant on {@link ThrustAltRNG} that gives up some speed to gain a much better period and the ability to produce
 * all possible long values over that period. Its period is 2 to the 128, and it produces all long outputs with equal
 * likelihood. OrbitRNG is close to ThrustAltRNG in implementation, and ThrustAltRNG passes PractRand and TestU01 just
 * fine, but OrbitRNG should actually be more robust. For some purposes you may want to instead consider
 * {@link TangleRNG}, which also has two states and uses a very similar algorithm, but it skips some work Orbit does and
 * in doing so speeds up a lot and drops its period down to 2 to the 64. An individual TangleRNG can't produce all
 * possible long outputs and can produce some duplicates, but each pair of states for a TangleRNG has a different set of
 * which outputs will be skipped and which will be duplicated. Since it would require months of solid number generation
 * to exhaust the period of a TangleRNG, and that's the only time an output can be confirmed as skipped, it's probably
 * fine for some usage to use many different TangleRNGs, all seeded differently. In other cases you could use just one
 * OrbitRNG; its period is large enough not to worry about exhausting it in a game, though it isn't quite as fast as
 * TangleRNG. Orbit is extremely close in speed to a 64-bit variant on {@link Lathe32RNG}, and should have similarly
 * high quality; it should be slightly slower than {@link XoRoRNG} but doesn't fail the statistical tests that XoRo
 * fails rather badly.
 * <br>
 * The name comes from how the pair of states act like two planets orbiting a star at different rates, and also evokes
 * the larger-scale period relative to {@link TangleRNG}.
 * <br>
 * Created by Tommy Ettinger on 7/9/2018.
 */
public final class OrbitRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 4L;
    /**
     * Can be any long value.
     */
    public long stateA;
    
    public long stateB;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public OrbitRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public OrbitRNG(long seed) {
        stateA = (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25;
        stateB = (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25;
    }

    public OrbitRNG(final long seedA, final long seedB) {
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
        final long s = (stateA += 0x6C8E9CF570932BD5L);
        if(s == 0L)
            stateB += 0x9E3779B97F4A7C15L;
        final long z = (s ^ (s >>> 25)) * ((stateB += 0x9E3779B97F4A7C15L) | 1L);
        return (int)(z ^ (z >>> 22)) >>> (32 - bits);
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
        final long s = (stateA += 0x6C8E9CF570932BD5L);
        if(s == 0L)
            stateB += 0x9E3779B97F4A7C15L;
        final long z = (s ^ (s >>> 25)) * ((stateB += 0x9E3779B97F4A7C15L) | 1L);
        return z ^ (z >>> 22);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public OrbitRNG copy() {
        return new OrbitRNG(stateA, stateB);
    }
    @Override
    public String toString() {
        return "OrbitRNG with stateA 0x" + StringKit.hex(stateA) + "L and stateB 0x" + StringKit.hex(stateB) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrbitRNG orbitRNG = (OrbitRNG) o;

        return stateA == orbitRNG.stateA && stateB == orbitRNG.stateB;
    }

    @Override
    public int hashCode() {
        return (int) (31L * (stateA ^ (stateA >>> 32)) + (stateB ^ stateB >>> 32));
    }
    
//    public static void main(String[] args)
//    {
//        /*
//        cd target/classes
//        java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly sarong/ThrustAltRNG > ../../thrustalt_asm.txt
//         */
//        long seed = 1L;
//        ThrustAltRNG rng = new ThrustAltRNG(seed);
//
//        for (int i = 0; i < 1000000007; i++) {
//            seed += rng.nextLong();
//        }
//        System.out.println(seed);
//    }

}
