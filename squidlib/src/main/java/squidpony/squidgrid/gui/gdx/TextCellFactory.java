package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import regexodus.ds.CharCharMap;
import squidpony.IColorCenter;
import squidpony.StringKit;
import squidpony.squidmath.OrderedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static squidpony.squidgrid.gui.gdx.SColor.colorFromFloat;

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
 * @author Tommy Ettinger
 */
public class TextCellFactory implements Disposable {

    /**
     * How many styles are supported by this TextCellFactory; always 1 unless changed in a subclass.
     */
    public final int STYLES = 1;
    /**
     * The commonly used symbols in roguelike games.
     */
    public static final String DEFAULT_FITTING =
            " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~",
            LINE_FITTING = StringKit.BOX_DRAWING_SINGLE,
            SQUID_FITTING = StringKit.PERMISSIBLE_CHARS;
    /**
     * The {@link AssetManager} from where to load the font. Use it to share
     * loading of a font's file across multiple factories.
     */
    protected /* Nullable */AssetManager assetManager;
    public BitmapFont bmpFont = null;
    protected Texture block = null;
    protected TextureRegion dirMarker = null;
    protected String fitting = SQUID_FITTING;
    protected IColorCenter<Color> scc;
    protected int leftPadding = 0, rightPadding = 0, topPadding = 0, bottomPadding = 0;
    protected float width = 1, height = 1;
    public float actualCellWidth = 1, actualCellHeight = 1;
    protected float distanceFieldScaleX = 36f, distanceFieldScaleY = 36f;
    protected boolean initialized = false, initializedByFont = false, initializedBySize = false;
    protected boolean distanceField = false;
    protected boolean msdf = false;
    /**
     * For distance field and MSDF fonts, this is the ShaderProgram that will produce the intended effect.
     * Usually should not be changed manually unless you know what you are doing.
     */
    public ShaderProgram shader;
    protected float smoothingMultiplier = 1.2f;
    protected float descent, lineHeight;
    protected Label.LabelStyle style;
    //protected OrderedMap<Character, Character> swap = new OrderedMap<>(32);
    protected CharCharMap swap = new CharCharMap(8);
    {
        swap.defaultReturnValue('\uffff');
    }
    protected char directionGlyph = '^';
    protected OrderedMap<Character, TextureRegion> glyphTextures = new OrderedMap<>(16);

