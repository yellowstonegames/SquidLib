package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.SnapshotArray;
import squidpony.ArrayTools;
import squidpony.IColorCenter;
import squidpony.panel.IColoredString;
import squidpony.panel.ISquidPanel;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.StatefulRNG;

/**
 * Created by Tommy Ettinger on 7/28/2017.
 */
public class SparseLayers extends Actor implements IPackedColorPanel {
    public final int gridWidth, gridHeight;
    public float[][] backgrounds;
    public Color defaultForeground = SColor.WHITE,
            defaultBackground = SColor.BLACK;
    public float defaultPackedForeground = SColor.WHITE.toFloatBits(),
            defaultPackedBackground = SColor.BLACK.toFloatBits();
    public Array<SparseTextMap> layers;
    protected IntIntMap mapping;
    public TextCellFactory font;
    protected int animationCount = 0;
    private SnapshotArray<TextCellFactory.Glyph> glyphs;
    public IColorCenter<Color> scc;


    public SparseLayers(int gridWidth, int gridHeight)
    {
        this(gridWidth, gridHeight, 10, 16, DefaultResources.getStretchableFont());
    }

    public SparseLayers(int gridWidth, int gridHeight, float cellWidth, float cellHeight)
    {
        this(gridWidth, gridHeight, cellWidth, cellHeight, DefaultResources.getStretchableFont());
    }

    public SparseLayers(int gridWidth, int gridHeight, float cellWidth, float cellHeight, TextCellFactory font) {
        this(gridWidth, gridHeight, cellWidth, cellHeight, font, 0f, 0f);
    }

    public SparseLayers(int gridWidth, int gridHeight, float cellWidth, float cellHeight, TextCellFactory font, float xOffset, float yOffset) {
        this.gridWidth = MathUtils.clamp(gridWidth, 1, 65535);
        this.gridHeight = MathUtils.clamp(gridHeight, 1, 65535);
        backgrounds = ArrayTools.fill(0f, this.gridWidth, this.gridHeight);
        layers = new Array<>(true, 4, SparseTextMap.class);
        this.font = font.width(cellWidth).height(cellHeight).initBySize();
        layers.add(new SparseTextMap(gridWidth * gridHeight >> 2));
        mapping = new IntIntMap(4);
        mapping.put(0, 0);
        glyphs = new SnapshotArray<>(true, 16, TextCellFactory.Glyph.class);
        scc = DefaultResources.getSCC();
        setBounds(xOffset, yOffset,
                this.font.actualCellWidth * this.gridWidth, this.font.actualCellHeight * this.gridHeight);
    }

    /**
     * Gets the number of layers currently used in this SparseLayers; if a layer is put into that is equal to or greater
     * than this layer count, then a new layer is created to hold the latest placement. It is recommended that you
     * assign to the layer equal to the result of this method if you want to add a layer. You can add to layers that are
     * higher than this result, and can technically do so out of order, but this just gets confusing because their
     * ordering won't be related to the numbers of the layers.
     * @return the current number of layers in this SparseLayers
     */
    public int getLayerCount()
    {
        return layers.size;
    }

    /**
     * Puts the character {@code c} at {@code (x, y)} with the default foreground. Does not change the background.
     *
     * @param x the x position to place the char at
     * @param y the y position to place the char at
     * @param c the char to place, using the default foreground color
     */
    @Override
    public void put(int x, int y, char c) {
        put(x, y, c, defaultPackedForeground, 0f);
    }

    /**
     * Puts the given string horizontally with the first character at the given
     * offset. Uses the given color for the text and does not change the background
     * color at all.
     * <p>
     * Does not word wrap. Characters that are not renderable (due to being at
     * negative offsets or offsets greater than the grid size) will not be shown
     * but will not cause any malfunctions.
     *
     * @param xOffset    the x coordinate of the first character
     * @param yOffset    the y coordinate of the first character
     * @param string     the characters to be displayed
     * @param foreground the color to use for the text
     */
    @Override
    public void put(int xOffset, int yOffset, String string, Color foreground) {
        put(xOffset, yOffset, string, foreground, null);
    }

    /**
     * Puts the given string horizontally with the first character at the given
     * offset, using the colors that {@code cs} provides. Does not change the
     * background colors. Does filter the colors from cs if the color center this
     * uses is set up to filter colors.
     * <p>
     * Does not word wrap. Characters that are not renderable (due to being at
     * negative offsets or offsets greater than the grid size) will not be shown
     * but will not cause any malfunctions.
     *
     * @param xOffset the x coordinate of the first character
     * @param yOffset the y coordinate of the first character
     * @param cs an {@link IColoredString} with potentially multiple colors
     */
    @Override
    public void put(int xOffset, int yOffset, IColoredString<? extends Color> cs) {
        int x = xOffset;
        for (IColoredString.Bucket<? extends Color> fragment : cs) {
            final String s = fragment.getText();
            final Color color = fragment.getColor();
            put(x, yOffset, s, color == null ? getDefaultForegroundColor() : scc.filter(color), null);
            x += s.length();
        }
    }

