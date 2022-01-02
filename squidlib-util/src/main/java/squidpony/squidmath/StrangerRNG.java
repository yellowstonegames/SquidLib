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
 * A RandomnessSource with four {@code long} states that changes its state with complex ways it can connect states to
 * other states. This has been backported from jdkgdxds. This generator has a known minimum period of (2 to the 65) - 2,
 * but a shorter probable maximum period than {@link FourWheelRNG} (which has no guarantee on minimum period). It is a
 * very fast generator on Java 16 and newer, though slower than FourWheelRNG when multiplication is fast on the platform
 * in question. It's about 30% faster than Java 17's built-in Xoshiro256PlusPlus when also run on Java 17; both use no
 * multiplication and have the same state size. It's unusually slower on Java 8, relative to more recent JDKs like 16 or
 * 17; on Java 8 specifically, you might prefer FourWheelRNG or {@link TricycleRNG}.
 * <br>
 * StrangerRNG has an unusual structure that uses no multiplication. Its core is in two states, A and B, that interleave
 * a 64-bit xorshift generator in each state, with A reaching a "complete" return value when B has not, and vice versa.
 * Each of the interleaved generators has a period of (2 to the 64) - 1 "complete" return values, which makes the two
 * generators have a combined period of (2 to the 65) - 2 . The A and B states must be significantly different for this
 * to pass testing, so the seeding for this class takes advantage of the presence of a fast jump algorithm for the
 * xorshift generator, and jumps B ahead of A by {@code 0x9E3779B97F4A7C15} (or 11.4 quintillion) steps forward. States
 * C and D are more typical; C incorporates a rotated D and B, while D incorporates C, A, and a large additive constant.
 * This class returns the C state as it was before next state is calculated, as an optimization, so it is recommended
 * that you set the state with one long, or with three longs that are sufficiently random already for your purposes.
 * Both the one- and three-parameter setSeed() methods separate the A and B states by many steps; the one-parameter
 * setSeed() also randomizes state C so the first result returned will be randomly different from the seed.
 * <br>
 * This generator has been tested thoroughly; it has passed 64TB of PractRand testing with no anomalies, 6PB of hwd
 * testing, and multiple weeks of testing for differences from the expected frequency of "extinction" (where the bitwise
 * AND of outputs becomes 0) or "saturation" (where the bitwise OR of outputs becomes all 1s) events in short output
 * sequences.
 * <br>
 * Stranger has two states that perpetually circle each other, never getting closer and remaining strangers to each
 * other. This algorithm should also be faster on "stranger" hardware that doesn't behave like x86_64, like code that
 * runs on the GPU.
 * <br>
 * Created by Tommy Ettinger on 10/8/2021, with substantial help from Spencer Fleming on the jump() method.
 */
