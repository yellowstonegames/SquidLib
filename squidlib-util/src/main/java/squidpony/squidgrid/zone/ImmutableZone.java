package squidpony.squidgrid.zone;

/**
 * Created by Tommy Ettinger on 11/24/2016.
 */
public interface ImmutableZone extends Zone {
    /**
     * Expands the area of this Zone in the four cardinal directions, performing the expansion consecutively
     * {@code distance} times. Does not modify this Zone; returns a new Zone with the requested changes.
     * @param distance the amount to expand outward using Manhattan distance (diamond shape)
     * @return a freshly-constructed Zone with the requested changes
     */
    Zone expand(int distance);

    /**
     * Expands the area of this Zone in the four cardinal and four diagonal directions, performing the expansion
     * consecutively {@code distance} times. Does not modify this Zone; returns a new Zone with the requested changes.
     * @param distance the amount to expand outward using Chebyshev distance (square shape)
     * @return a freshly-constructed Zone with the requested changes
     */
    Zone expand8way(int distance);

}
