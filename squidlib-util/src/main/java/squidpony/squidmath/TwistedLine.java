package squidpony.squidmath;

import squidpony.squidai.astar.*;
import squidpony.squidgrid.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Like {@link WobblyLine}, this generates orthogonally-connected paths of {@link Coord} that meander through an area;
 * unlike WobblyLine, this won't ever generate paths that cross themselves.
 * <br>
 * This uses a similar algorithm to {@link squidpony.squidgrid.mapping.GrowingTreeMazeGenerator} to generate a
 * fully-connected graph for a given rectangular area, then solves it with {@link squidpony.squidai.astar.Pathfinder}.
 * <br>
 * Created by Tommy Ettinger on 6/26/2020.
 */
public class TwistedLine implements Graph<Coord> {
    public final int width, height;
    protected final ArrayList<List<Connection<Coord>>> allConnections;
    public IRNG rng;
    public final Pathfinder<Coord> pathfinder;
    public final ArrayList<Coord> lastPath;

    public TwistedLine() {
        this(40, 40, null);
    }

    public TwistedLine(int width, int height) {
        this(width, height, null);
    }

    public TwistedLine(int width, int height, IRNG rng) {
        this.width = Math.max(width, 2);
        this.height = Math.max(height, 2);
        this.rng = rng == null ? new RNG() : rng;
        lastPath = new ArrayList<>(this.width + this.height);
        allConnections = new ArrayList<>(this.width * this.height);

        reinitialize();
        pathfinder = new Pathfinder<>(this);
    }

    /**
     * Called automatically during construction, this sets up a random maze as a {@link Graph} so a path can be found.
     * You can call this after construction to change the paths this can find.
     */
    public void reinitialize() {
        if (allConnections.size() == width * height) {
            for (int i = width * height - 1; i >= 0; i--) {
                allConnections.get(i).clear();
            }
        } else {
            allConnections.clear();
            for (int i = width * height; i > 0; i--) {
                allConnections.add(new ArrayList<Connection<Coord>>(4));
            }
        }

        int x = rng.nextSignedInt(width);
        int y = rng.nextSignedInt(height);

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
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    if (allConnections.get(x + y * this.width).isEmpty() && deck.add(Coord.get(x, y))) {
                        allConnections.get(p.x + p.y * this.width).add(new DefaultConnection<>(p, Coord.get(x, y)));
                        allConnections.get(x + y * this.width).add(new DefaultConnection<>(Coord.get(x, y), p));
                        continue OUTER;
                    }
                }
            }

            deck.remove(p);
        }

    }

    /**
     * Returns the connections outgoing from the given node; probably irrelevant except internally.
     *
     * @param fromNode the node whose outgoing connections will be returned
     * @return the array of connections outgoing from the given node.
     */
    @Override
    public List<Connection<Coord>> getConnections(Coord fromNode) {
        return allConnections.get(fromNode.x + fromNode.y * width);
    }

    /**
     * Returns the unique index of the given node; probably irrelevant except internally.
     *
     * @param node the node whose index will be returned
     * @return the unique index of the given node.
     */
    @Override
    public int getIndex(Coord node) {
        return node.x + node.y * width;
    }

    /**
     * Returns the number of nodes in this graph; probably irrelevant except internally.
     */
    @Override
    public int getNodeCount() {
        return width * height;
    }

    public ArrayList<Coord> line(int startX, int startY, int endX, int endY) {
        return line(Coord.get(startX, startY), Coord.get(endX, endY));
    }

    public ArrayList<Coord> line(Coord start, Coord end) {
        pathfinder.searchNodePath(start, end, DefaultGraph.EUCLIDEAN, lastPath);
        return lastPath;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
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
