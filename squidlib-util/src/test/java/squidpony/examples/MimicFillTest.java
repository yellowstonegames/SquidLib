package squidpony.examples;

import squidpony.squidgrid.MimicFill;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.SerpentMapGenerator;
import squidpony.squidmath.Coord;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;

/**
 * Created by Tommy Ettinger on 5/14/2016.
 */
public class MimicFillTest {
    public static void main(String[] args)
    {
        //seed is, in base 36, the number SQUIDLIB
        StatefulRNG rng = new StatefulRNG(2252637788195L);
        boolean[][] result;

        for(boolean[][] sample : MimicFill.samples)
        {
            result = MimicFill.fill(sample, 32, 0.2, 3, rng);

            DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
                    DungeonUtility.wallWrap(MimicFill.sampleToMap(result, '.', '#')),
                    true));
            System.out.println();
            //rng.setState(2252637788195L);
        }

        //char[][] dungeon = new DungeonBoneGen(rng).generate(TilesetType.DEFAULT_DUNGEON, 47, 47);
        SerpentMapGenerator serpent = new SerpentMapGenerator(35, 35, rng, 0.1);
        serpent.putWalledRoundRoomCarvers(3);
        serpent.putWalledBoxRoomCarvers(2);
        char[][] dungeon = serpent.generate();
        //dungeon = new DungeonBoneGen(rng).generate(TilesetType.DEFAULT_DUNGEON, 30, 30);
        result = MimicFill.fill(MimicFill.mapToSample(dungeon, '.'), 32, 0.25, 5, rng);

        DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
                DungeonUtility.wallWrap(MimicFill.sampleToMap(result, '.', '#')),
                true));
        System.out.println();
        //DungeonUtility.debugPrint(MimicFill.sampleToMap(result, '1', '0'));

        rng.setState(2252637788195L);

        ArrayList<Coord> pts = SerpentMapGenerator.pointPath(60, 60, rng);
        result = MimicFill.markSample(
                MimicFill.fill(MimicFill.mapToSample(dungeon, '.'), 60, 0.25, 5, rng),
                pts);
        DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
                DungeonUtility.wallWrap(MimicFill.sampleToMap(result, '.', '#')),
                true));


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
