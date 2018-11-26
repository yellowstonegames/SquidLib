package squidpony.squidmath;

import squidpony.annotation.Beta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.SortedSet;

/**
 * A generic method of holding a probability table to determine weighted random
 * outcomes.
 *
 * The weights do not need to add up to any particular value, they will be
 * normalized when choosing a random entry.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 *
 * @param <T> The type of object to be held in the table
 */
@Beta
public class ProbabilityTable<T> implements Serializable {
    private static final long serialVersionUID = -1307656083434154736L;
    /**
     * The set of items that can be produced directly from {@link #random()} (without additional lookups).
     */
    public final Arrangement<T> table;
    /**
     * The list of items that can be produced indirectly from {@link #random()} (looking up values from inside
     * the nested tables).
     */
    public final ArrayList<ProbabilityTable<T>> extraTable;
    public final IntVLA weights;
    public GWTRNG rng;
    protected int total, normalTotal;

    /**
     * Creates a new probability table with a random seed.
     */
    public ProbabilityTable() {
        this((RandomnessSource) null);
    }

    /**
     * Creates a new probability table with the provided source of randomness
     * used.
     *
     * @param rng the source of randomness
     */
    public ProbabilityTable(RandomnessSource rng) {
        this.rng = rng == null ? new GWTRNG() : new GWTRNG(rng.next(32), rng.next(32));
        table = new Arrangement<>(64, 0.75f);
        extraTable = new ArrayList<>(16);
        weights = new IntVLA(64);
        total = 0;
        normalTotal = 0;
    }

    /**
     * Creates a new probability table with the provided long seed used.
     *
     * @param seed the RNG seed as a long
     */
    public ProbabilityTable(long seed) {
        this.rng = new GWTRNG(seed);
        table = new Arrangement<>(64, 0.75f);
        extraTable = new ArrayList<>(16);
        weights = new IntVLA(64);
        total = 0;
        normalTotal = 0;
    }

    /**
     * Creates a new probability table with the provided String seed used.
     *
     * @param seed the RNG seed as a String
     */
    public ProbabilityTable(String seed) {
        this(CrossHash.hash64(seed));
    }

    /**
     * Returns an object randomly based on assigned weights.
     *
     * Returns null if no elements have been put in the table.
     *
     * @return the chosen object or null
     */
    public T random() {
        if (table.isEmpty()) {
            return null;
        }
        int index = (int) ((total * ((long)rng.next(31))) >>> 31), sz = table.size();
        for (int i = 0; i < sz; i++) {
            index -= weights.get(i);
            if (index < 0)
                return table.keyAt(i);
        }
        for (int i = 0; i < extraTable.size(); i++) {
            index -= weights.get(sz + i);
            if(index < 0)
                return extraTable.get(i).random();
        }
        return null;//something went wrong, shouldn't have been able to get all the way through without finding an item
    }

    /**
     * Adds the given item to the table.
     *
     * Weight must be greater than 0.
     *
     * @param item the object to be added
     * @param weight the weight to be given to the added object
     * @return this for chaining
     */
    public ProbabilityTable<T> add(T item, int weight) {
        if(weight <= 0)
            return this;
        int i = table.getInt(item);
        if (i < 0) {
            weights.insert(table.size, Math.max(0, weight));
            table.add(item);
            int w = Math.max(0, weight);
            total += w;
            normalTotal += w;
        } else {
            int i2 = weights.get(i);
            int w = Math.max(0, i2 + weight);
            weights.set(i, w);
            total += w - i2;
            normalTotal += w - i2;
        }
        return this;
    }

    /**
     * Given an OrderedMap of T element keys and Integer weight values, adds all T keys with their corresponding weights
     * into this ProbabilityTable. You may want to use {@link OrderedMap#makeMap(Object, Object, Object...)} to produce
     * the parameter, unless you already have one.
     * @param itemsAndWeights an OrderedMap of T keys to Integer values, where a key will be an item this can retrieve
     *                        and a value will be its weight
     * @return this for chaining
     */
    public ProbabilityTable<T> addAll(OrderedMap<T, Integer> itemsAndWeights)
    {
        if(itemsAndWeights == null) return this;
        int sz = itemsAndWeights.size;
        for (int i = 0; i < sz; i++) {
            add(itemsAndWeights.keyAt(i), itemsAndWeights.getAt(i));
        }
        return this;
    }

