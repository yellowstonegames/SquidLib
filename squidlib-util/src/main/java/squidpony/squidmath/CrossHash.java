package squidpony.squidmath;

import java.io.Serializable;

/**
 * Simple hashing functions that we can rely on staying the same cross-platform.
 * These use the Fowler/Noll/Vo Hash (FNV-1a) algorithm, which is public domain.
 * The hashes this returns are always 0 when given null to hash. Arrays with identical
 * elements of identical types will hash identically. Arrays with identical numerical
 * values but different types will hash differently. There are faster hashes out there,
 * but many of them are intended to run on very modern desktop processors (not, say,
 * Android phone processors, and they don't need to worry about uncertain performance
 * on GWT regarding 64-bit math). We probably don't need to hash arrays or Strings so
 * often that this (still very high-performance!) hash would be a bottleneck.
 * <br>
 * Note: This class was formerly called StableHash, but since that refers to a specific
 * category of hashing algorithm that this is not, and since the goal is to be cross-
 * platform, the name was changed to CrossHash.
 * Created by Tommy Ettinger on 1/16/2016.
 *
 * @author Glenn Fowler
 * @author Phong Vo
 * @author Landon Curt Noll
 * @author Tommy Ettinger
 */
public class CrossHash {
    public static int hash(boolean[] data) {
        if (data == null)
            return 0;
        int h = -2128831035, len = data.length - 1, o = 0;
        for (int i = 0; i <= len; i++) {
            o |= (data[i]) ? (1 << (i & 7)) : 0;
            if ((i & 7) == 7 || i == len) {
                h ^= o;
                h *= 16777619;
                o = 0;
            }
        }
        return h;
    }

