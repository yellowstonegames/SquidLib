package squidpony.squidutility.graph;

/**
 *
 * @author http://www.vogella.com/articles/JavaAlgorithmsDijkstra/article.html
 */
public class Edge {

    private final Vertex source;
    private final Vertex destination;
    private final double weight;

    public Edge(Vertex source, Vertex destination, double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public Vertex getDestination() {
        return destination;
    }

    public Vertex getSource() {
        return source;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return source + " " + destination;
    }
}
