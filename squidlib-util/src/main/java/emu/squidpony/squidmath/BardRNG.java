package squidpony.squidmath;

import squidpony.StringKit;

import java.io.Serializable;
import java.util.Arrays;

import com.google.gwt.typedarrays.client.Int32ArrayNative;
import com.google.gwt.typedarrays.shared.Int32Array;

public class BardRNG implements RandomnessSource, Serializable {

    public static int splitMix32(int z) {
        z = (z ^ (z >>> 16)) * 0x85EBCA6B;
        z = (z ^ (z >>> 13)) * 0xC2B2AE35;
        return z ^ (z >>> 16);
    }

    public static long splitMix64(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private static final long serialVersionUID = 1L;
    private final Int32Array state = Int32ArrayNative.create(8);
    private int choice = 0;

    public BardRNG() {
        this((int) ((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    public BardRNG(final int seed) {
        setState(seed);
    }


    public BardRNG(final long seed) {
        setState(seed);
    }

    public BardRNG(final int[] seed) {
        int len, t;
        if (seed == null || (len = seed.length) == 0) {
            for (int i = 0; i < 128; i++) {
                choice += (t = splitMix32(0x632D978F + i * 0x9E3779B9));
                state.set(i, t);
            }
        } else if (len < 128) {
            for (int i = 0, s = 0; i < 128; i++, s++) {
                if (s == len) s = 0;
                choice += (t = state.get(i) ^ splitMix32(seed[s] + i * 0x9E3779B9));
                state.set(i, t);
            }
        } else {
            for (int i = 0, s = 0; s < len; s++, i = (i + 1) & 127) {
                choice += (t = state.get(i) ^ seed[s]);
                state.set(i, t);
            }
        }
    }

    public BardRNG(final CharSequence seed) {
        int t;
        for (int i = 0; i < 48; i++) {
            choice += (t = CrossHash.Mist.predefined[i].hash(seed));
            state.set(i, t);
            choice += (t = CrossHash.Mist.predefined[i].hash(seed));
            state.set(64 + i, t);
        }
        for (int i = 48; i < 64; i++) {
            choice += (t = CrossHash.Storm.predefined[i & 15].hash(seed));
            state.set(i, t);
            choice += (t = CrossHash.Storm.predefined[i & 15].hash(seed));
            state.set(64 + i, t);
        }
    }

    public void setState(final int seed) {
        choice = 0;
        int t;
        for (int i = 0; i < 128; i++) {
            choice += (t = splitMix32(seed + i * 0x9E3779B9));
            state.set(i, t);
        }
    }

    public void setState(final long seed) {
        choice = 0;
        int t;
        for (int i = 0; i < 128; i++) {
            choice += (t = (int) splitMix64(seed + i * 0x9E3779B9));
            state.set(i, t);
        }
    }

    public void setState(final int[] seed) {
        int len, t;
        if (seed == null || (len = seed.length) == 0) {
            for (int i = 0; i < 128; i++) {
                choice += (t = splitMix32(0x632D978F + i * 0x9E3779B9));
                state.set(i, t);
            }
        } else if (len < 128) {
            for (int i = 0, s = 0; i < 128; i++, s++) {
                if (s == len) s = 0;
                choice += (t = state.get(i) ^ splitMix32(seed[s] + i * 0x9E3779B9));
                state.set(i, t);
            }
        } else {
            if (len == 128) {
                choice = 0;
                state.set(seed);
                for (int i = 0; i < 128; i++) {
                    choice += state.get(i);
                }
            } else {
                for (int i = 0, s = 0; s < len; s++, i = (i + 1) & 127) {
                    choice += (t = state.get(i) ^ seed[s]);
                    state.set(i, t);
                }
            }
        }
    }

    @Override
    public final long nextLong() {
        final int c2 = (choice += 0x7345085E), c = c2 - 0xB9A2842F,
                i = c * 0x85157AF5 >>> 25, i2 = c2 * 0x85157AF5 >>> 25,
                s = state.get(i) + (c >>> (c >>> 28)) * 0x632BE5AB;
        state.set(i, s);
        final int s2 = state.get(i2) + (c2 >>> (c2 >>> 28)) * 0x632BE5AB;
        state.set(i2, s2);
        return (long) s << 32 ^ s2;
    }

    public final int nextInt() {
        final int c = (choice += 0xB9A2842F), i = c * 0x85157AF5 >>> 25,
                s = state.get(i) + (c >>> (c >>> 28)) * 0x632BE5AB;
        state.set(i, s);
        return s;
    }

    @Override
    public final int next(final int bits) {
        final int c = (choice += 0xB9A2842F), i = c * 0x85157AF5 >>> 25,
                s = state.get(i) + (c >>> (c >>> 28)) * 0x632BE5AB;
        state.set(i, s);
        return s >>> (32 - bits);
    }

    @Override
    public RandomnessSource copy() {
        BardRNG br = new BardRNG(0);
        br.state.set(state);
        br.choice = choice;
        return br;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(1056);
        sb.append("BardRNG{state=");
        for (int i = 0; i < 128; i++) {
            sb.append(StringKit.hex(state.get(i)));
        }
        return sb.append(", choice=").append(StringKit.hex(choice)).append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BardRNG bardRNG = (BardRNG) o;

        if (choice != bardRNG.choice)
            return false;
        int len = state.length();
        if (len != bardRNG.state.length())
            return false;

        for (int i = 0; i < len; i++) {
            if (state.get(i) != bardRNG.state.get(i))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0x9E3779B9, a = 0x632BE5AB;
        for (int i = 0; i < 128; i++) {
            result += (a ^= 0x85157AF5 * state.get(i));
        }
        return 31 * choice + (result * (a | 1) ^ (result >>> 11 | result << 21));
    }

}