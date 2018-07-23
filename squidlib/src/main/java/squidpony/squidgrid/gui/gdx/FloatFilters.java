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
    private FloatFilters()
    {
        // don't build me!
    }

    /**
     * Wraps the functionality of {@link SColor#toEditedFloat(float, float, float, float, float)} so it can be called as
     * a FloatFilter, adding values to hue, saturation, and value (clamping saturation and value and wrapping hue).
     */
    public static class HSVFilter extends FloatFilter
    {
        public float hueAddend, saturationAddend, valueAddend;
        
        public HSVFilter(float saturation, float value)
        {
            this(0f, saturation, value);
        }
        
        public HSVFilter(float hueAdd, float saturationAdd, float valueAdd)
        {
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
    public static class ColorizeFilter extends FloatFilter
    {
        public float targetHue, targetSaturation, saturationMultiplier, valueAddend;

        public ColorizeFilter(float color)
        {
            this(color, 1f, 0f);
        }
        public ColorizeFilter(Color color)
        {
            this(color.toFloatBits(), 1f, 0f);
        }
        public ColorizeFilter(Color color, float saturationMul, float valueAdd)
        {
            this(color.toFloatBits(), saturationMul, valueAdd);
        }
        public ColorizeFilter(float color, float saturationMul, float valueAdd)
        {
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
            if ( delta < 0.0039f )                           //This is a gray, no chroma...
            {
                s = targetSaturation * 0.5f;
            }
            else                                             //Chromatic data...
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
            if(diff > 101)
                return floatGet(Math.min(1f, 0x1.010102p-16f * r * (203 + (diff >> 1))),
                        Math.min(1f, 0x1.010102p-16f * g * (228 + (diff >> 1))),
                        Math.min(1f, 0x1.010102p-16f * b * (203 + (diff >> 1))),
                        0x1.010102p-8f * a);
            else if(diff < -75)
                return floatGet(Math.min(1f, 0x1.010102p-16f * r * (152 - diff)),
                        Math.min(1f, 0x1.010102p-16f * g * (177 - diff)),
                        Math.min(1f, 0x1.010102p-16f * b * (177 - diff)),
                        0x1.010102p-8f * a);
            else
                return color;
        }
    }

}
