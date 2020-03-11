package squidpony.squidmath;

import squidpony.annotation.Beta;

import static squidpony.squidmath.ValueNoise.valueNoise;

/**
 * An unusual continuous noise generator that tends to produce organic-looking forms, currently supporting 2D, 3D, and
 * 4D. Produces noise values from -1.0 inclusive to 1.0 exclusive. Typically needs about a third as many octaves as the
 * Simplex option in FastNoise to produce roughly comparable quality, but it also has about a third the speed. A useful
 * property of FoamNoise is how its visual "character" doesn't change much as dimensions are added; whereas 4D simplex
 * noise tends to separate into "surflets" separated by spans of 0, and higher dimensions of simplex only have larger
 * such spans, FoamNoise seems to stay approximately as coherent in 2D, 3D, and 4D. Verifying this claim about FoamNoise
 * is not easy, but it makes sense intuitively because of how this generator works. Simplex noise in N dimensions relies
 * on a lattice of N-simplices (such as triangles in 2D or tetrahedra in 3D) and evaluates the noise at a point by
 * hashing each of the N+1 vertices, looking up a gradient vector for each vertex, and combining the gradient vectors
 * based on proximity of the evaluated point to each vertex. FoamNoise in N dimensions is not nearly as complex; it
 * relies on making N+1 averaged calls to N-dimensional value noise, each call using a rotated (and potentially skewed)
 * set of axes, with each call's result also affecting the inputs to the next call (domain warping). Value noise uses a
 * cubic lattice or its hypercube equivalent in higher dimensions, which seems to be more "stable" as dimensionality
 * increases, and the number of value noise calls increases at the same rate as simplex noise adds gradient vectors.
 * Averaging more calls causes the distribution of the noise to gradually approach Gaussian, but the effect of this
 * approach gets less pronounced past 3 calls.
 * <br>
 * It's strongly encouraged to experiment with the lacunarity parameter in {@link Noise.Layered3D} and similar classes
 * if you use one of those variants, which also probably needs adjustments to frequency. Changing lacunarity with
 * multiple octaves can be useful to edit how tightly the noise clumps together.
 * <br>
 * <a href="https://i.imgur.com/WpUz1xP.png">2D FoamNoise, one octave</a>,
 * <a href="https://i.imgur.com/39Brm9Y.png">2D FoamNoise, one octave colorized</a>,
 * <a href="https://i.imgur.com/ytKe4MW.png">2D FoamNoise, two octaves colorized</a> (note fewer peaks and valleys),
 * <a href="https://i.imgur.com/5FTjEIR.gifv">3D FoamNoise animated over time, one octave</a>,
 * <a href="https://i.imgur.com/su9naGx.gifv">3D FoamNoise animated over time, one octave colorized</a>,
 * <a href="https://i.imgur.com/nr4TROD.gifv">3D FoamNoise animated over time, two octaves colorized</a>,
 * <a href="https://i.imgur.com/r5cx6im.gifv">4D FoamNoise animated over time, one octave colorized</a>,
 * <a href="https://i.imgur.com/jEbUdun.gifv">4D FoamNoise animated over time, two octaves colorized</a>,
 * <a href="https://i.imgur.com/ktCTiIK.jpg">World map made using FoamNoise</a>.
 */
@Beta
public class FoamNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D {
    public static final FoamNoise instance = new FoamNoise();
    
    public int seed = 0xD1CEBEEF;
    public FoamNoise() {
    }

    public FoamNoise(int seed) {
        this.seed = seed;
    }

    public FoamNoise(long seed) {
        this.seed = (int) (seed ^ seed >>> 32);
    }

    public static double foamNoise(final double x, final double y, int seed) {
        final double p0 = x;
        final double p1 = x * -0.5 + y * 0.8660254037844386;
        final double p2 = x * -0.5 + y * -0.8660254037844387;

        double xin = p1;
        double yin = p2;
        //double xin = x * 0.540302 + y * 0.841471; // sin and cos of 1
        //double yin = x * -0.841471 + y * 0.540302;
        final double a = valueNoise(seed, xin, yin);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p2;
        yin = p0;
        //xin = x * -0.989992 + y * 0.141120; // sin and cos of 3
        //yin = x * -0.141120 + y * -0.989992;
        final double b = valueNoise(seed, xin + a, yin);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        //xin = x * 0.283662 + y * -0.958924; // sin and cos of 5
        //yin = x * 0.958924 + y * 0.283662;
        final double c = valueNoise(seed, xin + b, yin);
        final double result = a * 0.3125 + (b + c) * 0.34375;
//        return result * result * (6.0 - 4.0 * result) - 1.0;
        return (result <= 0.5)
                ? Math.pow(result * 2, 2.0) - 1.0
                : 1.0 - Math.pow((result - 1) * 2, 2.0);
    }
    
