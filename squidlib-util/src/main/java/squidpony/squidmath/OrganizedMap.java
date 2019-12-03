package squidpony.squidmath;

import squidpony.annotation.Beta;

import java.io.Serializable;
import java.util.*;

/**
 * Work in progress Map that can be split up into separate or overlapping iteration orders; it's probably easier and
 * more full-featured to manage an iteration order as an {@link OrderedSet} of keys and fetch them from any kind of
 * larger Map with the same type of keys. The larger Map could work well as a HashMap. This class is not recommended.
 * <br>
 * Created by Tommy Ettinger on 11/29/2019.
 */
@Beta
public class OrganizedMap<K, V> implements Map<K, V>, Serializable {
    private static final long serialVersionUID = 0L;
    protected HashMap<K, V> internalMap;
    protected OrderedSet<K> order;

    private SharedEntries entries;
    private SharedKeySet keySet;
    private SharedValues values;

    public OrganizedMap()
    {
        this(16, 0.75f);
    }

    public OrganizedMap(int capacity)
    {
        this(capacity, 0.75f);
    }

    public OrganizedMap(int capacity, float loadFactor)
    {
        internalMap = new HashMap<>(capacity, loadFactor);
        order = new OrderedSet<>(capacity, loadFactor * 0.5f);
    }
    
    public OrganizedMap(Map<? extends K, ? extends V> other)
    {
        this(other.size(), 0.75f);
        putAll(other);
    }
    
    public int size() {
        return order.size();
    }

    public boolean isEmpty() {
        return order.isEmpty();
    }

    public boolean containsKey(Object key) {
        return internalMap.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return internalMap.containsValue(value);
    }

    public V get(Object key) {
        return internalMap.get(key);
    }

    public V put(K key, V value) {
        if(!internalMap.containsKey(key))
            order.add(key);
        return internalMap.put(key, value);
    }

    public V remove(Object key) {
        order.remove(key);
        return internalMap.remove(key);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        for(Entry<? extends K, ? extends V> ent : m.entrySet())
            put(ent.getKey(), ent.getValue());
    }

    public void clear() {
        order.clear();
        internalMap.clear();
    }

    public class SharedKeySet extends AbstractSet<K> implements Serializable {
        private static final long serialVersionUID = 0L;

        public OrderedSet<K> order;

        public SharedKeySet()
        {
            order = OrganizedMap.this.order;
        }
        
        public SharedKeySet(OrderedSet<K> order)
        {
            this.order = order;
        }

        public class SharedKeyIterator implements Iterator<K>, Serializable
        {
            private static final long serialVersionUID = 0L;
            
            public int current;
            
            public SharedKeyIterator()
            {
                current = 0;
            }
            
            @Override
            public boolean hasNext() {
                return current < order.size;
            }

            @Override
            public K next() {
                return order.getAt(current++);
            }

            @Override
            public void remove() {
                final K k = order.getAt(current);
                internalMap.remove(k);
                order.removeAt(current);
                OrganizedMap.this.order.remove(k);
            }
        }
        
        @Override
        public SharedKeyIterator iterator() {
            return new SharedKeyIterator();
        }

        @Override
        public int size() {
            return order.size;
        }
    }
    public SharedKeySet keySet() {
        if(keySet == null)
            keySet = new SharedKeySet();
        return keySet;
    }

    public SharedKeySet keySet(OrderedSet<K> alternateOrder) {
        return new SharedKeySet(alternateOrder);
    }

    public class SharedValues extends AbstractCollection<V> implements Serializable {
        private static final long serialVersionUID = 0L;
        public OrderedSet<K> order;

        public SharedValues()
        { 
            order = OrganizedMap.this.order;
        }

        public SharedValues(OrderedSet<K> order)
        {
            this.order = order;
        }

        public class SharedValueIterator implements Iterator<V>, Serializable  {
            private static final long serialVersionUID = 0L;

            public int current;

            public SharedValueIterator()
            {
                current = 0;
            }

            @Override
            public boolean hasNext() {
                return current < order.size;
            }

            @Override
            public V next() {
                return internalMap.get(order.getAt(current++));
            }

            @Override
            public void remove() {
                final K k = order.getAt(current);
                internalMap.remove(k);
                order.removeAt(current);
                OrganizedMap.this.order.remove(k);
            }
        }

        @Override
        public SharedValueIterator iterator() {
            return new SharedValueIterator();
        }

        @Override
        public int size() {
            return order.size;
        }
    }
    
    public SharedValues values() {
        if(values == null)
            values = new SharedValues();
        return values;
    }

    public SharedValues values(OrderedSet<K> alternateOrder) {
        return new SharedValues(alternateOrder);
    }

    public class MapEntry implements Map.Entry<K, V>, Serializable {
        private static final long serialVersionUID = 0L;

        public K key;
        public MapEntry(){}
        public MapEntry(K k)
        {
            key = k;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return internalMap.get(key);
        }

        @Override
        public V setValue(V value) {
            return put(key, value);
        }
    }
    public class SharedEntries extends AbstractSet<Entry<K, V>> implements Serializable {
        private static final long serialVersionUID = 0L;

        public OrderedSet<K> order;

        public SharedEntries()
        {
            order = OrganizedMap.this.order;
        }

        public SharedEntries(OrderedSet<K> order)
        {
            this.order = order;
        }

        public class SharedEntryIterator implements Iterator<Entry<K, V>>, Serializable
        {
            private static final long serialVersionUID = 0L;

            public int current;

            public SharedEntryIterator()
            {
                current = 0;
            }

            @Override
            public boolean hasNext() {
                return current < order.size;
            }

            @Override
            public Entry<K, V> next() {
                return new MapEntry(order.getAt(current++));
            }

            @Override
            public void remove() {
                final K k = order.getAt(current);
                internalMap.remove(k);
                order.removeAt(current);
                OrganizedMap.this.order.remove(k);
            }
        }

        @Override
        public SharedEntryIterator iterator() {
            return new SharedEntryIterator();
        }

        @Override
        public int size() {
            return order.size;
        }
    }

    public SharedEntries entrySet() {
        if(entries == null)
            entries = new SharedEntries();
        return entries;
    }

    public SharedEntries entrySet(OrderedSet<K> alternateOrder) {
        return new SharedEntries(alternateOrder);
    }

    public V getOrDefault(Object key, V defaultValue) {
        if(internalMap.containsKey(key))
            return internalMap.get(key);
        return defaultValue;
    }

    public K keyAt(int index)
    {
        return order.getAt(index);
    }

    public V getAt(int index)
    {
        return internalMap.get(order.getAt(index));
    }

    public Entry<K, V> entryAt(int index)
    {
        return new MapEntry(order.getAt(index));
    }

    public V removeAt(int index)
    {
        return remove(order.getAt(index));
    }


}
