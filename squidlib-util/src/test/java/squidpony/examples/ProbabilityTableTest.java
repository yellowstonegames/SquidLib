package squidpony.examples;

import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.ProbabilityTable;

/**
 * Created by Tommy Ettinger on 2/21/2017.
 */
public class ProbabilityTableTest {
    public static void main(String[] args) {
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
                System.out.print(current);
                System.out.print(' ');
            }
            System.out.println();
        }
        System.out.println();
        System.out.println(woodCounts);
        System.out.println('\n');
        for (int l = 0; l < 10; l++) {
            for (int i = 0; i < 20; i++) {
                current = carpenter.random();
                carpenterCounts.put(current, carpenterCounts.get(current) + 1);
                System.out.print(current);
                System.out.print(' ');
            }
            System.out.println();
        }
        System.out.println();
        System.out.println(carpenterCounts);
        System.out.println('\n');
        carpenterCounts = new OrderedMap<>(
                new String[]{"splinter", "twig", "branch", "plank", "table", "shelf", "chair"},
                new Integer[]{0, 0, 0, 0, 0, 0, 0});
        carpenter.remove("shelf", 4);
        carpenter.remove("chair");
        for (int l = 0; l < 10; l++) {
            for (int i = 0; i < 20; i++) {
                current = carpenter.random();
                carpenterCounts.put(current, carpenterCounts.get(current) + 1);
                System.out.print(current);
                System.out.print(' ');
            }
            System.out.println();
        }
        System.out.println();
        System.out.println(carpenterCounts);
    }
}
