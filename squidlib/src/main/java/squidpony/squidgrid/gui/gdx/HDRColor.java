package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.NumberUtils;

/**
 * High Dynamic Range Color class. Can be used like LibGDX Colors, even with code that _gets_ r, g, and b directly,
 * but not code that _sets_ r, g, and b directly.
 * Based on work by Mario Zechner for libgdx in the Color class
 * Ported and altered from libgdx by Tommy Ettinger on 10/28/2015.
 */
public class HDRColor extends Color {
    public static final HDRColor CLEAR = new HDRColor(0, 0, 0, 0);
    public static final HDRColor BLACK = new HDRColor(0, 0, 0, 1);

    public static final HDRColor WHITE = new HDRColor(0xffffffff);
    public static final HDRColor LIGHT_GRAY = new HDRColor(0xbfbfbfff);
    public static final HDRColor GRAY = new HDRColor(0x7f7f7fff);
    public static final HDRColor DARK_GRAY = new HDRColor(0x3f3f3fff);
    public static final HDRColor SLATE = new HDRColor(0x708090ff);

    public static final HDRColor BLUE = new HDRColor(0, 0, 1, 1);
    public static final HDRColor NAVY = new HDRColor(0, 0, 0.5f, 1);
    public static final HDRColor ROYAL = new HDRColor(0x4169e1ff);
    public static final HDRColor SKY = new HDRColor(0x87ceebff);
    public static final HDRColor CYAN = new HDRColor(0, 1, 1, 1);
    public static final HDRColor TEAL = new HDRColor(0, 0.5f, 0.5f, 1);

    public static final HDRColor GREEN = new HDRColor(0x00ff00ff);
    public static final HDRColor CHARTREUSE = new HDRColor(0x7fff00ff);
    public static final HDRColor LIME = new HDRColor(0x32cd32ff);
    public static final HDRColor FOREST = new HDRColor(0x228b22ff);
    public static final HDRColor OLIVE = new HDRColor(0x6b8e23ff);

    public static final HDRColor YELLOW = new HDRColor(0xffff00ff);
    public static final HDRColor GOLD = new HDRColor(0xffd700ff);
    public static final HDRColor GOLDENROD = new HDRColor(0xdaa520ff);

    public static final HDRColor BROWN = new HDRColor(0x8b4513ff);
    public static final HDRColor TAN = new HDRColor(0xd2b48cff);
    public static final HDRColor FIREBRICK = new HDRColor(0xb22222ff);

    public static final HDRColor RED = new HDRColor(0xff0000ff);
    public static final HDRColor CORAL = new HDRColor(0xff7f50ff);
    public static final HDRColor ORANGE = new HDRColor(0xffa500ff);
    public static final HDRColor SALMON = new HDRColor(0xfa8072ff);
    public static final HDRColor PINK = new HDRColor(0xff69b4ff);
    public static final HDRColor MAGENTA = new HDRColor(1, 0, 1, 1);

    public static final HDRColor PURPLE = new HDRColor(0xa020f0ff);
    public static final HDRColor VIOLET = new HDRColor(0xee82eeff);
    public static final HDRColor MAROON = new HDRColor(0xb03060ff);

    public static final HDRColor SHINING_WHITE = new HDRColor(2f, 2f, 2f, 1f);
    public static final HDRColor SHINING_RED = new HDRColor(2f, 0.1f, 0.1f, 1f);
    public static final HDRColor SHINING_GREEN = new HDRColor(0.1f, 2f, 0.1f, 1f);
    public static final HDRColor SHINING_BLUE = new HDRColor(0.1f, 0.1f, 2f, 1f);
    public static final HDRColor SHINING_YELLOW = new HDRColor(2f, 2f, 0.2f, 1f);
    public static final HDRColor SHINING_CYAN = new HDRColor(0.2f, 1.9f, 1.9f, 1f);
    public static final HDRColor SHINING_MAGENTA = new HDRColor(1.8f, 0.2f, 1.8f, 1f);

