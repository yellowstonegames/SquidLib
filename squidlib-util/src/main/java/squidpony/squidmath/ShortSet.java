
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
import java.util.NoSuchElementException;

/** An unordered set that uses short keys. This implementation uses cuckoo hashing using 3 hashes, random walking, and a
 * small stash for problematic keys. No allocation is done except when growing the table size. Used internally by
 * CoordPacker, and unlikely to be used outside of it.
 * <br>
 * This set performs very fast contains and remove (typically O(1), worst case O(log(n))). Add may be a bit slower,
 * depending on hash collisions. Load factors greater than 0.91 greatly increase the chances the set will have to rehash
 * to the next higher POT size.
 * @author Nathan Sweet
 * Ported from libGDX by Tommy Ettinger on 10/19/2015.
 */
public class ShortSet implements Serializable{
    private static final long serialVersionUID = -4390851800502156007L;

    private static final int PRIME2 = 0xb4b82e39;
    private static final int PRIME3 = 0xced1c241;
    private static final short EMPTY = 0;

    public int size;

    short[] keyTable;
    int capacity, stashSize;
    boolean hasZeroValue;

    private float loadFactor;
    private int hashShift, threshold;
    private int stashCapacity;
    private int pushIterations;
    private int mask;
    private static long rngState;

    private ShortSetIterator iterator1, iterator2;

    /** Creates a new sets with an initial capacity of 32 and a load factor of 0.8. This set will hold 25 items before growing the
     * backing table. */
    public ShortSet() {
        this(32, 0.8f);
    }

    /** Creates a new set with a load factor of 0.8. This set will hold initialCapacity * 0.8 items before growing the backing
     * table. */
    public ShortSet(int initialCapacity) {
        this(initialCapacity, 0.8f);
    }

    /** Creates a new set with the specified initial capacity and load factor. This set will hold initialCapacity * loadFactor items
     * before growing the backing table. */
    public ShortSet(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException("initialCapacity must be >= 0: " + initialCapacity);
        if (initialCapacity > 1 << 30) throw new IllegalArgumentException("initialCapacity is too large: " + initialCapacity);
        capacity = nextPowerOfTwo(initialCapacity);

        rngState = (long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L);

        if (loadFactor <= 0) throw new IllegalArgumentException("loadFactor must be > 0: " + loadFactor);
        this.loadFactor = loadFactor;

        threshold = (int)(capacity * loadFactor);
        mask = capacity - 1;
        hashShift = 31 - Integer.numberOfTrailingZeros(capacity);
        stashCapacity = Math.max(3, (int)Math.ceil(Math.log(capacity)) * 2);
        pushIterations = Math.max(Math.min(capacity, 8), (int)Math.sqrt(capacity) / 8);

        keyTable = new short[capacity + stashCapacity];
    }

    /** Creates a new map identical to the specified map. */
    public ShortSet(ShortSet map) {
        this(map.capacity, map.loadFactor);
        stashSize = map.stashSize;
        System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
        size = map.size;
        hasZeroValue = map.hasZeroValue;
    }

    /** Returns true if the key was not already in the set. */
    public boolean add (short key) {
        if (key == 0) {
            if (hasZeroValue) return false;
            hasZeroValue = true;
            size++;
            return true;
        }

        short[] keyTable = this.keyTable;

        // Check for existing keys.
        int index1 = key & mask;
        short key1 = keyTable[index1];
        if (key1 == key) return false;

        int index2 = hash2(key);
        short key2 = keyTable[index2];
        if (key2 == key) return false;

        int index3 = hash3(key);
        short key3 = keyTable[index3];
        if (key3 == key) return false;

        // Find key in the stash.
        for (int i = capacity, n = i + stashSize; i < n; i++)
            if (keyTable[i] == key) return false;

        // Check for empty buckets.
        if (key1 == EMPTY) {
            keyTable[index1] = key;
            if (size++ >= threshold) resize(capacity << 1);
            return true;
        }

        if (key2 == EMPTY) {
            keyTable[index2] = key;
            if (size++ >= threshold) resize(capacity << 1);
            return true;
        }

        if (key3 == EMPTY) {
            keyTable[index3] = key;
            if (size++ >= threshold) resize(capacity << 1);
            return true;
        }

        push(key, index1, key1, index2, key2, index3, key3);
        return true;
    }

