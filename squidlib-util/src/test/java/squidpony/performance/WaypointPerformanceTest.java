package squidpony.performance;

import squidpony.squidai.WaypointPathfinder;
import squidpony.squidgrid.Radius;
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
 * <ul>
 * <li>generate a random walkable cell <b>R</b> using DungeonUtility.randomFloor</li>
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
final class WaypointPerformanceTest extends AbstractPerformanceTest {
	// a 60 * 60 map should be possible to really profile
	private static final int WIDTH = 60, HEIGHT = 60;
	private final char[][] maps;

	public WaypointPerformanceTest() {
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
		private WaypointPathfinder pathfinder;
		private DungeonUtility utility;
		private Coord[] floors;

		public Test(char[][] map) {
			this.map = map;
			utility = new DungeonUtility(new StatefulRNG(new LightRNG(0x1337BEEF)));
			floors = new GreasedRegion(map, '.').asCoords();
		}

		@Override
		protected void doWork() {
			Coord r, s;
			pathfinder = new WaypointPathfinder(map, Radius.DIAMOND, new StatefulRNG(new LightRNG(0x1337BEEF)));
			for (int x = 1; x < WIDTH - 1; x++) {
				for (int y = 1; y < HEIGHT - 1; y++) {
					if (map[x][y] == '#')
						continue;

                    s = Coord.get(x, y);
					// this should ensure no blatant correlation between R and W
					utility.rng.setState((x << 22) | (y << 16) | (x * y));
					pathfinder.rng.setState((x << 20) | (y << 14) | (x * y));
					r = utility.rng.getRandomElement(floors);
					pathfinder.getKnownPath(s, r);
                }
			}
		}

	}
}
