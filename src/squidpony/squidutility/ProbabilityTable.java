package squidpony.squidutility;

import java.util.ArrayList;
import squidpony.annotation.Beta;
import squidpony.squidmath.RNG;

/**
 * A generic method of holding a probability table to determine weighted random
 * outcomes.
 *
 * The weights do not need to add up to any particular value, they will be
 * normalized when choosing a random entry.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class ProbabilityTable<T> {

    private final ArrayList<Pair<Integer, T>> table = new ArrayList<>();
    private static final RNG rng = new RNG();
    private int total = 0;

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
        for (int i = 0; i < table.size(); i++) {//start looping at second item
            index -= table.get(i).getFirst();
            if (index < 0) {
                return table.get(i).getSecond();
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
     */
    public void add(T item, int weight) {
        table.add(new Pair(weight, item));
        total += weight;
    }
}
