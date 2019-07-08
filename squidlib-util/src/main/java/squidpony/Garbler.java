package squidpony;

import squidpony.squidmath.CrossHash;
import squidpony.squidmath.CrossHash.Mist;

/**
 * Tools for garbling Strings (making them appear to be gibberish) and degarbling earlier outputs to get the original
 * inputs. This is like a weak form of encryption, and is probably enough to stop random users from editing saved files
 * effectively. This allows a key as a String, long, or long array to be used to affect the garbling process.
 * <br>
 * A minor step of obfuscation could be to run some combination of garble calls with different keys and then require
 * they be degarbled by degarble calls (with the same keys as before) in the reverse order of the garble calls.
 * This is made more efficient with the {@link #garble(String, long[])} and {@link #degarble(String, long[])} methods,
 * which avoid allocating multiple temporary char arrays when multiple keys are used. A more major step of obfuscation
 * would be to run any garbling on already-compressed text, which {@link LZSPlus} does by using this class.
 * <br>
 * Created by Tommy Ettinger on 5/22/2017.
 */
public final class Garbler {

    private Garbler()
    {
    }
    
    /**
     * Get a long from this with {@code splitMix64(z += 0x9E3779B97F4A7C15L)}, where z is a long to use as state.
     * 0x9E3779B97F4A7C15L can be changed for any odd long if the same number is used across calls.
     * @param state long, must be changed with each call; {@code splitMix64(z += 0x9E3779B97F4A7C15L)} is recommended
     * @return a pseudo-random long; all are possible
     */
    private static long splitMix64(long state) {
        state = ((state >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        return state ^ (state >>> 31);
    }

    /**
     * ThrustAltRNG.determine() in a stable form. Expects state to change by 1.
     * @param state should change by 1
     * @return a pseudo-random long; only about 2/3 of all 64-bit values are possible
     */
    private static long ta(long state) {
        return (state = ((state *= 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 23);
    }

    /**
     * Mizuchi.nextLong() in a stable form. 
     * Expects to be called with with {@code mizuchi(state = state * 0x369DEA0F31A53F85L + stream)}
     * @param state should change with an LCG
     * @return a pseudo-random long; all are possible
     */
    private static long mizuchi(long state)
    {
        return (state = (state ^ state >>> 23 ^ state >>> 47) * 0xAEF17502108EF2D9L) ^ state >>> 25;
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
        return garble(text, CrossHash.Water.hash64(keyText) ^ 0x7F4A7C15L);
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
        long state = ta(key);
        final long increment = ta(state) | 1;
        long wiggle;
        for (int i = len - 1; i > 0; i--) {
            wiggle = splitMix64(state += increment);
            int r = (int) (((i+1L) * (wiggle & 0x7FFFFFFFL)) >>> 31);;
            char c = cs[r];
            cs[r] = cs[i];
            cs[i] = (char) (c ^ wiggle >>> 59);
        }
        wiggle = splitMix64(state + increment);
        cs[0] ^= wiggle >>> 59;
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
        return degarble(garbled,CrossHash.Water.hash64(keyText) ^ 0x7F4A7C15L);
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
        long state = ta(key);
        final long increment = ta(state) | 1;
        long wiggle = splitMix64(state += increment * (len+1));
        cs[0] ^= wiggle >>> 59;
        for (int i = 1; i <= len; i++) {
            wiggle = splitMix64(state -= increment);
            int r = (int) (((i+1L) * (wiggle & 0x7FFFFFFFL)) >>> 31);
            if(i == r)
            {
                cs[i] ^= wiggle >>> 59;
            }
            else {
                char c = cs[r];
                cs[r] = (char) (cs[i] ^ wiggle >>> 59);
                cs[i] = c;
            }
        }
        return String.valueOf(cs);
    }

    /**
     * Garbles text with the given keys as a long array, effectively garbling the same text one time per item in keys.
     * This can seen as a way to improve the quality of the shuffle by adding more bits of state to the key(s).
     * The result can be degarbled with {@link #degarble(String, long[])}, which must be given the same keys. This
     * method is more efficient than calling garble() repeatedly because it only allocates one temporary char array
     * for the whole batch of keys, as opposed to needing one temporary array per key with repeated calls.
     * @param text the text to garble
     * @param keys the key array this will use to garble text, as a long array
     * @return a new String that appears unrelated to text and should look like gibberish
     */
    public static String garble(final String text, final long[] keys)
    {
        if(keys == null)
            return garble(text);
        final char[] cs = text.toCharArray();
        final int len = cs.length;
        for (int k = 0; k < keys.length; k++) {
            final long key = keys[k];
            long state = ta(key);
            final long increment = ta(state) | 1;
            long wiggle;
            for (int i = len - 1; i > 0; i--) {
                wiggle = splitMix64(state += increment);
                int r = (int) (((i+1L) * (wiggle & 0x7FFFFFFFL)) >>> 31);
                char c = cs[r];
                cs[r] = cs[i];
                cs[i] = (char) (c ^ wiggle >>> 59);
            }
            wiggle = splitMix64(state + increment);
            cs[0] ^= wiggle >>> 59;
        }
        return String.valueOf(cs);
    }
    /**
     * Given a garbled String that was produced by {@link #garble(String, long[])} (using the given keys), this
     * reverses the garbling and gets the original String. This is not the same as calling degarble() repeatedly, in
     * part because this uses the keys in reverse order (just like every part of the degarbling process, it needs to be
     * in reverse), and in part because this only creates one temporary char array for the whole batch of keys, instead
     * of creating one new char array per repeated call.
     * @param garbled a String produced by a garble() method using the same keyText
     * @param keys the key array that was used during garbling
     * @return the original String before garbling, if the keys match
     */
    public static String degarble(final String garbled, final long[] keys)
    {
        if(keys == null)
            return degarble(garbled);
        final char[] cs = garbled.toCharArray();
        final int len = cs.length - 1;
        for (int k = keys.length - 1; k >= 0; k--) {
            final long key = keys[k];
            long state = ta(key);
            final long increment = ta(state) | 1;
            long wiggle = splitMix64(state += increment * (len + 1));
            cs[0] ^= wiggle >>> 59;
            for (int i = 1; i <= len; i++) {
                wiggle = splitMix64(state -= increment);
                int r = (int) (((i+1L) * (wiggle & 0x7FFFFFFFL)) >>> 31);
                if (i == r) {
                    cs[i] ^= wiggle >>> 59;
                } else {
                    char c = cs[r];
                    cs[r] = (char) (cs[i] ^ wiggle >>> 59);
                    cs[i] = c;
                }
            }
        }
        return String.valueOf(cs);
    }

    /**
     * If you need to produce an long array as a key for {@link #garble(String, long[])} when you only have a String,
     * you can use this method if the String isn't too small (at least 8 char Strings should be fine). This produces a
     * diverse array of longs without the correlation between items that you would get if you just generated a sequence
     * of random longs from one small seed, by using multiple different {@link Mist} objects to hash the text.
     * @param size the size of the key array to produce; larger key arrays take proportionately longer to process
     * @param keyText the String to use as a basis for generating random-seeming numbers for keys
     * @return a long array that can be given to {@link #garble(String, long[])} and {@link #degarble(String, long[])}
     */
    public static long[] makeKeyArray(final int size, final String keyText)
    {
        if(size <= 1) return new long[]{Mist.predefined[keyText.length() & 31].hash64(keyText)};
        long[] keys = new long[size];
        long ctr = keyText.length() * 181L + 0xB9A2842FL;
        for (int i = 0; i < size; i++) {
            ctr += (keys[i] = Mist.predefined[(int)splitMix64(ctr) & 31].hash64(keyText)) + 0xB9A2842FL;
            keys[i] ^= ta(ctr);
        }
        return keys;
    }
}
