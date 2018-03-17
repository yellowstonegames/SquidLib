package squidpony.squidmath;

import squidpony.StringKit;

/**
 * An RNG that cannot be seeded and should be fairly hard to predict what it will return next. Useful for competitions
 * where a seeded RNG is used for dungeon generation and enemy placement but an unpredictable RNG is needed for combat,
 * so players can't abuse the RNG to make improbable events guaranteed or unfavorable outcomes impossible.
 * <br>
 * This is intended to be used as a RandomnessSource for an RNG, and does not have any methods other than those needed
 * for that interface, with one exception -- the randomize() method, which can be used to completely change all (many)
 * bits of state using cryptographic random numbers. If you create a ChaosRNG and keep it around for later, then you can
 * pass it to the RNG constructor and later call randomize() on the ChaosRNG if you suspect it may be becoming
 * predictable. The period on this RNG is preposterously large, since it involves a pair of IsaacRNGs as well as other
 * random state, so predicting it may be essentially impossible unless the user can poke around in the application, use
 * reflection, and so on.
 * Created by Tommy Ettinger on 3/17/2016.
 */
public class ChaosRNG implements RandomnessSource{

    private transient long[] z;
    private transient int choice;
    private transient IsaacRNG r0, r1;
    private static final long serialVersionUID = -254415589291474491L;

    private long determine()
    {
        long state = (z[(choice += 0xC6BC278D) >>> 28] += 0x9E3779B97F4A7C15L ^ choice * 0x2C9277B5000000L);
        state = (state ^ state >>> 26) * 0x2545F4914F6CDD1DL;
        return state ^ state >>> 28;

    }
    /**
     * Builds a ChaosRNG with a fairly-random seed derived from somewhat-OK sources of non-seed randomness, such as time
     * before and after garbage collection. We're forced to use sub-par techniques here due to GWT not supporting any
     * better methods. Future random generation uses less secure methods but should still make it extremely difficult to
     * "divine" the future RNG results from the outputs.
     */
    public ChaosRNG()
    {
        z = new long[16];
        // produces some garbage; this is intentional
        String s = StringKit.hexHash(System.currentTimeMillis(), System.identityHashCode(this), System.identityHashCode(z));
        s += StringKit.LATIN_LETTERS_LOWER;
        // sway causes most of the state of Math's Random field to be non-visible in the output
        s += NumberTools.sway((126.621 + Math.random()) * (17.71 - Math.random()) + (71.17 * Math.random()));
        // this should take some time because of the earlier garbage
        System.gc();
        // so this should have a better chance of being different.
        s += System.currentTimeMillis();
        r0 = new IsaacRNG(s);
        r1 = new IsaacRNG(r0.nextBlock());
        r1.fillBlock(z);
        choice = r0.next(32);
    }

    @Override
    public final int next( int bits ) {
        return (int)( nextLong() & ( 1L << bits ) - 1 );
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     * @return any long, all 64 bits are random
     */
    @Override
    public final long nextLong() {
        long rot = (determine() & 31) + 12;
        return r0.nextLong() << rot ^ r1.nextLong() >>> 45 - rot;
    }

    /**
     * Produces another ChaosRNG with no relation to this one; this breaks the normal rules that RandomnessSource.copy
     * abides by because this class should never have its generated number sequence be predictable.
     * @return a new, unrelated ChaosRNG as a RandomnessSource
     */
    @Override
    public ChaosRNG copy() {
        return new ChaosRNG();
    }

    /**
     * Changes the internal state to a new, fully-random version that should have no relation to the previous state.
     * May be somewhat slow; you shouldn't need to call this often.
     */
    public void randomize()
    {
        String s = System.currentTimeMillis() + "0" + System.identityHashCode(this);
        s += StringKit.LATIN_LETTERS_LOWER;
        System.gc();
        s += System.currentTimeMillis();
        r0.init(s);
        r1.init(r0.nextBlock());
        r1.fillBlock(z);
        choice = r0.next(32);
    }

    @Override
    public String toString() {
        return "ChaosRNG with hidden state (id is " + System.identityHashCode(this) + ')';
    }
}
