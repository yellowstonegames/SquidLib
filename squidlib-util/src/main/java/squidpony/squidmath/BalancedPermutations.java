package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * Based on <a href="http://extremelearning.com.au/isotropic-blue-noise-point-sets/">Martin Roberts' blog post about
 * blue noise point sets</a>, this class generates "balanced" permutations of a specific size with good performance.
 * It may be added to later to include the rest of that blog post.
 */
@Beta
public class BalancedPermutations {
	public final int size;
	private final int halfSize;
	private final int[] delta, targets;
	/**
	 * Can be any long.
	 */
	private long stateA;
	/**
	 * Must be odd.
	 */
	private long stateB;

	public BalancedPermutations(){
		this(16,
				(long) ((Math.random() - 0.5) * 0x10000000000000L)
						^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
				(long) ((Math.random() - 0.5) * 0x10000000000000L)
						^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
	}
	public BalancedPermutations(int size, long stateA, long stateB)
	{
		this.size = size;
		this.halfSize = size >>> 1;
		delta = new int[size];
		targets = new int[size];
		this.stateA = stateA;
		this.stateB = stateB | 1;
	}

	/**
	 * It's a weird RNG. Returns a slightly-biased pseudo-random int between 0 inclusive and bound exclusive. The bias comes from
	 * not completely implementing Daniel Lemire's fastrange algorithm, but it should only be relevant for huge bounds. The number
	 * generator itself passes PractRand without anomalies, has a state size of 127 bits, and a period of 2 to the 127.
	 * This generator will probably be added to SquidLib as "GearRNG."
	 * @param bound upper exclusive bound
	 * @return an int between 0 (inclusive) and bound (exclusive)
	 */
	private int nextIntBounded (int bound) {
		final long s = (stateA += 0xC6BC279692B5C323L);
		final long z = ((s < 0x800000006F17146DL) ? stateB : (stateB += 0x9479D2858AF899E6L)) * (s ^ s >>> 31);
		return (int)(bound * ((z ^ z >>> 25) & 0xFFFFFFFFL) >>> 32);
	}

	private static void swap(int[] arr, int pos1, int pos2) {
		final int tmp = arr[pos1];
		arr[pos1] = arr[pos2];
		arr[pos2] = tmp;
	}

	/**
	 * Fisher-Yates and/or Knuth shuffle, done in-place on an int array.
	 * @param elements will be modified in-place by a relatively fair shuffle
	 */
	private void shuffleInPlace(int[] elements) {
		final int size = elements.length;
		for (int i = size; i > 1; i--) {
			swap(elements, i - 1, nextIntBounded(i));
		}
	}

	/**
	 * Fills {@code items} with a balanced permutation from 0 to {@code size - 1}. The length of {@code items} must be
	 * at least {@code size}. This may take a while if size is large; half a second is reasonable for when size is 48,
	 * with smaller sizes taking much less time and larger ones taking much more.
	 * @param items an int array which will be modified in-place
	 */
	public void fill(final int[] items) {
		if(items == null || items.length < size) return;
		BIG_LOOP:
		while (true) {
			for (int i = 0; i < halfSize; i++) {
				delta[i] = i + 1;
				delta[i + halfSize] = ~i;
			}
			shuffleInPlace(delta);
			for (int i = 0; i < size; i++) {
				targets[i] = i;
			}
			targets[items[0] = nextIntBounded(size)] = -1;
			for (int i = 1; i < size; i++) {
				int d = 0;
				for (int j = 0; j < size; j++) {
					d = delta[j];
					if (d == 0) continue;
					int t = items[i - 1] + d;
					if (t >= 0 && t < size && targets[t] != -1) {
						items[i] = t;
						targets[t] = -1;
						delta[j] = 0;
						break;
					} else d = 0;
				}
				if (d == 0) continue BIG_LOOP;
			}
			int d = items[0] - items[size - 1];
			for (int j = 0; j < size; j++) {
				if (d == delta[j]) {
					return; // found a valid balanced permutation
				}
			}
		}
	}
	
	public GreasedRegion rotatedGrid(){
		int size2 = size * size;
		GreasedRegion region = new GreasedRegion(size2, size2);
		int px, py = 0;
		for (int x = 0; x < size; x++) {
			px = size - 1 + x * size;
			for (int y = 0; y < size; y++) {
				region.insert(px--, py + y * size);
			}
			py++;
		}
		return region;
	}

	public GreasedRegion shuffledGrid(){
		int[] xPerm = new int[size], yPerm = new int[size];
		fill(xPerm);
		fill(yPerm);
		int size2 = size * size;
		GreasedRegion region = new GreasedRegion(size2, size2);
		int px, py;
		for (int x = 0; x < size; x++) {
			px = size - 1;
			py = yPerm[x];
			for (int y = 0; y < size; y++) {
				region.insert(px - xPerm[y] + xPerm[x] * size, py + yPerm[y] * size);
			}
		}
		return region;
	}

	public GreasedRegion shuffledGridMultiple(int repeats){
		int[] xPerm = new int[size], yPerm = new int[size], rxPerm = new int[size], ryPerm = new int[size];
		repeats = (repeats + size - 1) % size;
		fill(xPerm);
		fill(yPerm);
		fill(rxPerm);
		fill(ryPerm);
		int size2 = size * size;
		GreasedRegion region = new GreasedRegion(size2, size2);
		int px, py, rx, ry;
		for (int r = 0; r <= repeats; r++) {
			rx = rxPerm[r];
			ry = ryPerm[r];
			for (int x = 0; x < size; x++) {
				px = size - 1;
				py = yPerm[x];
				for (int y = 0; y < size; y++) {
					region.insert(px - xPerm[y] + xPerm[(x + rx) % size] * size, py + yPerm[(y + ry) % size] * size);
				}
			}
		}
		return region;
	}
}
