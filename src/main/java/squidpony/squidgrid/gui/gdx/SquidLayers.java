package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import squidpony.Colors;
import squidpony.annotation.Beta;
import com.badlogic.gdx.graphics.Color;
import squidpony.squidgrid.Direction;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * A helper class to make using multiple SquidPanels easier.
 * There is some useful documentation in this class' getPalette method (honestly, I don't know where else to put
 * documentation specifically about this class' default palette). Notably, it uses libGDX Color objects instead of the
 * AWT Color objects used elsewhere, and a convenience method is provided to convert from AWT to GDX, awtColorToGDX().
 * Created by Tommy Ettinger on 7/6/2015.
 */
@Beta
public class SquidLayers extends Group {
    protected int width;
    protected int height;
    protected int cellWidth;
    protected int cellHeight;
    protected SquidPanel backgroundPanel, lightnessPanel, foregroundPanel;
    protected int[][] bgIndices;
    protected int[][] lightnesses;
    protected ArrayList<SquidPanel> extraPanels;
    protected TextCellFactory textFactory;
    protected ArrayList<Color> palette, lightingPalette;
    protected boolean[][] values;
    protected float animationDuration;

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
     * Get all the background color indices at once, which are probably indexed into the default palette. See the
     * documentation for the getPalette() method in this class for more information.
     *
     * @return
     */
    public int[][] getBgIndices() {
        return bgIndices;
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
     * Set all of the background colors at once, using indexes into what is probably the default palette unless
     * otherwise specified in the call to put(). If you want subtle variations on a color, don't add a new entry to
     * the default palette every time if you can set the lightness differently for things with that color. Indices
     * should correspond to the numbers in getPalette()'s documentation, or higher if you extended it.
     *
     * @param bgIndices 2D array, width and height should match this class' gridWidth and gridHeight.
     */
    public void setBgIndices(int[][] bgIndices) {
        this.bgIndices = bgIndices;
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
        super();
        initPalettes();
        width = gridWidth;
        height = gridHeight;

        cellWidth = 12;
        cellHeight = 12;

        bgIndices = new int[width][height];
        lightnesses = new int[width][height];
        values = new boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                lightnesses[x][y] = 256;
            }
        }

        textFactory = new TextCellFactory().defaultSquareFont().width(12).height(12).initBySize();

        backgroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory);
        lightnessPanel = new SquidPanel(gridWidth, gridHeight, textFactory);
        foregroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory);
        animationDuration = foregroundPanel.DEFAULT_ANIMATION_DURATION;

        extraPanels = new ArrayList<SquidPanel>();

        super.addActorAt(0, backgroundPanel);
        super.addActorAt(1, lightnessPanel);
        super.addActorAt(2, foregroundPanel);

        this.setSize(backgroundPanel.getWidth(), backgroundPanel.getHeight());
    }

    /**
     * Create a new SquidLayers widget with a default font (it will be square if cellWidth and cellHeight are equal, or
     * narrow otherwise), the given number of cells for gridWidth
     * and gridHeight, and the size in pixels for each cell given by cellWidth and cellHeight.
     *
     * @param gridWidth  in grid cells
     * @param gridHeight in grid cells
     * @param cellWidth  in pixels
     * @param cellHeight in pixels
     */
    public SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight) {
        super();
        initPalettes();
        width = gridWidth;
        height = gridHeight;

        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;

        bgIndices = new int[width][height];
        lightnesses = new int[width][height];
        values = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                lightnesses[x][y] = 256;
            }
        }

        textFactory = new TextCellFactory();
        if (cellHeight == cellWidth) {
            textFactory = textFactory.defaultSquareFont();
        } else {
            textFactory = textFactory.defaultNarrowFont();
        }
        textFactory = textFactory.width(cellWidth).height(cellHeight).initBySize();
        backgroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory);
        lightnessPanel = new SquidPanel(gridWidth, gridHeight, textFactory);
        foregroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory);
        animationDuration = foregroundPanel.DEFAULT_ANIMATION_DURATION;

        extraPanels = new ArrayList<SquidPanel>();

        super.addActorAt(0, backgroundPanel);
        super.addActorAt(1, lightnessPanel);
        super.addActorAt(2, foregroundPanel);

        this.setSize(backgroundPanel.getWidth(), backgroundPanel.getHeight());
    }

    /**
     * Create a new SquidLayers widget with the given path to a Font file, the given number of cells for gridWidth
     * and gridHeight, and the size in pixels for each cell given by cellWidth and cellHeight.
     *
     * @param gridWidth  in grid cells
     * @param gridHeight in grid cells
     * @param cellWidth  in pixels
     * @param cellHeight in pixels
     * @param fontpath   A Font that should have been assigned a size before being passed here.
     */
    public SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight, String fontpath) {
        super();
        initPalettes();

        width = gridWidth;
        height = gridHeight;

        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;

        bgIndices = new int[width][height];
        lightnesses = new int[width][height];
        values = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                lightnesses[x][y] = 256;
            }
        }

        textFactory = new TextCellFactory().font(fontpath).width(cellWidth).height(cellHeight).initBySize();

        backgroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory);
        lightnessPanel = new SquidPanel(gridWidth, gridHeight, textFactory);
        foregroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory);
        animationDuration = foregroundPanel.DEFAULT_ANIMATION_DURATION;

        extraPanels = new ArrayList<SquidPanel>();

        super.addActorAt(0, backgroundPanel);
        super.addActorAt(1, lightnessPanel);
        super.addActorAt(2, foregroundPanel);

        this.setSize(backgroundPanel.getWidth(), backgroundPanel.getHeight());
    }

    public static Color awtColorToGDX(java.awt.Color original) {
        return new Color(original.getRed() / 255f, original.getGreen() / 255f, original.getBlue() / 255f, original.getAlpha() / 255f);
    }

    private void initPalettes() {
        palette = new ArrayList<Color>(256);
        palette.add(awtColorToGDX(Colors.DARK_SLATE_GRAY));
        palette.add(awtColorToGDX(Colors.CREAM));
        palette.add(awtColorToGDX(Colors.FLATTERY_BROWN));
        palette.add(awtColorToGDX(Colors.SILVER_GREY));
        palette.add(awtColorToGDX(Colors.RUST));
        palette.add(awtColorToGDX(Colors.WATER));
        palette.add(awtColorToGDX(Colors.INTERNATIONAL_ORANGE));

        palette.add(awtColorToGDX(Colors.WHITE));
        palette.add(awtColorToGDX(Colors.LIGHT_GRAY));
        palette.add(awtColorToGDX(Colors.DARK_GRAY));

        palette.add(awtColorToGDX(Colors.RED_INCENSE));
        palette.add(awtColorToGDX(Colors.RED));
        palette.add(awtColorToGDX(Colors.COCHINEAL_RED));

        palette.add(awtColorToGDX(Colors.PEACH_ORANGE));
        palette.add(awtColorToGDX(Colors.ORANGE_PEEL));
        palette.add(awtColorToGDX(Colors.TANGERINE));

        palette.add(awtColorToGDX(Colors.LEMON_CHIFFON));
        palette.add(awtColorToGDX(Colors.CORN));
        palette.add(awtColorToGDX(Colors.GOLDEN_YELLOW));

        palette.add(awtColorToGDX(Colors.TEA_GREEN));
        palette.add(awtColorToGDX(Colors.LIME_GREEN));
        palette.add(awtColorToGDX(Colors.PINE_GREEN));

        palette.add(awtColorToGDX(Colors.BABY_BLUE));
        palette.add(awtColorToGDX(Colors.CYAN));
        palette.add(awtColorToGDX(Colors.BLUE_GREEN));

        palette.add(awtColorToGDX(Colors.COLUMBIA_BLUE));
        palette.add(awtColorToGDX(Colors.ROYAL_BLUE));
        palette.add(awtColorToGDX(Colors.PERSIAN_BLUE));

        palette.add(awtColorToGDX(Colors.LAVENDER_BLUE));
        palette.add(awtColorToGDX(Colors.THIN_VIOLET));
        palette.add(awtColorToGDX(Colors.DARK_VIOLET));

        palette.add(awtColorToGDX(Colors.CARNATION_PINK));
        palette.add(awtColorToGDX(Colors.HOT_MAGENTA));
        palette.add(awtColorToGDX(Colors.LIGHT_MAROON));

        palette.add(awtColorToGDX(Colors.TAN));
        palette.add(awtColorToGDX(Colors.DARK_TAN));
        palette.add(awtColorToGDX(Colors.PALE_BROWN));

        palette.add(awtColorToGDX(Colors.STEAMED_CHESTNUT));
        palette.add(awtColorToGDX(Colors.DARK_CHESTNUT));
        palette.add(awtColorToGDX(Colors.SAPPANWOOD_INCENSE));

        lightingPalette = new ArrayList<Color>(512);
        for (int i = 0; i < 512; i++) {
            lightingPalette.add(Color.CLEAR);
        }
        for (int i = 1; i < 256; i++) {
            lightingPalette.set(256 + i, new Color(1.0f, 0xFD / 255f, 0xD8 / 255f, i / 255f));
            lightingPalette.set(256 - i, new Color(0, 0, 0, i / 255f));
        }

    }

    /**
     * Add an extra layer on top of the foreground layer. Use putInto methods to specify the layer when adding a char (0
     * is background, 1 is lightness modifiers, 2 is foreground, and the first call to this method creates layer 3).
     *
     * @return
     */
    public SquidLayers addExtraLayer() {
        SquidPanel sp = new SquidPanel(width, height, textFactory);
        super.addActor(sp);
        extraPanels.add(sp);
        return this;
    }

    /**
     * Adds a color to the end of the default palette, then returns that palette.
     * 
     * The default palette's entries can be seen in the documentation for SquidLayers.getPalette() .
     *
     * @param color
     * @return
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
     * @param color
     * @return
     */
    public ArrayList<Color> alterPalette(int index, Color color) {
        if (index >= 0 && index < palette.size())
            palette.set(index, color);

        return palette;
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
        values[x][y] = true;
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
        values[x][y] = true;
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
        values[x][y] = true;
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
        values[x][y] = true;
        lightnesses[x][y] = 256 + backgroundLightness;

        lightnessPanel.put(x, y, 256 + backgroundLightness, lightingPalette);
        backgroundPanel.put(x, y, backgroundIndex, palette);
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
        backgroundLightness = clamp(backgroundLightness, -255, 255);
        foregroundPanel.put(x, y, c, foregroundIndex, alternatePalette);
        values[x][y] = true;
        lightnesses[x][y] = 256 + backgroundLightness;

        lightnessPanel.put(x, y, 256 + backgroundLightness, lightingPalette);
        backgroundPanel.put(x, y, backgroundIndex, alternatePalette);
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
        backgroundLightness = clamp(backgroundLightness, -255, 255);
        if (fgPalette == null) fgPalette = palette;
        if (bgPalette == null) bgPalette = palette;
        foregroundPanel.put(x, y, c, foregroundIndex, fgPalette);
        values[x][y] = true;
        lightnesses[x][y] = 256 + backgroundLightness;

        lightnessPanel.put(x, y, 256 + backgroundLightness, lightingPalette);
        backgroundPanel.put(x, y, backgroundIndex, bgPalette);
        return this;
    }


    public SquidLayers put(int x, int y, char[][] c) {
        foregroundPanel.put(x, y, c);
        for (int i = x; i < c.length && i < width; i++) {
            for (int j = y; j < c[i].length && j < height; j++) {
                values[i][j] = true;
            }
        }
        return this;
    }

    public SquidLayers put(int x, int y, char[][] c, int[][] foregroundIndex) {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        for (int i = x; i < c.length && i < width; i++) {
            for (int j = y; j < c[i].length && j < height; j++) {
                values[i][j] = true;
            }
        }
        return this;
    }

    public SquidLayers put(int x, int y, char c[][], int[][] foregroundIndex, int[][] backgroundIndex) {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        for (int i = x; i < c.length && i < width; i++) {
            for (int j = y; j < c[i].length && j < height; j++) {
                values[i][j] = true;
            }
        }
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
        for (int i = x; i < width && i < backgroundLightness.length; i++) {
            for (int j = y; j < height && j < backgroundLightness[i].length; j++) {
                lightnesses[i][j] = 256 + clamp(backgroundLightness[i][j], -255, 255);
                values[i][j] = true;
            }
        }
        lightnessPanel.put(0, 0, lightnesses, lightingPalette);
        backgroundPanel.put(x, y, backgroundIndex, palette);
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
        for (int i = x; i < width && i < backgroundLightness.length; i++) {
            for (int j = y; j < height && j < backgroundLightness[i].length; j++) {
                lightnesses[i][j] = 256 + clamp(backgroundLightness[i][j], -255, 255);
                values[i][j] = true;
            }
        }
        lightnessPanel.put(0, 0, lightnesses, lightingPalette);
        backgroundPanel.put(x, y, backgroundIndex, alternatePalette);
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
        for (int i = x; i < width && i < backgroundLightness.length; i++) {
            for (int j = y; j < height && j < backgroundLightness[i].length; j++) {
                lightnesses[i][j] = 256 + clamp(backgroundLightness[i][j], -255, 255);
                values[i][j] = true;
            }
        }
        lightnessPanel.put(0, 0, lightnesses, lightingPalette);
        backgroundPanel.put(x, y, backgroundIndex, bgPalette);
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into alternatePalette.
     *
     * @param layer 0 for background, 1 for lightness, 2 for foreground, 3 or higher for extra layers added on.
     * @param x     in grid cells.
     * @param y     in grid cells.
     * @param c     char[][] to be drawn in the foreground starting from x, y
     */
    public SquidLayers putInto(int layer, int x, int y, char c) {
        SquidPanel p = backgroundPanel;
        switch (layer) {
            case 0:
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                p = foregroundPanel;
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        p.put(x, y, c);
        values[x][y] = true;
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into the default palette.
     *
     * @param layer      0 for background, 1 for lightness, 2 for foreground, 3 or higher for extra layers added on.
     * @param x          in grid cells.
     * @param y          in grid cells.
     * @param c          char[][] to be drawn in the foreground starting from x, y
     * @param colorIndex int[][] of indices into alternatePalette for the char being drawn
     */
    public SquidLayers putInto(int layer, int x, int y, char c, int colorIndex) {
        SquidPanel p = backgroundPanel;
        switch (layer) {
            case 0:
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                p = foregroundPanel;
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        p.put(x, y, c, colorIndex, palette);
        values[x][y] = true;
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into alternatePalette.
     *
     * @param layer            0 for background, 1 for lightness, 2 for foreground, 3 or higher for extra layers added on.
     * @param x                in grid cells.
     * @param y                in grid cells.
     * @param c                char[][] to be drawn in the foreground starting from x, y
     * @param alternatePalette an alternate Color ArrayList for both foreground and background
     * @param colorIndex       int[][] of indices into alternatePalette for the char being drawn
     */
    public SquidLayers putInto(int layer, int x, int y, char c, ArrayList<Color> alternatePalette, int colorIndex) {
        SquidPanel p = backgroundPanel;
        switch (layer) {
            case 0:
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                p = foregroundPanel;
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        if (alternatePalette == null) alternatePalette = palette;
        p.put(x, y, c, colorIndex, alternatePalette);
        values[x][y] = true;
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into alternatePalette.
     *
     * @param layer 0 for background, 1 for lightness, 2 for foreground, 3 or higher for extra layers added on.
     * @param x     in grid cells.
     * @param y     in grid cells.
     * @param c     char[][] to be drawn in the foreground starting from x, y
     */
    public SquidLayers putInto(int layer, int x, int y, char[][] c) {
        SquidPanel p = backgroundPanel;
        switch (layer) {
            case 0:
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                p = foregroundPanel;
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        p.put(x, y, c);
        for (int i = x; i < c.length && i < width; i++) {
            for (int j = y; j < c[i].length && j < height; j++) {
                values[i][j] = true;
            }
        }
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into the default palette.
     *
     * @param layer      0 for background, 1 for lightness, 2 for foreground, 3 or higher for extra layers added on.
     * @param x          in grid cells.
     * @param y          in grid cells.
     * @param c          char[][] to be drawn in the foreground starting from x, y
     * @param colorIndex int[][] of indices into alternatePalette for the char being drawn
     */
    public SquidLayers putInto(int layer, int x, int y, char[][] c, int[][] colorIndex) {
        SquidPanel p = backgroundPanel;
        switch (layer) {
            case 0:
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                p = foregroundPanel;
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        p.put(x, y, c, colorIndex, palette);
        for (int i = x; i < c.length && i < width; i++) {
            for (int j = y; j < c[i].length && j < height; j++) {
                values[i][j] = true;
            }
        }
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into alternatePalette.
     *
     * @param layer            0 for background, 1 for lightness, 2 for foreground, 3 or higher for extra layers added on.
     * @param x                in grid cells.
     * @param y                in grid cells.
     * @param c                char[][] to be drawn in the foreground starting from x, y
     * @param alternatePalette an alternate Color ArrayList for both foreground and background
     * @param colorIndex       int[][] of indices into alternatePalette for the char being drawn
     */
    public SquidLayers putInto(int layer, int x, int y, char[][] c, ArrayList<Color> alternatePalette, int[][] colorIndex) {
        SquidPanel p = backgroundPanel;
        switch (layer) {
            case 0:
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                p = foregroundPanel;
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        if (alternatePalette == null) alternatePalette = palette;
        p.put(x, y, c, colorIndex, alternatePalette);
        for (int i = x; i < c.length && i < width; i++) {
            for (int j = y; j < c[i].length && j < height; j++) {
                values[i][j] = true;
            }
        }
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
     * @param foregroundIndex
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
     * @param foregroundIndex
     * @param backgroundIndex
     * @return this, for chaining
     */
    public SquidLayers putString(int x, int y, String s, int foregroundIndex, int backgroundIndex) {
        foregroundPanel.put(x, y, s, palette.get(foregroundIndex));
        for (int i = x; i < s.length() && i < width; i++) {
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
     * @param alternatePalette
     * @param foregroundIndex
     * @param backgroundIndex
     * @return this, for chaining
     */
    public SquidLayers putString(int x, int y, String s, ArrayList<Color> alternatePalette, int foregroundIndex, int backgroundIndex) {
        foregroundPanel.put(x, y, s, alternatePalette.get(foregroundIndex));
        for (int i = x; i < s.length() && i < width; i++) {
            backgroundPanel.put(i, y, alternatePalette.get(backgroundIndex));
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
        if (y > 0 && y + 1 < height && x > 0 && x + 1 < width) {
            for (int j = y - 1; j < y + 2 && j < height; j++) {
                for (int i = x - 1; i < s.length() + x + 2 && i < width; i++) {
                    foregroundPanel.put(i, j, ' ');
                    backgroundPanel.put(i, j, palette.get(9));
                    lightnesses[i][j] = -255;
                    lightnessPanel.put(i, j, 1, lightingPalette);
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
        lightness = clamp(lightness, -255, 255);
        lightnesses[x][y] = 256 + lightness;

        lightnessPanel.put(x, y, 256 + lightness, lightingPalette);
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
        for (int i = 0; i < lightness.length && x + i < width; i++) {
            for (int j = 0; j < lightness[i].length && y + j < height; j++) {
                lightness[i][j] = 256 + clamp(lightness[i][j], -255, 255);
                lightnesses[x+i][y+j] = lightness[i][j];
            }
        }
        lightnessPanel.put(x, y, lightness, lightingPalette);
        return this;
    }
    /**
     * Very basic check to see if something was rendered at the x,y cell requested. (usually this only checks the
     * foreground) If blank, false, otherwise true.
     *
     * @param x in grid cells.
     * @param y in grid cells.
     * @return
     */
    public boolean hasValue(int x, int y) {
        return values[x][y];
    }

    /**
     * Clear one cell at position x, y of  its foreground contents.
     * 
     * You may be looking for the erase() method, which erases all panels and all cells.
     *
     * @param x in grid cells
     * @param y in grid cells
     * @return
     */
    public SquidLayers clear(int x, int y) {
        foregroundPanel.clear(x, y);
        values[x][y] = false;
        return this;
    }

    /**
     * Erase everything visible in all cells or all layers.  This can be expensive to do in a traditional game loop,
     * since Swing is not meant for that at all.
     *
     * @return this, for chaining
     */
    public SquidLayers erase() {
        foregroundPanel.erase();
        lightnessPanel.erase();
        backgroundPanel.erase();
        for (SquidPanel sp : extraPanels) {
            sp.erase();
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                values[i][j] = false;
            }
        }
        return this;
    }

    public SquidLayers bump(int x, int y, int layer, Direction dir, float duration) {
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        if (duration < 0)
            duration = animationDuration;
        p.bump(x, y, dir, duration);
        return this;
    }
    public SquidLayers bump(AnimatedEntity ae, int layer, Direction dir, float duration) {
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        if (duration < 0)
            duration = animationDuration;
        p.bump(ae, dir, duration);
        return this;
    }

    public SquidLayers bump(int x, int y, Direction dir) {
        return bump(x, y, 2, dir, -1);
    }
    public SquidLayers bump(AnimatedEntity ae, Direction dir) {
        return bump(ae, 2, dir, -1);
    }

    public SquidLayers slide(AnimatedEntity ae, int endX, int endY, int layer, float duration) {
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        if (duration < 0)
            duration = animationDuration;
        p.slide(ae, endX, endY, duration);
        return this;
    }
    public SquidLayers slide(int x, int y, int endX, int endY, int layer, float duration) {
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        if (duration < 0)
            duration = animationDuration;
        p.slide(x, y, endX, endY, duration);
        return this;
    }

    public SquidLayers slide(int x, int y, int endX, int endY) {
        return slide(x, y, endX, endY, 2, -1);
    }
    public SquidLayers slide(AnimatedEntity ae, int endX, int endY) {
        return slide(ae, endX, endY, 2, -1);
    }

    public SquidLayers wiggle(int x, int y, int layer, float duration) {
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        if (duration < 0)
            duration = animationDuration;
        p.wiggle(x, y, duration);
        return this;
    }
    public SquidLayers wiggle(AnimatedEntity ae, int layer, float duration) {
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        if (duration < 0)
            duration = animationDuration;
        p.wiggle(ae, duration);
        return this;
    }

    public SquidLayers wiggle(int x, int y) {
        return wiggle(x, y, 2, -1);
    }
    public SquidLayers wiggle(AnimatedEntity ae) {
        return wiggle(ae, 2, -1);
    }
    public SquidLayers tint(int x, int y, Color color, int layer, float duration) {
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        if (duration < 0)
            duration = animationDuration;
        p.tint(x, y, color, duration);
        return this;
    }
    public SquidLayers tint(AnimatedEntity ae, Color color, int layer, float duration) {
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        if (duration < 0)
            duration = animationDuration;
        p.tint(ae, color, duration);
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
        if (lightnessPanel.hasActiveAnimations())
            return true;
        for (SquidPanel panel : extraPanels) {
            if (panel.hasActiveAnimations())
                return true;
        }
        return false;
    }

    public AnimatedEntity animateActor(int x, int y, char c, Color color, int layer) {
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        return p.animateActor(x, y, c, color);
    }

    public AnimatedEntity animateActor(int x, int y, char c, Color color) {
        return foregroundPanel.animateActor(x, y, c, color);
    }
    public AnimatedEntity animateActor(int x, int y, char c, Color color, boolean doubleWidth) {
        return foregroundPanel.animateActor(x, y, doubleWidth, c, color);
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
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        return p.animateActor(x, y, s, color);
    }
    public AnimatedEntity animateActor(int x, int y, String s, Color color, int layer, boolean doubleWidth) {
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        return p.animateActor(x, y, doubleWidth, s, color);
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

    public LinkedHashSet<AnimatedEntity> getAnimatedEntities(int layer) {
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        return p.getAnimatedEntities();
    }

    public LinkedHashSet<AnimatedEntity> getAnimatedEntities() {
        return foregroundPanel.getAnimatedEntities();
    }

    public AnimatedEntity getAnimatedEntityByCell(int x, int y, int layer) {
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        return p.getAnimatedEntityByCell(x, y);
    }

    public AnimatedEntity getAnimatedEntityByCell(int x, int y) {
        return foregroundPanel.getAnimatedEntityByCell(x, y);
    }
        @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    public void drawActor(Batch batch, float parentAlpha, AnimatedEntity ae, int layer)
    {
        SquidPanel p = foregroundPanel;
        switch (layer) {
            case 0:
                p = backgroundPanel;
                break;
            case 1:
                p = lightnessPanel;
                break;
            case 2:
                break;
            default:
                p = extraPanels.get(layer - 3);
        }
        p.drawActor(batch, parentAlpha, ae);
    }
    public void drawActor(Batch batch, float parentAlpha, AnimatedEntity ae)
    {
        foregroundPanel.drawActor(batch, parentAlpha, ae);
    }


    private int clamp(int x, int min, int max)
    {
        return Math.min(Math.max(min, x), max);
    }

}
