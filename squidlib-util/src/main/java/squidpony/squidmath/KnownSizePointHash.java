package squidpony.squidmath;

/**
 * A very simple point hash meant only for fixed-size grids, and only for when the hash must be unique but does
 * not need to be randomized. If a hashed collection such as a Map has sufficient capacity to hold all items on
 * a grid without resizing, then this should never collide when only entering the items on that grid. If any of
 * the methods that need more than two dimensions of input are called, this delegates to {@link IntPointHash}.
 * <br>
 * Based heavily on GoRogue/SadConsole/TheSadRogue.Primitives and
 * <a href="https://github.com/thesadrogue/TheSadRogue.Primitives/blob/master/TheSadRogue.Primitives/PointHashers/KnownSizeHasher.cs">its KnownSizeHasher</a>.
 */
public class KnownSizePointHash extends IPointHash.IntImpl {

    public final int width;
    /**
     * Creates a KnownSizePointHash with a grid width of 80.
     */
    public KnownSizePointHash() {
        this(42, 80);
    }

    /**
     * Creates a KnownSizePointHash with the specified grid width; the grid height doesn't matter.
     * @param width how many cells wide the known grid is
     */
    public KnownSizePointHash(int width) {
        this(42, width);
    }

    /**
     * Creates a KnownSizePointHash with the specified state and grid width; the grid height doesn't matter.
     * @param state the state of the point hash, which has minimal effect on the result (it is XORed in)
     * @param width how many cells wide the known grid is
     */
    public KnownSizePointHash(int state, int width) {
        super(state);
        this.width = width;
    }

    @Override
    public int hashWithState(int x, int y, int state) {
        return x + width * y ^ state;
    }

    @Override
    public int hashWithState(int x, int y, int z, int state) {
        return IntPointHash.hashAll(x, y, z, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int state) {
        return IntPointHash.hashAll(x, y, z, w, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int state) {
        return IntPointHash.hashAll(x, y, z, w, u, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
        return IntPointHash.hashAll(x, y, z, w, u, v, state);
    }
}
