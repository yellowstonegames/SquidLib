package squidpony.squidmath;

import squidpony.annotation.Beta;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Simple hashing functions that we can rely on staying the same cross-platform.
 * The static methods of this class (not its inner classes) use a custom algorithm
 * designed for speed and general-purpose usability, but not cryptographic security;
 * this algorithm is sometimes referred to as Wisp, and several other algorithms are
 * available in static inner classes, some of which have different goals, such as
 * reduced likelihood of successfully reversing a hash, or just providing another
 * choice. The hashes this returns are always 0 when given null to hash. Arrays with
 * identical elements of identical types will hash identically. Arrays with identical
 * numerical values but different types will sometimes hash differently. This class
 * always provides 64-bit hashes via hash64() and 32-bit hashes via hash(), and some
 * of the algorithms here may provide a hash32() method that matches older behavior
 * and uses only 32-bit math. The hash64() and hash() methods, at least in Wisp and
 * Mist, use 64-bit math even when producing 32-bit hashes, for GWT reasons. GWT
 * doesn't have the same behavior as desktop and Android applications when using ints
 * because it treats doubles mostly like ints, sometimes, due to it using JavaScript.
 * If we use mainly longs, though, GWT emulates the longs with a more complex
 * technique behind-the-scenes, that behaves the same on the web as it does on
 * desktop or on a phone. Since CrossHash is supposed to be stable cross-platform,
 * this is the way we need to go, despite it being slightly slower.
 * <br>
 * There are several static inner classes in CrossHash: Lightning, Falcon, and Mist,
 * each providing different hashing properties, as well as the inner IHasher interface and a
 * compatibility version of Wisp as a subclass. Older versions of SquidLib encouraged using a
 * subclass because the non-nested-class methods used a lousy implementation of the FNV-1a algorithm,
 * which was roughly 10x slower than the current methods in CrossHash and had poor correlation
 * properties. In the current version, you probably will be fine with the default functions in
 * CrossHash, using the Wisp algorithm. If you need a salt to alter the hash function,
 * using one of a large family of such functions instead of a single function like Wisp, then Mist
 * is a good choice. Lightning is mostly superseded by Wisp, but it can have better behavior on some
 * collections regarding collisions; Falcon is meant to be a faster version of Lightning.
 * <br>
 * IHasher values are provided as static fields, and use Wisp to hash a specific type or fall
 * back to Object.hashCode if given an object with the wrong type. IHasher values are optional
 * parts of OrderedMap, OrderedSet, Arrangement, and the various classes that use Arrangement
 * like K2 and K2V1, and allow arrays to be used as keys in those collections while keeping
 * hashing by value instead of the normal hashing by reference for arrays. You probably won't
 * ever need to make a class that implements IHasher yourself; for some cases you may want to
 * look at the {@link Hashers} class for additional functions.
 * <br>
 * The inner classes provide alternate, faster hashing algorithms. Lightning, Wisp, and Falcon
 * have no theoretical basis or grounds in any reason other than empirical testing for why they
 * do what they do, and this seems to be in line with many widely-used hashes (see: The Art of
 * Hashing, http://eternallyconfuzzled.com/tuts/algorithms/jsw_tut_hashing.aspx ). That said, Wisp
 * performs very well, ahead of Arrays.hashCode (10.5 ms instead of 15 ms) for over a million
 * hashes of 16-element long arrays, not including overhead for generating them, while SipHash and
 * FNV-1a take approximately 80 ms and 135-155 ms, respectively, for the same data). Lightning and
 * Falcon perform less-well, with Lightning taking 17 ms instead of 15 ms for Arrays.hashCode, and
 * Falcon taking about 12.3 ms but slowing down somewhat if a 32-bit hash is requested from long
 * data. All of these have good, low, collision rates on Strings and long arrays. Sketch is only
 * slightly slower than Wisp, but offers little to no advantage over it yet.
 * <br>
 * Mist is a variant on Wisp with 128 bits for a salt-like modifier as a member variable, which can
 * make 2 to the 128 individual hashing functions from one set of code, and uses 64 bits for some other
 * hashes (only calls to hash() with data that doesn't involve long or double arrays). Mist has some
 * minor resemblance to a cryptographic hash, but is not recommended it for that usage. It is,
 * however ideal for situations that show up often in game development where end users may be able
 * to see and possibly alter some information that you don't want changed (i.e. save data stored on
 * a device or in the browser's LocalStorage). If you want a way to verify the data is what you
 * think it is, you can store a hash, using one of the many-possible hash functions this can
 * produce, somewhere else and verify that the saved data has the hash it did last time; if the
 * exact hashing function isn't known (or exact functions aren't known) by a tampering user,
 * then it is unlikely they can make the hash match even if they can edit it. Mist is slightly slower
 * than Wisp, at about 18 ms for Mist for the same data instead of Wisp's 10.5, but should never be
 * worse than twice as slow as Arrays.hashCode, and is still about three times faster than the similar
 * SipHash that SquidLib previously had here.
 * <br>
 * All of the hashes used here have about the same rate of collisions on identical data
 * (testing used Arrays.hashCode, all the hashes in here now, and the now-removed SipHash), with
 * any fluctuation within some small margin of error. Wisp (typically via the non-nested methods
 * in CrossHash) and Mist are the two most likely algorithms you might use here.
 * <br>
 * To help find patterns in hash output in a visual way, you can hash an x,y point, take the bottom 24 bits,
 * and use that as an RGB color for the pixel at that x,y point. On a 512x512 grid of points, the patterns
 * in Arrays.hashCode and the former default CrossHash algorithm (FNV-1a) are evident, and Sip (implementing
 * SipHash) did approximately as well as Lightning, with no clear patterns visible (Sip has been removed from
 * SquidLib because it needs a lot of code and is slower than all of the current hashes). The idea is from
 * <a href="http://www.clockandflame.com/media/Goulburn06.pdf">a technical report on visual uses for hashing (PDF)</a>.
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
 * Note 2: FNV-1a was removed from SquidLib on July 25, 2017, and replaced as default with Wisp. This
 * accompanied a general start toward preferring 64-bit math for GWT compatibility reasons, although
 * that is counter-intuitive because 64-bit longs aren't native to JavaScript and GWT implements its
 * own variety.
 * <br>
 * Created by Tommy Ettinger on 1/16/2016.
 * @author Tommy Ettinger
 */
