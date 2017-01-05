package squidpony.squidgrid.mapping.locks.constraints;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.locks.IRoomLayout;
import squidpony.squidgrid.mapping.locks.generators.ILayoutGenerator;
import squidpony.squidmath.Arrangement;
import squidpony.squidmath.Coord;
import squidpony.squidmath.IntVLA;

import java.util.*;

/**
 * Limits the {@link ILayoutGenerator} in
 * the <i>number</i> of keys, switches and rooms it is allowed to place.
 * 
 * Also restrict to a grid of 1x1 rooms.
 * 
 * @see ILayoutConstraints
 */
public class CountConstraints implements ILayoutConstraints {

    protected int maxSpaces, maxKeys, maxSwitches;

    protected Arrangement<Coord> roomIds;
    protected int firstRoomId;
    
    public CountConstraints(int maxSpaces, int maxKeys, int maxSwitches) {
        this.maxSpaces = maxSpaces;
        this.maxKeys = maxKeys;
        this.maxSwitches = maxSwitches;

        roomIds = new Arrangement<Coord>();
        Coord first = Coord.get(127,127);
        firstRoomId = getRoomId(first);
    }
    
    public int getRoomId(Coord xy) {
        if (roomIds.containsKey(xy)) {
            return roomIds.get(xy);
        } else {
            roomIds.add(xy);
            return roomIds.size()-1;
        }
    }
    
    public Coord getRoomCoords(int id) {
        assert roomIds.containsValue(id);
        return roomIds.keyAt(id);

    }
    
    @Override
    public int getMaxRooms() {
        return maxSpaces;
    }
    
    public void setMaxSpaces(int maxSpaces) {
        this.maxSpaces = maxSpaces;
    }
    
    @Override
    public IntVLA initialRooms() {
        return IntVLA.with(firstRoomId);
    }

    @Override
    public int getMaxKeys() {
        return maxKeys;
    }
    
    public void setMaxKeys(int maxKeys) {
        this.maxKeys = maxKeys;
    }
    
    @Override
    public boolean isAcceptable(IRoomLayout dungeon) {
        return true;
    }

    @Override
    public int getMaxSwitches() {
        return maxSwitches;
    }

    public void setMaxSwitches(int maxSwitches) {
        this.maxSwitches = maxSwitches;
    }

    protected boolean validRoomCoords(Coord c) {
        return c.y <= 0;
    }
    
    @Override
    public IntVLA getAdjacentRooms(int id, int keyLevel) {
        Coord xy = roomIds.keyAt(id);
        IntVLA ids = new IntVLA();
        for (Direction d: Direction.CARDINALS) {
            Coord neighbor = xy.translate(d);
            if (validRoomCoords(neighbor))
                ids.add(getRoomId(neighbor));
        }
        return ids;
    }

    @Override
    public Set<Coord> getCoords(int id) {
        return Collections.singleton(getRoomCoords(id));
    }

    @Override
    public double edgeGraphifyProbability(int id, int nextId) {
        return 0.2;
    }

    @Override
    public boolean roomCanFitItem(int id, int key) {
        return true;
    }

}
