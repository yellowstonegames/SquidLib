package squidpony.squidgrid.gui.gdx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import squidpony.annotation.Beta;
import squidpony.panel.IColoredString;
import squidpony.panel.ISquidPanel;
import squidpony.squidgrid.gui.gdx.UIUtil.CornerStyle;

/**
 * A panel that layouts buttons vertically. It offers various features:
 * 
 * <ul>
 * <li>It can display margins around buttons, with various {@link CornerStyle
 * styles}.</li>
 * <li>It returns a handler to detect mouse clicks on button or add it to
 * {@code this} (if you want the handler to be called from the enclosing
 * {@link Stage}).</li>
 * <li>If {@link #shortcutCharacterColor configured}, the handler also handles
 * key shortcuts. In that case this panel highlights the shortcuts in text
 * automatically.</li>
 * <li>To handle what happens when buttons are clicked, you should implement the
 * abstract method {@link #selectedButton(int)}.</li>
 * </ul>
 * 
 * <p>
 * The panel is configured via its {@code public} fields. Configuration must
 * happen in-between creation and the call to {@link #putAll(boolean, boolean)}.
 * </p>
 * 
 * <p>
 * This class has two different behaviors w.r.t to the backing panels. They can
 * either be given at creation time or they can be created on the fly. If you're
 * doing a full screen menu, you should likely give the panels at creation time
 * (because you'll compute theirs sizes so that they fit the whole screen). If
 * you're doing an in-game menu, that is quickly dispatched, you should likely
 * let this class create the panels (it'll create panels as small as possible,
 * yet that suffice to display the buttons correctly). There is a dedicated
 * subtype for the first usage: {@link PreAllocatedPanels}.
 * </p>
 * 
 * <p>
 * Here's a full screen example of this class:
 * 
 * <br/>
 * <br/>
 * 
 * <img src="http://i.imgur.com/AQgWeic.png"/>
 * 
 * <br/>
 * <br/>
 * 
 * and this shows a non-full screen example (the Drink/Throw/Drop menu):
 * 
 * <br/>
 * <br/>
 * 
 * <img src="http://i.imgur.com/dyd7IoN.png"/>
 * </p>
 * 
 * @author smelC
 * 
 * @param <T>
 *            The type of colors.
 */
@Beta
public abstract class ButtonsPanel<T extends Color> extends GroupCombinedPanel<T> {

	/**
	 * The margin, in number of cells, in-between buttons. Do not set a negative
	 * value. If set to {@code 0}, {@code this} will layout like:
	 * 
	 * <pre>
	 *    button1
	 * longer button
	 *    button2
	 * </pre>
	 * 
	 * If set to {@code 1}, {@code this} will layout like:
	 * 
	 * <pre>
	 *    button1
	 *    
	 * longer button
	 * 
	 *    button2
	 * </pre>
	 */
	public int interButtonMargin;

	/**
	 * The margin to use around the whole panel, in number of pixels. Do not set
	 * a negative value. Typical good looking values: cellWidth/cellHeight
	 * divided by 2, 3, or 4.
	 * 
	 * <p>
	 * As an example, this margin is in dark red in the Drink/Throw/Drop menu
	 * in:
	 * 
	 * <br/>
	 * <br/>
	 * <img src="http://i.imgur.com/dyd7IoN.png">small example</img>
	 * </p>
	 */
	public int borderMargin;

	/**
	 * The x-padding in-between a button's text and its margin, in number of
	 * cells. This padding is used at the left and the right. Do not set a
	 * negative value. If set to {@code 0}, {@code this} will layout each button
	 * like:
	 * 
	 * <pre>
	 * ---------
	 * |button1|
	 * --------
	 * </pre>
	 * 
	 * If set to {@code 2}, {@code this} will layout like:
	 * 
	 * <pre>
	 * -------------
	 * |  button1  |
	 * -------------
	 * </pre>
	 */
	public int xpadding;

