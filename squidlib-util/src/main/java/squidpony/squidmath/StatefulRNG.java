package squidpony.squidmath;

import java.io.Serializable;

/**
 * A slight variant on RNG that always uses a stateful RandomessSource and so can have its state
 * set or retrieved using setState() or getState().
 * Created by Tommy Ettinger on 9/15/2015.
 * @author Tommy Ettinger
 */
public class StatefulRNG extends RNG implements Serializable, IRNG {

	private static final long serialVersionUID = -2456306898212937163L;

	public StatefulRNG() {
        super();
    }

    public StatefulRNG(RandomnessSource random) {
        super((random instanceof StatefulRandomness) ? random : new LightRNG(random.nextLong()));
    }

    /**
     * Seeded constructor uses LightRNG, which is of high quality, but low period (which rarely matters for games),
     * and has good speed and tiny state size.
     */
    public StatefulRNG(long seed) {
        this(new LightRNG(seed));
    }
    /**
     * String-seeded constructor uses the hash of the String as a seed for LightRNG, which is of high quality, but
     * low period (which rarely matters for games), and has good speed and tiny state size.
     *
     * Note: This constructor changed behavior on April 22, 2017, and again on December 23, 2017. The first was when it
     * was noticed that it was not seeding very effectively (only assigning to 32 bits of seed instead of all 64). The
     * older behavior isn't fully preserved, since it used a rather low-quality String hashing algorithm and so probably
     * had problems producing good starting seeds, but you can get close by replacing {@code new StatefulRNG(text)} with
     * {@code new StatefulRNG(new LightRNG(CrossHash.hash(text)))}. The new technique assigns to all 64 bits and has
     * less correlation between similar inputs causing similar starting states. It's also faster, but that shouldn't
     * matter in a constructor. It uses a better hashing algorithm because CrossHash no longer has the older, worse one.
     */
    public StatefulRNG(CharSequence seedString) {
        this(new LightRNG(CrossHash.hash64(seedString)));
    }

    @Override
    public void setRandomness(RandomnessSource random) {
        super.setRandomness(random == null ? new LightRNG() :
                (random instanceof StatefulRandomness) ? random : new LightRNG(random.nextLong()));
    }

    /**
     * Creates a copy of this StatefulRNG; it will generate the same random numbers, given the same calls in order, as
     * this StatefulRNG at the point copy() is called. The copy will not share references with this StatefulRNG.
     *
     * @return a copy of this StatefulRNG
     */
    @Override
    public StatefulRNG copy() {
        return new StatefulRNG(random.copy());
    }

    /**
     * Get a long that can be used to reproduce the sequence of random numbers this object will generate starting now.
     * @return a long that can be used as state.
     */
    public long getState()
    {
        return ((StatefulRandomness)random).getState();
    }

    /**
     * Sets the state of the random number generator to a given long, which will alter future random numbers this
     * produces based on the state.
     * @param state a long, which typically should not be 0 (some implementations may tolerate a state of 0, however).
     */
    public void setState(long state)
    {
        ((StatefulRandomness)random).setState(state);
    }

    @Override
    public String toString() {
        return "StatefulRNG{" + Long.toHexString(((StatefulRandomness)random).getState()) + "}";
    }

}
