package squidpony.squidgrid.fov;

/**
 * This interface indicates that an object can be used to determine Field of
 * View and Line of Sight.
 *
 * The values returned need not be related to sight, they may be scent, sound,
 * etc.
 *
 * In order to support multiple such values per cell, all methods use an
 * arbitrary String key. This key is referred to by the solving algorithms and
 * passed in when calling a solver.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public interface FOVCell {

    /**
     * Returns the amount of resistance this cell has associated with the
     * given key. A value of 1 indicates that the cell is fully opaque while a
     * value of 0 indicates it is fully transparent.
     *
     * It is acceptable that this value falls outside the range of 0 to 1.
     *
     * @param key
     * @return
     */
    public float getResistance(String key);
}
