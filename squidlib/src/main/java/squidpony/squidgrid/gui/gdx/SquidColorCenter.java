package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntMap;
import squidpony.IColorCenter;
import squidpony.IFilter;
import squidpony.panel.IColoredString;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.IRNG;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;

/**
 * A concrete implementation of {@link IColorCenter} for libgdx's {@link com.badlogic.gdx.graphics.Color}.
 * Supports filtering any colors that this creates using an {@link IFilter}, such as one from {@link Filters}.
 * This class largely supersedes the earlier {@link SColorFactory} class, and supports similar operations
 * while also allowing filters to modify the returned colors. Some things use different terms between the
 * two classes; {@link SColorFactory#blend(SColor, SColor, double)} is {@link #lerp(Color, Color, double)}
 * here, and {@link SColorFactory#setFloor(int)} is {@link #setGranularity(int)} (with different behavior).
 * @author smelC
 * @author Tommy Ettinger
 * @see SColor Another way to obtain colors by using pre-allocated (and named) instances.
 */
public class SquidColorCenter implements IColorCenter<Color> {

    /**
     * How different requested colors need to be to make a new color; should range from 0 to at most 6.
     * If this is 0, all requested colors will be looked up (using a cached version if the exact request had been made
     * before), but if this is greater than 0, then exponentially less colors will be used, using the cache for twice as
     * many requests at granularity 1 (2 raised to the granularity), four times as many at granularity 2, and so on.
     * Defaults to 1, which seems to help ensure visually-identical colors are not created more than once.
     */
    private int granularity = 1;
    private int granularityMask = 0xFE;

    /**
     * Gets the granularity, which is how different requested colors need to be to make a new color; can be from 0 to 6.
     * If this is 0, all requested colors will be looked up (using a cached version if the exact request had been made
     * before), but if this is greater than 0, then exponentially less colors will be used, using the cache for twice as
     * many requests at granularity 1 (2 raised to the granularity), four times as many at granularity 2, and so on.
     * If no granularity was set, the default is 1.
     * @return the current granularity, as an int
     */
    public int getGranularity() {
        return granularity;
    }

    /**
     * Sets the granularity, which is how different requested colors must be to make a new color; from 0 to at most 6.
     * If this is 0, all requested colors will be looked up (using a cached version if the exact request had been made
     * before), but if this is greater than 0, then exponentially less colors will be used, using the cache for twice as
     * many requests at granularity 1 (2 raised to the granularity), four times as many at granularity 2, and so on.
     * If no granularity was set, the default is 1.
     * @param granularity the granularity to use; will be clamped between 0 and 6
     */
    public void setGranularity(int granularity) {
        this.granularity = MathUtils.clamp(granularity, 0, 6);
        granularityMask = 0xFF << this.granularity & 0xFF;
    }

    public IFilter<Color> filter;
    protected IntMap<Color> cache;
    /**
     * A fresh filter-less color center.
     */
    public SquidColorCenter()
    {
    	this(null);
    }

    /**
	 * A fresh filtered color center.
	 * 
	 * @param filterEffect
	 *            The filter to use.
	 */
    public SquidColorCenter(/*Nullable*/IFilter<Color> filterEffect)
    {
        cache = new IntMap<>(256);
    	filter = filterEffect;
    }

    @Override
    public Color get(int red, int green, int blue, int opacity) {
        int abgr = (opacity &= granularityMask & 0xFE) << 24
                | (blue &= granularityMask) << 16
                | (green &= granularityMask) << 8
                | (red &= granularityMask);
        Color result;
        if ((result = cache.get(abgr)) != null)
            return result;
        if (filter == null) {    /* No filtering */
            result = new Color(red / 255f, green / 255f, blue / 255f, opacity / 254f);
        }
        else {                   /* Some filtering */
            result = filter.alter(red / 255f, green / 255f, blue / 255f, opacity / 254f);
        }
        cache.put(abgr, result);
        return result;
    }

    /**
     * @param red   The red component. For screen colors, in-between 0 (inclusive)
     *              and 256 (exclusive).
     * @param green The green component. For screen colors, in-between 0 (inclusive)
     *              and 256 (exclusive).
     * @param blue  The blue component. For screen colors, in-between 0 (inclusive)
     *              and 256 (exclusive).
     * @return An opaque color.
     */
    @Override
    public Color get(int red, int green, int blue) {
        return get(red, green, blue, 255);
    }

