package squidpony.squidgrid.gui;

import squidpony.Colors;
import squidpony.annotation.Beta;

import javax.swing.JLayeredPane;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to make using multiple SquidPanels easier.
 * There is some useful documentation in this class' getPalette method (honestly, I don't know where else to put
 * documentation specifically about this class' default palette).
 * Created by Tommy Ettinger on 7/6/2015.
 */
@Beta
public class SquidLayers extends JLayeredPane {
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

    /**
     * The pixel width of the entire map.
     * @return
     */
    @Override
    public int getWidth() {
        return width * cellWidth;
    }

    /**
     * The pixel height of the entire map.
     * @return
     */
    @Override
    public int getHeight() {
        return height * cellHeight;
    }

    /**
     * Width of the map in grid cells.
     * @return
     */
    public int getGridWidth() {
        return width;
    }

    /**
     * Height of the map in grid cells.
     * @return
     */
    public int getGridHeight() {
        return height;
    }

    /**
     * Width of one cell in pixels.
     * @return
     */
    public int getCellWidth() {
        return cellWidth;
    }

    /**
     * Height of one cell in pixels.
     * @return
     */
    public int getCellHeight() {
        return cellHeight;
    }

    /**
     * Gets the current palette used when no other is specified.
     *
     * The palette can be customized with SquidLayers.alterPalette() and SquidLayers.extendPalette() .
     *
     * The default palette has colors at these elements:
     * <ul>
     *     <li>0: Black, also used for backgrounds if not specified</li>
     *     <li>1: Off-white, used as the default foreground at times</li>
     *     <li>2: Dark gray for walls</li>
     *     <li>3: Silver gray for floors</li>
     *     <li>4: Rust brown for doors</li>
     *     <li>5: Gray-blue for water</li>
     *     <li>6: Bright orange for traps</li>
     *     <li>7: White</li>
     *     <li>8: Light gray</li>
     *     <li>9: Dark gray</li>
     *     <li>10: Light red</li>
     *     <li>11: Medium red</li>
     *     <li>12: Dark red</li>
     *     <li>13: Light orange</li>
     *     <li>14: Medium orange</li>
     *     <li>15: Dark orange</li>
     *     <li>16: Light yellow</li>
     *     <li>17: Medium yellow</li>
     *     <li>18: Dark yellow</li>
     *     <li>19: Light green</li>
     *     <li>20: Medium green</li>
     *     <li>21: Dark green</li>
     *     <li>22: Light blue-green</li>
     *     <li>23: Medium blue-green</li>
     *     <li>24: Dark blue-green</li>
     *     <li>25: Light blue</li>
     *     <li>26: Medium blue</li>
     *     <li>27: Dark blue</li>
     *     <li>28: Light purple</li>
     *     <li>29: Medium purple</li>
     *     <li>30: Dark purple</li>
     *     <li>31: Light pink</li>
     *     <li>32: Medium pink</li>
     *     <li>33: Dark pink</li>
     *     <li>34: Light gray-brown</li>
     *     <li>35: Medium gray-brown</li>
     *     <li>36: Dark gray-brown</li>
     *     <li>37: Light brown</li>
     *     <li>38: Medium brown</li>
     *     <li>39: Dark brown</li>
     * </ul>
     * @return the current Color ArrayList used as a default palette.
     */
    public ArrayList<Color> getPalette() {
        return palette;
    }

    /**
     * Get the lightness modifiers used for background cells as an int[][], with elements between 0 and 511, 256 as the
     * unmodified lightness level, lower numbers meaning darker, and higher meaning lighter.
     * @return
     */
    public int[][] getLightnesses() {
        return lightnesses;
    }

    /**
     * Get all the background color indices at once, which are probably indexed into the default palette. See the
     * documentation for the getPalette() method in this class for more information.
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
     * @param bgIndices 2D array, width and height should match this class' gridWidth and gridHeight.
     */
    public void setBgIndices(int[][] bgIndices) {
        this.bgIndices = bgIndices;
    }

