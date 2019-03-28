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
 * or SquidLayers, it is recommended that you use a "stretchable" font (which use a signed distance field, or SDF,
 * effect to stretch smoothly) or a "crisp" font (which use a multi-channel signed distance field, or MSDF, effect to
 * stretch smoothly and remain sharp at very high zooms). For some usage you may want a TextFamily instead of a normal
 * TextCellFactory or BitmapFont; these TextFamily fonts store regular, bold, italic, and bold-italic faces in one file,
 * and {@link GDXMarkup} can produce text that will render in those faces. Some good choices for fonts here are
 * Iosevka, which comes in a sans-serif version with {@link #getStretchableLeanFont()}, {@link #getCrispLeanFont()}, 
 * {@link #getCrispLeanFamily()}, and more, as well as a slab-serif version with {@link #getStretchableSlabFont()},
 * {@link #getCrispSlabFont()}, and {@link #getCrispSlabFamily()}, among others, as well as Inconsolata-LGC, which is
 * more geometric sans-serif accessible with {@link #getStretchableFont()}, or (well-suited for square glyphs)
 * {@link #getStretchableSquareFont()} (it does not have a crisp variety). Another is Computer Modern, accessible by
 * {@link #getStretchableTypewriterFont()}, which has a chunky, ornamented style. For variable-width fonts
 * that can be used in TextPanel and LinesPanel, among others, you can use the recommended crisp font family Noto Serif
 * with {@link #getCrispPrintFamily()}, or for stretchable fonts there's the serif font Gentium with
 * {@link #getStretchablePrintFont()} or Noto Sans with {@link #getStretchableCleanFont()} (Noto Sans currently has
 * problems that Noto Serif does not have). All of the fonts so far support the Latin, Greek, and Cyrillic alphabets.
 * <br>
 * If you can't decide, go with {@link #getCrispLeanFont()} or {@link #getCrispSlabFont()}, which return TextCellFactory
 * objects, and call their .width(int), .height(int), and .initBySize() methods to make them the size (and aspect ratio)
 * you want. You can use stretchable or crisp fonts with equal width and height to make
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
            smooth1 = null, smooth2 = null, smoothSquare = null,
            square1 = null, square2 = null,
            unicode1 = null, unicode2 = null,
            arial15 = null, tiny = null, lessTiny;

    private TextCellFactory distanceNarrow = null, distanceSquare = null, typewriterDistanceNarrow = null,
            distancePrint = null, distanceClean = null, distanceCode = null, distanceCodeJP = null,
            distanceDejaVu = null, distanceOrbit = null, distanceHeavySquare = null,
            distanceSlab = null, distanceSlabLight = null,
            distanceLean = null, distanceLeanLight = null,
            msdfSlab = null, msdfSlabItalic = null, msdfLean = null, msdfLeanItalic = null,
            msdfDejaVu = null, msdfCurvySquare = null, msdfCarved = null,
            msdfIcons = null;
    private TextFamily familyLean = null, familySlab = null, familyGo = null,
            familyLeanMSDF = null, familySlabMSDF = null, familyPrintMSDF = null;
    private TextureAtlas iconAtlas = null;
    public static final String squareName = "Zodiac-Square-12x12.fnt", squareTexture = "Zodiac-Square-12x12.png",
            narrowName = "Rogue-Zodiac-6x12.fnt", narrowTexture = "Rogue-Zodiac-6x12_0.png",
            unicodeName = "Mandrill-6x16.fnt", unicodeTexture = "Mandrill-6x16.png",
            squareNameLarge = "Zodiac-Square-24x24.fnt", squareTextureLarge = "Zodiac-Square-24x24.png",
            narrowNameLarge = "Rogue-Zodiac-12x24.fnt", narrowTextureLarge = "Rogue-Zodiac-12x24_0.png",
            unicodeNameLarge = "Mandrill-12x32.fnt", unicodeTextureLarge = "Mandrill-12x32.png",
            narrowNameExtraLarge = "Rogue-Zodiac-18x36.fnt", narrowTextureExtraLarge = "Rogue-Zodiac-18x36_0.png",
            tinyName = "Monty-4x10.fnt", tinyTexture = "Monty-4x10.png",
            lessTinyName = "Monty-8x20.fnt", lessTinyTexture = "Monty-8x20.png",
            smoothName = "Inconsolata-LGC-8x18.fnt", smoothTexture = "Inconsolata-LGC-8x18.png",
            smoothNameLarge = "Inconsolata-LGC-12x24.fnt", smoothTextureLarge = "Inconsolata-LGC-12x24.png",
            smoothSquareName = "Inconsolata-LGC-Square-25x25.fnt", smoothSquareTexture = "Inconsolata-LGC-Square-25x25.png",
            distanceFieldSquare = "Inconsolata-LGC-Square-distance.fnt",
            distanceFieldSquareTexture = "Inconsolata-LGC-Square-distance.png",
            distanceFieldNarrow = "Inconsolata-LGC-Custom-distance.fnt",
            distanceFieldNarrowTexture = "Inconsolata-LGC-Custom-distance.png",
            distanceFieldHeavySquare = "BoxedIn-distance.fnt",
            distanceFieldHeavySquareTexture = "BoxedIn-distance.png",
            distanceFieldPrint = "Gentium-distance.fnt",
            distanceFieldPrintTexture = "Gentium-distance.png",
            distanceFieldClean = "Noto-Sans-distance.fnt",
            distanceFieldCleanTexture = "Noto-Sans-distance.png",
            distanceFieldTypewriterNarrow = "CM-Custom-distance.fnt",
            distanceFieldTypewriterNarrowTexture = "CM-Custom-distance.png",
            distanceFieldCode = "SourceCodePro-Medium-distance.fnt",
            distanceFieldCodeTexture = "SourceCodePro-Medium-distance.png",
            distanceFieldCodeJP = "SourceHanCodeJP-Regular-distance.fnt",
            distanceFieldCodeJPTexture = "SourceHanCodeJP-Regular-distance.png",
            distanceFieldDejaVu = "DejaVuSansMono-distance.fnt",
            distanceFieldDejaVuTexture = "DejaVuSansMono-distance.png",
            distanceFieldOrbit = "Orbitron-distance.fnt",
            distanceFieldOrbitTexture = "Orbitron-distance.png",
            distanceFieldLean = "Iosevka-distance.fnt",
            distanceFieldLeanTexture = "Iosevka-distance.png",
            distanceFieldLeanLight = "Iosevka-Light-distance.fnt",
            distanceFieldLeanLightTexture = "Iosevka-Light-distance.png",
            distanceFieldSlabLight = "Iosevka-Slab-Light-distance.fnt",
            distanceFieldSlabLightTexture = "Iosevka-Slab-Light-distance.png",
            distanceFieldSlab = "Iosevka-Slab-distance.fnt",
            distanceFieldSlabTexture = "Iosevka-Slab-distance.png",
            crispSlab = "Iosevka-Slab-msdf.fnt",
            crispSlabTexture = "Iosevka-Slab-msdf.png",
            crispSlabItalic = "Iosevka-Slab-Oblique-msdf.fnt",
            crispSlabItalicTexture = "Iosevka-Slab-Oblique-msdf.png",
            crispLean = "Iosevka-msdf.fnt",
            crispLeanTexture = "Iosevka-msdf.png",
            crispLeanItalic = "Iosevka-Oblique-msdf.fnt",
            crispLeanItalicTexture = "Iosevka-Oblique-msdf.png",
            crispDejaVu = "DejaVuSansMono-msdf.fnt",
            crispDejaVuTexture = "DejaVuSansMono-msdf.png",
            crispNotoSerif = "NotoSerif-Family-msdf.fnt",
            crispNotoSerifTexture = "NotoSerif-Family-msdf.png",
            crispCarved = "bloccus-msdf.fnt",
            crispCarvedTexture = "bloccus-msdf.png",
            crispCurvySquare = "square-msdf.fnt",
            crispCurvySquareTexture = "square-msdf.png",
            crispIcons = "awesome-solid-msdf.fnt",
            crispIconsTexture = "awesome-solid-msdf.png"
                    ;
    public static final String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n"
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

    public static final String fragmentShader = "#ifdef GL_ES\n"
            + " precision mediump float;\n"
            + " precision mediump int;\n"
            + "#endif\n"
            + "\n"
            + "uniform sampler2D u_texture;\n"
            + "uniform float u_smoothing;\n"
            + "varying vec4 v_color;\n"
            + "varying vec2 v_texCoords;\n"
            + "\n"
            + "void main() {\n"
            + "	 if (u_smoothing > 0.0) {\n"
            + "	   vec4 box = vec4(v_texCoords-0.000125, v_texCoords+0.000125);\n"
            + "	   float asum = smoothstep(0.5 - u_smoothing, 0.5 + u_smoothing, texture2D(u_texture, box.xy).a) +\n"
            + "                 smoothstep(0.5 - u_smoothing, 0.5 + u_smoothing, texture2D(u_texture, box.zw).a) +\n"
            + "                 smoothstep(0.5 - u_smoothing, 0.5 + u_smoothing, texture2D(u_texture, box.xw).a) +\n"
            + "                 smoothstep(0.5 - u_smoothing, 0.5 + u_smoothing, texture2D(u_texture, box.zy).a);\n"
            + "    gl_FragColor = vec4(v_color.rgb, ((smoothstep(0.5 - u_smoothing, 0.5 + u_smoothing, texture2D(u_texture, v_texCoords).a) + 0.5 * asum) / 3.0) * v_color.a);\n"
            + "	 } else {\n"
            + "		gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n"
            + "	 }\n"
            + "}\n";
//    public static final String fragmentShader = "#ifdef GL_ES\n"
//            + "	precision mediump float;\n"
//            + "	precision mediump int;\n"
//            + "#endif\n"
//            + "\n"
//            + "uniform sampler2D u_texture;\n"
//            + "uniform float u_smoothing;\n"
//            + "varying vec4 v_color;\n"
//            + "varying vec2 v_texCoords;\n"
//            + "\n"
//            + "void main() {\n"
//            + "	if (u_smoothing > 0.0) {\n"
//            + "		float distance = texture2D(u_texture, v_texCoords).a;\n"
//            + "		float alpha = smoothstep(0.5 - u_smoothing, 0.5 + u_smoothing, distance);\n"
//            + "		gl_FragColor = vec4(v_color.rgb, alpha * v_color.a);\n"
//            + "	} else {\n"
//            + "		gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n"
//            + "	}\n"
//            + "}\n";
    public static final String msdfFragmentShader =  "#ifdef GL_ES\n"
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
            + "  vec3 sdf = texture2D(u_texture, v_texCoords).rgb;\n"
            + "  gl_FragColor = vec4(v_color.rgb, clamp((max(min(sdf.r, sdf.g), min(max(sdf.r, sdf.g), sdf.b)) - 0.5) * u_smoothing + 0.5, 0.0, 1.0) * v_color.a);\n"
            //+ " float dist = (max(min(sdf.r, sdf.g), min(max(sdf.r, sdf.g), sdf.b)) - 0.5) / (2.75 * u_smoothing);\n"
            //+ " float d = u_smoothing * 1.75;\n" 
            //+ " float d = fwidth(dist);\n"
            //+ " float alpha = clamp(dist + 0.5, 0.0, 1.0);\n"
            //+ " float alpha = smoothstep(-d, d, dist);\n"
            //+ " if(alpha == 0) { discard; }\n"
            //+ "	gl_FragColor = vec4(v_color.rgb, alpha * v_color.a);\n"

            //+ "	 vec3 sdf;\n"
            //+ "	 vec4 box = vec4(v_texCoords-0.0000625, v_texCoords+0.0000625);\n"
            //+ "  sdf = texture2D(u_texture, box.xy).rgb;\n"
            //+ "	 float asum = clamp((max(min(sdf.r, sdf.g), min(max(sdf.r, sdf.g), sdf.b)) - 0.5) / (6.0 * u_smoothing) + 0.5, 0.0, 0.5);\n"
            //+ "  sdf = texture2D(u_texture, box.zw).rgb;\n"
            //+ "  asum = asum + clamp((max(min(sdf.r, sdf.g), min(max(sdf.r, sdf.g), sdf.b)) - 0.5) / (6.0 * u_smoothing) + 0.5, 0.0, 0.5); +\n"
            //+ "  sdf = texture2D(u_texture, box.xw).rgb;\n"
            //+ "  asum = asum + clamp((max(min(sdf.r, sdf.g), min(max(sdf.r, sdf.g), sdf.b)) - 0.5) / (6.0 * u_smoothing) + 0.5, 0.0, 0.5); +\n"
            //+ "  sdf = texture2D(u_texture, box.zy).rgb;\n"
            //+ "  asum = asum + clamp((max(min(sdf.r, sdf.g), min(max(sdf.r, sdf.g), sdf.b)) - 0.5) / (6.0 * u_smoothing) + 0.5, 0.0, 0.5); +\n"
            //+ "  sdf = texture2D(u_texture, v_texCoords).rgb;\n"
            //+ "  gl_FragColor = vec4(v_color.rgb, clamp(((max(min(sdf.r, sdf.g), min(max(sdf.r, sdf.g), sdf.b)) - 0.625) / (3.0 * u_smoothing) + 0.625 + asum) / 3.0, 0.0, 1.0) * v_color.a);\n"
            + "}\n";
    /**
     * An alternate shader based on {@link DefaultResources#fragmentShader}, but this draws outlines around characters.
     * Only works with distance field fonts. You probably need to create a new ShaderProgram to use this, which would
     * look like {@code new ShaderProgram(DefaultResources.vertexShader, DefaultResources.outlineFragmentShader)}, and
     * would assign this to your TextCellFactory's {@link TextCellFactory#shader} field after the TextCellFactory is
     * fully initialized (calling {@link TextCellFactory#initBySize()}) will set the shader back to the default).
     */
    public static final String outlineFragmentShader = "#ifdef GL_ES\n"
            + "precision mediump float;\n"
            + "precision mediump int;\n"
            + "#endif\n"
            + "\n"
            + "uniform sampler2D u_texture;\n"
            + "uniform float u_smoothing;\n"
            + "varying vec4 v_color;\n"
            + "varying vec2 v_texCoords;\n"
            + "\n"
            + "void main() {\n"
            + "  if(u_smoothing <= 0.0) {\n"
            + "    float smoothing = -u_smoothing;\n"
            + "	   vec4 box = vec4(v_texCoords-0.000125, v_texCoords+0.000125);\n"
            + "	   float asum = smoothstep(0.5 - smoothing, 0.5 + smoothing, texture2D(u_texture, v_texCoords).a) + 0.5 * (\n"
            + "                 smoothstep(0.5 - smoothing, 0.5 + smoothing, texture2D(u_texture, box.xy).a) +\n"
            + "                 smoothstep(0.5 - smoothing, 0.5 + smoothing, texture2D(u_texture, box.zw).a) +\n"
            + "                 smoothstep(0.5 - smoothing, 0.5 + smoothing, texture2D(u_texture, box.xw).a) +\n"
            + "                 smoothstep(0.5 - smoothing, 0.5 + smoothing, texture2D(u_texture, box.zy).a));\n"
            + "    gl_FragColor = vec4(v_color.rgb, (asum / 3.0) * v_color.a);\n"
            + "	 } else {\n"
            + "    float distance = texture2D(u_texture, v_texCoords).a;\n"
            + "	   vec2 box = vec2(0.0, 0.00375 * (u_smoothing + 0.0825));\n"
            + "	   float asum = 0.7 * (smoothstep(0.5 - u_smoothing, 0.5 + u_smoothing, distance) + \n"
            + "                   smoothstep(0.5 - u_smoothing, 0.5 + u_smoothing, texture2D(u_texture, v_texCoords + box.xy).a) +\n"
            + "                   smoothstep(0.5 - u_smoothing, 0.5 + u_smoothing, texture2D(u_texture, v_texCoords - box.xy).a) +\n"
            + "                   smoothstep(0.5 - u_smoothing, 0.5 + u_smoothing, texture2D(u_texture, v_texCoords + box.yx).a) +\n"
            + "                   smoothstep(0.5 - u_smoothing, 0.5 + u_smoothing, texture2D(u_texture, v_texCoords - box.yx).a)),\n"
            + "                 outline = clamp((distance * 0.8 - 0.415) * 18, 0, 1);\n"
            + "	   gl_FragColor = vec4(mix(vec3(0.0), v_color.rgb, outline), asum * v_color.a);\n"
            + "  }\n"
            + "}\n";
    /**
     * An alternate shader based on {@link DefaultResources#msdfFragmentShader}, but this draws outlines around
     * characters. This should be sharper than {@link #outlineFragmentShader},  but only works with MSDF (crisp) fonts.
     * Unlike that shader, this doesn't have special behavior for negative
     * {@link TextCellFactory#getSmoothingMultiplier()} values, though that may be added later.
     * You probably need to create a new ShaderProgram to use this, which would look like
     * {@code new ShaderProgram(DefaultResources.vertexShader, DefaultResources.msdfOutlineFragmentShader)}, and
     * would assign this to your TextCellFactory's {@link TextCellFactory#shader} field after the TextCellFactory is
     * fully initialized (calling {@link TextCellFactory#initBySize()}) will set the shader back to the default).
     */
    public static final String msdfOutlineFragmentShader =  "#ifdef GL_ES\n"
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
            + "  vec3 sdf = texture2D(u_texture, v_texCoords).rgb;\n"
            + "  float distance = (max(min(sdf.r, sdf.g), min(max(sdf.r, sdf.g), sdf.b)) - 0.425);"
            //+ "  float cd = clamp(distance, 0.0, 1.0);"
            + "  gl_FragColor = vec4(step(3.5 * u_smoothing, distance * 0.75) * v_color.rgb, clamp((distance / (3.4 * u_smoothing)) + 0.9125, 0.0, 1.0) * v_color.a);\n"
            + "}\n";

    private SquidColorCenter scc = null;
    private Texture tentacle = null;
    private TextureRegion tentacleRegion = null;
    private StatefulRNG guiRandom;

    private static DefaultResources instance = null;

    static BitmapFont copyFont(BitmapFont font)
    {
        if(font == null) return new BitmapFont();
        return new BitmapFont(new BitmapFont.BitmapFontData(font.getData().getFontFile(), false),
                font.getRegions(), font.usesIntegerPositions());
    }
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
        return copyFont(instance.arial15);
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
                instance.square1 = new BitmapFont(Gdx.files.internal(squareName), Gdx.files.internal(squareTexture), false);
                //instance.square1.getData().padBottom = instance.square1.getDescent();
            } catch (Exception e) {
            }
        }
        return copyFont(instance.square1);
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
                instance.square2 = new BitmapFont(Gdx.files.internal(squareNameLarge), Gdx.files.internal(squareTextureLarge), false);
                //instance.square2.getData().padBottom = instance.square2.getDescent();
            } catch (Exception e) {
            }
        }
        return copyFont(instance.square2);
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
                instance.narrow1 = new BitmapFont(Gdx.files.internal(narrowName), Gdx.files.internal(narrowTexture), false);
                //instance.narrow1.getData().padBottom = instance.narrow1.getDescent();
            } catch (Exception e) {
            }
        }
        return copyFont(instance.narrow1);
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
                instance.narrow2 = new BitmapFont(Gdx.files.internal(narrowNameLarge), Gdx.files.internal(narrowTextureLarge), false);
                //instance.narrow2.getData().padBottom = instance.narrow2.getDescent();
            } catch (Exception e) {
            }
        }
        return copyFont(instance.narrow2);
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
                instance.narrow3 = new BitmapFont(Gdx.files.internal(narrowNameExtraLarge), Gdx.files.internal(narrowTextureExtraLarge), false);
                //instance.narrow3.getData().padBottom = instance.narrow3.getDescent();
            } catch (Exception e) {
            }
        }
        return copyFont(instance.narrow3);
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
                instance.smooth1 = new BitmapFont(Gdx.files.internal(smoothName), Gdx.files.internal(smoothTexture), false);
                //instance.smooth1.getData().padBottom = instance.smooth1.getDescent();
            } catch (Exception e) {
            }
        }
        return copyFont(instance.smooth1);
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
                instance.smooth2 = new BitmapFont(Gdx.files.internal(smoothNameLarge), Gdx.files.internal(smoothTextureLarge), false);
                //instance.smooth2.getData().padBottom = instance.smooth2.getDescent();
            } catch (Exception e) {
            }
        }
        return copyFont(instance.smooth2);
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
                instance.unicode1 = new BitmapFont(Gdx.files.internal(unicodeName), Gdx.files.internal(unicodeTexture), false);
                //instance.unicode1.getData().padBottom = instance.unicode1.getDescent();
            } catch (Exception e) {
            }
        }
        return copyFont(instance.unicode1);
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
                instance.unicode2 = new BitmapFont(Gdx.files.internal(unicodeNameLarge), Gdx.files.internal(unicodeTextureLarge), false);
                //instance.unicode2.getData().padBottom = instance.unicode2.getDescent();
            } catch (Exception e) {
            }
        }
        return copyFont(instance.unicode2);
    }
    /**
     * Returns a 25x25px, very smooth and generally good-looking font (based on Inconsolata) as an embedded resource.
     * This font fully supports Latin, Greek, Cyrillic, and of particular interest to SquidLib, Box Drawing characters.
     * This variant is (almost) perfectly square, and box drawing characters should line up at size 25x25 px, but other
     * glyphs will have much more horizontal spacing than in other fonts. Caches the font for later calls. You may want
     * {@link #getStretchableSquareFont()} instead, which is the same font but can stretch smoothly to most sizes.
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
                instance.smoothSquare = new BitmapFont(Gdx.files.internal(smoothSquareName), Gdx.files.internal(smoothSquareTexture), false);
                //instance.smoothSquare.getData().padBottom = instance.smoothSquare.getDescent();
            } catch (Exception e) {
            }
        }
        return copyFont(instance.smoothSquare);
    }
    /**
     * Returns a 4x10px, extremely thin font (that may be barely legible) as an embedded resource.
     * The font builds on work by Christian Munk in his font called Monotwist, making some changes for legibility.
     * This font has the lowest width of any font asset distributed with SquidLib. Caches the font for later calls.
     * Attribution to Christian Munk (or the username he used, CMunk) is required to use this font; this is in the
     * license file linked below.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Monty-4x10.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Monty-4x10.png</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Monty-license.txt</li>
     * </ul>
     * @return the BitmapFont object representing Monty.ttf at size 8 pt
     */
    public static BitmapFont getTinyFont()
    {
        initialize();
        if(instance.tiny == null)
        {
            try {
                instance.tiny = new BitmapFont(Gdx.files.internal(tinyName), Gdx.files.internal(tinyTexture), false);
                //instance.narrow1.getData().padBottom = instance.narrow1.getDescent();
            } catch (Exception e) {
            }
        }
        return copyFont(instance.tiny);
    }
    /**
     * Returns a 8x20px, extremely thin font (that may be barely legible) as an embedded resource.
     * The font builds on work by Christian Munk in his font called Monotwist, making some changes for legibility.
     * A smaller version of this font, {@link #getTinyFont()}, has the lowest width of any font asset distributed with
     * SquidLib; this doubles its resolution in case you like its appearance but don't want to go blind. Caches the font
     * for later calls. Attribution to Christian Munk (or the username he used, CMunk) is required to use this font;
     * this is in the license file linked below.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Monty-8x20.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Monty-8x20.png</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Monty-license.txt</li>
     * </ul>
     * @return the BitmapFont object representing Monty.ttf at size 16 pt
     */
    public static BitmapFont getLessTinyFont()
    {
        initialize();
        if(instance.lessTiny == null)
        {
            try {
                instance.lessTiny = new BitmapFont(Gdx.files.internal(lessTinyName), Gdx.files.internal(lessTinyTexture), false);
            } catch (Exception e) {
            }
        }
        return copyFont(instance.lessTiny);
    }

    /**
     * Returns a TextCellFactory already configured to use a square font that should scale cleanly to many sizes. Caches
     * the result for later calls.
     * <br>
     * Preview: http://i.imgur.com/DD1RkPa.png
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
     * Returns a TextCellFactory already configured to use an blocky, fairly-bold square font that should scale cleanly
     * to many sizes. Unlike {@link #getStretchableSquareFont()}, the font this uses was made to be square initially,
     * and is not a distorted/stretched version of an existing font. Caches the result for later calls.
     * <br>
     * <a href="https://i.imgur.com/nIjgTOp.png">Preview at 6x6 size, https://i.imgur.com/nIjgTOp.png</a>
     * <a href="https://i.imgur.com/lWaSgVN.png">Preview at much larger size, https://i.imgur.com/lWaSgVN.png</a>
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/BoxedIn-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/BoxedIn-distance.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the square font BoxedIn.ttf
     */
    public static TextCellFactory getStretchableHeavySquareFont()
    {
        initialize();
        if(instance.distanceHeavySquare == null)
        {
            try {
                instance.distanceHeavySquare = new TextCellFactory()
                        .fontDistanceField(distanceFieldHeavySquare, distanceFieldHeavySquareTexture)
                        .setSmoothingMultiplier(2.125f);
            } catch (Exception e) {
            }
        }
        if(instance.distanceHeavySquare != null)
            return instance.distanceHeavySquare.copy();
        return null;
    }

    /**
     * Returns a TextCellFactory already configured to use an all-caps (with lighter-weight versions of upper-case
     * letters for the lower-case glyphs) square font that should scale cleanly to many sizes.
     * Unlike {@link #getStretchableSquareFont()}, the font this uses was made to be square initially, and is not a
     * distorted/stretched version of an existing font. This font only supports ASCII, and as said before, it doesn't
     * have separate lower-case letters. Note: if you use this font as-is, most characters will overlap slightly with
     * each other, which can be an aesthetic benefit in some styles but can also be hard to read, at least if the colors
     * aren't different between overlapping characters. For that reason, you may want to tweak the width and height of
     * this font after passing it to a SquidLayers or SparseLayers; often you would pass 0.875 times the original
     * width and height to {@link TextCellFactory#tweakWidth(float)} and {@link TextCellFactory#tweakHeight(float)}.
     * Caches the result for later calls. The font is "square" by Wouter van Oortmerssen; it is available under a
     * CC-BY-3.0 license, which requires attribution to Wouter van Oortmerssen if you use it.
     * <br>
     * <a href="https://i.imgur.com/nvHl64v.png">Preview at large size</a>
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/square-msdf.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/square-msdf.png</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/square-License.txt</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font square.ttf
     */
    public static TextCellFactory getCrispCurvySquareFont()
    {
        initialize();
        if(instance.msdfCurvySquare == null)
        {
            try {
                instance.msdfCurvySquare = new TextCellFactory()
                        .fontMultiDistanceField(crispCurvySquare, crispCurvySquareTexture).setSmoothingMultiplier(6f);
            } catch (Exception e) {
            }
        }
        if(instance.msdfCurvySquare != null)
            return instance.msdfCurvySquare.copy();
        return null;
    }


    /**
     * Returns a TextCellFactory already configured to use a narrow font (twice as tall as it is wide) that should scale
     * cleanly to many sizes. Caches the result for later calls.
     * <br>
     * Preview: http://i.imgur.com/dvEEMqo.png
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
     * Preview: http://i.imgur.com/oN2gRci.png
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
     * Returns a TextCellFactory already configured to use a highly-legible fixed-width font with broad Unicode support
     * that should scale cleanly to many sizes. Caches the result for later calls. The font used is Source Code Pro, an
     * open-source (SIL Open Font License) typeface by Adobe, and it has good Unicode support among the fixed-width
     * fonts used by SquidLib. It may be a good choice for science-fiction games because of its modern feel, but the
     * legibility enhancements made so the font could be usable in text editors also are nice for all text-based games.
     * The high glyph count means the part of the image for each glyph is smaller, though, so this may look slightly
     * pixelated if it starts small and is resized to much larger. A cell width of 15 and cell height of 27 is ideal;
     * this allows the font to resize fairly well to larger sizes using Viewports.
     * <br>
     * Preview: http://i.imgur.com/VC0xn2r.png
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/SourceCodePro-Medium-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/SourceCodePro-Medium-distance.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font SourceCodePro-Medium.otf
     */
    public static TextCellFactory getStretchableCodeFont()
    {
        initialize();
        if(instance.distanceCode == null)
        {
            try {
                instance.distanceCode = new TextCellFactory()
                        .fontDistanceField(distanceFieldCode, distanceFieldCodeTexture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.distanceCode != null)
            return instance.distanceCode.copy();
        return null;
    }
    /**
     * Returns a TextCellFactory already configured to use a highly-legible fixed-width font with strong CJK support (in
     * particular, very good coverage for Japanese) that should scale cleanly to many sizes. Caches the result for later
     * calls. The font used is Source Han Code JP, an open-source (SIL Open Font License) typeface by Adobe, and it has
     * the best CJK char support among the fonts used by SquidLib. It is extremely wide if used only for English text,
     * which may be desirable to make square cells. The very high glyph count means the part of the image for each glyph
     * is smaller, though, so this may look slightly pixelated if it starts small and is resized to much larger. A cell
     * width of 19 and cell height of 20 is ideal; this allows the font to resize fairly well to larger sizes.
     * <br>
     * Preview: https://i.imgur.com/g65jXxB.png
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/SourceHanCodeJP-Regular-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/SourceHanCodeJP-Regular-distance.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font SourceHanCodeJP-Regular.otf
     */
    public static TextCellFactory getStretchableCodeJPFont()
    {
        initialize();
        if(instance.distanceCodeJP == null)
        {
            try {
                instance.distanceCodeJP = new TextCellFactory()
                        .fontDistanceField(distanceFieldCodeJP, distanceFieldCodeJPTexture).setSmoothingMultiplier(1.125f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.distanceCodeJP != null)
            return instance.distanceCodeJP.copy();
        return null;
    }
    /**
     * Returns a TextCellFactory already configured to use a highly-legible fixed-width font with good Unicode support
     * and a thin, geometric style, that should scale cleanly to many sizes. Caches the result for later calls. The font
     * used is Iosevka, an open-source (SIL Open Font License) typeface by Belleve Invis (see
     * https://be5invis.github.io/Iosevka/ ), and it uses several customizations thanks to Iosevka's special build
     * process. It supports a lot of glyphs, including quite a bit of extended Latin, Greek, and Cyrillic, but also
     * circled letters and digits and the necessary box drawing characters. The high glyph count means the part of the
     * image for each glyph is smaller, though, so this may look slightly pixelated if it starts small and is resized to
     * much larger. A cell width of 11 and cell height of 20 is ideal (or some approximate multiple of that aspect
     * ratio); this allows the font to resize fairly well to larger sizes using Viewports. As an aside, Luc Devroye (a
     * true typography expert) called Iosevka <a href="http://luc.devroye.org/fonts-82704.html">"A tour de force that
     * deserves an award."</a> You may want to try using both this sans-serif version of Iosevka and the slab-serif
     * version SquidLib has, {@link #getStretchableSlabFont()}, though they have subtly different sizes. There are
     * also versions of Iosevka that use a Light weight, available by {@link #getStretchableLeanLightFont()} and
     * {@link #getStretchableSlabLightFont()}.
     * <br>
     * Preview: http://i.imgur.com/sm0ULbU.png
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-distance.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font Iosevka.ttf
     */
    public static TextCellFactory getStretchableLeanFont()
    {
        initialize();
        if(instance.distanceLean == null)
        {
            try {
                instance.distanceLean = new TextCellFactory()
                        .fontDistanceField(distanceFieldLean, distanceFieldLeanTexture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.distanceLean != null)
            return instance.distanceLean.copy();
        return null;
    }

    /**
     * Returns a TextCellFactory already configured to use a highly-legible fixed-width font with good Unicode support
     * and a slab-serif geometric style, that should scale cleanly to many sizes. Caches the result for later calls. The
     * font used is Iosevka with Slab style, an open-source (SIL Open Font License) typeface by Belleve Invis (see
     * https://be5invis.github.io/Iosevka/ ), and it uses several customizations thanks to Iosevka's special build
     * process. It supports a lot of glyphs, including quite a bit of extended Latin, Greek, and Cyrillic, but also
     * circled letters and digits and the necessary box drawing characters. The high glyph count means the part of the
     * image for each glyph is smaller, though, so this may look slightly pixelated if it starts small and is resized to
     * much larger. A cell width of 11 and cell height of 20 is ideal (or some approximate multiple of that aspect
     * ratio); this allows the font to resize fairly well to larger sizes using Viewports. As an aside, Luc Devroye (a
     * true typography expert) called Iosevka <a href="http://luc.devroye.org/fonts-82704.html">"A tour de force that
     * deserves an award."</a> You may want to try using both this version of Iosevka with slab serifs and the other
     * version SquidLib has, {@link #getStretchableLeanFont()}, though they have subtly different sizes. There are
     * also versions of Iosevka that use a Light weight, available by {@link #getStretchableLeanLightFont()} and
     * {@link #getStretchableSlabLightFont()}.
     * <br>
     * Preview: http://i.imgur.com/5kb697p.png
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Slab-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Slab-distance.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font Iosevka-Slab.ttf
     */
    public static TextCellFactory getStretchableSlabFont()
    {
        initialize();
        if(instance.distanceSlab == null)
        {
            try {
                instance.distanceSlab = new TextCellFactory()
                        .fontDistanceField(distanceFieldSlab, distanceFieldSlabTexture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.distanceSlab != null)
            return instance.distanceSlab.copy();
        return null;
    }
    /**
     * Returns a TextCellFactory already configured to use a highly-legible fixed-width font with good Unicode support
     * and a very thin, geometric style, that should scale cleanly to many sizes. Caches the result for later calls. The
     * font used is Iosevka (at Light weight), an open-source (SIL Open Font License) typeface by Belleve Invis (see
     * https://be5invis.github.io/Iosevka/ ), and it uses several customizations thanks to Iosevka's special build
     * process. It supports a lot of glyphs, including quite a bit of extended Latin, Greek, and Cyrillic, but also
     * circled letters and digits and the necessary box drawing characters. The high glyph count means the part of the
     * image for each glyph is smaller, though, so this may look slightly pixelated if it starts small and is resized to
     * much larger. A cell width of 11 and cell height of 20 is ideal (or some approximate multiple of that aspect
     * ratio); this allows the font to resize fairly well to larger sizes using Viewports. As an aside, Luc Devroye (a
     * true typography expert) called Iosevka <a href="http://luc.devroye.org/fonts-82704.html">"A tour de force that
     * deserves an award."</a> You may want to try using both this sans-serif version of Iosevka and the slab-serif
     * version SquidLib has, {@link #getStretchableSlabLightFont()}, though they have subtly different sizes. There are
     * also versions of Iosevka that do not use this Light weight, available by {@link #getStretchableLeanFont()} and
     * {@link #getStretchableSlabFont()}.
     * <br>
     * Preview: http://i.imgur.com/edKimT4.png
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Light-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Light-distance.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font Iosevka-Light.ttf
     */
    public static TextCellFactory getStretchableLeanLightFont()
    {
        initialize();
        if(instance.distanceLeanLight == null)
        {
            try {
                instance.distanceLeanLight = new TextCellFactory()
                        .fontDistanceField(distanceFieldLeanLight, distanceFieldLeanLightTexture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.distanceLeanLight != null)
            return instance.distanceLeanLight.copy();
        return null;
    }

    /**
     * Returns a TextCellFactory already configured to use a highly-legible fixed-width font with good Unicode support
     * and a very thin, slab-serif geometric style, that should scale cleanly to many sizes. Caches the result for later
     * calls. The font used is Iosevka with Slab style at Light weight, an open-source (SIL Open Font License) typeface
     * by Belleve Invis (see https://be5invis.github.io/Iosevka/ ), and it uses several customizations thanks to
     * Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin, Greek, and
     * Cyrillic, but also circled letters and digits and the necessary box drawing characters. The high glyph count
     * means the part of the image for each glyph is smaller, though, so this may look slightly pixelated if it starts
     * small and is resized to much larger. A cell width of 11 and cell height of 20 is ideal (or some approximate
     * multiple of that aspect ratio); this allows the font to resize fairly well to larger sizes using Viewports. As an
     * aside, Luc Devroye (a true typography expert) called Iosevka <a href="http://luc.devroye.org/fonts-82704.html">"A
     * tour de force that deserves an award."</a> You may want to try using both this version of Iosevka with slab
     * serifs and the other version SquidLib has, {@link #getStretchableLeanLightFont()}, though they have subtly
     * different sizes. There are also versions of Iosevka that do not use this Light weight, available by
     * {@link #getStretchableLeanFont()} and {@link #getStretchableSlabFont()}.
     * <br>
     * Preview: http://i.imgur.com/B5eSGfj.png
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Slab-Light-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Slab-Light-distance.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font Iosevka-Slab-Light.ttf
     */
    public static TextCellFactory getStretchableSlabLightFont()
    {
        initialize();
        if(instance.distanceSlabLight == null)
        {
            try {
                instance.distanceSlabLight = new TextCellFactory()
                        .fontDistanceField(distanceFieldSlabLight, distanceFieldSlabLightTexture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.distanceSlabLight != null)
            return instance.distanceSlabLight.copy();
        return null;
    }
    
    /**
     * Returns a TextCellFactory already configured to use a font with extremely wide Unicode support that should scale
     * cleanly to many sizes. Caches the result for later calls. The font is DejaVu Sans Mono, a common font on Linux
     * operating systems and a clean-looking, legible font, though it has some visual quirks like a "tail" on lower-case
     * 'l', that take some getting used to. A possible requirement for this font is that the size of the text in a
     * SquidPanel or SquidLayers may need to be increased 1-5 pixels past what the cell width and height are; this can
     * be done with {@link SquidPanel#setTextSize(float, float)} or {@link SquidLayers#setTextSize(float, float)},
     * giving 1-2 more than the cell width for x and 3-5 more than the cell height for y.
     * <br>
     * Preview: http://i.imgur.com/1haETOe.png
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/DejaVuSansMono-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/DejaVuSansMono-distance.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font DejaVuSansMono.ttf
     */
    public static TextCellFactory getStretchableDejaVuFont()
    {
        initialize();
        if(instance.distanceDejaVu == null)
        {
            try {
                instance.distanceDejaVu = new TextCellFactory().fontDistanceField(distanceFieldDejaVu, distanceFieldDejaVuTexture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.distanceDejaVu != null)
            return instance.distanceDejaVu.copy();
        return null;

    }
    /**
     * Returns a TextCellFactory already configured to use a variable-width serif font that should look like the serif
     * fonts used in many novels' main texts, and that should scale cleanly to many sizes. Meant to be used in variable-
     * width displays like TextPanel. Caches the result for later calls.
     * <br>
     * Preview: http://i.imgur.com/eIYYt8C.png
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
                        /* .setSmoothingMultiplier(0.4f) */.height(37).tweakHeight(34).width(8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.distancePrint != null)
            return instance.distancePrint.copy();
        return null;
    }

    /**
     * Returns a TextCellFactory already configured to use a variable-width sans-serif font that should have a blocky,
     * futuristic look (based on the font Orbitron), and that should scale cleanly to many sizes. Meant to be used in
     * variable-width displays like TextPanel. Caches the result for later calls.
     * <br>
     * Preview: http://i.imgur.com/grJhoMs.png
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Orbitron-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Orbitron-distance.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font Orbitron, by The League of Movable Type
     */
    public static TextCellFactory getStretchableOrbitFont() {
        initialize();
        if (instance.distanceOrbit == null) {
            try {
                instance.distanceOrbit = new TextCellFactory().setDirectionGlyph('ˆ')
                        .fontDistanceField(distanceFieldOrbit, distanceFieldOrbitTexture)
                        .setSmoothingMultiplier(1.3f).height(30).width(11);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.distanceOrbit != null)
            return instance.distanceOrbit.copy();
        return null;
    }

    /**
     * Returns a TextCellFactory already configured to use a variable-width sans-serif font that currently looks
     * slightly jumbled without certain layout features. Meant to be used in variable-width displays like TextPanel, but
     * currently you should prefer getStretchablePrintFont() for legibility. Caches the result for later calls.
     * <br>
     * Preview: http://i.imgur.com/GF5qQxn.png
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work. Sets the smoothing multiplier to 0.8f; if you set the smoothing
     * multiplier yourself, it should be similarly smaller than what other fonts use (most fonts are 1.0, so multiplying
     * whatever you would normally set the smoothing multiplier to by 0.8f should work).
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
                        .setSmoothingMultiplier(0.8f).height(30).width(5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.distanceClean != null)
            return instance.distanceClean.copy();
        return null;
    }

    /**
     * Returns a TextCellFactory already configured to use a highly-legible fixed-width font with good Unicode support
     * and a slab-serif geometric style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Iosevka with Slab style, an open-source (SIL Open Font
     * License) typeface by Belleve Invis (see https://be5invis.github.io/Iosevka/ ), and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic, but also the necessary box drawing characters. This uses the Multi-channel Signed Distance
     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
     * sharper edges and precise corners instead of rounded tips on strokes. As an aside, Luc Devroye (a true typography
     * expert) called Iosevka
     * <a href="http://luc.devroye.org/fonts-82704.html">"A tour de force that deserves an award."</a>
     * <br>
     * Preview: <a href="https://i.imgur.com/YlzFEVX.png">Image link</a>
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * multi-channel distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Slab-msdf.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Slab-msdf.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font Iosevka-Slab.ttf using MSDF
     */
    public static TextCellFactory getCrispSlabFont()
    {
        initialize();
        if(instance.msdfSlab == null)
        {
            try {
                instance.msdfSlab = new TextCellFactory()
                        .fontMultiDistanceField(crispSlab, crispSlabTexture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.msdfSlab != null)
            return instance.msdfSlab.copy();
        return null;
    }
    /**
     * Returns a TextCellFactory already configured to use a highly-legible oblique (similar to italic) fixed-width font
     * with good Unicode support and a slab-serif geometric style, that should scale cleanly to even very large sizes
     * (using an MSDF technique). Caches the result for later calls. The font used is Iosevka with Oblique Slab style,
     * an open-source (SIL Open Font License) typeface by Belleve Invis (see https://be5invis.github.io/Iosevka/ ), and
     * it uses several customizations thanks to Iosevka's special build process. It supports a lot of glyphs, including
     * quite a bit of extended Latin, Greek, and Cyrillic, but also the necessary box drawing characters. This uses the
     * Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field technique,
     * which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes. As an aside,
     * Luc Devroye (a true typography expert) called Iosevka
     * <a href="http://luc.devroye.org/fonts-82704.html">"A tour de force that deserves an award."</a>
     * <br>
     * Preview: <a href="https://i.imgur.com/njA1ae1.png">Image link</a>
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * multi-channel distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Slab-Oblique-msdf.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Slab-Oblique-msdf.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font Iosevka-Slab-Oblique.ttf
     */
    public static TextCellFactory getCrispSlabItalicFont()
    {
        initialize();
        if(instance.msdfSlabItalic == null)
        {
            try {
                instance.msdfSlabItalic = new TextCellFactory()
                        .fontMultiDistanceField(crispSlabItalic, crispSlabItalicTexture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.msdfSlabItalic != null)
            return instance.msdfSlabItalic.copy();
        return null;
    }
    /**
     * Returns a TextCellFactory already configured to use a highly-legible fixed-width font with good Unicode support
     * and a sans-serif geometric style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font
     * License) typeface by Belleve Invis (see https://be5invis.github.io/Iosevka/ ), and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic, but also the necessary box drawing characters. This uses the Multi-channel Signed Distance
     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
     * sharper edges and precise corners instead of rounded tips on strokes. As an aside, Luc Devroye (a true typography
     * expert) called Iosevka
     * <a href="http://luc.devroye.org/fonts-82704.html">"A tour de force that deserves an award."</a>
     * <br>
     * Preview: <a href="https://i.imgur.com/42rMRz5.png">Image link</a>
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * multi-channel distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-msdf.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-msdf.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font Iosevka.ttf
     */
    public static TextCellFactory getCrispLeanFont()
    {
        initialize();
        if(instance.msdfLean == null)
        {
            try {
                instance.msdfLean = new TextCellFactory()
                        .fontMultiDistanceField(crispLean, crispLeanTexture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.msdfLean != null)
            return instance.msdfLean.copy();
        return null;
    }
    /**
     * Returns a TextCellFactory already configured to use a highly-legible oblique (similar to italic) fixed-width font
     * with good Unicode support and a sans-serif geometric style, that should scale cleanly to even very large sizes
     * (using an MSDF technique). Caches the result for later calls. The font used is Iosevka with Oblique style,
     * an open-source (SIL Open Font License) typeface by Belleve Invis (see https://be5invis.github.io/Iosevka/ ), and
     * it uses several customizations thanks to Iosevka's special build process. It supports a lot of glyphs, including
     * quite a bit of extended Latin, Greek, and Cyrillic, but also the necessary box drawing characters. This uses the
     * Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field technique,
     * which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes. As an aside,
     * Luc Devroye (a true typography expert) called Iosevka
     * <a href="http://luc.devroye.org/fonts-82704.html">"A tour de force that deserves an award."</a>
     * <br>
     * Preview: <a href="https://i.imgur.com/EfGBgdM.png">Image link</a>
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * multi-channel distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Oblique-msdf.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Oblique-msdf.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font Iosevka-Oblique.ttf
     */
    public static TextCellFactory getCrispLeanItalicFont()
    {
        initialize();
        if(instance.msdfLeanItalic == null)
        {
            try {
                instance.msdfLeanItalic = new TextCellFactory()
                        .fontMultiDistanceField(crispLeanItalic, crispLeanItalicTexture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.msdfLeanItalic != null)
            return instance.msdfLeanItalic.copy();
        return null;
    }
    /**
     * Returns a TextCellFactory already configured to use a fixed-width sans-serif font with excellent Unicode support,
     * that should scale cleanly to even very large sizes. Caches the result for later calls. The font used is DejaVu
     * Sans Mono, an open-source (SIL Open Font License) typeface that is widely used by Linux distros and other groups.
     * It supports a lot of glyphs, including quite a bit of extended Latin, Greek, and Cyrillic, but also the
     * necessary box drawing characters. This uses the Multi-channel Signed Distance Field technique as opposed to the
     * normal Signed Distance Field technique, which should allow sharper edges. It has been updated so it is laid out
     * mostly-correctly now; some glyphs may be a little wide, such as {@code @}, but the line height, baseline, and the
     * width for most glyphs seem correct. You may need to tweak the size more for width than for height (recommended to
     * have box drawing characters line up is, after giving an initially-sized TextCellFactory to a class like
     * SquidLayers, SparseLayers, or SquidPanel, to call {@link TextCellFactory#tweakWidth(float)} with 1.125f times the
     * original width and {@link TextCellFactory#tweakHeight(float)} with 1.075f times the original height).
     * <br>
     * Preview: <a href="https://i.imgur.com/SCwhduv.png">Image link</a>
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * multi-channel distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/DejaVuSansMono-msdf.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/DejaVuSansMono-msdf.png</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font DejaVuSansMono.ttf with an MSDF effect
     */
    public static TextCellFactory getCrispDejaVuFont()
    {
        initialize();
        if(instance.msdfDejaVu == null)
        {
            try {
                instance.msdfDejaVu = new TextCellFactory()
                        .fontMultiDistanceField(crispDejaVu, crispDejaVuTexture).setSmoothingMultiplier(1f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.msdfDejaVu != null)
            return instance.msdfDejaVu.copy();
        return null;
    }
//    /**
//     * DO NOT USE YET; this will not be laid-out correctly by TextCellFactory without future changes.
//     * <br>
//     * Returns a TextCellFactory already configured to use a fixed-width sans-serif oblique (similar to italic) font
//     * with good Unicode support, that should scale cleanly to even very large sizes. Caches the result for later calls.
//     * The font used is DejaVu Sans Mono, an open-source (SIL Open Font License) typeface that is widely used by Linux
//     * distros and other groups. It supports a lot of glyphs, including quite a bit of extended Latin, Greek, and Cyrillic,
//     * but also the necessary box drawing characters. This uses the Multi-channel Signed Distance Field technique as
//     * opposed to the normal Signed Distance Field technique, which should allow sharper edges.
//     * <br>
//     * NOTE: This currently has some errors on a few Greek lower-case letters with accents, where a large blob appears
//     * above and overlapping with the accents. Un-accented Greek letters should be fine.
//     * <br>
//     * Preview: none yet
//     * <br>
//     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
//     * multi-channel distance field font technique this uses can work.
//     * <br>
//     * Needs files (THESE ARE NOT YET AVAILABLE AT THESE LINKS BECAUSE THEY ARE NOT READY):
//     * <ul>
//     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/DejaVuSansMono-Oblique-msdf.fnt</li>
//     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/DejaVuSansMono-Oblique-msdf.png</li>
//     * </ul>
//     * @return the TextCellFactory object that can represent many sizes of the font DejaVuSansMono-Oblique.ttf
//     */
//
//    public static TextCellFactory getCrispDejaVuItalicFont()
//    {
//        initialize();
//        if(instance.msdfDejaVuItalic == null)
//        {
//            try {
//                instance.msdfDejaVuItalic = new TextCellFactory()
//                        .fontMultiDistanceField(crispDejaVuItalic, crispDejaVuItalicTexture);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if(instance.msdfDejaVuItalic != null)
//            return instance.msdfDejaVuItalic.copy();
//        return null;
//    }
    /**
     * Returns a TextFamily already configured to use a highly-legible fixed-width font with good Unicode support and a
     * slab-serif geometric style, that should scale cleanly to many sizes and supports 4 styles (regular, bold, italic,
     * and bold italic). Caches the result for later calls. The font used is Iosevka with Slab style, an open-source
     * (SIL Open Font License) typeface by Belleve Invis (see https://be5invis.github.io/Iosevka/ ), and it uses several
     * customizations thanks to Iosevka's special build process, applied to the 4 styles. It supports a lot of glyphs,
     * including quite a bit of extended Latin, Greek, and Cyrillic, but also circled letters and digits and the
     * necessary box drawing characters (which line up even for italic text). The high glyph count means the part of the
     * image for each glyph is smaller, though, so this may look slightly pixelated if it starts small and is resized to
     * much larger. A cell width of 11 and cell height of 20 is ideal (or some approximate multiple of that aspect
     * ratio); this allows the font to resize fairly well to larger sizes using Viewports. As an aside, Luc Devroye (a
     * true typography expert) called Iosevka <a href="http://luc.devroye.org/fonts-82704.html">"A tour de force that
     * deserves an award."</a> You may want to try using both this version of Iosevka with slab serifs and the other
     * version SquidLib has, {@link #getLeanFamily()}.
     * <br>
     * <br>
     * This creates a TextFamily instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work, but it can also be used as a TextCellFactory.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Slab-Family-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Slab-Family-distance.png</li>
     * </ul>
     * @return the TextFamily object that can represent many sizes of the font Iosevka-Slab.ttf with 4 styles
     */
    public static TextFamily getSlabFamily()
    {
        initialize();
        if(instance.familySlab == null)
        {
            try {
                instance.familySlab = new TextFamily().defaultFamilySlabDistance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.familySlab != null)
            return instance.familySlab.copy();
        return null;
    }
    /**
     * Returns a TextFamily already configured to use a highly-legible fixed-width font with good Unicode support and a
     * sans-serif geometric style, that should scale cleanly to many sizes and supports 4 styles (regular, bold, italic,
     * and bold italic). Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font
     * License) typeface by Belleve Invis (see https://be5invis.github.io/Iosevka/ ), and it uses several customizations
     * thanks to Iosevka's special build process, applied to the 4 styles. It supports a lot of glyphs, including quite
     * a bit of extended Latin, Greek, and Cyrillic, but also circled letters and digits and the necessary box drawing
     * characters (which line up even for italic text). The high glyph count means the part of the image for each glyph
     * is smaller, though, so this may look slightly pixelated if it starts small and is resized to much larger. A cell
     * width of 11 and cell height of 20 is ideal (or some approximate multiple of that aspect ratio); this allows the
     * font to resize fairly well to larger sizes using Viewports. As an aside, Luc Devroye (a true typography expert)
     * called Iosevka <a href="http://luc.devroye.org/fonts-82704.html">"A tour de force that deserves an award."</a>
     * You may want to try using both this version of Iosevka without serifs and the other version SquidLib has,
     * {@link #getSlabFamily()}.
     * <br>
     * <br>
     * This creates a TextFamily instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work, but it can also be used as a TextCellFactory.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Family-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Family-distance.png</li>
     * </ul>
     * @return the TextFamily object that can represent many sizes of the font Iosevka.ttf with 4 styles
     */
    public static TextFamily getLeanFamily()
    {
        initialize();
        if(instance.familyLean == null)
        {
            try {
                instance.familyLean = new TextFamily().defaultFamilyLeanDistance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.familyLean != null)
            return instance.familyLean.copy();
        return null;
    }
    /**
     * Returns a TextFamily already configured to use a fixed-width font with good Unicode support and a traditional
     * serif style, that should scale cleanly to many sizes and supports 4 styles (regular, bold, italic,
     * and bold italic). Caches the result for later calls. The font used is Go Mono, a typeface released by the team
     * behind the Go programming language as open-source (3-clause BSD, the same license as the Go language; the license
     * is included with the assets). It supports a lot of glyphs, including quite a bit of extended Latin, Greek, and
     * Cyrillic, bu also the necessary box drawing characters (which line up even for italic text). There's more
     * information about this font available where it was introduced,
     * <a href="https://blog.golang.org/go-fonts">on the Go blog</a>.
     * You may want to try using this Go font family and contrasting it with Iosevka with and without slab serifs, which
     * are {@link #getSlabFamily()} and {@link #getLeanFamily()} respectively.
     * <br>
     * <br>
     * This creates a TextFamily instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work, but it can also be used as a TextCellFactory.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/GoMono-Family-distance.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/GoMono-Family-distance.png</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/GoMono-License.txt</li>
     * </ul>
     * @return the TextFamily object that can represent many sizes of the font GoMono.ttf with 4 styles
     */
    public static TextFamily getGoFamily()
    {
        initialize();
        if(instance.familyGo == null)
        {
            try {
                instance.familyGo = new TextFamily();
                instance.familyGo.fontDistanceField("GoMono-Family-distance.fnt", "GoMono-Family-distance.png");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.familyGo != null)
            return instance.familyGo.copy();
        return null;
    }
    /**
     * Returns a TextFamily already configured to use a highly-legible fixed-width font with good Unicode support and a
     * sans-serif geometric style, that should scale cleanly to many sizes (using an MSDF technique) and supports 4
     * styles (regular, bold, italic, and bold italic). Caches the result for later calls. The font used is Iosevka, an
     * open-source (SIL Open Font License) typeface by Belleve Invis (see https://be5invis.github.io/Iosevka/ ), and it 
     * uses several customizations thanks to Iosevka's special build process, applied to the 4 styles. It supports a lot
     * of glyphs, including quite a bit of extended Latin, Greek, and Cyrillic, but also circled letters and digits and
     * the necessary box drawing characters (which line up even for italic text). The high glyph count means the texture
     * that holds all four faces of the font is larger than normal, at 4096x4096; this may be too large for some devices
     * to load correctly (mostly older phones or tablets). A cell width of 11 and cell height of 20 is ideal (or some
     * approximate multiple of that aspect ratio); this allows the font to resize fairly well to larger sizes using
     * Viewports.  This uses the Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed
     * Distance Field technique, which gives the rendered font sharper edges and precise corners instead of rounded tips
     * on strokes. As an aside, Luc Devroye (a true typography expert) called Iosevka
     * <a href="http://luc.devroye.org/fonts-82704.html">"A tour de force that deserves an award."</a> You may want to
     * try using both this version of Iosevka without serifs and the other version SquidLib has with an MSDF effect, 
     * {@link #getCrispSlabFamily()}.
     * <br>
     * Preview: <a href="https://i.imgur.com/dMVzpEi.png">image link</a>, with bold at the bottom.
     * <br>
     * This creates a TextFamily instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work, but it can also be used as a TextCellFactory.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Family-msdf.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Family-msdf.png</li>
     * </ul>
     * @return the TextFamily object that can represent many sizes of the font Iosevka.ttf with 4 styles and an MSDF effect
     */
    public static TextFamily getCrispLeanFamily()
    {
        initialize();
        if(instance.familyLeanMSDF == null)
        {
            try {
                instance.familyLeanMSDF = new TextFamily();
                instance.familyLeanMSDF.fontMultiDistanceField("Iosevka-Family-msdf.fnt", "Iosevka-Family-msdf.png");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.familyLeanMSDF != null)
            return instance.familyLeanMSDF.copy();
        return null;

    }

    /**
     * Returns a TextFamily already configured to use a highly-legible fixed-width font with good Unicode support and a
     * slab-serif geometric style, that should scale cleanly to many sizes (using an MSDF technique) and supports 4
     * styles (regular, bold, italic, and bold italic). Caches the result for later calls. The font used is Iosevka with
     * slab style, an open-source (SIL Open Font License) typeface by Belleve Invis (see
     * https://be5invis.github.io/Iosevka/ ), and it uses several customizations thanks to Iosevka's special build
     * process, applied to the 4 styles. It supports a lot of glyphs, including quite a bit of extended Latin, Greek,
     * and Cyrillic, but also circled letters and digits and the necessary box drawing characters (which line up even 
     * for italic text). The high glyph count means the texture that holds all four faces of the font is larger than
     * normal, at 4096x4096; this may be too large for some devices to load correctly (mostly older phones or tablets).
     * A cell width of 11 and cell height of 20 is ideal (or some approximate multiple of that aspect ratio); this
     * allows the font to resize fairly well to larger sizes using Viewports.  This uses the Multi-channel Signed
     * Distance Field (MSDF) technique as opposed to the normal Signed  Distance Field technique, which gives the
     * rendered font sharper edges and precise corners instead of rounded tips on strokes. As an aside, Luc Devroye (a
     * true typography expert) called Iosevka <a href="http://luc.devroye.org/fonts-82704.html">"A tour de force that
     * deserves an award."</a> You may want to try using both this version of Iosevka with slab serifs and the other
     * version SquidLib has with an MSDF effect, {@link #getCrispLeanFamily()}.
     * <br>
     * This TextFamily has its smoothing multiplier set to 1.55 instead of the default 1.2, which helps it stay sharp.
     * <br>
     * Preview: <a href="https://i.imgur.com/wRNlpL5.png">image link</a>, with bold at the bottom.
     * <br>
     * This creates a TextFamily instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work, but it can also be used as a TextCellFactory.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Slab-Family-msdf.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Iosevka-Slab-Family-msdf.png</li>
     * </ul>
     * @return the TextFamily object that can represent many sizes of the font Iosevka-Slab.ttf with 4 styles and an MSDF effect
     */
    public static TextFamily getCrispSlabFamily()
    {
        initialize();
        if(instance.familySlabMSDF == null)
        {
            try {
                instance.familySlabMSDF = new TextFamily();
                instance.familySlabMSDF
                        .fontMultiDistanceField("Iosevka-Slab-Family-msdf.fnt", "Iosevka-Slab-Family-msdf.png")
                        .setSmoothingMultiplier(1.55f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.familySlabMSDF != null)
            return instance.familySlabMSDF.copy();
        return null;

    }
    /**
     * Returns a TextFamily already configured to use a variable-width serif font that should look like the serif
     * fonts used in many novels' main texts, and that should scale cleanly to many sizes using an MSDF technique, and
     * supports 4 styles (regular, bold, italic, and bold italic). Caches the result for later calls. The font used is 
     * Noto Serif, which is OFL-licensed by Google, and looks very legible in normal use. Meant to be used in
     * variable-width displays like TextPanel.
     * <br>
     * Preview: <a href="https://i.imgur.com/WsDqSfJ.png">In the foreground message box in ZoneDemo</a>
     * <br>
     * This creates a TextFamily instead of a BitmapFont because it needs to set some extra information so the
     * distance field font technique this uses can work, but it can also be used as a TextCellFactory.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/NotoSerif-Family-msdf.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/NotoSerif-Family-msdf.png</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/NotoSerif-license.txt</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font Noto Serif, made available by Google
     */
    public static TextFamily getCrispPrintFamily() {
        initialize();
        if (instance.familyPrintMSDF == null) {
            try {
                instance.familyPrintMSDF = new TextFamily();
                instance.familyPrintMSDF.fontMultiDistanceField(crispNotoSerif, crispNotoSerifTexture)
                        .width(12).height(30).setSmoothingMultiplier(1.5f);
                instance.familyPrintMSDF.font().setUseIntegerPositions(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.familyPrintMSDF != null)
            return instance.familyPrintMSDF.copy();
        return null;
    }
    /**
     * Returns a TextCellFactory already configured to use a partially-angular variable-width font with good Unicode
     * support and an appearance as if it were carved into solid rock, that should scale cleanly to even very large
     * sizes (using an MSDF technique). Caches the result for later calls. The font used is
     * <a href="https://fontstruct.com/fontstructions/show/507930/bloccus">Bloccus by Christian Munk</a>. It supports a
     * lot of glyphs, including most of extended Latin, Greek, Cyrillic, and the International Phonetic Alphabet (IPA),
     * but not box drawing characters because this is variable-width. This uses the Multi-channel Signed Distance
     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
     * sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/zaqgHTW.png">Image link</a>
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * multi-channel distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/bloccus-msdf.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/bloccus-msdf.png</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/bloccus-license.txt</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of the font bloccus.ttf
     */
    public static TextCellFactory getCrispCarvedFont()
    {
        initialize();
        if(instance.msdfCarved == null)
        {
            try {
                instance.msdfCarved = new TextCellFactory()
                        .fontMultiDistanceField(crispCarved, crispCarvedTexture)
                        .width(16f).height(32f).setSmoothingMultiplier(1.5f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.msdfCarved != null)
            return instance.msdfCarved.copy();
        return null;
    }

    /**
     * Returns a TextCellFactory already configured to use a fixed-width icon font (no letters are supported) using the
     * Font-Awesome icon set, that should scale cleanly to even very large sizes. Caches the result for later calls.
     * The icon font used is the popular Font-Awesome free set (solid style), an open-source (SIL Open Font License)
     * typeface that is often used online. You will probably want to consult the cheatsheet for what chars are actually
     * supported; <a href="https://fontawesome.com/cheatsheet">the cheatsheet is here</a>. This uses the Multi-channel
     * Signed Distance Field technique as opposed to the normal Signed Distance Field technique, which should allow
     * sharper edges. You may also want the list of all chars in this icon font; {@link #iconFontAll} has that.
     * <br>
     * Preview: <a href="">Image link</a>
     * <br>
     * This creates a TextCellFactory instead of a BitmapFont because it needs to set some extra information so the
     * multi-channel distance field font technique this uses can work.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/awesome-solid-msdf.fnt</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/awesome-solid-msdf.png</li>
     *     <li>https://github.com/SquidPony/SquidLib/blob/master/assets/Font-Awesome-license.txt</li>
     * </ul>
     * @return the TextCellFactory object that can represent many sizes of Font Awesome Free (solid) with an MSDF effect
     */
    public static TextCellFactory getCrispIconFont()
    {
        initialize();
        if(instance.msdfIcons == null)
        {
            try {
                instance.msdfIcons = new TextCellFactory()
                        .fontMultiDistanceField(crispIcons, crispIconsTexture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.msdfIcons != null)
            return instance.msdfIcons.copy();
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
    public static final String narrowFontSymbols = "ሀሁሂሃሄህሆሇለሉሊላሌልሎሏሐሑሒሓሔሕሖሗመሙሚማሜ";
    public static final String narrowFontAll = " !\"#$%&'()*+,-./0123\n" +
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
     * All of the Font-Awesome Free (solid style) characters in {@link #getCrispIconFont()}, as a String.
     */
    public static final String iconFontAll = "\u200E\uF000\uF001\uF002\uF004\uF005\uF007\uF008\uF009\uF00A\uF00B\uF00C\uF00D\uF00E\uF010\uF011\uF012\uF013\uF015\uF017\uF018\uF019\uF01C\uF01E\uF021\uF022\uF023\uF024\uF025\uF026\uF027\uF028\uF029\uF02A\uF02B\uF02C\uF02D\uF02E\uF02F\uF030\uF031\uF032\uF033\uF034\uF035\uF036\uF037\uF038\uF039\uF03A\uF03B\uF03C\uF03D\uF03E\uF041\uF042\uF043\uF044\uF048\uF049\uF04A\uF04B\uF04C\uF04D\uF04E\uF050\uF051\uF052\uF053\uF054\uF055\uF056\uF057\uF058\uF059\uF05A\uF05B\uF05E\uF060\uF061\uF062\uF063\uF064\uF065\uF066\uF067\uF068\uF069\uF06A\uF06B\uF06C\uF06D\uF06E\uF070\uF071\uF072\uF073\uF074\uF075\uF076\uF077\uF078\uF079\uF07A\uF07B\uF07C\uF080\uF083\uF084\uF085\uF086\uF089\uF08D\uF091\uF093\uF094\uF095\uF098\uF09C\uF09D\uF09E\uF0A0\uF0A1\uF0A3\uF0A4\uF0A5\uF0A6\uF0A7\uF0A8\uF0A9\uF0AA\uF0AB\uF0AC\uF0AD\uF0AE\uF0B0\uF0B1\uF0B2\uF0C0\uF0C1\uF0C2\uF0C3\uF0C4\uF0C5\uF0C6\uF0C7\uF0C8\uF0C9\uF0CA\uF0CB\uF0CC\uF0CD\uF0CE\uF0D0\uF0D1\uF0D6\uF0D7\uF0D8\uF0D9\uF0DA\uF0DB\uF0DC\uF0DD\uF0DE\uF0E0\uF0E2\uF0E3\uF0E7\uF0E8\uF0E9\uF0EA\uF0EB\uF0F0\uF0F1\uF0F2\uF0F3\uF0F4\uF0F8\uF0F9\uF0FA\uF0FB\uF0FC\uF0FD\uF0FE\uF100\uF101\uF102\uF103\uF104\uF105\uF106\uF107\uF108\uF109\uF10A\uF10B\uF10D\uF10E\uF110\uF111\uF118\uF119\uF11A\uF11B\uF11C\uF11E\uF120\uF121\uF122\uF124\uF125\uF126\uF127\uF128\uF129\uF12A\uF12B\uF12C\uF12D\uF12E\uF130\uF131\uF133\uF134\uF135\uF137\uF138\uF139\uF13A\uF13D\uF13E\uF140\uF141\uF142\uF143\uF144\uF146\uF14A\uF14B\uF14D\uF14E\uF150\uF151\uF152\uF153\uF154\uF155\uF156\uF157\uF158\uF159\uF15B\uF15C\uF15D\uF15E\uF160\uF161\uF162\uF163\uF164\uF165\uF182\uF183\uF185\uF186\uF187\uF188\uF191\uF192\uF193\uF195\uF197\uF199\uF19C\uF19D\uF1AB\uF1AC\uF1AD\uF1AE\uF1B0\uF1B2\uF1B3\uF1B8\uF1B9\uF1BA\uF1BB\uF1C0\uF1C1\uF1C2\uF1C3\uF1C4\uF1C5\uF1C6\uF1C7\uF1C8\uF1C9\uF1CD\uF1CE\uF1D8\uF1DA\uF1DC\uF1DD\uF1DE\uF1E0\uF1E1\uF1E2\uF1E3\uF1E4\uF1E5\uF1E6\uF1EA\uF1EB\uF1EC\uF1F6\uF1F8\uF1F9\uF1FA\uF1FB\uF1FC\uF1FD\uF1FE\uF200\uF201\uF204\uF205\uF206\uF207\uF20A\uF20B\uF217\uF218\uF21A\uF21B\uF21C\uF21D\uF21E\uF221\uF222\uF223\uF224\uF225\uF226\uF227\uF228\uF229\uF22A\uF22B\uF22C\uF22D\uF233\uF234\uF235\uF236\uF238\uF239\uF240\uF241\uF242\uF243\uF244\uF245\uF246\uF247\uF248\uF249\uF24D\uF24E\uF251\uF252\uF253\uF254\uF255\uF256\uF257\uF258\uF259\uF25A\uF25B\uF25C\uF25D\uF26C\uF271\uF272\uF273\uF274\uF275\uF276\uF277\uF279\uF27A\uF28B\uF28D\uF290\uF291\uF292\uF295\uF29A\uF29D\uF29E\uF2A0\uF2A1\uF2A2\uF2A3\uF2A4\uF2A7\uF2A8\uF2B5\uF2B6\uF2B9\uF2BB\uF2BD\uF2C1\uF2C2\uF2C7\uF2C8\uF2C9\uF2CA\uF2CB\uF2CC\uF2CD\uF2CE\uF2D0\uF2D1\uF2D2\uF2DB\uF2DC\uF2E5\uF2E7\uF2EA\uF2ED\uF2F1\uF2F2\uF2F5\uF2F6\uF2F9\uF2FE\uF302\uF303\uF304\uF305\uF309\uF30A\uF30B\uF30C\uF31E\uF328\uF337\uF338\uF358\uF359\uF35A\uF35B\uF35D\uF360\uF362\uF381\uF382\uF3A5\uF3BE\uF3BF\uF3C1\uF3C5\uF3C9\uF3CD\uF3D1\uF3DD\uF3E0\uF3E5\uF3ED\uF3FA\uF3FD\uF3FF\uF406\uF410\uF433\uF434\uF436\uF439\uF43A\uF43C\uF43F\uF441\uF443\uF445\uF447\uF44B\uF44E\uF450\uF453\uF458\uF45C\uF45D\uF45F\uF461\uF462\uF466\uF468\uF469\uF46A\uF46B\uF46C\uF46D\uF470\uF471\uF472\uF474\uF477\uF478\uF479\uF47D\uF47E\uF47F\uF481\uF482\uF484\uF485\uF486\uF487\uF48B\uF48D\uF48E\uF490\uF491\uF492\uF493\uF494\uF496\uF497\uF49E\uF4AD\uF4B3\uF4B8\uF4B9\uF4BA\uF4BD\uF4BE\uF4C0\uF4C2\uF4C4\uF4CD\uF4CE\uF4D3\uF4D6\uF4D7\uF4D8\uF4D9\uF4DA\uF4DB\uF4DE\uF4DF\uF4E2\uF4E3\uF4FA\uF4FB\uF4FC\uF4FD\uF4FE\uF4FF\uF500\uF501\uF502\uF503\uF504\uF505\uF506\uF507\uF508\uF509\uF517\uF518\uF519\uF51A\uF51B\uF51C\uF51D\uF51E\uF51F\uF520\uF521\uF522\uF523\uF524\uF525\uF526\uF527\uF528\uF529\uF52A\uF52B\uF52C\uF52D\uF52E\uF52F\uF530\uF531\uF532\uF533\uF534\uF535\uF536\uF537\uF538\uF539\uF53A\uF53B\uF53C\uF53D\uF53E\uF53F\uF540\uF541\uF542\uF543\uF544\uF545\uF546\uF547\uF548\uF549\uF54A\uF54B\uF54C\uF54D\uF54E\uF54F\uF550\uF551\uF552\uF553\uF554\uF555\uF556\uF557\uF558\uF559\uF55A\uF55B\uF55C\uF55D\uF55E\uF55F\uF560\uF561\uF562\uF563\uF564\uF565\uF566\uF567\uF568\uF569\uF56A\uF56B\uF56C\uF56D\uF56E\uF56F\uF570\uF571\uF572\uF573\uF574\uF575\uF576\uF577\uF578\uF579\uF57A\uF57B\uF57C\uF57D\uF57E\uF57F\uF580\uF581\uF582\uF583\uF584\uF585\uF586\uF587\uF588\uF589\uF58A\uF58B\uF58C\uF58D\uF58E\uF58F\uF590\uF591\uF593\uF594\uF595\uF596\uF597\uF598\uF599\uF59A\uF59B\uF59C\uF59D\uF59F\uF5A0\uF5A1\uF5A2\uF5A4\uF5A5\uF5A6\uF5A7\uF5AA\uF5AB\uF5AC\uF5AD\uF5AE\uF5AF\uF5B0\uF5B1\uF5B3\uF5B4\uF5B6\uF5B7\uF5B8\uF5BA\uF5BB\uF5BC\uF5BD\uF5BF\uF5C0\uF5C1\uF5C2\uF5C3\uF5C4\uF5C5\uF5C7\uF5C8\uF5C9\uF5CA\uF5CB\uF5CD\uF5CE\uF5D0\uF5D1\uF5D2\uF5D7\uF5DA\uF5DC\uF5DE\uF5DF\uF5E1\uF5E4\uF5E7\uF5EB\uF5EE\uF5FC\uF5FD\uF610\uF613\uF619\uF61F\uF621\uF62E\uF62F\uF630\uF637\uF63B\uF63C\uF641\uF644\uF647\uF64A\uF64F\uF651\uF653\uF654\uF655\uF658\uF65D\uF65E\uF662\uF664\uF665\uF666\uF669\uF66A\uF66B\uF66D\uF66F\uF674\uF676\uF678\uF679\uF67B\uF67C\uF67F\uF681\uF682\uF683\uF684\uF687\uF688\uF689\uF696\uF698\uF699\uF69A\uF69B\uF6A0\uF6A1\uF6A7\uF6AD";
    
    /**
     * Called when the {@link Application} is about to pause
     */
    @Override
    public void pause() {
    }

    /**
     * Called when the Application is about to be resumed
     */
    @Override
    public void resume() {
    }

    /**
     * Called when the {@link Application} is about to be disposed
     */
    @Override
    public void dispose() {
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
            square2 = null;
        }
        if(smoothSquare != null) {
            smoothSquare.dispose();
            smoothSquare = null;
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
        if(distanceCode != null) {
            distanceCode.dispose();
            distanceCode = null;
        }
        if(distanceCodeJP != null) {
            distanceCodeJP.dispose();
            distanceCodeJP = null;
        }
        if(distanceDejaVu != null) {
            distanceDejaVu.dispose();
            distanceDejaVu = null;
        }
        if(distanceLean != null) {
            distanceLean.dispose();
            distanceLean = null;
        }
        if(distanceSlab != null) {
            distanceSlab.dispose();
            distanceSlab = null;
        }
        if(distanceLeanLight != null) {
            distanceLeanLight.dispose();
            distanceLeanLight = null;
        }
        if(distanceSlabLight != null) {
            distanceSlabLight.dispose();
            distanceSlabLight = null;
        }
        if(distanceClean != null) {
            distanceClean.dispose();
            distanceClean = null;
        }
        if(distancePrint != null) {
            distancePrint.dispose();
            distancePrint = null;
        }
        if(distanceOrbit != null) {
            distanceOrbit.dispose();
            distanceOrbit = null;
        }
        if(distanceHeavySquare != null) {
            distanceHeavySquare.dispose();
            distanceHeavySquare = null;
        }
        if(msdfSlab != null) {
            msdfSlab.dispose();
            msdfSlab = null;
        }
        if(msdfSlabItalic != null) {
            msdfSlabItalic.dispose();
            msdfSlabItalic = null;
        }
        if(msdfLean != null) {
            msdfLean.dispose();
            msdfLean = null;
        }
        if(msdfLeanItalic != null) {
            msdfLeanItalic.dispose();
            msdfLeanItalic = null;
        }
        if(msdfDejaVu != null) {
            msdfDejaVu.dispose();
            msdfDejaVu = null;
        }
        if(msdfCarved != null) {
            msdfCarved.dispose();
            msdfCarved = null;
        }
        if(msdfCurvySquare != null) {
            msdfCurvySquare.dispose();
            msdfCurvySquare = null;
        }
        if(msdfIcons != null) {
            msdfIcons.dispose();
            msdfIcons = null;
        }
        if (unicode1 != null) {
            unicode1.dispose();
            unicode1 = null;
        }
        if (unicode2 != null) {
            unicode2.dispose();
            unicode2 = null;
        }
        if(tiny != null){
            tiny.dispose();
            tiny = null;
        }
        if(lessTiny != null){
            lessTiny.dispose();
            lessTiny = null;
        }
        if(familyLean != null) {
            familyLean.dispose();
            familyLean = null;
        }
        if(familySlab != null) {
            familySlab.dispose();
            familySlab = null;
        }
        if(familyGo != null) {
            familyGo.dispose();
            familyGo = null;
        }
        if(familyLeanMSDF != null) {
            familyLeanMSDF.dispose();
            familyLeanMSDF = null;
        }
        if(familySlabMSDF != null) {
            familySlabMSDF.dispose();
            familySlabMSDF = null;
        }
        if(familyPrintMSDF != null) {
            familyPrintMSDF.dispose();
            familyPrintMSDF = null;
        }
        if(tentacle != null) {
            tentacle.dispose();
            tentacle = null;
        }
        if(iconAtlas != null) {
            iconAtlas.dispose();
            iconAtlas = null;
        }
        Gdx.app.removeLifecycleListener(this);
        instance = null;
    }
}
