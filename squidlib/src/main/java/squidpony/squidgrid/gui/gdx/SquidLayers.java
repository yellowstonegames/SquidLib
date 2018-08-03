package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import squidpony.ArrayTools;
import squidpony.IColorCenter;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.PerlinNoise;

import java.util.ArrayList;
import java.util.Collection;

import static com.badlogic.gdx.math.MathUtils.clamp;

/**
 * A helper class to make using multiple SquidPanels easier.
 * Created by Tommy Ettinger on 7/6/2015.
 */
public class SquidLayers extends Group {
    protected int width;
    protected int height;
    protected int cellWidth;
    protected int cellHeight;
    public int[][] lightnesses;
    protected SquidPanel backgroundPanel, foregroundPanel;
    protected ArrayList<SquidPanel> extraPanels;
    protected TextCellFactory textFactory;
    protected float animationDuration;

    public static final char EMPTY_CELL = ' ';

    /**
     * The pixel width of the entire map as displayed.
     *
     * @return the pixel width of the entire map as displayed
     */
    @Override
    public float getWidth() {
        return width * cellWidth;
    }

    /**
     * The pixel height of the entire map as displayed.
     *
     * @return the pixel height of the entire map as displayed
     */
    @Override
    public float getHeight() {
        return height * cellHeight;
    }

    /**
     * Width of the shown area of the map in grid cells.
     *
     * @return the width of the map in grid cells
     */
    public int getGridWidth() {
        return width;
    }

    /**
     * Height of the shown area of the map in grid cells.
     *
     * @return the height of the map in grid cells
     */
    public int getGridHeight() {
        return height;
    }

    /**
     * Width of one cell in pixels.
     *
     * @return the width of one cell in pixels
     */
    public int getCellWidth() {
        return cellWidth;
    }

    /**
     * Height of one cell in pixels.
     *
     * @return the height of one cell in pixels
     */
    public int getCellHeight() {
        return cellHeight;
    }

    public float getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(float animationDuration) {
        this.animationDuration = animationDuration;
    }

    public TextCellFactory getTextFactory() {
        return textFactory;
    }

    /**
     * Get the lightness modifiers used for background cells as an int[][], with elements between 0 and 511, 256 as the
     * unmodified lightness level, lower numbers meaning darker, and higher meaning lighter.
     *
     * @return
     */
    public int[][] getLightnesses() {
        return lightnesses;
    }
    /**
     * Alters the lightnesses that affect the background colors, accepting a parameter for
     * animation frame if rippling water and waving grass using {@link PerlinNoise} are desired. It may make sense to
     * pass some fraction of the current time, as given by {@link System#currentTimeMillis()}, instead of a frame.
     * Does not allocate a new 2D int array like {@link MapUtility#generateLightnessModifiers(char[][])} or its
     * equivalent in DungeonUtility, and directly mutates the lightness data. You can use the {@link #lightnesses} field
     * to access the 2D array directly, where you can make further modifications to the lighting.
     * @return this for chaining
     */
    public SquidLayers autoLight () {
        return autoLight(1.2345, '~', ',');
    }

