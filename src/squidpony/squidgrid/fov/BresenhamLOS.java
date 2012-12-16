package squidpony.squidgrid.fov;

import java.util.Queue;
import squidpony.squidmath.Bresenham;
import squidpony.squidmath.Point3D;

/**
 * A Bresenham-based line-of-sight algorithm.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public class BresenhamLOS implements LOSSolver {

    @Override
    public boolean isVisible(FOVCell[][] map, int x, int y, int targetX, int targetY, String key) {
        //check to see if target is in total darkness
        if (map[targetX][targetY].getCurrentLight(key) <= 0) {
            return false;//too dark, can't see it
        }

        Queue<Point3D> path = Bresenham.line2D(x, y, targetX, targetY);
        path.poll();//remove starting point
        for (Point3D p : path) {
            if (map[p.x][p.y].getTransparency(key) <= 0 && p.x != targetX && p.y != targetY) {
                return false;//found a blocking instance
            }
        }
        return true;//made it all the way to the target
    }

    @Override
    public boolean isReachable(FOVCell[][] map, int x, int y, int targetX, int targetY, float force, String key) {
        Queue<Point3D> path = Bresenham.line2D(x, y, targetX, targetY);
        path.poll();//remove starting point
        for (Point3D p : path) {
            if (p.x == targetX && p.y == targetY) {
                return true;//reached the end 
            }
            force -= map[p.x][p.y].getReistance(key);
            if (force <= 0) {
                return false;//too much resistance
            }
        }
        return true;//made it all the way to the target
    }
}
