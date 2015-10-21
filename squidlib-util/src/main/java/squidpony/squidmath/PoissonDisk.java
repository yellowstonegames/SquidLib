package squidpony.squidmath;

import squidpony.squidgrid.Radius;

import java.util.ArrayList;

/**
 * This provides a Uniform Poisson Disk Sampling technique that can be used to generate random points that have a
 * uniform minimum distance between each other. Due to Coord in SquidLib using ints and most Poisson Disk algorithms
 * using floating-point numbers, some imprecision is to be expected from rounding to the nearest integers x and y.
 *
 * The algorithm is from the "Fast Poisson Disk Sampling in Arbitrary Dimensions" paper by Robert Bridson
 * http://www.cs.ubc.ca/~rbridson/docs/bridson-siggraph07-poissondisk.pdf
 *
 * Adapted from C# by Renaud Bedard, which was adapted from Java source by Herman Tulleken
 * http://theinstructionlimit.com/fast-uniform-poisson-disk-sampling-in-c
 * Created by Tommy Ettinger on 10/20/2015.
 */
public class PoissonDisk {
    private static final float rootTwo = (float) Math.sqrt(2),
            pi = (float) Math.PI, pi2 = pi * 2f, halfPi = pi * 0.5f;
    
    private static final int defaultPointsPlaced = 10;
    private static final Radius disk = Radius.CIRCLE;


    /**
     * Get a list of Coords, each randomly positioned around the given center out to the given radius (measured with
     * Euclidean distance, so a true circle), but with the given minimum distance from any other Coord in the list.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param center the center of the circle to spray Coords into
     * @param radius the radius of the circle to spray Coords into
     * @param minimumDistance the minimum distance between Coords, in Euclidean distance as a float.
     * @param maxX one more than the highest x that can be assigned; typically an array length
     * @param maxY one more than the highest y that can be assigned; typically an array length
     * @return an ArrayList of Coord that satisfy the minimum distance; the length of the array can vary
     */
    public static ArrayList<Coord> sampleCircle(Coord center, float radius, float minimumDistance,
                                                int maxX, int maxY)
    {
        return sampleCircle(center, radius, minimumDistance, maxX, maxY, defaultPointsPlaced, new StatefulRNG());
    }

    /**
     * Get a list of Coords, each randomly positioned around the given center out to the given radius (measured with
     * Euclidean distance, so a true circle), but with the given minimum distance from any other Coord in the list.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param center the center of the circle to spray Coords into
     * @param radius the radius of the circle to spray Coords into
     * @param minimumDistance the minimum distance between Coords, in Euclidean distance as a float.
     * @param maxX one more than the highest x that can be assigned; typically an array length
     * @param maxY one more than the highest y that can be assigned; typically an array length
     * @param pointsPerIteration with small radii, this can be around 5; with larger ones, 30 is reasonable
     * @param rng an RNG to use for all random sampling.
     * @return an ArrayList of Coord that satisfy the minimum distance; the length of the array can vary
     */
    public static ArrayList<Coord> sampleCircle(Coord center, float radius, float minimumDistance,
                                                int maxX, int maxY, int pointsPerIteration, RNG rng)
    {
        int radius2 = Math.round(radius);
        return sample(center.translate(-radius2, -radius2), center.translate(radius2, radius2), radius, minimumDistance, maxX, maxY, pointsPerIteration, rng);
    }

    /**
     * Get a list of Coords, each randomly positioned within the rectangle between the given minPosition and
     * maxPosition, but with the given minimum distance from any other Coord in the list.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param minPosition the Coord with the lowest x and lowest y to be used as a corner for the bounding box
     * @param maxPosition the Coord with the highest x and highest y to be used as a corner for the bounding box
     * @param minimumDistance the minimum distance between Coords, in Euclidean distance as a float.
     * @param maxX one more than the highest x that can be assigned; typically an array length
     * @param maxY one more than the highest y that can be assigned; typically an array length
     * @return an ArrayList of Coord that satisfy the minimum distance; the length of the array can vary
     */
    public static ArrayList<Coord> sampleRectangle(Coord minPosition, Coord maxPosition, float minimumDistance,
                                                   int maxX, int maxY)
    {
        return sampleRectangle(minPosition, maxPosition, minimumDistance, maxX, maxY, defaultPointsPlaced, new StatefulRNG());
    }

