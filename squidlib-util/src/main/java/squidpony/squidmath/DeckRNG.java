package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;
import java.util.*;

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
public class DeckRNG extends StatefulRNG implements Serializable {
	private static final long serialVersionUID = 7828346657944720807L;
    private int step;
    private long lastShuffledState;
    private static final double[] baseDeck = new double[]{0.0, 0.0625, 0.125, 0.1875, 0.25, 0.3125, 0.375, 0.4375,
                                             0.5, 0.5625, 0.625, 0.6875, 0.75, 0.8125, 0.875, 0.9375};
    private final double[] deck = new double[16];

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
    public DeckRNG(CharSequence seedString) {
        this(CrossHash.hash64(seedString));
    }
    /**
     * Seeds this DeckRNG using the RandomnessSource it is given. Does not assign the RandomnessSource to any fields
     * that would affect future pseudo-random number generation.
     * @param random will be used to generate a new seed, but will not be assigned as this object's RandomnessSource
     */
    public DeckRNG(RandomnessSource random) {
        this(random.nextLong());

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

        return Math.round((float) sum / samples);
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
     * Returns a random element from the provided ShortSet. If the set is empty
     * then an exception is thrown.
     *
     * <p>
     * Requires iterating through a random amount of the elements in set, so performance depends on the size of set but
     * is likely to be decent. This is mostly meant for internal use, the same as ShortSet.
     * </p>
     * @param set the ShortSet to get an element from
     * @return the randomly selected element
     */
    public short getRandomElement(ShortSet set) {
        if (set.size <= 0) {
            throw new UnsupportedOperationException("ShortSet cannot be empty when getting a random element");
        }
        int n = nextInt(set.size);
        short s = 0;
        ShortSet.ShortSetIterator ssi = set.iterator();
        while (n-- >= 0 && ssi.hasNext)
            s = ssi.next();
        ssi.reset();
        return s;
    }

    /**
     * Returns a random element from the provided Collection, which should have predictable iteration order if you want
     * predictable behavior for identical RNG seeds, though it will get a random element just fine for any Collection
     * (just not predictably in all cases). If you give this a Set, it should be a LinkedHashSet or some form of sorted
     * Set like TreeSet if you want predictable results. Any List or Queue should be fine. Map does not implement
     * Collection, thank you very much Java library designers, so you can't actually pass a Map to this, though you can
     * pass the keys or values. If coll is empty, returns null.
     *
     * <p>
     * Requires iterating through a random amount of coll's elements, so performance depends on the size of coll but is
     * likely to be decent, as long as iteration isn't unusually slow. This replaces {@code getRandomElement(Queue)},
     * since Queue implements Collection and the older Queue-using implementation was probably less efficient.
     * </p>
     * @param <T> the type of the returned object
     * @param coll the Collection to get an element from; remember, Map does not implement Collection
     * @return the randomly selected element
     */
    public <T> T getRandomElement(Collection<T> coll) {
        if (coll.size() <= 0) {
            return null;
        }
        int n = nextInt(coll.size());
        T t = null;
        Iterator<T> it = coll.iterator();
        while (n-- >= 0 && it.hasNext())
            t = it.next();
        return t;
    }


    /**
     * @return a value from the gaussian distribution
     */
    @Override
    public double nextGaussian() {
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
            double multiplier = Math.sqrt(-2 * Math.log(s) / s);
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
     * Shuffle an array using the Fisher-Yates algorithm. Not GWT-compatible; use the overload that takes two arrays.
     * <br>
     * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
     *
     * @param elements an array of T; will not be modified
     * @return a shuffled copy of elements
     */
    @Override
    public <T> T[] shuffle(T[] elements)
    {
        int n = elements.length;
        T[] array = Arrays.copyOf(elements, n);
        for (int i = 0; i < n; i++)
        {
            int r = i + nextIntHasty(n - i);
            T t = array[r];
            array[r] = array[i];
            array[i] = t;
        }
        return array;
    }


    /**
     * Generates a random permutation of the range from 0 (inclusive) to length (exclusive).
     * Useful for passing to OrderedMap or OrderedSet's reorder() methods.
     *
     * @param length the size of the ordering to produce
     * @return a random ordering containing all ints from 0 to length (exclusive)
     */
    @Override
    public int[] randomOrdering(int length)
    {
        int[] dest = new int[length];
        for (int i = 0; i < length; i++)
        {
            int r = nextIntHasty(i + 1);
            if(r != i)
                dest[i] = dest[r];
            dest[r] = i;
        }
        return dest;
    }

    /**
     * Returns a random non-negative integer below the given bound, or 0 if the bound is 0.
     * Uses a slightly optimized technique. This method is considered "hasty" since
     * it should be faster than nextInt() doesn't check for "less-valid" bounds values. It also
     * has undefined behavior if bound is negative, though it will probably produce a negative
     * number (just how negative is an open question).
     *
     * @param bound the upper bound (exclusive); behavior is undefined if bound is negative
     * @return the found number
     */
    @Override
    public int nextIntHasty(int bound) {
        return (int)(nextDouble() * bound);
    }

    /**
     * Returns a random integer, which may be positive or negative.
     * @return A random int
     */
    @Override
    public int nextInt() {
        return (int)((nextDouble() * 2.0 - 1.0) * 0x7FFFFFFF);
    }

    /**
     * Returns a random long, which may be positive or negative.
     * @return A random long
     */
    @Override
    public long nextLong() {
        double nx = nextDouble();
        return (long)((nx * 2.0 - 1.0) * 0x7FFFFFFFFFFFFFFFL);
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
        return (long)(nx * bound);
        //return ((long)(nx * bound)) ^ (long)((nx * 0xFFFFFL) % bound) ^ (long)((nx * 0xFFFFF00000L) % bound);
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
        return (int)(nextDouble() * (1L << bits));

    }

    @Override
    public Random asRandom() {
        if(ran == null)
        {
            ran = new CustomRandom(random.copy());
        }
        return ran;
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
     * @param dest     Where to put the shuffle. It MUST have the same length as {@code elements}
     * @return {@code dest}
     * @throws IllegalStateException If {@code dest.length != elements.length}
     */
    @Override
    public <T> T[] shuffle(T[] elements, T[] dest) {
        return super.shuffle(elements, dest);
    }

    @Override
    public <T> ArrayList<T> shuffle(Collection<T> elements) {
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
     * Creates a copy of this DeckRNG; it will generate the same random numbers, given the same calls in order, as
     * this DeckRNG at the point copy() is called. The copy will not share references with this DeckRNG.
     *
     * @return a copy of this DeckRNG
     */
    @Override
    public DeckRNG copy()
    {
        DeckRNG next = new DeckRNG(lastShuffledState);
        next.random = random.copy();
        System.arraycopy(deck, 0, next.deck, 0, deck.length);
        next.step = step;
        return next;
    }

    /**
     * Gets a random portion of data (an array), assigns that portion to output (an array) so that it fills as much as
     * it can, and then returns output. Will only use a given position in the given data at most once; does this by
     * shuffling a copy of data and getting a section of it that matches the length of output.
     *
     * Based on http://stackoverflow.com/a/21460179 , credit to Vincent van der Weele; modifications were made to avoid
     * copying or creating a new generic array (a problem on GWT).
     * @param data an array of T; will not be modified.
     * @param output an array of T that will be overwritten; should always be instantiated with the portion length
     * @param <T> can be any non-primitive type.
     * @return an array of T that has length equal to output's length and may contain null elements if output is shorter
     * than data
     */
    @Override
    public <T> T[] randomPortion(T[] data, T[] output) {
        return super.randomPortion(data, output);
    }

    /**
     * Shuffle an array using the Fisher-Yates algorithm.
     * @param array an array of double; WILL be modified
     */
    private void shuffleInPlace(double[] array)
    {
        lastShuffledState = ((StatefulRandomness)random).getState();
        final int n = array.length;
        System.arraycopy(baseDeck, 0, array, 0, n);
//        for (int i = 0; i < n; i++)
//        {
//            int r = i + LightRNG.determineBounded(lastShuffledState + (i << 1), n - i);
//            double t = array[r];
//            array[r] = array[i];
//            array[i] = LightRNG.determineDouble(lastShuffledState + (i << 1) + 1) * 0.0625 + t;
//        }
        for (int i = n; i > 1; i--) {
            final int r = LinnormRNG.determineBounded(lastShuffledState + i, i);
            final double t = array[i - 1];
            array[i - 1] = array[r];
            array[r] = t;
        }
        for (int i = 0; i < n; i++) {
            array[i] += LinnormRNG.determineDouble(lastShuffledState ^ ~i) * 0.0625;
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
        ((StatefulRandomness)random).setState(state);
        shuffleInPlace(deck);
        step = 0;
    }

    @Override
    public String toString() {
        return "DeckRNG{state: 0x" + StringKit.hex(lastShuffledState) + "L, step: 0x" + StringKit.hex(step) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DeckRNG deckRNG = (DeckRNG) o;

        return step == deckRNG.step && lastShuffledState == deckRNG.lastShuffledState;
    }

    @Override
    public int hashCode() {
        int result = random.hashCode();
        result = 31 * result + step;
        result = 31 * result + (int) (lastShuffledState ^ (lastShuffledState >>> 32));
        return result;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    /**
     * Returns this DeckRNG in a way that can be deserialized even if only {@link IRNG}'s methods can be called.
     * @return a {@link Serializable} view of this DeckRNG; always {@code this}
     */
    @Override
    public Serializable toSerializable() {
        return this;
    }

}