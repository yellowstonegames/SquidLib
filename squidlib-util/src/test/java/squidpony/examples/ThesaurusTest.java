package squidpony.examples;

import squidpony.FakeLanguageGen;
import squidpony.Mnemonic;
import squidpony.StringKit;
import squidpony.Thesaurus;
import squidpony.squidmath.GWTRNG;

import java.util.TreeSet;

/**
 * Created by Tommy Ettinger on 5/23/2016.
 */
public class ThesaurusTest {
    public static void main(String[] args) {
        Thesaurus thesaurus  = new Thesaurus(System.currentTimeMillis() >>> 25); // changes seed roughly once/9 hours
        TreeSet<String> synonyms = new TreeSet<>();
        int len = thesaurus.mappings.size();
        for (int i = 0; i < len; i++) {
            System.out.print(thesaurus.mappings.keyAt(i) + " : ");
            synonyms.clear();
            thesaurus.mappings.getAt(i).fillInto(synonyms);
            System.out.println(StringKit.join(", ", synonyms));
        }
        System.out.println();
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
        thesaurus.refresh(thesaurus.rng.nextLong());
        thesaurus.defaultLanguage = FakeLanguageGen.mixAll(FakeLanguageGen.randomLanguage(thesaurus.rng).removeAccents(), 1, FakeLanguageGen.SIMPLISH, 3, FakeLanguageGen.MALAY, 4);
        StringBuilder text = new StringBuilder("The elixir you desire is no simple matter to brew. " +
                "I would require many ingredients, most rare or exotic, to even begin to craft this tonic. " +
                "If you remain undaunted... I require ");
        for (int i = 0; i < 10; i++) {
            text.append("leaf`noun` of plant`term`, ");
        }
        text.append("and leaf`noun` of plant`term`. Bring me this before the winter equinox and you shall have your potion." +
                "\n...Oh, and could you also pick me up a fruit`term`, maybe a fruit`term`, uh, a nut`term`, a nut`term`, " +
                "a flower`term`, and maybe a flower`term`, if it's not too much trouble.");
//        text.append("and ").append(thesaurus.lookup("leaf`noun`")).append(" of ").append(thesaurus.makePlantName(lang))
//                .append(". Bring me this before the winter equinox and you shall have your potion.")
//                .append("\n...Oh, and could you also pick me up a ").append(thesaurus.makeFruitName(lang)).append(", maybe a ")
//                .append(thesaurus.makeFruitName(lang))
//                .append(", uh, a ").append(thesaurus.makeNutName(lang)).append(", a ").append(thesaurus.makeNutName(lang))
//                .append(", a ").append(thesaurus.makeFlowerName(lang)).append(", and maybe a ").append(thesaurus.makeFlowerName(lang))
//                .append(", if it's not too much trouble.");
//        System.out.println(text);
        text.append('\n');
        text.append("\nYou have slain the ").append(FakeLanguageGen.DEMONIC.word(thesaurus.rng, true, 1, 2))
                .append("!\nYou loot its body, and find potion`term`, potion`term`, potion`term`, and potion`term`.");
        text.append('\n');
        text.append("For the emperor`noun`, and for ").append(thesaurus.makeNationName()).append('!');
        for(String s : StringKit.wrap(thesaurus.process(text), 80))
        {
            System.out.println(s);
        }


        Mnemonic[] m = {new Mnemonic(0), new Mnemonic(1), new Mnemonic(2), new Mnemonic(3)};
        for (int i = 0; i < 10; i++) {
            int a = GWTRNG.determineInt(i);
            for (int j = 0; j < m.length; j++) {
                String words = m[j].toWordMnemonic(i, true);
                System.out.println("Mnemonic(" + j + "), encoding " + StringKit.hex(i) + ": "+ words + ", decoding to " + StringKit.hex(m[j].fromWordMnemonic(words)));
            }
        }
        for (int i = 0; i < 10; i++) {
            int a = GWTRNG.determineInt(i);
            for (int j = 0; j < m.length; j++) {
                String words = m[j].toWordMnemonic(a, true);
                System.out.println("Mnemonic(" + j + "), encoding " + StringKit.hex(a) + ": "+ words + ", decoding to " + StringKit.hex(m[j].fromWordMnemonic(words)));
            }
        }
    }
}
