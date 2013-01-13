package squidpony.squidgrid.fov;

import java.util.HashMap;
import squidpony.squidutility.Pair;

/**
 * An Adapter which bridges FOVSolvers and allows the input and output of
 * multiple types, as well as the compositing of multiple FOV algorithms into a
 * single result.
 *
 * When combining the results of multiple FOV runs, an weighted average is taken
 * for each cell and used as the result. The methods that add a solver without a
 * weight assign a weight of 1f.
 *
 * If no solvers have been added, then calculations return null;
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
 * Regarding scale: floating point operations have an implicit scale of 1f and
 * boolean operations have an implicit infinitely small scale making any value
 * which indicates some light present be treated as fully lit. Using an integer
 * scale of 1 is the inverse of using the boolean scale because rounding will
 * make any value less than fully lit be considered unlit.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class FOVTranslator implements FOVSolver {

    private HashMap<Pair<? extends FOVSolver, ? extends RadiusStrategy>, Float> solvers = new HashMap<>();
    private float[][] lightMap, resistanceMap;//backing map for results
    private float force, decay, totalWeight = 0;
    private int width, height;

    /**
     * Creates an empty instance. At least one FOVSolver must be added through
     * an add method before FOV calculations can be performed with this object.
     */
    public FOVTranslator() {
    }

    /**
     * Creates an adapter with the elements needed to run FOV calculations. More
     * solvers can be added if desired later.
     *
     * @param resistanceMap
     * @param force
     * @param decay
     * @param solver
     * @param strategy
     */
    public FOVTranslator(float[][] resistanceMap, float force, float decay, FOVSolver solver, RadiusStrategy strategy) {
        this(resistanceMap, force, decay);
        add(solver, strategy);
    }

    /**
     * Creates an adapter with the elements for FOV calculations.
     *
     * At least one FOVSolver must be added before calculations can be
     * performed.
     *
     * @param resistanceMap
     * @param force
     * @param decay
     */
    public FOVTranslator(float[][] resistanceMap, float force, float decay) {
        width = resistanceMap.length;
        height = resistanceMap[0].length;
        this.resistanceMap = resistanceMap;
        this.force = force;
        this.decay = decay;
    }

    /**
     * Creates an adapter with the elements needed to run FOV calculations. More
     * solvers can be added if desired later.
     *
     * @param resistanceMap
     * @param scale
     * @param force
     * @param decay
     * @param solver
     * @param strategy
     */
    public FOVTranslator(int[][] resistanceMap, int scale, int force, int decay, FOVSolver solver, RadiusStrategy strategy) {
        this(resistanceMap, scale, force, decay);
        add(solver, strategy);
    }

    /**
     * Creates an adapter with the elements needed to run FOV calculations.
     *
     * At least one FOVSolver must be added before calculations can be
     * performed.
     *
     * @param resistanceMap
     * @param scale
     * @param force
     * @param decay
     */
    public FOVTranslator(int[][] resistanceMap, int scale, int force, int decay) {
        width = resistanceMap.length;
        height = resistanceMap[0].length;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                this.resistanceMap[x][y] = (float) scale / resistanceMap[x][y];
            }
        }
        this.force = (float) scale / force;
        this.decay = (float) scale / decay;
    }

    /**
     * Creates an adapter with the elements needed to run FOV calculations. More
     * solvers can be added if desired later.
     *
     * @param resistanceMap
     * @param force
     * @param decay
     * @param solver
     * @param strategy
     */
    public FOVTranslator(boolean[][] resistanceMap, float force, float decay, FOVSolver solver, RadiusStrategy strategy) {
        this(resistanceMap, force, decay);
        add(solver, strategy);
    }

    /**
     * Creates an adapter with the elements needed to run FOV calculations. More
     * solvers can be added if desired later.
     *
     * @param resistanceMap
     * @param force
     * @param decay
     */
    public FOVTranslator(boolean[][] resistanceMap, float force, float decay) {
        width = resistanceMap.length;
        height = resistanceMap[0].length;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                this.resistanceMap[x][y] = resistanceMap[x][y] ? 1f : 0f;
            }
        }
        this.force = force;
        this.decay = decay;
    }

    /**
     * Setting the force overwrites any previously set radius value.
     *
     * @param force
     */
    public void setForce(float force) {
        this.force = force;
    }

    /**
     * Setting the decay overwrites any previously set radius value.
     *
     * @param decay
     */
    public void setDecay(float decay) {
        this.decay = decay;
    }

    /**
     * Setting the radius overwrites any previously set force and decay values.
     *
     * @param radius
     */
    public void setRadius(float radius) {
        force = 1f;
        decay = force / radius;
    }

    /**
     * Setting the radius overwrites any previously set force and decay values.
     *
     * @param radius
     */
    public void setRadius(int radius) {
        force = radius;
        decay = force / radius;
    }

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float force, float decay, RadiusStrategy radiusStrategy) {
        resistanceMap = map;
        this.force = force;
        this.decay = decay;
        width = map.length;
        height = map[0].length;
        if (solvers.isEmpty()) {
            return null;//no solvers loaded
        } else if (solvers.size() == 1) {//just one solver so run only it
            Pair<? extends FOVSolver, ? extends RadiusStrategy> p = (Pair<? extends FOVSolver, ? extends RadiusStrategy>) solvers.keySet().toArray()[0];
            lightMap = p.getFirst().calculateFOV(resistanceMap, startx, starty, force, decay, p.getSecond());
        } else {//multiple solvers, run them and take the average results for each cell
            lightMap = new float[width][height];
            int quantity = solvers.size();
            for (Pair<? extends FOVSolver, ? extends RadiusStrategy> p : solvers.keySet()) {
                float[][] tempMap = p.getFirst().calculateFOV(resistanceMap, startx, starty, force, decay, p.getSecond());
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        lightMap[x][y] += tempMap[x][y] / quantity * (solvers.get(p) / totalWeight);//add portion to running average
                    }
                }
            }
        }

        return lightMap;
    }

    @Override
    public float[][] calculateFOV(float[][] map, int startx, int starty, float radius) {
        return calculateFOV(map, startx, starty, 1, 1f / radius, null);//don't need a radius strategy as it uses ones added from pairs
    }

    /**
     * Runs an FOV calculation with the previously provided elements, with the
     * provided location as the start point.
     *
     * @param startx
     * @param starty
     * @return
     */
    public float[][] calculateFOV(int startx, int starty) {
        if (resistanceMap == null) {//make sure there's a map to work with
            return null;
        }
        return calculateFOV(lightMap, startx, starty, force, decay, null);//run FOV from the provided start location with preloaded variables
    }

    /**
     * Returns the last calculated light map as a boolean 2d array. Any cell
     * with any amount of light > 0 is considered to be lit. Lit cells are
     * marked true and unlit marked false;
     *
     * @return
     */
    public boolean[][] getBooleanArray() {
        if (lightMap == null) {//make sure there's something to work with
            return null;
        }

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
    public int[][] getIntArray(float multiplyer) {
        if (lightMap == null) {//make sure there's something to work with
            return null;
        }

        int[][] retMap = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                retMap[x][y] = (int) (lightMap[x][y] * multiplyer);
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

    public void add(Pair<FOVSolver, RadiusStrategy> pair, float weight) {
        totalWeight += weight;
        solvers.put(pair, force);
    }

    /**
     * Adds the provided pair to the list of solvers that will be run during
     * calculations.
     *
     * @param pair
     */
    public void add(Pair<FOVSolver, RadiusStrategy> pair) {
        add(pair, 1f);
    }

    /**
     * Adds the provided solver and strategy as a pair to the list of solvers
     * that will be run during calculations.
     *
     * @param solver
     * @param strategy
     */
    public void add(FOVSolver solver, RadiusStrategy strategy) {
        add(new Pair<>(solver, strategy));
    }

    /**
     * Adds the provided solver with a default radius strategy.
     *
     * @param solver
     */
    public void add(FOVSolver solver) {
        add(new Pair<FOVSolver, RadiusStrategy>(solver, BasicRadiusStrategy.CIRCLE));
    }
}
