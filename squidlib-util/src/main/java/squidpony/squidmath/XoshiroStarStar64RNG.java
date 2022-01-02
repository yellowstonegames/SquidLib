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

/**
 * A random number generator that is fairly fast and guarantees 4-dimensional equidistribution (with the exception of the
 * quartet with four zeroes in a row, every quartet of long results is produced exactly once over the period). It has a
 * period of (2 to the 256) - 1, which would take millennia to exhaust on current-generation hardware (at least). This
 * implements the Xoshiro256** algorithm, using 64-bit math with four 64-bit state variables.
 * It can be considered stable, like the other EnhancedRandom implementations here. This passes heavy testing, but isn't a
 * cryptographic generator, and it does have known issues when its output is multiplied by certain specific constants (any
 * of a lot) and tests are then run. The only invalid state is the one with 0 in each state variable, and this won't ever
 * occur in the normal period of that contains all other states.
 * <br>
 * The main reasons you could prefer this generator to the typically-faster {@link FourWheelRNG} are:
 * <ul>
 *     <li>This generator is 4D equidistributed, so groups of four coordinates will always be unique.</li>
 *     <li>This generator has a known, incredibly long period.</li>
 *     <li>This generator is well-studied and appeared in a paper.</li>
 *     <li>You will never use Java 16, and if you use Java 17, you would rather use the implementation in the JDK there.</li>
 *     <li>You need a regular structure to the generated numbers, with guarantees about that structure.</li>
 * </ul>
 * <br>
 * Xoshiro256** was written in 2018 by David Blackman and Sebastiano Vigna. You can consult their paper for technical details:
 * <a href="https://vigna.di.unimi.it/ftp/papers/ScrambledLinear.pdf">PDF link here</a>.
 */
public class XoshiroStarStar64RNG implements RandomnessSource {

    /**
     * The first state; can be any long, as long as all states are not 0.
     */
    protected long stateA;
    /**
	 * The second state; can be any long, as long as all states are not 0.
     * This is the state that is scrambled and returned; if it is 0 before a number
     * is generated, then the next number will be 0.
     */
    protected long stateB;
    /**
     * The third state; can be any long, as long as all states are not 0.
     */
    protected long stateC;
    /**
     * The fourth state; can be any long, as long as all states are not 0.
     */
    protected long stateD;

    /**
     * Creates a new FourWheelRandom with a random state.
     */
    public XoshiroStarStar64RNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    /**
     * Creates a new FourWheelRandom with the given seed; all {@code long} values are permitted.
     * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
     * @param seed any {@code long} value
     */
    public XoshiroStarStar64RNG(long seed) {
        setSeed(seed);
    }

    /**
     * Creates a new FourWheelRandom with the given four states; all {@code long} values are permitted.
     * These states will be used verbatim, as long as they are not all 0. In that case, stateD is changed.
     * @param stateA any {@code long} value
     * @param stateB any {@code long} value
     * @param stateC any {@code long} value
     * @param stateD any {@code long} value
     */
    public XoshiroStarStar64RNG(long stateA, long stateB, long stateC, long stateD) {
        this.stateA = stateA;
        this.stateB = stateB;
        this.stateC = stateC;
        this.stateD = stateD;
        if((stateA | stateB | stateC | stateD) == 0L) this.stateD = 0x9E3779B97F4A7C15L;
    }

    /**
     * This initializes all 4 states of the generator to random values based on the given seed.
     * (2 to the 64) possible initial generator states can be produced here, all with a different
     * first value returned by {@link #nextLong()} (because {@code stateB} is guaranteed to be
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

    public long getStateA() {
        return stateA;
    }

    /**
     * Sets the first part of the state.
     * @param stateA can be any long
     */
    public void setStateA(long stateA) {
        this.stateA = stateA;
    }

    public long getStateB() {
        return stateB;
    }

    /**
     * Sets the second part of the state. Note that if you set this state to 0, the next random long (or most other types)
     * will be 0, regardless of the other states.
     * @param stateB can be any long
     */
    public void setStateB(long stateB) {
        this.stateB = stateB;
    }

    public long getStateC() {
        return stateC;
    }

    /**
     * Sets the third part of the state.
     * @param stateC can be any long
     */
    public void setStateC(long stateC) {
        this.stateC = stateC;
    }

    public long getStateD() {
        return stateD;
    }

    /**
     * Sets the fourth part of the state.
     * @param stateD can be any long
     */
    public void setStateD(long stateD) {
        this.stateD = stateD;
    }

    /**
     * Sets the state completely to the given four state variables.
     * This is the same as calling {@link #setStateA(long)}, {@link #setStateB(long)},
     * {@link #setStateC(long)}, and {@link #setStateD(long)} as a group.
     * @param stateA the first state; can be any long
     * @param stateB the second state; can be any long
     * @param stateC the third state; can be any long
     * @param stateD the fourth state; this will be returned as-is if the next call is to {@link #nextLong()}
     */
    public void setState(long stateA, long stateB, long stateC, long stateD) {
        this.stateA = stateA;
        this.stateB = stateB;
        this.stateC = stateC;
        this.stateD = stateD;
        if((stateA | stateB | stateC | stateD) == 0L) this.stateD = 0x9E3779B97F4A7C15L;
    }

    @Override
    public long nextLong() {
        final long result = Long.rotateLeft(stateB * 5, 7) * 9;
        final long t = stateB << 17;
        stateC ^= stateA;
        stateD ^= stateB;
        stateB ^= stateC;
        stateA ^= stateD;
        stateC ^= t;
        stateD = Long.rotateLeft(stateD, 45);
        return result;
    }

    @Override
    public int next(int bits) {
        final long result = Long.rotateLeft(stateB * 5, 7) * 9;
        final long t = stateB << 17;
        stateC ^= stateA;
        stateD ^= stateB;
        stateB ^= stateC;
        stateA ^= stateD;
        stateC ^= t;
        stateD = Long.rotateLeft(stateD, 45);
        return (int)(result >>> 64 - bits);
    }

    @Override
    public XoshiroStarStar64RNG copy() {
        return new XoshiroStarStar64RNG(stateA, stateB, stateC, stateD);
    }

    @Override
    public boolean equals (Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        XoshiroStarStar64RNG that = (XoshiroStarStar64RNG)o;

        if (stateA != that.stateA)
            return false;
        if (stateB != that.stateB)
            return false;
        if (stateC != that.stateC)
            return false;
        return stateD == that.stateD;
    }

    public String toString() {
        return "XoshiroStarStar64Random{" +
                "stateA=" + stateA +
                "L, stateB=" + stateB +
                "L, stateC=" + stateC +
                "L, stateC=" + stateD +
                "L}";
    }
}
