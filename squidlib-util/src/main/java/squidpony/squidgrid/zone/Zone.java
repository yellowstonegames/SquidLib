package squidpony.squidgrid.zone;

import squidpony.squidmath.Coord;

import java.io.Serializable;
import java.util.List;

/**
 * Abstraction over a list of {@link Coord}. This allows to use the short arrays
 * coming from {@link squidpony.squidmath.CoordPacker}, which are compressed for
 * better memory usage, regular {@link List lists of Coord}, which are often the
 * simplest option, or {@link squidpony.squidmath.GreasedRegion GreasedRegions},
 * which are "greasy" in the fatty-food sense (they are heavier objects, and are
 * uncompressed) but also "greased" like greased lightning (they are very fast at
 * spatial transformations on their region).
 * <p>
 * Zones are {@link Serializable}, but serialization doesn't change the internal
 * representation (some would want to pack {@link ListZone} into
 * {@link CoordPackerZone}s when serializing). I find that overzealous for a
 * simple interface. If you want your zones to be be packed when serialized,
 * create {@link CoordPackerZone} yourself. In squidlib-extra, GreasedRegions are
 * given slightly special treatment during that JSON-like serialization so they
 * avoid repeating certain information, but they are still going to be larger than
 * compressed short arrays from CoordPacker.
 * </p>
 * <p>
 * While CoordPacker produces short arrays that can be wrapped in CoordPackerZone
 * objects, and a List of Coord can be similarly wrapped in a ListZone object,
 * GreasedRegion extends {@link Zone.Skeleton} and so implements Zone itself.
 * Unlike CoordPackerZone, which is immutable in practice (changing the short
 * array reference is impossible and changing the elements rarely works as
 * planned), GreasedRegion is mutable for performance reasons, and may need copies
 * to be created if you want to keep around older GreasedRegions.
 * </p>
 * @author smelC
 * @see squidpony.squidmath.CoordPacker
 * @see squidpony.squidmath.GreasedRegion
 */
public interface Zone extends Serializable {

    /**
     * @return Whether this zone is empty.
     */
    boolean isEmpty();

    /**
     * @return The number of cells that this zone contains (the size
     * {@link #getAll()}).
     */
    int size();

    /**
     * @param x
     * @param y
     * @return Whether this zone contains the coordinate (x,y).
     */
    boolean contains(int x, int y);

    /**
     * @param c
     * @return Whether this zone contains {@code c}.
     */
    boolean contains(Coord c);

    /**
     * @return All cells in this zone.
     */
    List<Coord> getAll();

    /**
     * A convenience partial implementation. Please try for all new
     * implementations of {@link Zone} to be subtypes of this class. It usually
     * prove handy at some point to have a common superclass.
     *
     * @author smelC
     */
    abstract class Skeleton implements Zone {

        private static final long serialVersionUID = 4436698111716212256L;

        @Override
        /* Convenience implementation, feel free to override */
        public boolean contains(Coord c) {
            return contains(c.x, c.y);
        }

    }
}
