package squidpony.squidmath;

/**
 * Various numeric functions that are important to performance but need alternate implementations on GWT to obtain it.
 * Super-sourced on GWT, but most things here are direct calls to JDK methods when on desktop or Android.
 * Some of this code makes use of "creative" bit manipulation of floats and doubles, which can sometimes allow uncommon
 * input-to-output patterns (as in {@link #bounce(float)}), or even can yield a performance boost (compare
 * {@link #zigzag(float)} to using modulus to accomplish the same results). The bit manipulation has good performance on
 * GWT thanks to JS typed arrays, which are well-supported now across all recent browsers and have fallbacks in GWT in
 * the unlikely event of a browser not supporting them.
 */
public final class NumberTools {
    /**
     * Identical to {@link Double#doubleToLongBits(double)} on desktop; optimized on GWT. When compiling to JS via GWT,
     * there is no way to distinguish NaN values with different bits but that are still NaN, so this doesn't try to
     * somehow permit that. Uses JS typed arrays on GWT, which are well-supported now across all recent browsers and
     * have fallbacks in GWT in the unlikely event of a browser not supporting them. JS typed arrays support double, but
     * not long, so this needs to compose a long from two ints, which means the double-to/from-long conversions aren't
     * as fast as float-to/from-int conversions.
     * @param value a {@code double} floating-point number.
     * @return the bits that represent the floating-point number.
     */
    public static long doubleToLongBits(final double value)
    {
        return Double.doubleToLongBits(value);
    }
    /**
     * Identical to {@link Double#doubleToLongBits(double)} on desktop (note, not
     * {@link Double#doubleToRawLongBits(double)}); optimized on GWT. When compiling to JS via GWT, there is no way to
     * distinguish NaN values with different bits but that are still NaN, so this doesn't try to somehow permit that.
     * Uses JS typed arrays on GWT, which are well-supported now across all recent browsers and have fallbacks in GWT in
     * the unlikely event of a browser not supporting them. JS typed arrays support double, but not long, so this needs
     * to compose a long from two ints, which means the double-to/from-long conversions aren't as fast as
     * float-to/from-int conversions.
     * @param value a {@code double} floating-point number.
     * @return the bits that represent the floating-point number.
     */
    public static long doubleToRawLongBits(final double value)
    {
        return Double.doubleToLongBits(value);
    }

    /**
     * Identical to {@link Double#longBitsToDouble(long)} on desktop; optimized on GWT. Uses JS typed arrays on GWT,
     * which are well-supported now across all recent browsers and have fallbacks in GWT in the unlikely event of a
     * browser not supporting them. JS typed arrays support double, but not long, so this needs to compose a long from
     * two ints, which means the double-to/from-long conversions aren't as fast as float-to/from-int conversions.
     * @param bits a long.
     * @return the {@code double} floating-point value with the same bit pattern.
     */
    public static double longBitsToDouble(final long bits)
    {
        return Double.longBitsToDouble(bits);
    }
    /**
     * Converts {@code value} to a long and gets the lower 32 bits of that long, as an int.
     * @param value a {@code double} precision floating-point number.
     * @return the lower half of the bits that represent the floating-point number, as an int.
     */
    public static int doubleToLowIntBits(final double value)
    {
        return (int)(Double.doubleToLongBits(value) & 0xffffffffL);
    }

    /**
     * Converts {@code value} to a long and gets the upper 32 bits of that long, as an int.
     * @param value a {@code double} precision floating-point number.
     * @return the upper half of the bits that represent the floating-point number, as an int.
     */
    public static int doubleToHighIntBits(final double value)
    {
        return (int)(Double.doubleToLongBits(value) >>> 32);
    }

    /**
     * Converts {@code value} to a long and gets the XOR of its upper and lower 32-bit sections. Useful for numerical
     * code where a 64-bit double needs to be reduced to a 32-bit value with some hope of keeping different doubles
     * giving different ints.
     * @param value a {@code double} precision floating-point number.
     * @return the XOR of the lower and upper halves of the bits that represent the floating-point number.
     */
    public static int doubleToMixedIntBits(final double value)
    {
        final long l = Double.doubleToLongBits(value);
        return (int)((l ^ l >>> 32) & 0xFFFFFFFFL);
    }

    /**
     * Makes a modified version of value that uses the specified bits (up to 12) for its exponent and sign.
     * Meant for some specific cases, like adjusting the exponent on an unknown double to the 1.0 to 2.0 range (which
     * would pass 0x3ff for exponentBits). If you have a double from 1.0 to 2.0, you can subtract 1.0 from it to get the
     * often-desirable 0.0-1.0 range. Other common cases are 0x400, which adjusts to between 2.0 and 4.0 (subtracting
     * 3.0 from this gives the -1.0 to 1.0 range, useful for noise), and 0xBFF, which adjusts to between -2.0 and -1.0.
     * For the last case, you might think that -0x3ff would work, but sadly it doesn't. You can use
     * {@code exponentBits |= 0x800} to set the sign bit to negative, or {@code exponentBits &= 0x7ff} for positive.
     * @param value a double that will have its sign and exponent set to the specified bits
     * @param exponentBits the bits to use for the sign and exponent section of the returned modification of value
     * @return the double produced by keeping the significand of value but changing its exponent and sign as given
     */
    public static double setExponent(final double value, final int exponentBits)
    {
        return Double.longBitsToDouble((Double.doubleToLongBits(value) & 0xfffffffffffffL) | ((long) exponentBits << 52));
    }

    /**
     * Gets an 8-bit section of the given double {@code value}, using {@code whichByte} to select whether this should
     * return byte 0 (least significant), 1, 2, and so on up to 7 (most significant).
     * @param value a float
     * @param whichByte an int that will be used to select the byte to take from value (any int is allowed, only the bottom 3 bits are used to select)
     * @return the selected byte from the given float
     */
    public static byte getSelectedByte(final double value, final int whichByte)
    {
        return (byte)(Double.doubleToLongBits(value) >>> ((whichByte & 7) << 3));
    }

    /**
     * Like {@link #getSelectedByte(double, int)}, this sets the byte at a selected position in the int representation of
     * a double, then returns the double produced by the bit change. Uses {@code whichByte} to select whether this should
     * set byte 0 (least significant), 1, 2, and so on up to 7 (most significant). {@code newValue} is a byte.
     * @param value a double
     * @param whichByte an int that will be used to select the byte to take from value (any int is allowed, only the bottom 3 bits are used to select)
     * @param newValue a byte that will be placed into the returned double's bits at the selected position
     * @return a double that results from changing the bits at the selected position to match newValue
     */
    public static double setSelectedByte(final double value, final int whichByte, final byte newValue)
    {
        return Double.longBitsToDouble((Double.doubleToLongBits(value) & ~(255 << ((whichByte & 7) << 3)))
                | ((newValue & 255) << ((whichByte & 7) << 3)));
    }

    /**
     * Very limited-use; takes any double and produces a double in the -1.0 to 1.0 range, with similar inputs producing
     * close to a consistent rate of up and down through the range. This is meant for noise, where it may be useful to
     * limit the amount of change between nearby points' noise values and prevent sudden "jumps" in noise value.
     * @param value any double
     * @return a double from -1.0 (inclusive) to 1.0 (exclusive)
     */
    public static double bounce(final double value)
    {
        final long s = Double.doubleToLongBits(value);
        return Double.longBitsToDouble(((s ^ -((s & 0x8000000000000L)>>51)) & 0xfffffffffffffL)
                | 0x4010000000000000L) - 5.0;
    }

    /**
     * Very limited-use; takes any double and produces a double in the -1.0 to 1.0 range, with similar inputs producing
     * close to a consistent rate of up and down through the range. This is meant for noise, where it may be useful to
     * limit the amount of change between nearby points' noise values and prevent sudden "jumps" in noise value.
     * @param value any double
     * @return a double from -1.0 (inclusive) to 1.0 (exclusive)
     */
    public static float bounce(final float value)
    {
        final int s = Float.floatToIntBits(value);
        return Float.intBitsToFloat(((s ^ -((s & 0x00400000)>>22)) & 0x007fffff)
                | 0x40800000) - 5f;
    }
    /**
     * Very limited-use; takes the significand bits of a double, represented as a long of which this uses 52 bits, and
     * produces a double in the -1.0 to 1.0 range, with similar inputs producing close to a consistent rate of up and
     * down through the range. This is meant for noise, where it may be useful to limit the amount of change between
     * nearby points' noise values and prevent sudden "jumps" in noise value.
     * @param value any long; only the lower 52 bits will be used
     * @return a double from -1.0 (inclusive) to 1.0 (exclusive)
     */
    public static double bounce(final long value)
    {
        return Double.longBitsToDouble(((value ^ -((value & 0x8000000000000L)>>51)) & 0xfffffffffffffL)
                | 0x4010000000000000L) - 5.0;
    }
    /**
     * Very limited-use; takes the significand bits of a double, represented as a pair of ints {@code valueLow} and
     * {@code valueHigh}, using all bits in valueLow and the least-significant 20 bits of valueHigh, and
     * produces a double in the -1.0 to 1.0 range, with similar inputs producing close to a consistent rate of up and
     * down through the range. This is meant for noise, where it may be useful to limit the amount of change between
     * nearby points' noise values and prevent sudden "jumps" in noise value.
     * @param valueLow any int; all bits will be used as the less-significant bits of the significand
     * @param valueHigh any int; only the bottom 20 bits will be used as the more-significant bits of the significand
     * @return a double from -1.0 (inclusive) to 1.0 (exclusive)
     */

    public static double bounce(final int valueLow, final int valueHigh)
    {
        final long s = (((long) valueHigh) << 32 | valueLow);
        return Double.longBitsToDouble(((s ^ -((s & 0x8000000000000L)>>51)) & 0xfffffffffffffL)
                | 0x4010000000000000L) - 5.0;
    }

    /**
     * Limited-use; takes any double and produces a double in the -1.0 to 1.0 range, with similar inputs producing
     * close to a consistent rate of up and down through the range. This is meant for noise, where it may be useful to
     * limit the amount of change between nearby points' noise values and prevent sudden "jumps" in noise value. It is
     * very similar to {@link #bounce(double)}, but unlike bounce() this will maintain a continuous rate regardless of
     * the magnitude of its input. An input of any even number should produce something very close to -1.0, any odd
     * number should produce something very close to 1.0, and any number halfway between two incremental integers (like
     * 8.5 or -10.5) should produce 0.0 or a very small fraction. This method is closely related to
     * {@link #sway(double)}, which will smoothly curve its output to produce more values that are close to -1 or 1.
     * @param value any double
     * @return a double from -1.0 (inclusive) to 1.0 (inclusive)
     */
    public static double zigzag(double value)
    {
        long floor = (value >= 0.0 ? (long) value : (long) value - 1L);
        value -= floor;
        floor = (-(floor & 1L) | 1L);
        return value * (floor << 1) - floor;
    }

