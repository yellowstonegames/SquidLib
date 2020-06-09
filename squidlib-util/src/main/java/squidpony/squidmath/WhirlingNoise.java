package squidpony.squidmath;

import java.io.Serializable;

import static squidpony.squidmath.Noise.fastFloor;

/**
 * A Noise class that's here for compatibility; it extends {@link SeededNoise} and delegates to it for all methods
 * except {@link #noiseAlt(double, double)} and {@link #noiseAlt(double, double, double)}. Normally you should use
 * SeededNoise directly for new code if you expect to mostly use the inner classes in {@link Noise} for special effects,
 * or {@link FastNoise} if you want the effects all in one place or to use {@code float} instead of {@code double}.
 * <br>
 * Created by Tommy Ettinger on 12/14/2016. The technique for point hashing in the "noiseAlt" code is based closely on
 * <a href="http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.140.3594&rep=rep1&type=pdf">this paper</a>,
 * with credit to Andrew Kensler, Aaron Knoll and Peter Shirley. This technique is good, but it may be periodic in
 * undesirable ways, and isn't much faster when implemented in Java than {@link IntPointHash}, if at all.
 */
public class WhirlingNoise extends SeededNoise implements Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D,
        Serializable {

    private static final long serialVersionUID = 6L;
    public static final WhirlingNoise instance = new WhirlingNoise();
    public WhirlingNoise()
    {
        this(123456789);
    }
    public WhirlingNoise(long seed) {
        super(seed);
//        System.out.println("{");
//        for (int i = 0; i < grad3f.length; i++) {
//            System.out.printf("{% 2.15ff, % 2.15ff, % 2.15ff},\n", grad3f[i][0], grad3f[i][1], grad3f[i][2]);
//        }
//        System.out.println("}");
    }

    /**
     * The 32 3D vertices of a rhombic triacontahedron. These were modified from values taken from Vladimir Bulatov's
     * stellation applet, which has available source but is unlicensed, and is
     * <a href="http://www.bulatov.org/polyhedra/stellation_applet/index.html">available here</a>, but the vertices are
     * mathematical constants so copyright isn't an issue.
     */
    protected static final float[][] grad3f =
            {
                    {-0.448549002408981f,  1.174316525459290f,  0.000000000000001f},
                    { 0.000000000000001f,  1.069324374198914f,  0.660878777503967f},
                    { 0.448549002408981f,  1.174316525459290f,  0.000000000000001f},
                    { 0.000000000000001f,  1.069324374198914f, -0.660878777503967f},
                    {-0.725767493247986f,  0.725767493247986f, -0.725767493247986f},
                    {-1.069324374198914f,  0.660878777503967f,  0.000000000000001f},
                    {-0.725767493247986f,  0.725767493247986f,  0.725767493247986f},
                    { 0.725767493247986f,  0.725767493247986f,  0.725767493247986f},
                    { 1.069324374198914f,  0.660878777503967f,  0.000000000000000f},
                    { 0.725767493247986f,  0.725767493247986f, -0.725767493247986f},
                    {-0.660878777503967f,  0.000000000000003f, -1.069324374198914f},
                    {-1.174316525459290f,  0.000000000000003f, -0.448549002408981f},
                    { 0.000000000000000f,  0.448549002408981f, -1.174316525459290f},
                    {-0.660878777503967f,  0.000000000000001f,  1.069324374198914f},
                    { 0.000000000000001f,  0.448549002408981f,  1.174316525459290f},
                    {-1.174316525459290f,  0.000000000000001f,  0.448549002408981f},
                    { 0.660878777503967f,  0.000000000000001f,  1.069324374198914f},
                    { 1.174316525459290f,  0.000000000000001f,  0.448549002408981f},
                    { 0.660878777503967f,  0.000000000000001f, -1.069324374198914f},
                    { 1.174316525459290f,  0.000000000000001f, -0.448549002408981f},
                    {-0.725767493247986f, -0.725767493247986f, -0.725767493247986f},
                    {-1.069324374198914f, -0.660878777503967f, -0.000000000000001f},
                    {-0.000000000000001f, -0.448549002408981f, -1.174316525459290f},
                    {-0.000000000000001f, -0.448549002408981f,  1.174316525459290f},
                    {-0.725767493247986f, -0.725767493247986f,  0.725767493247986f},
                    { 0.725767493247986f, -0.725767493247986f,  0.725767493247986f},
                    { 1.069324374198914f, -0.660878777503967f,  0.000000000000001f},
                    { 0.725767493247986f, -0.725767493247986f, -0.725767493247986f},
                    {-0.000000000000004f, -1.069324374198914f, -0.660878777503967f},
                    {-0.448549002408981f, -1.174316525459290f, -0.000000000000003f},
                    {-0.000000000000003f, -1.069324374198914f,  0.660878777503967f},
                    { 0.448549002408981f, -1.174316525459290f,  0.000000000000003f},
            };

    //    public static void randomUnitVector4(long seed, final float[] vector)
//    {
//        double mag = 0.0;
//        float t;
//        vector[0] = (t = NumberTools.formCurvedFloat(seed += 0xCB72F6C7));
//        mag += t * t;
//        vector[1] = (t = NumberTools.formCurvedFloat(seed += 0xCB72F6C7));
//        mag += t * t;
//        vector[2] = (t = NumberTools.formCurvedFloat(seed += 0xCB72F6C7));
//        mag += t * t;
//        vector[3] = (t = NumberTools.formCurvedFloat(seed + 0xCB72F6C7));
//        mag += t * t;
//
//        if(mag == 0)
//        {
//            vector[0] = 1f;
//            mag = 1.0;
//        }
//        else
//            mag = Math.sqrt(mag);
//        vector[0] /= mag;
//        vector[1] /= mag;
//        vector[2] /= mag;
//        vector[3] /= mag;
//    }

//    static {
//        final float len = (float) (1.7861513777574233 / Math.sqrt(3.0));
//        for (int i = 0; i < 64; i++) {
////            float x = grad4f[i][0], y = grad4f[i][1], z = grad4f[i][2], w = grad4f[i][3];
////            final float len = 1.4142135623730951f / (float)Math.sqrt(x * x + y * y + z * z + w * w);
//            //final float len = 2f / Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z))), len3 = len * 1.5f;
//            grad4f[i][0] *= len;
//            grad4f[i][1] *= len;
//            grad4f[i][2] *= len;
//            grad4f[i][3] *= len;
//            System.out.println("{" + squidpony.StringKit.join(", ", grad4f[i]) + "},");
//        }
//    }

//    protected static final float[][] phiGrad3f = new float[96][3];
//
//    static {
//        final float root2 = 1.2599211f;
//        int i = 0;
//        for (; i < 16; i++) {
//            phiGrad3f[i][0] = phiGrad2f[i & 15][0] * root2;
//            phiGrad3f[i][1] = phiGrad2f[i & 15][1] * root2;
//        }
//        for (; i < 32; i++) {
//            phiGrad3f[i][0] = phiGrad2f[i & 15][1] * root2;
//            phiGrad3f[i][1] = phiGrad2f[i & 15][0] * root2;
//        }
//        for (; i < 48; i++) {
//            phiGrad3f[i][0] = phiGrad2f[i & 15][0] * root2;
//            phiGrad3f[i][2] = phiGrad2f[i & 15][1] * root2;
//        }
//        for (; i < 64; i++) {
//            phiGrad3f[i][0] = phiGrad2f[i & 15][1] * root2;
//            phiGrad3f[i][2] = phiGrad2f[i & 15][0] * root2;
//        }
//        for (; i < 80; i++) {
//            phiGrad3f[i][1] = phiGrad2f[i & 15][0] * root2;
//            phiGrad3f[i][2] = phiGrad2f[i & 15][1] * root2;
//        }
//        for (; i < 96; i++) {
//            phiGrad3f[i][1] = phiGrad2f[i & 15][1] * root2;
//            phiGrad3f[i][2] = phiGrad2f[i & 15][0] * root2;
//        }
//    }

//    public static final int[]
//            perm_x = {59, 146, 27, 99, 226, 210, 44, 129, 102, 237, 2, 107, 157, 173, 159, 16, 128, 41, 228, 114, 63, 105, 241, 144, 187, 116, 223, 122, 234, 52, 96, 35, 213, 176, 177, 141, 132, 240, 194, 163, 0, 3, 168, 133, 55, 203, 53, 50, 42, 79, 130, 156, 209, 135, 151, 178, 85, 154, 117, 148, 140, 82, 6, 69, 127, 214, 95, 175, 46, 30, 104, 197, 170, 33, 70, 167, 217, 233, 219, 84, 196, 109, 40, 190, 123, 165, 61, 212, 255, 184, 19, 182, 38, 112, 172, 103, 25, 244, 245, 201, 192, 60, 14, 231, 68, 71, 236, 193, 115, 7, 113, 118, 110, 131, 198, 216, 29, 195, 211, 246, 153, 222, 185, 208, 200, 158, 66, 137, 179, 26, 147, 235, 106, 90, 164, 9, 238, 101, 138, 227, 21, 37, 23, 152, 8, 161, 108, 250, 183, 225, 121, 24, 51, 252, 87, 242, 98, 188, 232, 171, 93, 56, 57, 5, 12, 120, 74, 43, 136, 139, 32, 13, 191, 67, 189, 186, 162, 199, 10, 20, 89, 15, 31, 58, 221, 18, 253, 28, 4, 218, 142, 205, 247, 94, 215, 39, 166, 150, 224, 77, 34, 169, 206, 47, 81, 97, 83, 220, 76, 229, 160, 54, 243, 45, 181, 92, 119, 48, 155, 62, 174, 248, 36, 239, 145, 124, 125, 65, 72, 180, 134, 111, 204, 207, 100, 73, 251, 143, 249, 254, 230, 11, 78, 80, 149, 75, 91, 126, 17, 86, 49, 88, 64, 22, 202, 1},
//            perm_y = {189, 111, 17, 214, 57, 208, 191, 225, 241, 152, 145, 71, 2, 141, 183, 218, 66, 178, 34, 161, 198, 47, 200, 180, 134, 239, 162, 18, 155, 216, 192, 173, 219, 9, 51, 124, 95, 122, 217, 135, 31, 50, 179, 237, 32, 39, 209, 112, 96, 92, 68, 79, 228, 193, 234, 90, 164, 137, 196, 184, 185, 114, 226, 67, 249, 163, 85, 26, 125, 28, 251, 45, 61, 220, 213, 139, 70, 201, 243, 22, 142, 246, 102, 229, 10, 107, 4, 240, 194, 35, 230, 86, 223, 20, 12, 233, 23, 77, 119, 176, 147, 182, 21, 195, 91, 118, 247, 33, 100, 99, 29, 188, 172, 144, 136, 131, 40, 13, 38, 150, 224, 205, 8, 252, 253, 190, 46, 143, 53, 231, 153, 94, 177, 88, 55, 105, 121, 16, 25, 207, 138, 5, 63, 82, 202, 58, 170, 41, 78, 167, 64, 60, 14, 103, 42, 154, 19, 80, 72, 37, 83, 129, 187, 244, 215, 242, 81, 15, 151, 186, 59, 101, 168, 175, 89, 248, 232, 212, 204, 199, 108, 73, 98, 210, 44, 1, 76, 48, 49, 250, 106, 203, 113, 43, 221, 146, 245, 148, 115, 165, 181, 84, 93, 3, 206, 65, 123, 158, 6, 126, 238, 109, 130, 227, 140, 120, 74, 171, 110, 222, 87, 156, 132, 97, 159, 197, 255, 56, 27, 62, 157, 75, 211, 254, 127, 169, 236, 235, 149, 52, 36, 24, 0, 11, 160, 133, 174, 30, 104, 69, 128, 116, 117, 54, 166, 7},
//            perm_z = {253, 212, 4, 237, 36, 182, 213, 233, 147, 239, 226, 41, 74, 65, 68, 165, 70, 231, 217, 116, 113, 193, 162, 112, 228, 254, 183, 176, 151, 80, 17, 60, 155, 246, 174, 3, 202, 208, 127, 7, 57, 1, 132, 79, 224, 99, 238, 195, 236, 9, 115, 154, 23, 227, 76, 158, 130, 16, 89, 214, 61, 114, 187, 90, 49, 24, 64, 33, 96, 242, 25, 37, 215, 35, 46, 109, 134, 141, 136, 225, 138, 43, 21, 184, 189, 13, 230, 188, 40, 50, 243, 244, 211, 156, 85, 120, 223, 58, 234, 71, 6, 28, 179, 67, 125, 69, 192, 131, 44, 175, 34, 15, 32, 77, 191, 222, 83, 47, 128, 218, 198, 84, 149, 26, 121, 190, 255, 150, 117, 92, 140, 101, 172, 62, 93, 97, 27, 103, 106, 161, 194, 201, 204, 45, 206, 111, 81, 252, 249, 73, 42, 248, 108, 118, 63, 56, 31, 216, 153, 180, 19, 126, 38, 139, 66, 88, 247, 143, 177, 137, 12, 199, 104, 235, 102, 75, 100, 129, 251, 18, 159, 107, 196, 22, 10, 152, 209, 94, 181, 250, 51, 210, 185, 144, 200, 169, 232, 122, 145, 173, 95, 171, 166, 229, 14, 5, 0, 105, 2, 163, 157, 48, 53, 133, 110, 52, 160, 186, 123, 124, 91, 20, 221, 240, 87, 178, 98, 207, 142, 148, 59, 203, 245, 205, 72, 11, 164, 39, 170, 135, 168, 197, 55, 86, 219, 167, 8, 82, 78, 220, 29, 146, 241, 54, 119, 30},
//            perm_w = {57, 1, 140, 48, 61, 156, 230, 173, 2, 231, 12, 214, 142, 242, 255, 195, 198, 220, 157, 139, 194, 99, 247, 248, 155, 178, 29, 41, 23, 193, 0, 30, 95, 171, 174, 222, 91, 54, 8, 67, 32, 129, 46, 124, 172, 148, 17, 105, 228, 118, 191, 33, 224, 5, 25, 158, 185, 92, 63, 199, 53, 107, 34, 180, 125, 69, 200, 116, 121, 216, 42, 233, 70, 43, 72, 26, 202, 62, 51, 15, 10, 16, 217, 207, 14, 175, 59, 52, 223, 246, 89, 109, 83, 13, 68, 90, 147, 239, 234, 18, 151, 114, 76, 143, 100, 197, 106, 176, 232, 208, 85, 165, 40, 186, 251, 101, 44, 65, 93, 218, 253, 144, 123, 11, 113, 167, 102, 240, 177, 137, 4, 184, 181, 20, 110, 37, 138, 111, 132, 94, 6, 122, 119, 75, 78, 84, 21, 3, 74, 235, 127, 112, 19, 58, 149, 161, 159, 73, 136, 150, 215, 35, 38, 86, 211, 190, 128, 203, 168, 9, 166, 244, 36, 28, 153, 225, 108, 254, 55, 169, 104, 141, 145, 22, 49, 212, 183, 79, 189, 227, 170, 60, 245, 205, 64, 252, 241, 80, 162, 97, 206, 163, 192, 146, 66, 182, 187, 135, 130, 152, 81, 71, 134, 39, 179, 188, 87, 126, 209, 229, 219, 133, 204, 210, 27, 103, 98, 154, 31, 250, 196, 7, 236, 77, 226, 56, 96, 88, 221, 45, 160, 50, 115, 237, 164, 201, 213, 82, 117, 24, 243, 131, 249, 47, 238, 120},
//            perm_u = {132, 148, 19, 244, 162, 163, 194, 37, 4, 250, 198, 154, 170, 137, 6, 60, 123, 73, 138, 41, 145, 92, 61, 82, 251, 175, 57, 207, 153, 50, 113, 105, 106, 242, 253, 94, 128, 9, 164, 143, 234, 80, 160, 252, 136, 239, 232, 150, 89, 167, 100, 131, 127, 178, 31, 188, 217, 5, 27, 33, 119, 152, 83, 195, 72, 88, 223, 176, 110, 111, 134, 233, 200, 190, 130, 86, 102, 69, 202, 240, 63, 13, 70, 229, 93, 24, 241, 22, 191, 99, 245, 139, 85, 254, 53, 45, 46, 182, 185, 26, 76, 197, 104, 67, 174, 20, 108, 184, 68, 171, 172, 90, 29, 107, 32, 79, 59, 126, 211, 112, 157, 201, 215, 237, 51, 84, 23, 135, 12, 28, 155, 124, 42, 43, 74, 173, 140, 114, 78, 18, 34, 1, 142, 180, 243, 193, 2, 161, 25, 212, 181, 218, 115, 39, 177, 71, 17, 186, 249, 225, 226, 122, 117, 214, 8, 129, 44, 7, 98, 216, 40, 116, 0, 103, 96, 30, 209, 47, 236, 11, 247, 58, 151, 52, 81, 141, 147, 169, 255, 16, 219, 75, 192, 208, 87, 56, 230, 231, 14, 97, 64, 54, 10, 222, 238, 205, 66, 120, 183, 133, 206, 109, 213, 144, 121, 158, 55, 235, 125, 3, 221, 118, 189, 165, 166, 62, 49, 146, 196, 77, 224, 203, 38, 156, 228, 48, 204, 35, 36, 210, 149, 227, 168, 199, 179, 246, 91, 248, 21, 65, 95, 101, 187, 220, 159, 15},
//            perm_v = {2, 9, 4, 237, 219, 73, 247, 203, 228, 220, 46, 229, 61, 156, 170, 75, 223, 144, 81, 252, 172, 208, 76, 218, 177, 103, 123, 244, 14, 39, 255, 90, 168, 43, 174, 3, 113, 107, 145, 233, 130, 254, 192, 11, 211, 190, 68, 105, 117, 178, 251, 18, 66, 242, 230, 248, 95, 137, 29, 26, 164, 65, 153, 120, 70, 77, 64, 33, 23, 133, 59, 7, 40, 16, 106, 41, 121, 216, 238, 135, 19, 212, 157, 48, 232, 28, 128, 22, 245, 171, 183, 56, 74, 99, 51, 150, 236, 111, 234, 71, 189, 167, 213, 37, 198, 50, 12, 79, 31, 250, 136, 165, 185, 246, 55, 86, 142, 62, 42, 52, 147, 205, 89, 94, 224, 141, 221, 180, 138, 129, 140, 101, 83, 193, 127, 67, 108, 84, 166, 109, 181, 20, 34, 195, 87, 24, 217, 116, 36, 88, 196, 82, 57, 239, 243, 124, 134, 175, 119, 210, 32, 163, 38, 139, 249, 227, 25, 97, 10, 118, 72, 131, 91, 54, 204, 225, 253, 58, 115, 154, 202, 122, 110, 112, 215, 1, 149, 146, 44, 201, 17, 240, 206, 197, 200, 169, 159, 13, 179, 143, 160, 152, 226, 161, 241, 80, 102, 15, 155, 92, 21, 184, 96, 148, 8, 158, 125, 35, 63, 176, 194, 235, 187, 30, 100, 231, 98, 207, 53, 47, 93, 173, 78, 186, 132, 199, 151, 114, 0, 45, 49, 126, 191, 222, 6, 182, 162, 188, 27, 69, 209, 214, 104, 5, 85, 60};

    //    protected static double dot(final float g[], final double x, final double y, final double z) {
//        return g[0] * x + g[1] * y + g[2] * z;
//    }
//    protected static double dot(final float g[], final double x, final double y, final double z, final double w) {
//        return g[0] * x + g[1] * y + g[2] * z + g[3] * w;
//    }

    protected static float dotf(final float[] g, final float x, final float y, final float z) {
        return g[0] * x + g[1] * y + g[2] * z;
    }

    /**
     * Delegates to {@link SeededNoise#noise(double, double, long)} with the initial seed given to this object.
     *
     * @param x X input
     * @param y Y input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public double getNoise(final double x, final double y) {
        return noise(x, y, defaultSeed);
    }

    /**
     * Delegates to {@link SeededNoise#noise(double, double, long)}.
     * 
     * @param x X input
     * @param y Y input
     * @param seed will completely alter the shape of the noise if changed between calls
     * @return noise from -1.0 to 1.0, inclusive
     */
    public double getNoiseWithSeed(final double x, final double y, final long seed) {
        return noise(x, y, seed);
    }
    /**
     * Delegates to {@link SeededNoise#noise(double, double, double, long)} with the initial seed given to this object.
     * 
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public double getNoise(final double x, final double y, final double z) {
        return noise(x, y, z, defaultSeed);
    }
    /**
     * Delegates to {@link SeededNoise#noise(double, double, double, long)}.
     * 
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @param seed will completely alter the shape of the noise if changed between calls
     * @return noise from -1.0 to 1.0, inclusive
     */
    public double getNoiseWithSeed(final double x, final double y, final double z, final long seed) {
        return noise(x, y, z, seed);
    }

    /**
     * Delegates to {@link SeededNoise#noise(double, double, double, double, long)} with the initial seed given to this object.
     * 
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @param w W input (fourth-dimension)
     * @return noise from -1.0 to 1.0, inclusive
     */
    public double getNoise(final double x, final double y, final double z, final double w) {
        return noise(x, y, z, w, defaultSeed);
    }
    /**
     * Delegates to {@link SeededNoise#noise(double, double, double, double, long)}.
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @param w W input (fourth-dimension)
     * @param seed will completely alter the shape of the noise if changed between calls
     * @return noise from -1.0 to 1.0, inclusive
     */
    public double getNoiseWithSeed(final double x, final double y, final double z, final double w, final long seed) {
        return noise(x, y, z, w, seed);
    }


    /**
     * Delegates to {@link SeededNoise#noise(double, double, long)} with 123456789 as the seed.
     *
     * @param xin X input
     * @param yin Y input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double xin, final double yin){
        return SeededNoise.noise(xin, yin, 123456789);
    }

    /**
     * Delegates to {@link SeededNoise#noise(double, double, long)}.
     *
     * @param xin X input
     * @param yin Y input
     * @param seed will completely alter the shape of the noise if changed between calls
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double xin, final double yin, final long seed) {
        return SeededNoise.noise(xin, yin, seed);
    }



    /**
     * Delegates to {@link SeededNoise#noise(double, double, double, long)} with 123456789 as the seed.
     * @param xin X input
     * @param yin Y input
     * @param zin Z input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double xin, final double yin, final double zin){
        return SeededNoise.noise(xin, yin, zin, 123456789);
    }

    /**
     * Delegates to {@link SeededNoise#noise(double, double, double, long)}.
     * @param xin X input
     * @param yin Y input
     * @param zin Z input
     * @param seed will completely alter the shape of the noise if changed between calls
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double xin, final double yin, final double zin, final long seed){ 
        return SeededNoise.noise(xin, yin, zin, seed);
    }

    /**
     * Delegates to {@link SeededNoise#noise(double, double, double, double, long)} with 123456789 as the seed.
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @param w W input (fourth-dimensional)
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double x, final double y, final double z, final double w) {
        return SeededNoise.noise(x, y, z, w, 123456789);
    }

    /**
     * Delegates to {@link SeededNoise#noise(double, double, double, double, long)}.
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @param w W input (fourth-dimensional)
     * @param seed will completely alter the shape of the noise if changed between calls
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static double noise(final double x, final double y, final double z, final double w, final long seed) {
        return SeededNoise.noise(x, y, z, w, seed);
    }


//    public static double noise(final double x, final double y, final double z, final double w, final long seed)
//    {
//        // The skewing and unskewing factors are hairy again for the 4D case
//
//        // Skew the (x,y,z,w) space to figure out which cell of 24 simplices
//        // we're in
//        double s = (x + y + z + w) * F4; // Factor for 4D skewing
//        int i = fastFloor(x + s);
//        int j = fastFloor(y + s);
//        int k = fastFloor(z + s);
//        int l = fastFloor(w + s);
//        double t = (i + j + k + l) * G4; // Factor for 4D unskewing
//        double X0 = i - t; // Unskew the cell origin back to (x,y,z,w) space
//        double Y0 = j - t;
//        double Z0 = k - t;
//        double W0 = l - t;
//        double x0 = x - X0; // The x,y,z,w distances from the cell origin
//        double y0 = y - Y0;
//        double z0 = z - Z0;
//        double w0 = w - W0;
//        // For the 4D case, the simplex is a 4D shape I won't even try to
//        // describe.
//        // To find out which of the 24 possible simplices we're in, we need
//        // to figure out the magnitude ordering of x0, y0, z0 and w0.
//        // The method below is a good way of finding the ordering of x,y,z,w
//        // and
//        // then find the correct traversal order for the simplex we’re in.
//        // First, six pair-wise comparisons are performed between each
//        // possible pair
//        // of the four coordinates, and the results are used to add up binary
//        // bits
//        // for an integer index.
//        int c = (x0 > y0 ? 32 : 0) | (x0 > z0 ? 16 : 0) | (y0 > z0 ? 8 : 0) |
//                (x0 > w0 ? 4 : 0) | (y0 > w0 ? 2 : 0) | (z0 > w0 ? 1 : 0);
//
//        // simplex[c] is a 4-vector with the numbers 0, 1, 2 and 3 in some
//        // order.
//        // Many values of c will never occur, since e.g. x>y>z>w makes x<z,
//        // y<w and x<w
//        // impossible. Only the 24 indices which have non-zero entries make
//        // any sense.
//        // We use a thresholding to set the coordinates in turn from the
//        // largest magnitude.
//        // The number 3 in the "simplex" array is at the position of the
//        // largest coordinate.
//
//        // The integer offsets for the second simplex corner
//        int i1 = simplex[c][0] >= 3 ? 1 : 0;
//        int j1 = simplex[c][1] >= 3 ? 1 : 0;
//        int k1 = simplex[c][2] >= 3 ? 1 : 0;
//        int l1 = simplex[c][3] >= 3 ? 1 : 0;
//        // The number 2 in the "simplex" array is at the second largest
//        // coordinate.
//
//        // The integer offsets for the third simplex corner
//        int i2 = simplex[c][0] >= 2 ? 1 : 0;
//        int j2 = simplex[c][1] >= 2 ? 1 : 0;
//        int k2 = simplex[c][2] >= 2 ? 1 : 0;
//        int l2 = simplex[c][3] >= 2 ? 1 : 0;
//        // The number 1 in the "simplex" array is at the second smallest
//        // coordinate.
//
//        // The integer offsets for the fourth simplex corner
//        int i3 = simplex[c][0] >= 1 ? 1 : 0;
//        int j3 = simplex[c][1] >= 1 ? 1 : 0;
//        int k3 = simplex[c][2] >= 1 ? 1 : 0;
//        int l3 = simplex[c][3] >= 1 ? 1 : 0;
//        // The fifth corner has all coordinate offsets = 1, so no need to
//        // look that up.
//        double x1 = x0 - i1 + G4; // Offsets for second corner in (x,y,z,w) coords
//        double y1 = y0 - j1 + G4;
//        double z1 = z0 - k1 + G4;
//        double w1 = w0 - l1 + G4;
//        double x2 = x0 - i2 + 2.0 * G4; // Offsets for third corner in (x,y,z,w) coords
//        double y2 = y0 - j2 + 2.0 * G4;
//        double z2 = z0 - k2 + 2.0 * G4;
//        double w2 = w0 - l2 + 2.0 * G4;
//        double x3 = x0 - i3 + 3.0 * G4; // Offsets for fourth corner in (x,y,z,w) coords
//        double y3 = y0 - j3 + 3.0 * G4;
//        double z3 = z0 - k3 + 3.0 * G4;
//        double w3 = w0 - l3 + 3.0 * G4;
//        double x4 = x0 - 1.0 + 4.0 * G4; // Offsets for last corner in (x,y,z,w) coords
//        double y4 = y0 - 1.0 + 4.0 * G4;
//        double z4 = z0 - 1.0 + 4.0 * G4;
//        double w4 = w0 - 1.0 + 4.0 * G4;
//
//        final int s0 = (int)(seed & 63), s1 = (int)(seed >>> 6 & 63), s2 = (int)(seed >>> 12 & 63), s3 = (int)(seed >>> 18 & 63);
//        final int gi0 = (perm_x[(i) + s0 & 255] ^ perm_y[(j) + s1 & 255]           ^ perm_z[(k) + s2 & 255]      ^ perm_w[(l) + s3 & 255]) & 63;
//        final int gi1 = (perm_x[(i + i1) + s0 & 255] ^ perm_y[(j + j1) + s1 & 255] ^ perm_z[(k + k1) + s2 & 255] ^ perm_w[(l + l1) + s3 & 255]) & 63;
//        final int gi2 = (perm_x[(i + i2) + s0 & 255] ^ perm_y[(j + j2) + s1 & 255] ^ perm_z[(k + k2) + s2 & 255] ^ perm_w[(l + l2) + s3 & 255]) & 63;
//        final int gi3 = (perm_x[(i + i3) + s0 & 255] ^ perm_y[(j + j3) + s1 & 255] ^ perm_z[(k + k3) + s2 & 255] ^ perm_w[(l + l3) + s3 & 255]) & 63;
//        final int gi4 = (perm_x[(i + 1) + s0 & 255] ^ perm_y[(j + 1) + s1 & 255]   ^ perm_z[(k + 1) + s2  & 255] ^ perm_w[(l + 1) + s3 & 255]) & 63;
//        // Noise contributions from the five corners are n0 to n4
//
//        // Calculate the contribution from the five corners
//        double t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0, n0;
//        if (t0 < 0) {
//            n0 = 0.0;
//        } else {
//            t0 *= t0;
//            n0 = t0 * t0 * dot(grad4f[gi0], x0, y0, z0, w0);
//        }
//        double t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1, n1;
//        if (t1 < 0) {
//            n1 = 0.0;
//        } else {
//            t1 *= t1;
//            n1 = t1 * t1 * dot(grad4f[gi1], x1, y1, z1, w1);
//        }
//        double t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2,  n2;
//        if (t2 < 0) {
//            n2 = 0.0;
//        } else {
//            t2 *= t2;
//            n2 = t2 * t2 * dot(grad4f[gi2], x2, y2, z2, w2);
//        }
//        double t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3, n3;
//        if (t3 < 0) {
//            n3 = 0.0;
//        } else {
//            t3 *= t3;
//            n3 = t3 * t3 * dot(grad4f[gi3], x3, y3, z3, w3);
//        }
//        double t4 = 0.6 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4, n4;
//        if (t4 < 0) {
//            n4 = 0.0;
//        } else {
//            t4 *= t4;
//            n4 = t4 * t4 * dot(grad4f[gi4], x4, y4, z4, w4);
//        }
//        // Sum up and scale the result to cover the range [-1,1]
//        return 17.0 * (n0 + n1 + n2 + n3 + n4);
//    }
// The 6 permutation tables were created with the code in the commented static block after these definitions.
public static final int[]
        perm_x = {59, 146, 27, 99, 226, 210, 44, 129, 102, 237, 2, 107, 157, 173, 159, 16, 128, 41, 228, 114, 63, 105, 241, 144, 187, 116, 223, 122, 234, 52, 96, 35, 213, 176, 177, 141, 132, 240, 194, 163, 0, 3, 168, 133, 55, 203, 53, 50, 42, 79, 130, 156, 209, 135, 151, 178, 85, 154, 117, 148, 140, 82, 6, 69, 127, 214, 95, 175, 46, 30, 104, 197, 170, 33, 70, 167, 217, 233, 219, 84, 196, 109, 40, 190, 123, 165, 61, 212, 255, 184, 19, 182, 38, 112, 172, 103, 25, 244, 245, 201, 192, 60, 14, 231, 68, 71, 236, 193, 115, 7, 113, 118, 110, 131, 198, 216, 29, 195, 211, 246, 153, 222, 185, 208, 200, 158, 66, 137, 179, 26, 147, 235, 106, 90, 164, 9, 238, 101, 138, 227, 21, 37, 23, 152, 8, 161, 108, 250, 183, 225, 121, 24, 51, 252, 87, 242, 98, 188, 232, 171, 93, 56, 57, 5, 12, 120, 74, 43, 136, 139, 32, 13, 191, 67, 189, 186, 162, 199, 10, 20, 89, 15, 31, 58, 221, 18, 253, 28, 4, 218, 142, 205, 247, 94, 215, 39, 166, 150, 224, 77, 34, 169, 206, 47, 81, 97, 83, 220, 76, 229, 160, 54, 243, 45, 181, 92, 119, 48, 155, 62, 174, 248, 36, 239, 145, 124, 125, 65, 72, 180, 134, 111, 204, 207, 100, 73, 251, 143, 249, 254, 230, 11, 78, 80, 149, 75, 91, 126, 17, 86, 49, 88, 64, 22, 202, 1},
        perm_y = {189, 111, 17, 214, 57, 208, 191, 225, 241, 152, 145, 71, 2, 141, 183, 218, 66, 178, 34, 161, 198, 47, 200, 180, 134, 239, 162, 18, 155, 216, 192, 173, 219, 9, 51, 124, 95, 122, 217, 135, 31, 50, 179, 237, 32, 39, 209, 112, 96, 92, 68, 79, 228, 193, 234, 90, 164, 137, 196, 184, 185, 114, 226, 67, 249, 163, 85, 26, 125, 28, 251, 45, 61, 220, 213, 139, 70, 201, 243, 22, 142, 246, 102, 229, 10, 107, 4, 240, 194, 35, 230, 86, 223, 20, 12, 233, 23, 77, 119, 176, 147, 182, 21, 195, 91, 118, 247, 33, 100, 99, 29, 188, 172, 144, 136, 131, 40, 13, 38, 150, 224, 205, 8, 252, 253, 190, 46, 143, 53, 231, 153, 94, 177, 88, 55, 105, 121, 16, 25, 207, 138, 5, 63, 82, 202, 58, 170, 41, 78, 167, 64, 60, 14, 103, 42, 154, 19, 80, 72, 37, 83, 129, 187, 244, 215, 242, 81, 15, 151, 186, 59, 101, 168, 175, 89, 248, 232, 212, 204, 199, 108, 73, 98, 210, 44, 1, 76, 48, 49, 250, 106, 203, 113, 43, 221, 146, 245, 148, 115, 165, 181, 84, 93, 3, 206, 65, 123, 158, 6, 126, 238, 109, 130, 227, 140, 120, 74, 171, 110, 222, 87, 156, 132, 97, 159, 197, 255, 56, 27, 62, 157, 75, 211, 254, 127, 169, 236, 235, 149, 52, 36, 24, 0, 11, 160, 133, 174, 30, 104, 69, 128, 116, 117, 54, 166, 7},
        perm_z = {253, 212, 4, 237, 36, 182, 213, 233, 147, 239, 226, 41, 74, 65, 68, 165, 70, 231, 217, 116, 113, 193, 162, 112, 228, 254, 183, 176, 151, 80, 17, 60, 155, 246, 174, 3, 202, 208, 127, 7, 57, 1, 132, 79, 224, 99, 238, 195, 236, 9, 115, 154, 23, 227, 76, 158, 130, 16, 89, 214, 61, 114, 187, 90, 49, 24, 64, 33, 96, 242, 25, 37, 215, 35, 46, 109, 134, 141, 136, 225, 138, 43, 21, 184, 189, 13, 230, 188, 40, 50, 243, 244, 211, 156, 85, 120, 223, 58, 234, 71, 6, 28, 179, 67, 125, 69, 192, 131, 44, 175, 34, 15, 32, 77, 191, 222, 83, 47, 128, 218, 198, 84, 149, 26, 121, 190, 255, 150, 117, 92, 140, 101, 172, 62, 93, 97, 27, 103, 106, 161, 194, 201, 204, 45, 206, 111, 81, 252, 249, 73, 42, 248, 108, 118, 63, 56, 31, 216, 153, 180, 19, 126, 38, 139, 66, 88, 247, 143, 177, 137, 12, 199, 104, 235, 102, 75, 100, 129, 251, 18, 159, 107, 196, 22, 10, 152, 209, 94, 181, 250, 51, 210, 185, 144, 200, 169, 232, 122, 145, 173, 95, 171, 166, 229, 14, 5, 0, 105, 2, 163, 157, 48, 53, 133, 110, 52, 160, 186, 123, 124, 91, 20, 221, 240, 87, 178, 98, 207, 142, 148, 59, 203, 245, 205, 72, 11, 164, 39, 170, 135, 168, 197, 55, 86, 219, 167, 8, 82, 78, 220, 29, 146, 241, 54, 119, 30},
        perm_w = {57, 1, 140, 48, 61, 156, 230, 173, 2, 231, 12, 214, 142, 242, 255, 195, 198, 220, 157, 139, 194, 99, 247, 248, 155, 178, 29, 41, 23, 193, 0, 30, 95, 171, 174, 222, 91, 54, 8, 67, 32, 129, 46, 124, 172, 148, 17, 105, 228, 118, 191, 33, 224, 5, 25, 158, 185, 92, 63, 199, 53, 107, 34, 180, 125, 69, 200, 116, 121, 216, 42, 233, 70, 43, 72, 26, 202, 62, 51, 15, 10, 16, 217, 207, 14, 175, 59, 52, 223, 246, 89, 109, 83, 13, 68, 90, 147, 239, 234, 18, 151, 114, 76, 143, 100, 197, 106, 176, 232, 208, 85, 165, 40, 186, 251, 101, 44, 65, 93, 218, 253, 144, 123, 11, 113, 167, 102, 240, 177, 137, 4, 184, 181, 20, 110, 37, 138, 111, 132, 94, 6, 122, 119, 75, 78, 84, 21, 3, 74, 235, 127, 112, 19, 58, 149, 161, 159, 73, 136, 150, 215, 35, 38, 86, 211, 190, 128, 203, 168, 9, 166, 244, 36, 28, 153, 225, 108, 254, 55, 169, 104, 141, 145, 22, 49, 212, 183, 79, 189, 227, 170, 60, 245, 205, 64, 252, 241, 80, 162, 97, 206, 163, 192, 146, 66, 182, 187, 135, 130, 152, 81, 71, 134, 39, 179, 188, 87, 126, 209, 229, 219, 133, 204, 210, 27, 103, 98, 154, 31, 250, 196, 7, 236, 77, 226, 56, 96, 88, 221, 45, 160, 50, 115, 237, 164, 201, 213, 82, 117, 24, 243, 131, 249, 47, 238, 120},
        perm_u = {132, 148, 19, 244, 162, 163, 194, 37, 4, 250, 198, 154, 170, 137, 6, 60, 123, 73, 138, 41, 145, 92, 61, 82, 251, 175, 57, 207, 153, 50, 113, 105, 106, 242, 253, 94, 128, 9, 164, 143, 234, 80, 160, 252, 136, 239, 232, 150, 89, 167, 100, 131, 127, 178, 31, 188, 217, 5, 27, 33, 119, 152, 83, 195, 72, 88, 223, 176, 110, 111, 134, 233, 200, 190, 130, 86, 102, 69, 202, 240, 63, 13, 70, 229, 93, 24, 241, 22, 191, 99, 245, 139, 85, 254, 53, 45, 46, 182, 185, 26, 76, 197, 104, 67, 174, 20, 108, 184, 68, 171, 172, 90, 29, 107, 32, 79, 59, 126, 211, 112, 157, 201, 215, 237, 51, 84, 23, 135, 12, 28, 155, 124, 42, 43, 74, 173, 140, 114, 78, 18, 34, 1, 142, 180, 243, 193, 2, 161, 25, 212, 181, 218, 115, 39, 177, 71, 17, 186, 249, 225, 226, 122, 117, 214, 8, 129, 44, 7, 98, 216, 40, 116, 0, 103, 96, 30, 209, 47, 236, 11, 247, 58, 151, 52, 81, 141, 147, 169, 255, 16, 219, 75, 192, 208, 87, 56, 230, 231, 14, 97, 64, 54, 10, 222, 238, 205, 66, 120, 183, 133, 206, 109, 213, 144, 121, 158, 55, 235, 125, 3, 221, 118, 189, 165, 166, 62, 49, 146, 196, 77, 224, 203, 38, 156, 228, 48, 204, 35, 36, 210, 149, 227, 168, 199, 179, 246, 91, 248, 21, 65, 95, 101, 187, 220, 159, 15},
        perm_v = {2, 9, 4, 237, 219, 73, 247, 203, 228, 220, 46, 229, 61, 156, 170, 75, 223, 144, 81, 252, 172, 208, 76, 218, 177, 103, 123, 244, 14, 39, 255, 90, 168, 43, 174, 3, 113, 107, 145, 233, 130, 254, 192, 11, 211, 190, 68, 105, 117, 178, 251, 18, 66, 242, 230, 248, 95, 137, 29, 26, 164, 65, 153, 120, 70, 77, 64, 33, 23, 133, 59, 7, 40, 16, 106, 41, 121, 216, 238, 135, 19, 212, 157, 48, 232, 28, 128, 22, 245, 171, 183, 56, 74, 99, 51, 150, 236, 111, 234, 71, 189, 167, 213, 37, 198, 50, 12, 79, 31, 250, 136, 165, 185, 246, 55, 86, 142, 62, 42, 52, 147, 205, 89, 94, 224, 141, 221, 180, 138, 129, 140, 101, 83, 193, 127, 67, 108, 84, 166, 109, 181, 20, 34, 195, 87, 24, 217, 116, 36, 88, 196, 82, 57, 239, 243, 124, 134, 175, 119, 210, 32, 163, 38, 139, 249, 227, 25, 97, 10, 118, 72, 131, 91, 54, 204, 225, 253, 58, 115, 154, 202, 122, 110, 112, 215, 1, 149, 146, 44, 201, 17, 240, 206, 197, 200, 169, 159, 13, 179, 143, 160, 152, 226, 161, 241, 80, 102, 15, 155, 92, 21, 184, 96, 148, 8, 158, 125, 35, 63, 176, 194, 235, 187, 30, 100, 231, 98, 207, 53, 47, 93, 173, 78, 186, 132, 199, 151, 114, 0, 45, 49, 126, 191, 222, 6, 182, 162, 188, 27, 69, 209, 214, 104, 5, 85, 60};

//    static {
//        int s = 1;
//        for(int i = 0; i < 256; i++) {
//            System.out.print(((s = s * 53 + 3 & 255) ^ s >>> 4) + ", ");
//        }
//        System.out.println();
//        s = 7;
//        for(int i = 0; i < 256; i++) {
//            System.out.print(((s = s * 61 + 11 & 255) ^ s >>> 4) + ", ");
//        }
//        System.out.println();
//        s = 31;
//        for(int i = 0; i < 256; i++) {
//            System.out.print(((s = s * 29 + 111 & 255) ^ s >>> 4) + ", ");
//        }
//        System.out.println();
//        s = 127;
//        for(int i = 0; i < 256; i++) {
//            System.out.print(((s = s * 101 + 31 & 255) ^ s >>> 4) + ", ");
//        }
//        System.out.println();
//        s = 15;
//        for(int i = 0; i < 256; i++) {
//            System.out.print(((s = s * 37 + 97 & 255) ^ s >>> 4) + ", ");
//        }
//        System.out.println();
//        s = 63;
//        for(int i = 0; i < 256; i++) {
//            System.out.print(((s = s * 109 + 47 & 255) ^ s >>> 4) + ", ");
//        }
//
//    }

    /**
     * 2D simplex noise returning a float; extremely similar to {@link #noise(double, double)}, but this may be slightly
     * faster or slightly slower. This uses its parameters verbatim, so you should apply frequency changes yourself.
     * This also cannot take a seed, while {@link #noise(double, double, long)} can.
     * @param x x input
     * @param y y input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static float noiseAlt(double x, double y) {
        //xin *= SCALE;
        //yin *= SCALE;
        float noise0, noise1, noise2; // from the three corners
        float xin = (float)x, yin = (float)y;
        // Skew the input space to figure out which simplex cell we're in
        float skew = (xin + yin) * FastNoise.F2f; // Hairy factor for 2D
        int i = fastFloor(xin + skew);
        int j = fastFloor(yin + skew);
        float t = (i + j) * FastNoise.G2f;
        float X0 = i - t; // Unskew the cell origin back to (x,y) space
        float Y0 = j - t;
        float x0 = xin - X0; // The x,y distances from the cell origin
        float y0 = yin - Y0;
        // For the 2D case, the simplex shape is an equilateral triangle.
        // determine which simplex we are in.
        int i1, j1; // Offsets for second (middle) corner of simplex in (i,j)
        // coords
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } // lower triangle, XY order: (0,0)->(1,0)->(1,1)
        else {
            i1 = 0;
            j1 = 1;
        } // upper triangle, YX order: (0,0)->(0,1)->(1,1)
        // A step of (1,0) in (i,j) means a step of (1-c,-c) in (x,y), and
        // a step of (0,1) in (i,j) means a step of (-c,1-c) in (x,y),
        // where: c = (3-sqrt(3))/6
        float x1 = x0 - i1 + FastNoise.G2f; // Offsets for middle corner in (x,y)
        // unskewed coords
        float y1 = y0 - j1 + FastNoise.G2f;
        float x2 = x0 - 1f + FastNoise.H2f; // Offsets for last corner in (x,y)
        // unskewed coords
        float y2 = y0 - 1f + FastNoise.H2f;
        // Work out the hashed gradient indices of the three simplex corners
//        int gi0 = determine256(i + determine(j));
//        int gi1 = determine256(i + i1 + determine(j + j1));
//        int gi2 = determine256(i + 1 + determine(j + 1));
        final int gi0 = perm_x[i & 255] ^ perm_y[j & 255];
        final int gi1 = perm_x[i + i1 & 255] ^ perm_y[j + j1 & 255];
        final int gi2 = perm_x[i + 1 & 255] ^ perm_y[j + 1 & 255];

        // Calculate the contribution from the three corners
        float t0 = 0.75f - x0 * x0 - y0 * y0;
        if (t0 < 0) {
            noise0 = 0.0f;
        } else {
            t0 *= t0;
            noise0 = t0 * t0 * FastNoise.dotf(FastNoise.phiGrad2f[gi0], x0, y0);
            // for 2D gradient
        }
        float t1 = 0.75f - x1 * x1 - y1 * y1;
        if (t1 < 0) {
            noise1 = 0.0f;
        } else {
            t1 *= t1;
            noise1 = t1 * t1 * FastNoise.dotf(FastNoise.phiGrad2f[gi1], x1, y1);
        }
        float t2 = 0.75f - x2 * x2 - y2 * y2;
        if (t2 < 0) {
            noise2 = 0.0f;
        } else {
            t2 *= t2;
            noise2 = t2 * t2 * FastNoise.dotf(FastNoise.phiGrad2f[gi2], x2, y2);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is clamped to return values in the interval [-1,1].
        return Math.max(-1f, Math.min(1f, 9.125f * (noise0 + noise1 + noise2)));
    }

    /**
     * 3D simplex noise returning a float; extremely similar to {@link #noise(double, double, double)}, but this may
     * be slightly faster or slightly slower. This uses its parameters verbatim, so you should apply frequency changes
     * yourself. This also cannot take a seed, while {@link #noise(double, double, double, long)} can.
     *
     * @param x X input
     * @param y Y input
     * @param z Z input
     * @return noise from -1.0 to 1.0, inclusive
     */
    public static float noiseAlt(double x, double y, double z) {
        //xin *= SCALE;
        //yin *= SCALE;
        //zin *= SCALE;
        float xin = (float)x, yin = (float)y, zin = (float)z;
        float n0, n1, n2, n3; // Noise contributions from the four corners
        // Skew the input space to figure out which simplex cell we're in
        float s = (xin + yin + zin) * FastNoise.F3f; // Very nice and simple skew
        // factor for 3D
        int i = fastFloor(xin + s);
        int j = fastFloor(yin + s);
        int k = fastFloor(zin + s);
        float t = (i + j + k) * FastNoise.G3f;
        float X0 = i - t; // Unskew the cell origin back to (x,y,z) space
        float Y0 = j - t;
        float Z0 = k - t;
        float x0 = xin - X0; // The x,y,z distances from the cell origin
        float y0 = yin - Y0;
        float z0 = zin - Z0;
        // For the 3D case, the simplex shape is a slightly irregular
        // tetrahedron.
        // determine which simplex we are in.
        int i1, j1, k1; // Offsets for second corner of simplex in (i,j,k)
        // coords
        int i2, j2, k2; // Offsets for third corner of simplex in (i,j,k)
        // coords
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } // X Y Z order
            else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } // X Z Y order
            else {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } // Z X Y order
        } else { // x0<y0
            if (y0 < z0) {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } // Z Y X order
            else if (x0 < z0) {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 0;
                j2 = 1;
                k2 = 1;
            } // Y Z X order
            else {
                i1 = 0;
                j1 = 1;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } // Y X Z order
        }
        // A step of (1,0,0) in (i,j,k) means a step of (1-c,-c,-c) in
        // (x,y,z),
        // a step of (0,1,0) in (i,j,k) means a step of (-c,1-c,-c) in
        // (x,y,z), and
        // a step of (0,0,1) in (i,j,k) means a step of (-c,-c,1-c) in
        // (x,y,z), where
        // c = 1/6.
        float x1 = x0 - i1 + FastNoise.G3f; // Offsets for second corner in (x,y,z)
        // coords
        float y1 = y0 - j1 + FastNoise.G3f;
        float z1 = z0 - k1 + FastNoise.G3f;
        float x2 = x0 - i2 + FastNoise.F3f; // Offsets for third corner in
        // (x,y,z) coords
        float y2 = y0 - j2 + FastNoise.F3f;
        float z2 = z0 - k2 + FastNoise.F3f;
        float x3 = x0 - 0.5f; // Offsets for last corner in
        // (x,y,z) coords
        float y3 = y0 - 0.5f;
        float z3 = z0 - 0.5f;
        // Work out the hashed gradient indices of the four simplex corners

        /*
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;

        int gi0 = perm[ii + perm[jj + perm[kk]]] % 12;
        int gi1 = perm[ii + i1 + perm[jj + j1 + perm[kk + k1]]] % 12;
        int gi2 = perm[ii + i2 + perm[jj + j2 + perm[kk + k2]]] % 12;
        int gi3 = perm[ii + 1 + perm[jj + 1 + perm[kk + 1]]] % 12;
        */
