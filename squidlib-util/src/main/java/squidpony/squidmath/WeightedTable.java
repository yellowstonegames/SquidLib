package squidpony.squidmath;

import java.io.Serializable;

/**
 * A different approach to the same task {@link ProbabilityTable} solves, though this only looks up an appropriate index
 * instead of also storing items it can choose; allows positive doubles for weights but does not allow nested tables for
 * simplicity. This doesn't store an RNG (or RandomnessSource) in this class, and instead expects a long to be given for
 * each random draw from the table (these long parameters can be random, sequential, or in some other way different
 * every time). Uses <a href="http://www.keithschwarz.com/darts-dice-coins/">Vose's Alias Method</a>, and is based
 * fairly-closely on the code given by Keith Schwarz at that link. Because Vose's Alias Method is remarkably fast (O(1)
 * generation time in use, and O(n) time to construct a WeightedTable instance), this may be useful to consider if you
 * don't need all the features of ProbabilityTable or if you want deeper control over the random aspects of it.
 * <br>
 * Internally, this uses ThrustAltRNG's algorithm as found in {@link ThrustAltRNG#determineBounded(long, int)} and
 * {@link ThrustAltRNG#determine(long)} to generate two ints, one used for probability and treated as a 31-bit integer
 * and the other used to determine the chosen column, which is bounded to an arbitrary positive int. It does thsi with
 * just one randomized 64-bit value, allowing the state given to {@link #random(long)} to be just one long.
 * <br>
 * Created by Tommy Ettinger on 1/5/2018.
 */
public class WeightedTable implements Serializable {
    private static final long serialVersionUID = 101L;
//    protected final int[] alias;
//    protected final int[] probability;
    protected final int[] mixed;
    public final int size;

    /**
     * Constructs a useless WeightedTable that always returns the index 0.
     */
    public WeightedTable()
    {
        this(1);
    }

    /**
     * Constructs a WeightedTable with the given array of weights for each index. The array can also be a varargs for
     * convenience. The weights can be any positive non-zero doubles, but should usually not be so large or small that
     * precision loss is risked. Each weight will be used to determine the likelihood of that weight's index being
     * returned by {@link #random(long)}.
     * @param prob an array or varargs of positive doubles representing the weights for their own indices
     */
    public WeightedTable(double... prob) {
        /* Begin by doing basic structural checks on the inputs. */
        if (prob == null)
            throw new NullPointerException("Array 'probabilities' given to WeightedTable cannot be null");
        if ((size = prob.length) == 0)
            throw new IllegalArgumentException("Array 'probabilities' given to WeightedTable must be nonempty.");

        /* Allocate space for the probability and alias tables. */
//        probability = new int[size];
//        alias = new int[size];
        mixed = new int[size<<1];

        /* Compute the average probability and cache it for later use. */
        double sum = 0.0;

        /* Make a copy of the probabilities list, since we will be making
         * changes to it.
         */
        double[] probabilities = new double[size];
        for (int i = 0; i < size; ++i) {
            if(prob[i] <= 0) continue;
            sum += (probabilities[i] = prob[i]);
        }
        if(sum <= 0)
            throw new IllegalArgumentException("At least one probability must be positive");
        final double average = sum / size;

        /* Create two stacks to act as worklists as we populate the tables. */
        IntVLA small = new IntVLA(size);
        IntVLA large = new IntVLA(size);

        /* Populate the stacks with the input probabilities. */
        for (int i = 0; i < size; ++i) {
            /* If the probability is below the average probability, then we add
             * it to the small list; otherwise we add it to the large list.
             */
            if (probabilities[i] >= average)
                large.add(i);
            else
                small.add(i);
        }

        /* As a note: in the mathematical specification of the algorithm, we
         * will always exhaust the small list before the big list.  However,
         * due to floating point inaccuracies, this is not necessarily true.
         * Consequently, this inner loop (which tries to pair small and large
         * elements) will have to check that both lists aren't empty.
         */
        while (!small.isEmpty() && !large.isEmpty()) {
            /* Get the index of the small and the large probabilities. */
            int less = small.pop(), less2 = less << 1;
            int more = large.pop();

            /* These probabilities have not yet been scaled up to be such that
             * sum/n is given weight 1.0.  We do this here instead.
             */
            mixed[less2] = (int)(0x7FFFFFFF * (probabilities[less] / average));
            mixed[less2|1] = more;

            probabilities[more] += probabilities[less] - average;

            if (probabilities[more] >= average)
                large.add(more);
            else
                small.add(more);
        }

        while (!small.isEmpty())
            mixed[small.pop()<<1] = 0x7FFFFFFF;
        while (!large.isEmpty())
            mixed[large.pop()<<1] = 0x7FFFFFFF;
    }

    /**
     * Gets an index of one of the weights in this WeightedTable, with the choice determined deterministically by the
     * given long, but higher weights will be returned by more possible inputs than lower weights. The state parameter
     * can be from a random source, but this will randomize it again anyway, so it is also fine to just give sequential
     * longs. The important thing is that each state input this is given will produce the same result for this
     * WeightedTable every time, so you should give different state values when you want random-seeming results. You may
     * want to call this like {@code weightedTable.random(++state)}, where state is a long, to ensure the inputs change.
     * This will always return an int between 0 (inclusive) and {@link #size} (exclusive).
     * @param state a long that should be different every time; consider calling with {@code ++state}
     * @return a random-seeming index from 0 to {@link #size} - 1, determined by weights and the given state
     */
    public int random(long state)
    {
        // This is ThrustAltRNG's algorithm to generate a random long given sequential states
        state = ((state = ((state *= 0x6C8E9CF570932BD5L) ^ (state >>> 25)) * (state | 0xA529L)) ^ (state >>> 22));
        // get a random int (using half the bits of our previously-calculated state) that is less than size
        int column = (int)((size * (state & 0xFFFFFFFFL)) >> 32);
        // use the other half of the bits of state to get a double, compare to probability and choose either the
        // current column or the alias for that column based on that probability
        return ((state >>> 33) <= mixed[column << 1]) ? column : mixed[column << 1 | 1];
    }

}
