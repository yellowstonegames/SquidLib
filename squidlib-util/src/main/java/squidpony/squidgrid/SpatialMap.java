package squidpony.squidgrid;

import squidpony.squidmath.*;

import java.util.*;

/**
 * A data structure that seems to be re-implemented often for games, this associates Coord positions and generic I
 * identities with generic E elements. You can get an element from a SpatialMap with either an identity or a position,
 * change the position of an element without changing its value or identity, modify an element given its identity and
 * a new value, and perform analogues to most of the features of the Map interface, though this does not implement Map
 * because it essentially has two key types and one value type. You can also iterate through the values in insertion
 * order, where insertion order should be stable even when elements are moved or modified (the relevant key is the
 * identity, which is never changed in this class). Uses two OrderedMap fields internally.
 * Created by Tommy Ettinger on 1/2/2016.
 */
public class SpatialMap<I, E> implements Iterable<E> {

    public static class SpatialTriple<I,E>
    {
        public Coord position;
        public I id;
        public E element;

        public SpatialTriple()
        {
            position = Coord.get(0,0);
            id = null;
            element = null;
        }
        public SpatialTriple(Coord position, I id, E element) {
            this.position = position;
            this.id = id;
            this.element = element;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SpatialTriple<?, ?> that = (SpatialTriple<?, ?>) o;

            if (position != null ? !position.equals(that.position) : that.position != null) return false;
            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            return element != null ? element.equals(that.element) : that.element == null;

        }

        @Override
        public int hashCode() {
            int result = position != null ? position.hashCode() : 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            result = 31 * result + (element != null ? element.hashCode() : 0);
            return result;
        }
    }

    protected OrderedMap<I, SpatialTriple<I, E>> itemMapping;
    protected OrderedMap<Coord, SpatialTriple<I, E>> positionMapping;

    /**
     * Constructs a SpatialMap with capacity 32.
     */
    public SpatialMap()
    {
        itemMapping = new OrderedMap<>(32);
        positionMapping = new OrderedMap<>(32);
    }

    /**
     * Constructs a SpatialMap with the given capacity
     * @param capacity the capacity for each of the internal OrderedMaps
     */
    public SpatialMap(int capacity)
    {
        itemMapping = new OrderedMap<>(capacity);
        positionMapping = new OrderedMap<>(capacity);
    }

    /**
     * Constructs a SpatialMap given arrays of Coord, identity, and element; all 3 arrays should have the same length,
     * since this will use only up to the minimum length of these arrays for how many it adds. Each unique id will be
     * added with the corresponding element at the corresponding Coord position if that position is not already filled.
     * @param coords a starting array of Coord positions; indices here correspond to the other parameters
     * @param ids a starting array of identities; indices here correspond to the other parameters
     * @param elements a starting array of elements; indices here correspond to the other parameters
     */
    public SpatialMap(Coord[] coords, I[] ids, E[] elements)
    {
        itemMapping = new OrderedMap<>(
                Math.min(coords.length, Math.min(ids.length, elements.length)));
        positionMapping = new OrderedMap<>(
                Math.min(coords.length, Math.min(ids.length, elements.length)));

        for (int i = 0; i < coords.length && i < ids.length && i < elements.length; i++) {
            add(coords[i], ids[i], elements[i]);
        }
    }

    /**
     * Constructs a SpatialMap given collections of Coord, identity, and element; all 3 collections should have the same
     * length, since this will use only up to the minimum length of these collections for how many it adds. Each unique
     * id will be added with the corresponding element at the corresponding Coord position if that position is not
     * already filled.
     * @param coords a starting collection of Coord positions; indices here correspond to the other parameters
     * @param ids a starting collection of identities; indices here correspond to the other parameters
     * @param elements a starting collection of elements; indices here correspond to the other parameters
     */
    public SpatialMap(Collection<Coord> coords, Collection<I> ids, Collection<E> elements)
    {
        itemMapping = new OrderedMap<>(
                Math.min(coords.size(), Math.min(ids.size(), elements.size())));
        positionMapping = new OrderedMap<>(
                Math.min(coords.size(), Math.min(ids.size(), elements.size())));
        if(itemMapping.size() <= 0)
            return;
        Iterator<Coord> cs = coords.iterator();
        Iterator<I> is = ids.iterator();
        Iterator<E> es = elements.iterator();
        Coord c = cs.next();
        I i = is.next();
        E e = es.next();
        for (; cs.hasNext() && is.hasNext() && es.hasNext(); c = cs.next(), i = is.next(), e = es.next()) {
            add(c, i, e);
        }
    }

