package squidpony;

import regexodus.Matcher;
import regexodus.Pattern;
import regexodus.REFlags;
import squidpony.annotation.Beta;
import squidpony.squidmath.Arrangement;
import squidpony.squidmath.IntVLA;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A simple Markov chain text generator; call {@link #analyze(CharSequence)} once on a large sample text, then you can
 * call {@link #chain(long)} many times to get odd-sounding "remixes" of the sample text. This is meant to allow easy
 * serialization of the necessary data to call chain(); if you can store the {@link #words} and {@link #processed}
 * arrays in some serialized form, then you can reassign them to the same fields to avoid calling analyze().
 * <br>
 * Created by Tommy Ettinger on 1/30/2018.
 */
@Beta
public class Markov implements Serializable {
    private static final long serialVersionUID = 0L;
    private Arrangement<String> body;
    private ArrayList<IntVLA> working;

    public String[] words;
    public int[][] processed;

    private static final String INITIAL = "", FULL_STOP = ".", EXCLAMATION = "!", QUESTION = "?", ELLIPSIS = "...";
    private static final Matcher matcher = Pattern.compile("\\.\\.\\.|[\\.!\\?]|[^\\.!\\?\"\\(\\)\\[\\]\\{\\}\\s]+", REFlags.IGNORE_CASE | REFlags.UNICODE).matcher();
    public Markov()
    {
        body = new Arrangement<>(1024);
        body.add(INITIAL);
        body.add(FULL_STOP);
        body.add(EXCLAMATION);
        body.add(QUESTION);
        body.add(ELLIPSIS);
        working = new ArrayList<>(1024);
        working.add(new IntVLA(128));
        int[] links = {0};
        working.add(new IntVLA(links));
        working.add(new IntVLA(links));
        working.add(new IntVLA(links));
        working.add(new IntVLA(links));
    }
    public void analyze(CharSequence corpus)
    {
        matcher.setTarget(corpus);
        int previous = 0, current;
        while (matcher.find())
        {
            current = body.addOrIndex(matcher.group());
            if(working.size() != body.size())
            {
                working.add(new IntVLA(16));
            }
            working.get(previous).add(current);
            if(current > 0 && current < 5)
            {
                working.get(current).add(0);
                previous = 0;
            }
            else
                previous = current;
        }
        IntVLA w = working.get(previous), v;
        if(w.size == 0) w.add(0);
        final int len = working.size();
        words = new String[len];
        body.keySet().toArray(words);
        processed = new int[len][];
        w = new IntVLA(128);
        IntVLA small = new IntVLA(128);
        IntVLA large = new IntVLA(128);
        IntVLA probabilities = new IntVLA(128);
        for(int iv = 0; iv < len; iv++ )
        {
            v = working.get(iv);
            w.clear();
            probabilities.clear();
            if(v.size <= 0)
            {
                v.add(1);
            }
            int vv;
            final long vs = v.size;
            OUTER:
            for (int i = 0; i < v.size; ++i) {
                vv = v.get(i);
                for (int j = 0; j < w.size; j++) {
                    if (w.get(j) == vv) {
                        probabilities.incr(j, 1);
                        continue OUTER;
                    }
                }
                w.add(vv);
                probabilities.add(1);
            }
            final int iAverage = (int)((0x7FFFFFFFL * w.size) / v.size);

            small.clear();
            large.clear();
            /* Populate the stacks with the input probabilities. */
            for (int i = 0; i < probabilities.size; i++) {
                /* If the probability is below the average probability, then we add
                 * it to the small list; otherwise we add it to the large list.
                 */
                if (probabilities.get(i) * 0x7FFFFFFFL >= iAverage * vs)
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
                processed[iv][less2] = iAverage * probabilities.get(less);
                processed[iv][less2+1] = w.get(less);
                processed[iv][less2+2] = w.get(more);

                probabilities.incr(more, probabilities.get(less) - iAverage);

                if (probabilities.get(more) >= iAverage)
                    large.add(more);
                else
                    small.add(more);
            }
            int t;
            while (!small.isEmpty())
            {
                processed[iv][(t = small.pop()) * 3] = 0x7FFFFFFF;
                processed[iv][t * 3 + 1] = processed[iv][t * 3 + 2] = w.get(t);
            }
            while (!large.isEmpty())
            {
                processed[iv][(t = large.pop()) * 3] = 0x7FFFFFFF;
                processed[iv][t * 3 + 1] = processed[iv][t * 3 + 2] = w.get(t);
            }
        }
        working.clear();
        body.clear();
    }
    public String chain(long seed) {
        return chain(seed, 200);
    }
    public String chain(long seed, int maxLength) {
        int before = 0;
        long state;
        StringBuilder sb = new StringBuilder(1000);
        int[] rf;
        while (sb.length() < maxLength) {
            rf = processed[before];
            // This is ThrustAltRNG's algorithm to generate a random long given sequential states
            state = ((state = ((seed += 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) ^ (state >>> 22));
            // get a random int (using half the bits of our previously-calculated state) that is less than size
            int column = (int) ((rf.length * (state & 0xFFFFFFFFL)) * 0x1.5555555555555p-34) * 3; // 1/3 of 2^-32
            // use the other half of the bits of state to get a double, compare to probability and choose either the
            // current column or the alias for that column based on that probability
            before = ((state >>> 33) <= rf[column]) ? rf[column + 1] : rf[column + 2];
            if(sb.length() >= maxLength - 3)
            {
                sb.append('.');
                break;
            }
            if(before >= 5)
            {
                if(sb.length() + words[before].length() + 1 < maxLength)
                    sb.append(' ').append(words[before]);
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
}
