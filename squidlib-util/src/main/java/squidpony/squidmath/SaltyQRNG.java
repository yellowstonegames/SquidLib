package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * A different kind of quasi-random number generator (also called a sub-random sequence) that can be "salted" like some
 * hashing functions can, to produce many distinct sub-random sequences without changing its performance qualities.
 * This generally will be used to produce doubles, using {@link #nextDouble()}, and the other generator methods use the
 * same implementation internally. This tends to have fairly good distribution regardless of salt, with the first 256
 * doubles produced (between 0.0 and 1.0, for any salt tested) staying separated enough that {@code (int)(result * 512)}
 * will be unique, though using tighter restrictions than 512, like {@code (int)(result * 256)}, did not produce fully
 * unique values. Internally, this gets an exponent of the golden ratio {@code 1.6180339887498948482...} and adjusts its
 * significand by manipulating the binary representation of the double directly (using
 * {@link NumberTools#setExponent(double, int)}, which is a good fit here).
 * <br>
 * Created by Tommy Ettinger on 9/9/2017.
 */
@Beta
public class SaltyQRNG implements StatefulRandomness {

    /**
     * Creates a SaltyQRNG with a random salt and a random starting state. The random source used here is
     * {@link Math#random()}, which produces rather few particularly-random bits, but enough for this step.
     */
    public SaltyQRNG()
    {
        salt = Math.random();
        if(salt == 0.0) salt = 0.0123456789;
        current = (long) (Math.random() * 4.294967296E9) & 0xFFFFFFFFL;
    }

    /**
     * Creates a SaltyQRNG with a specific salt (this should usually be between 0.0 and 1.0, both exclusive). The salt
     * determines the precise sequence that will be produced over the whole lifetime of the QRNG, and two SaltyQRNG
     * objects with different salt values should produce different sequences, at least at some points in generation.
     * The starting state will be 0, which this tolerates well.
     * @param salt a double that should be between 0.0 and 1.0, both exclusive
     */
    public SaltyQRNG(double salt)
    {
        current = 0;
        setSalt(salt);
    }

    /**
     * Creates a SaltyQRNG with a specific salt (this should usually be between 0.0 and 1.0, both exclusive) and a point
     * it has already advanced to in the sequence this generates. The salt determines the precise sequence that will be
     * produced over the whole lifetime of the QRNG, and two SaltyQRNG objects with different salt values should produce
     * different sequences, at least at some points in generation. The advance will only have its least-significant 32
     * bits used, so an int can be safely passed as advance without issue (even a negative int).
     * @param salt a double that should be between 0.0 and 1.0, both exclusive
     * @param advance a long to use as the state; only the bottom 32 bits are used, so any int can also be used
     */
    public SaltyQRNG(double salt, long advance)
    {
        setState(advance);
        current = 0;
        setSalt(salt);
    }
    private double salt;

    public double getSalt()
    {
        return salt;
    }
    public void setSalt(double newSalt)
    {
        if(newSalt == 0.0) salt = 0.0123456789;
        else salt = Math.abs(newSalt) % 1.0;
    }

    private long current;
    @Override
    public long getState() {
        return current;
    }

    /**
     * Sets the current "state" of the QRNG (which number in the sequence it will produce), using the least-significant
     * 32 bits of a given long.
     * @param state a long, which is allowed to be 0; this only uses the bottom 32 bits, so you could pass an int
     */
    @Override
    public void setState(long state) {
        current = state & 0xFFFFFFFFL;
    }

    @Override
    public int next(int bits) {
        return (int) (nextLong() >>> (64 - bits));
    }

    @Override
    public long nextLong() {
        return (long)(-9223372036854775808L *
                (NumberTools.setExponent( Math.pow(1.6180339887498948482, salt + (++current & 0xFFFFFFFFL)), 0x400) - 3.0));
    }

    /**
     * Gets the next double in the sequence, between 0.0 (inclusive) and 1.0 (exclusive)
     * @return a double between 0.0 (inclusive) and 1.0 (exclusive)
     */
    public double nextDouble()
    {
        return NumberTools.setExponent( Math.pow(1.6180339887498948482, salt + (++current & 0xFFFFFFFFL)), 0x3ff) - 1.0;
    }

    @Override
    public RandomnessSource copy() {
        return new SaltyQRNG(salt, current);
    }
}
