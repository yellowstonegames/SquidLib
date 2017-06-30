package squidpony;

import squidpony.squidmath.CrossHash;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.PermutedRNG;

/**
 * Tools for garbling Strings (making them appear to be gibberish) and degarbling earlier outputs to get the original
 * inputs. This is like a weak form of encryption, and is probably enough to stop random users from editing saved files
 * effectively. This allows a key as a String or long to be used to affect the garbling process. There are also garble32
 * and degarble32 methods meant for use on GWT and 32-bit target platforms; these methods don't need 64-bit math but
 * produce incompatible output when compared to the regular garble and degarble methods. A minor step of obfuscation
 * could be to run some combination of garble and/or garble32 with different keys and then require they be degarbled by
 * degarble and/or degarble32 calls (with the same keys as before) in the reverse order of the garble/garble32 calls.
 * A more major step of obfuscation would be to run any garbling on already-compressed text, as code in the
 * squidlib-extra module can do (or anything using a string compression library, like the
 * <a href="https://github.com/tommyettinger/BlazingChain">BlazingChain</a> library that squidlib-extra uses).
 * <br>
 * Created by Tommy Ettinger on 5/22/2017.
 */
public final class Garbler {

    private Garbler()
    {
    }

    /**
     * Garbles text with the default key. This can be degarbled with {@link #degarble(String)}, which also uses the
     * default key.
     * @param text the text to garble
     * @return a new String that appears unrelated to text and should look like gibberish
     */
    public static String garble(final String text) {
        return garble(text, "HOWARD PHILLIPS LOVECRAFT, EVERYBODY!");
    }
    /**
     * Garbles text with the given keyText. This can be degarbled with {@link #degarble(String, String)}, which must be
     * given the same keyText.
     * @param text the text to garble
     * @param keyText used to determine the key this will use to garble text
     * @return a new String that appears unrelated to text and should look like gibberish
     */
    public static String garble(final String text, final String keyText)
    {
        return garble(text, PermutedRNG.determine(CrossHash.Wisp.hash64(keyText) ^ 0x9E3779B97F4A7C15L));

    }
    /**
     * Garbles text with the given key as a long. This can be degarbled with {@link #degarble(String, long)}, which must
     * be given the same key.
     * @param text the text to garble
     * @param key the key this will use to garble text
     * @return a new String that appears unrelated to text and should look like gibberish
     */
    public static String garble(final String text, final long key)
    {
        final char[] cs = text.toCharArray();
        final int len = cs.length;
        LightRNG rng = new LightRNG(key);
        long wiggle;
        for (int i = len - 1; i > 0; i--) {
            wiggle = rng.nextLong();
            int r = (int) (((i+1L) * (wiggle & 0x7FFFFFFFL)) >>> 31);
            char c = cs[r];
            cs[r] = cs[i];
            cs[i] = (char) (c ^ (31 & wiggle >>> 33 + (wiggle >>> 60)));
        }
        wiggle = rng.nextLong();
        cs[0] ^= (31 & wiggle >>> 33 + (wiggle >>> 60));
        return String.valueOf(cs);
    }

    /**
     * Given a garbled String that was produced by {@link #garble(String)} (using the default key), this reverses the
     * garbling and gets the original String.
     * @param garbled a String produced by a garble() method using the default key
     * @return the original String before garbling, if the keys match
     */
    public static String degarble(final String garbled) {
        return degarble(garbled, "HOWARD PHILLIPS LOVECRAFT, EVERYBODY!");
    }
    /**
     * Given a garbled String that was produced by {@link #garble(String, String)} (using the given keyText), this
     * reverses the garbling and gets the original String.
     * @param garbled a String produced by a garble() method using the same keyText
     * @param keyText the keyText that was used during garbling
     * @return the original String before garbling, if the keys match
     */
    public static String degarble(final String garbled, final String keyText)
    {
        return degarble(garbled, PermutedRNG.determine(CrossHash.Wisp.hash64(keyText) ^ 0x9E3779B97F4A7C15L));
    }

    /**
     * Given a garbled String that was produced by {@link #garble(String, long)} (using the given key), this reverses
     * the garbling and gets the original String.
     * @param garbled a String produced by a garble() method using the same keyText
     * @param key the key that was used during garbling
     * @return the original String before garbling, if the keys match
     */
    public static String degarble(final String garbled, final long key)
    {
        final char[] cs = garbled.toCharArray();
        final int len = cs.length - 1;
        LightRNG rng = new LightRNG(key);
        long wiggle = rng.skip(len+1L);
        cs[0] ^= (31 & wiggle >>> 33 + (wiggle >>> 60));
        for (int i = 1; i <= len; i++) {
            wiggle = rng.skip(-1L);
            int r = (int) (((i+1L) * (wiggle & 0x7FFFFFFFL)) >>> 31);
            if(i == r)
            {
                cs[i] ^= (31 & wiggle >>> 33 + (wiggle >>> 60));
            }
            else {
                char c = cs[r];
                cs[r] = (char) (cs[i] ^ (31 & wiggle >>> 33 + (wiggle >>> 60)));
                cs[i] = c;
            }
        }
        return String.valueOf(cs);
    }

