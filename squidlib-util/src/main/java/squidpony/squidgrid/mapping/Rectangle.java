package squidpony.squidgrid.mapping;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.iterator.SquidIterators;
import squidpony.squidgrid.zone.Zone;
import squidpony.squidmath.Coord;

import java.util.*;

/**
 * Rectangles in 2D grids. Checkout {@link Utils} for utility methods.
 *
 * @author smelC
 * @see RectangleRoomFinder How to find rectangles in a dungeon
 */
public interface Rectangle extends Zone {

	/**
	 * @return The bottom left coordinate of the room.
	 */
	Coord getBottomLeft();
	/**
	 * @return The room's width (from {@link #getBottomLeft()). It is greater or
	 *         equal than 0.
	 */
	@Override
	int getWidth();

	/**
	 * @return The room's height (from {@link #getBottomLeft()). It is greater
	 *         or equal than 0.
	 */
	@Override
	int getHeight();

	/**
	 * Utilities pertaining to {@link Rectangle}
	 *
	 * @author smelC
	 */
	class Utils {

		/**
		 * A comparator that uses {@link #size(Rectangle)} as the measure.
		 */
		public static final Comparator<Rectangle> SIZE_COMPARATOR = new Comparator<Rectangle>() {
			@Override
			public int compare(Rectangle o1, Rectangle o2) {
				return Integer.compare(size(o1), size(o2));
			}
		};

		/**
         * @param r a Rectangle
         * @param c a Coord to check against r for presence
		 * @return Whether {@code r} contains {@code c}.
		 */
		public static boolean contains(Rectangle r, Coord c) {
			return c != null && contains(r, c.x, c.y);
		}

		/**
         * @param r a Rectangle
         * @param x x-coordinate of a point to check against r
         * @param y y-coordinate of a point to check against r
		 * @return Whether {@code r} contains {@code c}.
		 */
		public static boolean contains(Rectangle r, int x, int y) {
			if (r == null)
				return false;
			final Coord bottomLeft = r.getBottomLeft();
			final int width = r.getWidth();
			final int height = r.getHeight();
			return !(x < bottomLeft.x /* Too much to the left */
					|| bottomLeft.x + width < x /* Too much to the right */
					|| bottomLeft.y < y /* Too low */
					|| y < bottomLeft.y - height); /* Too high */
		}

		/**
         * @param r  a Rectangle
         * @param cs a Collection of Coord to check against r; returns true if r contains any items in cs
		 * @return {@code true} if {@code r} contains a member of {@code cs}.
		 */
		public static boolean containsAny(Rectangle r, Collection<Coord> cs) {
			for (Coord c : cs) {
				if (contains(r, c))
					return true;
			}
			return false;
		}

		/**
         * @param rs an Iterable of Rectangle items to check against c
         * @param c  a Coord to try to find in any of the Rectangles in rs
		 * @return {@code true} if a member of {@code rs}
		 *         {@link #contains(Rectangle, Coord) contains} {@code c}.
		 */
		public static boolean contains(Iterable<? extends Rectangle> rs, Coord c) {
			for (Rectangle r : rs) {
				if (contains(r, c))
					return true;
			}
			return false;
		}

		/**
         * @param r a Rectangle
		 * @return The number of cells that {@code r} covers.
		 */
		public static int size(Rectangle r) {
			return r.getWidth() * r.getHeight();
		}

		/**
         * @param r a Rectangle
		 * @return The center of {@code r}.
		 */
		public static Coord center(Rectangle r) {
			final Coord bl = r.getBottomLeft();
			/*
			 * bl.y - ... : because we're in SquidLib coordinates (0,0) is top
			 * left
			 */
			return Coord.get(bl.x + Math.round(r.getWidth() / 2), bl.y - Math.round(r.getHeight() / 2));
		}

		/**
		 * Use {@link #cellsList(Rectangle)} if you want them all.
		 *
         * @param r a Rectangle
		 * @return The cells that {@code r} contains, from bottom left to top
		 *         right; lazily computed.
		 */
		public static Iterator<Coord> cells(Rectangle r) {
			return new SquidIterators.RectangleFromBottomLeftToTopRight(r.getBottomLeft(), r.getWidth(),
					r.getHeight());
		}

		/**
		 * Use {@link #cells(Rectangle)} if you may stop before the end of the
		 * list, you'll save some memory.
		 *
		 * @param r
		 * @return The cells that {@code r} contains, from bottom left to top
		 *         right.
		 */
		public static List<Coord> cellsList(Rectangle r) {
			/* Allocate it with the right size, to avoid internal resizings */
			final List<Coord> result = new ArrayList<Coord>(size(r));
			final Iterator<Coord> it = cells(r);
			while (it.hasNext())
				result.add(it.next());
			assert result.size() == size(r);
			return result;
		}

