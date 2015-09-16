package squidpony.squidmath;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Created by Tommy Ettinger on 9/15/2015.
 */
public class StatefulRNG extends RNG {
    public StatefulRNG() {
        super(new LightRNG());
    }

    public StatefulRNG(RandomnessSource random) {
        super((random instanceof StatefulRandomness) ? random : new LightRNG(random.next(32)));
    }

    @Override
    public RandomnessSource getRandomness() {
        return super.getRandomness();
    }

    /**
     * @return a Random instance that can be used for legacy compatability
     */
    @Override
    public Random asRandom() {
        return super.asRandom();
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
     * <p>
     * The inclusive and exclusive behavior is to match the behavior of the
     * similar method that deals with floating point values.
     *
     * @param min the minimum bound on the return value (inclusive)
     * @param max the maximum bound on the return value (exclusive)
     * @return the found value
     */
    @Override
    public int between(int min, int max) {
        return super.between(min, max);
    }

    /**
     * Returns the average of a number of randomly selected numbers from the
     * provided range, with min being inclusive and max being exclusive. It will
     * sample the number of times passed in as the third parameter.
     * <p>
     * The inclusive and exclusive behavior is to match the behavior of the
     * similar method that deals with floating point values.
     * <p>
     * This can be used to weight RNG calls to the average between min and max.
     *
     * @param min     the minimum bound on the return value (inclusive)
     * @param max     the maximum bound on the return value (exclusive)
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
     * @param list the list to get an element from
     * @return the randomly selected element
     */
    @Override
    public <T> T getRandomElement(List<T> list) {
        return super.getRandomElement(list);
    }

    /**
     * Returns a random elements from the provided queue. If the queue is empty
     * then null is returned.
     * <p>
     * <p>
     * Beware that this method allocates a copy of {@code list}, hence it's
     * quite costly.
     * </p>
     *
     * @param list the list to get an element from
     * @return the randomly selected element
     */
    @Override
    public <T> T getRandomElement(Queue<T> list) {
        return super.getRandomElement(list);
    }

    /**
     * Given a {@link List} l, this selects a random element of l to be the first value in the returned list l2. It
     * retains the order of elements in l after that random element and makes them follow the first element in l2, and
     * loops around to use elements from the start of l after it has placed the last element of l into l2.
     * <p>
     * Essentially, it does what it says on the tin. It randomly rotates the List l.
     *
     * @param l A {@link List} that will not be modified by this method. All elements of this parameter will be
     *          shared with the returned List.
     * @return A shallow copy of {@code l} that has been rotated so its first element has been randomly chosen
     * from all possible elements but order is retained. Will "loop around" to contain element 0 of l after the last
     * element of l, then element 1, etc.
     */
    @Override
    public <T> List<T> randomRotation(List<T> l) {
        return super.randomRotation(l);
    }

    /**
     * Get an Iterable that starts at a random location in list and continues on through list in its current order.
     * Loops around to the beginning after it gets to the end, stops when it returns to the starting location.
     *
     * @param list A list <b>with a constant-time {@link List#get(int)}
     *             method</b> (otherwise performances are degraded).
     * @return An {@link Iterable} that iterates over {@code list} but start at
     * a random index. If the chosen index is {@code i}, the iterator
     * will return
     * {@code list[i]; list[i+1]; ...; list[list.length() - 1]; list[0]; list[i-1]}
     * .
     * <p>
     * <p>
     * You should not modify {@code list} while you use the returned
     * reference. And there'll be no
     * ConcurrentModificationException to detect such erroneous
     * uses.
     * </p>
     */
    @Override
    public <T> Iterable<T> getRandomStartIterable(List<T> list) {
        return super.getRandomStartIterable(list);
    }

    /**
     * Shuffle an array using the Fisher-Yates algorithm.
     *
     * @param elements an array of T; will not be modified
     * @return a shuffled copy of elements
     */
    @Override
    public <T> T[] shuffle(T[] elements) {
        return super.shuffle(elements);
    }

    /**
     * Shuffle a {@link List} using the Fisher-Yates algorithm.
     *
     * @param elements an array of T; will not be modified
     * @return a shuffled ArrayList containing the whole of elements in pseudo-random order.
     */
    @Override
    public <T> ArrayList<T> shuffle(List<T> elements) {
        return super.shuffle(elements);
    }

    /**
     * @return a value from the gaussian distribution
     */
    @Override
    public synchronized double nextGaussian() {
        return super.nextGaussian();
    }

    /**
     * This returns a maximum of 0.9999999999999999 because that is the largest
     * Double value that is less than 1.0
     *
     * @return a value between 0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    @Override
    public double nextDouble() {
        return super.nextDouble();
    }

    /**
     * This returns a random double between 0.0 (inclusive) and max (exclusive).
     *
     * @param max
     * @return a value between 0 (inclusive) and max (exclusive)
     */
    @Override
    public double nextDouble(double max) {
        return super.nextDouble(max);
    }

    /**
     * This returns a maximum of 0.99999994 because that is the largest Float
     * value that is less than 1.0f
     *
     * @return a value between 0 (inclusive) and 0.99999994 (inclusive)
     */
    @Override
    public float nextFloat() {
        return super.nextFloat();
    }

    /**
     * Get a random bit of state, interpreted as true or false with approximately equal likelihood.
     * @return a random boolean.
     */
    @Override
    public boolean nextBoolean() {
        return super.nextBoolean();
    }

    /**
     * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive).
     * @return a 64-bit random long.
     */
    @Override
    public long nextLong() {
        return super.nextLong();
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
        return super.nextInt(bound);
    }

    /**
     * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both inclusive).
     * @return a 32-bit random int.
     */
    @Override
    public int nextInt() {
        return super.nextInt();
    }

    /**
     * Get up to 32 bits (inclusive) of random state from the RandomnessSource.
     * @param bits 1 to 32
     * @return a random number that fits in the specified number of bits.
     */
    @Override
    public int next(int bits) {
        return super.next(bits);
    }

    @Override
    public void setRandomness(RandomnessSource random) {
        super.setRandomness((random instanceof StatefulRandomness) ? random : new LightRNG(random.next(32)));
    }

    /**
     * Get a long that can be used to reproduce the sequence of random numbers this object will generate starting now.
     * @return a long that can be used as state.
     */
    public long getState()
    {
        return ((StatefulRandomness)random).getState();
    }

    /**
     * Sets the state of the random number generator to a given long, which will alter future random numbers this
     * produces based on the state.
     * @param state a long, which typically should not be 0 (some implementations may tolerate a state of 0, however).
     */
    public void setState(long state)
    {
        ((StatefulRandomness)random).setState(state);
    }

    @Override
    public String toString() {
        return "StatefulRNG{" + Long.toHexString(((StatefulRandomness)random).getState()) + "}";
    }
}
