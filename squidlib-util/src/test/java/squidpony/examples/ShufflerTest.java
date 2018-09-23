package squidpony.examples;

import org.junit.Test;
import squidpony.FakeLanguageGen;
import squidpony.squidmath.GapShuffler;
import squidpony.squidmath.LowStorageShuffler;
import squidpony.squidmath.RNG;

import java.util.ArrayList;

import static squidpony.examples.LanguageGenTest.PRINTING;

/**
 * Created by Tommy Ettinger on 5/21/2016.
 */
public class ShufflerTest {

    @Test
    public void testGapShuffler() {
        for (int n = 8; n <= 48; n+= 8) {
            RNG rng = new RNG("SquidLib!");
            ArrayList<String> names = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                names.add(FakeLanguageGen.FANTASY_NAME.word(rng, true, 3));
            }
            
            GapShuffler<String> gap = new GapShuffler<>(names, rng);
            String name;
            for (int i = 0; i < 200; i++) {
                name = gap.next();
                if(PRINTING) 
                    System.out.println(name);
            }

            // alternate API
            /*
            GapShuffler<String> gap = new GapShuffler<>(names, rng);
            Iterator<String> it = gap.iterator();
            for (int i = 0; i < 200; i++) {
                if(it.hasNext())
                    System.out.println(it.next());
            }*/

            if(PRINTING)
                System.out.println("\n");
        }
    }
    @Test
    public void testLSSBounds()
    {
        for (int i = 2; i <= 42; i++) {
            LowStorageShuffler lss = new LowStorageShuffler(i, 0x31337);
            System.out.printf("Bound %02d: %d", i, lss.next());
            for (int j = 1; j < i; j++) {
                System.out.print(", " + lss.next());
            }
            System.out.println();
        }
    }
    @Test
    public void testLSSReseed()
    {
        LowStorageShuffler lss = new LowStorageShuffler(7, 0);
        for (int i = 0; i < 30; i++) {
            lss.restart(i);
            System.out.printf("Seed %08X: %d", i, lss.next());
            for (int j = 1; j < 7; j++) {
                System.out.print(", " + lss.next());
            }
            System.out.println();
        }
    }
}
