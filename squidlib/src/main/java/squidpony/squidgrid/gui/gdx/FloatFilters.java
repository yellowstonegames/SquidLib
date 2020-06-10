package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import squidpony.squidmath.NumberTools;
import com.github.tommyettinger.anim8.PaletteReducer;

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
     * Hue is in the 0.0 to 1.0 range, as SquidLib handles it, instead of libGDX's 0 to 360 range.
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
//            final int bits = NumberTools.floatToIntBits(color),
//                    r = bits & 0xFF, g = bits >>> 8 & 0xFF, b = bits >>> 16 & 0xFF, a = bits >>> 24,
//                    diff = g - r;
//            if (diff > 101)
//                return floatGet(Math.min(1f, 0x1.010102p-16f * r * (203 + (diff >> 1))),
//                        Math.min(1f, 0x1.010102p-16f * g * (228 + (diff >> 1))),
//                        Math.min(1f, 0x1.010102p-16f * b * (203 + (diff >> 1))),
//                        0x1.010102p-8f * a);
//            else if (diff < -75)
//                return floatGet(Math.min(1f, 0x1.010102p-16f * r * (152 - diff)),
//                        Math.min(1f, 0x1.010102p-16f * g * (177 - diff)),
//                        Math.min(1f, 0x1.010102p-16f * b * (177 - diff)),
//                        0x1.010102p-8f * a);
//            else
//                return color;
            final int bits = NumberTools.floatToIntBits(color);
            final float opacity = (bits >>> 24 & 0xFE) * 0.003937008f;
            float luma = (
                    (bits & 0x000000ff) * (0x1.010102p-8f * 0.299f) +
                            (bits & 0x0000ff00) * (0x1.010102p-16f * 0.587f) +
                            (bits & 0x00ff0000) * (0x1.010102p-24f * 0.114f));
            float chromaB = (
                    (bits & 0x000000ff) * (0x1.010102p-8f * -0.168736f) +
                            (bits & 0x0000ff00) * (0x1.010102p-16f * -0.331264f) +
                            (bits & 0x00ff0000) * (0x1.010102p-24f * 0.5f));
            float chromaR = (
                    (bits & 0x000000ff) * (0x1.010102p-8f * 0.5f) +
                            (bits & 0x0000ff00) * (0x1.010102p-16f * -0.418688f) +
                            (bits & 0x00ff0000) * (0x1.010102p-24f * -0.081312f));
            if(chromaB < -0.05f)
            {
                float theta = NumberTools.atan2(chromaR, chromaB);
                float dist = (float) Math.sqrt(chromaB * chromaB + chromaR * chromaR);
                if(theta >= 0f)
                {
                    theta *= 0.5f;
                    luma += theta * 0.15f;
                    theta += 0.7853981633974483f;
                }
                else
                {
                    theta *= 0.4f;
                    luma += theta * 0.225f;
                    theta -= 0.9424778335276408f;
                }
                chromaR = MathUtils.sin(theta) * dist;
                chromaB = MathUtils.cos(theta) * dist;
            }
            return floatGet(MathUtils.clamp(luma + chromaR * 1.402f, 0f, 1f),
                    MathUtils.clamp(luma - chromaB * 0.344136f - chromaR * 0.714136f, 0f, 1f),
                    MathUtils.clamp(luma + chromaB * 1.772f, 0f, 1f),
                    opacity);

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
     * A static constant of the one possible IdentityFilter, to avoid needing to make duplicates.
     * IdentityFilter makes no changes to the colors given to it.
     */
    public static final IdentityFilter identityFilter = new IdentityFilter();

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
            this(yMul, 1f, 1f);
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
            final float chromaB = cbMul * (
                    (bits & 0x000000ff) * (0x1.010102p-8f * -0.168736f) +
                            (bits & 0x0000ff00) * (0x1.010102p-16f * -0.331264f) +
                            (bits & 0x00ff0000) * (0x1.010102p-24f * 0.5f));
            final float chromaR = crMul * (
                    (bits & 0x000000ff) * (0x1.010102p-8f * 0.5f) +
                            (bits & 0x0000ff00) * (0x1.010102p-16f * -0.418688f) +
                            (bits & 0x00ff0000) * (0x1.010102p-24f * -0.081312f));
            return floatGet(MathUtils.clamp(luma + chromaR * 1.402f, 0f, 1f),
                    MathUtils.clamp(luma - chromaB * 0.344136f - chromaR * 0.714136f, 0f, 1f),
                    MathUtils.clamp(luma + chromaB * 1.772f, 0f, 1f),
                    opacity);
        }
    }

    /**
     * Like {@link YCbCrFilter}, but edits its input colors in YCoCg color space, or like {@link HSVFilter} except it
     * doesn't add, it multiplies. Most of the time you should prefer {@link YCbCrFilter} as long as it isn't a
     * performance bottleneck; this method is faster but less accurate. Y is luminance, ranging from 0 (dark) to 1
     * (light), and affects how bright the color is, but isn't very accurate perceptually. Co is Chrominance(orange) and
     * Cg is Chrominance(green) (both range from -0.5 to 0.5), two inter-related channels that determine the hue and
     * vividness of a specific color. When Co and Cg are both 0, the color is grayscale. When Co is 0.5 and Cg is -0.5,
     * the color is red unless Y is very high or low. When Co is -0.5 and Cg is 0.5, the color is cyan with the same
     * caveats re: Y. When Co and Cg are both -0.5, the color is blue (same caveats), and when both are 0.5, the color
     * is yellow.
     * <br>
     * Valid values for Co and Cg are from -0.5 to 0.5 at the widest part of the range (it shrinks as Y approaches 0 or
     * 1), but there aren't really invalid values here because this filter will clamp results with higher or lower
     * channel values than a color can have. Each of yMul, coMul, and cgMul can have any float value, but yMul should be
     * positive (unless you want this to only produce solid black). Similarly, coMul and cgMul will not produce
     * meaningful results if they are very large (either positive or negative); it's recommended to use values between
     * 0.0 and 1.0 for both if you want to desaturate colors or values somewhat greater than 1.0 to oversaturate them.
     */
    public static class YCoCgFilter extends FloatFilter {
        public float yMul, coMul, cgMul;

        public YCoCgFilter(float luminanceMul, float orangeMul, float greenMul) {
            this.yMul = luminanceMul;
            this.coMul = orangeMul;
            this.cgMul = greenMul;
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
            final float y = yMul * (((bits & 0x000000ff) + ((bits & 0x0000ff00) >>> 7) + ((bits & 0x00ff0000) >>> 16)) * 0x1.010102p-10f);
            final float co = coMul * (((bits & 0x000000ff) - ((bits & 0x00ff0000) >>> 16)) * 0x1.010102p-9f);
            final float cg = cgMul * ((((bits & 0x0000ff00) >>> 7) - (bits & 0x000000ff) - ((bits & 0x00ff0000) >>> 16)) * 0x1.010102p-10f);

            final float t = y - cg;
            return floatGet(MathUtils.clamp(t + co, 0f, 1f),
                    MathUtils.clamp(y + cg, 0f, 1f),
                    MathUtils.clamp(t - co, 0f, 1f),
                    opacity);
        }
    }

    /**
     * Like {@link YCbCrFilter} or  {@link YCoCgFilter}, but edits its input colors in YCwCm color space, which is very
     * similar to YCoCg but has chroma/chrominance components that are useful aesthetically on their own. You may often
     * prefer {@link YCbCrFilter} because it calculates lightness (luma) more precisely, but the Cb (blue-ness) and Cr
     * (red-ness) components are less useful for some purposes individually. Y is luminance, ranging from 0 (dark) to 1
     * (light), and affects how bright the color is, but isn't very accurate perceptually. Cw is Chroma(warm) and
     * Cm is Chroma(mild) (both range from -1.0 to 1.0), two inter-related channels that determine the hue and vividness
     * of a specific color. When Cw and Cm are both 0, the color is grayscale. When Cw is 1 and Cm is -1, the color
     * is red or like red. When Cw is -1 and Cm is 1, the color is green or like green. When Cw and Cm are both -1, the
     * color is blue or like blue, and when both are 1, the color is roughly yellow or brown (depending on Y).
     * <br>
     * Valid values for Cw and Cm are from -1.0 to 1.0, but there aren't really invalid values here because this filter
     * will clamp results with higher or lower channel values than a color can have. Each of yMul, cwMul, and cmMul can
     * have any float value, but yMul should be positive (unless you want this to only produce solid black). Similarly,
     * cwMul and cmMul will not produce meaningful results if they are very large (either positive or negative); it's
     * recommended to use values between 0.0 and 1.0 for both if you want to desaturate colors or values somewhat
     * greater than 1.0 to oversaturate them. Unlike {@link YCbCrFilter} and {@link YCoCgFilter}, you can benefit from
     * setting cwMul independently of the other chroma component, which can be used to emphasize warm vs. cool colors if
     * cwMul is greater than 1.0, or to de-emphasize that comparison if it is between 0.0 and 1.0. A similar option is
     * possible for cmMul, but it isn't as clear of an artistic convention; a high cmMul will separate green-and-yellow
     * colors further from red-purple-and-blue colors. Also unlike the other YCC filters, this allows an additive change
     * to Y, Cw, and Cm applied after the multiplicative change but before converting to RGB and clamping. This can be
     * used to make all colors warmer or cooler (such as for volcano or frozen scenes) by adding or subtracting from Cw,
     * for instance. It can also lighten or darken all colors by changing luma.
     */
    public static class YCwCmFilter extends FloatFilter {
        public float yMul, cwMul, cmMul, yAdd, cwAdd, cmAdd;

        public YCwCmFilter(float yMul, float cwMul, float cmMul) {
            this(yMul, cwMul, cmMul, 0f, 0f, 0f);
        }
        public YCwCmFilter(float yMul, float cwMul, float cmMul, float yAdd, float cwAdd, float cmAdd) {
            this.yMul = yMul;
            this.cwMul = cwMul;
            this.cmMul = cmMul;
            this.yAdd = yAdd;
            this.cwAdd = cwAdd;
            this.cmAdd = cmAdd;
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
            final float luma = yAdd + yMul * ((bits & 0xFF) * 0x3p-11f + (bits >>> 8 & 0xFF) * 0x1p-9f + (bits >>> 16 & 0xFF) * 0x1p-11f);
            final float warm = (cwAdd + cwMul * (((bits & 0xFF) - (bits >>> 16 & 0xff)) * 0x1.010102p-8f));
            final float mild = 0.5f * (cmAdd + cmMul * (((bits >>> 8 & 0xff) - (bits >>> 16 & 0xff)) * 0x1.010102p-8f));

            return floatGet(MathUtils.clamp(luma + warm * 0.625f - mild, 0f, 1f),
                    MathUtils.clamp(luma + mild - warm * 0.375f, 0f, 1f),
                    MathUtils.clamp(luma - warm * 0.375f - mild, 0f, 1f),
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
     * A FloatFilter that limits the colors it can return to a fixed palette, and won't return any colors that are
     * missing from that palette (although it can always return fully-transparent). {@link PaletteReducerFilter} is also
     * an option; it uses more memory but is faster to look up colors in larger palettes (it has a maximum size of 256
     * colors, though, which this class doesn't).
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

    /**
     * A FloatFilter that limits the colors it can return to a fixed palette as determined by a {@link PaletteReducer},
     * and won't return any colors that are missing from that palette (although it can always return fully-transparent).
     * This is like {@link PaletteFilter} but trades off memory usage (it uses about 33KB to store a large-ish lookup
     * table) to improve speed on large palettes. This can't use a palette larger than 256 colors (including transparent
     * almost always).
     */
    public static class PaletteReducerFilter extends FloatFilter {
        public PaletteReducer reducer;

        /**
         * Builds a PaletteReducerFilter that will use the 256-color (including transparent) DawnBringer Aurora palette.
         */
        public PaletteReducerFilter()
        {
            reducer = new PaletteReducer();
        }
        
        /**
         * Builds a PaletteReducerFilter with the given PaletteReducer, which will be referenced without copying. You
         * can call {@link PaletteReducer#exact(Color[])} or other similar methods before this filter is used to set up
         * the palette, and this is often done before the PaletteReducer is passed here.
         * @param palette a PaletteReducer that should have the desired palette set up before this is used
         */
        public PaletteReducerFilter(final PaletteReducer palette) {
            reducer = palette;
        }

        /**
         * Builds a PaletteReducerFilter with an array or vararg of libGDX colors (at most 256 colors, and often
         * starting with a transparent color) that this will choose from. The array will have its contents read but will
         * not be held onto, so later changes won't affect it.
         * @param targets an array or vararg of libGDX colors; must not be null
         */

        public PaletteReducerFilter(final Color... targets)
        {
            reducer = new PaletteReducer(targets);
        }

        /**
         * Takes a packed float color and produces a potentially-different packed float color that this FloatFilter edited.
         *
         * @param color a packed float color, as produced by {@link Color#toFloatBits()}
         * @return a packed float color, as produced by {@link Color#toFloatBits()}
         */
        @Override
        public float alter(float color) {
            return reducer.reduceFloat(color);
        }
    }

}
