package squidpony.squidmath;

import squidpony.annotation.Beta;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * An insertion-ordered Map implementation that also allows fetching indices from keys and values from indices.
 * Created by Tommy Ettinger on 6/13/2016.
 */
@Beta
public class OrderedMap<K, V> extends AbstractMap<K, V> {
    protected volatile HashMap<K, Integer> indexSet;
    private transient volatile Set<K> keySet = null;
    protected volatile ArrayList<V> values;
    protected volatile EntrySet entries;

    public int getTotalEntered() {
        return totalEntered;
    }

    protected volatile int totalEntered = 0;

    public OrderedMap()
    {
        indexSet = new HashMap<>(16, 0.25f);
        values = new ArrayList<>(16);
        entries = new EntrySet();
    }

    public OrderedMap(int initialCapacity)
    {
        indexSet = new HashMap<>(initialCapacity, 0.25f);
        values = new ArrayList<>(initialCapacity);
        entries = new EntrySet();
    }

    public OrderedMap(int initialCapacity, float loadFactor)
    {
        indexSet = new HashMap<>(initialCapacity, loadFactor);
        values = new ArrayList<>(initialCapacity);
        entries = new EntrySet();
    }

    public OrderedMap(Map<? extends K, ? extends V> m)
    {
        int max = m.size();
        indexSet = new HashMap<>(max, 0.25f);
        values = new ArrayList<>(max);
        entries = new EntrySet();
        for(Map.Entry<? extends K, ? extends  V> ent : m.entrySet())
        {
            indexSet.put(ent.getKey(), totalEntered++);
            values.add(ent.getValue());
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return entries;
    }

    /**
     * {@inheritDoc}
     *
     * @param key a K-type key; if key is already present in this OrderedMap, it will be overwritten
     * @param value a V-type value
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    @Override
    public V put(K key, V value) {
        if(key == null)
            return null;
        Integer i = indexSet.get(key);
        V old = null;
        if(!(i == null || i < 0 || i >= values.size()))
            old = values.get(i);
        indexSet.put(key, totalEntered);
        values.add(value);
        if(i == null) totalEntered = values.size();
        return old;
    }

    /**
     * {@inheritDoc}
     *
     * @param key
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     */
    @Override
    public V remove(Object key) {
        Integer old = indexSet.remove(key);
        if (old == null || old < 0 || old >= values.size()) return null;
        V vanish = values.get(old);
        if (old.compareTo(values.size() - 1) == 0) {
            values.remove(old.intValue());
            totalEntered = values.size();
        } else
            values.set(old, null);
        return vanish;
    }

    /**
     * {@inheritDoc}
     *
     * @param value
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value) {
        return values.contains(value);
    }

    /**
     * {@inheritDoc}
     *
     * @param key
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key) {
        return key != null && indexSet.containsKey(key);
    }

    /**
     * {@inheritDoc}
     *
     * @param key
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
     */
    @Override
    public V get(Object key) {
        Integer old = indexSet.get(key);
        if(old == null || old < 0 || old >= values.size()) return null;
        return values.get(old);
    }

    /**
     * Gets the (possibly null) Integer index associated with key, that corresponds to the position of the value
     * associated with key in a linear List. If this returns 0, key is part of the first entry; if it returns 1, it is
     * part of the second entry; if it returns null, key is not part of this OrderedMap as a key.
     * @param key should be a K key; this will return null if key is null or not a K type
     * @return the index of key in the
     */
    public int getPosition(Object key) {
        Integer t = indexSet.get(key);
        if(t == null) return -1;
        return t;
    }

    /**
     * Gets the value at the nth position in the OrderedMap, using insertion order. You can use getPosition()to find the
     * index for a key.
     * @param position an int
     * @return the V value at the requested position, if it is present
     * @throws IndexOutOfBoundsException if position is not between 0 (inclusive) and totalEntered (exclusive)
     */
    public V getAt(int position) {
        return values.get(position);
    }

    /**
     * Clears the OrderedMap of all elements and resets any internal counters.
     */
    @Override
    public void clear() {
        indexSet.clear();
        values.clear();
        totalEntered = 0;
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation returns a set that subclasses {@link AbstractSet}.
     * The subclass's iterator method returns a "wrapper object" over this
     * map's <tt>entrySet()</tt> iterator.  The <tt>size</tt> method
     * delegates to this map's <tt>size</tt> method and the
     * <tt>contains</tt> method delegates to this map's
     * <tt>containsKey</tt> method.
     * <p>
     * <p>The set is created the first time this method is called,
     * and returned in response to all subsequent calls.  No synchronization
     * is performed, so there is a slight chance that multiple calls to this
     * method will not all return the same set.
     */
    @Override
    public Set<K> keySet() {
        return ((keySet == null) ? (keySet = indexSet.keySet()) : (keySet));
    }

    /**
     * {@inheritDoc}
     *
     * <p>The collection this returns should not be modified, since it is a direct copy
     * of the "special" implementation of a collection of values that this uses.
     * Internally, it's an ArrayList of V.
     */
    @Override
    public Collection<V> values() {
        return values;
    }

    /**
     * Returns the value to which the specified key is mapped, or
     * {@code defaultValue} if this map contains no mapping for the key.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue the default mapping of the key
     * @return the value to which the specified key is mapped, or
     * {@code defaultValue} if this map contains no mapping for the key
     * @throws ClassCastException   if the key is of an inappropriate type for
     *                              this map
     *                              (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this map
     *                              does not permit null keys
     *                              (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    public V getOrDefault(Object key, V defaultValue) {
        V vl = get(key);
        return (vl == null) ? defaultValue : vl;
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
     * previously associated {@code null} with the key,
     * if the implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation
     *                                       is not supported by this map
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException            if the key or value is of an inappropriate
     *                                       type for this map
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified key or value is null,
     *                                       and this map does not permit null keys or values
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws IllegalArgumentException      if some property of the specified key
     *                                       or value prevents it from being stored in this map
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    public V putIfAbsent(K key, V value) {
        if(key == null) return null;
        V v = get(key);
        if (v == null)
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
     * @throws UnsupportedOperationException if the {@code remove} operation
     *                                       is not supported by this map
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException            if the key or value is of an inappropriate
     *                                       type for this map
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified key or value is null,
     *                                       and this map does not permit null keys or values
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
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
     * mapped to the specified value.
     *
     * @param key      key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return {@code true} if the value was replaced
     * @throws UnsupportedOperationException if the {@code put} operation
     *                                       is not supported by this map
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException            if the class of a specified key or value
     *                                       prevents it from being stored in this map
     * @throws NullPointerException          if a specified key or newValue is null,
     *                                       and this map does not permit null keys or values
     * @throws NullPointerException          if oldValue is null and this map does not
     *                                       permit null values
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws IllegalArgumentException      if some property of a specified key
     *                                       or value prevents it from being stored in this map
     */
    public boolean replace(K key, V oldValue, V newValue) {
        if(key != null && containsKey(key) && Objects.equals(get(key), oldValue)) {
            put(key, newValue);
            return true;
        } else
            return false;
    }

    /**
     * Replaces the entry for the specified key only if it is
     * currently mapped to some value.
     *
     * @param key   key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     * {@code null} if there was no mapping for the key.
     * (A {@code null} return can also indicate that the map
     * previously associated {@code null} with the key,
     * if the implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation
     *                                       is not supported by this map
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException            if the class of the specified key or value
     *                                       prevents it from being stored in this map
     *                                       (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if the specified key or value is null,
     *                                       and this map does not permit null keys or values
     * @throws IllegalArgumentException      if some property of the specified key
     *                                       or value prevents it from being stored in this map
     */
    public V replace(K key, V value) {
        if (containsKey(key)) {
            return put(key, value);
        } else
            return null;
    }


    private class EntrySet extends AbstractSet<Entry<K, V>>{
        /**
         * Returns an iterator over the elements contained in this collection.
         *
         * @return an iterator over the elements contained in this collection
         */
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return indexSet.size();
        }


    }

    private class EntryIterator implements Iterator<Entry<K, V>>
    {
        Iterator<Map.Entry<K, Integer>> kit;
        SimpleEntry<K, V> current;
        EntryIterator()
        {
            kit = indexSet.entrySet().iterator();
            current = null;
        }
        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return kit.hasNext();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public Entry<K, V> next() {
            Entry<K, Integer> en = kit.next();
            return current = new SimpleEntry<>(en.getKey(), values.get(en.getValue()));
        }

        /**
         * Removes from the underlying collection the last element returned
         * by this iterator (optional operation).  This method can be called
         * only once per call to {@link #next}.  The behavior of an iterator
         * is unspecified if the underlying collection is modified while the
         * iteration is in progress in any way other than by calling this
         * method.
         *
         * @throws UnsupportedOperationException if the {@code remove}
         *                                       operation is not supported by this iterator
         * @throws IllegalStateException         if the {@code next} method has not
         *                                       yet been called, or the {@code remove} method has already
         *                                       been called after the last call to the {@code next}
         *                                       method
         */
        @Override
        public void remove() {
            if(current == null)
                throw new IllegalStateException("remove() can only be called once per iteration");
            OrderedMap.this.remove(current.getKey());
            current = null;
        }
    }
}
