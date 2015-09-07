package squidpony.squidmath;

import java.util.List;
import java.util.Queue;

/**
 * An alteration to a RandomnessSource that attempts to produce values that are perceived as fair to an imperfect user.
 * <p>
 * This takes a RandomnessSource, defaulting to a LightRNG, and uses it to generate random values, but tracks the total
 * and compares it to the potential total of a generator of only numbers with a desired value (default 0.54,
 * so it compares against a sequence of all 0.54). If the current generated total is too high or low compared to the
 * desired total, the currently used seed is possibly changed, the generated number is moved in the direction of the
 * desired fairness, and it returns that instead of the number that would have pushed the current generated total
 * beyond the desired threshold. The new number, if one is changed, will always be closer to the desired fairness.
 * This is absolutely insecure for cryptographic purposes, but should seem more "fair" to a player than a
 * random number generator that seeks to be truly random.
 * You can create multiple DharmaRNG objects with different fairness values and seeds, and use favorable generators
 * (with fairness greater than 0.54) for characters that need an easier time, or unfavorable generators if you want
 * the characters that use that RNG to be impeded somewhat.
 * The name comes from the Wheel of Dharma.
 * This class currently will have a slight bias toward lower numbers with many RNGs unless fairness is tweaked; 0.54
 * can be used as a stand-in because 0.5 leans too low.
 *
 * <p>
 * You can get values from this generator with: {@link #nextDouble()}, {@link #nextInt()},
 *   {@link #nextLong()}, and the bounded variants on each of those.
 * <p>
 * You can alter the tracking information or requested fairness with {@link #resetFortune()},
 *   {@link #setFairness(double)}, and {@link #getFairness()}.
 *
 * Created by Tommy Ettinger on 5/2/2015.
 */
public class DharmaRNG extends RNG {

    /** The current index in the sequence. Starts at 1, not 0, because 0 acts differently and shouldn't be typical.*/
    private int seed = 1;

    /** Used to tweak the generator toward high or low values. */
    private double fairness = 0.54;

    /** Running total for what this has actually produced. */
    private double produced = 0.0;

    /** Running total for what this would produce if it always produced a value equal to fairness. */
    private double baseline = 0.0;

    /**
     * Constructs a DharmaRNG with a pseudo-random seed from Math.random().
     */
    public DharmaRNG()
    {
        this((long)(Math.random() * ((1L << 50) - 1)));
    }
    /**
     * Construct a new DharmaRNG with the given seed.
     *
     * @param seed used to seed the default RandomnessSource.
     */
    public DharmaRNG(final long seed) {
        super(new LightRNG(seed));
    }

    /**
     * Construct a new DharmaRNG with the given seed.
     *
     * @param seed used to seed the default RandomnessSource.
     * @param fairness the desired fairness metric, which must be between 0.0 and 1.0
     */
    public DharmaRNG(final long seed, final double fairness) {
        super(new LightRNG(seed));
        if(fairness < 0.0 || fairness >= 1.0)
            this.fairness = 0.54;
        else
            this.fairness = fairness;
    }

    /**
     * Construct a new DharmaRNG with the given seed.
     *
     * @param rs the implementation used to generate random bits.
     */
    public DharmaRNG(final RandomnessSource rs) {
        super(rs);
    }
    /**
     * Construct a new DharmaRNG with the given seed.
     *
     * @param rs the implementation used to generate random bits.
     * @param fairness the desired fairness metric, which must be between 0.0 and 1.0
     */
    public DharmaRNG(final RandomnessSource rs, final double fairness) {
        super(rs);
        if(fairness < 0.0 || fairness >= 1.0)
            this.fairness = 0.54;
        else
            this.fairness = fairness;
    }

