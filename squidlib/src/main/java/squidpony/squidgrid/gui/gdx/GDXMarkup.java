package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import squidpony.panel.IMarkup;

/**
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
