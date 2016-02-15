package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import squidpony.IColorCenter;
import squidpony.IFilter;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;

/**
 * A concrete implementation of {@link IColorCenter} for libgdx's {@link com.badlogic.gdx.graphics.Color}.
 * Supports filtering any colors that this creates using a {@link Filter}, such as one from {@link Filters}.
 *
 * @author smelC
 * @author Tommy Ettinger
 * @see SColor Another way to obtain colors by using pre-allocated (and named) instances.
 */
public class SquidColorCenter extends IColorCenter.Skeleton<Color> {

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
        return get(Math.round(255 * r), Math.round(255 * g), Math.round(255 * b), Math.round(255 * a));
    }

    /**
     * Gets the linear interpolation from Color start to Color end, changing by the fraction given by change.
     * @param start the initial Color
     * @param end the "target" color
     * @param change the degree to change closer to end; a change of 0.0f produces start, 1.0f produces end
     * @return a new Color
     */
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
    public Color desaturate(Color color, float degree)
    {
        return lerp(color, desaturated(color), degree);
    }

    /**
     * Saturates color (makes it closer to a vivid color like red or green and less gray) by the specified degree and
     * returns the new color (saturated somewhat). If this is called on a color that is very close to gray, this is
     * likely to produce a red hue by default (if there's no hue to make vivid, it needs to choose something).
     * @param color the color to saturate
     * @param degree a float between 0.0f and 1.0f; more makes it more colorful
     * @return the saturated (and if a filter is used, also filtered) new color
     */
    public Color saturate(Color color, float degree)
    {
        Color fully = getHSV(getHue(color), 1f, getValue(color));
        fully.a = color.a;
        return lerp(color, fully, degree);
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
        ArrayList<Color> colors = new ArrayList<>((steps > 1) ? steps : 1);
        colors.add(filter(fromColor));
        if(steps < 2)
            return colors;
        for (float i = 1; i < steps; i++) {
            colors.add(lerp(fromColor, toColor, i / (steps - 1)));
        }
        return colors;
    }

    @Override
    public String toString() {
        return "SquidColorCenter{" +
                "filter=" + (filter == null ? "null" : filter.getClass().getSimpleName()) +
                '}';
    }
}
