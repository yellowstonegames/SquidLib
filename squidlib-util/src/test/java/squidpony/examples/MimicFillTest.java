package squidpony.examples;

import squidpony.squidgrid.MimicFill;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.PacMazeGenerator;
import squidpony.squidmath.StatefulRNG;

/**
 * Created by Tommy Ettinger on 5/14/2016.
 */
public class MimicFillTest {
    public static void main(String[] args)
    {
        //seed is, in base 36, the number SQUIDLIB
        StatefulRNG rng = new StatefulRNG(2252637788195L);
        boolean[][] sample = new boolean[][]{
                new boolean[4],
                new boolean[]{false, true, true, true},
                new boolean[]{false, true, false, true},
                new boolean[]{false, true, true, true},
        },
        result = MimicFill.fill(sample, 32, 0.2, 5, rng);

        boolean[][] sample2 = new PacMazeGenerator(33, 33, rng).create();

        DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
                DungeonUtility.wallWrap(MimicFill.sampleToMap(result, '.', '#')),
                true));
        System.out.println();
        result = MimicFill.fill(sample2, 48, 0.2, 5, rng);

        DungeonUtility.debugPrint(DungeonUtility.hashesToLines(
                DungeonUtility.wallWrap(MimicFill.sampleToMap(result, '.', '#')),
                true));
    }
}
