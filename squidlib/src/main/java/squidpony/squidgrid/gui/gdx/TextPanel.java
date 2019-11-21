package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Align;
import squidpony.panel.IColoredString;
import squidpony.squidgrid.gui.gdx.UIUtil.CornerStyle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
 */
public class TextPanel {

	/**
	 * The color to use to paint the background (outside buttons) using
	 * {@link ShapeRenderer}. Or {@code null} to disable background coloring.
	 */
	public /* @Nullable */ Color backgroundColor;

	/**
	 * The color of the border around this panel, if any. If set, it'll be
	 * rendered using {@link ShapeRenderer} and {@link #borderStyle}.
	 */
	public /* @Nullable */ Color borderColor;

	/** The size of the border, if any */
	public float borderSize;

	public CornerStyle borderStyle = CornerStyle.ROUNDED;
	
	protected BitmapFont font;
	protected TextCellFactory tcf;
	/** The text to display */
	public ArrayList<CharSequence> text;

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
	 * @param font
	 *            The font to use. It can be set later using
	 *            {@link #setFont(BitmapFont)}, but it MUST be set before
	 *            drawing this panel.
	 */
	public TextPanel(/* @Nullable */ BitmapFont font) {
		if (font != null)
			setFont(font);
		textActor = new TextActor();

		this.scrollPane = new ScrollPane(textActor);
	}

	/**
	 * The text to display MUST be set later on with {@link #init(float, float, Collection)} (which can't be updated) or
	 * {@link #initShared(float, float, ArrayList)} (which reflects changes in the given ArrayList).
	 *
	 * @param font
	 *            A TextCellFactory, typically holding a distance field font ("stretchable" or "crisp" in
	 *            DefaultResources). This won't force glyphs into same-size cells, despite the name.
	 */
	public TextPanel(/* @Nullable */ TextCellFactory font) {
		if (font != null)
		{
			tcf = font;
			tcf.initBySize();
			this.font = tcf.font();
			this.font.getData().markupEnabled = true;
		}
		textActor = new TextActor();
		scrollPane = new ScrollPane(textActor);
	}
	
	/**
	 * Sets the font to use. This method should be called once before {@link #init(float, float, Collection)} if the
	 * font wasn't given at creation-time.
	 *
	 * @param font The font to use as a BitmapFont.
	 */
	public void setFont(BitmapFont font) {
		font.getData().markupEnabled = true;
		this.font = font;
		tcf = new TextCellFactory().font(font).height(MathUtils.ceil(font.getLineHeight()))
				.width(MathUtils.round(font.getSpaceXadvance()));
	}

	/**
	 * Sets the font to use. This method should be called once before {@link #init(float, float, Collection)} if the
	 * font wasn't given at creation-time.
	 *
	 * @param font The font to use as a TextCellFactory.
	 */
	public void setFont(TextCellFactory font) {
		if (font != null)
		{
			tcf = font;
			tcf.initBySize();
			this.font = tcf.font();
			this.font.getData().markupEnabled = true;
		}
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
	 * @param coloredText any Collection of IColoredString that use Color or a subclass as their color type
	 */
	public void init(float width, float maxHeight, Collection<? extends IColoredString<Color>> coloredText) {
		if (tcf == null)
			throw new NullPointerException(
					"The font should be set before calling TextPanel.init()");
		
		this.text = new ArrayList<>(coloredText.size());
		for (IColoredString<Color> ics : coloredText)
			text.add(ics.presentWithMarkup(GDXMarkup.instance));
		scrollPane.setWidth(width);
		textActor.setWidth(width);
		
		scrollPane.setHeight(maxHeight);
		scrollPane.setActor(textActor);
		scrollPane.layout();
	}

	/**
	 * This method sets the sizes of {@link #scrollPane} and {@link #textActor}, and shares a direct reference to
	 * {@code text} so changes to that ArrayList will also be picked up here and rendered.
	 * This method MUST be called before rendering.
	 *
	 * @param maxHeight
	 *            The maximum height that the scrollpane can take (equal or
	 *            smaller than the height of the text actor).
	 * @param width
	 *            The width of the scrollpane and the text actor.
	 * @param text an ArrayList of CharSequence that will be used directly by this TextPanel (changes
	 *             to the ArrayList will show up in the TextPanel)
	 */
	public void initShared(float width, float maxHeight, ArrayList<CharSequence> text) {
		this.text = text;

		scrollPane.setWidth(width);
		textActor.setWidth(width);

		if (tcf == null)
			throw new NullPointerException(
					"The font should be set before calling TextPanel.init()");

		//prepareText();
//		final boolean yscroll = maxHeight < textActor.getHeight();
		scrollPane.setHeight(maxHeight);
		scrollPane.setActor(textActor);
		//yScrollingCallback(yscroll);
		scrollPane.layout();
	}

	public void init(float width, float maxHeight, Color color, String... text)
	{
		if (tcf == null)
			throw new NullPointerException(
					"The font should be set before calling TextPanel.init()");

		this.text = new ArrayList<>(text.length);
		Collections.addAll(this.text, text);
		scrollPane.setWidth(width);
		textActor.setWidth(width);

		scrollPane.setHeight(maxHeight);
		scrollPane.setActor(textActor);
		scrollPane.layout();
	}

	/**
	 * Draws the border. You have to call this method manually, because the
	 * border is outside the actor and hence should be drawn at the very end,
	 * otherwise it can get overwritten by UI elements.
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
//			prepareText();
			final float down = font.getData().down;
			UIUtil.drawMarginsAround(sr, scrollPane.getX(), scrollPane.getY() + down * -0.5f + 4f, scrollPane.getWidth(),
					scrollPane.getHeight() + down * -1.5f - 4f, borderSize * 2f, borderColor, borderStyle, 1f, 1f);
			sr.end();

			if (reset)
				batch.begin();
		}
	}

	/**
	 * @return The text to draw, without color information present in {@link #text}.
	 */
	public /* @Nullable */ ArrayList<String> getTypesetText() {
		if (text == null)
			return null;
		final ArrayList<String> result = new ArrayList<>();
		for (CharSequence line : text) {
			result.add(GDXMarkup.instance.removeMarkup(line).toString());
		}
		return result;
	}

	/**
	 * Updates the text this will show based on the current contents of the ArrayList of IColoredString values that may
	 * be shared due to {@link #initShared(float, float, ArrayList)}, then resizes the {@link #getTextActor()} to fit
	 * the current text and lays out the {@link #getScrollPane()} to match. Called in the text actor's draw() method. 
	 */
	protected void prepareText() {
		if (text == null)
			return;
		final BitmapFontCache cache = font.getCache();
		//cache.clear();
		final float w = scrollPane.getWidth();
		float lineHeight = -font.getData().down, capHeight = font.getCapHeight();
		int lines = 1;//, ci = 0;
		StringBuilder sb = new StringBuilder(256);
		if(tcf.supportedStyles() <= 1) {
			for (int m = 0, textSize = text.size(); m < textSize; m++) {
				sb.append(GDXMarkup.instance.colorStringOnlyMarkup(text.get(m))).append('\n');
			}
		}
		else
		{
			for (int m = 0, textSize = text.size(); m < textSize; m++) {
				sb.append(GDXMarkup.instance.colorStringMarkup(text.get(m))).append('\n');
			}
		}

		GlyphLayout layout = cache.setText(sb, 0, 0, w, Align.left, true);			
		lines += layout.height / capHeight;
		
		////TODO: BitmapFontCache.setColors(float, int, int) is broken in libGDX 1.9.10; find some workaround
//		for (int m = 0, textSize = text.size(); m < textSize; m++) {
//			IColoredString<Color> line = text.get(m);
//			ArrayList<IColoredString.Bucket<Color>> frags = line.getFragments();
//			for (int i = 0; i < frags.size(); i++) {
//				final IColoredString.Bucket<Color> b = frags.get(i);
////				Color c = b.getColor();
////				if(c != null) 
////					cache.setColors(c, ci, (ci += b.length()));
////				else
//					ci += b.length();
//			}
//
////			if(m + 1 < textSize)
////			{
//				//cache.addText("\n", pos, (-lines) * totalTextHeight, w, Align.left, true);
//				//lines++;
////			}
//		}
		lineHeight *= lines;
		if(lineHeight < 0)
			lineHeight = 0;
		textActor.setHeight(/* Entire height */ lineHeight);
		scrollPane.layout();

	}

	/**
	 * Scrolls the scroll pane this holds down by some number of rows (which may be fractional, and may be negative to
	 * scroll up). This is not a smooth scroll, and will not be animated.
	 * @param downDistance The distance in rows to scroll down, which can be negative to scroll up instead
	 */
	public void scroll(final float downDistance)
	{
		prepareText();
		scrollPane.setScrollY(scrollPane.getScrollY() + downDistance * tcf.actualCellHeight);
	}

	/**
	 * If the parameter is true, scrolls to the top of this scroll pane; otherwise scrolls to the bottom. This is not a
	 * smooth scroll, and will not be animated.
	 * @param goToTop If true, will scroll to the top edge; if false, will scroll to the bottom edge.
	 */
	public void scrollToEdge(final boolean goToTop)
	{
		prepareText();
		scrollPane.setScrollPercentY(goToTop ? 0f : 1f);
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
	 * @return The font used, if set, as a TextCellFactory (one is always created even if only given a BitmapFont).
	 */
	public /* @Nullable */ TextCellFactory getFont() {
		return tcf;
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
			prepareText();
			final float tx = 0f;//scrollPane.getX();
			final float ty = 0f;//scrollPane.getY();
			final float twidth = scrollPane.getWidth();
			final float theight = scrollPane.getHeight();

			if (backgroundColor != null) {
				batch.setColor(backgroundColor);
				batch.draw(tcf.getSolid(), tx, ty, twidth, theight);
				batch.setColor(SColor.WHITE);
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
						"The font should be set when drawing a TextPanel's TextActor");
			if (text == null)
				throw new NullPointerException(
						"The font should be set when drawing a TextPanel's TextActor");
			if (tcf != null) {
				tcf.configureShader(batch);
			}
//			final float offY = 0;//(tcf != null) ? tcf.actualCellHeight * 0.5f : 0;
			final BitmapFontCache cache = font.getCache();
			cache.setPosition(tx, scrollPane.getHeight() + scrollPane.getScrollY());
			cache.draw(batch);
		}
	}

}
