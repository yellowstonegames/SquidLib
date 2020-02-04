package squidpony.squidmath;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A high-quality and very fast RNG that has no apparent visual artifacts here; uses Mark Overton's CMR subcycle
 * generator type, with a multiplication on the output. This is meant to be an answer to when people ask for a
 * bare-minimum generator that's still "good enough" for games. It still passes at least 8TB of PractRand testing and
 * passes TestU01 with both the bits in normal forward order and in reversed bit order, which is remarkable for a
 * generator this small and simple. It has an unknown period that is fairly high; unless the seed used puts the
 * generator in a worse cycle (some of which  have a much lower period, like the seed 0), the period probably won't be
 * exhausted without hours (possibly days) of pure random number generation. It cannot produce all possible longs in its
 * longest cycle, and it can't produce even a fraction of all possible longs in its smallest cycle.
 * <br>
 * This implements RandomnessSource, but if you just want to copy this class with no dependencies, then the class
 * declaration can easily be changed to {@code public class BasicRandom64 extends Random implements Serializable}
 * without any other changes. Note, it does extend java.util.Random for additional ease of integration, but doesn't use
 * the slow {@code synchronized} keyword that Random's implementations do.
 * <br>
 * <a href="http://www.drdobbs.com/tools/fast-high-quality-parallel-random-number/231000484">This Dr. Dobb's article has
 * more on this type of generator</a>. A multiplication applied to the output of a CMR seems to be enough to pass
 * stringent testing, which is amazing.
 * @author Mark Overton
 * @author Tommy Ettinger
 */
