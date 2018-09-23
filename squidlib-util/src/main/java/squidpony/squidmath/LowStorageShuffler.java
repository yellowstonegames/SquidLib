package squidpony.squidmath;

import java.io.Serializable;

/**
 * Gets a sequence of distinct pseudo-random ints (typically used as indices) from 0 to some bound, without storing all
 * of the sequence in memory. Uses a Feistel network, as described in
 * <a href="https://blog.demofox.org/2013/07/06/fast-lightweight-random-shuffle-functionality-fixed/">this post by Alan Wolfe</a>.
 * The API is very simple; you construct a LowStorageShuffler by specifying how many items it can shuffle, and you can
 * optionally use a seed (it will be random if you don't specify a seed). Call {@link #next()} on a LowStorageShuffler
 * to get the next distinct int in the shuffled ordering; next() will return -1 if there are no more distinct ints (if
 * {@link #bound} items have already been returned). You can go back to the previous item with {@link #previous()},
 * which similarly returns -1 if it can't go earlier in the sequence. You can restart the sequence with
 * {@link #restart()} to use the same sequence over again, or {@link #restart(int)} to use a different seed (the bound
 * is fixed).
 * <br>
 * This differs from the version in Alan Wolfe's example code and blog post; it uses a very different round function,
 * and it only uses 2 rounds of it (instead of 4). Wolfe's round function is MurmurHash2, but as far as I can tell the
 * version he uses doesn't have anything like MurmurHash3's fmix32() to adequately avalanche bits, and since all keys
 * are small keys with the usage of MurmurHash2 in his code, avalanche is the most important thing. It's also perfectly
 * fine to use irreversible operations in a Feistel network round function, and I do that since it seems to improve
 * randomness slightly. The round function used here acts like a rather-robust 2-item hash. First, it runs a step where
 * it takes one of the two parameters, multiplies it by a 16-bit value, "sometimes" adds a 32-bit value, and XORs that
 * with the other parameter. It It runs this step twice each on both combinations of the two parameters, once doing the
 * "sometimes" adding and once not, then runs a different Overton iadla generator on each parameter (one such generator
 * is {@code x += x >>> 21; return (x += x << 8);}, for reference). It then returns data XORed with seed. Using 4 rounds
 * turns out to be overkill in this case. This also uses a different seed for each round.
 * <br>
 * Created by Tommy Ettinger on 9/22/2018.
 * @author Alan Wolfe
 * @author Tommy Ettinger
 */
public class LowStorageShuffler implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int bound;
    protected int index, pow4, halfBits, leftMask, rightMask;
    protected int key0, key1;//, key2, key3;

    /**
     * Constructs a LowStorageShuffler with the given exclusive upper bound and a random seed.
     * @param bound how many distinct ints this can return
     */
    public LowStorageShuffler(int bound)
    {
        this(bound, (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    /**
     * Constructs a LowStorageShuffler with the given exclusive upper bound and int seed.
     * @param bound how many distinct ints this can return
     * @param seed any int; will be used to get several seeds used internally
     */
    public LowStorageShuffler(int bound, int seed)
    {
        // initialize our state
        this.bound = bound;
        restart(seed);
        // calculate next power of 4.  Needed since the balanced Feistel network needs
        // an even number of bits to work with
        pow4 = HashCommon.nextPowerOfTwo(bound);
        pow4 = ((pow4 | pow4 << 1) & 0x55555554) - 1;
        // calculate our left and right masks to split our indices for the Feistel network
        halfBits = Integer.bitCount(pow4) >>> 1;
        rightMask = pow4 >>> halfBits;
        leftMask = pow4 ^ rightMask;
    }

    /**
     * Gets the next distinct int in the sequence, or -1 if all distinct ints have been returned that are non-negative
     * and less than {@link #bound}.
     * @return the next item in the sequence, or -1 if the sequence has been exhausted
     */
    public int next()
    {
        int shuffleIndex;
        // index is the index to start searching for the next number at
        while (index <= pow4)
        {
            // get the next number
            shuffleIndex = encode(index++);

            // if we found a valid index, return it!
            if (shuffleIndex < bound)
                return shuffleIndex;
        }

        // end of shuffled list if we got here.
        return -1;
    }
    /**
     * Gets the previous returned int from the sequence (as yielded by {@link #next()}), or -1 if next() has never been
     * called (or the LowStorageShuffler has reached the beginning from repeated calls to this method).
     * @return the previously-given item in the sequence, or -1 if this can't go earlier
     */
    public int previous()
    {
        int shuffleIndex;
        // index is the index to start searching for the next number at
        while (index > 0)
        {
            // get the next number
            shuffleIndex = encode(index--);

            // if we found a valid index, return success!
            if (shuffleIndex < bound)
                return shuffleIndex;
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
        key0 = Light32RNG.determine(seed ^ 0xDE4D * ~bound);
        key1 = Light32RNG.determine(key0 ^ 0xBA55 * bound);
        key0 ^= Light32RNG.determine(~key1 ^ 0xBEEF * bound);
        key1 ^= Light32RNG.determine(~key0 ^ 0x1337 * bound);
    }

    /**
     * An irreversible mixing function that seems to give good results; GWT-compatible.
     * First, it runs a step where it takes one of the two parameters, multiplies it by a 16-bit value, "sometimes" adds
     * a 32-bit value, and XORs that with the other parameter. It It runs this step twice each on both combinations of
     * the two parameters, once doing the "sometimes" adding and once not, then runs a different Overton iadla generator
     * on each parameter (one such generator is {@code x += x >>> 21; return (x += x << 8);}, for reference). It then
     * returns data XORed with seed. This is complicated, but less involved schemes did not do well at all. There's four
     * multiplications used here, all by small enough values to not risk GWT safety.
     * @param data the data being ciphered
     * @param seed the current seed
     * @return the ciphered data
     */
    protected int round(int data, int seed)
    {
        seed ^= data * 0xC6D5 + 0xB531A935;
        data ^= seed * 0xBCFD + 0x41C64E6D;
        seed ^= data * 0xACED;
        data ^= seed * 0xBA55;
        data += data >>> 21;
        seed += seed >>> 22;
        data += data << 8;
        seed += seed << 5;
//        data += data >>> 21;
//        seed += seed >>> 22;
//        data += data << 8;
//        seed += seed << 5;
//        data += data >>> 16;
//        seed += seed >>> 13;
//        data += data << 9;
//        seed += seed << 11;
        return data ^ seed;
    }

    /**
     * Encodes an index with a 2-round Feistel network. It is possible that someone would want to override this method
     * to use more or less rounds, but there must always be an even number.
     * @param index the index to cipher; must be betweeen 0 and {@link #pow4}, inclusive
     * @return the ciphered index, which might not be less than bound but will be less than or equal to {@link #pow4}
     */
    protected int encode(int index)
    {
        // break our index into the left and right half
        int left = (index & leftMask) >>> halfBits;
        int right = (index & rightMask);
        // do 2 Feistel rounds
        int newRight = left ^ (round(right, key0) & rightMask);
        left = right;
        right = newRight;
        newRight = left ^ (round(right, key1) & rightMask);
//        left = right;
//        right = newRight;
//        newRight = left ^ (round(right, key2) & rightMask);
//        left = right;
//        right = newRight;
//        newRight = left ^ (round(right, key3) & rightMask);

//        left = right;
//        right = newRight;

        // put the left and right back together to form the encrypted index
//        return left << halfBits | right;
        return right << halfBits | newRight;
    }
}
