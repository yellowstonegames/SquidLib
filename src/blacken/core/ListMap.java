/* blacken - a library for Roguelike games
 * Copyright © 2010, 2011 Steven Black <yam655@gmail.com>
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
package com.googlecode.blacken.core;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * A List which is also (sort of) a Map.
 * 
 * <p>The goal was to have a List that was also a Map. Unfortunately, the
 * interfaces are incompatible (<code><V> remove(<K>)</code> vs. 
 * <code> boolean remove(<E>)</code>). As such we have all of the same
 * functions as the Map interface... without actually <i>being</i> a Map.</p>
 * 
 * <p>Later, we decided we wanted the keys to more clearly be labels for the
 * index entries -- so that when indexed entries are changed the labels (and
 * aliases) are updated as well. It isn't quite a List as it doesn't support
 * arbitrary insertion locations.</p>
 * 
 * <p>Basically, the Map's key acts as a label for the entries in the List.
 * The List can have entries which are unlabeled, but a Map entry always has an
 * entry in the List. (Though it need not be 1:1.)</p>
 * 
 * @author yam655
 * @param <K> Key
 * @param <V> Value
 *
 */
public class ListMap<K, V> 
implements List<V>, Cloneable, Serializable {
    /**
     * The Consolidated List Map entry allows for easier copying between 
     * ListMaps.
     * 
     * @author yam655
     * @param <L> Key type
     * @param <W> Value type
     */
    protected static class ConsolidatedListMapEntry<L, W> {
        private Set<L> keys;
        private ListMapEntry<W> entry;
        /**
         * Create new consolidated list map entry.
         */
        public ConsolidatedListMapEntry() {
            this.keys = new HashSet<L>();
            this.entry = new ListMapEntry<W>();
        }
        /**
         * Add a new key to the entry
         * @param key the key to add
         */
        public void addKey(L key) {
            keys.add(key);
        }
        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ConsolidatedListMapEntry)) {
                return false;
            }
            ConsolidatedListMapEntry<?,?> othr = (ConsolidatedListMapEntry<?,?>)other;
            return keys.equals(othr.keys) && entry.equals(entry);
        }
        /**
         * Get the ListMapEntry
         * @return the ListMapEntry
         */
        public ListMapEntry<W> getEntry() {
            return entry;
        }
        /**
         * Get the index from the entry
         * @return the index
         */
        public int getIndex() {
            return entry.getIndex();
        }
        /**
         * Get the list of keys
         * @return the key list
         */
        public Collection<L> getKeys() {
            return keys;
        }
        /**
         * Get the value from the entry
         * @return the value
         */
        public W getValue() {
            return entry.getValue();
        }
        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            int ret = 1;
            ret = ret * 31 + keys.hashCode();
            ret = ret * 31 + entry.hashCode();
            return ret;
        }
        /**
         * Set the ListMapEntry
         * @param entry the entry
         */
        public void setEntry(ListMapEntry<W> entry) {
            this.entry = new ListMapEntry<W>(entry);
        }
        /**
         * Set the index on the entry
         * @param index the index
         */
        public void setIndex(int index) {
            this.entry.setIndex(index);
        }
        /**
         * Set the entire set of keys
         * @param keys the entire set of keys
         */
        public void setKeys(Collection<L> keys) {
            this.keys = new HashSet<L>(keys);
        }
        /**
         * Set the value on the entry
         * @param value the value
         */
        public void setValue(W value) {
            this.entry.setValue(value);
        }
        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("%s -> %s", keys, entry); //$NON-NLS-1$
        }
    }

    protected static class ListMapEntry<W> {
        private W value;
        private int index;
        public ListMapEntry() {
            setValue(null);
            setIndex(-1);
        }
        /**
         * @param entry an existing list-map entry
         */
        public ListMapEntry(ListMapEntry<? extends W> entry) {
            setValue(entry.value);
            setIndex(entry.index);
        }
        public ListMapEntry(W value, int index) {
            setValue(value);
            setIndex(index);
        }
        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ListMapEntry)) {
                return false;
            }
            ListMapEntry<?> othr = (ListMapEntry<?>)other;
            return index == othr.index && value.equals(othr.value);
        }
        public int getIndex() { return index; }
        public W getValue() { return value; }
        @Override
        public int hashCode() {
            int ret = 1;
            ret = ret * 31 + (value == null ? 0 : value.hashCode());
            ret = ret * 31 + new Integer(index).hashCode();
            return ret;
        }
        public void setIndex(int index) { this.index = index; }
        public void setValue(W value) { this.value = value; }
        @Override
        public String toString() {
            return String.format("%d: %s", index, value); //$NON-NLS-1$
        }
    }

    /**
     * Iterator for the ListMap
     * 
     * @author yam655
     */
    protected class ListMapIterator implements ListIterator<V> {
        private int index;
        private List<V> backing;
        ListMapIterator(List<V> backing) {
            this.backing = backing;
            this.index = 0;
        }
        ListMapIterator(List<V> backing, int index) {
            this.backing = backing;
            this.index = index;
        }
        
        @Override
        public void add(V e) {
            backing.add(index, e);
        }

        @Override
        public boolean hasNext() {
            return index < backing.size();
        }

        @Override
        public boolean hasPrevious() {
            return index > 0;
        }

        @Override
        public V next() {
            if (index >= backing.size()) {
                throw new NoSuchElementException();
            }
            return backing.get(index++);
        }

        @Override
        public int nextIndex() {
            return index;
        }

        @Override
        public V previous() {
            if (index <= 0) {
                throw new NoSuchElementException();
            }
            return backing.get(--index);
        }

        @Override
        public int previousIndex() {
            return index -1;
        }

        @Override
        public void remove() {
            if (index >= backing.size()) {
                return;
            }
            backing.remove(index);
        }

        @Override
        public void set(V e) {
            backing.set(index, e);
        }        
    }
    /**
     * A List view in of the ListMap.
     * 
     * @author yam655
     */
    protected class ListMapView implements List<V>{
        private List<V> backing;
        private int start;
        private int end;

        ListMapView(List<V> backing, int start, int end) {
            this.backing = backing;
            this.start = start;
            if (end == -1) {
                end = backing.size();
            }
            this.end = end;
        }

        @Override
        public void add(int index, V element) {
            if (index < 0 || index + start >= end) {
                throw new NoSuchElementException();
            }
            backing.add(index + start, element);
        }

        @Override
        public boolean add(V e) {
            backing.add(end++, e);
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends V> c) {
            boolean ret = backing.addAll(c);
            end += c.size();
            return ret;
        }

        @Override
        public boolean addAll(int index, Collection<? extends V> c) {
            boolean ret = backing.addAll(index, c);
            end += c.size();
            return ret;
        }

        @Override
        public void clear() {
            while (start < end) {
                backing.remove(start);
                end--;
            }
        }

        @Override
        public boolean contains(Object value) {
            for (int i = start; i < end; i++) {
                V entry = backing.get(i);
                if (entry == null) {
                    if (value == null) {
                        return true;
                    }
                } else if (entry.equals(value)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> values) {
            for (Object value : values) {
                if (!this.contains(value)) return false;
            }
            return true;
        }

        @Override
        public V get(int index) {
            if (index < 0 || index + start >= end) {
                throw new NoSuchElementException();
            }
            return backing.get(index);
        }

        @Override
        public int indexOf(Object o) {
            int i = backing.indexOf(o);
            if (i == -1) return -1;
            if (i < start || i >= end) {
                throw new NoSuchElementException();
            }
            return i - start;
        }

        @Override
        public boolean isEmpty() {
            return start == end;
        }

        @Override
        public Iterator<V> iterator() {
            return new ListMapIterator(this);
        }

        @Override
        public int lastIndexOf(Object o) {
            int i = backing.lastIndexOf(o);
            if (i == -1) return -1;
            if (i < start || i >= end) {
                throw new NoSuchElementException();
            }
            return i - start;
        }

        @Override
        public ListIterator<V> listIterator() {
            return new ListMapIterator(this);
        }

        @Override
        public ListIterator<V> listIterator(int index) {
            return new ListMapIterator(this, index);
        }

        @Override
        public V remove(int index) {
            if (index < 0 || index + start >= end) {
                throw new NoSuchElementException();
            }
            return backing.remove(index + start);
        }

        @Override
        public boolean remove(Object o) {
            int index = this.indexOf(o);
            if (index == -1) {
                return false;
            }
            backing.remove(index + start);
            return true;
        }

        @Override
        public boolean removeAll(Collection<?> values) {
            int origSize = this.size();
            for (Object value : values) {
                int index = valueList.indexOf(value);
                if (index < 0 || index + start >= end) {
                    continue;
                }
                backing.remove(index);
                end--;
            }
            return this.size() != origSize;
        }

        @Override
        public boolean retainAll(Collection<?> values) {
            if (values == null) return false;
            int oldSize = this.size();
            int index = 0;
            while (index < this.size()) {
                V value = this.get(index);
                if (values.contains(value)) {
                    index++;
                } else {
                    remove(index);
                }
            }
            return oldSize != this.size();
        }

        @Override
        public V set(int index, V element) {
            if (index < 0 || index + start >= end) {
                throw new NoSuchElementException();
            }
            return backing.set(index + start, element);
        }

        @Override
        public int size() {
            return end - start;
        }

        @Override
        public List<V> subList(int fromIndex, int toIndex) {
            if (toIndex == -1) toIndex = size();
            if (toIndex < 0 || toIndex > size()) {
                throw new NoSuchElementException();
            }
            if (fromIndex < 0 || fromIndex > size()) {
                throw new NoSuchElementException();
            }
            return new ListMapView(this, fromIndex, toIndex);
        }

        @Override
        public Object[] toArray() {
            Object[] ret = new Object[this.size()];
            for (int i = 0; i < size(); i++) {
                ret[i] = get(i);
            }
            return ret;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            T[] ret;
            if (a.length >= this.size()) {
                ret = a;
            } else {
                ret = (T[]) Array.newInstance(a.getClass(), this.size());
            }
            for (int i = 0; i < size(); i++) {
                ret[i] = (T) get(i);
            }
            return ret;
        }
    }
    
    /**
     * serialization ID
     */
    private static final long serialVersionUID = 8849864398418605556L;
    
    private List<ListMapEntry<V>> valueList = null;
    private Map<K, ListMapEntry<V>> keyMap = new HashMap<K, ListMapEntry<V>>();

    /**
     * Create a new, empty, ListMap
     */
    public ListMap() {
        valueList = new ArrayList<ListMapEntry<V>>();
        keyMap = new HashMap<K, ListMapEntry<V>>();
    }
    /**
     * Create a ListMap based upon an ordered Collection
     * 
     * @param collection base collection
     */
    public ListMap(Collection<? extends V> collection) {
        valueList = new ArrayList<ListMapEntry<V>>(collection.size());
        keyMap = new HashMap<K, ListMapEntry<V>>();
        this.addAll(collection);
    }
    /**
     * Create a ListMap expecting a particular size
     * 
     * @param size expected size
     */
    public ListMap(int size) {
        valueList = new ArrayList<ListMapEntry<V>>(size);
        keyMap = new HashMap<K, ListMapEntry<V>>(size);
    }
    /**
     * Create a ListMap based upon an existing ListMap
     * 
     * @param other other list map
     */
    public ListMap(ListMap<K, V> other) {
        valueList = new ArrayList<ListMapEntry<V>>(other.size());
        keyMap = new HashMap<K, ListMapEntry<V>>();
        addAll(other);
    }
    /**
     * Add a new <code>Entry</code> to the list.
     * 
     * @param entry entry to add
     * @return old index that the entry's key referred to
     */
    protected V add(Entry<K, V> entry) {
        ListMapEntry<V> e = new ListMapEntry<V>(entry.getValue(), this.size());
        this.valueList.add(e);
        ListMapEntry<V> old = keyMap.put(entry.getKey(), e);
        if (old == null) return null;
        return old.getValue();
    }

    /*
     * (non-Javadoc)
     * @see java.util.List#add(int, java.lang.Object)
     */
    @Override
    public void add(int index, V element) {
        ListMapEntry<V> e = new ListMapEntry<V>(element, index);
        if (index == this.valueList.size()) {
            this.valueList.add(e);
        } else {
            this.valueList.add(index, e);
            this.reindex(index+1);
        }
    }
    
    /**
     * Add a new <code>value</code> to the end of the list, naming it 
     * <code>key</code>.
     * 
     * <p>This always adds the entry. If the <code>key</code> already exists,
     * the value located at the old index is orphaned, the new value is added
     * to the end of the list, and the index is updated. This is by design as 
     * it provides a way to separate an aliased name from the shared index 
     * entry.</p>
     * 
     * @param key key to add
     * @param value value to add
     * @return true
     */
    public boolean add(K key, V value) {
        ListMapEntry<V> e = new ListMapEntry<V>(value, this.size());
        this.valueList.add(e);
        keyMap.put(key, e);
        return true;
    }
    /**
     * Map a set of keys to a particular value
     * 
     * <p>In addition to making all of the keys reference the 
     * <code>value</code>, this adds the value to the indexable list.</p>
     * 
     * @param keys array of keys
     * @param value value to assign to the keys
     * @return true on change
     */
    public boolean add(K[] keys, V value) {
        if (keys.length == 0) {
            return add(value);
        }
        Integer idx = size();
        add(keys[0], value);
        for (int i = 1; i < keys.length; i++) {
            putKey(keys[i], idx);
        }
        return true;
    }
    @Override
    public boolean add(V value) {
        ListMapEntry<V> e = new ListMapEntry<V>(value, valueList.size());
        return this.valueList.add(e);
    }
    /**
     * Add the indexed <code>values</code> to the end of this list.
     */
    @Override
    public boolean addAll(Collection<? extends V> values) {
        boolean ret = false;
        if (values == null) return ret;
        for (V value : values) {
            if (add(value)) {
                ret = true;
            }
        }
        return ret;
    }
    /*
     * (non-Javadoc)
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    @Override
    public boolean addAll(int index, Collection<? extends V> values) {
        boolean ret = false;
        if (values == null) return ret;
        if (!values.isEmpty()) ret = true;
        for (V value : values) {
            add(index++, value);
        }
        reindex(index);
        return ret;
    }
    /**
     * Add a set of values at a particular index position.
     * 
     * <p>This does not add/update the keys.</p>
     * 
     * @param index starting index position
     * @param items items to insert
     * @return true on change
     */
    public boolean addAll(int index, V[] items) {
        if (items.length == 0) {
            return false;
        }
        for (V item : items) {
            add(index++, item);
        }
        reindex(index);
        return true;
    }
    /**
     * Add a synchronized pair of key and item arrays.
     * 
     * <p>Given an array of keys and an array of values of the same length,
     * this will process {k1, k2, k3...} and {v1, v2, v3...} so that 
     * <code>add(k1, v1)</code>, <code>add(k2, v2)</code>... is processed.
     * 
     * <p>If the two arrays are different sizes, this only processes the 
     * shorter amount.</p>
     * 
     * @param keys an array of keys
     * @param items an array of values
     */
    public void addAll(K[] keys, V[] items) {
        int commonLength = keys.length < items.length ? keys.length : items.length;
        for (int i=0; i < commonLength; i++) {
            add(keys[i], items[i]);
        }
        return;
    }

    /**
     * Add a synchronized pair of key and item Lists.
     * 
     * <p>Given a List of keys and a List of values of the same length,
     * this will process {k1, k2, k3...} and {v1, v2, v3...} so that 
     * <code>add(k1, v1)</code>, <code>add(k2, v2)</code>... is processed.
     * 
     * <p>If the two Lists are different sizes, this only processes the 
     * shorter amount.</p>
     * 
     * @param keys an array of keys
     * @param items an array of values
     */
    public void addAll(List<K> keys, List<V> items) {
        int commonLength = keys.size() < items.size() ? keys.size() : items.size();
        for (int i=0; i < commonLength; i++) {
            add(keys.get(i), items.get(i));
        }
        return;
    }
    /**
     * Add an existing ListMap to the end of this one.
     * 
     * <p>This first copies all of the indexed entries, then goes through
     * and adds or updates the names to point to the newly added indexed 
     * entries. This always adds all of the indexes to the end of the current
     * ListMap, and the new/updated keys always only point to the new 
     * entries.</p>
     * 
     * @param old the old ListMap
     * @return true
     */
    public boolean addAll(ListMap<K, V> old) {
        if (old.isEmpty()) {
            return false;
        }
        int add = this.size();
        for (V value : old) {
            add(value);
        }
        for (Entry<K, ListMapEntry<V>> entry : old.keyMap.entrySet()) {
            putKey(entry.getKey(), entry.getValue().getIndex() + add);
        }
        return true;
    }
    /**
     * Add a set of keys from an existing ListMap in a particular order.
     * 
     * This does not copy any of the alias names for a value.
     * 
     * @param old the previous <code>ListMap</code>
     * @param order the new order
     */
    public void addAll(ListMap<K, V> old, K[] order) {
        for (K key : order) {
            this.add(key, old.get(key));
        }
    }
    
    /**
     * @param list set of entries to add
     */
    public void addAll(Set<Entry<K, V>> list) {
        for (Entry<K, V> entry : list) {
            add(entry);
        }
        return;
    }
    /**
     * Add an array of values.
     * 
     * @param items value array
     * @return true if ListMap changed
     */
    public boolean addAll(V[] items) {
        if (items.length == 0) {
            return false;
        }
        for (V item : items) {
            add(item);
        }
        return true;
    }

    /**
     * A version of {@link #add(Object[], Object)} that will work with varargs.
     * 
     * @param value value to assign to the keys
     * @param keys array of keys
     * @return true on change
     */
    public boolean addAndLabel(V value, K... keys) {
        return add(keys, value);
    }

    /**
     * Clear both the index as well as the name map.
     */
    @Override
    public void clear() {
        this.valueList.clear();
        clearKeys();
    }

    /**
     * Clear the key map.
     */
    public void clearKeys() {
        keyMap.clear();
    }

    protected void 
    consolidateEntries(Map<Integer, ConsolidatedListMapEntry<K, V>> map) {
        // map = new HashMap<Integer, ConsolidatedListMapEntry<K, V>>();
        ConsolidatedListMapEntry<K, V> clme;
        for (K key : keyMap.keySet()) {
            ListMapEntry<V> entry = keyMap.get(key);
            Integer idx = entry.getIndex();
            clme = map.get(idx);
            if (clme == null) {
                clme = new ConsolidatedListMapEntry<K, V>();
                clme.setEntry(entry);
                map.put(idx, clme);
            }
            clme.addKey(key);
            
        }
    }

    /**
     * Warning: This may be slow.
     */
    @Override
    public boolean contains(Object value) {
        for (ListMapEntry<V> entry : this.valueList) {
            if (entry == null) {
                continue;
            } else if (entry.getValue() == null) {
                if (value == null) {
                    return true;
                }
            } else if (entry.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Warning: This is probably super slow.
     */
    @Override
    public boolean containsAll(Collection<?> values) {
        for (Object value : values) {
            if (!this.contains(value)) return false;
        }
        return true;
    }

    /**
     * Do we have a specific key?
     * 
     * @param key key to check
     * @return true if we have the key, false if not
     */
    public boolean containsKey(K key) {
        return keyMap.containsKey(key);
    }

    /**
     * Do we have a key for a value?
     * 
     * <p>Remember that we can have elements in our index which have no key,
     * so this returns true for a subset of what will be true for 
     * {@link #contains(Object)}.</p>
     * 
     * @param value value to check.
     * @return true if there is a key for <code>value</code>
     */
    public boolean containsValue(V value) {
        Set<Entry<K, V>> set = entrySet();
        for (Entry<K, V> entry : set) {
            if (entry.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create an entry set for the ListMap.
     * 
     * @return a Set<Entry<K, V>>
     */
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> set = new HashSet<Entry<K, V>>();
        for (Entry<K, ListMapEntry<V>> entry : keyMap.entrySet()) {
            SimpleEntry<K, V> e = 
                new AbstractMap.SimpleEntry<K, V>(entry.getKey(), 
                                                  entry.getValue().getValue());
            set.add(e);
        }
        return set;
    }
    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ListMap)) {
            return false;
        }
        ListMap<?, ?> othr = (ListMap<?, ?>)other;
        if (othr.size() != this.size()) return false;
        if (othr.keyMap.size() != this.keyMap.size()) return false;
        if (!this.keyMap.isEmpty()) {
            if (!othr.keyMap.keySet().iterator().next().getClass()
                    .isAssignableFrom(this.keyMap.keySet().iterator()
                                      .next().getClass())) {
                return false;
            }
        }
        for (int i = 0; i < size(); i++) {
            if (!this.get(i).equals(othr.get(i))) {
                return false;
            }
        }
        for (Object key : othr.keySet()) {
            if (!this.containsKey((K) key))  {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get the value for an index
     */
    @Override
    public V get(int index) {
        return valueList.get(index).getValue();
    }

    /**
     * Get the value for a key
     * 
     * This is similar to {@link Map#get(Object)}
     * 
     * @param key key value to get
     * @return the requested value
     */
    public V get(K key) {
        if (keyMap.containsKey(key)) {
            return keyMap.get(key).getValue();
        }
        return null;
    }
    /**
     * Walk the entries to find the key related to a value.
     * 
     * <p>This is not considered a key function, and the speed is appropriate
     * for the priority.</p>
     * 
     * @param value value to find the key for
     * @return key associated with a value
     */
    public K getKey(V value) {
        Set<Map.Entry<K, V>> entries;
        entries = entrySet();
        K found = null;
        for (Map.Entry<K, V> entry : entries) {
            if (entry.getValue().equals(value)) {
                found = entry.getKey();
                break;
            }
        }
        return found;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int ret = 1;
        Map<Integer, ConsolidatedListMapEntry<K, V>> c = new HashMap<>();
        consolidateEntries(c);
        for (Integer i = 0; i < c.size(); i++) {
            ret = ret * 31 + Objects.hashCode(c.get(i));
        }
        return ret;
    }

    /**
     * Warning: This may be slow.
     */
    @Override
    public int indexOf(Object value) {
        for (int index = 0; index < valueList.size(); index++) {
            ListMapEntry<V> entry = this.valueList.get(index);
            if (entry == null) {
                continue;
            } else if (entry.getValue() == null) {
                if (value == null) {
                    return index;
                }
            } else if (entry.getValue().equals(value)) {
                return index;
            }
        }
        return -1;
    }
    /**
     * Get the index of a key.
     * 
     * <p>This is guaranteed to be the same speed as getting the value.</p>
     * 
     * @param key key to find the index of
     * @return the index for the key
     */
    public int indexOfKey(K key) {
        return keyMap.get(key).getIndex();
    }
    /*
     * (non-Javadoc)
     * @see java.util.List#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return valueList.isEmpty();
    }
    /*
     * (non-Javadoc)
     * @see java.util.List#iterator()
     */
    @Override
    public Iterator<V> iterator() {
        return new ListMapIterator(this);
    }
    /**
     * Similar to {@link Map#keySet()}.
     * 
     * @return a set containing all of the keys
     */
    public Set<? extends K> keySet() {
        return keyMap.keySet();
    }
    /*
     * (non-Javadoc)
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    @Override
    public int lastIndexOf(Object value) {
        for (int index = valueList.size()-1; index > 0 ; index--) {
            ListMapEntry<V> entry = this.valueList.get(index);
            if (entry == null) {
                continue;
            } else if (entry.getValue() == null) {
                if (value == null) {
                    return index;
                }
            } else if (entry.getValue().equals(value)) {
                return index;
            }
        }
        return -1;
    }
    /*
     * (non-Javadoc)
     * @see java.util.List#listIterator()
     */
    @Override
    public ListIterator<V> listIterator() {
        return new ListMapIterator(this);
    }
    /*
     * (non-Javadoc)
     * @see java.util.List#listIterator(int)
     */
    @Override
    public ListIterator<V> listIterator(int index) {
        return new ListMapIterator(this, index);
    }
    /**
     * Add or update entry
     * 
     * @see #put(K, V)
     * @param entry entry to add or update
     * @return the previous value
     */
    protected V put(Entry<K, V> entry) {
        ListMapEntry<V> old = keyMap.get(entry.getKey());
        if (old == null) {
            add(entry);
            return null;
        }
        return this.set(old.getIndex(), entry.getValue());
    }
    /**
     * Add or update a key-labeled entry.
     * 
     * <p>If the key exists, the index it uses is updated with the value.
     * This update will be seen by all keys using that index. If you want
     * to always add a new index instead of updating it, use 
     * {@link #add(Object, Object)} instead.</p>
     * 
     * @param key key to use
     * @param value value to use
     * @return previous value
     */
    public V put(K key, V value) {
        ListMapEntry<V> old = keyMap.get(key);
        if (old == null) {
            this.add(key, value);
            return null;
        }
        return this.set(old.getIndex(), value);
    }
    
    /**
     * Put all the values from an existing list map in a new order.
     * 
     * <p>This drops the references to the same index entries that were present
     * in the <code>old</code> ListMap. They all get promoted to their own
     * entries, all guaranteed to be in the same order described in 
     * <code>newOrder</code>.</p>
     * 
     * @param old old ListMap
     * @param newOrder the new order
     */
    public void putAll(ListMap<K, ? extends V> old, K[] newOrder) {
        for (K key : newOrder) {
            this.put(key, old.get(key));
        }
    }
    
    /**
     * Copy entries from an existing ListMap in a particular order.
     * 
     * <p>This copies the items in <code>order</code>, while keeping all
     * keys intact.</p>
     * 
     * @param m existing list map
     * @param order new order
     */
    @SuppressWarnings("unchecked")
    public void putAll(ListMap<K, V> m, int[] order) {
        Map<Integer, ConsolidatedListMapEntry<K, V>> clmes = 
            new HashMap<Integer, ConsolidatedListMapEntry<K, V>>();
        m.consolidateEntries(clmes);
        for (int idx : order) {
            if (idx < 0) {
                idx = m.size() + idx;
            }
            if (!clmes.containsKey(idx)) {
                throw new IndexOutOfBoundsException(String.format(
                 "requested index out of bounds: %d requested; size is only %d", //$NON-NLS-1$
                 idx, clmes.size()));
            }
            Integer oldidx = null;
            for (K key : clmes.get(idx).getKeys()) {
                if (this.containsKey(key)) {
                    oldidx = this.indexOfKey(key);
                    break;
                }
            }
            if (oldidx == null) {
                this.add((K[])clmes.get(idx).getKeys().toArray(),
                         clmes.get(idx).getValue());
            } else {
                set(oldidx, clmes.get(idx).getValue());
                putKey((K[])clmes.get(idx).getKeys().toArray(), oldidx);
            }
        }
    }
    
    /**
     * Put all entries from an existing map into this one.
     * 
     * This is similar to {@link Map#putAll(Map)}
     * 
     * @param m the existing map
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Put all entries from an existing map, using an explicit order.
     * 
     * <p>
     * Note that entries which are not in the <code>order</code> are not copied
     * to the new map. Also, this doesn't work with key aliases, as each item
     * in <code>order</code> gets a new entry in to index -- aliases are copied.
     * </p>
     * 
     * @param m existing map
     * @param order new order
     */
    public void putAll(Map<K, ? extends V> m, K[] order) {
        for (K key : order) {
            this.put(key, m.get(key));
        }
    }
    
    /**
     * Add a (possibly additional) key for an index.
     * 
     * @param key key to use
     * @param index index to use
     * @return previous index used (or -1)
     */
    public int putKey(K key, int index) {
        ListMapEntry<V> cur = valueList.get(index);
        ListMapEntry<V> old = keyMap.put(key, cur);
        if (old == null) return -1;
        return old.getIndex();
    }
    
    /**
     * Create another association to the same index entry
     * 
     * <p>This causes another key to be associated with the same index.
     * This means:</p>
     * <pre>
     *    ListMap<String, Integer> listmap = new ListMap<String, Integer>();
     *    listmap.add("a", 1);
     *    listmap.putKey("b", "a");
     *    listmap.put("b", 2);
     *    assert(listmap.get("a") == 2);
     * </pre>
     * 
     * @param newKey new key to create
     * @param existingKey existing key
     * @return index old index used by the alias
     */
    public int putKey(K newKey, K existingKey) {
        ListMapEntry<V> old = keyMap.put(newKey, keyMap.get(existingKey)); 
        if (old == null) {
            return -1;
        }
        return old.getIndex();
    }
    /**
     * Add a (possibly additional) key for an index.
     * 
     * @param keys keys to use
     * @param index index to use
     * @return true if change; false is not
     */
    public boolean putKey(K[] keys, int index) {
        if (keys.length == 0) {
            return false;
        }
        boolean changed = false;
        ListMapEntry<V> cur;
        ListMapEntry<V> old;
        cur = valueList.get(index);
        for (K key : keys) {
            old = keyMap.put(key, cur);
            if (old == null || old.getIndex() != index) {
                changed = true;
            }
        }
        return changed;
    }
    /**
     * Update the index used by the label system to match what is in place.
     * @param start starting index position
     */
    private void reindex(int start) {
        for (int i = start; i < this.valueList.size(); i++) {
            this.valueList.get(i).setIndex(i);
        }
    }
    
    /**
     * Entirely remove an index entry
     * 
     * <p>This first scans the key map and removes any entries referencing the
     * index entry and then removes the index entry itself, updating any of the
     * indexes which may be impacted by the change.</p>
     */
    @Override
    public V remove(int index) {
        if (index < 0 || index >= valueList.size()) {
            return null;
        }
        Set<Entry<K, ListMapEntry<V>>> entries = keyMap.entrySet();
        Iterator<Entry<K, ListMapEntry<V>>> i = entries.iterator();
        while(i.hasNext()) {
            Entry<K, ListMapEntry<V>> entry = i.next();
            if (entry == null) {
                // should never happen
                i.remove();
            } else if (entry.getValue() == null) {
                continue;
            } else if (entry.getValue().getIndex() == index) {
                i.remove();
            }
        }
        ListMapEntry<V> old = this.valueList.remove(index);
        if (index != valueList.size()) {
            reindex(index);
        }
        if (old == null) return null;
        return old.getValue();
    }

    /*
     * (non-Javadoc)
     * @see java.util.List#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object value) {
        int origSize = this.size();
        int index = indexOf(value);
        if (index == -1) return false;
        remove(index);
        return this.size() != origSize;
    }
    /*
     * (non-Javadoc)
     * @see java.util.List#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll(Collection<?> values) {
        int origSize = this.size();
        for (Object value : values) {
            remove(value);
        }
        return this.size() != origSize;
    }
    
    /**
     * Remove a key.
     * 
     * <p>This does not remove the index the key references, nor does it remove
     * any other keys or aliases using the same index.</p>
     * 
     * <p>To perform a complete removal, you can call {@link #remove(int)} on
     * the results of this function. (It will ignore indexes of -1.)</p>
     * 
     * @param key key to remove
     * @return index value used by the key or -1 if not found
     */
    public int removeKey(K key) {
        ListMapEntry<V> old = keyMap.remove(key);
        if (old == null) return -1;
        return old.getIndex();
    }
    /*
     * (non-Javadoc)
     * @see java.util.List#retainAll(java.util.Collection)
     */
    @Override
    public boolean retainAll(Collection<?> values) {
        if (values == null) {
            throw new NullPointerException("'values' parameter can not be null"); //$NON-NLS-1$
        }
        int oldSize = this.size();
        int index = 0;
        while (index < this.valueList.size()) {
            V value = this.valueList.get(index).getValue();
            if (values.contains(value)) {
                index++;
            } else {
                remove(index);
            }
        }
        return oldSize != this.size();
    }

    /**
     * Rotate a section of the list
     * 
     * @param startIdx index to start rotation
     * @param count the number of indexes to effect
     * @param dir the speed and direction of movement
     */
    public void rotate(int startIdx, int changeCount, int dir) {
        if (dir == 0) {
            return;
        }
        if (startIdx < 0) {
            startIdx = 0;
        }
        if (changeCount < 0) {
            changeCount = valueList.size() - startIdx;
        }
        if (dir > 0) {
            List<ListMapEntry<V>> t = this.valueList.subList(startIdx, startIdx+dir);
            List<ListMapEntry<V>> move = new ArrayList<>();
            move.addAll(t);
            t.clear();
            this.valueList.addAll(startIdx+changeCount-dir, move);
        } else {
            dir *= -1;
            List<ListMapEntry<V>> t = this.valueList.subList(startIdx + changeCount-dir, startIdx + changeCount);
            List<ListMapEntry<V>> move = new ArrayList<>();
            move.addAll(t);
            t.clear();
            this.valueList.addAll(startIdx, move);
        }
        for (int i = startIdx; i < startIdx + changeCount; i++) {
            this.valueList.get(i).setIndex(i);
        }
    }

    /**
     * Set the value of an index to a new value.
     * 
     * <p>Since we have both an index and one-or-more keys for a value,
     * it should be stated that when you change the value associated with
     * an index, all the labels that point to that index also see the new
     * value.</p>
     * 
     * @param index the index to change a value
     * @param value the new value to set
     * @return previous value (if any) at the index location
     */
    @Override
    public V set(int index, V value) {
        ListMapEntry<V> e = this.valueList.get(index);
        V oldValue = e.getValue();
        e.setValue(value);
        return oldValue;
    }
    /**
     * Returns the number of values in our list
     * 
     * <p>This is just the number of values. There may be more keys than 
     * this.</p>
     * 
     * @return the number of values (available via indexed access)
     */
    @Override
    public int size() {
        return this.valueList.size();
    }

    /*
     * (non-Javadoc)
     * @see java.util.List#subList(int, int)
     */
    @Override
    public List<V> subList(int start, int end) {
        if (start == -1 || end == -1) return null;
        ArrayList<V> ret = new ArrayList<V>();
        for (int i = start; i < end; i++) {
            ret.add(get(i));
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * @see java.util.List#toArray()
     */
    @Override
    public Object[] toArray() {
        return this.toArray(new Object[0]);
    }
    /*
     * (non-Javadoc)
     * @see java.util.List#toArray(T[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        T[] ret;
        if (size() == 0) {
            return a;
        }
        V sample = get(0);
        if (!a.getClass().getComponentType()
                .isAssignableFrom(sample.getClass())) {
            throw new ArrayStoreException();
        }
        if (a.length >= this.size()) {
            ret = a;
        } else {
            ret = Arrays.copyOf(a, this.size());
        }
        for (int i = 0; i < size(); i++) {
            ret[i] = (T) get(i);
        }
        return ret;
    }
    /**
     * Return a collection of just the values.
     * 
     * @return this is a collection of just the values.
     */
    public Collection<V> values() {
        return this;
    }

}
