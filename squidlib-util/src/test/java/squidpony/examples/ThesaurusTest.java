package squidpony.examples;

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
            System.out.println(
                    thesaurus2.process("The small state of Ru`gen` in the Empire`noun` of Fr`gen`, ruled by Duke`noun` So`mod`gen`."));
        }
    }
}
