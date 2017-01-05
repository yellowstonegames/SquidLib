package squidpony.squidgrid.mapping.locks.constraints;

import squidpony.squidgrid.mapping.locks.IRoomLayout;
import squidpony.squidgrid.mapping.locks.generators.ILayoutGenerator;
import squidpony.squidmath.Coord;
import squidpony.squidmath.IntVLA;

import java.util.Set;

/**
 * Implementing classes may specify constraints to be placed on Layout
 * generation.
 * 
 * @see ILayoutGenerator
 */
public interface ILayoutConstraints {

    /**
     * @return  the maximum number of Rooms an 
     * {@link ILayoutGenerator} may
     *          place in an {@link IRoomLayout}
     */
    int getMaxRooms();
    
    /**
     * @return  the maximum number of keys an 
     * {@link ILayoutGenerator} may
     *          place in an {@link IRoomLayout}
     */
    int getMaxKeys();

    /**
     * Gets the number of switches the
     * {@link ILayoutGenerator} is allowed to
     * place in an {@link IRoomLayout}.
     * Note only one switch is ever placed due to limitations of the current
     * algorithm.
     * 
     * @return  the maximum number of switches an
     * {@link ILayoutGenerator} may
     *          place in an {@link IRoomLayout}
     */
    int getMaxSwitches();
    
    /**
     * Gets the collection of ids from which an
     * {@link ILayoutGenerator} is allowed to
     * pick the entrance room.
     * 
     * @return the collection of ids
     */
    IntVLA initialRooms();
    
    /**
     * @return a weighted list of ids of rooms that are adjacent to the room
     * with the given id.
     */
    IntVLA getAdjacentRooms(int id, int keyLevel);
    
    /**
     * @return desired probability for an extra edge to be added between the
     * given rooms during the graphify phase.
     */
    double edgeGraphifyProbability(int id, int nextId);
    
    /**
     * @return a set of Coords which the room with the given id occupies.
     */
    Set<Coord> getCoords(int id);
    
    /**
     * Runs post-generation checks to determine the suitability of the dungeon.
     * 
     * @param dungeon   the {@link IRoomLayout} to check
     * @return  true to keep the dungeon, or false to discard the dungeon and
     *          attempt generation again
     */
    boolean isAcceptable(IRoomLayout dungeon);
    
    boolean roomCanFitItem(int id, int key);
    
}
