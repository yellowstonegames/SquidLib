package squidpony.squidmath;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

/**
 * A low-quality but very fast RNG that has no apparent visual artifacts here; uses Mark Overton's CMR subcycle
 * generator type, but modified to be especially GWT-friendly. Even though it has no visual issues when rendered as
 * pixels, it still fails PractRand testing almost immediately. This is meant to be an answer to when people ask for
 * a bare-minimum generator that's still "good enough" for games. It has a period of 0xFFF43787 or 4294195079, which
 * can be exhausted in seconds if you only generate numbers in that time, but some seeds will be in a different
 * cycle with a much lower period. The likelihood of choosing one of these seeds is low, less than a fiftieth of one
 * percent, but it can happen. It cannot produce all possible ints in its longest cycle, and it can't produce even a
 * fraction of all possible ints in its smallest cycle. It implements RandomnessSource, but if you just want to copy
 * this class with no dependencies, then the class declaration can easily be changed to
 * {@code public class BasicRandom32 extends Random implements Serializable} without any other changes. Note, it does
 * extend java.util.Random for additional ease of integration, but doesn't use the slow {@code synchronized} keyword
 * that Random's implementations do.
 * <br>
 * <a href="http://www.drdobbs.com/tools/fast-high-quality-parallel-random-number/231000484">This Dr. Dobb's article has
 * more on this type of generator</a>.
 * @author Mark Overton
 * @author Tommy Ettinger
 */
public class BasicRandom32 extends Random implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 1L;
    public int state;

    public BasicRandom32()
    {
        state = 1;
    }

    public BasicRandom32(final int seed) {
        setState(seed);
    }

    public void setState(final int seed)
    {
        state = (seed ^ 0x41C64E6D) * 0x9E373 ^ (seed >>> 16);
    }

    public final long nextLong() {
        int y = state * 0xBCFD;
        y = (y << 17 | y >>> 15);
        final int x = y * 0xBCFD;
        return ((long) y << 32 ^ (state = (x << 17 | x >>> 15)));

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
        final int y = state * 0xBCFD;
        return (state = (y << 17 | y >>> 15)) >>> (32 - bits);
    }
    public final int nextInt() {
        final int y = state * 0xBCFD;
        return (state = (y << 17 | y >>> 15));
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
        return (int) ((bound * (nextInt() & 0xFFFFFFFFL)) >> 32);
    }
    /**
     * Exclusive on bound (which must be positive), with an inner bound of 0.
     * If bound is negative or 0 this always returns 0.
     * <br>
     * Credit for this method goes to <a href="https://oroboro.com/large-random-in-range/">Rafael Baptista's blog</a>,
     * with some adaptation for signed long values and a 64-bit generator. It also always gets
     * exactly two random numbers, so it advances the state as much as {@link #nextLong()}.
     * @param bound the outer exclusive bound; should be positive, otherwise this always returns 0L
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    public long nextLong(long bound) {
        final long rand = nextInt() & 0xFFFFFFFFL;
        final long randLow = nextInt() & 0xFFFFFFFFL;
        if (bound <= 0) return 0;
        final long boundLow = bound & 0xFFFFFFFFL;
        bound >>>= 32;
        final long a = rand * bound;
        final long b = randLow * boundLow;
        return (((b >>> 32) + (rand + randLow) * (bound + boundLow) - a - b) >>> 32) + a;
    }

    /**
     * Sets the seed using a long, by XORing the upper and lower halves of {@code seed} and passing that to
     * {@link #setState(int)}.
     * @param seed the initial seed
     */
    @Override
    public void setSeed(long seed) {
        setState((int)(seed ^ seed >>> 32));
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

    public BasicRandom32 copy() {
        BasicRandom32 sr = new BasicRandom32();
        sr.state = state;
        return sr;
    }
}
