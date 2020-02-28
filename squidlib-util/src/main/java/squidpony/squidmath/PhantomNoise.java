package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 */
@Beta
public class PhantomNoise {
    public static final PhantomNoise instance = new PhantomNoise();
    
    private final CrossHash.Yolk yolk;
    public final int dim;
    private final double inverse;
    private final double scale;
    private final double[] working, points;
    private final int[] floors, hashFloors;
    public PhantomNoise() {
        this(0xFEEDBEEF1337CAFEL, 3);
    }

    public PhantomNoise(long seed, int dimension) {
        dim = Math.max(2, dimension);
        working = new double[dim+1];
        points = new double[dim+1];
        floors = new int[dim+1];
        hashFloors = new int[dim+1];
        yolk = new CrossHash.Yolk(seed);
        scale = -1.0 / (1.0 + Math.sqrt(1.0 + dim));
        inverse = 1.0 / (dim+1);
    }

    public double valueNoise()
    {
        hashFloors[dim] = NumberTools.doubleToMixedIntBits(working[dim]);
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
            for (int j = 0; j < dim; j++) {
                bit = (i >>> j & 1);
                temp *= bit + (1|-bit) * working[j];
                hashFloors[j] = floors[j] + 1 - bit;
            }
            sum += temp * yolk.hash(hashFloors);
        }
        return (sum * 0x1p-32 + 0.5);
    }

    public double getNoise(double... args) { 
        System.arraycopy(args, 0, points, 0, dim);
//        for (int i = 0; i < dim; i++) {
//            points[i] = args[i];
//            for (int j = 0; j < dim; j++) {
//                if(i != j)
//                    points[i] -= args[j];
//            }
//            points[i] *= scale;
//        }
        points[dim] = 0.0;
        for (int i = 0; i < dim; i++) {
            points[dim] += args[i];
        }
        points[dim] *= scale;
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
        for (int i = 1; i < dim; i++) {
            result *= result * (3.0 - 2.0 * result);
        }
        return  (result * result * (6.0 - 4.0 * result) - 1.0);
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
        result *= result * (3.0 - 2.0 * result);
        return  (result * result * (6.0 - 4.0 * result) - 1.0);
    }
}
