package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.math.MathUtils;
import regexodus.Matcher;
import regexodus.Pattern;
import squidpony.panel.IColoredString;
import squidpony.panel.IMarkup;

/**
 * GDXMarkup implements IMarkup for libGDX Color objects, and can start blocks of markup that libGDX understands that
 * display text in a given Color. Typically, this class needs only to be instantiated and passed to a class that uses
 * IMarkup of Color, and then anything else will be handled internally as Colors are given to the using class.
 * Created by Tommy Ettinger on 1/23/2016.
 */
public class GDXMarkup implements IMarkup<Color>{
    public static GDXMarkup instance = new GDXMarkup();
    public GDXMarkup()
    {

    }
    private static String floatToHex(float f)
    {
        String s = Integer.toHexString(MathUtils.round(f * 255));
        if(s.length() < 2) return "0" + s;
        else return s;
    }
    @Override
    public String getMarkup(Color value) {
        return "[#" + floatToHex(value.r) + floatToHex(value.g) + floatToHex(value.b) + floatToHex(value.a) + "]";
    }

    @Override
    public String closeMarkup() {
        return "[]";
    }

    Matcher markupMatcher = Pattern.compile("({=p}[^\\[\\]]+)|(?:\\[({=e}\\[))|(?:\\[#({=h}[0-9A-Fa-f]{6,8})\\])|(?:\\[({=n}[^\\]]+?)\\])|(?:\\[({=r}\\]))").matcher();

    public IColoredString<Color> colorString(final CharSequence markupString)
    {
        markupMatcher.setTarget(markupString);
        IColoredString<Color> cs = new IColoredString.Impl<>();
        Color current = Color.WHITE;
        String m;
        while (markupMatcher.find())
        {
            if((m = markupMatcher.group("p")) != null) {
                cs.append(m, current);
            }
            else if((m = markupMatcher.group("e")) != null)
            {
                cs.append('[', current);
            }
            else if((m = markupMatcher.group("r")) != null)
            {
                current = Color.WHITE;
            }
            else if((m = markupMatcher.group("h")) != null)
            {
                current = Color.valueOf(m);
            }
            else if((m = markupMatcher.group("n")) != null)
            {
                current = Colors.get(m);
            }
        }
        return cs;
    }

    /*
    @Override
    public String escape(String initialText)
    {
        return initialText.replace("[", "[[");
    }
    */
}
