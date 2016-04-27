package squidpony.examples;

import squidpony.squidmath.*;

import java.util.Arrays;

/**
 * Created by Tommy Ettinger on 3/21/2016.
 */
public class EditRNGTest {

    public static void d20Graph(EditRNG rng, double expected, String description)
    {
        System.out.println("Testing " + description + ", expecting average of " + expected + ", centrality of " +
                rng.getCentrality());
        rng.setExpected(expected);
        rng.setState(-3036294613074652313L);
        int[] rolls = new int[20];
        for (int i = 0; i < 500; i++) {
            rolls[rng.nextInt(20)]++;
        }

        double avg = 0;
        for (int i = 0; i < 20; i++) {
            char[] c = new char[rolls[i]];
            Arrays.fill(c, '*');
            avg += rolls[i] * i;
            System.out.print(i + ": ");
            System.out.print(c);
            System.out.print(" Count: ");
            System.out.println(rolls[i]);
        }
        System.out.println("Total: " + avg + ", Real Average: " + avg / 500.0);
        System.out.println();
    }
    public static void main(String[] args) {

        EditRNG lr = new EditRNG(new LightRNG(0xDADA157), 0.5, 0.0),
                lpr = new EditRNG(new LongPeriodRNG(0xDADA157), 0.5, 0.0);

        for (double d = 0.85; d < 0.9; d+= 0.05) {
            d20Graph(lr, d, "LightRNG");
            d20Graph(lpr, d, "LongPeriodRNG");
        }
        lr.setCentrality(-25);
        lpr.setCentrality(-25);
        for (double d = 0.85; d < 0.9; d+= 0.05) {
            d20Graph(lr, d, "LightRNG");
            d20Graph(lpr, d, "LongPeriodRNG");
        }
        lr.setCentrality(-50);
        lpr.setCentrality(-50);
        for (double d = 0.1; d < 0.9; d+= 0.05) {
            d20Graph(lr, d, "LightRNG");
            d20Graph(lpr, d, "LongPeriodRNG");
        }
        lr.setCentrality(150);
        lpr.setCentrality(150);
        for (double d = 0.1; d < 0.9; d+= 0.05) {
            d20Graph(lr, d, "LightRNG");
            d20Graph(lpr, d, "LongPeriodRNG");
        }
        lr.setCentrality(200);
        lpr.setCentrality(200);
        for (double d = 0.1; d < 0.9; d+= 0.05) {
            d20Graph(lr, d, "LightRNG");
            d20Graph(lpr, d, "LongPeriodRNG");
        }

        lr.setCentrality(-25);
        lpr.setCentrality(-25);
        for (double d = 0.1; d < 0.9; d+= 0.05) {
            d20Graph(lr, d, "LightRNG");
            d20Graph(lpr, d, "LongPeriodRNG");
        }
        lr.setCentrality(-50);
        lpr.setCentrality(-50);
        for (double d = 0.1; d < 0.9; d+= 0.05) {
            d20Graph(lr, d, "LightRNG");
            d20Graph(lpr, d, "LongPeriodRNG");
        }
        lr.setCentrality(-150);
        lpr.setCentrality(-150);
        for (double d = 0.1; d < 0.9; d+= 0.05) {
            d20Graph(lr, d, "LightRNG");
            d20Graph(lpr, d, "LongPeriodRNG");
        }
        lr.setCentrality(-200);
        lpr.setCentrality(-200);
        for (double d = 0.1; d < 0.9; d+= 0.05) {
            d20Graph(lr, d, "LightRNG");
            d20Graph(lpr, d, "LongPeriodRNG");
        }

    }
}
