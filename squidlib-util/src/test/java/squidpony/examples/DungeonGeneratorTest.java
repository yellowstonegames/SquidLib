package squidpony.examples;

import squidpony.GwtCompatibility;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.*;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.RNG;
import squidpony.squidmath.StatefulRNG;

import java.io.FileWriter;
import java.io.IOException;

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
    public static int width = 70, height = 70, depth = 16;
    public static void main( String[] args )
    {
        //seed is, in base 36, the number SQUIDLIB
        StatefulRNG rng = new StatefulRNG(2252637788195L);
        DungeonGenerator dungeonGenerator = new DungeonGenerator(width, height, rng);

        dungeonGenerator.addDoors(15, true);
        dungeonGenerator.addWater(15);
        dungeonGenerator.addGrass(10);
        dungeonGenerator.addBoulders(5);
        dungeonGenerator.addTraps(2);

        //MixedGenerator mix = new MixedGenerator(width, height, rng);
        //mix.putCaveCarvers(3);
        //mix.putWalledBoxRoomCarvers(2);
        //mix.putWalledRoundRoomCarvers(2);
        //dungeonGenerator.generate(mix.generate());
        dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);
        char[][] dungeon = dungeonGenerator.getDungeon();
        dungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        dungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(dungeon, true)));
        System.out.println(dungeonGenerator);
        System.out.println("------------------------------------------------------------");


        dungeonGenerator = new DungeonGenerator(width, height, rng);
        dungeonGenerator.addDoors(12, false);
        dungeonGenerator.addWater(8);
        dungeonGenerator.addGrass(11);
        rng.setState(2252637788195L);
        SerpentMapGenerator serpent = new SerpentMapGenerator(width, height, rng, 0.2);
        serpent.putWalledBoxRoomCarvers(5);
        serpent.putRoundRoomCarvers(3);
        serpent.putCaveCarvers(3);
        char[][] map = serpent.generate();
        dungeonGenerator.generate(map);

        char[][] sdungeon = DungeonUtility.closeDoors(dungeonGenerator.getDungeon());
        sdungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        sdungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon)));
        System.out.println(dungeonGenerator);

        System.out.println("------------------------------------------------------------");
        rng.setState(2252637788195L);
        SectionDungeonGenerator sdg = new SectionDungeonGenerator(width, height, rng);
        sdg.addDoors(12, false);
        //sdg.addWater(SectionDungeonGenerator.CAVE, 13);
        sdg.addBoulders(SectionDungeonGenerator.ALL, 13);
        sdg.addWater(SectionDungeonGenerator.CAVE, 9);
        sdg.addMaze(15);
        sdg.addLake(8, '£', '¢');
        rng.setState(0xFEEEEEEEEEL);
        serpent = new SerpentMapGenerator(width, height, rng, 0.2);
        serpent.putWalledBoxRoomCarvers(5);
        serpent.putRoundRoomCarvers(3);
        serpent.putCaveCarvers(3);
        map = serpent.generate();
        int[][] env = serpent.getEnvironment();
        sdg.generate(map, env);
        //RNG rand = new RNG();
        //sdg.generate(rand.getRandomElement(TilesetType.values()));
        sdungeon = GwtCompatibility.copy2D(sdg.getDungeon());
        sdungeon[sdg.stairsUp.x][sdg.stairsUp.y] = '<';
        sdungeon[sdg.stairsDown.x][sdg.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon, true)));
        System.out.println(dungeonGenerator);

        /*
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                switch (env[x][y])
                {
                    case MixedGenerator.ROOM_FLOOR: sdungeon[x][y] = '1';
                        break;
                    case MixedGenerator.CAVE_FLOOR: sdungeon[x][y] = '3';
                        break;
                    case MixedGenerator.CORRIDOR_FLOOR: sdungeon[x][y] = '5';
                        break;
                }
            }
        }
        */

        for(OrderedSet<Coord> lhs : sdg.placement.getAlongStraightWalls())
        {
            for(Coord c : lhs)
            {
                sdungeon[c.x][c.y] = '}';
            }
        }

        for(OrderedSet<Coord> lhs : sdg.placement.getCorners())
        {
            for(Coord c : lhs)
            {
                sdungeon[c.x][c.y] = 'º';
            }
        }

        for(OrderedSet<Coord> lhs : sdg.placement.getCenters())
        {
            for(Coord c : lhs)
            {
                sdungeon[c.x][c.y] = '$';
            }
        }

        for(Coord c : sdg.placement.getHidingPlaces(Radius.CIRCLE, 8)) {
            sdungeon[c.x][c.y] = 'h';
        }
        // Just a little fun with Java that uses no alphanumerics other than keywords to express "Hello, World!"
        //char[] __ = new char[]{'$' << (',' ^ '-'), '`' | ('-' - '('), '$' << (',' ^ '-') | '$', '$' << (',' ^ '-') | '$', '/' + '@', ',', ' ', '(' + '/', '/' + '@', '(' + '%' + '%', '$' << (',' ^ '-') | '$', '*' + ':', '!'};

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon, true)));
        System.out.println(dungeonGenerator);
        System.out.println("------------------------------------------------------------");

