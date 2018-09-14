package squidpony.squidmath;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

/**
 * A mid-quality but very fast RNG that has no apparent visual artifacts here; uses Mark Overton's CMRES subcycle
 * generator type, with minor modifications. This is meant to be an answer to when people ask for a bare-minimum
 * generator that's still "good enough" for games. It still passes at least 512GB of PractRand testing, which is good
 * for a generator this small and simple. It has an unknown period that is fairly high; unless the seed used
 * puts the generator in a worse cycle (some of which  have a much lower period, like the seed 0), the period probably
 * won't be exhausted without hours (possibly days) of pure random number generation. It cannot produce all possible
 * longs in its longest cycle, and it can't produce even a fraction of all possible longs in its smallest cycle. It
 * implements RandomnessSource, but if you just want to copy this class with no dependencies, then the class declaration
 * can easily be changed to {@code public class BasicRandom64 extends Random implements Serializable} without any other
 * changes. Note, it does extend java.util.Random for additional ease of integration, but doesn't use the slow
 * {@code synchronized} keyword that Random's implementations do.
 * <br>
 * <a href="http://www.drdobbs.com/tools/fast-high-quality-parallel-random-number/231000484">This Dr. Dobb's article has
 * more on this type of generator</a>. The code associated with it seems to be mostly accurate, but the period on this
 * 64-bit CMRES generator seems to be understated in the code documentation; my tests seem to show that the period is
 * either much longer, or that due to the irreversible nature of the generator, it can't return to its original state.
 * @author Mark Overton
 * @author Tommy Ettinger
 */
public class BasicRandom64 extends Random implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 1L;
    public long state;

    public BasicRandom64()
    {
        state = 1L;
    }

    public BasicRandom64(final long seed) {
        setState(seed);
    }

    public void setState(final long seed)
    {
        state = (seed ^ 0x9E3779B97F4A7C15L) * 0x41C64E6B ^ (seed >>> 32);
    }

    public final long nextLong() {
        final long x = state * 0x7CDD0CFB6AD2A499L;
        return (state -= (x << 35 | x >> 29)) + x;
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
        final long x = state * 0x7CDD0CFB6AD2A499L;
        return (int)((state -= (x << 35 | x >> 29)) + x >>> (64 - bits));
    }
    public final int nextInt() {
        final long x = state * 0x7CDD0CFB6AD2A499L;
        return (int)((state -= (x << 35 | x >> 29)) + x);
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
        final long boundLow = bound & 0xFFFFFFFFL;
        final long randLow = rand & 0xFFFFFFFFL;
        final long z = (randLow * boundLow >> 32);
        bound >>= 32;
        rand >>>= 32;
        final long t = rand * boundLow + z;
        return rand * bound + (t >> 32) + ((t & 0xFFFFFFFFL) + randLow * bound >> 32) - (z >> 63);
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

    public BasicRandom64 copy() {
        BasicRandom64 sr = new BasicRandom64();
        sr.state = state;
        return sr;
    }
}
