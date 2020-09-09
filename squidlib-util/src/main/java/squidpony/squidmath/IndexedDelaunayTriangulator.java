package squidpony.squidmath;
/******************************************************************************
 Copyright 2011 See AUTHORS file.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/** Delaunay triangulation. Adapted from Paul Bourke's triangulate: http://paulbourke.net/papers/triangulate/
 * @author Nathan Sweet */
public class IndexedDelaunayTriangulator {
    static private final double EPSILON = 0.000001;
    static private final int INSIDE = 0;
    static private final int COMPLETE = 1;
    static private final int INCOMPLETE = 2;

    private final IntVLA quicksortStack = new IntVLA();
    private double[] sortedPoints;
    private final IntVLA triangles = new IntVLA(16);
    private final IntVLA originalIndices = new IntVLA(0);
    private final IntVLA edges = new IntVLA();
    private final ShortVLA complete = new ShortVLA(false, 16); // only a ShortVLA because we don't have BooleanArray
    private final double[] superTriangle = new double[6];
    
    /** @see #computeTriangles(double[], int, int, boolean) */
    public IntVLA computeTriangles (double[] polygon, boolean sorted) {
        return computeTriangles(polygon, 0, polygon.length, sorted);
    }

    /** Triangulates the given point cloud to a list of triangle indices that make up the Delaunay triangulation.
     * @param points x,y pairs describing points. Duplicate points will result in undefined behavior.
     * @param sorted If false, the points will be sorted by the x coordinate, which is required by the triangulation
     *               algorithm. In that case, the input array is not modified, the returned indices are for the input
     *               array, and count*2 additional working memory is needed.
     * @return triples of indices into the points that describe the triangles in clockwise order. Note the returned array is reused
     *         for later calls to the same method. */
    public IntVLA computeTriangles (double[] points, int offset, int count, boolean sorted) {
        IntVLA triangles = this.triangles;
        triangles.clear();
        if (count < 6) return triangles;
        triangles.ensureCapacity(count);

        if (!sorted) {
            if (sortedPoints == null || sortedPoints.length < count) sortedPoints = new double[count];
            System.arraycopy(points, offset, sortedPoints, 0, count);
            points = sortedPoints;
            offset = 0;
            sort(points, count);
        }

        int end = offset + count;

        // Determine bounds for super triangle.
        double xmin = points[0], ymin = points[1];
        double xmax = xmin, ymax = ymin;
        for (int i = offset + 2; i < end; i++) {
            double value = points[i];
            if (value < xmin) xmin = value;
            if (value > xmax) xmax = value;
            i++;
            value = points[i];
            if (value < ymin) ymin = value;
            if (value > ymax) ymax = value;
        }
        double dx = xmax - xmin, dy = ymax - ymin;
        double dmax = (Math.max(dx, dy)) * 20f;
        double xmid = (xmax + xmin) / 2f, ymid = (ymax + ymin) / 2f;

        // Setup the super triangle, which contains all points.
        double[] superTriangle = this.superTriangle;
        superTriangle[0] = xmid - dmax;
        superTriangle[1] = ymid - dmax;
        superTriangle[2] = xmid;
        superTriangle[3] = ymid + dmax;
        superTriangle[4] = xmid + dmax;
        superTriangle[5] = ymid - dmax;

        IntVLA edges = this.edges;
        edges.ensureCapacity(count / 2);

        ShortVLA complete = this.complete;
        complete.clear();
        complete.ensureCapacity(count);

        // Add super triangle.
        triangles.add(end);
        triangles.add(end + 2);
        triangles.add(end + 4);
        complete.add((short) 0);

        // Include each point one at a time into the existing mesh.
        for (int pointIndex = offset; pointIndex < end; pointIndex += 2) {
            double x = points[pointIndex], y = points[pointIndex + 1];

            // If x,y lies inside the circumcircle of a triangle, the edges are stored and the triangle removed.
            int[] trianglesArray = triangles.items;
            short[] completeArray = complete.items;
            for (int triangleIndex = triangles.size - 1; triangleIndex >= 0; triangleIndex -= 3) {
                int completeIndex = triangleIndex / 3;
                if (completeArray[completeIndex] != 0) continue;
                int p1 = trianglesArray[triangleIndex - 2];
                int p2 = trianglesArray[triangleIndex - 1];
                int p3 = trianglesArray[triangleIndex];
                double x1, y1, x2, y2, x3, y3;
                if (p1 >= end) {
                    int i = p1 - end;
                    x1 = superTriangle[i];
                    y1 = superTriangle[i + 1];
                } else {
                    x1 = points[p1];
                    y1 = points[p1 + 1];
                }
                if (p2 >= end) {
                    int i = p2 - end;
                    x2 = superTriangle[i];
                    y2 = superTriangle[i + 1];
                } else {
                    x2 = points[p2];
                    y2 = points[p2 + 1];
                }
                if (p3 >= end) {
                    int i = p3 - end;
                    x3 = superTriangle[i];
                    y3 = superTriangle[i + 1];
                } else {
                    x3 = points[p3];
                    y3 = points[p3 + 1];
                }
                switch (circumCircle(x, y, x1, y1, x2, y2, x3, y3)) {
                    case COMPLETE:
                        completeArray[completeIndex] = 1;
                        break;
                    case INSIDE:
                        edges.add(p1);
                        edges.add(p2);
                        edges.add(p2);
                        edges.add(p3);
                        edges.add(p3);
                        edges.add(p1);

                        trianglesArray[triangleIndex] = trianglesArray[--triangles.size];
                        trianglesArray[triangleIndex - 1] = trianglesArray[--triangles.size];
                        trianglesArray[triangleIndex - 2] = trianglesArray[--triangles.size];
                        completeArray[completeIndex] = completeArray[--complete.size];
                        break;
                }
            }

            int[] edgesArray = edges.items;
            for (int i = 0, n = edges.size; i < n; i += 2) {
                // Skip multiple edges. If all triangles are anticlockwise then all interior edges are opposite pointing in direction.
                int p1 = edgesArray[i];
                if (p1 == -1) continue;
                int p2 = edgesArray[i + 1];
                boolean skip = false;
                for (int ii = i + 2; ii < n; ii += 2) {
                    if (p1 == edgesArray[ii + 1] && p2 == edgesArray[ii]) {
                        skip = true;
                        edgesArray[ii] = -1;
                    }
                }
                if (skip) continue;

                // Form new triangles for the current point. Edges are arranged in clockwise order.
                triangles.add(p1);
                triangles.add(edgesArray[i + 1]);
                triangles.add(pointIndex);
                complete.add((short)0);
            }
            edges.clear();
        }

        // Remove triangles with super triangle vertices.
        int[] trianglesArray = triangles.items;
        for (int i = triangles.size - 1; i >= 0; i -= 3) {
            if (trianglesArray[i] >= end || trianglesArray[i - 1] >= end || trianglesArray[i - 2] >= end) {
                trianglesArray[i] = trianglesArray[--triangles.size];
                trianglesArray[i - 1] = trianglesArray[--triangles.size];
                trianglesArray[i - 2] = trianglesArray[--triangles.size];
            }
        }

        // Convert sorted to unsorted indices.
        if (!sorted) {
            int[] originalIndicesArray = originalIndices.items;
            for (int i = 0, n = triangles.size; i < n; i++)
                trianglesArray[i] = (originalIndicesArray[trianglesArray[i] / 2] * 2);
        }

        // Adjust triangles to start from zero and count by 1, not by vertex x,y coordinate pairs.
        if (offset == 0) {
            for (int i = 0, n = triangles.size; i < n; i++)
                trianglesArray[i] = (trianglesArray[i] / 2);
        } else {
            for (int i = 0, n = triangles.size; i < n; i++)
                trianglesArray[i] = ((trianglesArray[i] - offset) / 2);
        }

        return triangles;
    }

