package squidpony.examples;

import squidpony.FakeLanguageGen;
import squidpony.MonsterGen;
import squidpony.squidmath.StatefulRNG;

/**
 * Created by Tommy Ettinger on 11/29/2015.
 */
public class MonsterGenTest {
    public static void main(String[] args)
    {
        StatefulRNG rng = new StatefulRNG(0xf00df00L);

        rng.setState(0xbababadaL);
        FakeLanguageGen flg = FakeLanguageGen.GREEK_ROMANIZED.mix(
                FakeLanguageGen.RUSSIAN_ROMANIZED.mix(
                        FakeLanguageGen.FRENCH.removeAccents().mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.6), 0.8),
                0.85); //.addAccents(0.4, 0.1);
        //for (int i = 0; i < 40; i++) {
            //System.out.println("The " + flg.word(rng, true, rng.between(2, 4)) + " approaches!");
        //}
        MonsterGen mg = new MonsterGen();
        System.out.println(MonsterGen.SNAKE.present(true));
        System.out.println(MonsterGen.SNAKE.presentVisible(true));
        System.out.println(MonsterGen.LION.present(true));
        System.out.println(MonsterGen.LION.presentVisible(true));
        System.out.println(MonsterGen.HAWK.present(true));
        System.out.println(MonsterGen.HAWK.presentVisible(true));
        System.out.println(MonsterGen.HORSE.present(true));
        System.out.println(MonsterGen.HORSE.presentVisible(true));
        System.out.println(MonsterGen.SHOGGOTH.present(true));
        System.out.println(MonsterGen.SHOGGOTH.presentVisible(true));
        System.out.println(MonsterGen.SNAKE.mix(rng, "slitherking", MonsterGen.LION, 0.5).present(true));
        System.out.println(MonsterGen.LION.mix(rng, "slion", MonsterGen.SNAKE, 0.3).present(true));
        System.out.println(MonsterGen.HORSE.mix(rng, "elderhorse", MonsterGen.SHOGGOTH, 0.35).present(true));
        System.out.println(mg.randomizeAppearance(rng, MonsterGen.HAWK, mg.randomName(rng), 3).present(true));
        System.out.println(mg.randomizePowers(rng, MonsterGen.HAWK, mg.randomName(rng), 3).present(true));



        //For generating the random guard interjections in some demos
        /*
        System.out.println();
        rng.setState(0xBEEFF00DC00L);
        for (int i = 0; i < 80; i++) {
            System.out.println("\"" + FakeLanguageGen.RUSSIAN_AUTHENTIC.sentence(rng, 1, 4,
                    new String[]{",", ",", ",", " -"}, new String[]{"!"}, 0.2, 60) + "\",");
        }
        */
    }
}
