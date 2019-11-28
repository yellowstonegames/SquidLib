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
     * @return a double within the range of {@link #getLowerBound()} and {@link #getUpperBound()}
     */
    double nextDouble(IRNG rng);

    /**
     * Gets the lower bound of the distribution. The documentation should specify whether the bound is inclusive or
     * exclusive; if unspecified, it can be assumed to be inclusive (like {@link IRNG#nextDouble()}).
     * @return the lower bound of the distribution
     */
    double getLowerBound();
    /**
     * Gets the upper bound of the distribution. The documentation should specify whether the bound is inclusive or
     * exclusive; if unspecified, it can be assumed to be exclusive (like {@link IRNG#nextDouble()}).
     * @return the upper bound of the distribution
     */
    double getUpperBound();
    
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
         * Using the offset allows distributions like {@link GaussianDistribution} to become centered halfway on 0.5,
         * making the result of this distribution have a Gaussian-like peak on 0.5 instead of of peaking at the bounds
         * when offset is 0.0.
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
         * centered on 0.0.
         * @param otherDistribution any other IDistribution
         * @return a new anonymous implementation of SimpleDistribution that clamps {@code otherDistribution} in range.
         */
        public static SimpleDistribution clampedDistribution(final IDistribution otherDistribution)
        {
            return new SimpleDistribution() {
                @Override
                public double nextDouble(IRNG rng) {
                    return Math.max(0.0, Math.min(0.9999999999999999, otherDistribution.nextDouble(rng)));
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
         * Gets the upper exclusive bound of the distribution, which is 1.0.
         *
         * @return the upper exclusive bound of the distribution, 1.0
         */
        @Override
        public double getUpperBound() {
            return 1.0;
        }
    }
}
