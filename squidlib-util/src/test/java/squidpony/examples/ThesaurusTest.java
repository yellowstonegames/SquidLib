package squidpony.examples;

import squidpony.FakeLanguageGen;
import squidpony.StringKit;
import squidpony.Thesaurus;

import static squidpony.Maker.makeList;

/**
 * Created by Tommy Ettinger on 5/23/2016.
 */
public class ThesaurusTest {
    public static void main(String[] args) {
        Thesaurus thesaurus = new Thesaurus("SquidLib!"), thesaurus2  = new Thesaurus("SquidLib!");
        thesaurus.addSynonyms(makeList("devil", "fiend", "demon", "horror", "abomination", "terror", "hellspawn"));
        thesaurus.addSynonyms(makeList("despoiler", "defiler", "blighter", "poisoner"));
        thesaurus2.addKnownCategories().addFakeWords();
        for (int i = 0; i < 12; i++) {
            //System.out.println(
            //        thesaurus.process("You fiend! You demon! You despoiler of creation; devil made flesh!"));
            //System.out.println(
            //        thesaurus2.process("The small state of Ru`gen` in the Empire`noun` of Fr`gen`, ruled by Duke`noun` So`mod`gen`."));
            System.out.println(
                    thesaurus2.process("Calm`adj` Org`noun`\n"+
                            "Fancy`adj` Fr`gen` Empire`noun`\n"+
                            "Ar`jp`gen` Militia`noun`\n"+
                            "Lethal`noun` Blade`noun`\n"+
                            "Sole`adj` Empire`noun`\n"+
                            "Bandit`nouns`\n"+
                            "Forest`adj` Org`noun` of Sw`gr`gen`\n"+
                            "People's Union`noun` of Ru`so`gen`\n"+
                            "Holy`adj` En`hi`gen` Empire`noun`\n"+
                            "Fancy`adj` Militia`noun`\n"+
                            "Rage`noun` of Gr`gen`\n"+
                            "En`jp`gen` Union`noun`\n"+
                            "Tech`adj` Guard`nouns`\n"+
                            "New Bandit`nouns` of So`mod`gen`\n"+
                            "Light`noun` of Smart`noun`")
            );
        }
        System.out.println();
        for (int i = 0; i < 10; i++) {
            System.out.println(thesaurus2.makeNationName(FakeLanguageGen.JAPANESE_ROMANIZED));
            System.out.println(thesaurus2.makeNationName(FakeLanguageGen.FRENCH)            );
            System.out.println(thesaurus2.makeNationName(FakeLanguageGen.NAHUATL)           );
            System.out.println(thesaurus2.makeNationName(FakeLanguageGen.INUKTITUT)         );
            System.out.println(thesaurus2.makeNationName(FakeLanguageGen.MONGOLIAN)         );
        }
        System.out.println();
        String[] ozzes = new String[] {
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
        };
        String oz = StringKit.join("", ozzes);
        System.out.println(Thesaurus.ORK.process(oz));
    }
}