    /**
     * Create a new SquidLayers widget with the default <b>square</b> font, 40 cells wide and high, with a size of
     * 12x12 pixels for each cell.
     */
    public SquidLayers()
    {
        this(40, 40);
    }

    /**
     * Create a new SquidLayers widget with the default <b>square</b> font, the given number of cells for gridWidth
     * and gridHeight, and 12x12 pixels for each cell.
     * @param gridWidth in grid cells
     * @param gridHeight in grid cells
     */
    public SquidLayers(int gridWidth, int gridHeight)
    {
        super();
        initPalettes();
        width = gridWidth;
        height = gridHeight;

        cellWidth = 12;
        cellHeight = 12;

        bgIndices = new int[width][height];
        lightnesses = new int[width][height];
        values = new boolean[width][height];

        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                lightnesses[x][y] = 256;
            }
        }

        textFactory = new TextCellFactory().font(DefaultResources.getDefaultFont()).width(12).height(12).initBySize();

        backgroundPanel = new SquidPanel(gridWidth, gridHeight);
        backgroundPanel.setOpaque(true);
        lightnessPanel = new SquidPanel(gridWidth, gridHeight);
        lightnessPanel.setOpaque(false);
        foregroundPanel = new SquidPanel(gridWidth, gridHeight);
        foregroundPanel.setOpaque(false);

        backgroundPanel.refresh();
        lightnessPanel.refresh();
        foregroundPanel.refresh();

        extraPanels = new ArrayList<SquidPanel>();

        this.setLayer(backgroundPanel, 0);
        this.add(backgroundPanel);
        this.setLayer(lightnessPanel, 1);
        this.add(lightnessPanel);
        this.setLayer(foregroundPanel, 2);
        this.add(foregroundPanel);

        this.setSize(backgroundPanel.getPreferredSize());
        this.setPreferredSize(backgroundPanel.getPreferredSize());
    }

    /**
     * Create a new SquidLayers widget with the default <b>narrow</b> font, the given number of cells for gridWidth
     * and gridHeight, and the size in pixels for each cell given by cellWidth and cellHeight.
     * @param gridWidth in grid cells
     * @param gridHeight in grid cells
     * @param cellWidth in pixels
     * @param cellHeight in pixels
     */
    public  SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight)
    {
        super();
        initPalettes();
        width = gridWidth;
        height = gridHeight;

        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;

        bgIndices = new int[width][height];
        lightnesses = new int[width][height];
        values = new boolean[width][height];
        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                lightnesses[x][y] = 256;
            }
        }

        textFactory = new TextCellFactory().font(DefaultResources
                .getDefaultNarrowFont().deriveFont(4.0f * gridHeight / 3.0f))
                .width(cellWidth).height(cellHeight).initBySize();

        backgroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);
        backgroundPanel.setOpaque(true);
        lightnessPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);
        lightnessPanel.setOpaque(false);
        foregroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);
        foregroundPanel.setOpaque(false);

        backgroundPanel.refresh();
        lightnessPanel.refresh();
        foregroundPanel.refresh();

        extraPanels = new ArrayList<SquidPanel>();

        this.setLayer(backgroundPanel, 0);
        this.add(backgroundPanel);
        this.setLayer(lightnessPanel, 1);
        this.add(lightnessPanel);
        this.setLayer(foregroundPanel, 2);
        this.add(foregroundPanel);

        this.setSize(backgroundPanel.getPreferredSize());
        this.setPreferredSize(backgroundPanel.getPreferredSize());
    }

    /**
     * Create a new SquidLayers widget with the given Font, the given number of cells for gridWidth
     * and gridHeight, and the size in pixels for each cell given by cellWidth and cellHeight.
     * @param gridWidth in grid cells
     * @param gridHeight in grid cells
     * @param cellWidth in pixels
     * @param cellHeight in pixels
     * @param font A Font that should have been assigned a size before being passed here.
     */
    public SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight, Font font)
    {
        super();
        initPalettes();

        width = gridWidth;
        height = gridHeight;

        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;

        bgIndices = new int[width][height];
        lightnesses = new int[width][height];
        values = new boolean[width][height];
        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                lightnesses[x][y] = 256;
            }
        }

        textFactory = new TextCellFactory().font(font).width(cellWidth).height(cellHeight).initVerbatim();

        backgroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);
        backgroundPanel.setOpaque(true);
        lightnessPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);
        lightnessPanel.setOpaque(false);
        foregroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);
        foregroundPanel.setOpaque(false);

        backgroundPanel.refresh();
        lightnessPanel.refresh();
        foregroundPanel.refresh();

        extraPanels = new ArrayList<SquidPanel>();

        this.setLayer(backgroundPanel, 0);
        this.add(backgroundPanel);
        this.setLayer(lightnessPanel, 1);
        this.add(lightnessPanel);
        this.setLayer(foregroundPanel, 2);
        this.add(foregroundPanel);

        this.setSize(backgroundPanel.getPreferredSize());
        this.setPreferredSize(backgroundPanel.getPreferredSize());
    }

    private void initPalettes()
    {
        palette = new ArrayList<Color>(256);
        palette.add(Colors.DARK_SLATE_GRAY);
        palette.add(Colors.CREAM);
        palette.add(Colors.FLATTERY_BROWN);
        palette.add(Colors.SILVER_GREY);
        palette.add(Colors.RUST);
        palette.add(Colors.WATER);
        palette.add(Colors.INTERNATIONAL_ORANGE);

        palette.add(Colors.WHITE);
        palette.add(Colors.LIGHT_GRAY);
        palette.add(Colors.DARK_GRAY);

        palette.add(Colors.RED_INCENSE);
        palette.add(Colors.RED);
        palette.add(Colors.COCHINEAL_RED);

        palette.add(Colors.PEACH_ORANGE);
        palette.add(Colors.ORANGE_PEEL);
        palette.add(Colors.TANGERINE);

        palette.add(Colors.LEMON_CHIFFON);
        palette.add(Colors.CORN);
        palette.add(Colors.GOLDEN_YELLOW);

        palette.add(Colors.TEA_GREEN);
        palette.add(Colors.LIME_GREEN);
        palette.add(Colors.PINE_GREEN);

        palette.add(Colors.BABY_BLUE);
        palette.add(Colors.CYAN);
        palette.add(Colors.BLUE_GREEN);

        palette.add(Colors.COLUMBIA_BLUE);
        palette.add(Colors.ROYAL_BLUE);
        palette.add(Colors.PERSIAN_BLUE);

        palette.add(Colors.LAVENDER_BLUE);
        palette.add(Colors.THIN_VIOLET);
        palette.add(Colors.DARK_VIOLET);

        palette.add(Colors.CARNATION_PINK);
        palette.add(Colors.HOT_MAGENTA);
        palette.add(Colors.LIGHT_MAROON);

        palette.add(Colors.TAN);
        palette.add(Colors.DARK_TAN);
        palette.add(Colors.PALE_BROWN);

        palette.add(Colors.STEAMED_CHESTNUT);
        palette.add(Colors.DARK_CHESTNUT);
        palette.add(Colors.SAPPANWOOD_INCENSE);

        lightingPalette = new ArrayList<Color>(512);
        for(int i = 0; i < 512; i++)
        {
            lightingPalette.add(Colors.TRANSPARENT);
        }
        for(int i = 1; i < 256; i++)
        {
            lightingPalette.set(256 + i, new Color(0xFF, 0xFD, 0xD8, i));
            lightingPalette.set(256 - i, new Color(0, 0, 0, i));
        }

    }

    /**
     * Add an extra layer on top of the foreground layer. Use putInto methods to specify the layer when adding a char (0
     * is background, 1 is lightness modifiers, 2 is foreground, and the first call to this method creates layer 3).
     * @return
     */
    public SquidLayers addExtraLayer()
    {
        SquidPanel sp = new SquidPanel(width, height, textFactory, null);
        this.setLayer(sp, 3 + extraPanels.size());
        sp.refresh();
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
    public ArrayList<Color> extendPalette(Color color)
    {
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
    public ArrayList<Color> alterPalette(int index, Color color)
    {
        if(index >= 0 && index < palette.size())
            palette.set(index, color);

        return palette;
    }

    /**
     * Place a char c into the foreground at position x, y, with the default color.
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c a character to be drawn in the foreground
     */
    public SquidLayers put(int x, int y, char c)
    {
        foregroundPanel.put(x, y, c);
        values[x][y] = true;
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color specified by an index into the default palette.
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c a character to be drawn in the foreground
     * @param foregroundIndex int index into the default palette for the char being drawn
     */
    public SquidLayers put(int x, int y, char c, int foregroundIndex)
    {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        values[x][y] = true;
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color specified by an index into the default palette, and a
     * background color specified in the same way.
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c a character to be drawn in the foreground
     * @param foregroundIndex int index into the default palette for the char being drawn
     * @param backgroundIndex int index into the default palette for the background
     */
    public SquidLayers put(int x, int y, char c, int foregroundIndex, int backgroundIndex)
    {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        backgroundPanel.put(x, y, backgroundIndex, palette);
        values[x][y] = true;
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color specified by an index into the default palette, a
     * background color specified in the same way, and a lightness variation for the background (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c a character to be drawn in the foreground
     * @param foregroundIndex int index into the default palette for the char being drawn
     * @param backgroundIndex int index into the default palette for the background
     * @param backgroundLightness int between -255 and 255 , lower numbers are darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char c, int foregroundIndex, int backgroundIndex, int backgroundLightness)
    {
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
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c a character to be drawn in the foreground
     * @param alternatePalette an alternate Color List for both foreground and background
     * @param foregroundIndex int index into alternatePalette for the char being drawn
     * @param backgroundIndex int index into alternatePalette for the background
     * @param backgroundLightness int between -255 and 255 , lower numbers are darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char c, List<Color> alternatePalette, int foregroundIndex, int backgroundIndex, int backgroundLightness)
    {
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
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c a character to be drawn in the foreground
     * @param foregroundIndex int index into alternatePalette for the char being drawn
     * @param fgPalette an alternate Color List for the foreground; can be null to use the default.
     * @param backgroundIndex int index into alternatePalette for the background
     * @param bgPalette an alternate Color List for the background; can be null to use the default.
     * @param backgroundLightness int between -255 and 255 , lower numbers are darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char c, int foregroundIndex, List<Color> fgPalette, int backgroundIndex, List<Color> bgPalette, int backgroundLightness)
    {
        backgroundLightness = clamp(backgroundLightness, -255, 255);
        if(fgPalette == null) fgPalette = palette;
        if(bgPalette == null) bgPalette = palette;
        foregroundPanel.put(x, y, c, foregroundIndex, fgPalette);
        values[x][y] = true;
        lightnesses[x][y] = 256 + backgroundLightness;

        lightnessPanel.put(x, y, 256 + backgroundLightness, lightingPalette);
        backgroundPanel.put(x, y, backgroundIndex, bgPalette);
        return this;
    }


    public SquidLayers put(int x, int y, char[][] c)
    {
        foregroundPanel.put(x, y, c);
        for(int i = x; i < c.length && i < width; i++)
        {
            for(int j = y; j < c[i].length && j < height; j++)
            {
                values[i][j] = true;
            }
        }
        return this;
    }

    public SquidLayers put(int x, int y, char[][] c, int[][] foregroundIndex)
    {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        for(int i = x; i < c.length && i < width; i++)
        {
            for(int j = y; j < c[i].length && j < height; j++)
            {
                values[i][j] = true;
            }
        }
        return this;
    }

    public SquidLayers put(int x, int y, char c[][], int[][] foregroundIndex, int[][] backgroundIndex)
    {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        for(int i = x; i < c.length && i < width; i++)
        {
            for(int j = y; j < c[i].length && j < height; j++)
            {
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
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c char[][] to be drawn in the foreground starting from x, y
     * @param foregroundIndex int[][] of indices into the default palette for the char being drawn
     * @param backgroundIndex int[][] of indices into the default palette for the background
     * @param backgroundLightness int[][] with elements between -255 and 255 , lower darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char[][] c, int[][] foregroundIndex, int[][] backgroundIndex, int[][] backgroundLightness)
    {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        for(int i = x; i < width && i < backgroundLightness.length; i++) {
            for (int j = y; j < height && j < backgroundLightness[i].length; j++)
            {
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
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c char[][] to be drawn in the foreground starting from x, y
     * @param alternatePalette an alternate Color List for both foreground and background
     * @param foregroundIndex int[][] of indices into alternatePalette for the char being drawn
     * @param backgroundIndex int[][] of indices into alternatePalette for the background
     * @param backgroundLightness int[][] with elements between -255 and 255 , lower darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char[][] c, List<Color> alternatePalette,  int[][] foregroundIndex, int[][] backgroundIndex, int[][] backgroundLightness)
    {

        if(alternatePalette == null) alternatePalette = palette;
        foregroundPanel.put(x, y, c, foregroundIndex, alternatePalette);
        for(int i = x; i < width && i < backgroundLightness.length; i++) {
            for (int j = y; j < height && j < backgroundLightness[i].length; j++)
            {
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
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c char[][] to be drawn in the foreground starting from x, y
     * @param foregroundIndex int[][] of indices into fgPalette for the char being drawn
     * @param fgPalette an alternate Color List for the foreground; can be null to use the default.
     * @param backgroundIndex int[][] of indices into bgPalette for the background
     * @param bgPalette an alternate Color List for the background; can be null to use the default.
     * @param backgroundLightness int[][] with elements between -255 and 255 , lower darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char[][] c, int[][] foregroundIndex, List<Color> fgPalette, int[][] backgroundIndex, List<Color> bgPalette, int[][] backgroundLightness)
    {
        if(fgPalette == null) fgPalette = palette;
        if(bgPalette == null) bgPalette = palette;
        foregroundPanel.put(x, y, c, foregroundIndex, fgPalette);
        for(int i = x; i < width && i < backgroundLightness.length; i++) {
            for (int j = y; j < height && j < backgroundLightness[i].length; j++)
            {
                lightnesses[i][j] =  256 +clamp(backgroundLightness[i][j], -255, 255);
                values[i][j] = true;
            }
        }
        lightnessPanel.put(0, 0, lightnesses, lightingPalette);
        backgroundPanel.put(x, y, backgroundIndex, bgPalette);
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into alternatePalette.
     * @param layer 0 for background, 1 for lightness, 2 for foreground, 3 or higher for extra layers added on.
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c char[][] to be drawn in the foreground starting from x, y
     */
    public SquidLayers putInto(int layer, int x, int y, char c)
    {
        SquidPanel p = backgroundPanel;
        switch (layer)
        {
            case 0: break;
            case 1: p = lightnessPanel;
                break;
            case 2: p = foregroundPanel;
                break;
            default: p = extraPanels.get(layer - 3);
        }
        p.put(x, y, c);
        values[x][y] = true;
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into the default palette.
     * @param layer 0 for background, 1 for lightness, 2 for foreground, 3 or higher for extra layers added on.
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c char[][] to be drawn in the foreground starting from x, y
     * @param colorIndex int[][] of indices into alternatePalette for the char being drawn
     */
    public SquidLayers putInto(int layer, int x, int y, char c,  int colorIndex)
    {
        SquidPanel p = backgroundPanel;
        switch (layer)
        {
            case 0: break;
            case 1: p = lightnessPanel;
                break;
            case 2: p = foregroundPanel;
                break;
            default: p = extraPanels.get(layer - 3);
        }
        p.put(x, y, c, colorIndex, palette);
        values[x][y] = true;
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into alternatePalette.
     * @param layer 0 for background, 1 for lightness, 2 for foreground, 3 or higher for extra layers added on.
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c char[][] to be drawn in the foreground starting from x, y
     * @param alternatePalette an alternate Color List for both foreground and background
     * @param colorIndex int[][] of indices into alternatePalette for the char being drawn
     */
    public SquidLayers putInto(int layer, int x, int y, char c, List<Color> alternatePalette,  int colorIndex)
    {
        SquidPanel p = backgroundPanel;
        switch (layer)
        {
            case 0: break;
            case 1: p = lightnessPanel;
                break;
            case 2: p = foregroundPanel;
                break;
            default: p = extraPanels.get(layer - 3);
        }
        if(alternatePalette == null) alternatePalette = palette;
        p.put(x, y, c, colorIndex, alternatePalette);
        values[x][y] = true;
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into alternatePalette.
     * @param layer 0 for background, 1 for lightness, 2 for foreground, 3 or higher for extra layers added on.
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c char[][] to be drawn in the foreground starting from x, y
     */
    public SquidLayers putInto(int layer, int x, int y, char[][] c)
    {
        SquidPanel p = backgroundPanel;
        switch (layer)
        {
            case 0: break;
            case 1: p = lightnessPanel;
                break;
            case 2: p = foregroundPanel;
                break;
            default: p = extraPanels.get(layer - 3);
        }
        p.put(x, y, c);
        for(int i = x; i < c.length && i < width; i++)
        {
            for(int j = y; j < c[i].length && j < height; j++)
            {
                values[i][j] = true;
            }
        }
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into the default palette.
     * @param layer 0 for background, 1 for lightness, 2 for foreground, 3 or higher for extra layers added on.
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c char[][] to be drawn in the foreground starting from x, y
     * @param colorIndex int[][] of indices into alternatePalette for the char being drawn
     */
    public SquidLayers putInto(int layer, int x, int y, char[][] c,  int[][] colorIndex)
    {
        SquidPanel p = backgroundPanel;
        switch (layer)
        {
            case 0: break;
            case 1: p = lightnessPanel;
                break;
            case 2: p = foregroundPanel;
                break;
            default: p = extraPanels.get(layer - 3);
        }
        p.put(x, y, c, colorIndex, palette);
        for(int i = x; i < c.length && i < width; i++)
        {
            for(int j = y; j < c[i].length && j < height; j++)
            {
                values[i][j] = true;
            }
        }
        return this;
    }

    /**
     * Place a char c[][] into the specified layer, with a color specified by an index into alternatePalette.
     * @param layer 0 for background, 1 for lightness, 2 for foreground, 3 or higher for extra layers added on.
     * @param x in grid cells.
     * @param y in grid cells.
     * @param c char[][] to be drawn in the foreground starting from x, y
     * @param alternatePalette an alternate Color List for both foreground and background
     * @param colorIndex int[][] of indices into alternatePalette for the char being drawn
     */
    public SquidLayers putInto(int layer, int x, int y, char[][] c, List<Color> alternatePalette,  int[][] colorIndex)
    {
        SquidPanel p = backgroundPanel;
        switch (layer)
        {
            case 0: break;
            case 1: p = lightnessPanel;
                break;
            case 2: p = foregroundPanel;
                break;
            default: p = extraPanels.get(layer - 3);
        }
        if(alternatePalette == null) alternatePalette = palette;
        p.put(x, y, c, colorIndex, alternatePalette);
        for(int i = x; i < c.length && i < width; i++)
        {
            for(int j = y; j < c[i].length && j < height; j++)
            {
                values[i][j] = true;
            }
        }
        return this;
    }

    /**
     * Put a string at the given x, y position, using the default color.
     * @param x in grid cells.
     * @param y in grid cells.
     * @param s the string to print
     * @return this, for chaining
     */
    public SquidLayers putString(int x, int y, String s)
    {
        foregroundPanel.put(x, y, s);
        return this;
    }
    /**
     * Put a string at the given x, y position, with the given index for foreground color that gets looked up in the
     * default palette.
     * @param x in grid cells.
     * @param y in grid cells.
     * @param s the string to print
     * @param foregroundIndex
     * @return this, for chaining
     */
    public SquidLayers putString(int x, int y, String s, int foregroundIndex)
    {
        foregroundPanel.put(x, y, s, palette.get(foregroundIndex));
        return this;
    }
    /**
     * Put a string at the given x, y position, with the given indices for foreground and background color that look up
     * their index in the default palette.
     * @param x in grid cells.
     * @param y in grid cells.
     * @param s the string to print
     * @param foregroundIndex
     * @param backgroundIndex
     * @return this, for chaining
     */
    public SquidLayers putString(int x, int y, String s, int foregroundIndex, int backgroundIndex)
    {
        foregroundPanel.put(x, y, s, palette.get(foregroundIndex));
        for(int i = x; i < s.length() && i < width; i++)
        {
            backgroundPanel.put(i, y, palette.get(backgroundIndex));
        }
        return this;
    }

    /**
     * Put a string at the given x, y position, with the given indices for foreground and background color that look up
     * their index in alternatePalette.
     * @param x in grid cells.
     * @param y in grid cells.
     * @param s the string to print
     * @param alternatePalette
     * @param foregroundIndex
     * @param backgroundIndex
     * @return this, for chaining
     */
    public SquidLayers putString(int x, int y, String s, List<Color> alternatePalette, int foregroundIndex, int backgroundIndex)
    {
        foregroundPanel.put(x, y, s, alternatePalette.get(foregroundIndex));
        for(int i = x; i < s.length() && i < width; i++)
        {
            backgroundPanel.put(i, y, alternatePalette.get(backgroundIndex));
        }
        return this;
    }

    /**
     * A utility method that draws a 1-cell-wide black box around the text you request (as s) and replaces the contents
     * of anything that was below or adjacent to the string's new position. Useful for message boxes.
     * @param x in grid cells.
     * @param y in grid cells.
     * @param s the string to print inside the box
     * @return this, for chaining
     */
    public SquidLayers putBoxedString(int x, int y, String s)
    {
        if(y > 0 && y + 1 < height && x > 0 && x + 1 < width) {
            for(int j = y - 1; j < y + 2 && j < height; j++) {
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
     * Used to check if an x,y cell has changed, checking all layers for changes.
     * @param x in grid cells.
     * @param y in grid cells.
     * @return true if it changed, false if it didn't.
     */
    public boolean hasChanged(int x, int y)
    {
        if(backgroundPanel.hasChanged(x, y) ||
           lightnessPanel.hasChanged(x, y) ||
           foregroundPanel.hasChanged(x, y)) return true;
        for(int i = 0; i < extraPanels.size(); i++)
        {
            if(extraPanels.get(i).hasChanged(x, y)) return true;

        }
        return false;
    }

    /**
     * Very basic check to see if something was rendered at the x,y cell requested. (usually this only checks the
     * foreground) If blank, false, otherwise true.
     * @param x in grid cells.
     * @param y in grid cells.
     * @return
     */
    public boolean hasValue(int x, int y)
    {
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
    public SquidLayers clear(int x, int y)
    {
        foregroundPanel.clear(x, y);
        values[x][y] = false;
        return this;
    }

    /**
     * Erase everything visible in all cells or all layers.  This can be expensive to do in a traditional game loop,
     * since Swing is not meant for that at all.
     * @return this, for chaining
     */
    public SquidLayers erase()
    {
        foregroundPanel.erase();
        lightnessPanel.erase();
        backgroundPanel.erase();
        for(SquidPanel sp : extraPanels)
        {
            sp.erase();
        }
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                values[i][j] = false;
            }
        }
        return this;
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintComponents(g);
        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Call this whenever you want to show the changes you made to contents of this widget.
     */
    public void refresh()
    {
        backgroundPanel.refresh();
        lightnessPanel.refresh();
        foregroundPanel.refresh();
        for(SquidPanel sp : extraPanels)
        {
            sp.refresh();
        }
    }

    private int clamp(int x, int min, int max)
    {
        return Math.min(Math.max(min, x), max);
    }

}
