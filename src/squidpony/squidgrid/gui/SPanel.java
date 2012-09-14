package squidpony.squidgrid.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JLayeredPane;
import squidpony.squidcolor.SColor;
import squidpony.squidgrid.gui.animation.Animation;
import squidpony.squidgrid.gui.animation.AnimationManager;
import squidpony.squidgrid.gui.animation.BumpAnimation;
import squidpony.squidgrid.gui.animation.SlideAnimation;
import squidpony.squidgrid.gui.animation.WiggleAnimation;
import squidpony.squidgrid.util.Direction;

/**
 *
 * @author Eben Howard
 */
public class SPanel extends JLayeredPane {

    protected static int DEFAULT_MOVEMENT_SPEED = 1; //one move step per x milliseconds
    protected AnimationManager animationManager;
    protected ConcurrentLinkedQueue<Animation> animations = new ConcurrentLinkedQueue<Animation>();
    protected BufferedImage[][] backgroundContents;
    protected boolean[][] imageChanged;
    protected Dimension cellDimension;
    protected BufferedImage contentsImage = new BufferedImage(20, 20, BufferedImage.TYPE_4BYTE_ABGR);
    protected Color defaultBackground = SColor.BLACK;
    protected Color defaultForeground = SColor.WHITE;
    protected BufferedImage[][] foregroundContents;
    protected int gridHeight;
    protected int gridWidth;
    protected Dimension panelDimension;
    protected BufferedImage worldBackgroundImage = null;

    public SPanel() {
    }

    protected void doInitialization(int gridWidth, int gridHeight) {
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
        cellDimension = TextCellFactory.getInstance().getCellDimension();
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

    public Dimension getCellDimension() {
        return cellDimension;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    /**
     * Signals that this component should update its display image.
     */
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

    public void setDefaultBackground(Color defaultBackground) {
        this.defaultBackground = defaultBackground;
    }

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
            animationManager.add(anim);
        }
    }

    protected void trimAnimations() {
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
            imageChanged[anim.getLocation().x / cellDimension.width][anim.getLocation().y / cellDimension.height] = true;
        }
    }

    public void waitForAnimations() {
        while (!animations.isEmpty()) {
            trimAnimations();
        }
        redraw();
        repaint();
    }
}