    /**
     * Get a list of Coords, each randomly positioned within the rectangle between the given minPosition and
     * maxPosition, but with the given minimum distance from any other Coord in the list.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param minPosition the Coord with the lowest x and lowest y to be used as a corner for the bounding box
     * @param maxPosition the Coord with the highest x and highest y to be used as a corner for the bounding box
     * @param minimumDistance the minimum distance between Coords, in Euclidean distance as a float.
     * @param maxX one more than the highest x that can be assigned; typically an array length
     * @param maxY one more than the highest y that can be assigned; typically an array length
     * @param pointsPerIteration with small areas, this can be around 5; with larger ones, 30 is reasonable
     * @param rng an RNG to use for all random sampling.
     * @return an ArrayList of Coord that satisfy the minimum distance; the length of the array can vary
     */
    public static ArrayList<Coord> sampleRectangle(Coord minPosition, Coord maxPosition, float minimumDistance,
                                                   int maxX, int maxY, int pointsPerIteration, RNG rng)
    {
        return sample(minPosition, maxPosition, 0f, minimumDistance, maxX, maxY, pointsPerIteration, rng);
    }

    private static ArrayList<Coord> sample(Coord minPosition, Coord maxPosition, float rejectionDistance,
                                   float minimumDistance, int maxX, int maxY, int pointsPerIteration, RNG rng)
    {

        Coord center = minPosition.average(maxPosition);
        Coord dimensions = maxPosition.subtract(minPosition);
        float cellSize = minimumDistance / rootTwo;
        int gridWidth = (int)(dimensions.x / cellSize) + 1;
        int gridHeight = (int)(dimensions.y / cellSize) + 1;
        Coord[][] grid = new Coord[gridWidth][gridHeight];
        ArrayList<Coord> activePoints = new ArrayList<Coord>(),
                    points = new ArrayList<Coord>();

        //add first point
        boolean added = false;
        while (!added)
        {
            float d = rng.nextFloat();
            int xr = Math.round(minPosition.x + dimensions.x * d);

            d = rng.nextFloat();
            int yr = Math.round(minPosition.y + dimensions.y * d);

            if (rejectionDistance > 0 && disk.radius(center.x, center.y, xr, yr) > rejectionDistance)
                continue;
            added = true;
            Coord p = Coord.get(Math.min(xr, maxX), Math.min(yr, maxY));
            Coord index = p.subtract(minPosition).divide(cellSize);

            grid[index.x][index.y] = p;

            activePoints.add(p);
            points.add(p);
        }
        //end add first point

        while (activePoints.size() != 0)
        {
            int listIndex = rng.nextInt(activePoints.size());

            Coord point = activePoints.get(listIndex);
            boolean found = false;

            for (int k = 0; k < pointsPerIteration; k++)
            {
                //add next point
                //get random point around
                float d = rng.nextFloat();
                float radius = minimumDistance + minimumDistance * d;
                d = rng.nextFloat();
                float angle = pi2 * d;

                float newX = radius * (float)Math.sin(angle);
                float newY = radius * (float)Math.cos(angle);
                Coord q = point.translateCapped(Math.round(newX), Math.round(newY), maxX, maxY);
                //end get random point around

                if (q.x >= minPosition.x && q.x < maxPosition.x &&
                        q.y >= minPosition.y && q.y < maxPosition.y &&
                        (rejectionDistance <= 0 || disk.radius(center.x, center.y, q.x, q.y) <= rejectionDistance))
                {
                    Coord qIndex = q.subtract(minPosition).divide(cellSize);
                    boolean tooClose = false;

                    for (int i = Math.max(0, qIndex.x - 2); i < Math.min(gridWidth, qIndex.x + 3) && !tooClose; i++) {
                        for (int j = Math.max(0, qIndex.y - 2); j < Math.min(gridHeight, qIndex.y + 3); j++) {
                            if (grid[i][j] != null && disk.radius(grid[i][j], q) < minimumDistance) {
                                tooClose = true;
                                break;
                            }
                        }
                    }
                    if (!tooClose)
                    {
                        found = true;
                        activePoints.add(q);
                        points.add(q);
                        grid[qIndex.x][qIndex.y] = q;
                    }
                }
                //end add next point
            }

            if (!found)
                activePoints.remove(listIndex);
        }

        return points;
    }
}
