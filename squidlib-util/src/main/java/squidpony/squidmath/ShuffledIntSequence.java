package squidpony.squidmath;

import java.io.Serializable;

/**
 * An infinite sequence of pseudo-random ints (typically used as indices) from 0 to some bound, with all possible ints
 * returned in a shuffled order before re-shuffling for the next result. Does not store the sequence in memory. Uses a
 * Feistel network, as described in <a href="https://blog.demofox.org/2013/07/06/fast-lightweight-random-shuffle-functionality-fixed/">this post by Alan Wolfe</a>.
 * The API is very simple; you construct a ShuffledIntSequence by specifying how many items it should shuffle (the
 * actual sequence is boundless, but the items it can return are limited to between 0 and some bound), and you can
 * optionally use a seed (it will be random if you don't specify a seed). Call {@link #next()} on a ShuffledIntSequence
 * to get the next distinct int in the shuffled ordering; next() will re-shuffle the sequence if it runs out of distinct
 * possible results. You can go back to the previous item with {@link #previous()}, which is allowed to go earlier than
 * the first result generated (it will jump back to what is effectively the previous shuffled sequence). You can restart
 * the sequence with {@link #restart()} to use the same sequence over again (which doesn't make much sense here, since
 * this makes many sequences by re-shuffling), or {@link #restart(int)} to use a different seed (the bound is fixed).
 * <br>
 * This differs from the version in Alan Wolfe's example code and blog post; it uses a very different round function,
 * and it only uses 2 rounds of it (instead of 4). Wolfe's round function is MurmurHash2, but as far as I can tell the
 * version he uses doesn't have anything like MurmurHash3's fmix32() to adequately avalanche bits, and since all keys
 * are small keys with the usage of MurmurHash2 in his code, avalanche is the most important thing. It's also perfectly
 * fine to use irreversible operations in a Feistel network round function, and I do that since it seems to improve
 * randomness slightly. The {@link #round(int, int)} method used here acts like {@link Coord#hashCode()}, but with two
 * small multiplications included to increase randomness significantly. Using 4 rounds turns out to be overkill in this
 * case. This also uses a different seed for each round.
 * <br>
 * Created by Tommy Ettinger on 9/30/2018.
 * @author Alan Wolfe
 * @author Tommy Ettinger
 */
public class ShuffledIntSequence extends LowStorageShuffler implements Serializable {
    private static final long serialVersionUID = 1L;
    protected int seed;
    /**
     * Constructs a ShuffledIntSequence with a random seed and a bound of 10.
     */
    public ShuffledIntSequence(){
        this(10);
    }

    /**
     * Constructs a ShuffledIntSequence with the given exclusive upper bound and a random seed.
     * @param bound how many distinct ints this can return
     */
    public ShuffledIntSequence(int bound)
    {
        this(bound, (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    /**
     * Constructs a ShuffledIntSequence with the given exclusive upper bound and int seed.
     * @param bound how many distinct ints this can return
     * @param seed any int; will be used to get several seeds used internally
     */
    public ShuffledIntSequence(int bound, int seed)
    {
        super(bound, seed);
        this.seed = seed;
    }

    /**
     * Gets the next distinct int in the sequence, shuffling the sequence if it has been exhausted so it never runs out.
     * @return the next item in the sequence
     */
    public int next()
    {
        int r = super.next();
        if(r == -1)
        {
            restart(seed = seed + 0x9E3779B9 | 0);
            return super.next();
        }
        return r;
    }
    /**
     * Gets the previous returned int from the sequence (as yielded by {@link #next()}), restarting the sequence in a
     * correctly-ordered way if it would go to before the "start" of the sequence (it is actually close to infinite both
     * going forwards and backwards).
     * @return the previously-given item in the sequence, or -1 if something goes wrong (which shouldn't be possible)
     */
    public int previous()
    {
        int shuffleIndex;
        // two tries to get a result, return -1 in the probably-impossible case this fails
        for (int i = 0; i < 2; i++) {
            while (index > 0) {
                // get the next number
                shuffleIndex = encode(--index);

                // if we found a valid index, return success!
                if (shuffleIndex < bound)
                    return shuffleIndex;
            }
            restart(seed = seed - 0x9E3779B9 | 0);
            index = pow4;
        }
        return -1;
    }
    
    /**
     * Starts the sequence over, but can change the seed (completely changing the sequence). If {@code seed} is the same
     * as the seed given in the constructor, this will use the same sequence, acting like {@link #restart()}.
     * @param seed any int; will be used to get several seeds used internally
     */
    public void restart(int seed)
    {
        super.restart(seed);
        this.seed = seed;
    }
}
