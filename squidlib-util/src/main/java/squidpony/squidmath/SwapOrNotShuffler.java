package squidpony.squidmath;

import java.io.Serializable;

/**
 * Gets a sequence of distinct pseudo-random ints (typically used as indices) from 0 to some bound, without storing all
 * of the sequence in memory. Uses a Swap-Or-Not network with 6 rounds using on a non-power-of-two domain, as described
 * in <a href="https://arxiv.org/abs/1208.1176">this paper by Viet Tung Hoang, Ben Morris, and Phillip Rogaway</a>.
 * The API is very simple; you construct a SwapOrNotShuffler by specifying how many items it can shuffle, and you can
 * optionally use a seed (it will be random if you don't specify a seed). Call {@link #next()} on a SwapOrNotShuffler
 * to get the next distinct int in the shuffled ordering; next() will return -1 if there are no more distinct ints (if
 * {@link #bound} items have already been returned). You can go back to the previous item with {@link #previous()},
 * which similarly returns -1 if it can't go earlier in the sequence. You can restart the sequence with
 * {@link #restart()} to use the same sequence over again, or {@link #restart(long)} to use a different seed (the bound
 * is fixed).
 * <br>
 * This class is extremely similar to {@link LowStorageShuffler}, but LowStorageShuffler is optimized for usage on GWT
 * while SwapOrNotShuffler is meant to have higher quality in general. There's also {@link ShuffledIntSequence}, which
 * extends LowStorageShuffler and uses different behavior so it "re-shuffles" the results when all results have been
 * produced, and {@link SNShuffledIntSequence}, which extends this class but is otherwise like ShuffledIntSequence.
 * <br>
 * Created by Tommy Ettinger on 10/1/2018.
 * @author Viet Tung Hoang, Ben Morris, and Phillip Rogaway
 * @author Tommy Ettinger
 */
public class SwapOrNotShuffler implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int bound;
    protected static final int ROUNDS = 6;
    protected int index;
    protected final int[] keys = new int[ROUNDS];
    protected final long[] functions = new long[ROUNDS];

    /**
     * Constructs a SwapOrNotShuffler with a random seed and a bound of 10.
     */
    public SwapOrNotShuffler(){
        this(10);
    }
    /**
     * Constructs a SwapOrNotShuffler with the given exclusive upper bound and a random seed.
     * @param bound how many distinct ints this can return
     */
    public SwapOrNotShuffler(int bound)
    {
        this(bound, (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    /**
     * Constructs a SwapOrNotShuffler with the given exclusive upper bound and long seed.
     * @param bound how many distinct ints this can return
     * @param seed any long; will be used to get several seeds used internally
     */
    public SwapOrNotShuffler(int bound, long seed)
    {
        // initialize our state
        this.bound = bound;
        restart(seed);
    }

    /**
     * Gets the next distinct int in the sequence, or -1 if all distinct ints have been returned that are non-negative
     * and less than {@link #bound}.
     * @return the next item in the sequence, or -1 if the sequence has been exhausted
     */
    public int next()
    {
        if (index < bound)
        {
            return encode(index++);
        }
        // end of shuffled list if we got here.
        return -1;
    }
    /**
     * Gets the previous returned int from the sequence (as yielded by {@link #next()}), or -1 if next() has never been
     * called (or the SwapOrNotShuffler has reached the beginning from repeated calls to this method).
     * @return the previously-given item in the sequence, or -1 if this can't go earlier
     */
    public int previous()
    {
        if (index > 0) {
            return encode(--index);
        }
        // end of shuffled list if we got here.
        return -1;
    }

    /**
     * Starts the same sequence over from the beginning.
     */
    public void restart()
    {
        index = 0;
    }

    /**
     * Starts the sequence over, but can change the seed (completely changing the sequence). If {@code seed} is the same
     * as the seed given in the constructor, this will use the same sequence, acting like {@link #restart()}.
     * @param seed any int; will be used to get several seeds used internally
     */
    public void restart(long seed)
    {
        index = 0;
        for (int i = 0; i < ROUNDS; i++) {
            long z = (seed = seed * 0x41C64E6DL + ~(i<<1));
            z = (z ^ z >>> 27) * 0xAEF17502108EF2D9L;
            keys[i] = (int)((bound * ((z ^= z >>> 25) & 0xFFFFFFFFL)) >> 32);
            z += (seed ^ seed >>> 26) * 0xDB4F0B9175AE2165L;
            functions[i] = z ^ z >>> 24;
        }
    }

    /**
     * @param data the data being ciphered
     * @param key the current key portion
     * @param fun the current round function portion
     * @return the ciphered data
     */
    public int round(int data, int key, long fun)
    {
        // this is X′ in the paper
        key -= data;
        // cheaper modulo for when we know key (X') is >= -bound
        key += (key >> 31) & bound;
        // the operation of fun doesn't happen in the Abelian group, but X' and data are in it
        return (fun * (Math.max(data, key) ^ fun) < 0L) ? key : data;
    }

    /**
     * @param index the index to cipher; must be between 0 and {@link #bound}, inclusive
     * @return the ciphered index, which might not be less than bound but will be less than or equal to {@link #bound}
     */
    public int encode(int index)
    {
        for (int i = 0; i < ROUNDS; i++) {
            index = round(index, keys[i], functions[i]);
        }
        return index;
    }
}
