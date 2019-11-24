package squidpony.squidmath;

/**
 * An IDistribution that implements the <a href="https://en.wikipedia.org/wiki/Exponential_distribution">Exponential
 * distribution</a>. Takes lambda as a parameter during construction (default 1), and lambda also has getters/setters.
 * <br>
 * Created by Tommy Ettinger on 11/23/2019.
 */
public class ExponentialDistribution implements IDistribution {
    private double i_lambda = 1.0;
    public ExponentialDistribution()
    {
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
}
