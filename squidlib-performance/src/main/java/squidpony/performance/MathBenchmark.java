/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package squidpony.performance;

import com.badlogic.gdx.math.MathUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import squidpony.squidmath.LinnormRNG;
import squidpony.squidmath.NumberTools;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark                               Mode  Cnt   Score   Error  Units
 * MathBenchmark.measureBaseline           avgt    5   3.517 ± 0.052  ns/op // subtract this time from other results
 * MathBenchmark.measureCosApprox          avgt    5   9.233 ± 0.052  ns/op
 * MathBenchmark.measureCosApproxDeg       avgt    5   8.992 ± 0.146  ns/op
 * MathBenchmark.measureCosApproxFloat     avgt    5   8.985 ± 0.176  ns/op
 * MathBenchmark.measureCosApproxNickBit   avgt    5  10.234 ± 0.194  ns/op
 * MathBenchmark.measureCosApproxNickBitF  avgt    5  10.531 ± 0.156  ns/op
 * MathBenchmark.measureCosApproxOld       avgt    5   9.155 ± 0.096  ns/op
 * MathBenchmark.measureCosGdx             avgt    5   5.279 ± 0.139  ns/op
 * MathBenchmark.measureCosGdxDeg          avgt    5   5.345 ± 0.201  ns/op
 * MathBenchmark.measureMathCos            avgt    5  50.765 ± 1.040  ns/op
 * MathBenchmark.measureMathCosDeg         avgt    5  53.063 ± 7.088  ns/op
 * MathBenchmark.measureMathSin            avgt    5  50.679 ± 1.024  ns/op
 * MathBenchmark.measureMathSinDeg         avgt    5  51.600 ± 1.304  ns/op
 * MathBenchmark.measureSinApprox          avgt    5   8.489 ± 0.158  ns/op
 * MathBenchmark.measureSinApproxDeg       avgt    5   8.309 ± 0.045  ns/op
 * MathBenchmark.measureSinApproxFloat     avgt    5   8.363 ± 0.261  ns/op
 * MathBenchmark.measureSinApproxNickBit   avgt    5  10.202 ± 0.231  ns/op
 * MathBenchmark.measureSinApproxNickBitF  avgt    5  10.799 ± 0.147  ns/op
 * MathBenchmark.measureSinApproxOld       avgt    5   9.213 ± 0.112  ns/op
 * MathBenchmark.measureSinGdx             avgt    5   4.974 ± 0.072  ns/op // best radians
 * MathBenchmark.measureSinGdxDeg          avgt    5   4.972 ± 0.050  ns/op // best degrees
 */

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class MathBenchmark {

    private final double[] inputs = new double[65536];
    private final float[] floatInputs = new float[65536];
    private final double[] arcInputs = new double[65536];
    {
        for (int i = 0; i < 65536; i++) {
            floatInputs[i] = (float) (inputs[i] =
                    //-2.0 + (i * 0.0625)
                    NumberTools.randomSignedDouble(i + 107) * 4096.0
            );
            arcInputs[i] = NumberTools.randomSignedDouble(i + 421L);
        }
    }
    public static double asin(double a)
    {
        return (a * (1.0 + (a *= a) * (-0.141514171442891431 + a * -0.719110791477959357)))
                / (1.0 + a * (-0.439110389941411144 + a * -0.471306172023844527));
    }

    private short mathCos = -0x8000;
    private short mathSin = -0x8000;
    private short mathASin = -0x8000;
    private short asinChristensen = -0x8000;
    private short cosOld = -0x8000;
    private short sinOld = -0x8000;
    private short sinNick = -0x8000;
    private short cosNick = -0x8000;
    private short sinBit = -0x8000;
    private short cosBit = -0x8000;
    private short sinBitF = -0x8000;
    private short cosBitF = -0x8000;
    private short cosFloat = -0x8000;
    private short sinFloat = -0x8000;
    private short cosGdx = -0x8000;
    private short sinGdx = -0x8000;
    private short mathCosDeg = -0x8000;
    private short mathSinDeg = -0x8000;
    private short sinNickDeg = -0x8000;
    private short cosNickDeg = -0x8000;
    private short cosGdxDeg = -0x8000;
    private short sinGdxDeg = -0x8000;
    private short baseline = -0x8000;
    private short mathAtan2X = -0x4000;
    private short mathAtan2Y = -0x8000;
    private short atan2ApproxX = -0x4000;
    private short atan2ApproxY = -0x8000;
    private short atan2ApproxXF = -0x4000;
    private short atan2ApproxYF = -0x8000;
    private short atan2GdxX = -0x4000;
    private short atan2GdxY = -0x8000;
    private short atan2ApproxAX = -0x4000;
    private short atan2ApproxAY = -0x8000;
    private short atan2ApproxAXF = -0x4000;
    private short atan2ApproxAYF = -0x8000;


    @Benchmark
    public double measureBaseline()
    {
        return inputs[baseline++ & 0xFFFF];
    }



    @Benchmark
    public double measureMathCos()
    {
        return Math.cos(inputs[mathCos++ & 0xFFFF]);
    }

    @Benchmark
    public double measureMathSin()
    {
        return Math.sin(inputs[mathSin++ & 0xFFFF]);
    }

    @Benchmark
    public double measureMathASin()
    {
        return Math.asin(arcInputs[mathASin++ & 0xFFFF]);
    }

    @Benchmark
    public double measureASinApprox()
    {
        return asin(arcInputs[asinChristensen++ & 0xFFFF]);
    }


    @Benchmark
    public double measureCosApproxOld() {
        return cosOld(inputs[cosOld++ & 0xFFFF]);
//        cosOld += 0.0625;
//        final long s = Double.doubleToLongBits(cosOld * 0.3183098861837907 + (cosOld < 0.0 ? -2.0 : 2.0)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
//        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L) >> 51)) & 0xfffffffffffffL) | 0x4000000000000000L) - 2.0);
//        return a * a * (3.0 - 2.0 * a) * -2.0 + 1.0;
    }

    @Benchmark
    public double measureSinApproxOld() {
        return sinOld(inputs[sinOld++ & 0xFFFF]);
//        sinOld += 0.0625;
//        final long s = Double.doubleToLongBits(sinOld * 0.3183098861837907 + (sinOld < -1.5707963267948966 ? -1.5 : 2.5)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
//        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L) >> 51)) & 0xfffffffffffffL) | 0x4000000000000000L) - 2.0);
//        return a * a * (3.0 - 2.0 * a) * 2.0 - 1.0;
    }

    private static double sinOld(final double radians)
    {
        final long s = Double.doubleToLongBits(radians * 0.3183098861837907 + (radians < -1.5707963267948966 ? -1.5 : 2.5)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L) >> 51)) & 0xfffffffffffffL) | 0x4000000000000000L) - 2.0);
        return a * a * (3.0 - 2.0 * a) * 2.0 - 1.0;
    }

    private static float sinOld(final float radians)
    {
        final int s = Float.floatToIntBits(radians * 0.3183098861837907f + (radians < -1.5707963267948966f ? -1.5f : 2.5f)), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
        final float a = (Float.intBitsToFloat(((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40000000) - 2f);
        return a * a * (3f - 2f * a) * 2f - 1f;
    }

    private static double cosOld(final double radians)
    {
        final long s = Double.doubleToLongBits(radians * 0.3183098861837907 + (radians < 0.0 ? -2.0 : 2.0)), m = (s >>> 52 & 0x7FFL) - 0x400, sm = s << m;
        final double a = (Double.longBitsToDouble(((sm ^ -((sm & 0x8000000000000L) >> 51)) & 0xfffffffffffffL) | 0x4000000000000000L) - 2.0);
        return a * a * (3.0 - 2.0 * a) * -2.0 + 1.0;
    }

    private static float cosOld(final float radians)
    {
        final int s = Float.floatToIntBits(radians * 0.3183098861837907f + (radians < 0f ? -2f : 2f)), m = (s >>> 23 & 0xFF) - 0x80, sm = s << m;
        final float a = (Float.intBitsToFloat(((sm ^ -((sm & 0x00400000)>>22)) & 0x007fffff) | 0x40000000) - 2f);
        return a * a * (3f - 2f * a) * -2f + 1f;
    }

    @Benchmark
    public float measureCosApproxFloat() {
        return NumberTools.cos(floatInputs[cosFloat++ & 0xFFFF]);
    }

    @Benchmark
    public float measureSinApproxFloat() {
        return NumberTools.sin(floatInputs[sinFloat++ & 0xFFFF]);
    }
//    private double sinNick = 1.0;
//    @Benchmark
//    public double measureSinApproxNick()
//    {
//        double a = Math.abs(sinNick += 0.0625), n = (a % 3.141592653589793);
//        n *= 1.2732395447351628 - 0.4052847345693511 * n;
//        return n * (0.775 + 0.225 * n) * Math.signum(((a + 3.141592653589793) % 6.283185307179586) - 3.141592653589793) * Math.signum(sinNick);
//    }


    /**
     * Sine approximation code from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick".
     * @return a close approximation of the sine of an internal variable this changes by 0.0625 each time
     */
    @Benchmark
    public double measureSinApprox()
    {
        return NumberTools.sin(inputs[sinNick++ & 0xFFFF]);
    }

    @Benchmark
    public double measureCosApprox()
    {
        return  NumberTools.cos(inputs[cosNick++ & 0xFFFF]);
    }

    @Benchmark
    public double measureSinApproxNickBit()
    {
        return sinBit(inputs[sinBit++ & 0xFFFF]);
    }

    @Benchmark
    public double measureCosApproxNickBit()
    {
        return cosBit(inputs[cosBit++ & 0xFFFF]);
    }

    @Benchmark
    public float measureSinApproxNickBitF()
    {
        return sinBit(floatInputs[sinBitF++ & 0xFFFF]);
    }

    @Benchmark
    public float measureCosApproxNickBitF()
    {
        return cosBit(floatInputs[cosBitF++ & 0xFFFF]);
    }
    /**
     * A fairly-close approximation of {@link Math#sin(double)} that can be significantly faster (between 4x and 40x
     * faster sin() calls in benchmarking, depending on whether HotSpot deoptimizes Math.sin() for its own inscrutable
     * reasons), and both takes and returns doubles. Takes the same arguments Math.sin() does, so one angle in radians,
     * which may technically be any double (but this will lose precision on fairly large doubles, such as those that
     * are larger than about 65536.0). This is closely related to {@link NumberTools#sway(float)}, but the shape of the output when
     * graphed is almost identical to sin().  The difference between the result of this method and
     * {@link Math#sin(double)} should be under 0.001 at all points between -pi and pi, with an average difference of
     * about 0.0005; not all points have been checked for potentially higher errors, though. Coercion between float and
     * double takes about as long as this method normally takes to run, so if you have floats you should usually use
     * methods that take floats (or return floats, if assigning the result to a float), and likewise for doubles.
     * <br>
     * If you call this frequently, consider giving it either all positive numbers, i.e. 0 to PI * 2 instead of -PI to
     * PI; this can help the performance of this particular approximation by making its one branch easier to predict.
     * <br>
     * The technique for sine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any double to the valid input range,
     * using code extremely similar to {@link NumberTools#zigzag(double)}.
     * @param radians an angle in radians as a double, often from 0 to pi * 2, though not required to be.
     * @return the sine of the given angle, as a double between -1.0 and 1.0 (probably exclusive on -1.0, but not 1.0)
     */
    public static double sinBit(final double radians)
    {
        long sign, s;
        if(radians < 0.0) {
            s = Double.doubleToLongBits(radians * 0.3183098861837907 - 2.0);
            sign = 1L;
        }
        else {
            s = Double.doubleToLongBits(radians * 0.3183098861837907 + 2.0);
            sign = -1L;
        }
        final long m = (s >>> 52 & 0x7FFL) - 0x400L, sm = s << m, sn = -((sm & 0x8000000000000L) >> 51);
        double n = (Double.longBitsToDouble(((sm ^ sn) & 0xfffffffffffffL) | 0x4010000000000000L) - 4.0);
        n *= 2.0 - n;
        return n * (-0.775 - 0.225 * n) * ((sn ^ sign) | 1L);
    }

    /**
     * A fairly-close approximation of {@link Math#sin(double)} that can be significantly faster (between 4x and 40x
     * faster sin() calls in benchmarking, depending on whether HotSpot deoptimizes Math.sin() for its own inscrutable
     * reasons), and both takes and returns floats. Takes the same arguments Math.sin() does, so one angle in radians,
     * which may technically be any float (but this will lose precision on fairly large floats, such as those that are
     * larger than about 4096f). This is closely related to {@link NumberTools#sway(float)}, but the shape of the output when
     * graphed is almost identical to sin(). The difference between the result of this method and
     * {@link Math#sin(double)} should be under 0.001 at all points between -pi and pi, with an average difference of
     * about 0.0005; not all points have been checked for potentially higher errors, though. The error for this float
     * version is extremely close to the double version, {@link NumberTools#sin(double)}, so you should choose based on what type
     * you have as input and/or want to return rather than on quality concerns. Coercion between float and double takes
     * about as long as this method normally takes to run, so if you have floats you should usually use methods that
     * take floats (or return floats, if assigning the result to a float), and likewise for doubles.
     * <br>
     * If you call this frequently, consider giving it either all positive numbers, i.e. 0 to PI * 2 instead of -PI to
     * PI; this can help the performance of this particular approximation by making its one branch easier to predict.
     * <br>
     * The technique for sine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any double to the valid input range,
     * using code extremely similar to {@link NumberTools#zigzag(float)}.
     * @param radians an angle in radians as a float, often from 0 to pi * 2, though not required to be.
     * @return the sine of the given angle, as a float between -1f and 1f (probably exclusive on -1f, but not 1f)
     */
    public static float sinBit(final float radians)
    {
        int sign, s;
        if(radians < 0.0f) {
            s = Float.floatToIntBits(radians * 0.3183098861837907f - 2f);
            sign = 1;
        }
        else {
            s = Float.floatToIntBits(radians * 0.3183098861837907f + 2f);
            sign = -1;
        }
        final int m = (s >>> 23 & 0xFF) - 0x80, sm = s << m, sn = -((sm & 0x00400000) >> 22);
        float n = (Float.intBitsToFloat(((sm ^ sn) & 0x007fffff) | 0x40800000) - 4f);
        n *= 2f - n;
        return n * (-0.775f - 0.225f * n) * ((sn ^ sign) | 1);
    }

    /**
     * A fairly-close approximation of {@link Math#cos(double)} that can be significantly faster (between 4x and 40x
     * faster cos() calls in benchmarking, depending on whether HotSpot deoptimizes Math.cos() for its own inscrutable
     * reasons), and both takes and returns doubles. Takes the same arguments Math.cos() does, so one angle in radians,
     * which may technically be any double (but this will lose precision on fairly large doubles, such as those that
     * are larger than about 65536.0). This is closely related to {@link NumberTools#sway(float)}, but the shape of the output when
     * graphed is almost identical to cos(). The difference between the result of this method and
     * {@link Math#cos(double)} should be under 0.001 at all points between -pi and pi, with an average difference of
     * about 0.0005; not all points have been checked for potentially higher errors, though.Coercion between float and
     * double takes about as long as this method normally takes to run, so if you have floats you should usually use
     * methods that take floats (or return floats, if assigning the result to a float), and likewise for doubles.
     * <br>
     * If you call this frequently, consider giving it either all positive numbers, i.e. 0 to PI * 2 instead of -PI to
     * PI; this can help the performance of this particular approximation by making its one branch easier to predict.
     * <br>
     * The technique for cosine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any double to the valid input range,
     * using code extremely similar to {@link NumberTools#zigzag(double)}.
     * @param radians an angle in radians as a double, often from 0 to pi * 2, though not required to be.
     * @return the cosine of the given angle, as a double between -1.0 and 1.0 (probably exclusive on 1.0, but not -1.0)
     */
    public static double cosBit(final double radians)
    {
        long sign, s;
        if(radians < -1.5707963267948966) {
            s = Double.doubleToLongBits(radians * 0.3183098861837907 - 1.5);
            sign = 1L;
        }
        else {
            s = Double.doubleToLongBits(radians * 0.3183098861837907 + 2.5);
            sign = -1L;
        }
        final long m = (s >>> 52 & 0x7FFL) - 0x400L, sm = s << m, sn = -((sm & 0x8000000000000L) >> 51);
        double n = (Double.longBitsToDouble(((sm ^ sn) & 0xfffffffffffffL) | 0x4010000000000000L) - 4.0);
        n *= 2.0 - n;
        return n * (-0.775 - 0.225 * n) * ((sn ^ sign) | 1L);
    }

    /**
     * A fairly-close approximation of {@link Math#cos(double)} that can be significantly faster (between 4x and 40x
     * faster cos() calls in benchmarking, depending on whether HotSpot deoptimizes Math.cos() for its own inscrutable
     * reasons), and both takes and returns floats. Takes the same arguments Math.cos() does, so one angle in radians,
     * which may technically be any float (but this will lose precision on fairly large floats, such as those that are
     * larger than about 4096f). This is closely related to {@link NumberTools#sway(float)}, but the shape of the output when
     * graphed is almost identical to cos(). The difference between the result of this method and
     * {@link Math#cos(double)} should be under 0.001 at all points between -pi and pi, with an average difference of
     * about 0.0005; not all points have been checked for potentially higher errors, though. The error for this float
     * version is extremely close to the double version, {@link NumberTools#cos(double)}, so you should choose based on what type
     * you have as input and/or want to return rather than on quality concerns. Coercion between float and double takes
     * about as long as this method normally takes to run, so if you have floats you should usually use methods that
     * take floats (or return floats, if assigning the result to a float), and likewise for doubles.
     * <br>
     * If you call this frequently, consider giving it either all positive numbers, i.e. 0 to PI * 2 instead of -PI to
     * PI; this can help the performance of this particular approximation by making its one branch easier to predict.
     * <br>
     * The technique for cosine approximation is mostly from
     * <a href="https://web.archive.org/web/20080228213915/http://devmaster.net/forums/showthread.php?t=5784">this archived DevMaster thread</a>,
     * with credit to "Nick". Changes have been made to accelerate wrapping from any double to the valid input range,
     * using code extremely similar to {@link NumberTools#zigzag(float)}.
     * @param radians an angle in radians as a float, often from 0 to pi * 2, though not required to be.
     * @return the cosine of the given angle, as a float between -1f and 1f (probably exclusive on 1f, but not -1f)
     */
    public static float cosBit(final float radians)
    {
        int sign, s;
        if(radians < -1.5707963267948966f) {
            s = Float.floatToIntBits(radians * 0.3183098861837907f - 1.5f);
            sign = 1;
        }
        else {
            s = Float.floatToIntBits(radians * 0.3183098861837907f + 2.5f);
            sign = -1;
        }
        final int m = (s >>> 23 & 0xFF) - 0x80, sm = s << m, sn = -((sm & 0x00400000) >> 22);
        float n = (Float.intBitsToFloat(((sm ^ sn) & 0x007fffff) | 0x40800000) - 4f);
        n *= 2f - n;
        return n * (-0.775f - 0.225f * n) * ((sn ^ sign) | 1);
    }
    @Benchmark
    public float measureSinGdx()
    {
        return MathUtils.sin(floatInputs[sinGdx++ & 0xFFFF]);
    }

    @Benchmark
    public float measureCosGdx() {
        return MathUtils.cos(floatInputs[cosGdx++ & 0xFFFF]);
    }

    @Benchmark
    public double measureMathCosDeg()
    {
        return Math.cos(inputs[mathCosDeg++ & 0xFFFF] * 0.017453292519943295);
    }

    @Benchmark
    public double measureMathSinDeg()
    {
        return Math.sin(inputs[mathSinDeg++ & 0xFFFF] * 0.017453292519943295);
    }

    @Benchmark
    public float measureCosApproxDeg() {
        return NumberTools.cosDegrees(floatInputs[cosNickDeg++ & 0xFFFF]);
    }

    @Benchmark
    public float measureSinApproxDeg() {
        return NumberTools.sinDegrees(floatInputs[sinNickDeg++ & 0xFFFF]);
    }

    @Benchmark
    public float measureSinGdxDeg()
    {
        return MathUtils.sinDeg(floatInputs[sinGdxDeg++ & 0xFFFF]);
    }

    @Benchmark
    public float measureCosGdxDeg() {
        return MathUtils.cosDeg(floatInputs[cosGdxDeg++ & 0xFFFF]);
    }
    @Benchmark
    public double measureMathAtan2()
    {
        return Math.atan2(inputs[mathAtan2Y++ & 0xFFFF], inputs[mathAtan2X++ & 0xFFFF]);
    }

    @Benchmark
    public double measureApproxAtan2()
    {
        return NumberTools.atan2(inputs[atan2ApproxY++ & 0xFFFF], inputs[atan2ApproxX++ & 0xFFFF]);
    }

    @Benchmark
    public float measureApproxAtan2Float()
    {
        return NumberTools.atan2(floatInputs[atan2ApproxYF++ & 0xFFFF], floatInputs[atan2ApproxXF++ & 0xFFFF]);
    }

    @Benchmark
    public float measureGdxAtan2()
    {
        return MathUtils.atan2(floatInputs[atan2GdxY++ & 0xFFFF], floatInputs[atan2GdxX++ & 0xFFFF]);
    }
    /**
     * Rather rough approximation of the frequently-used trigonometric method atan2, meant for speed rather than high
     * precision. Maximum error is below 0.07 radians, though most angles apparently have a much lower average error.
     * Takes y and x (in that unusual order) as doubles, and returns the angle from the origin to that point in radians.
     * It is between 10 and 20 times faster than {@link Math#atan2(double, double)} (roughly 3-4 ns instead of roughly
     * 77 ns for Math). Somewhat surprisingly, it is also 3 to 4 times faster than LibGDX' MathUtils approximation of
     * the same method (this is true for both the double and float overloads); MathUtils has significantly lower maximum
     * and average error, though. Credit to Jim Shima, who posted this to Usenet in 1999 and placed it in the public
     * domain: <a href="http://dspguru.com/dsp/tricks/fixed-point-atan2-with-self-normalization/">archive here</a>.
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in radians as a double
     */
    public static double atan2_alt(double y, double x) {
        if(y == 0.0)
        {
            return x < 0 ? 3.141592653589793 : 0.0;
        }
        else if(y < 0.0)
        {
            return (x >= 0.0)
                    ? 0.7853981633974483 * ((x + y) / (x - y)) - 0.7853981633974483
                    : 0.7853981633974483 * ((x - y) / (-y - x)) - 2.3561944901923453;
        }
        else
        {
            return (x >= 0.0)
                    ? 0.7853981633974483 - 0.7853981633974483 * ((x - y) / (x + y))
                    : 2.3561944901923453 - 0.7853981633974483 * ((x + y) / (y - x));
        }
    }

    /**
     * Rather rough approximation of the frequently-used trigonometric method atan2, meant for speed rather than high
     * precision. Maximum error is below 0.07 radians, though most angles apparently have a much lower average error.
     * Takes y and x (in that unusual order) as floats, and returns the angle from the origin to that point in radians.
     * It is between 10 and 20 times faster than {@link Math#atan2(double, double)} (roughly 3-4 ns instead of roughly
     * 77 ns for Math), even ignoring the double to float to double conversions needed to use float parameters and get a
     * float returned. Somewhat surprisingly, it is also 3 to 4 times faster than LibGDX' MathUtils approximation of the
     * same method (this is true for both the double and float overloads); MathUtils has significantly lower maximum and
     * average error, though. Credit to Jim Shima, who posted this to Usenet in 1999 and placed it in the public domain:
     * <a href="http://dspguru.com/dsp/tricks/fixed-point-atan2-with-self-normalization/">archive here</a>.
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, in radians as a float
     */
    public static float atan2_alt(float y, float x) {
        if(y == 0f)
        {
            return x < 0f ? 3.141592653589793f : 0.0f;
        }
        else if(y < 0.0f)
        {
            return (x >= 0.0f)
                    ? 0.7853981633974483f * ((x + y) / (x - y)) - 0.7853981633974483f
                    : 0.7853981633974483f * ((x - y) / (-y - x)) - 2.3561944901923453f;
        }
        else
        {
            return (x >= 0.0f)
                    ? 0.7853981633974483f - 0.7853981633974483f * ((x - y) / (x + y))
                    : 2.3561944901923453f - 0.7853981633974483f * ((x + y) / (y - x));
        }
    }

    @Benchmark
    public double measureApproxAtan2Alt()
    {
        return NumberTools.atan2Rough(inputs[atan2ApproxAY++ & 0xFFFF], inputs[atan2ApproxAX++ & 0xFFFF]);
    }

    @Benchmark
    public float measureApproxAtan2AltFloat()
    {
        return NumberTools.atan2Rough(floatInputs[atan2ApproxAYF++ & 0xFFFF], floatInputs[atan2ApproxAXF++ & 0xFFFF]);
    }

    @Benchmark
    public double measureAtan2Baseline()
    {
        return inputs[atan2ApproxAY++ & 0xFFFF] + inputs[atan2ApproxAX++ & 0xFFFF];
    }
    @Benchmark
    public float measureAtan2BaselineFloat()
    {
        return floatInputs[atan2ApproxAYF++ & 0xFFFF] + floatInputs[atan2ApproxAXF++ & 0xFFFF];
    }

    /*
mvn clean install
java -jar target/benchmarks.jar UncommonBenchmark -wi 5 -i 5 -f 1 -gc true
     */
    public static void main2(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MathBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(60))
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
    public static void main(String[] args)
    {
        MathBenchmark u = new MathBenchmark();
        double cosOldError = 0.0, cosFError = 0.0, cosNickError = 0.0, cosBitError = 0.0, cosBitFError = 0.0, cosGdxError = 0.0,
                sinOldError = 0.0, sinFError = 0.0,  sinNickError = 0.0, sinBitError = 0.0, sinBitFError = 0.0, sinGdxError = 0.0,
                precisionError = 0.0, cosDegNickError = 0.0, sinDegNickError = 0.0, cosDegGdxError = 0.0, sinDegGdxError = 0.0,
                asinChristensenError = 0.0;
        ;
        System.out.println("Math.sin()       : " + u.measureMathSin());
        System.out.println("Math.sin()       : " + u.measureMathSin());
        System.out.println("Math.cos()       : " + u.measureMathCos());
        System.out.println("double sin approx: " + u.measureSinApprox());
        System.out.println("double cos approx: " + u.measureCosApprox());
//        System.out.println("float approx     : " + u.measureCosApproxFloat());
//        System.out.println("Climatiano       : " + u.measureCosApproxClimatiano());
//        System.out.println("ClimatianoLP     : " + u.measureCosApproxClimatianoLP());
        for (int r = 0; r < 0x1000; r++) {
            //margin += 0.0001;
            short i = (short) (LinnormRNG.determine(r) & 0xFFFF);
            u.mathCos = i;
            u.mathSin = i;
            u.cosOld = i;
            u.sinOld = i;
            u.sinNick = i;
            u.cosNick = i;
            u.sinBit = i;
            u.cosBit = i;
            u.sinBitF = i;
            u.cosBitF = i;
            u.cosFloat = i;
            u.sinFloat = i;
            u.cosGdx = i;
            u.sinGdx = i;
            u.mathCosDeg = i;
            u.mathSinDeg = i;
            u.cosNickDeg = i;
            u.sinNickDeg = i;
            u.cosGdxDeg = i;
            u.sinGdxDeg = i;
            double c = u.measureMathCos(), s = u.measureMathSin(), cd = u.measureMathCosDeg(), sd = u.measureMathSinDeg(), as = u.measureMathASin();
            precisionError += Math.abs(c - (float)c);
            cosOldError += Math.abs(u.measureCosApproxOld() - c);
            cosFError += Math.abs(u.measureCosApproxFloat() - c);
            cosGdxError += Math.abs(u.measureCosGdx() - c);
            sinGdxError += Math.abs(u.measureSinGdx() - s);
            cosNickError += Math.abs(u.measureCosApprox() - c);
            sinOldError += Math.abs(u.measureSinApproxOld() - s);
            sinNickError += Math.abs(u.measureSinApprox() - s);
            sinFError += Math.abs(u.measureSinApproxFloat() - s);
            cosBitError += Math.abs(u.measureCosApproxNickBit() - c);
            sinBitError += Math.abs(u.measureSinApproxNickBit() - s);
            cosBitFError += Math.abs(u.measureCosApproxNickBitF() - c);
            sinBitFError += Math.abs(u.measureSinApproxNickBitF() - s);
            cosDegNickError += Math.abs(u.measureCosApproxDeg() - cd);
            sinDegNickError += Math.abs(u.measureSinApproxDeg() - sd);
            cosDegGdxError += Math.abs(u.measureCosGdxDeg() - cd);
            sinDegGdxError += Math.abs(u.measureSinGdxDeg() - sd);
            asinChristensenError += Math.abs(u.measureASinApprox() - as);
        }
        //System.out.println("Margin allowed   : " + margin);
        System.out.println("double approx    : " + cosOldError);
        System.out.println("base float error : " + precisionError);
        System.out.println("float approx     : " + cosFError);
        System.out.println("cos GDX          : " + cosGdxError);
        System.out.println("sin GDX          : " + sinGdxError);
        System.out.println("sin approx       : " + sinOldError);
        System.out.println("sin approx float : " + sinFError);
        System.out.println("sin Nick approx  : " + sinNickError);
        System.out.println("cos Nick approx  : " + cosNickError);
        System.out.println("sin Bit approx   : " + sinBitError);
        System.out.println("cos Bit approx   : " + cosBitError);
        System.out.println("sin BitF approx  : " + sinBitFError);
        System.out.println("cos BitF approx  : " + cosBitFError);
        System.out.println("sin Nick deg     : " + sinDegNickError);
        System.out.println("cos Nick deg     : " + cosDegNickError);
        System.out.println("sin GDX deg      : " + sinDegGdxError);
        System.out.println("cos GDX deg      : " + cosDegGdxError);
        System.out.println("asin approx      : " + asinChristensenError);
        double atan2ApproxError = 0, atan2GDXError = 0, atan2AltError = 0, at;
        for(int r = 0; r < 0x10000; r++)
        {
            short i = (short) (LinnormRNG.determine(r) & 0xFFFF);
            short j = (short) (LinnormRNG.determine(-0x20000 - r - i) & 0xFFFF);
            u.mathAtan2X = i;
            u.mathAtan2Y = j;
            u.atan2ApproxX = i;
            u.atan2ApproxY = j;
            u.atan2ApproxAX = i;
            u.atan2ApproxAY = j;
            u.atan2GdxX = i;
            u.atan2GdxY = j;
            at = u.measureMathAtan2();
            atan2ApproxError += Math.abs(u.measureApproxAtan2() - at);
            atan2AltError += Math.abs(u.measureApproxAtan2Alt() - at);
            atan2GDXError += Math.abs(u.measureGdxAtan2() - at);
        }
        System.out.println("atan2 approx     : " + atan2ApproxError);
        System.out.println("atan2 alt approx : " + atan2AltError);
        System.out.println("atan2 GDX approx : " + atan2GDXError);
    }
}