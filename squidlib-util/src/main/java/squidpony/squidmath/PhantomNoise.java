package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 */
@Beta
public class PhantomNoise {
    public static final PhantomNoise instance = new PhantomNoise();
    
    private final CrossHash.Yolk yolk;
    public final int dim;
    private final double scale, inverse;
    private final double[] working, points;
    private final int[] floors;
    public PhantomNoise() {
        this(0xFEEDBEEF1337CAFEL, 3);
    }

    public PhantomNoise(long seed, int dimension) {
        dim = Math.max(2, dimension);
        working = new double[dim+1];
        points = new double[dim+1];
        floors = new int[dim+1];
        yolk = new CrossHash.Yolk(seed);
        scale = 1.0 / Math.sqrt(dim);
        inverse = 1.0 / (dim+1);
    }

    public double valueNoise()
    {
        floors[dim] = NumberTools.doubleToMixedIntBits(working[dim]);
        for (int i = 0; i < dim; i++) {
            floors[i] = working[i] >= 0.0 ? (int)working[i] : (int)working[i] - 1;
            working[i] -= floors[i];
            floors[i] <<= 1;
            working[i] *= working[i] * (3.0 - 2.0 * working[i]);
        }
        double sum = 0.0, temp;
        final int limit = 1 << dim;
        int bit;
        for (int i = 0; i < limit; i++) {
            temp = 1.0;
            for (int j = 0; j < dim; j++) {
                bit = (i >>> j & 1);
                temp *= bit + (1|-bit) * (working[j]);
                floors[j] = (floors[j] & -2) | bit;
            }
            sum += temp * yolk.hash(floors);
        }
        return (sum * 0x1p-32 + 0.5) * inverse;
    }

    public double phantomNoise(double[] args) {
        for (int i = 0; i < dim; i++) {
            points[i] = args[i];
            for (int j = 0; j < dim; j++) {
                if(i != j)
                    points[i] -= args[i];
            }
        }
        points[dim] = 0.0;
        for (int i = 0; i < dim; i++) {
            points[dim] += args[i];
        }
        working[dim] = Math.PI;
        double result = 0.0;
        for (int i = 0; i <= dim; i++) {
            for (int j = 0, d = 0; j < dim; j++) {
                if(d == i) d++;
                working[j] = points[d];
            }
            result += valueNoise();
            working[dim] += Math.E;
        }
        return  (result * result * (6.0 - 4.0 * result) - 1.0);
    }
    
    public double getNoise(double... args) {
        return phantomNoise(args);
    }
}
