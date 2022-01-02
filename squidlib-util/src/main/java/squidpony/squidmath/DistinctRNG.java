/*
 * Copyright (c) 2022  Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A StatefulRandomness with one {@code long} state; calling {@link #nextLong()} will produce every {@code long} exactly
 * once before repeating. This has been backported from jdkgdxds, which got this from SquidSquad originally. It is
 * extremely similar to {@link LightRNG}, but uses a different mixing/unary-hash function -- where LightRNG uses a
 * variant on MurmurHash3's mixer called Variant 13, this uses Pelle Evensen's
 * <a href="https://mostlymangling.blogspot.com/2019/12/stronger-better-morer-moremur-better.html">Moremur mixer</a>,
 * which performs at the same (high) speed but has somewhat higher quality. This generator has a period of exactly 2 to
 * the 64. It is a rather fast generator, particularly on Java 8 and/or OpenJ9. It has passed 64TB of PractRand testing
 * with no anomalies. This is also a SkippingRandomness.
 * <br>
 * Created by Tommy Ettinger on 7/13/2021.
 */
public class DistinctRNG implements StatefulRandomness, SkippingRandomness, Serializable {
    private static final long serialVersionUID = 0L;
    /**
     * Can be any long value.
     */
    public long state;
    /**
     * Creates a new generator seeded using Math.random.
     */
    public DistinctRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public DistinctRNG(long seed) {
        setSeed(seed);
    }

    /**
     * This initializes all 3 states of the generator to random values based on the given seed.
     * (2 to the 64) possible initial generator states can be produced here, all with a different
     * first value returned by {@link #nextLong()} (because {@code stateA} is guaranteed to be
     * different for every different {@code seed}).
     * @param seed the initial seed; may be any long
     */
    public void setSeed(long seed) {
        state = seed;
    }

    @Override
    public long getState() {
        return state;
    }

    @Override
    public void setState(long state) {
        this.state = state;
    }

    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public int next(final int bits) {
        long x = (state += 0x9E3779B97F4A7C15L);
        x ^= x >>> 27;
        x *= 0x3C79AC492BA7B653L;
        x ^= x >>> 33;
        x *= 0x1C69B3F74AC4AE35L;
        return (int)(x ^ x >>> 27) >>> (32 - bits);
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
    public long nextLong() {
        long x = (state += 0x9E3779B97F4A7C15L);
        x ^= x >>> 27;
        x *= 0x3C79AC492BA7B653L;
        x ^= x >>> 33;
        x *= 0x1C69B3F74AC4AE35L;
        return (x ^ x >>> 27);
    }

    @Override
    public long skip(long advance) {
        long x = (state += 0x9E3779B97F4A7C15L * advance);
        x ^= x >>> 27;
        x *= 0x3C79AC492BA7B653L;
        x ^= x >>> 33;
        x *= 0x1C69B3F74AC4AE35L;
        return (x ^ x >>> 27);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public DistinctRNG copy() {
        return new DistinctRNG(state);
    }
    @Override
    public String toString() {
        return "DistinctRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DistinctRNG distinctRNG = (DistinctRNG) o;

        return state == distinctRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ state >>> 32);
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
