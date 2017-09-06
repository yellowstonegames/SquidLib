package squidpony.squidmath;

import squidpony.annotation.Beta;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Mostly just a way to have an EnumMap-like class that can be serialized reasonably. Should be usable for anything an
 * EnumMap is usable for. Serialization is possible with this, like EnumMap can be serialized in squidlib-extra, but
 * this still needs some hack-y techniques.
 * Created by Tommy Ettinger on 9/5/2017.
 */
@Beta
public class EnumMapPlus<K extends Enum<K>, V> implements Map<K, V> {
    public final K[] keys;
    public final Class<K> keyType;
    public final V[] vals;
    public int size;
    /**
     * Creates an empty enum map with the specified key type.
     *
     * @param keyType the class object of the key type for this enum map
     * @throws NullPointerException if <tt>keyType</tt> is null
     */
    @SuppressWarnings("unchecked")
    public EnumMapPlus(Class<K> keyType) {
        this.keyType = keyType;
        keys = keyType.getEnumConstants();
        vals = (V[]) new Object[keys.length];
    }

    /**
     * Creates an enum map with the same key type as the specified enum
     * map, initially containing the same mappings (if any).
     *
     * @param m the enum map from which to initialize this enum map
     * @throws NullPointerException if <tt>m</tt> is null
     */
    public EnumMapPlus(EnumMapPlus<K, ? extends V> m) {
        this(m.keyType);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size <= 0;
    }

    public int validate(Object key) {
        if(key != null && (key instanceof Enum) && ((Enum)key).getDeclaringClass() == keyType.getDeclaringClass())
        {
            int o = ((Enum)key).ordinal();
            return (o >= 0 && o < keys.length) ? o : -1;
        }
        return -1;
    }

    @Override
    public boolean containsKey(Object key) {
        int v = validate(key);
        return v != -1 && vals[v] != null;
    }

    @Override
    public boolean containsValue(Object value) {
        if(value == null)
            return false;
        int len = keys.length;
        for (int i = 0; i < len; i++) {
            if(value.equals(vals[i]))
                return true;
        }
        return false;
    }

    @Override
    public V get(Object key) {
        int v = validate(key);
        return (v != -1) ? vals[v] : null;
    }

    @Override
    public V put(K key, V value) {
        if(key == null || value == null)
            return null;
        int ord = key.ordinal();
        V old = vals[ord];
        if(old == null)
            size++;
        vals[ord] = value;
        return old;
    }
    public V putAt(int place, V value) {
        if(value == null || place < 0 || place >= keys.length)
            return null;
        V old = vals[place];
        if(old == null)
            size++;
        vals[place] = value;
        return old;
    }

    @Override
    public V remove(Object key) {
        int v = validate(key);
        if(v != -1)
        {
            vals[v] = null;
            size--;
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for(Entry<? extends K, ? extends V> kv : m.entrySet())
        {
            put(kv.getKey(), kv.getValue());
        }
    }

    @Override
    public void clear() {
        Arrays.fill(vals, null);
        size = 0;
    }

    @Override
    public OrderedSet<K> keySet() {
        OrderedSet<K> k = new OrderedSet<>(size);
        int len = keys.length;
        for (int i = 0, s = 0; s < size && i < len; i++) {
            if(vals[i] != null)
            {
                k.add(keys[i]);
                s++;
            }
        }
        return k;
    }

    @Override
    public ArrayList<V> values() {
        ArrayList<V> v = new ArrayList<>(size);
        int len = keys.length;
        V vl;
        for (int i = 0, s = 0; s < size && i < len; i++) {
            if((vl = vals[i]) != null)
            {
                v.add(vl);
                s++;
            }
        }
        return v;
    }

    @Override
    public OrderedSet<Entry<K, V>> entrySet() {
        OrderedSet<Entry<K, V>> kv = new OrderedSet<>(size);
        int len = keys.length;
        V v;
        for (int i = 0, s = 0; s < size && i < len; i++) {
            if((v = vals[i]) != null)
            {
                kv.add(new AbstractMap.SimpleEntry<>(keys[i], v));
                s++;
            }
        }
        return kv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnumMapPlus<?, ?> that = (EnumMapPlus<?, ?>) o;

        if (size != that.size) return false;
        if (!keyType.equals(that.keyType)) return false;
        final int len = vals.length;
        V v;
        for (int i = 0; i < len; i++) {
            if((v = vals[i]) != null && !v.equals(that.vals[i]))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = CrossHash.hash(keys);
        result = 31 * result + keyType.hashCode();
        result = 31 * result + CrossHash.hash(vals);
        result = 31 * result + size;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(size * 32);
        int len = keys.length;
        V v;
        sb.append('{');
        for (int i = 0, s = 0; s < size && i < len; i++) {
            if((v = vals[i]) != null)
            {
                sb.append(keys[i]).append("=>").append(v);
                if(++s < size)
                    sb.append(", ");
            }
        }
        return sb.append('}').toString();
    }
}
