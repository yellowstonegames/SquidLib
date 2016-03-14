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

    public RoomFinder finder;

    private short[] allRooms = ALL_WALL, allCaves = ALL_WALL, allCorridors = ALL_WALL, working, nonRoom;

    private LinkedHashSet<LinkedHashSet<Coord>> alongStraightWalls = null,
            corners = null;

    private Placement()
    {

    }
    public Placement(RoomFinder finder)
    {
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
