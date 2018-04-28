package squidpony.squidmath;

import squidpony.ArrayTools;

import java.io.Serializable;
import java.util.*;

import static squidpony.squidmath.NumberTools.intBitsToFloat;

/**
 * A wrapper class for working with random number generators in a more friendly way.
 * Implements {@link IRNG}, which covers most of the API surface, but RNG implements
 * a decent amount of additional methods. You should consider if your code needs
 * these additional methods, and if not you should use IRNG as the type for when you
 * need some random number generator.
 * <p>
 * Includes methods for getting values between two numbers and for getting
 * random elements from a collection or array. There are methods to shuffle
 * a collection and to get a random ordering that can be applied as one shuffle
 * across multiple collections, such as via {@link OrderedMap#reorder(int...)},
 * {@link ArrayTools#reorder(ArrayList, int...)}, and so on. You can construct
 * an RNG with all sorts of RandomnessSource implementations, and choosing them
 * is usually not a big concern because the default works very well.
 * <br>
 * But if you do want advice on what RandomnessSource to use... {@link LightRNG}
 * is the default, and is very fast, but relative to many of the others it has a
 * significantly shorter period (the amount of random  numbers it will go through
 * before repeating the sequence), at {@code pow(2, 64)} as opposed to
 * {@link XoRoRNG}'s {@code pow(2, 128) - 1}, . {@link LapRNG} is about twice as
 * fast as LightRNG, but that's all it's good at; it fails quality tests almost
 * all around, though it can fool a human observer, and has a period that's only
 * barely better than LightRNG at {@code pow(2, 65)}. LightRNG also allows the
 * current RNG state to be retrieved and altered with {@code getState()} and
 * {@code setState()}, and the subclass of RNG, {@link StatefulRNG}, usually uses
 * LightRNG to handle random number generation when the state may need to be
 * saved and reloaded. {@link ThrustAltRNG} provides similar qualities to LightRNG,
 * and can be faster, but can't produce all possible 64-bit values (possibly some 32-bit
 * values as well); it was the default at one point so you may want to keep compatibility
 * with some versions by specifying ThrustAltRNG (before ThrustAltRNG was the default, the
 * current default of LightRNG was used). For most cases, you should decide between
 * ThrustAltRNG, LightRNG, XoRoRNG, and LapRNG based on your priorities. LightRNG is the
 * best if you want high speed, very good quality of randomness, and expect to either generate
 * less than 18446744073709551616 numbers or don't care if patterns appear after you generate
 * that many numbers, or if you need an RNG that can skip backwards or jump forwards
 * without incurring speed penalties. XoRoRNG is best if you want good speed and
 * quality but need to generate more than 18446744073709551616 numbers, though less
 * than 340282366920938463463374607431768211456 numbers. LapRNG is best if you only
 * care about getting random numbers quickly, and don't expect their quality to be
 * scrutinized; it can generate 36893488147419103232 numbers before the entire cycle
 * repeats, but patterns can easily appear before that. {@link JabRNG} may be a good option
 * as an alternative to LapRNG in the no-quality-guarantees department; it's slightly slower
 * than LapRNG but faster than ThrustAltRNG, actually can pass a fair amount of quality
 * tests, and can't produce a slim majority of all possible 64-bit values. Its period is
 * half that of ThrustAltRNG and LightRNG, at 2 to the 63.
 * <br>
 * There are many more RandomnessSource implementations! If XoRoRNG's large
 * period is not enough, then we also supply {@link LongPeriodRNG}, which has a
 * tremendous period of {@code pow(2, 1024) - 1}. You might want significantly less
 * predictable random results, which  {@link IsaacRNG} can provide, along with a
 * large period. The quality of {@link PermutedRNG} is also good, usually, and it
 * has a sound basis in PCG-Random, an involved library with many variants on its
 * RNGs.
 * There may be reasons why you would want a random number generator that uses 32-bit
 * math instead of the more common 64-bit math, but using a 32-bit int on desktop and
 * Android won't act the same as that same 32-bit int on GWT. Since GWT is stuck with
 * JavaScript's implementation of ints with doubles, overflow (which is needed for an
 * RNG) doesn't work with ints as expected, but does with GWT's implementation of longs.
 * If targeting GWT, {@link Lathe32RNG} is significantly faster at producing int values
 * than any long-based generator, and will produce the same results on GWT as on desktop
 * or Android (not all 32-bit generators do this). {@link ThrustAlt32RNG},
 * {@link Zag32RNG}, and {@link Oriole32RNG} are also GWT-safe, but other generators that
 * were thought to be GWT-friendly are not. These GWT-unsafe generators have other uses,
 * but should not be used on GWT: {@link PintRNG}, {@link Light32RNG},
 * and {@link FlapRNG}. All other generators use longs, and so will be
 * slower than the recommended Lathe32RNG on GWT, but much faster on 64-bit JREs.
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger
 * @author smelC
 */
public class RNG implements Serializable, IRNG {