public class StrangerRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 0L;
    /**
     * Can be any long value except 0.
     */
    protected long stateA;

    /**
     * Can be any long value except 0, and should be very distant from stateA in the xorshift sequence.
     */
    protected long stateB;

    /**
     * Can be any long value; will be returned verbatim from {@link #nextLong()} and only change for the next call.
     */
    protected long stateC;

    /**
     * Can be any long value.
     */
    protected long stateD;

    /**
     * Jumps {@code state} ahead by 0x9E3779B97F4A7C15 steps of the generator StrangerRandom uses for its stateA
     * and stateB. When used how it is here, it ensures stateB is 11.4 quintillion steps ahead of stateA in their
     * shared sequence, or 7 quintillion behind if you look at it another way. It would typically take years of
     * continuously running this generator at 100GB/s to have stateA become any state that stateB has already been.
     * Users only need this function if setting stateB by-hand; in that case, {@code state} should be their stateA.
     * <br>
     * Massive credit to Spencer Fleming for writing essentially all of this function over several days.
     * @param state the initial state of a 7-9 xorshift generator
     * @return state jumped ahead 0x9E3779B97F4A7C15 times (unsigned)
     */
    public static long jump(long state){
        final long poly = 0x5556837749D9A17FL;
        long val = 0L, b = 1L;
        for (int i = 0; i < 63; i++, b <<= 1) {
            if((poly & b) != 0L) val ^= state;
            state ^= state << 7;
            state ^= state >>> 9;
        }
        return val;
    }

    /**
     * Creates a new generator seeded using Math.random.
     */
    public StrangerRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public StrangerRNG(long seed) {
        setSeed(seed);
    }

    public StrangerRNG(final long seedA, final long seedB, long seedC, long seedD) {
        this.stateA = (seedA == 0L) ? 0xD3833E804F4C574BL : seedA;
        this.stateB = (seedB == 0L) ? 0x790B300BF9FE738FL : seedB;
        this.stateC = seedC;
        this.stateD = seedD;
    }

    /**
     * This initializes all 4 states of the generator to random values based on the given seed.
     * (2 to the 64) possible initial generator states can be produced here, all with a different
     * first value returned by {@link #nextLong()} (because {@code stateC} is guaranteed to be
     * different for every non-zero {@code seed}). This ensures stateB is a sufficient distance
     * from stateA in their shared sequence, and also does some randomizing on the seed before it
     * assigns the result to stateC. This isn't an instantaneously-fast method to call like some
     * versions of setSeed(), but it shouldn't be too slow unless it is called before every
     * generated number (even then, it might be fine).
     * @param seed the initial seed; may be any long
     */
    public void setSeed(long seed) {
        stateA = seed ^ 0xFA346CBFD5890825L;
        if(stateA == 0L) stateA = 0xD3833E804F4C574BL;
        stateB = jump(stateA);
        stateC = jump(stateB - seed);
        stateD = jump(stateC + 0xC6BC279692B5C323L);
    }

    public long getStateA() {
        return stateA;
    }

    /**
     * Sets the first part of the state.
     * @param stateA can be any long except 0; this treats 0 as 0xD3833E804F4C574BL
     */
    public void setStateA(long stateA) {
        this.stateA = (stateA == 0L) ? 0xD3833E804F4C574BL : stateA;
    }

    public long getStateB() {
        return stateB;
    }

    /**
     * Sets the second part of the state.
     * @param stateB can be any long except 0; this treats 0 as 0x790B300BF9FE738FL
     */
    public void setStateB(long stateB) {
        this.stateB = (stateB == 0L) ? 0x790B300BF9FE738FL : stateB;
    }

    public long getStateC() {
        return stateC;
    }

    /**
     * Sets the third part of the state. Note that if you call {@link #nextLong()}
     * immediately after this, it will return the given {@code stateC} as-is, so you
     * may want to call some random generation methods (such as nextLong()) and discard
     * the results after setting the state.
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
     * Sets the state completely to the given four state variables, unless stateA or stateB are 0.
     * This is the same as calling {@link #setStateA(long)}, {@link #setStateB(long)},
     * {@link #setStateC(long)}, and {@link #setStateD(long)} as a group. You may want
     * to call {@link #nextLong()} a few times after setting the states like this, unless
     * the value for stateC (in particular) is already adequately random; the first call
     * to {@link #nextLong()}, if it is made immediately after calling this, will return {@code stateC} as-is.
     * @param stateA the first state; can be any long; can be any long except 0
     * @param stateB the second state; can be any long; can be any long except 0
     * @param stateC the third state; this will be returned as-is if the next call is to {@link #nextLong()}
     * @param stateD the fourth state; can be any long
     */
    public void setState(long stateA, long stateB, long stateC, long stateD) {
        this.stateA = (stateA == 0L) ? 0xD3833E804F4C574BL : stateA;
        this.stateB = (stateB == 0L) ? 0x790B300BF9FE738FL : stateB;
        this.stateC = stateC;
        this.stateD = stateD;
    }

    /**
     * Sets the state with three variables, ensuring that the result has states A and B
     * sufficiently separated from each other, while keeping states C and D as given.
     * Note that this does not take a stateB parameter, and instead obtains it by jumping
     * stateA ahead by about 11.4 quintillion steps using {@link #jump(long)}. If stateA is
     * given as 0, this uses 0xD3833E804F4C574BL instead for stateA and 0x790B300BF9FE738FL
     * for stateB. States C and D can each be any long.
     * @param stateA the long value to use for stateA and also used to get stateB; can be any long except 0
     * @param stateC the long value to use for stateC; this will be returned as-is if the next call is to {@link #nextLong()}
     * @param stateD the long value to use for stateD; can be any long
     */
    public void setState (long stateA, long stateC, long stateD) {
        this.stateA = (stateA == 0L) ? 0xD3833E804F4C574BL : stateA;
        this.stateB = jump(this.stateA);
        this.stateC = stateC;
        this.stateD = stateD;
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
        final long fa = this.stateA;
        final long fb = this.stateB;
        final long fc = this.stateC;
        final long fd = this.stateD;
        this.stateA = fb ^ fb << 7;
        this.stateB = fa ^ fa >>> 9;
        this.stateC = Long.rotateLeft(fd, 39) - fb;
        this.stateD = fa - fc + 0xC6BC279692B5C323L;
        return (int)fc >>> (32 - bits);
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
        final long fa = this.stateA;
        final long fb = this.stateB;
        final long fc = this.stateC;
        final long fd = this.stateD;
        this.stateA = fb ^ fb << 7;
        this.stateB = fa ^ fa >>> 9;
        this.stateC = Long.rotateLeft(fd, 39) - fb;
        this.stateD = fa - fc + 0xC6BC279692B5C323L;
        return fc;
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public StrangerRNG copy() {
        return new StrangerRNG(stateA, stateB, stateC, stateD);
    }
    @Override
    public String toString() {
        return "StrangerRNG with stateA 0x" + StringKit.hex(stateA) + "L, stateB 0x" + StringKit.hex(stateB)
                + "L, stateC 0x" + StringKit.hex(stateC) + "L, and stateD 0x" + StringKit.hex(stateD) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StrangerRNG strangerRNG = (StrangerRNG) o;

        return stateA == strangerRNG.stateA && stateB == strangerRNG.stateB && stateC == strangerRNG.stateC
                && stateD == strangerRNG.stateD;
    }

    @Override
    public int hashCode() {
        return (int) (9689L * (stateA ^ (stateA >>> 32)) + 421L * (stateB ^ (stateB >>> 32)) + 29L * (stateC ^ (stateC >>> 32)) + (stateD ^ stateD >>> 32));
    }
}
