package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.scenes.scene2d.Group;

import squidpony.panel.IColoredString;
import squidpony.panel.ICombinedPanel;
import squidpony.panel.ISquidPanel;

/**
 * An implementation of {@link ICombinedPanel} that extends libGDX's Group. If
 * you're a new user or need only a foreground and background, it's likely what
 * you should use.
 *
 * this is a concrete implementation of {@link ICombinedPanel} that you should
 * use if you're concretely in need of a panel to display/write to, without
 * doing fancy GUI stuff. Because it extends libGDX's {@link Group}, it offers a
 * lot of features.
 * 
 * @see SquidLayers for a more advanced Group that supports multiple layers.
 * @author smelC
 * 
 * @param <T>
 *            The type of colors.
 */
public class GroupCombinedPanel<T> extends Group implements ICombinedPanel<T> {

	protected/* @Nullable */ISquidPanel<T> bg;
	protected/* @Nullable */ISquidPanel<T> fg;

	/**
	 * @param bg
	 *            The backing background panel. Typically a {@link SquidPanel}.
	 * @param fg
	 *            The backing foreground panel. Typically a {@link SquidPanel}.
	 * @throws IllegalStateException
	 *             In various cases of errors regarding sizes of panels.
	 */
	public GroupCombinedPanel(ISquidPanel<T> bg, ISquidPanel<T> fg) {
		setPanels(bg, fg);
	}

	/**
	 * Constructor that defer providing the backing panels. Useful for
	 * subclasses that compute their size after being constructed. Use
	 * {@link #setPanels(ISquidPanel, ISquidPanel)} to set the panels (required
	 * before calling any {@code put} method).
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
	 * @param bg
	 *            Typically a {@link SquidPanel}.
	 * @param fg
	 *            Typically a {@link SquidPanel}.
	 * @throws IllegalStateException
	 *             In various cases of errors regarding sizes of panels.
	 */
	public final void setPanels(ISquidPanel<T> bg, ISquidPanel<T> fg) {
		if (this.bg != null)
			throw new IllegalStateException("Cannot change the background panel");
		this.bg = bg;

		if (this.fg != null)
			throw new IllegalStateException("Cannot change the foreground panel");
		this.fg = fg;

		final int bgw = bg.gridWidth();
		final int bgh = bg.gridHeight();

		if (bgw != fg.gridWidth())
			throw new IllegalStateException("Cannot build a combined panel with backers of different widths");
		if (bgh != fg.gridHeight())
			throw new IllegalStateException(
					"Cannot build a combined panel with backers of different heights");

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

	@Override
	public void put(int x, int y, char c, T background, T foreground) {
		checkFG();
		checkBG();
		bg.put(x, y, background);
		fg.put(x, y, c, foreground);
	}

	@Override
	public void put(int x, int y, T background, IColoredString<? extends T> cs) {
		checkFG();
		checkBG();
		final int w = getGridWidth();
		final int l = cs.length();
		for (int i = x; i < l && i < w; i++) {
			bg.put(i, y, background);
		}
		fg.put(x, y, cs);
	}

	@Override
	public void put(int x, int y, String s, T background, T foreground) {
		checkFG();
		checkBG();
		final int w = getGridWidth();
		final int l = s.length();
		for (int i = x; i < l && i < w; i++) {
			bg.put(i, y, background);
		}
		fg.put(x, y, s, foreground);
	}

	/**
	 * Writes {@code string} at the bottom right. If {@code string} is wider
	 * than {@code this}, its end will be stripped.
	 * 
	 * @param string
	 */
	public void putBottomRight(IColoredString<? extends T> string) {
		final int width = bg.gridWidth();
		final int len = string.length();
		final int x = len < width ? width - len : 0;
		fg.put(x, bg.gridHeight() - 1, string);
	}

	@Override
	public void fillBG(T color) {
		final int gridWidth = getGridWidth();
		final int gridHeight = getGridHeight();

		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++)
				putBG(x, y, color);
		}
	}

	/**
	 * @return The backer's width
	 * @throws IllegalStateException
	 *             If backers aren't set yet.
	 */
	public int getGridWidth() {
		if (bg == null)
			throw new NullPointerException("The background panel must be set before requesting the width");
		return bg.gridWidth();
	}

	/**
	 * @return The backer's height
	 * @throws IllegalStateException
	 *             If backers aren't set yet.
	 */
	public int getGridHeight() {
		if (bg == null)
			throw new NullPointerException("The background panel must be set before requesting the height");
		return bg.gridHeight();
	}

	/**
	 * @return The width of cells in the backing panel.
	 */
	public int cellWidth() {
		return bg.cellWidth();
	}

	/**
	 * @return The height of cells in the backing panel.
	 */
	public int cellHeight() {
		return bg.cellHeight();
	}

	@Override
	public void refresh() {
		bg.refresh();
		fg.refresh();
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
