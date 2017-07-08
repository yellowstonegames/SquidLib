package squidpony.examples;

import org.junit.Test;
import squidpony.squidmath.BeardRNG;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.RNG;

import static org.junit.Assert.assertTrue;

/**
 * Created by Tommy Ettinger on 7/7/2017.
 */
public class RNGFeatureTest {
    public static BeardRNG beard = new BeardRNG("Testing all the while...");
    public static RNG rng = new RNG(beard);
    public static final boolean PRINTING = false;
    @Test
    public void testUniqueCells(){
        int width = 31, height = 19;
        Coord[] unique = rng.getRandomUniqueCells(0, 0, width, height);
        OrderedSet<Coord> set = new OrderedSet<>(unique);
        int ctr = 0;
        for (Coord coord : unique) {
            assertTrue("Non-unique Coord: " + coord, coord.isWithin(width, height));
            if(PRINTING) {
                System.out.printf("(%02d,%02d)", coord.x, coord.y);
                if (++ctr == width) {
                    System.out.println();
                    ctr = 0;
                }
            }
        }
        // using a Set to test for uniqueness
        assertTrue(set.size() == width * height);
    }
}
