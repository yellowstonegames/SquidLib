/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package squidpony.squidmath;

import squidpony.ArrayTools;

import java.io.Serializable;
import java.util.*;

import static squidpony.squidmath.LowStorageShuffler.round;
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
 * is usually not a big concern because the default works very well. If you target
 * GWT, then it is suggested that you use {@link GWTRNG} instead of RNG; both
 * implement {@link IRNG}, which is enough for most usage across SquidLib, but
 * GWTRNG is optimized heavily for better performance on GWT, even returning long
 * values faster than implementations that natively do their math on longs. It has
 * worse performance on 64-bit PCs and mobile devices, but should also have better
 * performance on 32-bit PCs and mobile devices.
 * <br>
 * But if you do want advice on what RandomnessSource to use... {@link DiverRNG}
 * is the default, and is the fastest generator that passes most tests and can
 * produce all 64-bit values, and though relative to many of the others it has a
 * significantly shorter period (the amount of random numbers it will go through
 * before repeating the sequence), this almost never matters in games, and is
 * primarily relevant for massively-parallel scientific programs. DiverRNG has a
 * period of {@code pow(2, 64)} as opposed to {@link XoRoRNG}'s
 * {@code pow(2, 128) - 1}, or {@link LongPeriodRNG}'s {@code pow(2, 1024) - 1}.
 * {@link LightRNG} is a solid choice and a former default RandomnessSource;
 * additional features of LightRNG are exposed in {@link MoonwalkRNG} and using
 * MoonwalkRNG is recommended if you need unusual features like skipping backwards
 * in a random number sequence, taking a result of a nextLong() call and reversing
 * it to get the state that produced it, or calculating the distance in number of
 * nextLong() calls between two results of nextLong() calls. LightRNG is a
 * StatefulRandomness, which lets it be used in {@link StatefulRNG}, and so is
 * DiverRNG, but LightRNG is also a {@link SkippingRandomness}, which means you can
 * leap forward or backwards in its sequence very efficiently (DiverRNG is not a
 * SkippingRandomness). {@link ThrustAltRNG} provides similar qualities to LightRNG,
 * and is one of the fastest generators here, but can't produce all possible 64-bit
 * values (possibly some 32-bit values as well); it was the default at one point so
 * you may want to keep compatibility with some versions by specifying ThrustAltRNG.
 * The defaults have changed in the past as issues are found in various generators;
 * LightRNG has high quality all-around but is slower than the other defaults,
 * ThrustAltRNG can't produce all results, LinnormRNG passed tests in an earlier
 * version of the PractRand test suite but now fails in the current version, and now
 * the default is DiverRNG, which shares the known issue of LightRNG and LinnormRNG
 * that it can't produce the same result twice from {@link #nextLong()} until the
 * generator exhausts its period and repeats its output from the beginning.
 * For most cases, you should decide between DiverRNG, ThrustAltRNG, LightRNG,
 * LongPeriodRNG, MiniMover64RNG, and XoshiroStarPhi32RNG based on your priorities.
 * Some tasks are better solved by using a different class, usually {@link GWTRNG},
 * which can always be serialized on GWT to save its state easily and is usually the
 * fastest substitute for RNG on that platform. DiverRNG is the best if you want high
 * speed, very good quality of randomness, and expect to generate a reasonable quantity
 * of numbers for a game (less than 18446744073709551616 numbers) without any single
 * results being impossible. LightRNG is the second-best at the above criteria, but is
 * the best option if you need an RNG that can skip backwards or jump forwards without
 * incurring speed penalties. LongPeriodRNG is best if you for some reason need a massive
 * amount of random numbers (as in, ten quintillion would be far too little) or want to
 * split up such a large generator into unrelated subsequences. XoshiroStarPhi32RNG is
 * best if GWT is a possible target but you either need to generate more than
 * 18446744073709551616 numbers (but less than 340282366920938463463374607431768211455
 * numbers) or you need to ensure that each 128-bit chunk of output is unique; if GWT is
 * a target but those specific needs don't matter, use GWTRNG. ThrustAltRNG and
 * MiniMover64RNG are both faster than DiverRNG usually (MiniMover64RNG is the fastest),
 * but because they are unable to generate some outputs, that may make them a poor choice
 * for some usage (ThrustAltRNG also has some bias toward specific numbers and produces
 * them more frequently, but not frequently enough to make it fail statistical tests, and
 * ThrustAltRNG can skip around in its output sequence like LightRNG).
 * <br>
 * There are many more RandomnessSource implementations! You might want significantly less
 * predictable random results, which  {@link IsaacRNG} can provide, along with a
 * large period. The quality of {@link PermutedRNG} is also good, usually, and it
 * has a sound basis in PCG-Random, an involved library with many variants on its
 * RNGs.
 * <br>
 * There may be reasons why you would want a random number generator that uses 32-bit
 * math instead of the more common 64-bit math, but using a 32-bit int on desktop and
 * Android won't act the same as that same 32-bit int on GWT. Since GWT is stuck with
 * JavaScript's implementation of ints with doubles, overflow (which is needed for an
 * RNG) doesn't work with ints as expected, but does with GWT's implementation of longs.
 * If targeting GWT, you should probably consider {@link GWTRNG} or {@link SilkRNG},
 * which would be used in place of this class and have a similar API. You can instead
 * choose a RandomnessSource that is efficient on GWT; {@link Lathe32RNG} is
 * significantly faster at producing int values on GWT than any long-based generator,
 * and will produce the same results on GWT as on desktop or Android (not all 32-bit
 * generators do this). {@link Starfish32RNG} goes one step further than Lathe32RNG at
 * an even distribution, and has better quality, but is slightly slower; it is also used
 * internally by GWTRNG. While Lathe32RNG can produce all ints over the course of its
 * period, it will produce some pairs of ints, or longs, more often than others and will
 * never produce some longs. Starfish32RNG will produce all longs but one.
 * {@link Oriole32RNG} and {@link XoshiroStarPhi32RNG} are also GWT-safe, but other
 * generators that were thought to be GWT-friendly are not. {@link PintRNG} is a
 * GWT-unsafe generators with other uses, but should not be used on GWT. All other
 * RandomnessSources use longs, and so will be slower than the recommended Starfish32RNG
 * or Lathe32RNG on GWT, but probably are much faster on 64-bit JREs.
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger
 * @author smelC
 */
