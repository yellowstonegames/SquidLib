package squidpony.squidmath;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * A subclass of StatefulRNG (and thus RNG) that allows customizing many parts of the random number generation.
 * This is meant to be a more comprehensible version of the functionality present in RandomBias, and also for it to be
 * easier to use with methods that expect an RNG.
 * <br>
 * You can change the expected average for the values this produces, which uses the RandomBias.EXPONENTIAL distribution,
 * with all the caveats it has: it strongly favors either high or low values when the average gets especially high or
 * low, but it can essentially cover all averages between 0.0 and 1.0 (this class limits it to 0.1 and 0.9, so other
 * techniques can be used effectively).
 * <br>
 * You can also affect the "centrality" of random numbers, causing more to occur near the expected average (a bell curve
 * effect), or cause more near extreme ends of the random number spectrum. In practice, centrality changes are hard to
 * notice, but may be useful to simulate certain effects. An example of centrality changes in existing games include the
 * Nintendo title Advance Wars 2, where a brutish commander could increase the amount of damage his units dealt but also
 * suffered unpredictability; attacks could deal even more or much less damage than normal without any way to build
 * tactics around it. Square Enix's Final Fantasy XII also notably differentiated certain weapons (axes, hammers, and
 * "hand-cannons") from other similar options by making them deal less predictable damage. In both cases the connotation
 * is that more randomness is fitting for a brute-force approach to combat where pre-planned strategies are less
 * emphasized. It should also be noted that increasing the frequency of extreme results makes small bonuses to defense
 * or offense typically less useful, and small penalties less harmful. The opposite can be true for a carefully tuned
 * game where the most common results are tightly clustered, and most target numbers are just slightly above the
 * ordinary average. In tabletop games, 1d20 and 3d6 have the same average, but 1d20 is uniform, where 3d6 is clustered
 * around 10 and 11, each the result of 1/8 of rolls on their own and 1/4 together. This makes the case where a +1 bonus
 * to succeed changes the outcome on approximately 5% of 1d20 rolls, regardless of the required number to succeed if it
 * is less than 20. However, a +1 bonus matters on a variable portion of 3d6 rolls; if you become able to succeed on a
 * 10 or 11 where that was a failure before, the bonus applies approximately 12.5% of the time. Becoming able to succeed
 * on an 18 where that was a failure before is essentially worthless, affecting less than 0.5% of rolls. This property
 * of centralized results should be considered if game balance and/or the lethality of combat is important. One lengthy
 * stretch of extreme results by enemies that work against the favor of a player character generally result in a dead
 * player character, and RNGs that make extreme results more common may seem particularly cruel to players.
 * <br>
 * This generator sets a field, rawLatest, every time a random number is produced. This stores a pseudo-random double
 * between 0.0 (inclusive) and 1.0 (exclusive) that is not subject to the bias an expected average introduces, and is
 * close to uniformly distributed. You should expect rawLatest to be higher when higher numbers are returned from a
 * method like nextInt(), and lower when lower numbers are returned. This can be useful for rare effects that should not
 * be drastically more or less likely when slight changes are made to the expected average; if the expected average is
 * 0.65, many more random doubles from nextDouble() will be between 0.95 and 1.0 (probably more than 10% of random
 * numbers), but rawLatest will only be between 0.95 and 1.0 for close to 5% of all generations.
 * <br>
 * You can get and set the state this uses internally, and this is stored as a 64-bit long.
 * <br>
 * The choice of RandomnessSource doesn't really matter since this will always use a ThrustAltRNG internally.
 * ThrustAltRNG is the current best StatefulRandomness implementation, with excellent performance characteristics and
 * few flaws, and though its relatively low period may sometimes be a detriment, all StatefulRandomness implementations
 * will have the same or lower period.
 * <br>
 * More customizations may be added in the future to the ones available currently.
 */
public class EditRNG extends StatefulRNG implements Serializable{

