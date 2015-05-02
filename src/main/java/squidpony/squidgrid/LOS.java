package squidpony.squidgrid;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import squidpony.squidmath.Bresenham;
import squidpony.squidmath.Elias;

/**
 * Line of Sight (LOS) algorithms find if there is or is not a path between two
 * given points.
 *
 * The line found between two points will end at either the target, the
 * obstruction closest to the start, or the edge of the map.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class LOS {

    public static final int //constants to indicate desired type of solving algorithm to use
            /**
             * A Bresenham-based line-of-sight algorithm.
             */
            BRESENHAM = 1,
            /**
             * Uses Wu's Algorithm as modified by Elias to draw the line. Does
             * not end at an obstruction but rather returns one of the possible
             * attempted paths in full.
             */
            ELIAS = 2,
            /**
             * Uses a series of rays internal to the start and end point to
             * determine visibility. Does not respect translucency.
             */
            RAY = 3;
    private Queue<Point> lastPath = new LinkedList<>();
    private int type;
    private double[][] resistanceMap;
    private int startx, starty, targetx, targety;

    public Radius getRadiusStrategy() {
        return radiusStrategy;
    }

    public void setRadiusStrategy(Radius radiusStrategy) {
        this.radiusStrategy = radiusStrategy;
    }

    private Radius radiusStrategy = Radius.CIRCLE;

    public LOS() {
        this(BRESENHAM);
    }

    public LOS(int type) {
        this.type = type;
    }

    /**
     * Returns true if a line can be drawn from the start point to the target
     * point without intervening obstructions.
     *
     * Uses RadiusStrategy.CIRCLE, or whatever RadiusStrategy was set with setRadiusStrategy .
     *
     * @param walls '#' is fully opaque, anything else is fully transparent, as always this uses x,y indexing.
     * @param startx
     * @param starty
     * @param targetx
     * @param targety
     * @return
     */
    public boolean isReachable(char[][] walls, int startx, int starty, int targetx, int targety) {
        if(walls.length < 1) return false;
        double[][] resMap = new double[walls.length][walls[0].length];
        for(int x = 0; x < walls.length; x++)
        {
            for(int y = 0; y < walls[0].length; y++)
            {
                resMap[x][y] = (walls[x][y] == '#') ? 1.0 : 0.0;
            }
        }
        return isReachable(resMap, startx, starty, targetx, targety, this.radiusStrategy);
    }

    /**
     * Returns true if a line can be drawn from the start point to the target
     * point without intervening obstructions.
     *
     * Does not take into account resistance less than opaque or distance cost.
     *
     * Uses RadiusStrategy.CIRCLE, or whatever RadiusStrategy was set with setRadiusStrategy .
     *
     * @param resistanceMap 0.0 is fully transparent, 1.0 is fully opaque, as always this uses x,y indexing.
     * @param startx
     * @param starty
     * @param targetx
     * @param targety
     * @return
     */
    public boolean isReachable(double[][] resistanceMap, int startx, int starty, int targetx, int targety) {
        return isReachable(resistanceMap, startx, starty, targetx, targety, this.radiusStrategy);
    }

    /**
     * Returns true if a line can be drawn from the start point to the target
     * point without intervening obstructions.
     *
     * @param resistanceMap 0.0 is fully transparent, 1.0 is fully opaque, as always this uses x,y indexing.
     * @param startx
     * @param starty
     * @param targetx
     * @param targety
     * @param radiusStrategy the strategy to use in computing unit distance
     * @return
     */
    public boolean isReachable(double[][] resistanceMap, int startx, int starty, int targetx, int targety, Radius radiusStrategy) {
        this.resistanceMap = resistanceMap;
        this.startx = startx;
        this.starty = starty;
        this.targetx = targetx;
        this.targety = targety;
        switch (type) {
            case BRESENHAM:
                return bresenhamReachable(radiusStrategy);
            case ELIAS:
                return eliasReachable(radiusStrategy);
            case RAY:
                return rayReachable(radiusStrategy);
        }
        return false;
    }

    /**
     * Returns true if a line can be drawn from the start point to the target
     * point without intervening obstructions.
     *
     * @param walls '#' is fully opaque, anything else is fully transparent, as always this uses x,y indexing.
     * @param startx
     * @param starty
     * @param targetx
     * @param targety
     * @param radiusStrategy the strategy to use in computing unit distance
     * @return
     */
    public boolean isReachable(char[][] walls, int startx, int starty, int targetx, int targety, Radius radiusStrategy) {
        if(walls.length < 1) return false;
        double[][] resMap = new double[walls.length][walls[0].length];
        for(int x = 0; x < walls.length; x++)
        {
            for(int y = 0; y < walls[0].length; y++)
            {
                resMap[x][y] = (walls[x][y] == '#') ? 1.0 : 0.0;
            }
        }
        return isReachable(resMap, startx, starty, targetx, targety, radiusStrategy);
    }
    /**
     * Returns the path of the last LOS calculation, with the starting point as
     * the head of the queue.
     *
     * @return
     */
    public Queue<Point> getLastPath() {
        return lastPath;
    }

    private boolean bresenhamReachable(Radius radiusStrategy) {
        Queue<Point> path = Bresenham.line2D(startx, starty, targetx, targety);
        lastPath = new LinkedList<>();
        lastPath.add(new Point(startx, starty));
        double force = 1;
        double decay = 1 / radiusStrategy.radius(startx, starty, targetx, targety);
        double currentForce = force;
        for (Point p : path) {
            lastPath.offer(p);
            if (p.x == targetx && p.y == targety) {
                return true;//reached the end 
            }
            if (p.x != startx || p.y != starty) {//don't discount the start location even if on resistant cell
                currentForce -= resistanceMap[p.x][p.y];
            }
            double r = radiusStrategy.radius(startx, starty, p.x, p.y);
            if (currentForce - (r * decay) <= 0) {
                return false;//too much resistance
            }
        }
        return false;//never got to the target point
    }

    private boolean rayReachable(Radius radiusStrategy) {
        lastPath = new LinkedList<>();//save path for later retreival
        lastPath.add(new Point(startx, starty));
        if (startx == targetx && starty == targety) {//already there!
            return true;
        }

        int width = resistanceMap.length;
        int height = resistanceMap[0].length;

        Point2D.Double start = new Point2D.Double(startx, starty);
        Point2D.Double end = new Point2D.Double(targetx, targety);
        //find out which direction to step, on each axis
        int stepX = (int) Math.signum(end.x - start.x);
        int stepY = (int) Math.signum(end.y - start.y);

        double deltaY = end.x - start.x;
        double deltaX = end.y - start.y;

        deltaX = Math.abs(deltaX);
        deltaY = Math.abs(deltaY);

        int testX = (int) start.x;
        int testY = (int) start.y;

        double maxX = (float) (start.x % 1);
        double maxY = (float) (start.y % 1);

        int endTileX = (int) end.x;
        int endTileY = (int) end.y;

        Point2D.Double collisionPoint = new Point2D.Double();
        while (testX >= 0 && testX < width && testY >= 0 && testY < height && (testX != endTileX || testY != endTileY)) {
            lastPath.add(new Point(testX, testY));
            if (maxX < maxY) {
                maxX += deltaX;
                testX += stepX;
                if (resistanceMap[testX][testY] >= 1f) {
                    collisionPoint.y = testY;
                    collisionPoint.x = testX;
                    end = collisionPoint;
                    break;
                }
            } else if (maxY < maxX) {
                maxY += deltaY;
                testY += stepY;
                if (resistanceMap[testX][testY] >= 1f) {
                    collisionPoint.y = testY;
                    collisionPoint.x = testX;
                    end = collisionPoint;
                    break;
                }
            } else {//directly on diagonal, move both full step
                maxY += deltaY;
                testY += stepY;
                maxX += deltaX;
                testX += stepX;
                if (resistanceMap[testX][testY] >= 1f) {
                    collisionPoint.y = testY;
                    collisionPoint.x = testX;
                    end = collisionPoint;
                    break;
                }
            }
            if (radiusStrategy.radius(testX, testY, start.x, start.y) > radiusStrategy.radius(startx, starty, targetx, targety)) {//went too far
                break;
            }
        }

        if (end.x >= 0 && end.x < width && end.y >= 0 && end.y < height) {
            lastPath.add(new Point((int) end.x, (int) end.y));
        }

        return (int) end.x == targetx && (int) end.y == targety;
    }

    private boolean eliasReachable(Radius radiusStrategy) {
        List<Point> ePath = Elias.line(startx, starty, targetx, targety);
        lastPath = new LinkedList<>(ePath);//save path for later retreival

        HashMap<eliasWorker, Thread> pool = new HashMap<>();

        for (Point p : ePath) {
            eliasWorker worker = new eliasWorker(p.x, p.y, radiusStrategy);
            Thread thread = new Thread(worker);
            thread.start();
            pool.put(worker, thread);
        }

        for (eliasWorker w : pool.keySet()) {
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

    private class eliasWorker implements Runnable {

        private Queue<Point> path;
        private boolean succeeded = false;
        private int testx, testy;
        private Radius radiusStrategy;
        eliasWorker(int testx, int testy, Radius radiusStrategy) {
            this.testx = testx;
            this.testy = testy;
            this.radiusStrategy = radiusStrategy;
        }

        @Override
        public void run() {
            LOS los1 = new LOS(BRESENHAM);
            LOS los2 = new LOS(BRESENHAM);
            //if a non-solid midpoint on the path can see both the start and end, consider the two ends to be able to see each other
            if (resistanceMap[testx][testy] < 1
                    && radiusStrategy.radius(startx, starty, testx, testy) <= radiusStrategy.radius(startx, starty, targetx, targety)
                    && los1.isReachable(resistanceMap, testx, testy, targetx, targety, radiusStrategy)
                    && los2.isReachable(resistanceMap, startx, starty, testx, testy, radiusStrategy)) {

                //record actual sight path used
                path = new LinkedList<>(los2.lastPath);
                path.addAll(los1.lastPath);
                succeeded = true;
            }
        }
    }
}
