package squidpony.squidtext.nolithiusgen;

import java.util.LinkedHashMap;
import squidpony.annotation.Beta;

/**
 * Keeps track of what letter occurs after some other letter.
 *
 * Based on work by Nolithius available at the following two sites
 * https://github.com/Nolithius/weighted-letter-namegen
 * http://code.google.com/p/weighted-letter-namegen/
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class WeightedLetterGroup {

    public LinkedHashMap<Character, Integer> sequences = new LinkedHashMap<>();
    private char[] samples = new char[0];
    private boolean expanded = true;

    public void add(char sequence) {
        expanded = false;
        if (sequences.containsKey(sequence)) {
            sequences.put(sequence, sequences.get(sequence) + 1);
        } else {
            sequences.put(sequence, 1);
        }
    }

    /**
     * Turn the letter set into a single array.
     */
    public char[] expandSamples() {
        if (expanded) {
            return samples;
        }

        String chars = "";
        for (char cs : sequences.keySet()) {
            for (int i = 0; i < sequences.values().size(); i++) {
                chars += cs;
            }
        }
        samples = chars.toCharArray();
        expanded = true;
        return samples;
    }

}
