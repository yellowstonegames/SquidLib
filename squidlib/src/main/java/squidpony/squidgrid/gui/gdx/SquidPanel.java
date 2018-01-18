package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import squidpony.ArrayTools;
import squidpony.IColorCenter;
import squidpony.StringKit;
import squidpony.panel.IColoredString;
import squidpony.panel.ISquidPanel;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.StatefulRNG;

import java.util.Collection;

import static com.badlogic.gdx.math.MathUtils.clamp;

/**
 * Displays text and images in a grid pattern. Supports basic animations.
 * 
 * Grid width and height settings are in terms of number of cells. Cell width and height
 * are in terms of number of pixels.
 *
 * When text is placed, the background color is set separately from the foreground character. When moved, only the
 * foreground character is moved.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SquidPanel extends Group implements IPackedColorPanel {

    public float DEFAULT_ANIMATION_DURATION = 0.12F;
    protected int animationCount = 0;
    protected Color defaultForeground = Color.WHITE;
    protected IColorCenter<Color> scc;
    protected int cellWidth, cellHeight;
    protected int gridWidth, gridHeight, gridOffsetX = 0, gridOffsetY = 0;
    /**
     * The 2D array of chars that this will render, using x,y indexing.
     * Full-block cells that are completely filled with their color will be the char at Unicode codepoint 0,
     * usually represented with {@code '\0'}.
     */
    public char[][] contents;
    /**
     * The 2D array of floats representing colors in a way that libGDX can efficiently use, ABGR-packed.
     * Most use won't directly involve this field, but there are various techniques SquidLib uses to boost
     * performance by treating a color as a float. This is mainly advantageous when many colors are involved
     * and objects shouldn't be instantiated many times. You may want to consider using
     * {@link SColor#lerpFloatColors(float, float, float)} if you expect to smoothly mix these float colors,
     * which avoids creating intermediate Color objects. There are more methods like that in SColor.
     */
    public float[][] colors;
    protected Color lightingColor = SColor.WHITE, tmpColor = new Color();
    protected TextCellFactory textFactory;
    protected float xOffset, yOffset, lightingFloat = SColor.FLOAT_WHITE;
    public OrderedSet<AnimatedEntity> animatedEntities;
    public OrderedSet<Actor> autoActors;
    /**
     * For thin-wall maps, where only cells where x and y are both even numbers have backgrounds displayed.
     * Should be false when using this SquidPanel for anything that isn't specifically a background of a map
     * that uses the thin-wall method from ThinDungeonGenerator or something similar. Even the foregrounds of
     * thin-wall maps should have this false, since ThinDungeonGenerator (in conjunction with DungeonUtility's
     * hashesToLines method) makes thin lines for walls that should be displayed as between the boundaries of
     * other cells. The overlap behavior needed for some "thin enough" cells to be displayed between the cells
     * can be accomplished by using {@link #setTextSize(float, float)} to double the previously-given cell width
     * and height.
     */
    public boolean onlyRenderEven = false;

    /**
     * Creates a bare-bones panel with all default values for text rendering.
     * <br>
     * This uses a default font that is not supplied in the JAR library of SquidLib; you need two files to use it if it
     * does not render correctly:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Zodiac-Square-12x12.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Zodiac-Square-12x12.png</li>
     * </ul>
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     */
    public SquidPanel(int gridWidth, int gridHeight) {
        this(gridWidth, gridHeight, new TextCellFactory().defaultSquareFont());
    }

    /**
     * Creates a panel with the given grid and cell size. Uses a default square font.
     * <br>
     * This uses a default font that is not supplied in the JAR library of SquidLib; you need two files to use it if it
     * does not render correctly:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Zodiac-Square-12x12.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Zodiac-Square-12x12.png</li>
     * </ul>
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param cellWidth the number of horizontal pixels in each cell
     * @param cellHeight the number of vertical pixels in each cell
     */
    public SquidPanel(int gridWidth, int gridHeight, int cellWidth, int cellHeight) {
        this(gridWidth, gridHeight, new TextCellFactory().defaultSquareFont().width(cellWidth).height(cellHeight).initBySize());
    }

    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions.
     *
     * If the TextCellFactory has not yet been initialized, then it will be sized at 12x12 px per cell. If it is null
     * then a default one will be created and initialized.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory the factory to use for cell rendering
     */
    public SquidPanel(int gridWidth, int gridHeight, TextCellFactory factory) {
        this(gridWidth, gridHeight, factory, DefaultResources.getSCC());
    }

    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions.
     *
     * If the TextCellFactory has not yet been initialized, then it will be sized at 12x12 px per cell. If it is null
     * then a default one will be created and initialized.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory the factory to use for cell rendering
     * @param center the color center to use. Can be {@code null}, which will use a default
     */
    public SquidPanel(int gridWidth, int gridHeight, TextCellFactory factory, IColorCenter<Color> center) {
        this(gridWidth, gridHeight, factory, center, 0f, 0f);
    }

    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions.
     * <br>
     * If the TextCellFactory has not yet been initialized, then it will be sized at 12x12 px per cell. If it is null
     * then a default one will be created and initialized. The xOffset and yOffset arguments are measured in pixels or
     * whatever sub-cell unit of measure your game uses (world coordinates, in libGDX parlance), and change where the
     * SquidPanel starts drawing by simply adding to the initial x and y coordinates. 0 and 0 are usually fine.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory the factory to use for cell rendering
     * @param center the color center to use. Can be {@code null}, which will use a default
     * @param xOffset the x offset to start rendering at, in pixels (or some other sub-cell measurement your game uses)
     * @param yOffset the y offset to start rendering at, in pixels (or some other sub-cell measurement your game uses)
     */
    public SquidPanel(int gridWidth, int gridHeight, TextCellFactory factory, IColorCenter<Color> center,
                      float xOffset, float yOffset) {
        this(gridWidth, gridHeight, factory, center, xOffset, yOffset, null);
    }
    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions. Importantly,
     * this constructor takes a 2D char array argument that can be sized differently than the displayed area. The
     * displayed area is gridWidth by gridHeight in cells, but the actualMap argument can be much larger, and only a
     * portion will be displayed at a time. This requires some special work with the Camera and Viewports to get working
     * correctly; in the squidlib module's examples, EverythingDemo may be a good place to see how this can be done.
     * You can pass null for actualMap, which will simply create a char array to use internally that is exactly
     * gridWidth by gridHeight, in cells.
     * <br>
     * If the TextCellFactory has not yet been initialized, then it will be sized at 12x12 px per cell. If it is null
     * then a default one will be created and initialized. The xOffset and yOffset arguments are measured in pixels or
     * whatever sub-cell unit of measure your game uses (world coordinates, in libGDX parlance), and change where the
     * SquidPanel starts drawing by simply adding to the initial x and y coordinates. 0 and 0 are usually fine.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory the factory to use for cell rendering
     * @param center the color center to use. Can be {@code null}, which will use a default
     * @param xOffset the x offset to start rendering at, in pixels (or some other sub-cell measurement your game uses)
     * @param yOffset the y offset to start rendering at, in pixels (or some other sub-cell measurement your game uses)
     * @param actualMap will often be a different size than gridWidth by gridHeight, which enables camera scrolling
     */
    public SquidPanel(int gridWidth, int gridHeight, TextCellFactory factory, IColorCenter<Color> center,
                      float xOffset, float yOffset, char[][] actualMap) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        if(center == null)
            scc = DefaultResources.getSCC();
        else
            scc = center;
        if (factory == null) {
            textFactory = new TextCellFactory();
        }
        else
            textFactory = factory;
        if (!textFactory.initialized()) {
            textFactory.initByFont();
        }

        cellWidth = MathUtils.round(textFactory.actualCellWidth);
        cellHeight = MathUtils.round(textFactory.actualCellHeight);

        if(actualMap == null || actualMap.length <= 0)
            contents = ArrayTools.fill('\0', gridWidth, gridHeight);
        else
            contents = actualMap;
        colors = ArrayTools.fill(-0x1.0p125F, contents.length, contents[0].length);

        int w = gridWidth * cellWidth;
        int h = gridHeight * cellHeight;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        setSize(w, h);
        animatedEntities = new OrderedSet<>();
        autoActors = new OrderedSet<>();
    }

    /**
     * Places the given characters into the grid starting at 0,0.
     *
     * @param chars
     */
    public void put(char[][] chars) {
        put(0, 0, chars);
    }

	@Override
	public void put(/* @Nullable */char[][] chars, Color[][] foregrounds) {
		if (chars == null) {
			/* Only colors to put */
			final int width = foregrounds.length;
			final int height = width == 0 ? 0 : foregrounds[0].length;
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++)
					put(x, y, foregrounds[x][y]);
			}
		} else
			put(0, 0, chars, foregrounds);
	}

    public void put(int xOffset, int yOffset, char[][] chars) {
        put(xOffset, yOffset, chars, defaultForeground);
    }

    public void put(int xOffset, int yOffset, char[][] chars, Color[][] foregrounds) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    put(x, y, chars[x - xOffset][y - yOffset], foregrounds[x - xOffset][y - yOffset]);
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, Color[][] foregrounds) {
        for (int x = xOffset; x < xOffset + foregrounds.length; x++) {
            for (int y = yOffset; y < yOffset + foregrounds[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    put(x, y, '\0', foregrounds[x - xOffset][y - yOffset]);
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, char[][] chars, Color foreground) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    put(x, y, chars[x - xOffset][y - yOffset], foreground);
                }
            }
        }
    }

    /**
     * Puts the given string horizontally with the first character at the given offset.
     *
     * Does not word wrap. Characters that are not renderable (due to being at negative offsets or offsets greater than
     * the grid size) will not be shown but will not cause any malfunctions.
     *
     * Will use the default color for this component to draw the characters.
     *
     * @param xOffset the x coordinate of the first character
     * @param yOffset the y coordinate of the first character
     * @param string the characters to be displayed
     */
    public void put(int xOffset, int yOffset, String string) {
        put(xOffset, yOffset, string, defaultForeground);
    }

	@Override
	public void put(int xOffset, int yOffset, IColoredString<? extends Color> cs) {
		int x = xOffset;
		for (IColoredString.Bucket<? extends Color> fragment : cs) {
			final String s = fragment.getText();
			final Color color = fragment.getColor();
			put(x, yOffset, s, color == null ? getDefaultForegroundColor() : scc.filter(color));
			x += s.length();
		}
	}

    @Override
    public void put(int xOffset, int yOffset, String string, Color foreground) {
        if(string == null || string.isEmpty())
            return;
        if (string.length() == 1) {
            put(xOffset, yOffset, string.charAt(0), scc.filter(foreground).toFloatBits());
        }
        else
        {
            float enc = scc.filter(foreground).toFloatBits();
            for (int i = 0; i < string.length(); i++) {
                put(xOffset + i, yOffset, string.charAt(i), enc);
            }
        }
    }
    public void put(int xOffset, int yOffset, String string, float encodedColor) {
        if(string == null || string.isEmpty())
            return;
        if (string.length() == 1) {
            put(xOffset, yOffset, string.charAt(0), encodedColor);
        }
        else
        {
            for (int i = 0; i < string.length(); i++) {
                put(xOffset + i, yOffset, string.charAt(i), encodedColor);
            }
        }
    }

    public void put(int xOffset, int yOffset, String string, Color foreground, float colorMultiplier) {
        if (string.length() == 1) {
            put(xOffset, yOffset, string.charAt(0), foreground, colorMultiplier);
        }
        else
        {
            for (int i = 0; i < string.length(); i++) {
                put(xOffset + i, yOffset, string.charAt(i), foreground, colorMultiplier);
            }
        }
    }

    public void put(int xOffset, int yOffset, String string, float encodedColor, float colorMultiplier) {
        if (string.length() == 1) {
            put(xOffset, yOffset, string.charAt(0), encodedColor, colorMultiplier);
        }
        else
        {
            for (int i = 0; i < string.length(); i++) {
                put(xOffset + i, yOffset, string.charAt(i), encodedColor, colorMultiplier);
            }
        }
    }

    public void put(int xOffset, int yOffset, char[][] chars, Color foreground, float colorMultiplier) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    put(x, y, chars[x - xOffset][y - yOffset], foreground, colorMultiplier);
                }
            }
        }
    }

    /**
     * Puts the given string horizontally or optionally vertically, with the first character at the given offset.
     *
     * Does not word wrap. Characters that are not renderable (due to being at negative offsets or offsets greater than
     * the grid size) will not be shown but will not cause any malfunctions.
     *
     * Will use the default color for this component to draw the characters.
     *
     * @param xOffset the x coordinate of the first character
     * @param yOffset the y coordinate of the first character
     * @param string the characters to be displayed
     * @param vertical true if the text should be written vertically, from top to bottom
     */
    public void put(int xOffset, int yOffset, String string, boolean vertical) {
        put(xOffset, yOffset, string, defaultForeground, vertical);
    }

    /**
     * Puts the given string horizontally or optionally vertically, with the first character at the given offset.
     *
     * Does not word wrap. Characters that are not renderable (due to being at negative offsets or offsets greater than
     * the grid size) will not be shown but will not cause any malfunctions.
     *
     * @param xOffset the x coordinate of the first character
     * @param yOffset the y coordinate of the first character
     * @param string the characters to be displayed
     * @param foreground the color to draw the characters
     * @param vertical true if the text should be written vertically, from top to bottom
     */
    public void put(int xOffset, int yOffset, String string, Color foreground, boolean vertical) {
        if (vertical) {
            for (int i = 0; i < string.length(); i++) {
                put(xOffset, yOffset + i, string.charAt(i), foreground);
            }
        } else {
            put(xOffset, yOffset, string, foreground);
        }
    }

    /**
     * Puts the given string in the chosen direction, with the first character shown (not necessarily the first in the
     * string) at the given offset. If you use {@link Direction#LEFT}, then this effectively reverses the String and
     * prints it with the last character of the String at the minimum-x position, which is the same position that the
     * first character would be if you printed normally or if you gave this RIGHT (x is xOffset, y is yOffset). Giving
     * UP acts similarly to LEFT, but has the last character at the minimum-y position and has the first character below
     * it. The diagonals act as you would expect, combining the behavior of one of UP or DOWN with one of LEFT or RIGHT.
     * <br>
     * Does not word wrap. Characters that are not renderable (due to being at negative offsets or offsets greater than
     * the grid size) will not be shown but will not cause any malfunctions.
     *
     * @param xOffset the x coordinate of the first character
     * @param yOffset the y coordinate of the first character
     * @param string the characters to be displayed
     * @param foreground the color to draw the characters
     * @param direction the direction the text should be written in, such as {@link Direction#RIGHT} for normal layout
     */
    public void put(int xOffset, int yOffset, String string, Color foreground, Direction direction) {
        float enc = scc.filter(foreground).toFloatBits();
        switch (direction)
        {
            case DOWN:
                for (int i = 0; i < string.length(); i++) {
                    put(xOffset, yOffset + i, string.charAt(i), enc);
                }
                break;
            case UP:
                for (int i = 0, p = string.length() - 1; i < string.length(); i++, p--) {
                    put(xOffset, yOffset + p, string.charAt(i), enc);
                }
                break;
            case LEFT:
                for (int i = 0, p = string.length() - 1; i < string.length(); i++, p--) {
                    put(xOffset + p, yOffset, string.charAt(i), enc);
                }
                break;
            case DOWN_RIGHT:
                for (int i = 0; i < string.length(); i++) {
                    put(xOffset + i, yOffset + i, string.charAt(i), enc);
                }
                break;
            case UP_RIGHT:
                for (int i = 0, p = string.length() - 1; i < string.length(); i++, p--) {
                    put(xOffset + i, yOffset + p, string.charAt(i), enc);
                }
                break;
            case UP_LEFT:
                for (int i = 0, p = string.length() - 1; i < string.length(); i++, p--) {
                    put(xOffset + p, yOffset + p, string.charAt(i), enc);
                }
                break;
            case DOWN_LEFT:
                for (int i = 0, p = string.length() - 1; i < string.length(); i++, p--) {
                    put(xOffset + p, yOffset + i, string.charAt(i), enc);
                }
                break;
            default:
                for (int i = 0; i < string.length(); i++) {
                    put(xOffset + i, yOffset, string.charAt(i), enc);
                }
        }
    }

    /**
     * Changes the chars at the edge of the SquidPanel to be a border drawn with box drawing characters in white.
     */
    public void putBorders()
    {
        putBorders(SColor.FLOAT_WHITE, null);
    }
    /**
     * Changes the chars at the edge of the SquidPanel to be a border drawn with box drawing characters in the given
     * Color, which will be run through any IColorCenter this has for filtering. If caption is non-null, then this puts
     * that String starting at x=1, y=0.
     * @param color a libGDX Color to use for the borders
     * @param caption an optional caption that will be drawn at (1, 0). May be null.
     * @see #putBordersCaptioned(Color, IColoredString) Another method that takes an IColoredString caption
     */
    public void putBorders(Color color, String caption)
    {
        putBorders(scc.filter(color).toFloatBits(), caption);
    }
    /**
     * Changes the chars at the edge of the SquidPanel to be a border drawn with box drawing characters in the given
     * Color, which will be run through any IColorCenter this has for filtering.
     * @param color a libGDX Color to use for the borders
     */
    public void putBorders(Color color)
    {
	putBorders(color, null);
    }
    /**
     * Changes the chars at the edge of the SquidPanel to be a border drawn with box drawing characters in the given
     * color as a packed float.
     * @param encodedColor a packed float color to use for the borders, as from {@link Color#toFloatBits()}
     */
    public void putBorders(float encodedColor)
    {
        putBorders(encodedColor, null);
    }
    /**
     * Changes the chars at the edge of the SquidPanel to be a border drawn with box drawing characters in the given
     * color as a packed float. If caption is non-null, then this puts that String starting at x=1, y=0.
     * @param encodedColor a packed float color to use for the borders, as from {@link Color#toFloatBits()}
     * @param caption an optional caption that will be drawn at (1, 0). May be null to have no caption.
     * @see #putBordersCaptioned(float, IColoredString) Another method that takes an IColoredString caption
     */
    public void putBorders(float encodedColor, String caption)
    {
        contents[0][0] = '┌';
        contents[gridWidth - 1][0] = '┐';
        contents[0][gridHeight - 1] = '└';
        contents[gridWidth - 1][gridHeight - 1] = '┘';
        for (int i = 1; i < gridWidth - 1; i++) {
            contents[i][0] = '─';
            contents[i][gridHeight - 1] = '─';
        }
        for (int y = 1; y < gridHeight - 1; y++) {
            contents[0][y] = '│';
            contents[gridWidth - 1][y] = '│';
            colors[0][y] = encodedColor;
            colors[gridWidth - 1][y] = encodedColor;
        }
        for (int y = 1; y < gridHeight - 1; y++) {
            for (int x = 1; x < gridWidth - 1; x++) {
                contents[x][y] = ' ';
                contents[x][y] = ' ';
            }
        }
        for (int i = 0; i < gridWidth; i++) {
            colors[i][0] = encodedColor;
            colors[i][gridHeight - 1] = encodedColor;
        }

        if (caption != null) {
            put(1, 0, caption, encodedColor);
        }
    }

    /**
     * Changes the chars at the edge of the SquidPanel to be a border drawn with box drawing characters in the given
     * libGDX Color. If caption is non-null, then this puts that IColoredString starting at x=1, y=0.
     * @param color a libGDX Color to use for the borders
     * @param caption an optional caption as an IColoredString that will be drawn at (1, 0). May be null to have no
     *                caption. Will be colored independently from the border lines.
     */
    public void putBordersCaptioned(Color color, IColoredString<Color> caption)
    {
        putBordersCaptioned(scc.filter(color).toFloatBits(), caption);
    }

    /**
     * Changes the chars at the edge of the SquidPanel to be a border drawn with box drawing characters in the given
     * color as a packed float. If caption is non-null, then this puts that IColoredString starting at x=1, y=0.
     * @param encodedColor a packed float color to use for the borders, as from {@link Color#toFloatBits()}
     * @param caption an optional caption as an IColoredString that will be drawn at (1, 0). May be null to have no
     *                caption. Will be colored independently from the border lines.
     */
    public void putBordersCaptioned(float encodedColor, IColoredString<Color> caption)

    {
        contents[0][0] = '┌';
        contents[gridWidth - 1][0] = '┐';
        contents[0][gridHeight - 1] = '└';
        contents[gridWidth - 1][gridHeight - 1] = '┘';
        for (int i = 1; i < gridWidth - 1; i++) {
            contents[i][0] = '─';
            contents[i][gridHeight - 1] = '─';
        }
        for (int y = 1; y < gridHeight - 1; y++) {
            contents[0][y] = '│';
            contents[gridWidth - 1][y] = '│';
            colors[0][y] = encodedColor;
            colors[gridWidth - 1][y] = encodedColor;
        }
        for (int y = 1; y < gridHeight - 1; y++) {
            for (int x = 1; x < gridWidth - 1; x++) {
                contents[x][y] = ' ';
                contents[x][y] = ' ';
            }
        }
        for (int i = 0; i < gridWidth; i++) {
            colors[i][0] = encodedColor;
            colors[i][gridHeight - 1] = encodedColor;
        }

        if (caption != null) {
            put(1, 0, caption);
        }

    }

    /**
     * Erases the entire panel, leaving only a transparent space.
     */
    public void erase() {
        ArrayTools.fill(contents, ' ');
        ArrayTools.fill(colors, 0f);
    }

    @Override
	public void clear(int x, int y) {
        put(x, y, ' ', 0f);
    }

    @Override
    public void put(int x, int y, Color color) {
        put(x, y, '\0', color);
    }

    public void put(int x, int y, float encodedColor) {
        put(x, y, '\0', encodedColor);
    }

    public void put(int x, int y, float encodedColor, float colorMultiplier) {
        put(x, y, '\0', encodedColor, colorMultiplier);
    }

    public void put(int x, int y, float encodedColor, float colorMultiplier, float mixColor) {
        put(x, y, '\0', encodedColor, colorMultiplier, mixColor);
    }
    @Override
    public void blend(int x, int y, float color, float mixBy)
    {
        colors[x][y] = SColor.lerpFloatColorsBlended(colors[x][y], color, mixBy);
    }

    public void put(int x, int y, Color color, float colorMultiplier) {
        put(x, y, '\0', color, colorMultiplier);
    }

    public void put(int x, int y, Color color, float mixAmount, Color mix) {
        put(x, y, '\0', color, mixAmount, mix);
    }

    @Override
	public void put(int x, int y, char c) {
        put(x, y, c, defaultForeground);
    }

    /**
     * Takes a unicode codepoint for input.
     *
     * @param x
     * @param y
     * @param code
     */
    public void put(int x, int y, int code) {
        put(x, y, code, defaultForeground);
    }

    public void put(int x, int y, int c, Color color) {
        put(x, y, (char) c, color);
    }

    /**
     * Takes a unicode char for input.
     *
     * @param x
     * @param y
     * @param c
     * @param color
     */
    @Override
    public void put(int x, int y, char c, Color color) {
        if (x < 0 || x >= contents.length || y < 0 || y >= contents[0].length) {
            return;//skip if out of bounds
        }
        contents[x][y] = c;
        colors[x][y] = scc.filter(color).toFloatBits();
    }
    /**
     * Takes a unicode char for input.
     *
     * @param x
     * @param y
     * @param c
     * @param encodedColor a float color as produced by {@link SColor#floatGet(float, float, float, float)}
     */
    public void put(int x, int y, char c, float encodedColor) {
        if (x < 0 || x >= contents.length || y < 0 || y >= contents[0].length) {
            return;//skip if out of bounds
        }
        contents[x][y] = c;
        colors[x][y] = encodedColor;
    }

    /**
     * Takes a unicode char for input and a color multiplier that determines how much of {@link #lightingColor} will
     * affect the given encodedColor. The encodedColor is a float that might be produced by {@link Color#toFloatBits()}
     * or by mixing multiple such floats with {@link SColor#lerpFloatColors(float, float, float)}.
     *
     * @param x
     * @param y
     * @param c
     * @param encodedColor a float color as produced by {@link SColor#floatGet(float, float, float, float)}
     * @param colorMultiplier how much of {@link #lightingColor} to use in place of encodedColor, from 0.0 to 1.0
     */
    public void put(int x, int y, char c, float encodedColor, float colorMultiplier) {
        if (x < 0 || x >= contents.length || y < 0 || y >= contents[0].length) {
            return;//skip if out of bounds
        }
        contents[x][y] = c;
        colors[x][y] = SColor.lerpFloatColors(encodedColor, lightingFloat, colorMultiplier);
    }

    /**
     * Intended for colored lighting; takes a unicode char for input and a color multiplier that determines how much of
     * mixColor will affect encodedColor. Both encodedColor and mixColor are floats that might be produced by
     * {@link Color#toFloatBits()} or by mixing multiple such floats with
     * {@link SColor#lerpFloatColors(float, float, float)}; colorMultiplier is a normal float between 0.0f and 1.0f .
     *
     * @param x
     * @param y
     * @param c
     * @param encodedColor a float color as produced by {@link SColor#floatGet(float, float, float, float)}
     * @param colorMultiplier how much of mixColor to use in place of encodedColor, from 0.0 to 1.0
     * @param mixColor a color to mix with encodedColor, typically as part of colored lighting
     */
    public void put(int x, int y, char c, float encodedColor, float colorMultiplier, float mixColor) {
        if (x < 0 || x >= contents.length || y < 0 || y >= contents[0].length) {
            return;//skip if out of bounds
        }
        contents[x][y] = c;
        colors[x][y] = SColor.lerpFloatColors(encodedColor, mixColor, colorMultiplier);
    }

    /**
     * Puts the given character at position x, y, with its color determined by the given color interpolated with
     * this SquidPanel's lightingColor (default is white light) by the amount specified by colorMultiplier (0.0
     * causes no change to the given color, 1.0 uses the lightingColor only, and anything between 0 and 1 will
     * produce some tint to color, and probably cache the produced color in the IColorCenter this uses).
     */
    public void put(int x, int y, char c, Color color, float colorMultiplier) {
        if (x < 0 || x >= contents.length || y < 0 || y >= contents[0].length) {
            return;//skip if out of bounds
        }
        contents[x][y] = c;
        colors[x][y] = scc.lerp(color, lightingColor, colorMultiplier).toFloatBits();
    }

    /**
     * Puts the given character at position x, y, with its color determined by the given color interpolated with
     * the given mix color by the amount specified by mixAmount (0.0 causes no change to the given color, 1.0 uses mix
     * only, and anything between 0 and 1 will produce some tint to color, and probably cache the produced color in the
     * IColorCenter this uses).
     * <br>
     * Note, unlike {@link #put(int, int, char, float, float, float)}, this will use the IColorCenter to produce the
     * finished color, which may be slightly slower if you don't need any of IColorCenter's features, and will use
     * more memory if many colors are cached, but has the advantage of being able to adjust colors with filters.
     */
    public void put(int x, int y, char c, Color color, float mixAmount, Color mix) {
        if (x < 0 || x >= contents.length || y < 0 || y >= contents[0].length) {
            return;//skip if out of bounds
        }
        contents[x][y] = c;
        colors[x][y] = scc.lerp(color, mix, mixAmount).toFloatBits();
    }

    @Override
	public int cellWidth() {
        return cellWidth;
    }

    @Override
	public int cellHeight() {
        return cellHeight;
    }

    @Override
	public int gridHeight() {
        return gridHeight;
    }

    @Override
	public int gridWidth() {
        return gridWidth;
    }

	/**
	 * @return The {@link TextCellFactory} backing {@code this}.
	 */
	public TextCellFactory getTextCellFactory() {
		return textFactory;
	}

    /**
     * Sets the size of the text in this SquidPanel (but not the size of the cells) to the given width and height in
     * pixels (which may be stretched by viewports later on, if your program uses them).
     * @param wide the width of a glyph in pixels
     * @param high the height of a glyph in pixels
     * @return this for chaining
     */
    public SquidPanel setTextSize(float wide, float high)
    {
        textFactory.tweakHeight(high).tweakWidth(wide).initBySize();
        //textFactory.setSmoothingMultiplier((3f + Math.max(cellWidth * 1f / wide, cellHeight * 1f / high)) / 4f);
        return this;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        textFactory.configureShader(batch);
        int inc = onlyRenderEven ? 2 : 1, widthInc = inc * cellWidth, heightInc = inc * cellHeight;
        float screenX = xOffset - (gridOffsetX <= 0 ? 0 : cellWidth) + getX(),
                screenY_base = 1f + yOffset + (gridOffsetY <= 0 ? 0 : cellHeight) + gridHeight * cellHeight + getY(), screenY;
        for (int x = Math.max(0, gridOffsetX-1), xx = (gridOffsetX <= 0) ? 0 : -1; xx <= gridWidth && x < contents.length; x += inc, xx += inc, screenX += widthInc) {
            screenY = screenY_base;
            for (int y = Math.max(0, gridOffsetY-1), yy = (gridOffsetY <= 0) ? 0 : -1; yy <= gridHeight && y < contents[x].length; y += inc, yy += inc, screenY -= heightInc) {
                textFactory.draw(batch, contents[x][y],
                        colors[x][y],
                        screenX,// xOffset + /*- getX() + 1f * */ x * cellWidth,
                        screenY // yOffset + /*- getY() + 1f * */ (gridHeight - y) * cellHeight + 1f
                );
            }
        }
        super.draw(batch, parentAlpha);
        int len = animatedEntities.size();
        for (int i = 0; i < len; i++) {
            animatedEntities.getAt(i).actor.act(Gdx.graphics.getDeltaTime());
        }
        len = autoActors.size();
        Actor a;
        for (int i = 0; i < len; i++) {
            a = autoActors.getAt(i);
            if(a == null) continue;
            if(a.isVisible())
                drawActor(batch, parentAlpha, a);
            a.act(Gdx.graphics.getDeltaTime());
        }
    }

    /**
     * Draws one AnimatedEntity, specifically the Actor it contains. Batch must be between start() and end()
     * @param batch Must have start() called already but not stop() yet during this frame.
     * @param parentAlpha This can be assumed to be 1.0f if you don't know it
     * @param ae The AnimatedEntity to draw; the position to draw ae is stored inside it.
     */
    public void drawActor(Batch batch, float parentAlpha, AnimatedEntity ae)
    {
        drawActor(batch, parentAlpha, ae.actor);
        /*
        float prevX = ae.actor.getX(), prevY = ae.actor.getY();
        ae.actor.setPosition(prevX - gridOffsetX * cellWidth, prevY + gridOffsetY * cellHeight);
        ae.actor.draw(batch, parentAlpha);
        ae.actor.setPosition(prevX, prevY);
        */
    }

    /**
     * Draws one AnimatedEntity, specifically the Actor it contains. Batch must be between start() and end()
     * @param batch Must have start() called already but not stop() yet during this frame.
     * @param parentAlpha This can be assumed to be 1.0f if you don't know it
     * @param ac The Actor to draw; the position to draw ac is modified and reset based on some fields of this object
     */
    public void drawActor(Batch batch, float parentAlpha, Actor ac)
    {
        float prevX = ac.getX(), prevY = ac.getY();
        ac.setPosition(prevX - gridOffsetX * cellWidth, prevY + gridOffsetY * cellHeight);
        ac.draw(batch, parentAlpha);
        ac.setPosition(prevX, prevY);
    }

    @Override
	public void setDefaultForeground(Color defaultForeground) {
        this.defaultForeground = defaultForeground;
    }

	@Override
	public Color getDefaultForegroundColor() {
		return defaultForeground;
	}

    public AnimatedEntity getAnimatedEntityByCell(int x, int y) {
        for(AnimatedEntity ae : animatedEntities)
        {
            if(ae.gridX == x && ae.gridY == y)
                return ae;
        }
        return  null;
    }

    /**
     * Create an AnimatedEntity at position x, y, using the char c in the given color.
     * @param x
     * @param y
     * @param c
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, char c, Color color)
    {
        return animateActor(x, y, false, c, color);
        /*
        Actor a = textFactory.makeActor("" + c, color);
        a.setName("" + c);
        a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());

        AnimatedEntity ae = new AnimatedEntity(a, x, y);
        animatedEntities.add(ae);
        return ae;
        */
    }

    /**
     * Create an AnimatedEntity at position x, y, using the char c in the given color. If doubleWidth is true, treats
     * the char c as the left char to be placed in a grid of 2-char cells.
     * @param x
     * @param y
     * @param doubleWidth
     * @param c
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, char c, Color color)
    {
        Actor a = textFactory.makeActor(c, color);
        a.setName(String.valueOf(c));
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;

        /*
        Actor a = textFactory.makeActor("" + c, color);
        a.setName("" + c);
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());

        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
        */
    }

    /**
     * Create an AnimatedEntity at position x, y, using the String s in the given color.
     * @param x
     * @param y
     * @param s
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, String s, Color color)
    {
        return animateActor(x, y, false, s, color);
    }

    /**
     * Create an AnimatedEntity at position x, y, using the String s in the given color. If doubleWidth is true, treats
     * the String s as starting in the left cell of a pair to be placed in a grid of 2-char cells.
     * @param x
     * @param y
     * @param doubleWidth
     * @param s
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, String s, Color color)
    {
        Actor a = textFactory.makeActor(s, color);
        a.setName(s);
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    /**
     * Create an AnimatedEntity at position x, y, using the String s in the given color. If doubleWidth is true, treats
     * the String s as starting in the left cell of a pair to be placed in a grid of 2-char cells.
     * @param x
     * @param y
     * @param doubleWidth
     * @param s
     * @param colors
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, String s, Collection<Color> colors)
    {
        return animateActor(x, y, doubleWidth, s, colors, 2f);
    }
    /**
     * Create an AnimatedEntity at position x, y, using the String s in the given color. If doubleWidth is true, treats
     * the String s as starting in the left cell of a pair to be placed in a grid of 2-char cells.
     * @param x
     * @param y
     * @param doubleWidth
     * @param s
     * @param colors
     * @param loopTime
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, String s, Collection<Color> colors, float loopTime)
    {
        Actor a = textFactory.makeActor(s, colors, loopTime, doubleWidth);
        a.setName(s);
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    /**
     * Create an AnimatedEntity at position x, y, using '^' as its contents, but as an image so it can be rotated.
     * Uses the given colors in a looping pattern, that doesn't count as an animation. If doubleWidth is true, treats
     * the '^' as starting in the middle of a 2-char cell.
     * @param x
     * @param y
     * @param doubleWidth
     * @param colors
     * @param loopTime
     * @return
     */
    public AnimatedEntity directionMarker(int x, int y, boolean doubleWidth, Collection<Color> colors, float loopTime)
    {
        Actor a = textFactory.makeDirectionMarker(colors, loopTime, doubleWidth);
        a.setName("^");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    public AnimatedEntity directionMarker(int x, int y, boolean doubleWidth, Color color)
    {
        Actor a = textFactory.makeDirectionMarker(color);
        a.setName("^");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with no color modifications, which will be
     * stretched to fit one cell.
     * @param x
     * @param y
     * @param texture
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, TextureRegion texture)
    {
        return animateActor(x, y, false, texture, Color.WHITE);
        /*
        Actor a = textFactory.makeActor(texture, Color.WHITE);
        a.setName("");
        a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());

        AnimatedEntity ae = new AnimatedEntity(a, x, y);
        animatedEntities.add(ae);
        return ae;
        */
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which will be
     * stretched to fit one cell.
     * @param x
     * @param y
     * @param texture
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, TextureRegion texture, Color color)
    {
        return animateActor(x, y, false, texture, color);
        /*
        Actor a = textFactory.makeActor(texture, color);
        a.setName("");
        a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());

        AnimatedEntity ae = new AnimatedEntity(a, x, y);
        animatedEntities.add(ae);
        return ae;
        */
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with no color modifications, which will be
     * stretched to fit one cell, or two cells if doubleWidth is true.
     * @param x
     * @param y
     * @param doubleWidth
     * @param texture
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, TextureRegion texture)
    {
        return animateActor(x, y, doubleWidth, texture, Color.WHITE);
        /*
        Actor a = textFactory.makeActor(texture, Color.WHITE, (doubleWidth ? 2 : 1) * cellWidth, cellHeight);

        a.setName("");
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());

        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
        */
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which will be
     * stretched to fit one cell, or two cells if doubleWidth is true.
     * @param x
     * @param y
     * @param doubleWidth
     * @param texture
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, TextureRegion texture, Color color) {
        Actor a = textFactory.makeActor(texture, color, (doubleWidth ? 2 : 1) * cellWidth, cellHeight);
        a.setName("");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        /*
        if (doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        */
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which will be
     * stretched to fit one cell, or two cells if doubleWidth is true.
     * @param x
     * @param y
     * @param doubleWidth
     * @param texture
     * @param colors
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, TextureRegion texture, Collection<Color> colors){
        return animateActor(x, y, doubleWidth, texture, colors, 2f);
    }
    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which will be
     * stretched to fit one cell, or two cells if doubleWidth is true.
     * @param x
     * @param y
     * @param doubleWidth
     * @param texture
     * @param colors
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, TextureRegion texture, Collection<Color> colors, float loopTime) {
        Actor a = textFactory.makeActor(texture, colors, loopTime, doubleWidth, (doubleWidth ? 2 : 1) * cellWidth, cellHeight);
        a.setName("");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        /*
        if (doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        */
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with no color modifications, which, if and only
     * if stretch is true, will be stretched to fit one cell, or two cells if doubleWidth is true. If stretch is false,
     * this will preserve the existing size of texture.
     * @param x
     * @param y
     * @param doubleWidth
     * @param stretch
     * @param texture
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, boolean stretch, TextureRegion texture)
    {
        Actor a = (stretch)
                ? textFactory.makeActor(texture, Color.WHITE, (doubleWidth ? 2 : 1) * cellWidth, cellHeight)
                : textFactory.makeActor(texture, Color.WHITE, texture.getRegionWidth(), texture.getRegionHeight());
        a.setName("");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        /*
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight  - textFactory.getDescent() + getY());
        */
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which, if and only
     * if stretch is true, will be stretched to fit one cell, or two cells if doubleWidth is true. If stretch is false,
     * this will preserve the existing size of texture.
     * @param x
     * @param y
     * @param doubleWidth
     * @param stretch
     * @param texture
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, boolean stretch, TextureRegion texture, Color color) {

        Actor a = (stretch)
                ? textFactory.makeActor(texture, color, (doubleWidth ? 2 : 1) * cellWidth, cellHeight)
                : textFactory.makeActor(texture, color, texture.getRegionWidth(), texture.getRegionHeight());
        a.setName("");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        /*
        if (doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight  - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight  - textFactory.getDescent() + getY());
            */
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }

    /**
     * Created an Actor from the contents of the given x,y position on the grid.
     * @param x
     * @param y
     * @return
     */
    public Actor cellToActor(int x, int y)
    {
        return cellToActor(x, y, false);
    }

    /**
     * Created an Actor from the contents of the given x,y position on the grid; deleting
     * the grid's String content at this cell.
     * 
     * @param x
     * @param y
     * @param doubleWidth
     * @return A fresh {@link Actor}, that has just been added to {@code this}.
     */
    public Actor cellToActor(int x, int y, boolean doubleWidth)
    {
    	return createActor(x, y, contents[x][y], colors[x][y], doubleWidth);
    }

    protected /* @Nullable */ Actor createActor(int x, int y, char name, Color color, boolean doubleWidth) {
        final Actor a = textFactory.makeActor(name, scc.filter(color));
        a.setName(String.valueOf(name));
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        autoActors.add(a);
        return a;
    }

    protected /* @Nullable */ Actor createActor(int x, int y, char name, float encodedColor, boolean doubleWidth) {
        final Actor a = textFactory.makeActor(name, encodedColor);
        a.setName(String.valueOf(name));
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        autoActors.add(a);
        return a;
    }

    public float adjustX(float x, boolean doubleWidth)
    {
        if(doubleWidth)
            return (x - gridOffsetX) * 2 * cellWidth + getX();
        else
            return (x) * cellWidth + getX();
    }

    public float adjustY(float y)
    {
        return (gridHeight - y - 1) * cellHeight + getY() + (textFactory.msdf ? -textFactory.descent : 1 + cellHeight - textFactory.actualCellHeight); // - textFactory.lineHeight //textFactory.lineTweak * 3f
        //return (gridHeight - y - 1) * cellHeight + textFactory.getDescent() * 3 / 2f + getY();
    }

    /*
    public void startAnimation(Actor a, int oldX, int oldY)
    {
        Coord tmp = Coord.get(oldX, oldY);

        tmp.x = Math.round(a.getX() / cellWidth);
        tmp.y = gridHeight - Math.round(a.getY() / cellHeight) - 1;
        if(tmp.x >= 0 && tmp.x < gridWidth && tmp.y > 0 && tmp.y < gridHeight)
        {
        }
    }
    */
    public void recallActor(Actor a, boolean restoreSym)
    {
        animationCount--;
        int x = Math.round((a.getX() - getX()) / cellWidth) + gridOffsetX,
                y = gridHeight - (int)(a.getY() / cellHeight) - 1 + gridOffsetY;
        if(onlyRenderEven)
        {
            // this just sets the least significant bit to 0, making any odd numbers even (decrementing)
            x &= -2;
            y &= -2;
        }
        String n;
        if(restoreSym && x >= 0 && y >= 0 && x < contents.length && y < contents[x].length
                && (n = a.getName()) != null && !n.isEmpty())
        {
                contents[x][y] = n.charAt(0);
        }
        removeActor(a);
        autoActors.remove(a);
    }

    public void recallActor(Actor a, boolean restoreSym, int nextX, int nextY)
    {
        animationCount--;
        if(onlyRenderEven)
        {
            // this just sets the least significant bit to 0, making any odd numbers even (decrementing)
            nextX &= -2;
            nextY &= -2;
        }
        String n;
        if(restoreSym && nextX >= 0 && nextY >= 0 && nextX < contents.length && nextY < contents[nextX].length
                && (n = a.getName()) != null && !n.isEmpty())
        {
            contents[nextX][nextY] = n.charAt(0);
        }
        removeActor(a);
        autoActors.remove(a);
    }

    public void recallActor(AnimatedEntity ae)
    {
        if(ae.doubleWidth)
            ae.gridX = Math.round((ae.actor.getX() - getX()) / (2 * cellWidth)) + gridOffsetX;
        else
            ae.gridX = Math.round((ae.actor.getX() - getX()) / cellWidth) + gridOffsetY;
        ae.gridY = gridHeight - (int)((ae.actor.getY() - getY()) / cellHeight) - 1 + gridOffsetY;
        if(onlyRenderEven)
        {
            // this just sets the least significant bit to 0, making any odd numbers even (decrementing)
            ae.gridX &= -2;
            ae.gridY &= -2;
        }
        ae.animating = false;
        animationCount--;
    }
    public void recallActor(AnimatedEntity ae, int nextX, int nextY)
    {
        ae.gridX = nextX;
        ae.gridY = nextY;
        if(onlyRenderEven)
        {
            // this just sets the least significant bit to 0, making any odd numbers even (decrementing)
            ae.gridX &= -2;
            ae.gridY &= -2;
        }
        //fixPosition(ae);
        ae.animating = false;
        animationCount--;
    }

    public void fixPosition(AnimatedEntity ae)
    {
        ae.actor.setPosition(adjustX(ae.gridX, ae.doubleWidth), adjustY(ae.gridY));
    }
    public void fixPositions()
    {
        for (int i = 0; i < animatedEntities.size(); i++) {
            AnimatedEntity ae = animatedEntities.getAt(i);
            ae.actor.setPosition(adjustX(ae.gridX, ae.doubleWidth), adjustY(ae.gridY));
        }
    }

    /**
     * Start a bumping animation in the given direction that will last duration seconds.
     * @param ae an AnimatedEntity returned by animateActor()
     * @param direction
     * @param duration a float, measured in seconds, for how long the animation should last; commonly 0.12f
     */
    public void bump(final AnimatedEntity ae, Direction direction, float duration)
    {
        final Actor a = ae.actor;
        final float x = adjustX(ae.gridX, ae.doubleWidth),
                y = adjustY(ae.gridY);
        if(a == null || ae.animating) return;
        duration = clampDuration(duration);
        animationCount++;
        ae.animating = true;
        a.addAction(Actions.sequence(
                Actions.moveToAligned(x + direction.deltaX * cellWidth * 0.35f, y - direction.deltaY * cellHeight * 0.35f,
                        Align.bottomLeft, duration * 0.35F),
                Actions.moveToAligned(x, y, Align.bottomLeft, duration * 0.65F),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(ae, ae.gridX, ae.gridY);
                    }
                }))));

    }
    /**
     * Start a bumping animation in the given direction that will last duration seconds.
     * @param x
     * @param y
     * @param direction
     * @param duration a float, measured in seconds, for how long the animation should last; commonly 0.12f
     */
    public void bump(final int x, final int y, Direction direction, float duration)
    {
        final Actor a = cellToActor(x, y);
        if(a == null) return;
        duration = clampDuration(duration);
        animationCount++;
        float nextX = adjustX(x, false), nextY = adjustY(y);
        /*
        x *= cellWidth;
        y = (gridHeight - y - 1);
        y *= cellHeight;
        y -= 1;
        x +=  getX();
        y +=  getY();
        */
        a.addAction(Actions.sequence(
                Actions.moveToAligned(nextX + direction.deltaX * cellWidth * 0.35f, nextY + direction.deltaY * cellHeight * 0.35f,
                        Align.bottomLeft, duration * 0.35F),
                Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration * 0.65F),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(a, true, x, y);
                    }
                }))));

    }

    /**
     * Starts a bumping animation in the direction provided.
     *
     * @param x
     * @param y
     * @param direction
     */
    public void bump(int x, int y, Direction direction) {
        bump(x, y, direction, DEFAULT_ANIMATION_DURATION);
    }
    /**
     * Starts a bumping animation in the direction provided.
     *
     * @param location
     * @param direction
     */
    public void bump(Coord location, Direction direction) {
        bump(location.x, location.y, direction, DEFAULT_ANIMATION_DURATION);
    }
    /**
     * Start a movement animation for the object at the grid location x, y and moves it to newX, newY over a number of
     * seconds given by duration (often 0.12f or somewhere around there).
     * @param ae an AnimatedEntity returned by animateActor()
     * @param newX
     * @param newY
     * @param duration
     */
    public void slide(final AnimatedEntity ae, final int newX, final int newY, float duration)
    {
        final Actor a = ae.actor;
        final float nextX = adjustX(newX, ae.doubleWidth), nextY = adjustY(newY);
        if(a == null || ae.animating) return;
        duration = clampDuration(duration);
        animationCount++;
        ae.animating = true;
        ae.gridX = newX;
        ae.gridY = newY;
        a.addAction(Actions.sequence(
                Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(ae, newX, newY);
                    }
                }))));
    }

    /**
     * Start a movement animation for the object at the grid location x, y and moves it to newX, newY over a number of
     * seconds given by duration (often 0.12f or somewhere around there).
     * @param x
     * @param y
     * @param newX
     * @param newY
     * @param duration
     */
    public void slide(int x, int y, final int newX, final int newY, float duration)
    {
        final Actor a = cellToActor(x, y);
        if(a == null) return;
        duration = clampDuration(duration);
        animationCount++;
        float nextX = adjustX(newX, false), nextY = adjustY(newY);

        /*
        newX *= cellWidth;
        newY = (gridHeight - newY - 1);
        newY *= cellHeight;
        newY -= 1;
        x += getX();
        y += getY();
        */
        a.addAction(Actions.sequence(
                Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(a, true, newX, newY);
                    }
                }))));
    }

	/**
	 * Slides {@code name} from {@code (x,y)} to {@code (newx, newy)}. If
	 * {@code name} or {@code
	 * color} is {@code null}, it is picked from this panel (hereby removing the
	 * current name, if any).
	 * 
	 * @param x
	 *            Where to start the slide, horizontally.
	 * @param y
	 *            Where to start the slide, vertically.
	 * @param name
	 *            The name to slide, or {@code null} to pick it from this
	 *            panel's {@code (x,y)} cell.
	 * @param color
	 *            The color to use, or {@code null} to pick it from this panel's
	 *            {@code (x,y)} cell.
	 * @param newX
	 *            Where to end the slide, horizontally.
	 * @param newY
	 *            Where to end the slide, vertically.
	 * @param duration
	 *            The animation's duration.
	 */
	public void slide(int x, int y, final /* @Nullable */ String name, /* @Nullable */ Color color, int newX,
			int newY, float duration) {
	    slide(x, y, name, color, newX, newY, duration, null);
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
     * @param name
     *            The name to slide, or {@code null} to pick it from this
     *            panel's {@code (x,y)} cell.
     * @param color
     *            The color to use, or {@code null} to pick it from this panel's
     *            {@code (x,y)} cell.
     * @param newX
     *            Where to end the slide, horizontally.
     * @param newY
     *            Where to end the slide, vertically.
     * @param duration
     *            The animation's duration.
     * @param postRunnable a Runnable to execute after the slide completes; may be null to do nothing.
     */
    public void slide(int x, int y, final /* @Nullable */ String name, /* @Nullable */ Color color, final int newX,
                      final int newY, float duration, /* @Nullable */ Runnable postRunnable) {
        if(name != null && name.isEmpty())
            return;
        final Actor a = createActor(x, y, name == null ? contents[x][y] : name.charAt(0),
                color == null ? SColor.colorFromFloat(tmpColor, colors[x][y]) : color, false);
        if (a == null)
            return;

        duration = clampDuration(duration);
        animationCount++;

        final int nbActions = 2 + (postRunnable == null ? 0 : 1);

        int index = 0;
        final Action[] sequence = new Action[nbActions];
        final float nextX = adjustX(newX, false);
        final float nextY = adjustY(newY);
        sequence[index++] = Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration);
        if(postRunnable != null)
        {
            sequence[index++] = Actions.run(postRunnable);
        }
		/* Do this one last, so that hasActiveAnimations() returns true during 'postRunnables' */
        sequence[index] = Actions.delay(duration, Actions.run(new Runnable() {
            @Override
            public void run() {
                recallActor(a, name == null, newX, newY);
            }
        }));

        a.addAction(Actions.sequence(sequence));
    }


    /**
     * Starts a movement animation for the object at the given grid location at the default speed.
     *
     * @param start Coord to pick up a tile from and slide
     * @param end Coord to end the slide on
     */
    public void slide(Coord start, Coord end) {
        slide(start.x, start.y, end.x, end.y, DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Starts a movement animation for the object at the given grid location at the default speed for one grid square in
     * the direction provided.
     *
     * @param start Coord to pick up a tile from and slide
     * @param direction Direction enum that indicates which way the slide should go
     */
    public void slide(Coord start, Direction direction) {
        slide(start.x, start.y, start.x + direction.deltaX, start.y + direction.deltaY, DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Starts a sliding movement animation for the object at the given location at the provided speed. The duration is
     * how many seconds should pass for the entire animation.
     *
     * @param start Coord to pick up a tile from and slide
     * @param end Coord to end the slide on
     * @param duration in seconds, as a float
     */
    public void slide(Coord start, Coord end, float duration) {
        slide(start.x, start.y, end.x, end.y, duration);
    }

    /**
     * Starts an wiggling animation for the object at the given location for the given duration in seconds.
     *
     * @param ae an AnimatedEntity returned by animateActor()
     * @param duration in seconds, as a float
     */
    public void wiggle(final AnimatedEntity ae, float duration) {

        final Actor a = ae.actor;
        final float x = adjustX(ae.gridX, ae.doubleWidth), y = adjustY(ae.gridY);
        //final int x = ae.gridX * cellWidth * ((ae.doubleWidth) ? 2 : 1) + (int)getX(), y = (gridHeight - ae.gridY - 1) * cellHeight - 1 + (int)getY();
        if(a == null || ae.animating)
            return;
        duration = clampDuration(duration);
        ae.animating = true;
        animationCount++;
        StatefulRNG gRandom = DefaultResources.getGuiRandom();
        a.addAction(Actions.sequence(
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
                        recallActor(ae, ae.gridX, ae.gridY);
                    }
                }))));
    }
    /**
     * Starts an wiggling animation for the object at the given location for the given duration in seconds.
     *
     * @param x
     * @param y
     * @param duration
     */
    public void wiggle(final int x, final int y, float duration) {
        final Actor a = cellToActor(x, y);
        if(a == null) return;
        duration = clampDuration(duration);
        animationCount++;
        float nextX = adjustX(x, false), nextY = adjustY(y);
        /*
        x *= cellWidth;
        y = (gridHeight - y - 1);
        y *= cellHeight;
        y -= 1;
        x +=  getX();
        y +=  getY();
        */
        StatefulRNG gRandom = DefaultResources.getGuiRandom();
        a.addAction(Actions.sequence(
                Actions.moveToAligned(nextX + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        nextY + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(nextX + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        nextY + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(nextX + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        nextY + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(nextX + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        nextY + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration * 0.2F),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(a, true, x, y);
                    }
                }))));
    }

    /**
     * Starts a tint animation for {@code ae} for the given {@code duration} in seconds.
     *
     * @param ae an AnimatedEntity returned by animateActor()
     * @param color what to transition ae's color towards, and then transition back from
     * @param duration how long the total "round-trip" transition should take in seconds
     */
    public void tint(final AnimatedEntity ae, Color color, float duration) {
        final Actor a = ae.actor;
        if(a == null)
            return;
        duration = clampDuration(duration);
        ae.animating = true;
        animationCount++;
        Color ac = scc.filter(a.getColor());
        a.addAction(Actions.sequence(
                Actions.color(color, duration * 0.3f),
                Actions.color(ac, duration * 0.7f),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(ae, ae.gridX, ae.gridY);
                    }
                }))));
    }

    /**
	 * Like {@link #tint(int, int, Color, float)}, but waits for {@code delay}
	 * (in seconds) before performing it.
     * @param delay how long to wait in seconds before starting the effect
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
     * @param color what to transition ae's color towards, and then transition back from
     * @param duration how long the total "round-trip" transition should take in seconds
     */
    public void tint(float delay, int x, int y, Color color, float duration) {
        tint(delay, x, y, color, duration, null);
    }

    /**
     * Like {@link #tint(int, int, Color, float)}, but waits for {@code delay}
     * (in seconds) before performing it. Additionally, enqueue {@code postRunnable}
     * for running after the created action ends.
     * @param delay how long to wait in seconds before starting the effect
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
     * @param color what to transition ae's color towards, and then transition back from
     * @param duration how long the total "round-trip" transition should take in seconds
     * @param postRunnable a Runnable to execute after the tint completes; may be null to do nothing.
     */

    public void tint(float delay, final int x, final int y, Color color, float duration, Runnable postRunnable) {
        final Actor a = cellToActor(x, y);
        if(a == null)
            return;
        duration = clampDuration(duration);
        animationCount++;

        Color ac = scc.filter(a.getColor());

        final int nbActions = 3 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        final Action[] sequence = new Action[nbActions];
        int index = 0;
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        sequence[index++] = Actions.color(color, duration * 0.3f);
        sequence[index++] = Actions.color(ac, duration * 0.7f);
        if(postRunnable != null)
        {
            sequence[index++] = Actions.run(postRunnable);
        }
        /* Do this one last, so that hasActiveAnimations() returns true during 'postRunnable' */
        sequence[index] = Actions.run(new Runnable() {
            @Override
            public void run() {
                recallActor(a, true, x, y);
            }
        });

        a.addAction(Actions.sequence(sequence));
    }

    /**
	 * Starts a tint animation for the object at {@code (x,y)} for the given
	 * {@code duration} (in seconds).
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
     * @param color
     * @param duration
     */
    public void tint(int x, int y, Color color, float duration) {
    	tint(0f, x, y, color, duration);
    }

	/**
	 * Fade the cell at {@code (x,y)} to {@code color}. Contrary to
	 * {@link #tint(int, int, Color, float)}, this action does not restore the
	 * cell's color at the end of its execution. This is for example useful to
	 * fade the game screen when the rogue dies.
	 *
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
	 * @param color
	 *            The color at the end of the fadeout.
	 * @param duration
	 *            The fadeout's duration.
	 */
	public void fade(final int x, final int y, Color color, float duration) {
		final Actor a = cellToActor(x, y);
		if (a == null)
			return;
        duration = clampDuration(duration);
		animationCount++;
		final Color c = scc.filter(color);
		a.addAction(Actions.sequence(Actions.color(c, duration), Actions.run(new Runnable() {
			@Override
			public void run() {
				recallActor(a, true, x, y);
			}
		})));
	}

    /**
     * Create a new Actor at (x, y) that looks like glyph but can rotate, and immediately starts changing color from
     * startColor to endColor and changing rotation from startRotation to endRotation, taking duration seconds to
     * complete before removing the Actor.
     * @param x the x position in cells; doesn't change
     * @param y the y position in cells; doesn't change
     * @param glyph the char to show (the same char throughout the effect, but it can rotate)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param startRotation the amount of rotation, in degrees, the glyph should start at
     * @param endRotation the amount of rotation, in degrees, the glyph should end at
     * @param duration the duration in seconds for the effect
     */
    public void summon(int x, int y, char glyph, Color startColor, Color endColor,
                       float startRotation, float endRotation, float duration)
    {
        summon(x, y, x, y, glyph, startColor, endColor, false, startRotation, endRotation, duration);
    }
    /**
     * Create a new Actor at (startX, startY) that looks like glyph but can rotate, sets its color, and immediately
     * starts changing position so it ends on the cell (endX, endY) and changing rotation from startRotation to
     * endRotation, taking duration seconds to complete before removing the Actor.
     * @param startX the starting x position in cells
     * @param startY the starting y position in cells
     * @param endX the ending x position in cells
     * @param endY the ending y position in cells
     * @param glyph the char to show (the same char throughout the effect, but it can rotate)
     * @param color the Color of the glyph throughout the effect
     * @param startRotation the amount of rotation, in degrees, the glyph should start at
     * @param endRotation the amount of rotation, in degrees, the glyph should end at
     * @param duration the duration in seconds for the effect
     */
    public void summon(int startX, int startY, int endX, int endY, char glyph, Color color,
                       float startRotation, float endRotation, float duration)
    {
        summon(startX, startY, endX, endY, glyph, color, color, false, startRotation, endRotation, duration);
    }
    /**
     * Create a new Actor at (startX, startY) that looks like glyph but has the given rotation, and immediately starts
     * changing color from startColor to endColor, and changing position so it ends on the cell (endX, endY), taking
     * duration seconds to complete before removing the Actor.
     * @param startX the starting x position in cells
     * @param startY the starting y position in cells
     * @param endX the ending x position in cells
     * @param endY the ending y position in cells
     * @param glyph the char to show (the same char throughout the effect, but it can rotate)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param rotation the amount of rotation, in degrees, the glyph should have throughout the effect
     * @param duration the duration in seconds for the effect
     */
    public void summon(int startX, int startY, int endX, int endY, char glyph, Color startColor, Color endColor,
                       float rotation, float duration)
    {
        summon(startX, startY, endX, endY, glyph, startColor, endColor, false, rotation, rotation, duration);
    }
    /**
     * Create a new Actor at (startX, startY) that looks like glyph but can rotate, and immediately starts changing
     * color from startColor to endColor, changing position so it ends on the cell (endX, endY), and changing rotation
     * from startRotation to endRotation, taking duration seconds to complete before removing the Actor.
     * @param startX the starting x position in cells
     * @param startY the starting y position in cells
     * @param endX the ending x position in cells
     * @param endY the ending y position in cells
     * @param glyph the char to show (the same char throughout the effect, but it can rotate)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param startRotation the amount of rotation, in degrees, the glyph should start at
     * @param endRotation the amount of rotation, in degrees, the glyph should end at
     * @param duration the duration in seconds for the effect
     */
    public void summon(int startX, int startY, int endX, int endY, char glyph, Color startColor, Color endColor,
                       float startRotation, float endRotation, float duration)
    {
        summon(startX, startY, endX, endY, glyph, startColor, endColor, false, startRotation, endRotation, duration);
    }
    /**
     * Create a new Actor at (startX, startY) that looks like glyph but can rotate, and immediately starts changing
     * color from startColor to endColor, changing position so it ends on the cell (endX, endY), and changing rotation
     * from startRotation to endRotation, taking duration seconds to complete before removing the Actor. Allows
     * setting doubleWidth, which centers the created Actor in the space between the two glyphs in a cell.
     * @param startX the starting x position in cells
     * @param startY the starting y position in cells
     * @param endX the ending x position in cells
     * @param endY the ending y position in cells
     * @param glyph the char to show (the same char throughout the effect, but it can rotate)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param doubleWidth true if this uses double-width cells, false in most cases
     * @param startRotation the amount of rotation, in degrees, the glyph should start at
     * @param endRotation the amount of rotation, in degrees, the glyph should end at
     * @param duration the duration in seconds for the effect
     */
    public void summon(int startX, int startY, int endX, int endY, char glyph, Color startColor, Color endColor, boolean doubleWidth,
                       float startRotation, float endRotation, float duration)
    {
        summon(0f, startX, startY, endX, endY, glyph, startColor, endColor, doubleWidth, startRotation, endRotation, duration);
    }
    /**
     * Create a new Actor at (startX, startY) that looks like glyph but can rotate, and immediately starts changing
     * color from startColor to endColor, changing position so it ends on the cell (endX, endY), and changing rotation
     * from startRotation to endRotation, taking duration seconds to complete before removing the Actor. Allows
     * setting doubleWidth, which centers the created Actor in the space between the two glyphs in a cell.
     * @param delay amount of time, in seconds, to wait before starting the effect
     * @param startX the starting x position in cells
     * @param startY the starting y position in cells
     * @param endX the ending x position in cells
     * @param endY the ending y position in cells
     * @param glyph the char to show (the same char throughout the effect, but it can rotate)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param doubleWidth true if this uses double-width cells, false in most cases
     * @param startRotation the amount of rotation, in degrees, the glyph should start at
     * @param endRotation the amount of rotation, in degrees, the glyph should end at
     * @param duration the duration in seconds for the effect
     */
    public void summon(float delay, final int startX, final int startY, final int endX, final int endY,
                       final char glyph, final Color startColor, final Color endColor, final boolean doubleWidth,
                       final float startRotation, final float endRotation, float duration)

    {
        final float dur = clampDuration(duration);
        animationCount++;
        final Action[] sequence = new Action[2];
        if (0 < delay) {
            addAction(Actions.delay(delay, Actions.run(new Runnable() {
                @Override
                public void run() {
                    final ColorChangeImage
                            gi = textFactory.makeGlyphImage(glyph, scc.gradient(startColor, endColor, (int) (dur * 40)), dur * 1.1f, doubleWidth);
                    gi.setPosition(adjustX(startX, doubleWidth), adjustY(startY));
                    gi.setRotation(startRotation);
                    autoActors.add(gi);
                    sequence[0] = Actions.parallel(
                            Actions.moveTo(adjustX(endX, doubleWidth), adjustY(endY), dur),
                            Actions.rotateTo(endRotation, dur));
                    sequence[1] = Actions.run(new Runnable() {
                        @Override
                        public void run() {
                            recallActor(gi, false);
                        }
                    });

                    gi.addAction(Actions.sequence(sequence));

                }
            })));
        }
        else {
            final ColorChangeImage
                    gi = textFactory.makeGlyphImage(glyph, scc.gradient(startColor, endColor, (int) (dur * 40)), dur * 1.1f, doubleWidth);
            gi.setPosition(adjustX(startX, doubleWidth), adjustY(startY));
            gi.setRotation(startRotation);
            autoActors.add(gi);
            sequence[0] = Actions.parallel(
                    Actions.moveTo(adjustX(endX, doubleWidth), adjustY(endY), dur),
                    Actions.rotateTo(endRotation, dur));
            sequence[1] = Actions.run(new Runnable() {
                @Override
                public void run() {
                    recallActor(gi, false);
                }
            });
            gi.addAction(Actions.sequence(sequence));
        }
    }
    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Actors that change color, position, and rotation.
     * This overload always moves Actors 1 cell away, which is a safe default, uses a "normal" amount of rotation for
     * for all of the actors (a value of 1f if you used another overload), and always uses an end color that is a
     * modified copy of startColor with 0 alpha (making the Actors all fade to transparent). The parameter
     * eightWay determines whether this produces 4 (cardinal) or 8 (cardinal and diagonal) rotations and directions.
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param eightWay if true, creates 8 Actors and moves them away in a square, otherwise, 4 Actors in a diamond
     * @param glyph the char to make a rotate-able Actor of; should definitely be visible
     * @param startColor the color to start the effect with
     * @param duration how long, in seconds, the effect should last
     */

    public void burst(int x, int y, boolean eightWay, char glyph,
                      Color startColor,
                      float duration)
    {
        burst(0f, x, y, 1, eightWay, glyph, startColor, startColor.cpy().sub(0,0,0,1), false, 1f, duration);
    }


    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Actors that change color, position, and rotation.
     * This overload always moves Actors 1 cell away, which is a safe default, and uses a "normal" amount of rotation
     * for all of the actors (a value of 1f if you used another overload). The parameter
     * eightWay determines whether this produces 4 (cardinal) or 8 (cardinal and diagonal) rotations and directions.
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param eightWay if true, creates 8 Actors and moves them away in a square, otherwise, 4 Actors in a diamond
     * @param glyph the char to make a rotate-able Actor of; should definitely be visible
     * @param startColor the color to start the effect with
     * @param endColor the color to end the effect on
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, boolean eightWay, char glyph,
                      Color startColor, Color endColor,
                      float duration)
    {
        burst(0f, x, y, 1, eightWay, glyph, startColor, endColor, false, 1f, duration);
    }

    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Actors that change color, position, and rotation.
     * This overload always moves Actors 1 cell away, which is a safe default. Some parameters need explanation:
     * eightWay determines whether this produces 4 (cardinal) or 8 (cardinal and diagonal) rotations and directions;
     * rotationStrength can default to 1 if you want some rotation (which looks good) or 0 if you want the Actors to
     * start at the correct rotation and not change that rotation over the course of the effect, but can be between 0
     * and 1 or higher than 1 (negative values may also work).
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param eightWay if true, creates 8 Actors and moves them away in a square, otherwise, 4 Actors in a diamond
     * @param glyph the char to make a rotate-able Actor of; should definitely be visible
     * @param startColor the color to start the effect with
     * @param endColor the color to end the effect on
     * @param rotationStrength how strongly to rotate the Actors; 0 is no rotation, 1 is a normal rotation
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, boolean eightWay, char glyph,
                      Color startColor, Color endColor,
                      float rotationStrength, float duration)
    {
        burst(0f, x, y, 1, eightWay, glyph, startColor, endColor, false, rotationStrength, duration);
    }


    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Actors that change color, position, and rotation.
     * Some parameters need explanation: distance is how many cells away to move the created Actors away from (x,y);
     * eightWay determines whether this produces 4 (cardinal) or 8 (cardinal and diagonal) rotations and directions;
     * rotationStrength can default to 1 if you want some rotation (which looks good) or 0 if you want the Actors to
     * start at the correct rotation and not change that rotation over the course of the effect, but can be between 0
     * and 1 or higher than 1 (negative values may also work).
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param distance how far away, in cells, to move each actor from the center (Chebyshev distance, forming a square)
     * @param eightWay if true, creates 8 Actors and moves them away in a square, otherwise, 4 Actors in a diamond
     * @param glyph the char to make a rotate-able Actor of; should definitely be visible
     * @param startColor the color to start the effect with
     * @param endColor the color to end the effect on
     * @param rotationStrength how strongly to rotate the Actors; 0 is no rotation, 1 is a normal rotation
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, int distance, boolean eightWay, char glyph,
                      Color startColor, Color endColor,
                      float rotationStrength, float duration)
    {
        burst(0f, x, y, distance, eightWay, glyph, startColor, endColor, false, rotationStrength, duration);
    }


    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Actors that change color, position, and rotation.
     * This overload always moves Actors 1 cell away, which is a safe default. Some parameters need explanation:
     * eightWay determines whether this produces 4 (cardinal) or 8 (cardinal and diagonal) rotations and directions;
     * rotationStrength can default to 1 if you want some rotation (which looks good) or 0 if you want the Actors to
     * start at the correct rotation and not change that rotation over the course of the effect, but can be between 0
     * and 1 or higher than 1 (negative values may also work).
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param eightWay if true, creates 8 Actors and moves them away in a square, otherwise, 4 Actors in a diamond
     * @param glyph the char to make a rotate-able Actor of; should definitely be visible
     * @param startColor the color to start the effect with
     * @param endColor the color to end the effect on
     * @param doubleWidth true if this should use the double-width-cell technique, false in most cases
     * @param rotationStrength how strongly to rotate the Actors; 0 is no rotation, 1 is a normal rotation
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, boolean eightWay, char glyph,
                      Color startColor, Color endColor, boolean doubleWidth,
                      float rotationStrength, float duration)
    {
        burst(0f, x, y, 1, eightWay, glyph, startColor, endColor, doubleWidth, rotationStrength, duration);
    }

    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Actors that change color, position, and rotation.
     * Some parameters need explanation: distance is how many cells away to move the created Actors away from (x,y);
     * eightWay determines whether this produces 4 (cardinal) or 8 (cardinal and diagonal) rotations and directions;
     * rotationStrength can default to 1 if you want some rotation (which looks good) or 0 if you want the Actors to
     * start at the correct rotation and not change that rotation over the course of the effect, but can be between 0
     * and 1 or higher than 1 (negative values may also work).
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param distance how far away, in cells, to move each actor from the center (Chebyshev distance, forming a square)
     * @param eightWay if true, creates 8 Actors and moves them away in a square, otherwise, 4 Actors in a diamond
     * @param glyph the char to make a rotate-able Actor of; should definitely be visible
     * @param startColor the color to start the effect with
     * @param endColor the color to end the effect on
     * @param doubleWidth true if this should use the double-width-cell technique, false in most cases
     * @param rotationStrength how strongly to rotate the Actors; 0 is no rotation, 1 is a normal rotation
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, int distance, boolean eightWay, char glyph,
                      Color startColor, Color endColor, boolean doubleWidth,
                      float rotationStrength, float duration)
    {
        burst(0f, x, y, distance, eightWay, glyph, startColor, endColor, doubleWidth, rotationStrength, duration);
    }

    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Actors that change color, position, and rotation.
     * Some parameters need explanation: distance is how many cells away to move the created Actors away from (x,y);
     * eightWay determines whether this produces 4 (cardinal) or 8 (cardinal and diagonal) rotations and directions;
     * rotationStrength can default to 1 if you want some rotation (which looks good) or 0 if you want the Actors to
     * start at the correct rotation and not change that rotation over the course of the effect, but can be between 0
     * and 1 or higher than 1 (negative values may also work).
     * @param delay amount of time, in seconds, to wait before starting the effect
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param distance how far away, in cells, to move each actor from the center (Chebyshev distance, forming a square)
     * @param eightWay if true, creates 8 Actors and moves them away in a square, otherwise, 4 Actors in a diamond
     * @param glyph the char to make a rotate-able Actor of; should definitely be visible
     * @param startColor the color to start the effect with
     * @param endColor the color to end the effect on
     * @param doubleWidth true if this should use the double-width-cell technique, false in most cases
     * @param rotationStrength how strongly to rotate the Actors; 0 is no rotation, 1 is a normal rotation
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(float delay, int x, int y, int distance, boolean eightWay, char glyph,
                      Color startColor, Color endColor, boolean doubleWidth,
                      float rotationStrength, float duration)
    {
        Direction d;
        if(eightWay)
        {
            for (int i = 0; i < 8; i++) {
                d = Direction.CLOCKWISE[i];
                summon(delay, x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                        glyph, startColor, endColor, doubleWidth,
                        45f * i, 45f * (i - rotationStrength),
                        duration);
            }
        }
        else
        {
            for (int i = 0; i < 4; i++) {
                d = Direction.CARDINALS_CLOCKWISE[i];
                summon(delay, x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                        glyph, startColor, endColor, doubleWidth,
                        90f * i, 90f * (i - rotationStrength),
                        duration);
            }

        }
    }

	@Override
    public boolean hasActiveAnimations() {
        return 0 < animationCount || 0 < getActions().size;
    }

    public OrderedSet<AnimatedEntity> getAnimatedEntities() {
        return animatedEntities;
    }

    public void removeAnimatedEntity(AnimatedEntity ae)
    {
        animatedEntities.remove(ae);
    }

	@Override
	public ISquidPanel<Color> getBacker() {
		return this;
	}

	/**
	 * @return The current color center. Never {@code null}.
	 */
	public IColorCenter<Color> getColorCenter() {
		return scc;
	}

	/**
	 * Use this method if you use your own {@link IColorCenter} and want this
	 * panel not to allocate its own colors (or fill
	 * {@link DefaultResources#getSCC()} but instead to the provided center.
	 * 
	 * @param scc
	 *            The color center to use. Should not be {@code null}.
	 * @return {@code this}
	 * @throws NullPointerException
	 *             If {@code scc} is {@code null}.
	 */
	@Override
	public SquidPanel setColorCenter(IColorCenter<Color> scc) {
		if (scc == null)
			/* Better fail now than later */
			throw new NullPointerException(
					"The color center should not be null in " + getClass().getSimpleName());
		this.scc = scc;
		return this;
	}

    public char getAt(int x, int y)
    {
        return contents[x][y];
    }
    public Color getColorAt(int x, int y)
    {
        return SColor.colorFromFloat(tmpColor, colors[x][y]);
    }

    public Color getLightingColor() {
        return lightingColor;
    }

    public void setLightingColor(Color lightingColor) {
        this.lightingColor = lightingColor;
        lightingFloat = lightingColor.toFloatBits();
    }

    protected float clampDuration(float duration) {
    	if (duration < 0.02f)
    		return 0.02f;
    	else
    		return duration;
    }

    /**
     * The X offset that the whole panel's internals will be rendered at. If the {@code gridWidth} of this SquidPanel is
     * less than the actual size of the char[][] it renders, then you can use gridOffsetX to start rendering at a
     * different position
     * @return the current offset in cells along the x axis
     */
    public int getGridOffsetX() {
        return gridOffsetX;
    }

    /**
     * Sets the X offset that the whole panel's internals will be rendered at.
     * @param gridOffsetX the requested offset in cells
     */
    public void setGridOffsetX(int gridOffsetX) {
        this.gridOffsetX = clamp(gridOffsetX,0,  contents.length - gridWidth);
    }

    /**
     * The Y offset that the whole panel's internals will be rendered at.
     * @return the current offset in cells along the y axis
     */
    public int getGridOffsetY() {
        return gridOffsetY;
    }

    /**
     * Sets the Y offset that the whole panel's internals will be rendered at.
     * @param gridOffsetY the requested offset in cells
     */
    public void setGridOffsetY(int gridOffsetY) {
        this.gridOffsetY = clamp(gridOffsetY,0,  contents[0].length - gridHeight);
    }

    /**
     * The number of cells along the x-axis that will be rendered of this panel.
     * @return the number of cells along the x-axis that will be rendered of this panel
     */
    public int getGridWidth() {
        return gridWidth;
    }

    /**
     * Sets the number of cells along the x-axis that will be rendered of this panel to gridWidth.
     * @param gridWidth the requested width in cells
     */
    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    /**
     * The number of cells along the y-axis that will be rendered of this panel
     * @return the number of cells along the y-axis that will be rendered of this panel
     */
    public int getGridHeight() {
        return gridHeight;
    }

    /**
     * Sets the number of cells along the y-axis that will be rendered of this panel to gridHeight.
     * @param gridHeight the requested height in cells
     */
    public void setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }

    /**
     * Gets the total number of cells along the x-axis that this stores; this is usually equivalent to
     * {@link #getGridWidth()}, but not if the constructor
     * {@link #SquidPanel(int, int, TextCellFactory, IColorCenter, float, float, char[][])} was used to set a
     * larger-than-normal map.
     * @return the width of the internal array this can render, which may be larger than the visible width
     */
    public int getTotalWidth()
    {
        return contents.length;
    }
    /**
     * Gets the total number of cells along the y-axis that this stores; this is usually equivalent to
     * {@link #getGridHeight()}, but not if the constructor
     * {@link #SquidPanel(int, int, TextCellFactory, IColorCenter, float, float, char[][])} was used to set a
     * larger-than-normal map.
     * @return the height of the internal array this can render, which may be larger than the visible height
     */
    public int getTotalHeight()
    {
        return contents[0].length;
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
    }

    public float getxOffset() {
        return xOffset;
    }

    public void setOffsetX(float xOffset) {
        this.xOffset = xOffset;
    }

    public float getyOffset() {
        return yOffset;
    }

    public void setOffsetY(float yOffset) {
        this.yOffset = yOffset;
    }

    public void setOffsets(float x, float y) {
        xOffset = x;
        yOffset = y;
    }

    /**
     * Gets the status of a boolean flag used for rendering thin maps; it will almost always be false unless it
     * was set to true with {@link #setOnlyRenderEven(boolean)}.
     * <br>
     * This is meant for thin-wall maps, where only cells where x and y are both even numbers have backgrounds
     * displayed. Should be false when using this SquidPanel for anything that isn't specifically a background
     * of a map that uses the thin-wall method from ThinDungeonGenerator or something similar. Even the
     * foregrounds of thin-wall maps should have this false, since ThinDungeonGenerator (in conjunction with
     * DungeonUtility's hashesToLines() method) makes thin lines for walls that should be displayed as between
     * the boundaries of other cells. The overlap behavior needed for some "thin enough" cells to be displayed
     * between the cells can be accomplished by using {@link #setTextSize(float, float)} to double the
     * previously-given cell width and height.
     *
     * @return the current status of the onlyRenderEven flag, which defaults to false
     */
    public boolean getOnlyRenderEven() {
        return onlyRenderEven;
    }
    /**
     * Sets the status of a boolean flag used for rendering thin maps; it should almost always be the default,
     * which is false, unless you are using a thin-wall map, and then this should be true only if this
     * SquidPanel is used for the background layer.
     * <br>
     * This is meant for thin-wall maps, where only cells where x and y are both even numbers have backgrounds
     * displayed. Should be false when using this SquidPanel for anything that isn't specifically a background
     * of a map that uses the thin-wall method from ThinDungeonGenerator or something similar. Even the
     * foregrounds of thin-wall maps should have this false, since ThinDungeonGenerator (in conjunction with
     * DungeonUtility's hashesToLines() method) makes thin lines for walls that should be displayed as between
     * the boundaries of other cells. The overlap behavior needed for some "thin enough" cells to be displayed
     * between the cells can be accomplished by using {@link #setTextSize(float, float)} to double the
     * previously-given cell width and height.
     *
     * @param onlyRenderEven generally, should only be true if this SquidPanel is a background of a thin map
     */

    public void setOnlyRenderEven(boolean onlyRenderEven) {
        this.onlyRenderEven = onlyRenderEven;
    }

    /**
     * Gets a "snapshot" of the data represented by this SquidPanel; stores the dimensions, the char data, and the color
     * data in a way that can be set back to a SquidPanel using {@link #setFromSnapshot(String, int, int, int, int)} or
     * its overload that takes a StringBuilder. The actual contents of the returned StringBuilder are unlikely to be
     * legible in any way if read as text, and are meant to be concise and stable across versions.
     * @return a StringBuilder representation of this SquidPanel's data that can be passed later to {@link #setFromSnapshot(StringBuilder, int, int, int, int)} or converted to String and passed to its overload
     */
    public StringBuilder getSnapshot()
    {
        return getSnapshot(0, 0, gridWidth, gridHeight);
    }
    /**
     * Gets a "snapshot" of the data represented by this SquidPanel; stores the dimensions, the char data, and the color
     * data in a way that can be set back to a SquidPanel using {@link #setFromSnapshot(String, int, int, int, int)} or
     * its overload that takes a StringBuilder. The actual contents of the returned StringBuilder are unlikely to be
     * legible in any way if read as text, and are meant to be concise and stable across versions. This overload allows
     * the first x and y position used to be specified, as well as the width and height to use (the actual width and
     * height stored may be different if this SquidPanel's gridWidth and/or gridHeight are smaller than the width and/or
     * height given).
     * @param startX the first x position to use in the snapshot, inclusive
     * @param startY the first y position to use in the snapshot, inclusive
     * @param width how wide the snapshot area should be; x positions from startX to startX + width - 1 will be used
     * @param height how tall the snapshot area should be; y positions from startY to startY + height - 1 will be used
     * @return a StringBuilder representation of this SquidPanel's data that can be passed later to {@link #setFromSnapshot(StringBuilder, int, int, int, int)} or converted to String and passed to its overload
     */
    public StringBuilder getSnapshot(int startX, int startY, int width, int height) {
        width = Math.min(gridWidth - startX, width);
        height = Math.min(gridHeight - startY, height);
        StringBuilder sb = new StringBuilder(width * height * 9 + 12);
        sb.append(width).append('x').append(height).append(':');
        for (int x = startX, i = 0; i < width; x++, i++) {
            sb.append(contents[x], startY, height);
        }
        char[] reuse = new char[8];
        for (int x = startX, i = 0; i < width; x++, i++) {
            for (int y = startY, j = 0; j < height; y++, j++) {
                sb.append(SColor.floatToChars(reuse, colors[x][y]));
            }
        }
        return sb;
    }

    /**
     * Given a "snapshot" from {@link #getSnapshot(int, int, int, int)}, this assigns the chars and colors in this
     * SquidPanel from 0,0 (inclusive) up to the dimensions stored in the snapshot to match the snapshot's data.
     * @param snapshot a StringBuilder in a special format as produced by {@link #getSnapshot(int, int, int, int)}
     * @return this after setting, for chaining
     */
    public SquidPanel setFromSnapshot(StringBuilder snapshot)
    {
        return setFromSnapshot(snapshot, 0, 0, -1, -1);
    }
    /**
     * Given a "snapshot" from {@link #getSnapshot(int, int, int, int)}, this assigns the chars and colors in this
     * SquidPanel from the position given by putX,putY (inclusive) up to the dimensions stored in the snapshot
     * (considering putX and putY as offsets) so they have the values stored in the snapshot.
     * @param snapshot a StringBuilder in a special format as produced by {@link #getSnapshot(int, int, int, int)}
     * @param putX where to start placing the data from the snapshot, x position
     * @param putY where to start placing the data from the snapshot, y position
     * @return this after setting, for chaining
     */
    public SquidPanel setFromSnapshot(StringBuilder snapshot, int putX, int putY)
    {
        return setFromSnapshot(snapshot, putX, putY, -1, -1);
    }
    /**
     * Given a "snapshot" from {@link #getSnapshot(int, int, int, int)}, this assigns the chars and colors in this
     * SquidPanel from the position given by putX,putY (inclusive) to putX+limitWidth,putY+limitHeight (exclusive) so
     * they have the values stored in the snapshot. If limitWidth or limitHeight is negative, this uses the full width
     * and height of the snapshot (stopping early if it would extend past the gridWidth or gridHeight of this
     * SquidPanel).
     * @param snapshot a StringBuilder in a special format as produced by {@link #getSnapshot(int, int, int, int)}
     * @param putX where to start placing the data from the snapshot, x position
     * @param putY where to start placing the data from the snapshot, y position
     * @param limitWidth if negative, uses all of snapshot's width as possible, otherwise restricts the width allowed
     * @param limitHeight if negative, uses all of snapshot's height as possible, otherwise restricts the height allowed
     * @return this after setting, for chaining
     */
    public SquidPanel setFromSnapshot(StringBuilder snapshot, int putX, int putY, int limitWidth, int limitHeight)
    {
        if(putX >= gridWidth || putY >= gridHeight || snapshot == null || snapshot.length() < 4) return this;
        if(putX < 0) putX = 0;
        if(putY < 0) putY = 0;
        int start = snapshot.indexOf(":")+1, width = StringKit.intFromDec(snapshot),
                height = StringKit.intFromDec(snapshot, snapshot.indexOf("x") + 1, start),
                run = start;
        if(limitWidth < 0)
            limitWidth = Math.min(width, gridWidth - putX);
        else
            limitWidth = Math.min(limitWidth, Math.min(width, gridWidth - putX));

        if(limitHeight < 0)
            limitHeight = Math.min(height, gridHeight - putY);
        else
            limitHeight = Math.min(limitHeight, Math.min(height, gridHeight - putY));
        for (int x = putX, i = 0; i < limitWidth; x++, i++, run += height) {
            snapshot.getChars(run, run + limitHeight, contents[x], putY);
        }
        run = start + width * height;
        for (int x = putX, i = 0; i < limitWidth; x++, i++) {
            for (int y = putY, j = 0; j < limitHeight; y++, j++) {
                colors[x][y] = SColor.charsToFloat(snapshot, run);
                run += 8;
            }
        }
        return this;
    }

    /**
     * Given a "snapshot" from {@link #getSnapshot(int, int, int, int)}, this assigns the chars and colors in this
     * SquidPanel from 0,0 (inclusive) up to the dimensions stored in the snapshot to match the snapshot's data.
     * <br>
     * This overload takes a String instead of a StringBuilder for potentially-easier loading from files.
     * @param snapshot a String in a special format as produced by {@link #getSnapshot(int, int, int, int)}
     * @return this after setting, for chaining
     */
    public SquidPanel setFromSnapshot(String snapshot)
    {
        return setFromSnapshot(snapshot, 0, 0, -1, -1);
    }
    /**
     * Given a "snapshot" from {@link #getSnapshot(int, int, int, int)}, this assigns the chars and colors in this
     * SquidPanel from the position given by putX,putY (inclusive) up to the dimensions stored in the snapshot
     * (considering putX and putY as offsets) so they have the values stored in the snapshot.
     * <br>
     * This overload takes a String instead of a StringBuilder for potentially-easier loading from files.
     * @param snapshot a String in a special format as produced by {@link #getSnapshot(int, int, int, int)}
     * @param putX where to start placing the data from the snapshot, x position
     * @param putY where to start placing the data from the snapshot, y position
     * @return this after setting, for chaining
     */
    public SquidPanel setFromSnapshot(String snapshot, int putX, int putY)
    {
        return setFromSnapshot(snapshot, putX, putY, -1, -1);
    }

    /**
     * Given a "snapshot" from {@link #getSnapshot(int, int, int, int)}, this assigns the chars and colors in this
     * SquidPanel from the position given by putX,putY (inclusive) to putX+limitWidth,putY+limitHeight (exclusive) so
     * they have the values stored in the snapshot. If limitWidth or limitHeight is negative, this uses the full width
     * and height of the snapshot (stopping early if it would extend past the gridWidth or gridHeight of this
     * SquidPanel).
     * <br>
     * This overload takes a String instead of a StringBuilder for potentially-easier loading from files.
     * @param snapshot a String in a special format as produced by {@link #getSnapshot(int, int, int, int)}
     * @param putX where to start placing the data from the snapshot, x position
     * @param putY where to start placing the data from the snapshot, y position
     * @param limitWidth if negative, uses all of snapshot's width as possible, otherwise restricts the width allowed
     * @param limitHeight if negative, uses all of snapshot's height as possible, otherwise restricts the height allowed
     * @return this after setting, for chaining
     */

    public SquidPanel setFromSnapshot(String snapshot, int putX, int putY, int limitWidth, int limitHeight)
    {
        if(putX >= gridWidth || putY >= gridHeight || snapshot == null || snapshot.length() < 4) return this;
        if(putX < 0) putX = 0;
        if(putY < 0) putY = 0;
        int start = snapshot.indexOf(":")+1, width = StringKit.intFromDec(snapshot),
                height = StringKit.intFromDec(snapshot, snapshot.indexOf("x") + 1, start),
                run = start;
        if(limitWidth < 0)
            limitWidth = Math.min(width, gridWidth - putX);
        else
            limitWidth = Math.min(limitWidth, Math.min(width, gridWidth - putX));

        if(limitHeight < 0)
            limitHeight = Math.min(height, gridHeight - putY);
        else
            limitHeight = Math.min(limitHeight, Math.min(height, gridHeight - putY));
        for (int x = putX, i = 0; i < limitWidth; x++, i++, run += height) {
            snapshot.getChars(run, run + limitHeight, contents[x], putY);
        }
        run = start + width * height;
        for (int x = putX, i = 0; i < limitWidth; x++, i++) {
            for (int y = putY, j = 0; j < limitHeight; y++, j++) {
                colors[x][y] = SColor.charsToFloat(snapshot, run);
                run += 8;
            }
        }
        return this;
    }
}
