package squidpony.squidgrid.fov;

import java.awt.Point;
import java.util.Queue;

/**
 * An interface for Line of Sight algorithms.
 *
 * Line of Site (LOS) algorithms find if there is or is not a path between two
 * given points.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public interface LOSSolver {

    /**
     * Returns true if the target can be affected by physical objects from the
     * provided starting coordinates.
     *
     * If the line run from the origin to the target is blocked, the target is
     * considered to not be visible. Light quantities are not taken into
     * account, so this method may be used for linear effects besides just
     * sight.
     *
     * @param map
     * @param x
     * @param y
     * @param targetX
     * @param targetY
     * @param force the amount of impetus to start with, will be decreased by
     * each cell's resistance as it passes through
     * @return
     */
    public boolean isReachable(float[][] map, int x, int y, int targetX, int targetY, float force);

    /**
     * Returns the path of the last LOS calculation, with the starting point as
     * the head of the queue.
     *
     * @return
     */
    public Queue<Point> getLastPath();
}
