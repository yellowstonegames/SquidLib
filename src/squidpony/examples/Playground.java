package squidpony.examples;

import squidpony.squidmath.RNG;

/**
 * This class is a scratchpad area to test things out quickly.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Playground {

    public static void main(String... args) {
        RNG rng = new RNG();
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            double val = rng.nextDouble();
            max = Math.max(val, max);
            min = Math.min(val, min);
        }
        System.out.println("Max: " + max);
        System.out.println("Min: " + min);
        
        double d = Math.nextUp(0.0);
        System.out.println("0ish: " + d);
    }
}
