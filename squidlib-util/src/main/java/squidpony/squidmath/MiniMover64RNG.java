package squidpony.squidmath;

import squidpony.StringKit;
import java.io.Serializable;

/**
 * The fastest generator in this library on desktop JVMs; one of Mark Overton's subcycle generators from
 * <a href="http://www.drdobbs.com/tools/229625477">this article</a>, specifically a CMR with a 64-bit state, that has
 * its result multiplied by a constant. Its period is unknown, and it has multiple subcycles with different period
 * lengths, but one is at the very least 2 to the 42, since the generator passes PractRand after generating that many
 * 64-bit integers (it passes 32TB with no anomalies), and all states that can be produced using {@link #seed(int)} have
 * a minimum period of 2 to the 20, or roughly one million. It also can pass TestU01's BigCrush suite in both forward
 * and reversed bit order, though not for all seeds (as expected).
 * <br>
 * Notably, this generator's {@link #nextLong()} method is extremely small (as are all of the methods that use it as a
 * basis), which may help with inlining decisions for HotSpot. Generating the next step just needs a bitwise rotation of
 * the current state, multiplying the result by a 32-bit constant, and assigning that to state. Generating a long after
 * that only needs a multiplication by another constant, which doesn't have the issues with reversed-bits testing that
 * some other generators do when they multiply as their only output step (xorshift128 notably has patterns in its low
 * bits, so multiplying by a constant doesn't help those bits, but the CMR generator here has no such low-bit problems).
 * This means only three math instructions need to be performed to get a new random number (bitwise rotate left,
 * multiply, multiply), which is extremely low for a high-quality generator. While the CMR state change does not
 * pipeline well, the ease of calculating a complete number seems to make up for it on desktop JVMs.
 * <br>
 * The choice of constants for the multipliers and for the rotation needs to be carefully verified; earlier choices came
 * close to failing PractRand at 8TB (and were worsening, so were likely to fail at 16TB), but this set of constants has
 * higher quality in testing. Other constants did well in PractRand but had failures in TestU01 (even down to SmallCrush
 * when reversed). For transparency, the constants used in this version:
 * <ul>
 * <li>The state multiplier is 0xAC564B05L (or 2891336453L), which is one of the constants evaluated in Tables of Linear
 * Congruential Generators of Different Sizes and Good Lattice Structure, by Pierre L'Ecuyer, to be optimal for an LCG
 * with a modulus of 2 to the 32 and an odd addend (this doesn't have an addend, but it isn't an LCG either).</li>
 * <li>The post-processing multiplier is 0x818102004182A025L (or -9115001970698837979L), which is a probable prime with
 * a low Hamming weight (14 bits of 64 are set), in the hope it will perform well speed-wise. This number doesn't seem
 * as critical to the quality of the generator, and some other multipliers probably work just as well.</li>
 * <li>A left rotation constant of 29, which was chosen because it seems to allow the generator to pass certain
 * TestU01 statistical tests, such as Birthday Spacings, where most other rotations do not.</li>
 * </ul>
 * <br>
 * This is a RandomnessSource but not a StatefulRandomness because it needs to take care and avoid seeds that would put
 * it in a short-period subcycle. One long test brute-force checked all seeds from 1 to {@code Math.pow(2, 25)} and
 * validated that each of their periods is at least {@code Math.pow(2, 20) - 1}. This means that as long as a period
 * as low as 1 million is rarely allowed, a starting state can be quickly chosen from a 32-bit int by using the bottom
 * 25 bits of the int (plus 1, to disallow the 0 state) and using the remaining 7 bits to step up to 127 times through
 * the generator.
 * <br>
 * The name comes from M. Overton, who discovered this category of subcycle generators, and also how this generator can
 * really move when it comes to speed. This generator has less state than {@link Mover64RNG}, has a shorter period than
 * it, and is faster than it in all aspects.
 * <br>
 * Created by Tommy Ettinger on 11/26/2018.
 * @author Mark Overton
 * @author Tommy Ettinger
 */
