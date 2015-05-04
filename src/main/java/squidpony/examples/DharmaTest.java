package squidpony.examples;

import squidpony.squidmath.DharmaRNG;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.SobolQRNG;

/**
 * Created by Tommy Ettinger on 5/2/2015.
 */
public class DharmaTest {
    public static void main(String[] args) {
        for(int d = 1; d < 20; d++)
        {
            long sum = 0l;
            System.out.println("\nFairness: " + (d * 0.05));
            DharmaRNG sq = new DharmaRNG(new LightRNG(d * 0xbabe), d * 0.05);
            int result;
            for(int c = 0; c < 100; c++)
            {
                result = sq.nextInt(100);
                sum += result;
                System.out.print(result + " "); // System.out.print(result + "(" + sq.getFortune() + ") ");
            }
            System.out.println("\nTotal: " + sum);
            System.out.println("Average for this many calls: " + (50 * 100));
        }
    }
}
