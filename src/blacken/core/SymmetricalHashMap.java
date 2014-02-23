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
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A HashMap where searching by value is as fast as searching by key.
 * 
 * <p>This normally functions like a Map, but every function that accepts a key
 * has an equivalent function that accepts a value -- and is just as fast.</p>
 * 
 * <p>This is implemented with a pair of {@link HashMap}'s, so if you're worried
 * about memory consumption, this may be a problem.</p>
 * 
 * @author yam655
 * @param <K> key type
 * @param <V> value type
 */
public class SymmetricalHashMap<K, V> implements Map<K, V>, Cloneable, Serializable {
    private static final long serialVersionUID = -4760600560495552929L;
    private Map<K, V> kMap;
    private Map<V, K> vMap;

    /**
     * Create an empty SymmetricalHashMap.
     */
    public SymmetricalHashMap() {
        kMap = new HashMap<K, V>();
        vMap = new HashMap<V, K>();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public SymmetricalHashMap<K, V> clone() throws CloneNotSupportedException {
        SymmetricalHashMap<K, V> ret = (SymmetricalHashMap<K, V>) super.clone();
        ret.kMap = new HashMap<K, V>(kMap);
        ret.kMap = new HashMap<K, V>(kMap);
        return ret;
    }
    
    /**
     * Create a new map based upon an existing map.
     * 
     * @param existingMap existing map
     */
    public SymmetricalHashMap(Map<? extends K, ? extends V> existingMap) {
        kMap = new HashMap<K, V>();
        vMap = new HashMap<V, K>();
        this.putAll(existingMap);
    }
    
    /*
     * (non-Javadoc)
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        kMap.clear();
        vMap.clear();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object arg0) {
        return kMap.containsKey(arg0);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object arg0) {
        return vMap.containsKey(arg0);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return kMap.entrySet();
    }

    /**
     * This is like {@link #entrySet()}, but V, K instead of K, V.
     * 
     * @return inverted entry set
     */
    public Set<Entry<V, K>> inverseEntrySet() {
        return vMap.entrySet();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public V get(Object key) {
        return kMap.get(key);
    }

    /**
     * Alias for {@link #get(Object)}
     * 
     * <p>This exists so {@link #getKey(Object)} has a symmetrical 
     * function.</p>
     * 
     * @param key key to fetch
     * @return value stored in the key
     */
    public V getValue(Object key) {
        return kMap.get(key);
    }
    
    /**
     * Get the key associated with a value
     * 
     * <p>This is guaranteed to be as fast as a standard {@link #get(Object)}</p>
     * 
     * @param value value to search
     * @return key for the value
     */
    public K getKey(Object value) {
        return vMap.get(value);
    }
    
    /*
     * (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return kMap.isEmpty();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<K> keySet() {
        return kMap.keySet();
    }

    /**
     * Return the set containing the values.
     * 
     * <p>This is the symmetrical version of {@link #keySet()}.</p>
     * 
     * @return value set
     */
    public Set<V> valueSet() {
        return vMap.keySet();
    }
    
    /*
     * (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public V put(K key, V value) {
        return putValue(key, value);
    }

    /**
     * Try to put the key and value, overriding an old value or key.
     * 
     * <p>Fails (with IllegalArgumentException) when both the key and value 
     * are in the map but not already assigned to each other.</p>
     * 
     * <p>This will overwrite an existing value or an existing key.</p>
     * 
     * <p>If both the key and value already exists, they need to be assigned 
     * to each other or an IllegalArgumentException is raised. Handling that 
     * condition would require overwriting two different entries, meaning the 
     * size of the map would be reduced by one, and the "return old values" 
     * logic needs to handle returning a pair of Entries.)<p>
     * 
     * @param key key to insert
     * @param value value to insert
     * @return null if no previous value, otherwise key/value of old value
     */
    public Entry<K, V> fuzzyPut(K key, V value) {
        Entry<K, V> ret = null;
        V oldValue = null;
        K oldKey = null;
        if (kMap.containsKey(key) && vMap.containsKey(value)) {
            if (kMap.get(key).hashCode() != value.hashCode()) {
                throw new IllegalArgumentException(
                       "key and value exist in the map with different relations"); //$NON-NLS-1$
            }
            oldValue = kMap.put(key, value);
            oldKey = vMap.put(value, key); // oldKey == key
            ret = new AbstractMap.SimpleEntry<K, V>(oldKey, oldValue);
        } else if (kMap.containsKey(key)) {
            oldValue = kMap.put(key, value);
            oldKey = vMap.remove(oldValue); // oldKey == key
            vMap.put(value, key);
            ret = new AbstractMap.SimpleEntry<K, V>(oldKey, oldValue);
        } else if (vMap.containsKey(key)) {
            oldKey = vMap.put(value, key);
            oldValue = kMap.remove(oldKey); // oldValue == value
            kMap.put(key, value);
            ret = new AbstractMap.SimpleEntry<K, V>(oldKey, oldValue);
        } else if (key == null || value == null) {
            throw new NullPointerException("neither key nor value can be null"); //$NON-NLS-1$
        } else {
            kMap.put(key, value);
            vMap.put(value, key);
        }
        return ret;
    }
    
    /**
     * Put a value, updating the key if needed.
     * 
     * <p>This is the inverse of {@link #put(Object, Object)} or 
     * {@link #putValue(Object, Object)}.</p>
     * 
     * @param value value to update
     * @param key new key value
     * @return old key value
     */
    public K putKey(V value, K key) {
        if (kMap.containsKey(key) && vMap.containsKey(value)) {
            if (kMap.get(key).hashCode() != value.hashCode()) {
                throw new IllegalArgumentException(
                       "key and value exist in the map with different relations"); //$NON-NLS-1$
            }
            K oldKey = vMap.put(value, key);
            kMap.put(key, value);
            return oldKey;
        }
        return null;
    }
    
    /**
     * Put a key, updating the value.
     * 
     * <p>This is normally called {@link #put(Object, Object)}, but we have this
     * name to be symmetrical with {@link #putKey(Object, Object)}.</p>
     * 
     * @param key new or existing key value
     * @param value an updated value
     * @return previous value for <code>key</code>
     */
    public V putValue(K key, V value) {
        if (kMap.containsKey(key) && vMap.containsKey(value)) {
            if (kMap.get(key).hashCode() != value.hashCode()) {
                throw new IllegalArgumentException(
                     "key and value exist in the map with different relations"); //$NON-NLS-1$
            }
            V oldValue = kMap.put(key, value);
            vMap.put(value, key);
            return oldValue;
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }
    /**
     * An inverse version of {@link #putAll(Map)}.
     * 
     * <p>Note that the passed in map are expected to be in the opposite order
     * to the current map. This does require that both the keys and values are
     * unique or the resulting map will be unexpected.</p>
     * 
     * @param map mappings to be stored in this map
     */
    public void inversePutAll(Map<? extends V, ? extends K> map) {
        for (Entry<? extends V, ? extends K> e : map.entrySet()) {
            putKey(e.getKey(), e.getValue());
        }
    }
    /**
     * Put all the entries, existing keys are overwritten with new value.
     * 
     * <p>This would normally be called {@link #put(Object, Object)} but to be
     * symmetrical with {@link #putAllKey(Map)} we have this name, too.</p>
     * 
     * @param map existing map
     */
    public void putAllValue(Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            putValue(e.getKey(), e.getValue());
        }
    }
    
    /**
     * Put all the entries, existing values are overwritten with new keys.
     * 
     * <p>This is the inverse of {@link #put(Object, Object)} 
     * and {@link #putAllValue(Map)}.</p>
     * 
     * @param map existing map
     */
    public void putAllKey(Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            putKey(e.getValue(), e.getKey());
        }
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public V remove(Object key) {
        if (kMap.containsKey(key)) {
            V ret = kMap.remove(key);
            vMap.remove(ret);
            return ret;
        }
        return null;
    }
    
    /**
     * Alias for {@link #remove(Object)}.
     * 
     * <p>This exists to be symmetrical with {@link #removeValue(Object)}.</p>
     * 
     * @param key key to remove
     * @return previous value
     */
    public V removeKey(Object key) {
        return remove(key);
    }
    /**
     * Remove a value and the associated key.
     * 
     * <p>This is the symmetrical version of {@link #removeKey(Object)} and
     * {@link #remove(Object)}.</p>
     * 
     * @param value value to remove
     * @return previous key associated with it.
     */
    public K removeValue(Object value) {
        if (vMap.containsKey(value)) {
            K ret = vMap.remove(value);
            kMap.remove(ret);
            return ret;
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return kMap.size();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#values()
     */
    @Override
    public Collection<V> values() {
        return this.valueSet();
    }

    /**
     * Added to be symmetrical with values()
     * 
     * This is the same as {@link #keySet()}.
     * 
     * @return all current keys
     */
    public Collection<K> keys() {
        return this.keySet();
    }
    
}
