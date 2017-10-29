package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import squidpony.panel.ISquidPanel;

/**
 * Created by Tommy Ettinger on 8/5/2017.
 */
public interface IPackedColorPanel extends ISquidPanel<Color> {
    /**
     * Places a full cell of color at the given x,y position; this may be used as a background or foreground, depending
     * on the implementation. The color is given as a packed float, the kind produced by {@link Color#toFloatBits()}.
     * If the implementation performs color filtering on Color objects, it generally won't on packed float colors.
     * @param x x position of the cell
     * @param y y position of the cell
     * @param color color for the full cell as a packed float, as made by {@link Color#toFloatBits()}
     */
    void put(int x, int y, float color);

    /**
     * Using the existing color at the position x,y, this performs color blending from that existing color to the given
     * color (as a float), using the mixBy parameter to determine how much of the color parameter to use (1f will set
     * the color in this to the parameter, while 0f for mixBy will ignore the color parameter entirely).
     * @param x the x component of the position in this panel to draw the starting color from
     * @param y the y component of the position in this panel to draw the starting color from
     * @param color the new color to mix with the starting color; a packed float, as made by {@link Color#toFloatBits()}
     * @param mixBy the amount by which the new color will affect the old one, between 0 (no effect) and 1 (overwrite)
     */
    void blend(int x, int y, float color, float mixBy);

    /**
     * Places a char in the given color at the given x,y position; if the implementation has a separate background from
     * the foreground characters, this will not affect it. The color is given as a packed float, the kind produced by
     * {@link Color#toFloatBits()}. If the implementation performs color filtering on Color objects, it generally won't
     * on packed float colors.
     * @param x x position of the char
     * @param y y position of the char
     * @param c the char to put at the given cell
     * @param encodedColor the color for the char as a packed float, as made by {@link Color#toFloatBits()}
     */
    void put(int x, int y, char c, float encodedColor);
}
