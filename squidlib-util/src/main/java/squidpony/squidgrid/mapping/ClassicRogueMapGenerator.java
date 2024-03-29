/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package squidpony.squidgrid.mapping;


import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.IRNG;
import squidpony.squidmath.OrderedSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

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
public class ClassicRogueMapGenerator implements IDungeonGenerator{

    /**
     * Holds the information needed to track rooms in the classic rogue
     * generation algorithm.
     */
    private static class ClassicRogueRoom {

        private int x;
        private int y;
        private int width;
        private int height;
        private final int cellx;
        private final int celly;
        private final OrderedSet<ClassicRogueRoom> connections = new OrderedSet<>(4);
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
            return Coord.rosenbergStrongHashCode(cellx, celly);
            // the actual algorithm doesn't matter much here.
            // another good option, if you don't feel like looking up rosenbergStrongHashCode(), is
            //return (int)(0xC13FA9A902A6328FL * cellx + 0x91E10DA5C79E7B1DL * celly >>> 32);
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
            if (cellx != other.cellx) {
                return false;
            }
            return celly == other.celly;
        }
    }

    private final IRNG rng;

    private final int horizontalRooms;
    private final int verticalRooms;
    private final int dungeonWidth;
    private final int dungeonHeight;
    private int minRoomWidth;
    private int maxRoomWidth;
    private int minRoomHeight;
    private int maxRoomHeight;
    private ClassicRogueRoom[][] rooms;
    private char[][] dungeon;
    private final Direction[] dirToCheck = new Direction[4];
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
    public ClassicRogueMapGenerator(int horizontalRooms, int verticalRooms, int dungeonWidth, int dungeonHeight,
                                    int minRoomWidth, int maxRoomWidth, int minRoomHeight, int maxRoomHeight) {
        this(horizontalRooms, verticalRooms, dungeonWidth, dungeonHeight, minRoomWidth, maxRoomWidth, minRoomHeight, maxRoomHeight, new GWTRNG());
    }

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
    public ClassicRogueMapGenerator(int horizontalRooms, int verticalRooms, int dungeonWidth, int dungeonHeight,
                                    int minRoomWidth, int maxRoomWidth, int minRoomHeight, int maxRoomHeight, IRNG rng)
    {
        this.rng = rng;
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

        test = (dungeonHeight - 3 * verticalRooms) / verticalRooms;//have to leave space for hallways
        maxRoomHeight = Math.min(test, maxRoomHeight);
        minRoomHeight = Math.max(minRoomHeight, 2);
        minRoomHeight = Math.min(minRoomHeight, maxRoomHeight);
    }

    /**
     * Builds and returns a map in the Classic Rogue style, returned as a 2D char array.
     * <br>
     * Only includes rooms ('.' for floor and '#' for walls), corridors (using the same chars as rooms) and doors ('+'
     * for closed doors, does not generate open doors).
     * @return a 2D char array version of the map
     */
    @Override
    public char[][] generate()
    {
        initRooms();
        connectRooms();
        connectUnconnectedRooms();
        fullyConnect();
        createRooms();
        createCorridors();
        return dungeon;
    }

    @Override
    public char[][] getDungeon() {
        return dungeon;
    }

    private void initRooms() {
        rooms = new ClassicRogueRoom[horizontalRooms][verticalRooms];
        dungeon = new char[dungeonWidth][dungeonHeight];
        for (int x = 0; x < horizontalRooms; x++) {
            for (int y = 0; y < verticalRooms; y++) {
                rooms[x][y] = new ClassicRogueRoom(0, 0, 0, 0, x, y);
            }
        }
        for (int x = 0; x < dungeonWidth; x++) {
            for (int y = 0; y < dungeonHeight; y++) {
                dungeon[x][y] = '#';
            }
        }
    }

    private void connectRooms() {
        ArrayList<ClassicRogueRoom> unconnected = new ArrayList<>();
        for (int x = 0; x < horizontalRooms; x++) {
            unconnected.addAll(Arrays.asList(rooms[x]).subList(0, verticalRooms));
        }
        rng.shuffleInPlace(unconnected);

        for (int i = 0; i < unconnected.size(); i++) {
            ClassicRogueRoom room = unconnected.get(i);
            rng.shuffle(Direction.CARDINALS, dirToCheck);
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
                    rng.shuffle(Direction.CARDINALS, dirToCheck);

                    boolean validRoom = false;
                    ClassicRogueRoom otherRoom = null;
                    int idx = 0;
                    do {
                        Direction dir = dirToCheck[idx++];

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
                        }
                        else {
                            break;
                        }
                    } while (idx < 4);

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
            ArrayDeque<ClassicRogueRoom> deq = new ArrayDeque<>(horizontalRooms * verticalRooms);
            for (int x = 0; x < horizontalRooms; x++) {
                for (int y = 0; y < verticalRooms; y++) {
                    deq.offer(rooms[x][y]);
                }
            }
            ArrayList<ClassicRogueRoom> connected = new ArrayList<>();
            connected.add(deq.removeFirst());
            boolean changed = true;
            TESTING:
            while (changed) {
                changed = false;
                for (ClassicRogueRoom test : deq) {
                    for (int i = 0, connectedSize = connected.size(); i < connectedSize; i++) {
                        ClassicRogueRoom r = connected.get(i);
                        if (test.connections.contains(r) || r.connections.contains(test)) {
                            connected.add(test);
                            deq.remove(test);
                            changed = true;
                            continue TESTING;
                        }
                    }
                }
            }

            allGood = true;
            if (!deq.isEmpty()) {
                TESTING:
                for (ClassicRogueRoom room : deq) {
                    for (Direction dir : Direction.CARDINALS) {
                        int x = room.cellx + dir.deltaX;
                        int y = room.celly + dir.deltaY;
                        if (x >= 0 && y >= 0 && x < horizontalRooms && y < verticalRooms) {
                            ClassicRogueRoom otherRoom = rooms[x][y];
                            if (connected.contains(otherRoom)) {
                                room.connections.add(otherRoom);
                                allGood = false;
                                break TESTING;
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

                int sxOffset = Math.round(rng.nextInt(cwp - roomw) * 0.5f);
                int syOffset = Math.round(rng.nextInt(chp - roomh) * 0.5f);

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
                        dungeon[xx][yy] = '.';
                    }
                }
            }
        }
    }

    private Coord randomWallPosition(ClassicRogueRoom room, Direction dir) {
        int x, y;
        Coord p = null;

        switch (dir) {
            case LEFT:
                y = rng.between(room.y + 1, room.y + room.height);
                x = room.x - 1;
                dungeon[x][y] = '+';
                p = Coord.get(x - 1, y);
                break;
            case RIGHT:
                y = rng.between(room.y + 1, room.y + room.height);
                x = room.x + room.width;
                dungeon[x][y] = '+';
                p = Coord.get(x + 1, y);
                break;
            case DOWN:
                x = rng.between(room.x + 1, room.x + room.width);
                y = room.y + room.height;
                dungeon[x][y] = '+';
                p = Coord.get(x, y + 1);
                break;
            case UP:
                x = rng.between(room.x + 1, room.x + room.width);
                y = room.y - 1;
                dungeon[x][y] = '+';
                p = Coord.get(x, y - 1);
                break;
        case NONE:
        	break;
		case DOWN_LEFT:
		case DOWN_RIGHT:
		case UP_LEFT:
		case UP_RIGHT:
			throw new IllegalStateException("There should only be cardinal positions here");
        }

        return p;
    }

    /**
     * Draws a corridor between the two points with a zig-zag in between.
     *
     * @param start
     * @param end
     */
    private void digPath(Coord start, Coord end) {
        int xOffset = end.x - start.x;
        int yOffset = end.y - start.y;
        int xpos = start.x;
        int ypos = start.y;

        ArrayDeque<Magnitude> moves = new ArrayDeque<>();

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

        dungeon[xpos][ypos] = '.';

        while (!moves.isEmpty()) {
            Magnitude move = moves.removeFirst();
            Direction dir = move.dir;
            int dist = move.distance;
            while (dist > 0) {
                xpos += dir.deltaX;
                ypos += dir.deltaY;
                dungeon[xpos][ypos] = '.';
                dist--;
            }
        }
    }

    private void createCorridors() {
        for (int x = 0; x < horizontalRooms; x++) {
            for (int y = 0; y < verticalRooms; y++) {
                ClassicRogueRoom room = rooms[x][y];
                for (int r = 0; r < room.connections.size(); r++) {
                    ClassicRogueRoom otherRoom = room.connections.getAt(r);
                    Direction dir = Direction.getCardinalDirection(otherRoom.cellx - room.cellx, otherRoom.celly - room.celly);
                    digPath(randomWallPosition(room, dir), randomWallPosition(otherRoom, dir.opposite()));
                }
            }
        }
    }

    private static class Magnitude {

        public Direction dir;
        public int distance;

        public Magnitude(Direction dir, int distance) {
            this.dir = dir;
            this.distance = distance;
        }
    }
}
