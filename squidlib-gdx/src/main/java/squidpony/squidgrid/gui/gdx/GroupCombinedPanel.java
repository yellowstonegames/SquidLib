package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import squidpony.panel.IColoredString;
import squidpony.panel.ICombinedPanel;
import squidpony.panel.ISquidPanel;

import java.util.LinkedList;
import java.util.List;


/**
 * An implementation of {@link ICombinedPanel} that extends libGDX's Group.
 * If you're a new user or need only a foreground and background, it's
 * likely what you should use.
 *
 * this is a concrete implementation of {@link ICombinedPanel} that you should
 * use if you're concretely in need of a panel to display/write to, without
 * doing fancy GUI stuff. Because it extends libGDX's {@link Group}, it
 * offers a lot of features.
 * 
 * @see SquidLayers for a more advanced Group that supports multiple layers.
 * @author smelC
 */
public class GroupCombinedPanel<T> extends Group implements ICombinedPanel<T> {

    protected/* @Nullable */ISquidPanel<T> bg;
    protected/* @Nullable */ISquidPanel<T> fg;

    /** The width, in cell sizes */
    protected int gridWidth = -1;

    /** The height, in cell sizes */
    protected int gridHeight = -1;

    /**
     * @param bg
     *            The backing background panel. Typically a SquidPanel from this package.
     * @param fg
     *            The backing foreground panel. Typically a SquidPanel from this package.
     * @param gridWidth
     *            The width of this panel, used for {@link #fillBG(Object)}
     *            (so that it fills within {@code [0, width)}).
     * @param gridHeight
     *            The height of this panel, used for {@link #fillBG(Object)}
     *            (so that it fills within {@code [0, height)}).
     * @throws IllegalStateException
     *             In various cases of errors regarding sizes of panels.
     */
    public GroupCombinedPanel(ISquidPanel<T> bg, ISquidPanel<T> fg,
                              int gridWidth, int gridHeight) {
        if (bg.gridWidth() != fg.gridWidth())
            throw new IllegalStateException(
                    "Cannot build a combined panel with backers of different widths");
        if (bg.gridHeight() != fg.gridHeight())
            throw new IllegalStateException(
                    "Cannot build a combined panel with backers of different heights");

        this.bg = bg;
        this.fg = fg;
        if (gridWidth < 0)
            throw new IllegalStateException("Cannot create a panel with a negative width");
        this.gridWidth = gridWidth;
        if (gridHeight < 0)
            throw new IllegalStateException("Cannot create a panel with a negative height");
        this.gridHeight = gridHeight;

        addActors();
    }

    /**
     * Constructor that defer providing the backing panels. Useful for
     * subclasses that compute their size after being constructed. Use
     * {@link #setPanels(ISquidPanel, ISquidPanel)} to set the panels
     * (required before calling any {@code put} method).
     *
     * <p>
     * Width and height are computed using the provided panels.
     * </p>
     */
    public GroupCombinedPanel() {
    }

    /**
     * Sets the backing panels.
     *
     * @param bg Typically a SquidPanel from this package.
     * @param fg Typically a SquidPanel from this package.
     */
    public void setPanels(ISquidPanel<T> bg, ISquidPanel<T> fg) {
        if (this.bg != null)
            throw new IllegalStateException("Cannot change the background panel");
        this.bg = bg;

        if (this.fg != null)
            throw new IllegalStateException("Cannot change the foreground panel");
        this.fg = fg;

        if (bg.gridWidth() != fg.gridWidth())
            throw new IllegalStateException(
                    "Cannot build a combined panel with backers of different widths");
        if (bg.gridHeight() != fg.gridHeight())
            throw new IllegalStateException(
                    "Cannot build a combined panel with backers of different heights");

        this.gridWidth = bg.gridWidth();
        this.gridHeight = bg.gridHeight();

        addActors();
    }

    @Override
    public void putFG(int x, int y, char c) {
        checkFG();
        fg.put(x, y, c);
    }

    @Override
    public void putFG(int x, int y, char c, T color) {
        checkFG();
        fg.put(x, y, c, color);
    }

    @Override
    public void putFG(int x, int y, String string, T foreground) {
        checkFG();
        fg.put(x, y, string, foreground);
    }

    @Override
    public void putFG(int x, int y, IColoredString<? extends T> cs) {
        checkFG();
        fg.put(x, y, cs);
    }

    @Override
    public void putBG(int x, int y, T color) {
        checkBG();
        bg.put(x, y, color);
    }

    public void put(int x, int y, char c, T foreground, T background)
    {
        checkFG();
        checkBG();
        bg.put(x, y, background);
        fg.put(x, y, c, foreground);
    }
    public void put(int x, int y, IColoredString<? extends T> cs, T background)
    {
        checkFG();
        checkBG();
        for (int i = x; i < cs.length() && i < gridWidth; i++) {
            bg.put(i, y, background);
        }
        fg.put(x, y, cs);
    }
    public void put(int x, int y, String s, T foreground, T background)
    {
        checkFG();
        checkBG();
        for (int i = x; i < s.length() && i < gridWidth; i++) {
            bg.put(i, y, background);
        }
        fg.put(x, y, s, foreground);
    }
    
    @Override
    public void fillBG(T color) {
        if (gridWidth < 0 || gridHeight < 0)
            throw new IllegalStateException("Width and height must be set before calling fillBG");
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++)
                putBG(x, y, color);
        }
    }

    @Override
    public void refresh() {
        bg.refresh();
        fg.refresh();
    }

    @Override
    public List<ISquidPanel<?>> getBackers() {
        final List<ISquidPanel<?>> backers = new LinkedList<ISquidPanel<?>>();
        backers.add(fg.getBacker());
        backers.add(bg.getBacker());
        return backers;
    }

    protected void addActors() {
        addActor((SquidPanel) bg.getBacker());
        addActor((SquidPanel) fg.getBacker());
    }

    protected void checkFG() {
        if (fg == null)
            throw new NullPointerException("The foreground panel must be set before writing to it");
    }

    protected void checkBG() {
        if (bg == null)
            throw new NullPointerException("The background panel must be set before writing to it");
    }

    @Override
    public String toString() {
        return String.format("%s@%s", this.getClass().getSimpleName(), Integer.toHexString(hashCode()));
    }

}
