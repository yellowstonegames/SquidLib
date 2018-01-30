package squidpony.squidai;

import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Area of Effect interface meant to be implemented by various specific burst, line, flowing, and user-made AOE types.
 * Created by Tommy Ettinger on 5/8/2015.
 */
public interface AOE extends Serializable {
    /**
     * After an AOE has been constructed, it may need to have the affected area shifted over to a different position
     * without changing any other properties of the AOE. Some AOE implementations may have an origin where the AOE
     * starts emanating from, but the origin will not be affected by this method; instead the cell specified by target
     * must be enough on its own to select a different target area without the producer of the AOE needing to move.
     * @param aim a  that will be used to change the location of the AOE without its producer needing to move
     */
    void shift(Coord aim);

    /**
     * Given a Set of Points that the producer of the AOE wants to include in the region of this AOE, this method does
     * a quick approximation to see if there is any possibility that the AOE as currently configured might include one
     * of those Points within itself. It does not do a full, detailed scan, nor does it count how many opponents might
     * be included. It does not check the map to verify that there is any sort of path to a target. It is recommended
     * that the Set of Points consist only of enemies that are within FOV, which cuts down a lot on the amount of checks
     * this needs to make; if the game doesn't restrict the player's FOV, this is still recommended (just with a larger
     * FOV radius) because it prevents checking enemies on the other side of the map and through multiple walls.
     * @param targets a Collection (usually a Set) of Points that are desirable targets to include in this AOE
     * @return true if there could be at least one target within the AOE, false otherwise. Very approximate.
     */
    boolean mayContainTarget(Collection<Coord> targets);

    /**
     * Returns a OrderedMap of Coord keys and ArrayList of Coord values, where each Coord key is an ideal location to
     * hit as many of the Points in targets as possible without hitting any Points in requiredExclusions, and each value
     * is the collection of targets that will be hit if the associated key is used. The length of any ArrayList in the
     * returned collection's values will be the number of targets likely to be affected by the AOE when shift() is
     * called with the Coord key as an argument; all of the ArrayLists should have the same length. The second argument
     * may be null, in which case this will initialize it to an empty Set of Coord and disregard it.
     *
     * With complex maps and varied arrangements of obstacles and desirable targets, calculating the best points to
     * evaluate for AI can be computationally difficult. This method provides a way to calculate with good accuracy
     * the best Points to pass to shift(Coord) before calling findArea(). For "blackened thrash industrial death metal"
     * levels of brutality for the AI, the results of this can be used verbatim, but for more reasonable AI levels, you
     * can intentionally alter the best options to simulate imperfect aim or environmental variance on the AOE.
     *
     * Beast-like creatures that do not need devious AI should probably not use this method at all and instead use
     * shift(Coord) with the location of some enemy (probably the closest) as its argument.
     * @param targets a Set of Points that are desirable targets to include in this AOE
     * @param requiredExclusions a Set of Points that this tries strongly to avoid including in this AOE
     * @return a OrderedMap of Coord keys and ArrayList of Coord values where keys are ideal locations and values are the target points that will be hit when that key is used.
     */
    OrderedMap<Coord, ArrayList<Coord>> idealLocations(Collection<Coord> targets, Collection<Coord> requiredExclusions);

    /**
     * A variant of idealLocations that takes two groups of desirable targets, and will rate locations by how many
     * priorityTargets are in the AOE, then by how many lesserTargets are in the AOE, and will only consider locations
     * that do not affect a Coord in requiredExclusions. Unlike the variant of idealLocations that only takes one group
     * of targets, this variant can return a collection with ArrayList values where the same Coord appears four times
     * in the same ArrayList; this is done only for priorityTargets that are affected by the target Coord at the
     * associated key, and is done so that the length of each similar-quality ArrayList should be identical (since a
     * priorityTarget is worth four times what a lesserTarget is worth in the calculation this uses).
     * @param priorityTargets A Set of Points that are the most-wanted targets to include in this AOE
     * @param lesserTargets A Set of Points that are the less-wanted targets to include in this AOE, should not overlap with priorityTargets
     * @param requiredExclusions a Set of Points that this tries strongly to avoid including in this AOE
     * @return a OrderedMap of Coord keys and ArrayList of Coord values where keys are ideal locations and values are the target points that will be hit when that key is used.
     */
    OrderedMap<Coord, ArrayList<Coord>> idealLocations(Collection<Coord> priorityTargets, Collection<Coord> lesserTargets, Collection<Coord> requiredExclusions);

    /**
     * This must be called before any other methods, and takes a char[][] with '#' for walls, anything else for floors.
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
     * This returns an OrderedMap of Coord keys to Double values; if a cell is 100% affected by the AOE then the value
     * should be 1.0; if it is 50% affected it should be 0.5, if unaffected should be 0.0, etc. The Coord keys should
     * have the same x and y as the x,y map positions they correspond to.
     * @return an OrderedMap of Coord keys to Double values from 1.0 (fully affected) to 0.0 (unaffected).
     */
    OrderedMap<Coord, Double> findArea();

    /**
     * Get the position from which the AOE originates, which may be related to the location of the AOE's effect, as for
     * lines, cones, and other emitted effects, or may be unrelated except for determining which enemies can be seen
     * or targeted from a given origin point (as for distant effects that radiate from a chosen central point, but
     * have a maxRange at which they can deliver that effect).
     */
    Coord getOrigin();