    public void addAll (ShortVLA array) {
        addAll(array, 0, array.size);
    }

    public void addAll (ShortVLA array, int offset, int length) {
        if (offset + length > array.size)
            throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
        addAll(array.items, offset, length);
    }

    public void addAll (short... array) {
        addAll(array, 0, array.length);
    }

    public void addAll (short[] array, int offset, int length) {
        ensureCapacity(length);
        for (int i = offset, n = i + length; i < n; i++)
            add(array[i]);
    }

    public void addAll (ShortSet set) {
        ensureCapacity(set.size);
        ShortSetIterator iterator = set.iterator();
        while (iterator.hasNext)
            add(iterator.next());
    }

    /** Skips checks for existing keys. */
    private void addResize (short key) {
        if (key == 0) {
            hasZeroValue = true;
            return;
        }

        // Check for empty buckets.
        int index1 = key & mask;
        short key1 = keyTable[index1];
        if (key1 == EMPTY) {
            keyTable[index1] = key;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        int index2 = hash2(key);
        short key2 = keyTable[index2];
        if (key2 == EMPTY) {
            keyTable[index2] = key;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        int index3 = hash3(key);
        short key3 = keyTable[index3];
        if (key3 == EMPTY) {
            keyTable[index3] = key;
            if (size++ >= threshold) resize(capacity << 1);
            return;
        }

        push(key, index1, key1, index2, key2, index3, key3);
    }

    private void push (short insertKey, int index1, short key1, int index2, short key2, int index3, short key3) {
        short[] keyTable = this.keyTable;

        int mask = this.mask;

        // Push keys until an empty bucket is found.
        short evictedKey;
        int i = 0, pushIterations = this.pushIterations;
        do {
            // Replace the key and value for one of the hashes.
            switch (LightRNG.determineBounded(++rngState, 3)) {
                case 0:
                    evictedKey = key1;
                    keyTable[index1] = insertKey;
                    break;
                case 1:
                    evictedKey = key2;
                    keyTable[index2] = insertKey;
                    break;
                default:
                    evictedKey = key3;
                    keyTable[index3] = insertKey;
                    break;
            }

            // If the evicted key hashes to an empty bucket, put it there and stop.
            index1 = evictedKey & mask;
            key1 = keyTable[index1];
            if (key1 == EMPTY) {
                keyTable[index1] = evictedKey;
                if (size++ >= threshold) resize(capacity << 1);
                return;
            }

            index2 = hash2(evictedKey);
            key2 = keyTable[index2];
            if (key2 == EMPTY) {
                keyTable[index2] = evictedKey;
                if (size++ >= threshold) resize(capacity << 1);
                return;
            }

            index3 = hash3(evictedKey);
            key3 = keyTable[index3];
            if (key3 == EMPTY) {
                keyTable[index3] = evictedKey;
                if (size++ >= threshold) resize(capacity << 1);
                return;
            }

            if (++i == pushIterations) break;

            insertKey = evictedKey;
        } while (true);

        addStash(evictedKey);
    }

    private void addStash (short key) {
        if (stashSize == stashCapacity) {
            // Too many pushes occurred and the stash is full, increase the table size.
            resize(capacity << 1);
            add(key);
            return;
        }
        // Store key in the stash.
        int index = capacity + stashSize;
        keyTable[index] = key;
        stashSize++;
        size++;
    }

    /** Returns true if the key was removed. */
    public boolean remove (short key) {
        if (key == 0) {
            if (!hasZeroValue) return false;
            hasZeroValue = false;
            size--;
            return true;
        }

        int index = key & mask;
        if (keyTable[index] == key) {
            keyTable[index] = EMPTY;
            size--;
            return true;
        }

        index = hash2(key);
        if (keyTable[index] == key) {
            keyTable[index] = EMPTY;
            size--;
            return true;
        }

        index = hash3(key);
        if (keyTable[index] == key) {
            keyTable[index] = EMPTY;
            size--;
            return true;
        }

        return removeStash(key);
    }

    boolean removeStash (short key) {
        short[] keyTable = this.keyTable;
        for (int i = capacity, n = i + stashSize; i < n; i++) {
            if (keyTable[i] == key) {
                removeStashIndex(i);
                size--;
                return true;
            }
        }
        return false;
    }

    void removeStashIndex (int index) {
        // If the removed location was not last, move the last tuple to the removed location.
        stashSize--;
        int lastIndex = capacity + stashSize;
        if (index < lastIndex) keyTable[index] = keyTable[lastIndex];
    }

    /** Reduces the size of the backing arrays to be the specified capacity or less. If the capacity is already less, nothing is
     * done. If the set contains more items than the specified capacity, the next highest power of two capacity is used instead. */
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
        hasZeroValue = false;
        size = 0;
        resize(maximumCapacity);
    }

    public void clear () {
        if (size == 0) return;
        short[] keyTable = this.keyTable;
        for (int i = capacity + stashSize; i-- > 0;)
            keyTable[i] = EMPTY;
        size = 0;
        stashSize = 0;
        hasZeroValue = false;
    }

    public boolean contains (short key) {
        if (key == 0) return hasZeroValue;
        int index = key & mask;
        if (keyTable[index] != key) {
            index = hash2(key);
            if (keyTable[index] != key) {
                index = hash3(key);
                if (keyTable[index] != key) return containsKeyStash(key);
            }
        }
        return true;
    }

    private boolean containsKeyStash (short key) {
        short[] keyTable = this.keyTable;
        for (int i = capacity, n = i + stashSize; i < n; i++)
            if (keyTable[i] == key) return true;
        return false;
    }

    public int first () {
        if (hasZeroValue) return 0;
        short[] keyTable = this.keyTable;
        for (int i = 0, n = capacity + stashSize; i < n; i++)
            if (keyTable[i] != EMPTY) return keyTable[i];
        throw new IllegalStateException("ShortSet is empty.");
    }

    /** Increases the size of the backing array to accommodate the specified number of additional items. Useful before adding many
     * items to avoid multiple backing array resizes. */
    public void ensureCapacity (int additionalCapacity) {
        int sizeNeeded = size + additionalCapacity;
        if (sizeNeeded >= threshold) resize(nextPowerOfTwo((int)(sizeNeeded / loadFactor)));
    }

    private void resize (int newSize) {
        int oldEndIndex = capacity + stashSize;

        capacity = newSize;
        threshold = (int)(newSize * loadFactor);
        mask = newSize - 1;
        hashShift = 31 - Integer.numberOfTrailingZeros(newSize);
        stashCapacity = Math.max(3, (int)Math.ceil(Math.log(newSize)) * 2);
        pushIterations = Math.max(Math.min(newSize, 8), (int)Math.sqrt(newSize) / 8);

        short[] oldKeyTable = keyTable;

        keyTable = new short[newSize + stashCapacity];

        int oldSize = size;
        size = hasZeroValue ? 1 : 0;
        stashSize = 0;
        if (oldSize > 0) {
            for (int i = 0; i < oldEndIndex; i++) {
                short key = oldKeyTable[i];
                if (key != EMPTY) addResize(key);
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

    @Override
	public int hashCode () {
        int h = 0;
        for (int i = 0, n = capacity + stashSize; i < n; i++)
            if (keyTable[i] != EMPTY) h += keyTable[i];
        return h;
    }

    @Override
	public boolean equals (Object obj) {
        if (!(obj instanceof ShortSet)) return false;
        ShortSet other = (ShortSet)obj;
        if (other.size != size) return false;
        if (other.hasZeroValue != hasZeroValue) return false;
        for (int i = 0, n = capacity + stashSize; i < n; i++)
            if (keyTable[i] != EMPTY && !other.contains(keyTable[i])) return false;
        return true;
    }

    @Override
	public String toString () {
        if (size == 0) return "[]";
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('[');
        short[] keyTable = this.keyTable;
        int i = keyTable.length;
        if (hasZeroValue)
            buffer.append("0");
        else {
            while (i-- > 0) {
                int key = keyTable[i];
                if (key == EMPTY) continue;
                buffer.append(key);
                break;
            }
        }
        while (i-- > 0) {
            int key = keyTable[i];
            if (key == EMPTY) continue;
            buffer.append(", ");
            buffer.append(key);
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Gets a random short from this ShortSet, using the given {@link IRNG} to generate random values.
     * If this ShortSet is empty, throws an UnsupportedOperationException. This method operates in linear time, unlike
     * the random item retrieval methods in {@link OrderedSet} and {@link OrderedMap}, which take constant time.
     * @param rng an {@link IRNG}, such as {@link RNG} or {@link GWTRNG}
     * @return a random short from this ShortSet
     */
    public short random(IRNG rng)
    {
        if (size <= 0) {
            throw new UnsupportedOperationException("ShortSet cannot be empty when getting a random element");
        }
        int n = rng.nextInt(size);
        short s = 0;
        ShortSet.ShortSetIterator ssi = iterator();
        while (n-- >= 0 && ssi.hasNext)
            s = ssi.next();
        ssi.reset();
        return s;

    }

    private static int nextPowerOfTwo(int n)
    {
        int highest = Integer.highestOneBit(n);
        return  (highest == NumberTools.lowestOneBit(n)) ? highest : highest << 1;
    }

    /** Returns an iterator for the keys in the set. Remove is supported. Note that the same iterator instance is returned each time
     * this method is called. Use the {@link ShortSetIterator} constructor for nested or multithreaded iteration. */
    public ShortSetIterator iterator () {
        if (iterator1 == null) {
            iterator1 = new ShortSetIterator(this);
            iterator2 = new ShortSetIterator(this);
        }
        if (!iterator1.valid) {
            iterator1.reset();
            iterator1.valid = true;
            iterator2.valid = false;
            return iterator1;
        }
        iterator2.reset();
        iterator2.valid = true;
        iterator1.valid = false;
        return iterator2;
    }

    public static ShortSet with (short... array) {
        ShortSet set = new ShortSet();
        set.addAll(array);
        return set;
    }

    public static class ShortSetIterator {
        static final int INDEX_ILLEGAL = -2;
        static final int INDEX_ZERO = -1;

        public boolean hasNext;

        final ShortSet set;
        int nextIndex, currentIndex;
        boolean valid = true;

        public ShortSetIterator(ShortSet set) {
            this.set = set;
            reset();
        }

        public void reset () {
            currentIndex = INDEX_ILLEGAL;
            nextIndex = INDEX_ZERO;
            if (set.hasZeroValue)
                hasNext = true;
            else
                findNextIndex();
        }

        void findNextIndex () {
            hasNext = false;
            short[] keyTable = set.keyTable;
            for (int n = set.capacity + set.stashSize; ++nextIndex < n;) {
                if (keyTable[nextIndex] != EMPTY) {
                    hasNext = true;
                    break;
                }
            }
        }

        public void remove () {
            if (currentIndex == INDEX_ZERO && set.hasZeroValue) {
                set.hasZeroValue = false;
            } else if (currentIndex < 0) {
                throw new IllegalStateException("next must be called before remove.");
            } else if (currentIndex >= set.capacity) {
                set.removeStashIndex(currentIndex);
                nextIndex = currentIndex - 1;
                findNextIndex();
            } else {
                set.keyTable[currentIndex] = EMPTY;
            }
            currentIndex = INDEX_ILLEGAL;
            set.size--;
        }

        public short next () {
            if (!hasNext) throw new NoSuchElementException();
            if (!valid) throw new RuntimeException("ShortSetIterator cannot be used nested.");
            short key = nextIndex == INDEX_ZERO ? 0 : set.keyTable[nextIndex];
            currentIndex = nextIndex;
            findNextIndex();
            return key;
        }

        /** Returns a new array containing the remaining keys. */
        public ShortVLA toArray () {
            ShortVLA array = new ShortVLA(true, set.size);
            while (hasNext)
                array.add(next());
            return array;
        }
    }
}