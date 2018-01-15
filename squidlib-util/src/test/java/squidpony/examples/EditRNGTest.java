package squidpony.examples;

import squidpony.squidmath.*;

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
        //rng.setState(-3036294613074652313L);
        ArrayList<StringBuilder> rolls = new ArrayList<StringBuilder>(20);
        for (int i = 0; i < 20; i++) {
            rolls.add(new StringBuilder(200));
        }
        for (int i = 0; i < 500; i++) {
            rolls.get(rng.nextInt(20)).append('*');
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
        System.out.println("Total: " + avg + ", Average: " + (avg / 19.5) / 500.0);
        System.out.println();
    }
    public static void d20GraphCurved(RandomnessSource rng, String description)
    {
        System.out.println("Testing " + description);
        ArrayList<StringBuilder> rolls = new ArrayList<StringBuilder>(21);
        for (int i = 0; i < 21; i++) {
            rolls.add(new StringBuilder(200));
        }
        int t;
        for (int i = 0; i < 1000; i++) {
            t = Math.round(NumberTools.formCurvedFloat(rng.next(32)) * 10);
            rolls.get(t + 10).append('*');
        }

        double avg = 0;
        for (int i = 0; i < 21; i++) {
            StringBuilder sb = rolls.get(i);
            int len = sb.length();

            avg += len * (i - 10);
            System.out.printf("% 4d: ", (i - 10));
            System.out.print(sb);
            System.out.print(" Count: ");
            System.out.println(len);
        }
        System.out.println("Total: " + avg + ", Average: " + avg / 1000.0);
        System.out.println();
    }

    public static void main(String[] args) {

        EditRNG lr = new EditRNG(0xDADA157, 0.5, 0.0),
                lpr = new EditRNG(0xBEEFD00DBA77L, 0.5, 0.0);
        RandomnessSource source = new ThrustAltRNG(0x1337CAFEBA77L);
        d20GraphCurved(source, "Curved");
        d20GraphCurved(source, "Curved");
        d20GraphCurved(source, "Curved");
        d20GraphCurved(source, "Curved");

//        for (double d = 0.1; d < 0.9; d+= 0.05) {
//            d20Graph(lr, d, "LightRNG");
//            d20Graph(lpr, d, "Other Seed");
//        }
//        lr.setCentrality(-25);
//        lpr.setCentrality(-25);
//        for (double d = 0.1; d < 0.9; d+= 0.05) {
//            d20Graph(lr, d, "LightRNG");
//            d20Graph(lpr, d, "Other Seed");
//        }
//        lr.setCentrality(-50);
//        lpr.setCentrality(-50);
//        for (double d = 0.1; d < 0.9; d+= 0.05) {
//            d20Graph(lr, d, "LightRNG");
//            d20Graph(lpr, d, "Other Seed");
//        }
//        lr.setCentrality(150);
//        lpr.setCentrality(150);
//        for (double d = 0.1; d < 0.9; d+= 0.05) {
//            d20Graph(lr, d, "LightRNG");
//            d20Graph(lpr, d, "Other Seed");
//        }
//        lr.setCentrality(200);
//        lpr.setCentrality(200);
//        for (double d = 0.1; d < 0.9; d+= 0.05) {
//            d20Graph(lr, d, "LightRNG");
//            d20Graph(lpr, d, "Other Seed");
//        }
//
//        lr.setCentrality(-25);
//        lpr.setCentrality(-25);
//        for (double d = 0.1; d < 0.9; d+= 0.05) {
//            d20Graph(lr, d, "LightRNG");
//            d20Graph(lpr, d, "Other Seed");
//        }
//        lr.setCentrality(-50);
//        lpr.setCentrality(-50);
//        for (double d = 0.1; d < 0.9; d+= 0.05) {
//            d20Graph(lr, d, "LightRNG");
//            d20Graph(lpr, d, "Other Seed");
//        }
//        lr.setCentrality(-150);
//        lpr.setCentrality(-150);
//        for (double d = 0.1; d < 0.9; d+= 0.05) {
//            d20Graph(lr, d, "LightRNG");
//            d20Graph(lpr, d, "Other Seed");
//        }
//        lr.setCentrality(-200);
//        lpr.setCentrality(-200);
//        for (double d = 0.1; d < 0.9; d+= 0.05) {
//            d20Graph(lr, d, "LightRNG");
//            d20Graph(lpr, d, "Other Seed");
//        }
    }
}
