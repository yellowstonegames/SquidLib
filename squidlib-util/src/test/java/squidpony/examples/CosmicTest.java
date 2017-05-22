package squidpony.examples;

import squidpony.StringKit;
import squidpony.squidmath.CosmicNumbering;
import squidpony.squidmath.NumberTools;

/**
 * Created by Tommy Ettinger on 5/18/2017.
 */
public class CosmicTest {
    public static void main(String[] args)
    {
        double[] data = {100.0, 0.0, 0.0, 1.7, 0.067, 0.42, -0.666, 1.9};
        CosmicNumbering cn = new CosmicNumbering(data);
        int state = 66666;
        double total = 0.0, t, min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i <= 10000; i++) {
            System.out.println((t = cn.getDouble()) + " or " + cn.getInt());
            min = Math.min(min, t);
            max = Math.max(max, t);
            total += t;
            if(Math.abs(cn.getDouble()) >= 1.0)
            {
                System.out.println(StringKit.join(",", data));
                System.out.println(i);
                break;
            }
            data[i & 7] += NumberTools.randomSignedFloat(state += 98765) * 0.35;
            data[i + 1 & 7] += NumberTools.randomSignedFloat(state += 777777777) * 0.25;
        }
        System.out.println("\nmin:   " + min);
        System.out.println("\nmax:   " + max);
        System.out.println("\ntotal: " + total);
    }
}