	/** Used to tweak the generator toward high or low values. */
    private double expected = 0.5;

    /**
     * When positive, makes the generator more likely to generate values close to the average (bell curve).
     * When zero (the default), makes no changes to the centering of values.
     * When negative, makes the generator swing more toward extremes rather than gravitate toward the average.
     * Values are typically between -100 and 100, but can have extreme weight and overshadow other parts of the RNG if
     * they go much higher than 200.
     */
    private double centrality = 0.0;
    /**
     * The latest generated double, between 0.0 and 1.0, before changes for centrality and expected average.
     * Doubles are used to generate all random numbers this class produces, so be aware that calling getRandomElement()
     * will change this just as much as nextDouble(), nextInt(), or between() will. Primarily useful to obtain
     * uniformly-distributed random numbers that are related to the biased random numbers this returns as a main result,
     * such as to find when the last number generated was in the bottom 5% (less than 0.05, which could represent some
     * kind of critical failure or fumble) or top 10% (greater than or equal to 0.9, which could grant a critical
     * success or luck-based reward of some kind).
     */
    public double rawLatest = 0.5;
	private static final long serialVersionUID = -2458726316853811777L;

    /**
     * Constructs an EditRNG with a pseudo-random seed from Math.random().
     */
    public EditRNG()
    {
    }
    /**
     * Construct a new EditRNG with the given seed.
     *
     * @param seed used to seed the default RandomnessSource.
     */
    public EditRNG(final long seed) {
        super(seed);
    }
    /**
     * Construct a new EditRNG with the given seed.
     *
     * @param seed used to seed the default RandomnessSource.
     */
    public EditRNG(final CharSequence seed) {
        super(seed);
    }

    /**
     * Construct a new EditRNG with the given seed.
     *
     * @param seed used to seed the default RandomnessSource.
     * @param expected the expected average for random doubles, which will be capped between 0.1 and 0.9
     */
    public EditRNG(final long seed, double expected) {
        super(seed);
        this.expected = Math.max(0.1, Math.min(0.89999994, expected));
    }
    /**
     * Construct a new EditRNG with the given seed.
     *
     * @param seed used to seed the default RandomnessSource.
     * @param expected the expected average for random doubles, which will be capped between 0.1 and 0.9
     */
    public EditRNG(final String seed, double expected) {
        super(seed);
        this.expected = Math.max(0.1, Math.min(0.89999994, expected));
    }

    /**
     * Construct a new EditRNG with the given seed.
     *
     * @param seed used to seed the default RandomnessSource.
     * @param expected the expected average for random doubles, which will be capped between 0.1 and 0.9
     * @param centrality if positive, makes results more likely to be near expected; if negative, the opposite. The
     *                   absolute value of centrality affects how centered results will be, with 0 having no effect
     */
    public EditRNG(final long seed, double expected, double centrality) {
        super(seed);
        this.expected = Math.max(0.1, Math.min(0.89999994, expected));
        this.centrality = centrality;
    }
    /**
     * Construct a new EditRNG with the given seed.
     *
     * @param seed used to seed the default RandomnessSource.
     * @param expected the expected average for random doubles, which will be capped between 0.1 and 0.9
     * @param centrality if positive, makes results more likely to be near expected; if negative, the opposite. The
     *                   absolute value of centrality affects how centered results will be, with 0 having no effect
     */
    public EditRNG(final String seed, double expected, double centrality) {
        super(seed);
        this.expected = Math.max(0.1, Math.min(0.89999994, expected));
        this.centrality = centrality;
    }

