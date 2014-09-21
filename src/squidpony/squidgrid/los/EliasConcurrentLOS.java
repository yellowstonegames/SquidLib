package squidpony.squidgrid.los;

import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import squidpony.squidgrid.RadiusStrategy;
import squidpony.squidgrid.BasicRadiusStrategy;
import squidpony.squidmath.Elias;

/**
 * Uses Wu's Algorithm as modified by Elias to draw the line.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class EliasConcurrentLOS implements LOSSolver {

    private Queue<Point> lastPath = new LinkedList<>();
    private float[][] resistanceMap;
    private int startx, starty, targetx, targety;
    private float force, decay, checkRadius;
    private RadiusStrategy radiusStrategy;

    @Override
    public boolean isReachable(float[][] resistanceMap, int startx, int starty, int targetx, int targety, float force, float decay, RadiusStrategy radiusStrategy) {
        this.resistanceMap = resistanceMap;
        this.startx = startx;
        this.starty = starty;
        this.targetx = targetx;
        this.targety = targety;
        this.force = force;
        this.decay = decay;
        this.radiusStrategy = radiusStrategy;
        List<Point> ePath = Elias.line(startx, starty, targetx, targety);
        lastPath = new LinkedList<>(ePath);//save path for later retreival

        checkRadius = radiusStrategy.radius(startx, starty, targetx, targety);

        HashMap<LOSWorker, Thread> pool = new HashMap<>();

        for (Point p : ePath) {
            LOSWorker worker = new LOSWorker(p.x, p.y);
            Thread thread = new Thread(worker);
            thread.start();
            pool.put(worker, thread);
        }

        for (LOSWorker w : pool.keySet()) {
            try {
                pool.get(w).join();
            } catch (InterruptedException ex) {
            }
            if (w.succeeded) {
                lastPath = w.path;
                return true;
            }
        }

        return false;//never got to the target point
    }

    @Override
    public boolean isReachable(float[][] resistanceMap, int startx, int starty, int targetx, int targety) {
        return isReachable(resistanceMap, startx, starty, targetx, targety, Float.MAX_VALUE, 0f, BasicRadiusStrategy.CIRCLE);
    }

    @Override
    public Queue<Point> getLastPath() {
        return lastPath;
    }

    private class LOSWorker implements Runnable {

        private Queue<Point> path;
        private boolean succeeded = false;
        private int testx, testy;

        LOSWorker(int testx, int testy) {
            this.testx = testx;
            this.testy = testy;
        }

        @Override
        public void run() {
            BresenhamLOS los1 = new BresenhamLOS(), los2 = new BresenhamLOS();
            //if a non-solid midpoint on the path can see both the start and end, consider the two ends to be able to see each other
            if (resistanceMap[testx][testy] < 1
                    && radiusStrategy.radius(startx, starty, testx, testy) <= checkRadius
                    && los1.isReachable(resistanceMap, testx, testy, targetx, targety, force - (radiusStrategy.radius(startx, starty, testx, testy) * decay), decay, radiusStrategy)
                    && los2.isReachable(resistanceMap, startx, starty, testx, testy, force, decay, radiusStrategy)) {

                //record actual sight path used
                path = new LinkedList<>(los2.lastPath);
                path.addAll(los1.lastPath);
                succeeded = true;
            }
        }
    }
}
