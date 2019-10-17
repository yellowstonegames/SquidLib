package squidpony.squidgrid.mapping;

import squidpony.annotation.Beta;
import squidpony.squidmath.FastNoise;
import squidpony.squidmath.PerlinNoise;

/**
 * Tools to create maps. Not commonly used outside of code that needs height maps.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class HeightMapFactory {
    private static final int[] perlinDivisors = {1, 1, 2, 4, 8, 16, 64};

    /**
     * Returns a randomly generated map of doubles. Commonly referred to as a
     * Height Map. Uses {@link PerlinNoise} in layers to generate coherent heights.
     *
     * @param width  in cells
     * @param height in cells
     * @param offset a double that changes the sampling process; often randomly generated
     * @return the created map as a 2D double array
     */
    public static double[][] heightMap(int width, int height, double offset) {
        double[][] heightMap = new double[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                //Get noise
                double n = 0;
                double xi = width * 0.1375, yi = height * 0.1375;//Math.max(width, height);

                for (int p = 0; p < perlinDivisors.length; p++) {
                    n += PerlinNoise.noise((x + offset) / xi, (y + offset) / yi) / perlinDivisors[p];
                    xi *= 0.5;
                    yi *= 0.5;
                }
                double xdist = x - width * 0.5;
                xdist *= xdist;
                double ydist = y - height * 0.5;
                ydist *= ydist;
                double dist = Math.sqrt(xdist + ydist);
                n -= Math.max(0, Math.pow(dist / (width * 0.5), 2) - 0.4);

                heightMap[x][y] = n;
            }
        }
        return heightMap;
    }
    private static final FastNoise noise = new FastNoise(1, 0x1p-5f, FastNoise.SIMPLEX_FRACTAL, 7);
    /**
     * Returns a randomly generated map of floats. Commonly referred to as a
     * Height Map. Uses {@link FastNoise} (producing) FBM Simplex noise) to generate coherent heights.
     *
     * @param width  in cells
     * @param height in cells
     * @param seed   an int that significantly changes the generation process (more than an offset does)
     * @return the created map as a 2D float array
     */
    public static float[][] heightMapSeeded(int width, int height, int seed) {
        noise.setSeed(seed);
        float[][] heights = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                heights[x][y] = noise.getConfiguredNoise(x, y);
            }
        }
        return heights;
    }
}