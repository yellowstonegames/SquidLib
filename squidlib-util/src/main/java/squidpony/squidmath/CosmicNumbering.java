package squidpony.squidmath;

import java.io.Serializable;

import static squidpony.squidmath.Noise.emphasizeSigned;
import static squidpony.squidmath.NumberTools.sway;
import static squidpony.squidmath.NumberTools.swayRandomized;

/**
 * Like a kind of RNG, but fully deterministic in a way that depends on a "connected" double array.
 * Intended as a way to produce similar values when small changes occur in the connections, while potentially producing
 * larger changes when the changes are more significant (unlike an RNG or hashing function, which can and should produce
 * very different output given even slightly different seeds/input). This might be useful to produce procedural story
 * data that is similar when most of the connected inputs are similar, or for terrain generation/population. This can
 * produce ints and doubles, and does not produce a different output unless its input is changed (usually by altering a
 * shared reference to {@code connections}). Also implements the various {@link Noise} interfaces, which this doesn't
 * do perfectly but is at least different (it may yield large spans of high or low results, which Simplex and Perlin
 * noise cannot actually do). <a href="https://i.imgur.com/BrTVlj1.gifv">Here's a short clip of this in motion</a>.
 * <br>
 * If you're using this as an arbitrary-dimensional noise algorithm, you may want to also consider {@link PhantomNoise},
 * which has output that looks more like Perlin or Simplex noise but scales to arbitrary dimensions.
 * <a href="https://i.imgur.com/rmwXwO5.gifv">Here's a short clip of PhantomNoise in motion</a>.
 * <br>
 * Created by Tommy Ettinger on 5/18/2017.
 */
public class CosmicNumbering implements Serializable, Noise.Noise2D, Noise.Noise3D, Noise.Noise4D, Noise.Noise6D {
    private static final long serialVersionUID = 0L;
    public static final CosmicNumbering instance = new CosmicNumbering(0x1337BEEFL, new double[]{1.618, 3.14});
    protected double[] connections;
    protected int len;
//    private int upper;
    protected long seed;
//    private transient long[] scratch3;
//    private transient double[] scratch;

    protected double effect;
    public CosmicNumbering() {
        this(1234567890L, new double[1]);
    }
    public CosmicNumbering(double[] connections) {
        this(1234567890L, connections);
    }

    public CosmicNumbering(long seed, double[] connections) {
        if(connections == null || connections.length == 0)
            this.connections = new double[1];
        else
            this.connections = connections;
        len = this.connections.length;
//        upper = 1 << len;
//        scratch3 = new long[len * 3];
//        scratch = new double[upper];
        this.seed = seed;
//        effect = 0x1.81p-62 * Math.pow(1.1875, len);
    }

    public double[] getConnections() {
        return connections;
    }

    public void setConnections(double[] connections) {
        if (connections == null || connections.length == 0)
            this.connections = new double[1];
        else
            this.connections = connections;
        if (len != this.connections.length) {
            len = this.connections.length;
//            upper = 1 << len;
//            scratch3 = new long[len * 3];
//            scratch = new double[upper];
//            effect = 0x1.81p-62 * Math.pow(1.1875, len);
        }
    }
//    /*
//     * Quintic-interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
//     * Will smoothly transition toward start or end as a approaches 0 or 1, respectively.
//     * @param start a valid float
//     * @param end a valid float
//     * @param a a float between 0 and 1 inclusive
//     * @return a float between x and y inclusive
//     */
//    public static double querp(final double start, final double end, double a){
//        return (1.0 - (a *= a * a * (a * (a * 6.0 - 15.0) + 10.0))) * start + a * end;
//    }
//    /*
//     * Linearly interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
//     * @param start a valid float
//     * @param end a valid float
//     * @param a a float between 0 and 1 inclusive
//     * @return a float between x and y inclusive
//     */
//    public static double interpolate(final double start, final double end, final double a)
//    {
//        return (1.0 - a) * start + a * end;
//    }

//    public double getDoubleBase()
//    {
//        double[] connections = this.connections;
//        final int len = connections.length;
//        long floor, seed = 1234567;
//        double diff, conn, result = 0.0;
//        for (int i = 0; i < len; i++) {
//            diff = (conn = connections[i]) - (floor = longFloor(conn));
//            seed += 10000;
//            result += querp(
//                    NumberTools.formCurvedFloat(NumberTools.splitMix64(floor * seed + 100 * (i + 1))),
//                    NumberTools.formCurvedFloat(NumberTools.splitMix64((floor + 1L) * seed + 100 * (i + 1))),
//                    diff
//            );
//        }
//        return NumberTools.bounce(5.0 + 2.4 * result);
//    }

