package squidpony.squidgrid.gui;

import squidpony.Colors;
import squidpony.annotation.Beta;

import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class to make using multiple SquidPanels easier.
 * Created by Tommy Ettinger on 7/6/2015.
 */
@Beta
public class SquidLayers extends JLayeredPane {
    protected int width, height, cellWidth, cellHeight;
    protected SquidPanel backgroundPanel, lightnessPanel, foregroundPanel;
    protected int[][] bgIndices;
    protected int[][] lightnesses;
    protected ArrayList<SquidPanel> extraPanels;
    protected TextCellFactory textFactory;
    protected ArrayList<Color> palette, lightingPalette;

    @Override
    public int getWidth() {
        return width * cellWidth;
    }

    @Override
    public int getHeight() {
        return height * cellHeight;
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

    public int[][] getLightnesses() {
        return lightnesses;
    }

    public int[][] getBgIndices() {
        return bgIndices;
    }

    public void setLightnesses(int[][] lightnesses) {
        this.lightnesses = lightnesses;
    }

    public void setBgIndices(int[][] bgIndices) {
        this.bgIndices = bgIndices;
    }

    public SquidLayers()
    {
        this(40, 40);
    }
    public  SquidLayers(int gridWidth, int gridHeight)
    {
        super();
        initPalettes();
        width = gridWidth;
        height = gridHeight;

        cellWidth = 16;
        cellHeight = 16;

        bgIndices = new int[width][height];
        lightnesses = new int[width][height];

        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                lightnesses[x][y] = 256;
            }
        }

        textFactory = new TextCellFactory().width(16).height(16);

        backgroundPanel = new SquidPanel(gridWidth, gridHeight);
        lightnessPanel = new SquidPanel(gridWidth, gridHeight);
        foregroundPanel = new SquidPanel(gridWidth, gridHeight);

        backgroundPanel.refresh();
        lightnessPanel.refresh();
        foregroundPanel.refresh();

        extraPanels = new ArrayList<SquidPanel>();

        this.setLayer(backgroundPanel, 0);
        this.add(backgroundPanel);
        this.setLayer(lightnessPanel, 1);
        this.add(lightnessPanel);
        this.setLayer(foregroundPanel, 3);
        this.add(foregroundPanel);

        this.setSize(backgroundPanel.getPreferredSize());
        this.setPreferredSize(backgroundPanel.getPreferredSize());
    }

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
        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                lightnesses[x][y] = 256;
            }
        }

        textFactory = new TextCellFactory().width(cellWidth).height(cellHeight);

        backgroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);
        lightnessPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);
        foregroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);

        backgroundPanel.refresh();
        lightnessPanel.refresh();
        foregroundPanel.refresh();

        extraPanels = new ArrayList<SquidPanel>();

        this.setLayer(backgroundPanel, 0);
        this.add(backgroundPanel);
        this.setLayer(lightnessPanel, 1);
        this.add(lightnessPanel);
        this.setLayer(foregroundPanel, 3);
        this.add(foregroundPanel);

        this.setSize(backgroundPanel.getPreferredSize());
        this.setPreferredSize(backgroundPanel.getPreferredSize());
    }
    public  SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight, Font font)
    {
        super();
        initPalettes();

        width = gridWidth;
        height = gridHeight;

        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;

        bgIndices = new int[width][height];
        lightnesses = new int[width][height];
        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                lightnesses[x][y] = 256;
            }
        }

        textFactory = new TextCellFactory().font(font).width(cellWidth).height(cellHeight);

        backgroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);
        lightnessPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);
        foregroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);

        backgroundPanel.refresh();
        lightnessPanel.refresh();
        foregroundPanel.refresh();

        extraPanels = new ArrayList<SquidPanel>();

        this.setLayer(backgroundPanel, 0);
        this.add(backgroundPanel);
        this.setLayer(lightnessPanel, 1);
        this.add(lightnessPanel);
        this.setLayer(foregroundPanel, 3);
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

    public SquidLayers addExtraLayer()
    {
        SquidPanel sp = new SquidPanel(width, height, textFactory, null);
        this.setLayer(sp, 4 + extraPanels.size());
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

    public SquidLayers put(int x, int y, char c)
    {
        foregroundPanel.put(x, y, c);
        return this;
    }

    public SquidLayers put(int x, int y, char c, int foregroundIndex)
    {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        return this;
    }

    public SquidLayers put(int x, int y, char c, int foregroundIndex, int backgroundIndex)
    {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        backgroundPanel.put(x, y, backgroundIndex, palette);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color specified by an index into the default palette, a
     * background color specified in the same way, and a lightness variation for the background (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     * @param x
     * @param y
     * @param c a character to be drawn in the foreground
     * @param foregroundIndex int index into the default palette for the char being drawn
     * @param backgroundIndex int index into the default palette for the background
     * @param backgroundLightness int between -255 and 255 , lower numbers are darker, higher lighter.
     */
    public SquidLayers put(int x, int y, char c, int foregroundIndex, int backgroundIndex, int backgroundLightness)
    {
        backgroundLightness = clamp(backgroundLightness, -255, 255);
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        lightnesses[x][y] = 256 + backgroundLightness;

        lightnessPanel.put(x, y, 256 + backgroundLightness, lightingPalette);
        backgroundPanel.put(x, y, backgroundIndex, palette);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color specified by an index into alternatePalette, a
     * background color specified in the same way, and a lightness variation for the background (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     * @param x
     * @param y
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
        lightnesses[x][y] = 256 + backgroundLightness;

        lightnessPanel.put(x, y, 256 + backgroundLightness, lightingPalette);
        backgroundPanel.put(x, y, backgroundIndex, alternatePalette);
        return this;
    }
    /**
     * Place a char c into the foreground, with a foreground color specified by an index into alternatePalette, a
     * background color specified in the same way, and a lightness variation for the background (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     * @param x
     * @param y
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
        lightnesses[x][y] = 256 + backgroundLightness;

        lightnessPanel.put(x, y, 256 + backgroundLightness, lightingPalette);
        backgroundPanel.put(x, y, backgroundIndex, bgPalette);
        return this;
    }


    public SquidLayers put(int x, int y, char[][] c)
    {
        foregroundPanel.put(x, y, c);
        return this;
    }

    public SquidLayers put(int x, int y, char[][] c, int[][] foregroundIndex)
    {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        return this;
    }

    public SquidLayers put(int x, int y, char c[][], int[][] foregroundIndex, int[][] backgroundIndex)
    {
        foregroundPanel.put(x, y, c, foregroundIndex, palette);
        backgroundPanel.put(x, y, backgroundIndex, palette);
        return this;
    }

    /**
     * Place a char c into the foreground, with a foreground color specified by an index into alternatePalette, a
     * background color specified in the same way, and a lightness variation for the background (0 is no change, 100 is
     * very bright, -100 is very dark, anything past -150 or 150 will make the background almost fully black or white).
     * @param x
     * @param y
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
     * @param x
     * @param y
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
     * @param x
     * @param y
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
            }
        }
        lightnessPanel.put(0, 0, lightnesses, lightingPalette);
        backgroundPanel.put(x, y, backgroundIndex, bgPalette);
        return this;
    }

    public SquidLayers clear(int x, int y)
    {
        foregroundPanel.clear(x, y);
        return this;
    }
    public SquidLayers erase()
    {
        foregroundPanel.erase();
        lightnessPanel.erase();
        backgroundPanel.erase();
        for(SquidPanel sp : extraPanels)
        {
            sp.erase();
        }
        return this;
    }

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
