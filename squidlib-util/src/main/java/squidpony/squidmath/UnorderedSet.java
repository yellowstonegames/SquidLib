/*
 * Copyright (C) 2002-2015 Sebastiano Vigna
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package squidpony.squidmath;

import squidpony.annotation.GwtIncompatible;

import java.util.*;

/**
 * A generic unordered hash set with with a fast implementation, based on {@link OrderedSet} in this library, which is
 * based on the fastutil library's ObjectLinkedOpenHashSet class; the ordering and indexed access have been removed to
 * potentially reduce the time cost of insertion and removal at the expense of increasing time cost for access by index.
 * This does support optional hash strategies for array (and other) keys, which fastutil's collections can do in a
 * different way, and has better support than {@link HashSet} for construction with an array of items or construction
 * with a Collection of items (this also helps {@link #addAll(Object[])}).
 * <p>
 * Instances of this class use a hash table to represent a set. The table is
 * filled up to a specified <em>load factor</em>, and then doubled in size to
 * accommodate new entries. If the table is emptied below <em>one fourth</em> of
 * the load factor, it is halved in size. However, halving is not performed when
 * deleting entries from an iterator, as it would interfere with the iteration
 * process.
 * </p>
 * <p>
 * Note that {@link #clear()} does not modify the hash table size. Rather, a
 * family of {@linkplain #trim() trimming methods} lets you control the size of
 * the table; this is particularly useful if you reuse instances of this class.
 * </p>
 * <p>
 * This class implements the interface of a Set, not a SortedSet.
 * <p>
 * <p>
 * You can pass an {@link CrossHash.IHasher} instance such as {@link CrossHash#generalHasher} as an extra parameter to
 * most of this class' constructors, which allows the OrderedSet to use arrays (usually primitive arrays) as items. If
 * you expect only one type of array, you can use an instance like {@link CrossHash#intHasher} to hash int arrays, or
 * the aforementioned generalHasher to hash most kinds of arrays (it can't handle most multi-dimensional arrays well).
 * If you aren't using array items, you don't need to give an IHasher to the constructor and can ignore this feature.
 * </p>
 * <br>
 * Thank you, Sebastiano Vigna, for making FastUtil available to the public with such high quality.
 * <br>
 * See https://github.com/vigna/fastutil for the original library.
 *
 * @author Sebastiano Vigna (responsible for all the hard parts)
 * @author Tommy Ettinger (mostly responsible for squashing several layers of parent classes into one monster class)
 */
public class UnorderedSet<K> implements Set<K>, java.io.Serializable, Cloneable {
    private static final long serialVersionUID = 0L;
    /**
     * The array of keys.
     */
    protected K[] key;
    /**
     * The array of values.
     */
    //protected V[] value;
    /**
     * The mask for wrapping a position counter.
     */
    protected int mask;
    /**
     * Whether this set contains the key zero.
     */
    protected boolean containsNull;
    /**
     * The current table size.
     */
    protected int n;
    /**
     * Threshold after which we rehash. It must be the table size times {@link #f}.
     */
    protected int maxFill;
    /**
     * Number of entries in the set (including the key zero, if present).
     */
    protected int size;
    /**
     * The acceptable load factor.
     */
    public final float f;

    /**
     * The initial default size of a hash table.
     */
    public static final int DEFAULT_INITIAL_SIZE = 16;
    /**
     * The default load factor of a hash table.
     */
    public static final float DEFAULT_LOAD_FACTOR = .375f; // .1875f; // .75f;
    /**
     * The load factor for a (usually small) table that is meant to be particularly fast.
     */
    public static final float FAST_LOAD_FACTOR = .5f;
    /**
     * The load factor for a (usually very small) table that is meant to be extremely fast.
     */
    public static final float VERY_FAST_LOAD_FACTOR = .25f;

    protected CrossHash.IHasher hasher = null;

    /**
     * Creates a new hash map.
     * <p>
     * <p>The actual table size will be the least power of two greater than <code>expected</code>/<code>f</code>.
     *
     * @param expected the expected number of elements in the hash set.
     * @param f        the load factor.
     */

