package squidpony;

import squidpony.squidmath.CrossHash;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.PermutedRNG;

/**
 * Tools for garbling Strings (making them appear to be gibberish) and degarbling earlier outputs to get the original
 * inputs. This is like a weak form of encryption, and is probably enough to stop random users from editing saved files
 * effectively. This allows a key as a String or long to be used to affect the garbling process.
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
            cs[i] = (char) (c ^ (95 & wiggle >>> 33 + (wiggle >>> 60)));
        }
        wiggle = rng.nextLong();
        cs[0] ^= (95 & wiggle >>> 33 + (wiggle >>> 60));
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
        cs[0] ^= (95 & wiggle >>> 33 + (wiggle >>> 60));
        for (int i = 1; i <= len; i++) {
            wiggle = rng.skip(-1L);
            int r = (int) (((i+1L) * (wiggle & 0x7FFFFFFFL)) >>> 31);
            if(i == r)
            {
                cs[i] ^= (95 & wiggle >>> 33 + (wiggle >>> 60));
            }
            else {
                char c = cs[r];
                cs[r] = (char) (cs[i] ^ (95 & wiggle >>> 33 + (wiggle >>> 60)));
                cs[i] = c;
            }
        }
        return String.valueOf(cs);
    }
}