    /** Returns INSIDE if point xp,yp is inside the circumcircle made up of the points x1,y1, x2,y2, x3,y3. Returns COMPLETE if xp
     * is to the right of the entire circumcircle. Otherwise returns INCOMPLETE. Note: a point on the circumcircle edge is
     * considered inside. */
    private int circumCircle (double xp, double yp, double x1, double y1, double x2, double y2, double x3, double y3) {
        double xc, yc;
        double y1y2 = Math.abs(y1 - y2);
        double y2y3 = Math.abs(y2 - y3);
        if (y1y2 < EPSILON) {
            if (y2y3 < EPSILON) return INCOMPLETE;
            double m2 = -(x3 - x2) / (y3 - y2);
            double mx2 = (x2 + x3) / 2f;
            double my2 = (y2 + y3) / 2f;
            xc = (x2 + x1) / 2f;
            yc = m2 * (xc - mx2) + my2;
        } else {
            double m1 = -(x2 - x1) / (y2 - y1);
            double mx1 = (x1 + x2) / 2f;
            double my1 = (y1 + y2) / 2f;
            if (y2y3 < EPSILON) {
                xc = (x3 + x2) / 2f;
                yc = m1 * (xc - mx1) + my1;
            } else {
                double m2 = -(x3 - x2) / (y3 - y2);
                double mx2 = (x2 + x3) / 2f;
                double my2 = (y2 + y3) / 2f;
                xc = (m1 * mx1 - m2 * mx2 + my2 - my1) / (m1 - m2);
                yc = m1 * (xc - mx1) + my1;
            }
        }

        double dx = x2 - xc;
        double dy = y2 - yc;
        double rsqr = dx * dx + dy * dy;

        dx = xp - xc;
        dx *= dx;
        dy = yp - yc;
        if (dx + dy * dy - rsqr <= EPSILON) return INSIDE;
        return xp > xc && dx > rsqr ? COMPLETE : INCOMPLETE;
    }

//    public void sortPairs(double[] points)
//    {
//        final int pointCount = points.length / 2;
//        if (sortedPoints == null || sortedPoints.length < points.length) sortedPoints = new double[points.length];
//        System.arraycopy(points, 0, sortedPoints, 0, points.length);
////        points = sortedPoints;
//        sort(sortedPoints, sortedPoints.length);
//        int[] originalIndicesArray = originalIndices.items;
//        int p;
//        for (int i = 0; i < pointCount; i++) {
//            p = originalIndicesArray[i];
//            points[p<<1] = sortedPoints[i<<1];
//            points[p<<1|1] = sortedPoints[i<<1|1];
//        }
//        System.out.println(sortedPoints.length);
//    }
    
