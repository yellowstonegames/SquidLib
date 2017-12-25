package squidpony.squidmath;

import java.io.Serializable;

/**
 * This implements the random number function from the "Random Number" XKCD comic, with minor compatibility changes.
 * https://xkcd.com/221/ See explanation here: https://www.explainxkcd.com/wiki/index.php/221:_Random_Number
 * The period of this generator is optimal for its state size. Its state size is 0 bits. Thus, its period is 1.
 * <br>
 * Perhaps this is a good time to point out that none of our RandomnessSource implementations are truly random, but some
 * may be closer to random than others... or further from it.
 * <br>
 * Primarily useful for benchmarking, when you want to test the absolute minimum work a RandomnessSource can do as a
 * control group in a larger benchmark.
 */
public final class XkcdRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 4L;
    /**
     * Constructs an XkcdRNG with the default state (0 bits) and corresponding qualities of randomness (period of 1).
     */
    public XkcdRNG() {
    }

    /**
     * This takes a seed for some reason, but doesn't use it. I mean, why should XkcdRNG have to concern itself with
     * deterministic seeding using a user input, when it already has determinism down pat?
     * @param seed whatever you want it to be
     */
    public XkcdRNG(final long seed) {
    }

    /**
     * Gets a random value that fits in the given number of bits. It doesn't actually use the bits parameter, so
     * technically you could request a negative bit count, or zero bits, and it will still give you "the loneliest
     * number" every time.
     * @param bits the number of bits to be returned; should usually be between 1 and 32 but I've stopped caring
     * @return Ein, uno, un, whatever you want to call it
     */
    @Override
    public int next(int bits) {
        return 1;   // chosen by fair dice roll.
        // guaranteed to be random.
    }

    /**
     * Gets a random long. A, singular. The number returned will always be coprime to all of:
     * <ul>
     *     <li>the number of fingers the user has, regardless of user (note: this includes non-human users),</li>
     *     <li>the number of Twitter followers the developer writing the code has,</li>
     *     <li>the number of protons in this universe,</li>
     *     <li>420,</li>
     *     <li>the additive identity of the set of all integers,</li>
     *     <li>the answer to the question of life, the universe, and everything,</li>
     *     <li>and all integers in the set of all integers, why not.</li>
     * </ul>
     * @return the loneliest number, as a long
     */
    @Override
    public long nextLong() {
        return 1L;   // chosen by fair dice roll.
        // guaranteed to be random.
    }

    /**
     * State? What state? This returns itself as a copy because it can.
     * @return this XkcdRNG
     */
    @Override
    public RandomnessSource copy() {
        return this;
    }

    /**
     * Gets a random int. A, singular. The number returned will always be coprime to all of:
     * <ul>
     *     <li>the number of fingers the user has, regardless of user (note: this includes non-human users),</li>
     *     <li>the number of Twitter followers the developer writing the code has,</li>
     *     <li>the number of protons in this universe,</li>
     *     <li>420,</li>
     *     <li>the additive identity of the set of all integers,</li>
     *     <li>the answer to the question of life, the universe, and everything,</li>
     *     <li>and all integers in the set of all integers, why not.</li>
     * </ul>
     * @return the loneliest number, as an int
     */
    public int nextInt() {
        return 1;   // chosen by fair dice roll.
        // guaranteed to be random.
    }

    /**
     * Normally this would return a random number between lower and upper, but because actually generating a random
     * number seems kinda like it would require the computer to do extra work, this just returns lower. That's easier.
     *
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, discarded, unused, we don't care what it wants to restrict us to
     * @return lower, as-is
     */
    public int nextInt(final int lower, final int upper) {
        return lower;
    }

    /**
     * Returns the probability of three independent, fair coin flips of the same coin all resulting in the same outcome.
     * @return A quarter, as a double.
     */
    public double nextDouble() {
        return 0x4p-4;   // chosen by fair "dice" roll.
        // guaranteed to be random.
    }

    /**
     * Returns outer times the probability of three independent, fair coin flips of the same coin all resulting in the
     * same outcome.
     * @return A quarter of outer, as a double.
     */
    public double nextDouble(final double outer) {
        return 0x4p-4 * outer;
    }

    /**
     * Returns the probability of three independent, fair coin flips of the same coin all resulting in the same outcome.
     * @return A quarter, as a float.
     */
    public float nextFloat() {
        return 0x4p-4f;   // chosen by fair "dice" roll.
        // guaranteed to be random.
    }

    /**
     * Gets a boolean answer to this question, when I ask it of my dog: "Do you want to go for a WALK!?!?"
     *
     * @return a highly uncertain boolean value. I mean, cosmic rays could hit your computer as you run this.
     */
    public boolean nextBoolean() {
        return true;   // chosen by fair coin flip.
        // guaranteed to be random.
    }

    @Override
    public String toString() {
        return "XKCD Random Number Generator";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return  (o != null && getClass() == o.getClass());
    }

    @Override
    public int hashCode() {
        return 4;
    }
}
