package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;

import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.IRNG;
import squidpony.squidmath.IntIntOrderedMap;
import squidpony.squidmath.IntVLA;

/**
 * A room placing algorithm developed by Rayvolution for his game Fail To Hero, this was simple to implement but
 * delivers complex connectivity. It is meant to ensure all rooms are connected, but usually not directly, and many
 * routes need to wind throughout the map to get to a goal.
 * <br>
 * <pre>{@code
 * ┌────────────────────────────┐┌────────┐┌────────┐┌────────┐
 * │............................││........││........││........│
 * │............................││........││........││........│
 * │............................││........││........││........│
 * │...┌──────────┐...┌─────┐...││...┌┐...│└────┐...││...┌────┘
 * │...│┌───┐┌────┘...│┌────┘...└┘...││...└────┐│...││...└────┐
 * │...││...││........││.............││........││...││........│
 * │...││...││........││.............││........││...││........│
 * │...││...││........││.......<.....││........││...││........│
 * └───┘│...││...┌────┘│...┌─────────┘└────────┘│...│└────┐...│
 * ┌────┘...││...└────┐│...│┌───────────────────┘...└─────┘...│
 * │........││........││...││.................................│
 * │.......>││........││...││.................................│
 * │........││........││...││.................................│
 * │...┌────┘└────┐...│└───┘│...┌─────────────────────────────┘
 * │...│┌────────┐│...└─────┘...└────┐┌───┐┌────────┐┌────────┐
 * │...││........││..................││...││........││........│
 * │...││........││..................││...││........││........│
 * │...││........││..................││...││........││........│
 * │...││...┌┐...│└────┐...┌─────────┘│...│└────┐...│└────┐...│
 * │...││...││...└─────┘...│┌────────┐│...└────┐│...└─────┘...│
 * │...││...││.............││........││........││.............│
 * │...││...││.............││........││........││.............│
 * │...││...││.............││........││........││.............│
 * │...││...││...┌─────────┘│...┌┐...││...┌────┘│...┌─────┐...│
 * │...└┘...││...└──────────┘...││...└┘...└─────┘...│┌────┘...│
 * │........││..................││..................││........│
 * │........││..................││..................││........│
 * │........││..................││..................││........│
 * └────────┘└──────────────────┘└──────────────────┘└────────┘
 * }</pre>
 * <br>
 * <pre>{@code
 * ┌───────────────┬─┬───────────┬─────┬───┬─────────┬─┬───┬─┐
 * │...............│.│...........│.....│...│.........│.│...│.│
 * │.┌──────────.┌─┘.│.┌──.────┬─┤.┌───┤.│.│.──┐.──┐.│.│.│.│.│
 * │.│...........│.....│.......│.│.│...│.│.....│...│.│...│...│
 * ├─┘.┌────.┌─┐.└─────┘.┌──.│.│.│.│.┌─┘.│.│.──┼───┤.└─┬─┘.│.│
 * │...│.....│.│.........│...│.│...│.│...│.│...│...│...│...│.│
 * │.┌─┴───┬─┘.│.┌──.┌───┤.──┤.│.┌─┤.│.┌─┴─┼─┐.│.│.└───┤.│.└─┤
 * │.│.....│...│.│...│...│...│.│.│.│...│...│.│...│.....│.│...│
 * ├─┤.│.│.│.──┘.│.│.└─┐.├───┤.│.│.│.──┤.│.│.│.──┤.│.│.│.└─┐.│
 * │.│.│.│.......│.│...│.│...│.│.......│.│...│...│.│.│.│...│.│
 * │.│.└─┼────.┌─┘.└───┘.│.│.└─┴─┬─┬──.├─┤.──┴───┼─┤.├─┴─┐.└─┤
 * │.│...│>....│.........│.│.....│.│...│.│.......│.│.│...│...│
 * │.└─┐.│.┌───┴────.│.│.│.└─┬───┘.│.┌─┘.├───┐.┌─┘.├─┘.│.│.│<│
 * │...│.│.│.........│.│.....│.......│...│...│.│...│...│...│.│
 * ├──.├─┼─┴──.│.│.┌─┘.├───┐.└──.──┬─┘.┌─┘.│.│.│.┌─┘.──┴───┤.│
 * │...│.│.....│.│.│...│...│.......│...│...│...│.│.........│.│
 * ├─┐.│.│.──┬─┘.├─┘.┌─┤.──┼───┐.│.│.│.└──.└───┤.│.│.┌─┐.│.│.│
 * │.│...│...│...│...│.│...│...│.│.│.│.........│.│.│.│.│.│.│.│
 * │.│.│.└─┬─┴─┬─┴─┬─┤.├──.│.──┘.│.│.├──────.│.│.└─┤.│.│.└─┤.│
 * │...│...│...│...│.│.│.........│...│.......│.....│.│.│...│.│
 * │.┌─┤.│.│.│.│.│.│.│.│.──┐.──┐.├──.└───────┴─────┘.│.├──.├─┤
 * │.│.│.│.│.│.│.│...│.│...│...│.│...................│.│...│.│
 * │.│.├─┘.│.│.│.├──.│.└───┴─┐.│.└───────┐.──┐.──┬─┐.│.│.──┘.│
 * │.│.│.....│.│.│...│.......│.│.........│...│...│.│...│.....│
 * ├─┘.│.┌──.│.└─┘.┌─┴────.│.│.├───────┐.└─┐.├──.│.└─┬─┘.┌──.│
 * │.....│...│.....│.......│...│.......│...│.│.......│...│...│
 * │.────┴─┐.├────.│.│.────┤.──┘.┌────.├───┘.│.┌────.├──.│.──┤
 * │.......│.│.......│.....│.....│.....│.....│.│.....│...│...│
 * └───────┴─┴───────┴─────┴─────┴─────┴─────┴─┴─────┴───┴───┘
 * }</pre>
 * <br>
 * Created by Tommy Ettinger on 5/7/2019.
 */