    /**
     * Adds a new element with the given identity and Coord position. If the position is already occupied by an element
     * in this data structure, does nothing. If the identity is already used, this also does nothing. If the identity
     * and position are both unused, this adds element to the data structure.
     * <br>
     * You should strongly avoid calling remove() and add() to change an element; prefer modify() and move().
     * @param coord the Coord position to place the element at; should be empty
     * @param id the identity to associate the element with; should be unused
     * @param element the element to add
     */
    public void add(Coord coord, I id, E element)
    {
        if(itemMapping.containsKey(id))
            return;
        if(!positionMapping.containsKey(coord))
        {
            SpatialTriple<I, E> triple = new SpatialTriple<>(coord, id, element);
            itemMapping.put(id, triple);
            positionMapping.put(coord, triple);
        }
    }

    /**
     * Inserts a new element with the given identity and Coord position, potentially overwriting an existing element.
     * <br>
     * If you want to alter an existing element, use modify() or move().
     * @param coord the Coord position to place the element at; should be empty
     * @param id the identity to associate the element with; should be unused
     * @param element the element to add
     */
    public void put(Coord coord, I id, E element)
    {
        SpatialTriple<I, E> triple = new SpatialTriple<>(coord, id, element);
        itemMapping.remove(id);
        positionMapping.remove(coord);
        itemMapping.put(id, triple);
        positionMapping.put(coord, triple);
    }

    /**
     * Inserts a SpatialTriple into this SpatialMap without changing it, potentially overwriting an existing element.
     * SpatialTriple objects can be obtained by the triples() or tripleIterator() methods, and can also be constructed
     * on their own.
     * <br>
     * If you want to alter an existing element, use modify() or move().
     * @param triple a SpatialTriple (an inner class of SpatialMap) with the same type parameters as this class
     */
    public void put(SpatialTriple<I, E> triple)
    {
        itemMapping.remove(triple.id);
        positionMapping.remove(triple.position);
        itemMapping.put(triple.id, triple);
        positionMapping.put(triple.position, triple);
    }

    /**
     * Changes the element's value associated with id. The key id should exist before calling this; if there is no
     * matching id, this returns null.
     * @param id the identity of the element to modify
     * @param newValue the element value to replace the previous element with.
     * @return the previous element value associated with id
     */
    public E modify(I id, E newValue)
    {
        SpatialTriple<I, E> gotten = itemMapping.get(id);
        if(gotten != null) {
            E previous = gotten.element;
            gotten.element = newValue;
            return previous;
        }
        return null;
    }
    /**
     * Changes the element's value associated with pos. The key pos should exist before calling this; if there is no
     * matching position, this returns null.
     * @param pos the position of the element to modify
     * @param newValue the element value to replace the previous element with.
     * @return the previous element value associated with id
     */
    public E positionalModify(Coord pos, E newValue)
    {
        SpatialTriple<I, E> gotten = positionMapping.get(pos);
        if(gotten != null) {
            E previous = gotten.element;
            gotten.element = newValue;
            return previous;
        }
        return null;
    }

    /**
     * Move an element from one position to another; moves whatever is at the Coord position previous to the new Coord
     * position target. The element will not be present at its original position if target is unoccupied, but nothing
     * will change if target is occupied.
     * @param previous the starting Coord position of an element to move
     * @param target the Coord position to move the element to
     * @return the moved element if movement was successful or null otherwise
     */
    public E move(Coord previous, Coord target)
    {
        if(positionMapping.containsKey(previous) && !positionMapping.containsKey(target)) {
            SpatialTriple<I, E> gotten = positionMapping.remove(previous);
            gotten.position = target;
            positionMapping.put(target, gotten);
            return gotten.element;
        }
        return null;
    }