    /** the red, green, blue and alpha components **/
    public float hr, hg, hb;

    /** Constructs a new HDRColor with all components set to 0. */
    public HDRColor () {
    }

    /** @see #rgba8888ToColor(HDRColor, int) */
    public HDRColor (int rgba8888) {
        rgba8888ToColor(this, rgba8888);
    }

    /** @see #rgba8888ToColor(HDRColor, int) */
    public HDRColor (long rgbaHDR) {
        rgbaHDRToColor(this, rgbaHDR);
    }

    /** Constructor, sets the components of the HDRColor
     *
     * @param hr High Dynamic Range red component
     * @param hg High Dynamic Range green component
     * @param hb High Dynamic Range blue component
     * @param a the alpha component */
    public HDRColor (float hr, float hg, float hb, float a) {
        this.hr = hr;
        this.hg = hg;
        this.hb = hb;
        this.a = a;
        clamp();
    }

    /** Constructs a new HDRColor using the given HDRColor
     *
     * @param color the HDRColor */
    public HDRColor (HDRColor color) {
        set(color);
    }

    /**
     * Constructs a new color using the given color
     *
     * @param color the color
     */
    public HDRColor(Color color) {
        set(color);
    }

    /** Sets this HDRColor to the given HDRColor.
     *
     * @param color the HDRColor */
    public HDRColor set (HDRColor color) {
        this.hr = color.hr;
        this.hg = color.hg;
        this.hb = color.hb;
        this.a = color.a;
        return clamp();
    }

    /**
     * Sets this color to the given color.
     *
     * @param color the Color
     */
    @Override
    public HDRColor set(Color color) {
        this.hr = color.r;
        this.hg = color.g;
        this.hb = color.b;
        this.a = color.a;
        return clamp();
    }

    /** Multiplies the this HDRColor and the given HDRColor
     *
     * @param color the HDRColor
     * @return this HDRColor. */
    public HDRColor mul (HDRColor color) {
        this.hr *= color.hr;
        this.hg *= color.hg;
        this.hb *= color.hb;
        this.a *= color.a;
        return clamp();
    }

    /**
     * Multiplies the this color and the given color
     *
     * @param color the color
     * @return this color.
     */
    @Override
    public HDRColor mul(Color color) {
        this.hr *= color.r;
        this.hg *= color.g;
        this.hb *= color.b;
        this.a *= color.a;
        return clamp();
    }

    /** Multiplies all components of this HDRColor with the given value.
     *
     * @param value the value
     * @return this HDRColor */
    @Override
    public HDRColor mul (float value) {
        this.hr *= value;
        this.hg *= value;
        this.hb *= value;
        this.a *= value;
        return clamp();
    }

    /** Adds the given HDRColor to this HDRColor.
     *
     * @param color the HDRColor
     * @return this HDRColor */
    public HDRColor add (HDRColor color) {
        this.hr += color.hr;
        this.hg += color.hg;
        this.hb += color.hb;
        this.a += color.a;
        return clamp();
    }

    /**
     * Adds the given color to this color.
     *
     * @param color the color
     * @return this color
     */
    @Override
    public HDRColor add(Color color) {
        this.hr += color.r;
        this.hg += color.g;
        this.hb += color.b;
        this.a += color.a;
        return clamp();
    }

    /** Subtracts the given HDRColor from this HDRColor
     *
     * @param color the HDRColor
     * @return this HDRColor */
    public HDRColor sub (HDRColor color) {
        this.hr -= color.hr;
        this.hg -= color.hg;
        this.hb -= color.hb;
        this.a -= color.a;
        return clamp();
    }

    /**
     * Subtracts the given color from this color
     *
     * @param color the color
     * @return this color
     */
    @Override
    public HDRColor sub(Color color) {
        this.hr -= color.r;
        this.hg -= color.g;
        this.hb -= color.b;
        this.a -= color.a;
        return clamp();
    }

