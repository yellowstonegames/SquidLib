package squidpony.squidgrid.mapping;

import squidpony.annotation.Beta;
import squidpony.squidmath.PerlinNoise;

/**
 * Tools to create maps. Not commonly used outside of code that needs height maps.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class HeightMapFactory {
    /**
     * Returns a randomly generated map of doubles. Commonly referred to as a
     * Height Map. Uses {@link PerlinNoise} in layers to generate coherent heights.
     *
     * @param width in cells
     * @param height in cells
     * @param offset a double that changes the sampling process; often randomly generated
     * @return the created map as a 2D double array
     */
    public static double[][] heightMap(int width, int height, double offset) {
        double[][] heightMap = new double[width][height];
        int perldivisors[] = new int[]{1, 1, 2, 4, 8, 16, 64};

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                //Get noise
                double n = 0;
                double xi = width * 1.1, yi = height * 1.1;//Math.max(width, height);

                for (int p = 0; p < perldivisors.length; p++) {
                    n += (PerlinNoise.noise((x + offset) / xi, (y + offset) / yi)) / perldivisors[p];
                    xi /= 2;
                    yi /= 2;
                }
                double xdist = x - width / 2.0;
                xdist *= xdist;
                double ydist = y - height / 2.0;
                ydist *= ydist;
                double dist = Math.sqrt(xdist + ydist);
                n -= Math.max(0, Math.pow(dist / (width / 2), 2) - 0.4);

                heightMap[x][y] = n;
            }
        }
        return heightMap;
    }
}
