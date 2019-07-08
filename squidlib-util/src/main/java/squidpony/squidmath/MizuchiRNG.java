package squidpony.squidmath;

import squidpony.StringKit;
import squidpony.annotation.Beta;

import java.io.Serializable;

/**
 * A high-quality StatefulRandomness based on {@link LinnormRNG} but modified to allow any odd number as a stream,
 * instead of LinnormRNG's hardcoded stream of 1. Although some streams may have quality issues, the structure is based
 * on a linear congruential generator where the stream is the additive component, and in that context all odd numbers
 * are usually considered equally effective. Has 64 bits of state, 64 bits used to store a stream (which cannot be
 * changed after construction) and natively outputs 64 bits at a time. Changes its state with a basic linear
 * congruential generator (it is simply {@code state = state * 3935559000370003845L + stream}). Starting with that LCG's
 * output, it xorshifts that output twice, multiplies by a very large negative long, then returns another xorshift. Like
 * LinnormRNG, the output of this simple function passes all 32TB of PractRand (for one stream, it had 3 anomalies, but
 * another had none, and none were ever significant or persistent), meaning its statistical quality is excellent. The
 * speed of this particular class isn't fully clear yet, but benchmarks performed under the heavy load of PractRand
 * testing happening at the same time appeared to show no significant difference between LinnormRNG and MizuchiRNG in
 * speed (which means it's tied for second place in its category, behind {@link DiverRNG}).
 * <br>
 * This generator is a StatefulRandomness but not a SkippingRandomness, so it can't (efficiently) have the skip() method
 * that LightRNG has. A method could be written to run the generator's state backwards, though, as well as to get the
 * state from an output of {@link #nextLong()}. {@link LinnormRNG} uses the same algorithm except for the number added
 * in the LCG state update; there this number is always 1, but here it can be any odd long. This means that any given
 * MizuchiRNG object has two long values stored in it instead of the one in a LinnormRNG, but it allows two MizuchiRNG
 * objects with different streams to produce different, probably-not-correlated sequences of results, even with the same
 * seed. This property may be useful for cases where an adversary is trying to predict results in some way, though using
 * different streams for this purpose isn't enough and should be coupled with truncation of a large part of output (see
 * PCG-Random's techniques for this).
 * <br>
 * The name comes from combining the concept of a linnorm, which is a dragon and the namesake of LinnormRNG, with
 * streams, since Mizuchi allows many possible streams, to get the concept of a river-or-stream-dwelling dragon. The
 * mizuchi is a (by some versions of the story) river dragon from Japanese mythology.
 * <br>
 * Written June 29, 2019 by Tommy Ettinger. Thanks to M.E. O'Neill for her insights into the family of generators both
 * this and her PCG-Random fall into, and to the team that worked on SplitMix64 for SplittableRandom in JDK 8. Chris
 * Doty-Humphrey's work on PractRand has been invaluable. The LCG state multiplier is listed in a paper by L'Ecuyer from
 * 1999, Tables of Linear Congruential Generators of Different Sizes and Good Lattice Structure. The other
 * multiplier is from PCG-Random, and that's both the nothing-up-my-sleeve numbers used here. Thanks also to Sebastiano
 * Vigna and David Blackwell for creating the incredibly fast xoroshiro128+ generator and also very fast
 * <a href="http://xoshiro.di.unimi.it/hwd.php">HWD tool</a>; the former inspired me to make my code even faster and the
 * latter tool seems useful so far in proving the quality of the generator (LinnormRNG passes over 100TB of HWD, and
 * probably would pass much more if I gave it more days to run).
 * @author Tommy Ettinger
 */
@Beta
public final class MizuchiRNG implements StatefulRandomness, Serializable {

    private static final long serialVersionUID = 153186732328748834L;

    private long state; /* The state can be seeded with any value. */
    
