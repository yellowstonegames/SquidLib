package squidpony.panel;

import squidpony.annotation.Beta;

import java.util.LinkedList;
import java.util.List;

/**
 * The combination of two panels, one to color the background, the other to
 * write characters on the foreground.
 * 
 * <p>
 * <ul>
 * <li>
 * There is a very generic implementation in this file: {@link Impl} that you
 * should use if you're combining generic things.
 * </li>
 * <li>
 * There is a libgdx-{@code Group} based implementation that offers more
 * features and that you should likely use if you're a new user (in
 * squidlib-gdx).
 * </li>
 * </ul>
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
	void putFG(int x, int y, char c);

	/**
	 * Puts the character {@code c} at {@code (x, y)} with some {@code color}.
	 * 
	 * @param x
	 * @param y
	 * @param c
	 * @param color
	 */
	void putFG(int x, int y, char c, T color);

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
	void putFG(int x, int y, String string, T color);

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
	void putFG(int x, int y, IColoredString<? extends T> cs);

	/**
	 * Puts the color {@code c} at {@code (x, y)}.
	 * 
	 * @param x
	 * @param y
	 * @param color
	 */
	void putBG(int x, int y, T color);

	/**
	 * Puts {@code c} at (x, y), using {@code fgc} for {@code c} and {@code bgc}
	 * for the background.
	 */
	void put(int x, int y, char c, T bgc, T fgc);

    /**
     * Put {@code cs} at (x,y) using {@code bgc} for the background.
     */
	void put(int x, int y, T bgc, IColoredString<? extends T> cs);

	/**
	 * Put {@code cs} at (x,y) using {@code bgc} for the background and
	 * {@code fgc} for the foreground.
	 */
	void put(int x, int y, String s, T bgc, T fgc);
	
	/**
	 * @param color
	 *            The color to put within this panel.
	 */
	void fillBG(T color);

	void refresh();

	/**
	 * @return The two backers, with the panel at the top (the foreground)
	 *         first. They are instances of {@code SquidPanel}.
	 */
	List<ISquidPanel<?>> getBackers();

	/**
	 * A generic implementation of {@link ICombinedPanel}. Useful to combine
	 * things. If you're a new user, you likely would prefer the more specific
	 * implementation using libGDX, GroupCombinedPanel, instead.
	 * 
	 * @author smelC
	 * 
	 * @param <T>
	 *            The type of colors.
	 */
	@Beta
	class Impl<T> implements ICombinedPanel<T> {

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
		public void put(int x, int y, char c, T bgc, T fgc) {
			bg.put(x, y, bgc);
			fg.put(x, y, c, fgc);
		}

		@Override
		public void put(int x, int y, T bgc, IColoredString<? extends T> cs) {
			final int l = cs.length();
			for (int i = x; i < l && i < width; i++)
				bg.put(i, y, bgc);
			fg.put(x, y, cs);
		}

		@Override
		public void put(int x, int y, String s, T bgc, T fgc) {
			final int l = s.length();
			for (int i = x; i < l && i < width; i++)
				bg.put(i, y, bgc);
			fg.put(x, y, s, fgc);
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

}
