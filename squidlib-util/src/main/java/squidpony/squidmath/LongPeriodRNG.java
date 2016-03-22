package squidpony.squidmath;

import squidpony.StringKit;

/**
 * An RNG that has a drastically longer period than the other generators in SquidLib, other than MersenneTwister,
 * without sacrificing speed or HTML target compatibility. If you don't already know what the period of an RNG is, this
 * probably isn't needed for your purposes, or many purposes in games at all. It is primarily meant for applications
 * that need to generate massive amounts of random numbers, more than 2 to the 64 (18,446,744,073,709,551,616), without
 * repeating sequences of generated numbers. An RNG's period refers to the number of numbers it generates given a single
 * seed before the sequence repeats from the beginning. The period of this class is 2 to the 1024 minus 1
 * (179,769,313,486,231,590,772,930,519,078,902,473,361,797,697,894,230,657,273,430,081,157,732,675,805,500,963,132,708,
 * 477,322,407,536,021,120,113,879,871,393,357,658,789,768,814,416,622,492,847,430,639,474,124,377,767,893,424,865,485,
 * 276,302,219,601,246,094,119,453,082,952,085,005,768,838,150,682,342,462,881,473,913,110,540,827,237,163,350,510,684,
 * 586,298,239,947,245,938,479,716,304,835,356,329,624,224,137,215).
 * This class has 128 bytes of state plus more in overhead (compare to the 16-byte-with-overhead LightRNG), but due to
 * its massive period and createMany() static method, you can create a large number of subsequences with rather long
 * periods themselves from a single seed. This uses the xorshift-1024* algorithm, and has competitive speed.
 * Created by Tommy Ettinger on 3/21/2016.
 * Ported from CC0-licensed C code by Sebastiano Vigna, at http://xorshift.di.unimi.it/xorshift1024star.c
 */
public class LongPeriodRNG implements RandomnessSource{

    public long[] state = new long[16];
    public int choice;
    private static final long serialVersionUID = 163524490381383244L;
    private static final long jumpTable[] = { 0x84242f96eca9c41dL,
            0xa3c65b8776f96855L, 0x5b34a39f070b5837L, 0x4489affce4f31a1eL,
            0x2ffeeb0a48316f40L, 0xdc2d9891fe68c022L, 0x3659132bb12fea70L,
            0xaac17d8efa43cab8L, 0xc4cb815590989b13L, 0x5ee975283d71c93bL,
            0x691548c86c1bd540L, 0x7910c41d10a1e6a5L, 0x0b5fc64563b3e2a8L,
            0x047f7684e9fc949dL, 0xb99181f2d8f685caL, 0x284600e3f30e38c3L
    };

    /**
     * Builds a LongPeriodRNG and initializes this class' 1024 bits of state with a random seed passed into SplitMix64,
     * the algorithm also used by LightRNG. A different algorithm is used in non-constructor code to generate random
     * numbers, which is a recommended technique to generate seeds.
     */
    public LongPeriodRNG()
    {
        long ts = (Double.doubleToLongBits(Math.random()) << 32) ^
                (Double.doubleToLongBits(Math.random()) << 16) ^
                (Double.doubleToLongBits(Math.random()));
        if(ts == 0)
            ts++;
        choice = (int)(ts & 15);
        for (int i = 0; i < 16; i++) {
            long z = ( ts += 0x9E3779B97F4A7C15L );
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            state[i] = z ^ (z >>> 31);
        }
    }

    /**
     * Builds a LongPeriodRNG and initializes this class' 1024 bits of state with the given seed passed into SplitMix64,
     * the algorithm also used by LightRNG. A different algorithm is used in non-constructor code to generate random
     * numbers, which is a recommended technique to generate seeds.
     * @param seed a 64-bit seed; can be any value, though a seed of exactly 0 will be replaced with 1.
     */
    public LongPeriodRNG(long seed)
    {
        if(seed == 0)
            seed++;
        choice = (int)(seed & 15);
        for (int i = 0; i < 16; i++) {
            long z = ( seed += 0x9E3779B97F4A7C15L );
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            state[i] = z ^ (z >>> 31);
        }
    }
    public LongPeriodRNG(LongPeriodRNG other)
    {
        choice = other.choice;
        state = new long[16];
        System.arraycopy(other.state, 0, state, 0, 16);
    }