		/**
         * @param d A direction.
		 * @return {@code r} extended to {@code d} by one row and/or column.
		 */
		public static Rectangle extend(Rectangle r, Direction d) {
			final Coord bl = r.getBottomLeft();
			final int width = r.getWidth();
			final int height = r.getHeight();

			switch (d) {
			case DOWN_LEFT:
				return new Rectangle.Impl(bl.translate(Direction.DOWN_LEFT), width + 1, height + 1);
			case DOWN_RIGHT:
				return new Rectangle.Impl(bl.translate(Direction.DOWN), width + 1, height + 1);
			case NONE:
				return r;
			case UP_LEFT:
				return new Rectangle.Impl(bl.translate(Direction.LEFT), width + 1, height + 1);
			case UP_RIGHT:
				return new Rectangle.Impl(bl, width + 1, height + 1);
			case DOWN:
				return new Rectangle.Impl(bl.translate(Direction.DOWN), width, height + 1);
			case LEFT:
				return new Rectangle.Impl(bl.translate(Direction.LEFT), width + 1, height);
			case RIGHT:
				return new Rectangle.Impl(bl, width + 1, height);
			case UP:
				return new Rectangle.Impl(bl, width, height + 1);
			}
			throw new IllegalStateException("Unmatched direction in Rectangle.Utils::extend: " + d);
		}

		/**
		 * @param r
		 * @param dir
		 * @return The coord at the corner identified by {@code dir} in
		 *         {@code r}.
		 */
		public static Coord getCorner(Rectangle r, Direction dir) {
			switch (dir) {
			case DOWN_LEFT:
				return r.getBottomLeft();
			case DOWN_RIGHT:
				return r.getBottomLeft().translate(r.getWidth() - 1, 0);
			case UP_LEFT:
				/* -y because in SquidLib higher y is smaller */
				return r.getBottomLeft().translate(0, -(r.getHeight() - 1));
			case UP_RIGHT:
				/* -y because in SquidLib higher y is smaller */
				return r.getBottomLeft().translate(r.getWidth() - 1, -(r.getHeight() - 1));
			case NONE:
				return r.getCenter();
			case DOWN:
			case LEFT:
			case RIGHT:
			case UP:
				final Coord c1 = getCorner(r, dir.clockwise());
				final Coord c2 = getCorner(r, dir.counterClockwise());
				return Coord.get((c1.x + c2.x) / 2, (c1.y + c2.y) / 2);
			}
			throw new IllegalStateException("Unmatched direction in Rectangle.Utils::getCorner: " + dir);
		}

		/**
		 * @param r
		 * @param buf
		 *            An array of (at least) size 4, to hold the 4 corners. It
		 *            is returned, except if {@code null} or too small, in which
		 *            case a fresh array is returned.
		 * @return {@code buf}, if it had length of at least 4, or a new 4-element array; it contains this Rectangle's 4 corners
		 */
		public static Coord[] getAll4Corners(Rectangle r, Coord[] buf) {
			final Coord[] result = buf == null || buf.length < 4 ? new Coord[4] : buf;
			result[0] = getCorner(r, Direction.DOWN_LEFT);
			result[1] = getCorner(r, Direction.DOWN_RIGHT);
			result[2] = getCorner(r, Direction.UP_RIGHT);
			result[3] = getCorner(r, Direction.UP_LEFT);
			return result;
		}

		/**
         * Creates a new Rectangle that is smaller than r by 1 cell from each of r's edges, to a minimum of a 1x1 cell.
         * @param r a Rectangle to shrink
		 * @return the shrunken Rectangle, newly-allocated
		 */
        public static Rectangle shrink(Rectangle r)
        {
            return new Rectangle.Impl(r.getBottomLeft().translate(1, 1),
                    Math.max(1, r.getWidth() - 2), Math.max(1, r.getHeight() - 2));
		}

		/**
		 * @param r
		 * @param cardinal
		 * @param buf
		 *            The buffer to fill or {@code null} to let this method
		 *            allocate.
		 * @return The border of {@code r} at the position {@code cardinal},
		 *         i.e. the lowest line if {@code r} is {@link Direction#DOWN},
		 *         the highest line if {@code r} is {@link Direction#UP}, the
		 *         leftest column if {@code r} is {@link Direction#LEFT}, and
		 *         the rightest column if {@code r} is {@link Direction#RIGHT}.
		 */
		public static List<Coord> getBorder(Rectangle r, Direction cardinal,
				/* @Nullable */ List<Coord> buf) {
			Coord start = null;
			Direction dir = null;
			int len = -1;
			switch (cardinal) {
			case DOWN:
			case UP:
				len = r.getWidth();
				dir = Direction.RIGHT;
				start = cardinal == Direction.DOWN ? r.getBottomLeft() : getCorner(r, Direction.UP_LEFT);
				break;
			case LEFT:
			case RIGHT:
				len = r.getHeight();
				dir = Direction.UP;
				start = cardinal == Direction.LEFT ? r.getBottomLeft() : getCorner(r, Direction.DOWN_RIGHT);
				break;
			case DOWN_LEFT:
			case DOWN_RIGHT:
			case NONE:
			case UP_LEFT:
			case UP_RIGHT:
				throw new IllegalStateException(
						"Expected a cardinal direction in Rectangle.Utils::getBorder. Received: " + cardinal);
			}
			if (start == null)
				throw new IllegalStateException(
						"Unmatched direction in Rectangle.Utils::Border: " + cardinal);

			final List<Coord> result = buf == null ? new ArrayList<Coord>(len) : buf;
			Coord now = start;
			for (int i = 0; i < len; i++) {
				result.add(now);
				now = now.translate(dir);
			}
			return result;
		}
	}

