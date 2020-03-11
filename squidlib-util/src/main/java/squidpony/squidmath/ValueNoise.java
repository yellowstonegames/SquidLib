package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * A low-quality continuous noise generator with strong grid artifacts, this is nonetheless useful as a building block.
 * This implements Noise2D, Noise3D, and Noise4D, and could have more dimensionality support added later. It has much
 * lower quality than {@link ClassicNoise}, but is structured similarly in many ways, and should be a little faster.
 * @see FoamNoise FoamNoise produces high-quality noise by combining a few rotated results of ValueNoise with domain warping.
 */
@Beta
public class ValueNoise implements Noise.Noise1D, Noise.Noise2D, Noise.Noise3D, Noise.Noise4D {
    public static final ValueNoise instance = new ValueNoise();
    
    public int seed = 0xD1CEBEEF;
    public ValueNoise() {
    }

    public ValueNoise(int seed) {
        this.seed = seed;
    }

    public ValueNoise(long seed) {
        this.seed = (int) (seed ^ seed >>> 32);
    }

    public static double valueNoise(int seed, double x) {
        int xFloor = x >= 0.0 ? (int) x : (int) x - 1;
        x -= xFloor;
        x *= x * (3.0 - 2.0 * x);
        xFloor *= 0x9E377;
        seed ^= seed >>> 17;
        return ((1.0 - x) * hashPart1024(xFloor, seed) + x * hashPart1024(xFloor + 0x9E377, seed))
                * (0x1.0040100401004p-10);
    }
    private static int hashPart1024(final int x, int s) {
        s *= ((x ^ x >>> 12) | 1);
        s = (s ^ s >>> 16) * 0xAC451;
        return (s >>> 3 ^ s >>> 10) & 0x3FF;
    }

