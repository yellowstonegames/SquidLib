package squidpony.squidgrid.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JLayeredPane;
import squidpony.SColor;
import squidpony.squidgrid.gui.animation.Animation;
import squidpony.squidgrid.gui.animation.AnimationManager;
import squidpony.squidgrid.gui.animation.BumpAnimation;
import squidpony.squidgrid.gui.animation.SlideAnimation;
import squidpony.squidgrid.gui.animation.WiggleAnimation;
import squidpony.squidgrid.Direction;

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
public class SquidPanel extends JLayeredPane {

    private static int DEFAULT_ANIMATION_DURATION = 200;
    private static Font DEFAULT_FONT = new Font("Helvetica", Font.PLAIN, 22);
    private AnimationManager animationManager;
    private final ConcurrentLinkedQueue<Animation> animations = new ConcurrentLinkedQueue<>();
    private BufferedImage[][] contents;
    private boolean[][] imageChanged;
    private BufferedImage contentsImage = new BufferedImage(20, 20, BufferedImage.TYPE_4BYTE_ABGR);
    private Color defaultForeground = SColor.WHITE;
    private final int gridWidth, gridHeight, cellWidth, cellHeight;
    private Dimension panelDimension;
    private final TextCellFactory textFactory;
    private final ImageCellMap imageCellMap;
    
    /**
     * Creates a bare-bones panel with all default values for text rendering.
     * 
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     */
    public SquidPanel(int gridWidth, int gridHeight) {
        this(gridWidth, gridHeight, new TextCellFactory().font(DEFAULT_FONT), null);
    }

    /**
     * Creates a panel with the given grid and cell size. Uses a default font.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param cellWidth the number of horizontal pixels in each cell
     * @param cellHeight the number of vertical pixels in each cell
     */
    public SquidPanel(int gridWidth, int gridHeight, int cellWidth, int cellHeight) {
        this(gridWidth, gridHeight, new TextCellFactory().font(DEFAULT_FONT).width(cellWidth).height(cellHeight), null);
    }

    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions.
     * 
     * If the TextCellFactory has not yet been initialized, then it will be sized based on its font. If it is null
     * then a default one will be created and initialized.
     *
     * For proper display, The imageMap (if not null) should have the same cell size as the factory.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory the factory to use for cell rendering
     * @param imageMap can be null if no explicit images will be used
     */
    public SquidPanel(int gridWidth, int gridHeight, TextCellFactory factory, ImageCellMap imageMap) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        textFactory = factory;
        
        if (factory == null) {
            factory = new TextCellFactory();
        }
        
        if (!factory.initialized()) {
            factory.initByFont();
        }
        
        cellWidth = factory.width();
        cellHeight = factory.height();
        setFont(textFactory.font());
        if (imageMap == null) {
            imageCellMap = new ImageCellMap(cellWidth, cellHeight);
        } else {
            imageCellMap = imageMap;
        }