public final class MiniMover64RNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 2L;
    private long state;

    /**
     * Calls {@link #seed(int)} with a random int value (obtained using {@link Math#random()}).
     */
    public MiniMover64RNG()
    {
        seed((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    /**
     * The recommended constructor, this guarantees the generator will have a period of at least 2 to the 20 (roughly
     * one million, but most if not all initial states will have much longer periods). All ints are permissible values
     * for {@code state}.
     * @param state any int; will be used to get the actual state used in the generator (which is a long internally)
     */
    public MiniMover64RNG(final int state)
    {
        seed(state);
    }

    /**
     * Not advised for external use; prefer {@link #MiniMover64RNG(int)} because it guarantees a good subcycle. This
     * constructor allows all subcycles to be produced, including ones with a shorter period.
     * @param state the state to use, exactly unless it is 0 (then this uses 1)
     */
    public MiniMover64RNG(final long state)
    {
        this.state = state == 0L ? 1L : state;
    }

    /**
     * Seeds the state using all bits of the given int {@code s}. Between 33554432 and 4294967296 seeds are possible,
     * with the actual count probably much closer to 4294967296. This treats the bottom 25 bits of {@code s} (plus 1, to
     * avoid a seed of 0) as the starting point and then generates the next state at most 127 times, with
     * each generated state taking less time than {@link #nextLong()}. Some of the starting states are entirely possible
     * to be within 127 steps of another starting state, so not all seeds are necessarily unique. This is not
     * guaranteed to put the generator on an optimal subcycle, but it is guaranteed that any subcycle will have a period
     * of at least 1048575.
     * @param s all bits are used, none verbatim (0 is tolerated)
     */
    public final void seed(final int s) {
        long v = (s & 0x1FFFFFF) + 1L; // at least 2 to the 25 sequential seeds have periods of at least 1048575.
        for (int i = s >>> 25; i > 0; i--) {
            v = (v << 29 | v >>> 35) * 0xAC564B05L;
        }
        state = v;
    }

    public final int nextInt()
    {
        return (int)((state = (state << 29 | state >>> 35) * 0xAC564B05L) * 0x818102004182A025L);
    }
    @Override
    public final int next(final int bits)
    {
        return (int)((state = (state << 29 | state >>> 35) * 0xAC564B05L) * 0x818102004182A025L) >>> (32 - bits);
    }
    @Override
    public final long nextLong() {

        return (state = (state << 29 | state >>> 35) * 0xAC564B05L) * 0x818102004182A025L;
//        return (state = (state << 21 | state >>> 43) * 0x9E3779B9L) * 0x41C64E6DL; // earlier, fails some of TestU01
    }

    /**
     * Gets a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive).
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive)
     */
    public final double nextDouble() {
        return ((state = (state << 29 | state >>> 35) * 0xAC564B05L) * 0x818102004182A025L & 0x1fffffffffffffL) * 0x1p-53;
    }

    /**
     * Gets a pseudo-random float between 0.0f (inclusive) and 1.0f (exclusive).
     * @return a pseudo-random float between 0.0f (inclusive) and 1.0f (exclusive)
     */
    public final float nextFloat() {
        return ((state = (state << 29 | state >>> 35) * 0xAC564B05L) * 0x818102004182A025L & 0xffffffL) * 0x1p-24f;
    }

    /**
     * Produces a copy of this MiniMover64RNG that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this MiniMover64RNG
     */
    @Override
    public MiniMover64RNG copy() {
        return new MiniMover64RNG(state);
    }

    /**
     * Gets the state; if this generator was set with {@link #MiniMover64RNG()},
     * {@link #MiniMover64RNG(int)}, or {@link #seed(int)}, then this will be on a good subcycle, otherwise it
     * may not be. 
     * @return the state, a long
     */
    public long getState()
    {
        return state;
    }

    /**
     * Sets the state to any long, which may put the generator in a low-period subcycle.
     * Use {@link #seed(int)} to guarantee a good subcycle.
     * @param state any int
     */
    public void setState(final long state)
    {
        this.state = state == 0L ? 1L : state;
    }

    @Override
    public String toString() {
        return "MiniMover64RNG with state 0x" + StringKit.hex(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MiniMover64RNG miniMover64RNG = (MiniMover64RNG) o;

        return state == miniMover64RNG.state;
    }

    @Override
    public int hashCode() {
        return (int)(state ^ state >>> 32);
    }
}
