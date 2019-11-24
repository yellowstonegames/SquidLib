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
}
