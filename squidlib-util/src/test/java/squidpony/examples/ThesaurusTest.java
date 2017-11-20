package squidpony.examples;

import squidpony.FakeLanguageGen;
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
    }
}
