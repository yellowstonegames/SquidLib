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

/**
 * An IDistribution that produces results between -1.0 inclusive and 1.0 exclusive, but is much more likely to produce
 * results near 0.0, and does not "round off" like a Gaussian curve around the midpoint.
 * <br>
 * Created by Tommy Ettinger on 11/23/2019.
 */
public class SpikeDistribution implements IDistribution {
    public static final SpikeDistribution instance = new SpikeDistribution();
    /**
     * Gets a double between {@link #getLowerBound()} and {@link #getUpperBound()} that obeys this distribution.
     *
     * @param rng an IRNG, such as {@link RNG} or {@link GWTRNG}, that this will get one or more random numbers from
     * @return a double within the range of {@link #getLowerBound()} and {@link #getUpperBound()}
     */
    @Override
    public double nextDouble(IRNG rng) {
        final double d = (rng.nextDouble() - 0.5) * 2.0;
        return d * d * d;
    }

    /**
     * Gets the lower bound of the distribution, which is -1, inclusive.
     * @return the lower bound of the distribution
     */
    @Override
    public double getLowerBound() {
        return -1.0;
    }

    /**
     * Gets the upper bound of the distribution, which is 1, exclusive.
     *
     * @return the upper bound of the distribution
     */
    @Override
    public double getUpperBound() {
        return 1.0;
    }

    /**
     * A variant on SpikeDistribution that has its range shrunk and moved from {@code [-1,1)} to {@code [0,1)}. It is a
     * {@link squidpony.squidmath.IDistribution.SimpleDistribution}, and the spike is centered on 0.5.
     */
    public static class SimpleSpikeDistribution extends SimpleDistribution implements IDistribution
    {
        /**
         * Gets a double between {@link #getLowerBound()} and {@link #getUpperBound()} that obeys this distribution.
         *
         * @param rng an IRNG, such as {@link RNG} or {@link GWTRNG}, that this will get one or more random numbers from
         * @return a double within the range of {@link #getLowerBound()} and {@link #getUpperBound()}
         */
        @Override
        public double nextDouble(IRNG rng) {
            final double d = (rng.nextDouble() - 0.5) * 2.0;
            return d * d * d * 0.5 + 0.5;
        }
    }
}
