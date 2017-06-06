package squidpony.squidmath;

import squidpony.StringKit;
import squidpony.annotation.Beta;

import java.util.Arrays;

/**
 * A mix of something like the fast algorithm of LapRNG with the larger state size of LongPeriodRNG, in the hopes of
 * improving Lap's period without seriously reducing speed. It sorta works, since {@link #nextLong()} is faster than
 * {@link LongPeriodRNG#nextLong()} by a fair amount, but only "sorta" because methods like {@link #next(int)} slow
 * down when they are called by classes like RNG, though not as severely as with the related {@link HerdRNG}. There's
 * some behavior of the JVM at play here, and it may be different across machines and installations. This has 1024 bits
 * of {@link #state} in an long array with 16 elements, plus 32 bits of semi-state in the {@link #choice} field (used to
 * decide which of the 16 longs in state to update and query). The period is known to be fairly good, and must be at
 * least (2 to the 80) but is almost certainly much higher, since testing a variant of this with significantly fewer
 * bits of state (using 4 shorts instead of 16 longs, with the same int for choice) still had a period greater than 2 to
 * the 38, implying the period here may be greater than (2 to the 512), and potentially as high as (2 to the 1024),
 * though this last possibility is very unlikely.
 * Created by Tommy Ettinger on 6/4/2017.
 */
@Beta
public class HordeRNG implements RandomnessSource {
    public final long[] state = new long[16];
    public int choice = 0;
    public HordeRNG() {
        this((long) ((Math.random() * 2.0 - 1.0) * 0x8000000000000L)
                ^ (long) ((Math.random() * 2.0 - 1.0) * 0x8000000000000000L));
    }

    public HordeRNG(final long seed) {
        for (int i = 0; i < 16; i++) {
            state[i] = LightRNG.determine(seed + i);
        }
        choice = (int) (state[0] ^ seed);
    }

    public HordeRNG(final long a, final long b, final long c, final long d,
                    final long e, final long f, final long g, final long h,
                    final long i, final long j, final long k, final long l,
                    final long m, final long n, final long o, final long p)
    {
        state[0]  = a;
        state[1]  = b;
        state[2]  = c;
        state[3]  = d;
        state[4]  = e;
        state[5]  = f;
        state[6]  = g;
        state[7]  = h;
        state[8]  = i;
        state[9]  = j;
        state[10] = k;
        state[11] = l;
        state[12] = m;
        state[13] = n;
        state[14] = o;
        state[15] = p;
        choice = (int)(a ^ ~p);
    }

    public HordeRNG(final long[] seed) {
        int len;
        if (seed == null || (len = seed.length) == 0) {
            for (int i = 0; i < 16; i++) {
                state[i] = LightRNG.determine(0xC6BC279692B5C483L + i);
            }
            choice = (int) (state[0]);
        } else if (len < 16) {
            for (int i = 0, s = 0; i < 16; i++, s++) {
                if(s == len) s = 0;
                state[i] ^= seed[s];
            }
            choice = (int) (state[0] * len);
        } else {
            for (int i = 0, s = 0; s < len; s++, i = (i + 1) & 15) {
                state[i] ^= seed[s];
            }
            choice = (int) (state[0] * len);
        }
    }

    public void setState(final long seed) {
        for (int i = 0; i < 16; i++) {
            state[i] = LightRNG.determine(seed + i);
        }
        choice = (int) (state[0] ^ seed);
    }

    public final long nextLong() {
        final int c = (choice += 0x9CBC278D);
        return (state[c & 15] += (state[c >>> 28] >>> 1) + 0x8E3779B97F4A7C15L);
        // 0x632AE59B69B3C209L

        //        + high ^ (0x9E3779B97F4A7C15L * ((high += low & (low += 0xAB79B96DCD7FE75EL)) >> 20))); // thunder
        //        + ((low = (low >>> 1 ^ (-(low & 1L) & 0x6000000000000000L)))) // LFSR, 63-bit
        ///        ^ (high = high >>> 1 ^ (-(high & 1L) & 0xD800000000000000L))); // LFSR, 64-bit;
    }

    public final int nextInt() {
        final int c = (choice += 0x9CBC278D);
        return (int)(state[c & 15] += (state[c >>> 28] >>> 1) + 0x8E3779B97F4A7C15L);
        //0x9E3779B97F4A7C15L
        //0xBE377BB97F4A7C17L
        /*
        return (int) ((state1 += (state0 += 0x632AE59B69B3C209L) * 0x9E3779B97F4A7C15L)
                + (low = (low >>> 1 ^ (-(low & 1L) & 0x6000000000000000L))) // LFSR, 63-bit
                ^ (high = high >>> 1 ^ (-(high & 1L) & 0xD800000000000000L))); // LFSR, 64-bit;
        */
    }

    public final int next(final int bits) {
        final int c = (choice += 0x9CBC278D);
        return (int) ((state[c & 15] += (state[c >>> 28] >>> 1) + 0x8E3779B97F4A7C15L) >>> (64 - bits));
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
        HordeRNG hr = new HordeRNG(state);
        hr.choice = choice;
        return hr;
    }

    @Override
    public String toString() {
        return "HordeRNG{" +
                "state=" + StringKit.hex(state) +
                ", choice=" + choice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HordeRNG hordeRNG = (HordeRNG) o;

        if (choice != hordeRNG.choice) return false;
        return Arrays.equals(state, hordeRNG.state);
    }

    @Override
    public int hashCode() {
        return 31 * choice + CrossHash.Wisp.hash(state);
    }
}