public class ConnectingMapGenerator implements IDungeonGenerator {
    
    public int width;
    public int height;
    public int roomWidth;
    public int roomHeight;
    public int wallThickness;
    public char[][] dungeon;
    public int[][] environment;
    public GreasedRegion region;
    private transient GreasedRegion tempRegion;
    public IRNG rng;

    /**
     * Calls {@link #ConnectingMapGenerator(int, int, int, int, IRNG, int)} with width 80, height 80, roomWidth 8,
     * roomHeight 8, a new {@link GWTRNG} for random, and wallThickness 2.
     */
    public ConnectingMapGenerator()
    {
        this(80, 80, 8, 8, new GWTRNG(), 2);
    }
    /**
     * Determines room width and room height by dividing width or height by 10; wallThickness is 2. 
     * @param width total width of the map, in cells
     * @param height total height of the map, in cells
     * @param random an IRNG to make random choices for connecting rooms
     */

    public ConnectingMapGenerator(int width, int height, IRNG random)
    {
        this(width, height, width / 10, height / 10, random, 2);
    }
    /**
     * Exactly like {@link #ConnectingMapGenerator(int, int, int, int, IRNG, int)} with wallThickness 2.
     * @param width total width of the map, in cells
     * @param height total height of the map, in cells
     * @param roomWidth target width of each room, in cells; only counts the center floor area of a room
     * @param roomHeight target height of each room, in cells; only counts the center floor area of a room
     * @param random an IRNG to make random choices for connecting rooms
     */
    public ConnectingMapGenerator(int width, int height, int roomWidth, int roomHeight, IRNG random)
    {
        this(width, height, roomWidth, roomHeight, random, 2);
    }

