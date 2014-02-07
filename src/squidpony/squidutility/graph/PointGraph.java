package squidpony.squidutility.graph;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import squidpony.annotation.Beta;
import squidpony.squidgrid.fov.FOVTranslator;
import squidpony.squidgrid.fov.ShadowFOV;

/**
 *
 * @author http://www.vogella.com/articles/JavaAlgorithmsDijkstra/article.html
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class PointGraph implements Graph {

    private ArrayList<Vertex> vertexes = new ArrayList<>();
    private ArrayList<Edge> edges = new ArrayList<>();

    @Override
    public void addVertex(Vertex v) {
        vertexes.add(v);
    }

    @Override
    public void addEdge(Vertex v1, Vertex v2, int weight) {
        edges.add(new Edge(v1, v2, weight));
    }

    @Override
    public List<Vertex> getVertexes() {
        return vertexes;
    }

    @Override
    public List<Edge> getEdges() {
        return edges;
    }

    public void calculateEdges() {
        for (Vertex v1 : vertexes) {
            for (Vertex v2 : vertexes) {
                edges.add(new Edge(v1, v2, Point.distance(v1.point.x, v1.point.y, v2.point.x, v2.point.y)));
            }
        }
    }

    public void calculateEdges(float[][] blocking) {
        FOVTranslator fov = new FOVTranslator(new ShadowFOV());
        for (Vertex v1 : vertexes) {
            for (Vertex v2 : vertexes) {
                if (v1 != v2) {
                    fov.calculateFOV(blocking, v1.point.x, v1.point.y, 100);
                    if (fov.isLit(v2.point.x, v2.point.y)) {
                        edges.add(new Edge(v1, v2, Point.distance(v1.point.x, v1.point.y, v2.point.x, v2.point.y)));
                    }
                }
            }
        }
    }

    public LinkedList<Vertex> getDijkstraPath(Point startp, Point endp) {
        Vertex startv = findVertex(startp);
        Vertex endv = findVertex(endp);
        return getDijkstraPath(startv, endv);
    }

    @Override
    public LinkedList<Vertex> getDijkstraPath(Vertex v1, Vertex v2) {
        DijkstraAlgorithm algo = new DijkstraAlgorithm(this);
        algo.execute(v1);
        return algo.getPath(v2);
    }

    public Vertex findVertex(Point p) {
        for (Vertex v : vertexes) {
            if (v.point.equals(p)) {
                return v;
            }
        }
        return null;//no vertex matched
    }
}
