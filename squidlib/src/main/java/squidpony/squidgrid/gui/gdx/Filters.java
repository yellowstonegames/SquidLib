package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.NumberUtils;
import squidpony.IFilter;
import squidpony.squidmath.LightRNG;

/**
 * Implementations of {@link IFilter}, that all are meant to perform different changes
 * to colors before they are created (they should be passed to SquidColorCenter's constructor, which can use them).
 * Created by Tommy Ettinger on 10/31/2015.
 */
public class Filters {

	private Filters() {
		/* You should not build me */
	}

    /**
     * A Filter that does nothing to the colors it is given but pass them along unchanged.
     */
    public static class IdentityFilter implements IFilter<Color>
    {
        public IdentityFilter()
        {
        	/* Nothing to do */
        }

        @Override
        public Color alter(float r, float g, float b, float a) {
            return new Color(r, g, b, a);
        }
    }

    /**
     * A Filter that converts all colors passed to it to grayscale, like a black and white film.
     */
    public static class GrayscaleFilter implements IFilter<Color>
    {
        public GrayscaleFilter()
        {
        	/* Nothing to do */
        }

        @Override
        public Color alter(float r, float g, float b, float a) {
            float v = (r + g + b) / 3f;
            return new Color(v, v, v, a);
        }

    }

    /**
     * A Filter that tracks the highest brightness for any component it was assigned and stores it in state as the first
     * and only element.
     */
    public static class MaxValueFilter extends Filter<Color>
    {
        public MaxValueFilter()
        {
            state = new float[]{0};
        }

        @Override
        public Color alter(float r, float g, float b, float a) {
            state[0] = Math.max(state[0], Math.max(r, Math.max(g, b)));
            return new Color(r, g, b, a);
        }

    }

    /**
     * A Filter that performs a brightness adjustment to make dark areas lighter and light areas not much less bright.
     */
    public static class GammaCorrectFilter extends Filter<Color> {
        /**
         * Sets up a GammaCorrectFilter with the desired gamma adjustment.
         *
         * @param gamma    should be 1.0 or less, and must be greater than 0. Typical values are between 0.4 to 0.8.
         * @param maxValue the maximum brightness in the colors this will be passed; use MaxValueFilter for this
         */
        public GammaCorrectFilter(float gamma, float maxValue) {
            state = new float[]{gamma, 1f / (float) Math.pow(maxValue, gamma)};
        }

        @Override
        public Color alter(float r, float g, float b, float a) {
            return new Color(state[1] * (float) Math.pow(r, state[0]),
                    state[1] * (float) Math.pow(g, state[0]),
                    state[1] * (float) Math.pow(b, state[0]),
                    a);
        }
    }
    /**
     * A Filter that is constructed with a color and linear-interpolates any color it is told to alter toward the color
     * it was constructed with.
     */
    public static class LerpFilter extends Filter<Color> {
        /**
         * Sets up a LerpFilter with the desired color to linearly interpolate towards.
         *
         * @param r the red component to lerp towards
         * @param g the green component to lerp towards
         * @param b the blue component to lerp towards
         * @param a the opacity component to lerp towards
         * @param amount the amount to lerp by, should be between 0.0 and 1.0
         */
        public LerpFilter(float r, float g, float b, float a, float amount) {
            state = new float[]{r, g, b, a, amount};
        }
        /**
         * Sets up a LerpFilter with the desired color to linearly interpolate towards.
         *
         * @param color the Color to lerp towards
         * @param amount the amount to lerp by, should be between 0.0 and 1.0
         */
        public LerpFilter(Color color, float amount) {
            state = new float[]{color.r, color.g, color.b, color.a, amount};
        }

