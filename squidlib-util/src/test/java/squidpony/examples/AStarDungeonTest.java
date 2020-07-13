package squidpony.examples;

import squidpony.squidai.astar.DefaultGraph;
import squidpony.squidai.astar.Heuristic;
import squidpony.squidai.astar.Pathfinder;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.AStarSearch;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.RNG;
import squidpony.squidmath.TangleRNG;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Tommy Ettinger on 4/5/2015.
 */
public class AStarDungeonTest {
    public static void main(String[] args) {
        for (AStarSearch.SearchType st : AStarSearch.SearchType.values()) {
            TangleRNG r = new TangleRNG(0x57a8deadbeef0ffaL);
            RNG rng = new RNG(r);
            DungeonGenerator dg = new DungeonGenerator(40, 40, rng);
            char[][] dun = dg.generate();
            Coord[] floors = new GreasedRegion(dun, '.').asCoords();
            double[][] costMap = DungeonUtility.generateAStarCostMap(dun, new HashMap<Character, Double>(0), 1);
            AStarSearch astar = new AStarSearch(costMap, st);
            System.out.println(dg);

            Coord goal1 = rng.getRandomElement(floors),
                    goal2 = rng.getRandomElement(floors), goal3 = rng.getRandomElement(floors),
                    goal4 = rng.getRandomElement(floors), goal5 = rng.getRandomElement(floors),
                    entry = rng.getRandomElement(floors);

            ArrayList<Coord> path1 = astar.path(entry, goal1);
            System.out.println(astar + "\n");
            ArrayList<Coord> path2 = astar.path(goal1, goal2);
            System.out.println(astar + "\n");
            ArrayList<Coord> path3 = astar.path(goal2, goal3);
            System.out.println(astar + "\n");
            ArrayList<Coord> path4 = astar.path(goal3, goal4);
            System.out.println(astar + "\n");
            ArrayList<Coord> path5 = astar.path(goal4, goal5);
            System.out.println(astar + "\n");
        }
        for(Heuristic<Coord> h : DefaultGraph.HEURISTICS) {
            TangleRNG r = new TangleRNG(0x57a8deadbeef0ffaL);
            RNG rng = new RNG(r);
            DungeonGenerator dg = new DungeonGenerator(40, 40, rng);
            char[][] dun = dg.generate();
            Coord[] floors = new GreasedRegion(dun, '.').asCoords();
            DefaultGraph graph = new DefaultGraph(dun, h != DefaultGraph.MANHATTAN);
            Pathfinder<Coord> astar = new Pathfinder<>(graph, true);
            System.out.println(dg);

            Coord goal1 = rng.getRandomElement(floors),
                    goal2 = rng.getRandomElement(floors), goal3 = rng.getRandomElement(floors),
                    goal4 = rng.getRandomElement(floors), goal5 = rng.getRandomElement(floors),
                    entry = rng.getRandomElement(floors);
            ArrayList<Coord> path = new ArrayList<>(50);
            StringBuilder sb = new StringBuilder(3 * 40 * 41);
            astar.searchNodePath(entry, goal1, h, path);
            System.out.println(graph.show(sb, astar, astar.metrics).append('\n'));
            sb.setLength(0);
            astar.searchNodePath(goal1, goal2, h, path);
            System.out.println(graph.show(sb, astar, astar.metrics).append('\n'));
            sb.setLength(0);
            astar.searchNodePath(goal2, goal3, h, path);
            System.out.println(graph.show(sb, astar, astar.metrics).append('\n'));
            sb.setLength(0);
            astar.searchNodePath(goal3, goal4, h, path);
            System.out.println(graph.show(sb, astar, astar.metrics).append('\n'));
            sb.setLength(0);
            astar.searchNodePath(goal4, goal5, h, path);
            System.out.println(graph.show(sb, astar, astar.metrics).append('\n'));
            sb.setLength(0);
        }
    }
}