    /**
     * @param hue        The hue of the desired color from 0.0 (red, inclusive) towards orange, then
     *                   yellow, and eventually to purple before looping back to almost the same red
     *                   (1.0, exclusive). Values outside this range should be treated as wrapping
     *                   around, so 1.1f and -0.9f would be the same as 0.1f .
     * @param saturation the saturation of the color from 0.0 (a grayscale color; inclusive)
     *                   to 1.0 (a bright color, inclusive)
     * @param value      the value (essentially lightness) of the color from 0.0 (black,
     *                   inclusive) to 1.0 (very bright, inclusive).
     * @param opacity    the alpha component as a float; 0.0f is clear, 1.0f is opaque.
     * @return a possibly transparent color
     */
    @Override
    public Color getHSV(float hue, float saturation, float value, float opacity) {
        if ( saturation < 0.0001f )                       //HSV from 0 to 1
        {
            return get(Math.round(value * 255), Math.round(value * 255), Math.round(value * 255),
                    Math.round(opacity * 255));
        }
        else
        {
            float h = ((hue + 6f) % 1f) * 6f; // allows negative hue to wrap
            int i = (int)h;
            float a = value * (1 - saturation);
            float b = value * (1 - saturation * (h - i));
            float c = value * (1 - saturation * (1 - (h - i)));

            switch (i)
            {
                case 0: return get(Math.round(value * 255), Math.round(c * 255), Math.round(a * 255),
                        Math.round(opacity * 255));
                case 1: return get(Math.round(b * 255), Math.round(value * 255), Math.round(a * 255),
                        Math.round(opacity * 255));
                case 2: return get(Math.round(a * 255), Math.round(value * 255), Math.round(c * 255),
                        Math.round(opacity * 255));
                case 3: return get(Math.round(a * 255), Math.round(b * 255), Math.round(value * 255),
                        Math.round(opacity * 255));
                case 4: return get(Math.round(c * 255), Math.round(a * 255), Math.round(value * 255),
                        Math.round(opacity * 255));
                default: return get(Math.round(value * 255), Math.round(a * 255), Math.round(b * 255),
                        Math.round(opacity * 255));
            }
        }
    }

    /**
     * @param hue        The hue of the desired color from 0.0 (red, inclusive) towards orange, then
     *                   yellow, and eventually to purple before looping back to almost the same red
     *                   (1.0, exclusive)
     * @param saturation the saturation of the color from 0.0 (a grayscale color; inclusive)
     *                   to 1.0 (a bright color, exclusive)
     * @param value      the value (essentially lightness) of the color from 0.0 (black,
     *                   inclusive) to 1.0 (very bright, inclusive).
     * @return an opaque color
     */
    @Override
    public Color getHSV(float hue, float saturation, float value) {
        return getHSV(hue, saturation, value, 1f);
    }

    /**
     * @return Opaque white.
     */
    @Override
    public Color getWhite() {
        return SColor.WHITE;
    }

    /**
     * @return Opaque black.
     */
    @Override
    public Color getBlack() {
        return SColor.BLACK;
    }

    /**
     * @return The fully transparent color.
     */
    @Override
    public Color getTransparent() {
        return SColor.TRANSPARENT;
    }

    /**
     * @param rng     any IRNG from SquidLib, such as an RNG, StatefulRNG, or GWTRNG.
     * @param opacity The alpha component. In-between 0 (inclusive) and 256
     *                (exclusive). Larger values mean more opacity; 0 is clear.
     * @return A random color, except for the alpha component.
     */
    @Override
    public Color getRandom(IRNG rng, int opacity) {
        return get((rng.nextInt() & 0xFFFFFF00) | (opacity & 0xFF));
    }

    @Override
    public Color filter(Color c)
    {
        if(c == null)
            return Color.CLEAR;
        else
            return filter == null ? c : filter.alter(c.r, c.g, c.b, c.a);
    }

    /**
     * @param ics
     * @return {@code ics} filtered according to {@link #filter(Object)}. May be
     * {@code ics} itself if unchanged.
     */
    @Override
    public IColoredString<Color> filter(IColoredString<Color> ics) {
        /*
         * It is common not to have a filter or to have the identity one. To
         * avoid always copying strings in this case, we first roll over the
         * string to see if there'll be a change.
         *
         * This is clearly a subjective design choice but my industry
         * experience is that minimizing allocations is the thing to do for
         * performances, hence I prefer iterating twice to do that.
         */
        boolean change = false;
        for (IColoredString.Bucket<Color> bucket : ics) {
            final Color in = bucket.getColor();
            if (in == null)
                continue;
            final Color out = filter(in);
            if (in != out) {
                change = true;
                break;
            }
        }

        if (change) {
            final IColoredString<Color> result = IColoredString.Impl.create();
            for (IColoredString.Bucket<Color> bucket : ics)
                result.append(bucket.getText(), filter(bucket.getColor()));
            return result;
        } else
            /* Only one allocation: the iterator, yay \o/ */
            return ics;
    }

