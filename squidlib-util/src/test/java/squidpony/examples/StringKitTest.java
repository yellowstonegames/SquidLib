package squidpony.examples;

import org.junit.Test;
import squidpony.Maker;
import squidpony.StringKit;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tommy Ettinger on 8/9/2017.
 */
public class StringKitTest {
    //@Test
    public void testWrapping()
    {
        String[] ad = new String[]{
                // I needed some text that would make sense with unorthodox punctuation.
                // Naturally, Always Sunny in Philadelphia came to mind, with Charlie Day's
                // portrayal of a guy who shouldn't be making a local TV ad, but is anyway.
                // I don't claim any ownership of this script, only admiration for it, and
                // believe a transcript of a short segment of a work falls under fair use.
                "Charlie Kelly here, local business owner - and cat en-thu-siast! ",
                "Is your cat making too, much, noise-all-the-time? Is your cat constantly ",
                "stomping around, DRIVING you CRAZY? Is your cat clawing at your furnitures? ",
                "Think there's no answer? You're so stupid. There is! *KITTEN MITTONS* Finally, ",
                "there's an elegant, comfortable, mitten -- for cats. I couldn't hear anything. ",
                "Is your cat one-legged? Is your cat fat, skinny, or an in-between? It don't matter! ",
                "Cuz one SIZE fits all! Kitten mittens. You'll be smitten. So come on down to Paddy's ",
                "Pub. We're the hooooome of the original kitten mittens. Meeee-ow..."
        };
        String joined = StringKit.join("", ad);
        String oneWord = joined.replace(" ", "");
        List<String> lines = StringKit.wrap(joined, 32);
        lines.add("");
        StringKit.wrap(lines, oneWord, 30);
        System.out.println("0123456789ABCDEF0123456789ABCDEF");
        for(String line : lines) System.out.println(line);
        System.out.println("Done.");
    }
    @Test
    public void testHex()
    {
        assertTrue(StringKit.intFromHex("FF") == 255);
        assertTrue(StringKit.longFromHex("FF") == 255L);
        assertTrue(StringKit.intFromHex("2") == 2);
        assertTrue(StringKit.longFromHex("2") == 2L);
        assertTrue(StringKit.intFromHex("FFFFFFFF") == -1);
        assertTrue(StringKit.longFromHex("FFFFFFFFFFFFFFFF") == -1L);

        assertTrue(StringKit.intFromHex("FF".toCharArray(), 0, 2) == 255);
        assertTrue(StringKit.longFromHex("FF".toCharArray(), 0, 2) == 255L);
        assertTrue(StringKit.intFromHex("2".toCharArray(), 0, 1) == 2);
        assertTrue(StringKit.longFromHex("2".toCharArray(), 0, 1) == 2L);
        assertTrue(StringKit.intFromHex("FFFFFFFF".toCharArray(), 0, 8) == -1);
        assertTrue(StringKit.longFromHex("FFFFFFFFFFFFFFFF".toCharArray(), 0, 16) == -1L);

        // in this next section, the '0' is disregarded because it is after the specified end
        assertTrue(StringKit.intFromHex("FF0".toCharArray(), 0, 2) == 255);
        assertTrue(StringKit.longFromHex("FF0".toCharArray(), 0, 2) == 255L);
        assertTrue(StringKit.intFromHex("20".toCharArray(), 0, 1) == 2);
        assertTrue(StringKit.longFromHex("20".toCharArray(), 0, 1) == 2L);
        assertTrue(StringKit.intFromHex("FFFFFFFF0".toCharArray(), 0, 8) == -1);
        assertTrue(StringKit.longFromHex("FFFFFFFFFFFFFFFF0".toCharArray(), 0, 16) == -1L);
    }
    @Test
    public void testDec()
    {
        assertTrue(StringKit.intFromDec("255") == 255);
        assertTrue(StringKit.longFromDec("255") == 255L);
        assertTrue(StringKit.intFromDec("2") == 2);
        assertTrue(StringKit.longFromDec("2") == 2L);
        assertTrue(StringKit.intFromDec("-42") == -42);
        assertTrue(StringKit.longFromDec("-42") == -42L);

        // in this next section, the '0' is disregarded because it is after the specified end
        assertTrue(StringKit.intFromDec("2550", 0, 3) == 255);
        assertTrue(StringKit.longFromDec("2550", 0, 3) == 255L);
        assertTrue(StringKit.intFromDec("20", 0, 1) == 2);
        assertTrue(StringKit.longFromDec("20", 0, 1) == 2L);
        assertTrue(StringKit.intFromDec("-420", 0, 3) == -42);
        assertTrue(StringKit.longFromDec("-420", 0, 3) == -42L);
    }

    @Test
    public void testJoin()
    {
        assertEquals(StringKit.join(",", 1, 2, 3), "1,2,3");
        assertEquals(StringKit.join(",", 1L, 2L, 3L), "1,2,3");
        assertEquals(StringKit.join(",", "a", "b", "c"), "a,b,c");
        assertEquals(StringKit.join("", 1, 2, 3), "123");
        assertEquals(StringKit.join("", 1L, 2L, 3L), "123");
        assertEquals(StringKit.join("", "a", "b", "c"), "abc");
        assertEquals(StringKit.join(":)", 1, 2, 3), "1:)2:)3");
        assertEquals(StringKit.join(":)", 1L, 2L, 3L), "1:)2:)3");
        assertEquals(StringKit.join(":)", "a", "b", "c"), "a:)b:)c");
        assertEquals(StringKit.join(",", Maker.makeArrange("a", "b", "c")), "a,b,c");
        assertEquals(StringKit.join("", Maker.makeArrange("a", "b", "c")), "abc");
        assertEquals(StringKit.join(":)", Maker.makeArrange("a", "b", "c")), "a:)b:)c");

    }


}
