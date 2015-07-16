package squidpony.squidai;

import java.awt.*;
import java.util.HashMap;
import java.util.Set;

/**
 * Area of Effect interface meant to be implemented by various specific burst, line, flowing, and user-made AOE types.
 * Created by Tommy Ettinger on 5/8/2015.
 */
public interface AOE {
    /**
     * After an AOE has been constructed, it may need to have the affected area shifted over to a different position
     * without changing any other properties of the AOE. Some AOE implementations may have an origin where the AOE
     * starts emanating from, but the origin will not be affected by this method; instead the cell specified by target
     * must be enough on its own to select a different target area without the producer of the AOE needing to move.
     * @param aim a Point that will be used to change the location of the AOE without its producer needing to move
     */
    void shift(Point aim);

    /**
     * Given a Set of Points that the producer of the AOE wants to include in the region of this AOE, this method does
     * a quick approximation to see if there is any possibility that the AOE as currently configured might include one
     * of those Points within itself. It does not do a full, detailed scan, nor does it count how many opponents might
     * be included. It does not check the map to verify that there is any sort of path to a target. It is recommended
     * that the Set of Points consist only of enemies that are within FOV, which cuts down a lot on the amount of checks
     * this needs to make; if the game doesn't restrict the player's FOV, this is still recommended (just with a larger
     * FOV radius) because it prevents checking enemies on the other side of the map and through multiple walls.
     * @param targets a Set of Points that are desirable targets to include in this AOE
     * @return true if there could be at least one target within the AOE, false otherwise. Very approximate.
     */
    boolean mayContainTarget(Set<Point> targets);
    /**
     * This must be called before findArea() can be called, and takes a char[][] with '#' for walls, '.' for floors.
     * It must be bounded with walls, which DungeonGenerator does automatically.
     * @param map width first, height second, 2D char array.
     */
    void setMap(char[][] map);
    /**
     * This is how an AOE interacts with anything that uses it. It expects a map to have already been set with setMap,
     * with '#' for walls, '.' for floors and potentially other chars that implementors can use if they are present in
     * the map. The map must be bounded by walls, which DungeonGenerator does automatically and other generators can
     * easily add with two loops.
     *
     * This returns a HashMap of Point keys to Double values; if a cell is 100% affected by the AOE then the value
     * should be 1.0; if it is 50% affected it should be 0.5, if unaffected should be 0.0, etc. The Point keys should
     * have the same x and y as the x,y map positions they correspond to.
     * @return a HashMap of Point keys to Double values from 1.0 (fully affected) to 0.0 (unaffected).
     */
    HashMap<Point, Double> findArea();
}