//        int gi0 = determine32(i + determine(j + determine(k)));
//        int gi1 = determine32(i + i1 + determine(j + j1 + determine(k + k1)));
//        int gi2 = determine32(i + i2 + determine(j + j2 + determine(k + k2)));
//        int gi3 = determine32(i + 1 + determine(j + 1 + determine(k + 1)));
        final int gi0 = (perm_x[(i) & 255] ^ perm_y[(j) & 255]           ^ perm_z[(k) + 67 & 255]) & 31;
        final int gi1 = (perm_x[(i + i1) & 255] ^ perm_y[(j + j1) & 255] ^ perm_z[(k + k1) + 67 & 255]) & 31;
        final int gi2 = (perm_x[(i + i2) & 255] ^ perm_y[(j + j2) & 255] ^ perm_z[(k + k2) + 67 & 255]) & 31;
        final int gi3 = (perm_x[(i + 1) & 255] ^ perm_y[(j + 1) & 255]   ^ perm_z[(k + 1) + 67  & 255]) & 31;


//        int gi0 = determineBounded(i + determine(j + determine(k)), 92);
//        int gi1 = determineBounded(i + i1 + determine(j + j1 + determine(k + k1)), 92);
//        int gi2 = determineBounded(i + i2 + determine(j + j2 + determine(k + k2)), 92);
//        int gi3 = determineBounded(i + 1 + determine(j + 1 + determine(k + 1)), 92);

        /*
        int hash = (int) rawNoise(i + ((j + k * 0x632BE5AB) * 0x9E3779B9),
                i + i1 + ((j + j1 + (k + k1) * 0x632BE5AB) * 0x9E3779B9),
                i + i2 + ((j + j2 + (k + k2) * 0x632BE5AB) * 0x9E3779B9),
                i + 1 + ((j + 1 + ((k + 1) * 0x632BE5AB)) * 0x9E3779B9),
                seed);
        int gi0 = (hash >>>= 4) % 12;
        int gi1 = (hash >>>= 4) % 12;
        int gi2 = (hash >>>= 4) % 12;
        int gi3 = (hash >>> 4) % 12;
        */

        //int hash = (int) rawNoise(i, j, k, seed);
        //int gi0 = (hash >>>= 4) % 12, gi1 = (hash >>>= 4) % 12, gi2 = (hash >>>= 4) % 12, gi3 = (hash >>>= 4) % 12;
        // Calculate the contribution from the four corners
        float t0 = 0.6f - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 < 0) {
            n0 = 0f;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dotf(grad3f[gi0], x0, y0, z0);
        }
        float t1 = 0.6f - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 < 0) {
            n1 = 0f;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dotf(grad3f[gi1], x1, y1, z1);
        }
        float t2 = 0.6f - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 < 0) {
            n2 = 0f;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dotf(grad3f[gi2], x2, y2, z2);
        }
        float t3 = 0.6f - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 < 0) {
            n3 = 0f;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * dotf(grad3f[gi3], x3, y3, z3);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is clamped to stay just inside [-1,1]
        return Math.max(-1f, Math.min(1f, 31.5f * (n0 + n1 + n2 + n3)));
    }

}
