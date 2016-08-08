package squidpony.squidgrid.mapping;

import squidpony.squidmath.RNG;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Tommy Ettinger on 8/7/2016.
 */
public class ThinDungeonGenerator extends SectionDungeonGenerator {
    /**
     * Make a DungeonGenerator with a LightRNG using a random seed, height 40, and width 40.
     */
    public ThinDungeonGenerator() {
    }

    /**
     * Make a DungeonGenerator with the given height and width; the RNG used for generating a dungeon and
     * adding features will be a LightRNG using a random seed.
     *
     * @param width  The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     */
    public ThinDungeonGenerator(int width, int height) {
        super(width, height);
    }

    /**
     * Make a DungeonGenerator with the given height, width, and RNG. Use this if you want to seed the RNG.
     *
     * @param width  The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     * @param rng    The RNG to use for all purposes in this class; if it is a StatefulRNG, then it will be used as-is,
     */
    public ThinDungeonGenerator(int width, int height, RNG rng) {
        super(width, height, rng);
    }

    /**
     * Copies all fields from copying and makes a new DungeonGenerator.
     *
     * @param copying the DungeonGenerator to copy
     */
    public ThinDungeonGenerator(ThinDungeonGenerator copying) {
        super(copying);
    }

    @Override
    protected char[][] innerGenerate() {
        finder = finder.makeThin();
        width = finder.width;
        height = finder.height;
        if(stairsUp != null)
        {
            stairsUp = stairsUp.multiply(2);
        }
        if(stairsDown != null)
        {
            stairsDown = stairsDown.multiply(2);
        }
        dungeon = new char[width][height];
        for (int x = 0; x < width; x++) {
            Arrays.fill(dungeon[x], '#');
        }
        ArrayList<char[][]> rm = finder.findRooms(),
                cr = finder.findCorridors(),
                cv = finder.findCaves();
        char[][] roomMap = innerGenerate(RoomFinder.merge(rm, width, height), roomFX),
                allCorridors = RoomFinder.merge(cr, width, height),
                corridorMap = innerGenerate(allCorridors, corridorFX),
                allCaves = RoomFinder.merge(cv, width, height),
                caveMap = innerGenerate(allCaves, caveFX),
                doorMap = makeDoors(rm, cr, allCaves, allCorridors);
        char[][][] lakesAndMazes = makeLake(rm, cv);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(corridorMap[x][y] != '#' && lakesAndMazes[0][x][y] != '#')
                    dungeon[x][y] = ':';
                else if(doorMap[x][y] == '+' || doorMap[x][y] == '/')
                    dungeon[x][y] = doorMap[x][y];
                else if(doorMap[x][y] == '*')
                    dungeon[x][y] = '#';
                else if(roomMap[x][y] != '#')
                    dungeon[x][y] = roomMap[x][y];
                else if(lakesAndMazes[1][x][y] != '#')
                    dungeon[x][y] = lakesAndMazes[1][x][y];
                else if(corridorMap[x][y] != '#')
                    dungeon[x][y] = corridorMap[x][y];
                else if(caveMap[x][y] != '#')
                    dungeon[x][y] = caveMap[x][y];
                else if(lakesAndMazes[0][x][y] != '#')
                    dungeon[x][y] = lakesAndMazes[0][x][y];
            }
        }

        placement = new Placement(finder);
        return dungeon;
    }
}
