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
 * A RandomnessSource with four {@code long} states; one is a simple counter, and the rest mix and match all four states
 * to get their next value. This has been backported from jdkgdxds. This generator has a known minimum period of at
 * least 2 to the 64 and is statistically extremely likely to be much longer; no combinations of states are known that
 * put it in a bad starting state, and none are possible that reduce period below 2 to the 64. It uses no multiplication
 * and actually only needs addition, bitwise-rotation, and XOR to run; this may help on some exotic hardware.
 * <br>
 * It is a very fast generator on Java 16 and newer, though {@link FourWheelRNG} is faster on desktop hardware.
 * Another RandomnessSource that avoids multiplication, {@link StrangerRNG}, is not as fast as this (in general). It has
 * passed 64TB of PractRand testing with no anomalies, and 300TB of hwd testing.
 * <br>
 * Trim, because it uses a trimmed-down set of operations (just add, bitwise-rotate, and XOR).
 * <br>
 * Created by Tommy Ettinger on 12/20/2021.
 */
public class TrimRNG implements RandomnessSource, Serializable {
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
    public TrimRNG() {
        this((long) ((Math.random() - 0.5) * 0x1p52)
                        ^ (long) ((Math.random() - 0.5) * 0x1p64),
                (long) ((Math.random() - 0.5) * 0x1p52)
                        ^ (long) ((Math.random() - 0.5) * 0x1p64),
                (long) ((Math.random() - 0.5) * 0x1p52)
                        ^ (long) ((Math.random() - 0.5) * 0x1p64),
                (long) ((Math.random() - 0.5) * 0x1p52)
                        ^ (long) ((Math.random() - 0.5) * 0x1p64));
    }

    public TrimRNG(long seed) {
        setSeed(seed);
    }

    public TrimRNG(final long seedA, final long seedB, long seedC, long seedD) {
        stateA = seedA;
        stateB = seedB;
        stateC = seedC;
        stateD = seedD;
    }
    /**
     * This initializes all 3 states of the generator to random values based on the given seed.
     * (2 to the 64) possible initial generator states can be produced here, all with a different
     * first value returned by {@link #nextLong()} (because {@code stateC} is guaranteed to be
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
        x = (seed += 0x9E3779B97F4A7C15L);
        x ^= x >>> 27;
        x *= 0x3C79AC492BA7B653L;
        x ^= x >>> 33;
        x *= 0x1C69B3F74AC4AE35L;
        stateC = x ^ x >>> 27;
        x = (seed + 0x9E3779B97F4A7C15L);
        x ^= x >>> 27;
        x *= 0x3C79AC492BA7B653L;
        x ^= x >>> 33;
        x *= 0x1C69B3F74AC4AE35L;
        stateD = x ^ x >>> 27;
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
    public long nextLong() {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long bc = fb + fc, cd = fc ^ fd;
        stateA = (bc << 35 | bc >>> 29);
        stateB = (cd << 46 | cd >>> 18);
        stateC = fa + fb;
        stateD = fd + 0x06A0F81D3D2E35EFL;
        return fc;
    }

    public long previousLong() {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        stateD -= 0x06A0F81D3D2E35EFL;
        long t = (fb >>> 46 | fb << 18);
        stateC = t ^ stateD;
        t = (fa >>> 35 | fa << 29);
        stateB = t - stateC;
        stateA = fc - stateB;
        return (stateB >>> 46 | stateB << 18) ^ stateD - 0x06A0F81D3D2E35EFL;
    }

    @Override
    public int next(int bits) {
        final long fa = stateA;
        final long fb = stateB;
        final long fc = stateC;
        final long fd = stateD;
        final long bc = fb + fc, cd = fc ^ fd;
        stateA = (bc << 35 | bc >>> 29);
        stateB = (cd << 46 | cd >>> 18);
        stateC = fa + fb;
        stateD = fd + 0x06A0F81D3D2E35EFL;
        return (int)fc >>> (32 - bits);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public TrimRNG copy() {
        return new TrimRNG(stateA, stateB, stateC, stateD);
    }
    @Override
    public String toString() {
        return "TrimRNG with stateA 0x" + StringKit.hex(stateA) + "L, stateB 0x" + StringKit.hex(stateB)
                + "L, stateC 0x" + StringKit.hex(stateC) + "L, and stateD 0x" + StringKit.hex(stateD) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrimRNG trimRNG = (TrimRNG) o;

        return stateA == trimRNG.stateA && stateB == trimRNG.stateB && stateC == trimRNG.stateC
                && stateD == trimRNG.stateD;
    }

    @Override
    public int hashCode() {
        return (int) (9689L * (stateA ^ (stateA >>> 32)) + 421L * (stateB ^ (stateB >>> 32)) + 29L * (stateC ^ (stateC >>> 32)) + (stateD ^ stateD >>> 32));
    }
}
