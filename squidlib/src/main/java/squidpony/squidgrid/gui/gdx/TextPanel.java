package squidpony.squidgrid.gui.gdx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Align;

import squidpony.panel.IColoredString;
import squidpony.panel.IMarkup;
import squidpony.squidgrid.gui.gdx.UIUtil.CornerStyle;
import squidpony.squidgrid.gui.gdx.UIUtil.YMoveKind;

/**
 * A panel to display some text using libgdx directly (i.e. without using
 * {@link SquidPanel}) as in these examples (no scrolling first, then with a
 * scroll bar):
 * 
 * <p>
 * <ul>
 * <li><img src="http://i.imgur.com/EqEXqlu.png"/></li>
 * <li><img src="http://i.imgur.com/LYbxQZE.png"/></li>
 * </ul>
 * </p>
 * 
 * <p>
 * It supports vertical scrolling, i.e. it'll put a vertical scrollbar if
 * there's too much text to display. This class does a lot of stuff, you
 * typically only have to provide the textures for the scrollbars and the scroll
 * knobs (see example below).
 * </p>
 * 
 * <p>
 * A typical usage of this class is as follows:
 * 
 * <pre>
 * final TextPanel<Color> tp = new TextPanel<>(new GDXMarkup(), font);
 * tp.init(screenHeight, screenWidth, text); <- first 2 params: for fullscreen
 * final ScrollPane sp = tp.getScrollPane();
 * sp.setScrollPaneStyle(new ScrollPaneStyle(...)); <- set textures
 * stage.addActor(sp);
 * stage.setKeyboardFocus(sp);
 * stage.setScrollFocus(sp);
 * stage.draw();
 * </pre>
 * </p>
 * 
 * <p>
 * In addition to what {@link ScrollPane} does (knobs, handling of the wheel);
 * this class plugs srolling with arrow keys (up, down, page up, page down) and
 * vim shortcuts (j/k).
 * </p>
 * 
 * @author smelC
 * 
 * @see ScrollPane
 */
public class TextPanel<T extends Color> {

	/**
	 * The color to use to paint the background (outside buttons) using
	 * {@link ShapeRenderer}. Or {@code null} to disable background coloring.
	 */
	public /* @Nullable */ T backgroundColor;

	/**
	 * The color of the border around this panel, if any. If set, it'll be
	 * rendered using {@link ShapeRenderer} and {@link #borderStyle}.
	 */
	public /* @Nullable */ T borderColor;

	/** The size of the border, if any */
	public float borderSize;

	public CornerStyle borderStyle = CornerStyle.ROUNDED;

	/**
	 * Whether to use 'j' to scroll down, and 'k' to scroll up. Serious
	 * roguelikes leave that to {@code true}.
	 */
	public boolean vimShortcuts = true;

	protected /* @Nullable */ IMarkup<T> markup;

	protected BitmapFont font;

	/** The text to display */
	protected List<IColoredString<T>> text;

	protected final ScrollPane scrollPane;

	/**
	 * The actor whose size is adjusted to the text. When scrolling is required,
	 * it is bigger than {@link #scrollPane}.
	 */
	protected final Actor textActor;

	/** Do not access directly, use {@link #getRenderer()} */
	private /* @Nullable */ ShapeRenderer renderer;