    /**
     * Removes the possibility of generating the given T item, except by nested ProbabilityTable results.
     * Returns true iff the item was removed.
     * @param item the item to make less likely or impossible
     * @return true if the probability changed or false if nothing changed
     */
    public boolean remove(T item)
    {
        return remove(item, weight(item));
    }

    /**
     * Reduces the likelihood of generating the given T item by the given weight, which can reduce the chance below 0
     * and thus remove the item entirely. Does not affect nested ProbabilityTables. The value for weight must be greater
     * than 0, otherwise this simply returns false without doing anything. Returns true iff the probabilities changed.
     * @param item the item to make less likely or impossible
     * @param weight how much to reduce the item's weight by, as a positive non-zero int (greater values here subtract
     *               more from the item's weight)
     * @return true if the probability changed or false if nothing changed
     */
    public boolean remove(T item, int weight)
    {
        if(weight <= 0)
            return false;
        int idx = table.getInt(item);
        if(idx < 0)
            return false;
        int o = weights.get(idx);
        weights.incr(idx, -weight);
        int w = weights.get(idx);
        if(w <= 0)
        {
            table.removeAt(idx);
            weights.removeIndex(idx);
        }
        w = Math.min(o, o - w);
        total -= w;
        normalTotal -= w;
        return true;
    }

    /**
     * Given an Iterable of T item keys to remove, this tries to remove each item in items, though it can't affect items
     * in nested ProbabilityTables, and returns true if any probabilities were changed.
     * @param items an Iterable of T items that will all be removed from the normal (non-nested) items in this
     * @return true if the probabilities changed, or false otherwise
     */
    public boolean removeAll(Iterable<T> items)
    {
        boolean changed = false;
        for(T t : items)
        {
            changed |= remove(t);
        }
        return changed;
    }

    /**
     * Given an OrderedMap of T item keys and Integer weight values, reduces the weights in this ProbabilityTable for
     * all T keys by their corresponding weights, removing them if the weight becomes 0 or less. You may want to use
     * {@link OrderedMap#makeMap(Object, Object, Object...)} to produce the parameter, unless you already have one.
     * Returns true iff the probabilities changed.
     * @param itemsAndWeights an OrderedMap of T keys to Integer values, where a key will be an item that should be
     *                        reduced in weight or removed and a value will be that item's weight
     * @return true if the probabilities changed or false otherwise
     */
    public boolean removeAll(OrderedMap<T, Integer> itemsAndWeights)
    {
        if(itemsAndWeights == null) return false;
        boolean changed = false;
        int sz = itemsAndWeights.size;
        for (int i = 0; i < sz; i++) {
            changed |= remove(itemsAndWeights.keyAt(i), itemsAndWeights.getAt(i));
        }
        return changed;
    }

    /**
     * Adds the given probability table as a possible set of results for this table.
     * The table parameter should not be the same object as this ProbabilityTable, nor should it contain cycles
     * that could reference this object from inside the values of table. This could cause serious issues that would
     * eventually terminate in a StackOverflowError if the cycles randomly repeated for too long. Only the first case
     * is checked for (if the contents of this and table are equivalent, it returns without doing anything; this also
     * happens if table is empty or null).
     *
     * Weight must be greater than 0.
     *
     * @param table the ProbabilityTable to be added; should not be the same as this object (avoid cycles)
     * @param weight the weight to be given to the added table
     * @return this for chaining
     */
    public ProbabilityTable<T> add(ProbabilityTable<T> table, int weight) {
        if(weight <= 0 || table == null || contentEquals(table) || table.total <= 0)
            return this;
        weights.add(Math.max(0, weight));
        extraTable.add(table);
        total += Math.max(0, weight);
        return this;
    }

    /**
     * Given an OrderedMap of ProbabilityTable keys and Integer weight values, adds all keys as nested tables with their
     * corresponding weights into this ProbabilityTable. All ProbabilityTable keys should have the same T type as this
     * ProbabilityTable. You may want to use {@link OrderedMap#makeMap(Object, Object, Object...)} to produce the
     * parameter, unless you already have one.
     *
     * The same rules apply to this as apply to {@link #add(ProbabilityTable, int)}; that is, no key in itemsAndWeights
     * can be the same object as this ProbabilityTable, nor should any key contain cycles that could reference this
     * object from inside the values of a key. This could cause serious issues that would eventually terminate in a
     * StackOverflowError if the cycles randomly repeated for too long. Only the first case is checked for (if the
     * contents of this and a key are equivalent, it ignores that key; this also
     * happens if a key is empty or null).

     * @param itemsAndWeights an OrderedMap of T keys to Integer values, where a key will be an item this can retrieve
     *                        and a value will be its weight
     * @return this for chaining
     */
    public ProbabilityTable<T> addAllNested(OrderedMap<ProbabilityTable<T>, Integer> itemsAndWeights)
    {
        if(itemsAndWeights == null) return this;
        int sz = itemsAndWeights.size;
        for (int i = 0; i < sz; i++) {
            add(itemsAndWeights.keyAt(i), itemsAndWeights.getAt(i));
        }
        return this;
    }

