package squidpony.squidutility.graph;

import java.awt.Point;
import java.util.Objects;

/**
 *
 * @author http://www.vogella.com/articles/JavaAlgorithmsDijkstra/article.html
 */
public class Vertex {

    final public Point point;

    public Vertex(Point point) {
        this.point = point;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Vertex ? point.equals(((Vertex) obj).point) : false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.point);
        return hash;
    }

    @Override
    public String toString() {
        return point.toString();
    }
}
