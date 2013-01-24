package squidpony.squidgrid.fov;

import squidpony.annotation.Beta;

/**
 * A threaded wrapper for an FOVSolver.
 *
 * Will calculate in its own thread when standard calculation methods are
 * called. Will also calculate in its own thread if simply run.
 *
 * Depending on which constructor is used it may or may not be appropriate to
 * calculate just by running this object as a thread. Please see the individual
 * constructor's documentation for more detail.
 *
 * If interrupted, results may not be available.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class ThreadedFOVSolver implements FOVSolver, Runnable {

    private FOVSolver solver;
    private int startx, starty;
    private float force, decay;
    private RadiusStrategy strategy;
    private boolean calculating = false;
    private float[][] lightMap;

    /**
     * Builds a FOVSolver which will use the full information for calculations
     * when run as a Thread.
     *
     * @param solver
     * @param startx
     * @param starty
     * @param force
     * @param decay
     * @param strategy
     */
    public ThreadedFOVSolver(FOVSolver solver, int startx, int starty, float force, float decay, RadiusStrategy strategy) {
        this.solver = solver;
        this.startx = startx;
        this.starty = starty;
        this.force = force;
        this.decay = decay;
        this.strategy = strategy;
    }

    /**
     * Builds a FOVSolver which will use the default RadiusStrategy for
     * calculations when used as a standard Thread.
     *
     * @param solver
     * @param startx
     * @param starty
     * @param radius
     */
    public ThreadedFOVSolver(FOVSolver solver, int startx, int starty, float radius) {
        this.solver = solver;
        this.startx = startx;
        this.starty = starty;
        force = 1f;
        decay = 1f / radius;
    }

    /**
     * Simply wraps a solver in order to thread it. If this constructor is used
     * then the standard FOVSolver methods should be used.
     *
     * @param solver
     */
    public ThreadedFOVSolver(FOVSolver solver) {
        this.solver = solver;
    }

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, int startx, int starty, float force, float decay, RadiusStrategy radiusStrategy) {
        this.startx = startx;
        this.starty = starty;
        this.force = force;
        this.decay = decay;
        this.strategy = radiusStrategy;
        return runThreadedCalculation();
    }

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, int startx, int starty, float radius) {
        this.startx = startx;
        this.starty = starty;
        force = 1f;
        decay = 1f / radius;
        strategy = BasicRadiusStrategy.CIRCLE;
        return runThreadedCalculation();
    }

    private synchronized float[][] runThreadedCalculation() {
        calculating = true;
        new Thread(this).start();
        while (calculating) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
        return lightMap;
    }

    @Override
    public void run() {
        calculating = true;
        lightMap = calculateFOV(lightMap, startx, starty, force, decay, strategy);
        calculating = false;
    }

    public boolean isCalculating() {
        return calculating;
    }

    /**
     * Returns the last calculated light map. If no calculation has yet been
     * completed (or one is underway) then null is returned.
     *
     * Depending on what FOVSolver this object wraps, it may be more appropriate
     * to check if this object is calculating and once it's finished take the
     * result directly from the inner solver, if it allows for it.
     *
     * @return
     */
    public float[][] getLightMap() {
        if (calculating) {
            return null;
        } else {
            return lightMap;
        }
    }
}