    /**
     * Puts the character {@code c} at {@code (x, y)} with some {@code color}.
     * Does not change the background colors.
     *
     * @param x the x position to place the char at
     * @param y the y position to place the char at
     * @param c the char to place
     * @param color the color to use for c; if null or fully transparent, nothing will change in the foreground
     */
    @Override
    public void put(int x, int y, char c, Color color) {
        put(x, y, c, color, null);
    }

    /**
     * Places the given char 2D array, if-non-null, in the default foreground color starting at x=0, y=0, while also
     * setting the background colors to match the given Color 2D array. If the colors argument is null, does nothing. If
     * the chars argument is null, only affects the background colors.
     * @param chars Can be {@code null}, indicating that only colors must be put.
     * @param colors the background colors for the given chars
     */
    @Override
    public void put(char[][] chars, Color[][] colors) {
        if(colors == null)
            return;
        if(chars == null)
        {
            for (int i = 0; i < colors.length; i++) {
                if(colors[i] == null)
                    continue;
                for (int j = 0; j < colors.length; j++) {
                    backgrounds[i][j] = (colors[i][j] == null) ? 0f : scc.filter(colors[i][j]).toFloatBits();
                }
            }
        }
        else
        {
            for (int i = 0; i < chars.length && i < colors.length; i++) {
                if(colors[i] == null || chars[i] == null)
                    continue;
                for (int j = 0; j < chars[i].length && j < colors[i].length; j++) {
                    if(colors[i][j] == null)
                        put(i, j, chars[i][j], defaultPackedForeground, 0f);
                    else
                        put(i, j, chars[i][j], defaultPackedForeground, scc.filter(colors[i][j]).toFloatBits());
                }
            }
        }
    }

    /**
     * @return The number of cells that this panel spans, horizontally.
     */
    @Override
    public int gridWidth() {
        return gridWidth;
    }

    /**
     * @return The number of cells that this panel spans, vertically.
     */
    @Override
    public int gridHeight() {
        return gridHeight;
    }

    /**
     * @return The width of a cell, in number of pixels.
     */
    @Override
    public int cellWidth() {
        return Math.round(font.actualCellWidth);
    }

    /**
     * @return The height of a cell, in number of pixels.
     */
    @Override
    public int cellHeight() {
        return Math.round(font.actualCellHeight);
    }

    /**
     * Sets the default foreground color.
     * Unlike most ISquidPanel implementations, color can be null, which will
     * usually not be rendered unless a different color is specified.
     * @param color a libGDX Color object or an extension of Color, such as SColor
     */
    @Override
    public void setDefaultForeground(Color color) {
        defaultForeground = color;
    }

    /**
     * @return The default foreground color (if none was set with
     * {@link #setDefaultForeground(Object)}), or the last color set
     * with {@link #setDefaultForeground(Object)}. Unlike most
     * ISquidPanel implementations, this can be null, which will
     * usually not be rendered unless a different color is specified.
     */
    @Override
    public Color getDefaultForegroundColor() {
        return defaultForeground;
    }

    /**
     * Method to change the backing {@link IColorCenter}.
     * Note that the IColorCenter is not used to filter floats that encode colors, so passing the result of
     * {@link Color#toFloatBits()} can be used to bypass the filtering if you want a color to be used exactly.
     * @param icc an IColorCenter that can cache and possibly filter {@link Color} objects
     * @return {@code this} for chaining
     */
    @Override
    public ISquidPanel<Color> setColorCenter(IColorCenter<Color> icc) {
        scc = icc;
        return this;
    }

    /**
     * Gets the IColorCenter this can use to filter Color objects; this is usually a {@link SquidColorCenter}.
     * Note that the IColorCenter is not used to filter floats that encode colors, so passing the result of
     * {@link Color#toFloatBits()} can be used to bypass the filtering if you want a color to be used exactly.
     * @return the IColorCenter this uses
     */
    public IColorCenter<Color> getColorCenter() {
        return scc;
    }

    /**
     * @return The panel doing the real job, i.e. an instance of
     * {@code SquidPanel}. The type of colors is unspecified, as some
     * clients have forwarding instances of this class that hides that
     * the type of color of the backer differs from the type of color in
     * {@code this}.
     * <p>
     * This implementation returns {@code this}.
     */
    @Override
    public ISquidPanel<?> getBacker() {
        return this;
    }

