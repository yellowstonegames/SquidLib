package squidpony.examples;

import squidpony.squidgrid.MimicFill;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.*;

/**
 * Created by Tommy Ettinger on 5/14/2016.
 */
public class MimicFillTest {
    public static void main(String[] args)
    {
        //seed is, in base 36, the number SQUIDLIB
        ThunderRNG thunder = new ThunderRNG(2252637788195L);
        LightRNG light = new LightRNG(2252637788195L);
        XoRoRNG xoro = new XoRoRNG(2252637788195L);
        RNG rng;
        //rng = new RNG(thunder);
        //rng = new RNG(light);
        rng = new RNG(xoro);
        boolean[][] result;
        boolean[] solo;
        long time1d = 0L, time2d = 0L, time, junk = 0L;

        GreasedRegion gr = new GreasedRegion(200, 200);
        thunder.reseed(2252637788195L); light.setState(2252637788195L); xoro.setSeed(2252637788195L);
        time = System.currentTimeMillis();
        for(boolean[][] sample : MimicFill.samples)
        {
            result = MimicFill.fill(sample, 200, 0.2, 3, rng);

            DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
                    DungeonUtility.wallWrap(MimicFill.sampleToMap(result, '.', '#')),
                    true));
            System.out.println();
//            DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
//                    DungeonUtility.wallWrap(gr.clear().refill(result).toChars())));
//            System.out.println("\n\n*****************\n\n");

        }
        time2d += System.currentTimeMillis() - time;

        junk += thunder.nextLong(); junk += light.nextLong(); junk += xoro.nextLong();
        thunder.reseed(2252637788195L); light.setState(2252637788195L); xoro.setSeed(2252637788195L);

        time = System.currentTimeMillis();
        for(boolean[][] sample : MimicFill.samples)
        {
            solo = MimicFill.fillSolo(sample, 200, 0.2, 3, rng);

            DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
                    DungeonUtility.wallWrap(MimicFill.sampleToMap(solo, 200, 200, '.', '#')),
                    true));
            System.out.println();
//            DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
//                    DungeonUtility.wallWrap(gr.clear().refill(solo, 200, 200).toChars())));
//            System.out.println("\n\n*****************\n\n");

        }
        time1d += System.currentTimeMillis() - time;
        junk += thunder.nextLong(); junk += light.nextLong(); junk += xoro.nextLong();

        /*
        thunder.reseed(2252637788195L); light.setState(2252637788195L); xoro.setSeed(2252637788195L);
        time = System.currentTimeMillis();
        for(boolean[][] sample : MimicFill.samples)
        {
            result = MimicFill.fill(sample, 200, 0.2, 3, rng);
        }
        time2d += System.currentTimeMillis() - time;
        junk += thunder.nextLong(); junk += light.nextLong(); junk += xoro.nextLong();
        thunder.reseed(2252637788195L); light.setState(2252637788195L); xoro.setSeed(2252637788195L);
        time = System.currentTimeMillis();
        for(boolean[][] sample : MimicFill.samples)
        {
            solo = MimicFill.fillSolo(sample, 200, 0.2, 3, rng);
        }
        time1d += System.currentTimeMillis() - time;
        junk += thunder.nextLong(); junk += light.nextLong(); junk += xoro.nextLong();

        thunder.reseed(2252637788195L); light.setState(2252637788195L); xoro.setSeed(2252637788195L);
        time = System.currentTimeMillis();
        for(boolean[][] sample : MimicFill.samples)
        {
            solo = MimicFill.fillSolo(sample, 200, 0.2, 3, rng);
        }
        time1d += System.currentTimeMillis() - time;
        junk += thunder.nextLong(); junk += light.nextLong(); junk += xoro.nextLong();
        thunder.reseed(2252637788195L); light.setState(2252637788195L); xoro.setSeed(2252637788195L);
        time = System.currentTimeMillis();
        for(boolean[][] sample : MimicFill.samples)
        {
            result = MimicFill.fill(sample, 200, 0.2, 3, rng);
        }
        time2d += System.currentTimeMillis() - time;
        junk += thunder.nextLong(); junk += light.nextLong(); junk += xoro.nextLong();
        thunder.reseed(2252637788195L); light.setState(2252637788195L); xoro.setSeed(2252637788195L);
        time = System.currentTimeMillis();
        for(boolean[][] sample : MimicFill.samples)
        {
            solo = MimicFill.fillSolo(sample, 200, 0.2, 3, rng);
        }
        time1d += System.currentTimeMillis() - time;
        junk += thunder.nextLong(); junk += light.nextLong(); junk += xoro.nextLong();
        thunder.reseed(2252637788195L); light.setState(2252637788195L); xoro.setSeed(2252637788195L);
        time = System.currentTimeMillis();
        for(boolean[][] sample : MimicFill.samples)
        {
            result = MimicFill.fill(sample, 200, 0.2, 3, rng);
        }
        time2d += System.currentTimeMillis() - time;
        junk += thunder.nextLong(); junk += light.nextLong(); junk += xoro.nextLong();
        */

        System.out.println("2D time: " + time2d);
        System.out.println("1D time: " + time1d);
        System.out.println("Extra data, irrelevant except that it forces calculations: " + junk);

        /*
        //char[][] dungeon = new DungeonBoneGen(rng).generate(TilesetType.DEFAULT_DUNGEON, 47, 47);
        SerpentMapGenerator serpent = new SerpentMapGenerator(35, 35, rng, 0.1);
        serpent.putWalledRoundRoomCarvers(3);
        serpent.putWalledBoxRoomCarvers(2);
        char[][] dungeon = serpent.generate();
        //dungeon = new DungeonBoneGen(rng).generate(TilesetType.DEFAULT_DUNGEON, 30, 30);
        result = MimicFill.fill(MimicFill.mapToSample(dungeon, '.'), 200, 0.25, 5, rng);

        DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
                DungeonUtility.wallWrap(MimicFill.sampleToMap(result, '.', '#')),
                true));
        System.out.println();
        //DungeonUtility.debugPrint(MimicFill.sampleToMap(result, '1', '0'));

        thunder.reseed(2252637788195L); light.setState(2252637788195L); xoro.setSeed(2252637788195L);

        ArrayList<Coord> pts = SerpentMapGenerator.pointPath(60, 60, rng);
        result = MimicFill.markSample(
                MimicFill.fill(MimicFill.mapToSample(dungeon, '.'), 200, 0.25, 5, rng),
                pts);
        DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
                DungeonUtility.wallWrap(MimicFill.sampleToMap(result, '.', '#')),
                true));
*/

        /*
        boolean[][] sample2 = new PacMazeGenerator(33, 33, rng).create();

        result = MimicFill.fill(sample2, 48, 0.2, 5, rng);

        DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
                DungeonUtility.wallWrap(MimicFill.sampleToMap(sample2, '.', '#')),
                true));
        System.out.println();
        DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
                DungeonUtility.wallWrap(MimicFill.sampleToMap(result, '.', '#')),
                true));
                */
    }
}
