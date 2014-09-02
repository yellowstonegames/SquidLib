package squidpony.squidmath;

import java.awt.Point;
import java.security.SecureRandom;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import squidpony.squidgrid.util.RadiusStrategy;

/**
 * Customized extension of Random to allow for common roguelike operations.
 *
 * Uses the Mersenne Twister algorithm to provide superior results. Because of the seed requirements for the MT, the
 * seed setting methods and constructors that take a long do not set the seed. The methods that use a byte[] to set the
 * seed must be used instead if a custom seed is desired.
 *
 * @author Daniel Dyer (Java Port)
 * @author Makoto Matsumoto and Takuji Nishimura (original C version)
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Lewis Potter
 */
public class RNG extends Random {

    // The actual seed size isn't that important, but it should be a multiple of 4.
    private static final int SEED_SIZE_BYTES = 16;
    // Magic numbers from original C version.
    private static final int N = 624;
    private static final int M = 397;
    private static final int[] MAG01 = {0, 0x9908b0df};
    private static final int UPPER_MASK = 0x80000000;
    private static final int LOWER_MASK = 0x7fffffff;
    private static final int BOOTSTRAP_SEED = 19650218;
    private static final int BOOTSTRAP_FACTOR = 1812433253;
    private static final int SEED_FACTOR1 = 1664525;
    private static final int SEED_FACTOR2 = 1566083941;
    private static final int GENERATE_MASK1 = 0x9d2c5680;
    private static final int GENERATE_MASK2 = 0xefc60000;
    private final byte[] seed;
    // Lock to prevent concurrent modification of the RNG's internal state.
    private final ReentrantLock lock = new ReentrantLock();
    private final int[] mt = new int[N]; // State vector.
    private int mtIndex = 0; // Index into state vector.    
    private static final int BITWISE_BYTE_TO_INT = 0x000000FF;

    /**
     * Creates a new RNG and seeds it using the default seeding strategy.
     */
    public RNG() {
        this((new SecureRandom()).generateSeed(SEED_SIZE_BYTES));
    }

    /**
     * Ignores the seed parameter. For setting the seed, please use the constructor that takes a byte[]
     *
     * @param seed
     */
    public RNG(long seed) {
        this();
    }

