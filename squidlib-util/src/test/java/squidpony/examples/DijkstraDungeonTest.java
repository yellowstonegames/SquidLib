package squidpony.examples;

import squidpony.squidgrid.LOS;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.DungeonBoneGen;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidai.DijkstraMap;
import squidpony.squidmath.Coord;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

/**
 * Created by Tommy Ettinger on 4/5/2015.
 */
public class DijkstraDungeonTest {
    public static void main(String[] args) {
        for (DijkstraMap.Measurement m : DijkstraMap.Measurement.values()) {
            LightRNG lrng = new LightRNG(0x57a8deadbeef0ffal);
            RNG rng = new RNG(lrng);
            DungeonGenerator dg = new DungeonGenerator(40, 40, rng);
            char[][] dun = dg.generate();
            DijkstraMap dijkstra = new DijkstraMap(dun, m);

            System.out.println(dg);

            Coord goal1 = dg.utility.randomFloor(dun),
                    goal2 = dg.utility.randomFloor(dun), goal3 = dg.utility.randomFloor(dun),
                    goal4 = dg.utility.randomFloor(dun), goal5 = dg.utility.randomFloor(dun),
                    entry = dg.utility.randomFloor(dun);

            dijkstra.findPath(100, null, null, entry, goal1, goal2, goal3, goal4, goal5);
            double[][] gm = dijkstra.gradientMap;
            char[][] md = DungeonUtility.doubleWidth(dun),
                    hl = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(dun));
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    char t;
                    if (x % 2 == 0 && gm[x / 2][y] < 200)
                        t = '.';
                    else if (gm[x / 2][y] == DijkstraMap.WALL)
                        t = hl[x][y];
                    else
                        t = ' ';
                    md[x][y] = t;
                }
            }
            md[entry.x * 2][entry.y] = '@';
            int i = 1;
            for (Coord pt : dijkstra.path) {
                md[pt.x * 2][pt.y] = ("" + (i++ % 10)).charAt(0);
            }
            dg.setDungeon(md);
            System.out.println(dg);


            lrng.setState(0x57a8deadbeef0ffal);
            rng = new RNG(lrng);
            dg = new DungeonGenerator(40, 40, rng);
            dun = dg.generate();
            dijkstra.initialize(dun);

            System.out.println(dg);

            goal1 = dg.utility.randomFloor(dun);
            goal2 = dg.utility.randomFloor(dun);
            goal3 = dg.utility.randomFloor(dun);
            goal4 = dg.utility.randomFloor(dun);
            goal5 = dg.utility.randomFloor(dun);

            entry = dg.utility.randomFloor(dun);

            dijkstra.findFleePath(100, 1.9, null, null, entry, goal1, goal2, goal3, goal4, goal5);

            gm = dijkstra.gradientMap;
            md = DungeonUtility.doubleWidth(dun);
            hl = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(dun));
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    char t;
                    if (x % 2 == 0 && gm[x / 2][y] < 200)
                        t = '.';
                    else if (gm[x / 2][y] == DijkstraMap.WALL)
                        t = hl[x][y];
                    else
                        t = ' ';
                    md[x][y] = t;
                }
            }
            md[entry.x * 2][entry.y] = '@';
            i = 1;
            for (Coord pt : dijkstra.path) {
                md[pt.x * 2][pt.y] = ("" + (i++ % 10)).charAt(0);
            }
            dg.setDungeon(md);
            System.out.println(dg);

            System.out.println();


            lrng.setState(0x57a8deadbeef0ffal);
            rng = new RNG(lrng);
            dg = new DungeonGenerator(40, 40, rng);
            dun = dg.generate();
            dijkstra.initialize(dun);

            System.out.println(dg);

            goal1 = dg.utility.randomFloor(dun);
            goal2 = dg.utility.randomFloor(dun);
            goal3 = dg.utility.randomFloor(dun);
            goal4 = dg.utility.randomFloor(dun);
            goal5 = dg.utility.randomFloor(dun);

            entry = dg.utility.randomFloor(dun);

            LOS los = new LOS();
            los.setRadiusStrategy(Radius.DIAMOND);

            dijkstra.findAttackPath(100, 6, 8, los, null, null, entry, goal1, goal2, goal3, goal4, goal5);

            gm = dijkstra.gradientMap;
            md = DungeonUtility.doubleWidth(dun);
            hl = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(dun));
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    char t;
                    if (x % 2 == 0 && ((x /2 == goal1.x && y == goal1.y) || (x /2 == goal2.x && y == goal2.y) ||
                            (x /2 == goal3.x && y == goal3.y) || (x /2 == goal4.x && y == goal4.y)))
                        t = '*';
                    else if (x % 2 == 0 && gm[x / 2][y] < 200)
                        t = '.';
                    else if (gm[x / 2][y] == DijkstraMap.WALL)
                        t = hl[x][y];
                    else
                        t = ' ';
                    md[x][y] = t;
                }
            }
            md[entry.x * 2][entry.y] = '@';
            i = 1;
            for (Coord pt : dijkstra.path) {
                md[pt.x * 2][pt.y] = ("" + (i++ % 10)).charAt(0);
            }
            dg.setDungeon(md);
            System.out.println(dg);

            System.out.println();


        }
    }
}
