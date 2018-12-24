package squidpony.squidmath;

import java.io.Serializable;

/**
 * An alteration to a RandomnessSource that attempts to produce values that are perceived as fair to an imperfect user.
 * <p>
 * This takes a RandomnessSource, defaulting to a DiverRNG, and uses it to generate random values, but tracks the total
 * and compares it to the potential total of a generator of only numbers with a desired value (default 0.54,
 * so it compares against a sequence of all 0.54). If the current generated total is too high or low compared to the
 * desired total, the currently used seed is possibly changed, the generated number is moved in the direction of the
 * desired fairness, and it returns that instead of the number that would have pushed the current generated total
 * beyond the desired threshold. The new number, if one is changed, will always be closer to the desired fairness.
 * This is absolutely insecure for cryptographic purposes, but should seem more "fair" to a player than a
 * random number generator that seeks to be truly random.
 * You can create multiple DharmaRNG objects with different fairness values and seeds, and use favorable generators
 * (with fairness greater than 0.54) for characters that need an easier time, or unfavorable generators if you want
 * the characters that use that RNG to be impeded somewhat.
 * The name comes from the Wheel of Dharma.
 * This class currently will have a slight bias toward lower numbers with many RNGs unless fairness is tweaked; 0.54
 * can be used as a stand-in because 0.5 leans too low.
 *
 * <p>
 * You can get values from this generator with: {@link #nextDouble()}, {@link #nextInt()},
 *   {@link #nextLong()}, and the bounded variants on each of those.
 * <p>
 * You can alter the tracking information or requested fairness with {@link #resetFortune()},
 *   {@link #setFairness(double)}, and {@link #getFairness()}.
 *
 * Created by Tommy Ettinger on 5/2/2015.
 */
public class DharmaRNG extends RNG implements Serializable{

	/** Used to tweak the generator toward high or low values. */
    private double fairness = 0.54;

    /** Running total for what this has actually produced. */
    private double produced = 0.0;

    /** Running total for what this would produce if it always produced a value equal to fairness. */
    private double baseline = 0.0;

	private static final long serialVersionUID = -8919455766853811999L;

    /**
     * Constructs a DharmaRNG with a pseudo-random seed from Math.random().
     */
    public DharmaRNG()
    {
        this((long)(Math.random() * ((1L << 50) - 1)));
    }
    /**
     * Construct a new DharmaRNG with the given seed.
     *
     * @param seed used to seed the default RandomnessSource.
     */
    public DharmaRNG(final long seed) {
        super(seed);
    }

    /**
     * Construct a new DharmaRNG with the given seed.
     *
     * @param seed used to seed the default RandomnessSource.
     * @param fairness the desired fairness metric, which must be between 0.0 and 1.0
     */
    public DharmaRNG(final long seed, final double fairness) {
        super(seed);
        if(fairness < 0.0 || fairness >= 1.0)
            this.fairness = 0.54;
        else
            this.fairness = fairness;
    }


    /**
     * String-seeded constructor; uses a platform-independent hash of the String (it does not use String.hashCode) as a
     * seed for DiverRNG, which is of high quality, but low period (which rarely matters for games), and has good speed,
     * tiny state size, and excellent 64-bit number generation.
     *
     * @param seedString a String as a seed
     */
    public DharmaRNG(CharSequence seedString) {
        super(seedString);
    }


    /**
     * String-seeded constructor; uses a platform-independent hash of the String (it does not use String.hashCode) as a
     * seed for DiverRNG, which is of high quality, but low period (which rarely matters for games), and has good speed,
     * tiny state size, and excellent 64-bit number generation.
     *
     * @param seedString a String as a seed
     */
    public DharmaRNG(String seedString, double fairness) {
        super(seedString);
        if(fairness < 0.0 || fairness >= 1.0)
            this.fairness = 0.54;
        else
            this.fairness = fairness;

    }
    /**
     * Construct a new DharmaRNG with the given seed.
     *
     * @param rs the implementation used to generate random bits.
     */
    public DharmaRNG(final RandomnessSource rs) {
        super(rs);
    }
    /**
     * Construct a new DharmaRNG with the given seed.
     *
     * @param rs the implementation used to generate random bits.
     * @param fairness the desired fairness metric, which must be between 0.0 and 1.0
     */
    public DharmaRNG(final RandomnessSource rs, final double fairness) {
        super(rs);
        if(fairness < 0.0 || fairness >= 1.0)
            this.fairness = 0.54;
        else
            this.fairness = fairness;
    }

    /**
     * Generate a random double, altering the result if recently generated results have been leaning
     * away from this class' fairness value.
     * @return a double between 0.0 (inclusive) and 1.0 (exclusive)
     */
    @Override
    public double nextDouble() {
        double gen = (random.nextLong() & 0x1fffffffffffffL) * 0x1p-53;
        /*if(Math.abs((produced + gen) - (baseline + fairness)) > 1.5) {
            //do some reseeding here if possible
        }*/
        if(Math.abs((produced + gen) - (baseline + fairness)) > 0.5)
        {
            gen = (gen + fairness) / 2.0;
            produced *= 0.5;
            baseline *= 0.5;
            produced += gen;
            baseline += fairness;
            return gen;
        }
        else
        {
            produced += gen;
            baseline += fairness;
            return gen;
        }
    }