    public static int hash(byte[] data) {
        if (data == null)
            return 0;
        int h = -2128831035, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i];
            h *= 16777619;
        }
        return h;
    }

    public static int hash(char[] data) {
        if (data == null)
            return 0;
        int h = -2128831035, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i] & 0xff;
            h *= 16777619;
            h ^= data[i] >>> 8;
            h *= 16777619;
        }
        return h;
    }

    public static int hash(short[] data) {
        if (data == null)
            return 0;
        int h = -2128831035, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i] & 0xff;
            h *= 16777619;
            h ^= data[i] >>> 8;
            h *= 16777619;
        }
        return h;
    }

    public static int hash(int[] data) {
        if (data == null)
            return 0;
        int h = -2128831035, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i] & 0xff;
            h *= 16777619;
            h ^= (data[i] >>> 8) & 0xff;
            h *= 16777619;
            h ^= (data[i] >>> 16) & 0xff;
            h *= 16777619;
            h ^= data[i] >>> 24;
            h *= 16777619;
        }
        return h;
    }

    public static int hash(long[] data) {
        if (data == null)
            return 0;
        int h = -2128831035, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= (int) (data[i] & 0xff);
            h *= 16777619;
            h ^= (int) ((data[i] >>> 8) & 0xff);
            h *= 16777619;
            h ^= (int) ((data[i] >>> 16) & 0xff);
            h *= 16777619;
            h ^= (int) ((data[i] >>> 24) & 0xff);
            h *= 16777619;
            h ^= (int) ((data[i] >>> 32) & 0xff);
            h *= 16777619;
            h ^= (int) ((data[i] >>> 40) & 0xff);
            h *= 16777619;
            h ^= (int) ((data[i] >>> 48) & 0xff);
            h *= 16777619;
            h ^= (int) (data[i] >>> 56);
            h *= 16777619;
        }
        return h;
    }

    public static int hash(float[] data) {
        if (data == null)
            return 0;
        int h = -2128831035, len = data.length, t;
        for (int i = 0; i < len; i++) {
            t = Float.floatToIntBits(data[i]);
            h ^= t & 0xff;
            h *= 16777619;
            h ^= (t >>> 8) & 0xff;
            h *= 16777619;
            h ^= (t >>> 16) & 0xff;
            h *= 16777619;
            h ^= t >>> 24;
            h *= 16777619;
        }
        return h;
    }

    public static int hash(double[] data) {
        if (data == null)
            return 0;
        int h = -2128831035, len = data.length;
        long t;
        for (int i = 0; i < len; i++) {
            t = Double.doubleToLongBits(data[i]);
            h ^= (int) (t & 0xff);
            h *= 16777619;
            h ^= (int) ((t >>> 8) & 0xff);
            h *= 16777619;
            h ^= (int) ((t >>> 16) & 0xff);
            h *= 16777619;
            h ^= (int) ((t >>> 24) & 0xff);
            h *= 16777619;
            h ^= (int) ((t >>> 32) & 0xff);
            h *= 16777619;
            h ^= (int) ((t >>> 40) & 0xff);
            h *= 16777619;
            h ^= (int) ((t >>> 48) & 0xff);
            h *= 16777619;
            h ^= (int) (t >>> 56);
            h *= 16777619;
        }
        return h;
    }

    public static int hash(String s) {
        if (s == null)
            return 0;
        return hash(s.toCharArray());
    }

    public static int hash(String[] data) {
        if (data == null)
            return 0;
        int h = -2128831035, len = data.length, t;
        for (int i = 0; i < len; i++) {
            t = hash(data[i]);
            h ^= t & 0xff;
            h *= 16777619;
            h ^= (t >>> 8) & 0xff;
            h *= 16777619;
            h ^= (t >>> 16) & 0xff;
            h *= 16777619;
            h ^= t >>> 24;
            h *= 16777619;
        }
        return h;
    }

    public static int hash(char[][] data) {
        if (data == null)
            return 0;
        int h = -2128831035, len = data.length, t;
        for (int i = 0; i < len; i++) {
            t = hash(data[i]);
            h ^= t & 0xff;
            h *= 16777619;
            h ^= (t >>> 8) & 0xff;
            h *= 16777619;
            h ^= (t >>> 16) & 0xff;
            h *= 16777619;
            h ^= t >>> 24;
            h *= 16777619;
        }
        return h;
    }

    public static int hash(String[]... data) {
        if (data == null)
            return 0;
        int h = -2128831035, len = data.length, t;
        for (int i = 0; i < len; i++) {
            t = hash(data[i]);
            h ^= t & 0xff;
            h *= 16777619;
            h ^= (t >>> 8) & 0xff;
            h *= 16777619;
            h ^= (t >>> 16) & 0xff;
            h *= 16777619;
            h ^= t >>> 24;
            h *= 16777619;
        }
        return h;
    }

    public static long hash64(boolean[] data) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length - 1, o = 0;
        for (int i = 0; i <= len; i++) {
            o |= (data[i]) ? (1 << (i & 7)) : 0;
            if ((i & 7) == 7 || i == len) {
                h ^= o;
                h *= 1099511628211L;
                o = 0;
            }
        }
        return h;
    }

    public static long hash64(byte[] data) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i];
            h *= 1099511628211L;
        }
        return h;
    }

    public static long hash64(char[] data) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i] & 0xff;
            h *= 1099511628211L;
            h ^= data[i] >>> 8;
            h *= 1099511628211L;
        }
        return h;
    }

    public static long hash64(short[] data) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i] & 0xff;
            h *= 1099511628211L;
            h ^= data[i] >>> 8;
            h *= 1099511628211L;
        }
        return h;
    }

    public static long hash64(int[] data) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i] & 0xff;
            h *= 1099511628211L;
            h ^= (data[i] >>> 8) & 0xff;
            h *= 1099511628211L;
            h ^= (data[i] >>> 16) & 0xff;
            h *= 1099511628211L;
            h ^= data[i] >>> 24;
            h *= 1099511628211L;
        }
        return h;
    }

    public static long hash64(long[] data) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= (data[i] & 0xff);
            h *= 1099511628211L;
            h ^= ((data[i] >>> 8) & 0xff);
            h *= 1099511628211L;
            h ^= ((data[i] >>> 16) & 0xff);
            h *= 1099511628211L;
            h ^= ((data[i] >>> 24) & 0xff);
            h *= 1099511628211L;
            h ^= ((data[i] >>> 32) & 0xff);
            h *= 1099511628211L;
            h ^= ((data[i] >>> 40) & 0xff);
            h *= 1099511628211L;
            h ^= ((data[i] >>> 48) & 0xff);
            h *= 1099511628211L;
            h ^= (data[i] >>> 56);
            h *= 1099511628211L;
        }
        return h;
    }

    public static long hash64(float[] data) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length, t;
        for (int i = 0; i < len; i++) {
            t = Float.floatToIntBits(data[i]);
            h ^= t & 0xff;
            h *= 1099511628211L;
            h ^= (t >>> 8) & 0xff;
            h *= 1099511628211L;
            h ^= (t >>> 16) & 0xff;
            h *= 1099511628211L;
            h ^= t >>> 24;
            h *= 1099511628211L;
        }
        return h;
    }

    public static long hash64(double[] data) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length, t;
        for (int i = 0; i < len; i++) {
            t = Double.doubleToLongBits(data[i]);
            h ^= (t & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 8) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 16) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 24) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 32) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 40) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 48) & 0xff);
            h *= 1099511628211L;
            h ^= (t >>> 56);
            h *= 1099511628211L;
        }
        return h;
    }

    public static long hash64(String s) {
        if (s == null)
            return 0;
        return hash64(s.toCharArray());
    }

    public static long hash64(String[] data) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length, t;
        for (int i = 0; i < len; i++) {
            t = hash64(data[i]);
            h ^= (t & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 8) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 16) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 24) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 32) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 40) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 48) & 0xff);
            h *= 1099511628211L;
            h ^= (t >>> 56);
            h *= 1099511628211L;
        }
        return h;
    }

    public static long hash64(char[][] data) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length, t;
        for (int i = 0; i < len; i++) {
            t = hash64(data[i]);
            h ^= (t & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 8) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 16) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 24) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 32) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 40) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 48) & 0xff);
            h *= 1099511628211L;
            h ^= (t >>> 56);
            h *= 1099511628211L;
        }
        return h;
    }

    public static long hash64(Iterable<String> data) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, t;
        for (String datum : data) {
            t = hash64(datum);
            h ^= (t & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 8) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 16) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 24) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 32) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 40) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 48) & 0xff);
            h *= 1099511628211L;
            h ^= (t >>> 56);
            h *= 1099511628211L;
        }
        return h;
    }

    public static long hash64(String[]... data) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length, t;
        for (int i = 0; i < len; i++) {
            t = hash64(data[i]);
            h ^= (t & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 8) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 16) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 24) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 32) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 40) & 0xff);
            h *= 1099511628211L;
            h ^= ((t >>> 48) & 0xff);
            h *= 1099511628211L;
            h ^= (t >>> 56);
            h *= 1099511628211L;
        }
        return h;
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return
     */
    public static long hash64(char[] data, int start, int end) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length;
        start %= len;
        end %= len;
        if (end <= start || start < 0 || end <= 0)
            return 0;
        for (int i = start; i < end; i++) {
            h ^= data[i] & 0xff;
            h *= 1099511628211L;
            h ^= data[i] >>> 8;
            h *= 1099511628211L;
        }
        return h;
    }

    public interface IHasher extends Serializable {
        int hash(Object data);
    }

    private static class BooleanHasher implements IHasher {
        BooleanHasher() {
        }

        public int hash(Object data) {
            return (data instanceof boolean[]) ? CrossHash.hash((boolean[]) data) : data.hashCode();
        }
    }

    public static final IHasher booleanHasher = new BooleanHasher();

    private static class ByteHasher implements IHasher {
        ByteHasher() {
        }

        public int hash(Object data) {
            return (data instanceof byte[]) ? CrossHash.hash((byte[]) data) : data.hashCode();
        }
    }

    public static final IHasher byteHasher = new ByteHasher();

    private static class ShortHasher implements IHasher {
        ShortHasher() {
        }

        public int hash(Object data) {
            return (data instanceof short[]) ? CrossHash.hash((short[]) data) : data.hashCode();
        }
    }

    public static final IHasher shortHasher = new ShortHasher();

    private static class CharHasher implements IHasher {
        CharHasher() {
        }

        public int hash(Object data) {
            return (data instanceof char[]) ? CrossHash.hash((char[]) data) : data.hashCode();
        }
    }

    public static final IHasher charHasher = new CharHasher();

    private static class IntHasher implements IHasher {
        IntHasher() {
        }

        public int hash(Object data) {
            return (data instanceof int[]) ? CrossHash.hash((int[]) data) : data.hashCode();
        }
    }

    public static final IHasher intHasher = new IntHasher();

    private static class LongHasher implements IHasher {
        LongHasher() {
        }

        public int hash(Object data) {
            return (data instanceof long[]) ? CrossHash.hash((long[]) data) : data.hashCode();
        }
    }

    public static final IHasher longHasher = new LongHasher();

    private static class FloatHasher implements IHasher {
        FloatHasher() {
        }

        public int hash(Object data) {
            return (data instanceof float[]) ? CrossHash.hash((float[]) data) : data.hashCode();
        }
    }

    public static final IHasher floatHasher = new FloatHasher();

    private static class DoubleHasher implements IHasher {
        DoubleHasher() {
        }

        public int hash(Object data) {
            return (data instanceof double[]) ? CrossHash.hash((double[]) data) : data.hashCode();
        }
    }

    public static final IHasher doubleHasher = new DoubleHasher();

    private static class Char2DHasher implements IHasher {
        Char2DHasher() {
        }

        public int hash(Object data) {
            return (data instanceof char[][]) ? CrossHash.hash((char[][]) data) : data.hashCode();
        }
    }

    public static final IHasher char2DHasher = new Char2DHasher();

    private static class StringHasher implements IHasher {
        StringHasher() {
        }

        public int hash(Object data) {
            return (data instanceof String) ? CrossHash.hash((String) data) : data.hashCode();
        }
    }

    public static final IHasher stringHasher = new StringHasher();

    private static class StringArrayHasher implements IHasher {
        StringArrayHasher() {
        }

        public int hash(Object data) {
            return (data instanceof String[]) ? CrossHash.hash((String[]) data) : data.hashCode();
        }
    }

    public static final IHasher stringArrayHasher = new StringArrayHasher();

    public static class DefaultHasher implements IHasher {
        public DefaultHasher() {
        }

        public int hash(Object data) {
            return data.hashCode();
        }
    }

    /**
     * Implementation of hashing functions using SipHash instead of FNV. Faster than FNV.
     * Code taken from https://github.com/nahi/siphash-java-inline with some minor tweaks.
     */
    public static class Sip implements Serializable {
        private static final long serialVersionUID = 0L;
        public long k0, k1;

        public Sip() {
            k0 = 0x2ffeeb0a48316f40L;
            k1 = 0x5b34a39f070b5837L;
        }

        public Sip(long k) {
            k0 = k;
            // murmurhash3 avalanche function
            k ^= k >> 33;
            k *= 0xff51afd7ed558ccdL;
            k ^= k >> 33;
            k *= 0xc4ceb9fe1a85ec53L;
            k1 = k ^ (k >> 33);
        }

        public Sip(long k0, long k1) {
            this.k0 = k0;
            this.k1 = k1;
        }

        /**
         * SipHash implementation with hand inlining the SIPROUND.
         * <br>
         * To know details about SipHash, see;
         * "a fast short-input PRF" https://www.131002.net/siphash/
         * <br>
         * SIPROUND is defined in siphash24.c that can be downloaded from the above
         * site.  Following license notice is subject to change based on the licensing
         * policy of siphash24.c (currently Apache 2 license).
         */

        public long hash64(boolean[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m, o;
            int last = (data.length >> 6) << 6,
                    i = 0;
            // processing 8 bytes blocks in data
            while (i < last) {
                // pack a block to long, as LE 8 bytes
                m = 0;
                for (long j = 1L; j != 0; j <<= 1) {
                    m |= (data[i++]) ? j : 0;
                }
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            // packing the last block to long, as LE 0-7 bytes + the length in the top byte
            m = 0;
            for (i = data.length - 1; i >= last; --i) {
                m <<= 1;
                m |= (data[i]) ? 1L : 0L;
            }
            m |= (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return v0 ^ v1 ^ v2 ^ v3;
        }

        public long hash64(byte[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = (data.length >> 3) << 3,
                    i = 0;
            // processing 8 bytes blocks in data
            while (i < last) {
                // pack a block to long, as LE 8 bytes
                m = data[i++] & 0xffL
                        | (data[i++] & 0xffL) << 8
                        | (data[i++] & 0xffL) << 16
                        | (data[i++] & 0xffL) << 24
                        | (data[i++] & 0xffL) << 32
                        | (data[i++] & 0xffL) << 40
                        | (data[i++] & 0xffL) << 48
                        | (data[i++] & 0xffL) << 56;
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            // packing the last block to long, as LE 0-7 bytes + the length in the top byte
            m = 0;
            for (i = data.length - 1; i >= last; --i) {
                m <<= 8;
                m |= (data[i] & 0xffL);
            }
            m |= (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return v0 ^ v1 ^ v2 ^ v3;
        }

        public long hash64(short[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = (data.length >> 2) << 2,
                    i = 0;
            // processing 4 shorts blocks in data
            while (i < last) {
                // pack a block to long, as LE 4 shorts
                m = data[i++] & 0xffffL
                        //| (data[i++] & 0xffL) << 8
                        | (data[i++] & 0xffffL) << 16
                        //| (data[i++] & 0xffL) << 24
                        | (data[i++] & 0xffffL) << 32
                        //| (data[i++] & 0xffL) << 40
                        | (data[i++] & 0xffffL) << 48;
                //| (data[i++] & 0xffL) << 56;
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            // packing the last block to long, as LE 0-7 bytes + the length in the top byte
            m = 0;
            for (i = data.length - 1; i >= last; --i) {
                m <<= 16;
                m |= (data[i] & 0xffffL);
            }
            m |= (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return v0 ^ v1 ^ v2 ^ v3;
        }

        public long hash64(char[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = (data.length >> 2) << 2,
                    i = 0;
            // processing 4 "shorts" blocks in data
            while (i < last) {
                // pack a block to long, as LE 4 "shorts"
                m = data[i++] & 0xffffL
                        //| (data[i++] & 0xffL) << 8
                        | (data[i++] & 0xffffL) << 16
                        //| (data[i++] & 0xffL) << 24
                        | (data[i++] & 0xffffL) << 32
                        //| (data[i++] & 0xffL) << 40
                        | (data[i++] & 0xffffL) << 48;
                //| (data[i++] & 0xffL) << 56;
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            // packing the last block to long, as LE 0-7 bytes + the length in the top byte
            m = 0;
            for (i = data.length - 1; i >= last; --i) {
                m <<= 16;
                m |= (data[i] & 0xffffL);
            }
            m |= (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return v0 ^ v1 ^ v2 ^ v3;
        }

        public long hash64(int[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = (data.length >> 1) << 1,
                    i = 0;
            // processing 4 ints blocks in data
            while (i < last) {
                // pack a block to long, as LE 4 ints
                m = data[i++] & 0xffffffffL
                        //| (data[i++] & 0xffL) << 8
                        //| (data[i++] & 0xffffL) << 16
                        //| (data[i++] & 0xffL) << 24
                        | (data[i++] & 0xffffffffL) << 32;
                //| (data[i++] & 0xffL) << 40
                //| (data[i++] & 0xffffL) << 48
                //| (data[i++] & 0xffL) << 56;
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            // packing the last block to long, as LE 0-7 bytes + the length in the top byte
            m = 0;
            for (i = data.length - 1; i >= last; --i) {
                m <<= 32;
                m |= (data[i] & 0xffffffffL);
            }
            m |= (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return v0 ^ v1 ^ v2 ^ v3;
        }

        public long hash64(long[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = data.length,
                    i = 0;
            // processing long as block in data
            while (i < last) {
                // pack a long as block
                m = data[i++];
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            m = (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return v0 ^ v1 ^ v2 ^ v3;
        }

        public long hash64(float[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = (data.length >> 1) << 1,
                    i = 0;
            // processing 4 "ints" blocks in data
            while (i < last) {
                // pack a block to long, as LE 4 "ints"
                m = Float.floatToIntBits(data[i++]) & 0xffffffffL
                        //| (data[i++] & 0xffL) << 8
                        //| (data[i++] & 0xffffL) << 16
                        //| (data[i++] & 0xffL) << 24
                        | (Float.floatToIntBits(data[i++]) & 0xffffffffL) << 32;
                //| (data[i++] & 0xffL) << 40
                //| (data[i++] & 0xffffL) << 48
                //| (data[i++] & 0xffL) << 56;
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            // packing the last block to long, as LE 0-7 bytes + the length in the top byte
            m = 0;
            for (i = data.length - 1; i >= last; --i) {
                m <<= 32;
                m |= (Float.floatToIntBits(data[i++]) & 0xffffffffL);
            }
            m |= (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return v0 ^ v1 ^ v2 ^ v3;
        }

        public long hash64(double[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = data.length,
                    i = 0;
            // processing long as block in data
            while (i < last) {
                // pack a long as block
                m = Double.doubleToLongBits(data[i++]);
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            m = (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return v0 ^ v1 ^ v2 ^ v3;
        }

        public long hash64(String data) {
            if (data == null)
                return 0;
            return hash64(data.toCharArray());
        }

        public long hash64(char[] data, int start, int end) {
            if (data == null || start >= end || end > data.length)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = ((end - start) >> 2) << 2,
                    i = start;
            // processing 4 "shorts" blocks in data
            while (i < last) {
                // pack a block to long, as LE 4 "shorts"
                m = data[i++] & 0xffffL
                        //| (data[i++] & 0xffL) << 8
                        | (data[i++] & 0xffffL) << 16
                        //| (data[i++] & 0xffL) << 24
                        | (data[i++] & 0xffffL) << 32
                        //| (data[i++] & 0xffL) << 40
                        | (data[i++] & 0xffffL) << 48;
                //| (data[i++] & 0xffL) << 56;
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            // packing the last block to long, as LE 0-7 bytes + the length in the top byte
            m = 0;
            for (i = end - 1; i >= last; --i) {
                m <<= 16;
                m |= (data[i] & 0xffffL);
            }
            m |= (long) (end - start) << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return v0 ^ v1 ^ v2 ^ v3;
        }

        public int hash(boolean[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m, o;
            int last = (data.length >> 6) << 6,
                    i = 0;
            // processing 8 bytes blocks in data
            while (i < last) {
                // pack a block to long, as LE 8 bytes
                m = 0;
                for (long j = 1L; j != 0; j <<= 1) {
                    m |= (data[i++]) ? j : 0;
                }
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            // packing the last block to long, as LE 0-7 bytes + the length in the top byte
            m = 0;
            for (i = data.length - 1; i >= last; --i) {
                m <<= 1;
                m |= (data[i]) ? 1L : 0L;
            }
            m |= (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return (int) (v0 ^ v1 ^ v2 ^ v3);
        }

        public int hash(byte[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = (data.length >> 3) << 3,
                    i = 0;
            // processing 8 bytes blocks in data
            while (i < last) {
                // pack a block to long, as LE 8 bytes
                m = data[i++] & 0xffL
                        | (data[i++] & 0xffL) << 8
                        | (data[i++] & 0xffL) << 16
                        | (data[i++] & 0xffL) << 24
                        | (data[i++] & 0xffL) << 32
                        | (data[i++] & 0xffL) << 40
                        | (data[i++] & 0xffL) << 48
                        | (data[i++] & 0xffL) << 56;
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            // packing the last block to long, as LE 0-7 bytes + the length in the top byte
            m = 0;
            for (i = data.length - 1; i >= last; --i) {
                m <<= 8;
                m |= (data[i] & 0xffL);
            }
            m |= (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return (int) (v0 ^ v1 ^ v2 ^ v3);
        }

        public int hash(short[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = (data.length >> 2) << 2,
                    i = 0;
            // processing 4 shorts blocks in data
            while (i < last) {
                // pack a block to long, as LE 4 shorts
                m = data[i++] & 0xffffL
                        //| (data[i++] & 0xffL) << 8
                        | (data[i++] & 0xffffL) << 16
                        //| (data[i++] & 0xffL) << 24
                        | (data[i++] & 0xffffL) << 32
                        //| (data[i++] & 0xffL) << 40
                        | (data[i++] & 0xffffL) << 48;
                //| (data[i++] & 0xffL) << 56;
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            // packing the last block to long, as LE 0-7 bytes + the length in the top byte
            m = 0;
            for (i = data.length - 1; i >= last; --i) {
                m <<= 16;
                m |= (data[i] & 0xffffL);
            }
            m |= (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return (int) (v0 ^ v1 ^ v2 ^ v3);
        }

        public int hash(char[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = (data.length >> 2) << 2,
                    i = 0;
            // processing 4 "shorts" blocks in data
            while (i < last) {
                // pack a block to long, as LE 4 "shorts"
                m = data[i++] & 0xffffL
                        //| (data[i++] & 0xffL) << 8
                        | (data[i++] & 0xffffL) << 16
                        //| (data[i++] & 0xffL) << 24
                        | (data[i++] & 0xffffL) << 32
                        //| (data[i++] & 0xffL) << 40
                        | (data[i++] & 0xffffL) << 48;
                //| (data[i++] & 0xffL) << 56;
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            // packing the last block to long, as LE 0-7 bytes + the length in the top byte
            m = 0;
            for (i = data.length - 1; i >= last; --i) {
                m <<= 16;
                m |= (data[i] & 0xffffL);
            }
            m |= (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return (int) (v0 ^ v1 ^ v2 ^ v3);
        }

        public int hash(int[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = (data.length >> 1) << 1,
                    i = 0;
            // processing 4 ints blocks in data
            while (i < last) {
                // pack a block to long, as LE 4 ints
                m = data[i++] & 0xffffffffL
                        //| (data[i++] & 0xffL) << 8
                        //| (data[i++] & 0xffffL) << 16
                        //| (data[i++] & 0xffL) << 24
                        | (data[i++] & 0xffffffffL) << 32;
                //| (data[i++] & 0xffL) << 40
                //| (data[i++] & 0xffffL) << 48
                //| (data[i++] & 0xffL) << 56;
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            // packing the last block to long, as LE 0-7 bytes + the length in the top byte
            m = 0;
            for (i = data.length - 1; i >= last; --i) {
                m <<= 32;
                m |= (data[i] & 0xffffffffL);
            }
            m |= (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return (int) (v0 ^ v1 ^ v2 ^ v3);
        }

        public int hash(long[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = data.length,
                    i = 0;
            // processing long as block in data
            while (i < last) {
                // pack a long as block
                m = data[i++];
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            m = (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return (int) (v0 ^ v1 ^ v2 ^ v3);
        }

        public int hash(float[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = (data.length >> 1) << 1,
                    i = 0;
            // processing 4 "ints" blocks in data
            while (i < last) {
                // pack a block to long, as LE 4 "ints"
                m = Float.floatToIntBits(data[i++]) & 0xffffffffL
                        //| (data[i++] & 0xffL) << 8
                        //| (data[i++] & 0xffffL) << 16
                        //| (data[i++] & 0xffL) << 24
                        | (Float.floatToIntBits(data[i++]) & 0xffffffffL) << 32;
                //| (data[i++] & 0xffL) << 40
                //| (data[i++] & 0xffffL) << 48
                //| (data[i++] & 0xffL) << 56;
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            // packing the last block to long, as LE 0-7 bytes + the length in the top byte
            m = 0;
            for (i = data.length - 1; i >= last; --i) {
                m <<= 32;
                m |= (Float.floatToIntBits(data[i++]) & 0xffffffffL);
            }
            m |= (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return (int) (v0 ^ v1 ^ v2 ^ v3);
        }

        public int hash(double[] data) {
            if (data == null)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = data.length,
                    i = 0;
            // processing long as block in data
            while (i < last) {
                // pack a long as block
                m = Double.doubleToLongBits(data[i++]);
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            m = (long) data.length << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return (int) (v0 ^ v1 ^ v2 ^ v3);
        }

        public int hash(String data) {
            if (data == null)
                return 0;
            return hash(data.toCharArray());
        }

        public int hash(char[] data, int start, int end) {
            if (data == null || start >= end || end > data.length)
                return 0;
            long k0 = this.k0,
                    k1 = this.k1,
                    v0 = 0x736f6d6570736575L ^ k0,
                    v1 = 0x646f72616e646f6dL ^ k1,
                    v2 = 0x6c7967656e657261L ^ k0,
                    v3 = 0x7465646279746573L ^ k1,
                    m;
            int last = ((end - start) >> 2) << 2,
                    i = start;
            // processing 4 "shorts" blocks in data
            while (i < last) {
                // pack a block to long, as LE 4 "shorts"
                m = data[i++] & 0xffffL
                        //| (data[i++] & 0xffL) << 8
                        | (data[i++] & 0xffffL) << 16
                        //| (data[i++] & 0xffL) << 24
                        | (data[i++] & 0xffffL) << 32
                        //| (data[i++] & 0xffL) << 40
                        | (data[i++] & 0xffffL) << 48;
                //| (data[i++] & 0xffL) << 56;
                // MSGROUND {
                v3 ^= m;

                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                // SIPROUND {
                v0 += v1;
                v2 += v3;
                v1 = (v1 << 13) | v1 >>> 51;
                v3 = (v3 << 16) | v3 >>> 48;
                v1 ^= v0;
                v3 ^= v2;
                v0 = (v0 << 32) | v0 >>> 32;
                v2 += v1;
                v0 += v3;
                v1 = (v1 << 17) | v1 >>> 47;
                v3 = (v3 << 21) | v3 >>> 43;
                v1 ^= v2;
                v3 ^= v0;
                v2 = (v2 << 32) | v2 >>> 32;
                // }
                v0 ^= m;
                // }
            }

            // packing the last block to long, as LE 0-7 bytes + the length in the top byte
            m = 0;
            for (i = end - 1; i >= last; --i) {
                m <<= 16;
                m |= (data[i] & 0xffffL);
            }
            m |= (long) (end - start) << 56;
            // MSGROUND {
            v3 ^= m;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            v0 ^= m;
            // }

            // finishing...
            v2 ^= 0xff;
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            // SIPROUND {
            v0 += v1;
            v2 += v3;
            v1 = (v1 << 13) | v1 >>> 51;
            v3 = (v3 << 16) | v3 >>> 48;
            v1 ^= v0;
            v3 ^= v2;
            v0 = (v0 << 32) | v0 >>> 32;
            v2 += v1;
            v0 += v3;
            v1 = (v1 << 17) | v1 >>> 47;
            v3 = (v3 << 21) | v3 >>> 43;
            v1 ^= v2;
            v3 ^= v0;
            v2 = (v2 << 32) | v2 >>> 32;
            // }
            return (int) (v0 ^ v1 ^ v2 ^ v3);
        }

        /*
        public static void main(String[] args)
        {
            boolean[] bools = new boolean[129];
            Arrays.fill(bools, true);
            byte[] bytes = new byte[17];
            Arrays.fill(bytes, (byte)-1);
            bytes[16] = 1;
            short[] shorts = new short[9];
            Arrays.fill(shorts, (short)-1);
            shorts[8] = 1;
            int[] ints = new int[5];
            Arrays.fill(ints, -1);
            ints[4] = 1;
            long[] longs = new long[3];
            Arrays.fill(longs, -1L);
            ints[2] = 1;
            Sip a = new Sip(), b = new Sip(0xBEEFCAFE), c = new Sip(0xBEEFCAFE, 0xD0D0FEED);
            System.out.println("bools   : " + StringKit.hex(a.hash64(bools)) + " " +
                    StringKit.hex(b.hash64(bools)) + " " + StringKit.hex(c.hash64(bools)));
            System.out.println("shorts  : " + StringKit.hex(a.hash64(shorts)) + " " +
                    StringKit.hex(b.hash64(shorts)) + " " + StringKit.hex(c.hash64(shorts)));
            System.out.println("ints    : " + StringKit.hex(a.hash64(ints)) + " " +
                    StringKit.hex(b.hash64(ints)) + " " + StringKit.hex(c.hash64(ints)));
            System.out.println("longs   : " + StringKit.hex(a.hash64(longs)) + " " +
                    StringKit.hex(b.hash64(longs)) + " " + StringKit.hex(c.hash64(longs)));
        }
        */
    }
/*
    public static class Lightning implements Serializable {
        private static final long serialVersionUID = 0L;
        public static long hash64(boolean[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( (data[i] ? 127 : 0) + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return result;
        }
        public static long hash64(byte[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( data[i] + 0x9E3779B97F4A7C15L);
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return result;
        }
        public static long hash64(short[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( data[i] + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return result;
        }
        public static long hash64(char[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( data[i] + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return result;
        }
        public static long hash64(int[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( data[i] + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return result;
        }
        public static long hash64(long[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( data[i] + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return result;
        }
        public static long hash64(float[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return result;
        }
        public static long hash64(double[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return result;
        }

        public static long hash64(char[] data, int start, int end)
        {
            if (data == null || start >= end)
                return 0;

            long z, result = 1;
            for (int i = start; i < end && i < data.length; i++) {
                z = ( data[i] + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return result;
        }
        public static long hash64(String data)
        {
            return hash64(data.toCharArray());
        }

        public static long hash64(char[][] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( hash64(data[i]) + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return result;
        }
        public static long hash64(String[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( hash64(data[i]) + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return result;
        }
        public static long hash64(String[]... data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( hash64(data[i]) + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return result;
        }

        public static int hash(boolean[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( (data[i] ? 127 : 0) + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return (int)(result ^ (result >>> 32));
        }
        public static int hash(byte[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( data[i] + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return (int)(result ^ (result >>> 32));
        }
        public static int hash(short[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( data[i] + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return (int)(result ^ (result >>> 32));
        }
        public static int hash(char[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( data[i] + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return (int)(result ^ (result >>> 32));
        }
        public static int hash(int[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( data[i] + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return (int)(result ^ (result >>> 32));
        }
        public static int hash(long[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( data[i] + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return (int)(result ^ (result >>> 32));
        }
        public static int hash(float[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return (int)(result ^ (result >>> 32));
        }
        public static int hash(double[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return (int)(result ^ (result >>> 32));
        }

        public static int hash(char[] data, int start, int end)
        {
            if (data == null || start >= end)
                return 0;

            long z, result = 1;
            for (int i = start; i < end && i < data.length; i++) {
                z = ( data[i] + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return (int)(result ^ (result >>> 32));
        }
        public static int hash(String data)
        {
            return hash(data.toCharArray());
        }

        public static int hash(char[][] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( hash64(data[i]) + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return (int)(result ^ (result >>> 32));
        }
        public static int hash(String[] data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( hash64(data[i]) + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return (int)(result ^ (result >>> 32));
        }
        public static int hash(String[]... data)
        {
            if(data == null)
                return 0;
            long z, result = 1;
            for (int i = 0; i < data.length; i++) {
                z = ( hash64(data[i]) + 0x9E3779B97F4A7C15L );
                z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
                z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
                result ^= z ^ (z >>> 31);
            }
            return (int)(result ^ (result >>> 32));
        }
    }
    */

    /**
     * A quick, simple hashing function that seems to have good results. Like LightRNG, it stores a state that
     * it updates independently of the output. At each step, it multiplies the current item in the array being
     * hashed by a large prime, adds a large non-prime used in LightRNG's generation function, adds the result
     * to the state and stores it, then computes the gray code of the state and XORs the current result with
     * that value. This all can be done very quickly; a million hashes of a million different 16-element long
     * arrays can be computed in under 21 ms (in the benchmark, some amount of that is overhead from
     * generating a new array with LongPeriodRNG, since the benchmark uses that RNG's state for data, and the
     * default Arrays.hashCode implementation is only somewhat faster at under 16 ms).
     */
    public static class Lightning implements Serializable {
        private static final long serialVersionUID = 0L;

        public static long hash64(boolean[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ? 127L : 0L) * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return result;
        }

        public static long hash64(byte[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += data[i] * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return result;
        }

        public static long hash64(short[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += data[i] * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return result;
        }

        public static long hash64(char[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += data[i] * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return result;
        }

        public static long hash64(int[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += data[i] * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return result;
        }

        public static long hash64(long[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += data[i] * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return result;
        }

        public static long hash64(float[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += Float.floatToIntBits(data[i]) * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return result;
        }

        public static long hash64(double[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += Double.doubleToLongBits(data[i]) * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return result;
        }

        public static long hash64(char[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;
            long z = 1, result = 1;
            for (int i = start; i < end && i < data.length; i++) {
                result ^= (z += data[i] * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return result;
        }

        public static long hash64(String data) {
            return hash64(data.toCharArray());
        }

        public static long hash64(char[][] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += hash64(data[i]) * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return result;
        }

        public static long hash64(String[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += hash64(data[i]) * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return result;
        }

        public static long hash64(String[]... data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += hash64(data[i]) * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return result;
        }

        public static int hash(boolean[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ? 127L : 0L) * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return (int) (result ^ (result >>> 32));
        }

        public static int hash(byte[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += data[i] * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return (int) (result ^ (result >>> 32));
        }

        public static int hash(short[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += data[i] * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return (int) (result ^ (result >>> 32));
        }

        public static int hash(char[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += data[i] * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return (int) (result ^ (result >>> 32));
        }

        public static int hash(int[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += data[i] * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);

            }
            return (int) (result ^ (result >>> 32));
        }

        public static int hash(long[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += data[i] * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return (int) (result ^ (result >>> 32));
        }

        public static int hash(float[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += Float.floatToIntBits(data[i]) * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return (int) (result ^ (result >>> 32));
        }

        public static int hash(double[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += Double.doubleToLongBits(data[i]) * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return (int) (result ^ (result >>> 32));
        }

        public static int hash(char[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;

            long z = 1, result = 1;
            for (int i = start; i < end && i < data.length; i++) {
                result ^= (z += data[i] * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return (int) (result ^ (result >>> 32));
        }

        public static int hash(String data) {
            return hash(data.toCharArray());
        }

        public static int hash(char[][] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += hash64(data[i]) * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return (int) (result ^ (result >>> 32));
        }

        public static int hash(String[] data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += hash64(data[i]) * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return (int) (result ^ (result >>> 32));
        }

        public static int hash(String[]... data) {
            if (data == null)
                return 0;
            long z = 1, result = 1;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += hash64(data[i]) * 0x632BE59BD9B4E019L + 0x9E3779B97F4A7C15L) ^ (z >>> 1);
            }
            return (int) (result ^ (result >>> 32));
        }
    }

}

