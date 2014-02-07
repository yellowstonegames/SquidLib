package squidpony.squidgrid.los;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Queue;
import squidpony.annotation.Beta;
import squidpony.squidgrid.util.RadiusStrategy;
import squidpony.squidgrid.util.BasicRadiusStrategy;

/**
 * Uses a series of rays internal to the start and end point to determine
 * visibility.
 * 
 * Does not respect translucency.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class RayCastingLOS implements LOSSolver {

    private Queue<Point> path;
    private float maxRadius, gap;
    private int width, height;
    private float[][] resistanceMap;
    private RadiusStrategy rStrat;

    /**
     * Creates an instance of this solver which uses the default gap and step.
     */
    public RayCastingLOS() {
    }

    @Override
    public boolean isReachable(float[][] resistanceMap, int startx, int starty, int targetx, int targety, float force, float decay, RadiusStrategy radiusStrategy) {
        this.resistanceMap = resistanceMap;
        this.rStrat = radiusStrategy;
        width = resistanceMap.length;
        height = resistanceMap[0].length;
        path = new LinkedList<>();
        maxRadius = force / decay;

        path.add(new Point(startx, starty));

        Point2D.Float stop = castRay(new Point2D.Float(startx, starty), new Point2D.Float(targetx, targety));

        if (stop != null && stop.x >= 0 && stop.x < width && stop.y >= 0 && stop.y < height) {
            path.add(new Point((int) stop.x, (int) stop.y));
        }

        return stop == null || ((int) stop.x == targetx && (int) stop.y == targety);
    }

    public Point2D.Float castRay(Point2D.Float start, Point2D.Float end) {
        // normalise the points
        Point2D.Float p1 = start;
        Point2D.Float p2 = end;
        if (p1.x == p2.x && p1.y == p2.y) {//already there!
            return end;
        }
        //find out which direction to step, on each axis
        int stepX = (int) Math.signum(p2.x - p1.x);
        int stepY = (int) Math.signum(p2.y - p1.y);

        float deltaY = p2.x - p1.x;
        float deltaX = p2.y - p1.y;

        deltaX = Math.abs(deltaX);
        deltaY = Math.abs(deltaY);

        int testX = (int) p1.x;
        int testY = (int) p1.y;

        float maxX = (float) (p1.x % 1);

        float maxY = (float) (p1.y % 1);

        int endTileX = (int) p2.x;
        int endTileY = (int) p2.y;

        Point2D.Float collisionPoint = new Point2D.Float();
        while (testX >= 0 && testX < width && testY >= 0 && testY < height && (testX != endTileX || testY != endTileY)) {
            path.add(new Point(testX, testY));
            if (maxX < maxY) {
                maxX += deltaX;
                testX += stepX;
                if (resistanceMap[testX][testY] >= 1f) {
                    collisionPoint.y = testY;
                    collisionPoint.x = testX;
                    return collisionPoint;
                }
            } else if (maxY < maxX) {
                maxY += deltaY;
                testY += stepY;
                if (resistanceMap[testX][testY] >= 1f) {
                    collisionPoint.y = testY;
                    collisionPoint.x = testX;
                    return collisionPoint;
                }
            } else {//directly on diagonal, move both full step
                maxY += deltaY;
                testY += stepY;
                maxX += deltaX;
                testX += stepX;
                if (resistanceMap[testX][testY] >= 1f) {
                    collisionPoint.y = testY;
                    collisionPoint.x = testX;
                    return collisionPoint;
                }
            }
            if (rStrat.radius(testX, testY, start.x, start.y) > maxRadius) {//went too far
                return null;
            }
        }

        //no intersection found, just return end point:
        return end;
    }

    @Override
    public boolean isReachable(float[][] resistanceMap, int startx, int starty, int targetx, int targety) {
        return isReachable(resistanceMap, startx, starty, targetx, targety, 1, 0, BasicRadiusStrategy.CIRCLE);
    }

    @Override
    public Queue<Point> getLastPath() {
        return path;
    }
}
