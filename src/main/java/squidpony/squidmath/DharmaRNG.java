package squidpony.squidmath;

/**
 * A Quasi-Random number generator that attempts to produce values that are perceived as fair to an imperfect user.
 * <p>
 * This uses the values from a 4D Sobol sequence, only using one of the dimensions at a time, but tracking the total
 * and comparing it to the potential total of a generator of only-average numbers (this generates doubles internally,
 * so it compares against a sequence of all 0.5). If the current generated total is too high or low compared to the
 * average total, the currently used seed is possibly changed, the generated number is moved in the direction of the
 * desired fairness, and it returns that instead of the number that would have pushed the current generated total
 * beyond the desired threshold. The new number, if one is changed, will always be closer to the desired fairness.
 * This is absolutely insecure for cryptographic purposes, but should seem more "fair" to a player than a
 * random number generator that seeks to be truly random.
 * You can create multiple DharmaRNG objects with different fairness values and seeds, and use favorable generators
 * (with fairness greater than 0.5) for characters that need an easier time, or unfavorable generators if you want
 * the characters that use that RNG to be impeded somewhat.
 * The name comes from the Wheel of Dharma, since this rotates the bits of the seed like a wheel.
 * This class currently will have a slight bias toward lower numbers unless fairness is tweaked; 0.54 is used now
 * because 0.5 leans too low.
 *
 * <p>
 * The implementation already comes with support for up to 1000 dimensions with direction numbers
 * calculated from <a href="http://web.maths.unsw.edu.au/~fkuo/sobol/">Stephen Joe and Frances Kuo</a>.
 * <p>
 * You can get values from this generator with: {@link #nextDouble()}, {@link #nextInt()},
 *   {@link #nextLong()}, and the bounded variants on each of those.
 * <p>
 * You can alter the tracking information or requested fairness with {@link #resetFortune()}, {@link #reseed(int)},
 *   {@link #setFairness(double)}, and {@link #getFairness()}.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Sobol_sequence">Sobol sequence (Wikipedia)</a>
 * @see <a href="http://web.maths.unsw.edu.au/~fkuo/sobol/">Sobol sequence direction numbers</a>
 *
 * Created by Tommy Ettinger on 5/2/2015.
 */
public class DharmaRNG implements RandomnessSource {

    /** The scaling factor. */
    private static final double SCALE = Math.pow(2, 52);

    /** The number of bits to use. */
    private SobolQRNG sobol;

    /** The current index in the sequence. Starts at 1, not 0, because 0 acts differently and shouldn't be typical.*/
    private int seed = 1;

    /** Used to tweak the generator toward high or low values. */
    private double fairness = 0.54;

    /** Running total for what this has actually produced. */
    private double produced = 0.0;

    /** Running total for what this would produce if it always produced a value equal to fairness. */
    private double baseline = 0.0;

    /**
     * Constructs a DharmaRNG with a pseudo-random seed from Math.random().
     */
    public DharmaRNG()
    {
        this((int)(Math.random() * ((1l << 32) - 1)));
    }
    /**
     * Construct a new DharmaRNG with the given seed.
     *
     * @param seed used to determine the point in the Sobol sequence to start at; this should be non-negative.
     */
    public DharmaRNG(final int seed) {
        sobol = new SobolQRNG(1);

        if(seed < 0) this.seed = seed * -1;
        else this.seed = seed;
        if(this.seed <= 9000) this.seed = this.seed + 9001; //IT'S OVER 9000!

        sobol.skipTo(this.seed);
    }

    /**
     * Construct a new DharmaRNG with the given seed.
     *
     * @param seed used to determine the point in the Sobol sequence to start at; this should be non-negative
     * @param fairness the desired fairness metric, which must be between 0.0 and 1.0
     */
    public DharmaRNG(final int seed, final double fairness) {
        if(fairness < 0.0 || fairness >= 1.0)
            this.fairness = 0.54;
        else
            this.fairness = fairness;
        sobol = new SobolQRNG(1);

        if(seed < 0) this.seed = seed * -1;
        else this.seed = seed;
        if(this.seed <= 9000) this.seed = this.seed + 9001; //IT'S OVER 9000!

        sobol.skipTo(this.seed);
    }

    /** Generate a more-fair-random double.
     * @return a more-fair-random double in the range [0.0, 1.0).
     */
    public double nextDouble() {
        double gen = sobol.nextVector()[0];
        seed++;
        if(Math.abs((produced + gen) - (baseline + fairness)) > 1.5) {
            seed &= 0x03ffffff;
            seed *= 21;
            gen = sobol.skipTo(seed)[0];
        }
        if(Math.abs((produced + gen) - (baseline + fairness)) > 0.7)
        {
            gen = (gen + gen + fairness) / 3.0;
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

    /** Generate a more-fair-random double.
     * @param max the maximum exclusive value to be returned; minimum is 0 inclusive.
     * @return a more-fair-random double in the range [0.0, 1.0).
     */
    public double nextDouble(final double max) {
        final double gen = nextDouble();
        return gen * max;
    }

    /** Generate a more-fair-random long.
     * @return a random long, positive or negative (only 52 bits are actually used for the result, plus sign bit).
     */
    public long nextLong() {
        final double gen = nextDouble();
        return (long)(2.0 * (gen - 0.5) * SCALE);
    }
    /** Generate a more-fair-random long.
     * @param max the maximum exclusive value to be returned; minimum is 0 inclusive.
     * @return a random long in the range [0,max) (only 52 bits are actually used for the result, plus sign bit).
     */
    public long nextLong(final long max) {
        final double gen = nextDouble();
        return (long)(gen * max);
    }

    /** Generate a more-fair-random int.
     * @return a random int, can be positive or negative.
     */
    public int nextInt() {
        final double gen = nextDouble();
        return (int)(Math.pow(2.0, 33.0) * (gen - 0.5));
    }
    /** Generate a more-fair-random int.
     * @param max the maximum exclusive value to be returned; minimum is 0 inclusive.
     * @return a random long in the range [0,max).
     */
    public int nextInt(final int max) {
        final double gen = nextDouble();
        return (int)(gen * max);
    }

    /**
     * Sets the seed at any point after being constructed.
     * @param seed used to determine the point in the Sobol sequence to start at; this should be non-negative.
     */
    public void reseed(final int seed)
    {
        if(seed < 0) this.seed = seed * -1;
        else this.seed = seed;
        if(this.seed <= 9000) this.seed = this.seed + 9001; //IT'S OVER 9000!
        this.seed--;
        sobol.skipTo(this.seed);
    }

    /**
     * Get the current seed.
     * @return
     */
    public int getSeed() {
        return seed;
    }

    /**
     * Gets the measure that this class uses for RNG fairness, defaulting to 0.5 (always between 0.0 and 1.0).
     * @return the current fairness metric.
     */
    public double getFairness() {
        return fairness;
    }

    /**
     * Sets the measure that this class uses for RNG fairness, which must always be between 0.0 and 1.0, and will be
     * set to 0.5 if an invalid value is passed.
     * @param fairness the desired fairness metric, which must be between 0.0 and 1.0
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
        return (int) (nextInt() & (1L << bits) - 1);
    }

}