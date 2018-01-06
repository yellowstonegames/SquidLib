package squidpony.examples;

import org.junit.Test;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.ProbabilityTable;
import squidpony.squidmath.WeightedTable;

import static squidpony.examples.LanguageGenTest.PRINTING;

/**
 * Created by Tommy Ettinger on 2/21/2017.
 */
public class ProbabilityTableTest {
    @Test
    public void testTable() {
        ProbabilityTable<String> wood = new ProbabilityTable<>("wood"),
                carpenter = new ProbabilityTable<>("carpenter");
        wood.add("splinter", 10).add("twig", 4).add("branch", 2).add("plank", 1).add("twig", 3);
        carpenter.add("table", 3).add("shelf", 7).add(wood, 5).add("chair", 3).add("chair", 2);

        OrderedMap<String, Integer> woodCounts = new OrderedMap<>(
                new String[]{"splinter", "twig", "branch", "plank"}, new Integer[]{0, 0, 0, 0}),
                carpenterCounts = new OrderedMap<>(
                        new String[]{"splinter", "twig", "branch", "plank", "table", "shelf", "chair"},
                        new Integer[]{0, 0, 0, 0, 0, 0, 0});
        String current;
        for (int l = 0; l < 10; l++) {
            for (int i = 0; i < 20; i++) {
                current = wood.random();
                woodCounts.put(current, woodCounts.get(current) + 1);
                if (PRINTING) {
                    System.out.print(current);
                    System.out.print(' ');
                }
            }
            if (PRINTING) System.out.println();
        }
        if (PRINTING) {
            System.out.println();
            System.out.println(woodCounts);
            System.out.println('\n');
        }
        for (int l = 0; l < 10; l++) {
            for (int i = 0; i < 20; i++) {
                current = carpenter.random();
                carpenterCounts.put(current, carpenterCounts.get(current) + 1);
                if (PRINTING) {
                    System.out.print(current);
                    System.out.print(' ');
                }
            }
            if (PRINTING) System.out.println();
        }
        if (PRINTING) {
            System.out.println();
            System.out.println(carpenterCounts);
            System.out.println('\n');
        }
        carpenterCounts = new OrderedMap<>(
                new String[]{"splinter", "twig", "branch", "plank", "table", "shelf", "chair"},
                new Integer[]{0, 0, 0, 0, 0, 0, 0});
        carpenter.remove("shelf", 4);
        carpenter.remove("chair");
        for (int l = 0; l < 10; l++) {
            for (int i = 0; i < 20; i++) {
                current = carpenter.random();
                carpenterCounts.put(current, carpenterCounts.get(current) + 1);
                if (PRINTING) {
                    System.out.print(current);
                    System.out.print(' ');
                }
            }
            if (PRINTING) System.out.println();
        }
        if (PRINTING) {
            System.out.println();
            System.out.println(carpenterCounts);
        }
    }
    @Test
    public void testWeighted()
    {
        double[] weights = {5.777, 4.666, 2.444, 3.222};
        double sum = weights[0] + weights[1] + weights[2] + weights[3];
        WeightedTable wood = new WeightedTable(weights);
        String[] woodStrings = {"splinter", "twig", "plank", "branch"};

        OrderedMap<String, Integer> woodCounts = new OrderedMap<>(
                new String[]{"splinter", "twig", "plank", "branch"}, new Integer[]{0, 0, 0, 0});
        String current;
        long state = (long) (System.nanoTime() / (Math.random() * Math.random() + 0.01));
        for (int l = 0; l < 100; l++) {
            for (int i = 0; i < 28; i++) {
                current = woodStrings[wood.random(++state)];
                woodCounts.put(current, woodCounts.get(current) + 1);
                if (PRINTING) {
                    System.out.print(current);
                    System.out.print(' ');
                }
            }
            if (PRINTING) System.out.println();
        }
        if (PRINTING) {
            System.out.println();
            for (int i = 0; i < 4; i++) {
                System.out.println("There should be about " + (2800 * weights[i] / sum) + " " + woodStrings[i] +
                        " and there are " + woodCounts.get(woodStrings[i]));
            }
            System.out.println('\n');
        }
    }
}
