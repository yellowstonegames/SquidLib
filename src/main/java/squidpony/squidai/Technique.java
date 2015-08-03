package squidpony.squidai;

import squidpony.squidgrid.Radius;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A simple struct-like class that stores various public fields which describe the targeting properties of a skill,
 * spell, tech, or any other game-specific term for a targeted (typically offensive) ability we call a Technique.
 *
 * The typical usage of a Technique is:
 * <ul>
 * <li>Construct any AOE implementation the Technique would use (if the Technique affects multiple grid cells).</li>
 * <li>Construct the Technique (passing the AOE as a parameter if needed).</li>
 * <li>Call setMap() before considering the Technique if it has not been called yet, if the physical map (including
 * doors and destructible objects) has changed since setMap() was last called, or simply on every Technique every time
 * the map changes if there are few enemies with few Techniques. PERFORMING ANY SUBSEQUENT STEPS WITHOUT SETTING THE
 * MAP TO THE CURRENT ACTUAL PHYSICAL MAP WILL HAVE BAD CONSEQUENCES FOR LOGIC AND MAY CAUSE CRASHING BUGS DUE TO
 * ARRAY BOUNDS VIOLATIONS IF YOU HAVEN'T SET THE MAP ARRAY IN THE FIRST PLACE. The map should be bounded by wall chars
 * ('#'), which is done automatically by squidpony.squidgrid.mapping.DungeonGenerator .</li>
 * <li>When the Technique is being considered by an AI, call idealLocations() with the values of targets,
 * lesserTargets, and/or priorityTargets set to beings that the AI can see (likely using FOV) and wants to affect with
 * this Technique (enemies for offensive Techniques, allies for supporting ones), and requiredExclusions typically set
 * to allies for offensive Techniques that can cause friendly-fire damage, or to null for supporting ones or Techniques
 * that don't affect allies.</li>
 * <li>When an ideal location has been determined from the previous step, and the AI decides (possibly randomly, by an
 * aggression ("aggro") level tracked per-enemy, or by weights on Techniques for different situations) to use this
 * Technique on a specific target point, call apply() with the user position as a Point and the chosen Point, and
 * proceed to process the effects of the Technique as fitting for your game on the returned Map of Point keys to Double
 * values that describe the amount of effect (from 0.0 for none to 1.0 for full) that Point receives.</li>
 * </ul>
 *
 * A Technique always has an AOE implementation that it uses to determine which cells it actually affects, and
 * Techniques that do not actually affect an area use the default single-cell "Area of Effect" implementation, PointAOE.
 * You typically will need to construct the implementing class of the AOE interface in a different way for each
 * implementation; BeamAOE, LineAOE, and ConeAOE depend on the user's position, BurstAOE and BlastAOE treat radii
 * differently from BeamAOE and LineAOE, and CloudAOE has a random component that can be given a seed.
 *
 * Every Technique has a Radius enum it uses to measure distance called radiusType; the value given to a Technique for
 * radiusType does not automatically propagate into the possible Radius enums that AOE implementations can have.
 *
 * A Technique may have a Radius enum that is used to limit the cells that are tested for AI, called limitType; this
 * enum may be null if the cells are not given limitations more strict than minRange and maxRange. If it is
 * Radius.DIAMOND or Radius.OCTAHEDRON, then it limits the cells that can be picked as ideal locations to those directly
 * north, east, south, and west (90 degree increments); for any other value of Radius, the cells affected can be in any
 * 45 degree increments, so north, northeast, east, southeast, south, etc.  The cells must be in a straight line.
 *
 * A Technique has a minimum and maximum range that applies to the "target point" of the AOE. It may be desirable to
 * restrict target points to a specific ring of cells, especially for ConeAOE, and this can be done by setting minRange
 * and maxRange to the same value: the distance the ring should have from the user, measured by radiusType.
 *
 * A Technique finally has a String  name, which typically should be in a form that can be presented to a user, and a
 * String id, which defaults to the same value as name but can be given some value not meant for users that records
 * any additional identifying characteristics the game needs for things like comparisons.
 *
 * Created by Tommy Ettinger on 7/27/2015.
 */