    /**
     * A very small multiplier used to reduce random numbers to from the {@code [0.0,9007199254740991.0)} range to the
     * {@code [0.0,1.0)} range. Equivalent to {@code 1.0 / (1 << 53)}, if that number makes more sense to you, but the
     * source uses the hexadecimal double literal {@code 0x1p-53}. The hex literals are a nice "hidden feature" of Java
     * 5 onward, and allow exact declaration of floating-point numbers without precision loss from decimal conversion.
     */
	protected static final double DOUBLE_UNIT = 0x1p-53; // more people should know about hex double literals!
    /**
     * A very small multiplier used to reduce random numbers to from the {@code [0.0,16777216.0)} range to the
     * {@code [0.0,1.0)} range. Equivalent to {@code 1.0f / (1 << 24)}, if that number makes more sense to you, but the
     * source uses the hexadecimal double literal {@code 0x1p-24f}. The hex literals are a nice "hidden feature" of Java
     * 5 onward, and allow exact declaration of floating-point numbers without precision loss from decimal conversion.
     */
	protected static final float FLOAT_UNIT = 0x1p-24f;
	protected RandomnessSource random;
	protected double nextNextGaussian;
	protected boolean haveNextNextGaussian = false;
	protected Random ran = null;

    private static final long serialVersionUID = 2352426757973945105L;


    /**
     * Default constructor; uses {@link LightRNG}, which is of high quality, but low period (which rarely matters
     * for games), and has excellent speed, tiny state size, and natively generates 64-bit numbers.
     * <br>
     * Previous versions of SquidLib used different implementations, including {@link MersenneTwister} and for some
     * time {@link ThrustAltRNG}. You can still use one of these by instantiating one of those classes and passing it to
     * {@link #RNG(RandomnessSource)}, which may be the best way to ensure the same results across versions.
     */
    public RNG() {
        this(new LightRNG());
    }

    /**
     * Default constructor; uses {@link LightRNG}, which is of high quality, but low period (which rarely matters
     * for games), and has excellent speed, tiny state size, and natively generates 64-bit numbers. The seed can be
     * any long, including 0.
     * @param seed any long
     */
    public RNG(long seed) {
        this(new LightRNG(seed));
    }

    /**
     * String-seeded constructor; uses a platform-independent hash of the String (it does not use String.hashCode) as a
     * seed for {@link LightRNG}, which is of high quality, but low period (which rarely matters for games), and has
     * excellent speed, tiny state size, and natively generates 64-bit numbers.
     */
    public RNG(CharSequence seedString) {
        this(new LightRNG(CrossHash.hash64(seedString)));
    }

    /**
     * Uses the provided source of randomness for all calculations. This constructor should be used if an alternate
     * RandomnessSource other than LightRNG is desirable, such as to keep compatibility with earlier SquidLib
     * versions that defaulted to LightRNG.
     * <br>
     * If the parameter is null, this is equivalent to using {@link #RNG()} as the constructor.
     * @param random the source of pseudo-randomness, such as a LightRNG or LongPeriodRNG object
     */
    public RNG(RandomnessSource random) {
        this.random = (random == null) ? new LightRNG() : random;
    }

    /**
     * A subclass of java.util.Random that uses a RandomnessSource supplied by the user instead of the default.
     *
     * @author Tommy Ettinger
     */
    public static class CustomRandom extends Random {

        private static final long serialVersionUID = 8211985716129281944L;
        private final RandomnessSource randomnessSource;

        /**
         * Creates a new random number generator. This constructor uses
         * a LightRNG with a random seed.
         */
        public CustomRandom() {
            randomnessSource = new LightRNG();
        }

        /**
         * Creates a new random number generator. This constructor uses
         * the seed of the given RandomnessSource if it has been seeded.
         *
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
         *
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
     * Returns a value from an even distribution from min (inclusive) to max
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
     * <br>
     * The inclusive and exclusive behavior is to match the behavior of the similar
     * method that deals with floating point values.
     * <br>
     * If {@code min} and {@code max} happen to be the same, {@code min} is returned
     * (breaking the exclusive behavior, but it's convenient to do so).
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
     * Returns a value between min (inclusive) and max (exclusive).
     * <p>
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
     * Returns the average of a number of randomly selected numbers from the
     * provided range, with min being inclusive and max being exclusive. It will
     * sample the number of times passed in as the third parameter.
     * <p>
     * The inclusive and exclusive behavior is to match the behavior of the
     * similar method that deals with floating point values.
     * <p>
     * This can be used to weight RNG calls to the average between min and max.
     *
     * @param min     the minimum bound on the return value (inclusive)
     * @param max     the maximum bound on the return value (exclusive)
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
     * @param <T>   the type of the returned object
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
     * @param <T>  the type of the returned object
     * @param list the list to get an element from
     * @return the randomly selected element
     */
    @Override
    public <T> T getRandomElement(List<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(nextInt(list.size()));
    }

