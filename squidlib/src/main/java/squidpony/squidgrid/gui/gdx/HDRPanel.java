package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import squidpony.panel.IColoredString;
import squidpony.panel.ISquidPanel;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;
import java.util.LinkedHashSet;

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
public class HDRPanel extends Group implements ISquidPanel<HDRColor> {

    public float DEFAULT_ANIMATION_DURATION = 0.12F;
    private int animationCount = 0;
    private HDRColor defaultForeground = HDRColor.WHITE;
    private SquidColorCenter scc;
    private final int gridWidth, gridHeight, cellWidth, cellHeight;
    private String[][] contents;
    private long[][] colors;
    private final TextCellFactory textFactory;
    private LinkedHashSet<AnimatedEntity> animatedEntities;

    /**
     * Creates a bare-bones panel with all default values for text rendering.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     */
    public HDRPanel(int gridWidth, int gridHeight) {
        this(gridWidth, gridHeight, new TextCellFactory().defaultSquareFont());
    }

    /**
     * Creates a panel with the given grid and cell size. Uses a default square font.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param cellWidth the number of horizontal pixels in each cell
     * @param cellHeight the number of vertical pixels in each cell
     */
    public HDRPanel(int gridWidth, int gridHeight, int cellWidth, int cellHeight) {
        this(gridWidth, gridHeight, new TextCellFactory().defaultSquareFont().width(cellWidth).height(cellHeight));
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
    public HDRPanel(int gridWidth, int gridHeight, TextCellFactory factory) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        textFactory = factory;
        scc = DefaultResources.getSCC();

        if (factory == null) {
            factory = new TextCellFactory();
        }

        if (!factory.initialized()) {
            factory.initByFont();
        }

        cellWidth = factory.width();
        cellHeight = factory.height();

        contents = new String[gridWidth][gridHeight];
        colors = new long[gridWidth][gridHeight];

        int w = gridWidth * cellWidth;
        int h = gridHeight * cellHeight;
        setSize(w, h);
        animatedEntities = new LinkedHashSet<AnimatedEntity>();
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
	public void put(char[][] chars, HDRColor[][] foregrounds) {
        put(0, 0, chars, foregrounds);
    }

    public void put(char[][] chars, int[][] indices, ArrayList<HDRColor> palette) {
        put(0, 0, chars, indices, palette);
    }

    public void put(int xOffset, int yOffset, char[][] chars) {
        put(xOffset, yOffset, chars, defaultForeground);
    }

    public void put(int xOffset, int yOffset, char[][] chars, HDRColor[][] foregrounds) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    put(x, y, chars[x - xOffset][y - yOffset], foregrounds[x - xOffset][y - yOffset]);
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, char[][] chars, int[][] indices, ArrayList<HDRColor> palette) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    put(x, y, chars[x - xOffset][y - yOffset], palette.get(indices[x - xOffset][y - yOffset]));
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, HDRColor[][] foregrounds) {
        for (int x = xOffset; x < xOffset + foregrounds.length; x++) {
            for (int y = yOffset; y < yOffset + foregrounds[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    put(x, y, '\0', foregrounds[x - xOffset][y - yOffset]);
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, int[][] indices, ArrayList<HDRColor> palette) {
        for (int x = xOffset; x < xOffset + indices.length; x++) {
            for (int y = yOffset; y < yOffset + indices[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    put(x, y, '\0', palette.get(indices[x - xOffset][y - yOffset]));
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, char[][] chars, HDRColor foreground) {
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
	public void put(int xOffset, int yOffset, IColoredString<? extends HDRColor> cs) {
		int x = xOffset;
		for (IColoredString.Bucket<? extends HDRColor> fragment : cs) {
			final String s = fragment.getText();
			final HDRColor color = fragment.getColor();
			put(x, yOffset, s, color == null ? getDefaultForegroundColor() : color);
			x += s.length();
		}
	}

	@Override
	public void put(int xOffset, int yOffset, String string, HDRColor foreground) {
        char[][] temp = new char[string.length()][1];
        for (int i = 0; i < string.length(); i++) {
            temp[i][0] = string.charAt(i);
        }
        put(xOffset, yOffset, temp, foreground);
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
     * @param vertical true if the text should be written vertically, from top to bottom
     */
    public void placeVerticalString(int xOffset, int yOffset, String string, boolean vertical) {
        put(xOffset, yOffset, string, defaultForeground, vertical);
    }

    /**
     * Puts the given string horizontally with the first character at the given offset.
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
    public void put(int xOffset, int yOffset, String string, HDRColor foreground, boolean vertical) {
        if (vertical) {
            put(xOffset, yOffset, new char[][]{string.toCharArray()}, foreground);
        } else {
            put(xOffset, yOffset, string, foreground);
        }
    }

    /**
     * Erases the entire panel, leaving only a transparent space.
     */
    public void erase() {
        for (int i = 0; i < contents.length; i++) {
            for (int j = 0; j < contents[i].length; j++) {
                contents[i][j] = "";
                colors[i][j] = 255;
            }

        }
    }

    @Override
	public void clear(int x, int y) {
        put(x, y, HDRColor.CLEAR);
    }

    @Override
	public void put(int x, int y, HDRColor color) {
        put(x, y, '\0', color);
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

    public void put(int x, int y, int c, HDRColor color) {
        put(x, y, String.valueOf(Character.toChars(c)), color);
    }

    public void put(int x, int y, int index, ArrayList<HDRColor> palette) {
        put(x, y, palette.get(index));
    }

    public void put(int x, int y, char c, int index, ArrayList<HDRColor> palette) {
        put(x, y, c, palette.get(index));
    }

    /**
     * Takes a unicode codepoint for input.
     *
     * @param x
     * @param y
     * @param c
     * @param color
     */
    @Override
	public void put(int x, int y, char c, HDRColor color) {
        if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) {
            return;//skip if out of bounds
        }
        contents[x][y] = String.valueOf(c);
        colors[x][y] = HDRColor.rgbaHDR(color);
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

    @Override
    public void draw (Batch batch, float parentAlpha) {
        HDRColor tmp;
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                tmp = scc.get(colors[x][y]);
                textFactory.draw(batch, contents[x][y], tmp, 1f * x * cellWidth, 1f * (gridHeight - y) * cellHeight);
            }
        }
        super.draw(batch, parentAlpha);
        for(AnimatedEntity ae : animatedEntities)
        {
            ae.actor.act(Gdx.graphics.getDeltaTime());
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
            ae.actor.draw(batch, parentAlpha);
    }

    @Override
	public void setDefaultForeground(HDRColor defaultForeground) {
        this.defaultForeground = defaultForeground;
    }

	@Override
	public HDRColor getDefaultForegroundColor() {
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
    public AnimatedEntity animateActor(int x, int y, char c, HDRColor color)
    {
        Actor a = textFactory.makeActor("" + c, color);
        a.setName("" + c);
        a.setPosition(x * cellWidth, (gridHeight - y - 1) * cellHeight - 1);

        AnimatedEntity ae = new AnimatedEntity(a, x, y);
        animatedEntities.add(ae);
        return ae;
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
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, char c, HDRColor color)
    {
        Actor a = textFactory.makeActor("" + c, color);
        a.setName("" + c);
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth, (gridHeight - y - 1) * cellHeight - 1);
        else
            a.setPosition(x * cellWidth, (gridHeight - y - 1) * cellHeight - 1);

        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }

    /**
     * Create an AnimatedEntity at position x, y, using the String s in the given color.
     * @param x
     * @param y
     * @param s
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, String s, HDRColor color)
    {
        Actor a = textFactory.makeActor(s, color);
        a.setName(s);
        a.setPosition(x * cellWidth, (gridHeight - y - 1) * cellHeight - 1);

        AnimatedEntity ae = new AnimatedEntity(a, x, y);
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
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, String s, HDRColor color)
    {
        Actor a = textFactory.makeActor(s, color);
        a.setName(s);
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth, (gridHeight - y - 1) * cellHeight - 1);
        else
            a.setPosition(x * cellWidth, (gridHeight - y - 1) * cellHeight - 1);

        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }

    /**
     * Create an AnimatedEntity at position x, y, using the char c with a color looked up by index in palette.
     * @param x
     * @param y
     * @param c
     * @param index
     * @param palette
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, char c, int index, ArrayList<HDRColor> palette)
    {
        return animateActor(x, y, c, palette.get(index));
    }

    /**
     * Create an AnimatedEntity at position x, y, using the String s with a color looked up by index in palette.
     * @param x
     * @param y
     * @param s
     * @param index
     * @param palette
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, String s, int index, ArrayList<HDRColor> palette)
    {
        return animateActor(x, y, s, palette.get(index));
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
        Actor a = textFactory.makeActor(texture, HDRColor.WHITE);
        a.setName("");
        a.setPosition(x * cellWidth, (gridHeight - y - 1) * cellHeight - 1);

        AnimatedEntity ae = new AnimatedEntity(a, x, y);
        animatedEntities.add(ae);
        return ae;
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
    public AnimatedEntity animateActor(int x, int y, TextureRegion texture, HDRColor color)
    {
        Actor a = textFactory.makeActor(texture, color);
        a.setName("");
        a.setPosition(x * cellWidth, (gridHeight - y - 1) * cellHeight - 1);

        AnimatedEntity ae = new AnimatedEntity(a, x, y);
        animatedEntities.add(ae);
        return ae;
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
        Actor a = textFactory.makeActor(texture, HDRColor.WHITE, (doubleWidth ? 2 : 1) * cellWidth, cellHeight);
        a.setName("");
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth, (gridHeight - y - 1) * cellHeight - 1);
        else
            a.setPosition(x * cellWidth, (gridHeight - y - 1) * cellHeight - 1);

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
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, TextureRegion texture, HDRColor color) {
        Actor a = textFactory.makeActor(texture, color, (doubleWidth ? 2 : 1) * cellWidth, cellHeight);
        a.setName("");
        if (doubleWidth)
            a.setPosition(x * 2 * cellWidth, (gridHeight - y - 1) * cellHeight - 1);
        else
            a.setPosition(x * cellWidth, (gridHeight - y - 1) * cellHeight - 1);

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
                ? textFactory.makeActor(texture, HDRColor.WHITE, (doubleWidth ? 2 : 1) * cellWidth, cellHeight)
                : textFactory.makeActor(texture, HDRColor.WHITE, texture.getRegionWidth(), texture.getRegionHeight());
        a.setName("");
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth, (gridHeight - y - 1) * cellHeight - 1);
        else
            a.setPosition(x * cellWidth, (gridHeight - y - 1) * cellHeight - 1);

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
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, boolean stretch, TextureRegion texture, HDRColor color) {

        Actor a = (stretch)
                ? textFactory.makeActor(texture, color, (doubleWidth ? 2 : 1) * cellWidth, cellHeight)
                : textFactory.makeActor(texture, color, texture.getRegionWidth(), texture.getRegionHeight());
        a.setName("");
        if (doubleWidth)
            a.setPosition(x * 2 * cellWidth, (gridHeight - y - 1) * cellHeight - 1);
        else
            a.setPosition(x * cellWidth, (gridHeight - y - 1) * cellHeight - 1);

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
        if(contents[x][y] == null || contents[x][y].equals(""))
            return null;

        Actor a = textFactory.makeActor(contents[x][y], scc.get(colors[x][y]));
        a.setName(contents[x][y]);
        a.setPosition(x * cellWidth, (gridHeight - y - 1) * cellHeight - 1);

        addActor(a);

        contents[x][y] = "";
        return a;
    }

    /**
     * Created an Actor from the contents of the given x,y position on the grid.
     * @param x
     * @param y
     * @param doubleWidth
     * @return
     */
    public Actor cellToActor(int x, int y, boolean doubleWidth)
    {
        if(contents[x][y] == null || contents[x][y].equals(""))
            return null;

        Actor a = textFactory.makeActor(contents[x][y], scc.get(colors[x][y]));
        a.setName(contents[x][y]);
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth, (gridHeight - y - 1) * cellHeight - 1);
        else
            a.setPosition(x * cellWidth, (gridHeight - y - 1) * cellHeight - 1);

        addActor(a);

        contents[x][y] = "";
        return a;
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
    public void recallActor(Actor a)
    {
        int x = Math.round(a.getX() / cellWidth),
             y = gridHeight - Math.round(a.getY() / cellHeight) - 1;
        contents[x][y] = a.getName();
        animationCount--;
        removeActor(a);
    }
    public void recallActor(AnimatedEntity ae)
    {
        if(ae.doubleWidth)
            ae.gridX = Math.round(ae.actor.getX() / (2 * cellWidth));
        else
            ae.gridX = Math.round(ae.actor.getX() / cellWidth);
        ae.gridY = gridHeight - Math.round(ae.actor.getY() / cellHeight) - 1;
        ae.animating = false;
        animationCount--;
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
        final int x = ae.gridX * cellWidth, y = (gridHeight - ae.gridY - 1) * cellHeight - 1;
        if(a == null || ae.animating) return;
        if(duration < 0.02f) duration = 0.02f;
        animationCount++;
        ae.animating = true;
        a.addAction(Actions.sequence(
                Actions.moveToAligned(x + (direction.deltaX / 3F) * ((ae.doubleWidth) ? 2F : 1F), y + direction.deltaY / 3F,
                        Align.center, duration * 0.35F),
                Actions.moveToAligned(x, y, Align.bottomLeft, duration * 0.65F),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(ae);
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
    public void bump(int x, int y, Direction direction, float duration)
    {
        final Actor a = cellToActor(x, y);
        if(a == null) return;
        if(duration < 0.02f) duration = 0.02f;
        animationCount++;
        x *= cellWidth;
        y = (gridHeight - y - 1);
        y *= cellHeight;
        y -= 1;
        a.addAction(Actions.sequence(
                Actions.moveToAligned(x + direction.deltaX / 3F, y + direction.deltaY / 3F,
                        Align.center, duration * 0.35F),
                Actions.moveToAligned(x, y, Align.bottomLeft, duration * 0.65F),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(a);
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
    public void slide(final AnimatedEntity ae, int newX, int newY, float duration)
    {
        final Actor a = ae.actor;
        final int nextX = newX * cellWidth * ((ae.doubleWidth) ? 2 : 1), nextY = (gridHeight - newY - 1) * cellHeight - 1;
        if(a == null || ae.animating) return;
        if(duration < 0.02f) duration = 0.02f;
        animationCount++;
        ae.animating = true;
        a.addAction(Actions.sequence(
                Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(ae);
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
    public void slide(int x, int y, int newX, int newY, float duration)
    {
        final Actor a = cellToActor(x, y);
        if(a == null) return;
        if(duration < 0.02f) duration = 0.02f;
        animationCount++;
        newX *= cellWidth;
        newY = (gridHeight - newY - 1);
        newY *= cellHeight;
        newY -= 1;
        a.addAction(Actions.sequence(
                Actions.moveToAligned(newX, newY, Align.bottomLeft, duration),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(a);
                    }
                }))));
    }
    /**
     * Starts a movement animation for the object at the given grid location at the default speed.
     *
     * @param start
     * @param end
     */
    public void slide(Coord start, Coord end) {
        slide(start.x, start.y, end.x, end.y, DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Starts a movement animation for the object at the given grid location at the default speed for one grid square in
     * the direction provided.
     *
     * @param start
     * @param direction
     */
    public void slide(Coord start, Direction direction) {
        slide(start.x, start.y, start.x + direction.deltaX, start.y + direction.deltaY, DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Starts a sliding movement animation for the object at the given location at the provided speed. The duration is
     * how many seconds should pass for the entire animation.
     *
     * @param start
     * @param end
     * @param duration
     */
    public void slide(Coord start, Coord end, float duration) {
        slide(start.x, start.y, end.x, end.y, duration);
    }

    /**
     * Starts an wiggling animation for the object at the given location for the given duration in seconds.
     *
     * @param ae an AnimatedEntity returned by animateActor()
     * @param duration
     */
    public void wiggle(final AnimatedEntity ae, float duration) {

        final Actor a = ae.actor;
        final int x = ae.gridX * cellWidth * ((ae.doubleWidth) ? 2 : 1), y = (gridHeight - ae.gridY - 1) * cellHeight - 1;
        if(a == null || ae.animating)
            return;
        if(duration < 0.02f) duration = 0.02f;
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
                        recallActor(ae);
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
    public void wiggle(int x, int y, float duration) {
        final Actor a = cellToActor(x, y);
        if(a == null) return;
        if(duration < 0.02f) duration = 0.02f;
        animationCount++;
        x *= cellWidth;
        y = (gridHeight - y - 1);
        y *= cellHeight;
        y -= 1;
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
                        recallActor(a);
                    }
                }))));
    }

    /**
     * Starts an wiggling animation for the object at the given location for the given duration in seconds.
     *
     * @param ae an AnimatedEntity returned by animateActor()
     * @param color
     * @param duration
     */
    public void tint(final AnimatedEntity ae, HDRColor color, float duration) {

        final Actor a = ae.actor;
        if(a == null || ae.animating)
            return;
        if(duration < 0.02f) duration = 0.02f;
        ae.animating = true;
        animationCount++;
        HDRColor ac = scc.get(a.getColor());
        a.addAction(Actions.sequence(
                Actions.color(color, duration * 0.3f),
                Actions.color(ac, duration * 0.7f),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(ae);
                    }
                }))));
    }
    /**
     * Starts an wiggling animation for the object at the given location for the given duration in seconds.
     *
     * @param x
     * @param y
     * @param color
     * @param duration
     */
    public void tint(int x, int y, HDRColor color, float duration) {
        final Actor a = cellToActor(x, y);
        if(a == null)
            return;
        if(duration < 0.02f) duration = 0.02f;
        animationCount++;

        HDRColor ac = scc.get(a.getColor());
        a.addAction(Actions.sequence(
                Actions.color(color, duration * 0.3f),
                Actions.color(ac, duration * 0.7f),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        recallActor(a);
                    }
                }))));
    }

    /**
     * Returns true if there are animations running when this method is called.
     *
     * @return
     */
    public boolean hasActiveAnimations() {
        return animationCount != 0;
    }

    public LinkedHashSet<AnimatedEntity> getAnimatedEntities() {
        return animatedEntities;
    }

	@Override
	public void refresh() {
		/* smelC: should we do something here ? */
        /* Tommy Ettinger: potentially, but it would need to call draw, and that means keeping a Batch. */
	}

	@Override
	public ISquidPanel<HDRColor> getBacker() {
		return this;
	}

}
