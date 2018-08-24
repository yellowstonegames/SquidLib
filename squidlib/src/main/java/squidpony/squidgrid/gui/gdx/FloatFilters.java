package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import squidpony.squidmath.NumberTools;

import static squidpony.squidgrid.gui.gdx.SColor.floatGet;
import static squidpony.squidgrid.gui.gdx.SColor.lerpFloatColorsBlended;

/**
 * Pre-made FloatFilter classes that you can use to filter colors without producing extra Color objects.
 * <br>
 * Created by Tommy Ettinger on 7/22/2018.
 */
public final class FloatFilters {
    private FloatFilters() {
        // don't build me!
    }

    /**
     * Wraps the functionality of {@link SColor#toEditedFloat(float, float, float, float, float)} so it can be called as
     * a FloatFilter, adding values to hue, saturation, and value (clamping saturation and value and wrapping hue).
     */
    public static class HSVFilter extends FloatFilter {
        public float hueAddend, saturationAddend, valueAddend;

        public HSVFilter(float saturation, float value) {
            this(0f, saturation, value);
        }

        public HSVFilter(float hueAdd, float saturationAdd, float valueAdd) {
            hueAddend = hueAdd;
            saturationAddend = saturationAdd;
            valueAddend = valueAdd;
        }

        /**
         * Takes a packed float color and produces a potentially-different packed float color that this FloatFilter edited.
         *
         * @param color a packed float color, as produced by {@link Color#toFloatBits()}
         * @return a packed float color, as produced by {@link Color#toFloatBits()}
         */
        @Override
        public float alter(float color) {
            return SColor.toEditedFloat(color, hueAddend, saturationAddend, valueAddend, 0f);
        }
    }

    /**
     * Changes all colors this alters to have the same hue as, and a closer saturation to, a target color given in the
     * constructor, as well as optionally multiplying saturation of the result and/or adding lightness/value. A good
     * example usage of this is to make a sepia-tone effect with
     * {@code new FloatFilters.ColorizeFilter(SColor.CLOVE_BROWN, 0.6f, 0.0f)}.
     */
    public static class ColorizeFilter extends FloatFilter {
        public float targetCb, targetCr, lumaAddend;

        public ColorizeFilter(float color) {
            this(color, 1f, 0f);
        }

        public ColorizeFilter(Color color) {
            this(color.toFloatBits(), 1f, 0f);
        }

        public ColorizeFilter(Color color, float chromaMul, float lumaAdd) {
            this(color.toFloatBits(), chromaMul, lumaAdd);
        }

        public ColorizeFilter(float color, float chromaMul, float lumaAdd) {
            targetCb = SColor.chromaBOfFloat(color) * chromaMul;
            targetCr = SColor.chromaROfFloat(color) * chromaMul;
            lumaAddend = lumaAdd;
        }

        /**
         * Takes a packed float color and produces a potentially-different packed float color that this FloatFilter edited.
         *
         * @param color a packed float color, as produced by {@link Color#toFloatBits()}
         * @return a packed float color, as produced by {@link Color#toFloatBits()}
         */
        @Override
        public float alter(float color) {
            final int bits = NumberTools.floatToIntBits(color);
            return SColor.floatGetYCbCr((bits & 0x000000ff) * (0x1.010102p-8f  * 0.299f) +
                    (bits & 0x0000ff00) * (0x1.010102p-16f * 0.587f) +
                    (bits & 0x00ff0000) * (0x1.010102p-24f * 0.114f),
                    targetCb, targetCr,
                    ((bits & 0xfe000000) >>> 24) * 0x1.020408p-8f);
        }
    }

    /**
     * A FloatFilter that alters primarily-red and primarily-green colors so they can be more easily be distinguished by
     * people with at least some forms of red-green color-blindness (deuteranopia should be handled well, protanopia
     * very well, and tritanopia may not benefit at all). Causes reds to be darkened and greens to be lightened if the
     * other of the pair is not present in similar quantities (which is the case for yellows and blues).
     */
    public static class DistinctRedGreenFilter extends FloatFilter {
        /**
         * Constructs a DistinctRedGreenFilter. This class is a simple wrapper around a function that doesn't need
         * member variables, so there should be little overhead with this filter.
         */
        public DistinctRedGreenFilter() {
        }

