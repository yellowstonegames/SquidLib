package squidpony.squidgrid.mapping.locks;

import squidpony.squidgrid.mapping.locks.util.Rect2I;

import java.util.Collection;

/**
 * Represents the spatial layout of a lock-and-key puzzle and contains all
 * {@link Symbol}s, {@link Room}s and {@link Edge}s within the puzzle.
 */
public interface IRoomLayout {

    /**
     * @return  the rooms within the dungeon
     */
    Collection<Room> getRooms();
    /**
     * @return the number of rooms in the dungeon
     */
    int roomCount();

    /**
     * @param id        the id of the room
     * @return  the room with the given id
     */
    Room get(int id);

    /**
     * Adds a new room to the dungeon, overwriting any rooms already in it that
     * have the same coordinates.
     * 
     * @param room  the room to add
     */
    void add(Room room);

    /**
     * Adds a one-way unconditional edge between the given rooms.
     * A one-way edge may be used to travel from room1 to room2, but not room2
     * to room1.
     * 
     * @param room1 the first room to link
     * @param room2 the second room to link
     */
    void linkOneWay(Room room1, Room room2);
    /**
     * Adds a two-way unconditional edge between the given rooms.
     * A two-way edge may be used to travel from each room to the other.
     * 
     * @param room1 the first room to link
     * @param room2 the second room to link
     */
    void link(Room room1, Room room2);
    /**
     * Adds a one-way conditional edge between the given rooms.
     * A one-way edge may be used to travel from room1 to room2, but not room2
     * to room1.
     * 
     * @param room1 the first room to link
     * @param room2 the second room to link
     * @param cond  the condition on the edge
     */
    void linkOneWay(Room room1, Room room2, int cond);
    /**
     * Adds a two-way conditional edge between the given rooms.
     * A two-way edge may be used to travel from each room to the other.
     * 
     * @param room1 the first room to link
     * @param room2 the second room to link
     * @param cond  the condition on the edge
     */
    void link(Room room1, Room room2, int cond);
    /**
     * Tests whether two rooms are linked.
     * Two rooms are linked if there are any edges (in any direction) between
     * them.
     * 
     * @return  true if the rooms are linked, false otherwise
     */
    boolean roomsAreLinked(Room room1, Room room2);
    
    /**
     * @return  the room containing the START symbol
     */
    Room findStart();
    /**
     * @return  the room containing the BOSS symbol
     */
    Room findBoss();
    /**
     * @return  the room containing the GOAL symbol
     */
    Room findGoal();
    /**
     * @return  the room containing the SWITCH symbol
     */
    Room findSwitch();
    
    /**
     * Gets the {@link Rect2I} that encloses every room within the dungeon.
     * <p>
     * The Bounds object has the X coordinate of the West-most room, the Y
     * coordinate of the North-most room, the 'right' coordinate of the
     * East-most room, and the 'bottom' coordinate of the South-most room.
     * 
     * @return  the rectangle enclosing every room within the dungeon
     */
    Rect2I getExtentBounds();

}
