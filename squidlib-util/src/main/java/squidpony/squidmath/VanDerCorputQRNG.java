package squidpony.squidmath;

import java.io.Serializable;

/**
 * Created by Tommy Ettinger on 11/18/2016.
 */
public class VanDerCorputQRNG implements StatefulRandomness, RandomnessSource, Serializable {
    private static final long serialVersionUID = 1;
    protected long state;
    public final int base;

    /**
     * Constructs a new van der Corput sequence generator with base 3 and starting point 409.
     */
    public VanDerCorputQRNG()
    {
        base = 3;
        state = 409;
    }

    /**
     * Constructs a new van der Corput sequence generator with the given starting point in the sequence as a seed
     * (negative seeds will be treated as positive by discarding their sign bit, so don't use them).
     * @param seed the seed as a long that will be used as the starting point in the sequence; ideally positive but low
     */
    public VanDerCorputQRNG(long seed)
    {
        base = 3;
        state = seed & 0x7fffffffffffffffL;
    }

    /**
     * Constructs a new van der Corput sequence generator with the given base (radix) and starting point in the sequence
     * as a seed (negative seeds will be treated as positive by discarding their sign bit, so don't use them).
     * @param base the base or radix used for this VanDerCorputQRNG; for certain uses this should be prime but small
     * @param seed the seed as a long that will be used as the starting point in the sequence; ideally positive but low
     */
    public VanDerCorputQRNG(int base, long seed)
    {
        this.base = base < 2 ? 2 : base;
        state = seed & 0x7fffffffffffffffL;
    }

    @Override
    public long nextLong() {
        long num = ++state % base, den = base;
        while (den <= state)
        {
            num *= base;
            num += (state % (den * base)) / den;
            den *= base;
        }
        return (Long.MAX_VALUE / den) * num;
    }

    @Override
    public int next(int bits) {
        return (int)(nextLong()) >>> (32 - bits);
    }

    public double nextDouble() {
        long num = ++state % base, den = base;
        while (den <= state)
        {
            num *= base;
            num += (state % (den * base)) / den;
            den *= base;
        }
        return num / ((double)den);
    }

    @Override
    public VanDerCorputQRNG copy() {
        return new VanDerCorputQRNG(base, state);
    }

    @Override
    public long getState() {
        return state;
    }

    @Override
    public void setState(long state) {
        this.state = state & 0x7fffffffffffffffL;
    }
}
