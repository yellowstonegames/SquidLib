package squidpony.squidgrid.mapping.locks;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.locks.generators.LayoutGenerator;
import squidpony.squidmath.Coord;

import java.util.*;

/**
 * Represents an individual space within the dungeon.
 * <p>
 * A Room contains:
 * <ul>
 * <li>an item ({@link Symbol}) that the player may (at his or her choice)
 *      collect by passing through this Room;
 * <li>an intensity, which is a measure of the relative difficulty of the room
 *      and ranges from 0.0 to 1.0;
 * <li>{@link Edge}s for each door to an adjacent Room.
 * </ul>
 */
public class Room {

    protected Condition precond;
    public final int id;
    protected Set<Coord> coords;
    protected Coord center;
    protected int item;
    protected List<Edge> edges;
    protected double intensity;
    protected Room parent;
    protected List<Room> children;
    
    /**
     * Creates a Room at the given coordinates, with the given parent,
     * containing a specific item, and having a certain pre-{@link Condition}.
     * <p>
     * The parent of a room is the parent node of this Room in the initial
     * tree of the dungeon during
     * {@link LayoutGenerator#generate()}, and
     * before
     * {@link LayoutGenerator#graphify()}.
     * 
     * @param coords    the coordinates of the new room
     * @param parent    the parent room or null if it is the root / entry room
     * @param item      the symbol to place in the room or null if no item
     * @param precond   the precondition of the room
     * @see Condition
     */
    public Room(int id, Set<Coord> coords, Room parent, int item, Condition precond) {
        this.id = id;
        this.coords = coords;
        this.item = item;
        this.edges = new ArrayList<Edge>();
        this.precond = precond;
        this.intensity = 0.0;
        this.parent = parent;
        this.children = new ArrayList<Room>(3);
        // all edges initially null
        
        int x = 0, y = 0;
        for (Coord xy: coords) {
            x += xy.x; y += xy.y;
        }
        center = Coord.get(x/coords.size(), y/coords.size());
    }
    
    public Room(int id, Coord coords, Room parent, int item, Condition precond) {
        this(id, Collections.singleton(coords), parent, item,
                precond);
    }
    
    /**
     * @return the intensity of the Room
     * @see Room
     */
    public double getIntensity() {
        return intensity;
    }
    
    /**
     * @param intensity the value to set the Room's intensity to
     * @see Room
     */
    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    /**
     * @return  the item contained in the Room, or null if there is none
     */
    public int getItem() {
        return item;
    }

    /**
     * @param item  the item to place in the Room
     */
    public void setItem(int item) {
        this.item = item;
    }

    /**
     * Gets the array of {@link Edge} slots this Room has. There is one slot
     * for each compass {@link Direction}. Non-null slots in this array
     * represent links between this Room and adjacent Rooms.
     * 
     * @return the array of Edges
     */
    public List<Edge> getEdges() {
        return edges;
    }
    
    /**
     * Gets the Edge object for a link in a given direction.
     * 
     * @param targetRoomId the compass {@link Direction} of the Edge for the link from this
     *          Room to an adjacent Room
     * @return  the {@link Edge} for the link in the given direction, or null if
     *          there is no link from this Room in the given direction
     */
    public Edge getEdge(int targetRoomId) {
        for (Edge e: edges) {
            if (e.getTargetRoomId() == targetRoomId)
                return e;
        }
        return null;
    }
    
    public Edge setEdge(int targetRoomId, int symbol) {
        Edge e = getEdge(targetRoomId);
        if (e != null) {
            e.symbol = symbol;
        } else {
            e = new Edge(targetRoomId, symbol);
            edges.add(e);
        }
        return e;
    }
    
    /**
     * Gets the number of Rooms this Room is linked to.
     * 
     * @return  the number of links
     */
    public int linkCount() {
        return edges.size();
    }
    
    /**
     * @return whether this room is the entry to the dungeon.
     */
    public boolean isStart() {
        return Symbol.isStart(item);
    }
    
    /**
     * @return whether this room is the goal room of the dungeon.
     */
    public boolean isGoal() {
        return Symbol.isGoal(item);
    }

    /**
     * @return whether this room contains the dungeon's boss.
     */
    public boolean isBoss() {
        return Symbol.isBoss(item);
    }
    
    /**
     * @return whether this room contains the dungeon's switch object.
     */
    public boolean isSwitch() {
        return Symbol.isSwitch(item);
    }
    
    /**
     * @return the precondition for this Room
     * @see Condition
     */
    public Condition getPrecond() {
        return precond;
    }
    
    /**
     * @param precond   the precondition to set this Room's to
     * @see Condition
     */
    public void setPrecond(Condition precond) {
        this.precond = precond;
    }

    /**
     * @return the parent of this Room
     * @see Room#Room
     */
    public Room getParent() {
        return parent;
    }

    /**
     * @param parent the Room to set this Room's parent to
     * @see Room#Room
     */
    public void setParent(Room parent) {
        this.parent = parent;
    }
    
    /**
     * @return the collection of Rooms this Room is a parent of
     * @see Room#Room
     */
    public Collection<Room> getChildren() {
        return children;
    }
    
    /**
     * Registers this Room as a parent of another.
     * Does not modify the child room's parent property.
     * 
     * @param child the room to parent
     */
    public void addChild(Room child) {
        children.add(child);
    }
    
    public Set<Coord> getCoords() {
        return coords;
    }
    
    public Coord getCenter() {
        return center;
    }
    
    public String toString() {
        return "Room(" + coords.toString() + ")";
    }
    
}
