package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import squidpony.IFilter;

/**
 * Like {@link IFilter}, but produces packed floats that encode colors instead of {@link Color} objects.
 * Accepts packed float colors (as produced by {@link Color#toFloatBits()}, or given in SColor documentation) or Color
 * objects (including {@link SColor} instances).
 * <br>
 * Created by Tommy Ettinger on 7/22/2018.
 */
public abstract class FloatFilter {
    /**
     * Takes a packed float color and produces a potentially-different packed float color that this FloatFilter edited.
     * @param color a packed float color, as produced by {@link Color#toFloatBits()}
     * @return a packed float color, as produced by {@link Color#toFloatBits()}
     */
    public abstract float alter(float color);
    /**
     * Takes a {@link Color} or subclass of Color (such as {@link SColor}, which is a little more efficient here) and
     * produces a packed float color that this FloatFilter edited.
     * @param color a {@link Color} or instance of a subclass such as {@link SColor} 
     * @return a packed float color, as produced by {@link Color#toFloatBits()}
     */
    public float alter(Color color)
    {
        return alter(color.toFloatBits());
    }
}
