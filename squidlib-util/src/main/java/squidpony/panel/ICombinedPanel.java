package squidpony.panel;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Group;

import squidpony.annotation.Beta;
import squidpony.squidgrid.gui.gdx.SquidPanel;

/**
 * The combination of two panels, one to color the background, the other to
 * write characters on the foreground.
 * 
 * <p>
 * There are two implementations:
 * 
 * <ol>
 * <li>a very generic one: {@link Impl} that you should use if you're combining
 * generic things.</li>
 * <li>a more concrete one: {@link GroupImpl} that you should use if you're
 * concretely in need of a panel to display/write to, without doing fancy GUI
 * stuff. Because it extends libgdx's {@link Group}, it offers a lot of
 * features.</li>
 * </p>
 * 
 * @author smelC
 * 
 * @param <T>
 *            The type of colors.
 * 
 * @see SquidLayers A more complex combined panel.
 */
@Beta
public interface ICombinedPanel<T> {

	/**
	 * Puts the character {@code c} at {@code (x, y)}.
	 * 
	 * @param x
	 * @param y
	 * @param c
	 */
	public void putFG(int x, int y, char c);

	/**
	 * Puts the character {@code c} at {@code (x, y)} with some {@code color}.
	 * 
	 * @param x
	 * @param y
	 * @param c
	 * @param color
	 */
	public void putFG(int x, int y, char c, T color);

	/**
	 * Puts the given string horizontally with the first character at the given
	 * offset.
	 *
	 * Does not word wrap. Characters that are not renderable (due to being at
	 * negative offsets or offsets greater than the grid size) will not be shown
	 * but will not cause any malfunctions.
	 *
	 * @param x
	 *            the x coordinate of the first character
	 * @param y
	 *            the y coordinate of the first character
	 * @param string
	 *            the characters to be displayed
	 * @param color
	 *            the color to draw the characters
	 */
	public void putFG(int x, int y, String string, T color);

	/**
	 * Puts the given string horizontally with the first character at the given
	 * offset.
	 *
	 * Does not word wrap. Characters that are not renderable (due to being at
	 * negative offsets or offsets greater than the grid size) will not be shown
	 * but will not cause any malfunctions.
	 *
	 * @param x
	 *            the x coordinate of the first character
	 * @param y
	 *            the y coordinate of the first character
	 * @param cs
	 *            the text to be displayed, with its color.
	 */
	public void putFG(int x, int y, IColoredString<? extends T> cs);

	/**
	 * Puts the color {@code c} at {@code (x, y)}.
	 * 
	 * @param x
	 * @param y
	 * @param color
	 */
	public void putBG(int x, int y, T color);

	/**
	 * @param color
	 *            The color to put within this panel.
	 */
	public void fillBG(T color);

	public void refresh();

	/**
	 * @return The two backers, with the panel at the top (the foreground)
	 *         first. They are instances of {@code SquidPanel}.
	 */
	public List<ISquidPanel<?>> getBackers();

	/**
	 * A generic implementation of {@link ICombinedPanel}. Useful to combine
	 * things. If you're a new user, you likely need {@link GroupImpl} instead.
	 * 
	 * @author smelC
	 * 
	 * @param <T>
	 *            The type of colors.
	 */
	@Beta
	public static class Impl<T> implements ICombinedPanel<T> {

		protected final ISquidPanel<T> bg;
		protected final ISquidPanel<T> fg;

		protected final int width;
		protected final int height;

		/**
		 * @param bg
		 *            The backing background panel.
		 * @param fg
		 *            The backing foreground panel.
		 * @param width
		 *            The width of this panel, used for {@link #fillBG(Object)}
		 *            (so that it fills within {@code [0, width)}).
		 * @param height
		 *            The height of this panel, used for {@link #fillBG(Object)}
		 *            (so that it fills within {@code [0, height)}).
		 * @throws IllegalStateException
		 *             In various cases of errors regarding sizes of panels.
		 */
		public Impl(ISquidPanel<T> bg, ISquidPanel<T> fg, int width, int height) {
			if (bg.gridWidth() != fg.gridWidth())
				throw new IllegalStateException(
						"Cannot build a combined panel with backers of different widths");
			if (bg.gridHeight() != fg.gridHeight())
				throw new IllegalStateException(
						"Cannot build a combined panel with backers of different heights");

			this.bg = bg;
			this.fg = fg;
			if (width < 0)
				throw new IllegalStateException("Cannot create a panel with a negative width");
			this.width = width;
			if (height < 0)
				throw new IllegalStateException("Cannot create a panel with a negative height");
			this.height = height;
		}

		@Override
		public void putFG(int x, int y, char c) {
			fg.put(x, y, c);
		}

		@Override
		public void putFG(int x, int y, char c, T color) {
			fg.put(x, y, c, color);
		}

		@Override
		public void putFG(int x, int y, String string, T foreground) {
			fg.put(x, y, string, foreground);
		}

		@Override
		public void putFG(int x, int y, IColoredString<? extends T> cs) {
			fg.put(x, y, cs);
		}

		@Override
		public void putBG(int x, int y, T color) {
			bg.put(x, y, color);
		}

		@Override
		public void fillBG(T color) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++)
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

	}

	/**
	 * An implementation of {@link ICombinedPanel} that extends libgdx's group.
	 * If you're a new user, that's likely what you should use.
	 * 
	 * @author smelC
	 */
	public class GroupImpl<T> extends Group implements ICombinedPanel<T> {

		protected/* @Nullable */ISquidPanel<T> bg;
		protected/* @Nullable */ISquidPanel<T> fg;

		/** The width, in cell sizes */
		protected int gridWidth = -1;

		/** The height, in cell sizes */
		protected int gridHeight = -1;

		/**
		 * @param bg
		 *            The backing background panel.
		 * @param fg
		 *            The backing foreground panel.
		 * @param gridWidth
		 *            The width of this panel, used for {@link #fillBG(Object)}
		 *            (so that it fills within {@code [0, width)}).
		 * @param gridHeight
		 *            The height of this panel, used for {@link #fillBG(Object)}
		 *            (so that it fills within {@code [0, height)}).
		 * @throws IllegalStateException
		 *             In various cases of errors regarding sizes of panels.
		 */
		public GroupImpl(ISquidPanel<T> bg, ISquidPanel<T> fg, int gridWidth, int gridHeight) {
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
		public GroupImpl() {
		}

		/**
		 * Sets the backing panels.
		 * 
		 * @param bg
		 * @param fg
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
}
