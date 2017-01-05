package squidpony.squidgrid.mapping.locks.constraints;

import squidpony.squidgrid.mapping.locks.Room;
import squidpony.squidgrid.mapping.locks.generators.ILayoutGenerator;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;

import java.util.*;

/**
 * Controls which spaces are valid for an
 * {@link ILayoutGenerator} to create
 * {@link Room}s in.
 * <p>
 * Essentially just a Set<{@link Coord}> with some convenience methods.
 * 
 * @see Coord
 * @see SpaceConstraints
 */
public class SpaceMap {
    protected OrderedSet<Coord> spaces = new OrderedSet<>();
    
    public int numberSpaces() {
        return spaces.size();
    }
    
    public boolean get(Coord c) {
        return spaces.contains(c);
    }
    
    public void set(Coord c, boolean val) {
        if (val)
            spaces.add(c);
        else
            spaces.remove(c);
    }
    
    private Coord getFirst() {
        return spaces.first();
    }
    
    public Collection<Coord> getBottomSpaces() {
        List<Coord> bottomRow = new ArrayList<Coord>();
        bottomRow.add(getFirst());
        int bottomY = getFirst().y;
        for (Coord space: spaces) {
            if (space.y > bottomY) {
                bottomY = space.y;
                bottomRow = new ArrayList<Coord>();
                bottomRow.add(space);
            } else if (space.y == bottomY) {
                bottomRow.add(space);
            }
        }
        return bottomRow;
    }
}