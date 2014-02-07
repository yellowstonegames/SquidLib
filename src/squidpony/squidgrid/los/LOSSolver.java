package squidpony.squidgrid.los;

import java.awt.Point;
import java.util.Queue;
import squidpony.squidgrid.util.RadiusStrategy;

/**
 * An interface for Line of Sight algorithms.
 *
 * Line of Site (LOS) algorithms find if there is or is not a path between two
 * given points.
 *
 * If the decay is set to 0 then the line will be run until an obstruction or
 * target is reached. Otherwise the target is considered not reached if the
 * calculation of force, decay, and resistances prevent the line from reaching
 * the target.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public interface LOSSolver {

    /**
     * Returns true if a line can be drawn from the start point to the target
     * point without intervening obstructions.
     *
     * @param resistanceMap marks the level of resistance the the line per cell
     * @param startx
     * @param starty
     * @param targetx
     * @param targety
     * @param force the amount of impetus to start with
     * @param decay the amount the force is reduced per unit distance
     * @param radiusStrategy the strategy to use in computing unit distance
     * @return
     */
    public boolean isReachable(float[][] resistanceMap, int startx, int starty, int targetx, int targety, float force, float decay, RadiusStrategy radiusStrategy);

    /**
     * Returns true if a line can be drawn from the start point to the target
     * point without intervening obstructions.
     *
     * Does not take into account resistance less than opaque or distance cost.
     *
     * Uses the implementation's default RadiusStrategy.
     *
     * @param resistanceMap
     * @param startx
     * @param starty
     * @param targetx
     * @param targety
     * @return
     */
    public boolean isReachable(float[][] resistanceMap, int startx, int starty, int targetx, int targety);

    /**
     * Returns the path of the last LOS calculation, with the starting point as
     * the head of the queue.
     *
     * @return
     */
    public Queue<Point> getLastPath();
}
