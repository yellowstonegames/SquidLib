package squidpony.squidmath;

import org.junit.Test;
import squidpony.Maker;

import java.util.HashMap;

public class BinaryHeapTest {
    private static class Node extends BinaryHeap.Node{
        public Node(float value) {
            super(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return Float.compare(((Node) o).value, value) == 0;
        }
    }
    @Test
    public void testGDXIssue5984(){
        // using the same references by looking up nodes by their values.
        // normally Float keys would be a bad idea, but making int identifiers would be a pain.
        // Maker is a SquidLib class with a static method to fill up a Map with varargs of
        // alternating keys and values.
        HashMap<Float, Node> m = Maker.makeHM(
                44.683983f, new Node(44.683983f),
                160.47682f, new Node(160.47682f),
                95.038086f, new Node(95.038086f),
                396.49918f, new Node(396.49918f),
                835.0006f, new Node(835.0006f),
                439.67096f, new Node(439.67096f),
                377.55692f, new Node(377.55692f),
                373.29028f, new Node(373.29028f),
                926.524f, new Node(926.524f),
                189.30789f, new Node(189.30789f),
                926.524f, new Node(926.524f),
                924.88995f, new Node(924.88995f),
                700.856f, new Node(700.856f),
                342.5846f, new Node(342.5846f),
                313.3819f, new Node(313.3819f),
                407.9829f, new Node(407.9829f),
                1482.5394f, new Node(1482.5394f),
                1135.7894f, new Node(1135.7894f),
                362.44937f, new Node(362.44937f),
                725.86615f, new Node(725.86615f),
                1656.2006f, new Node(1656.2006f),
                490.8201f, new Node(490.8201f),
                725.86615f, new Node(725.86615f),
                723.18396f, new Node(723.18396f),
                716.36115f, new Node(716.36115f),
                490.8201f, new Node(490.8201f),
                474.9852f, new Node(474.9852f),
                379.61304f, new Node(379.61304f),
                465.81775f, new Node(465.81775f),
                440.83838f, new Node(440.83838f),
                1690.9901f, new Node(1690.9901f),
                1711.5605f, new Node(1711.5605f),
                1795.7483f, new Node(1795.7483f),
                388.60376f, new Node(388.60376f),
                2119.6921f, new Node(2119.6921f),
                1040.5143f, new Node(1040.5143f),
                1018.3097f, new Node(1018.3097f),
                1039.8417f, new Node(1039.8417f),
                1142.326f, new Node(1142.326f),
                1045.692f, new Node(1045.692f),
                820.3383f, new Node(820.3383f),
                474.9852f, new Node(474.9852f),
                430.27383f, new Node(430.27383f),
                506.89728f, new Node(506.89728f),
                973.9379f, new Node(973.9379f),
                723.18396f, new Node(723.18396f),
                619.83624f, new Node(619.83624f),
                1656.2006f, new Node(1656.2006f),
                1547.9089f, new Node(1547.9089f),
                1018.3097f, new Node(1018.3097f),
                930.3666f, new Node(930.3666f),
                1039.8417f, new Node(1039.8417f),
                950.749f, new Node(950.749f),
                1142.326f, new Node(1142.326f),
                1055.636f, new Node(1055.636f),
                1045.692f, new Node(1045.692f),
                958.5852f, new Node(958.5852f),
                820.3383f, new Node(820.3383f),
                771.37115f, new Node(771.37115f),
                506.89728f, new Node(506.89728f),
                417.02042f, new Node(417.02042f),
                930.3666f, new Node(930.3666f),
                864.04517f, new Node(864.04517f),
                950.749f, new Node(950.749f),
                879.2704f, new Node(879.2704f),
                958.5852f, new Node(958.5852f),
                894.9335f, new Node(894.9335f),
                1534.2864f, new Node(1534.2864f),
                619.83624f, new Node(619.83624f),
                548.92786f, new Node(548.92786f),
                924.88995f, new Node(924.88995f),
                905.3478f, new Node(905.3478f),
                440.83838f, new Node(440.83838f),
                436.48087f, new Node(436.48087f),
                1040.5143f, new Node(1040.5143f),
                950.6953f, new Node(950.6953f),
                992.51624f, new Node(992.51624f),
                808.5153f, new Node(808.5153f),
                876.47845f, new Node(876.47845f),
                472.963f, new Node(472.963f),
                465.81775f, new Node(465.81775f),
                461.85135f, new Node(461.85135f),
                1552.4479f, new Node(1552.4479f),
                950.6953f, new Node(950.6953f),
                862.6192f, new Node(862.6192f),
                992.51624f, new Node(992.51624f),
                900.9059f, new Node(900.9059f),
                808.5153f, new Node(808.5153f),
                716.3565f, new Node(716.3565f),
                876.47845f, new Node(876.47845f),
                610.04565f, new Node(610.04565f),
                598.95935f, new Node(598.95935f),
                487.93192f, new Node(487.93192f),
                864.04517f, new Node(864.04517f),
                852.66907f, new Node(852.66907f),
                879.2704f, new Node(879.2704f),
                867.3523f, new Node(867.3523f),
                894.9335f, new Node(894.9335f),
                884.0505f, new Node(884.0505f),
                548.7671f, new Node(548.7671f),
                1437.1154f, new Node(1437.1154f),
                1934.038f, new Node(1934.038f),
                2401.7002f, new Node(2401.7002f),
                973.9379f, new Node(973.9379f),
                903.2409f, new Node(903.2409f),
                1547.9089f, new Node(1547.9089f),
                1481.2589f, new Node(1481.2589f),
                1430.7216f, new Node(1430.7216f)
        );
        
        BinaryHeap<Node> h = new BinaryHeap<Node>();

        h.add(m.get(44.683983f));
        if(h.pop().value != 44.683983f) throw new RuntimeException("Should be 44.683983");
        h.add(m.get(160.47682f));
        h.add(m.get(95.038086f));
        h.add(m.get(396.49918f));
        h.add(m.get(835.0006f));
        h.add(m.get(439.67096f));
        h.add(m.get(377.55692f));
        h.add(m.get(373.29028f));
        if(h.pop().value != 95.038086f) throw new RuntimeException("Should be 95.038086");
        h.add(m.get(926.524f));
        if(h.pop().value != 160.47682f) throw new RuntimeException("Should be 160.47682");
        h.add(m.get(189.30789f));
        h.remove(m.get(926.524f));
        h.add(m.get(924.88995f));
        h.add(m.get(700.856f));
        h.add(m.get(342.5846f));
        h.add(m.get(313.3819f));
        if(h.pop().value != 189.30789f) throw new RuntimeException("Should be 189.30789");
        h.add(m.get(407.9829f));
        h.add(m.get(1482.5394f));
        h.add(m.get(1135.7894f));
        h.add(m.get(362.44937f));
        if(h.pop().value != 313.3819f) throw new RuntimeException("Should be 313.3819");
        h.add(m.get(725.86615f));
        h.add(m.get(1656.2006f));
        h.add(m.get(490.8201f));
        if(h.pop().value != 342.5846f) throw new RuntimeException("Should be 342.5846");
        h.remove(m.get(725.86615f));
        h.add(m.get(723.18396f));
        h.add(m.get(716.36115f));
        h.remove(m.get(490.8201f));
        h.add(m.get(474.9852f));
        h.add(m.get(379.61304f));
        if(h.pop().value != 362.44937f) throw new RuntimeException("Should be 362.44937");
        h.add(m.get(465.81775f));
        h.add(m.get(440.83838f));
        h.add(m.get(1690.9901f));
        h.add(m.get(1711.5605f));
        h.add(m.get(1795.7483f));
        h.add(m.get(388.60376f));
        h.add(m.get(2119.6921f));
        if(h.pop().value != 373.29028f) throw new RuntimeException("Should be 373.29028");
        h.add(m.get(1040.5143f));
        h.add(m.get(1018.3097f));
        h.add(m.get(1039.8417f));
        h.add(m.get(1142.326f));
        h.add(m.get(1045.692f));
        h.add(m.get(820.3383f));
        h.remove(m.get(474.9852f));
        h.add(m.get(430.27383f));
        h.add(m.get(506.89728f));
        if(h.pop().value != 377.55692f) throw new RuntimeException("Should be 377.55692");
        h.add(m.get(973.9379f));
        h.remove(m.get(723.18396f));
        h.add(m.get(619.83624f));
        h.remove(m.get(1656.2006f));
        h.add(m.get(1547.9089f));
        if(h.pop().value != 379.61304f) throw new RuntimeException("Should be 379.61304");
        h.remove(m.get(1018.3097f));
        h.add(m.get(930.3666f));
        h.remove(m.get(1039.8417f));
        h.add(m.get(950.749f));
        h.remove(m.get(1142.326f));
        h.add(m.get(1055.636f));
        h.remove(m.get(1045.692f));
        h.add(m.get(958.5852f));
        h.remove(m.get(820.3383f));
        h.add(m.get(771.37115f));
        h.remove(m.get(506.89728f));
        h.add(m.get(417.02042f));
        if(h.pop().value != 388.60376f) throw new RuntimeException("Should be 388.60376");
        h.remove(m.get(930.3666f));
        h.add(m.get(864.04517f));
        h.remove(m.get(950.749f));
        h.add(m.get(879.2704f));
        h.remove(m.get(958.5852f));
        h.add(m.get(894.9335f));
        h.add(m.get(1534.2864f));
        if(h.pop().value != 396.49918f) throw new RuntimeException("Should be 396.49918");
        h.remove(m.get(619.83624f));
        h.add(m.get(548.92786f));
        h.remove(m.get(924.88995f));
        h.add(m.get(905.3478f));
        if(h.pop().value != 407.9829f) throw new RuntimeException("Should be 407.9829");
        h.remove(m.get(440.83838f));
        h.add(m.get(436.48087f));
        if(h.pop().value != 417.02042f) throw new RuntimeException("Should be 417.02042");
        h.remove(m.get(1040.5143f));
        h.add(m.get(950.6953f));
        h.add(m.get(992.51624f));
        h.add(m.get(808.5153f));
        if(h.pop().value != 430.27383f) throw new RuntimeException("Should be 430.27383");
        h.add(m.get(876.47845f));
        h.add(m.get(472.963f));
        if(h.pop().value != 436.48087f) throw new RuntimeException("Should be 436.48087");
        h.remove(m.get(465.81775f));
        h.add(m.get(461.85135f));
        h.add(m.get(1552.4479f));
        if(h.pop().value != 439.67096f) throw new RuntimeException("Should be 439.67096");
        if(h.pop().value != 461.85135f) throw new RuntimeException("Should be 461.85135");
        h.remove(m.get(950.6953f));
        h.add(m.get(862.6192f));
        h.remove(m.get(992.51624f));
        h.add(m.get(900.9059f));
        h.remove(m.get(808.5153f));
        h.add(m.get(716.3565f));
        h.remove(m.get(876.47845f));
        h.add(m.get(610.04565f));
        h.add(m.get(598.95935f));
        h.add(m.get(487.93192f));
        h.remove(m.get(864.04517f));
        h.add(m.get(852.66907f));
        h.remove(m.get(879.2704f));
        h.add(m.get(867.3523f));
        h.remove(m.get(894.9335f));
        h.add(m.get(884.0505f));
        if(h.pop().value != 472.963f) throw new RuntimeException("Should be 472.963");
        if(h.pop().value != 487.93192f) throw new RuntimeException("Should be 487.93192");
        h.add(m.get(548.7671f));
        if(h.pop().value != 548.7671f) throw new RuntimeException("Should be 548.7671");
        h.add(m.get(1437.1154f));
        h.add(m.get(1934.038f));
        h.add(m.get(2401.7002f));
        if(h.pop().value != 548.92786f) throw new RuntimeException("Should be 548.92786");
        h.remove(m.get(973.9379f));
        h.add(m.get(903.2409f));
        h.remove(m.get(1547.9089f));
        h.add(m.get(1481.2589f));
        if(h.pop().value != 598.95935f) throw new RuntimeException("Should be 598.95935");
        h.add(m.get(1430.7216f));
        
        // at this point in a debugger, you can tell that 610.04565 is in position 1, while 700.856 is in position 0.
        // this is incorrect, but I'm not sure at what point in the test it became incorrect.
        float popped = h.pop().value;
//        if(popped == 700.856f) throw new RuntimeException("Incorrect result! Should NOT be 700.856, should be 610.04565 .");
        if(popped != 610.04565f) throw new RuntimeException("Should be 610.04565, but is " + popped);
    }
}
