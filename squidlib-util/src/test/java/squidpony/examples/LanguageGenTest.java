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
        FakeLanguageGen flg = FakeLanguageGen.ENGLISH;

        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 10, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.17));
        }
        rng = new RNG(new LightRNG(0xf00df00L));
        flg = FakeLanguageGen.LOVECRAFT;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 3, 9, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "!", "?", "...", "..."}, 0.15));
        }
        rng = new RNG(new LightRNG(0xf00df00L));
        flg = FakeLanguageGen.GREEK_ROMANIZED;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.2));
        }
        rng = new RNG(new LightRNG(0xf00df00L));
        flg = FakeLanguageGen.GREEK_AUTHENTIC;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.2));
        }
        rng = new RNG(new LightRNG(0xf00df00L));
        flg = FakeLanguageGen.FRENCH;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 4, 12, new String[]{",", ",", ",", ";", ";"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.17));
        }
        rng = new RNG(new LightRNG(0xf00df00L));
        flg = FakeLanguageGen.RUSSIAN_ROMANIZED;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 6, 13, new String[]{",", ",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.25));
        }
        rng = new RNG(new LightRNG(0xf00df00L));
        flg = FakeLanguageGen.RUSSIAN_AUTHENTIC;
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 6, 13, new String[]{",", ",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.25));
        }


        rng = new RNG(new LightRNG(0xf00df00L));
        flg = FakeLanguageGen.ENGLISH.mix(FakeLanguageGen.FRENCH, 0.5);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }
        rng = new RNG(new LightRNG(0xf00df00L));
        flg = FakeLanguageGen.RUSSIAN_ROMANIZED.mix(FakeLanguageGen.ENGLISH, 0.35);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 4, 10, new String[]{",", ",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.22));
        }
        rng = new RNG(new LightRNG(0xf00df00L));
        flg = FakeLanguageGen.FRENCH.mix(FakeLanguageGen.GREEK_ROMANIZED, 0.55);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.22));
        }
        rng = new RNG(new LightRNG(0xf00df00L));
        flg = FakeLanguageGen.ENGLISH.mix(FakeLanguageGen.GREEK_AUTHENTIC, 0.25);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }
        rng = new RNG(new LightRNG(0xf00df00L));
        flg = FakeLanguageGen.ENGLISH.addAccents(0.5, 0.15);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }
    }
}