    /** Clamps this HDRColor's components to a valid range [0 - 1]
     * @return this HDRColor for chaining */
    @Override
    public HDRColor clamp () {
        if (hr < 0) {
            r = 0;
            hr = 0;
        }
        else if (hr > 1) r = 1;
        else r = hr;

        if (hg < 0) {
            g = 0;
            hg = 0;
        }
        else if (hg > 1) g = 1;
        else g = hg;

        if (hb < 0) {
            b = 0;
            hb = 0;
        }
        else if (hb > 1) b = 1;
        else b = hb;

        if (a < 0)
            a = 0;
        else if (a > 1) a = 1;
        return this;
    }

    /** Sets this HDRColor's component values.
     *
     * @param hr High Dynamic Range red component
     * @param hg High Dynamic Range green component
     * @param hb High Dynamic Range blue component
     * @param a Alpha component
     *
     * @return this HDRColor for chaining */
    @Override
    public HDRColor set (float hr, float hg, float hb, float a) {
        this.hr = hr;
        this.hg = hg;
        this.hb = hb;
        this.a = a;
        return clamp();
    }

    /** Sets this HDRColor's component values through an integer representation.
     *
     * @return this HDRColor for chaining
     * @see #rgba8888ToColor(HDRColor, int) */
    @Override
    public HDRColor set (int rgba) {
        rgba8888ToColor(this, rgba);
        return this;
    }

    /** Adds the given HDRColor component values to this HDRColor's values.
     *
     * @param hr High Dynamic Range red component
     * @param hg High Dynamic Range green component
     * @param hb High Dynamic Range blue component
     * @param a Alpha component
     *
     * @return this HDRColor for chaining */
    @Override
    public HDRColor add (float hr, float hg, float hb, float a) {
        this.hr += hr;
        this.hg += hg;
        this.hb += hb;
        this.a += a;
        return clamp();
    }

    /** Subtracts the given values from this HDRColor's component values.
     *
     * @param hr High Dynamic Range red component
     * @param hg High Dynamic Range green component
     * @param hb High Dynamic Range blue component
     * @param a Alpha component
     *
     * @return this HDRColor for chaining */
    @Override
    public HDRColor sub (float hr, float hg, float hb, float a) {
        this.hr -= hr;
        this.hg -= hg;
        this.hb -= hb;
        this.a -= a;
        return clamp();
    }

    /** Multiplies this HDRColor's HDRColor components by the given ones.
     *
     * @param hr High Dynamic Range red component
     * @param hg High Dynamic Range green component
     * @param hb High Dynamic Range blue component
     * @param a Alpha component
     *
     * @return this HDRColor for chaining */
    @Override
    public HDRColor mul (float hr, float hg, float hb, float a) {
        this.hr *= hr;
        this.hg *= hg;
        this.hb *= hb;
        this.a *= a;
        return clamp();
    }

    /** Linearly interpolates between this HDRColor and the target HDRColor by t which is in the range [0,1]. The result is stored in this
     * HDRColor.
     * @param target The target HDRColor
     * @param t The interpolation coefficient
     * @return This HDRColor for chaining. */
    public HDRColor lerp (final HDRColor target, final float t) {
        this.hr += t * (target.hr - this.hr);
        this.hg += t * (target.hg - this.hg);
        this.hb += t * (target.hb - this.hb);
        this.a += t * (target.a - this.a);
        return clamp();
    }

    /**
     * Linearly interpolates between this color and the target color by t which is in the range [0,1]. The result is stored in this
     * color.
     *
     * @param target The target color
     * @param t      The interpolation coefficient
     * @return This color for chaining.
     */
    @Override
    public HDRColor lerp(Color target, float t) {
        this.hr += t * (target.r - this.hr);
        this.hg += t * (target.g - this.hg);
        this.hb += t * (target.b - this.hb);
        this.a += t * (target.a - this.a);
        return clamp();
    }