public class CrossHash {
    public static int hash(final boolean[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * (data[i] ? 0xC6BC279692B5CC83L : 0xAEF17502108EF2D9L));
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final byte[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final short[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final char[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final int[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final long[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final float[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.floatToIntBits(data[i]));
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final double[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.doubleToMixedIntBits(data[i]));
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final CharSequence data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data.charAt(i));
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }
    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 32-bit hash code for the requested section of data
     */
    public static int hash(final char[] data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = end < data.length ? end : data.length;
        for (int i = start; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }
    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 32-bit hash code for the requested section of data
     */
    public static int hash(final CharSequence data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = end < data.length() ? end : data.length();
        for (int i = start; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data.charAt(i));
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
     * moving between chars in increments of step (which is always greater than 0).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @param step  how many elements to advance after using one element from data; must be greater than 0
     * @return a 32-bit hash code for the requested section of data
     */
    public static int hash(final char[] data, final int start, final int end, final int step) {
        if (data == null || start >= end || step <= 0)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = end < data.length ? end : data.length;
        for (int i = start; i < len; i+= step) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }
    /**
     * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
     * moving between chars in increments of step (which is always greater than 0).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @param step  how many elements to advance after using one element from data; must be greater than 0
     * @return a 32-bit hash code for the requested section of data
     */
    public static int hash(final CharSequence data, final int start, final int end, final int step) {
        if (data == null || start >= end || step <= 0)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = end < data.length() ? end : data.length();
        for (int i = start; i < len; i += step) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data.charAt(i));
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final char[][] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final int[][] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final long[][] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final CharSequence[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final CharSequence[]... data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final Iterable<? extends CharSequence> data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        for (CharSequence datum : data) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(datum));
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final List<? extends CharSequence> data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.size();
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data.get(i)));
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final Object[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        Object o;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * ((o = data[i]) == null ? -1L : o.hashCode()));
        }
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static int hash(final Object data) {
        if (data == null)
            return 0;
        long a = 0x632BE59BD9B4E019L ^ 0x8329C6EB9E6AD3E3L * data.hashCode(), result = 0x9E3779B97F4A7C94L + a;
        return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
    }

    public static long hash64(final boolean[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * (data[i] ? 0xC6BC279692B5CC83L : 0xAEF17502108EF2D9L));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final byte[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final short[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final char[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final int[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final long[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final float[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.floatToIntBits(data[i]));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final double[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.doubleToMixedIntBits(data[i]));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final CharSequence data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data.charAt(i));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }
    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 64-bit hash code for the requested section of data
     */
    public static long hash64(final char[] data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = end < data.length ? end : data.length;
        for (int i = start; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }
    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 64-bit hash code for the requested section of data
     */
    public static long hash64(final CharSequence data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = end < data.length() ? end : data.length();
        for (int i = start; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data.charAt(i));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }
    /**
     * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
     * moving between chars in increments of step (which is always greater than 0).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @param step  how many elements to advance after using one element from data; must be greater than 0
     * @return a 64-bit hash code for the requested section of data
     */
    public static long hash64(final char[] data, final int start, final int end, final int step) {
        if (data == null || start >= end || step <= 0)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = end < data.length ? end : data.length;
        for (int i = start; i < len; i += step) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }
    /**
     * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
     * moving between chars in increments of step (which is always greater than 0).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @param step  how many elements to advance after using one element from data; must be greater than 0
     * @return a 64-bit hash code for the requested section of data
     */
    public static long hash64(final CharSequence data, final int start, final int end, final int step) {
        if (data == null || start >= end || step <= 0)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = end < data.length() ? end : data.length();
        for (int i = start; i < len; i += step) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * data.charAt(i));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final char[][] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final int[][] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final long[][] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final CharSequence[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final CharSequence[]... data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final Iterable<? extends CharSequence> data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        for (CharSequence datum : data) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(datum));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final List<? extends CharSequence> data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.size();
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data.get(i)));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final Object[] data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = data.length;
        Object o;
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * ((o = data[i]) == null ? -1L : o.hashCode()));
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    public static long hash64(final Object data) {
        if (data == null)
            return 0L;
        long a = 0x632BE59BD9B4E019L ^ 0x8329C6EB9E6AD3E3L * data.hashCode(), result = 0x9E3779B97F4A7C94L + a;
        return result * (a | 1L) ^ (result >>> 27 | result << 37);
    }

    /**
     * An interface that can be used to move the logic for the hashCode() and equals() methods from a class' methods to
     * an implementation of IHasher that certain collections in SquidLib can use. Primarily useful when the key type is
     * an array, which normally doesn't work as expected in Java hash-based collections, but can if the right collection
     * and IHasher are used. See also {@link Hashers} for additional implementations, some of which need dependencies on
     * things the rest of CrossHash doesn't, like a case-insensitive String hasher/equator that uses RegExodus to handle
     * CharSequence comparison on GWT.
     */
    public interface IHasher extends Serializable {
        /**
         * If data is a type that this IHasher can specifically hash, this method should use that specific hash; in
         * other situations, it should simply delegate to calling {@link Object#hashCode()} on data. The body of an
         * implementation of this method can be very small; for an IHasher that is meant for byte arrays, the body could
         * be: {@code return (data instanceof byte[]) ? CrossHash.Lightning.hash((byte[]) data) : data.hashCode();}
         *
         * @param data the Object to hash; this method should take any type but often has special behavior for one type
         * @return a 32-bit int hash code of data
         */
        int hash(final Object data);

        /**
         * Not all types you might want to use an IHasher on meaningfully implement .equals(), such as array types; in
         * these situations the areEqual method helps quickly check for equality by potentially having special logic for
         * the type this is meant to check. The body of implementations for this method can be fairly small; for byte
         * arrays, it looks like: {@code return left == right
         * || ((left instanceof byte[] && right instanceof byte[])
         * ? Arrays.equals((byte[]) left, (byte[]) right)
         * : Objects.equals(left, right));} , but for multidimensional arrays you should use the
         * {@link #equalityHelper(Object[], Object[], IHasher)} method with an IHasher for the inner arrays that are 1D
         * or otherwise already-hash-able, as can be seen in the body of the implementation for 2D char arrays, where
         * charHasher is an existing IHasher that handles 1D arrays:
         * {@code return left == right
         * || ((left instanceof char[][] && right instanceof char[][])
         * ? equalityHelper((char[][]) left, (char[][]) right, charHasher)
         * : Objects.equals(left, right));}
         *
         * @param left  allowed to be null; most implementations will have special behavior for one type
         * @param right allowed to be null; most implementations will have special behavior for one type
         * @return true if left is equal to right (preferably by value, but reference equality may sometimes be needed)
         */
        boolean areEqual(final Object left, final Object right);
    }

    /**
     * Not a general-purpose method; meant to ease implementation of {@link IHasher#areEqual(Object, Object)}
     * methods when the type being compared is a multi-dimensional array (which normally requires the heavyweight method
     * {@link Arrays#deepEquals(Object[], Object[])} or doing more work yourself; this reduces the work needed to
     * implement fixed-depth equality). As mentioned in the docs for {@link IHasher#areEqual(Object, Object)}, example
     * code that hashes 2D char arrays can be done using an IHasher for 1D char arrays called charHasher:
     * {@code return left == right
     * || ((left instanceof char[][] && right instanceof char[][])
     * ? equalityHelper((char[][]) left, (char[][]) right, charHasher)
     * : Objects.equals(left, right));}
     *
     * @param left an array of some kind of Object, usually an array, that the given IHasher can compare
     * @param right an array of some kind of Object, usually an array, that the given IHasher can compare
     * @param inner an IHasher to compare items in left with items in right
     * @return true if the contents of left and right are equal by the given IHasher, otherwise false
     */
    public static boolean equalityHelper(Object[] left, Object[] right, IHasher inner) {
        if (left == right)
            return true;
        if (left == null || right == null || left.length != right.length)
            return false;
        for (int i = 0; i < left.length; i++) {
            if (!inner.areEqual(left[i], right[i]))
                return false;
        }
        return true;
    }

    private static class BooleanHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        BooleanHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof boolean[]) ? CrossHash.hash((boolean[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof boolean[] && right instanceof boolean[]) ? Arrays.equals((boolean[]) left, (boolean[]) right) : Objects.equals(left, right));
        }
    }

    public static final IHasher booleanHasher = new BooleanHasher();

    private static class ByteHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        ByteHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof byte[]) ? CrossHash.hash((byte[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right
                    || ((left instanceof byte[] && right instanceof byte[])
                    ? Arrays.equals((byte[]) left, (byte[]) right)
                    : Objects.equals(left, right));
        }
    }

    public static final IHasher byteHasher = new ByteHasher();

    private static class ShortHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        ShortHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof short[]) ? CrossHash.hash((short[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof short[] && right instanceof short[]) ? Arrays.equals((short[]) left, (short[]) right) : Objects.equals(left, right));
        }
    }

    public static final IHasher shortHasher = new ShortHasher();

    private static class CharHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        CharHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof char[]) ? CrossHash.hash((char[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof char[] && right instanceof char[]) ? Arrays.equals((char[]) left, (char[]) right) : Objects.equals(left, right));
        }
    }

    public static final IHasher charHasher = new CharHasher();

    private static class IntHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        IntHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof int[]) ? CrossHash.hash((int[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return (left instanceof int[] && right instanceof int[]) ? Arrays.equals((int[]) left, (int[]) right) : Objects.equals(left, right);
        }
    }

    public static final IHasher intHasher = new IntHasher();

    private static class LongHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        LongHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof long[]) ? CrossHash.hash((long[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return (left instanceof long[] && right instanceof long[]) ? Arrays.equals((long[]) left, (long[]) right) : Objects.equals(left, right);
        }
    }

    public static final IHasher longHasher = new LongHasher();

    private static class FloatHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        FloatHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof float[]) ? CrossHash.hash((float[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof float[] && right instanceof float[]) ? Arrays.equals((float[]) left, (float[]) right) : Objects.equals(left, right));
        }
    }

    public static final IHasher floatHasher = new FloatHasher();

    private static class DoubleHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        DoubleHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof double[]) ? CrossHash.hash((double[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof double[] && right instanceof double[]) ? Arrays.equals((double[]) left, (double[]) right) : Objects.equals(left, right));
        }
    }

    public static final IHasher doubleHasher = new DoubleHasher();

    private static class Char2DHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        Char2DHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof char[][]) ? CrossHash.hash((char[][]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right
                    || ((left instanceof char[][] && right instanceof char[][])
                    ? equalityHelper((char[][]) left, (char[][]) right, charHasher)
                    : Objects.equals(left, right));
        }
    }

    public static final IHasher char2DHasher = new Char2DHasher();

    private static class Int2DHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        Int2DHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof int[][]) ? CrossHash.hash((int[][]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right
                    || ((left instanceof int[][] && right instanceof int[][])
                    ? equalityHelper((int[][]) left, (int[][]) right, intHasher)
                    : Objects.equals(left, right));
        }
    }

    public static final IHasher int2DHasher = new Int2DHasher();

    private static class Long2DHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        Long2DHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof long[][]) ? CrossHash.hash((long[][]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right
                    || ((left instanceof long[][] && right instanceof long[][])
                    ? equalityHelper((long[][]) left, (long[][]) right, longHasher)
                    : Objects.equals(left, right));
        }
    }

    public static final IHasher long2DHasher = new Long2DHasher();

    private static class StringHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        StringHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof CharSequence) ? CrossHash.hash((CharSequence) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return Objects.equals(left, right);
        }
    }

    public static final IHasher stringHasher = new StringHasher();

    private static class StringArrayHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        StringArrayHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof CharSequence[]) ? CrossHash.hash((CharSequence[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof CharSequence[] && right instanceof CharSequence[]) ? equalityHelper((CharSequence[]) left, (CharSequence[]) right, stringHasher) : Objects.equals(left, right));
        }
    }

    /**
     * Though the name suggests this only hashes String arrays, it can actually hash any CharSequence array as well.
     */
    public static final IHasher stringArrayHasher = new StringArrayHasher();

    private static class ObjectArrayHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        ObjectArrayHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof Object[]) ? CrossHash.hash((Object[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof Object[] && right instanceof Object[]) && Arrays.equals((Object[]) left, (Object[]) right) || Objects.equals(left, right));
        }
    }
    public static final IHasher objectArrayHasher = new ObjectArrayHasher();

    private static class DefaultHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 4L;

        DefaultHasher() {
        }

        @Override
        public int hash(final Object data) {
            if(data == null) return 0;
            final int x = data.hashCode() * 0x62BD5;
            return x ^ ((x << 17) | (x >>> 15)) ^ ((x << 9) | (x >>> 23));
        }

        @Override
        public boolean areEqual(final Object left, final Object right) {
            return (left == right) || (left != null && left.equals(right));
        }
    }

    public static final IHasher defaultHasher = new DefaultHasher();

    private static class IdentityHasher implements IHasher, Serializable
    {
        private static final long serialVersionUID = 4L;
        IdentityHasher() { }

        @Override
        public int hash(Object data) {
            final int x = System.identityHashCode(data) * 0x62BD5;
            return x ^ ((x << 17) | (x >>> 15)) ^ ((x << 9) | (x >>> 23));
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right;
        }
    }
    public static final IHasher identityHasher = new IdentityHasher();

    private static class GeneralHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        GeneralHasher() {
        }

        @Override
        public int hash(final Object data) {
            return CrossHash.hash(data);
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            if(left == right) return true;
            Class l = left.getClass(), r = right.getClass();
            if(l == r)
            {
                if(l.isArray())
                {
                    if(left instanceof int[]) return Arrays.equals((int[]) left, (int[]) right);
                    else if(left instanceof long[]) return Arrays.equals((long[]) left, (long[]) right);
                    else if(left instanceof char[]) return Arrays.equals((char[]) left, (char[]) right);
                    else if(left instanceof double[]) return Arrays.equals((double[]) left, (double[]) right);
                    else if(left instanceof boolean[]) return Arrays.equals((boolean[]) left, (boolean[]) right);
                    else if(left instanceof byte[]) return Arrays.equals((byte[]) left, (byte[]) right);
                    else if(left instanceof float[]) return Arrays.equals((float[]) left, (float[]) right);
                    else if(left instanceof short[]) return Arrays.equals((short[]) left, (short[]) right);
                    else if(left instanceof char[][]) return equalityHelper((char[][]) left, (char[][]) right, charHasher);
                    else if(left instanceof int[][]) return equalityHelper((int[][]) left, (int[][]) right, intHasher);
                    else if(left instanceof long[][]) return equalityHelper((long[][]) left, (long[][]) right, longHasher);
                    else if(left instanceof CharSequence[]) return equalityHelper((CharSequence[]) left, (CharSequence[]) right, stringHasher);
                    else if(left instanceof Object[]) return Arrays.equals((Object[]) left, (Object[]) right);
                }
                return Objects.equals(left, right);
            }
            return false;
        }
    }

    /**
     * This IHasher is the one you should use if you aren't totally certain what types will go in an OrderedMap's keys
     * or an OrderedSet's items, since it can handle mixes of elements.
     */
    public static final IHasher generalHasher = new GeneralHasher();

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
     * gets 135 collisions. Dispersion is not perfect, but
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
     * from SquidLib because it needs a lot of code and is slower than Mist). The
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
    public static final class Lightning {

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
                result ^= (z += (NumberTools.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        public static long hash64(final double[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (NumberTools.doubleToMixedIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }

        /**
         * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @return a 64-bit hash code for the requested section of data
         */
        public static long hash64(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = start; i < end && i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
        }
        /**
         * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
         * moving between chars in increments of step (which is always greater than 0).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @param step  how many elements to advance after using one element from data; must be greater than 0
         * @return a 64-bit hash code for the requested section of data
         */
        public static long hash64(final char[] data, final int start, final int end, final int step) {
            if (data == null || start >= end || step <= 0)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = start; i < end && i < data.length; i += step) {
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
            return result ^ Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58));
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

        public static long hash64(final long[][] data) {
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

        public static long hash64(final Iterable<? extends CharSequence> data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (CharSequence datum : data) {
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
                result ^= (z += (NumberTools.floatToIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        public static int hash(final double[] data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (NumberTools.doubleToMixedIntBits(data[i]) + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }

        /**
         * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @return a 32-bit hash code for the requested section of data
         */
        public static int hash(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;

            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = start; i < end && i < data.length; i++) {
                result ^= (z += (data[i] + 0x9E3779B97F4A7C15L) * 0xD0E89D2D311E289FL) * 0xC6BC279692B5CC83L;
            }
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
        }
        /**
         * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
         * moving between chars in increments of step (which is always greater than 0).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @param step  how many elements to advance after using one element from data; must be greater than 0
         * @return a 32-bit hash code for the requested section of data
         */
        public static int hash(final char[] data, final int start, final int end, final int step) {
            if (data == null || start >= end || step <= 0)
                return 0;

            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (int i = start; i < end && i < data.length; i += step) {
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
            return (int) ((result ^= Long.rotateLeft((z * 0xC6BC279692B5CC83L ^ result * 0x9E3779B97F4A7C15L) + 0x632BE59BD9B4E019L, (int) (z >>> 58))) ^ (result >>> 32));
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

        public static int hash(final long[][] data) {
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

        public static int hash(final Iterable<? extends CharSequence> data) {
            if (data == null)
                return 0;
            long z = 0x632BE59BD9B4E019L, result = 1L;
            for (CharSequence datum : data) {
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
     * An alternative hashing function that can have many fewer collisions in some pathological cases where Wisp fails.
     * Testing on repetitive Strings like
     * {@code "dun dun, dun dun, dun dundun, dun dun dundun dun dundun, dun dundun, dun dundun, dun dundun, dun dun dun, dun dun, dun dun, dun dun, dun..."},
     * this avoids difficult collision situations when using a small mask on the hash. It may be fairly fast, but
     * benchmarking is ongoing.
     * <br>
     * The old Falcon class had some serious flaws and wasn't used in many places in SquidLib, so it's been removed.
     * <br>
     * Created by Tommy Ettinger on 1/16/2016; completely rewritten on 6/27/2018.
     */
    @Beta
    public static final class Falcon {
        public static long hash64(final boolean[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += ((data[i] ? 0xC6BC279692B5CC83L : 0x789ABCDEFEDCBA98L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static long hash64(final byte[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static long hash64(final short[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static long hash64(final char[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static long hash64(final int[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static long hash64(final long[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static long hash64(final float[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (NumberTools.floatToIntBits(data[i]) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static long hash64(final double[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (NumberTools.doubleToLongBits(data[i]) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static long hash64(final CharSequence data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length(); i++) {
                result ^= (z += (data.charAt(i) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static long hash64(final String data) {
            if (data == null)
                return 0;
            long x = data.hashCode() * 0x9E3779B97F4A7C15L;
            x ^= x >>> 25;
            return x ^ ((x << 17) | (x >>> 47)) ^ ((x << 41) | (x >>> 23));
        }

        /**
         * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @return a 64-bit hash code for the requested section of data
         */
        public static long hash64(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = start; i < end && i < data.length; i++) {
                result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        /**
         * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
         * moving between chars in increments of step (which is always greater than 0).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @param step  how many elements to advance after using one element from data; must be greater than 0
         * @return a 64-bit hash code for the requested section of data
         */
        public static long hash64(final char[] data, final int start, final int end, final int step) {
            if (data == null || start >= end || step <= 0)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = start; i < end && i < data.length; i += step) {
                result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static long hash64(final char[][] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static long hash64(final long[][] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static long hash64(final CharSequence[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static long hash64(final CharSequence[]... data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static long hash64(final Object[] data) {
            if (data == null)
                return 0;
            Object o;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                o = data[i];
                result ^= (z += ((o == null ? 0 : o.hashCode()) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (result ^ result >>> 25 ^ z ^ z >>> 29);
        }


        public static int hash(final boolean[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += ((data[i] ? 0xC6BC279692B5CC83L : 0x789ABCDEFEDCBA98L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static int hash(final byte[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static int hash(final short[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static int hash(final char[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static int hash(final int[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static int hash(final long[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static int hash(final float[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (NumberTools.floatToIntBits(data[i]) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static int hash(final double[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (NumberTools.doubleToLongBits(data[i]) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static int hash(final CharSequence data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length(); i++) {
                result ^= (z += (data.charAt(i) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }
        public static int hash(final String data) {
            if (data == null)
                return 0;
            int x = data.hashCode() * 0x62BD5;
            x ^= x >>> 13;
            return x ^ ((x << 17) | (x >>> 15)) ^ ((x << 9) | (x >>> 23));
        }

        /**
         * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @return a 64-bit hash code for the requested section of data
         */
        public static int hash(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = start; i < end && i < data.length; i++) {
                result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        /**
         * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
         * moving between chars in increments of step (which is always greater than 0).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @param step  how many elements to advance after using one element from data; must be greater than 0
         * @return a 64-bit hash code for the requested section of data
         */
        public static int hash(final char[] data, final int start, final int end, final int step) {
            if (data == null || start >= end || step <= 0)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = start; i < end && i < data.length; i += step) {
                result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);

        }

        public static int hash(final char[][] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static int hash(final long[][] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static int hash(final CharSequence[] data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static int hash(final CharSequence[]... data) {
            if (data == null)
                return 0;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                result ^= (z += (hash64(data[i]) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }

        public static int hash(final Object[] data) {
            if (data == null)
                return 0;
            Object o;
            long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
            for (int i = 0; i < data.length; i++) {
                o = data[i];
                result ^= (z += ((o == null ? 0 : o.hashCode()) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
            }
            result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
            return (int)(result ^ result >>> 25 ^ z ^ z >>> 29);
        }
    }
    // Nice ints, all probable primes except the last one, for 32-bit hashing
    // 0x62E2AC0D 0x632BE5AB 0x85157AF5 0x9E3779B9
    /**
     * The fastest hash in CrossHash, but no slouch on quality, either. This is the default used by methods in the outer
     * class like {@link CrossHash#hash(int[])}, which should be the same as {@link CrossHash.Wisp#hash(int[])}; you
     * should prefer using the outer class since this inner class is only still here for backwards-compatibility. Uses a
     * finely-tuned mix of very few operations for each element, plus a minimal and simple finalization step, and as
     * such obtains superior speed on the standard benchmark SquidLib uses for hashes (hashing one million 16-element
     * long arrays, remaining the best in both 32-bit and 64-bit versions). Specifically, Wisp takes 9.478 ms to
     * generate a million 64-bit hashes on a recent laptop with an i7-6700HQ processor (removing the time the control
     * takes to generate the million arrays), whereas on the same setup the second-fastest hash, Falcon, takes 12.783 ms
     * (also removing generation time). For comparison, the JDK's Arrays.hashCode method takes 13.642 ms on the same
     * workload, though it produces 32-bit hashes. Wisp performs almost exactly as well producing 32-bit hashes as it
     * does 64-bit hashes, where Falcon slows down slightly, and other hashes suffer a larger penalty producing 32-bit.
     * This also passes visual tests where an earlier version of Wisp did not. Collision rates are on-par with all other
     * CrossHash classes and the JDK's Arrays.hashCode method, that is, acceptably low.
     * <br>
     * This version replaces an older version of Wisp that had serious quality issues and wasn't quite as fast. Since
     * the only reason one would use the older version was speed without regard for quality, and it was marked as Beta,
     * a faster version makes sense to replace the slower one, rather than add yet another nested class in CrossHash.
     * <br>
     * Wisp is no longer considered Beta-quality, and due to its speed and apparently very low collision rate in most
     * arrays, it's recommended for usage in more places now. Code that used Lightning should in probably switch to Wisp
     * if GWT is a potential target, since Wisp doesn't rely on having certain JVM optimizations that are probably only
     * available on desktop platforms.
     * @see CrossHash Prefer using the hash() and hash64() methods in the outer class to this inner one; this inner
     * class is only here for compatibility and can be removed by minifiers if you don't use it
     */
    public static final class Wisp {
        public static long hash64(final boolean[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * (data[i] ? 0xC6BC279692B5CC83L : 0xAEF17502108EF2D9L));
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final byte[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final short[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final char[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final int[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final long[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final float[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.floatToIntBits(data[i]));
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final double[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.doubleToMixedIntBits(data[i]));
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final CharSequence data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length();
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data.charAt(i));
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        /**
         * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @return a 64-bit hash code for the requested section of data
         */
        public static long hash64(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = end < data.length ? end : data.length;
            for (int i = start; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        /**
         * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
         * moving between chars in increments of step (which is always greater than 0).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @param step  how many elements to advance after using one element from data; must be greater than 0
         * @return a 64-bit hash code for the requested section of data
         */
        public static long hash64(final char[] data, final int start, final int end, final int step) {
            if (data == null || start >= end || step <= 0)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = end < data.length ? end : data.length;
            for (int i = start; i < len; i += step) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final char[][] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final int[][] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final long[][] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final CharSequence[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final CharSequence[]... data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final Iterable<? extends CharSequence> data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            for (CharSequence datum : data) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(datum));
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final List<? extends CharSequence> data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.size();
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data.get(i)));
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final Object[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            Object o;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * ((o = data[i]) == null ? -1L : o.hashCode()));
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static long hash64(final Object data) {
            if (data == null)
                return 0L;
            long a = 0x632BE59BD9B4E019L ^ 0x8329C6EB9E6AD3E3L * data.hashCode(), result = 0x9E3779B97F4A7C94L + a;
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        public static int hash32(final boolean[] data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * (data[i] ? 0x789ABCDE : 0x62E2AC0D));
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }


        public static int hash32(final byte[] data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * data[i]);
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        public static int hash32(final short[] data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * data[i]);
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        public static int hash32(final char[] data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * data[i]);
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }
        public static int hash32(final int[] data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * data[i]);
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        public static int hash32(final long[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return (int)((a += (result >>> 27 | result << 37)) ^ (a >>> 32));
        }

        public static int hash32(final float[] data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * NumberTools.floatToIntBits(data[i]));
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        public static int hash32(final double[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            double t;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * ((long) (-0xD0E8.9D2D311E289Fp-25 * (t = data[i]) + t * -0x1.39b4dce80194cp9)));
            }
            return (int)((result = (result * (a | 1L) ^ (result >>> 27 | result << 37))) ^ (result >>> 32));
        }

        public static int hash32(final CharSequence data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.length();
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * data.charAt(i));
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        /**
         * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
         * Uses 32-bit math on most platforms, but will give different results on GWT due to it using double values that
         * only somewhat act like int values.
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @return a 32-bit hash code for the requested section of data
         */
        public static int hash32(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = end < data.length ? end : data.length;
            for (int i = start; i < len; i++) {
                result += (a ^= 0x85157AF5 * data[i]);
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        public static int hash32(final char[][] data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * hash32(data[i]));
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        public static int hash32(final int[][] data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * hash32(data[i]));
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        public static int hash32(final long[][] data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * hash32(data[i]));
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        public static int hash32(final CharSequence[] data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * hash32(data[i]));
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        public static int hash32(final CharSequence[]... data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * hash32(data[i]));
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        public static int hash32(final Iterable<? extends CharSequence> data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            for (CharSequence datum : data) {
                result += (a ^= 0x85157AF5 * hash32(datum));
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        public static int hash32(final List<? extends CharSequence> data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.size();
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * hash32(data.get(i)));
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        public static int hash32(final Object[] data) {
            if (data == null)
                return 0;
            int result = 0x9E3779B9, a = 0x632BE5AB;
            final int len = data.length;
            Object o;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x85157AF5 * ((o = data[i]) == null ? -1 : o.hashCode()));
            }
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }

        public static int hash32(final Object data) {
            if (data == null)
                return 0;
            int a = 0x632BE5AB ^ 0x85157AF5 * data.hashCode(), result = 0x9E3779B9 + a;
            return result * (a | 1) ^ (result >>> 11 | result << 21);
        }
        public static int hash(final boolean[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * (data[i] ? 0xC6BC279692B5CC83L : 0xAEF17502108EF2D9L));
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final byte[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final short[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final char[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final int[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final long[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final float[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.floatToIntBits(data[i]));
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final double[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.doubleToMixedIntBits(data[i]));
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final CharSequence data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length();
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data.charAt(i));
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }
        /**
         * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @return a 32-bit hash code for the requested section of data
         */
        public static int hash(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = end < data.length ? end : data.length;
            for (int i = start; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        /**
         * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
         * moving between chars in increments of step (which is always greater than 0).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @param step  how many elements to advance after using one element from data; must be greater than 0
         * @return a 32-bit hash code for the requested section of data
         */
        public static int hash(final char[] data, final int start, final int end, final int step) {
            if (data == null || start >= end || step <= 0)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = end < data.length ? end : data.length;
            for (int i = start; i < len; i+= step) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]);
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final char[][] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final int[][] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final long[][] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final CharSequence[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final CharSequence[]... data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i]));
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final Iterable<? extends CharSequence> data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            for (CharSequence datum : data) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(datum));
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final List<? extends CharSequence> data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.size();
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data.get(i)));
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final Object[] data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
            final int len = data.length;
            Object o;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * ((o = data[i]) == null ? -1L : o.hashCode()));
            }
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

        public static int hash(final Object data) {
            if (data == null)
                return 0;
            long a = 0x632BE59BD9B4E019L ^ 0x8329C6EB9E6AD3E3L * data.hashCode(), result = 0x9E3779B97F4A7C94L + a;
            return (int)(result * (a | 1L) ^ (result >>> 27 | result << 37));
        }

    }

    /**
     * A whole cluster of Wisp-like hash functions that sacrifice a small degree of speed, but can be built with up
     * to 128 bits of salt values that help to obscure what hashing function is actually being used. This class is
     * similar to the older Storm variety, but is somewhat faster and has many more possible salt "states" when using
     * the constructors that take two longs or a CharSequence. There isn't really any reason to use Storm, so Mist has
     * now replaced Storm entirely. Code that used Storm should be able to just change any usage of "Storm" to "Mist".
     * <br>
     * The salt fields are not serialized, so it is important that the same salt will be given by the
     * program when the same hash results are wanted for some inputs.
     * <br>
     * A group of 48 static, final, pre-initialized Mist members are present in this class, 24 with the
     * name of a letter in the Greek alphabet (this uses the convention on Wikipedia,
     * https://en.wikipedia.org/wiki/Greek_alphabet#Letters , where lambda is spelled with a 'b') and 24 with the same
     * name followed by an underscore, such as {@link #alpha_}. The whole group of 48 pre-initialized members are also
     * present in a static array called {@code predefined}. These can be useful when, for example, you want to get
     * multiple hashes of a single array or String as part of cuckoo hashing or similar techniques that need multiple
     * hashes for the same inputs.
     */
    @Beta
    public static final class Mist implements Serializable {
        private static final long serialVersionUID = -1275284837479983271L;

        private transient final long $l1, $l2;

        public Mist() {
            this(0x1234567876543210L, 0xEDCBA98789ABCDEFL);
        }

        public Mist(final CharSequence alteration) {
            this(CrossHash.hash64(alteration), Lightning.hash64(alteration));
        }
        private static int permute(final long state)
        {
            int s = (int)state ^ 0xD0E89D2D;
            s = (s >>> 19 | s << 13);
            s ^= state >>> (5 + (state >>> 59));
            return ((s *= 277803737) >>> 22) ^ s;
        }

        @SuppressWarnings("NumericOverflow")
        public Mist(final long alteration) {
            long l1, l2;
            l1 = alteration + permute(alteration);
            l1 = (l1 ^ (l1 >>> 30)) * 0xBF58476D1CE4E5B9L;
            l1 = (l1 ^ (l1 >>> 27)) * 0x94D049BB133111EBL;
            $l1 = l1 ^ l1 >>> 31;

            l2 = alteration + 6 * 0x9E3779B97F4A7C15L;
            l2 = (l2 ^ (l2 >>> 30)) * 0xBF58476D1CE4E5B9L;
            l2 = (l2 ^ (l2 >>> 27)) * 0x94D049BB133111EBL;
            $l2 = l2 ^ l2 >>> 31;
        }

        @SuppressWarnings("NumericOverflow")
        public Mist(final long alteration1, long alteration2) {
            final int i1 = permute(alteration1);
            $l1 = alteration1 + i1;
            $l2 = alteration2 + permute(alteration2 + i1);
        }

        /**
         * Makes a new Mist with all of the salt values altered based on the previous salt values.
         * This will make a different, incompatible Mist object that will give different results than the original.
         * Meant for use in Cuckoo Hashing, which can need the hash function to be updated or changed.
         * An alternative is to select a different Mist object from {@link #predefined}, or to simply
         * construct a new Mist with a different parameter or set of parameters.
         */
        @SuppressWarnings("NumericOverflow")
        public Mist randomize()
        {
            long l1, l2;
            l1 = $l2 + permute($l2 + 3 * 0x9E3779B97F4A7C15L);
            l1 = (l1 ^ (l1 >>> 30)) * 0xBF58476D1CE4E5B9L;
            l1 = (l1 ^ (l1 >>> 27)) * 0x94D049BB133111EBL;
            l1 ^= l1 >>> 31;

            l2 = permute(l1 + 5 * 0x9E3779B97F4A7C15L) + 6 * 0x9E3779B97F4A7C15L;
            l2 = (l2 ^ (l2 >>> 30)) * 0xBF58476D1CE4E5B9L;
            l2 = (l2 ^ (l2 >>> 27)) * 0x94D049BB133111EBL;
            l2 ^= l2 >>> 31;

            return new Mist(l1, l2);
        }

        public static final Mist alpha = new Mist("alpha"), beta = new Mist("beta"), gamma = new Mist("gamma"),
                delta = new Mist("delta"), epsilon = new Mist("epsilon"), zeta = new Mist("zeta"),
                eta = new Mist("eta"), theta = new Mist("theta"), iota = new Mist("iota"),
                kappa = new Mist("kappa"), lambda = new Mist("lambda"), mu = new Mist("mu"),
                nu = new Mist("nu"), xi = new Mist("xi"), omicron = new Mist("omicron"), pi = new Mist("pi"),
                rho = new Mist("rho"), sigma = new Mist("sigma"), tau = new Mist("tau"),
                upsilon = new Mist("upsilon"), phi = new Mist("phi"), chi = new Mist("chi"), psi = new Mist("psi"),
                omega = new Mist("omega"),
                alpha_ = new Mist("ALPHA"), beta_ = new Mist("BETA"), gamma_ = new Mist("GAMMA"),
                delta_ = new Mist("DELTA"), epsilon_ = new Mist("EPSILON"), zeta_ = new Mist("ZETA"),
                eta_ = new Mist("ETA"), theta_ = new Mist("THETA"), iota_ = new Mist("IOTA"),
                kappa_ = new Mist("KAPPA"), lambda_ = new Mist("LAMBDA"), mu_ = new Mist("MU"),
                nu_ = new Mist("NU"), xi_ = new Mist("XI"), omicron_ = new Mist("OMICRON"), pi_ = new Mist("PI"),
                rho_ = new Mist("RHO"), sigma_ = new Mist("SIGMA"), tau_ = new Mist("TAU"),
                upsilon_ = new Mist("UPSILON"), phi_ = new Mist("PHI"), chi_ = new Mist("CHI"), psi_ = new Mist("PSI"),
                omega_ = new Mist("OMEGA");
        /**
         * Has a length of 48, which may be relevant if automatically choosing a predefined hash functor.
         */
        public static final Mist[] predefined = new Mist[]{alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota,
                kappa, lambda, mu, nu, xi, omicron, pi, rho, sigma, tau, upsilon, phi, chi, psi, omega,
                alpha_, beta_, gamma_, delta_, epsilon_, zeta_, eta_, theta_, iota_,
                kappa_, lambda_, mu_, nu_, xi_, omicron_, pi_, rho_, sigma_, tau_, upsilon_, phi_, chi_, psi_, omega_};

        public long hash64(final boolean[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * (data[i] ? 0x9E3779B97F4A7C15L : 0x789ABCDEFEDCBA98L)) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }


        public long hash64(final byte[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final short[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final char[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final int[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final long[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }


        public long hash64(final float[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.floatToIntBits(data[i])) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final double[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.doubleToMixedIntBits(data[i])) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        /**
         * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @return a 64-bit hash code for the requested section of data
         */
        public long hash64(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = start; i < end && i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        /**
         * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
         * moving between chars in increments of step (which is always greater than 0).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @param step  how many elements to advance after using one element from data; must be greater than 0
         * @return a 64-bit hash code for the requested section of data
         */
        public long hash64(final char[] data, final int start, final int end, final int step) {
            if (data == null || start >= end || step <= 0)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = start; i < end && i < len; i += step) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final CharSequence data) {
            if (data == null)
                return 0;
            final int len = data.length();
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data.charAt(i)) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final char[][] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i])) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final long[][] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i])) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final CharSequence[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i])) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final Iterable<? extends CharSequence> data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (CharSequence datum : data) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(datum)) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final CharSequence[]... data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i])) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final Object[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            Object o;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * ((o = data[i]) == null ? -1 : o.hashCode())) ^ $l2 * a + $l1;
            }
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }

        public long hash64(final Object data) {
            if (data == null)
                return 0;
            final long a = 0x632BE59BD9B4E019L ^ 0x8329C6EB9E6AD3E3L * data.hashCode(),
                    result = 0x9E3779B97F4A7C94L + $l2 + (a ^ $l2 * a + $l1);
            return result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37);
        }
        public int hash(final boolean[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * (data[i] ? 0x9E3779B97F4A7C15L : 0x789ABCDEFEDCBA98L)) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }


        public int hash(final byte[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }

        public int hash(final short[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }

        public int hash(final char[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }

        public int hash(final int[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }

        public int hash(final long[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }


        public int hash(final float[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.floatToIntBits(data[i])) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }

        public int hash(final double[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * NumberTools.doubleToMixedIntBits(data[i])) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }

        /**
         * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @return a 32-bit hash code for the requested section of data
         */
        public int hash(final char[] data, final int start, final int end) {
            if (data == null || start >= end)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = start; i < end && i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }
        /**
         * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
         * moving between chars in increments of step (which is always greater than 0).
         *
         * @param data  the char array to hash
         * @param start the start of the section to hash (inclusive)
         * @param end   the end of the section to hash (exclusive)
         * @param step  how many elements to advance after using one element from data; must be greater than 0
         * @return a 32-bit hash code for the requested section of data
         */
        public int hash(final char[] data, final int start, final int end, final int step) {
            if (data == null || start >= end || step <= 0)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = start; i < end && i < len; i += step) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data[i]) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }

        public int hash(final CharSequence data) {
            if (data == null)
                return 0;
            final int len = data.length();
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * data.charAt(i)) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }

        public int hash(final char[][] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i])) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }

        public int hash(final long[][] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i])) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }

        public int hash(final CharSequence[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i])) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }

        public int hash(final Iterable<? extends CharSequence> data) {
            if (data == null)
                return 0;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (CharSequence datum : data) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(datum)) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }

        public int hash(final CharSequence[]... data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * hash64(data[i])) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }

        public int hash(final Object[] data) {
            if (data == null)
                return 0;
            final int len = data.length;
            long result = 0x9E3779B97F4A7C94L + $l2, a = 0x632BE59BD9B4E019L;
            Object o;
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * ((o = data[i]) == null ? -1 : o.hashCode())) ^ $l2 * a + $l1;
            }
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }

        public int hash(final Object data) {
            if (data == null)
                return 0;
            final long a = 0x632BE59BD9B4E019L ^ 0x8329C6EB9E6AD3E3L * data.hashCode(),
                    result = 0x9E3779B97F4A7C94L + $l2 + (a ^ $l2 * a + $l1);
            return (int)(result * (a * $l1 | 1L) ^ (result >>> 27 | result << 37));
        }
    }
}