    /**
     * Puts the char c at the position x,y with the given foreground and background colors. If foreground is null or is
     * fully transparent (all channels 0), then this does not change the foreground contents.
     * If background is null or is fully transparent, this does not change the background. Uses the color center to
     * potentially filter the given colors; this can be changed with {@link #setColorCenter(IColorCenter)}.
     * @param x the x position to place the char at
     * @param y the y position to place the char at
     * @param c the char to place
     * @param foreground the color to use for c; if null or fully transparent, nothing will change in the foreground
     * @param background the color to use for the cell; if null or fully transparent, nothing will change in the background
     */
    public void put(int x, int y, char c, Color foreground, Color background)
    {
        put(x, y, c, foreground == null ? 0f : scc.filter(foreground).toFloatBits(),
                background == null ? 0f : scc.filter(background).toFloatBits());
    }
    /**
     * Puts the char c at the position x,y with the given foreground and background colors as encoded floats, such as
     * those produced by {@link Color#toFloatBits()}. If foreground is 0f, then this does not
     * change the foreground contents. If background is 0f, this does not change the background. Does not filter the
     * given colors.
     * @param x the x position to place the char at
     * @param y the y position to place the char at
     * @param c the char to place
     * @param foreground the color to use for c; if 0f, nothing will change in the foreground
     * @param background the color to use for the cell; if null or fully transparent, nothing will change in the background
     */
    public void put(int x, int y, char c, float foreground, float background)
    {
        if(x < 0 || x >= gridWidth || y < 0 || y >= gridHeight)
            return;
        if(background != 0f)
            backgrounds[x][y] = background;
        if(foreground != 0f)
            layers.items[0].place(x, y, c, foreground);
    }


    /**
     * Puts the char c at the position x,y with the given foreground color as an encoded float (the kind produced by
     * {@link Color#toFloatBits()}). If foreground is 0f, then this does nothing. Does not filter the given color.
     * @param x the x position to place the char at
     * @param y the y position to place the char at
     * @param c the char to place
     * @param foreground the color to use for c; if 0f, this call does nothing
     */
    public void put(int x, int y, char c, float foreground)
    {
        if(x < 0 || x >= gridWidth || y < 0 || y >= gridHeight || foreground == 0f)
            return;
        layers.items[0].place(x, y, c, foreground);
    }
    /**
     * Puts the char c at the position x,y in the requested layer with the given foreground and background colors. If
     * foreground is null or is fully transparent (all channels 0), then this does not change
     * the foreground contents. If background is null or is fully transparent, this does not change the background. Uses
     * the color center to potentially filter the given colors; this can be changed with
     * {@link #setColorCenter(IColorCenter)}. The layer can be greater than the number of layers currently present,
     * which will add a layer to be rendered over the existing layers, but the number that refers to that layer will not
     * change. It is recommended that to add a layer, you only add at the value equal to {@link #getLayerCount()}, which
     * will maintain the order and layer numbers in a sane way.
     * @param x the x position to place the char at
     * @param y the y position to place the char at
     * @param c the char to place
     * @param foreground the color to use for c; if null or fully transparent, nothing will change in the foreground
     * @param background the color to use for the cell; if null or fully transparent, nothing will change in the background
     * @param layer the layer to place the colorful char into; should usually be between 0 and {@link #getLayerCount()} inclusive
     */
    public void put(int x, int y, char c, Color foreground, Color background, int layer)
    {
        put(x, y, c, foreground == null ? 0f : scc.filter(foreground).toFloatBits(),
                background == null ? 0f : scc.filter(background).toFloatBits(), layer);

    }
    /**
     * Puts the char c at the position x,y in the requested layer with the given foreground and background colors as
     * encoded floats, such as those produced by {@link Color#toFloatBits()}. If foreground is 0f, then this does not
     * change the foreground contents. If background is 0f, this does not change the
     * background. Does not filter the given colors. The layer can be greater than the number of layers currently
     * present, which will add a layer to be rendered over the existing layers, but the number that refers to that layer
     * will not change. It is recommended that to add a layer, you only add at the value equal to
     * {@link #getLayerCount()}, which will maintain the order and layer numbers in a sane way.
     * @param x the x position to place the char at
     * @param y the y position to place the char at
     * @param c the char to place
     * @param foreground the color to use for c; if 0f, nothing will change in the foreground
     * @param background the color to use for the cell; if null or fully transparent, nothing will change in the background
     * @param layer the layer to place the colorful char into; should usually be between 0 and {@link #getLayerCount()} inclusive
     */
    public void put(int x, int y, char c, float foreground, float background, int layer)
    {
        if(x < 0 || x >= gridWidth || y < 0 || y >= gridHeight || layer < 0)
            return;
        if(background != 0f)
            backgrounds[x][y] = background;
        layer = mapping.get(layer, layer);
        if(foreground != 0f)
        {
            if(layer >= layers.size)
            {
                mapping.put(layer, layers.size);
                SparseTextMap stm = new SparseTextMap(gridWidth * gridHeight >> 4);
                stm.place(x, y, c, foreground);
                layers.add(stm);
            }
            else
            {
                layers.get(layer).place(x, y, c, foreground);
            }
        }
    }
    /**
     * Puts text at the position x,y with the given foreground and background colors. If foreground is null or is
     * fully transparent (all channels 0), then this does not change the foreground contents.
     * If background is null or is fully transparent, this does not change the background. Uses the color center to
     * potentially filter the given colors; this can be changed with {@link #setColorCenter(IColorCenter)}.
     * @param x the x position to place the String at
     * @param y the y position to place the String at
     * @param text the String to place
     * @param foreground the color to use for text; if null or fully transparent, nothing will change in the foreground
     * @param background the color to use for the cells; if null or fully transparent, nothing will change in the background
     */
    public void put(int x, int y, String text, Color foreground, Color background) {
        put(x, y, text, foreground == null ? 0f : scc.filter(foreground).toFloatBits(),
                background == null ? 0f : scc.filter(background).toFloatBits());
    }
    /**
     * Puts text at the position x,y with the given foreground and background colors as encoded floats, such as
     * those produced by {@link Color#toFloatBits()}. If foreground is 0f, then this does not change the foreground
     * contents. If background is 0f, this does not change the background. Does not filter the given colors.
     * @param x the x position to place the String at
     * @param y the y position to place the String at
     * @param text the String to place
     * @param foreground the color to use for text; if 0f, nothing will change in the foreground
     * @param background the color to use for the cells; if null or fully transparent, nothing will change in the background
     */
    public void put(int x, int y, String text, float foreground, float background)
    {
        int len = Math.min(text.length(), gridWidth - x);
        for (int i = 0; i < len; i++) {
            put(x + i, y, text.charAt(i), foreground, background);
        }
    }
    /**
     * Puts text at the position x,y in the requested layer with the given foreground and background colors. If
     * foreground is null or is fully transparent (all channels 0), then this does not change the foreground contents.
     * If background is null or is fully transparent, this does not change the background. Uses the color center to
     * potentially filter the given colors; this can be changed with {@link #setColorCenter(IColorCenter)}. The layer
     * can be greater than the number of layers currently present, which will add a layer to be rendered over the
     * existing layers, but the number that refers to that layer will not change. It is recommended that to add a layer,
     * you only add at the value equal to {@link #getLayerCount()}, which will maintain the order and layer numbers in a
     * sane way.
     * @param x the x position to place the String at
     * @param y the y position to place the String at
     * @param text the String to place
     * @param foreground the color to use for text; if null or fully transparent, nothing will change in the foreground
     * @param background the color to use for the cells; if null or fully transparent, nothing will change in the background
     * @param layer the layer to place the colorful char into; should usually be between 0 and {@link #getLayerCount()} inclusive
     */
    public void put(int x, int y, String text, Color foreground, Color background, int layer) {
        put(x, y, text, foreground == null ? 0f : scc.filter(foreground).toFloatBits(),
                background == null ? 0f : scc.filter(background).toFloatBits(), layer);
    }
    /**
     * Puts text at the position x,y in the requested layer with the given foreground and background colors as encoded
     * floats, such as those produced by {@link Color#toFloatBits()}. If foreground is 0f, then this does not change the
     * foreground contents. If background is 0f, this does not change the background. Does not filter the given colors.
     * The layer can be greater than the number of layers currently present, which will add a layer to be rendered over
     * the existing layers, but the number that refers to that layer will not change. It is recommended that to add a
     * layer, you only add at the value equal to {@link #getLayerCount()}, which will maintain the order and layer
     * numbers in a sane way.
     * @param x the x position to place the String at
     * @param y the y position to place the String at
     * @param text the String to place
     * @param foreground the color to use for text; if 0f, nothing will change in the foreground
     * @param background the color to use for the cells; if null or fully transparent, nothing will change in the background
     * @param layer the layer to place the colorful char into; should usually be between 0 and {@link #getLayerCount()} inclusive
     */
    public void put(int x, int y, String text, float foreground, float background, int layer) {
        if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight || layer < 0)
            return;
        int len = Math.min(text.length(), gridWidth - x);
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
                SparseTextMap stm = new SparseTextMap(gridWidth * gridHeight >> 4);
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

