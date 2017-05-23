package squidpony.store.util;

import squidpony.squidmath.CrossHash;
import squidpony.squidmath.LightRNG;

/**
 * Created by Tommy Ettinger on 5/22/2017.
 */
public final class Garbler {

    private final LightRNG rng = new LightRNG();
    private transient final long $;

    public Garbler()
    {
        $ = CrossHash.Wisp.hash64("West of Arkham the hills rise wild, and there are valleys with deep woods that " +
                "no axe has ever cut. There are dark narrow glens where the trees slope fantastically, and where " +
                "thin brooklets trickle without ever having caught the glint of sunlight. On the gentler slopes " +
                "there are farms, ancient and rocky, with squat, moss-coated cottages brooding eternally over old " +
                "New England secrets in the lee of great ledges; but these are all vacant now, the wide chimneys " +
                "crumbling and the shingled sides bulging perilously beneath low gambrel roofs. " +
                "THE LATE GREAT HOWARD PHILLIPS LOVECRAFT!");
        rng.setState($);
    }
    public Garbler(final String seedText)
    {
        $ = CrossHash.Wisp.hash64(seedText + "!!!!!!!!!!!!!!!!???!!!!!...!");
        rng.setState($);
    }
    private int nextInt(final int bound) {
        return (int) ((bound * (rng.nextLong() & 0x7FFFFFFFL)) >> 31);
    }
    private int nextIntReverse(final int bound) {
        return (int) ((bound * (rng.skip(-1L) & 0x7FFFFFFFL)) >> 31);
    }

    public final String garble(final String text)
    {
        final char[] cs = text.toCharArray();
        final int len = cs.length;
        rng.setState($);
        for (int i = len - 1; i > 0; i--) {
            long wiggle = rng.nextLong();
            int r = (int) (((i+1L) * (wiggle & 0x7FFFFFFFL)) >>> 31);
            char c = cs[r];
            cs[r] = cs[i];
            cs[i] = (char) (c ^ (wiggle >>> 60));
        }
        cs[0] ^= rng.nextLong() >>> 60;
        return String.valueOf(cs);
    }
    public final String degarble(final String garbled)
    {
        final char[] cs = garbled.toCharArray();
        final int len = cs.length - 1;
        rng.setState($);
        rng.skip(len+2L);
        cs[0] ^= rng.skip(-1L) >>> 60;
        for (int i = 1; i <= len; i++) {
            long wiggle = rng.skip(-1L);
            int r = (int) (((i+1L) * (wiggle & 0x7FFFFFFFL)) >>> 31);
            if(i == r)
            {
                cs[i] ^= (wiggle >>> 60);
            }
            else {
                char c = cs[r];
                cs[r] = (char) (cs[i] ^ (wiggle >>> 60));
                cs[i] = c;
            }
        }
        return String.valueOf(cs);
    }
}
