/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package squidpony.squidmath;

import java.io.Serializable;
import java.util.*;

/** An unordered map of regions (specifically, packed data from CoordPacker or something that uses it, like FOVCache or
 * ZOI, as short arrays) to values of a generic type. Regions can overlap, and you can technically store the same set of
 * points as two or more different keys by appending 0 multiple times, which doesn't change the cells encoded in a short
 * array of packed data but does cause it to be hashed differently and thus not over-write the previous region in this
 * RegionMap. Overlapping areas can be useful, since this provides a way to find all values associated with regions
 * containing a given point using the method allAt(), in a way that is similar to a multimap data structure.
 * <br>
 * Normal Java Maps aren't meant to use primitive arrays as keys, in part because they are mutable and in part because
 * the equals() method on an array isn't exactly correct for this purpose. Here, we have the collection specialized for
 * short[] keys (which still should not be mutated!), use Arrays.equals() for comparing equality, and use
 * CrossHash.hash() for hashing short arrays efficiently.
 * <br>
 * Ported from LibGDX (original class was ObjectMap) by Tommy Ettinger on 1/25/2016.
 * @author Nathan Sweet
 * @author Tommy Ettinger
 */
public class RegionMap<V> implements Iterable<RegionMap.Entry<V>>, Serializable {
    private static final long serialVersionUID = -6026166931953522091L;
    private static final int PRIME1 = 0xbe1f14b1;
    private static final int PRIME2 = 0xb4b82e39;
    private static final int PRIME3 = 0xced1c241;
    private LightRNG lrng = new LightRNG(0xbadfad);
    public int size;

    short[][] keyTable;
    V[] valueTable;
    int capacity, stashSize;

    private float loadFactor;
    private int hashShift, mask, threshold;
    private int stashCapacity;
    private int pushIterations;

    private Entries<V> entries1, entries2;
    private Values<V> values1, values2;
    private Keys keys1, keys2;

    /** Creates a new map with an initial capacity of 51 and a load factor of 0.8. */
    public RegionMap () {
        this(51, 0.8f);
    }

    /** Creates a new map with a load factor of 0.8.
     * @param initialCapacity If not a power of two, it is increased to the next nearest power of two. */
    public RegionMap (int initialCapacity) {
        this(initialCapacity, 0.8f);
    }

    /** Creates a new map with the specified initial capacity and load factor. This map will hold initialCapacity items before
     * growing the backing table.
     * @param initialCapacity If not a power of two, it is increased to the next nearest power of two. */
    @SuppressWarnings("unchecked")
    public RegionMap (int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException("initialCapacity must be >= 0: " + initialCapacity);
        initialCapacity = nextPowerOfTwo((int)Math.ceil(initialCapacity / loadFactor));
        if (initialCapacity > 1 << 30) throw new IllegalArgumentException("initialCapacity is too large: " + initialCapacity);
        capacity = initialCapacity;

        if (loadFactor <= 0) throw new IllegalArgumentException("loadFactor must be > 0: " + loadFactor);
        this.loadFactor = loadFactor;

        threshold = (int)(capacity * loadFactor);
        mask = capacity - 1;
        hashShift = 31 - Integer.numberOfTrailingZeros(capacity);
        stashCapacity = Math.max(3, (int)Math.ceil(Math.log(capacity)) * 2);
        pushIterations = Math.max(Math.min(capacity, 8), (int)Math.sqrt(capacity) / 8);

        keyTable = new short[capacity + stashCapacity][];
        valueTable = (V[])new Object[keyTable.length];
    }

    /** Creates a new map identical to the specified map. */
    public RegionMap (RegionMap<? extends V> map) {
        this(map.capacity, map.loadFactor);
        stashSize = map.stashSize;
        System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
        System.arraycopy(map.valueTable, 0, valueTable, 0, map.valueTable.length);
        size = map.size;
    }

    /** Returns the old value associated with the specified key, or null. */
    public V put (short[] key, V value) {
        if (key == null) throw new IllegalArgumentException("key cannot be null.");
        return put_internal(key, value);
    }

