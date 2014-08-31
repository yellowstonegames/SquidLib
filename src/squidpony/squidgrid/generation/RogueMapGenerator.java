package squidpony.squidgrid.generation;

import java.awt.Point;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import squidpony.squidgrid.util.DirectionCardinal;
import squidpony.squidmath.RNG;
import squidpony.squidutility.Pair;

/**
 * This dungeon generator is a port of the rot.js version.
 *
 * @author hyakugei
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
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
        createRooms();
        createCorridors();
        while (!fullyConnected()) {
            System.out.println("Not yet fully connected.");
            createRandomRoomConnections();
        }
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
        int x = rng.nextInt(cellWidth);
        int y = rng.nextInt(cellHeight);

        boolean found;
        List<DirectionCardinal> dirToCheck = new LinkedList<>();
        do {
            dirToCheck.addAll(Arrays.asList(DirectionCardinal.CARDINALS));
            Collections.shuffle(dirToCheck, rng);

            do {
                found = false;
                DirectionCardinal dir = dirToCheck.remove(0);
                int nextX = x + dir.deltaX;
                if (nextX < 0 || nextX >= cellWidth) {
                    continue;
                }
                int nextY = y + dir.deltaY;
                if (nextY < 0 || nextY >= cellHeight) {
                    continue;
                }

                Room room = rooms[x][y];
                Room otherRoom = rooms[nextX][nextY];

                if (room.connections.contains(otherRoom) ) {
                    break;//already connected to this room
                }

                if (!otherRoom.connections.isEmpty()) {
                    otherRoom.connections.add(room);
                    connectedCells.add(otherRoom);
                    x = nextX;
                    y = nextY;
                    found = true;
                }
            } while (!dirToCheck.isEmpty() && found == false);
        } while (!dirToCheck.isEmpty());
    }

    private void connectUnconnectedRooms() {
        Collections.shuffle(connectedCells, rng);

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

                        if (otherRoom.connections.isEmpty()) {
                            break;
                        }

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
                        System.out.println("-- Unable to connect room.");
                    }
                }
            }
        }
    }

    private boolean fullyConnected() {
        boolean[][] marked = new boolean[width][height];
        int startx = 0, starty = 0;

        findStart:
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (map[x][y] != Terrain.WALL) {
                    startx = x;
                    starty = y;
                    break findStart;
                }
            }
        }

        Deque<Point> points = new LinkedList<>();
        points.offer(new Point(startx, starty));
        while (!points.isEmpty()) {
            Point p = points.pop();
            if (marked[p.x][p.y]) {
                continue;//already been here
            }
            marked[p.x][p.y] = true;
            for (DirectionCardinal dir : DirectionCardinal.CARDINALS) {
                int dx = p.x + dir.deltaX;
                int dy = p.y + dir.deltaY;
                if (dx < 0 || dx >= width || dy < 0 || dy >= height) {
                    continue;
                }

                if (!marked[dx][dy] && map[dx][dy] != Terrain.WALL) {
                    points.offer(new Point(dx, dy));
                }
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (map[x][y] != Terrain.WALL && !marked[x][y]) {
                    return false;
                }
            }
        }
        return true;//all tests passed
    }

    private void createRandomRoomConnections() {
        //find a random open spot
        int x = 0, y = 0;
        while (map[x][y] != Terrain.FLOOR) {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
        }
        Point start = new Point(x, y);

        x = 0;
        y = 0;
        while (map[x][y] != Terrain.FLOOR) {
            x = rng.nextInt(width);
            y = rng.nextInt(height);
        }
        Point end = new Point(x, y);
        drawCorridor(start, end);
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

    private Point getWallPosition(Room room, DirectionCardinal dir) {
        int rx, ry;
        Point p = null;

        switch (dir) {
            case LEFT:
            case RIGHT:
                rx = rng.between(room.x + 1, room.x + room.width - 1);
                if (dir == DirectionCardinal.LEFT) {
                    ry = room.y - 2;
                } else {
                    ry = room.y + room.height + 1;
                }
                map[rx][ry - dir.deltaX] = Terrain.DOOR;
                p = new Point(rx, ry);
                break;
            case UP:
            case DOWN:
                ry = rng.between(room.y + 1, room.y + room.height - 1);
                if (dir == DirectionCardinal.DOWN) {
                    rx = room.x + room.width + 1;
                } else {
                    rx = room.x - 2;
                }
                map[rx - dir.deltaY][ry] = Terrain.DOOR;
                p = new Point(rx, ry);
                break;
        }

        return p;
    }

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
                    DirectionCardinal dir = DirectionCardinal.getDirection(room.cellx - otherRoom.cellx, room.celly - otherRoom.celly);
                    drawCorridor(getWallPosition(room, dir), getWallPosition(otherRoom, dir.opposite()));
                }
            }
        }
    }
}
