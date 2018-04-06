package squidpony.squidmath;

import squidpony.ArrayTools;
import squidpony.annotation.Beta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * An ordered bidirectional map-like data structure, with "bundles" of unique E1 (Element type 1) keys and unique E2
 * (Element type 2) keys updated together like a map that can be queried by E1 key bundles, E2 key bundles, or int
 * indices, as well as like a multimap that can be queried by lone E1 or E2 keys. A bundle can be specified as a
 * Collection of E1 or E2 values, an array of E1 or E2 values, or in two parts as either of the above given to a
 * BundleBiMap method along with an int array that can represent variations on keys (e.g. some quantity for an E1 or E2
 * value that permits different amounts to be passed with the unique element values to it yield a different E2 bundle
 * for, say, 1 Foo and 1 Bar vs. 2 Foo and 3 Bar). This allows an intended purpose for this class as a way to describe
 * crafting recipes that may need various amounts of an item to be mixed in and for multiple items with their own
 * quantities to be produced, and since the quantities are optional, it can still be used to group multiple E1 keys with
 * multiple E2 keys. The multimap property allows you to use an E1 or E2 element key to get all of the E2 or E1 keys
 * that are associated with a bundle that contains that E1 or E2 key.
 * <br>
 * Does not implement any interfaces that you would expect for a data structure, because almost every method it has
 * needs to specify whether it applies to E1 or E2 items, or otherwise doesn't fit a normal Collection-like interface's
 * requirements.
 * <br>
 * Closely related to BundleBiMap, Arrangement, K2, and K2V1 in implementation.
 * <br>
 * Created by Tommy Ettinger on 4/26/2017.
 */
@Beta
public class BundleBundleBiMap<E1, E2>
{
    private Arrangement<E1> elements1;
    private Arrangement<E2> elements2;
    private K2<int[][], int[][]> bm;
    private ArrayList<IntVLA> mm1, mm2;