public class RNG extends AbstractRNG implements Serializable {

	protected RandomnessSource random;
	protected Random ran;

    private static final long serialVersionUID = 2352426757973945105L;


    /**
     * Default constructor; uses {@link DiverRNG}, which is of high quality, but low period (which rarely matters
     * for games), and has excellent speed, tiny state size, and natively generates 64-bit numbers.
     * <br>
     * Previous versions of SquidLib used different implementations, including {@link LightRNG}, {@link ThrustAltRNG},
     * {@link LinnormRNG}, and {@link MersenneTwister}. You can still use one of these by instantiating one of those
     * classes and passing it to {@link #RNG(RandomnessSource)}, which may be the best way to ensure the same results
     * across versions.
     */
    public RNG() {
        this(new DiverRNG());
    }

    /**
     * Default constructor; uses {@link DiverRNG}, which is of high quality, but low period (which rarely matters
     * for games), and has excellent speed, tiny state size, and natively generates 64-bit numbers. The seed can be
     * any long, including 0.
     * @param seed any long
     */
    public RNG(long seed) {
        this(new DiverRNG(seed));
    }

    /**
     * String-seeded constructor; uses a platform-independent hash of the String (it does not use String.hashCode,
     * instead using {@link CrossHash#hash64(CharSequence)}) as a seed for {@link DiverRNG}, which is of high quality,
     * but low period (which rarely matters for games), and has excellent speed, tiny state size, and natively generates
     * 64-bit numbers.
     */
    public RNG(CharSequence seedString) {
        this(new DiverRNG(CrossHash.hash64(seedString)));
    }

