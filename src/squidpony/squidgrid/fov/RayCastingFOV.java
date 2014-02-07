package squidpony.squidgrid.fov;

import squidpony.squidgrid.util.RadiusStrategy;
import java.awt.Point;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import squidpony.annotation.Beta;
import squidpony.squidgrid.los.LOSSolver;
import squidpony.squidgrid.los.RayCastingLOS;
import squidpony.squidgrid.util.BasicRadiusStrategy;

/**
 * Simple raytracing algorithm for Field of View. In large areas will be
 * relatively inefficient due to repeated visiting of some cells.
 *
 * Tracing is done from four points near the corners of the cells. The line
 * between points is then traversed to find what cells are intersected. Once the
 * traversal hits an opaque cell, runs out of light (based on decay), or reaches
 * the set radius, that walk is terminated.
 *
 *
 * Light will decay, with solid objects being lit if there is a lit cell next to
 * them in the direction of the source point. Such objects will be lit according
 * to the decay so a solid object at the edge of vision will not be lit if a
 * transparent object in the same cell would not be lit.
 *
 * Currently a work in progress.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class RayCastingFOV implements FOVSolver {

    private float[][] lightMap, resistanceMap;
    private int width, height, startx, starty;
    private float force, decay;
    private RadiusStrategy radiusStrategy;
    private ExecutorService pool;

    /**
     * Builds a new ray tracing fov solver with the default step size.
     */
    public RayCastingFOV() {
    }

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, int startx, int starty, float force, float decay, RadiusStrategy radiusStrategy) {
        this.resistanceMap = resistanceMap;
        this.startx = startx;
        this.starty = starty;
        this.force = force;
        this.decay = decay;
        this.radiusStrategy = radiusStrategy;
        width = resistanceMap.length;
        height = resistanceMap[0].length;
        lightMap = new float[width][height];
        pool = Executors.newCachedThreadPool();

        float maxRadius = force / decay + 1;
        maxRadius = radiusStrategy.radius(maxRadius, maxRadius);

        int left = (int) Math.max(0, startx - maxRadius);
        int right = (int) Math.min(width - 1, startx + maxRadius);
        int top = (int) Math.max(0, starty - maxRadius);
        int bottom = (int) Math.min(height - 1, starty + maxRadius);

        lightMap[startx][starty] = force;

        //run rays out to edges
        LinkedList<LOSWorker> loss = new LinkedList<>();
        for (int x = left; x <= right; x++) {
            LOSWorker worker = new LOSWorker(new RayCastingLOS(), x, top);
            loss.add(worker);
            pool.execute(worker);
            worker = new LOSWorker(new RayCastingLOS(), x, bottom);
            loss.add(worker);
            pool.execute(worker);
        }
        for (int y = top; y <= bottom; y++) {
            LOSWorker worker = new LOSWorker(new RayCastingLOS(), left, y);
            loss.add(worker);
            pool.execute(worker);
            worker = new LOSWorker(new RayCastingLOS(), right, y);
            loss.add(worker);
            pool.execute(worker);
        }



        pool.shutdown();
        while (!pool.isTerminated()) {
        }//spin lock until pool tasks are done

        for (LOSWorker worker : loss) {
            float brightness = force;
            Point previous = null;
            for (Point p : worker.los.getLastPath()) {
                lightMap[p.x][p.y] = Math.max(lightMap[p.x][p.y], brightness);
                if (previous != null && (p.x != startx || p.y != starty)) {//only update if not in starting cell
                    brightness -= radiusStrategy.radius(previous.x, previous.y, p.x, p.y) * decay;//update brightness for passing out of this cell
                    brightness -=  resistanceMap[p.x][p.y];
                }
                previous = p;
            }
        }

        return lightMap;
    }

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, int startx, int starty, float radius) {
        return calculateFOV(resistanceMap, startx, starty, 1, 1 / radius, BasicRadiusStrategy.CIRCLE);
    }

    private class LOSWorker implements Runnable {

        LOSSolver los;
        int targetx, targety;

        LOSWorker(LOSSolver fov, int targetx, int targety) {
            this.los = fov;
            this.targetx = targetx;
            this.targety = targety;
        }

        @Override
        public void run() {
            los.isReachable(resistanceMap, startx, starty, targetx, targety, force, decay, radiusStrategy);
        }
    }
}
