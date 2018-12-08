package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.g2d.FloatFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * A drop-in substitute for {@link SpriteBatch} that filters any colors used to tint text or images using a
 * {@link FloatFilter}. The filter may have an effect on speed in some cases, but even moderately complex filters like
 * {@link FloatFilters.YCbCrFilter} seem to perform perfectly well, spiking at above 1000 FPS on SparseDemo with a
 * filter that changes parameters.
 * <br>
 * Delegates pretty much everything to a different FilterBatch class in a libGDX package, needed for package-private
 * access to an important field.
 * <br>
 * Created by Tommy Ettinger on 8/2/2018.
 */
public class FilterBatch extends com.badlogic.gdx.graphics.g2d.FilterBatch {
    /**
     * Constructs a new SpriteBatch with a size of 1000, one buffer, and the default shader.
     *
     * @see SpriteBatch#SpriteBatch(int, ShaderProgram)
     */
    public FilterBatch() {
        super();
    }

    /**
     * Constructs a SpriteBatch with one buffer and the default shader.
     *
     * @param size
     * @see SpriteBatch#SpriteBatch(int, ShaderProgram)
     */
    public FilterBatch(int size) {
        super(size);
    }

    /**
     * Constructs a new SpriteBatch. Sets the projection matrix to an orthographic projection with y-axis point upwards, x-axis
     * point to the right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
     * respect to the current screen resolution.
     * <p>
     * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
     * the ones expected for shaders set with {@link #setShader(ShaderProgram)}. See {@link #createDefaultShader()}.
     *
     * @param size          The max number of sprites in a single batch. Max of 8191.
     * @param defaultShader The default shader to use. This is not owned by the SpriteBatch and must be disposed separately.
     */
    public FilterBatch(int size, ShaderProgram defaultShader) {
        super(size, defaultShader);
    }

    /**
     * Constructs a new SpriteBatch with a size of 1000, one buffer, the default shader, and the given FloatFilter.
     * @param filter        a {@link FloatFilter}, such as one from {@link FloatFilters}
     * @see SpriteBatch#SpriteBatch(int, ShaderProgram)
     */
    public FilterBatch(FloatFilter filter) {
        super(filter);
    }

    /**
     * Constructs a SpriteBatch with one buffer of the given size, the default shader, and the given FloatFilter.
     *
     * @param size          The max number of sprites in a single batch. Max of 8191.
     * @param filter        a {@link FloatFilter}, such as one from {@link FloatFilters}
     * @see SpriteBatch#SpriteBatch(int, ShaderProgram)
     */
    public FilterBatch(int size, FloatFilter filter) {
        super(size, filter);
    }

    /**
     * Constructs a new SpriteBatch with the given FloatFilter.
     * Sets the projection matrix to an orthographic projection with y-axis pointing upwards, x-axis pointing to the
     * right and the origin being in the bottom left corner of the screen. The projection will be pixel perfect with
     * respect to the current screen resolution.
     * <p>
     * The defaultShader specifies the shader to use. Note that the names for uniforms for this default shader are different than
     * the ones expected for shaders set with {@link #setShader(ShaderProgram)}. See {@link #createDefaultShader()}.
     *
     * @param size          The max number of sprites in a single batch. Max of 8191.
     * @param defaultShader The default shader to use. This is not owned by the SpriteBatch and must be disposed separately.
     * @param filter        a {@link FloatFilter}, such as one from {@link FloatFilters}
     */
    public FilterBatch(int size, ShaderProgram defaultShader, FloatFilter filter) {
        super(size, defaultShader, filter);
    }
}
