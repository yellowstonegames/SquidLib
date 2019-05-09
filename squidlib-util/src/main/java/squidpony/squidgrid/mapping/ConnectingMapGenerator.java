package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.squidmath.*;

/**
 * A dungeon-or-other-map generator that can start with a pre-placed {@code char[][]} and connect any features in that
 * map by placing rooms and corridors where there is blank space ({@code ' '}) in the original {@code char[][]}.
 * <br>
 * Created by Tommy Ettinger on 5/7/2019.
 */
public class ConnectingMapGenerator implements IDungeonGenerator {
    
    public int width;
    public int height;
    public char[][] dungeon;
    public int[][] environment;
    public IRNG rng;
    
    public ConnectingMapGenerator()
    {
        this(80, 80, new GWTRNG());
    }
    public ConnectingMapGenerator(int width, int height, IRNG random)
    {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        dungeon = ArrayTools.fill(' ', this.width, this.height);
        environment = new int[this.width][this.height];
        rng = random;
    }
    public ConnectingMapGenerator(char[][] prePlaced, IRNG random)
    {
        dungeon = ArrayTools.copy(prePlaced);
        width = dungeon.length;
        height = dungeon[0].length;
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
        GreasedRegion empty = new GreasedRegion(dungeon, ' ').retract8way();
        //GreasedRegion blocked = empty.copy().not();
        GreasedRegion working = empty.copy().empty(), working2 = working.copy(), working3 = working.copy();
        GreasedRegion rooms = working.copy(), corridors = working.copy(), doors = new GreasedRegion(dungeon, '+');
        Coord pt;
        int frustration = 0, targetSize = width * height * 3 >> 3;
        while (frustration < 100 && empty.size() > targetSize)
        {
            pt = empty.singleRandom(rng);
            working.insertRectangle(
                    MathExtras.clamp(pt.x + 4 - rng.nextSignedInt(9), 0, width - 1),
                    MathExtras.clamp(pt.y + 4 - rng.nextSignedInt(9), 0, height - 1),
                    rng.between(6, 12), rng.between(6, 12));
            if (empty.not().intersects(working))
            {
                empty.or(working);
                rooms.or(working.retract8way());
            }
            else frustration++;
            empty.not();
            working.clear();
        }
        doors.or(working.remake(rooms).fringe().removeEdges().quasiRandomRegion(0.075));
        working.remake(doors);
        frustration = 0;
        for(Coord door : working)
        {
            pt = doors.singleRandom(rng);
            working2.insertRectangle(Math.min(door.x, pt.x), Math.min(door.y, pt.y),
                    Math.abs(door.x - pt.x), Math.abs(door.y - pt.y)).and(empty).largestPart();
            working3.remake(working2).expand();
            if(working3.contains(door) && working3.contains(pt))
                corridors.or(working2);
            else if(++frustration >= 150)
                break;
        }
        rooms.writeCharsInto(dungeon, '.');
        rooms.writeIntsInto(environment, MixedGenerator.ROOM_FLOOR);
        corridors.writeCharsInto(dungeon, '.');
        corridors.writeIntsInto(environment, MixedGenerator.CORRIDOR_FLOOR);
        doors.writeCharsInto(dungeon, '+');
        corridors.fringe8way().writeIntsInto(environment, MixedGenerator.CORRIDOR_WALL);
        rooms.fringe8way().writeIntsInto(environment, MixedGenerator.ROOM_WALL);
        doors.writeIntsInto(environment, MixedGenerator.CORRIDOR_FLOOR);
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
     * MixedGenerator. This array will have the same size as the last char 2D array produced by generate(); the value
     * of this method if called before generate() is undefined, but probably will be a 2D array of all 0 (UNTOUCHED).
     * <ul>
     * <li>MixedGenerator.UNTOUCHED, equal to 0, is used for any cells that aren't near a floor.</li>
     * <li>MixedGenerator.ROOM_FLOOR, equal to 1, is used for floor cells inside wide room areas.</li>
     * <li>MixedGenerator.ROOM_WALL, equal to 2, is used for wall cells around wide room areas.</li>
     * <li>MixedGenerator.CAVE_FLOOR, equal to 3, is used for floor cells inside rough cave areas.</li>
     * <li>MixedGenerator.CAVE_WALL, equal to 4, is used for wall cells around rough cave areas.</li>
     * <li>MixedGenerator.CORRIDOR_FLOOR, equal to 5, is used for floor cells inside narrow corridor areas.</li>
     * <li>MixedGenerator.CORRIDOR_WALL, equal to 6, is used for wall cells around narrow corridor areas.</li>
     * </ul>
     *
     * @return a 2D int array where each element is an environment type constant in MixedGenerator
     */
    public int[][] getEnvironment() {
        return environment;
    }

}
