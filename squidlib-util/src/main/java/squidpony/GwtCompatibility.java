package squidpony;

import squidpony.squidmath.Coord;

/**
 * Methods useful to be GWT compatible.
 * 
 * @author smelC
 */
public class GwtCompatibility {

	/**
	 * @param input
	 * @return A clone of {@code input}.
	 */
	public static Coord[] cloneCoords(Coord[] input) {
		final Coord[] result = new Coord[input.length];
		for (int i = 0; i < input.length; i++)
			result[i] = input[i];
		return result;
	}

	/**
	 * @param op
	 * @param d
	 * @return The remainder of {@code op / d}.
	 */
	/* smelC: because Math.IEEEremainder isn't GWT compatible */
	public static double IEEEremainder(double op, double d) {
		final double div = op / d;
		return op - (div * d);
	}

}
