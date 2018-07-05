package squidpony.squidmath;

/**
 * Created by Tommy Ettinger on 6/19/2017.
 */
public class OrderedDataTest {
    public static void main(String[] args) {
        OrderedMap<Coord, String> om = new OrderedMap<>(10);
        LightRNG rng = new LightRNG(0x89ABCDEF);
        RNG rn = new RNG(rng);
        Coord r;
        final int COUNT = 100;
        for (int i = 0; i < COUNT; i++) {
            do {
                r = rn.nextCoord(60, 60);
            }while (om.containsKey(r));
            om.put(r, i + ":" + r);
        }
        for (int i = 0; i < COUNT; i++) {
            System.out.println(om.keyAt(i) + " => " + om.getAt(i));
        }
        System.out.println();
        System.out.println();

        om.putAt(om.keyAt(1), om.removeAt(1), 1);
        for (int i = 0; i < COUNT; i++) {
            System.out.println(om.keyAt(i) + " => " + om.getAt(i));
        }
        System.out.println();

        om.putAt(om.keyAt(1), om.removeAt(1), 1);
        om.putAt(om.keyAt(2), om.removeAt(2), 2);
        for (int i = 0; i < COUNT; i++) {
            System.out.println(om.keyAt(i) + " => " + om.getAt(i));
        }
        System.out.println();

        for (int n = 0; n < 4; n++) {
            System.out.println(om.order.size);
            for (int i = 0; i < COUNT; i++) {
                int pos = om.order.get(i);
                if(om.order.indexOf(pos) != om.order.lastIndexOf(pos))
                    System.out.println("DUPLICATE! RUN " + n + ", I " + i);
            }
            for (int i = 0; i < COUNT; i++) {
                r = om.keyAt(i);
                while(true)
                {
                    String s = om.alterAtCarefully(i, r.translateCapped(rn.nextIntHasty(3) - 1, rn.nextIntHasty(3) - 1, 100, 100));
                    if(s == null)
                        System.out.println("ISSUE IN RUN " + n + " AT i " + i);
                    else
                        break;
                }
                // Try commenting out the above line and uncommenting below to see the difference.
                om.alterAtCarefully(i, r.translateCapped(rn.nextIntHasty(3) - 1, rn.nextIntHasty(3) - 1, 100, 100));
                //om.alterAt(i, r.translateCapped(rn.nextIntHasty(3) - 1, rn.nextIntHasty(3) - 1, 100, 100));
            }
            for (int i = 0; i < om.size(); i++) {
                System.out.println(om.keyAt(i) + " => " + om.getAt(i));
            }
            System.out.println();
        }
    }
}