    private V put_internal (short[] key, V value) {
        short[][] keyTable = this.keyTable;

        // Check for existing keys.
        int hashCode = CrossHash.hash(key);
        int index1 = hashCode & mask;
        short[] key1 = keyTable[index1];
        if (Arrays.equals(key, key1)) {
            V oldValue = valueTable[index1];
            valueTable[index1] = value;
            return oldValue;
        }

        int index2 = hash2(hashCode);
        short[] key2 = keyTable[index2];
        if (Arrays.equals(key, key2)) {
            V oldValue = valueTable[index2];
            valueTable[index2] = value;
            return oldValue;
        }

        int index3 = hash3(hashCode);
        short[] key3 = keyTable[index3];
        if (Arrays.equals(key, key3)) {
            V oldValue = valueTable[index3];
            valueTable[index3] = value;
            return oldValue;
        }

        // Update key in the stash.
        for (int i = capacity, n = i + stashSize; i < n; i++) {
            if (Arrays.equals(key, keyTable[i])) {
                V oldValue = valueTable[i];
                valueTable[i] = value;
                return oldValue;
            }
        }

        // Check for empty buckets.
        if (key1 == null) {
            keyTable[index1] = key;
            valueTable[index1] = value;
            if (size++ >= threshold) resize(capacity << 1);
            return null;
        }

        if (key2 == null) {
            keyTable[index2] = key;
            valueTable[index2] = value;
            if (size++ >= threshold) resize(capacity << 1);
            return null;
        }

        if (key3 == null) {
            keyTable[index3] = key;
            valueTable[index3] = value;
            if (size++ >= threshold) resize(capacity << 1);
            return null;
        }

        push(key, value, index1, key1, index2, key2, index3, key3);
        return null;
    }

    public void putAll (RegionMap<V> map) {
        ensureCapacity(map.size);
        for (Entry<V> entry : map)
            put(entry.key, entry.value);
    }

