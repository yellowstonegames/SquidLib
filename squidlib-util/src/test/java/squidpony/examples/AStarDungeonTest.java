package squidpony.examples;

import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.*;

import java.util.Collections;
import java.util.Queue;

/**
 * Created by Tommy Ettinger on 4/5/2015.
 */
public class AStarDungeonTest {
    public static void main(String[] args) {
        for (AStarSearch.SearchType st : AStarSearch.SearchType.values()) {
            ThrustAltRNG lrng = new ThrustAltRNG(0x57a8deadbeef0ffaL);
            RNG rng = new RNG(lrng);
            DungeonGenerator dg = new DungeonGenerator(40, 40, rng);
            char[][] dun = dg.generate();
            Coord[] floors = new GreasedRegion(dun, '.').asCoords();
            double[][] costMap = DungeonUtility.generateAStarCostMap(dun, Collections.<Character, Double>emptyMap(), 0);
            AStarSearch astar = new AStarSearch(costMap, st);
            System.out.println(dg);

            Coord goal1 = rng.getRandomElement(floors),
                    goal2 = rng.getRandomElement(floors), goal3 = rng.getRandomElement(floors),
                    goal4 = rng.getRandomElement(floors), goal5 = rng.getRandomElement(floors),
                    entry = rng.getRandomElement(floors);

            Queue<Coord> path1 = astar.path(entry, goal1);
            System.out.println(astar + "\n");
            Queue<Coord> path2 = astar.path(goal1, goal2);
            System.out.println(astar + "\n");
            Queue<Coord> path3 = astar.path(goal2, goal3);
            System.out.println(astar + "\n");
            Queue<Coord> path4 = astar.path(goal3, goal4);
            System.out.println(astar + "\n");
            Queue<Coord> path5 = astar.path(goal4, goal5);
            System.out.println(astar + "\n");
        }
    }
}
