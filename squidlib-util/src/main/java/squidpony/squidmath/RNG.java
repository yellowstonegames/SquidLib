package squidpony.squidmath;

import squidpony.ArrayTools;
import squidpony.annotation.GwtIncompatible;

import java.io.Serializable;
import java.util.*;

/**
 * A wrapper class for working with random number generators in a more friendly way.
 * <p>
 * Includes methods for getting values between two numbers and for getting
 * random elements from a collection or array. There are methods to shuffle
 * a collection and to get a random ordering that can be applied as one shuffle
 * across multiple collections, such as via {@link OrderedMap#reorder(int...)},
 * {@link ArrayTools#reorder(ArrayList, int...)}, and so on. You can construct
 * an RNG with all sorts of RandomnessSource implementations, and choosing them
 * is usually not a big concern because the default works very well.
 * <br>
 * But if you do want advice on what RandomnessSource to use... LightRNG is the
 * default, and is very fast, but relative to many of the others it has a
 * significantly shorter period (the amount of random  numbers it will go through
 * before repeating the sequence), at {@code pow(2, 64)} as opposed to XorRNG and
 * XoRoRNG's {@code pow(2, 128)}. LightRNG also allows the current RNG state
 * to be retrieved and altered with {@code getState()} and {@code setState()}. For
 * most cases, you should decide between LightRNG, XoRoRNG, and other
 * RandomnessSource implementations based on your needs for period length and state
 * manipulation (LightRNG is also used internally by almost all {@link StatefulRNG}
 * objects). You might want significantly less predictable random results, which
 * {@link IsaacRNG} and {@link Isaac32RNG} can provide, along with a large period.
 * You may want a very long period of random numbers, which would suggest
 * {@link LongPeriodRNG} as the best choice. You may want better performance on
 * 32-bit machines or especially on GWT (which has to emulate Java's behavior with
 * 64-bit longs), which would mean {@link PintRNG} (for generating only ints via
 * {@link PintRNG#next(int)}, since its {@link PintRNG#nextLong()} method is very
 * slow) or {@link FlapRNG} (for generating ints and longs at relatively good speed
 * using mainly int math; also capable of the state changing that LightRNG can do).
 * {@link ThunderRNG} is the fastest generator we have, and has a decent period when
 * considering all bits, but if you only consider the less-significant bits then it
 * has a very poor period. This bad behavior is similar to how linear congruential
 * generators act, such as {@link java.util.Random}, which simply truncates off the
 * lower bits.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger
 * @author smelC
 */
