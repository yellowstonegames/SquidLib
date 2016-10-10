package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import squidpony.squidmath.StatefulRNG;

/**
 * Default BitmapFonts, a sample image, "stretchable" fonts and icons, and a central RNG for use with LibGDX.
 * All but one of the resources here need to be downloaded separately, to avoid bloating the size of the dependency jar
 * with many fonts that a game probably won't use. You can download assets in a jar with a release of SquidLib, or
 * individually from https://github.com/SquidPony/SquidLib/tree/master/assets . The assets you choose should go in the
 * android/assets (core/assets if you don't have an Android project) subfolder of your game if you use the normal libGDX
 * setup tool or SquidSetup, or the assets subfolder if you use czyzby's alternate setup tool,
 * https://github.com/czyzby/gdx-setup . They should not be placed in subdirectories of assets, otherwise this won't be
 * able to find them.
 * <br>
 * The fonts provided are mostly monospaced, with most looking rather similar (straight orthogonal lines and right-angle
 * curves), but some that look... better than the rest. For monospaced fonts that could be used on a grid in SquidPanel
 * or SquidLayers, one good choice is Inconsolata-LGC, accessible by getSmoothFont(), getLargeSmoothFont(), (ideally for
 * narrow glyphs) getStretchableFont(), or (ideally for square glyphs) getStretchableSquareFont(). Another is Computer
 * Modern, accessible by getStretchableTypewriterFont(), which has a chunky, ornamented style. For variable-width fonts
 * that can be used in TextPanel and LinesPanel, among others, you can use the high-quality serif font Gentium,
 * accessible by getStretchablePrintFont(), or Noto Sans, a clean sans font accessible by getStretchableCleanFont(). All
 * of the fonts so far support the Latin, Greek, and Cyrillic alphabets.
 * <br>
 * If you can't decide, go with getStretchableFont() or getStretchableSquareFont(), which return TextCellFactory
 * objects, and call their .width(int), .height(int), and .initBySize() methods to make them the size (and aspect ratio)
 * you want. You can use getStretchableFont() or getStretchableTypewriterFont() with the same width and height to make
 * a horizontally-stretched version of a square font instead of the existing square fonts that add blank space.
 * <br>
 * The most Latin script support for a monospaced font is in the font Mandrill, accessible by getDefaultUnicodeFont()
 * and getLargeUnicodeFont() in two different sizes, and the latter should be suitable for everything from Spanish and
 * French, to Polish to Vietnamese. You can use Gentium for even better language support with a variable-width font in
 * LinesPanel or the like; it is accessible with getStretchablePrintFont(). Google's Noto font also supports many glyphs
 * in a variable-width format; it is accessible with getStretchableCleanFont() but may have some issues with baseline
 * level changing and character-to-character alignment seeming too high or low.
 * <br>
 * The sample image is a tentacle taken from a public domain icon collection graciously released by Henrique Lazarini;
 * it's fitting for SquidLib to have a tentacle as a logo or something, I guess?
 * <br>
 * The icons are from http://game-icons.net , there are over 2000 of them, and they're among the only images that should
 * both be recolor-able easily (like chars in a font) and resize-able easily if you use a stretchable font at the same
 * time. The stretchable font TextCellFactories do some work with shaders that makes most images look incorrect, but
 * allow distance field fonts and these distance field icons to resize smoothly. Mixing stretchable fonts with
 * non-stretchable images or fonts is not a great idea, and usually involves a slow shader change back and forth.
 * <br>
 * You can get a default RNG with getGuiRandom(); this should probably not be reused for non-GUI-related randomness,
 * but is meant instead to be used wherever randomized purely-aesthetic effects are needed, such as a jiggling effect.
 * Created by Tommy Ettinger on 7/11/2015.
 */
public class DefaultResources implements LifecycleListener {
    private BitmapFont narrow1 = null, narrow2 = null, narrow3 = null,
            smooth1 = null, smooth2 = null, smoothSquare = null, smoothSquareOld = null,
            square1 = null, square2 = null,
            unicode1 = null, unicode2 = null, arial15 = null;

