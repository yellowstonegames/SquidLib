package squidpony.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.ArrayTools;
import squidpony.IColorCenter;
import squidpony.squidgrid.gui.gdx.*;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Tommy Ettinger on 8/28/2018.
 */
public class SparseLayersExperiment extends SparseLayers {
    public SparseLayersExperiment(int gridWidth, int gridHeight)
    {
        this(gridWidth, gridHeight, 10, 16, DefaultResources.getStretchableFont());
    }

    public SparseLayersExperiment(int gridWidth, int gridHeight, float cellWidth, float cellHeight)
    {
        this(gridWidth, gridHeight, cellWidth, cellHeight, DefaultResources.getStretchableFont());
    }

    public SparseLayersExperiment(int gridWidth, int gridHeight, float cellWidth, float cellHeight, TextCellFactory font) {
        this(gridWidth, gridHeight, cellWidth, cellHeight, font, 0f, 0f);
    }

    public SparseLayersExperiment(int gridWidth, int gridHeight, float cellWidth, float cellHeight, TextCellFactory font, float xOffset, float yOffset) {
        this.gridWidth = MathUtils.clamp(gridWidth, 1, 65535);
        this.gridHeight = MathUtils.clamp(gridHeight, 1, 65535);
        backgrounds = ArrayTools.fill(0f, this.gridWidth * 3, this.gridHeight * 3);
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
     * setting the background colors to match the given Color 2D array. If the colors argument is null, does nothing. If
     * the chars argument is null, only affects the background colors. This will filter each Color in colors if the
     * color center this uses has a filter.
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
     * setting the background colors to match the given 2D array of colors as packed floats. If the colors argument is
     * null, does nothing. If the chars argument is null, only affects the background colors. This will not filter the
     * passed colors at all.
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
        Arrays.fill(backgrounds[x * 3], y * 3, y * 3 + 3, bg);
        Arrays.fill(backgrounds[x * 3 + 1], y * 3, y * 3 + 3, bg);
        Arrays.fill(backgrounds[x * 3 + 2], y * 3, y * 3 + 3, bg);
    }

    /**
     * Changes the background at position x,y to the given color as an encoded float. The color can be transparent,
     * which will show through to whatever is behind this SparseLayers, or the color the screen was last cleared with.
     * Unlike other methods in this class, a float equal to 0f will be used instead of being used to skip over a cell,
     * and will change the background at the given position to fully transparent.
     *
     * @param x     where to change the background color, x-coordinate
     * @param y     where to change the background color, y-coordinate
     * @param color the color, as an encoded float, to change to; may be transparent, and considers 0f a valid color
     */
    @Override
    public void put(int x, int y, float color) {
        Arrays.fill(backgrounds[x * 3], y * 3, y * 3 + 3, color);
        Arrays.fill(backgrounds[x * 3 + 1], y * 3, y * 3 + 3, color);
        Arrays.fill(backgrounds[x * 3 + 2], y * 3, y * 3 + 3, color);
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

    public void putSingle(int x, int y, float color)
    {
        backgrounds[x][y] = color;
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
            if(
                    !glyph.isVisible() ||
                            (x = Math.round((gxo = glyph.getX() - xo) / font.actualCellWidth)) < 0 || x >= gridWidth ||
                            (y = Math.round((gyo = glyph.getY() - yo)  / -font.actualCellHeight + gridHeight)) < 0 || y >= gridHeight ||
                            backgrounds[x * 3 + 1][y * 3 + 1] == 0f || (frustum != null && !frustum.boundsInFrustum(gxo, gyo, 0f, font.actualCellWidth, font.actualCellHeight, 0f)))
                continue;
            glyph.draw(batch, 1f);
        }
    }
}
