package squidpony.squidmath;

import squidpony.annotation.Beta;

/**
 * An interface for point hashes that are statistically biased, as well as a holder for inner classes that implement
 * this. The point hashes here are mostly chosen because they are aesthetically interesting, at least on some of their
 * output bits.
 * <br>
 * Don't count on this class giving reliable output; it is marked Beta and will remain so. If you want to ensure a
 * particular behavior of a FlawedPointHash can be replicated, copy the implementation into your own code.
 * <br>
 * Created by Tommy Ettinger on 4/14/2020.
 */
@Beta
public interface FlawedPointHash extends IPointHash, IFlawed {
    /**
     * Produces hashes that show strong bias on one axis (usually the later axes matter more) and have nice-looking
     * patterns of dots. Better patterns are present in the higher bits.
     */
    class RugHash extends IPointHash.LongImpl implements FlawedPointHash {
        public RugHash() {
        }

        public RugHash(long state) {
            super(state);
        }

        public long getState() {
            return state;
        }

        public static int hashLongs(long x, long y, long s) {
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + y);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (y + x);
            return (int) (s >>> 32);
        }

        public static int hashLongs(long x, long y, long z, long s) {
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + z);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (z + y);
            return (int) (s >>> 32);
        }

        public static int hashLongs(long x, long y, long z, long w, long s) {
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + w);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
            w = (w + 0x9E3779B97F4A7C15L ^ w) * (z + y);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (w + z);
            return (int) (s >>> 32);
        }

        public static int hashLongs(long x, long y, long z, long w, long u, long s) {
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + u);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
            w = (w + 0x9E3779B97F4A7C15L ^ w) * (z + y);
            u = (u + 0x9E3779B97F4A7C15L ^ u) * (w + z);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (u + w);
            return (int) (s >>> 32);
        }

        public static int hashLongs(long x, long y, long z, long w, long u, long v, long s) {
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + v);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
            w = (w + 0x9E3779B97F4A7C15L ^ w) * (z + y);
            u = (u + 0x9E3779B97F4A7C15L ^ u) * (w + z);
            v = (v + 0x9E3779B97F4A7C15L ^ v) * (u + w);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (v + u);
            return (int) (s >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int state) {
            return hashLongs(x, y, state);
        }

        @Override
        public int hashWithState(int x, int y, int z, int state) {
            return hashLongs(x, y, z, state);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int state) {
            return hashLongs(x, y, z, w, state);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int state) {
            return hashLongs(x, y, z, w, u, state);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
            return hashLongs(x, y, z, w, u, v, state);
        }
    }

    /**
     * Extremely flawed if you're using this as a point hash, but meant to be aesthetically interesting, this produces
     * different symmetrical patterns in squares, as if on a quilt.
     */
    class QuiltHash extends IPointHash.LongImpl implements FlawedPointHash {
        private int size = 6;
        private long mask = (1L << size) - 1L;
        public QuiltHash() {
        }

        /**
         * The same as calling {@link #QuiltHash(long, int)} with size = 64 .
         * @param state the state to use for hashing when no state is otherwise specified
         */
        public QuiltHash(long state) {
            super(state);
        }

        /**
         * Creates a QuiltHash with the given state and a size for each "quilt square" equal to size, rounded up to the
         * next power of two if it is not already a power of two. These quilt squares become quilt cubes in 3D.
         * @param state the state to use for hashing when no state is otherwise specified
         * @param size the size of each quilt square/cube, in no particular unit, but larger sizes can be more complex
         */
        public QuiltHash(long state, int size) {
            super(state);
            setSize(size);
        }

        public long getState() {
            return state;
        }

        public int getSize() {
            return 1 << size;
        }

        public void setSize(int size) {
            this.size = 32 - Integer.numberOfLeadingZeros(Math.max(1, size));
            mask = (1L << this.size) - 1L;
        }

        public long hashLongs(long x, long y, long s) {
            s ^= (x >> size) * 0xC13FA9A902A6328FL;
            s ^= (y >> size) * 0x91E10DA5C79E7B1DL;
            x *= x;
            y *= y;
            x = x >>> 1 & mask;
            y = y >>> 1 & mask;
            long t;
            if (x < y) {
                t = x;
                x = y;
                y = t;
            }
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + y);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (y + x);
            return s;
        }

        public long hashLongs(long x, long y, long z, long s) {
//            return hashLongs(x, hashLongs(y, z, s), s);
            s ^= (x >> size) * 0xD1B54A32D192ED03L;
            s ^= (y >> size) * 0xABC98388FB8FAC03L;
            s ^= (z >> size) * 0x8CB92BA72F3D8DD7L;
            x = x * x >>> 1 & mask;
            y = y * y >>> 1 & mask;
            z = z * z >>> 1 & mask;
            long t;
            if (x < y) {
                t = x;
                x = y;
                y = t;
            }
            if(x < z){
                t = x;
                x = z;
                z = t;
            }
            if(y < z){
                t = y;
                y = z;
                z = t;
            }
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + z);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (z + y);
            return s;
        }

        public long hashLongs(long x, long y, long z, long w, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, w, s), s), s);
        }

        public long hashLongs(long x, long y, long z, long w, long u, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, hashLongs(w, u, s), s), s), s);
        }
        
        public long hashLongs(long x, long y, long z, long w, long u, long v, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, hashLongs(w, hashLongs(u, v, s), s), s), s), s);
        }

        @Override
        public int hashWithState(int x, int y, int state) {
            return (int)(hashLongs(x, y, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int state) {
            return (int)(hashLongs(x, y, z, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int state) {
            return (int)(hashLongs(x, y, z, w, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int state) {
            return (int)(hashLongs(x, y, z, w, u, state) >>> 32);
        }
        
        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
            return (int)(hashLongs(x, y, z, w, u, v, state) >>> 32);
        }
    }

    /**
     * Very similar to {@link QuiltHash}, but this doesn't change the pattern in different large squares, and instead
     * repeats a square or cube of symmetric and patterned results over and over (so it can be tiled).
     */
    class CubeHash extends IPointHash.LongImpl implements FlawedPointHash {
        private int size = 6;
        private long mask = (1L << size) - 1L;
        public CubeHash() {
        }

        public CubeHash(long state) {
            super(state);
        }

        public CubeHash(long state, int size) {
            super(state);
            setSize(size);
        }

        public long getState() {
            return state;
        }

        public int getSize() {
            return 1 << size;
        }

        public void setSize(int size) {
            this.size = 32 - Integer.numberOfLeadingZeros(Math.max(1, size));
            mask = (1L << this.size) - 1L;
        }

        public long hashLongs(long x, long y, long s) {
            x &= mask;
            y &= mask;
            x *= x * 0xC13FA9A902A6328FL;
            y *= y * 0x91E10DA5C79E7B1DL;
            x &= mask;
            y &= mask;
            long t;
            if (x < y) {
                t = x;
                x = y;
                y = t;
            }
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + y);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (y + x);
            return s;
        }

        public long hashLongs(long x, long y, long z, long s) {
            x &= mask;
            y &= mask;
            z &= mask;
            x *= x * 0xD1B54A32D192ED03L;
            y *= y * 0xABC98388FB8FAC03L;
            z *= z * 0x8CB92BA72F3D8DD7L;
            x &= mask;
            y &= mask;
            z &= mask;
            long t;
            if (x < y) {
                t = x;
                x = y;
                y = t;
            }
            if(x < z){
                t = x;
                x = z;
                z = t;
            }
            if(y < z){
                t = y;
                y = z;
                z = t;
            }
            x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + z);
            y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
            z = (z + 0x9E3779B97F4A7C15L ^ z) * (y + x);
            s = (s + 0x9E3779B97F4A7C15L ^ s) * (z + y);
            return s;
        }

        public long hashLongs(long x, long y, long z, long w, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, w, s), s), s);
        }

        public long hashLongs(long x, long y, long z, long w, long u, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, hashLongs(w, u, s), s), s), s);
        }

        public long hashLongs(long x, long y, long z, long w, long u, long v, long s) {
            return hashLongs(x, hashLongs(y, hashLongs(z, hashLongs(w, hashLongs(u, v, s), s), s), s), s);
        }

        @Override
        public int hashWithState(int x, int y, int state) {
            return (int)(hashLongs(x, y, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int state) {
            return (int)(hashLongs(x, y, z, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int state) {
            return (int)(hashLongs(x, y, z, w, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int state) {
            return (int)(hashLongs(x, y, z, w, u, state) >>> 32);
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
            return (int)(hashLongs(x, y, z, w, u, v, state) >>> 32);
        }
    }

    /**
     * FNV32a is OK as a hash for bytes when used in some hash tables, but it has major issues on its low-order bits
     * when used as a point hash (the high bits aren't much better). Unfortunately, it is not aesthetically pleasing as
     * a point hash. Some usages might be able to use it to apply a grimy, glitchy effect.
     */
    class FNVHash extends IntImpl implements FlawedPointHash {

        public FNVHash() {
            super();
        }

        public FNVHash(int state) {
            super(state);
        }

        public int getState() {
            return state;
        }
        @Override
        public int hashWithState(int x, int y, int state) {
            return ((state ^ 0x811c9dc5 ^ x) * 0x1000193 ^ y) * 0x1000193;
        }

        @Override
        public int hashWithState(int x, int y, int z, int state) {
            return (((state ^ 0x811c9dc5 ^ x) * 0x1000193 ^ y) * 0x1000193 ^ z) * 0x1000193;
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int state) {
            return ((((state ^ 0x811c9dc5 ^ x) * 0x1000193 ^ y) * 0x1000193 ^ z) * 0x1000193 ^ w) * 0x1000193;
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int state) {
            return (((((state ^ 0x811c9dc5 ^ x) * 0x1000193 ^ y) * 0x1000193 ^ z) * 0x1000193
                    ^ w) * 0x1000193 ^ u) * 0x1000193;
        }

        @Override
        public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
            return ((((((state ^ 0x811c9dc5 ^ x) * 0x1000193 ^ y) * 0x1000193 ^ z) * 0x1000193
                    ^ w) * 0x1000193 ^ u) * 0x1000193 ^ v) * 0x1000193;
        }
    }
}