    private final long stream;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public MizuchiRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public MizuchiRNG(final long seed) {
        state = seed;
        this.stream = (seed
                ^ Long.rotateLeft((seed ^ 0x369DEA0F31A53F85L) * 0x6A5D39EAE116586DL + 0x9E3779B97F4A7C15L, (int)seed)
                ^ Long.rotateLeft((seed ^ 0x6A5D39EAE116586DL) * 0x369DEA0F31A53F85L + 0x4A7C159E3779B97FL, (int)seed * 0x41C64E6D >>> 26)) | 1L;
    }

    public MizuchiRNG(final long seed, final long stream) {
        state = seed;
        this.stream = (stream | 1L);
    }

    public MizuchiRNG(final String seed) {
        state = CrossHash.Mist.predefined[32].hash64(seed);
        stream = (CrossHash.Mist.predefined[(int) state & 31].hash64(seed) | 1L);
    }

    @Override
    public final int next(int bits)
    {
        long z = (state = state * 0x369DEA0F31A53F85L + stream);
        z = (z ^ z >>> 23 ^ z >>> 47) * 0xAEF17502108EF2D9L;
        return (int)(z ^ z >>> 25) >>> (32 - bits);
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public final long nextLong() {
        long z = (state = state * 0x369DEA0F31A53F85L + stream);
        z = (z ^ z >>> 23 ^ z >>> 47) * 0xAEF17502108EF2D9L;
        return (z ^ z >>> 25);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public MizuchiRNG copy() {
        return new MizuchiRNG(state);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     *
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        long z = (state = state * 0x369DEA0F31A53F85L + stream);
        z = (z ^ z >>> 23 ^ z >>> 47) * 0xAEF17502108EF2D9L;
        return (int)(z ^ z >>> 25);
    }

    /**
     * Exclusive on the outer bound.  The inner bound is 0.
     * The bound can be negative, which makes this produce either a negative int or 0.
     *
     * @param bound the upper bound; should be positive
     * @return a random int between 0 (inclusive) and bound (exclusive)
     */
    public final int nextInt(final int bound) {
        long z = (state = state * 0x369DEA0F31A53F85L + stream);
        z = (z ^ z >>> 23 ^ z >>> 47) * 0xAEF17502108EF2D9L;
        return (int)((bound * ((z ^ z >>> 25) & 0xFFFFFFFFL)) >> 32);
    }

    /**
     * Inclusive inner, exclusive outer.
     *
     * @param inner the inner bound, inclusive, can be positive or negative
     * @param outer the outer bound, exclusive, can be positive or negative, usually greater than inner
     * @return a random int between inner (inclusive) and outer (exclusive)
     */
    public final int nextInt(final int inner, final int outer) {
        return inner + nextInt(outer - inner);
    }

    /**
     * Exclusive on the upper bound. The lower bound is 0.
     *
     * @param bound the upper bound; should be positive (if negative, this returns 0)
     * @return a random long less than n
     */
    public final long nextLong(final long bound) {
        if (bound <= 0) return 0;
        final long threshold = (0x7fffffffffffffffL - bound + 1) % bound;
        for (; ; ) {
            long bits = nextLong() & 0x7fffffffffffffffL;
            if (bits >= threshold)
                return bits % bound;
        }
    }

    /**
     * Inclusive lower, exclusive upper.
     *
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, should be positive, must be greater than lower
     * @return a random long at least equal to lower and less than upper
     */
    public final long nextLong(final long lower, final long upper) {
        if (upper - lower <= 0) throw new IllegalArgumentException("Upper bound must be greater than lower bound");
        return lower + nextLong(upper - lower);
    }

    /**
     * Gets a uniform random double in the range [0.0,1.0)
     *
     * @return a random double at least equal to 0.0 and less than 1.0
     */
    public final double nextDouble() {
        long z = (state = state * 0x369DEA0F31A53F85L + stream);
        z = (z ^ z >>> 23 ^ z >>> 47) * 0xAEF17502108EF2D9L;
        return ((z ^ z >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;

    }

    /**
     * Gets a uniform random double in the range [0.0,outer) given a positive parameter outer. If outer
     * is negative, it will be the (exclusive) lower bound and 0.0 will be the (inclusive) upper bound.
     *
     * @param outer the exclusive outer bound, can be negative
     * @return a random double between 0.0 (inclusive) and outer (exclusive)
     */
    public final double nextDouble(final double outer) {
        long z = (state = state * 0x369DEA0F31A53F85L + stream);
        z = (z ^ z >>> 23 ^ z >>> 47) * 0xAEF17502108EF2D9L;
        return ((z ^ z >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53 * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     *
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public final float nextFloat() {
        final long z = (state = state * 0x369DEA0F31A53F85L + stream);
        return ((z ^ z >>> 23 ^ z >>> 47) * 0xAEF17502108EF2D9L >>> 40) * 0x1p-24f;
    }

    /**
     * Gets a random value, true or false.
     * Calls nextLong() once.
     *
     * @return a random true or false value.
     */
    public final boolean nextBoolean() {
        final long z = (state = state * 0x369DEA0F31A53F85L + stream);
        return ((z ^ z >>> 23 ^ z >>> 47) * 0xAEF17502108EF2D9L) < 0;
    }

    /**
     * Given a byte array as a parameter, this will fill the array with random bytes (modifying it
     * in-place). Calls nextLong() {@code Math.ceil(bytes.length / 8.0)} times.
     *
     * @param bytes a byte array that will have its contents overwritten with random bytes.
     */
    public final void nextBytes(final byte[] bytes) {
        int i = bytes.length, n;
        while (i != 0) {
            n = Math.min(i, 8);
            for (long bits = nextLong(); n-- != 0; bits >>>= 8) bytes[--i] = (byte) bits;
        }
    }

    /**
     * Sets the seed (also the current state) of this generator.
     *
     * @param seed the seed to use for this LightRNG, as if it was constructed with this seed.
     */
    @Override
    public final void setState(final long seed) {
        state = seed;
    }

    /**
     * Gets the current state of this generator.
     *
     * @return the current seed of this LightRNG, changed once per call to nextLong()
     */
    @Override
    public final long getState() {
        return state;
    }

    @Override
    public String toString() {
        return "MizuchiRNG with state 0x" + StringKit.hex(state) + "L on stream 0x" + StringKit.hex(stream) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MizuchiRNG mizuchiRNG = ((MizuchiRNG) o);
        return state == mizuchiRNG.state && stream == mizuchiRNG.stream;
    }

    @Override
    public int hashCode() {
        return (int) ((state ^ (state >>> 32)) * 0xDB4F0B9175AE2165L + (stream ^ (stream >>> 32)));
    }

//    public static void main(String[] args)
//    {
//        /*
//        cd target/classes
//        java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly sarong/DervishRNG > Dervish_asm.txt
//         */
//        long longState = 1L;
//        int intState = 1;
//        float floatState = 0f;
//        double doubleState = 0.0;
//        MizuchiRNG rng = new MizuchiRNG(1L, 123456789L);
//        //longState += determine(i);
//        //longState = longState + 0x9E3779B97F4A7C15L;
//        //seed += determine(longState++);
//        for (int r = 0; r < 10; r++) {
//            for (int i = 0; i < 10000007; i++) {
//                longState += rng.nextLong();
//            }
//        }
//        System.out.println(longState);
//
//        for (int r = 0; r < 10; r++) {
//            for (int i = 0; i < 10000007; i++) {
//                intState += rng.next(16);
//            }
//        }
//        System.out.println(intState);
//
//        for (int r = 0; r < 10; r++) {
//            for (int i = 0; i < 10000007; i++) {
//                floatState += rng.nextFloat();
//            }
//        }
//        System.out.println(floatState);
//
//        for (int r = 0; r < 10; r++) {
//            for (int i = 0; i < 10000007; i++) {
//                doubleState += rng.nextDouble();
//            }
//        }
//        System.out.println(doubleState);
//
//    }

}
