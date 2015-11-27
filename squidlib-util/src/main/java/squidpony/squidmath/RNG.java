package squidpony.squidmath;

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
 */
public class RNG implements Serializable {

	protected static final double DOUBLE_UNIT = 1.0 / (1L << 53);
    protected RandomnessSource random;
    protected double nextNextGaussian;
    protected boolean haveNextNextGaussian = false;
    protected Random ran = null;

	private static final long serialVersionUID = 5716284182286645149L;

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
     * @author Tommy Ettinger
     */
    protected static class CustomRandom extends Random
    {

		private static final long serialVersionUID = 8211985716129281944L;
		private final RandomnessSource randomnessSource;

        /**
         * Creates a new random number generator. This constructor sets
         * the seed of the random number generator to a value very likely
         * to be distinct from any other invocation of this constructor.
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
    public <T> T getRandomElement(Queue<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return new ArrayList<T>(list).get(nextInt(list.size()));
    }

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
	public <T> List<T> randomRotation(final List<T> l) {
		final int sz = l.size();
		if (sz == 0)
			return Collections.<T>emptyList();

		/*
		 * Collections.rotate should prefer the best-performing way to rotate l, which would be an in-place
		 * modification for ArrayLists and an append to a sublist for Lists that don't support efficient random access.
		 */
        List<T> l2 = new ArrayList<T>(l);
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
                            throw new NoSuchElementException();
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
                    	throw new UnsupportedOperationException();
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
     * Shuffle an array using the Fisher-Yates algorithm.
     * @param elements an array of T; will not be modified
     * @param <T> can be any non-primitive type.
     * @return a shuffled copy of elements
     */
    public <T> T[] shuffle(T[] elements)
    {
        T[] array = elements.clone();
        int n = array.length;
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
     * Shuffle a {@link List} using the Fisher-Yates algorithm.
     * @param elements a List of T; will not be modified
     * @param <T> can be any non-primitive type.
     * @return a shuffled ArrayList containing the whole of elements in pseudo-random order.
     */
    public <T> ArrayList<T> shuffle(List<T> elements)
    {
        ArrayList<T> al = new ArrayList<T>(elements);
        int n = al.size();
        for (int i = 0; i < n; i++)
        {
            Collections.swap(al, i + nextInt(n - i), i);
        }
        return al;
    }

    /**
     * Gets a random portion of an array and returns it as a new array. Will only use a given position in the given
     * array at most once; does this by shuffling a copy of the array and getting a section of it.
     * @param data an array of T; will not be modified.
     * @param count the non-negative number of elements to randomly take from data
     * @param <T> can be any non-primitive type.
     * @return an array of T that has length equal to the smaller of count or data.length
     */
    public <T> T[] randomPortion(T[] data, int count)
    {
        T[] array = Arrays.copyOfRange(data, 0, Math.min(count, data.length));
        System.arraycopy(shuffle(data), 0, array, 0, Math.min(count, data.length));
        return array;
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
        int[] data = new int[end - start];
        for (int e = start, i = 0; e < end; e++) {
            data[i++] = e;
        }
        int n = data.length;
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
        return ((long) (next(32)) << 32) + next(32);
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

        long r = ((long)next(31) << 32) + next(32);
        long m = bound - 1;
        if ((bound & m) == 0) { // i.e., bound is a power of 2
            r = (r & m);
        } else {
            for (long u = r; u - (r = u % bound) + m < 0; u = ((long)next(31) << 32) + next(32)) {
            }
        }
        return r;
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


}
