package squidpony.squidmath;

import static org.junit.Assert.*;

import org.junit.Test;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


/**
 * Created by Tommy Ettinger on 10/1/2015.
 */
public class CoordPackerTest {
    public void printBits16(int n) {
        for (int i = 0x8000; i > 0; i >>= 1)
            System.out.print((n & i) > 0 ? 1 : 0);
    }

    public void printBits32(int n) {
        for (int i = 1 << 31; i != 0; i >>>= 1)
            System.out.print((n & i) != 0 ? 1 : 0);
    }

    @Test
    public void testHilbertCurve() {
        assertEquals(0, CoordPacker.posToHilbert(0, 0));
        assertEquals(21845, CoordPacker.posToHilbert(255, 0));
        assertEquals(65535, CoordPacker.posToHilbert(0, 255));
        assertEquals(43690, CoordPacker.posToHilbert(255, 255));

        assertEquals(43690, CoordPacker.coordToHilbert(Coord.get(255, 255)));
        assertEquals(CoordPacker.posToHilbert(255, 255), CoordPacker.coordToHilbert(Coord.get(255, 255)));
        assertEquals(Coord.get(255, 255), CoordPacker.hilbertToCoord(CoordPacker.coordToHilbert(Coord.get(255, 255))));
    }

    @Test
    public void testPack()
    {
        RNG rng = new RNG(new LightRNG(0xd00bacca));
        DungeonGenerator dungeonGenerator = new DungeonGenerator(35, 60, rng);
        dungeonGenerator.addDoors(15, true);
        dungeonGenerator.addWater(25);
        dungeonGenerator.addTraps(2);
        char[][] map = dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);

        FOV fov = new FOV();
        Coord viewer = dungeonGenerator.utility.randomFloor(map);
        double[][] resMap = DungeonUtility.generateResistances(map);
        double[][] seen = fov.calculateFOV(resMap, viewer.x, viewer.y, 8, Radius.DIAMOND);
        IntVLA packed = CoordPacker.pack(seen);
        assertEquals("Packed ints", 19, packed.size);
        assertEquals("Unpacked doubles: ", 2100, seen.length * seen[0].length);

        //map[viewer.x][viewer.y] = '@';
        //dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(map));
        //System.out.println(dungeonGenerator.toString());
    }

    @Test
    public void testMorton() {
        assertEquals(0, CoordPacker.mortonEncode(0, 0));
        assertEquals(0x5555, CoordPacker.mortonEncode(0xFF, 0));
        assertEquals(0xAAAA, CoordPacker.mortonEncode(0, 0xFF));
        assertEquals(0xFFFF, CoordPacker.mortonEncode(0xFF, 0xFF));
        assertEquals(0x7AAD, CoordPacker.mortonEncode(0xC3, 0x7E));
        assertEquals(Coord.get(0xC3, 0x7E), CoordPacker.mortonDecode(0x7AAD));
        //generateHilbert();
    }

    public static void generateHilbert() {
        int sideLength = (1 << 8);
        int capacity = sideLength * sideLength;
        short[] xOut = new short[capacity], yOut = new short[capacity];
        Coord c;
        for (int i = 0; i < capacity; i++) {
            c = CoordPacker.hilbertToCoord(i);
            xOut[i] = (short) c.x;
            yOut[i] = (short) c.y;
        }

        try {
            FileChannel channel = new FileOutputStream("target/x.bin").getChannel();
            channel.write(shortsToBytes(xOut));
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileChannel channel = new FileOutputStream("target/y.bin").getChannel();
            channel.write(shortsToBytes(yOut));
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