    /**
     * Move an element, picked by its identity, to a new Coord position. Finds the element using only the id, and does
     * not need the previous position. The target position must be empty for this to move successfully, and the id must
     * exist in this data structure for this to move anything.
     * @param id the identity of the element to move
     * @param target the Coord position to move the element to
     * @return the moved element if movement was successful or null otherwise
     */
    public E move(I id, Coord target)
    {
        if(itemMapping.containsKey(id) && !positionMapping.containsKey(target)) {
            SpatialTriple<I, E> gotten = itemMapping.get(id);
            positionMapping.remove(gotten.position);
            gotten.position = target;
            positionMapping.put(target, gotten);
            return gotten.element;
        }
        return null;
    }

    /**
     * Removes the element at the given position from all storage in this data structure.
     * <br>
     * You should strongly avoid calling remove() and add() to change an element; prefer modify() and move().
     * @param coord the position of the element to remove
     * @return the value of the element that was removed or null if nothing was present at the position
     */
    public E remove(Coord coord)
    {
        SpatialTriple<I, E> gotten = positionMapping.remove(coord);
        if(gotten != null) {
            itemMapping.remove(gotten.id);
            return gotten.element;
        }
        return null;
    }
    /**
     * Removes the element with the given identity from all storage in this data structure.
     * <br>
     * You should strongly avoid calling remove() and add() to change an element; prefer modify() and move().
     * @param id the identity of the element to remove
     * @return the value of the element that was removed or null if nothing was present at the position
     */
    public E remove(I id)
    {
        SpatialTriple<I, E> gotten = itemMapping.remove(id);
        if(gotten != null) {
            positionMapping.remove(gotten.position);
            return gotten.element;
        }
        return null;
    }

    /**
     * Checks whether this contains the given element. Slower than containsKey and containsPosition (linear time).
     * @param o an Object that should be an element if you expect this to possibly return true
     * @return true if o is contained as an element in this data structure
     */
    public boolean containsValue(Object o)
    {
        if(o == null)
        {
            for(SpatialTriple<I,E> v : itemMapping.values())
            {
                if(v != null && v.element == null)
                    return true;
            }
        }
        else {
            for (SpatialTriple<I, E> v : itemMapping.values()) {
                if (v != null && v.element != null && v.element.equals(o))
                    return true;
            }
        }
        return false;
    }
    /**
     * Checks whether this contains the given identity key.
     * @param o an Object that should be of the generic I type if you expect this to possibly return true
     * @return true if o is an identity key that can be used with this data structure
     */
    public boolean containsKey(Object o)
    {
        return itemMapping.containsKey(o);
    }
    /**
     * Checks whether this contains anything at the given position.
     * @param o an Object that should be a Coord if you expect this to possibly return true
     * @return true if o is a Coord that is associated with some element in this data structure
     */
    public boolean containsPosition(Object o)
    {
        return positionMapping.containsKey(o);
    }

    /**
     * Gets the element at the given Coord position.
     * @param c the position to get an element from
     * @return the element if it exists or null otherwise
     */
    public E get(Coord c)
    {
        SpatialTriple<I, E> gotten = positionMapping.get(c);
        if(gotten != null)
            return gotten.element;
        return null;
    }

    /**
     * Gets the element with the given identity.
     * @param i the identity of the element to get
     * @return the element if it exists or null otherwise
     */
    public E get(I i)
    {
        SpatialTriple<I, E> gotten = itemMapping.get(i);
        if(gotten != null)
            return gotten.element;
        return null;
    }

    /**
     * Gets the position of the element with the given identity.
     * @param i the identity of the element to get a position from
     * @return the position of the element if it exists or null otherwise
     */
    public Coord getPosition(I i)
    {
        SpatialTriple<I, E> gotten = itemMapping.get(i);
        if(gotten != null)
            return gotten.position;
        return null;
    }

