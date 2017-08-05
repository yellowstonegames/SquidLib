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
