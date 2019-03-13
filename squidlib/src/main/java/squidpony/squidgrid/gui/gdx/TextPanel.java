package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Align;
import squidpony.panel.IColoredString;
import squidpony.panel.IMarkup;
import squidpony.squidgrid.gui.gdx.UIUtil.CornerStyle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
 * tp.init(screenWidth, screenHeight, text); <- first 2 params: for fullscreen
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
 * This class shares what {@link ScrollPane} does (knobs, handling of the wheel).
 * </p>
 * <p>
 * Drawing the result of {@link #getScrollPane()} will set the shader of {@code batch} if using a distance field or MSDF
 * font and the shader is currently not configured for such a font; it does not reset the shader to the default so that
 * multiple Actors can all use the same shader and so specific extra glyphs or other items can be rendered after calling
 * draw(). If you need to draw both a distance field font and full-color art, you should set the shader on the Batch to
 * null when you want to draw full-color art, and end the Batch between drawing this object and the other art.
 * </p>
 * @author smelC
 * 
 * @see ScrollPane A libGDX widget for general scrolling through only the visible part of a large widget
 * @see LinesPanel An alternative for displaying lines of text in a variable-width font
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
	
	protected /* @Nullable */ IMarkup<T> markup;

	protected BitmapFont font;
    protected TextCellFactory tcf;
	/** The text to display */
	public ArrayList<IColoredString<T>> text;

    protected StringBuilder builder;

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
        builder = new StringBuilder(512);
        textActor = new TextActor();

        this.scrollPane = new ScrollPane(textActor);
    }

    /**
     * The text to display MUST be set later on with
     * {@link #init(float, float, Collection)}.
     *
     * @param markup
     *            An optional way to compute markup.
     * @param distanceFieldFont
     *            A distance field font as a TextCellFactory to use.
     *            Won't be used for drawing in cells, just the distance field code it has matters.
     */
    public TextPanel(/* @Nullable */IMarkup<T> markup, /* @Nullable */ TextCellFactory distanceFieldFont) {
        if (markup != null)
            setMarkup(markup);
        if (distanceFieldFont != null)
        {
            tcf = distanceFieldFont;
            tcf.initBySize();
            font = tcf.font();
            if (markup != null)
                font.getData().markupEnabled = true;
        }
        builder = new StringBuilder(512);
        textActor = new TextActor();
        scrollPane = new ScrollPane(textActor);
    }

    /**
	 * @param m
	 *            The markup to use.
	 */
	public void setMarkup(IMarkup<T> m) {
		if (font != null)
			font.getData().markupEnabled = true;
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
        tcf = new TextCellFactory().font(font).height(MathUtils.ceil(font.getLineHeight()))
                .width(MathUtils.round(font.getSpaceWidth()));
		if (markup != null)
			font.getData().markupEnabled = true;
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
		this.text = new ArrayList<IColoredString<T>>(text);

		scrollPane.setWidth(width);
		textActor.setWidth(width);

		if (tcf == null)
			throw new NullPointerException(
					"The font should be set before calling TextPanel.init()");

		typesetText();
		final boolean yscroll = maxHeight < textActor.getHeight();
		scrollPane.setHeight(/* Maybe not the entire height */ Math.min(textActor.getHeight(), maxHeight));
		scrollPane.setActor(textActor);
		yScrollingCallback(yscroll);
		scrollPane.layout();
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
	 * @param text an ArrayList of IColoredString that will be used directly by this TextPanel (changes
	 *             to the ArrayList will show up in the TextPanel)
	 */
	public void initShared(float width, float maxHeight, ArrayList<IColoredString<T>> text) {
		this.text = text;

		scrollPane.setWidth(width);
		textActor.setWidth(width);

		if (tcf == null)
			throw new NullPointerException(
					"The font should be set before calling TextPanel.init()");

		typesetText();
		final boolean yscroll = maxHeight < textActor.getHeight();
		scrollPane.setHeight(/* Maybe not the entire height */ Math.min(textActor.getHeight(), maxHeight));
		scrollPane.setActor(textActor);
		yScrollingCallback(yscroll);
		scrollPane.layout();
	}

	public void init(float width, float maxHeight, T color, String... text)
    {
        ArrayList<IColoredString.Impl<T>> coll = new ArrayList<>(text.length);
        for(String t : text)
        {
            coll.add(new IColoredString.Impl<T>(t, color));
        }
        init(width, maxHeight, coll);
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
		builder.delete(0, builder.length());
		final List<String> result = new ArrayList<>();
		for (IColoredString<T> line : text) {
			/* This code must be consistent with #draw in the custom Actor */
			final IColoredString<T> tmp = present(line);
			final String marked = applyMarkup(tmp);
			result.add(marked);
			builder.append(marked);
			builder.append('\n');
		}
		if(builder.length() > 0)
			builder.deleteCharAt(builder.length() - 1);
		return result;
	}
	
	public void typesetText() {
		if (text == null)
			return;
		builder.setLength(0);
		for (IColoredString<T> line : text) {
			/* This code must be consistent with #draw in the custom Actor */
			final IColoredString<T> tmp = present(line);
			final String marked = applyMarkup(tmp);
			builder.append(marked);
			builder.append('\n');
		}
		if(builder.length() > 0)
			builder.deleteCharAt(builder.length() - 1);
		float totalTextHeight = tcf.height();
		GlyphLayout layout = font.getCache().addText(builder, 0, 0, 0, builder.length(),
				textActor.getWidth(), Align.left, true);
		totalTextHeight += layout.height;
		if(totalTextHeight < 0)
			totalTextHeight = 0;
		textActor.setHeight(/* Entire height */ totalTextHeight);

	}

	/**
	 * @return The {@link ScrollPane} containing {@link #getTextActor()}.
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

    private class TextActor extends Actor
    {
        TextActor()
        {

        }
        @Override
        public void draw(Batch batch, float parentAlpha) {
            final float tx = 0;//getX();
            final float ty = 0;//getY();
            final float twidth = getWidth();
            final float theight = getHeight();
            
            if (backgroundColor != null) {
                batch.setColor(backgroundColor);
                batch.draw(tcf.getSolid(), tx, ty, twidth, theight);
                batch.setColor(Color.WHITE);
                /*
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
                */
            }

            if (font == null)
                throw new NullPointerException(
                        "The font should be set when drawing a " + getClass().getSimpleName());
            if (text == null)
                throw new NullPointerException(
                        "The text should be set when drawing a " + getClass().getSimpleName());
            if (tcf != null) {
                tcf.configureShader(batch);
            }
            float yscroll = scrollPane.getScrollY();

            final float offY = (tcf != null) ? tcf.height * 0.5f : 0;
            typesetText();
            font.draw(batch, builder, tx, theight + yscroll - offY,
                    0, builder.length(), twidth, Align.left, true);

        }
    }

}
