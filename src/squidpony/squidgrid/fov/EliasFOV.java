package squidpony.squidgrid.fov;

import squidpony.squidgrid.util.RadiusStrategy;
import java.awt.Point;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import squidpony.annotation.Beta;
import squidpony.squidgrid.los.EliasConcurrentLOS;
import squidpony.squidgrid.util.BasicRadiusStrategy;

/**
 * Uses the Elias line running to raycast.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class EliasFOV implements FOVSolver {

    private float[][] lightMap, resistanceMap;
    private float maxRadius, force, decay;
    private int width, height, startx, starty;
    private RadiusStrategy radiusStrategy;

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, final int startx, final int starty, float force, float decay, final RadiusStrategy radiusStrategy) {
        this.resistanceMap = resistanceMap;
        width = resistanceMap.length;
        height = resistanceMap[0].length;
        lightMap = new float[width][height];
        this.force = force;
        this.decay = decay;
        this.radiusStrategy = radiusStrategy;
        this.startx = startx;
        this.starty = starty;

        maxRadius = force / decay;
        int left = (int) Math.max(0, startx - maxRadius - 1);
        int right = (int) Math.min(width - 1, startx + maxRadius + 1);
        int top = (int) Math.max(0, starty - maxRadius - 1);
        int bottom = (int) Math.min(height - 1, starty + maxRadius + 1);

        HashMap<LOSWorker, Thread> pool = new HashMap<>();
        //run rays out to edges
        for (int x = left; x <= right; x++) {
            addWorker(pool, x, top);
            addWorker(pool, x, bottom);
        }
        for (int y = top; y <= bottom; y++) {
            addWorker(pool, left, y);
            addWorker(pool, right, y);
        }

        for (LOSWorker w : pool.keySet()) {
            try {
                pool.get(w).join();
            } catch (InterruptedException ex) {
            }

            LinkedList<Point> path = new LinkedList<>(w.path);
            Collections.sort(path, new Comparator<Point>() {
                @Override
                public int compare(Point o1, Point o2) {
                    return (int) Math.signum(radiusStrategy.radius(o1.x, o1.y, startx, starty) - radiusStrategy.radius(o2.x, o2.y, startx, starty));
                }
            });

            Point previous = null;
            float brightness = force;
            for (Point p : path) {
                if (brightness <= 0) {
                    break;//no more light to spread out
                }

                if (previous != null) {
                    brightness = Math.max(brightness, lightMap[previous.x][previous.y]);
                    brightness -= radiusStrategy.radius(p.x, p.y, previous.x, previous.y) * decay;
                    brightness -=  resistanceMap[previous.x][previous.y];
                }

                lightMap[p.x][p.y] = Math.max(lightMap[p.x][p.y], brightness);
                brightness = lightMap[p.x][p.y];
                previous = p;
            }
        }

        return lightMap;
    }

    private void addWorker(HashMap<LOSWorker, Thread> pool, int x, int y) {
        LOSWorker worker = new LOSWorker(x, y);
        Thread thread = new Thread(worker);
        thread.start();
        pool.put(worker, thread);
    }

    @Override
    public float[][] calculateFOV(float[][] resistanceMap, int startx, int starty, float radius) {
        return calculateFOV(resistanceMap, startx, starty, 1, 1 / radius, BasicRadiusStrategy.CIRCLE);
    }

    private class LOSWorker implements Runnable {

        private Queue<Point> path;
        private int testx, testy;

        LOSWorker(int testx, int testy) {
            this.testx = testx;
            this.testy = testy;
        }

        @Override
        public void run() {
            EliasConcurrentLOS los = new EliasConcurrentLOS();
            los.isReachable(resistanceMap, startx, starty, testx, testy, force, decay, radiusStrategy);
            path = los.getLastPath();
        }
    }
}
