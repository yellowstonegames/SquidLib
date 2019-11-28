package squidpony.squidmath;

import java.io.Serializable;

/**
 * An implementation of {@link IRNG} that allows specifying a distribution for all random numbers it produces via a
 * {@link squidpony.squidmath.IDistribution.SimpleDistribution} value. You can adapt any IDistribution to a
 * SimpleDistribution with the static methods in SimpleDistribution, like
 * {@link squidpony.squidmath.IDistribution.SimpleDistribution#fractionalDistribution(IDistribution)}. If no
 * distribution is specified, this uses {@link CurvedBoundedDistribution#instance}.
 * <br>
 * This uses a {@link MoonwalkRNG} internally to handle the number generation that the distribution requests. While you
 * can call methods on {@link #rng} that are specific to MoonwalkRNG, many distributions will get multiple random
 * numbers where a normal RNG would only get one, and this makes the state-jumping features of MoonwalkRNG less useful
 * here. It's still a fast generator when it comes to generating doubles, which is why it's used here; GWT-oriented
 * generators would be slower at generating doubles on desktop and many mobile platforms.
 * <br>
 * Created by Tommy Ettinger on 11/27/2019.
 */
public class DistributedRNG extends AbstractRNG implements IStatefulRNG, StatefulRandomness, Serializable {

    private static final long serialVersionUID = 1L;
    public IDistribution.SimpleDistribution distribution;
    public MoonwalkRNG rng;
    
    public DistributedRNG()
    {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }
    public DistributedRNG(long state)
    {
        this(state, CurvedBoundedDistribution.instance);
    }
    public DistributedRNG(long state, IDistribution.SimpleDistribution distribution)
    {
        this.rng = new MoonwalkRNG(state);
        this.distribution =  distribution;
    }
    /**
     * Get up to 32 bits (inclusive) of random output; the int this produces
     * will not require more than {@code bits} bits to represent.
     *
     * @param bits an int between 1 and 32, both inclusive
     * @return a random number that fits in the specified number of bits
     */
    @Override
    public final int next( int bits ) {
        return (int) (distribution.nextDouble(rng) * (1 << bits));
    }

    /**
     * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both inclusive).
     *
     * @return a 32-bit random int.
     */
    @Override
    public int nextInt() {
        return (int) (distribution.nextDouble(rng) * 0x1p32 - 0x1p31);
    }

    /**
     * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive). This implementation has "holes" in
     * its range, numbers that it cannot produce regardless of distribution.
     *
     * @return a 64-bit random long.
     */
    @Override
    public long nextLong() {
        return (long) (distribution.nextDouble(rng) * 0x1p64 - 0x1p63);
    }

    /**
     * Get a random bit of state, interpreted as true or false with approximately equal likelihood.
     * 
     * @return a random boolean.
     */
    @Override
    public boolean nextBoolean() {
        return distribution.nextDouble(rng) < 0.5;
    }

    /**
     * Gets a random double between 0.0 inclusive and 1.0 exclusive.
     * This returns a maximum of 0.9999999999999999 because that is the largest double value that is less than 1.0 .
     *
     * @return a double between 0.0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    @Override
    public double nextDouble() {
        return distribution.nextDouble(rng);
    }

    /**
     * Gets a random float between 0.0f inclusive and 1.0f exclusive.
     * This returns a maximum of 0.99999994 because that is the largest float value that is less than 1.0f .
     *
     * @return a float between 0f (inclusive) and 0.99999994f (inclusive)
     */
    @Override
    public float nextFloat() {
        return (float) distribution.nextDouble(rng);
    }
    /**
     * Returns a random non-negative integer below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    @Override
    public int nextInt(int bound) {
        return (int) (distribution.nextDouble(rng) * bound) & ~(bound >> 31);
    }

    /**
     * Exclusive on bound (which must be positive), with an inner bound of 0.
     * If bound is negative or 0 this always returns 0.
     *
     * @param bound the outer exclusive bound; should be positive, otherwise this always returns 0L
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    @Override
    public long nextLong(long bound) {
        return (long) (distribution.nextDouble(rng) * bound) & ~(bound >> 63);
    }

    /**
     * Exclusive on bound (which may be positive or negative), with an inner bound of 0.
     * If bound is negative this returns a negative long; if bound is positive this returns a positive long. The bound
     * can even be 0, which will cause this to return 0L every time.
     *
     * @param bound the outer exclusive bound; can be positive or negative
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    @Override
    public long nextSignedLong(long bound) {
        return (long) (distribution.nextDouble(rng) * bound);
    }

    /**
     * Returns a random non-negative integer between 0 (inclusive) and the given bound (exclusive),
     * or 0 if the bound is 0. The bound can be negative, which will produce 0 or a negative result.
     *
     * @param bound the outer bound (exclusive), can be negative or positive
     * @return the found number
     */
    @Override
    public int nextSignedInt(int bound) {
        return (int) (distribution.nextDouble(rng) * bound);
    }
    
    /**
     * Creates a copy of this IRNG; it will generate the same random numbers, given the same calls in order, as this
     * IRNG at the point copy() is called. The copy will not share references with this IRNG, except to
     * {@link #distribution}, which usually shouldn't change much.
     *
     * @return a copy of this IRNG
     */
    @Override
    public DistributedRNG copy() {
        return new DistributedRNG(rng.getState(), distribution);
    }

    /**
     * Gets a view of this IRNG in a way that implements {@link Serializable}, which may simply be this IRNG if it
     * implements Serializable as well as IRNG.
     *
     * @return a {@link Serializable} view of this IRNG or a similar one; may be {@code this}
     */
    @Override
    public Serializable toSerializable() {
        return this;
    }

    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    @Override
    public long getState() {
        return rng.getState();
    }

    /**
     * Set the current internal state of this StatefulRandomness with a long; this accepts a state of 0 with no issues.
     *
     * @param state a 64-bit long.
     */
    @Override
    public void setState(long state) {
        rng.setState(state);
    }
}