    /** Linearly interpolates between this HDRColor and the target HDRColor by t which is in the range [0,1]. The result is stored in this
     * HDRColor.
     * @param hr The High Dynamic Range red component of the target HDRColor
     * @param hg The High Dynamic Range green component of the target HDRColor
     * @param hb The High Dynamic Range blue component of the target HDRColor
     * @param a The alpha component of the target HDRColor
     * @param t The interpolation coefficient
     * @return This HDRColor for chaining. */
    @Override
    public HDRColor lerp (final float hr, final float hg, final float hb, final float a, final float t) {
        this.hr += t * (hr - this.hr);
        this.hg += t * (hg - this.hg);
        this.hb += t * (hb - this.hb);
        this.a += t * (a - this.a);
        return clamp();
    }

    /** Multiplies the RGB values by the alpha. */
    @Override
    public HDRColor premultiplyAlpha () {
        hr *= a;
        hg *= a;
        hb *= a;
        return clamp();
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HDRColor color = (HDRColor)o;
        return toLongBits() == color.toLongBits();
    }

    @Override
    public int hashCode () {
        int result = (r != +0.0f ? NumberUtils.floatToIntBits(r) : 0);
        result = 31 * result + (g != +0.0f ? NumberUtils.floatToIntBits(g) : 0);
        result = 31 * result + (b != +0.0f ? NumberUtils.floatToIntBits(b) : 0);
        result = 31 * result + (hr != +0.0f ? NumberUtils.floatToIntBits(hr) : 0);
        result = 31 * result + (hg != +0.0f ? NumberUtils.floatToIntBits(hg) : 0);
        result = 31 * result + (hb != +0.0f ? NumberUtils.floatToIntBits(hb) : 0);
        result = 31 * result + (a != +0.0f ? NumberUtils.floatToIntBits(a) : 0);
        return result;
    }

    /** Packs the HDRColor components (rgba only, not storing hr, hg, or hb) into a 32-bit integer with the format ABGR
     * and then converts it to a float.
     * @return the packed HDRColor as a 32-bit float
     * @see NumberUtils#intToFloatColor(int) */
    @Override
    public float toFloatBits () {
        int color = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
        return NumberUtils.intToFloatColor(color);
    }

    /** Packs the HDRColor components (rgba only, not storing hr, hg, or hb) into a 32-bit integer with the format ABGR.
     * @return the packed HDRColor as a 32-bit int. */
    @Override
    public int toIntBits () {
        return ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
    }
    /** Packs the HDRColor components (HDR values and alpha only, not storing r, g, or b) into a 32-bit integer with the
     * format ABGR.
     * @return the packed HDRColor as a 32-bit int. */
    public long toLongBits () {
        return ((long)(255 * hr) << 40) |
                (((long)(255 * hg) & 0xffff) << 24) |
                (((long)(255 * hb) & 0xffff) << 8) |
                ((long)(255 * a) & 0xff);
    }

    /** Returns the HDRColor encoded as hex string with the format RRRRGGGGBBBBAA. */
    @Override
    public String toString () {
        String value = Long.toHexString((((long)(255 * hr) & 0xffff) << 40) |
                        (((long)(255 * hg) & 0xffff) << 24) |
                        (((long)(255 * hb) & 0xffff) << 8) |
                        (long)(255 * a)
        );
        while (value.length() < 14)
            value = "0" + value;
        return value;
    }

    /** Returns a new HDRColor from a hex string with the format RRRRGGGGBBBBAA.
     * @see #toString() */
    public static HDRColor valueOf (String hex) {
        hex = hex.charAt(0) == '#' ? hex.substring(1) : hex;
        int r = Integer.valueOf(hex.substring(0, 4), 16);
        int g = Integer.valueOf(hex.substring(4, 8), 16);
        int b = Integer.valueOf(hex.substring(8, 12), 16);
        int a = hex.length() != 14 ? 255 : Integer.valueOf(hex.substring(12, 14), 16);
        return new HDRColor(r / 255f, g / 255f, b / 255f, a / 255f);
    }

