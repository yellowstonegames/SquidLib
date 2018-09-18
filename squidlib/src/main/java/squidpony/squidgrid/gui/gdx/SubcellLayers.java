package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.IColorCenter;
import squidpony.panel.ISquidPanel;

import java.util.ArrayList;

/**
 * A subclass of SparseLayers that acts almost the same, but uses 3x3 subcells of background color for every cell that
 * may contain a char. Methods that affect the background sometimes specify positions in subcells, which allows
 * affecting less than one cell with some visual effect. You will usually want to use a triple-size resistance and FOV
 * map; you can get a resistance map that respects subcells with
 * {@link squidpony.squidgrid.mapping.DungeonUtility#generateResistances3x3(char[][])} (this looks better if it is given
 * a "line" dungeon as produced by {@link squidpony.squidgrid.mapping.DungeonUtility#hashesToLines(char[][], boolean)}),
 * and then you can use that resistance map with normal FOV methods, just typically at triple vision range. Much of the
 * time, it makes sense to run both that triple-range FOV, and a normal-range FOV with a resistance map calculated in
 * cells; you can use the normal-range FOV for gameplay and AI calculations and the triple-range for visual display. 
 * <br>
 * Created by Tommy Ettinger on 8/28/2018.
 */
public class SubcellLayers extends SparseLayers {
    public SubcellLayers(int gridWidth, int gridHeight)
    {
        this(gridWidth, gridHeight, 10, 16, DefaultResources.getStretchableFont());
    }

    public SubcellLayers(int gridWidth, int gridHeight, float cellWidth, float cellHeight)
    {
        this(gridWidth, gridHeight, cellWidth, cellHeight, DefaultResources.getStretchableFont());
    }

    public SubcellLayers(int gridWidth, int gridHeight, float cellWidth, float cellHeight, TextCellFactory font) {
        this(gridWidth, gridHeight, cellWidth, cellHeight, font, 0f, 0f);
    }