public class Technique {
    public String name;
    public String id;
    public int minRange;
    public int maxRange;
    public AOE aoe;
    public Radius radiusType;
    public Radius limitType = null;
    protected char[][] dungeon;
    protected final static Point DEFAULT_POINT = new Point(0, 0);

    /**
     * Creates a Technique that can target a single Point at any range from 1 cell away to range cells away, using
     * Chebyshev (8-way square) distance.
     * @param name An identifier that may be displayed to the user. Also used for id.
     * @param range The maximum range, inclusive, of this Technique. Minimum range is 1.
     */
    public Technique(String name, int range) {
        this.name = name;
        this.id = name;
        this.minRange = 1;
        this.maxRange = range;
        if(this.maxRange < this.minRange) this.maxRange = this.minRange;
        this.aoe = new PointAOE(DEFAULT_POINT);
        this.radiusType = Radius.SQUARE;
    }

    /**
     * Creates a Technique that can target a single Point at any range from minRange cell away to maxRange cells away,
     * using Chebyshev (8-way square) distance.
     * @param name An identifier that may be displayed to the user. Also used for id.
     * @param minRange The minimum range, inclusive, of this Technique.
     * @param maxRange The maximum range, inclusive, of this Technique.
     */
    public Technique(String name, int minRange, int maxRange) {
        this.name = name;
        this.id = name;
        this.minRange = minRange;
        this.maxRange = maxRange;
        if(this.maxRange < this.minRange) this.maxRange = this.minRange;
        this.aoe = new PointAOE(DEFAULT_POINT);
        this.radiusType = Radius.SQUARE;
    }

    /**
     * Creates a Technique that can target a Point at any range from minRange cell away to maxRange cells away,
     * using Chebyshev (8-way square) distance, and use that target Point for the given AOE.
     * @param name An identifier that may be displayed to the user. Also used for id.
     * @param minRange The minimum range, inclusive, of this Technique.
     * @param maxRange The maximum range, inclusive, of this Technique.
     * @param aoe An implementation of the AOE interface; typically needs construction beforehand.
     */
    public Technique(String name, int minRange, int maxRange, AOE aoe) {
        this.name = name;
        this.id = name;
        this.minRange = minRange;
        this.maxRange = maxRange;
        if(this.maxRange < this.minRange) this.maxRange = this.minRange;
        this.aoe = aoe;
        this.radiusType = Radius.SQUARE;
    }

    /**
     * Creates a Technique that can target a Point at any range from minRange cell away to maxRange cells away,
     * using the given radiusType for how to measure distance, and use that target Point for the given AOE.
     * @param name An identifier that may be displayed to the user. Also used for id.
     * @param minRange The minimum range, inclusive, of this Technique.
     * @param maxRange The maximum range, inclusive, of this Technique.
     * @param aoe An implementation of the AOE interface; typically needs construction beforehand.
     * @param radiusType A Radius enum type such as Radius.DIAMOND for Manhattan (4-way diamond) distance. Not used for the AOE, only for measuring distance here.
     */
    public Technique(String name, int minRange, int maxRange, AOE aoe, Radius radiusType) {
        this.name = name;
        this.id = name;
        this.minRange = minRange;
        this.maxRange = maxRange;
        if(this.maxRange < this.minRange) this.maxRange = this.minRange;
        this.aoe = aoe;
        this.radiusType = radiusType;
    }

