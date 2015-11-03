package squidpony.squidgrid.iterator;

import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

import java.util.NoSuchElementException;

/**
 * Instances of {@link SquidIterator}.
 * 
 * @author smelC
 */
public class SquidIterators {

	/**
	 * Iterator that starts from the bottom left element of the grid, to the top
	 * right.
	 * 
	 * @author smelC
	 */
	public static class BottomLeftToTopRight implements SquidIterator {

		protected final int width;
		protected final int height;

		/**
		 * The point whose character was returned by the previous call to
		 * {@link #next()}, or {@code null} if none.
		 */
		protected/* @Nullable */Coord previous;

		/**
		 * A fresh iterator.
		 * 
		 * @param width
		 *            The grid's width.
		 * @param height
		 *            The grid's height.
		 */
		public BottomLeftToTopRight(int width, int height) {
			this.width = width;
			this.height = height;
		}

		@Override
		public boolean hasNext() {
			if (previous == null)
				return !gridIsEmpty();
			else {
				/*
				 * Previous element is not on leftmost column or not on the
				 * leftmost colum's highest cell.
				 */
				return previous.x < width - 1 || 0 < previous.y;
			}
		}

		@Override
		public Coord next() {
			if (previous == null) {
				if (gridIsEmpty())
					throw new NoSuchElementException("Iterator on an empty grid has no next element");
				else {
					previous = Coord.get(0, height - 1);
					return previous;
				}
			} else {
				if (previous.x == width - 1) {
					/* On the leftmost column */
					if (previous.y == 0)
						throw new NoSuchElementException(
								"Bottom left to top right iterator has no more element");
					else {
						previous = Coord.get(0, previous.y - 1);
						return previous;
					}

				} else {
					previous = Coord.get(previous.x + 1, previous.y);
					return previous;
				}
			}
		}

		/**
		 * @return Whether {@link #above()} would return an element (i.e. not
		 *         throw an exception).
		 */
		public boolean hasAbove() {
			return previous != null && 0 < previous.y;
		}