    @SuppressWarnings("unchecked")
    public UnorderedSet(final int expected, final float f) {
        if (f <= 0 || f > 1)
            throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than or equal to 1");
        if (expected < 0) throw new IllegalArgumentException("The expected number of elements must be nonnegative");
        this.f = f;
        n = arraySize(expected, f);
        mask = n - 1;
        maxFill = maxFill(n, f);
        key = (K[]) new Object[n + 1];
        hasher = CrossHash.defaultHasher;
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor.
     *
     * @param expected the expected number of elements in the hash set.
     */
    public UnorderedSet(final int expected) {
        this(expected, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new hash set with initial expected
     * {@link #DEFAULT_INITIAL_SIZE} elements and
     * {@link #DEFAULT_LOAD_FACTOR} as load factor.
     */
    public UnorderedSet() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new hash set copying a given collection.
     *
     * @param c a {@link Collection} to be copied into the new hash set.
     * @param f the load factor.
     */
    public UnorderedSet(final Collection<? extends K> c,
                        final float f) {
        this(c.size(), f, (c instanceof UnorderedSet) ? ((UnorderedSet) c).hasher : CrossHash.defaultHasher);
        addAll(c);
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor copying a given collection.
     *
     * @param c a {@link Collection} to be copied into the new hash set.
     */
    public UnorderedSet(final Collection<? extends K> c) {
        this(c, (c instanceof UnorderedSet) ? ((UnorderedSet) c).f : DEFAULT_LOAD_FACTOR, (c instanceof UnorderedSet) ? ((UnorderedSet) c).hasher : CrossHash.defaultHasher);
    }

    /**
     * Creates a new hash set using elements provided by a type-specific
     * iterator.
     *
     * @param i a type-specific iterator whose elements will fill the set.
     * @param f the load factor.
     */
    public UnorderedSet(final Iterator<? extends K> i, final float f) {
        this(DEFAULT_INITIAL_SIZE, f);
        while (i.hasNext())
            add(i.next());
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor using elements provided by a type-specific iterator.
     *
     * @param i a type-specific iterator whose elements will fill the set.
     */
    public UnorderedSet(final Iterator<? extends K> i) {
        this(i, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new hash set and fills it with the elements of a given array.
     *
     * @param a      an array whose elements will be used to fill the set.
     * @param offset the first element to use.
     * @param length the number of elements to use.
     * @param f      the load factor.
     */
    public UnorderedSet(final K[] a, final int offset,
                        final int length, final float f) {
        this(length < 0 ? 0 : length, f);
        if (a == null) throw new NullPointerException("Array passed to OrderedSet constructor cannot be null");
        if (offset < 0) throw new ArrayIndexOutOfBoundsException("Offset (" + offset + ") is negative");
        if (length < 0) throw new IllegalArgumentException("Length (" + length + ") is negative");
        if (offset + length > a.length) {
            throw new ArrayIndexOutOfBoundsException(
                    "Last index (" + (offset + length) + ") is greater than array length (" + a.length + ")");
        }
        for (int i = 0; i < length; i++)
            add(a[offset + i]);
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor and fills it with the elements of a given array.
     *
     * @param a      an array whose elements will be used to fill the set.
     * @param offset the first element to use.
     * @param length the number of elements to use.
     */
    public UnorderedSet(final K[] a, final int offset,
                        final int length) {
        this(a, offset, length, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new hash set copying the elements of an array.
     *
     * @param a an array to be copied into the new hash set.
     * @param f the load factor.
     */
    public UnorderedSet(final K[] a, final float f) {
        this(a, 0, a.length, f);
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor copying the elements of an array.
     *
     * @param a an array to be copied into the new hash set.
     */
    public UnorderedSet(final K[] a) {
        this(a, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new hash map.
     * <p>
     * <p>The actual table size will be the least power of two greater than <code>expected</code>/<code>f</code>.
     *
     * @param expected the expected number of elements in the hash set.
     * @param f        the load factor.
     * @param hasher   used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    @SuppressWarnings("unchecked")
    public UnorderedSet(final int expected, final float f, CrossHash.IHasher hasher) {
        if (f <= 0 || f > 1)
            throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than or equal to 1");
        if (expected < 0) throw new IllegalArgumentException("The expected number of elements must be nonnegative");
        this.f = f;
        n = arraySize(expected, f);
        mask = n - 1;
        maxFill = maxFill(n, f);
        key = (K[]) new Object[n + 1];
        this.hasher = hasher == null ? CrossHash.defaultHasher : hasher;
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor.
     *
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    public UnorderedSet(CrossHash.IHasher hasher) {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR, hasher);
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor.
     *
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    public UnorderedSet(final int expected, CrossHash.IHasher hasher) {
        this(expected, DEFAULT_LOAD_FACTOR, hasher);
    }

    /**
     * Creates a new hash set copying a given collection.
     *
     * @param c      a {@link Collection} to be copied into the new hash set.
     * @param f      the load factor.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    public UnorderedSet(final Collection<? extends K> c,
                        final float f, CrossHash.IHasher hasher) {
        this(c.size(), f, hasher);
        addAll(c);
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor copying a given collection.
     *
     * @param c      a {@link Collection} to be copied into the new hash set.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    public UnorderedSet(final Collection<? extends K> c, CrossHash.IHasher hasher) {
        this(c, DEFAULT_LOAD_FACTOR, hasher);
    }

    /**
     * Creates a new hash set and fills it with the elements of a given array.
     *
     * @param a      an array whose elements will be used to fill the set.
     * @param offset the first element to use.
     * @param length the number of elements to use.
     * @param f      the load factor.
     */
    public UnorderedSet(final K[] a, final int offset,
                        final int length, final float f, CrossHash.IHasher hasher) {
        this(length < 0 ? 0 : length, f, hasher);
        if (a == null) throw new NullPointerException("Array passed to OrderedSet constructor cannot be null");
        if (offset < 0) throw new ArrayIndexOutOfBoundsException("Offset (" + offset + ") is negative");
        if (length < 0) throw new IllegalArgumentException("Length (" + length + ") is negative");
        if (offset + length > a.length) {
            throw new ArrayIndexOutOfBoundsException(
                    "Last index (" + (offset + length) + ") is greater than array length (" + a.length + ")");
        }
        for (int i = 0; i < length; i++)
            add(a[offset + i]);
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor and fills it with the elements of a given array.
     *
     * @param a      an array whose elements will be used to fill the set.
     * @param offset the first element to use.
     * @param length the number of elements to use.
     */
    public UnorderedSet(final K[] a, final int offset,
                        final int length, CrossHash.IHasher hasher) {
        this(a, offset, length, DEFAULT_LOAD_FACTOR, hasher);
    }

    /**
     * Creates a new hash set copying the elements of an array.
     *
     * @param a an array to be copied into the new hash set.
     * @param f the load factor.
     */
    public UnorderedSet(final K[] a, final float f, CrossHash.IHasher hasher) {
        this(a, 0, a.length, f, hasher);
    }

    /**
     * Creates a new hash set with {@link #DEFAULT_LOAD_FACTOR} as load
     * factor copying the elements of an array.
     *
     * @param a an array to be copied into the new hash set.
     */
    public UnorderedSet(final K[] a, CrossHash.IHasher hasher) {
        this(a, DEFAULT_LOAD_FACTOR, hasher);
    }

    private int realSize() {
        return containsNull ? size - 1 : size;
    }

    private void ensureCapacity(final int capacity) {
        final int needed = arraySize(capacity, f);
        if (needed > n)
            rehash(needed);
    }

    private void tryCapacity(final long capacity) {
        final int needed = (int) Math.min(
                1 << 30,
                Math.max(2, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity
                        / f))));
        if (needed > n)
            rehash(needed);
    }

    public boolean addAll(Collection<? extends K> c) {
        int n = c.size();
        // The resulting collection will be at least c.size() big
        if (f <= .5)
            ensureCapacity(n); // The resulting collection will be sized
            // for c.size() elements
        else
            tryCapacity(size + n); // The resulting collection will be
        // tentatively sized for size() + c.size() elements
        boolean retVal = false;
        final Iterator<? extends K> i = c.iterator();
        while (n-- != 0)
            if (add(i.next()))
                retVal = true;
        return retVal;
    }

    public boolean addAll(K[] a) {
        if(a == null)
            return false;
        int n = a.length;
        // The resulting collection will be at least a.length big
        if (f <= .5)
            ensureCapacity(n); // The resulting collection will be sized
            // for a.length elements
        else
            tryCapacity(size + n); // The resulting collection will be
        // tentatively sized for size() + a.length elements
        boolean retVal = false;
        for (int i = 0; i < n; i++) {
            if(add(a[i]))
                retVal = true;
        }
        return retVal;
    }


    public boolean add(final K k) {
        int pos;
        if (k == null) {
            if (containsNull)
                return false;
            containsNull = true;
        } else {
            K curr;
            final K[] key = this.key;
            // The starting point.
            if (!((curr = key[pos = HashCommon.mix(hasher.hash(k)) & mask]) == null)) {
                if (hasher.areEqual(curr, k))
                    return false;
                while (!((curr = key[pos = pos + 1 & mask]) == null))
                    if (hasher.areEqual(curr, k))
                        return false;
            }
            key[pos] = k;
        }
        if (size++ >= maxFill)
            rehash(arraySize(size + 1, f));
        return true;
    }

    /**
     * Add a random element if not present, get the existing value if already
     * present.
     * <p>
     * This is equivalent to (but faster than) doing a:
     * <p>
     * <pre>
     * K exist = set.get(k);
     * if (exist == null) {
     * 	set.add(k);
     * 	exist = k;
     * }
     * </pre>
     */
    public K addOrGet(final K k) {
        int pos;
        if (k == null) {
            if (containsNull)
                return key[n];
            containsNull = true;
        } else {
            K curr;
            final K[] key = this.key;
            // The starting point.
            if (!((curr = key[pos = HashCommon.mix(hasher.hash(k)) & mask]) == null)) {
                if (hasher.areEqual(curr, k))
                    return curr;
                while (!((curr = key[pos = pos + 1 & mask]) == null))
                    if (hasher.areEqual(curr, k))
                        return curr;
            }
            key[pos] = k;
        }
        if (size++ >= maxFill)
            rehash(arraySize(size + 1, f));
        return k;
    }

    /**
     * Shifts left entries with the specified hash code, starting at the
     * specified position, and empties the resulting free entry.
     *
     * @param pos a starting position.
     */
    protected final void shiftKeys(int pos) {
        // Shift entries with the same hash.
        int last, slot;
        K curr;
        final K[] key = this.key;
        for (; ; ) {
            pos = (last = pos) + 1 & mask;
            for (; ; ) {
                if ((curr = key[pos]) == null) {
                    key[last] = null;
                    return;
                }
                slot = HashCommon.mix(hasher.hash(curr))
                        & mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot
                        && slot > pos)
                    break;
                pos = pos + 1 & mask;
            }
            key[last] = curr;
        }
    }

    private boolean removeEntry(final int pos) {
        size--;
        shiftKeys(pos);
        if (size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE)
            rehash(n / 2);
        return true;
    }

    private boolean removeNullEntry() {
        containsNull = false;
        key[n] = null;
        size--;
        if (size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE)
            rehash(n / 2);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(final Object k) {
        if (k == null)
            return containsNull && removeNullEntry();
        K curr;
        final K[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = HashCommon.mix(hasher.hash(k)) & mask]) == null)
            return false;
        if (hasher.areEqual(k, curr))
            return removeEntry(pos);
        while (true) {
            if ((curr = key[pos = pos + 1 & mask]) == null)
                return false;
            if (hasher.areEqual(k, curr))
                return removeEntry(pos);
        }
    }

    /**
     * Returns the element of this set that is equal to the given key, or
     * <code>null</code>.
     *
     * @return the element of this set that is equal to the given key, or
     * <code>null</code>.
     */
    public K get(final Object k) {
        if (k == null)
            return key[n]; // This is correct independently of the value of
        // containsNull and of the map being custom
        K curr;
        final K[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = HashCommon.mix(hasher.hash(k)) & mask]) == null)
            return null;
        if (hasher.areEqual(k, curr))
            return curr;
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = pos + 1 & mask]) == null)
                return null;
            if (hasher.areEqual(k, curr))
                return curr;
        }
    }

    public boolean contains(final Object k) {
        if (k == null)
            return containsNull;
        K curr;
        final K[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = HashCommon.mix(hasher.hash(k)) & mask]) == null)
            return false;
        if (hasher.areEqual(k, curr))
            return true;
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = pos + 1 & mask]) == null)
                return false;
            if (hasher.areEqual(k, curr))
                return true;
        }
    }

    protected int positionOf(final Object k) {
        if (k == null)
        {
            if(containsNull)
                return n;
            else
                return -1;
        }
        K curr;
        final K[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = HashCommon.mix(hasher.hash(k)) & mask]) == null)
            return -1;
        if (hasher.areEqual(k, curr))
            return pos;
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = pos + 1 & mask]) == null)
                return -1;
            if (hasher.areEqual(k, curr))
                return pos;
        }
    }

    /*
     * Removes all elements from this set.
     *
     * <P>To increase object reuse, this method does not change the table size.
     * If you want to reduce the table size, you must use {@link #trim()}.
     */
    public void clear() {
        if (size == 0)
            return;
        size = 0;
        containsNull = false;
        Arrays.fill(key, null);
    }

    public int size() {
        return size;
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
        int n = size;
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
        return size == 0;
    }


    /*
     * A list iterator over a set.
     * <p>
     * <p>
     * This class provides a list iterator over a hash set. The
     * constructor takes constant time.
     */
    private class SetIterator implements Iterator <K> {
        /** The index of the last entry returned, if positive or zero; initially, {@link #n}. If negative, the last
         element returned was that of index {@code - pos - 1} from the {@link #wrapped} list. */
        int pos = n;
        /** The index of the last entry that has been returned (more precisely, the value of {@link #pos} if {@link #pos} is positive,
         or {@link Integer#MIN_VALUE} if {@link #pos} is negative). It is -1 if either
         we did not return an entry yet, or the last returned entry has been removed. */
        int last = -1;
        /** A downward counter measuring how many entries must still be returned. */
        int c = size;
        /** A boolean telling us whether we should return the null key. */
        boolean mustReturnNull = UnorderedSet.this.containsNull;
        /** A lazily allocated list containing elements that have wrapped around the table because of removals. */
        ArrayList <K> wrapped;
        public boolean hasNext() {
            return c != 0;
        }
        public K next() {
            if ( ! hasNext() ) throw new NoSuchElementException();
            c--;
            if ( mustReturnNull ) {
                mustReturnNull = false;
                last = n;
                return key[ n ];
            }
            final K key[] = UnorderedSet.this.key;
            for(;;) {
                if ( --pos < 0 ) {
                    // We are just enumerating elements from the wrapped list.
                    last = Integer.MIN_VALUE;
                    return wrapped.get( - pos - 1 );
                }
                if ( ! ( (key[ pos ]) == null ) ) return key[ last = pos ];
            }
        }
        /** Shifts left entries with the specified hash code, starting at the specified position,
         * and empties the resulting free entry.
         *
         * @param pos a starting position.
         */
        private final void shiftKeys( int pos ) {
            // Shift entries with the same hash.
            int last, slot;
            K current;
            final K[] key = UnorderedSet.this.key;
            for(;;) {
                pos = ( ( last = pos ) + 1 ) & mask;
                for(;;) {
                    if ( ( (current = key[ pos ]) == null ) ) {
                        key[ last ] = (null);
                        return;
                    }
                    slot = ( HashCommon.mix(hasher.hash(current)) ) & mask;
                    if ( last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos ) break;
                    pos = ( pos + 1 ) & mask;
                }
                if ( pos < last ) { // Wrapped entry.
                    if ( wrapped == null ) wrapped = new ArrayList<K>( 2 );
                    wrapped.add( key[ pos ] );
                }
                key[ last ] = current;
            }
        }
        public void remove() {
            if ( last == -1 ) throw new IllegalStateException();
            if ( last == n ) {
                UnorderedSet.this.containsNull = false;
                UnorderedSet.this.key[ n ] = (null);
            }
            else if ( pos >= 0 ) shiftKeys( last );
            else {
                // We're removing wrapped entries.
                UnorderedSet.this.remove( wrapped.set( - pos - 1, null ) );
                last = -1; // Note that we must not decrement size
                return;
            }
            size--;
            last = -1; // You can no longer remove this entry.
        }
        /** This method just iterates the type-specific version of {@link #next()} for at most
         * <code>n</code> times, stopping if {@link #hasNext()} becomes false.*/
        public int skip( final int n ) {
            int i = n;
            while( i-- != 0 && hasNext() ) next();
            return n - i - 1;
        }

    }

    public Iterator<K> iterator() {
        return new SetIterator();
    }

    /**
     * Rehashes the map, making the table as small as possible.
     * <p>
     * <P>This method rehashes the table to the smallest size satisfying the load factor. It can be used when the set will not be changed anymore, so to optimize access speed and size.
     * <p>
     * <P>If the table size is already the minimum possible, this method does nothing.
     *
     * @return true if there was enough memory to trim the map.
     * @see #trim(int)
     */
    public boolean trim() {
        final int l = arraySize(size, f);
        if (l >= n || size > maxFill(l, f)) return true;
        try {
            rehash(l);
        } catch (Exception cantDoIt) {
            return false;
        }
        return true;
    }

    /**
     * Rehashes this map if the table is too large.
     * <p>
     * <P>Let <var>N</var> be the smallest table size that can hold <code>max(n,{@link #size()})</code> entries, still satisfying the load factor. If the current table size is smaller than or equal to
     * <var>N</var>, this method does nothing. Otherwise, it rehashes this map in a table of size <var>N</var>.
     * <p>
     * <P>This method is useful when reusing maps. {@linkplain #clear() Clearing a map} leaves the table size untouched. If you are reusing a map many times, you can call this method with a typical
     * size to avoid keeping around a very large table just because of a few large transient maps.
     *
     * @param n the threshold for the trimming.
     * @return true if there was enough memory to trim the map.
     * @see #trim()
     */
    public boolean trim(final int n) {
        final int l = HashCommon.nextPowerOfTwo((int) Math.ceil(n / f));
        if (l >= n || size > maxFill(l, f)) return true;
        try {
            rehash(l);
        } catch (Exception cantDoIt) {
            return false;
        }
        return true;
    }

    /**
     * Given a hash position, this finds the next position that contains an item, or -1 if none remain.
     * To scan from the start, give this -1 for position.
     * @param position a hash-based masked position in the K array
     * @return the next position after the given one
     */
    private int scanNext(int position)
    {
        int h = position;
        while (++h < n)
        {
            if(key[h] != null)
            {
                return h;
            }
        }
        if(containsNull)
            return n;
        return -1;
    }

    /**
     * Rehashes the map.
     * <p>
     * <p>
     * This method implements the basic rehashing strategy, and may be overriden
     * by subclasses implementing different rehashing strategies (e.g.,
     * disk-based rehashing). However, you should not override this method
     * unless you understand the internal workings of this class.
     *
     * @param newN the new size
     */
    @SuppressWarnings("unchecked")
    protected void rehash(final int newN) {
        final K key[] = this.key;
        final int mask = newN - 1; // Note that this is used by the hashing
        // macro
        final K newKey[] = (K[]) new Object[newN + 1];
        int i = -1, pos, sz = size;
        for (int q = 0; q < sz; q++) {
            i = scanNext(i);
            if (key[i] == null)
                pos = newN;
            else {
                pos = HashCommon.mix(hasher.hash(key[i])) & mask;
                while (!(newKey[pos] == null))
                    pos = pos + 1 & mask;
            }
            newKey[pos] = key[i];
        }
        n = newN;
        this.mask = mask;
        maxFill = maxFill(n, f);
        this.key = newKey;
    }

    /**
     * Returns a deep copy of this map.
     * <p>
     * <p>
     * This method performs a deep copy of this hash map; the data stored in the
     * map, however, is not cloned. Note that this makes a difference only for
     * object keys.
     *
     * @return a deep copy of this map.
     */
    @SuppressWarnings("unchecked")
    @GwtIncompatible
    public Object clone() {
        UnorderedSet<K> c;
        try {
            c = (UnorderedSet<K>) super.clone();
            c.key = (K[]) new Object[n + 1];
            System.arraycopy(key, 0, c.key, 0, n + 1);
            c.hasher = hasher;
            return c;
        } catch (Exception cantHappen) {
            throw new UnsupportedOperationException(cantHappen + (cantHappen.getMessage() != null ?
                    "; " + cantHappen.getMessage() : ""));
        }
    }

    /**
     * Returns a hash code for this set.
     * <p>
     * This method overrides the generic method provided by the superclass.
     * Since <code>equals()</code> is not overriden, it is important that the
     * value returned by this method is the same value as the one returned by
     * the overriden method.
     *
     * @return a hash code for this set.
     */
    public int hashCode() {
        int h = 0;
        for (int j = realSize(), i = 0; j-- != 0; ) {
            while (key[i] == null)
                i++;
            if (this != key[i])
                h += hasher.hash(key[i]);
            i++;
        }
        // Zero / null have hash zero.
        return h;
    }

    public long hash64()
    {
        return 31L * size + CrossHash.hash64(key);
    }

    /**
     * Returns the maximum number of entries that can be filled before rehashing.
     *
     * @param n the size of the backing array.
     * @param f the load factor.
     * @return the maximum number of entries before rehashing.
     */
    public static int maxFill(final int n, final float f) {
        /* We must guarantee that there is always at least
		 * one free entry (even with pathological load factors). */
        return Math.min((int) Math.ceil(n * f), n - 1);
    }

    /**
     * Returns the maximum number of entries that can be filled before rehashing.
     *
     * @param n the size of the backing array.
     * @param f the load factor.
     * @return the maximum number of entries before rehashing.
     */
    public static long maxFill(final long n, final float f) {
		/* We must guarantee that there is always at least
		 * one free entry (even with pathological load factors). */
        return Math.min((long) Math.ceil(n * f), n - 1);
    }

    /**
     * Returns the least power of two smaller than or equal to 2<sup>30</sup> and larger than or equal to <code>Math.ceil( expected / f )</code>.
     *
     * @param expected the expected number of elements in a hash table.
     * @param f        the load factor.
     * @return the minimum possible size for a backing array.
     * @throws IllegalArgumentException if the necessary size is larger than 2<sup>30</sup>.
     */
    public static int arraySize(final int expected, final float f) {
        final long s = Math.max(2, HashCommon.nextPowerOfTwo((long) Math.ceil(expected / f)));
        if (s > 1 << 30)
            throw new IllegalArgumentException("Too large (" + expected + " expected elements with load factor " + f + ")");
        return (int) s;
    }

    @Override
    public Object[] toArray() {
        final Object[] a = new Object[size];
        objectUnwrap(iterator(), a);
        return a;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        final int size = this.size;
        objectUnwrap(iterator(), a);
        if (size < a.length)
            a[size] = null;
        return a;
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
    private static <K> int objectUnwrap(final Iterator<? extends K> i, final K array[], int offset, final int max) {
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
    private static <K> int objectUnwrap(final Iterator<? extends K> i, final K array[]) {
        return objectUnwrap(i, array, 0, array.length);
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        int i = scanNext(-1);
        boolean first = true;
        s.append("OrderedSet{");
        while (i != -1) {
            if (first) first = false;
            else s.append(", ");
            s.append(key[i]);
            i = scanNext(i);
        }
        s.append("}");
        return s.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Set))
            return false;
        Set<?> s = (Set<?>) o;
        if (s.size() != size)
            return false;
        return containsAll(s);
    }

    @GwtIncompatible
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        final Iterator<K> i = iterator();
        s.defaultWriteObject();
        s.writeObject(hasher);
        for (int j = size; j-- != 0; )
            s.writeObject(i.next());
    }

    @GwtIncompatible
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        hasher = (CrossHash.IHasher) s.readObject();
        n = arraySize(size, f);
        maxFill = maxFill(n, f);
        mask = n - 1;
        final K key[] = this.key = (K[]) new Object[n + 1];
        K k;
        for (int i = size, pos; i-- != 0; ) {
            k = (K) s.readObject();
            if (k == null) {
                pos = n;
                containsNull = true;
            } else {
                if (!(key[pos = HashCommon.mix(hasher.hash(k)) & mask] == null))
                    while (!(key[pos = pos + 1 & mask] == null)) ;
            }
            key[pos] = k;
        }
    }
}