	/**
	 * The text to display MUST be set later on with
	 * {@link #init(float, float, Collection)}.
	 * 
	 * @param markup
	 *            An optional way to compute markup.
	 * @param font
	 *            The font to use. It can be set later using
	 *            {@link #setFont(BitmapFont)}, but it MUST be set before
	 *            drawing this panel.
	 */
	public TextPanel(/* @Nullable */IMarkup<T> markup, /* @Nullable */ BitmapFont font) {
		if (markup != null)
			setMarkup(markup);
		if (font != null)
			setFont(font);
		this.textActor = new Actor() {
			@Override
			public void draw(Batch batch, float parentAlpha) {
				final float tx = textActor.getX();
				final float ty = textActor.getY();
				final float twidth = textActor.getWidth();
				final float theight = textActor.getHeight();

				final float height = scrollPane.getHeight();

				if (backgroundColor != null) {
					batch.end();

					final Matrix4 m = batch.getTransformMatrix();
					final ShapeRenderer sr = getRenderer();
					sr.setTransformMatrix(m);
					sr.begin(ShapeType.Filled);
					sr.setColor(backgroundColor);
					UIUtil.drawRectangle(renderer, tx, ty, twidth, theight, ShapeType.Filled,
							backgroundColor);
					sr.end();

					batch.begin();
				}

				if (TextPanel.this.font == null)
					throw new NullPointerException(
							"The font should be set when drawing a " + getClass().getSimpleName());
				if (TextPanel.this.text == null)
					throw new NullPointerException(
							"The text should be set when drawing a " + getClass().getSimpleName());

				float yscroll = scrollPane.getScrollY();

				float hauteur = height;
				final float destx = tx;
				for (String toDisplay : getTypesetText()) {
					final GlyphLayout glyph = TextPanel.this.font.draw(batch, toDisplay, destx,
							hauteur + yscroll, 0, toDisplay.length(), twidth, Align.left, /* wrap */ true);
					hauteur -= glyph.height;
				}
			}
		};
		this.scrollPane = new ScrollPane(textActor);

		this.scrollPane.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				/* To receive key up */
				return true;
			}

			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				final YMoveKind d = UIUtil.YMoveKind.of(keycode, vimShortcuts);
				if (d == null)
					return false;
				else {
					switch (d) {
					case DOWN:
					case UP: {
						handleArrow(!d.isDown());
						return true;
					}
					case PAGE_DOWN:
					case PAGE_UP:
						final float scrollY = scrollPane.getScrollY();
						final int mult = d.isDown() ? 1 : -1;
						scrollPane.setScrollY(scrollY + mult * textActor.getHeight());
						return true;
					}
					throw new IllegalStateException(
							"Unmatched " + YMoveKind.class.getSimpleName() + ": " + d);
				}
			}

			@Override
			public boolean keyTyped(InputEvent event, char character) {
				if (vimShortcuts && (character == 'j' || character == 'k'))
					return true;
				else
					return super.keyTyped(event, character);
			}

			private void handleArrow(boolean up) {
				final float scrollY = scrollPane.getScrollY();
				final int mult = up ? -1 : 1;
				scrollPane.setScrollY(scrollY + (scrollPane.getHeight() * 0.8f * mult));
			}
		});
	}

	/**
	 * @param m
	 *            The markup to use.
	 */
	public void setMarkup(IMarkup<T> m) {
		if (font != null)
			font.getData().markupEnabled |= true;
		this.markup = m;
	}

	/**
	 * Sets the font to use. This method should be called once before
	 * {@link #init(float, float, Collection)} if the font wasn't given at
	 * creation-time.
	 * 
	 * @param font
	 *            The font to use.
	 */
	public void setFont(BitmapFont font) {
		this.font = font;
		if (markup != null)
			font.getData().markupEnabled |= true;
	}

	/**
	 * This method sets the sizes of {@link #scrollPane} and {@link #textActor}.
	 * This method MUST be called before rendering.
	 * 
	 * @param maxHeight
	 *            The maximum height that the scrollpane can take (equal or
	 *            smaller than the height of the text actor).
	 * @param width
	 *            The width of the scrollpane and the text actor.
	 * @param text
	 */
	public void init(float width, float maxHeight, Collection<? extends IColoredString<T>> text) {
		this.text = new ArrayList<>(text);

		scrollPane.setWidth(width);
		textActor.setWidth(width);

		if (font == null)
			throw new NullPointerException(
					"The font should be set before calling " + TextPanel.class.getSimpleName() + "::init");

		final BitmapFontCache cache = font.getCache();
		final List<String> toDisplay = getTypesetText();
		float totalTextHeight = -font.getDescent();
		assert 0 <= totalTextHeight;
		for (String s : toDisplay) {
			cache.clear();
			/*
			 * This doesn't draw to screen, but does the typesetting computation
			 */
			final GlyphLayout layout = cache.addText(s, 0, 0, 0, s.length(), width, Align.left, true);
			totalTextHeight += layout.height;
		}
		cache.clear();
		textActor.setHeight(/* Entire height */ totalTextHeight);
		final boolean yscroll = maxHeight < totalTextHeight;
		scrollPane.setHeight(/* Maybe not the entire height */ Math.min(totalTextHeight, maxHeight));

		yScrollingCallback(yscroll);
	}

	/**
	 * Draws the border. You have to call this method manually, because the
	 * border is outside the actor and hence should be drawn at the very end,
	 * otherwise it can get overwritten by UI element.
	 * 
	 * @param batch
	 */
	public void drawBorder(Batch batch) {
		if (borderColor != null && 0 < borderSize) {
			final boolean reset = batch.isDrawing();
			if (reset)
				batch.end();

			final ShapeRenderer sr = getRenderer();
			final Matrix4 m = batch.getTransformMatrix();
			sr.setTransformMatrix(m);
			sr.begin(ShapeType.Filled);
			sr.setColor(borderColor);
			UIUtil.drawMarginsAround(sr, scrollPane.getX(), scrollPane.getY(), scrollPane.getWidth(),
					scrollPane.getHeight() - 1, borderSize, borderColor, borderStyle, 1f, 1f);
			sr.end();

			if (reset)
				batch.begin();
		}
	}

	/**
	 * @return The text to draw, after applying {@link #present(IColoredString)}
	 *         and {@link #applyMarkup(IColoredString)}.
	 */
	public /* @Nullable */ List<String> getTypesetText() {
		if (text == null)
			return null;
		final List<String> result = new ArrayList<>();
		for (IColoredString<T> line : text) {
			/* This code must be consistent with #draw in the custom Actor */
			final IColoredString<T> tmp = present(line);
			result.add(applyMarkup(tmp));
		}

		return result;
	}

	/**
	 * @return The {@link Scrollpane} containing {@link #getTextActor()}.
	 */
	public ScrollPane getScrollPane() {
		return scrollPane;
	}

	/**
	 * @return The {@link Actor} where the text is drawn. It may be bigger than
	 *         {@link #getScrollPane()}.
	 */
	public Actor getTextActor() {
		return textActor;
	}

	/**
	 * @return The font used, if set.
	 */
	public /* @Nullable */ BitmapFont getFont() {
		return font;
	}

	public void dispose() {
		if (renderer != null)
			renderer.dispose();
	}

	/**
	 * Callback done to do stuff according to whether y-scrolling is required
	 * 
	 * @param required
	 *            Whether y scrolling is required.
	 */
	protected void yScrollingCallback(boolean required) {
		if (required) {
			/* Disable borders, they don't mix well with scrollbars */
			borderSize = 0;
			scrollPane.setFadeScrollBars(false);
			scrollPane.setForceScroll(false, true);
		}
	}

	/**
	 * @param ics
	 *            Text set when building {@code this}
	 * @return The text to display to screen. If you wanna
	 *         {@link squidpony.IColorCenter#filter(IColoredString) filter} your
	 *         text , do it here.
	 */
	protected IColoredString<T> present(IColoredString<T> ics) {
		return ics;
	}

	/**
	 * @param ics
	 * @return The text obtained after applying {@link #markup}.
	 */
	protected String applyMarkup(IColoredString<T> ics) {
		return markup == null ? ics.toString() : ics.presentWithMarkup(markup);
	}

	/**
	 * @return A fresh renderer.
	 */
	protected ShapeRenderer buildRenderer() {
		return new ShapeRenderer();
	}

	/**
	 * @return The renderer to use.
	 */
	protected ShapeRenderer getRenderer() {
		if (renderer == null)
			renderer = buildRenderer();
		return renderer;
	}

}