    /** Packs the HDRColor components (rgba only, not storing hr, hg, or hb) into a 32-bit integer with the format ABGR
     * and then converts it to a float. Note that no range checking is performed for higher performance.
     * @param r the red component, 0 - 255
     * @param g the green component, 0 - 255
     * @param b the blue component, 0 - 255
     * @param a the alpha component, 0 - 255
     * @return the packed HDRColor as a float
     * @see NumberUtils#intToFloatColor(int) */
    public static float toFloatBits (int r, int g, int b, int a) {
        int color = (a << 24) | (b << 16) | (g << 8) | r;
        return NumberUtils.intToFloatColor(color);
    }

    /** Packs the HDRColor components into a 32-bit integer with the format ABGR and then converts it to a float.
     * @return the packed HDRColor as a 32-bit float
     * @see NumberUtils#intToFloatColor(int) */
    public static float toFloatBits (float r, float g, float b, float a) {
        int color = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
        return NumberUtils.intToFloatColor(color);
    }

    /** Packs the HDRColor components into a 32-bit integer with the format ABGR. Note that no range checking is performed for higher
     * performance.
     * @param r the red component, 0 - 255
     * @param g the green component, 0 - 255
     * @param b the blue component, 0 - 255
     * @param a the alpha component, 0 - 255
     * @return the packed HDRColor as a 32-bit int */
    public static int toIntBits (int r, int g, int b, int a) {
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    public static int alpha (float alpha) {
        return (int)(alpha * 255.0f);
    }

    public static int luminanceAlpha (float luminance, float alpha) {
        return ((int)(luminance * 255.0f) << 8) | (int)(alpha * 255);
    }

    public static int rgb565 (float r, float g, float b) {
        return ((int)(r * 31) << 11) | ((int)(g * 63) << 5) | (int)(b * 31);
    }

    public static int rgba4444 (float r, float g, float b, float a) {
        return ((int)(r * 15) << 12) | ((int)(g * 15) << 8) | ((int)(b * 15) << 4) | (int)(a * 15);
    }

    public static int rgb888 (float r, float g, float b) {
        return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }

    public static int rgba8888 (float r, float g, float b, float a) {
        return ((int)(r * 255) << 24) | ((int)(g * 255) << 16) | ((int)(b * 255) << 8) | (int)(a * 255);
    }

    public static int argb8888 (float a, float r, float g, float b) {
        return ((int)(a * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }

    public static int rgb565 (HDRColor color) {
        return ((int)(color.r * 31) << 11) | ((int)(color.g * 63) << 5) | (int)(color.b * 31);
    }

    public static int rgba4444 (HDRColor color) {
        return ((int)(color.r * 15) << 12) | ((int)(color.g * 15) << 8) | ((int)(color.b * 15) << 4) | (int)(color.a * 15);
    }

    public static int rgb888 (HDRColor color) {
        return ((int)(color.r * 255) << 16) | ((int)(color.g * 255) << 8) | (int)(color.b * 255);
    }

    public static int rgba8888 (HDRColor color) {
        return ((int)(color.r * 255) << 24) | ((int)(color.g * 255) << 16) | ((int)(color.b * 255) << 8) | (int)(color.a * 255);
    }
    public static long rgbaHDR (HDRColor color) {
        return (Math.round(color.r * 255.0) << 40) | (Math.round(color.g * 255.0) << 24) | (Math.round(color.b * 255.0) << 8) | Math.round(color.a * 255.0);
    }

    public static int argb8888 (HDRColor color) {
        return ((int)(color.a * 255) << 24) | ((int)(color.r * 255) << 16) | ((int)(color.g * 255) << 8) | (int)(color.b * 255);
    }

    /** Sets the HDRColor components using the specified integer value in the format RGB565. This is inverse to the rgb565(r, g, b)
     * method.
     *
     * @param color The HDRColor to be modified.
     * @param value An integer HDRColor value in RGB565 format. */
    public static void rgb565ToColor (HDRColor color, int value) {
        color.hr = ((value & 0x0000F800) >>> 11) / 31f;
        color.hg = ((value & 0x000007E0) >>> 5) / 63f;
        color.hb = (value & 0x0000001F)        / 31f;
    }

    /** Sets the HDRColor components using the specified integer value in the format RGBA4444. This is inverse to the rgba4444(r, g, b,
     * a) method.
     *
     * @param color The HDRColor to be modified.
     * @param value An integer HDRColor value in RGBA4444 format. */
    public static void rgba4444ToColor (HDRColor color, int value) {
        color.hr = ((value & 0x0000f000) >>> 12) / 15f;
        color.hg = ((value & 0x00000f00) >>> 8) / 15f;
        color.hb = ((value & 0x000000f0) >>> 4) / 15f;
        color.a = ((value & 0x0000000f)) / 15f;
    }

    /** Sets the HDRColor components using the specified integer value in the format RGB888. This is inverse to the rgb888(r, g, b)
     * method.
     *
     * @param color The HDRColor to be modified.
     * @param value An integer HDRColor value in RGB888 format. */
    public static void rgb888ToColor (HDRColor color, int value) {
        color.hr = ((value & 0x00ff0000) >>> 16) / 255f;
        color.hg = ((value & 0x0000ff00) >>> 8) / 255f;
        color.hb = ((value & 0x000000ff)) / 255f;
        color.clamp();
    }

    /** Sets the HDRColor components using the specified integer value in the format RGBA8888. This is inverse to the rgba8888(r, g, b,
     * a) method.
     *
     * @param color The HDRColor to be modified.
     * @param value An integer HDRColor value in RGBA8888 format. */
    public static void rgba8888ToColor (HDRColor color, int value) {
        color.hr = ((value & 0xff000000) >>> 24) / 255f;
        color.hg = ((value & 0x00ff0000) >>> 16) / 255f;
        color.hb = ((value & 0x0000ff00) >>> 8) / 255f;
        color.a = ((value & 0x000000ff)) / 255f;
        color.clamp();
    }
    /** Sets the HDRColor components using the specified long value with 16 red, 16 green, 16 blue, and 8 alpha bits.
     * This is inverse to the rgbaHDR() method.
     *
     * @param color The HDRColor to be modified.
     * @param value An long HDRColor value in HDR 16 bt RGB, 8 bit A format. */
    public static void rgbaHDRToColor (HDRColor color, long value) {
        color.hr = ((value & 0xffff0000000000L) >> 40) / 255f;
        color.hg = ((value & 0x0000ffff000000L) >> 24) / 255f;
        color.hb = ((value & 0x00000000ffff00L) >> 8) / 255f;
        color.a = ((value  & 0x000000000000ffL)) / 255f;
        color.clamp();
    }

    /** Sets the HDRColor components using the specified integer value in the format ARGB8888. This is the inverse to the argb8888(a,
     * r, g, b) method
     *
     * @param color The HDRColor to be modified.
     * @param value An integer HDRColor value in ARGB8888 format. */
    public static void argb8888ToColor (HDRColor color, int value) {
        color.a = ((value & 0xff000000) >>> 24) / 255f;
        color.hr = ((value & 0x00ff0000) >>> 16) / 255f;
        color.hg = ((value & 0x0000ff00) >>> 8) / 255f;
        color.hb = ((value & 0x000000ff)) / 255f;
    }

    /** @return a copy of this HDRColor */
    @Override
    public HDRColor cpy () {
        return new HDRColor(this);
    }


}
