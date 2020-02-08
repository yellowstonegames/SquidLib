package squidpony.performance;

import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.StatefulRNG;

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
final class DijkstraPerformanceTest extends AbstractPerformanceTest {
	// a 60 * 60 map should be possible to really profile
	private static final int WIDTH = 60, HEIGHT = 60, PATH_LENGTH = (WIDTH - 2) * (HEIGHT - 2);
	private final char[][] maps;

	public DijkstraPerformanceTest() {
		final DungeonGenerator generator = new DungeonGenerator(WIDTH, HEIGHT, RNG);
		maps = generator.generate();
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
		private DungeonUtility utility;
		private Coord[] floors;

		public Test(char[][] m) {
			map = m;
			utility = new DungeonUtility(new StatefulRNG(new LightRNG(0x1337BEEF)));
			floors = new GreasedRegion(map, '.').asCoords();
		}

		@Override
		protected void doWork() {
			DijkstraMap dijkstra = new DijkstraMap(map, new StatefulRNG(new LightRNG(0x1337BEEF)));
			Coord[] filler = new Coord[1];
			for (int x = 1; x < WIDTH - 1; x++) {
				for (int y = 1; y < HEIGHT - 1; y++) {
					if (map[x][y] == '#')
						continue;
					// this should ensure no blatant correlation between R and W
					utility.rng.setState((x << 22) | (y << 16) | (x * y));
					((StatefulRNG) dijkstra.rng).setState((x << 20) | (y << 14) | (x * y));
					filler[0] = utility.rng.getRandomElement(floors);
					dijkstra.setGoal(filler[0]);
					dijkstra.findPath(PATH_LENGTH, null, null, Coord.get(x, y), filler);
				}
			}
		}

	}
}
