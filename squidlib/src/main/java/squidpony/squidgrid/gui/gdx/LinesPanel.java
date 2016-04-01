package squidpony.squidgrid.gui.gdx;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;

import squidpony.IColorCenter;
import squidpony.SquidTags;
import squidpony.panel.IColoredString;
import squidpony.panel.IMarkup;

/**
 * An actor capable of drawing {@link IColoredString}s. It is lines-oriented:
 * putting a line erases the previous line. It is designed for panels to write
 * text with a serif font (where {@link SquidPanel} is inappropriate, because of
 * the too-much space it lefts between characters).
 * 
 * <p>
 * This
 * <a href="https://twitter.com/hgamesdev/status/709147572548575233">tweet</a>
 * shows an example. The panel at the top is implemented using this class.
 * </p>
 * 
 * <p>
 * Contrary to {@link SquidMessageBox}, this panel doesn't support scrolling
 * (for now). So it's suited when it is fine forgetting old messages (as in
 * brogue's messages area).
 * </p>
 * 
 * @author smelC
 * 
 * @see SquidMessageBox An alternative, somehow doing similar business, but
 *      being backed up by {@link SquidPanel}.
 */
public class LinesPanel<T extends Color> extends Actor {

	protected final IMarkup<T> markup;

	public final BitmapFont font;

	/** The number of horizontal cells */
	public final int hcells;

	/** The number of vertical cells */
	public final int vcells;

	protected boolean gdxYStyle = false;

	protected final T defaultTextColor;
	protected final T bgColor;

	protected final Map<Integer, ToDraw<T>> yToLine;

	/** The offset applied when drawing */
	protected int yoffset = 0;

	/** The color center to filter colors, or {@code null}. */
	protected /* @Nullable */ IColorCenter<T> colorCenter;

	public boolean clearBeforeDrawing = true;

	/**
	 * Lazily allocated, and kept in-between calls to
	 * {@link #draw(Batch, float)} to avoid bloating the heap if drawing is done
	 * at every frame.
	 */
	private /* @Nullable */ ShapeRenderer renderer;

	/**
	 * <b>This call sets markup in {@code font}</b>
	 * 
	 * @param markup
	 *            The markup to use
	 * @param font
	 *            The font to use.
	 * @param pixelwidth
	 *            The width (in pixels) that {@code this} must have.
	 * @param pixelheight
	 *            The height (in pixels) that {@code this} must have.
	 * @param defaultTextColor
	 *            The color to use when buckets of {@link IColoredString}
	 *            contains {@code null} for the color.
	 * @param bgColor
	 *            The background color to use.
	 */
	public LinesPanel(IMarkup<T> markup, BitmapFont font, int pixelwidth, int pixelheight, T bgColor,
			T defaultTextColor) {
		this.markup = markup;
		font.getData().markupEnabled |= true;
		this.font = font;

		this.hcells = (int) Math.floor(pixelwidth / font.getSpaceWidth());
		this.vcells = (int) Math.floor(pixelheight / font.getLineHeight());
		this.defaultTextColor = defaultTextColor;
		this.bgColor = bgColor;
		this.yToLine = new HashMap<Integer, ToDraw<T>>();

		setWidth(pixelwidth);
		setHeight(pixelheight);
	}

	public void put(int x, int y, char c) {
		put(x, y, c, null);
	}

	public void put(int x, int y, char c, /* @Nullable */ T color) {
		final IColoredString<T> buf = IColoredString.Impl.<T> create();
		buf.append(c, color);
		put(x, y, buf);
	}

	public void put(int x, int y, String string, T color) {
		final IColoredString<T> buf = IColoredString.Impl.<T> create();
		buf.append(string, color);
		put(x, y, buf);
	}

	public void put(int x, int y, IColoredString<T> ics) {
		if (vcells <= y) {
			Gdx.app.log(SquidTags.LAYOUT, getClass().getSimpleName() + "'s height is " + vcells + " cell"
					+ (vcells == 1 ? "" : "s") + ", but it is receiving a request to draw at height " + y);
			return;
		}
		yToLine.put(gdxYStyle ? vcells - y : y, new ToDraw<T>(x, ics));
	}

	/**
	 * You should not change this value while text has been drawn already. It
	 * should not be changed after allocating {@code this}. By default it is
	 * {@code false}.
	 * 
	 * @param b
	 *            {@code true} for {@code y=0} to be the bottom (gdx-style).
	 *            {@code false} for {@code y=0} to be the top (squidlib-style).
	 */
	public void setGdxYStyle(boolean b) {
		this.gdxYStyle = b;
	}

	/**
	 * @return The y offset applied to every drawing.
	 */
	public int getYOffset() {
		return yoffset;
	}

	/**
	 * Use that if you wanna draw top-aligned, or bottom-aligned, or are doing
	 * funky stuff.
	 * 
	 * @param offset
	 *            The offset to apply to every drawing (can be negative).
	 */
	public void setYOffset(int offset) {
		this.yoffset = offset;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (clearBeforeDrawing) {
			batch.end();

			clearArea(bgColor);

			batch.begin();
		}

		final float charwidth = font.getSpaceWidth();
		final float charheight = font.getLineHeight();
		final float x = getX();
		final float y = getY() + (charheight / 2);

		/*
		 * Do not fetch memory (field read) at every loop roll, you're not
		 * supposed to change #yoffset in the middle of this call anyway.
		 */
		final int yoff = yoffset;

		for (Map.Entry<Integer, ToDraw<T>> yAndLine : yToLine.entrySet()) {
			final ToDraw<T> todraw = yAndLine.getValue();
			final int key = yAndLine.getKey();
			final float xdest = x + (todraw.x * charwidth);
			final float ydest = y + (key * charheight) + charheight + yoff;
			final IColoredString<T> shown = colorCenter == null ? todraw.ics : colorCenter.filter(todraw.ics);
			font.draw(batch, shown.presentWithMarkup(markup), xdest, ydest);
		}
	}

	public void dispose() {
		if (renderer != null)
			renderer.dispose();
	}

	/**
	 * @param cc
	 *            The color center to use. Used to change the font color using
	 *            {@link IColorCenter#filter(Object)}.
	 */
	public void setColorCenter(IColorCenter<T> cc) {
		this.colorCenter = cc;
	}

	/**
	 * Paints this panel with {@code color}.
	 * 
	 * @param color
	 */
	public void clearArea(T color) {
		UIUtil.drawRectangle(getX(), getY(), getWidth(), getHeight(), ShapeType.Filled, color);
		if (renderer == null) {
			renderer = new ShapeRenderer();
			renderer.setColor(color);
		} else
			renderer.setColor(color);
		renderer.begin(ShapeType.Filled);
		renderer.rect(getX(), getY(), getWidth(), getHeight());
		renderer.end();
	}

	@Override
	public String toString() {
		return super.toString() + " hcells:" + hcells + " vcells:" + vcells + " fontwidth:"
				+ font.getSpaceWidth() + " fontheight:" + font.getLineHeight();
	}

	/**
	 * @author smelC
	 * 
	 * @param <T>
	 *            The type of color
	 */
	protected static class ToDraw<T> {

		final int x;
		final IColoredString<T> ics;

		ToDraw(int x, IColoredString<T> ics) {
			this.x = x;
			this.ics = ics;
		}

		@Override
		public String toString() {
			return (x == 0 ? "" : (">" + x + " ")) + ics.toString();
		}
	}

}
