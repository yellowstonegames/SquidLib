package squidpony.examples;

import squidpony.ArrayTools;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.SobolQRNG;
import squidpony.squidmath.VanDerCorputQRNG;

/**
 * Created by Tommy Ettinger on 5/2/2015.
 */
public class SobolTest {
    public static void main(String[] args) {
        System.out.println("SOBOL:");
        for(int d = 1; d <= 16; d++)
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
            
            sq.skipTo(9000);
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
        System.out.println("\nVDC:");
        for(int d : new int[]{2, 3, 5, 7, 11, 13, 17, 19})
        {
            VanDerCorputQRNG vdc = new VanDerCorputQRNG(d, 0L);
            System.out.println("Base " + d);
            System.out.print(vdc.nextDouble());
            for (int i = 0; i < 32; i++) {
                System.out.print(", " + vdc.nextDouble());
            }
            System.out.println();
            vdc.setState(0L);
            System.out.print(vdc.nextLong());
            for (int i = 0; i < 32; i++) {
                System.out.print(", " + vdc.nextLong());
            }
            System.out.println();
            vdc.setState(0L);
            System.out.print(vdc.nextLong() ^ (vdc.nextLong() >>> vdc.next(3) + 2));
            for (int i = 0; i < 32; i++) {
                System.out.print(", " + (vdc.nextLong() ^ (vdc.nextLong() >>> vdc.next(3) + 2)));
            }
            System.out.println();

            vdc.setState(83L);
            System.out.print(vdc.nextDouble());
            for (int i = 0; i < 32; i++) {
                System.out.print(", " + vdc.nextDouble());
            }
            System.out.println();
            vdc.setState(83L);
            System.out.print(vdc.nextLong());
            for (int i = 0; i < 32; i++) {
                System.out.print(", " + vdc.nextLong());
            }
            System.out.println();
            vdc.setState(83L);
            System.out.print(vdc.nextLong() ^ (vdc.nextLong() >>> vdc.next(3) + 2));
            for (int i = 0; i < 32; i++) {
                System.out.print(", " + (vdc.nextLong() ^ (vdc.nextLong() >>> vdc.next(3) + 2)));
            }
            System.out.println();
        }
        System.out.println("HALTON:");
        char[][] map = ArrayTools.fill(' ', 80, 40);
        char[] letters = ArrayTools.letterSpan(128);
        int x, y;
        int[] primes = {2, 3, 5, 7, 11};
        for (int a = 0; a < primes.length; a++) {
            for (int b = a+1; b < primes.length; b++) {
                VanDerCorputQRNG xAxis = new VanDerCorputQRNG(primes[a], 59),
                        yAxis = new VanDerCorputQRNG(primes[b], 59);
                ArrayTools.fill(map, ' ');
                System.out.println("Prime for x axis: " + primes[a] + ", for y axis: " + primes[b]);
                for (int i = 0; i < 64; i++) {
                    x = (int)(40*xAxis.nextDouble());
                    y = (int)(40*yAxis.nextDouble());
                    if(map[x<<1][y] == ' ')
                        map[x<<1][y] = letters[i];
                    else
                        System.out.println("Oh no, overlap! primes are " + primes[a] + ", " + primes[b] + ", i is " + i +
                                ", x is " + x + ", y is " + y);
                }
                DungeonUtility.debugPrint(map);
                System.out.println();
            }
        }

    }
}