    /**
     * Creates a Technique that can target a Point at any range from minRange cell away to maxRange cells away,
     * using the given radiusType for how to measure distance, and use that target Point for the given AOE.
     * @param name An identifier that may be displayed to the user.
     * @param id An identifier that should always be internal, and will probably never be shown to the user.
     * @param minRange The minimum range, inclusive, of this Technique.
     * @param maxRange The maximum range, inclusive, of this Technique.
     * @param aoe An implementation of the AOE interface; typically needs construction beforehand.
     * @param radiusType A Radius enum type such as Radius.DIAMOND for Manhattan (4-way diamond) distance. Not used for the AOE, only for measuring distance here.
     */
    public Technique(String name, String id, int minRange, int maxRange, AOE aoe, Radius radiusType) {
        this.name = name;
        this.id = id;
        this.minRange = minRange;
        this.maxRange = maxRange;
        if(this.maxRange < this.minRange) this.maxRange = this.minRange;
        this.aoe = aoe;
        this.radiusType = radiusType;
    }

    /**
     * Creates a Technique that can target a Point at any range from minRange cell away to maxRange cells away,
     * using the given radiusType for how to measure distance, and use that target Point for the given AOE.
     * @param name An identifier that may be displayed to the user.
     * @param id An identifier that should always be internal, and will probably never be shown to the user.
     * @param minRange The minimum range, inclusive, of this Technique.
     * @param maxRange The maximum range, inclusive, of this Technique.
     * @param aoe An implementation of the AOE interface; typically needs construction beforehand.
     * @param radiusType A Radius enum type such as Radius.DIAMOND for Manhattan (4-way diamond) distance. Not used for the AOE, only for measuring distance here.
     * @param limitType A Radius enum that, if non-null, limits the valid ideal cells to straight lines from the user's position.
     * */
    public Technique(String name, String id, int minRange, int maxRange, AOE aoe, Radius radiusType, Radius limitType) {
        this.name = name;
        this.id = id;
        this.minRange = minRange;
        this.maxRange = maxRange;
        if(this.maxRange < this.minRange) this.maxRange = this.minRange;
        this.aoe = aoe;
        this.radiusType = radiusType;
        this.limitType = limitType;
    }

    /**
     * VITAL: Call this method before any calls to idealLocations() or apply(), and call it again if the map changes.
     *
     * This simple method sets the map that this Technique can find targets in to a given char 2D array with '#' for
     * walls and any other character (including characters for open and closed doors) treated as a floor for most
     * purposes (certain AOE implementations may treat open and closed doors differently, specifically any that use
     * FOV internally and can yield values other than 1.0 from their findArea() method, like BlastAOE and ConeAOE).
     * @param map A char 2D array like one generated by squidpony.squidgrid.mapping.DungeonGenerator, with '#' for walls and bounded edges.
     */
    public void setMap(char[][] map)
    {
        this.dungeon = map;
        this.aoe.setMap(map);
    }

    /**
     * Get a mapping of Point keys representing locations to apply this Technique to, to ArrayList of Point values
     * representing which targets (by their location) are effected by choosing that Point. All targets with this method
     * are valued equally, and the ideal location affects as many as possible without hitting any requiredExclusions.
     *
     * YOU MUST CALL setMap() with the current map status at some point before using this method, and call it again if
     * the map changes. Failure to do so can cause serious bugs, from logic errors where monsters consider a door
     * closed when it is open or vice versa, to an ArrayIndexOutOfBoundsException being thrown if the player moved to a
     * differently-sized map and the Technique tries to use the previous map with coordinates from the new one.
     *
     * @param user The location of the user of this Technique
     * @param targets Set of Point of desirable targets to include in the area of this Technique, as many as possible.
     * @param requiredExclusions Set of Point where each value is something this Technique will really try to avoid.
     * @return LinkedHashMap of Point keys representing target points to pass to apply, to ArrayList of Point values representing what targets' locations will be affected.
     */
    public LinkedHashMap<Point, ArrayList<Point>> idealLocations(Point user, Set<Point> targets, Set<Point> requiredExclusions) {
        aoe.limit(user, limitType);
        LinkedHashMap<Point, ArrayList<Point>> area = aoe.idealLocations(targets, requiredExclusions),
                r = new LinkedHashMap<Point, ArrayList<Point>>();
        for (Point shifter : area.keySet())
        {
            double dist = radiusType.radius(user.x, user.y, shifter.x, shifter.y);
            if(dist >= minRange && dist <= maxRange)
                r.put(shifter, area.get(shifter));
        }
        return  r;
    }

