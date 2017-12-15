package squidpony.squidmath;

import squidpony.ArrayTools;
import squidpony.annotation.Beta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * An ordered bidirectional map-like data structure, with "bundles" of unique E (for Element) keys and unique S (for
 * Single) keys updated together like a map that can be queried by E key bundles, S keys, or int indices, as well as
 * like a multimap that can be queried by lone E keys. A bundle can be specified as a Collection of E values, an array
 * of E values, or in two parts as either of the above given to a BundleBiMap method along with an int array that can
 * represent variations on E keys (e.g. some quantity for an E value that permits different amounts to be passed with
 * the unique E values to getS and have it yield a different S for a, say, 1 Foo and 1 Bar vs. 2 Foo and 3 Bar). This
 * allows an intended purpose for this class as a way to describe crafting recipes that may need various amounts of an
 * item to be mixed in, and since the quantities are optional, it can still be used to group multiple E keys with S
 * keys. The multimap property allows you to use an E element key to get all of the S keys that are associated with a
 * bundle that contains that E key (you can also get the ordering indices for those S keys, which you can use to get the
 * full bundle if you want).
 * <br>
 * Does not implement any interfaces that you would expect for a data structure, because almost every method it has
 * needs to specify whether it applies to E or S items, or otherwise doesn't fit a normal Collection-like interface's
 * requirements.
 * <br>
 * Closely related to Arrangement, K2, and K2V1 in implementation.
 * <br>
 * Created by Tommy Ettinger on 4/26/2017.
 */
@Beta
public class BundleBiMap<E, S>
{
    private Arrangement<E> elements;
    private K2<int[][], S> bm;
    private ArrayList<IntVLA> mm;

