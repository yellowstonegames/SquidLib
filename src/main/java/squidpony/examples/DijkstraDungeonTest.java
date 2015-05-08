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
public class DijkstraDungeonTest {
    public static void main(String[] args) {
        for (DijkstraMap.Measurement m : DijkstraMap.Measurement.values()) {
            LightRNG lrng = new LightRNG(0x57a8deadbeef0ffal);
            RNG rng = new RNG(lrng);
            DungeonUtility.rng = rng;
            DungeonBoneGen dg = new DungeonBoneGen(rng);

            dg.generate(TilesetType.DEFAULT_DUNGEON, 40, 40);
            dg.wallWrap();

            char[][] dun = dg.getDungeon();
            DijkstraMap dijkstra = new DijkstraMap(dun, m);

            System.out.println(dg);

            Point goal1 = DungeonUtility.randomFloor(dun),
                    goal2 = DungeonUtility.randomFloor(dun), goal3 = DungeonUtility.randomFloor(dun),
                    goal4 = DungeonUtility.randomFloor(dun), goal5 = DungeonUtility.randomFloor(dun),
                    entry = DungeonUtility.randomFloor(dun);

            dijkstra.findPath(100, null, null, entry, goal1, goal2, goal3, goal4, goal5);
            double[][] gm = dijkstra.gradientMap;
            char[][] md = DungeonUtility.doubleWidth(dun),
                    hl = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(dun));
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    char t = (char) 33;
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
            for (Point pt : dijkstra.path) {
                md[pt.x * 2][pt.y] = ("" + (i++ % 10)).charAt(0);
            }
            dg.setDungeon(md);
            System.out.println(dg);


            lrng.setState(0x57a8deadbeef0ffal);
            rng = new RNG(lrng);
            DungeonUtility.rng = rng;

            dg.generate(TilesetType.DEFAULT_DUNGEON, 40, 40);
            dg.wallWrap();

            dun = dg.getDungeon();
            dijkstra.initialize(dun);

            System.out.println(dg);

            goal1 = DungeonUtility.randomFloor(dun);
            goal2 = DungeonUtility.randomFloor(dun);
            goal3 = DungeonUtility.randomFloor(dun);
            goal4 = DungeonUtility.randomFloor(dun);
            goal5 = DungeonUtility.randomFloor(dun);

            entry = DungeonUtility.randomFloor(dun);

            dijkstra.findFleePath(100, 1.9, null, null, entry, goal1, goal2, goal3, goal4, goal5);

            gm = dijkstra.gradientMap;
            md = DungeonUtility.doubleWidth(dun);
            hl = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(dun));
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    char t = (char) 33;
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
            for (Point pt : dijkstra.path) {
                md[pt.x * 2][pt.y] = ("" + (i++ % 10)).charAt(0);
            }
            dg.setDungeon(md);
            System.out.println(dg);

            System.out.println();


            lrng.setState(0x57a8deadbeef0ffal);
            rng = new RNG(lrng);
            DungeonUtility.rng = rng;

            dg.generate(TilesetType.DEFAULT_DUNGEON, 40, 40);
            dg.wallWrap();

            dun = dg.getDungeon();
            dijkstra.initialize(dun);

            System.out.println(dg);

            goal1 = DungeonUtility.randomFloor(dun);
            goal2 = DungeonUtility.randomFloor(dun);
            goal3 = DungeonUtility.randomFloor(dun);
            goal4 = DungeonUtility.randomFloor(dun);
            goal5 = DungeonUtility.randomFloor(dun);

            entry = DungeonUtility.randomFloor(dun);

            LOS los = new LOS();
            los.setRadiusStrategy(Radius.DIAMOND);

            dijkstra.findAttackPath(100, 6, 8, los, null, null, entry, goal1, goal2, goal3, goal4, goal5);

            gm = dijkstra.gradientMap;
            md = DungeonUtility.doubleWidth(dun);
            hl = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(dun));
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    char t = (char) 33;
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
            for (Point pt : dijkstra.path) {
                md[pt.x * 2][pt.y] = ("" + (i++ % 10)).charAt(0);
            }
            dg.setDungeon(md);
            System.out.println(dg);

            System.out.println();


        }
    }
}
