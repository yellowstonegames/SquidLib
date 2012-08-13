package squidpony.squidgrid.gui;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import javax.swing.JLayeredPane;
import squidpony.squidcolor.SColor;
import squidpony.squidgrid.gui.animation.Animation;
import squidpony.squidgrid.gui.animation.AnimationManager;
import squidpony.squidgrid.gui.animation.BumpAnimation;
import squidpony.squidgrid.gui.animation.SlideAnimation;
import squidpony.squidgrid.gui.animation.WiggleAnimation;
import squidpony.squidgrid.util.Direction;

/**
 * This class is a JComponent that will display a text string as a monospaced font
 * regardless of the font's actual spacing.
 *
 * @author Eben Howard - http://squidpony.com
 */
public class SGTextPanel extends JLayeredPane {
    private AnimationManager animationManager;
    private static int DEFAULT_MOVEMENT_SPEED = 3;//one move step per x milliseconds
    private BufferedImage[][] backgroundContents, foregroundContents;
    private LinkedList<Animation> animations = new LinkedList<Animation>();
    private int gridHeight, gridWidth;
    private Dimension cellDimension, panelDimension;
    private BufferedImage contentsImage = new BufferedImage(20, 20, BufferedImage.TYPE_4BYTE_ABGR),
            worldBackgroundImage = null;
    private TextBlockFactory factory = TextBlockFactory.getInstance();
    private Color defaultForeground = SColor.WHITE;
    private Color defaultBackground = SColor.BLACK;

    /**
     * Builds a new panel with the desired traits. The size of the font will be
     * used unless it's too large to fit, in which case it will be stepped down
     * until the characters fit.
     *
     * @param cellWidth cell width in pixels.
     * @param cellHeight cell height in pixels.
     * @param panelWidth number of cells vertically..
     * @param panelHeight number of cells horizontally.
     * @param font
     */
    public SGTextPanel(int cellWidth, int cellHeight, int panelWidth, int panelHeight, Font font) {
        initialize(cellWidth, cellHeight, panelWidth, panelHeight, font);
    }

    /**
     * Builds a panel with the given Font determining the size of the cell
     * dimensions.
     *
     * @param panelWidth
     * @param panelHeight
     * @param font
     */
    public SGTextPanel(int panelWidth, int panelHeight, Font font) {
        initialize(panelWidth, panelHeight, font);
    }

    /**
     * Empty constructor. One of the initialization methods must be called
     * before this panel is used.
     */
    public SGTextPanel() {
    }

