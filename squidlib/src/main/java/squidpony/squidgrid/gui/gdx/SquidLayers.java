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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.badlogic.gdx.math.MathUtils.clamp;

/**
 * A helper class to make using multiple SquidPanels easier.
 * <br>
 * There is some useful documentation in this class' getPalette method (honestly, I don't know where else to put
 * documentation specifically about this class' default palette)..
 * Created by Tommy Ettinger on 7/6/2015.
 */
public class SquidLayers extends Group {
    protected int width;
    protected int height;
    protected int cellWidth;
    protected int cellHeight;
    protected SquidPanel backgroundPanel, foregroundPanel;
    protected int[][] lightnesses;
    protected ArrayList<SquidPanel> extraPanels;
    protected TextCellFactory textFactory;
    protected ArrayList<Color> palette;
    protected float animationDuration;

    public static final char EMPTY_CELL = ' ';
    /**
     * The pixel width of the entire map.
     *
     * @return
     */
    @Override
    public float getWidth() {
        return width * cellWidth;
    }

    /**
     * The pixel height of the entire map.
     *
     * @return
     */
    @Override
    public float getHeight() {
        return height * cellHeight;
    }

    /**
     * Width of the map in grid cells.
     *
     * @return
     */
    public int getGridWidth() {
        return width;
    }

    /**
     * Height of the map in grid cells.
     *
     * @return
     */
    public int getGridHeight() {
        return height;
    }

    /**
     * Width of one cell in pixels.
     *
     * @return
     */
    public int getCellWidth() {
        return cellWidth;
    }