    /**
     * Gets a copy of t and modifies it to make a shade of gray with the same brightness.
     * The doAlpha parameter causes the alpha to be considered in the calculation of brightness and also changes the
     * returned alpha of the color, so translucent colors are considered darker and fully clear ones are black (and
     * still fully clear).
     * <br>
     * This uses a perceptual calculation of brightness that matches the luma calculation used in the YCbCr color space.
     * It does not necessarily match other brightness calculations, such as value as used in HSV.
     * <br>
     * Not related to reified types or any usage of "reify."
     *
     * @param color   a T to copy; only the copy will be modified
     * @param doAlpha Whether to include (and hereby change) the alpha component; if false alpha is kept as-is
     * @return A monochromatic variation of {@code t}.
     */
    @Override
    public Color greify(Color color, boolean doAlpha) {
        float luma = SColor.luma(color);
        if(doAlpha) {
            luma *= color.a;
            return get(luma, luma, luma, luma);
        }
        return get(luma, luma, luma, color.a);

    }

    public Color get(long c)
    {
        return get((int)((c >>> 24) & 0xff), (int)((c >>> 16) & 0xff), (int)((c >>> 8) & 0xff), (int)(c & 0xff));
    }
    public Color get(float r, float g, float b, float a)
    {
        return get((int)(255.9999f * r), (int)(255.9999f * g), (int)(255.9999f * b), (int)(255.9999f * a));
    }
    /**
     * Gets the linear interpolation from Color start to Color end, changing by the fraction given by change.
     * @param start the initial Color
     * @param end the "target" color
     * @param change the degree to change closer to end; a change of 0.0f produces start, 1.0f produces end
     * @return a new Color
     */
    @Override
    public Color lerp(Color start, Color end, float change)
    {
        if(start == null || end == null)
            return Color.CLEAR;
        return get(
                start.r + change * (end.r - start.r),
                start.g + change * (end.g - start.g),
                start.b + change * (end.b - start.b),
                start.a + change * (end.a - start.a)
        );
    }
    /**
     * Gets the linear interpolation from Color start to Color end, changing by the fraction given by change.
     * @param start the initial Color
     * @param end the "target" color
     * @param change the degree to change closer to end; a change of 0.0 produces start, 1.0 produces end
     * @return a new Color
     */
    public Color lerp(Color start, Color end, double change)
    {
        return lerp(start, end, (float)change);
    }
	@Override
	public int getRed(Color c) {
		return Math.round(c.r * 255f);
	}

	@Override
	public int getGreen(Color c) {
		return Math.round(c.g * 255f);
	}

	@Override
	public int getBlue(Color c) {
		return Math.round(c.b * 255f);
	}

	@Override
	public int getAlpha(Color c) {
		return Math.round(c.a * 255f);
	}

    /**
     * @param c a concrete color
     * @return The hue of the color from 0.0 (red, inclusive) towards orange, then yellow, and
     * eventually to purple before looping back to almost the same red (1.0, exclusive)
     */
    @Override
    public float getHue(Color c) {
        return SColor.hue(c);
    }

    /**
     * @param c a concrete color
     * @return the saturation of the color from 0.0 (a grayscale color; inclusive) to 1.0 (a
     * bright color, exclusive)
     */
    @Override
    public float getSaturation(Color c) {
        return SColor.saturation(c);
    }
    /**
     * @param r the red component in 0.0 to 1.0 range, typically
     * @param g the green component in 0.0 to 1.0 range, typically
     * @param b the blue component in 0.0 to 1.0 range, typically
     * @return the saturation of the color from 0.0 (a grayscale color; inclusive) to 1.0 (a
     * bright color, exclusive)
     */
    public float getSaturation(float r, float g, float b) {
        float min = Math.min(Math.min(r, g ), b);    //Min. value of RGB
        float max = Math.max(Math.max(r, g), b);    //Min. value of RGB
        float delta = max - min;                     //Delta RGB value

        float saturation;

        if ( delta < 0.0001f )                     //This is a gray, no chroma...
        {
            saturation = 0;
        }
        else                                    //Chromatic data...
        {
            saturation = delta / max;
        }
        return saturation;
    }

