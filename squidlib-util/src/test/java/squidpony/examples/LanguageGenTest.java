package squidpony.examples;

import squidpony.FakeLanguageGen;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

/**
 * Created by Tommy Ettinger on 11/29/2015.
 */
public class LanguageGenTest {
    public static void main(String[] args)
    {
        RNG rng = new RNG(new LightRNG(0xf00df00L));
        FakeLanguageGen flg = FakeLanguageGen.LOVECRAFT;
        for (int i = 0; i < 50; i++) {
            System.out.println(flg.sentence(rng, 3, 9, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "!", "?", "...", "..."}, 0.15));
        }
        rng = new RNG(new LightRNG(0xf00df00L));
        flg = FakeLanguageGen.GREEK_ROMANIZED;
        for (int i = 0; i < 50; i++) {
            System.out.println(flg.sentence(rng, 5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.2));
        }
        rng = new RNG(new LightRNG(0xf00df00L));
        flg = FakeLanguageGen.GREEK_AUTHENTIC;
        for (int i = 0; i < 50; i++) {
            System.out.println(flg.sentence(rng, 5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.2));
        }
        rng = new RNG(new LightRNG(0xf00df00L));
        flg = FakeLanguageGen.FRENCH;
        for (int i = 0; i < 50; i++) {
            System.out.println(flg.sentence(rng, 4, 12, new String[]{",", ",", ",", ";", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.17));
        }
    }
}
