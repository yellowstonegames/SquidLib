package squidpony.performance;

import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.LOS;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * a simple performance test
 *
 * steps taken:
 * <ul>
 * <li>generate dungeon</li>
 * <li>for every walkable cell <b>W</b> in the dungeon:</li>
 * <li>
 * <ul>
 * <li>generate a random walkable cell <b>R</b> using DungeonUtility.randomFloor
 * </li>
 * <li>compute DijkstraMap, spanning the entire dungeon level, with the goals
 * being <b>W</b> and <b>R</b></li>
 * <li>compute path using findPath from <b>W</b> to <b>R</b></li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author David Becker
 * @author Tommy Ettinger
 *
 */
public final class DijkstraPerformanceTest extends AbstractPerformanceTest {
	// a 60 * 60 map should be more taxing
	private static final int DIMENSION = 60, PATH_LENGTH = (DIMENSION - 2) * (DIMENSION - 2);
	private final char[][] maps;

	private DijkstraPerformanceTest() {
		final DungeonGenerator generator = new DungeonGenerator(DIMENSION, DIMENSION, RNG);
		maps = generator.generate();
		System.out.println(generator.toString());
		createThreadList();
	}

	@Override
	protected AbstractPerformanceUnit createWorkUnit() {
		return new Test(maps);
	}

	/**
	 * separate thread that does the real test
	 * 
	 * @author David Becker
	 * @author Tommy Ettinger
	 */
	private static final class Test extends AbstractPerformanceUnit {

		private char[][] map;
		private DijkstraMap dijkstra;
		private DungeonUtility utility;

		public Test(char[][] m) {
			map = m;
			dijkstra = new DijkstraMap(map, new StatefulRNG(new LightRNG(0x1337BEEF)));
			utility = new DungeonUtility(new StatefulRNG(new LightRNG(0x1337BEEF)));
		}

		@Override
		protected void doWork() {
			Coord r;

			for (int x = 1; x < DIMENSION - 1; x++) {
				for (int y = 1; y < DIMENSION - 1; y++) {
					if (map[x][y] == '#')
						continue;
					// this should ensure no blatant correlation between R and W
					utility.rng.setState((x << 22) | (y << 16) | (x * y));
					((StatefulRNG) dijkstra.rng).setState((x << 20) | (y << 14) | (x * y));
					r = utility.randomFloor(map);
					dijkstra.setGoal(x, y);
					dijkstra.setGoal(r);
					dijkstra.scan(null);
					dijkstra.clearGoals();
					dijkstra.findPath(PATH_LENGTH, null, null, Coord.get(x, y), r);
				}
			}
		}

	}
}
