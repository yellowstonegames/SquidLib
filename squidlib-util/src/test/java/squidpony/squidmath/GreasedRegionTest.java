package squidpony.squidmath;

import org.junit.Test;
import squidpony.ArrayTools;
import squidpony.examples.TestConfiguration;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DividedMazeGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.DungeonBoneGen;
import squidpony.squidgrid.mapping.styled.TilesetType;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static squidpony.examples.TestConfiguration.PRINTING;


/**
 * Created by Tommy Ettinger on 10/1/2015.
 */
public class GreasedRegionTest {
    static {
        Coord.expandPoolTo(801, 801);
    }
    public static GreasedRegion dataCross = new GreasedRegion(64, 64).insertRectangle(25, 2, 14, 60).insertRectangle(2, 25, 60, 14);
    public static GreasedRegion dataCross2 = new GreasedRegion(128, 128).insertRectangle(24+32, 2 + 32, 16, 60).insertRectangle(2 + 32, 24 + 32, 60, 16);
    public static GreasedRegion box = new GreasedRegion(64, 64).insertRectangle(24, 24, 16, 16);
    public static GreasedRegion box2 = new GreasedRegion(120, 120).insertRectangle(24+32, 24+32, 16, 16);
    public static GreasedRegion box3 = new GreasedRegion(240, 240).insertRectangle(30, 30, 180, 180);
    public static StatefulRNG srng = new StatefulRNG(0xB0BAFE7715BADA55L);
    public static RNG rng = new RNG(0xB0BAFE7715BADA55L);
    public static DungeonBoneGen dungeonGen = new DungeonBoneGen(srng);
    public static char[][] dungeon = dungeonGen.generate(TilesetType.DEFAULT_DUNGEON, 66, 70);
    public static GreasedRegion dataDungeon = dungeonGen.region.copy().removeEdges();
    public static char[][] giantDungeon = dungeonGen.generate(TilesetType.DEFAULT_DUNGEON, 800, 800);
    public static GreasedRegion giantDataDungeon = dungeonGen.region.copy().removeEdges();
    public static final char[] letters = ArrayTools.letterSpan(256);
    public static void print2D(int[][] data)
    {
        if(!PRINTING)
            return;
        if(data == null || data.length <= 0 || data[0] == null || data[0].length <= 0)
            TestConfiguration.println("null");
        else
        {
            int d;
            for (int y = 0; y < data[0].length; y++) {
                for (int x = 0; x < data.length; x++) {
                    d = data[x][y]& 255;
                    TestConfiguration.print(letters[d]);
                }
                TestConfiguration.println("");
            }
        }
    }
    @Test
    public void testBasics() {
        //printPacked(dataCross, 64, 64);
        GreasedRegion doubleNegative = new GreasedRegion(dataCross).not().not();
        assertEquals(dataCross, doubleNegative);
        GreasedRegion gr = new GreasedRegion(box2);
        printRegion(gr);
        GreasedRegion gr2 = new GreasedRegion(120, 120);
        gr2.insertRectangle(24 + 32, 24 + 32, 16, 16);
        assertEquals(gr, gr2);
        if(PRINTING) {
            srng.setState(0x123456789ABCDEFL);
            printRegion(new GreasedRegion(120, 120).insertSeveral(gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng),
                    gr.singleRandom(srng), gr.singleRandom(srng)));
            int dcs = dataCross.copy().not().size() + dataCross.size() / 20;
            TestConfiguration.println("\nSOBOL:");
            printRegion(gr = dataCross.copy().not().insertSeveral(dataCross.separatedPortion(0.05)));
            TestConfiguration.println("expected size: " + (dcs) + ", actual size " + gr.size());
            TestConfiguration.println("\nVDC_2:");
            printRegion(gr2 = dataCross.copy().not().insertSeveral(dataCross.quasiRandomSeparated(0.05)));
            TestConfiguration.println("expected size: " + (dcs) + ", actual size " + gr2.size());
            TestConfiguration.println("\nZ_CURVE:");
            printRegion(gr2 = dataCross.copy().not().insertSeveral(dataCross.separatedZCurve(0.05)));
            TestConfiguration.println("expected size: " + (dcs) + ", actual size " + gr2.size());
            TestConfiguration.println("\nRANDOM_MIXED:");
            printRegion(gr2 = dataCross.copy().not().insertSeveral(dataCross.mixedRandomSeparated(0.05)));
            TestConfiguration.println("expected size: " + (dcs) + ", actual size " + gr2.size());
            TestConfiguration.println("\nBLUE:");
            printRegion(gr2 = dataCross.copy().not().insertSeveral(dataCross.separatedBlue(0.05)));
            TestConfiguration.println("expected size: " + (dcs) + ", actual size " + gr2.size());
            TestConfiguration.println("");
            printRegion(dataDungeon);
            TestConfiguration.println("\nWith fraction 0.05:");
            TestConfiguration.println("\nSOBOL:");
            printRegion(gr = dataDungeon.copy().empty().insertSeveral(dataDungeon.separatedPortion(0.05)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() / 20) + ", actual size " + gr.size());
            TestConfiguration.println("\nVDC_2:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.quasiRandomSeparated(0.05)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() / 20) + ", actual size " + gr2.size());
            TestConfiguration.println("\nZ_CURVE:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.separatedZCurve(0.05)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() / 20) + ", actual size " + gr2.size());
            TestConfiguration.println("\nRANDOM_MIXED:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.mixedRandomSeparated(0.05)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() / 20) + ", actual size " + gr2.size());
            TestConfiguration.println("\nBLUE:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.separatedBlue(0.05)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() / 20) + ", actual size " + gr2.size());

            TestConfiguration.println("\nWith fraction 0.15:");
            TestConfiguration.println("\nSOBOL:");
            printRegion(gr = dataDungeon.copy().empty().insertSeveral(dataDungeon.separatedPortion(0.15)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() * 3 / 20) + ", actual size " + gr.size());
            TestConfiguration.println("\nVDC_2:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.quasiRandomSeparated(0.15)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() * 3 / 20) + ", actual size " + gr2.size());
            TestConfiguration.println("\nZ_CURVE:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.separatedZCurve(0.15)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() * 3 / 20) + ", actual size " + gr2.size());
            TestConfiguration.println("\nRANDOM_MIXED:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.mixedRandomSeparated(0.15)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() * 3 / 20) + ", actual size " + gr2.size());
            TestConfiguration.println("\nBLUE:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.separatedBlue(0.15)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() * 3 / 20) + ", actual size " + gr2.size());

            TestConfiguration.println("\nWith fraction 0.25:");
            TestConfiguration.println("\nSOBOL:");
            printRegion(gr = dataDungeon.copy().empty().insertSeveral(dataDungeon.separatedPortion(0.25)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() / 4) + ", actual size " + gr.size());
            TestConfiguration.println("\nVDC_2:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.quasiRandomSeparated(0.25)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() / 4) + ", actual size " + gr2.size());
            TestConfiguration.println("\nZ_CURVE:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.separatedZCurve(0.25)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() / 4) + ", actual size " + gr2.size());
            TestConfiguration.println("\nRANDOM_MIXED:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.mixedRandomSeparated(0.25)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() / 4) + ", actual size " + gr2.size());
            TestConfiguration.println("\nBLUE:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.separatedBlue(0.25)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() / 4) + ", actual size " + gr2.size());

            TestConfiguration.println("\nWith fraction 0.4:");
            TestConfiguration.println("\nSOBOL:");
            printRegion(gr = dataDungeon.copy().empty().insertSeveral(dataDungeon.separatedPortion(0.4)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() * 2 / 5) + ", actual size " + gr.size());
            TestConfiguration.println("\nVDC_2:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.quasiRandomSeparated(0.4)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() * 2 / 5) + ", actual size " + gr2.size());
            TestConfiguration.println("\nZ_CURVE:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.separatedZCurve(0.4)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() * 2 / 5) + ", actual size " + gr2.size());
            TestConfiguration.println("\nRANDOM_MIXED:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.mixedRandomSeparated(0.4)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() * 2 / 5) + ", actual size " + gr2.size());
            TestConfiguration.println("\nBLUE:");
            printRegion(gr2 = dataDungeon.copy().empty().insertSeveral(dataDungeon.separatedBlue(0.4)));
            TestConfiguration.println("expected size: " + (dataDungeon.size() * 2 / 5) + ", actual size " + gr2.size());

            gr = dataDungeon.copy();
            TestConfiguration.println("\nrandomScatter with minimum distance 1:");
            printRegion(gr.randomScatter(rng, 1));
            TestConfiguration.println("\nrandomScatter with minimum distance 2:");
            printRegion(gr.allOn().randomScatter(rng, 2));
            TestConfiguration.println("\nrandomScatter with minimum distance 3:");
            printRegion(gr.allOn().randomScatter(rng, 3));
            gr = dataDungeon.copy();
            TestConfiguration.println("\nrandomScatter with minimum distance 1 and limit of 20:");
            printRegion(gr.randomScatter(rng, 1, 20));
            TestConfiguration.println("\nrandomScatter with minimum distance 2 and limit of 20:");
            printRegion(gr.allOn().randomScatter(rng, 2, 20));
            TestConfiguration.println("\nrandomScatter with minimum distance 3 and limit of 20:");
            printRegion(gr.allOn().randomScatter(rng, 3, 20));
        }
        GreasedRegion g = new GreasedRegion(box);
        GreasedRegion g2 = new GreasedRegion(64, 64);
        g2.insertRectangle(24, 24, 16, 16);
        assertEquals(g, g2);
        GreasedRegion grr = new GreasedRegion(box3);
        GreasedRegion grr2 = new GreasedRegion(240, 240);
        grr2.insertRectangle(30, 30, 180, 180);
        assertEquals(grr, grr2);
//        GreasedRegion gri = new GreasedRegion(grr).insertRectangle(24, 52, 16, 16);
//        GreasedRegion gri2 = new GreasedRegion(grr).insert(0, 28, box);
//        printRegion(box);
//        printRegion(gri);
//        printRegion(gri2);
//        assertTrue(gri.equals(gri2));
        GreasedRegion bigOn = new GreasedRegion(152, 152).not();
        GreasedRegion gri = new GreasedRegion(150, 150).insertRectangle(3, 4, 147, 146);
        GreasedRegion gri2 = new GreasedRegion(150, 150).insert(3, 4, bigOn);
//        printRegion(box);
//        printRegion(gri);
//        printRegion(gri2);
        assertEquals(gri, gri2);
//        StatefulRNG srng2 = new StatefulRNG(12345L);
//        srng.setState(12345L);
//        gr.remake(dataDungeon);
//        gr2.remake(dataDungeon);
//        assertEquals(gr.singleRandom(srng), gr2.singleRandomAlt(srng2));
//        assertEquals(gr.singleRandom(srng), gr2.singleRandomAlt(srng2));
//        assertEquals(gr.singleRandom(srng), gr2.singleRandomAlt(srng2));

        DividedMazeGenerator mazeGen = new DividedMazeGenerator(120, 120);
        TestConfiguration.println(gr2.refill(mazeGen.generate(), '.').toString());
    }

    public static int FOV_RANGE = 12;
    public static Radius RADIUS = Radius.SQUARE;

    public static void printRegion(GreasedRegion r) {
        if (PRINTING) {
            DungeonUtility.debugPrint(r.toChars());
            TestConfiguration.println("");
        }
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
    public void testTranslate() {
        CoordPacker.init();

        GreasedRegion crossZeroTranslated = new GreasedRegion(dataCross).translate(0, 0);
        GreasedRegion crossTranslated = new GreasedRegion(dataCross).translate(1, 1);
        GreasedRegion crossUnTranslated = new GreasedRegion(crossTranslated).translate(-1, -1);

        assertTrue(dataCross.equals(crossZeroTranslated));
        assertTrue(dataCross.equals(crossUnTranslated));

        printRegion(dataCross);
        GreasedRegion crossBox = new GreasedRegion(dataCross).translate(25, 25).translate(-50, -50);
        printRegion(crossBox);
        assertTrue(crossBox.equals(new GreasedRegion(CoordPacker.unpack(CoordPacker.rectangle(14, 14), 64, 64))));

        GreasedRegion big = new GreasedRegion(150, 150).not().translate(100, 100);
        GreasedRegion big2 = new GreasedRegion(150, 150).insertRectangle(100, 100, 50, 50);
        printRegion(big);
        printRegion(big2);
        assertTrue(big.equals(big2));
    }

    @Test
    public void testUnion() {
        CoordPacker.init();

        GreasedRegion box = new GreasedRegion(dataCross);
        //printRegion(box);
        box.translate(25, 25);
        //printRegion(box);
        box.translate(-50, -50);
        //printRegion(box);
        box.translate(25, 25);
        GreasedRegion alter = new GreasedRegion(CoordPacker.unpack(CoordPacker.rectangle(25, 2, 14, 60), 64, 64))
                .and(new GreasedRegion(CoordPacker.unpack(CoordPacker.rectangle(2, 25, 60, 14), 64, 64)));
        //printRegion(box);
        //printRegion(alter);
        assertTrue(box.equals(alter));
        GreasedRegion minus = new GreasedRegion(dataCross).andNot(box);
        GreasedRegion xr = new GreasedRegion(CoordPacker.unpack(CoordPacker.rectangle(25, 2, 14, 60), 64, 64))
                .xor(new GreasedRegion(CoordPacker.unpack(CoordPacker.rectangle(2, 25, 60, 14), 64, 64)));
        printRegion(minus);
        printRegion(xr);
        assertTrue(minus.equals(xr));

    }
    @Test
    public void testExpanding() {

        GreasedRegion edge = new GreasedRegion(dataCross).fringe();
        printRegion(edge);
        GreasedRegion bonus = new GreasedRegion(dataCross).expand();
        printRegion(bonus);
        assertTrue(new GreasedRegion(bonus).andNot(edge).equals(dataCross));

        GreasedRegion edge2 = new GreasedRegion(dataCross2).fringe8way();
        printRegion(edge2);
        GreasedRegion bonus2 = new GreasedRegion(dataCross2).expand8way();
        printRegion(bonus2);
        assertTrue(new GreasedRegion(bonus2).andNot(edge2).equals(dataCross2));


        GreasedRegion flooded = new GreasedRegion(Coord.get(26, 2), 64, 64).flood(dataCross, 2);
        GreasedRegion manual = new GreasedRegion(64, 64, Coord.get(25, 2), Coord.get(26, 2), Coord.get(27, 2), Coord.get(28, 2),
                Coord.get(25, 3), Coord.get(26, 3), Coord.get(27, 3),
                Coord.get(26, 4));
        printRegion(flooded);
        printRegion(manual);
        assertTrue(flooded.equals(manual));

    }
    @Test
    public void testRetracting() {
        GreasedRegion surf = new GreasedRegion(dataCross).surface();
        printRegion(surf);
        GreasedRegion shrunk = new GreasedRegion(dataCross).retract();
        printRegion(shrunk);
        assertTrue(new GreasedRegion(shrunk).or(surf).equals(dataCross));

        GreasedRegion surf2 = new GreasedRegion(dataCross2).surface8way();
        printRegion(surf2);
        GreasedRegion shrunk2 = new GreasedRegion(dataCross2).retract8way();
        printRegion(shrunk2);
        assertTrue(new GreasedRegion(shrunk2).or(surf2).equals(dataCross2));
        TestConfiguration.println("");
        GreasedRegion littleBox = new GreasedRegion(32, 32).insertRectangle(12, 12, 8, 8);
        printRegion(littleBox);
        TestConfiguration.println("");
        ArrayList<GreasedRegion> toLimit = littleBox.fringeSeriesToLimit8way();
        for(GreasedRegion gr : toLimit)
            printRegion(gr);
    }
    @Test
    public void testFitting()
    {
        GreasedRegion wrecked = new GreasedRegion(dataCross);
        int[][] numbers = GreasedRegion.sum(new GreasedRegion(64, 64).not().retractSeries8way(30));
        print2D(numbers);
        TestConfiguration.println("");
        printRegion(wrecked);
        TestConfiguration.println("");
        print2D(wrecked.fit(numbers, 0));
    }
    @Test
    public void testRandom()
    {
        StatefulRNG rng = new StatefulRNG(0x1337BEEF);
        GreasedRegion wrecked = dataCross.copy().disperse();
        printRegion(wrecked);
        wrecked.disperse8way();
        printRegion(wrecked);
        wrecked.remake(dataCross).disperseRandom(rng);
        printRegion(wrecked);
    }

    @Test
    public void testCorners() {
        GreasedRegion beveled = new GreasedRegion(dataCross).removeCorners();
        printRegion(beveled);
        GreasedRegion other = new GreasedRegion(dataCross).expand().retract8way();
        printRegion(other);
        assertTrue(beveled.equals(other));
        beveled.fill(true).removeCorners();
        printRegion(beveled);
    }

    @Test
    public void testZoom() {
        GreasedRegion crossCopy = new GreasedRegion(dataCross);
        printRegion(crossCopy);
        crossCopy.zoom(13, 13);
        printRegion(crossCopy);
        GreasedRegion midCross = new GreasedRegion(71, 71).insertRectangle(27, 2, 17, 60).insertRectangle(2, 27, 60, 17);
        printRegion(midCross);
        midCross.zoom(30, 33);
        printRegion(midCross);
    }
    @Test
    public void testCA() {
        RNG rng = new RNG(0x1337BEEFAAAAAAAAL);
        GreasedRegion current = new GreasedRegion(rng, 0.52, 64, 64);
        if (PRINTING)
            TestConfiguration.println(current + "\n\n");
        CellularAutomaton ca = new CellularAutomaton(current);
        if (PRINTING) {
            TestConfiguration.println(ca.runBasicSmoothing());
            TestConfiguration.println("");
            TestConfiguration.println(ca.runBasicSmoothing());
            TestConfiguration.println("");
            TestConfiguration.println(ca.runBasicSmoothing());
            TestConfiguration.println("");
            TestConfiguration.println(ca.runBasicSmoothing());
            TestConfiguration.println("");
            TestConfiguration.println(ca.runBasicSmoothing());
            TestConfiguration.println("");
            TestConfiguration.println(ca.runBasicSmoothing().removeEdges());
            TestConfiguration.println("");
        }
        if (PRINTING)
            TestConfiguration.println(current.largestPart() + "\n");
        current.remake(dataDungeon);
        if (PRINTING)
            TestConfiguration.println(current + "\n\n");
        ca.remake(current.copy());
        if (PRINTING) {
            TestConfiguration.println(current.and(ca.runBasicSmoothing()));
            TestConfiguration.println("");
            TestConfiguration.println(current.and(ca.runBasicSmoothing()));
            TestConfiguration.println("");
            ca.current.remake(current);
            TestConfiguration.println(current.or(ca.runBasicSmoothing()).removeEdges());
            TestConfiguration.println("");
        }
        if (PRINTING)
            TestConfiguration.println(current.largestPart() + "\n");
    }
    
    @Test
    public void testInteriorFill()
    {
        if(!PRINTING) return;
        String[] map = new String[]{
                "..............................................................................................",
                "##############################################################################################",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................+.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "####+###########################.............................................................#",
                "#........#...................................................................................#",
                "#........#...................................................................................#",
                "#........#...................................................................................#",
                "#........#...................................................................................#",
                "########+########################################################+############################",
                "..............................................................................................",
                "..............................................................................................",
                "..............................................................................................",
                "..............................................................................................",
                "..............................................................................................",
                "..............................................................................................",
                "..............................................................................................",
                "..............................................................................................",
                "..............................................................................................",
                "..............................................................................................",
                "..............................................................................................",
                "..............................................................................................",
                "########+########################################################+############################",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................+.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "#..............................#.............................................................#",
                "####+###########################.............................................................#",
                "#........#...................................................................................#",
                "#........#...................................................................................#",
                "#........#...................................................................................#",
                "#........#...................................................................................#",
                "##############################################################################################",
                ".............................................................................................."};
        GreasedRegion floors = new GreasedRegion(map, '.'), doors = new GreasedRegion(map, '+'), 
                walls = floors.copy().not(), edit = walls.copy(); // includes all walls and doors
        edit.connect8way().removeEdges().andNot(walls);
        edit.flood(floors, floors.size());
        if(PRINTING) 
            DungeonUtility.debugPrint(doors.inverseMask(walls.inverseMask(edit.toChars('%', '.'), '#'), '+'));
    }
    @Test
    public void testCompression()
    {
        String compressed;
        GreasedRegion decompressed;
        compressed = dataDungeon.toCompressedString();
        decompressed = GreasedRegion.decompress(compressed);
        assertEquals(dataDungeon, decompressed);
        String basicText = dataDungeon.serializeToString();
        if(PRINTING) TestConfiguration.println(basicText.length() + " compresses down to " + compressed.length());
//        TestConfiguration.println("\n\nORIGINAL\n\n");
//        TestConfiguration.println(dataDungeon);
//        TestConfiguration.println("\n\nDECOMPRESSED\n\n");
//        TestConfiguration.println(decompressed);
//        TestConfiguration.println("\n\nDIFF:\n\n");
//        TestConfiguration.println(decompressed.xor(dataDungeon));
//        TestConfiguration.println("giant stats: width=" + giantDataDungeon.width + ", height=" + giantDataDungeon.height
//                + ", size=" + giantDataDungeon.size());
        compressed = giantDataDungeon.toCompressedString();
        decompressed = GreasedRegion.decompress(compressed);
//        TestConfiguration.println("decom stats: width=" + decompressed.width + ", height=" + decompressed.height
//                + ", size=" + decompressed.size());
//        TestConfiguration.println(decompressed.copy().xor(giantDataDungeon).size());
        assertEquals(giantDataDungeon, decompressed);
        basicText = giantDataDungeon.serializeToString();
        if(PRINTING) TestConfiguration.println(basicText.length() + " compresses down to " + compressed.length());
//        TestConfiguration.println("\n\nORIGINAL\n\n");
//        TestConfiguration.println(giantDataDungeon);
//        TestConfiguration.println("\n\nDECOMPRESSED\n\n");
//        TestConfiguration.println(decompressed);
//        TestConfiguration.println("\n\nDIFF:\n\n");
//        TestConfiguration.println(decompressed.xor(giantDataDungeon));
    }
    @Test
    public void testFlipRotate()
    {
        GreasedRegion work = dataDungeon.copy();
        GreasedRegion once = work.copy().flip(true, false);
        GreasedRegion twice = once.copy().flip(true, false);
        assertEquals(twice, work);
        printRegion(work);
        printRegion(once);
        if(PRINTING) TestConfiguration.println("");
        once = work.copy().flip(false, true);
        twice = once.copy().flip(false, true);
        assertEquals(twice, work);
        printRegion(work);
        printRegion(once);
        if(PRINTING) TestConfiguration.println("");
        once = work.copy().flip(true, true);
        twice = once.copy().flip(true, true);
        assertEquals(twice, work);
        printRegion(work);
        printRegion(once);
        if(PRINTING) TestConfiguration.println("");
        work = giantDataDungeon.copy();
        once = work.copy().flip(true, false);
        twice = once.copy().flip(true, false);
        assertEquals(twice, work);
        once = work.copy().flip(false, true);
        twice = once.copy().flip(false, true);
        assertEquals(twice, work);
        once = work.copy().flip(true, true);
        twice = once.copy().flip(true, true);
        assertEquals(twice, work);

        work = dataDungeon.copy();
        once.remake(work.copyRotated(1));
        twice.remake(work.copyRotated(2));
        GreasedRegion triple = work.copyRotated(3);
        printRegion(work);
        printRegion(once);
        printRegion(twice);
        printRegion(triple);
    }
}