    @Override
    public int next( int bits ) {
        return (int)( nextLong() & ( 1L << bits ) - 1 );
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     * <br>
     * Written by Sebastiano Vigna, from http://xorshift.di.unimi.it/xorshift1024star.c
     * @return any long, all 64 bits are random
     */
    @Override
    public long nextLong() {
        final long s0 = state[choice];
        long s1 = state[choice = (choice + 1) & 15];
        s1 ^= s1 << 31; // a
        state[choice] = s1 ^ s0 ^ (s1 >> 11) ^ (s0 >> 30); // b,c
        return state[choice] * 1181783497276652981L;
    }

    /**
     * This is the jump function for the generator. It is equivalent to 2^512 calls to nextLong(); it can be used to
     * generate 2^512 non-overlapping subsequences for parallel computations. Alters the state of this object.
     * <br>
     * Written by Sebastiano Vigna, from http://xorshift.di.unimi.it/xorshift1024star.c , don't ask how it works.
     * */
    public void jump() {

        long[] t = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for(int i = 0; i < 16; i++)
        for(int b = 0; b < 64; b++) {
            if ((jumpTable[i] & 1L << b) != 0) {
                for (int j = 0; j < 16; j++)
                    t[j] ^= state[(j + choice) & 15];
            }
            nextLong();
        }

        for(int j = 0; j < 16; j++)
            state[(j + choice) & 15] = t[j];
    }

    /**
     * Creates many LongPeriodRNG objects in an array, where each will generate a sequence of 2 to the 512 numbers that
     * will not overlap with other sequences in the array. The number of items in the array is specified by count.
     * @param count the number of LongPeriodRNG objects to generate in the array.
     * @return an array of LongPeriodRNG where none of the RNGs will generate overlapping sequences.
     */
    public static LongPeriodRNG[] createMany(int count)
    {
        if(count < 1) count = 1;
        LongPeriodRNG origin = new LongPeriodRNG();
        LongPeriodRNG[] values = new LongPeriodRNG[count];
        for (int i = 0; i < count; i++) {
            values[i] = new LongPeriodRNG(origin);
            origin.jump();
        }
        return values;
    }

    /**
     * Creates many LongPeriodRNG objects in an array, where each will generate a sequence of 2 to the 512 numbers that
     * will not overlap with other sequences in the array. The number of items in the array is specified by count. A
     * seed can be given that will affect all items in the array, but with each item using a different section of the
     * massive period this class supports. Essentially, each LongPeriodRNG in the array will generate a different random
     * sequence relative to any other element of the array, but the sequences are reproducible if the same seed is given
     * to this method a different time (useful for testing).
     * @param count the number of LongPeriodRNG objects to generate in the array.
     * @param seed the RNG seed that will determine the different sequences the returned LongPeriodRNG objects produce
     * @return an array of LongPeriodRNG where none of the RNGs will generate overlapping sequences.
     */
    public static LongPeriodRNG[] createMany(int count, long seed)
    {
        if(count < 1) count = 1;
        LongPeriodRNG origin = new LongPeriodRNG(seed);
        LongPeriodRNG[] values = new LongPeriodRNG[count];
        for (int i = 0; i < count; i++) {
            values[i] = new LongPeriodRNG(origin);
            origin.jump();
        }
        return values;
    }

    @Override
    public String toString() {
        return "LongPeriodRNG with state hash 0x" + StringKit.hexHash(state) + "L, choice 0x" + StringKit.hex(choice);
    }
}
