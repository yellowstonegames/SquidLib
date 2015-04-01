package squidpony.squidgrid.mapping;

import java.awt.Point;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.RNG;

/**
 * Creates a dungeon in the style of the original Rogue game. It will always
 * make a grid style of rooms where there are a certain number horizontally and
 * vertically and it will link them only next to each other.
 *
 * This dungeon generator is based on a port of the rot.js version.
 *
 * @author hyakugei
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class ClassicRogueMapGenerator {

    /**
     * Holds the information needed to track rooms in the classic rogue
     * generation algorithm.
     */
    private class ClassicRogueRoom {

        private int x, y, width, height, cellx, celly;
        private final List<ClassicRogueRoom> connections = new LinkedList<>();

        ClassicRogueRoom(int x, int y, int width, int height, int cellx, int celly) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.cellx = cellx;
            this.celly = celly;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + this.cellx;
            hash = 89 * hash + this.celly;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ClassicRogueRoom other = (ClassicRogueRoom) obj;
            if (this.cellx != other.cellx) {
                return false;
            }
            if (this.celly != other.celly) {
                return false;
            }
            return true;
        }
    }

    private static final RNG rng = new RNG();

    private int horizontalRooms, verticalRooms, dungeonWidth, dungeonHeight,
            minRoomWidth, maxRoomWidth, minRoomHeight, maxRoomHeight;
    private ClassicRogueRoom[][] rooms;
    private Terrain[][] map;

    /**
     * Initializes the generator to turn out random dungeons within the specific
     * parameters.
     *
     * Will size down the room width and height parameters if needed to ensure
     * the desired number of rooms will fit both horizontally and vertically.
     *
     * @param horizontalRooms How many rooms will be made horizontally
     * @param verticalRooms How many rooms will be made vertically
     * @param dungeonWidth How wide the total dungeon will be
     * @param dungeonHeight How high the total dungeon will be
     * @param minRoomWidth The minimum width a room can be
     * @param maxRoomWidth The maximum width a room can be
     * @param minRoomHeight The minimum height a room can be
     * @param maxRoomHeight The maximum height a room can be
     */
    public ClassicRogueMapGenerator(int horizontalRooms, int verticalRooms, int dungeonWidth, int dungeonHeight, int minRoomWidth, int maxRoomWidth, int minRoomHeight, int maxRoomHeight) {
        this.horizontalRooms = horizontalRooms;
        this.verticalRooms = verticalRooms;
        this.dungeonWidth = dungeonWidth;
        this.dungeonHeight = dungeonHeight;
        this.minRoomWidth = minRoomWidth;
        this.maxRoomWidth = maxRoomWidth;
        this.minRoomHeight = minRoomHeight;
        this.maxRoomHeight = maxRoomHeight;

        sanitizeRoomDimensions();
    }

    private void sanitizeRoomDimensions() {
        int test = (dungeonWidth - 3 * horizontalRooms) / horizontalRooms;//have to leave space for hallways
        maxRoomWidth = Math.min(test, maxRoomWidth);
        minRoomWidth = Math.max(minRoomWidth, 2);
        minRoomWidth = Math.min(minRoomWidth, maxRoomWidth);

        test = (dungeonHeight - 3 * verticalRooms) / (verticalRooms);//have to leave space for hallways
        maxRoomHeight = Math.min(test, maxRoomHeight);
        minRoomHeight = Math.max(minRoomHeight, 2);
        minRoomHeight = Math.min(minRoomHeight, maxRoomHeight);
    }

    /**
     * Builds and returns a map in the Classic Rogue style.
     *
     * Only includes rooms, corridors and doors.
     *
     * @return
     */
    public Terrain[][] create() {
        initRooms();
        connectRooms();
        connectUnconnectedRooms();
        fullyConnect();
        createRooms();
        createCorridors();
        return map;
    }

    private void initRooms() {
        rooms = new ClassicRogueRoom[horizontalRooms][verticalRooms];
        map = new Terrain[dungeonWidth][dungeonHeight];
        for (int x = 0; x < horizontalRooms; x++) {
            for (int y = 0; y < verticalRooms; y++) {
                rooms[x][y] = new ClassicRogueRoom(0, 0, 0, 0, x, y);
            }
        }
        for (int x = 0; x < dungeonWidth; x++) {
            for (int y = 0; y < dungeonHeight; y++) {
                map[x][y] = Terrain.WALL;
            }
        }
    }

    private void connectRooms() {
        List<ClassicRogueRoom> unconnected = new LinkedList<>();
        for (int x = 0; x < horizontalRooms; x++) {
            for (int y = 0; y < verticalRooms; y++) {
                unconnected.add(rooms[x][y]);
            }
        }
        Collections.shuffle(unconnected, rng.asRandom());

        List<Direction> dirToCheck;
        for (ClassicRogueRoom room : unconnected) {
            dirToCheck = Arrays.asList(Direction.CARDINALS);
            Collections.shuffle(dirToCheck, rng.asRandom());
            for (Direction dir : dirToCheck) {
                int nextX = room.x + dir.deltaX;
                int nextY = room.y + dir.deltaY;
                if (nextX < 0 || nextX >= horizontalRooms || nextY < 0 || nextY >= verticalRooms) {
                    continue;
                }
                ClassicRogueRoom otherRoom = rooms[nextX][nextY];

                if (room.connections.contains(otherRoom)) {
                    break;//already connected to this room
                }

                if (!otherRoom.connections.isEmpty()) {
                    room.connections.add(otherRoom);
                    break;
                }
            }
        }
    }

    private void connectUnconnectedRooms() {
        for (int x = 0; x < horizontalRooms; x++) {
            for (int y = 0; y < verticalRooms; y++) {
                ClassicRogueRoom room = rooms[x][y];

                if (room.connections.isEmpty()) {
                    List<Direction> dirToCheck = new LinkedList<>();
                    dirToCheck.addAll(Arrays.asList(Direction.CARDINALS));
                    Collections.shuffle(dirToCheck, rng.asRandom());

                    boolean validRoom = false;
                    ClassicRogueRoom otherRoom = null;

                    do {
                        Direction dir = dirToCheck.remove(0);

                        int nextX = x + dir.deltaX;
                        if (nextX < 0 || nextX >= horizontalRooms) {
                            continue;
                        }
                        int nextY = y + dir.deltaY;
                        if (nextY < 0 || nextY >= verticalRooms) {
                            continue;
                        }

                        otherRoom = rooms[nextX][nextY];
                        validRoom = true;

                        if (otherRoom.connections.contains(room)) {
                            validRoom = false;
                            continue;
                        }

                        if (validRoom) {
                            break;
                        }

                    } while (!dirToCheck.isEmpty());

                    if (validRoom) {
                        room.connections.add(otherRoom);
                    }
                }
            }
        }
    }

    private void fullyConnect() {
        boolean allGood;
        do {
            Deque<ClassicRogueRoom> deq = new LinkedList<>();
            for (int x = 0; x < horizontalRooms; x++) {
                for (int y = 0; y < verticalRooms; y++) {
                    deq.offer(rooms[x][y]);
                }
            }
            Deque<ClassicRogueRoom> connected = new LinkedList<>();
            connected.add(deq.pop());
            boolean changed = true;
            testing:
            while (changed) {
                changed = false;
                for (ClassicRogueRoom test : deq) {
                    for (ClassicRogueRoom r : connected) {
                        if (test.connections.contains(r) || r.connections.contains(test)) {
                            connected.offer(test);
                            deq.remove(test);
                            changed = true;
                            continue testing;
                        }
                    }
                }
            }

            allGood = true;
            if (!deq.isEmpty()) {
                testing:
                for (ClassicRogueRoom room : deq) {
                    for (Direction dir : Direction.CARDINALS) {
                        int x = room.cellx + dir.deltaX;
                        int y = room.celly + dir.deltaY;
                        if (x >= 0 && y >= 0 && x < horizontalRooms && y < verticalRooms) {
                            ClassicRogueRoom otherRoom = rooms[x][y];
                            if (connected.contains(otherRoom)) {
                                room.connections.add(otherRoom);
                                allGood = false;
                                break testing;
                            }
                        }
                    }
                }
            }

        } while (!allGood);
    }

    private void createRooms() {
        int cwp = dungeonWidth / horizontalRooms;
        int chp = dungeonHeight / verticalRooms;

        ClassicRogueRoom otherRoom;

        for (int x = 0; x < horizontalRooms; x++) {
            for (int y = 0; y < verticalRooms; y++) {
                int sx = cwp * x;
                int sy = chp * y;

                sx = Math.max(sx, 2);
                sy = Math.max(sy, 2);

                int roomw = rng.between(minRoomWidth, maxRoomWidth + 1);
                int roomh = rng.between(minRoomHeight, maxRoomHeight + 1);

                if (y > 0) {
                    otherRoom = rooms[x][y - 1];
                    while (sy - (otherRoom.y + otherRoom.height) < 3) {
                        sy++;
                    }
                }

                if (x > 0) {
                    otherRoom = rooms[x - 1][y];
                    while (sx - (otherRoom.x + otherRoom.width) < 3) {
                        sx++;
                    }
                }

                int sxOffset = Math.round(rng.nextInt(cwp - roomw) / 2);
                int syOffset = Math.round(rng.nextInt(chp - roomh) / 2);

                while (sx + sxOffset + roomw >= dungeonWidth) {
                    if (sxOffset > 0) {
                        sxOffset--;
                    } else {
                        roomw--;
                    }
                }
                while (sy + syOffset + roomh >= dungeonHeight) {
                    if (syOffset > 0) {
                        syOffset--;
                    } else {
                        roomh--;
                    }
                }

                sx += sxOffset;
                sy += syOffset;

                ClassicRogueRoom r = rooms[x][y];
                r.x = sx;
                r.y = sy;
                r.width = roomw;
                r.height = roomh;

                for (int xx = sx; xx < sx + roomw; xx++) {
                    for (int yy = sy; yy < sy + roomh; yy++) {
                        map[xx][yy] = Terrain.FLOOR;
                    }
                }
            }
        }
    }

    private Point randomWallPosition(ClassicRogueRoom room, Direction dir) {
        int x, y;
        Point p = null;

        switch (dir) {
            case LEFT:
                y = rng.between(room.y + 1, room.y + room.height);
                x = room.x - 1;
                map[x][y] = Terrain.CLOSED_DOOR;
                p = new Point(x - 1, y);
                break;
            case RIGHT:
                y = rng.between(room.y + 1, room.y + room.height);
                x = room.x + room.width;
                map[x][y] = Terrain.CLOSED_DOOR;
                p = new Point(x + 1, y);
                break;
            case UP:
                x = rng.between(room.x + 1, room.x + room.width);
                y = room.y - 1;
                map[x][y] = Terrain.CLOSED_DOOR;
                p = new Point(x, y - 1);
                break;
            case DOWN:
                x = rng.between(room.x + 1, room.x + room.width);
                y = room.y + room.height;
                map[x][y] = Terrain.CLOSED_DOOR;
                p = new Point(x, y + 1);
                break;
        }

        return p;
    }

    /**
     * Draws a corridor between the two points with a zig-zag in between.
     *
     * @param start
     * @param end
     */
    private void digPath(Point start, Point end) {
        int xOffset = end.x - start.x;
        int yOffset = end.y - start.y;
        int xpos = start.x;
        int ypos = start.y;

        List<Magnitude> moves = new LinkedList<>();

        int xAbs = Math.abs(xOffset);
        int yAbs = Math.abs(yOffset);

        double firstHalf = rng.nextDouble();
        double secondHalf = 1 - firstHalf;

        Direction xDir = xOffset < 0 ? Direction.LEFT : Direction.RIGHT;
        Direction yDir = yOffset > 0 ? Direction.DOWN : Direction.UP;

        if (xAbs < yAbs) {
            int tempDist = (int) Math.ceil(yAbs * firstHalf);
            moves.add(new Magnitude(yDir, tempDist));
            moves.add(new Magnitude(xDir, xAbs));
            tempDist = (int) Math.floor(yAbs * secondHalf);
            moves.add(new Magnitude(yDir, tempDist));
        } else {
            int tempDist = (int) Math.ceil(xAbs * firstHalf);
            moves.add(new Magnitude(xDir, tempDist));
            moves.add(new Magnitude(yDir, yAbs));
            tempDist = (int) Math.floor(xAbs * secondHalf);
            moves.add(new Magnitude(xDir, tempDist));
        }

        map[xpos][ypos] = Terrain.FLOOR;

        while (!moves.isEmpty()) {
            Magnitude move = moves.remove(0);
            Direction dir = move.dir;
            int dist = move.distance;
            while (dist > 0) {
                xpos += dir.deltaX;
                ypos += dir.deltaY;
                map[xpos][ypos] = Terrain.FLOOR;
                dist--;
            }
        }
    }

    private void createCorridors() {
        for (int x = 0; x < horizontalRooms; x++) {
            for (int y = 0; y < verticalRooms; y++) {
                ClassicRogueRoom room = rooms[x][y];
                for (ClassicRogueRoom otherRoom : room.connections) {
                    Direction dir = Direction.getCardinalDirection(otherRoom.cellx - room.cellx, otherRoom.celly - room.celly);
                    digPath(randomWallPosition(room, dir), randomWallPosition(otherRoom, dir.opposite()));
                }
            }
        }
    }

    private class Magnitude {

        public Direction dir;
        public int distance;

        public Magnitude(Direction dir, int distance) {
            this.dir = dir;
            this.distance = distance;
        }
    }
}