    /**
     * Call this with {@code splitMix32(z += 0x9E3779B9)}, where z is an int to use as state.
     * 0x9E3779B9 can be changed for any odd int if the same number is used across calls.
     * @param z int, must be changed with each call; {@code splitMix32(z += 0x9E3779B9)} is recommended
     * @return a pseudo-random int
     */
    private static int splitMix32(int z) {
        z = (z ^ (z >>> 16)) * 0x85EBCA6B;
        z = (z ^ (z >>> 13)) * 0xC2B2AE35;
        return z ^ (z >>> 16);
    }


    /**
     * Garbles text with the default key. This can be degarbled with {@link #degarble32(String)}, which also uses the
     * default key.
     * @param text the text to garble
     * @return a new String that appears unrelated to text and should look like gibberish
     */
    public static String garble32(final String text) {
        return garble32(text, "HOWARD PHILLIPS LOVECRAFT, EVERYBODY!");
    }

    /**
     * Garbles text with the given keyText. This can be degarbled with {@link #degarble32(String, String)}, which must be
     * given the same keyText.
     * @param text the text to garble
     * @param keyText used to determine the key this will use to garble text
     * @return a new String that appears unrelated to text and should look like gibberish
     */
    public static String garble32(final String text, final String keyText)
    {
        return garble32(text,CrossHash.Wisp.hash(keyText) ^ 0x7F4A7C15);
    }

    /**
     * Garbles text with the given key as an int. This can be degarbled with {@link #degarble32(String, int)}, which must
     * be given the same key.
     * @param text the text to garble
     * @param key the key this will use to garble text
     * @return a new String that appears unrelated to text and should look like gibberish
     */
    public static String garble32(final String text, final int key)
    {
        final char[] cs = text.toCharArray();
        final int len = cs.length;
        int state = splitMix32(key);
        final int increment = splitMix32(key * key ^ 0x9E3779B9 + ~key) | 1;
        int wiggle;
        for (int i = len - 1; i > 0; i--) {
            wiggle = splitMix32(state += increment);
            int r = (wiggle & 0x7FFFFFFF) % (i + 1);
            char c = cs[r];
            cs[r] = cs[i];
            cs[i] = (char) (c ^ (wiggle + state) >>> 27);
        }
        wiggle = splitMix32(state += increment);
        cs[0] ^= (wiggle + state) >>> 27;
        return String.valueOf(cs);
    }

    /**
     * Given a garbled String that was produced by {@link #garble32(String)} (using the default key), this reverses the
     * garbling and gets the original String.
     * @param garbled a String produced by a garble() method using the default key
     * @return the original String before garbling, if the keys match
     */
    public static String degarble32(final String garbled) {
        return degarble32(garbled, "HOWARD PHILLIPS LOVECRAFT, EVERYBODY!");
    }
    /**
     * Given a garbled String that was produced by {@link #garble32(String, String)} (using the given keyText), this
     * reverses the garbling and gets the original String.
     * @param garbled a String produced by a garble() method using the same keyText
     * @param keyText the keyText that was used during garbling
     * @return the original String before garbling, if the keys match
     */
    public static String degarble32(final String garbled, final String keyText)
    {
        return degarble32(garbled,CrossHash.Wisp.hash(keyText) ^ 0x7F4A7C15);
    }

    /**
     * Given a garbled String that was produced by {@link #garble32(String, int)} (using the given key), this reverses
     * the garbling and gets the original String.
     * @param garbled a String produced by a garble() method using the same keyText
     * @param key the key that was used during garbling
     * @return the original String before garbling, if the keys match
     */
    public static String degarble32(final String garbled, final int key)
    {
        final char[] cs = garbled.toCharArray();
        final int len = cs.length - 1;
        int state = splitMix32(key);
        final int increment = splitMix32(key * key ^ 0x9E3779B9 + ~key) | 1;
        int wiggle = splitMix32(state += increment * (len+1));
        cs[0] ^= (wiggle + state) >>> 27;
        for (int i = 1; i <= len; i++) {
            wiggle = splitMix32(state -= increment);
            int r = (wiggle & 0x7FFFFFFF) % (i + 1);
            if(i == r)
            {
                cs[i] ^= (wiggle + state) >>> 27;
            }
            else {
                char c = cs[r];
                cs[r] = (char) (cs[i] ^ (wiggle + state) >>> 27);
                cs[i] = c;
            }
        }
        return String.valueOf(cs);
    }

}
