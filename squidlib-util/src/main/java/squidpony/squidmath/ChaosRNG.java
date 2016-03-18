package squidpony.squidmath;

import squidpony.annotation.GwtIncompatible;

import java.security.SecureRandom;

/**
 * An RNG that cannot be seeded and should be fairly hard to predict what it will return next. Useful for competitions
 * where a seeded RNG is used for dungeon generation and enemy placement but an unpredictable RNG is needed for combat,
 * so players can't abuse the RNG to make improbable events guaranteed or unfavorable outcomes impossible.
 * Created by Tommy Ettinger on 3/17/2016.
 */
@GwtIncompatible
public class ChaosRNG implements RandomnessSource{

    private long[] state = new long[16];
    private int choiceA, choiceB;
    SecureRandom sec;

    /**
     * Builds a ChaosRNG with a cryptographically-random seed. Future random generation uses less secure methods but
     * should still make it extremely difficult to "divine" the future RNG results.
     */
    public ChaosRNG()
    {
        sec = new SecureRandom();
        byte[] bytes = new byte[128];
        sec.nextBytes(bytes);
        for (int i = sec.nextInt(128), c = 0; c < 128; c++, i = (i + 1) % 128) {
            state[i & 15] |= bytes[c] << ((i >> 4) << 3);
        }
        choiceA = sec.nextInt();
        choiceB = choiceA & 15;
        choiceA = (choiceA >> (((choiceA >> 4) & 7) | 8)) & 15;
    }

    @Override
    public int next( int bits ) {
        return (int)( nextLong() & ( 1L << bits ) - 1 );
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     * @return any long, all 64 bits are random
     */
    @Override
    public long nextLong() {
        choiceA = ((choiceB ^ ((choiceA + 5) * (choiceB + 10)))) & 15;
        choiceB = (int)(state[choiceA] >> choiceB) & 15;
        long z = ( state[choiceA] += 0x9E3779B97F4A7C15L ), w = (state[choiceB] += 0x9E3779B97F4A7C15L);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        w = (w ^ (w >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        w = (w ^ (w >>> 27)) * 0x94D049BB133111EBL;
        z ^= (z >>> 31);
        return (w ^ (w >>> 31)) ^ z;
    }

    /**
     * Changes the internal state to a new, fully-random version that should have no relation to the previous state.
     * May be somewhat slow; you shouldn't need to call this often.
     */
    public void randomize()
    {
        byte[] bytes = new byte[128];
        sec.nextBytes(bytes);
        for (int i = sec.nextInt(128), c = 0; c < 128; c++, i = (i + 1) % 128) {
            state[i & 15] |= bytes[c] << ((i >> 4) << 3);
        }
        choiceA = sec.nextInt();
        choiceB = choiceA & 15;
        choiceA = (choiceA >> (((choiceA >> 4) & 7) | 8)) & 15;
    }
}
