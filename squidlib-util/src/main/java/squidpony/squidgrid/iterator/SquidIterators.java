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
				 * leftmost column's highest cell.
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

	/**
	 * An iterator that returns cells in a square around a location. Cells are
	 * iterated from bottom left to top right in this square. A square size of
	 * {@code 0} creates an iterator that returns one location (the starting
	 * one); a square of size {@code 1} is an iterator that returns at most 9
	 * locations, (start.x-1,start.y+1), (start.x,start.y+1), ...; a square of
	 * size {@code 2} returns at most ((2*2)+1)*((2*2)+1) = 25 locations, etc..
	 * 
	 * <p>
	 * Instances of this iterator never return a coordinate outside the map.
	 * </p>
	 * 
	 * @author smelC
	 */
	public static class CenteredSquare implements SquidIterator {

		protected final int width;
		protected final int height;

		protected/* @Nullable */Coord previous;

		protected final int xstart;
		protected final int ystart;

		protected final int size;

		protected boolean done = false;

		/**
		 * An iterator to iterate in the square of size {@code size} around
		 * {@code (x, y)}.
		 * 
		 * @param width
		 *            The map's width
		 * @param height
		 *            The map's height
		 * @param x
		 *            The starting x coordinate.
		 * @param y
		 *            The starting y coordinate.
		 * @param size
		 *            The square's size. Can be {@code 0} but not negative.
		 * @throws IllegalStateException
		 *             If {@code width <= 0 || height <= 0 || size < 0}.
		 */
		public CenteredSquare(int width, int height, int x, int y, int size) {
			this.width = width;
			if (width <= 0)
				throw new IllegalStateException("Cannot build a centered square iterator over an empty grid");
			this.height = height;
			if (height <= 0)
				throw new IllegalStateException("Cannot build a centered square iterator over an empty grid");

			this.xstart = x;
			this.ystart = y;

			if (size < 0)
				throw new IllegalStateException("Cannot build a square iterator with a negative size");

			this.size = size;
		}

		/**
		 * An iterator to iterate in the square of size {@code size} around
		 * {@code start}.
		 * 
		 * @param width
		 *            The grid's width
		 * @param height
		 *            The grid's height
		 * @param start
		 *            The starting coordinate.
		 */
		public CenteredSquare(int width, int height, Coord start, int size) {
			this(width, height, start.x, start.y, size);
		}

		@Override
		public boolean hasNext() {
			return findNext(false) != null;
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

		protected/* @Nullable */Coord findNext(boolean mute) {
			while (!done) {
				final Coord result = findNext0();
				if (result != null) {
					if (isInGrid(result.x, result.y)) {
						if (mute)
							previous = result;
						return result;
					}
					/*
					 * We need to record progression, even if mutation isn't
					 * required. This is correct, because this is progression
					 * that isn't observable (skipping cells outside the map).
					 */
					previous = result;
				}
			}
			return null;
		}

		/*
		 * This method doesn't care about validity, findNext(boolean) handles it
		 */
		protected/* @Nullable */Coord findNext0() {
			if (previous == null) {
				/* Init */
				/* We're in SquidLib coordinates here ((0,0) is top left) */
				return Coord.get(xstart - size, ystart + size);
			}

			assert xstart - size <= previous.x && previous.x <= xstart + size;
			assert ystart - size <= previous.y && previous.y <= ystart + size;

			if (previous.x == xstart + size) {
				/* Need to go up and left (one column up, go left) */
				if (previous.y == ystart - size) {
					/* We're done */
					done = true;
					return null;
				} else
					return Coord.get(xstart - size, previous.y - 1);
			} else {
				/* Can go right in the same line */
				return Coord.get(previous.x + 1, previous.y);
			}
		}

		protected boolean isInGrid(int x, int y) {
			return 0 <= x && x < width && 0 <= y && y < height;
		}
	}

	/**
	 * An iterator that starts from a cell and iterates from the bottom left to
	 * the top right, in the rectangle defined by the given width and height.
	 * Widths and heights are like list-sizes w.r.t indexes. So a rectangle of
	 * width or height 0 is empty, a rectangle of width and height 1 has one
	 * cell, a rectangle of width and height 2 has four cells, etc.
	 * 
	 * <p>
	 * Put differently, the rectangle whose bottom left is (x, y) and has width
	 * and height 2, contains the cells (x, y), (x + 1, y), (x, y - 1), and (x +
	 * 1, y - 1); but it does NOT contain (x + 2, y), nor (x + 2, y - 1), nor (x
	 * + 2, y - 2).
	 * </p>
	 * 
	 * @author smelC
	 */
	public static class RectangleFromBottomLeftToTopRight implements SquidIterator {

		protected final int xstart;
		protected final int ystart;

		protected final int width;
		protected final int height;

		/** The last cell returned */
		protected Coord previous = null;

		public RectangleFromBottomLeftToTopRight(Coord start, int width, int height) {
			this.xstart = start.x;
			this.ystart = start.y;

			if (width < 0)
				throw new IllegalStateException(
						"Width of " + getClass().getSimpleName() + " shouldn't be negative");
			this.width = width;
			if (height < 0)
				throw new IllegalStateException(
						"Height of " + getClass().getSimpleName() + " shouldn't be negative");
			this.height = height;
		}

		@Override
		public boolean hasNext() {
			return next0() != null;
		}

		@Override
		public Coord next() {
			final Coord result = next0();
			if (result == null)
				throw new NoSuchElementException();
			previous = result;
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		protected /* @Nullable */ Coord next0() {
			if (previous == null) {
				/* Initialization */
				return width == 0 || height == 0 ? null : Coord.get(xstart, ystart);
			} else {
				/* We're in SquidLib coordinates: (0,0) is top left */
				assert xstart <= previous.x && previous.x < xstart + width;
				assert previous.y <= ystart && ystart - height < previous.y;

				if (previous.x == xstart + width - 1) {
					/* Need to go up and left (one column up, go left) */
					if (previous.y == ystart - (height - 1)) {
						/* We're done */
						return null;
					} else
						/* One line above */
						return Coord.get(xstart, previous.y - 1);
				} else {
					/* Can go right in the same line */
					return Coord.get(previous.x + 1, previous.y);
				}
			}
		}
	}

	/**
	 * An iterator that iterates around a starting position (counter clockwise).
	 * It can return at most 9 elements. Instances of this iterator only return
	 * coordinates that are valid w.r.t. to the widths and heights given at
	 * creation time (i.e. they do not go off the map).
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
				throw new IllegalArgumentException(
						"x-coordinate: " + xstart + " in grid with width " + width);
			if (ystart < 0 || height <= ystart)
				throw new IllegalArgumentException(
						"y-coordinate: " + ystart + " in grid with height " + height);

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
				throw new IllegalStateException(
						"Illegal x-coordinate: " + startx + " (map's width: " + width + ")");
			this.startx = startx;
			if (starty < 0 || height <= starty)
				throw new IllegalStateException(
						"Illegal y-coordinate: " + starty + " (map's width: " + height + ")");
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

	/**
	 * An iterator to iterate from a starting position (inclusive) and going in
	 * one Direction. This iterator stops when reaching the map's bound.
	 * 
	 * @author smelC
	 */
	public static class Linear implements SquidIterator {

		/** The current X-coordinate */
		protected int x;

		/** The Y-coordinate */
		protected int y;

		/** The grid's width */
		protected final int width;
		/** The grid's height */
		protected final int height;
		protected Direction direction;
		protected Linear()
		{
			width = 0;
			height = 0;
		}
		public Linear(Direction direction, int startx, int y, int width, int height) {
			this.direction = direction;
			this.x = startx;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		@Override
		public boolean hasNext() {
			return next(true) != null;
		}

		@Override
		public Coord next() {
			return next(false);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private /* @Nullable */Coord next(boolean peek) {
			if (height <= y || 0 > y || width <= x || 0 > x)
				return null;
			final Coord result = Coord.get(x, y);
			if (!peek) {
				x+= direction.deltaX;
				y+= direction.deltaY;
			}
			return result;
		}
	}
	public static class Right extends Linear
	{
		public Right(int startx, int y, int width, int height)
		{
			super(Direction.RIGHT, startx, y, width, height);
		}
	}
	public static class Left extends Linear
	{
		public Left(int startx, int y, int width, int height)
		{
			super(Direction.LEFT, startx, y, width, height);
		}
	}
	public static class Up extends Linear
	{
		public Up(int startx, int y, int width, int height)
		{
			super(Direction.UP, startx, y, width, height);
		}
	}
	public static class Down extends Linear
	{
		public Down(int startx, int y, int width, int height)
		{
			super(Direction.DOWN, startx, y, width, height);
		}
	}
}
