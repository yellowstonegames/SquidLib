package squidpony.squidmath;

import squidpony.StringKit;
import squidpony.annotation.Beta;

import java.io.Serializable;
import java.util.Arrays;

/**
 * One of the highest-quality 32-bit-math generators present in this library, complete with a very long period and over
 * 1024 bits of state (1056 if you include a necessary counter). While BirdRNG does not have a fast lookahead or
 * lookbehind and isn't especially fast, those are its most significant flaws. While it is slower than FlapRNG and
 * HerdRNG, it should be faster than PintRNG, which is also its closest competitor on quality. While it is similar to
 * LongPeriodRNG in that it has 1056 bits of state, BirdRNG is much faster on int generation and (strangely) competitive
 * on long generation as well. BirdRNG currently passes the PractRand suite of RNG quality testing with no failures
 * given 64MB of random ints, except on one of PractRand's optional folding modes (32-bit folding with the stdin32
 * argument), where it has 2 failures and 97 anomaly-free tests. It may be able to pass DIEHARDER, but that wouldn't be
 * much of a surprise because BirdRNG has so much state, and that tends to make passing that test suite easier.
 * <br>
 * There are a lot of quirks in each generator, but it should be mentioned that BirdRNG has an unorthodox state update
 * pattern that may result in a lower period but also ensures its high quality. There are 32 ints of main state stored
 * in an array, as well as 1 int for a counter that is incremented by a very large int with each number generated.
 * The bottom 5 bits of the counter determine the primary part of the state array being updated, which can be any of the
 * 32 ints. The actual value used during the update, which is a simple add-and-assign, is determined by the upper 4 bits
 * of counter, which means only 16 of the ints in the main state can be used for this step. The int chosen by the upper
 * bits is incremented by a large value (this is assigned to it as well), then the result unsigned-right-shifted by 1
 * (which reduces the issues of low period on the least significant bit), and this is added to the int of main state
 * chosen by the lower bits. Confused yet? I know I was making this... Even slight tweaks to any part of this generator
 * can completely change the quality. It is unclear if this actually has a lower period than 2 to the 1024, since
 * testing more than 2 to the 128 generated numbers empirically would take an incredible amount of time, as well as
 * requiring an impossible amount of storage space, so the proof would need to be algorithmic. Anyone want to try?
 * <br>
 * Created by Tommy Ettinger on 6/14/2017.
 */
@Beta
public class BirdRNG implements RandomnessSource, Serializable {
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
     * @param z must be changed with each call; {@code splitMix32(z += 0x9E3779B9)} is recommended
     * @return a pseudo-random int
     */
    public static int splitMix32(int z)
    {
        z = (z ^ (z >>> 16)) * 0x85EBCA6B;
        z = (z ^ (z >>> 13)) * 0xC2B2AE35;
        return z ^ (z >>> 16);
    }

    private static final long serialVersionUID = 1L;
    public final int[] state = new int[32];
    public int choice = 0;
    public BirdRNG() {
        this((int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    public BirdRNG(final int seed) {
        setState(seed);
    }


    public BirdRNG(final long seed) {
        setState(seed);
    }

    public BirdRNG(final int[] seed) {
        int len;
        if (seed == null || (len = seed.length) == 0) {
            for (int i = 0; i < 32; i++) {
                choice += (state[i] = splitMix32(0x632D978F + i * 0x9E3779B9));
            }
        } else if (len < 32) {
            for (int i = 0, s = 0; i < 32; i++, s++) {
                if(s == len) s = 0;
                choice += (state[i] ^= splitMix32(seed[s] + i * 0x9E3779B9));
            }
        } else {
            for (int i = 0, s = 0; s < len; s++, i = (i + 1) & 31) {
                choice += (state[i] ^= seed[s]);
            }
        }
    }

    /**
     * Uses the given String or other CharSequence as the basis for the 16 ints this uses as state, assigning choice to
     * be the sum of the rest of state.
     * Internally, this gets a 32-bit hash for seed with 16 different variations on the {@link CrossHash.Mist} hashing
     * algorithm, and uses one for each int in state. This tolerates null and empty-String values for seed.
     * @param seed a String or other CharSequence; may be null
     */
    public BirdRNG(final CharSequence seed)
    {
        for (int i = 0; i < 32; i++) {
            choice += (state[i] = CrossHash.Mist.predefined[i].hash(seed));
        }
    }

    public void setState(final int seed) {
        choice = 0;
        for (int i = 0; i < 32; i++) {
            choice += (state[i] = splitMix32(seed + i * 0x9E3779B9));
        }
    }

    public void setState(final long seed) {
        choice = 0;
        for (int i = 0; i < 32; i++) {
            choice += (state[i] = (int)LightRNG.determine(seed + i));
        }
    }

    public void setState(final int[] seed)
    {
        int len;
        if (seed == null || (len = seed.length) == 0) {
            for (int i = 0; i < 32; i++) {
                choice += (state[i] = splitMix32(0x632D978F + i * 0x9E3779B9));
            }
        } else if (len < 32) {
            for (int i = 0, s = 0; i < 32; i++, s++) {
                if(s == len) s = 0;
                choice += (state[i] ^= splitMix32(seed[s] + i * 0x9E3779B9));
            }
        } else {
            for (int i = 0, s = 0; s < len; s++, i = (i + 1) & 31) {
                choice += (state[i] ^= seed[s]);
            }
        }
    }

    @Override
    public final long nextLong() {
        return (state[(choice += 0xCBBC475B) & 31] += (state[choice >>> 28] += 0x9C7B7B99) >>> 1)
                * 0x632AE59B69B3C209L - choice;
    }

    public final int nextInt() {
        return (state[(choice += 0xCBBC475B) & 31] += (state[choice >>> 28] += 0x9C7B7B99) >>> 1);
    }

    @Override
    public final int next(final int bits) {
        return ((state[(choice += 0xCBBC475B) & 31] += (state[choice >>> 28] += 0x9C7B7B99) >>> 1) >>> (32 - bits)); //0x9E3779B9
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
        BirdRNG hr = new BirdRNG(state);
        hr.choice = choice;
        return hr;
    }

    @Override
    public String toString() {
        return "BirdRNG{" +
                "state=" + StringKit.hex(state) +
                ", choice=" + choice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BirdRNG birdRNG = (BirdRNG) o;

        return choice == birdRNG.choice && Arrays.equals(state, birdRNG.state);
    }

    @Override
    public int hashCode() {
        return 31 * choice + CrossHash.Wisp.hash(state);
    }
}
