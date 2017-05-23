package squidpony.store.util;

import squidpony.squidmath.CrossHash;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.PermutedRNG;

/**
 * Created by Tommy Ettinger on 5/22/2017.
 */
public final class Garbler {

    private Garbler()
    {
    }
    public static String garble(final String text) {
        return garble(text, "HOWARD PHILLIPS LOVECRAFT, EVERYBODY!");
    }

    public static String garble(final String text, final String keyText)
    {
        return garble(text, PermutedRNG.determine(CrossHash.Wisp.hash64(keyText) ^ 0x9E3779B97F4A7C15L));

    }
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
            cs[i] = (char) (c ^ (-33 & wiggle >>> 49 + 2 * (wiggle >>> 61)));
        }
        wiggle = rng.nextLong();
        cs[0] ^= (-33 & wiggle >>> 49 + 2 * (wiggle >>> 61));
        return String.valueOf(cs);
    }

    public static String degarble(final String garbled) {
        return degarble(garbled, "HOWARD PHILLIPS LOVECRAFT, EVERYBODY!");
    }

    public static String degarble(final String garbled, final String keyText)
    {
        return degarble(garbled, PermutedRNG.determine(CrossHash.Wisp.hash64(keyText) ^ 0x9E3779B97F4A7C15L));
    }

    public static String degarble(final String garbled, final long key)
    {
        final char[] cs = garbled.toCharArray();
        final int len = cs.length - 1;
        LightRNG rng = new LightRNG(key);
        long wiggle = rng.skip(len+1L);
        cs[0] ^= (-33 & wiggle >>> 49 + 2 * (wiggle >>> 61));
        for (int i = 1; i <= len; i++) {
            wiggle = rng.skip(-1L);
            int r = (int) (((i+1L) * (wiggle & 0x7FFFFFFFL)) >>> 31);
            if(i == r)
            {
                cs[i] ^= (-33 & wiggle >>> 49 + 2 * (wiggle >>> 61));
            }
            else {
                char c = cs[r];
                cs[r] = (char) (cs[i] ^ (-33 & wiggle >>> 49 + 2 * (wiggle >>> 61)));
                cs[i] = c;
            }
        }
        return String.valueOf(cs);
    }
}