    @Override
    public void paintComponent(Graphics g) {
        if (worldBackgroundImage != null) {
            g.drawImage(worldBackgroundImage, 0, 0, null);
        }
        g.drawImage(contentsImage, 0, 0, null);
        paintComponents(g);
        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Sets the contents of the component to reflect the two dimensional
     * character array. Will ignore any portion of the array that is outside the
     * bounds of the component itself.
     *
     * The default colors of the foreground and background will be used.
     *
     * @param chars
     */
    public void setText(char[][] chars) {
        placeText(0, 0, chars);
    }

    /**
     * Sets the contents of the component to reflect the two dimensional
     * character array, starting at the given offset position.
     *
     * Any content that would be off the screen to the right or down is ignored.
     *
     * @param chars
     * @param xOffset
     * @param yOffset
     */
    public void placeText(int xOffset, int yOffset, char[][] chars) {
        placeText(xOffset, yOffset, chars, defaultForeground, defaultBackground);
    }

    /**
     * Sets the contents of the component to reflect the two dimensional
     * character array, starting at the given offset position.
     *
     * @param xOffset
     * @param yOffset
     * @param chars
     * @param foreground
     * @param background
     */
    public void placeText(int xOffset, int yOffset, char[][] chars, Color foreground, Color background) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    placeCharacter(x, y, chars[x - xOffset][y - yOffset], foreground, background);
                }
            }
        }
    }

    /**
     * Prints out a string starting at the given offset position. Any portion of
     * the string that would cross the edge is ignored.
     *
     * @param string
     * @param xOffset
     * @param yOffset
     */
    public void placeHorizontalString(int xOffset, int yOffset, String string) {
        placeHorizontalString(xOffset, yOffset, string, defaultForeground, defaultBackground);
    }

    /**
     * Prints out a string vertically starting at the given offset position and
     * traveling down.
     *
     * @param xOffset
     * @param yOffset
     * @param string
     * @param foreground
     * @param background
     */
    public void placeHorizontalString(int xOffset, int yOffset, String string, Color foreground, Color background) {
        char[][] temp = new char[string.length()][1];
        for (int i = 0; i < string.length(); i++) {
            temp[i][0] = string.charAt(i);
        }
        placeText(xOffset, yOffset, temp, foreground, background);
    }

    /**
     * Prints out a string starting at the given offset position. Any portion of
     * the string that would cross the edge is ignored.
     *
     * @param xOffset
     * @param yOffset
     * @param string
     * @param foreground
     * @param background
     */
    public void placeVerticalString(int xOffset, int yOffset, String string, Color foreground, Color background) {
        placeText(xOffset, yOffset, new char[][]{string.toCharArray()}, foreground, background);
    }

    /**
     * Prints out a string vertically starting at the given offset position and
     * traveling down.
     *
     * @param xOffset
     * @param yOffset
     * @param string
     */
    public void placeVerticalString(int xOffset, int yOffset, String string) {
        placeVerticalString(xOffset, yOffset, string, defaultForeground, defaultBackground);
    }

    /**
     * Sets one specific block to the given character.
     *
     * This block is not drawn immediately, refresh() must be called to update
     * display.
     *
     * @param x The x coordinate to set
     * @param y The y coordinate to set
     * @param c The character to be displayed
     */
    public void placeCharacter(int x, int y, char c) {
        placeCharacter(x, y, c, defaultForeground);
    }

    /**
     * Sets one specific block to the given character with the given foreground
     * and background colors.
     *
     * This block is not drawn immediately, refresh() must be called to update
     * display.
     *
     * @param x The x coordinate to set
     * @param y The y coordinate to set
     * @param c The character to be displayed
     * @param fore The foreground color
     * @param back The background color
     */
    public void placeCharacter(int x, int y, char c, Color fore, Color back) {
        if (c != ' ') {
            foregroundContents[x][y] = factory.getImageFor(c, fore);
        } else {
            foregroundContents[x][y] = null;
        }

        if (back.equals(defaultBackground)) {
            backgroundContents[x][y] = null;
        } else {
            backgroundContents[x][y] = factory.getImageFor(' ', defaultForeground, back);
        }

    }

    public void placeCharacter(int x, int y, char c, Color fore) {
        foregroundContents[x][y] = factory.getImageFor(c, fore);
    }

    /**
     * Starts a movement animation for the object at the given grid location at the default speed.
     * 
     * @param start
     * @param end 
     */
    public void slide(Point start, Point end) {
        slide(start, end, DEFAULT_MOVEMENT_SPEED);
    }

    /**
     * Starts a movement animation for the object at the given grid location at the default speed
     * for one grid square in the direction provided.
     * 
     * @param start
     * @param direction 
     */
    public void slide(Point start, Direction direction) {
        slide(start, new Point(direction.deltaX + start.x, direction.deltaY + start.y), DEFAULT_MOVEMENT_SPEED);
    }

    /**
     * Starts a sliding movement animation for the object at the given location at the provided speed.
     * The speed is how many milliseconds should pass between movement steps.
     * 
     * @param start
     * @param end
     * @param speed 
     */
    public void slide(Point start, Point end, int speed) {
        if (foregroundContents[start.x][start.y] != null) {
            Animation anim = new SlideAnimation(foregroundContents[start.x][start.y],
                    new Point(start.x * cellDimension.width, start.y * cellDimension.height),
                    new Point(end.x * cellDimension.width, end.y * cellDimension.height), speed);
            foregroundContents[start.x][start.y] = null;
            animations.add(anim);
            animationManager.add(anim);
        }
    }

    /**
     * Starts an wiggling animation for the object at the given location.
     * 
     * @param location 
     */
    public void wiggle(Point location) {
        if (foregroundContents[location.x][location.y] != null) {
            Animation anim = new WiggleAnimation(foregroundContents[location.x][location.y],
                    new Point(location.x * cellDimension.width, location.y * cellDimension.height), 0.3,
                    new Point(cellDimension.width / 4, cellDimension.height / 4), 500);
            foregroundContents[location.x][location.y] = null;
            animations.add(anim);
            animationManager.add(anim);
        }
    }

    /**
     * Starts a bumping animation in the direction provided.
     * 
     * @param location
     * @param direction 
     */
    public void bump(Point location, Direction direction) {
        bump(location, new Point(direction.deltaX, direction.deltaY));
    }

    /**
     * Starts a bumping animation in the direction provided.
     * 
     * @param location
     * @param direction 
     */
    public void bump(Point location, Point direction) {
        if (foregroundContents[location.x][location.y] != null) {
            Animation anim = new BumpAnimation(foregroundContents[location.x][location.y],
                    new Point(location.x * cellDimension.width, location.y * cellDimension.height), cellDimension, direction);
            foregroundContents[location.x][location.y] = null;
            animations.add(anim);
            animationManager.add(anim);
        }
    }

    private void trimAnimations() {
        LinkedList<Animation> removals = new LinkedList<Animation>();
        for (Animation anim : animations) {
            if (!anim.isActive()) {
                removals.add(anim);
            }
        }

        animations.removeAll(removals);
        for (Animation anim : removals) {
            animationManager.stopAnimation(anim);
            anim.remove();
            foregroundContents[anim.getLocation().x / cellDimension.width][anim.getLocation().y / cellDimension.height] = anim.getImage();
        }
    }
    

    /**
     * Signals that this component should update its display image.
     */
    public void refresh() {
        trimAnimations();

        Graphics2D g = contentsImage.createGraphics();

        if (worldBackgroundImage == null) {//clear to background color
            g.setColor(defaultBackground);
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
            Composite backup = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            g.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
            g.setComposite(backup);
        }

        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                if (backgroundContents[x][y] != null) {
                    g.drawImage(backgroundContents[x][y], null, x * cellDimension.width, y * cellDimension.height);
                }
                if (foregroundContents[x][y] != null) {
                    g.drawImage(foregroundContents[x][y], null, x * cellDimension.width, y * cellDimension.height);
                }
            }
        }

        repaint();
    }

    /**
     * Initializes the component with the supplied values. The cells will be set
     * to the desired width and height and if the size of the font is too large,
     * it will be shrunk until everything fits.
     *
     * @param cellWidth in pixels
     * @param cellHeight in pixels
     * @param panelWidth in cells
     * @param panelHeight in cells
     * @param font
     */
    public void initialize(int cellWidth, int cellHeight, int panelWidth, int panelHeight, Font font) {
        factory.initializeBySize(cellWidth, cellHeight, font);
        doInitialization(panelWidth, panelHeight);
    }

    /**
     * Initializes the component with the supplied number of rows and columns.
     * The size of the display will be adjusted to match the requested font size
     * as closely as possible.
     *
     * @param panelWidth in cells
     * @param panelHeight in cells
     * @param font
     */
    public void initialize(int panelWidth, int panelHeight, Font font) {
        factory.initializeByFont(font);
        doInitialization(panelWidth, panelHeight);
    }

    private void doInitialization(int gridWidth, int gridHeight) {
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;

        backgroundContents = new BufferedImage[gridWidth][gridHeight];
        foregroundContents = new BufferedImage[gridWidth][gridHeight];
        setBackground(defaultBackground);

        cellDimension = factory.getCellDimension();

        int w = gridWidth * cellDimension.width;
        int h = gridHeight * cellDimension.height;
        panelDimension = new Dimension(w, h);

        setSize(panelDimension);
        setMinimumSize(panelDimension);
        setPreferredSize(panelDimension);

        contentsImage = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        animationManager = AnimationManager.startNewAnimationManager(this);

        refresh();
    }

    /**
     * Sets the character set that will be guaranteed to fit on the next
     * initialization of the grid.
     *
     * If whiteSpace is true, then every character is guaranteed to have at
     * least one pixel between it and the edge of the cell. If false, some
     * characters might sit against the edge of the cell, but will not be
     * cropped.
     *
     * @param characters
     * @param whiteSpace
     */
    public void ensureFits(char[] characters, boolean whiteSpace) {
        factory.setFitCharacters(characters, whiteSpace);
    }

    /**
     * Test if the given character will fit in the current cell dimension using
     * the current Font.
     *
     * @param character
     * @return true if it will fit, false otherwise.
     */
    public boolean willFit(char character) {
        return factory.willFit(character);
    }

    public void setDefaultBackground(Color defaultBackground) {
        this.defaultBackground = defaultBackground;
    }

    public void setDefaultForeground(Color defaultForeground) {
        this.defaultForeground = defaultForeground;
    }

    public Dimension getCellDimension() {
        return cellDimension;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public int getGridWidth() {
        return gridWidth;
    }
}
