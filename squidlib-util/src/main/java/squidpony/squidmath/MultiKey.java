package squidpony.squidmath;

import squidpony.ArrayTools;
import squidpony.annotation.Beta;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * An ordered multi-directional "map" that keeps 1 or more keysets organized so you can look up one key given a key in
 * another keyset, and do the same for the indices of keys. Does not have generic type parameters, which is needed
 * because we handle arbitrary counts of keysets, and each keyset could have its own type. You can use most of the
 * normal Map methods here, though they often need an int as their first argument, {@code which}, that specifies which
 * keyset the method applies to. For example, {@link #contains(int, Object)} checks for the presence of the second
 * parameter in the keyset specified by the first parameter. Adding items to a MultiKey uses {@link #put(Object...)},
 * and that does not take a {@code which} parameter because it needs to add an item to every keyset to succeed, and will
 * do nothing if the array or varargs passed to it has a different length than the {@link #keyCount} of the MultiKey.
 * Created by Tommy Ettinger on 10/23/2016.
 */
@Beta
@SuppressWarnings("unchecked")
public class MultiKey {
    public final int keyCount;
    Arrangement[] keys;

    /**
     * Constructs an empty MultiKey.
     */
    public MultiKey()
    {
        this(3, 16, 0.5f);
    }

    /**
     * Constructs a MultiKey with the expected number of indices to hold (the number of items in each keyset is always
     * the same, and this will be more efficient if expected is greater than that number).
     * @param expected how many items this should be ready to hold; can resize if needed
     */
    public MultiKey(int keyCount, int expected)
    {
        this(keyCount, expected, 0.5f);
    }

    /**
     * Constructs a MultiKey with the expected number of indices to hold (the number of items in each keyset is always
     * the same, and this will be more efficient if expected is greater than that number) and the load factor to use,
     * between 0.1f and 0.8f usually (using load factors higher than 0.8f can cause problems).
     * @param expected the amount of indices (the number of items in each keyset is always the same) this should hold
     * @param f the load factor, probably between 0.1f and 0.8f
     */
    public MultiKey(int keyCount, int expected, float f)
    {
        this.keyCount = keyCount;
        keys = new Arrangement[keyCount];
        for (int i = 0; i < keyCount; i++) {
            keys[i] = new Arrangement(expected, f);
        }
    }

    /**
     * Constructs a MultiKey from a Collection of Iterables that will be processed in tandem, adding a unique item from
     * each keyset in keysets if and only if it can also add a unique item from all other keysets, otherwise skipping
     * that iteration in each Iterable.
     * @param keysets a Collection of Iterable data structures, each containing items that should all be unique
     */
    public MultiKey(Collection<Iterable> keysets) {
        if (keysets == null) {
            keyCount = 0;
            keys = new Arrangement[0];
        } else {
            keyCount = keysets.size();
            keys = new Arrangement[keyCount];
            for (int i = 0; i < keyCount; i++) {
                keys[i] = new Arrangement();
            }
            putAll(keysets);
        }
    }

    public MultiKey(MultiKey other)
    {
        if(other == null)
        {
            keyCount = 0;
            keys = new Arrangement[0];
        }
        else
        {
            keyCount = other.keyCount;
            keys = new Arrangement[keyCount];
            for (int i = 0; i < keyCount; i++) {
                keys[i] = new Arrangement(other.keys[i]);
            }
        }
    }

    public MultiKey(Arrangement[] keysets)
    {
        if(keysets == null)
        {
            keyCount = 0;
            keys = new Arrangement[0];
        }
        else
        {
            keyCount = keysets.length;
            keys = new Arrangement[keyCount];
            int minLength = Integer.MAX_VALUE;
            for (int k = 0; k < keyCount; k++) {
                if(keysets[k] == null)
                    return;
                minLength = Math.min(minLength, keysets[k].size);
            }
            for (int k = 0; k < keyCount; k++) {
                keys[k] = keysets[k].take(minLength);
            }
        }
    }
    /**
     * Returns true if this contains key in the keyset specified by which.
     * @param which which keyset to check in
     * @param key the key to check the presence of
     * @return true if key is present in the keyset at which; false otherwise
     */
    public boolean contains(int which, Object key) {
        if(which >= 0 && which < keyCount)
            return keys[which].containsKey(key);
        return false;
    }

    /**
     * Returns true if index is between 0 (inclusive) and {@link #size()} (exclusive), or false otherwise.
     * @param index the index to check
     * @return true if index is a valid index in the ordering of this MultiKey
     */
    public boolean containsIndex(int index) {
        if(keyCount > 0) return keys[0].containsValue(index);
        return false;
    }

    /**
     * Given an index of a keyset (which) and an Object key, finds the position in the ordering that key has in the
     * keyset at which, or -1 if key is not present.
     * <br>
     * Unlike {@link java.util.List#indexOf(Object)}, this runs in constant time.
     * @param which which keyset to check in
     * @param key the key to find the position of
     * @return the int index of key in the ordering, or -1 if it is not present
     */
    public int indexOf(int which, Object key)
    {
        if(which >= 0 && which < keyCount)
            return keys[which].getInt(key);
        return -1;
    }

    /**
     * Given an index of a keyset (which) and an int index, finds the associated key in the keyset specified by which
     * (using index as a point in the ordering).
     * @param which which keyset to get from
     * @param index an int index into this MultiKey
     * @return the key (in the keyset found by which) with index for its position in the ordering, or null if index or which was invalid
     */
    public Object getAt(int which, int index)
    {
        if(which >= 0 && which < keyCount)
            return keys[which].keyAt(index);
        return null;
    }

    /**
     * Given an int index, finds the associated keys at all keysets (using index as a point in the ordering) and returns
     * them as a newly-allocated Object array.
     * @param index an int index into this MultiKey
     * @return the array of keys with index for their position in the ordering, or an array of null if index was invalid
     * @see #getAllAt(int, Object[]) getAllAt can avoid allocating a new array each time
     */
    public Object[] getAllAt(int index) {
        Object[] os = new Object[keyCount];
        for (int i = 0; i < keyCount; i++) {
            os[i] = keys[i].keyAt(index);
        }
        return os;
    }

    /**
     * Given an int index and an Object array to reuse, finds the associated keys at all keysets (using index as a point
     * in the ordering) and fills into with those keys, up to keyCount items.
     * @param index an int index into this MultiKey
     * @param into an Object array to reuse and fill with items; will be returned as-is if smaller than keyCount
     * @return the array of keys with index for their position in the ordering, or an array of null if index was invalid
     */
    public Object[] getAllAt(int index, Object[] into) {
        if (into != null && into.length >= keyCount) {
            for (int i = 0; i < keyCount; i++) {
                into[i] = keys[i].keyAt(index);
            }
        }
        return into;
    }

    /**
     * Given an index of the keyset to look up a key in (lookingUp), an index of the keyset to get from (getting), and
     * an Object key to look up (key), finds the Object key in the keyset specified by getting that is associated with
     * key in the keyset specified by lookingUp. key and the returned value will be at the same point in the ordering.
     * @param lookingUp which keyset to look up the {@code key} parameter in
     * @param getting which keyset to get a value from
     * @param key an object to use as a key, which should be the right type for the keyset at lookingUp
     * @return the object from getting's keyset that is associated with key, or null if key was not present
     */
    public Object getFrom(int lookingUp, int getting, Object key)
    {
        if(lookingUp >= 0 && lookingUp < keyCount && getting >= 0 && getting < keyCount)
            return keys[getting].keyAt(keys[lookingUp].getInt(key));
        return null;
    }

    /**
     * Gets a random key from the keyset specified by which using the given IRNG.
     * @param which which keyset to get a random key from
     * @param random generates a random index to get a key with
     * @return a randomly chosen key from the keyset specified, or null if this is empty
     */
    public Object randomKey(int which, IRNG random)
    {
        if(which >= 0 && which < keyCount)
            return keys[which].randomKey(random);
        return null;
    }
    /**
     * Gets a random key from a random keyset in this MultiKey using the given IRNG.
     * @param random generates a random keyset index and random item index, to get a random key
     * @return a randomly chosen Object key from possibly any keyset in this, or null if this is empty
     */
    public Object randomKey(IRNG random)
    {
        if(keyCount > 0)
            return keys[random.nextInt(keyCount)].randomKey(random);
        return null;
    }

    /**
     * In the keyset specified by {@code which}, changes an existing key, {@code past}, to another key, {@code future},
     * if past exists in that keyset and future does not yet exist in that keyset. This will retain past's point in the
     * ordering for future, so the associated other key(s) will still be associated in the same way.
     * @param which which keyset to alter the items in
     * @param past a key, that must exist in the keyset specified by which, and will be changed
     * @param future a key, that cannot currently exist in the keyset specified by which, but will if this succeeds
     * @return this for chaining
     */
    public MultiKey alter(int which, Object past, Object future)
    {
        if(which >= 0 && which < keyCount && keys[which].containsKey(past) && !keys[which].containsKey(future))
            keys[which].alter(past, future);
        return this;
    }
    /**
     * In the keyset specified by {@code which}, changes the key at {@code index} to another key, {@code future}, if
     * index is valid and future does not yet exist in that keyset. The position in the ordering for future will be the
     * same as index, and the same as the key this replaced, if this succeeds, so the other key(s) at that position will
     * still be associated in the same way.
     * @param which which keyset to alter the items in
     * @param index a position in the ordering to change; must be at least 0 and less than {@link #size()}
     * @param future a key, that cannot currently exist in the keyset specified by which, but will if this succeeds
     * @return this for chaining
     */
    public MultiKey alterAt(int which, int index, Object future)
    {
        if(which >= 0 && which < keyCount && !keys[which].containsKey(future) && index >= 0 && index < keys[which].size)
            keys[which].alter(keys[which].keyAt(index), future);
        return this;
    }

    /**
     * Adds a key to each keyset at the same point in the ordering (the end) of this MultiKey. The length of k must
     * match the keyCount of this MultiKey, and the nth item in k will go into the nth keyset. No item in k can be
     * present in the matching keyset in this before this is called. If you want to change or update an existing key,
     * use {@link #alter(int, Object, Object)} or {@link #alterAt(int, int, Object)}.
     * @param k an array or varargs of keys to add after the last index of this MultiKey; length must equal keyCount
     * @return true if this collection changed as a result of this call
     */
    public boolean put(Object... k)
    {
        if(k != null && keyCount > 0 && k.length == keyCount) {
            for (int i = 0; i < keyCount; i++) {
                if(keys[i].containsKey(k[i]))
                    return false;
            }
            for (int i = 0; i < keyCount; i++) {
                keys[i].add(k[i]);
            }
            return true;
        }
        return false;
    }

    /**
     * Goes through all Iterable items in {@code k} and adds their unique items into their corresponding keyset at the
     * end. If an item from the nth Iterable is already present in the nth keyset in this when this would add one, this
     * will not put any keys at that point in the iteration order, and will place the next unique group of items it
     * finds in the arguments at that position instead.
     * @param k a Collection of Iterable s of keys to add to their respective keysets; should all be unique (like a Set)
     * @return true if this collection changed as a result of this call
     */
    public boolean putAll(Collection<Iterable> k)
    {
        if(k == null || k.size() != keyCount) return false;
        boolean changed = false;
        Iterator[] its = new Iterator[keyCount];
        int idx = 0;
        for (Iterable it : k) {
            its[idx++] = it.iterator();
        }
        Object[] os = new Object[keyCount];
        while (true)
        {
            for (int i = 0; i < keyCount; i++) {
                if(!its[i].hasNext())
                    return changed;
                os[i] = its[i].next();
            }
            changed = put(os) || changed;
        }
    }
    /**
     * Puts all unique keys in {@code other} into this MultiKey, respecting other's ordering. If a key in other is
     * already present when this would add one, this will not put the keys at that point in the iteration
     * order, and will place the next unique items it finds in other at that position instead.
     * @param other another MultiKey collection with the same keyCount
     * @return true if this collection changed as a result of this call
     */
    public boolean putAll(MultiKey other)
    {
        if(other == null || other.keyCount != keyCount) return false;
        boolean changed = false;
        int sz = other.size();
        Object[] os = new Object[keyCount];
        for (int i = 0; i < sz; i++) {
            changed = put(other.getAllAt(i, os)) || changed;
        }
        return changed;
    }

    /**
     * Adds a key to each keyset at the given index in the ordering of this MultiKey. The length of k must
     * match the keyCount of this MultiKey, and the nth item in k will go into the nth keyset. No item in k can be
     * present in the matching keyset in this before this is called. If you want to change or update an existing key,
     * use {@link #alter(int, Object, Object)} or {@link #alterAt(int, int, Object)}.
     * @param k an array or varargs of keys to add after the last index of this MultiKey; length must equal keyCount
     * @return true if this collection changed as a result of this call
     */
    public boolean putAt(int index, Object... k)
    {
        if(k != null && keyCount > 0 && k.length == keyCount) {
            for (int i = 0; i < keyCount; i++) {
                if(keys[i].containsKey(k[i]))
                    return false;
            }
            for (int i = 0; i < keyCount; i++) {
                keys[i].addAt(index, k[i]);
            }
            return true;
        }
        return false;
    }

    /**
     * Removes a given Object key from the keyset specified by which, if {@code removing} exists in that keyset, and
     * also removes any keys associated with its point in the ordering.
     * @param which which keyset to remove the item from; if {@code removing} isn't in that keyset, this does nothing
     * @param removing the key to remove
     * @return this for chaining
     */
    public MultiKey remove(int which, Object removing)
    {
        if(which >= 0 && which < keyCount) {
            int i = keys[which].removeInt(removing);
            if(i >= 0) {
                for (int j = 0; j < keyCount; j++) {
                    if(j != which)
                        keys[j].removeAt(i);
                }
            }
        }
        return this;
    }

    /**
     * Removes a given point in the ordering, if {@code index} is at least 0 and less than {@link #size()}.
     * @param index the position in the ordering to remove
     * @return this for chaining
     */
    public MultiKey removeAt(int index)
    {
        if(index >= 0 && keyCount > 0 && index < keys[0].size) {
            for (int i = 0; i < keyCount; i++) {
                keys[i].removeAt(index);
            }
        }
        return this;
    }

    /**
     * Reorders this MultiKey using {@code ordering}, which have the same length as this MultiKey's {@link #size()}
     * and can be generated with {@link ArrayTools#range(int)} (which, if applied, would produce no
     * change to the current ordering), {@link IRNG#randomOrdering(int)} (which gives a random ordering, and if
     * applied immediately would be the same as calling {@link #shuffle(IRNG)}), or made in some other way. If you
     * already have an ordering and want to make a different ordering that can undo the change, you can use
     * {@link ArrayTools#invertOrdering(int[])} called on the original ordering.
     * @param ordering an int array or vararg that should contain each int from 0 to {@link #size()} (or less)
     * @return this for chaining
     */
    public MultiKey reorder(int... ordering)
    {
        if(ordering != null) {
            for (int i = 0; i < keyCount; i++) {
                keys[i].reorder(ordering);
            }
        }
        return this;
    }

    /**
     * Generates a random ordering with rng and applies the same ordering to all kinds of keys this has; they will
     * maintain their current association to other keys but their ordering/indices will change.
     * @param rng an IRNG to produce the random ordering this will use
     * @return this for chaining
     */
    public MultiKey shuffle(IRNG rng)
    {
        if(keyCount > 0) {
            int[] ordering = rng.randomOrdering(keys[0].size);
            for (int i = 0; i < keyCount; i++) {
                keys[i].reorder(ordering);
            }
        }
        return this;
    }

    /**
     * Creates a new iterator over the keys this holds in the keyset specified by which. This can be problematic for
     * garbage collection if called very frequently; it may be better to access items by index (which also lets you
     * access other keys associated with that index) using {@link #getAt(int, int)} in a for(int i=0...) loop.
     * @return a newly-created iterator over this MultiKey's keys in the specified keyset
     */
    public Iterator iterator(int which)
    {
        if(which >= 0 && which < keyCount)
            return keys[which].iterator();
        return null;
    }

    /**
     * Gets and caches the keys in the keyset specified by which as a Collection that implements SortedSet (and so also
     * implements Set).
     * @param which which keyset to get as a separate value
     * @return the keys from the keyset specified by which, as a SortedSet
     */
    public SortedSet getSet(int which) {
        if(which >= 0 && which < keyCount)
            return keys[which].keySet();
        return null;
    }

    /**
     * To be called sparingly, since this allocates a new OrderedSet instead of reusing one.
     * @param which which keyset to get as a separate value
     * @return the keys from the keyset specified by which, as an OrderedSet
     */
    public OrderedSet getOrderedSet(int which) {
        if(which >= 0 && which < keyCount)
            return keys[which].keysAsOrderedSet();
        return null;

    }

    public int keyCount() {
        return keyCount;
    }
    public int valueCount() {
        return 0;
    }

    public int size() {
        return (keyCount > 0) ? keys[0].size : 0;
    }

    public boolean isEmpty()
    {
        return keyCount > 0 && keys[0].size > 0;
    }

}
