package squidpony.squidgrid.zone;

import java.io.Serializable;
import java.util.List;

import squidpony.squidmath.Coord;

/**
 * Abstraction over a list of {@link Coord}. This allows to use list of coords
 * coming from {@link CoordPacker} or regular {@link List lists}.
 * 
 * <p>
 * Zones are {@link Serializable}, but serialization doesn't change the internal
 * representation (some would want to pack {@link ListZone} into
 * {@link CoordPackerZone}s when serializing). I find that overzealous for a
 * simple interface. If you want your zones to be be packed when serialized,
 * create {@link CoordPackerZone} yourself.
 * </p>
 * 
 * @author smelC
 * 
 * @see CoordPacker
 */
public interface Zone extends Serializable {

	/**
	 * @return Whether this zone is empty.j
	 */
	public boolean isEmpty();

	/**
	 * @return The number of cells that this zone contains (the size
	 *         {@link #getAll()}).
	 */
	public int size();

	/**
	 * @param x
	 * @param y
	 * @return Whether this zone contains the coordinate (x,y).
	 */
	public boolean contains(int x, int y);

	/**
	 * @param c
	 * @return Whether this zone contains {@code c}.
	 */
	public boolean contains(Coord c);

	/**
	 * @return All cells in this zone.
	 */
	public List<Coord> getAll();

	/**
	 * A convenience partial implementation. Please try for all new
	 * implementations of {@link Zone} to be subtypes of this class. It usually
	 * prove handy at some point to have a common superclass.
	 * 
	 * @author smelC
	 */
	public static abstract class Skeleton implements Zone {

		private static final long serialVersionUID = 4436698111716212256L;

		@Override
		/* Convenience implementation, feel free to override */
		public boolean contains(Coord c) {
			return contains(c.x, c.y);
		}

	}
}
