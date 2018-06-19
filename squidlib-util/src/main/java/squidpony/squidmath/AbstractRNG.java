package squidpony.squidmath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A helper class for implementing {@link IRNG} without so much busy-work.
 * You need to provide implementations for the abstract methods {@link #nextInt()}, {@link #next(int)},
 * {@link #nextLong()}, {@link #nextBoolean()}, {@link #nextDouble()}, {@link #nextFloat()}, {@link #copy()}, and
 * {@link #toSerializable()}, many of which may be built off of each other and some of which have sample code in their
 * documentation strings in this class.
 * <br>
 * Big thanks to smelc for the concept in his hgameshrogue library.
 * @author Tommy Ettinger
 * @author smelC
 */
public abstract class AbstractRNG implements IRNG {
    /**
     * Get up to 32 bits (inclusive) of random output; the int this produces
     * will not require more than {@code bits} bits to represent.
     *
     * @param bits an int between 1 and 32, both inclusive
     * @return a random number that fits in the specified number of bits
     */
    @Override
    public abstract int next(int bits);
    /**
     * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both inclusive).
     *
     * @return a 32-bit random int.
     */
    @Override
    public abstract int nextInt();

    /**
     * Returns a random non-negative integer below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    @Override
    public int nextInt(int bound) {
        return (int) ((bound * ((long)next(31))) >>> 31) & ~(bound >> 31);
    }

    /**
     * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive).
     *
     * @return a 64-bit random long.
     */
    @Override
    public abstract long nextLong();

    /**
     * Returns a random long below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    @Override
    /**
     * Exclusive on bound (which must be positive), with an inner bound of 0.
     * If bound is negative or 0 this always returns 0.
     * <br>
     * Credit for this method goes to <a href="https://oroboro.com/large-random-in-range/">Rafael Baptista's blog</a>,
     * with some adaptation for signed long values and a 64-bit generator. This method is drastically faster than the
     * previous implementation when the bound varies often (roughly 4x faster, possibly more). It also always gets
     * exactly one random number, so it advances the state as much as {@link #nextInt(int)}.
     * @param bound the outer exclusive bound; should be positive, otherwise this always returns 0L
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    public long nextLong(long bound) {
        long rand = nextLong();
        if (bound <= 0) return 0;
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>>= 32;
        final long z = (randLow * boundLow >>> 32);
        long t = rand * boundLow + z;
        final long tLow = t & 0xFFFFFFFFL;
        t >>>= 32;
        return rand * bound + t + (tLow + randLow * bound >> 32);
    }

    /**
     * Get a random bit of state, interpreted as true or false with approximately equal likelihood.
     * <br>
     * Note: This is abstract because some implementations may be best served by using {@link #next(int)} to get 1 bit,
     * returning {@code next(1) == 1}, but others will get much better results with a sign check by calling their choice
     * of {@link #nextInt()} or {@link #nextLong()} and returning {@code nextInt() < 0} or {@code nextLong < 0L}. For
     * example, an implementation that uses a linear congruential generator without truncating some lower bits will have
     * very-low-period results for the bottom bit (alternating true and false), but perfectly fine results from a sign
     * check. There are tested issues on the bottom (at least 2) bits of {@link XoRoRNG}, but again not on a sign check.
     * @return a random boolean.
     */
    @Override
    public abstract boolean nextBoolean();
    /**
     * Gets a random double between 0.0 inclusive and 1.0 exclusive.
     * This returns a maximum of 0.9999999999999999 because that is the largest double value that is less than 1.0 .
     * <br>
     * This is abstract because some generators may natively work with double or float values, but others may need to
     * convert a long to a double as with {@code (nextLong() & 0x1fffffffffffffL) * 0x1p-53}, which is recommended if
     * longs are fast to produce.
     * @return a double between 0.0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    @Override
    public abstract double nextDouble();
    /**
     * This returns a random double between 0.0 (inclusive) and outer (exclusive). The value for outer can be positive
     * or negative. Because of how math on doubles works, there are at most 2 to the 53 values this can return for any
     * given outer bound, and very large values for outer will not necessarily produce all numbers you might expect.
     *
     * @param outer the outer exclusive bound as a double; can be negative or positive
     * @return a double between 0.0 (inclusive) and outer (exclusive)
     */
    @Override
    public double nextDouble(double outer) {
        return nextDouble() * outer;
    }

    /**
     * Gets a random float between 0.0f inclusive and 1.0f exclusive.
     * This returns a maximum of 0.99999994 because that is the largest float value that is less than 1.0f .
     * <br>
     * This is abstract because some generators may natively work with double or float values, but others may need to
     * convert an int or long to a float as with {@code (nextInt() & 0xffffff) * 0x1p-24f},
     * {@code (nextLong() & 0xffffffL) * 0x1p-24f}, or {@code next(24) * 0x1p-24f}, any of which can work when the
     * method they call is high-quality and fast. You probably would want to use nextInt() or next() if your
     * implementation is natively 32-bit and is slower at producing longs, for example.
     * @return a float between 0f (inclusive) and 0.99999994f (inclusive)
     */
    @Override
    public abstract float nextFloat();
    /**
     * This returns a random float between 0.0f (inclusive) and outer (exclusive). The value for outer can be positive
     * or negative. Because of how math on floats works, there are at most 2 to the 24 values this can return for any
     * given outer bound, and very large values for outer will not necessarily produce all numbers you might expect.
     *
     * @param outer the outer exclusive bound as a float; can be negative or positive
     * @return a float between 0f (inclusive) and outer (exclusive)
     */
    @Override
    public float nextFloat(float outer) {
        return nextFloat() * outer;
    }

    /**
     * Returns a value between min (inclusive) and max (exclusive) as ints.
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
     * Returns a value between min (inclusive) and max (exclusive) as longs.
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
    public long between(long min, long max) {
        return nextLong(max - min) + min;
    }

    /**
     * Returns a value from a uniform distribution from min (inclusive) to max
     * (exclusive).
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    @Override
    public double between(double min, double max) {
        return nextDouble(max - min) + min;
    }

    /**
     * Returns a random element from the provided array and maintains object
     * type.
     *
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
     * then null is returned. This will perform well on Lists that allow random access,
     * but will not perform as well on {@link java.util.LinkedList} or similar classes
     * that need to iterate one-by-one in their {@link List#get(int)} method.
     *
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
     *
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

    /**
     * Mutates the array arr by switching the contents at pos1 and pos2.
     * @param arr an array of T; must not be null
     * @param pos1 an index into arr; must be at least 0 and no greater than arr.length
     * @param pos2 an index into arr; must be at least 0 and no greater than arr.length
     */
    protected static <T> void swap(T[] arr, int pos1, int pos2) {
        final T tmp = arr[pos1];
        arr[pos1] = arr[pos2];
        arr[pos2] = tmp;
    }

    /**
     * Shuffle an array using the Fisher-Yates algorithm and returns a shuffled copy, freshly-allocated, without
     * modifying elements.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; will not be modified
     * @return a shuffled copy of elements
     */
    @Override
    public <T> T[] shuffle(T[] elements) {
        final int size = elements.length;
        final T[] array = Arrays.copyOf(elements, size);
        for (int i = size; i > 1; i--) {
            swap(array, i - 1, nextInt(i));
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
     * @return elements after shuffling it in-place
     */
    @Override
    public <T> T[] shuffleInPlace(T[] elements) {
        final int size = elements.length;
        for (int i = size; i > 1; i--) {
            swap(elements, i - 1, nextInt(i));
        }
        return elements;
    }

    /**
     * Shuffle an array using the Fisher-Yates algorithm. If possible, create a new array or reuse an existing array
     * with the same length as elements and pass it in as dest; the dest array will contain the shuffled contents of
     * elements and will also be returned as-is. If dest does not have the same length as elements, this will throw an
     * IllegalArgumentException
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; will not be modified
     * @param <T>      can be any non-primitive type.
     * @param dest     Where to put the shuffle. If it does not have the same length as {@code elements}, this will
     *                 throw an IllegalArgumentException.
     * @return {@code dest} after modifications
     */
    @Override
    public <T> T[] shuffle(T[] elements, T[] dest) {
        if (dest.length != elements.length)
            throw new IllegalArgumentException("Not allowed! In AbstractRNG.shuffle(), elements and dest do not have the same length.");
        System.arraycopy(elements, 0, dest, 0, elements.length);
        shuffleInPlace(dest);
        return dest;
    }

    /**
     * Shuffles a {@link Collection} of T using the Fisher-Yates algorithm and returns an ArrayList of T.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements a Collection of T; will not be modified
     * @return a shuffled ArrayList containing the whole of elements in pseudo-random order.
     */
    @Override
    public <T> ArrayList<T> shuffle(Collection<T> elements) {
        return shuffle(elements, null);
    }

    /**
     * Shuffles a {@link Collection} of T using the Fisher-Yates algorithm and puts it in a buffer.
     * The result is allocated if {@code buf} is null or if {@code buf} isn't empty,
     * otherwise {@code elements} is poured into {@code buf}.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements a Collection of T; will not be modified
     * @param buf      a buffer as an ArrayList that will be filled with the shuffled contents of elements;
     *                 if null or non-empty, a new ArrayList will be allocated and returned
     * @return a shuffled ArrayList containing the whole of elements in pseudo-random order, which may be {@code buf}
     */
    @Override
    public <T> ArrayList<T> shuffle(Collection<T> elements, ArrayList<T> buf) {
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
            final int r = nextInt(i+1),
                    t = dest[r];
            dest[r] = dest[i];
            dest[i] = t;
        }
        return dest;
    }

    /**
     * Creates a copy of this IRNG; it will generate the same random numbers, given the same calls in order, as this
     * IRNG at the point copy() is called. The copy will not share references with this IRNG. If this IRNG does not
     * permit copying itself, it is suggested to either throw an {@link UnsupportedOperationException} or return a new
     * IRNG of the same type but with a random seed, with the latter meant as a partial defense against cheating.
     * <br>
     * This is abstract because every implementation is likely to have different specifics for this.
     * @return a copy of this IRNG
     */
    @Override
    public abstract IRNG copy();

    /**
     * Gets a view of this IRNG in a way that implements {@link Serializable}, which may simply be this IRNG if it
     * implements Serializable as well as IRNG.
     * <br>
     * For implementors: It is suggested to return an {@link RNG} initialized by calling
     * {@link RNG#RNG(long)} with {@link #nextLong()} if you are unable to save the current state of this IRNG and the
     * caller still needs something saved. This won't preserve the current state or the choice of IRNG implementation,
     * however, so it is simply a last resort in case you don't want to throw an exception.
     *
     * @return a {@link Serializable} view of this IRNG or a similar one; may be {@code this}
     */
    @Override
    public abstract Serializable toSerializable();
}
