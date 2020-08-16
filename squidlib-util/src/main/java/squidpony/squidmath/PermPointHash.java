package squidpony.squidmath;

/**
 * A mid-to-low quality point hash that uses a similar (not identical) technique to what OpenSimplex2 uses, with
 * a permutation array created at construction. To get an x,y,z point, this looks up x in the permutation array (using
 * only the bottom 11 bits of x), which gets an 11-bit number, then XORs that with y, masks to 11 bits, and looks that
 * up again in the same permutation array, finishing by XORing with z and masking to 11 bits. To match the behavior of
 * other IPointHash implementations, this finishes by attempting to include variable state using:
 * {@code (((...) - state ^ 0xD1B54A35) * 0x125493 + state ^ 0xD1B54A35) * 0x125493;}, which also pushes the usable bits
 * up throughout the returned int, instead of just in the lowest 11 bits.
 * <br>
 * This is fast in OpenSimplex2, but the quality is so lackluster here, even with extra operations, that this isn't
 * recommended at all.
 */
public class PermPointHash extends IPointHash.IntImpl {

    public PermPointHash() {
        this(1234567890);
    }

    private final short[] perm = new short[2048];

    public PermPointHash(int state) {
        super(state);
        short[] source = new short[2048];
        for (short i = 0; i < 2048; i++)
            source[i] = i;
        long s = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L;
        for (int i = 2047; i >= 0; i--) {
            s = (s ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L;
            int r = (int) ((s >>> 32) * i >>> 32);
            perm[i] = source[r];
            source[r] = source[i];
        }
    }

    public int getState() {
        return state;
    }

    @Override
    public int hashWithState(int x, int y, int state) {
        return (((perm[x & 2047] ^ y & 2047) - state ^ 0xD1B54A35) * 0x125493 + state ^ 0xD1B54A35) * 0x125493;
    }

    @Override
    public int hashWithState(int x, int y, int z, int state) {
        return (((perm[perm[x & 2047] ^ y & 2047] ^ z & 2047) - state ^ 0xD1B54A35) * 0x125493 + state ^ 0xD1B54A35) * 0x125493;
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int state) {
        return (((perm[perm[perm[x & 2047] ^ y & 2047] ^ z & 2047] ^ w & 2047) - state ^ 0xD1B54A35) * 0x125493 + state ^ 0xD1B54A35) * 0x125493;
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int state) {
        return (((perm[perm[perm[perm[x & 2047] ^ y & 2047] ^ z & 2047] ^ w & 2047] ^ u & 2047) - state ^ 0xD1B54A35) * 0x125493 + state ^ 0xD1B54A35) * 0x125493;
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
        return (((perm[perm[perm[perm[perm[x & 2047] ^ y & 2047] ^ z & 2047] ^ w & 2047] ^ u & 2047] ^ v & 2047) - state ^ 0xD1B54A35) * 0x125493 + state ^ 0xD1B54A35) * 0x125493;
    }
}