    /**
     * @param r the red component in 0.0 to 1.0 range, typically
     * @param g the green component in 0.0 to 1.0 range, typically
     * @param b the blue component in 0.0 to 1.0 range, typically
     * @return the value (essentially lightness) of the color from 0.0 (black, inclusive) to
     * 1.0 (very bright, inclusive).
     */
    public float getValue(float r, float g, float b)
    {
        return Math.max(Math.max(r, g), b);
    }

    /**
     * @param r the red component in 0.0 to 1.0 range, typically
     * @param g the green component in 0.0 to 1.0 range, typically
     * @param b the blue component in 0.0 to 1.0 range, typically
     * @return The hue of the color from 0.0 (red, inclusive) towards orange, then yellow, and
     * eventually to purple before looping back to almost the same red (1.0, exclusive)
     */
    public float getHue(float r, float g, float b) {
        float min = Math.min(Math.min(r, g ), b);    //Min. value of RGB
        float max = Math.max(Math.max(r, g), b);    //Min. value of RGB
        float delta = max - min;                     //Delta RGB value

        float hue;

        if ( delta < 0.0001f )                     //This is a gray, no chroma...
        {
            hue = 0;                                //HSV results from 0 to 1
        }
        else                                    //Chromatic data...
        {
            float rDelta = (((max - r) / 6f) + (delta / 2f)) / delta;
            float gDelta = (((max - g) / 6f) + (delta / 2f)) / delta;
            float bDelta = (((max - b) / 6f) + (delta / 2f)) / delta;

            if       (r == max) hue = bDelta - gDelta;
            else if (g == max) hue = (1f / 3f) + rDelta - bDelta;
            else                 hue = (2f / 3f) + gDelta - rDelta;

            if (hue < 0) hue += 1f;
            else if (hue > 1) hue -= 1;
        }
        return hue;
    }

    /**
     * @param c a concrete color
     * @return the value (essentially lightness) of the color from 0.0 (black, inclusive) to
     * 1.0 (very bright, inclusive).
     */
    @Override
    public float getValue(Color c) {
        return SColor.value(c);
    }

    public static int encode (Color color) {
        if (color == null)
            return 0;
        return color.toIntBits();
    }

    /**
     * Gets a modified copy of color as if it is lit with a colored light source.
     * @param color the color to shine the light on
     * @param light the color of the light source
     * @return a copy of the Color color that factors in the lighting of the Color light.
     */
    public Color lightWith(Color color, Color light)
    {
        return filter(color.cpy().mul(light));
    }

    /**
     * Lightens a color by degree and returns the new color (mixed with white).
     * @param color the color to lighten
     * @param degree a float between 0.0f and 1.0f; more makes it lighter
     * @return the lightened (and if a filter is used, also filtered) new color
     */
    public Color light(Color color, float degree)
    {
        return lerp(color, Color.WHITE, degree);
    }

    /**
     * Lightens a color by degree and returns the new color (mixed with white).
     * @param color the color to lighten
     * @param degree a double between 0.0 and 1.0; more makes it lighter
     * @return the lightened (and if a filter is used, also filtered) new color
     */
    public Color light(Color color, double degree)
    {
        return lerp(color, Color.WHITE, degree);
    }
    /**
     * Lightens a color slightly and returns the new color (10% mix with white).
     * @param color the color to lighten
     * @return the lightened (and if a filter is used, also filtered) new color
     */
    public Color light(Color color)
    {
        return lerp(color, Color.WHITE, 0.1f);
    }
    /**
     * Lightens a color significantly and returns the new color (30% mix with white).
     * @param color the color to lighten
     * @return the lightened (and if a filter is used, also filtered) new color
     */
    public Color lighter(Color color)
    {
        return lerp(color, Color.WHITE, 0.3f);
    }
    /**
     * Lightens a color massively and returns the new color (70% mix with white).
     * @param color the color to lighten
     * @return the lightened (and if a filter is used, also filtered) new color
     */
    public Color lightest(Color color)
    {
        return lerp(color, Color.WHITE, 0.7f);
    }

    /**
     * Darkens a color by the specified degree and returns the new color (mixed with black).
     * @param color the color to darken
     * @param degree a float between 0.0f and 1.0f; more makes it darker
     * @return the darkened (and if a filter is used, also filtered) new color
     */
    public Color dim(Color color, float degree)
    {
        return lerp(color, Color.BLACK, degree);
    }

