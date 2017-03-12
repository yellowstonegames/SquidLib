package squidpony.squidgrid.zone;

/**
 * Created by Tommy Ettinger on 11/24/2016.
 */
public interface MutableZone extends Zone {

    /**
     * Expands this Zone in the four cardinal directions, performing the expansion consecutively {@code distance} times.
     * Modified this Zone in-place and returns it for chaining.
     * @param distance the amount to expand outward using Manhattan distance (diamond shape)
     * @return this for chaining, after being modified in-place
     */
    Zone expand(int distance);

    /**
     * Expands this Zone in the four cardinal and four diagonal directions, performing the expansion consecutively
     * {@code distance} times. Modified this Zone in-place and returns it for chaining.
     * @param distance the amount to expand outward using Chebyshev distance (square shape)
     * @return this for chaining, after being modified in-place
     */
    Zone expand8way(int distance);
}
