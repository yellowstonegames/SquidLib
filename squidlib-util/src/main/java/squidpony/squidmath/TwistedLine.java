package squidpony.squidmath;

import squidpony.squidai.graph.DefaultGraph;
import squidpony.squidai.graph.Heuristic;
import squidpony.squidgrid.Direction;

import java.util.ArrayList;

/**
 * Like {@link WobblyLine}, this generates orthogonally-connected paths of {@link Coord} that meander through an area;
 * unlike WobblyLine, this won't ever generate paths that cross themselves.
 * <br>
 * This uses a similar algorithm to {@link squidpony.squidgrid.mapping.GrowingTreeMazeGenerator} to generate a
 * fully-connected graph for a given rectangular area, then solves it with
 * {@link DefaultGraph#findShortestPath(Coord, Coord, ArrayList, Heuristic)}.
 * <br>
 * Created by Tommy Ettinger on 6/26/2020.
 */
public class TwistedLine {
    public IRNG rng;
    public final DefaultGraph graph;
    public final ArrayList<Coord> lastPath;

    public TwistedLine() {
        this(40, 40, null);
    }

    public TwistedLine(int width, int height) {
        this(width, height, null);
    }

    public TwistedLine(int width, int height, IRNG rng) {
        graph = new DefaultGraph();
        graph.width = Math.max(width, 2);
        graph.height = Math.max(height, 2);
        this.rng = rng == null ? new RNG() : rng;
        lastPath = new ArrayList<>(graph.width + graph.height);
        reinitialize();
    }

    /**
     * Called automatically during construction, this sets up a random maze as a {@link DefaultGraph} so a path can be
     * found. You can call this after construction to change the paths this can find.
     */
    public void reinitialize() {
        graph.removeAllVertices();
        for (int x = 0; x < graph.width; x++) {
            for (int y = 0; y < graph.height; y++) {
                graph.addVertex(Coord.get(x, y));
            }
        }

        int x = rng.nextSignedInt(graph.width);
        int y = rng.nextSignedInt(graph.height);

        OrderedSet<Coord> deck = new OrderedSet<>();
        deck.add(Coord.get(x, y));

        Direction[] dirs = new Direction[4];
        System.arraycopy(Direction.CARDINALS, 0, dirs, 0, 4);
        OUTER:
        while (!deck.isEmpty()) {
            int i = deck.size() - 1;
            Coord p = deck.getAt(i);
            rng.shuffleInPlace(dirs);

            for (Direction dir : dirs) {
                x = p.x + dir.deltaX;
                y = p.y + dir.deltaY;
                if (x >= 0 && x < graph.width && y >= 0 && y < graph.height) {
                    Coord c = Coord.get(x, y);
                    if (graph.getEdges(c).isEmpty() && deck.add(c)) {
                        graph.addEdge(p, c);
                        continue OUTER;
                    }
                }
            }

            deck.remove(p);
        }

    }

    public ArrayList<Coord> line(int startX, int startY, int endX, int endY) {
        return line(Coord.get(startX, startY), Coord.get(endX, endY));
    }

    public ArrayList<Coord> line(Coord start, Coord end) {
        graph.findShortestPath(start, end, lastPath, Heuristic.EUCLIDEAN);
        return lastPath;
    }

    public int getWidth() {
        return graph.width;
    }

    public int getHeight() {
        return graph.height;
    }

    public IRNG getRng() {
        return rng;
    }

    public void setRng(IRNG rng) {
        this.rng = rng;
    }

    public ArrayList<Coord> getLastPath() {
        return lastPath;
    }
}