    /**
     * Darkens a color by the specified degree and returns the new color (mixed with black).
     * @param color the color to darken
     * @param degree a double between 0.0 and 1.0; more makes it darker
     * @return the darkened (and if a filter is used, also filtered) new color
     */
    public Color dim(Color color, double degree)
    {
        return lerp(color, Color.BLACK, degree);
    }
    /**
     * Darkens a color slightly and returns the new color (10% mix with black).
     * @param color the color to darken
     * @return the darkened (and if a filter is used, also filtered) new color
     */
    public Color dim(Color color)
    {
        return lerp(color, Color.BLACK, 0.1f);
    }
    /**
     * Darkens a color significantly and returns the new color (30% mix with black).
     * @param color the color to darken
     * @return the darkened (and if a filter is used, also filtered) new color
     */
    public Color dimmer(Color color)
    {
        return lerp(color, Color.BLACK, 0.3f);
    }
    /**
     * Darkens a color massively and returns the new color (70% mix with black).
     * @param color the color to darken
     * @return the darkened (and if a filter is used, also filtered) new color
     */
    public Color dimmest(Color color)
    {
        return lerp(color, Color.BLACK, 0.7f);
    }


    /**
     * Gets a fully-desaturated version of the given color (keeping its brightness, but making it grayscale).
     * @param color the color to desaturate (will not be modified)
     * @return the grayscale version of color
     */
    @Override
    public Color desaturated(Color color)
    {
        float f = color.r * 0.299f + color.g * 0.587f + color.b * 0.114f;
        return get(f, f, f, color.a);
    }

    /**
     * Brings a color closer to grayscale by the specified degree and returns the new color (desaturated somewhat).
     * @param color the color to desaturate
     * @param degree a float between 0.0f and 1.0f; more makes it less colorful
     * @return the desaturated (and if a filter is used, also filtered) new color
     */
    @Override
    public Color desaturate(Color color, float degree)
    {
        return lerp(color, desaturated(color), degree);
    }

    /**
     * Brings a color closer to grayscale by the specified degree and returns the new color (desaturated somewhat).
     * @param color the color to desaturate
     * @param degree a double between 0.0 and 1.0; more makes it less colorful
     * @return the desaturated (and if a filter is used, also filtered) new color
     */
    public Color desaturate(Color color, double degree)
    {
        return lerp(color, desaturated(color), degree);
    }

    /**
     * Fully saturates color (makes it a vivid color like red or green and less gray) and returns the modified copy.
     * Leaves alpha unchanged.
     *
     * @param color the color T to saturate (will not be modified)
     * @return the saturated version of color
     */
    @Override
    public Color saturated(Color color) {
        return getHSV(getHue(color), 1f, getValue(color), getAlpha(color));
    }

    /**
     * Saturates color (makes it closer to a vivid color like red or green and less gray) by the specified degree and
     * returns the new color (saturated somewhat). If this is called on a color that is very close to gray, this is
     * likely to produce a red hue by default (if there's no hue to make vivid, it needs to choose something).
     * @param color the color to saturate
     * @param degree a float between 0.0f and 1.0f; more makes it more colorful
     * @return the saturated (and if a filter is used, also filtered) new color
     */
    @Override
    public Color saturate(Color color, float degree)
    {
        return lerp(color, saturated(color), degree);
    }

    /**
     * Saturates color (makes it closer to a vivid color like red or green and less gray) by the specified degree and
     * returns the new color (saturated somewhat). If this is called on a color that is very close to gray, this is
     * likely to produce a red hue by default (if there's no hue to make vivid, it needs to choose something).
     * @param color the color to saturate
     * @param degree a double between 0.0 and 1.0; more makes it more colorful
     * @return the saturated (and if a filter is used, also filtered) new color
     */
    public Color saturate(Color color, double degree)
    {
        return lerp(color, saturated(color), degree);
    }
    /**
     * Gets a fully random color that is only required to be opaque.
     * @return a random Color
     */
    public Color random()
    {
        StatefulRNG rng = DefaultResources.getGuiRandom();
        return get(rng.nextFloat(), rng.nextFloat(), rng.nextFloat(), 1f);
    }

    /**
     * Blends a color with a random (opaque) color by a factor of 10% random.
     * @param color the color to randomize
     * @return the randomized (and if a filter is used, also filtered) new color
     */
    public Color randomize(Color color)
    {
        return lerp(color, random(), 0.1f);
    }
    /**
     * Blends a color with a random (opaque) color by a factor of 30% random.
     * @param color the color to randomize
     * @return the randomized (and if a filter is used, also filtered) new color
     */
    public Color randomizeMore(Color color)
    {
        return lerp(color, random(), 0.3f);
    }
    /**
     * Blends a color with a random (opaque) color by a factor of 70% random.
     * @param color the color to randomize
     * @return the randomized (and if a filter is used, also filtered) new color
     */
    public Color randomizeMost(Color color)
    {
        return lerp(color, random(), 0.7f);
    }