	/**
	 * The y-padding in-between a button's text and its margin, in number of
	 * cells. This padding is used at the top and the bottom. Do not set a
	 * negative value. If set to {@code 0}, {@code this} will layout each button
	 * like:
	 * 
	 * <pre>
	 * ---------
	 * |button1|
	 * --------
	 * </pre>
	 * 
	 * If set to {@code 1}, {@code this} will layout like:
	 * 
	 * <pre>
	 * -------------
	 * |           |
	 * |  button1  |
	 * |           |
	 * -------------
	 * </pre>
	 * 
	 */
	public int ypadding;

	/**
	 * The margin to show around each button, in number of pixels. Do not set a
	 * negative value.
	 */
	public int buttonMargin;

	/**
	 * The style to use for the buttons' corners. Do not set to {@code null}.
	 */
	public CornerStyle cornerStyle = CornerStyle.ROUNDED;

	/**
	 * The color to use to paint the background (outside buttons). Or
	 * {@code null} to disable background coloring.
	 */
	public /* @Nullable */ T bgColor;

	/**
	 * The color to use to paint the background of the inside of buttons, or
	 * {@code null} to disable painting.
	 */
	public /* @Nullable */ T insideButtonBGColor;

	/**
	 * The color for the margin around buttons, or {@code null} to disable
	 * painting.
	 */
	public /* @Nullable */ T buttonsMarginColor;

	/**
	 * The color for the border around the whole panel, or {@code null} to
	 * disable painting.
	 */
	public /* @Nullable */ T borderColor;

	/**
	 * The color to use to highlight shortcuts of buttons. Or {@code null} to
	 * disable shortcuts.
	 */
	public /* @Nullable */ T shortcutCharacterColor;

	/**
	 * The style of borders, around the panel. Do not set it to {@code null}.
	 */
	public CornerStyle borderStyle = CornerStyle.ROUNDED;

	/**
	 * The alignment of buttons, -1 for left, 0 for center, 1 for right. If non-
	 * {@code null}, this array's length must be the number of buttons.
	 * 
	 * <p>
	 * The default is center.
	 * </p>
	 */
	public /* @Nullable */ int[] buttonsAlignment;

	/**
	 * Indexes of buttons that should not receive a shortcut, neither click
	 * events. Ignored if {@link #shortcutCharacterColor} is {@code null}.
	 */
	public /* @Nullable */ Set<Integer> doNotBind;

	/**
	 * Really, if you're muting this beyond {@link #init(List)}, you're doing
	 * bad.
	 */
	protected List<IColoredString<T>> buttonsTexts;

	/** The positions of buttons, set by {@link #putAll(boolean, boolean)} */
	protected /* @Nullable */ List<Rectangle> buttons;

	/**
	 * The shortcuts to select the buttons. Keys are the shortcuts (always in
	 * lowercase) while values are indexes of {@link #buttonsTexts}. Or
	 * {@code null} if shortcuts are disabled.
	 * 
	 * <p>
	 * Initialized and filled in {@link #putShortcut(int, IColoredString)}.
	 * </p>
	 */
	protected /* @Nullable */ Map<Character, Integer> shortcuts;

	protected int hcells = -1;
	protected int vcells = -1;

	/**
	 * If you use this constructor, you can use {@link PreAllocatedPanels} to
	 * avoid having to define {@link #buildPanel(int, int)}.
	 * 
	 * @param bg
	 *            The backing background panel.
	 * @param fg
	 *            The backing foreground panel.
	 * @param buttonTexts
	 *            The text of buttons. It should not contain end of lines
	 *            (beware that this isn't checked). If {@code null}, it MUST be
	 *            set later on with {@link #init(List)}.
	 * @throws IllegalStateException
	 *             In various cases of errors regarding sizes of panels.
	 */
	public ButtonsPanel(ISquidPanel<T> bg, ISquidPanel<T> fg, List<IColoredString<T>> buttonTexts) {
		super(bg, fg);
		if (buttonTexts != null) {
			this.buttonsTexts = new ArrayList<IColoredString<T>>(buttonTexts.size());
			this.buttonsTexts.addAll(buttonTexts);
		}
	}