public class RNG implements Serializable {

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
    public double between(double min, double max) {
        return min + (max - min) * nextDouble();
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
    public <T> T getRandomElement(List<T> list) {
        if (list.size() <= 0) {
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
     * should have better performance.
     *
     * @param l   A {@link List} that will not be modified by this method. All elements of this parameter will be
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
     * @param width  the width of the space to place Coords in
     * @param height the height of the space to place Coords in
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
     * @param width  the width of the space to place Coords in
     * @param height the height of the space to place Coords in
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
     * @param width  the width of the space to place Coords in
     * @param height the height of the space to place Coords in
     * @param dest   a Coord array that will be modified to contain randomly-ordered Coords, but will not be resized
     * @return dest, now with up to its first {@code width * height} items assigned to random Coords inside the given bounds
     */
    public Coord[] getRandomUniqueCells(final int startX, final int startY, final int width, final int height,
                                        final Coord[] dest) {
        if (width <= 0 || height <= 0 || dest == null || dest.length <= 0)
            return dest;
        int[] o = randomOrdering(width * height);
        for (int i = 0; i < o.length && i < dest.length; i++) {
            dest[i] = Coord.get(startX + o[i] / width, startY + o[i] % width);
        }
        return dest;
    }

    /**
     * Shuffle an array using the Fisher-Yates algorithm and returns a shuffled copy.
     * Not GWT-compatible; use the overload that takes two arrays if you use GWT.
     * <br>
     * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
     *
     * @param elements an array of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @return a shuffled copy of elements
     */
    @GwtIncompatible
    public <T> T[] shuffle(T[] elements) {
        int n = elements.length;
        T[] array = Arrays.copyOf(elements, n);
        for (int i = 0; i < n; i++) {
            int r = i + nextInt(n - i);
            T t = array[r];
            array[r] = array[i];
            array[i] = t;
        }
        return array;
    }

    /**
     * Shuffles an array in place using the Fisher-Yates algorithm.
     * If you don't want the array modified, use {@link #shuffle(Object[], Object[])}.
     * Unlike {@link #shuffle(Object[])}, this is GWT-compatible.
     * <br>
     * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
     *
     * @param elements an array of T; <b>will</b> be modified
     * @param <T>      can be any non-primitive type.
     * @return elements after shuffling it in-place
     */
    public <T> T[] shuffleInPlace(T[] elements) {
        for (int i = elements.length - 1; i > 0; i--) {
            int r = nextInt(i + 1);
            T t = elements[r];
            elements[r] = elements[i];
            elements[i] = t;
        }
        return elements;
    }

    /**
     * Shuffle an array using the "inside-out" Fisher-Yates algorithm. DO NOT give the same array for both elements and
     * dest, since the prior contents of dest are rearranged before elements is used, and if they refer to the same
     * array, then you can end up with bizarre bugs where one previously-unique item shows up dozens of times. If
     * possible, create a new array with the same length as elements and pass it in as dest; the returned value can be
     * assigned to whatever you want and will have the same items as the newly-formed array.
     * <br>
     * https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#The_.22inside-out.22_algorithm
     *
     * @param elements an array of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @param dest     Where to put the shuffle. If it does not have the same length as {@code elements}, this will use the
     *                 randomPortion method of this class to fill the smaller dest. MUST NOT be the same array as elements!
     * @return {@code dest} after modifications
     */
	/* This method has this prototype to be compatible with GWT. */
    public <T> T[] shuffle(T[] elements, T[] dest) {
        if (dest.length != elements.length)
            return randomPortion(elements, dest);
        for (int i = 0; i < elements.length; i++) {
            int r = nextInt(i + 1);
            if (r != i)
                dest[i] = dest[r];
            dest[r] = elements[i];
        }
        return dest;
    }

    /**
     * Shuffles a {@link Collection} of T using the Fisher-Yates algorithm and returns an ArrayList of T.
     *
     * @param elements a Collection of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @return a shuffled ArrayList containing the whole of elements in pseudo-random order.
     */
    public <T> ArrayList<T> shuffle(Collection<T> elements) {
        return shuffle(elements, null);
    }

    /**
     * Shuffles a {@link Collection} of T using the Fisher-Yates algorithm. The result
     * is allocated if {@code buf} is null or if {@code buf} isn't empty,
     * otherwise {@code elements} is poured into {@code buf}.
     *
     * @param elements a Collection of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @return a shuffled ArrayList containing the whole of elements in pseudo-random order.
     */
    public <T> ArrayList<T> shuffle(Collection<T> elements, /*@Nullable*/ ArrayList<T> buf) {
        final ArrayList<T> al;
        if (buf == null || !buf.isEmpty())
            al = new ArrayList<>(elements);
        else {
            al = buf;
            al.addAll(elements);
        }
        int n = al.size();
        for (int i = 0; i < n; i++) {
            Collections.swap(al, i + nextInt(n - i), i);
        }
        return al;
    }

    /**
     * Generates a random permutation of the range from 0 (inclusive) to length (exclusive).
     * Useful for passing to OrderedMap or OrderedSet's reorder() methods.
     *
     * @param length the size of the ordering to produce
     * @return a random ordering containing all ints from 0 to length (exclusive)
     */
    public int[] randomOrdering(int length) {
        if (length <= 0)
            return new int[0];
        int[] dest = new int[length];
        for (int i = 0; i < length; i++) {
            int r = nextInt(i + 1);
            if (r != i)
                dest[i] = dest[r];
            dest[r] = i;
        }
        return dest;
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
    public int[] randomOrdering(int length, int[] dest) {
        if (dest == null) return null;
        for (int i = 0; i < length && i < dest.length; i++) {
            int r = nextIntHasty(i + 1);
            if (r != i)
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
     *
     * @param data   an array of T; will not be modified.
     * @param output an array of T that will be overwritten; should always be instantiated with the portion length
     * @param <T>    can be any non-primitive type.
     * @return an array of T that has length equal to output's length and may contain unchanged elements (null if output
     * was empty) if data is shorter than output
     */
    public <T> T[] randomPortion(T[] data, T[] output) {
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
        int[] mapping = ArrayTools.range(n);
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
     *
     * @param data  a List of T; will not be modified.
     * @param count the non-negative number of elements to randomly take from data
     * @param <T>   can be any non-primitive type
     * @return a List of T that has length equal to the smaller of count or data.length
     */
    public <T> List<T> randomPortion(List<T> data, int count) {
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
        int[] data = new int[n];

        for (int e = start, i = 0; e < end; e++) {
            data[i++] = e;
        }

        for (int i = 0; i < n; i++) {
            int r = i + nextInt(n - i), t = data[r];
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
        //return Double.longBitsToDouble(0x3FF0000000000000L | random.nextLong() >>> 12) - 1.0;
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
     *
     * @return a random boolean.
     */
    public boolean nextBoolean() {
        return nextLong() < 0L;
    }

    /**
     * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive).
     *
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
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    public int nextInt(final int bound) {
        if (bound <= 0) return 0;
        int threshold = (0x7fffffff - bound + 1) % bound;
        for (; ; ) {
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
     *
     * @param bound the upper bound (exclusive); behavior is undefined if bound is negative
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
     * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both inclusive).
     *
     * @return a 32-bit random int.
     */
    public int nextInt() {
        return next(32);
    }

    /**
     * Get up to 32 bits (inclusive) of random state from the RandomnessSource.
     *
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
     *
     * @return a copy of this RNG
     */
    public RNG copy() {
        return new RNG(random.copy());
    }

    /**
     * Generates a random 64-bit long with a number of '1' bits (Hamming weight) approximately equal to bitCount.
     * For example, calling this with a parameter of 32 will be equivalent to calling nextLong() on this object's
     * RandomnessSource (it doesn't consider overridden nextLong() methods, where present, on subclasses of RNG).
     * Calling this with a parameter of 16 will have on average 16 of the 64 bits in the returned long set to '1',
     * distributed pseudo-randomly, while a parameter of 47 will have on average 47 bits set. This can be useful for
     * certain code that uses bits to represent data but needs a different ratio of set bits to unset bits than 1:1.
     * <br>
     * Implementors should limit any overriding method to calling and returning super(), potentially storing any extra
     * information they need to internally, but should not change the result. This works based on a delicate balance of
     * the RandomnessSource producing bits with an even 50% chance of being set, regardless of position, and RNG
     * subclasses that alter the odds won't work as expected here, particularly if those subclasses use doubles
     * internally (which almost always produce less than 64 random bits). You should definitely avoid using certain
     * RandomnessSources that aren't properly pseudo-random, such as any QRNG class (SobolQRNG and VanDerCorputQRNG,
     * pretty much), since these won't fill all 64 bits with equal likelihood.
     *
     * @param bitCount an int, only considered if between 0 and 64, that is the average number of bits to set
     * @return a 64-bit long that, on average, should have bitCount bits set to 1, potentially anywhere in the long
     */
    public long approximateBits(int bitCount) {
        if (bitCount <= 0)
            return 0L;
        if (bitCount >= 64)
            return -1L;
        if (bitCount == 32)
            return random.nextLong();
        boolean high = bitCount > 32;
        int altered = (high ? 64 - bitCount : bitCount), lsb = Integer.lowestOneBit(altered);
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
     *
     * @return a random long with 32 "1" bits, distributed so exactly one bit is "1" for each pair of bits
     */
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
        return random.hashCode();
    }
}
