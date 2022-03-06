/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
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
 */

package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A RandomnessSource with three {@code long} states that changes its state with three ways it can connect states to
 * other states. This has been backported from jdkgdxds, which got this from SquidSquad originally. This generator has
 * an unknown period that is statistically extremely likely to be very long (more than 2 to the 64), and no combinations
 * of states are known that put it in a bad starting state or one with a shorter period. It is a very fast generator,
 * particularly on Java 16 and newer. It has passed 64TB of PractRand testing with no anomalies, and 4PB of hwd testing.
 * <br>
 * TricycleRNG, it's got three states and it cycles changes between them.
 * <br>
 * Created by Tommy Ettinger on 7/13/2021.
 */
public class TricycleRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 0L;
    /**
     * Can be any long value.
     */
    public long stateA;

    /**
     * Can be any long value.
     */
    public long stateB;

    /**
     * Can be any long value.
     */
    public long stateC;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public TricycleRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public TricycleRNG(long seed) {
        setSeed(seed);
    }

    public TricycleRNG(final long seedA, final long seedB, long seedC) {
        stateA = seedA;
        stateB = seedB;
        stateC = seedC;
    }
    /**
     * This initializes all 3 states of the generator to random values based on the given seed.
     * (2 to the 64) possible initial generator states can be produced here, all with a different
     * first value returned by {@link #nextLong()} (because {@code stateA} is guaranteed to be
     * different for every different {@code seed}).
     * @param seed the initial seed; may be any long
     */
    public void setSeed(long seed) {
        long x = (seed += 0x9E3779B97F4A7C15L);
        x ^= x >>> 27;
        x *= 0x3C79AC492BA7B653L;
        x ^= x >>> 33;
        x *= 0x1C69B3F74AC4AE35L;
        stateA = x ^ x >>> 27;
        x = (seed += 0x9E3779B97F4A7C15L);
        x ^= x >>> 27;
        x *= 0x3C79AC492BA7B653L;
        x ^= x >>> 33;
        x *= 0x1C69B3F74AC4AE35L;
        stateB = x ^ x >>> 27;
        x = (seed + 0x9E3779B97F4A7C15L);
        x ^= x >>> 27;
        x *= 0x3C79AC492BA7B653L;
        x ^= x >>> 33;
        x *= 0x1C69B3F74AC4AE35L;
        stateC = x ^ x >>> 27;
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
     * Get the "C" part of the internal state as a long.
     *
     * @return the current internal "C" state of this object.
     */
    public long getStateC() {
        return stateC;
    }

    /**
     * Set the "C" part of the internal state with a long.
     *
     * @param stateC any 64-bit long
     */
    public void setStateC(long stateC) {
        this.stateC = stateC;
    }


    @Override
    public long nextLong() {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        stateA = 0xD1342543DE82EF95L * fc;
        stateB = fa ^ fb ^ fc;
        stateC = (fb << 41 | fb >>> 23) + 0xC6BC279692B5C323L;
        return fa;
    }

    public long previousLong() {
        final long fa = stateA;
        final long fb = stateB;
        long fc = stateC - 0xC6BC279692B5C323L;
        stateC = 0x572B5EE77A54E3BDL * fa;
        stateB = (fc >>> 41 | fc << 23);
        stateA = fb ^ stateB ^ stateC;
        fc = stateC - 0xC6BC279692B5C323L;
        return stateB ^ 0x572B5EE77A54E3BDL * stateA ^ (fc >>> 41 | fc << 23);

    }

    @Override
    public int next(int bits) {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        stateA = 0xD1342543DE82EF95L * fc;
        stateB = fa ^ fb ^ fc;
        stateC = (fb << 41 | fb >>> 23) + 0xC6BC279692B5C323L;
        return (int)fa >>> (32 - bits);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public TricycleRNG copy() {
        return new TricycleRNG(stateA, stateB, stateC);
    }
    @Override
    public String toString() {
        return "TricycleRNG with stateA 0x" + StringKit.hex(stateA) + "L, stateB 0x" + StringKit.hex(stateB) + "L, and stateC 0x" + StringKit.hex(stateC) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TricycleRNG tricycleRNG = (TricycleRNG) o;

        return stateA == tricycleRNG.stateA && stateB == tricycleRNG.stateB && stateC == tricycleRNG.stateC;
    }

    @Override
    public int hashCode() {
        return (int) (421L * (stateA ^ (stateA >>> 32)) + 29L * (stateB ^ (stateB >>> 32)) + (stateC ^ stateC >>> 32));
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