public class BasicRandom64 extends Random implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 3L;

    public long state;

    /**
     * Calls {@link #seed(int)} with a random int value (obtained using {@link Math#random()}).
     */
    public BasicRandom64()
    {
        seed((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    /**
     * The recommended constructor, this guarantees the generator will have a period of at least 2 to the 20 (roughly
     * one million, but most if not all initial states will have much longer periods). All ints are permissible values
     * for the {@code seed} parameter.
     * @param seed any int; will be used to get the actual state used in the generator (which is a long internally)
     */
    public BasicRandom64(final int seed)
    {
        seed(seed);
    }

    /**
     * Like {@link #BasicRandom64(int)}, this doesn't use the seed as-is, and instead uses it to get a valid state
     * (which is a long internally). If you want to duplicate the state of another BasicRandom64, get the existing state
     * either with the field {@link #state} or with {@link #getState()} (you could store the state and load it later
     * at this stage), then make some new BasicRandom64 (such as with {@code new BasicRandom64(0);}) and call
     * {@link #setState(long)} with the previous state. You can also use {@link #copy()}.
     * @param seed any long; will be mixed around and given to {@link #seed(int)} as an int, not used as-is
     */
    public BasicRandom64(final long seed)
    {
        seed((int)(seed ^ seed >>> 11 ^ seed >>> 21 ^ seed >>> 32));
    }

    /**
     * Seeds the state using all bits of the given int {@code s}. Between 33554432 and 4294967296 seeds are possible,
     * with the actual count probably much closer to 4294967296. This treats the top 25 bits of {@code s} (moved to the
     * bottom, plus 1, to avoid a seed of 0) as the starting point and then generates the next state at most 127 times,
     * with each generated state taking less time than {@link #nextLong()}. Some of the starting states are entirely
     * possible to be within 127 steps of another starting state, so not all seeds are necessarily unique. This is not
     * guaranteed to put the generator on an optimal subcycle, but it is guaranteed that any subcycle will have a period
     * of at least 1048575.
     * @param s all bits are used, none verbatim (0 is tolerated)
     */
    public final void seed(final int s) {
        long v = (s >>> 7) + 1L; // at least 2 to the 25 sequential seeds have periods of at least 1048575.
        for (int i = (s & 0x7F); i > 0; i--) {
            v = (v << 29 | v >>> 35) * 0xAC564B05L;
        }
        state = v;
    }

    public long getState() {
        return state;
    }

    public void setState(final long seed)
    {
        if(seed == 0L) state = 1L;
        else state = seed;
    }

    public final long nextLong() {
        return (state = (state << 29 | state >>> 35) * 0xAC564B05L) * 0x818102004182A025L;
    }

    /**
     * Gets an int with at most the specified amount of bits; don't confuse this with {@link #nextInt(int)}, which gets
     * a number between 0 and its int argument, where this draws from a different (larger) range of random results. For
     * example, {@code next(2)} can return any 2-bit int,
     * which is limited to 0, 1, 2, or 3. Note that if you request 0 bits, this can give you any int (32 bits).
     * @param bits the number of bits to get, from 1 to 32
     * @return an int with at most the specified bits
     */
    public final int next(final int bits) {
        return (int)((state = (state << 29 | state >>> 35) * 0xAC564B05L) * 0x818102004182A025L) >>> (32 - bits);
    }
    public final int nextInt() {
        return (int)((state = (state << 29 | state >>> 35) * 0xAC564B05L) * 0x818102004182A025L);
    }

    /**
     * Returns a random non-negative integer between 0 (inclusive) and the given bound (exclusive),
     * or 0 if the bound is 0. The bound can be negative, which will produce 0 or a negative result.
     * <br>
     * Credit goes to Daniel Lemire, http://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/
     *
     * @param bound the outer bound (exclusive), can be negative or positive
     * @return the found number
     */
    public int nextInt(final int bound) {
        return (int) ((bound * (nextLong() & 0xFFFFFFFFL)) >> 32);
    }
    /**
     * Exclusive on bound (which must be positive), with an inner bound of 0.
     * If bound is negative or 0 this always returns 0.
     * <br>
     * Credit for this method goes to <a href="https://oroboro.com/large-random-in-range/">Rafael Baptista's blog</a>,
     * with some adaptation for signed long values and a 64-bit generator. It also always gets
     * exactly two random numbers, so it advances the state as much as {@link #nextInt(int)}.
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
        final long a = rand * bound;
        final long b = randLow * boundLow;
        return (((b >>> 32) + (rand + randLow) * (bound + boundLow) - a - b) >>> 32) + a;
    }

    /**
     * Sets the seed using a long, passing its argument to {@link #setState(long)}. That method just sets the public
     * field {@link #state} to its argument currently, but it may do more to ensure cycle length in the future.
     * @param seed the initial seed
     */
    @Override
    public void setSeed(long seed) {
        setState(seed);
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
    public <T> T[] shuffle(T[] elements) {
        final int size = elements.length;
        final T[] array = Arrays.copyOf(elements, size);
        for (int i = size; i > 1; i--) {
            swap(array, i - 1, nextInt(i));
        }
        return array;
    }

    /**
     * Shuffles an array in-place using the Fisher-Yates algorithm, affecting indices from 0 (inclusive) to length
     * (exclusive). May be useful with libGDX Array instances, which can be shuffled with
     * {@code random.shuffleInPlace(arr.items, arr.size)}. If you don't want the array modified, use
     * {@link #shuffle(Object[])}.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; <b>will</b> be modified
     * @return elements after shuffling it in-place
     */
    public <T> T[] shuffleInPlace(T[] elements, int length) {
        final int size = Math.min(elements.length, length);
        for (int i = size; i > 1; i--) {
            swap(elements, i - 1, nextInt(i));
        }
        return elements;
    }

    /**
     * Shuffles an array in-place using the Fisher-Yates algorithm.
     * If you don't want the array modified, use {@link #shuffle(Object[])}.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements an array of T; <b>will</b> be modified
     * @return elements after shuffling it in-place
     */
    public <T> T[] shuffleInPlace(T[] elements) {
        final int size = elements.length;
        for (int i = size; i > 1; i--) {
            swap(elements, i - 1, nextInt(i));
        }
        return elements;
    }
    /**
     * Shuffles a Collection of T items in-place using the Fisher-Yates algorithm.
     * This only shuffles List data structures.
     * <br>
     * <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Wikipedia has more on this algorithm</a>.
     *
     * @param elements a List of T; <b>will</b> be modified
     * @param <T>      can be any non-primitive type.
     * @return elements after shuffling it in-place
     */
    public <T> List<T> shuffleInPlace(List<T> elements) {
        final int n = elements.size();
        for (int i = n; i > 1; i--) {
            Collections.swap(elements, nextInt(i), i - 1);
        }
        return elements;
    }


    public BasicRandom64 copy() {
        BasicRandom64 sr = new BasicRandom64();
        sr.state = state;
        return sr;
    }
}
