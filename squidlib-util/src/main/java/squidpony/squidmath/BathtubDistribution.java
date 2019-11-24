package squidpony.squidmath;

/**
 * An IDistribution that produces results between 0.0 inclusive and 1.0 exclusive, but is much more likely to produce
 * results near 0.0 or 1.0, further from 0.5.
 * <br>
 * Created by Tommy Ettinger on 11/23/2019.
 */
public class BathtubDistribution implements IDistribution {
    public static final BathtubDistribution instance = new BathtubDistribution();
    /**
     * Gets a double between {@link #getLowerBound()} and {@link #getUpperBound()} that obeys this distribution.
     *
     * @param rng an IRNG, such as {@link RNG} or {@link GWTRNG}, that this will get one or more random numbers from
     * @return a double within the range of {@link #getLowerBound()} and {@link #getUpperBound()}
     */
    @Override
    public double nextDouble(IRNG rng) {
        double d = (rng.nextDouble() - 0.5) * 2.0;
        d = d * d * d + 1.0;
        return d - (int)d;
    }

    /**
     * Gets the lower bound of the distribution, which is 0, inclusive.
     * @return the lower bound of the distribution
     */
    @Override
    public double getLowerBound() {
        return 0.0;
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
}
