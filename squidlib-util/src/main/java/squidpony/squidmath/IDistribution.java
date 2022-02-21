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
 * A way to take an {@link IRNG} and get one or more random numbers from it to produce a double in some statistical
 * distribution, such as Gaussian (also called the normal distribution), exponential, or various simpler schemes that
 * don't have common mathematical names. An example of the last category is "spike" for a distribution that is very
 * likely to be 0 and quickly drops off to being less likely for positive or negative results between 0 and -1 or 1, or
 * "bathtub" for the "spike" distribution's fractional part from 0 to 1 (which is likely to be 0 or 1 and very unlikely
 * to be near 0.5).
 * Created by Tommy Ettinger on 11/23/2019.
 */
public interface IDistribution {
    /**
     * Gets a double between {@link #getLowerBound()} and {@link #getUpperBound()} that obeys this distribution.
     * @param rng an IRNG, such as {@link RNG} or {@link GWTRNG}, that this will get one or more random numbers from
     * @return a double within the range of {@link #getLowerBound()} and {@link #getUpperBound()}, both inclusive
     */
    double nextDouble(IRNG rng);

    /**
     * Gets the lower inclusive bound of the distribution. If the bound is exclusive above 0.0, then this will be
     * {@link #EXCLUSIVE_ZERO}.
     * @return the lower inclusive bound of the distribution
     */
    double getLowerBound();
    /**
     * Gets the upper inclusive bound of the distribution. If the bound is exclusive below 1.0, like
     * {@link IRNG#nextDouble()}), then this will be {@link #EXCLUSIVE_ONE}.
     * @return the upper inclusive bound of the distribution
     */
    double getUpperBound();

    /**
     * Gets the mean value of this distribution.
     * @return the mean value of this distribution
     */
    double getMean();

    /**
     * A double that is greater than 0.0 by the smallest representable amount. Equivalent to {@link Double#MIN_VALUE}.
     */
    double EXCLUSIVE_ZERO = Double.MIN_VALUE;
    /**
     * A double that is less than 1.0 by the smallest representable amount. Equivalent to {@code 0.9999999999999999}.
     */
    double EXCLUSIVE_ONE = 0.9999999999999999;

    /**
     * An abstract IDistribution that always has a lower bound of 0.0 and an upper bound of {@link #EXCLUSIVE_ONE},
     * matching the bounds of {@link IRNG#nextDouble()}. This provides methods to create SimpleDistribution instances
     * from arbitrary IDistribution instances.
     */
    abstract class SimpleDistribution implements IDistribution {

        /**
         * Makes a new SimpleDistribution implementation given any IDistribution (typically one with large or infinite
         * bounds) by getting the fractional component of a result from {@code otherDistribution}.
         * @param otherDistribution any other IDistribution
         * @return a new anonymous implementation of SimpleDistribution that gets the fractional part of {@code otherDistribution}.
         */
        public static SimpleDistribution fractionalDistribution(final IDistribution otherDistribution)
        {
            return new SimpleDistribution() {
                @Override
                public double nextDouble(IRNG rng) {
                    final double v = otherDistribution.nextDouble(rng);
                    return v - (v >= 0.0 ? (int) v : (int)v - 1);
                }
            };
        }
        /**
         * Makes a new SimpleDistribution implementation given any IDistribution (typically one with large or infinite
         * bounds) by getting the fractional component of {@code offset} plus a result from {@code otherDistribution}.
         * Using the offset allows distributions like {@link GaussianDistribution}, which are centered on 0.0, to become
         * centered halfway on 0.5, making the result of this distribution have a Gaussian-like peak on 0.5 instead of
         * peaking at the bounds when offset is 0.0.
         * @param otherDistribution any other IDistribution
         * @return a new anonymous implementation of SimpleDistribution that gets the fractional part of {@code otherDistribution}.
         */
        public static SimpleDistribution fractionalOffsetDistribution(final IDistribution otherDistribution, final double offset)
        {
            return new SimpleDistribution() {
                @Override
                public double nextDouble(IRNG rng) {
                    final double v = otherDistribution.nextDouble(rng) + offset;
                    return v - (v >= 0.0 ? (int) v : (int)v - 1);
                }
            };
        }

        /**
         * Makes a new SimpleDistribution implementation given any IDistribution (typically one with large or infinite
         * bounds) by simply clamping results that are below 0 to 0 and at least 1 to 0.9999999999999999 (the largest
         * double less than 1.0 than can be represented). This will behave very oddly for distributions that are
         * centered on 0.0; for those you probably want {@link #fractionalOffsetDistribution(IDistribution, double)}.
         * @param otherDistribution any other IDistribution
         * @return a new anonymous implementation of SimpleDistribution that clamps {@code otherDistribution} in range.
         */
        public static SimpleDistribution clampedDistribution(final IDistribution otherDistribution)
        {
            return new SimpleDistribution() {
                @Override
                public double nextDouble(IRNG rng) {
                    return Math.max(0.0, Math.min(EXCLUSIVE_ONE, otherDistribution.nextDouble(rng)));
                }
            };
        }

        /**
         * Gets the lower inclusive bound, which is 0.0.
         *
         * @return the lower inclusive bound of the distribution, 0.0
         */
        @Override
        public double getLowerBound() {
            return 0.0;
        }

        /**
         * Gets the upper inclusive bound of the distribution, which is 0.9999999999999999 ({@link #EXCLUSIVE_ONE}).
         *
         * @return the upper inclusive bound of the distribution, 0.9999999999999999
         */
        @Override
        public double getUpperBound() {
            return EXCLUSIVE_ONE;
        }

        /**
         * Gets the mean value of this distribution, which by default is {@link Double#NaN}. Implementing classes are
         * strongly advised to override this and provide a meaningful estimate (at least) of the mean.
         * @return the mean value of this distribution (by default, {@link Double#NaN})
         */
        @Override
        public double getMean(){
            return Double.NaN;
        }
    }
}
