package squidpony.squidmath;

/**
 * An unusual continuous noise generator that tends to produce organic-looking forms, currently supporting 2D.
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
public class FoamNoise implements Noise.Noise2D, Noise.Noise3D {
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
        final double p0 = x * 0.139640 + y * -0.304485 + z * 0.942226;
        final double p1 = x * -0.185127 + y * -0.791704 + z * -0.582180;
        final double p2 = x * -0.776796 + y * 0.628752 + z * -0.035464;
        final double p3 = x * 0.822283 + y * 0.467437 + z * -0.324582;
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

    private static double valueNoise(int seed, double x, double y)
    {
        int xFloor = x >= 0.0 ? (int) x : (int) x - 1;
        x -= xFloor;
        x *= x * (3.0 - 2.0 * x);
        int yFloor = y >= 0.0 ? (int) y : (int) y - 1;
        y -= yFloor;
        y *= y * (3.0 - 2.0 * y);
        xFloor *= 0xD1B55;
        yFloor *= 0xABC99;
        return ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, seed) + x * hashPart1024(xFloor + 0xD1B55, yFloor, seed))
                + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xABC99, seed) + x * hashPart1024(xFloor + 0xD1B55, yFloor + 0xABC99, seed))) * (0x1.010101010101p-10);
    }

    //// constants are the most significant 20 bits of constants from MummyNoise, incremented if even
    //// they should normally be used for the 3D version of R2, but we only use 2 of the 3 constants
    //x should be premultiplied by 0xD1B55
    //y should be premultiplied by 0xABC99
    private static int hashPart1024(final int x, final int y, int s) {
        s += x ^ y;
        return (s >>> 3 ^ s >>> 10) & 0x3FF;
    }

    private static double valueNoise(int seed, double x, double y, double z)
    {
        int xFloor = x >= 0.0 ? (int) x : (int) x - 1;
        x -= xFloor;
        x *= x * (3.0 - 2.0 * x);
        int yFloor = y >= 0.0 ? (int) y : (int) y - 1;
        y -= yFloor;
        y *= y * (3.0 - 2.0 * y);
        int zFloor = z >= 0.0 ? (int) z : (int) z - 1;
        z -= zFloor;
        z *= z * (3.0 - 2.0 * z);
        //0xDB4F1, 0xBBE05, 0xA0F2F
        xFloor *= 0xDB4F1;
        yFloor *= 0xBBE05;
        zFloor *= 0xA0F2F;
        return ((1.0 - z) *
                ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor, seed) + x * hashPart1024(xFloor + 0xDB4F1, yFloor, zFloor, seed))
                        + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xBBE05, zFloor, seed) + x * hashPart1024(xFloor + 0xDB4F1, yFloor + 0xBBE05, zFloor, seed)))
                + z * 
                ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xA0F2F, seed) + x * hashPart1024(xFloor + 0xDB4F1, yFloor, zFloor + 0xA0F2F, seed)) 
                        + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xBBE05, zFloor + 0xA0F2F, seed) + x * hashPart1024(xFloor + 0xDB4F1, yFloor + 0xBBE05, zFloor + 0xA0F2F, seed)))
                ) * (0x1.010101010101p-10);
    }

    //// constants are the most significant 20 bits of constants from MummyNoise, incremented if even
    //// they should normally be used for the 4D version of R2, but we only use 3 of the 4 constants
    //x should be premultiplied by 0xDB4F1
    //y should be premultiplied by 0xBBE05
    //z should be premultiplied by 0xA0F2F
    private static int hashPart1024(final int x, final int y, final int z, int s) {
        s += x ^ y ^ z;
        return (s >>> 3 ^ s >>> 10) & 0x3FF;
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
}
