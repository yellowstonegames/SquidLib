package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * A variant on {@link ThrustAltRNG} that gives up some speed, but allows choosing any of 2 to the 63 odd-number streams
 * that change the set of possible outputs this can produce (amending the main flaw of ThrustAltRNG). This does well in
 * PractRand quality tests, passing at least 8TB and probably more (it shares a lot of structure with ThrustAltRNG,
 * which does very well in PractRand's testing as well as TestU01's BigCrush). It also outperforms LinnormRNG and comes
 * close to ThrustAltRNG in JMH benchmarks, making it arguably the fastest random number generator algorithm here that
 * can produce all long values (it just needs multiple generator objects to do so, all seeded differently). If you
 * expect to have many individual RandomnessSources all seeded differently, this should be a good pick; if you only have
 * one RandomnessSource in use, you should prefer {@link LinnormRNG} if you don't mind that it can't produce duplicates,
 * {@link OrbitRNG} if you want something similar to this generator that allows all state pairs, or {@link GWTRNG} if
 * you expect to use GWT to target HTML.
 * <br>
 * Because this can produce multiple occurrences of any number in its sequence (except 0, which it should always produce
 * once over its period of 2 to the 64), it can be considered as passing the "birthday problem" test; after running
 * <a href="http://www.pcg-random.org/posts/birthday-test.html">this test provided by Melissa E. O'Neill</a> on Tangle,
 * it correctly has 9 repeats compared to an expected 10, using the Skipping adapter to check one out of every 65536
 * outputs for duplicates. A generator that failed that test would have 0 repeats or more than 20, so Tangle passes.
 * ThrustAltRNG probably also passes (or its structure allows it to potentially do so), but LightRNG, LinnormRNG,
 * MizuchiRNG, and even ThrustRNG will fail it by never repeating an output. Truncating the output bits of any of these
 * generators will allow them to pass this test, at the cost of reducing the size of the output to an int instead of a
 * long (less than ideal). Notably, an individual Tangle generator tends to be able to produce about 2/3 of all possible
 * long outputs, with roughly 1/3 of all outputs not possible to produce and another 1/3 produced exactly once. These
 * are approximations and will vary between instances. The test that gave the "roughly 2/3 possible" result gave similar
 * results with 8-bit stateA and stateB and with 16-bit stateA and stateB, though it could change with the 64-bit states
 * Tangle actually uses.
 * <br>
 * The name "Tangle" comes from how the two states of this generator are "tied up in each other," with synchronized
 * periods of 2 to the 64 (stateA) and 2 to the 63 (stateB) that repeat as a whole every 2 to the 64 outputs. Contrary
 * to the name, Tangle isn't slowed down at all by this, but the period length of the generator is less than the maximum
 * possible (which OrbitRNG has, though that one is slowed down).
 * <br>
 * See also {@link OrbitRNG}, which gives up more speed but moves through all 2 to the 64 long values as streams over
 * its full period, which is 2 to the 128 (with one stream) instead of the 2 to the 64 (with 2 to the 63 streams) here.
 * Orbit hasn't been evaluated for quality as fully as Tangle, but reduced-word-size variants show it should have a full
 * period of 2 to the 128.
 * <br>
 * Created by Tommy Ettinger on 7/9/2018.
 */
public final class TangleRNG implements RandomnessSource, SkippingRandomness, Serializable {
    private static final long serialVersionUID = 4L;
    /**
     * Can be any long value.
     */
    private long stateA;

    /**
     * Must be odd.
     */
    private long stateB;