    /** Skips checks for existing keys. */
    private void putResize (short[] key, V value) {
        // Check for empty buckets.
        int hashCode = CrossHash.hash(key);
        int index1 = hashCode & mask;
        short[] key1 = keyTable[index1];
        if (key1 == null) {
            keyTable[index1] = key;
            valueTable[index1] = value;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        int index2 = hash2(hashCode);
        short[] key2 = keyTable[index2];
        if (key2 == null) {
            keyTable[index2] = key;
            valueTable[index2] = value;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        int index3 = hash3(hashCode);
        short[] key3 = keyTable[index3];
        if (key3 == null) {
            keyTable[index3] = key;
            valueTable[index3] = value;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        push(key, value, index1, key1, index2, key2, index3, key3);
    }

    private void push (short[] insertKey, V insertValue, int index1, short[] key1, int index2, short[] key2, int index3, short[] key3) {
        short[][] keyTable = this.keyTable;
        V[] valueTable = this.valueTable;
        int mask = this.mask;

        // Push keys until an empty bucket is found.
        short[] evictedKey;
        V evictedValue;
        int i = 0, pushIterations = this.pushIterations;
        do {
            // Replace the key and value for one of the hashes.
            switch (lrng.nextInt(2)) {
                case 0:
                    evictedKey = key1;
                    evictedValue = valueTable[index1];
                    keyTable[index1] = insertKey;
                    valueTable[index1] = insertValue;
                    break;
                case 1:
                    evictedKey = key2;
                    evictedValue = valueTable[index2];
                    keyTable[index2] = insertKey;
                    valueTable[index2] = insertValue;
                    break;
                default:
                    evictedKey = key3;
                    evictedValue = valueTable[index3];
                    keyTable[index3] = insertKey;
                    valueTable[index3] = insertValue;
                    break;
            }

            // If the evicted key hashes to an empty bucket, put it there and stop.
            int hashCode = CrossHash.hash(evictedKey);
            index1 = hashCode & mask;
            key1 = keyTable[index1];
            if (key1 == null) {
                keyTable[index1] = evictedKey;
                valueTable[index1] = evictedValue;
                if (size++ >= threshold) resize(capacity << 1);
                return;
            }

            index2 = hash2(hashCode);
            key2 = keyTable[index2];
            if (key2 == null) {
                keyTable[index2] = evictedKey;
                valueTable[index2] = evictedValue;
                if (size++ >= threshold) resize(capacity << 1);
                return;
            }

            index3 = hash3(hashCode);
            key3 = keyTable[index3];
            if (key3 == null) {
                keyTable[index3] = evictedKey;
                valueTable[index3] = evictedValue;
                if (size++ >= threshold) resize(capacity << 1);
                return;
            }

            if (++i == pushIterations) break;

            insertKey = evictedKey;
            insertValue = evictedValue;
        } while (true);

        putStash(evictedKey, evictedValue);
    }

    private void putStash (short[] key, V value) {
        if (stashSize == stashCapacity) {
            // Too many pushes occurred and the stash is full, increase the table size.
            resize(capacity << 1);
            put_internal(key, value);
            return;
        }
        // Store key in the stash.
        int index = capacity + stashSize;
        keyTable[index] = key;
        valueTable[index] = value;
        stashSize++;
        size++;
    }

    public V get (short[] key) {
        int hashCode = CrossHash.hash(key);
        int index = hashCode & mask;
        if (!Arrays.equals(key, keyTable[index])) {
            index = hash2(hashCode);
            if (!Arrays.equals(key, keyTable[index])) {
                index = hash3(hashCode);
                if (!Arrays.equals(key, keyTable[index])) return getStash(key);
            }
        }
        return valueTable[index];
    }

    private V getStash (short[] key) {
        short[][] keyTable = this.keyTable;
        for (int i = capacity, n = i + stashSize; i < n; i++)
            if (Arrays.equals(key, keyTable[i])) return valueTable[i];
        return null;
    }

    /** Returns the value for the specified key, or the default value if the key is not in the map. */
    public V get (short[] key, V defaultValue) {
        int hashCode = CrossHash.hash(key);
        int index = hashCode & mask;
        if (!Arrays.equals(key, keyTable[index])) {
            index = hash2(hashCode);
            if (!Arrays.equals(key, keyTable[index])) {
                index = hash3(hashCode);
                if (!Arrays.equals(key, keyTable[index])) return getStash(key, defaultValue);
            }
        }
        return valueTable[index];
    }

    private V getStash (short[] key, V defaultValue) {
        short[][] keyTable = this.keyTable;
        for (int i = capacity, n = i + stashSize; i < n; i++)
            if (Arrays.equals(key, keyTable[i])) return valueTable[i];
        return defaultValue;
    }

    /**
     * Gets a List of all values associated with regions containing a given x,y point.
     * @param x the x coordinate of the point in question
     * @param y the y coordinate of the point in question
     * @return an ArrayList of all V values corresponding to regions containing the given x,y point.
     */
    public OrderedSet<V> allAt(int x, int y)
    {
        OrderedSet<V> found = new OrderedSet<>(capacity);
        OrderedSet<short[]> regions = CoordPacker.findManyPacked(x, y, keyTable);
        for(short[] region : regions)
        {
            found.add(get(region));
        }
        return found;
    }

    /**
     * Checks if a region, stored as packed data (possibly from CoordPacker or this class) overlaps with regions stored
     * in this object as keys. Returns true if there is any overlap, false otherwise
     * @param region the packed region to check for overlap with regions this stores values for
     * @return true if the region overlaps at all, false otherwise
     */
    public boolean containsRegion(short[] region)
    {
        return CoordPacker.regionsContain(region, keyTable);
    }
    /**
     * Gets a List of all regions containing a given x,y point.
     * @param x the x coordinate of the point in question
     * @param y the y coordinate of the point in question
     * @return an ArrayList of all regions in this data structure containing the given x,y point.
     */
    public OrderedSet<short[]> regionsContaining(int x, int y)
    {
        return CoordPacker.findManyPacked(x, y, keyTable);
    }

    public V remove (short[] key) {
        int hashCode = CrossHash.hash(key);
        int index = hashCode & mask;
        if (Arrays.equals(key, keyTable[index])) {
            keyTable[index] = null;
            V oldValue = valueTable[index];
            valueTable[index] = null;
            size--;
            return oldValue;
        }

        index = hash2(hashCode);
        if (Arrays.equals(key, keyTable[index])) {
            keyTable[index] = null;
            V oldValue = valueTable[index];
            valueTable[index] = null;
            size--;
            return oldValue;
        }

        index = hash3(hashCode);
        if (Arrays.equals(key, keyTable[index])) {
            keyTable[index] = null;
            V oldValue = valueTable[index];
            valueTable[index] = null;
            size--;
            return oldValue;
        }

        return removeStash(key);
    }

    V removeStash (short[] key) {
        short[][] keyTable = this.keyTable;
        for (int i = capacity, n = i + stashSize; i < n; i++) {
            if (Arrays.equals(key, keyTable[i])) {
                V oldValue = valueTable[i];
                removeStashIndex(i);
                size--;
                return oldValue;
            }
        }
        return null;
    }

    void removeStashIndex (int index) {
        // If the removed location was not last, move the last tuple to the removed location.
        stashSize--;
        int lastIndex = capacity + stashSize;
        if (index < lastIndex) {
            keyTable[index] = keyTable[lastIndex];
            valueTable[index] = valueTable[lastIndex];
            valueTable[lastIndex] = null;
        } else
            valueTable[index] = null;
    }

    /** Reduces the size of the backing arrays to be the specified capacity or less. If the capacity is already less, nothing is
     * done. If the map contains more items than the specified capacity, the next highest power of two capacity is used instead. */
    public void shrink (int maximumCapacity) {
        if (maximumCapacity < 0) throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity);
        if (size > maximumCapacity) maximumCapacity = size;
        if (capacity <= maximumCapacity) return;
        maximumCapacity = nextPowerOfTwo(maximumCapacity);
        resize(maximumCapacity);
    }

    /** Clears the map and reduces the size of the backing arrays to be the specified capacity if they are larger. */
    public void clear (int maximumCapacity) {
        if (capacity <= maximumCapacity) {
            clear();
            return;
        }
        size = 0;
        resize(maximumCapacity);
    }

    public void clear () {
        if (size == 0) return;
        short[][] keyTable = this.keyTable;
        V[] valueTable = this.valueTable;
        for (int i = capacity + stashSize; i-- > 0;) {
            keyTable[i] = null;
            valueTable[i] = null;
        }
        size = 0;
        stashSize = 0;
    }

    /** Returns true if the specified value is in the map. Note this traverses the entire map and compares every value, which may
     * be an expensive operation.
     * @param identity If true, uses == to compare the specified value with values in the map. If false, uses
     *           {@link #equals(Object)}. */
    public boolean containsValue (Object value, boolean identity) {
        V[] valueTable = this.valueTable;
        if (value == null) {
            short[][] keyTable = this.keyTable;
            for (int i = capacity + stashSize; i-- > 0;)
                if (keyTable[i] != null && valueTable[i] == null) return true;
        } else if (identity) {
            for (int i = capacity + stashSize; i-- > 0;)
                if (valueTable[i] == value) return true;
        } else {
            for (int i = capacity + stashSize; i-- > 0;)
                if (value.equals(valueTable[i])) return true;
        }
        return false;
    }

    public boolean containsKey (short[] key) {
        int hashCode = CrossHash.hash(key);
        int index = hashCode & mask;
        if (!Arrays.equals(key, keyTable[index])) {
            index = hash2(hashCode);
            if (!Arrays.equals(key, keyTable[index])) {
                index = hash3(hashCode);
                if (!Arrays.equals(key, keyTable[index])) return containsKeyStash(key);
            }
        }
        return true;
    }

    private boolean containsKeyStash (short[] key) {
        short[][] keyTable = this.keyTable;
        for (int i = capacity, n = i + stashSize; i < n; i++)
            if (Arrays.equals(key, keyTable[i])) return true;
        return false;
    }

    /** Returns the key for the specified value, or null if it is not in the map. Note this traverses the entire map and compares
     * every value, which may be an expensive operation.
     * @param identity If true, uses == to compare the specified value with values in the map. If false, uses
     *           {@link #equals(Object)}. */
    public short[] findKey (Object value, boolean identity) {
        V[] valueTable = this.valueTable;
        if (value == null) {
            short[][] keyTable = this.keyTable;
            for (int i = capacity + stashSize; i-- > 0;)
                if (keyTable[i] != null && valueTable[i] == null) return keyTable[i];
        } else if (identity) {
            for (int i = capacity + stashSize; i-- > 0;)
                if (valueTable[i] == value) return keyTable[i];
        } else {
            for (int i = capacity + stashSize; i-- > 0;)
                if (value.equals(valueTable[i])) return keyTable[i];
        }
        return null;
    }

    /** Increases the size of the backing array to accommodate the specified number of additional items. Useful before adding many
     * items to avoid multiple backing array resizes. */
    public void ensureCapacity (int additionalCapacity) {
        int sizeNeeded = size + additionalCapacity;
        if (sizeNeeded >= threshold) resize(nextPowerOfTwo((int)Math.ceil(sizeNeeded / loadFactor)));
    }

    @SuppressWarnings("unchecked")
    private void resize (int newSize) {
        int oldEndIndex = capacity + stashSize;

        capacity = newSize;
        threshold = (int)(newSize * loadFactor);
        mask = newSize - 1;
        hashShift = 31 - Integer.numberOfTrailingZeros(newSize);
        stashCapacity = Math.max(3, (int)Math.ceil(Math.log(newSize)) * 2);
        pushIterations = Math.max(Math.min(newSize, 8), (int)Math.sqrt(newSize) / 8);

        short[][] oldKeyTable = keyTable;
        V[] oldValueTable = valueTable;

        keyTable = new short[newSize + stashCapacity][];
        valueTable = (V[])new Object[newSize + stashCapacity];

        int oldSize = size;
        size = 0;
        stashSize = 0;
        if (oldSize > 0) {
            for (int i = 0; i < oldEndIndex; i++) {
                short[] key = oldKeyTable[i];
                if (key != null) putResize(key, oldValueTable[i]);
            }
        }
    }

    private int hash2 (int h) {
        h *= PRIME2;
        return (h ^ h >>> hashShift) & mask;
    }

    private int hash3 (int h) {
        h *= PRIME3;
        return (h ^ h >>> hashShift) & mask;
    }

    public int hashCode () {
        int h = 0;
        short[][] keyTable = this.keyTable;
        V[] valueTable = this.valueTable;
        for (int i = 0, n = capacity + stashSize; i < n; i++) {
            short[] key = keyTable[i];
            if (key != null) {
                h += CrossHash.hash(key) * 31;

                V value = valueTable[i];
                if (value != null) {
                    h += value.hashCode();
                }
            }
        }
        return h;
    }

    @SuppressWarnings("unchecked")
    public boolean equals (Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof RegionMap)) return false;
        RegionMap<V> other = (RegionMap<V>)obj;
        if (other.size != size) return false;
        short[][] keyTable = this.keyTable;
        V[] valueTable = this.valueTable;
        for (int i = 0, n = capacity + stashSize; i < n; i++) {
            short[] key = keyTable[i];
            if (key != null) {
                V value = valueTable[i];
                if (value == null) {
                    if (!other.containsKey(key) || other.get(key) != null) {
                        return false;
                    }
                } else {
                    if (!value.equals(other.get(key))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public String toString (String separator) {
        return toString(separator, false);
    }

    @Override
	public String toString () {
        return toString(", ", true);
    }

    private String toString (String separator, boolean braces) {
        if (size == 0) return braces ? "{}" : "";
        StringBuilder buffer = new StringBuilder(32);
        if (braces) buffer.append('{');
        short[][] keyTable = this.keyTable;
        V[] valueTable = this.valueTable;
        int i = keyTable.length;
        while (i-- > 0) {
            short[] key = keyTable[i];
            if (key == null) continue;
            buffer.append("Packed Region:");
            buffer.append(CoordPacker.encodeASCII(key));
            buffer.append('=');
            buffer.append(valueTable[i]);
            break;
        }
        while (i-- > 0) {
            short[] key = keyTable[i];
            if (key == null) continue;
            buffer.append(separator);
            buffer.append("Packed Region:");
            buffer.append(CoordPacker.encodeASCII(key));
            buffer.append('=');
            buffer.append(valueTable[i]);
        }
        if (braces) buffer.append('}');
        return buffer.toString();
    }
    private static int nextPowerOfTwo(int n)
    {
        int highest = Integer.highestOneBit(n);
        return  (highest == Integer.lowestOneBit(n)) ? highest : highest << 1;
    }
    @Override
	public Entries<V> iterator () {
        return entries();
    }

    /** Returns an iterator for the entries in the map. Remove is supported. Note that the same iterator instance is returned each
     * time this method is called. Use the {@link Entries} constructor for nested or multithreaded iteration. */
    public Entries<V> entries () {
        if (entries1 == null) {
            entries1 = new Entries<>(this);
            entries2 = new Entries<>(this);
        }
        if (!entries1.valid) {
            entries1.reset();
            entries1.valid = true;
            entries2.valid = false;
            return entries1;
        }
        entries2.reset();
        entries2.valid = true;
        entries1.valid = false;
        return entries2;
    }

    /** Returns an iterator for the values in the map. Remove is supported. Note that the same iterator instance is returned each
     * time this method is called. Use the {@link Values} constructor for nested or multithreaded iteration. */
    public Values<V> values () {
        if (values1 == null) {
            values1 = new Values<>(this);
            values2 = new Values<>(this);
        }
        if (!values1.valid) {
            values1.reset();
            values1.valid = true;
            values2.valid = false;
            return values1;
        }
        values2.reset();
        values2.valid = true;
        values1.valid = false;
        return values2;
    }

    /** Returns an iterator for the keys in the map. Remove is supported. Note that the same iterator instance is returned each
     * time this method is called. Use the {@link Keys} constructor for nested or multithreaded iteration. */
    public Keys keys () {
        if (keys1 == null) {
            keys1 = new Keys(this);
            keys2 = new Keys(this);
        }
        if (!keys1.valid) {
            keys1.reset();
            keys1.valid = true;
            keys2.valid = false;
            return keys1;
        }
        keys2.reset();
        keys2.valid = true;
        keys1.valid = false;
        return keys2;
    }

    public static class Entry<V> {
        public short[] key;
        public V value;

        @Override
		public String toString () {
            return "Packed Region:" + CoordPacker.encodeASCII(key) + "=" + value;
        }
    }

    private abstract static class MapIterator<V, I> implements Iterable<I>, Iterator<I> {
        public boolean hasNext;

        final RegionMap<V> map;
        int nextIndex, currentIndex;
        boolean valid = true;

        public MapIterator (RegionMap<V> map) {
            this.map = map;
            reset();
        }

        public void reset () {
            currentIndex = -1;
            nextIndex = -1;
            findNextIndex();
        }

        void findNextIndex () {
            hasNext = false;
            short[][] keyTable = map.keyTable;
            for (int n = map.capacity + map.stashSize; ++nextIndex < n;) {
                if (keyTable[nextIndex] != null) {
                    hasNext = true;
                    break;
                }
            }
        }

        @Override
		public void remove () {
            if (currentIndex < 0) throw new IllegalStateException("next must be called before remove.");
            if (currentIndex >= map.capacity) {
                map.removeStashIndex(currentIndex);
                nextIndex = currentIndex - 1;
                findNextIndex();
            } else {
                map.keyTable[currentIndex] = null;
                map.valueTable[currentIndex] = null;
            }
            currentIndex = -1;
            map.size--;
        }
    }

    public static class Entries<V> extends MapIterator<V, Entry<V>> {
        Entry<V> entry = new Entry<>();

        public Entries (RegionMap<V> map) {
            super(map);
        }

        /** Note the same entry instance is returned each time this method is called. */
        @Override
		public Entry<V> next () {
            if (!hasNext) throw new NoSuchElementException();
            if (!valid) throw new RuntimeException("#iterator() cannot be used nested.");
            short[][] keyTable = map.keyTable;
            entry.key = keyTable[nextIndex];
            entry.value = map.valueTable[nextIndex];
            currentIndex = nextIndex;
            findNextIndex();
            return entry;
        }

        @Override
		public boolean hasNext () {
            if (!valid) throw new RuntimeException("#iterator() cannot be used nested.");
            return hasNext;
        }

        @Override
		public Entries<V> iterator () {
            return this;
        }
    }

    public static class Values<V> extends MapIterator<V, V> {
        public Values (RegionMap<V> map) {
            super(map);
        }

        @Override
		public boolean hasNext () {
            if (!valid) throw new RuntimeException("#iterator() cannot be used nested.");
            return hasNext;
        }

        @Override
		public V next () {
            if (!hasNext) throw new NoSuchElementException();
            if (!valid) throw new RuntimeException("#iterator() cannot be used nested.");
            V value = map.valueTable[nextIndex];
            currentIndex = nextIndex;
            findNextIndex();
            return value;
        }

        @Override
		public Values<V> iterator () {
            return this;
        }

        /** Returns a new list containing the remaining values. */
        public List<V> toList () {
            return toList(new ArrayList<V>(map.size));
        }

        /** Adds the remaining values to the specified list. */
        public List<V> toList (List<V> list) {
            while (hasNext)
                list.add(next());
            return list;
        }
    }

    public static class Keys extends MapIterator<Object, short[]> {
        @SuppressWarnings("unchecked")
        public Keys (RegionMap<?> map) {
            super((RegionMap<Object>) map);
        }

        @Override
		public boolean hasNext () {
            if (!valid) throw new RuntimeException("#iterator() cannot be used nested.");
            return hasNext;
        }

        @Override
		public short[] next () {
            if (!hasNext) throw new NoSuchElementException();
            if (!valid) throw new RuntimeException("#iterator() cannot be used nested.");
            short[] key = map.keyTable[nextIndex];
            currentIndex = nextIndex;
            findNextIndex();
            return key;
        }

        @Override
		public Keys iterator () {
            return this;
        }

        /** Returns a new list containing the remaining keys. */
        public List<short[]> toList() {
            return toList(new ArrayList<short[]>(map.size));
        }

        /** Adds the remaining keys to the array. */
        public List<short[]> toList(List<short[]> list) {
            while (hasNext)
                list.add(next());
            return list;
        }
    }
}