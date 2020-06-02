package squidpony;

import regexodus.Category;
import regexodus.Matcher;
import regexodus.Pattern;
import squidpony.squidmath.Arrangement;
import squidpony.squidmath.IntIntOrderedMap;
import squidpony.squidmath.IntVLA;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A simple Markov chain text generator; call {@link #analyze(CharSequence)} once on a large sample text, then you can
 * call {@link #chain(long)} many times to get odd-sounding "remixes" of the sample text. This is an order-2 Markov
 * chain, so it chooses the next word based on the previous two words; {@link MarkovTextLimited} is an order-1 Markov
 * chain, and is faster, but produces lousy output because it only uses one previous word. This is meant to allow easy
 * serialization of the necessary data to call chain(); if you can store the {@link #words} and {@link #processed}
 * arrays in some serialized form, then you can reassign them to the same fields to avoid calling analyze(). One way to
 * do this conveniently is to use {@link #serializeToString()} after calling analyze() once and to save the resulting
 * String; then, rather than calling analyze() again on future runs, you would call
 * {@link #deserializeFromString(String)} to create the MarkovText without needing any repeated analysis.
 * <br>
 * Created by Tommy Ettinger on 1/30/2018.
 */
public class MarkovText implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * All words (case-sensitive and counting some punctuation as part of words) that this encountered during the latest
     * call to {@link #analyze(CharSequence)}. Will be null if {@link #analyze(CharSequence)} was never called.
     */
    public String[] words;

    /**
     * Map of all pairs of words encountered to the position in the order they were encountered. Pairs are stored using
     * their 16-bit {@link #words} indices placed into the most-significant bits for the first word and the
     * least-significant bits for the second word. The size of this IntIntOrderedMap is likely to be larger than the
     * String array {@link #words}, but should be equal to {@code processed.length}. Will be null if
     * {@link #analyze(CharSequence)} was never called.
     */
    public IntIntOrderedMap pairs;
    /**
     * Complicated data that mixes probabilities of words using their indices in {@link #words} and the indices of word
     * pairs in {@link #pairs}, generated during the latest call to {@link #analyze(CharSequence)}. This is a jagged 2D
     * array. Will be null if {@link #analyze(CharSequence)} was never called.
     */
    public int[][] processed;

    private static final String INITIAL = "", FULL_STOP = ".", EXCLAMATION = "!", QUESTION = "?", ELLIPSIS = "...";
    private static final Matcher matcher = Pattern.compile("\\.\\.\\.|[\\.!\\?]|[^\\.!\\?\"\\(\\)\\[\\]\\{\\}\\s]+").matcher();
    public MarkovText()
    {
    }

    /**
     * This is the main necessary step before using a MarkovText; you must call this method at some point before you can
     * call any other methods. You can serialize this MarkovText after calling to avoid needing to call this again on later
     * runs, or even include serialized MarkovText objects with a game to only need to call this during pre-processing.
     * This method analyzes the pairings of words in a (typically large) corpus text, including some punctuation as part
     * of words and some kinds as their own "words." It only uses one preceding word to determine the subsequent word.
     * When it finishes processing, it stores the results in {@link #words} and {@link #processed}, which allows other
     * methods to be called (they will throw a {@link NullPointerException} if analyze() hasn't been called).
     * @param corpus a typically-large sample text in the style that should be mimicked
     */
    public void analyze(CharSequence corpus)
    {
        Arrangement<String> body = new Arrangement<>((corpus.length() >> 4) + 5);
        pairs = new IntIntOrderedMap(corpus.length() / 5 + 5);
        ArrayList<IntVLA> working = new ArrayList<>(corpus.length() / 5 + 5);
        body.add(INITIAL);
        working.add(new IntVLA(128));
        pairs.put(0, 0);
        body.add(FULL_STOP);
        body.add(EXCLAMATION);
        body.add(QUESTION);
        body.add(ELLIPSIS);
//        working.add(new IntVLA(links));

        matcher.setTarget(corpus);
        int current = 0, pair = 0, pre = 0, post;
        while (matcher.find())
        {
            current = body.addOrIndex(matcher.group());
            pair = pair << 16 | (current & 0xFFFF);
            post = pairs.putIfAbsent(pair, pairs.size());
            if(working.size() != pairs.size())
            {
                working.add(new IntVLA(16));
            }
            working.get(pre).add(current);
            if(current > 0 && current < 5)
            {
                working.get(post).add(0);
                pair = 0;
                pre = 0;
            }
            else
            {
                pre = post;
            }
        }
        IntVLA w = working.get(pre), v;
        if(w.size == 0) w.add(0);
        final int len = body.size(), pairLen = working.size();
        words = new String[len];
        body.keySet().toArray(words);

        processed = new int[pairLen][];
        w = new IntVLA(128);
        IntVLA small = new IntVLA(128);
        IntVLA large = new IntVLA(128);
        IntVLA probabilities = new IntVLA(128);
        for(int iv = 0; iv < pairLen; iv++ )
        {
            v = working.get(iv);
            w.clear();
            probabilities.clear();
            if(v.size <= 0)
            {
                v.add(1);
            }
            int vv, sum = 0;
            final int vs = v.size;
            OUTER:
            for (int i = 0; i < vs; ++i) {
                vv = v.get(i);
                for (int j = 0; j < w.size; j++) {
                    if (w.get(j) == vv) {
                        probabilities.incr(j, 0x10000);
                        sum += 0x10000;
                        continue OUTER;
                    }
                }
                w.add(vv);
                probabilities.add(0x10000);
                sum += 0x10000;
            }
            int iAverage = (sum / w.size);

            small.clear();
            large.clear();
            /* Populate the stacks with the input probabilities. */
            for (int i = 0; i < probabilities.size; i++) {
                /* If the probability is below the average probability, then we add
                 * it to the small list; otherwise we add it to the large list.
                 */
                if (probabilities.get(i) >= iAverage)
                    large.add(i);
                else
                    small.add(i);
            }

            processed[iv] = new int[w.size * 3];

            while (!small.isEmpty() && !large.isEmpty()) {
                /* Get the index of the small and the large probabilities. */
                int less = small.pop(), less2 = less * 3;
                int more = large.pop();

                /* These probabilities have not yet been scaled up to be such that
                 * sum/n is given weight 1.0.  We do this here instead.
                 */
                processed[iv][less2] = (probabilities.size * probabilities.get(less)) / (sum >> 16);
                processed[iv][less2+1] = w.get(less);
                processed[iv][less2+2] = w.get(more);
                vv = probabilities.get(less) - iAverage;
                probabilities.incr(more, vv);
                if (probabilities.get(more) >= iAverage)
                    large.add(more);
                else
                    small.add(more);
            }
            int t;
            while (!small.isEmpty())
            {
                processed[iv][(t = small.pop()) * 3] = 0xFFFF;
                processed[iv][t * 3 + 1] = processed[iv][t * 3 + 2] = w.get(t);
            }
            while (!large.isEmpty())
            {
                processed[iv][(t = large.pop()) * 3] = 0xFFFF;
                processed[iv][t * 3 + 1] = processed[iv][t * 3 + 2] = w.get(t);
            }
        }
    }

    /**
     * After calling {@link #analyze(CharSequence)}, you can optionally call this to alter any words in this MarkovText that
     * were used as a proper noun (determined by whether they were capitalized in the middle of a sentence), changing
     * them to a ciphered version using the given {@link NaturalLanguageCipher}. Normally you would initialize a
     * NaturalLanguageCipher with a {@link FakeLanguageGen} that matches the style you want for all names in this text,
     * then pass that to this method during pre-processing (not necessarily at runtime, since this method isn't
     * especially fast if the corpus was large). This method modifies this MarkovText in-place.
     * @param translator a NaturalLanguageCipher that will be used to translate proper nouns in this MarkovText's word array
     */
    public void changeNames(NaturalLanguageCipher translator)
    {
        String name;
        PER_WORD:
        for (int i = 5; i < words.length; i++) {
            if(Category.Lu.contains((name = words[i]).charAt(0)))
            {
                for (int w = 5; w < words.length; w++) {
                    for (int p = 0; p < processed[w].length; p++) {
                        if (i == processed[w][++p] || i == processed[w][++p])
                        {
                            words[i] = translator.cipher(name);
                            continue PER_WORD;
                        }
                    }
                }
            }
        }
    }
    /**
     * Generate a roughly-sentence-sized piece of text based on the previously analyzed corpus text (using
     * {@link #analyze(CharSequence)}) that terminates when stop punctuation is used (".", "!", "?", or "..."), or once
     * the length would be greater than 200 characters without encountering stop punctuation(it terminates such a
     * sentence with "." or "...").
     * @param seed the seed for the random decisions this makes, as a long; any long can be used
     * @return a String generated from the analyzed corpus text's word placement, usually a small sentence
     */
    public String chain(long seed) {
        return chain(seed, 200);
    }

    /**
     * Generate a roughly-sentence-sized piece of text based on the previously analyzed corpus text (using
     * {@link #analyze(CharSequence)}) that terminates when stop punctuation is used (".", "!", "?", or "...") or once
     * the maxLength would be exceeded by any other words (it terminates such a sentence with "." or "...").
     * @param seed the seed for the random decisions this makes, as a long; any long can be used
     * @param maxLength the maximum length for the generated String, in number of characters
     * @return a String generated from the analyzed corpus text's word placement, usually a small sentence
     */
    public String chain(long seed, int maxLength) {
        int before = 0, pair = 0;
        boolean later;
        long state;
        StringBuilder sb = new StringBuilder(1000);
        int[] rf;
        while (sb.length() < maxLength) {
            if(sb.length() >= maxLength - 3)
            {
                sb.append('.');
                break;
            }
            later = (pair != 0);
            rf = processed[pairs.get(pair)];
            // This is LightRNG's algorithm to generate a random long given sequential states
            state = ((state = ((state = ((seed += 0x9E3779B97F4A7C15L) ^ seed >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31);
            // get a random int (using half the bits of our previously-calculated state) that is less than size
            int column = (int) ((rf.length * (state & 0xFFFFFFFFL)) / 0x300000000L) * 3; // divide by 2^32, round down to multiple of 3
            // use the other half of the bits of state to get a double, compare to probability and choose either the
            // current column or the alias for that column based on that probability
            //before = ((state >>> 33) > rf[column]) ? rf[column + 1] : rf[column + 2];
            if((state >>> 48) > rf[column])
                before = rf[column + 1];
            else
                before = rf[column + 2];
            if(before >= 5)
            {
                if(sb.length() + words[before].length() + 1 < maxLength)
                {
                    if(later)
                        sb.append(' ');
                    sb.append(words[before]);
                    pair = pair << 16 | (before & 0xFFFF);
                }
                else
                {
                    if(sb.length() + 3 <= maxLength)
                        sb.append("...");
                    else
                        sb.append('.');
                    break;
                }
            }
            else if(before != 0)
            {
                sb.append(words[before]);
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Returns a representation of this MarkovText as a String; use {@link #deserializeFromString(String)} to get a
     * MarkovText back from this String. The {@link #words} and {@link #processed} fields must have been given values by
     * either direct assignment, calling {@link #analyze(CharSequence)}, or building this MarkovTest with the
     * aforementioned deserializeToString method. Uses spaces to separate words and a tab to separate the two fields.
     * @return a String that can be used to store the analyzed words and frequencies in this MarkovText
     */
    public String serializeToString()
    {
        return StringKit.join(" ", words) + "\t" + StringKit.join(",", pairs.keysAsArray()) + "\t" + Converters.convertArrayInt2D.stringify(processed);
    }

    /**
     * Recreates an already-analyzed MarkovText given a String produced by {@link #serializeToString()}.
     * @param data a String returned by {@link #serializeToString()}
     * @return a MarkovText that is ready to generate text with {@link #chain(long)}
     */
    public static MarkovText deserializeFromString(String data)
    {
        int split = data.indexOf('\t');
        MarkovText markov = new MarkovText();
        markov.words = StringKit.split(data.substring(0, split), " ");
        int[] arr = Converters.convertArrayInt.restore(data.substring(split+1, split = data.indexOf('\t', split + 1)));
        markov.pairs = new IntIntOrderedMap(arr, ArrayTools.range(arr.length));
        markov.processed = Converters.convertArrayInt2D.restore(data.substring(split + 1));
        return markov;
    }

    /**
     * Copies the String array {@link #words} and the 2D jagged int array {@link #processed} into a new MarkovText.
     * None of the arrays will be equivalent references, but the Strings (being immutable) will be the same objects in
     * both MarkovText instances. This is primarily useful with {@link #changeNames(NaturalLanguageCipher)}, which can
     * produce several variants on names given several initial copies produced with this method.
     * @return a copy of this MarkovText
     */
    public MarkovText copy()
    {
        MarkovText other = new MarkovText();
        other.words = new String[words.length];
        System.arraycopy(words, 0, other.words, 0, words.length);
        other.processed = new int[processed.length][];
        int len;
        for (int i = 0; i < processed.length; i++) {
            other.processed[i] = new int[len = processed[i].length];
            System.arraycopy(processed[i], 0, other.processed[i], 0, len);
        }
        return other;
    }
}
