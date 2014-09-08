package squidpony.squidmath;

import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * A wrapper class for working with random number generators in a more friendly way.
 *
 * Includes methods for getting values between two numbers and for getting random elements from a collection or array.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class RNG {

    private static final double DOUBLE_UNIT = 0x1.0p-53; // 1.0 / (1L << 53)
    private RandomnessSource random;
    private double nextNextGaussian;
    private boolean haveNextNextGaussian = false;
    private Random ran = null;

    /**
     * Default constructor uses Mersenne Twister, which is of high quality and fairly high speed.
     */
    public RNG() {
        this(new MersenneTwister());
    }

    /**
     * Uses the provided source of randomness for all calculations. This constructor should be used if setting the seed
     * is needed as the provided source of randomness can be seeded as desired before passing it in.
     *
     * @param random
     */
    public RNG(RandomnessSource random) {
        this.random = random;
    }

    /**
     * Returns a Random instance that can be used for legacy compatability.
     *
     * @return
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
     * Returns a value from a even distribution from min (inclusive) to max (exclusive).
     *
     * @param min
     * @param max
     * @return
     */
    public double between(double min, double max) {
        return min + (max - min) * nextDouble();
    }

    /**
     * Returns a value between min (inclusive) and max (exclusive).
     *
     * The inclusive and exclusive behavior is to match the behavior of the similar method that deals with floating
     * point values.
     *
     * @param min
     * @param max
     * @return
     */
    public int between(int min, int max) {
        return nextInt(max - min) + min;
    }

    /**
     * Returns the average of a number of randomly selected numbers from the provided range, with min being inclusive
     * and max being exclusive. It will sample the number of times passed in as the third parameter.
     *
     * The inclusive and exclusive behavior is to match the behavior of the similar method that deals with floating
     * point values.
     *
     * This can be used to weight RNG calls to the average between min and max.
     *
     * @param min
     * @param max
     * @param samples
     * @return
     */
    public int betweenWeighted(int min, int max, int samples) {
        int sum = 0;
        for (int i = 0; i < samples; i++) {
            sum += between(min, max);
        }

        int answer = Math.round((float) sum / samples);
        return answer;
    }

    public <T> T getRandomElement(T[] array) {
        if (array.length < 1) {
            return null;
        }
        return array[nextInt(array.length)];
    }

    /**
     * Returns a random element from the provided list. If the list is empty then null is returned.
     *
     * @param <T>
     * @param list
     * @return
     */
    public <T> T getRandomElement(List<T> list) {
        if (list.size() <= 0) {
            return null;
        }
        return list.get(nextInt(list.size()));
    }

    /**
     * Returns a random elements from the provided queue. If the queue is empty then null is returned.
     *
     * @param <T>
     * @param list
     * @return
     */
    public <T> T getRandomElement(Queue<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return (T) list.toArray()[nextInt(list.size())];
    }

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

    public double nextDouble() {
        return (((long) (next(26)) << 27) + next(27)) * DOUBLE_UNIT;
    }

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
     * Returns a random integer below the given bound, or 0 if the bound is 0 or negative.
     *
     * @param bound
     * @return
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
        return next(32); //To change body of generated methods, choose Tools | Templates.
    }

    private int next(int bits) {
        return random.next(bits);
    }

}
