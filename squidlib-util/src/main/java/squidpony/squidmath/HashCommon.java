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
     //* @see #invMix(int)
     */
    static int mixOriginal(final int x) {
        final int h = x * INT_PHI;
        return h ^ (h >>> 16);
    }
    /**
     * Thoroughly mixes the bits of an integer.
     * <br>
     * This method mixes the bits of the argument using a multiplication by a smaller int (19 bits) followed by XOR with
     * two different rotations of the earlier product; it should work well even on GWT, where overflow can't be relied
     * on without bitwise operations being used. The previous mix(int) method would lose precision rather than
     * overflowing on GWT, which could have serious effects on the performance of a hash table (where lost precision
     * means more collisions). This is a little slower than {@link #mix(int)}, and does almost exactly as well at
     * avoiding collisions on most input, so mix() is preferred.
     *
     * @param x an integer.
     * @return a hash value obtained by mixing the bits of {@code x}.
     */
    public static int mixOther(int x)
    {
//        x = ((x *= 0x62BD5) ^ x >>> 13) * ((x & 0xFFFF8) ^ 0xCD7B5);
//        return ((x << 21) | (x >>> 11)) ^ (((x << 7) | (x >>> 25)) * 0x62BD5);
        return (x *= 0x62BD5) ^ ((x << 17) | (x >>> 15)) ^ ((x << 9) | (x >>> 23));
    }
    /**
     * Thoroughly mixes the bits of an integer.
     * <br>
     * This method acts like {@link #mixOriginal(int)} (which is used by Koloboke and Fastutil), but doesn't multiply by
     * any too-large numbers to ease compatibility with GWT. Specifically, it multiplies {@code n} by {@code 0x9E375}
     * (found using the golden ratio times 2 to the 20, minus 2 to improve quality slightly) then xors that with itself
     * unsigned-right-shifted by 16 before returning. It tends to have less pathologically-bad cases than using an
     * unmixed integer in a hash table, but will still often have more collisions than an unmixed integer if that hash
     * table is filled with numbers that vary in their lower bits. The value of this is that when ints are given that
     * only differ in their upper bits, if you didn't mix a hash code you would have 95% or higher collision rates in
     * some cases. This acts as a safeguard for that kind of scenario.
     * <br>
     * This replaces {@link #mixOther(int)}, which is also GWT-compatible but is a little slower without offering any
     * improvement in collision rates.
     * <br>
     * This is used in {@link IntDoubleOrderedMap} and {@link IntIntOrderedMap}, at the least. The algorithm this uses
     * is also used by {@link CrossHash#defaultHasher}.
     * @param n an integer.
     * @return a hash value obtained by mixing the bits of {@code x}.
     */
    public static int mix(final int n){
        final int h = n * 0x9E375;
        return h ^ (h >>> 16);
    }

//    /**
//     * The inverse of {@link #mix(int)}. This method is mainly useful to create unit tests.
//     *
//     * @param x an integer.
//     * @return a value that passed through {@link #mix(int)} would give {@code x}.
//     */
//    static int invMix(final int x) {
//        return (x ^ x >>> 16) * INV_INT_PHI;
//    }

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
    public static long mix(final long x) {
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
     * @param x a long smaller than or equal to 2<sup>62</sup>.
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
