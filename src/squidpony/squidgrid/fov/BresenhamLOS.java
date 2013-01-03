package squidpony.squidgrid.fov;

import java.awt.Point;
import java.util.Deque;
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

    Queue<Point3D> lastPath = new LinkedList<Point3D>();

    @Override
    public boolean isReachable(FOVCell[][] map, int x, int y, int targetX, int targetY, float force, String key) {
        Queue<Point3D> path = Bresenham.line2D(x, y, targetX, targetY);
        lastPath = new LinkedList<Point3D>(path);
        path.poll();//remove starting point
        for (Point3D p : path) {
            if (p.x == targetX && p.y == targetY) {
                return true;//reached the end 
            }
            force -= map[p.x][p.y].getResistance(key);
            if (force <= 0) {
                return false;//too much resistance
            }
        }
        return true;//made it all the way to the target
    }

    @Override
    public Queue<Point> getLastPath() {
        //copy the Point3D elements into a 2D Point structure only if needed
        Queue<Point> returnPath = new LinkedList<Point>();
        for (Point3D p : lastPath) {
            returnPath.add(new Point(p.x, p.y));
        }
        return returnPath;
    }
}
