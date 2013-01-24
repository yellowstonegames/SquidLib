package squidpony.squidutility;

import java.util.List;
import java.util.Queue;
import squidpony.squidmath.RNG;

/**
 * This utility class contains functions for working with Collections.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SCollections {

    private static RNG rng = new RNG();

    /**
     * Returns a random element from the provided list. If the list is empty
     * then null is returned.
     *
     * @param list
     * @return
     */
    public static <T> T getRandomElement(List<T> list) {
        if (list.size() <= 0) {
            return null;
        }
        return list.get(rng.nextInt(list.size()));
    }

    /**
     * Returns a random elements from the provided queue. If the queue is empty
     * then null is returned.
     *
     * @param list
     * @return
     */
    public static <T> T getRandomElement(Queue<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return (T) list.toArray()[rng.nextInt(list.size())];
    }
}