    /**
     * Constructs an empty BundleBiMap.
     */
    public BundleBundleBiMap()
    {
        this(Arrangement.DEFAULT_INITIAL_SIZE, Arrangement.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a BundleBiMap with the expected number of indices to hold (the number of bundles and number of E2 items
     * is always the same, and this will be more efficient if expected is greater than that number).
     * @param expected how many bundle-to-single pairings this is expected to hold
     */
    public BundleBundleBiMap(int expected)
    {
        this(expected, Arrangement.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a BundleBiMap with the expected number of indices to hold (the number of bundles and number of E2 items
     * is always the same, and this will be more efficient if expected is greater than that number) and the load factor
     * to use, between 0.1f and 0.8f usually (using load factors higher than 0.8f can cause problems).
     * @param expected the amount of indices (the count of bundles is the same as the count of E2 items) this should hold
     * @param f the load factor, probably between 0.1f and 0.8f
     */
    public BundleBundleBiMap(int expected, float f)
    {
        elements1 = new Arrangement<>(expected, f);
        elements2 = new Arrangement<>(expected, f);
        bm = new K2<>(expected, f, CrossHash.int2DHasher, CrossHash.int2DHasher);
        mm1 = new ArrayList<>(expected * 4);
        mm2 = new ArrayList<>(expected * 4);
    }


    /**
     * Constructs a BundleBiMap using another BundleBiMap to copy.
     * @param other the other BundleBiMap to copy
     */
    public BundleBundleBiMap(BundleBundleBiMap<? extends E1, ? extends E2> other)
    {
        if(other == null)
        {
            elements1 = new Arrangement<>(64, 0.75f);
            elements2 = new Arrangement<>(64, 0.75f);
            bm = new K2<>(16, 0.75f, CrossHash.int2DHasher, CrossHash.int2DHasher);
            mm1 = new ArrayList<>(64);
            mm2 = new ArrayList<>(64);
        }
        else
        {
            elements1 = new Arrangement<>(other.elements1);
            elements2 = new Arrangement<>(other.elements2);
            bm = new K2<>(other.bm);
            mm1 = new ArrayList<>(other.mm1);
            mm2 = new ArrayList<>(other.mm2);
        }
    }

    /**
     * Returns true if this contains the E1, element, in any of its bundles of E1 keys.
     * @param element the E1 to check the presence of in all bundles
     * @return true if element is present in this; false otherwise
     */
    public boolean containsElement1(E1 element) { return elements1.containsKey(element); }
    /**
     * Returns true if this contains the E2, element, in its collection of E2 items.
     * @param element the E2 to check the presence of
     * @return true if element is present in this; false otherwise
     */
    public boolean containsElement2(E2 element) { return elements2.containsKey(element); }

    /**
     * Returns true if index is between 0 (inclusive) and {@link #size()} (exclusive), or false otherwise.
     * @param index the index to check
     * @return true if index is a valid index in the ordering of this BundleBiMap
     */
    public boolean containsIndex(int index) { return index >= 0 && index < bm.size(); }

    /**
     * Gets a random E1 from this BundleBiMap's individual elements1 using the given IRNG.
     * @param random generates a random index to get an E1 with
     * @return a randomly chosen E1, or null if this is empty
     */
    public E1 randomElement1(IRNG random)
    {
        return elements1.randomKey(random);
    }

    /**
     * Gets a random E2 from this BundleBiMap using the given IRNG.
     * @param random generates a random index to get a E2 with
     * @return a randomly chosen E2, or null if this is empty
     */
    public E2 randomElement2(IRNG random)
    {
        return elements2.randomKey(random);
    }

    /**
     * Adds a bundle of E1 keys and a E2 key at the same point in the ordering (the end) to this BundleBiMap. Neither
     * parameter can be present in this collection before this is called.
     * @param e1 an array of E1 keys to add; the array cannot already be present, nor can it be null
     * @param e2 an array of E2 keys to add; the array cannot already be present, nor can it be null
     * @return true if this collection changed as e1 result of this call
     */
    public boolean put(E1[] e1, E2[] e2)
    {
        if(e1 == null || e2 == null) return false;
        int len = elements1.size;
        elements1.putAll(e1);
        for (int i = len; i < elements1.size; i++) {
            mm1.add(new IntVLA(4));
        }
        len = elements2.size;
        elements2.putAll(e2);
        for (int i = len; i < elements2.size; i++) {
            mm2.add(new IntVLA(4));
        }
        int[][] bundle1 = new int[][]{elements1.getArray(e1)};
        int[][] bundle2 = new int[][]{elements2.getArray(e2)};
        if(!bm.put(bundle1, bundle2))
            return false;
        len = e1.length;
        for (int i = 0; i < len; i++) {
            mm1.get(bundle1[0][i]).add(bm.size()-1);
        }
        len = e2.length;
        for (int i = 0; i < len; i++) {
            mm2.get(bundle2[0][i]).add(bm.size()-1);
        }
        return true;
    }
    /**
     * Adds a bundle of E1 keys, mixed with an int array of variations, and a E2 key at the same point in the ordering
     * (the end) to this BundleBiMap. Neither the E2 key nor the bundle (effectively, the pair of e1 and variation) can be
     * present in this collection before this is called.
     * @param e1 an array of E1 keys to add; the array cannot already have been inserted with an equivalent variation, nor can it be null
     * @param variation1 an int array that can be used to make a different version of e1, i.e. the same things at different quantities; cannot be null
     * @param e2 an array of E2 keys to add; the array cannot already have been inserted with an equivalent variation, nor can it be null
     * @param variation2 an int array that can be used to make a different version of e2, i.e. the same things at different quantities; cannot be null
     * @return true if this collection changed as e1 result of this call
     */
    public boolean put(E1[] e1, int[] variation1, E2[] e2, int[] variation2)
    {
        if(e1 == null || e2 == null) return false;
        int len = elements1.size;
        elements1.putAll(e1);
        for (int i = len; i < elements1.size; i++) {
            mm1.add(new IntVLA(4));
        }
        len = elements2.size;
        elements2.putAll(e2);
        for (int i = len; i < elements2.size; i++) {
            mm2.add(new IntVLA(4));
        }
        int[][] bundle1, bundle2;
        if(variation1 == null)
            bundle1 = new int[][]{elements1.getArray(e1)};
        else
            bundle1 = new int[][]{elements1.getArray(e1), variation1};
        if(variation2 == null)
            bundle2 = new int[][]{elements2.getArray(e2)};
        else
            bundle2 = new int[][]{elements2.getArray(e2), variation2};
        if(!bm.put(bundle1, bundle2))
            return false;
        len = e1.length;
        for (int i = 0; i < len; i++) {
            mm1.get(bundle1[0][i]).add(bm.size()-1);
        }
        len = e2.length;
        for (int i = 0; i < len; i++) {
            mm2.get(bundle2[0][i]).add(bm.size()-1);
        }
        return true;
    }
    /**
     * Adds a bundle of E1 keys and a E2 key at the same point in the ordering (the end) to this BundleBiMap. Neither
     * parameter can be present in this collection before this is called.
     * @param e1 a Collection of E1 keys to add; the contents cannot already be present, nor can it be null
     * @param e2 a Collection of E2 keys to add; the contents cannot already be present, nor can it be null
     * @return true if this collection changed as a result of this call
     */
    public boolean put(Collection<? extends E1> e1, Collection<? extends E2> e2)
    {
        if(e1 == null || e2 == null) return false;
        int len = elements1.size;
        elements1.putAll(e1);
        for (int i = len; i < elements1.size; i++) {
            mm1.add(new IntVLA(4));
        }
        len = elements2.size;
        elements2.putAll(e2);
        for (int i = len; i < elements2.size; i++) {
            mm2.add(new IntVLA(4));
        }
        int[][] bundle1 = new int[][]{elements1.getArray(e1)};
        int[][] bundle2 = new int[][]{elements2.getArray(e2)};
        if(!bm.put(bundle1, bundle2))
            return false;
        len = e1.size();
        for (int i = 0; i < len; i++) {
            mm1.get(bundle1[0][i]).add(bm.size()-1);
        }
        len = e2.size();
        for (int i = 0; i < len; i++) {
            mm2.get(bundle2[0][i]).add(bm.size()-1);
        }
        return true;

    }
    /**
     * Adds a bundle of E1 keys, mixed with an int array of variations, and a bundle of E2 keys at the same point in the
     * ordering (the end) to this BundleBiMap. Neither the E1 bundle nor E2 bundle (effectively, the pair of element
     * collection and variation) can be present in this collection before this is called.
     * @param e1 a Collection of E1 keys to add; the Collection cannot already have been inserted with an equivalent variation, nor can it be null
     * @param variation1 an int array that can be used to make a different version of e1, i.e. the same things at different quantities; cannot be null
     * @param e2 a Collection of E2 keys to add; the Collection cannot already have been inserted with an equivalent variation, nor can it be null
     * @param variation2 an int array that can be used to make a different version of e2, i.e. the same things at different quantities; cannot be null
     * @return true if this collection changed as a result of this call
     */
    public boolean put(Collection<? extends E1> e1, int[] variation1, Collection<? extends E2> e2, int[] variation2)
    {
        if(e1 == null || e2 == null) return false;
        int len = elements1.size;
        elements1.putAll(e1);
        for (int i = len; i < elements1.size; i++) {
            mm1.add(new IntVLA(4));
        }
        len = elements2.size;
        elements2.putAll(e2);
        for (int i = len; i < elements2.size; i++) {
            mm2.add(new IntVLA(4));
        }
        int[][] bundle1, bundle2;
        if(variation1 == null)
            bundle1 = new int[][]{elements1.getArray(e1)};
        else
            bundle1 = new int[][]{elements1.getArray(e1), variation1};
        if(variation2 == null)
            bundle2 = new int[][]{elements2.getArray(e2)};
        else
            bundle2 = new int[][]{elements2.getArray(e2), variation2};
        if(!bm.put(bundle1, bundle2))
            return false;
        len = e1.size();
        for (int i = 0; i < len; i++) {
            mm1.get(bundle1[0][i]).add(bm.size()-1);
        }
        len = e2.size();
        for (int i = 0; i < len; i++) {
            mm2.get(bundle2[0][i]).add(bm.size()-1);
        }
        return true;
    }

    /**
     * Puts all unique E1 and E2 keys in {@code aKeys} and {@code bKeys} into this K2 at the end. If an E1 in aKeys or a E2
     * in bKeys is already present when this would add one, this will not put the E1 and E2 keys at that point in the
     * iteration order, and will place the next unique E1 and E2 it finds in the arguments at that position instead.
     * @param aKeys an Iterable or Collection of E1 keys to add; should all be unique (like a Set)
     * @param bKeys an Iterable or Collection of E2 keys to add; should all be unique (like a Set)
     * @return true if this collection changed as a result of this call
     */
    public boolean putAll(Iterable<? extends Collection<? extends E1>> aKeys, Iterable<? extends Collection<? extends E2>> bKeys)
    {
        if(aKeys == null || bKeys == null) return false;
        Iterator<? extends Collection<? extends E1>> aIt = aKeys.iterator();
        Iterator<? extends Collection<? extends E2>> bIt = bKeys.iterator();
        boolean changed = false;
        while (aIt.hasNext() && bIt.hasNext())
        {
            changed = put(aIt.next(), bIt.next()) || changed;
        }
        return changed;
    }
    /**
     * Puts all unique E1 and E2 keys in {@code other} into this K2, respecting other's ordering. If an E1 or a E2 in other
     * is already present when this would add one, this will not put the E1 and E2 keys at that point in the iteration
     * order, and will place the next unique E1 and E2 it finds in the arguments at that position instead.
     * @param other another K2 collection with the same E1 and E2 types
     * @return true if this collection changed as a result of this call
     */
    public boolean putAll(BundleBundleBiMap<? extends E1, ? extends E2> other)
    {
        if(other == null) return false;
        boolean changed = false;
        int sz = other.size();
        for (int i = 0; i < sz; i++) {
            int[][] bundle1 = other.bm.getAAt(i), bundle2 = other.bm.getBAt(i);
            if(bundle1 == null || bundle1.length == 0 || bundle2 == null || bundle2.length == 0) continue;
            if(bundle1.length == 1 && bundle2.length == 1)
            {
                changed |= put(elements1.keysAt(bundle1[0]), elements2.keysAt(bundle2[0]));
            }
            else
            {
                changed |= put(elements1.keysAt(bundle1[0]), bundle1.length > 1 ? bundle1[1] : null,
                        elements2.keysAt(bundle2[0]), bundle2.length > 1 ? bundle2[1] : null);
            }
        }
        return changed;
    }

    /**
     * Given an index to look up, gets a 2D int array representing the bundle at that index. The bundle is likely to
     * use a representation for the first sub-array that will be meaningless without internal information from this
     * BundleBiMap, but the second sub-array, if present, should match the variation supplied with that bundle
     * to {@link #put(Object[], int[], Object[], int[])} or {@link #put(Collection, int[], Collection, int[])}.
     * <br>
     * This method copies the 2D int array it returns, so modifying it won't affect the original BundleBiMap.
     * @param index an int index to look up
     * @return a 2D int array that represents a bundle, or null if index is out of bounds
     */
    public int[][] getCodeAt(int index)
    {
        return ArrayTools.copy(bm.getAAt(index));
    }
    /**
     * Given an index to look up, gets the E2 key present at that position in the ordering.
     * @param index an int index to look up
     */
    public OrderedSet<E2> getElement2At(int index)
    {
        return elements2.keysAt(bm.getBAt(index)[0]);
    }

    /**
     * Given a bundle of E1 keys as an array with no variation, gets the matching E2 key for that bundle, or null if there
     * is none.
     * @param e1 an array of E1
     * @return the E2 keys that correspond to the given bundle, e1 combined with variations
     */
    public OrderedSet<E2> getElement2(E1[] e1)
    {
        if(e1 == null) return null;
        return elements2.keysAt(bm.getBFromA(new int[][]{elements1.getArray(e1)})[0]);
    }


    /**
     * Given a bundle of E1 keys as an array with an int array variation, gets the matching E2 key for that bundle, or
     * null if there is none.
     * @param e1 an array of E1 element keys
     * @param variations an int array that should match an int array used as a variation parameter to put
     * @return the E2 keys that correspond to the given bundle, e1 combined with variations
     */
    public OrderedSet<E2> getElement2(E1[] e1, int[] variations)
    {
        if(e1 == null || variations == null) return null;
        return elements2.keysAt(bm.getBFromA(new int[][]{elements1.getArray(e1), variations})[0]);
    }

    /**
     * Given a bundle of E1 keys as a Collection with no variation, gets the matching E2 key for that bundle, or
     * null if there is none.
     * @param e1 a Collection of E1 element keys (each key can be an E1 or an instance of any subclass of E1)
     * @return the E2 key that corresponds to the given bundle, e
     */
    public OrderedSet<E2> getElement2(Collection<? extends E1> e1)
    {
        if(e1 == null) return null;
        return elements2.keysAt(bm.getBFromA(new int[][]{elements1.getArray(e1)})[0]);
    }

    /**
     * Given a bundle of E1 keys as a Collection with an int array variation, gets the matching E2 key for that bundle, or
     * null if there is none.
     * @param e1 a Collection of E1 element keys (each key can be an E1 or an instance of any subclass of E1)
     * @param variations an int array that should match an int array used as a variation parameter to put
     * @return the E2 key that corresponds to the given bundle, e1 combined with variations
     */
    public OrderedSet<E2> getElement2(Collection<? extends E1> e1, int[] variations)
    {
        if(e1 == null || variations == null) return null;
        return elements2.keysAt(bm.getBFromA(new int[][]{elements1.getArray(e1), variations})[0]);
    }
    /**
     * Given an E1 bundle as a coded 2D int array, gets the matching E2 bundle, also coded, or null if there is none.
     * The code parameter should be obtained from one of the methods that specifically returns that kind of 2D array,
     * since the code uses internal information to efficiently represent a bundle.
     * @param code a 2D int array representing a bundle that should have been obtained directly from this object
     * @return the E2 key that corresponds to the given coded bundle
     */
    public int[][] getElement2Coded(int[][] code)
    {
        if(code == null) return null;
        return bm.getBFromA(code);
    }

    /**
     * Gets (in near-constant time) the index of the given E2 coded key in the ordering.
     * @param code a E2 coded key to look up
     * @return the position in the ordering of code
     */
    public int indexOfElement2Coded(int[][] code)
    {
        return bm.indexOfB(code);
    }

    /**
     * Given a coded bundle as produced by some methods in this class, decodes the elements1 part of the bundle and
     * returns it as a newly-allocated OrderedSet of E1 element keys.
     * @param bundle a coded bundle as a 2D int array
     * @return an OrderedSet of E1 element keys corresponding to the data coded into bundle
     */
    public OrderedSet<E1> elementsFromCode(int[][] bundle)
    {
        if(bundle == null || bundle.length < 1 || bundle[0] == null) return null;
        return elements1.keysAt(bundle[0]);
    }

    /**
     * Given a coded bundle as produced by some methods in this class, decodes the variation part of the bundle, if
     * present, and returns it as a newly-allocated 1D int array. If there is no variation in the given coded bundle,
     * this returns null.
     * @param bundle a coded bundle as a 2D int array
     * @return the variation part of the data coded into bundle
     */
    public int[] variationFromCode(int[][] bundle)
    {
        if(bundle == null || bundle.length < 2 || bundle[1] == null) return null;
        int[] ret = new int[bundle[1].length];
        System.arraycopy(bundle[1], 0, ret, 0, ret.length);
        return ret;
    }

    /**
     * Given an E2 key to look up, gets a (newly-allocated) OrderedSet of E1 element keys corresponding to that E2 key.
     * If a variation is part of the bundle, it will not be present in this, but a copy can be retrieved with
     * {@link #getElement1BundleVariation(Object[])}.
     * @param lookup a E2 key to look up
     * @return an OrderedSet of the E1 elements1 used in the bundle that corresponds to single, or null if invalid
     */
    public OrderedSet<E1> getBundleElements(E2[] lookup)
    {
        int[][] bundle;
        if(lookup == null || (bundle = bm.getAFromB(new int[][]{elements2.getArray(lookup)})) == null
                || bundle.length < 1 || bundle[0] == null) return null;
        return elements1.keysAt(bundle[0]);
    }

    /**
     * Given an E2 key to look up, gets a (newly-allocated) int array that is equivalent to the variation part of the
     * bundle corresponding to single. If there is no variation in the corresponding bundle, then this returns null.
     * To get the E1 elements1 that are the main part of a bundle, you can use {@link #getBundleElements(Object[])}.
     * @param lookup a bundle of E2 keys to look up
     * @return the variation part of the E1 bundle that corresponds to the looked-up bundle, or null if invalid
     */
    public int[] getElement1BundleVariation(E2[] lookup)
    {
        int[][] bundle;
        if(lookup == null || (bundle = bm.getAFromB(new int[][]{elements2.getArray(lookup)})) == null
                || bundle.length < 2 || bundle[1] == null) return null;
        int[] ret = new int[bundle[1].length];
        System.arraycopy(bundle[1], 0, ret, 0, ret.length);
        return ret;
    }

    /**
     * Given an index to look up, gets a (newly-allocated) OrderedSet of E1 element keys in the bundle at that index.
     * If a variation is part of the bundle, it will not be present in this, but a copy can be retrieved with
     * {@link #getBundleVariationAt(int)}.
     * @param index an int position in the ordering to look up
     * @return an OrderedSet of the E1 elements1 used in the bundle present at index, or null if invalid
     */
    public OrderedSet<E1> getBundleElementsAt(int index)
    {
        int[][] bundle;
        if((bundle = bm.getAAt(index)) == null || bundle.length < 1) return null;
        return elements1.keysAt(bundle[0]);
    }
    /**
     * Given an index to look up, gets a (newly-allocated) int array that is equivalent to the variation part of the
     * bundle present at that index. If there is no variation in the corresponding bundle, then this returns null.
     * To get the E1 elements1 that are the main part of a bundle, you can use {@link #getBundleElementsAt(int)}.
     * @param index an int position in the ordering to look up
     * @return an int array copied from the variation part of the bundle present at index, or null if invalid
     */
    public int[] getBundleVariationAt(int index)
    {
        int[][] bundle;
        if((bundle = bm.getAAt(index)) == null || bundle.length < 2 || bundle[1] == null) return null;
        int[] ret = new int[bundle[1].length];
        System.arraycopy(bundle[1], 0, ret, 0, ret.length);
        return ret;
    }

    /**
     * Given an E1 element key that could be used in one or more bundles this uses, finds all E2 single keys corresponding
     * to bundles that contain the given element. Thus, if E1 was String and this BundleBiMap contained ["Copper", "Tin"]
     * mapped to "Bronze" and ["Zinc", "Copper"] mapped to "Brass", then calling this method with "Copper" would get an
     * OrderedSet that contains ["Bronze", "Brass"].
     * @param element an E1 element key to look up (probably a component of one or more bundles)
     * @return an OrderedSet of all E2 keys where the given element is part of the bundle corresponding to that E2 key, or null if E1 does not match anything
     */
    public OrderedSet<OrderedSet<? extends E2>> getManyElement2(E1 element)
    {
        if(element == null) return null;
        int pos;
        if((pos = elements1.getInt(element)) < 0) return null;

        IntVLA positions = mm1.get(pos);
        OrderedSet<OrderedSet<? extends E2>> ks = new OrderedSet<>(positions.size);
        for(int i = 0; i < positions.size; i++)
        {
            ks.add(getElement2At(positions.get(i)));
        }
        return ks;
    }
    /**
     * Given an E1 element key that could be used in one or more bundles this uses, gets all bundles in this object that
     * contain the given element, as coded 2D int arrays. These coded bundles can be given to
     * {@link #elementsFromCode(int[][])} and {@link #variationFromCode(int[][])} to get usable information from them.
     * @param element an E1 element key to look up (probably a component of one or more bundles)
     * @return an OrderedSet of all coded 2D int array bundles that contain the given element, or null if E1 is not in any bundles
     */
    public OrderedSet<int[][]> getManyCoded(E1 element)
    {
        if(element == null) return null;
        int pos;
        if((pos = elements1.getInt(element)) < 0) return null;
        return bm.keysA.keysAt(mm1.get(pos));
    }

    /**
     * Given an E1 element key that could be used in one or more bundles this uses, gets all indices in the ordering
     * that contain a bundle with that element.  From such an index, you can use {@link #getElement2At(int)} (int)} to get the
     * E2 key at that position, {@link #getBundleElementsAt(int)} to get the E1 element keys at that position,
     * {@link #getBundleVariationAt(int)} to get the possible variation at that position, or {@link #getCodeAt(int)} to
     * get the coded bundle at that position.
     * @return an OrderedSet of all coded 2D int array bundles that contain the given element, or null if E1 is not in any bundles
     */
    public int[] getManyIndices(E1 element)
    {
        if(element == null) return null;
        int pos;
        if((pos = elements1.getInt(element)) < 0) return null;
        return mm1.get(pos).toArray();
    }

    /**
     * Reorders this BundleBiMap using {@code ordering}, which has the same length as this object's {@link #size()}
     * and can be generated with {@link ArrayTools#range(int)} (which, if applied, would produce no
     * change to the current ordering), {@link IRNG#randomOrdering(int)} (which gives a random ordering, and if
     * applied immediately would be the same as calling {@link #shuffle(IRNG)}), or made in some other way. If you
     * already have an ordering and want to make a different ordering that can undo the change, you can use
     * {@link ArrayTools#invertOrdering(int[])} called on the original ordering. The effects of this method, if called
     * with an ordering that has repeat occurrences of an int or contains ints that are larger than its size should
     * permit, are undefined other than the vague definition of "probably bad, unless you like crashes."
     * @param ordering an int array or vararg that must contain each int from 0 to {@link #size()}
     * @return this for chaining
     */
    public BundleBundleBiMap<E1, E2> reorder(int... ordering)
    {
        if(ordering == null || ordering.length != bm.size()) return this;
        bm.reorder(ordering);
        int len = mm1.size();
        for (int i = 0; i < len; i++) {
            IntVLA iv = mm1.get(i);
            for (int j = 0; j < iv.size; j++) {
                iv.set(i, ordering[iv.get(i)]);
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
    public BundleBundleBiMap<E1, E2> shuffle(IRNG rng)
    {
        return reorder(rng.randomOrdering(bm.size()));
    }

    /**
     * Creates a new iterator over the individual E1 element keys this holds, with a larger total count the iterator may
     * yield than {@link #size()} in most cases (it should be equal to {@link #elementSize()}), and in no particular
     * order (though the order should be stable across versions and platforms, no special means are provided to further
     * control the order). The E1 keys are individual, not bundled, and duplicate keys should never be encountered even
     * if an E1 key appears in many bundles.
     * @return a newly-created iterator over this BundleBiMap's individual (non-bundled) E1 keys
     */
    public Iterator<E1> iteratorElement1()
    {
        return elements1.iterator();
    }
    /**
     * Creates a new iterator over the E2 single keys this holds. The total number of items this yields should be equal
     * to {@link #size()}. This method can be problematic for garbage collection if called very frequently; it may be
     * better to access items by index (which also lets you access other keys associated with that index) using
     * {@link #getElement2At(int)} in a for(int i=0...) loop.
     * @return a newly-created iterator over this BundleBiMap's E2 keys
     */
    public Iterator<E2> iteratorElement2()
    {
        return elements2.iterator();
    }

    /**
     * Gets and caches the individual E1 keys as a Collection that implements SortedSet (and so also implements Set).
     * @return the E1 keys as a SortedSet
     */
    public SortedSet<E1> getElement1Set() {
        return elements1.keySet();
    }

    /**
     * Gets and caches the E2 single keys as a Collection that implements SortedSet (and so also implements Set).
     * @return the E2 keys as a SortedSet
     */
    public SortedSet<E2> getElement2Set() {
        return elements2.keySet();
    }

    /**
     * To be called sparingly, since this allocates a new OrderedSet instead of reusing one.
     * @return the E1 keys as an OrderedSet
     */
    public OrderedSet<E1> getElement1OrderedSet() {
        return elements1.keysAsOrderedSet();
    }

    /**
     * To be called sparingly, since this allocates a new OrderedSet instead of reusing one.
     * @return the E2 keys as an OrderedSet
     */
    public OrderedSet<E2> getElement2OrderedSet() {
        return elements2.keysAsOrderedSet();
    }

    /**
     * Gets the total number of bundle-to-single pairs in this BundleBiMap.
     * @return the total number of bundle keys (equivalently, the number of single keys) in this object
     */
    public int size() {
        return bm.size();
    }

    /**
     * Gets the total number of unique E1 element keys across all bundles in this BundleBiMap. Usually not the same as
     * {@link #size()}, and ordinarily a fair amount larger, though this can also be smaller.
     * @return the total number of unique E1 element keys in all bundles this contains
     */
    public int elementSize() {
        return elements1.size();
    }

    public boolean isEmpty()
    {
        return bm.isEmpty();
    }

}
