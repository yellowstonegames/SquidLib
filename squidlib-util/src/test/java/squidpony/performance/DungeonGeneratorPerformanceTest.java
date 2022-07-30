package squidpony.performance;

import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.mapping.ConnectingMapGenerator;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.IDungeonGenerator;
import squidpony.squidmath.*;

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
final class DungeonGeneratorPerformanceTest extends AbstractPerformanceTest {
	public DungeonGeneratorPerformanceTest() {
		Coord.expandPoolTo(1000, 1000);
		createThreadList();
	}

	@Override
	protected AbstractPerformanceUnit createWorkUnit() {
		return new Test(1000, new RNG(new WhiskerRNG(1L)));
	}

	/**
	 * separate thread that does the real test
	 * 
	 * @author David Becker
	 * @author Tommy Ettinger
	 */
	private static final class Test extends AbstractPerformanceUnit {

		private IDungeonGenerator gen;
		private DungeonGenerator dg;
		private RNG rng;

		public Test(int size, RNG rng) {
			this.rng = rng;
			gen = new ConnectingMapGenerator(size, size, 1, 1, rng, 1, 0.5);
			dg = new DungeonGenerator(20, 20, rng);
		}

		@Override
		protected void doWork() {
//			dg.generate(
			char[][] map = gen.generate();
			DijkstraMap dm = new DijkstraMap(map, rng);
			GreasedRegion region = new GreasedRegion(map, '.'); // floors here
			Coord stairsUp = region.singleRandom(rng);
			dm.setGoal(stairsUp);
			double[][] distances = dm.scan();
			region.refill(distances, dm.getMappedCount() * 0.4, Double.MAX_VALUE); // floors with sufficient distance
			Coord stairsDown = region.singleRandom(rng);
//			);
//			dg.addStairs();
		}

	}
}