    /**
     * Construct a new EditRNG with the given seed.
     *
     * @param rs the implementation used to generate random bits.
     */
    public EditRNG(final RandomnessSource rs) {
        super(rs);
    }
    /**
     * Construct a new EditRNG with the given seed.
     *
     * @param rs the implementation used to generate random bits.
     * @param expected the expected average for random doubles, which will be capped between 0.1 and 0.9
     */
    public EditRNG(final RandomnessSource rs, double expected) {
        super(rs);
        this.expected = Math.max(0.1, Math.min(0.89999994, expected));
    }
    /**
     * Construct a new EditRNG with the given seed.
     *
     * @param rs the implementation used to generate random bits.
     * @param expected the expected average for random doubles, which will be capped between 0.1 and 0.9
     * @param centrality if positive, makes results more likely to be near expected; if negative, the opposite. The
     *                   absolute value of centrality affects how centered results will be, with 0 having no effect
     */
    public EditRNG(final RandomnessSource rs, double expected, double centrality) {
        super(rs);
        this.expected = Math.max(0.1, Math.min(0.89999994, expected));
        this.centrality = centrality;
    }

    /**
     * Generate a random double, altered to try to match the expected average and centrality.
     * @return a double between 0.0 (inclusive) and 1.0 (exclusive)
     */
    @Override
    public double nextDouble() {
        long l = random.nextLong();
        double gen = (l & 0x1fffffffffffffL) * DOUBLE_UNIT, scatter = (l & 0xffffffL) * FLOAT_UNIT;
        rawLatest = 0.9999999999999999 - gen;
        gen = 0.9999999999999999 - Math.pow(gen, 1.0 / (1.0 - expected) - 1.0);
        if(centrality > 0) {
            scatter = 0.9999999999999999 - Math.pow(scatter, 1.0 / (1.0 - expected) - 1.0);
            gen = (gen * 100 + scatter * centrality) / (100 + centrality);
        }
        else if(centrality < 0)
        {
            scatter = Math.sin(scatter * Math.PI * 0.5);
            scatter *= scatter;
            if(expected >= 0.5)
                scatter = scatter * (1.0 - expected) * 2 + expected - (1.0 - expected);
            else
                scatter *= expected * 2;
            gen = (gen * 100 - scatter * centrality) / (100 - centrality);
        }

        return gen;

    }

    /**
     * This returns a random double between 0.0 (inclusive) and max (exclusive).
     *
     * @return a value between 0 (inclusive) and max (exclusive)
     */
    @Override
    public double nextDouble(double max) {
        return nextDouble() * max;
    }

    /**
     * Returns a value from a even distribution from min (inclusive) to max
     * (exclusive).
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    @Override
    public double between(double min, double max) {
        return super.between(min, max);
    }

    /**
     * Returns a value between min (inclusive) and max (exclusive).
     *
     * The inclusive and exclusive behavior is to match the behavior of the
     * similar method that deals with floating point values.
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    @Override
    public int between(int min, int max)
    {
        return super.between(min, max);
    }

    @Override
    public long between(long min, long max) {
        return super.between(min, max);
    }

    /**
     * Returns the average of a number of randomly selected numbers from the
     * provided range, with min being inclusive and max being exclusive. It will
     * sample the number of times passed in as the third parameter.
     *
     * The inclusive and exclusive behavior is to match the behavior of the
     * similar method that deals with floating point values.
     *
     * This can be used to weight RNG calls to the average between min and max.
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @param samples the number of samples to take
     * @return the found value
     */
    @Override
    public int betweenWeighted(int min, int max, int samples) {
        return super.betweenWeighted(min, max, samples);
    }

    /**
     * Returns a random element from the provided array and maintains object
     * type.
     *
     * @param <T> the type of the returned object
     * @param array the array to get an element from
     * @return the randomly selected element
     */
    @Override
    public <T> T getRandomElement(T[] array) {
        return super.getRandomElement(array);
    }

    /**
     * Returns a random element from the provided list. If the list is empty
     * then null is returned.
     *
     * @param <T> the type of the returned object
     * @param list the list to get an element from
     * @return the randomly selected element
     */
    @Override
    public <T> T getRandomElement(List<T> list) {
        return super.getRandomElement(list);
    }

