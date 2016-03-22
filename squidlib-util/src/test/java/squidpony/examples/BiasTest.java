package squidpony.examples;

import squidpony.squidmath.*;

import java.util.Arrays;

/**
 * Created by Tommy Ettinger on 3/21/2016.
 */
public class BiasTest {

    public static void d10Graph(RandomBias bias, double expected)
    {
        System.out.println("Testing " + bias + ", expecting average of " + expected);
        int[] rolls = new int[10];
        for (int i = 0; i < 200; i++) {
            rolls[bias.biasedInt(expected, 10)]++;
        }
        for (int o = 0; o < 10; o++) {
            for (int i = (int) '0'; i <= (int) '9'; i++) {
                System.out.print((char) i);
            }
        }
        System.out.println();
        double avg = 0;
        for (int i = 0; i < 10; i++) {
            char[] c = new char[rolls[i]];
            Arrays.fill(c, '*');
            avg += rolls[i] * i;
            System.out.println(c);
        }
        System.out.println("Total: " + avg + ", Real Average: " + avg / 200.0);
        System.out.println();
    }
    public static void main(String[] args) {

        RNG lr = new RNG(new LightRNG(0xDADA157)),
                der = new DeckRNG(0xDADA157);
                //dhr = new DharmaRNG(0xDADA157, 0.54);
        RandomBias[] randomBiases = new RandomBias[]{new RandomBias(lr, null, 0),
                new RandomBias(lr, null, 1),
                new RandomBias(lr, null, 2),
                new RandomBias(lr, null, 3),
                new RandomBias(lr, null, 4),
                new RandomBias(der, null, 0),
                new RandomBias(der, null, 1),
                new RandomBias(der, null, 2),
                new RandomBias(der, null, 3),
                new RandomBias(der, null, 4),
                //new RandomBias(dhr, null, 0),
                //new RandomBias(dhr, null, 1),
                //new RandomBias(dhr, null, 2),
                //new RandomBias(dhr, null, 3)
        };
        for(RandomBias rb : randomBiases)
        {
            for (double d = 0.1; d < 0.95; d+= 0.1) {
                d10Graph(rb, d);
            }
        }

    }
}
