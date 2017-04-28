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
    /**
     * Initializes a substance array that can be given to other static methods. Uses very little (if any) 64-bit math,
     * making it ideal for GWT.
     * @param width the width of the substance array; should be consistent throughout calls
     * @param height the height of the substance array; should be consistent throughout calls
     * @return a 1D double array that represents a 2D array with random contents; should be given to other methods
     */
    public static double[] initialize(int width, int height){
        if(width <= 0 || height <= 0) return new double[0];
        double[] substance = new double[width * height];
        final double fraction = 1.0 / 0x80000000L;
        int seed = 65537;
        for (int i = 0; i < width * height; i++) {
            seed += 0x9E3779B9;
            substance[i] = PintRNG.determine((seed ^ 0xD0E89D2D) >>> 19 | (seed ^ 0xD0E89D2D) << 13) * fraction;
        }
        return substance;
    }
    /**
     * Initializes a substance array that can be given to other static methods. Uses very little (if any) 64-bit math,
     * making it ideal for GWT.
     * @param width the width of the substance array; should be consistent throughout calls
     * @param height the height of the substance array; should be consistent throughout calls
     * @param seed the int seed to use for the random contents
     * @return a 1D double array that represents a 2D array with random contents; should be given to other methods
     */
    public static double[] initialize(int width, int height, int seed){
        if(width <= 0 || height <= 0) return new double[0];
        double[] substance = new double[width * height];
        final double fraction = 1.0 / 0x80000000L;
        for (int i = 0; i < width * height; i++) {
            seed += 0x9E3779B9;
            substance[i] = PintRNG.determine((seed ^ 0xD0E89D2D) >>> 19 | (seed ^ 0xD0E89D2D) << 13) * fraction;
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
    public static double[] initialize(int width, int height, RNG rng){
        if(width <= 0 || height <= 0 || rng == null) return new double[0];
        double[] substance = new double[width * height];
        for (int i = 0; i < width * height; i++) {
            substance[i] = rng.nextDouble(2.0) - 1.0;
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
     * Pre-calculates the indices into a substance array that will need to be averaged per point in that array for an
     * activator or inhibitor. The radius should usually (by convention) be smaller for an activator and larger for an
     * inhibitor, but very large radii cause this to require significantly more time; consider a maximum of about 20, or
     * less depending on how fast it needs to run vs. quality, and how many generations you expect.
     * @param width the width of the substance array; should be consistent throughout calls
     * @param height the height of the substance array; should be consistent throughout calls
     * @param radius the radius of the circle to
     * @return
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
        int quadSize = ivx.size + (int)radius << 2;
        int[] bx = new int[quadSize], by = new int[quadSize];
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
        for (int i = (ivx.size + 1 << 2), p = 0; i < quadSize - 3 && p <= radius + 0.5; i+=4, p++) {
            bx[i] = p;
            by[i] = 0;
            bx[i|1] = 0;
            by[i|1] = p;
            bx[i|2] = -p;
            by[i|2] = 0;
            bx[i|3] = 0;
            by[i|3] = -p;
        }
        int[][] val = new int[quadSize][width * height];
        int[] vp;
        int bxp, byp;
        for (int p = 0; p < quadSize; p++) {
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
