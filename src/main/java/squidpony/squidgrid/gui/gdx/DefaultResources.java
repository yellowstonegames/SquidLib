package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * Created by Tommy Ettinger on 7/11/2015.
 */
public class DefaultResources {
    private static FreeTypeFontGenerator narrow = null, square = null;

    /**
     * Returns a 12x12px, stretched but curvaceous font as an embedded resource. Caches it for later calls.
     * @return the Font object representing Zodiac-Square.ttf at size 16 pt.
     */
    public static FreeTypeFontGenerator getDefaultFont()
    {
        if(square == null)
        {
            try {
                square = new FreeTypeFontGenerator(Gdx.files.classpath("/Zodiac-Square.ttf"));
            } catch (Exception e) {
            }
        }
        return square;
    }
    /**
     * Returns a 6x12px, narrow and curving font as an embedded resource. Caches it for later calls.
     * @return the Font object representing Rogue-Zodiac.ttf at size 16 pt.
     */
    public static FreeTypeFontGenerator getDefaultNarrowFont()
    {
        if(narrow == null)
        {
            try {
                narrow = new FreeTypeFontGenerator(Gdx.files.classpath("/Rogue-Zodiac.ttf"));
            } catch (Exception e) {
            }
        }
        return narrow;
    }
}