    /**
     * Creates an RNG and seeds it with the specified seed data.
     *
     * @param seed The seed data used to initialize the RNG.
     */
    public RNG(byte[] seed) {
        if (seed == null || seed.length != SEED_SIZE_BYTES) {
            throw new IllegalArgumentException("Mersenne Twister RNG requires a 128-bit (16-byte) seed.");
        }
        this.seed = seed.clone();

        int[] seedInts = convertBytesToInts(this.seed);

        // This section is translated from the init_genrand code in the C version.
        mt[0] = BOOTSTRAP_SEED;
        for (mtIndex = 1; mtIndex < N; mtIndex++) {
            mt[mtIndex] = (BOOTSTRAP_FACTOR
                    * (mt[mtIndex - 1] ^ (mt[mtIndex - 1] >>> 30))
                    + mtIndex);
        }

        // This section is translated from the init_by_array code in the C version.
        int i = 1;
        int j = 0;
        for (int k = Math.max(N, seedInts.length); k > 0; k--) {
            mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >>> 30)) * SEED_FACTOR1)) + seedInts[j] + j;
            i++;
            j++;
            if (i >= N) {
                mt[0] = mt[N - 1];
                i = 1;
            }
            if (j >= seedInts.length) {
                j = 0;
            }
        }
        for (int k = N - 1; k > 0; k--) {
            mt[i] = (mt[i] ^ ((mt[i - 1] ^ (mt[i - 1] >>> 30)) * SEED_FACTOR2)) - i;
            i++;
            if (i >= N) {
                mt[0] = mt[N - 1];
                i = 1;
            }
        }
        mt[0] = UPPER_MASK; // Most significant bit is 1 - guarantees non-zero initial array.
    }

    /**
     * Take four bytes from the specified position in the specified block and convert them into a 32-bit int, using the
     * big-endian convention.
     *
     * @param bytes The data to read from.
     * @param offset The position to start reading the 4-byte int from.
     * @return The 32-bit integer represented by the four bytes.
     */
    public static int convertBytesToInt(byte[] bytes, int offset) {
        return (BITWISE_BYTE_TO_INT & bytes[offset + 3])
                | ((BITWISE_BYTE_TO_INT & bytes[offset + 2]) << 8)
                | ((BITWISE_BYTE_TO_INT & bytes[offset + 1]) << 16)
                | ((BITWISE_BYTE_TO_INT & bytes[offset]) << 24);
    }

    /**
     * Convert an array of bytes into an array of ints. 4 bytes from the input data map to a single int in the output
     * data.
     *
     * @param bytes The data to read from.
     * @return An array of 32-bit integers constructed from the data.
     * @since 1.1
     */
    public static int[] convertBytesToInts(byte[] bytes) {
        if (bytes.length % 4 != 0) {
            throw new IllegalArgumentException("Number of input bytes must be a multiple of 4.");
        }
        int[] ints = new int[bytes.length / 4];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = convertBytesToInt(bytes, i * 4);
        }
        return ints;
    }

    public byte[] getSeed() {
        return seed.clone();
    }

    @Override
    public void setSeed(long seed) {
        //ignored
    }

    @Override
    protected final int next(int bits) {
        int y;
        try {
            lock.lock();
            if (mtIndex >= N) // Generate N ints at a time.
            {
                int kk;
                for (kk = 0; kk < N - M; kk++) {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                    mt[kk] = mt[kk + M] ^ (y >>> 1) ^ MAG01[y & 0x1];
                }
                for (; kk < N - 1; kk++) {
                    y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
                    mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ MAG01[y & 0x1];
                }
                y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
                mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ MAG01[y & 0x1];

                mtIndex = 0;
            }

            y = mt[mtIndex++];
        } finally {
            lock.unlock();
        }
        // Tempering
        y ^= (y >>> 11);
        y ^= (y << 7) & GENERATE_MASK1;
        y ^= (y << 15) & GENERATE_MASK2;
        y ^= (y >>> 18);

        return y >>> (32 - bits);
    }

    /**
     * Returns a value from a even distribution from min (inclusive) to max (exclusive).
     *
     * @param min
     * @param max
     * @return
     */
    public double between(double min, double max) {
        return min + (max - min) * nextDouble();
    }

    /**
     * Returns a value between min (inclusive) and max (exclusive).
     *
     * The inclusive and exclusive behavior is to match the behavior of the similar method that deals with floating
     * point values.
     *
     * @param min
     * @param max
     * @return
     */
    public int between(int min, int max) {
        return nextInt(max - min) + min;
    }

    /**
     * Returns the average of a number of randomly selected numbers from the provided range, with min being inclusive
     * and max being exclusive. It will sample the number of times passed in as the third parameter.
     *
     * The inclusive and exclusive behavior is to match the behavior of the similar method that deals with floating
     * point values.
     *
     * This can be used to weight RNG calls to the average between min and max.
     *
     * @param min
     * @param max
     * @param samples
     * @return
     */
    public int betweenWeighted(int min, int max, int samples) {
        int sum = 0;
        for (int i = 0; i < samples; i++) {
            sum += between(min, max);
        }

        int answer = Math.round((float) sum / samples);
        return answer;
    }

    public <T> T getRandomElement(T[] array) {
        if (array.length < 1) {
            return null;
        }
        return array[nextInt(array.length)];
    }

    /**
     * Returns a random element from the provided list. If the list is empty then null is returned.
     *
     * @param <T>
     * @param list
     * @return
     */
    public <T> T getRandomElement(List<T> list) {
        if (list.size() <= 0) {
            return null;
        }
        return list.get(nextInt(list.size()));
    }

    /**
     * Returns a random elements from the provided queue. If the queue is empty then null is returned.
     *
     * @param <T>
     * @param list
     * @return
     */
    public <T> T getRandomElement(Queue<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return (T) list.toArray()[nextInt(list.size())];
    }
}
