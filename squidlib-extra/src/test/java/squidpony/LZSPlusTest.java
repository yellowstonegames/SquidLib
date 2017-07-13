package squidpony;

import squidpony.squidmath.RNG;

/**
 * Created by Tommy Ettinger on 7/13/2017.
 */
public class LZSPlusTest {
    public static void main(String[] args)
    {
        RNG rng = new RNG(0xBE1337);
        String[] ozzes = new String[] {
                "Hello! ",
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
//                FakeLanguageGen.RUSSIAN_AUTHENTIC.sentence(rng, 7, 16),
//                FakeLanguageGen.GREEK_AUTHENTIC.sentence(rng, 7, 16),
//                FakeLanguageGen.INFERNAL.sentence(rng,7, 16),
        }, compressed = new String[ozzes.length];
        String oz = StringKit.join("", ozzes), allCompressed;
        int[] keys = Garbler.makeKeyArray(7, "There's no place like home...");
        for (int i = 0; i < ozzes.length; i++) {
            System.out.println(compressed[i] = LZSPlus.compress(ozzes[i]));
        }
        allCompressed = LZSPlus.compress(oz);
        for (int i = 0; i < ozzes.length; i++) {
            System.out.println(LZSPlus.decompress(compressed[i]));
        }
        System.out.println(LZSPlus.decompress(allCompressed));
        int olen = 0, clen = 0;
        for (int i = 0; i < ozzes.length; i++) {
            olen += ozzes[i].length();
            clen += compressed[i].length();
        }
        System.out.println("Original used " + olen + " chars, compressed used " + clen + " chars.");
        olen = oz.length();
        clen = allCompressed.length();
        System.out.println("All merged, original used " + olen + " chars, compressed used " + clen + " chars.");

    }
}