    /**
     * Gets a double determined by the current values in the connections, accessible via {@link #getConnections()}.
     * Returns a value between -1.0 and 1.0 (exclusive on 1.0). Used as the basis for other methods in this class.
     * @return a double between -1.0 and 1.0; will be the same value until/unless connections change
     */
    public final double getDoubleBase() {
        //return (getDouble() - 0.5) * 2.0;
        double sum = swayRandomized(seed, connections[len - 1] + connections[0]);
        for (int i = 1; i < len; i++) {
            sum += swayRandomized(seed, sum + connections[i - 1] + connections[i]);
        }
        return sum / len;
    }

//    {
//        double[] connections = this.connections;
//        final int len = connections.length;
//        long floor;
//        double diff, conn, result = 0.0;//, total = 1.0;
//        for (int i = 0; i < len; i++) {
//            diff = (conn = connections[i]) - (floor = longFloor(conn));
//            //  & 0xfffffffffffffL
//            result +=
//                    NumberTools.bounce((NumberTools.longBitsToDouble((floor * 0x9E3779B97F4A7C15L >>> 12) | 0x4000000000000000L) - 3.0)
//                            * (1.0 - diff)
//                            + (NumberTools.longBitsToDouble(((floor + 1L) * 0x9E3779B97F4A7C15L >>> 12) | 0x4000000000000000L) - 3.0)
//                            * diff
//                            + 5 + ~i * 0.618);
//        }
//        return result / len;
//    }

    /**
     * Gets a double determined by the current values in the connections, accessible via {@link #getConnections()}.
     * Returns a value between 0.0 and 1.0 (exclusive on 1.0).
     * @return a double between 0.0 and 1.0; will be the same value until/unless connections change
     */
    public double getDouble()
    {
//        for (int i = 0; i < len; i++) {
//            long seed = seeds[i];
//            scratch3[i * 3 + 1] = (scratch3[i * 3] = (scratch3[i * 3 + 2] = longFloor(connections[i])) * seed) + seed;
//        }
//        long working;
//        for (int i = 0; i < upper; i++) {
//            working = 0L;
//            for (int j = 0; j < len; j++) {
//                working += scratch3[j * 3 + (i >>> j & 1)];
//            }
//            scratch[i] = determine(working) * effect;
//        }
//        for (int i = 0; i < len; ++i) {
//            for (int j = 0, t = upper >> i; j < t; j += 2) {
//                scratch[j >>> 1] = cerp(scratch[j], scratch[j + 1], connections[i] - scratch3[i * 3 + 2]);
//            }
//        }
//        return scratch[0] - longFloor(scratch[0]);
//// has a different look than the above line
////        return NumberTools.sway(scratch[0]);
        double sum = swayRandomized(seed, connections[len - 1] + connections[0]);
        for (int i = 1; i < len; i++) {
            sum += swayRandomized(seed, sum + connections[i - 1] + connections[i]);
        }
        return sum / (len << 1) + 0.5;

    }

//    public double getDouble()
//    {
//        double v = 0.0, diff;
//        double[] connections = this.connections;
//        final int len = connections.length;
//        long floor;
//        for (int i = 0; i < len; i++) {
//            diff = connections[i] - (floor = longFloor(connections[i]));
//            v += randomDouble(floor) * (1.0 - diff) + randomDouble(floor + 1L) * diff;
//        }
//        return v / len;
//    }
    /**
     * Gets an int determined by the current values in the connections, accessible via {@link #getConnections()}.
     * Returns a value in the full range of ints, but is less likely to produce ints close to {@link Integer#MAX_VALUE}
     * or {@link Integer#MIN_VALUE} (expect very few values in the bottom and top quarters of the range).
     * @return an int which can be positive or negative; will be the same value until/unless connections change
     */
    public int getInt()
    {
        return (int)(0x80000000 * getDoubleBase());
    }
    
