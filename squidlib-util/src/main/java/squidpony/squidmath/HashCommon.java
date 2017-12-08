package squidpony.squidmath;

import java.util.Objects;

/**
 * Code used internally for hashing OrderedMap, OrderedSet, IntDoubleOrderedMap, Arrangement, and so on.
 * Has some methods and constants that may be useful in other kinds of code.
 * Created by Tommy Ettinger on 7/28/2017.
 */
public class HashCommon {
    public static class EnumHasher implements CrossHash.IHasher
    {
        @Override
        public int hash(Object data) {
            return (data instanceof Enum) ? ((Enum)data).ordinal() : -1;
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return Objects.equals(left, right);
        }
    }
    public static final EnumHasher enumHasher = new EnumHasher();

    private HashCommon() {
    }

    /**
     * 2<sup>32</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2.
     */
    public static final int INT_PHI = 0x9E3779B9;
    /**
     * The reciprocal of {@link #INT_PHI} modulo 2<sup>32</sup>.
     */
    public static final int INV_INT_PHI = 0x144cbc89;
    /**
     * 2<sup>64</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2.
     */
    public static final long LONG_PHI = 0x9E3779B97F4A7C15L;
    /**
     * The reciprocal of {@link #LONG_PHI} modulo 2<sup>64</sup>.
     */
    public static final long INV_LONG_PHI = 0xf1de83e19937733dL;

    /**
     * Quickly mixes the bits of an integer.
     * <br>This method mixes the bits of the argument by multiplying by the golden ratio and
     * xorshifting the result. It is borrowed from <a href="https://github.com/OpenHFT/Koloboke">Koloboke</a>, and
     * it has slightly worse behaviour than MurmurHash3 (in open-addressing hash tables the average number of probes
     * is slightly larger), but it's much faster.
     *
     * @param x an integer.
     * @return a hash value obtained by mixing the bits of {@code x}.
     * @see #invMix(int)
     */
    static int mix(final int x) {
        final int h = x * INT_PHI;
        return h ^ (h >>> 16);
    }

    /**
     * The inverse of {@link #mix(int)}. This method is mainly useful to create unit tests.
     *
     * @param x an integer.
     * @return a value that passed through {@link #mix(int)} would give {@code x}.
     */
    static int invMix(final int x) {
        return (x ^ x >>> 16) * INV_INT_PHI;
    }

    /**
     * Quickly mixes the bits of a long integer.
     * <br>This method mixes the bits of the argument by multiplying by the golden ratio and
     * xorshifting twice the result. It is borrowed from <a href="https://github.com/OpenHFT/Koloboke">Koloboke</a>, and
     * it has slightly worse behaviour than MurmurHash3 (in open-addressing hash tables the average number of probes
     * is slightly larger), but it's much faster.
     *
     * @param x a long integer.
     * @return a hash value obtained by mixing the bits of {@code x}.
     * @see #invMix(long)
     */
    static long mix(final long x) {
        long h = x * LONG_PHI;
        h ^= h >>> 32;
        return h ^ (h >>> 16);
    }

    /**
     * The inverse of {@link #mix(long)}. This method is mainly useful to create unit tests.
     *
     * @param x a long integer.
     * @return a value that passed through {@link #mix(long)} would give {@code x}.
     */
    static long invMix(long x) {
        x ^= x >>> 32;
        x ^= x >>> 16;
        return (x ^ x >>> 32) * INV_LONG_PHI;
    }

    /**
     * Return the least power of two greater than or equal to the specified value.
     * <br>Note that this function will return 1 when the argument is 0.
     *
     * @param x an integer smaller than or equal to 2<sup>30</sup>.
     * @return the least power of two greater than or equal to the specified value.
     */
    public static int nextPowerOfTwo(int x) {
        if (x == 0) return 1;
        x--;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        return (x | x >> 16) + 1;
    }

    /**
     * Return the least power of two greater than or equal to the specified value.
     * <br>Note that this function will return 1 when the argument is 0.
     *
     * @param x a long integer smaller than or equal to 2<sup>62</sup>.
     * @return the least power of two greater than or equal to the specified value.
     */
    public static long nextPowerOfTwo(long x) {
        if (x == 0) return 1;
        x--;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return (x | x >> 32) + 1;
    }
}