    /**
     * Constructs an empty BundleBiMap.
     */
    public BundleBiMap()
    {
        this(Arrangement.DEFAULT_INITIAL_SIZE, Arrangement.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a BundleBiMap with the expected number of indices to hold (the number of bundles and number of S items
     * is always the same, and this will be more efficient if expected is greater than that number).
     * @param expected how many bundle-to-single pairings this is expected to hold
     */
    public BundleBiMap(int expected)
    {
        this(expected, Arrangement.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a BundleBiMap with the expected number of indices to hold (the number of bundles and number of S items
     * is always the same, and this will be more efficient if expected is greater than that number) and the load factor
     * to use, between 0.1f and 0.8f usually (using load factors higher than 0.8f can cause problems).
     * @param expected the amount of indices (the count of bundles is the same as the count of S items) this should hold
     * @param f the load factor, probably between 0.1f and 0.8f
     */
    public BundleBiMap(int expected, float f)
    {
        elements = new Arrangement<>(expected, f);
        bm = new K2<>(expected, f, CrossHash.int2DHasher, CrossHash.defaultHasher);
        mm = new ArrayList<>(expected * 4);
    }


    /**
     * Constructs a BundleBiMap using another BundleBiMap to copy.
     * @param other the other BundleBiMap to copy
     */
    public BundleBiMap(BundleBiMap<? extends E, ? extends S> other)
    {
        if(other == null)
        {
            elements = new Arrangement<>(64, 0.75f);
            bm = new K2<>(16, 0.75f, CrossHash.int2DHasher, CrossHash.defaultHasher);
            mm = new ArrayList<>(64);
        }
        else
        {
            elements = new Arrangement<>(other.elements);
            bm = new K2<>(other.bm);
            mm = new ArrayList<>(other.mm);
        }
    }

    /**
     * Returns true if this contains the E, element, in any of its bundles of E keys.
     * @param element the E to check the presence of in all bundles
     * @return true if element is present in this; false otherwise
     */
    public boolean containsElement(E element) { return elements.containsKey(element); }
    /**
     * Returns true if this contains the S, single, in its collection of S items.
     * @param single the S to check the presence of
     * @return true if single is present in this; false otherwise
     */
    public boolean containsSingle(S single) { return bm.containsB(single); }

    /**
     * Returns true if index is between 0 (inclusive) and {@link #size()} (exclusive), or false otherwise.
     * @param index the index to check
     * @return true if index is a valid index in the ordering of this BundleBiMap
     */
    public boolean containsIndex(int index) { return index >= 0 && index < bm.size(); }

    /**
     * Given an int index, finds the associated S key (using index as a point in the ordering).
     * @param index an int index into this BundleBiMap
     * @return the S object with index for its position in the ordering, or null if index was invalid
     */
    public S getSingleAt(int index)
    {
        return bm.getBAt(index);
    }

    /**
     * Gets a random E from this BundleBiMap's individual elements using the given RNG.
     * @param random generates a random index to get an E with
     * @return a randomly chosen E, or null if this is empty
     */
    public E randomElement(RNG random)
    {
        return elements.randomKey(random);
    }

    /**
     * Gets a random S from this BundleBiMap using the given RNG.
     * @param random generates a random index to get a S with
     * @return a randomly chosen S, or null if this is empty
     */
    public S randomSingle(RNG random)
    {
        return bm.randomB(random);
    }

    /**
     * Changes an existing S key, {@code past}, to another S key, {@code future}, if past exists in this BundleBiMap
     * and future does not yet exist in this BundleBiMap. This will retain past's point in the ordering for future, so
     * the associated other key(s) will still be associated in the same way.
     * @param past a S key, that must exist in this BundleBiMap's S keys, and will be changed
     * @param future a S key, that cannot currently exist in this BundleBiMap's S keys, but will if this succeeds
     * @return this for chaining
     */
    public BundleBiMap<E, S> alterB(S past, S future)
    {
        bm.alterB(past, future);
        return this;
    }



    /**
     * Changes the S key at {@code index} to another S key, {@code future}, if index is valid and future does not
     * yet exist in this K2. The position in the ordering for future will be the same as index, and the same
     * as the key this replaced, if this succeeds, so the other key(s) at that position will still be associated in
     * the same way.
     * @param index a position in the ordering to change; must be at least 0 and less than {@link #size()}
     * @param future a S key, that cannot currently exist in this BundleBiMap's S keys, but will if this succeeds
     * @return this for chaining
     */
    public BundleBiMap<E, S> alterSingleAt(int index, S future)
    {
        bm.alterBAt(index, future);
        return this;
    }
    /**
     * Adds a bundle of E keys and a S key at the same point in the ordering (the end) to this BundleBiMap. Neither
     * parameter can be present in this collection before this is called.
     * @param e an array of E keys to add; the array cannot already be present, nor can it be null
     * @param s e S key to add; cannot already be present
     * @return true if this collection changed as e result of this call
     */
    public boolean put(E[] e, S s)
    {
        if(e == null) return false;
        int len = elements.size;
        elements.putAll(e);
        for (int i = len; i < elements.size; i++) {
            mm.add(new IntVLA(e.length));
        }

        elements.putAll(e);
        int[][] bundle = new int[][]{elements.getArray(e)};
        if(!bm.put(bundle, s))
            return false;
        for (int i = 0; i < e.length; i++) {
            mm.get(bundle[0][i]).add(bm.size()-1);
        }
        return true;
    }
    /**
     * Adds a bundle of E keys, mixed with an int array of variations, and a S key at the same point in the ordering
     * (the end) to this BundleBiMap. Neither the S key nor the bundle (effectively, the pair of e and variation) can be
     * present in this collection before this is called.
     * @param e an array of E keys to add; the array cannot already have been inserted with an equivalent variation, nor can it be null
     * @param variation an int array that can be used to make a different version of e, i.e. the same things at different quantities; cannot be null
     * @param s e S key to add; cannot already be present
     * @return true if this collection changed as e result of this call
     */
    public boolean put(E[] e, int[] variation, S s)
    {
        if(e == null || variation == null) return false;
        int len = elements.size;
        elements.putAll(e);
        for (int i = len; i < elements.size; i++) {
            mm.add(new IntVLA(e.length));
        }

        int[][] bundle = new int[][]{elements.getArray(e), variation};
        if(!bm.put(bundle, s))
            return false;
        for (int i = 0; i < e.length; i++) {
            mm.get(bundle[0][i]).add(bm.size()-1);
        }
        return true;
    }
    /**
     * Adds a bundle of E keys and a S key at the same point in the ordering (the end) to this BundleBiMap. Neither
     * parameter can be present in this collection before this is called.
     * @param e an array of E keys to add; the array cannot already be present, nor can it be null
     * @param s e S key to add; cannot already be present
     * @return true if this collection changed as e result of this call
     */
    public boolean put(Collection<? extends E> e, S s)
    {
        if(e == null) return false;
        int len = elements.size;
        elements.putAll(e);
        for (int i = len; i < elements.size; i++) {
            mm.add(new IntVLA(e.size()));
        }
        int[][] bundle = new int[][]{elements.getArray(e)};
        if(!bm.put(bundle, s))
            return false;
        len = bundle[0].length;
        for (int i = 0; i < len; i++) {
            mm.get(bundle[0][i]).add(bm.size()-1);
        }
        return true;
    }
    /**
     * Adds a bundle of E keys, mixed with an int array of variations, and a S key at the same point in the ordering
     * (the end) to this BundleBiMap. Neither the S key nor the bundle (effectively, the pair of e and variation) can be
     * present in this collection before this is called.
     * @param e an array of E keys to add; the array cannot already have been inserted with an equivalent variation, nor can it be null
     * @param variation an int array that can be used to make a different version of e, i.e. the same things at different quantities; cannot be null
     * @param s e S key to add; cannot already be present
     * @return true if this collection changed as e result of this call
     */
    public boolean put(Collection<? extends E> e, int[] variation, S s)
    {
        if(e == null || variation == null) return false;
        int len = elements.size;
        elements.putAll(e);
        for (int i = len; i < elements.size; i++) {
            mm.add(new IntVLA(e.size()));
        }
        int[][] bundle = new int[][]{elements.getArray(e), variation};
        if(!bm.put(bundle, s))
            return false;
        len = bundle[0].length;
        for (int i = 0; i < len; i++) {
            mm.get(bundle[0][i]).add(bm.size()-1);
        }
        return true;
    }

    /**
     * Puts all unique E and S keys in {@code aKeys} and {@code bKeys} into this K2 at the end. If an E in aKeys or a S
     * in bKeys is already present when this would add one, this will not put the E and S keys at that point in the
     * iteration order, and will place the next unique E and S it finds in the arguments at that position instead.
     * @param aKeys an Iterable or Collection of E keys to add; should all be unique (like a Set)
     * @param bKeys an Iterable or Collection of S keys to add; should all be unique (like a Set)
     * @return true if this collection changed as a result of this call
     */
    public boolean putAll(Iterable<? extends Collection<? extends E>> aKeys, Iterable<? extends S> bKeys)
    {
        if(aKeys == null || bKeys == null) return false;
        Iterator<? extends Collection<? extends E>> aIt = aKeys.iterator();
        Iterator<? extends S> bIt = bKeys.iterator();
        boolean changed = false;
        while (aIt.hasNext() && bIt.hasNext())
        {
            changed = put(aIt.next(), bIt.next()) || changed;
        }
        return changed;
    }
    /**
     * Puts all unique E and S keys in {@code other} into this K2, respecting other's ordering. If an E or a S in other
     * is already present when this would add one, this will not put the E and S keys at that point in the iteration
     * order, and will place the next unique E and S it finds in the arguments at that position instead.
     * @param other another K2 collection with the same E and S types
     * @return true if this collection changed as a result of this call
     */
    public boolean putAll(BundleBiMap<? extends E, ? extends S> other)
    {
        if(other == null) return false;
        boolean changed = false;
        int sz = other.size();
        for (int i = 0; i < sz; i++) {
            int[][] bundle = other.bm.getAAt(i);
            if(bundle == null || bundle.length == 0) continue;
            if(bundle.length == 1)
                changed |= put(elements.keysAt(bundle[0]), other.bm.getBAt(i));
            else
                changed |= put(elements.keysAt(bundle[0]), bundle[1], other.bm.getBAt(i));
        }
        return changed;
    }

    /**
     * Given an S key to look up, gets a 2D int array representing the key's matching bundle. The bundle is likely to
     * use a representation for the first sub-array that will be meaningless without internal information from this
     * BundleBiMap, but the second sub-array, if present, should match the variation supplied with that bundle
     * to {@link #put(Object[], int[], Object)} or {@link #put(Collection, int[], Object)}.
     * <br>
     * This method copies the 2D int array it returns, so modifying it won't affect the original BundleBiMap.
     * @param single a S key to look up
     * @return a copied 2D int array that represents a bundle, or null if single is not present in this
     */
    public int[][] getCode(S single)
    {
        if(single == null) return null;
        return ArrayTools.copy(bm.getAFromB(single));
    }
    /**
     * Given an index to look up, gets a 2D int array representing the bundle at that index. The bundle is likely to
     * use a representation for the first sub-array that will be meaningless without internal information from this
     * BundleBiMap, but the second sub-array, if present, should match the variation supplied with that bundle
     * to {@link #put(Object[], int[], Object)} or {@link #put(Collection, int[], Object)}.
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
     * Given an index to look up, gets the S key present at that position in the ordering.
     * @param index an int index to look up
     * @return a 2D int array that represents a bundle, or null if index is out of bounds
     */
    public S singleAt(int index)
    {
        return bm.getBAt(index);
    }

    /**
     * Given a bundle of E keys as an array with no variation, gets the matching S key for that bundle, or null if there
     * is none.
     * @param e an array of E
     * @return the S key that corresponds to the given bundle, e
     */
    public S getSingle(E[] e)
    {
        if(e == null) return null;
        return bm.getBFromA(new int[][]{elements.getArray(e)});
    }

    /**
     * Given a bundle of E keys as an array with an int array variation, gets the matching S key for that bundle, or
     * null if there is none.
     * @param e an array of E element keys
     * @param variations an int array that should match an int array used as a variation parameter to put
     * @return the S key that corresponds to the given bundle, e combined with variations
     */
    public S getSingle(E[] e, int[] variations)
    {
        if(e == null || variations == null) return null;
        return bm.getBFromA(new int[][]{elements.getArray(e), variations});
    }

    /**
     * Given a bundle of E keys as a Collection with no variation, gets the matching S key for that bundle, or
     * null if there is none.
     * @param e a Collection of E element keys (each key can be an E or an instance of any subclass of E)
     * @return the S key that corresponds to the given bundle, e
     */
    public S getSingle(Collection<? extends E> e)
    {
        if(e == null) return null;
        return bm.getBFromA(new int[][]{elements.getArray(e)});
    }

    /**
     * Given a bundle of E keys as a Collection with an int array variation, gets the matching S key for that bundle, or
     * null if there is none.
     * @param e a Collection of E element keys (each key can be an E or an instance of any subclass of E)
     * @param variations an int array that should match an int array used as a variation parameter to put
     * @return the S key that corresponds to the given bundle, e combined with variations
     */
    public S getSingle(Collection<? extends E> e, int[] variations)
    {
        if(e == null || variations == null) return null;
        return bm.getBFromA(new int[][]{elements.getArray(e), variations});
    }
    /**
     * Given a bundle as a coded 2D int array, gets the matching S key for that bundle, or null if there is none.
     * The code parameter should be obtained from one of the methods that specifically returns that kind of 2D array,
     * since the code uses internal information to efficiently represent a bundle.
     * @param code a 2D int array representing a bundle that should have been obtained directly from this object
     * @return the S key that corresponds to the given coded bundle
     */
    public S getSingleCoded(int[][] code)
    {
        if(code == null) return null;
        return bm.getBFromA(code);
    }

    /**
     * Gets (in near-constant time) the index of the given S single key in the ordering.
     * @param single a S single key to look up
     * @return the position in the ordering of single
     */
    public int indexOfSingle(S single)
    {
        return bm.indexOfB(single);
    }

    /**
     * Given a coded bundle as produced by some methods in this class, decodes the elements part of the bundle and
     * returns it as a newly-allocated OrderedSet of E element keys.
     * @param bundle a coded bundle as a 2D int array
     * @return an OrderedSet of E element keys corresponding to the data coded into bundle
     */
    public OrderedSet<E> elementsFromCode(int[][] bundle)
    {
        if(bundle == null || bundle.length < 1 || bundle[0] == null) return null;
        return elements.keysAt(bundle[0]);
    }

    /**
     * Given a coded bundle as produced by some methods in this class, decodes the variation part of the bundle, if
     * present, and returns it as a newly-allocated 1D int array. If there is no variation in the given coded bundle,
     * this returns null.
     * @param bundle a coded bundle as a 2D int array
     * @return an OrderedSet of E element keys corresponding to the data coded into bundle
     */
    public int[] variationFromCode(int[][] bundle)
    {
        if(bundle == null || bundle.length < 2 || bundle[1] == null) return null;
        int[] ret = new int[bundle[1].length];
        System.arraycopy(bundle[1], 0, ret, 0, ret.length);
        return ret;
    }

    /**
     * Given an S key to look up, gets a (newly-allocated) OrderedSet of E element keys corresponding to that S key.
     * If a variation is part of the bundle, it will not be present in this, but a copy can be retrieved with
     * {@link #getBundleVariation(Object)}.
     * @param single a S key to look up
     * @return an OrderedSet of the E elements used in the bundle that corresponds to single, or null if invalid
     */
    public OrderedSet<E> getBundleElements(S single)
    {
        int[][] bundle;
        if(single == null || (bundle = bm.getAFromB(single)) == null || bundle.length < 1 || bundle[0] == null) return null;
        return elements.keysAt(bundle[0]);
    }

    /**
     * Given an S key to look up, gets a (newly-allocated) int array that is equivalent to the variation part of the
     * bundle corresponding to single. If there is no variation in the corresponding bundle, then this returns null.
     * To get the E elements that are the main part of a bundle, you can use {@link #getBundleElements(Object)}.
     * @param single a S key to look up
     * @return an int array copied from the variation part of the bundle that corresponds to single, or null if invalid
     */
    public int[] getBundleVariation(S single)
    {
        int[][] bundle;
        if(single == null || (bundle = bm.getAFromB(single)) == null || bundle.length < 2 || bundle[1] == null) return null;
        int[] ret = new int[bundle[1].length];
        System.arraycopy(bundle[1], 0, ret, 0, ret.length);
        return ret;
    }

    /**
     * Given an index to look up, gets a (newly-allocated) OrderedSet of E element keys in the bundle at that index.
     * If a variation is part of the bundle, it will not be present in this, but a copy can be retrieved with
     * {@link #getBundleVariationAt(int)}.
     * @param index an int position in the ordering to look up
     * @return an OrderedSet of the E elements used in the bundle present at index, or null if invalid
     */
    public OrderedSet<E> getBundleElementsAt(int index)
    {
        int[][] bundle;
        if((bundle = bm.getAAt(index)) == null || bundle.length < 1) return null;
        return elements.keysAt(bundle[0]);
    }
    /**
     * Given an index to look up, gets a (newly-allocated) int array that is equivalent to the variation part of the
     * bundle present at that index. If there is no variation in the corresponding bundle, then this returns null.
     * To get the E elements that are the main part of a bundle, you can use {@link #getBundleElementsAt(int)}.
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
     * Given an E element key that could be used in one or more bundles this uses, finds all S single keys corresponding
     * to bundles that contain the given element. Thus, if E was String and this BundleBiMap contained ["Copper", "Tin"]
     * mapped to "Bronze" and ["Zinc", "Copper"] mapped to "Brass", then calling this method with "Copper" would get an
     * OrderedSet that contains ["Bronze", "Brass"].
     * @param element an E element key to look up (probably a component of one or more bundles)
     * @return an OrderedSet of all S keys where the given element is part of the bundle corresponding to that S key, or null if E does not match anything
     */
    public OrderedSet<S> getManySingles(E element)
    {
        if(element == null) return null;
        int pos;
        if((pos = elements.getInt(element)) < 0) return null;
        return bm.keysB.keysAt(mm.get(pos));
    }
    /**
     * Given an E element key that could be used in one or more bundles this uses, gets all bundles in this object that
     * contain the given element, as coded 2D int arrays. These coded bundles can be given to
     * {@link #elementsFromCode(int[][])} and {@link #variationFromCode(int[][])} to get usable information from them.
     * @param element an E element key to look up (probably a component of one or more bundles)
     * @return an OrderedSet of all coded 2D int array bundles that contain the given element, or null if E is not in any bundles
     */
    public OrderedSet<int[][]> getManyCoded(E element)
    {
        if(element == null) return null;
        int pos;
        if((pos = elements.getInt(element)) < 0) return null;
        return bm.keysA.keysAt(mm.get(pos));
    }

    /**
     * Given an E element key that could be used in one or more bundles this uses, gets all indices in the ordering
     * that contain a bundle with that element.  From such an index, you can use {@link #getSingleAt(int)} to get the
     * S key at that position, {@link #getBundleElementsAt(int)} to get the E element keys at that position,
     * {@link #getBundleVariationAt(int)} to get the possible variation at that position, or {@link #getCodeAt(int)} to
     * get the coded bundle at that position.
     * @return an OrderedSet of all coded 2D int array bundles that contain the given element, or null if E is not in any bundles
     */
    public int[] getManyIndices(E element)
    {
        if(element == null) return null;
        int pos;
        if((pos = elements.getInt(element)) < 0) return null;
        return mm.get(pos).toArray();
    }

    /**
     * Reorders this BundleBiMap using {@code ordering}, which has the same length as this object's {@link #size()}
     * and can be generated with {@link ArrayTools#range(int)} (which, if applied, would produce no
     * change to the current ordering), {@link RNG#randomOrdering(int)} (which gives a random ordering, and if
     * applied immediately would be the same as calling {@link #shuffle(RNG)}), or made in some other way. If you
     * already have an ordering and want to make a different ordering that can undo the change, you can use
     * {@link ArrayTools#invertOrdering(int[])} called on the original ordering. The effects of this method, if called
     * with an ordering that has repeat occurrences of an int or contains ints that are larger than its size should
     * permit, are undefined other than the vague definition of "probably bad, unless you like crashes."
     * @param ordering an int array or vararg that must contain each int from 0 to {@link #size()}
     * @return this for chaining
     */
    public BundleBiMap<E, S> reorder(int... ordering)
    {
        if(ordering == null || ordering.length != bm.size()) return this;
        bm.reorder(ordering);
        int len = mm.size();
        for (int i = 0; i < len; i++) {
            IntVLA iv = mm.get(i);
            for (int j = 0; j < iv.size; j++) {
                iv.set(i, ordering[iv.get(i)]);
            }
        }
        return this;
    }

    /**
     * Generates a random ordering with rng and applies the same ordering to all kinds of keys this has; they will
     * maintain their current association to other keys but their ordering/indices will change.
     * @param rng an RNG to produce the random ordering this will use
     * @return this for chaining
     */
    public BundleBiMap<E, S> shuffle(RNG rng)
    {
        return reorder(rng.randomOrdering(bm.size()));
    }

    /**
     * Creates a new iterator over the individual E element keys this holds, with a larger total count the iterator may
     * yield than {@link #size()} in most cases (it should be equal to {@link #elementSize()}), and in no particular
     * order (though the order should be stable across versions and platforms, no special means are provided to further
     * control the order). The E keys are individual, not bundled, and duplicate keys should never be encountered even
     * if an E key appears in many bundles.
     * @return a newly-created iterator over this BundleBiMap's individual (non-bundled) E keys
     */
    public Iterator<E> iteratorElements()
    {
        return elements.iterator();
    }
    /**
     * Creates a new iterator over the S single keys this holds. The total number of items this yields should be equal
     * to {@link #size()}. This method can be problematic for garbage collection if called very frequently; it may be
     * better to access items by index (which also lets you access other keys associated with that index) using
     * {@link #getSingleAt(int)} in a for(int i=0...) loop.
     * @return a newly-created iterator over this BundleBiMap's S keys
     */
    public Iterator<S> iteratorSingles()
    {
        return bm.iteratorB();
    }

    /**
     * Gets and caches the individual E keys as a Collection that implements SortedSet (and so also implements Set).
     * @return the E keys as a SortedSet
     */
    public SortedSet<E> getElementSet() {
        return elements.keySet();
    }

    /**
     * Gets and caches the S single keys as a Collection that implements SortedSet (and so also implements Set).
     * @return the S keys as a SortedSet
     */
    public SortedSet<S> getSingleSet() {
        return bm.getSetB();
    }

    /**
     * To be called sparingly, since this allocates a new OrderedSet instead of reusing one.
     * @return the E keys as an OrderedSet
     */
    public OrderedSet<E> getElementOrderedSet() {
        return elements.keysAsOrderedSet();
    }

    /**
     * To be called sparingly, since this allocates a new OrderedSet instead of reusing one.
     * @return the S keys as an OrderedSet
     */
    public OrderedSet<S> getSingleOrderedSet() {
        return bm.getOrderedSetB();
    }

    /**
     * Gets the total number of bundle-to-single pairs in this BundleBiMap.
     * @return the total number of bundle keys (equivalently, the number of single keys) in this object
     */
    public int size() {
        return bm.size();
    }

    /**
     * Gets the total number of unique E element keys across all bundles in this BundleBiMap. Usually not the same as
     * {@link #size()}, and ordinarily a fair amount larger, though this can also be smaller.
     * @return the total number of unique E element keys in all bundles this contains
     */
    public int elementSize() {
        return elements.size();
    }

    public boolean isEmpty()
    {
        return bm.isEmpty();
    }

}