		/**
		 * @return The point above the last point returned by {@link #next()}.
		 * @throws IllegalStateException
		 *             If {@link #next()} wasn't called before.
		 * @throws NoSuchElementException
		 *             If there's no point above the last point returned by
		 *             {@link #next()}.
		 */
		public Coord above() {
			if (previous == null)
				throw new IllegalStateException("next() should be called before above()");
			else {
				if (previous.y == 0)
					throw new NoSuchElementException("There's no element above the first row");
				else
					return Coord.get(previous.x, previous.y - 1);
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		protected boolean gridIsEmpty() {
			return width == 0 || height == 0;
		}

	}

	/* **************************************************************************************************** */

	/**
	 * An iterator that circles around a location, in a square-shape,
	 * counter-clockwise, starting at the east. This iterator can return
	 * locations that are outside the map.
	 * 
	 * @author smelC
	 */
	public static class Square implements SquidIterator {

		protected final int width;
		protected final int height;

		protected/* @Nullable */Coord previous;

		protected final int xstart;
		protected final int ystart;

		/** Invariant: less or equal to {@link #maxDistanceFromStart} */
		protected int distanceFromStart;

		protected int streakBound; // inclusive
		protected int indexInStreak;
		protected Direction direction;

		/** The maximum distance from the starting point */
		protected final int maxDistanceFromStart;

		/**
		 * A circling iterator around (x, y).
		 * 
		 * @param width
		 *            The grid's width
		 * @param height
		 *            The grid's height
		 * @param x
		 *            The starting x coordinate.
		 * @param y
		 *            The starting y coordinate.
		 */
		public Square(int width, int height, int x, int y) {
			this.width = width;
			if (width == 0)
				throw new IllegalStateException("Cannot build a square iterator over an empty grid");
			this.height = height;
			if (height == 0)
				throw new IllegalStateException("Cannot build a square iterator over an empty grid");

			xstart = x;
			ystart = y;

			maxDistanceFromStart = Math.max(Math.abs(x - width), Math.abs(x - height));
		}

		/**
		 * A circling iterator around (x, y).
		 * 
		 * @param width
		 *            The grid's width
		 * @param height
		 *            The grid's height
		 * @param start
		 *            The starting coordinate.
		 */
		public Square(int width, int height, Coord start) {
			this(width, height, start.x, start.y);
		}

		@Override
		public boolean hasNext() {
			return findNext(true) != null;
		}

		protected/* @Nullable */Coord findNext(boolean mute) {
			if (previous == null) {
				if (maxDistanceFromStart == 0)
					/* This is an empty iterator */
					return null;

				/* Iterator is pristine */
				final Coord result = Coord.get(xstart + 1, ystart);

				if (mute)
					previous = result;

				return result;
			}

			final int dfs = distanceFromStart;
			final int iis = indexInStreak;
			final Direction pdirection = direction;

			final Coord result;
			if (direction == Direction.RIGHT && indexInStreak == streakBound) {
				/* Enlarge the square */
				if (distanceFromStart == maxDistanceFromStart)
					result = null;
				else {
					distanceFromStart++;
					result = Coord.get(xstart + distanceFromStart, ystart);
				}
			} else {
				if (indexInStreak == streakBound) {
					/* Need to change the direction */
					indexInStreak = 0;
					if (direction == null)
						direction = Direction.RIGHT;
					else
						direction = direction.counterClockwise().counterClockwise();
				} else {
					/* Continue in streak */
					indexInStreak++;
				}

				result = Coord.get(previous.x + direction.deltaX, previous.y + direction.deltaY);
			}

			if (mute)
				previous = result;
			else {
				distanceFromStart = dfs;
				indexInStreak = iis;
				direction = pdirection;
			}


			return result;
		}

		@Override
		public Coord next() {
			final Coord next = findNext(true);
			if (next == null)
				throw new NoSuchElementException();
			return next;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	/* **************************************************************************************************** */

	/**
	 * An iterator that iterates around a starting position (counter clockwise).
	 * It can return at most 9 elements.
	 * 
	 * @author smelC
	 */
	public static class AroundCounterClockWise implements SquidIterator {

		protected final int width;
		protected final int height;

		protected final int xstart;
		protected final int ystart;
		protected Direction prev;

		/**
		 * A fresh iterator, to iterate counter clock wise around {@code start}
		 * starting on {@code start}'s right.
		 * 
		 * @param width
		 *            The grid's width.
		 * @param height
		 *            The grid's height.
		 * @param start
		 */
		public AroundCounterClockWise(int width, int height, Coord start) {
			this(width, height, start.x, start.y);
		}

		/**
		 * A fresh iterator, to iterate counter clock wise around
		 * {@code (xstart, ystart)} starting on {@code start}'s right.
		 * 
		 * @param width
		 *            The grid's width.
		 * @param height
		 *            The grid's height.
		 * @param xstart
		 *            The starting x-coordinate.
		 * @param ystart
		 *            The starting y-coordinate.
		 */
		public AroundCounterClockWise(int width, int height, int xstart, int ystart) {
			this.width = width;
			this.height = height;

			if (xstart < 0 || width <= xstart)
				throw new IllegalArgumentException(String.format("x-coordinate: %d in grid with width %d",
						xstart, width));
			if (ystart < 0 || height <= ystart)
				throw new IllegalArgumentException(String.format("y-coordinate: %d in grid with height %d",
						xstart, width));

			this.xstart = xstart;
			this.ystart = ystart;
		}

		@Override
		public boolean hasNext() {
			return findNext(false) != null;
		}

		@Override
		public Coord next() {
			final Coord result = findNext(true);
			if (result == null)
				throw new NoSuchElementException();
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		/*
		 * Method does not allocate anything if it returns null or if it hits
		 * the Coords cache, which is nice.
		 */
		protected/* @Nullable */Coord findNext(boolean mute) {
			Direction d = prev;
			while (d != Direction.DOWN_RIGHT) {
				if (d == null)
					/* Initialization */
					d = Direction.DOWN_RIGHT;
				d = d.counterClockwise();
				final int x = xstart + d.deltaX;
				final int y = ystart + d.deltaY;
				if (isInGrid(x, y)) {
					if (mute)
						prev = d;
					return Coord.get(x, y);
				}
			}
			return null;
		}

		protected boolean isInGrid(int x, int y) {
			return 0 <= x && x < width && 0 <= y && y < height;
		}
	}

	/**
	 * An iterator to iterate from a starting position (exclusive) and going up.
	 * This iterator cycles when reaching the map's bound, but it iterates at
	 * most once on a cell, i.e. it does at most one roll over a column of the
	 * map.
	 * 
	 * @author smelC
	 */
	public static class VerticalUp implements SquidIterator {

		/** The starting X-coordinate */
		protected final int startx;

		/** The starting Y-coordinate */
		protected final int starty;

		/* Initially null */
		protected Coord prev;

		/** The grid's width */
		protected final int width;
		/** The grid's height */
		protected final int height;

		/**
		 * An iterator to iterate vertically, starting AFTER
		 * {@code (startx, starty)}. This iterates cycles when it reaches the
		 * map's bound, but it iterates at most once on a cell, i.e. it does at
		 * most one roll over a column of the map.
		 * 
		 * @param startx
		 *            The starting X-coordinate.
		 * @param starty
		 *            The starting vertical-coordinate.
		 * @param width
		 *            The map's width.
		 * @param height
		 *            The map's height.
		 */
		public VerticalUp(int startx, int starty, int width, int height) {
			if (startx < 0 || width <= startx)
				throw new IllegalStateException(String.format("Illegal x-coordinate: %d (map's width: %d)",
						startx, width));
			this.startx = startx;
			if (starty < 0 || height <= starty)
				throw new IllegalStateException(String.format("Illegal y-coordinate: %d (map's height: %d)",
						starty, height));
			this.starty = starty;

			this.width = width;
			this.height = height;
		}

		/**
		 * An iterator to iterate vertically, starting AFTER {@code start}. This
		 * iterates cycles when it reaches the map's bound, but it iterates at
		 * most once on a cell, i.e. it does at most one roll over a column of
		 * the map.
		 * 
		 * @param start
		 *            The starting coordinate.
		 * @param width
		 *            The map's width.
		 * @param height
		 *            The map's height.
		 */
		public VerticalUp(Coord start, int width, int height) {
			this(start.x, start.y, width, height);
		}

		@Override
		public boolean hasNext() {
			final Coord n = findNext();
			if (prev == null)
				return n != null;
			else {
				/* Not done && has next */
				return (prev.x != startx || prev.y != starty) && n != null;
			}
		}

		@Override
		public Coord next() {
			prev = findNext();
			if (prev == null)
				throw new NoSuchElementException();
			return prev;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		protected Coord findNext() {
			if (prev == null) {
				/* First iteration */
				if (starty == 0)
					/* Start from the bottom */
					return Coord.get(startx, height - 1);
				else
					/* Start from the cell above (startx, starty) */
					return Coord.get(startx, starty - 1);
			} else {
				if (prev.x == startx && prev.y == starty)
					/* Done iterating */
					return null;
				else if (prev.y == 0) {
					/* Continue from the bottom */
					return Coord.get(startx, height - 1);
				} else {
					/* Go up */
					assert 0 < prev.y && prev.y < height;
					final Coord r = Coord.get(startx, prev.y - 1);
					if (r.y == starty)
						/* We would return the starting position */
						return null;
					else
						return r;
				}
			}
		}

	}
}