    /**
     * Creates a new generator seeded using Math.random.
     */
    public TangleRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public TangleRNG(long seed) {
        stateA = (seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25;
        stateB = ((seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25) | 1L;
    }

    public TangleRNG(final long seedA, final long seedB) {
        stateA = seedA;
        stateB = seedB | 1L;
    }

    /**
     * Get the "A" part of the internal state as a long.
     *
     * @return the current internal "A" state of this object.
     */
    public long getStateA() {
        return stateA;
    }

    /**
     * Set the "A" part of the internal state with a long.
     *
     * @param stateA a 64-bit long
     */
    public void setStateA(long stateA) {
        this.stateA = stateA;
    }

    /**
     * Get the "B" part of the internal state as a long.
     *
     * @return the current internal "B" state of this object.
     */
    public long getStateB() {
        return stateB;
    }

    /**
     * Set the "B" part of the internal state with a long; the least significant bit is ignored (will always be odd).
     *
     * @param stateB a 64-bit long
     */
    public void setStateB(long stateB) {
        this.stateB = stateB | 1L;
    }

    /**
     * Using this method, any algorithm that might use the built-in Java Random
     * can interface with this randomness source.
     *
     * @param bits the number of bits to be returned
     * @return the integer containing the appropriate number of bits
     */
    @Override
    public final int next(final int bits) {
        final long s = (stateA += 0x6C8E9CF570932BD5L);
        final long z = (s ^ (s >>> 25)) * (stateB += 0x9E3779B97F4A7C16L);
        return (int)(z ^ (z >>> 22)) >>> (32 - bits);
    }
    /**
     * Using this method, any algorithm that needs to efficiently generate more
     * than 32 bits of random data can interface with this randomness source.
     * <p>
     * Get a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive).
     *
     * @return a random long between Long.MIN_VALUE and Long.MAX_VALUE (both inclusive)
     */
    @Override
    public final long nextLong() {
        final long s = (stateA += 0x6C8E9CF570932BD5L);
        final long z = (s ^ s >>> 25) * (stateB += 0x9E3779B97F4A7C16L);
        return z ^ z >>> 22;
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public TangleRNG copy() {
        return new TangleRNG(stateA, stateB);
    }
    @Override
    public String toString() {
        return "TangleRNG with stateA 0x" + StringKit.hex(stateA) + "L and stateB 0x" + StringKit.hex(stateB) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TangleRNG tangleRNG = (TangleRNG) o;

        return stateA == tangleRNG.stateA && stateB == tangleRNG.stateB;
    }

    @Override
    public int hashCode() {
        return (int) (31L * (stateA ^ (stateA >>> 32)) + (stateB ^ stateB >>> 32));
    }

    /**
     * Advances or rolls back the SkippingRandomness' state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to {@link #nextLong()},
     * and returns the random number produced at that step. Negative numbers can be used to step backward, or 0 can be
     * given to get the most-recently-generated long from {@link #nextLong()}.
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most-recently-generated number
     * @return the random long generated after skipping forward or backwards by {@code advance} numbers
     */
    @Override
    public long skip(long advance) {
        final long s = (stateA += 0x6C8E9CF570932BD5L * advance);
        final long z = (s ^ (s >>> 25)) * (stateB += 0x9E3779B97F4A7C16L * advance);
        return z ^ (z >>> 22);
    }

    public static long determine(final long state)
    {
        long s = (state * 0x6C8E9CF570932BD5L);
        s = (s ^ s >>> 26) * (state * 0x9E3779B97F4A7C15L | 1L);
        return s ^ s >>> 24;
    }
    public static int determineBounded(final long state, final int bound)
    {
        long s = (state * 0x6C8E9CF570932BD5L);
        s = (s ^ s >>> 26) * (state * 0x9E3779B97F4A7C15L | 1L);
        return (int)((bound * ((s ^ s >>> 24) & 0x7FFFFFFFL)) >> 31);
    }
    public static float determineFloat(final long state)
    {
        long s = (state * 0x6C8E9CF570932BD5L);
        s = (s ^ s >>> 26) * (state * 0x9E3779B97F4A7C15L | 1L);
        return (s >>> 40) * 0x1p-24f;
    }
    public static double determineDouble(final long state)
    {
        long s = (state * 0x6C8E9CF570932BD5L);
        s = (s ^ s >>> 26) * (state * 0x9E3779B97F4A7C15L | 1L);
        return ((s ^ s >>> 24) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    public static long determine(long stateA, final long stateB)
    {
        return (stateA = ((stateA *= 0x6C8E9CF570932BD5L) ^ stateA >>> 26) * (stateB * 0x9E3779B97F4A7C15L | 1L)) ^ stateA >>> 24;
    }
    public static int determineBounded(long stateA, final long stateB, final int bound)
    {
        stateA *= 0x6C8E9CF570932BD5L;
        stateA = (stateA ^ stateA >>> 26) * (stateB * 0x9E3779B97F4A7C15L | 1L);
        return (int)((bound * ((stateA ^ stateA >>> 24) & 0x7FFFFFFFL)) >> 31);
    }
    public static float determineFloat(long stateA, final long stateB)
    {
        stateA *= 0x6C8E9CF570932BD5L;
        stateA = (stateA ^ stateA >>> 26) * (stateB * 0x9E3779B97F4A7C15L | 1L);
        return (stateA >>> 40) * 0x1p-24f;
    }
    public static double determineDouble(long stateA, final long stateB)
    {
        stateA *= 0x6C8E9CF570932BD5L;
        stateA = (stateA ^ stateA >>> 26) * (stateB * 0x9E3779B97F4A7C15L | 1L);
        return ((stateA ^ stateA >>> 24) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }
}
