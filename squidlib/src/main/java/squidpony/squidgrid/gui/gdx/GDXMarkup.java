package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import squidpony.panel.IMarkup;

/**
 * GDXMarkup implements IMarkup for libGDX Color objects, and can start blocks of markup that libGDX understands that
 * display text in a given Color. Typically, this class needs only to be instantiated and passed to a class that uses
 * IMarkup of Color, and then anything else will be handled internally as Colors are given to the using class.
 * Created by Tommy Ettinger on 1/23/2016.
 */
public class GDXMarkup implements IMarkup<Color>{
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
}
