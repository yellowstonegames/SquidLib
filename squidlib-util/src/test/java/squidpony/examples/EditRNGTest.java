package squidpony.examples;

import squidpony.squidmath.EditRNG;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.LongPeriodRNG;

import java.util.ArrayList;

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
        ArrayList<StringBuilder> rolls = new ArrayList<StringBuilder>(20);
        for (int i = 0; i < 20; i++) {
            rolls.add(new StringBuilder(200));
        }
        for (int i = 0; i < 500; i++) {
            rolls.get(rng.nextInt(20)).append(rng.rawLatest < 0.05 ? '~' : rng.rawLatest >= 0.95 ? '!' : '*');
        }

        double avg = 0;
        for (int i = 0; i < 20; i++) {
            StringBuilder sb = rolls.get(i);
            int len = sb.length();

            avg += len * i;
            System.out.print(i + (i < 10 ? " : " : ": "));
            System.out.print(sb);
            System.out.print(" Count: ");
            System.out.println(len);
        }
        System.out.println("Total: " + avg + ", Real Average: " + avg / 500.0);
        System.out.println();
    }
    public static void main(String[] args) {

        EditRNG lr = new EditRNG(new LightRNG(0xDADA157), 0.5, 0.0),
                lpr = new EditRNG(new LongPeriodRNG(0xDADA157), 0.5, 0.0);

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