    /**
     * Returns the weight of the item if the item is in the table. Returns zero
     * if the item is not in the table.
     *
     * @param item the item searched for
     * @return the weight of the item, or zero
     */
    public int weight(T item) {
        int i = table.getInt(item);
        return i < 0 ? 0 : weights.get(i);
    }

    /**
     * Returns the weight of the extra table if present. Returns zero
     * if the extra table is not present.
     *
     * @param item the extra ProbabilityTable to search for
     * @return the weight of the ProbabilityTable, or zero
     */
    public int weight(ProbabilityTable<T> item) {
        int i = extraTable.indexOf(item);
        return i < 0 ? 0 : weights.get(i + table.size());
    }

    /**
     * Provides a set of the items in this table, without reference to their
     * weight. Includes nested ProbabilityTable values, but as is the case throughout
     * this class, cyclical references to ProbabilityTable values that reference this
     * table will result in significant issues (such as a {@link StackOverflowError}
     * crashing your program).
     *
     * @return an OrderedSet of all items stored; iteration order should be predictable
     */
    public OrderedSet<T> items() {
        OrderedSet<T> os = table.keysAsOrderedSet();
        for (int i = 0; i < extraTable.size(); i++) {
            os.addAll(extraTable.get(i).items());
        }
        return os;
    }

    /**
     * Provides a set of the items in this table that are not in nested tables, without
     * reference to their weight. These are the items that are simple to access, hence
     * the name. If you want the items that are in both the top-level and nested tables,
     * you can use {@link #items()}.
     * @return a predictably-ordered set of the items in the top-level table
     */
    public SortedSet<T> simpleItems()
    {
        return table.keySet();
    }

    /**
     * Provides a set of the nested ProbabilityTable values in this table, without reference
     * to their weight. Does not include normal values (non-table); for that, use items().
     *
     * @return a "sorted" set of all nested tables stored, really sorted in insertion order
     */
    public ArrayList<ProbabilityTable<T>> tables() {
        return extraTable;
    }

    /**
     * Sets the current random number generator to the given GWTRNG.
     * @param random an RNG, typically with a seed you want control over; may be a StatefulRNG or some other subclass
     */
    public void setRandom(GWTRNG random)
    {
        if(random != null)
            rng = random;
    }

    /**
     * Gets the random number generator (a RandomnessSource) this uses.
     * @return the RandomnessSource used by this class, which is always a GWTRNG
     */
    public GWTRNG getRandom()
    {
        return rng;
    }

    /**
     * Copies this ProbabilityTable so nothing in the copy is shared with the original, except for the T items (which
     * might not be possible to copy). The RNG is also copied.
     * @return a copy of this ProbabilityTable; no references should be shared except for T items
     */
    public ProbabilityTable<T> copy()
    {
        ProbabilityTable<T> n = new ProbabilityTable<>(rng.getState());
        n.weights.addAll(weights);
        n.table.putAll(table);
        for (int i = 0; i < extraTable.size(); i++) {
            n.extraTable.add(extraTable.get(i).copy());
        }
        n.total = total;
        n.normalTotal = normalTotal;
        return n;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProbabilityTable<?> that = (ProbabilityTable<?>) o;

        if (!table.equals(that.table)) return false;
        if (!extraTable.equals(that.extraTable)) return false;
        if (!weights.equals(that.weights)) return false;
        return rng != null ? rng.equals(that.rng) : that.rng == null;
    }

    public boolean contentEquals(ProbabilityTable<T> o) {
        if (this == o) return true;
        if (o == null) return false;

        if (!table.equals(o.table)) return false;
        if (!extraTable.equals(o.extraTable)) return false;
        return weights.equals(o.weights);
    }

    @Override
    public int hashCode() {
        int result = table.hashCode();
        result = 31 * result + extraTable.hashCode() | 0;
        result = 31 * result + weights.hashWisp() | 0;
        result = 31 * result + (rng != null ? rng.hashCode() : 0) | 0;
        return result;
    }
}
