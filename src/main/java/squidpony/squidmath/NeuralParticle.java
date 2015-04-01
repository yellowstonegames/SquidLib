package squidpony.squidmath;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import squidpony.annotation.Beta;

/**
 * Creates a field of particles that tend to form a neuron image type
 * distribution. The distribution tends to reach towards the largest area of
 * empty space, but features many nice branches and curls as well.
 *
 * If no points are added before the populate method is run, the center of the
 * area is chosen as the single pre-populated point.
 *
 * Based on work by Nolithius
 *
 * http://www.nolithius.com/game-development/neural-particle-deposition
 *
 * Source code is available on GitHub:
 * https://github.com/Nolithius/neural-particle As well as Google Code:
 * http://code.google.com/p/neural-particle/
 *
 * @author @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class NeuralParticle {

    private final RNG rng;
    private final int maxDistance, minDistance, width, height;
    private final List<Point> distribution = new LinkedList<>();

    public NeuralParticle(int width, int height, int maxDistance, RNG rng) {
        this.rng = rng;
        this.maxDistance = maxDistance;
        this.width = width;
        this.height = height;
        minDistance = 1;
    }

    /**
     * Populates the field with given number of points.
     *
     * @param quantity
     */
    public void populate(int quantity) {
        for (int i = 0; i < quantity; i++) {
            add(createPoint());
        }
    }

    /**
     * Returns a list of the current distribution.
     *
     * @return
     */
    public List<Point> asList() {
        return new LinkedList<>(distribution);
    }

    /**
     * Returns an integer mapping of the current distribution.
     *
     * @param scale the value that active points will hold
     * @return
     */
    public int[][] asIntMap(int scale) {
        int ret[][] = new int[width][height];
        for (Point p : distribution) {
            ret[p.x][p.y] = scale;
        }
        return ret;
    }

    /**
     * Adds a single specific point to the distribution.
     *
     * @param point
     */
    public void add(Point point) {
        distribution.add(point);
    }

    /**
     * Creates a pip that falls within the required distance from the current
     * distribution. Does not add the pip to the distribution.
     *
     * @return the created pip
     */
    public Point createPoint() {
        Point randomPoint = randomPoint();
        Point nearestPoint = nearestPoint(randomPoint);
        double pointDistance = randomPoint.distance(nearestPoint);
        // Too close, toss
        while (pointDistance < minDistance) {
            randomPoint = randomPoint();
            nearestPoint = nearestPoint(randomPoint);
            pointDistance = randomPoint.distance(nearestPoint);
        }
        // Adjust if we're too far
        if (pointDistance > maxDistance) {
            // Calculate unit vector
            double unitX = (randomPoint.x - nearestPoint.x) / pointDistance;
            double unitY = (randomPoint.y - nearestPoint.y) / pointDistance;
            randomPoint.x = (int) (rng.between(minDistance, maxDistance + 1) * unitX + nearestPoint.x);
            randomPoint.y = (int) (rng.between(minDistance, maxDistance + 1) * unitY + nearestPoint.y);
        }
        return randomPoint;
    }

    private Point nearestPoint(Point point) {
        if (distribution.isEmpty()) {
            Point center = new Point(width / 2, height / 2);
            distribution.add(center);
            return center;
        }

        Point nearestPoint = distribution.get(0);
        double nearestDistance = point.distance(nearestPoint);
        for (Point candidatePoint : distribution) {
            double candidateDistance = point.distance(candidatePoint);
            if (candidateDistance > 0 && candidateDistance <= maxDistance) {
                return candidatePoint;
            }

            if (candidateDistance < nearestDistance) {
                nearestPoint = candidatePoint;
                nearestDistance = candidateDistance;
            }
        }
        return nearestPoint;
    }

    private Point randomPoint() {
        return new Point(rng.nextInt(width), rng.nextInt(height));
    }

}
