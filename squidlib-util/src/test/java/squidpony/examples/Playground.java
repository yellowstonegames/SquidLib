package squidpony.examples;

import squidpony.squidmath.CrossHash;
import squidpony.squidmath.OrderedMap;

/**
 * This class is a scratchpad area to test things out.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Playground {

    public static void main(String... args) {
        new Playground().go();
    }

    private void go() {
        OrderedMap<String, String> normal, unusual;
        normal = new OrderedMap<>(16, 0.5f);
        unusual = new OrderedMap<>(16, 0.5f, new CrossHash.IHasher() {
            @Override
            public int hash(Object data) {
                return (data instanceof byte[]) ? CrossHash.Falcon.hash((byte[]) data) : data.hashCode();
            }

            @Override
            public boolean areEqual(Object left, Object right) {
                return false; // intentional reference equality
            }
        });

        System.out.println("normal (put 0): " + normal);
        System.out.println("unusual (put 0): " + unusual);

        normal.put("alpha", "foo");
        unusual.put("alpha", "foo");

        System.out.println("normal (put 1): " + normal);
        System.out.println("unusual (put 1): " + unusual);

        normal.put("alpha", "bar");
        unusual.put("alpha", "bar");

        System.out.println("normal (put 2): " + normal);
        System.out.println("unusual (put 2): " + unusual);

        System.out.println("unusual[0] is " + unusual.getAt(0));
        System.out.println("unusual[1] is " + unusual.getAt(1));

        System.out.println("unusual['alpha'] is " + unusual.get("alpha"));


    }

}