    public static double valueNoise(int seed, double x, double y)
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
                + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xABC99, seed) + x * hashPart1024(xFloor + 0xD1B55, yFloor + 0xABC99, seed)))
                * (0x1.0040100401004p-10);
    }

    //// constants are the most significant 20 bits of constants from MummyNoise, incremented if even
    //// they should normally be used for the 3D version of R2, but we only use 2 of the 3 constants
    //x should be premultiplied by 0xD1B55
    //y should be premultiplied by 0xABC99
    private static int hashPart1024(final int x, final int y, int s) {
        s += x ^ y;
        return x + y + (s >>> 3 ^ s >>> 10) & 0x3FF;
    }

    public static double valueNoise(int seed, double x, double y, double z)
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
                ) * (0x1.0040100401004p-10);
    }

    //// constants are the most significant 20 bits of constants from MummyNoise, incremented if even
    //// they should normally be used for the 4D version of R2, but we only use 3 of the 4 constants
    //x should be premultiplied by 0xDB4F1
    //y should be premultiplied by 0xBBE05
    //z should be premultiplied by 0xA0F2F
    private static int hashPart1024(final int x, final int y, final int z, int s) {
        s += x ^ y ^ z;
        return x + y + z + (s >>> 3 ^ s >>> 10) & 0x3FF;
    }

    public static double valueNoise(int seed, double x, double y, double z, double w)
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
        int wFloor = w >= 0.0 ? (int) w : (int) w - 1;
        w -= wFloor;
        w *= w * (3.0 - 2.0 * w);
        //0xE19B1, 0xC6D1D, 0xAF36D, 0x9A695
        xFloor *= 0xE19B1;
        yFloor *= 0xC6D1D;
        zFloor *= 0xAF36D;
        wFloor *= 0x9A695;
        return ((1.0 - w) *
                ((1.0 - z) *
                        ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor, zFloor, wFloor, seed))
                                + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xC6D1D, zFloor, wFloor, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor + 0xC6D1D, zFloor, wFloor, seed)))
                        + z *
                        ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xAF36D, wFloor, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor, zFloor + 0xAF36D, wFloor, seed))
                                + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xC6D1D, zFloor + 0xAF36D, wFloor, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor + 0xC6D1D, zFloor + 0xAF36D, wFloor, seed))))
                + (w *
                ((1.0 - z) *
                        ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0x9A695, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor, zFloor, wFloor + 0x9A695, seed))
                                + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xC6D1D, zFloor, wFloor + 0x9A695, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor + 0xC6D1D, zFloor, wFloor + 0x9A695, seed)))
                        + z *
                        ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xAF36D, wFloor + 0x9A695, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor, zFloor + 0xAF36D, wFloor + 0x9A695, seed))
                                + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xC6D1D, zFloor + 0xAF36D, wFloor + 0x9A695, seed) + x * hashPart1024(xFloor + 0xE19B1, yFloor + 0xC6D1D, zFloor + 0xAF36D, wFloor + 0x9A695, seed)))
                ))) * (0x1.0040100401004p-10);
    }

    //// constants are the most significant 20 bits of constants from MummyNoise, incremented if even
    //// they should normally be used for the 5D version of R2, but we only use 4 of the 5 constants
    //x should be premultiplied by 0xE19B1
    //y should be premultiplied by 0xC6D1D
    //z should be premultiplied by 0xAF36D
    //w should be premultiplied by 0x9A695
    private static int hashPart1024(final int x, final int y, final int z, final int w, int s) {
        s += x ^ y ^ z ^ w;
        return x + y + z + w + (s >>> 3 ^ s >>> 10) & 0x3FF;
    }

    public static double valueNoise(int seed, double x, double y, double z, double w, double u, double v)
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
        int wFloor = w >= 0.0 ? (int) w : (int) w - 1;
        w -= wFloor;
        w *= w * (3.0 - 2.0 * w);
        int uFloor = u >= 0.0 ? (int) u : (int) u - 1;
        u -= uFloor;
        u *= u * (3.0 - 2.0 * u);
        int vFloor = v >= 0.0 ? (int) v : (int) v - 1;
        v -= vFloor;
        v *= v * (3.0 - 2.0 * v);
        //0xE95E1, 0xD4BC7, 0xC1EDB, 0xB0C8B, 0xA1279, 0x92E85
        xFloor *= 0xE95E1;
        yFloor *= 0xD4BC7;
        zFloor *= 0xC1EDB;
        wFloor *= 0xB0C8B;
        uFloor *= 0xA127B;
        vFloor *= 0x92E85;
        return ((1.0 - v) *
                ((1.0 - u) *
                        ((1.0 - w) *
                                ((1.0 - z) *
                                        ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor, vFloor, seed))
                                                + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor, seed)))
                                        + z *
                                        ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed))
                                                + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor, seed))))
                                + (w *
                                ((1.0 - z) *
                                        ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed))
                                                + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor, seed)))
                                        + z *
                                        ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed))
                                                + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor, seed)))
                                )))
                        + (u *
                        ((1.0 - w) *
                                ((1.0 - z) *
                                        ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed)))
                                        + z *
                                        ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor, seed))))
                                + (w *
                                ((1.0 - z) *
                                        ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed)))
                                        + z *
                                        ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor, seed)))
                                )))))
                + (v * 
                ((1.0 - u) *
                ((1.0 - w) *
                        ((1.0 - z) *
                                ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed))
                                        + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor, vFloor + 0x92E85, seed)))
                                + z *
                                ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed))
                                        + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor, vFloor + 0x92E85, seed))))
                        + (w *
                        ((1.0 - z) *
                                ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed))
                                        + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed)))
                                + z *
                                ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed))
                                        + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor, vFloor + 0x92E85, seed)))
                        )))
                + (u *
                ((1.0 - w) *
                        ((1.0 - z) *
                                ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed))
                                        + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed)))
                                + z *
                                ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed))
                                        + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor, uFloor + 0xA127B, vFloor + 0x92E85, seed))))
                        + (w *
                        ((1.0 - z) *
                                ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed))
                                        + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed)))
                                + z *
                                ((1.0 - y) * ((1.0 - x) * hashPart1024(xFloor, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed))
                                        + y * ((1.0 - x) * hashPart1024(xFloor, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed) + x * hashPart1024(xFloor + 0xE95E1, yFloor + 0xD4BC7, zFloor + 0xC1EDB, wFloor + 0xB0C8B, uFloor + 0xA127B, vFloor + 0x92E85, seed)))
                        ))))))
        ) * (0x1.0040100401004p-10);
    }

    //// constants are the most significant 20 bits of constants from MummyNoise, incremented if even
    //// they should normally be used for the 7D version of R2, but we only use 6 of the 7 constants
    //x should be premultiplied by 0xE95E1
    //y should be premultiplied by 0xD4BC7
    //z should be premultiplied by 0xC1EDB
    //w should be premultiplied by 0xB0C8B
    //u should be premultiplied by 0xA127B
    //v should be premultiplied by 0x92E85
    private static int hashPart1024(final int x, final int y, final int z, final int w, final int u, final int v, int s) {
        s += x ^ y ^ z ^ w ^ u ^ v;
        return x + y + z + w + u + v + (s >>> 3 ^ s >>> 10) & 0x3FF;
    }
    
    @Override
    public double getNoise(double x) {
        return valueNoise(seed, x) * 2.0 - 1.0;
    }

    @Override
    public double getNoiseWithSeed(double x, long seed) {
        return valueNoise((int) (seed ^ seed >>> 32), x) * 2.0 - 1.0;
    }

    @Override
    public double getNoise(double x, double y) {
        return valueNoise(seed, x, y) * 2.0 - 1.0;
    }
    @Override
    public double getNoiseWithSeed(double x, double y, long seed) {
        return valueNoise((int) (seed ^ seed >>> 32), x, y) * 2.0 - 1.0;
    }
    @Override
    public double getNoise(double x, double y, double z) {
        return valueNoise(seed, x, y, z) * 2.0 - 1.0;
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        return valueNoise((int) (seed ^ seed >>> 32), x, y, z) * 2.0 - 1.0;
    }
    @Override
    public double getNoise(double x, double y, double z, double w) {
        return valueNoise(seed, x, y, z, w) * 2.0 - 1.0;
    }
    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
        return valueNoise((int) (seed ^ seed >>> 32), x, y, z, w) * 2.0 - 1.0;
    }
}
