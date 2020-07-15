/******************************************************************************
 Copyright 2011 See AUTHORS file.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package squidpony.squidmath;

import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;

/** An unordered set where the items are unboxed ints. No allocation is done except when growing the table size.
 * <p>
 * This class performs fast contains and remove (typically O(1), worst case O(n) but that is rare in practice). Add may be
 * slightly slower, depending on hash collisions. Hashcodes are rehashed to reduce collisions and the need to resize. Load factors
 * greater than 0.91 greatly increase the chances to resize to the next higher POT size.
 * <p>
 * Unordered sets and maps are not designed to provide especially fast iteration. Iteration is faster with OrderedSet,
 * OrderedMap, and for primitive-valued keys, IntIntOrderedMap and IntDoubleOrderedMap.
 * <p>
 * This implementation uses linear probing with the backward shift algorithm for removal. Hashcodes are rehashed using Fibonacci
 * hashing, instead of the more common power-of-two mask, to better distribute poor hashCodes (see <a href=
 * "https://probablydance.com/2018/06/16/fibonacci-hashing-the-optimization-that-the-world-forgot-or-a-better-alternative-to-integer-modulo/">Malte
 * Skarupke's blog post</a>). Linear probing continues to work even when all hashCodes collide, just more slowly.
 * <br>
 * This class is based on libGDX 1.9.11-SNAPSHOT's IntSet class, and fully replaces the older ShortSet class within
 * SquidLib's usage. Porting older code that uses ShortSet should be a little easier due to the {@link #addAll(short[])}
 * and {@link #random(IRNG)} methods. ShortSet used the somewhat-problematic cuckoo-hashing technique, where this uses
 * the more standard linear probing (like {@link OrderedSet}).
 * @author Nathan Sweet
 * @author Tommy Ettinger */
public class IntSet implements Serializable {
	private static final long serialVersionUID = 1L;

	public int size;

	int[] keyTable;
	boolean hasZeroValue;

	private final float loadFactor;
	private int threshold;

	/** Used by {@link #place(int)} to bit shift the upper bits of a {@code long} into a usable range (&gt;= 0 and &lt;=
	 * {@link #mask}). The shift can be negative, which is convenient to match the number of bits in mask: if mask is a 7-bit
	 * number, a shift of -7 shifts the upper 7 bits into the lowest 7 positions. This class sets the shift &gt; 32 and &lt; 64,
	 * which if used with an int will still move the upper bits of an int to the lower bits due to Java's implicit modulus on
	 * shifts.
	 * <p>
	 * {@link #mask} can also be used to mask the low bits of a number, which may be faster for some hashcodes, if
	 * {@link #place(int)} is overridden. */
	protected int shift;

	/** A bitmask used to confine hashcodes to the size of the table. Must be all 1 bits in its low positions, ie a power of two
	 * minus 1. If {@link #place(int)} is overriden, this can be used instead of {@link #shift} to isolate usable bits of a
	 * hash. */
	protected int mask;

	private IntSetIterator iterator1, iterator2;

	/** Creates a new set with an initial capacity of 51 and a load factor of 0.8. */
	public IntSet () {
		this(51, 0.8f);
	}

	/** Creates a new set with a load factor of 0.8.
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two. */
	public IntSet (int initialCapacity) {
		this(initialCapacity, 0.8f);
	}

	/** Creates a new set with the specified initial capacity and load factor. This set will hold initialCapacity items before
	 * growing the backing table.
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two. */
	public IntSet (int initialCapacity, float loadFactor) {
		if (loadFactor <= 0f || loadFactor >= 1f)
			throw new IllegalArgumentException("loadFactor must be > 0 and < 1: " + loadFactor);
		this.loadFactor = loadFactor;

		int tableSize = tableSize(initialCapacity, loadFactor);
		threshold = (int)(tableSize * loadFactor);
		mask = tableSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		keyTable = new int[tableSize];
	}

	/** Creates a new set identical to the specified set. */
	public IntSet (IntSet set) {
		this((int)(set.keyTable.length * set.loadFactor), set.loadFactor);
		System.arraycopy(set.keyTable, 0, keyTable, 0, set.keyTable.length);
		size = set.size;
		hasZeroValue = set.hasZeroValue;
	}

