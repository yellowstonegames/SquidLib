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

import java.io.Serializable;
import java.util.*;

/**
 * A generic unordered hash map with with a fast implementation, originally from fastutil as
 * Object2ObjectOpenCustomHashMap but modified to support SquidLib's {@link squidpony.squidmath.CrossHash.IHasher}
 * interface for custom hashing instead of fastutil's Strategy interface.
 * <br>
 * Instances of this class use a hash table to represent a map. The table is filled up to a specified <em>load factor</em>, and then doubled in size to accommodate new entries. If the table is
 * emptied below <em>one fourth</em> of the load factor, it is halved in size. However, halving is not performed when deleting entries from an iterator, as it would interfere with the iteration
 * process.
 * <br>
 * Note that {@link #clear()} does not modify the hash table size. Rather, a family of {@linkplain #trim() trimming methods} lets you control the size of the table; this is particularly useful if
 * you reuse instances of this class.
 * <br>
 * You can pass a {@link CrossHash.IHasher} instance such as {@link CrossHash#generalHasher} as an extra parameter to
 * most of this class' constructors, which allows the OrderedMap to use arrays (usually primitive arrays) as keys. If
 * you expect only one type of array, you can use an instance like {@link CrossHash#intHasher} to hash int arrays, or
 * the aforementioned generalHasher to hash most kinds of arrays (it can't handle most multi-dimensional arrays well).
 * If you aren't using arrays as keys, you don't need to give an IHasher to the constructor and can ignore this feature
 * most of the time. However, the default IHasher this uses if none is specified performs a small but significant
 * "mixing" step to make the default generated hashCode() implementation many classes use into a higher-quality
 * random-like value. This isn't always optimal; if you plan to insert 1000 sequential Integer keys with some small
 * amount of random Integers after them, then the mixing actually increases the likelihood of a collision and takes time
 * to calculate. You could use a very simple IHasher in that case, relying on the fact that only Integers will be added:
 * <pre>
 * new CrossHash.IHasher() {
 *     public int hash(Object data) { return (int)data; }
 *     public boolean areEqual(Object left, Object right) { return Objects.equals(left, right); }
 * };
 * </pre>
 * This is just one example of a case where a custom IHasher can be useful for performance reasons; there are also cases
 * where an IHasher is needed to enforce hashing by identity or by value, which affect program logic. Note that the
 * given IHasher is likely to be sub-optimal for many situations with Integer keys, and you may want to try a few
 * different approaches if you know OrderedMap is a bottleneck in your application. If the IHasher is a performance
 * problem, it will be at its worst if the OrderedMap needs to resize, and thus rehash, many times; this won't happen if
 * the capacity is set correctly when the OrderedMap is created (with the capacity equal to or greater than the maximum
 * number of entries that will be added).
 * <br>
 * Thank you, Sebastiano Vigna, for making FastUtil available to the public with such high quality.
 * <br>
 * See https://github.com/vigna/fastutil for the original library.
 * @author Sebastiano Vigna (responsible for all the hard parts)
 * @author Tommy Ettinger (mostly responsible for squashing several layers of parent classes into one monster class)
 */
public class UnorderedMap<K, V> implements Map<K, V>, Serializable, Cloneable {
    private static final long serialVersionUID = 0L;
    /**
     * The array of keys.
     */
    protected K[] key;
    /**
     * The array of values.
     */
    protected V[] value;
    /**
     * The mask for wrapping a position counter.
     */
    protected int mask;
    /**
     * Whether this set contains the key zero.
     */
    protected boolean containsNullKey;
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
     * Cached set of entries.
     */
    protected volatile MapEntrySet entries;
    /**
     * Cached set of keys.
     */
    protected volatile KeySet keys;
    /**
     * Cached collection of values.
     */
    protected volatile Collection<V> values;
    /**
     * Default return value.
     */
    protected V defRetValue = null;

    /**
     * The initial default size of a hash table.
     */
    public static final int DEFAULT_INITIAL_SIZE = 16;
    /**
     * The default load factor of a hash table.
     */
    public static final float DEFAULT_LOAD_FACTOR = .75f; // .1875f; // .75f;
    /**
     * The load factor for a (usually small) table that is meant to be particularly fast.
     */
    public static final float FAST_LOAD_FACTOR = .5f;
    /**
     * The load factor for a (usually very small) table that is meant to be extremely fast.
     */
    public static final float VERY_FAST_LOAD_FACTOR = .25f;

    protected CrossHash.IHasher hasher = null;

    public void defaultReturnValue(final V rv) {
        defRetValue = rv;
    }

    public V defaultReturnValue() {
        return defRetValue;
    }

    /**
     * Creates a new OrderedMap.
     * <p>
     * <p>The actual table size will be the least power of two greater than <code>expected</code>/<code>f</code>.
     *
     * @param expected the expected number of elements in the hash set.
     * @param f        the load factor.
     */

    @SuppressWarnings("unchecked")
    public UnorderedMap(final int expected, final float f) {
        if (f <= 0 || f > 1)
            throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than or equal to 1");
        if (expected < 0) throw new IllegalArgumentException("The expected number of elements must be nonnegative");
        this.f = f;
        n = arraySize(expected, f);
        mask = n - 1;
        maxFill = maxFill(n, f);
        key = (K[]) new Object[n + 1];
        value = (V[]) new Object[n + 1];
        hasher = CrossHash.defaultHasher;
    }

