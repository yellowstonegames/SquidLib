package squidpony.squidgrid.mapping.locks;

import squidpony.squidgrid.mapping.locks.util.Rect2I;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedMap;

import java.util.Collection;

/**
 * @see IRoomLayout
 */
public class RoomLayout implements IRoomLayout {

    protected int itemCount;
    protected OrderedMap<Integer, Room> rooms;
    protected Rect2I bounds;
    
    public RoomLayout() {
        rooms = new OrderedMap<>();
        bounds = Rect2I.fromExtremes(Integer.MAX_VALUE,Integer.MAX_VALUE,
                Integer.MIN_VALUE,Integer.MIN_VALUE);
    }

    @Override
    public Rect2I getExtentBounds() {
        return bounds;
    }
    
    @Override
    public Collection<Room> getRooms() {
        return rooms.values();
    }
    
    @Override
    public int roomCount() {
        return rooms.size();
    }
    
    @Override
    public Room get(int id) {
        return rooms.get(id);
    }
    
    @Override
    public void add(Room room) {
        rooms.put(room.id, room);
        
        for (Coord xy: room.getCoords()) {
            if (xy.x < bounds.left()) {
                bounds = Rect2I.fromExtremes(xy.x, bounds.top(),
                        bounds.right(), bounds.bottom());
            }
            if (xy.x >= bounds.right()) {
                bounds = Rect2I.fromExtremes(bounds.left(), bounds.top(),
                        xy.x+1, bounds.bottom());
            }
            if (xy.y < bounds.top()) {
                bounds = Rect2I.fromExtremes(bounds.left(), xy.y,
                        bounds.right(), bounds.bottom());
            }
            if (xy.y >= bounds.bottom()) {
                bounds = Rect2I.fromExtremes(bounds.left(), bounds.top(),
                        bounds.right(), xy.y+1);
            }
        }
    }
    
    @Override
    public void linkOneWay(Room room1, Room room2) {
        linkOneWay(room1, room2, Symbol.NOTHING);
    }
    
    @Override
    public void link(Room room1, Room room2) {
        link(room1, room2, Symbol.NOTHING);
    }
    
    @Override
    public void linkOneWay(Room room1, Room room2, int cond) {
        assert rooms.values().contains(room1) && rooms.values().contains(room2);
        room1.setEdge(room2.id, cond);
    }
    
    @Override
    public void link(Room room1, Room room2, int cond) {
        linkOneWay(room1, room2, cond);
        linkOneWay(room2, room1, cond);
    }
    
    @Override
    public boolean roomsAreLinked(Room room1, Room room2) {
        return room1.getEdge(room2.id) != null ||
            room2.getEdge(room1.id) != null;
    }
    
    @Override
    public Room findStart() {
        for (Room room: getRooms()) {
            if (room.isStart()) return room;
        }
        return null;
    }

    @Override
    public Room findBoss() {
        for (Room room: getRooms()) {
            if (room.isBoss()) return room;
        }
        return null;
    }

    @Override
    public Room findGoal() {
        for (Room room: getRooms()) {
            if (room.isGoal()) return room;
        }
        return null;
    }

    @Override
    public Room findSwitch() {
        for (Room room: getRooms()) {
            if (room.isSwitch()) return room;
        }
        return null;
    }

}
