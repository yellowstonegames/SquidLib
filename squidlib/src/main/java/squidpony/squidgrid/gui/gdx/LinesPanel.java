package squidpony.squidgrid.gui.gdx;

import java.util.LinkedList;
import java.util.ListIterator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;

import squidpony.IColorCenter;
import squidpony.panel.IColoredString;
import squidpony.panel.IMarkup;

/**
 * An actor capable of drawing {@link IColoredString}s. It is lines-oriented:
 * putting a line may erase a line put before. It is designed to write text with
 * a serif font (as opposed to {@link SquidPanel}). It performs line wrapping by
 * default. It can write from top to bottom or from bottom to top (the default).
 * 
 * <p>
 * This
 * <a href="https://twitter.com/hgamesdev/status/736091292132724736">tweet</a>
 * shows an example. The panel at the top of the screenshot is implemented using
 * this class (with {@link #drawBottomUp} being {@code true}).
 * </p>
 * 
 * <p>
 * This class is usually used as follows:
 * 
 * <pre>
 * final int nbLines = LinesPanel.computeMaxLines(font, pixelHeight);
 * final LinesPanel<Color> lp = new LinesPanel(new GDXMarkup(), font, nbLines);
 * lp.setSize(pixelWidth, pixelHeight);
 * stage.addActor(lp);
 * </pre>
 * </p>
 * 
 * <p>
 * Contrary to {@link SquidMessageBox}, this panel doesn't support scrolling
 * (for now). So it's suited when it is fine forgetting old messages (as in
 * brogue's messages area).
 * </p>
 * 
 * @author smelC
 * @param <T>
 * 
 * @see SquidMessageBox An alternative, doing similar lines-drawing business,
 *      but being backed up by {@link SquidPanel}.
 */
public class LinesPanel<T extends Color> extends Actor {

	/** The markup used to typeset {@link #content}. */
	protected final IMarkup<T> markup;

	/** The font used to draw {@link #content}. */
	protected final BitmapFont font;

	/** What to display. Doesn't contain {@code null} entries. */
	protected final LinkedList<IColoredString<T>> content;

	/** The maximal size of {@link #content} */
	protected final int maxLines;

	/**
	 * The renderer used by {@link #clearArea(Batch)}. Do not access directly:
	 * use {@link #getRenderer()} instead.
	 */
	protected /* @Nullable */ ShapeRenderer renderer;

	/**
	 * The horizontal offset to use when writing. If you aren't doing anything
	 * weird, should be left to {@code 0}.
	 */
	public float xOffset = 0;

	/**
	 * The vertical offset to use when writing. If you aren't doing anything
	 * weird, should be left to {@code 0}.
	 */
	public float yOffset = 0;

	/**
	 * If {@code true}, draws:
	 * 
	 * <pre>
	 * ...
	 * content[1]
	 * content[0]
	 * </pre>
	 * 
	 * If {@code false}, draws:
	 * 
	 * <pre>
	 * content[0]
	 * content[1]
	 * ...
	 * </pre>
	 */
	public boolean drawBottomUp = false;

	/**
	 * The color to use to clear the screen before drawing. Set it to
	 * {@code null} if you clean on your own.
	 */
	public Color clearingColor = Color.BLACK;

	/* Now comes the usual libgdx options */

	/** Whether to wrap text */
	public boolean wrap = true;

	/** The alignment used when typesetting */
	public int align = Align.left;

	/**
	 * @param markup
	 *            The markup to use, or {@code null} if none. You likely want to
	 *            give {@link GDXMarkup}. If non-{@code null}, markup will be
	 *            enabled in {@code font}.
	 * @param font
	 *            The font to use.
	 * @param maxLines
	 *            The maximum number of lines that this panel should display.
	 *            Must be {@code >= 0}.
	 * @throws IllegalStateException
	 *             If {@code maxLines < 0}
	 */
	public LinesPanel(/* @Nullable */ IMarkup<T> markup, BitmapFont font, int maxLines) {
		this.markup = markup;
		this.font = font;
		if (markup != null)
			this.font.getData().markupEnabled |= true;
		this.content = new LinkedList<IColoredString<T>>();
		if (maxLines < 0)
			throw new IllegalStateException("The maximum number of lines in an instance of "
					+ getClass().getSimpleName() + " must be greater or equal than zero");
		this.maxLines = maxLines;
	}

