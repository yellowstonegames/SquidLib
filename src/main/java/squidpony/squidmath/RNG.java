package squidpony.squidmath;

import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * A wrapper class for working with random number generators in a more friendly
 * way.
 *
 * Includes methods for getting values between two numbers and for getting
 * random elements from a collection or array.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class RNG {

    protected static final double DOUBLE_UNIT = 0x1.0p-53; // 1.0 / (1L << 53)
    protected RandomnessSource random;
    protected double nextNextGaussian;
    protected boolean haveNextNextGaussian = false;
    private Random ran = null;

    /**
     * Default constructor uses Mersenne Twister, which is of high quality and
     * fairly high speed.
     */
    public RNG() {
        this(new MersenneTwister());
    }

    /**
     * Uses the provided source of randomness for all calculations. This
     * constructor should be used if setting the seed is needed as the provided
     * source of randomness can be seeded as desired before passing it in.
     *
     * @param random the source of randomness
     */
    public RNG(RandomnessSource random) {
        this.random = random;
    }

    /**
     * @return a Random instance that can be used for legacy compatability
     */
    public Random asRandom() {
        if (ran == null) {
            ran = new Random() {
                @Override
                protected int next(int bits) {
                    return super.next(bits);
                }
            };
        }
        return ran;
    }

    /**
     * Returns a value from a even distribution from min (inclusive) to max
     * (exclusive).
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    public double between(double min, double max) {
        return min + (max - min) * nextDouble();
    }

    /**
     * Returns a value between min (inclusive) and max (exclusive).
     *
     * The inclusive and exclusive behavior is to match the behavior of the
     * similar method that deals with floating point values.
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    public int between(int min, int max) {
        return nextInt(max - min) + min;
    }

    /**
     * Returns the average of a number of randomly selected numbers from the
     * provided range, with min being inclusive and max being exclusive. It will
     * sample the number of times passed in as the third parameter.
     *
     * The inclusive and exclusive behavior is to match the behavior of the
     * similar method that deals with floating point values.
     *
     * This can be used to weight RNG calls to the average between min and max.
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @param samples the number of samples to take
     * @return the found value
     */
    public int betweenWeighted(int min, int max, int samples) {
        int sum = 0;
        for (int i = 0; i < samples; i++) {
            sum += between(min, max);
        }

        int answer = Math.round((float) sum / samples);
        return answer;
    }

    /**
     * Returns a random element from the provided array and maintains object
     * type.
     *
     * @param <T> the type of the returned object
     * @param array the array to get an element from
     * @return the randomly selected element
     */
    public <T> T getRandomElement(T[] array) {
        if (array.length < 1) {
            return null;
        }
        return array[nextInt(array.length)];
    }

    /**
     * Returns a random element from the provided list. If the list is empty
     * then null is returned.
     *
     * @param <T> the type of the returned object
     * @param list the list to get an element from
     * @return the randomly selected element
     */
    public <T> T getRandomElement(List<T> list) {
        if (list.size() <= 0) {
            return null;
        }
        return list.get(nextInt(list.size()));
    }

    /**
     * Returns a random elements from the provided queue. If the queue is empty
     * then null is returned.
     *
     * @param <T> the type of the returned object
     * @param list the list to get an element from
     * @return the randomly selected element
     */
    public <T> T getRandomElement(Queue<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return (T) list.toArray()[nextInt(list.size())];
    }

    /**
     * @return a value from the gaussian distribution
     */
    public synchronized double nextGaussian() {
        if (haveNextNextGaussian) {
            haveNextNextGaussian = false;
            return nextNextGaussian;
        } else {
            double v1, v2, s;
            do {
                v1 = 2 * nextDouble() - 1; // between -1 and 1
                v2 = 2 * nextDouble() - 1; // between -1 and 1
                s = v1 * v1 + v2 * v2;
            } while (s >= 1 || s == 0);
            double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
            nextNextGaussian = v2 * multiplier;
            haveNextNextGaussian = true;
            return v1 * multiplier;
        }
    }

    /**
     * This returns a maximum of 0.9999999999999999 because that is the largest
     * Double value that is less than 1.0
     *
     * @return a value between 0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    public double nextDouble() {
        return (((long) (next(26)) << 27) + next(27)) * DOUBLE_UNIT;
    }

    /**
     * This returns a random double between 0.0 (inclusive) and max (exclusive).
     *
     * @return a value between 0 (inclusive) and max (exclusive)
     */
    public double nextDouble(double max) {
        return (((long) (next(26)) << 27) + next(27)) * DOUBLE_UNIT * max;
    }

    /**
     * This returns a maximum of 0.99999994 because that is the largest Float
     * value that is less than 1.0f
     *
     * @return a value between 0 (inclusive) and 0.99999994 (inclusive)
     */
    public float nextFloat() {
        return next(24) / ((float) (1 << 24));
    }

    public boolean nextBoolean() {
        return next(1) != 0;
    }

    public long nextLong() {
        return ((long) (next(32)) << 32) + next(32);
    }

    /**
     * Returns a random integer below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    public int nextInt(int bound) {
        if (bound <= 0) {
            return 0;
        }

        int r = next(31);
        int m = bound - 1;
        if ((bound & m) == 0) { // i.e., bound is a power of 2
            r = (int) ((bound * (long) r) >> 31);
        } else {
            for (int u = r; u - (r = u % bound) + m < 0; u = next(31)) {
            }
        }
        return r;
    }

    public int nextInt() {
        return next(32);
    }

    public int next(int bits) {
        return random.next(bits);
    }

    public RandomnessSource getRandomness() {
        return random;
    }

    public void setRandomness(RandomnessSource random) {
        this.random = random;
    }


}