        /**
         * Takes a packed float color and produces a potentially-different packed float color that this FloatFilter edited.
         *
         * @param color a packed float color, as produced by {@link Color#toFloatBits()}
         * @return a packed float color, as produced by {@link Color#toFloatBits()}
         */
        @Override
        public float alter(float color) {
            final int bits = NumberTools.floatToIntBits(color),
                    r = bits & 0xFF, g = bits >>> 8 & 0xFF, b = bits >>> 16 & 0xFF, a = bits >>> 24,
                    diff = g - r;
            if (diff > 101)
                return floatGet(Math.min(1f, 0x1.010102p-16f * r * (203 + (diff >> 1))),
                        Math.min(1f, 0x1.010102p-16f * g * (228 + (diff >> 1))),
                        Math.min(1f, 0x1.010102p-16f * b * (203 + (diff >> 1))),
                        0x1.010102p-8f * a);
            else if (diff < -75)
                return floatGet(Math.min(1f, 0x1.010102p-16f * r * (152 - diff)),
                        Math.min(1f, 0x1.010102p-16f * g * (177 - diff)),
                        Math.min(1f, 0x1.010102p-16f * b * (177 - diff)),
                        0x1.010102p-8f * a);
            else
                return color;
        }
    }

    /**
     * A FloatFilter that makes no changes to the colors given to it; useful as a default for when no filter is wanted.
     */
    public static class IdentityFilter extends FloatFilter {
        /**
         * Takes a packed float color and returns it un-edited.
         *
         * @param color a packed float color, as produced by {@link Color#toFloatBits()}
         * @return a packed float color, as produced by {@link Color#toFloatBits()}
         */
        @Override
        public float alter(float color) {
            return color;
        }
    }

    /**
     * A FloatFilter that makes all colors given to it grayscale, using only their luma as calculated by
     * {@link SColor#lumaOfFloat(float)} as the lightness (it does also preserve alpha transparency).
     */
    public static class GrayscaleFilter extends FloatFilter {
        /**
         * Takes a packed float color and produces a grayscale packed float color that this FloatFilter edited.
         * Uses the luma calculation from {@link SColor#lumaOfFloat(float)} instead of the value calculation from
         * {@link SColor#valueOfFloat(float)}; luma tends to be more visually-accurate on modern monitors.
         *
         * @param color a packed float color, as produced by {@link Color#toFloatBits()}
         * @return a packed float color, as produced by {@link Color#toFloatBits()}
         */
        @Override
        public float alter(float color) {
            final int bits = NumberTools.floatToIntBits(color);
            color = (bits & 0x000000ff) * (0x1.010102p-8f * 0.299f) +
                    (bits & 0x0000ff00) * (0x1.010102p-16f * 0.587f) +
                    (bits & 0x00ff0000) * (0x1.010102p-24f * 0.114f);
            return floatGet(color, color, color, (bits >>> 24 & 0xFE) * 0.003937008f);
        }
    }

    /**
     * Like {@link HSVFilter}, but edits its input colors in YCbCr color space, and multiplies rather than adds.
     * Y is luma, and affects how bright the color is (luma 1 is white, luma 0 is black). Cb is Chroma(blue) amd Cr is
     * Chroma(red), two inter-related channels that determine the hue and vividness of a specific color. When Cb and Cr
     * are both 0, the color is grayscale. When Cb is 0.5 and Cr is -0.5, the color is blue unless Y is very high or
     * low. When Cb is -0.5 and Cr is 0.5, the color is red with the same caveats re: Y. When Cb and Cr are both -0.5,
     * the color is green (same caveats), and when both are 0.5, the color is purple. When Y is 0.5, Cb and Cr form a
     * graph like this:
     * <br>
     * <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/YCbCr-CbCr_Y50.png/240px-YCbCr-CbCr_Y50.png" />
     * <br>
     * Valid values for Cb and Cr are from -0.5 to 0.5 at the widest part of the range (it shrinks as Y approaches 0 or
     * 1), but there aren't really invalid values here because this filter will clamp results with higher or lower
     * channel values than a color can have. Each of yMul, cbMul, and crMul can have any float value, but yMul should be
     * positive (unless you want this to only produce solid black). Similarly, cbMul and crMul will not produce
     * meaningful results if they are very large (either positive or negative); it's recommended to use values between
     * 0.0 and 1.0 for both if you want to desaturate colors or values somewhat greater than 1.0 to oversaturate them.
     */
    public static class YCbCrFilter extends FloatFilter {
        public float yMul, cbMul, crMul;

        public YCbCrFilter(float yMul) {
            this(1f, 1f, yMul);
        }

        public YCbCrFilter(float yMul, float cbMul, float crMul) {
            this.yMul = yMul;
            this.cbMul = cbMul;
            this.crMul = crMul;
        }