    private TextCellFactory distanceNarrow = null, distanceSquare = null, typewriterDistanceNarrow = null,
            distancePrint = null, distanceClean = null;
    private TextureAtlas iconAtlas = null;
    public static final String squareName = "Zodiac-Square-12x12.fnt",
            narrowName = "Rogue-Zodiac-6x12.fnt",
            unicodeName = "Mandrill-6x16.fnt",
            squareNameLarge = "Zodiac-Square-24x24.fnt",
            narrowNameLarge = "Rogue-Zodiac-12x24.fnt",
            unicodeNameLarge = "Mandrill-12x32.fnt",
            narrowNameExtraLarge = "Rogue-Zodiac-18x36.fnt",
            smoothName = "Inconsolata-LGC-8x18.fnt",
            smoothNameLarge = "Inconsolata-LGC-12x24.fnt",
            distanceFieldSquare = "Inconsolata-LGC-Square-distance.fnt",
            distanceFieldSquareTexture = "Inconsolata-LGC-Square-distance.png",
            distanceFieldNarrow = "Inconsolata-LGC-Custom-distance.fnt",
            distanceFieldNarrowTexture = "Inconsolata-LGC-Custom-distance.png",
            distanceFieldPrint = "Gentium-distance.fnt",
            distanceFieldPrintTexture = "Gentium-distance.png",
            distanceFieldClean = "Noto-Sans-distance.fnt",
            distanceFieldCleanTexture = "Noto-Sans-distance.png",
            distanceFieldTypewriterNarrow = "CM-Custom-distance.fnt",
            distanceFieldTypewriterNarrowTexture = "CM-Custom-distance.png";
    public static String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
            + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
            + "uniform mat4 u_projTrans;\n"
            + "varying vec4 v_color;\n"
            + "varying vec2 v_texCoords;\n"
            + "\n"
            + "void main() {\n"
            + "	v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
            + "	v_color.a = v_color.a * (255.0/254.0);\n"
            + "	v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
            + "	gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
            + "}\n";

    public static String fragmentShader = "#ifdef GL_ES\n"
            + "	precision mediump float;\n"
            + "	precision mediump int;\n"
            + "#endif\n"
            + "\n"
            + "uniform sampler2D u_texture;\n"
            + "uniform float u_smoothing;\n"
            + "varying vec4 v_color;\n"
            + "varying vec2 v_texCoords;\n"
            + "\n"
            + "void main() {\n"
            + "	if (u_smoothing > 0.0) {\n"
            + "		float smoothing = 0.25 / u_smoothing;\n"
            + "		float distance = texture2D(u_texture, v_texCoords).a;\n"
            + "		float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);\n"
            + "		gl_FragColor = vec4(v_color.rgb, alpha * v_color.a);\n"
            + "	} else {\n"
            + "		gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n"
            + "	}\n"
            + "}\n";
    private SquidColorCenter scc = null;
    private Texture tentacle = null;
    private TextureRegion tentacleRegion = null;
    private StatefulRNG guiRandom;

    private static DefaultResources instance = null;

    private DefaultResources()
    {
        if(Gdx.app == null)
            throw new IllegalStateException("Gdx.app cannot be null; initialize GUI-using objects in create() or later.");
        Gdx.app.addLifecycleListener(this);
    }

    private static void initialize()
    {
        if(instance == null)
            instance = new DefaultResources();
    }

