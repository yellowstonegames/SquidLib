package squidpony.squidmath;

import squidpony.GwtCompatibility;
import squidpony.annotation.GwtIncompatible;

import java.io.Serializable;
import java.util.*;

/**
 * A wrapper class for working with random number generators in a more friendly
 * way.
 *
 * Includes methods for getting values between two numbers and for getting
 * random elements from a collection or array.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger
 * @author smelC
 */
public class RNG implements Serializable {

    protected static final double DOUBLE_UNIT = 1.0 / (1L << 53);
    protected static final float FLOAT_UNIT = 1.0f / (1 << 24);
    protected RandomnessSource random;
    protected double nextNextGaussian;
    protected boolean haveNextNextGaussian = false;
    protected Random ran = null;

	private static final long serialVersionUID = 2352426757973945149L;

    /**
     * Default constructor; uses SplitMix64, which is of high quality, but low period (which rarely matters for games),
     * and has good speed, tiny state size, and excellent 64-bit number generation.
     * <br>
     * Compatibility note: previous versions of SquidLib used Mersenne Twister by default. Due to the incompatibility
     * of the threads used by this Mersenne Twister implementation with GWT and HTML5 applications, the randomness
     * algorithm has been changed to a faster, more compatible algorithm, though it does suffer from a much lower
     * period. If you need drastically larger periods than 2^64, you can pass a LongPeriodRNG (or MersenneTwister on
     * targets other than HTML) object to the constructor that takes a RandomnessSource. If you don't know what the
     * period of a PRNG is, you probably don't need to worry about it; it's mainly relevant to heavily multi-threaded
     * applications anyway. The addition of LongPeriodRNG on March 21, 2016 should help to take the part of a fast,
     * large-period RNG, which MersenneTwister is unable to act as on GWT. The default may change again some time after
     * May 1, 2016, now that we have XoRoRNG, which is approximately as fast as LightRNG and has a substantially better
     * period (pow(2, 128) - 1).
     */
    public RNG() {
        this(new LightRNG());
    }

    /**
     * Seeded constructor; uses LightRNG, which is of high quality, but low period (which rarely matters for games),
     * and has good speed, tiny state size, and excellent 64-bit number generation.
     */
    public RNG(long seed) {
        this(new LightRNG(seed));
    }

    /**
     * String-seeded constructor; uses a platform-independent hash of the String (it does not use String.hashCode) as a
     * seed for LightRNG, which is of high quality, but low period (which rarely matters for games), and has good speed,
     * tiny state size, and excellent 64-bit number generation.
     */
    public RNG(String seedString) {
        this(new LightRNG(CrossHash.hash(seedString)));
    }

    /**
     * Uses the provided source of randomness for all calculations. This
     * constructor should be used if an alternate RandomnessSource other than LightRNG is desirable.
     *
     * @param random the source of pseudo-randomness, such as a MersenneTwister or SobolQRNG object
     */
    public RNG(RandomnessSource random) {
        this.random = random;
    }

    /**
     * A subclass of java.util.Random that uses a RandomnessSource supplied by the user instead of the default.
     * @author Tommy Ettinger
     */
    public static class CustomRandom extends Random
    {

		private static final long serialVersionUID = 8211985716129281944L;
		private final RandomnessSource randomnessSource;

        /**
         * Creates a new random number generator. This constructor uses
         * a LightRNG with a random seed.
         */
        public CustomRandom()
        {
            randomnessSource = new LightRNG();
        }
        /**
         * Creates a new random number generator. This constructor uses
         * the seed of the given RandomnessSource if it has been seeded.
         * @param randomnessSource a way to get random bits, supplied by RNG
         */
        public CustomRandom(RandomnessSource randomnessSource) {
            this.randomnessSource = randomnessSource;
        }

