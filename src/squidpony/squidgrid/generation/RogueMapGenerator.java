package squidpony.squidgrid.generation;

import java.awt.Point;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import squidpony.annotation.Beta;
import squidpony.squidgrid.util.DirectionCardinal;
import squidpony.squidmath.RNG;
import squidpony.squidutility.Pair;

/**
 * This dungeon generator is based on a port of the rot.js version.
 *
 * @author hyakugei
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class RogueMapGenerator {

    public class Room {

        public int x, y, width, height, cellx, celly;
        public List<Room> connections = new LinkedList<>();

        public Room(int x, int y, int width, int height, int cellx, int celly) {
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
            final Room other = (Room) obj;
            if (this.cellx != other.cellx) {
                return false;
            }
            if (this.celly != other.celly) {
                return false;
            }
            return true;
        }
    }

    public enum Terrain {

        FLOOR('.'), WALL('#'), DOOR('+');
        public char symbol;

        private Terrain(char symbol) {
            this.symbol = symbol;
        }
    }

    private static final RNG rng = new RNG();

    private int cellWidth, cellHeight, width, height,
            minRoomWidth, maxRoomWidth, minRoomHeight, maxRoomHeight;
    private Room[][] rooms;
    private Terrain[][] map;
    private List<Room> connectedCells = new LinkedList<>();

    public RogueMapGenerator(int cellWidth, int cellHeight, int width, int height, int minRoomWidth, int maxRoomWidth, int minRoomHeight, int maxRoomHeight) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.width = width;
        this.height = height;
        this.minRoomWidth = minRoomWidth;
        this.maxRoomWidth = maxRoomWidth;
        this.minRoomHeight = minRoomHeight;
        this.maxRoomHeight = maxRoomHeight;
    }

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
        rooms = new Room[cellWidth][cellHeight];
        map = new Terrain[width][height];
        for (int x = 0; x < cellWidth; x++) {
            for (int y = 0; y < cellHeight; y++) {
                rooms[x][y] = new Room(0, 0, 0, 0, x, y);
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = Terrain.WALL;
            }
        }
    }

    private void connectRooms() {
        List<Room> unconnected = new LinkedList<>();
        for (int x = 0; x < cellWidth; x++) {
            for (int y = 0; y < cellHeight; y++) {
                unconnected.add(rooms[x][y]);
            }
        }
        Collections.shuffle(unconnected, rng);

        List<DirectionCardinal> dirToCheck;
        for (Room room : unconnected) {
            dirToCheck = Arrays.asList(DirectionCardinal.CARDINALS);
            Collections.shuffle(dirToCheck, rng);
            for (DirectionCardinal dir : dirToCheck) {
                int nextX = room.x + dir.deltaX;
                int nextY = room.y + dir.deltaY;
                if (nextX < 0 || nextX >= cellWidth || nextY < 0 || nextY >= cellHeight) {
                    continue;
                }
                Room otherRoom = rooms[nextX][nextY];

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
        for (int x = 0; x < cellWidth; x++) {
            for (int y = 0; y < cellHeight; y++) {
                Room room = rooms[x][y];

                if (room.connections.isEmpty()) {
                    List<DirectionCardinal> dirToCheck = new LinkedList<>();
                    dirToCheck.addAll(Arrays.asList(DirectionCardinal.CARDINALS));
                    Collections.shuffle(dirToCheck, rng);

                    boolean validRoom = false;
                    Room otherRoom = null;

                    do {
                        DirectionCardinal dir = dirToCheck.remove(0);

                        int nextX = x + dir.deltaX;
                        if (nextX < 0 || nextX >= cellWidth) {
                            continue;
                        }
                        int nextY = y + dir.deltaY;
                        if (nextY < 0 || nextY >= cellHeight) {
                            continue;
                        }

                        otherRoom = rooms[nextX][nextY];
                        validRoom = true;

//                        if (otherRoom.connections.isEmpty()) {
//                            break;
//                        }
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
                    } else {
//                        System.out.println("-- Unable to connect room " + room.cellx + ", " + room.celly);
                    }
                }
            }
        }
    }

    private void fullyConnect() {
        boolean[][] marked = new boolean[cellWidth][cellHeight];
        Deque<Room> deq = new LinkedList<>();
        for (int x = 0; x < cellWidth; x++) {
            for (int y = 0; y < cellHeight; y++) {
                deq.offer(rooms[x][y]);
            }
        }
        Deque<Room> connected = new LinkedList<>();
        connected.add(deq.pop());
        boolean changed = true;
        testing:
        while (changed) {
            changed = false;
            for (Room test : deq) {
                for (Room r : connected) {
                    if (test.connections.contains(r) || r.connections.contains(test)) {
                        connected.offer(test);
                        deq.remove(test);
                        changed = true;
                        continue testing;
                    }
                }
            }
        }

        boolean allGood = true;
        if (!deq.isEmpty()) {
//            System.out.println("Disconnected: " + deq.size());
            testing:
            for (Room room : deq) {
                for (DirectionCardinal dir : DirectionCardinal.CARDINALS) {
                    int x = room.cellx + dir.deltaX;
                    int y = room.celly + dir.deltaY;
                    if (x >= 0 && y >= 0 && x < cellWidth && y < cellHeight) {
                        Room otherRoom = rooms[x][y];
                        if (connected.contains(otherRoom)) {
                            room.connections.add(otherRoom);
                            allGood = false;
                            break testing;
                        }
                    }
                }
            }
        }
        
        if (!allGood){
            fullyConnect();
        }
    }

    private void createRooms() {
        int cwp = width / cellWidth;
        int chp = height / cellHeight;

        Room otherRoom;

        for (int x = 0; x < cellWidth; x++) {
            for (int y = 0; y < cellHeight; y++) {
                int sx = cwp * x;
                int sy = chp * y;

                sx = Integer.max(sx, 2);
                sy = Integer.max(sy, 2);

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

                while (sx + sxOffset + roomw >= width) {
                    if (sxOffset > 0) {
                        sxOffset--;
                    } else {
                        roomw--;
                    }
                }
                while (sy + syOffset + roomh >= height) {
                    if (syOffset > 0) {
                        syOffset--;
                    } else {
                        roomh--;
                    }
                }

                sx += sxOffset;
                sy += syOffset;

                Room r = rooms[x][y];
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

    /**
     * Returns a random position on the wall of the room in the given direction.
     *
     * @param room
     * @param dir
     * @return
     */
    private Point getWallPosition(Room room, DirectionCardinal dir) {
        int x, y;
        Point p = null;

        switch (dir) {
            case LEFT:
                y = rng.between(room.y + 1, room.y + room.height);
                x = room.x - 1;
                map[x][y] = Terrain.DOOR;
                p = new Point(x - 1, y);
                break;
            case RIGHT:
                y = rng.between(room.y + 1, room.y + room.height);
                x = room.x + room.width;
                map[x][y] = Terrain.DOOR;
                p = new Point(x + 1, y);
                break;
            case UP:
                x = rng.between(room.x + 1, room.x + room.width);
                y = room.y - 1;
                map[x][y] = Terrain.DOOR;
                p = new Point(x, y - 1);
                break;
            case DOWN:
                x = rng.between(room.x + 1, room.x + room.width);
                y = room.y + room.height;
                map[x][y] = Terrain.DOOR;
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
    private void drawCorridor(Point start, Point end) {
        int xOffset = end.x - start.x;
        int yOffset = end.y - start.y;
        int xpos = start.x;
        int ypos = start.y;

        List<Pair<DirectionCardinal, Integer>> moves = new LinkedList<>();

        int xAbs = Math.abs(xOffset);
        int yAbs = Math.abs(yOffset);

        double firstHalf = rng.nextDouble();
        double secondHalf = 1 - firstHalf;

        DirectionCardinal xDir = xOffset < 0 ? DirectionCardinal.LEFT : DirectionCardinal.RIGHT;
        DirectionCardinal yDir = yOffset > 0 ? DirectionCardinal.DOWN : DirectionCardinal.UP;

        if (xAbs < yAbs) {
            int tempDist = (int) Math.ceil(yAbs * firstHalf);
            moves.add(new Pair(yDir, tempDist));
            moves.add(new Pair(xDir, xAbs));
            tempDist = (int) Math.floor(yAbs * secondHalf);
            moves.add(new Pair(yDir, tempDist));
        } else {
            int tempDist = (int) Math.ceil(xAbs * firstHalf);
            moves.add(new Pair(xDir, tempDist));
            moves.add(new Pair(yDir, yAbs));
            tempDist = (int) Math.floor(xAbs * secondHalf);
            moves.add(new Pair(xDir, tempDist));
        }

        map[xpos][ypos] = Terrain.FLOOR;

        while (!moves.isEmpty()) {
            Pair<DirectionCardinal, Integer> move = moves.remove(0);
            DirectionCardinal dir = move.getFirst();
            int dist = move.getSecond();
            while (dist > 0) {
                xpos += dir.deltaX;
                ypos += dir.deltaY;
                map[xpos][ypos] = Terrain.FLOOR;
                dist--;
            }
        }
    }

    private void createCorridors() {
        for (int x = 0; x < cellWidth; x++) {
            for (int y = 0; y < cellHeight; y++) {
                Room room = rooms[x][y];
                for (Room otherRoom : room.connections) {
                    DirectionCardinal dir = DirectionCardinal.getDirection(otherRoom.cellx - room.cellx, otherRoom.celly - room.celly);
                    drawCorridor(getWallPosition(room, dir), getWallPosition(otherRoom, dir.opposite()));
                }
            }
        }
    }
}
