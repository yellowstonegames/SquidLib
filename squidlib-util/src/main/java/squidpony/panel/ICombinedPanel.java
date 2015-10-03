package squidpony.panel;

import java.util.LinkedList;
import java.util.List;

import squidpony.annotation.Beta;

/**
 * The combination of two panels, one to color the background, the other to
 * write characters on the foreground.
 * 
 * @author smelC
 * 
 * @param <T>
 *            The type of colors.
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
	 * @param margin
	 *            The color to put at this panel's borders.
	 * @param inside
	 *            The color to put within this panel.
	 */
	public void fillBG(T margin, T inside);

	public void refresh();

	/**
	 * @return The two backers, with the panel at the top (the foreground)
	 *         first. They are instances of {@code SquidPanel}.
	 */
	public List<ISquidPanel<?>> getBackers();

	/**
	 * A basic implementation of {@link ICombinedPanel}.
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
		 *            The width of this panel, used for
		 *            {@link #fillBG(Object, Object)} (so that it fills within
		 *            {@code [0, width)}).
		 * @param height
		 *            The height of this panel, used for
		 *            {@link #fillBG(Object, Object)} (so that it fills within
		 *            {@code [0, height)}).
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
		public void fillBG(T margin, T inside) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (x == 0 || y == 0 || x == width - 1 || y == height - 1)
						putBG(x, y, margin);
					else
						putBG(x, y, inside);
				}
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
}
