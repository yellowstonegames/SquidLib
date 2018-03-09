package squidpony;

import squidpony.annotation.Beta;
import squidpony.squidmath.Arrangement;
import squidpony.squidmath.IntVLA;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple Markov chain generator that works with Lists of some type instead of text like {@link MarkovText}.
 * Call {@link #analyze(Iterable)} or {@link #analyze(Object[])} once on a large sample Iterable or array where
 * sequences of items matter (this is called a corpus, and could be e.g. a List or an array), then you can call
 * {@link #chain(long)} many times to get "remixes" of the sample Iterable/array as a List. This is meant to allow easy
 * serialization of the necessary data to call chain(); if you can store the {@link #body} and {@link #processed} data
 * structures in some serialized form, then you can reassign them to the same fields to avoid calling analyze(). This
 * requires some way to serialize body, which is an {@link Arrangement} of T, and so T must be serializable in some way
 * (not necessarily the {@link java.io.Serializable} interface, but possibly that).
 * <br>
 * Created by Tommy Ettinger on 2/26/2018.
 */
@Beta
public class MarkovObject<T> implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * All unique T items that this encountered during the latest call to {@link #analyze(Iterable)}.
     * Will be null if analyze() was never called.
     */
    public Arrangement<T> body;
    /**
     * Complicated data that mixes probabilities and the indices of items in {@link #body}, generated during the latest
     * call to {@link #analyze(Iterable)}. Will be null if analyze() was never called.
     */
    public ArrayList<IntVLA> processed;
    public ArrayList<IntVLA> raw;

    public MarkovObject()
    {
    }

    /**
     * This is the main necessary step before using a MarkovObject; you must call this method at some point before you
     * can call any other methods. This method analyzes the pairings of items in a (typically large) corpus Iterable.
     * It only uses one preceding item to determine the subsequent word. It does not store any items as special stop
     * terms, but it does use {@code null} to represent the start of a section (effectively treating any corpus as
     * starting with null prepended), and will not produce null as output from {@link #chain(long)}. If null is
     * encountered as part of corpus, it will be interpreted as a point to stop on and potentially start a new section.
     * Since the last item in the corpus could have no known items to produce after it, the end of the corpus is treated
     * as having null appended as well. When it finishes processing, it stores the results in {@link #body} and
     * {@link #processed}, which allows other methods to be called (they will throw a {@link NullPointerException} if
     * analyze() hasn't been called).
     * <br>
     * Unlike in {@link MarkovText}, you can analyze multiple corpus Iterables by calling this method more than once.
     *
     * @param corpus a typically-large sample Iterable in the style that should be mimicked
     */
    public void analyze(Iterable<T> corpus)
    {
        if(body == null || processed == null) {
            body = new Arrangement<>(64);
            body.add(null);
            raw = new ArrayList<>(64);
            raw.add(new IntVLA(128));
            processed = new ArrayList<>(64);
            processed.add(new IntVLA(128));
        }
        int previous = 0, current;
        for (T item : corpus)
        {
            current = body.addOrIndex(item);
            if(raw.size() != body.size())
            {
                raw.add(new IntVLA(16));
            }
            raw.get(previous).add(current);
            previous = current;
        }
        raw.get(previous).add(0);
        IntVLA w, v;
        final int len = raw.size();
        processed.ensureCapacity(len);
        //processed = new int[len][];
        w = new IntVLA(128);
        IntVLA small = new IntVLA(128);
        IntVLA large = new IntVLA(128);
        IntVLA probabilities = new IntVLA(128);
        for(int iv = 0; iv < len; iv++ )
        {
            v = raw.get(iv);
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
            if(processed.size() <= iv)
                processed.add(v = new IntVLA(w.size * 3));
            else
            {
                v = processed.get(iv);
                v.clear();
            }
            final int[] va = v.setSize(w.size * 3);
            //processed[iv] = new int[w.size * 3];

            while (!small.isEmpty() && !large.isEmpty()) {
                /* Get the index of the small and the large probabilities. */
                int less = small.pop(), less2 = less * 3;
                int more = large.pop();

                /* These probabilities have not yet been scaled up to be such that
                 * sum/n is given weight 1.0.  We do this here instead.
                 */
                va[less2] = iAverage * probabilities.get(less);
                va[less2+1] = w.get(less);
                va[less2+2] = w.get(more);

                probabilities.incr(more, probabilities.get(less) - iAverage);

                if (probabilities.get(more) >= iAverage)
                    large.add(more);
                else
                    small.add(more);
            }
            int t;
            while (!small.isEmpty())
            {
                va[(t = small.pop()) * 3] = 0x7FFFFFFF;
                va[t * 3 + 1] = va[t * 3 + 2] = w.get(t);
            }
            while (!large.isEmpty())
            {
                va[(t = large.pop()) * 3] = 0x7FFFFFFF;
                va[t * 3 + 1] = va[t * 3 + 2] = w.get(t);
            }
        }
    }

    /**
     * This is the main necessary step before using a MarkovObject; you must call this method at some point before you
     * can call any other methods. This method analyzes the pairings of items in a (typically large) corpus array of T.
     * It only uses one preceding item to determine the subsequent word. It does not store any items as special stop
     * terms, but it does use {@code null} to represent the start of a section (effectively treating any corpus as
     * starting with null prepended), and will not produce null as output from {@link #chain(long)}. If null is
     * encountered as part of corpus, it will be interpreted as a point to stop on and potentially start a new section.
     * Since the last item in the corpus could have no known items to produce after it, the end of the corpus is treated
     * as having null appended as well. When it finishes processing, it stores the results in {@link #body} and
     * {@link #processed}, which allows other methods to be called (they will throw a {@link NullPointerException} if
     * analyze() hasn't been called).
     * <br>
     * Unlike in {@link MarkovText}, you can analyze multiple corpus arrays by calling this method more than once.
     *
     * @param corpus a typically-large sample array of T in the style that should be mimicked
     */
    public void analyze(T[] corpus)
    {
        if(body == null || processed == null) {
            body = new Arrangement<>(corpus.length * 3 >> 2);
            body.add(null);
            raw = new ArrayList<>(corpus.length * 3 >> 2);
            raw.add(new IntVLA(128));
            processed = new ArrayList<>(corpus.length * 3 >> 2);
            processed.add(new IntVLA(128));
        }
        int previous = 0, current;
        for (T item : corpus)
        {
            current = body.addOrIndex(item);
            if(raw.size() != body.size())
            {
                raw.add(new IntVLA(16));
            }
            raw.get(previous).add(current);
            previous = current;
        }
        raw.get(previous).add(0);
        IntVLA w, v;
        final int len = raw.size();
        processed.ensureCapacity(len);
        //processed = new int[len][];
        w = new IntVLA(128);
        IntVLA small = new IntVLA(128);
        IntVLA large = new IntVLA(128);
        IntVLA probabilities = new IntVLA(128);
        for(int iv = 0; iv < len; iv++ )
        {
            v = raw.get(iv);
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
            if(processed.size() <= iv)
                processed.add(v = new IntVLA(w.size * 3));
            else
            {
                v = processed.get(iv);
                v.clear();
            }
            final int[] va = v.setSize(w.size * 3);
            //processed[iv] = new int[w.size * 3];

            while (!small.isEmpty() && !large.isEmpty()) {
                /* Get the index of the small and the large probabilities. */
                int less = small.pop(), less2 = less * 3;
                int more = large.pop();

                /* These probabilities have not yet been scaled up to be such that
                 * sum/n is given weight 1.0.  We do this here instead.
                 */
                va[less2] = iAverage * probabilities.get(less);
                va[less2+1] = w.get(less);
                va[less2+2] = w.get(more);

                probabilities.incr(more, probabilities.get(less) - iAverage);

                if (probabilities.get(more) >= iAverage)
                    large.add(more);
                else
                    small.add(more);
            }
            int t;
            while (!small.isEmpty())
            {
                va[(t = small.pop()) * 3] = 0x7FFFFFFF;
                va[t * 3 + 1] = va[t * 3 + 2] = w.get(t);
            }
            while (!large.isEmpty())
            {
                va[(t = large.pop()) * 3] = 0x7FFFFFFF;
                va[t * 3 + 1] = va[t * 3 + 2] = w.get(t);
            }
        }
    }

    /**
     * Generates a 32-element List of T based on the given seed and previously analyzed corpus data (using
     * {@link #analyze(Iterable)}). This can't stop before generating a chain of 32 items unless analyze() hasn't been
     * called or it was called on an empty or invalid Iterable/array (i.e. all null).
     * @param seed the seed for the random decisions this makes, as a long; any long can be used
     * @return a 32-element T List generated from the analyzed corpus Iterable/array's pairings of items
     */
    public List<T> chain(long seed) {
        return chain(seed, 32, false, new ArrayList<T>(32));
    }

    /**
     * Adds T items to buffer to fill it up to maxLength, based on the given seed and previously analyzed corpus
     * data (using {@link #analyze(Iterable)}). If buffer is already at least as long as maxLength, if analyze() hasn't
     * been called or if it was called on an empty or invalid Iterable/array (i.e. all null), then this won't change
     * buffer and will return it as-is. If null was present in the analyzed corpus along with other items and
     * canStopEarly is true, then if null would be generated this will instead stop adding items to buffer and return
     * buffer as it is. If canStopEarly was false in the last case, the generated null would be discarded and a value
     * from the start of the corpus or following a null in the corpus would be used instead.
     * @param seed the seed for the random decisions this makes, as a long; any long can be used
     * @param maxLength the maximum length for the generated List, in items
     * @param canStopEarly if true, this may add less than maxLength elements if null was present in the corpus
     * @param buffer a List of T that will have elements added until maxLength is reached; if it already is larger than
     *               maxLength this won't do anything
     * @return buffer, after items were added to fill maxLength (or to fill less if this stopped early)
     */
    public List<T> chain(long seed, int maxLength, boolean canStopEarly, List<T> buffer) {
        if(body == null || body.size() <= 1)
            return buffer;
        int before = 0;
        long state;
        IntVLA rf;
        while (buffer.size() < maxLength) {
            rf = processed.get(before);
            // This is LightRNG's algorithm to generate a random long given sequential states
            state = ((state = ((state = ((seed += 0x9E3779B97F4A7C15L) ^ seed >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31);

            // get a random int (using half the bits of our previously-calculated state) that is less than size
            int column = (int) ((rf.size * (state & 0xFFFFFFFFL)) / 0x300000000L) * 3; // divide by 2^32, round down to multiple of 3
            // use the other half of the bits of state to get a double, compare to probability and choose either the
            // current column or the alias for that column based on that probability
            before = ((state >>> 33) <= rf.get(column)) ? rf.get(column + 1) : rf.get(column + 2);
            if(before != 0)
            {                                  
                buffer.add(body.keyAt(before));
            }
            else if(canStopEarly)
            {
                break;
            }
        }
        return buffer;
    }
    
    /**
     * Copies the T items in {@link #body} and the int-based data structure {@link #processed} into a new MarkovObject.
     * None of the inner values, such as IntVLA values in processed, will be equivalent references, but the items in
     * body will be the same objects in both MarkovObject instances.
     * @return a copy of this MarkovObject
     */
    public MarkovObject<T> copy()
    {
        MarkovObject<T> other = new MarkovObject<>();
        other.body = new Arrangement<>(body);
        other.processed = new ArrayList<>(processed.size());
        for (int i = 0; i < processed.size(); i++) {
            other.processed.add(new IntVLA(processed.get(i)));
        }
        return other;
    }
}
