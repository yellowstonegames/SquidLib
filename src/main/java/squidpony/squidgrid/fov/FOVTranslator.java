package squidpony.squidgrid.fov;

/**
 * An Adapter which wraps a FOVSolver and allows the input and output of
 * multiple types.
 *
 * The FOV methods that don't require an explicit resistance map will only
 * return a valid result if one of the constructors passing one in or one of the
 * explicit resistance map calculation methods is used.
 *
 * Once any of the calculateFOV methods have been called, the results can be
 * accessed as a variety of types through getter methods.
 *
 * When using the integer methods a scaling factor is required. A typical usage
 * scenario for this would be to simply make it 100, indicating that an opaque
 * cell will have a resistance of 100. Force and decay are also adjusted by the
 * scaling factor so it does not need to be pre-multiplied for them. Note that
 * any light value that when scaled down would be less than 1/scale will be
 * treated as unlit by the integer returning methods but may still be considered
 * lit under the boolean and float systems.
 *
 * When using the boolean methods, true is equivalent to fully resistant on the
 * input and fully lit on the output.
 *
 * Regarding scale: floating point operations have an implicit scale of 1f and
 * boolean operations have an implicit infinitely small scale making any value
 * which indicates some light present be treated as fully lit. Using an integer
 * scale of 1 is the inverse of using the boolean scale because rounding will
 * make any value less than fully lit be considered unlit.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class FOVTranslator implements FOVSolver {

    private FOVSolver solver;
    private float[][] lightMap;//backing map for results

    /**
     * Creates an empty instance. At least one FOVSolver must be added through
     * an add method before FOV calculations can be performed with this object.
     */
    public FOVTranslator(FOVSolver solver) {
        this.solver = solver;
    }

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float force, float decay, RadiusStrategy radiusStrategy) {
        lightMap = solver.calculateFOV(map, startx, starty, force, decay, radiusStrategy);
        return lightMap;
    }

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float radius) {
        lightMap = solver.calculateFOV(map, startx, starty, 1, 1f / radius, BasicRadiusStrategy.CIRCLE);
        return lightMap;
    }

    /**
     * Calculates the FOV using an integer array.
     *
     * @param map
     * @param startx
     * @param starty
     * @param force
     * @param decay
     * @param radiusStrategy
     * @param scale
     * @return
     */
    public int[][] calculateFOV(int[][] map, int startx, int starty, float force, float decay, RadiusStrategy radiusStrategy, float scale) {
        int width = map.length;
        int height = map[0].length;
        float[][] tempMap = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tempMap[x][y] = map[x][y] / scale;
            }
        }
        lightMap = solver.calculateFOV(tempMap, startx, starty, force, decay, radiusStrategy);
        int[][] resultMap = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                resultMap[x][y] = (int) (lightMap[x][y] * scale);
            }
        }
        return resultMap;
    }

    /**
     * Calculates the FOV using an integer array.
     *
     * @param map
     * @param startx
     * @param starty
     * @param radius
     * @param scale
     * @return
     */
    public int[][] calculateFOV(int[][] map, int startx, int starty, float radius, float scale) {
        int width = map.length;
        int height = map[0].length;
        float[][] tempMap = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tempMap[x][y] = map[x][y] / scale;
            }
        }
        lightMap = solver.calculateFOV(tempMap, startx, starty, radius);
        int[][] resultMap = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                resultMap[x][y] = (int) (lightMap[x][y] / scale);
            }
        }
        return resultMap;
    }

    /**
     * Calculates the FOV using a boolean array where true indicates that the
     * location blocks all light.
     *
     * In the returned array, true indicates that the cell is lit.
     *
     * @param map
     * @param startx
     * @param starty
     * @param force
     * @param decay
     * @param radiusStrategy
     * @return
     */
    public boolean[][] calculateFOV(boolean[][] map, int startx, int starty, float force, float decay, RadiusStrategy radiusStrategy) {
        int width = map.length;
        int height = map[0].length;
        float[][] tempMap = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tempMap[x][y] = map[x][y] ? 1f : 0f;
            }
        }
        lightMap = solver.calculateFOV(tempMap, startx, starty, force, decay, radiusStrategy);
        boolean[][] resultMap = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                resultMap[x][y] = lightMap[x][y] > 0;
            }
        }
        return resultMap;
    }

    /**
     * Calculates the FOV using a boolean array where true indicates that the
     * location blocks all light.
     *
     * In the returned array, true indicates that the cell is lit.
     *
     * @param map
     * @param startx
     * @param starty
     * @param radius
     * @return
     */
    public boolean[][] calculateFOV(boolean[][] map, int startx, int starty, float radius) {
        int width = map.length;
        int height = map[0].length;
        float[][] tempMap = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tempMap[x][y] = map[x][y] ? 1f : 0f;
            }
        }
        lightMap = solver.calculateFOV(tempMap, startx, starty, radius);
        boolean[][] resultMap = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                resultMap[x][y] = lightMap[x][y] > 0;
            }
        }
        return resultMap;
    }

    /**
     * Returns the last calculated light map as a boolean 2d array.
     *
     * Lit cells are marked true and unlit marked false;
     *
     * @return
     */
    public boolean[][] getBooleanArray() {
        if (lightMap == null) {//make sure there's something to work with
            return null;
        }

        int width = lightMap.length;
        int height = lightMap[0].length;
        boolean[][] retMap = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                retMap[x][y] = lightMap[x][y] > 0f;
            }
        }

        return retMap;
    }

    /**
     * Returns an integer array representation of the last calculated FOV light
     * map.
     *
     * @param multiplyer
     * @return
     */
    public int[][] getIntArray(float scale) {
        if (lightMap == null) {//make sure there's something to work with
            return null;
        }

        int width = lightMap.length;
        int height = lightMap[0].length;
        int[][] retMap = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                retMap[x][y] = (int) (lightMap[x][y] * scale);
            }
        }

        return retMap;
    }

    /**
     * Returns true if the cell at (x, y) is considered lit.
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isLit(int x, int y) {
        return lightMap[x][y] > 0f;
    }

    /**
     * Returns the scaled value at (x, y).
     *
     * @param x
     * @param y
     * @param scale
     * @return
     */
    public int getInt(int x, int y, int scale) {
        return (int) (lightMap[x][y] * scale);
    }
}
