//The MIT License (MIT)
//
//Copyright (c) 2015 Johannes Diemke
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
//copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//SOFTWARE.

package squidpony.squidmath;

import squidpony.annotation.Beta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ListIterator;

/**
 * A Java implementation of an incremental 2D Delaunay triangulation algorithm.
 * This is a port of <a href="https://github.com/jdiemke/delaunay-triangulator">Johannes Diemke's code</a>, with
 * some substantial non-algorithmic changes to better work in SquidLib and to reduce allocations.
 * @author Johannes Diemke
 * @author Tommy Ettinger
 */
@Beta
public class DelaunayTriangulator implements Serializable {
    private static final long serialVersionUID = 1L;

    private OrderedSet<CoordDouble> pointSet;
    private TriangleSoup triangleSoup;

    /**
     * Constructs a triangulator instance but does not insert any points; you should add points to
     * {@link #getPointSet()} before running {@link #triangulate()}.
     */
    public DelaunayTriangulator() {
        this.pointSet = new OrderedSet<>(256);
        this.triangleSoup = new TriangleSoup();
    }

    /**
     * Constructs a new triangulator instance using the specified point set.
     *
     * @param pointSet The point set to be triangulated
     */
    public DelaunayTriangulator(OrderedSet<CoordDouble> pointSet) {
        this.pointSet = pointSet;
        this.triangleSoup = new TriangleSoup();
    }

    /**
     * This method generates a Delaunay triangulation from the specified point
     * set.
     */
    public ArrayList<Triangle> triangulate() {
        int numPoints;
        if (pointSet == null || (numPoints = pointSet.size()) < 3) {
            throw new IllegalArgumentException("Less than three points in point set.");
        }
        triangleSoup = new TriangleSoup();

        /*
         * In order for the in circumcircle test to not consider the vertices of
         * the super triangle we have to start out with a big triangle
         * containing the whole point set. We have to scale the super triangle
         * to be very large. Otherwise the triangulation is not convex.
         */
        double maxOfAnyCoordinate = 0.0;

        for (int i = 0; i < numPoints; i++) {
            final CoordDouble pt = pointSet.getAt(i);
            maxOfAnyCoordinate = Math.max(Math.max(pt.x, pt.y), maxOfAnyCoordinate);
        }

        maxOfAnyCoordinate *= 48.0;

        CoordDouble p1 = new CoordDouble(0.0, maxOfAnyCoordinate);
        CoordDouble p2 = new CoordDouble(maxOfAnyCoordinate, 0.0);
        CoordDouble p3 = new CoordDouble(-maxOfAnyCoordinate, -maxOfAnyCoordinate);

        Triangle superTriangle = new Triangle(p1, p2, p3);

        triangleSoup.add(superTriangle);

        for (int i = 0; i < pointSet.size(); i++) {
            Triangle triangle = triangleSoup.findContainingTriangle(pointSet.getAt(i));

            if (triangle == null) {
                /**
                 * If no containing triangle exists, then the vertex is not
                 * inside a triangle (this can also happen due to numerical
                 * errors) and lies on an edge. In order to find this edge we
                 * search all edges of the triangle soup and select the one
                 * which is nearest to the point we try to add. This edge is
                 * removed and four new edges are added.
                 */
                Edge edge = triangleSoup.findNearestEdge(pointSet.getAt(i));

                Triangle first = triangleSoup.findOneTriangleSharing(edge);
                Triangle second = triangleSoup.findNeighbor(first, edge);

                CoordDouble firstNonEdgeVertex = first.getNonEdgeVertex(edge);
                CoordDouble secondNonEdgeVertex = second.getNonEdgeVertex(edge);
                CoordDouble p = pointSet.getAt(i);
                
                triangleSoup.remove(first);
                triangleSoup.remove(second);

                Triangle triangle1 = new Triangle(edge.a, firstNonEdgeVertex,  p);
                Triangle triangle2 = new Triangle(edge.b, firstNonEdgeVertex,  p);
                Triangle triangle3 = new Triangle(edge.a, secondNonEdgeVertex, p);
                Triangle triangle4 = new Triangle(edge.b, secondNonEdgeVertex, p);

                triangleSoup.add(triangle1);
                triangleSoup.add(triangle2);
                triangleSoup.add(triangle3);
                triangleSoup.add(triangle4);

                legalizeEdge(triangle1, edge.a, firstNonEdgeVertex,  p);
                legalizeEdge(triangle2, edge.b, firstNonEdgeVertex,  p);
                legalizeEdge(triangle3, edge.a, secondNonEdgeVertex, p);
                legalizeEdge(triangle4, edge.b, secondNonEdgeVertex, p);
            } else {
                /*
                 * The vertex is inside a triangle.
                 */
                CoordDouble a = triangle.a;
                CoordDouble b = triangle.b;
                CoordDouble c = triangle.c;
                CoordDouble p = pointSet.getAt(i);
                
                triangleSoup.remove(triangle);

                Triangle first = new Triangle(a, b, p);
                Triangle second = new Triangle(b, c, p);
                Triangle third = new Triangle(c, a, p);

                triangleSoup.add(first);
                triangleSoup.add(second);
                triangleSoup.add(third);

                legalizeEdge(first,  a, b, p);
                legalizeEdge(second, b, c, p);
                legalizeEdge(third,  c, a, p);
            }
        }

        /*
         * Remove all triangles that contain vertices of the super triangle.
         */
        triangleSoup.removeTrianglesUsing(superTriangle.a);
        triangleSoup.removeTrianglesUsing(superTriangle.b);
        triangleSoup.removeTrianglesUsing(superTriangle.c);
        return triangleSoup.getTriangles();
    }

