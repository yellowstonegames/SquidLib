// ============================================================================
//   Copyright 2006-2012 Daniel W. Dyer
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ============================================================================
package squidpony.squidmath;

import squidpony.annotation.GwtIncompatible;

import java.math.BigInteger;
import java.util.Random;

/**
 * Mathematical operations not provided by {@link Math java.lang.Math}.
 * <br>
 * Originally part of the <a href="http://maths.uncommons.org/">Uncommon Maths software package</a> as Maths.
 * @author Daniel Dyer
 */
public final class MathExtras
{
    // The biggest factorial that can be calculated using 64-bit signed longs.
    private static final int MAX_LONG_FACTORIAL = 20;

    // Cache BigInteger factorial values because they are expensive to generate.
    private static final int CACHE_SIZE = 256;
    static final long[] factorialsStart = new long[]{
            0, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800, 39916800, 479001600,
            6227020800L, 87178291200L, 1307674368000L, 20922789888000L, 355687428096000L,
            6402373705728000L, 121645100408832000L, 2432902008176640000L
    };
    private static final OrderedMap<Integer, BigInteger> BIG_FACTORIALS
        = new OrderedMap<Integer, BigInteger>();


    private MathExtras()
    {
        // Prevent instantiation.
    }


    /**
     * Calculates the factorial of n where n is a number in the
     * range 0 - 20.  Zero factorial is equal to 1.  For values of
     * n greater than 20 you must use {@link #bigFactorial(int)}.
     * @param n The factorial to calculate.
     * @return The factorial of n.
     * @see #bigFactorial(int)
     */
    public static long factorial(int n)
    {
        if (n < 0 || n > MAX_LONG_FACTORIAL)
        {
            throw new IllegalArgumentException("Argument must be in the range 0 - 20.");
        }
        /*
        long factorial = 1;
        for (int i = n; i > 1; i--)
        {
            factorial *= i;
        }*/
        return factorialsStart[n];
    }


    /**
     * Calculates the factorial of n where n is a positive integer.
     * Zero factorial is equal to 1.  For values of n up to 20, consider
     * using {@link #factorial(int)} instead since it uses a faster
     * implementation.
     * @param n The factorial to calculate.
     * @return The factorial of n.
     * @see #factorial(int)
     */
    public static BigInteger bigFactorial(int n)
    {
        if (n < 0)
        {
            throw new IllegalArgumentException("Argument must greater than or equal to zero.");
        }

        BigInteger factorial = null;
        if (n < CACHE_SIZE) // Check for a cached value.
        {
            factorial = BIG_FACTORIALS.get(n);
        }

        if (factorial == null)
        {
            factorial = BigInteger.ONE;
            for (int i = n; i > 1; i--)
            {
                factorial = factorial.multiply(BigInteger.valueOf(i));
            }
            if (n < CACHE_SIZE) // Cache value.
            {
                if(!BIG_FACTORIALS.containsKey(n))
                    BIG_FACTORIALS.put(n, factorial);
            }
        }

        return factorial;
    }


    /**
     * Calculate the first argument raised to the power of the second.
     * This method only supports non-negative powers.
     * @param value The number to be raised.
     * @param power The exponent (must be positive).
     * @return {@code value} raised to {@code power}.
     */
    public static long raiseToPower(int value, int power)
    {
        if (power < 0)
        {
            throw new IllegalArgumentException("This method does not support negative powers.");
        }
        long result = 1;
        for (int i = 0; i < power; i++)
        {
            result *= value;
        }
        return result;
    }


    /**
     * Calculate logarithms for arbitrary bases.
     * @param base The base for the logarithm.
     * @param arg The value to calculate the logarithm for.
     * @return The log of {@code arg} in the specified {@code base}.
     */
    public static double log(double base, double arg)
    {
        // Use natural logarithms and change the base.
        return Math.log(arg) / Math.log(base);
    }


    /**
     * Checks that two values are approximately equal (plus or minus a specified absolute tolerance).
     * @param value1 The first value to compare.
     * @param value2 The second value to compare.
     * @param tolerance How much the values are allowed to differ and still be considered equal, as an absolute amount.
     * @return true if the values are approximately equal (give or take tolerance), false otherwise.
     */
    public static boolean approxEquals(double value1,
                                       double value2,
                                       double tolerance)
    {
        return Math.abs(value1 - value2) <= tolerance;
    }


