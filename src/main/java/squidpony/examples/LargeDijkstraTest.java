package squidpony.examples;

import squidpony.squidgrid.LOS;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.DungeonBoneGen;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidai.DijkstraMap;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import java.awt.Point;

/**
 * Created by Tommy Ettinger on 4/5/2015.
 */
public class LargeDijkstraTest {
    public static void debugPrint(DijkstraMap d)
    {
        int high = d.height;
        int wide = d.width;
        double[][] dungeon = d.gradientMap;
        double[][] trans = new double[high][wide];
        for (int x = 0; x < wide; x++) {
            for (int y = 0; y < high; y++) {
                trans[y][x] = dungeon[x][y];
            }
        }
        StringBuffer sb = new StringBuffer();
        for (int row = 0; row < high; row++) {
            for (int col = 0; col < wide; col++) {
                sb.append(String.format("%06.0f ", trans[row][col]));
            }
            sb.append('\n');
        }
        System.out.println(sb.toString());
    }
    public static void main(String[] args) {
        for (DijkstraMap.Measurement m : DijkstraMap.Measurement.values()) {
            LightRNG lrng = new LightRNG(0x57a8deadbeef0ffal);
            RNG rng = new RNG(lrng);
            int size = 2;
            DungeonUtility.rng = rng;
            DungeonBoneGen dg = new DungeonBoneGen(rng);

            dg.generate(TilesetType.DEFAULT_DUNGEON, 40, 40);
            dg.wallWrap();

            char[][] dun = dg.getDungeon();
            DijkstraMap dijkstra = new DijkstraMap(dun, m);

            System.out.println(dg);
//            DungeonUtility.randomFloorLarge(dun, size);
//            DungeonUtility.randomFloorLarge(dun, size);
            Point goal1 = DungeonUtility.randomFloorLarge(dun, size),
                    goal2 = DungeonUtility.randomFloorLarge(dun, size), goal3 = DungeonUtility.randomFloorLarge(dun, size),
                    goal4 = DungeonUtility.randomFloorLarge(dun, size), goal5 = DungeonUtility.randomFloorLarge(dun, size),
                    entry = DungeonUtility.randomFloorLarge(dun, size);

            dijkstra.findPathLarge(size, 100, null, null, entry, goal1, goal2, goal3, goal4, goal5);
            double[][] gm = dijkstra.gradientMap;
            char[][] md = DungeonUtility.doubleWidth(dun),
                    hl = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(dun));
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    char t = (char) 33;
                    if (x % 2 == 0 && gm[x / 2][y] < 200)
                        t = '.';// ("" + (gm[x / 2][y] % 10)).charAt(0);
                    else if (gm[x / 2][y] == DijkstraMap.WALL)
                        t = hl[x][y];
                    else
                        t = ' ';
                    md[x][y] = t;
                }
            }
            md[entry.x * 2][entry.y] = '@';
            int i = 1;
            for (Point pt : dijkstra.path) {
                md[pt.x * 2][pt.y] = ("" + (i % 10)).charAt(0);
                i++;
            }
            dg.setDungeon(md);
            System.out.println("SEEK PATH");

            System.out.println(dg);
            //debugPrint(dijkstra);

            lrng.setState(0x57a8deadbeef0ffal);
            rng = new RNG(lrng);
            DungeonUtility.rng = rng;

            dg.generate(TilesetType.DEFAULT_DUNGEON, 40, 40);
            dg.wallWrap();

            dun = dg.getDungeon();
            dijkstra.initialize(dun);

            System.out.println(dg);
//            DungeonUtility.randomFloorLarge(dun, size);
//            DungeonUtility.randomFloorLarge(dun, size);
            goal1 = DungeonUtility.randomFloorLarge(dun, size);
            goal2 = DungeonUtility.randomFloorLarge(dun, size);
            goal3 = DungeonUtility.randomFloorLarge(dun, size);
            goal4 = DungeonUtility.randomFloorLarge(dun, size);
            goal5 = DungeonUtility.randomFloorLarge(dun, size);

            entry = DungeonUtility.randomFloorLarge(dun, size);

            dijkstra.findFleePathLarge(size, 100, 1.9, null, null, entry, goal1, goal2, goal3, goal4, goal5);

            gm = dijkstra.gradientMap;
            md = DungeonUtility.doubleWidth(dun);
            hl = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(dun));
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    char t = (char) 33;
                    if (x % 2 == 0 && gm[x / 2][y] < 200)
                        t = '.';// ("" + (gm[x / 2][y] % 10)).charAt(0);
                    else if (gm[x / 2][y] == DijkstraMap.WALL)
                        t = hl[x][y];
                    else
                        t = ' ';
                    md[x][y] = t;
                }
            }
            md[entry.x * 2][entry.y] = '@';
            i = 1;
            for (Point pt : dijkstra.path) {
                md[pt.x * 2][pt.y] = ("" + (i % 10)).charAt(0);
                i++;
            }
            dg.setDungeon(md);

            System.out.println("FLEE PATH");
            System.out.println(dg);
            //debugPrint(dijkstra);
            System.out.println();

            lrng.setState(0x57a8deadbeef0ffal);
            rng = new RNG(lrng);
            DungeonUtility.rng = rng;

            dg.generate(TilesetType.DEFAULT_DUNGEON, 40, 40);
            dg.wallWrap();

            dun = dg.getDungeon();
            dijkstra.initialize(dun);

            System.out.println(dg);
//            DungeonUtility.randomFloorLarge(dun, size);
//            DungeonUtility.randomFloorLarge(dun, size);
            goal1 = DungeonUtility.randomFloorLarge(dun, size);
            goal2 = DungeonUtility.randomFloorLarge(dun, size);
            goal3 = DungeonUtility.randomFloorLarge(dun, size);
            goal4 = DungeonUtility.randomFloorLarge(dun, size);
            goal5 = DungeonUtility.randomFloorLarge(dun, size);

            entry = DungeonUtility.randomFloorLarge(dun, size);

            LOS los = new LOS();
            if(m == DijkstraMap.Measurement.MANHATTAN)
            {
                los.setRadiusStrategy(Radius.DIAMOND);
            }
            else
            {
                los.setRadiusStrategy(Radius.SQUARE);
            }

            dijkstra.findAttackPathLarge(size, 100, 6, 8, los, null, null, entry, goal1, goal2, goal3, goal4, goal5);

            gm = dijkstra.gradientMap;
            md = DungeonUtility.doubleWidth(dun);
            hl = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(dun));
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    char t = (char) 33;
                    if (x % 2 == 0 && ((x /2 == goal1.x && y == goal1.y) || (x /2 == goal2.x && y == goal2.y) ||
                            (x /2 == goal3.x && y == goal3.y) || (x /2 == goal4.x && y == goal4.y) ||
                            (x /2 == goal5.x && y == goal5.y)))
                        t = '*';
                    else if (x % 2 == 0 && gm[x / 2][y] < 200)
                        t = '.';// ("" + (gm[x / 2][y] % 10)).charAt(0);
                    else if (gm[x / 2][y] == DijkstraMap.WALL)
                        t = hl[x][y];
                    else
                        t = ' ';
                    md[x][y] = t;
                }
            }
            md[entry.x * 2][entry.y] = '@';
            i = 1;
            for (Point pt : dijkstra.path) {
                md[pt.x * 2][pt.y] = ("" + (i % 10)).charAt(0);
                i++;
            }
            dg.setDungeon(md);
            System.out.println("ATTACK PATH, 6-8 RANGE");
            System.out.println(dg);
//            debugPrint(dijkstra);
            System.out.println();


        }
    }
}