    /**
     * This method legalizes edges by recursively flipping all illegal edges.
     * 
     * @param triangle
     *            The triangle
     * @param ea
     *            The "a" point on the edge to be legalized
     * @param eb
     *            The "b" point on the edge to be legalized
     * @param newVertex
     *            The new vertex
     */
    private void legalizeEdge(Triangle triangle, CoordDouble ea, CoordDouble eb, CoordDouble newVertex) {
        Triangle neighborTriangle = triangleSoup.findNeighbor(triangle, ea, eb);

        /**
         * If the triangle has a neighbor, then legalize the edge
         */
        if (neighborTriangle != null) {
            if (neighborTriangle.isPointInCircumcircle(newVertex)) {
                triangleSoup.remove(triangle);
                triangleSoup.remove(neighborTriangle);

                CoordDouble noneEdgeVertex = neighborTriangle.getNonEdgeVertex(ea, eb);

                Triangle firstTriangle = new Triangle(noneEdgeVertex, ea, newVertex);
                Triangle secondTriangle = new Triangle(noneEdgeVertex, eb, newVertex);

                triangleSoup.add(firstTriangle);
                triangleSoup.add(secondTriangle);

                legalizeEdge(firstTriangle, noneEdgeVertex, ea, newVertex);
                legalizeEdge(secondTriangle, noneEdgeVertex, eb, newVertex);
            }
        }
    }

    /**
     * Creates a random permutation of the specified point set. Based on the
     * implementation of the Delaunay algorithm this can speed up the
     * computation.
     */
    public void shuffle(IRNG rng) {
        pointSet.shuffle(rng);
    }

    /**
     * Shuffles the point set using a custom permutation sequence.
     * 
     * @param permutation
     *            The permutation used to shuffle the point set
     */
    public void reorder(int[] permutation) {
        pointSet.reorder(permutation);
    }

    /**
     * Returns the point set in form of a vector of 2D vectors.
     * 
     * @return Returns the points set.
     */
    public OrderedSet<CoordDouble> getPointSet() {
        return pointSet;
    }

    /**
     * Returns the triangles of the triangulation in form of a list of 2D
     * triangles.
     * 
     * @return Returns the triangles of the triangulation.
     */
    public ArrayList<Triangle> getTriangles() {
        return triangleSoup.getTriangles();
    }
    
    public static class Edge implements Serializable
    {
        private static final long serialVersionUID = 1L;
        
        public CoordDouble a;
        public CoordDouble b;
        