    /**
     * Alters the lightnesses that affect the background colors, accepting a parameter for
     * animation frame if rippling water and waving grass using {@link PerlinNoise} are desired. It may make sense to
     * pass some fraction of the current time, as given by {@link System#currentTimeMillis()}, instead of a frame.
     * Does not allocate a new 2D int array like {@link MapUtility#generateLightnessModifiers(char[][])} or its
     * equivalent in DungeonUtility, and directly mutates the lightness data. You can use the {@link #lightnesses} field
     * to access the 2D array directly, where you can make further modifications to the lighting.
     * @param frame         a counter that typically should increase by between 10.0 and 20.0 each second; higher numbers make
     *                      water and grass move more, and 0.013 multiplied by the current time in milliseconds works well
     *                      as long as only the smaller digits of the time are used; this can be accomplished with
     *                      {@code (System.currentTimeMillis() & 0xFFFFFF) * 0.013} .
     * @return a 2D array of lightness values from -255 to 255 but usually close to 0; can be passed to SquidLayers
     */
    public SquidLayers autoLight (double frame) {
        return autoLight(frame, '~', ',');
    }
    /**
     * Alters the lightnesses that affect the background colors, accepting a parameter for
     * animation frame if rippling water and waving grass using {@link PerlinNoise} are desired. It may make sense to
     * pass some fraction of the current time, as given by {@link System#currentTimeMillis()}, instead of a frame.
     * Also allows additional chars to be treated like deep and shallow water regarding the ripple animation.
     * Does not allocate a new 2D int array like {@link MapUtility#generateLightnessModifiers(char[][])} or its
     * equivalent in DungeonUtility, and directly mutates the lightness data. You can use the {@link #lightnesses} field
     * to access the 2D array directly, where you can make further modifications to the lighting.
     * @param frame         a counter that typically should increase by between 10.0 and 20.0 each second; higher numbers make
     *                      water and grass move more, and 0.013 multiplied by the current time in milliseconds works well
     *                      as long as only the smaller digits of the time are used; this can be accomplished with
     *                      {@code (System.currentTimeMillis() & 0xFFFFFF) * 0.013} .
     * @param deepLiquid    a char that will be treated like deep water when animating ripples
     * @param shallowLiquid a char that will be treated like shallow water when animating ripples
     * @return a 2D array of lightness values from -255 to 255 but usually close to 0; can be passed to SquidLayers
     */
    public SquidLayers autoLight (double frame, char deepLiquid, char shallowLiquid) {
        final char[][] map = foregroundPanel.contents;
        final int w = getTotalWidth(), h = getTotalHeight();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                switch (map[i][j]) {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        lightnesses[i][j] = 30;
                        break;
                    case '.':
                    case ' ':
                    case '\u0006':
                        lightnesses[i][j] = 0;
                        break;
                    case ':':
                        lightnesses[i][j] = -15;
                        break;
                    case '+':
                    case '/':
                        lightnesses[i][j] = -10;
                        break;
                    case ',':
                        lightnesses[i][j] = (int) (85 * (PerlinNoise.noise(i * 1.45, j * 1.45, frame * 0.45) * 0.55 - 0.7));
                        break;
                    case '~':
                        lightnesses[i][j] = (int) (100 * (PerlinNoise.noise(i * 1.45, j * 1.45, frame * 0.45) * 0.4 - 0.65));
                        break;
                    case '"':
                        lightnesses[i][j] = (int) (95 * (PerlinNoise.noise(i * 1.45, j * 1.45, frame * 0.5) * 0.3 - 1.5));
                        break;
                    case '^':
                        lightnesses[i][j] = 40;
                        break;
                    default:
                        if (map[i][j] == deepLiquid)
                            lightnesses[i][j] = (int) (180 * (PerlinNoise.noise(i * 4.2, j * 4.2, frame * 0.55) * 0.45 - 0.7));
                        else if (map[i][j] == shallowLiquid)
                            lightnesses[i][j] = (int) (110 * (PerlinNoise.noise(i * 3.1, j * 3.1, frame * 0.3) * 0.4 - 0.65));
                        else
                            lightnesses[i][j] = 0;
                }
            }
        }
        return this;
    }

    /**
     * Sets the lightness modifiers used for background cells with the int[][] passed as lightnesses. This 2D array
     * should have elements between 0 to 511, with 256 as the unmodified lightness level, lower numbers meaning darker,
     * and higher meaning lighter. Elements less than 0 or higher than 511 will probably cause array out-of-bounds
     * exceptions to be thrown when this renders, so just don't do that. This doesn't validate because maps can get
     * large, validating many cells could be expensive, and this might be called often if it's being called at all.
     *
     * @param lightnesses 2D array, width and height should match this class' gridWidth and gridHeight. elements must
     *                    be between 0 and 511.
     */
    public void setLightnesses(int[][] lightnesses) {
        this.lightnesses = lightnesses;
    }

    /**
     * Create a new SquidLayers widget with a default stretchable font, 40 cells wide and 40 cells high,
     * and 12x12 pixels for each cell.
     * <br>
     * This uses a default font that is not supplied in the JAR library of SquidLib; you need two files to use it if it
     * does not render correctly:
     * <ul>
     * <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Custom-distance.fnt</li>
     * <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Custom-distance.png</li>
     * </ul>
     */
    public SquidLayers() {
        this(40, 40);
    }

    /**
     * Create a new SquidLayers widget with a default stretchable font, the given number of cells for gridWidth
     * and gridHeight, and 12x12 pixels for each cell.
     * <br>
     * This uses a default font that is not supplied in the JAR library of SquidLib; you need two files to use it if it
     * does not render correctly:
     * <ul>
     * <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Custom-distance.fnt</li>
     * <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Custom-distance.png</li>
     * </ul>
     *
     * @param gridWidth  in grid cells
     * @param gridHeight in grid cells
     */
    public SquidLayers(int gridWidth, int gridHeight) {
        this(gridWidth, gridHeight, 12, 12);
    }

    /**
     * Create a new SquidLayers widget with a default stretchable font (it will adapt to the cellWidth and cellHeight
     * you give it), the given number of cells for gridWidth and gridHeight, and the size in pixels for each cell
     * given by cellWidth and cellHeight.
     * <br>
     * This uses a default font that is not supplied in the JAR library of SquidLib; you need two files to use it if it
     * does not render correctly:
     * <ul>
     * <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Custom-distance.fnt</li>
     * <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Custom-distance.png</li>
     * </ul>
     *
     * @param gridWidth  in grid cells
     * @param gridHeight in grid cells
     * @param cellWidth  in pixels
     * @param cellHeight in pixels
     */
    public SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight) {
        this(gridWidth, gridHeight, cellWidth, cellHeight, DefaultResources.getSCC(), DefaultResources.getSCC());
    }

    /**
     * Create a new SquidLayers widget with the given path to a BitmapFont file, the given number of cells for gridWidth
     * and gridHeight, and the size in pixels for each cell given by cellWidth and cellHeight.
     *
     * @param gridWidth  in grid cells
     * @param gridHeight in grid cells
     * @param cellWidth  in pixels
     * @param cellHeight in pixels
     * @param fontpath   A path to a BitmapFont in the assets folder
     */
    public SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight, String fontpath) {
        this(gridWidth, gridHeight, cellWidth, cellHeight, fontpath,
                DefaultResources.getSCC(), DefaultResources.getSCC());
    }

    /**
     * Create a new SquidLayers widget with the given path to a BitmapFont file, the given number of cells for gridWidth
     * and gridHeight, and the size in pixels for each cell given by cellWidth and cellHeight.
     *
     * @param gridWidth  in grid cells
     * @param gridHeight in grid cells
     * @param cellWidth  in pixels
     * @param cellHeight in pixels
     * @param bitmapFont A BitmapFont that you already constructed
     */
    public SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight, BitmapFont bitmapFont) {
        this(gridWidth, gridHeight, cellWidth, cellHeight, bitmapFont,
                DefaultResources.getSCC(), DefaultResources.getSCC());
    }

    /**
     * Create a new SquidLayers widget with the given path pre-constructed TextCellFactory, the given number of cells
     * for gridWidth and gridHeight, and the size in pixels for each cell given by cellWidth and cellHeight.
     * Consider using predefined TextCellFactory objects from {@link DefaultResources}, which will be configured by
     * this constructor for sizing when passed here.
     *
     * @param gridWidth  in grid cells
     * @param gridHeight in grid cells
     * @param cellWidth  in pixels
     * @param cellHeight in pixels
     * @param tcf        A TextCellFactory that you already constructed
     */
    public SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight, TextCellFactory tcf) {
        this(gridWidth, gridHeight, cellWidth, cellHeight, tcf,
                DefaultResources.getSCC(), DefaultResources.getSCC());
    }

    /**
     * Create a new SquidLayers widget with a default stretchable font (it will adapt to the cellWidth and cellHeight
     * you give it), the given number of cells for gridWidth and gridHeight, the size in pixels for each cell
     * given by cellWidth and cellHeight, and the given SquidColorCenter instances to affect colors.
     * <br>
     * This uses a default font that is not supplied in the JAR library of SquidLib; you need two files to use it if it
     * does not render correctly:
     * <ul>
     * <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Custom-distance.fnt</li>
     * <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Custom-distance.png</li>
     * </ul>
     *
     * @param gridWidth     in grid cells
     * @param gridHeight    in grid cells
     * @param cellWidth     in pixels
     * @param cellHeight    in pixels
     * @param bgColorCenter a SquidColorCenter (possibly with a filter) to use for the background
     * @param fgColorCenter a SquidColorCenter (possibly with a filter) to use for the foreground
     */
    public SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight,
                       SquidColorCenter bgColorCenter, SquidColorCenter fgColorCenter) {
        this(gridWidth, gridHeight, cellWidth, cellHeight, DefaultResources.getStretchableFont(),
                bgColorCenter, fgColorCenter);
    }

    /**
     * Create a new SquidLayers widget with the given path to a BitmapFont file, the given number of cells for gridWidth
     * and gridHeight, and the size in pixels for each cell given by cellWidth and cellHeight.
     *
     * @param gridWidth     in grid cells
     * @param gridHeight    in grid cells
     * @param cellWidth     in pixels
     * @param cellHeight    in pixels
     * @param fontpath      A path to a BitmapFont that can be on the classpath (in SquidLib) or in the assets folder.
     * @param bgColorCenter a SquidColorCenter (possibly with a filter) to use for the background
     * @param fgColorCenter a SquidColorCenter (possibly with a filter) to use for the foreground
     */
    public SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight, String fontpath,
                       SquidColorCenter bgColorCenter, SquidColorCenter fgColorCenter) {
        this(gridWidth, gridHeight, cellWidth, cellHeight, new TextCellFactory().font(fontpath), bgColorCenter, fgColorCenter);
    }

    /**
     * Create a new SquidLayers widget with the given BitmapFont (already constructed), the given number of cells for
     * gridWidth and gridHeight, and the size in pixels for each cell given by cellWidth and cellHeight.
     *
     * @param gridWidth     in grid cells
     * @param gridHeight    in grid cells
     * @param cellWidth     in pixels
     * @param cellHeight    in pixels
     * @param bitmapFont    A BitmapFont that you already constructed
     * @param bgColorCenter a SquidColorCenter (possibly with a filter) to use for the background
     * @param fgColorCenter a SquidColorCenter (possibly with a filter) to use for the foreground
     */
    public SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight, BitmapFont bitmapFont,
                       SquidColorCenter bgColorCenter, SquidColorCenter fgColorCenter) {
        this(gridWidth, gridHeight, cellWidth, cellHeight, new TextCellFactory().font(bitmapFont), bgColorCenter, fgColorCenter);
    }

    /**
     * Create a new SquidLayers widget with the given TextCellFactory, the given number of cells for gridWidth
     * and gridHeight, the size in pixels for each cell given by cellWidth and cellHeight, and the given
     * SquidColorCenters for background and foreground. Consider using predefined TextCellFactory objects from
     * {@link DefaultResources}, which will be configured by this constructor for sizing when passed here.
     *
     * @param gridWidth     in grid cells
     * @param gridHeight    in grid cells
     * @param cellWidth     in pixels
     * @param cellHeight    in pixels
     * @param tcf           A TextCellFactory that will be (re-)initialized here with the given cellHeight and cellWidth.
     * @param bgColorCenter a SquidColorCenter (possibly with a filter) to use for the background
     * @param fgColorCenter a SquidColorCenter (possibly with a filter) to use for the foreground
     */
    public SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight, TextCellFactory tcf,
                       SquidColorCenter bgColorCenter, SquidColorCenter fgColorCenter) {
        this(gridWidth, gridHeight, cellWidth, cellHeight, tcf, bgColorCenter, fgColorCenter, null);
    }

    /**
     * Create a new SquidLayers widget with the given TextCellFactory, the given number of cells for gridWidth
     * and gridHeight, the size in pixels for each cell given by cellWidth and cellHeight, the given
     * SquidColorCenters for background and foreground, and the given 2D char array for an area map that may be
     * sized differently than gridWidth by gridHeight (it is usually larger than gridWidth by gridHeight, which
     * allows camera scrolling across the map). This requires some special work with the Camera and Viewports to
     * get working correctly; in the squidlib module's examples,
     * <a href="https://github.com/SquidPony/SquidLib/blob/master/squidlib/src/test/java/squidpony/gdx/examples/EverythingDemo.java">EverythingDemo</a>
     * may be a good place to see how this can be done. You can pass null for actualMap, which will simply create
     * a char array to use internally that is exactly gridWidth by gridHeight, in cells. Consider using predefined
     * TextCellFactory objects from {@link DefaultResources}, which will be configured by this constructor for
     * sizing when passed here.
     *
     * @param gridWidth     in grid cells
     * @param gridHeight    in grid cells
     * @param cellWidth     in pixels
     * @param cellHeight    in pixels
     * @param tcf           A TextCellFactory that will be (re-)initialized here with the given cellHeight and cellWidth.
     * @param bgColorCenter a SquidColorCenter (possibly with a filter) to use for the background
     * @param fgColorCenter a SquidColorCenter (possibly with a filter) to use for the foreground
     * @param actualMap     will often be a different size than gridWidth by gridHeight, which enables camera scrolling
     */
    public SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight, TextCellFactory tcf,
                       SquidColorCenter bgColorCenter, SquidColorCenter fgColorCenter, char[][] actualMap) {

        width = gridWidth;
        height = gridHeight;

        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;

        textFactory = tcf.width(cellWidth).height(cellHeight).initBySize();

        if (actualMap == null || actualMap.length <= 0) {
            backgroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, bgColorCenter);
            foregroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, fgColorCenter);
            lightnesses = new int[width][height];
        } else {
            backgroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, bgColorCenter, 0, 0, ArrayTools.fill(' ', actualMap.length, actualMap[0].length));
            foregroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, fgColorCenter, 0, 0, actualMap);
            lightnesses = new int[actualMap.length][actualMap[0].length];
        }
        animationDuration = foregroundPanel.DEFAULT_ANIMATION_DURATION;

        extraPanels = new ArrayList<>();

        addActorAt(0, backgroundPanel);
        addActorAt(2, foregroundPanel);

        setSize(backgroundPanel.getWidth(), backgroundPanel.getHeight());
    }

    /**
     * Add an extra layer on top of the foreground layer. Use putInto methods to specify the layer when adding a char (0
     * is background, 1 is unused, 2 is foreground, and the first call to this method creates layer 3).
     *
     * @return this for chaining
     */
    public SquidLayers addExtraLayer() {
        SquidPanel sp;
        if (width != foregroundPanel.getTotalWidth() || height != foregroundPanel.getTotalHeight())
            sp = new SquidPanel(width, height, textFactory, foregroundPanel.getColorCenter(), 0, 0,
                    ArrayTools.fill(' ', foregroundPanel.getTotalWidth(), foregroundPanel.getTotalHeight()));
        else
            sp = new SquidPanel(width, height, textFactory);
        addActor(sp);
        extraPanels.add(sp);
        return this;
    }

    /**
     * Sets the size of the text in the given layer  (but not the size of the cells) to the given width and height in
     * pixels (which may be stretched by viewports later on, if your program uses them).
     *
     * @param layer the layer to affect; 0 is background, 1 is unused, 2 is foreground, 3 and higher are extra panels
     * @param wide  the width of a glyph in pixels
     * @param high  the height of a glyph in pixels
     * @return this for chaining
     */
    public SquidLayers setTextSize(int layer, float wide, float high) {
        SquidPanel p = backgroundPanel;
        switch (layer) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                p = foregroundPanel;
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        p.setTextSize(wide, high);
        return this;
    }

    /**
     * Sets the size of the text in all layers (but not the size of the cells) to the given width and height in pixels
     * (which may be stretched by viewports later on, if your program uses them).
     *
     * @param wide the width of a glyph in pixels
     * @param high the height of a glyph in pixels
     * @return this for chaining
     */
    public SquidLayers setTextSize(float wide, float high) {
        textFactory.tweakHeight(high).tweakWidth(wide).initBySize();
        setTextSize(0, wide, high);
        setTextSize(2, wide, high);
        for (int i = 0; i < extraPanels.size(); i++) {
            setTextSize(i + 3, wide, high);
        }
        return this;
    }


    /**
     * Place a char c into the foreground at position x, y, with the default color.
     *
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c a character to be drawn in the foreground
     * @return this for chaining
     */
    public SquidLayers put(int x, int y, char c) {
        foregroundPanel.put(x, y, c);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color as a libGDX Color (or SColor).
     *
     * @param x          in grid cells.
     * @param y          in grid cells.
     * @param c          a character to be drawn in the foreground
     * @param foreground Color for the char being drawn
     * @return this for chaining
     */
    public SquidLayers put(int x, int y, char c, Color foreground) {
        foregroundPanel.put(x, y, c, foreground);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color as a packed float.
     *
     * @param x                 in grid cells.
     * @param y                 in grid cells.
     * @param c                 a character to be drawn in the foreground
     * @param encodedForeground float encoding the color for the char being drawn
     * @return this for chaining
     */
    public SquidLayers put(int x, int y, char c, float encodedForeground) {
        foregroundPanel.put(x, y, c, encodedForeground);
        return this;
    }

    /**
     * Place a char c into the foreground, with a specified foreground color and background color.
     *
     * @param x          in grid cells.
     * @param y          in grid cells.
     * @param c          a character to be drawn in the foreground
     * @param foreground Color for the char being drawn
     * @param background Color for the background
     * @return this for chaining
     */
    public SquidLayers put(int x, int y, char c, Color foreground, Color background) {
        foregroundPanel.put(x, y, c, foreground);
        backgroundPanel.put(x, y, background);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground and background libGDX Color and a lightness variation for
     * the background (255 will make the background equal the background panel's
     * {@link SquidPanel#getLightingColor()}, -255 will use the background as-is, and values in between will be
     * linearly interpolated between those two extremes).
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param c                   a character to be drawn in the foreground
     * @param foreground          Color for the char being drawn
     * @param background          Color for the background
     * @param backgroundLightness int between -255 and 255 , lower numbers are changed less, higher changed closer to the lighting color
     * @return this for chaining
     */
    public SquidLayers put(int x, int y, char c, Color foreground, Color background, int backgroundLightness) {
        foregroundPanel.put(x, y, c, foreground);
        backgroundPanel.put(x, y, background, (clamp(lightnesses[x][y] + 256 + backgroundLightness, 1, 511)) * 0.001953125f);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground and background libGDX Color and a lightness variation for
     * the background (255 will make the background equal the background panel's
     * {@link SquidPanel#getLightingColor()}, -255 will use the background as-is, and values in between will be
     * linearly interpolated between those two extremes).
     *
     * @param x             in grid cells.
     * @param y             in grid cells.
     * @param c             a character to be drawn in the foreground
     * @param foreground    Color for the char being drawn
     * @param background    Color for the background
     * @param mixAmount     int between -255 and 255 , lower numbers are changed less, higher changed closer to mixBackground
     * @param mixBackground Color to mix with the background, dependent on mixAmount
     * @return this for chaining
     */
    public SquidLayers put(int x, int y, char c, Color foreground, Color background, int mixAmount, Color mixBackground) {
        foregroundPanel.put(x, y, c, foreground);
        backgroundPanel.put(x, y, background,
                (clamp(lightnesses[x][y] + 256 + mixAmount, 1, 511)) * 0.001953125f,
                mixBackground);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground and background color each encoded as a packed float and a
     * lightness variation for the background (255 will make the background equal the background panel's
     * {@link SquidPanel#getLightingColor()}, -255 will use encodedBackground as-is, and values in between will be
     * linearly interpolated between those two extremes).
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param c                   a character to be drawn in the foreground
     * @param encodedForeground   float encoding the color for the char being drawn
     * @param encodedBackground   float encoding the color for the background
     * @param backgroundLightness int between -255 and 255 , lower numbers are darker, higher lighter.
     * @return this for chaining
     */
    public SquidLayers put(int x, int y, char c, float encodedForeground, float encodedBackground, int backgroundLightness) {
        foregroundPanel.put(x, y, c, encodedForeground);
        backgroundPanel.put(x, y, encodedBackground,
                (clamp(lightnesses[x][y] + 256 + backgroundLightness, 1, 511)) * 0.001953125f);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground, background, and mix color (which affects the background)
     * each encoded as a packed float and a lightness variation for the background (255 will make the background equal
     * mixColor, -255 will use encodedBackground as-is, and values in between will be linearly interpolated between
     * those two extremes).
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param c                   a character to be drawn in the foreground
     * @param encodedForeground   float encoding the color for the char being drawn
     * @param encodedBackground   float encoding the color for the background
     * @param backgroundLightness int between -255 and 255 , lower numbers are darker, higher lighter.
     * @param mixBackground       float encoding a color to mix with the background instead of the normal lighting color
     * @return this for chaining
     */
    public SquidLayers put(int x, int y, char c, float encodedForeground, float encodedBackground, int backgroundLightness, float mixBackground) {
        foregroundPanel.put(x, y, c, encodedForeground);
        backgroundPanel.put(x, y, encodedBackground,
                (clamp(lightnesses[x][y] + 256 + backgroundLightness, 1, 511)) * 0.001953125f, mixBackground);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground, background, and mix color (which affects the background)
     * each encoded as a packed float and a lightness variation for the background (using the style that SquidPanel
     * does, with the "lightness" a float between 0.0f and 1.0f inclusive, encodedBackground used on its own for 0
     * lightness, mixBackground used on its own for 1 lightness, and values in between mixing the two).
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param c                   a character to be drawn in the foreground
     * @param encodedForeground   float encoding the color for the char being drawn
     * @param encodedBackground   float encoding the color for the background
     * @param backgroundLightness float between 0.0f and 1.0f (both inclusive); higher means closer to mixBackground
     * @param mixBackground       float encoding a color to mix with the background instead of the normal lighting color
     * @return this for chaining
     */
    public SquidLayers put(int x, int y, char c, float encodedForeground, float encodedBackground, float backgroundLightness, float mixBackground) {
        foregroundPanel.put(x, y, c, encodedForeground);
        backgroundPanel.put(x, y, encodedBackground, backgroundLightness, mixBackground);
        //        lightnesses[x][y] + (int) (backgroundLightness * 255), mixBackground);
        return this;
    }

    public SquidLayers put(int x, int y, char[][] c) {
        foregroundPanel.put(x, y, c);
        return this;
    }

    public SquidLayers put(int x, int y, char c[][], Color[][] foreground, Color[][] background) {
        foregroundPanel.put(x, y, c, foreground);
        backgroundPanel.put(x, y, background);
        return this;
    }


    /**
     * Place a 2@ char array c into the foreground, with foreground colors specified by a 2D Color array, background
     * colors with another 2D Color array, and lightness variations for the background in a 2D int array (255 will make
     * the background equal the background panel's {@link SquidPanel#getLightingColor()}, -255 will use the background
     * as-is, and values in between will be linearly interpolated between those two extremes).
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param c                   char[][] to be drawn in the foreground starting from x, y
     * @param foregrounds         Color[][] of colors for the char being drawn
     * @param backgrounds         Color[][] of colors for the background
     * @param backgroundLightness int[][] with elements between -255 and 255 , lower darker, higher lighter.
     * @return this for chaining
     */
    public SquidLayers put(int x, int y, char[][] c, Color[][] foregrounds, Color[][] backgrounds, int[][] backgroundLightness) {

        foregroundPanel.put(x, y, c, foregrounds);
        for (int i = x; i < getTotalWidth() && i - x < backgroundLightness.length; i++) {
            for (int j = y; j < getTotalHeight() && j - y < backgroundLightness[i].length; j++) {
                backgroundPanel.put(i, j, backgrounds[i - x][j - y],
                        (clamp(lightnesses[i][j] + 256 + backgroundLightness[i - x][j - y], 1, 511)) * 0.001953125f);
            }
        }
        return this;
    }


    /**
     * Place a char c into the specified layer, using the specified layer's default foreground color.
     *
     * @param layer 0 or 1 for background, 2 for foreground, 3 or higher for extra layers added on.
     * @param x     in grid cells.
     * @param y     in grid cells.
     * @param c     char to be drawn in the foreground at x, y
     * @return this for chaining
     */
    public SquidLayers putInto(int layer, int x, int y, char c) {
        getLayer(layer).put(x, y, c);
        return this;
    }

    /**
     * Place a char c into the specified layer with the specified Color.
     *
     * @param layer the layer to draw into
     * @param x     in grid cells.
     * @param y     in grid cells.
     * @param c     a character to be drawn in the specified layer
     * @param color Color for the char being drawn
     * @return this for chaining
     */
    public SquidLayers putInto(int layer, int x, int y, char c, Color color) {
        getLayer(layer).put(x, y, c, color);
        return this;
    }

    /**
     * Place a 2D char array c into the specified layer, using the specified layer's default foreground color.
     *
     * @param layer 0 or 1 for background, 2 for foreground, 3 or higher for extra layers added on.
     * @param x     in grid cells.
     * @param y     in grid cells.
     * @param c     char[][] to be drawn in the foreground starting from x, y
     * @return this for chaining
     */
    public SquidLayers putInto(int layer, int x, int y, char[][] c) {
        getLayer(layer).put(x, y, c);
        return this;
    }

    /**
     * Place a 2D char array c into the specified layer, using the matching Color from the given 2D Color array.
     *
     * @param layer  0 or 1 for background, 2 for foreground, 3 or higher for extra layers added on.
     * @param x      in grid cells.
     * @param y      in grid cells.
     * @param c      char[][] to be drawn in the foreground starting from x, y
     * @param colors Color[][] that should have the same width and height as c
     * @return this for chaining
     */
    public SquidLayers putInto(int layer, int x, int y, char[][] c, Color[][] colors) {
        getLayer(layer).put(x, y, c, colors);
        return this;
    }

    /**
     * Put a string at the given x, y position, using the default foreground color.
     *
     * @param x in grid cells.
     * @param y in grid cells.
     * @param s the string to print
     * @return this for chaining
     */
    public SquidLayers putString(int x, int y, String s) {
        foregroundPanel.put(x, y, s);
        return this;
    }

    /**
     * Put a string at the given x, y position, with the given foreground Color.
     *
     * @param x               in grid cells.
     * @param y               in grid cells.
     * @param s               the string to print
     * @param foreground      the Color to use
     * @return this, for chaining
     */
    public SquidLayers putString(int x, int y, String s, Color foreground) {
        foregroundPanel.put(x, y, s, foreground);
        return this;
    }

    /**
     * Put a string at the given x, y position, with the given foreground and background Colors.
     *
     * @param x          in grid cells.
     * @param y          in grid cells.
     * @param s          the string to print
     * @param foreground the Color of the string's chars
     * @param background the Color of the background of the string
     * @return this, for chaining
     */
    public SquidLayers putString(int x, int y, String s, Color foreground, Color background) {
        foregroundPanel.put(x, y, s, foreground);
        for (int i = x; i < x + s.length() && i < getTotalWidth(); i++) {
            backgroundPanel.put(i, y, background);
        }
        return this;
    }

    /**
     * A utility method that draws a 1-cell-wide black box around the text you request (as s) and replaces the contents
     * of anything that was below or adjacent to the string's new position. Useful for message boxes.
     *
     * @param x in grid cells.
     * @param y in grid cells.
     * @param s the string to print inside the box
     * @return this, for chaining
     */
    public SquidLayers putBoxedString(int x, int y, String s) {
        if (y > 0 && y + 1 < getTotalHeight() && x > 0 && x + 1 < getTotalWidth()) {
            for (int j = y - 1; j < y + 2 && j < getTotalHeight(); j++) {
                for (int i = x - 1; i < s.length() + x + 2 && i < getTotalWidth(); i++) {
                    foregroundPanel.put(i, j, ' ');
                    lightnesses[i][j] = -200;

                    backgroundPanel.put(i, j, backgroundPanel.getAt(i, j),
                            SColor.DARK_GRAY, 0f * 0.001953125f);
                }
            }
        }
        foregroundPanel.put(x, y, s, SColor.CREAM);

        return this;
    }

    /**
     * Change the lightness for the background of the cell at x, y (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     *
     * @param x         in grid cells.
     * @param y         in grid cells.
     * @param lightness int between -255 and 255 , lower numbers are darker, higher lighter.
     */
    public SquidLayers highlight(int x, int y, int lightness) {
        backgroundPanel.put(x, y, backgroundPanel.getAt(x, y),
                backgroundPanel.getColorAt(x, y),
                (clamp(lightnesses[x][y] + 256 + lightness, 1, 511)) * 0.001953125f);
        return this;
    }

    /**
     * Change the lightness for the background of the cell at x, y (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     *
     * @param x         in grid cells.
     * @param y         in grid cells.
     * @param lightness int[][] with elements between -255 and 255 , lower numbers are darker, higher lighter.
     */
    public SquidLayers highlight(int x, int y, int[][] lightness) {
        for (int i = 0; i < lightness.length && x + i < getTotalWidth(); i++) {
            for (int j = 0; j < lightness[i].length && y + j < getTotalHeight(); j++) {
                backgroundPanel.put(x, y, backgroundPanel.getAt(x, y),
                        backgroundPanel.getColorAt(x, y), (clamp(lightnesses[x + i][y + j] + 256 + lightness[i][j], 1, 511)) * 0.001953125f);
            }
        }
        return this;
    }

    /**
     * Very basic check to see if something was rendered at the x,y cell requested; this only checks the
     * foreground. If the foreground contains the character {@code ' '} at the given position or has not
     * been assigned a value at that position, then this returns false, otherwise it returns true.
     *
     * @param x in grid cells.
     * @param y in grid cells.
     * @return true if something was rendered in the foreground at the given x,y position
     */
    public boolean hasValue(int x, int y) {
        return foregroundPanel.getAt(x, y) != ' ';
    }

    /**
     * Clear one cell at position x, y of  its foreground contents.
     * <p>
     * You may be looking for the erase() method, which erases all panels and all cells.
     *
     * @param x in grid cells
     * @param y in grid cells
     * @return this for chaining
     */
    public SquidLayers clear(int x, int y) {
        foregroundPanel.clear(x, y);
        return this;
    }

    public SquidLayers eraseLayer(int layer) {
        getLayer(layer).erase();
        return this;
    }

    /**
     * Erase everything visible in all cells or all layers.  This is not at all expensive to do compared to the
     * pre-SquidLib-3.0.0 version of this method that used Swing and took a long time to erase.
     *
     * @return this, for chaining
     */
    public SquidLayers erase() {
        foregroundPanel.erase();
        backgroundPanel.erase();
        for (SquidPanel sp : extraPanels) {
            sp.erase();
        }
        return this;
    }

    public SquidLayers bump(int x, int y, int layer, Direction dir, float duration) {
        if (duration < 0)
            duration = animationDuration;
        getLayer(layer).bump(x, y, dir, duration);
        return this;
    }

    public SquidLayers bump(AnimatedEntity ae, int layer, Direction dir, float duration) {
        if (duration < 0)
            duration = animationDuration;
        getLayer(layer).bump(ae, dir, duration);
        return this;
    }

    public SquidLayers bump(int x, int y, Direction dir) {
        return bump(x, y, 2, dir, -1);
    }

    public SquidLayers bump(AnimatedEntity ae, Direction dir) {
        return bump(ae, 2, dir, -1);
    }

    public SquidLayers slide(AnimatedEntity ae, int endX, int endY, int layer, float duration) {
        if (duration < 0)
            duration = animationDuration;
        getLayer(layer).slide(ae, endX, endY, duration);
        return this;
    }

    public SquidLayers slide(int x, int y, int endX, int endY, int layer, float duration) {
        if (duration < 0)
            duration = animationDuration;
        getLayer(layer).slide(x, y, endX, endY, duration);
        return this;
    }

    public SquidLayers slide(int x, int y, int endX, int endY) {
        return slide(x, y, endX, endY, 2, -1);
    }

    public SquidLayers slide(AnimatedEntity ae, int endX, int endY) {
        return slide(ae, endX, endY, 2, -1);
    }

    public SquidLayers wiggle(int x, int y, int layer, float duration) {
        if (duration < 0)
            duration = animationDuration;
        getLayer(layer).wiggle(x, y, duration);
        return this;
    }

    public SquidLayers wiggle(AnimatedEntity ae, int layer, float duration) {
        if (duration < 0)
            duration = animationDuration;
        getLayer(layer).wiggle(ae, duration);
        return this;
    }

    public SquidLayers wiggle(int x, int y) {
        return wiggle(x, y, 2, -1);
    }

    public SquidLayers wiggle(AnimatedEntity ae) {
        return wiggle(ae, 2, -1);
    }

    public SquidLayers tint(int x, int y, Color color, int layer, float duration) {
        if (duration < 0)
            duration = animationDuration;
        getLayer(layer).tint(x, y, color, duration);
        return this;
    }

    public SquidLayers tint(AnimatedEntity ae, Color color, int layer, float duration) {
        if (duration < 0)
            duration = animationDuration;
        getLayer(layer).tint(ae, color, duration);
        return this;
    }

    public SquidLayers tint(int x, int y, Color color) {
        return tint(x, y, color, 2, -1);
    }

    public SquidLayers tint(AnimatedEntity ae, Color color) {
        return tint(ae, color, 2, -1);
    }

    public boolean hasActiveAnimations() {
        if (foregroundPanel.hasActiveAnimations())
            return true;
        if (backgroundPanel.hasActiveAnimations())
            return true;
        for (SquidPanel panel : extraPanels) {
            if (panel.hasActiveAnimations())
                return true;
        }
        return false;
    }

    public AnimatedEntity directionMarker(int x, int y, Color color, int layer, boolean doubleWidth) {
        return getLayer(layer).directionMarker(x, y, doubleWidth, color);
    }

    public AnimatedEntity directionMarker(int x, int y, Collection<Color> colors, float loopTime, int layer, boolean doubleWidth) {
        return getLayer(layer).directionMarker(x, y, doubleWidth, colors, loopTime);
    }

    public AnimatedEntity animateActor(int x, int y, char c, Color color, int layer) {
        return getLayer(layer).animateActor(x, y, c, color);
    }

    public AnimatedEntity animateActor(int x, int y, char c, Color color) {
        return foregroundPanel.animateActor(x, y, c, color);
    }

    public AnimatedEntity animateActor(int x, int y, char c, Color color, boolean doubleWidth) {
        return foregroundPanel.animateActor(x, y, doubleWidth, c, color);
    }

    public AnimatedEntity animateActor(int x, int y, char c, Collection<Color> colors, boolean doubleWidth) {
        return foregroundPanel.animateActor(x, y, doubleWidth, String.valueOf(c), colors);
    }

    public AnimatedEntity animateActor(int x, int y, char c, Collection<Color> colors, float loopTime, boolean doubleWidth) {
        return foregroundPanel.animateActor(x, y, doubleWidth, String.valueOf(c), colors, loopTime);
    }

    public AnimatedEntity animateActor(int x, int y, String s, Color color, int layer) {
        return getLayer(layer).animateActor(x, y, s, color);
    }

    public AnimatedEntity animateActor(int x, int y, String s, Color color, int layer, boolean doubleWidth) {
        return getLayer(layer).animateActor(x, y, doubleWidth, s, color);
    }

    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, Color color, int layer) {
        return getLayer(layer).animateActor(x, y, tr, color);
    }

    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, Color color, int layer, boolean doubleWidth, boolean stretch) {
        return getLayer(layer).animateActor(x, y, doubleWidth, stretch, tr, color);
    }

    public AnimatedEntity animateActor(int x, int y, String s, Color color) {
        return foregroundPanel.animateActor(x, y, s, color);
    }

    public AnimatedEntity animateActor(int x, int y, String s, Color color, boolean doubleWidth) {
        return foregroundPanel.animateActor(x, y, doubleWidth, s, color);
    }

    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, Color color) {
        return foregroundPanel.animateActor(x, y, tr, color);
    }

    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, Color color, boolean doubleWidth) {
        return foregroundPanel.animateActor(x, y, doubleWidth, tr, color);
    }

    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, Color color, boolean doubleWidth, boolean stretch) {
        return foregroundPanel.animateActor(x, y, doubleWidth, stretch, tr, color);
    }

    public AnimatedEntity animateActor(int x, int y, TextureRegion tr) {
        return animateActor(x, y, tr, Color.WHITE);
    }

    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, boolean doubleWidth) {
        return animateActor(x, y, tr, Color.WHITE, doubleWidth);
    }

    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, boolean doubleWidth, boolean stretch) {
        return animateActor(x, y, tr, Color.WHITE, doubleWidth, stretch);
    }

    public OrderedSet<AnimatedEntity> getAnimatedEntities(int layer) {
        return getLayer(layer).getAnimatedEntities();
    }

    public OrderedSet<AnimatedEntity> getAnimatedEntities() {
        return foregroundPanel.getAnimatedEntities();
    }

    public AnimatedEntity getAnimatedEntityByCell(int x, int y, int layer) {
        return getLayer(layer).getAnimatedEntityByCell(x, y);
    }

    public void removeAnimatedEntity(AnimatedEntity ae, int layer) {
        getLayer(layer).removeAnimatedEntity(ae);
    }

    public void removeAnimatedEntity(AnimatedEntity ae) {
        foregroundPanel.removeAnimatedEntity(ae);
    }

    public AnimatedEntity getAnimatedEntityByCell(int x, int y) {
        return foregroundPanel.getAnimatedEntityByCell(x, y);
    }

    public void removeAnimatedEntityByCell(int x, int y, int layer) {
        getLayer(layer).removeAnimatedEntity(getAnimatedEntityByCell(x, y, layer));
    }

    public void removeAnimatedEntityByCell(int x, int y) {
        foregroundPanel.removeAnimatedEntity(getAnimatedEntityByCell(x, y));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        //textFactory.configureShader(batch);
        super.draw(batch, parentAlpha);
    }

    public void drawActor(Batch batch, float parentAlpha, AnimatedEntity ae, int layer) {
        getLayer(layer).drawActor(batch, parentAlpha, ae);
    }

    public void drawActor(Batch batch, float parentAlpha, AnimatedEntity ae) {
        foregroundPanel.drawActor(batch, parentAlpha, ae);
    }

    public void setLightingColor(Color lightingColor) {
        backgroundPanel.setLightingColor(lightingColor);
    }

    public Color getLightingColor() {
        return backgroundPanel.getLightingColor();
    }


    /**
     * Sets the position of the actor's bottom left corner.
     *
     * @param x
     * @param y
     */
    @Override
    public void setPosition(float x, float y) {
        //super.setPosition(x, y);
        //setBounds(x, y, getWidth(), getHeight());
        foregroundPanel.setPosition(x, y);
        backgroundPanel.setPosition(x, y);
        for (SquidPanel panel : extraPanels) {
            panel.setPosition(x, y);
        }
    }

    /**
     * Gets a SquidPanel from this SquidLayers and returns a direct reference. Layer 0 is the background, layer 1 or 2
     * is the foreground, and any values 3 or higher are extra SquidPanels added via {@link #addExtraLayer()}.
     * If the layer is not found, this returns the foreground SquidPanel.
     * @param layer 0 for background, 1 or 2 for foreground, 3 for any extra layers; defaults to foreground
     * @return the corresponding SquidPanel as a direct reference
     */
    public SquidPanel getLayer(int layer) {
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        return p;
    }

    public SquidPanel getForegroundLayer() {
        return foregroundPanel;
    }

    /**
     * Sets the foreground panel to match the given SquidPanel (it can also be a subclass of SquidPanel, a likely use).
     * This does not do any validation of size, but the {@link SquidPanel#getTotalWidth()} and
     * {@link SquidPanel#getTotalHeight()} of panel should almost always match this object's {@link #getTotalWidth()}
     * and {@link #getTotalHeight()}, though there are good reasons why the cell sizes or text sizes might not match.
     * This method is probably most useful when you want a system of {@link ImageSquidPanel} layers; as long as you have
     * the reference to an ImageSquidPanel outside this class, you can call its additional methods there and have it
     * affect {@code panel} here.
     *
     * @param panel a SquidPanel, ImageSquidPanel, or other subclass of SquidPanel that should have identical size
     */
    public void setForegroundLayer(SquidPanel panel) {
        if (panel != null) {
            removeActor(foregroundPanel);
            foregroundPanel = panel;
            addActorAt(2, panel);

        }
    }

    public SquidPanel getBackgroundLayer() {
        return backgroundPanel;
    }

    /**
     * Sets the background panel to match the given SquidPanel (it can also be a subclass of SquidPanel, a likely use).
     * This does not do any validation of size, but the {@link SquidPanel#getTotalWidth()} and
     * {@link SquidPanel#getTotalHeight()} of panel should almost always match this object's {@link #getTotalWidth()}
     * and {@link #getTotalHeight()}, though there are good reasons why the cell sizes or text sizes might not match.
     * This method is probably most useful when you want a system of {@link ImageSquidPanel} layers; as long as you have
     * the reference to an ImageSquidPanel outside this class, you can call its additional methods there and have it
     * affect {@code panel} here.
     *
     * @param panel a SquidPanel, ImageSquidPanel, or other subclass of SquidPanel that should have identical size
     */
    public void setBackgroundLayer(SquidPanel panel) {

        if (panel != null) {
            removeActor(backgroundPanel);
            backgroundPanel = panel;
            addActorAt(0, panel);
        }
    }

    /**
     * Sets an extra panel to match the given SquidPanel (it can also be a subclass of SquidPanel, a likely use).
     * This tries to find the SquidPanel at the given index in the extra panels that may have been added to this with
     * {@link #addExtraLayer()}; if index is invalid because it is negative, this does nothing. If index is higher
     * than the highest index for a layer, this will simply add {@code panel} to the extra panels, possibly at an
     * earlier index (it will use the next available index, which could easily be 0 or 1). If index is valid, this sets
     * the extra panel at that index to {@code panel}, without changes.
     * This does not do any validation of size, but the {@link SquidPanel#getTotalWidth()} and
     * {@link SquidPanel#getTotalHeight()} of panel should almost always match this object's {@link #getTotalWidth()}
     * and {@link #getTotalHeight()}, though there are good reasons why the cell sizes or text sizes might not match.
     * This method is probably most useful when you want a system of {@link ImageSquidPanel} layers; as long as you have
     * the reference to an ImageSquidPanel outside this class, you can call its additional methods there and have it
     * affect {@code panel} here.
     *
     * @param panel a SquidPanel, ImageSquidPanel, or other subclass of SquidPanel that should have identical size
     * @param index the 0-based index into the extra panels list to try to assign panel to, or to append panel to
     */

    public void setExtraPanel(SquidPanel panel, int index) {
        if (index < 0 || panel == null || extraPanels == null)
            return;
        if (index - 3 >= extraPanels.size()) {
            extraPanels.add(panel);
            addActor(panel);
        } else {
            removeActor(extraPanels.get(index - 3));
            extraPanels.set(index - 3, panel);
            addActorAt(index, panel);
        }
    }


    /**
     * Sets the IColorCenter used by the foreground layer.
     *
     * @param scc an IColorCenter<Color>; commonly a SquidColorCenter with an optional filter
     */
    public void setFGColorCenter(IColorCenter<Color> scc) {
        foregroundPanel.setColorCenter(scc);
    }

    /**
     * Sets the IColorCenter used by the background layer.
     *
     * @param scc an IColorCenter<Color>; commonly a SquidColorCenter with an optional filter
     */
    public void setBGColorCenter(IColorCenter<Color> scc) {
        backgroundPanel.setColorCenter(scc);
    }

    public void setOffsets(float x, float y) {
        foregroundPanel.setOffsets(x, y);
        backgroundPanel.setOffsets(x, y);
        for (SquidPanel p : extraPanels)
            p.setOffsets(x, y);
    }

    public int getGridOffsetX() {
        return foregroundPanel.getGridOffsetX();
    }

    public int getGridOffsetY() {
        return foregroundPanel.getGridOffsetY();
    }

    public void setGridOffsetX(int offset) {
        foregroundPanel.setGridOffsetX(offset);
        backgroundPanel.setGridOffsetX(offset);
        for (SquidPanel sp : extraPanels)
            sp.setGridOffsetX(offset);
    }

    public void setGridOffsetY(int offset) {
        foregroundPanel.setGridOffsetY(offset);
        backgroundPanel.setGridOffsetY(offset);
        for (SquidPanel sp : extraPanels)
            sp.setGridOffsetY(offset);
    }

    public int getTotalWidth() {
        return foregroundPanel.getTotalWidth();
    }

    public int getTotalHeight() {
        return foregroundPanel.getTotalHeight();
    }
}
