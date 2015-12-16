package squidpony.squidgrid.gui.gdx;

import squidpony.squidmath.Coord;

/**
 * A container that keeps tracks of the sizes of margins and cells. It is at the
 * screen-level, i.e. it is intended to keep track of the full screen, not of a
 * nested panel. Given the screen's size and the desired cell size, it computes
 * the margins around the usable part of the screen, and hence the position of
 * the full-screen SquidPanel ({@code (leftMargin,botMargin)}).
 * 
 * @author smelC
 */
public class ScreenSizeManager {

	/** The screen's width, as given at creation time. */
	public final int screenWidth;
	/** The screen's height, as given at creation time. */
	public final int screenHeight;

	/** The top margin */
	public final int topMargin;
	/**
	 * The bottom margin, which has at most a 1 pixel difference with
	 * {@link #topMargin}.
	 */
	public final int botMargin;
	/** The left margin */
	public final int leftMargin;
	/**
	 * The right margin, which has at most a 1 pixel difference with
	 * {@link #leftMargin}.
	 */
	public final int rightMargin;

	/** A cell's width, as given at creation time. */
	public final int cellWidth;

	/** A cell's height, as given at creation time. */
	public final int cellHeight;

	/** The number of cells, horizontally */
	public final int wCells;

	/** The number of cells, vertically */
	public final int hCells;

	/**
	 * A fresh size manager, for the given screen size and the given cell size.
	 * 
	 * @param screenWidth
	 * @param screenHeight
	 * @param cellWidth
	 *            The desired cell width.
	 * @param cellHeight
	 *            The desired cell height.
	 * @throws IllegalStateException
	 *             If a size is negative.
	 */
	public ScreenSizeManager(int screenWidth, int screenHeight, int cellWidth, int cellHeight) {
		this.screenWidth = screenWidth;
		if (this.screenWidth < 0)
			throw new IllegalStateException("Screen width should not be negative");
		this.screenHeight = screenHeight;
		if (this.screenHeight < 0)
			throw new IllegalStateException("Screen height should not be negative");

		if (cellWidth < 0)
			throw new IllegalStateException("Cell width should not be negative");
		this.cellWidth = cellWidth;

		if (cellHeight < 0)
			throw new IllegalStateException("Cell height should not be negative");
		this.cellHeight = cellHeight;

		{
			this.wCells = screenWidth / cellWidth;
			final int xmargin = screenWidth - (wCells * cellWidth);
			assert 0 <= xmargin;
			final int xmargindiv2 = xmargin / 2;
			if (isEven(xmargin)) {
				this.leftMargin = xmargindiv2;
				this.rightMargin = xmargindiv2;
			} else {
				this.leftMargin = xmargindiv2;
				this.rightMargin = xmargindiv2 + 1;
			}

			assert leftMargin + rightMargin == xmargin;
		}

		{
			this.hCells = screenHeight / cellHeight;
			final int ymargin = screenHeight - (hCells * cellHeight);
			assert 0 <= ymargin;
			final int ymargindiv2 = ymargin / 2;
			if (isEven(ymargin)) {
				this.topMargin = ymargindiv2;
				this.botMargin = ymargindiv2;
			} else {
				this.topMargin = ymargindiv2;
				this.botMargin = ymargindiv2 + 1;
			}

			assert topMargin + botMargin == ymargin;
		}

		assert leftMargin + (wCells * cellWidth) + rightMargin == screenWidth;
		assert botMargin + (hCells * cellHeight) + topMargin == screenHeight;
	}

	public void log() {
		/*
		Gdx.app.log(SquidTags.LAYOUT, String.format("Screen size: %dx%d", screenWidth, screenHeight));
		Gdx.app.log(SquidTags.LAYOUT,
				String.format("Displaying %d cells horizontally and %d cells vertically", wCells, hCells));
		Gdx.app.log(SquidTags.LAYOUT,
				String.format("Margins (in pixels): left:%d, right:%d, top:%d, bottom:%d", leftMargin,
						rightMargin, topMargin, botMargin));
		*/
	}

	public Coord toScreenSize() {
		return Coord.get(screenWidth, screenHeight);
	}

	public ScreenSizeManager changeScreenSize(int x, int y) {
		return new ScreenSizeManager(x, y, cellWidth, cellHeight);
	}

	private static boolean isEven(int i) {
		return i % 2 == 0;
	}
}