    /**
     * If the specified value is not greater than or equal to the specified minimum and
     * less than or equal to the specified maximum, adjust it so that it is.
     * @param value The value to check.
     * @param min The minimum permitted value.
     * @param max The maximum permitted value.
     * @return {@code value} if it is between the specified limits, {@code min} if the value
     * is too low, or {@code max} if the value is too high.
     */
    public static int clamp(int value, int min, int max)
    {
        return Math.min(Math.max(value, min), max);
    }


    /**
     * If the specified value is not greater than or equal to the specified minimum and
     * less than or equal to the specified maximum, adjust it so that it is.
     * @param value The value to check.
     * @param min The minimum permitted value.
     * @param max The maximum permitted value.
     * @return {@code value} if it is between the specified limits, {@code min} if the value
     * is too low, or {@code max} if the value is too high.
     */
    public static long clamp(long value, long min, long max)
    {
        return Math.min(Math.max(value, min), max);
    }


    /**
     * If the specified value is not greater than or equal to the specified minimum and
     * less than or equal to the specified maximum, adjust it so that it is.
     * @param value The value to check.
     * @param min The minimum permitted value.
     * @param max The maximum permitted value.
     * @return {@code value} if it is between the specified limits, {@code min} if the value
     * is too low, or {@code max} if the value is too high.
     */
    public static double clamp(double value, double min, double max)
    {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * If the specified value is not greater than or equal to the specified minimum and
     * less than or equal to the specified maximum, adjust it so that it is.
     * @param value The value to check.
     * @param min The minimum permitted value.
     * @param max The maximum permitted value.
     * @return {@code value} if it is between the specified limits, {@code min} if the value
     * is too low, or {@code max} if the value is too high.
     */
    public static float clamp(float value, float min, float max)
    {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Like the modulo operator {@code %}, but the result will always match the sign of {@code d} instead of {@code op}.
     * @param op the dividend; negative values are permitted and wrap instead of producing negative results
     * @param d the divisor; if this is negative then the result will be negative, otherwise it will be positive
     * @return the remainder of the division of op by d, with a sign matching d
     */
    public static double remainder(final double op, final double d) {
        return (op % d + d) % d;
    }

    /**
     * Determines the greatest common divisor of a pair of natural numbers
     * using the Euclidean algorithm.  This method only works with natural
     * numbers.  If negative integers are passed in, the absolute values will
     * be used.  The return value is always positive.
     * @param a The first value.
     * @param b The second value.
     * @return The greatest common divisor.
     */
    public static long greatestCommonDivisor(long a, long b)
    {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0)
        {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * Given any odd int {@code a}, this finds another odd int {@code b} such that {@code a * b == 1}.
     * <br>
     * This is incompatible with GWT, but it should usually only find uses in exploratory code or in tests anyway...
     * It is only incompatible because it tends to rely on multiplication overflow to work.
     * @param a any odd int; note that even numbers do not have inverses modulo 2 to the 32
     * @return the multiplicative inverse of {@code a} modulo 4294967296 (or, 2 to the 32)
     */
    @GwtIncompatible
    public static int modularMultiplicativeInverse(int a)
    {
        int x = 2 ^ a * 3;     //  5 bits
        x *= 2 - a * x;        // 10
        x *= 2 - a * x;        // 20
        x *= 2 - a * x;        // 40 -- 32 low bits
        return x;
    }

    /**
     * Given any odd long {@code a}, this finds another odd long {@code b} such that {@code a * b == 1L}.
     * @param a any odd long; note that even numbers do not have inverses modulo 2 to the 64
     * @return the multiplicative inverse of {@code a} modulo 18446744073709551616 (or, 2 to the 64)
     */
    public static long modularMultiplicativeInverse(long a)
    {
        long x = 2 ^ a * 3;    //  5 bits
        x *= 2 - a * x;        // 10
        x *= 2 - a * x;        // 20
        x *= 2 - a * x;        // 40
        x *= 2 - a * x;        // 80 -- 64 low bits
        return x;
    }

    /**
     * A way of taking a double in the (0.0, 1.0) range and mapping it to a Gaussian or normal distribution, so high
     * inputs correspond to high outputs, and similarly for the low range. This is centered on 0.0 and its standard
     * deviation seems to be 1.0 (the same as {@link Random#nextGaussian()}). If this is given an input of 0.0
     * or less, it returns -38.5, which is slightly less than the result when given {@link Double#MIN_VALUE}. If it is
     * given an input of 1.0 or more, it returns 38.5, which is significantly larger than the result when given the
     * largest double less than 1.0 (this value is further from 1.0 than {@link Double#MIN_VALUE} is from 0.0). If
     * given {@link Double#NaN}, it returns whatever {@link Math#copySign(double, double)} returns for the arguments
     * {@code 38.5, Double.NaN}, which is implementation-dependent. It uses an algorithm by Peter John Acklam, as
     * implemented by Sherali Karimov.
     * <a href="https://web.archive.org/web/20150910002142/http://home.online.no/~pjacklam/notes/invnorm/impl/karimov/StatUtil.java">Original source</a>.
     * <a href="https://web.archive.org/web/20151030215612/http://home.online.no/~pjacklam/notes/invnorm/">Information on the algorithm</a>.
     * <a href="https://en.wikipedia.org/wiki/Probit_function">Wikipedia's page on the probit function</a> may help, but
     * is more likely to just be confusing.
     * <br>
     * Acklam's algorithm and Karimov's implementation are both quite fast. This appears faster than generating
     * Gaussian-distributed numbers using either the Box-Muller Transform or Marsaglia's Polar Method, though it isn't
     * as precise and can't produce as extreme min and max results in the extreme cases they should appear. If given
     * a typical uniform random {@code double} that's exclusive on 1.0, it won't produce a result higher than
     * {@code 8.209536145151493}, and will only produce results of at least {@code -8.209536145151493} if 0.0 is
     * excluded from the inputs (if 0.0 is an input, the result is {@code 38.5}). A chief advantage of using this with
     * a random number generator is that it only requires one random double to obtain one Gaussian value;
     * {@link Random#nextGaussian()} generates at least two random doubles for each two Gaussian values, but may rarely
     * require much more random generation.
     * <br>
     * This can be used both as an optimization for generating Gaussian random values, and as a way of generating
     * Gaussian values that match a pattern present in the inputs (which you could have by using a sub-random sequence
     * as the input, such as those produced by {@link VanDerCorputQRNG#determine(int, int)} or the R2 sequence).
     * @param d should be between 0 and 1, exclusive, but other values are tolerated
     * @return a normal-distributed double centered on 0.0; all results will be between -38.5 and 38.5, both inclusive
     */
    public static double probit(final double d) {
        if (d <= 0 || d >= 1) {
            return Math.copySign(38.5, d - 0.5);
        }
        /* Rational approximation for lower region: */
        else if (d < 0.02425) {
            final double q = Math.sqrt(-2.0 * Math.log(d));
            return (((((-7.784894002430293e-03 * q + -3.223964580411365e-01) * q + -2.400758277161838e+00) * q + -2.549732539343734e+00) * q + 4.374664141464968e+00) * q + 2.938163982698783e+00)
                    / ((((7.784695709041462e-03 * q + 3.224671290700398e-01) * q + 2.445134137142996e+00) * q + 3.754408661907416e+00) * q + 1.0);
        }
        /* Rational approximation for upper region: */
        else if (0.97575 < d) {
            final double q = Math.sqrt(-2.0 * Math.log(1 - d));
            return -(((((-7.784894002430293e-03 * q + -3.223964580411365e-01) * q + -2.400758277161838e+00) * q + -2.549732539343734e+00) * q + 4.374664141464968e+00) * q + 2.938163982698783e+00)
                    / ((((7.784695709041462e-03 * q + 3.224671290700398e-01) * q + 2.445134137142996e+00) * q + 3.754408661907416e+00) * q + 1.0);
        }
        /* Rational approximation for central region: */
        else {
            final double q = d - 0.5;
            final double r = q * q;
            return (((((-3.969683028665376e+01 * r + 2.209460984245205e+02) * r + -2.759285104469687e+02) * r + 1.383577518672690e+02) * r + -3.066479806614716e+01) * r + 2.506628277459239e+00) * q
                    / (((((-5.447609879822406e+01 * r + 1.615858368580409e+02) * r + -1.556989798598866e+02) * r + 6.680131188771972e+01) * r + -1.328068155288572e+01) * r + 1.0);
        }
    }

    /**
     * Integer square root (using floor), maintaining correct results even for very large {@code long} values. This
     * version treats negative inputs as unsigned and returns positive square roots for them (these are usually large).
     * <br>
     * This is based on <a href="https://github.com/python/cpython/pull/13244">code recently added to Python</a>, but
     * isn't identical. Notably, this doesn't branch except in the for loop, and it handles negative inputs differently.
     * @param n a {@code long} value that will be treated as if unsigned
     * @return the square root of n, rounded down to the next lower {@code long} if the result isn't already a {@code long}
     */
    public static long isqrt(final long n) {
        final int c = 63 - Long.numberOfLeadingZeros(n) >> 1;
        long a = 1, d = 0, e;
        for(int s = 31 & 32 - Integer.numberOfLeadingZeros(c); s > 0;) {
            e = d;
            d = c >>> --s;
            a = (a << d - e - 1) + (n >>> c + c - e - d + 1) / a;
        }
        return a - (n - a * a >>> 63);
    }

    /**
     * An approximation of the cube-root function for float inputs and outputs.
     * This can be about twice as fast as {@link Math#cbrt(double)}. It
     * correctly returns negative results when given negative inputs.
     * <br>
     * Has very low relative error (less than 1E-9) when inputs are uniformly
     * distributed between -512 and 512, and absolute mean error of less than
     * 1E-6 in the same scenario. Uses a bit-twiddling method similar to one
     * presented in Hacker's Delight and also used in early 3D graphics (see
     * https://en.wikipedia.org/wiki/Fast_inverse_square_root for more, but
     * this code approximates cbrt(x) and not 1/sqrt(x)). This specific code
     * was originally by Marc B. Reynolds, posted in his "Stand-alone-junk"
     * repo: https://github.com/Marc-B-Reynolds/Stand-alone-junk/blob/master/src/Posts/ballcube.c#L182-L197 .
     * @param x any finite float to find the cube root of
     * @return the cube root of x, approximated
     */
    public static float cbrt(float x) {
        int ix = NumberTools.floatToRawIntBits(x);
        final int sign = ix & 0x80000000;
        ix &= 0x7FFFFFFF;
        final float x0 = x;
        ix = (ix>>>2) + (ix>>>4);
        ix += (ix>>>4);
        ix = ix + (ix>>>8) + 0x2A5137A0 | sign;
        x  = NumberTools.intBitsToFloat(ix);
        x  = 0.33333334f*(2f * x + x0/(x*x));
        x  = 0.33333334f*(2f * x + x0/(x*x));
        return x;
    }

    /**
     * A generalization on bias and gain functions that can represent both; this version is branch-less.
     * This is based on <a href="https://arxiv.org/abs/2010.09714">this micro-paper</a> by Jon Barron, which
     * generalizes the earlier bias and gain rational functions by Schlick. The second and final page of the
     * paper has useful graphs of what the s (shape) and t (turning point) parameters do; shape should be 0
     * or greater, while turning must be between 0 and 1, inclusive. This effectively combines two different
     * curving functions so they continue into each other when x equals turning. The shape parameter will
     * cause this to imitate "smoothstep-like" splines when greater than 1 (where the values ease into their
     * starting and ending levels), or to be the inverse when less than 1 (where values start like square
     * root does, taking off very quickly, but also end like square does, landing abruptly at the ending
     * level). You should only give x values between 0 and 1, inclusive.
     * @param x progress through the spline, from 0 to 1, inclusive
     * @param shape must be greater than or equal to 0; values greater than 1 are "normal interpolations" 
     * @param turning a value between 0.0 and 1.0, inclusive, where the shape changes
     * @return a float between 0 and 1, inclusive
     */
    public static float barronSpline(final float x, final float shape, final float turning) {
        final float d = turning - x;
        final int f = NumberTools.floatToIntBits(d) >> 31, n = f | 1;
        return ((turning * n - f) * (x + f)) / (Float.MIN_VALUE - f + (x + shape * d) * n) - f;
    }

    /**
     * A generalization on bias and gain functions that can represent both; this version is branch-less.
     * This is based on <a href="https://arxiv.org/abs/2010.09714">this micro-paper</a> by Jon Barron, which
     * generalizes the earlier bias and gain rational functions by Schlick. The second and final page of the
     * paper has useful graphs of what the s (shape) and t (turning point) parameters do; shape should be 0
     * or greater, while turning must be between 0 and 1, inclusive. This effectively combines two different
     * curving functions so they continue into each other when x equals turning. The shape parameter will
     * cause this to imitate "smoothstep-like" splines when greater than 1 (where the values ease into their
     * starting and ending levels), or to be the inverse when less than 1 (where values start like square
     * root does, taking off very quickly, but also end like square does, landing abruptly at the ending
     * level). You should only give x values between 0 and 1, inclusive.
     * @param x progress through the spline, from 0 to 1, inclusive
     * @param shape must be greater than or equal to 0; values greater than 1 are "normal interpolations" 
     * @param turning a value between 0.0 and 1.0, inclusive, where the shape changes
     * @return a double between 0 and 1, inclusive
     */
    public static double barronSpline(final double x, final double shape, final double turning) {
        final double d = turning - x;
        final int f = NumberTools.doubleToHighIntBits(d) >> 31, n = f | 1;
        return ((turning * n - f) * (x + f)) / (Double.MIN_VALUE - f + (x + shape * d) * n) - f;
    }
}
