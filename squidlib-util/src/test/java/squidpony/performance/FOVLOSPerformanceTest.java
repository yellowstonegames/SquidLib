package squidpony.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import squidpony.squidgrid.FOV;
import squidpony.squidgrid.LOS;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;
import squidpony.squidmath.RandomnessSource;

/**
 * a simple performance test
 *
 * steps taken:
 * <ul>
 * <li>generate dungeon</li>
 * <li>compute FOV for each position on the map for each FOV algorithm</li>
 * <li>compute LOS for each position on the map to the corners for each LOS
 * algorithm</li>
 * </ul>
 * 
 * @author David Becker
 *
 */
public final class FOVLOSPerformanceTest extends AbstractPerformanceTest {
	// we want predictable outcome for our test
	private static final RandomnessSource SOURCE = new LightRNG(0x1337BEEF);
	private static final RNG RNG = new RNG(SOURCE);

	// a 30 * 30 map should be enough
	private static final int DIMENSION = 30;
	private final char[][] maps;
	private final double[][] res;

	private FOVLOSPerformanceTest() {
		final DungeonGenerator generator = new DungeonGenerator(DIMENSION, DIMENSION, RNG);
		maps = generator.generate();
		res = DungeonUtility.generateResistances(maps);
		createThreadList();
	}

	@Override
	protected AbstractPerformanceUnit createWorkUnit() {
		return new Test(maps, res);
	}

	/**
	 * separate thread that does the real test
	 * 
	 * @author David Becker
	 *
	 */
	private static final class Test extends AbstractPerformanceUnit {

		private char[][] map;
		private double[][] res;

		public Test(char[][] m, double[][] r) {
			map = m;
			res = r;
		}

		@Override
		protected void doWork() {
			final FOV fovRipple = new FOV(FOV.RIPPLE);
			final FOV fovRippleL = new FOV(FOV.RIPPLE_LOOSE);
			final FOV fovRippleT = new FOV(FOV.RIPPLE_TIGHT);
			final FOV fovRippleV = new FOV(FOV.RIPPLE_VERY_LOOSE);
			final FOV fovShadow = new FOV(FOV.SHADOW);
			final LOS losBresenham = new LOS(LOS.BRESENHAM);
			final LOS losElias = new LOS(LOS.ELIAS);
			final LOS losRay = new LOS(LOS.RAY);
			final int end = DIMENSION - 2;

			for (int x = 1; x < DIMENSION - 1; x++) {
				for (int y = 1; y < DIMENSION - 1; y++) {
					fovRipple.calculateFOV(res, x, y);
					fovRippleL.calculateFOV(res, x, y);
					fovRippleT.calculateFOV(res, x, y);
					// FIXME causes exception !
					// fovRippleV.calculateFOV(res, x, y);
					fovShadow.calculateFOV(res, x, y);

					losBresenham.isReachable(map, x, y, 1, 1);
					losBresenham.isReachable(map, x, y, 1, end);
					losBresenham.isReachable(map, x, y, end, 1);
					losBresenham.isReachable(map, x, y, end, end);

					losElias.isReachable(map, x, y, 1, 1);
					losElias.isReachable(map, x, y, 1, end);
					losElias.isReachable(map, x, y, end, 1);
					losElias.isReachable(map, x, y, end, end);

					losRay.isReachable(map, x, y, 1, 1);
					losRay.isReachable(map, x, y, 1, end);
					losRay.isReachable(map, x, y, end, 1);
					losRay.isReachable(map, x, y, end, end);
				}
			}
		}

	}
}