    public SubcellLayers(int gridWidth, int gridHeight, float cellWidth, float cellHeight, TextCellFactory font, float xOffset, float yOffset) {
        this.gridWidth = MathUtils.clamp(gridWidth, 1, 65535);
        this.gridHeight = MathUtils.clamp(gridHeight, 1, 65535);
        backgrounds = new float[this.gridWidth*3][this.gridHeight*3];
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
     * Places the given char 2D array, if-non-null, in the default foreground color starting at x=0, y=0, while also
     * setting the background colors to match the given Color 2D array. The colors 2D array should have a width that is
     * 3 * {@link #gridWidth} and a height that is 3 * {@link #gridHeight}. If the colors argument is null, does not
     * affect backgrounds but may still affect chars. If the chars argument is null, only affects the background colors.
     * This will filter each Color in colors if the color center this uses has a filter.
     *
     * @param chars  Can be {@code null}, indicating that only colors must be put.
     * @param colors the background colors for the given chars; this array should have 3 times the width and height of chars
     */
    @Override
    public void put(char[][] chars, Color[][] colors) {
        super.putChars(chars);
        super.put(null, colors);
    }

    /**
     * Places the given char 2D array, if-non-null, in the default foreground color starting at x=0, y=0, while also
     * setting the background colors to match the given 2D array of colors as packed floats. The colors 2D array should
     * have a width that is 3 * {@link #gridWidth} and a height that is 3 * {@link #gridHeight}. If the colors argument 
     * is null, does not affect backgrounds but may still affect chars. If the chars argument is null, only affects the
     * background colors. This will not filter the passed colors at all.
     *
     * @param chars  Can be {@code null}, indicating that only colors must be put.
     * @param colors the background colors for the given chars; this array should have 3 times the width and height of chars
     */
    @Override
    public void put(char[][] chars, float[][] colors) {
        super.putChars(chars);
        super.put(null, colors);
    }

    /**
     * Places the given char 2D array, if-non-null, with the given foreground colors in the first Color 2D array,
     * starting at x=0, y=0, while also setting the background colors to match the second Color 2D array. If the
     * bgColors argument is null, only affects foreground chars and colors. If the chars argument or the fgColors
     * argument is null, only affects the background colors. Any positions where a Color in fgColors is null will not
     * have a char placed (this can be used to restrict what is placed). This will filter each Color in the background
     * and foreground if the color center this uses has a filter.
     *
     * @param chars    Can be {@code null}, indicating that only colors must be put.
     * @param fgColors the foreground Colors for the given chars
     * @param bgColors the background Colors for the given chars; this array should have 3 times the width and height of chars
     */
    @Override
    public void put(char[][] chars, Color[][] fgColors, Color[][] bgColors) {
        super.putChars(chars, fgColors);
        super.put(null, bgColors);
    }

    /**
     * Places the given char 2D array, if-non-null, with the given foreground colors in the first float 2D array,
     * starting at x=0, y=0, while also setting the background colors to match the second float 2D array. If the
     * bgColors argument is null, only affects foreground chars and colors. If the chars argument or the fgColors
     * argument is null, only affects the background colors. Any positions where a float in fgColors is 0 will not
     * have a char placed (this can be used to restrict what is placed). This will not filter any colors.
     *
     * @param chars    Can be {@code null}, indicating that only colors must be put.
     * @param fgColors the foreground colors for the given chars, as packed floats
     * @param bgColors the background colors for the given chars, as packed floats; this array should have 3 times the width and height of chars
     */
    @Override
    public void put(char[][] chars, float[][] fgColors, float[][] bgColors) {
        super.putChars(chars, fgColors);
        super.put(null, bgColors);
    }

    /**
     * Puts the char c at the position x,y with the given foreground and background colors. If foreground is null or is
     * fully transparent (all channels 0), then this does not change the foreground contents.
     * If background is null or is fully transparent, this does not change the background. Uses the color center to
     * potentially filter the given colors; this can be changed with {@link #setScc(IColorCenter)}.
     *
     * @param x          the x position to place the char at
     * @param y          the y position to place the char at
     * @param c          the char to place
     * @param foreground the color to use for c; if null or fully transparent, nothing will change in the foreground
     * @param background the color to use for the cell; if null, nothing will change in the background
     */
    @Override
    public void put(int x, int y, char c, Color foreground, Color background) {
        super.put(x, y, c, foreground, null);
        if(background != null) 
            put(x, y, background);
    }

    /**
     * Puts the char c at the position x,y with the given foreground and background colors as encoded floats, such as
     * those produced by {@link Color#toFloatBits()}. If foreground is 0f, then this does not
     * change the foreground contents. If background is 0f, this does not change the background. Does not filter the
     * given colors.
     *
     * @param x          the x position to place the char at
     * @param y          the y position to place the char at
     * @param c          the char to place
     * @param foreground the color to use for c; if 0f, nothing will change in the foreground
     * @param background the color to use for the cell; if null or fully transparent, nothing will change in the background
     */
    @Override
    public void put(int x, int y, char c, float foreground, float background) {
        super.put(x, y, c, foreground, 0f);
        if(background != 0f)
            put(x, y, background);
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
     *
     * @param x          the x position to place the char at
     * @param y          the y position to place the char at
     * @param c          the char to place
     * @param foreground the color to use for c; if null or fully transparent, nothing will change in the foreground
     * @param background the color to use for the cell; if null or fully transparent, nothing will change in the background
     * @param layer      the layer to place the colorful char into; should usually be between 0 and {@link #getLayerCount()} inclusive
     */
    @Override
    public void put(int x, int y, char c, Color foreground, Color background, int layer) {
        super.put(x, y, c, foreground, null, layer);
        if(background != null)
            put(x, y, background);
    }

    /**
     * Puts the char c at the position x,y in the requested layer with the given foreground and background colors as
     * encoded floats, such as those produced by {@link Color#toFloatBits()}. If foreground is 0f, then this does not
     * change the foreground contents. If background is 0f, this does not change the
     * background. Does not filter the given colors. The layer can be greater than the number of layers currently
     * present, which will add a layer to be rendered over the existing layers, but the number that refers to that layer
     * will not change. It is recommended that to add a layer, you only add at the value equal to
     * {@link #getLayerCount()}, which will maintain the order and layer numbers in a sane way.
     *
     * @param x          the x position to place the char at
     * @param y          the y position to place the char at
     * @param c          the char to place
     * @param foreground the color to use for c; if 0f, nothing will change in the foreground
     * @param background the color to use for the cell; if null or fully transparent, nothing will change in the background
     * @param layer      the layer to place the colorful char into; should usually be between 0 and {@link #getLayerCount()} inclusive
     */
    @Override
    public void put(int x, int y, char c, float foreground, float background, int layer) {
        super.put(x, y, c, foreground, 0f, layer);
        if(background != 0f)
            put(x, y, background);

    }

    /**
     * Puts text at the position x,y with the given foreground and background colors. If foreground is null or is
     * fully transparent (all channels 0), then this does not change the foreground contents.
     * If background is null or is fully transparent, this does not change the background. Uses the color center to
     * potentially filter the given colors; this can be changed with {@link #setColorCenter(IColorCenter)}.
     *
     * @param x          the x position to place the String at
     * @param y          the y position to place the String at
     * @param text       the String to place
     * @param foreground the color to use for text; if null or fully transparent, nothing will change in the foreground
     * @param background the color to use for the cells; if null or fully transparent, nothing will change in the background
     */
    @Override
    public void put(int x, int y, String text, Color foreground, Color background) {
        super.put(x, y, text, foreground, null);
        if(background != null && text != null)
        {
            for (int i = 0; i < text.length(); i++) {
                put(x+i, y, background);
            }
        }
    }

    /**
     * Puts text at the position x,y with the given foreground and background colors as encoded floats, such as
     * those produced by {@link Color#toFloatBits()}. If foreground is 0f, then this does not change the foreground
     * contents. If background is 0f, this does not change the background. Does not filter the given colors.
     *
     * @param x          the x position to place the String at
     * @param y          the y position to place the String at
     * @param text       the String to place
     * @param foreground the color to use for text; if 0f, nothing will change in the foreground
     * @param background the color to use for the cells; if null or fully transparent, nothing will change in the background
     */
    @Override
    public void put(int x, int y, String text, float foreground, float background) {
        super.put(x, y, text, foreground, 0f);
        if(background != 0f && text != null)
        {
            for (int i = 0; i < text.length(); i++) {
                put(x+i, y, background);
            }
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
     *
     * @param x          the x position to place the String at
     * @param y          the y position to place the String at
     * @param text       the String to place
     * @param foreground the color to use for text; if null or fully transparent, nothing will change in the foreground
     * @param background the color to use for the cells; if null or fully transparent, nothing will change in the background
     * @param layer      the layer to place the colorful char into; should usually be between 0 and {@link #getLayerCount()} inclusive
     */
    @Override
    public void put(int x, int y, String text, Color foreground, Color background, int layer) {
        super.put(x, y, text, foreground, null, layer);
        if(background != null && text != null)
        {
            for (int i = 0; i < text.length(); i++) {
                put(x+i, y, background);
            }
        }
    }

    /**
     * Puts text at the position x,y in the requested layer with the given foreground and background colors as encoded
     * floats, such as those produced by {@link Color#toFloatBits()}. If foreground is 0f, then this does not change the
     * foreground contents. If background is 0f, this does not change the background. Does not filter the given colors.
     * The layer can be greater than the number of layers currently present, which will add a layer to be rendered over
     * the existing layers, but the number that refers to that layer will not change. It is recommended that to add a
     * layer, you only add at the value equal to {@link #getLayerCount()}, which will maintain the order and layer
     * numbers in a sane way.
     *
     * @param x          the x position to place the String at
     * @param y          the y position to place the String at
     * @param text       the String to place
     * @param foreground the color to use for text; if 0f, nothing will change in the foreground
     * @param background the color to use for the cells; if null or fully transparent, nothing will change in the background
     * @param layer      the layer to place the colorful char into; should usually be between 0 and {@link #getLayerCount()} inclusive
     */
    @Override
    public void put(int x, int y, String text, float foreground, float background, int layer) {
        super.put(x, y, text, foreground, 0f, 0);
        if(background != 0f && text != null)
        {
            for (int i = 0; i < text.length(); i++) {
                put(x+i, y, background);
            }
        }
    }

    /**
     * Changes the background at position x,y to the given Color. If the color is null, then this will make the
     * background fully transparent at the specified position.
     *
     * @param x     where to change the background color, x-coordinate
     * @param y     where to change the background color, y-coordinate
     * @param color the Color to change to; if null will be considered fully transparent
     */
    @Override
    public void put(int x, int y, Color color) {
        final float bg = (color == null) ? 0x0.0p0F : scc.filter(color).toFloatBits();
        x *= 3;
        y *= 3;
        backgrounds[x  ][y  ] = bg;
        backgrounds[x  ][y+1] = bg;
        backgrounds[x  ][y+2] = bg;
        backgrounds[x+1][y  ] = bg;
        backgrounds[x+1][y+1] = bg;
        backgrounds[x+1][y+2] = bg;
        backgrounds[x+2][y  ] = bg;
        backgrounds[x+2][y+1] = bg;
        backgrounds[x+2][y+2] = bg;
    }

    /**
     * Changes the background at position x,y to the given color as an encoded float. The color can be transparent,
     * which will show through to whatever is behind this SparseLayers, or the color the screen was last cleared with.
     * Unlike other methods in this class, a float equal to 0f will be used instead of being used to skip over a cell,
     * and will change the background at the given position to fully transparent.
     *
     * @param x     where to change the background color, x-coordinate
     * @param y     where to change the background color, y-coordinate
     * @param bg    the color, as an encoded float, to change to; may be transparent, and considers 0f a valid color
     */
    @Override
    public void put(int x, int y, float bg) {
        x *= 3;
        y *= 3;
        backgrounds[x  ][y  ] = bg;
        backgrounds[x  ][y+1] = bg;
        backgrounds[x  ][y+2] = bg;
        backgrounds[x+1][y  ] = bg;
        backgrounds[x+1][y+1] = bg;
        backgrounds[x+1][y+2] = bg;
        backgrounds[x+2][y  ] = bg;
        backgrounds[x+2][y+1] = bg;
        backgrounds[x+2][y+2] = bg;
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position.
     *
     * @param x           the x position, in cells
     * @param y           the y position, in cells
     * @param background  the "base" color to use for the background, which will be combined with lightColor, as a libGDX Color
     * @param lightColor  the color to mix with the background, as a libGDX Color
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     */
    @Override
    public void putWithLight(int x, int y, Color background, Color lightColor, float lightAmount) {
        putWithLight(x, y, background == null ? 0f :  background.toFloatBits(),
                lightColor == null ? 0f : lightColor.toFloatBits(), lightAmount);
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position.
     *
     * @param x           the x position, in subcells (3 subcells in a row have the width of one character cell)
     * @param y           the y position, in subcells (3 subcells in a column have the height of one character cell)
     * @param background  the "base" color to use for the background, which will be combined with lightColor, as a packed float
     * @param lightColor  the color to mix with the background, as a packed float
     * @param lightAmount a float that determines how much lightColor should affect background by; not strictly limited
     */
    @Override
    public void putWithLight(int x, int y, float background, float lightColor, float lightAmount) {         
        putSingle(x, y,
                SColor.lerpFloatColors(background, lightColor,
                        MathUtils.clamp(0xAAp-9f + (0xC8p-9f * lightAmount), 0f, 1f)) 
                        );

    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position. This will use the same
     * brightness for all cells given identical lightAmount values when this is called; this differentiates it from
     * the putWithLight methods that take a Noise3D argument, which would light "splotches" of map with
     * brighter or darker color. Instead, if lightAmount is obtained via SquidLib's {@code FOV} class, then all cells
     * at a short distance from an FOV center will be lit brightly and cells far away will flicker in and out of view.
     *
     * @param x            the x position, in cells
     * @param y            the y position, in cells
     * @param background   the "base" color to use for the background, which will be combined with lightColor, as a packed float color
     * @param lightColor   the color to mix with the background, as a packed float color
     * @param lightAmount  a float that determines how much lightColor should affect background by; not strictly limited
     *                     to between 0 and 1, and negative values can be given to favor background more
     * @param flickerSpeed a small float multiplier applied to the time in milliseconds; often between 0.0005f and 0.005f
     */
    @Override
    public void putWithConsistentLight(int x, int y, float background, float lightColor, float lightAmount, float flickerSpeed) {
        super.putWithConsistentLight(x, y, background, lightColor, lightAmount, flickerSpeed);
    }

    /**
     * A convenience method that handles blending the background color with a specified light color, by a specific
     * amount, without putting a char on the screen; as a whole this affects one x,y position. This will use the same
     * brightness for all cells given identical lightAmount values when this is called; this differentiates it from
     * the putWithLight methods that take a Noise3D argument, which would light "splotches" of map with
     * brighter or darker color. Instead, if lightAmount is obtained via SquidLib's {@code FOV} class, then all cells
     * at a short distance from an FOV center will be lit brightly and cells far away will flicker in and out of view.
     *
     * @param x            the x position, in cells
     * @param y            the y position, in cells
     * @param background   the "base" color to use for the background, which will be combined with lightColor, as a libGDX Color
     * @param lightColor   the color to mix with the background, as a libGDX Color
     * @param lightAmount  a float that determines how much lightColor should affect background by; not strictly limited
     *                     to between 0 and 1, and negative values can be given to favor background more
     * @param flickerSpeed a small float multiplier applied to the time in milliseconds; often between 0.0005f and 0.005f
     */
    @Override
    public void putWithConsistentLight(int x, int y, Color background, Color lightColor, float lightAmount, float flickerSpeed) {
        super.putWithConsistentLight(x, y, background, lightColor, lightAmount, flickerSpeed);
    }

    /**
     * Sets a single subcell of the background to use the specified color as a packed float. The values given for x and
     * y refer to a triple-width, triple-height grid, so the meaning of x and y are different here from other methods.
     * If you want to set the center subcell of the cell that would normally be set with
     * {@link #put(int, int, char, float, float)} at the position x=20,y=30, this would set that subcell at
     * x=20*3+1,y=30*3+1 , where the *3 gets the x and y to refer to the correct 3x3 subcell grid, and the +1 chooses
     * the subcell as one to the right and one down.
     * @param x the x position in the triple-width, triple-height grid of background subcells
     * @param y the y position in the triple-width, triple-height grid of background subcells
     * @param color a packed float color to place in the background of one subcell
     */
    public void putSingle(int x, int y, float color)
    {
        backgrounds[x][y] = color;
    }

    /**
     * Sets the background colors to match the given Color 2D array. The colors 2D array should have a width that is 3 *
     * {@link #gridWidth} and a height that is 3 * {@link #gridHeight}. If the colors argument is null, does nothing.
     * This will filter each Color in colors if the color center this uses has a filter.
     *
     * @param colors the background colors for the given chars
     */
    @Override
    public void put(Color[][] colors) {
        super.put(colors);
    }

    /**
     * Sets the background colors to match the given 2D array of colors as packed floats. The colors 2D array should
     * have a width that is 3 * {@link #gridWidth} and a height that is 3 * {@link #gridHeight}. If the colors argument
     * is null, does nothing. This will not filter the passed colors at all.
     *
     * @param colors the background colors to use for this SparseLayers
     */
    @Override
    public void put(float[][] colors) {
        super.put(colors);
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
     * Removes the foreground chars, where present, in all layers at the given x,y position.
     * The backgrounds will be unchanged.
     *
     * @param x the x-coordinate of the position to remove all chars from
     * @param y the y-coordinate of the position to remove all chars from
     */
    @Override
    public void clear(int x, int y) {
        if(x < 0 || x >= gridWidth || y < 0 || y >= gridHeight)
            return;
        int code = SparseTextMap.encodePosition(x, y);
        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).remove(code);
        }
        x *= 3;
        y *= 3;
        backgrounds[x  ][y  ] = 0f;
        backgrounds[x  ][y+1] = 0f;
        backgrounds[x  ][y+2] = 0f;
        backgrounds[x+1][y  ] = 0f;
        backgrounds[x+1][y+1] = 0f;
        backgrounds[x+1][y+2] = 0f;
        backgrounds[x+2][y  ] = 0f;
        backgrounds[x+2][y+1] = 0f;
        backgrounds[x+2][y+2] = 0f;

    }

    /**
     * Changes the background color in an area to all have the given color, as a libGDX Color (or SColor, etc.).
     * This uses subcell measurements for x, y, width, and height, where the grid width in subcells is
     * 3 * {@link #gridWidth} and the grid height in subcells is 3 * {@link #gridHeight}. To affect a full cell, this
     * needs a width and height of at least 3 each; anything less will affect only some subcells.
     *
     * @param color  a libGDX Color to fill the area with; may be null to make the background transparent
     * @param x      left edge's x coordinate, in subcells (3 subcells to one cell horizontally)
     * @param y      top edge's y coordinate, in subcells (3 subcells to one cell vertically)
     * @param width  the width of the area to change the color on, in subcells (3 subcells to one cell horizontally)
     * @param height the height of the area to change the color on, in subcells (3 subcells to one cell vertically)
     */
    @Override
    public void fillArea(Color color, int x, int y, int width, int height) {
        fillArea(color == null ? 0f : scc.filter(color).toFloatBits(), x, y, width, height);
    }
    /**
     * Changes the background color in an area to all have the given color, as a packed float.
     * This uses subcell measurements for x, y, width, and height, where the grid width in subcells is
     * 3 * {@link #gridWidth} and the grid height in subcells is 3 * {@link #gridHeight}. To affect a full cell, this
     * needs a width and height of at least 3 each; anything less will affect only some subcells.
     * 
     * @param color a color as a packed float to fill the area with; may be 0f to make the background transparent
     * @param x      left edge's x coordinate, in subcells (3 subcells to one cell horizontally)
     * @param y      top edge's y coordinate, in subcells (3 subcells to one cell vertically)
     * @param width  the width of the area to change the color on, in subcells (3 subcells to one cell horizontally)
     * @param height the height of the area to change the color on, in subcells (3 subcells to one cell vertically)
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
        final int gw = gridWidth * 3, gh = gridHeight * 3;
        for (int i = 0, xx = x; i < width && xx < gw; i++, xx++) {
            for (int j = 0, yy = y; j < height && yy < gh; j++, yy++) {
                backgrounds[xx][yy] = color;
            }
        }
    }

    /**
     * Using the existing background color at the subcell position x,y, this performs color blending from that existing
     * color to the given color (as a float), using the mixBy parameter to determine how much of the color parameter to
     * use (1f will set the color in this to the parameter, while 0f for mixBy will ignore the color parameter
     * entirely). The x and y parameters are in subcells, where 3 subcells have the same width or height as one cell (a
     * cell holds a char, and 3x3 subcells fit in the same area).
     * @param x the x component of the position in this panel to draw the starting color from
     * @param y the y component of the position in this panel to draw the starting color from
     * @param color the new color to mix with the starting color; a packed float, as made by {@link Color#toFloatBits()}
     * @param mixBy the amount by which the new color will affect the old one, between 0 (no effect) and 1 (overwrite)
     */
    @Override
    public void blend(int x, int y, float color, float mixBy)
    {
        backgrounds[x][y] = SColor.lerpFloatColorsBlended(backgrounds[x][y], color, mixBy);
    }

    /**
     * Tints the background at position x,y (in cells) so it becomes the given encodedColor, waiting for {@code delay}
     * (in seconds) before performing it, then after the tint is complete it returns the cell to its original color,
     * taking duration seconds. Additionally, enqueue {@code postRunnable} for running after the created action ends.
     * All subcells in the tinted cell will reach the same color during this animation, but the subcells can start with
     * different colors, and they will return to those starting colors after this animation finishes.
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
        final int xx = x * 3, yy = y * 3;
        final float
                x0y0 = backgrounds[xx][yy], x1y0 = backgrounds[xx+1][yy], x2y0 = backgrounds[xx+2][yy],
                x0y1 = backgrounds[xx][yy+1], x1y1 = backgrounds[xx+1][yy+1], x2y1 = backgrounds[xx+2][yy+1],
                x0y2 = backgrounds[xx][yy+2], x1y2 = backgrounds[xx+1][yy+2], x2y2 = backgrounds[xx+2][yy+2];
        final int nbActions = 3 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        final Action[] sequence = new Action[nbActions];
        int index = 0;
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        sequence[index++] = new TemporalAction(duration * 0.3f) {
            @Override
            protected void update(float percent) {
                backgrounds[xx  ][yy  ] = SColor.lerpFloatColors(x0y0, encodedColor, percent);
                backgrounds[xx  ][yy+1] = SColor.lerpFloatColors(x0y1, encodedColor, percent);
                backgrounds[xx  ][yy+2] = SColor.lerpFloatColors(x0y2, encodedColor, percent);
                backgrounds[xx+1][yy  ] = SColor.lerpFloatColors(x1y0, encodedColor, percent);
                backgrounds[xx+1][yy+1] = SColor.lerpFloatColors(x1y1, encodedColor, percent);
                backgrounds[xx+1][yy+2] = SColor.lerpFloatColors(x1y2, encodedColor, percent);
                backgrounds[xx+2][yy  ] = SColor.lerpFloatColors(x2y0, encodedColor, percent);
                backgrounds[xx+2][yy+1] = SColor.lerpFloatColors(x2y1, encodedColor, percent);
                backgrounds[xx+2][yy+2] = SColor.lerpFloatColors(x2y2, encodedColor, percent);
            }
        };
        sequence[index++] = new TemporalAction(duration * 0.7f) {
            @Override
            protected void update(float percent) {
                backgrounds[xx  ][yy  ] = SColor.lerpFloatColors(encodedColor, x0y0, percent);
                backgrounds[xx  ][yy+1] = SColor.lerpFloatColors(encodedColor, x0y1, percent);
                backgrounds[xx  ][yy+2] = SColor.lerpFloatColors(encodedColor, x0y2, percent);
                backgrounds[xx+1][yy  ] = SColor.lerpFloatColors(encodedColor, x1y0, percent);
                backgrounds[xx+1][yy+1] = SColor.lerpFloatColors(encodedColor, x1y1, percent);
                backgrounds[xx+1][yy+2] = SColor.lerpFloatColors(encodedColor, x1y2, percent);
                backgrounds[xx+2][yy  ] = SColor.lerpFloatColors(encodedColor, x2y0, percent);
                backgrounds[xx+2][yy+1] = SColor.lerpFloatColors(encodedColor, x2y1, percent);
                backgrounds[xx+2][yy+2] = SColor.lerpFloatColors(encodedColor, x2y2, percent);
            }
        };
        if(postRunnable != null)
        {
            sequence[index++] = Actions.run(postRunnable);
        }
        sequence[index] = Actions.delay(duration, Actions.run(new Runnable() {
            @Override
            public void run() {
                backgrounds[xx  ][yy  ] = x0y0;
                backgrounds[xx  ][yy+1] = x0y1;
                backgrounds[xx  ][yy+2] = x0y2;
                backgrounds[xx+1][yy  ] = x1y0;
                backgrounds[xx+1][yy+1] = x1y1;
                backgrounds[xx+1][yy+2] = x1y2;
                backgrounds[xx+2][yy  ] = x2y0;
                backgrounds[xx+2][yy+1] = x2y1;
                backgrounds[xx+2][yy+2] = x2y2;
            }
        }));

        addAction(Actions.sequence(sequence));
    }
    @Override
    public void draw(Batch batch, float parentAlpha) {
        float xo = getX(), yo = getY(), yOff = yo + 1f + gridHeight * font.actualCellHeight, gxo, gyo;
        font.draw(batch, backgrounds, xo, yo, 3, 3);
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
            if(!glyph.isVisible() ||
                            (x = Math.round((gxo = glyph.getX() - xo) / font.actualCellWidth)) < 0 || x >= gridWidth ||
                            (y = Math.round((gyo = glyph.getY() - yo)  / -font.actualCellHeight + gridHeight)) < 0 || y >= gridHeight ||
                            backgrounds[x * 3 + 1][y * 3 + 1] == 0f || (frustum != null && !frustum.boundsInFrustum(gxo, gyo, 0f, font.actualCellWidth, font.actualCellHeight, 0f)))
                continue;
            glyph.draw(batch, 1f);
        }
    }
}
