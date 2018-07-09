package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.ArrayTools;
import squidpony.IColorCenter;
import squidpony.panel.IColoredString;
import squidpony.panel.ISquidPanel;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.Noise;
import squidpony.squidmath.NumberTools;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.WhirlingNoise;

import java.util.ArrayList;

/**
 * Created by Tommy Ettinger on 7/28/2017.
 */
public class SparseLayers extends Actor implements IPackedColorPanel {
    public final int gridWidth, gridHeight;
    /**
     * A 2D float array of background colors as packed floats. Must have dimensions matching {@link #gridWidth} and
     * {@link #gridHeight}, and must be non-null with non-null interior arrays, but can otherwise be assigned 2D float
     * arrays from other sources (that use floats to represent colors, not just any number).
     */
    public float[][] backgrounds;
    /**
     * The default foreground color when none is specified, as a Color object. Defaults to white.
     */
    public Color defaultForeground = SColor.WHITE,
    /**
     * Currently unused internally, but public so if some background color needs to be stored with this SparseLayers,
     * then there will be a logical place for it. Defaults to black.
     */
            defaultBackground = SColor.BLACK;
    /**
     * The value of {@link #defaultForeground} as a float, for easier usage with the methods that use floats for colors.
     * Defaults to white.
     */
    public float defaultPackedForeground = SColor.FLOAT_WHITE,
    /**
     * The value of {@link #defaultBackground} as a float, for easier usage with the methods that use floats for colors.
     * Currently unused internally, but public so if some background color needs to be stored with this SparseLayers,
     * then there will be a logical place for it. Defaults to black.
     */
            defaultPackedBackground = SColor.FLOAT_BLACK;
    /**
     * A list of SparseTextMap objects, with each representing a foreground layer.
     */
    public ArrayList<SparseTextMap> layers;
    protected IntIntMap mapping;
    /**
     * The TextCellFactory that is used to determine font size as well as cell size; must be initialized, usually using
     * {@link TextCellFactory#initBySize()}, if this is changed after construction.
     */
    public TextCellFactory font;
    /**
     * Will always be 0 unless user code affects it; use {@link #hasActiveAnimations()} to track animations instead.
     * The approach of tracking animations via a counter was prone to error when multiple effects might remove a Glyph
     * or adjust the animationCount in one direction but not the other. This could result in never-ending phases of a
     * game where input wasn't handled because animationCount was greater than 0, but whatever effect was supposed to
     * reduce animationCount would never happen.
     * @deprecated Use {@link #hasActiveAnimations()} instead of adjusting this manually
     */
    public int animationCount = 0;
    /**
     * A list of individually-movable Glyph objects. This field is public, and though it shouldn't be assigned null (you
     * don't really need to be told that), there may be cases where you may need manual control over what Glyph objects
     * should be added or removed from this SparseLayers.
     */
    public ArrayList<TextCellFactory.Glyph> glyphs;
    /**
     * An IColorCenter to affect color caching and filtering; usually a SquidColorCenter, which can be easily obtained
     * via {@link DefaultResources#getSCC()}.
     */
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
        layers = new ArrayList<>(4);
        if(font.initialized())
            this.font = font;
        else
            this.font = font.width(cellWidth).height(cellHeight).initBySize();
        layers.add(new SparseTextMap(gridWidth * gridHeight >> 2));
        mapping = new IntIntMap(4);
        mapping.put(0, 0);
        glyphs = new ArrayList<>(16);
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
        return layers.size();
    }

    /**
     * Gets the layer, as a SparseTextMap, that is associated with the given int. If none is associated, this returns
     * null. The associating number does not necessarily have to be less than the number of layers in this SparseLayers.
     * Returns a direct reference to the SparseTextMap layer.
     * @param layer the int that is associated with a layer, usually the int used to add to that layer.
     * @return the SparseTextMap layer associated with the given int
     */
    public SparseTextMap getLayer(int layer)
    {
        layer = mapping.get(layer, -1);
        if(layer < 0)
            return null;
        else
            return layers.get(layer);
    }

    /**
     * Sets the SparseTextMap associated with the given layerNumber to the given contents. If layerNumber is too high
     * to set an existing layer, this will add contents as a new layer on top of the others.
     * @param layerNumber must be 0 or greater
     * @param contents a SparseTextMap, possibly obtained with {@link #getLayer(int)}
     */
    public void setLayer(int layerNumber, SparseTextMap contents)
    {
        if(layerNumber < 0)
            return;
        layerNumber = mapping.get(layerNumber, layerNumber);
        if(layerNumber >= layers.size())
        {
            mapping.put(layerNumber, layers.size());
            layers.add(contents);
        }
        else
        {
            layers.set(layerNumber, contents);
        }

    }

    /**
     * Finds the layer number associated with layerMap, or -1 if the given SparseTextMap is not in this SparseLayers.
     * @param layerMap a SparseTextMap that was likely returned by {@link #addLayer()}
     * @return the int the layer is associated with, or -1 if it is not in this SparseLayers.
     */
    public int findLayer(SparseTextMap layerMap)
    {
        int a = layers.indexOf(layerMap);
        if(a < 0)
            return -1;
        return mapping.findKey(a, -1);
    }

    /**
     * Adds a layer as a SparseTextMap to this SparseLayers and returns the one just added. The layer will generally be
     * associated with a layer number equal to the number of layers currently present, and will be rendered over the
     * existing layers. It might not be associated with the same number if layers were added out-of-order or with skips
     * between associations. If you want to add a layer at some alternate layer number (which can be confusing but may
     * be needed for some reason), there is another overload that allows you to specify the association number.
     * If you may have added some layers out of order or skipped some numbers, you can use
     * {@link #findLayer(SparseTextMap)} on the returned SparseTextMap to get its association number.
     * @return a SparseTextMap that was just added; null if something went wrong
     */
    public SparseTextMap addLayer()
    {
        int association = layers.size();
        while (mapping.containsKey(association)) {
            ++association;
        }
        return addLayer(association);
    }
    /**
     * Adds a layer as a SparseTextMap to this SparseLayers and returns the one just added, or returns an existing layer
     * if one is already associated with the given number. The layer can be greater than the number of layers currently
     * present, which will add a layer to be rendered over the existing layers, but the number that refers to that layer
     * will not change. It is recommended that to add a layer, you only add at the value equal to
     * {@link #getLayerCount()}, which will maintain the order and layer numbers in a sane way, and this is the behavior
     * of the addLayer overload that does not take a parameter.
     * @param association the number to associate the layer with; should usually be between 0 and {@link #getLayerCount()} inclusive
     * @return a SparseTextMap that either was just added or was already associated with the given layer number; null if something went wrong
     */
    public SparseTextMap addLayer(int association)
    {
        if(association < 0)
            return null;
        association = mapping.get(association, association);
        if(association >= layers.size())
        {
            mapping.put(association, layers.size());
            SparseTextMap stm = new SparseTextMap(gridWidth * gridHeight >> 4);
            layers.add(stm);
            return stm;
        }
        else
        {
            return layers.get(association);
        }
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
     * Sets the background colors to match the given Color 2D array. If the colors argument is null, does nothing.
     * This will filter each Color in colors if the color center this uses has a filter.
     * @param colors the background colors for the given chars
     */
    public void put(Color[][] colors)
    {
        put(null, colors);
    }

    /**
     * Sets the background colors to match the given 2D array of colors as packed floats. If the colors argument is
     * null, does nothing. This will not filter the passed colors at all.
     * @param colors the background colors to use for this SparseLayers
     */
    public void put(float[][] colors)
    {
        put(null, colors);
    }
    /**
     * Places the given char 2D array, if-non-null, in the default foreground color starting at x=0, y=0, while also
     * setting the background colors to match the given Color 2D array. If the colors argument is null, does nothing. If
     * the chars argument is null, only affects the background colors. This will filter each Color in colors if the
     * color center this uses has a filter.
     * @param chars Can be {@code null}, indicating that only colors must be put.
     * @param colors the background colors for the given chars
     */
    @Override
    public void put(char[][] chars, Color[][] colors) {
        if(chars == null)
        {
            if(colors != null)
            {
                for (int i = 0; i < colors.length; i++) {
                    if (colors[i] == null)
                        continue;
                    for (int j = 0; j < colors[i].length; j++) {
                        backgrounds[i][j] = (colors[i][j] == null) ? 0f : scc.filter(colors[i][j]).toFloatBits();
                    }
                }
            }
        }
        else
        {
            if(colors == null)
            {
                for (int i = 0; i < chars.length; i++) {
                    if(chars[i] == null)
                        continue;
                    for (int j = 0; j < chars[i].length; j++) {
                        put(i, j, chars[i][j], defaultPackedForeground, 0f);
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
    }
    /**
     * Places the given char 2D array, if-non-null, in the default foreground color starting at x=0, y=0, while also
     * setting the background colors to match the given 2D array of colors as packed floats. If the colors argument is
     * null, does nothing. If the chars argument is null, only affects the background colors. This will not filter the
     * passed colors at all.
     * @param chars Can be {@code null}, indicating that only colors must be put.
     * @param colors the background colors for the given chars
     */
    public void put(char[][] chars, float[][] colors) {
        if(chars == null)
        {
            if(colors != null) {
                for (int i = 0; i < colors.length; i++) {
                    if (colors[i] == null)
                        continue;
                    System.arraycopy(colors[i], 0, backgrounds[i], 0, colors[i].length);
                }
            }
        }
        else
        {
            if(colors == null)
            {
                for (int i = 0; i < chars.length; i++) {
                    if(chars[i] == null)
                        continue;
                    for (int j = 0; j < chars[i].length; j++) {
                        put(i, j, chars[i][j], defaultPackedForeground, 0f);
                    }
                }
            }
            else
            {
                for (int i = 0; i < chars.length && i < colors.length; i++) {
                    if(colors[i] == null || chars[i] == null)
                        continue;
                    for (int j = 0; j < chars[i].length && j < colors[i].length; j++) {
                        put(i, j, chars[i][j], defaultPackedForeground, colors[i][j]);
                    }
                }
            }
        }
    }
    /**
     * Places the given char 2D array, if-non-null, with the given foreground colors in the first Color 2D array,
     * starting at x=0, y=0, while also setting the background colors to match the second Color 2D array. If the
     * bgColors argument is null, only affects foreground chars and colors. If the chars argument or the fgColors
     * argument is null, only affects the background colors. Any positions where a Color in fgColors is null will not
     * have a char placed (this can be used to restrict what is placed). This will filter each Color in the background
     * and foreground if the color center this uses has a filter.
     * @param chars Can be {@code null}, indicating that only colors must be put.
     * @param fgColors the foreground Colors for the given chars
     * @param bgColors the background Colors for the given chars
     */
    public void put(char[][] chars, Color[][] fgColors, Color[][] bgColors) {
        if(chars == null || fgColors == null)
        {
            if(bgColors != null)
            {
                for (int i = 0; i < bgColors.length; i++) {
                    if (bgColors[i] == null)
                        continue;
                    for (int j = 0; j < bgColors[i].length; j++) {
                        backgrounds[i][j] = (bgColors[i][j] == null) ? 0f : scc.filter(bgColors[i][j]).toFloatBits();
                    }
                }
            }
        }
        else
        {
            if(bgColors == null)
            {
                for (int i = 0; i < chars.length && i < fgColors.length; i++) {
                    if(chars[i] == null || fgColors[i] == null)
                        continue;
                    for (int j = 0; j < chars[i].length && j < fgColors[i].length; j++) {
                        put(i, j, chars[i][j], fgColors[i][j], null);
                    }
                }
            }
            else
            {
                for (int i = 0; i < chars.length && i < bgColors.length && i < fgColors.length; i++) {
                    if(bgColors[i] == null || chars[i] == null || fgColors[i] == null)
                        continue;
                    for (int j = 0; j < chars[i].length && j < bgColors[i].length && j < fgColors[i].length; j++) {                         
                        put(i, j, chars[i][j], fgColors[i][j], bgColors[i][j]);
                    }
                }
            }
        }
    }
    /**
     * Places the given char 2D array, if-non-null, with the given foreground colors in the first float 2D array,
     * starting at x=0, y=0, while also setting the background colors to match the second float 2D array. If the
     * bgColors argument is null, only affects foreground chars and colors. If the chars argument or the fgColors
     * argument is null, only affects the background colors. Any positions where a float in fgColors is 0 will not
     * have a char placed (this can be used to restrict what is placed). This will not filter any colors.
     * @param chars Can be {@code null}, indicating that only colors must be put.
     * @param fgColors the foreground colors for the given chars, as packed floats
     * @param bgColors the background colors for the given chars, as packed floats
     */
    public void put(char[][] chars, float[][] fgColors, float[][] bgColors) {
        if(chars == null || fgColors == null)
        {
            if(bgColors != null)
            {
                for (int i = 0; i < bgColors.length; i++) {
                    if (bgColors[i] == null)
                        continue;
                    System.arraycopy(bgColors[i], 0, backgrounds[i], 0, bgColors[i].length);
                }
            }
        }
        else
        {
            if(bgColors == null)
            {
                for (int i = 0; i < chars.length && i < fgColors.length; i++) {
                    if(chars[i] == null || fgColors[i] == null)
                        continue;
                    for (int j = 0; j < chars[i].length && j < fgColors[i].length; j++) {
                        put(i, j, chars[i][j], fgColors[i][j], 0f);
                    }
                }
            }
            else
            {
                for (int i = 0; i < chars.length && i < bgColors.length && i < fgColors.length; i++) {
                    if(bgColors[i] == null || chars[i] == null || fgColors[i] == null)
                        continue;
                    for (int j = 0; j < chars[i].length && j < bgColors[i].length && j < fgColors[i].length; j++) {
                        put(i, j, chars[i][j], fgColors[i][j], bgColors[i][j]);
                    }
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
        if(color != null)
            defaultPackedForeground = color.toFloatBits();
        else
            defaultPackedForeground = 0f;
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
     * Sets the default background color.
     * Unlike most ISquidPanel implementations, color can be null, which will
     * usually not be rendered unless a different color is specified.
     * @param color a libGDX Color object or an extension of Color, such as SColor
     */
    public void setDefaultBackground(Color color) {
        defaultBackground = color;
        if(color != null)
            defaultPackedBackground = color.toFloatBits();
        else
            defaultPackedBackground = 0f;
    }
    /**
     * @return The default background color (if none was set with
     * {@link #setDefaultBackground(Color)}), or the last color set
     * with {@link #setDefaultBackground(Color)}. This can be null,
     * which will usually not be rendered unless a different color
     * is specified.
     */
    public Color getDefaultBackgroundColor() {
        return defaultBackground;
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
            layers.get(0).place(x, y, c, foreground);
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
        layers.get(0).place(x, y, c, foreground);
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
            if(layer >= layers.size())
            {
                mapping.put(layer, layers.size());
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
            if (layer >= layers.size()) {
                mapping.put(layer, layers.size());
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
     * Unlike other methods in this class, a float equal to 0f will be used instead of being used to skip over a cell,
     * and will change the background at the given position to fully transparent.
     * @param x where to change the background color, x-coordinate
     * @param y where to change the background color, y-coordinate
     * @param color the color, as an encoded float, to change to; may be transparent, and considers 0f a valid color
     */
    public void put(int x, int y, float color)
    {
        if(x < 0 || x >= gridWidth || y < 0 || y >= gridHeight)
            return;
        backgrounds[x][y] = color;
    }
    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a libGDX Color
     * @param lightColor the color to mix with the background, as a libGDX Color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithLight(int x, int y, Color background, Color lightColor, float lightAmount) {
        putWithLight(x, y, '\0', 0f, background == null ? 0f :  background.toFloatBits(),
                lightColor == null ? 0f : lightColor.toFloatBits(), lightAmount);
    }
    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a libGDX Color
     * @param lightColor the color to mix with the background, as a libGDX Color
     * @param lightAmount a double that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithLight(int x, int y, Color background, Color lightColor, double lightAmount) {
        putWithLight(x, y, '\0', 0f, background == null ? 0f :  background.toFloatBits(),
                lightColor == null ? 0f : lightColor.toFloatBits(), lightAmount);
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, while also putting a char on the screen; as a whole this affects one x,y position.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to put in the foreground
     * @param foreground the color to use for the foreground, as a libGDX Color; if null, this won't place a foreground char
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a libGDX Color
     * @param lightColor the color to mix with the background, as a libGDX Color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithLight(int x, int y, char c, Color foreground, Color background, Color lightColor, float lightAmount) {
        putWithLight(x, y, c, foreground == null ? 0f : foreground.toFloatBits(), background == null ? 0f :  background.toFloatBits(),
                lightColor == null ? 0f : lightColor.toFloatBits(), lightAmount);
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, while also putting a char on the screen; as a whole this affects one x,y position.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to put in the foreground
     * @param foreground the color to use for the foreground, as a libGDX Color; if null, this won't place a foreground char
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a libGDX Color
     * @param lightColor the color to mix with the background, as a libGDX Color
     * @param lightAmount a double that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithLight(int x, int y, char c, Color foreground, Color background, Color lightColor, double lightAmount) {
        putWithLight(x, y, c, foreground == null ? 0f : foreground.toFloatBits(), background == null ? 0f :  background.toFloatBits(),
                lightColor == null ? 0f : lightColor.toFloatBits(), lightAmount);
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position. This will use the same
     * brightness for all cells given identical lightAmount values when this is called; this differentiates it from
     * {@link #putWithLight(int, int, float, float, float, Noise.Noise3D)}, which would light "splotches" of map with
     * brighter or darker color. Instead, if lightAmount is obtained via SquidLib's {@code FOV} class, then all cells
     * at a short distance from an FOV center will be lit brightly and cells far away will flicker in and out of view.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a packed float color
     * @param lightColor the color to mix with the background, as a packed float color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithConsistentLight(int x, int y, float background, float lightColor, float lightAmount) {
        putWithConsistentLight(x, y, background, lightColor, lightAmount, 0.0015f);
    }
    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position. This will use the same
     * brightness for all cells given identical lightAmount values when this is called; this differentiates it from
     * {@link #putWithLight(int, int, float, float, float, Noise.Noise3D)}, which would light "splotches" of map with
     * brighter or darker color. Instead, if lightAmount is obtained via SquidLib's {@code FOV} class, then all cells
     * at a short distance from an FOV center will be lit brightly and cells far away will flicker in and out of view.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a packed float color
     * @param lightColor the color to mix with the background, as a packed float color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     * @param flickerSpeed a small float multiplier applied to the time in milliseconds; often between 0.0005f and 0.005f
     */
    public void putWithConsistentLight(int x, int y, float background, float lightColor, float lightAmount, float flickerSpeed) {
        final float time = (System.currentTimeMillis() & 0xffffffL) * flickerSpeed; // if you want to adjust the speed of flicker, change the multiplier
        final long time0 = Noise.longFloor(time);
        final float noise = Noise.querp(NumberTools.randomFloatCurved(time0), NumberTools.randomFloatCurved(time0 + 1L), time - time0);
        lightAmount = Math.max(lightAmount * 0.15f, Math.min(lightAmount - NumberTools.swayTight(noise * 3.141592f) * 0.15f - 0.1f + 0.25f * noise, lightAmount)); // 0.1f * noise for light theme, 0.2f * noise for dark theme
        putWithLight(x, y, '\0', 0f, background, lightColor, lightAmount);
    }
    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position. This will use the same
     * brightness for all cells given identical lightAmount values when this is called; this differentiates it from
     * {@link #putWithLight(int, int, float, float, float, Noise.Noise3D)}, which would light "splotches" of map with
     * brighter or darker color. Instead, if lightAmount is obtained via SquidLib's {@code FOV} class, then all cells
     * at a short distance from an FOV center will be lit brightly and cells far away will flicker in and out of view.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a libGDX Color
     * @param lightColor the color to mix with the background, as a libGDX Color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithConsistentLight(int x, int y, Color background, Color lightColor, float lightAmount) {
        putWithConsistentLight(x, y, background, lightColor, lightAmount, 0.0015f);
    }
    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position. This will use the same
     * brightness for all cells given identical lightAmount values when this is called; this differentiates it from
     * {@link #putWithLight(int, int, float, float, float, Noise.Noise3D)}, which would light "splotches" of map with
     * brighter or darker color. Instead, if lightAmount is obtained via SquidLib's {@code FOV} class, then all cells
     * at a short distance from an FOV center will be lit brightly and cells far away will flicker in and out of view.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a libGDX Color
     * @param lightColor the color to mix with the background, as a libGDX Color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     * @param flickerSpeed a small float multiplier applied to the time in milliseconds; often between 0.0005f and 0.005f
     */
    public void putWithConsistentLight(int x, int y, Color background, Color lightColor, float lightAmount, float flickerSpeed) {
        final float time = (System.currentTimeMillis() & 0xffffffL) * flickerSpeed; // if you want to adjust the speed of flicker, change the multiplier
        final long time0 = Noise.longFloor(time);
        final float noise = Noise.querp(NumberTools.randomFloatCurved(time0), NumberTools.randomFloatCurved(time0 + 1L), time - time0);
        lightAmount = Math.max(lightAmount * 0.15f, Math.min(lightAmount - NumberTools.swayTight(noise * 3.141592f) * 0.15f - 0.1f + 0.25f * noise, lightAmount)); // 0.1f * noise for light theme, 0.2f * noise for dark theme
        putWithLight(x, y, '\0', 0f, background == null ? 0f :  background.toFloatBits(),
                lightColor == null ? 0f : lightColor.toFloatBits(), lightAmount);
    }
    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position. This will use the same
     * brightness for all cells given identical lightAmount values when this is called; this differentiates it from
     * {@link #putWithLight(int, int, float, float, float, Noise.Noise3D)}, which would light "splotches" of map with
     * brighter or darker color. Instead, if lightAmount is obtained via SquidLib's {@code FOV} class, then all cells
     * at a short distance from an FOV center will be lit brightly and cells far away will flicker in and out of view.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to draw; may be {@code '\0'} to draw a solid block
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a packed float color
     * @param lightColor the color to mix with the background, as a packed float color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithConsistentLight(int x, int y, char c, float foreground, float background, float lightColor, float lightAmount) {
        putWithConsistentLight(x, y, c, foreground, background, lightColor, lightAmount, 0.0015f);
    }
    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position. This will use the same
     * brightness for all cells given identical lightAmount values when this is called; this differentiates it from
     * {@link #putWithLight(int, int, float, float, float, Noise.Noise3D)}, which would light "splotches" of map with
     * brighter or darker color. Instead, if lightAmount is obtained via SquidLib's {@code FOV} class, then all cells
     * at a short distance from an FOV center will be lit brightly and cells far away will flicker in and out of view.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to draw; may be {@code '\0'} to draw a solid block
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a packed float color
     * @param lightColor the color to mix with the background, as a packed float color
     * @param lightAmount a double that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithConsistentLight(int x, int y, char c, float foreground, float background, float lightColor, double lightAmount) {
        putWithConsistentLight(x, y, c, foreground, background, lightColor, lightAmount, 0.0015f);
    }
    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position. This will use the same
     * brightness for all cells given identical lightAmount values when this is called; this differentiates it from
     * {@link #putWithLight(int, int, float, float, float, Noise.Noise3D)}, which would light "splotches" of map with
     * brighter or darker color. Instead, if lightAmount is obtained via SquidLib's {@code FOV} class, then all cells
     * at a short distance from an FOV center will be lit brightly and cells far away will flicker in and out of view.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to draw; may be {@code '\0'} to draw a solid block
     * @param foreground the color to use for the foreground, as a packed float; if 0f, this won't place a foreground char
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a packed float color
     * @param lightColor the color to mix with the background, as a packed float color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     * @param flickerSpeed a small float multiplier applied to the time in milliseconds; often between 0.0005f and 0.005f
     */
    public void putWithConsistentLight(int x, int y, char c, float foreground, float background, float lightColor, double lightAmount, float flickerSpeed) {
        final float time = (System.currentTimeMillis() & 0xffffffL) * flickerSpeed; // if you want to adjust the speed of flicker, change the multiplier
        final long time0 = Noise.longFloor(time);
        final float noise = Noise.querp(NumberTools.randomFloatCurved(time0), NumberTools.randomFloatCurved(time0 + 1L), time - time0);
        lightAmount = Math.max(lightAmount * 0.15f, Math.min(lightAmount - NumberTools.swayTight(noise * 3.141592f) * 0.15f - 0.1f + 0.25f * noise, lightAmount)); // 0.1f * noise for light theme, 0.2f * noise for dark theme
        putWithLight(x, y, c, foreground, background, lightColor, lightAmount);
    }
    /**
     * A convenience method that handles blending the foreground color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position and placed the background
     * as-is. This will use the same brightness for all cells given identical lightAmount values when this is called;
     * this differentiates it from {@link #putWithLight(int, int, float, float, float, Noise.Noise3D)}, which would
     * light "splotches" of map with brighter or darker color. Instead, if lightAmount is obtained via SquidLib's
     * {@code FOV} class, then all cells at a short distance from an FOV center will be lit brightly and cells far away
     * will flicker in and out of view.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to draw; may be {@code '\0'} to draw a solid block
     * @param foreground the "base" color to use for the foreground, which will be combined with lightColor, as a packed float color
     * @param background the "base" color to use for the background, which will used without modification, as a packed float color
     * @param lightColor the color to mix with the foreground, as a packed float color
     * @param lightAmount a float that determines how much lightColor should affect foreground by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor foreground more
     */
    public void putWithReverseConsistentLight(int x, int y, char c, float foreground, float background, float lightColor, float lightAmount) {
        putWithReverseConsistentLight(x, y, c, foreground, background, lightColor, lightAmount, 0.0015f);
    }
    /**
     * A convenience method that handles blending the foreground color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position and placed the background
     * as-is. This will use the same brightness for all cells given identical lightAmount values when this is called;
     * this differentiates it from {@link #putWithLight(int, int, float, float, float, Noise.Noise3D)}, which would
     * light "splotches" of map with brighter or darker color. Instead, if lightAmount is obtained via SquidLib's
     * {@code FOV} class, then all cells at a short distance from an FOV center will be lit brightly and cells far away
     * will flicker in and out of view.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to draw; may be {@code '\0'} to draw a solid block
     * @param foreground the "base" color to use for the foreground, which will be combined with lightColor, as a packed float color
     * @param background the "base" color to use for the background, which will used without modification, as a packed float color
     * @param lightColor the color to mix with the foreground, as a packed float color
     * @param lightAmount a float that determines how much lightColor should affect foreground by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor foreground more
     * @param flickerSpeed a small float multiplier applied to the time in milliseconds; often between 0.0005f and 0.005f
     */
    public void putWithReverseConsistentLight(int x, int y, char c, float foreground, float background, float lightColor, float lightAmount, float flickerSpeed) {
        final float time = (System.currentTimeMillis() & 0xffffffL) * flickerSpeed; // if you want to adjust the speed of flicker, change the multiplier
        final long time0 = Noise.longFloor(time);
        final float noise = Noise.querp(NumberTools.randomFloatCurved(time0), NumberTools.randomFloatCurved(time0 + 1L), time - time0);
        lightAmount = Math.max(lightAmount * 0.15f, Math.min(lightAmount - NumberTools.swayTight(noise * 3.141592f) * 0.15f - 0.1f + 0.25f * noise, lightAmount)); // 0.1f * noise for light theme, 0.2f * noise for dark theme
        putWithReverseLight(x, y, c, foreground, background, lightColor, lightAmount);
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position. This will use the same
     * brightness for all cells given identical lightAmount values when this is called; this differentiates it from
     * {@link #putWithLight(int, int, float, float, float, Noise.Noise3D)}, which would light "splotches" of map with
     * brighter or darker color. Instead, if lightAmount is obtained via SquidLib's {@code FOV} class, then all cells
     * at a short distance from an FOV center will be lit brightly and cells far away will flicker in and out of view.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to draw; may be {@code '\0'} to draw a solid block
     * @param foreground the "base" color to use for the foreground, which will be combined with lightColor, as a libGDX Color
     * @param background the "base" color to use for the background, which will used without modification
     * @param lightColor the color to mix with the background, as a libGDX Color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithConsistentLight(int x, int y, char c, Color foreground, Color background, Color lightColor, float lightAmount) {
        putWithConsistentLight(x, y, c, foreground, background, lightColor, lightAmount, 0.0015f);
    }
    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position. This will use the same
     * brightness for all cells given identical lightAmount values when this is called; this differentiates it from
     * {@link #putWithLight(int, int, float, float, float, Noise.Noise3D)}, which would light "splotches" of map with
     * brighter or darker color. Instead, if lightAmount is obtained via SquidLib's {@code FOV} class, then all cells
     * at a short distance from an FOV center will be lit brightly and cells far away will flicker in and out of view.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to draw; may be {@code '\0'} to draw a solid block
     * @param foreground the "base" color to use for the foreground, which will used without modification
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a libGDX Color
     * @param lightColor the color to mix with the background, as a libGDX Color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     * @param flickerSpeed a small float multiplier applied to the time in milliseconds; often between 0.0005f and 0.005f
     */
    public void putWithConsistentLight(int x, int y, char c, Color foreground, Color background, Color lightColor, float lightAmount, float flickerSpeed) {
        final float time = (System.currentTimeMillis() & 0xffffffL) * flickerSpeed; // if you want to adjust the speed of flicker, change the multiplier
        final long time0 = Noise.longFloor(time);
        final float noise = Noise.querp(NumberTools.randomFloatCurved(time0), NumberTools.randomFloatCurved(time0 + 1L), time - time0);
        lightAmount = Math.max(lightAmount * 0.15f, Math.min(lightAmount - NumberTools.swayTight(noise * 3.141592f) * 0.15f - 0.1f + 0.25f * noise, lightAmount)); // 0.1f * noise for light theme, 0.2f * noise for dark theme
        putWithLight(x, y, c, foreground == null ? 0f : foreground.toFloatBits(), background == null ? 0f :  background.toFloatBits(),
                lightColor == null ? 0f : lightColor.toFloatBits(), lightAmount);
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position. This will use the same
     * brightness for all cells given identical lightAmount values when this is called; this differentiates it from
     * {@link #putWithLight(int, int, float, float, float, Noise.Noise3D)}, which would light "splotches" of map with
     * brighter or darker color. Instead, if lightAmount is obtained via SquidLib's {@code FOV} class, then all cells
     * at a short distance from an FOV center will be lit brightly and cells far away will flicker in and out of view.
     * <br>
     * Identical to calling {@link #putWithReverseConsistentLight(int, int, char, Color, Color, Color, float, float)}
     * with 0.0015f as the last parameter.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to draw; may be {@code '\0'} to draw a solid block
     * @param foreground the "base" color to use for the foreground, which will be combined with lightColor, as a libGDX Color
     * @param background the "base" color to use for the background, which will used without modification
     * @param lightColor the color to mix with the foreground, as a libGDX Color
     * @param lightAmount a float that determines how much lightColor should affect foreground by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor foreground more
     */
    public void putWithReverseConsistentLight(int x, int y, char c, Color foreground, Color background, Color lightColor, float lightAmount) {
        putWithReverseConsistentLight(x, y, c, foreground, background, lightColor, lightAmount, 0.0015f);
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position. This will use the same
     * brightness for all cells given identical lightAmount values when this is called; this differentiates it from
     * {@link #putWithLight(int, int, float, float, float, Noise.Noise3D)}, which would light "splotches" of map with
     * brighter or darker color. Instead, if lightAmount is obtained via SquidLib's {@code FOV} class, then all cells
     * at a short distance from an FOV center will be lit brightly and cells far away will flicker in and out of view.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to draw; may be {@code '\0'} to draw a solid block
     * @param foreground the "base" color to use for the foreground, which will be combined with lightColor, as a libGDX Color
     * @param background the "base" color to use for the background, which will used without modification
     * @param lightColor the color to mix with the foreground, as a libGDX Color
     * @param lightAmount a float that determines how much lightColor should affect foreground by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor foreground more
     * @param flickerSpeed a small float multiplier applied to the time in milliseconds; often between 0.0005f and 0.005f
     */
    public void putWithReverseConsistentLight(int x, int y, char c, Color foreground, Color background, Color lightColor, float lightAmount, float flickerSpeed) {
        final float time = (System.currentTimeMillis() & 0xffffffL) * flickerSpeed; // if you want to adjust the speed of flicker, change the multiplier
        final long time0 = Noise.longFloor(time);
        final float noise = Noise.querp(NumberTools.randomFloatCurved(time0), NumberTools.randomFloatCurved(time0 + 1L), time - time0);
        lightAmount = Math.max(lightAmount * 0.15f, Math.min(lightAmount - NumberTools.swayTight(noise * 3.141592f) * 0.15f - 0.1f + 0.25f * noise, lightAmount)); // 0.1f * noise for light theme, 0.2f * noise for dark theme
        putWithReverseLight(x, y, c, foreground == null ? 0f : foreground.toFloatBits(), background == null ? 0f :  background.toFloatBits(),
                lightColor == null ? 0f : lightColor.toFloatBits(), lightAmount);
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position. This will use the same
     * brightness for all cells given identical lightAmount values when this is called; this differentiates it from
     * {@link #putWithLight(int, int, float, float, float, Noise.Noise3D)}, which would light "splotches" of map with
     * brighter or darker color. Instead, if lightAmount is obtained via SquidLib's {@code FOV} class, then all cells
     * at a short distance from an FOV center will be lit brightly and cells far away will flicker in and out of view.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to draw; may be {@code '\0'} to draw a solid block
     * @param foreground the "base" color to use for the foreground, which will be combined with lightColor, as a libGDX Color
     * @param background the "base" color to use for the background, which will used without modification
     * @param lightColor the color to mix with the foreground, as a libGDX Color
     * @param lightAmount a double that determines how much lightColor should affect foreground by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor foreground more
     * @param flickerSpeed a small float multiplier applied to the time in milliseconds; often between 0.0005f and 0.005f
     */
    public void putWithReverseConsistentLight(int x, int y, char c, Color foreground, Color background, Color lightColor, double lightAmount, float flickerSpeed) {
        final float time = (System.currentTimeMillis() & 0xffffffL) * flickerSpeed; // if you want to adjust the speed of flicker, change the multiplier
        final long time0 = Noise.longFloor(time);
        final float noise = Noise.querp(NumberTools.randomFloatCurved(time0), NumberTools.randomFloatCurved(time0 + 1L), time - time0);
        lightAmount = Math.max(lightAmount * 0.15f, Math.min(lightAmount - NumberTools.swayTight(noise * 3.141592f) * 0.15f - 0.1f + 0.25f * noise, lightAmount)); // 0.1f * noise for light theme, 0.2f * noise for dark theme
        putWithReverseLight(x, y, c, foreground == null ? 0f : foreground.toFloatBits(), background == null ? 0f :  background.toFloatBits(),
                lightColor == null ? 0f : lightColor.toFloatBits(), lightAmount);
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a packed float
     * @param lightColor the color to mix with the background, as a packed float
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithLight(int x, int y, float background, float lightColor, float lightAmount) {
        put(x, y, '\0', 0f,
                SColor.lerpFloatColors(background, lightColor,
                        MathUtils.clamp(0xAAp-9f + (0xC8p-9f * lightAmount), 0f, 1f)));
    }
    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a packed float
     * @param lightColor the color to mix with the background, as a packed float
     * @param lightAmount a double that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithLight(int x, int y, float background, float lightColor, double lightAmount) {
        put(x, y, '\0', 0f,
                SColor.lerpFloatColors(background, lightColor,
                        MathUtils.clamp(0xAAp-9f + (0xC8p-9f * (float) lightAmount), 0f, 1f)));
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, while also putting a char on the screen; as a whole this affects one x,y position.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to put in the foreground
     * @param foreground the color to use for the foreground, as a packed float; if 0f, this won't place a foreground char
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a packed float
     * @param lightColor the color to mix with the background, as a packed float
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithLight(int x, int y, char c, float foreground, float background, float lightColor, float lightAmount) {
        put(x, y, c, foreground,
                SColor.lerpFloatColors(background, lightColor,
                        MathUtils.clamp(0xAAp-9f + (0xC8p-9f * lightAmount), 0f, 1f)));
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, while also putting a char on the screen; as a whole this affects one x,y position.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to put in the foreground
     * @param foreground the color to use for the foreground, as a packed float; if 0f, this won't place a foreground char
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a packed float
     * @param lightColor the color to mix with the background, as a packed float
     * @param lightAmount a double that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithLight(int x, int y, char c, float foreground, float background, float lightColor, double lightAmount) {
        put(x, y, c, foreground,
                SColor.lerpFloatColors(background, lightColor,
                        MathUtils.clamp(0xAAp-9f + (0xC8p-9f * (float) lightAmount), 0f, 1f)));
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, while also putting a char on the screen; as a whole this affects one x,y position.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to put in the foreground
     * @param foreground the color to use for the foreground, as a packed float; if 0f, this won't place a foreground char
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a packed float
     * @param lightColor the color to mix with the background, as a packed float
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithReverseLight(int x, int y, char c, float foreground, float background, float lightColor, float lightAmount) {
        put(x, y, c, SColor.lerpFloatColors(foreground, lightColor,
                MathUtils.clamp(0x88p-9f + (0xC8p-9f * lightAmount), 0f, 1f)), background);
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, while also putting a char on the screen; as a whole this affects one x,y position.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to put in the foreground
     * @param foreground the color to use for the foreground, as a packed float; if 0f, this won't place a foreground char
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a packed float
     * @param lightColor the color to mix with the background, as a packed float
     * @param lightAmount a double that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     */
    public void putWithReverseLight(int x, int y, char c, float foreground, float background, float lightColor, double lightAmount) {
        put(x, y, c, SColor.lerpFloatColors(foreground, lightColor,
                MathUtils.clamp(0x88p-9f + (0xC8p-9f * (float) lightAmount), 0f, 1f)), background);
    }
    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount that will be adjusted by the given {@link Noise.Noise3D} object, without putting a char on the screen;
     * as a whole this affects one x,y position and will change what it puts as the time (in milliseconds) changes. If
     * {@code flicker} is null, this will default to using {@link WhirlingNoise}. You can make the lighting dimmer by
     * using a darker color for {@code lightColor} or by giving a lower value for {@code lightAmount}.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a libGDX Color
     * @param lightColor the color to mix with the background, as a libGDX Color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     *@param flicker a Noise.Noise3D instance, such as {@link WhirlingNoise#instance}; may be null to use WhirlingNoise
     */
    public void putWithLight(int x, int y, Color background, Color lightColor, float lightAmount, Noise.Noise3D flicker) {
        putWithLight(x, y, '\0', 0f, background == null ? 0f :  background.toFloatBits(),
                lightColor == null ? 0f : lightColor.toFloatBits(), lightAmount, flicker);
    }
    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount that will be adjusted by the given {@link Noise.Noise3D} object, while also putting a char on the screen;
     * as a whole this affects one x,y position and will change what it puts as the time (in milliseconds) changes. If
     * {@code flicker} is null, this will default to using {@link WhirlingNoise}. You can make the lighting dimmer by
     * using a darker color for {@code lightColor} or by giving a lower value for {@code lightAmount}.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to put in the foreground
     * @param foreground the color to use for the foreground, as a libGDX Color; if null, this won't place a foreground char
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a libGDX Color
     * @param lightColor the color to mix with the background, as a libGDX Color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     *@param flicker a Noise.Noise3D instance, such as {@link WhirlingNoise#instance}; may be null to use WhirlingNoise
     */
    public void putWithLight(int x, int y, char c, Color foreground, Color background, Color lightColor, float lightAmount, Noise.Noise3D flicker) {
        putWithLight(x, y, c, foreground == null ? 0f : foreground.toFloatBits(), background == null ? 0f :  background.toFloatBits(),
                lightColor == null ? 0f : lightColor.toFloatBits(), lightAmount, flicker);
    }
    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount that will be adjusted by the given {@link Noise.Noise3D} object, without putting a char on the screen;
     * as a whole this affects one x,y position and will change what it puts as the time (in milliseconds) changes. If
     * {@code flicker} is null, this will default to using {@link WhirlingNoise}. You can make the lighting dimmer by
     * using a darker color for {@code lightColor} or by giving a lower value for {@code lightAmount}.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a packed float color
     * @param lightColor the color to mix with the background, as a packed float color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     *@param flicker a Noise.Noise3D instance, such as {@link WhirlingNoise#instance}; may be null to use WhirlingNoise
     */
    public void putWithLight(int x, int y, float background, float lightColor, float lightAmount, Noise.Noise3D flicker) {
        putWithLight(x, y, '\0', 0f, background, lightColor, lightAmount, flicker);
    }
    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount that will be adjusted by the given {@link Noise.Noise3D} object, while also putting a char on the screen;
     * as a whole this affects one x,y position and will change what it puts as the time (in milliseconds) changes. If
     * {@code flicker} is null, this will default to using {@link WhirlingNoise}. You can make the lighting dimmer by
     * using a darker color for {@code lightColor} or by giving a lower value for {@code lightAmount}.
     * @param x the x position, in cells
     * @param y the y position, in cells
     * @param c the char to put in the foreground
     * @param foreground the color to use for the foreground, as a packed float; if 0f, this won't place a foreground char
     * @param background the "base" color to use for the background, which will be combined with lightColor, as a packed float color
     * @param lightColor the color to mix with the background, as a packed float color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     *                    to between 0 and 1, and negative values can be given to favor background more
     *@param flicker a Noise.Noise3D instance, such as {@link WhirlingNoise#instance}; may be null to use WhirlingNoise
     */
    public void putWithLight(int x, int y, char c, float foreground, float background, float lightColor, float lightAmount, Noise.Noise3D flicker) {
        if(flicker == null)
            put(x, y, c, foreground,
                    SColor.lerpFloatColors(background, lightColor,(0xAAp-9f + (0xC8p-9f * lightAmount *
                            (1f + 0.35f * (float) WhirlingNoise.noise(x * 0.3, y * 0.3, (System.currentTimeMillis() & 0xffffffL) * 0.00125))))));
        else
            put(x, y, c, foreground,
                SColor.lerpFloatColors(background, lightColor,(0xAAp-9f + (0xC8p-9f * lightAmount *
                        (1f + 0.35f * (float) flicker.getNoise(x * 0.3, y * 0.3, (System.currentTimeMillis() & 0xffffffL) * 0.00125))))));
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
        for (int i = 0; i < layers.size(); i++) {
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
        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).clear();
        }
    }

    /**
     * Removes all foreground chars in the requested layer; does not affect the background or other layers.
     */
    public void clear(int layer)
    {
        int lay = mapping.get(layer, -1);
        if(lay >= 0)
            layers.get(lay).clear();
    }
    /**
     * Fills all of the background with the given color as libGDX Color object.
     * @param color the color to use for all of the background, as a libGDX Color or some subclass like SColor
     */
    public void fillBackground(Color color)
    {
        ArrayTools.fill(backgrounds, color == null ? 0f : scc.filter(color).toFloatBits());
    }

    /**
     * Fills all of the background with the given color as a packed float.
     * @param color the color to use for all of the background, as a packed float
     */
    public void fillBackground(float color)
    {
        ArrayTools.fill(backgrounds, color);
    }

    /**
     * Changes the background color in an area to all have the given color, as a libGDX Color (or SColor, etc.).
     * @param color a libGDX Color to fill the area with; may be null to make the background transparent
     * @param x left edge's x coordinate, in cells
     * @param y top edge's y coordinate, in cells
     * @param width the width of the area to change the color on
     * @param height the height of the area to change the color on
     */
    public void fillArea(Color color, int x, int y, int width, int height) {
        fillArea(color == null ? 0f : scc.filter(color).toFloatBits(), x, y, width, height);
    }
    /**
     * Changes the background color in an area to all have the given color, as a packed float.
     * @param color a color as a packed float to fill the area with; may be 0f to make the background transparent
     * @param x left edge's x coordinate, in cells
     * @param y top edge's y coordinate, in cells
     * @param width the width of the area to change the color on
     * @param height the height of the area to change the color on
     */
    public void fillArea(float color, int x, int y, int width, int height) {
        if (x < 0) {
            width += x;
            x = 0;
        }
        if (y < 0) {
            height += y;
            y = 0;
        }
        if (width <= 0 || height <= 0)
            return;
        for (int i = 0, xx = x; i < width && xx < gridWidth; i++, xx++) {
            for (int j = 0, yy = y; j < height && yy < gridHeight; j++, yy++) {
                backgrounds[xx][yy] = color;
            }
        }
    }

    /**
     * Gets whether any animations or other scene2d Actions are running on this SparseLayers.
     * @return true if any animations or Actions are currently running, false otherwise
     */
    @Override
    public boolean hasActiveAnimations()
    {
        if(hasActions())
            return true;
        for (int i = 0; i < glyphs.size(); i++) {
            if(glyphs.get(i).hasActions()) return true;
        }
        return false;
    }

    /**
     * Gets a direct reference to the 2D float array this uses for background colors.
     * @return a direct reference to the 2D float array this uses for background colors.
     */
    public float[][] getBackgrounds() {
        return backgrounds;
    }

    /**
     * Changes the reference this uses for the float array for background colors; this will not accept null parameters,
     * nor will it accept any 2D array with dimensions that do not match the (unchanging) gridWidth and gridHeight of
     * this SparseLayers.
     * @param backgrounds a non-null 2D float array of colors; must have width == gridWidth and height == gridHeight
     */
    public void setBackgrounds(float[][] backgrounds) {
        if(backgrounds == null || backgrounds[0] == null)
            throw new IllegalArgumentException("SparseLayers.backgrounds must not be set to null");
        if(backgrounds.length != gridWidth || backgrounds[0].length != gridHeight)
            throw new IllegalArgumentException("Must be given a 2D array with equal dimensions to the current gridWidth and gridHeight");
        this.backgrounds = backgrounds;
    }

    /**
     * Gets a direct reference to the TextCellFactory this uses to draw and size its text items and cells.
     * @return a direct reference to the TextCellFactory this uses to draw and size its text items and cells.
     */
    public TextCellFactory getFont() {
        return font;
    }

    /**
     * Sets the TextCellFactory this uses to draw and size its text items and cells. The given TextCellFactory must not
     * be null. If font is uninitialized, this will initialize it using cellWidth and cellHeight. If font has been
     * initialized with a different height and width, then the sizing of this SparseLayers will change.
     * @param font a non-null TextCellFactory; if uninitialized, this will initialize it using cellWidth and cellHeight.
     */
    public void setFont(TextCellFactory font) {
        if(font == null)
            throw new IllegalArgumentException("SparseLayers.font must not be set to null");
        if(!font.initialized())
            font.width(cellWidth()).height(cellHeight()).initBySize();
        this.font = font;
    }

    /**
     * Gets the IColorCenter for Color objects (almost always a SquidColorCenter, but this isn't guaranteed) that this
     * uses to cache and possibly alter Colors that given to it as parameters.
     * @return the IColorCenter of Color that this uses to cache and modify Colors given to it
     */
    public IColorCenter<Color> getScc() {
        return scc;
    }

    /**
     * Sets the IColorCenter for Color objects that this uses to cache and modify Colors given to it; does not accept
     * null parameters.
     * @param scc a non-null IColorCenter of Color that this will use to cache and modify Colors given to it
     */
    public void setScc(IColorCenter<Color> scc) {
        if(scc == null)
            throw new IllegalArgumentException("The IColorCenter<Color> given to setScc() must not be null");
        this.scc = scc;
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

    @Override
    public void blend(int x, int y, float color, float mixBy)
    {
        backgrounds[x][y] = SColor.lerpFloatColorsBlended(backgrounds[x][y], color, mixBy);
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
        SparseTextMap stm = layers.get(0);
        char shown = stm.getChar(code, ' ');
        float color = stm.getFloat(code, 0f);
        stm.remove(code);
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
            SparseTextMap stm = layers.get(layer);
            int code = SparseTextMap.encodePosition(x, y);
            char shown = stm.getChar(code, ' ');
            float color = stm.getFloat(code, 0f);
            stm.remove(code);
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
        layers.get(0).place(gridX(glyph.getY()), gridY(glyph.getY()), glyph.shown, glyph.getPackedColor());
        glyphs.remove(glyph);
    }

    /**
     * Brings a Glyph back into normal rendering, removing it from the Glyphs this class knows about and filling the
     * grid's char at the Glyph's position in the given layer with the Glyph's char and color.
     * @param glyph the Glyph to remove and fit back into the grid
     */
    public void recallToGrid(TextCellFactory.Glyph glyph, int layer)
    {
        layer = mapping.get(layer, 0);
        layers.get(layer).place(gridX(glyph.getY()), gridY(glyph.getY()), glyph.shown, glyph.getPackedColor());
        glyphs.remove(glyph);
    }

    /**
     * A way to remove a Glyph from the group of glyphs this renders, while also ending any animations or other Actions
     * that the removed Glyph was scheduled to perform.
     * @param glyph a Glyph that should be removed from the {@link #glyphs} List this holds
     */
    public void removeGlyph(TextCellFactory.Glyph glyph)
    {
        glyph.clearActions();
        glyphs.remove(glyph);
    }
    /**
     * Start a bumping animation in the given direction that will last duration seconds.
     * @param glyph
     *              A {@link TextCellFactory.Glyph}, probably produced by
     *              {@link #glyph(char, float, int, int)} or {@link #glyphFromGrid(int, int)}
     * @param direction the direction for the glyph to bump towards
     * @param duration a float, measured in seconds, for how long the animation should last; commonly 0.12f
     */
    public void bump(final TextCellFactory.Glyph glyph, Direction direction, float duration) {
        bump(0f, glyph, direction, duration, null);
    }
    /**
     * Start a bumping animation in the given direction after delay seconds, that will last duration seconds; runs
     * postRunnable after the duration completes if postRunnable is non-null.
     * 
     * @param delay how long to wait in seconds before starting the effect
     * @param glyph
     *              A {@link TextCellFactory.Glyph}, which should be produced by a SparseLayers method like
     *              {@link #glyph(char, float, int, int)} or {@link #glyphFromGrid(int, int)}
     * @param direction the direction for the glyph to bump towards
     * @param duration a float, measured in seconds, for how long the animation should last; commonly 0.12f
     * @param postRunnable a Runnable to execute after the bump completes; may be null to do nothing.
     */
    public void bump(final float delay, final TextCellFactory.Glyph glyph, Direction direction, float duration, final /* @Nullable */ Runnable postRunnable)
    {
        final float x = glyph.getX(),
                y = glyph.getY();
        duration = Math.max(0.015f, duration);
        final int nbActions = 2 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        int index = 0;
        final Action[] sequence = new Action[nbActions];
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        sequence[index++] = Actions.moveToAligned(x + direction.deltaX * 0.35f * font.actualCellWidth,
                y - direction.deltaY * 0.35f * font.actualCellHeight,
                Align.bottomLeft, duration * 0.35F);
        sequence[index++] = Actions.moveToAligned(x, y, Align.bottomLeft, duration * 0.65F);
        if(postRunnable != null)
        {
            sequence[index] = Actions.run(postRunnable);
        }
        glyph.addAction(Actions.sequence(sequence));
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
        slide(0f, glyph, startX, startY, newX, newY, duration, postRunnable);
    }

    /**
     * Slides {@code glyph} from {@code (xstartX,startY)} to {@code (newx, newy)} after waiting {@code delay} seconds.
     * Takes a number of seconds equal to duration to complete (starting after the delay). This also allows
     * a Runnable to be given as {@code postRunnable} to be run after the
     * slide completes, or null to not run anything after the slide.
     *
     * @param delay how long to wait in seconds before starting the effect
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
    public void slide(float delay, TextCellFactory.Glyph glyph, final int startX, final int startY, final int newX,
                      final int newY, float duration, /* @Nullable */ Runnable postRunnable) {
        glyph.setPosition(worldX(startX), worldY(startY));
        duration = Math.max(0.015f, duration);
        final int nbActions = 1 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        int index = 0;
        final Action[] sequence = new Action[nbActions];
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        final float nextX = worldX(newX);
        final float nextY = worldY(newY);
        sequence[index++] = Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration);
        if(postRunnable != null)
        {
            sequence[index] = Actions.run(postRunnable);
        }

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
        wiggle(0f, glyph, duration, null);
    }

    /**
     * Starts a wiggling animation for the object at the given location, after waiting delay seconds, for the given
     * duration in seconds; runs postRunnable afterwards if it is non-null.
     *
     * @param delay how long to wait in seconds before starting the effect
     * @param glyph
     *            A {@link TextCellFactory.Glyph}, probably produced by
     *            {@link #glyph(char, float, int, int)} or {@link #glyphFromGrid(int, int)}
     * @param duration in seconds, as a float
     * @param postRunnable a Runnable to execute after the wiggle completes; may be null to do nothing.
     */
    public void wiggle(final float delay, final TextCellFactory.Glyph glyph, float duration,
            /* @Nullable */ Runnable postRunnable) {
        final float x = glyph.getX(), y = glyph.getY(),
                cellWidth = font.actualCellWidth, cellHeight = font.actualCellHeight;
        duration = Math.max(0.015f, duration);
        final int nbActions = 5 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        final Action[] sequence = new Action[nbActions];
        int index = 0;
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        final StatefulRNG gRandom = DefaultResources.getGuiRandom();
        sequence[index++] = Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f, Align.bottomLeft, duration * 0.2F);
        sequence[index++] = Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f, Align.bottomLeft, duration * 0.2F);
        sequence[index++] = Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f, Align.bottomLeft, duration * 0.2F);
        sequence[index++] = Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f, Align.bottomLeft, duration * 0.2F);
        sequence[index++] = Actions.moveToAligned(x, y, Align.bottomLeft, duration * 0.2F);
        if(postRunnable != null)
        {
            sequence[index] = Actions.run(postRunnable);
        }
        glyph.addAction(Actions.sequence(sequence));
    }
    /**
     * Tints the background at position x,y so it becomes the given encodedColor, then after the tint is complete it 
     * returns the cell to its original color, taking duration seconds.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the tint this applies, then apply the tint when you call act(), then quickly overwrite
     * the tint in the next frame. That visually appears as nothing happening other than a delay.
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
     * @param color what to transition the cell's color towards, and then transition back from, as a Color object
     * @param duration how long the total "round-trip" transition should take in seconds
     */
    public void tint(final int x, final int y, final Color color, float duration) {
        tint(0f, x, y, color.toFloatBits(), duration, null);
    }
    /**
     * Tints the background at position x,y so it becomes the given encodedColor, then after the tint is complete it 
     * returns the cell to its original color, taking duration seconds.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the tint this applies, then apply the tint when you call act(), then quickly overwrite
     * the tint in the next frame. That visually appears as nothing happening other than a delay.
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
     * @param encodedColor what to transition the cell's color towards, and then transition back from, as a packed float
     * @param duration how long the total "round-trip" transition should take in seconds
     */
    public void tint(final int x, final int y, final float encodedColor, float duration) {
        tint(0f, x, y, encodedColor, duration, null);
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
    public void tint(final float delay, final int x, final int y, final float encodedColor, float duration,
            /* @Nullable */ Runnable postRunnable) {
        if(x < 0 || x >= gridWidth || y < 0 || y >= gridHeight)
            return;
        duration = Math.max(0.015f, duration);
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
        sequence[index] = Actions.delay(duration, Actions.run(new Runnable() {
            @Override
            public void run() {
                backgrounds[x][y] = ac;
            }
        }));

        addAction(Actions.sequence(sequence));
    }

    /**
     * Tints the foreground in the given layer at position x,y so it becomes the given encodedColor, then after the tint
     * is complete it returns the cell to its original color, taking duration seconds.
     * <br>
     * The {@link SquidPanel#tint(float, int, int, Color, float, Runnable)} method has been reworked to use the same
     * technique this class uses for rendering text, and the two classes should have similar appearance (if not the
     * same) when rendering the same text. SparseLayers tends to be faster, especially when not all of the map is shown.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the tint this applies, then apply the tint when you call act(), then quickly overwrite
     * the tint in the next frame. That visually appears as nothing happening other than a delay.
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
     * @param layer which layer to affect; if you haven't specified a layer when placing text, then this should be 0
     * @param color what to transition the cell's color towards, and then transition back from, as a Color object
     * @param duration how long the total "round-trip" transition should take in seconds
     */
    public void tint(final int x, final int y, final int layer, final Color color, float duration) {
        tint(0f, x, y, layer, color.toFloatBits(), duration, null);
    }

    /**
     * Tints the foreground in the given layer at position x,y so it becomes the given encodedColor, then after the tint
     * is complete it returns the cell to its original color, taking duration seconds.
     * <br>
     * The {@link SquidPanel#tint(float, int, int, Color, float, Runnable)} method has been reworked to use the same
     * technique this class uses for rendering text, and the two classes should have similar appearance (if not the
     * same) when rendering the same text. SparseLayers tends to be faster, especially when not all of the map is shown.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the tint this applies, then apply the tint when you call act(), then quickly overwrite
     * the tint in the next frame. That visually appears as nothing happening other than a delay.
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
     * @param layer which layer to affect; if you haven't specified a layer when placing text, then this should be 0
     * @param encodedColor what to transition the cell's color towards, and then transition back from, as a packed float
     * @param duration how long the total "round-trip" transition should take in seconds
     */
    public void tint(final int x, final int y, final int layer, final float encodedColor, float duration) {
        tint(0f, x, y, layer, encodedColor, duration, null);
    }
    /**
     * Tints the foreground in the given layer at position x,y so it becomes the given encodedColor, waiting for
     * {@code delay} (in seconds) before performing it, then after the tint is complete it returns the cell to its
     * original color, taking duration seconds. Additionally, enqueue {@code postRunnable} for running after the created
     * action ends.
     * <br>
     * The {@link SquidPanel#tint(float, int, int, Color, float, Runnable)} method has been reworked to use the same
     * technique this class uses for rendering text, and the two classes should have similar appearance (if not the
     * same) when rendering the same text. SparseLayers tends to be faster, especially when not all of the map is shown.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the tint this applies, then apply the tint when you call act(), then quickly overwrite
     * the tint in the next frame. That visually appears as nothing happening other than a delay.
     * @param delay how long to wait in seconds before starting the effect
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
     * @param layer which layer to affect; if you haven't specified a layer when placing text, then this should be 0
     * @param encodedColor what to transition the cell's color towards, and then transition back from, as a packed float
     * @param duration how long the total "round-trip" transition should take in seconds
     * @param postRunnable a Runnable to execute after the tint completes; may be null to do nothing.
     */
    public void tint(final float delay, final int x, final int y, final int layer, final float encodedColor, float duration,
            /* @Nullable */ Runnable postRunnable) {
        if(x < 0 || x >= gridWidth || y < 0 || y >= gridHeight || layer < 0 || layer >= layers.size())
            return;
        final SparseTextMap l = layers.get(layer);
        duration = Math.max(0.015f, duration);
        final int pos = SparseTextMap.encodePosition(x, y);
        final float ac = l.getFloat(pos,0f);
        final int nbActions = 3 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        final Action[] sequence = new Action[nbActions];
        int index = 0;
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        sequence[index++] = new TemporalAction(duration * 0.3f) {
            @Override
            protected void update(float percent) {
                l.updateFloat(pos, SColor.lerpFloatColors(ac, encodedColor, percent));
            }
        };
        sequence[index++] = new TemporalAction(duration * 0.7f) {
            @Override
            protected void update(float percent) {
                l.updateFloat(pos, SColor.lerpFloatColors(encodedColor, ac, percent));
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
                l.updateFloat(pos, ac);
            }
        }));
        addAction(Actions.sequence(sequence));
    }
    /**
     * Tints the given glyph (which may or may not be part of the {@link #glyphs} list this holds) so it becomes the
     * given encodedColor, then after the tint is complete it returns the cell to its original color, taking duration
     * seconds. This resets the glyph to its pre-tint color before it ends.
     * @param glyph the {@link TextCellFactory.Glyph} to tint
     * @param encodedColor what to transition the cell's color towards, and then transition back from, as a packed float
     * @param duration how long the total "round-trip" transition should take in seconds
     */
    public void tint(final TextCellFactory.Glyph glyph, final float encodedColor, float duration) {
        tint(0f, glyph, encodedColor, duration, null);
    }
    /**
     * Tints the given glyph (which may or may not be part of the {@link #glyphs} list this holds) so it becomes the
     * given encodedColor, waiting for {@code delay} (in seconds) before performing it, then after the tint is complete
     * it returns the cell to its original color, taking duration seconds. Additionally, enqueue {@code postRunnable}
     * for running after the created action ends. This resets the glyph to its pre-tint color before it runs any
     * {@code postRunnable}.
     * @param delay how long to wait in seconds before starting the effect
     * @param glyph the {@link TextCellFactory.Glyph} to tint
     * @param encodedColor what to transition the cell's color towards, and then transition back from, as a packed float
     * @param duration how long the total "round-trip" transition should take in seconds
     * @param postRunnable a Runnable to execute after the tint completes; may be null to do nothing.
     */
    public void tint(float delay, final TextCellFactory.Glyph glyph, final float encodedColor, float duration,
            /* @Nullable */ Runnable postRunnable) {
        duration = Math.max(0.015f, duration);
        final float ac = glyph.getPackedColor();
        final int nbActions = 3 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        final Action[] sequence = new Action[nbActions];
        int index = 0;
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        sequence[index++] = new TemporalAction(duration * 0.3f) {
            @Override
            protected void update(float percent) {
                glyph.setPackedColor(SColor.lerpFloatColors(ac, encodedColor, percent));
            }
        };
        sequence[index++] = new TemporalAction(duration * 0.7f) {
            @Override
            protected void update(float percent) {
                glyph.setPackedColor(SColor.lerpFloatColors(encodedColor, ac, percent));
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
                glyph.setPackedColor(ac);
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
        summon(0f, startX, startY, endX, endY, shown, startColor, endColor, duration, null);
    }
    /**
     * Create a new Glyph at (startX, startY) using the char shown with the given startColor, and after delay seconds,
     * starts changing color to endColor, changing position so it ends on the cell (endX, endY), taking duration seconds
     * to complete before running postRunnable (if it is non-null) and finally removing the Glyph.
     * <br>
     * Unlike {@link SquidPanel#summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)},
     * this does not rotate the Glyph it produces.
     * @param delay how long to wait in seconds before starting the effect
     * @param startX the starting x position in cells
     * @param startY the starting y position in cells
     * @param endX the ending x position in cells
     * @param endY the ending y position in cells
     * @param shown the char to show (the same char throughout the effect)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param duration the duration in seconds for the effect
     * @param postRunnable a Runnable to execute after the summon completes; may be null to do nothing.
     */
    public void summon(float delay, int startX, int startY, int endX, int endY, char shown,
                       final float startColor, final float endColor, float duration,
            /* @Nullable */ Runnable postRunnable)
    {
        duration = Math.max(0.015f, duration);
        final int nbActions = 2 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        int index = 0;
        final Action[] sequence = new Action[nbActions];
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        final TextCellFactory.Glyph glyph = glyph(shown, startColor, startX, startY);
        sequence[index++] = Actions.parallel(
                new TemporalAction(duration) {
                    @Override
                    protected void update(float percent) {
                        glyph.setPackedColor(SColor.lerpFloatColors(startColor, endColor, percent * 0.95f));
                    }
                },
                Actions.moveTo(worldX(endX), worldY(endY), duration));
        if(postRunnable != null)
        {
            sequence[index++] = Actions.run(postRunnable);
        }
        /* Do this one last, so that hasActiveAnimations() returns true during 'postRunnables' */
        sequence[index] = Actions.run(new Runnable() {
            @Override
            public void run() {
                glyphs.remove(glyph);
            }
        });
        glyph.addAction(Actions.sequence(sequence));
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
        summon(0f, startX, startY, endX, endY, shown, startColor, endColor, duration, null);
    }
    /**
     * Create a new Glyph at (startX, startY) in world coordinates (often pixels on the screen) using the char shown
     * with the given startColor, and after delay seconds, starts changing color to endColor, changing position so it
     * ends at the world coordinates (endX, endY), taking duration seconds to complete before running postRunnable (if
     * it is non-null) and finally removing the Glyph.
     * <br>
     * Unlike {@link SquidPanel#summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)},
     * this does not rotate the Glyph it produces.
     * @param delay how long to wait in seconds before starting the effect
     * @param startX the starting x position in world coordinates
     * @param startY the starting y position in world coordinates
     * @param endX the ending x position in world coordinates
     * @param endY the ending y position in world coordinates
     * @param shown the char to show (the same char throughout the effect)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param duration the duration in seconds for the effect
     * @param postRunnable a Runnable to execute after the summon completes; may be null to do nothing.
     */
    public void summon(float delay, float startX, float startY, float endX, float endY, char shown,
                       final float startColor, final float endColor, float duration,
            /* @Nullable */ Runnable postRunnable)
    {
        duration = Math.max(0.015f, duration);
        final int nbActions = 2 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        int index = 0;
        final Action[] sequence = new Action[nbActions];
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        final TextCellFactory.Glyph glyph = glyph(shown, startColor, gridX(startX), gridY(startY));
        sequence[index++] = Actions.parallel(
                new TemporalAction(duration) {
                    @Override
                    protected void update(float percent) {
                        glyph.setPackedColor(SColor.lerpFloatColors(startColor, endColor, percent * 0.95f));
                    }
                },
                Actions.moveTo(endX, endY, duration));
        if(postRunnable != null)
        {
            sequence[index++] = Actions.run(postRunnable);
        }
        /* Do this one last, so that hasActiveAnimations() returns true during 'postRunnables' */
        sequence[index] = Actions.run(new Runnable() {
            @Override
            public void run() {
                glyphs.remove(glyph);
            }
        });
        glyph.addAction(Actions.sequence(sequence));
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
     * @param x the starting, center, x-position in cells to create all Actors at
     * @param y the starting, center, y-position in cells to create all Actors at
     * @param distance how far away, in cells, to move each actor from the center (Chebyshev distance, forming a square)
     * @param measurement a Radius enum that determines if 4 (DIAMOND, OCTAHEDRON) or 8 (anything else) Glyphs are
     *                    created, and the shape they will take
     * @param shown the char to use for Glyphs; should definitely be visible
     * @param startColor the encoded color to start the effect with
     * @param endColor the encoded color to end the effect on
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, int distance, Radius measurement, char shown,
                      float startColor, float endColor, float duration) {
        burst(0f, x, y, distance, measurement, shown, startColor, endColor, duration, null);
    }
    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, float, float, float, Runnable)} repeatedly with
     * different parameters. As with summon(), this creates temporary Glyphs that change color and position.
     * The distance is how many cells away to move the created Actors away from (x,y). The measurement determines
     * whether this produces Glyphs in 4 (cardinal) directions for DIAMOND or OCTAHEDRON, or 8 (cardinal and diagonal)
     * directions for any other enum value for Radius; CIRCLE and SPHERE will position the 8 glyphs in a circle, while
     * SQUARE and CUBE will position their 8 glyphs in a square. This takes a delay in seconds that can be used to make
     * the effect wait to start for some amount of time, and a Runnable that will be run once after the burst's full
     * duration completes (not once per summoned Glyph).
     * <br>
     * Unlike {@link SquidPanel#burst(float, int, int, int, boolean, char, Color, Color, boolean, float, float)}, this
     * does not rotate the individual Glyphs it produces.
     * @param delay how long to wait in seconds before starting the effect
     * @param x the starting, center, x-position in cells to create all Actors at
     * @param y the starting, center, y-position in cells to create all Actors at
     * @param distance how far away, in cells, to move each actor from the center (Chebyshev distance, forming a square)
     * @param measurement a Radius enum that determines if 4 (DIAMOND, OCTAHEDRON) or 8 (anything else) Glyphs are
     *                    created, and the shape they will take
     * @param shown the char to use for Glyphs; should definitely be visible
     * @param startColor the encoded color to start the effect with
     * @param endColor the encoded color to end the effect on
     * @param duration how long, in seconds, the effect should last
     * @param postRunnable a Runnable to execute after the burst completes; may be null to do nothing,
     *                     and will only be run once for the whole burst effect.
     */
    public void burst(float delay, int x, int y, int distance, Radius measurement, char shown,
                      float startColor, float endColor, float duration, /* @Nullable */ Runnable postRunnable)
    {
        Direction d;
        switch (measurement)
        {
            case SQUARE:
            case CUBE:
                for (int i = 0; i < 7; i++) {
                    d = Direction.CLOCKWISE[i];
                    summon(delay, x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                            shown, startColor, endColor, duration, null);
                }
                d = Direction.CLOCKWISE[7];
                summon(delay, x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                        shown, startColor, endColor, duration, postRunnable);
                break;
            case CIRCLE:
            case SPHERE:
                float xf = worldX(x), yf = worldY(y);
                for (int i = 0; i < 4; i++) {
                    d = Direction.DIAGONALS[i];
                    summon(delay, xf, yf, xf - d.deltaX * font.actualCellWidth * distance * 0.7071067811865475f, // the constant is 1.0 / Math.sqrt(2.0)
                            yf + d.deltaY * font.actualCellHeight * distance * 0.7071067811865475f,
                            shown, startColor, endColor, duration, null);
                }
                // break intentionally absent
            default:
                for (int i = 0; i < 3; i++) {
                    d = Direction.CARDINALS_CLOCKWISE[i];
                    summon(delay, x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                            shown, startColor, endColor, duration, null);
                }
                d = Direction.CARDINALS_CLOCKWISE[3];
                summon(delay, x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                        shown, startColor, endColor, duration, postRunnable);
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
     * @param x the starting, center, x-position in cells to create all Actors at
     * @param y the starting, center, y-position in cells to create all Actors at
     * @param distance how far away, in cells, to move each actor from the center (Chebyshev distance, forming a square)
     * @param measurement a Radius enum that determines if 4 (DIAMOND, OCTAHEDRON) or 8 (anything else) Glyphs are
     *                    created, and the shape they will take
     * @param choices a String or other CharSequence containing the chars this can randomly choose
     * @param startColor the encoded color to start the effect with
     * @param endColor the encoded color to end the effect on
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, int distance, Radius measurement, CharSequence choices,
                      float startColor, float endColor, float duration) {
        burst(0f, x, y, distance, measurement, choices, startColor, endColor, duration, null);
    }
    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(int, int, int, int, char, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Glyphs that change color and position.
     * The distance is how many cells away to move the created Actors away from (x,y). The measurement determines
     * whether this produces Glyphs in 4 (cardinal) directions for DIAMOND or OCTAHEDRON, or 8 (cardinal and diagonal)
     * directions for any other enum value for Radius; CIRCLE and SPHERE will position the 8 glyphs in a circle, while
     * SQUARE and CUBE will position their 8 glyphs in a square. This takes a delay in seconds that can be used to make
     * the effect wait to start for some amount of time, and a Runnable that will be run once after the burst's full
     * duration completes (not once per summoned Glyph).
     * <br>
     * Unlike {@link SquidPanel#burst(float, int, int, int, boolean, char, Color, Color, boolean, float, float)}, this
     * does not rotate the individual Glyphs it produces.
     * @param delay how long to wait in seconds before starting the effect
     * @param x the starting, center, x-position in cells to create all Actors at
     * @param y the starting, center, y-position in cells to create all Actors at
     * @param distance how far away, in cells, to move each actor from the center (Chebyshev distance, forming a square)
     * @param measurement a Radius enum that determines if 4 (DIAMOND, OCTAHEDRON) or 8 (anything else) Glyphs are
     *                    created, and the shape they will take
     * @param choices a String or other CharSequence containing the chars this can randomly choose
     * @param startColor the encoded color to start the effect with
     * @param endColor the encoded color to end the effect on
     * @param duration how long, in seconds, the effect should last
     * @param postRunnable a Runnable to execute after the burst completes; may be null to do nothing,
     *                     and will only be run once for the whole burst effect.
     */
    public void burst(float delay, int x, int y, int distance, Radius measurement, CharSequence choices,
                      float startColor, float endColor, float duration, /* @Nullable */ Runnable postRunnable)
    {
        Direction d;
        final int len = choices.length();
        final StatefulRNG rng = DefaultResources.getGuiRandom();
        switch (measurement)
        {
            case SQUARE:
            case CUBE:
                for (int i = 0; i < 7; i++) {
                    d = Direction.CLOCKWISE[i];
                    summon(delay, x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                            choices.charAt(rng.nextIntHasty(len)), startColor, endColor, duration, null);
                }
                d = Direction.CLOCKWISE[7];
                summon(delay, x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                        choices.charAt(rng.nextIntHasty(len)), startColor, endColor, duration, postRunnable);

                break;
            case CIRCLE:
            case SPHERE:
                float xf = worldX(x), yf = worldY(y);
                for (int i = 0; i < 4; i++) {
                    d = Direction.DIAGONALS[i];
                    summon(delay, xf, yf, xf - d.deltaX * font.actualCellWidth * distance * 0.7071067811865475f,
                            yf + d.deltaY * font.actualCellHeight * distance * 0.7071067811865475f,
                            choices.charAt(rng.nextIntHasty(len)), startColor, endColor, duration, null);
                }
                // break intentionally absent
            default:
                for (int i = 0; i < 3; i++) {
                    d = Direction.CARDINALS_CLOCKWISE[i];
                    summon(delay, x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                            choices.charAt(rng.nextIntHasty(len)), startColor, endColor, duration, null);
                }
                d = Direction.CARDINALS_CLOCKWISE[3];
                summon(delay, x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                        choices.charAt(rng.nextIntHasty(len)), startColor, endColor, duration, postRunnable);

                break;
        }
    }

    /**
     * Changes the background at position x,y so it becomes the given color, taking duration seconds. The
     * background will keep the changed color after the effect, unless drawn over.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the recolor this applies, then apply the recolor when you call act(), then quickly
     * overwrite the recolor in the next frame. That visually appears as nothing happening other than a delay.
     * @param x the x-coordinate of the cell to recolor
     * @param y the y-coordinate of the cell to recolor
     * @param color what to gradually change the cell's color to, as a Color object
     * @param duration how long the total transition should take in seconds
     */
    public void recolor(final int x, final int y, final Color color, float duration) {
        recolor(0f, x, y, color.toFloatBits(), duration, null);
    }

    /**
     * Changes the background at position x,y so it becomes the given encodedColor, taking duration seconds. The
     * background will keep the changed color after the effect, unless drawn over.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the recolor this applies, then apply the recolor when you call act(), then quickly
     * overwrite the recolor in the next frame. That visually appears as nothing happening other than a delay.
     * @param x the x-coordinate of the cell to recolor
     * @param y the y-coordinate of the cell to recolor
     * @param encodedColor what to gradually change the cell's color to, as a packed float
     * @param duration how long the total transition should take in seconds
     */
    public void recolor(final int x, final int y, final float encodedColor, float duration) {
        recolor(0f, x, y, encodedColor, duration, null);
    }

    /**
     * Changes the background at position x,y so it becomes the given encodedColor, waiting for {@code delay} (in
     * seconds) before performing it, taking duration seconds. The background will keep the changed color after
     * the effect, unless drawn over.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the recolor this applies, then apply the recolor when you call act(), then quickly
     * overwrite the recolor in the next frame. That visually appears as nothing happening other than a delay.
     * @param delay how long to wait in seconds before starting the effect
     * @param x the x-coordinate of the cell to recolor
     * @param y the y-coordinate of the cell to recolor
     * @param encodedColor what to gradually change the cell's color to, as a packed float
     * @param duration how long the total transition should take in seconds
     */
    public void recolor(final float delay, final int x, final int y, final float encodedColor, float duration) {
        recolor(delay, x, y, encodedColor, duration, null);
    }
    /**
     * Changes the background at position x,y so it becomes the given encodedColor, waiting for {@code delay} (in
     * seconds) before performing it, taking duration seconds. The background will keep the changed color after the
     * effect, unless drawn over. Additionally, enqueue {@code postRunnable} for running after the created action ends.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the recolor this applies, then apply the recolor when you call act(), then quickly
     * overwrite the recolor in the next frame. That visually appears as nothing happening other than a delay.
     * @param delay how long to wait in seconds before starting the effect
     * @param x the x-coordinate of the cell to recolor
     * @param y the y-coordinate of the cell to recolor
     * @param encodedColor what to gradually change the cell's color to, as a packed float
     * @param duration how long the total transition should take in seconds
     * @param postRunnable a Runnable to execute after the recolor completes; may be null to do nothing.
     */
    public void recolor(final float delay, final int x, final int y, final float encodedColor, float duration,
            /* @Nullable */ Runnable postRunnable) {
        if(x < 0 || x >= gridWidth || y < 0 || y >= gridHeight)
            return;
        duration = Math.max(0.015f, duration);
        final float ac = backgrounds[x][y];
        final int nbActions = 2 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        final Action[] sequence = new Action[nbActions];
        int index = 0;
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        sequence[index++] = new TemporalAction(duration) {
            @Override
            protected void update(float percent) {
                backgrounds[x][y] = SColor.lerpFloatColors(ac, encodedColor, percent);
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
                backgrounds[x][y] = encodedColor;
            }
        }));

        addAction(Actions.sequence(sequence));
    }




    /**
     * Changes the foreground in the given layer at position x,y so it becomes the given color, taking duration
     * seconds. The foreground color will keep the changed color after the effect, unless drawn over.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the recolor this applies, then apply the recolor when you call act(), then quickly
     * overwrite the recolor in the next frame. That visually appears as nothing happening other than a delay.
     * @param x the x-coordinate of the cell to recolor
     * @param y the y-coordinate of the cell to recolor
     * @param layer which layer to affect; if you haven't specified a layer when placing text, then this should be 0
     * @param color what to gradually change the cell's color to, as a Color object
     * @param duration how long the total transition should take in seconds
     */
    public void recolor(final int x, final int y, final int layer, final Color color, float duration) {
        recolor(0f, x, y, layer, color.toFloatBits(), duration, null);
    }
    /**
     * Changes the foreground in the given layer at position x,y so it becomes the given encodedColor, taking duration
     * seconds. The foreground color will keep the changed color after the effect, unless drawn over.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the recolor this applies, then apply the recolor when you call act(), then quickly
     * overwrite the recolor in the next frame. That visually appears as nothing happening other than a delay.
     * @param x the x-coordinate of the cell to recolor
     * @param y the y-coordinate of the cell to recolor
     * @param layer which layer to affect; if you haven't specified a layer when placing text, then this should be 0
     * @param encodedColor what to gradually change the cell's color to, as a packed float
     * @param duration how long the total transition should take in seconds
     */
    public void recolor(final int x, final int y, final int layer, final float encodedColor, float duration) {
        recolor(0f, x, y, layer, encodedColor, duration, null);
    }


    /**
     * Changes the foreground in the given layer at position x,y so it becomes the given encodedColor, waiting for
     * {@code delay} (in seconds) before performing it, taking duration seconds. The foreground color will keep the 
     * changed color after the effect, unless drawn over.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the recolor this applies, then apply the recolor when you call act(), then quickly
     * overwrite the recolor in the next frame. That visually appears as nothing happening other than a delay.
     * @param delay how long to wait in seconds before starting the effect
     * @param x the x-coordinate of the cell to recolor
     * @param y the y-coordinate of the cell to recolor
     * @param layer which layer to affect; if you haven't specified a layer when placing text, then this should be 0
     * @param encodedColor what to gradually change the cell's color to, as a packed float
     * @param duration how long the total transition should take in seconds
     */
    public void recolor(final float delay, final int x, final int y, final int layer, final float encodedColor, float duration) {
        recolor(delay, x, y, layer, encodedColor, duration, null);
    }


    /**
     * Changes the foreground in the given layer at position x,y so it becomes the given encodedColor, waiting for
     * {@code delay} (in seconds) before performing it, taking duration seconds. The foreground color will keep the 
     * changed color after the effect, unless drawn over. Additionally, enqueue {@code postRunnable} for running after
     * the created action ends.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the recolor this applies, then apply the recolor when you call act(), then quickly
     * overwrite the recolor in the next frame. That visually appears as nothing happening other than a delay.
     * @param delay how long to wait in seconds before starting the effect
     * @param x the x-coordinate of the cell to recolor
     * @param y the y-coordinate of the cell to recolor
     * @param layer which layer to affect; if you haven't specified a layer when placing text, then this should be 0
     * @param encodedColor what to gradually change the cell's color to, as a packed float
     * @param duration how long the total transition should take in seconds
     * @param postRunnable a Runnable to execute after the recolor completes; may be null to do nothing.
     */
    public void recolor(final float delay, final int x, final int y, final int layer, final float encodedColor, float duration,
            /* @Nullable */ Runnable postRunnable) {
        if(x < 0 || x >= gridWidth || y < 0 || y >= gridHeight || layer < 0 || layer >= layers.size())
            return;
        final SparseTextMap l = layers.get(layer);
        duration = Math.max(0.015f, duration);
        final int pos = SparseTextMap.encodePosition(x, y);
        final float ac = l.getFloat(pos, 0f);
        final int nbActions = 2 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        final Action[] sequence = new Action[nbActions];
        int index = 0;
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        sequence[index++] = new TemporalAction(duration) {
            @Override
            protected void update(float percent) {
                l.updateFloat(pos, SColor.lerpFloatColors(ac, encodedColor, percent));
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
                l.updateFloat(pos, encodedColor);
            }
        }));
        addAction(Actions.sequence(sequence));
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
        float xo = getX(), yo = getY(), yOff = yo + 1f + gridHeight * font.actualCellHeight, gxo, gyo;
        font.draw(batch, backgrounds, xo, yo);
        int len = layers.size();
        Frustum frustum = null;
        Stage stage = getStage();
        if(stage != null) {
            Viewport viewport = stage.getViewport();             
            if(viewport != null)
            {
                Camera camera = viewport.getCamera();
                if(camera != null)
                {
                    if(
                            camera.frustum != null &&
                                    (!camera.frustum.boundsInFrustum(xo, yOff - font.actualCellHeight - 1f, 0f, font.actualCellWidth, font.actualCellHeight, 0f) ||
                                     !camera.frustum.boundsInFrustum(xo + font.actualCellWidth * (gridWidth-1), yo, 0f, font.actualCellWidth, font.actualCellHeight, 0f))
                    )
                        frustum = camera.frustum;
                }
            }
        }
        font.configureShader(batch);
        if(frustum == null) {
            for (int i = 0; i < len; i++) {
                layers.get(i).draw(batch, font, xo, yOff);
            }

        }
        else
        {
            for (int i = 0; i < len; i++) {
                layers.get(i).draw(batch, font, frustum, xo, yOff);
            }
        }
        
        int x, y;
        for (int i = 0; i < glyphs.size(); i++) {
            TextCellFactory.Glyph glyph = glyphs.get(i);
            if(glyph == null)
                continue;
            glyph.act(Gdx.graphics.getDeltaTime());
            if(
                    !glyph.isVisible() ||
                    (x = Math.round((gxo = glyph.getX() - xo) / font.actualCellWidth)) < 0 || x >= gridWidth ||
                    (y = Math.round((gyo = glyph.getY() - yo)  / -font.actualCellHeight + gridHeight)) < 0 || y >= gridHeight ||
                    backgrounds[x][y] == 0f || (frustum != null && !frustum.boundsInFrustum(gxo, gyo, 0f, font.actualCellWidth, font.actualCellHeight, 0f)))
                continue;
            glyph.draw(batch, 1f);
        }
    }
}