    /**
     * Get a mapping of Point keys representing locations to apply this Technique to, to ArrayList of Point values
     * representing which targets (by their location) are effected by choosing that Point. This method will strongly
     * prefer including priorityTargets in its area, especially multiple one if possible, and primarily uses
     * lesserTargets as a tiebreaker if two locations have the same number of included priorityTargets but one has more
     * lesserTargets in its area.
     *
     * YOU MUST CALL setMap() with the current map status at some point before using this method, and call it again if
     * the map changes. Failure to do so can cause serious bugs, from logic errors where monsters consider a door
     * closed when it is open or vice versa, to an ArrayIndexOutOfBoundsException being thrown if the player moved to a
     * differently-sized map and the Technique tries to use the previous map with coordinates from the new one.
     *
     * @param user The location of the user of this Technique
     * @param priorityTargets Set of Point of important targets to include in the area of this Technique, preferring to target a single priorityTarget over four lesserTargets.
     * @param lesserTargets Set of Point of desirable targets to include in the area of this Technique, as many as possible without excluding priorityTargets.
     * @param requiredExclusions Set of Point where each value is something this Technique will really try to avoid.
     * @return LinkedHashMap of Point keys representing target points to pass to apply, to ArrayList of Point values representing what targets' locations will be affected.
     */
    public LinkedHashMap<Point, ArrayList<Point>> idealLocations(Point user, Set<Point> priorityTargets, Set<Point> lesserTargets, Set<Point> requiredExclusions) {
        aoe.limit(user, limitType);
        LinkedHashMap<Point, ArrayList<Point>> area = aoe.idealLocations(priorityTargets, lesserTargets, requiredExclusions),
                r = new LinkedHashMap<Point, ArrayList<Point>>();
        for (Point shifter : area.keySet())
        {
            double dist = radiusType.radius(user.x, user.y, shifter.x, shifter.y);
            if(dist >= minRange && dist <= maxRange)
                r.put(shifter, area.get(shifter));
        }
        return  r;
    }

    /**
     * This does one last validation of the location aimAt (checking that it is within the valid range for this
     * Technique) before getting the area affected by the AOE targeting that cell. It considers the origin of the AOE
     * to be the Point parameter user, for purposes of directional limitations and for AOE implementations that need
     * the user's location, such as ConeAOE and LineAOE.
     *
     * YOU MUST CALL setMap() with the current map status at some point before using this method, and call it again if
     * the map changes. Failure to do so can cause serious bugs, from logic errors where monsters consider a door
     * closed when it is open or vice versa, to an ArrayIndexOutOfBoundsException being thrown if the player moved to a
     * differently-sized map and the Technique tries to use the previous map with coordinates from the new one.
     *
     * @param user The position of the Technique's user, x first, y second.
     * @param aimAt A target Point typically obtained from idealLocations that determines how to position the AOE.
     * @return a HashMap of Point keys to Double values from 1.0 (fully affected) to 0.0 (unaffected).
     */
    public LinkedHashMap<Point, Double> apply(Point user, Point aimAt)
    {
        aoe.limit(user, limitType);
        double dist = radiusType.radius(user.x, user.y, aimAt.x, aimAt.y);
        LinkedHashMap<Point, Double> r = new LinkedHashMap<Point, Double>();
        if(dist >= minRange && dist <= maxRange)
        {
            aoe.shift(aimAt);
            r = aoe.findArea();
        }
        return r;
    }
}