package squidpony.performance;

import org.junit.Test;
import squidpony.squidmath.Arrangement;
import squidpony.squidmath.OrderedSet;

/**
 * Testing the speed of Arrangement when a lot of items are in use.
 * Created by Tommy Ettinger on 5/19/2017.
 */
public class ArrangementRace {
    @Test
    public void checkArrangement()
    {
        final int count = 1000;
        long time = -System.currentTimeMillis();
        Arrangement<Integer> rush = new Arrangement<>(count * 2, 0.5f);
        for (int i = 0; i < count; i++) {
            rush.add(i);
        }
        for (int i = 0; i < count; i++) {
            rush.addAt(rush.getInt(i), -1 - i);
        }
        time += System.currentTimeMillis();
        System.out.println("Combined add/addAt/getInt took " + time + " ms");
        rush.clear();
        for (int i = 0; i < count; i++) {
            rush.add(i);
        }
        long total = 0L;
        time = -System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            total += rush.getInt(i);
        }
        time += System.currentTimeMillis();
        System.out.println("Simply getInt took " + time + " ms (" + total + ")");

    }
    @Test
    public void checkSet()
    {
        final int count = 1000;
        long time = -System.currentTimeMillis();
        OrderedSet<Integer> rush = new OrderedSet<>(count * 2, 0.5f);
        for (int i = 0; i < count; i++) {
            rush.add(i);
        }
        for (int i = 0; i < count; i++) {
            rush.addAt(-1 - i,rush.indexOf(i));
        }
        time += System.currentTimeMillis();
        System.out.println("Combined add/addAt/indexOf took " + time + " ms");
        rush.clear();
        for (int i = 0; i < count; i++) {
            rush.add(i);
        }
        long total = 0L;
        time = -System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            total += rush.indexOf(i);
        }
        time += System.currentTimeMillis();
        System.out.println("Simply indexOf took " + time + " ms (" + total + ")");

    }

}