        /**
         * Takes a packed float color and produces a potentially-different packed float color that this FloatFilter edited.
         *
         * @param color a packed float color, as produced by {@link Color#toFloatBits()}
         * @return a packed float color, as produced by {@link Color#toFloatBits()}
         */
        @Override
        public float alter(float color) {
            final int bits = NumberTools.floatToIntBits(color);
            final float opacity = (bits >>> 24 & 0xFE) * 0.003937008f;
            float luma = yMul * (
                    (bits & 0x000000ff) * (0x1.010102p-8f * 0.299f) +
                            (bits & 0x0000ff00) * (0x1.010102p-16f * 0.587f) +
                            (bits & 0x00ff0000) * (0x1.010102p-24f * 0.114f));
            if (luma <= 0.0039f) {
                return floatGet(0f, 0f, 0f, opacity);
            } else if (luma >= 0.9961f) {
                return floatGet(1f, 1f, 1f, opacity);
            }
            final float chromaB = cbMul * (
                    (bits & 0x000000ff) * (0x1.010102p-8f * -0.168736f) +
                            (bits & 0x0000ff00) * (0x1.010102p-16f * -0.331264f) +
                            (bits & 0x00ff0000) * (0x1.010102p-24f * 0.5f));
            final float chromaR = crMul * (
                    (bits & 0x000000ff) * (0x1.010102p-8f * 0.5f) +
                            (bits & 0x0000ff00) * (0x1.010102p-16f * -0.418688f) +
                            (bits & 0x00ff0000) * (0x1.010102p-24f * -0.081312f));

            if (chromaR >= -0.0039f && chromaR <= 0.0039f && chromaB >= -0.0039f && chromaB <= 0.0039f) {
                return floatGet(luma, luma, luma, opacity);
            }
            return floatGet(MathUtils.clamp(luma + chromaR * 1.402f, 0f, 1f),
                    MathUtils.clamp(luma - chromaB * 0.344136f - chromaR * 0.714136f, 0f, 1f),
                    MathUtils.clamp(luma + chromaB * 1.772f, 0f, 1f),
                    opacity);
        }
    }

    /**
     * A FloatFilter that chains together one or more FloatFilters one after the next, passing the float output of one
     * as input to the next until the chain has all been called.
     */
    public static class ChainFilter extends FloatFilter {
        public FloatFilter[] filters;

        /**
         * Takes a vararg or array of FloatFilter objects and produces a ChainFilter that will call all of them in order
         * on any color given to this to alter.
         * @param filters an array or vararg of FloatFilter objects; none can be null
         */
        public ChainFilter(FloatFilter... filters)
        {
            if(filters == null || filters.length == 0)
                this.filters = new FloatFilter[]{new IdentityFilter()};
            this.filters = filters;
        }

        /**
         * Takes a packed float color and produces a potentially-different packed float color that this FloatFilter edited.
         *
         * @param color a packed float color, as produced by {@link Color#toFloatBits()}
         * @return a packed float color, as produced by {@link Color#toFloatBits()}
         */
        @Override
        public float alter(float color) {
            for (int i = 0; i < filters.length; i++) {
                color = filters[i].alter(color);
            }
            return color;
        }
    }

    /**
     * A FloatFilter that linearly interpolates (lerps) any color it is given toward a specified color by a specified
     * amount. Uses {@link SColor#lerpFloatColorsBlended(float, float, float)} to mix a requested color with the target
     * color, and this means the alpha of the target color affects the amount of change instead of the resulting alpha. 
     */
    public static class LerpFilter extends FloatFilter {
        public float target, amount;

        /**
         * Builds a LerpFilter with a Color (which will be converted to a packed float color) and an amount as a float.
         * The amount is how much the target color will affect input colors, from 0f to 1f. If the target color has an
         * alpha component that is less than 1, then amount is effectively multiplied by that alpha.
         * @param target a libGDX color; must not be null
         * @param amount a float that determines how much target will affect an input color; will be clamped between 0f and 1f
         */
        public LerpFilter(Color target, float amount) {
            this.target = target.toFloatBits();
            this.amount = MathUtils.clamp(amount, 0f, 1f);
        }

        /**
         * Builds a LerpFilter with a packed float color and an amount as a float.
         * The amount is how much the target color will affect input colors, from 0f to 1f. If the target color has an
         * alpha component that is less than 1, then amount is effectively multiplied by that alpha.
         * @param target a packed float color; must not be null
         * @param amount a float that determines how much target will affect an input color; will be clamped between 0f and 1f
         */

        public LerpFilter(float target, float amount)
        {
            this.target = target;
            this.amount = MathUtils.clamp(amount, 0f, 1f);
        }

        /**
         * Takes a packed float color and produces a potentially-different packed float color that this FloatFilter edited.
         *
         * @param color a packed float color, as produced by {@link Color#toFloatBits()}
         * @return a packed float color, as produced by {@link Color#toFloatBits()}
         */
        @Override
        public float alter(float color) {
            return lerpFloatColorsBlended(color, target, amount);
        }
    }

