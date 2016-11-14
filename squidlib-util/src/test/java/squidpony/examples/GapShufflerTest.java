package squidpony.examples;

import squidpony.FakeLanguageGen;
import squidpony.squidmath.GapShuffler;
import squidpony.squidmath.RNG;

import java.util.ArrayList;

/**
 * Created by Tommy Ettinger on 5/21/2016.
 */
public class GapShufflerTest {

    public static void main(String[] args) {
        for (int n = 8; n <= 48; n+= 8) {
            RNG rng = new RNG("SquidLib!");
            ArrayList<String> names = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                names.add(FakeLanguageGen.ARABIC_ROMANIZED.addModifiers(FakeLanguageGen.Modifier.SIMPLIFY_ARABIC)
                        .word(rng, true, 3));
            }

            GapShuffler<String> gap = new GapShuffler<>(names, rng);
            for (int i = 0; i < 200; i++) {
                System.out.println(gap.next());
            }

            // alternate API
            /*
            GapShuffler<String> gap = new GapShuffler<>(names, rng);
            Iterator<String> it = gap.iterator();
            for (int i = 0; i < 200; i++) {
                if(it.hasNext())
                    System.out.println(it.next());
            }*/

            System.out.println("\n");
        }
    }
}
