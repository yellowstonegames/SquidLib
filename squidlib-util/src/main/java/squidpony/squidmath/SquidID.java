package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A UUID-like identifier; not compatible with Java's standard UUID but will work on GWT.
 * <br>
 * Meant to be used as an identity type for things like SpatialMap, especially when no special game-specific logic is
 * needed for identities.
 * <br>
 * Changed on April 27, 2018 so it isn't possible to generate two identical SquidIDs until 2 to the 128 minus 1
 * SquidIDs have been generated, which will take a while. Before, there was a small possibility that even two sequential
 * SquidIDs could be the same, and the new way gives a better guarantee of how many can be produced without duplicates.
 * Changed again on December 7, 2018 so the class can be automatically serialized by libGDX's Json class and other ways
 * of reflection on GWT. Now this uses 4 ints instead of 2 longs, since libGDX reflection couldn't be used to serialize
 * the long fields this used before. The random number generator has the same guarantee of 2 to the 128 minus 1 IDs, but
 * uses a different algorithm, and it can be restarted now using {@link #store()} and {@link #load(CharSequence)}.
 * <br>
 * Created by Tommy Ettinger on 4/30/2016.
 */
public class SquidID implements Serializable, Comparable<SquidID> {
    private static final long serialVersionUID = 8946534790126874460L;
    private static XoshiroStarPhi32RNG rng = new XoshiroStarPhi32RNG();
    public final int a, b, c, d;

    /**
     * Constructs a new random SquidID. If you want different random IDs with every run, the defaults should be fine.
     * If you want stable IDs to be generated, use SquidID.stabilize(), but be careful about collisions!
     */
    public SquidID() {
        a = rng.getStateA();
        b = rng.getStateB();
        c = rng.getStateC();
        d = rng.getStateD();
        rng.nextInt();

    }

    /**
     * Constructs a fixed SquidID with the given four 32-bit ints, which will be used exactly.
     * @param a the least-significant bits of the ID
     * @param b the second-to-least-significant bits of the ID
     * @param c the second-to-most-significant bits of the ID
     * @param d the most-significant bits of the ID
     */
    public SquidID(int a, int b, int c, int d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    /**
     * Constructs a fixed SquidID with the given low and high 64-bit longs.
     * @param low the least-significant bits of the ID
     * @param high the most-significant bits of the ID
     */
    public SquidID(long low, long high) {
        a = (int)(low & 0xFFFFFFFFL);
        b = (int)(low >>> 32);
        c = (int)(high & 0xFFFFFFFFL);
        d = (int)(high >>> 32);
    }

    /**
     * Gets a new random SquidID, the same as calling the no-argument constructor.
     * The name is for compatibility with Java's standard UUID class.
     * @return a newly-constructed random SquidID.
     */
    public static SquidID randomUUID()
    {
        return new SquidID();
    }

    /**
     * Makes the IDs generated after calling this repeatable, with the same IDs generated in order after this is called.
     * This class uses a random number generator with a random seed by default to produce IDs, and properties of the
     * {@link XoshiroStarPhi32RNG} this uses make it incredibly unlikely that IDs will repeat even if the game was run
     * for years without stopping. For the purposes of tests, you may want stable SquidID values to be generated, the
     * same for every startup of the program, generating the same IDs in order. This will change the seed used
     * internally to a constant (large) seed the first time it is called, and it should only be called at or near the
     * start of your program, no more than once. If an ID is requested immediately after calling this method, and then
     * this method is called again, the next ID to be generated will be identical to the previous one generated (a
     * collision). There may be reasons you want this during testing, so there isn't any check for multiple calls to
     * this method. If IDs can persist between runs of the game (i.e. saved in a file), using this is generally a bad
     * idea, and you should instead use either random IDs or save the state with.
     * <br>
     * You can "undo" the effects of this method with randomize(), changing the seed to a new random value.
     * <br>
     * Because IDs aren't likely to have gameplay significance, this uses one seed, and is equivalent to calling
     * {@code SquidID.load("C13FA9A902A6328F91E10DA5C79E7B1D")}, which are values based on the Plastic Constant (2 to
     * the 64 divided by the plastic constant, upper 32 bits and then lower 32 bits, then the upper and lower bits of
     * that number divided again by the plastic constant). Irrational numbers like the plastic constant generally have a
     * good distribution of bits, which should help delay the point when the generator hits "zeroland" and produces
     * multiple small numbers for a short while.
     */
    public static void stabilize()
    {
        rng.setState(0xC13FA9A9, 0x02A6328F, 0x91E10DA5, 0xC79E7B1D);
    }

    public static StringBuilder store()
    {
        final StringBuilder sb = new StringBuilder(32);
        StringKit.appendHex(sb, rng.getStateA());
        StringKit.appendHex(sb, rng.getStateB());
        StringKit.appendHex(sb, rng.getStateC());
        return StringKit.appendHex(sb, rng.getStateD());
    }
    /**
     * Makes the IDs generated after calling this repeatable, with the same IDs generated in order after this is called.
     * This class uses a random number generator with a random seed by default to produce IDs, and properties of the
     * {@link XoshiroStarPhi32RNG} this uses make it incredibly unlikely that IDs will repeat even if the game was run
     * for years without stopping. For the purposes of tests, you may want stable SquidID values to be generated, the
     * same for every startup of the program, generating the same IDs in order; you may also want this when loading a
     * saved game. This will change the seed used internally to match a value that should have been produced by
     * {@link #store()} but can be any 32 hex digits, and it should only be called at or near the start of your program,
     * no more than once per load of a save. If an ID is requested immediately after calling this method, and then this
     * method is called again with the same data parameter, the next ID to be generated will be identical to the
     * previous one generated (a collision). There may be reasons you want this during testing, so there isn't any check
     * for multiple calls to this method. If IDs can persist between runs of the game (i.e. saved in a file), you can be
     * fine with random IDs in almost all cases, or you can have more certainty by saving the last state of the
     * generator using {@link #store()} when saving and loading that state with this method later.
     * <br>
     * You can "undo" the effects of this method with randomize(), changing the seed to a new random value.
     */
    public static void load(CharSequence data)
    {
        rng.setState(StringKit.intFromHex(data, 0, 8), StringKit.intFromHex(data, 8, 16),
                StringKit.intFromHex(data, 16, 24), StringKit.intFromHex(data, 24, 32));
    }

    /**
     * Makes the IDs generated after calling this non-repeatable, with a random 128-bit seed.
     * This class uses a random number generator with a random seed by default to produce IDs, and properties of the
     * {@link XoshiroStarPhi32RNG} this uses make it incredibly unlikely that IDs will repeat even if the game was run
     * for years without stopping. However, if you call stabilize(), generate some IDs, call stabilize() again, and
     * generate some  more IDs, the first, second, third, etc. IDs generated after each call will be identical -- hardly
     * the unique ID you usually want. You can "undo" the effects of stabilize by calling this method, making the seed a
     * new random value. This does not affect the constructor that takes two longs to produce an exact ID, nor will it
     * change any existing IDs.
     */
    public static void randomize()
    {
        rng.setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000),
                (int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    /**
     * Gets the least-significant bits, also accessible by the field low.
     * The name is for compatibility with Java's standard UUID class.
     * @return the least-significant bits as a long
     */
    public long getLeastSignificantBits()
    {
        return (long)b << 32 | (a & 0xFFFFFFFFL);
    }

    /**
     * Gets the most-significant bits, also accessible by the field high.
     * The name is for compatibility with Java's standard UUID class.
     * @return the most-significant bits as a long
     */
    public long getMostSignificantBits()
    {
        return (long)d << 32 | (c & 0xFFFFFFFFL);
    }

    /**
     * Gets the 32 least-significant bits as an int.
     * @return an int with the 32 least-significant bits of this ID
     */
    public int getA() {
        return a;
    }

    /**
     * Gets the 32 second-to-least-significant bits as an int.
     * @return an int with the 32 second-to-least-significant bits of this ID
     */
    public int getB() {
        return b;
    }
    /**
     * Gets the 32 second-to-most-significant bits as an int.
     * @return an int with the 32 second-to-most-significant bits of this ID
     */
    public int getC() {
        return c;
    }
    /**
     * Gets the 32 most-significant bits as an int.
     * @return an int with the 32 most-significant bits of this ID
     */
    public int getD() {
        return d;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SquidID squidID = (SquidID) o;

        return a == squidID.a && b == squidID.b && c == squidID.c && d == squidID.d;

    }

    @Override
    public int hashCode() {
        return 31 * 31 * 31 * a +
                31 * 31 * b +
                31 * c +
                d;
    }

    @Override
    public String toString()
    {
        return StringKit.hex(d) + StringKit.hex(c) + '-' + StringKit.hex(b) + StringKit.hex(a);
    }

    @Override
    public int compareTo(SquidID o) {
        if(o == null)
            return 1;
        int diff = d - o.d;
        if(diff == 0)
            diff = c - o.c;
        if(diff == 0)
            diff = b - o.b;
        if(diff == 0)
            diff = a - o.a;
        return diff > 0 ? 1 : diff < 0 ? -1 : 0;
    }
}
