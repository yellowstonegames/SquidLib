package squidpony.squidmath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Interface for full-featured random number generators to implement (it does more than, and includes all of,
 * {@link RandomnessSource}). It's a stripped down version of the original {@link RNG}. It's an interface instead of a
 * class, to be able to implement using random number generators that don't implement RandomnessSource, like libGDX's
 * RandomXS128, or to hard-code the RandomnessSource to avoid overhead or use some methods differently (like preferring
 * 32-bit math or optimizing for GWT, as {@link GWTRNG} does). You can use any IRNG as a RandomnessSource, but that only
 * allows its {@link #next(int)}, {@link #nextLong()}, and {@link #copy()} methods to be called, so most usage that can
 * benefit from methods like {@link #nextDouble()} or {@link #between(int, int)} should prefer IRNG for parameter types.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger
 * @author smelC
 */
public interface IRNG extends RandomnessSource {

    /**
     * Get up to 32 bits (inclusive) of random output; the int this produces
     * will not require more than {@code bits} bits to represent.
     *
     * @param bits an int between 1 and 32, both inclusive
     * @return a random number that fits in the specified number of bits
     */
    int next(int bits);

    /**
     * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both inclusive).
     *
     * @return a 32-bit random int.
     */
    int nextInt();

    /**
     * Returns a random non-negative integer below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    int nextInt(final int bound);

    /**
     * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive).
     *
     * @return a 64-bit random long.
     */
    long nextLong();
    
    /**
     * Returns a random long below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    long nextLong(final long bound);

    /**
     * Get a random bit of state, interpreted as true or false with approximately equal likelihood.
     * @return a random boolean.
     */
    boolean nextBoolean();

    /**
     * Gets a random double between 0.0 inclusive and 1.0 exclusive.
     * This returns a maximum of 0.9999999999999999 because that is the largest double value that is less than 1.0 .
     *
     * @return a double between 0.0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    double nextDouble();
    /**
     * This returns a random double between 0.0 (inclusive) and outer (exclusive). The value for outer can be positive
     * or negative. Because of how math on doubles works, there are at most 2 to the 53 values this can return for any
     * given outer bound, and very large values for outer will not necessarily produce all numbers you might expect.
     *
     * @param outer the outer exclusive bound as a double; can be negative or positive
     * @return a double between 0.0 (inclusive) and outer (exclusive)
     */
    double nextDouble(final double outer);

    /**
     * Gets a random float between 0.0f inclusive and 1.0f exclusive.
     * This returns a maximum of 0.99999994 because that is the largest float value that is less than 1.0f .
     *
     * @return a float between 0f (inclusive) and 0.99999994f (inclusive)
     */
    float nextFloat();
    /**
     * This returns a random float between 0.0f (inclusive) and outer (exclusive). The value for outer can be positive
     * or negative. Because of how math on floats works, there are at most 2 to the 24 values this can return for any
     * given outer bound, and very large values for outer will not necessarily produce all numbers you might expect.
     *
     * @param outer the outer exclusive bound as a float; can be negative or positive
     * @return a float between 0f (inclusive) and outer (exclusive)
     */
    float nextFloat(final float outer);
    /**
     * Exclusive on bound (which may be positive or negative), with an inner bound of 0.
     * If bound is negative this returns a negative long; if bound is positive this returns a positive long. The bound
     * can even be 0, which will cause this to return 0L every time. This uses a biased technique to get numbers from
     * large ranges, but the amount of bias is incredibly small (expected to be under 1/1000 if enough random ranged
     * numbers are requested, which is about the same as an unbiased method that was also considered). It may have
     * noticeable bias if the generator's period is exhausted by only calls to this method. Unlike all unbiased methods,
     * this advances the state by an equivalent to exactly one call to {@link #nextLong()}, where rejection sampling
     * would sometimes advance by one call, but other times by arbitrarily many more.
     * <br>
     * Credit for this method goes to <a href="https://oroboro.com/large-random-in-range/">Rafael Baptista's blog</a>,
     * with some adaptation for signed long values and a 64-bit generator. This method is drastically faster than the
     * previous implementation when the bound varies often (roughly 4x faster, possibly more). It also always gets at
     * most one random number, so it advances the state as much as {@link #nextInt(int)} or {@link #nextLong()}.
     * @param bound the outer exclusive bound; can be positive or negative
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    long nextSignedLong(long bound);

    /**
     * Returns a random non-negative integer between 0 (inclusive) and the given bound (exclusive),
     * or 0 if the bound is 0. The bound can be negative, which will produce 0 or a negative result.
     * <br>
     * Credit goes to Daniel Lemire, http://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/
     *
     * @param bound the outer bound (exclusive), can be negative or positive
     * @return the found number
     */
    int nextSignedInt(final int bound);
    /**
     * Returns a value between min (inclusive) and max (exclusive) as ints.
     * <br>
     * The inclusive and exclusive behavior is to match the behavior of the similar
     * method that deals with floating point values.
     * <br>
     * If {@code min} and {@code max} happen to be the same, {@code min} is returned
     * (breaking the exclusive behavior, but it's convenient to do so).
     *
     * @param min
     *            the minimum bound on the return value (inclusive)
     * @param max
     *            the maximum bound on the return value (exclusive)
     * @return the found value
     */
    int between(int min, int max);

    /**
     * Returns a value between min (inclusive) and max (exclusive) as longs.
     * <br>
     * The inclusive and exclusive behavior is to match the behavior of the similar
     * method that deals with floating point values.
     * <br>
     * If {@code min} and {@code max} happen to be the same, {@code min} is returned
     * (breaking the exclusive behavior, but it's convenient to do so).
     *
     * @param min
     *            the minimum bound on the return value (inclusive)
     * @param max
     *            the maximum bound on the return value (exclusive)
     * @return the found value
     */
    long between(long min, long max);

    /**
     * Returns a value from a uniform distribution from min (inclusive) to max
     * (exclusive).
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    double between(double min, double max);

    /**
     * Returns a random element from the provided array and maintains object
     * type.
     *
     * @param <T>   the type of the returned object
     * @param array the array to get an element from
     * @return the randomly selected element
     */
    <T> T getRandomElement(T[] array);
    /**
     * Returns a random element from the provided list. If the list is empty
     * then null is returned.
     *
     * @param <T>  the type of the returned object
     * @param list the list to get an element from
     * @return the randomly selected element
     */
    <T> T getRandomElement(List<T> list);

    /**
     * Returns a random element from the provided Collection, which should have predictable iteration order if you want
     * predictable behavior for identical RNG seeds, though it will get a random element just fine for any Collection
     * (just not predictably in all cases). If you give this a Set, it should be a LinkedHashSet or some form of sorted
     * Set like TreeSet if you want predictable results. Any List or Queue should be fine. Map does not implement
     * Collection, thank you very much Java library designers, so you can't actually pass a Map to this, though you can
     * pass the keys or values. If coll is empty, returns null.
     * <br>
     * Requires iterating through a random amount of coll's elements, so performance depends on the size of coll but is
     * likely to be decent, as long as iteration isn't unusually slow. This replaces {@code getRandomElement(Queue)},
     * since Queue implements Collection and the older Queue-using implementation was probably less efficient.
     * <br>
     * You should generally prefer {@link #getRandomElement(List)} whenever possible, or in some cases you can use
     * methods that get a random value on the Collection (or Map, in the case of OrderedMap) itself.
     * @param <T>  the type of the returned object
     * @param coll the Collection to get an element from; remember, Map does not implement Collection
     * @return the randomly selected element
     */
    <T> T getRandomElement(Collection<T> coll);
    /**
     * Shuffle an array using the Fisher-Yates algorithm and returns a shuffled copy, freshly-allocated, without
     * modifying elements.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @return a shuffled copy of elements
     */
    <T> T[] shuffle(final T[] elements);
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
    <T> T[] shuffleInPlace(T[] elements);
    /**
     * Shuffle an array using the Fisher-Yates algorithm. DO NOT give the same array for both elements and
     * dest, since the prior contents of dest are rearranged before elements is used, and if they refer to the same
     * array, then you can end up with bizarre bugs where one previously-unique item shows up dozens of times. If
     * possible, create a new array with the same length as elements and pass it in as dest; the returned value can be
     * assigned to whatever you want and will have the same items as the newly-formed array.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @param dest     Where to put the shuffle. If it does not have the same length as {@code elements}, this will use the
     *                 randomPortion method of this class to fill the smaller dest. MUST NOT be the same array as elements!
     * @return {@code dest} after modifications
     */
    <T> T[] shuffle(T[] elements, T[] dest);
    /**
     * Shuffles a {@link Collection} of T using the Fisher-Yates algorithm and returns an ArrayList of T.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     * @param elements a Collection of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @return a shuffled ArrayList containing the whole of elements in pseudo-random order.
     */
    <T> ArrayList<T> shuffle(Collection<T> elements);

    /**
     * Shuffles a {@link Collection} of T using the Fisher-Yates algorithm and puts it in a buffer.
     * The result is allocated if {@code buf} is null or if {@code buf} isn't empty,
     * otherwise {@code elements} is poured into {@code buf}.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     * @param elements a Collection of T; will not be modified
     * @param buf a buffer as an ArrayList that will be filled with the shuffled contents of elements;
     *            if null or non-empty, a new ArrayList will be allocated and returned
     * @param <T>      can be any non-primitive type.
     * @return a shuffled ArrayList containing the whole of elements in pseudo-random order, which may be {@code buf}
     */
    <T> ArrayList<T> shuffle(Collection<T> elements, /*@Nullable*/ ArrayList<T> buf);
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
    <T> List<T> shuffleInPlace(List<T> elements);
    /**
     * Generates a random permutation of the range from 0 (inclusive) to length (exclusive).
     * Useful for passing to OrderedMap or OrderedSet's reorder() methods.
     *
     * @param length the size of the ordering to produce
     * @return a random ordering containing all ints from 0 to length (exclusive)
     */
    int[] randomOrdering(int length);
    
    /**
     * Generates a random permutation of the range from 0 (inclusive) to length (exclusive) and stores it in
     * the dest parameter, avoiding allocations.
     * Useful for passing to OrderedMap or OrderedSet's reorder() methods.
     *
     * @param length the size of the ordering to produce
     * @param dest   the destination array; will be modified
     * @return dest, filled with a random ordering containing all ints from 0 to length (exclusive)
     */
    int[] randomOrdering(int length, int[] dest);
    /**
     * Gets a random portion of data (an array), assigns that portion to output (an array) so that it fills as much as
     * it can, and then returns output. Will only use a given position in the given data at most once.
     * 
     * @param data   an array of T; will not be modified.
     * @param output an array of T that will be overwritten; should always be instantiated with the portion length
     * @param <T>    can be any non-primitive type.
     * @return output, after {@code Math.min(output.length, data.length)} unique items have been put into it from data
     */
    <T> T[] randomPortion(T[] data, T[] output);
    /**
     * Creates a copy of this IRNG; it will generate the same random numbers, given the same calls in order, as this
     * IRNG at the point copy() is called. The copy will not share references with this IRNG. If this IRNG does not
     * permit copying itself, it is suggested to either throw an {@link UnsupportedOperationException} or return a new
     * IRNG of the same type but with a random seed, with the latter meant as a partial defense against cheating.
     *
     * @return a copy of this IRNG
     */
    IRNG copy();

    /**
     * Gets a view of this IRNG in a way that implements {@link Serializable}, which may simply be this IRNG if it
     * implements Serializable as well as IRNG.
     * <br>
     * For implementors: It is suggested to return an {@link RNG} initialized by calling 
     * {@link RNG#RNG(long)} with {@link #nextLong()} if you are unable to save the current state of this IRNG and the
     * caller still needs something saved. This won't preserve the current state or the choice of IRNG implementation,
     * however, so it is simply a last resort in case you don't want to throw an exception.
     * @return a {@link Serializable} view of this IRNG or a similar one; may be {@code this}
     */
    Serializable toSerializable();
}
