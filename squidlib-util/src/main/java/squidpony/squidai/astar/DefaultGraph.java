package squidpony.squidai.astar;

import squidpony.Maker;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Arrangement;
import squidpony.squidmath.Coord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultGraph implements Graph<Coord> {

    /**
     * A predefined Heuristic for Coord nodes in a 2D plane where diagonal movement is estimated as costing twice as
     * much as orthogonal movement. This is a good choice for graphs where only four-way movement is used.
     */
    public static final Heuristic<Coord> MANHATTAN = new Heuristic<Coord>() {
        @Override
        public float estimate(Coord node, Coord endNode) {
            return Math.abs(node.x - endNode.x) + Math.abs(node.y - endNode.y);
        }
    };
    /**
     * A predefined Heuristic for Coord nodes in a 2D plane where diagonal movement is estimated as costing the same as
     * orthogonal movement. This is only suggested for graphs where eight-way movement is used, and it may produce
     * erratic paths compared to {@link #EUCLIDEAN}.
     */
    public static final Heuristic<Coord> CHEBYSHEV = new Heuristic<Coord>() {
        @Override
        public float estimate(Coord node, Coord endNode) {
            return Math.max(Math.abs(node.x - endNode.x), Math.abs(node.y - endNode.y));
        }
    };
    /**
     * A predefined Heuristic for Coord nodes in a 2D plane where all movement is calculated "as-the-crow-flies," using
     * the standard Pythagorean formula for distance as in the real world. This does not make diagonal connections, if
     * they are allowed, actually cost more or less, but they won't be preferred if an orthogonal route can be taken.
     * This is recommended for graphs where eight-way movement is used.
     */
    public static final Heuristic<Coord> EUCLIDEAN = new Heuristic<Coord>() {
        @Override
        public float estimate(Coord node, Coord endNode) {
            return (float) node.distance(endNode);
        }
    };
    /**
     * A predefined Heuristic for Coord nodes in a 2D plane where the heuristic is not used, and all cells are
     * considered equivalent regardless of actual distance.
     */
    public static final Heuristic<Coord> DIJKSTRA = new Heuristic<Coord>() {
        @Override
        public float estimate(Coord node, Coord endNode) {
            return 1f;
        }
    };

    public static final List<Heuristic<Coord>> HEURISTICS = Collections.unmodifiableList(Maker.makeList(MANHATTAN, CHEBYSHEV, EUCLIDEAN, DIJKSTRA));
    
    public Arrangement<Coord> positions;
    public ArrayList<List<Connection<Coord>>> allConnections;

    /**
     * Creates an empty DefaultGraph with capacity 16; you must call {@link #init(char[][], boolean)} or
     * otherwise manually initialize this DefaultGraph before using it. The capacity is expected to resize to fit a
     * typically-much-larger map.
     */
    public DefaultGraph() {
        this(16);
    }

    /**
     * Creates an empty DefaultGraph with the given minimum capacity; you must call {@link #init(char[][], boolean)} or
     * otherwise manually initialize this DefaultGraph before using it.
     * @param capacity how many nodes, approximately, to expect in the map
     */
    public DefaultGraph(int capacity) {
        positions = new Arrangement<>(capacity);
        allConnections = new ArrayList<>(capacity);
    }

    /**
     * Creates a DefaultGraph and immediately initializes it using {@link #init(char[][], boolean)}, with eightWay set
     * to false (only orthogonal neighbors will be connected).
     * @param map a char[][] where {@code #} is used to mean a wall or an impassable cell
     */
    public DefaultGraph(char[][] map)
    {
        this(map, false);
    }

    /**
     * Creates a DefaultGraph and immediately initializes it using {@link #init(char[][], boolean)}.
     * @param map a char[][] where {@code #} is used to mean a wall or an impassable cell
     * @param eightWay if true, this will try to connect diagonals as well as orthogonally adjacent cells
     */
    public DefaultGraph(char[][] map, boolean eightWay)
    {
        this(map.length * map[0].length * 3 >> 2); // 3/4 of the map could maybe be walkable; more is rare
        init(map, eightWay);
    }

    /**
     * Initializes or resets the nodes this knows about and their connections to their neighbors. This can be called
     * more than once on a DefaultGraph, typically when the map changes, and will forget its old nodes and only store
     * the ones in the latest map. The {@code map} should use {@code #} for walls and impassable areas; all other chars
     * will be considered equally passable.
     * @param map a char[][] where {@code #} is used to mean a wall or an impassable cell
     * @param eightWay if true, this will try to connect diagonals as well as orthogonally adjacent cells
     */
    public void init(char[][] map, boolean eightWay)
    {
        final int width = map.length, height = map[0].length;
        Coord.expandPoolTo(width, height);
        positions.clear();
        allConnections.clear();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(map[i][j] != '#')
                    positions.add(Coord.get(i, j));
            }
        }
        final Direction[] dirs = eightWay ? Direction.CLOCKWISE : Direction.CARDINALS_CLOCKWISE;
        final int size = positions.size(), dirCount = dirs.length;
        for (int i = 0; i < size; i++) {
            Coord c = positions.keyAt(i);
            ArrayList<Connection<Coord>> list = new ArrayList<>(dirCount);
            for(Direction dir : dirs)
            {
                Coord offset = c.translate(dir);
                if(positions.containsKey(offset))
                    list.add(new DefaultConnection<Coord>(c, offset));
            }
            allConnections.add(list);
        }
    }
    
    /**
     * Returns the connections outgoing from the given node.
     *
     * @param fromNode the node whose outgoing connections will be returned
     * @return the array of connections outgoing from the given node.
     */
    @Override
    public List<Connection<Coord>> getConnections(Coord fromNode) {
        return allConnections.get(positions.getInt(fromNode));
    }

    /**
     * Returns the unique index of the given node.
     *
     * @param node the node whose index will be returned
     * @return the unique index of the given node.
     */
    @Override
    public int getIndex(Coord node) {
        return positions.getInt(node);
    }

    /**
     * Returns the number of nodes in this graph.
     */
    @Override
    public int getNodeCount() {
        return positions.size();
    }
}
