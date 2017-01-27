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
    public final Arrangement<T> table;
    public final IntVLA weights;
    protected RNG rng;
    protected int total;

    /**
     * Creates a new probability table.
     */
    public ProbabilityTable() {
        this(new StatefulRNG());
    }

    /**
     * Creates a new probability table with the provided source of randomness
     * used.
     *
     * @param rng the source of randomness
     */
    public ProbabilityTable(RNG rng) {
        this.rng = rng;
        table = new Arrangement<>(64, 0.75f);
        weights = new IntVLA(64);
        total = 0;
    }

    /**
     * Creates a new probability table with the provided long seed used.
     *
     * @param seed the RNG seed as a long
     */
    public ProbabilityTable(long seed) {
        this.rng = new StatefulRNG(seed);
        table = new Arrangement<>(64, 0.75f);
        weights = new IntVLA(64);
        total = 0;
    }

    /**
     * Creates a new probability table with the provided String seed used.
     *
     * @param seed the RNG seed as a String
     */
    public ProbabilityTable(String seed) {
        this.rng = new StatefulRNG(CrossHash.Lightning.hash64(seed));
        table = new Arrangement<>(64, 0.75f);
        weights = new IntVLA(64);
        total = 0;
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
        int index = rng.nextInt(total);
        for (int i = 0; i < table.size(); i++) {
            index -= weights.get(i);
            if (index < 0) {
                return table.keyAt(i);
            }
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
        int i = table.getInt(item);
        if (i < 0) {
            table.add(item);
            weights.add(Math.max(0, weight));
            total += Math.max(0, weight);
        } else {
            i = weights.get(i);
            table.add(item);
            weights.add(Math.max(0, i + weight));
            total += Math.max(0, i + weight) - i;
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
     * Provides a set of the items in this table, without reference to their
     * weight.
     *
     * @return a "sorted" set of all items stored, really sorted in insertion order
     */
    public SortedSet<T> items() {
        return table.keySet();
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
}
