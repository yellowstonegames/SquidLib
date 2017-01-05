package squidpony.squidgrid.mapping.locks.constraints;

import squidpony.squidmath.Coord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Constrains the coordinates where Rooms may be placed to be only those within
 * the {@link SpaceMap}, as well as placing limitations on the number of keys
 * and switches.
 * 
 * @see CountConstraints
 * @see SpaceMap
 */
public class SpaceConstraints extends CountConstraints {

    public static final int DEFAULT_MAX_KEYS = 4,
            DEFAULT_MAX_SWITCHES = 1;
    
    protected SpaceMap spaceMap;
    
    public SpaceConstraints(SpaceMap spaceMap) {
        super(spaceMap.numberSpaces(), DEFAULT_MAX_KEYS, DEFAULT_MAX_SWITCHES);
        this.spaceMap = spaceMap;
    }

    @Override
    protected boolean validRoomCoords(Coord c) {
        return spaceMap.get(c);
    }

    @Override
    public Collection<Integer> initialRooms() {
        List<Integer> ids = new ArrayList<Integer>();
        for (Coord xy: spaceMap.getBottomSpaces()) {
            ids.add(getRoomId(xy));
        }
        return ids;
    }
    
    

}
