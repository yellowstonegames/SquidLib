package squidpony;

import squidpony.squidmath.Arrangement;
import squidpony.squidmath.K2;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Utility methods for more easily constructing data structures, particularly those in Java's standard library.
 * All static methods and inner classes; meant to be imported with {@code import static squidpony.Maker.*}.
 * Created by Tommy Ettinger on 5/19/2016.
 */
public class Maker {

    /**
     * Stores any information relating to non-fatal issues, such as caught and handled Exceptions that still change the
     * behavior of methods. Typically shouldn't be cleared while debugging, since it could be useful later on, and
     * hopefully won't need to be written to in a release build.
     */
    public static final StringBuilder issueLog = new StringBuilder(1024);
    /**
     * Makes a LinkedHashMap (LHM) with key and value types inferred from the types of k0 and v0, and considers all
     * parameters key-value pairs, casting the Objects at positions 0, 2, 4... etc. to K and the objects at positions
     * 1, 3, 5... etc. to V. If rest has an odd-number length, then it discards the last item. If any pair of items in
     * rest cannot be cast to the correct type of K or V, then this inserts nothing for that pair and logs information
     * on the problematic pair to the static Maker.issueLog field.
     * @param k0 the first key; used to infer the types of other keys if generic parameters aren't specified.
     * @param v0 the first value; used to infer the types of other values if generic parameters aren't specified.
     * @param rest an array or vararg of keys and values in pairs; should contain alternating K, V, K, V... elements
     * @param <K> the type of keys in the returned LinkedHashMap; if not specified, will be inferred from k0
     * @param <V> the type of values in the returned LinkedHashMap; if not specified, will be inferred from v0
     * @return a freshly-made LinkedHashMap with K keys and V values, using k0, v0, and the contents of rest to fill it
     */
    @SuppressWarnings("unchecked")
    public static <K, V> LinkedHashMap<K, V> makeLHM(K k0, V v0, Object... rest)
    {
        if(rest == null)
            return makeLHM(k0, v0);
        LinkedHashMap<K, V> lhm = new LinkedHashMap<>(1 + (rest.length / 2));
        lhm.put(k0, v0);

        for (int i = 0; i < rest.length - 1; i+=2) {
            try {
                lhm.put((K) rest[i], (V) rest[i + 1]);
            }catch (ClassCastException cce) {
                issueLog.append("makeLHM call had a casting problem with pair at rest[");
                issueLog.append(i);
                issueLog.append("] and/or rest[");
                issueLog.append(i + 1);
                issueLog.append("], with contents: ");
                issueLog.append(rest[i]);
                issueLog.append(", ");
                issueLog.append(rest[i+1]);
                issueLog.append(".\n\nException messages:\n");
                issueLog.append(cce);
                String msg = cce.getMessage();
                if (msg != null) {
                    issueLog.append('\n');
                    issueLog.append(msg);
                }
                issueLog.append('\n');
            }
        }
        return lhm;
    }

    /**
     * Makes an empty LinkedHashMap (LHM); needs key and value types to be specified in order to work. For an empty
     * LinkedHashMap with String keys and Coord values, you could use {@code Maker.<String, Coord>makeLHM();}. Using
     * the new keyword is probably just as easy in this case; this method is provided for completeness relative to
     * makeLHM() with 2 or more parameters.
     * @param <K> the type of keys in the returned LinkedHashMap; cannot be inferred and must be specified
     * @param <V> the type of values in the returned LinkedHashMap; cannot be inferred and must be specified
     * @return an empty LinkedHashMap with the given key and value types.
     */
    public static <K, V> LinkedHashMap<K, V> makeLHM()
    {
        return new LinkedHashMap<>();
    }

