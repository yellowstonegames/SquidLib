package squidpony.squidmath;

import java.io.Serializable;

/**
 * An infinite sequence of pseudo-random ints (typically used as indices) from 0 to some bound, with all possible ints
 * returned in a shuffled order before re-shuffling for the next result. Does not store the sequence in memory. Uses a
 * Swap-Or-Not shuffle with 6 rounds on a non-power-of-two domain (0 inclusive to bound exclusive), as described
 * in <a href="https://arxiv.org/abs/1208.1176">this paper by Viet Tung Hoang, Ben Morris, and Phillip Rogaway</a>.
 * The API is very simple; you construct a SNShuffledIntSequence by specifying how many items it should shuffle (the
 * actual sequence is boundless, but the items it can return are limited to between 0 and some bound), and you can
 * optionally use a seed (it will be random if you don't specify one). Call {@link #next()} on a SNShuffledIntSequence
 * to get the next distinct int in the shuffled ordering; next() will re-shuffle the sequence if it runs out of distinct
 * possible results. You can go back to the previous item with {@link #previous()}, which is allowed to go earlier than
 * the first result generated (it will jump back to what is effectively the previous shuffled sequence). You can restart
 * the sequence with {@link #restart()} to use the same sequence over again (which doesn't make much sense here, since
 * this makes many sequences by re-shuffling), or {@link #restart(int)} to use a different seed (the bound is fixed).
 * <br>
 * Like {@link SwapOrNotShuffler}, which this is based on, don't use this for cryptographic purposes. While the
 * Swap-or-Not Shuffle algorithm is capable of strong security guarantees, this implementation emphasizes speed and does
 * not offer any hope of security against a competent attacker.
 * <br>
 * Created by Tommy Ettinger on 10/6/2018.
 * @author Viet Tung Hoang, Ben Morris, and Phillip Rogaway
 * @author Tommy Ettinger
 */
public class SNShuffledIntSequence extends SwapOrNotShuffler implements Serializable {
    private static final long serialVersionUID = 1L;
    protected int seed;
    /**
     * Constructs a ShuffledIntSequence with a random seed and a bound of 10.
     */
    public SNShuffledIntSequence(){
        this(10);
    }

    /**
     * Constructs a ShuffledIntSequence with the given exclusive upper bound and a random seed.
     * @param bound how many distinct ints this can return
     */
    public SNShuffledIntSequence(int bound)
    {
        this(bound, (int)((Math.random() - 0.5) * 0x1.0p32));
    }

    /**
     * Constructs a ShuffledIntSequence with the given exclusive upper bound and int seed.
     * @param bound how many distinct ints this can return
     * @param seed any int; will be used to get several seeds used internally
     */
    public SNShuffledIntSequence(int bound, int seed)
    {
        super(bound, seed);
        this.seed = seed;
    }

    /**
     * Gets the next distinct int in the sequence, shuffling the sequence if it has been exhausted so it never runs out.
     * @return the next item in the sequence
     */
    @Override
    public int next()
    {
        int shuffleIndex = super.next();
        if(shuffleIndex == -1)
        {
            restart(seed += 0x9E3779B9);
            index = 0;
            shuffleIndex = super.next();
        }
        return shuffleIndex;
    }
    /**
     * Gets the previous returned int from the sequence (as yielded by {@link #next()}), restarting the sequence in a
     * correctly-ordered way if it would go to before the "start" of the sequence (it is actually close to infinite both
     * going forwards and backwards).
     * @return the previously-given item in the sequence, or -1 if something goes wrong (which shouldn't be possible)
     */
    @Override
    public int previous()
    {
        int shuffleIndex = super.previous();
        if(shuffleIndex == -1)
        {
            restart(seed -= 0x9E3779B9);
            index = bound;
            shuffleIndex = super.previous();
        }
        return shuffleIndex;
    }
    
    /**
     * Starts the sequence over, but can change the seed (completely changing the sequence). If {@code seed} is the same
     * as the seed given in the constructor, this will use the same sequence, acting like {@link #restart()}.
     * @param seed any long; will be used to get several seeds used internally
     */
    @Override
    public void restart(int seed)
    {
        super.restart(seed);
        this.seed = seed;
    }
}
