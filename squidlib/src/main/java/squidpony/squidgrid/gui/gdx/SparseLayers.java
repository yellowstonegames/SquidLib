package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.SnapshotArray;
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
    protected int animationCount = 0;
    private SnapshotArray<TextCellFactory.Glyph> glyphs;


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
        glyphs = new SnapshotArray<>(true, 16, TextCellFactory.Glyph.class);
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

    public boolean hasActiveAnimations()
    {
        return animationCount > 0;
    }

    public TextCellFactory.Glyph glyph(char shown, float color, int x, int y)
    {
        TextCellFactory.Glyph g =
                font.glyph(shown, color,
                        xOffset + x * font.actualCellWidth,
                        yOffset + (height - y) * font.actualCellHeight);
        glyphs.add(g);
        return g;
    }

    public TextCellFactory.Glyph glyphFromGrid(int x, int y)
    {
        int code = SparseTextMap.encodePosition(x, y);
        char shown = layers.items[0].getChar(code, ' ');
        float color = layers.items[0].getFloat(code, 0f);
        layers.items[0].remove(code);
        TextCellFactory.Glyph g =
                font.glyph(shown, color, xOffset + x * font.actualCellWidth, yOffset + (height - y) * font.actualCellHeight);
        glyphs.add(g);
        return g;
    }

    public TextCellFactory.Glyph glyphFromGrid(int x, int y, int layer) {
        layer = mapping.get(layer, -1);
        if (layer >= 0) {
            SparseTextMap l = layers.get(layer);
            int code = SparseTextMap.encodePosition(x, y);
            char shown = l.getChar(code, ' ');
            float color = l.getFloat(code, 0f);
            l.remove(code);
            TextCellFactory.Glyph g =
                    font.glyph(shown, color, xOffset + x * font.actualCellWidth, yOffset + (height - y) * font.actualCellHeight);
            glyphs.add(g);
            return g;
        }
        else
        {
            return null;
        }
    }

    public void recallToGrid(TextCellFactory.Glyph glyph)
    {
        layers.items[0].place(Math.round((glyph.getX() - xOffset) / font.actualCellWidth),
                Math.round((glyph.getY() - yOffset)  / -font.actualCellHeight + height),
                glyph.shown, glyph.color);
        glyphs.removeValue(glyph, true);
    }

    public void recallToGrid(TextCellFactory.Glyph glyph, int layer)
    {
        layer = mapping.get(layer, 0);
        layers.get(layer).place(Math.round((glyph.getX() - xOffset) / font.actualCellWidth),
                Math.round((glyph.getY() - yOffset)  / -font.actualCellHeight + height),
                glyph.shown, glyph.color);
        glyphs.removeValue(glyph, true);
    }

    /**
     * Slides {@code name} from {@code (x,y)} to {@code (newx, newy)}. If
     * {@code name} or {@code color} is {@code null}, it is picked from this
     * panel (thereby removing the current name, if any). This also allows
     * a Runnable to be given as {@code postRunnable} to be run after the
     * slide completes.
     *
     * @param x
     *            Where to start the slide, horizontally.
     * @param y
     *            Where to start the slide, vertically.
     * @param glyph
     *            A
     * @param newX
     *            Where to end the slide, horizontally.
     * @param newY
     *            Where to end the slide, vertically.
     * @param duration
     *            The animation's duration.
     * @param postRunnable a Runnable to execute after the slide completes; may be null to do nothing.
     */
    public void slide(int x, int y, TextCellFactory.Glyph glyph, final int newX,
                      final int newY, float duration, /* @Nullable */ Runnable postRunnable) {
        glyph.setPosition(xOffset + x * font.actualCellWidth, yOffset + (height - y) * font.actualCellHeight);
        duration = Math.max(0.015f, duration);
        animationCount++;
        final int nbActions = 2 + (postRunnable == null ? 0 : 1);
        int index = 0;
        final Action[] sequence = new Action[nbActions];
        final float nextX = xOffset + newX * font.actualCellWidth;
        final float nextY = yOffset + (height - newY) * font.actualCellHeight;
        sequence[index++] = Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration);
        if(postRunnable != null)
        {
            sequence[index++] = Actions.run(postRunnable);
        }
		/* Do this one last, so that hasActiveAnimations() returns true during 'postRunnables' */
        sequence[index] = Actions.delay(duration, Actions.run(new Runnable() {
            @Override
            public void run() {
                SparseLayers.this.animationCount--;
            }
        }));

        glyph.addAction(Actions.sequence(sequence));
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
        TextCellFactory.Glyph[] items = glyphs.begin();
        for (int i = 0, n = glyphs.size; i < n; i++) {
            TextCellFactory.Glyph glyph = items[i];
            if(glyph == null || backgrounds[Math.round((glyph.getX() - xOffset) / font.actualCellWidth)]
            [Math.round((glyph.getY() - yOffset)  / -font.actualCellHeight + height)] == 0f)
                continue;
            glyph.act(Gdx.graphics.getDeltaTime());
            glyph.draw(batch, 1f);
        }
        glyphs.end();
    }
}