        @Override
        public Color alter(float r, float g, float b, float a) {
            return new Color(r, g, b, a).lerp(state[0], state[1], state[2], state[3], state[4]);
        }

    }
    /**
     * A Filter that is constructed with a group of colors and linear-interpolates any color it is told to alter toward
     * the color it was constructed with that has the closest hue.
     */
    public static class MultiLerpFilter extends Filter<Color> {
        private SquidColorCenter globalSCC;
        /**
         * Sets up a MultiLerpFilter with the desired colors to linearly interpolate towards; the lengths of each given
         * array should be identical.
         *
         * @param r the red components to lerp towards
         * @param g the green components to lerp towards
         * @param b the blue components to lerp towards
         * @param a the opacity components to lerp towards
         * @param amount the amounts to lerp by, should each be between 0.0 and 1.0
         */
        public MultiLerpFilter(float[] r, float[] g, float[] b, float[] a, float[] amount) {
            state = new float[Math.min(r.length, Math.min(g.length, Math.min(b.length,
                    Math.min(a.length, amount.length)))) * 6];
            globalSCC = DefaultResources.getSCC();
            for (int i = 0; i < state.length / 6; i++) {
                state[i * 6] = r[i];
                state[i * 6 + 1] = g[i];
                state[i * 6 + 2] = b[i];
                state[i * 6 + 3] = a[i];
                state[i * 6 + 4] = amount[i];
                state[i * 6 + 5] = globalSCC.getHue(r[i], g[i], b[i]);
            }
        }/**
         * Sets up a MultiLerpFilter with the desired colors to linearly interpolate towards and their amounts.
         *
         * @param colors the Colors to lerp towards
         * @param amount the amounts to lerp by, should each be between 0.0 and 1.0
         */
        public MultiLerpFilter(Color[] colors, float[] amount) {
            state = new float[Math.min(colors.length, amount.length) * 6];
            globalSCC = DefaultResources.getSCC();
            for (int i = 0; i < state.length / 6; i++) {
                state[i * 6] = colors[i].r;
                state[i * 6 + 1] = colors[i].g;
                state[i * 6 + 2] = colors[i].b;
                state[i * 6 + 3] = colors[i].a;
                state[i * 6 + 4] = amount[i];
                state[i * 6 + 5] = globalSCC.getHue(colors[i]);
            }
        }

        @Override
        public Color alter(float r, float g, float b, float a) {
            float givenH = globalSCC.getHue(r, g, b), givenS = globalSCC.getSaturation(r, g, b),
                    minDiff = 999.0f, temp;
            if(givenS < 0.05)
                return new Color(r, g, b, a);
            int choice = 0;
            for (int i = 5; i < state.length; i += 6) {
                temp = state[i] - givenH;
                temp = (temp >= 0.5f) ? Math.abs(temp - 1f) % 1f : Math.abs(temp);
                if(temp < minDiff) {
                    minDiff = temp;
                    choice = i / 6; // rounds down
                }
            }
            return new Color(r, g, b, a).lerp(state[choice * 6], state[choice * 6 + 1], state[choice * 6 + 2],
                    state[choice * 6 + 3], state[choice * 6 + 4]);
        }
    }

    /**
     * A Filter that is constructed with a color and makes any color it is told to alter have the same hue as the given
     * color, have saturation that is somewhere between the given color's and the altered colors, and chiefly is
     * distinguishable from other colors by value. Useful for sepia effects, which can be created satisfactorily with
     * {@code new Filters.ColorizeFilter(SColor.CLOVE_BROWN, 0.6f, 0.0f)}.
     */
    public static class ColorizeFilter extends Filter<Color> {
        private SquidColorCenter globalSCC;
        /**
         * Sets up a ColorizeFilter with the desired color to colorize towards.
         *
         * @param r the red component to colorize towards
         * @param g the green component to colorize towards
         * @param b the blue component to colorize towards
         */
        public ColorizeFilter(float r, float g, float b) {
            globalSCC = DefaultResources.getSCC();

            state = new float[]{globalSCC.getHue(r, g, b), globalSCC.getSaturation(r, g, b), 1f, 0f};
        }
        /**
         * Sets up a ColorizeFilter with the desired color to colorize towards.
         *
         * @param color the Color to colorize towards
         */
        public ColorizeFilter(Color color) {
            globalSCC = DefaultResources.getSCC();
            state = new float[]{globalSCC.getHue(color), globalSCC.getSaturation(color), 1f, 0f};
        }
        /**
         * Sets up a ColorizeFilter with the desired color to colorize towards.
         *
         * @param r the red component to colorize towards
         * @param g the green component to colorize towards
         * @param b the blue component to colorize towards
         * @param saturationMultiplier a multiplier to apply to the final color's saturation; may be greater than 1
         * @param valueModifier a modifier that affects the final brightness value of any color this alters;
         *                      typically very small, such as in the -0.2f to 0.2f range
         */
        public ColorizeFilter(float r, float g, float b, float saturationMultiplier, float valueModifier) {
            globalSCC = DefaultResources.getSCC();

            state = new float[]{
                    globalSCC.getHue(r, g, b),
                    globalSCC.getSaturation(r, g, b),
                    saturationMultiplier,
                    valueModifier};
        }
        /**
         * Sets up a ColorizeFilter with the desired color to colorize towards.
         *
         * @param color the Color to colorize towards
         * @param saturationMultiplier a multiplier to apply to the final color's saturation; may be greater than 1
         * @param valueModifier a modifier that affects the final brightness value of any color this alters;
         *                      typically very small, such as in the -0.2f to 0.2f range
         */
        public ColorizeFilter(Color color, float saturationMultiplier, float valueModifier) {
            globalSCC = DefaultResources.getSCC();
            state = new float[]{
                    globalSCC.getHue(color),
                    globalSCC.getSaturation(color),
                    saturationMultiplier,
                    valueModifier};
        }

