package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import squidpony.squidmath.LightRNG;

/**
 * Created by Tommy Ettinger on 7/11/2015.
 */
public class DefaultResources {
    private static BitmapFont narrow = null, square = null, unicode = null;
    public final static String squareName = "Zodiac-Square-12x12.fnt",
            narrowName = "Rogue-Zodiac-6x12.fnt",
            unicodeName = "Mandrill-6x16.fnt",
            squareNameLarge = "Zodiac-Square-24x24.fnt",
            narrowNameLarge = "Rogue-Zodiac-12x24.fnt",
            unicodeNameLarge = "Mandrill-12x32.fnt";
    /**
     * Returns a 12x12px, stretched but curvaceous font as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Zodiac-Square.ttf at size 16 pt.
     */
    public static BitmapFont getDefaultFont()
    {
        if(square == null)
        {
            try {
                square = new BitmapFont(Gdx.files.classpath("Zodiac-Square-12x12.fnt"), Gdx.files.classpath("Zodiac-Square-12x12.png"), false);
            } catch (Exception e) {
            }
        }
        return square;
    }
    /**
     * Returns a 12x12px, stretched but curvaceous font as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Zodiac-Square.ttf at size 16 pt.
     */
    public static BitmapFont getLargeFont()
    {
        if(square == null)
        {
            try {
                square = new BitmapFont(Gdx.files.classpath("Zodiac-Square-24x24.fnt"), Gdx.files.classpath("Zodiac-Square-24x24.png"), false);
            } catch (Exception e) {
            }
        }
        return square;
    }
    /**
     * Returns a 6x12px, narrow and curving font as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Rogue-Zodiac.ttf at size 16 pt.
     */
    public static BitmapFont getDefaultNarrowFont()
    {
        if(narrow == null)
        {
            try {
                narrow = new BitmapFont(Gdx.files.classpath("Rogue-Zodiac-6x12.fnt"), Gdx.files.classpath("Rogue-Zodiac-6x12.png"), false);
            } catch (Exception e) {
            }
        }
        return narrow;
    }

    /**
     * Returns a 6x12px, narrow and curving font as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Rogue-Zodiac.ttf at size 16 pt.
     */
    public static BitmapFont getLargeNarrowFont()
    {
        if(narrow == null)
        {
            try {
                narrow = new BitmapFont(Gdx.files.classpath("Rogue-Zodiac-12x24.fnt"), Gdx.files.classpath("Rogue-Zodiac-12x24.png"), false);
            } catch (Exception e) {
            }
        }
        return narrow;
    }

    /**
     * Returns a 6x12px, narrow and curving font with a lot of unicode chars as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Mandrill.ttf at size 16 pt.
     */
    public static BitmapFont getDefaultUnicodeFont()
    {
        if(unicode == null)
        {
            try {
                unicode = new BitmapFont(Gdx.files.classpath("Mandrill-6x16.fnt"), Gdx.files.classpath("Mandrill-6x16.png"), false);
            } catch (Exception e) {
            }
        }
        return unicode;
    }

    /**
     * Returns a 6x12px, narrow and curving font with a lot of unicode chars as an embedded resource. Caches it for later calls.
     * @return the BitmapFont object representing Mandrill.ttf at size 32 pt.
     */
    public static BitmapFont getLargeUnicodeFont()
    {
        if(unicode == null)
        {
            try {
                unicode = new BitmapFont(Gdx.files.classpath("Mandrill-12x32.fnt"), Gdx.files.classpath("Mandrill-12x32.png"), false);
            } catch (Exception e) {
            }
        }
        return unicode;
    }

    /**
     * This is a static global LightRNG that's meant for usage in cases where the seed does not matter and any changes
     * to this LightRNG's state will not change behavior elsewhere in the program; this means the GUI mainly.
     */
    public static LightRNG guiRandom = new LightRNG();

    /**
     * Special symbols that can be used as icons if you use the narrow default font.
     */
    public static final String narrowFontSymbols = "ሀሁሂሃሄህሆሇለሉሊላሌልሎሏሐሑሒሓሔሕሖሗመሙሚማሜ";

}
