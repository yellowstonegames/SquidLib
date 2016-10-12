package squidpony.squidmath;

import squidpony.annotation.Beta;

import java.io.Serializable;

/**
 * Simple hashing functions that we can rely on staying the same cross-platform.
 * The static methods of this class (not its inner classes) use the Fowler/Noll/Vo
 * Hash (FNV-1a) algorithm, which is public domain. The hashes this returns are always
 * 0 when given null to hash. Arrays with identical elements of identical types will
 * hash identically. Arrays with identical numerical values but different types will
 * hash differently (only for FNV-1a, not the inner classes). FNV-1a may be somewhat
 * slow if you are running many hashes or hashing very large data; in that case you
 * should consider one of the inner classes, Lightning or Storm.
 * <br>
 * There are two static inner classes in CrossHash, Lightning and Storm, that provide different
 * hashing properties, as well as the inner IHasher interface.
 * <br>
 * IHasher values are provided as static fields, and use FNV-1a to hash a specific type or fall
 * back to Object.hashCode if given an object with the wrong type. IHasher values are optional
 * parts of OrderedMap and OrderedSet, and allow arrays to be used as keys in those collections
 * while keeping hashing by value instead of the normal hashing by reference for arrays. You
 * probably won't ever need to make a class that implements IHasher yourself.
 * <br>
 * The inner classes provide alternate, faster hashing algorithms. Lightning has no theoretical
 * basis or grounds in any reason other than empirical testing for why it does what it does, and
 * this seems to be in line with many widely-used hashes (see: The Art of Hashing,
 * http://eternallyconfuzzled.com/tuts/algorithms/jsw_tut_hashing.aspx ). That said, it performs
 * very well, just slightly behind Arrays.hashCode (18-20 ms instead of 15 ms for over a million
 * hashes of 16-element long arrays, including overhead for generating them, while SipHash and
 * FNV-1a take approximately 80 ms and 135-155 ms, respectively, for the same data).
 * <br>
 * Storm is a variant on Lightning with 64 bits for a salt-like modifier as a member variable,
 * which can make 2 to the 64 individual hashing functions from one set of code. Storm has some
 * properties of a cryptographic hash, but is not recommended it for that usage. It is, however
 * ideal for situations that show up often in game development where end users may be able to see
 * and possibly alter some information that you don't want changed (i.e. save data stored on a
 * device or in the browser's LocalStorage). If you want a way to verify the data is what you
 * think it is, you can store a hash, using one of the many-possible hash functions this can
 * produce, somewhere else and verify that the saved data has the hash it did last time; if the
 * exact hashing function isn't known (or exact functions aren't known) by a tampering user,
 * then it is unlikely they can make the hash match even if they can edit it. Storm is slightly
 * slower than Lightning, at about 25-26 ms for a million hashes (still including some RNG
 * overhead) instead of Lightning's 18-20 ms, but should never be worse than twice as slow as
 * Arrays.hashCode, and is still about three times faster than the similar SipHash that SquidLib
 * previously had in this class.
 * <br>
 * All of the hashes used here have about the same rate of collisions on identical data
 * (testing used Arrays.hashCode, FNV-1a, Lightning, Storm, and the now-removed SipHash), but
 * Lightning (and Storm) seem to do better than the rest on collision rates more often than
 * they do worse than all the others. Lightning has been changed frequently but is considered
 * stable now, but it isn't being considered to replace the FNV-1a algorithm in CrossHash for
 * compatibility reasons. It seems to meet all the criteria for a good hash function, though,
 * including doing well with a visual test that shows issues in FNV-1a and especially
 * Arrays.hashCode. Storm is still not necessarily in a final version; the commit that removed
 * SipHash also changed Storm's algorithm to more heavily factor in the salt and allow that
 * salt to alter any patterns present in the output, making similar data hash very differently
 * when the salts are different.
 * <br>
 * To help find patterns in hash output in a visual way, you can hash an x,y point, take the bottom 24 bits,
 * and use that as an RGB color for the pixel at that x,y point. On a 512x512 grid of points, the patterns
 * in Arrays.hashCode and the default CrossHash algorithm (FNV-1a) are evident, and Sip (implementing SipHash)
 * does approximately as well as Lightning, with no clear patterns visible (Sip has been removed from SquidLib
 * because it needs a lot of code and is slower than Storm and especially Lightning). The idea is from a
 * technical report on visual uses for hashing, http://www.clockandflame.com/media/Goulburn06.pdf .
 * <ul>
 * <li>{@link java.util.Arrays#hashCode(int[])}: http://i.imgur.com/S4Gh1sX.png</li>
 * <li>{@link CrossHash#hash(int[])}: http://i.imgur.com/x8SDqvL.png</li>
 * <li>(Former) CrossHash.Sip.hash(int[]): http://i.imgur.com/keSpIwm.png</li>
 * <li>{@link CrossHash.Lightning#hash(int[])}: http://i.imgur.com/afGJ9cA.png</li>
 * </ul>
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
    public static int hash(final boolean[] data) {
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

    public static int hash(final byte[] data) {
        if (data == null)
            return 0;
        int h = -2128831035, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i];
            h *= 16777619;
        }
        return h;
    }

    public static int hash(final char[] data) {
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

    public static int hash(final short[] data) {
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

    public static int hash(final int[] data) {
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

    public static int hash(final long[] data) {
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

    public static int hash(final float[] data) {
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

    public static int hash(final double[] data) {
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

    public static int hash(final CharSequence data) {
        if (data == null)
            return 0;
        int h = -2128831035, len = data.length();
        for (int i = 0; i < len; i++) {
            h ^= data.charAt(i) & 0xff;
            h *= 16777619;
            h ^= data.charAt(i) >>> 8;
            h *= 16777619;
        }
        return h;
    }

    public static int hash(final CharSequence[] data) {
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

    public static int hash(final char[][] data) {
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

    public static int hash(final CharSequence[]... data) {
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

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return
     */
    public static int hash(final char[] data, int start, int end) {
        if (data == null)
            return 0;
        int h = -2128831035, len = data.length;
        start %= len;
        end %= len;
        if (end <= start || start < 0 || end <= 0)
            return 0;
        for (int i = start; i < end; i++) {
            h ^= data[i] & 0xff;
            h *= 16777619;
            h ^= data[i] >>> 8;
            h *= 16777619;
        }
        return h;
    }

    public static long hash64(final boolean[] data) {
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

    public static long hash64(final byte[] data) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length;
        for (int i = 0; i < len; i++) {
            h ^= data[i];
            h *= 1099511628211L;
        }
        return h;
    }

    public static long hash64(final char[] data) {
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

    public static long hash64(final short[] data) {
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

    public static long hash64(final int[] data) {
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

    public static long hash64(final long[] data) {
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

    public static long hash64(final float[] data) {
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

    public static long hash64(final double[] data) {
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

    public static long hash64(final CharSequence data) {
        if (data == null)
            return 0;
        long h = -3750763034362895579L, len = data.length();
        for (int i = 0; i < len; i++) {
            h ^= data.charAt(i) & 0xff;
            h *= 1099511628211L;
            h ^= data.charAt(i) >>> 8;
            h *= 1099511628211L;
        }
        return h;
    }

    public static long hash64(final CharSequence[] data) {
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

    public static long hash64(final char[][] data) {
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

    public static long hash64(final Iterable<String> data) {
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

    public static long hash64(final CharSequence[]... data) {
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
    public static long hash64(final char[] data, int start, int end) {
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
        int hash(final Object data);
    }

    private static class BooleanHasher implements IHasher {
        protected BooleanHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof boolean[]) ? CrossHash.hash((boolean[]) data) : data.hashCode();
        }
    }

    public static final IHasher booleanHasher = new BooleanHasher();

    private static class ByteHasher implements IHasher {
        protected ByteHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof byte[]) ? CrossHash.hash((byte[]) data) : data.hashCode();
        }
    }

    public static final IHasher byteHasher = new ByteHasher();

    private static class ShortHasher implements IHasher {
        protected ShortHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof short[]) ? CrossHash.hash((short[]) data) : data.hashCode();
        }
    }

    public static final IHasher shortHasher = new ShortHasher();

    private static class CharHasher implements IHasher {
        protected CharHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof char[]) ? CrossHash.hash((char[]) data) : data.hashCode();
        }
    }

    public static final IHasher charHasher = new CharHasher();

    private static class IntHasher implements IHasher {
        protected IntHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof int[]) ? CrossHash.hash((int[]) data) : data.hashCode();
        }
    }

    public static final IHasher intHasher = new IntHasher();

    private static class LongHasher implements IHasher {
        protected LongHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof long[]) ? CrossHash.hash((long[]) data) : data.hashCode();
        }
    }

    public static final IHasher longHasher = new LongHasher();

    private static class FloatHasher implements IHasher {
        protected FloatHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof float[]) ? CrossHash.hash((float[]) data) : data.hashCode();
        }
    }

    public static final IHasher floatHasher = new FloatHasher();

    private static class DoubleHasher implements IHasher {
        protected DoubleHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof double[]) ? CrossHash.hash((double[]) data) : data.hashCode();
        }
    }

    public static final IHasher doubleHasher = new DoubleHasher();

    private static class Char2DHasher implements IHasher {
        protected Char2DHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof char[][]) ? CrossHash.hash((char[][]) data) : data.hashCode();
        }
    }

    public static final IHasher char2DHasher = new Char2DHasher();

    private static class StringHasher implements IHasher {
        protected StringHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof CharSequence) ? CrossHash.hash((CharSequence) data) : data.hashCode();
        }
    }

    public static final IHasher stringHasher = new StringHasher();

    private static class StringArrayHasher implements IHasher {
        protected StringArrayHasher() {
        }

        public int hash(final Object data) {
            return (data instanceof CharSequence[]) ? CrossHash.hash((CharSequence[]) data) : data.hashCode();
        }
    }

    public static final IHasher stringArrayHasher = new StringArrayHasher();

    public static class DefaultHasher implements IHasher {
        protected DefaultHasher() {
        }

        public int hash(final Object data) {
            return data.hashCode();
        }
    }

    public static final IHasher defaultHasher = new DefaultHasher();

    /**
     * A quick, simple hashing function that seems to have good results. Like LightRNG, it stores a state that
     * it updates independently of the output, and this starts at a large prime. At each step, it takes the
     * current item in the array being hashed, adds a large non-prime used in LightRNG's generation function
     * (it's 2 to the 64, times the golden ratio phi, and truncated to a signed long), multiplies by a prime
     * called the "state multiplier", adds the result to the state and stores it, multiplies the value of the
     * state by another prime called the "output multiplier", then XORs the current result with that value
     * before moving onto the next item in the array. A finalization step XORs the result with a complex value
     * made by adding the state (left over from the previous step) to what was the output multiplier, adding
     * the last known value for result to the phi-related constant from LightRNG, multiplying that pair, adding
     * the initial state (which turns out to be unusually good for this, despite no particularly special numeric
     * qualities other than being a probable prime) and then bitwise-rotating it left by a seemingly-random
     * number drawn from the highest 6 bits of the state.
     * <br>
     * This all can be done very quickly; a million hashes of a million different 16-element long arrays can be
     * computed in under 18-20 ms (in the benchmark, some amount of that is overhead from generating a new
     * array with LongPeriodRNG, since the benchmark uses that RNG's state for data, and the default
     * Arrays.hashCode implementation is only somewhat faster at under 16 ms). After several tries and tweaks
     * to the constants this uses, it also gets remarkably few hash collisions. On the same 0x100000, or
     * 1048576, RNG states for data, Lightning gets 110 collisions, the JDK Arrays.hashCode method gets 129
     * collisions, Sip (implementing SipHash) gets 145 collisions, and CrossHash (using the FNV-1a algorithm)
     * gets 135 collisions. Storm depends on the salt chosen, but with one initialized with the phi-based
     * constant that shows up in LightRNG and here, Storm gets 116 collisions. Dispersion is not perfect, but
     * at many bit sizes Lightning continues to have less collisions (it disperses better than the other hashes
     * with several quantities of bits, at least on this test data). Lightning also does relatively well, though
     * it isn't clearly ahead of the rest all the time, when hashing Strings, especially ones that use a larger
     * set of letters, it seems (FakeLanguageGen was used to make test data, and languages that used more
     * characters in their alphabet seemed to hash better with this than competing hashes for some reason).
     * <br>
     * There is certainly room for improvement with the specific numbers chosen; earlier versions used the
     * state multiplier "Neely's number", which is a probable prime made by taking the radix-29 number
     * "HARGNALLINSCLOPIOPEEPIO" (a reference to a very unusual TV show), truncating to 64 bits, and rotating
     * right by 42 bits. This version uses "Neely's number" for an initial state and during finalization, and
     * uses a different probable prime as the state multiplier, made with a similar process; it starts with the
     * radix-36 number "EDSGERWDIJKSTRA", then does the same process but rotates right by 37 bits to obtain a
     * different prime. This tweak seems to help with hash collisions. Extensive trial and error was used to
     * find the current output multiplier, which has no real relationship to anything else but has exactly 32 of
     * 64 bits set to 1, has 1 in the least and most significant bit indices (meaning it is negative and odd),
     * and other than that seems to have better results on most inputs for mystifying reasons. Earlier versions
     * applied a Gray code step to alter the output instead of a multiplier that heavily overflows to obfuscate
     * state, but that had a predictable pattern for most of the inputs tried, which seemed less-than-ideal for
     * a hash. Vitally, Lightning avoids predictable collisions that Arrays.hashCode has, like
     * {@code Arrays.hashCode(new long[]{0})==Arrays.hashCode(new long[]{-1})}.
     * <br>
     * The output multiplier is 0xC6BC279692B5CC83L, the state multiplier is 0xD0E89D2D311E289FL, the number
     * added to the state (from LightRNG and code derived from FastUtil, but obtained from the golden ratio
     * phi) is 0x9E3779B97F4A7C15L, and the starting state ("Neely's Number") is 0x632BE59BD9B4E019L.
     * <br>
     * To help find patterns in hash output in a visual way, you can hash an x,y point, take the bottom 24 bits,
     * and use that as an RGB color for the pixel at that x,y point. On a 512x512 grid of points, the patterns
     * in Arrays.hashCode and the default CrossHash algorithm (FNV-1a) are evident, and Sip (implementing
     * SipHash) does approximately as well as Lightning, with no clear patterns visible (Sip has been removed
     * from SquidLib because it needs a lot of code and is slower than Storm and especially Lightning). The
     * idea is from a technical report on visual uses for hashing,
     * http://www.clockandflame.com/media/Goulburn06.pdf .
     * <ul>
     * <li>{@link java.util.Arrays#hashCode(int[])}: http://i.imgur.com/S4Gh1sX.png</li>
     * <li>{@link CrossHash#hash(int[])}: http://i.imgur.com/x8SDqvL.png</li>
     * <li>(Former) CrossHash.Sip.hash(int[]): http://i.imgur.com/keSpIwm.png</li>
     * <li>{@link CrossHash.Lightning#hash(int[])}: http://i.imgur.com/afGJ9cA.png</li>
     * </ul>
     */
    // tested output multipliers
    // 0x DA1A459BD9B4C619L
    // 0x DC1A459879B5C619L
    // 0x DC1A479829B5E639L
    // 0x DC1A479C29B5C639L
    // 0x EA1C479692B5C639L
    // 0x CA1C479692B5C635L // this gets 105 collisions, low
    // 0x CABC479692B5C635L
    // 0x DC1A479C29B5C647L
    // 0x DC1A479C29B5C725L
    // 0x CABC279692B5CB21L
    // 0x C6BC279692B5CC83L // this gets 100 collisions, lowest
    // 0x C6BC279692B4D8A5L
    // 0x C6BC279692B4D345L
    // 0x C6EC273692B4A4B9L
    // 0x C6A3256B52D5B463L
    // 0x C6A3256B52D5B463L
    // 0x C6A3256D52D5B4C9L
    // 0x D8A3256D52D5B619L
    // 0x D96E6AC724658947L
    // 0x D96E6AC724658C2DL
    // 0x CCABF9E32FD684F9L
    // 0x C314163FAF912A01L
    // 0x C3246007A332C12AL
    // 0x CA1C479692B5C6ABL
    // 0x C6B5275692B5CC83 // untested so far
    @SuppressWarnings("NumericOverflow")
    public static class Lightning {

        public static long hash64(final boolean[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ? 0x9E3779B97F4A7C94L : 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final byte[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final short[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final char[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final int[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final long[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final float[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final double[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final char[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = start; i < end && i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final CharSequence data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length(); i++) {
                result ^= (z += (data.charAt(i) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58));
        }

        public static long hash64(final char[][] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final CharSequence[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final Iterable<String> data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (String datum : data) {
                result ^= (z += (hash64(datum) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final CharSequence[]... data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }
        public static long hash64(final Object[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            Object o;
            for (int i = 0; i < data.length; i++) {
                o = data[i];
                result ^= (z += ((o == null ? 0 : o.hashCode()) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static int hash(final boolean[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ? 0x9E3779B97F4A7C94L : 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final byte[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final short[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final char[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final int[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;

            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final long[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final float[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final double[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final char[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;

            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = start; i < end && i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final CharSequence data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length(); i++) {
                result ^= (z += (data.charAt(i) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final char[][] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final CharSequence[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final Iterable<String> data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (String datum : data) {
                result ^= (z += (hash64(datum) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final CharSequence[]... data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final Object[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            Object o;
            for (int i = 0; i < data.length; i++) {
                o = data[i];
                result ^= (z += ((o == null ? 0 : o.hashCode()) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }
    }
    /**
     * A whole cluster of Lightning-like hash functions that sacrifice a small degree of speed, but can be
     * constructed with a salt value that helps obscure what hashing function is actually being used.
     * <br>
     * The salt field is not serialized, so it is important that the same salt will be given by the
     * program when the same hash results are wanted for some inputs.
     * <br>
     * A group of 24 static, final, pre-initialized Storm members are present in this class, each with the
     * name of a letter in the Greek alphabet (this uses the convention on Wikipedia,
     * https://en.wikipedia.org/wiki/Greek_alphabet#Letters , where lambda is spelled with a 'b'). The whole
     * group of 24 pre-initialized members are also present in a static array called {@code predefined}.
     * These can be useful when, for example, you want to get multiple hashes of a single array or String
     * as part of cuckoo hashing or similar techniques that need multiple hashes for the same inputs.
     */
    public static class Storm implements Serializable
    {
        private static final long serialVersionUID = 3152426757973945155L;

        private transient long $alt;

        public Storm()
        {
            this(0L);
        }

        public Storm(final CharSequence alteration)
        {
            this(Lightning.hash64(alteration));
        }
        public Storm(final long alteration)
        {
            long s = (alteration + 0x9E3779B97F4A7C15L);
            s = (s ^ (s >>> 30)) * 0xBF58476D1CE4E5B9L;
            s = (s ^ (s >>> 27)) * 0x94D049BB133111EBL;
            s ^= (s >>> 31);
            $alt = (s += (191 - Long.bitCount(s)));
        }

        public static final Storm alpha = new Storm("alpha"), beta = new Storm("beta"), gamma = new Storm("gamma"),
                delta = new Storm("delta"), epsilon = new Storm("epsilon"), zeta = new Storm("zeta"),
                eta = new Storm("eta"), theta = new Storm("theta"), iota = new Storm("iota"),
                kappa = new Storm("kappa"), lambda = new Storm("lambda"), mu = new Storm("mu"),
                nu = new Storm("nu"), xi = new Storm("xi"), omicron = new Storm("omicron"), pi = new Storm("pi"),
                rho = new Storm("rho"), sigma = new Storm("sigma"), tau = new Storm("tau"),
                upsilon = new Storm("upsilon"), phi = new Storm("phi"), chi = new Storm("chi"), psi = new Storm("psi"),
                omega = new Storm("omega");
        public static final Storm[] predefined = new Storm[]{alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota,
                kappa, lambda, mu, nu, xi, omicron, pi, rho, sigma, tau, upsilon, phi, chi, psi, omega};

        public long hash64(final boolean[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] ? 0x9E3779B97F4A7C94L : 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final byte[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final short[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final char[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final int[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final long[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final float[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final double[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final char[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = start; i < end && i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final CharSequence data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length();
            for (int i = 0; i < len; i++) {
                result ^= (z += (data.charAt(i) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58));
        }

        public long hash64(final char[][] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final CharSequence[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final Iterable<String> data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (String datum : data) {
                result ^= (z += (hash64(datum) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final CharSequence[]... data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }

        public long hash64(final Object[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            Object o;
            for (int i = 0; i < len; i++) {
                o = data[i];
                result ^= (z += ((o == null ? 0 : o.hashCode()) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;

            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58));
        }
        public int hash(final boolean[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] ? 0x9E3779B97F4A7C94L : 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final byte[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final short[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final char[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final int[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;

            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final long[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final float[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (Float.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final double[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (Double.doubleToLongBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final char[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;

            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = start; i < end && i < len; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final CharSequence data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length();
            for (int i = 0; i < len; i++) {
                result ^= (z += (data.charAt(i) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int)(z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final char[][] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final CharSequence[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final Iterable<String> data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L;
            for (String datum : data) {
                result ^= (z += (hash64(datum) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final CharSequence[]... data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            for (int i = 0; i < len; i++) {
                result ^= (z += (hash64(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }

        public int hash(final Object[] data) {
            if (data == null)
                return 0;
            final long chips = $alt << 1 ^ 0xC6BC279692B5CC83L; long z = 0x632BE59BD9B4E019L + chips, result = 1L, len = data.length;
            Object o;
            for (int i = 0; i < len; i++) {
                o = data[i];
                result ^= (z += ((o == null ? 0 : o.hashCode()) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * chips;

            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ $alt ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (chips + z >>> 58))) ^ (result >>> 32));
        }
    }
    @Beta
    public static class Falcon
    {
        public static long hash64(final byte[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result;
        }

        public static long hash64(final short[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result;
        }

        public static long hash64(final char[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result;
        }

        public static long hash64(final int[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result;
        }

        public static long hash64(final long[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result;
        }

        public static long hash64(final float[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= Float.floatToIntBits(data[i]) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result;
        }

        public static long hash64(final double[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= Double.doubleToLongBits(data[i]) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result;
        }

        public static long hash64(final CharSequence data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length(); i++) {
                result += (z ^= data.charAt(i) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result;
        }

        public static long hash64(final char[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = start; i < end && i < data.length; i++) {
                result += (z ^= data[i] * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result;
        }

        public static long hash64(final char[][] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= hash64(data[i]) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result;
        }

        public static long hash64(final CharSequence[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= hash64(data[i]) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result;
        }

        public static long hash64(final CharSequence[]... data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= hash64(data[i]) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result;
        }

        public static long hash64(final Object[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            Object o;
            for (int i = 0; i < data.length; i++) {
                o = data[i];
                result += (z ^= (o == null ? 0 : o.hashCode()) * 0xD0E89D2D311E289FL) + 0x9E3779B97F4A7C15L;
            }
            return result;
        }


        public static int hash(final byte[] data) {
            if (data == null)
                return 0;
            int z = 0xD9B4E019, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD11E289F) + 0x9E3779B9;
            }
            return result;
        }

        public static int hash(final short[] data) {
            if (data == null)
                return 0;
            int z = 0xD9B4E019, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD11E289F) + 0x9E3779B9;
            }
            return result;
        }

        public static int hash(final char[] data) {
            if (data == null)
                return 0;
            int z = 0xD9B4E019, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD11E289F) + 0x9E3779B9;
            }
            return result;
        }

        public static int hash(final int[] data) {
            if (data == null)
                return 0;
            int z = 0xD9B4E019, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD11E289F) + 0x9E3779B9;
            }
            return result;
        }

        public static int hash(final long[] data) {
            if (data == null)
                return 0;
            int z = 0xD9B4E019, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= data[i] * 0xD11E289F) + 0x9E3779B9;
            }
            return result;
        }

        public static int hash(final float[] data) {
            if (data == null)
                return 0;
            int z = 0xD9B4E019, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= Float.floatToIntBits(data[i]) * 0xD11E289F) + 0x9E3779B9;
            }
            return result;
        }

        public static int hash(final double[] data) {
            if (data == null)
                return 0;
            int z = 0xD9B4E019, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= Double.doubleToLongBits(data[i]) * 0xD11E289F) + 0x9E3779B9;
            }
            return result;
        }

        public static int hash(final CharSequence data) {
            if (data == null)
                return 0;
            int z = 0xD9B4E019, result = 1;
            for (int i = 0; i < data.length(); i++) {
                result += (z ^= data.charAt(i) * 0xD11E289F) + 0x9E3779B9;
            }
            return result;
        }

        public static int hash(final char[] data, int start, int end) {
            if (data == null || start >= end)
                return 0;
            int z = 0xD9B4E019, result = 1;
            for (int i = start; i < end && i < data.length; i++) {
                result += (z ^= data[i] * 0xD11E289F) + 0x9E3779B9;
            }
            return result;
        }

        public static int hash(final char[][] data) {
            if (data == null)
                return 0;
            int z = 0xD9B4E019, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= hash(data[i]) * 0xD11E289F) + 0x9E3779B9;
            }
            return result;
        }

        public static int hash(final CharSequence[] data) {
            if (data == null)
                return 0;
            int z = 0xD9B4E019, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= hash(data[i]) * 0xD11E289F) + 0x9E3779B9;
            }
            return result;
        }

        public static int hash(final CharSequence[]... data) {
            if (data == null)
                return 0;
            int z = 0xD9B4E019, result = 1;
            for (int i = 0; i < data.length; i++) {
                result += (z ^= hash(data[i]) * 0xD11E289F) + 0x9E3779B9;
            }
            return result;
        }

        public static int hash(final Object[] data) {
            if (data == null)
                return 0;
            int z = 0xD9B4E019, result = 1;
            Object o;
            for (int i = 0; i < data.length; i++) {
                o = data[i];
                result += (z ^= (o == null ? 0 : o.hashCode()) * 0xD11E289F) + 0x9E3779B9;
            }
            return result;
        }


    }
}

