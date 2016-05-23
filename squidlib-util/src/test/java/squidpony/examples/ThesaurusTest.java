package squidpony.examples;

import squidpony.Thesaurus;
import squidpony.squidmath.StatefulRNG;

import static squidpony.Maker.makeList;

/**
 * Created by Tommy Ettinger on 5/23/2016.
 */
public class ThesaurusTest {
    public static void main(String[] args) {
        Thesaurus thesaurus = new Thesaurus(new StatefulRNG("SquidLib!"));
        thesaurus.addSynonyms(makeList("devil", "fiend", "demon", "horror", "abomination", "terror", "hellspawn"));
        thesaurus.addSynonyms(makeList("despoiler", "defiler", "blighter", "poisoner"));
        for (int i = 0; i < 12; i++) {
            System.out.println(
                    thesaurus.process("You fiend! You demon! You despoiler of creation; devil made flesh!"));
        }
    }
}
