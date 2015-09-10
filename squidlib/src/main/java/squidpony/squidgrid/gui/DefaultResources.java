package squidpony.squidgrid.gui;

import java.awt.*;

/**
 * Created by Tommy Ettinger on 7/11/2015.
 */
public class DefaultResources {
    private static Font narrow = null, square = null, unicode = null;

    /**
     * Returns a 12x12px, stretched but curvaceous font as an embedded resource. Caches it for later calls.
     * @return the Font object representing Zodiac-Square.ttf at size 12 pt.
     */
    public static Font getDefaultFont()
    {
        if(square == null || square.getSize() != 12)
        {
            square = new Font("Dialog", Font.PLAIN, 12);
            try {
                square = Font.createFont(Font.TRUETYPE_FONT, DefaultResources.class.getResourceAsStream("/Zodiac-Square.ttf")).deriveFont(12.0f);
            } catch (Exception e) {
            }
        }
        return square;
    }
    /**
     * Returns a stretched but curvaceous font as an embedded resource. Multiplies the default size of 12 pt, 12x12 px
     * by the given sizeMultiplier, which should be rounded off if you want pixel precision. Caches it for later calls.
     * @return the Font object representing Zodiac-Square.ttf at size 12 point times sizeMultiplier.
     */
    public static Font getDefaultFont(float sizeMultiplier)
    {
        if(square == null || square.getSize() != Math.round(12 * sizeMultiplier))
        {
            square = new Font("Dialog", Font.PLAIN, 12);
            try {
                square = Font.createFont(Font.TRUETYPE_FONT, DefaultResources.class.getResourceAsStream("/Zodiac-Square.ttf")).deriveFont(12.0f * sizeMultiplier);
            } catch (Exception e) {
            }
        }
        return square;
    }
    /**
     * Returns a 6x12px, narrow and curving font as an embedded resource. Caches it for later calls.
     * @return the Font object representing Rogue-Zodiac.ttf at size 12 pt.
     */
    public static Font getDefaultNarrowFont()
    {
        if(narrow == null || narrow.getSize() != 12)
        {
            narrow = new Font("Dialog", Font.PLAIN, 12);
            try {
                narrow = Font.createFont(Font.TRUETYPE_FONT, DefaultResources.class.getResourceAsStream("/Rogue-Zodiac.ttf")).deriveFont(12.0f);
            } catch (Exception e) {
            }
        }
        return narrow;
    }
    /**
     * Returns a narrow and curving font as an embedded resource. Multiplies the default size of 12 pt, 6x12 px
     * by the given sizeMultiplier, which should be rounded off if you want pixel precision. Caches it for later calls.
     * @return the Font object representing Rogue-Zodiac.ttf at size 12 pt times sizeMultiplier.
     */
    public static Font getDefaultNarrowFont(float sizeMultiplier)
    {
        if(narrow == null || narrow.getSize() != Math.round(12 * sizeMultiplier))
        {
            narrow = new Font("Dialog", Font.PLAIN, 12);
            try {
                narrow = Font.createFont(Font.TRUETYPE_FONT, DefaultResources.class.getResourceAsStream("/Rogue-Zodiac.ttf")).deriveFont(12.0f * sizeMultiplier);
            } catch (Exception e) {
            }
        }
        return narrow;
    }
    /**
     * Returns a 6x16px, narrow and curving font as an embedded resource. Caches it for later calls.
     * @return the Font object representing Mandrill.ttf at size 12 pt.
     */
    public static Font getDefaultUnicodeFont()
    {
        if(unicode == null || unicode.getSize() != 12)
        {
            unicode = new Font("Dialog", Font.PLAIN, 12);
            try {
                unicode = Font.createFont(Font.TRUETYPE_FONT, DefaultResources.class.getResourceAsStream("/Mandrill.ttf")).deriveFont(12.0f);
            } catch (Exception e) {
            }
        }
        return unicode;
    }
    /**
     * Returns a narrow and curving font as an embedded resource. Multiplies the default size of 12 pt, 6x16 px
     * by the given sizeMultiplier, which should be rounded off if you want pixel precision. Caches it for later calls.
     * @return the Font object representing Mandrill.ttf at size 12 pt times sizeMultiplier.
     */
    public static Font getDefaultUnicodeFont(float sizeMultiplier)
    {
        if(unicode == null || unicode.getSize() != Math.round(12 * sizeMultiplier))
        {
            unicode = new Font("Dialog", Font.PLAIN, 12);
            try {
                unicode = Font.createFont(Font.TRUETYPE_FONT, DefaultResources.class.getResourceAsStream("/Mandrill.ttf")).deriveFont(12.0f * sizeMultiplier);
            } catch (Exception e) {
            }
        }
        return unicode;
    }

    /**
     * Special symbols that can be used as icons if you use the narrow default font.
     */
    public static final String narrowFontSymbols = "ሀሁሂሃሄህሆሇለሉሊላሌልሎሏሐሑሒሓሔሕሖሗመሙሚማሜ";
}
