package squidpony.squidgrid.los;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;
import squidpony.squidgrid.RadiusStrategy;
import squidpony.squidgrid.BasicRadiusStrategy;
import squidpony.squidmath.Bresenham;
import squidpony.squidmath.Point3D;

/**
 * A Bresenham-based line-of-sight algorithm.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class BresenhamLOS implements LOSSolver3D {

    Queue<Point3D> lastPath3D = new LinkedList<>();
    Queue<Point> lastPath = new LinkedList<>();

    @Override
    public boolean isReachable(float[][][] resistanceMap, int startx, int starty, int startz, int targetx, int targety, int targetz, float force, float decay, RadiusStrategy radiusStrategy) {
        Queue<Point3D> path = Bresenham.line3D(startx, starty, startz, startx, starty, startz);
        lastPath3D = new LinkedList<>(path);//save path for later retreival
        float currentForce = force;
        for (Point3D p : path) {
            if (p.x == targetx && p.y == targety) {
                return true;//reached the end 
            }
            if (p.x != startx || p.y != starty || p.z != startz) {//don't discount the start location even if on resistant cell
                currentForce -= resistanceMap[p.x][p.y][p.z];
            }
            double radius = radiusStrategy.radius(startx, starty, p.x, p.y);
            if (currentForce - (radius * decay) <= 0) {
                return false;//too much resistance
            }
        }
        return false;//never got to the target point
    }

    @Override
    public Queue<Point3D> getLastPath3D() {
        return lastPath3D;
    }

    @Override
    public boolean isReachable(float[][][] resistanceMap, int startx, int starty, int startz, int targetx, int targety, int targetz) {
        return isReachable(resistanceMap, startx, starty, startz, targetx, targety, targetz, Float.MAX_VALUE, 0f, BasicRadiusStrategy.CIRCLE);
    }

    @Override
    public boolean isReachable(float[][] resistanceMap, int startx, int starty, int targetx, int targety, float force, float decay, RadiusStrategy radiusStrategy) {
        Queue<Point> path = Bresenham.line2D(startx, starty, targetx, targety);
        lastPath = new LinkedList<>(path);//save path for later retreival
        float currentForce = force;
        for (Point p : path) {
            if (p.x == targetx && p.y == targety) {
                return true;//reached the end 
            }
            if (p.x != startx || p.y != starty) {//don't discount the start location even if on resistant cell
                currentForce -=  resistanceMap[p.x][p.y];
            }
            double radius = radiusStrategy.radius(startx, starty, p.x, p.y);
            if (currentForce - (radius * decay) <= 0) {
                return false;//too much resistance
            }
        }
        return false;//never got to the target point
    }

    @Override
    public Queue<Point> getLastPath() {
        return lastPath;
    }

    @Override
    public boolean isReachable(float[][] resistanceMap, int startx, int starty, int targetx, int targety) {
        return isReachable(resistanceMap, startx, starty, targetx, targety, Float.MAX_VALUE, 0f, BasicRadiusStrategy.CIRCLE);
    }
}