	/**
	 * @author smelC
	 */
	class Impl extends Zone.Skeleton implements Rectangle {

		protected final Coord bottomLeft;
		protected final int width;
		protected final int height;

		private static final long serialVersionUID = -6197401003733967116L;

		public Impl(Coord bottomLeft, int width, int height) {
			this.bottomLeft = bottomLeft;
			this.width = width;
			this.height = height;
		}

		@Override
		public Coord getBottomLeft() {
			return bottomLeft;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bottomLeft == null) ? 0 : bottomLeft.hashCode());
			result = prime * result + height;
			result = prime * result + width;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Impl other = (Impl) obj;
			if (bottomLeft == null) {
				if (other.bottomLeft != null)
					return false;
			} else if (!bottomLeft.equals(other.bottomLeft))
				return false;
			if (height != other.height)
				return false;
			if (width != other.width)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Rectangle at " + bottomLeft + ", width:" + width + ", height:" + height;
		}

		// Implementation of Zone:

		@Override
		public boolean isEmpty() {
			return width == 0 || height == 0;
		}

		@Override
		public int size() {
			return width * height;
		}

		@Override
		public boolean contains(int x, int y) {
			return x >= bottomLeft.x && x < bottomLeft.x + width && y <= bottomLeft.y
					&& bottomLeft.y - height < y;
		}

		@Override
		public boolean contains(Coord c) {
			return contains(c.x, c.y);
		}

		@Override
		public int x(boolean smallestOrBiggest) {
			return bottomLeft.x + (smallestOrBiggest ? 0 : getWidth() - 1);
		}

		@Override
		public int y(boolean smallestOrBiggest) {
			return bottomLeft.y - (smallestOrBiggest ? (getHeight() - 1) : 0);
		}

		@Override
		public Coord getCenter() {
			return Utils.center(this);
		}

		@Override
		public List<Coord> getAll() {
			final List<Coord> result = Utils.cellsList(this);
			assert result.size() == size();
			return result;
		}

		@Override
		public Zone translate(int x, int y) {
			return new Impl(bottomLeft.translate(x, y), width, height);
		}

		@Override
		public List<Coord> getInternalBorder() {
			if (width <= 1 || height <= 1)
				return getAll();
			final int expectedSize = width + (height - 1) + (width - 1) + (height - 2);
			final List<Coord> result = new ArrayList<Coord>(expectedSize);
			Coord current = Utils.getCorner(this, Direction.DOWN_LEFT);
			for (int i = 0; i < width; i++) {
				assert !result.contains(current);
				result.add(current);
				current = current.translate(Direction.RIGHT);
			}
			current = Utils.getCorner(this, Direction.UP_LEFT);
			for (int i = 0; i < width; i++) {
				assert !result.contains(current);
				result.add(current);
				current = current.translate(Direction.RIGHT);
			}
			current = Utils.getCorner(this, Direction.DOWN_LEFT);
			/* Stopping at height - 1 to avoid doublons */
			for (int i = 0; i < height - 1; i++) {
				if (0 < i) {
					/*
					 * To avoid doublons (with the very first value of 'current'
					 * atop this method.
					 */
					assert !result.contains(current);
					result.add(current);
				}
				current = current.translate(Direction.UP);
			}
			current = Utils.getCorner(this, Direction.DOWN_RIGHT);
			/* Stopping at height - 1 to avoid doublons */
			for (int i = 0; i < height - 1; i++) {
				if (0 < i) {
					/*
					 * To avoid doublons (with the very first value of 'current'
					 * atop this method.
					 */
					assert !result.contains(current);
					result.add(current);
				}
				current = current.translate(Direction.UP);
			}
			assert result.size() == expectedSize;
			return result;
		}

		@Override
		public Collection<Coord> getExternalBorder() {
			final Rectangle extension = extend();
			final List<Coord> result = new ArrayList<Coord>(
					(extension.getWidth() + extension.getHeight()) * 2);
			for (Direction dir : Direction.CARDINALS)
				Utils.getBorder(extension, dir, result);
			return result;
		}

		@Override
		public Rectangle extend() {
			return new Rectangle.Impl(bottomLeft.translate(Direction.DOWN_LEFT), width + 2, height + 2);
		}

		@Override
		public Iterator<Coord> iterator() {
			/* Do not rely on getAll(), to avoid allocating the list */
			return Rectangle.Utils.cells(this);
		}
	}

}
