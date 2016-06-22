package squidpony.squidgrid.mapping;

import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;

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

    private short[] allRooms = ALL_WALL, allCaves = ALL_WALL, allCorridors = ALL_WALL, allFloors = ALL_WALL,
            nonRoom;
    private OrderedSet<OrderedSet<Coord>> alongStraightWalls = null,
            corners = null, centers = null;
    private OrderedSet<Coord> hidingPlaces = null;
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
        allFloors = unionPacked(unionPacked(allRooms, allCorridors), allCaves);
        nonRoom = expand(unionPacked(allCorridors, allCaves), 2, finder.width, finder.height, false);
    }

    /**
     * Gets an OrderedSet of OrderedSet of Coord, where each inner OrderedSet of Coord refers to a placement
     * region along a straight wall with length 3 or more, not including corners. Each Coord refers to a single cell
     * along the straight wall. This could be useful for placing weapon racks in armories, chalkboards in schoolrooms
     * (tutorial missions, perhaps?), or even large paintings/murals in palaces.
     * @return a set of sets of Coord where each set of Coord is a wall's viable placement for long things along it
     */
    public OrderedSet<OrderedSet<Coord>> getAlongStraightWalls() {
        if(alongStraightWalls == null)
        {
            alongStraightWalls = new OrderedSet<>(32);
            short[] working;
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
     * Gets an OrderedSet of OrderedSet of Coord, where each inner OrderedSet of Coord refers to a room's
     * corners, and each Coord is one of those corners. There are more uses for corner placement than I can list. This
     * doesn't always identify all corners, since it only finds ones in rooms, and a cave too close to a corner can
     * cause that corner to be ignored.
     * @return a set of sets of Coord where each set of Coord is a room's corners
     */
    public OrderedSet<OrderedSet<Coord>> getCorners() {
        if(corners == null)
        {
            corners = new OrderedSet<>(32);
            short[] working;
            for(short[] region : finder.rooms.keys()) {
                working =
                        differencePacked(
                                differencePacked(region,
                                        retract(
                                                expand(region, 1, finder.width, finder.height, false),
                                                1, finder.width, finder.height, true)
                                ),
                                nonRoom);
                for(Coord c : allPacked(working))
                {
                    OrderedSet<Coord> lhs = new OrderedSet<Coord>();
                    lhs.add(c);
                    corners.add(lhs);
                }

            }
        }
        return corners;
    }
    /**
     * Gets an OrderedSet of OrderedSet of Coord, where each inner OrderedSet of Coord refers to a room's cells
     * that are furthest from the walls, and each Coord is one of those central positions. There are many uses for this,
     * like finding a position to place a throne or shrine in a large room where it should be visible from all around.
     * This doesn't always identify all centers, since it only finds ones in rooms, and it can also find multiple
     * central points if they are all the same distance from a wall (common in something like a 3x7 room, where it will
     * find a 1x5 column as the centers of that room).
     * @return a set of sets of Coord where each set of Coord contains a room's cells that are furthest from the walls.
     */
    public OrderedSet<OrderedSet<Coord>> getCenters() {
        if(centers == null)
        {
            centers = new OrderedSet<>(32);
            short[] working, working2;
            for(short[] region : finder.rooms.keys()) {

                working = null;
                working2 = retract(region, 1, finder.width, finder.height, false);
                for (int i = 2; i < 7; i++) {
                    if(count(working2) <= 0)
                        break;
                    working = working2;
                    working2 = retract(region, i, finder.width, finder.height, false);
                }
                if(working == null)
                    continue;

                //working =
                //        differencePacked(
                //                working,
                //                nonRoom);
                centers.add(arrayToSet(allPacked(working)));

            }
        }
        return centers;
    }

    /**
     * Gets an OrderedSet of Coord, where each Coord is hidden (using the given radiusStrategy and range for FOV
     * calculations) from any doorways or similar narrow choke-points where a character might be easily ambushed. If
     * multiple choke-points can see a cell (using shadow-casting FOV, which is asymmetrical), then the cell is very
     * unlikely to be included in the returned Coords, but if a cell is visible from one or no choke-points and is far
     * enough away, then it is more likely to be included.
     * @param radiusStrategy a Radius object that will be used to determine visibility.
     * @param range the minimum distance things are expected to hide at; often related to player FOV range
     * @return a Set of Coord where each Coord is either far away from or is concealed from a door-like area
     */
    public OrderedSet<Coord> getHidingPlaces(Radius radiusStrategy, int range) {
        if(hidingPlaces == null)
        {
            double[][] composite = new double[finder.width][finder.height],
                    resMap = DungeonUtility.generateResistances(finder.map),
                    temp;
            FOV fov = new FOV(FOV.SHADOW);
            Coord pt;
            for (int d = 0; d < finder.connections.length; d++) {
                pt = finder.connections[d];
                temp = fov.calculateFOV(resMap, pt.x, pt.y, range, radiusStrategy);
                for (int x = 0; x < finder.width; x++) {
                    for (int y = 0; y < finder.height; y++) {
                        composite[x][y] += temp[x][y] * temp[x][y];
                    }
                }
            }
            hidingPlaces = arrayToSet(allPacked(intersectPacked(allFloors, pack(composite, 0.25))));
        }
        return hidingPlaces;
    }

    private static OrderedSet<Coord> arrayToSet(Coord[] arr)
    {
        return new OrderedSet<> (arr);
    }
}