    /**
     * 
     * @param width total width of the map, in cells
     * @param height total height of the map, in cells
     * @param roomWidth target width of each room, in cells; only counts the center floor area of a room
     * @param roomHeight target height of each room, in cells; only counts the center floor area of a room
     * @param random an IRNG to make random choices for connecting rooms
     * @param wallThickness how thick a wall between two rooms should be, in cells; 1 is minimum, and this usually
     *                      shouldn't be much more than roomWidth or roomHeight
     */
    public ConnectingMapGenerator(int width, int height, int roomWidth, int roomHeight, IRNG random, int wallThickness)
    {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.region = new GreasedRegion(this.width, this.height);
        tempRegion = new GreasedRegion(this.width, this.height);
        this.roomWidth = Math.max(1, roomWidth);
        this.roomHeight = Math.max(1, roomHeight);
        this.wallThickness = Math.max(1, wallThickness);
        dungeon = ArrayTools.fill(' ', this.width, this.height);
        environment = new int[this.width][this.height];
        rng = random;
    }
    /**
     * Generates a dungeon or other map as a 2D char array. Uses the convention of '#' representing a wall and '.'
     * representing a bare floor, and also fills {@link #environment} with appropriate constants from DungeonUtility,
     * like {@link DungeonUtility#ROOM_FLOOR} and {@link DungeonUtility#ROOM_WALL}.
     * 
     * @return a 2D char array representing a room-based map, using standard conventions for walls/floors
     */
    @Override
    public char[][] generate() {
        int gridWidth = (width + wallThickness - 2) / (roomWidth + wallThickness), gridHeight = (height + wallThickness - 2) / (roomHeight + wallThickness), gridMax = gridWidth * gridHeight;
        if(gridWidth <= 0 || gridHeight <= 0)
            return dungeon;
        ArrayTools.fill(dungeon, '#');
        ArrayTools.fill(environment, DungeonUtility.UNTOUCHED);
        region.resizeAndEmpty(width, height);
        IntIntOrderedMap links = new IntIntOrderedMap(gridMax), surface = new IntIntOrderedMap(gridMax);
        IntVLA choices = new IntVLA(4);
        int dx = rng.nextSignedInt(gridWidth), dy = rng.nextSignedInt(gridHeight),
                d = dy << 16 | dx;
        links.put(d, 0);
        surface.put(d, 0);
        for (int i = 0; i < 15 && links.size() < gridMax && !surface.isEmpty(); i++) {
            choices.clear();
            if (dx < gridWidth - 1 && !links.containsKey(d + 1)) choices.add(1);
            if (dy < gridHeight - 1 && !links.containsKey(d + 0x10000)) choices.add(2);
            if (dx > 0 && !links.containsKey(d - 1)) choices.add(4);
            if (dy > 0 && !links.containsKey(d - 0x10000)) choices.add(8);
            if (choices.isEmpty()) {
                surface.remove(d);
                break;
            }
            int choice = choices.getRandomElement(rng);
            links.replace(d, links.get(d) | choice);
            if (choices.size == 1)
                surface.remove(d);
            switch (choice) {
                case 1:
                    d += 1;
                    links.put(d, 4);
                    surface.put(d, 4);
                    break;
                case 2:
                    d += 0x10000;
                    links.put(d, 8);
                    surface.put(d, 8);
                    break;
                case 4:
                    d -= 1;
                    links.put(d, 1);
                    surface.put(d, 1);
                    break;
                default:
                    d -= 0x10000;
                    links.put(d, 2);
                    surface.put(d, 2);
                    break;
            }
            dx = d & 0xFFFF;
            dy = d >>> 16;
        }
        while(links.size() < gridMax)
        {
            d = surface.randomKey(rng);
            dx = d & 0xFFFF;
            dy = d >>> 16;
            for (int i = 0; i < 5 && links.size() < gridMax && !surface.isEmpty(); i++) {
                choices.clear();
                if (dx < gridWidth - 1 && !links.containsKey(d + 1)) choices.add(1);
                if (dy < gridHeight - 1 && !links.containsKey(d + 0x10000)) choices.add(2);
                if (dx > 0 && !links.containsKey(d - 1)) choices.add(4);
                if (dy > 0 && !links.containsKey(d - 0x10000)) choices.add(8);
                if (choices.isEmpty()) {
                    surface.remove(d);
                    break;
                }
                int choice = choices.getRandomElement(rng);
                links.replace(d, links.get(d) | choice);
                if (choices.size == 1)
                    surface.remove(d);
                switch (choice) {
                    case 1:
                        d += 1;
                        links.put(d, 4);
                        surface.put(d, 4);
                        break;
                    case 2:
                        d += 0x10000;
                        links.put(d, 8);
                        surface.put(d, 8);
                        break;
                    case 4:
                        d -= 1;
                        links.put(d, 1);
                        surface.put(d, 1);
                        break;
                    default:
                        d -= 0x10000;
                        links.put(d, 2);
                        surface.put(d, 2);
                        break;
                }
                dx = d & 0xFFFF;
                dy = d >>> 16;
            }
        }
        for (int i = 0; i < links.size(); i++) {
            d = links.keyAt(i);
            dx = d & 0xFFFF;
            dy = d >>> 16;
            int conn = links.getAt(i);
            
            region.insertRectangle(1 + dx * (roomWidth + wallThickness), 1 + dy * (roomHeight + wallThickness), roomWidth, roomHeight);
            if((conn & 1) != 0)
                region.insertRectangle(1 + dx * (roomWidth + wallThickness) + roomWidth, 1 + dy * (roomHeight + wallThickness), wallThickness, roomHeight);
            if((conn & 2) != 0)
                region.insertRectangle(1 + dx * (roomWidth + wallThickness), 1 + dy * (roomHeight + wallThickness) + roomHeight, roomWidth, wallThickness);
            if((conn & 4) != 0)
                region.insertRectangle(1 + dx * (roomWidth + wallThickness) - wallThickness, 1 + dy * (roomHeight + wallThickness), wallThickness, roomHeight);
            if((conn & 8) != 0)
                region.insertRectangle(1 + dx * (roomWidth + wallThickness), 1 + dy * (roomHeight + wallThickness) - wallThickness, roomWidth, wallThickness);
        }
        region.writeCharsInto(dungeon, '.');
        region.writeIntsInto(environment, DungeonUtility.ROOM_FLOOR);
        tempRegion.remake(region).fringe8way().writeIntsInto(environment, DungeonUtility.ROOM_WALL);
        return dungeon;
    }

