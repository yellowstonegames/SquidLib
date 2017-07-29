package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import squidpony.ArrayTools;

/**
 * Created by Tommy Ettinger on 7/28/2017.
 */
public class SparseLayers extends Actor {
    public final int width, height;
    public float[][] backgrounds;
    public Array<SparseTextMap> layers;
    protected IntIntMap mapping;
    public TextCellFactory font;
    public float xOffset, yOffset;

    public SparseLayers(int width, int height)
    {
        this(width, height, 10, 16, DefaultResources.getStretchableFont());
    }

    public SparseLayers(int width, int height, float cellWidth, float cellHeight)
    {
        this(width, height, cellWidth, cellHeight, DefaultResources.getStretchableFont());
    }

    public SparseLayers(int width, int height, float cellWidth, float cellHeight, TextCellFactory font) {
        this(width, height, cellWidth, cellHeight, font, 0f, 0f);
    }

    public SparseLayers(int width, int height, float cellWidth, float cellHeight, TextCellFactory font, float xOffset, float yOffset) {
        this.width = MathUtils.clamp(width, 1, 65535);
        this.height = MathUtils.clamp(height, 1, 65535);
        backgrounds = ArrayTools.fill(0f, this.width, this.height);
        layers = new Array<>(true, 4, SparseTextMap.class);
        this.font = font.width(cellWidth).height(cellHeight).initBySize();
        layers.add(new SparseTextMap(width * height >> 2));
        mapping = new IntIntMap(4);
        mapping.put(0, 0);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        setBounds(xOffset, yOffset,
                this.font.actualCellWidth * this.width, this.font.actualCellHeight * this.height);
    }

    public void put(int x, int y, char c, Color foreground, Color background)
    {
        put(x, y, c, foreground == null ? 0f : foreground.toFloatBits(),
                background == null ? 0f : background.toFloatBits());
    }

    public void put(int x, int y, char c, float foreground, float background)
    {
        if(x < 0 || x >= width || y < 0 || y >= height)
            return;
        if(background != 0f)
            backgrounds[x][y] = background;
        if(foreground != 0f)
            layers.items[0].place(x, y, c, foreground);
    }

    public void put(int x, int y, char c, Color foreground, Color background, int layer)
    {
        put(x, y, c, foreground == null ? 0f : foreground.toFloatBits(),
                background == null ? 0f : background.toFloatBits(), layer);

    }

    public void put(int x, int y, char c, float foreground, float background, int layer)
    {
        if(x < 0 || x >= width || y < 0 || y >= height || layer < 0)
            return;
        if(background != 0f)
            backgrounds[x][y] = background;
        layer = mapping.get(layer, layer);
        if(foreground != 0f)
        {
            if(layer >= layers.size)
            {
                mapping.put(layer, layers.size);
                SparseTextMap stm = new SparseTextMap(width * height >> 4);
                stm.place(x, y, c, foreground);
                layers.add(stm);
            }
            else
            {
                layers.get(layer).place(x, y, c, foreground);
            }
        }
    }
    public void print(int x, int y, String text, Color foreground, Color background) {
        print(x, y, text, foreground == null ? 0f : foreground.toFloatBits(),
                background == null ? 0f : background.toFloatBits());
    }

    public void print(int x, int y, String text, float foreground, float background)
    {
        int len = Math.min(text.length(), width - x);
        for (int i = 0; i < len; i++) {
            put(x + i, y, text.charAt(i), foreground, background);
        }
    }

    public void print(int x, int y, String text, Color foreground, Color background, int layer) {
        print(x, y, text, foreground == null ? 0f : foreground.toFloatBits(),
                background == null ? 0f : background.toFloatBits(), layer);
    }
    public void print(int x, int y, String text, float foreground, float background, int layer) {
        if (x < 0 || x >= width || y < 0 || y >= height || layer < 0)
            return;
        int len = Math.min(text.length(), width - x);
        if(background != 0f) {
            for (int i = 0; i < len; i++) {
                backgrounds[x + i][y] = background;
            }
        }
        if (foreground != 0f)
        {
            layer = mapping.get(layer, layer);
            if (layer >= layers.size) {
                mapping.put(layer, layers.size);
                SparseTextMap stm = new SparseTextMap(width * height >> 4);
                for (int i = 0; i < len; i++) {
                    stm.place(x + i, y, text.charAt(i), foreground);
                }
                layers.add(stm);
            } else {
                SparseTextMap stm = layers.get(layer);
                for (int i = 0; i < len; i++) {
                    stm.place(x + i, y, text.charAt(i), foreground);
                }
            }
        }
    }

    public void changeBackground(int x, int y, Color color)
    {
        changeBackground(x, y, color == null ? 0f : color.toFloatBits());
    }
    public void changeBackground(int x, int y, float color)
    {
        if(x < 0 || x >= width || y < 0 || y >= height)
            return;
        backgrounds[x][y] = color;
    }
    public void erase(int x, int y)
    {
        if(x < 0 || x >= width || y < 0 || y >= height)
            return;
        backgrounds[x][y] = 0f;
        int code = SparseTextMap.encodePosition(x, y);
        for (int i = 0; i < layers.size; i++) {
            layers.get(i).remove(code);
        }
    }
    public void removeChar(int x, int y, int layer)
    {
        layer = mapping.get(layer, -1);
        if(layer >= 0)
        {
            layers.get(layer).remove(SparseTextMap.encodePosition(x, y));
        }
    }

    public void clear()
    {
        ArrayTools.fill(backgrounds, 0f);
        for (int i = 0; i < layers.size; i++) {
            layers.get(i).clear();
        }
    }

    /**
     * Draws the actor. The batch is configured to draw in the parent's coordinate system.
     * {@link Batch#draw(TextureRegion, float, float, float, float, float, float, float, float, float)
     * This draw method} is convenient to draw a rotated and scaled TextureRegion. {@link Batch#begin()} has already been called on
     * the batch. If {@link Batch#end()} is called to draw without the batch then {@link Batch#begin()} must be called before the
     * method returns.
     *
     * @param batch a Batch such as a SpriteBatch that must be between a begin() and end() call; usually done by Stage
     * @param parentAlpha currently ignored
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        font.draw(batch, backgrounds, xOffset, yOffset);
        int len = layers.size;
        float yOff2 = yOffset + 1f + height * font.actualCellHeight;
        for (int i = 0; i < len; i++) {
            layers.get(i).draw(batch, font, xOffset, yOff2);
        }
    }
}
