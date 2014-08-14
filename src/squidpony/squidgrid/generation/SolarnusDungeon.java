package squidpony.squidgrid.generation;

import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Dungeon generation algorithm modified from
 * http://www.roguebasin.com/index.php?title=Java_Example_of_Dungeon-Building_Algorithm
 *
 * Compatible with Java 7
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SolarnusDungeon {

    // misc. messages to print
    String MsgXSize = "X size of dungeon: \t";
    String MsgYSize = "Y size of dungeon: \t";
    String MsgMaxObjects = "max # of objects: \t";
    String MsgNumObjects = "# of objects made: \t";

    // size of the map
    int xsize;
    int ysize;

    // number of "objects" to generate on the map
    int objects;

    // define the %chance to generate either a room or a corridor on the map
    // BTW, rooms are 1st priority so actually it's enough to just define the chance
    // of generating a room
    int ChanceRoom = 75;

    // our map
    Tile[][] dungeonMap = {};

    Random rand = new Random();

    public enum Direction {

        North(0, 1), East(1, 0), South(0, -1), West(-1, 0);
        public int deltaX, deltaY;

        Direction(int deltaX, int deltaY) {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
        }
    }

    public enum Tile {

        Unused(' '), DirtWall('X'), DirtFloor('.'), StoneWall('#'), Corridor('c'), Door('+'), Upstairs('<'), Downstairs('>'), Chest('C');
        public char symbol;

        Tile(char symbol) {
            this.symbol = symbol;
        }
    }

    public int Corridors;

    /**
     * Checks for point being at the edge of a room or the edge of the map.
     *
     * @param centerX
     * @param centerY
     * @param width
     * @param height
     * @param x
     * @param y
     * @return
     */
    public boolean IsWall(int centerX, int centerY, int width, int height, int x, int y) {
        return (x <= Math.max(0, centerX - width / 2) || x >= Math.min(xsize - 1, centerX + width / 2))
                || (y <= Math.max(0, centerY - height / 2) || y >= Math.min(ysize - 1, centerY + height / 2));
    }

    /**
     * Takes the center of the desired room and expands it out to the width and height desired.
     *
     * @param centerX
     * @param centerY
     * @param width
     * @param height
     * @param dir
     * @return
     */
    public List<Point> roomPoints(int centerX, int centerY, int width, int height, Direction dir) {
        List<Point> ret = new LinkedList<>();
        for (int x = Math.max(0, centerX - width / 2); x <= Math.min(xsize - 1, centerX + (width + 1) / 2); x++) {
            for (int y = Math.max(0, centerY - height / 2); y <= Math.min(ysize - 1, centerY + (height + 1) / 2); y++) {
                ret.add(new Point(x, y));
            }
        }
        return ret;
    }

    /**
     * Gets a random number between the min (inclusive) and max (exclusive).
     *
     * @param min
     * @param max
     * @return
     */
    public int GetRand(int min, int max) {
        return rand.nextInt((max - 1) - min) + min;
    }

    public boolean MakeCorridor(int x, int y, int length, Direction direction) {
        // define the dimensions of the corridor (er.. only the width and height..)
        int len = this.GetRand(2, length);
        Tile Floor = Tile.Corridor;

        int xtemp;
        int ytemp = 0;

        switch (direction) {
            case North:
                // north
                // check if there's enough space for the corridor
                // start with checking it's not out of the boundaries
                if (x < 0 || x > this.xsize) {
                    return false;
                }
                xtemp = x;

                // same thing here, to make sure it's not out of the boundaries
                for (ytemp = y; ytemp > (y - len); ytemp--) {
                    if (ytemp < 0 || ytemp > this.ysize) {
                        return false; // oh boho, it was!
                    }
                    if (dungeonMap[xtemp][ytemp] != Tile.Unused) {
                        return false;
                    }
                }

                // if we're still here, let's start building
                Corridors++;
                for (ytemp = y; ytemp > (y - len); ytemp--) {
                    this.SetCell(xtemp, ytemp, Floor);
                }

                break;

            case East:
                // east
                if (y < 0 || y > this.ysize) {
                    return false;
                }
                ytemp = y;

                for (xtemp = x; xtemp < (x + len); xtemp++) {
                    if (xtemp < 0 || xtemp > this.xsize) {
                        return false;
                    }
                    if (dungeonMap[xtemp][ytemp] != Tile.Unused) {
                        return false;
                    }
                }

                Corridors++;
                for (xtemp = x; xtemp < (x + len); xtemp++) {
                    this.SetCell(xtemp, ytemp, Floor);
                }

                break;

            case South:
                // south
                if (x < 0 || x > this.xsize) {
                    return false;
                }
                xtemp = x;

                for (ytemp = y; ytemp < (y + len); ytemp++) {
                    if (ytemp < 0 || ytemp > this.ysize) {
                        return false;
                    }
                    if (dungeonMap[xtemp][ytemp] != Tile.Unused) {
                        return false;
                    }
                }

                Corridors++;
                for (ytemp = y; ytemp < (y + len); ytemp++) {
                    this.SetCell(xtemp, ytemp, Floor);
                }

                break;
            case West:
                // west
                if (ytemp < 0 || ytemp > this.ysize) {
                    return false;
                }
                ytemp = y;

                for (xtemp = x; xtemp > (x - len); xtemp--) {
                    if (xtemp < 0 || xtemp > this.xsize) {
                        return false;
                    }
                    if (dungeonMap[xtemp][ytemp] != Tile.Unused) {
                        return false;
                    }
                }

                Corridors++;
                for (xtemp = x; xtemp > (x - len); xtemp--) {
                    this.SetCell(xtemp, ytemp, Floor);
                }

                break;
        }

        // woot, we're still here! let's tell the other guys we're done!!
        return true;
    }

    public Map<Direction, Point> GetSurroundingPoints(Point v) {
        Map<Direction, Point> points = new HashMap<>();
        for (Direction dir : Direction.values()) {
            if (InBounds(v.x + dir.deltaX, v.y + dir.deltaY)) {
                points.put(dir, new Point(v.x + dir.deltaX, v.y + dir.deltaY));
            }
        }
        return points;
    }

    public Map<Point, Direction> GetSurroundings(Point v) {
        Map<Point, Direction> ret = new HashMap<>();
        for (Direction dir : Direction.values()) {
            ret.put(new Point(v.x + dir.deltaX, v.y + dir.deltaY), dir);
        }
        return ret;
    }

    public boolean InBounds(int x, int y) {
        return x > 0 && x < xsize && y > 0 && y < ysize;
    }

    public boolean InBounds(Point v) {
        return InBounds(v.x, v.y);
    }

    public boolean MakeRoom(int x, int y, int xlength, int ylength, Direction direction) {
        // define the dimensions of the room, it should be at least 4x4 tiles (2x2 for walking on, the rest is walls)
        int xlen = this.GetRand(4, xlength);
        int ylen = this.GetRand(4, ylength);

        // the tile type it's going to be filled with
        Tile Floor = Tile.DirtFloor;
        Tile Wall = Tile.DirtWall;
        // choose the way it's pointing at

        List<Point> points = roomPoints(x, y, xlen, ylen, direction);

        // Check if there's enough space left for it
        for (Point s : points) {
            if (!InBounds(s) || dungeonMap[s.x][s.y] != Tile.Unused) {
                return false;
            }
        }

        System.out.format("Making room:int x=%d, int y=%d, int xlength=%d, int ylength=%d, int direction=%s",
                x, y, xlength, ylength, direction);

        for (Point p : points) {
            if (IsWall(x, y, xlen, ylen, p.x, p.y)) {
                SetCell(p.x, p.y, Wall);
            } else {
                SetCell(p.x, p.y, Floor);
            }

        }

        // yay, all done
        return true;
    }

    public char GetCellTile(int x, int y) {
        return dungeonMap[x][y].symbol;
    }

    //used to print the map on the screen
    public void ShowDungeon() {
        for (int y = 0; y < ysize; y++) {
            for (int x = 0; x < xsize; x++) {
                System.out.print(GetCellTile(x, y));
            }
            System.out.println("");
        }
    }

    public Direction RandomDirection() {
        return Direction.values()[rand.nextInt(Direction.values().length)];
    }

    //and here's the one generating the whole map
    public boolean CreateDungeon(int inx, int iny, int inobj) {
        this.objects = inobj < 1 ? 10 : inobj;

        // adjust the size of the map, if it's smaller or bigger than the limits
        if (inx < 3) {
            xsize = 3;
        } else {
            xsize = inx;
        }

        if (iny < 3) {
            this.ysize = 3;
        } else {
            this.ysize = iny;
        }

        System.out.println(MsgXSize + xsize);
        System.out.println(MsgYSize + ysize);
        System.out.println(MsgMaxObjects + objects);

        // redefine the map var, so it's adjusted to our new map size
        dungeonMap = new Tile[xsize][ysize];

        // start with making the "standard stuff" on the map
        Initialize();

        /**
         * *****************************************************************************
         * And now the code of the random-map-generation-algorithm begins!
         * *****************************************************************************
         */
        // start with making a room in the middle, which we can start building upon
        MakeRoom(xsize / 2, ysize / 2, 8, 6, RandomDirection());

        // keep count of the number of "objects" we've made
        int currentFeatures = 1; // +1 for the first room we just made

        // then we sart the main loop
        for (int countingTries = 0; countingTries < 1000; countingTries++) {
            // check if we've reached our quota
            if (currentFeatures == this.objects) {
                break;
            }

            // start with a random wall
            int newx = 0;
            int xmod = 0;
            int newy = 0;
            int ymod = 0;
            Direction validTile = null;

            // 1000 chances to find a suitable object (room or corridor)..
            for (int testing = 0; testing < 1000; testing++) {
                newx = GetRand(1, xsize - 1);
                newy = GetRand(1, ysize - 1);

                if (dungeonMap[newx][newy] == Tile.DirtWall || dungeonMap[newx][newy] == Tile.Corridor) {
                    Map<Point, Direction> surroundings = GetSurroundings(new Point(newx, newy));

                    // check if we can reach the place
                    boolean canReach = false;
                    for (Point p : surroundings.keySet()) {
                        if (dungeonMap[p.x][p.y] == Tile.Corridor || dungeonMap[p.x][p.y] == Tile.DirtFloor) {
                            canReach = true;
                            validTile = surroundings.get(p);
                            xmod = validTile.deltaX;
                            ymod = validTile.deltaY;
                            break;
                        }
                    }
                    if (!canReach) {
                        continue;
                    }

                    // check that we haven't got another door nearby, so we won't get a lot of openings besides each other
                    for (Direction dir : Direction.values()) {
                        if (dungeonMap[newx + dir.deltaX][newy + dir.deltaY] == Tile.Door) {
                            validTile = null;
                        }
                    }

                    // if we can, jump out of the loop and continue with the rest
                    if (validTile != null) {
                        break;
                    }
                }
            }

            if (validTile != null) {
                // choose what to build now at our newly found place, and at what direction
                int feature = GetRand(0, 100);
                if (feature <= ChanceRoom) { // a new room
                    if (MakeRoom(newx + xmod, newy + ymod, 8, 6, validTile)) {
                        currentFeatures++; // add to our quota

                        // then we mark the wall opening with a door
                        SetCell(newx, newy, Tile.Door);

                        // clean up infront of the door so we can reach it
                        SetCell(newx + xmod, newy + ymod, Tile.DirtFloor);
                    }
                } else { // new corridor
                    if (MakeCorridor(newx + xmod, newy + ymod, 6, validTile)) {
                        // same thing here, add to the quota and a door
                        currentFeatures++;

                        SetCell(newx, newy, Tile.Door);
                    }
                }
            }
        }

        /**
         * *****************************************************************************
         * All done with the building, let's finish this one off
         * *****************************************************************************
         */
        AddSprinkles();

        // all done with the map generation, tell the user about it and finish
        System.out.println(MsgNumObjects + currentFeatures);

        return true;
    }

    void Initialize() {
        for (int y = 0; y < this.ysize; y++) {
            for (int x = 0; x < this.xsize; x++) {
                // ie, making the borders of unwalkable walls
                if (y == 0 || y == this.ysize - 1 || x == 0 || x == this.xsize - 1) {
                    this.SetCell(x, y, Tile.StoneWall);
                } else {                        // and fill the rest with dirt
                    this.SetCell(x, y, Tile.Unused);
                }
            }
        }
    }

    // setting a tile's type
    void SetCell(int x, int y, Tile celltype) {
        dungeonMap[x][y] = celltype;
    }

    void AddSprinkles() {
        // sprinkle out the bonusstuff (stairs, chests etc.) over the map
        int state = 0; // the state the loop is in, start with the stairs
        while (state != 10) {
            for (int testing = 0; testing < 1000; testing++) {
                int newx = this.GetRand(1, this.xsize - 1);
                int newy = this.GetRand(1, this.ysize - 2);

                // Console.WriteLine("x: " + newx + "\ty: " + newy);
                int ways = Direction.values().length; // from how many directions we can reach the random spot from

                // check if we can reach the spot
                for (Direction dir : Direction.values()) {
                    if (!(dungeonMap[newx + dir.deltaX][newy + dir.deltaY] == Tile.DirtFloor
                            || dungeonMap[newx + dir.deltaX][newy + dir.deltaY] == Tile.Corridor
                            || dungeonMap[newx + dir.deltaX][newy + dir.deltaY] == Tile.Door)) {
                        ways--;
                    }
                }

                if (state == 0) {
                    if (ways == 0) {
                        // we're in state 0, let's place a "upstairs" thing
                        this.SetCell(newx, newy, Tile.Upstairs);
                        state = 1;
                        break;
                    }
                } else if (state == 1) {
                    if (ways == 0) {
                        // state 1, place a "downstairs"
                        this.SetCell(newx, newy, Tile.Downstairs);
                        state = 10;
                        break;
                    }
                }
            }
        }
    }
}