    /**
     * Returns a random element from the provided ShortSet. If the set is empty
     * then an exception is thrown.
     *
     * <p>
     * Requires iterating through a random amount of the elements in set, so performance depends on the size of set but
     * is likely to be decent. This is mostly meant for internal use, the same as ShortSet.
     * </p>
     * @param set the ShortSet to get an element from
     * @return the randomly selected element
     */
    public short getRandomElement(ShortSet set) {
        return super.getRandomElement(set);
    }

    /**
     * Returns a random element from the provided Collection, which should have predictable iteration order if you want
     * predictable behavior for identical RNG seeds, though it will get a random element just fine for any Collection
     * (just not predictably in all cases). If you give this a Set, it should be a LinkedHashSet or some form of sorted
     * Set like TreeSet if you want predictable results. Any List or Queue should be fine. Map does not implement
     * Collection, thank you very much Java library designers, so you can't actually pass a Map to this, though you can
     * pass the keys or values. If coll is empty, returns null.
     *
     * <p>
     * Requires iterating through a random amount of coll's elements, so performance depends on the size of coll but is
     * likely to be decent, as long as iteration isn't unusually slow. This replaces {@code getRandomElement(Queue)},
     * since Queue implements Collection and the older Queue-using implementation was probably less efficient.
     * </p>
     * @param <T> the type of the returned object
     * @param coll the Collection to get an element from; remember, Map does not implement Collection
     * @return the randomly selected element
     */
    public <T> T getRandomElement(Collection<T> coll) {
        return super.getRandomElement(coll);
    }

    /**
     * @return a value from the gaussian distribution
     */
    @Override
    public synchronized double nextGaussian() {
        return super.nextGaussian();
    }
    /**
     * Returns a random integer below the given bound, or 0 if the bound is 0 or
     * negative.
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
     * Returns a random integer, which may be positive or negative.
     * @return A random int
     */
    @Override
    public int nextInt() {
        return (int)((nextDouble() * 2.0 - 1.0) * 0x7FFFFFFF);
    }