    /**
     * Returns a random element from the provided ShortSet. If the set is empty
     * then an exception is thrown.
     * <p>
     * <p>
     * Requires iterating through a random amount of the elements in set, so performance depends on the size of set but
     * is likely to be decent. This is mostly meant for internal use, the same as ShortSet.
     * </p>
     *
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
     * <p>
     * <p>
     * Requires iterating through a random amount of coll's elements, so performance depends on the size of coll but is
     * likely to be decent, as long as iteration isn't unusually slow. This replaces {@code getRandomElement(Queue)},
     * since Queue implements Collection and the older Queue-using implementation was probably less efficient.
     * </p>
     *
     * @param <T>  the type of the returned object
     * @param coll the Collection to get an element from; remember, Map does not implement Collection
     * @return the randomly selected element
     */
    @Override
    public <T> T getRandomElement(Collection<T> coll) {
        int n;
        if ((n = coll.size()) <= 0) {
            return null;
        }
        n = nextInt(n);
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
	 * Requires iterating through a random amount of the elements in set, so
	 * performance depends on the size of set but is likely to be decent. This
	 * is mostly meant for internal use, the same as ShortSet.
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
     * should have better performance. This was GWT incompatible before GWT 2.8.0 became the version SquidLib uses; now
     * this method works fine with GWT.
     *
     * @param l   A {@link List} that will not be modified by this method. All elements of this parameter will be
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
		 * Collections.rotate should prefer the best-performing way to rotate l,
		 * which would be an in-place modification for ArrayLists and an append
		 * to a sublist for Lists that don't support efficient random access.
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
     *
     * @param list A list <b>with a constant-time {@link List#get(int)} method</b> (otherwise performance degrades).
     * @return An {@link Iterable} that iterates over {@code list} but start at
     * a random index. If the chosen index is {@code i}, the iterator
     * will return:
     * {@code list[i]; list[i+1]; ...; list[list.length() - 1]; list[0]; list[i-1]}
     */
    public <T> Iterable<T> getRandomStartIterable(final List<T> list) {
        final int sz = list.size();
        if (sz == 0)
            return Collections.<T>emptyList();

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
     * Mutates the array arr by switching the contents at pos1 and pos2.
     * @param arr an array of T; must not be null
     * @param pos1 an index into arr; must be at least 0 and no greater than arr.length
     * @param pos2 an index into arr; must be at least 0 and no greater than arr.length
     */
    private static <T> void swap(T[] arr, int pos1, int pos2) {
        final T tmp = arr[pos1];
        arr[pos1] = arr[pos2];
        arr[pos2] = tmp;
    }

    /**
     * Shuffle an array using the Fisher-Yates algorithm and returns a shuffled copy.
     * GWT-compatible since GWT 2.8.0, which is the default if you use libGDX 1.9.5 or higher.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @return a shuffled copy of elements
     */
    @Override
    public <T> T[] shuffle(final T[] elements) {
        final int size = elements.length;
        final T[] array = Arrays.copyOf(elements, size);
        for (int i = size; i > 1; i--) {
            swap(array, i - 1, nextIntHasty(i));
        }
        return array;
    }

    /**
     * Shuffles an array in-place using the Fisher-Yates algorithm.
     * If you don't want the array modified, use {@link #shuffle(Object[], Object[])}.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; <b>will</b> be modified
     * @param <T>      can be any non-primitive type.
     * @return elements after shuffling it in-place
     */
    @Override
    public <T> T[] shuffleInPlace(T[] elements) {
        final int size = elements.length;
        for (int i = size; i > 1; i--) {
            swap(elements, i - 1, nextIntHasty(i));
        }
        return elements;
    }

    /**
     * Shuffle an array using the Fisher-Yates algorithm. If possible, create a new array or reuse an existing array
     * with the same length as elements and pass it in as dest; the dest array will contain the shuffled contents of
     * elements and will also be returned as-is.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @param dest     Where to put the shuffle. If it does not have the same length as {@code elements}, this will use the
     *                 randomPortion method of this class to fill the smaller dest.
     * @return {@code dest} after modifications
     */
    @Override
    public <T> T[] shuffle(T[] elements, T[] dest) {
        if (dest.length != elements.length)
            return randomPortion(elements, dest);
        System.arraycopy(elements, 0, dest, 0, elements.length);
        shuffleInPlace(dest);
        return dest;
    }

    /**
     * Shuffles a {@link Collection} of T using the Fisher-Yates algorithm and returns an ArrayList of T.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     * @param elements a Collection of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @return a shuffled ArrayList containing the whole of elements in pseudo-random order.
     */
    @Override
    public <T> ArrayList<T> shuffle(Collection<T> elements) {
        return shuffle(elements, null);
    }

    /**
     * Shuffles a {@link Collection} of T using the Fisher-Yates algorithm. The result
     * is allocated if {@code buf} is null or if {@code buf} isn't empty,
     * otherwise {@code elements} is poured into {@code buf}.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     * @param elements a Collection of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @param buf a buffer as an ArrayList that will be filled with the shuffled contents of elements;
     *            if null or non-empty, a new ArrayList will be allocated and returned
     * @return a shuffled ArrayList containing the whole of elements in pseudo-random order.
     */
    @Override
    public <T> ArrayList<T> shuffle(Collection<T> elements, /*@Nullable*/ ArrayList<T> buf) {
        final ArrayList<T> al;
        if (buf == null || !buf.isEmpty())
            al = new ArrayList<>(elements);
        else {
            al = buf;
            al.addAll(elements);
        }
        final int n = al.size();
        for (int i = n; i > 1; i--) {
            Collections.swap(al, nextInt(i), i - 1);
        }
        return al;
    }
    /**
     * Shuffles a Collection of T items in-place using the Fisher-Yates algorithm.
     * This only shuffles List data structures.
     * If you don't want the array modified, use {@link #shuffle(Collection)}, which returns a List as well.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements a Collection of T; <b>will</b> be modified
     * @param <T>      can be any non-primitive type.
     * @return elements after shuffling it in-place
     */
    @Override
    public <T> List<T> shuffleInPlace(List<T> elements) {
        final int n = elements.size();
        for (int i = n; i > 1; i--) {
            Collections.swap(elements, nextInt(i), i - 1);
        }
        return elements;
    }


    /**
     * Generates a random permutation of the range from 0 (inclusive) to length (exclusive).
     * Useful for passing to OrderedMap or OrderedSet's reorder() methods.
     *
     * @param length the size of the ordering to produce
     * @return a random ordering containing all ints from 0 to length (exclusive)
     */
    @Override
    public int[] randomOrdering(int length) {
        if (length <= 0)
            return new int[0];
        return randomOrdering(length, new int[length]);
    }

    /**
     * Generates a random permutation of the range from 0 (inclusive) to length (exclusive) and stores it in
     * the dest parameter, avoiding allocations.
     * Useful for passing to OrderedMap or OrderedSet's reorder() methods.
     *
     * @param length the size of the ordering to produce
     * @param dest   the destination array; will be modified
     * @return dest, filled with a random ordering containing all ints from 0 to length (exclusive)
     */
    @Override
    public int[] randomOrdering(int length, int[] dest) {
        if (dest == null) return null;

        final int n = Math.min(length, dest.length);
        for (int i = 0; i < n; i++) {
            dest[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            final int r = nextIntHasty(i+1),
                    t = dest[r];
            dest[r] = dest[i];
            dest[i] = t;
        }
        return dest;
    }

    /**
     * Gets a random portion of a Collection and returns it as a new List. Will only use a given position in the given
     * Collection at most once; does this by shuffling a copy of the Collection and getting a section of it.
     *
     * @param data  a Collection of T; will not be modified.
     * @param count the non-negative number of elements to randomly take from data
     * @param <T>   can be any non-primitive type
     * @return a List of T that has length equal to the smaller of count or data.length
     */
    public <T> List<T> randomPortion(Collection<T> data, int count) {
        return shuffle(data).subList(0, Math.min(count, data.size()));
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
    public int[] randomRange(int start, int end, int count) {
        if (end <= start || start < 0)
            return new int[0];

        int n = end - start;
        final int[] data = new int[n];

        for (int e = start, i = 0; e < end; e++) {
            data[i++] = e;
        }

        for (int i = 0; i < n - 1; i++) {
            final int r = i + nextInt(n - i), t = data[r];
            data[r] = data[i];
            data[i] = t;
        }
        final int[] array = new int[Math.min(count, n)];
        System.arraycopy(data, 0, array, 0, Math.min(count, n));
        return array;
    }

    /**
     * Generates a random float with a curved distribution that centers on 0 (where it has a bias) and can (rarely)
     * approach -1f and 1f, but not go beyond those bounds. This is similar to {@link #nextGaussian()} in that it uses
     * a curved distribution, but it is not the same. The distribution for the values is similar to Irwin-Hall, and is
     * frequently near 0 but not too-rarely near -1f or 1f. It cannot produce values greater than or equal to 1f, or
     * less than -1f, but it can produce -1f.
     * @return a deterministic float between -1f (inclusive) and 1f (exclusive), that is very likely to be close to 0f
     */
    public float nextCurvedFloat() {
        final long start = random.nextLong();
        return   (intBitsToFloat((int)start >>> 9 | 0x3F000000)
                + intBitsToFloat((int) (start >>> 41) | 0x3F000000)
                + intBitsToFloat(((int)(start ^ ~start >>> 20) & 0x007FFFFF) | 0x3F000000)
                + intBitsToFloat(((int) (~start ^ start >>> 30) & 0x007FFFFF) | 0x3F000000)
                - 3f);
    }

    /**
     * Gets a pseudo-random double from the Gaussian distribution, which will usually be between -1.0 and 1.0 but is not
     * always in that range. If you do want values to always be between -1 and 1 and a float is OK, consider using
     * {@link #nextCurvedFloat()}, which is a different distribution that is less sharply-curved towards 0 and
     * terminates at -1 and 1.
     * @return a value from the Gaussian distribution
     */
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
     * Gets a random double between 0.0 inclusive and 1.0 exclusive.
     * This returns a maximum of 0.9999999999999999 because that is the largest double value that is less than 1.0 .
     *
     * @return a double between 0.0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    @Override
    public double nextDouble() {
        return (random.nextLong() & 0x1fffffffffffffL) * 0x1p-53;
        //this is here for a record of another possibility; it can't generate quite a lot of possible values though
        //return Double.longBitsToDouble(0x3FF0000000000000L | random.nextLong() >>> 12) - 1.0;
    }

    /**
     * This returns a random double between 0.0 (inclusive) and outer (exclusive). The value for outer can be positive
     * or negative. Because of how math on doubles works, there are at most 2 to the 53 values this can return for any
     * given outer bound, and very large values for outer will not necessarily produce all numbers you might expect.
     *
     * @param outer the outer exclusive bound as a double; can be negative or positive
     * @return a double between 0.0 (inclusive) and outer (exclusive)
     */
    @Override
    public double nextDouble(final double outer) {
        return (random.nextLong() & 0x1fffffffffffffL) * 0x1p-53 * outer;
    }

    /**
     * Gets a random float between 0.0f inclusive and 1.0f exclusive.
     * This returns a maximum of 0.99999994 because that is the largest float value that is less than 1.0f .
     *
     * @return a float between 0f (inclusive) and 0.99999994f (inclusive)
     */
    @Override
    public float nextFloat() {
        return random.next(24) * 0x1p-24f;
    }
    /**
     * This returns a random float between 0.0f (inclusive) and outer (exclusive). The value for outer can be positive
     * or negative. Because of how math on floats works, there are at most 2 to the 24 values this can return for any
     * given outer bound, and very large values for outer will not necessarily produce all numbers you might expect.
     *
     * @param outer the outer exclusive bound as a float; can be negative or positive
     * @return a float between 0f (inclusive) and outer (exclusive)
     */
    @Override
    public float nextFloat(final float outer) {
        return random.next(24) * 0x1p-24f * outer;
    }

    /**
     * Get a random bit of state, interpreted as true or false with approximately equal likelihood.
     * This may have better behavior than {@code rng.next(1)}, depending on the RandomnessSource implementation; the
     * default LightRNG will behave fine, as will ThrustRNG and ThrustAltRNG (these all use similar algorithms), but the
     * normally-high-quality XoRoRNG will produce very predictable output with {@code rng.next(1)} and very good output
     * with {@code rng.nextBoolean()}. This is a known and considered flaw of Xoroshiro128+, the algorithm used by
     * XoRoRNG, and a large number of generators have lower quality on the least-significant bit than the most-
     * significant bit, where this method only checks the most-significant bit.
     * @return a random boolean.
     */
    @Override
    public boolean nextBoolean() {
        return nextLong() < 0L;
    }

    /**
     * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive).
     *
     * @return a 64-bit random long.
     */
    @Override
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
    @Override
    public long nextLong(final long bound) {
        if (bound <= 0) return 0;
        long threshold = (0x7fffffffffffffffL - bound + 1) % bound;
        for (; ; ) {
            long bits = random.nextLong() & 0x7fffffffffffffffL;
            if (bits >= threshold)
                return bits % bound;
        }
    }

    /**
     * Returns a random non-negative integer below the given bound, or 0 if the bound is 0 or
     * negative. Always makes one call to the {@link RandomnessSource#next(int)} method of the RandomnessSource that
     * would be returned by {@link #getRandomness()}, even if bound is 0 or negative, to avoid branching and also to
     * ensure consistent advancement rates for the RandomnessSource (this can be important if you use a
     * {@link SkippingRandomness} and want to go back before a result was produced).
     * <br>
     * This method changed a fair amount on April 5, 2018 to better support RandomnessSource implementations with a
     * slower nextLong() method, such as {@link Lathe32RNG}, and to avoid branching/irregular state advancement/modulus
     * operations. It is now almost identical to {@link #nextIntHasty(int)}, but won't return negative results if bound
     * is negative (matching its previous behavior). This may have statistical issues (small ones) if bound is very
     * large (the estimate is still at least a bound of a billion or more before issues are observable).
     * <br>
     * Credit goes to Daniel Lemire, http://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    @Override
    public int nextInt(final int bound) {
        return (int) ((bound * ((long)random.next(31))) >>> 31) & ~(bound >> 31);
//        int threshold = (0x7fffffff - bound + 1) % bound;
//        for (; ; ) {
//            int bits = random.next(31);
//            if (bits >= threshold)
//                return bits % bound;
//        }
    }

    /**
     * Returns a random non-negative integer between 0 (inclusive) and the given bound (exclusive),
     * or 0 if the bound is 0. The bound can be negative, which will produce 0 or a negative result.
     * Uses an aggressively optimized technique that has some bias, but mostly for values of
     * bound over 1 billion. This method is considered "hasty" since it should be faster than
     * {@link #nextInt(int)} but gives up some statistical quality to do so.
     * <br>
     * Credit goes to Daniel Lemire, http://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/
     *
     * @param bound the outer bound (exclusive), can be negative or positive
     * @return the found number
     */
    public int nextIntHasty(final int bound) {
        return (int) ((bound * (random.nextLong() & 0x7FFFFFFFL)) >> 31);
    }

    /**
     * Generates random bytes and places them into the given byte array, modifying it in-place.
     * The number of random bytes produced is equal to the length of the byte array. Unlike the
     * method in java.util.Random, this generates 8 bytes at a time, which can be more efficient
     * with many RandomnessSource types than the JDK's method that generates 4 bytes at a time.
     * <br>
     * Adapted from code in the JavaDocs of {@link Random#nextBytes(byte[])}.
     * <br>
     * @param  bytes the byte array to fill with random bytes; cannot be null, will be modified
     * @throws NullPointerException if the byte array is null
     */
    public void nextBytes(final byte[] bytes) {
        for (int i = 0; i < bytes.length; )
            for (long r = random.nextLong(), n = Math.min(bytes.length - i, 8); n-- > 0; r >>>= 8)
                bytes[i++] = (byte) r;
    }

    /**
     * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both inclusive).
     *
     * @return a 32-bit random int.
     */
    @Override
    public int nextInt() {
        return random.next(32);
    }

    /**
     * Get up to 32 bits (inclusive) of random output; the int this produces
     * will not require more than {@code bits} bits to represent.
     *
     * @param bits an int between 1 and 32, both inclusive
     * @return a random number that fits in the specified number of bits
     */
    @Override
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
     *
     * @return a copy of this RNG
     */
    @Override
    public RNG copy() {
        return new RNG(random.copy());
    }

    /**
     * Generates a random 64-bit long with a number of '1' bits (Hamming weight) equal on average to bitCount.
     * For example, calling this with a parameter of 32 will be equivalent to calling nextLong() on this object's
     * RandomnessSource (it doesn't consider overridden nextLong() methods, where present, on subclasses of RNG).
     * Calling this with a parameter of 16 will have on average 16 of the 64 bits in the returned long set to '1',
     * distributed pseudo-randomly, while a parameter of 47 will have on average 47 bits set. This can be useful for
     * certain code that uses bits to represent data but needs a different ratio of set bits to unset bits than 1:1.
     * <br>
     * This method is deprecated because it really only finds usage in GreasedRegion, so it has been moved there and
     * made so it can take any RandomnessSource as a parameter, including any IRNG or RNG.
     *
     * @param bitCount an int, only considered if between 0 and 64, that is the average number of bits to set
     * @return a 64-bit long that, on average, should have bitCount bits set to 1, potentially anywhere in the long
     * @deprecated see the version in GreasedRegion, {@link GreasedRegion#approximateBits(RandomnessSource, int)}
     */
    @Deprecated
    public long approximateBits(int bitCount) {
        if (bitCount <= 0)
            return 0L;
        if (bitCount >= 64)
            return -1L;
        if (bitCount == 32)
            return random.nextLong();
        boolean high = bitCount > 32;
        int altered = (high ? 64 - bitCount : bitCount), lsb = NumberTools.lowestOneBit(altered);
        long data = random.nextLong();
        for (int i = lsb << 1; i <= 16; i <<= 1) {
            if ((altered & i) == 0)
                data &= random.nextLong();
            else
                data |= random.nextLong();
        }
        return high ? ~(random.nextLong() & data) : (random.nextLong() & data);
    }

    /**
     * Gets a somewhat-random long with exactly 32 bits set; in each pair of bits starting at bit 0 and bit 1, then bit
     * 2 and bit 3, up to bit 62 and bit 3, one bit will be 1 and one bit will be 0 in each pair.
     * <br>
     * Not exactly general-use; meant for generating data for GreasedRegion. This is deprecated in favor of the version
     * in GreasedRegion.
     * @return a random long with 32 "1" bits, distributed so exactly one bit is "1" for each pair of bits
     * @deprecated See {@link GreasedRegion#randomInterleave(RandomnessSource)} for where this will be moved
     */
    @Deprecated
    public long randomInterleave() {
        long bits = nextLong() & 0xFFFFFFFFL, ib = ~bits & 0xFFFFFFFFL;
        bits |= (bits << 16);
        ib |= (ib << 16);
        bits &= 0x0000FFFF0000FFFFL;
        ib &= 0x0000FFFF0000FFFFL;
        bits |= (bits << 8);
        ib |= (ib << 8);
        bits &= 0x00FF00FF00FF00FFL;
        ib &= 0x00FF00FF00FF00FFL;
        bits |= (bits << 4);
        ib |= (ib << 4);
        bits &= 0x0F0F0F0F0F0F0F0FL;
        ib &= 0x0F0F0F0F0F0F0F0FL;
        bits |= (bits << 2);
        ib |= (ib << 2);
        bits &= 0x3333333333333333L;
        ib &= 0x3333333333333333L;
        bits |= (bits << 1);
        ib |= (ib << 1);
        bits &= 0x5555555555555555L;
        ib &= 0x5555555555555555L;
        return (bits | (ib << 1));
    }

    /**
     * Gets the minimum random long between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias toward zero, but all possible values are between 0, inclusive,
     * and bound, exclusive.
     * @param bound the outer exclusive bound
     * @param trials how many numbers to generate and get the minimum of
     * @return the minimum generated long between 0 and bound out of the specified amount of trials
     */
    public long minLongOf(final long bound, final int trials)
    {
        long value = nextLong(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.min(value, nextLong(bound));
        }
        return value;
    }
    /**
     * Gets the maximum random long between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias away from zero, but all possible values are between 0,
     * inclusive, and bound, exclusive.
     * @param bound the outer exclusive bound
     * @param trials how many numbers to generate and get the maximum of
     * @return the maximum generated long between 0 and bound out of the specified amount of trials
     */
    public long maxLongOf(final long bound, final int trials)
    {
        long value = nextLong(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.max(value, nextLong(bound));
        }
        return value;
    }

    /**
     * Gets the minimum random int between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias toward zero, but all possible values are between 0, inclusive,
     * and bound, exclusive.
     * @param bound the outer exclusive bound
     * @param trials how many numbers to generate and get the minimum of
     * @return the minimum generated int between 0 and bound out of the specified amount of trials
     */
    public int minIntOf(final int bound, final int trials)
    {
        int value = nextIntHasty(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.min(value, nextIntHasty(bound));
        }
        return value;
    }
    /**
     * Gets the maximum random int between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias away from zero, but all possible values are between 0,
     * inclusive, and bound, exclusive.
     * @param bound the outer exclusive bound
     * @param trials how many numbers to generate and get the maximum of
     * @return the maximum generated int between 0 and bound out of the specified amount of trials
     */
    public int maxIntOf(final int bound, final int trials)
    {
        int value = nextIntHasty(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.max(value, nextIntHasty(bound));
        }
        return value;
    }

    /**
     * Gets the minimum random double between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias toward zero, but all possible values are between 0, inclusive,
     * and bound, exclusive.
     * @param bound the outer exclusive bound
     * @param trials how many numbers to generate and get the minimum of
     * @return the minimum generated double between 0 and bound out of the specified amount of trials
     */
    public double minDoubleOf(final double bound, final int trials)
    {
        double value = nextDouble(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.min(value, nextDouble(bound));
        }
        return value;
    }

    /**
     * Gets the maximum random double between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias away from zero, but all possible values are between 0,
     * inclusive, and bound, exclusive.
     * @param bound the outer exclusive bound
     * @param trials how many numbers to generate and get the maximum of
     * @return the maximum generated double between 0 and bound out of the specified amount of trials
     */
    public double maxDoubleOf(final double bound, final int trials)
    {
        double value = nextDouble(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.max(value, nextDouble(bound));
        }
        return value;
    }
    /**
     * Gets the minimum random float between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias toward zero, but all possible values are between 0, inclusive,
     * and bound, exclusive.
     * @param bound the outer exclusive bound
     * @param trials how many numbers to generate and get the minimum of
     * @return the minimum generated float between 0 and bound out of the specified amount of trials
     */
    public float minFloatOf(final float bound, final int trials)
    {
        float value = nextFloat(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.min(value, nextFloat(bound));
        }
        return value;
    }

    /**
     * Gets the maximum random float between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias away from zero, but all possible values are between 0,
     * inclusive, and bound, exclusive.
     * @param bound the outer exclusive bound
     * @param trials how many numbers to generate and get the maximum of
     * @return the maximum generated float between 0 and bound out of the specified amount of trials
     */
    public float maxFloatOf(final float bound, final int trials)
    {
        float value = nextFloat(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.max(value, nextFloat(bound));
        }
        return value;
    }


    @Override
    public String toString() {
        return "RNG with Randomness Source " + random;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RNG)) return false;

        RNG rng = (RNG) o;

        return random.equals(rng.random);
    }

    @Override
    public int hashCode() {
        return 31 * random.hashCode();
    }

    /**
     * Gets a random portion of data (an array), assigns that portion to output (an array) so that it fills as much as
     * it can, and then returns output. Will only use a given position in the given data at most once; does this by
     * generating random indices for data's elements, but only as much as needed, assigning the copied section to output
     * and not modifying data.
     * <br>
     * Based on http://stackoverflow.com/a/21460179 , credit to Vincent van der Weele; modifications were made to avoid
     * copying or creating a new generic array (a problem on GWT).
     *
     * @param data   an array of T; will not be modified.
     * @param output an array of T that will be overwritten; should always be instantiated with the portion length
     * @param <T>    can be any non-primitive type.
     * @return an array of T that has length equal to output's length and may contain unchanged elements (null if output
     * was empty) if data is shorter than output
     */
    public <T> T[] randomPortion(T[] data, T[] output) {
        int length = data.length;
        int n = Math.min(length, output.length);
        int[] mapping = ArrayTools.range(n);
        for (int i = 0; i < n; i++) {
            int r = nextIntHasty(length);
            output[i] = data[mapping[r]];
            mapping[r] = mapping[--length];
        }

        return output;
    }


    /**
     * Gets a random double between 0.0 inclusive and 1.0 inclusive.
     *
     * @return a double between 0.0 (inclusive) and 1.0 (inclusive)
     */
    public double nextDoubleInclusive()
    {
        return (random.nextLong() & 0x1fffffffffffffL) * 0x1.0000000000001p-53;
    }

    /**
     * This returns a random double between 0.0 (inclusive) and outer (inclusive). The value for outer can be positive
     * or negative. Because of how math on doubles works, there are at most 2 to the 53 values this can return for any
     * given outer bound, and very large values for outer will not necessarily produce all numbers you might expect.
     *
     * @param outer the outer inclusive bound as a double; can be negative or positive
     * @return a double between 0.0 (inclusive) and outer (inclusive)
     */
    public double nextDoubleInclusive(final double outer) {
        return (random.nextLong() & 0x1fffffffffffffL) * 0x1.0000000000001p-53 * outer;
    }

    /**
     * Gets a random float between 0.0f inclusive and 1.0f inclusive.
     *
     * @return a float between 0f (inclusive) and 1f (inclusive)
     */
    public float nextFloatInclusive() {
        return (random.nextLong() & 0xFFFFFF) * 0x1.000002p-24f;
    }

    /**
     * This returns a random float between 0.0f (inclusive) and outer (inclusive). The value for outer can be positive
     * or negative. Because of how math on floats works, there are at most 2 to the 24 values this can return for any
     * given outer bound, and very large values for outer will not necessarily produce all numbers you might expect.
     *
     * @param outer the outer inclusive bound as a float; can be negative or positive
     * @return a float between 0f (inclusive) and outer (inclusive)
     */
    public float nextFloatInclusive(final float outer) {
        return (random.nextLong() & 0xFFFFFF) * 0x1.000002p-24f * outer;
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
     *
     * @param width  the upper bound (exclusive) for x coordinates
     * @param height the upper bound (exclusive) for y coordinates
     * @return a random Coord between (0,0) inclusive and (width,height) exclusive
     */
    public Coord nextCoord(int width, int height) {
        final long n = random.nextLong();
        return Coord.get((int) ((width * (n >>> 33)) >> 31), (int) ((height * (n & 0x7FFFFFFFL)) >> 31));
    }

    /**
     * Use that to get random cells in a rectangular map.
     *
     * @param width  The map's width (bounds the x-coordinate in returned coords).
     * @param height The map's height (bounds the y-coordinate in returned coords).
     * @param size   The number of elements in the returned iterable or anything
     *               negative for no bound (in which case the iterator is infinite, it's
     *               up to you to bound your iteration).
     * @return An iterable that returns random cells in the rectangle (0,0)
     * (inclusive) .. (width, height) (exclusive).
     */
    public Iterable<Coord> getRandomCellsIterable(final int width, final int height, final int size) {
        return new Iterable<Coord>() {
            @Override
            public Iterator<Coord> iterator() {
                return new Iterator<Coord>() {

                    /**
                     * The number of elements returned so far
                     */
                    int returned = 0;

                    @Override
                    public boolean hasNext() {
                        return size < 0 || returned < size;
                    }

                    @Override
                    public Coord next() {
                        if (!hasNext())
                            throw new NoSuchElementException();
                        returned++;
                        return nextCoord(width, height);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Gets an array of unique Coords, from (startX,startY) inclusive to (startX+width,startY+height) exclusive, in a
     * random order, with the array containing {@code width * height} items.
     *
     * @param startX the inclusive starting x position
     * @param startY the inclusive starting y position
     * @param width  the width of the space to place Coords in, extending from startX
     * @param height the height of the space to place Coords in, extending from startY
     * @return an array containing {@code width * height} Coord items in random order, inside the given bounds
     */
    public Coord[] getRandomUniqueCells(final int startX, final int startY, final int width, final int height) {
        if (width <= 0 || height <= 0)
            return new Coord[0];
        return getRandomUniqueCells(startX, startY, width, height, new Coord[width * height]);
    }

    /**
     * Gets an array of unique Coords, from (startX,startY) inclusive to (startX+width,startY+height) exclusive, in a
     * random order, with the array containing {@code Math.min(width * height, size)} items. If size is less than width
     * times height, then not all Coords in the space will be used.
     *
     * @param startX the inclusive starting x position
     * @param startY the inclusive starting y position
     * @param width  the width of the space to place Coords in, extending from startX
     * @param height the height of the space to place Coords in, extending from startY
     * @param size   the size of the array to return; only matters if it is smaller than {@code width * height}
     * @return an array containing {@code Math.min(width * height, size)} Coord items in random order, inside the given bounds
     */
    public Coord[] getRandomUniqueCells(final int startX, final int startY, final int width, final int height,
                                        final int size) {
        if (width <= 0 || height <= 0 || size <= 0)
            return new Coord[0];
        return getRandomUniqueCells(startX, startY, width, height, new Coord[Math.min(width * height, size)]);
    }

    /**
     * Assigns to dest an array of unique Coords, from (startX,startY) inclusive to (startX+width,startY+height)
     * exclusive, in a random order, with dest after this is called containing the lesser of {@code width * height} or
     * {@code dest.length} items. This will not allocate a new array for dest, but will create a temporary int array for
     * handling the shuffle.
     *
     * @param startX the inclusive starting x position
     * @param startY the inclusive starting y position
     * @param width  the width of the space to place Coords in, extending from startX
     * @param height the height of the space to place Coords in, extending from startY
     * @param dest   a Coord array that will be modified to contain randomly-ordered Coords, but will not be resized
     * @return dest, now with up to its first {@code width * height} items assigned to random Coords inside the given bounds
     */
    public Coord[] getRandomUniqueCells(final int startX, final int startY, final int width, final int height,
                                        final Coord[] dest) {
        if (width <= 0 || height <= 0 || dest == null || dest.length <= 0)
            return dest;
        int[] o = randomOrdering(width * height);
        for (int i = 0; i < o.length && i < dest.length; i++) {
            dest[i] = Coord.get(startX + o[i] % width, startY + o[i] / width);
        }
        return dest;
    }

    /**
     * Returns this RNG in a way that can be deserialized even if only {@link IRNG}'s methods can be called.
     * @return a {@link Serializable} view of this RNG; always {@code this}
     */
    @Override
    public Serializable toSerializable() {
        return this;
    }
}