    /**
     * Set the position from which the AOE originates, which may be related to the location of the AOE's effect, as for
     * lines, cones, and other emitted effects, or may be unrelated except for determining which enemies can be seen
     * or targeted from a given origin point (as for distant effects that radiate from a chosen central point, but
     * have a maxRange at which they can deliver that effect).
     */
    void setOrigin(Coord origin);

    /**
     * Gets the AimLimit enum that can be used to restrict points this checks (defaults to null if not set).
     * You can use limitType to restrict any Points that might be processed based on the given origin (which will be
     * used as the geometric origin for any calculations this makes) with AimLimit values having the following meanings:
     *
     * <ul>
     *     <li>AimLimit.FREE makes no restrictions; it is equivalent here to passing null for limit.</li>
     *     <li>AimLimit.EIGHT_WAY will only consider Points to be valid targets
     *     if they are along a straight line with an angle that is a multiple of 45 degrees, relative to the positive x
     *     axis. Essentially, this limits the points to those a queen could move to in chess.</li>
     *     <li>AimLimit.ORTHOGONAL will cause the AOE to only consider Points to be valid targets if
     *     they are along a straight line with an angle that is a multiple of 90 degrees, relative to the positive x
     *     axis. Essentially, this limits the points to those a rook could move to in chess.</li>
     *     <li>AimLimit.DIAGONAL will cause the AOE to only consider Points to be valid targets if they are along a
     *     straight line with an angle that is 45 degrees greater than a multiple of 90 degrees, relative to the
     *     positive x axis. Essentially, this limits the points to those a bishop could move to in chess.</li>
     *     <li>null will cause the AOE to consider all points.</li>
     * </ul>
     */
    AimLimit getLimitType();
    /**
     * The minimum inclusive range that the AOE can be shift()-ed to using the distance measurement from radiusType.
     */
    int getMinRange();
    /**
     * The maximum inclusive range that the AOE can be shift()-ed to using the distance measurement from radiusType.
     */
    int getMaxRange();
    /**
     * Used to determine distance from origin for the purposes of selecting a target location that is within the bounds
     * of minRange and maxRange. Not necessarily used for the implementation of the AOE (randomized-floodfill-based AOE
     * should almost always use Manhattan distance for its spread due to how the algorithm works, but the positioning of
     * where that floodfill should be allowed to start should likely follow the same distance measurement as the rest of
     * the game, like Radius.SQUARE for Chebyshev distance/8-way movement).
     */
    Radius getMetric();

    /**
     * Gets the same values returned by getLimitType(), getMinRange(), getMaxRange(), and getMetric() bundled into one
     * Reach object.
     * @return a non-null Reach object.
     */
    Reach getReach();

    /**
     * You can use limitType to restrict any Points that might be processed based on the given origin (which will be
     * used as the geometric origin for any calculations this makes) with AimLimit values having the following meanings:
     *
     * <ul>
     *     <li>AimLimit.FREE makes no restrictions; it is equivalent here to passing null for limit.</li>
     *     <li>AimLimit.EIGHT_WAY will only consider Points to be valid targets
     *     if they are along a straight line with an angle that is a multiple of 45 degrees, relative to the positive x
     *     axis. Essentially, this limits the points to those a queen could move to in chess.</li>
     *     <li>AimLimit.ORTHOGONAL will cause the AOE to only consider Points to be valid targets if
     *     they are along a straight line with an angle that is a multiple of 90 degrees, relative to the positive x
     *     axis. Essentially, this limits the points to those a rook could move to in chess.</li>
     *     <li>AimLimit.DIAGONAL will cause the AOE to only consider Points to be valid targets if they are along a
     *     straight line with an angle that is 45 degrees greater than a multiple of 90 degrees, relative to the
     *     positive x axis. Essentially, this limits the points to those a bishop could move to in chess.</li>
     * </ul>
     *
     * Points that are not valid for this limit will simply not be considered.
     * @param limitType an AimLimit enum
     */
    void setLimitType(AimLimit limitType);
    /**
     * The minimum inclusive range that the AOE can be shift()-ed to using the distance measurement from radiusType.
     */
    void setMinRange(int minRange);
    /**
     * The maximum inclusive range that the AOE can be shift()-ed to using the distance measurement from radiusType.
     */
    void setMaxRange(int maxRange);
    /**
     * Used to determine distance from origin for the purposes of selecting a target location that is within the bounds
     * of minRange and maxRange. Not necessarily used for the implementation of the AOE (randomized-floodfill-based AOE
     * should almost always use Manhattan distance for its spread due to how the algorithm works, but the positioning of
     * where that floodfill should be allowed to start should likely follow the same distance measurement as the rest of
     * the game, like Radius.SQUARE for Chebyshev distance/8-way movement).
     */
    void setMetric(Radius metric);

    /**
     * Sets the same values as setLimitType(), setMinRange(), setMaxRange(), and setMetric() using one Reach object.
     * @param reach a non-null Reach object.
     */
    void setReach(Reach reach);

    /**
     * Unused because FOVCache rarely provides a speed boost and usually does the opposite. The implementation for this
     * method should be a no-op.
     * @param cache an FOV that could be an FOVCache for the current level; can be null to stop using the cache
     * @deprecated AOE doesn't really benefit from using an FOVCache
     */
    @Deprecated
    void setCache(FOV cache);

}
