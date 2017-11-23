package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.math.MathUtils;
import regexodus.Matcher;
import regexodus.Pattern;
import squidpony.StringKit;
import squidpony.panel.IColoredString;
import squidpony.panel.IMarkup;

/**
 * GDXMarkup implements IMarkup for libGDX Color objects, and can start blocks of markup that libGDX understands that
 * display text in a given Color. Typically, the singleton {@link #instance} would be passed to a class that uses
 * IMarkup of Color, and then anything else will be handled internally as Colors are given to the using class. This does
 * extend GDX's markup to handle bold and italic options for text; this only works if you are using a {@link TextFamily}
 * as your {@link TextCellFactory}, such as {@link DefaultResources#getSlabFamily()}, and only if you use
 * {@link #colorString(CharSequence)} or {@link #styleString(CharSequence)} to generate a value that can be drawn later.
 * <br>
 * The notation for colors is the same as in the rest of libGDX, but if you make an IColoredString with colorString(),
 * it doesn't need any flag to be changed on your BitmapFont (like it does for GDX markup normally). This notation looks
 * like {@code [#FF00FF]Magenta text here[]}, which starts a tag for a hex color, uses the hex value for bright magenta,
 * then contains the text "Magenta text here" which will be shown in bright magenta, followed by "[]" to change the
 * color (and style, see next) back to the default white (and normal style). You can also use the names of colors, as
 * defined in the documentation for every {@link SColor}, and some libGDX Color values as well; this looks like
 * {@code [Inside Of A Bottle]Gray text[]} to produce the words "Gray text" with the color
 * {@link SColor#INSIDE_OF_A_BOTTLE} (note that the docs for that SColor say what the precise name is, and case needs to
 * match; you can also look up the {@link SColor#name} field). You can use {@code [[} to escape an opening bracket, and
 * {@code []} to reset formatting. As an addition to GDX color markup, if using a TextFamily you can toggle the font
 * style as bold with {@code [*]} and as italic with {@code [/]}. If bold is on when this encounters another bold tag,
 * it will turn bold off; the same is true for italic. These formatting styles can overlap and do not need to be nested
 * as in HTML; this notation is valid: {@code [*]bold, [/]bold and italic, [*] just italic,[] plain}. While {@code []}
 * resets both color and style to white color and regular style, there is also {@code [,]} to reset only style, or
 * {@code [WHITE]} to reset only color (to white).
 * <br>
 * Created by Tommy Ettinger on 1/23/2016.
 */
public class GDXMarkup implements IMarkup<Color>{
    public static GDXMarkup instance = new GDXMarkup();
    public GDXMarkup()
    {

    }
    private static String floatToHex(float f)
    {
        //String s = Integer.toHexString(MathUtils.round(f * 255));
        //if(s.length() < 2) return "0" + s;
        //else return s;
        int r = MathUtils.round(f * 255);
        return String.valueOf(StringKit.hexDigits[r >>> 8 & 15]) + StringKit.hexDigits[r & 15];
    }
    @Override
    public String getMarkup(Color value) {
        return "[#" + floatToHex(value.r) + floatToHex(value.g) + floatToHex(value.b) + floatToHex(value.a) + "]";
    }

    @Override
    public String closeMarkup() {
        return "[]";
    }

    private final Matcher markupMatcher = Pattern.compile("({=p}[^\\[]+)|(?:\\[({=e}\\[))|(?:\\[#({=h}[0-9A-Fa-f]{6,8})\\])|(?:\\[({=b}\\*)\\])|(?:\\[({=i}/)\\])|(?:\\[({=u},)\\])|(?:\\[({=n}[^\\]]+?)\\])|(?:\\[({=r}\\]))").matcher();
    private static final char BOLD = '\u4000', ITALIC = '\u8000', REGULAR = '\0';
    private final StringBuilder sb = new StringBuilder(128);