	/**
	 * Constructor to use when you want the panels to be build using
	 * {@link #buildPanel(int, int)}.
	 * 
	 * @param buttonTexts
	 *            The text of buttons. It should not contain end of lines
	 *            (beware that this isn't checked). If {@code null}, it MUST be
	 *            set later on with {@link #init(List)}.
	 * @throws IllegalStateException
	 *             In various cases of errors regarding sizes of panels.
	 */
	public ButtonsPanel(List<IColoredString<T>> buttonTexts) {
		if (buttonTexts != null) {
			this.buttonsTexts = new ArrayList<IColoredString<T>>(buttonTexts.size());
			this.buttonsTexts.addAll(buttonTexts);
		}
	}

	/**
	 * Sets the buttons' text. Use this method if you gave {@code null} at
	 * creation time.
	 * 
	 * @param buttonTexts
	 * @return {@code this}
	 */
	public ButtonsPanel<T> init(List<IColoredString<T>> buttonTexts) {
		this.buttonsTexts = new ArrayList<IColoredString<T>>(buttonTexts);
		return this;
	}

	/**
	 * Displays this panel. You should very likely call this method exactly
	 * once.
	 * 
	 * @param putBordersAndMargins
	 *            Whether to draw margins and the border. This should be
	 *            {@code true} if {@code this}'s position (I mean, in terms of
	 *            {@link #setPosition(float, float)}) is set already. If that's
	 *            not the case (for example, because you need this method to be
	 *            called to compute the position from {@link #getHCells()} and
	 *            {@link #getVCells()}), give {@code false}.
	 * @return The {@link InputProcessor} to plug if you want
	 *         {@link #selectedButton(int)} to be called.
	 * 
	 *         <p>
	 *         See {@link #y_gdxToSquid()} to configure the processor's
	 *         behavior.
	 *         </p>
	 * @throws NullPointerException
	 *             If the text of buttons wasn't given at creation time, and
	 *             {@link #init(List)} wasn't called since then.
	 */
	public InputProcessor putAll(boolean addListener, boolean putBordersAndMargins) {
		{
			hcells = computeRequiredCellsWidth();
			vcells = computeRequiredCellsHeight();

			if (bg == null) {
				/* Panels weren't given at creation time */
				assert fg == null;
				setPanels(buildPanel(hcells, vcells), buildPanel(hcells, vcells));
			}

			/*
			 * We don't include buttons margins, because they are rendered using
			 * ShapeRenderer, whereas the "important" space is the one for the
			 * SquidPanel, i.e. the text of buttons, the paddings, and the inter
			 * button margins.
			 */
			/* The call to #cellWidth() requires #bg to be set */
			final int width = hcells * cellWidth();
			final int availableW = pixelsWidth();
			if (availableW < width)
				Gdx.app.log("layout",
						String.format(
								"Cannot layout %s correctly. Required pixels width: %d. Pixels width available: %d",
								getClass().getSimpleName(), width, availableW));

			/* The call to #cellHeight() requires #bg to be set */
			final int height = vcells * cellHeight();
			final int availableH = pixelsHeight();
			if (availableH < height)
				Gdx.app.log("layout",
						String.format(
								"Cannot layout %s. Required pixels height: %d. Pixels height available: %d",
								getClass().getSimpleName(), height, availableH));
		}

		final int gridHeight = bg.gridHeight();

		final int totalVerticalMargin = gridHeight - vcells;
		/*
		 * Inexact division is floored (3/2 = 1), hence this aligns to the top.
		 */
		int topMargin = totalVerticalMargin / 2;

		if (buttonsTexts == null)
			throw new NullPointerException(
					"The text of buttons must be set before displaying a " + getClass().getSimpleName());

		final int nbButtons = buttonsTexts.size();

		this.buttons = new ArrayList<Rectangle>(nbButtons);

		if (bgColor != null) {
			/* Paint whole background */
			fillBG(bgColor);
		}

		int m = topMargin;
		/* In number of cells */
		final int buttonHeight = buttonCellsHeight();
		for (int i = 0; i < nbButtons; i++) {
			final int alignment;
			if (buttonsAlignment != null && i < buttonsAlignment.length)
				alignment = buttonsAlignment[i];
			else
				/* The default: centering */
				alignment = 0;
			putButton(i, m, alignment, putBordersAndMargins);
			m += buttonHeight;
			m += interButtonMargin;
		}

		if (putBordersAndMargins)
			putBorder();

		final InputMultiplexer result = new InputMultiplexer();
		/*
		 * First put the key handler. It'd be nice to use SquidLib's KeyHandler
		 * interface (it'd be more convenient in #selectedButton for complex
		 * inputs), but it requires more work.
		 */
		result.addProcessor(buildKeyInputProcessor());
		/*
		 * Then the mouse handler. SquidInput is required here, since it does
		 * the translation from libgdx's coordinates to SquidLib coordinates.
		 */
		result.addProcessor(new SquidInput(new SquidMouse(bg.cellWidth(), bg.cellHeight(), bg.gridWidth(),
				bg.gridHeight(), 0, 0, buildMouseInputProcessor())));

		if (addListener) {
			addListener(new InputListener() {
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					/* Because we want to receive touchUp */
					return true;
				}

				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
					result.touchUp(MathUtils.round(x), MathUtils.round(y), pointer, buttonHeight);
				}

				@Override
				public boolean keyTyped(InputEvent event, char character) {
					return result.keyTyped(character);
				}

			});
		}

		return result;
	}

	/**
	 * Draws the margins around the buttons and the border around the panel.
	 * Because this uses a {@link ShapeRenderer}, margins do not move if
	 * {@code this} is moved (such as with
	 * {@link Actions#moveTo(float, float, float)}). This method's purpose is to
	 * draw margins in such a case.
	 * 
	 * <p>
	 * Remember that, when using {@link SquidPanel}s; margins (more generally:
	 * anything done with {@link ShapeRenderer}) should always be drawn after
	 * everything else, so when sliding/moving, your code should be like:
	 * 
	 * <pre>
	 * stage.act()   <- if the panel is moving
	 * stage.draw(); <- of course, 'stage' is the Stage containing {@code this}
	 * buttonsPanel.putMarginsAndBorder();
	 * </pre>
	 * </p>
	 */
	public void putButtonsMarginsAndBorder() {
		if (buttons == null)
			/* Not set yet */
			return;

		/* Margins around buttons */
		final int bound = buttons.size();
		/* index-based loop, to avoid allocating the Iterator */
		for (int i = 0; i < bound; i++) {
			final Rectangle r = buttons.get(i);
			displayMarginsAround(r.botLeftX, r.botLeftY, r.width, r.height);
		}

		/* The border */
		putBorder();
	}

	/**
	 * This method should only be called after {@link #putAll(boolean, boolean)}
	 * .
	 * 
	 * @return The number of cells that the buttons of {@code this} spans,
	 *         horizontally. Ignores margins, but not padding.
	 * @throws IllegalStateException
	 *             If {@link #putAll(boolean, boolean)} wasn't called yet.
	 */
	public int getHCells() {
		if (hcells == -1)
			throw new IllegalStateException("This method should be called after #putAll");
		return hcells;
	}

	/**
	 * This method should only be called after {@link #putAll(boolean, boolean)}
	 * .
	 * 
	 * @return The number of cells that the buttons of {@code this} spans,
	 *         vertically. Ignores margins, but not padding.
	 * @throws IllegalStateException
	 *             If {@link #putAll(boolean, boolean)} wasn't called yet.
	 */
	public int getVCells() {
		if (vcells == -1)
			throw new IllegalStateException("This method should be called after #putAll");
		return vcells;
	}

	protected InputProcessor buildMouseInputProcessor() {
		return new InputAdapter() {

			@Override
			public boolean touchUp(int x, int gdxy, int pointer, int button) {
				final int y;
				if (y_gdxToSquid())
					y = getGridHeight() - (gdxy + 1);
				else
					y = gdxy;
				final int bound = buttons.size();
				for (int i = 0; i < bound; i++) {
					final Rectangle r = buttons.get(i);
					if (x < r.botLeftX)
						/* Too much to the left */
						continue;
					if (y < r.botLeftY - r.height)
						/* Too high */
						continue;
					if (r.botLeftY <= y)
						/* Too low */
						continue;
					if (r.botLeftX + r.width <= x)
						/* Too much to the right */
						continue;

					/* It's a hit! */

					if (doNotBind == null || !doNotBind.contains(i))
						/* Send event */
						selectedButton(i);

					return true;
				}

				return false;
			}

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				/* Not handled for now */
				return false;
			}

			@Override
			public boolean mouseMoved(int screenX, int screenY) {
				/* Not handled for now */
				return false;
			}

			@Override
			public boolean scrolled(int amount) {
				/* Not handled for now */
				return false;
			}
		};
	}

	protected InputProcessor buildKeyInputProcessor() {
		return new InputAdapter() {
			@Override
			public boolean keyTyped(char character) {
				if (shortcuts == null)
					return false;
				Integer buttonIndex = shortcuts.get(Character.toLowerCase(character));
				if (buttonIndex != null) {
					selectedButton(buttonIndex);
					return true;
				} else
					return false;
			}
		};
	}

	/**
	 * This method can be left unimplemented if you give the panels at
	 * construction time (constructor
	 * {@link #ButtonsPanel(ISquidPanel, ISquidPanel, List)}.
	 * 
	 * @param width
	 *            The width that the panel must have.
	 * @param height
	 *            The height that the panel must have.
	 * @return A freshly allocated {@link ISquidPanel}.
	 */
	protected abstract ISquidPanel<T> buildPanel(int width, int height);

	/**
	 * This method is called when the button at index {@code i} is hit.
	 * 
	 * @param i
	 *            The index of a button (starts at 0).
	 */
	protected abstract void selectedButton(int i);

	/**
	 * smelC: when I plug {@link #putAll(boolean, boolean)} result directly into
	 * {@link Gdx#input} (i.e. when I give {@code false} as the first argument
	 * to {@code putAll}), I leave this definition. When I give {@code putAll}
	 * {@code true}, and the listener is plugged behind a {@link Stage}, I
	 * redefine this method to return {@code true}.
	 * 
	 * @return {@code true} if the y-coordinate must be translated from libgdx
	 *         coordinates (0,0) at bottom left to squid coordinates (0,0) at
	 *         top left.
	 */
	protected boolean y_gdxToSquid() {
		return false;
	}

	/**
	 * @param botLeftX
	 *            The bottom left x cell of the button's inside, in squidlib's
	 *            coordinates ((0,0) is top left).
	 * @param botLeftY
	 *            The bottom left y cell of the button's inside, in squidlib's
	 *            coordinates ((0,0) is top left).
	 * @param width
	 *            The width of the button considered.
	 * @param height
	 *            The width of the button considered.
	 */
	protected void displayMarginsAround(int botLeftX, int botLeftY, int width, int height) {
		if (buttonMargin == 0 || buttonsMarginColor == null)
			/* Nothing to do */
			return;

		/* Actor's bottom left */
		final float x = getX();
		final float y = getY();

		final int cw = bg.cellWidth();
		final int ch = bg.cellHeight();

		/* Button button left, in libgdx's coordinates */
		final float gdxx = x + botLeftX * cw;
		final float gdxy = y + ((bg.gridHeight() - botLeftY) * ch);

		/* Width of the button's inside */
		final int w = width * cw;
		/* Height of the button's inside */
		final int h = height * ch;

		UIUtil.drawMarginsAround(gdxx, gdxy, w, h, buttonMargin, buttonsMarginColor, cornerStyle);
	}

	protected int pixelsWidth() {
		return bg.cellWidth() * bg.gridWidth();
	}

	protected int pixelsHeight() {
		return bg.cellHeight() * bg.gridHeight();
	}

	/**
	 * Method that paint's the background of a button's inside.
	 * 
	 * @param xoff
	 *            The x offset
	 * @param yoff
	 *            The y offset
	 * @param width
	 *            The inside's width
	 * @param height
	 *            The inside's height
	 */
	protected void putButtonInside(int xoff, int yoff, int width, int height) {
		if (insideButtonBGColor == null)
			return;

		for (int x = 0; x < width; x++) {
			for (int w = 0; w < height; w++)
				putBG(x + xoff, w + yoff, insideButtonBGColor);
		}
	}

	/**
	 * Paints the border
	 */
	protected void putBorder() {
		if (0 < borderMargin && borderColor != null)
			UIUtil.drawMarginsAround(getX(), getY(), getHCells() * cellWidth(), getVCells() * cellHeight(),
					borderMargin, borderColor, borderStyle);
	}

	/**
	 * @param buttonIndex
	 *            The index of the button for which the text is being built.
	 * @param text
	 * @return The text to display, with the coloring to highlight the shortcut.
	 */
	protected IColoredString<? extends T> putShortcut(int buttonIndex, IColoredString<T> text) {
		assert 0 <= buttonIndex && buttonIndex < buttonsTexts.size();
		if (shortcutCharacterColor == null || (doNotBind != null && doNotBind.contains(buttonIndex)))
			/*
			 * Shortcuts are disabled or no shortcut should be put on this
			 * button
			 */
			return text;

		if (shortcuts == null)
			shortcuts = new HashMap<Character, Integer>();

		final IColoredString<T> result = new IColoredString.Impl<T>();
		boolean set = false;
		for (IColoredString.Bucket<T> bucket : text) {
			final String bucketText = bucket.getText();
			final T bucketColor = bucket.getColor();
			if (set) {
				/* Append the bucket to the result */
				result.append(bucketText, bucketColor);
			} else {
				final int bound = bucketText.length();
				/* Iterate over the bucket's text */
				for (int i = 0; i < bound; i++) {
					final char c = bucketText.charAt(i);
					if (set || shortcuts.containsKey(Character.toLowerCase(c))
							|| !Character.isAlphabetic(c)) {
						/*
						 * Shortcut already used or we went into the 'else'
						 * already, or character is inadequate.
						 */
						result.append(c, bucketColor);
					} else {
						/* Let's use 'c' as the shortcut! */
						result.append(c, shortcutCharacterColor);
						shortcuts.put(Character.toLowerCase(c), buttonIndex);
						set = true;
					}
				}
			}
		}
		return result;
	}

	/**
	 * @param i
	 *            The button index with {@link #buttonsTexts}.
	 * @param y
	 *            The y offset from the panel's top.
	 * @param alignment
	 *            The alignment, in the format of {@link #buttonsAlignment}.
	 */
	private void putButton(int i, int y, int alignment, boolean putMargins) {
		final IColoredString<T> text = buttonsTexts.get(i);
		final int textLength = text.length();

		final int insideWidth = textLength + (xpadding * 2);
		final int insideHeight = 1 + (ypadding * 2);

		final int gridWidth = getGridWidth();
		final int center = gridWidth / 2;
		/* The leftmost cell of the button's inside */
		final int left;
		/* The left most cell of the button's text */
		final int textLeft;
		if (alignment == -1) {
			/* Align to left */
			left = 0;
			textLeft = 0;
		} else if (alignment == 1) {
			/* Align to right */
			left = gridWidth - insideWidth;
			textLeft = gridWidth - textLength;
		} else {
			/* Align to center */
			left = center - (insideWidth / 2);
			textLeft = center - textLength / 2;
		}

		/* Paint the button's inside's background */
		if (insideButtonBGColor != null) {
			for (int x = 0; x < insideWidth; x++) {
				for (int w = 0; w < insideHeight; w++)
					putBG(x + left, w + y, insideButtonBGColor);
			}
		}

		/* Now place text */
		putFG(textLeft, y + (insideHeight / 2), putShortcut(i, text));

		final int botLeftY = y + insideHeight;

		if (putMargins)
			/* Finally, paint margins */
			displayMarginsAround(left, botLeftY, insideWidth, insideHeight);

		if (this.buttons == null)
			throw new NullPointerException(String.format("%s: this.buttons should not be null at this moment",
					getClass().getSimpleName()));

		this.buttons.add(new Rectangle(left, botLeftY, insideWidth, insideHeight));
	}

	/**
	 * This method's implementation should not inspect {@link #fg} or
	 * {@link #bg}, because they may not have been set yet
	 */
	private int computeRequiredCellsWidth() {
		int maxButtonWidth = 0;
		for (IColoredString<?> cs : buttonsTexts) {
			final int local = cs.length();
			if (maxButtonWidth < local)
				maxButtonWidth = local;
		}
		final int nbCells = maxButtonWidth + (xpadding * 2);
		return nbCells;
	}

	/**
	 * This method's implementation should not inspect {@link #fg} or
	 * {@link #bg}, because they may not have been set yet
	 */
	private int computeRequiredCellsHeight() {
		final int nbButtons = buttonsTexts.size();
		final int nbCells = nbButtons + (nbButtons * 2 * ypadding) + ((nbButtons - 1) * interButtonMargin);
		/*
		 * Call to max to avoid returning something negative (can happen if
		 * nbButtons == 0)
		 */
		return Math.max(nbCells, 0);
	}

	/**
	 * @return The height of one button, in number of cells.
	 */
	private int buttonCellsHeight() {
		/* The button's text + the padding at the bottom and the top */
		return 1 + ypadding * 2;
	}

	/**
	 * A convenience subclass for people that give preallocated
	 * {@link ISquidPanel}s. A single abstract method remains to be implemented.
	 * 
	 * @author smelC
	 */
	public static abstract class PreAllocatedPanels<T extends Color> extends ButtonsPanel<T> {

		protected PreAllocatedPanels(ISquidPanel<T> bg, ISquidPanel<T> fg,
				List<IColoredString<T>> buttonTexts) {
			super(bg, fg, buttonTexts);
		}

		@Override
		protected ISquidPanel<T> buildPanel(int width, int height) {
			throw new IllegalStateException("This method should not be called");
		}

	}

	/**
	 * @author smelC
	 */
	protected static class Rectangle {

		/**
		 * The bottom left coordinate of this rectangle (in number of cells), in
		 * SquidLib's coordinates.
		 */
		protected final int botLeftX;
		/**
		 * The bottom left coordinate of this rectangle (in number of cells), in
		 * SquidLib's coordinates.
		 */
		protected final int botLeftY;

		/** The width of this rectangle, in number of cells */
		protected final int width;

		/** The height of this rectangle, in number of cells */
		protected final int height;

		public Rectangle(int botLeftX, int botLeftY, int width, int height) {
			this.botLeftX = check(botLeftX, "bottom left x");
			this.botLeftY = check(botLeftY, "bottom left y");
			this.width = check(width, "width");
			this.height = check(height, "height");
		}

		private int check(int i, String s) {
			if (i < 0)
				Gdx.app.log("layout", String.format("Invalid rectangle component (%s): %d", s, i));
			return i;
		}

		@Override
		public String toString() {
			return String.format("Rectangle at (%d,%d): width %d, height %d", botLeftX, botLeftY, width,
					height);
		}
	}
}
