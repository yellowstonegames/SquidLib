package squidpony.squidai.astar;

import squidpony.squidgrid.Direction;
import squidpony.squidmath.Arrangement;
import squidpony.squidmath.Coord;

import java.util.ArrayList;
import java.util.List;

public class DefaultGraph implements Graph<Coord> {
    
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
        final int size = positions.size();
        final Direction[] dirs = eightWay ? Direction.CLOCKWISE : Direction.CARDINALS_CLOCKWISE;
        for (int i = 0; i < size; i++) {
            Coord c = positions.keyAt(i);
            ArrayList<Connection<Coord>> list = new ArrayList<>(4);
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
