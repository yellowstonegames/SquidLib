/*
 * Copyright 2005, Nick Galbreath -- nickg [at] modp [dot] com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *   Neither the name of the modp.com nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This is the standard "new" BSD license:
 * http://www.opensource.org/licenses/bsd-license.php
 *
 * Portions may also be
 * Copyright (C) 2004, Makoto Matsumoto and Takuji Nishimura,
 * All rights reserved.
 * (and covered under the BSD license)
 * See http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/VERSIONS/C-LANG/mt19937-64.c
 */
package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Mersenne Twister, 64-bit version as a RandomnessSource.
 * <br>
 * Similar to the regular 32-bit Mersenne Twister but implemented with 64-bit
 * values (Java <code>long</code>), and with different output. This generator is probably
 * not the best to use because of known statistical problems and low speed, but its period
 * is absurdly high, {@code pow(2, 19937) - 1}. {@link LongPeriodRNG} has significantly
 * better speed and statistical quality, and also has a large period, {@code pow(2, 1024) - 1}.
 * {@link IsaacRNG} is slower, but offers impeccable quality, and from its webpage, "Cycles
 * are guaranteed to be at least {@code pow(2, 40)} values long, and they are
 * {@code pow(2, 8295)} values long on average." IsaacRNG should be your choice if security is a
 * concern, LongPeriodRNG if quality and speed are important, and MersenneTwister should be used
 * if period is the only criterion to judge an RNG on. There may be a CMWC generator added at
 * some point, which would have potentially a greater period than the Mersenne Twister.
 * <br>
 * This is mostly a straight port of the
 * <a href="http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/VERSIONS/C-LANG/mt19937-64.c">
 * C-code (mt19937-64.c)</a>, but made more "java-like". This version was originally found
 * at <a href="https://github.com/javabeanz/javarng/blob/master/com/modp/random/MersenneTwister64.java">
 * an archived version of a Google Code repo</a>, and it is licensed under the 3-clause BSD license, like
 * the Mersenne Twister.
 * <br>
 * References:
 * <br>
 * <ul>
 * <li>
 * <a href="http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/emt.html">
 * The Mersenne Twister Home Page </a>
 * </li>
 * <li>  Makato Matsumoto and Takuji Nishimura,
 * <a href="http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/ARTICLES/mt.pdf">"Mersenne Twister:A 623-Dimensionally Equidistributed Uniform Pseudo-Random Number Generator"</a>,
 * <i>ACM Transactions on Modeling and Computer Simulation, </i> Vol. 8, No. 1,
 * January 1998, pp 3--30.</li>
 * </ul>
 *
 * @author Nick Galbreath -- nickg [at] modp [dot] com
 * @author Tommy Ettinger
 * @version 1.1 -- 07-Oct-2017
 */
public class MersenneTwister implements Serializable, RandomnessSource {

    private static final long serialVersionUID = 1001000L;

    private static final int NN = 312;

    private static final int MM = 156;

    private static final long MATRIX_A = 0xB5026F5AA96619E9L;

    /**
     * Mask: Most significant 33 bits
     */
    private static final long UM = 0xFFFFFFFF80000000L;

    /**
     * Mask: Least significant 31 bits
     */
    private static final long LM = 0x7FFFFFFFL;

    /**
     * Mersenne Twister data.
     */
    private long[] mt = new long[NN];

    /**
     * Mersenne Twister Index.
     */
    private int mti = NN + 1;

    /**
     * Internal to hold 64 bits, that might
     * used to generate two 32 bit values.
     */
    private long extra;

    /**
     * Set to true if we need to generate another 64 bits, false if we have enough bits available for an int.
     */
    private boolean bitState = true;

    /**
     * Seeds this using two calls to Math.random().
     */
    public MersenneTwister() {
        setSeed((long) ((Math.random() * 2.0 - 1.0) * 0x8000000000000L)
                ^ (long) ((Math.random() * 2.0 - 1.0) * 0x8000000000000000L));
    }

    /**
     * Seeds this with the given long, which will be used to affect the large state.
     *
     * @param seed any long
     */
    public MersenneTwister(final long seed) {
        setSeed(seed);
    }

    /**
     * Seeds this with the given long array, which will be used to affect the large state, and not used directly.
     *
     * @param seed a long array; generally should be non-null
     */
    public MersenneTwister(final long[] seed) {
        setSeed(seed);
    }

