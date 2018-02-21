package squidpony.squidmath;

/**
 * A technique for producing organic-seeming patterns via iterative processing of random values (reaction-diffusion).
 * The fundamental basis for why this looks organic should be credited to Alan Turing, but the specific algorithm should
 * be credited to <a href="http://www.jonathanmccabe.com/Cyclic_Symmetric_Multi-Scale_Turing_Patterns.pdf">Jonathan
 * McCabe</a>, who has produced significant expansions on the scope the original Turing work covered; you can see
 * <a href=https://www.flickr.com/photos/jonathanmccabe/sets>various examples of art he produced using this and related
 * techniques</a>.
 * <br>
 * This class tries to provide a fairly raw API so different adjustments can be made on top of it.
 * <br>
 * Created by Tommy Ettinger on 4/27/2017.
 */
public class TuringPattern {
    private static final double fraction = 0x1p-53;

    /**
     * Initializes a substance array that can be given to other static methods. Uses very little 64-bit math,
     * making it ideal for GWT.
     * @param width the width of the substance array; should be consistent throughout calls
     * @param height the height of the substance array; should be consistent throughout calls
     * @return a 1D double array that represents a 2D array with random contents; should be given to other methods
     */
    public static double[] initialize(int width, int height){
        return initializeInto(new double[width * height]);
    }

    /**
     * Refills a substance array that can be given to other static methods. Uses very little 64-bit math,
     * making it ideal for GWT.
     * @param substance a 1D double array that will be modified and filled with random values
     * @return substance, after modification; should be given to other methods
     */
    public static double[] initializeInto(double[] substance){
        if(substance == null) return null;
        long seed = 65537;
        for (int i = 0; i < substance.length; i++) {
            substance[i] = (ThrustAltRNG.determine(++seed) >> 11) * fraction;
        }
        return substance;
    }
    /**
     * Initializes a substance array that can be given to other static methods.
     * @param width the width of the substance array; should be consistent throughout calls
     * @param height the height of the substance array; should be consistent throughout calls
     * @param seed the int seed to use for the random contents
     * @return a 1D double array that represents a 2D array with random contents; should be given to other methods
     */
    public static double[] initialize(int width, int height, int seed){
        return initializeInto(new double[width * height], seed);
    }

    /**
     * Refills a substance array that can be given to other static methods.
     * @param substance a 1D double array that will be modified and filled with random values
     * @param seed the int seed to use for the random contents
     * @return substance, after modification; should be given to other methods
     */
    public static double[] initializeInto(double[] substance, int seed){
        if(substance == null) return null;
        for (int i = 0; i < substance.length; i++) {
            substance[i] = (ThrustAltRNG.determine(++seed) >> 11) * fraction;
        }
        return substance;
    }

    /**
     * Initializes a substance array that can be given to other static methods. Uses an RNG to produce long values that
     * this turns into doubles in the desired range (that is, -1.0 to 1.0).
     * @param width the width of the substance array; should be consistent throughout calls
     * @param height the height of the substance array; should be consistent throughout calls
     * @param rng the random number generator responsible for producing random double values
     * @return a 1D double array that represents a 2D array with random contents; should be given to other methods
     */
    public static double[] initialize(int width, int height, RNG rng) {
        return initializeInto(new double[width * height], rng);
    }
    /**
     * Initializes a substance array that can be given to other static methods. Uses an RNG to produce long values that
     * this turns into doubles in the desired range (that is, -1.0 to 1.0).
     * @param substance a 1D double array that will be modified and filled with random values
     * @param rng the random number generator responsible for producing random double values
     * @return a 1D double array that represents a 2D array with random contents; should be given to other methods
     */
    public static double[] initializeInto(double[] substance, RNG rng){
        if(substance == null || rng == null) return null;
        for (int i = 0; i < substance.length; i++) {
            substance[i] = rng.nextDouble(2.0) - 1.0;
        }
        return substance;
    }

