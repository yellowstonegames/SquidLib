package squidpony.examples;

import squidpony.ArrayTools;
import squidpony.squidai.WaypointPathfinder;
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
 * Sample output: <pre>
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
 * </pre>
 * Created by Tommy Ettinger on 4/8/2015.
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
public class DungeonGeneratorTest {
    public static int width = 110, height = 70, depth = 16;

    public static void main(String[] args) {
        //seed is, in base 36, the number SQUIDLIB
        StatefulRNG rng = new StatefulRNG(2252637788195L);
        System.out.println("MixedGenerator");
        DungeonGenerator dungeonGenerator = new DungeonGenerator(width, height, rng);
        char[][] dungeon;
        dungeonGenerator.addDoors(15, true);
        dungeonGenerator.addWater(15);
        dungeonGenerator.addGrass(10);
        dungeonGenerator.addBoulders(5);
        dungeonGenerator.addTraps(2);
        MixedGenerator mix = new MixedGenerator(width, height, rng);
        mix.putCaveCarvers(1);
        mix.putBoxRoomCarvers(9);
        mix.putRoundRoomCarvers(7);
        dungeonGenerator.generate(mix.generate());
        //dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);
        dungeon = dungeonGenerator.getDungeon();
        dungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        dungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(
                DungeonUtility.hashesToLines(dungeon, true));
        System.out.println(dungeonGenerator);
        System.out.println();
        dungeonGenerator.setDungeon(DungeonUtility.linesToHashes(dungeonGenerator.getDungeon()));
        System.out.println(dungeonGenerator);
        System.out.println("------------------------------------------------------------");
        System.out.println("SerpentMapGenerator\n");


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
        dungeonGenerator.generate(ArrayTools.copy(map));

        char[][] sdungeon = DungeonUtility.closeDoors(dungeonGenerator.getDungeon());
        sdungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        sdungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon)));
        System.out.println(dungeonGenerator);
        System.out.println("------------------------------------------------------------");
        System.out.println("SerpentMapGenerator with chokepoint detection\n");

        dungeonGenerator.clearEffects();
        dungeonGenerator.generate(map);
        OrderedSet<Coord> chokepoints = new WaypointPathfinder(map, Radius.DIAMOND, rng).getWaypoints();
        for(Coord c : chokepoints) {
            map[c.x][c.y] = '*';
        }
        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(map)));
        System.out.println(dungeonGenerator);

        System.out.println("------------------------------------------------------------");
        System.out.println("SerpentMapGenerator with LAVA\n");
        rng.setState(2252637788195L);
        SectionDungeonGenerator sdg = new SectionDungeonGenerator(width, height, rng);
        sdg.addDoors(12, false);
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
        sdungeon = ArrayTools.copy(sdg.getDungeon());
        sdungeon[sdg.stairsUp.x][sdg.stairsUp.y] = '<';
        sdungeon[sdg.stairsDown.x][sdg.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon, true)));
        System.out.println(dungeonGenerator);

        System.out.println("SerpentMapGenerator with LAVA and Placement info\n");

        for (OrderedSet<Coord> lhs : sdg.placement.getAlongStraightWalls()) {
            for (Coord c : lhs) {
                sdungeon[c.x][c.y] = '}';
            }
        }

        for (OrderedSet<Coord> lhs : sdg.placement.getCorners()) {
            for (Coord c : lhs) {
                sdungeon[c.x][c.y] = 'º';
            }
        }

        for (OrderedSet<Coord> lhs : sdg.placement.getCenters()) {
            for (Coord c : lhs) {
                sdungeon[c.x][c.y] = '$';
            }
        }

        for (Coord c : sdg.placement.getHidingPlaces(Radius.CIRCLE, 8)) {
            sdungeon[c.x][c.y] = 'h';
        }
        // Just a little fun with Java that uses no alphanumerics other than keywords to express "Hello, World!"
        //char[] __ = {'$' << (',' ^ '-'), '`' | ('-' - '('), '$' << (',' ^ '-') | '$', '$' << (',' ^ '-') | '$', '/' + '@', ',', ' ', '(' + '/', '/' + '@', '(' + '%' + '%', '$' << (',' ^ '-') | '$', '*' + ':', '!'};

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
        System.out.println("OrganicMapGenerator\n");

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
        char[][] dungeon2 = GwtCompatibility.fill('#', width, height);
        for(Rectangle rect : rectangles)
        {
            if(rng.nextDouble() < 0.1) continue;
            center = Rectangle.Utils.center(rect);
            nh = (int)(rect.getHeight() * (rng.nextDouble(1.5) + 1.3));
            nw = (int)(rect.getWidth() * (rng.nextDouble(1.5) + 1.3));
            nx = Math.max(1, Math.min(width - 2, center.x - nw/2));
            ny = Math.max(1, Math.min(height - 2, center.y - nh/2));
            GwtCompatibility.insert(DungeonUtility.wallWrap(GwtCompatibility.fill('.', nw, nh)),
                    dungeon2, nx, ny);
        }
        DungeonUtility.wallWrap(dungeon2);

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(dungeon2, true)));
        System.out.println(dungeonGenerator);
        System.out.println("------------------------------------------------------------");
        */
        System.out.println("DenseRoomMapGenerator\n");
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

        System.out.println("ModularMapGenerator\n");
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

        System.out.println("FlowingCaveGenerator\n");
        rng.setState(2252637788195L);
        FlowingCaveGenerator flow = new FlowingCaveGenerator(width, height, TilesetType.DEFAULT_DUNGEON, rng);
        dungeonGenerator.clearEffects();
        dungeonGenerator.addWater(10);
        dungeonGenerator.addGrass(12);
        dungeon = dungeonGenerator.generate(flow.generate(TilesetType.DEFAULT_DUNGEON));
        //dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);
        dungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        dungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(
                DungeonUtility.hashesToLines(dungeon, true));
        System.out.println(dungeonGenerator);
        System.out.println();
        dungeonGenerator.setDungeon(DungeonUtility.linesToHashes(dungeonGenerator.getDungeon()));
        System.out.println(dungeonGenerator);
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
            sdungeon = GwtCompatibility.copy(tdg.getDungeon());
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
        sdungeon = GwtCompatibility.copy(sdg.getDungeon());
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
    public static void mainAlt(String[] args) {
        RNG rng = new RNG(2252637788195L);
        SectionDungeonGenerator dungeonGenerator = new SectionDungeonGenerator(width, width, rng);
        dungeonGenerator.addDoors(15, true);
        dungeonGenerator.addWater(SectionDungeonGenerator.ALL, 12);
        dungeonGenerator.addGrass(SectionDungeonGenerator.ALL, 8);
        dungeonGenerator.addBoulders(SectionDungeonGenerator.ALL, 4);
        //dungeonGenerator.addMaze(4);
        dungeonGenerator.addLake(15);
        /*SerpentMapGenerator serpent = new SerpentMapGenerator(width, width, rng, 0.12);
        serpent.putWalledBoxRoomCarvers(4);
        serpent.putWalledRoundRoomCarvers(2);
        serpent.putCaveCarvers(5);
        */
        // /dungeonGenerator.generate(serpent.generate(), serpent.getEnvironment());

        char[][] iso = DungeonUtility.hashesToLines(dungeonGenerator.generate(), true);
        int[][] water = new int[width][width];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                water[i][j] = -1;
            }
        }
        boolean even = true;
        StringBuilder sb = new StringBuilder(64000);
        String[] people = new String[]{
                "palette0_Hero_Chain_Bow_Attack.gif",
                "palette0_Hero_Chain_Crossbow_Attack.gif",
                "palette0_Hero_Chain_Dagger_Attack.gif",
                "palette0_Hero_Chain_Mace_Attack.gif",
                "palette0_Hero_Chain_Staff_Attack.gif",
                "palette0_Hero_Chain_Sword_Attack.gif",
                "palette0_Hero_Leather_Bow_Attack.gif",
                "palette0_Hero_Leather_Crossbow_Attack.gif",
                "palette0_Hero_Leather_Dagger_Attack.gif",
                "palette0_Hero_Leather_Mace_Attack.gif",
                "palette0_Hero_Leather_Staff_Attack.gif",
                "palette0_Hero_Leather_Sword_Attack.gif",
                "palette0_Hero_Plate_Bow_Attack.gif",
                "palette0_Hero_Plate_Crossbow_Attack.gif",
                "palette0_Hero_Plate_Dagger_Attack.gif",
                "palette0_Hero_Plate_Mace_Attack.gif",
                "palette0_Hero_Plate_Staff_Attack.gif",
                "palette0_Hero_Plate_Sword_Attack.gif",
                "palette0_Hero_Robe_Bow_Attack.gif",
                "palette0_Hero_Robe_Crossbow_Attack.gif",
                "palette0_Hero_Robe_Dagger_Attack.gif",
                "palette0_Hero_Robe_Mace_Attack.gif",
                "palette0_Hero_Robe_Staff_Attack.gif",
                "palette0_Hero_Robe_Sword_Attack.gif",
                "palette6_Heroine_Chain_Bow_Attack.gif",
                "palette6_Heroine_Chain_Crossbow_Attack.gif",
                "palette6_Heroine_Chain_Dagger_Attack.gif",
                "palette6_Heroine_Chain_Mace_Attack.gif",
                "palette6_Heroine_Chain_Staff_Attack.gif",
                "palette6_Heroine_Chain_Sword_Attack.gif",
                "palette6_Heroine_Leather_Bow_Attack.gif",
                "palette6_Heroine_Leather_Crossbow_Attack.gif",
                "palette6_Heroine_Leather_Dagger_Attack.gif",
                "palette6_Heroine_Leather_Mace_Attack.gif",
                "palette6_Heroine_Leather_Staff_Attack.gif",
                "palette6_Heroine_Leather_Sword_Attack.gif",
                "palette6_Heroine_Plate_Bow_Attack.gif",
                "palette6_Heroine_Plate_Crossbow_Attack.gif",
                "palette6_Heroine_Plate_Dagger_Attack.gif",
                "palette6_Heroine_Plate_Mace_Attack.gif",
                "palette6_Heroine_Plate_Staff_Attack.gif",
                "palette6_Heroine_Plate_Sword_Attack.gif",
                "palette6_Heroine_Robe_Bow_Attack.gif",
                "palette6_Heroine_Robe_Crossbow_Attack.gif",
                "palette6_Heroine_Robe_Dagger_Attack.gif",
                "palette6_Heroine_Robe_Mace_Attack.gif",
                "palette6_Heroine_Robe_Staff_Attack.gif",
                "palette6_Heroine_Robe_Sword_Attack.gif"
        };
        for (int y = 0; y < width; y++) {
            sb.append("<div class=\"row\">\n");
            for (int x = 0; x < width; x++) {
                sb.append("<div class=\"orthotile\">");
                switch (iso[x][y]) {
                    case '.':
                        sb.append("<img src=\"dungeon/palette13_Floor_Ortho_face").append(rng.nextInt(4)).append("_0.png\" />");
                        if (rng.nextInt(15) == 0) {
                            sb.append("<img class=\"ppl\" src=\"dungeon/");
                            sb.append(rng.getRandomElement(people));
                            sb.append("\" />");
                        }
                        break;
                    case '"':
                    case ':':
                        sb.append("<img src=\"dungeon/palette14_Grass_Ortho_face").append(rng.nextInt(4)).append("_0.png\" />");
                        break;
                    case '#':
                        sb.append("<img src=\"dungeon/palette13_Boulder_Ortho_face").append(rng.nextInt(4)).append("_0.png\" />");
                        break;
                    case '~':
                    case ',':
                        water[x][y] = rng.nextInt(16);
                        if (water[x][y - 1] > -1) water[x][y] = (water[x][y] & 14) | ((water[x][y - 1] & 4) >> 2);
                        if (water[x][y + 1] > -1) water[x][y] = (water[x][y] & 11) | ((water[x][y + 1] & 1) << 2);
                        if (water[x - 1][y] > -1) water[x][y] = (water[x][y] & 7) | ((water[x - 1][y] & 2) << 2);
                        if (water[x + 1][y] > -1) water[x][y] = (water[x][y] & 13) | ((water[x + 1][y] & 8) >> 2);

                        sb.append("<img src=\"dungeon/palette15_Water_Ortho_face0_").append(Integer.toHexString(water[x][y])).append(".png\" />");
                        break;
                    case '│':
                        sb.append("<img src=\"dungeon/palette13_Wall_Straight_Ortho_face").append(rng.nextInt(2) * 2 + 1).append("_0.png\" />");
                        break;
                    case '─':
                        sb.append("<img src=\"dungeon/palette13_Wall_Straight_Ortho_face").append(rng.nextInt(2) * 2).append("_0.png\" />");
                        break;

                    case '┌':
                        sb.append("<img src=\"dungeon/palette13_Wall_Corner_Ortho_face1_0.png\" />");
                        break;
                    case '┐':
                        sb.append("<img src=\"dungeon/palette13_Wall_Corner_Ortho_face2_0.png\" />");
                        break;
                    case '└':
                        sb.append("<img src=\"dungeon/palette13_Wall_Corner_Ortho_face0_0.png\" />");
                        break;
                    case '┘':
                        sb.append("<img src=\"dungeon/palette13_Wall_Corner_Ortho_face3_0.png\" />");
                        break;

                    case '┤':
                        sb.append("<img src=\"dungeon/palette13_Wall_Tee_Ortho_face3_0.png\" />");
                        break;
                    case '┴':
                        sb.append("<img src=\"dungeon/palette13_Wall_Tee_Ortho_face0_0.png\" />");
                        break;
                    case '├':
                        sb.append("<img src=\"dungeon/palette13_Wall_Tee_Ortho_face1_0.png\" />");
                        break;
                    case '┬':
                        sb.append("<img src=\"dungeon/palette13_Wall_Tee_Ortho_face2_0.png\" />");
                        break;

                    case '┼':
                        sb.append("<img src=\"dungeon/palette13_Wall_Cross_Ortho_face").append(rng.nextInt(4)).append("_0.png\" />");
                        break;

                    case '+':
                        sb.append("<img src=\"dungeon/palette13_Door_Closed_Ortho_face").append(rng.nextInt(2) * 2 + 1).append("_0.png\" />");
                        break;
                    case '/':
                        sb.append("<img src=\"dungeon/palette13_Door_Closed_Ortho_face").append(rng.nextInt(2) * 2).append("_0.png\" />");
                        break;
                }

                sb.append("</div>\n");
            }
            sb.append("</div>\n");
        }
        try {
            FileWriter fw = new FileWriter("index.html");
            fw.write(sb.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

//        for(int row = 0, sx = 1 - (width>>1), sy = (width>>1) - 1; row < (width<<1); ++row, even = (row % 2 == 0), sx += (even) ? 1 : 0, sy += (!even) ? 1 : 0)
//        {
//            sb.append("<div class=\"row\">\n");
//            for(int col = 0; col < width; col++)
//            {
//                int x = sx + col;
//                int y = sy - col;
//                if(x < 0 || y < 0 || x >= width || y >= width)
//                {
//                    sb.append("<div class=\"isotile\"></div>\n");
//                }
//                else
//                {
//                    switch (iso[x][y])
//                    {
//                        case '.':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Floor_Huge_face" + rng.nextInt(4) + "_0.png\" />");
//                            if(rng.nextInt(40) == 0) {
//                                sb.append("<img class=\"ppl\" src=\"dungeon/");
//                                sb.append(rng.getRandomElement(people));
//                                sb.append("\" />");
//                            }
//                            sb.append("</div>\n");
//                            break;
//                        case '"':
//                        case ':':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette47_Grass_Huge_face" + rng.nextInt(4) + "_0.png\" /></div>\n");
//                            break;
//                        case '#':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Boulder_Huge_face" + rng.nextInt(4) + "_0.png\" /></div>\n");
//                            break;
//                        case '~':
//                        case ',':
//                            water[x][y] = rng.nextInt(16);
//                            if(water[x][y-1] > -1) water[x][y] = (water[x][y] & 14) | ((water[x][y-1] & 4) >> 2);
//                            if(water[x][y+1] > -1) water[x][y] = (water[x][y] & 11) | ((water[x][y+1] & 1) << 2);
//                            if(water[x-1][y] > -1) water[x][y] = (water[x][y] & 7)  | ((water[x-1][y] & 2) << 2);
//                            if(water[x+1][y] > -1) water[x][y] = (water[x][y] & 13) | ((water[x+1][y] & 8) >> 2);
//
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette0_Water_Huge_face0_" + (Integer.toHexString(water[x][y])) + ".png\" /></div>\n");
//                            break;
//                        case '│':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Straight_Huge_face" + (rng.nextInt(2) * 2) + "_0.png\" /></div>\n");
//                            break;
//                        case '─':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Straight_Huge_face" + (rng.nextInt(2) * 2 + 1) + "_0.png\" /></div>\n");
//                            break;
//
//                        case '┌':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face2_0.png\" /></div>\n");
//                            break;
//                        case '┐':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face3_0.png\" /></div>\n");
//                            break;
//                        case '└':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face1_0.png\" /></div>\n");
//                            break;
//                        case '┘':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face0_0.png\" /></div>\n");
//                            break;
//
//                        case '┤':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face0_0.png\" /></div>\n");
//                            break;
//                        case '┴':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face1_0.png\" /></div>\n");
//                            break;
//                        case '├':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face2_0.png\" /></div>\n");
//                            break;
//                        case '┬':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face3_0.png\" /></div>\n");
//                            break;
//
//                        case '┼':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Cross_Huge_face" + rng.nextInt(4) + "_0.png\" /></div>\n");
//                            break;
//
//                        case '+':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Door_Closed_Huge_face" + (rng.nextInt(2) * 2) + "_0.png\" /></div>\n");
//                            break;
//                        case '/':
//                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Door_Closed_Huge_face" + (rng.nextInt(2) * 2 + 1) + "_0.png\" /></div>\n");
//                            break;
//
//                        case ' ':
//                            sb.append("<div class=\"isotile\"></div>\n");
//                            break;
//                        default:
//                            System.out.println("BAD GLYPH: " + iso[x][y] + " AT X: " + x + ", Y: " + y);
//                    }
//                }
//            }
//            sb.append("</div>\n");
//        }
//        try {
//            FileWriter fw = new FileWriter("index.html");
//            fw.write(sb.toString());
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}