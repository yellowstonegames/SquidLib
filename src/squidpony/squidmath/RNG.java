package squidpony.squidmath;

import java.util.Random;

/**
 * Customized extension of Random to allow for common roguelike operations.
 * 
 * @author Lewis Potter
 * @author Eben Howard - http://squidpony.com
 */
public class RNG extends Random {
    /**
     * Returns a value from a even distribution from min (inclusive) to max (exclusive).
     * 
     * @param min
     * @param max
     * @return 
     */
    public double between(double min, double max) {
        return min + (max - min) * nextDouble();
    }

    /**
     * Returns a value between min (inclusive) to max (exclusive).
     * 
     * @param min
     * @param max
     * @return 
     */
    public int between(int min, int max) {
        return nextInt(max - min + 1) + min;
    }

    public int betweenWeighted(int min, int max, int samples) {
        int sum = 0;
        for (int i = 0; i < samples; i++) {
            sum += between(min, max);
        }

        int answer = Math.round((float) sum / samples);
        return answer;
    }
}