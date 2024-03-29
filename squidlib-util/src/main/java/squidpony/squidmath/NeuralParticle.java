/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package squidpony.squidmath;


import squidpony.annotation.Beta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a field of particles that tend to form a neuron image type
 * distribution. The distribution tends to reach towards the largest area of
 * empty space, but features many nice branches and curls as well.
 * If no points are added before the populate method is run, the center of the
 * area is chosen as the single pre-populated point.
 * <br>
 * <a href="http://www.nolithius.com/game-development/neural-particle-deposition">Based on work by Nolithius</a>
 * <br>
 * Source code is available on <a href="https://github.com/Nolithius/neural-particle">GitHub</a>,
 * as well as <a href="http://code.google.com/p/neural-particle/">Google Code (now archived)</a>
 * <br>
 * This class is marked Beta because no test or demo was ever written to use it.
 * Who could be to blame for this omission...
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class NeuralParticle implements Serializable{
    private static final long serialVersionUID = -3742942580678517149L;

    private final IRNG rng;
    private final int maxDistance, minDistance, width, height;
    private final ArrayList<Coord> distribution = new ArrayList<>();

    public NeuralParticle(int width, int height, int maxDistance, IRNG rng) {
        this.rng = rng;
        this.maxDistance = maxDistance;
        this.width = width;
        this.height = height;
        minDistance = 1;
    }

    /**
     * Populates the field with given number of points.
     *
     * @param quantity the number of points to insert
     */
    public void populate(int quantity) {
        for (int i = 0; i < quantity; i++) {
            add(createPoint());
        }
    }

    /**
     * Returns a list of the current distribution.
     *
     * @return the distribution as a List of Coord
     */
    public List<Coord> asList() {
        return new ArrayList<>(distribution);
    }

    /**
     * Returns an integer mapping of the current distribution.
     *
     * @param scale the value that active points will hold
     * @return a 2D int array, with all elements equal to either 0 or scale
     */
    public int[][] asIntMap(int scale) {
        int[][] ret = new int[width][height];
        for (Coord p : distribution) {
            ret[p.x][p.y] = scale;
        }
        return ret;
    }

    /**
     * Adds a single specific point to the distribution.
     *
     * @param point the Coord, also called a pip here, to insert
     */
    public void add(Coord point) {
        distribution.add(point);
    }

    /**
     * Creates a pip that falls within the required distance from the current
     * distribution. Does not add the pip to the distribution.
     *
     * @return the created pip
     */
    public Coord createPoint() {
        Coord randomPoint = randomPoint();
        Coord nearestPoint = nearestPoint(randomPoint);
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
            randomPoint = Coord.get( (int) (rng.between(minDistance, maxDistance + 1) * unitX + nearestPoint.x)
                                   , (int) (rng.between(minDistance, maxDistance + 1) * unitY + nearestPoint.y));
        }
        return randomPoint;
    }

    private Coord nearestPoint(Coord point) {
        if (distribution.isEmpty()) {
            Coord center = Coord.get(width / 2, height / 2);
            distribution.add(center);
            return center;
        }

        Coord nearestPoint = distribution.get(0);
        double nearestDistance = point.distance(nearestPoint);
        for (Coord candidatePoint : distribution) {
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

    private Coord randomPoint() {
        return Coord.get(rng.nextInt(width), rng.nextInt(height));
    }

}