    /**
     * Gets the one font guaranteed to be included in libGDX, which is Arial at size 15 px.
     * @return the BitmapFont representing Arial.ttf at size 15 px
     */
    public static BitmapFont getIncludedFont()
    {
        initialize();
        if(instance.arial15 == null)
        {
            try {
                instance.arial15 = new BitmapFont();
                //instance.narrow1.getData().padBottom = instance.narrow1.getDescent();
            } catch (Exception e) {
            }
        }
        return instance.arial15;
    }
    /**
     * Returns a 12x12px, stretched but curvaceous font as an embedded resource. Caches it for later calls.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Zodiac-Square-12x12.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Zodiac-Square-12x12.png</li>
     * </ul>
     * @return the BitmapFont object representing Zodiac-Square.ttf at size 16 pt
     */
    public static BitmapFont getDefaultFont()
    {
        initialize();
        if(instance.square1 == null)
        {
            try {
                instance.square1 = new BitmapFont(Gdx.files.internal("Zodiac-Square-12x12.fnt"), Gdx.files.internal("Zodiac-Square-12x12.png"), false);
                //instance.square1.getData().padBottom = instance.square1.getDescent();
            } catch (Exception e) {
            }
        }
        return instance.square1;
    }
    /**
     * Returns a 24x24px, stretched but curvaceous font as an embedded resource. Caches it for later calls.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Zodiac-Square-24x24.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Zodiac-Square-24x24.png</li>
     * </ul>
     * @return the BitmapFont object representing Zodiac-Square.ttf at size 32 pt
     */
    public static BitmapFont getLargeFont()
    {
        initialize();
        if(instance.square2 == null)
        {
            try {
                instance.square2 = new BitmapFont(Gdx.files.internal("Zodiac-Square-24x24.fnt"), Gdx.files.internal("Zodiac-Square-24x24.png"), false);
                //instance.square2.getData().padBottom = instance.square2.getDescent();
            } catch (Exception e) {
            }
        }
        return instance.square2;
    }
    /**
     * Returns a 6x12px, narrow and curving font as an embedded resource. Caches it for later calls.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Rogue-Zodiac-6x12.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Rogue-Zodiac-6x12_0.png</li>
     * </ul>
     * @return the BitmapFont object representing Rogue-Zodiac.ttf at size 16 pt
     */
    public static BitmapFont getDefaultNarrowFont()
    {
        initialize();
        if(instance.narrow1 == null)
        {
            try {
                instance.narrow1 = new BitmapFont(Gdx.files.internal("Rogue-Zodiac-6x12.fnt"), Gdx.files.internal("Rogue-Zodiac-6x12_0.png"), false);
                //instance.narrow1.getData().padBottom = instance.narrow1.getDescent();
            } catch (Exception e) {
            }
        }
        return instance.narrow1;
    }

    /**
     * Returns a 12x24px, narrow and curving font as an embedded resource. Caches it for later calls.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Rogue-Zodiac-12x24.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Rogue-Zodiac-12x24_0.png</li>
     * </ul>
     * @return the BitmapFont object representing Rogue-Zodiac.ttf at size 32 pt
     */
    public static BitmapFont getLargeNarrowFont()
    {
        initialize();
        if(instance.narrow2 == null)
        {
            try {
                instance.narrow2 = new BitmapFont(Gdx.files.internal("Rogue-Zodiac-12x24.fnt"), Gdx.files.internal("Rogue-Zodiac-12x24_0.png"), false);
                //instance.narrow2.getData().padBottom = instance.narrow2.getDescent();
            } catch (Exception e) {
            }
        }
        return instance.narrow2;
    }
    /**
     * Returns a 12x24px, narrow and curving font as an embedded resource. Caches it for later calls.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Rogue-Zodiac-18x36.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Rogue-Zodiac-18x36_0.png</li>
     * </ul>
     * @return the BitmapFont object representing Rogue-Zodiac.ttf at size 32 pt
     */
    public static BitmapFont getExtraLargeNarrowFont()
    {
        initialize();
        if(instance.narrow3 == null)
        {
            try {
                instance.narrow3 = new BitmapFont(Gdx.files.internal("Rogue-Zodiac-18x36.fnt"), Gdx.files.internal("Rogue-Zodiac-18x36_0.png"), false);
                //instance.narrow3.getData().padBottom = instance.narrow3.getDescent();
            } catch (Exception e) {
            }
        }
        return instance.narrow3;
    }

