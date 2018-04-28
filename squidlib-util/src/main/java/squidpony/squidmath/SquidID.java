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
 * <br>
 * Created by Tommy Ettinger on 4/30/2016.
 */
public class SquidID implements Serializable, Comparable<SquidID> {
    private static final long serialVersionUID = 8946534790126874460L;
    private static XoRoRNG rng = new XoRoRNG();
    public final long low, high;

    /**
     * Constructs a new random SquidID. If you want different random IDs with every run, the defaults should be fine.
     * If you want stable IDs to be generated, use SquidID.stabilize(), but be careful about collisions!
     */
    public SquidID() {
        rng.nextLong();
        low = rng.getStateA();
        high = rng.getStateB();
    }

    /**
     * Constructs a fixed SquidID with the given low and high 64-bit longs.
     * @param low the least-significant bits of the ID
     * @param high the most-significant bits of the ID
     */
    public SquidID(long low, long high) {
        this.low = low;
        this.high = high;
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
     * {@link XoRoRNG} this uses make it incredibly unlikely that IDs will repeat even if the game was run for years
     * without stopping. For the purposes of tests, you may want stable SquidID values to be generated, the same for
     * every startup of the program, generating the same IDs in order. This will change the seed used internally to a
     * constant (large) seed the first time it is called, and it should only be called at or near the start of your
     * program, no more than once. If an ID is requested immediately after calling this method, and then this method is
     * called again, the next ID to be generated will be identical to the previous one generated (a collision). There
     * may be reasons you want this during testing, so there isn't any check for multiple calls to this method. If IDs
     * can persist between runs of the game (i.e. saved in a file), using this is generally a bad idea, and the default
     * random IDs should more than suffice.
     * <br>
     * You can "undo" the effects of this method with randomize(), changing the seed to a new random value.
     * <br>
     * Because IDs aren't likely to have gameplay significance, this uses one seed, the opening paragraph of The
     * Wonderful Wizard of Oz, by Frank L. Baum, which is in the public domain. Changing the seed is unlikely to change
     * the likelihood of collisions, which should be less likely than a tornado transporting you to Oz, as long as this
     * method is called at most once per program run.
     */
    public static void stabilize()
    {
        rng.setSeed(
                CrossHash.hash64("Dorothy lived in the midst of the great Kansas prairies, with Uncle Henry, who was a "+
                        "farmer, and Aunt Em, who was the farmer's wife. Their house was small, for the "+
                        "lumber to build it had to be carried by wagon many miles. There were four walls, "+
                        "a floor and a roof, which made one room; and this room contained a rusty looking "+
                        "cookstove, a cupboard for the dishes, a table, three or four chairs, and the beds."),
                CrossHash.hash64(" Uncle Henry and Aunt Em had a big bed in one corner, and Dorothy a little bed in "+
                        "another corner. There was no garret at all, and no cellar—except a small hole dug "+
                        "in the ground, called a cyclone cellar, where the family could go in case one of "+
                        "those great whirlwinds arose, mighty enough to crush any building in its path. It "+
                        "was reached by a trap door in the middle of the floor, from which a ladder led "+
                        "down into the small, dark hole."));
    }

    /**
     * Makes the IDs generated after calling this non-repeatable, with a random 128-bit seed.
     * This class uses a random number generator with a random seed by default to produce IDs, and properties of the
     * {@link XoRoRNG} this uses make it incredibly unlikely that IDs will repeat even if the game was run for years
     * without stopping. However, if you call stabilize(), generate some IDs, call stabilize() again, and generate some
     * more IDs, the first, second, third, etc. IDs generated after each call will be identical -- hardly the unique ID
     * you usually want. You can "undo" the effects of stabilize by calling this method, making the seed a new random
     * value. This does not affect the constructor that takes two longs to produce an exact ID, nor will it change any
     * existing IDs.
     */
    public static void randomize()
    {
        rng.setSeed((long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    /**
     * Gets the least-significant bits, also accessible by the field low.
     * The name is for compatibility with Java's standard UUID class.
     * @return the least-significant bits as a long
     */
    public long getLeastSignificantBits()
    {
        return low;
    }

    /**
     * Gets the most-significant bits, also accessible by the field high.
     * The name is for compatibility with Java's standard UUID class.
     * @return the most-significant bits as a long
     */
    public long getMostSignificantBits()
    {
        return high;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SquidID squidID = (SquidID) o;

        if (low != squidID.low) return false;
        return high == squidID.high;

    }

    @Override
    public int hashCode() {
        return (int) (31 * (low ^ (low >>> 32)) + (high ^ (high >>> 32)));
    }

    @Override
    public String toString()
    {
        return StringKit.hex(high) + '-' + StringKit.hex(low);
    }

    @Override
    public int compareTo(SquidID o) {
        if(o == null)
            return 1;
        long diff = high - o.high;
        if(diff == 0)
            diff = low - o.low;
        return diff > 0 ? 1 : diff < 0 ? -1 : 0;
    }
}
