package squidpony.performance;

import squidpony.squidmath.Arrangement;

/**
 * Testing the speed of Arrangement when a lot of items are in use.
 * Created by Tommy Ettinger on 5/19/2017.
 */
public class ArrangementRace {
    public static void main(String[] args)
    {
        long time = -System.currentTimeMillis();
        Arrangement<Integer> rush = new Arrangement<>(200000, 0.5f);
        for (int i = 0; i < 100000; i++) {
            rush.add(i);
        }
        for (int i = 0; i < 100000; i++) {
            rush.addAt(rush.getInt(i), -1 - i);
        }
        time += System.currentTimeMillis();
        System.out.println("Took " + time + " ms");
    }
}
