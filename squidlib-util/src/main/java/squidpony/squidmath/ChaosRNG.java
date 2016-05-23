package squidpony.squidmath;

import squidpony.annotation.GwtIncompatible;

import java.security.SecureRandom;

/**
 * An RNG that cannot be seeded and should be fairly hard to predict what it will return next. Useful for competitions
 * where a seeded RNG is used for dungeon generation and enemy placement but an unpredictable RNG is needed for combat,
 * so players can't abuse the RNG to make improbable events guaranteed or unfavorable outcomes impossible. The
 * performance of this as a RandomnessSource is also fairly good, taking approximately 1.5x to 1.7x as long as LightRNG
 * to produce random 64-bit data, and of course it is far faster than java.util.Random (which is 10x slower than this).
 * In the secure random numbers category, where this isn't quite as secure as most, ChaosRNG is about 80x faster than
 * SecureRandom once SecureRandom warms up, which takes about 10 minutes of continuous number generation. Before that,
 * ChaosRNG is about 110x faster than SecureRandom for 64-bit data.
 * <br>
 * This is intended to be used as a RandomnessSource for an RNG, and does not have any methods other than those needed
 * for that interface, with one exception -- the randomize() method, which can be used to completely change all 1024
 * bits of state using cryptographic random numbers. If you create a ChaosRNG and keep it around for later, then you can
 * pass it to the RNG constructor and later call randomize() on the ChaosRNG if you suspect it may be becoming
 * predictable. The period on this RNG is (2 to the 1024) - 1, so predicting it may be essentially impossible unless the
 * user can poke around in the application, use reflection, etc.
 * Created by Tommy Ettinger on 3/17/2016.
 */
@GwtIncompatible
public class ChaosRNG implements RandomnessSource{

    private long[] state = new long[16];
    private int choice;
    private SecureRandom sec;
    private static final long serialVersionUID = -254415589291474491L;

    /**
     * Builds a ChaosRNG with a cryptographically-random seed. Future random generation uses less secure methods but
     * should still make it extremely difficult to "divine" the future RNG results.
     */
    public ChaosRNG()
    {
        sec = new SecureRandom();
        byte[] bytes = new byte[128];
        sec.nextBytes(bytes);
        for (int i = sec.nextInt() & 127, c = 0; c < 128; c++, i = i + 1 & 127) {
            state[i & 15] |= bytes[c] << ((i >> 4) << 3);
        }
        choice = sec.nextInt(16);
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
        final long s0 = state[choice];
        long s1 = state[choice = (choice + 1) & 15];
        s1 ^= s1 << 31; // a
        state[choice] = s1 ^ s0 ^ (s1 >> 11) ^ (s0 >> 30); // b,c
        return state[choice] * 1181783497276652981L;
    }

    /**
     * Produces another ChaosRNG with no relation to this one; this breaks the normal rules that RandomnessSource.copy
     * abides by because this class should never have its generated number sequence be predictable.
     * @return a new, unrelated ChaosRNG as a RandomnessSource
     */
    @Override
    public RandomnessSource copy() {
        return new ChaosRNG();
    }

    /**
     * Changes the internal state to a new, fully-random version that should have no relation to the previous state.
     * May be somewhat slow; you shouldn't need to call this often.
     */
    public void randomize()
    {
        byte[] bytes = sec.generateSeed(128);
        for (int i = sec.nextInt() & 127, c = 0; c < 128; c++, i = i + 1 & 127) {
            state[i & 15] |= bytes[c] << ((i >> 4) << 3);
        }
        choice = sec.nextInt(16);
    }

    @Override
    public String toString() {
        return "ChaosRNG with state determined by the power of friendship";
    }
}
