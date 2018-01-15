package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import squidpony.IColorCenter;
import squidpony.IFilter;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;

/**
 * A concrete implementation of {@link IColorCenter} for libgdx's {@link com.badlogic.gdx.graphics.Color}.
 * Supports filtering any colors that this creates using a {@link Filter}, such as one from {@link Filters}.
 * This class largely supersedes the earlier {@link SColorFactory} class, and supports similar operations
 * while also allowing filters to modify the returned colors. Some things use different terms between the
 * two classes; {@link SColorFactory#blend(SColor, SColor, double)} is {@link #lerp(Color, Color, double)}
 * here, and {@link SColorFactory#setFloor(int)} is {@link #setGranularity(int)} (with different behavior).
 * @author smelC
 * @author Tommy Ettinger
 * @see SColor Another way to obtain colors by using pre-allocated (and named) instances.
 */
public class SquidColorCenter extends IColorCenter.Skeleton<Color> {

    /**
     * How different requested colors need to be to make a new color; should range from 0 to at most 6.
     * If this is 0, all requested colors will be looked up (using a cached version if the exact request had been made
     * before), but if this is greater than 0, then exponentially less colors will be used, using the cache for twice as
     * many requests at granularity 1 (2 raised to the granularity), four times as many at granularity 2, and so on.
     * Defaults to 1, which seems to help ensure visually-identical colors are not created more than once.
     */
    public int granularity = 1;

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
    }

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
    	super(filterEffect);
    }

    @Override
    protected Color create(int red, int green, int blue, int opacity) {
        if (filter == null)
			/* No filtering */
            return new Color(red / 255f, green / 255f, blue / 255f, opacity / 255f);
        else
			/* Some filtering */
            return filter.alter(red / 255f, green / 255f, blue / 255f, opacity / 255f);
    }
    @Override
    public Color filter(Color c)
    {
        if(c == null)
            return Color.CLEAR;
        else
            return super.filter(c);
    }

    public Color get(long c)
    {
        return get((int)((c >> 24) & 0xff), (int)((c >> 16) & 0xff), (int)((c >> 8) & 0xff), (int)(c & 0xff));
    }
    public Color get(float r, float g, float b, float a)
    {
        return get((int)(255.9999f * r), (int)(255.9999f * g), (int)(255.9999f * b), (int)(255.9999f * a));
    }

    @Override
    protected long getUniqueIdentifier(int r, int g, int b, int a) {
        return super.getUniqueIdentifier(
                r & 0xff << granularity,
                g & 0xff << granularity,
                b & 0xff << granularity,
                a & 0xff << granularity);
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

    public static int encode (Color color) {
        if (color == null)
            return 0;
        return (Math.round(color.r * 255.0f) << 24)
                | (Math.round(color.g * 255.0f) << 16)
                | (Math.round(color.b * 255.0f) << 8)
                | Math.round(color.a * 255.0f);
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
}