        /**
         * Generates the next pseudorandom number. Subclasses should
         * override this, as this is used by all other methods.
         * <p>
         * <p>The general contract of {@code next} is that it returns an
         * {@code int} value and if the argument {@code bits} is between
         * {@code 1} and {@code 32} (inclusive), then that many low-order
         * bits of the returned value will be (approximately) independently
         * chosen bit values, each of which is (approximately) equally
         * likely to be {@code 0} or {@code 1}. The method {@code next} is
         * implemented by class {@code Random} by atomically updating the seed to
         * <pre>{@code (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1)}</pre>
         * and returning
         * <pre>{@code (int)(seed >>> (48 - bits))}.</pre>
         * <p>
         * This is a linear congruential pseudorandom number generator, as
         * defined by D. H. Lehmer and described by Donald E. Knuth in
         * <i>The Art of Computer Programming,</i> Volume 3:
         * <i>Seminumerical Algorithms</i>, section 3.2.1.
         *
         * @param bits random bits
         * @return the next pseudorandom value from this random number
         * generator's sequence
         * @since 1.1
         */
        @Override
        protected int next(int bits) {
            return randomnessSource.next(bits);
        }
    }
    /**
     * @return a Random instance that can be used for legacy compatibility
     */
    public Random asRandom() {
        if (ran == null) {
            ran = new CustomRandom(random);
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
     * Returns a value between min (inclusive) and max (exclusive).
     *
     * The inclusive and exclusive behavior is to match the behavior of the
     * similar method that deals with floating point values.
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    public long between(long min, long max) {
        return nextLong(max - min) + min;
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

    /*
     * Returns a random elements from the provided queue. If the queue is empty
     * then null is returned.
     * 
	 * <p>
	 * Beware that this method allocates a copy of {@code list}, hence it's
	 * quite costly.
	 * </p>
     *
     * @param <T> the type of the returned object
     * @param list the list to get an element from
     * @return the randomly selected element
     */
    /*
    public <T> T getRandomElement(Queue<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return new ArrayList<>(list).get(nextInt(list.size()));
    }*/

	/**
     * Given a {@link List} l, this selects a random element of l to be the first value in the returned list l2. It
     * retains the order of elements in l after that random element and makes them follow the first element in l2, and
     * loops around to use elements from the start of l after it has placed the last element of l into l2.
     * <br>
     * Essentially, it does what it says on the tin. It randomly rotates the List l.
     * <br>
     * If you only need to iterate through a collection starting at a random point, the method getRandomStartIterable()
     * should have better performance.
     *
	 * @param l A {@link List} that will not be modified by this method. All elements of this parameter will be
     *            shared with the returned List.
     * @param <T> No restrictions on type. Changes to elements of the returned List will be reflected in the parameter.
     * @return A shallow copy of {@code l} that has been rotated so its first element has been randomly chosen
     * from all possible elements but order is retained. Will "loop around" to contain element 0 of l after the last
     * element of l, then element 1, etc.
	 */
    @GwtIncompatible /* Because of Collections.rotate */
	public <T> List<T> randomRotation(final List<T> l) {
		final int sz = l.size();
		if (sz == 0)
			return Collections.<T>emptyList();

		/*
		 * Collections.rotate should prefer the best-performing way to rotate l, which would be an in-place
		 * modification for ArrayLists and an append to a sublist for Lists that don't support efficient random access.
		 */
        List<T> l2 = new ArrayList<>(l);
        Collections.rotate(l2, nextInt(sz));
        return l2;
	}
    /**
     * Get an Iterable that starts at a random location in list and continues on through list in its current order.
     * Loops around to the beginning after it gets to the end, stops when it returns to the starting location.
     * <br>
     * You should not modify {@code list} while you use the returned reference. And there'll be no
     * ConcurrentModificationException to detect such erroneous uses.
     * @param list A list <b>with a constant-time {@link List#get(int)} method</b> (otherwise performance degrades).
     * @return An {@link Iterable} that iterates over {@code list} but start at
     *         a random index. If the chosen index is {@code i}, the iterator
     *         will return:
     *         {@code list[i]; list[i+1]; ...; list[list.length() - 1]; list[0]; list[i-1]}
     *
     */
    public <T> Iterable<T> getRandomStartIterable(final List<T> list) {
        final int sz = list.size();
        if (sz == 0)
            return Collections.<T> emptyList();

		/*
		 * Here's a tricky bit: Defining 'start' here means that every Iterator
		 * returned by the returned Iterable will have the same iteration order.
		 * In other words, if you use more than once the returned Iterable,
		 * you'll will see elements in the same order every time, which is
		 * desirable.
		 */
        final int start = nextInt(sz);

        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {

                    int next = -1;

                    @Override
                    public boolean hasNext() {
                        return next != start;
                    }

                    @Override
                    public T next() {
                        if (next == start)
                            throw new NoSuchElementException("Iteration terminated; check hasNext() before next()");
                        if (next == -1)
					/* First call */
                            next = start;
                        final T result = list.get(next);
                        if (next == sz - 1)
					/*
					 * Reached the list's end, let's continue from the list's
					 * left.
					 */
                            next = 0;
                        else
                            next++;
                        return result;
                    }

                    @Override
					public void remove() {
                    	throw new UnsupportedOperationException("Remove is not supported from a randomStartIterable");
					}

					@Override
                    public String toString() {
                        return "RandomStartIterator at index " + next;
                    }
                };
            }
        };
    }

    /**
     * Shuffle an array using the Fisher-Yates algorithm. Not GWT-compatible; use the overload that takes two arrays.
     * <br>
     * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
     * @param elements an array of T; will not be modified
     * @param <T> can be any non-primitive type.
     * @return a shuffled copy of elements
     */
    @GwtIncompatible
    public <T> T[] shuffle(T[] elements)
    {
        int n = elements.length;
        T[] array = Arrays.copyOf(elements, n);
        for (int i = 0; i < n; i++)
        {
            int r = i + nextInt(n - i);
            T t = array[r];
            array[r] = array[i];
            array[i] = t;
        }
        return array;
    }

    /**
     * Shuffle an array using the "inside-out" Fisher-Yates algorithm. DO NOT give the same array for both elements and
     * dest, since the prior contents of dest are rearranged before elements is used, and if they refer to the same
     * array, then you can end up with bizarre bugs where one previously-unique item shows up dozens of times. If
     * possible, create a new array with the same length as elements and pass it in as dest; the returned value can be
     * assigned to whatever you want and will have the same items as the newly-formed array.
     * <br>
     * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#The_.22inside-out.22_algorithm
     * @param elements an array of T; will not be modified
     * @param <T> can be any non-primitive type.
     * @param dest Where to put the shuffle. If it does not have the same length as {@code elements}, this will use the
     *             randomPortion method of this class to fill the smaller dest. MUST NOT be the same array as elements!
     * @return {@code dest} after modifications
     */
    /* This method has this prototype to be compatible with GWT. */
    public <T> T[] shuffle(T[] elements, T[] dest)
    {
    	if (dest.length != elements.length)
            return randomPortion(elements, dest);
        for (int i = 0; i < elements.length; i++)
        {
            int r = nextInt(i + 1);
            if(r != i)
                dest[i] = dest[r];
            dest[r] = elements[i];
        }
    	return dest;
    }

	/**
     * Shuffles a {@link Collection} of T using the Fisher-Yates algorithm and returns an ArrayList of T.
     * @param elements a Collection of T; will not be modified
     * @param <T> can be any non-primitive type.
     * @return a shuffled ArrayList containing the whole of elements in pseudo-random order.
     */
    public <T> ArrayList<T> shuffle(Collection<T> elements)
    {
        ArrayList<T> al = new ArrayList<>(elements);
        int n = al.size();
        for (int i = 0; i < n; i++)
        {
            Collections.swap(al, i + nextInt(n - i), i);
        }
        return al;
    }

    /**
     * Generates a random permutation of the range from 0 (inclusive) to length (exclusive).
     * Useful for passing to OrderedMap or OrderedSet's reorder() methods.
     * @param length the size of the ordering to produce
     * @return a random ordering containing all ints from 0 to length (exclusive)
     */
    public int[] randomOrdering(int length)
    {
        if(length <= 0)
            return new int[0];
        int[] dest = new int[length];
        for (int i = 0; i < length; i++)
        {
            int r = nextInt(i + 1);
            if(r != i)
                dest[i] = dest[r];
            dest[r] = i;
        }
        return dest;
    }

    /**
     * Generates a random permutation of the range from 0 (inclusive) to length (exclusive) and stores it in
     * the dest parameter, avoiding allocations.
     * Useful for passing to OrderedMap or OrderedSet's reorder() methods.
     * @param length the size of the ordering to produce
     * @param dest the destination array; will be modified
     * @return dest, filled with a random ordering containing all ints from 0 to length (exclusive)
     */
    public int[] randomOrdering(int length, int[] dest)
    {
        if(dest == null) return null;
        for (int i = 0; i < length && i < dest.length; i++)
        {
            int r = nextIntHasty(i + 1);
            if(r != i)
                dest[i] = dest[r];
            dest[r] = i;
        }
        return dest;
    }

    /**
     * Gets a random portion of data (an array), assigns that portion to output (an array) so that it fills as much as
     * it can, and then returns output. Will only use a given position in the given data at most once; does this by
     * generating random indices for data's elements, but only as much as needed, assigning the copied section to output
     * and not modifying data.
     * <br>
     * Based on http://stackoverflow.com/a/21460179 , credit to Vincent van der Weele; modifications were made to avoid
     * copying or creating a new generic array (a problem on GWT).
     * @param data an array of T; will not be modified.
     * @param output an array of T that will be overwritten; should always be instantiated with the portion length
     * @param <T> can be any non-primitive type.
     * @return an array of T that has length equal to output's length and may contain unchanged elements (null if output
     * was empty) if data is shorter than output
     */
    public <T> T[] randomPortion(T[] data, T[] output)
    {
        /*
        int length = data.length;
        int[] mapping = new int[length];
        for (int i = 0; i < length; i++) {
            mapping[i] = i;
        }
        for (int i = 0; i < output.length && length > 0; i++) {
            int r = nextInt(length);
            output[i] = data[mapping[r]];
            mapping[r] = length-1;
        }
        */

        int length = data.length;
        int n = Math.min(length, output.length);
        int[] mapping = GwtCompatibility.range(n);
        for (int i = 0; i < n; i++) {
            int r = nextInt(length);
            output[i] = data[mapping[r]];
            mapping[r] = mapping[--length];
        }

        return output;
    }

    /**
     * Gets a random portion of a List and returns it as a new List. Will only use a given position in the given
     * List at most once; does this by shuffling a copy of the List and getting a section of it.
     * @param data a List of T; will not be modified.
     * @param count the non-negative number of elements to randomly take from data
     * @param <T> can be any non-primitive type
     * @return a List of T that has length equal to the smaller of count or data.length
     */
    public <T> List<T> randomPortion(List<T> data, int count)
    {
        return shuffle(data).subList(0, Math.min(count, data.size()));
    }

    /**
     * Gets a random subrange of the non-negative ints from start (inclusive) to end (exclusive), using count elements.
     * May return an empty array if the parameters are invalid (end is less than/equal to start, or start is negative).
     * @param start the start of the range of numbers to potentially use (inclusive)
     * @param end  the end of the range of numbers to potentially use (exclusive)
     * @param count the total number of elements to use; will be less if the range is smaller than count
     * @return an int array that contains at most one of each number in the range
     */
    public int[] randomRange(int start, int end, int count)
    {
        if(end <= start || start < 0)
            return new int[0];

        int n = end - start;
        int[] data = new int[n];

        for (int e = start, i = 0; e < end; e++) {
            data[i++] = e;
        }

        for (int i = 0; i < n; i++)
        {
            int r = i + nextInt(n - i);
            int t = data[r];
            data[r] = data[i];
            data[i] = t;
        }
        int[] array = new int[Math.min(count, n)];
        System.arraycopy(data, 0, array, 0, Math.min(count, n));
        return array;
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
            double multiplier = Math.sqrt(-2 * Math.log(s) / s);
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
        return (random.nextLong() & 0x1fffffffffffffL) * DOUBLE_UNIT;
        // consider changing to this in a future version; it will break compatibility but should be fast/correct
        //return Double.longBitsToDouble(0x3FFL << 52 | random.nextLong() >>> 12) - 1.0;

    }

    /**
     * This returns a random double between 0.0 (inclusive) and max (exclusive).
     *
     * @return a value between 0 (inclusive) and max (exclusive)
     */
    public double nextDouble(double max) {
        return nextDouble() * max;
    }

    /**
     * This returns a maximum of 0.99999994 because that is the largest Float
     * value that is less than 1.0f
     *
     * @return a value between 0 (inclusive) and 0.99999994 (inclusive)
     */
    public float nextFloat() {
        return next(24) * FLOAT_UNIT;
    }

    /**
     * Get a random bit of state, interpreted as true or false with approximately equal likelihood.
     * @return a random boolean.
     */
    public boolean nextBoolean() {
        return next(1) != 0;
    }

    /**
     * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive).
     * @return a 64-bit random long.
     */
    public long nextLong() {
        return random.nextLong();
    }

    /**
     * Returns a random long below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    public long nextLong( final long bound ) {
        if ( bound <= 0 ) return 0;
        long threshold = (0x7fffffffffffffffL - bound + 1) % bound;
        for (;;) {
            long bits = random.nextLong() & 0x7fffffffffffffffL;
            if (bits >= threshold)
                return bits % bound;
        }
    }
    /**
     * Returns a random non-negative integer below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    public int nextInt(final int bound) {
        if ( bound <= 0 ) return 0;
        int threshold = (0x7fffffff - bound + 1) % bound;
        for (;;) {
            int bits = random.next(31);
            if (bits >= threshold)
                return bits % bound;
        }
    }
    /**
     * Returns a random non-negative integer below the given bound, or 0 if the bound is 0.
     * Uses an aggressively optimized technique that has some bias, but mostly for values of
     * bound over 1 billion. This method is considered "hasty" since it should be faster than
     * nextInt() but gives up some statistical quality to do so. It also has undefined behavior
     * if bound is negative, though it will probably produce a negative number (just how
     * negative is an open question).
     * <br>
     * Credit goes to Daniel Lemire, http://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/
     * @param bound the upper bound (exclusive); behavior is undefined if bound is negative
     * @return the found number
     */
    public int nextIntHasty(final int bound) {
        return (int)((bound * (random.nextLong() & 0x7FFFFFFFL)) >> 31);
    }

    /**
     * Gets a random Coord that has x between 0 (inclusive) and width (exclusive) and y between 0 (inclusive)
     * and height (exclusive). This makes one call to randomLong to generate (more than) 31 random bits for
     * each axis, and should be very fast. Remember that Coord values are cached in a pool that starts able to
     * hold up to 255 x and 255 y for positive values, and the pool should be grown with the static method
     * Coord.expandPool() in order to efficiently use larger Coord values. If width and height are very large,
     * greater than 100,000 for either, this particular method may show bias toward certain positions due to
     * the "hasty" technique used to reduce the random numbers to the given size, but because most maps in
     * tile-based games are relatively small, this technique should be fine.
     * <br>
     * Credit goes to Daniel Lemire, http://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/
     * @param width the upper bound (exclusive) for x coordinates
     * @param height the upper bound (exclusive) for y coordinates
     * @return a random Coord between (0,0) inclusive and (width,height) exclusive
     */
    public Coord nextCoord(int width, int height)
    {
        final long n = random.nextLong();
        return Coord.get((int)((width * (n >>> 33)) >> 31), (int)((height * (n & 0x7FFFFFFFL)) >> 31));
    }
    /**
     * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both inclusive).
     * @return a 32-bit random int.
     */
    public int nextInt() {
        return next(32);
    }

    /**
     * Get up to 32 bits (inclusive) of random state from the RandomnessSource.
     * @param bits 1 to 32
     * @return a random number that fits in the specified number of bits.
     */
    public int next(int bits) {
        return random.next(bits);
    }

    public RandomnessSource getRandomness() {
        return random;
    }

    public void setRandomness(RandomnessSource random) {
        this.random = random;
    }

    /**
     * Creates a copy of this RNG; it will generate the same random numbers, given the same calls in order, as this RNG
     * at the point copy() is called. The copy will not share references with this RNG.
     * @return a copy of this RNG
     */
    public RNG copy()
    {
        return new RNG(random.copy());
    }

    @Override
    public String toString() {
        return "RNG with Randomness Source " + random;
    }
}
