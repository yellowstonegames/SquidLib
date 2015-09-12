package squidpony.examples;

import squidpony.squidmath.SobolQRNG;

/**
 * Created by Tommy Ettinger on 5/2/2015.
 */
public class SobolTest {
    public static void main(String[] args) {
        for(int d = 1; d <= 20; d++)
        {
            SobolQRNG sq = new SobolQRNG(d);
            sq.skipTo(9000);
            long[] result;
            for(int c = 0; c < 10; c++)
            {
                result = sq.nextLongVector(100);
                for(int i = 0; i < d; i++)
                {
                    System.out.print(result[i] + " ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }
}