    /**
     * Initializes a substance array that can be given to other static methods. Uses a Noise2D instance (it may or may
     * not use the given seed) to produce values between -1.0 and 1.0.
     * @param width the width of the substance array; should be consistent throughout calls
     * @param height the height of the substance array; should be consistent throughout calls
     * @param noise a Noise.Noise2D instance, such as {@link SeededNoise#instance}
     * @param seed the seed to use with the noise generator
     * @return a 1D double array that represents a 2D array with random contents; should be given to other methods
     */
    public static double[] initialize(int width, int height, Noise.Noise2D noise, int seed) {
        return initializeInto(new double[width * height], width, height, noise, seed);
    }
    /**
     * Initializes a substance array that can be given to other static methods. Uses a Noise2D instance (it may or may
     * not use the given seed) to produce values between -1.0 and 1.0.
     * @param width the width of the substance array; should be consistent throughout calls
     * @param height the height of the substance array; should be consistent throughout calls
     * @param noise a Noise.Noise2D instance, such as {@link SeededNoise#instance}
     * @param seed the seed to use with the noise generator
     * @return a 1D double array that represents a 2D array with random contents; should be given to other methods
     */
    public static double[] initializeInto(double[] substance, int width, int height, Noise.Noise2D noise, int seed){
        if(substance == null || noise == null) return null;
        int i = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                substance[i++] = noise.getNoiseWithSeed(x, y, seed);
            }
        }
        return substance;
    }

    /**
     * Modifies the data parameter so no value in it is outside the range -1.0 inclusive to 1.0 exclusive. Makes no
     * guarantees about the values this puts in data beyond that they will be inside that range. Has undefined results
     * if any values in data are NaN or are infinite, though it probably will still work. If all values in data are
     * already in the range, this will still change them, though probably not beyond recognition. Does not return a
     * value because the changes are applied to data in-place.
     * @param data a double array that will be modified in-place so all values in it will be between -1.0 and 1.0
     */
    public static void refit(final double[] data)
    {
        if(data == null) return;
        for (int i = 0; i < data.length; i++) {
            data[i] = NumberTools.bounce(data[i] + 5);
        }
    }

    /**
     * Given an offset information array that has been modified and should be returned to its unmodified state, this
     * uses the given width, height, and radius (which should be the same as what this was originally constructed with)
     * to modify offsets in-place as if it was freshly-made, even if the array is final.
     * @param offsets an offset information array that should have been produced by {@link #offsetsCircle(int, int, double)} and may have been distorted
     * @param width the width of the substance array; should be consistent throughout calls
     * @param height the height of the substance array; should be consistent throughout calls
     * @param radius the radius of the circle to
     * @return an offset information array, as a jagged 2D int array, that can be passed to {@link #step(double[], int[][], double, int[][], double)}
     */
    public static int[][] offsetsCircleInto(final int[][] offsets, final int width, final int height, double radius) {
        if (radius > width || radius > height) radius = Math.min(width, height);
        radius = Math.max(1, radius);
        IntVLA ivx = new IntVLA((int) (Math.PI * radius * (radius + 1))),
                ivy = new IntVLA((int) (Math.PI * radius * (radius + 1)));
        for (int x = 1; x <= radius; x++) {
            for (int y = 1; y <= radius; y++) {
                if (Math.sqrt(x * x + y * y) + 0.5 <= radius) {
                    ivx.add(x);
                    ivy.add(y);
                }
            }
        }
        int pointSize = (ivx.size + (int) radius - 1 << 2) + 1;
        int[] bx = new int[pointSize], by = new int[pointSize];
        for (int i = 0; i < ivx.size; i++) {
            int x = ivx.get(i), y = ivy.get(i);
            bx[i << 2] = x;
            by[i << 2] = y;
            bx[i << 2 | 1] = -x;
            by[i << 2 | 1] = y;
            bx[i << 2 | 2] = -x;
            by[i << 2 | 2] = -y;
            bx[i << 2 | 3] = x;
            by[i << 2 | 3] = -y;
        }
        for (int i = (ivx.size + 1 << 2), p = 1; i < pointSize - 3 && p <= radius + 0.5; i += 4, p++) {
            bx[i] = p;
            by[i] = 0;
            bx[i | 1] = 0;
            by[i | 1] = p;
            bx[i | 2] = -p;
            by[i | 2] = 0;
            bx[i | 3] = 0;
            by[i | 3] = -p;
        }
        bx[pointSize - 1] = 0;
        by[pointSize - 1] = 0;
        int[] vp;
        int bxp, byp;
        for (int p = 0; p < pointSize; p++) {
            vp = offsets[p];
            bxp = bx[p];
            byp = by[p];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    vp[x * height + y] = ((x + bxp + width) % width) * height + ((y + byp + height) % height);
                }
            }
        }
        return offsets;
    }

    /**
     * Pre-calculates the indices into a substance array that will need to be averaged per point in that array for an
     * activator or inhibitor. The radius should usually (by convention) be smaller for an activator and larger for an
     * inhibitor, but very large radii cause this to require significantly more time; consider a maximum of about 20, or
     * less depending on how fast it needs to run vs. quality, and how many generations you expect.
     * @param width the width of the substance array; should be consistent throughout calls
     * @param height the height of the substance array; should be consistent throughout calls
     * @param radius the radius of the circle to
     * @return an offset information array, as a jagged 2D int array, that can be passed to {@link #step(double[], int[][], double, int[][], double)}
     */
    public static int[][] offsetsCircle(int width, int height, double radius)
    {
        if(radius > width || radius > height) radius = Math.min(width, height);
        radius = Math.max(1, radius);
        IntVLA ivx = new IntVLA((int)(Math.PI * radius * (radius+1))),
               ivy = new IntVLA((int)(Math.PI * radius * (radius+1)));
        for (int x = 1; x <= radius; x++) {
            for (int y = 1; y <= radius; y++) {
                if(Math.sqrt(x * x + y * y) + 0.5 <= radius)
                {
                    ivx.add(x);
                    ivy.add(y);
                }
            }
        }
        int pointSize = (ivx.size + (int)radius - 1 << 2) + 1;
        int[] bx = new int[pointSize], by = new int[pointSize];
        for (int i = 0; i < ivx.size; i++) {
            int x = ivx.get(i), y = ivy.get(i);
            bx[i << 2] = x;
            by[i << 2] = y;
            bx[i << 2 | 1] = -x;
            by[i << 2 | 1] = y;
            bx[i << 2 | 2] = -x;
            by[i << 2 | 2] = -y;
            bx[i << 2 | 3] = x;
            by[i << 2 | 3] = -y;
        }
        for (int i = (ivx.size + 1 << 2), p = 1; i < pointSize - 3 && p <= radius + 0.5; i+=4, p++) {
            bx[i] = p;
            by[i] = 0;
            bx[i|1] = 0;
            by[i|1] = p;
            bx[i|2] = -p;
            by[i|2] = 0;
            bx[i|3] = 0;
            by[i|3] = -p;
        }
        bx[pointSize-1] = 0;
        by[pointSize-1] = 0;
        int[][] val = new int[pointSize][width * height];
        int[] vp;
        int bxp, byp;
        for (int p = 0; p < pointSize; p++) {
            vp = val[p];
            bxp = bx[p];
            byp = by[p];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    vp[x * height + y] = ((x + bxp + width) % width) * height + ((y + byp + height) % height);
                }
            }
        }
        return val;
    }

    /**
     * Alters the given offset information (as a jagged 2D int array) with the given Noise2D instance and seed. This can
     * change the quality of the pattern produced significantly, going from stripes to whorls and fungoid shapes.
     * Modifies offsets in-place, and repeated calls can be a good way to alter the pattern this produces.
     * @param offsets a jagged 2D int array as produced by {@link #offsetsCircle(int, int, double)}; will be modified!
     * @param width the width of the full area that will be used by the TuringPattern
     * @param height the height of the full area that will be used by the TuringPattern
     * @param noise a Noise2D instance, such as {@link SeededNoise#instance}, that will be used to alter offsets
     * @param seed a seed for the Noise2D
     */
    public static void distort(int[][] offsets, int width, int height, Noise.Noise2D noise, int seed)
    {
        int pointSize = offsets.length, altSeed = ThrustAlt32RNG.determine(seed);
        int[] vp;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pushX = (int)(2.5 * (noise.getNoiseWithSeed(x, y, seed) + 1)) - 2,
                        pushY = (int)(2.5 * (noise.getNoiseWithSeed(x, y, altSeed) + 1)) - 2;
                for (int p = 0; p < pointSize; p++) {
                    vp = offsets[p];
                    int a = vp[x * height + y], px = a / height, py = a % height;
                    vp[x * height + y] = ((px + pushX + width) % width) * height + ((py + pushY + height) % height);
                }
            }
        }
    }
    /**
     * Alters the given offset information (as a jagged 2D int array) with the given Noise3D instance and seed, allowing
     * a z position for the 3D component so this can change over time with changing z. This can method change the
     * quality of the pattern produced significantly, going from stripes to whorls and fungoid shapes.
     * Modifies offsets in-place, and repeated calls can be a good way to alter the pattern this produces.
     * @param offsets a jagged 2D int array as produced by {@link #offsetsCircle(int, int, double)}; will be modified!
     * @param width the width of the full area that will be used by the TuringPattern
     * @param height the height of the full area that will be used by the TuringPattern
     * @param noise a Noise3D instance, such as {@link SeededNoise#instance}, that will be used to alter offsets
     * @param z a z position to be given to the Noise3D along with a point's x and y
     * @param seed a seed for the Noise3D
     */
    public static void distort(int[][] offsets, int width, int height, Noise.Noise3D noise, double z, int seed)
    {
        int pointSize = offsets.length, altSeed = ThrustAlt32RNG.determine(seed);
        int[] vp;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pushX = (int)(2.5 * (noise.getNoiseWithSeed(x, y, z, seed) + 1)) - 2,
                        pushY = (int)(2.5 * (noise.getNoiseWithSeed(x, y, z, altSeed) + 1)) - 2;
                for (int p = 0; p < pointSize; p++) {
                    vp = offsets[p];
                    int a = vp[x * height + y], px = a / height, py = a % height;
                    vp[x * height + y] = ((px + pushX + width) % width) * height + ((py + pushY + height) % height);
                }
            }
        }
    }

    /**
     * Brings together the other methods to advance the substance simulation by one step, modifying substance in-place.
     * You should have generated substance with either one of the initialize() methods or with {@link #refit(double[])},
     * and the activator and inhibitor should have been produced by calls to an offsets method like
     * {@link #offsetsCircle(int, int, double)}, with the same width and height passed to initialize (or if you used
     * refit, the length of the substance array should be equal to width times height). The activation and inhibition
     * parameters should be very small (larger numbers will cause more significant jumps in a simulation, but may be
     * better for single generations; neither amount should have an absolute value larger than 0.1 in general), and
     * inhibition should have the opposite sign of activation.
     * @param substance as produced by initialize; will be modified!
     * @param activator as produced by an offsets method
     * @param activation the small double amount to use when the activator is dominant; should usually be positive
     * @param inhibitor as produced by an offsets method
     * @param inhibition the small double amount to use when the inhibitor is dominant; should usually be negative
     */
    public static void step(final double[] substance, final int[][] activator,
                            final double activation, final int[][] inhibitor, final double inhibition) {
        double mn = Double.POSITIVE_INFINITY, mx = Double.NEGATIVE_INFINITY, t;

        for (int s = 0; s < substance.length; s++) {
            double activate = 0.0, inhibit = 0.0;
            for (int p = 0; p < activator.length; p++) {
                activate += substance[activator[p][s]];
            }
            for (int p = 0; p < inhibitor.length; p++) {
                inhibit += substance[inhibitor[p][s]];
            }
            t = (substance[s] += (activate > inhibit) ? activation : inhibition);
            mx = Math.max(mx, t);
            mn = Math.min(mn, t);
        }
        t = 2.0 / (mx - mn);
        for (int s = 0; s < substance.length; s++) {
            substance[s] = (substance[s] - mn) * t - 1;
        }
    }

    /**
     * Computes the first part of a step, allowing other adjustments to be mixed in before finishing by calling
     * {@link #normalize(double[])}. A sample adjustment would be
     * {@link #addNoise(double[], int, int, double, Noise.Noise2D, int)}. This is probably not very useful yet.
     * @param substance as produced by initialize; will be modified!
     * @param activator as produced by an offsets method
     * @param activation the small double amount to use when the activator is dominant; should usually be positive
     * @param inhibitor as produced by an offsets method
     * @param inhibition the small double amount to use when the inhibitor is dominant; should usually be negative
     * @see #step(double[], int[][], double, int[][], double) step, which is preferred to this method
     */
    public static void stepPartial(final double[] substance, final int[][] activator,
                            final double activation, final int[][] inhibitor, final double inhibition) {
        for (int s = 0; s < substance.length; s++) {
            double activate = 0.0, inhibit = 0.0;
            for (int p = 0; p < activator.length; p++) {
                activate += substance[activator[p][s]];
            }
            for (int p = 0; p < inhibitor.length; p++) {
                inhibit += substance[inhibitor[p][s]];
            }
            substance[s] += (activate > inhibit) ? activation : inhibition;
        }
    }

    /**
     * Simply adds the result of a noise call, multiplied by the given multiplier, to each point in substance.
     * Modifies substance in-place.
     * @param substance as produced by initialize; will be modified!
     * @param width the width of the substance array; should be consistent throughout calls
     * @param height the height of the substance array; should be consistent throughout calls
     * @param multiplier multiplied with each noise call, so noise (usually too significant) can have its effect reduced
     * @param noise a Noise2D instance, such as {@link SeededNoise#instance}
     * @param seed a seed for the Noise2D
     */
    public static void addNoise(final double[] substance, final int width, final int height,
                                final double multiplier, final Noise.Noise2D noise, final int seed)
    {
        int s = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                substance[s++] += noise.getNoiseWithSeed(x, y, seed) * multiplier;
            }
        }
    }
    /**
     * Simply adds the result of a noise call, multiplied by the given multiplier, to each point in substance.
     * Modifies substance in-place.
     * @param substance as produced by initialize; will be modified!
     * @param width the width of the substance array; should be consistent throughout calls
     * @param height the height of the substance array; should be consistent throughout calls
     * @param multiplier multiplied with each noise call, so noise (usually too significant) can have its effect reduced
     * @param noise a Noise3D instance, such as {@link SeededNoise#instance}
     * @param z a z position to be given to the Noise3D along with a point's x and y
     * @param seed a seed for the Noise3D
     */
    public static void addNoise(final double[] substance, final int width, final int height,
                                final double multiplier, final Noise.Noise3D noise, final double z, final int seed)
    {
        int s = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                substance[s++] += noise.getNoiseWithSeed(x, y, z, seed) * multiplier;
            }
        }
    }

    /**
     * Finds the highest and lowest values in the substance array and modifies the whole array so the lowest and highest
     * values are contracted or expanded to -1.0 and 1.0, respectively, and other values change commensurately.
     * @param substance a substance array, as produced by initialize, will be modified!
     */
    public static void normalize(final double[] substance)
    {
        double mn = Double.POSITIVE_INFINITY, mx = Double.NEGATIVE_INFINITY, t;
        for (int s = 0; s < substance.length; s++) {
            mx = Math.max(mx, (t = substance[s]));
            mn = Math.min(mn, t);
        }
        t = 2.0 / (mx - mn);
        for (int s = 0; s < substance.length; s++) {
            substance[s] = (substance[s] - mn) * t - 1;
        }
    }

    /**
     * Makes a new 2D double array with the given width and height, using the given substance array for contents.
     * @param width the width of the 2D array to produce; must be 1 or greater
     * @param height the height of the 2D array to produce; must be 1 or greater
     * @param substance a substance array as produced by initialize and modified by step
     * @return a new 2D double array with the contents of substance and the requested size
     */
    public static double[][] reshape(final int width, final int height, final double[] substance)
    {
        double[][] val = new double[width][height];
        int s = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                val[x][y] = substance[s++];
            }
        }
        return val;
    }

    /**
     * Modifies target in-place so it is filled with as much data as possible from substance.
     * @param target a non-null, non-empty 2D double array; will be modified!
     * @param substance a substance array as produced by initialize and modified by step
     */
    public static void fill(final double[][] target, final double[] substance)
    {
        if(target == null || substance == null || target.length == 0 || target[0].length == 0 || substance.length == 0)
            return;
        int s = 0, width = target.length, height = target[0].length, size = substance.length;
        for (int x = 0; x < width && s < size; x++) {
            for (int y = 0; y < height && s < size; y++) {
                target[x][y] = substance[s++];
            }
        }
    }
}