        public Edge()
        {
            a = new CoordDouble(0.0, 0.0);
            b = new CoordDouble(0.0, 1.0);
        }
        public Edge(CoordDouble a, CoordDouble b)
        {
            this.a = a;
            this.b = b;
        }
        public Edge(double ax, double ay, double bx, double by)
        {
            a = new CoordDouble(ax, ay);
            b = new CoordDouble(bx, by);
        }
    }
    
    public static class Triangle implements Serializable
    {
        private static final long serialVersionUID = 1L;

        public CoordDouble a;
        public CoordDouble b;
        public CoordDouble c;

        /**
         * Constructor of the 2D triangle class used to create a new triangle
         * instance from three 2D vectors describing the triangle's vertices.
         *
         * @param a
         *            The first vertex of the triangle
         * @param b
         *            The second vertex of the triangle
         * @param c
         *            The third vertex of the triangle
         */
        public Triangle(CoordDouble a, CoordDouble b, CoordDouble c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        /**
         * Tests if a 2D point lies inside this 2D triangle. See Real-Time Collision
         * Detection, chap. 5, p. 206.
         *
         * @param point
         *            The point to be tested
         * @return Returns true iff the point lies inside this 2D triangle
         */
        public boolean contains(CoordDouble point) {
            double px = point.x - a.x;
            double py = point.y - a.y;
            double sx = b.x - a.x;
            double sy = b.y - a.y;
            double pab = py * sx - px * sy;//point.sub(a).cross(b.sub(a));
            px = point.x - b.x;
            py = point.y - b.y;
            sx = c.x - b.x;
            sy = c.y - b.y;
            double pbc = py * sx - px * sy;//point.sub(b).cross(c.sub(b));

            if (!hasSameSign(pab, pbc)) {
                return false;
            }

            px = point.x - c.x;
            py = point.y - c.y;
            sx = a.x - c.x;
            sy = a.y - c.y;
            double pca = py * sx - px * sy;//point.sub(c).cross(a.sub(c));

            return hasSameSign(pab, pca);

        }

        /**
         * Tests if a given point lies in the circumcircle of this triangle. Let the
         * triangle ABC appear in counterclockwise (CCW) order. Then when det &gt;
         * 0, the point lies inside the circumcircle through the three points a, b
         * and c. If instead det &lt; 0, the point lies outside the circumcircle.
         * When det = 0, the four points are cocircular. If the triangle is oriented
         * clockwise (CW) the result is reversed. See Real-Time Collision Detection,
         * chap. 3, p. 34.
         *
         * @param point
         *            The point to be tested
         * @return Returns true iff the point lies inside the circumcircle through
         *         the three points a, b, and c of the triangle
         */
        public boolean isPointInCircumcircle(CoordDouble point) {
            double a11 = a.x - point.x;
            double a21 = b.x - point.x;
            double a31 = c.x - point.x;

            double a12 = a.y - point.y;
            double a22 = b.y - point.y;
            double a32 = c.y - point.y;

            double a13 = (a.x - point.x) * (a.x - point.x) + (a.y - point.y) * (a.y - point.y);
            double a23 = (b.x - point.x) * (b.x - point.x) + (b.y - point.y) * (b.y - point.y);
            double a33 = (c.x - point.x) * (c.x - point.x) + (c.y - point.y) * (c.y - point.y);

            double det = a11 * a22 * a33 + a12 * a23 * a31 + a13 * a21 * a32 - a13 * a22 * a31 - a12 * a21 * a33
                    - a11 * a23 * a32;

            if (isOrientedCCW()) {
                return det > 0.0;
            }

            return det < 0.0;
        }

        /**
         * Test if this triangle is oriented counterclockwise (CCW). Let A, B and C
         * be three 2D points. If det &gt; 0, C lies to the left of the directed
         * line AB. Equivalently the triangle ABC is oriented counterclockwise. When
         * det &lt; 0, C lies to the right of the directed line AB, and the triangle
         * ABC is oriented clockwise. When det = 0, the three points are colinear.
         * See Real-Time Collision Detection, chap. 3, p. 32
         *
         * @return Returns true iff the triangle ABC is oriented counterclockwise
         *         (CCW)
         */
        public boolean isOrientedCCW() {
            double a11 = a.x - c.x;
            double a21 = b.x - c.x;

            double a12 = a.y - c.y;
            double a22 = b.y - c.y;

            double det = a11 * a22 - a12 * a21;

            return det > 0.0;
        }

        /**
         * Returns true if this triangle contains the given edge.
         *
         * @param edge
         *            The edge to be tested
         * @return Returns true if this triangle contains the edge
         */
        public boolean isNeighbor(Edge edge) {
            return (a == edge.a || b == edge.a || c == edge.a) && (a == edge.b || b == edge.b || c == edge.b);
        }
        public boolean isNeighbor(CoordDouble ea, CoordDouble eb) {
            return (a == ea || b == ea || c == ea) && (a == eb || b == eb || c == eb);
        }

        /**
         * Returns the vertex of this triangle that is not part of the given edge.
         *
         * @param edge
         *            The edge
         * @return The vertex of this triangle that is not part of the edge
         */
        public CoordDouble getNonEdgeVertex(Edge edge) {
            if (a != edge.a && a != edge.b) {
                return a;
            } else if (b != edge.a && b != edge.b) {
                return b;
            } else if (c != edge.a && c != edge.b) {
                return c;
            }

            return null;
        }
        public CoordDouble getNonEdgeVertex(CoordDouble ea, CoordDouble eb) {
            if (a != ea && a != eb) {
                return a;
            } else if (b != ea && b != eb) {
                return b;
            } else if (c != ea && c != eb) {
                return c;
            }

            return null;
        }

        /**
         * Returns true if the given vertex is one of the vertices describing this
         * triangle.
         *
         * @param vertex
         *            The vertex to be tested
         * @return Returns true if the Vertex is one of the vertices describing this
         *         triangle
         */
        public boolean hasVertex(CoordDouble vertex) {
            if (a == vertex || b == vertex || c == vertex) {
                return true;
            }

            return false;
        }

        /**
         * Computes the closest point on the given edge to the specified point.
         *
         * @param edge
         *            The edge on which we search the closest point to the specified
         *            point
         * @param point
         *            The point to which we search the closest point on the edge
         * @return The closest point on the given edge to the specified point
         */
        CoordDouble computeClosestPoint(Edge edge, CoordDouble point) {
            //CoordDouble ab = edge.b.sub(edge.a);
            double abx = edge.b.x = edge.a.x;
            double aby = edge.b.y = edge.a.y;
            double t = ((point.x - edge.a.x) * abx + (point.y - edge.a.y) * aby) / (abx * abx + aby * aby);
            //double t = point.sub(edge.a).dot(ab) / ab.dot(ab);

            if (t < 0.0d) {
                t = 0.0d;
            } else if (t > 1.0d) {
                t = 1.0d;
            }
            return new CoordDouble(edge.a.x + abx * t, edge.a.y + aby * t);
//            return edge.a.add(ab.mult(t));
        }

        /**
         * Tests if the two arguments have the same sign.
         *
         * @param a
         *            The first floating point argument
         * @param b
         *            The second floating point argument
         * @return Returns true iff both arguments have the same sign
         */
        private boolean hasSameSign(double a, double b) {
            return (a == 0.0 && b == 0.0) || ((a > 0.0) == (b > 0.0));
        }

        @Override
        public String toString() {
            return "Triangle[" + a + ", " + b + ", " + c + "]";
        }
    }
    private static final Comparator<Double> doubleComparator = new Comparator<Double>() {
        @Override
        public int compare(Double o1, Double o2) {
            return o1.compareTo(o2);
        }
    };

