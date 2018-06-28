package squidpony.squidgrid.zone;

import squidpony.squidgrid.zone.Zone.Skeleton;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.CrossHash;

import java.util.*;

/**
 * A zone constructed by {@link CoordPacker}.
 *
 * @author smelC
 */
public class CoordPackerZone extends Skeleton implements Collection<Coord>, ImmutableZone {

    protected final short[] shorts;

    protected transient List<Coord> unpacked;

    private static final long serialVersionUID = -3718415979846804238L;

    public CoordPackerZone(short[] shorts) {
        this.shorts = shorts;
    }

    @Override
    public boolean isEmpty() {
        return CoordPacker.isEmpty(shorts);
    }

    /**
     * Returns <tt>true</tt> if this collection contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this collection
     * contains at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this collection is to be tested
     * @return <tt>true</tt> if this collection contains the specified
     * element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this collection
     *                              (<a href="#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              collection does not permit null elements
     *                              (<a href="#optional-restrictions">optional</a>)
     */
    @Override
    public boolean contains(Object o) {
        return (o instanceof Coord) && CoordPacker.queryPacked(shorts, ((Coord) o).x, ((Coord) o).y);
    }

    /**
     * Returns an array containing all of the elements in this collection.
     * If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order.
     * <p>
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this collection.  (In other words, this method must
     * allocate a new array even if this collection is backed by an array).
     * The caller is thus free to modify the returned array.
     * <p>
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this collection
     */
    @Override
    public Object[] toArray() {
        return CoordPacker.allPacked(shorts);
    }

    /**
     * Returns an array containing all of the elements in this collection;
     * the runtime type of the returned array is that of the specified array.
     * If the collection fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this collection.
     * <p>
     * <p>If this collection fits in the specified array with room to spare
     * (i.e., the array has more elements than this collection), the element
     * in the array immediately following the end of the collection is set to
     * <tt>null</tt>.  (This is useful in determining the length of this
     * collection <i>only</i> if the caller knows that this collection does
     * not contain any <tt>null</tt> elements.)
     * <p>
     * <p>If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order.
     * <p>
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     * <p>
     * <p>Suppose <tt>x</tt> is a collection known to contain only strings.
     * The following code can be used to dump the collection into a newly
     * allocated array of <tt>String</tt>:
     * <p>
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     * <p>
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the elements of this collection are to be
     *          stored, if it is big enough; otherwise, a new array of the same
     *          runtime type is allocated for this purpose.
     * @return an array containing all of the elements in this collection
     * @throws ArrayStoreException  if the runtime type of the specified array
     *                              is not a supertype of the runtime type of every element in
     *                              this collection
     * @throws NullPointerException if the specified array is null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a == null)
            throw new NullPointerException("Array passed to CoordPackerZone.toArray() must not be null");
        final int size = a.length, ssize = CoordPacker.count(shorts);
        if (ssize == size)
            return (T[]) CoordPacker.allPacked(shorts);
        a = Arrays.copyOf(a, ssize);
        for (int i = 0; i < ssize; i++) {
            a[i] = (T) CoordPacker.nth(shorts, i);
        }
        return a;
    }

    /**
     * Does nothing (this Zone is immutable).
     */
    @Override
    public boolean add(Coord coord) {
        return false;
    }

    /**
     * Does nothing (this Zone is immutable).
     */
    @Override
    public boolean remove(Object o) {
        return false;
    }

    /**
     * Returns <tt>true</tt> if this collection contains all of the elements
     * in the specified collection.
     *
     * @param c collection to be checked for containment in this collection
     * @return <tt>true</tt> if this collection contains all of the elements
     * in the specified collection
     * @throws ClassCastException if the types of one or more elements
     *                            in the specified collection are not Coord
     * @see #contains(Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean containsAll(Collection<?> c) {
        return CoordPacker.count(shorts) == CoordPacker.count(CoordPacker.insertSeveralPacked(shorts, (Collection) c));
    }

    /**
     * Does nothing (this Zone is immutable).
     */
    @Override
    public boolean addAll(Collection<? extends Coord> c) {
        return false;
    }

    /**
     * Does nothing (this Zone is immutable).
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    /**
     * Does nothing (this Zone is immutable).
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    /**
     * Does nothing (this Zone is immutable).
     */
    @Override
    public void clear() {

    }

    @Override
    public int size() {
        return CoordPacker.count(shorts);
    }

    @Override
    public boolean contains(int x, int y) {
        return CoordPacker.regionsContain(shorts, CoordPacker.packOne(x, y));
    }

    @Override
    public boolean contains(Coord c) {
        return CoordPacker.regionsContain(shorts, CoordPacker.packOne(c));
    }

    @Override
    public List<Coord> getAll() {
        if (unpacked == null) {
            final Coord[] allPacked = CoordPacker.allPacked(shorts);
            unpacked = new ArrayList<Coord>(allPacked.length);
            Collections.addAll(unpacked, allPacked);
        }
        return unpacked;
    }

    @Override
    public CoordPackerZone expand(int distance) {
        return new CoordPackerZone(CoordPacker.expand(shorts, distance, 256, 256));
    }

    @Override
    public CoordPackerZone expand8way(int distance) {
        return new CoordPackerZone(CoordPacker.expand(shorts, distance, 256, 256, true));
    }

    @Override
    public boolean contains(Zone other) {
        return CoordPacker.count(shorts) == CoordPacker.count(CoordPacker.insertSeveralPacked(shorts, other.getAll()));
    }

    @Override
    public boolean intersectsWith(Zone other) {
        if (other instanceof CoordPackerZone)
            return CoordPacker.intersects(shorts, ((CoordPackerZone) other).shorts);
        for (Coord c : other) {
            if (CoordPacker.queryPacked(shorts, c.x, c.y))
                return true;
        }
        return false;
    }

    @Override
    public Zone extend() {
        return new CoordPackerZone(CoordPacker.expand(shorts, 1, 256, 256, true));
    }

    @Override
    public Collection<Coord> getInternalBorder() {
        return new CoordPackerZone(CoordPacker.surface(shorts, 1, 256, 256, true));
    }

    @Override
    public Collection<Coord> getExternalBorder() {
        return new CoordPackerZone(CoordPacker.fringe(shorts, 1, 256, 256, true));
    }

    @Override
    public Zone translate(int x, int y) {
        return new CoordPackerZone(CoordPacker.translate(shorts, x, y, 256, 256));
    }

    @Override
    public String toString() {
        return (unpacked == null ? shorts : unpacked).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoordPackerZone that = (CoordPackerZone) o;

        return Arrays.equals(shorts, that.shorts);
    }

    @Override
    public int hashCode() {
        return CrossHash.hash(shorts);
    }
}