/*
        dungeonGenerator = new DungeonGenerator(width, height, rng);
        //dungeonGenerator.addDoors(10, false);
        rng.setState(2252637788195L);
        PacMazeGenerator pac = new PacMazeGenerator(width, height, rng);
        map = pac.generate();
        dungeonGenerator.generate(map);

        sdungeon = DungeonUtility.closeDoors(dungeonGenerator.getDungeon());
        sdungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        sdungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon)));
        System.out.println(dungeonGenerator);

        System.out.println("------------------------------------------------------------");
*/

        rng.setState(2252637788195L);
        sdg = new SectionDungeonGenerator(width, height, rng);
        //sdg.addDoors(12, false);
        //sdg.addWater(SectionDungeonGenerator.CAVE, 13);
        //sdg.addBoulders(SectionDungeonGenerator.ALL, 13);
        //sdg.addWater(SectionDungeonGenerator.CAVE, 9);
        //sdg.addMaze(30);
        //sdg.addLake(10, '£', '¢');
        rng.setState(0xFEEEEEEEEEL);
        OrganicMapGenerator organic = new OrganicMapGenerator(0.57, 0.65, width, height, rng);
        map = organic.generate();
        env = organic.getEnvironment();
        sdg.generate(map, env);
        //RNG rand = new RNG();
        //sdg.generate(rand.getRandomElement(TilesetType.values()));
        sdungeon = sdg.getDungeon();
        //sdungeon[sdg.stairsUp.x][sdg.stairsUp.y] = '<';
        //sdungeon[sdg.stairsDown.x][sdg.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon, false)));
        System.out.println(dungeonGenerator);
        System.out.println("------------------------------------------------------------");

        /*
        rng.setState(2252637788195L);
        RectangleRoomFinder rrf = new RectangleRoomFinder(sdungeon);
        rrf.minimumDiagonal = 3;
        rrf.onlySquareRooms = true;
        List<Rectangle> rectangles = rng.shuffle(rrf.findRectangles());
        int nh, nw, nx, ny;
        Coord center;
        char[][] dungeon2 = GwtCompatibility.fill2D('#', width, height);
        for(Rectangle rect : rectangles)
        {
            if(rng.nextDouble() < 0.1) continue;
            center = Rectangle.Utils.center(rect);
            nh = (int)(rect.getHeight() * (rng.nextDouble(1.5) + 1.3));
            nw = (int)(rect.getWidth() * (rng.nextDouble(1.5) + 1.3));
            nx = Math.max(1, Math.min(width - 2, center.x - nw/2));
            ny = Math.max(1, Math.min(height - 2, center.y - nh/2));
            GwtCompatibility.insert2D(DungeonUtility.wallWrap(GwtCompatibility.fill2D('.', nw, nh)),
                    dungeon2, nx, ny);
        }
        DungeonUtility.wallWrap(dungeon2);

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(dungeon2, true)));
        System.out.println(dungeonGenerator);
        System.out.println("------------------------------------------------------------");
        */
        rng.setState(0xFEEEEEEEEEL);
        DenseRoomMapGenerator dense = new DenseRoomMapGenerator(width, height, rng);
        map = dense.generate();
        DungeonUtility.ensurePath(map, rng, '\t', '#');
        env = dense.getEnvironment();
        sdg.addDoors(80, false);
        sdg.generate(map, env);
        //RNG rand = new RNG();
        //sdg.generate(rand.getRandomElement(TilesetType.values()));
        sdungeon = sdg.getDungeon();
        //sdungeon[sdg.stairsUp.x][sdg.stairsUp.y] = '<';
        //sdungeon[sdg.stairsDown.x][sdg.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon, false)));
        System.out.println(dungeonGenerator);
        System.out.println("------------------------------------------------------------");


        rng.setState(2252637788195L);
        ModularMapGenerator mmg = new ModularMapGenerator(width, height, rng);
        sdg.clearEffects();
        sdg.addDoors(70, false);
        sdg.addBoulders(SectionDungeonGenerator.ROOM, 15);
        dungeon = mmg.generate();
        dungeon = sdg.generate(dungeon, mmg.getEnvironment());
        sdg.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(dungeon, false)));
        System.out.println(sdg);
        System.out.println("------------------------------------------------------------");


        /*
        dungeonGenerator = new DungeonGenerator(width, height, rng);
        //dungeonGenerator.addDoors(15, false);
        //dungeonGenerator.addWater(20);
        //dungeonGenerator.addGrass(10);
        rng.setState(0xf00dd00dL);
        serpent = new SerpentMapGenerator(width, height, rng, 0.4, true);
        serpent.putWalledBoxRoomCarvers(2);
        serpent.putWalledRoundRoomCarvers(2);
        serpent.putCaveCarvers(3);
        map = serpent.generate();
        dungeonGenerator.generate(map);

        sdungeon = dungeonGenerator.getDungeon();
        sdungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        sdungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon)));
        System.out.println(dungeonGenerator);
        */
        /*
        dungeonGenerator = new DungeonGenerator(width, height, rng);
        //dungeonGenerator.addDoors(15, false);
        //dungeonGenerator.addWater(20);
        //dungeonGenerator.addGrass(10);
        rng.setState(0xf00dd00dL);
        LanesMapGenerator lanes = new LanesMapGenerator(width, height, rng, 1);
        lanes.putBoxRoomCarvers(1);
        dungeon = lanes.generate();
        sdungeon = dungeonGenerator.generate(dungeon);

        //sdungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        //sdungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon)));
        System.out.println(dungeonGenerator);

        System.out.println("------------------------------------------------------------");

        dungeonGenerator = new DungeonGenerator(width, height, rng);
        //dungeonGenerator.addDoors(15, false);
        //dungeonGenerator.addWater(20);
        //dungeonGenerator.addGrass(10);
        rng.setState(0xf00dd00dL);
        lanes = new LanesMapGenerator(width, height, rng, 2);
        lanes.putBoxRoomCarvers(1);
        dungeon = lanes.generate();
        sdungeon = dungeonGenerator.generate(dungeon);

        //sdungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        //sdungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon)));
        System.out.println(dungeonGenerator);

        System.out.println("------------------------------------------------------------");

        dungeonGenerator = new DungeonGenerator(width, height, rng);
        //dungeonGenerator.addDoors(15, false);
        //dungeonGenerator.addWater(20);
        //dungeonGenerator.addGrass(10);
        rng.setState(0xf00dd00dL);
        lanes = new LanesMapGenerator(width, height, rng, 3);
        lanes.putBoxRoomCarvers(1);
        dungeon = lanes.generate();
        sdungeon = dungeonGenerator.generate(dungeon);

        //sdungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        //sdungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon)));
        System.out.println(dungeonGenerator);

        System.out.println("------------------------------------------------------------");
        */
        /*
        rng.setState(2252637788195L);
        SerpentDeepMapGenerator deepSerpent = new SerpentDeepMapGenerator(width, height, depth, rng, 0.15);
        deepSerpent.putWalledBoxRoomCarvers(2);
        deepSerpent.putWalledRoundRoomCarvers(2);
        deepSerpent.putCaveCarvers(3);
        char[][][] map3D = deepSerpent.generate();
        DungeonGenerator[] gens = new DungeonGenerator[depth];
        for (int i = 0; i < depth; i++) {
            gens[i] = new DungeonGenerator(width, height, rng);
            gens[i].addWater(rng.nextInt(25));
            gens[i].addGrass(rng.nextInt(15));
            gens[i].addBoulders(rng.nextInt(30));
            gens[i].addDoors(rng.between(4, 10), false);
            gens[i].generateRespectingStairs(map3D[i]);
            gens[i].setDungeon(DungeonUtility.doubleWidth(
                    DungeonUtility.hashesToLines(gens[i].getDungeon(), true)));
            System.out.println(gens[i]);
            System.out.print  ("------------------------------------------------------------");
            System.out.print  ("------------------------------------------------------------");
            System.out.print  ("------------------------------------------------------------");
            System.out.println("------------------------------------------------------------");

        }

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
            rng.setState(0xFEEEEEEEEEL);
            serpent = new SerpentMapGenerator(width, height, rng, 0.2);
            serpent.putWalledBoxRoomCarvers(5);
            serpent.putWalledRoundRoomCarvers(3);
            serpent.putCaveCarvers(4);
            map = serpent.generate();
            env = serpent.getEnvironment();
            tdg.generate(map, env);
            //RNG rand = new RNG();
            //tdg.generate(rand.getRandomElement(TilesetType.values()));
            sdungeon = GwtCompatibility.copy2D(tdg.getDungeon());
            sdungeon[tdg.stairsUp.x][tdg.stairsUp.y] = '<';
            sdungeon[tdg.stairsDown.x][tdg.stairsDown.y] = '>';
            //System.out.println(tdg);
            tdg.setDungeon(DungeonUtility.hashesToLines(sdungeon, true));
            System.out.println(tdg);
        }

        sdg = new SectionDungeonGenerator(width, height, rng);
        sdg.addDoors(12, false);
        //tdg.addWater(SectionDungeonGenerator.CAVE, 13);
        //tdg.addBoulders(SectionDungeonGenerator.ALL, 13);
        sdg.addWater(SectionDungeonGenerator.CAVE, 9);
        sdg.addMaze(30);
        sdg.addLake(10, '£', '¢');
        rng.setState(0xFEEEEEEEEEL);
        serpent = new SerpentMapGenerator(width, height, rng, 0.2);
        serpent.putWalledBoxRoomCarvers(5);
        serpent.putWalledRoundRoomCarvers(3);
        serpent.putCaveCarvers(4);
        map = serpent.generate();
        env = serpent.getEnvironment();
        sdg.generate(map, env);
        //RNG rand = new RNG();
        //tdg.generate(rand.getRandomElement(TilesetType.values()));
        sdungeon = GwtCompatibility.copy2D(sdg.getDungeon());
        sdungeon[sdg.stairsUp.x][sdg.stairsUp.y] = '<';
        sdungeon[sdg.stairsDown.x][sdg.stairsDown.y] = '>';
        //System.out.println(sdg);
        sdg.setDungeon(DungeonUtility.hashesToLines(sdungeon, true));
        System.out.println(sdg);
        */
    }
    // Generates a preview webpage when given the right images.
    // See the following for generated images with different parameters (scroll down and right to see the map)
    // http://tommyettinger.github.io/home/PixVoxel/dungeon/dungeon.html   (DungeonGenerator)
    // http://tommyettinger.github.io/home/PixVoxel/dungeon/serpent.html   (SerpentMapGenerator)
    // http://tommyettinger.github.io/home/PixVoxel/dungeon/occupied.html  (SectionDungeonGenerator with people)
    // The assets are CC0 licensed (effectively public domain), made by me (Tommy Ettinger). If you intend to use
    // them in a project, maybe ask to see if there's a newer version already made or in progress.
    public static void mainAlt(String[] args ) {
        RNG rng = new RNG(2252637788195L);
        SectionDungeonGenerator dungeonGenerator = new SectionDungeonGenerator(120, 120, rng);
        dungeonGenerator.addDoors(15, true);
        dungeonGenerator.addWater(SectionDungeonGenerator.CAVE, 15);
        dungeonGenerator.addGrass(SectionDungeonGenerator.CAVE, 15);
        dungeonGenerator.addBoulders(SectionDungeonGenerator.ALL, 6);
        dungeonGenerator.addLake(20);
        SerpentMapGenerator serpent = new SerpentMapGenerator(120, 120, rng, 0.1);
        serpent.putWalledBoxRoomCarvers(3);
        serpent.putWalledRoundRoomCarvers(1);
        serpent.putCaveCarvers(6);
        //dungeonGenerator.generate(serpent.generate(), serpent.getEnvironment());
        dungeonGenerator.generate();

        dungeonGenerator.setDungeon(
                DungeonUtility.hashesToLines(dungeonGenerator.getDungeon(), true));
        char[][] iso = dungeonGenerator.getDungeon();
        int[][] water = new int[120][120];
        for (int i = 0; i < 120; i++)
        {
            for (int j = 0; j < 120; j++)
            {
                water[i][j] = -1;
            }
        }
        boolean even = true;
        StringBuilder sb = new StringBuilder(64000);
        String[] people = new String[]{
                "palette0_Hero_Chain_Bow_Iso.gif",
                "palette0_Hero_Chain_Dagger_Iso.gif",
                "palette0_Hero_Chain_Mace_Iso.gif",
                "palette0_Hero_Chain_Staff_Iso.gif",
                "palette0_Hero_Chain_Sword_Iso.gif",
                "palette0_Hero_Leather_Bow_Iso.gif",
                "palette0_Hero_Leather_Dagger_Iso.gif",
                "palette0_Hero_Leather_Mace_Iso.gif",
                "palette0_Hero_Leather_Staff_Iso.gif",
                "palette0_Hero_Leather_Sword_Iso.gif",
                "palette0_Hero_Plate_Bow_Iso.gif",
                "palette0_Hero_Plate_Dagger_Iso.gif",
                "palette0_Hero_Plate_Mace_Iso.gif",
                "palette0_Hero_Plate_Robe_Iso.gif",
                "palette0_Hero_Plate_Staff_Iso.gif",
                "palette0_Hero_Plate_Sword_Iso.gif",
                "palette0_Hero_Robe_Bow_Iso.gif",
                "palette0_Hero_Robe_Dagger_Iso.gif",
                "palette0_Hero_Robe_Staff_Iso.gif",
                "palette0_Hero_Robe_Sword_Iso.gif"
        };
        for(int row = 0, sx = -59, sy = 59; row < 240; ++row, even = (row % 2 == 0), sx += (even) ? 1 : 0, sy += (!even) ? 1 : 0)
        {
            sb.append("<div class=\"row\">\n");
            for(int col = 0; col < 120; col++)
            {
                int x = sx + col;
                int y = sy - col;
                if(x < 0 || y < 0 || x > 119 || y > 119)
                {
                    sb.append("<div class=\"isotile\"></div>\n");
                }
                else
                {
                    switch (iso[x][y])
                    {
                        case '.':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Floor_Huge_face" + rng.nextInt(4) + "_0.png\" />");
                            if(rng.nextInt(120) == 0) {
                                sb.append("<img class=\"ppl\" src=\"dungeon/");
                                sb.append(rng.getRandomElement(people));
                                sb.append("\" />");
                            }
                            sb.append("</div>\n");
                            break;
                        case '"':
                        case ':':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette47_Grass_Huge_face" + rng.nextInt(4) + "_0.png\" /></div>\n");
                            break;
                        case '#':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Boulder_Huge_face" + rng.nextInt(4) + "_0.png\" /></div>\n");
                            break;
                        case '~':
                        case ',':
                            water[x][y] = rng.nextInt(16);
                            if(water[x][y-1] > -1) water[x][y] = (water[x][y] & 14) | ((water[x][y-1] & 4) >> 2);
                            if(water[x][y+1] > -1) water[x][y] = (water[x][y] & 11) | ((water[x][y+1] & 1) * 4);
                            if(water[x-1][y] > -1) water[x][y] = (water[x][y] & 7)  | ((water[x-1][y] & 2) * 4);
                            if(water[x+1][y] > -1) water[x][y] = (water[x][y] & 13) | ((water[x+1][y] & 8) >> 2);

                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette0_Water_Huge_face0_" + (Integer.toHexString(water[x][y])) + ".png\" /></div>\n");
                            break;
                        case '│':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Straight_Huge_face" + (rng.nextInt(2) * 2) + "_0.png\" /></div>\n");
                            break;
                        case '─':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Straight_Huge_face" + (rng.nextInt(2) * 2 + 1) + "_0.png\" /></div>\n");
                            break;

                        case '┌':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face2_0.png\" /></div>\n");
                            break;
                        case '┐':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face3_0.png\" /></div>\n");
                            break;
                        case '└':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face1_0.png\" /></div>\n");
                            break;
                        case '┘':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face0_0.png\" /></div>\n");
                            break;

                        case '┤':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face0_0.png\" /></div>\n");
                            break;
                        case '┴':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face1_0.png\" /></div>\n");
                            break;
                        case '├':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face2_0.png\" /></div>\n");
                            break;
                        case '┬':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face3_0.png\" /></div>\n");
                            break;

                        case '┼':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Cross_Huge_face" + rng.nextInt(4) + "_0.png\" /></div>\n");
                            break;

                        case '+':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Door_Closed_Huge_face" + (rng.nextInt(2) * 2) + "_0.png\" /></div>\n");
                            break;
                        case '/':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Door_Closed_Huge_face" + (rng.nextInt(2) * 2 + 1) + "_0.png\" /></div>\n");
                            break;

                        case ' ':
                            sb.append("<div class=\"isotile\"></div>\n");
                            break;
                        default:
                            System.out.println("BAD GLYPH: " + iso[x][y] + " AT X: " + x + ", Y: " + y);
                    }
                }
            }
            sb.append("</div>\n");
        }
        try {
            FileWriter fw = new FileWriter("data.html");
            fw.write(sb.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
