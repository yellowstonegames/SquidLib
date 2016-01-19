package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import squidpony.IColorCenter;

/**
 * Class for creating text blocks.
 *
 * This class defaults to having no padding and having no font set. You can use a
 * default square or narrow font by calling the appropriate method, or set the font
 * to any AngelCode bitmap font on the classpath (typically in libGDX, this would be
 * in the assets folder; these fonts can be created by Hiero in the libGDX tools,
 * see https://github.com/libgdx/libgdx/wiki/Hiero for more)
 *
 * After all settings are set, one of the initialization methods must be called
 * before the factory can be used.
 *
 * In order to easily support Unicode, strings are treated as a series of code
 * points.
 *
 * All images have transparent backgrounds.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class TextCellFactory implements Disposable {

    /**
     * The commonly used symbols in roguelike games.
     */
    public static final String DEFAULT_FITTING = "@!#$%^&*()_+1234567890-=~ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz;:,'\"{}?/\\ ",
    LINE_FITTING = "┼├┤┴┬┌┐└┘│─", SQUID_FITTING = DEFAULT_FITTING + LINE_FITTING;

	/**
	 * The {@link AssetManager} from where to load the font. Use it to share
	 * loading of a font's file across multiple factories.
	 */
	protected /* Nullable */AssetManager assetManager;
    protected BitmapFont bmpFont = null;
    protected Texture block = null;
    protected String fitting = SQUID_FITTING;
    protected IColorCenter<Color> scc;
    protected int leftPadding = 0, rightPadding = 0, topPadding = 0, bottomPadding = 0;
    protected int width = 1, height = 1;
    protected float distanceFieldScaleX = 36f, distanceFieldScaleY = 36f;
    private boolean initialized = false, distanceField = false;

    /**
     * Creates a default valued factory. One of the initialization methods must
     * be called before this factory can be used!
     */
    public TextCellFactory() {
    	this(null);
    }

	/**
	 * A default valued factory that uses the given {@link AssetManager} to load
	 * the font file. Use this constructor if you are likely to load the same
	 * font over and over (recall that, without an {@link AssetManager}, each
	 * instance of {@link TextCellFactory} will load its font from disk). This
     * primarily matters if you are using fonts not bundled with SquidLib, since
     * accessing a BitmapFont with a method (not a String) from DefaultResources
     * caches the BitmapFont already.
	 * 
	 * @param assetManager an ordinary libGDX AssetManager
	 */
    public TextCellFactory(/* Nullable */ AssetManager assetManager) {
    	this.assetManager = assetManager;
        this.scc = DefaultResources.getSCC();
    }

    /**
     * Initializes the factory to then be able to create text cells on demand.
     *
     * Will match the width and height to 12 and 12, scaling the font to fit.
     *
     * Calling this after the factory has already been initialized will
     * re-initialize it.
     *
     * @return this for method chaining
     */
    public TextCellFactory initByFont() {
        bmpFont.setFixedWidthGlyphs(fitting);
        width = (int)bmpFont.getSpaceWidth();
        height = (int)(bmpFont.getLineHeight());
        Pixmap temp = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        temp.setColor(Color.WHITE);
        temp.fill();
        block = new Texture(1, 1, Pixmap.Format.RGBA8888);
        block.draw(temp, 0, 0);
        temp.dispose();
        initialized = true;
        return this;
    }

    /**
     * Initializes the factory to then be able to create text cells on demand.
     *
     * Will strictly use the provided width and height values to size the cells.
     *
     * Calling this after the factory has already been initialized will
     * re-initialize it.
     *
     * @return this for method chaining
     */
    public TextCellFactory initBySize() {
        bmpFont.setFixedWidthGlyphs(fitting);
        Pixmap temp = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        temp.setColor(Color.WHITE);
        temp.fill();
        block = new Texture(1, 1, Pixmap.Format.RGBA8888);
        block.draw(temp, 0, 0);
        temp.dispose();
        if(distanceField)
        {
            bmpFont.getData().setScale(width / distanceFieldScaleX, height / distanceFieldScaleY);
            //distanceFieldScaleX *= (((float)width) / height) / (distanceFieldScaleX / distanceFieldScaleY);
        }
        initialized = true;
        return this;
    }

    /**
     * Initializes the factory to then be able to create text cells on demand.
     *
     * (This is identical to initBySize() when using libGDX.)
     *
     * @return this for method chaining
     */
    public TextCellFactory initVerbatim() {
        return initBySize();
    }

    /**
     * Returns the font used by this factory.
     *
     * @return the font
     */
    public BitmapFont font() {
        return bmpFont;
    }

    /**
     * Sets this factory to use the provided font.
     *
     * This is a way to complete a needed step; the font must be set before initializing, which can be done by a few
     * methods in this class.
     *
     * This should be called with an argument such as "Rogue-Zodiac-6x12.fnt", that is, it should have the .fnt
     * extension as opposed to the .png that accompanies such a bitmap font. The bitmap font should be either in the
     * internal folder that libGDX knows about, which means it is in the assets folder of your project usually, or it
     * can be on the classpath, which mostly applies to these resources bundled with SquidLib:
     * <ul>
     *     <li>DefaultResources.squareName = "Zodiac-Square-12x12.fnt"</li>
     *     <li>DefaultResources.narrowName = "Rogue-Zodiac-6x12.fnt"</li>
     *     <li>DefaultResources.unicodeName = "Mandrill-6x16.fnt"</li>
     *     <li>DefaultResources.smoothName = "Inconsolata-LGC-8x18.fnt"</li>
     *     <li>DefaultResources.squareNameLarge = "Zodiac-Square-24x24.fnt"</li>
     *     <li>DefaultResources.narrowNameLarge = "Rogue-Zodiac-12x24.fnt"</li>
     *     <li>DefaultResources.unicodeNameLarge = "Mandrill-12x32.fnt"</li>
     *     <li>DefaultResources.smoothNameLarge = "Inconsolata-LGC-12x24.fnt"</li>
     *     <li>DefaultResources.narrowNameExtraLarge = "Rogue-Zodiac-18x36.fnt"</li>
     *     <li>There is also a sequence of resized versions of Inconsolata LGC, altered to fit in a square area. These
     *     don't have names in DefaultResources; you should use the overload of font() that takes a BitmapFont if you
     *     want to use the multiple, increasingly-resized versions.</li>
     * </ul>
     * "Rogue-Zodiac-12x24.fnt", which is easily accessed by the field DefaultResources.narrowNameLarge , can also
     * be set using TextCellFactory.defaultNarrowFont() instead of font(). "Zodiac-Square-12x12.fnt", also accessible
     * as DefaultResources.squareName , can be set using TextCellFactory.defaultSquareFont() instead of font().
     * "Inconsolata-LGC-12x24.fnt", also accessible as DefaultResources.smoothNameLarge , can be set using
     * TextCellFactory.defaultFont() instead of font(). All three of these alternatives will cache the BitmapFont if
     * the same one is requested later, but this font() method will not.
     * <br>
     * See https://github.com/libgdx/libgdx/wiki/Hiero for some ways to create a bitmap font this can use. Several fonts
     * in this list were created using Hiero (not Hiero4), and several were created with AngelCode's BMFont tool.
     *
     * @param fontpath the path to the font to use
     * @return this factory for method chaining
     */
    public TextCellFactory font(String fontpath) {
        if (assetManager == null) {
            if (Gdx.files.internal(fontpath).exists())
                bmpFont = new BitmapFont(Gdx.files.internal(fontpath));
            else if (Gdx.files.classpath(fontpath).exists())
                bmpFont = new BitmapFont(Gdx.files.classpath(fontpath));
            else
                bmpFont = DefaultResources.getDefaultFont();
        }
        else {
            assetManager.load(new AssetDescriptor<BitmapFont>(fontpath, BitmapFont.class));
			/*
			 * We're using the AssetManager not be asynchronous, but to avoid
			 * loading a file twice (because that takes some time (tens of
			 * milliseconds)). Hence this KISS code to avoid having to handle a
			 * not-yet-loaded font:
			 */
            assetManager.finishLoading();
            bmpFont = assetManager.get(fontpath, BitmapFont.class);
        }
        return this;
    }
    /**
     * Sets this factory to use the provided BitmapFont as its font without re-constructing anything.
     *
     * This is a way to complete a needed step; the font must be set before initializing, which can be done by a few
     * methods in this class.
     *
     * This should be called with an argument such as {@code DefaultResources.getDefaultFont()} or any other variable
     * with BitmapFont as its type. The bitmap font will not be loaded from file with this method, which it would be if
     * you called the overload of font() that takes a String more than once. These BitmapFont resources are already
     * bundled with SquidLib:
     * <ul>
     *     <li>DefaultResources.getDefaultFont() = "Zodiac-Square-12x12.fnt"</li>
     *     <li>DefaultResources.getDefaultNarrowFont() = "Rogue-Zodiac-6x12.fnt"</li>
     *     <li>DefaultResources.getDefaultUnicodeFont() = "Mandrill-6x16.fnt"</li>
     *     <li>DefaultResources.getSmoothFont() = "Inconsolata-LGC-8x18.fnt"</li>
     *     <li>DefaultResources.getLargeFont() = "Zodiac-Square-24x24.fnt"</li>
     *     <li>DefaultResources.getLargeNarrowFont() = "Rogue-Zodiac-12x24.fnt"</li>
     *     <li>DefaultResources.getLargeUnicodeFont() = "Mandrill-12x32.fnt"</li>
     *     <li>DefaultResources.getLargeSmoothFont() = "Inconsolata-LGC-12x24.fnt"</li>
     *     <li>DefaultResources.getExtraLargeNarrowFont() = "Rogue-Zodiac-18x36.fnt"</li>
     *     <li>There is also a sequence of resized versions of Inconsolata LGC, altered to fit in a square area. These
     *     can be accessed with DefaultResources.getZoomedFont(), passing an int between 0 and 11 inclusive.</li>
     * </ul>
     * "Rogue-Zodiac-12x24.fnt", which is easily accessed by the method DefaultResources.getLargeNarrowFont() , can also
     * be set using TextCellFactory.defaultNarrowFont() instead of font(). "Zodiac-Square-12x12.fnt", also accessible
     * with DefaultResources.getDefaultFont() , can be set using TextCellFactory.defaultSquareFont() instead of font().
     * "Inconsolata-LGC-12x24.fnt", also accessible with DefaultResources.getLargeSmoothFont() , can be set using
     * TextCellFactory.defaultFont() instead of font(). All three of these alternatives will cache the BitmapFont if
     * the same one is requested later, but this font() method will not.
     * <br>
     * See https://github.com/libgdx/libgdx/wiki/Hiero for some ways to create a bitmap font this can use. Several fonts
     * in this list were created using Hiero (not Hiero4), and several were created with AngelCode's BMFont tool.
     *
     * @param bitmapFont the BitmapFont this should use
     * @return this factory for method chaining
     */
    public TextCellFactory font(BitmapFont bitmapFont) {
        if (bitmapFont == null) {
            bmpFont = DefaultResources.getDefaultFont();
        }
        else {
            bmpFont = bitmapFont;
        }
        return this;
    }

    /**
     * Sets the font to a distance field font with the given String path to a .fnt file and String path to a texture.
     * Distance field fonts should scale cleanly to multiple resolutions without artifacts. Does not use AssetManager
     * since you shouldn't need to reload the font if it scales with one image.
     * <br>
     * At least two distance field fonts are included in SquidLib; one is square, one is narrow, and they can both be
     * accessed using either the predefined TextCellFactory objects in DefaultResources, accessible with
     * getStretchableFont() for narrow or getStretchableSquareFont() for square, or the setter methods in this class,
     * defaultDistanceFieldFont() for square and defaultNarrowDistanceFieldFont() for narrow.
     * <br>
     * To create distance field fonts that work well with monospace layout is... time-consuming and error-prone, though
     * not especially difficult for most fonts. The process is documented as well as we can, given how differently all
     * fonts are made, in a file not included in the distribution JAR but present on GitHub:
     * https://github.com/SquidPony/SquidLib/blob/master/squidlib/etc/making-distance-field-fonts.txt
     * @param fontPath the path to a .fnt bitmap font file with distance field effects applied, which requires a complex
     *                 process to create.
     * @param texturePath the path to the texture used by the bitmap font
     * @return this factory for method chaining
     */
    public TextCellFactory fontDistanceField(String fontPath, String texturePath) {
        Texture tex;
        if (Gdx.files.internal(fontPath).exists()) {
            tex = new Texture(Gdx.files.internal(texturePath), true);
            tex.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear);
        } else if (Gdx.files.classpath(fontPath).exists()) {
            tex = new Texture(Gdx.files.classpath(texturePath), true);
            tex.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear);
        } else {
            bmpFont = DefaultResources.getDefaultFont();
            Gdx.app.error("TextCellFactory", "Could not find font files, using defaults");
            return this;
        }
        if (Gdx.files.internal(fontPath).exists()) {
            bmpFont = new BitmapFont(Gdx.files.internal(fontPath), new TextureRegion(tex), false);
            distanceField = true;
        } else if (Gdx.files.classpath(fontPath).exists()) {
            bmpFont = new BitmapFont(Gdx.files.classpath(fontPath), new TextureRegion(tex), false);
            distanceField = true;
        } else {
            bmpFont = DefaultResources.getDefaultFont();
            Gdx.app.error("TextCellFactory", "Could not find font files, using defaults");
        }
        distanceFieldScaleX = bmpFont.getData().getGlyph(' ').xadvance - 1f;
        distanceFieldScaleY = bmpFont.getLineHeight() - 1f;
        return this;
    }
    /**
     * Sets this factory to use a default 12x24 font that supports Latin, Greek, Cyrillic, and many more, including
     * box-drawing characters, zodiac signs, playing-card suits, and chess piece symbols. This is enough to support the
     * output of anything that DungeonUtility can make for a dungeon or FakeLanguageGen can make for text with its
     * defaults, which is difficult for any font to do. The box-drawing characters in this don't quite line up, and in
     * some colors there may appear to be gaps (white text on black backgrounds will show it, but not much else).
     *
     * This is a way to complete a needed step; the font must be set before initializing, which can be done by a few
     * methods in this class.
     *
     * @return this factory for method chaining
     */
    public TextCellFactory defaultFont()
    {
        bmpFont = DefaultResources.getLargeSmoothFont();
        return this;
    }
    /**
     * Sets this factory to use a default 12x24 font that renders very accurately, with no gaps between box-drawing
     * characters and very geometric lines.
     *
     * This is a way to complete a needed step; the font must be set before initializing, which can be done by a few
     * methods in this class.
     *
     * @return this factory for method chaining
     */
    public TextCellFactory defaultNarrowFont()
    {
        bmpFont = DefaultResources.getLargeNarrowFont();
        return this;
    }

    /**
     * Sets this factory to use a default 12x12 font, which... is square, and doesn't look as bad as many square fonts
     * do, plus it supports box-drawing characters with no gaps.
     *
     * This is a way to complete a needed step; the font must be set before initializing, which can be done by a few
     * methods in this class.
     *
     * @return this factory for method chaining
     */
    public TextCellFactory defaultSquareFont()
    {
        bmpFont = DefaultResources.getDefaultFont();
        return this;
    }
    public TextCellFactory defaultDistanceFieldFont()
    {
        fontDistanceField(DefaultResources.distanceFieldSquare, DefaultResources.distanceFieldSquareTexture);
        return this;
    }
    public TextCellFactory defaultNarrowDistanceFieldFont()
    {
        fontDistanceField(DefaultResources.distanceFieldNarrow, DefaultResources.distanceFieldNarrowTexture);
        return this;
    }

    /**
     * Returns the width of a single cell.
     *
     * @return the width
     */
    public int width() {
        return width;
    }

    /**
     * Sets the factory's cell width to the provided value. Clamps at 1 on the
     * lower bound to ensure valid calculations.
     *
     * @param width the desired width
     * @return this factory for method chaining
     */
    public TextCellFactory width(int width) {
        this.width = Math.max(1, width);
        return this;
    }

    /**
     * Returns the height of a single cell.
     *
     * @return
     */
    public int height() {
        return height;
    }

    /**
     * Sets the factory's cell height to the provided value. Clamps at 1 on the
     * lower bound to ensure valid calculations.
     *
     * @param height the desired width
     * @return this factory for method chaining
     */
    public TextCellFactory height(int height) {
        this.height = Math.max(1, height);
        return this;
    }

    /**
     * Returns the current String of code points that are used for sizing the
     * cells.
     *
     * Note that this is actually a set of codepoints and treating them as an
     * array of chars might give undesired results.
     *
     * @return the String used for sizing calculations
     */
    public String fit() {
        return fitting;
    }

    /**
     * Sets the characters that will be guaranteed to fit to the provided ones.
     * This will override any previously set string.
     *
     * @param fit the String of code points to size to
     * @return this factory for method chaining
     */
    public TextCellFactory fit(String fit) {
        fitting = fit;
        bmpFont.setFixedWidthGlyphs(fitting);
        width = (int)bmpFont.getSpaceWidth();
        return this;
    }

    /**
     * Adds the code points in the string to the list of characters that will be
     * guaranteed to fit.
     *
     * @param fit the String of code points to size to
     * @return this factory for method chaining
     */
    public TextCellFactory addFit(String fit) {
        fitting += fit;
        bmpFont.setFixedWidthGlyphs(fitting);
        width = (int)bmpFont.getSpaceWidth();
        return this;
    }

    /**
     * Returns whether this factory is currently set to do antialiasing on the
     * characters rendered, which is always true.
     *
     * @return true if antialiasing is set
     */
    public boolean antialias() {
        return true;
    }

    /**
     * All fonts will be rendered with antialiasing, this doesn't do anything.
     *
     * @param antialias ignored, will always use antialiasing
     * @return this factory for method chaining
     * @deprecated AA is the wave of the future!
     */
    public TextCellFactory antialias(boolean antialias) {
        return this;
    }

    /**
     * Sets the amount of padding on all sides to the provided value.
     *
     * @param padding how much padding in pixels
     * @return this for method chaining
     */
    public TextCellFactory padding(int padding) {
        leftPadding = padding;
        rightPadding = padding;
        topPadding = padding;
        bottomPadding = padding;
        return this;
    }
    
    /**
     * Returns the padding on the left side.
     * 
     * @return amount of padding in pixels
     */
    public int leftPadding() {
        return leftPadding;
    }

    /**
     * Sets the amount of padding on the left side to the provided value.
     *
     * @param padding how much padding in pixels
     * @return this for method chaining
     */
    public TextCellFactory leftPadding(int padding) {
        leftPadding = padding;
        return this;
    }

    /**
     * Returns the padding on the right side.
     * 
     * @return amount of padding in pixels
     */
    public int rightPadding() {
        return rightPadding;
    }
    
    /**
     * Sets the amount of padding on the right side to the provided value.
     *
     * @param padding how much padding in pixels
     * @return this for method chaining
     */
    public TextCellFactory rightPadding(int padding) {
        rightPadding = padding;
        return this;
    }

    /**
     * Returns the padding on the top side.
     * 
     * @return amount of padding in pixels
     */
    public int topPadding() {
        return topPadding;
    }
    
    /**
     * Sets the amount of padding on the top side to the provided value.
     *
     * @param padding how much padding in pixels
     * @return this for method chaining
     */
    public TextCellFactory topPadding(int padding) {
        topPadding = padding;
        return this;
    }

    /**
     * Returns the padding on the bottom side.
     * 
     * @return amount of padding in pixels
     */
    public int bottomPadding() {
        return bottomPadding;
    }

    /**
     * Sets the amount of padding on the bottom side to the provided value.
     *
     * @param padding how much padding in pixels
     * @return this for method chaining
     */
    public TextCellFactory bottomPadding(int padding) {
        bottomPadding = padding;
        return this;
    }

	/**
	 * @param icc
	 *            The color center to use. Should not be {@code null}.
	 * @return {@code this}
	 * @throws NullPointerException
	 *             If {@code icc} is {@code null}.
	 */
	public TextCellFactory setColorCenter(IColorCenter<Color> icc) {
		if (icc == null)
			/* Better fail now than later */
			throw new NullPointerException(
					"The color center should not be null in " + getClass().getSimpleName());
		this.scc = icc;
		return this;
	}

    /**
     * Returns true if this factory is fully initialized and ready to build text cells.
     * 
     * @return true if initialized
     */
    public boolean initialized() {
        return initialized;
    }

    /**
     * Returns true if the given character will fit inside the current cell
     * dimensions with the current font.
     *
     * ISO Control characters, non-printing characters and invalid unicode
     * characters are all considered by definition to fit.
     *
     * @param codepoint
     * @return
     */
    public boolean willFit(int codepoint) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        
        if (!Character.isValidCodePoint(codepoint) ||
                (codepoint <= 0x001F) || (codepoint >= 0x007F && codepoint <= 0x009F)) // same as isIsoControl
        {
            return true;
        }

        return fitting.contains(String.valueOf(Character.toChars(codepoint)));
    }

    /**
     * Use the specified Batch to draw a String (often just one char long) with the default color (white), with x and y
     * determining the world-space coordinates for the upper-left corner.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param s the string to draw, often but not necessarily one char. Can be null to draw a solid block instead.
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, String s, float x, float y) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (s == null) {
            batch.draw(block, x, y - height, width, height);
        } else if(s.length() > 0 && s.charAt(0) == '\0') {
            batch.draw(block, x, y - height, width * s.length(), height);
        } else {
            bmpFont.draw(batch, s, x, y - bmpFont.getDescent(), width * s.length(), Align.center, false);
        }
    }
    /**
     * Use the specified Batch to draw a String (often just one char long) in the specified rgba color, with x and y
     * determining the world-space coordinates for the upper-left corner.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param s the string to draw, often but not necessarily one char. Can be null to draw a solid block instead.
     * @param r 0.0 to 0.1 red value
     * @param g 0.0 to 0.1 green value
     * @param b 0.0 to 0.1 blue value
     * @param a 0.0 to 0.1 alpha value
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, String s, float r, float g, float b, float a, float x, float y) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (s == null) {
            Color orig = scc.filter(batch.getColor());
            batch.setColor(r, g, b, a);
            batch.draw(block, x, y - height, width, height);
            batch.setColor(orig);
        } else if(s.length() > 0 && s.charAt(0) == '\0') {
            Color orig = scc.filter(batch.getColor());
            batch.setColor(r, g, b, a);
            batch.draw(block, x, y - height, width * s.length(), height);
            batch.setColor(orig);
        } else {
            bmpFont.setColor(r, g, b, a);
            bmpFont.draw(batch, s, x, y - bmpFont.getDescent(), width * s.length(), Align.center, false);
        }
    }

    /**
     * Use the specified Batch to draw a String (often just one char long) in the specified LibGDX Color, with x and y
     * determining the world-space coordinates for the upper-left corner.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param s the string to draw, often but not necessarily one char. Can be null to draw a solid block instead.
     * @param color the LibGDX Color to draw the char(s) with, all the same color
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, String s, Color color, float x, float y) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        bmpFont.setColor(color);

        if (s == null) {
            Color orig = scc.filter(batch.getColor());
            batch.setColor(color);
            batch.draw(block, x, y - height, width, height);
            batch.setColor(orig);
        } else if(s.length() > 0 && s.charAt(0) == '\0') {
            Color orig = scc.filter(batch.getColor());
            batch.setColor(color);
            batch.draw(block, x, y - height, width * s.length(), height);
            batch.setColor(orig);
        } else {
            bmpFont.setColor(color);
            bmpFont.draw(batch, s, x, y - bmpFont.getDescent(), width * s.length(), Align.center, false);
        }
    }

    /**
     * Use the specified Batch to draw a TextureRegion with the default tint color (white, so un-tinted), with x and y
     * determining the world-space coordinates for the upper-left corner. The TextureRegion will be stretched
     * if its size does not match what this TextCellFactory uses for width and height.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param tr the TextureRegion to draw. Can be null to draw a solid block instead.
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, TextureRegion tr, float x, float y) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (tr == null) {
            batch.draw(block, x, y - height, width, height);
        } else {
            batch.draw(tr, x, y - height, width, height);
        }
    }
    /**
     * Use the specified Batch to draw a TextureRegion tinted with the specified rgba color, with x and y
     * determining the world-space coordinates for the upper-left corner. The TextureRegion will be stretched
     * if its size does not match what this TextCellFactory uses for width and height.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param tr the TextureRegion to draw. Can be null to draw a solid block instead.
     * @param r 0.0 to 0.1 red value
     * @param g 0.0 to 0.1 green value
     * @param b 0.0 to 0.1 blue value
     * @param a 0.0 to 0.1 alpha value
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, TextureRegion tr, float r, float g, float b, float a, float x, float y) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (tr == null) {
            Color orig = scc.filter(batch.getColor());
            batch.setColor(r, g, b, a);
            batch.draw(block, x, y - height, width, height);
            batch.setColor(orig);
        } else {
            Color orig = scc.filter(batch.getColor());
            batch.setColor(r, g, b, a);
            batch.draw(tr, x, y - height, width, height);
            batch.setColor(orig);
        }
    }

    /**
     * Use the specified Batch to draw a TextureRegion tinted with the specified LibGDX Color, with x and y
     * determining the world-space coordinates for the upper-left corner. The TextureRegion will be stretched
     * if its size does not match what this TextCellFactory uses for width and height.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param tr the TextureRegion to draw. Can be null to draw a solid block instead.
     * @param color the LibGDX Color to draw the char(s) with, all the same color
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, TextureRegion tr, Color color, float x, float y) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        bmpFont.setColor(color);

        if (tr == null) {
            Color orig = scc.filter(batch.getColor());
            batch.setColor(color);
            batch.draw(block, x, y - height, width, height);
            batch.setColor(orig);
        } else {
            Color orig = scc.filter(batch.getColor());
            batch.setColor(color);
            batch.draw(tr, x, y - height, width, height);
            batch.setColor(orig);
        }
    }

    /**
     * Use the specified Batch to draw a TextureRegion with the default tint color (white, so un-tinted), with x and y
     * determining the world-space coordinates for the upper-left corner. The TextureRegion will be stretched
     * only if the supplied width and height do not match what its own dimensions are.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param tr the TextureRegion to draw. Can be null to draw a solid block instead.
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     * @param width the width of the TextureRegion or solid block in pixels.
     * @param height the height of the TextureRegion or solid block in pixels.
     */
    public void draw(Batch batch, TextureRegion tr, float x, float y, float width, float height) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (tr == null) {
            batch.draw(block, x, y - height, width, height);
        } else {
            batch.draw(tr, x, y - height, width, height);
        }
    }
    /**
     * Use the specified Batch to draw a TextureRegion tinted with the specified rgba color, with x and y
     * determining the world-space coordinates for the upper-left corner. The TextureRegion will be stretched
     * only if the supplied width and height do not match what its own dimensions are.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param tr the TextureRegion to draw. Can be null to draw a solid block instead.
     * @param r 0.0 to 0.1 red value
     * @param g 0.0 to 0.1 green value
     * @param b 0.0 to 0.1 blue value
     * @param a 0.0 to 0.1 alpha value
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     * @param width the width of the TextureRegion or solid block in pixels.
     * @param height the height of the TextureRegion or solid block in pixels.
     */
    public void draw(Batch batch, TextureRegion tr, float r, float g, float b, float a, float x, float y, float width, float height) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (tr == null) {
            Color orig = scc.filter(batch.getColor());
            batch.setColor(r, g, b, a);
            batch.draw(block, x, y - height, width, height);
            batch.setColor(orig);
        } else {
            Color orig = scc.filter(batch.getColor());
            batch.setColor(r, g, b, a);
            batch.draw(tr, x, y - height, width, height);
            batch.setColor(orig);
        }
    }

    /**
     * Use the specified Batch to draw a TextureRegion tinted with the specified LibGDX Color, with x and y
     * determining the world-space coordinates for the upper-left corner. The TextureRegion will be stretched
     * only if the supplied width and height do not match what its own dimensions are.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param tr the TextureRegion to draw. Can be null to draw a solid block instead.
     * @param color the LibGDX Color to draw the char(s) with, all the same color
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     * @param width the width of the TextureRegion or solid block in pixels.
     * @param height the height of the TextureRegion or solid block in pixels.
     */
    public void draw(Batch batch, TextureRegion tr, Color color, float x, float y, float width, float height) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }

        if (tr == null) {
            Color orig = scc.filter(batch.getColor());
            batch.setColor(color);
            batch.draw(block, x, y - height, width, height);
            batch.setColor(orig);
        } else {
            Color orig = scc.filter(batch.getColor());
            batch.setColor(color);
            batch.draw(tr, x, y - height, width, height);
            batch.setColor(orig);
        }
    }

    /**
     * Converts a String into a Label, or if the argument s is null, creates an Image of a solid block. Can be used
     * for preparing glyphs for animation effects, and is used internally for this purpose.
     * @param s a String to make into an Actor, which can be null for a solid block.
     * @param color a Color to tint s with.
     * @return the Actor, with no position set.
     */
    public Actor makeActor(String s, Color color) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (s == null) {
            Image im = new Image(block);
            im.setColor(color);
            im.setSize(width, height);
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else if(s.length() > 0 && s.charAt(0) == '\0') {
            Image im = new Image(block);
            im.setColor(color);
            im.setSize(width * s.length(), height);
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else {
            Label lb = new Label(s, new Label.LabelStyle(bmpFont, null));
            lb.setColor(color);
            // lb.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return lb;
        }
    }

    /**
     * Converts a TextureRegion into an Image, or if the argument s is null, creates an Image of a solid block. Can be
     * used for preparing images for animation effects. Stretches the TextureRegion to match a single cell's dimensions.
     * @param tr a TextureRegion to make into an Actor, which can be null for a solid block.
     * @param color a Color to tint tr with.
     * @return the Actor, with no position set.
     */
    public Actor makeActor(TextureRegion tr, Color color) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (tr == null) {
            Image im = new Image(block);
            im.setColor(color);
            im.setSize(width, height);
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else {
            Image im = new Image(tr);
            im.setColor(color);
            im.setSize(width, height);
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        }
    }

    /**
     * Converts a TextureRegion into an Image, or if the argument s is null, creates an Image of a solid block. Can be
     * used for preparing images for animation effects. Ensures the returned Image has the given width and height.
     * @param tr a TextureRegion to make into an Actor, which can be null for a solid block.
     * @param color a Color to tint tr with.
     * @return the Actor, with no position set.
     */
    public Actor makeActor(TextureRegion tr, Color color, float width, float height) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (tr == null) {
            Image im = new Image(block);
            im.setColor(color);
            im.setSize(width, height);
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else {
            Image im = new Image(tr);
            im.setColor(color);
            im.setSize(width, height);
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        }
    }

    /**
     * Returns a solid block of white, 1x1 pixel in size; can be drawn at other sizes by Batch.
     *
     * @return a white 1x1 pixel Texture.
     */
    public Texture getSolid() {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        return block;
    }

    public boolean isDistanceField() {
        return distanceField;
    }

    public float getDistanceFieldScaleX() {
        return distanceFieldScaleX;
    }
    public float getDistanceFieldScaleY() {
        return distanceFieldScaleY;
    }

    /**
     * Releases all resources of this object.
     */
    @Override
    public void dispose() {
        if(bmpFont != null) bmpFont.dispose();
        if(block != null) block.dispose();
    }
}
