package squidpony.examples;

import squidpony.squidmath.LightRNG;
import squidpony.squidmath.OrderedMap;

/**
 * Created by Tommy Ettinger on 6/19/2017.
 */
public class OrderedDataTest {
    public static void main(String[] args) {
        OrderedMap<Integer, String> om = new OrderedMap<>(16);
        LightRNG rng = new LightRNG(0x89ABCDEF);
        int r;
        for (int i = 0; i < 260; i++) {
            do {
                r = rng.next(12) + 400;
            }while (om.containsKey(r));
            om.put(r, i + ":" + r);
        }
        for(String s : om.values())
        {
            System.out.println(s);
        }
        System.out.println();

        om.putAt(om.keyAt(1), om.removeAt(1), 1);
        for(String s : om.values())
        {
            System.out.println(s);
        }
        System.out.println();

        om.putAt(om.keyAt(1), om.removeAt(1), 1);
        om.putAt(om.keyAt(2), om.removeAt(2), 2);
        for(String s : om.values())
        {
            System.out.println(s);
        }
        System.out.println();

        for (int i = 0; i < 260; i++) {
            r = rng.next(12) + 400;
            om.alterAtCarefully(i, r);
            // Try commenting out the above line and uncommenting below to see the difference.
            //om.alterAt(i, r);
        }
        for(String s : om.values())
        {
            System.out.println(s);
        }
        System.out.println();
        for (int i = 0; i < 260; i++) {
            System.out.println(om.getAt(i));
        }
    }
}