    /**
     * Limited-use; takes any float and produces a float in the -1f to 1f range, with similar inputs producing
     * close to a consistent rate of up and down through the range. This is meant for noise, where it may be useful to
     * limit the amount of change between nearby points' noise values and prevent sudden "jumps" in noise value. It is
     * very similar to {@link #bounce(float)}, but unlike bounce() this will maintain a continuous rate regardless of
     * the magnitude of its input. An input of any even number should produce something very close to -1f, any odd
     * number should produce something very close to 1f, and any number halfway between two incremental integers (like
     * 8.5f or -10.5f) should produce 0f or a very small fraction. This method is closely related to
     * {@link #sway(float)}, which will smoothly curve its output to produce more values that are close to -1 or 1.
     * @param value any float
     * @return a float from -1f (inclusive) to 1f (inclusive)
     */
    public static float zigzag(float value)
    {
        int floor = (value >= 0f ? (int) value : (int) value - 1);
        value -= floor;
        floor = (-(floor & 1) | 1);
        return value * (floor << 1) - floor;
    }

    /**
     * Limited-use; takes any double and produces a double in the -1 to 1 range, with a graph of input to output that
     * looks much like a sine wave, curving to have a flat slope when given an integer input and a steep slope when the
     * input is halfway between two integers, smoothly curving at any points between those extremes. This is meant for
     * noise, where it may be useful to limit the amount of change between nearby points' noise values and prevent both
     * sudden "jumps" in noise value and "cracks" where a line takes a sudden jagged movement at an angle. It is very
     * similar to {@link #bounce(double)} and {@link #zigzag(double)}, but unlike bounce() this will maintain its
     * frequency of returning max or min values, regardless of the magnitude of its input (as long as there is enough
     * floating-point precision to represent changes smaller than 1.0), and unlike zigzag() this will smooth its path.
     * An input of any even number should produce something very close to -1.0, any odd number should produce something
     * very close to 1.0, and any number halfway between two incremental integers (like 8.5 or -10.5) should produce 0.0
     * or a very small fraction. In the (unlikely) event that this is given a double that is too large to represent
     * many or any non-integer values, this will simply return -1.0 or 1.0.
     * @param value any double other than NaN or infinite values; extremely large values can't work properly
     * @return a double from -1.0 (inclusive) to 1.0 (inclusive)
     */
    public static double sway(double value)
    {
        long floor = (value >= 0.0 ? (long) value : (long) value - 1L);
        value -= floor;
        floor = (-(floor & 1L) | 1L);
        return value * value * value * (value * (value * 6.0 - 15.0) + 10.0) * (floor << 1) - floor;
    }

    /**
     * Limited-use; takes any float and produces a float in the -1f to 1f range, with a graph of input to output that
     * looks much like a sine wave, curving to have a flat slope when given an integer input and a steep slope when the
     * input is halfway between two integers, smoothly curving at any points between those extremes. This is meant for
     * noise, where it may be useful to limit the amount of change between nearby points' noise values and prevent both
     * sudden "jumps" in noise value and "cracks" where a line takes a sudden jagged movement at an angle. It is very
     * similar to {@link #bounce(float)} and {@link #zigzag(float)}, but unlike bounce() this will maintain its
     * frequency of returning max or min values, regardless of the magnitude of its input (as long as there is enough
     * floating-point precision to represent changes smaller than 1f), and unlike zigzag() this will smooth its path.
     * An input of any even number should produce something very close to -1f, any odd number should produce something
     * very close to 1f, and any number halfway between two incremental integers (like 8.5f or -10.5f) should produce 0f
     * or a very small fraction. In the (unlikely) event that this is given a float that is too large to represent
     * many or any non-integer values, this will simply return -1f or 1f.
     * @param value any float other than NaN or infinite values; extremely large values can't work properly
     * @return a float from -1f (inclusive) to 1f (inclusive)
     */
    public static float sway(float value)
    {
        int floor = (value >= 0f ? (int) value : (int) value - 1);
        value -= floor;
        floor = (-(floor & 1) | 1);
        return value * value * value * (value * (value * 6f - 15f) + 10f) * (floor << 1) - floor;
    }

    /**
     * Limited-use; takes any float and produces a float in the 0f to 1f range, with a graph of input to output that
     * looks much like a sine wave, curving to have a flat slope when given an integer input and a steep slope when the
     * input is halfway between two integers, smoothly curving at any points between those extremes. This is meant for
     * noise, where it may be useful to limit the amount of change between nearby points' noise values and prevent both
     * sudden "jumps" in noise value and "cracks" where a line takes a sudden jagged movement at an angle. It is very
     * similar to {@link #bounce(float)} and {@link #zigzag(float)}, but unlike bounce() this will not change its
     * frequency of returning max or min values, regardless of the magnitude of its input (as long as there is enough
     * floating-point precision to represent changes smaller than 1f), and unlike zigzag() this will smooth its path.
     * An input of any even number should produce something very close to 0f, any odd number should produce something
     * very close to 1f, and any number halfway between two incremental integers (like 8.5f or -10.5f) should produce
     * 0.5f. In the (unlikely) event that this is given a float that is too large to represent many or any non-integer
     * values, this will simply return 0f or 1f. This version is called "Tight" because its range is tighter than
     * {@link #sway(float)}.
     * @param value any float other than NaN or infinite values; extremely large values can't work properly
     * @return a float from 0f (inclusive) to 1f (inclusive)
     */
    public static float swayTight(float value)
    {
        int floor = (value >= 0f ? (int) value : (int) value - 1);
        value -= floor;
        floor &= 1;
        return value * value * value * (value * (value * 6f - 15f) + 10f) * (-floor | 1) + floor;
    }

    /**
     * Limited-use; takes any double and produces a double in the 0.0 to 1.0 range, with a graph of input to output that
     * looks much like a sine wave, curving to have a flat slope when given an integer input and a steep slope when the
     * input is halfway between two integers, smoothly curving at any points between those extremes. This is meant for
     * noise, where it may be useful to limit the amount of change between nearby points' noise values and prevent both
     * sudden "jumps" in noise value and "cracks" where a line takes a sudden jagged movement at an angle. It is very
     * similar to {@link #bounce(double)} and {@link #zigzag(double)}, but unlike bounce() this will not change its
     * frequency of returning max or min values, regardless of the magnitude of its input (as long as there is enough
     * floating-point precision to represent changes smaller than 1.0), and unlike zigzag() this will smooth its path.
     * An input of any even number should produce something very close to 0.0, any odd number should produce something
     * very close to 1.0, and any number halfway between two incremental integers (like 8.5 or -10.5) should produce
     * 0.5f. In the (unlikely) event that this is given a double that is too large to represent many or any non-integer
     * values, this will simply return 0.0 or 1.0. This version is called "Tight" because its range is tighter than
     * {@link #sway(double)}.
     * @param value any double other than NaN or infinite values; extremely large values can't work properly
     * @return a double from 0.0 (inclusive) to 1.0 (inclusive)
     */
    public static double swayTight(double value)
    {
        long floor = (value >= 0.0 ? (long) value : (long) value - 1L);
        value -= floor;
        floor &= 1L;
        return value * value * value * (value * (value * 6.0 - 15.0) + 10.0) * (-floor | 1L) + floor;
    }