    /**
     * Triangle soup class implementation.
     *
     * @author Johannes Diemke
     */
    static class TriangleSoup {

        private ArrayList<Triangle> triangleSoup;

        /**
         * Constructor of the triangle soup class used to create a new triangle soup
         * instance.
         */
        public TriangleSoup() {
            this.triangleSoup = new ArrayList<Triangle>();
        }

        /**
         * Adds a triangle to this triangle soup.
         *
         * @param triangle
         *            The triangle to be added to this triangle soup
         */
        public void add(Triangle triangle) {
            this.triangleSoup.add(triangle);
        }

        /**
         * Removes a triangle from this triangle soup.
         *
         * @param triangle
         *            The triangle to be removed from this triangle soup
         */
        public void remove(Triangle triangle) {
            this.triangleSoup.remove(triangle);
        }

        /**
         * Returns the triangles from this triangle soup.
         *
         * @return The triangles from this triangle soup
         */
        public ArrayList<Triangle> getTriangles() {
            return this.triangleSoup;
        }

        /**
         * Returns the triangle from this triangle soup that contains the specified
         * point or null if no triangle from the triangle soup contains the point.
         *
         * @param point
         *            The point
         * @return Returns the triangle from this triangle soup that contains the
         *         specified point or null
         */
        public Triangle findContainingTriangle(CoordDouble point) {
            for (Triangle triangle : triangleSoup) {
                if (triangle.contains(point)) {
                    return triangle;
                }
            }
            return null;
        }

