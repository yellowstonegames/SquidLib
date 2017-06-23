package squidpony.squidmath;

import squidpony.StringKit;
import squidpony.annotation.Beta;

import java.io.Serializable;
import java.util.Arrays;

/**
 * One of the highest-quality 64-bit-math generators present in this library, complete with a very long period and over
 * 4096 bits of state (4128 if you include a necessary counter). While BeardRNG does not have a fast lookahead or
 * lookbehind and isn't especially fast, those are its most significant flaws. It is related to BirdRNG, but uses more
 * state and 64-bit math to speed up its {@link #nextLong()} operation. BeardRNG currently passes the PractRand suite of
 * RNG quality testing with no failures given 64MB of random ints, on any of the given folding modes. It may be able to
 * pass DIEHARDER, but that wouldn't be much of a surprise because BeardRNG has so much state, and that tends to make
 * passing that test suite easier.
 * <br>
 * The exact period of BeardRNG is unknown at this point. It has 64 longs of state, and updates one long of state by
 * adding a constant value with each generation. Another long, which may be the same one, is also updated by adding a
 * value derived from the state update earlier in the generation. The two longs of state are chosen by using the most
 * significant 6 bits and least significant 6 bits of an int value that has a constant added to it each generation.
 * The maximum period a PRNG can have with 4096 bits of state is 2 to the 4096, but Beard probably has a smaller period
 * than that, possibly as low as 2 to the 127.
 * <br>
 * Created by Tommy Ettinger on 6/14/2017.
 */
@Beta
public class BeardRNG implements RandomnessSource, Serializable {
    private static final long serialVersionUID = 1L;
    public final long[] state = new long[64];
    public int choice = 0;
    public BeardRNG() {
        this((long) ((Math.random() * 2.0 - 1.0) * 0x8000000000000L)
                ^ (long) ((Math.random() * 2.0 - 1.0) * 0x8000000000000000L));
    }

    public BeardRNG(final long seed) {
        setState(seed);
    }

    public BeardRNG(final long[] seed) {
        int len;
        if (seed == null || (len = seed.length) == 0) {
            for (int i = 0; i < 64; i++) {
                choice += (state[i] = LightRNG.determine(0x632D978F + i));
            }
        } else if (len < 64) {
            for (int i = 0, s = 0; i < 64; i++, s++) {
                if(s == len) s = 0;
                choice += (state[i] ^= LightRNG.determine(seed[s] + i));
            }
        } else {
            for (int i = 0, s = 0; s < len; s++, i = (i + 1) & 63) {
                choice += (state[i] ^= seed[s]);
            }
        }
    }

    /**
     * Uses the given String or other CharSequence as the basis for the 64 longs this uses as state, assigning choice to
     * be the sum of the rest of state.
     * Internally, this gets a 64-bit hash for seed with 48 different variations on the {@link CrossHash.Mist} hashing
     * algorithm and 16 variations on the {@link CrossHash.Storm} algorithm, and uses one for each long in state. This
     * tolerates null and empty-String values for seed.
     * @param seed a String or other CharSequence; may be null
     */
    public BeardRNG(final CharSequence seed)
    {
        for (int i = 0; i < 48; i++) {
            choice += (state[i] = CrossHash.Mist.predefined[i].hash64(seed));
        }
        for (int i = 48; i < 64; i++) {
            choice += (state[i] = CrossHash.Storm.predefined[i & 15].hash64(seed));
        }
    }

    public void setState(final int seed) {
        choice = 0;
        for (int i = 0; i < 64; i++) {
            choice += (state[i] = LightRNG.determine(seed + i));
        }
    }

    public void setState(final long seed) {
        choice = 0;
        for (int i = 0; i < 64; i++) {
            choice += (state[i] = (int)LightRNG.determine(seed + i));
        }
    }

    public void setState(final long[] seed)
    {
        int len;
        if (seed == null || (len = seed.length) == 0) {
            for (int i = 0; i < 64; i++) {
                choice += (state[i] = LightRNG.determine(0x632D978F + i));
            }
        } else if (len < 64) {
            for (int i = 0, s = 0; i < 64; i++, s++) {
                if(s == len) s = 0;
                choice += (state[i] ^= LightRNG.determine(seed[s] + i));
            }
        } else {
            if(len == 64)
            {
                choice = 0;
                for (int i = 0; i < 64; i++) {
                    choice += (state[i] = seed[i]);
                }
            }
            else {
                for (int i = 0, s = 0; s < len; s++, i = (i + 1) & 63) {
                    choice += (state[i] ^= seed[s]);
                }
            }
        }
    }

    @Override
    public final long nextLong() {
        return (state[(choice += 0xCBBC475B) & 63] += (state[choice >>> 26] += 0xAC8C0FE02D14624DL) >>> 1);
    }

    public final int nextInt() {
        return (int) (state[(choice += 0xCBBC475B) & 63] += (state[choice >>> 26] += 0xAC8C0FE02D14624DL) >>> 1); //0xF1B188FEB7A8C8F5L
    }

    @Override
    public final int next(final int bits) {
        return (int) ((state[(choice += 0xCBBC475B) & 63] += (state[choice >>> 26] += 0xAC8C0FE02D14624DL) >>> 1) >>> (64 - bits)); //0x9E3779B9
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public RandomnessSource copy() {
        BeardRNG hr = new BeardRNG(state);
        hr.choice = choice;
        return hr;
    }

    @Override
    public String toString() {
        return "BeardRNG{" +
                "state=" + StringKit.hex(state) +
                ", choice=" + choice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BeardRNG beardRNG = (BeardRNG) o;

        return choice == beardRNG.choice && Arrays.equals(state, beardRNG.state);
    }

    @Override
    public int hashCode() {
        return 31 * choice + CrossHash.Wisp.hash(state);
    }
}
