package squidpony.squidmath;

import squidpony.annotation.Beta;

import java.io.Serializable;
import java.util.*;

/**
 * Work in progress Map that can be split up into separate or overlapping iteration orders.
 * <br>
 * Created by Tommy Ettinger on 11/29/2019.
 */
@Beta
public class OrganizedMap<K, V> extends OrderedMap<K, V> {
    /**
     * A list of organizing regions in the {@link #order} of this OrganizedMap, where each region is a contiguous span
     * of entries represented by a {@link Coord} where x is the lower bound (inclusive) and y is the upper bound
     * (exclusive).
     */
    public OrderedMap<Coord, MapEntrySet> organizations = new OrderedMap<>(4, CrossHash.mildHasher);
    public OrderedMap<Coord, KeySet> organizedKeys = new OrderedMap<>(4, CrossHash.mildHasher);
    public OrderedMap<Coord, ValueCollection> organizedValues = new OrderedMap<>(4, CrossHash.mildHasher);
    /**
     * Creates a new OrganizedMap.
     * <p>
     * <p>The actual table size will be the least power of two greater than <code>expected</code>/<code>f</code>.
     *
     * @param expected the expected number of elements in the hash set.
     * @param f        the load factor.
     */
    public OrganizedMap(int expected, float f) {
        super(expected, f);
    }

    /**
     * Creates a new OrganizedMap with 0.75f as load factor.
     *
     * @param expected the expected number of elements in the OrganizedMap.
     */
    public OrganizedMap(int expected) {
        super(expected);
    }

    /**
     * Creates a new OrganizedMap with initial expected 16 entries and 0.75f as load factor.
     */
    public OrganizedMap() {
        super();
    }

    /**
     * Creates a new OrganizedMap copying a given one.
     *
     * @param m a {@link Map} to be copied into the new OrganizedMap.
     * @param f the load factor.
     */
    public OrganizedMap(Map<? extends K, ? extends V> m, float f) {
        super(m, f);
    }

    /**
     * Creates a new OrganizedMap with 0.75f as load factor copying a given one.
     *
     * @param m a {@link Map} to be copied into the new OrganizedMap.
     */
    public OrganizedMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    /**
     * Creates a new OrganizedMap using the elements of two parallel arrays.
     *
     * @param keyArray   the array of keys of the new OrganizedMap.
     * @param valueArray the array of corresponding values in the new OrganizedMap.
     * @param f          the load factor.
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public OrganizedMap(K[] keyArray, V[] valueArray, float f) {
        super(keyArray, valueArray, f);
    }

    /**
     * Creates a new OrganizedMap using the elements of two parallel arrays.
     *
     * @param keyColl   the collection of keys of the new OrganizedMap.
     * @param valueColl the collection of corresponding values in the new OrganizedMap.
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public OrganizedMap(Collection<K> keyColl, Collection<V> valueColl) {
        super(keyColl, valueColl);
    }

    /**
     * Creates a new OrganizedMap using the elements of two parallel arrays.
     *
     * @param keyColl   the collection of keys of the new OrganizedMap.
     * @param valueColl the collection of corresponding values in the new OrganizedMap.
     * @param f         the load factor.
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public OrganizedMap(Collection<K> keyColl, Collection<V> valueColl, float f) {
        super(keyColl, valueColl, f);
    }

    /**
     * Creates a new OrganizedMap with 0.75f as load factor using the elements of two parallel arrays.
     *
     * @param keyArray   the array of keys of the new OrganizedMap.
     * @param valueArray the array of corresponding values in the new OrganizedMap.
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public OrganizedMap(K[] keyArray, V[] valueArray) {
        super(keyArray, valueArray);
    }

    /**
     * Creates a new OrganizedMap.
     * <p>
     * <p>The actual table size will be the least power of two greater than <code>expected</code>/<code>f</code>.
     *
     * @param expected the expected number of elements in the hash set.
     * @param f        the load factor.
     * @param hasher   used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    public OrganizedMap(int expected, float f, CrossHash.IHasher hasher) {
        super(expected, f, hasher);
    }

    /**
     * Creates a new OrganizedMap with 0.75f as load factor.
     *
     * @param expected the expected number of elements in the OrganizedMap.
     * @param hasher   used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    public OrganizedMap(int expected, CrossHash.IHasher hasher) {
        super(expected, hasher);
    }

    /**
     * Creates a new OrganizedMap with initial expected 16 entries and 0.75f as load factor.
     *
     * @param hasher
     */
    public OrganizedMap(CrossHash.IHasher hasher) {
        super(hasher);
    }