    /**
     * Generate a random double, altering the result if recently generated results have been leaning
     * away from this class' fairness value.
     * @return a double between 0.0 (inclusive) and 1.0 (exclusive)
     */
    @Override
    public double nextDouble() {
        double gen = (((long) (super.next(26)) << 27) + super.next(27)) * DOUBLE_UNIT;
        /*if(Math.abs((produced + gen) - (baseline + fairness)) > 1.5) {
            //do some reseeding here if possible
        }*/
        if(Math.abs((produced + gen) - (baseline + fairness)) > 0.5)
        {
            gen = (gen + fairness) / 2.0;
            produced *= 0.5;
            baseline *= 0.5;
            produced += gen;
            baseline += fairness;
            return gen;
        }
        else
        {
            produced += gen;
            baseline += fairness;
            return gen;
        }
    }

    /**
     * This returns a random double between 0.0 (inclusive) and max (exclusive).
     *
     * @return a value between 0 (inclusive) and max (exclusive)
     */
    public double nextDouble(double max) {
        return this.nextDouble() * max;
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
        return min + (max - min) * this.nextDouble();
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
        return this.nextInt(max - min) + min;
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
        return array[this.nextInt(array.length)];
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
        return list.get(this.nextInt(list.size()));
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
        return (T) list.toArray()[this.nextInt(list.size())];
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
                v1 = 2 * this.nextDouble() - 1; // between -1 and 1
                v2 = 2 * this.nextDouble() - 1; // between -1 and 1
                s = v1 * v1 + v2 * v2;
            } while (s >= 1 || s == 0);
            double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
            nextNextGaussian = v2 * multiplier;
            haveNextNextGaussian = true;
            return v1 * multiplier;
        }
    }
    /**
     * Returns a random integer below the given bound, or 0 if the bound is 0 or
     * negative. Affects the current fortune.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    @Override
    public int nextInt(int bound) {
        if (bound <= 0) {
            return 0;
        }

        return (int)(this.nextDouble() * bound);
    }

    /**
     * Returns a random integer, which may be positive or negative. Affects the current fortune.
     * @return A random int
     */
    @Override
    public int nextInt() {
        return (int)((this.nextDouble() - 0.5) * 2.0 * 0x7FFFFFFF);
    }

    /**
     * Returns a random long, which may be positive or negative. Affects the current fortune.
     * @return A random long
     */
    @Override
    public long nextLong() {
        return (long)(2 * (this.nextDouble() - 0.5) * 0x7FFFFFFFFFFFFFFFL);
    }

    /**
     * Returns a random long below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    public long nextLong(long bound) {
        if (bound <= 0) {
            return 0;
        }
        return (long)(this.nextDouble() * bound);
    }
    /**
     * Gets the measure that this class uses for RNG fairness, defaulting to 0.54 (always between 0.0 and 1.0).
     * @return the current fairness metric.
     */
    public double getFairness() {
        return fairness;
    }

    /**
     * Sets the measure that this class uses for RNG fairness, which must always be between 0.0 and 1.0, and will be
     * set to 0.54 if an invalid value is passed.
     * @param fairness the desired fairness metric, which must be 0.0 &lt;= fairness &lt; 1.0
     */
    public void setFairness(double fairness) {
        if(fairness < 0.0 || fairness >= 1.0)
            this.fairness = 0.54;
        else
            this.fairness = fairness;
    }

    /**
     * Gets the status of the fortune used when calculating fairness adjustments.
     * @return the current value used to determine whether the results should be adjusted toward fairness.
     */
    public double getFortune()
    {
        return Math.abs(produced - baseline);
    }

    /**
     * Resets the stored history this RNG uses to try to ensure fairness.
     */
    public void resetFortune()
    {
        produced = 0.0;
        baseline = 0.0;
    }
    /**
     *
     * @param bits the number of bits to be returned
     * @return a random int of the number of bits specified.
     */
    @Override
    public int next(int bits) {
        if(bits <= 0)
            return 0;
        if(bits > 32)
            bits = 32;
        return (int)(this.nextDouble() * (1l << bits));

    }

}