    /**
     * Gets the identity of the element at the given Coord position.
     * @param c the position to get an identity from
     * @return the identity of the element if it exists at the given position or null otherwise
     */
    public I getIdentity(Coord c)
    {
        SpatialTriple<I, E> gotten = positionMapping.get(c);
        if(gotten != null)
            return gotten.id;
        return null;
    }

    /**
     * Get a Set of all positions used for values in this data structure, returning a OrderedSet (defensively copying
     * the key set used internally) for its stable iteration order.
     * @return a OrderedSet of Coord corresponding to the positions present in this data structure.
     */
    public OrderedSet<Coord> positions()
    {
        return new OrderedSet<>(positionMapping.keySet());
    }
    /**
     * Get a Set of all identities used for values in this data structure, returning a OrderedSet (defensively
     * copying the key set used internally) for its stable iteration order.
     * @return a OrderedSet of I corresponding to the identities present in this data structure.
     */
    public OrderedSet<I> identities()
    {
        return new OrderedSet<>(itemMapping.keySet());
    }

    /**
     * Gets all data stored in this as a collection of values similar to Map.Entry, but containing a Coord, I, and E
     * value for each entry, in insertion order. The type is SpatialTriple, defined in a nested class.
     * @return a Collection of SpatialTriple of I, E
     */
    public Collection<SpatialTriple<I, E>> triples()
    {
        return itemMapping.values();
    }

    /**
     * Given an Iterable (such as a List, Set, or other Collection) of Coord, gets all elements in this SpatialMap that
     * share a position with one of the Coord objects in positions and returns them as an ArrayList of elements.
     * @param positions an Iterable (such as a List or Set) of Coord
     * @return an ArrayList, possibly empty, of elements that share a position with a Coord in positions
     */
    public ArrayList<E> getManyPositions(Iterable<Coord> positions)
    {
        ArrayList<E> gotten = new ArrayList<>();
        SpatialTriple<I, E> ie;
        for(Coord p : positions)
        {
            if((ie = positionMapping.get(p)) != null)
                gotten.add(ie.element);
        }
        return gotten;
    }

    /**
     * Given an Iterable (such as a List, Set, or other Collection) of I, gets all elements in this SpatialMap that
     * share an identity with one of the I objects in identities and returns them as an ArrayList of elements.
     * @param identities an Iterable (such as a List or Set) of I
     * @return an ArrayList, possibly empty, of elements that share an Identity with an I in identities
     */
    public ArrayList<E> getManyIdentities(Iterable<I> identities)
    {
        ArrayList<E> gotten = new ArrayList<>();
        SpatialTriple<I, E> ie;
        for(I i : identities)
        {
            if((ie = itemMapping.get(i)) != null)
                gotten.add(ie.element);
        }
        return gotten;
    }

    /**
     * Given an array of Coord, gets all elements in this SpatialMap that share a position with one of the Coord objects
     * in positions and returns them as an ArrayList of elements.
     * @param positions an array of Coord
     * @return an ArrayList, possibly empty, of elements that share a position with a Coord in positions
     */
    public ArrayList<E> getManyPositions(Coord[] positions)
    {
        ArrayList<E> gotten = new ArrayList<>(positions.length);
        SpatialTriple<I, E> ie;
        for(Coord p : positions)
        {
            if((ie = positionMapping.get(p)) != null)
                gotten.add(ie.element);
        }
        return gotten;
    }
    /**
     * Given an array of I, gets all elements in this SpatialMap that share an identity with one of the I objects in
     * identities and returns them as an ArrayList of elements.
     * @param identities an array of I
     * @return an ArrayList, possibly empty, of elements that share an Identity with an I in identities
     */
    public ArrayList<E> getManyIdentities(I[] identities)
    {
        ArrayList<E> gotten = new ArrayList<>(identities.length);
        SpatialTriple<I, E> ie;
        for(I i : identities)
        {
            if((ie = itemMapping.get(i)) != null)
                gotten.add(ie.element);
        }
        return gotten;
    }

