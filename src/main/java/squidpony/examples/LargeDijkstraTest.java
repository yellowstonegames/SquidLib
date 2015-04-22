package squidpony.examples;

import squidpony.squidgrid.LOS;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.DungeonGen;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.DijkstraMap;
import squidpony.squidmath.LightRNG;

import java.awt.Point;

/**
 * Created by Tommy Ettinger on 4/5/2015.
 */
public class LargeDijkstraTest {
    public static void main(String[] args) {
        for (DijkstraMap.Measurement m : DijkstraMap.Measurement.values()) {
            LightRNG rng = new LightRNG(0x57a8deadbeef0ffal);

            int size = 2;
            DungeonUtility.rng = rng;
            DungeonGen dg = new DungeonGen(rng);

            dg.generate(TilesetType.DEFAULT_DUNGEON, 40, 40);
            dg.wallWrap();

            char[][] dun = dg.getDungeon();
            DijkstraMap dijkstra = new DijkstraMap(dun, m);

            System.out.println(dg);
            DungeonUtility.randomFloorLarge(dun, size);
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
                        t = '.';
                    else if (gm[x / 2][y] == DijkstraMap.WALL)
                        t = hl[x][y];
                    else
                        t = ' ';
                    md[x][y] = t;
                }
            }
            md[entry.x * 2][entry.y] = '@';
            md[entry.x * 2 + 2][entry.y] = '@';
            md[entry.x * 2][entry.y + 1] = '@';
            md[entry.x * 2 + 2][entry.y + 1] = '@';
            int i = 1;
            for (Point pt : dijkstra.path) {
                md[pt.x * 2][pt.y] = ("" + (i % 10)).charAt(0);
                md[pt.x * 2 + 2][pt.y] = ("" + (i % 10)).charAt(0);
                md[pt.x * 2][pt.y + 1] = ("" + (i % 10)).charAt(0);
                md[pt.x * 2 + 2][pt.y + 1] = ("" + (i % 10)).charAt(0);
                i++;
            }
            dg.setDungeon(md);
            System.out.println(dg);


            rng.setState(0x57a8deadbeef0ffal);
            DungeonUtility.rng = rng;

            dg.generate(TilesetType.DEFAULT_DUNGEON, 40, 40);
            dg.wallWrap();

            dun = dg.getDungeon();
            dijkstra.initialize(dun);

            System.out.println(dg);
            DungeonUtility.randomFloorLarge(dun, size);
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
                        t = '.';
                    else if (gm[x / 2][y] == DijkstraMap.WALL)
                        t = hl[x][y];
                    else
                        t = ' ';
                    md[x][y] = t;
                }
            }
            md[entry.x * 2][entry.y] = '@';
            md[entry.x * 2 + 2][entry.y] = '@';
            md[entry.x * 2][entry.y + 1] = '@';
            md[entry.x * 2 + 2][entry.y + 1] = '@';
            i = 1;
            for (Point pt : dijkstra.path) {
                md[pt.x * 2][pt.y] = ("" + (i % 10)).charAt(0);
                md[pt.x * 2 + 2][pt.y] = ("" + (i % 10)).charAt(0);
                md[pt.x * 2][pt.y + 1] = ("" + (i % 10)).charAt(0);
                md[pt.x * 2 + 2][pt.y + 1] = ("" + (i % 10)).charAt(0);
                i++;
            }
            dg.setDungeon(md);
            System.out.println(dg);

            System.out.println();


            rng.setState(0x57a8deadbeef0ffal);
            DungeonUtility.rng = rng;

            dg.generate(TilesetType.DEFAULT_DUNGEON, 40, 40);
            dg.wallWrap();

            dun = dg.getDungeon();
            dijkstra.initialize(dun);

            System.out.println(dg);
            DungeonUtility.randomFloorLarge(dun, size);
            goal1 = DungeonUtility.randomFloorLarge(dun, size);
            goal2 = DungeonUtility.randomFloorLarge(dun, size);
            goal3 = DungeonUtility.randomFloorLarge(dun, size);
            goal4 = DungeonUtility.randomFloorLarge(dun, size);
            goal5 = DungeonUtility.randomFloorLarge(dun, size);

            entry = DungeonUtility.randomFloorLarge(dun, size);

            LOS los = new LOS();
            los.setRadiusStrategy(Radius.DIAMOND);

            dijkstra.findAttackPathLarge(size, 100, 6, 8, los, null, null, entry, goal1, goal2, goal3, goal4, goal5);

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
            md[entry.x * 2 + 2][entry.y] = '@';
            md[entry.x * 2][entry.y + 1] = '@';
            md[entry.x * 2 + 2][entry.y + 1] = '@';
            i = 1;
            for (Point pt : dijkstra.path) {
                md[pt.x * 2][pt.y] = ("" + (i % 10)).charAt(0);
                md[pt.x * 2 + 2][pt.y] = ("" + (i % 10)).charAt(0);
                md[pt.x * 2][pt.y + 1] = ("" + (i % 10)).charAt(0);
                md[pt.x * 2 + 2][pt.y + 1] = ("" + (i % 10)).charAt(0);
                i++;
            }
            dg.setDungeon(md);
            System.out.println(dg);

            System.out.println();


        }
    }
}