        @Override
        public Color alter(float r, float g, float b, float a) {
            return globalSCC.getHSV(
                    state[0],
                    Math.max(0f, Math.min((globalSCC.getSaturation(r, g, b) + state[1]) * 0.5f * state[2], 1f)),
                    globalSCC.getValue(r, g, b) * (1f - state[3]) + state[3],
                    a);
        }
    }
    /**
     * A Filter that makes the colors requested from it highly saturated, with the original hue, value and a timer that
     * increments very slowly altering hue, with hue, value and the timer altering saturation, and the original hue,
     * saturation, and value all altering value. It should look like a hallucination.
     * <br>
     * A short (poorly recorded) video can be seen here http://i.imgur.com/SEw2LXe.gifv ; performance should be smoother
     * during actual gameplay.
     */
    public static class HallucinateFilter extends Filter<Color> {
        private SquidColorCenter globalSCC;
        /**
         * Sets up a HallucinateFilter with the timer at 0..
         */
        public HallucinateFilter() {
            globalSCC = DefaultResources.getSCC();

            state = new float[]{0f};
        }
        @Override
        public Color alter(float r, float g, float b, float a) {
            state[0] += 0.00003f;
            if(state[0] >= 1.0f)
                state[0] = 0f;
            float h = globalSCC.getHue(r, g, b),
                    s = globalSCC.getSaturation(r, g, b),
                    v = globalSCC.getValue(r, g, b);
            return globalSCC.getHSV(
                    (v * 4f + h + state[0]) % 1.0f,
                    Math.max(0f, Math.min((h + v) * 0.65f + state[0] * 0.4f, 1f)),
                    (h + v + s) * 0.35f + 0.7f,
                    a);
        }

    }


    /**
     * A Filter that multiplies the saturation of any color requested from it by a number given during construction.
     */
    public static class SaturationFilter extends Filter<Color> {
        private SquidColorCenter globalSCC;
        /**
         * Sets up a SaturationFilter with the desired saturation multiplier. Using a multiplier of 0f, as you would
         * expect, makes the image grayscale. Using a multiplier of 0.5 make the image "muted", with no truly bright
         * colors, while 1.0f makes no change, and and any numbers higher than 1.0f will make the image more saturated,
         * with the exception of colors that were already grayscale or close to it. This clamps the result, so there's
         * no need to worry about using too high of a saturation multiplier.
         *
         * @param multiplier the amount to multiply each requested color's saturation by; 1.0f means "no change"
         */
        public SaturationFilter(float multiplier) {
            globalSCC = DefaultResources.getSCC();

            state = new float[]{multiplier};
        }

        @Override
        public Color alter(float r, float g, float b, float a) {
            return globalSCC.getHSV(
                    globalSCC.getHue(r, g, b),
                    Math.max(0f, Math.min((globalSCC.getSaturation(r, g, b) * state[0]), 1f)),
                    globalSCC.getValue(r, g, b),
                    a);
        }

    }

