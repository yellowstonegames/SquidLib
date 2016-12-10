package squidpony.examples;

import squidpony.squidmath.PerlinNoise;

/**
 * This class is a scratchpad area to test things out.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Playground {

    public static void main(String... args) {
        new Playground().go();
    }

    private void go() {
        System.out.println(PerlinNoise.noise(1000, 10, 111222333));
        System.out.println(PerlinNoise.noise(1000 * 0.11709966304863834, 10 * 0.11709966304863834, 111222333 * 0.11709966304863834));
        System.out.println(PerlinNoise.noise(1000.25 * 0.11709966304863834, 10 * 0.11709966304863834, 111222333 * 0.11709966304863834));
        System.out.println(PerlinNoise.noise(1000.5 * 0.11709966304863834, 10 * 0.11709966304863834, 111222333 * 0.11709966304863834));
        System.out.println(PerlinNoise.noise(1001 * 0.11709966304863834, 10 * 0.11709966304863834, 111222333 * 0.11709966304863834));
    }

}
