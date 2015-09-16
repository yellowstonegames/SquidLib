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
    public Random asRandom() {
        return super.asRandom();
    }

    @Override
    public double between(double min, double max) {
        return super.between(min, max);
    }

    @Override
    public int between(int min, int max) {
        return super.between(min, max);
    }

    @Override
    public int betweenWeighted(int min, int max, int samples) {
        return super.betweenWeighted(min, max, samples);
    }

    @Override
    public <T> T getRandomElement(T[] array) {
        return super.getRandomElement(array);
    }

    @Override
    public <T> T getRandomElement(List<T> list) {
        return super.getRandomElement(list);
    }

    @Override
    public <T> T getRandomElement(Queue<T> list) {
        return super.getRandomElement(list);
    }

    @Override
    public <T> List<T> randomRotation(List<T> l) {
        return super.randomRotation(l);
    }

    @Override
    public <T> Iterable<T> getRandomStartIterable(List<T> list) {
        return super.getRandomStartIterable(list);
    }

    @Override
    public <T> T[] shuffle(T[] elements) {
        return super.shuffle(elements);
    }

    @Override
    public <T> ArrayList<T> shuffle(List<T> elements) {
        return super.shuffle(elements);
    }

    @Override
    public synchronized double nextGaussian() {
        return super.nextGaussian();
    }

    @Override
    public double nextDouble() {
        return super.nextDouble();
    }

    @Override
    public double nextDouble(double max) {
        return super.nextDouble(max);
    }

    @Override
    public float nextFloat() {
        return super.nextFloat();
    }

    @Override
    public boolean nextBoolean() {
        return super.nextBoolean();
    }

    @Override
    public long nextLong() {
        return super.nextLong();
    }

    @Override
    public int nextInt(int bound) {
        return super.nextInt(bound);
    }

    @Override
    public int nextInt() {
        return super.nextInt();
    }

    @Override
    public int next(int bits) {
        return super.next(bits);
    }

    @Override
    public RandomnessSource getRandomness() {
        return super.getRandomness();
    }

    @Override
    public void setRandomness(RandomnessSource random) {
        super.setRandomness((random instanceof StatefulRandomness) ? random : new LightRNG(random.next(32)));
    }

    public long getState()
    {
        return ((StatefulRandomness)random).getState();
    }

    public void setState(long state)
    {
        ((StatefulRandomness)random).setState(state);
    }

    @Override
    public String toString() {
        return "StatefulRNG{" + Long.toHexString(((StatefulRandomness)random).getState()) + "}";
    }
}