    /**
     * Creates a new OrderedMap with 0.75f as load factor.
     *
     * @param expected the expected number of elements in the OrderedMap.
     */
    public UnorderedMap(final int expected) {
        this(expected, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new OrderedMap with initial expected 16 entries and 0.75f as load factor.
     */
    public UnorderedMap() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new OrderedMap copying a given one.
     *
     * @param m a {@link Map} to be copied into the new OrderedMap.
     * @param f the load factor.
     */
    public UnorderedMap(final Map<? extends K, ? extends V> m, final float f) {
        this(m.size(), f, (m instanceof UnorderedMap) ? ((UnorderedMap) m).hasher : CrossHash.defaultHasher);
        putAll(m);
    }

    /**
     * Creates a new OrderedMap with 0.75f as load factor copying a given one.
     *
     * @param m a {@link Map} to be copied into the new OrderedMap.
     */
    public UnorderedMap(final Map<? extends K, ? extends V> m) {
        this(m, (m instanceof UnorderedMap) ? ((UnorderedMap) m).f : DEFAULT_LOAD_FACTOR, (m instanceof UnorderedMap) ? ((UnorderedMap) m).hasher : CrossHash.defaultHasher);
    }

    /**
     * Creates a new OrderedMap using the elements of two parallel arrays.
     *
     * @param keyArray the array of keys of the new OrderedMap.
     * @param valueArray the array of corresponding values in the new OrderedMap.
     * @param f the load factor.
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public UnorderedMap(final K[] keyArray, final V[] valueArray, final float f) {
        this(keyArray.length, f);
        if (keyArray.length != valueArray.length)
            throw new IllegalArgumentException("The key array and the value array have different lengths (" + keyArray.length + " and " + valueArray.length + ")");
        for (int i = 0; i < keyArray.length; i++)
            put(keyArray[i], valueArray[i]);
    }
    /**
     * Creates a new OrderedMap using the elements of two parallel arrays.
     *
     * @param keyColl the collection of keys of the new OrderedMap.
     * @param valueColl the collection of corresponding values in the new OrderedMap.
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public UnorderedMap(final Collection<K> keyColl, final Collection<V> valueColl) {
        this(keyColl, valueColl, DEFAULT_LOAD_FACTOR);
    }
        /**
         * Creates a new OrderedMap using the elements of two parallel arrays.
         *
         * @param keyColl the collection of keys of the new OrderedMap.
         * @param valueColl the collection of corresponding values in the new OrderedMap.
         * @param f the load factor.
         * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
         */
    public UnorderedMap(final Collection<K> keyColl, final Collection<V> valueColl, final float f) {
        this(keyColl.size(), f);
        if (keyColl.size() != valueColl.size())
            throw new IllegalArgumentException("The key array and the value array have different lengths (" + keyColl.size() + " and " + valueColl.size() + ")");
        Iterator<K> ki = keyColl.iterator();
        Iterator<V> vi = valueColl.iterator();
        while (ki.hasNext() && vi.hasNext())
        {
            put(ki.next(), vi.next());
        }
    }

    /**
     * Creates a new OrderedMap with 0.75f as load factor using the elements of two parallel arrays.
     *
     * @param keyArray the array of keys of the new OrderedMap.
     * @param valueArray the array of corresponding values in the new OrderedMap.
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public UnorderedMap(final K[] keyArray, final V[] valueArray) {
        this(keyArray, valueArray, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new OrderedMap.
     * <p>
     * <p>The actual table size will be the least power of two greater than <code>expected</code>/<code>f</code>.
     *
     * @param expected the expected number of elements in the hash set.
     * @param f        the load factor.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */

    @SuppressWarnings("unchecked")
    public UnorderedMap(final int expected, final float f, CrossHash.IHasher hasher) {
        if (f <= 0 || f > 1)
            throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than or equal to 1");
        if (expected < 0) throw new IllegalArgumentException("The expected number of elements must be nonnegative");
        this.f = f;
        n = arraySize(expected, f);
        mask = n - 1;
        maxFill = maxFill(n, f);
        key = (K[]) new Object[n + 1];
        value = (V[]) new Object[n + 1];
        this.hasher = (hasher == null) ? CrossHash.defaultHasher : hasher;
    }
    /**
     * Creates a new OrderedMap with 0.75f as load factor.
     *
     * @param expected the expected number of elements in the OrderedMap.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    public UnorderedMap(final int expected, CrossHash.IHasher hasher) {
        this(expected, DEFAULT_LOAD_FACTOR, hasher);
    }

    /**
     * Creates a new OrderedMap with initial expected 16 entries and 0.75f as load factor.
     */
    public UnorderedMap(CrossHash.IHasher hasher) {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR, hasher);
    }

    /**
     * Creates a new OrderedMap copying a given one.
     *
     * @param m a {@link Map} to be copied into the new OrderedMap.
     * @param f the load factor.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    public UnorderedMap(final Map<? extends K, ? extends V> m, final float f, CrossHash.IHasher hasher) {
        this(m.size(), f, hasher);
        putAll(m);
    }

    /**
     * Creates a new OrderedMap with 0.75f as load factor copying a given one.
     * @param m a {@link Map} to be copied into the new OrderedMap.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     */
    public UnorderedMap(final Map<? extends K, ? extends V> m, CrossHash.IHasher hasher) {
        this(m, DEFAULT_LOAD_FACTOR, hasher);
    }

    /**
     * Creates a new OrderedMap using the elements of two parallel arrays.
     *
     * @param keyArray the array of keys of the new OrderedMap.
     * @param valueArray the array of corresponding values in the new OrderedMap.
     * @param f the load factor.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public UnorderedMap(final K[] keyArray, final V[] valueArray, final float f, CrossHash.IHasher hasher) {
        this(keyArray.length, f, hasher);
        if (keyArray.length != valueArray.length)
            throw new IllegalArgumentException("The key array and the value array have different lengths (" + keyArray.length + " and " + valueArray.length + ")");
        for (int i = 0; i < keyArray.length; i++)
            put(keyArray[i], valueArray[i]);
    }
    /**
     * Creates a new OrderedMap with 0.75f as load factor using the elements of two parallel arrays.
     *
     * @param keyArray the array of keys of the new OrderedMap.
     * @param valueArray the array of corresponding values in the new OrderedMap.
     * @param hasher used to hash items; typically only needed when K is an array, where CrossHash has implementations
     * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
     */
    public UnorderedMap(final K[] keyArray, final V[] valueArray, CrossHash.IHasher hasher) {
        this(keyArray, valueArray, DEFAULT_LOAD_FACTOR, hasher);
    }

    private int realSize() {
        return containsNullKey ? size - 1 : size;
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
    private V removeEntry(final int pos) {
        final V oldValue = value[pos];
        value[pos] = null;
        size--;
        shiftKeys(pos);
        if (size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE)
            rehash(n / 2);
        return oldValue;
    }
    private V removeNullEntry() {
        containsNullKey = false;
        key[n] = null;
        final V oldValue = value[n];
        value[n] = null;
        size--;
        if (size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE)
            rehash(n / 2);
        return oldValue;
    }

    /**
     * Puts the first key in keyArray with the first value in valueArray, then the second in each and so on.
     * The entries are all appended to the end of the iteration order, unless a key was already present. Then,
     * its value is changed at the existing position in the iteration order.
     * If the lengths of the two arrays are not equal, this puts a number of entries equal to the lesser length.
     * If either array is null, this returns without performing any changes.
     * @param keyArray an array of K keys that should usually have the same length as valueArray
     * @param valueArray an array of V values that should usually have the same length as keyArray
     */
    public void putAll(final K[] keyArray, final V[] valueArray)
    {
        if(keyArray == null || valueArray == null)
            return;
        for (int i = 0; i < keyArray.length && i < valueArray.length; i++)
            put(keyArray[i], valueArray[i]);

    }

    /**
     * Puts all key-value pairs in the Map m into this OrderedMap.
     * The entries are all appended to the end of the iteration order, unless a key was already present. Then,
     * its value is changed at the existing position in the iteration order. This can take any kind of Map,
     * including unordered HashMap objects; if the Map does not have stable ordering, the order in which entries
     * will be appended is not stable either. For this reason, OrderedMap, LinkedHashMap, and TreeMap (or other
     * SortedMap implementations) will work best when order matters.
     * @param m a Map that should have the same or compatible K key and V value types; OrderedMap and TreeMap work best
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        if (f <= .5)
            ensureCapacity(m.size()); // The resulting map will be sized for
            // m.size() elements
        else
            tryCapacity(size() + m.size()); // The resulting map will be
        int n = m.size();
        final Iterator<? extends Entry<? extends K, ? extends V>> i = m
                .entrySet().iterator();
        Entry<? extends K, ? extends V> e;
        while (n-- != 0) {
            e = i.next();
            put(e.getKey(), e.getValue());
        }
    }
    private int insert(final K k, final V v) {
        int pos;
        if (k == null) {
            if (containsNullKey)
                return n;
            containsNullKey = true;
            pos = n;
        } else {
            K curr;
            final K[] key = this.key;
            // The starting point.
            if ((curr = key[pos = (hasher.hash(k)) & mask]) != null) {
                if (hasher.areEqual(curr, k))
                    return pos;
                while ((curr = key[pos = (pos + 1) & mask]) != null)
                    if (hasher.areEqual(curr, k))
                        return pos;
            }
        }
        key[pos] = k;
        value[pos] = v;
        if (size++ >= maxFill)
            rehash(arraySize(size + 1, f));
        return -1;
    }
    public V put(final K k, final V v) {
        final int pos = insert(k, v);
        if (pos < 0)
            return defRetValue;
        final V oldValue = value[pos];
        value[pos] = v;
        return oldValue;
    }
    /**
     * Shifts left entries with the specified hash code, starting at the
     * specified position, and empties the resulting free entry.
     *
     * @param pos
     *            a starting position.
     */
    protected final void shiftKeys(int pos) {
        // Shift entries with the same hash.
        int last, slot;
        K curr;
        final K[] key = this.key;
        for (;;) {
            pos = ((last = pos) + 1) & mask;
            for (;;) {
                if ((curr = key[pos]) == null) {
                    key[last] = null;
                    value[last] = null;
                    return;
                }
                slot = (hasher.hash(curr))
                        & mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot
                        && slot > pos)
                    break;
                pos = (pos + 1) & mask;
            }
            key[last] = curr;
            value[last] = value[pos];
        }
    }
    @SuppressWarnings("unchecked")
    public V remove(final Object k) {
        if (k == null) {
            if (containsNullKey)
                return removeNullEntry();
            return defRetValue;
        }
        K curr;
        final K[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = (hasher.hash(k)) & mask]) == null)
            return defRetValue;
        if (hasher.areEqual(k, curr))
            return removeEntry(pos);
        while (true) {
            if ((curr = key[pos = (pos + 1) & mask]) == null)
                return defRetValue;
            if (hasher.areEqual(k, curr))
                return removeEntry(pos);
        }
    }
    private V setValue(final int pos, final V v) {
        final V oldValue = value[pos];
        value[pos] = v;
        return oldValue;
    }
    public V get(final Object k) {
        if (k == null)
            return containsNullKey ? value[n] : defRetValue;
        K curr;
        final K[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = (hasher.hash(k)) & mask]) == null)
            return defRetValue;
        if (hasher.areEqual(k, curr))
            return value[pos];
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = (pos + 1) & mask]) == null)
                return defRetValue;
            if (hasher.areEqual(k, curr))
                return value[pos];
        }
    }


    public V getOrDefault(final Object k, final V defaultValue) {
        if (k == null)
            return containsNullKey ? value[n] : defaultValue;
        K curr;
        final K[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = (hasher.hash(k)) & mask]) == null)
            return defaultValue;
        if (hasher.areEqual(k, curr))
            return value[pos];
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = (pos + 1) & mask]) == null)
                return defaultValue;
            if (hasher.areEqual(k, curr))
                return value[pos];
        }
    }

    protected int positionOf(final Object k) {
        if (k == null)
        {
            if(containsNullKey)
                return n;
            else
                return -1;
        }
        K curr;
        final K[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = (hasher.hash(k)) & mask]) == null)
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

    public boolean containsKey(final Object k) {
        if (k == null)
            return containsNullKey;
        K curr;
        final K[] key = this.key;
        int pos;
        // The starting point.
        if ((curr = key[pos = (hasher.hash(k)) & mask]) == null)
            return false;
        if (hasher.areEqual(k, curr))
            return true;
        // There's always an unused entry.
        while (true) {
            if ((curr = key[pos = (pos + 1) & mask]) == null)
                return false;
            if (hasher.areEqual(k, curr))
                return true;
        }
    }
    public boolean containsValue(final Object v) {
        final V value[] = this.value;
        final K key[] = this.key;
        if (containsNullKey
                && (value[n] == null ? v == null : value[n].equals(v)))
            return true;
        for (int i = n; i-- != 0;)
            if (key[i] != null
                    && (value[i] == null ? v == null : value[i].equals(v)))
                return true;
        return false;
    }
    /*
     * Removes all elements from this map.
     *
     * <P>To increase object reuse, this method does not change the table size.
     * If you want to reduce the table size, you must use {@link #trim()}.
     */
    public void clear() {
        if (size == 0)
            return;
        size = 0;
        containsNullKey = false;
        Arrays.fill(key, null);
        Arrays.fill(value, null);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * The entry class for a OrderedMap does not record key and value, but rather the position in the hash table of the corresponding entry. This is necessary so that calls to
     * {@link Entry#setValue(Object)} are reflected in the map
     */
    final class MapEntry implements Entry<K, V> {
        // The table index this entry refers to, or -1 if this entry has been
        // deleted.
        int index;
        MapEntry(final int index) {
            this.index = index;
        }
        MapEntry() {
        }
        public K getKey() {
            return key[index];
        }
        public V getValue() {
            return value[index];
        }
        public V setValue(final V v) {
            final V oldValue = value[index];
            value[index] = v;
            return oldValue;
        }
        @SuppressWarnings("unchecked")
        public boolean equals(final Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Entry<K, V> e = (Entry<K, V>) o;
            return (key[index] == null
                    ? e.getKey() == null
                    : hasher.areEqual(key[index], e.getKey()))
                    && (value[index] == null
                    ? e.getValue() == null
                    : value[index].equals(e.getValue()));
        }
        public int hashCode() {
            return hasher.hash(key[index])
                    ^ (value[index] == null ? 0 : value[index].hashCode());
        }
        @Override
        public String toString() {
            return key[index] + "=>" + value[index];
        }
    }
    
    /**
     * An iterator over a hash map.
     */
    private class MapIterator {
        /**
         * The index of the last entry returned, if positive or zero; initially, {@link #n}. If negative, the last
         * <p>
         * entry returned was that of the key of index {@code - pos - 1} from the {@link #wrapped} list.
         */
        int pos = n;
        /**
         * The index of the last entry that has been returned (more precisely, the value of {@link #pos} if {@link #pos} is positive,
         * <p>
         * or {@link Integer#MIN_VALUE} if {@link #pos} is negative). It is -1 if either
         * <p>
         * we did not return an entry yet, or the last returned entry has been removed.
         */
        int last = -1;
        /**
         * A downward counter measuring how many entries must still be returned.
         */
        int c = size;
        /**
         * A boolean telling us whether we should return the entry with the null key.
         */
        boolean mustReturnNullKey = UnorderedMap.this.containsNullKey;
        /**
         * A lazily allocated list containing keys of entries that have wrapped around the table because of removals.
         */
        ArrayList<K> wrapped;

        public boolean hasNext() {
            return c != 0;
        }

        public int nextEntry() {
            if (!hasNext()) throw new NoSuchElementException();
            c--;
            if (mustReturnNullKey) {
                mustReturnNullKey = false;
                return last = n;
            }
            final K key[] = UnorderedMap.this.key;
            for (; ; ) {
                if (--pos < 0) {
                    // We are just enumerating elements from the wrapped list.
                    last = Integer.MIN_VALUE;
                    final K k = wrapped.get(-pos - 1);
                    int p = hasher.hash(k) & mask;
                    while (!(hasher.areEqual(k, key[p]))) p = (p + 1) & mask;
                    return p;
                }
                if (!((key[pos]) == null)) return last = pos;
            }
        }

        /**
         * Shifts left entries with the specified hash code, starting at the specified position,
         * <p>
         * and empties the resulting free entry.
         *
         * @param pos a starting position.
         */
        private final void shiftKeys(int pos) {
            // Shift entries with the same hash.
            int last, slot;
            K curr;
            final K[] key = UnorderedMap.this.key;
            for (; ; ) {
                pos = ((last = pos) + 1) & mask;
                for (; ; ) {
                    if (((curr = key[pos]) == null)) {
                        key[last] = (null);
                        value[last] = null;
                        return;
                    }
                    slot = (hasher.hash(curr)) & mask;
                    if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) break;
                    pos = (pos + 1) & mask;
                }
                if (pos < last) { // Wrapped entry.
                    if (wrapped == null) wrapped = new ArrayList<>(2);
                    wrapped.add(key[pos]);
                }
                key[last] = curr;
                value[last] = value[pos];
            }
        }

        public void remove() {
            if (last == -1) throw new IllegalStateException();
            if (last == n) {
                containsNullKey = false;
                key[n] = null;
                value[n] = null;
            } else if (pos >= 0) shiftKeys(last);
            else {
                // We're removing wrapped entries.
                UnorderedMap.this.remove(wrapped.set(-pos - 1, null));
                last = -1; // Note that we must not decrement size
                return;
            }
            size--;
            last = -1; // You can no longer remove this entry.
        }

        public int skip(final int n) {
            int i = n;
            while (i-- != 0 && hasNext()) nextEntry();
            return n - i - 1;
        }
    }

    private class EntryIterator extends MapIterator implements Iterator<Map.Entry<K, V>> {
        private MapEntry entry;

        public Map.Entry<K, V> next() {
            return entry = new MapEntry(nextEntry());
        }

        @Override
        public void remove() {
            super.remove();
            entry.index = -1; // You cannot use a deleted entry.
        }
    }

    private final class MapEntrySet extends AbstractSet<Map.Entry<K, V>> {
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @SuppressWarnings("unchecked")
        public boolean contains(final Object o) {
            if (!(o instanceof Map.Entry)) return false;
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            final K k = ((K) e.getKey());
            final V v = ((V) e.getValue());
            if (hasher.areEqual(k, null))
                return UnorderedMap.this.containsNullKey && (value[n] == null ? v == null : value[n].equals(v));
            K curr;
            final K[] key = UnorderedMap.this.key;
            int pos;
            // The starting point.
            if (((curr = key[pos = hasher.hash(k) & mask]) == null))
                return false;
            if (hasher.areEqual(k, curr)) return (value[pos] == null ? (v) == null : (value[pos]).equals(v));
            // There's always an unused entry.
            while (true) {
                if (((curr = key[pos = (pos + 1) & mask]) == null)) return false;
                if (hasher.areEqual(k, curr))
                    return (value[pos] == null ? v == null : value[pos].equals(v));
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean remove(final Object o) {
            if (!(o instanceof Map.Entry)) return false;
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            final K k = ((K) e.getKey());
            final V v = ((V) e.getValue());
            if ((hasher.areEqual(k, null))) {
                if (containsNullKey && ((value[n]) == null ? (v) == null : (value[n]).equals(v))) {
                    removeNullEntry();
                    return true;
                }
                return false;
            }
            K curr;
            final K[] key = UnorderedMap.this.key;
            int pos;
            // The starting point.
            if (((curr = key[pos = hasher.hash(k) & mask]) == null))
                return false;
            if (hasher.areEqual(curr, k)) {
                if (((value[pos]) == null ? (v) == null : (value[pos]).equals(v))) {
                    removeEntry(pos);
                    return true;
                }
                return false;
            }
            while (true) {
                if (((curr = key[pos = (pos + 1) & mask]) == null)) return false;
                if (hasher.areEqual(curr, k)) {
                    if (((value[pos]) == null ? (v) == null : (value[pos]).equals(v))) {
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
            UnorderedMap.this.clear();
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (entries == null) entries = new MapEntrySet();
        return entries;
    }


    /**
     * An iterator on keys.
     *
     *
     *
     * <P>We simply override the {@link java.util.ListIterator#next()}/{@link java.util.ListIterator#previous()} methods
     * <p>
     * (and possibly their type-specific counterparts) so that they return keys
     * <p>
     * instead of entries.
     */
    private final class KeyIterator extends MapIterator implements Iterator<K> {
        public KeyIterator() {
            super();
        }

        public K next() {
            return key[nextEntry()];
        }
    }

    private final class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        public int size() {
            return size;
        }

        public boolean contains(Object k) {
            return containsKey(k);
        }

        public boolean rem(Object k) {
            final int oldSize = size;
            UnorderedMap.this.remove(k);
            return size != oldSize;
        }

        public void clear() {
            UnorderedMap.this.clear();
        }
    }

    public Set<K> keySet() {
        if (keys == null) keys = new KeySet();
        return keys;
    }

    /**
     * An iterator on values.
     * <br>
     * We simply override the {@link java.util.Iterator#next()} method so that it returns values instead of entries.
     */
    private final class ValueIterator extends MapIterator implements Iterator<V> {
        public ValueIterator() {
            super();
        }

        public V next() {
            return value[nextEntry()];
        }
    }

    public final class ValueCollection extends AbstractCollection<V> implements Serializable
    {
        private static final long serialVersionUID = 0L;
        public ValueIterator iterator() {
            return new ValueIterator();
        }
        public int size() {
            return size;
        }
        public boolean contains(Object v) {
            return containsValue(v);
        }
        public void clear() {
            UnorderedMap.this.clear();
        }
    }
    public Collection<V> values() {
        if (values == null) values = new ValueCollection();
        return values;
    }

    public ArrayList<V> valuesAsList()
    {
        ArrayList<V> ls = new ArrayList<>(size);
        ValueIterator vi = new ValueIterator();
        while (vi.hasNext())
            ls.add(vi.next());
        return ls;
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
     * Rehashes the map.
     *
     * <P>
     * This method implements the basic rehashing strategy, and may be overriden
     * by subclasses implementing different rehashing strategies (e.g.,
     * disk-based rehashing). However, you should not override this method
     * unless you understand the internal workings of this class.
     *
     * @param newN
     *            the new size
     */

    @SuppressWarnings("unchecked")
    protected void rehash(final int newN) {
        final K key[] = this.key;
        final V value[] = this.value;
        final int mask = newN - 1; // Note that this is used by the hashing macro
        final K newKey[] = (K[]) new Object[newN + 1];
        final V newValue[] = (V[]) new Object[newN + 1];

        int i = n, pos;
        for (int j = realSize(); j-- != 0; ) {
            while (((key[--i]) == null)) ;
            if (!((newKey[pos = hasher.hash(key[i]) & mask]) == null))
                while (!((newKey[pos = (pos + 1) & mask]) == null)) ;
            newKey[pos] = key[i];
            newValue[pos] = value[i];
        }
        newValue[newN] = value[n];
        n = newN;
        this.mask = mask;
        maxFill = maxFill(n, f);
        this.key = newKey;
        this.value = newValue;
    }
    /*
    @SuppressWarnings("unchecked")
    protected void rehash(final int newN) {
        final K key[] = this.key;
        final V value[] = this.value;
        final int mask = newN - 1; // Note that this is used by the hashing
        // macro
        final K newKey[] = (K[]) new Object[newN + 1];
        final V newValue[] = (V[]) new Object[newN + 1];
        int i = first, prev = -1, newPrev = -1, t, pos;
        final long link[] = this.link;
        final long newLink[] = new long[newN + 1];
        first = -1;
        for (int j = size; j-- != 0;) {
            if (((key[i]) == null))
                pos = newN;
            else {
                pos = (((key[i]).hashCode())) & mask;
                while (!((newKey[pos]) == null))
                    pos = (pos + 1) & mask;
            }
            newKey[pos] = key[i];
            newValue[pos] = value[i];
            if (prev != -1) {
                newLink[newPrev] ^= ((newLink[newPrev] ^ (pos & 0xFFFFFFFFL)) & 0xFFFFFFFFL);
                newLink[pos] ^= ((newLink[pos] ^ ((newPrev & 0xFFFFFFFFL) << 32)) & 0xFFFFFFFF00000000L);
                newPrev = pos;
            } else {
                newPrev = first = pos;
                // Special case of SET(newLink[ pos ], -1, -1);
                newLink[pos] = -1L;
            }
            t = i;
            i = (int) link[i];
            prev = t;
        }
        this.link = newLink;
        this.last = newPrev;
        if (newPrev != -1)
            // Special case of SET_NEXT( newLink[ newPrev ], -1 );
            newLink[newPrev] |= -1 & 0xFFFFFFFFL;
        n = newN;
        this.mask = mask;
        maxFill = maxFill(n, f);
        this.key = newKey;
        this.value = newValue;
    }
    */
    /**
     * Returns a deep copy of this map.
     *
     * <P>
     * This method performs a deep copy of this OrderedMap; the data stored in the
     * map, however, is not cloned. Note that this makes a difference only for
     * object keys.
     *
     * @return a deep copy of this map.
     */
    @SuppressWarnings("unchecked")
    @GwtIncompatible
    public UnorderedMap<K, V> clone() {
        UnorderedMap<K, V> c;
        try {
            c = (UnorderedMap<K, V>) super.clone();
            c.key = (K[]) new Object[n + 1];
            System.arraycopy(key, 0, c.key, 0, n + 1);
            c.value = (V[]) new Object[n + 1];
            System.arraycopy(value, 0, c.value, 0, n + 1);
            c.hasher = hasher;
            return c;
        } catch (Exception cantHappen) {
            throw new UnsupportedOperationException(cantHappen + (cantHappen.getMessage() != null ?
                    "; " + cantHappen.getMessage() : ""));
        }
    }
    /**
     * Returns a hash code for this map.
     *
     * This method overrides the generic method provided by the superclass.
     * Since <code>equals()</code> is not overriden, it is important that the
     * value returned by this method is the same value as the one returned by
     * the overriden method.
     *
     * @return a hash code for this map.
     */
    public int hashCode() {
        int h = 0;
        for (int j = realSize(), i = 0, t = 0; j-- != 0;) {
            while (key[i] == null)
                i++;
            if (this != key[i])
                t = hasher.hash(key[i]);
            if (this != value[i])
                t ^= value[i] == null ? 0 : value[i].hashCode();
            h += t;
            i++;
        }
        // Zero / null keys have hash zero.
        if (containsNullKey)
            h += value[n] == null ? 0 : value[n].hashCode();
        return h;
    }

    public long hash64()
    {
        return 31L * (31L * CrossHash.hash64(key) + CrossHash.hash64(value)) + size;
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
        return Math.min((int)(n * f + 0.99999994f), n - 1);
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
        if (s > (1 << 30))
            throw new IllegalArgumentException("Too large (" + expected + " expected elements with load factor " + f + ")");
        return (int) s;
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
    private int unwrap(final ValueIterator i, final Object[] array, int offset, final int max) {
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
    private int unwrap(final ValueIterator i, final Object[] array) {
        return unwrap(i, array, 0, array.length);
    }


    /** Unwraps an iterator into an array starting at a given offset for a given number of elements.
     *
     * <P>This method iterates over the given type-specific iterator and stores the elements returned, up to a maximum of <code>length</code>, in the given array starting at <code>offset</code>. The
     * number of actually unwrapped elements is returned (it may be less than <code>max</code> if the iterator emits less than <code>max</code> elements).
     *
     * @param i a type-specific iterator.
     * @param array an array to contain the output of the iterator.
     * @param offset the first element of the array to be returned.
     * @param max the maximum number of elements to unwrap.
     * @return the number of elements unwrapped. */
    private static <K> int objectUnwrap(final Iterator<? extends K> i, final K array[], int offset, final int max ) {
        if ( max < 0 ) throw new IllegalArgumentException( "The maximum number of elements (" + max + ") is negative" );
        if ( offset < 0 || offset + max > array.length ) throw new IllegalArgumentException();
        int j = max;
        while ( j-- != 0 && i.hasNext() )
            array[ offset++ ] = i.next();
        return max - j - 1;
    }

    /** Unwraps an iterator into an array.
     *
     * <P>This method iterates over the given type-specific iterator and stores the elements returned in the given array. The iteration will stop when the iterator has no more elements or when the end
     * of the array has been reached.
     *
     * @param i a type-specific iterator.
     * @param array an array to contain the output of the iterator.
     * @return the number of elements unwrapped. */
    private static <K> int objectUnwrap(final Iterator<? extends K> i, final K array[] ) {
        return objectUnwrap(i, array, 0, array.length );
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        final Iterator<Map.Entry<K, V>> i = entrySet().iterator();
        int n = size();
        Map.Entry <K,V> e;
        boolean first = true;
        s.append("UnorderedMap {");
        while(n-- != 0) {
            if (first) first = false;
            else s.append(", ");
            e = i.next();
            if (this == e.getKey()) s.append("(this map)"); else
                s.append(String.valueOf(e.getKey()));
            s.append("=>");
            if (this == e.getValue()) s.append("(this map)"); else
                s.append(String.valueOf(e.getValue()));
        }
        s.append("}");
        return s.toString();
    }
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Map))
            return false;
        Map<?, ?> m = (Map<?, ?>) o;
        if (m.size() != size())
            return false;
        return entrySet().containsAll(m.entrySet());
    }

    @GwtIncompatible
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        final K key[] = this.key;
        final V value[] = this.value;
        final MapIterator i = new MapIterator();
        s.defaultWriteObject();
        s.writeObject(hasher);
        for (int j = size, e; j-- != 0;) {
            e = i.nextEntry();
            s.writeObject(key[e]);
            s.writeObject(value[e]);
        }
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
        final V value[] = this.value = (V[]) new Object[n + 1];
        K k;
        V v;
        for (int i = size, pos; i-- != 0;) {
            k = (K) s.readObject();
            v = (V) s.readObject();
            if (k == null) {
                pos = n;
                containsNullKey = true;
            } else {
                pos = (hasher.hash(k))
                        & mask;
                while (key[pos] != null)
                    pos = (pos + 1) & mask;
            }

            key[pos] = k;
            value[pos] = v;
        }
    }

    public List<V> getMany(Collection<K> keys)
    {
        if(keys == null)
            return new ArrayList<>(1);
        ArrayList<V> vals = new ArrayList<>(keys.size());
        for(K k : keys)
        {
            vals.add(get(k));
        }
        return vals;
    }
    
    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}) associates it with the given value and returns
     * {@code null}, else returns the current value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     * {@code null} if there was no mapping for the key.
     * (A {@code null} return can also indicate that the map
     * previously associated {@code null} with the key.)
     */
    public V putIfAbsent(K key, V value) {
        V v = get(key);
        if(v == null)
            v = put(key, value);
        return v;
    }

    /**
     * Removes the entry for the specified key only if it is currently
     * mapped to the specified value.
     *
     * @param key   key with which the specified value is associated
     * @param value value expected to be associated with the specified key
     * @return {@code true} if the value was removed
     */
    public boolean remove(Object key, Object value) {
        if (containsKey(key) && Objects.equals(get(key), value)) {
            remove(key);
            return true;
        } else
            return false;
    }

    /**
     * Replaces the entry for the specified key only if currently
     * mapped to the specified value. The position in the iteration
     * order is retained.
     *
     * @param key      key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return {@code true} if the value was replaced
     */
    public boolean replace(K key, V oldValue, V newValue) {
        if (containsKey(key) && Objects.equals(get(key), newValue)) {
            put(key, newValue);
            return true;
        } else
            return false;
    }

    /**
     * Replaces the entry for the specified key only if it is
     * currently mapped to some value. Preserves the existing key's
     * position in the iteration order.
     *
     * @param key   key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     * {@code null} if there was no mapping for the key.
     * (A {@code null} return can also indicate that the map
     * previously associated {@code null} with the key.)
     */
    public V replace(K key, V value) {
        if (containsKey(key)) {
            return put(key, value);
        } else
            return null;
    }
    /**
     * Given alternating key and value arguments in pairs, puts each key-value pair into this OrderedMap as if by
     * calling {@link #put(Object, Object)} repeatedly for each pair. This mimics the parameter syntax used for
     * {@link #makeMap(Object, Object, Object...)}, and can be used to retain that style of insertion after an
     * OrderedMap has been instantiated.
     * @param k0 the first key to add
     * @param v0 the first value to add
     * @param rest an array or vararg of keys and values in pairs; should contain alternating K, V, K, V... elements
     * @return this, after adding all viable key-value pairs given
     */
    @SuppressWarnings("unchecked")
    public UnorderedMap<K, V> putPairs(K k0, V v0, Object... rest)
    {
        if(rest == null || rest.length == 0)
        {
            put(k0, v0);
            return this;
        }
        put(k0, v0);

        for (int i = 0; i < rest.length - 1; i+=2) {
            try {
                put((K) rest[i], (V) rest[i + 1]);
            }catch (ClassCastException ignored) {
            }
        }
        return this;
    }

    /**
     * Makes an OrderedMap (OM) with the given load factor (which should be between 0.1 and 0.9), key and value types
     * inferred from the types of k0 and v0, and considers all remaining parameters key-value pairs, casting the Objects
     * at positions 0, 2, 4... etc. to K and the objects at positions 1, 3, 5... etc. to V. If rest has an odd-number
     * length, then it discards the last item. If any pair of items in rest cannot be cast to the correct type of K or
     * V, then this inserts nothing for that pair. This is similar to the makeOM method in the Maker class, but does not
     * allow setting the load factor (since that extra parameter can muddle how javac figures out which generic types
     * the map should use), nor does it log debug information if a cast fails. The result should be the same otherwise.
     * <br>
     * This is named makeMap to indicate that it expects key and value parameters, unlike a Set or List. This convention
     * may be extended to other data structures that also have static methods for instantiation.
     * @param k0 the first key; used to infer the types of other keys if generic parameters aren't specified.
     * @param v0 the first value; used to infer the types of other values if generic parameters aren't specified.
     * @param rest an array or vararg of keys and values in pairs; should contain alternating K, V, K, V... elements
     * @param <K> the type of keys in the returned OrderedMap; if not specified, will be inferred from k0
     * @param <V> the type of values in the returned OrderedMap; if not specified, will be inferred from v0
     * @return a freshly-made OrderedMap with K keys and V values, using k0, v0, and the contents of rest to fill it
     */
    @SuppressWarnings("unchecked")
    public static <K, V> UnorderedMap<K, V> makeMap(K k0, V v0, Object... rest)
    {
        if(rest == null || rest.length == 0)
        {
            UnorderedMap<K, V> om = new UnorderedMap<>(2);
            om.put(k0, v0);
            return om;
        }
        UnorderedMap<K, V> om = new UnorderedMap<>(1 + (rest.length >> 1));
        om.put(k0, v0);

        for (int i = 0; i < rest.length - 1; i+=2) {
            try {
                om.put((K) rest[i], (V) rest[i + 1]);
            }catch (ClassCastException ignored) {
            }
        }
        return om;
    }

    /**
     * Makes an empty OrderedMap (OM); needs key and value types to be specified in order to work. For an empty
     * OrderedMap with String keys and Coord values, you could use {@code Maker.<String, Coord>makeOM()}. Using
     * the new keyword is probably just as easy in this case; this method is provided for completeness relative to
     * makeMap() with 2 or more parameters.
     * @param <K> the type of keys in the returned OrderedMap; cannot be inferred and must be specified
     * @param <V> the type of values in the returned OrderedMap; cannot be inferred and must be specified
     * @return an empty OrderedMap with the given key and value types.
     */
    public static <K, V> UnorderedMap<K, V> makeMap()
    {
        return new UnorderedMap<>();
    }

}
