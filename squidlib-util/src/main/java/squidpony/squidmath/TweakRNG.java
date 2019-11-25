package squidpony.squidmath;

import squidpony.annotation.Beta;

import java.io.Serializable;

/**
 * Highly experimental RNG that can be configured to smoothly transition between producing mostly values in the center
 * of its range, to producing more values at or near the extremes, as well as favoring high or low results. The
 * probability distribution is... unusual, with lumps that rise or fall based on centrality.
 * <br>
 * <a href="https://i.imgur.com/VCvtlSc.gifv">Here's an animation of the distribution graph changing.</a>
 * <br>
 * Created by Tommy Ettinger on 10/6/2019.
 */
@Beta
public class TweakRNG extends AbstractRNG implements Serializable {

    private static final long serialVersionUID = 1L;

    private long stateA, stateB, centrality, favor;
    public TweakRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                (long) ((Math.random() - 0.5) * 0x10000000000000L)
                        ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L),
                0L);
    }

    public TweakRNG(long seed) {
        this((seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25, 
                ((seed = ((seed = (((seed * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ seed >>> 27) * 0xAEF17502108EF2D9L) ^ seed >>> 25),
                0L);
    }

    public TweakRNG(final long seedA, final long seedB, final long centrality) {
        stateA = seedA;
        stateB = seedB | 1L;
        this.centrality = centrality % 0x10000L;
        this.favor = 0L;
    }

    public TweakRNG(final long seedA, final long seedB, final long centrality, final long favor) {
        stateA = seedA;
        stateB = seedB | 1L;
        this.centrality = centrality % 0x10000L;
        this.favor = favor % 0x10000L;
    }

    @Override
    public int next(int bits) {
        return (int)(nextLong() >>> 64 - bits);
    }

    @Override
    public int nextInt() {
        return (int) (nextLong() >>> 32);
    }

    @Override
    public long nextLong() {
        final long r = internalLong(), s = internalLong();
        return (long) (NumberTools.atan2_(((r & 0xFF) - (r >>> 8 & 0xFF)) * ((r >>> 16 & 0xFF) - (r >>> 24 & 0xFF)) - favor,
                ((r >>> 32 & 0xFF) - (r >>> 40 & 0xFF)) * ((r >>> 48 & 0xFF) - (r >>> 56 & 0xFF)) + centrality) * 0x6000000000000000L)
                + (s & 0x1FFFFFFFFFFFFFFFL) << 1 ^ (s >>> 63);
    }

    @Override
    public boolean nextBoolean() {
        return nextLong() < 0;
    }
    
    @Override
    public double nextDouble() {
        final long r = internalLong();
        return NumberTools.atan2_(((r & 0xFF) - (r >>> 8 & 0xFF)) * ((r >>> 16 & 0xFF) - (r >>> 24 & 0xFF)) - favor,
                ((r >>> 32 & 0xFF) - (r >>> 40 & 0xFF)) * ((r >>> 48 & 0xFF) - (r >>> 56 & 0xFF)) + (double)centrality) * 0.75
                + (internalLong() & 0xfffffffffffffL) * 0x1p-54;
    }

    @Override
    public float nextFloat() {
        final long r = internalLong();
        return NumberTools.atan2_(((r & 0xFF) - (r >>> 8 & 0xFF)) * ((r >>> 16 & 0xFF) - (r >>> 24 & 0xFF)) - favor,
                ((r >>> 32 & 0xFF) - (r >>> 40 & 0xFF)) * ((r >>> 48 & 0xFF) - (r >>> 56 & 0xFF)) + centrality) * 0.75f
                + (internalLong() & 0x7fffffL) * 0x1p-25f;
    }
    
    @Override
    public TweakRNG copy() {
        return this;
    }

    @Override
    public Serializable toSerializable() {
        return this;
    }

    public long getStateA() {
        return stateA;
    }

    public void setStateA(long stateA) {
        this.stateA = stateA;
    }

    public long getStateB() {
        return stateB;
    }

    public void setStateB(long stateB) {
        this.stateB = stateB | 1L;
    }

    public long getCentrality() {
        return centrality;
    }

    /**
     * Adjusts the central bias of this TweakRNG, often to positive numbers (which bias toward the center of the range),
     * but also often to negative numbers (which bias toward extreme values, though still within the range).
     * @param centrality should be between -65535 and 65535; positive values bias toward the center of the range
     */
    public void setCentrality(long centrality) {
        this.centrality = centrality % 65536L;
    }


    public long getFavor() {
        return favor;
    }

    /**
     * Adjusts the value bias of this TweakRNG, often to positive numbers (which bias toward higher results), but also
     * often to negative numbers (which bias toward lower results). All results will still be in their normal range, but
     * will change how often high or low values occur. Unusual results will occur if favor is non-zero and you get a
     * long with {@link #nextLong()}; in that case, the values are treated as higher when unsigned, so positive favor
     * makes both high positive values and all negative values more common. Doubles and floats will behave normally.
     * @param favor should be between -65535 and 65535; positive values bias toward higher (unsigned for longs) results
     */
    public void setFavor(long favor) {
        this.favor = favor % 65536L;
    }

    /**
     *{@link TangleRNG}'s algorithm; not all longs will be returned by any individual generator, but all generators as a
     * whole will return all longs with equal likelihood.
     * @return a random long in the full range; each state is advanced by 1 step.
     */
    private long internalLong()
    {
        final long s = (stateA += 0xC6BC279692B5C323L);
        final long z = (s ^ s >>> 31) * (stateB += 0x9E3779B97F4A7C16L);
        return z ^ z >>> 26;
    }
}
