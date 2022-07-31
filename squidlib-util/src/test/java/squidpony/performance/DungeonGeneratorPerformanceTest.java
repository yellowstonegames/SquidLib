package squidpony.performance;

import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.mapping.*;
import squidpony.squidgrid.mapping.styled.TilesetType;
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
	public static final int SIZE = 500;
	public DungeonGeneratorPerformanceTest() {
		Coord.expandPoolTo(SIZE, SIZE);
		createThreadList();
	}

	@Override
	protected AbstractPerformanceUnit createWorkUnit() {
		return new Test(SIZE, new RNG(new WhiskerRNG()));
	}

	/**
	 * separate thread that does the real test
	 * 
	 * @author David Becker
	 * @author Tommy Ettinger
	 */
	private static final class Test extends AbstractPerformanceUnit {

		private IDungeonGenerator gen;
		private TimedSectionDungeonGenerator dg;
		private RNG rng;

		public Test(int size, RNG rng) {
			this.rng = rng;
//			gen = new GrowingTreeMazeGenerator(size, size, rng); // Task took 2539.5 ms on average
//			gen = new ConnectingMapGenerator(size, size, 1, 1, rng, 1, 0.5); // Task took 1248.90625 ms on average
			dg = new TimedSectionDungeonGenerator(size, size, rng);
		}

		@Override
		protected void doWork() {
//			char[][] map = gen.generate();
//			dg.generate(map);
			dg.generate(TilesetType.DEFAULT_DUNGEON); // Task took 285.3125 ms on average
			dg.addStairs();
//			System.out.println("Up: " + dg.stairsUp + ", Down: " + dg.stairsDown);
/*
			DijkstraMap dm = new DijkstraMap(map, rng);
			GreasedRegion region = new GreasedRegion(map, '.'); // floors here
			Coord stairsUp = region.singleRandom(rng);
			dm.setGoal(stairsUp);
			double[][] distances = dm.scan();
			region.refill(distances, dm.getMappedCount() * 0.4, Double.MAX_VALUE); // floors with sufficient distance
			Coord stairsDown = region.singleRandom(rng);
//			System.out.println("Up: " + stairsUp + ", Down: " + stairsDown);
 */
		}

	}
}
