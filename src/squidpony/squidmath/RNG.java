package squidpony.squidmath;

import java.util.Random;

/**
 * Customized extension of Random to allow for common roguelike operations.
 *
 * @author Lewis Potter
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class RNG extends Random {

    /**
     * Returns a value from a even distribution from min (inclusive) to max
     * (exclusive).
     *
     * @param min
     * @param max
     * @return
     */
    public double between(double min, double max) {
        return min + (max - min) * nextDouble();
    }

    /**
     * Returns a value between min and max inclusive.
     *
     * @param min
     * @param max
     * @return
     */
    public int between(int min, int max) {
        return nextInt(max - min + 1) + min;
    }

    /**
     * Returns the average of a number of randomly selected numbers from the
     * provided range. It will sample the number of times passed in as the third
     * parameter.
     *
     * This can be used to weight RNG calls to the average between min and max.
     *
     * @param min
     * @param max
     * @param samples
     * @return
     */
    public int betweenWeighted(int min, int max, int samples) {
        int sum = 0;
        for (int i = 0; i < samples; i++) {
            sum += between(min, max);
        }

        int answer = Math.round((float) sum / samples);
        return answer;
    }
}