    /**
     * A mix of the smooth transitions of {@link #sway(double)} with (seeded) random peaks and valleys between -1.0 and
     * 1.0 (both exclusive). The pattern this will produces will be completely different if the seed changes, and it is
     * suitable for 1D noise. Uses a simple method of cubic interpolation between random values, where a random value is
     * used without modification when given an integer for {@code value}. Note that this uses a different type of
     * interpolation than {@link #sway(double)}, which uses quintic (this causes swayRandomized() to produce more
     * outputs in the mid-range and less at extremes; it is also slightly faster and simpler).
     * <br>
     * Performance note: HotSpot seems to be much more able to optimize swayRandomized(long, float) than
     * swayRandomized(long, double), with the float version almost twice as fast after JIT warms up. On GWT, the
     * reverse should be expected because floats must be emulated there.
     * @param seed a long seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a double that typically changes slowly, by less than 1.0, with direction changes at integer inputs
     * @return a pseudo-random double between -1.0 and 1.0 (both exclusive), smoothly changing with value
     */
    public static double swayRandomized(long seed, double value)
    {
        final long floor = value >= 0.0 ? (long) value : (long) value - 1L; // the closest long that is less than value
        // gets a random start and endpoint. there's a sequence of start and end values for each seed, and changing the
        // seed changes the start and end values unpredictably (so use the same seed for one curving line).
        final double start = (((seed += floor * 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) * 0x0.fffffffffffffbp-63,
                end = (((seed += 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) * 0x0.fffffffffffffbp-63;
        // gets the fractional part of value
        value -= floor;
        // cubic interpolation to smooth the curve
        value *= value * (3.0 - 2.0 * value);
        // interpolate between start and end based on how far we are between the start and end points of this section
        return (1.0 - value) * start + value * end;
    }

    /**
     * A mix of the smooth transitions of {@link #sway(float)} with (seeded) random peaks and valleys between -1f and
     * 1f (both exclusive). The pattern this will produces will be completely different if the seed changes, and it is
     * suitable for 1D noise. Uses a simple method of cubic interpolation between random values, where a random value is
     * used without modification when given an integer for {@code value}. Note that this uses a different type of
     * interpolation than {@link #sway(float)}, which uses quintic (this causes swayRandomized() to produce more
     * outputs in the mid-range and less at extremes; it is also slightly faster and simpler).
     * <br>
     * Performance note: HotSpot seems to be much more able to optimize swayRandomized(long, float) than
     * swayRandomized(long, double), with the float version almost twice as fast after JIT warms up. On GWT, the
     * reverse should be expected because floats must be emulated there.
     * @param seed a long seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a float that typically changes slowly, by less than 2.0, with direction changes at integer inputs
     * @return a pseudo-random float between -1f and 1f (both exclusive), smoothly changing with value
     */
    public static float swayRandomized(long seed, float value)
    {
        final long floor = value >= 0f ? (long) value : (long) value - 1L;
        final float start = (((seed += floor * 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) * 0x0.ffffffp-63f,
                end = (((seed += 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) * 0x0.ffffffp-63f;
        value -= floor;
        value *= value * (3f - 2f * value);
        return (1f - value) * start + value * end;
    }

    /**
     * A variant on {@link #swayRandomized(long, double)} that takes an int seed instead of a long, and is optimized for
     * usage on GWT. Like the version with a long seed, this uses cubic interpolation between random peak or valley
     * points; only the method of generating those random peaks and valleys has changed.
     * @param seed an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a double that typically changes slowly, by less than 2.0, with direction changes at integer inputs
     * @return a pseudo-random double between -1.0 and 1.0 (both exclusive), smoothly changing with value
     */
    public static double swayRandomized(final int seed, double value)
    {
        final int floor = value >= 0.0 ? (int) value : (int) value - 1;
        int z = seed + floor;
        final double start = (((z = (z ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31,
                end = (((z = (seed + floor + 1 ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31;
        value -= floor;
        value *= value * (3.0 - 2.0 * value);
        return (1.0 - value) * start + value * end;
    }

    /**
     * A variant on {@link #swayRandomized(long, float)} that takes an int seed instead of a long, and is optimized for
     * usage on GWT. Like the version with a long seed, this uses cubic interpolation between random peak or valley
     * points; only the method of generating those random peaks and valleys has changed.
     * @param seed an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a float that typically changes slowly, by less than 2.0, with direction changes at integer inputs
     * @return a pseudo-random float between -1f and 1f (both exclusive), smoothly changing with value
     */
    public static float swayRandomized(final int seed, float value)
    {
        final int floor = value >= 0f ? (int) value : (int) value - 1;
        int z = seed + floor;
        final float start = (((z = (z ^ 0xD1B54A35) * 0x102473) ^ (z << 11 | z >>> 21) ^ (z << 19 | z >>> 13)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z) * 0x0.ffffffp-31f,
                end = (((z = (seed + floor + 1 ^ 0xD1B54A35) * 0x102473) ^ (z << 11 | z >>> 21) ^ (z << 19 | z >>> 13)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z) * 0x0.ffffffp-31f;
        value -= floor;
        value *= value * (3 - 2 * value);
        return (1 - value) * start + value * end;
    }
    /**
     * A 1D "noise" method that produces smooth transitions like {@link #sway(float)}, but also wrapping around at pi *
     * 2 so this can be used to get smoothly-changing random angles. Has (seeded) random peaks and valleys where it
     * slows its range of change, but can return any value from 0 to 6.283185307179586f, or pi * 2. The pattern this
     * will produces will be completely different if the seed changes, and the value is expected to be something other
     * than an angle, like time. Uses a simple method of cubic interpolation between random values, where a random value
     * is used without modification when given an integer for {@code value}. Note that this uses a different type of
     * interpolation than {@link #sway(float)}, which uses quintic (this causes swayAngleRandomized() to be slightly
     * faster and simpler).
     * @param seed a long seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a float that typically changes slowly, by less than 1.0, with possible direction changes at integer inputs
     * @return a pseudo-random float between 0f and 283185307179586f (both inclusive), smoothly changing with value and wrapping
     */
    public static float swayAngleRandomized(long seed, float value)
    {
        final long floor = value >= 0f ? (long) value : (long) value - 1L;
        float start = (((seed += floor * 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L) >>> 1) * 0x0.ffffffp-62f,
                end = (((seed += 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L) >>> 1) * 0x0.ffffffp-62f;
        value -= floor;
        value *= value * (3f - 2f * value);
        end = end - start + 1.5f;
        end -= (long)end + 0.5f;
        start += end * value + 1;
        return (start - (long)start) * 6.283185307179586f;
    }
    
    /**
     * Identical to {@link Float#floatToIntBits(float)} on desktop; optimized on GWT. Uses JS typed arrays on GWT, which
     * are well-supported now across all recent browsers and have fallbacks in GWT in the unlikely event of a browser
     * not supporting them.
     * @param value a floating-point number.
     * @return the bits that represent the floating-point number.
     */
    public static int floatToIntBits(final float value)
    {
        return Float.floatToIntBits(value);
    }
    /**
     * Identical to {@link Float#floatToIntBits(float)} on desktop (note, not {@link Float#floatToRawIntBits(float)});
     * optimized on GWT. When compiling to JS via GWT, there is no way to distinguish NaN values with different bits but
     * that are still NaN, so this doesn't try to somehow permit that. Uses JS typed arrays on GWT, which are
     * well-supported now across all recent browsers and have fallbacks in GWT in the unlikely event of a browser not
     * supporting them.
     * @param value a floating-point number.
     * @return the bits that represent the floating-point number.
     */
    public static int floatToRawIntBits(final float value)
    {
        return Float.floatToIntBits(value);
    }


    /**
     * Gets the bit representation of the given float {@code value}, but with reversed byte order. On desktop, this is
     * equivalent to calling {@code Integer.reverseBytes(Float.floatToIntBits(value))}, but it is implemented using
     * typed arrays on GWT.
     * @param value a floating-point number
     * @return the bits that represent the floating-point number, with their byte order reversed from normal.
     */
    public static int floatToReversedIntBits(final float value) {
        return Integer.reverseBytes(Float.floatToIntBits(value));
    }

    /**
     * Reverses the byte order of {@code bits} and converts that to a float. On desktop, this is
     * equivalent to calling {@code Float.intBitsToFloat(Integer.reverseBytes(bits))}, but it is implemented using
     * typed arrays on GWT.
     * @param bits an integer
     * @return the {@code float} floating-point value with the given bits using their byte order reversed from normal.
     */
    public static float reversedIntBitsToFloat(final int bits) {
        return Float.intBitsToFloat(Integer.reverseBytes(bits));
    }

    /**
     * Identical to {@link Float#intBitsToFloat(int)} on desktop; optimized on GWT. Uses JS typed arrays on GWT, which
     * are well-supported now across all recent browsers and have fallbacks in GWT in the unlikely event of a browser
     * not supporting them.
     * @param bits an integer.
     * @return the {@code float} floating-point value with the same bit pattern.
     */
    public static float intBitsToFloat(final int bits)
    {
        return Float.intBitsToFloat(bits);
    }

    /**
     * Gets an 8-bit section of the given float {@code value}, using {@code whichByte} to select whether this should
     * return byte 0 (least significant), 1, 2, or 3 (most significant).
     * @param value a float
     * @param whichByte an int that will be used to select the byte to take from value (any int is allowed, only the bottom 2 bits are used to select)
     * @return the selected byte from the given float
     */
    public static byte getSelectedByte(final float value, final int whichByte)
    {
        return (byte)(Float.floatToIntBits(value) >>> ((whichByte & 3) << 3));
    }

    /**
     * Like {@link #getSelectedByte(float, int)}, this sets the byte at a selected position in the int representation of
     * a float, then returns the float produced by the bit change. Uses {@code whichByte} to select whether this should
     * set byte 0 (least significant), 1, 2, or 3 (most significant). {@code newValue} is a byte.
     * @param value a float
     * @param whichByte an int that will be used to select the byte to take from value (any int is allowed, only the bottom 2 bits are used to select)
     * @param newValue a byte that will be placed into the returned float's bits at the selected position
     * @return a float that results from changing the bits at the selected position to match newValue
     */
    public static float setSelectedByte(final float value, final int whichByte, final byte newValue)
    {
        return Float.intBitsToFloat((Float.floatToIntBits(value) & ~(255 << ((whichByte & 3) << 3)))
                | ((newValue & 255) << ((whichByte & 3) << 3)));
    }

    /**
     * Generates a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive) using the given long seed, passing
     * it once through the (decent-quality and very fast) {@link ThrustAltRNG} algorithm.
     * <br>
     * Consider calling this with {@code NumberTools.randomDouble(++seed)} for an optimal period of 2 to the 64 when
     * repeatedly called, but {@code NumberTools.randomDouble(seed += ODD_LONG)} will also work just fine if ODD_LONG is
     * any odd-number long, positive or negative.
     * @param seed any long to be used as a seed
     * @return a pseudo-random double from 0.0 (inclusive) to 1.0 (exclusive)
     */
    public static double randomDouble(long seed)
    {
        return (((seed = ((seed *= 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 23)) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    /**
     * Generates a pseudo-random double between -1.0 (inclusive) and 1.0 (exclusive) using the given long seed, passing
     * it once through the (decent-quality and very fast) {@link ThrustAltRNG} algorithm.
     * <br>
     * Consider calling this with {@code NumberTools.randomSignedDouble(++seed)} for an optimal period of 2 to the 64
     * when repeatedly called, but {@code NumberTools.randomSignedDouble(seed += ODD_LONG)} will also work just fine if
     * ODD_LONG is any odd-number long, positive or negative.
     * @param seed any long to be used as a seed
     * @return a pseudo-random double from 0.0 (inclusive) to 1.0 (exclusive)
     */
    public static double randomSignedDouble(long seed)
    {
        return (((seed = ((seed *= 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 23)) >> 10) * 0x1p-53;
    }
    /**
     * Generates a pseudo-random float between 0f (inclusive) and 1f (exclusive) using the given long seed, passing it
     * once through the (decent-quality and very fast) {@link ThrustAltRNG} algorithm.
     * <br>
     * Consider calling this with {@code NumberTools.randomFloat(++seed)} for an optimal period of 2 to the 64 when
     * repeatedly called, but {@code NumberTools.randomFloat(seed += ODD_LONG)} will also work just fine if ODD_LONG is
     * any odd-number long, positive or negative.
     * @param seed any long to be used as a seed
     * @return a pseudo-random float from -1.0f (exclusive) to 1.0f (exclusive)
     */
    public static float randomFloat(long seed)
    {
        return (((seed = ((seed *= 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 23)) & 0xFFFFFF) * 0x1p-24f;
    }
    /**
     * Generates a pseudo-random float between -1f (inclusive) and 1f (exclusive) using the given long seed, passing
     * it once through the (decent-quality and very fast) {@link ThrustAltRNG} algorithm. This can be useful as a
     * multiplier that has approximately equal likelihood of changing or leaving the sign of its multiplicand, and won't
     * make the result larger (more significant) but will usually make it closer to 0.
     * <br>
     * Consider calling this with {@code NumberTools.randomDouble(++seed)} for an optimal period of 2 to the 64 when
     * repeatedly called, but {@code NumberTools.randomDouble(seed += ODD_LONG)} will also work just fine if ODD_LONG is
     * any odd-number long, positive or negative.
     * @param seed any long to be used as a seed
     * @return a pseudo-random float from -1.0f (exclusive) to 1.0f (exclusive)
     */
    public static float randomSignedFloat(long seed)
    {
        return (((seed = ((seed *= 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 23)) >> 39) * 0x1p-24f;
    }

    /**
     * Generates a pseudo-random double between -1.0 (exclusive) and 1.0 (exclusive) with a distribution that has a
     * strong central bias (around 0.0). Uses the given long seed, passing it once through the (decent-quality and very
     * fast) {@link ThrustAltRNG} algorithm. This produces a pseudo-random long, which this simply passes to
     * {@link #formCurvedFloat(long)}, since it is already well-suited to generating a curved distribution.
     * <br>
     * Consider calling this with {@code NumberTools.randomFloatCurved(++seed)} for an optimal period of 2 to the 64
     * when repeatedly called, but {@code NumberTools.randomFloatCurved(seed += ODD_LONG)} will also work just fine if
     * ODD_LONG is any odd-number long, positive or negative.
     * @param seed any int to be used as a seed
     * @return a pseudo-random double from -1.0 (exclusive) to 1.0 (exclusive), distributed on a curve centered on 0.0
     */
    public static float randomFloatCurved(long seed)
    {
        return formCurvedFloat(((seed = ((seed *= 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (seed >>> 23)));
    }

    /**
     * Given an int as a seed, this uses its least-significant 23 bits to produce a float between 0f (inclusive) and 1f
     * (exclusive). This does not randomize the seed at all, and the upper 9 bits of the seed are ignored.
     * @param seed an int; only the bottom 23 bits will be used
     * @return a float between 0f (inclusive) and 1f (exclusive)
     */
    public static float formFloat(final int seed)
    {
        return Float.intBitsToFloat((seed & 0x7FFFFF) | 0x3f800000) - 1f;
    }
    /**
     * Given an int as a seed, this uses its least-significant 23 bits to produce a float between -1f (inclusive) and 1f
     * (exclusive). This does not randomize the seed at all, and the upper 9 bits of the seed are ignored.
     * @param seed an int; only the bottom 23 bits will be used
     * @return a float between -1f (inclusive) and 1f (exclusive)
     */
    public static float formSignedFloat(final int seed)
    {
        return Float.intBitsToFloat((seed & 0x7FFFFF) | 0x40000000) - 3f;
    }

    /**
     * Given a long as a seed, this uses its least-significant 52 bits to produce a double between 0 (inclusive) and 1
     * (exclusive). This does not randomize the seed at all, and the upper 12 bits of the seed are ignored.
     * @param seed a long; only the bottom 52 bits will be used
     * @return a double between 0 (inclusive) and 1 (exclusive)
     */
    public static double formDouble(final long seed)
    {
        return Double.longBitsToDouble((seed & 0xfffffffffffffL) | 0x3ff0000000000000L) - 1f;
    }
    /**
     * Given a long as a seed, this uses its least-significant 52 bits to produce a double between -1 (inclusive) and 1
     * (exclusive). This does not randomize the seed at all, and the upper 12 bits of the seed are ignored.
     * @param seed a long; only the bottom 52 bits will be used
     * @return a double between -1 (inclusive) and 1 (exclusive)
     */
    public static double formSignedDouble(final long seed)
    {
        return Double.longBitsToDouble((seed & 0xfffffffffffffL) | 0x4000000000000000L) - 3f;
    }

    /**
     * A different kind of determine-like method that expects to be given a random long and produces a random double
     * with a curved distribution that centers on 0 (where it has a bias) and can (rarely) approach -1f and 1f.
     * The distribution for the values is similar to Irwin-Hall, and is frequently near 0 but not too-rarely near -1.0
     * or 1.0. It cannot produce 1.0, -1.0, or any values further from 0 than those bounds.
     * @param start a long, usually random, such as one produced by any RandomnessSource; all bits will be used
     * @return a deterministic double between -1.0 (exclusive) and 1.0 (exclusive); very likely to be close to 0.0
     */
    public static double formCurvedDouble(long start) {
        return    longBitsToDouble((start >>> 12) | 0x3fe0000000000000L)
                + longBitsToDouble(((start *= 0x2545F4914F6CDD1DL) >>> 12) | 0x3fe0000000000000L)
                - longBitsToDouble(((start *= 0x2545F4914F6CDD1DL) >>> 12) | 0x3fe0000000000000L)
                - longBitsToDouble(((start *  0x2545F4914F6CDD1DL) >>> 12) | 0x3fe0000000000000L)
                ;
    }
    /**
     * A different kind of determine-like method that expects to be given a random long and produces a random double
     * with a curved distribution that centers on 0 (where it has a bias) and can (rarely) approach 0.0 and 1.0.
     * The distribution for the values is similar to Irwin-Hall, and is frequently near 0 but not too-rarely near 0.0 or
     * 1.0. It cannot produce 0.0, 1.0, or any values further from 0.5 than those bounds.
     * @param start a long, usually random, such as one produced by any RandomnessSource; all bits will be used
     * @return a deterministic double between 0.0 (exclusive) and 1.0 (exclusive); very likely to be close to 0.5
     */
    public static double formCurvedDoubleTight(long start) {
        return  0.5
                + longBitsToDouble((start >>> 12) | 0x3fd0000000000000L)
                + longBitsToDouble(((start *= 0x2545F4914F6CDD1DL) >>> 12) | 0x3fd0000000000000L)
                - longBitsToDouble(((start *= 0x2545F4914F6CDD1DL) >>> 12) | 0x3fd0000000000000L)
                - longBitsToDouble(((start *  0x2545F4914F6CDD1DL) >>> 12) | 0x3fd0000000000000L);
    }

    /**
     * A different kind of determine-like method that expects to be given a random long and produces a random float with
     * a curved distribution that centers on 0 (where it has a bias) and can (rarely) approach -1f and 1f.
     * The distribution for the values is similar to Irwin-Hall, and is frequently near 0 but not too-rarely near -1f or
     * 1f. It cannot produce 1f, -1f, or any values further from 0 than those bounds.
     * @param start a long, usually random, such as one produced by any RandomnessSource
     * @return a deterministic float between -1f (exclusive) and 1f (exclusive), that is very likely to be close to 0f
     */
    public static float formCurvedFloat(final long start) {
        return    intBitsToFloat((int)start >>> 9 | 0x3F000000)
                + intBitsToFloat((int) (start >>> 41) | 0x3F000000)
                - intBitsToFloat(((int)(start ^ ~start >>> 20) & 0x007FFFFF) | 0x3F000000)
                - intBitsToFloat(((int) (~start ^ start >>> 30) & 0x007FFFFF) | 0x3F000000)
                ;
    }

    /**
     * A different kind of determine-like method that expects to be given random ints and produces a random float with
     * a curved distribution that centers on 0 (where it has a bias) and can (rarely) approach -1f and 1f.
     * The distribution for the values is similar to Irwin-Hall, and is frequently near 0 but not too-rarely near -1f or
     * 1f. It cannot produce 1f, -1f, or any values further from 0 than those bounds.
     * @param start1 an int usually random, such as one produced by any RandomnessSource
     * @param start2 an int usually random, such as one produced by any RandomnessSource
     * @return a deterministic float between -1f (exclusive) and 1f (exclusive), that is very likely to be close to 0f
     */
    public static float formCurvedFloat(final int start1, final int start2) {
        return    intBitsToFloat(start1 >>> 9 | 0x3F000000)
                + intBitsToFloat((~start1 & 0x007FFFFF) | 0x3F000000)
                - intBitsToFloat(start2 >>> 9 | 0x3F000000)
                - intBitsToFloat((~start2 & 0x007FFFFF) | 0x3F000000)
                ;
    }
    /**
     * A different kind of determine-like method that expects to be given a random int and produces a random float with
     * a curved distribution that centers on 0 (where it has a bias) and can (rarely) approach -1f and 1f.
     * The distribution for the values is similar to Irwin-Hall, and is frequently near 0 but not too-rarely near -1f or
     * 1f. It cannot produce 1f, -1f, or any values further from 0 than those bounds.
     * @param start an int, usually random, such as one produced by any RandomnessSource
     * @return a deterministic float between -1f (exclusive) and 1f (exclusive), that is very likely to be close to 0f
     */
    public static float formCurvedFloat(final int start) {
        return    intBitsToFloat(start >>> 9 | 0x3F000000)
                + intBitsToFloat((start & 0x007FFFFF) | 0x3F000000)
                - intBitsToFloat(((start << 18 & 0x007FFFFF) ^ ~start >>> 14) | 0x3F000000)
                - intBitsToFloat(((start << 13 & 0x007FFFFF) ^ ~start >>> 19) | 0x3F000000)
                ;
    }

    /**
     * Returns an int value with at most a single one-bit, in the position of the lowest-order ("rightmost") one-bit in
     * the specified int value. Returns zero if the specified value has no one-bits in its two's complement binary
     * representation, that is, if it is equal to zero.
     * <br>
     * Identical to {@link Integer#lowestOneBit(int)}, but super-sourced to act correctly on GWT. If you have GWT as a
     * target and do bit manipulation work, double-check everything! An int can be higher than {@link Integer#MAX_VALUE}
     * or lower than {@link Integer#MIN_VALUE} on GWT, without actually being a long (internally it's a double). This
     * is especially relevant for the overload of this method that takes and returns a long;
     * {@link Long#lowestOneBit(long)} does not provide correct results for certain inputs on GWT, such as
     * -17592186044416L, which it mysteriously returns 0L on, so you should use {@link #lowestOneBit(long)}.
     * @param num the value whose lowest one bit is to be computed
     * @return an int value with a single one-bit, in the position of the lowest-order one-bit in the specified value,
     *         or zero if the specified value is itself equal to zero.
     */
    public static int lowestOneBit(int num)
    {
        return num & -num;
    }
    /**
     * Returns an long value with at most a single one-bit, in the position of the lowest-order ("rightmost") one-bit in
     * the specified long value. Returns zero if the specified value has no one-bits in its two's complement binary
     * representation, that is, if it is equal to zero.
     * <br>
     * Identical to {@link Long#lowestOneBit(long)}, but super-sourced to act correctly on GWT. If you have GWT as a
     * target and do bit manipulation work, double-check everything! An int can be higher than {@link Integer#MAX_VALUE}
     * or lower than {@link Integer#MIN_VALUE} on GWT, without actually being a long (internally it's a double). This
     * is especially relevant for this overload (for longs more so than for ints); {@link Long#lowestOneBit(long)} does
     * not provide correct results for certain inputs on GWT, such as -17592186044416L, which it mysteriously returns 0L
     * on, so you should use this method.
     * @param num the value whose lowest one bit is to be computed
     * @return a long value with a single one-bit, in the position of the lowest-order one-bit in the specified value,
     *         or zero if the specified value is itself equal to zero.
     */
    public static long lowestOneBit(long num)
    {
        return num & -num;
    }

    /**
     * A fairly-close approximation of {@link Math#sin(double)} that can be significantly faster (between 8x and 80x
     * faster sin() calls in benchmarking; if you have access to libGDX you should consider its sometimes-more-precise
     * and sometimes-faster MathUtils.sin() method. Because this method doesn't rely on a
     * lookup table, where libGDX's MathUtils does, applications that have a bottleneck on memory may perform better
     * with this method than with MathUtils. Takes the same arguments Math.sin() does, so one angle in radians,
     * which may technically be any double (but this will lose precision on fairly large doubles, such as those that are
     * larger than {@link Long#MAX_VALUE}, because those doubles themselves will lose precision at that scale). This
     * is closely related to {@link #sway(double)}, but the shape of the output when graphed is almost identical to
     * sin(). The difference between the result of this method and {@link Math#sin(double)} should be under 0.0011 at
     * all points between -pi and pi, with an average difference of about 0.0005; not all points have been checked for
     * potentially higher errors, though.
     * <br>
     * The error for this double version is extremely close to the float version, {@link #sin(float)}, so you should
     * choose based on what type you have as input and/or want to return rather than on quality concerns. Coercion
     * between float and double takes about as long as this method normally takes to run (or longer), so if you have
     * floats you should usually use methods that take floats (or return floats, if assigning the result to a float),
     * and likewise for doubles.
     * <br>
     * Unlike in previous versions of this method, the sign of the input doesn't affect performance here, at least not
     * by a measurable amount.
     * <br>
     * The technique for sine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any double to the valid input range.
     * @param radians an angle in radians as a double, often from 0 to pi * 2, though not required to be.
     * @return the sine of the given angle, as a double between -1.0 and 1.0 (both inclusive)
     */

    public static double sin(double radians)
    {
        radians *= 0.6366197723675814;
        final long floor = (radians >= 0.0 ? (long) radians : (long) radians - 1L) & -2L;
        radians -= floor;
        radians *= 2.0 - radians;
        return radians * (-0.775 - 0.225 * radians) * ((floor & 2L) - 1L);
    }

    /**
     * A fairly-close approximation of {@link Math#cos(double)} that can be significantly faster (between 8x and 80x
     * faster cos() calls in benchmarking; if you have access to libGDX you should consider its sometimes-more-precise
     * and sometimes-faster MathUtils.cos() method. Because this method doesn't rely on a
     * lookup table, where libGDX's MathUtils does, applications that have a bottleneck on memory may perform better
     * with this method than with MathUtils. Takes the same arguments Math.cos() does, so one angle in radians,
     * which may technically be any double (but this will lose precision on fairly large doubles, such as those that are
     * larger than {@link Long#MAX_VALUE}, because those doubles themselves will lose precision at that scale). This
     * is closely related to {@link #sway(double)}, but the shape of the output when graphed is almost identical to
     * cos(). The difference between the result of this method and {@link Math#cos(double)} should be under 0.0011 at
     * all points between -pi and pi, with an average difference of about 0.0005; not all points have been checked for
     * potentially higher errors, though.
     * <br>
     * The error for this double version is extremely close to the float version, {@link #cos(float)}, so you should
     * choose based on what type you have as input and/or want to return rather than on quality concerns. Coercion
     * between float and double takes about as long as this method normally takes to run (or longer), so if you have
     * floats you should usually use methods that take floats (or return floats, if assigning the result to a float),
     * and likewise for doubles.
     * <br>
     * Unlike in previous versions of this method, the sign of the input doesn't affect performance here, at least not
     * by a measurable amount.
     * The technique for cosine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any double to the valid input range.
     * @param radians an angle in radians as a double, often from 0 to pi * 2, though not required to be.
     * @return the cosine of the given angle, as a double between -1.0 and 1.0 (both inclusive)
     */
    public static double cos(double radians)
    {
        radians = radians * 0.6366197723675814 + 1.0;
        final long floor = (radians >= 0.0 ? (long) radians : (long) radians - 1L) & -2L;
        radians -= floor;
        radians *= 2.0 - radians;
        return radians * (-0.775 - 0.225 * radians) * ((floor & 2L) - 1L);
    }

    /**
     * A fairly-close approximation of {@link Math#sin(double)} that can be significantly faster (between 8x and 80x
     * faster sin() calls in benchmarking, and both takes and returns floats; if you have access to libGDX you should
     * consider its more-precise and sometimes-faster MathUtils.sin() method. Because this method doesn't rely on a
     * lookup table, where libGDX's MathUtils does, applications that have a bottleneck on memory may perform better
     * with this method than with MathUtils. Takes the same arguments Math.sin() does, so one angle in radians,
     * which may technically be any float (but this will lose precision on fairly large floats, such as those that are
     * larger than {@link Integer#MAX_VALUE}, because those floats themselves will lose precision at that scale). This
     * is closely related to {@link #sway(float)}, but the shape of the output when graphed is almost identical to
     * sin(). The difference between the result of this method and {@link Math#sin(double)} should be under 0.0011 at
     * all points between -pi and pi, with an average difference of about 0.0005; not all points have been checked for
     * potentially higher errors, though.
     * <br>
     * The error for this float version is extremely close to the double version, {@link #sin(double)}, so you should
     * choose based on what type you have as input and/or want to return rather than on quality concerns. Coercion
     * between float and double takes about as long as this method normally takes to run (or longer), so if you have
     * floats you should usually use methods that take floats (or return floats, if assigning the result to a float),
     * and likewise for doubles.
     * <br>
     * Unlike in previous versions of this method, the sign of the input doesn't affect performance here, at least not
     * by a measurable amount.
     * <br>
     * The technique for sine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any float to the valid input range.
     * @param radians an angle in radians as a float, often from 0 to pi * 2, though not required to be.
     * @return the sine of the given angle, as a float between -1f and 1f (both inclusive)
     */
    public static float sin(float radians)
    {
        radians *= 0.6366197723675814f;
        final int floor = (radians >= 0.0 ? (int) radians : (int) radians - 1) & -2;
        radians -= floor;
        radians *= 2f - radians;
        return radians * (-0.775f - 0.225f * radians) * ((floor & 2) - 1);
    }

    /**
     * A fairly-close approximation of {@link Math#cos(double)} that can be significantly faster (between 8x and 80x
     * faster cos() calls in benchmarking, and both takes and returns floats; if you have access to libGDX you should
     * consider its more-precise and sometimes-faster MathUtils.cos() method. Because this method doesn't rely on a
     * lookup table, where libGDX's MathUtils does, applications that have a bottleneck on memory may perform better
     * with this method than with MathUtils. Takes the same arguments Math.cos() does, so one angle in radians,
     * which may technically be any float (but this will lose precision on fairly large floats, such as those that are
     * larger than {@link Integer#MAX_VALUE}, because those floats themselves will lose precision at that scale). This
     * is closely related to {@link #sway(float)}, but the shape of the output when graphed is almost identical to
     * cos(). The difference between the result of this method and {@link Math#cos(double)} should be under 0.0011 at
     * all points between -pi and pi, with an average difference of about 0.0005; not all points have been checked for
     * potentially higher errors, though.
     * <br>
     * The error for this float version is extremely close to the double version, {@link #cos(double)}, so you should
     * choose based on what type you have as input and/or want to return rather than on quality concerns. Coercion
     * between float and double takes about as long as this method normally takes to run (or longer), so if you have
     * floats you should usually use methods that take floats (or return floats, if assigning the result to a float),
     * and likewise for doubles.
     * <br>
     * Unlike in previous versions of this method, the sign of the input doesn't affect performance here, at least not
     * by a measurable amount.
     * <br>
     * The technique for cosine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any float to the valid input range.
     * @param radians an angle in radians as a float, often from 0 to pi * 2, though not required to be.
     * @return the cosine of the given angle, as a float between -1f and 1f (both inclusive)
     */
    public static float cos(float radians)
    {
        radians = radians * 0.6366197723675814f + 1f;
        final int floor = (radians >= 0.0 ? (int) radians : (int) radians - 1) & -2;
        radians -= floor;
        radians *= 2f - radians;
        return radians * (-0.775f - 0.225f * radians) * ((floor & 2) - 1);
    }
    /**
     * A fairly-close approximation of {@link Math#sin(double)} that can be significantly faster (between 8x and 80x
     * faster sin() calls in benchmarking, and both takes and returns floats; if you have access to libGDX, you should
     * consider its more-precise and sometimes-faster MathUtils.sinDeg() method. Because this method doesn't rely on a
     * lookup table, where libGDX's MathUtils does, applications that have a bottleneck on memory may perform better
     * with this method than with MathUtils. Takes one angle in degrees,
     * which may technically be any float (but this will lose precision on fairly large floats, such as those that are
     * larger than {@link Integer#MAX_VALUE}, because those floats themselves will lose precision at that scale). This
     * is closely related to {@link #sway(float)}, but the shape of the output when graphed is almost identical to
     * sin(). The difference between the result of this method and {@link Math#sin(double)} should be under 0.0011 at
     * all points between -360 and 360, with an average difference of about 0.0005; not all points have been checked for
     * potentially higher errors, though.
     * <br>
     * The error for this float version is extremely close to the double version, {@link #sin(double)}, so you should
     * choose based on what type you have as input and/or want to return rather than on quality concerns. Coercion
     * between float and double takes about as long as this method normally takes to run (or longer), so if you have
     * floats you should usually use methods that take floats (or return floats, if assigning the result to a float),
     * and likewise for doubles.
     * <br>
     * Unlike in previous versions of this method, the sign of the input doesn't affect performance here, at least not
     * by a measurable amount.
     * <br>
     * The technique for sine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any float to the valid input range.
     * @param degrees an angle in degrees as a float, often from 0 to 360, though not required to be.
     * @return the sine of the given angle, as a float between -1f and 1f (both inclusive)
     */
    public static float sinDegrees(float degrees)
    {
        degrees = degrees * 0.011111111111111112f;
        final int floor = (degrees >= 0.0 ? (int) degrees : (int) degrees - 1) & -2;
        degrees -= floor;
        degrees *= 2f - degrees;
        return degrees * (-0.775f - 0.225f * degrees) * ((floor & 2) - 1);
    }

    /**
     * A fairly-close approximation of {@link Math#cos(double)} that can be significantly faster (between 8x and 80x
     * faster cos() calls in benchmarking, and both takes and returns floats; if you have access to libGDX, you should
     * consider its more-precise and sometimes-faster MathUtils.cosDeg() method. Because this method doesn't rely on a
     * lookup table, where libGDX's MathUtils does, applications that have a bottleneck on memory may perform better
     * with this method than with MathUtils. Takes one angle in degrees,
     * which may technically be any float (but this will lose precision on fairly large floats, such as those that are
     * larger than {@link Integer#MAX_VALUE}, because those floats themselves will lose precision at that scale). This
     * is closely related to {@link #sway(float)}, but the shape of the output when graphed is almost identical to
     * cos(). The difference between the result of this method and {@link Math#cos(double)} should be under 0.0011 at
     * all points between -360 and 360, with an average difference of about 0.0005; not all points have been checked for
     * potentially higher errors, though.
     * <br>
     * The error for this float version is extremely close to the double version, {@link #cos(double)}, so you should
     * choose based on what type you have as input and/or want to return rather than on quality concerns. Coercion
     * between float and double takes about as long as this method normally takes to run (or longer), so if you have
     * floats you should usually use methods that take floats (or return floats, if assigning the result to a float),
     * and likewise for doubles.
     * <br>
     * Unlike in previous versions of this method, the sign of the input doesn't affect performance here, at least not
     * by a measurable amount.
     * <br>
     * The technique for cosine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any float to the valid input range.
     * @param degrees an angle in degrees as a float, often from 0 to pi * 2, though not required to be.
     * @return the cosine of the given angle, as a float between -1f and 1f (both inclusive)
     */
    public static float cosDegrees(float degrees)
    {
        degrees = degrees * 0.011111111111111112f + 1f;
        final int floor = (degrees >= 0.0 ? (int) degrees : (int) degrees - 1) & -2;
        degrees -= floor;
        degrees *= 2f - degrees;
        return degrees * (-0.775f - 0.225f * degrees) * ((floor & 2) - 1);
    }

    /**
     * A variation on {@link Math#sin(double)} that takes its input as a fraction of a turn instead of in radians; one
     * turn is equal to 360 degrees or two*PI radians. This can be useful as a building block for other measurements;
     * to make a sine method that takes its input in grad (with 400 grad equal to 360 degrees), you would just divide
     * the grad value by 400.0 (or multiply it by 0.0025) and pass it to this method. Similarly for binary degrees, also
     * called brad (with 256 brad equal to 360 degrees), you would divide by 256.0 or multiply by 0.00390625 before
     * passing that value here. The brad case is especially useful because you can use a byte for any brad values, and
     * adding up those brad values will wrap correctly (256 brad goes back to 0) while keeping perfect precision for the
     * results (you still divide by 256.0 when you pass the brad value to this method).
     * <br>
     * The error for this double version is extremely close to the float version, {@link #sin_(float)}, so you should
     * choose based on what type you have as input and/or want to return rather than on quality concerns. Coercion
     * between float and double takes about as long as this method normally takes to run (or longer), so if you have
     * floats you should usually use methods that take floats (or return floats, if assigning the result to a float),
     * and likewise for doubles.
     * <br>
     * The technique for sine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any double to the valid input range.
     * @param turns an angle as a fraction of a turn as a double, with 0.5 here equivalent to PI radians in {@link #cos(double)}
     * @return the sine of the given angle, as a double between -1.0 and 1.0 (both inclusive)
     */
    public static double sin_(double turns)
    {
        turns *= 4.0;
        final long floor = (turns >= 0.0 ? (long) turns : (long) turns - 1L) & -2L;
        turns -= floor;
        turns *= 2.0 - turns;
        return turns * (-0.775 - 0.225 * turns) * ((floor & 2L) - 1L);
    }

    /**
     * A variation on {@link Math#cos(double)} that takes its input as a fraction of a turn instead of in radians; one
     * turn is equal to 360 degrees or two*PI radians. This can be useful as a building block for other measurements;
     * to make a cosine method that takes its input in grad (with 400 grad equal to 360 degrees), you would just divide
     * the grad value by 400.0 (or multiply it by 0.0025) and pass it to this method. Similarly for binary degrees, also
     * called brad (with 256 brad equal to 360 degrees), you would divide by 256.0 or multiply by 0.00390625 before
     * passing that value here. The brad case is especially useful because you can use a byte for any brad values, and
     * adding up those brad values will wrap correctly (256 brad goes back to 0) while keeping perfect precision for the
     * results (you still divide by 256.0 when you pass the brad value to this method).
     * <br>
     * The error for this double version is extremely close to the float version, {@link #cos_(float)}, so you should
     * choose based on what type you have as input and/or want to return rather than on quality concerns. Coercion
     * between float and double takes about as long as this method normally takes to run (or longer), so if you have
     * floats you should usually use methods that take floats (or return floats, if assigning the result to a float),
     * and likewise for doubles.
     * <br>
     * The technique for cosine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any double to the valid input range.
     * @param turns an angle as a fraction of a turn as a double, with 0.5 here equivalent to PI radians in {@link #cos(double)}
     * @return the cosine of the given angle, as a double between -1.0 and 1.0 (both inclusive)
     */
    public static double cos_(double turns)
    {
        turns = turns * 4.0 + 1.0;
        final long floor = (turns >= 0.0 ? (long) turns : (long) turns - 1L) & -2L;
        turns -= floor;
        turns *= 2.0 - turns;
        return turns * (-0.775 - 0.225 * turns) * ((floor & 2L) - 1L);
    }

    /**
     * A variation on {@link Math#sin(double)} that takes its input as a fraction of a turn instead of in radians (it
     * also takes and returns a float); one turn is equal to 360 degrees or two*PI radians. This can be useful as a
     * building block for other measurements; to make a sine method that takes its input in grad (with 400 grad equal to
     * 360 degrees), you would just divide the grad value by 400.0 (or multiply it by 0.0025) and pass it to this
     * method. Similarly for binary degrees, also called brad (with 256 brad equal to 360 degrees), you would divide by
     * 256.0 or multiply by 0.00390625 before passing that value here. The brad case is especially useful because you
     * can use a byte for any brad values, and adding up those brad values will wrap correctly (256 brad goes back to 0)
     * while keeping perfect precision for the results (you still divide by 256.0 when you pass the brad value to this
     * method).
     * <br>
     * The error for this float version is extremely close to the double version, {@link #sin_(double)}, so you should
     * choose based on what type you have as input and/or want to return rather than on quality concerns. Coercion
     * between float and double takes about as long as this method normally takes to run (or longer), so if you have
     * floats you should usually use methods that take floats (or return floats, if assigning the result to a float),
     * and likewise for doubles.
     * <br>
     * The technique for sine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any double to the valid input range.
     * @param turns an angle as a fraction of a turn as a float, with 0.5 here equivalent to PI radians in {@link #cos(double)}
     * @return the sine of the given angle, as a float between -1.0 and 1.0 (both inclusive)
     */
    public static float sin_(float turns)
    {
        turns *= 4f;
        final long floor = (turns >= 0.0 ? (long) turns : (long) turns - 1L) & -2L;
        turns -= floor;
        turns *= 2f - turns;
        return turns * (-0.775f - 0.225f * turns) * ((floor & 2L) - 1L);
    }

    /**
     * A variation on {@link Math#cos(double)} that takes its input as a fraction of a turn instead of in radians (it
     * also takes and returns a float); one turn is equal to 360 degrees or two*PI radians. This can be useful as a
     * building block for other measurements; to make a cosine method that takes its input in grad (with 400 grad equal
     * to 360 degrees), you would just divide the grad value by 400.0 (or multiply it by 0.0025) and pass it to this
     * method. Similarly for binary degrees, also called brad (with 256 brad equal to 360 degrees), you would divide by
     * 256.0 or multiply by 0.00390625 before passing that value here. The brad case is especially useful because you
     * can use a byte for any brad values, and adding up those brad values will wrap correctly (256 brad goes back to 0)
     * while keeping perfect precision for the results (you still divide by 256.0 when you pass the brad value to this
     * method).
     * <br>
     * The error for this float version is extremely close to the float version, {@link #cos_(double)}, so you should
     * choose based on what type you have as input and/or want to return rather than on quality concerns. Coercion
     * between float and double takes about as long as this method normally takes to run (or longer), so if you have
     * floats you should usually use methods that take floats (or return floats, if assigning the result to a float),
     * and likewise for doubles.
     * <br>
     * The technique for cosine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any double to the valid input range.
     * @param turns an angle as a fraction of a turn as a float, with 0.5 here equivalent to PI radians in {@link #cos(double)}
     * @return the cosine of the given angle, as a float between -1.0 and 1.0 (both inclusive)
     */
    public static float cos_(float turns)
    {
        turns = turns * 4f + 1f;
        final long floor = (turns >= 0.0 ? (long) turns : (long) turns - 1L) & -2L;
        turns -= floor;
        turns *= 2f - turns;
        return turns * (-0.775f - 0.225f * turns) * ((floor & 2L) - 1L);
    }

    /**
     * Close approximation of the frequently-used trigonometric method atan2, with higher precision than libGDX's atan2
     * approximation. Maximum error is below 0.001 radians.
     * Takes y and x (in that unusual order) as doubles, and returns the angle from the origin to that point in radians.
     * It is about 5 times faster than {@link Math#atan2(double, double)} (roughly 17 ns instead of roughly 88 ns for
     * Math, though the computer was under some load during testing). It is almost identical in speed to libGDX'
     * MathUtils approximation of the same method; MathUtils seems to have worse average error, though.
     * Credit to Nic Taylor and imuli, with Taylor publishing
     * <a href="https://www.dsprelated.com/showarticle/1052.php">this nice post</a> and imuli commenting with very
     * handy information that makes this approach usable. This method changed from an earlier
     * technique that was twice as fast but had very poor quality, enough to be visually noticeable. See also
     * {@link #atan2_(double, double)} if you don't want a mess converting to degrees or some other measurement, since
     * that method returns an angle from 0.0 (equal to 0 degrees) to 1.0 (equal to 360 degrees).
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in radians as a double; ranges from -PI to PI
     */
    public static double atan2(double y, double x)
    {
        if(y == 0.0 && x >= 0.0) return 0.0;
        double ay = Math.abs(y), ax = Math.abs(x);
        boolean invert = ay > ax;
        double z = invert ? ax / ay : ay / ax;
        z = ((((0.141499  * z) - 0.343315 ) * z - 0.016224 ) * z + 1.003839 ) * z - 0.000158 ;
        if (invert) z = 1.5707963267948966 - z;
        if (x < 0) z = 3.141592653589793 - z;
        return Math.copySign(z, y);
    }

    /**
     * Close approximation of the frequently-used trigonometric method atan2, with higher precision than libGDX's atan2
     * approximation. Maximum error is below 0.001 radians.
     * Takes y and x (in that unusual order) as floats, and returns the angle from the origin to that point in radians.
     * It is about 5 times faster than {@link Math#atan2(double, double)} (roughly 17 ns instead of roughly 88 ns for
     * Math, though the computer was under some load during testing). It is almost identical in speed to libGDX'
     * MathUtils approximation of the same method; MathUtils seems to have worse average error, though.
     * Credit to Nic Taylor and imuli, with Taylor publishing
     * <a href="https://www.dsprelated.com/showarticle/1052.php">this nice post</a> and imuli commenting with very
     * handy information that makes this approach usable. This method changed from an earlier
     * technique that was twice as fast but had very poor quality, enough to be visually noticeable. See also
     * {@link #atan2_(float, float)} if you don't want a mess converting to degrees or some other measurement, since
     * that method returns an angle from 0f (equal to 0 degrees) to 1f (equal to 360 degrees).
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in radians as a float; ranges from -PI to PI
     */
    public static float atan2(float y, float x) {
        if (y == 0f && x >= 0f) return 0f;
        float ay = Math.abs(y), ax = Math.abs(x);
        boolean invert = ay > ax;
        float z = invert ? ax / ay : ay / ax;
        z = ((((0.141499f * z) - 0.343315f) * z - 0.016224f) * z + 1.003839f) * z - 0.000158f;
        if (invert) z = 1.5707963267948966f - z;
        if (x < 0) z = 3.141592653589793f - z;
        return Math.copySign(z, y);
    }

    /**
     * Altered-range approximation of the frequently-used trigonometric method atan2, taking y and x positions as 
     * doubles and returning an angle measured in turns from 0.0 to 1.0 (inclusive), with one cycle over the range
     * equivalent to 360 degrees or 2PI radians. You can multiply the angle by {@code 6.2831855f} to change to radians,
     * or by {@code 360f} to change to degrees. Takes y and x (in that unusual order) as doubles. Will never return a
     * negative number, which may help avoid costly floating-point modulus when you actually want a positive number.
     * Credit to Nic Taylor and imuli, with Taylor publishing
     * <a href="https://www.dsprelated.com/showarticle/1052.php">this nice post</a> and imuli commenting with very
     * handy information that makes this approach usable. Note that
     * {@link #atan2(double, double)} returns an angle in radians and can return negative results, which may be fine for
     * many tasks; these two methods are extremely close in implementation and speed.
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, as a double from 0.0 to 1.0, inclusive
     */
    public static double atan2_(double y, double x)
    {
        if(y == 0.0 && x >= 0.0) return 0.0;
        double ay = Math.abs(y), ax = Math.abs(x);
        boolean invert = ay > ax;
        double z = invert ? ax/ay : ay/ax;
        z = (((((0.022520265292560102) * z) - (0.054640279287594046)) * z - (0.0025821297967229097)) * z + (0.1597659389184251)) * z - (0.000025146481008519463);
        if(invert) z = 0.25 - z;
        if(x < 0) z = 0.5 - z;
        return y < 0 ? (int)(1+z) - z : z;

    }
    /**
     * Altered-range approximation of the frequently-used trigonometric method atan2, taking y and x positions as floats
     * and returning an angle measured in turns from 0.0f to 1.0f, with one cycle over the range equivalent to 360
     * degrees or 2PI radians. You can multiply the angle by {@code 6.2831855f} to change to radians, or by {@code 360f}
     * to change to degrees. Takes y and x (in that unusual order) as floats. Will never return a negative number, which
     * may help avoid costly floating-point modulus when you actually want a positive number.
     * Credit to Nic Taylor and imuli, with Taylor publishing
     * <a href="https://www.dsprelated.com/showarticle/1052.php">this nice post</a> and imuli commenting with very
     * handy information that makes this approach usable. Note that
     * {@link #atan2(float, float)} returns an angle in radians and can return negative results, which may be fine for
     * many tasks; these two methods are extremely close in implementation and speed.
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, as a float from 0.0f to 1.0f, inclusive
     */
    public static float atan2_(float y, float x)
    {
        if(y == 0.0 && x >= 0.0) return 0f;
        float ay = Math.abs(y), ax = Math.abs(x);
        boolean invert = ay > ax;
        float z = invert ? ax/ay : ay/ax;
        z = (((((0.022520265292560102f) * z) - (0.054640279287594046f)) * z - (0.0025821297967229097f)) * z + (0.1597659389184251f)) * z - (0.000025146481008519463f);
        if(invert) z = 0.25f - z;
        if(x < 0) z = 0.5f - z;
        return y < 0 ? (int)(1+z) - z : z;
    }

    /**
     * Close approximation of the frequently-used trigonometric method atan2, with higher precision than libGDX's atan2
     * approximation, and giving a result in degrees from -180 to 180. Maximum error is below 0.1 degrees.
     * Takes y and x (in that unusual order) as doubles, and returns the angle from the origin to that point in degrees.
     * It is about 5 times faster than {@link Math#atan2(double, double)} (roughly 17 ns instead of roughly 88 ns for
     * Math, though the computer was under some load during testing). It is almost identical in speed to libGDX'
     * MathUtils approximation after converting to degrees; MathUtils seems to have worse average error, though.
     * Credit to Nic Taylor and imuli, with Taylor publishing
     * <a href="https://www.dsprelated.com/showarticle/1052.php">this nice post</a> and imuli commenting with very
     * handy information that makes this approach usable.
     * <br>
     * See also {@link #atan2Degrees360(double, double)}, which is just like this but returns an angle from 0 to 360,
     * instead of -180 to 180, in case negative angles are undesirable.
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in degrees as a double
     */
    public static double atan2Degrees(double y, double x)
    {
        if(y == 0.0 && x >= 0.0) return 0.0;
        double ay = Math.abs(y), ax = Math.abs(x);
        boolean invert = ay > ax;
        double z = invert ? ax / ay : ay / ax;
        z = (((((8.107295505321636)  * z) - (19.670500543533855) ) * z - (0.9295667268202475) ) * z + (57.51573801063304) ) * z - (0.009052733163067006) ;
        if (invert) z = 90 - z;
        if (x < 0) z = 180 - z;
        return Math.copySign(z, y);
    }

    /**
     * Close approximation of the frequently-used trigonometric method atan2, with higher precision than libGDX's atan2
     * approximation, and giving a result in degrees from -180 to 180. Maximum error is below 0.1 degrees.
     * Takes y and x (in that unusual order) as floats, and returns the angle from the origin to that point in degrees.
     * It is about 5 times faster than {@link Math#atan2(double, double)} (roughly 17 ns instead of roughly 88 ns for
     * Math, though the computer was under some load during testing). It is almost identical in speed to libGDX'
     * MathUtils approximation after converting to degrees; MathUtils seems to have worse average error, though.
     * Credit to Nic Taylor and imuli, with Taylor publishing
     * <a href="https://www.dsprelated.com/showarticle/1052.php">this nice post</a> and imuli commenting with very
     * handy information that makes this approach usable.
     * <br>
     * See also {@link #atan2Degrees360(float, float)}, which is just like this but returns an angle from 0 to 360,
     * instead of -180 to 180, in case negative angles are undesirable.
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in degrees as a float
     */
    public static float atan2Degrees(float y, float x)
    {
        if(y == 0f && x >= 0f) return 0f;
        float ax = Math.abs(x), ay = Math.abs(y);
        boolean invert = ay > ax;
        float z = invert ? ax / ay : ay / ax;
        z = (((((8.107295505321636f)  * z) - (19.670500543533855f) ) * z - (0.9295667268202475f) ) * z + (57.51573801063304f) ) * z - (0.009052733163067006f) ;
        if (invert) z = 90 - z;
        if (x < 0) z = 180 - z;
        return Math.copySign(z, y);
    }

    /**
     * Close approximation of the frequently-used trigonometric method atan2, with higher precision than libGDX's atan2
     * approximation, and giving a result in degrees from 0 to 360 (both inclusive). Maximum error is below 0.1 degrees.
     * Takes y and x (in that unusual order) as doubles, and returns the angle from the origin to that point in degrees.
     * It is about 5 times faster than {@link Math#atan2(double, double)} (roughly 17 ns instead of roughly 88 ns for
     * Math, though the computer was under some load during testing). It is almost identical in speed to libGDX'
     * MathUtils approximation after converting to degrees; MathUtils seems to have worse average error, though.
     * Credit to Nic Taylor and imuli, with Taylor publishing
     * <a href="https://www.dsprelated.com/showarticle/1052.php">this nice post</a> and imuli commenting with very
     * handy information that makes this approach usable.
     * <br>
     * See also {@link #atan2Degrees(double, double)}, which is just like this but returns an angle from -180 to 180,
     * matching {@link Math#atan2(double, double)}'s convention.
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in degrees as a double
     */
    public static double atan2Degrees360(double y, double x)
    {
        if(y == 0.0 && x >= 0.0) return 0.0;
        double ay = Math.abs(y), ax = Math.abs(x);
        boolean invert = ay > ax;
        double z = invert ? ax / ay : ay / ax;
        z = (((((8.107295505321636)  * z) - (19.670500543533855) ) * z - (0.9295667268202475) ) * z + (57.51573801063304) ) * z - (0.009052733163067006) ;
        if (invert) z = 90 - z;
        if (x < 0) z = 180 - z;
        return y < 0 ? 360 - z : z;
    }
    /**
     * Close approximation of the frequently-used trigonometric method atan2, with higher precision than libGDX's atan2
     * approximation, and giving a result in degrees from 0 to 360 (both inclusive). Maximum error is below 0.1 degrees.
     * Takes y and x (in that unusual order) as floats, and returns the angle from the origin to that point in degrees.
     * It is about 5 times faster than {@link Math#atan2(double, double)} (roughly 17 ns instead of roughly 88 ns for
     * Math, though the computer was under some load during testing). It is almost identical in speed to libGDX'
     * MathUtils approximation after converting to degrees; MathUtils seems to have worse average error, though.
     * Credit to Nic Taylor and imuli, with Taylor publishing
     * <a href="https://www.dsprelated.com/showarticle/1052.php">this nice post</a> and imuli commenting with very
     * handy information that makes this approach usable.
     * <br>
     * See also {@link #atan2Degrees(float, float)}, which is just like this but returns an angle from -180 to 180,
     * matching {@link Math#atan2(double, double)}'s convention.
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in degrees as a float
     */
    public static float atan2Degrees360(float y, float x)
    {
        if(y == 0f && x >= 0f) return 0f;
        float ax = Math.abs(x), ay = Math.abs(y);
        boolean invert = ay > ax;
        float z = invert ? ax / ay : ay / ax;
        z = (((((8.107295505321636f)  * z) - (19.670500543533855f) ) * z - (0.9295667268202475f) ) * z + (57.51573801063304f) ) * z - (0.009052733163067006f) ;
        if (invert) z = 90 - z;
        if (x < 0) z = 180 - z;
        return y < 0 ? 360 - z : z;
    }


    //    /**
//     * Arc sine approximation with fairly low error while still being faster than {@link NumberTools#sin(double)}.
//     * This formula is number 201 in <a href=">http://www.fastcode.dk/fastcodeproject/articles/index.htm">Dennis
//     * Kjaer Christensen's unfinished math work on arc sine approximation</a>. This method is about 40 times faster
//     * than {@link Math#asin(double)}. Fast but imprecise.
//     * @param a an input to the inverse sine function, from -1 to 1 inclusive (error is higher approaching -1 or 1)
//     * @return an output from the inverse sine function, from -PI/2 to PI/2 inclusive.
//     */
//    public static double asin(double a) {
//        return (a * (1.0 + (a *= a) * (-0.141514171442891431 + a * -0.719110791477959357))) /
//                (1.0 + a * (-0.439110389941411144 + a * -0.471306172023844527));
//    }
//    /**
//     * Arc sine approximation with fairly low error while still being faster than {@link NumberTools#sin(float)}.
//     * This formula is number 201 in <a href=">http://www.fastcode.dk/fastcodeproject/articles/index.htm">Dennis
//     * Kjaer Christensen's unfinished math work on arc sine approximation</a>. This method is about 40 times faster
//     * than {@link Math#asin(double)}, and takes and returns a float. Fast but imprecise.
//     * @param a an input to the inverse sine function, from -1 to 1 inclusive (error is higher approaching -1 or 1)
//     * @return an output from the inverse sine function, from -PI/2 to PI/2 inclusive.
//     */
//    public static float asin(float a) {
//        return (a * (1f + (a *= a) * (-0.141514171442891431f + a * -0.719110791477959357f))) /
//                (1f + a * (-0.439110389941411144f + a * -0.471306172023844527f));
//    }
    /**
     * Arc sine approximation with very low error, based on a simplified version of {@link #atan2(double, double)}.
     * This method is usually much faster than {@link Math#asin(double)}, but is somewhat less precise than Math's
     * implementation). It is currently more precise than libGDX's approximation in their MathUtils, but this isn't
     * quite as fast; the difference in precision is hard to spot but can be noticeable in some usage.
     * @param n an input to the inverse sine function, from -1 to 1 inclusive
     * @return an output from the inverse sine function, from PI/-2.0 to PI/2.0 inclusive.
     */
    public static float asin(final float n)
    {
        final float ax = (float) Math.sqrt(1f - n * n), ay = Math.abs(n);
        if(ax < ay)
        {
            final float a = ax / ay, s = a * a,
                    r = 1.57079637f - (((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a);
            return (n < 0f) ? -r : r;
        }
        else {
            final float a = ay / ax, s = a * a,
                    r = (((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a);
            return (n < 0f) ? -r : r;
        }
    }

//    /**
//     * Arc cosine approximation with fairly low error while still being faster than {@link NumberTools#cos(double)}.
//     * This formula is number 201 in <a href=">http://www.fastcode.dk/fastcodeproject/articles/index.htm">Dennis
//     * Kjaer Christensen's unfinished math work on arc sine approximation</a>, with a basic change to go from arc sine
//     * to arc cosine. This method is faster than {@link Math#acos(double)}. Fast but imprecise.
//     * @param a an input to the inverse cosine function, from -1 to 1 inclusive (error is higher approaching -1 or 1)
//     * @return an output from the inverse cosine function, from 0 to PI inclusive.
//     */
//    public static double acos(double a) {
//        return 1.5707963267948966 - (a * (1.0 + (a *= a) * (-0.141514171442891431 + a * -0.719110791477959357))) /
//                (1.0 + a * (-0.439110389941411144 + a * -0.471306172023844527));
//    }

    /**
     * Arc sine approximation with very low error, based on a simplified version of {@link #atan2(float, float)}.
     * This method is usually much faster than {@link Math#asin(double)}, but is somewhat less precise than Math's
     * implementation). It is currently more precise than libGDX's approximation in their MathUtils, but this isn't
     * quite as fast; the difference in precision is hard to spot but can be noticeable in some usage.
     * @param n an input to the inverse sine function, from -1 to 1 inclusive
     * @return an output from the inverse sine function, from PI/-2.0 to PI/2.0 inclusive.
     */
    public static double asin(final double n)
    {
        final double ax = Math.sqrt(1.0 - n * n), ay = Math.abs(n);
        if(ax < ay)
        {
            final double a = ax / ay, s = a * a,
                    r = 1.57079637 - (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a);
            return (n < 0.0) ? -r : r;
        }
        else {
            final double a = ay / ax, s = a * a,
                    r = (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a);
            return (n < 0.0) ? -r : r;
        }
    }
    /**
     * Arc cosine approximation with very low error, based on a simplified version of {@link #atan2(double, double)}.
     * This method is usually much faster than {@link Math#acos(double)}, but is somewhat less precise than Math's
     * implementation). It is currently more precise than libGDX's approximation in their MathUtils, but this isn't
     * quite as fast; the difference in precision is hard to spot but can be noticeable in some usage.
     * @param n an input to the inverse cosine function, from -1 to 1 inclusive
     * @return an output from the inverse cosine function, from 0 to PI inclusive.
     */
    public static double acos(final double n)
    {
        final double ax = Math.abs(n), ay = Math.sqrt(1.0 - n * n);
        if(ax < ay)
        {
            final double a = ax / ay, s = a * a,
                    r = 1.57079637 - (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a);
            return (n < 0.0) ? Math.PI - r : r;
        }
        else {
            final double a = ay / ax, s = a * a,
                    r = (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a);
            return (n < 0.0) ? Math.PI - r : r;
        }
    }
//
//    /**
//     * Arc cosine approximation with fairly low error while still being faster than {@link NumberTools#cos(float)}.
//     * This formula is number 201 in <a href=">http://www.fastcode.dk/fastcodeproject/articles/index.htm">Dennis
//     * Kjaer Christensen's unfinished math work on arc sine approximation</a>, with a basic change to go from arc sine
//     * to arc cosine. This method is faster than {@link Math#acos(double)}, and takes and returns a float. Fast but
//     * imprecise.
//     * @param a an input to the inverse cosine function, from -1 to 1 inclusive (error is higher approaching -1 or 1)
//     * @return an output from the inverse cosine function, from 0 to PI inclusive.
//     */
//    public static float acos(float a) {
//        return 1.5707963267948966f - (a * (1f + (a *= a) * (-0.141514171442891431f + a * -0.719110791477959357f))) /
//                (1f + a * (-0.439110389941411144f + a * -0.471306172023844527f));
//    }
//
    /**
     * Arc cosine approximation with very low error, based on a simplified version of {@link #atan2(float, float)}.
     * This method is usually much faster than {@link Math#acos(double)}, but is somewhat less precise than Math's
     * implementation). It is currently more precise than libGDX's approximation in their MathUtils, but this isn't
     * quite as fast; the difference in precision is hard to spot but can be noticeable in some usage.
     * @param n an input to the inverse cosine function, from -1 to 1 inclusive
     * @return an output from the inverse cosine function, from 0 to PI inclusive.
     */
    public static float acos(final float n)
    {
        final float ax = Math.abs(n), ay = (float) Math.sqrt(1f - n * n);
        if(ax < ay)
        {
            final float a = ax / ay, s = a * a,
                    r = 1.57079637f - (((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a);
            return (n < 0f) ? 3.14159265358979323846f - r : r;
        }
        else {
            final float a = ay / ax, s = a * a,
                    r = (((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a);
            return (n < 0.0) ? 3.14159265358979323846f - r : r;
        }
    }

    /**
     * Inverse sine function (arcsine) but with output measured in turns instead of radians. Possible results for this
     * range from 0.75 (inclusive) to 1.0 (exclusive), and continuing past that to 0.0 (inclusive) to 0.25 (inclusive).
     * <br>
     * This method is extremely similar to the non-turn approximation.
     * @param n a double from -1.0 to 1.0 (both inclusive), usually the output of sin_() or cos_()
     * @return one of the values that would produce {@code n} if it were passed to {@link #sin_(double)}
     */
    public static double asin_(final double n)
    {
        if(n == 0.0) return 0.0;
        final double ax = Math.sqrt(1.0 - n * n), ay = Math.abs(n);
        if(ax < ay)
        {
            final double a = ax / ay, s = a * a,
                    r = 0.25 - (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a) * 0.15915494309189535;
            return (n < 0.0) ? 1.0 - r : r;
        }
        else {
            final double a = ay / ax, s = a * a,
                    r = (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a) * 0.15915494309189535;
            return (n < 0.0) ? 1.0 - r : r;
        }
    }
    /**
     * Inverse cosine function (arccos) but with output measured in turns instead of radians. Possible results for this
     * range from 0.0 (inclusive) to 0.5 (inclusive).
     * <br>
     * This method is extremely similar to the non-turn approximation.
     * @param n a double from -1.0 to 1.0 (both inclusive), usually the output of sin_() or cos_()
     * @return one of the values that would produce {@code n} if it were passed to {@link #cos_(double)}
     */
    public static double acos_(final double n)
    {
        final double ax = Math.abs(n), ay = Math.sqrt(1.0 - n * n);
        if(ax < ay)
        {
            final double a = ax / ay, s = a * a,
                    r = 0.25 - (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a) * 0.15915494309189535;
            return (n < 0.0) ? 0.5 - r : r;
        }
        else {
            final double a = ay / ax, s = a * a,
                    r = (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a) * 0.15915494309189535;
            return (n < 0.0) ? 0.5 - r : r;
        }
    }


    /**
     * Inverse sine function (arcsine) but with output measured in turns instead of radians. Possible results for this
     * range from 0.75f (inclusive) to 1.0f (exclusive), and continuing past that to 0.0f (inclusive) to 0.25f
     * (inclusive).
     * <br>
     * This method is extremely similar to the non-turn approximation.
     * @param n a float from -1.0f to 1.0f (both inclusive), usually the output of sin_() or cos_()
     * @return one of the values that would produce {@code n} if it were passed to {@link #sin_(float)}
     */
    public static float asin_(final float n)
    {
        if(n == 0.0f) return 0.0f;
        final float ax = (float) Math.sqrt(1f - n * n), ay = Math.abs(n);
        if(ax < ay)
        {
            final float a = ax / ay, s = a * a,
                    r = 0.25f - (((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a) * 0.15915494309189535f;
            return (n < 0.0f) ? 1.0f - r : r;
        }
        else {
            final float a = ay / ax, s = a * a,
                    r = (((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a) * 0.15915494309189535f;
            return (n < 0.0f) ? 1.0f - r : r;
        }
    }
    /**
     * Inverse cosine function (arccos) but with output measured in turns instead of radians. Possible results for this
     * range from 0.0f (inclusive) to 0.5f (inclusive).
     * <br>
     * This method is extremely similar to the non-turn approximation.
     * @param n a float from -1.0f to 1.0f (both inclusive), usually the output of sin_() or cos_()
     * @return one of the values that would produce {@code n} if it were passed to {@link #cos_(float)}
     */
    public static float acos_(final float n)
    {
        final float ax = Math.abs(n), ay = (float) Math.sqrt(1f - n * n);
        if(ax < ay)
        {
            final float a = ax / ay, s = a * a,
                    r = 0.25f - (((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a) * 0.15915494309189535f;
            return (n < 0.0f) ? 0.5f - r : r;
        }
        else {
            final float a = ay / ax, s = a * a,
                    r = (((-0.0464964749f * s + 0.15931422f) * s - 0.327622764f) * s * a + a) * 0.15915494309189535f;
            return (n < 0.0f) ? 0.5f - r : r;
        }
    }
}
