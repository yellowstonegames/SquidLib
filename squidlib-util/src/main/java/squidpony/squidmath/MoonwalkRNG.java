package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;

/**
 * An IRNG implementation that allows the extra functionality of a StatefulRandomness and a SkippingRandomness, as well
 * as allowing reverse-lookup of the state that produced a long using the static {@link #inverseNextLong(long)} method,
 * and distance checks between two generated numbers with the static {@link #distance(long, long)} method. A task this
 * might be useful for could be simple obfuscation that is hard to undo unless you know the starting state, like this:
 * <ol>
 *     <li>take a sequence of numbers or characters and a MoonwalkRNG with a given starting state,</li>
 *     <li>modify each item in the sequence with a random but reversible change such as a bitwise XOR
 *     with a number produced by the MoonwalkRNG (such as by {@link #nextInt()}),</li>
 *     <li>on a later run, take the modified sequence and a MoonwalkRNG with the same starting state (but no direct
 *     access to the starting sequence), and skip ahead by the length of the sequence with {@link #skip(long)},</li>
 *     <li>starting at the end of the sequence, apply the reverse change to the items with numbers generated
 *     <b>backwards</b> by MoonwalkRNG with {@link #previousInt()} (such as a XOR if the number was originally modified
 *     with a XOR or an addition if it was originally modified with a subtraction),</li>
 *     <li>when the full sequence has been reversed, you now have the original sequence again./li>
 * </ol>
 * This is also possible with determine() methods in various RandomnessSource implementations, but those require some
 * extra work to allow them to use sequential inputs instead of inputs that have a large difference between generations.
 * <br>
 * Internally, this is like {@link StatefulRNG} if it always used {@link LightRNG} and allowed access to LightRNG's
 * skip() method as well as the reverse lookup and distance methods that aren't in LightRNG but are allowed by it.
 * <br>
 * The name comes from the ability of this generator to easily go in reverse, like the moonwalk dance move, including
 * {@link #previousLong()} and {@link #skip(long)} for advancing backwards, but also {@link #inverseNextLong(long)} to
 * go from output back to state.
 * <br>
 * Created by Tommy Ettinger on 4/14/2018.
 */
public class MoonwalkRNG extends AbstractRNG implements StatefulRandomness, SkippingRandomness, Serializable {
    private static final long serialVersionUID = 1L;

    private long state;
    /**
     * Default constructor; uses a random seed.
     */
    public MoonwalkRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    /**
     * Constructs a MoonwalkRNG with the given seed as-is; any seed can be given.
     * @param seed any long
     */
    public MoonwalkRNG(long seed) {
        state = seed;
    }

    /**
     * String-seeded constructor; uses a platform-independent hash of the String (it does not use String.hashCode) as a
     * seed for this RNG.
     * @param seedString any CharSequence, such as a String or StringBuilder; if null this will use the seed 0
     */
    public MoonwalkRNG(CharSequence seedString) {
        this(CrossHash.hash64(seedString));
    }

    /**
     * Get up to 32 bits (inclusive) of random output; the int this produces
     * will not require more than {@code bits} bits to represent.
     *
     * @param bits an int between 1 and 32, both inclusive
     * @return a random number that fits in the specified number of bits
     */
    @Override
    public int next(int bits) {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (int)(z ^ (z >>> 31)) >>> (32 - bits);
    }

    /**
     * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both inclusive).
     *
     * @return a 32-bit random int.
     */
    @Override
    public int nextInt() {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (int)(z ^ (z >>> 31));
    }

