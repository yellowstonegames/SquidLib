package squidpony.examples;

import squidpony.ArrayTools;
import squidpony.squidgrid.MimicFill;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.DiverRNG;
import squidpony.squidmath.FastNoise;
import squidpony.squidmath.RNG;

/**
 * Created by Tommy Ettinger on 5/14/2016.
 */
public class MimicFillTest {
    public static void main(String[] args)
    {
        //seed is, in base 36, the number SQUIDLIB
        DiverRNG light = new DiverRNG(2252637788195L);
        RNG rng;
        //rng = new RNG(thunder);
        //rng = new RNG(light);
        rng = new RNG(light);
        boolean[][] result;
        boolean[] solo;
        long time1d = 0L, time2d = 0L, time, junk = 0L;

        light.setState(2252637788195L);

        boolean[][] world = new WorldMapGenerator.MimicMap(1L, FastNoise.instance, 0.1).earthOriginal.decode();
        boolean[][] doubleWorld = new boolean[world.length][world.length];
        ArrayTools.insert(world, doubleWorld, 0, 0);
        ArrayTools.insert(world, doubleWorld, 0, world[0].length);
        result = MimicFill.fill(doubleWorld, 220, 0.2, 5, rng);

        DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
                DungeonUtility.wallWrap(MimicFill.sampleToMap(result, '.', '#')),
                true));
        System.out.println();


        time = System.currentTimeMillis();
        for(boolean[][] sample : MimicFill.samples)
        {
            result = MimicFill.fill(sample, 140, 0.2, 3, rng);

            DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
                    DungeonUtility.wallWrap(MimicFill.sampleToMap(result, '.', '#')),
                    true));
            System.out.println();


//            DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
//                    DungeonUtility.wallWrap(gr.refill(result).toChars())));
//            System.out.println("\n\n*****************\n\n");
        }
        time2d += System.currentTimeMillis() - time;
        
        junk += light.nextLong();
        light.setState(2252637788195L);

        time = System.currentTimeMillis();
        for(boolean[][] sample : MimicFill.samples)
        {
            solo = MimicFill.fillSolo(sample, 200, 0.2, 3, rng);

            DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
                    DungeonUtility.wallWrap(MimicFill.sampleToMap(solo, 140, 140, '.', '#')),
                    true));
            System.out.println();
//            DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
//                    DungeonUtility.wallWrap(gr.refill(solo, 200, 200).toChars())));
//            System.out.println("\n\n*****************\n\n");

        }
        time1d += System.currentTimeMillis() - time;
        junk += light.nextLong();

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
