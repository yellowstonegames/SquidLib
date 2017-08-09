package squidpony.examples;

import squidpony.StringKit;

import java.util.List;

/**
 * Created by Tommy Ettinger on 8/9/2017.
 */
public class StringKitTest {
    public static void main(String... args) {
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
}