    /**
     * Creates a new OrganizedMap copying a given one.
     *
     * @param m      a {@link Map} to be copied into the new OrganizedMap.
     * @param f      the load factor.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    public OrganizedMap(Map<? extends K, ? extends V> m, float f, CrossHash.IHasher hasher) {
        super(m, f, hasher);
    }

    /**
     * Creates a new OrganizedMap with 0.75f as load factor copying a given one.
     *
     * @param m      a {@link Map} to be copied into the new OrganizedMap.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    public OrganizedMap(Map<? extends K, ? extends V> m, CrossHash.IHasher hasher) {
        super(m, hasher);
    }

    /**
     * Creates a new OrganizedMap using the elements of two parallel arrays.
     *
     * @param keyArray   the array of keys of the new OrganizedMap.
     * @param valueArray the array of corresponding values in the new OrganizedMap.
     * @param f          the load factor.
     * @param hasher     used to hash items; typically only needed when K is an array, where CrossHash has implementations
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public OrganizedMap(K[] keyArray, V[] valueArray, float f, CrossHash.IHasher hasher) {
        super(keyArray, valueArray, f, hasher);
    }

    /**
     * Creates a new OrganizedMap with 0.75f as load factor using the elements of two parallel arrays.
     *
     * @param keyArray   the array of keys of the new OrganizedMap.
     * @param valueArray the array of corresponding values in the new OrganizedMap.
     * @param hasher     used to hash items; typically only needed when K is an array, where CrossHash has implementations
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public OrganizedMap(K[] keyArray, V[] valueArray, CrossHash.IHasher hasher) {
        super(keyArray, valueArray, hasher);
    }
        /**
     * A list iterator over a OrderedMap.
     *
     * <P>
     * This class provides a list iterator over a OrderedMap. The
     * constructor runs in constant time.
     */
    private class MapIterator {
        /**
         * The entry that will be returned by the next call to
         * {@link java.util.ListIterator#previous()} (or <code>null</code> if no
         * previous entry exists).
         */
        int prev = -1;
        /**
         * The entry that will be returned by the next call to
         * {@link java.util.ListIterator#next()} (or <code>null</code> if no
         * next entry exists).
         */
        int next = -1;
        /**
         * The last entry that was returned (or -1 if we did not iterate or used
         * {@link java.util.Iterator#remove()}).
         */
        int curr = -1;
        /**
         * The current index (in the sense of a {@link java.util.ListIterator}).
         */
        int index = 0;
        
