package squidpony.squidgrid;

/**
 * A way of measuring what cells are adjacent and how much further any adjacent cells are from other adjacent cells.
 * In practice, this is used for pathfinding first and foremost, with some other code using this to fill nearby cells in
 * some way. You will usually want to use either {@link #MANHATTAN} through an entire codebase when only moves in
 * cardinal directions are allowed, {@link #EUCLIDEAN} when you want some things to look circular instead of always
 * diamond-shaped as with MANHATTAN (this allows diagonal movement for pathfinders only if it is the best option), or
 * maybe {@link #CHEBYSHEV} if you consider using EUCLIDEAN for pathfinding (CHEBYSHEV allows cardinal and diagonal
 * movement with equal cost, but this permits pathfinders to make very strange choices).
 */
public enum Measurement {

	/**
	 * The distance it takes when only the four primary directions can be
	 * moved in. The default.
	 */
	MANHATTAN,
	/**
	 * The distance it takes when diagonal movement costs the same as
	 * cardinal movement.
	 */
	CHEBYSHEV,
	/**
	 * The distance it takes as the crow flies. This will NOT affect movement cost when calculating a path,
	 * only the preferred squares to travel to (resulting in drastically more reasonable-looking paths).
	 */
	EUCLIDEAN;

	public double heuristic(Direction target) {
		if (this == Measurement.EUCLIDEAN) {
			if (target == Direction.DOWN_LEFT || target == Direction.DOWN_RIGHT || target == Direction.UP_LEFT || target == Direction.UP_RIGHT) {
				return 1.4142135623730951; //Math.sqrt(2.0);
			}
		}
		return 1.0;
	}

	public int directionCount() {
		return this == Measurement.MANHATTAN ? 4 : 8;
	}
	/**
	 * Gets the appropriate Measurement that matches a Radius enum.
	 * Matches SQUARE or CUBE to CHEBYSHEV, DIAMOND or OCTAHEDRON to MANHATTAN, and CIRCLE or SPHERE to EUCLIDEAN.
	 *
	 * @param radius the Radius to find the corresponding Measurement for
	 * @return a Measurement that matches radius; SQUARE to CHEBYSHEV, DIAMOND to MANHATTAN, etc.
	 */
	public static Measurement matchingMeasurement(Radius radius) {
		switch (radius)
		{
			case CUBE:
			case SQUARE:
				return Measurement.CHEBYSHEV;
			case DIAMOND:
			case OCTAHEDRON:
				return Measurement.MANHATTAN;
			default:
				return Measurement.EUCLIDEAN;
		}
	}

	/**
	 * Gets the appropriate Radius corresponding to a Measurement.
	 * Matches CHEBYSHEV to SQUARE, MANHATTAN to DIAMOND, and EUCLIDEAN to CIRCLE.
	 * @return a Radius enum that matches this Measurement; CHEBYSHEV to SQUARE, MANHATTAN to DIAMOND, etc.
	 */
	public Radius matchingRadius() {
		switch (this) {
			case CHEBYSHEV:
				return Radius.SQUARE;
			case EUCLIDEAN:
				return Radius.CIRCLE;
			default:
				return Radius.DIAMOND;
		}
	}
}
