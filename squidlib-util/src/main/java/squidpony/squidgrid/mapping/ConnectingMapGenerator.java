package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.annotation.Beta;
import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.IRNG;
import squidpony.squidmath.IntIntOrderedMap;
import squidpony.squidmath.IntVLA;

/**
 * Work in progress.
 * <br>
 * Created by Tommy Ettinger on 5/7/2019.
 */
@Beta
public class ConnectingMapGenerator implements IDungeonGenerator {
    
    public int width;
    public int height;
    public int roomWidth;
    public int roomHeight;
    public char[][] dungeon;
    public int[][] environment;
    public IRNG rng;
    
    public ConnectingMapGenerator()
    {
        this(80, 80, new GWTRNG());
    }
    public ConnectingMapGenerator(int width, int height, IRNG random)
    {
        this(width, height, width >> 3, height >> 3, random);
    }
    public ConnectingMapGenerator(int width, int height, int roomWidth, int roomHeight, IRNG random)
    {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.roomWidth = Math.max(3, roomWidth);
        this.roomHeight = Math.max(3, roomHeight);
        dungeon = ArrayTools.fill(' ', this.width, this.height);
        environment = new int[this.width][this.height];
        rng = random;
    }
    /**
     * Generates a dungeon or other map as a 2D char array. Any implementation may allow its own configuration and
     * customization of how dungeons are generated, but each must provide this as a sane default. Most implementations
     * should use the convention of '#' representing a wall and '.' representing a bare floor, but beyond that, anything
     * could be present in the char array.
     *
     * @return a 2D char array representing some kind of map, probably using standard conventions for walls/floors
     */
    @Override
    public char[][] generate() {
        int gridWidth = width / roomWidth, gridHeight = height / roomHeight, gridMax = gridWidth * gridHeight;
        if(gridWidth <= 0 || gridHeight <= 0)
            return dungeon;
        ArrayTools.fill(dungeon, '.');
        ArrayTools.fill(environment, DungeonUtility.ROOM_FLOOR);
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
                i--;
                continue;
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
            
            dungeon[dx * roomWidth][dy * roomHeight] = '#';
            dungeon[dx * roomWidth + roomWidth - 1][dy * roomHeight] = '#';
            dungeon[dx * roomWidth][dy * roomHeight + roomHeight - 1] = '#';
            dungeon[dx * roomWidth + roomWidth - 1][dy * roomHeight + roomHeight - 1] = '#';

            if((conn & 1) == 0) {
                ArrayTools.fill(dungeon, '#', dx * roomWidth + roomWidth - 1, dy * roomHeight, dx * roomWidth + roomWidth - 1, dy * roomHeight + roomHeight - 1);
                ArrayTools.fill(environment, DungeonUtility.ROOM_WALL, dx * roomWidth + roomWidth - 1, dy * roomHeight, dx * roomWidth + roomWidth - 1, dy * roomHeight + roomHeight - 1);
            }
            else {
                ArrayTools.fill(environment, DungeonUtility.CORRIDOR_FLOOR, dx * roomWidth + roomWidth - 1, dy * roomHeight + 1, dx * roomWidth + roomWidth - 1, dy * roomHeight + roomHeight - 2);
                environment[dx * roomWidth + roomWidth - 1][dy * roomHeight] = DungeonUtility.CORRIDOR_WALL;
                environment[dx * roomWidth + roomWidth - 1][dy * roomHeight + roomHeight - 1] = DungeonUtility.CORRIDOR_WALL;
            }

            if((conn & 2) == 0) {
                ArrayTools.fill(dungeon, '#', dx * roomWidth, dy * roomHeight + roomHeight - 1, dx * roomWidth + roomWidth - 1, dy * roomHeight + roomHeight - 1);
                ArrayTools.fill(environment, DungeonUtility.ROOM_WALL, dx * roomWidth, dy * roomHeight + roomHeight - 1, dx * roomWidth + roomWidth - 1, dy * roomHeight + roomHeight - 1);
            }
            else {
                ArrayTools.fill(environment, DungeonUtility.CORRIDOR_FLOOR, dx * roomWidth + 1, dy * roomHeight + roomHeight - 1, dx * roomWidth + roomWidth - 2, dy * roomHeight + roomHeight - 1);
                environment[dx * roomWidth + roomWidth - 1][dy * roomHeight] = DungeonUtility.CORRIDOR_WALL;
                environment[dx * roomWidth][dy * roomHeight] = DungeonUtility.CORRIDOR_WALL;
            }

            if((conn & 4) == 0) {
                ArrayTools.fill(dungeon, '#', dx * roomWidth, dy * roomHeight, dx * roomWidth, dy * roomHeight + roomHeight - 1);
                ArrayTools.fill(environment, DungeonUtility.ROOM_WALL, dx * roomWidth, dy * roomHeight, dx * roomWidth, dy * roomHeight + roomHeight - 1);
            }
            else {
                ArrayTools.fill(environment, DungeonUtility.CORRIDOR_FLOOR, dx * roomWidth, dy * roomHeight + 1, dx * roomWidth, dy * roomHeight + roomHeight - 2);
                environment[dx * roomWidth][dy * roomHeight] = DungeonUtility.CORRIDOR_WALL;
                environment[dx * roomWidth][dy * roomHeight + roomHeight - 1] = DungeonUtility.CORRIDOR_WALL;
            }

            if((conn & 8) == 0) {
                ArrayTools.fill(dungeon, '#', dx * roomWidth, dy * roomHeight, dx * roomWidth + roomWidth - 1, dy * roomHeight);
                ArrayTools.fill(environment, DungeonUtility.ROOM_WALL, dx * roomWidth, dy * roomHeight, dx * roomWidth + roomWidth - 1, dy * roomHeight);
            }
            else {
                ArrayTools.fill(environment, DungeonUtility.CORRIDOR_FLOOR, dx * roomWidth + 1, dy * roomHeight, dx * roomWidth + roomWidth - 2, dy * roomHeight);
                environment[dx * roomWidth][dy * roomHeight + roomHeight - 1] = DungeonUtility.CORRIDOR_WALL;
                environment[dx * roomWidth + roomWidth - 1][dy * roomHeight + roomHeight - 1] = DungeonUtility.CORRIDOR_WALL;
            }
            
        }
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
