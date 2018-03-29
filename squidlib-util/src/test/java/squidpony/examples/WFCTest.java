package squidpony.examples;

import squidpony.squidgrid.MimicWFC;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.StatefulRNG;

/**
 * Created by Tommy Ettinger on 3/28/2018.
 */
public class WFCTest {
    public static void main(String[] args)
    {
        StatefulRNG srng = new StatefulRNG(1234567L);
        int[][] grid = new int[32][32];
        DungeonGenerator dg = new DungeonGenerator(32, 32, srng);
        char[][] dungeon = DungeonUtility.hashesToLines(dg.addGrass(10).addWater(7).generate());
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                grid[x][y] = dungeon[x][y];
            }
        }
        MimicWFC wfc = new MimicWFC(grid, 3, 64, 64, true, true, 1, 0);
        while (!wfc.run(srng.nextLong(), 0));
        int[][] grid2 = wfc.result();
        dungeon = new char[128][128];
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                dungeon[x+64][y] = dungeon[x][y+64] = dungeon[x+64][y+64] = dungeon[x][y] = (char) grid2[x][y];
            }
        }
        DungeonUtility.debugPrint(dungeon);
        System.out.println();
        while (!wfc.run(srng.nextLong(), 0));
        grid2 = wfc.result();
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                dungeon[x+64][y] = dungeon[x][y+64] = dungeon[x+64][y+64] = dungeon[x][y] = (char) grid2[x][y];
            }
        }
        DungeonUtility.debugPrint(dungeon);
        
    }
}
