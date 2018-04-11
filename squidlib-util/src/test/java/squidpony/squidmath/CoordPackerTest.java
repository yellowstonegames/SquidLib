package squidpony.squidmath;

import org.junit.Test;
import squidpony.squidai.AimLimit;
import squidpony.squidai.Reach;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.SerpentMapGenerator;
import squidpony.squidgrid.mapping.styled.TilesetType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.*;
import static squidpony.squidmath.CoordPacker.*;


/**
 * Created by Tommy Ettinger on 10/1/2015.
 */
public class CoordPackerTest {

    static {
        CoordPacker.init();
    }
    public static short[] dataCross = unionPacked(rectangle(25, 2, 14, 60), rectangle(2, 25, 60, 14));
    public static short[] dataCross2 = unionPacked(rectangle(24, 2, 16, 60), rectangle(2, 24, 60, 16));
    public static StatefulRNG srng = new StatefulRNG(0x1337BEEF);
    @Test
    public void testBasics() {
        //printPacked(dataCross, 64, 64);
        assertArrayEquals(dataCross, unionPacked(rectangle(25, 2, 14, 60), rectangle(2, 25, 60, 14)));
        short[] singleNegative = negatePacked(unionPacked(rectangle(25, 2, 14, 60), rectangle(2, 25, 60, 14))),
                doubleNegative = negatePacked(singleNegative);
        assertArrayEquals(dataCross, doubleNegative);
    }

    public static int FOV_RANGE = 12;
    public static Radius RADIUS = Radius.SQUARE;

    public void printBits16(int n) {
        for (int i = 0x8000; i > 0; i >>= 1)
            System.out.print((n & i) > 0 ? 1 : 0);
    }

    public void printBits32(int n) {
        for (int i = 1 << 31; i != 0; i >>>= 1)
            System.out.print((n & i) != 0 ? 1 : 0);
    }

    public long arrayMemoryUsage(int length, long bytesPerItem)
    {
        return (((bytesPerItem * length + 12 - 1) / 8) + 1) * 8L;
    }
    public long arrayMemoryUsage2D(int xSize, int ySize, long bytesPerItem)
    {
        return arrayMemoryUsage(xSize, (((bytesPerItem * ySize + 12 - 1) / 8) + 1) * 8L);
    }
    public int arrayMemoryUsageJagged(short[][] arr)
    {
        int ctr = 0;
        for (int i = 0; i < arr.length; i++) {
            ctr += arrayMemoryUsage(arr[i].length, 2);
        }
        return (((ctr + 12 - 1) / 8) + 1) * 8;
    }

    @Test
    public void testBraille()
    {
        String encoded = encodeBraille(dataCross2);
        //System.out.println(encoded);
        //System.out.println("short array length: " + dataCross2.length);
        //System.out.println("encoded length: " + encoded.length());
        short[] decoded = decodeBraille(encoded);
        assertArrayEquals(dataCross2, decoded);

    }