	/** Returns an index between 0 (inclusive) and {@link #mask} (inclusive) for the specified {@code item}.
	 * <p>
	 * The default implementation uses Fibonacci hashing on the item's {@link Object#hashCode()}: the hashcode is multiplied by a
	 * long constant (2 to the 64th, divided by the golden ratio) then the uppermost bits are shifted into the lowest positions to
	 * obtain an index in the desired range. Multiplication by a long may be slower than int (eg on GWT) but greatly improves
	 * rehashing, allowing even very poor hashcodes, such as those that only differ in their upper bits, to be used without high
	 * collision rates. Fibonacci hashing has increased collision rates when all or most hashcodes are multiples of larger
	 * Fibonacci numbers (see <a href=
	 * "https://probablydance.com/2018/06/16/fibonacci-hashing-the-optimization-that-the-world-forgot-or-a-better-alternative-to-integer-modulo/">Malte
	 * Skarupke's blog post</a>).
	 * <p>
	 * This method can be overriden to customizing hashing. This may be useful eg in the unlikely event that most hashcodes are
	 * Fibonacci numbers, if keys provide poor or incorrect hashcodes, or to simplify hashing if keys provide high quality
	 * hashcodes and don't need Fibonacci hashing: {@code return item.hashCode() & mask;} */
	protected int place (int item) {
		return (int)(item * 0x9E3779B97F4A7C15L >>> shift);
	}

	/** Returns the index of the key if already present, else -(index + 1) for the next empty index. This can be overridden in this
	 * pacakge to compare for equality differently than {@link Object#equals(Object)}. */
	private int locateKey (int key) {
		int[] keyTable = this.keyTable;
		for (int i = place(key);; i = i + 1 & mask) {
			int other = keyTable[i];
			if (other == 0) return -(i + 1); // Empty space is available.
			if (other == key) return i; // Same key was found.
		}
	}

	/** Returns true if the key was not already in the set. */
	public boolean add (int key) {
		if (key == 0) {
			if (hasZeroValue) return false;
			hasZeroValue = true;
			size++;
			return true;
		}
		int i = locateKey(key);
		if (i >= 0) return false; // Existing key was found.
		i = -(i + 1); // Empty space was found.
		keyTable[i] = key;
		if (++size >= threshold) resize(keyTable.length << 1);
		return true;
	}

	public void addAll (IntVLA array) {
		addAll(array.items, 0, array.size);
	}

	public void addAll (IntVLA array, int offset, int length) {
		if (offset + length > array.size)
			throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
		addAll(array.items, offset, length);
	}

	public void addAll (int... array) {
		addAll(array, 0, array.length);
	}

	public void addAll (int[] array, int offset, int length) {
		ensureCapacity(length);
		for (int i = offset, n = i + length; i < n; i++)
			add(array[i]);
	}

	public void addAll (short[] array) {
		addAll(array, 0, array.length);
	}

	public void addAll (short[] array, int offset, int length) {
		ensureCapacity(length);
		for (int i = offset, n = i + length; i < n; i++)
			add(array[i]);
	}