    /**
     * Changes the background at position x,y to the given Color. If the color is null, then this will make the
     * background fully transparent at the specified position.
     * @param x where to change the background color, x-coordinate
     * @param y where to change the background color, y-coordinate
     * @param color the Color to change to; if null will be considered fully transparent
     */
    public void put(int x, int y, Color color)
    {
        put(x, y, color == null ? 0f : scc.filter(color).toFloatBits());
    }
    /**
     * Changes the background at position x,y to the given color as an encoded float. The color can be transparent,
     * which will show through to whatever is behind this SparseLayers, or the color the screen was last cleared with.
     * @param x where to change the background color, x-coordinate
     * @param y where to change the background color, y-coordinate
     * @param color the color, as an encoded float, to change to; may be transparent
     */
    public void put(int x, int y, float color)
    {
        if(x < 0 || x >= gridWidth || y < 0 || y >= gridHeight)
            return;
        backgrounds[x][y] = color;
    }

    /**
     * Removes the foreground chars, where present, in all layers at the given x,y position.
     * The backgrounds will be unchanged.
     * @param x the x-coordinate of the position to remove all chars from
     * @param y the y-coordinate of the position to remove all chars from
     */
    @Override
    public void clear(int x, int y)
    {
        if(x < 0 || x >= gridWidth || y < 0 || y >= gridHeight)
            return;
        backgrounds[x][y] = 0f;
        int code = SparseTextMap.encodePosition(x, y);
        for (int i = 0; i < layers.size; i++) {
            layers.get(i).remove(code);
        }
    }

