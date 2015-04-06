package squidpony.examples;

import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.DungeonGen;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.DijkstraMap;
import squidpony.squidmath.LightRNG;

import java.awt.Point;

/**
 * Created by Tommy Ettinger on 4/5/2015.
 */
public class DijkstraDungeonTest {
    public static void main(String[] args) {
        for (DijkstraMap.Measurement m : DijkstraMap.Measurement.values()) {
            LightRNG rng = new LightRNG(0x1337deadbeefc000l);
            DungeonUtility.rng = rng;
            DungeonGen bg = new DungeonGen(rng);

            bg.generate(TilesetType.DEFAULT_DUNGEON, 40, 40);
            bg.wallWrap();

            char[][] dun = bg.getDungeon();
            DijkstraMap dijkstra = new DijkstraMap(dun, m);

            System.out.println(bg);

            Point entry = DungeonUtility.randomFloor(dun), goal1 = DungeonUtility.randomFloor(dun),
                    goal2 = DungeonUtility.randomFloor(dun), goal3 = DungeonUtility.randomFloor(dun);
            dijkstra.findPath(100, null, null, entry, goal1, goal2, goal3);
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
            bg.setDungeon(md);
            System.out.println(bg);


            rng.setState(0x1337deadbeefc000l);
            DungeonUtility.rng = rng;

            bg.generate(TilesetType.DEFAULT_DUNGEON, 40, 40);
            bg.wallWrap();

            dun = bg.getDungeon();
            dijkstra.initialize(dun);

            System.out.println(bg);

            entry = DungeonUtility.randomFloor(dun);
            goal1 = DungeonUtility.randomFloor(dun);
            goal2 = DungeonUtility.randomFloor(dun);
            goal3 = DungeonUtility.randomFloor(dun);
            dijkstra.findFleePath(100, 2.5, null, null, entry, goal1, goal2, goal3);

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
            bg.setDungeon(md);
            System.out.println(bg);

            System.out.println();


        }
    }
}