    /*
x * -0.185127 + y * -0.791704 + z * -0.582180;
x * -0.776796 + y * 0.628752 + z * -0.035464;
x * 0.822283 + y * 0.467437 + z * -0.324582;

x * 0.139640 + y * -0.304485 + z * 0.942226;
x * -0.776796 + y * 0.628752 + z * -0.035464;
x * 0.822283 + y * 0.467437 + z * -0.324582;

x * 0.139640 + y * -0.304485 + z * 0.942226;
x * -0.185127 + y * -0.791704 + z * -0.582180;
x * 0.822283 + y * 0.467437 + z * -0.324582;

x * 0.139640 + y * -0.304485 + z * 0.942226;
x * -0.185127 + y * -0.791704 + z * -0.582180;
x * -0.776796 + y * 0.628752 + z * -0.035464;
     */


    public static double foamNoise(final double x, final double y, final double z, int seed) {
        final double p0 = x;
        final double p1 = x * -0.3333333333333333 + y * 0.9428090415820634;
        final double p2 = x * -0.3333333333333333 + y * -0.4714045207910317 + z * 0.816496580927726;
        final double p3 = x * -0.3333333333333333 + y * -0.4714045207910317 + z * -0.816496580927726;

        //final double p0 = (x + y + z);// * 0.5;
        //final double p1 = x - (y + z);// * 0.25;
        //final double p2 = y - (x + z);// * 0.25;
        //final double p3 = z - (x + y);// * 0.25;
////rotated version of above points on a tetrahedron; the ones below are "more correct" but more complex (and slower?)       
//        final double p0 = x * 0.139640 + y * -0.304485 + z * 0.942226;
//        final double p1 = x * -0.185127 + y * -0.791704 + z * -0.582180;
//        final double p2 = x * -0.776796 + y * 0.628752 + z * -0.035464;
//        final double p3 = x * 0.822283 + y * 0.467437 + z * -0.324582;
        double xin = p1;
        double yin = p2;
        double zin = p3;
        final double a = valueNoise(seed, xin, yin, zin);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p2;
        zin = p3;
        final double b = valueNoise(seed, xin + a, yin, zin);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p3;
        final double c = valueNoise(seed, xin + b, yin, zin);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        final double d = valueNoise(seed, xin + c, yin, zin);

        final double result = (a + b + c + d) * 0.25;
//        return  (result * result * (6.0 - 4.0 * result) - 1.0);
        return (result <= 0.5)
                ? Math.pow(result * 2, 3.0) - 1.0
                : Math.pow((result - 1) * 2, 3.0) + 1.0;
    }

    public static double foamNoise(final double x, final double y, final double z, final double w, int seed) {
        final double p0 = x;
        final double p1 = x * -0.25 + y * 0.9682458365518543;
        final double p2 = x * -0.25 + y * -0.3227486121839514 + z * 0.9128709291752769;
        final double p3 = x * -0.25 + y * -0.3227486121839514 + z * -0.45643546458763834 + w * 0.7905694150420949;
        final double p4 = x * -0.25 + y * -0.3227486121839514 + z * -0.45643546458763834 + w * -0.7905694150420947;
        //final double p0 = (x + y + z + w);
        //final double p1 = x - (y + z + w);
        //final double p2 = y - (x + z + w);
        //final double p3 = z - (x + y + w);
        //final double p4 = w - (x + y + z);
////rotated version of above points on a 5-cell; the ones below are "more correct" but more complex (and slower?)       
//        final double p0 = x * 0.139640 + y * -0.304485 + z * 0.942226;
//        final double p1 = x * -0.185127 + y * -0.791704 + z * -0.582180;
//        final double p2 = x * -0.776796 + y * 0.628752 + z * -0.035464;
//        final double p3 = x * 0.822283 + y * 0.467437 + z * -0.324582;
        double xin = p1;
        double yin = p2;
        double zin = p3;
        double win = p4;
        final double a = valueNoise(seed, xin, yin, zin, win);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        final double b = valueNoise(seed, xin + a, yin, zin, win);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        final double c = valueNoise(seed, xin + b, yin, zin, win);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        final double d = valueNoise(seed, xin + c, yin, zin, win);
        seed += 0x9E3779BD;
        seed = (seed ^ seed >>> 12) * 0xDAB;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        final double e = valueNoise(seed, xin + d, yin, zin, win);

        final double result = (a + b + c + d + e) * 0.2;
        return (result <= 0.5)
                ? Math.pow(result * 2, 4.0) - 1.0
                : 1.0 - Math.pow((result - 1) * 2, 4.0);

//        return  (result * result * (6.0 - 4.0 * result) - 1.0);
    }

    @Override
    public double getNoise(double x, double y) {
        return foamNoise(x, y, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, long seed) {
        return foamNoise(x, y, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z) {
        return foamNoise(x * 0.5, y * 0.5, z * 0.5, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        return foamNoise(x * 0.5, y * 0.5, z * 0.5, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z, double w) {
        return foamNoise(x * 0.5, y * 0.5, z * 0.5, w * 0.5, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
        return foamNoise(x * 0.5, y * 0.5, z * 0.5, w * 0.5, (int) (seed ^ seed >>> 32));
    }
}
