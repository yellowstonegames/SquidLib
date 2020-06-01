package squidpony.squidgrid.mapping;

import squidpony.squidmath.FastNoise;
import squidpony.squidmath.NumberTools;

/**
 * Tools to create maps. Not commonly used outside of code that needs height maps.
 *
 * @see WorldMapGenerator WorldMapGenerator is a much-more-detailed kind of map generator.
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class HeightMapFactory {
    /**
     * Returns a randomly generated map of doubles that smoothly change nearby. Commonly referred to as a
     * Height Map. Uses {@link FastNoise} to generate coherent heights. The {@code offset} parameter is
     * converted to an int seed via {@link NumberTools#doubleToMixedIntBits(double)}, so it can be any
     * double, even an infinite one, and will still be treated as a valid seed.
     *
     * @param width  in cells
     * @param height in cells
     * @param offset a double that changes the sampling process; the range doesn't matter
     * @return the created map as a 2D double array
     */
    public static double[][] heightMap(int width, int height, double offset) {
        double[][] heightMap = new double[width][height];
        int seed = NumberTools.doubleToMixedIntBits(offset);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                //Get noise; layered2D uses 6 octaves of Simplex noise with a low frequency
                double n = FastNoise.instance.layered2D(x, y, seed, 6, 0.0125f) * 0.8
                        // and singleFoam gets a very different type of noise, contributing less though
                        + FastNoise.instance.singleFoam(~seed, x * 0x1p-4f, y * 0x1p-4f) * 0.2;
                double xdist = x - width * 0.5;
                xdist *= xdist;
                double ydist = y - height * 0.5;
                ydist *= ydist;
                double dist = Math.sqrt(xdist + ydist);
                // drop off height toward the east and west edges so the map kinda tiles
                heightMap[x][y] = n - Math.max(0, Math.pow(dist / (width * 0.5), 2) - 0.4);
            }
        }
        return heightMap;
    }
    private static final FastNoise noise = new FastNoise(1, 0x1p-5f, FastNoise.SIMPLEX_FRACTAL, 6);
    /**
     * Returns a randomly generated map of floats. Commonly referred to as a
     * Height Map. Uses {@link FastNoise} (producing FBM Simplex noise) to generate coherent heights.
     * Unlike {@link #heightMap(int, int, double)}, this doesn't drop off heights at the east and west edges of the map.
     * As such, it may be more suitable for local maps than world maps, since it is unlikely to tile east-west.
     * @param width  in cells
     * @param height in cells
     * @param seed   an int that significantly changes the generation process
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