    /**
     * Uses the provided source of randomness for all calculations. This constructor should be used if an alternate
     * RandomnessSource other than DiverRNG is desirable, such as to keep compatibility with earlier SquidLib
     * versions that defaulted to MersenneTwister, LightRNG, ThrustAltRNG, or LinnormRNG.
     * <br>
     * If the parameter is null, this is equivalent to using {@link #RNG()} as the constructor.
     * @param random the source of pseudo-randomness, such as a LightRNG or LongPeriodRNG object
     */
    public RNG(RandomnessSource random) {
        this.random = (random == null) ? new DiverRNG() : random;
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
         * a DiverRNG with a random seed.
         */
        public CustomRandom() {
            randomnessSource = new DiverRNG();
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

        /**
         * Returns the next pseudorandom, uniformly distributed {@code long}
         * value from this random number generator's sequence. The general
         * contract of {@code nextLong} is that one {@code long} value is
         * pseudorandomly generated and returned.
         *
         * @return the next pseudorandom, uniformly distributed {@code long}
         * value from this random number generator's sequence
         */
        @Override
        public long nextLong() {
            return randomnessSource.nextLong();
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
            return new ArrayList<>(0);

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
            return new ArrayList<>(0);

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
     * approach -1f and 1f, but not go beyond those bounds. This is similar to {@link Random#nextGaussian()} in that it
     * uses a curved distribution, but it is not the same. The distribution for the values is similar to Irwin-Hall, and
     * is frequently near 0 but not too-rarely near -1f or 1f. It cannot produce values greater than or equal to 1f, or
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
     * Get a random bit of state, interpreted as true or false with approximately equal likelihood.
     * This may have better behavior than {@code rng.next(1)}, depending on the RandomnessSource implementation; the
     * default DiverRNG will behave fine, as will LightRNG and ThrustAltRNG (these all use similar algorithms), but
     * the  normally-high-quality XoRoRNG will produce very predictable output with {@code rng.next(1)} and very good
     * output with {@code rng.nextBoolean()}. This is a known and considered flaw of Xoroshiro128+, the algorithm used
     * by XoRoRNG, and a large number of generators have lower quality on the least-significant bit than the most-
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
     * Returns a random non-negative integer between 0 (inclusive) and the given bound (exclusive),
     * or 0 if the bound is 0. The bound can be negative, which will produce 0 or a negative result.
     * Uses an aggressively optimized technique that has some bias, but mostly for values of
     * bound over 1 billion. This method is no more "hasty" than {@link #nextInt(int)}, but it is a little
     * faster than that method because this avoids special behavior for when bound is negative.
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
     * @param bound the outer exclusive bound; may be negative or positive
     * @param trials how many numbers to generate and get the minimum of
     * @return the minimum generated long between 0 and bound out of the specified amount of trials
     */
    public long minLongOf(final long bound, final int trials)
    {
        long value = nextSignedLong(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.min(value, nextSignedLong(bound));
        }
        return value;
    }
    /**
     * Gets the maximum random long between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias away from zero, but all possible values are between 0,
     * inclusive, and bound, exclusive.
     * @param bound the outer exclusive bound; may be negative or positive
     * @param trials how many numbers to generate and get the maximum of
     * @return the maximum generated long between 0 and bound out of the specified amount of trials
     */
    public long maxLongOf(final long bound, final int trials)
    {
        long value = nextSignedLong(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.max(value, nextSignedLong(bound));
        }
        return value;
    }

    /**
     * Gets the minimum random int between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias toward zero, but all possible values are between 0, inclusive,
     * and bound, exclusive.
     * @param bound the outer exclusive bound; may be negative or positive
     * @param trials how many numbers to generate and get the minimum of
     * @return the minimum generated int between 0 and bound out of the specified amount of trials
     */
    public int minIntOf(final int bound, final int trials)
    {
        int value = nextSignedInt(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.min(value, nextSignedInt(bound));
        }
        return value;
    }
    /**
     * Gets the maximum random int between 0 and {@code bound} generated out of {@code trials} generated numbers.
     * Useful for when numbers should have a strong bias away from zero, but all possible values are between 0,
     * inclusive, and bound, exclusive.
     * @param bound the outer exclusive bound; may be negative or positive
     * @param trials how many numbers to generate and get the maximum of
     * @return the maximum generated int between 0 and bound out of the specified amount of trials
     */
    public int maxIntOf(final int bound, final int trials)
    {
        int value = nextSignedInt(bound);
        for (int i = 1; i < trials; i++) {
            value = Math.max(value, nextSignedInt(bound));
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
     * it can, and then returns output. Will only use a given position in the given data at most once; uses a Feistel
     * Network to accomplish this without allocations. Internally, makes 4 calls to {@link #nextLong()}, regardless of
     * the data being randomized.
     * <br>
     * Uses approximately the same code as {@link LowStorageShuffler}, but without any object or array allocations.
     *
     * @param data   an array of T; will not be modified.
     * @param output an array of T that will be overwritten; should always be instantiated with the portion length
     * @param <T>    can be any non-primitive type.
     * @return output, after {@code Math.min(output.length, data.length)} unique items have been put into it from data
     */
    @Override
    public <T> T[] randomPortion(T[] data, T[] output) {
        final int length = data.length, n = Math.min(length, output.length);
        // calculate next power of 4.  Needed since the balanced Feistel network needs
        // an even number of bits to work with
        int pow4 = HashCommon.nextPowerOfTwo(length);
        if((pow4 & 0x55555554) == 0)
            pow4 = HashCommon.nextPowerOfTwo(pow4);
        pow4--;
        // calculate our left and right masks to split our indices for the Feistel network
        final int halfBits = Integer.bitCount(pow4) >>> 1;
        final int rightMask = pow4 >>> halfBits;
        final int leftMask = pow4 ^ rightMask;
        final long key0 = nextLong(), key1 = nextLong(), key2 = nextLong(), key3 = nextLong();
        int index = 0;
        for (int i = 0; i < n; i++) {
            int shuffleIndex;
            // index is the index to start searching for the next number at
            while (index <= pow4)
            {
                // break our index into the left and right half
                int left = (index & leftMask) >>> halfBits;
                int right = (index & rightMask);
                // do 2 Feistel rounds
                int newRight;
                newRight = left + round(right, key0) & rightMask;
                left = right;
                right = newRight;
                newRight = left + round(right, key1) & rightMask;
                left = right;
                right = newRight;
                newRight = left + round(right, key2) & rightMask;
                left = right;
                right = newRight;
                newRight = left + round(right, key3) & rightMask;
                shuffleIndex = right << halfBits | newRight;
                index++;

                // if we found a valid index, return it!
                if (shuffleIndex < length) {
                    output[i] = data[shuffleIndex];
                    break;
                }
            }
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
        return random.next(24) * 0x1.000002p-24f;
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
        return random.next(24) * 0x1.000002p-24f * outer;
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
                    int returned;

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