    /**
     * Height of one cell in pixels.
     *
     * @return
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
     * Gets the current palette used when no other is specified.
     *
     * The palette can be customized with SquidLayers.alterPalette() and SquidLayers.extendPalette() .
     * 
     * The default palette has colors at these elements:
     * <ul>
     * <li>0: Black, also used for backgrounds if not specified</li>
     * <li>1: Off-white, used as the default foreground at times</li>
     * <li>2: Dark gray for walls</li>
     * <li>3: Silver gray for floors</li>
     * <li>4: Rust brown for doors</li>
     * <li>5: Gray-blue for water</li>
     * <li>6: Bright orange for traps</li>
     * <li>7: White</li>
     * <li>8: Light gray</li>
     * <li>9: Dark gray</li>
     * <li>10: Light red</li>
     * <li>11: Medium red</li>
     * <li>12: Dark red</li>
     * <li>13: Light orange</li>
     * <li>14: Medium orange</li>
     * <li>15: Dark orange</li>
     * <li>16: Light yellow</li>
     * <li>17: Medium yellow</li>
     * <li>18: Dark yellow</li>
     * <li>19: Light green</li>
     * <li>20: Medium green</li>
     * <li>21: Dark green</li>
     * <li>22: Light blue-green</li>
     * <li>23: Medium blue-green</li>
     * <li>24: Dark blue-green</li>
     * <li>25: Light blue</li>
     * <li>26: Medium blue</li>
     * <li>27: Dark blue</li>
     * <li>28: Light purple</li>
     * <li>29: Medium purple</li>
     * <li>30: Dark purple</li>
     * <li>31: Light pink</li>
     * <li>32: Medium pink</li>
     * <li>33: Dark pink</li>
     * <li>34: Light gray-brown</li>
     * <li>35: Medium gray-brown</li>
     * <li>36: Dark gray-brown</li>
     * <li>37: Light brown</li>
     * <li>38: Medium brown</li>
     * <li>39: Dark brown</li>
     * </ul>
     *
     * @return the current Color ArrayList used as a default palette.
     */
    public ArrayList<Color> getPalette() {
        return palette;
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
     * Create a new SquidLayers widget with the default <b>square</b> font, 40 cells wide and high, with a size of
     * 12x12 pixels for each cell.
     */
    public SquidLayers() {
        this(40, 40);
    }

    /**
     * Create a new SquidLayers widget with the default <b>square</b> font, the given number of cells for gridWidth
     * and gridHeight, and 12x12 pixels for each cell.
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
     * @param fontpath   A path to a BitmapFont that can be on the classpath (in SquidLib) or in the assets folder
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
     * @param gridWidth  in grid cells
     * @param gridHeight in grid cells
     * @param cellWidth  in pixels
     * @param cellHeight in pixels
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
     * @param gridWidth  in grid cells
     * @param gridHeight in grid cells
     * @param cellWidth  in pixels
     * @param cellHeight in pixels
     * @param fontpath   A path to a BitmapFont that can be on the classpath (in SquidLib) or in the assets folder.
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
     * @param gridWidth  in grid cells
     * @param gridHeight in grid cells
     * @param cellWidth  in pixels
     * @param cellHeight in pixels
     * @param bitmapFont A BitmapFont that you already constructed
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
     * SquidColorCenters for background and foreground. Consider using the overloads that take either a path
     * to a .fnt font file or a BitmapFont for simplicity.
     *
     * @param gridWidth  in grid cells
     * @param gridHeight in grid cells
     * @param cellWidth  in pixels
     * @param cellHeight in pixels
     * @param tcf   A TextCellFactory that will be (re-)initialized here with the given cellHeight and cellWidth.
     * @param bgColorCenter a SquidColorCenter (possibly with a filter) to use for the background
     * @param fgColorCenter a SquidColorCenter (possibly with a filter) to use for the foreground
     */
    public SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight, TextCellFactory tcf,
                       SquidColorCenter bgColorCenter, SquidColorCenter fgColorCenter) {
        this(gridWidth, gridHeight, cellWidth, cellHeight, tcf, bgColorCenter, fgColorCenter, null);
    }
    /**
     * Create a new SquidLayers widget with the given TextCellFactory, the given number of cells for gridWidth
     * and gridHeight, the size in pixels for each cell given by cellWidth and cellHeight, and the given
     * SquidColorCenters for background and foreground. Consider using the overloads that take either a path
     * to a .fnt font file or a BitmapFont for simplicity.
     *
     * @param gridWidth  in grid cells
     * @param gridHeight in grid cells
     * @param cellWidth  in pixels
     * @param cellHeight in pixels
     * @param tcf   A TextCellFactory that will be (re-)initialized here with the given cellHeight and cellWidth.
     * @param bgColorCenter a SquidColorCenter (possibly with a filter) to use for the background
     * @param fgColorCenter a SquidColorCenter (possibly with a filter) to use for the foreground
     */
    public SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight, TextCellFactory tcf,
                       SquidColorCenter bgColorCenter, SquidColorCenter fgColorCenter, char[][] actualMap) {
        initPalettes();

        width = gridWidth;
        height = gridHeight;

        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;

        textFactory = tcf.width(cellWidth).height(cellHeight).initBySize();

        if(actualMap == null || actualMap.length <= 0)
        {
            backgroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, bgColorCenter);
            foregroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, fgColorCenter);
            lightnesses = ArrayTools.fill(256, width, height);
        }
        else
        {
            backgroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, bgColorCenter, 0, 0, ArrayTools.fill(' ', actualMap.length, actualMap[0].length));
            foregroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, fgColorCenter, 0, 0, actualMap);
            lightnesses = ArrayTools.fill(256, actualMap.length, actualMap[0].length);
        }
        animationDuration = foregroundPanel.DEFAULT_ANIMATION_DURATION;

        extraPanels = new ArrayList<>();

        addActorAt(0, backgroundPanel);
        addActorAt(2, foregroundPanel);

        setSize(backgroundPanel.getWidth(), backgroundPanel.getHeight());
    }

    private void initPalettes() {
        palette = new ArrayList<>(256);
        Collections.addAll(palette, SColor.LIMITED_PALETTE);
        /*
        palette.add(SColor.PURE_DARK_GRAY);
        palette.add(SColor.CREAM);
        palette.add(SColor.FLATTERY_BROWN);
        palette.add(SColor.SILVER_GREY);
        palette.add(SColor.RUST);
        palette.add(SColor.PALE_CORNFLOWER_BLUE);
        palette.add(SColor.INTERNATIONAL_ORANGE);

        palette.add(SColor.WHITE);
        palette.add(SColor.LIGHT_GRAY);
        palette.add(SColor.DARK_GRAY);

        palette.add(SColor.RED_INCENSE);
        palette.add(SColor.RED);
        palette.add(SColor.COCHINEAL_RED);

        palette.add(SColor.PEACH_ORANGE);
        palette.add(SColor.ORANGE_PEEL);
        palette.add(SColor.TANGERINE);

        palette.add(SColor.LEMON_CHIFFON);
        palette.add(SColor.CORN);
        palette.add(SColor.GOLDEN_YELLOW);

        palette.add(SColor.TEA_GREEN);
        palette.add(SColor.LIME_GREEN);
        palette.add(SColor.GREEN_BAMBOO);

        palette.add(SColor.CYAN);
        palette.add(SColor.OCEAN_BLUE);
        palette.add(SColor.MIDORI);

        palette.add(SColor.COLUMBIA_BLUE);
        palette.add(SColor.ROYAL_BLUE);
        palette.add(SColor.PERSIAN_BLUE);

        palette.add(SColor.LAVENDER_BLUE);
        palette.add(SColor.DARK_VIOLET);
        palette.add(SColor.INDIGO);

        palette.add(SColor.CARNATION_PINK);
        palette.add(SColor.HOT_MAGENTA);
        palette.add(SColor.LIGHT_MAROON);

        palette.add(SColor.TAN);
        palette.add(SColor.DARK_TAN);
        palette.add(SColor.PALE_BROWN);

        palette.add(SColor.STEAMED_CHESTNUT);
        palette.add(SColor.DARK_CHESTNUT);
        palette.add(SColor.SAPPANWOOD_INCENSE);

         */
    }

    /**
     * Add an extra layer on top of the foreground layer. Use putInto methods to specify the layer when adding a char (0
     * is background, 1 is unused, 2 is foreground, and the first call to this method creates layer 3).
     *
     * @return this for chaining
     */
    public SquidLayers addExtraLayer() {
        SquidPanel sp;
        if(width != foregroundPanel.getTotalWidth() || height != foregroundPanel.getTotalHeight())
            sp = new SquidPanel(width, height, textFactory, foregroundPanel.getColorCenter(), 0, 0,
                    ArrayTools.fill(' ', foregroundPanel.getTotalWidth(), foregroundPanel.getTotalHeight()));
        else
            sp = new SquidPanel(width, height, textFactory);
        addActor(sp);
        extraPanels.add(sp);
        return this;
    }

    /**
     * Adds a color to the end of the default palette, then returns that palette.
     * 
     * The default palette's entries can be seen in the documentation for SquidLayers.getPalette() .
     *
     * @param color an Color to add to the palette at the end
     * @return the extended palette.
     */
    public ArrayList<Color> extendPalette(Color color) {
        palette.add(color);
        return palette;
    }

    /**
     * Changes a color at the specified index in the default palette, then returns that palette.
     * 
     * If the index is greater than or equal to the number of colors in the palette, does nothing.
     * 
     * The default palette's entries can be seen in the documentation for SquidLayers.getPalette() .
     *
     * @param index must be at least 0 and less than the length of palette (starts at length 40).
     * @param color the Color to put at the given index
     * @return the altered palette
     */
    public ArrayList<Color> alterPalette(int index, Color color) {
        if (index >= 0 && index < palette.size())
            palette.set(index, color);

        return palette;
    }

    /**
     * Sets the size of the text in the given layer  (but not the size of the cells) to the given width and height in
     * pixels (which may be stretched by viewports later on, if your program uses them).
     * @param layer the layer to affect; 0 is background, 1 is unused, 2 is foreground, 3 and higher are extra panels
     * @param wide the width of a glyph in pixels
     * @param high the height of a glyph in pixels
     * @return this for chaining
     */
    public SquidLayers setTextSize(int layer, int wide, int high)
    {
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
     * @param wide the width of a glyph in pixels
     * @param high the height of a glyph in pixels
     * @return this for chaining
     */
    public SquidLayers setTextSize(int wide, int high)
    {
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
     */
    public SquidLayers put(int x, int y, char c) {
        foregroundPanel.put(x, y, c);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color specified by an index into the default palette.
     *
     * @param x               in grid cells.
     * @param y               in grid cells.
     * @param c               a character to be drawn in the foreground
     * @param foregroundIndex int index into the default palette for the char being drawn
     */
    public SquidLayers put(int x, int y, char c, int foregroundIndex) {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color specified by an index into the default palette.
     *
     * @param x               in grid cells.
     * @param y               in grid cells.
     * @param c               a character to be drawn in the foreground
     * @param foreground    Color for the char being drawn
     */
    public SquidLayers put(int x, int y, char c, Color foreground) {
        foregroundPanel.put(x, y, c, foreground);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color specified by an index into the default palette, and a
     * background color specified in the same way.
     *
     * @param x               in grid cells.
     * @param y               in grid cells.
     * @param c               a character to be drawn in the foreground
     * @param foregroundIndex int index into the default palette for the char being drawn
     * @param backgroundIndex int index into the default palette for the background
     */
    public SquidLayers put(int x, int y, char c, int foregroundIndex, int backgroundIndex) {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        backgroundPanel.put(x, y, backgroundIndex, palette);
        return this;
    }
    /**
     * Place a char c into the foreground, with a foreground color specified by an index into the default palette, and a
     * background color specified in the same way.
     *
     * @param x               in grid cells.
     * @param y               in grid cells.
     * @param c               a character to be drawn in the foreground
     * @param foreground    Color for the char being drawn
     * @param background    Color for the background
     */
    public SquidLayers put(int x, int y, char c, Color foreground, Color background) {
        foregroundPanel.put(x, y, c, foreground);
        backgroundPanel.put(x, y, background);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color specified by an index into the default palette, a
     * background color specified in the same way, and a lightness variation for the background (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param c                   a character to be drawn in the foreground
     * @param foregroundIndex     int index into the default palette for the char being drawn
     * @param backgroundIndex     int index into the default palette for the background
     * @param backgroundLightness int between -255 and 255 , lower numbers are darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char c, int foregroundIndex, int backgroundIndex, int backgroundLightness) {
        backgroundLightness = clamp(backgroundLightness, -255, 255);
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        lightnesses[x][y] = 256 + clamp(backgroundLightness, -255, 255);
        backgroundPanel.put(x, y, palette.get(backgroundIndex), lightnesses[x][y] * 0.001953125f);
        return this;
    }
    /**
     * Place a char c into the foreground, with a foreground color specified by an index into alternatePalette, a
     * background color specified in the same way, and a lightness variation for the background (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param c                   a character to be drawn in the foreground
     * @param alternatePalette    an alternate Color ArrayList for both foreground and background
     * @param foregroundIndex     int index into alternatePalette for the char being drawn
     * @param backgroundIndex     int index into alternatePalette for the background
     * @param backgroundLightness int between -255 and 255 , lower numbers are darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char c, ArrayList<Color> alternatePalette, int foregroundIndex, int backgroundIndex, int backgroundLightness) {
        foregroundPanel.put(x, y, c, foregroundIndex, alternatePalette);
        lightnesses[x][y] = 256 + clamp(backgroundLightness, -255, 255);

        backgroundPanel.put(x, y, alternatePalette.get(backgroundIndex), lightnesses[x][y] * 0.001953125f);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground and background libGDX Color and a lightness variation for
     * the background (0 is no change, 255 will nearly double the brightness (capping at white), -255 will reduce the
     * color to nearly black (for most colors, all the way to black), and values in between will be proportional.
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param c                   a character to be drawn in the foreground
     * @param foreground            Color for the char being drawn
     * @param background            Color for the background
     * @param backgroundLightness int between -255 and 255 , lower numbers are darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char c, Color foreground, Color background, int backgroundLightness) {
        foregroundPanel.put(x, y, c, foreground);
        lightnesses[x][y] = 256 + clamp(backgroundLightness, -255, 255);

        backgroundPanel.put(x, y, background, lightnesses[x][y] * 0.001953125f);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color specified by an index into alternatePalette, a
     * background color specified in the same way, and a lightness variation for the background (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param c                   a character to be drawn in the foreground
     * @param foregroundIndex     int index into alternatePalette for the char being drawn
     * @param fgPalette           an alternate Color ArrayList for the foreground; can be null to use the default.
     * @param backgroundIndex     int index into alternatePalette for the background
     * @param bgPalette           an alternate Color ArrayList for the background; can be null to use the default.
     * @param backgroundLightness int between -255 and 255 , lower numbers are darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char c, int foregroundIndex, ArrayList<Color> fgPalette, int backgroundIndex, ArrayList<Color> bgPalette, int backgroundLightness) {
        if (fgPalette == null) fgPalette = palette;
        if (bgPalette == null) bgPalette = palette;
        foregroundPanel.put(x, y, c, foregroundIndex, fgPalette);
        lightnesses[x][y] = 256 + clamp(backgroundLightness, -255, 255);

        backgroundPanel.put(x, y, bgPalette.get(backgroundIndex), lightnesses[x][y] * 0.001953125f);
        return this;
    }


    public SquidLayers put(int x, int y, char[][] c) {
        foregroundPanel.put(x, y, c);
        return this;
    }

    public SquidLayers put(int x, int y, char[][] c, int[][] foregroundIndex) {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        return this;
    }

    public SquidLayers put(int x, int y, char c[][], int[][] foregroundIndex, int[][] backgroundIndex) {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        backgroundPanel.put(x, y, backgroundIndex, palette);
        return this;
    }

    /**
     * Place a char[][] c into the foreground, with a foreground color specified by an index into alternatePalette, a
     * background color specified in the same way, and a lightness variation for the background (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param c                   char[][] to be drawn in the foreground starting from x, y
     * @param foregroundIndex     int[][] of indices into the default palette for the char being drawn
     * @param backgroundIndex     int[][] of indices into the default palette for the background
     * @param backgroundLightness int[][] with elements between -255 and 255 , lower darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char[][] c, int[][] foregroundIndex, int[][] backgroundIndex, int[][] backgroundLightness) {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        for (int i = x; i < getTotalWidth() && i-x < backgroundLightness.length; i++) {
            for (int j = y; j < getTotalHeight() && j - y < backgroundLightness[i].length; j++) {
                lightnesses[i][j] = 256 + clamp(backgroundLightness[i-x][j-y], -255, 255);
                backgroundPanel.put(i, j, palette.get(backgroundIndex[i-x][j-y]), lightnesses[i][j] * 0.001953125f);

            }
        }
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color specified by an index into alternatePalette, a
     * background color specified in the same way, and a lightness variation for the background (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param c                   char[][] to be drawn in the foreground starting from x, y
     * @param alternatePalette    an alternate Color ArrayList for both foreground and background
     * @param foregroundIndex     int[][] of indices into alternatePalette for the char being drawn
     * @param backgroundIndex     int[][] of indices into alternatePalette for the background
     * @param backgroundLightness int[][] with elements between -255 and 255 , lower darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char[][] c, ArrayList<Color> alternatePalette, int[][] foregroundIndex, int[][] backgroundIndex, int[][] backgroundLightness) {

        if (alternatePalette == null) alternatePalette = palette;
        foregroundPanel.put(x, y, c, foregroundIndex, alternatePalette);
        for (int i = x; i < getTotalWidth() && i - x < backgroundLightness.length; i++) {
            for (int j = y; j < getTotalHeight() && j - y < backgroundLightness[i].length; j++) {
                lightnesses[i][j] = 256 + clamp(backgroundLightness[i-x][j-y], -255, 255);
                backgroundPanel.put(i, j, alternatePalette.get(backgroundIndex[i-x][j-y]), lightnesses[i][j] * 0.001953125f);

            }
        }
        return this;
    }
    /**
     * Place a char c into the foreground, with a foreground color specified by an index into alternatePalette, a
     * background color specified in the same way, and a lightness variation for the background (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param c                   char[][] to be drawn in the foreground starting from x, y
     * @param foregrounds     int[][] of indices into alternatePalette for the char being drawn
     * @param backgrounds     int[][] of indices into alternatePalette for the background
     * @param backgroundLightness int[][] with elements between -255 and 255 , lower darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char[][] c, Color[][] foregrounds, Color[][] backgrounds, int[][] backgroundLightness) {

        foregroundPanel.put(x, y, c, foregrounds);
        for (int i = x; i < getTotalWidth() && i - x < backgroundLightness.length; i++) {
            for (int j = y; j < getTotalHeight() && j - y < backgroundLightness[i].length; j++) {
                lightnesses[i][j] = 256 + clamp(backgroundLightness[i-x][j-y], -255, 255);
                backgroundPanel.put(i, j, backgrounds[i-x][j-y], lightnesses[i][j] * 0.001953125f);
            }
        }
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color specified by an index into alternatePalette, a
     * background color specified in the same way, and a lightness variation for the background (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param c                   char[][] to be drawn in the foreground starting from x, y
     * @param foregroundIndex     int[][] of indices into fgPalette for the char being drawn
     * @param fgPalette           an alternate Color ArrayList for the foreground; can be null to use the default.
     * @param backgroundIndex     int[][] of indices into bgPalette for the background
     * @param bgPalette           an alternate Color ArrayList for the background; can be null to use the default.
     * @param backgroundLightness int[][] with elements between -255 and 255 , lower darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char[][] c, int[][] foregroundIndex, ArrayList<Color> fgPalette, int[][] backgroundIndex, ArrayList<Color> bgPalette, int[][] backgroundLightness) {
        if (fgPalette == null) fgPalette = palette;
        if (bgPalette == null) bgPalette = palette;
        foregroundPanel.put(x, y, c, foregroundIndex, fgPalette);
        for (int i = x; i < getTotalWidth() && i - x < backgroundLightness.length; i++) {
            for (int j = y; j < getTotalHeight() && j - y < backgroundLightness[i].length; j++) {
                lightnesses[i][j] = 256 + clamp(backgroundLightness[i-x][j-y], -255, 255);
                backgroundPanel.put(i, j, bgPalette.get(backgroundIndex[i-x][j-y]), lightnesses[i-x][j-y] * 0.001953125f);

            }
        }
        return this;
    }

    /**
     * Place a char c into the specified layer, with a color specified by an index into alternatePalette.
     *
     * @param layer 0 or 1 for background, 2 for foreground, 3 or higher for extra layers added on.
     * @param x     in grid cells.
     * @param y     in grid cells.
     * @param c     char to be drawn in the foreground at x, y
     */
    public SquidLayers putInto(int layer, int x, int y, char c) {
        getLayer(layer).put(x, y, c);
        return this;
    }

    /**
     * Place a char c into the specified layer, with a color specified by an index into the default palette.
     *
     * @param layer      0 or 1 for background, 2 for foreground, 3 or higher for extra layers added on.
     * @param x          in grid cells.
     * @param y          in grid cells.
     * @param c          char to be drawn in the foreground at x, y
     * @param colorIndex int index into alternatePalette for the char being drawn
     */
    public SquidLayers putInto(int layer, int x, int y, char c, int colorIndex) {
        getLayer(layer).put(x, y, c, colorIndex, palette);
        return this;
    }

    /**
     * Place a char c into the specified layer, with a color specified by an index into alternatePalette.
     *
     * @param layer            0 or 1 for background, 2 for foreground, 3 or higher for extra layers added on.
     * @param x                in grid cells.
     * @param y                in grid cells.
     * @param c                char to be drawn in the foreground at x, y
     * @param alternatePalette an alternate Color ArrayList for both foreground and background
     * @param colorIndex       int index into alternatePalette for the char being drawn
     */
    public SquidLayers putInto(int layer, int x, int y, char c, ArrayList<Color> alternatePalette, int colorIndex) {
        if (alternatePalette == null) alternatePalette = palette;
        getLayer(layer).put(x, y, c, colorIndex, alternatePalette);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color specified by an index into the default palette.
     *
     * @param layer   the layer to draw into
     * @param x       in grid cells.
     * @param y       in grid cells.
     * @param c       a character to be drawn in the specified layer
     * @param color   Color for the char being drawn
     */
    public SquidLayers putInto(int layer, int x, int y, char c, Color color) {
        getLayer(layer).put(x, y, c, color);
        return this;
    }
    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into alternatePalette.
     *
     * @param layer 0 or 1 for background, 2 for foreground, 3 or higher for extra layers added on.
     * @param x     in grid cells.
     * @param y     in grid cells.
     * @param c     char[][] to be drawn in the foreground starting from x, y
     */
    public SquidLayers putInto(int layer, int x, int y, char[][] c) {
        getLayer(layer).put(x, y, c);
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into the default palette.
     *
     * @param layer      0 or 1 for background, 2 for foreground, 3 or higher for extra layers added on.
     * @param x          in grid cells.
     * @param y          in grid cells.
     * @param c          char[][] to be drawn in the foreground starting from x, y
     * @param colorIndex int[][] of indices into alternatePalette for the char being drawn
     */
    public SquidLayers putInto(int layer, int x, int y, char[][] c, int[][] colorIndex) {
        getLayer(layer).put(x, y, c, colorIndex, palette);
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into alternatePalette.
     *
     * @param layer            0 or 1 for background, 2 for foreground, 3 or higher for extra layers added on.
     * @param x                in grid cells.
     * @param y                in grid cells.
     * @param c                char[][] to be drawn in the foreground starting from x, y
     * @param alternatePalette an alternate Color ArrayList for both foreground and background
     * @param colorIndex       int[][] of indices into alternatePalette for the char being drawn
     */
    public SquidLayers putInto(int layer, int x, int y, char[][] c, ArrayList<Color> alternatePalette, int[][] colorIndex) {
        if (alternatePalette == null) alternatePalette = palette;
        getLayer(layer).put(x, y, c, colorIndex, alternatePalette);
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into alternatePalette.
     *
     * @param layer            0 or 1 for background, 2 for foreground, 3 or higher for extra layers added on.
     * @param x                in grid cells.
     * @param y                in grid cells.
     * @param c                char[][] to be drawn in the foreground starting from x, y
     * @param colors          int[][] of indices into alternatePalette for the char being drawn
     */
    public SquidLayers putInto(int layer, int x, int y, char[][] c, Color[][] colors) {
        getLayer(layer).put(x, y, c, colors);
        return this;
    }

    /**
     * Put a string at the given x, y position, using the default color.
     *
     * @param x in grid cells.
     * @param y in grid cells.
     * @param s the string to print
     * @return this, for chaining
     */
    public SquidLayers putString(int x, int y, String s) {
        foregroundPanel.put(x, y, s);
        return this;
    }

    /**
     * Put a string at the given x, y position, with the given index for foreground color that gets looked up in the
     * default palette.
     *
     * @param x               in grid cells.
     * @param y               in grid cells.
     * @param s               the string to print
     * @param foregroundIndex the indexed color to use
     * @return this, for chaining
     */
    public SquidLayers putString(int x, int y, String s, int foregroundIndex) {
        foregroundPanel.put(x, y, s, palette.get(foregroundIndex));
        return this;
    }

    /**
     * Put a string at the given x, y position, with the given indices for foreground and background color that look up
     * their index in the default palette.
     *
     * @param x               in grid cells.
     * @param y               in grid cells.
     * @param s               the string to print
     * @param foregroundIndex the indexed color to use
     * @param backgroundIndex the indexed color to use
     * @return this, for chaining
     */
    public SquidLayers putString(int x, int y, String s, int foregroundIndex, int backgroundIndex) {
        foregroundPanel.put(x, y, s, palette.get(foregroundIndex));
        for (int i = x; i < s.length() && i < getTotalWidth(); i++) {
            backgroundPanel.put(i, y, palette.get(backgroundIndex));
        }
        return this;
    }

    /**
     * Put a string at the given x, y position, with the given indices for foreground and background color that look up
     * their index in alternatePalette.
     *
     * @param x                in grid cells.
     * @param y                in grid cells.
     * @param s                the string to print
     * @param alternatePalette the colors this can use, where the indices are used instead of individual colors
     * @param foregroundIndex the indexed color to use
     * @param backgroundIndex the indexed color to use
     * @return this, for chaining
     */
    public SquidLayers putString(int x, int y, String s, ArrayList<Color> alternatePalette, int foregroundIndex, int backgroundIndex) {
        foregroundPanel.put(x, y, s, alternatePalette.get(foregroundIndex));
        for (int i = x; i < s.length() && i < getTotalWidth(); i++) {
            backgroundPanel.put(i, y, alternatePalette.get(backgroundIndex));
        }
        return this;
    }
    /**
     * Put a string at the given x, y position, with the given indices for foreground and background color that look up
     * their index in alternatePalette.
     *
     * @param x                in grid cells.
     * @param y                in grid cells.
     * @param s                the string to print
     * @param foreground      the Color of the string's chars
     * @param background      the Color of the background of the string
     * @return this, for chaining
     */
    public SquidLayers putString(int x, int y, String s, Color foreground, Color background) {
        foregroundPanel.put(x, y, s, foreground);
        for (int i = x; i < s.length() && i < getTotalWidth(); i++) {
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
                    lightnesses[i][j] = -255;

                    backgroundPanel.put(i, j, backgroundPanel.getAt(i, j),
                            palette.get(9), 0f * 0.001953125f);
                }
            }
        }
        foregroundPanel.put(x, y, s, palette.get(1));

        return this;
    }

    /**
     * Change the lightness for the background of the cell at x, y (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param lightness int between -255 and 255 , lower numbers are darker, higher lighter.
     */
    public SquidLayers highlight(int x, int y, int lightness) {
        lightnesses[x][y] = 256 + clamp(lightness, -255, 255);

        backgroundPanel.put(x, y, backgroundPanel.getAt(x, y),
                backgroundPanel.getColorAt(x, y), lightnesses[x][y] * 0.001953125f);
        return this;
    }
    /**
     * Change the lightness for the background of the cell at x, y (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     *
     * @param x                   in grid cells.
     * @param y                   in grid cells.
     * @param lightness int[][] with elements between -255 and 255 , lower numbers are darker, higher lighter.
     */
    public SquidLayers highlight(int x, int y, int[][] lightness) {
        for (int i = 0; i < lightness.length && x + i < getTotalWidth(); i++) {
            for (int j = 0; j < lightness[i].length && y + j < getTotalHeight(); j++) {
                lightnesses[x+i][y+j] = 256 + clamp(lightness[i][j], -255, 255);;
                backgroundPanel.put(x, y, backgroundPanel.getAt(x, y),
                        backgroundPanel.getColorAt(x, y), lightnesses[i][j] * 0.001953125f);
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
     * 
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

    public SquidLayers eraseLayer(int layer)
    {
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

    public AnimatedEntity animateActor(int x, int y, char c, int index, ArrayList<Color> palette, int layer) {
        return animateActor(x, y, c, palette.get(index), layer);
    }
    public AnimatedEntity animateActor(int x, int y, char c, int index, ArrayList<Color> palette) {
        return animateActor(x, y, c, palette.get(index));
    }

    public AnimatedEntity animateActor(int x, int y, char c, int index, int layer) {
        return animateActor(x, y, c, palette.get(index), layer);
    }
    public AnimatedEntity animateActor(int x, int y, char c, int index) {
        return animateActor(x, y, c, palette.get(index));
    }
    public AnimatedEntity animateActor(int x, int y, char c, int index, boolean doubleWidth) {
        return animateActor(x, y, c, palette.get(index), doubleWidth);
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

    public AnimatedEntity animateActor(int x, int y, String s, int index, ArrayList<Color> palette, int layer) {
        return animateActor(x, y, s, palette.get(index), layer);
    }
    public AnimatedEntity animateActor(int x, int y, String s, int index, ArrayList<Color> palette) {
        return animateActor(x, y, s, palette.get(index));
    }

    public AnimatedEntity animateActor(int x, int y, String s, int index, int layer) {
        return animateActor(x, y, s, palette.get(index), layer);
    }
    public AnimatedEntity animateActor(int x, int y, String s, int index) {
        return animateActor(x, y, s, palette.get(index));
    }
    public AnimatedEntity animateActor(int x, int y, String s, int index, boolean doubleWidth) {
        return animateActor(x, y, s, palette.get(index), doubleWidth);
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

    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, int index, ArrayList<Color> palette, int layer) {
        return animateActor(x, y, tr, palette.get(index), layer);
    }
    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, int index, ArrayList<Color> palette, int layer, boolean doubleWidth) {
        return animateActor(x, y, tr, palette.get(index), layer, doubleWidth, true);
    }
    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, int index, ArrayList<Color> palette, int layer, boolean doubleWidth, boolean stretch) {
        return animateActor(x, y, tr, palette.get(index), layer, doubleWidth, stretch);
    }
    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, int index, ArrayList<Color> palette) {
        return animateActor(x, y, tr, palette.get(index));
    }
    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, int index, int layer) {
        return animateActor(x, y, tr, palette.get(index), layer);
    }
    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, int index) {
        return animateActor(x, y, tr, palette.get(index));
    }
    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, int index, boolean doubleWidth) {
        return animateActor(x, y, tr, palette.get(index), doubleWidth);
    }
    public AnimatedEntity animateActor(int x, int y, TextureRegion tr, int index, boolean doubleWidth, boolean stretch) {
        return animateActor(x, y, tr, palette.get(index), doubleWidth, stretch);
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
    public void removeAnimatedEntity(AnimatedEntity ae, int layer)
    {
        getLayer(layer).removeAnimatedEntity(ae);
    }
    public void removeAnimatedEntity(AnimatedEntity ae)
    {
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

    public void drawActor(Batch batch, float parentAlpha, AnimatedEntity ae, int layer)
    {
        getLayer(layer).drawActor(batch, parentAlpha, ae);
    }
    public void drawActor(Batch batch, float parentAlpha, AnimatedEntity ae)
    {
        foregroundPanel.drawActor(batch, parentAlpha, ae);
    }
    public void setLightingColor(Color lightingColor)
    {
        backgroundPanel.setLightingColor(lightingColor);
    }
    public Color getLightingColor()
    {
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
        super.setPosition(x, y);
        setBounds(x, y, getWidth(), getHeight());
        foregroundPanel.setPosition(x, y);
        backgroundPanel.setPosition(x, y);
        for(SquidPanel panel : extraPanels)
        {
            panel.setPosition(x, y);
        }
    }

    public SquidPanel getLayer(int layer)
    {
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
    public SquidPanel getForegroundLayer()
    {
        return foregroundPanel;
    }
    public SquidPanel getBackgroundLayer()
    {
        return backgroundPanel;
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
    public void setOffsets(float x, float y)
    {
        foregroundPanel.setOffsets(x, y);
        backgroundPanel.setOffsets(x, y);
        for(SquidPanel p : extraPanels)
            p.setOffsets(x, y);
    }
    public int getGridOffsetX()
    {
        return foregroundPanel.getGridOffsetX();
    }
    public int getGridOffsetY()
    {
        return foregroundPanel.getGridOffsetY();
    }
    public void setGridOffsetX (int offset)
    {
        foregroundPanel.setGridOffsetX(offset);
        backgroundPanel.setGridOffsetX(offset);
        for(SquidPanel sp : extraPanels)
            sp.setGridOffsetX(offset);
    }
    public void setGridOffsetY (int offset)
    {
        foregroundPanel.setGridOffsetY(offset);
        backgroundPanel.setGridOffsetY(offset);
        for(SquidPanel sp : extraPanels)
            sp.setGridOffsetY(offset);
    }

    public int getTotalWidth()
    {
        return foregroundPanel.getTotalWidth();
    }
    public int getTotalHeight()
    {
        return foregroundPanel.getTotalHeight();
    }
}
