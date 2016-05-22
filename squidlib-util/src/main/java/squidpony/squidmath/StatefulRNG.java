package squidpony.squidmath;

/**
 * A slight variant on RNG that always uses a stateful RandomessSource and so can have its state
 * set or retrieved using setState() or getState().
 * Created by Tommy Ettinger on 9/15/2015.
 * @author Tommy Ettinger
 */
public class StatefulRNG extends RNG {

	private static final long serialVersionUID = -2456306898212937163L;

	public StatefulRNG() {
        super(new LightRNG());
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
     * String-seeded constructor uses the hash of the String as a seed for LightRNG, which is of high quality, but low
     * period (which rarely matters for games), and has good speed and tiny state size.
     */
    public StatefulRNG(String seedString) {
        this(new LightRNG(CrossHash.hash(seedString)));
    }

    @Override
    public void setRandomness(RandomnessSource random) {
        super.setRandomness((random instanceof StatefulRandomness) ? random : new LightRNG(random.nextLong()));
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