    @Override
    public double getNoise(double x, double y) {
        return getNoiseWithSeed(x, y, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, long seed) {
        x *= 2.25;
        y *= 2.25;
        double sum = (swayRandomized(seed, x) * swayRandomized(~seed, x + y));
        sum += (swayRandomized(seed, sum + y) * swayRandomized(~seed, x - y));
//        sum += swayRandomized(seed, sum - x + y);
        return Noise.extremeSigned(sum * 0.5);
//        return Noise.emphasizeSigned((sum + swayRandomized(seed, sum + x + y)) * 0.25);
        //return sum * 0.5;
    }

    @Override
    public double getNoise(double x, double y, double z) {
        return getNoiseWithSeed(x, y, z, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, long seed) {
        x *= 1.5;
        y *= 1.5;
        z *= 1.5;
        x += swayRandomized(seed ^ 0xD1B54A32D192ED03L, y + z) - swayRandomized(0xABC98388FB8FAC03L - seed, x - y);
        y += swayRandomized(seed ^ 0xDB4F0B9175AE2165L, x + z) - swayRandomized(0xBBE0563303A4615FL - seed, y - z);
        z += swayRandomized(seed ^ 0xE19B01AA9D42C633L, x + y) - swayRandomized(0xC6D1D6C8ED0C9631L - seed, z - x);
//        double sum = swayRandomized(seed, x - y) * (swayRandomized(~seed, x - z));
//        sum += swayRandomized(seed ^ 0xDB4F0B9175AE2165L, sum + y - z) * (swayRandomized(0xBBE0563303A4615FL - seed, x + y - sum));
//        sum += swayRandomized(seed ^ 0xE19B01AA9D42C633L, sum + z - x) * (swayRandomized(0xC6D1D6C8ED0C9631L - seed, -y - z + sum));
        return sway((x + y + z) * 0.3333333333333333);
        //return emphasizeSigned(sum * 0.3333333333333333);
    }

    @Override
    public double getNoise(double x, double y, double z, double w) {
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, long seed) {
        double sum = swayRandomized(seed, w + x - y);
        sum += swayRandomized(seed, sum + x + y - z);
        sum += swayRandomized(seed, sum + y + z - w);
        sum += swayRandomized(seed, sum + z + w - x);
        return emphasizeSigned(sum * 0.25);
    }

    @Override
    public double getNoise(double x, double y, double z, double w, double u, double v) {
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    @Override
    public double getNoiseWithSeed(double x, double y, double z, double w, double u, double v, long seed) {
        double sum = swayRandomized(seed, v + x - y);
        sum += swayRandomized(seed, sum + x + y - z);
        sum += swayRandomized(seed, sum + y + z - w);
        sum += swayRandomized(seed, sum + z + w - u);
        sum += swayRandomized(seed, sum + w + u - v);
        sum += swayRandomized(seed, sum + u + v - x);
        return emphasizeSigned(sum * 0.16666666666666666);
    }

    /*
     * Linearly interpolates between start and end (valid floats), with a between 0 (yields start) and 1 (yields end).
     * @param start a valid float
     * @param end a valid float
     * @param a a float between 0 and 1 inclusive
     * @return a float between x and y inclusive
     * /
    private static double interpolate(final double start, final double end, final double a)
    {
        return (1.0 - a) * start + a * end;
    }
    */
    /*
    private boolean haveNextNextGaussian = false;
    private double nextNextGaussian;
    private double nextGaussian(int state) {
        if (haveNextNextGaussian) {
            haveNextNextGaussian = false;
            return nextNextGaussian;
        } else {
            double v1, v2, s;
            do {
                v1 = 2 * NumberTools.randomDouble(state += 0xAE3779B9) - 1; // between -1 and 1
                v2 = 2 * NumberTools.randomDouble(state + 0xBE3779B9) - 1; // between -1 and 1
                s = v1 * v1 + v2 * v2;
            } while (s >= 1 || s == 0);
            double multiplier = Math.sqrt(-2 * Math.log(s) / s);
            nextNextGaussian = v2 * multiplier;
            haveNextNextGaussian = true;
            return v1 * multiplier;
        }
    }

    public void randomUnitVector(int seed, final double[] vector)
    {
        final int len = vector.length;
        double mag = 0.0, t;
        for (int i = 0; i < len; i++) {
            vector[i] = (t = nextGaussian(seed += 0x8E3779B9));
            mag += t * t;
        }
        if(mag == 0)
        {
            vector[0] = 1.0;
            mag = 1.0;
        }
        else
            mag = Math.sqrt(mag);
        for (int i = 0; i < len; i++) {
            vector[i] /= mag;
        }
    }
    public void randomManhattanVector (int seed, final double[] vector)
    {
        final int len = vector.length;
        double mag = 0.0;
        for (int i = 0; i < len; i++) {
            mag += Math.abs(vector[i] = NumberTools.randomFloatCurved(seed += 0x8E3779B9));
        }
        if(mag == 0)
        {
            vector[0] = 1.0;
            mag = 1.0;
        }
        for (int i = 0; i < len; i++) {
            vector[i] /= mag;
        }
    }
    
     * The same as {@link DiverRNG#determine(long)}, except this assumes state has already been multiplied by
     * 0x632BE59BD9B4E019L.
     * @param state a long that should change in increments of 0x632BE59BD9B4E019L
     * @return a pseudo-random permutation of state
    public static long determine(long state)
    {
        return (state = ((state = ((state ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25;
    }

    
    */
}
