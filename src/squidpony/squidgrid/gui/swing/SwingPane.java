package squidpony.squidgrid.gui.swing;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JLayeredPane;
import squidpony.squidcolor.SColor;
import squidpony.squidgrid.gui.SGPane;
import squidpony.squidgrid.gui.awt.ImageCellMap;
import squidpony.squidgrid.gui.awt.TextCellFactory;
import squidpony.squidgrid.gui.swing.animation.Animation;
import squidpony.squidgrid.gui.swing.animation.AnimationManager;
import squidpony.squidgrid.gui.swing.animation.BumpAnimation;
import squidpony.squidgrid.gui.swing.animation.SlideAnimation;
import squidpony.squidgrid.gui.swing.animation.WiggleAnimation;
import squidpony.squidgrid.util.Direction;

/**
 * Displays text and images in a grid pattern. Supports basic animations.
 *
 * When text is placed, the background color is set separately from the
 * foreground character. When moved, only the foreground character is moved.
 *
 * @author Eben Howard - http://squidpony.com
 */
public class SwingPane extends JLayeredPane implements SGPane {

    private static int DEFAULT_MOVEMENT_SPEED = 0; //one move step per x milliseconds
    private AnimationManager animationManager;//don't instantiate until an animation is needed
    private ConcurrentLinkedQueue<Animation> animations = new ConcurrentLinkedQueue<>();
    private BufferedImage[][] backgroundContents;
    private boolean[][] imageChanged;
    private Dimension cellDimension;
    private BufferedImage contentsImage = new BufferedImage(20, 20, BufferedImage.TYPE_4BYTE_ABGR);
    private Color defaultBackground = SColor.BLACK;
    private Color defaultForeground = SColor.WHITE;
    private BufferedImage[][] foregroundContents;
    private int gridHeight;
    private int gridWidth;
    private Dimension panelDimension;
    private BufferedImage worldBackgroundImage = null;
    private TextCellFactory textFactory = new TextCellFactory();
    private ImageCellMap imageCellMap;

    /**
     * Builds a new panel with the desired traits. The size of the font will be
     * used unless it's too large to fit, in which case it will be stepped down
     * until the characters fit.
     *
     * @param cellWidth cell width in pixels.
     * @param cellHeight cell height in pixels.
     * @param gridWidth number of cells vertically..
     * @param gridHeight number of cells horizontally.
     * @param font
     */
    public SwingPane(int cellWidth, int cellHeight, int gridWidth, int gridHeight, Font font) {
        initialize(cellWidth, cellHeight, gridWidth, gridHeight, font);
    }

    /**
     * Builds a panel with the given Font determining the size of the cell
     * dimensions.
     *
     * @param gridWidth
     * @param gridHeight
     * @param font
     */
    public SwingPane(int gridWidth, int gridHeight, Font font) {
        initialize(gridWidth, gridHeight, font);
    }