    /**
     * Returns a random long, which may be positive or negative.
     * @return A random long
     */
    @Override
    public long nextLong() {
        return (long)((nextDouble() * 2.0 - 1.0) * 0x7FFFFFFFFFFFFFFFL);
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
     * Gets the current expected average for this EditRNG.
     * @return the current expected average.
     */
    public double getExpected() {
        return expected;
    }

    /**
     * Sets the expected average for random doubles this produces, which must always be between 0.1 and 0.9, and will be
     * set to 0.5 if an invalid value is passed.
     * @param expected the expected average to use, which should be 0.1 &lt;= fairness &lt; 0.9
     */
    public void setExpected(double expected) {
        if(expected < 0.0 || expected >= 1.0)
            this.expected = 0.5;
        else
            this.expected = expected;
    }

    /**
     * Gets the current centrality measure of this EditRNG.
     * Centrality has several possible effects:
     * When positive, makes the generator more likely to generate values close to the average (bell curve).
     * When zero (the default), makes no changes to the centering of values.
     * When negative, makes the generator swing more toward extremes rather than gravitate toward the average.
     * <br>
     * Values are typically between -100 and 100, but can have extreme weight and overshadow other parts of the RNG if
     * they go much higher than 200.
     * @return the current centrality
     */
    public double getCentrality() {
        return centrality;
    }

    /**
     * Gets the current centrality measure of this EditRNG.
     * Centrality has several possible effects:
     * When positive, makes the generator more likely to generate values close to the average (bell curve).
     * When zero (the default), makes no changes to the centering of values.
     * When negative, makes the generator swing more toward extremes rather than gravitate toward the average.
     * <br>
     * Values are typically between -100 and 100, but can have extreme weight and overshadow other parts of the RNG if
     * they go much higher than 200.
     * @param centrality the new centrality measure to use
     */
    public void setCentrality(double centrality) {
        this.centrality = centrality;
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
        return (int)(nextDouble() * (1L << bits));

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
     * Gets a random portion of data (an array), assigns that portion to output (an array) so that it fills as much as
     * it can, and then returns output. Will only use a given position in the given data at most once; does this by
     * shuffling a copy of data and getting a section of it that matches the length of output.
     *
     * Based on http://stackoverflow.com/a/21460179 , credit to Vincent van der Weele; modifications were made to avoid
     * copying or creating a new generic array (a problem on GWT).
     * @param data an array of T; will not be modified.
     * @param output an array of T that will be overwritten; should always be instantiated with the portion length
     * @param <T> can be any non-primitive type.
     * @return an array of T that has length equal to output's length and may contain null elements if output is shorter
     * than data
     */
    @Override
    public <T> T[] randomPortion(T[] data, T[] output) {
        return super.randomPortion(data, output);
    }

    /**
     * Gets a random portion of a List and returns it as a new List. Will only use a given position in the given
     * List at most once; does this by shuffling a copy of the List and getting a section of it.
     *
     * @param data  a List of T; will not be modified.
     * @param count the non-negative number of elements to randomly take from data
     * @return a List of T that has length equal to the smaller of count or data.length
     */
    @Override
    public <T> List<T> randomPortion(List<T> data, int count) {
        return super.randomPortion(data, count);
    }

    /**
     * Gets a random subrange of the non-negative ints from start (inclusive) to end (exclusive), using count elements.
     * May return an empty array if the parameters are invalid (end is less than/equal to start, or start is negative).
     *
     * @param start the start of the range of numbers to potentially use (inclusive)
     * @param end   the end of the range of numbers to potentially use (exclusive)
     * @param count the total number of elements to use; will be less if the range is smaller than count
     * @return an int array that contains at most one of each number in the range
     */
    @Override
    public int[] randomRange(int start, int end, int count) {
        return super.randomRange(start, end, count);
    }

    /**
     * Gets the latest "un-biased" random double used to produce the most recent (potentially) biased random number
     * generated for another method in this class, such as nextDouble(), between(), or getRandomElement(). This is a
     * double between 0.0 (inclusive) and 1.0 (exclusive).
     * @return the latest uniformly-distributed double before bias is added; between 0.0 and 1.0 (exclusive upper)
     */
    public double getRawLatest() {
        return rawLatest;
    }

    /**
     * Creates a copy of this StatefulRNG; it will generate the same random numbers, given the same calls in order, as
     * this StatefulRNG at the point copy() is called. The copy will not share references with this StatefulRNG.
     *
     * @return a copy of this StatefulRNG
     */
    @Override
    public RNG copy() {
        EditRNG next = new EditRNG(random.copy(), expected, centrality);
        next.rawLatest = rawLatest;
        return next;
    }

    /**
     * Get a long that can be used to reproduce the sequence of random numbers this object will generate starting now.
     *
     * @return a long that can be used as state.
     */
    @Override
    public long getState() {
        return super.getState();
    }

    /**
     * Sets the state of the random number generator to a given long, which will alter future random numbers this
     * produces based on the state.
     *
     * @param state a long, which typically should not be 0 (some implementations may tolerate a state of 0, however).
     */
    @Override
    public void setState(long state) {
        super.setState(state);
    }

    @Override
    public String toString() {
        return "EditRNG{" +
                "expected=" + expected +
                ", centrality=" + centrality +
                ", Randomness Source=" + random +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EditRNG editRNG = (EditRNG) o;

        if (Double.compare(editRNG.expected, expected) != 0) return false;
        return Double.compare(editRNG.centrality, centrality) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode() * 31;
        result += NumberTools.doubleToMixedIntBits(expected);
        result = 31 * result + NumberTools.doubleToMixedIntBits(centrality);
        return result;
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


}