    public E randomElement(IRNG rng)
    {
        if(itemMapping.isEmpty())
            return null;
        return itemMapping.randomValue(rng).element;
    }

    public Coord randomPosition(IRNG rng)
    {
        if(positionMapping.isEmpty())
            return null;
        return positionMapping.randomKey(rng);
    }
    public I randomIdentity(IRNG rng)
    {
        if(itemMapping.isEmpty())
            return null;
        return itemMapping.randomKey(rng);
    }

    public SpatialTriple<I, E> randomEntry(IRNG rng)
    {
        if(itemMapping.isEmpty())
            return null;
        return itemMapping.randomValue(rng);
    }

    /**
     * Given the size and position of a rectangular area, creates a new SpatialMap from this one that refers only to the
     * subsection of this SpatialMap shared with the rectangular area. Will not include any elements from this
     * SpatialMap with positions beyond the bounds of the given rectangular area, and will include all elements from
     * this that are in the area.
     * @param x the minimum x-coordinate of the rectangular area
     * @param y the minimum y-coordinate of the rectangular area
     * @param width the total width of the rectangular area
     * @param height the total height of the rectangular area
     * @return a new SpatialMap that refers to a subsection of this one
     */
    public SpatialMap<I, E> rectangleSection(int x, int y, int width, int height)
    {
        SpatialMap<I, E> next = new SpatialMap<>(positionMapping.size());
        Coord tmp;
        for(SpatialTriple<I, E> ie : positionMapping.values())
        {
            tmp = ie.position;
            if(tmp.x >= x && tmp.y >= y && tmp.x + width > x && tmp.y + height > y)
                next.put(ie);
        }
        return next;
    }

    /**
     * Given the center position, Radius to determine measurement, and maximum distance from the center, creates a new
     * SpatialMap from this one that refers only to the subsection of this SpatialMap shared with the area within the
     * given distance from the center as measured by measurement. Will not include any elements from this SpatialMap
     * with positions beyond the bounds of the given area, and will include all elements from this that are in the area.
     * @param x the center x-coordinate of the area
     * @param y the center y-coordinate of the area
     * @param measurement a Radius enum, such as Radius.CIRCLE or Radius.DIAMOND, that calculates distance
     * @param distance the maximum distance from the center to include in the area
     * @return a new SpatialMap that refers to a subsection of this one
     */
    public SpatialMap<I, E> radiusSection(int x, int y, Radius measurement, int distance)
    {
        SpatialMap<I, E> next = new SpatialMap<>(positionMapping.size());
        Coord tmp;
        for(SpatialTriple<I, E> ie : positionMapping.values())
        {
            tmp = ie.position;
            if(measurement.inRange(x, y, tmp.x, tmp.y, 0, distance))
                next.put(ie);
        }
        return next;
    }

    /**
     * Given the center position and maximum distance from the center, creates a new SpatialMap from this one that
     * refers only to the subsection of this SpatialMap shared with the area within the given distance from the center,
     * measured with Euclidean distance to produce a circle shape. Will not include any elements from this SpatialMap
     * with positions beyond the bounds of the given area, and will include all elements from this that are in the area.
     * @param x the center x-coordinate of the area
     * @param y the center y-coordinate of the area
     * @param radius the maximum distance from the center to include in the area, using Euclidean distance
     * @return a new SpatialMap that refers to a subsection of this one
     */
    public SpatialMap<I, E> circleSection(int x, int y, int radius)
    {
        return radiusSection(x, y, Radius.CIRCLE, radius);
    }

    public void clear()
    {
        itemMapping.clear();
        positionMapping.clear();
    }
    public boolean isEmpty()
    {
        return itemMapping.isEmpty();
    }
    public int size()
    {
        return itemMapping.size();
    }
    public Object[] toArray()
    {
        Object[] contents = itemMapping.values().toArray();
        for (int i = 0; i < contents.length; i++) {
            contents[i] = ((SpatialTriple<?,?>)contents[i]).element;
        }
        return contents;
    }

