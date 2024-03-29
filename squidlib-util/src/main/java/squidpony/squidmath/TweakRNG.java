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

import java.io.Serializable;

/**
 * Somewhat experimental RNG that can be configured to smoothly transition between producing mostly values in the
 * center of its range, to producing more values at or near the extremes, as well as favoring high or low results.
 * The probability distribution is... unusual, with lumps that rise or fall based on centrality.
 * <br>
 * Even though this is experimental, it's still usable. Mostly the useful parts of this relate to changing centrality,
 * making results occur more or less frequently in the center of the output range. You can also change the "favor" to
 * bias results towards higher or lower parts of the same output range, though if favor is non-zero it may have
 * counterintuitive results for {@link #nextLong()}.
 * <br>
 * Internally, this acts as a {@link TangleRNG}, which is a fairly solid, very fast algorithm, and uses its results two
 * at a time to give to an atan2 calculation (specifically, {@link NumberTools#atan2_(float, float)} or
 * {@link NumberTools#atan2_(double, double)}. These particular approximations of atan2 range from 0.0 to 1.0 instead of
 * -pi to pi. This means atan2 inputs with positive x and small y are likely to return values near 1.0 or near 0.0, but
 * not between 0.25 and 0.75. The opposite is true for inputs with negative x and small y; that is likely to be near 0.5
 * and can't be between 0.0 and 0.25 or between 0.75 and 1.0. TweakRNG uses this property to implement centrality,
 * changing the inputs to its internal atan2 usage so x is positive when centrality is positive, or negative when
 * centrality is negative. Likewise, favor is implemented by changing y, though reversed; positive favor makes the atan2
 * calculation adjusted with negative y, making it more likely to be between 0.5 and 1.0, while negative favor pushes it
 * back to between 0.0 and 0.5.
 * <br>
 * <a href="https://i.imgur.com/VCvtlSc.gifv">Here's an animation of the distribution graph changing.</a>
 * <br>
 * Created by Tommy Ettinger on 10/6/2019.
 */
public class TweakRNG extends AbstractRNG implements Serializable {

    private static final long serialVersionUID = 1L;

    private long stateA, stateB, centrality, favor;
    public TweakRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                0L);
    }

    public TweakRNG(long seed) {
        this((seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25, 
                ((seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25),
                0L);
    }

    public TweakRNG(final long seedA, final long seedB, final long centrality) {
        stateA = seedA;
        stateB = seedB | 1L;
        this.centrality = centrality % 0x10000L;
        this.favor = 0L;
    }

    public TweakRNG(final long seedA, final long seedB, final long centrality, final long favor) {
        stateA = seedA;
        stateB = seedB | 1L;
        this.centrality = centrality % 0x10000L;
        this.favor = favor % 0x10000L;
    }

    @Override
    public int next(int bits) {
        return (int)(nextLong() >>> 64 - bits);
    }

    @Override
    public int nextInt() {
        return (int) (nextLong() >>> 32);
    }

    @Override
    public long nextLong() {
        final long r = internalLong(), s = internalLong();
        return (long) (NumberTools.atan2_(((r & 0xFF) - (r >>> 8 & 0xFF)) * ((r >>> 16 & 0xFF) - (r >>> 24 & 0xFF)) - (double)favor,
                ((r >>> 32 & 0xFF) - (r >>> 40 & 0xFF)) * ((r >>> 48 & 0xFF) - (r >>> 56 & 0xFF)) + centrality) * 0x6000000000000000L)
                + (s & 0x1FFFFFFFFFFFFFFFL) << 1 ^ (s >>> 63);
    }

    @Override
    public boolean nextBoolean() {
        return nextLong() < 0;
    }
    
    @Override
    public double nextDouble() {
        final long r = internalLong();
        return NumberTools.atan2_(((r & 0xFF) - (r >>> 8 & 0xFF)) * ((r >>> 16 & 0xFF) - (r >>> 24 & 0xFF)) - favor,
                ((r >>> 32 & 0xFF) - (r >>> 40 & 0xFF)) * ((r >>> 48 & 0xFF) - (r >>> 56 & 0xFF)) + (double)centrality) * 0.75
                + (internalLong() & 0xfffffffffffffL) * 0x1p-54;
    }

    @Override
    public float nextFloat() {
        final long r = internalLong();
        return NumberTools.atan2_(((r & 0xFF) - (r >>> 8 & 0xFF)) * ((r >>> 16 & 0xFF) - (r >>> 24 & 0xFF)) - favor,
                ((r >>> 32 & 0xFF) - (r >>> 40 & 0xFF)) * ((r >>> 48 & 0xFF) - (r >>> 56 & 0xFF)) + centrality) * 0.75f
                + (internalLong() & 0x7fffffL) * 0x1p-25f;
    }
    
    public TweakRNG copy() {
        return this;
    }

    @Override
    public Serializable toSerializable() {
        return this;
    }

    public long getStateA() {
        return stateA;
    }

    public void setStateA(long stateA) {
        this.stateA = stateA;
    }

    public long getStateB() {
        return stateB;
    }

    public void setStateB(long stateB) {
        this.stateB = stateB | 1L;
    }

    public long getCentrality() {
        return centrality;
    }

    /**
     * Adjusts the central bias of this TweakRNG, often to positive numbers (which bias toward the center of the range),
     * but also often to negative numbers (which bias toward extreme values, though still within the range).
     * @param centrality should be between -65535 and 65535; positive values bias toward the center of the range
     */
    public void setCentrality(long centrality) {
        this.centrality = centrality % 65536L;
    }


    public long getFavor() {
        return favor;
    }

    /**
     * Adjusts the value bias of this TweakRNG, often to positive numbers (which bias toward higher results), but also
     * often to negative numbers (which bias toward lower results). All results will still be in their normal range, but
     * will change how often high or low values occur. Unusual results will occur if favor is non-zero and you get a
     * long with {@link #nextLong()}; in that case, the values are treated as higher when unsigned, so positive favor
     * makes both high positive values and all negative values more common. Doubles and floats will behave normally.
     * @param favor should be between -65535 and 65535; positive values bias toward higher (unsigned for longs) results
     */
    public void setFavor(long favor) {
        this.favor = favor % 65536L;
    }

    /**
     *{@link TangleRNG}'s algorithm; not all longs will be returned by any individual generator, but all generators as a
     * whole will return all longs with equal likelihood.
     * @return a random long in the full range; each state is advanced by 1 step.
     */
    private long internalLong()
    {
        final long s = (stateA += 0xC6BC279692B5C323L);
        final long z = (s ^ s >>> 31) * (stateB += 0x9E3779B97F4A7C16L);
        return z ^ z >>> 26 ^ z >>> 6;
    }
}