    @Test
    public void testHilbertCurve() {
        assertEquals(0, posToHilbert(0, 0));
        assertEquals(21845, posToHilbert(255, 0));
        assertEquals(65535, posToHilbert(0, 255));
        assertEquals(43690, posToHilbert(255, 255));

        assertEquals(43690, coordToHilbert(Coord.get(255, 255)));
        assertEquals(posToHilbert(255, 255), coordToHilbert(Coord.get(255, 255)));
        assertEquals(Coord.get(255, 255), hilbertToCoord(coordToHilbert(Coord.get(255, 255))));
    }
    //@Test
    public void testHilbertCurve3D() {
        for(int i : new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,31,32,33,63,64,255,256,4092,4093,4094,4095})
            System.out.println("index " + i + ", x:" + hilbert3X[i] +
                    ", y:" + hilbert3Y[i] +
                    ", z:" + hilbert3Z[i]);
    }
    @Test
    public void testMooreCurve3D() {
        int x0 = getXMoore3D(0, 3), y0 = getYMoore3D(0, 3), z0 = getZMoore3D(0, 3), x1, y1, z1;
        for (int s = 1; s < 16 * 16 * 8 * 3; s++) {
            x1 = getXMoore3D(s, 3);
            y1 = getYMoore3D(s, 3);
            z1 = getZMoore3D(s, 3);
            assertEquals(1, Math.abs(x0 - x1) + Math.abs(y0 - y1) + Math.abs(z0 - z1));
            x0 = x1;
            y0 = y1;
            z0 = z1;
        }
    }
    //@Test
    public void testMooreCurve3DOld() {
        for (int s = 0; s < 12; s++) {

            for (int i0 : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 31, 32, 33, 63, 64, 255, 256, 511, 512, 1023, 1024, 4092, 4093, 4094, 4095}) {
                int i = i0 + s * 4096;
                System.out.println("index " + i + ", sector " + (i >> 12) + ", x:" + getXMoore3D(i, 3) +
                        ", y:" + getYMoore3D(i, 3) +
                        ", z:" + getZMoore3D(i, 3));
            }
        }
    }
    //@Test
    public void testMooreCurve() {
        for (int i = 0; i < 256; i++) {
            System.out.println("index " + i + "x:" + mooreX[i] + ", y:" + mooreY[i] +
            ", dist:" + mooreDistances[mooreX[i] + (mooreY[i] << 4)]);
        }
    }

    @Test
    public void testTranslate() {
        short[] packed = new short[]{0, 4}, squashed = new short[]{0, 1};
        short[] translated = translate(packed, -2, -2, 60, 60);
        assertArrayEquals(squashed, translated);


        /*
        false true
        true  false
         */
        /* MOVE OVER, X 1, limit width to 2
        false true
        false true
         */
        boolean[][] grid = new boolean[][]{new boolean[]{false, true}, new boolean[]{true, false}};
        boolean[][] grid2 = new boolean[][]{new boolean[]{false, false}, new boolean[]{true, true}};
        short[] packed2 = pack(grid), packed3 = pack(grid2);

        short[] translated2 = translate(packed2, 1, 0, 2, 2);
        assertArrayEquals(packed3, translated2);
        short[] crossZeroTranslated = translate(dataCross, 0, 0, 64, 64);
        short[] crossTranslated = translate(dataCross, 1, 1, 64, 64);
        short[] crossUnTranslated = translate(crossTranslated, -1, -1, 64, 64);

        assertArrayEquals(dataCross, crossZeroTranslated);
        assertArrayEquals(dataCross, crossUnTranslated);

        short[] crossBox = translate(translate(dataCross, 25, 25, 64, 64), -50, -50, 64, 64);
        //printPacked(crossBox, 64, 64);
        assertArrayEquals(crossBox, rectangle(14, 14));
    }

    @Test
    public void testUnion() {
        short[] union = unionPacked(new short[]{300, 5, 6, 8, 2, 4}, new short[]{290, 12, 9, 1});
        // 300, 5, 6, 8, 2, 4
        // 290, 12, 9, 1
        // =
        // 290, 15, 6, 8, 2, 4
        /*
        System.out.println("Union: ");
        for (int i = 0; i < union.length; i++) {
            System.out.print(union[i] + ", ");
        }
        System.out.println();
        */
        assertArrayEquals(new short[]{290, 15, 6, 8, 2, 4}, union);

        union = unionPacked(new short[]{300, 5, 6, 8, 2, 4}, new short[]{290, 10, 10, 1});
        /*
        System.out.println("Union: ");
        for (int i = 0; i < union.length; i++) {
            System.out.print(union[i] + ", ");
        }
        System.out.println();
        */
        assertArrayEquals(new short[]{290, 15, 5, 9, 2, 4}, union);

        short[] intersect = intersectPacked(new short[]{300, 5, 6, 8, 2, 4}, new short[]{290, 12, 9, 1});
        // 300, 5, 6, 8, 2, 4
        // 290, 12, 9, 1
        // =
        // 300, 2, 9, 1
        /*
        System.out.println("Intersect: ");
        for (int i = 0; i < intersect.length; i++) {
            System.out.print(intersect[i] + ", ");
        }
        System.out.println();
        */
        assertArrayEquals(new short[]{300, 2, 9, 1}, intersect);

        intersect = intersectPacked(new short[]{300, 5, 6, 8, 2, 4}, new short[]{290, 10, 11, 1});
        /*
        System.out.println("Intersect: ");
        for (int i = 0; i < intersect.length; i++) {
            System.out.print(intersect[i] + ", ");
        }
        System.out.println();
        */
        assertArrayEquals(new short[]{311, 1}, intersect);

        short[] box = translate(translate(translate(dataCross, 25, 25, 64, 64), -50, -50, 64, 64), 25, 25, 64, 64);
        assertArrayEquals(box, intersectPacked(rectangle(25, 2, 14, 60), rectangle(2, 25, 60, 14)));
        short[] minus = differencePacked(dataCross, box);
        short[] xor = xorPacked(rectangle(25, 2, 14, 60), rectangle(2, 25, 60, 14));
        assertArrayEquals(minus, xor);

    }
    @Test
    public void testExpanding() {

        /*
        StatefulRNG rng = new StatefulRNG(new LightRNG(0xAAAA2D2));

        DungeonGenerator dungeonGenerator = new DungeonGenerator(60, 60, rng);
        char[][] map = dungeonGenerator.generate();
        short[] floors = pack(map, '.');
        Coord viewer = dungeonGenerator.utility.randomCell(floors);
        FOV fov = new FOV(FOV.SHADOW);
        double[][] seen = fov.calculateFOV(DungeonUtility.generateResistances(map), viewer.x, viewer.y,
                FOV_RANGE, RADIUS);
        short[] visible = pack(seen);

        short[] fringe = fringe(visible, 1, 60, 60);
        printPacked(fringe, 60, 60);


        short[][] fringes = fringes(visible, 6, 60, 60);
        for (int i = 0; i < 6; i++) {
            printPacked(intersectPacked(fringes[i], floors), 60, 60);
        }
        */

        short[] edge = fringe(dataCross, 1, 64, 64);
        //printPacked(edge, 64, 64);
        short[] bonus = expand(dataCross, 1, 64, 64);
        //printPacked(bonus, 64, 64);
        assertArrayEquals(differencePacked(bonus, edge), dataCross);
        short[] flooded = flood(dataCross, packSeveral(Coord.get(26, 2)), 2);
        short[] manual = packSeveral(Coord.get(25, 2), Coord.get(26, 2), Coord.get(27, 2), Coord.get(28, 2),
                Coord.get(25, 3), Coord.get(26, 3), Coord.get(27, 3),
                Coord.get(26, 4));
        //printPacked(flooded, 64, 64);
        assertArrayEquals(flooded, manual);
        flooded = spill(dataCross, packOne(27, 4), 30, srng);
        //printPacked(flooded, 64, 64);
        //System.out.println(count(flooded));
/*
        flooded = spill(dataCross, packOne(27, 4), count(dataCross), srng);
        printPacked(flooded, 64, 64);
        System.out.println(count(flooded));*/
    }
    @Test
    public void testRetracting() {

        /*
        StatefulRNG rng = new StatefulRNG(new LightRNG(0xAAAA2D2));

        DungeonGenerator dungeonGenerator = new DungeonGenerator(60, 60, rng);
        char[][] map = dungeonGenerator.generate();
        short[] floors = pack(map, '.');
        Coord viewer = dungeonGenerator.utility.randomCell(floors);
        FOV fov = new FOV(FOV.SHADOW);
        double[][] seen = fov.calculateFOV(DungeonUtility.generateResistances(map), viewer.x, viewer.y,
                FOV_RANGE, RADIUS);
        short[] visible = pack(seen);

        short[] fringe = fringe(visible, 1, 60, 60);
        printPacked(fringe, 60, 60);


        short[][] fringes = fringes(visible, 6, 60, 60);
        for (int i = 0; i < 6; i++) {
            printPacked(intersectPacked(fringes[i], floors), 60, 60);
        }
        */

        short[] surf = surface(dataCross, 1, 64, 64);
        //printPacked(surf, 64, 64);
        short[] shrunk = retract(dataCross, 1, 64, 64);
        //printPacked(shrunk, 64, 64);
        assertArrayEquals(unionPacked(shrunk, surf), dataCross);
    }
    /*
    @Test
    public void testFloodRadiate()
    {
        /*
        short[] flooded = flood(dataCross, packSeveral(Coord.get(26, 2)), 2);
        short[] manual = packSeveral(Coord.get(25, 2), Coord.get(26, 2), Coord.get(27, 2), Coord.get(28, 2),
                Coord.get(25, 3), Coord.get(26, 3), Coord.get(27, 3),
                Coord.get(26, 4));
        //printPacked(flooded, 64, 64);
        assertArrayEquals(flooded, manual);
        * /
        for (int i = 10; i < 50; i++) {
            for (int j = 0; j < 10; j++) {
                short[] radiated = radiate(removeSeveralPacked(dataCross, Coord.get(28+j, i), Coord.get(27+j, i+1), Coord.get(28+j, i+1)), packOne(26, 23), 10);
                count(radiated);
            }
        }
        //printPacked(radiated, 64, 64);
    }
    */
    @Test
    public void testRadiate()
    {
        short[] groupFOVed = radiate(removeSeveralPacked(dataCross, Coord.get(30, 25), Coord.get(29, 26), Coord.get(30, 26)), packOne(26, 23), 10);
        //printPacked(groupFOVed, 64, 64);
    }

    @Test
    public void testReachable()
    {
        Reach reach = new Reach(3, 8, Radius.DIAMOND);
        short[] groupReachable = reachable(removeSeveralPacked(dataCross, Coord.get(30, 25), Coord.get(29, 26), Coord.get(30, 26)), packOne(26, 22), reach);
        //printPacked(groupReachable, 64, 64);
        reach = new Reach(3, 8, Radius.DIAMOND, AimLimit.ORTHOGONAL);
        groupReachable = reachable(removeSeveralPacked(dataCross, Coord.get(30, 25), Coord.get(29, 26), Coord.get(30, 26)), packOne(26, 22), reach);
        //printPacked(groupReachable, 64, 64);
        reach = new Reach(3, 8, Radius.DIAMOND, AimLimit.EIGHT_WAY);
        groupReachable = reachable(removeSeveralPacked(dataCross, Coord.get(30, 25), Coord.get(29, 26), Coord.get(30, 26)), packOne(26, 22), reach);
        //printPacked(groupReachable, 64, 64);
    }
    //@Test
    public void testFraction()
    {
        StatefulRNG rng = new StatefulRNG(new LightRNG(0xAAAA2D2));
        DungeonGenerator dungeonGenerator = new DungeonGenerator(60, 60, rng);
        SerpentMapGenerator serpent = new SerpentMapGenerator(60, 60, rng);
        serpent.putRoundRoomCarvers(1);
        serpent.putBoxRoomCarvers(1);
        serpent.putCaveCarvers(1);
        char[][] map = dungeonGenerator.generate(serpent.generate());
        short[] floors = pack(map, '.');
        Coord[] positions = fractionPacked(floors, 29);
        /*
        System.out.println(positions.length);
        System.out.println(count(floors));
        System.out.println(positions.length * 1.0 / count(floors));
        */
        printPacked(floors, 60, 60);
        System.out.println();
        for (int i = 0; i < positions.length; i++) {
            map[positions[i].x][positions[i].y] = '!';
        }
        dungeonGenerator.setDungeon(map);
        System.out.println(dungeonGenerator);


    }
    //Uncomment the @Test line to get a lot of printed info regarding room-finding
    //@Test
    public void testRooms()
    {
        StatefulRNG rng = new StatefulRNG(new LightRNG(0xAAAA2D2));
        DungeonGenerator dungeonGenerator = new DungeonGenerator(60, 60, rng);
        SerpentMapGenerator serpent = new SerpentMapGenerator(60, 60, rng);
        serpent.putRoundRoomCarvers(1);
        serpent.putBoxRoomCarvers(1);
        serpent.putCaveCarvers(1);
        char[][] map = dungeonGenerator.generate(serpent.generate());
        short[] floors = pack(map, '.'),
                shrunk = retract(floors, 1, 60, 60, true),
                rooms = flood(floors, shrunk, 2, false),
                corridors = differencePacked(floors, rooms),
                doors = intersectPacked(rooms, fringe(corridors, 1, 60, 60, false));

        printPacked(floors, 60, 60);
        System.out.println();
        printPacked(shrunk, 60, 60);
        System.out.println();
        printPacked(rooms, 60, 60);
        System.out.println();
        printPacked(corridors, 60, 60);
        System.out.println();
        printPacked(doors, 60, 60);
        System.out.println();
        ArrayList<short[]> separatedCorridors = split(corridors), separatedRooms = split(rooms);
        for (short[] sep : separatedRooms) {
            printPacked(sep, 60, 60);
            System.out.println();
        }
        /*
        for (short[] sep : separatedCorridors) {
            short[] someDoors = intersectPacked(rooms, fringe(sep, 1, 60, 60, false)),
                    connectedRooms = flood(rooms, someDoors, 512, false);
            printPacked(unionPacked(sep, connectedRooms), 60, 60);
            System.out.println();
        }
        */

    }


    @Test
    public void testPackOptimalParameters()
    {
        StatefulRNG rng = new StatefulRNG(new LightRNG(0xAAAA2D2));
        DungeonGenerator dungeonGenerator = new DungeonGenerator(80, 80, rng);
        dungeonGenerator.addDoors(15, true);
        dungeonGenerator.addWater(25);
        dungeonGenerator.addTraps(2);
        char[][] map = dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);

        FOV fov = new FOV();
        double[][] resMap = DungeonUtility.generateResistances(map), seen;
        short[] packed;
        boolean[][] unpacked;
        int ramPacked = 0, ramBoolean = 0, ramDouble = 0;
        Coord viewer;
        for (int t = 0; t < 100; t++) {
            viewer = dungeonGenerator.utility.randomFloor(map);
            seen = fov.calculateFOV(resMap, viewer.x, viewer.y, FOV_RANGE, RADIUS);
            packed = pack(seen);

            unpacked = unpack(packed, seen.length, seen[0].length);
            for (int i = 0; i < unpacked.length ; i++) {
                for (int j = 0; j < unpacked[i].length; j++) {
                    assertTrue((seen[i][j] > 0.0) == unpacked[i][j]);
                }
            }
            ramPacked += arrayMemoryUsage(packed.length, 2);
            ramBoolean += arrayMemoryUsage2D(seen.length, seen[0].length, 1);
            ramDouble += arrayMemoryUsage2D(seen.length, seen[0].length, 8);
        }
        //assertEquals("Packed shorts", 18, packed.length);
        //assertEquals("Unpacked doubles: ", 57600, seen.length * seen[0].length);
        /*
        System.out.println("Average Memory used by packed short[] (Appropriate):" +
                ramPacked / 100.0 + " bytes");
        System.out.println("Average Memory used by boolean[][] (Appropriate):" +
                ramBoolean / 100.0 + " bytes");
        System.out.println("Average Memory used by original double[][] (Appropriate):" +
                ramDouble / 100.0 + " bytes");
        System.out.println("Average Compression, short[] vs. boolean[][] (Appropriate):" +
                100.0 * ramPacked / ramBoolean + "%");
        System.out.println("Average Compression, short[] vs. double[][] (Appropriate):" +
                100.0 * ramPacked / ramDouble + "%");
        System.out.println("FOV Map stored for every cell, booleans, 240x240: " +
                arrayMemoryUsage2D(240, 240, arrayMemoryUsage2D(240, 240, 1)));
        System.out.println("FOV Map stored for every cell, floats, 240x240: " +
                arrayMemoryUsage2D(240, 240, arrayMemoryUsage2D(240, 240, 4)));
                */
    }

    @Test
    public void testPackPoorParameters()
    {
        StatefulRNG rng = new StatefulRNG(new LightRNG(0xAAAA2D2));
        DungeonGenerator dungeonGenerator = new DungeonGenerator(30, 70, rng);
        dungeonGenerator.addDoors(15, true);
        dungeonGenerator.addWater(25);
        dungeonGenerator.addTraps(2);
        char[][] map = dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);

        FOV fov = new FOV();
        double[][] resMap = DungeonUtility.generateResistances(map), seen;
        short[] packed;
        boolean[][] unpacked;
        int ramPacked = 0, ramBoolean = 0, ramDouble = 0;
        Coord viewer;
        for (int t = 0; t < 100; t++) {
            viewer = dungeonGenerator.utility.randomFloor(map);
            seen = fov.calculateFOV(resMap, viewer.x, viewer.y, FOV_RANGE, RADIUS);
            packed = pack(seen);

            unpacked = unpack(packed, seen.length, seen[0].length);
            for (int i = 0; i < unpacked.length ; i++) {
                for (int j = 0; j < unpacked[i].length; j++) {
                    assertTrue((seen[i][j] > 0.0) == unpacked[i][j]);
                }
            }
            ramPacked += arrayMemoryUsage(packed.length, 2);
            ramBoolean += arrayMemoryUsage2D(seen.length, seen[0].length, 1);
            ramDouble += arrayMemoryUsage2D(seen.length, seen[0].length, 8);
        }
        //assertEquals("Packed shorts", 18, packed.length);
        //assertEquals("Unpacked doubles: ", 57600, seen.length * seen[0].length);
        /*
        System.out.println("Average Memory used by packed short[] (Approaching Worst-Case):" +
                ramPacked / 100.0 + " bytes");
        System.out.println("Average Memory used by boolean[][] (Approaching Worst-Case):" +
                ramBoolean / 100.0 + " bytes");
        System.out.println("Average Memory used by original double[][] (Approaching Worst-Case):" +
                ramDouble / 100.0 + " bytes");
        System.out.println("Average Compression, short[] vs. boolean[][] (Approaching Worst-Case):" +
                100.0 * ramPacked / ramBoolean + "%");
        System.out.println("Average Compression, short[] vs. double[][] (Approaching Worst-Case):" +
                100.0 * ramPacked / ramDouble + "%");
        System.out.println("FOV Map stored for every cell, booleans, 30x70: " +
                arrayMemoryUsage2D(30, 70, arrayMemoryUsage2D(30, 70, 1)));
        System.out.println("FOV Map stored for every cell, floats, 30x70: " +
                arrayMemoryUsage2D(30, 70, arrayMemoryUsage2D(30, 70, 4)));
                */
    }
    /*
    @Test
    public void testPackPoorParameters()
    {
        StatefulRNG rng = new StatefulRNG(new LightRNG(0xAAAA2D2));
        DungeonGenerator dungeonGenerator = new DungeonGenerator(30, 70, rng);
        dungeonGenerator.addDoors(15, true);
        dungeonGenerator.addWater(25);
        dungeonGenerator.addTraps(2);
        char[][] map = dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);

        FOV fov = new FOV();
        Coord viewer = dungeonGenerator.utility.randomFloor(map);

        map[viewer.x][viewer.y] = '@';
        dungeonGenerator.setDungeon(map);
        //System.out.println(dungeonGenerator.toString());

        double[][] resMap = DungeonUtility.generateResistances(map);
        double[][] seen = fov.calculateFOV(resMap, viewer.x, viewer.y, 8, Radius.DIAMOND);
        short[] packed = pack(seen);

        assertEquals("Packed shorts", 28, packed.length);
        assertEquals("Unpacked doubles: ", 2100, seen.length * seen[0].length);
        System.out.println("Memory used by packed short[] (Approaching Worst-Case):" +
                arrayMemoryUsage(packed.length, 2) + " bytes");
        System.out.println("Memory used by boolean[][] (Approaching Worst-Case):" +
                arrayMemoryUsage2D(30, 70, 1) + " bytes");
        System.out.println("Memory used by original double[][] (Approaching Worst-Case):" +
                arrayMemoryUsage2D(30, 70, 8) + " bytes");
        System.out.println("Compression, short[] vs. boolean[][] (Approaching Worst-Case):" +
                100.0 * arrayMemoryUsage(packed.length, 2) / arrayMemoryUsage2D(30, 70, 1) + "%");
        System.out.println("Compression, short[] vs. double[][] (Approaching Worst-Case):" +
                100.0 * arrayMemoryUsage(packed.length, 2) / arrayMemoryUsage2D(30, 70, 8) + "%");

        boolean[][]unpacked = unpack(packed, seen.length, seen[0].length);
        for (int i = 0; i < unpacked.length ; i++) {
            for (int j = 0; j < unpacked[i].length; j++) {
                assertTrue((seen[i][j] > 0.0) == unpacked[i][j]);
            }
        }
    }
    */
    @Test
    public void testPackMultiOptimalParameters()
    {
        for(int FOV_RANGE = 1; FOV_RANGE < 21; FOV_RANGE++) {
            StatefulRNG rng = new StatefulRNG(new LightRNG(0xAAAA2D2));
            DungeonGenerator dungeonGenerator = new DungeonGenerator(80, 80, rng);
            dungeonGenerator.addDoors(15, true);
            dungeonGenerator.addWater(25);
            dungeonGenerator.addTraps(2);
            char[][] map = dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);
            double[][] resMap = DungeonUtility.generateResistances(map), seen;

            FOV fov = new FOV(FOV.RIPPLE);
            double[] packingLevels = generatePackingLevels(FOV_RANGE),
                    lightLevels = generateLightLevels(FOV_RANGE);
            short[][] packed;
            int ramPacked = 0, ramFloat = 0, ramDouble = 0;
            Coord viewer;
            HashSet<Double> seenValues = new HashSet<>(FOV_RANGE * 2);
            /*
            System.out.println("Packing levels at range " + FOV_RANGE + ": ");
            for (Double d : packingLevels) {
                System.out.print(d + "  ");
            }
            System.out.println();
            System.out.println("Light levels at range " + FOV_RANGE + ": ");
            for (Double d : lightLevels) {
                System.out.print(d + "  ");
            }
            System.out.println();
            */
            for (int t = 0; t < 100; t++) {
                viewer = dungeonGenerator.utility.randomFloor(map);
                seen = fov.calculateFOV(resMap, viewer.x, viewer.y, FOV_RANGE, RADIUS);
                packed = packMulti(seen, packingLevels);

                for (int j = 0; j < seen[0].length; j++) {
                    for (int i = 0; i < seen.length; i++) {
                        seenValues.add(seen[i][j]);
                    }
                }

                for (int ll = 0; ll < lightLevels.length; ll++) {
                    boolean[][] unpackedB = unpack(packed[ll], seen.length, seen[0].length);
                    for (int i = 0; i < unpackedB.length; i++) {
                        for (int j = 0; j < unpackedB[i].length; j++) {
                            assertTrue((seen[i][j] > packingLevels[ll]) == unpackedB[i][j]);
                        }
                    }
                    if (ll + 1 == lightLevels.length) {
                        assertTrue(packed[ll].length == 2);
                        assertTrue(queryPacked(packed[ll], viewer.x, viewer.y));
                    }
                }
                double[][] unpacked2 = unpackMultiDouble(packed, seen.length, seen[0].length, lightLevels);
                for (int j = 0; j < seen[0].length; j++) {
                    for (int i = 0; i < seen.length; i++) {
                        /*
                        if(Math.abs(seen[i][j] - unpacked2[i][j]) >= 0.75 / FOV_RANGE) {
                            System.out.println( "seen " + seen[i][j] + ", unpacked " + unpacked2[i][j]);
                            System.out.println(seen[i][j] - unpacked2[i][j]);

                            System.out.println("Values present in seen at range " + FOV_RANGE + ": ");
                            for (Double d : seenValues) {
                                System.out.print(d + "  ");
                            }
                        }
                        */
                        assertTrue(Math.abs(seen[i][j] - unpacked2[i][j]) < 0.75 / FOV_RANGE);
                    }
                }

                ramPacked += arrayMemoryUsageJagged(packed);
                ramFloat += arrayMemoryUsage2D(seen.length, seen[0].length, 4);
                ramDouble += arrayMemoryUsage2D(seen.length, seen[0].length, 8);
            }
            //System.out.println(dungeonGenerator.toString());
            /*
            System.out.println("Appropriate Parameter packed values " + FOV_RANGE);
            for (int p = 0; p < packed.length; p++) {
                if (packed[p].length == 0) continue;
                System.out.print(packed[p][0]);
                for (int i = 1; i < packed[p].length; i++) {
                    System.out.print(", " + (packed[p][i] & 0xffff));
                }
                System.out.println();
            }*/
            //assertEquals("Packed shorts", 19, packed.length);
            //assertEquals("Unpacked doubles: ", 57600, seen.length * seen[0].length);

            /*
            System.out.println("Memory used by multi-packed short[][] (Appropriate " + FOV_RANGE + "):" +
                    ramPacked / 100.0 + " bytes");
            System.out.println("Memory used by double[][] (Appropriate " + FOV_RANGE + "):" +
                    ramDouble / 100.0 + " bytes");
            System.out.println("Memory used by float[][] (Appropriate " + FOV_RANGE + "):" +
                    ramFloat / 100.0 + " bytes");
            System.out.println("Compression vs. double[][] (Appropriate " + FOV_RANGE + "):" +
                    100.0 * ramPacked / ramDouble + "%");
            System.out.println("Compression vs. float[][] (Appropriate " + FOV_RANGE + "):" +
                    100.0 * ramPacked / ramFloat + "%");
            */
        }
        /*
        byte[][] unpacked3 = unpackMultiByte(packed, seen.length, seen[0].length);
        for (int j = 0; j < seen[0].length; j++) {
            for (int i = 0; i < seen.length; i++) {
                System.out.print(String.format("%x", unpacked3[i][j]));
            }
            System.out.println();
        }
        System.out.println();
        */


        /*
        byte[][] unpacked3 = unpackMultiByte(packed, seen.length, seen[0].length);
        for (int j = 0; j < seen[0].length; j++) {
            for (int i = 0; i < seen.length; i++) {
                System.out.print(unpacked3[i][j]);
            }
            System.out.println();
        }
        System.out.println();
        for (int j = 0; j < seen[0].length; j++) {
            for (int i = 0; i < seen.length; i++) {
                System.out.print((int) (seen[i][j] * 8.05));
            }
            System.out.println();
        }*/
    }

    @Test
    public void testPackMultiPoorParameters() {
        for(int FOV_RANGE = 1; FOV_RANGE < 21; FOV_RANGE++) {
            StatefulRNG rng = new StatefulRNG(new LightRNG(0xAAAA2D2));
            DungeonGenerator dungeonGenerator = new DungeonGenerator(30, 240, rng);
            dungeonGenerator.addDoors(15, true);
            dungeonGenerator.addWater(25);
            dungeonGenerator.addTraps(2);
            char[][] map = dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);
            double[][] resMap = DungeonUtility.generateResistances(map), seen;

            FOV fov = new FOV(FOV.RIPPLE);

            double[] packingLevels = generatePackingLevels(FOV_RANGE),
                      lightLevels = generateLightLevels(FOV_RANGE);
            short[][] packed;
            int ramPacked = 0, ramFloat = 0, ramDouble = 0;
            Coord viewer;
            HashSet<Double> seenValues = new HashSet<>(FOV_RANGE * 2);
            /*
            System.out.println("Packing levels at range " + FOV_RANGE + ": ");
            for (Double d : packingLevels) {
                System.out.print(d + "  ");
            }
            System.out.println();
            System.out.println("Light levels at range " + FOV_RANGE + ": ");
            for (Double d : lightLevels) {
                System.out.print(d + "  ");
            }
            */
            for (int t = 0; t < 100; t++) {
                viewer = dungeonGenerator.utility.randomFloor(map);
                seen = fov.calculateFOV(resMap, viewer.x, viewer.y, FOV_RANGE, RADIUS);
                packed = packMulti(seen, packingLevels);
                for (int j = 0; j < seen[0].length; j++) {
                    for (int i = 0; i < seen.length; i++) {
                        seenValues.add(seen[i][j]);
                    }
                }

                for (int ll = 0; ll < lightLevels.length; ll++) {
                    boolean[][] unpackedB = unpack(packed[ll], seen.length, seen[0].length);
                    for (int i = 0; i < unpackedB.length; i++) {
                        for (int j = 0; j < unpackedB[i].length; j++) {
                            assertTrue((seen[i][j] > packingLevels[ll]) == unpackedB[i][j]);
                        }
                    }
                    if (ll + 1 == lightLevels.length) {
                        assertTrue(packed[ll].length == 2);
                        assertTrue(queryPacked(packed[ll], viewer.x, viewer.y));
                    }
                }
                double[][] unpacked2 = unpackMultiDouble(packed, seen.length, seen[0].length, lightLevels);
                for (int j = 0; j < seen[0].length; j++) {
                    for (int i = 0; i < seen.length; i++) {
                        /*
                        if(Math.abs(seen[i][j] - unpacked2[i][j]) >= 0.75 / FOV_RANGE)
                        {
                            System.out.println( "seen " + seen[i][j] + ", unpacked " + unpacked2[i][j]);
                            System.out.println(seen[i][j] - unpacked2[i][j]);
                            System.out.println("Values present in seen at range " + FOV_RANGE + ": ");
                            for (Double d : seenValues) {
                                System.out.print(d + "  ");
                            }
                            System.out.println();
                        }
                        */
                        assertTrue(Math.abs(seen[i][j] - unpacked2[i][j]) < 0.75 / FOV_RANGE);
                    }
                }
                ramPacked += arrayMemoryUsageJagged(packed);
                ramFloat += arrayMemoryUsage2D(seen.length, seen[0].length, 4);
                ramDouble += arrayMemoryUsage2D(seen.length, seen[0].length, 8);
            }
            //System.out.println(dungeonGenerator.toString());
            /*
            System.out.println("Appropriate Parameter packed values " + FOV_RANGE);
            for (int p = 0; p < packed.length; p++) {
                if (packed[p].length == 0) continue;
                System.out.print(packed[p][0]);
                for (int i = 1; i < packed[p].length; i++) {
                    System.out.print(", " + (packed[p][i] & 0xffff));
                }
                System.out.println();
            }*/
            //assertEquals("Packed shorts", 19, packed.length);
            //assertEquals("Unpacked doubles: ", 57600, seen.length * seen[0].length);
            /*
            System.out.println("Memory used by multi-packed short[][] (Approaching Worst-Case " + FOV_RANGE + "):" +
                    ramPacked / 100.0 + " bytes");
            System.out.println("Memory used by double[][] (Approaching Worst-Case " + FOV_RANGE + "):" +
                    ramDouble / 100.0 + " bytes");
            System.out.println("Memory used by float[][] (Approaching Worst-Case " + FOV_RANGE + "):" +
                    ramFloat / 100.0 + " bytes");
            System.out.println("Compression vs. double[][] (Approaching Worst-Case " + FOV_RANGE + "):" +
                    100.0 * ramPacked / ramDouble + "%");
            System.out.println("Compression vs. float[][] (Approaching Worst-Case " + FOV_RANGE + "):" +
                    100.0 * ramPacked / ramFloat + "%");
            */
        }
    }

    @Test
    public void testMorton() {
        assertEquals(0, mortonEncode(0, 0));
        assertEquals(0x5555, mortonEncode(0xFF, 0));
        assertEquals(0xAAAA, mortonEncode(0, 0xFF));
        assertEquals(0xFFFF, mortonEncode(0xFF, 0xFF));
        assertEquals(0x7AAD, mortonEncode(0xC3, 0x7E));
        assertEquals(Coord.get(0xC3, 0x7E), mortonDecode(0x7AAD));
        //generateHilbert();
    }

    public static void generateHilbert() {
        int sideLength = (1 << 8);
        int capacity = sideLength * sideLength;
        short[] out = new short[capacity];// xOut = new short[capacity], yOut = new short[capacity];
        /*
        Coord c;
        for (int i = 0; i < capacity; i++) {
            c = hilbertToCoord(i);
            xOut[i] = (short) c.x;
            yOut[i] = (short) c.y;
        }

        for (int y = 0; y < sideLength; y++) {
            for (int x = 0; x < sideLength; x++) {
                out[y * sideLength + x] = (short) posToHilbert(x, y);
            }
        }
        try {
            FileChannel channel = new FileOutputStream("target/distance").getChannel();
            channel.write(shortsToBytes(out));
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        /*
        try {
            FileChannel channel = new FileOutputStream("target/hilbertx").getChannel();
            channel.write(shortsToBytes(xOut));
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileChannel channel = new FileOutputStream("target/hilberty").getChannel();
            channel.write(shortsToBytes(yOut));
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
/*
StringBuilder text = new StringBuilder(0xffff * 11);
        text.append("private static final short[] hilbertX = new short[] {\n");
        text.append(xOut[0]);
        for (int i = 1, ln = 0; i < capacity; i++, ln +=4) {
            text.append(',');
            if(ln > 75)
            {
                ln = 0;
                text.append('\n');
            }
            text.append(xOut[i]);

        }
        text.append("\n},\n");
        text.append("hilbertY = new short[] {\n");
        text.append(yOut[0]);
        for (int i = 1, ln = 0; i < capacity; i++, ln +=4) {
            text.append(',');
            if(ln > 75)
            {
                ln = 0;
                text.append('\n');
            }
            text.append(yOut[i]);

        }
        text.append("\n}\n\n");
 */
    public static ByteBuffer shortsToBytes(short[] arr) {
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(arr.length * 2);
        bb.asShortBuffer().put(arr);
        return bb;
    }
}
