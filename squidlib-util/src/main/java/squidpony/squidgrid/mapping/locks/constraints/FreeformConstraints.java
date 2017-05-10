package squidpony.squidgrid.mapping.locks.constraints;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.locks.IRoomLayout;
import squidpony.squidgrid.mapping.locks.util.GenerationFailureException;
import squidpony.squidmath.Coord;
import squidpony.squidmath.IntVLA;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

import java.util.Collections;
import java.util.Set;

public class FreeformConstraints implements ILayoutConstraints {
    
    public static final int DEFAULT_MAX_KEYS = 8;
    
    protected static class Group {
        public int id;
        public OrderedSet<Coord> coords;
        public OrderedSet<Integer> adjacentGroups;
        
        public Group(int id) {
            this.id = id;
            this.coords = new OrderedSet<>();
            this.adjacentGroups = new OrderedSet<>();
        }
    }
    
    protected ColorMap colorMap;
    protected OrderedMap<Integer, Group> groups;
    protected int maxKeys;

    public FreeformConstraints(ColorMap colorMap) {
        this.colorMap = colorMap;
        this.groups = new OrderedMap<>();
        this.maxKeys = DEFAULT_MAX_KEYS;
        
        analyzeMap();
    }
    
    protected void analyzeMap() {
        colorMap.checkConnected();
        
        for (int x = colorMap.getLeft(); x <= colorMap.getRight(); ++x)
            for (int y = colorMap.getTop(); y <= colorMap.getBottom(); ++y) {
                Integer val = colorMap.get(x,y);
                if (val == null) continue;
                Group group = groups.get(val);
                if (group == null) {
                    group = new Group(val);
                    groups.put(val, group);
                }
                group.coords.add(Coord.get(x+127,y+127));
            }
        System.out.println(groups.size() + " groups");
        
        for (Group group: groups.values()) {
            for (Coord xy: group.coords) {
                for (Direction d: Direction.CARDINALS) {
                    Coord neighbor = xy.translate(d);
                    if (group.coords.contains(neighbor)) continue;
                    Integer val = colorMap.get(neighbor.x, neighbor.y);
                    if (val != null && allowRoomsToBeAdjacent(group.id, val)) {
                        group.adjacentGroups.add(val);
                    }
                }
            }
        }
        
        checkConnected();
    }
    
    protected boolean isConnected() {
        // This is different from ColorMap.checkConnected because it also checks
        // what the client says for allowRoomsToBeAdjacent allows the map to be
        // full connected.
        // Do a breadth first search starting at the top left to check if
        // every position is reachable.
        OrderedSet<Integer> world = groups.keysAsOrderedSet(),
                    queue = new OrderedSet<Integer>();
        
        Integer first = world.first();
        world.remove(first);
        queue.add(first);
        
        while (!queue.isEmpty()) {
            Integer pos = queue.removeFirst();
            IntVLA rooms = getAdjacentRooms(pos, getMaxKeys()+1);
            for (int i = 0; i < rooms.size; i++) {
                Integer adjId = rooms.get(i);
                
                if (world.contains(adjId)) {
                    world.remove(adjId);
                    queue.add(adjId);
                }
            }
        }
        
        return world.size() == 0;
    }
    
    protected void checkConnected() {
        if (!isConnected()) {
            // Parts of the map are unreachable!
            throw new GenerationFailureException("ColorMap is not fully connected");
        }
    }
    
    @Override
    public int getMaxRooms() {
        return groups.size();
    }

    @Override
    public int getMaxKeys() {
        return maxKeys;
    }
    
    public void setMaxKeys(int maxKeys) {
        this.maxKeys = maxKeys;
    }

    @Override
    public int getMaxSwitches() {
        return 0;
    }

    @Override
    public IntVLA initialRooms() {
        return IntVLA.with(groups.getAt(0).id);
    }

    @Override
    public IntVLA getAdjacentRooms(int id, int keyLevel) {
        IntVLA options = new IntVLA();
        for (int i: groups.get(id).adjacentGroups) {
            options.add(i);
        }
        return options;
    }

    /* The reason for this being separate from getAdjacentRooms is that this
     * method is called at most once for each pair of rooms during analyzeMap,
     * while getAdjacentRooms is called many times during generation under the
     * assumption that it's simply a cheap "getter". Subclasses may override
     * this method to perform more expensive checks than with getAdjacentRooms.
     */
    protected boolean allowRoomsToBeAdjacent(int id0, int id1) {
        return true;
    }
    
    @Override
    public Set<Coord> getCoords(int id) {
        return Collections.unmodifiableSet(groups.get(id).coords);
    }

    @Override
    public boolean isAcceptable(IRoomLayout dungeon) {
        return true;
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