    /**
     * Initalize the pseudo random number generator with 64 bits.
     * Not the same as setState() in StatefulRandomness; this changes the seed quite a bit.
     *
     * @param seed any long
     */
    public void setSeed(final long seed) {
        mt[0] = seed;
        for (mti = 1; mti < NN; mti++) {
            mt[mti] = (6364136223846793005L * (mt[mti - 1] ^ (mt[mti - 1] >>> 62)) + mti);
        }
    }

    /**
     * Initalize the pseudo random number generator with a long array of any size, which should not be null but can be.
     * Not the same as setState() in StatefulRandomness; this changes the seed quite a bit.
     *
     * @param array any long array
     */
    public void setSeed(final long[] array) {
        setSeed(19650218L);
        if (array == null)
            return;
        int i = 1;
        int j = 0;
        int k = (NN > array.length ? NN : array.length);
        for (; k != 0; k--) {
            mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >>> 62)) * 3935559000370003845L))
                    + array[j] + j;
            i++;
            j++;
            if (i >= NN) {
                mt[0] = mt[NN - 1];
                i = 1;
            }
            if (j >= array.length)
                j = 0;
        }
        for (k = NN - 1; k != 0; k--) {
            mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >>> 62)) * 2862933555777941757L))
                    - i;
            i++;
            if (i >= NN) {
                mt[0] = mt[NN - 1];
                i = 1;
            }
        }

        mt[0] = 0x8000000000000000L; /* MSB is 1; assuring non-zero initial array */
    }

    /**
     * Returns up to 32 random bits.
     * <br>
     * The implementation splits a 64-bit long into two 32-bit chunks.
     * @param bits the number of bits to output, between 1 and 32 (both inclusive)
     * @return an int with the specified number of pseudo-random bits
     */
    @Override
    public int next(final int bits) {
        //return ((int)nextLong()) >>> (32 - numbits);

        if (bitState) {
            extra = nextLong();
            bitState = false;
            return (int) (extra >>> (64 - bits));
        } else {
            bitState = true;
            return ((int) extra) >>> (32 - bits);
        }
    }

    /**
     * Returns 64 random bits.
     * @return a pseudo-random long, which can have any 64-bit value, positive or negative
     */
    @Override
    public long nextLong() {
        int i;
        long x;
        if (mti >= NN) { /* generate NN words at one time */

            for (i = 0; i < NN - MM; i++) {
                x = (mt[i] & UM) | (mt[i + 1] & LM);
                mt[i] = mt[i + MM] ^ (x >>> 1) ^ (x & 1L) * MATRIX_A;
            }
            for (; i < NN - 1; i++) {
                x = (mt[i] & UM) | (mt[i + 1] & LM);
                mt[i] = mt[i + (MM - NN)] ^ (x >>> 1) ^ (x & 1L) * MATRIX_A;
            }
            x = (mt[NN - 1] & UM) | (mt[0] & LM);
            mt[NN - 1] = mt[MM - 1] ^ (x >>> 1) ^ (x & 1L) * MATRIX_A;

            mti = 0;
        }

        x = mt[mti++];

        x ^= (x >>> 29) & 0x5555555555555555L;
        x ^= (x << 17) & 0x71D67FFFEDA60000L;
        x ^= (x << 37) & 0xFFF7EEE000000000L;
        x ^= (x >>> 43);

        return x;
    }

    @Override
    public final MersenneTwister copy() {
        MersenneTwister f = new MersenneTwister(MATRIX_A);
        f.mti = mti;
        f.extra = extra;
        f.bitState = bitState;
        System.arraycopy(mt, 0, f.mt, 0, mt.length);
        return f;
    }

    @Override
    public String toString() {
        return "MersenneTwister with state hashed as " + StringKit.hexHash(mt) +
                " and index " + mti;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MersenneTwister mt64RNG = (MersenneTwister) o;

        return mti == mt64RNG.mti && extra == mt64RNG.extra && bitState == mt64RNG.bitState && Arrays.equals(mt, mt64RNG.mt);
    }

    @Override
    public int hashCode() {
        int result = CrossHash.hash(mt);
        result = 31 * result + mti;
        result = 31 * result + (int) (extra ^ (extra >>> 32));
        result = 31 * result + (bitState ? 1 : 0);
        return result;
    }

}