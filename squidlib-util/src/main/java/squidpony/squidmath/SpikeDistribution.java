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
        return -1;
    }

    /**
     * Gets the upper bound of the distribution, which is 1, exclusive.
     *
     * @return the upper bound of the distribution
     */
    @Override
    public double getUpperBound() {
        return 1;
    }
}