    /**
     * Returns a 8x18px, very smooth and generally good-looking font (based on Inconsolata) as an embedded resource.
     * This font fully supports Latin, Greek, Cyrillic, and of particular interest to SquidLib, Box Drawing characters.
     * Caches the font for later calls.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-8x18.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-8x18.png</li>
     * </ul>
     * @return the BitmapFont object representing Inconsolata-LGC.ttf at size... pretty sure it's 8x18 pixels
     */
    public static BitmapFont getSmoothFont()
    {
        initialize();
        if(instance.smooth1 == null)
        {
            try {
                instance.smooth1 = new BitmapFont(Gdx.files.internal("Inconsolata-LGC-8x18.fnt"), Gdx.files.internal("Inconsolata-LGC-8x18.png"), false);
                //instance.smooth1.getData().padBottom = instance.smooth1.getDescent();
            } catch (Exception e) {
            }
        }
        return instance.smooth1;
    }
    /**
     * Returns a 12x24px, very smooth and generally good-looking font (based on Inconsolata) as an embedded resource.
     * This font fully supports Latin, Greek, Cyrillic, and of particular interest to SquidLib, Box Drawing characters.
     * Caches the font for later calls.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-12x24.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-12x24.png</li>
     * </ul>
     * @return the BitmapFont object representing Inconsolata-LGC.ttf at size... not actually sure, 12x24 pixels
     */
    public static BitmapFont getLargeSmoothFont()
    {
        initialize();
        if(instance.smooth2 == null)
        {
            try {
                instance.smooth2 = new BitmapFont(Gdx.files.internal("Inconsolata-LGC-12x24.fnt"), Gdx.files.internal("Inconsolata-LGC-12x24.png"), false);
                //instance.smooth2.getData().padBottom = instance.smooth2.getDescent();
            } catch (Exception e) {
            }
        }
        return instance.smooth2;
    }
    /**
     * Returns a 6x16px, narrow and curving font with a lot of unicode chars as an embedded resource. Caches it for later calls.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Mandrill-6x16.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Mandrill-6x16.png</li>
     * </ul>
     * @return the BitmapFont object representing Mandrill.ttf at size 16 pt
     */
    public static BitmapFont getDefaultUnicodeFont()
    {
        initialize();
        if(instance.unicode1 == null)
        {
            try {
                instance.unicode1 = new BitmapFont(Gdx.files.internal("Mandrill-6x16.fnt"), Gdx.files.internal("Mandrill-6x16.png"), false);
                //instance.unicode1.getData().padBottom = instance.unicode1.getDescent();
            } catch (Exception e) {
            }
        }
        return instance.unicode1;
    }

    /**
     * Returns a 12x32px, narrow and curving font with a lot of unicode chars as an embedded resource. Caches it for later calls.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Mandrill-12x32.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Mandrill-12x32.png</li>
     * </ul>
     * @return the BitmapFont object representing Mandrill.ttf at size 32 pt
     */
    public static BitmapFont getLargeUnicodeFont()
    {
        initialize();
        if(instance.unicode2 == null)
        {
            try {
                instance.unicode2 = new BitmapFont(Gdx.files.internal("Mandrill-12x32.fnt"), Gdx.files.internal("Mandrill-12x32.png"), false);
                //instance.unicode2.getData().padBottom = instance.unicode2.getDescent();
            } catch (Exception e) {
            }
        }
        return instance.unicode2;
    }
    /**
     * Returns a 25x25px, very smooth and generally good-looking font (based on Inconsolata) as an embedded resource.
     * This font fully supports Latin, Greek, Cyrillic, and of particular interest to SquidLib, Box Drawing characters.
     * This variant is (almost) perfectly square, and box drawing characters should line up at size 25x25 px, but other
     * glyphs will have much more horizontal spacing than in other fonts. Caches the font for later calls.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Square-25x25.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Square-25x25.png</li>
     * </ul>
     * @return the BitmapFont object representing Inconsolata-LGC-Square at size 25x25 pixels
     */
    public static BitmapFont getSquareSmoothFont()
    {
        initialize();
        if(instance.smoothSquare == null)
        {
            try {
                instance.smoothSquare = new BitmapFont(Gdx.files.internal("Inconsolata-LGC-Square-25x25.fnt"), Gdx.files.internal("Inconsolata-LGC-Square-25x25.png"), false);
                //instance.smoothSquare.getData().padBottom = instance.smoothSquare.getDescent();
            } catch (Exception e) {
            }
        }
        return instance.smoothSquare;
    }