    /**
     * Replaces the contents of the given array with the elements this holds, in insertion order, until either this
     * data structure or the array has been exhausted.
     * @param a the array to replace; should usually have the same length as this data structure's size.
     * @return an array of elements that should be the same as the changed array originally passed as a parameter.
     */
    public E[] toArray(E[] a)
    {
        Collection<SpatialTriple<I,E>> contents = itemMapping.values();
        int i = 0;
        for (SpatialTriple<I,E> triple : contents) {
            if(i < a.length)
                a[i] = triple.element;
            else
                break;
            i++;
        }
        return a;
    }

    /**
     * Iterates through values in insertion order.
     * @return an Iterator of generic type E
     */
    @Override
    public Iterator<E> iterator()
    {
        final Iterator<SpatialTriple<I, E>> it = itemMapping.values().iterator();
        return new Iterator<E>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public E next() {
                SpatialTriple<I,E> triple = it.next();
                if(triple != null)
                    return triple.element;
                return null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Iterates through values similar to Map.Entry, but containing a Coord, I, and E value for each entry, in insertion
     * order. The type is SpatialTriple, defined in a nested class.
     * @return an Iterator of SpatialTriple of I, E
     */
    public Iterator<SpatialTriple<I, E>> tripleIterator()
    {
        return itemMapping.values().iterator();
    }
    /**
     * Iterates through positions in insertion order; has less predictable iteration order than the other iterators.
     * @return an Iterator of Coord
     */
    public Iterator<Coord> positionIterator()
    {
        return positionMapping.keySet().iterator();
    }
    /**
     * Iterates through identity keys in insertion order.
     * @return an Iterator of generic type I
     */
    public Iterator<I> identityIterator()
    {
        return itemMapping.keySet().iterator();
    }

    /**
     * Iterates through positions in a rectangular region (starting at a minimum of x, y and extending to the specified
     * width and height) in left-to-right, then top-to-bottom order (the same as reading a page of text).
     * Any Coords this returns should be viable arguments to get() if you want a corresponding element.
     * @return an Iterator of Coord
     */
    public Iterator<Coord> rectanglePositionIterator(int x, int y, int width, int height)
    {
        return new RectangularIterator(x, y, width, height);
    }

    /**
     * Iterates through positions in a region defined by a Radius (starting at a minimum of x - distance, y - distance
     * and extending to x + distance, y + distance but skipping any positions where the Radius considers a position
     * further from x, y than distance) in left-to-right, then top-to-bottom order (the same as reading a page of text).
     * You can use Radius.SQUARE to make a square region (which could also be made with rectanglePositionIterator()),
     * Radius.DIAMOND to make a, well, diamond-shaped region, or Radius.CIRCLE to make a circle (which could also be
     * made with circlePositionIterator).
     * Any Coords this returns should be viable arguments to get() if you want a corresponding element.
     * @return an Iterator of Coord
     */
    public Iterator<Coord> radiusPositionIterator(int x, int y, Radius measurement, int distance)
    {
        return new RadiusIterator(x, y, measurement, distance);
    }
    /**
     * Iterates through positions in a circular region (starting at a minimum of x - distance, y - distance and
     * extending to x + distance, y + distance but skipping any positions where the Euclidean distance from x,y to the
     * position is more than distance) in left-to-right, then top-to-bottom order (the same as reading a page of text).
     * Any Coords this returns should be viable arguments to get() if you want a corresponding element.
     * @return an Iterator of Coord
     */
    public Iterator<Coord> circlePositionIterator(int x, int y, int distance)
    {
        return new RadiusIterator(x, y, Radius.CIRCLE, distance);
    }

    private class RectangularIterator implements Iterator<Coord>
    {
        int x, y, width, height, idx,
                poolWidth = Coord.getCacheWidth(), poolHeight = Coord.getCacheHeight();
        Set<Coord> positions;
        Coord temp;
        RectangularIterator(int x, int y, int width, int height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            idx = -1;
            positions = positionMapping.keySet();
        }

        @Override
        public boolean hasNext() {
            if (idx < width * height - 1) {
                Coord t2;
                int n = idx;
                do {
                    n = findNext(n);
                    if (idx < 0)
                        return n >= 0;
                    else {
                        if(x + n % width >= 0 && x + n % width < poolWidth
                                && y + n / width >= 0 && y + n / width < poolHeight)
                            t2 = Coord.get(x + n % width, y + n / width);
                        else t2 = Coord.get(-1, -1);
                    }
                } while (!positions.contains(t2));
				/* Not done && has next */
                return n >= 0;
            }
            return false;
        }


        @Override
        public Coord next() {
            do {
                idx = findNext(idx);
                if (idx < 0)
                    throw new NoSuchElementException();
                if(x + idx % width >= 0 && x + idx % width < poolWidth
                        && y + idx / width >= 0 && y + idx / width < poolHeight)
                    temp = Coord.get(x + idx % width, y + idx / width);
                else temp = Coord.get(-1, -1);
            } while (!positions.contains(temp));
            return temp;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private int findNext(final int idx) {
            if (idx < 0) {
				/* First iteration */
                return 0;
            } else {
                if (idx >= width * height - 1)
                {
					/* Done iterating */
                    return -1;
                } else {
                    return idx + 1;
                }
            }
        }
    }

    private class RadiusIterator implements Iterator<Coord>
    {
        int x, y, width, height, distance, idx,
                poolWidth = Coord.getCacheWidth(), poolHeight = Coord.getCacheHeight();
        Set<Coord> positions;
        Coord temp;
        Radius measurement;
        RadiusIterator(int x, int y, Radius measurement, int distance)
        {
            this.x = x;
            this.y = y;
            width = 1 + distance * 2;
            height = 1 + distance * 2;
            this.distance = distance;
            this.measurement = measurement;
            idx = -1;
            positions = positionMapping.keySet();
        }

        @Override
        public boolean hasNext() {
            if (idx < width * height - 1) {
                Coord t2;
                int n = idx;
                do {
                    n = findNext(n);
                    if (idx < 0)
                        return n >= 0;
                    else {
                        if(x - distance + n % width >= 0 && x - distance + n % width < poolWidth
                                && y - distance + n / width >= 0 && y - distance + n / width < poolHeight &&
                                measurement.radius(x, y,
                                        x - distance + n % width, y - distance + n / width) <= distance)
                            t2 = Coord.get(x - distance + n % width, y - distance + n / width);
                        else t2 = Coord.get(-1, -1);
                    }
                } while (!positions.contains(t2));
				/* Not done && has next */
                return n >= 0;
            }
            return false;
        }


        @Override
        public Coord next() {
            do {
                idx = findNext(idx);
                if (idx < 0)
                    throw new NoSuchElementException();
                if(x - distance + idx % width >= 0 && x - distance + idx % width < poolWidth
                        && y - distance + idx / width >= 0 && y - distance + idx / width < poolHeight &&
                        measurement.radius(x, y,
                                x - distance + idx % width, y - distance + idx / width) <= distance)
                    temp = Coord.get(x - distance + idx % width, y - distance + idx / width);
                else temp = Coord.get(-1, -1);
            } while (!positions.contains(temp));
            return temp;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private int findNext(final int idx) {
            if (idx < 0) {
				/* First iteration */
                return 0;
            } else {
                if (idx >= width * height - 1)
                {
					/* Done iterating */
                    return -1;
                } else {
                    return idx + 1;
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpatialMap<?, ?> that = (SpatialMap<?, ?>) o;

        if (itemMapping != null ? !itemMapping.equals(that.itemMapping) : that.itemMapping != null) return false;
        return positionMapping != null ? positionMapping.equals(that.positionMapping) : that.positionMapping == null;

    }

    @Override
    public int hashCode() {
        int result = itemMapping != null ? itemMapping.hashCode() : 0;
        result = 31 * result + (positionMapping != null ? positionMapping.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SpatialMap{" +
                "itemMapping=" + itemMapping +
                ", positionMapping=" + positionMapping +
                '}';
    }

}