    /**
     * A Filter that is constructed with a palette of colors and randomly increases or decreases the red, green, and
     * blue components of any color it is told to alter. Good for a "glitchy screen" effect.
     */
    public static class WiggleFilter extends Filter<Color> {
        LightRNG rng;
        public WiggleFilter()
        {
            rng = new LightRNG();
        }
        @Override
        public Color alter(float r, float g, float b, float a) {
            return new Color(r - 0.1f + rng.nextFloat() * 0.2f,
                    g - 0.1f + rng.nextFloat() * 0.2f,
                    b - 0.1f + rng.nextFloat() * 0.2f,
                    a);
        }
    }

    /**
     * A Filter that is constructed with a group of colors and forces any color it is told to alter to exactly
     * the color it was constructed with that has the closest red, green, and blue components. A convenient way to
     * use this is to pass in one of the color series from SColor, such as RED_SERIES or ACHROMATIC_SERIES.
     *
     * Preview using BLUE_GREEN_SERIES foreground, ACHROMATIC_SERIES background: http://i.imgur.com/2HdZpC9.png
     */
    public static class PaletteFilter extends Filter<Color> {
        /**
         * Sets up a PaletteFilter with the exact colors to use as individual components; the lengths of each given
         * array should be identical.
         *
         * @param r the red components to use
         * @param g the green components to use
         * @param b the blue components to use
         * @param a the opacity components to use
         */
        public PaletteFilter(float[] r, float[] g, float[] b, float[] a) {
            state = new float[Math.min(r.length, Math.min(g.length, Math.min(b.length,
                    a.length))) * 4];
            for (int i = 0; i < state.length / 4; i++) {
                state[i * 4] = MathUtils.clamp(r[i], 0f, 1f);
                state[i * 4 + 1] = MathUtils.clamp(g[i], 0f, 1f);
                state[i * 4 + 2] = MathUtils.clamp(b[i], 0f, 1f);
                state[i * 4 + 3] = MathUtils.clamp(a[i], 0f, 1f);
            }
        }/**
         * Sets up a PaletteFilter with the exact colors to use as Colors. A convenient way to
         * use this is to pass in one of the color series from SColor, such as RED_SERIES or ACHROMATIC_SERIES.
         *
         * @param colors the Colors to use
         */
        public PaletteFilter(Color[] colors) {
            state = new float[colors.length * 4];
            for (int i = 0; i < colors.length; i++) {
                state[i * 4] = colors[i].r;
                state[i * 4 + 1] = colors[i].g;
                state[i * 4 + 2] = colors[i].b;
                state[i * 4 + 3] = colors[i].a;
            }
        }

        @Override
        public Color alter(float r, float g, float b, float a) {
            float diff = 9999.0f, temp;
            int choice = 0;
            for (int i = 0; i < state.length; i += 4) {
                temp = Math.abs(state[i] - r) + Math.abs(state[i + 1] - g) + Math.abs(state[i + 2] - b);
                if(temp < diff) {
                    diff = temp;
                    choice = i;
                }
            }
            return new Color(state[choice], state[choice + 1], state[choice + 2],
                    a);
        }
    }
    /**
     * A Filter that alters primarily-red and primarily-green colors so they can be more easily be distinguished by
     * people with at least some forms of red-green color-blindness (deuteranopia should be handled well, protanopia
     * very well, and tritanopia may not benefit at all). Causes reds to be darkened and greens to be lightened if the
     * other of the pair is not present in similar quantities (which is the case for yellows and blues).
     */
    public static class DistinctRedGreenFilter implements IFilter<Color> {
        /**
         * Constructs a DistinctRedGreenFilter. This class is a simple wrapper around a function that doesn't need
         * member variables, so there should be little overhead with this filter.
         */
        public DistinctRedGreenFilter() {
        }

        @Override
        public Color alter(float r, float g, float b, float a) {
            float diff = g - r;
            if(diff > 0.4f)
                return new Color(Math.min(1f, r * (0.8f + diff * 0.5f)), Math.min(1f, g * (0.9f + diff * 0.5f)),
                        Math.min(1f, b * (0.8f + diff * 0.5f)), a);
            else if(diff < -0.3f)
                return new Color(r * (0.6f - diff), g * (0.7f - diff),
                        b * (0.7f - diff), a);
            else
                return new Color(r, g, b, a);
        }
    }
    public static class Utility
    {
        /**
         * Modifies the color parameter {@code changing} so its value is the one encoded in {@code value}. The way to
         * obtain value for libGDX Color objects is with {@link Color#toFloatBits()}, which uses ABGR order, so this
         * does some quick work to convert that to RGBA order and assign that into changing.
         * @param changing a Color object that will be modified to have the given value
         * @param value a value as a float that can be obtained by {@link Color#toFloatBits()}
         * @return
         */
        public static Color colorFromFloat(Color changing, float value)
        {
            return changing.set(Integer.reverseBytes(NumberUtils.floatToIntColor(value)));
        }

