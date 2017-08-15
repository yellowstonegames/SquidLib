package squidpony.squidmath;

import java.io.Serializable;

/**
 * Like a kind of RNG, but fully deterministic in a way that depends on certain connected variables.
 * Intended as a way to produce similar values when small changes occur in the connections, while potentially producing
 * larger changes when the changes are more significant (unlike an RNG or hashing function, which can and should produce
 * very different output given even slightly different seeds/input). This might be useful to produce procedural story
 * data that is similar when most of the connected inputs are similar, or for terrain generation/population. This can
 * produce ints and doubles, and does not produce a different output unless its input is changed (usually by altering a
 * shared reference to {@code connections}).
 * <br>
 * Created by Tommy Ettinger on 5/18/2017.
 */
public class CosmicNumbering implements Serializable {
    private static final long serialVersionUID = 0L;
    protected double[] connections;
    public CosmicNumbering()
    {
        connections = new double[1];
    }
    public CosmicNumbering(double[] connections) {
        if(connections == null || connections.length == 0)
            this.connections = new double[1];
        else
            this.connections = connections;
    }

    public double[] getConnections() {
        return connections;
    }

    public void setConnections(double[] connections) {
        if(connections == null || connections.length == 0)
            this.connections = new double[1];
        else
            this.connections = connections;
    }

    /**
     * Gets a double determined by the current values in the connections, accessible via {@link #getConnections()}.
     * Returns a value between -1.0 and 1.0 (exclusive on 1.0). Used as the basis for other methods in this class.
     * @return a double between -1.0 and 1.0; will be the same value until/unless connections change
     */
    public double getDoubleBase()
    {
        double[] connections = this.connections;
        final int len = connections.length;
        long floor;
        double diff, conn, result = 0.0;//, total = 1.0;
        for (int i = 0; i < len; i++) {
            diff = (conn = connections[i]) - (floor = fastFloor(conn));
            //  & 0xfffffffffffffL
            result +=
                    NumberTools.bounce((NumberTools.longBitsToDouble((floor * 0x9E3779B97F4A7C15L >>> 12) | 0x4000000000000000L) - 3.0)
                            * (1.0 - diff)
                            + (NumberTools.longBitsToDouble(((floor + 1L) * 0x9E3779B97F4A7C15L >>> 12) | 0x4000000000000000L) - 3.0)
                            * diff
                            + 5 + ~i * 0.618);
//            result *= 1.618;
//            total *= 1.618;
        }
        //return NumberTools.bounce(result + (len * 2.5));
        return result / len;
    }

    /**
     * Gets a double determined by the current values in the connections, accessible via {@link #getConnections()}.
     * Returns a value between 0.0 and 1.0 (exclusive on 1.0).
     * @return a double between 0.0 and 1.0; will be the same value until/unless connections change
     */
    public double getDouble()
    {
        return getDoubleBase() * 0.5 + 0.5;
    }

//    public double getDouble()
//    {
//        double v = 0.0, diff;
//        double[] connections = this.connections;
//        final int len = connections.length;
//        long floor;
//        for (int i = 0; i < len; i++) {
//            diff = connections[i] - (floor = fastFloor(connections[i]));
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

    /**
     * Like {@link Math#floor}, but returns an int. Doesn't consider weird doubles like INFINITY and NaN.
     *
     * @param t the double to find the floor for
     * @return the floor of t, as an int
     */
    public static long fastFloor(double t) {
        return t >= 0 ? (long) t : (long) t - 1L;
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
    }*/
}
