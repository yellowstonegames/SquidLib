package squidpony.examples;

import squidpony.squidgrid.MimicWFC;
import squidpony.squidmath.GWTRNG;

/**
 * Created by Tommy Ettinger on 3/28/2018.
 */
public class WFCTest {
    public static void main(String[] args)
    {
        GWTRNG random = new GWTRNG(12345678);
        int[][] grid = new int[32][32];
//        DungeonGenerator dg = new DungeonGenerator(32, 32, srng);
//        char[][] dungeon = DungeonUtility.hashesToLines(dg.generate());
        char[][] dungeon = new char[][]{
                "  ┌───────┐ ┌─────┐ ┌────────┐  ".toCharArray(),
                "┌─┤.......│ │.....└─┤........│  ".toCharArray(),
                "│.└┐......│┌┴───....│........│  ".toCharArray(),
                "│..├───┐..││.................│  ".toCharArray(),
                "│..│   │..││.................├─┐".toCharArray(),
                "│..└┐┌─┘..││....┌┐.....──────┘.│".toCharArray(),
                "│...└┘....││..──┤│.............│".toCharArray(),
                "│.........││....└┼─┐..........┌┘".toCharArray(),
                "└┐.....┌──┘│.....└┐└┬────────┬┘ ".toCharArray(),
                " │.....│   │......│ │........│  ".toCharArray(),
                " ├─...┌┘  ┌┴─..┌──┴─┘........│  ".toCharArray(),
                " │....│   │....│.............└─┐".toCharArray(),
                "┌┘...┌┘   │....│...............│".toCharArray(),
                "│....└─┐  │..┌─┴────...........│".toCharArray(),
                "│......└┐ │..│...............─.│".toCharArray(),
                "│.......└─┘..│.................│".toCharArray(),
                "│..┌┐...........┌───...........│".toCharArray(),
                "└──┘└─┐.........│............┌─┘".toCharArray(),
                "      └───┐..│..│............│  ".toCharArray(),
                "    ┌────┐└┬─┘..└┬───┐......┌┘  ".toCharArray(),
                " ┌──┘....│┌┘.....└─┐┌┘..─┬──┘   ".toCharArray(),
                "┌┘.......││........├┘....└┐     ".toCharArray(),
                "│........├┘........│......└┐    ".toCharArray(),
                "│........│...─┐....│.......└┐   ".toCharArray(),
                "└┐....│..│....│....│........│   ".toCharArray(),
                " └─┬──┘.......│..──┘..┌┐....│   ".toCharArray(),
                "   │..........│.......││....│   ".toCharArray(),
                "  ┌┘.....│....│......┌┘│...┌┘   ".toCharArray(),
                "  │......├────┤..──┬─┘ │...│    ".toCharArray(),
                "  │.....┌┘    │....│ ┌─┘..─┤    ".toCharArray(),
                "  └──┐..│     │....│ │.....│    ".toCharArray(),
                "     └──┘     └────┘ └─────┘    ".toCharArray(),
        };
//        System.out.println("new char[][]{");
        for (int y = 0; y < 32; y++) {
//            System.out.print('"');
            for (int x = 0; x < 32; x++) {
                grid[y][x] = dungeon[x][y];
//                System.out.print(dungeon[x][y]);
            }
//            System.out.println("\".toCharArray(),");
        }
//        System.out.println("};");
        MimicWFC wfc = new MimicWFC(grid, 2, 64, 64, false, true, 1, 0);
        while (!wfc.run(random.nextLong(), 0));
        int[][] grid2 = wfc.result();
        for (int y = 0; y < 128; y++) { 
            for (int x = 0; x < 128; x++) {
                System.out.print((char) grid2[x & 63][y & 63]);
            }
            System.out.println();
        }
        System.out.println();
        while (!wfc.run(random.nextLong(), 0));
        grid2 = wfc.result();
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                System.out.print((char) grid2[x & 63][y & 63]);
            }
            System.out.println();
        }
        
    }
}
