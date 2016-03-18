package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import squidpony.SquidTags;
import squidpony.annotation.Beta;
import squidpony.panel.IColoredString;
import squidpony.panel.ICombinedPanel;
import squidpony.panel.ISquidPanel;
import squidpony.squidgrid.gui.gdx.UIUtil.CornerStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * A panel to display some text. It can either compute its size on its own or
 * use preallocated panels to determine its size.
 * 
 * <p>
 * This class is somehow doing a simpler business as {@link ButtonsPanel} but
 * they did not get merged, because {@link ButtonsPanel} would then become a
 * monster.
 * </p>
 * 
 * @author smelC
 * @param <T>
 * 
 * @see ButtonsPanel
 */
@Beta
public abstract class TextPanel<T extends Color> extends GroupCombinedPanel<T> {

	protected List<IColoredString<T>> text;

	/**
	 * The maximum width that this panel can take (in number of cells). This is
	 * a pretty much random value. Overwrite it with something computed from
	 * your game (for example, use your dungeon's width if you display it
	 * entirely, and want this panel fullscreen).
	 */
	public int maxWidth = 8;

	/**
	 * The maximum height that this panel can take (in number of cells). This is
	 * a pretty much random value. Overwrite it with something computed from
	 * your game (for example, use your dungeon's height if you display it
	 * entirely, and want this panel fullscreen).
	 */
	public int maxHeight = 8;

	/** The background color */
	public T backgroundColor = null;

	/** The border's color */
	public T borderColor = null;

	/** The style of the border */
	public CornerStyle borderStyle = CornerStyle.ROUNDED;

	public int borderSize = 0;
	public float zoomMultiplierX = 1, zoomMultiplierY = 1;

	/** Whether to use {@link IColoredString#justify(int)} on text */
	public boolean justifyText = true;

	protected int h = -1;

	/**
	 * A panel with preallocated backers.
	 * 
	 * @param bg
	 * @param fg
	 */
	public TextPanel(ISquidPanel<T> bg, ISquidPanel<T> fg) {
		super(bg, fg);
	}

	/**
	 * A panel with backers created lazily.
	 */
	public TextPanel() {
		super();
	}

	/**
	 * Sets the text and prepares for display. This method should be called
	 * after the constructor, and before {@link #put(boolean)} or
	 * {@link #putBorder()}.
	 * 
	 * @param text
	 */
	public void init(List<IColoredString<T>> text) {
		this.text = new ArrayList<>(text);

		prepare();
	}

	/**
	 * @param putBorders
	 *            Puts this panel on screen, but do not draw it yet (we'd need a
	 *            {@code Stage} for that).
	 */
	public void put(boolean putBorders) {
		if (h == -1)
			throw new IllegalStateException(
					getClass().getSimpleName() + "::prepare() should be called before put(boolean)");

		if (backgroundColor != null)
			fill(ICombinedPanel.What.BG, backgroundColor);

		if (putBorders)
			putBorder();

		for (int y = 0; y < h; y++) {
			/* Put text */
			putFG(0, y, text.get(y));
		}
	}

	public void putBorder() {
		if (bg == null)
			return;

		if (borderColor != null && 0 < borderSize) {
			final float x = getX();
			final float y = getY();
			final int w = bg.gridWidth();
			final int h = bg.gridHeight();
			UIUtil.drawMarginsAround(x, y, w * bg.cellWidth(), h * bg.cellHeight(), borderSize, borderColor,
					borderStyle, zoomMultiplierX, zoomMultiplierY);
		}
	}

	/**
	 * This method can be left unimplemented if you give the panels at
	 * construction time.
	 * 
	 * @param width
	 *            The width that the panel must have.
	 * @param height
	 *            The height that the panel must have.
	 * @return A freshly allocated {@link ISquidPanel}.
	 */
	protected abstract ISquidPanel<T> buildPanel(int width, int height);

	protected void prepare() {
		if (text == null)
			throw new NullPointerException(
					"Text must be set before calling " + getClass().getSimpleName() + "::prepare()");

		final int w;
		if (bg == null) {
			/*
			 * We need to allocate the panels, hence we need to compute their
			 * sizes
			 */
			final int w_ = computeRequiredWidth();
			if (maxWidth < w_) {
				/* Wrapping needed */
				final List<IColoredString<T>> wrapped = new ArrayList<>(text.size() * 2);
				for (IColoredString<T> t : text) {
					final List<IColoredString<T>> wrap = t.wrap(maxWidth);
					for (IColoredString<T> ics : wrap)
						wrapped.add(justifyText ? ics.justify(maxWidth) : ics);
				}
				this.text = wrapped;
				w = maxWidth;
			} else
				w = w_;
			final int tsz = text.size();
			h = Math.min(maxHeight, tsz);

			Gdx.app.log(SquidTags.LAYOUT,
					"Chosen size of " + getClass().getSimpleName() + ": " + w + "x" + h);

			setPanels(buildPanel(w, h), buildPanel(w, h));
		} else {
			w = bg.gridWidth();
			h = bg.gridHeight();
			if (justifyText) {
				final List<IColoredString<T>> adjusted = new ArrayList<>(text.size());
				for (IColoredString<T> t : text)
					adjusted.add(t.justify(w));
				text = adjusted;
			}
		}
	}

	private int computeRequiredWidth() {
		int result = 0;
		for (IColoredString<?> ics : text)
			result = Math.max(result, ics.length());
		return result;
	}

}