    /**
     * Returns a random integer below the given bound, or 0 if the bound is 0 or
     * negative. Affects the current fortune.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    @Override
    public int nextInt(int bound) {
        if (bound <= 0) {
            return 0;
        }

        return (int)(nextDouble() * bound);
    }

    /**
     * Returns a random integer, which may be any positive or negative value. Affects the current fortune.
     * @return A random int (can be any int, without restriction)
     */
    @Override
    public int nextInt() {
        return (int)((nextDouble() - 0.5) * 2.0 * 0x7FFFFFFF);
    }

    /**
     * Returns a random long, which may be any positive or negative value. Affects the current fortune.
     * @return A random long (can be any long, without restriction)
     */
    @Override
    public long nextLong() {
        return (long)((nextDouble() - 0.5) * 2.0 * 0x7FFFFFFFFFFFFFFFL);
    }

    /**
     * Returns a random long below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    @Override
	public long nextLong(long bound) {
        if (bound <= 0) {
            return 0;
        }
        return (long)(nextDouble() * bound);
    }
    /**
     * Gets the measure that this class uses for RNG fairness, defaulting to 0.54 (always between 0.0 and 1.0).
     * @return the current fairness metric.
     */
    public double getFairness() {
        return fairness;
    }

    /**
     * Sets the measure that this class uses for RNG fairness, which must always be between 0.0 and 1.0, and will be
     * set to 0.54 if an invalid value is passed.
     * @param fairness the desired fairness metric, which must be 0.0 &lt;= fairness &lt; 1.0
     */
    public void setFairness(double fairness) {
        if(fairness < 0.0 || fairness >= 1.0)
            this.fairness = 0.54;
        else
            this.fairness = fairness;
    }

    /**
     * Gets the status of the fortune used when calculating fairness adjustments.
     * @return the current value used to determine whether the results should be adjusted toward fairness.
     */
    public double getFortune()
    {
        return Math.abs(produced - baseline);
    }

    /**
     * Resets the stored history this RNG uses to try to ensure fairness.
     */
    public void resetFortune()
    {
        produced = 0.0;
        baseline = 0.0;
    }
    /**
     *
     * @param bits the number of bits to be returned
     * @return a random int of the number of bits specified.
     */
    @Override
    public int next(int bits) {
        if(bits <= 0)
            return 0;
        if(bits > 32)
            bits = 32;
        return (int)(nextDouble() * (1l << bits));

    }

    /**
     * Returns a value between min (inclusive) and max (exclusive).
     * <p>
     * The inclusive and exclusive behavior is to match the behavior of the
     * similar method that deals with floating point values.
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    @Override
    public long between(long min, long max) {
        return min + nextLong(max - min);
    }

    /**
     * Returns a random non-negative integer below the given bound, or 0 if the bound is 0.
     * Uses a slightly optimized technique. This method is considered "hasty" since
     * it should be faster than nextInt() doesn't check for "less-valid" bounds values. It also
     * has undefined behavior if bound is negative, though it will probably produce a negative
     * number (just how negative is an open question).
     *
     * @param bound the upper bound (exclusive); behavior is undefined if bound is negative
     * @return the found number
     */
    @Override
    public int nextIntHasty(int bound) {
        return (int)(nextDouble() * bound);
    }

    @Override
    public float nextFloat() {
        return (float)nextDouble();
    }

    @Override
    public boolean nextBoolean() {
        return nextDouble() >= 0.5;
    }

    @Override
    public RandomnessSource getRandomness() {
        return random;
    }

    @Override
    public void setRandomness(RandomnessSource random) {
        this.random = random;
    }

    /**
     * Creates a copy of this DharmaRNG; it will generate the same random numbers, given the same calls in order, as
     * this DharmaRNG at the point copy() is called. The copy will not share references with this DharmaRNG.
     *
     * @return a copy of this DharmaRNG
     */
    @Override
    public DharmaRNG copy() {
        DharmaRNG next = new DharmaRNG(random.copy(), fairness);
        next.produced = produced;
        next.baseline = baseline;
        return next;
    }

    @Override
    public String toString() {
        return "DharmaRNG{" +
                "fairness=" + fairness +
                ", produced=" + produced +
                ", baseline=" + baseline +
                ", Randomness Source=" + random +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DharmaRNG dharmaRNG = (DharmaRNG) o;

        if (Double.compare(dharmaRNG.fairness, fairness) != 0) return false;
        return Double.compare(dharmaRNG.produced, produced) == 0 && Double.compare(dharmaRNG.baseline, baseline) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = NumberTools.doubleToLongBits(fairness);
        result = (int) (temp ^ (temp >>> 32));
        temp = NumberTools.doubleToLongBits(produced);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = NumberTools.doubleToLongBits(baseline);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
    /**
     * Returns this DharmaRNG in a way that can be deserialized even if only {@link IRNG}'s methods can be called.
     * @return a {@link Serializable} view of this DharmaRNG; always {@code this}
     */
    @Override
    public Serializable toSerializable() {
        return this;
    }

}