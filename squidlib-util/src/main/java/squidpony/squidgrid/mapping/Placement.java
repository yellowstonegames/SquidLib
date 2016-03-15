package squidpony.squidgrid.mapping;

import squidpony.squidmath.Coord;

import java.util.Collections;
import java.util.LinkedHashSet;

import static squidpony.squidmath.CoordPacker.*;

/**
 * Utility class for finding areas where game-specific terrain features might be suitable to place.
 * Example placement for alongStraightWalls, using all regions where there's an extended straight wall in a room to
 * place a rack of bows (as curly braces): https://gist.github.com/tommyettinger/2b69a265bd93304f091b
 * Created by Tommy Ettinger on 3/13/2016.
 */
public class Placement {

    /**
     * The RoomFinder this uses internally to find placement areas only where they are appropriate.
     */
    public RoomFinder finder;

    private short[] allRooms = ALL_WALL, allCaves = ALL_WALL, allCorridors = ALL_WALL, working, nonRoom;

    private LinkedHashSet<LinkedHashSet<Coord>> alongStraightWalls = null,
            corners = null;

    private Placement()
    {

    }

    /**
     * Constructs a Placement using the given RoomFinder, which will have collections of rooms, corridors, and caves.
     * A common use case for this class involves the Placement field that is constructed in a SectionDungeonGenerator
     * when generate() or generateRespectingStairs() in that class is called; if you use SectionDungeonGenerator, there
     * isn't much need for this constructor, since you can normally use the one created as a field in that class.
     * @param finder a RoomFinder that must not be null.
     */
    public Placement(RoomFinder finder)
    {
        if(finder == null)
            throw new UnsupportedOperationException("RoomFinder passed to Placement constructor cannot be null");

        this.finder = finder;

        for(short[] region : finder.rooms.keys())
        {
            allRooms = unionPacked(allRooms, region);
        }
        for(short[] region : finder.caves.keys())
        {
            allCaves = unionPacked(allCaves, region);
        }
        for(short[] region : finder.corridors.keys())
        {
            allCorridors = unionPacked(allCorridors, region);
        }
        nonRoom = expand(unionPacked(allCorridors, allCaves), 2, finder.width, finder.height, false);
    }

    /**
     * Gets a LinkedHashSet of LinkedHashSet of Coord, where each inner LinkedHashSet of Coord refers to a placement
     * region along a straight wall with length 3 or more, not including corners. Each Coord refers to a single cell
     * along the straight wall. This could be useful for placing weapon racks in armories, chalkboards in schoolrooms
     * (tutorial missions, perhaps?), or even large paintings/murals in palaces.
     * @return a set of sets of Coord where each set of Coord is a wall's viable placement for long things along it
     */
    public LinkedHashSet<LinkedHashSet<Coord>> getAlongStraightWalls() {
        if(alongStraightWalls == null)
        {
            alongStraightWalls = new LinkedHashSet<>(32);
            for(short[] region : finder.rooms.keys()) {
                working =
                        differencePacked(
                                fringe(
                                        retract(region, 1, finder.width, finder.height, false),
                                        1, finder.width, finder.height, false),
                                nonRoom);
                for (short[] sp : split(working)) {
                    if (count(sp) >= 3)
                        alongStraightWalls.add(arrayToSet(allPacked(sp)));
                }
            }

        }
        return alongStraightWalls;
    }

    /**
     * Gets a LinkedHashSet of LinkedHashSet of Coord, where each inner LinkedHashSet of Coord refers to a room's
     * corners, and each Coord is one of those corners. There are more uses for corner placement than I can list. This
     * doesn't always identify all corners, since it only finds ones in rooms, and a cave too close to a corner can
     * cause that corner to be ignored.
     * @return a set of sets of Coord where each set of Coord is a wall's viable placement for long things along it
     */
    public LinkedHashSet<LinkedHashSet<Coord>> getCorners() {
        if(corners == null)
        {
            corners = new LinkedHashSet<>(32);
            for(short[] region : finder.rooms.keys()) {
                working =
                        differencePacked(
                                differencePacked(region,
                                        retract(
                                                expand(region, 1, finder.width, finder.height, false),
                                                1, finder.width, finder.height, true)
                                ),
                                nonRoom);
                for(short[] sp : split(working))
                {
                    corners.add(arrayToSet(allPacked(sp)));
                }

            }
        }
        return corners;
    }

    private static LinkedHashSet<Coord> arrayToSet(Coord[] arr)
    {
        LinkedHashSet<Coord> lhs = new LinkedHashSet<>(arr.length);
        Collections.addAll(lhs, arr);
        return lhs;
    }
}
