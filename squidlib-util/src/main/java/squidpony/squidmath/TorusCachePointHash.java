package squidpony.squidmath;

/**
 * A group of similar methods for getting hashes of points based on int coordinates in 2, 3, 4, 5, or 6 dimensions and
 * an int for state; here, points are considered toroidally wrapping, where the wrap happens at a power of two that is
 * no greater than 1024. This implementation stores 6 caches of 1024 numbers each, where each cache is used for a
 * different dimension, and XORs the values along with the seed before running through Thomas Wang's integer hash.
 */
public final class TorusCachePointHash extends IPointHash.IntImpl {

    protected final int[] cache;
    protected int wrap = 63;
    public TorusCachePointHash() {
        cache = new int[6 * 1024];
        setState(42);
    }

    public TorusCachePointHash(int state) {
        cache = new int[6 * 1024];
        setState(state);
    }

    public TorusCachePointHash(int state, int wrapAt) {
        cache = new int[6 * 1024];
        setState(state);
        wrap = HashCommon.nextPowerOfTwo(wrapAt) - 1;
    }

    @Override
    public void setState(int state) {
        this.state = state;
        SilkRNG rng = new SilkRNG(state);
        for (int i = 0; i < cache.length; i++) {
            cache[i] = rng.nextInt();
        }
    }

    /**
     * Thomas Wang's 2002 integer hash.
     * @param x any int
     * @return a usually different and very-scrambled int
     */
    public static int determineInt(int x){
        x += ~(x << 15);
        x ^= x >>> 10;
        x += x << 3;
        x ^= x >>> 6;
        x += ~(x << 11);
        x ^= x >>> 16;
        return x;
    }

    @Override
    public int hashWithState(int x, int y, int state) {
        return determineInt(state ^ cache[(x & wrap)] ^ cache[(y & wrap) + 1024]);
    }

    @Override
    public int hashWithState(int x, int y, int z, int state) {
        return determineInt(state ^ cache[(x & wrap)] ^ cache[(y & wrap) + 1024] ^ cache[(z & wrap) + 2048]);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int state) {
        return determineInt(state ^ cache[(x & wrap)] ^ cache[(y & wrap) + 1024] ^ cache[(z & wrap) + 2048]
                ^ cache[(w & wrap) + 3072]);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int state) {
        return determineInt(state ^ cache[(x & wrap)] ^ cache[(y & wrap) + 1024] ^ cache[(z & wrap) + 2048]
                ^ cache[(w & wrap) + 3072] ^ cache[(u & wrap) + 4096]);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
        return determineInt(state ^ cache[(x & wrap)] ^ cache[(y & wrap) + 1024] ^ cache[(z & wrap) + 2048]
                ^ cache[(w & wrap) + 3072] ^ cache[(u & wrap) + 4096] ^ cache[(v & wrap) + 5120]);

    }
}