        int lowerLimit, upperLimit;
        MapIterator() {
                next = size == 0 ? -1 : order.items[0];
                index = 0;
                lowerLimit = 0;
                upperLimit = 0x7FFFFFFF;
            }
            MapIterator(int lower, int upper) {
                next = size == 0 ? -1 : order.items[0];
                index = 0;
                lowerLimit = lower;
                upperLimit = upper;
            }
        public boolean hasNext() {
            return next != -1;
        }
        public boolean hasPrevious() {
            return prev != -1;
        }
        private void ensureIndexKnown() {
            if (index >= lowerLimit)
                return;
            if (prev == -1) {
                index = lowerLimit;
                return;
            }
            if (next == -1) {
                index = Math.min(upperLimit, size);
                return;
            }
            index = lowerLimit;
        }
        public int nextIndex() {
            ensureIndexKnown();
            return index + 1;
        }
        public int previousIndex() {
            ensureIndexKnown();
            return index - 1;
        }
        public int nextEntry() {
            if (!hasNext())
                throw new NoSuchElementException();
            curr = next;
            if(++index >= order.size || index >= upperLimit)
                next = -1;
            else
                next = order.get(index);//(int) link[curr];
            prev = curr;
            return curr;
        }
        public int previousEntry() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            curr = prev;
            if(--index <= lowerLimit)
                prev = -1;
            else
                prev = order.get(index - 1);
            next = curr;
            return curr;
        }
        public void remove() {
            ensureIndexKnown();
            if (curr == -1)
                throw new IllegalStateException();
            if (curr == prev) {
                /*
                 * If the last operation was a next(), we are removing an entry
                 * that precedes the current index, and thus we must decrement
                 * it.
                 */
                if(--index > lowerLimit)
                    prev = order.get(index - 1); //(int) (link[curr] >>> 32);
                else
                    prev = -1;
            } else {
                if(index < order.size - 1 && index < upperLimit - 1)
                    next = order.get(index + 1);
                else
                    next = -1;
            }
            order.removeIndex(index);
            size--;
            int last, slot, pos = curr;
            curr = -1;
            if (pos == n) {
                containsNullKey = false;
                key[n] = null;
                value[n] = null;
            } else {
                K curr;
                final K[] key = OrganizedMap.this.key;
                // We have to horribly duplicate the shiftKeys() code because we
                // need to update next/prev.
                for (;;) {
                    pos = ((last = pos) + 1) & mask;
                    for (;;) {
                        if ((curr = key[pos]) == null) {
                            key[last] = null;
                            value[last] = null;
                            return;
                        }
                        slot = (hasher.hash(curr)) & mask;
                        if (last <= pos
                                ? last >= slot || slot > pos
                                : last >= slot && slot > pos)
                            break;
                        pos = (pos + 1) & mask;
                    }
                    key[last] = curr;
                    value[last] = value[pos];
                    if (next == pos)
                        next = last;
                    if (prev == pos)
                        prev = last;
                    fixOrder(pos, last);
                }
            }
        }
        public int skip(final int n) {
            int i = n;
            while (i-- != 0 && hasNext())
                nextEntry();
            return n - i - 1;
        }
        public int back(final int n) {
            int i = n;
            while (i-- != 0 && hasPrevious())
                previousEntry();
            return n - i - 1;
        }
    }
    private class EntryIterator extends MapIterator
            implements
            Iterator<Entry<K, V>>, Serializable {
        private static final long serialVersionUID = 0L;

        private MapEntry entry;
        public EntryIterator() {
        super();
        }

        EntryIterator(int lower, int upper) {
            super(lower, upper);
        }

        public MapEntry next() {
            return entry = new MapEntry(nextEntry());
        }
        public MapEntry previous() {
            return entry = new MapEntry(previousEntry());
        }
        @Override
        public void remove() {
            super.remove();
            entry.index = -1; // You cannot use a deleted entry.
        }
        public void set(Entry<K, V> ok) {
            throw new UnsupportedOperationException();
        }
        public void add(Entry<K, V> ok) {
            throw new UnsupportedOperationException();
        }
    }

    public final class MapEntrySet
            implements Cloneable, SortedSet<Entry<K, V>>, Set<Entry<K, V>>, Collection<Entry<K, V>>, Serializable {
        private static final long serialVersionUID = 0L;
        public int lowerLimit, upperLimit;
        public MapEntrySet()
        {
            lowerLimit = 0;
            upperLimit = 0x7FFFFFFF;
        }
        public MapEntrySet(int lower, int upper)
        {
            lowerLimit = lower;
            upperLimit = upper;
        }
        public EntryIterator iterator() {
            return new EntryIterator(lowerLimit, upperLimit);
        }
        public Comparator<? super Entry<K, V>> comparator() {
            return null;
        }
        public MapEntrySet subSet(
                Entry<K, V> fromElement,
                Entry<K, V> toElement) {
            return new MapEntrySet(OrganizedMap.this.indexOf(fromElement.getKey()),
                    OrganizedMap.this.indexOf(toElement.getKey()));
        }
        public MapEntrySet headSet(
                Entry<K, V> toElement) {
            return new MapEntrySet(0,
                    OrganizedMap.this.indexOf(toElement.getKey()));
        }
        public MapEntrySet tailSet(
                Entry<K, V> fromElement) {
            return new MapEntrySet(OrganizedMap.this.indexOf(fromElement.getKey()),
                    0x7FFFFFFF);
        }
        public Entry<K, V> first() {
            if (size <= lowerLimit)
                throw new NoSuchElementException();
            return new MapEntry(order.items[lowerLimit]);
        }
        public Entry<K, V> last() {
            if (size == 0)
                throw new NoSuchElementException();
            return new MapEntry(order.items[Math.min(order.size, upperLimit)-1]);
        }
        @SuppressWarnings("unchecked")
        public boolean contains(final Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            final K k = (K) e.getKey();
            final V v = (V) e.getValue();
            if (k == null)
                return containsNullKey
                        && (value[n] == null ? v == null : value[n]
                        .equals(v));
            K curr;
            final K[] key = OrganizedMap.this.key;
            int pos;
            // The starting point.
            if ((curr = key[pos = (hasher.hash(k)) & mask]) == null)
                return false;
            if (hasher.areEqual(k, curr))
                return value[pos] == null ? v == null : value[pos]
                        .equals(v);
            // There's always an unused entry.
            while (true) {
                if ((curr = key[pos = (pos + 1) & mask]) == null)
                    return false;
                if (hasher.areEqual(k, curr))
                    return value[pos] == null ? v == null : value[pos]
                            .equals(v);
            }
        }
        @SuppressWarnings("unchecked")
        public boolean remove(final Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            final K k = (K) e.getKey();
            final V v = (V) e.getValue();
            if (k == null) {
                if (containsNullKey
                        && (value[n] == null ? v == null : value[n]
                        .equals(v))) {
                    removeNullEntry();
                    return true;
                }
                return false;
            }
            K curr;
            final K[] key = OrganizedMap.this.key;
            int pos;
            // The starting point.
            if ((curr = key[pos = (hasher.hash(k)) & mask]) == null)
                return false;
            if (hasher.areEqual(curr, k)) {
                if (value[pos] == null ? v == null : value[pos]
                        .equals(v)) {
                    removeEntry(pos);
                    return true;
                }
                return false;
            }
            while (true) {
                if ((curr = key[pos = (pos + 1) & mask]) == null)
                    return false;
                if (hasher.areEqual(curr, k)) {
                    if (value[pos] == null ? v == null : value[pos]
                            .equals(v)) {
                        removeEntry(pos);
                        return true;
                    }
                }
            }
        }
        public int size() {
            return size;
        }
        public void clear() {
            OrganizedMap.this.clear();
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Set))
                return false;
            Set<?> s = (Set<?>) o;
            return s.size() == size() && containsAll(s);
        }

        public Object[] toArray() {
            final Object[] a = new Object[size()];
            objectUnwrap(iterator(), a);
            return a;
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            if (a == null || a.length < size()) a = (T[]) new Object[size()];
            objectUnwrap(iterator(), a);
            return a;
        }

        /**
         * Unsupported.
         *
         * @param c ignored
         * @return nothing, throws UnsupportedOperationException
         * @throws UnsupportedOperationException always
         */
        public boolean addAll(Collection<? extends Entry<K, V>> c) {
            throw new UnsupportedOperationException("addAll not supported");
        }

        /**
         * Unsupported.
         *
         * @param k ignored
         * @return nothing, throws UnsupportedOperationException
         * @throws UnsupportedOperationException always
         */
        public boolean add(Entry<K, V> k) {
            throw new UnsupportedOperationException("add not supported");
        }

        /**
         * Checks whether this collection contains all elements from the given
         * collection.
         *
         * @param c a collection.
         * @return <code>true</code> if this collection contains all elements of the
         * argument.
         */
        public boolean containsAll(Collection<?> c) {
            int n = c.size();
            final Iterator<?> i = c.iterator();
            while (n-- != 0)
                if (!contains(i.next()))
                    return false;
            return true;
        }

        /**
         * Retains in this collection only elements from the given collection.
         *
         * @param c a collection.
         * @return <code>true</code> if this collection changed as a result of the
         * call.
         */
        public boolean retainAll(Collection<?> c) {
            boolean retVal = false;
            int n = size();
            final Iterator<?> i = iterator();
            while (n-- != 0) {
                if (!c.contains(i.next())) {
                    i.remove();
                    retVal = true;
                }
            }
            return retVal;
        }

        /**
         * Remove from this collection all elements in the given collection. If the
         * collection is an instance of this class, it uses faster iterators.
         *
         * @param c a collection.
         * @return <code>true</code> if this collection changed as a result of the
         * call.
         */
        public boolean removeAll(Collection<?> c) {
            boolean retVal = false;
            int n = c.size();
            final Iterator<?> i = c.iterator();
            while (n-- != 0)
                if (remove(i.next()))
                    retVal = true;
            return retVal;
        }

        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public String toString() {
            final StringBuilder s = new StringBuilder();
            final EntryIterator i = iterator();
            int n = size();
            MapEntry k;
            boolean first = true;
            s.append("{");
            while (n-- != 0) {
                if (first)
                    first = false;
                else
                    s.append(", ");
                k = i.next();
                s.append(key[k.index] == OrganizedMap.this ? "(this collection)" : String.valueOf(key[k.index]))
                        .append("=>")
                        .append(value[k.index] == OrganizedMap.this ? "(this collection)" : String.valueOf(value[k.index]));
            }
            s.append("}");
            return s.toString();
        }

    }

    public SortedSet<Entry<K,V>> entrySet(int lower, int upper) {
        Coord c = Coord.get(lower, upper);
        MapEntrySet org = organizations.get(c);
        if (org == null) organizations.put(c, org = new MapEntrySet(lower, upper));
        return org;
    }

    /**
     * An iterator on keys.
     * <p>
     * <P>We simply override the {@link ListIterator#next()}/{@link ListIterator#previous()} methods (and possibly their type-specific counterparts) so that they return keys
     * instead of entries.
     */
    public class KeyIterator extends MapIterator implements Iterator<K>, Serializable {
        private static final long serialVersionUID = 0L;
        public K previous() {
            return key[previousEntry()];
        }
        public void set(K k) {
            throw new UnsupportedOperationException();
        }
        public void add(K k) {
            throw new UnsupportedOperationException();
        }
        public KeyIterator() {
            super();
        }
        public KeyIterator(int lower, int upper) {
            super(lower, upper);
        }
        public K next() {
            return key[nextEntry()];
        }
        public void remove() { super.remove(); }
    }

    public class KeySet implements SortedSet<K>, Serializable {
        private static final long serialVersionUID = 0L;

        public int lowerLimit, upperLimit;
        public KeySet(){
            lowerLimit = 0;
            upperLimit = 0x7FFFFFFF;
        }
        public KeySet(int lower, int upper)
        {
            lowerLimit = lower;
            upperLimit = upper;
        }
        public KeyIterator iterator() {
            return new KeyIterator(lowerLimit, upperLimit);
        }

        public int size() {
            return size;
        }

        public void clear() {
            OrganizedMap.this.clear();
        }

        public K first() {
            if (size <= lowerLimit) throw new NoSuchElementException();
            return key[order.items[lowerLimit]];
        }

        public K last() {
            if (size == 0) throw new NoSuchElementException();
            return key[order.items[Math.min(order.size, upperLimit)-1]];
        }

        public Comparator<K> comparator() {
            return null;
        }

        public KeySet tailSet(K from) {
            return new KeySet(OrganizedMap.this.indexOf(from), 0x7FFFFFFF);
        }

        public KeySet headSet(K to) {
            return new KeySet(0, OrganizedMap.this.indexOf(to));
        }

        public KeySet subSet(K from, K to) {
            return new KeySet(OrganizedMap.this.indexOf(from), OrganizedMap.this.indexOf(to));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            if (a == null || a.length < size()) a = (T[]) new Object[size()];
            unwrap(iterator(), a);
            return a;
        }

        /**
         * Always throws an UnsupportedOperationException
         */
        public boolean remove(Object ok) {
            throw new UnsupportedOperationException("Cannot remove from the key set directly");
        }

        /**
         * Always throws an UnsupportedOperationException
         */
        public boolean add(final K o) {
            throw new UnsupportedOperationException("Cannot add to the key set directly");
        }

        /**
         * Delegates to the corresponding type-specific method.
         */
        public boolean contains(final Object o) {
            return containsKey(o);
        }

        /**
         * Checks whether this collection contains all elements from the given type-specific collection.
         *
         * @param c a type-specific collection.
         * @return <code>true</code> if this collection contains all elements of the argument.
         */
        public boolean containsAll(Collection<?> c) {
            final Iterator<?> i = c.iterator();
            int n = c.size();
            while (n-- != 0)
                if (!contains(i.next())) return false;
            return true;
        }

        /**
         * Retains in this collection only elements from the given type-specific collection.
         *
         * @param c a type-specific collection.
         * @return <code>true</code> if this collection changed as a result of the call.
         */
        public boolean retainAll(Collection<?> c) {
            boolean retVal = false;
            int n = size();
            final Iterator<?> i = iterator();
            while (n-- != 0) {
                if (!c.contains(i.next())) {
                    i.remove();
                    retVal = true;
                }
            }
            return retVal;
        }

        /**
         * Remove from this collection all elements in the given type-specific collection.
         *
         * @param c a type-specific collection.
         * @return <code>true</code> if this collection changed as a result of the call.
         */
        public boolean removeAll(Collection<?> c) {
            boolean retVal = false;
            int n = c.size();
            final Iterator<?> i = c.iterator();
            while (n-- != 0)
                if (remove(i.next())) retVal = true;
            return retVal;
        }

        public Object[] toArray() {
            final Object[] a = new Object[size()];
            objectUnwrap(iterator(), a);
            return a;
        }

        /**
         * Adds all elements of the given collection to this collection.
         *
         * @param c a collection.
         * @return <code>true</code> if this collection changed as a result of the call.
         */
        public boolean addAll(Collection<? extends K> c) {
            boolean retVal = false;
            final Iterator<? extends K> i = c.iterator();
            int n = c.size();
            while (n-- != 0)
                if (add(i.next())) retVal = true;
            return retVal;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Set))
                return false;
            Set<?> s = (Set<?>) o;
            if (s.size() != size())
                return false;
            return containsAll(s);
        }
        /**
         * Unwraps an iterator into an array starting at a given offset for a given number of elements.
         * <p>
         * <P>This method iterates over the given type-specific iterator and stores the elements returned, up to a maximum of <code>length</code>, in the given array starting at <code>offset</code>. The
         * number of actually unwrapped elements is returned (it may be less than <code>max</code> if the iterator emits less than <code>max</code> elements).
         *
         * @param i      a type-specific iterator.
         * @param array  an array to contain the output of the iterator.
         * @param offset the first element of the array to be returned.
         * @param max    the maximum number of elements to unwrap.
         * @return the number of elements unwrapped.
         */
        public int unwrap(final KeyIterator i, final Object[] array, int offset, final int max) {
            if (max < 0) throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
            if (offset < 0 || offset + max > array.length) throw new IllegalArgumentException();
            int j = max;
            while (j-- != 0 && i.hasNext())
                array[offset++] = i.next();
            return max - j - 1;
        }

        /**
         * Unwraps an iterator into an array.
         * <p>
         * <P>This method iterates over the given type-specific iterator and stores the elements returned in the given array. The iteration will stop when the iterator has no more elements or when the end
         * of the array has been reached.
         *
         * @param i     a type-specific iterator.
         * @param array an array to contain the output of the iterator.
         * @return the number of elements unwrapped.
         */
        public int unwrap(final KeyIterator i, final Object[] array) {
            return unwrap(i, array, 0, array.length);
        }

        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public String toString() {
            final StringBuilder s = new StringBuilder();
            int n = size();
            boolean first = true;
            s.append("{");
            for (int i = 0; i < n; i++) {
                if (first) first = false;
                else s.append(", ");
                K k = keyAt(i);
                s.append(k == OrganizedMap.this ? "(this collection)" : String.valueOf(k));
            }
            s.append("}");
            return s.toString();
        }
    }

    public SortedSet<K> keySet(int lower, int upper) {
        Coord c = Coord.get(lower, upper);
        KeySet org = organizedKeys.get(c);
        if (org == null) organizedKeys.put(c, org = new KeySet(lower, upper));
        return org;
    }

    /**
     * An iterator on values.
     * <p>
     * <P>We simply override the {@link ListIterator#next()}/{@link ListIterator#previous()} methods (and possibly their type-specific counterparts) so that they return values
     * instead of entries.
     */
    public class ValueIterator extends MapIterator implements ListIterator<V>, Serializable {
        private static final long serialVersionUID = 0L;

        public V previous() {
            return value[previousEntry()];
        }
        public void set(V v) {
            throw new UnsupportedOperationException();
        }
        public void add(V v) {
            throw new UnsupportedOperationException();
        }
        public ValueIterator() {
            super();
        }
        public ValueIterator(int lower, int upper) {
            super(lower, upper);
        }
        public V next() {
            return value[nextEntry()];
        }
        public void remove() { super.remove(); }
    }
    public class ValueCollection extends AbstractCollection<V> implements Serializable
    {
        private static final long serialVersionUID = 0L;
        public int lowerLimit, upperLimit;

        public ValueCollection() {
            super();
            lowerLimit = 0;
            upperLimit = 0x7FFFFFFF;
        }

        public ValueCollection(int lower, int upper) {
            super();
            lowerLimit = lower;
            upperLimit = upper;
        }

        public ValueIterator iterator() {
            return new ValueIterator(lowerLimit, upperLimit);
        }
        public int size() {
            return size;
        }
        public boolean contains(Object v) {
            return containsValue(v);
        }
        public void clear() {
            OrganizedMap.this.clear();
        }
    }
    public Collection<V> values(int lower, int upper) {
        Coord c = Coord.get(lower, upper);
        ValueCollection org = organizedValues.get(c);
        if (org == null) organizedValues.put(c, org = new ValueCollection(lower, upper));
        return org;
    }
    
    public void reorganize(K key, Coord target)
    {
        final int idx = indexOf(key);
        int s = organizedKeys.size;
        Coord c;
        for (int i = 0; i < s; i++) {
            if((c = organizedKeys.keyAt(i)) == target)
                return;
        }
        // this is going to need a lot of work and may be quite error-prone... index ranges probably aren't good here.
    }
}