        /**
         * Returns the neighbor triangle of the specified triangle sharing the same
         * edge as specified. If no neighbor sharing the same edge exists null is
         * returned.
         *
         * @param triangle
         *            The triangle
         * @param edge
         *            The edge
         * @return The triangles neighbor triangle sharing the same edge or null if
         *         no triangle exists
         */
        public Triangle findNeighbor(Triangle triangle, Edge edge) {
            for (Triangle triangleFromSoup : triangleSoup) {
                if (triangleFromSoup.isNeighbor(edge) && triangleFromSoup != triangle) {
                    return triangleFromSoup;
                }
            }
            return null;
        }
        public Triangle findNeighbor(Triangle triangle, CoordDouble ea, CoordDouble eb) {
            for (Triangle triangleFromSoup : triangleSoup) {
                if (triangleFromSoup.isNeighbor(ea, eb) && triangleFromSoup != triangle) {
                    return triangleFromSoup;
                }
            }
            return null;
        }

        /**
         * Returns one of the possible triangles sharing the specified edge. Based
         * on the ordering of the triangles in this triangle soup the returned
         * triangle may differ. To find the other triangle that shares this edge use
         * the {@link #findNeighbor(Triangle, Edge)} method.
         *
         * @param edge
         *            The edge
         * @return Returns one triangle that shares the specified edge
         */
        public Triangle findOneTriangleSharing(Edge edge) {
            for (Triangle triangle : triangleSoup) {
                if (triangle.isNeighbor(edge)) {
                    return triangle;
                }
            }
            return null;
        }

        /**
         * Returns the edge from the triangle soup nearest to the specified point.
         *
         * @param point
         *            The point
         * @return The edge from the triangle soup nearest to the specified point
         */
        public Edge findNearestEdge(CoordDouble point) {
//            List<EdgeDistancePack> edgeList = new ArrayList<EdgeDistancePack>();

            OrderedMap<Edge, Double> edges = new OrderedMap<>(triangleSoup.size());
            for (Triangle triangle : triangleSoup) {
                Edge ab = new Edge(triangle.a, triangle.b);
                Edge bc = new Edge(triangle.b, triangle.c);
                Edge ca = new Edge(triangle.c, triangle.a);
                double abd = triangle.computeClosestPoint(ab, point).subtract(point).lengthSq();
                double bcd = triangle.computeClosestPoint(bc, point).subtract(point).lengthSq();
                double cad = triangle.computeClosestPoint(ca, point).subtract(point).lengthSq();
                
                if(abd <= bcd && abd <= cad)
                    edges.put(ab, abd);
                else if(bcd <= abd && bcd <= cad)
                    edges.put(bc, bcd);
                else 
                    edges.put(ca, cad);
            }
            edges.sortByValue(doubleComparator);
            return edges.keyAt(0);
        }

        /**
         * Removes all triangles from this triangle soup that contain the specified
         * vertex.
         *
         * @param vertex
         *            The vertex
         */
        public void removeTrianglesUsing(CoordDouble vertex) {
            ListIterator<Triangle> li = triangleSoup.listIterator();
            while (li.hasNext())
            {
                Triangle triangle = li.next();
                if(triangle.hasVertex(vertex))
                    li.remove();
            }
        }

    }
}