    /**
     * Gets the most recently-produced dungeon as a 2D char array, usually produced by calling {@link #generate()} or
     * some similar method present in a specific implementation. This normally passes a direct reference and not a copy,
     * so you can normally modify the returned array to propagate changes back into this IDungeonGenerator.
     *
     * @return the most recently-produced dungeon/map as a 2D char array
     */
    @Override
    public char[][] getDungeon() {
        return dungeon;
    }
    /**
     * Gets a 2D array of int constants, each representing a type of environment corresponding to a static field of
     * DungeonUtility. This array will have the same size as the last char 2D array produced by generate(); the value
     * of this method if called before generate() is undefined, but probably will be a 2D array of all 0 (UNTOUCHED).
     * <ul>
     * <li>DungeonUtility.UNTOUCHED, equal to 0, is used for any cells that aren't near a floor.</li>
     * <li>DungeonUtility.ROOM_FLOOR, equal to 1, is used for floor cells inside wide room areas.</li>
     * <li>DungeonUtility.ROOM_WALL, equal to 2, is used for wall cells around wide room areas.</li>
     * <li>DungeonUtility.CAVE_FLOOR, equal to 3, is used for floor cells inside rough cave areas.</li>
     * <li>DungeonUtility.CAVE_WALL, equal to 4, is used for wall cells around rough cave areas.</li>
     * <li>DungeonUtility.CORRIDOR_FLOOR, equal to 5, is used for floor cells inside narrow corridor areas.</li>
     * <li>DungeonUtility.CORRIDOR_WALL, equal to 6, is used for wall cells around narrow corridor areas.</li>
     * </ul>
     *
     * @return a 2D int array where each element is an environment type constant in DungeonUtility
     */
    public int[][] getEnvironment() {
        return environment;
    }

}
