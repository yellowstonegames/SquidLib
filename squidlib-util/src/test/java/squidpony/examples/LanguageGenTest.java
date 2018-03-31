package squidpony.examples;

import org.junit.Test;
import squidpony.*;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Tommy Ettinger on 11/29/2015.
 */
public class LanguageGenTest {
    public static boolean PRINTING = true;
    @Test
    public void testOutput() {
        if(!PRINTING) return;
        StatefulRNG rng = new StatefulRNG(0xf00df00L);

        for (int langi = 0; langi < FakeLanguageGen.registered.length; langi++) {
            FakeLanguageGen flg = FakeLanguageGen.registered[langi];
            String name = FakeLanguageGen.registeredNames[langi];
            rng.setState(0xf00df00L);
            System.out.println("\nImitating language: \"" + name + "\":\n");
            for (int i = 0; i < 40; i++) {
                System.out.println(flg.sentence(rng, 4, 9, new String[]{",", ",", ",", ";"},
                        new String[]{".", ".", "!", "?", "..."}, 0.14));
            }
        }
        FakeLanguageGen flg;
        System.out.println("\nImitating language: \"Norse with simplified spelling\":\n");
        rng.setState(0xf00df00L);
        flg = FakeLanguageGen.NORSE.addModifiers(FakeLanguageGen.Modifier.SIMPLIFY_NORSE);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 4, 9, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.14));
        }

        System.out.println("\n\nLANGUAGE MIXES:\n");

        System.out.println("\nImitating language: \"English 50%, French (no accents) 50%\":\n");
        rng.setState(0xf00df00L);
        flg = FakeLanguageGen.ENGLISH.mix(FakeLanguageGen.FRENCH.removeAccents(), 0.5);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }
        System.out.println("\nImitating language: \"Russian Romanized 65%, English 35%\":\n");
        rng.setState(0xf00df00L);
        flg = FakeLanguageGen.RUSSIAN_ROMANIZED.mix(FakeLanguageGen.ENGLISH, 0.35);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 4, 10, new String[]{",", ",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "..."}, 0.22));
        }
        System.out.println("\nImitating language: \"French 45%, Greek Romanized 55%\":\n");
        rng.setState(0xf00df00L);
        flg = FakeLanguageGen.FRENCH.mix(FakeLanguageGen.GREEK_ROMANIZED, 0.55);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.22));
        }
        System.out.println("\nImitating language: \"English 75%, Greek Authentic 25%\":\n");
        rng.setState(0xf00df00L);
        flg = FakeLanguageGen.ENGLISH.mix(FakeLanguageGen.GREEK_AUTHENTIC, 0.25);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 11, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }
        System.out.println("\nImitating language: \"English with added accents\":\n");
        rng.setState(0xf00df00L);
        flg = FakeLanguageGen.ENGLISH.addAccents(0.5, 0.15);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }

        System.out.println("\nImitating language: \"French 35%, Japanese Romanized 65%\":\n");
        rng.setState(0xf00df00L);
        flg = FakeLanguageGen.FRENCH.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.65);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";"},
                    new String[]{".", ".", "!", "?", "...", "..."}, 0.17));
        }

        System.out.println("\nImitating language: \"Russian Romanized 25%, Japanese Romanized 75%\":\n");
        rng.setState(0xf00df00L);
        flg = FakeLanguageGen.RUSSIAN_ROMANIZED.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.75);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";", " -"},
                    new String[]{".", ".", ".", "!", "?", "...", "..."}, 0.2));
        }

        System.out.println("\nImitating language: \"English with no repeats of the same letter twice in a row\":\n");
        rng.setState(0xf00df00L);
        flg = FakeLanguageGen.ENGLISH.addModifiers(FakeLanguageGen.Modifier.NO_DOUBLES);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }
        System.out.println("\nImitating language: \"Japanese Romanized with frequent doubled consonants\":\n");
        rng.setState(0xf00df00L);
        flg = FakeLanguageGen.JAPANESE_ROMANIZED.addModifiers(FakeLanguageGen.Modifier.DOUBLE_CONSONANTS);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.18));
        }

        System.out.println("\nImitating language: \"Somali 63%, Japanese Romanized 27%, Swahili 10%\":\n");
        rng.setState(0xf00df00L);
        flg = FakeLanguageGen.SOMALI.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.3).mix(FakeLanguageGen.SWAHILI, 0.1);
        for (int i = 0; i < 40; i++) {
            System.out.println(flg.sentence(rng, 5, 12, new String[]{",", ",", ";"},
                    new String[]{".", ".", "!", "?", "..."}, 0.15));
        }

    }
    @Test
    public void testSentences()
    {
        if(!PRINTING) return;
        StatefulRNG rng = new StatefulRNG(0xf00df00L);
        FakeLanguageGen flg;
        System.out.println("\n\nDEFAULT SENTENCES:\n\n");
        System.out.println('"' + FakeLanguageGen.ENGLISH.sentence(rng, 4, 10,
                new String[]{" -", ",", ",", ";"}, new String[]{"!", "!", "...", "...", ".", "?"}, 0.2) + "\",");
        System.out.println('"' + FakeLanguageGen.JAPANESE_ROMANIZED.sentence(rng, 4, 7,
                new String[]{" -", ",", ",", ";"}, new String[]{"!", "!", "...", "...", ".", "?"}, 0.2) + "\",");
        System.out.println('"' + FakeLanguageGen.FRENCH.sentence(rng, 5, 8,
                new String[]{" -", ",", ",", ";"}, new String[]{"!", "?", ".", "...", ".", "?"}, 0.1) + "\",");
        System.out.println('"' + FakeLanguageGen.GREEK_ROMANIZED.sentence(rng, 5, 8,
                new String[]{",", ",", ";"}, new String[]{"!", "?", ".", "...", ".", "?"}, 0.15) + "\",");
        System.out.println('"' + FakeLanguageGen.GREEK_AUTHENTIC.sentence(rng, 5, 8,
                new String[]{",", ",", ";"}, new String[]{"!", "?", ".", "...", ".", "?"}, 0.15) + "\",");
        System.out.println('"' + FakeLanguageGen.RUSSIAN_ROMANIZED.sentence(rng, 4, 7,
                new String[]{" -", ",", ",", ",", ";"}, new String[]{"!", "!", ".", "...", ".", "?"}, 0.22) + "\",");
        System.out.println('"' + FakeLanguageGen.RUSSIAN_AUTHENTIC.sentence(rng, 4, 7,
                new String[]{" -", ",", ",", ",", ";"}, new String[]{"!", "!", ".", "...", ".", "?"}, 0.22) + "\",");
        System.out.println('"' + FakeLanguageGen.LOVECRAFT.sentence(rng, 4, 7,
                new String[]{" -", ",", ",", ";"}, new String[]{"!", "!", "...", "...", ".", "?"}, 0.2) + "\",");
        System.out.println('"' + FakeLanguageGen.SWAHILI.sentence(rng, 4, 8,
                new String[]{",", ",", ";"}, new String[]{"!", "?", ".", ".", "."}, 0.12) + "\",");
        flg = FakeLanguageGen.FRENCH.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.65);
        System.out.println('"' + flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";"},
                new String[]{".", ".", "!", "?", "...", "..."}, 0.17) + "\",");
        flg = FakeLanguageGen.ENGLISH.addAccents(0.5, 0.15);
        System.out.println('"' + flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";"},
                new String[]{".", ".", "!", "?", "...", "..."}, 0.17) + "\",");
        flg = FakeLanguageGen.RUSSIAN_AUTHENTIC.mix(FakeLanguageGen.GREEK_AUTHENTIC, 0.5).mix(FakeLanguageGen.FRENCH, 0.35);
        System.out.println('"' + flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";", " -"},
                new String[]{".", ".", "!", "?", "...", "..."}, 0.2) + "\",");
        flg = FakeLanguageGen.FANCY_FANTASY_NAME;
        System.out.println('"' + flg.sentence(rng, 6, 12, new String[]{",", ",", ",", ";", " -"},
                new String[]{".", ".", "!", "?", "...", "..."}, 0.2) + "\",");
        flg = FakeLanguageGen.SWAHILI.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.35); //.mix(FakeLanguageGen.FRENCH, 0.35)
        System.out.println('"' + flg.sentence(rng, 4, 7, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.12) + "\",");
        flg = FakeLanguageGen.SWAHILI.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.32).mix(FakeLanguageGen.FANCY_FANTASY_NAME, 0.25);
        System.out.println('"' + flg.sentence(rng, 4, 7, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.12) + "\",");
        flg = FakeLanguageGen.SOMALI.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.3).mix(FakeLanguageGen.SWAHILI, 0.15);
        System.out.println('"' + flg.sentence(rng, 4, 7, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.15) + "\",");
        flg = FakeLanguageGen.INUKTITUT;
        System.out.println('"' + flg.sentence(rng, 4, 7, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.15) + "\",");
        flg = FakeLanguageGen.NORSE;
        System.out.println('"' + flg.sentence(rng, 4, 9, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.15) + "\",");
        flg = FakeLanguageGen.NORSE.addModifiers(FakeLanguageGen.Modifier.SIMPLIFY_NORSE);
        System.out.println('"' + flg.sentence(rng, 4, 9, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.15) + "\",");
        flg = FakeLanguageGen.NAHUATL;
        System.out.println('"' + flg.sentence(rng, 3, 6, new String[]{",", ",", ";"},
                new String[]{"!", "?", ".", ".", "."}, 0.1) + "\",");
        flg = FakeLanguageGen.MONGOLIAN;
        System.out.println('"' + flg.sentence(rng, 3, 8, new String[]{",", ",", ";", ",", " -"},
                new String[]{"!", "?", ".", ".", ".", ".", "..."}, 0.16) + "\",");
        flg = FakeLanguageGen.SIMPLISH;
        System.out.println('"' + flg.sentence(rng, 4, 10, new String[]{" -", ",", ",", ";"},
                new String[]{"!", "!", "...", "...", ".", "?"}, 0.2) + "\",");
        flg = FakeLanguageGen.KOREAN_ROMANIZED;
        System.out.println('"' + flg.sentence(rng, 5, 9, new String[]{",", ",", ";", ","},
                new String[]{"!", "?", ".", ".", ".", ".", "..."}, 0.13) + "\",");
    }
    @Test
    public void testNaturalCipher()
    {
        if(!PRINTING) return;
        FakeLanguageGen[] languages = new FakeLanguageGen[]{

                FakeLanguageGen.ENGLISH,
                FakeLanguageGen.LOVECRAFT,
                FakeLanguageGen.JAPANESE_ROMANIZED,
                FakeLanguageGen.FRENCH,
                FakeLanguageGen.GREEK_ROMANIZED,
                FakeLanguageGen.GREEK_AUTHENTIC,
                FakeLanguageGen.RUSSIAN_ROMANIZED,
                FakeLanguageGen.RUSSIAN_AUTHENTIC,
                FakeLanguageGen.SWAHILI,
                FakeLanguageGen.SOMALI,
                FakeLanguageGen.FANTASY_NAME,
                FakeLanguageGen.FANCY_FANTASY_NAME,
                FakeLanguageGen.ARABIC_ROMANIZED,
                FakeLanguageGen.HINDI_ROMANIZED.removeAccents(),
                FakeLanguageGen.RUSSIAN_ROMANIZED.mix(FakeLanguageGen.SOMALI, 0.25),
                FakeLanguageGen.GREEK_ROMANIZED.mix(FakeLanguageGen.HINDI_ROMANIZED.removeAccents(), 0.5),
                FakeLanguageGen.SWAHILI.mix(FakeLanguageGen.FRENCH, 0.3),
                FakeLanguageGen.ARABIC_ROMANIZED.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.4),
                FakeLanguageGen.SWAHILI.mix(FakeLanguageGen.GREEK_ROMANIZED, 0.4),
                FakeLanguageGen.GREEK_ROMANIZED.mix(FakeLanguageGen.SOMALI, 0.4),
                FakeLanguageGen.ENGLISH.mix(FakeLanguageGen.HINDI_ROMANIZED.removeAccents(), 0.4),
                FakeLanguageGen.ENGLISH.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.4),
                FakeLanguageGen.SOMALI.mix(FakeLanguageGen.HINDI_ROMANIZED.removeAccents(), 0.4),
                FakeLanguageGen.FRENCH.addModifiers(FakeLanguageGen.modifier("([^aeiou])\\1", "$1ph", 0.3),
                        FakeLanguageGen.modifier("([^aeiou])\\1", "$1ch", 0.4),
                        FakeLanguageGen.modifier("([^aeiou])\\1", "$1sh", 0.5),
                        FakeLanguageGen.modifier("([^aeiou])\\1", "$1", 0.9)),
                FakeLanguageGen.JAPANESE_ROMANIZED.addModifiers(FakeLanguageGen.Modifier.DOUBLE_VOWELS),
                FakeLanguageGen.randomLanguage(CrossHash.hash64("Kittenish")),
                FakeLanguageGen.randomLanguage(CrossHash.hash64("Puppyspeak")),
                FakeLanguageGen.randomLanguage(CrossHash.hash64("Rabbitese")),
                FakeLanguageGen.randomLanguage(CrossHash.hash64("Rabbit Language")),
                FakeLanguageGen.randomLanguage(CrossHash.hash64("The Roar Of That Slumbering Shadow Which Mankind Wills Itself To Forget")),
                FakeLanguageGen.INUKTITUT,
                FakeLanguageGen.NORSE,
                FakeLanguageGen.NORSE.addModifiers(FakeLanguageGen.Modifier.SIMPLIFY_NORSE),
                FakeLanguageGen.NAHUATL,
                FakeLanguageGen.MONGOLIAN,
                FakeLanguageGen.SIMPLISH,
                FakeLanguageGen.KOREAN_ROMANIZED,
                FakeLanguageGen.SOMALI.addModifiers(FakeLanguageGen.modifier("([kd])h", "$1"),
                        FakeLanguageGen.modifier("([pfsgkcb])([aeiouy])", "$1l$2", 0.35),
                        FakeLanguageGen.modifier("ii", "ai"),
                        FakeLanguageGen.modifier("uu", "ia"),
                        FakeLanguageGen.modifier("([aeo])\\1", "$1"),
                        FakeLanguageGen.modifier("^x", "v"),
                        FakeLanguageGen.modifier("([^aeiou]|^)u([^aeiou]|$)", "$1a$2", 0.6),
                        FakeLanguageGen.modifier("([aeiou])[^aeiou]([aeiou])", "$1v$2", 0.06),
                        FakeLanguageGen.modifier("([aeiou])[^aeiou]([aeiou])", "$1l$2", 0.07),
                        FakeLanguageGen.modifier("([aeiou])[^aeiou]([aeiou])", "$1n$2", 0.07),
                        FakeLanguageGen.modifier("([aeiou])[^aeiou]([aeiou])", "$1z$2", 0.08),
                        FakeLanguageGen.modifier("([^aeiou])[aeiou]+$", "$1ia", 0.35),
                        FakeLanguageGen.modifier("([^aeiou])[bpdtkgj]", "$1"),
                        FakeLanguageGen.modifier("[jg]$", "th"),
                        FakeLanguageGen.modifier("g", "c", 0.92),
                        FakeLanguageGen.modifier("([aeiou])[wy]$", "$1l", 0.6),
                        FakeLanguageGen.modifier("([aeiou])[wy]$", "$1n"),
                        FakeLanguageGen.modifier("[qf]$", "l", 0.4),
                        FakeLanguageGen.modifier("[qf]$", "n", 0.65),
                        FakeLanguageGen.modifier("[qf]$", "s"),
                        FakeLanguageGen.modifier("cy", "sp"),
                        FakeLanguageGen.modifier("kl", "sk"),
                        FakeLanguageGen.modifier("qu+", "qui"),
                        FakeLanguageGen.modifier("q([^u])", "qu$1"),
                        FakeLanguageGen.modifier("cc", "ch"),
                        FakeLanguageGen.modifier("[^aeiou]([^aeiou][^aeiou])", "$1"),
                        FakeLanguageGen.Modifier.NO_DOUBLES
                ),
                FakeLanguageGen.GOBLIN,
                FakeLanguageGen.ELF,
                FakeLanguageGen.DEMONIC,
                FakeLanguageGen.INFERNAL,
                FakeLanguageGen.DRAGON,
                FakeLanguageGen.KOBOLD,
                FakeLanguageGen.ALIEN_A,
                FakeLanguageGen.ALIEN_E,
                FakeLanguageGen.ALIEN_I,
                FakeLanguageGen.ALIEN_O,
                FakeLanguageGen.ALIEN_U,
                FakeLanguageGen.INSECT,
                FakeLanguageGen.MAORI,
                FakeLanguageGen.SPANISH
        };
        String marked = "What the [?]heck?[?] Check that out will ya? It's probably nothing, but - OH [?]NO, THIS IS BAD!";
        String[] oz = new String[]{
                "Uncle Uncles Carbuncle Carbuncles Live Lives Lived Living Liver Livers Livery Liveries",
                "Dorothy lived in the midst of the great Kansas prairies, with Uncle Henry, who was a ",
                "farmer, and Aunt Em, who was the farmer's wife. Their house was small, for the ",
                "lumber to build it had to be carried by wagon many miles. There were four walls, ",
                "a floor and a roof, which made one room; and this room contained a rusty looking ",
                "cookstove, a cupboard for the dishes, a table, three or four chairs, and the beds. ",
                "Uncle Henry and Aunt Em had a big bed in one corner, and Dorothy a little bed in ",
                "another corner. There was no garret at all, and no cellar-except a small hole dug ",
                "in the ground, called a cyclone cellar, where the family could go in case one of ",
                "those great whirlwinds arose, mighty enough to crush any building in its path. It ",
                "was reached by a trap door in the middle of the floor, from which a ladder led ",
                "down into the small, dark hole.",
        }, oz2 = new String[oz.length];
        System.out.println("ORIGINAL:");
        for(String o : oz)
        {
            System.out.println(o);
        }
        System.out.println("\n\nGENERATED:\n");
        StatefulRNG sr = new StatefulRNG(2252637788195L);
        for(FakeLanguageGen lang : languages) {
            NaturalLanguageCipher cipher = new NaturalLanguageCipher(lang, 41041041L);
//            System.out.println("princess   : " + cipher.lookup("princess"));
//            System.out.println("princesses : " + cipher.lookup("princesses"));
            //LanguageCipher cipher = new LanguageCipher(FakeLanguageGen.randomLanguage(sr));
            int ctr = 0;
            System.out.println(cipher.cipherMarkup(marked));
            for (String s : oz) {
                oz2[ctr] = cipher.cipher(s);
                System.out.println(oz2[ctr++]);
            }

            HashMap<String, String> vocabulary = new HashMap<>(16);
            cipher.learnTranslations(vocabulary, "Dorothy", "farmer", "the", "room", "one", "uncle", "aunt");
            for (String s : oz2) {
                System.out.println(cipher.decipher(s, vocabulary));
            }
            System.out.println();
            for (String s : oz2) {
                System.out.println(cipher.decipher(s, cipher.reverse));
            }
            System.out.println();

            /*
            LanguageCipher cipher = new LanguageCipher(lang, 2252637788195L);
            //LanguageCipher cipher = new LanguageCipher(FakeLanguageGen.randomLanguage(sr));
            int ctr = 0;
            for (String s : oz) {
                oz2[ctr] = cipher.cipher(s);
                System.out.println(oz2[ctr++]);
            }

            HashMap<String, String> vocabulary = new HashMap<>(16);
            cipher.learnTranslations(vocabulary, "Dorothy", "farmer", "the", "room", "one", "uncle", "aunt");
            for (String s : oz2) {
                System.out.println(cipher.decipher(s, vocabulary));
            }
            System.out.println();
            for (String s : oz2) {
                System.out.println(cipher.decipher(s, cipher.reverse));
            }
            System.out.println();
            */
            /*
            cipher = new LanguageCipher(lang, 0x123456789L);
            ctr = 0;
            for (String s : oz) {
                oz2[ctr] = cipher.cipher(s);
                System.out.println(oz2[ctr++]);
            }

            vocabulary.clear();
            cipher.learnTranslations(vocabulary, "Dorothy", "farmer", "the", "room", "one", "uncle", "aunt");
            for (String s : oz2) {
                System.out.println(cipher.decipher(s, vocabulary));
            }
            System.out.println();
            for (String s : oz2) {
                System.out.println(cipher.decipher(s, cipher.reverse));
            }
            System.out.println();
            */
        }
        /*
        rng.setState(0xF00DF00L);
        flg = FakeLanguageGen.randomLanguage(CrossHash.Mist.kappa.hash64("Space Speak")).removeAccents();
        for (int i = 0; i < 100; i++) {
            System.out.print(flg.word(rng, true, Math.min(rng.between(1, 6), rng.between(2, 4))) + " ");
        }
        */
        /*
        StatefulRNG nrng = new StatefulRNG("SquidLib!");

        System.out.println(nrng.getState());
        for(FakeLanguageGen lang : languages) {
            for (int n = 0; n < 20; n++) {
                for (int i = 0; i < 4; i++) {
                    System.out.print(nrng.getState() + " : " + lang.word(nrng, false, 3) + ", ");
                }
                System.out.println();
            }
            System.out.println();
        }
        */
    }
    @Test
    public void testNameGen()
    {
        if(!PRINTING) return;
        StatefulRNG rng = new StatefulRNG(2252637788195L);
        ArrayList<String> men = new WeightedLetterNamegen(WeightedLetterNamegen.COMMON_USA_MALE_NAMES, 2, rng).generateList(50),
                women = new WeightedLetterNamegen(WeightedLetterNamegen.COMMON_USA_FEMALE_NAMES, 2, rng).generateList(50),
                family = new WeightedLetterNamegen(WeightedLetterNamegen.COMMON_USA_LAST_NAMES, 2, rng).generateList(100);
        for (int i = 0; i < 50; i++) {
            System.out.println(men.get(i) + " " + family.get(i << 1) + ", " + women.get(i) + " " + family.get(i << 1 | 1)
                    + ", " + FakeLanguageGen.SIMPLISH.word(rng, true, rng.betweenWeighted(1, rng.between(1, 4), 3)) + " " + FakeLanguageGen.SIMPLISH.word(rng, true, rng.betweenWeighted(1, 4, 3)));
        }
    }
    @Test
    public void testMarkovText() {
        if (!PRINTING) return;
        long seed = 10040L;
        String oz = "Dorothy lived in the midst of the great Kansas prairies, with Uncle Henry, who was a " +
                "farmer, and Aunt Em, who was the farmer's wife. Their house was small, for the " +
                "lumber to build it had to be carried by wagon many miles. There were four walls, " +
                "a floor and a roof, which made one room; and this room contained a rusty looking " +
                "cookstove, a cupboard for the dishes, a table, three or four chairs, and the beds. " +
                "Uncle Henry and Aunt Em had a big bed in one corner, and Dorothy a little bed in " +
                "another corner. There was no garret at all, and no cellar-except a small hole dug " +
                "in the ground, called a cyclone cellar, where the family could go in case one of " +
                "those great whirlwinds arose, mighty enough to crush any building in its path. It " +
                "was reached by a trap door in the middle of the floor, from which a ladder led " +
                "down into the small, dark hole. When Dorothy stood in the doorway and looked around, " +
                "she could see nothing but the great gray prairie on every side. Not a tree nor a house " +
                "broke the broad sweep of flat country that reached to the edge of the sky in all directions. " +
                "The sun had baked the plowed land into a gray mass, with little cracks running through it. " +
                "Even the grass was not green, for the sun had burned the tops of the long blades until they " +
                "were the same gray color to be seen everywhere. Once the house had been painted, but the sun " +
                "blistered the paint and the rains washed it away, and now the house was as dull and gray as " +
                "everything else.";
        MarkovText markovText = new MarkovText();
        markovText.analyze(oz);
        for (int i = 0; i < 40; i++) {
            System.out.println(markovText.chain(++seed, 100 + (i * 2)));
        }
        seed = 10040L;
        System.out.println();
        NaturalLanguageCipher cipher = new NaturalLanguageCipher(FakeLanguageGen.JAPANESE_ROMANIZED);
        markovText.changeNames(cipher);
        for (int i = 0; i < 40; i++) {
            System.out.println(markovText.chain(++seed, 100 + (i * 2)));
        }
    }
    @Test
    public void testMarkovObject() {
        if (!PRINTING) return;
        long seed = 10040L;
        MarkovObject<String> markovObject = new MarkovObject<>();
        String[] ozzes = {"Dorothy lived in the midst of the great Kansas prairies, with Uncle Henry, who was a " +
                "farmer, and Aunt Em, who was the farmer's wife.",
                "Their house was small, for the lumber to build it had to be carried by wagon many miles.",
                "There were four walls, a floor and a roof, which made one room; and this room contained a rusty looking" +
                        " cookstove, a cupboard for the dishes, a table, three or four chairs, and the beds.",
                "Uncle Henry and Aunt Em had a big bed in one corner, and Dorothy a little bed in another corner.",
                "There was no garret at all, and no cellar-except a small hole dug in the ground, called a cyclone " +
                        "cellar, where the family could go in case one of those great whirlwinds arose, mighty enough to crush " +
                        "any building in its path.",
                "It was reached by a trap door in the middle of the floor, from which a ladder led down into the small, dark hole.",
                "When Dorothy stood in the doorway and looked around, she could see nothing but the great gray prairie on every side.",
                "Not a tree nor a house broke the broad sweep of flat country that reached to the edge of the sky in all directions.",
                "The sun had baked the plowed land into a gray mass, with little cracks running through it.",
                "Even the grass was not green, for the sun had burned the tops of the long blades until they were the same gray color to be seen everywhere.",
                "Once the house had been painted, but the sun blistered the paint and the rains washed it away, and now " +
                        "the house was as dull and gray as everything else."
        };
        for(String o : ozzes)
        {
            markovObject.analyze(StringKit.split(o, " "));
        }
        List<String> ls = new ArrayList<>(60);
        for (int i = 0; i < 40; i++) {
            System.out.println(StringKit.join(" ", markovObject.chain(++seed, 10 + i, true, ls)));
            ls.clear();
        } 
    }

}
