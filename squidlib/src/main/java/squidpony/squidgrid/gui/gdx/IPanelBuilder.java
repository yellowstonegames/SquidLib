package squidpony.squidgrid.gui.gdx;

/**
 * Interface to build instances of {@link SquidPanel} by taking care of
 * adjusting the panel to the screen size and the available fonts.
 * 
 * @author smelC
 */
public interface IPanelBuilder {

	/**
	 * This method builds a panel whose number of cells is such that the cell
	 * size is close to {@code desiredCellSize}. This method only supports
	 * square cells.
	 * 
	 * @param screenWidth
	 *            The screen's width, in number of pixels, as given by libgdx.
	 * @param screenHeight
	 *            The screen's height, in number of pixels, as given by libgdx.
	 * @param desiredCellSize
	 *            The cell size that you would like the panel to have. Width and
	 *            height cannot be different, because this method supports only
	 *            square cells.
	 * @param tcf
	 *            The {@link TextCellFactory} to use. Can be {@code null} to
	 *            create the panel.
	 * @return A screen wide squid panel, margins-aware, and with its position
	 *         set.
	 */
	public SquidPanel buildScreenWide(int screenWidth, int screenHeight, int desiredCellSize,
			/* @Nullable */ TextCellFactory tcf);

	/**
	 * Builds a panel by the number of requested cells.
	 * 
	 * @param hCells
	 *            The number of horizontal cells of the panel to build.
	 * @param vCells
	 *            The number of vertical cells of the panel to build.
	 * @param cellWidth
	 *            The width of a cell (in pixels).
	 * @param cellHeight
	 *            The height of a cell (in pixels).
	 * @param tcf_
	 *            The text cell factory to use, if any.
	 * @return A freshly built panel.
	 */
	public SquidPanel buildByCells(int hCells, int vCells, int cellWidth, int cellHeight,
			/* @Nullable */ TextCellFactory tcf_);

	/**
	 * @param sz
	 * @return A cell size close to {@code sz} that can be displayed (i.e. for
	 *         which there's a font, according to
	 *         {@link #fontSizeForCellSize(int)} and {@link #hasFontOfSize(int)}
	 *         ).
	 */
	public int adjustCellSize(int sz);

	/**
	 * Method to check whether increasing the cell size is possible.
	 * 
	 * @param cellSize
	 * @return {@code true} if {@code cellSize} is too big.
	 */
	public boolean cellSizeTooBig(int cellSize);

	/**
	 * Method to check whether decreasing the cell size is possible.
	 * 
	 * @param cellSize
	 * @return {@code true} if {@code cellSize} is too small.
	 */
	public boolean cellSizeTooSmall(int cellSize);

	/**
	 * @param cellSize
	 * @return Whether there's a font available for a cell of size
	 *         {@code cellSize}.
	 */
	public boolean hasFontForCellOfSize(int cellSize);

	/**
	 * @param cellSize
	 * @return The font size to use for a square cell size of {@code cellSize}.
	 *         Generally, it is {@code cellSize}; but it can be less in case
	 *         glyphs are too large.
	 */
	public int fontSizeForCellSize(int cellSize);

	/**
	 * @param sz
	 * @return Whether a square font of size {@code sz} is available.
	 */
	public boolean hasFontOfSize(int sz);

	/**
	 * A partial implementation of {@link IPanelBuilder}.
	 * 
	 * @author smelC
	 */
	public static abstract class Skeleton implements IPanelBuilder {

		@Override
		public boolean hasFontForCellOfSize(int cellSize) {
			return hasFontOfSize(fontSizeForCellSize(cellSize));
		}

	}
}
