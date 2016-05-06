package squidpony.squidmath;

import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.mapping.DungeonUtility;

/**
 * (SLOW) Modified version of AStarSearch that does some pre-planning and uses that to speed up pathfinding later on.
 * Pre-planning is in the form of full distance calculations using DijkstraMap, with each centered on an infrequent
 * landmark somewhere on a walkable tile on the map. This affects the A* heuristic and allows it to establish a better
 * lower bound for distance. Landmark placement is non-random; 1 out of 85 walkable tiles will be made into a landmark,
 * and they will be considered in Hilbert Curve order (which helps prevent nearby areas from being chosen twice).
 * <br>
 * Idea can be credited independently to Joshua Day, who mentioned this long before I understood it, and Amit Patel,
 * http://www.redblobgames.com/pathfinding/heuristics/differential.html
 * <br>
 * NOTE: Due to implementation problems that seem to also be present in AStarSearch, this class does not offer any speed
 * improvements or any other benefits. It may be fixed in future versions, but for now you should strongly prefer
 * DijkstraMap for pathfinding.
 * @see squidpony.squidai.DijkstraMap a much faster pathfinding algorithm with more features.
 * Created by Tommy Ettinger on 5/5/2016.
 */
public class PlannedAStar extends AStarSearch {
    protected DijkstraMap dm;
    protected double[][][] plans;
    protected Coord[] landmarks;
    protected PlannedAStar() {
    }

    public PlannedAStar(double[][] map, SearchType type) {
        super(map, type);
        dm = new DijkstraMap(DungeonUtility.translateAStarToDijkstra(map), translateSearchType(type));
        landmarks = CoordPacker.fractionPacked(CoordPacker.pack(dm.physicalMap, DijkstraMap.FLOOR),
                Math.round((width + height) * 3.7f));
        plans = new double[landmarks.length][][];
        for (int i = 0; i < landmarks.length; i++) {
            dm.setGoal(landmarks[i]);
            plans[i] = dm.scan(null);
            dm.reset();
        }
    }

    protected static DijkstraMap.Measurement translateSearchType(SearchType type)
    {
        if(type == null)
            return DijkstraMap.Measurement.MANHATTAN;
        switch (type)
        {
            case EUCLIDEAN: return DijkstraMap.Measurement.EUCLIDEAN;
            case MANHATTAN: return DijkstraMap.Measurement.MANHATTAN;
            default: return DijkstraMap.Measurement.CHEBYSHEV;
        }
    }

    @Override
    protected double h(int x, int y) {
        double d = super.h(x, y);
        for (int i = 0; i < plans.length; i++) {
            d = Math.max(d, plans[i][target.x][target.y] - plans[i][x][y]);
        }
        return d;
    }
}
