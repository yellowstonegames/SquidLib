package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import squidpony.squidmath.LightRNG;

/**
 * Default BitmapFonts for use with LibGDX. Caches these fonts in static fields, so not recommended for use on Android.
 * Created by Tommy Ettinger on 7/11/2015.
 */
public class DefaultResources {
    private static BitmapFont narrow1 = null, narrow2 = null, narrow3 = null, square1 = null, square2 = null,
            unicode1 = null, unicode2 = null;
    public final static String squareName = "Zodiac-Square-12x12.fnt",
            narrowName = "Rogue-Zodiac-6x12.fnt",
            unicodeName = "Mandrill-6x16.fnt",
            squareNameLarge = "Zodiac-Square-24x24.fnt",
            narrowNameLarge = "Rogue-Zodiac-12x24.fnt",
            unicodeNameLarge = "Mandrill-12x32.fnt",
            narrowNameExtraLarge = "Rogue-Zodiac-18x36.fnt";
    public static TextureRegion tentacle = null;
    /**
     * Returns a 12x12px, stretched but curvaceous font as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Zodiac-Square.ttf at size 16 pt.
     */
    public static BitmapFont getDefaultFont()
    {
        if(square1 == null)
        {
            try {
                square1 = new BitmapFont(Gdx.files.classpath("Zodiac-Square-12x12.fnt"), Gdx.files.classpath("Zodiac-Square-12x12.png"), false);
            } catch (Exception e) {
            }
        }
        return square1;
    }
    /**
     * Returns a 24x24px, stretched but curvaceous font as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Zodiac-Square.ttf at size 32 pt.
     */
    public static BitmapFont getLargeFont()
    {
        if(square2 == null)
        {
            try {
                square2 = new BitmapFont(Gdx.files.classpath("Zodiac-Square-24x24.fnt"), Gdx.files.classpath("Zodiac-Square-24x24.png"), false);
            } catch (Exception e) {
            }
        }
        return square2;
    }
    /**
     * Returns a 6x12px, narrow and curving font as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Rogue-Zodiac.ttf at size 16 pt.
     */
    public static BitmapFont getDefaultNarrowFont()
    {
        if(narrow1 == null)
        {
            try {
                narrow1 = new BitmapFont(Gdx.files.classpath("Rogue-Zodiac-6x12.fnt"), Gdx.files.classpath("Rogue-Zodiac-6x12.png"), false);
            } catch (Exception e) {
            }
        }
        return narrow1;
    }

    /**
     * Returns a 12x24px, narrow and curving font as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Rogue-Zodiac.ttf at size 32 pt.
     */
    public static BitmapFont getLargeNarrowFont()
    {
        if(narrow2 == null)
        {
            try {
                narrow2 = new BitmapFont(Gdx.files.classpath("Rogue-Zodiac-12x24.fnt"), Gdx.files.classpath("Rogue-Zodiac-12x24.png"), false);
            } catch (Exception e) {
            }
        }
        return narrow2;
    }
    /**
     * Returns a 12x24px, narrow and curving font as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Rogue-Zodiac.ttf at size 32 pt.
     */
    public static BitmapFont getExtraLargeNarrowFont()
    {
        if(narrow3 == null)
        {
            try {
                narrow3 = new BitmapFont(Gdx.files.classpath("Rogue-Zodiac-18x36.fnt"), Gdx.files.classpath("Rogue-Zodiac-18x36.png"), false);
            } catch (Exception e) {
            }
        }
        return narrow3;
    }

    /**
     * Returns a 6x16px, narrow and curving font with a lot of unicode chars as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Mandrill.ttf at size 16 pt.
     */
    public static BitmapFont getDefaultUnicodeFont()
    {
        if(unicode1 == null)
        {
            try {
                unicode1 = new BitmapFont(Gdx.files.classpath("Mandrill-6x16.fnt"), Gdx.files.classpath("Mandrill-6x16.png"), false);
            } catch (Exception e) {
            }
        }
        return unicode1;
    }

    /**
     * Returns a 12x32px, narrow and curving font with a lot of unicode chars as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Mandrill.ttf at size 32 pt.
     */
    public static BitmapFont getLargeUnicodeFont()
    {
        if(unicode2 == null)
        {
            try {
                unicode2 = new BitmapFont(Gdx.files.classpath("Mandrill-12x32.fnt"), Gdx.files.classpath("Mandrill-12x32.png"), false);
            } catch (Exception e) {
            }
        }
        return unicode2;
    }

    /**
     * Gets an image of a (squid-like, for SquidLib) tentacle, 32x32px.
     * Source is public domain: http://opengameart.org/content/496-pixel-art-icons-for-medievalfantasy-rpg
     * Created by Henrique Lazarini (7Soul1, http://7soul1.deviantart.com/ )
     * @return a TextureRegion containing an image of a tentacle.
     */
    public static TextureRegion getTentacle()
    {
        if(tentacle == null)
        {
            try {
                tentacle = new TextureRegion(new Texture(Gdx.files.classpath("Tentacle.png")));
            } catch (Exception e) {
            }
        }
        return tentacle;
    }

    /**
     * This is a static global LightRNG that's meant for usage in cases where the seed does not matter and any changes
     * to this LightRNG's state will not change behavior elsewhere in the program; this means the GUI mainly.
     */
    public static LightRNG guiRandom = new LightRNG();

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

}
