package squidpony.squidtext.namegen.weightedletter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Keeps track of what letter occurs after some other letter.
 *
 * Based on work by Nolithius available at the following two sites
 * https://github.com/Nolithius/weighted-letter-namegen
 * http://code.google.com/p/weighted-letter-namegen/
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class WeightedLetterGroup {

    public LinkedHashMap<CharSequence, Integer> sequences = new LinkedHashMap<>();

    public void add(CharSequence sequence) {
        if (!sequences.containsKey(sequence)) {
            sequences.put(sequence, sequences.get(sequence) + 1);
        }
    }

    /**
     * Turn the letter set into a single array.
     */
    public CharSequence[] expandSamples() {
        ArrayList<CharSequence> chars = new ArrayList<>();
        for (CharSequence cs : sequences.keySet()) {
            for (int i = 0; i < sequences.values().size(); i++) {
                chars.add(cs);
            }
        }
        return (CharSequence[])chars.toArray();
    }
}
