package squidpony.examples;

import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.Thesaurus;

/**
 * Created by Tommy Ettinger on 5/23/2016.
 */
public class ThesaurusTest {
    public static void main(String[] args) {
        Thesaurus thesaurus  = new Thesaurus("SQUID! LIB!");
//        Thesaurus thesaurus = new Thesaurus();
//        thesaurus.addSynonyms(makeList("devil", "fiend", "demon", "horror", "abomination", "terror", "hellspawn"));
//        thesaurus.addSynonyms(makeList("despoiler", "defiler", "blighter", "poisoner"));
//        thesaurus.addKnownCategories().addFakeWords();
//        for (int i = 0; i < 12; i++) {
//            //System.out.println(
//            //        thesaurus.process("You fiend! You demon! You despoiler of creation; devil made flesh!"));
//            //System.out.println(
//            //        thesaurus.process("The small state of Ru`gen` in the Empire`noun` of Fr`gen`, ruled by Duke`noun` So`mod`gen`."));
//            System.out.println(
//                    thesaurus.process("Calm`adj` Org`noun`\n"+
//                            "Fancy`adj` Fr`gen` Empire`noun`\n"+
//                            "Ar`jp`gen` Militia`noun`\n"+
//                            "Lethal`noun` Blade`noun`\n"+
//                            "Sole`adj` Empire`noun`\n"+
//                            "Bandit`nouns`\n"+
//                            "Forest`adj` Org`noun` of Sw`gr`gen`\n"+
//                            "People's Union`noun` of Ru`so`gen`\n"+
//                            "Holy`adj` En`hi`gen` Empire`noun`\n"+
//                            "Fancy`adj` Militia`noun`\n"+
//                            "Rage`noun` of Gr`gen`\n"+
//                            "En`jp`gen` Union`noun`\n"+
//                            "Tech`adj` Guard`nouns`\n"+
//                            "New Bandit`nouns` of So`mod`gen`\n"+
//                            "Light`noun` of Smart`noun`")
//            );
//        }
//        System.out.println();
//        for (int i = 0; i < 10; i++) {
//            System.out.println(thesaurus.makeNationName(FakeLanguageGen.JAPANESE_ROMANIZED));
//            System.out.println(thesaurus.makeNationName(FakeLanguageGen.FRENCH)            );
//            System.out.println(thesaurus.makeNationName(FakeLanguageGen.NAHUATL)           );
//            System.out.println(thesaurus.makeNationName(FakeLanguageGen.INUKTITUT)         );
//            System.out.println(thesaurus.makeNationName(FakeLanguageGen.MONGOLIAN)         );
//        }
//        System.out.println();
//        String[] ozzes = new String[] {
//                "Dorothy lived in the midst of the great Kansas prairies, with Uncle Henry, who was a ",
//                "farmer, and Aunt Em, who was the farmer's wife. Their house was small, for the ",
//                "lumber to build it had to be carried by wagon many miles. There were four walls, ",
//                "a floor and a roof, which made one room; and this room contained a rusty looking ",
//                "cookstove, a cupboard for the dishes, a table, three or four chairs, and the beds. ",
//                "Uncle Henry and Aunt Em had a big bed in one corner, and Dorothy a little bed in ",
//                "another corner. There was no garret at all, and no cellar-except a small hole dug ",
//                "in the ground, called a cyclone cellar, where the family could go in case one of ",
//                "those great whirlwinds arose, mighty enough to crush any building in its path. It ",
//                "was reached by a trap door in the middle of the floor, from which a ladder led ",
//                "down into the small, dark hole.",
//        };
//        String oz = StringKit.join("", ozzes);
//        System.out.println(Thesaurus.ORK.process(oz));
//        System.out.println();
        thesaurus.addKnownCategories();
        StringBuilder text = new StringBuilder("The elixir you desire is no simple matter to brew. I would require many ingredients, most rare or exotic, to even begin to craft this tonic. If you remain undaunted... I require ");
        thesaurus.refresh(thesaurus.rng.nextLong());
        FakeLanguageGen lang = FakeLanguageGen.mixAll(FakeLanguageGen.GREEK_ROMANIZED, 4, FakeLanguageGen.SIMPLISH, 2, FakeLanguageGen.MALAY, 1);
        for (int i = 0; i < 10; i++) {
            text.append(thesaurus.lookup("leaf`noun`")).append(" of ").append(thesaurus.makePlantName(lang)).append(", ");
        }
        text.append("and ").append(thesaurus.lookup("leaf`noun`")).append(" of ").append(thesaurus.makePlantName(lang)).append(". Bring me this before the winter equinox and you shall have your potion.")
//                .append(" Look for ").append(thesaurus.makePotionDescription()).append(", or maybe ")
//                .append(thesaurus.makePotionDescription()).append(", if you're lucky.")
                .append("\n...Oh, and could you also pick me up a ").append(thesaurus.makeFruitName(lang)).append(", maybe a ").append(thesaurus.makeFruitName(lang))
                .append(", uh, a ").append(thesaurus.makeNutName(lang)).append(", a ").append(thesaurus.makeNutName(lang))
                .append(", a ").append(thesaurus.makeFlowerName(lang)).append(", and maybe a ").append(thesaurus.makeFlowerName(lang)).append(", if it's not too much trouble.");
//        System.out.println(text);
        text.append("\nYou have slain the ").append(FakeLanguageGen.DEMONIC.word(thesaurus.rng, true))
                .append("!\nYou loot its body, and find ")
                .append(thesaurus.makePotionDescription()).append(", ")
                .append(thesaurus.makePotionDescription()).append(", ")
                .append(thesaurus.makePotionDescription()).append(", and ")
                .append(thesaurus.makePotionDescription()).append(".");
        for(String s : StringKit.wrap(text, 80))
        {
            System.out.println(s);
        }
//        for (int i = 0; i < 10; i++) {
//            System.out.println(FakeLanguageGen.removeAccents(thesaurus.makePlantName())    );
//            System.out.println(FakeLanguageGen.removeAccents(thesaurus.makePlantName())    );
//            System.out.println(FakeLanguageGen.removeAccents(thesaurus.makePlantName())    );
//            System.out.println(thesaurus.makePlantName(FakeLanguageGen.JAPANESE_ROMANIZED));
//            System.out.println(thesaurus.makePlantName(FakeLanguageGen.MALAY)             );
//            System.out.println(thesaurus.makePlantName(FakeLanguageGen.SIMPLISH)          );
//        }
    }
}
