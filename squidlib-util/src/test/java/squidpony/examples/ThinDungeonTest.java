package squidpony.examples;

import squidpony.ArrayTools;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.SectionDungeonGenerator;
import squidpony.squidgrid.mapping.SerpentMapGenerator;
import squidpony.squidgrid.mapping.ThinDungeonGenerator;
import squidpony.squidmath.StatefulRNG;

/**
 * Sample output: {@code
 * ┌─────────────────┐     ┌─────────────┐   ┌─────┐ ┌───────┐   ┌─────┐
 * │ ~ ~ ~ ~ ~ ~ ~ ~ └─┐   │ . . . . . . └───┤ . . │ │ . . . │   │ . . │
 * │ ~ ~ ~ ~ ~ ~ ~ ~ ~ │   │ . . . . . . . . │ . . │ │ . . ──┴─┬─┘ . . │
 * │ ~ ~ ~ ~ ~ ~ ~ ~ ~ │ ┌─┘ . . . . . . . . │ . . │ │ . . . . │ . . . │
 * │ ~ ~ ~ ┌───┐ ~ ~ ~ │ │ . . . . . . . │ . │ . . └─┘ . . . . . . . . │
 * │ ~ ~ ──┤   │ ~ ~ ~ │ │ . . . . . . . │ . . . . . . . ^ . . . . . . │
 * │ ~ ~ ~ └───┘ ~ ~ ~ │ │ . . . . . . . │ . . . ^ . . . . . . . . . ┌─┘
 * │ ~ ~ ~ ~ ~ ~ ~ ~ ~ │ │ . . . . . . . ├───────────┐ . . . . ┌─────┘
 * │ ~ . ~ ~ ~ ~ ~ ~ ┌─┘ │ . . . . . . . │           │ . . ┌───┘
 * └─┐ ~ ~ ~ ~ ~ ┌───┘   │ . . ────────┬─┘     ┌─────┘ . . │
 *   │ ~ ~ ~ ~ ~ │       │ . . . . . . │       │ . . . . . │     ┌─────┐
 *   │ ~ ~ ~ ┌───┴───┬───┴─┐ . . . . . └─┐   ┌─┘ . . . . . ├─────┘ . . │
 * ┌─┴─┐ ~ ~ │ ~ ~ ~ │ ~ ~ └─┐ . . . . . └───┘ . . . . . ┌─┘ . . . . . │
 * │ ~ │ ~ ~ │ ~ ~ ~ │ ~ ~ ~ └─┐ . . . . ~ . . . . . . ┌─┘ . . . . . . │
 * │ ~ │ ~ ~ │ ~ ~ ~ │ ~ ~ ~ ~ └─┬─┐ . ~ ~ . . . . . ┌─┘ . . . ┌─┐ . . │
 * │ ~ │ ~ ~ │ ~ ~ ~ │ ~ ~ ~ ~ ~ └─┼───┐ ~ ~ ┌─────┬─┘ . . . . │ └─────┘
 * │ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ └───┘ ~ ~ ├─────┘ . . . . . │
 * │ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ + . . . . . . . ┌─┘   ┌───┐
 * │ ~ ~ ~ ~ ~ ~ ~ ~ ┌───┐ ~ ~ ~ ~ ~ ~ ~ ~ ~ │ . . . . . . ┌─┘     │ . │
 * └─────────────────┘   │ ~ ~ ~ ~ ~ ┌───────┤ . . ──┬─────┤       │ . │
 * ┌───────────────┐     └───┐ ~ ~ ┌─┘       │ . . . + . . └───────┤ . │
 * │ . . . . . . . │         │ ~ ~ ├───────┐ │ . . . │ . . . . . . │ . │
 * │ . . . . . . . ├───┐     │ ~ ~ │ . . . └─┴───────┘ . . . . . . ├───┘
 * │ . . ┌─┐ . . . │ ~ │     │ ~ ~ │ . . . . . . . . . . . . . . . │
 * │ . . │ │ . . ~ . ~ └─────┤ ~ ~ │ ^ . . . . . . . . . . ┌───────┴───┐
 * │ . . │ │ ~ ~ ~ ~ ~ ~ ~ ~ │ ~ ~ │ . . . . . . ──────────┤ . . . . . │
 * │ . . │ │ ~ ~ ~ ~ ~ ~ ~ ~ │ ~ ~ │ ^ . . . . . . . . . . │ . . . . . │
 * └───┬─┴─┤ ~ ~ ──┐ ~ │ ~ ~ │ ~ ~ │ . . . . . . . . . . . │ . . . . . │
 *     │ ~ │ . . ~ ├───┤ ~ ~ │ ~ . │ . . ┌───┐ . . . . . . │ . . . . . │
 *     │ ~ + ~ ~ ~ │   │ ~ ~ + ~ ~ ~ ~ . │   │ . . . . . ┌─┴─────┐ ^ . │
 * ┌───┴───┴─┐ ~ ~ │   │ ~ ~ │ ~ ~ ~ ~ ~ │ ┌─┘ . . . . . │     ┌─┘ . . │
 * │ . . . . │ . ~ └───┼─────┤ ~ . ┌─────┘ │ . . . . . . │     │ . . . │
 * │ . . . . . . ~ . . │ . . └─┬───┘ ┌─────┤ . . . . . . │   ┌─┘ . . . │
 * └─┐ . . . . . ~ ~ . + . . . └───┐ │ . . ├───────┐ . . │   │ . . . . │
 *   │ . . . . . ~ . . │ . . . . . └─┤ . . ├───────┤ . . │   │ . . . . │
 *   │ . . . . . . . . ├─┐ . . . . . │ . . │ . ~ ~ │ . . └───┤ . . . ──┤
 *   │ . . . . . . . . │ └─┐ . . . . . . . + . ~ ~ │ . . . . + . . . . │
 *   │ . . ┌───────────┘   └─┐ . . . . . . │ . . . │ . . . . │ . . . . │
 *   │ . . │                 └─┐ . . . . ┌─┤ . . . └─────. . ├─┐ . . . │
 *   │ . . │               ┌───┴───. . ┌─┘ │ . . . . . . . . │ └─┬─────┤
 *   │ . . ├─────────┬─────┤ . . . . ^ │   │ . . . . . . . . │   │ . . │
 *   │ . . │ . . . . │ ~ ~ │ . . . . . └───┴─────┬─┐ . . . . │   │ . . │
 *   │ . . │ . . . . │ ~ ~ │ . . . . . . . . . . │ │ . ^ . . ├───┤ . . │
 *   │ . . │ . . . . │ ~ ~ │ . . . . . . . . . . │ │ . . . . │ . │ . . │
 *   │ . . │ . . . . │ ~ ~ │ . . . . . . ┌─┬───/ ┤ └───┬─────┘ . └─/ ──┤
 * ┌─┘ . . └─────. . │ ~ ~ │ . . . . . . │ │ . . │     │ . . . . . . . │
 * │ . . . . . . . . │ ~ ~ └─────────. . │ │ . ^ └─────┘ . . . . . . . │
 * │ . . . . . . . . │ ~ ~ ~ ~ ~ ~ ~ ~ ~ │ │ . . . . . . . . │ . . . . │
 * └─┐ . . . . . . . │ ~ ~ ~ ~ ~ ~ ~ ~ ~ │ │ . . ^ . . . . . ├─────────┘
 *   │ . . . . . ┌───┴───┐ ~ ~ ──┬───────┘ └───┐ . . . . . ┌─┘   ┌─────┐
 *   └───┐ . . ┌─┘       ├───~ ~ │             │ . . . . . │     │ . . │
 *       │ . . │       ┌─┘ ~ ~ ~ ├───────────┬─┴───. . . . └─────┘ . . │
 * ┌─────┤ . . └─┐     │ . ~ ~ ~ │ . . . . . │ . . . . . . . . . . . . │
 * │ . . │ . . . │     │ . . ~ ┌─┤ . . . . . + . . . . . . . . . . . . │
 * │ . . + . . . │     │ . . ┌─┘ │ . . │ . . │ . . ┌─┐ . . ┌─────┐ . . │
 * │ . . ├─┐ . . └─────┘ . . └───┘ . . │ . . │ . . │ │ . ^ │     │ . . │
 * │ . . │ │ . . . . . . . . . . . . . ├─────┘ . . └─┘ . . │     │ . . │
 * │ . . │ │ . ^ . . . . ^ ^ . . . . . │ . . . . . . . . . │     │ ^ . │
 * └─────┘ └─────────────┐ . . . . . . + . . . . . . . . . │     │ . . │
 *                       └─────────────┴───────────────────┘     └─────┘
 * }
 * Created by Tommy Ettinger on 4/8/2015.
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
public class ThinDungeonTest {
    public static int width = 70, height = 70, depth = 16;
    public static void main( String[] args )
    {
        //seed is, in base 36, the number SQUIDLIB
        StatefulRNG rng = new StatefulRNG(2252637788195L);
        SerpentMapGenerator serpent = new SerpentMapGenerator(width, height, rng, 0.2);
        serpent.putWalledBoxRoomCarvers(5);
        serpent.putWalledRoundRoomCarvers(3);
        serpent.putCaveCarvers(4);
        char[][] map = serpent.generate();

        char[][] sdungeon;
        int[][] env;

        for(int k : new int[]{ThinDungeonGenerator.CAVE_WALL_NORMAL | ThinDungeonGenerator.CORRIDOR_WALL_NORMAL | ThinDungeonGenerator.ROOM_WALL_NORMAL,
                ThinDungeonGenerator.CAVE_WALL_RETRACT | ThinDungeonGenerator.CORRIDOR_WALL_RETRACT | ThinDungeonGenerator.ROOM_WALL_RETRACT,
                ThinDungeonGenerator.CAVE_WALL_CHAOTIC | ThinDungeonGenerator.CORRIDOR_WALL_EXPAND | ThinDungeonGenerator.ROOM_WALL_EXPAND
        }) {
            ThinDungeonGenerator tdg = new ThinDungeonGenerator(width, height, rng, k, 0, 0);
            tdg.addDoors(12, false);
            //tdg.addWater(SectionDungeonGenerator.CAVE, 13);
            //tdg.addBoulders(SectionDungeonGenerator.ALL, 13);
            tdg.addWater(SectionDungeonGenerator.CAVE, 9);
            tdg.addMaze(30);
            tdg.addLake(10, '£', '¢');
            rng.setState(2252637788195L);
            map = serpent.generate();
            env = serpent.getEnvironment();
            tdg.generate(map, env);
            //RNG rand = new RNG();
            //tdg.generate(rand.getRandomElement(TilesetType.values()));
            sdungeon = ArrayTools.copy(tdg.getDungeon());
            sdungeon[tdg.stairsUp.x][tdg.stairsUp.y] = '<';
            sdungeon[tdg.stairsDown.x][tdg.stairsDown.y] = '>';
            //System.out.println(tdg);
            tdg.setDungeon(DungeonUtility.hashesToLines(sdungeon, true));
            System.out.println(tdg);
        }

    }
}