        setOpaque(false);
        contents = new BufferedImage[gridWidth][gridHeight];
        imageChanged = new boolean[gridWidth][gridHeight];
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                imageChanged[x][y] = true;
            }
        }
        int w = gridWidth * cellWidth;
        int h = gridHeight * cellHeight;
        panelDimension = new Dimension(w, h);
        setSize(panelDimension);
        setMinimumSize(panelDimension);
        setPreferredSize(panelDimension);
        contentsImage = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        redraw();
        repaint();
    }

    /**
     * Returns the image being displayed at the given coordinates. Because this is the actual image and not a copy, any
     * changes to it will be reflected in the original during the next GUI update.
     *
     * @param x
     * @param y
     * @return
     */
    public BufferedImage getImage(int x, int y) {
        return contents[x][y];
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(contentsImage, 0, 0, null);
        paintComponents(g);
        //TODO: The next line may be needed for animations, but it causes flickering with SquidLayers.
        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Places the given characters into the grid starting at 0,0.
     *
     * @param chars
     */
    public void put(char[][] chars) {//TODO - convert this to work with code points
        SquidPanel.this.put(0, 0, chars);
    }

    public void put(char[][] chars, Color[][] foregrounds) {//TODO - convert this to work with code points
        SquidPanel.this.put(0, 0, chars, foregrounds);
    }

    public void put(char[][] chars, int[][] indices, List<Color> palette) {//TODO - convert this to work with code points
        SquidPanel.this.put(0, 0, chars, indices, palette);
    }

    public void put(int x, int y, BufferedImage image) {
        contents[x][y] = image;
        imageChanged[x][y] = true;
    }

    public void putImage(int x, int y, String key) {
        BufferedImage image = imageCellMap.get(key);
        if (image == null) {
            image = imageCellMap.getNullImage();
        }
        contents[x][y] = image;
        imageChanged[x][y] = true;
    }

    public void put(int xOffset, int yOffset, char[][] chars) {
        SquidPanel.this.put(xOffset, yOffset, chars, defaultForeground);
    }

    public void put(int xOffset, int yOffset, char[][] chars, Color[][] foregrounds) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    SquidPanel.this.put(x, y, chars[x - xOffset][y - yOffset], foregrounds[x - xOffset][y - yOffset]);
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, char[][] chars, int[][] indices, List<Color> palette) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    SquidPanel.this.put(x, y, chars[x - xOffset][y - yOffset], palette.get(indices[x - xOffset][y - yOffset]));
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, Color[][] foregrounds) {
        for (int x = xOffset; x < xOffset + foregrounds.length; x++) {
            for (int y = yOffset; y < yOffset + foregrounds[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    SquidPanel.this.put(x, y, textFactory.getSolid(foregrounds[x - xOffset][y - yOffset]));
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, int[][] indices, List<Color> palette) {
        for (int x = xOffset; x < xOffset + indices.length; x++) {
            for (int y = yOffset; y < yOffset + indices[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    SquidPanel.this.put(x, y, textFactory.getSolid(palette.get(indices[x - xOffset][y - yOffset])));
                }
            }
        }
    }

    public void put(int xOffset, int yOffset, char[][] chars, Color foreground) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    SquidPanel.this.put(x, y, chars[x - xOffset][y - yOffset], foreground);
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
        SquidPanel.this.put(xOffset, yOffset, string, defaultForeground);
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
     */
    public void put(int xOffset, int yOffset, String string, Color foreground) {//TODO - make this work with code points
        char[][] temp = new char[string.length()][1];
        for (int i = 0; i < string.length(); i++) {
            temp[i][0] = string.charAt(i);
        }
        SquidPanel.this.put(xOffset, yOffset, temp, foreground);
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
        SquidPanel.this.put(xOffset, yOffset, string, defaultForeground, vertical);
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
    public void put(int xOffset, int yOffset, String string, Color foreground, boolean vertical) {//TODO - make this use any Direction
        if (vertical) {
            SquidPanel.this.put(xOffset, yOffset, new char[][]{string.toCharArray()}, foreground);
        } else {
            SquidPanel.this.put(xOffset, yOffset, string, foreground);
        }
    }

    /**
     * Erases the entire panel, leaving only a transparent space.
     */
    public void erase() {
        Graphics2D g = contentsImage.createGraphics();

        Composite c = g.getComposite();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setComposite(c);

        redraw();
    }

    /**
     * Removes the contents of this cell, leaving a transparent space.
     *
     * @param x
     * @param y
     */
    public void clear(int x, int y) {
        this.put(x, y, SColor.TRANSPARENT);
    }

    public void put(int x, int y, Color color) {
        put(x, y, textFactory.getSolid(color));
    }

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

    public void put(int x, int y, char c, Color color) {
        put(x, y, (int) c, color);
    }

    public void put(int x, int y, int index, List<Color> palette) {
        put(x, y, palette.get(index));
    }

    public void put(int x, int y, char c, int index, List<Color> palette) {
        put(x, y, (int) c, palette.get(index));
    }

    /**
     * Takes a unicode codepoint for input.
     *
     * @param x
     * @param y
     * @param code
     * @param color
     */
    public void put(int x, int y, int code, Color color) {
        if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) {
            return;//skip if out of bounds
        }
        contents[x][y] = textFactory.get(code, color);
        imageChanged[x][y] = true;
    }

    public int cellWidth() {
        return cellWidth;
    }

    public int cellHeight() {
        return cellHeight;
    }

    public int gridHeight() {
        return gridHeight;
    }

    public int gridWidth() {
        return gridWidth;
    }

    /**
     * Cause everything that has been prepared for drawing (such as with put) to actually be drawn.
     */
    public void refresh() {
        trimAnimations();
        redraw();
        repaint();
    }

    /**
     * A low-level check to see if a particular cell has been updated since the last draw. Not intended for general use.
     * @param x
     * @param y
     * @return
     */
    public boolean hasChanged(int x, int y)
    {
        return imageChanged[x][y];
    }
    private void redraw() {
        Graphics2D g = contentsImage.createGraphics();

        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                if (imageChanged[x][y]) {
                    Composite c = g.getComposite();
                    g.setComposite(AlphaComposite.Clear);
                    g.fillRect(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                    g.setComposite(c);

                    if (contents[x][y] != null) {
                        g.drawImage(contents[x][y], null, x * cellWidth, y * cellHeight);
                    }
                    imageChanged[x][y] = false;
                }
            }
        }
    }

	/**
	 * Sets the default foreground color.
	 * 
	 * @param defaultForeground
	 *            A non-{@code null} color.
	 */
	public void setDefaultForeground(Color defaultForeground) {
		if (defaultForeground == null)
			throw new NullPointerException("A null default foreground color is forbidden");
		this.defaultForeground = defaultForeground;
	}

	/**
	 * @return The default foreground color (if none was set with
	 *         {@link #setDefaultForeground(Color)}), or the last color set with
	 *         {@link #setDefaultForeground(Color)}. Cannot be {@code null}.
	 */
	public Color getDefaultForegroundColor() {
		return defaultForeground;
	}

    /**
     * Starts a bumping animation in the direction provided.
     *
     * @param location
     * @param direction
     */
    public void bump(Point location, Direction direction) {
        if (contents[location.x][location.y] != null) {
            int duration = 20;
            Animation anim = new BumpAnimation(contents[location.x][location.y], new Point(location.x * cellWidth, location.y * cellHeight), new Dimension(cellWidth / 3, cellHeight / 3), direction, duration);
            contents[location.x][location.y] = null;
            imageChanged[location.x][location.y] = true;
            redraw();
            animations.add(anim);
            if (animationManager == null) {
                animationManager = AnimationManager.startNewAnimationManager(this);
            }
            animationManager.add(anim);
        }
    }

    /**
     * Starts a movement animation for the object at the given grid location at the default speed.
     *
     * @param start
     * @param end
     */
    public void slide(Point start, Point end) {
        slide(start, end, DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Starts a movement animation for the object at the given grid location at the default speed for one grid square in
     * the direction provided.
     *
     * @param start
     * @param direction
     */
    public void slide(Point start, Direction direction) {
        slide(start, new Point(direction.deltaX + start.x, direction.deltaY + start.y), DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Starts a sliding movement animation for the object at the given location at the provided speed. The duration is
     * how many milliseconds should pass for the entire animation.
     *
     * @param start
     * @param end
     * @param duration
     */
    public void slide(Point start, Point end, int duration) {
        if (contents[start.x][start.y] != null) {
            Animation anim = new SlideAnimation(contents[start.x][start.y], new Point(start.x * cellWidth, start.y * cellHeight), new Point(end.x * cellWidth, end.y * cellHeight), duration);
            contents[start.x][start.y] = null;
            imageChanged[start.x][start.y] = true;
            redraw();
            animations.add(anim);
            if (animationManager == null) {
                animationManager = AnimationManager.startNewAnimationManager(this);
            }
            animationManager.add(anim);
        }
    }

    /**
     * Starts an wiggling animation for the object at the given location.
     *
     * @param location
     */
    public void wiggle(Point location) {
        if (contents[location.x][location.y] != null) {
            Animation anim = new WiggleAnimation(contents[location.x][location.y], new Point(location.x * cellWidth, location.y * cellHeight), 0.3, new Point(cellWidth / 4, cellHeight / 4), 160);
            contents[location.x][location.y] = null;
            imageChanged[location.x][location.y] = true;
            redraw();
            animations.add(anim);
            if (animationManager == null) {
                animationManager = AnimationManager.startNewAnimationManager(this);
            }
            animationManager.add(anim);
        }
    }

    /**
     * Drops any finished animations from the animation list.
     */
    private void trimAnimations() {
        if (animationManager == null) {
            return;//no manager means nothing to trim
        }
        LinkedList<Animation> removals = new LinkedList<>();
        for (Animation anim : animations) {
            if (!anim.isActive()) {
                removals.add(anim);
            }
        }
        animations.removeAll(removals);
        for (Animation anim : removals) {
            animationManager.stopAnimation(anim);
            anim.remove();
            contents[anim.getLocation().x / cellWidth][anim.getLocation().y / cellHeight] = anim.getImage();
            imageChanged[anim.getLocation().x / cellWidth][anim.getLocation().y / cellHeight] = true;
        }
    }

    /**
     * Causes the component to stop responding to input until all current animations are finished.
     *
     * Note that if an animation is set to not stop then this method will never return.
     */
    public void waitForAnimations() {
        while (!animations.isEmpty()) {
            trimAnimations();
        }
        redraw();
        repaint();
    }

    /**
     * Returns true if there are animations running when this method is called.
     *
     * Note that due to the nature of animations ending at various times, this result is not guaranteed to be accurate.
     *
     * To fully ensure no animations are running, waitForAnimations() must be used.
     *
     * @return
     */
    public boolean hasActiveAnimations() {
        return animations == null ? false : !animations.isEmpty();
    }

}
