package squidpony.squidmath;

/**
 * An IDistribution that allows a parameter to determine how many calls to {@link IRNG#nextDouble()} to make and average
 * whenever a double is requested. When this parameter {@code degree} is 1, this is uniform; when it is 2, this is a
 * triangular distribution, and when it is 3 or more it is an increasingly centralized bell curve. The average value is
 * always very close to 0.5, and the bounds are the same as {@link IRNG#nextDouble()}.
 * <br>
 * Created by Tommy Ettinger on 11/27/2019.
 */
public class CurvedBoundedDistribution implements IDistribution {
    public static final CurvedBoundedDistribution instance = new CurvedBoundedDistribution(3);
    public static final CurvedBoundedDistribution instanceTriangular = new CurvedBoundedDistribution(2);
    public static final CurvedBoundedDistribution instanceGaussianLike = new CurvedBoundedDistribution(6);
    private int degree;
    private double i_degree;
    public CurvedBoundedDistribution()
    {
        this(3);
    }
    public CurvedBoundedDistribution(int degree)
    {
        this.degree = Math.max(degree, 1);
        i_degree = 1.0 / this.degree;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = Math.max(degree, 1);
        i_degree = 1.0 / this.degree;
    }

    @Override
    public double nextDouble(IRNG rng) {
        double sum = 0.0;
        for (int i = 0; i < degree; i++) {
            sum += rng.nextDouble();
        }
        return sum  * i_degree;
    }
    /**
     * The lower inclusive bound is 0.0.
     * @return zero.
     */
    @Override
    public double getLowerBound() {
        return 0.0;
    }

    /**
     * The upper exclusive bound is 1.0.
     * @return 1.0.
     */
    @Override
    public double getUpperBound() {
        return 1.0;
    }
}
