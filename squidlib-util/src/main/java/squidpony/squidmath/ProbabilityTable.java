package squidpony.squidmath;

import squidpony.annotation.Beta;

import java.io.Serializable;
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
     * The set of items that can be produced indirectly from {@link #random()} (looking up values from inside
     * the nested tables). Uses identity equality and hashes values by identity, which allows the nested tables to be
     * modified but makes looking them up more of a challenge at times (you need to pass the same object to
     * {@link #weight(ProbabilityTable)} as you did to {@link #add(ProbabilityTable, int)}, though it may have changed).
     */
    public final Arrangement<ProbabilityTable<? extends T>> extraTable;
    public final IntVLA weights;
    protected RNG rng;
    protected int total, normalTotal;

    /**
     * Creates a new probability table with a random seed.
     */
    public ProbabilityTable() {
        this(new StatefulRNG());
    }

    /**
     * Creates a new probability table with the provided source of randomness
     * used. Gets one random long from rng to use as an internal identifier.
     *
     * @param rng the source of randomness
     */
    public ProbabilityTable(RNG rng) {
        this.rng = rng;
        table = new Arrangement<>(64, 0.75f);
        extraTable = new Arrangement<>(16, 0.75f, CrossHash.identityHasher);
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
        this.rng = new StatefulRNG(seed);
        table = new Arrangement<>(64, 0.75f);
        extraTable = new Arrangement<>(16, 0.75f, CrossHash.identityHasher);
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
        this(CrossHash.Lightning.hash64(seed));
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
        int index = rng.nextInt(total), sz = table.size();
        for (int i = 0; i < sz; i++) {
            index -= weights.get(i);
            if (index < 0)
                return table.keyAt(i);
        }
        for (int i = 0; i < extraTable.size(); i++) {
            index -= weights.get(sz + i);
            if(index < 0)
                return extraTable.keyAt(i).random();
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
            weights.insert(table.size(), Math.max(0, weight));
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
    public ProbabilityTable<T> add(ProbabilityTable<? extends T> table, int weight) {
        if(weight <= 0 || table == null || contentEquals(table) || table.total <= 0)
            return this;
        int i = extraTable.getInt(table);
        if (i < 0) {
            weights.add(Math.max(0, weight));
            extraTable.add(table);
            total += Math.max(0, weight);
        } else {
            int i2 = weights.get(i);
            int w = Math.max(0, i2 + weight);
            weights.set(i, w);
            total += w - i2;
        }
        return this;
    }

    /**
     * Returns the weight of the item if the item is in the table. Returns zero
     * of the item is not in the table.
     *
     * @param item the item searched for
     * @return the weight of the item, or zero
     */
    public int weight(T item) {
        int i = table.getInt(item);
        return i < 0 ? 0 : weights.get(i);
    }

    /**
     * Returns the weight of the item if the item is in the table. Returns zero
     * of the item is not in the table.
     *
     * @param item the item searched for
     * @return the weight of the item, or zero
     */
    public int weight(ProbabilityTable<? extends T> item) {
        int i = extraTable.getInt(item);
        return i < 0 ? 0 : weights.get(i + table.size());
    }

    /**
     * Provides a set of the items in this table, without reference to their
     * weight. Does not include nested ProbabilityTable values; for that, use tables().
     *
     * @return a "sorted" set of all items stored, really sorted in insertion order
     */
    public SortedSet<T> items() {
        return table.keySet();
    }

    /**
     * Provides a set of the nested ProbabilityTable values in this table, without reference
     * to their weight. Does not include normal values (non-table); for that, use items().
     *
     * @return a "sorted" set of all nested tables stored, really sorted in insertion order
     */
    public SortedSet<ProbabilityTable<? extends T>> tables() {
        return extraTable.keySet();
    }

    /**
     * Sets the current RNG to the given RNG. You may prefer using a StatefulRNG (typically passing one in the
     * constructor, but you can pass one here too) and setting its state in other code, which does not require calling
     * this method again when the StatefulRNG has its state set.
     * @param random an RNG, typically with a seed you want control over; may be a StatefulRNG or some other subclass
     */
    public void setRandom(RNG random)
    {
        rng = random;
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

    public boolean contentEquals(ProbabilityTable<? extends T> o) {
        if (this == o) return true;
        if (o == null) return false;

        if (!table.equals(o.table)) return false;
        if (!extraTable.equals(o.extraTable)) return false;
        return weights.equals(o.weights);
    }

    @Override
    public int hashCode() {
        int result = table.hashCode();
        result = 31 * result + extraTable.hashCode();
        result = 31 * result + weights.hashCode();
        result = 31 * result + (rng != null ? rng.hashCode() : 0);
        return result;
    }
}
