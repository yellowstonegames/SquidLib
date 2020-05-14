package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 */
@Beta
public class PhantomNoise {
    public static final PhantomNoise instance = new PhantomNoise();
    
    private final long[] coefficients;
    public final int dim;
    private final double inverse;
    private final double[] working, points;
    private final double[][] vertices;
    private final int[] floors;
    public PhantomNoise() {
        this(0xFEEDBEEF1337CAFEL, 3);
    }

    public PhantomNoise(long seed, int dimension) {
        dim = Math.max(2, dimension);
        working = new double[dim+1];
        points = new double[dim+1];
        vertices = new double[dim+1][dim];
        double id = -1.0 / dim;
        vertices[0][0] = 1.0;
        for (int v = 1; v <= dim; v++) {
            vertices[v][0] = id;
        }
        for (int d = 1; d < dim; d++) {
            double t = 0.0;
            for (int i = 0; i < d; i++) {
                t += vertices[d][i] * vertices[d][i];
            }
            vertices[d][d] = Math.sqrt(1.0 - t);
            t = (id - t) / vertices[d][d];
            for (int v = d + 1; v <= dim; v++) {
                vertices[v][d] = t;
            }
        }
        floors = new int[dim+1];
        coefficients = new long[dim+1];
        for (int i = 0; i <= dim; i++) {
            coefficients[i] = DiverRNG.randomize(seed ^ i) | 1L;
        }
        inverse = 1.0 / (dim + 1.0);
//        printDebugInfo();
    }

    protected double valueNoise()
    {
        final long sd = coefficients[dim] * NumberTools.doubleToMixedIntBits(working[dim]);
        for (int i = 0; i < dim; i++) {
            floors[i] = working[i] >= 0.0 ? (int)working[i] : (int)working[i] - 1;
            working[i] -= floors[i];
            working[i] *= working[i] * (3.0 - 2.0 * working[i]);
        }
        double sum = 0.0, temp;
        final int limit = 1 << dim;
        int bit;
        for (int i = 0; i < limit; i++) {
            temp = 1.0;
            long dot = sd;
            for (int j = 0; j < dim; j++) {
                bit = (i >>> j & 1);
                temp *= bit + (1|-bit) * working[j];
                dot += (floors[j] - bit) * coefficients[j];
            }
            sum += temp * (int)(dot ^ dot >>> 23 ^ dot >>> 47);
        }
        return (sum * 0x1p-32 + 0.5);
    }

    public double getNoise(double... args) {
        for (int v = 0; v <= dim; v++) {
            points[v] = 0.0;
            for (int d = 0; d < dim; d++) {
                points[v] += args[d] * vertices[v][d];
            }
        }
        working[dim] = 0.6180339887498949; // inverse golden ratio; irrational, so its bit representation nears random
        double result = 0.0, warp = 0.0;
        for (int i = 0; i <= dim; i++) {
            for (int j = 0, d = 0; j < dim; j++, d++) {
                if(d == i) d++;
                working[j] = points[d];
            }
            working[0] += warp;
            warp = valueNoise();
            result += warp;
            working[dim] += -0.423310825130748; // e - pi
        }
        result *= inverse;
        return (result <= 0.5)
                ? Math.pow(result * 2, dim) - 1.0
                : Math.pow((result - 1) * 2, dim) * (((dim & 1) << 1) - 1) + 1.0;
//        for (int i = 1; i < dim; i++) {
//            result *= result * (3.0 - 2.0 * result);
//        }
//        return  (result * result * (6.0 - 4.0 * result) - 1.0);
    }

    public double getNoise(double x, double y) {
        points[0] = -0.4161468365471422 * x + 0.9092974268256818 * y;
        points[1] = -0.5794012529532914 * x + -0.8150424455671962 * y;
        points[2] = 0.9955480895004332 * x + -0.09425498125848553 * y;
        working[dim] = Math.PI;
        double result = 0.0, warp = 0.0;
        for (int i = 0; i <= dim; i++) {
            for (int j = 0, d = 0; j < dim; j++, d++) {
                if(d == i) d++;
                working[j] = points[d] + warp;
            }
            warp = valueNoise();
            result += warp;
            working[dim] += Math.E;
        }
        result *= inverse;
        return (result <= 0.5)
                ? (result * result * 4) - 1.0
                : ((result - 1) * (result - 1) * -4) + 1.0;

//        result *= result * (3.0 - 2.0 * result);
//        return  (result * result * (6.0 - 4.0 * result) - 1.0);
    }
    
    void printDebugInfo(){         
        System.out.println("PhantomNoise with Dimension " + dim + ":");
        final String dimNames = "xyzwuvabcdefghijklmnopqrst";
        for (int v = 0; v <= dim; v++) {
            System.out.print("points[" + v + "] = ");
            for (int i = 0; i < dim; i++) {
                if(vertices[v][i] != 0.0) 
                {
                    if(i > 0)
                        System.out.print(" + ");
                    if(vertices[v][i] == 1.0)
                        System.out.print(dimNames.charAt(i % dimNames.length()));
                    else 
                        System.out.print(dimNames.charAt(i % dimNames.length()) + " * " + vertices[v][i]);
                }
            }
            System.out.println(';');
        }

    }
}