    /**
     * Empty constructor. One of the initialization methods must be called
     * before this panel is used.
     */
    public SwingPane() {
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

    @Override
    public void setText(char[][] chars) {
        placeText(0, 0, chars);
    }

    public void setImageCellMap(ImageCellMap map) {
        imageCellMap = map;
    }

    public void setTextCellFactory(TextCellFactory factory) {
        textFactory = factory;
    }

    public TextCellFactory getTextFactory() {
        return textFactory;
    }

    public ImageCellMap getImageCellMap() {
        return imageCellMap;
    }

    @Override
    public void placeImage(int x, int y, String key) {
        BufferedImage image = imageCellMap.getImage(key);
        if (image == null) {
            image = imageCellMap.getNullImage();
        }
        foregroundContents[x][y] = image;
        imageChanged[x][y] = true;
    }

    @Override
    public void placeImage(int x, int y, String key, Color background) {
        BufferedImage image = imageCellMap.getImage(key);
        if (image == null) {
            image = imageCellMap.getNullImage();
        }
        backgroundContents[x][y] = textFactory.getImageFor(' ', background, background);
        foregroundContents[x][y] = image;
        imageChanged[x][y] = true;
    }

    @Override
    public void placeText(int xOffset, int yOffset, char[][] chars) {
        placeText(xOffset, yOffset, chars, defaultForeground, null);
    }

    @Override
    public void placeText(int xOffset, int yOffset, char[][] chars, Color foreground, Color background) {
        for (int x = xOffset; x < xOffset + chars.length; x++) {
            for (int y = yOffset; y < yOffset + chars[0].length; y++) {
                if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {//check for valid input
                    if (background != null) {
                        placeCharacter(x, y, chars[x - xOffset][y - yOffset], foreground, background);
                    } else {
                        placeCharacter(x, y, chars[x - xOffset][y - yOffset], foreground);
                    }
                }
            }
        }
    }

    @Override
    public void placeHorizontalString(int xOffset, int yOffset, String string) {
        placeHorizontalString(xOffset, yOffset, string, defaultForeground, null);
    }

    @Override
    public void placeHorizontalString(int xOffset, int yOffset, String string, Color foreground, Color background) {
        char[][] temp = new char[string.length()][1];
        for (int i = 0; i < string.length(); i++) {
            temp[i][0] = string.charAt(i);
        }
        placeText(xOffset, yOffset, temp, foreground, background);
    }

    @Override
    public void placeVerticalString(int xOffset, int yOffset, String string, Color foreground, Color background) {
        placeText(xOffset, yOffset, new char[][]{string.toCharArray()}, foreground, background);
    }

    @Override
    public void placeVerticalString(int xOffset, int yOffset, String string) {
        placeVerticalString(xOffset, yOffset, string, defaultForeground, null);
    }

    @Override
    public void clearCell(int x, int y) {
        clearCell(x, y, defaultBackground);
    }

    @Override
    public void clearCell(int x, int y, Color color) {
        placeCharacter(x, y, ' ', color, color);
    }

    @Override
    public void setCellBackground(int x, int y, Color color) {
        BufferedImage fore = foregroundContents[x][y];
        clearCell(x, y, color);
        foregroundContents[x][y] = fore;
    }

    @Override
    public void placeCharacter(int x, int y, char c) {
        placeCharacter(x, y, c, defaultForeground);
    }

    @Override
    public void placeCharacter(int x, int y, char c, Color fore, Color back) {
        if (c != ' ') {
            foregroundContents[x][y] = textFactory.getImageFor(c, fore);
        } else {
            foregroundContents[x][y] = null;
        }

        if (back.equals(defaultBackground)) {
            backgroundContents[x][y] = null;
        } else {
            backgroundContents[x][y] = textFactory.getImageFor(' ', defaultForeground, back);
        }
        imageChanged[x][y] = true;
    }

    @Override
    public void placeCharacter(int x, int y, char c, Color fore) {
        foregroundContents[x][y] = textFactory.getImageFor(c, fore);
        imageChanged[x][y] = true;
    }

    @Override
    public void initialize(int cellWidth, int cellHeight, int gridWidth, int gridHeight, Font font) {
        textFactory.initializeBySize(cellWidth, cellHeight, font);
        setFont(textFactory.getFont());
        doInitialization(gridWidth, gridHeight);
        imageCellMap = new ImageCellMap(cellDimension);
    }

    @Override
    public void initialize(int gridWidth, int gridHeight, Font font) {
        textFactory.initializeByFont(font);
        setFont(textFactory.getFont());
        doInitialization(gridWidth, gridHeight);
        imageCellMap = new ImageCellMap(cellDimension);
    }

    /**
     * Sets the character set that will be guaranteed to fit on the next
     * initialization of the grid.
     *
     * @param characters
     */
    public void ensureFits(char[] characters) {
        textFactory.setFitCharacters(characters);
    }

    @Override
    public boolean willFit(char character) {
        return textFactory.willFit(character);
    }

    /**
     * Clears backing arrays and sets fields to proper size for the new grid
     * size.
     */
    private void doInitialization(int gridWidth, int gridHeight) {
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
        backgroundContents = new BufferedImage[gridWidth][gridHeight];
        foregroundContents = new BufferedImage[gridWidth][gridHeight];
        imageChanged = new boolean[gridWidth][gridHeight];
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                imageChanged[x][y] = true;
            }
        }
        setBackground(defaultBackground);
        cellDimension = textFactory.getCellDimension();
        int w = gridWidth * cellDimension.width;
        int h = gridHeight * cellDimension.height;
        panelDimension = new Dimension(w, h);
        setSize(panelDimension);
        setMinimumSize(panelDimension);
        setPreferredSize(panelDimension);
        contentsImage = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        refresh();
    }

    public Dimension getCellDimension() {
        return cellDimension;
    }

    @Override
    public int getGridHeight() {
        return gridHeight;
    }

    @Override
    public int getGridWidth() {
        return gridWidth;
    }

    @Override
    public void refresh() {
        trimAnimations();
        redraw();
        repaint();
    }

    private void redraw() {
        Graphics2D g = contentsImage.createGraphics();
        if (worldBackgroundImage != null) {
            Composite backup = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            g.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
            g.setComposite(backup);
        }
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                if (imageChanged[x][y]) {
                    //clear to background color
                    g.setColor(defaultBackground);
                    g.fillRect(x * cellDimension.width, y * cellDimension.height, cellDimension.width, cellDimension.height);
                    if (backgroundContents[x][y] != null) {
                        g.drawImage(backgroundContents[x][y], null, x * cellDimension.width, y * cellDimension.height);
                    }
                    if (foregroundContents[x][y] != null) {
                        g.drawImage(foregroundContents[x][y], null, x * cellDimension.width, y * cellDimension.height);
                    }
                    imageChanged[x][y] = false;
                }
            }
        }
    }

    @Override
    public void setDefaultBackground(Color defaultBackground) {
        this.defaultBackground = defaultBackground;
    }

    @Override
    public void setDefaultForeground(Color defaultForeground) {
        this.defaultForeground = defaultForeground;
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
            Animation anim = new BumpAnimation(foregroundContents[location.x][location.y], new Point(location.x * cellDimension.width, location.y * cellDimension.height), new Dimension(cellDimension.width / 3, cellDimension.height / 3), direction);
            foregroundContents[location.x][location.y] = null;
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
     * Starts a movement animation for the object at the given grid location at
     * the default speed.
     *
     * @param start
     * @param end
     */
    public void slide(Point start, Point end) {
        slide(start, end, DEFAULT_MOVEMENT_SPEED);
    }

    /**
     * Starts a movement animation for the object at the given grid location at
     * the default speed for one grid square in the direction provided.
     *
     * @param start
     * @param direction
     */
    public void slide(Point start, Direction direction) {
        slide(start, new Point(direction.deltaX + start.x, direction.deltaY + start.y), DEFAULT_MOVEMENT_SPEED);
    }

    /**
     * Starts a sliding movement animation for the object at the given location
     * at the provided speed. The speed is how many milliseconds should pass
     * between movement steps.
     *
     * @param start
     * @param end
     * @param speed
     */
    public void slide(Point start, Point end, int speed) {
        if (foregroundContents[start.x][start.y] != null) {
            Animation anim = new SlideAnimation(foregroundContents[start.x][start.y], new Point(start.x * cellDimension.width, start.y * cellDimension.height), new Point(end.x * cellDimension.width, end.y * cellDimension.height), speed);
            foregroundContents[start.x][start.y] = null;
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
        if (foregroundContents[location.x][location.y] != null) {
            Animation anim = new WiggleAnimation(foregroundContents[location.x][location.y], new Point(location.x * cellDimension.width, location.y * cellDimension.height), 0.3, new Point(cellDimension.width / 4, cellDimension.height / 4), 160);
            foregroundContents[location.x][location.y] = null;
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
            foregroundContents[anim.getLocation().x / cellDimension.width][anim.getLocation().y / cellDimension.height] = anim.getImage();
            imageChanged[anim.getLocation().x / cellDimension.width][anim.getLocation().y / cellDimension.height] = true;
        }
    }

    /**
     * Causes the component to stop responding to input until all current
     * animations are finished.
     *
     * Note that if an animation is set to not stop then this method will never
     * return.
     */
    public void waitForAnimations() {
        while (!animations.isEmpty()) {
            trimAnimations();
        }
        redraw();
        repaint();
    }

    @Override
    public int getCellHeight() {
        return cellDimension.height;
    }

    @Override
    public int getCellWidth() {
        return cellDimension.width;
    }
}
