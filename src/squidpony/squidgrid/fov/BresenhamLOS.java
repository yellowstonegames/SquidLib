package squidpony.squidgrid.fov;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;
import squidpony.squidmath.Bresenham;
import squidpony.squidmath.Point3D;

/**
 * A Bresenham-based line-of-sight algorithm.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class BresenhamLOS implements LOSSolver {

    Queue<Point3D> lastPath = new LinkedList<>();

    @Override
    public boolean isReachable(float[][] map, int x, int y, int targetX, int targetY, float force) {
        Queue<Point3D> path = Bresenham.line2D(x, y, targetX, targetY);
        lastPath = new LinkedList<>(path);
        path.poll();//remove starting point
        for (Point3D p : path) {
            if (p.x == targetX && p.y == targetY) {
                return true;//reached the end 
            }
            force -= map[p.x][p.y];
            if (force <= 0) {
                return false;//too much resistance
            }
        }
        return true;//made it all the way to the target
    }

    @Override
    public Queue<Point> getLastPath() {
        //copy the Point3D elements into a 2D Point structure only if needed
        Queue<Point> returnPath = new LinkedList<>();
        for (Point3D p : lastPath) {
            returnPath.add(new Point(p.x, p.y));
        }
        return returnPath;
    }
}
