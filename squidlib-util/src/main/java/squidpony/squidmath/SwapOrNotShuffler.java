package squidpony.squidmath;

import java.io.Serializable;

/**
 * Gets a sequence of distinct pseudo-random ints (typically used as indices) from 0 to some bound, without storing all
 * of the sequence in memory. Uses a Swap-Or-Not shuffle with 6 rounds on a non-power-of-two domain (0 inclusive to
 * bound exclusive), as described in <a href="https://arxiv.org/abs/1208.1176">this paper by Viet Tung Hoang, Ben
 * Morris, and Phillip Rogaway</a>. The API is very simple; you construct a SwapOrNotShuffler by specifying how many
 * items it can shuffle, and you can optionally use a seed (it will be random if you don't specify a seed). Call
 * {@link #next()} on a SwapOrNotShuffler to get the next distinct int in the shuffled ordering; next() will return -1
 * if there are no more distinct ints (if {@link #bound} items have already been returned). You can go back to the
 * previous item with {@link #previous()}, which similarly returns -1 if it can't go earlier in the sequence. You can
 * restart the sequence with {@link #restart()} to use the same sequence over again, or {@link #restart(int)} to use a
 * different seed (the bound is fixed).
 * <br>
 * This class is extremely similar to {@link LowStorageShuffler}; both classes are optimized for usage on GWT but
 * SwapOrNotShuffler is meant to have higher quality in general. LowStorageShuffler sometimes performs better (when the
 * bound is equal to or just less than a power of 4), but often performs much worse (when the bound is just a little
 * more than a power of 4), while SwapOrNotShuffler has steady performance that has an expected-case near the best-case
 * of LowStorageShuffler. There's also {@link ShuffledIntSequence}, which extends LowStorageShuffler and uses different
 * behavior so it "re-shuffles" the results when all results have been produced, and {@link SNShuffledIntSequence},
 * which extends this class but is otherwise like ShuffledIntSequence.
 * <br>
 * Don't use this for cryptographic purposes; it uses too-few rounds, the "function" is probably as insecure as it gets,
 * and in this version there are only 2 to the 32 possible keys, which would make a brute-force attack trivial. It seems
 * good enough for generating shuffles for domains with unusual small sizes when the purpose is game-related, and not
 * anything too serious. While the Swap-or-Not Shuffle algorithm is capable of strong security guarantees, this
 * implementation emphasizes speed and does not offer any hope of security against a competent attacker.
 * <br>
 * Created by Tommy Ettinger on 10/1/2018.
 * @author Viet Tung Hoang, Ben Morris, and Phillip Rogaway
 * @author Tommy Ettinger
 */
public class SwapOrNotShuffler implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int bound;
    protected final int ROUNDS;
    protected int index;
    protected final int[] keys;
    protected int func;
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
        this(bound, (int)((Math.random() - 0.5) * 0x1.0p32));
    }

    /**
     * Constructs a SwapOrNotShuffler with the given exclusive upper bound and int seed.
     * @param bound how many distinct ints this can return
     * @param seed any int; will be used to get several seeds used internally
     */
    public SwapOrNotShuffler(int bound, int seed)
    {
        // initialize our state
        this.bound = Math.max(1, bound);
        ROUNDS = this.bound + 4;
        keys = new int[ROUNDS];
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
    public void restart(int seed)
    {
        index = 0;
        int z;
        func = ~seed;
        for (int i = 0; i < ROUNDS; i++) {
            z = (seed += 0xC13FA9A9); // 2 to the 32 divided by the plastic constant (an irrational number)
            // 32-bit no-multiply version of Paperweight algorithm
            z ^= z >>> 13;
            z = (z << 19) - z;
            z ^= z >>> 12;
            z = (z << 17) - z;
            z ^= z >>> 14;
            z = (z << 13) - z;
            func += (keys[i] = (int)((bound * ((z ^ z >>> 15) & 0xFFFFFFFFL)) >> 32)) ^ z; // can we avoid this multiply?
            // func may be out of range for an int on GWT; this is OK because it always has bitwise
            // ops used on it before anything else. Bitwise ops fix the range to a valid int.
        }
//        z = (z << 9) + (z << 8 | z >>> 24) ^ 0x9E3779B9;
//        z = (z << 27) + (z << 20 | z >>> 12);
//        func = (z << 19) + (z << 14 | z >>> 18);
    }

//    /**
//     * @param data the data being ciphered
//     * @param key the current key portion
//     * @param fun the current round function portion
//     * @return the ciphered data
//     */
//    public int round(int data, int key, int fun)
//    {
//        // this is X′ in the paper
//        key -= data;
//        // cheaper modulo for when we know key (X') is >= -bound
//        key += (key >> 31) & bound;
//        // the operation of fun doesn't happen in the Abelian group, but X' and data are in it
//        return (fun * (Math.max(data, key) ^ fun) < 0) ? key : data;
//    }

    /**
     * @param index the index to cipher; must be between 0 and {@link #bound}, inclusive
     * @return the ciphered index, which will be less than {@link #bound}
     */
    public int encode(int index)
    {
        int f = func ^ func >>> 13;
        for (int i = 0; i < ROUNDS; i++) { 
            int key = keys[i] - index;
            key += (key >> 31 & bound);
            // (f ^ func >>> (f += i)) is a very special piece of code.
            // it takes the current value of f and XORs it with a shifted version of func; the shift is unusual.
            // to do the shift, it adds the current round counter i to f, and then uses the bottom 5 bits as the shift.
            if(((f ^ func >>> (f += i)) + Math.max(index, key) & 1) == 0) index = key;
        }
        return index;
    }

}