        /**
         * Gets a packed float representation of a color given as 4 RGBA float components. LibGDX expects ABGR format
         * in some places, but not all, and it can be confusing to track when it wants RGBA, ABGR, or ARGB. Generally,
         * packed floats like what this returns are ABGR format, the kind that can be passed directly to
         * {@link com.badlogic.gdx.graphics.g2d.Batch#setColor(float)} without constructing intermediate objects.
         * SquidPanel also uses floats internally instead of LibGDX Color objects in its internal 2D array that
         * associates colors to cells; this has changed from earlier releases and should be much more efficient.
         * @param r a float from 0.0 to 1.0 for red
         * @param g a float from 0.0 to 1.0 for green
         * @param b a float from 0.0 to 1.0 for blue
         * @param a a float from 0.0 to 1.0 for alpha/opacity
         * @return a packed float that can be given to the setColor method in LibGDX's Batch classes
         */
        public static float floatGet(float r, float g, float b, float a)
        {
            return NumberUtils.intToFloatColor(((int)(a * 255) << 24) | ((int)(b * 255) << 16)
                    | ((int)(g * 255) << 8) | (int)(r * 255));
        }
        /**
         * Gets a packed float representation of a color given an RGBA8888-format long. LibGDX expects ABGR format
         * in some places, but not all, and it can be confusing to track when it wants RGBA, ABGR, or ARGB. Generally,
         * packed floats like what this returns are ABGR format, the kind that can be passed directly to
         * {@link com.badlogic.gdx.graphics.g2d.Batch#setColor(float)} without constructing intermediate objects.
         * SquidPanel also uses floats internally instead of LibGDX Color objects in its internal 2D array that
         * associates colors to cells; this has changed from earlier releases and should be much more efficient.
         * <br>
         * This method is probably not what you want unless you specifically have RGBA8888-format ints or longs that you
         * want converted to packed floats. You probably should look at {@link #floatGet(float, float, float, float)} if
         * you have alpha and/or float components, or {@link #floatGetI(int, int, int)} for the common case of the 3 RGB
         * components as ints and alpha simply opaque.
         * @param c a long with format {@code 32 unused bits, 8 red bits, 8 green bits, 8 blue bits, 7 alpha bits, 1 unused bit}
         * @return a packed float that can be given to the setColor method in LibGDX's Batch classes
         */
        public static float floatGet(long c)
        {
            return NumberUtils.intToFloatColor((int)((c >>> 24 & 0xff) | (c >>> 8 & 0xff00) | (c << 8 & 0xff0000)
                    | (c << 24 & 0xfe000000)));
        }
        /**
         * Gets a packed float representation of a color given as 3 RGB int components, setting alpha to opaque. LibGDX
         * expects ABGR format in some places, but not all, and it can be confusing to track when it wants RGBA, ABGR,
         * or ARGB. Generally, packed floats like what this returns are ABGR format, the kind that can be passed
         * directly to {@link com.badlogic.gdx.graphics.g2d.Batch#setColor(float)} without constructing intermediate
         * objects. SquidPanel also uses floats internally instead of LibGDX Color objects in its internal 2D array that
         * associates colors to cells; this has changed from earlier releases and should be much more efficient.
         * @param r an int from 0 to 255 (both inclusive) for red
         * @param g an int from 0 to 255 (both inclusive) for green
         * @param b an int from 0 to 255 (both inclusive) for blue
         * @return a packed float that can be given to the setColor method in LibGDX's Batch classes
         */
        public static float floatGetI(int r, int g, int b)
        {
            return NumberUtils.intToFloatColor((r & 0xff) | (g << 8 & 0xff00) | (b << 16 & 0xff0000)
                    | 0xfe000000);
        }
    }

}
