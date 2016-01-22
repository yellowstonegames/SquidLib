package squidpony.squidmath;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * An RNG variant that has 16 possible grades of value it can produce and shuffles them like a deck of cards.
 * It repeats grades of value, but not exact values, every 16 numbers requested from it. Grades go in increments of
 * 0.0625 from 0.0 to 0.9375, and are added to a random double less than 0.0625 to get the random number for that
 * grade.
 * <p>
 * You can get values from this generator with: {@link #nextDouble()}, {@link #nextInt()},
 *   {@link #nextLong()}, and the bounded variants on each of those.
 *
 * Created by Tommy Ettinger on 5/2/2015.
 */
public class DeckRNG extends StatefulRNG {
	private static final long serialVersionUID = 7828346657944720807L;
    private int step;
    private long lastShuffledState;
    private double[] deck = new double[]{0.0, 0.0625, 0.125, 0.1875, 0.25, 0.3125, 0.375, 0.4375,
                                             0.5, 0.5625, 0.625, 0.6875, 0.75, 0.8125, 0.875, 0.9375};

    /**
     * Constructs a DeckRNG with a pseudo-random seed from Math.random().
     */
    public DeckRNG()
    {
        this((long)(Math.random() * ((1L << 50) - 1)));
    }
    /**
     * Construct a new DeckRNG with the given seed.
     *
     * @param seed used to seed the default RandomnessSource.
     */
    public DeckRNG(final long seed) {
        lastShuffledState = seed;
        random = new LightRNG(seed);
        step = 0;
    }

    /**
     * String-seeded constructor uses the hash of the String as a seed for LightRNG, which is of high quality, but low
     * period (which rarely matters for games), and has good speed and tiny state size.
     *
     * @param seedString a String to use as a seed; will be hashed in a uniform way across platforms.
     */
    public DeckRNG(String seedString) {
        this(StableHash.hash(seedString));
    }
    /**
     * Seeds this DeckRNG using the RandomnessSource it is given. Does not assign the RandomnessSource to any fields
     * that would affect future pseudo-random number generation.
     * @param random will be used to generate a new seed, but will not be assigned as this object's RandomnessSource
     */
    public DeckRNG(RandomnessSource random) {
        this(((long)random.next(32) << 32) | random.next(32));

    }

    /**
     * Generate a random double, altering the result if recently generated results have been leaning
     * away from this class' fairness value.
     * @return a double between 0.0 (inclusive) and 1.0 (exclusive)
     */
    @Override
    public double nextDouble() {
        if(step == 0)
            shuffleInPlace(deck);
        double gen = deck[step++];
        step %= 16;
        return gen;
    }

    /**
     * This returns a random double between 0.0 (inclusive) and max (exclusive).
     *
     * @return a value between 0 (inclusive) and max (exclusive)
     */
    @Override
    public double nextDouble(double max) {
        return nextDouble() * max;
    }

    /**
     * Returns a value from a even distribution from min (inclusive) to max
     * (exclusive).
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public <T> T getRandomElement(Queue<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return new ArrayList<T>(list).get(nextInt(list.size()));
    }

    /**
     * @return a value from the gaussian distribution
     */
    @Override
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

        return (int)(nextDouble() * bound);
    }

    /**
     * Returns a random integer, which may be positive or negative.
     * @return A random int
     */
    @Override
    public int nextInt() {
        return (int)((nextDouble() - 0.5) * 2.0 * 0x7FFFFFFF);
    }

    /**
     * Returns a random long, which may be positive or negative.
     * @return A random long
     */
    @Override
    public long nextLong() {
        double nx = nextDouble();
        return (long)((nx * 2.0 - 1.0) * 0x7FFFFFFFFFFFFFFFL) ^ (long)(nx * 0xFFFFFL) ^ (long)(nx * 0xFFFFF00000L);
    }

    /**
     * Returns a random long below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    @Override
	public long nextLong(long bound) {
        if (bound <= 0) {
            return 0;
        }
        double nx = nextDouble();
        return (long)((nx * bound)) ^ (long)((nx * 0xFFFFFL) % bound) ^ (long)((nx * 0xFFFFF00000L) % bound);
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
        return (int)(nextDouble() * (1l << bits));

    }

    @Override
    public Random asRandom() {
        if(ran == null)
        {
            ran = new CustomRandom(new LightRNG(getState()));
        }
        return ran;
    }

    @Override
    public <T> List<T> randomRotation(List<T> l) {
        return super.randomRotation(l);
    }

    @Override
    public <T> Iterable<T> getRandomStartIterable(List<T> list) {
        return super.getRandomStartIterable(list);
    }


    /**
     * Returns a value between min (inclusive) and max (exclusive).
     * <p/>
     * The inclusive and exclusive behavior is to match the behavior of the
     * similar method that deals with floating point values.
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    @Override
    public long between(long min, long max) {
        return nextLong(max - min) + min;
    }

    /**
     * Shuffle an array using the Fisher-Yates algorithm.
     *
     * @param elements an array of T; will not be modified
     * @return a shuffled copy of elements
     */
    @Override
    public <T> T[] shuffle(T[] elements) {
        return super.shuffle(elements);
    }

    /**
     * Shuffle an array using the Fisher-Yates algorithm.
     *
     * @param elements an array of T; will not be modified
     * @param dest     Where to put the shuffle. It MUST have the same length as {@code elements}
     * @return {@code dest}
     * @throws IllegalStateException If {@code dest.length != elements.length}
     */
    @Override
    public <T> T[] shuffle(T[] elements, T[] dest) {
        return super.shuffle(elements, dest);
    }

    @Override
    public <T> ArrayList<T> shuffle(List<T> elements) {
        return super.shuffle(elements);
    }

    @Override
    public float nextFloat() {
        return (float)nextDouble();
    }

    @Override
    public boolean nextBoolean() {
        return nextDouble() >= 0.5;
    }

    @Override
    public RandomnessSource getRandomness() {
        return random;
    }

    /**
     * Reseeds this DeckRNG using the RandomnessSource it is given. Does not assign the RandomnessSource to any fields
     * that would affect future pseudo-random number generation.
     * @param random will be used to generate a new seed, but will not be assigned as this object's RandomnessSource
     */
    @Override
    public void setRandomness(RandomnessSource random) {
        setState(((long)random.next(32) << 32) | random.next(32));

    }

    /**
     * Gets a random portion of an array and returns it as a new array. Will only use a given position in the given
     * array at most once; does this by shuffling a copy of the array and getting a section of it.
     *
     * @param data  an array of T; will not be modified.
     * @param count the non-negative number of elements to randomly take from data
     * @return an array of T that has length equal to the smaller of count or data.length
     */
    @Override
    public <T> T[] randomPortion(T[] data, int count) {
        return super.randomPortion(data, count);
    }

    /**
     * Gets a random portion of a List and returns it as a new List. Will only use a given position in the given
     * List at most once; does this by shuffling a copy of the List and getting a section of it.
     *
     * @param data  a List of T; will not be modified.
     * @param count the non-negative number of elements to randomly take from data
     * @return a List of T that has length equal to the smaller of count or data.length
     */
    @Override
    public <T> List<T> randomPortion(List<T> data, int count) {
        return super.randomPortion(data, count);
    }

    /**
     * Gets a random subrange of the non-negative ints from start (inclusive) to end (exclusive), using count elements.
     * May return an empty array if the parameters are invalid (end is less than/equal to start, or start is negative).
     *
     * @param start the start of the range of numbers to potentially use (inclusive)
     * @param end   the end of the range of numbers to potentially use (exclusive)
     * @param count the total number of elements to use; will be less if the range is smaller than count
     * @return an int array that contains at most one of each number in the range
     */
    @Override
    public int[] randomRange(int start, int end, int count) {
        return super.randomRange(start, end, count);
    }

    /**
     * Shuffle an array using the Fisher-Yates algorithm.
     * @param array an array of double; WILL be modified
     */
    private void shuffleInPlace(double[] array)
    {
        lastShuffledState = ((LightRNG)random).getState();
        int n = array.length;
        for (int i = 0; i < n; i++)
        {
            int r = i + ((LightRNG)random).nextInt(n - i);
            double t = array[r];
            array[r] = array[i];
            array[i] =((LightRNG)random).nextDouble(0.0625) + t;
        }
    }

    /**
     * Get a long that can be used to reproduce the sequence of random numbers this object will generate starting now.
     *
     * @return a long that can be used as state.
     */
    @Override
    public long getState() {
        return lastShuffledState;
    }

    /**
     * Sets the state of the random number generator to a given long, which will alter future random numbers this
     * produces based on the state. Setting the state always causes the "deck" of random grades to be shuffled.
     *
     * @param state any long (this can tolerate states of 0)
     */
    @Override
    public void setState(long state) {
        random = new LightRNG(state);
        step = 0;

    }

    @Override
    public String toString() {
        return "DeckRNG{state:" + Long.toHexString(lastShuffledState) +", step:" + step + "}";
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
}