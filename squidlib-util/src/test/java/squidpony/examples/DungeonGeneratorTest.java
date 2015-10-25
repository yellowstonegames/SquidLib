package squidpony.examples;

import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.MixedGenerator;
import squidpony.squidgrid.mapping.SerpentMapGenerator;
import squidpony.squidmath.LightRNG;
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
public class DungeonGeneratorTest {
    public static int width = 120, height = 120;
    public static void main( String[] args )
    {
        StatefulRNG rng = new StatefulRNG(new LightRNG(0xea7f00d));
        DungeonGenerator dungeonGenerator = new DungeonGenerator(width, height, rng);
        dungeonGenerator.addDoors(15, false);
        dungeonGenerator.addWater(25);
        dungeonGenerator.addTraps(2);
        MixedGenerator mix = new MixedGenerator(width, height, rng);
        mix.putCaveCarvers(3);
        mix.putBoxRoomCarvers(1);
        mix.putRoundRoomCarvers(2);
        dungeonGenerator.generate(mix.generate());
        char[][] dungeon = dungeonGenerator.getDungeon();
        dungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        dungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(dungeon)));
        System.out.println(dungeonGenerator);
        dungeonGenerator = new DungeonGenerator(width, height, rng);
        dungeonGenerator.addDoors(15, false);
        dungeonGenerator.addWater(20);
        dungeonGenerator.addGrass(10);
        rng.setState(0xea7f00d);
        SerpentMapGenerator serpent = new SerpentMapGenerator(width, height, rng);
        serpent.putBoxRoomCarvers(2);
        serpent.putRoundRoomCarvers(2);
        serpent.putCaveCarvers(3);
        char[][] map = serpent.generate();
        dungeonGenerator.generate(map);

        dungeon = dungeonGenerator.getDungeon();
        dungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        dungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(dungeon)));
        System.out.println(dungeonGenerator);

    }
}