    /**
     * Removes the foreground char, if present, in the given layer at the given x,y position.
     * The backgrounds and other layers will be unchanged.
     * @param x the x-coordinate of the position to remove all chars from
     * @param y the y-coordinate of the position to remove all chars from
     * @param layer the layer to remove from
     */
    public void clear(int x, int y, int layer)
    {
        layer = mapping.get(layer, -1);
        if(layer >= 0)
        {
            layers.get(layer).remove(SparseTextMap.encodePosition(x, y));
        }
    }

    /**
     * Removes all background colors, setting them to transparent, and all foreground chars in all layers.
     */
    @Override
    public void clear()
    {
        ArrayTools.fill(backgrounds, 0f);
        for (int i = 0; i < layers.size; i++) {
            layers.get(i).clear();
        }
    }

    @Override
    public boolean hasActiveAnimations()
    {
        return animationCount > 0;
    }

    /**
     * Used internally to go between grid positions and world positions.
     * @param gridX x on the grid
     * @return x in the world
     */
    public float worldX(int gridX)
    {
        return getX() + gridX * font.actualCellWidth;
    }
    /**
     * Used internally to go between grid positions and world positions.
     * @param gridY y on the grid
     * @return y in the world
     */
    public float worldY(int gridY)
    {
        return getY() + (gridHeight - gridY) * font.actualCellHeight;
    }

    /**
     * Used internally to go between world positions and grid positions.
     * @param worldX x in the world
     * @return x on the grid
     */
    public int gridX(float worldX)
    {
        return Math.round((worldX - getX()) / font.actualCellWidth);
    }

    /**
     * Used internally to go between world positions and grid positions.
     * @param worldY y in the world
     * @return y on the grid
     */
    public int gridY(float worldY)
    {
        return Math.round((getY() - worldY) / font.actualCellHeight + gridHeight);
    }


    /**
     * Produces a single char with a color, that can be positioned independently of the contents of this SparseLayers.
     * Various effects in this class take a Glyph parameter and can perform visual effects with one. This takes a char
     * to show, a color that may be filtered, and an x,y position in grid cells, and returns a Glyph that has those
     * qualities set.
     * @param shown the char to use in the Glyph
     * @param color the color to use for the Glyph, which can be filtered
     * @param x the x position, in grid cells
     * @param y the y position, in grid cells
     * @return a Glyph (an inner class of TextCellFactory) with the given qualities
     */
    public TextCellFactory.Glyph glyph(char shown, Color color, int x, int y)
    {
        return glyph(shown, color == null ? 0f : scc.filter(color).toFloatBits(), x, y);
    }

    /**
     * Produces a single char with a color, that can be positioned independently of the contents of this SparseLayers.
     * Various effects in this class take a Glyph parameter and can perform visual effects with one. This takes a char
     * to show, a color as an encoded float, and an x,y position in grid cells, and returns a Glyph that has those
     * qualities set.
     * @param shown the char to use in the Glyph
     * @param color the color to use for the Glyph as an encoded float
     * @param x the x position, in grid cells
     * @param y the y position, in grid cells
     * @return a Glyph (an inner class of TextCellFactory) with the given qualities
     */
    public TextCellFactory.Glyph glyph(char shown, float color, int x, int y)
    {
        TextCellFactory.Glyph g =
                font.glyph(shown, color,
                        worldX(x),
                        worldY(y));
        glyphs.add(g);
        return g;
    }
    /**
     * "Promotes" a colorful char in the first layer to a Glyph that can be positioned independently of the contents of
     * this SparseLayers. Various effects in this class take a Glyph parameter and can perform visual effects with one.
     * This takes only an x,y position in grid cells, removes the char at that position in the first layer from normal
     * rendering, and returns a Glyph at that same position with the same char and color, but that can be moved more.
     * @param x the x position, in grid cells
     * @param y the y position, in grid cells
     * @return a Glyph (an inner class of TextCellFactory) that took the qualities of the removed char and its color
     */
    public TextCellFactory.Glyph glyphFromGrid(int x, int y)
    {
        int code = SparseTextMap.encodePosition(x, y);
        char shown = layers.items[0].getChar(code, ' ');
        float color = layers.items[0].getFloat(code, 0f);
        layers.items[0].remove(code);
        TextCellFactory.Glyph g =
                font.glyph(shown, color, worldX(x), worldY(y));
        glyphs.add(g);
        return g;
    }
    /**
     * "Promotes" a colorful char in the given layer to a Glyph that can be positioned independently of the contents of
     * this SparseLayers. Various effects in this class take a Glyph parameter and can perform visual effects with one.
     * This takes only an x,y position in grid cells, removes the char at that position in the given layer from normal
     * rendering, and returns a Glyph at that same position with the same char and color, but that can be moved more.
     * @param x the x position, in grid cells
     * @param y the y position, in grid cells
     * @param layer the layer to take a colorful char from
     * @return a Glyph (an inner class of TextCellFactory) that took the qualities of the removed char and its color; may return null if the layer is invalid
     */
    public TextCellFactory.Glyph glyphFromGrid(int x, int y, int layer) {
        layer = mapping.get(layer, -1);
        if (layer >= 0) {
            SparseTextMap l = layers.get(layer);
            int code = SparseTextMap.encodePosition(x, y);
            char shown = l.getChar(code, ' ');
            float color = l.getFloat(code, 0f);
            l.remove(code);
            TextCellFactory.Glyph g =
                    font.glyph(shown, color, worldX(x), worldY(y));
            glyphs.add(g);
            return g;
        }
        else
        {
            return null;
        }
    }

