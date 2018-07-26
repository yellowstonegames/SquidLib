package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import squidpony.squidmath.NumberTools;

import static squidpony.squidgrid.gui.gdx.SColor.floatGet;

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
        public float targetHue, targetSaturation, saturationMultiplier, valueAddend;

        public ColorizeFilter(float color) {
            this(color, 1f, 0f);
        }

        public ColorizeFilter(Color color) {
            this(color.toFloatBits(), 1f, 0f);
        }

        public ColorizeFilter(Color color, float saturationMul, float valueAdd) {
            this(color.toFloatBits(), saturationMul, valueAdd);
        }

        public ColorizeFilter(float color, float saturationMul, float valueAdd) {
            targetHue = SColor.hueOfFloat(color);
            targetSaturation = SColor.saturationOfFloat(color);
            saturationMultiplier = saturationMul;
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
            float saturation = saturationMultiplier, value = valueAddend;
            final int bits = NumberTools.floatToIntBits(color);
            final float s,
                    r = (bits & 0x000000ff) * 0x1.010102p-8f,
                    g = (bits & 0x0000ff00) * 0x1.010102p-16f,
                    b = (bits & 0x00ff0000) * 0x1.010102p-24f;
            final float min = Math.min(Math.min(r, g), b);   //Min. value of RGB
            final float max = Math.max(Math.max(r, g), b);   //Max value of RGB, equivalent to value
            final float delta = max - min;                   //Delta RGB value
            if (delta < 0.0039f)                           //This is a gray, no chroma...
            {
                s = targetSaturation * 0.5f;
            } else                                             //Chromatic data...
            {
                s = ((delta / max) + targetSaturation) * 0.5f;
            }
            saturation = MathUtils.clamp(s * saturation, 0f, 1f);
            value = MathUtils.clamp(max + value, 0f, 1f);
            float opacity = MathUtils.clamp(((bits & 0xfe000000) >>> 24) * 0x1.020408p-8f, 0f, 1f);

            if (saturation <= 0.0039f) {
                return floatGet(value, value, value, opacity);
            } else if (value <= 0.0039f) {
                return NumberTools.intBitsToFloat((int) (opacity * 254f) << 24 & 0xFE000000);
            } else {
                final float hu = targetHue * 6f;
                final int i = (int) hu;
                final float x = value * (1 - saturation);
                final float y = value * (1 - saturation * (hu - i));
                final float z = value * (1 - saturation * (1 - (hu - i)));

                switch (i) {
                    case 0:
                        return floatGet(value, z, x, opacity);
                    case 1:
                        return floatGet(y, value, x, opacity);
                    case 2:
                        return floatGet(x, value, z, opacity);
                    case 3:
                        return floatGet(x, y, value, opacity);
                    case 4:
                        return floatGet(z, x, value, opacity);
                    default:
                        return floatGet(value, x, y, opacity);
                }
            }
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


}
