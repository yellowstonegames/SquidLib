package squidpony.squidai.astar;

import squidpony.Maker;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Arrangement;
import squidpony.squidmath.Coord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A convenient implementation of {@link Graph} for {@link Coord} nodes; this also allows constructing a Graph with a
 * 2D char array like those produced all over SquidLib. Some predefined heuristics are available as static constants
 * here. You can "show" a DefaultGraph in conjunction with a {@link Pathfinder}, which appends a big grid of node costs
 * matching the Pathfinder to a StringBuilder.
 */
public class DefaultGraph implements Graph<Coord> {

    /**
     * A predefined Heuristic for Coord nodes in a 2D plane where diagonal movement is estimated as costing twice as
     * much as orthogonal movement. This is a good choice for graphs where only four-way movement is used.
     */
    public static final Heuristic<Coord> MANHATTAN = new Heuristic<Coord>() {
        @Override
        public double estimate(Coord node, Coord endNode) {
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
        public double estimate(Coord node, Coord endNode) {
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
        public double estimate(Coord node, Coord endNode) {
            return node.distance(endNode);
        }
    };
    /**
     * A predefined Heuristic for Coord nodes in a 2D plane where the heuristic is not used, and all cells are
     * considered equivalent regardless of actual distance.
     */
    public static final Heuristic<Coord> DIJKSTRA = new Heuristic<Coord>() {
        @Override
        public double estimate(Coord node, Coord endNode) {
            return 1.0;
        }
    };

    public static final List<Heuristic<Coord>> HEURISTICS = Collections.unmodifiableList(Maker.makeList(MANHATTAN, CHEBYSHEV, EUCLIDEAN, DIJKSTRA));
    
    public Arrangement<Coord> positions;
    public ArrayList<List<Connection<Coord>>> allConnections;
    public int width;
    public int height;

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
     * Creates a DefaultGraph and immediately initializes it using {@link #init(double[][], boolean)}, with eightWay set
     * to false (only orthogonal neighbors will be connected).
     * @param costs a double[][] where negative values are blocked and any other values are the cost to enter that cell
     */
    public DefaultGraph(double[][] costs)
    {
        this(costs, false);
    }

    /**
     * Creates a DefaultGraph and immediately initializes it using {@link #init(double[][], boolean)}.
     * @param costs a double[][] where negative values are blocked and any other values are the cost to enter that cell
     * @param eightWay if true, this will try to connect diagonals as well as orthogonally adjacent cells
     */
    public DefaultGraph(double[][] costs, boolean eightWay)
    {
        this(costs.length * costs[0].length * 3 >> 2); // 3/4 of the map could maybe be walkable; more is rare
        init(costs, eightWay);
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
        width = map.length;
        height = map[0].length;
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
                    list.add(new DefaultConnection<>(c, offset));
            }
            allConnections.add(list);
        }
    }

    /**
     * Initializes or resets the nodes this knows about and their connections to their neighbors. This can be called
     * more than once on a DefaultGraph, typically when the map changes, and will forget its old nodes and only store
     * the ones in the latest map. The double[][] {@code costs} stores the costs to enter cells; it is typically
     * produced by {@link squidpony.squidgrid.mapping.DungeonUtility#generateAStarCostMap(char[][], Map, double)}.
     * Negative values in costs mark cells that cannot be entered (like walls); 1.0 is used for normal cells that can be
     * entered easily, and higher values mark more challenging cells to enter.
     * @param costs a double[][] where negative values are blocked and any other values are the cost to enter that cell
     * @param eightWay if true, this will try to connect diagonals as well as orthogonally adjacent cells
     */
    public void init(double[][] costs, boolean eightWay)
    {
        width = costs.length;
        height = costs[0].length;
        Coord.expandPoolTo(width, height);
        positions.clear();
        allConnections.clear();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(costs[i][j] >= 0.0)
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
                    list.add(new CostlyConnection<>(c, offset, costs[offset.x][offset.y]));
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

    /**
     * Given a StringBuilder to append to, a Pathfinder with Coord nodes that has run and at least attempted to find a
     * path, and the {@link Pathfinder#metrics} that the Pathfinder must have been configured to record, this makes a
     * textual representation of the explored nodes and appends it to the StringBuilder. This shows the actual costs of
     * all explored nodes in the Pathfinder for its latest path. If you intend to show a DefaultGraph like this, the
     * Pathfinder must be constructed with {@link Pathfinder#Pathfinder(Graph, boolean)} where calculateMetrics is true
     * (or where {@link Pathfinder#metrics} was assigned {@code new Pathfinder.Metrics()} before calculating a path).
     * This will vary in displayed width based on the maximum width required to print any node, so if the
     * longest-distance node costs 100 to enter, the displayed width will be longer than if the longest-distance node
     * only cost 99. Any unvisited nodes, including walls and out-of-the-way areas, will be shown with some number of
     * '#' chars. Spaces will separate nodes on the same line.
     * @param sb a StringBuilder that will be appended to and returned
     * @param pathfinder a Pathfinder that must have at least tried to find a path (and must have metrics, see below)
     * @param metrics the Metrics for the above Pathfinder, which are required to be non-null when the path was tried
     * @return the StringBuilder, after appending what is usually a lot of text in a grid
     */
    public StringBuilder show(StringBuilder sb, Pathfinder<Coord> pathfinder, Pathfinder.Metrics metrics) {
        int len = (int)Math.log10(Math.round(metrics.maxCost)) + 1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) { 
                Coord c = Coord.get(x, y);
                int index = positions.getInt(c);
                Pathfinder.NodeRecord<Coord> nr;
                if(index < 0 || (nr = pathfinder.getNodeRecord(c)).category == Pathfinder.UNVISITED) {
                    for (int i = 0; i < len; i++) {
                        sb.append('#');
                    }
                }
                else {
                    int cost = (int)(nr.costSoFar + 0.5);
                    if(cost == 0)
                    {
                        for (int i = 1; i < len; i++) {
                            sb.append(' ');
                        }
                        sb.append('0');
                    }
                    else {
                        int used = len - (int) Math.log10(cost) - 1;
                        for (int i = 0; i < used; i++) {
                            sb.append(' ');
                        }
                        sb.append(cost);
                    }
                }
                sb.append(' ');
            }
            if(y + 1 < height) 
                sb.append('\n');
        }
        return sb;
    }
}