    /**
     * A FloatFilter that linearly interpolates (lerps) any color it is given toward the most-similar of a group of
     * given colors. Uses {@link SColor#lerpFloatColorsBlended(float, float, float)} to mix a requested color with the
     * chosen target color, and this means the alpha of the target color affects the amount of change instead of the
     * resulting alpha. Changing the alpha of the colors this is given can be done easily with
     * {@link SColor#translucentColor(float, float)}, and this allows you to specify varying amounts to mix by.
     */
    public static class MultiLerpFilter extends FloatFilter {
        public float[] targets;
        public float amount;
        /**
         * Builds a MultiLerpFilter with an array of Color objects (which will be converted to an array of packed float
         * colors) and an amount as a float. The amount is how much the target colors will affect input colors, from 0f
         * to 1f. If a target color has an alpha component that is less than 1, then amount is effectively multiplied by
         * that alpha. If you want to edit the alpha without duplicating Color objects, you can use
         * {@link SColor#translucentColor(Color, float)} to make a float array to pass to
         *{@link #MultiLerpFilter(float, float...)}.
         * @param amount a float that determines how much target will affect an input color; will be clamped between 0f and 1f
         * @param targets an array of libGDX Color objects; must not be null or empty
         */
        public MultiLerpFilter(float amount, Color[] targets) {
            this.targets = new float[targets.length];
            for (int i = 0; i < targets.length; i++) {
                this.targets[i] = targets[i].toFloatBits();
            }
            this.amount = MathUtils.clamp(amount, 0f, 1f);
        }

        /**
         * Builds a MultiLerpFilter with an array of packed float colors and an amount as a float.
         * The amount is how much the target color will affect input colors, from 0f to 1f. If the target color has an
         * alpha component that is less than 1, then amount is effectively multiplied by that alpha.
         * @param amount a float that determines how much target will affect an input color; will be clamped between 0f and 1f
         * @param targets an array or vararg of packed float colors; must not be null or empty
         */

        public MultiLerpFilter(float amount, float... targets)
        {
            this.targets = targets;
            this.amount = MathUtils.clamp(amount, 0f, 1f);
        }

        /**
         * Takes a packed float color and produces a potentially-different packed float color that this FloatFilter edited.
         *
         * @param color a packed float color, as produced by {@link Color#toFloatBits()}
         * @return a packed float color, as produced by {@link Color#toFloatBits()}
         */
        @Override
        public float alter(float color) {
            int choice = 0, diff = SColor.difference2(targets[0], color);
            for (int i = 1; i < targets.length; i++) {
                if(diff != (diff = Math.min(SColor.difference2(targets[i], color), diff)))
                    choice = i;
            }
            return lerpFloatColorsBlended(color, targets[choice], amount);
        }
    }

    /**
     * A FloatFilter that linearly interpolates (lerps) any color it is given toward the most-similar of a group of
     * given colors. Uses {@link SColor#lerpFloatColorsBlended(float, float, float)} to mix a requested color with the
     * chosen target color, and this means the alpha of the target color affects the amount of change instead of the
     * resulting alpha. Changing the alpha of the colors this is given can be done easily with
     * {@link SColor#translucentColor(float, float)}, and this allows you to specify varying amounts to mix by.
     */
    public static class PaletteFilter extends FloatFilter {
        public float[] targets;
        /**
         * Builds a PaletteFilter with an array of Color objects that this will choose from. The array will be converted
         * to an array of packed float colors, and not referenced directly.
         * @param targets an array of libGDX Color objects; must not be null or empty
         */
        public PaletteFilter(final Color[] targets) {
            this.targets = new float[targets.length];
            for (int i = 0; i < targets.length; i++) {
                this.targets[i] = targets[i].toFloatBits();
            }
        }

        /**
         * Builds a PaletteFilter with an array of packed float colors that this will choose from. The array will be
         * referenced directly, not copied, so if you change the contents of targets, it will be reflected here.
         * @param targets an array or vararg of packed float colors; must not be null or empty
         */

        public PaletteFilter(final float... targets)
        {
            this.targets = targets;
        }

        /**
         * Takes a packed float color and produces a potentially-different packed float color that this FloatFilter edited.
         *
         * @param color a packed float color, as produced by {@link Color#toFloatBits()}
         * @return a packed float color, as produced by {@link Color#toFloatBits()}
         */
        @Override
        public float alter(float color) {
            if(color >= 0f) //if color is halfway-transparent or closer to transparent...
                return 0f; // return fully transparent
            int choice = 0, diff = SColor.difference2(targets[0], color);
            for (int i = 1; i < targets.length; i++) {
                if(diff != (diff = Math.min(SColor.difference2(targets[i], color), diff)))
                    choice = i;
            }
            return targets[choice];
        }
    }

}