    /** Sorts x,y pairs of values by the x value.
     * @param count Number of indices, must be even. */
    private void sort (double[] values, int count) {
        int pointCount = count / 2;
        originalIndices.clear();
        originalIndices.ensureCapacity(pointCount);
        int[] originalIndicesArray = originalIndices.items;
        for (int i = 0; i < pointCount; i++)
            originalIndicesArray[i] = i;

        int lower = 0;
        int upper = count - 1;
        IntVLA stack = quicksortStack;
        stack.add(lower);
        stack.add(upper - 1);
        while (stack.size > 0) {
            upper = stack.pop();
            lower = stack.pop();
            if (upper <= lower) continue;
            int i = quicksortPartition(values, lower, upper, originalIndicesArray);
            if (i - lower > upper - i) {
                stack.add(lower);
                stack.add(i - 2);
            }
            stack.add(i + 2);
            stack.add(upper);
            if (upper - i >= i - lower) {
                stack.add(lower);
                stack.add(i - 2);
            }
        }
    }

    private int quicksortPartition (final double[] values, int lower, int upper, int[] originalIndices) {
        double value = values[lower];
        int up = upper;
        int down = lower + 2;
        double tempValue;
        int tempIndex;
        while (down < up) {
            while (down < up && values[down] <= value)
                down = down + 2;
            while (values[up] > value)
                up = up - 2;
            if (down < up) {
                tempValue = values[down];
                values[down] = values[up];
                values[up] = tempValue;

                tempValue = values[down + 1];
                values[down + 1] = values[up + 1];
                values[up + 1] = tempValue;

                tempIndex = originalIndices[down / 2];
                originalIndices[down / 2] = originalIndices[up / 2];
                originalIndices[up / 2] = tempIndex;
            }
        }
        if(value > values[up]) {
            values[lower] = values[up];
            values[up] = value;

            tempValue = values[lower + 1];
            values[lower + 1] = values[up + 1];
            values[up + 1] = tempValue;

            tempIndex = originalIndices[lower / 2];
            originalIndices[lower / 2] = originalIndices[up / 2];
            originalIndices[up / 2] = tempIndex;
        }
        return up;
    }

    /** Removes all triangles with a centroid outside the specified hull, which may be concave. Note some triangulations may have
     * triangles whose centroid is inside the hull but a portion is outside. */
    public void trim (IntVLA triangles, double[] points, double[] hull, int offset, int count) {
        int[] trianglesArray = triangles.items;
        for (int i = triangles.size - 1; i >= 0; i -= 3) {
            int p1 = trianglesArray[i - 2] * 2;
            int p2 = trianglesArray[i - 1] * 2;
            int p3 = trianglesArray[i] * 2;
            double centroidX = (points[p1] + points[p2] + points[p3]) / 3.0;
            double centroidY = (points[p1+1] + points[p2+1] + points[p3+1]) / 3.0;
            if (!isPointInPolygon(hull, offset, count, centroidX, centroidY)) {
                trianglesArray[i] = trianglesArray[--triangles.size];
                trianglesArray[i - 1] = trianglesArray[--triangles.size];
                trianglesArray[i - 2] = trianglesArray[--triangles.size];
            }
        }
    }
    /** Returns true if the specified point is in the polygon.
     * @param offset Starting polygon index.
     * @param count Number of array indices to use after offset. */
    public static boolean isPointInPolygon (double[] polygon, int offset, int count, double x, double y) {
        boolean oddNodes = false;
        int j = offset + count - 2;
        for (int i = offset, n = j; i <= n; i += 2) {
            double yi = polygon[i + 1];
            double yj = polygon[j + 1];
            if ((yi < y && yj >= y) || (yj < y && yi >= y)) {
                double xi = polygon[i];
                if (xi + (y - yi) / (yj - yi) * (polygon[j] - xi) < x) oddNodes = !oddNodes;
            }
            j = i;
        }
        return oddNodes;
    }
}

