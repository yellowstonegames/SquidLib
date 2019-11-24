package squidpony.squidmath;

/**
 * An IDistribution that implements the <a href="https://en.wikipedia.org/wiki/Exponential_distribution">Exponential
 * distribution</a>. Takes lambda as a parameter during construction (default 1), and lambda also has getters/setters.
 * <br>
 * Created by Tommy Ettinger on 11/23/2019.
 */
public class ExponentialDistribution implements IDistribution {
    public static final ExponentialDistribution instance = new ExponentialDistribution();
    public static final ExponentialDistribution instance_0_5 = new ExponentialDistribution(0.5);
    public static final ExponentialDistribution instance_1_5 = new ExponentialDistribution(1.5);
    private double i_lambda;
    public ExponentialDistribution()
    {
        i_lambda = 1.0;
    }
    public ExponentialDistribution(double lambda)
    {
        i_lambda = 1.0 / lambda;
    }

    public double getLambda() {
        return 1.0 / i_lambda;
    }

    public void setLambda(double lambda) {
        this.i_lambda = 1.0 / lambda;
    }

    @Override
    public double nextDouble(IRNG rng) {
        return Math.log(1 - rng.nextDouble()) * i_lambda;
    }
    /**
     * The lower inclusive bound is 0 while lambda is positive; it is negative infinity if lambda is negative.
     * @return zero, or negative infinity if lambda is negative.
     */
    @Override
    public double getLowerBound() {
        return i_lambda < 0.0 ? Double.NEGATIVE_INFINITY : 0.0;
    }

    /**
     * The upper inclusive bound is infinity while lambda is positive; it is 0 if lambda is negative.
     * @return positive infinity, or zero if lambda is negative.
     */
    @Override
    public double getUpperBound() {
        return i_lambda < 0.0 ? 0.0 : Double.POSITIVE_INFINITY;
    }
}
