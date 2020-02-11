package squidpony.squidmath;

import static squidpony.squidmath.ValueNoise.valueNoise;

/**
 * An unusual continuous noise generator that tends to produce organic-looking forms, currently supporting 2D and 3D.
 * Produces noise values from -1.0 inclusive to 1.0 exclusive. Typically needs about a third as many octaves as the
 * Simplex option in FastNoise to produce roughly comparable quality, but it also has about a third the speed. It's
 * strongly encouraged to experiment with the lacunarity parameter in {@link Noise.Layered3D} and similar classes if you
 * use one of those variants; the default lacunarity of 0.5 is often not high enough for good results (0.6 to 0.8 should
 * yield better layering effects).
 * <br>
 * <a href="https://i.imgur.com/WpUz1xP.png">2D FoamNoise, one octave</a>,
 * <a href="https://i.imgur.com/5FTjEIR.gifv">3D FoamNoise animated over time, one octave</a>,
 * <a href="https://i.imgur.com/ktCTiIK.jpg">World map made using FoamNoise</a>.
 */
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
        double xin = x * 0.540302 + y * 0.841471; // sin and cos of 1
        double yin = x * -0.841471 + y * 0.540302;
        final double a = valueNoise(seed, xin, yin);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = x * -0.989992 + y * 0.141120; // sin and cos of 3
        yin = x * -0.141120 + y * -0.989992;
        final double b = valueNoise(seed, xin, yin + a);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = x * 0.283662 + y * -0.958924; // sin and cos of 5
        yin = x * 0.958924 + y * 0.283662;
        final double c = valueNoise(seed, xin, yin - b);
        final double result = a * 0.3125 + (b + c) * 0.34375;
        return result * result * (6.0 - 4.0 * result) - 1.0;
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
        final double p0 = (x + y + z) * 0.5;
        final double p1 = x - (y + z) * 0.25;
        final double p2 = y - (x + z) * 0.25;
        final double p3 = z - (x + y) * 0.25;
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
        seed ^= seed >>> 14;
        xin = p0;
        yin = p2;
        zin = p3;
        final double b = valueNoise(seed, xin - a, yin, zin + a);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p3;
        final double c = valueNoise(seed, xin + b, yin - b, zin);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        final double d = valueNoise(seed, xin, yin - c, zin + c);

        final double result = (a + b + c + d) * 0.25;
        return  (result * result * (6.0 - 4.0 * result) - 1.0);
    }

    public static double foamNoise(final double x, final double y, final double z, final double w, int seed) {
        final double p0 = (x + y + z + w);
        final double p1 = x - (y + z + w);
        final double p2 = y - (x + z + w);
        final double p3 = z - (x + y + w);
        final double p4 = w - (x + y + z);
////rotated version of above points on a tetrahedron; the ones below are "more correct" but more complex (and slower?)       
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
        seed ^= seed >>> 14;
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        final double b = valueNoise(seed, xin - a, yin - a, zin + a, win + a);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        final double c = valueNoise(seed, xin + b, yin - b, zin + b, win - b);
        //seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        final double d = valueNoise(seed, xin + c, yin + c, zin - c, win - c);
        seed += 0x9E3779BD;
        seed ^= seed >>> 14;
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        final double e = valueNoise(seed, xin - d, yin + d, zin - d, win + d);

        final double result = (a + b + c + d + e) * 0.2;
        return  (result * result * (6.0 - 4.0 * result) - 1.0);
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
        return foamNoise(x, y, z, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        return foamNoise(x, y, z, (int) (seed ^ seed >>> 32));
    }
    @Override
    public double getNoise(double x, double y, double z, double w) {
        return foamNoise(x, y, z, w, seed);
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
        return foamNoise(x, y, z, w, (int) (seed ^ seed >>> 32));
    }
}
