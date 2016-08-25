package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * Like LightRNG, but shares a lot in common with a hashing mechanism in CrossHash. The name comes from its
 * similarity to the name for that hash, Lightning, but also to how the current version acts like LightRNG,
 * sort-of, but involves a thunder-like "echo" where the earlier results are used as additional state for the
 * next result. Why should you consider it? It appears to be the fastest RandomnessSource we have available,
 * nearly twice the speed of LightRNG, but statistical testing shows it has significant flaws and probable
 * patterns, like the related category of fast RNGs used by java.util.Random (linear congruential generators).
 * For RNG usage that isn't particularly demanding statistically, this may be a good choice, but if the numbers
 * need to be fair, particularly for random shuffles, look elsewhere (LightRNG has good speed and implements
 * the same interfaces, for example).
 * <br>
 * The tool used for testing this RNG is PractRand, http://pracrand.sourceforge.net/ > The binaries it provides
 * don't seem to work as intended on Windows, so I built from source, generated 32MB files of random 64-bit
 * output with various generators as "Thunder.dat", "Light.dat" and so on, then ran the executables I had
 * built with the MS compilers, with the command line {@code RNG_test.exe stdin64 < Thunder.dat} . For most of
 * the other generators I tried, there were no or nearly-no statistical failures it could find, but there were
 * a significant amount of failures with ThunderRNG (though many more in earlier versions).
 * <br>
 * This is also a StatefulRandomness as well as a RandomnessSource, so it can be given to StatefulRNG's
 * constructor to allow the RNG to get and set the current state.
 * <br>
 * Created by Tommy Ettinger on 8/23/2016.
 */
@Beta
public class ThunderRNG implements StatefulRandomness, RandomnessSource {

    /** The state can be seeded with any value. */
    public long state, lag;
    /** Creates a new generator seeded using Math.random. */
    public ThunderRNG() {
        this((long) Math.floor(Math.random() * Long.MAX_VALUE));
    }

    public ThunderRNG( final long seed ) {
        setState(seed);
    }

    @Override
    public int next( int bits ) {
        return (int)( nextLong() & ( 1L << bits ) - 1 );
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     * @return any long, all 64 bits are random
     */
    @Override
    public long nextLong() {
        //return ((state << 4L) + 0xC6BC279692B5CC83L) * ((state += 0x9E3779B97F4A7C15L) >>> 5) + 0x632BE59BD9B4E019L;
        //return 0xD0E89D2D311E289FL * ((state += 0x9E3779B97F4A7C15L) >> 18L); //very fast
        //return ((state *= 0x9E3779B97F4A7C15L) * (++state >>> 7));
        //return ((((state += 0x9E3779B97F4A7C15L) >> 13) * 0xD0E89D2D311E289FL) >> 9) * 0xC6BC279692B5CC83L;
        //return ((state += 0x9E3779B97F4A7C15L) >> 16) * 0xD0E89D2D311E289FL;
        //return state * ((state += 0x9E3779B97F4A7C15L) >> 5) * 0xD0E89D2D311E289FL;
        //return ((state += 0x9E3779B97F4A7C15L) >> (state >>> 60L)) * 0xD0E89D2D311E289FL;
        //return (state * 0xD0E89D2D311E289FL) ^ (state += 0x9E3779B97F4A7C15L);
        //return ((state >> 5) * 0xC6BC279692B5CC83L) ^ (state += 0x9E3779B97F4A7C15L);
        //return ((state += 0x9E3779B97F4A7C15L) >>> (state >>> 60L)) * 0x632BE59BD9B4E019L; //pretty good quality
        return 0xC6BC279692B5CC83L * (lag ^= 0xD0E89D2D311E289FL * ((state += 0x9E3779B97F4A7C15L) >> 18L));
    }

    public int nextInt()
    {
        return (int)(nextLong());
    }
    /**
     * This returns a maximum of 0.9999999999999999 because that is the largest
     * Double value that is less than 1.0
     *
     * @return a value between 0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    public double nextDouble() {
        return Double.longBitsToDouble(0x3FFL << 52 | nextLong() >>> 12) - 1.0;
    }

    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    @Override
    public long getState() {
        return state;
    }

    /**
     * Set the current internal state of this StatefulRandomness with a long.
     *
     * @param state a 64-bit long. You should avoid passing 0, even though some implementations can handle that.
     */
    @Override
    public void setState(long state) {
        this.state = state + 0x9E3779B97F4A7C15L;
        lag = 0xD0E89D2D311E289FL * (this.state >> 18L);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public RandomnessSource copy() {
        return new ThunderRNG(state);
    }
}
