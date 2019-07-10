package squidpony.examples;

import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.LOS;
import squidpony.squidgrid.Measurement;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.Coord;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import java.util.HashMap;

/**
 * Created by Tommy Ettinger on 9/8/2015.
 */
public class DijkstraCostTest {
    public static void main(String[] args) {
        for (Measurement m : Measurement.values()) {
            LightRNG lrng = new LightRNG(0xdeadbeef0ffaL);
            RNG rng = new RNG(lrng);
            DungeonGenerator dg = new DungeonGenerator(40, 40, rng);
            dg.addWater(30);
            dg.addDoors(8, true);

            char[][] dun = dg.generate(TilesetType.DEFAULT_DUNGEON);
            HashMap<Character, Double> aquatic = new HashMap<>(16);
            aquatic.put('~', 1.0);
            aquatic.put(',', 1.0);
            double[][] costs = DungeonUtility.generateCostMap(dun, aquatic, 999.0);
            DijkstraMap dijkstra = new DijkstraMap(dun, m);
            dijkstra.initializeCost(costs);
            System.out.println(dg);

            Coord goal1 = dg.utility.randomFloor(dun),
                    goal2 = dg.utility.randomFloor(dun), goal3 = dg.utility.randomFloor(dun),
                    goal4 = dg.utility.randomMatchingTile(dun, '~'), goal5 = dg.utility.randomMatchingTile(dun, '~'),
                    entry = dg.utility.randomMatchingTile(dun, '~');

            dijkstra.findPath(10, null, null, entry, goal1, goal2, goal3, goal4, goal5);
            double[][] gm = dijkstra.gradientMap;
            char[][] md = DungeonUtility.doubleWidth(dun),
                    hl = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(dun));
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    char t;
                    if (x % 2 == 0 && gm[x / 2][y] < 200)
                        t = dun[x/2][y];
                    else if (gm[x / 2][y] == DijkstraMap.WALL)
                        t = hl[x][y];
                    else if(x % 2 == 1)
                        t = dun[x/2][y];
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

            md[goal1.x * 2][goal1.y] = '*';
            md[goal2.x * 2][goal2.y] = '*';
            md[goal3.x * 2][goal3.y] = '*';
            md[goal4.x * 2][goal4.y] = '*';
            md[goal5.x * 2][goal5.y] = '*';

            dg.setDungeon(md);
            System.out.println(dg);


            lrng.setState(0x2deadbeef0ffaL);
            dg = new DungeonGenerator(40, 40, rng);
            dg.addWater(10);
            dg.addDoors(16, true);

            dun = dg.generate(TilesetType.DEFAULT_DUNGEON);
            HashMap<Character, Double> vampire = new HashMap<>(16);
            vampire.put('/', 999.0);
            vampire.put('+', 999.0);
            vampire.put('~', 3.0);
            vampire.put(',', 2.0);
            costs = DungeonUtility.generateCostMap(dun, vampire, 1.0);
            dijkstra.initialize(dun);
            dijkstra.initializeCost(costs);

            System.out.println(dg);

            goal1 = dg.utility.randomFloor(dun);
            goal2 = dg.utility.randomFloor(dun);
            goal3 = dg.utility.randomFloor(dun);
            goal4 = dg.utility.randomFloor(dun);
            goal5 = dg.utility.randomFloor(dun);

            entry = dg.utility.randomFloor(dun);

            dijkstra.findPath(13, null, null, entry, goal1, goal2, goal3, goal4, goal5);

            gm = dijkstra.gradientMap;
            md = DungeonUtility.doubleWidth(dun);
            hl = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(dun));
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    char t;
                    if (x % 2 == 0 && gm[x / 2][y] < 200)
                        t = dun[x/2][y];
                    else if (gm[x / 2][y] == DijkstraMap.WALL)
                        t = hl[x][y];
                    else if(x % 2 == 1)
                        t = dun[x/2][y];
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

            md[goal1.x * 2][goal1.y] = '*';
            md[goal2.x * 2][goal2.y] = '*';
            md[goal3.x * 2][goal3.y] = '*';
            md[goal4.x * 2][goal4.y] = '*';
            md[goal5.x * 2][goal5.y] = '*';

            dg.setDungeon(md);
            System.out.println(dg);

            System.out.println();


            lrng.setState(0x2deadbeef0ffaL);
            dg = new DungeonGenerator(40, 40, rng);
            dg.addWater(40);
            dg.addDoors(10, true);

            dun = DungeonUtility.closeDoors(dg.generate(TilesetType.DEFAULT_DUNGEON));
            HashMap<Character, Double> fancy = new HashMap<>(16);
            fancy.put(',', 2.0);
            fancy.put('~', 8.0);
            fancy.put('+', 4.0);
            costs = DungeonUtility.generateCostMap(dun, fancy, 1.0);
            dijkstra.initialize(dun);
            dijkstra.initializeCost(costs);
            System.out.println(dg);

            goal1 = dg.utility.randomFloor(dun);
            goal2 = dg.utility.randomFloor(dun);
            goal3 = dg.utility.randomFloor(dun);
            goal4 = dg.utility.randomFloor(dun);
            goal5 = dg.utility.randomFloor(dun);

            entry = dg.utility.randomFloor(dun);

            LOS los = new LOS();
            los.setRadiusStrategy(Radius.DIAMOND);

            dijkstra.findPath(10, null, null, entry, goal1, goal2, goal3, goal4, goal5);

            gm = dijkstra.gradientMap;
            md = DungeonUtility.doubleWidth(dun);
            hl = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(dun));
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    char t;
                    if (x % 2 == 0 && gm[x / 2][y] < 200)
                        t = dun[x/2][y];
                    else if (gm[x / 2][y] == DijkstraMap.WALL)
                        t = hl[x][y];
                    else if(x % 2 == 1)
                        t = dun[x/2][y];
                    else
                        t = ' ';
                    md[x][y] = t;
                }
            }
            md[entry.x * 2][entry.y] = '@';
            i = 1;
            for (Coord pt : dijkstra.path) {
                md[pt.x * 2][pt.y] = (char)('0' + (i++ % 10));
            }

            md[goal1.x * 2][goal1.y] = '*';
            md[goal2.x * 2][goal2.y] = '*';
            md[goal3.x * 2][goal3.y] = '*';
            md[goal4.x * 2][goal4.y] = '*';
            md[goal5.x * 2][goal5.y] = '*';

            dg.setDungeon(md);
            System.out.println(dg);

            System.out.println();


        }
    }

}
