package squidpony.squidmath;

import java.io.Serializable;
import java.util.Random;

/**
 * This makes java.util.Random available for testing purposes.
 * It is relevant mainly as example code, or if you want to
 * compare what your results would have been without using a
 * better RNG. Results might not be apparent in some cases,
 * although the terrible performance of java.util.Random is
 * likely to be the first thing a user notices if this is
 * used heavily (i.e. to generate white noise with one call
 * to {@link #nextDouble()} per cell).
 * @author Ben McLean
 */
public class JavaRNG implements RandomnessSource, Serializable
{
    public Random random;

    /** Creates a new generator seeded using Math.random. */
    public JavaRNG() { this((long) Math.floor(Math.random() * Long.MAX_VALUE)); }

    public JavaRNG( final long seed ) { this.random = new Random(seed); }

    public JavaRNG( final Random random ) { this.random = random; }

    @Override
    public int next( int bits ) {
        return random.nextInt() >>> (32 - bits);
        // return random.next(bits);
    }

    @Override
    public long nextLong() { return random.nextLong(); }

    @Override
    public JavaRNG copy() { return new JavaRNG(random); }

    public int nextInt() { return random.nextInt(); }

    public int nextInt( final int bound ) { return random.nextInt(bound); }
    /**
     * Inclusive lower, exclusive upper.
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, should be positive, must be greater than lower
     * @return a random int at least equal to lower and less than upper
     */
    public int nextInt( final int lower, final int upper ) {
        if ( upper - lower <= 0 ) throw new IllegalArgumentException("Upper bound must be greater than lower bound");
        return lower + nextInt(upper - lower);
    }

    public double nextDouble() { return random.nextDouble(); }
    public double nextDouble(final double outer) {
        return nextDouble() * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public float nextFloat() { return random.nextFloat(); }
    /**
     * Gets a random value, true or false.
     * @return a random true or false value.
     */
    public boolean nextBoolean() { return ( random.nextBoolean()); }

    /**
     * Given a byte array as a parameter, this will fill the array with random bytes (modifying it
     * in-place).
     */
    public void nextBytes( final byte[] bytes ) { random.nextBytes(bytes); }

    @Override
    public String toString() {
        return "JavaRNG wrapping java.util.Random with id " + System.identityHashCode(random);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaRNG javaRNG = (JavaRNG) o;

        return random.equals(javaRNG.random);
    }

    @Override
    public int hashCode() {
        return random.hashCode() * 31;
    }
}