    protected StringBuilder mut = new StringBuilder(1).append('\0');

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
        scc = DefaultResources.getSCC();
        swap.put('\u0006', ' ');
    }

    public TextCellFactory copy()
    {
        TextCellFactory next = new TextCellFactory(assetManager);
        //next.bmpFont = bmpFont;
        if(bmpFont == null)
            bmpFont = DefaultResources.getIncludedFont();
        next.bmpFont = DefaultResources.copyFont(bmpFont);
        next.block = block;
        next.swap = swap.clone(); // explicitly implemented by CharCharMap
        next.swap.defaultReturnValue('\uffff'); // ... but it forgets to copy this field
        next.distanceField = distanceField;
        next.msdf = msdf;
        next.distanceFieldScaleX = distanceFieldScaleX;
        next.distanceFieldScaleY = distanceFieldScaleY;
        next.shader = null;
        next.fitting = fitting;
        next.height = height;
        next.width = width;
        next.actualCellWidth = actualCellWidth;
        next.actualCellHeight = actualCellHeight;
        next.descent = descent;
        next.lineHeight = lineHeight;
        //next.modifiedHeight = modifiedHeight;
        next.smoothingMultiplier = smoothingMultiplier;
        next.scc = scc;
        next.directionGlyph = directionGlyph;
        if(initializedBySize)
            next.initBySize();
        else if(initializedByFont)
            next.initByFont();
        return next;
    }
    /**
     * Initializes the factory to then be able to create text cells on demand.
     * <br>
     * Will match the width and height to the space width of the font and the
     * line height of the font, respectively. Calling this after the factory
     * has already been initialized will re-initialize it. This will not work
     * with distance field or MSDF fonts; for those, use {@link #initBySize()}.
     *
     * @return this for method chaining
     */
    public TextCellFactory initByFont() {
        if(bmpFont == null)
            bmpFont = DefaultResources.getIncludedFont();
        bmpFont.setFixedWidthGlyphs(fitting);
        width = bmpFont.getSpaceWidth();
        lineHeight = bmpFont.getLineHeight();
        height = (lineHeight);
        descent = bmpFont.getDescent();

        actualCellWidth = width;
        actualCellHeight = height;
        //modifiedHeight = height;
        Pixmap temp = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        temp.setColor(Color.WHITE);
        temp.fill();
        block = new Texture(1, 1, Pixmap.Format.RGBA8888);
        block.draw(temp, 0, 0);
        temp.dispose();
        style = new Label.LabelStyle(bmpFont, null);
        BitmapFont.Glyph g = bmpFont.getData().getGlyph(directionGlyph);
        dirMarker = new TextureRegion(bmpFont.getRegion(g.page), g.srcX, g.srcY, g.width, g.height);
        initialized = true;
        initializedByFont = true;
        return this;
    }

    /**
     * Initializes the factory to then be able to create text cells on demand.
     * <br>
     * Will strictly use the provided width and height values to size the cells.
     * Calling this after the factory has already been initialized will
     * re-initialize it, but will re-create several objects (including compiling
     * a ShaderProgram, which can be very challenging for a GPU to do each frame).
     * If you need to re-initialize often to adjust size, use {@link #resetSize()},
     * which does not allocate new objects if this was already initialized, and
     * will just delegate to this method if this wasn't initialized.
     *
     * @return this for method chaining
     */
    public TextCellFactory initBySize() {
        if(bmpFont == null)
            bmpFont = DefaultResources.getIncludedFont();

        //bmpFont.setFixedWidthGlyphs(fitting);
        Pixmap temp = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        temp.setColor(Color.WHITE);
        temp.fill();
        block = new Texture(1, 1, Pixmap.Format.RGBA8888);
        block.draw(temp, 0, 0);
        temp.dispose();
        if(msdf)
        {
            bmpFont.getData().setScale(width / distanceFieldScaleX, height / distanceFieldScaleY);

            shader = new ShaderProgram(DefaultResources.vertexShader, DefaultResources.msdfFragmentShader);
            if (!shader.isCompiled()) {
                Gdx.app.error("shader", "Distance Field font shader compilation failed:\n" + shader.getLog());
            }
            //lineTweak = lineHeight / 20f;
            //distanceFieldScaleX *= (((float)width) / height) / (distanceFieldScaleX / distanceFieldScaleY);
        }
        else if(distanceField)
        {
            bmpFont.getData().setScale(width / distanceFieldScaleX, height / distanceFieldScaleY);

            shader = new ShaderProgram(DefaultResources.vertexShader, DefaultResources.fragmentShader);
            if (!shader.isCompiled()) {
                Gdx.app.error("shader", "Distance Field font shader compilation failed:\n" + shader.getLog());
            }
            //lineTweak = lineHeight / 20f;
            //distanceFieldScaleX *= (((float)width) / height) / (distanceFieldScaleX / distanceFieldScaleY);
        }
        else {
            shader = SpriteBatch.createDefaultShader();
            //lineTweak = lineHeight * 0.0625f;
        }
        lineHeight = bmpFont.getLineHeight();
        descent = bmpFont.getDescent();
        //(msdf && bmpFont.getAscent() > 0 ? -bmpFont.getDescent(): bmpFont.getDescent());
        style = new Label.LabelStyle(bmpFont, null);
        BitmapFont.Glyph g = bmpFont.getData().getGlyph(directionGlyph);
        dirMarker = new TextureRegion(bmpFont.getRegion(g.page), g.srcX, g.srcY, g.width, g.height);
        initialized = true;
        initializedBySize = true;
        return this;
    }

    /**
     * Acts like calling {@link #initBySize()}, but doesn't create new ShaderPrograms or other objects if this has
     * already been initialized. If this has not been initialized, simply returns initBySize(). This method is safe to
     * call every frame if the font size continually changes, where initBySize() is not.
     * @return this for chaining
     */
    public TextCellFactory resetSize()
    {
        return resetSize(width, height);
    }

    /**
     * Acts like calling {@link #width(float)}, {@link #height(float)}, and {@link #initBySize()} in succession, but
     * doesn't create new ShaderPrograms or other objects if this has already been initialized. If this has not been
     * initialized, calls width() and height() and then simply returns initBySize(). This method is safe to call every
     * frame if the font size continually changes, where initBySize() is not.
     * @param newWidth the new width of a single cell, as a float that usually corresponds to pixels
     * @param newHeight the new height of a single cell, as a float that usually corresponds to pixels
     * @return this for chaining
     */
    public TextCellFactory resetSize(final float newWidth, final float newHeight)
    {
        width(newWidth);
        height(newHeight);
        if(!initialized)
            return initBySize();
        if(msdf || distanceField)
        {
            bmpFont.getData().setScale(width / distanceFieldScaleX, height / distanceFieldScaleY);
        }
        lineHeight = bmpFont.getLineHeight();
        descent = bmpFont.getDescent();

        return this;
    }

    /**
     * Identical to {@link #initBySize()}.
     *
     * @see #initBySize() The docs for initBySize() apply here.
     * @return this for method chaining
     */
    public TextCellFactory initVerbatim() {
        return initBySize();
    }

    /**
     * Returns the {@link BitmapFont} used by this factory.
     *
     * @return the BitmapFont this uses
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
                bmpFont = DefaultResources.getIncludedFont();
        }
        else {
            assetManager.load(new AssetDescriptor<>(fontpath, BitmapFont.class));
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
            bmpFont = DefaultResources.getIncludedFont();
        }
        else {
            bmpFont = bitmapFont;
        }
        return this;
    }

    /**
     * Sets the font to a distance field font with the given String path to a .fnt file and String path to a texture.
     * Distance field fonts should scale cleanly to multiple resolutions without artifacts. Does not use AssetManager
     * since you shouldn't need to reload the font if it scales with one image. You need to configure the shader to use
     * distance field fonts unless a class already does this for you (SquidLayers handles shader configuration
     * internally, for example). TextCellFactory has a method, configureShader(Batch), that does this and should be
     * called while that Batch has begun rendering, typically in an override of some containing Scene2D Group's
     * draw(Batch, float) method.
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
        if (Gdx.files.internal(texturePath).exists()) {
            Gdx.app.debug("font", "Using internal font texture at " + texturePath);
            tex = new Texture(Gdx.files.internal(texturePath), false);
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } else if (Gdx.files.classpath(texturePath).exists()) {
            Gdx.app.debug("font", "Using classpath font texture at " + texturePath);
            tex = new Texture(Gdx.files.classpath(texturePath), false);
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } else {
            bmpFont = DefaultResources.getIncludedFont();
            Gdx.app.error("TextCellFactory", "Could not find font file: " + texturePath + ", using defaults");
            return this;
        }
        if (Gdx.files.internal(fontPath).exists()) {
            Gdx.app.debug("font", "Using internal font at " + fontPath);
            bmpFont = new BitmapFont(Gdx.files.internal(fontPath), new TextureRegion(tex), false);
            distanceField = true;
        } else if (Gdx.files.classpath(fontPath).exists()) {
            Gdx.app.debug("font", "Using classpath font at " + fontPath);
            bmpFont = new BitmapFont(Gdx.files.classpath(fontPath), new TextureRegion(tex), false);
            distanceField = true;
        } else {
            bmpFont = DefaultResources.getIncludedFont();
            Gdx.app.error("TextCellFactory", "Could not find font file: " + fontPath + ", using defaults");
        }
        //bmpFont.getData().padBottom = bmpFont.getDescent();
        distanceFieldScaleX = bmpFont.getSpaceWidth() - 1f;
        distanceFieldScaleY = bmpFont.getLineHeight() - 1f;
        return this;
    }
    /**
     * @param fontPath the path to a .fnt bitmap font file with multi-channel distance field effects applied, which
     *                 requires a complex process to create.
     * @param texturePath the path to the texture used by the bitmap font
     * @return this factory for method chaining
     */
    public TextCellFactory fontMultiDistanceField(String fontPath, String texturePath) {
        Texture tex;
        if (Gdx.files.internal(texturePath).exists()) {
            Gdx.app.debug("font", "Using internal font texture at " + texturePath);
            tex = new Texture(Gdx.files.internal(texturePath), true);
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } else if (Gdx.files.classpath(texturePath).exists()) {
            Gdx.app.debug("font", "Using classpath font texture at " + texturePath);
            tex = new Texture(Gdx.files.classpath(texturePath), true);
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } else {
            bmpFont = DefaultResources.getIncludedFont();
            Gdx.app.error("TextCellFactory", "Could not find font file: " + texturePath + ", using defaults");
            return this;
        }
        if (Gdx.files.internal(fontPath).exists()) {
            Gdx.app.debug("font", "Using internal font at " + fontPath);
            bmpFont = new BitmapFont(Gdx.files.internal(fontPath), new TextureRegion(tex), false);
            msdf = true;
        } else if (Gdx.files.classpath(fontPath).exists()) {
            Gdx.app.debug("font", "Using classpath font at " + fontPath);
            bmpFont = new BitmapFont(Gdx.files.classpath(fontPath), new TextureRegion(tex), false);
            msdf = true;
        } else {
            bmpFont = DefaultResources.getIncludedFont();
            Gdx.app.error("TextCellFactory", "Could not find font file: " + fontPath + ", using defaults");
        }
        //bmpFont.getData().padBottom = bmpFont.getDescent();
        if(msdf)
        {
            bmpFont.getData().setScale(0.75f, 1f);
        }
        distanceFieldScaleX = bmpFont.getSpaceWidth() - 1f;
        distanceFieldScaleY = bmpFont.getLineHeight() - 1f;
        return this;
    }
    /**
     * Sets this factory to use the one font included with libGDX, which is Arial at size 15 px. Does it correctly
     * display when used in a grid? Probably not as well as you'd hope. You should probably get some of the assets that
     * accompany SquidLib, and can be downloaded directly from GitHub (not available as one monolithic jar via Maven
     * Central, but that lets you pick and choose individual assets). Get a .fnt and its matching .png file from
     * https://github.com/SquidPony/SquidLib/tree/master/assets and you can pass them to {@link #font(String)} or
     * {@link #fontDistanceField(String, String)}.
     *
     * @return this factory for method chaining
     */
    public TextCellFactory includedFont()
    {
        bmpFont = DefaultResources.getIncludedFont();
        return this;
    }/**
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

    /**
     * Sets the TextCellFactory to use a square distance field font that will resize to whatever size you request.
     * You must configure the shader if you use a distance field font, unless a class does it for you, like SquidLayers.
     * The configureShader(Batch) method of this class can be used to set up the shader if you don't use SquidLayers;
     * see its docs for more information.
     * @return this TextCellFactory set to use a square distance field font
     */
    public TextCellFactory defaultDistanceFieldFont()
    {
        fontDistanceField(DefaultResources.distanceFieldSquare, DefaultResources.distanceFieldSquareTexture);
        return this;
    }
    /**
     * Sets the TextCellFactory to use a half-square distance field font that will resize to whatever size you request.
     * You must configure the shader if you use a distance field font, unless a class does it for you, like SquidLayers.
     * The configureShader(Batch) method of this class can be used to set up the shader if you don't use SquidLayers;
     * see its docs for more information.
     * @return this TextCellFactory set to use a half-square distance field font
     */
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
    public float width() {
        return width;
    }

    /**
     * Sets the factory's cell width to the provided value. Clamps at 1 on the
     * lower bound to ensure valid calculations.
     *
     * @param width the desired width
     * @return this factory for method chaining
     */
    public TextCellFactory width(float width) {
        this.width = Math.max(1, width);
        actualCellWidth = this.width;
        return this;
    }

    /**
     * Returns the height of a single cell.
     *
     * @return the height of a single cell
     */
    public float height() {
        return height;
    }

    /**
     * Sets the factory's cell height to the provided value. Clamps at 1 on the
     * lower bound to ensure valid calculations.
     *
     * @param height the desired width
     * @return this factory for method chaining
     */
    public TextCellFactory height(float height) {
        this.height = Math.max(1, height);
        //modifiedHeight = this.height;
        actualCellHeight = this.height;
        return this;
    }
    /**
     * Sets the factory's height used for text to the provided value, but does not change the size of a cell. Clamps at
     * 1 on the lower bound to ensure valid calculations.
     *
     * @param width the desired width
     * @return this factory for method chaining
     */
    public TextCellFactory tweakWidth(float width) {
        this.width = Math.max(1, width);
        return this;
    }

    /**
     * Sets the factory's height used for text to the provided value, but does not change the size of a cell. Clamps at
     * 1 on the lower bound to ensure valid calculations.
     *
     * @param height the desired height
     * @return this factory for method chaining
     */
    public TextCellFactory tweakHeight(float height) {
        this.height = Math.max(1, height);
        //modifiedHeight = this.height;
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
        width = bmpFont.getSpaceWidth();
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
        width = bmpFont.getSpaceWidth();
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
        scc = icc;
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
     * @param codepoint the codepoint of the char in question
     * @return true if the char will fit, or if it is non-printing in some way; false otherwise
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
    
    private char getOrDefault(final char toGet)
    {
        final char got = swap.get(toGet);
        return got == '\uffff' ? toGet : got;
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

        // + descent * 3 / 2f
        // - distanceFieldScaleY / 12f

        //height - lineTweak * 2f
        if (s == null || s.isEmpty() || s.charAt(0) == 0) {
            batch.setColor(1f,1f,1f,1f);
            batch.draw(block, x, y - actualCellHeight, actualCellWidth, actualCellHeight); // + descent * 1 / 3f
        } else {
            bmpFont.setColor(1f,1f,1f,1f);
            bmpFont.draw(batch, s, x, y - descent + 1/* * 1.5f*//* - lineHeight * 0.2f */ /* + descent*/, width, Align.center, false);
        }
    }


    /**
     * Use the specified Batch to draw a char with the default color (white), with x and y
     * determining the world-space coordinates for the upper-left corner.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param c the char to draw, often but not necessarily one char. Can be null to draw a solid block instead.
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, char c, float x, float y) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }

        // + descent * 3 / 2f
        // - distanceFieldScaleY / 12f

        //height - lineTweak * 2f
        if (c == 0) {
            batch.setColor(1f,1f,1f,1f);
            batch.draw(block, x, y - actualCellHeight, actualCellWidth, actualCellHeight); // + descent * 1 / 3f
        } else {
            bmpFont.setColor(1f,1f,1f,1f);
            mut.setCharAt(0, getOrDefault(c));
            bmpFont.draw(batch, mut, x, y - descent + 1/* * 1.5f*//* - lineHeight * 0.2f */ /* + descent*/, width, Align.center, false);
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
            float orig = batch.getPackedColor();
            batch.setColor(r, g, b, a);
            batch.draw(block, x, y - actualCellHeight, actualCellWidth, actualCellHeight); // descent * 1 / 3f
            batch.setColor(orig);
        } else if(s.length() > 0 && s.charAt(0) == '\0') {
            float orig = batch.getPackedColor();
            batch.setColor(r, g, b, a);
            batch.draw(block, x, y - actualCellHeight, actualCellWidth * s.length(), actualCellHeight); // descent * 1 / 3f
            batch.setColor(orig);
        } else {
            bmpFont.setColor(r, g, b, a);
            bmpFont.draw(batch, s, x, y - descent + 1/* * 1.5f*//* - lineHeight * 0.2f */ /* + descent*/, width, Align.center, false);
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

        if (s == null) {
            float orig = batch.getPackedColor();
            batch.setColor(color);
            batch.draw(block, x, y - actualCellHeight, actualCellWidth, actualCellHeight); // descent * 1 / 3f
            batch.setColor(orig);
        } else if(s.length() > 0 && s.charAt(0) == '\0') {
            float orig = batch.getPackedColor();
            batch.setColor(color);
            batch.draw(block, x, y - actualCellHeight, actualCellWidth * s.length(), actualCellHeight); // descent * 1 / 3f
            batch.setColor(orig);
        } else {
            bmpFont.setColor(color);
            bmpFont.draw(batch, s, x, y - descent + 1/* * 1.5f*//* - lineHeight * 0.2f */ /* + descent*/, width, Align.center, false);
        }
    }
    /**
     * Use the specified Batch to draw a String (often just one char long) in the specified LibGDX Color, with x and y
     * determining the world-space coordinates for the upper-left corner.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param s the string to draw, often but not necessarily one char. Can be null to draw a solid block instead.
     * @param encodedColor the LibGDX Color to use, converted to float as by {@link Color#toFloatBits()}
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, String s, float encodedColor, float x, float y) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }

        if (s == null) {
            float orig = batch.getPackedColor();
            batch.setColor(encodedColor);
            batch.draw(block, x, y - actualCellHeight, actualCellWidth, actualCellHeight); // descent * 1 / 3f
            batch.setColor(orig);
        } else if(s.length() > 0 && s.charAt(0) == '\0') {
            float orig = batch.getPackedColor();
            batch.setColor(encodedColor);
            batch.draw(block, x, y - actualCellHeight, actualCellWidth * s.length(), actualCellHeight); // descent * 1 / 3f
            batch.setColor(orig);
        } else
        {
            colorFromFloat(bmpFont.getColor(), encodedColor);
            bmpFont.draw(batch, s, x, y - descent + 1/* * 1.5f*//* - lineHeight * 0.2f */ /* + descent*/, width, Align.center, false);
        }
    }

    /**
     * Use the specified Batch to draw a String (often just one char long) in the specified LibGDX Color, with x and y
     * determining the world-space coordinates for the upper-left corner.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param c the char to draw, often but not necessarily one char. Can be null to draw a solid block instead.
     * @param color the LibGDX Color (or SquidLib SColor) to use, as an object
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, char c, Color color, float x, float y) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (c == 0) {
            float orig = batch.getPackedColor();
            batch.setColor(color);
            batch.draw(block, x, y - actualCellHeight, actualCellWidth, actualCellHeight); // descent * 1 / 3f
            batch.setColor(orig);
        } else
        {
            bmpFont.getColor().set(color);
            mut.setCharAt(0, getOrDefault(c));
            bmpFont.draw(batch, mut, x, y - descent + 1/* * 1.5f*//* - lineHeight * 0.2f */ /* + descent*/, width, Align.center, false);
        }
    }

    /**
     * Use the specified Batch to draw a String (often just one char long) in the specified LibGDX Color, with x and y
     * determining the world-space coordinates for the upper-left corner.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param c the char to draw, often but not necessarily one char. Can be null to draw a solid block instead.
     * @param encodedColor the LibGDX Color to use, converted to float as by {@link Color#toFloatBits()}
     * @param x x of the upper-left corner of the region of text in world coordinates.
     * @param y y of the upper-left corner of the region of text in world coordinates.
     */
    public void draw(Batch batch, char c, float encodedColor, float x, float y) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (c == 0) {
            float orig = batch.getPackedColor();
            batch.setColor(encodedColor);
            batch.draw(block, x, y - actualCellHeight, actualCellWidth, actualCellHeight); // descent * 1 / 3f
            batch.setColor(orig);
        } else
        {
            colorFromFloat(bmpFont.getColor(), encodedColor);
            mut.setCharAt(0, getOrDefault(c));
            bmpFont.draw(batch, mut, x, y - descent + 1/* * 1.5f*//* - lineHeight * 0.2f */ /* + descent*/, width, Align.center, false);
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
            batch.draw(block, x, y - height, actualCellWidth, actualCellHeight);
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
            float orig = batch.getPackedColor();
            batch.setColor(r, g, b, a);
            batch.draw(block, x, y - height, actualCellWidth, actualCellHeight);
            batch.setColor(orig);
        } else {
            float orig = batch.getPackedColor();
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

        if (tr == null) {
            float orig = batch.getPackedColor();
            batch.setColor(color);
            batch.draw(block, x, y - height, actualCellWidth, actualCellHeight);
            batch.setColor(orig);
        } else {
            float orig = batch.getPackedColor();
            batch.setColor(color);
            batch.draw(tr, x, y - height, width, height);
            batch.setColor(orig);
        }
    }
    /**
     * Use the specified Batch to draw a TextureRegion tinted with the specified encoded color as a float, with x and y
     * determining the world-space coordinates for the upper-left corner. The TextureRegion will be stretched
     * if its size does not match what this TextCellFactory uses for width and height. Colors can be converted to and
     * from floats using methods in SColor such as {@link SColor#floatGet(float, float, float, float)},
     * {@link SColor#toFloatBits()}, {@link SColor#colorFromFloat(Color, float)}, and
     * {@link SColor#lerpFloatColors(float, float, float)}.
     *
     * @param batch the LibGDX Batch to do the drawing
     * @param tr the TextureRegion to draw. Can be null to draw a solid block instead.
     * @param encodedColor the float encoding a color (as ABGR8888; SColor can produce these) to draw the image with
     * @param x x of the upper-left corner of the image in world coordinates.
     * @param y y of the upper-left corner of the image in world coordinates.
     */
    public void draw(Batch batch, TextureRegion tr, float encodedColor, float x, float y)
    {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }

        if (tr == null) {
            float orig = batch.getPackedColor();
            batch.setColor(encodedColor);
            batch.draw(block, x, y - height, actualCellWidth, actualCellHeight);
            batch.setColor(orig);
        } else {
            float orig = batch.getPackedColor();
            batch.setColor(encodedColor);
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
            float orig = batch.getPackedColor();
            batch.setColor(r, g, b, a);
            batch.draw(block, x, y - height, width, height);
            batch.setColor(orig);
        } else {
            float orig = batch.getPackedColor();
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
     * @param color the LibGDX Color to draw the image with, all the same color
     * @param x x of the upper-left corner of the image in world coordinates.
     * @param y y of the upper-left corner of the image in world coordinates.
     * @param width the width of the TextureRegion or solid block in pixels.
     * @param height the height of the TextureRegion or solid block in pixels.
     */
    public void draw(Batch batch, TextureRegion tr, Color color, float x, float y, float width, float height) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }

        if (tr == null) {
            float orig = batch.getPackedColor();
            batch.setColor(color);
            batch.draw(block, x, y - height, width, height);
            batch.setColor(orig);
        } else {
            float orig = batch.getPackedColor();
            batch.setColor(color);
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
     * @param encodedColor the float encoding a color (as ABGR8888; SColor can produce these) to draw the image with
     * @param x x of the upper-left corner of the image in world coordinates.
     * @param y y of the upper-left corner of the image in world coordinates.
     * @param width the width of the TextureRegion or solid block in pixels.
     * @param height the height of the TextureRegion or solid block in pixels.
     */
    public void draw(Batch batch, TextureRegion tr, float encodedColor, float x, float y, float width, float height) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }

        if (tr == null) {
            float orig = batch.getPackedColor();
            batch.setColor(encodedColor);
            batch.draw(block, x, y - height, width, height);
            batch.setColor(orig);
        } else {
            float orig = batch.getPackedColor();
            batch.setColor(encodedColor);
            batch.draw(tr, x, y - height, width, height);
            batch.setColor(orig);
        }
    }

    public void draw(Batch batch, float[][] encodedColors, float x, float y)
    {
        float orig = batch.getPackedColor();
        final int w = encodedColors.length, h = encodedColors[0].length;
        float wm = x, hm;
        for (int i = 0; i < w; i++, wm += actualCellWidth) {
            hm = y + (h - 1) * actualCellHeight;
            for (int j = 0; j < h; j++, hm -= actualCellHeight) {
                batch.setColor(encodedColors[i][j]);
                batch.draw(block, wm, hm, actualCellWidth, actualCellHeight); // descent * 1 / 3f
            }
        }
        batch.setColor(orig);

    }

    /**
     * Converts a String into a Label, or if the argument s is null, creates an Image of a solid block. Can be used
     * for preparing glyphs for animation effects.
     * @param s a String to make into an Actor, which can be null for a solid block.
     * @return the Actor, with no position set.
     */
    public Label makeWrappingString(String s) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (s == null) {
            s = "";
        }
        Label lb = new Label(s, style);
        lb.setWrap(true);
        // lb.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
        return lb;

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
            im.setColor(scc.filter(color));
            //im.setSize(width, height - MathUtils.ceil(bmpFont.getDescent() / 2f));
            im.setSize(actualCellWidth, actualCellHeight + (distanceField ? 1 : 0)); //  - lineHeight / actualCellHeight //+ lineTweak * 1f
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else if(s.length() > 0 && s.charAt(0) == '\0') {
            Image im = new Image(block);
            im.setColor(scc.filter(color));
            //im.setSize(width * s.length(), height - MathUtils.ceil(bmpFont.getDescent() / 2f));
            im.setSize(actualCellWidth * s.length(), actualCellHeight + (distanceField ? 1 : 0)); //   - lineHeight / actualCellHeight //+ lineTweak * 1f
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else {
            Label lb = new Label(s, style);
            //lb.setFontScale(bmpFont.getData().scaleX, bmpFont.getData().scaleY);
            lb.setSize(width * s.length(), height - descent); //+ lineTweak * 1f
            lb.setColor(scc.filter(color));
            // lb.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return lb;
        }
    }

    /**
     * Converts a String into a ColorChangeLabel, or if the argument s is null, creates a ColorChangeImage of a solid
     * block. Can be used for preparing glyphs for animation effects, and is used internally for this purpose. The
     * ColorChange classes will rotate between all colors given in the List each second, and are not affected by setColor,
     * though they are affected by their setColors methods. Their color change is not considered an animation for the
     * purposes of things like SquidPanel.hasActiveAnimations() .
     * @param s a String to make into an Actor, which can be null for a solid block.
     * @param colors a List of Color to tint s with, looping through all elements in the list each second
     * @return the Actor, with no position set.
     */
    public Actor makeActor(String s, Collection<Color> colors) {
        return makeActor(s, colors, 2f, false);
    }
    /**
     * Converts a String into a ColorChangeLabel, or if the argument s is null, creates a ColorChangeImage of a solid
     * block. Can be used for preparing glyphs for animation effects, and is used internally for this purpose. The
     * ColorChange classes will rotate between all colors given in the List each second, and are not affected by setColor,
     * though they are affected by their setColors methods. Their color change is not considered an animation for the
     * purposes of things like SquidPanel.hasActiveAnimations() .
     * @param s a String to make into an Actor, which can be null for a solid block.
     * @param colors a List of Color to tint s with, looping through all elements in the list each second
     * @param loopTime the amount of time, in seconds, to spend looping through all colors in the list
     * @return the Actor, with no position set.
     */
    public Actor makeActor(String s, Collection<Color> colors, float loopTime)
    {
        return makeActor(s, colors, loopTime, false);
    }
    /**
     * Converts a String into a ColorChangeLabel, or if the argument s is null, creates a ColorChangeImage of a solid
     * block. Can be used for preparing glyphs for animation effects, and is used internally for this purpose. The
     * ColorChange classes will rotate between all colors given in the List each second, and are not affected by setColor,
     * though they are affected by their setColors methods. Their color change is not considered an animation for the
     * purposes of things like SquidPanel.hasActiveAnimations() .
     * @param s a String to make into an Actor, which can be null for a solid block.
     * @param colors a List of Color to tint s with, looping through all elements in the list each second
     * @param loopTime the amount of time, in seconds, to spend looping through all colors in the list
     * @return the Actor, with no position set.
     */
    public Actor makeActor(String s, Collection<Color> colors, float loopTime, boolean doubleWidth) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        ArrayList<Color> colors2 = null;
        if(colors != null && !colors.isEmpty())
        {
            colors2 = new ArrayList<>(colors.size());
            for (Color c : colors) {
                colors2.add(scc.filter(c));
            }
        }
        if (s == null) {
            ColorChangeImage im = new ColorChangeImage(block, loopTime, doubleWidth, colors2);
            //im.setSize(width, height - MathUtils.ceil(bmpFont.getDescent() / 2f));
            im.setSize(actualCellWidth * (doubleWidth ? 2 : 1), actualCellHeight + (distanceField ? 1 : 0)); //  - lineHeight / actualCellHeight //+ lineTweak * 1f
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else if(s.length() > 0 && s.charAt(0) == '\0') {
            ColorChangeImage im = new ColorChangeImage(block, loopTime, doubleWidth, colors2);
            //im.setSize(width * s.length(), height - MathUtils.ceil(bmpFont.getDescent() / 2f));
            im.setSize(actualCellWidth * s.length() * (doubleWidth ? 2 : 1), actualCellHeight + (distanceField ? 1 : 0)); //   - lineHeight / actualCellHeight //+ lineTweak * 1f
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else {
            ColorChangeLabel lb = new ColorChangeLabel(s, style, loopTime, doubleWidth, colors2);
            lb.setSize(width * s.length(), height - descent); //+ lineTweak * 1f
            // lb.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return lb;
        }
    }

    /**
     * Converts a char into a Label, or if the argument c is '\0', creates an Image of a solid block. Can be used
     * for preparing glyphs for animation effects, and is used internally for this purpose.
     * @param c a char to make into an Actor, which can be the character with Unicode value 0 for a solid block.
     * @param color a Color to tint c with.
     * @return the Actor, with no position set.
     */
    public Actor makeActor(char c, Color color) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (c == 0) {
            Image im = new Image(block);
            im.setColor(scc.filter(color));
            //im.setSize(width, height - MathUtils.ceil(bmpFont.getDescent() / 2f));
            im.setSize(actualCellWidth, actualCellHeight + (distanceField ? 1 : 0)); //  - lineHeight / actualCellHeight //+ lineTweak * 1f
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else {
            mut.setCharAt(0, getOrDefault(c));
            Label lb = new Label(mut, style);
            //lb.setFontScale(bmpFont.getData().scaleX, bmpFont.getData().scaleY);
            lb.setSize(width, height - descent); //+ lineTweak * 1f
            lb.setColor(scc.filter(color));
            // lb.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return lb;
        }
    }

    /**
     * Converts a char into a Label, or if the argument c is '\0', creates an Image of a solid block. Can be used
     * for preparing glyphs for animation effects, and is used internally for this purpose. Instead of a libGDX Color
     * object, this takes an encoded float that represents a color as libGDX often does internally, ABGR-packed format.
     * You can use various methods in SColor to produce these, like {@link SColor#floatGet(float, float, float, float)}
     * or {@link Color#toFloatBits()}.
     * @param c a char to make into an Actor, which can be the character with Unicode value 0 for a solid block.
     * @param encodedColor an ABGR packed float (as produced by {@link SColor#floatGet(float, float, float, float)}) to use as c's color
     * @return the Actor, with no position set.
     */
    public Actor makeActor(char c, float encodedColor) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        if (c == 0) {
            Image im = new Image(block);
            Color.abgr8888ToColor(im.getColor(), encodedColor);
            //im.setSize(width, height - MathUtils.ceil(bmpFont.getDescent() / 2f));
            im.setSize(actualCellWidth, actualCellHeight + (distanceField ? 1 : 0)); //  - lineHeight / actualCellHeight //+ lineTweak * 1f
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else {
            mut.setCharAt(0, getOrDefault(c));
            Label lb = new Label(mut, style);
            //lb.setFontScale(bmpFont.getData().scaleX, bmpFont.getData().scaleY);
            lb.setSize(width, height - descent); //+ lineTweak * 1f
            Color.abgr8888ToColor(lb.getColor(), encodedColor);
            // lb.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return lb;
        }
    }

    /**
     * Converts a char into a ColorChangeLabel, or if the argument c is '\0', creates a ColorChangeImage of a solid
     * block. Can be used for preparing glyphs for animation effects, and is used internally for this purpose. The
     * ColorChange classes will rotate between all colors given in the List each second, and are not affected by setColor,
     * though they are affected by their setColors methods. Their color change is not considered an animation for the
     * purposes of things like SquidPanel.hasActiveAnimations() .
     * @param c a char to make into an Actor, which can be the character with Unicode value 0 for a solid block.
     * @param colors a List of Color to tint c with, looping through all elements in the list each second
     * @return the Actor, with no position set.
     */
    public Actor makeActor(char c, Collection<Color> colors) {
        return makeActor(c, colors, 2f, false);
    }
    /**
     * Converts a char into a ColorChangeLabel, or if the argument c is '\0', creates a ColorChangeImage of a solid
     * block. Can be used for preparing glyphs for animation effects, and is used internally for this purpose. The
     * ColorChange classes will rotate between all colors given in the List each second, and are not affected by setColor,
     * though they are affected by their setColors methods. Their color change is not considered an animation for the
     * purposes of things like SquidPanel.hasActiveAnimations() .
     * @param c a char to make into an Actor, which can be the character with Unicode value 0 for a solid block.
     * @param colors a List of Color to tint c with, looping through all elements in the list each second
     * @param loopTime the amount of time, in seconds, to spend looping through all colors in the list
     * @return the Actor, with no position set.
     */
    public Actor makeActor(char c, Collection<Color> colors, float loopTime)
    {
        return makeActor(c, colors, loopTime, false);
    }
    /**
     * Converts a char into a ColorChangeLabel, or if the argument c is '\0', creates a ColorChangeImage of a solid
     * block. Can be used for preparing glyphs for animation effects, and is used internally for this purpose. The
     * ColorChange classes will rotate between all colors given in the List each second, and are not affected by setColor,
     * though they are affected by their setColors methods. Their color change is not considered an animation for the
     * purposes of things like SquidPanel.hasActiveAnimations() .
     * @param c a char to make into an Actor, which can be the character with Unicode value 0 for a solid block.
     * @param colors a List of Color to tint c with, looping through all elements in the list each second
     * @param loopTime the amount of time, in seconds, to spend looping through all colors in the list
     * @return the Actor, with no position set.
     */
    public Actor makeActor(char c, Collection<Color> colors, float loopTime, boolean doubleWidth) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        ArrayList<Color> colors2 = null;
        if(colors != null && !colors.isEmpty())
        {
            colors2 = new ArrayList<>(colors.size());
            for (Color color : colors) {
                colors2.add(scc.filter(color));
            }
        }
        if (c == 0) {
            ColorChangeImage im = new ColorChangeImage(block, loopTime, doubleWidth, colors2);
            //im.setSize(width, height - MathUtils.ceil(bmpFont.getDescent() / 2f));
            im.setSize(actualCellWidth * (doubleWidth ? 2 : 1), actualCellHeight + (distanceField ? 1 : 0)); //  - lineHeight / actualCellHeight //+ lineTweak * 1f
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else {
            mut.setCharAt(0, getOrDefault(c));
            ColorChangeLabel lb = new ColorChangeLabel(mut, style, loopTime, doubleWidth, colors2);
            lb.setSize(width, height - descent); //+ lineTweak * 1f
            // lb.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return lb;
        }
    }
    public Actor makeActor(TextureRegion tr, Collection<Color> colors)
    {
        return makeActor(tr, colors, 2f, false);
    }
    public Actor makeActor(TextureRegion tr, Collection<Color> colors, float loopTime)
    {
        return makeActor(tr, colors, loopTime, false);
    }
    /**
     * Converts a TextureRegion into a ColorChangeImage that will cycle through the given colors.
     * ColorChange classes will rotate between all colors given in the List each loopTime, and are not affected by
     * setColor, though they are affected by their setColors methods. Their color change is not considered an animation
     * for the purposes of things like SquidPanel.hasActiveAnimations() .
     * @param tr a TextureRegion to make into an Actor, which can be null for a solid block.
     * @param colors a List of Color to tint c with, looping through all elements in the list each second
     * @param loopTime the amount of time, in seconds, to spend looping through all colors in the list
     * @return the Actor, with no position set.
     */
    public Actor makeActor(TextureRegion tr, Collection<Color> colors, float loopTime, boolean doubleWidth){
        return makeActor(tr, colors, loopTime, doubleWidth,
                actualCellWidth * (doubleWidth ? 2 : 1), actualCellHeight + (distanceField ? 1 : 0));
    }

    /**
     * Converts a TextureRegion into a ColorChangeImage that will cycle through the given colors.
     * ColorChange classes will rotate between all colors given in the List each loopTime, and are not affected by
     * setColor, though they are affected by their setColors methods. Their color change is not considered an animation
     * for the purposes of things like SquidPanel.hasActiveAnimations() .
     * @param tr a TextureRegion to make into an Actor, which can be null for a solid block.
     * @param colors a List of Color to tint c with, looping through all elements in the list each second
     * @param loopTime the amount of time, in seconds, to spend looping through all colors in the list
     * @return the Actor, with no position set.
     */
    public Actor makeActor(TextureRegion tr, Collection<Color> colors, float loopTime, boolean doubleWidth,
                           float width, float height){
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        ArrayList<Color> colors2 = null;
        if(colors != null && !colors.isEmpty())
        {
            colors2 = new ArrayList<>(colors.size());
            for (Color color : colors) {
                colors2.add(scc.filter(color));
            }
        }
        if (tr == null) {
            ColorChangeImage im = new ColorChangeImage(block, loopTime, doubleWidth, colors2);
            im.setSize(width, height);
            return im;
        } else {
            ColorChangeImage im = new ColorChangeImage(tr, loopTime, doubleWidth, colors2);
            im.setSize(width, height);
            return im;
        }
    }

    /**
     * Creates a ColorChangeImage Actor that should look like the glyph '^' in this font, but will be rotate-able. The
     * ColorChange classes will rotate between all colors given in the List in the given amount of loopTime, and are not
     * affected by setColor, though they are affected by their setColors methods. Their color change is not considered
     * an animation for the purposes of things like SquidPanel.hasActiveAnimations() .
     * @param colors a List of Color to tint s with, looping through all elements in the list each second
     * @param loopTime the amount of time, in seconds, to spend looping through all colors in the list
     * @return the Actor, with no position set.
     */
    public Image makeDirectionMarker(Collection<Color> colors, float loopTime, boolean doubleWidth) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        ArrayList<Color> colors2 = null;
        if (colors != null && !colors.isEmpty()) {
            colors2 = new ArrayList<>(colors.size());
            for (Color c : colors) {
                colors2.add(scc.filter(c));
            }
        }
        ColorChangeImage im = new ColorChangeImage(dirMarker, loopTime, doubleWidth,
                actualCellWidth, actualCellHeight + (distanceField ? 1 : 0), colors2);
        im.setAlign(2);
        im.setSize(actualCellWidth * (doubleWidth ? 2 : 1), actualCellHeight + (distanceField ? 1 : 0)); //  - lineHeight / actualCellHeight //+ lineTweak * 1f
        return im;
    }
    /**
     * Creates a Image Actor that should look like the glyph '^' in this font, but will be rotate-able.
     * @param color a Color to tint the '^' with
     * @return the Actor, with no position set.
     */
    public Image makeDirectionMarker(Color color) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        Image im = new Image(dirMarker);
        im.setColor(scc.filter(color));
        im.setSize(actualCellWidth, actualCellHeight + (distanceField ? 1 : 0)); //  - lineHeight / actualCellHeight //+ lineTweak * 1f
        im.setOrigin(1); //center
        return im;
    }

    public ColorChangeImage makeGlyphImage(char glyph, Color color)
    {
        return makeGlyphImage(glyph, color, false);
    }
    public ColorChangeImage makeGlyphImage(char glyph, Color color, boolean doubleWidth)
    {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        TextureRegion tr;
        if (glyphTextures.containsKey(glyph))
        {
            tr = glyphTextures.get(glyph);
        }
        else
        {
            BitmapFont.Glyph g = bmpFont.getData().getGlyph(glyph);
            tr = new TextureRegion(bmpFont.getRegion(g.page), g.srcX, g.srcY, g.width, g.height);
            glyphTextures.put(glyph, tr);
        }

        ColorChangeImage im = new ColorChangeImage(tr, 1, doubleWidth,
                actualCellWidth, actualCellHeight + (distanceField ? 1 : 0), Collections.singletonList(scc.filter(color)));
        im.setAlign(2);
        im.setSize(actualCellWidth * (doubleWidth ? 2 : 1), actualCellHeight + (distanceField ? 1 : 0)); //  - lineHeight / actualCellHeight //+ lineTweak * 1f
        return im;
    }
    public ColorChangeImage makeGlyphImage(char glyph, Collection<Color> colors, float loopTime, boolean doubleWidth) {
        if (!initialized) {
            throw new IllegalStateException("This factory has not yet been initialized!");
        }
        TextureRegion tr;
        if (glyphTextures.containsKey(glyph))
        {
            tr = glyphTextures.get(glyph);
        }
        else
        {
            BitmapFont.Glyph g = bmpFont.getData().getGlyph(glyph);
            tr = new TextureRegion(bmpFont.getRegion(g.page), g.srcX, g.srcY, g.width, g.height);
            glyphTextures.put(glyph, tr);
        }
        ArrayList<Color> colors2 = null;
        if (colors != null && !colors.isEmpty()) {
            colors2 = new ArrayList<>(colors.size());
            for (Color c : colors) {
                colors2.add(scc.filter(c));
            }
        }
        ColorChangeImage im = new ColorChangeImage(tr, loopTime, doubleWidth,
                actualCellWidth, actualCellHeight + (distanceField ? 1 : 0), colors2);
        im.setAlign(2);
        im.setSize(actualCellWidth * (doubleWidth ? 2 : 1), actualCellHeight + (distanceField ? 1 : 0)); //  - lineHeight / actualCellHeight //+ lineTweak * 1f
        return im;
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
            im.setColor(scc.filter(color));
            im.setSize(width, height);
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else {
            Image im = new Image(tr);
            im.setColor(scc.filter(color));
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
            im.setColor(scc.filter(color));
            im.setSize(width, height);
            // im.setPosition(x - width * 0.5f, y - height * 0.5f, Align.center);
            return im;
        } else {
            Image im = new Image(tr);
            im.setColor(scc.filter(color));
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
    /**
     * Gets a Glyph with the given char to show, libGDX Color, and position as x and y in world coordinates.
     * Glyph is a kind of scene2d Actor that uses this TextCellFactory to handle its rendering instead of delegating
     * that to the Label class from scene2d.ui.
     * @param shown char to show; if this is the char with codepoint 0, then this will show a solid block
     * @param color the color as a libGDX Color; can also be an SColor or some other subclass of Color
     * @param x the x position of the Glyph in world coordinates, as would be passed to draw()
     * @param y the y position of the Glyph in world coordinates, as would be passed to draw()
     * @return a new Glyph that will use the specified char, color, and position
     */

    public Glyph glyph(char shown, Color color, float x, float y)
    {
        return new Glyph(shown, color, x, y);
    }

    /**
     * Gets a Glyph with the given char to show, color as a packed float, and position as x and y in world coordinates.
     * Glyph is a kind of scene2d Actor that uses this TextCellFactory to handle its rendering instead of delegating
     * that to the Label class from scene2d.ui.
     * @param shown char to show; if this is the char with codepoint 0, then this will show a solid block
     * @param encodedColor the encoded color as a float, as produced by {@link Color#toFloatBits()}
     * @param x the x position of the Glyph in world coordinates, as would be passed to draw()
     * @param y the y position of the Glyph in world coordinates, as would be passed to draw()
     * @return a new Glyph that will use the specified char, color, and position
     */
    public Glyph glyph(char shown, float encodedColor, float x, float y)
    {
        return new Glyph(shown, encodedColor, x, y);
    }

    public boolean isMultiDistanceField() {
        return msdf;
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
     * If this uses a distance field font, the smoothing multiplier affects how crisp or blurry lines are, with higher
     * numbers generally resulting in more crisp fonts, but numbers that are too high cause jagged aliasing.
     * @return the current smoothing multiplier as a float, which starts at 1f.
     */
    public float getSmoothingMultiplier() {
        return smoothingMultiplier;
    }

    /**
     * If this uses a distance field font, the smoothing multiplier affects how crisp or blurry lines are, with higher
     * numbers generally resulting in more crisp fonts, but numbers that are too high cause jagged aliasing.
     * @param smoothingMultiplier the new value for the smoothing multiplier as a float; should be fairly close to 1f.
     * @return this for chaining
     */
    public TextCellFactory setSmoothingMultiplier(float smoothingMultiplier) {
        this.smoothingMultiplier = smoothingMultiplier;
        return this;
    }

    /**
     * If using a distance field font, you MUST call this at some point while the batch has begun, or use code that
     * calls it for you (which is now much of SquidLib). A typical point to call it is in the
     * "void draw(Batch batch, float parentAlpha)" method or an overriding method for a Scene2D class. You should call
     * configureShader rarely, typically only a few times per frame if there are no images to render, and this means the
     * logical place to call it is in the outermost Group that contains any SquidPanel objects or other widgets. If you
     * have multipleTextCellFactory objects, each one needs to have configureShader called before it is used to draw.
     * <br>
     * SquidLayers and SquidPanel already call this method in their draw overrides, so you don't need to call this
     * manually if you use SquidLayers or SquidPanel.
     * <br>
     * If you don't use a distance field font, you don't need to call this, but calling it won't cause problems.
     *
     * @param batch the Batch, such as a SpriteBatch, to configure to render distance field fonts if necessary.
     */
    public void configureShader(Batch batch) {
        if(initialized)
        {
            if(msdf)
            {
                batch.setShader(shader);
                shader.setUniformf("u_smoothing", 0.35f / (3.5f * smoothingMultiplier * bmpFont.getData().scaleX));
            }
            else if (distanceField) {
                batch.setShader(shader);
                shader.setUniformf("u_smoothing", 0.35f / (1.9f * smoothingMultiplier * (bmpFont.getData().scaleX + bmpFont.getData().scaleY)));
            }
        }
    }
    /**
     * Releases all resources of this object.
     */
    @Override
    public void dispose() {
        if(bmpFont != null) bmpFont.dispose();
        if(block != null) block.dispose();
    }

    /**
     * Gets the descent of this TextCellFactory's BitmapFont, which may be useful for layout outside this class.
     * @return the descent of the BitmapFont this object uses
     */
    public float getDescent() {
        return descent;
    }

    public char getDirectionGlyph() {
        return directionGlyph;
    }

    public TextCellFactory setDirectionGlyph(char directionGlyph) {
        this.directionGlyph = directionGlyph;
        return this;
    }

    /**
     * Adds a pair of Strings (typically both with length 1) as a replacement pair, so when the find String is requested
     * to be drawn, the replace String is used instead. Swaps are used when drawing text in each cell in SquidPanel and
     * related classes, so Strings longer than 1 char are effectively disregarded beyond the first char.
     * <br>
     * This can be useful when you want to use certain defaults in squidlib-util's dungeon generation, like '~' for deep
     * water, but not others, like ',' for shallow water, and would rather have a glyph of your choice replace something
     * that would be drawn. Replacements will not be chained; that is, if you {@code addSwap("^", ",")} and also
     * {@code addSwap(",", ":")}, then a requested '^' will be drawn as ',', not ':', but a requested ',' will be drawn
     * as ':' (only one swap will be performed). Typically you want a different TextCellFactory for UI elements that use
     * swapping, like a top-down char-based map, and elements that should not, like those that display normal text.
     * @param find the requested String that will be changed
     * @param replace the replacement String that will be used in place of find
     * @return this for chaining
     */
    public TextCellFactory addSwap(String find, String replace)
    {
        if(find == null || replace == null || find.isEmpty()  || replace.isEmpty() || find.charAt(0) == 0)
            return this;
        swap.put(find.charAt(0), replace.charAt(0));
        return this;
    }

    /**
     * Adds a pair of chars as a replacement pair, so when the find char is requested to be drawn, the replace char is
     * used instead.
     * <br>
     * This can be useful when you want to use certain defaults in squidlib-util's dungeon generation, like '~' for deep
     * water, but not others, like ',' for shallow water, and would rather have a glyph of your choice replace something
     * that would be drawn. Replacements will not be chained; that is, if you {@code addSwap('^', ',')} and also
     * {@code addSwap(',', ':')}, then a requested '^' will be drawn as ',', not ':', but a requested ',' will be drawn
     * as ':' (only one swap will be performed). Typically you want a different TextCellFactory for UI elements that use
     * swapping, like a top-down char-based map, and elements that should not, like those that display normal text.
     * @param find the requested char that will be changed (converted to a length-1 String)
     * @param replace the replacement char that will be used in place of find (converted to a length-1 String)
     * @return this for chaining
     */
    public TextCellFactory addSwap(char find, char replace)
    {
        if(find == 0)
            return this;
        swap.put(find, replace);
        return this;
    }

    /**
     * Removes the replacement pair, if present, that searches for the given key, find. Swaps are used when drawing text
     * in each cell in SquidPanel and related classes, so Strings longer than 1 char are effectively disregarded beyond
     * the first char.
     * <br>
     * This can be useful when you want to use certain defaults in squidlib-util's dungeon generation, like '~' for deep
     * water, but not others, like ',' for shallow water, and would rather have a glyph of your choice replace something
     * that would be drawn. Replacements will not be chained; that is, if you {@code addSwap('^', ',')} and also
     * {@code addSwap(',', ':')}, then a requested '^' will be drawn as ',', not ':', but a requested ',' will be drawn
     * as ':' (only one swap will be performed). Typically you want a different TextCellFactory for UI elements that use
     * swapping, like a top-down char-based map, and elements that should not, like those that display normal text.
     * @param find the String that would be changed in the replacement pair
     * @return this for chaining
     */
    public TextCellFactory removeSwap(String find)
    {

        if(find != null && !find.isEmpty() && find.charAt(0) != 0)
            swap.remove(find.charAt(0));
        return this;
    }

    /**
     * Removes the replacement pair, if present, that searches for the given key, find.
     * <br>
     * This can be useful when you want to use certain defaults in squidlib-util's dungeon generation, like '~' for deep
     * water, but not others, like ',' for shallow water, and would rather have a glyph of your choice replace something
     * that would be drawn. Replacements will not be chained; that is, if you {@code addSwap('^', ',')} and also
     * {@code addSwap(',', ':')}, then a requested '^' will be drawn as ',', not ':', but a requested ',' will be drawn
     * as ':' (only one swap will be performed). Typically you want a different TextCellFactory for UI elements that use
     * swapping, like a top-down char-based map, and elements that should not, like those that display normal text.
     * @return this for chaining
     */
    public TextCellFactory removeSwap(char find)
    {
        swap.remove(find);
        return this;
    }

    /**
     * Gets the current mapping of "swaps", or replacement pairs, to replace keys requested for drawing with their
     * values in the {@link CharCharMap}. CharCharMap is a class from RegExodus (a dependency of squidlib-util that is
     * used for text matching and Unicode support), which is used here to avoid making yet another primitive-backed
     * collection class.
     * <br>
     * This can be useful when you want to use certain defaults in squidlib-util's dungeon generation, like '~' for deep
     * water, but not others, like ',' for shallow water, and would rather have a glyph of your choice replace something
     * that would be drawn. Replacements will not be chained; that is, if you {@code addSwap('^', ',')} and also
     * {@code addSwap(',', ':')}, then a requested '^' will be drawn as ',', not ':', but a requested ',' will be drawn
     * as ':' (only one swap will be performed). Typically you want a different TextCellFactory for UI elements that use
     * swapping, like a top-down char-based map, and elements that should not, like those that display normal text.
     * @return the mapping of replacement pairs
     */
    public CharCharMap getAllSwaps() {
        return swap;
    }

    /**
     * Sets the mapping of replacement pairs to a different one as a Map of Character keys to String values.
     * <br>
     * This can be useful when you want to use certain defaults in squidlib-util's dungeon generation, like '~' for deep
     * water, but not others, like ',' for shallow water, and would rather have a glyph of your choice replace something
     * that would be drawn. Replacements will not be chained; that is, if you {@code addSwap('^', ',')} and also
     * {@code addSwap(',', ':')}, then a requested '^' will be drawn as ',', not ':', but a requested ',' will be drawn
     * as ':' (only one swap will be performed). Typically you want a different TextCellFactory for UI elements that use
     * swapping, like a top-down char-based map, and elements that should not, like those that display normal text.
     * @param swaps the Map of replacement pairs; keys requested for drawing will be replaced with their values
     * @return this for chaining
     */
    public TextCellFactory setAllSwaps(OrderedMap<Character, Character> swaps) {
        swap.clear();
        for (int i = 0; i < swaps.size(); i++) {
            if(!swaps.keyAt(i).equals('\0'))
                swap.put(swaps.keyAt(i), swaps.getAt(i));
        }
        return this;
    }

    /**
     * Appends to the mapping of replacement pairs, adding or replacing any entries in the current mapping with the
     * entries in a Map of Character keys to String values.
     * <br>
     * This can be useful when you want to use certain defaults in squidlib-util's dungeon generation, like '~' for deep
     * water, but not others, like ',' for shallow water, and would rather have a glyph of your choice replace something
     * that would be drawn. Replacements will not be chained; that is, if you {@code addSwap('^', ',')} and also
     * {@code addSwap(',', ':')}, then a requested '^' will be drawn as ',', not ':', but a requested ',' will be drawn
     * as ':' (only one swap will be performed). Typically you want a different TextCellFactory for UI elements that use
     * swapping, like a top-down char-based map, and elements that should not, like those that display normal text.
     * @param swaps the Map of replacement pairs to add; keys requested for drawing will be replaced with their values
     * @return this for chaining
     */
    public TextCellFactory addSwaps(OrderedMap<Character, Character> swaps) {
        for (int i = 0; i < swaps.size(); i++) {
            if(!swaps.keyAt(i).equals('\0'))
                swap.put(swaps.keyAt(i), swaps.getAt(i));
        }
        return this;
    }

    /**
     * Clears all replacement pairs this has been told to swap.
     * <br>
     * This can be useful when you want to use certain defaults in squidlib-util's dungeon generation, like '~' for deep
     * water, but not others, like ',' for shallow water, and would rather have a glyph of your choice replace something
     * that would be drawn. Replacements will not be chained; that is, if you {@code addSwap('^', ',')} and also
     * {@code addSwap(',', ':')}, then a requested '^' will be drawn as ',', not ':', but a requested ',' will be drawn
     * as ':' (only one swap will be performed). Typically you want a different TextCellFactory for UI elements that use
     * swapping, like a top-down char-based map, and elements that should not, like those that display normal text.
     * @return this for chaining
     */
    public TextCellFactory clearSwaps()
    {
        swap.clear();
        return this;
    }

    /**
     * Not implemented in this class; in subclasses that do, this should change the currently-used font style, such as
     * from regular to italic or bold. Calling this method on a TextCellFactory does nothing, but won't cause problems.
     * @param style an int, typically a constant in a class that implements this, that determines what style to use.
     */
    public void setStyle(int style)
    {
    }

    public class Glyph extends Actor
    {
        public char shown;

        public Glyph() {
            this('@', SColor.SAFETY_ORANGE.toFloatBits(), 0f, 0f);
        }
        public Glyph(char shown, Color color, float x, float y)
        {
            super();
            this.shown = shown;
            super.getColor().set(color);
            setPosition(x, y);
        }
        public Glyph(char shown, float color, float x, float y) {
            super();
            this.shown = shown;
            Color.abgr8888ToColor(super.getColor(), color);
            setPosition(x, y);
        }

        public float getPackedColor() {
            return getColor().toFloatBits();
        }

        public void setPackedColor(float color) {
            Color.abgr8888ToColor(super.getColor(), color);
        }

        @Override
        public String toString() {
            return "Glyph{'" +
                    + shown +
                    "' with color " + getColor() +
                    ", position (" + getX() +
                    "," + getY() +
                    ")}";
        }

        /**
         * Draws the actor. The batch is configured to draw in the parent's coordinate system.
         * {@link Batch#draw(TextureRegion, float, float, float, float, float, float, float, float, float)
         * This draw method} is convenient to draw a rotated and scaled TextureRegion. {@link Batch#begin()} has already been called on
         * the batch. If {@link Batch#end()} is called to draw without the batch then {@link Batch#begin()} must be called before the
         * method returns.
         *
         * @param batch the batch should be between begin() and end(), usually handled by Stage
         * @param parentAlpha Should be multiplied with the actor's alpha, allowing a parent's alpha to affect all children.
         */
        @Override
        public void draw(Batch batch, float parentAlpha) {
            TextCellFactory.this.draw(batch, shown, getColor(), getX(), getY() + 1);
        }
    }
}