    /**
     * Takes a CharSequence (such as a String or StringBuilder) that contains the markup this class understands, and
     * produces an IColoredString (of Color) with the color markup tags used to mark text in colors in the resulting
     * IColoredString, and any style markup tags used to mark sections of text as bold or italic for a TextFamily to
     * render (normal TextCellFactory rendering may show bold/italic text as gibberish).
     * @param markupString a String or other CharSequence containing color and/or style markup tags
     * @return an IColoredString (of Color) with all the markup applied and removed from the text after applying
     */
    public IColoredString<Color> colorString(final CharSequence markupString)
    {
        markupMatcher.setTarget(markupString);
        IColoredString<Color> cs = new IColoredString.Impl<>();
        Color current = Color.WHITE;
        char mod = REGULAR;
        while (markupMatcher.find())
        {
            if(markupMatcher.getGroup("p", sb))
            {
                for (int i = 0; i < sb.length(); i++) {
                    sb.setCharAt(i, (char)(sb.charAt(i) | mod));
                }
                cs.append(sb.toString(), current);
            }
            else if(markupMatcher.getGroup("e", sb))
            {
                cs.append((char) ('[' | mod), current);
            }
            else if(markupMatcher.getGroup("r", sb))
            {
                current = Color.WHITE;
                mod = REGULAR;
            }
            else if(markupMatcher.getGroup("u", sb))
            {
                mod = REGULAR;
            }
            else if(markupMatcher.getGroup("h", sb))
            {
                current.set(StringKit.intFromHex(sb));
            }
            else if(markupMatcher.getGroup("n", sb))
            {
                current = Colors.get(sb.toString());
            }
            else if(markupMatcher.getGroup("b", sb))
            {
                mod ^= BOLD;
            }
            else if(markupMatcher.getGroup("i", sb))
            {
                mod ^= ITALIC;
            }
            sb.setLength(0);

        }
        return cs;
    }

    /**
     * Similar to {@link #colorString(CharSequence)}, but leaves color tags as they are and only uses style tags, like
     * {@code [*]} for bold and {@code [/]} for italic. The StringBuilder this returns can be converted to a String or
     * used directly for further modification, but styles will probably only render correctly if using a TextFamily.
     * You should be aware that if {@code []} is used to reset both color and style in the given markupString, then only
     * the style will be reset here but the {@code []} will be removed, which may affect colors if the result is later
     * given to something that expects color markup to also have been closed. A solution for this is to use {@code [,]}
     * to reset only styles, and to avoid using {@code []} to change color by explicitly using {@code [WHITE]} to set
     * the text color back to pure white. This way is only necessary if you have color markup in the markupString you
     * pass to this method.
     * @param markupString a String containing color markup (which is left as-is) and/or style markup (which is used)
     * @return a StringBuilder based on markupString that has the style markup applied and other markup left there.
     */
    public StringBuilder styleString(final CharSequence markupString)
    {
        markupMatcher.setTarget(markupString);
        char mod = REGULAR;
        StringBuilder fsb = new StringBuilder(markupString.length());
        while (markupMatcher.find())
        {
            if(markupMatcher.getGroup("p", sb))
            {
                for (int i = 0; i < sb.length(); i++) {
                    sb.setCharAt(i, (char)(sb.charAt(i) | mod));
                }
                fsb.append(sb);
            }
            else if(markupMatcher.getGroup("e", sb))
            {
                fsb.append((char) ('[' | mod));
            }
            else if(markupMatcher.isCaptured("r") || markupMatcher.isCaptured("u"))
            {
                mod = REGULAR;
            }
            else if(markupMatcher.getGroup("h", sb))
            {
                markupMatcher.getGroup(0, fsb);
            }
            else if(markupMatcher.getGroup("n", sb))
            {
                markupMatcher.getGroup(0, fsb);
            }
            else if(markupMatcher.getGroup("b", sb))
            {
                mod ^= BOLD;
            }
            else if(markupMatcher.getGroup("i", sb))
            {
                mod ^= ITALIC;
            }
            sb.setLength(0);

        }
        return fsb;
    }

    /*
    @Override
    public String escape(String initialText)
    {
        return initialText.replace("[", "[[");
    }
    */
}
