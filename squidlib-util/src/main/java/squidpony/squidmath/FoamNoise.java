package squidpony.squidmath;

/**
 * An unusual continuous noise generator that tends to produce organic-looking forms, currently supporting 2D.
 * Produces noise values from -1.0 inclusive to 1.0 exclusive.
 */
public class FoamNoise implements Noise.Noise2D, Noise.Noise3D {
    public static final FoamNoise instance = new FoamNoise();
    
    public int seed = 0xF0F0F0F0;
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
        final double a = valueNoise(seed, xin + NumberTools.swayRandomized(~seed, yin) * 0.5f, yin);
        seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed ^= seed >>> 14;
        xin = x * -0.989992 + y * 0.141120; // sin and cos of 3
        yin = x * -0.141120 + y * -0.989992;
        final double b = valueNoise(seed, xin + NumberTools.swayRandomized(~seed, yin - a) * 0.5f, yin + a);
        seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed ^= seed >>> 14;
        xin = x * 0.283662 + y * -0.958924; // sin and cos of 5
        yin = x * 0.958924 + y * 0.283662;
        final double c = valueNoise(seed, xin + NumberTools.swayRandomized(~seed, yin + b) * 0.5f, yin - b);
        final double result = (a + b) * 0.3125 + c * 0.375;
        return result * result * (6.0 - 4.0 * result) - 1.0;
    }
    
    public static double foamNoise(final double x, final double y, final double z, int seed) {
        double xin = x * 0.038219 + y * 0.059523 + z * 0.997495;
        double yin = x * -0.839363 + y * 0.538949 + z * -0.070737;
        double zin = x * -0.224845 + y * -0.350175 + z * 0.909297;
        final double a = valueNoise(seed, xin + NumberTools.swayRandomized(~seed, yin) * 0.5f, yin, zin);
        seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed ^= seed >>> 14;
        xin = x * 0.793126 + y * -0.113057 + z * 0.598472;
        yin = x * -0.084456 + y * -0.592483 + z * 0.801144;
        zin = x * 0.647102 + y * -0.092242 + z * -0.756802;
        final double b = valueNoise(seed, xin - a, yin + NumberTools.swayRandomized(~seed, zin - a) * 0.5f, zin + a);
        seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed ^= seed >>> 14;
        xin = x * -0.265637 + y * 0.897991 + z * -0.350783;
        yin = x * -0.336375 + y * -0.099504 + z * 0.936457;
        zin = x * 0.272364 + y * -0.920731 + z * -0.279415;
        final double c = valueNoise(seed, xin + b, yin - b, zin  + NumberTools.swayRandomized(~seed, xin - b) * 0.5f);
        seed = (seed ^ 0x9E3779BD) * 0xDAB;
        seed ^= seed >>> 14;
        xin = x * -0.158919 + y * -0.138490 + z * -0.977530;
        yin = x * 0.642224 + y * -0.736962 + z * 0.210796;
        zin = x * -0.109693 + y * -0.095592 + z * 0.989358;
        final double d = valueNoise(seed, xin + NumberTools.swayRandomized(~seed, zin - c) * 0.5f, yin - c, zin + c);

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

    //x should be premultiplied by 0xD1B55
    //y should be premultiplied by 0xABC99
    private static int hashPart1024(final int x, final int y, int s) {
        s += x ^ y;
        s ^= s << 8;
        return s >>> 10 & 0x3FF;
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
        xFloor *= 0xD1B55;
        yFloor *= 0xABC99;
        zFloor *= 0x8CB93;
        return ((1.0 - z) *
                ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor, seed) + x * hashPart1024(xFloor + 0xD1B55, yFloor, zFloor, seed))
                        + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xABC99, zFloor, seed) + x * hashPart1024(xFloor + 0xD1B55, yFloor + 0xABC99, zFloor, seed)))
                + z * 
                ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor + 0x8CB93, seed) + x * hashPart1024(xFloor + 0xD1B55, yFloor, zFloor + 0x8CB93, seed)) 
                        + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xABC99, zFloor + 0x8CB93, seed) + x * hashPart1024(xFloor + 0xD1B55, yFloor + 0xABC99, zFloor + 0x8CB93, seed)))
                ) * (0x1.010101010101p-10);
    }

    //x should be premultiplied by 0xD1B55
    //y should be premultiplied by 0xABC99
    //z should be premultiplied by 0x8CB93
    private static int hashPart1024(final int x, final int y, final int z, int s) {
        s += x ^ y ^ z;
        s ^= s << 8;
        return s >>> 10 & 0x3FF;
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