	public void addAll (IntSet set) {
		ensureCapacity(set.size);
		if (set.hasZeroValue) add(0);
		int[] keyTable = set.keyTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			int key = keyTable[i];
			if (key != 0) add(key);
		}
	}

	/** Skips checks for existing keys, doesn't increment size, doesn't need to handle key 0. */
	private void addResize (int key) {
		int[] keyTable = this.keyTable;
		for (int i = place(key);; i = (i + 1) & mask) {
			if (keyTable[i] == 0) {
				keyTable[i] = key;
				return;
			}
		}
	}

	/** Returns true if the key was removed. */
	public boolean remove (int key) {
		if (key == 0) {
			if (!hasZeroValue) return false;
			hasZeroValue = false;
			size--;
			return true;
		}

		int i = locateKey(key);
		if (i < 0) return false;
		int[] keyTable = this.keyTable;
		int mask = this.mask, next = i + 1 & mask;
		while ((key = keyTable[next]) != 0) {
			int placement = place(key);
			if ((next - placement & mask) > (i - placement & mask)) {
				keyTable[i] = key;
				i = next;
			}
			next = next + 1 & mask;
		}
		keyTable[i] = 0;
		size--;
		return true;
	}

	/** Returns true if the set has one or more items. */
	public boolean notEmpty () {
		return size > 0;
	}

	/** Returns true if the set is empty. */
	public boolean isEmpty () {
		return size == 0;
	}

	/** Reduces the size of the backing arrays to be the specified capacity / loadFactor, or less. If the capacity is already less,
	 * nothing is done. If the set contains more items than the specified capacity, the next highest power of two capacity is used
	 * instead. */
	public void shrink (int maximumCapacity) {
		if (maximumCapacity < 0) throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity);
		int tableSize = tableSize(maximumCapacity, loadFactor);
		if (keyTable.length > tableSize) resize(tableSize);
	}

	/** Clears the set and reduces the size of the backing arrays to be the specified capacity / loadFactor, if they are larger. */
	public void clear (int maximumCapacity) {
		int tableSize = tableSize(maximumCapacity, loadFactor);
		if (keyTable.length <= tableSize) {
			clear();
			return;
		}
		size = 0;
		hasZeroValue = false;
		resize(tableSize);
	}

	public void clear () {
		if (size == 0) return;
		size = 0;
		Arrays.fill(keyTable, 0);
		hasZeroValue = false;
	}

	public boolean contains (int key) {
		if (key == 0) return hasZeroValue;
		return locateKey(key) >= 0;
	}

	public int first () {
		if (hasZeroValue) return 0;
		int[] keyTable = this.keyTable;
		for (int i = 0, n = keyTable.length; i < n; i++)
			if (keyTable[i] != 0) return keyTable[i];
		throw new IllegalStateException("IntSet is empty.");
	}
	/**
	 * Gets a random int from this IntSet, using the given {@link IRNG} to generate random values.
	 * If this IntSet is empty, throws an UnsupportedOperationException. This method operates in linear time, unlike
	 * the random item retrieval methods in {@link OrderedSet} and {@link OrderedMap}, which take constant time.
	 * @param rng an {@link IRNG}, such as {@link RNG} or {@link GWTRNG}
	 * @return a random int from this IntSet
	 */
	public int random(IRNG rng) {
		if (size <= 0) {
			throw new UnsupportedOperationException("IntSet cannot be empty when getting a random element");
		}
		int n = rng.nextInt(size);
		int i = 0;
		IntSetIterator isi = iterator();
		while (n-- >= 0 && isi.hasNext)
			i = isi.next();
		isi.reset();
		return i;
	}

	/** Increases the size of the backing array to accommodate the specified number of additional items / loadFactor. Useful before
	 * adding many items to avoid multiple backing array resizes. */
	public void ensureCapacity (int additionalCapacity) {
		int tableSize = tableSize(size + additionalCapacity, loadFactor);
		if (keyTable.length < tableSize) resize(tableSize);
	}

	private void resize (int newSize) {
		int oldCapacity = keyTable.length;
		threshold = (int)(newSize * loadFactor);
		mask = newSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		int[] oldKeyTable = keyTable;

		keyTable = new int[newSize];

		if (size > 0) {
			for (int i = 0; i < oldCapacity; i++) {
				int key = oldKeyTable[i];
				if (key != 0) addResize(key);
			}
		}
	}

	public int hashCode () {
		int h = size;
		int[] keyTable = this.keyTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			int key = keyTable[i];
			if (key != 0) h += key;
		}
		return h;
	}

	public boolean equals (Object obj) {
		if (!(obj instanceof IntSet)) return false;
		IntSet other = (IntSet)obj;
		if (other.size != size) return false;
		if (other.hasZeroValue != hasZeroValue) return false;
		int[] keyTable = this.keyTable;
		for (int i = 0, n = keyTable.length; i < n; i++)
			if (keyTable[i] != 0 && !other.contains(keyTable[i])) return false;
		return true;
	}

	public String toString () {
		if (size == 0) return "[]";
		java.lang.StringBuilder buffer = new java.lang.StringBuilder(32);
		buffer.append('[');
		int[] keyTable = this.keyTable;
		int i = keyTable.length;
		if (hasZeroValue)
			buffer.append("0");
		else {
			while (i-- > 0) {
				int key = keyTable[i];
				if (key == 0) continue;
				buffer.append(key);
				break;
			}
		}
		while (i-- > 0) {
			int key = keyTable[i];
			if (key == 0) continue;
			buffer.append(", ");
			buffer.append(key);
		}
		buffer.append(']');
		return buffer.toString();
	}

	/** Returns an iterator for the keys in the set. Remove is supported.
	 * <p>
	 * The same iterator instance is returned each time this method is called.
	 * Use the {@link IntSetIterator} constructor for nested or multithreaded iteration. */
	public IntSetIterator iterator () {
		if (iterator1 == null) {
			iterator1 = new IntSetIterator(this);
			iterator2 = new IntSetIterator(this);
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

	static public IntSet with (int... array) {
		IntSet set = new IntSet(array.length);
		set.addAll(array);
		return set;
	}

	static public IntSet with (IntVLA array) {
		IntSet set = new IntSet(array.size);
		set.addAll(array);
		return set;
	}

	/**
	 * Allocates a new in array and fills it with the contents of this IntSet.
	 * @return a new int array containing all the int items of this IntSet.
	 */
	public int[] toArray(){
		int[] array = new int[size];
		IntSetIterator it = iterator();
		it.reset();
		int i = 0;
		while (it.hasNext)
			array[i++] = it.next();
		return array;
	}

	/**
	 * Fills an existing (non-null) int array with as many items from this IntSet as will fit in the array, until either
	 * the array is full or the IntSet has no more items to offer.
	 * @param array a non-null int array; can have a length that is the same as, more than or less than {@link #size}
	 * @return {@code array}, after adding items from this
	 */
	public int[] toArray(int[] array){
		IntSetIterator it = iterator();
		it.reset();
		int i = 0;
		while (it.hasNext && i < array.length)
			array[i++] = it.next();
		return array;
	}

	/**
	 * Appends to an existing (non-null) {@link IntVLA} with all the int items in this IntSet.
	 * @param array a non-null IntVLA; no items will be removed, and the contents of this will be added at the end
	 * @return {@code array}, after adding items from this
	 */
	public IntVLA appendInto(IntVLA array){
		IntSetIterator it = iterator();
		it.reset();
		while (it.hasNext)
			array.add(it.next());
		return array;
	}

	static public class IntSetIterator {
		static private final int INDEX_ILLEGAL = -2, INDEX_ZERO = -1;

		public boolean hasNext;

		final IntSet set;
		int nextIndex, currentIndex;
		boolean valid = true;

		public IntSetIterator (IntSet set) {
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
			int[] keyTable = set.keyTable;
			for (int n = keyTable.length; ++nextIndex < n;) {
				if (keyTable[nextIndex] != 0) {
					hasNext = true;
					return;
				}
			}
			hasNext = false;
		}

		public void remove () {
			int i = currentIndex;
			if (i == INDEX_ZERO && set.hasZeroValue) {
				set.hasZeroValue = false;
			} else if (i < 0) {
				throw new IllegalStateException("next must be called before remove.");
			} else {
				int[] keyTable = set.keyTable;
				int mask = set.mask, next = i + 1 & mask, key;
				while ((key = keyTable[next]) != 0) {
					int placement = set.place(key);
					if ((next - placement & mask) > (i - placement & mask)) {
						keyTable[i] = key;
						i = next;
					}
					next = next + 1 & mask;
				}
				keyTable[i] = 0;
				if (i != currentIndex) --nextIndex;
			}
			currentIndex = INDEX_ILLEGAL;
			set.size--;
		}

		public int next () {
			if (!hasNext) throw new NoSuchElementException();
			if (!valid) throw new IllegalStateException("#iterator() cannot be used nested.");
			int key = nextIndex == INDEX_ZERO ? 0 : set.keyTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return key;
		}

		/** Returns a new array containing the remaining keys. */
		public IntVLA toArray () {
			IntVLA array = new IntVLA(set.size);
			while (hasNext)
				array.add(next());
			return array;
		}
		public IntVLA toArray (IntVLA array) {
			while (hasNext)
				array.add(next());
			return array;
		}
	}

	static int tableSize (int capacity, float loadFactor) {
		if (capacity < 0) throw new IllegalArgumentException("capacity must be >= 0: " + capacity);
		int tableSize = HashCommon.nextPowerOfTwo(Math.max(2, (int)Math.ceil(capacity / loadFactor)));
		if (tableSize > 1 << 30) throw new IllegalArgumentException("The required capacity is too large: " + capacity);
		return tableSize;
	}
}