    /**
     * Makes an ArrayList of T given an array or vararg of T elements.
     * @param elements an array or vararg of T
     * @param <T> just about any non-primitive type
     * @return a newly-allocated ArrayList containing all of elements, in order
     */
    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> makeList(T... elements) {
        if(elements == null) return null;
        ArrayList<T> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    /**
     * Makes a LinkedHashSet (LHS) of T given an array or vararg of T elements. Duplicate items in elements will have
     * all but one item discarded, using the later item in elements.
     * @param elements an array or vararg of T
     * @param <T> just about any non-primitive type
     * @return a newly-allocated LinkedHashSet containing all of the non-duplicate items in elements, in order
     */
    @SuppressWarnings("unchecked")
    public static <T> LinkedHashSet<T> makeLHS(T... elements) {
        if(elements == null) return null;
        LinkedHashSet<T> set = new LinkedHashSet<>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    /**
     * Makes an OrderedMap (OM) with key and value types inferred from the types of k0 and v0, and considers all
     * parameters key-value pairs, casting the Objects at positions 0, 2, 4... etc. to K and the objects at positions
     * 1, 3, 5... etc. to V. If rest has an odd-number length, then it discards the last item. If any pair of items in
     * rest cannot be cast to the correct type of K or V, then this inserts nothing for that pair and logs information
     * on the problematic pair to the static Maker.issueLog field.
     * @param k0 the first key; used to infer the types of other keys if generic parameters aren't specified.
     * @param v0 the first value; used to infer the types of other values if generic parameters aren't specified.
     * @param rest an array or vararg of keys and values in pairs; should contain alternating K, V, K, V... elements
     * @param <K> the type of keys in the returned OrderedMap; if not specified, will be inferred from k0
     * @param <V> the type of values in the returned OrderedMap; if not specified, will be inferred from v0
     * @return a freshly-made OrderedMap with K keys and V values, using k0, v0, and the contents of rest to fill it
     */
    @SuppressWarnings("unchecked")
    public static <K, V> OrderedMap<K, V> makeOM(K k0, V v0, Object... rest)
    {
        return makeOM(0.625f, k0, v0, rest);
    }
    /**
     * Makes an OrderedMap (OM) with the given load factor (which should be between 0.1 and 0.9), key and value types
     * inferred from the types of k0 and v0, and considers all remaining parameters key-value pairs, casting the Objects
     * at positions 0, 2, 4... etc. to K and the objects at positions 1, 3, 5... etc. to V. If rest has an odd-number
     * length, then it discards the last item. If any pair of items in rest cannot be cast to the correct type of K or
     * V, then this inserts nothing for that pair and logs information on the problematic pair to the static
     * Maker.issueLog field.
     * @param factor the load factor; should be between 0.1 and 0.9, and 0.75f is a safe choice
     * @param k0 the first key; used to infer the types of other keys if generic parameters aren't specified.
     * @param v0 the first value; used to infer the types of other values if generic parameters aren't specified.
     * @param rest an array or vararg of keys and values in pairs; should contain alternating K, V, K, V... elements
     * @param <K> the type of keys in the returned OrderedMap; if not specified, will be inferred from k0
     * @param <V> the type of values in the returned OrderedMap; if not specified, will be inferred from v0
     * @return a freshly-made OrderedMap with K keys and V values, using k0, v0, and the contents of rest to fill it
     */
    @SuppressWarnings("unchecked")
    public static <K, V> OrderedMap<K, V> makeOM(float factor, K k0, V v0, Object... rest)
    {
        if(rest == null)
            rest = new Object[0];
        OrderedMap<K, V> om = new OrderedMap<>(1 + (rest.length / 2), factor);
        om.put(k0, v0);

        for (int i = 0; i < rest.length - 1; i+=2) {
            try {
                om.put((K) rest[i], (V) rest[i + 1]);
            }catch (ClassCastException cce) {
                issueLog.append("makeOM call had a casting problem with pair at rest[");
                issueLog.append(i);
                issueLog.append("] and/or rest[");
                issueLog.append(i + 1);
                issueLog.append("], with contents: ");
                issueLog.append(rest[i]);
                issueLog.append(", ");
                issueLog.append(rest[i+1]);
                issueLog.append(".\n\nException messages:\n");
                issueLog.append(cce);
                String msg = cce.getMessage();
                if (msg != null) {
                    issueLog.append('\n');
                    issueLog.append(msg);
                }
                issueLog.append('\n');
            }
        }
        return om;
    }

    /**
     * Makes an empty OrderedMap (OM); needs key and value types to be specified in order to work. For an empty
     * OrderedMap with String keys and Coord values, you could use {@code Maker.<String, Coord>makeOM();}. Using
     * the new keyword is probably just as easy in this case; this method is provided for completeness relative to
     * makeOM() with 2 or more parameters.
     * @param <K> the type of keys in the returned OrderedMap; cannot be inferred and must be specified
     * @param <V> the type of values in the returned OrderedMap; cannot be inferred and must be specified
     * @return an empty OrderedMap with the given key and value types.
     */
    public static <K, V> OrderedMap<K, V> makeOM()
    {
        return new OrderedMap<>();
    }

    /**
     * Makes a OrderedSet (OS) of T given an array or vararg of T elements. Duplicate items in elements will have
     * all but one item discarded, using the later item in elements.
     * @param elements an array or vararg of T
     * @param <T> just about any non-primitive type
     * @return a newly-allocated OrderedSet containing all of the non-duplicate items in elements, in order
     */
    @SuppressWarnings("unchecked")
    public static <T> OrderedSet<T> makeOS(T... elements) {
        if(elements == null) return null;
        return new OrderedSet<>(elements);
    }

    /**
     * Makes a Arrangement (Arrange) of T given an array or vararg of T elements. Duplicate items in elements will have
     * all but one item discarded, using the later item in elements. As is always the case with Arrangement, each item
     * will be mapped bi-directionally to its index in the iteration order, and an item can be retrieved with
     * {@link Arrangement#keyAt(int)} if you only know its index, or the int index for an item can be retrieved with
     * {@link Arrangement#getInt(Object)} if you only have an item.
     * @param elements an array or vararg of T
     * @param <T> just about any non-primitive type
     * @return a newly-allocated Arrangement containing all of the non-duplicate items in elements, in order
     */
    @SuppressWarnings("unchecked")
    public static <T> Arrangement<T> makeArrange(T... elements) {
        if(elements == null) return null;
        return new Arrangement<>(elements);
    }
    /**
     * Makes a K2 (two-key set/bimap) with A and B key types inferred from the types of a0 and b0, and considers all
     * parameters A-B pairs, casting the Objects at positions 0, 2, 4... etc. to A and the objects at positions
     * 1, 3, 5... etc. to B. If rest has an odd-number length, then it discards the last item. If any pair of items in
     * rest cannot be cast to the correct type of A or B, then this inserts nothing for that pair and logs information
     * on the problematic pair to the static Maker.issueLog field.
     * @param a0 the first A key; used to infer the types of other keys if generic parameters aren't specified.
     * @param b0 the first B key; used to infer the types of other values if generic parameters aren't specified.
     * @param rest an array or vararg of keys and values in pairs; should contain alternating A, B, A, B... elements
     * @param <A> a type of keys in the returned K2; if not specified, will be inferred from a0
     * @param <B> a type of keys in the returned K2; if not specified, will be inferred from b0
     * @return a freshly-made K2 with A and B keys, using a0, b0, and the contents of rest to fill it
     */
    @SuppressWarnings("unchecked")
    public static <A, B> K2<A, B> makeK2(A a0, B b0, Object... rest)
    {
        return makeK2(0.625f, a0, b0, rest);
    }
    /**
     * Makes a K2 (two-key set/bimap) with the given load factor (which should be between 0.1 and 0.9), A and B key
     * types inferred from the types of a0 and b0, and considers all parameters A-B pairs, casting the Objects at
     * positions 0, 2, 4... etc. to A and the objects at positions 1, 3, 5... etc. to B. If rest has an odd-number
     * length, then it discards the last item. If any pair of items in rest cannot be cast to the correct type of A or
     * B, then this inserts nothing for that pair and logs information on the problematic pair to the static
     * Maker.issueLog field.
     * @param factor the load factor; should be between 0.1 and 0.9, and 0.75f is a safe choice
     * @param a0 the first A key; used to infer the types of other keys if generic parameters aren't specified.
     * @param b0 the first B key; used to infer the types of other values if generic parameters aren't specified.
     * @param rest an array or vararg of keys and values in pairs; should contain alternating A, B, A, B... elements
     * @param <A> a type of keys in the returned K2; if not specified, will be inferred from a0
     * @param <B> a type of keys in the returned K2; if not specified, will be inferred from b0
     * @return a freshly-made K2 with A and B keys, using a0, b0, and the contents of rest to fill it
     */
    @SuppressWarnings("unchecked")
    public static <A, B> K2<A, B> makeK2(float factor, A a0, B b0, Object... rest)
    {
        if(rest == null)
            rest = new Object[0];
        K2<A, B> k2 = new K2<>(1 + (rest.length >> 1), factor);
        k2.put(a0, b0);

        for (int i = 0; i < rest.length - 1; i+=2) {
            try {
                k2.put((A) rest[i], (B) rest[i + 1]);
            }catch (ClassCastException cce) {
                issueLog.append("makeK2 call had a casting problem with pair at rest[");
                issueLog.append(i);
                issueLog.append("] and/or rest[");
                issueLog.append(i + 1);
                issueLog.append("], with contents: ");
                issueLog.append(rest[i]);
                issueLog.append(", ");
                issueLog.append(rest[i+1]);
                issueLog.append(".\n\nException messages:\n");
                issueLog.append(cce);
                String msg = cce.getMessage();
                if (msg != null) {
                    issueLog.append('\n');
                    issueLog.append(msg);
                }
                issueLog.append('\n');
            }
        }
        return k2;
    }

    /**
     * Makes an empty K2 (two-key set/bimap); needs A and B key types to be specified in order to work. For an empty
     * K2 with String A keys Coord B keys, you could use {@code Maker.<String, Coord>makeK2();}. Using
     * the new keyword is probably just as easy in this case; this method is provided for completeness relative to
     * makeK2() with 2 or more parameters.
     * @param <A> the type of "A" keys in the returned K2; cannot be inferred and must be specified
     * @param <B> the type of "B" keys in the returned K2; cannot be inferred and must be specified
     * @return an empty K2 with the given key and value types.
     */
    public static <A, B> K2<A, B> makeK2()
    {
        return new K2<>();
    }

}