    /**
     * Brings a Glyph back into normal rendering, removing it from the Glyphs this class knows about and filling the
     * grid's char at the Glyph's position in the first layer with the Glyph's char and color.
     * @param glyph the Glyph to remove and fit back into the grid
     */
    public void recallToGrid(TextCellFactory.Glyph glyph)
    {
        layers.items[0].place(gridX(glyph.getY()), gridY(glyph.getY()), glyph.shown, glyph.color);
        glyphs.removeValue(glyph, true);
    }

    /**
     * Brings a Glyph back into normal rendering, removing it from the Glyphs this class knows about and filling the
     * grid's char at the Glyph's position in the given layer with the Glyph's char and color.
     * @param glyph the Glyph to remove and fit back into the grid
     */
    public void recallToGrid(TextCellFactory.Glyph glyph, int layer)
    {
        layer = mapping.get(layer, 0);
        layers.get(layer).place(gridX(glyph.getY()), gridY(glyph.getY()), glyph.shown, glyph.color);
        glyphs.removeValue(glyph, true);
    }
    /**
     * Start a bumping animation in the given direction that will last duration seconds.
     * @param glyph
     *              A {@link TextCellFactory.Glyph}, probably produced by
     *              {@link #glyph(char, float, int, int)} or {@link #glyphFromGrid(int, int)}
     * @param direction the direction for the glyph to bump towards
     * @param duration a float, measured in seconds, for how long the animation should last; commonly 0.12f
     */
    public void bump(final TextCellFactory.Glyph glyph, Direction direction, float duration)
    {
        final float x = glyph.getX(),
                y = glyph.getY();
        duration = Math.max(0.015f, duration);
        animationCount++;
        glyph.addAction(Actions.sequence(
                Actions.moveToAligned(x + direction.deltaX * 0.35f * font.actualCellWidth,
                        y - direction.deltaY * 0.35f * font.actualCellHeight,
                        Align.bottomLeft, duration * 0.35F),
                Actions.moveToAligned(x, y, Align.bottomLeft, duration * 0.65F),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        --animationCount;
                    }
                }))));

    }

    /**
     * Slides {@code glyph} from {@code (xstartX,startY)} to {@code (newx, newy)}.
     * Takes a number of seconds equal to duration to complete. This also allows
     * a Runnable to be given as {@code postRunnable} to be run after the
     * slide completes, or null to not run anything after the slide.
     *
     * @param glyph
     *            A {@link TextCellFactory.Glyph}, probably produced by
     *            {@link #glyph(char, float, int, int)} or {@link #glyphFromGrid(int, int)}
     * @param startX
     *            Where to start the slide, horizontally.
     * @param startY
     *            Where to start the slide, vertically.
     * @param newX
     *            Where to end the slide, horizontally.
     * @param newY
     *            Where to end the slide, vertically.
     * @param duration
     *            The animation's duration.
     * @param postRunnable a Runnable to execute after the slide completes; may be null to do nothing.
     */
    public void slide(TextCellFactory.Glyph glyph, final int startX, final int startY, final int newX,
                      final int newY, float duration, /* @Nullable */ Runnable postRunnable) {
        glyph.setPosition(worldX(startX), worldY(startY));
        duration = Math.max(0.015f, duration);
        animationCount++;
        final int nbActions = 2 + (postRunnable == null ? 0 : 1);
        int index = 0;
        final Action[] sequence = new Action[nbActions];
        final float nextX = worldX(newX);
        final float nextY = worldY(newY);
        sequence[index++] = Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration);
        if(postRunnable != null)
        {
            sequence[index++] = Actions.run(postRunnable);
        }
		/* Do this one last, so that hasActiveAnimations() returns true during 'postRunnables' */
        sequence[index] = Actions.delay(duration, Actions.run(new Runnable() {
            @Override
            public void run() {
                --animationCount;
            }
        }));

        glyph.addAction(Actions.sequence(sequence));
    }

    /**
     * Starts an wiggling animation for the object at the given location for the given duration in seconds.
     *
     * @param glyph
     *            A {@link TextCellFactory.Glyph}, probably produced by
     *            {@link #glyph(char, float, int, int)} or {@link #glyphFromGrid(int, int)}
     * @param duration in seconds, as a float
     */
    public void wiggle(final TextCellFactory.Glyph glyph, float duration) {

        final float x = glyph.getX(), y = glyph.getY(),
                cellWidth = font.actualCellWidth, cellHeight = font.actualCellHeight;
        duration = Math.max(0.015f, duration);
        animationCount++;
        final StatefulRNG gRandom = DefaultResources.getGuiRandom();
        glyph.addAction(Actions.sequence(
                Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(x, y, Align.bottomLeft, duration * 0.2F),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        --animationCount;
                    }
                }))));
    }
    /**
     * Tints the background at position x,y so it becomes the given encodedColor, waiting for {@code delay} (in seconds)
     * before performing it, then after the tint is complete it returns the cell to its original color, taking duration
     * seconds. Additionally, enqueue {@code postRunnable} for running after the created action ends.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the tint this applies, then apply the tint when you call act(), then quickly overwrite
     * the tint in the next frame. That visually appears as nothing happening other than a delay.
     * @param delay how long to wait in seconds before starting the effect
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
     * @param encodedColor what to transition the cell's color towards, and then transition back from, as a packed float
     * @param duration how long the total "round-trip" transition should take in seconds
     * @param postRunnable a Runnable to execute after the tint completes; may be null to do nothing.
     */
    public void tint(float delay, final int x, final int y, final float encodedColor, float duration, Runnable postRunnable) {
        if(x < 0 || x >= gridWidth || y < 0 || y >= gridHeight)
            return;
        duration = Math.max(0.015f, duration);
        animationCount++;
        final float ac = backgrounds[x][y];
        final int nbActions = 3 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        final Action[] sequence = new Action[nbActions];
        int index = 0;
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        sequence[index++] = new TemporalAction(duration * 0.3f) {
            @Override
            protected void update(float percent) {
                backgrounds[x][y] = SColor.lerpFloatColors(ac, encodedColor, percent);
            }
        };
        sequence[index++] = new TemporalAction(duration * 0.7f) {
            @Override
            protected void update(float percent) {
                backgrounds[x][y] = SColor.lerpFloatColors(encodedColor, ac, percent);
            }
        };
        if(postRunnable != null)
        {
            sequence[index++] = Actions.run(postRunnable);
        }
		/* Do this one last, so that hasActiveAnimations() returns true during 'postRunnables' */
        sequence[index] = Actions.delay(duration, Actions.run(new Runnable() {
            @Override
            public void run() {
                --animationCount;
            }
        }));

        addAction(Actions.sequence(sequence));
    }

    /**
     * Create a new Glyph at (startX, startY) using the char shown with the given startColor, and immediately starts
     * changing color to endColor, changing position so it ends on the cell (endX, endY), taking duration seconds to
     * complete before removing the Glyph.
     * <br>
     * Unlike {@link SquidPanel#summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)},
     * this does not rotate the Glyph it produces.
     * @param startX the starting x position in cells
     * @param startY the starting y position in cells
     * @param endX the ending x position in cells
     * @param endY the ending y position in cells
     * @param shown the char to show (the same char throughout the effect)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param duration the duration in seconds for the effect
     */
    public void summon(int startX, int startY, int endX, int endY, char shown,
                       final float startColor, final float endColor, float duration)
    {
        duration = Math.max(0.015f, duration);
        animationCount++;
        final TextCellFactory.Glyph glyph = glyph(shown, startColor, startX, startY);
        glyph.addAction(Actions.sequence(
                Actions.parallel(
                        new TemporalAction(duration) {
                            @Override
                            protected void update(float percent) {
                                glyph.color = SColor.lerpFloatColors(startColor, endColor, percent * 0.95f);
                            }
                        },
                        Actions.moveTo(worldX(endX), worldY(endY), duration)),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        --animationCount;
                        glyphs.removeValue(glyph, true);
                    }
                })));
    }

    /**
     * Create a new Glyph at (startX, startY) in world coordinates (often pixels on the screen) using the char shown
     * with the given startColor, and immediately starts changing color to endColor, changing position so it ends at the
     * world coordinates (endX, endY), taking duration seconds to complete before removing the Glyph.
     * <br>
     * Unlike {@link SquidPanel#summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)},
     * this does not rotate the Glyph it produces.
     * @param startX the starting x position in world coordinates
     * @param startY the starting y position in world coordinates
     * @param endX the ending x position in world coordinates
     * @param endY the ending y position in world coordinates
     * @param shown the char to show (the same char throughout the effect)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param duration the duration in seconds for the effect
     */
    public void summon(float startX, float startY, float endX, float endY, char shown,
                       final float startColor, final float endColor, float duration)
    {
        duration = Math.max(0.015f, duration);
        animationCount++;
        final TextCellFactory.Glyph glyph = glyph(shown, startColor, gridX(startX), gridY(startY));
        glyph.addAction(Actions.sequence(
                Actions.parallel(
                        new TemporalAction(duration) {
                            @Override
                            protected void update(float percent) {
                                glyph.color = SColor.lerpFloatColors(startColor, endColor, percent * 0.9f);
                            }
                        },
                        Actions.moveTo(endX, endY, duration)),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        --animationCount;
                        glyphs.removeValue(glyph, true);
                    }
                })));
    }

    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(int, int, int, int, char, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Glyphs that change color and position.
     * The distance is how many cells away to move the created Actors away from (x,y). The measurement determines
     * whether this produces Glyphs in 4 (cardinal) directions for DIAMOND or OCTAHEDRON, or 8 (cardinal and diagonal)
     * directions for any other enum value for Radius; CIRCLE and SPHERE will position the 8 glyphs in a circle, while
     * SQUARE and CUBE will position their 8 glyphs in a square.
     * <br>
     * Unlike {@link SquidPanel#burst(float, int, int, int, boolean, char, Color, Color, boolean, float, float)}, this
     * does not rotate the individual Glyphs it produces.
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param distance how far away, in cells, to move each actor from the center (Chebyshev distance, forming a square)
     * @param measurement a Radius enum that determines if 4 (DIAMOND, OCTAHEDRON) or 8 (anything else) Glyphs are
     *                    created, and the shape they will take
     * @param shown the char to use for Glyphs; should definitely be visible
     * @param startColor the encoded color to start the effect with
     * @param endColor the encoded color to end the effect on
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, int distance, Radius measurement, char shown,
                      float startColor, float endColor, float duration)
    {
        Direction d;
        switch (measurement)
        {
            case SQUARE:
            case CUBE:
                for (int i = 0; i < 8; i++) {
                    d = Direction.CLOCKWISE[i];
                    summon(x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                            shown, startColor, endColor, duration);
                }
                break;
            case CIRCLE:
            case SPHERE:
                float xf = worldX(x), yf = worldY(y);
                for (int i = 0; i < 4; i++) {
                    d = Direction.DIAGONALS[i];
                    summon(xf, yf, xf - d.deltaX * font.actualCellWidth * distance * 0.7071067811865475f, // the constant is 1.0 / Math.sqrt(2.0)
                            yf + d.deltaY * font.actualCellHeight * distance * 0.7071067811865475f,
                            shown, startColor, endColor, duration);
                }
                // break intentionally absent
            default:
                for (int i = 0; i < 4; i++) {
                    d = Direction.CARDINALS_CLOCKWISE[i];
                    summon(x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                            shown, startColor, endColor, duration);
                }
                break;
        }
    }
    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(int, int, int, int, char, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Glyphs that change color and position.
     * The distance is how many cells away to move the created Actors away from (x,y). The measurement determines
     * whether this produces Glyphs in 4 (cardinal) directions for DIAMOND or OCTAHEDRON, or 8 (cardinal and diagonal)
     * directions for any other enum value for Radius; CIRCLE and SPHERE will position the 8 glyphs in a circle, while
     * SQUARE and CUBE will position their 8 glyphs in a square.
     * <br>
     * Unlike {@link SquidPanel#burst(float, int, int, int, boolean, char, Color, Color, boolean, float, float)}, this
     * does not rotate the individual Glyphs it produces.
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param distance how far away, in cells, to move each actor from the center (Chebyshev distance, forming a square)
     * @param measurement a Radius enum that determines if 4 (DIAMOND, OCTAHEDRON) or 8 (anything else) Glyphs are
     *                    created, and the shape they will take
     * @param choices a String or other CharSequence containing the chars this can randomly choose
     * @param startColor the encoded color to start the effect with
     * @param endColor the encoded color to end the effect on
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, int distance, Radius measurement, CharSequence choices,
                      float startColor, float endColor, float duration)
    {
        Direction d;
        final int len = choices.length();
        final StatefulRNG rng = DefaultResources.getGuiRandom();
        switch (measurement)
        {
            case SQUARE:
            case CUBE:
                for (int i = 0; i < 8; i++) {
                    d = Direction.CLOCKWISE[i];
                    summon(x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                            choices.charAt(rng.nextIntHasty(len)), startColor, endColor, duration);
                }
                break;
            case CIRCLE:
            case SPHERE:
                float xf = worldX(x), yf = worldY(y);
                for (int i = 0; i < 4; i++) {
                    d = Direction.DIAGONALS[i];
                    summon(xf, yf, xf - d.deltaX * font.actualCellWidth * distance * 0.7071067811865475f,
                            yf + d.deltaY * font.actualCellHeight * distance * 0.7071067811865475f,
                            choices.charAt(rng.nextIntHasty(len)), startColor, endColor, duration);
                }
                // break intentionally absent
            default:
                for (int i = 0; i < 4; i++) {
                    d = Direction.CARDINALS_CLOCKWISE[i];
                    summon(x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                            choices.charAt(rng.nextIntHasty(len)), startColor, endColor, duration);
                }
                break;
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
        float xo = getX(), yo = getY(), yOff = yo + 1f + gridHeight * font.actualCellHeight;
        font.draw(batch, backgrounds, xo, yo);
        int len = layers.size;
        for (int i = 0; i < len; i++) {
            layers.get(i).draw(batch, font, xo, yOff);
        }
        TextCellFactory.Glyph[] items = glyphs.begin();
        int x, y;
        for (int i = 0, n = glyphs.size; i < n; i++) {
            TextCellFactory.Glyph glyph = items[i];
            if(glyph == null)
                continue;
            glyph.act(Gdx.graphics.getDeltaTime());
            if(
                    (x = Math.round((glyph.getX() - xo) / font.actualCellWidth)) < 0 || x >= gridWidth ||
                    (y = Math.round((glyph.getY() - yo)  / -font.actualCellHeight + gridHeight)) < 0 || y >= gridHeight ||
                    backgrounds[x][y] == 0f)
                continue;
            glyph.draw(batch, 1f);
        }
        glyphs.end();
    }
}
