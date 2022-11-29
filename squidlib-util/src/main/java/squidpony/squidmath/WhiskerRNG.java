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
 * A RandomnessSource with four {@code long} states performs very few operations per random number, and can often
 * perform those operations as instruction-parallel. This has been backported from juniper, which is related to jdkgdxds
 * and SquidSquad. This generator has an unknown period that is statistically extremely likely to be very long (more
 * than 2 to the 64), and no combinations of states are known that put it in a bad starting state or one with a shorter
 * period. It is an extremely fast generator on Java 16 and newer, and is probably the fastest generator here when
 * running on HotSpot Java 16 (10% to 20% faster than the very-similar {@link FourWheelRNG}, the previous fastest
 * generator). It has passed 64TB of PractRand testing with no anomalies.
 * <br>
 * It's called Whisker because my very-bewhiskered cat Eddie was meowing at me while I tried to finish it.
 * <br>
 * Created by Tommy Ettinger on 7/16/2022.
 */
public class WhiskerRNG implements RandomnessSource, Serializable {
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
     * Can be any long value.
     */
    public long stateD;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public WhiskerRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public WhiskerRNG(long seed) {
        setSeed(seed);
    }

    public WhiskerRNG(final long seedA, final long seedB, long seedC, long seedD) {
        stateA = seedA;
        stateB = seedB;
        stateC = seedC;
        stateD = seedD;
    }

    /**
     * This initializes all 4 states of the generator to random values based on the given seed.
     * (2 to the 64) possible initial generator states can be produced here.
     *
     * @param seed the initial seed; may be any long
     */
    public void setSeed (long seed) {
        stateA = seed ^ 0xC6BC279692B5C323L;
        stateB = seed ^ ~0xC6BC279692B5C323L;
        seed ^= seed >>> 32;
        seed *= 0xBEA225F9EB34556DL;
        seed ^= seed >>> 29;
        seed *= 0xBEA225F9EB34556DL;
        seed ^= seed >>> 32;
        seed *= 0xBEA225F9EB34556DL;
        seed ^= seed >>> 29;
        stateC = ~seed;
        stateD = seed;
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

    /**
     * Get the "D" part of the internal state as a long.
     *
     * @return the current internal "D" state of this object.
     */
    public long getStateD() {
        return stateD;
    }

    /**
     * Set the "D" part of the internal state with a long.
     *
     * @param stateD any 64-bit long
     */
    public void setStateD(long stateD) {
        this.stateD = stateD;
    }

    @Override
    public long nextLong () {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        stateA = fd * 0xF1357AEA2E62A9C5L; // Considered good by Steele and Vigna, https://arxiv.org/abs/2001.05304v1
        stateB = (fa << 44 | fa >>> 20);
        stateC = fb + 0x9E3779B97F4A7C15L; // 2 to the 64 divided by the golden ratio
        return stateD = fa ^ fc;
    }

    public long previousLong () {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        stateA = (fb >>> 44 | fb << 20);
        stateB = fc - 0x9E3779B97F4A7C15L;
        stateC = stateA ^ fd;
        return stateD = fa * 0x781494A55DAAED0DL; // modular multiplicative inverse of 0xF1357AEA2E62A9C5L
    }

    @Override
    public int next (int bits) {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        stateA = fd * 0xF1357AEA2E62A9C5L;
        stateB = (fa << 44 | fa >>> 20);
        stateC = fb + 0x9E3779B97F4A7C15L;
        return (int)(stateD = fa ^ fc) >>> (32 - bits);
    }

    public double nextDouble() {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        stateA = fd * 0xF1357AEA2E62A9C5L; // Considered good by Steele and Vigna, https://arxiv.org/abs/2001.05304v1
        stateB = (fa << 44 | fa >>> 20);
        stateC = fb + 0x9E3779B97F4A7C15L; // 2 to the 64 divided by the golden ratio
        return ((stateD = fa ^ fc) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    public float nextFloat() {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        stateA = fd * 0xF1357AEA2E62A9C5L; // Considered good by Steele and Vigna, https://arxiv.org/abs/2001.05304v1
        stateB = (fa << 44 | fa >>> 20);
        stateC = fb + 0x9E3779B97F4A7C15L; // 2 to the 64 divided by the golden ratio
        return ((stateD = fa ^ fc) & 0xFFFFFFL) * 0x1p-24f;
    }

    public int nextInt(final int bound) {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        stateA = fd * 0xF1357AEA2E62A9C5L; // Considered good by Steele and Vigna, https://arxiv.org/abs/2001.05304v1
        stateB = (fa << 44 | fa >>> 20);
        stateC = fb + 0x9E3779B97F4A7C15L; // 2 to the 64 divided by the golden ratio
        return (int)((bound * ((stateD = fa ^ fc) & 0xFFFFFFFFL)) >> 32);
    }

    /**
     * Produces a copy of this WhiskerRNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so that it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this WhiskerRNG
     */
    @Override
    public WhiskerRNG copy() {
        return new WhiskerRNG(stateA, stateB, stateC, stateD);
    }
    
    @Override
    public String toString() {
        return "WhiskerRNG with stateA 0x" + StringKit.hex(stateA) + "L, stateB 0x" + StringKit.hex(stateB)
                + "L, stateC 0x" + StringKit.hex(stateC) + "L, and stateD 0x" + StringKit.hex(stateD) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WhiskerRNG whiskerRNG = (WhiskerRNG) o;

        return stateA == whiskerRNG.stateA && stateB == whiskerRNG.stateB && stateC == whiskerRNG.stateC
                && stateD == whiskerRNG.stateD;
    }

    @Override
    public int hashCode() {
        return (int) (9689L * (stateA ^ (stateA >>> 32)) + 421L * (stateB ^ (stateB >>> 32)) + 29L * (stateC ^ (stateC >>> 32)) + (stateD ^ stateD >>> 32));
    }
}
