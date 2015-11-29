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
        RNG rng = new RNG(new LightRNG(0xdeadbeefca77L));
        FakeLanguageGen flg = FakeLanguageGen.LOVECRAFT;
        for (int i = 0; i < 50; i++) {
            System.out.println(flg.sentence(rng, 3, 9, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "!", "?", "...", "..."}, 0.15));
        }
    }
}
