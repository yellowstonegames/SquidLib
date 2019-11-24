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
    double nextDouble(IRNG rng);
}
