package squidpony.examples;

import squidpony.squidmath.DharmaRNG;
import squidpony.squidmath.SobolQRNG;

/**
 * Created by Tommy Ettinger on 5/2/2015.
 */
public class DharmaTest {
    public static void main(String[] args) {
        long sum = 0l;
        for(int d = 1; d <= 20; d++)
        {
            DharmaRNG sq = new DharmaRNG(d * 0xd00d);
            long result;
            for(int c = 0; c < 20; c++)
            {
                result = sq.nextLong(100);
                sum += result;
                System.out.print(result + " ");
            }
            System.out.println();
        }
        System.out.println("\nTotal: " + sum);
        System.out.println("\nAverage for this many calls: " + (50 * 400));
    }
}
