package squidpony.squidmath;

import java.io.Serializable;

/**
 * This implements the random number function from the "Random Number" XKCD comic. https://xkcd.com/221/
 * See explanation here: https://www.explainxkcd.com/wiki/index.php/221:_Random_Number
 */
public class XkcdRNG implements RandomnessSource, Serializable {
    public XkcdRNG() {
    }

    public XkcdRNG(final long seed) {
    }

    @Override
    public int next(int bits) {
        return 4;   // chosen by fair dice roll.
        // guaranteed to be random.
    }

    @Override
    public long nextLong() {
        return 4;   // chosen by fair dice roll.
        // guaranteed to be random.
    }

    @Override
    public RandomnessSource copy() {
        return new XkcdRNG();
    }

    public int nextInt() {
        return 4;   // chosen by fair dice roll.
        // guaranteed to be random.
    }

    /**
     * Inclusive lower, exclusive upper.
     *
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, should be positive, must be greater than lower + 4
     * @return a random int at least equal to lower + 4 and less than upper
     */
    public int nextInt(final int lower, final int upper) {
        if (upper - lower < 4) throw new IllegalArgumentException("Upper bound must be greater than lower bound + 4");
        return lower + 4;
    }

    public double nextDouble() {
        return 4d / 6d;   // chosen by fair dice roll.
        // guaranteed to be random.
    }

    public double nextDouble(final double outer) {
        return nextDouble() * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     *
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public float nextFloat() {
        return 4f / 6f;   // chosen by fair dice roll.
        // guaranteed to be random.
    }

    /**
     * Gets a random value, true or false.
     *
     * @return a random true or false value.
     */
    public boolean nextBoolean() {
        return true;   // chosen by fair coin flip.
        // guaranteed to be random.
    }

    @Override
    public String toString() {
        return "XKCD Random Number Generator";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XkcdRNG xkcdRNG = (XkcdRNG) o;

        return hashCode() == xkcdRNG.hashCode();
    }

    @Override
    public int hashCode() {
        return 4;
    }
}