    /**
     * Blends the colors A and B by a random degree.
     * @param a a color to mix in
     * @param b another color to mix in
     * @return a random blend of a and b.
     */
    public Color randomBlend(Color a, Color b)
    {
        return lerp(a, b, DefaultResources.getGuiRandom().nextFloat());
    }

    public Color invert(Color start)
    {
        float v = getValue(start);
        return v > 0.65f
                ? getHSV((getHue(start) + 0.45f), 1f - getSaturation(start) * 0.85f, v * 0.1f, start.a)
                : getHSV((getHue(start) + 0.45f), 1f - getSaturation(start) * 1.15f, 1f, start.a);
    }
    /**
     * Finds a 16-step gradient going from fromColor to toColor, both included in the gradient.
     * @param fromColor the color to start with, included in the gradient
     * @param toColor the color to end on, included in the gradient
     * @return a 16-element ArrayList composed of the blending steps from fromColor to toColor
     */
    public ArrayList<Color> gradient(Color fromColor, Color toColor)
    {
        ArrayList<Color> colors = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            colors.add(lerp(fromColor, toColor, i / 15f));
        }
        return colors;
    }

    /**
     * Finds a gradient with the specified number of steps going from fromColor to toColor,
     * both included in the gradient.
     * @param fromColor the color to start with, included in the gradient
     * @param toColor the color to end on, included in the gradient
     * @param steps the number of elements to use in the gradient
     * @return an ArrayList composed of the blending steps from fromColor to toColor, with length equal to steps
     */
    public ArrayList<Color> gradient(Color fromColor, Color toColor, int steps)
    {
        return gradient(fromColor, toColor, steps, Interpolation.linear);
    }


    /**
     * Finds a gradient with the specified number of steps going from fromColor to midColor, then midColor to (possibly)
     * fromColor, with both included in the gradient but fromColor only repeated at the end if the number of steps is odd.
     * @param fromColor the color to start with (and end with, if steps is an odd number), included in the gradient
     * @param midColor the color to use in the middle of the loop, included in the gradient
     * @param steps the number of elements to use in the gradient, will be at least 3
     * @return an ArrayList composed of the blending steps from fromColor to midColor to fromColor again, with length equal to steps
     */
    public ArrayList<Color> loopingGradient(Color fromColor, Color midColor, int steps)
    {
        return loopingGradient(fromColor, midColor, steps, Interpolation.linear);
    }

    /**
     * Finds a gradient with the specified number of steps going from fromColor to toColor, both included in the
     * gradient. The interpolation argument can be used to make the color stay close to fromColor and/or toColor longer
     * than it would normally, or shorter if the middle colors are desirable.
     * @param fromColor the color to start with, included in the gradient
     * @param toColor the color to end on, included in the gradient
     * @param steps the number of elements to use in the gradient
     * @param interpolation a libGDX Interpolation that defines how quickly the color changes during the transition
     * @return an ArrayList composed of the blending steps from fromColor to toColor, with length equal to steps
     */
    public ArrayList<Color> gradient(Color fromColor, Color toColor, int steps, Interpolation interpolation)
    {
        ArrayList<Color> colors = new ArrayList<>((steps > 1) ? steps : 1);
        colors.add(filter(fromColor));
        if(steps < 2)
            return colors;
        for (float i = 1; i < steps; i++) {
            colors.add(lerp(fromColor, toColor, interpolation.apply(i / (steps - 1))));
        }
        return colors;
    }

    /**
     * Finds a gradient with the specified number of steps going from fromColor to midColor, then midColor to (possibly)
     * fromColor, with both included in the gradient but fromColor only repeated at the end if the number of steps is
     * odd. The interpolation argument can be used to make the color linger for a while with colors close to fromColor
     * or midColor, or to do the opposite and quickly change from one and spend more time in the middle.
     * @param fromColor the color to start with (and end with, if steps is an odd number), included in the gradient
     * @param midColor the color to use in the middle of the loop, included in the gradient
     * @param steps the number of elements to use in the gradient, will be at least 3
     * @param interpolation a libGDX Interpolation that defines how quickly the color changes at the start and end of
     *                      each transition, both from fromColor to midColor as well as back to fromColor
     * @return an ArrayList composed of the blending steps from fromColor to midColor to fromColor again, with length equal to steps
     */
    public ArrayList<Color> loopingGradient(Color fromColor, Color midColor, int steps, Interpolation interpolation)
    {
        ArrayList<Color> colors = new ArrayList<>((steps > 3) ? steps : 3);
        colors.add(filter(fromColor));
        for (float i = 1; i < steps / 2; i++) {
            colors.add(lerp(fromColor, midColor, interpolation.apply(i / (steps / 2))));
        }
        for (float i = 0, c = steps / 2; c < steps; i++, c++) {
            colors.add(lerp(midColor, fromColor, interpolation.apply(i / (steps / 2))));
        }
        return colors;
    }

    /**
     * Generates a hue-shifted rainbow of colors, starting at red and going through orange, yellow, green, blue, and
     * purple before getting close to red at the end again. If the given number of steps is less than 6 or so, you should
     * expect to see only some of those colors; if steps is larger (36 may be reasonable for gradients), you are more
     * likely to see colors that appear for shorter spans on the color wheel, like orange.
     * Produces fully saturated and max-brightness colors on the rainbow, which is what many people expect for a rainbow.
     * @param steps the number of different Color elements to generate in the returned ArrayList
     * @return an ArrayList of Color where each element goes around the color wheel, starting at red, then orange, etc.
     */
    public ArrayList<Color> rainbow(int steps)
    {
        return rainbow(1f, 1f, 1f, steps);
    }

    /**
     * Generates a hue-shifted rainbow of colors, starting at red and going through orange, yellow, green, blue, and
     * purple before getting close to red at the end again. If the given number of steps is less than 6 or so, you should
     * expect to see only some of those colors; if steps is larger (36 may be reasonable for gradients), you are more
     * likely to see colors that appear for shorter spans on the color wheel, like orange.
     * Uses the given saturation and value for all colors in the rainbow, and only changes hue.
     * @param saturation the saturation of the rainbow's colors; 1.0 is boldest and 0.0 is grayscale
     * @param value the brightness of the rainbow's colors; 1.0 is brightest
     * @param steps the number of different Color elements to generate in the returned ArrayList
     * @return an ArrayList of Color where each element goes around the color wheel, starting at red, then orange, etc.
     */
    public ArrayList<Color> rainbow(float saturation, float value, int steps)
    {
        return rainbow(saturation, value, 1f, steps);
    }

    /**
     * Generates a hue-shifted rainbow of colors, starting at red and going through orange, yellow, green, blue, and
     * purple before getting close to red at the end again. If the given number of steps is less than 6 or so, you should
     * expect to see only some of those colors; if steps is larger (36 may be reasonable for gradients), you are more
     * likely to see colors that appear for shorter spans on the color wheel, like orange.
     * Uses the given saturation, value, and opacity for all colors in the rainbow, and only changes hue.
     * @param saturation the saturation of the rainbow's colors; 1.0 is boldest and 0.0 is grayscale
     * @param value the brightness of the rainbow's colors; 1.0 is brightest
     * @param opacity the alpha value of all colors in the rainbow; 0.0 is fully transparent and 1.0 is opaque
     * @param steps the number of different Color elements to generate in the returned ArrayList
     * @return an ArrayList of Color where each element goes around the color wheel, starting at red, then orange, etc.
     */
    public ArrayList<Color> rainbow(float saturation, float value, float opacity, int steps)
    {
        steps = (steps > 1) ? steps : 1;
        ArrayList<Color> colors = new ArrayList<>(steps);
        for (float i = 0; i < 1f - 0.5f / steps; i+= 1.0f / steps) {
            colors.add(filter(getHSV(i, saturation, value, opacity)));
        }
        return colors;
    }

    /**
     * Generates a hue-shifted rainbow of colors, starting at red and going through orange, yellow, green, blue, and
     * purple before getting close to red at the end again. If the given number of steps is less than 6 or so, you should
     * expect to see only some of those colors; if steps is larger (36 may be reasonable for gradients), you are more
     * likely to see colors that appear for shorter spans on the color wheel, like orange.
     * Uses the given saturation and value for all colors in the rainbow, and only changes hue.
     * @param saturation the saturation of the rainbow's colors; 1.0 is boldest and 0.0 is grayscale
     * @param value the brightness of the rainbow's colors; 1.0 is brightest
     * @param steps the number of different Color elements to generate in the returned ArrayList
     * @return an ArrayList of Color where each element goes around the color wheel, starting at red, then orange, etc.
     */
    public ArrayList<Color> rainbow(double saturation, double value, int steps)
    {
        return rainbow((float)saturation, (float)value, 1f, steps);
    }

    /**
     * Generates a hue-shifted rainbow of colors, starting at red and going through orange, yellow, green, blue, and
     * purple before getting close to red at the end again. If the given number of steps is less than 6 or so, you should
     * expect to see only some of those colors; if steps is larger (36 may be reasonable for gradients), you are more
     * likely to see colors that appear for shorter spans on the color wheel, like orange.
     * Uses the given saturation, value, and opacity for all colors in the rainbow, and only changes hue.
     * @param saturation the saturation of the rainbow's colors; 1.0 is boldest and 0.0 is grayscale
     * @param value the brightness of the rainbow's colors; 1.0 is brightest
     * @param opacity the alpha value of all colors in the rainbow; 0.0 is fully transparent and 1.0 is opaque
     * @param steps the number of different Color elements to generate in the returned ArrayList
     * @return an ArrayList of Color where each element goes around the color wheel, starting at red, then orange, etc.
     */
    public ArrayList<Color> rainbow(double saturation, double value, double opacity, int steps)
    {
        return rainbow((float)saturation, (float)value, (float)opacity, steps);
    }
    /**
     * Finds a gradient with the specified number of steps going from fromColor to toColor, both included in the
     * gradient. This does not typically take a direct path on its way between fromColor and toColor, and is useful to
     * generate a wide variety of colors that can be confined to a rough amount of maximum difference by choosing values
     * for fromColor and toColor that are more similar.
     * <br>
     * Try using colors for fromColor and toColor that have different r, g, and b values, such as gray and white, then
     * compare to colors that don't differ on, for example, r, such as bright red and pink. In the first case, red,
     * green, blue, and many other colors will be generated if there are enough steps; in the second case, red will be
     * at the same level in all generated colors (very high, so no pure blue or pure green, but purple and yellow are
     * possible). This should help illustrate how this chooses how far to "zig-zag" off the straight-line path.
     * @param fromColor the color to start with, included in the gradient
     * @param toColor the color to end on, included in the gradient
     * @param steps the number of elements to use in the gradient; ideally no greater than 345 to avoid duplicates
     * @return an ArrayList composed of the zig-zag steps from fromColor to toColor, with length equal to steps
     */
    public ArrayList<Color> zigzagGradient(Color fromColor, Color toColor, int steps)
    {
        CoordPacker.init();
        ArrayList<Color> colors = new ArrayList<>((steps > 1) ? steps : 1);
        colors.add(filter(fromColor));
        if(steps < 2)
            return colors;
        float dr = toColor.r - fromColor.r, dg = toColor.g - fromColor.g, db = toColor.b - fromColor.b,
                a = fromColor.a, cr, cg, cb;
        int decoded;
        for (float i = 1; i < steps; i++) {
            // 345 happens to be the distance on our 3D Hilbert curve that corresponds to (7,7,7).
            decoded = Math.round(345 * (i / (steps - 1)));
            cr = (CoordPacker.hilbert3X[decoded] / 7f) * dr + fromColor.r;
            cg = (CoordPacker.hilbert3Y[decoded] / 7f) * dg + fromColor.g;
            cb = (CoordPacker.hilbert3Z[decoded] / 7f) * db + fromColor.b;
            colors.add(get(cr, cg, cb, a));
        }
        return colors;
    }

    @Override
    public String toString() {
        return "SquidColorCenter{" +
                "filter=" + (filter == null ? "null" : filter.getClass().getSimpleName()) +
                ",granularity=" + granularity +
                '}';
    }
    /**
     * It clears the cache. You may need to do this to limit the cache to the colors used in a specific section.
     * This is also useful if a Filter changes what colors it should return on a frame-by-frame basis; in that case,
     * you can call clearCache() at the start or end of a frame to ensure the next frame gets different colors.
     */
    public void clearCache()
    {
        cache.clear();
    }

    /**
     * The actual cache is not public, but there are cases where you may want to know how many different colors are
     * actually used in a frame or a section of the game. If the cache was emptied (which might be from calling
     * {@link #clearCache()}), some colors were requested, then this is called, the returned int should be the
     * count of distinct colors this IColorCenter had created and cached; duplicates won't be counted twice.
     * @return
     */
    public int cacheSize()
    {
        return cache.size;
    }

    /**
     * You may want to copy colors between IColorCenter instances that have different create() methods -- and as
     * such, will have different values for the same keys in the cache. This allows you to copy the cache from other
     * into this Skeleton, but using this Skeleton's create() method.
     * @param other another Skeleton of the same type that will have its cache copied into this Skeleton
     */
    public void copyCache(SquidColorCenter other)
    {
        cache.putAll(other.cache);
    }

}