	/**
	 * @param font
	 * @param height
	 * @return The last argument to give to
	 *         {@link #WrappingLinesPanel(IMarkup, BitmapFont, int)} when the
	 *         desired <b>pixel</b> height is {@code height}
	 */
	public static int computeMaxLines(BitmapFont font, float height) {
		return MathUtils.ceil(height / font.getData().lineHeight);
	}

	/**
	 * Adds {@code ics} first in {@code this}, possibly removing the last entry,
	 * if {@code this}' size would grow over {@link #maxLines}.
	 * 
	 * @param ics
	 */
	public void addFirst(IColoredString<T> ics) {
		if (ics == null)
			throw new NullPointerException("Adding a null entry is forbidden");
		if (atMax())
			content.removeLast();
		content.addFirst(ics);
	}

	/**
	 * Adds {@code ics} last in {@code this}, possibly removing the last entry,
	 * if {@code this}' size would grow over {@link #maxLines}.
	 * 
	 * @param ics
	 */
	public void addLast(IColoredString<T> ics) {
		if (ics == null)
			throw new NullPointerException("Adding a null entry is forbidden");
		if (atMax())
			content.removeLast();
		content.addLast(ics);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		clearArea(batch);

		final float width = getWidth();

		final BitmapFontData data = font.getData();
		final float lineHeight = data.lineHeight;

		final float height = getHeight();

		final float x = getX() + xOffset;
		float y = getY() + (drawBottomUp ? lineHeight : height) - data.descent + yOffset;

		final ListIterator<IColoredString<T>> it = content.listIterator();
		int ydx = 0;
		float consumed = 0;
		while (it.hasNext()) {
			final IColoredString<T> ics = it.next();
			final String str = toDraw(ics, ydx);
			/* Let's see if the drawing would go outside this Actor */
			final BitmapFontCache cache = font.getCache();
			cache.clear();
			final GlyphLayout glyph = cache.addText(str, 0, y, width, align, wrap);
			if (height < consumed + glyph.height)
				/* We would draw outside this Actor's bounds */
				break;
			final int increaseAlready;
			if (drawBottomUp) {
				/*
				 * If the text span multiple lines and we draw bottom-up, we
				 * must go up *before* drawing.
				 */
				final int nbLines = MathUtils.ceil(glyph.height / lineHeight);
				if (1 < nbLines) {
					increaseAlready = nbLines - 1;
					y += increaseAlready * lineHeight;
				} else
					increaseAlready = 0;
			} else
				increaseAlready = 0;
			/* Actually draw */
			font.draw(batch, str, x, y, width, align, wrap);
			y += (drawBottomUp ? /* Go up */ 1 : /* Go down */ -1) * glyph.height;
			y -= increaseAlready * lineHeight;
			consumed += glyph.height;
			ydx++;
		}
	}

	/**
	 * Paints this panel with {@link #clearingColor}
	 */
	protected void clearArea(Batch batch) {
		if (clearingColor != null) {
			batch.end();
			UIUtil.drawRectangle(getRenderer(), getX(), getY(), getWidth(), getHeight(), ShapeType.Filled,
					clearingColor);
			batch.begin();
		}
	}

	protected boolean atMax() {
		return content.size() == maxLines;

	}

	protected String toDraw(IColoredString<T> ics, int ydx) {
		return applyMarkup(transform(ics, ydx));
	}

	protected String applyMarkup(IColoredString<T> ics) {
		if (ics == null)
			return null;
		else
			return markup == null ? ics.toString() : ics.presentWithMarkup(markup);
	}

	/**
	 * If you want to grey out "older" messages, you would do it in this method,
	 * when {@code ydx > 0} (using an {@link IColorCenter} maybe ?).
	 * 
	 * @param ics
	 * @param ydx
	 *            The index of {@code ics} within {@link #content}.
	 * @return A variation of {@code ics}, or {@code ics} itself.
	 */
	protected IColoredString<T> transform(IColoredString<T> ics, int ydx) {
		return ics;
	}

	protected ShapeRenderer getRenderer() {
		if (renderer == null)
			renderer = new ShapeRenderer();
		return renderer;
	}

}