    /**
     * NOTE: May have issues with transparency. Prefer using distance field fonts with getStretchableSquareFont() if
     * possible, or getSquareSmoothFont() for a larger square BitmapFont. Returns a 20x20px, very smooth and generally
     * good-looking font (based on Inconsolata) as an embedded resource. This font fully supports Latin, Greek,
     * Cyrillic, and of particular interest to SquidLib, Box Drawing characters. This variant is (almost) perfectly
     * square, and box drawing characters should line up at size 20x20 px, but other glyphs will have much more
     * horizontal spacing than in other fonts. Caches the font for later calls.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Square.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Square.png</li>
     * </ul>
     * @return the BitmapFont object representing Inconsolata-LGC-Square at size 20x20 pixels
     */
    public static BitmapFont getSquareSmoothMediumFont()
    {
        initialize();
        if(instance.smoothSquareOld == null)
        {
            try {
                instance.smoothSquareOld = new BitmapFont(Gdx.files.internal("Inconsolata-LGC-Square.fnt"), Gdx.files.internal("Inconsolata-LGC-Square.png"), false);
                //instance.smoothSquareOld.getData().padBottom = instance.smoothSquareOld.getDescent();
            } catch (Exception e) {
            }
        }
        return instance.smoothSquareOld;
    }

    /**
     * Returns a TextCellFactory already configured to use a square font that should scale cleanly to many sizes. Caches
     * the result for later calls.
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Square-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Square-distance.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the square font Inconsolata-LGC-Square.ttf
     */
    public static TextCellFactory getStretchableSquareFont()
    {
        initialize();
        if(instance.distanceSquare == null)
        {
            try {
                instance.distanceSquare = new TextCellFactory().defaultDistanceFieldFont();
            } catch (Exception e) {
            }
        }
        if(instance.distanceSquare != null)
            return instance.distanceSquare.copy();
        return null;
    }
    /**
     * Returns a TextCellFactory already configured to use a narrow font (twice as tall as it is wide) that should scale
     * cleanly to many sizes. Caches the result for later calls.
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Custom-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Inconsolata-LGC-Custom-distance.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font Inconsolata-LGC-Custom.ttf
     */
    public static TextCellFactory getStretchableFont()
    {
        initialize();
        if(instance.distanceNarrow == null)
        {
            try {
                instance.distanceNarrow = new TextCellFactory().defaultNarrowDistanceFieldFont();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.distanceNarrow != null)
            return instance.distanceNarrow.copy();
        return null;

    }

    /**
     * Returns a TextCellFactory already configured to use a narrow typewriter-style serif font that should scale
     * cleanly to many sizes. Caches the result for later calls.
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/CM-Custom-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/CM-Custom-distance.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font CM-Custom.ttf
     */
    public static TextCellFactory getStretchableTypewriterFont()
    {
        initialize();
        if(instance.typewriterDistanceNarrow == null)
        {
            try {
                instance.typewriterDistanceNarrow = new TextCellFactory()
                        .fontDistanceField(distanceFieldTypewriterNarrow, distanceFieldTypewriterNarrowTexture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.typewriterDistanceNarrow != null)
            return instance.typewriterDistanceNarrow.copy();
        return null;
    }

    /**
     * Returns a TextCellFactory already configured to use a variable-width serif font that should look like the serif
     * fonts used in many novels' main texts, and that should scale cleanly to many sizes. Meant to be used in variable-
     * width displays like TextPanel. Caches the result for later calls.
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Gentium-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Gentium-distance.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font Gentium, by SIL
     */
    public static TextCellFactory getStretchablePrintFont() {
        initialize();
        if (instance.distancePrint == null) {
            try {
                instance.distancePrint = new TextCellFactory().fontDistanceField(distanceFieldPrint, distanceFieldPrintTexture)
                        .setSmoothingMultiplier(0.4f).height(30).width(7);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.distancePrint != null)
            return instance.distancePrint.copy();
        return null;
    }

    /**
     * Returns a TextCellFactory already configured to use a variable-width sans-serif font that currently looks
     * slightly jumbled without certain layout features. Meant to be used in variable-width displays like TextPanel, but
     * currently you should prefer getStretchablePrintFont() for legibility. Caches the result for later calls.
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Noto-Sans-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Noto-Sans-distance.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font Noto Sans, by Google
     */
    public static TextCellFactory getStretchableCleanFont() {
        initialize();
        if (instance.distanceClean == null) {
            try {
                instance.distanceClean = new TextCellFactory().fontDistanceField(distanceFieldClean, distanceFieldCleanTexture)
                        .setSmoothingMultiplier(0.4f).height(30).width(7);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.distanceClean != null)
            return instance.distanceClean.copy();
        return null;
    }

    /**
     * Gets an image of a (squid-like, for SquidLib) tentacle, 32x32px.
     * Source is public domain: http://opengameart.org/content/496-pixel-art-icons-for-medievalfantasy-rpg
     * Created by Henrique Lazarini (7Soul1, http://7soul1.deviantart.com/ )
     * <br>
     * Needs file:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Tentacle.png</li>
     * </ul>
     * @return a TextureRegion containing an image of a tentacle.
     */
    public static TextureRegion getTentacle()
    {
        initialize();
        if(instance.tentacle == null || instance.tentacleRegion == null)
        {
            try {
                instance.tentacle = new Texture(Gdx.files.internal("Tentacle.png"));
                instance.tentacleRegion = new TextureRegion(instance.tentacle);
            } catch (Exception ignored) {
            }
        }
        return instance.tentacleRegion;
    }


    /**
     * Gets a TextureAtlas containing many icons with a distance field effect applied, allowing them to be used with
     * "stretchable" fonts and be resized in the same way. These will not look as-expected if stretchable fonts are not
     * in use, and will seem hazy and indistinct if the shader hasn't been set up for a distance field effect by
     * TextCellFactory (which stretchable fonts will do automatically). The one page of the TextureAtlas is 2048x2048,
     * which may be too large for some old, low-end Android phones, and possibly integrated graphics with fairly old
     * processors on desktop. It has over 2000 icons.
     * <br>
     * The icons are CC-BY and the license is distributed with them, though the icons are not necessarily included with
     * SquidLib. If you use the icon atlas, be sure to include icons-license.txt with it and reference it with your
     * game's license and/or credits information.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/icons.atlas</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/icons.png</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/icons-license.txt Needed to credit artists</li>
     * </ul>
     * @return a TextureAtlas containing over 2000 icons with a distance field effect
     */
    public static TextureAtlas getIconAtlas()
    {
        initialize();
        if(instance.iconAtlas == null)
        {
            try {
                instance.iconAtlas = new TextureAtlas(Gdx.files.internal("icons.atlas"));
            } catch (Exception ignored) {
            }
        }
        return instance.iconAtlas;
    }

    /**
     * This is a static global StatefulRNG that's meant for usage in cases where the seed does not matter and any
     * changes to this RNG's state will not change behavior elsewhere in the program; this means the GUI mainly.
     */
    public static StatefulRNG getGuiRandom()
    {
        initialize();
        if(instance.guiRandom == null)
        {
            instance.guiRandom =  new StatefulRNG();
        }
        return instance.guiRandom;
    }
    /**
     * This is a static global SquidColorCenter that can be used in places that just need an existing object that can do
     * things like analyze hue or saturation of a color.
     */
    public static SquidColorCenter getSCC()
    {
        initialize();
        if(instance.scc == null)
        {
            instance.scc =  new SquidColorCenter();
        }
        return instance.scc;
    }

    /**
     * Special symbols that can be used as icons if you use the narrow default font.
     */
    public static final String narrowFontSymbols = "ሀሁሂሃሄህሆሇለሉሊላሌልሎሏሐሑሒሓሔሕሖሗመሙሚማሜ",
                                  narrowFontAll = " !\"#$%&'()*+,-./0123\n" +
                                          "456789:;<=>?@ABCDEFG\n" +
                                          "HIJKLMNOPQRSTUVWXYZ[\n" +
                                          "\\]^_`abcdefghijklmno\n" +
                                          "pqrstuvwxyz{|}~¡¢£¤¥\n" +
                                          "¦§¨©ª«¬\u00AD®¯°±²³´µ¶·¸¹\n" +
                                          "º»¼½¾¿×ß÷øɍɎሀሁሂሃሄህሆሇ\n" +
                                          "ለሉሊላሌልሎሏሐሑሒሓሔሕሖሗመሙሚማ\n" +
                                          "ሜẞ‐‒–—―‖‗‘’‚‛“”„‟†‡•\n" +
                                          "…‧‹›€™"+
                                          "←↑→↓↷↺↻"+ // left, up, right, down, "tap", "counterclockwise", "clockwise"
                                          "∀∁∂∃∄∅∆\n" +
                                          "∇∈∉∋∌∎∏∐∑−∓∔∕∖∘∙√∛∜∝\n" +
                                          "∞∟∠∡∢∣∤∥∦∧∨∩∪∫∬∮∯∱∲∳\n" +
                                          "∴∵∶∷≈≋≠≡≢≣≤≥≦≧≨≩≪≫─━\n" +
                                          "│┃┄┅┆┇┈┉┊┋┌┍┎┏┐┑┒┓└┕\n" +
                                          "┖┗┘┙┚┛├┝┞┟┠┡┢┣┤┥┦┧┨┩\n" +
                                          "┪┫┬┭┮┯┰┱┲┳┴┵┶┷┸┹┺┻┼┽\n" +
                                          "┾┿╀╁╂╃╄╅╆╇╈╉╊╋╌╍╎╏═║\n" +
                                          "╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥\n" +
                                          "╦╧╨╩╪╫╬╭╮╯╰╱╲╳╴╵╶╷╸╹\n" +
                                          "╺╻╼╽╾╿▁▄▅▆▇█▌▐░▒▓▔▖▗\n" +
                                          "▘▙▚▛▜▝▞▟";

    /**
     * Called when the {@link Application} is about to pause
     */
    @Override
    public void pause() {
        if(arial15 != null) {
            arial15.dispose();
            arial15 = null;
        }
        if(narrow1 != null) {
            narrow1.dispose();
            narrow1 = null;
        }
        if(narrow2 != null) {
            narrow2.dispose();
            narrow2 = null;
        }
        if(narrow3 != null) {
            narrow3.dispose();
            narrow3 = null;
        }
        if(smooth1 != null) {
            smooth1.dispose();
            smooth1 = null;
        }
        if(smooth2 != null) {
            smooth2.dispose();
            smooth2 = null;
        }
        if(square1 != null) {
            square1.dispose();
            square1 = null;
        }
        if(square2 != null) {
            square2.dispose();
            square1 = null;
        }
        if(smoothSquare != null) {
            smoothSquare.dispose();
            smoothSquare = null;
        }
        if(smoothSquareOld != null) {
            smoothSquareOld.dispose();
            smoothSquareOld = null;
        }
        if(distanceSquare != null) {
            distanceSquare.dispose();
            distanceSquare = null;
        }
        if(distanceNarrow != null) {
            distanceNarrow.dispose();
            distanceNarrow = null;
        }
        if(typewriterDistanceNarrow != null) {
            typewriterDistanceNarrow.dispose();
            typewriterDistanceNarrow = null;
        }
        if(distanceClean != null) {
            distanceClean.dispose();
            distanceClean = null;
        }
        if(distancePrint != null) {
            distancePrint.dispose();
            distancePrint = null;
        }
        if (unicode1 != null) {
            unicode1.dispose();
            unicode1 = null;
        }
        if (unicode2 != null) {
            unicode2.dispose();
            unicode2 = null;
        }
        if(tentacle != null) {
            tentacle.dispose();
            tentacle = null;
        }
        if(iconAtlas != null) {
            iconAtlas.dispose();
            iconAtlas = null;
        }
    }

    /**
     * Called when the Application is about to be resumed
     */
    @Override
    public void resume() {
        initialize();
    }

    /**
     * Called when the {@link Application} is about to be disposed
     */
    @Override
    public void dispose() {
        pause();
        Gdx.app.removeLifecycleListener(this);
        instance = null;
    }
}
