package squidpony.squidgrid.fov;

/**
 * This interface indicates that an object can be used to determine Field of
 * View and Line of Sight.
 *
 * The values returned may not be related to sight, they may be scent, sound,
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
     * Returns the amount of transparency this cell has associated with the
     * given key. A value of 0 indicates that the cell is fully opaque while a
     * value of 1 indicates it is fully transparent.
     *
     * It is acceptable that this value falls outside the range of 0 to 1.
     *
     * @param key
     * @return
     */
    public float getTransparency(String key);

    /**
     * Returns the amount of light currently in this cell. Generally a value of
     * 1 or higher indicates fully lit while a value of 0 or lower indicates
     * fully dark.
     *
     * @param key
     * @return
     */
    public float getCurrentLight(String key);

    /**
     * Returns the amount of resistance this cell provides to objects trying to
     * pass through. Generally values of 1 or higher means that objects may not
     * pass through, even if light may, such as in the case of glass. Values of
     * 0 or lower indicate that this cell offers no resistance to normal
     * objects.
     *
     * @param key
     * @return
     */
    public float getReistance(String key);
}