    /**
     * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive).
     *
     * @return a 64-bit random long.
     */
    @Override
    public long nextLong() {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
    /**
     * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both inclusive), but advances the state
     * "backwards," such that calling {@link #nextInt()} alternating with this method will return the same pair of
     * numbers for as long as you keep alternating those two calls. This can be useful with {@link #skip(long)} when it
     * advances ahead by a large amount and you want to step backward to reverse another set of forward-advancing number
     * generations that had been done by other code.
     *
     * @return a 32-bit random int.
     */
    public int previousInt() {
        long z = state -= 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (int)(z ^ (z >>> 31));
    }

    /**
     * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive), but advances the state
     * "backwards," such that calling {@link #nextLong()} alternating with this method will return the same pair of
     * numbers for as long as you keep alternating those two calls. This can be useful with {@link #skip(long)} when it
     * advances ahead by a large amount and you want to step backward to reverse another set of forward-advancing number
     * generations that had been done by other code.
     *
     * @return a 64-bit random long.
     */
    public long previousLong() {
        long z = state -= 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (z ^ (z >>> 31));
    }


    /**
     * Get a random bit of state, interpreted as true or false with approximately equal likelihood.
     * <br>
     * This implementation uses a sign check and is able to avoid some calculations needed to get a full int or long.
     *
     * @return a random boolean.
     */
    @Override
    public boolean nextBoolean() {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        return ((z ^ (z >>> 27)) * 0x94D049BB133111EBL) < 0;
    }

    /**
     * Gets a random double between 0.0 inclusive and 1.0 exclusive.
     * This returns a maximum of 0.9999999999999999 because that is the largest double value that is less than 1.0 .
     *
     * @return a double between 0.0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    @Override
    public double nextDouble() {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return ((z ^ (z >>> 31)) & 0x1fffffffffffffL) * 0x1p-53;
    }

    /**
     * Gets a random float between 0.0f inclusive and 1.0f exclusive.
     * This returns a maximum of 0.99999994 because that is the largest float value that is less than 1.0f .
     *
     * @return a float between 0f (inclusive) and 0.99999994f (inclusive)
     */
    @Override
    public float nextFloat() {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return ((z ^ (z >>> 31)) & 0xffffffL) * 0x1p-24f;
    }

    /**
     * Creates a copy of this MoonwalkRNG; it will generate the same random numbers, given the same calls in order, as
     * this MoonwalkRNG at the point copy() is called. The copy will not share references with this MoonwalkRNG.
     * @return a copy of this IRNG
     */
    @Override
    public MoonwalkRNG copy() {
        return new MoonwalkRNG(state);
    }

    /**
     * Gets a view of this IRNG in a way that implements {@link Serializable}, which may simply be this IRNG if it
     * implements Serializable as well as IRNG.
     * <br>
     * For implementors: It is suggested to return an {@link RNG} initialized by calling
     * {@link RNG#RNG(long)} with {@link #nextLong()} if you are unable to save the current state of this IRNG and the
     * caller still needs something saved. This won't preserve the current state or the choice of IRNG implementation,
     * however, so it is simply a last resort in case you don't want to throw an exception.
     *
     * @return a {@link Serializable} view of this IRNG or a similar one; may be {@code this}
     */
    @Override
    public Serializable toSerializable() {
        return this;
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
        long z = (state += 0x9E3779B97F4A7C15L * advance);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object as a long
     */
    @Override
    public long getState() {
        return state;
    }

    /**
     * Set the current internal state of this StatefulRandomness with a long; all longs are allowed.
     *
     * @param state a 64-bit long; this can be any long, even 0
     */
    @Override
    public void setState(long state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "MoonwalkRNG with state 0x" + StringKit.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoonwalkRNG moonwalkRNG = (MoonwalkRNG) o;

        return state == moonwalkRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }


    /**
     * Given the output of a call to {@link #nextLong()} as {@code out}, this finds the state of the MoonwalkRNG that
     * produce that output. If you set the state of a MoonwalkRNG with {@link #setState(long)} to the result of this
     * method and then call {@link #nextLong()} on it, you should get back {@code out}.
     * <br>
     * This isn't as fast as {@link #nextLong()}, but both run in constant time. Some random number generators take more
     * than constant time to reverse, so one was chosen for this class that would still be efficient ({@link LightRNG}).
     * <br>
     * This will not necessarily work if out was produced by a generator other than a MoonwalkRNG, or if it was produced
     * with the bounded {@link #nextLong(long)} method by any generator.
     * @param out a long as produced by {@link #nextLong()}, without changes
     * @return the state of the RNG that will produce the given long
     */
    public static long inverseNextLong(long out)
    {
        out ^= out >>> 31;
        out ^= out >>> 62;
        out *= 0x319642B2D24D8EC3L;
        out ^= out >>> 27;
        out ^= out >>> 54;
        out *= 0x96DE1B173F119089L;
        out ^= out >>> 30;
        return (out ^ out >>> 60) - 0x9E3779B97F4A7C15L;
        //0x96DE1B173F119089L 0x319642B2D24D8EC3L 0xF1DE83E19937733DL
    }

    /**
     * Returns the number of steps (where a step is equal to one call to most random number methods in this class)
     * needed to go from receiving out1 from a MoonwalkRNG's {@link #nextLong()} method to receiving out2 from another
     * call. This number can be used with {@link #skip(long)} to move a MoonwalkRNG forward or backward by the desired
     * distance.
     * @param out1 a long as produced by {@link #nextLong()}, without changes
     * @param out2 a long as produced by {@link #nextLong()}, without changes
     * @return the number of calls to {@link #nextLong()} that would be required to go from producing out1 to producing
     *         out2; can be positive or negative, and can be passed to {@link #skip(long)}
     */
    public static long distance(final long out1, final long out2)
    {
        return (inverseNextLong(out2) - inverseNextLong(out1)) * 0xF1DE83E19937733DL;
    }
}
