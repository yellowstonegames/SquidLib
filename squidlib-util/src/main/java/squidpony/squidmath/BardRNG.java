package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Another one of the highest-quality 32-bit-math generators present in this library, complete with a very long period
 * and over 4096 bits of state (4128 if you include a necessary counter). While BardRNG does not have a fast lookahead
 * or lookbehind and the state may be a burden if multiple instances are needed, those are its most significant flaws.
 * Speed is slightly better than BirdRNG and should be more reliable at obtaining optimizations from HotSpot, but this
 * is nowhere near the speed of FlapRNG (probably closer to HerdRNG). While it is similar to LongPeriodRNG and BirdRNG
 * in that it has a lot of state, BardRNG is faster on int generation and should be comparable on long generation. It
 * does not slow down significantly if used as a RandomnessSource for an RNG, while BirdRNG and sometimes LongPeriodRNG
 * will suffer from the indirection of calling {@link #next(int)}. BardRNG currently passes the PractRand suite of RNG
 * quality testing with no anomalies, failure or otherwise, given 64MB of random ints. This is better than BirdRNG,
 * which still has anomalies of varying severity. It may be able to pass DIEHARDER, but that wouldn't be much of a
 * surprise because BardRNG has so much state, and that tends to make passing that test suite easier.
 * <br>
 * Created by Tommy Ettinger on 6/14/2017.
 */
public class BardRNG implements RandomnessSource, Serializable {
    /*
// Thanks umireon! This is CC0 licensed code in this comment block.
// https://github.com/umireon/my-random-stuff/blob/master/xorshift/splitmix32.c
// Written in 2016 by Kaito Udagawa
// Released under CC0 <http://creativecommons.org/publicdomain/zero/1.0/>
// [1]: Guy L. Steele, Jr., Doug Lea, and Christine H. Flood. 2014. Fast splittable pseudorandom number generators. In Proceedings of the 2014 ACM International Conference on Object Oriented Programming Systems Languages & Applications (OOPSLA '14). ACM, New York, NY, USA, 453-472.
uint32_t splitmix32(uint32_t *x) {
  uint32_t z = (*x += 0x9e3779b9);
  z = (z ^ (z >> 16)) * 0x85ebca6b;
  z = (z ^ (z >> 13)) * 0xc2b2ae35;
  return z ^ (z >> 16);
}
     */


    /**
     * Call this with {@code splitMix32(z += 0x9E3779B9)}, where z is an int to use as state.
     * 0x9E3779B9 can be changed for any odd int if the same number is used across calls.
     * @param z int, must be changed with each call; {@code splitMix32(z += 0x9E3779B9)} is recommended
     * @return a pseudo-random int
     */
    public static int splitMix32(int z) {
        z = (z ^ (z >>> 16)) * 0x85EBCA6B;
        z = (z ^ (z >>> 13)) * 0xC2B2AE35;
        return z ^ (z >>> 16);
    }
    /**
     * Call this with {@code splitMix32(z += 0x9E3779B97F4A7C15L)}, where z is a long to use as state.
     * 0x9E3779B97F4A7C15L can be changed for any odd long if the same number is used across calls.
     * @param z long, must be changed with each call; {@code splitMix32(z += 0x9E3779B97F4A7C15L)} is recommended
     * @return a pseudo-random long
     */
    public static long splitMix64(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private static final long serialVersionUID = 1L;
    private final int[] state = new int[128];
    private int choice = 0;
    public BardRNG() {
        this((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    public BardRNG(final int seed) {
        setState(seed);
    }


    public BardRNG(final long seed) {
        setState(seed);
    }

    public BardRNG(final int[] seed) {
        int len;
        if (seed == null || (len = seed.length) == 0) {
            for (int i = 0; i < 128; i++) {
                choice += (state[i] = splitMix32(0x632D978F + i * 0x9E3779B9));
            }
        } else if (len < 128) {
            for (int i = 0, s = 0; i < 128; i++, s++) {
                if(s == len) s = 0;
                choice += (state[i] ^= splitMix32(seed[s] + i * 0x9E3779B9));
            }
        } else {
            for (int i = 0, s = 0; s < len; s++, i = (i + 1) & 127) {
                choice += (state[i] ^= seed[s]);
            }
        }
    }

    /**
     * Uses the given String or other CharSequence as the basis for the 64 ints this uses as state, assigning choice to
     * be the sum of the rest of state.
     * Internally, this gets a 32-bit hash for seed with 48 different variations on the {@link CrossHash.Mist} hashing
     * algorithm and 16 variations on the {@link CrossHash.Storm} algorithm, and uses one for each int in state. This
     * tolerates null and empty-String values for seed.
     * @param seed a String or other CharSequence; may be null
     */
    public BardRNG(final CharSequence seed)
    {
        for (int i = 0; i < 48; i++) {
            choice += (state[i] = CrossHash.Mist.predefined[i].hash(seed));
            choice += (state[64+i] = CrossHash.Mist.predefined[i].hash(seed));
        }
        for (int i = 48; i < 64; i++) {
            choice += (state[i] = CrossHash.Storm.predefined[i & 15].hash(seed));
            choice += (state[64 + i] = CrossHash.Storm.predefined[i & 15].hash(seed));
        }
    }

    public void setState(final int seed) {
        choice = 0;
        for (int i = 0; i < 128; i++) {
            choice += (state[i] = splitMix32(seed + i * 0x9E3779B9));
        }
    }

    public void setState(final long seed) {
        choice = 0;
        for (int i = 0; i < 128; i++) {
            choice += (state[i] = (int)splitMix64(seed + i * 0x9E3779B9));
        }
    }

    public void setState(final int[] seed)
    {
        int len;
        if (seed == null || (len = seed.length) == 0) {
            for (int i = 0; i < 128; i++) {
                choice += (state[i] = splitMix32(0x632D978F + i * 0x9E3779B9));
            }
        } else if (len < 128) {
            for (int i = 0, s = 0; i < 128; i++, s++) {
                if(s == len) s = 0;
                choice += (state[i] ^= splitMix32(seed[s] + i * 0x9E3779B9));
            }
        } else {
            if (len == 128) {
                choice = 0;
                for (int i = 0; i < 128; i++) {
                    choice += (state[i] = seed[i]);
                }
            } else {
                for (int i = 0, s = 0; s < len; s++, i = (i + 1) & 127) {
                    choice += (state[i] ^= seed[s]);
                }
            }
        }
    }

    @Override
    public final long nextLong() {
        final int c2 = (choice += 0x7345085E), c = c2 - 0xB9A2842F;
        return (long)(state[c * 0x85157AF5 >>> 25] += (c >>> (c >>> 28)) * 0x632BE5AB) << 32 ^
                (state[c2 * 0x85157AF5 >>> 25] += (c2 >>> (c2 >>> 28)) * 0x632BE5AB);
    }
    public final int nextInt() {
        final int c = (choice += 0xB9A2842F);
        return (state[c * 0x85157AF5 >>> 25] += (c >>> (c >>> 28)) * 0x632BE5AB);
    }
    @Override
    public final int next(final int bits) {
        final int c = (choice += 0xB9A2842F);
        return (state[c * 0x85157AF5 >>> 25] += (c >>> (c >>> 28)) * 0x632BE5AB)
                >>> (32 - bits);
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
        BardRNG hr = new BardRNG(state);
        hr.choice = choice;
        return hr;
    }

    @Override
    public String toString() {
        return "BardRNG{" +
                "state=" + StringKit.hex(state) +
                ", choice=" + StringKit.hex(choice) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BardRNG bardRNG = (BardRNG) o;

        return choice == bardRNG.choice && Arrays.equals(state, bardRNG.state);
    }

    @Override
    public int hashCode() {
        return 31 * choice + CrossHash.Wisp.hash(state);
    }
}
