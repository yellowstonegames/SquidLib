package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * The following description is from the wikipedia page on xorshift
 *
 * "Xorshift random number generators form a class of pseudorandom number generators that was discovered by George
 * Marsaglia. They generate the next number in their sequence by repeatedly taking the exclusive or of a number with a
 * bit shifted version of itself. This makes them extremely fast on modern computer architectures. The xor shift
 * primitive is invertible. They are a subclass of linear feedback shift registers, but their simple implementation
 * typically makes them faster and use less space. However, the parameters have to be chosen very carefully in order to
 * achieve a long period. The xorshift generators have been described as being fast but not reliable."
 *
 * The reliability and comparative speed of this implementation has not been fully tested.
 *
 * @author http://en.wikipedia.org/wiki/Xorshift
 */
@Beta
public class XorRNG implements RandomnessSource {

    private static final long serialVersionUID = 2L;

    private static final long DOUBLE_MASK = (1L << 53) - 1;
    private static final double NORM_53 = 1. / (1L << 53);
    private static final long FLOAT_MASK = (1L << 24) - 1;
    private static final double NORM_24 = 1. / (1L << 24);
    private long state;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public XorRNG() {
        this((long) Math.floor(Math.random() * Long.MAX_VALUE));
    }

    public XorRNG(final long seed) {
        setSeed(seed);
    }

    @Override
    public int next(int bits) {
        return (int) (nextLong() & (1L << bits) - 1);
    }

    public long nextLong() {
        state ^= state >>> 11;
        state ^= state >>> 32;
        return 1181783497276652981L * (state ^= (state << 5));
    }

    public int nextInt() {
        return (int) nextLong();
    }

    public int nextInt(final int n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        return (int) ((nextLong() >>> 1) % n);
    }

    public long nextLong(final long n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        for (;;) {
            final long bits = nextLong() >>> 1;
            final long value = bits % n;
            if (bits - value + (n - 1) >= 0) {
                return value;
            }
        }
    }

    public double nextDouble() {
        return (nextLong() & DOUBLE_MASK) * NORM_53;
    }

    public float nextFloat() {
        return (float) ((nextLong() & FLOAT_MASK) * NORM_24);
    }

    public boolean nextBoolean() {
        return (nextLong() & 1) != 0;
    }

    public void nextBytes(final byte[] bytes) {
        int i = bytes.length, n = 0;
        while (i != 0) {
            n = Math.min(i, 8);
            for (long bits = nextLong(); n-- != 0; bits >>= 8) {
                bytes[ --i] = (byte) bits;
            }
        }
    }

    /**
     * Sets the seed of this generator. Passing this 0 will just set it to -1 instead.
     *
     * @param seed
     */
    public void setSeed(final long seed) {
        state = seed == 0 ? -1 : seed;
    }
}
